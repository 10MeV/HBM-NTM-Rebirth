package com.hbm.ntm.fluid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hbm.ntm.HbmNtm;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HbmFluidTypeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "hbmFluidTypes.json";
    private static volatile LoadReport report = new LoadReport(false, false, 0, 0, List.of());

    public static LoadReport initialize(Path configDir) {
        Path hbmDir = configDir.resolve("hbm");
        Path config = hbmDir.resolve(CONFIG_FILE);
        boolean createdDefault = false;
        try {
            Files.createDirectories(hbmDir);
            if (Files.notExists(config)) {
                writeDefault(config);
                createdDefault = true;
            }
            HbmFluids.removeCustomFluids();
            try (Reader reader = Files.newBufferedReader(config)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                return remember(readCustomFluids(json, createdDefault));
            }
        } catch (IOException | RuntimeException ex) {
            HbmNtm.LOGGER.warn("Failed to load HBM custom fluid type config, using built-in fluid types.", ex);
            HbmFluids.removeCustomFluids();
            return remember(new LoadReport(false, createdDefault, 0, 1, List.of("failed to read " + CONFIG_FILE)));
        }
    }

    public static LoadReport loadReport() {
        return report;
    }

    private static LoadReport remember(LoadReport current) {
        report = current;
        return current;
    }

    private static LoadReport readCustomFluids(JsonObject root, boolean createdDefault) {
        List<String> warnings = new ArrayList<>();
        int loaded = 0;
        int skipped = 0;
        if (root == null) {
            return new LoadReport(true, createdDefault, 0, 1, List.of("empty root"));
        }

        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String name = entry.getKey();
            if (!entry.getValue().isJsonObject()) {
                skipped++;
                warnings.add(name + " is not an object");
                continue;
            }
            try {
                JsonObject object = entry.getValue().getAsJsonObject();
                int id = intValue(object, "id");
                String normalizedName = normalize(name);
                if (HbmFluids.hasRegisteredName(normalizedName)) {
                    skipped++;
                    warnings.add(name + " name collides with an existing HBM fluid");
                    continue;
                }
                if (HbmFluids.hasRegisteredId(id)) {
                    skipped++;
                    warnings.add(name + " id " + id + " collides with an existing HBM fluid");
                    continue;
                }

                String displayName = stringValue(object, "name", stringValue(object, "displayName", name));
                int color = intValue(object, "color");
                int tint = intValue(object, "tint", color);
                int poison = intValue(object, "p", 0);
                int flammability = intValue(object, "f", 0);
                int reactivity = intValue(object, "r", 0);
                FluidSymbol symbol = enumValue(FluidSymbol.class, stringValue(object, "symbol", "NONE"), FluidSymbol.NONE);
                String texture = stringValue(object, "texture", name.toLowerCase(Locale.US));
                int temperature = intValue(object, "temperature", FluidType.ROOM_TEMPERATURE);

                boolean renderTankWithTint = booleanValue(object, "renderTankWithTint", true);
                FluidType type = HbmFluids.registerCustom(normalizedName, id, color, poison, flammability,
                        reactivity, symbol, texture, tint, displayName)
                        .setTemperature(temperature)
                        .setRenderTankWithTint(renderTankWithTint);
                JsonObject traits = object(object, "traits");
                if (traits != null) {
                    HbmFluidTraitConfig.TraitParseResult parsed = HbmFluidTraitConfig.readTraitBlock(type.getName(), traits);
                    type.setTraits(parsed.traits());
                    skipped += parsed.skipped();
                    warnings.addAll(parsed.warnings());
                }
                loaded++;
            } catch (RuntimeException ex) {
                if (ex instanceof HbmFluidJsonUtil.UnknownFluidReferenceException) {
                    throw ex;
                }
                skipped++;
                warnings.add(name + " failed: " + ex.getMessage());
            }
        }
        return new LoadReport(true, createdDefault, loaded, skipped, warnings);
    }

    private static void writeDefault(Path config) throws IOException {
        JsonObject root = new JsonObject();
        JsonObject demo = new JsonObject();
        demo.addProperty("name", "Custom Fluid Demo");
        demo.addProperty("id", 1000);
        demo.addProperty("color", 0xff0000);
        demo.addProperty("tint", 0xff0000);
        demo.addProperty("p", 1);
        demo.addProperty("f", 2);
        demo.addProperty("r", 0);
        demo.addProperty("symbol", FluidSymbol.OXIDIZER.name());
        demo.addProperty("texture", "custom_water");
        demo.addProperty("temperature", FluidType.ROOM_TEMPERATURE);
        root.add("CUSTOM_DEMO", demo);
        try (Writer writer = Files.newBufferedWriter(config)) {
            GSON.toJson(root, writer);
        }
    }

    private static String stringValue(JsonObject object, String key, String fallback) {
        JsonElement element = object.get(key);
        return element == null ? fallback : element.getAsString();
    }

    private static int intValue(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null) {
            throw new IllegalArgumentException("missing " + key);
        }
        return intValue(element);
    }

    private static int intValue(JsonObject object, String key, int fallback) {
        JsonElement element = object.get(key);
        return element == null ? fallback : intValue(element);
    }

    private static int intValue(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return Integer.decode(element.getAsString());
        }
        return element.getAsInt();
    }

    private static boolean booleanValue(JsonObject object, String key, boolean fallback) {
        JsonElement element = object.get(key);
        return element == null ? fallback : element.getAsBoolean();
    }

    private static JsonObject object(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value, E fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.US));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private static String normalize(String name) {
        return name.toUpperCase(Locale.US);
    }

    public record LoadReport(boolean loadedConfig, boolean createdDefault, int customFluids, int skipped,
            List<String> warnings) {
        public String summary() {
            return "custom fluid types loadedConfig=" + loadedConfig + " createdDefault=" + createdDefault
                    + " customFluids=" + customFluids + " skipped=" + skipped;
        }
    }

    private HbmFluidTypeConfig() {
    }
}
