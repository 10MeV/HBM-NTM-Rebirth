package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.pollution.PollutionType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;

public record GenericMachineRecipeExtraData(Optional<PlasmaForge> plasmaForge,
                                            Optional<Fusion> fusion,
                                            Optional<Pollution> pollution) {
    public static final GenericMachineRecipeExtraData EMPTY =
            new GenericMachineRecipeExtraData(Optional.empty(), Optional.empty(), Optional.empty());

    public GenericMachineRecipeExtraData {
        plasmaForge = plasmaForge == null ? Optional.empty() : plasmaForge;
        fusion = fusion == null ? Optional.empty() : fusion;
        pollution = pollution == null ? Optional.empty() : pollution;
    }

    public static GenericMachineRecipeExtraData fromJson(JsonObject json) {
        Optional<PlasmaForge> plasma = readPlasmaForge(json);
        Optional<Fusion> fusion = readFusion(json);
        Optional<Pollution> pollution = readPollution(json);
        return plasma.isEmpty() && fusion.isEmpty() && pollution.isEmpty()
                ? EMPTY
                : new GenericMachineRecipeExtraData(plasma, fusion, pollution);
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
        pollution.ifPresent(pollutionData -> {
            json.addProperty("pollutionType", pollutionData.type().name());
            json.addProperty("pollutionAmount", pollutionData.amount());
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
        buffer.writeBoolean(pollution.isPresent());
        pollution.ifPresent(pollutionData -> {
            buffer.writeEnum(pollutionData.type());
            buffer.writeFloat(pollutionData.amount());
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
        Optional<Pollution> pollution = buffer.readBoolean()
                ? Optional.of(new Pollution(buffer.readEnum(PollutionType.class), buffer.readFloat()))
                : Optional.empty();
        return plasma.isEmpty() && fusion.isEmpty() && pollution.isEmpty()
                ? EMPTY
                : new GenericMachineRecipeExtraData(plasma, fusion, pollution);
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

    private static Optional<Pollution> readPollution(JsonObject json) {
        if (!json.has("pollutionType") && !json.has("pollutionAmount")) {
            return Optional.empty();
        }
        requirePollution(json, "pollutionType");
        requirePollution(json, "pollutionAmount");
        float amount = json.get("pollutionAmount").getAsFloat();
        if (amount == 0.0F) {
            return Optional.empty();
        }
        PollutionType type = PollutionType.byName(json.get("pollutionType").getAsString());
        if (type == null) {
            throw new JsonSyntaxException("Invalid HBM recipe pollutionType: "
                    + json.get("pollutionType").getAsString());
        }
        return Optional.of(new Pollution(type, amount));
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

    private static void requirePollution(JsonObject json, String key) {
        if (!json.has(key)) {
            throw new JsonSyntaxException("Missing HBM recipe pollution extra field: " + key);
        }
    }

    public record PlasmaForge(long ignitionTemp) {
    }

    public record Fusion(long ignitionTemp, long outputTemp, double outputFlux, float r, float g, float b) {
    }

    public record Pollution(PollutionType type, float amount) {
    }
}
