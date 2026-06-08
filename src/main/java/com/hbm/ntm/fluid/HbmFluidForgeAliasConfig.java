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
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

public final class HbmFluidForgeAliasConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "hbmFluidForgeAliases.json";
    private static volatile LoadReport report = new LoadReport(false, false, 0, 0, 0, List.of());

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
            try (Reader reader = Files.newBufferedReader(config)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                return remember(readAliases(json, createdDefault));
            }
        } catch (IOException | RuntimeException ex) {
            HbmNtm.LOGGER.warn("Failed to load HBM Forge fluid alias config, using built-in aliases only.", ex);
            return remember(new LoadReport(false, createdDefault, 0, 0, 1, List.of("failed to read " + CONFIG_FILE)));
        }
    }

    public static LoadReport loadReport() {
        return report;
    }

    private static LoadReport remember(LoadReport current) {
        report = current;
        return current;
    }

    private static LoadReport readAliases(JsonObject root, boolean createdDefault) {
        List<String> warnings = new ArrayList<>();
        int tagAliases = 0;
        int fluidMappings = 0;
        int skipped = 0;
        if (root == null) {
            return new LoadReport(true, createdDefault, 0, 0, 1, List.of("empty root"));
        }

        JsonObject aliases = root.has("aliases") && root.get("aliases").isJsonObject()
                ? root.getAsJsonObject("aliases")
                : root;
        for (Map.Entry<String, JsonElement> entry : aliases.entrySet()) {
            String tagName = entry.getKey();
            try {
                ResourceLocation tag = parseTag(tagName);
                FluidType type = parseFluid(entry.getValue());
                if (type == HbmFluids.NONE) {
                    skipped++;
                    warnings.add(tagName + " targets an unknown or empty HBM fluid");
                    continue;
                }
                HbmFluidForgeMappings.registerTagAlias(tag, type);
                tagAliases++;
            } catch (RuntimeException ex) {
                skipped++;
                warnings.add(tagName + " failed: " + ex.getMessage());
            }
        }

        JsonObject fluids = root.has("fluids") && root.get("fluids").isJsonObject()
                ? root.getAsJsonObject("fluids")
                : new JsonObject();
        for (Map.Entry<String, JsonElement> entry : fluids.entrySet()) {
            String forgeFluidName = entry.getKey();
            try {
                ResourceLocation forgeFluidId = parseId(forgeFluidName);
                Fluid forgeFluid = ForgeRegistries.FLUIDS.getValue(forgeFluidId);
                if (forgeFluid == null || forgeFluid == Fluids.EMPTY) {
                    skipped++;
                    warnings.add(forgeFluidName + " is not a registered Forge fluid");
                    continue;
                }
                FluidMapping mapping = parseFluidMapping(entry.getValue());
                if (mapping.type() == HbmFluids.NONE) {
                    skipped++;
                    warnings.add(forgeFluidName + " targets an unknown or empty HBM fluid");
                    continue;
                }
                if (mapping.export()) {
                    HbmFluidForgeMappings.register(mapping.type(), forgeFluid);
                } else {
                    HbmFluidForgeMappings.registerImportAlias(forgeFluid, mapping.type());
                }
                fluidMappings++;
            } catch (RuntimeException ex) {
                skipped++;
                warnings.add(forgeFluidName + " failed: " + ex.getMessage());
            }
        }
        return new LoadReport(true, createdDefault, tagAliases, fluidMappings, skipped, warnings);
    }

    private static ResourceLocation parseTag(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value.contains(":") ? value : "forge:" + value);
        if (id == null) {
            throw new IllegalArgumentException("invalid fluid tag id");
        }
        return id;
    }

    private static ResourceLocation parseId(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            throw new IllegalArgumentException("invalid fluid id");
        }
        return id;
    }

    private static FluidType parseFluid(JsonElement element) {
        if (element == null) {
            return HbmFluids.NONE;
        }
        String name;
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            JsonElement fluid = object.get("fluid");
            name = fluid == null ? "" : fluid.getAsString();
        } else {
            name = element.getAsString();
        }
        if (name.contains(":")) {
            ResourceLocation id = ResourceLocation.tryParse(name);
            name = id == null ? name : id.getPath();
        }
        return HbmFluids.fromName(name);
    }

    private static FluidMapping parseFluidMapping(JsonElement element) {
        if (element != null && element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            FluidType type = parseFluid(object);
            boolean export = object.has("export") && object.get("export").getAsBoolean();
            return new FluidMapping(type, export);
        }
        return new FluidMapping(parseFluid(element), false);
    }

    private static void writeDefault(Path config) throws IOException {
        JsonObject root = new JsonObject();
        JsonObject aliases = new JsonObject();
        aliases.addProperty("forge:oil", "OIL");
        aliases.addProperty("forge:crude_oil", "OIL");
        aliases.addProperty("forge:diesel", "DIESEL");
        aliases.addProperty("forge:gasoline", "GASOLINE");
        aliases.addProperty("forge:steam", "STEAM");
        aliases.addProperty("forge:sulfuric_acid", "SULFURIC_ACID");
        root.add("aliases", aliases);
        root.add("fluids", new JsonObject());
        try (Writer writer = Files.newBufferedWriter(config)) {
            GSON.toJson(root, writer);
        }
    }

    private record FluidMapping(FluidType type, boolean export) {
    }

    public record LoadReport(boolean loadedConfig, boolean createdDefault, int aliases, int fluidMappings, int skipped,
            List<String> warnings) {
        public String summary() {
            return "Forge fluid aliases loadedConfig=" + loadedConfig + " createdDefault=" + createdDefault
                    + " aliases=" + aliases + " fluidMappings=" + fluidMappings + " skipped=" + skipped;
        }
    }

    private HbmFluidForgeAliasConfig() {
    }
}
