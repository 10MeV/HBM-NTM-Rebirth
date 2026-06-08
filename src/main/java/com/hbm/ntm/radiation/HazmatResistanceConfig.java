package com.hbm.ntm.radiation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.compat.Compat;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HazmatResistanceConfig {
    private static final String CONFIG_FOLDER = "hbm";
    private static final String CONFIG_FILE = "hbmRadResist.json";
    private static final String TEMPLATE_FILE = "_hbmRadResist.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static volatile LoadReport report = LoadReport.defaults(null, null, false, 0, 0, List.of());

    public static LoadReport initialize(Path configDirectory) {
        HazmatRegistry.registerDefaults();
        Compat.CompatHazmatReport compatReport = Compat.registerCompatHazmat();

        Path folder = configDirectory.resolve(CONFIG_FOLDER);
        Path config = folder.resolve(CONFIG_FILE);
        Path template = folder.resolve(TEMPLATE_FILE);
        List<String> warnings = new ArrayList<>();
        boolean wroteTemplate = false;

        try {
            Files.createDirectories(folder);
            if (Files.notExists(config)) {
                writeTemplate(template, HazmatRegistry.resistanceSnapshot());
                wroteTemplate = true;
            }
        } catch (Exception exception) {
            warnings.add("Could not write hazmat resistance template: " + exception.getMessage());
        }

        if (Files.notExists(config)) {
            return remember(LoadReport.defaults(config, template, wroteTemplate,
                    HazmatRegistry.registrySnapshot().resistanceEntries(),
                    compatReport.totalEntries(),
                    warnings));
        }

        ConfigStats stats = readConfig(config, warnings);
        if (stats == null) {
            return remember(LoadReport.defaults(config, template, wroteTemplate,
                    HazmatRegistry.registrySnapshot().resistanceEntries(),
                    compatReport.totalEntries(),
                    warnings));
        }

        HazmatRegistry.replaceResistances(stats.entries);
        return remember(new LoadReport(true,
                HazmatRegistry.registrySnapshot().resistanceEntries(),
                compatReport.totalEntries(),
                stats.skippedEntries,
                config,
                template,
                wroteTemplate,
                List.copyOf(warnings)));
    }

    public static LoadReport loadReport() {
        return report;
    }

    private static LoadReport remember(LoadReport current) {
        report = current;
        return current;
    }

    private static void writeTemplate(Path template, Map<Item, Double> defaults) throws Exception {
        JsonObject root = new JsonObject();
        root.addProperty("comment", "Template file, remove the underscore ('_') from the name to enable the config.");
        JsonArray entries = new JsonArray();
        defaults.entrySet().stream()
                .sorted(Comparator.comparing(entry -> itemId(entry.getKey())))
                .forEach(entry -> {
                    JsonObject object = new JsonObject();
                    object.addProperty("item", itemId(entry.getKey()));
                    object.addProperty("resistance", entry.getValue());
                    entries.add(object);
                });
        root.add("entries", entries);
        try (Writer writer = Files.newBufferedWriter(template, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }
    }

    private static ConfigStats readConfig(Path config, List<String> warnings) {
        try (Reader reader = Files.newBufferedReader(config, StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null || !root.has("entries") || !root.get("entries").isJsonArray()) {
                warnings.add("Hazmat resistance config has no entries array: " + config);
                return null;
            }

            ConfigStats stats = new ConfigStats();
            JsonArray entries = root.getAsJsonArray("entries");
            for (int index = 0; index < entries.size(); index++) {
                readEntry(entries.get(index), stats, warnings, index);
            }
            return stats;
        } catch (Exception exception) {
            warnings.add("Could not read hazmat resistance config: " + exception.getMessage());
            return null;
        }
    }

    private static void readEntry(JsonElement element, ConfigStats stats, List<String> warnings, int index) {
        if (!element.isJsonObject()) {
            stats.skippedEntries++;
            warnings.add("Hazmat resistance entry #" + index + " is not an object.");
            return;
        }

        JsonObject object = element.getAsJsonObject();
        if (!object.has("item") || !object.has("resistance")) {
            stats.skippedEntries++;
            warnings.add("Hazmat resistance entry #" + index + " is missing item or resistance.");
            return;
        }

        String itemName = object.get("item").getAsString();
        Item item = item(itemName);
        if (item == null) {
            stats.skippedEntries++;
            warnings.add("Missing item " + itemName);
            return;
        }

        double resistance = object.get("resistance").getAsDouble();
        stats.entries.put(item, Math.max(0.0D, resistance));
    }

    private static Item item(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        ResourceLocation location = ResourceLocation.tryParse(id);
        String path = location == null ? id : location.getPath();
        RegistryObject<Item> legacy = ModItems.legacyItem(path);
        if (legacy != null && legacy.isPresent()) {
            return legacy.get();
        }

        Item item = registryItem(location);
        if (item != null) {
            return item;
        }

        if (location != null && "hbm".equals(location.getNamespace())) {
            item = registryItem(new ResourceLocation(HbmNtm.MOD_ID, location.getPath()));
            if (item != null) {
                return item;
            }
        }

        if (location == null) {
            return registryItem(new ResourceLocation(HbmNtm.MOD_ID, id));
        }
        return null;
    }

    private static Item registryItem(ResourceLocation location) {
        if (location == null) {
            return null;
        }
        Item item = ForgeRegistries.ITEMS.getValue(location);
        return item == null || item == Items.AIR ? null : item;
    }

    private static String itemId(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        return id == null ? String.valueOf(item) : id.toString();
    }

    public record LoadReport(boolean externalConfig, int resistanceEntries, int compatEntries, int skippedEntries,
                             Path configFile, Path templateFile, boolean templateWritten, List<String> warnings) {
        private static LoadReport defaults(Path config, Path template, boolean wroteTemplate, int entries,
                                           int compatEntries, List<String> warnings) {
            return new LoadReport(false, entries, compatEntries, 0, config, template, wroteTemplate, List.copyOf(warnings));
        }

        public String summary() {
            return externalConfig
                    ? "hazmat resistance config: entries=" + resistanceEntries + ", skipped=" + skippedEntries
                    + ", compatDefaults=" + compatEntries + ", warnings=" + warnings.size()
                    : "hazmat resistance defaults loaded; entries=" + resistanceEntries + ", compatDefaults="
                    + compatEntries + ", warnings=" + warnings.size();
        }
    }

    private static final class ConfigStats {
        private final Map<Item, Double> entries = new LinkedHashMap<>();
        private int skippedEntries;
    }

    private HazmatResistanceConfig() {
    }
}
