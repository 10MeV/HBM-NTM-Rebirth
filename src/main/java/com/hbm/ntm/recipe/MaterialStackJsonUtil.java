package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import java.util.Locale;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

final class MaterialStackJsonUtil {
    static JsonObject toJson(MaterialStack stack) {
        JsonObject object = new JsonObject();
        object.addProperty("material", stack.material.names[0]);
        object.addProperty("amount", stack.amount);
        return object;
    }

    static MaterialStack readRequired(JsonElement element, String name) {
        MaterialStack stack = readOptional(element, name);
        if (stack == null || stack.isEmpty()) {
            throw new JsonSyntaxException("Missing or empty material stack in " + name);
        }
        return stack;
    }

    @Nullable
    static MaterialStack readOptional(@Nullable JsonElement element, String name) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        MaterialStack stack;
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            stack = read(GsonHelper.getAsString(object, "material"),
                    GsonHelper.getAsInt(object, "amount", 0), name);
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() < 2) {
                throw new JsonSyntaxException("Legacy material array for " + name + " needs material and amount");
            }
            stack = read(array.get(0).getAsString(), array.get(1).getAsInt(), name);
        } else {
            throw new JsonSyntaxException("Expected object or legacy array for " + name + ": " + element);
        }
        if (stack == null || stack.isEmpty()) {
            throw new JsonSyntaxException("Invalid material stack in " + name + ": " + element);
        }
        return stack;
    }

    @Nullable
    static MaterialStack readNetwork(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return null;
        }
        NTMMaterial material = materialByName(buffer.readUtf());
        int amount = buffer.readVarInt();
        return material == null || amount <= 0 ? null : new MaterialStack(material, amount);
    }

    static void writeNetwork(FriendlyByteBuf buffer, @Nullable MaterialStack stack) {
        if (stack == null || stack.isEmpty()) {
            buffer.writeBoolean(false);
            return;
        }
        buffer.writeBoolean(true);
        buffer.writeUtf(stack.material.names[0]);
        buffer.writeVarInt(stack.amount);
    }

    private static MaterialStack read(String materialName, int amount, String name) {
        NTMMaterial material = materialByName(materialName);
        if (material == null) {
            throw new JsonSyntaxException("Unknown material '" + materialName + "' in " + name);
        }
        if (amount <= 0) {
            throw new JsonSyntaxException("Invalid material amount " + amount + " in " + name);
        }
        return new MaterialStack(material, amount);
    }

    @Nullable
    private static NTMMaterial materialByName(String name) {
        NTMMaterial material = Mats.matByName.get(name);
        if (material != null) {
            return material;
        }
        String normalized = name.toLowerCase(Locale.ROOT);
        for (NTMMaterial candidate : Mats.orderedList) {
            for (String candidateName : candidate.names) {
                if (candidateName.equalsIgnoreCase(name)
                        || candidateName.toLowerCase(Locale.ROOT).equals(normalized)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private MaterialStackJsonUtil() {
    }
}
