package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;

public record GenericMachineRecipeExtraData(Optional<PlasmaForge> plasmaForge,
                                            Optional<Fusion> fusion) {
    public static final GenericMachineRecipeExtraData EMPTY =
            new GenericMachineRecipeExtraData(Optional.empty(), Optional.empty());

    public GenericMachineRecipeExtraData {
        plasmaForge = plasmaForge == null ? Optional.empty() : plasmaForge;
        fusion = fusion == null ? Optional.empty() : fusion;
    }

    public static GenericMachineRecipeExtraData fromJson(JsonObject json) {
        Optional<PlasmaForge> plasma = readPlasmaForge(json);
        Optional<Fusion> fusion = readFusion(json);
        return plasma.isEmpty() && fusion.isEmpty()
                ? EMPTY
                : new GenericMachineRecipeExtraData(plasma, fusion);
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
    }

    public static GenericMachineRecipeExtraData fromNetwork(FriendlyByteBuf buffer) {
        Optional<PlasmaForge> plasma = buffer.readBoolean()
                ? Optional.of(new PlasmaForge(buffer.readVarLong()))
                : Optional.empty();
        Optional<Fusion> fusion = buffer.readBoolean()
                ? Optional.of(new Fusion(buffer.readVarLong(), buffer.readVarLong(), buffer.readDouble(),
                        buffer.readFloat(), buffer.readFloat(), buffer.readFloat()))
                : Optional.empty();
        return plasma.isEmpty() && fusion.isEmpty()
                ? EMPTY
                : new GenericMachineRecipeExtraData(plasma, fusion);
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
        require(json, "ignitionTemp");
        require(json, "outputTemp");
        require(json, "outputFlux");
        require(json, "r");
        require(json, "g");
        require(json, "b");
        return Optional.of(new Fusion(
                json.get("ignitionTemp").getAsLong(),
                json.get("outputTemp").getAsLong(),
                json.get("outputFlux").getAsDouble(),
                json.get("r").getAsFloat(),
                json.get("g").getAsFloat(),
                json.get("b").getAsFloat()));
    }

    private static boolean hasFusionFields(JsonObject json) {
        return json.has("outputTemp")
                || json.has("outputFlux")
                || json.has("r")
                || json.has("g")
                || json.has("b");
    }

    private static void require(JsonObject json, String key) {
        if (!json.has(key)) {
            throw new JsonSyntaxException("Missing HBM fusion recipe extra field: " + key);
        }
    }

    public record PlasmaForge(long ignitionTemp) {
    }

    public record Fusion(long ignitionTemp, long outputTemp, double outputFlux, float r, float g, float b) {
    }
}
