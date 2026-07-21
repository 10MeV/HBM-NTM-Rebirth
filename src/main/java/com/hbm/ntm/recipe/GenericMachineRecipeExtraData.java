package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hbm.inventory.material.Mats.MaterialStack;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record GenericMachineRecipeExtraData(Optional<PlasmaForge> plasmaForge,
                                            Optional<Fusion> fusion,
                                            List<MaterialStack> arcMaterialOutputs) {
    public static final GenericMachineRecipeExtraData EMPTY =
            new GenericMachineRecipeExtraData(Optional.empty(), Optional.empty(), List.of());

    public GenericMachineRecipeExtraData {
        plasmaForge = plasmaForge == null ? Optional.empty() : plasmaForge;
        fusion = fusion == null ? Optional.empty() : fusion;
        arcMaterialOutputs = copyMaterialStacks(arcMaterialOutputs);
    }

    public GenericMachineRecipeExtraData(Optional<PlasmaForge> plasmaForge,
            Optional<Fusion> fusion) {
        this(plasmaForge, fusion, List.of());
    }

    public GenericMachineRecipeExtraData withArcMaterialOutputs(List<MaterialStack> outputs) {
        return new GenericMachineRecipeExtraData(plasmaForge, fusion, outputs);
    }

    public static GenericMachineRecipeExtraData fromJson(JsonObject json) {
        Optional<PlasmaForge> plasma = readPlasmaForge(json);
        Optional<Fusion> fusion = readFusion(json);
        List<MaterialStack> arcMaterialOutputs = readArcMaterialOutputs(json);
        return plasma.isEmpty() && fusion.isEmpty() && arcMaterialOutputs.isEmpty()
                ? EMPTY
                : new GenericMachineRecipeExtraData(plasma, fusion, arcMaterialOutputs);
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        writeToJson(object);
        return object;
    }

    public void writeToJson(JsonObject json) {
        plasmaForge.ifPresent(plasma -> json.addProperty("ignitionTemp", plasma.ignitionTemp()));
        fusion.ifPresent(fusionData -> {
            json.addProperty("ignitionTemp", fusionData.ignitionTemp());
            json.addProperty("outputTemp", fusionData.outputTemp());
            json.addProperty("outputFlux", fusionData.outputFlux());
            json.addProperty("r", fusionData.r());
            json.addProperty("g", fusionData.g());
            json.addProperty("b", fusionData.b());
        });
        if (!arcMaterialOutputs.isEmpty()) {
            JsonArray outputs = new JsonArray();
            for (MaterialStack stack : arcMaterialOutputs) {
                outputs.add(MaterialStackJsonUtil.toJson(stack));
            }
            json.add("arc_material_outputs", outputs);
        }
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeBoolean(plasmaForge.isPresent());
        plasmaForge.ifPresent(plasma -> buffer.writeVarLong(plasma.ignitionTemp()));
        buffer.writeBoolean(fusion.isPresent());
        fusion.ifPresent(fusionData -> {
            buffer.writeVarLong(fusionData.ignitionTemp());
            buffer.writeVarLong(fusionData.outputTemp());
            buffer.writeDouble(fusionData.outputFlux());
            buffer.writeFloat(fusionData.r());
            buffer.writeFloat(fusionData.g());
            buffer.writeFloat(fusionData.b());
        });
        buffer.writeCollection(arcMaterialOutputs, MaterialStackJsonUtil::writeNetwork);
    }

    public static GenericMachineRecipeExtraData fromNetwork(FriendlyByteBuf buffer) {
        Optional<PlasmaForge> plasma = buffer.readBoolean()
                ? Optional.of(new PlasmaForge(buffer.readVarLong()))
                : Optional.empty();
        Optional<Fusion> fusion = buffer.readBoolean()
                ? Optional.of(new Fusion(buffer.readVarLong(), buffer.readVarLong(), buffer.readDouble(),
                        buffer.readFloat(), buffer.readFloat(), buffer.readFloat()))
                : Optional.empty();
        List<MaterialStack> arcMaterialOutputs = buffer.readList(MaterialStackJsonUtil::readNetwork);
        return plasma.isEmpty() && fusion.isEmpty() && arcMaterialOutputs.isEmpty()
                ? EMPTY
                : new GenericMachineRecipeExtraData(plasma, fusion, arcMaterialOutputs);
    }

    private static Optional<PlasmaForge> readPlasmaForge(JsonObject json) {
        if (hasFusionFields(json)) {
            return Optional.empty();
        }
        if (!json.has("ignitionTemp")) {
            return Optional.empty();
        }
        return Optional.of(new PlasmaForge(json.get("ignitionTemp").getAsLong()));
    }

    private static Optional<Fusion> readFusion(JsonObject json) {
        if (!hasFusionFields(json)) {
            return Optional.empty();
        }
        requireFusion(json, "ignitionTemp");
        requireFusion(json, "outputTemp");
        requireFusion(json, "outputFlux");
        requireFusion(json, "r");
        requireFusion(json, "g");
        requireFusion(json, "b");
        return Optional.of(new Fusion(
                json.get("ignitionTemp").getAsLong(),
                json.get("outputTemp").getAsLong(),
                json.get("outputFlux").getAsDouble(),
                json.get("r").getAsFloat(),
                json.get("g").getAsFloat(),
                json.get("b").getAsFloat()));
    }

    private static List<MaterialStack> readArcMaterialOutputs(JsonObject json) {
        if (!json.has("arc_material_outputs")) {
            return List.of();
        }
        JsonArray array = json.getAsJsonArray("arc_material_outputs");
        List<MaterialStack> outputs = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            outputs.add(MaterialStackJsonUtil.readRequired(element, "arc_material_outputs[" + i + "]"));
        }
        return outputs;
    }

    private static List<MaterialStack> copyMaterialStacks(List<MaterialStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return List.of();
        }
        List<MaterialStack> copy = new ArrayList<>();
        for (MaterialStack stack : stacks) {
            if (stack != null && !stack.isEmpty()) {
                copy.add(stack.copy());
            }
        }
        return List.copyOf(copy);
    }

    private static boolean hasFusionFields(JsonObject json) {
        return json.has("outputTemp")
                || json.has("outputFlux")
                || json.has("r")
                || json.has("g")
                || json.has("b");
    }

    private static void requireFusion(JsonObject json, String key) {
        if (!json.has(key)) {
            throw new JsonSyntaxException("Missing HBM fusion recipe extra field: " + key);
        }
    }

    public record PlasmaForge(long ignitionTemp) {
    }

    public record Fusion(long ignitionTemp, long outputTemp, double outputFlux, float r, float g, float b) {
    }

}
