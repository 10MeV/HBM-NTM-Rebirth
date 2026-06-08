package com.hbm.ntm.damage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DamageResistanceConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "hbmArmor.json";
    private static final String TEMPLATE_FILE = "_hbmArmor.json";
    private static volatile LoadReport report = new LoadReport(false, 0, 0, 0, 0, 0, List.of());

    public static LoadReport initialize(Path configDir) {
        DamageResistanceHandler.clear();
        ConfigStats defaultStats = registerDefaults(false);

        Path hbmDir = configDir.resolve("hbm");
        Path config = hbmDir.resolve(CONFIG_FILE);
        Path template = hbmDir.resolve(TEMPLATE_FILE);
        try {
            Files.createDirectories(hbmDir);
            if (Files.notExists(config)) {
                writeTemplate(template);
                return remember(new LoadReport(false, defaultStats.itemStats, defaultStats.setStats, defaultStats.entityStats,
                        defaultStats.skippedItems, defaultStats.skippedSets, defaultStats.missingIds));
            }
            try (Reader reader = Files.newBufferedReader(config)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                ConfigStats stats = readConfig(json);
                return remember(new LoadReport(true, stats.itemStats, stats.setStats, stats.entityStats,
                        stats.skippedItems, stats.skippedSets, stats.missingIds));
            }
        } catch (IOException | RuntimeException ex) {
            HbmNtm.LOGGER.warn("Failed to load HBM damage resistance config, using defaults.", ex);
            DamageResistanceHandler.clear();
            ConfigStats fallbackStats = registerDefaults(false);
            return remember(new LoadReport(false, fallbackStats.itemStats, fallbackStats.setStats, fallbackStats.entityStats,
                    fallbackStats.skippedItems, fallbackStats.skippedSets, fallbackStats.missingIds));
        }
    }

    public static LoadReport loadReport() {
        return report;
    }

    private static LoadReport remember(LoadReport current) {
        report = current;
        return current;
    }

    private static ConfigStats readConfig(JsonObject json) {
        DamageResistanceHandler.clear();
        ConfigStats stats = new ConfigStats();

        JsonArray itemStats = array(json, "itemStats");
        for (JsonElement element : itemStats) {
            JsonArray entry = element.getAsJsonArray();
            String itemId = entry.get(0).getAsString();
            Item item = item(itemId);
            if (item == null) {
                stats.skippedItems++;
                stats.addMissing("item " + itemId);
                continue;
            }
            DamageResistanceHandler.registerItem(item, deserialize(entry.get(1).getAsJsonObject()));
            stats.itemStats++;
        }

        JsonArray setStats = array(json, "setStats");
        for (JsonElement element : setStats) {
            JsonArray entry = element.getAsJsonArray();
            Item helmet = itemOrNull(entry.get(0));
            Item chest = itemOrNull(entry.get(1));
            Item legs = itemOrNull(entry.get(2));
            Item boots = itemOrNull(entry.get(3));
            if (helmet == null || chest == null || legs == null || boots == null) {
                stats.skippedSets++;
                stats.addMissingSet(entry);
                continue;
            }
            DamageResistanceHandler.registerSet(helmet, chest, legs, boots, deserialize(entry.get(4).getAsJsonObject()));
            stats.setStats++;
        }

        JsonArray entityStats = array(json, "entityStats");
        for (JsonElement element : entityStats) {
            JsonArray entry = element.getAsJsonArray();
            String className = entry.get(0).getAsString();
            DamageResistanceStats resistance = deserialize(entry.get(1).getAsJsonObject());
            Class<? extends net.minecraft.world.entity.Entity> entityClass = entityClass(className);
            if (className.endsWith(".EntityCreeper") || className.equals(Creeper.class.getName())) {
                DamageResistanceHandler.registerEntity(Creeper.class, resistance);
            } else if (entityClass != null) {
                DamageResistanceHandler.registerEntity(entityClass, resistance);
            } else {
                DamageResistanceHandler.registerEntitySimpleName(simpleName(className), resistance);
            }
            stats.entityStats++;
        }

        if (stats.entityStats == 0) {
            DamageResistanceHandler.registerEntity(Creeper.class, new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 2.0F, 0.25F));
        }
        return stats;
    }

    private static void writeTemplate(Path template) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("comment", "Template file, remove the underscore ('_') from the name to enable the config.");
        JsonObject defaults = new JsonObject();
        registerDefaults(true, defaults);
        root.add("itemStats", defaults.getAsJsonArray("itemStats"));
        root.add("setStats", defaults.getAsJsonArray("setStats"));
        root.add("entityStats", defaults.getAsJsonArray("entityStats"));
        try (Writer writer = Files.newBufferedWriter(template)) {
            GSON.toJson(root, writer);
        }
    }

    private static ConfigStats registerDefaults(boolean templateOnly) {
        return registerDefaults(templateOnly, null);
    }

    private static ConfigStats registerDefaults(boolean templateOnly, JsonObject output) {
        JsonArray itemStats = new JsonArray();
        JsonArray setStats = new JsonArray();
        JsonArray entityStats = new JsonArray();

        addEntity(entityStats, Creeper.class.getName(), new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 2.0F, 0.25F));
        addEntity(entityStats, "com.hbm.entity.mob.EntityCreeperNuclear", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 5.0F, 0.35F));

        addItem(itemStats, "jackt", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 1.0F, 0.20F));
        addItem(itemStats, "jackt2", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 2.0F, 0.25F));

        addSet(setStats, "steel_helmet", "steel_plate", "steel_legs", "steel_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 2.0F, 0.1F));
        addSet(setStats, "titanium_helmet", "titanium_plate", "titanium_legs", "titanium_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 3.0F, 0.1F));
        addSet(setStats, "alloy_helmet", "alloy_plate", "alloy_legs", "alloy_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 2.0F, 0.1F));
        addSet(setStats, "cobalt_helmet", "cobalt_plate", "cobalt_legs", "cobalt_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 2.0F, 0.1F));
        addSet(setStats, "starmetal_helmet", "starmetal_plate", "starmetal_legs", "starmetal_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 3.0F, 0.25F).setOther(1.0F, 0.1F));
        addSet(setStats, "zirconium_legs", "zirconium_legs", "zirconium_legs", "zirconium_legs", new DamageResistanceStats().setOther(0.0F, 1.0F));
        addSet(setStats, "dnt_helmet", "dnt_plate", "dnt_legs", "dnt_boots", new DamageResistanceStats());
        addSet(setStats, "cmb_helmet", "cmb_plate", "cmb_legs", "cmb_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 5.0F, 0.5F).setOther(5.0F, 0.25F));
        addSet(setStats, "schrabidium_helmet", "schrabidium_plate", "schrabidium_legs", "schrabidium_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 10.0F, 0.65F).setOther(5.0F, 0.5F));
        addSet(setStats, "robes_helmet", "robes_plate", "robes_legs", "robes_boots", new DamageResistanceStats());
        addSet(setStats, "security_helmet", "security_plate", "security_legs", "security_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 5.0F, 0.5F).addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 2.0F, 0.25F));
        addSet(setStats, "steamsuit_helmet", "steamsuit_plate", "steamsuit_legs", "steamsuit_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 2.0F, 0.15F).addCategory(DamageResistanceHandler.CATEGORY_FIRE, 0.5F, 0.25F).addExact(DamageTypes.FALL.location().getPath(), 5.0F, 0.25F).setOther(0.0F, 0.1F));
        addSet(setStats, "dieselsuit_helmet", "dieselsuit_plate", "dieselsuit_legs", "dieselsuit_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 1.0F, 0.15F).addCategory(DamageResistanceHandler.CATEGORY_FIRE, 0.5F, 0.5F).addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 2.0F, 0.15F).setOther(0.0F, 0.1F));
        addSet(setStats, "t51_helmet", "t51_plate", "t51_legs", "t51_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 2.0F, 0.15F).addCategory(DamageResistanceHandler.CATEGORY_FIRE, 0.5F, 0.35F).addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 5.0F, 0.25F).addExact(DamageTypes.FALL.location().getPath(), 0.0F, 1.0F).setOther(0.0F, 0.1F));
        addSet(setStats, "ajr_helmet", "ajr_plate", "ajr_legs", "ajr_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 4.0F, 0.15F).addCategory(DamageResistanceHandler.CATEGORY_FIRE, 0.5F, 0.35F).addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 7.5F, 0.25F).addExact(DamageTypes.FALL.location().getPath(), 0.0F, 1.0F).setOther(0.0F, 0.15F));
        addSet(setStats, "ajro_helmet", "ajro_plate", "ajro_legs", "ajro_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 4.0F, 0.15F).addCategory(DamageResistanceHandler.CATEGORY_FIRE, 0.5F, 0.35F).addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 7.5F, 0.25F).addExact(DamageTypes.FALL.location().getPath(), 0.0F, 1.0F).setOther(0.0F, 0.15F));
        addSet(setStats, "rpa_helmet", "rpa_plate", "rpa_legs", "rpa_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 25.0F, 0.65F).addCategory(DamageResistanceHandler.CATEGORY_FIRE, 10.0F, 0.9F).addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 15.0F, 0.25F).addCategory(DamageResistanceHandler.CATEGORY_ENERGY, 25.0F, 0.75F).addExact(DamageTypes.FALL.location().getPath(), 0.0F, 1.0F).setOther(15.0F, 0.3F));
        addSet(setStats, "ncrpa_helmet", "ncrpa_plate", "ncrpa_legs", "ncrpa_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 25.0F, 0.65F).addCategory(DamageResistanceHandler.CATEGORY_FIRE, 10.0F, 0.9F).addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 15.0F, 0.25F).addCategory(DamageResistanceHandler.CATEGORY_ENERGY, 10.0F, 0.5F).addExact(DamageTypes.FALL.location().getPath(), 0.0F, 1.0F).setOther(15.0F, 0.25F));
        DamageResistanceStats bj = new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 5.0F, 0.5F).addCategory(DamageResistanceHandler.CATEGORY_FIRE, 2.5F, 0.5F).addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 10.0F, 0.25F).addExact(DamageTypes.FALL.location().getPath(), 0.0F, 1.0F).setOther(2.0F, 0.15F);
        addSet(setStats, "bj_helmet", "bj_plate", "bj_legs", "bj_boots", bj);
        addSet(setStats, "bj_helmet", "bj_plate_jetpack", "bj_legs", "bj_boots", bj);
        addSet(setStats, "envsuit_helmet", "envsuit_plate", "envsuit_legs", "envsuit_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_FIRE, 2.0F, 0.75F).addExact(DamageTypes.DROWN.location().getPath(), 0.0F, 1.0F).addExact(DamageTypes.FALL.location().getPath(), 5.0F, 0.75F).setOther(0.0F, 0.1F));
        addSet(setStats, "hev_helmet", "hev_plate", "hev_legs", "hev_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 2.0F, 0.25F).addCategory(DamageResistanceHandler.CATEGORY_FIRE, 0.5F, 0.5F).addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 5.0F, 0.25F).addExact(DamageTypes.ON_FIRE.location().getPath(), 0.0F, 1.0F).addExact(DamageTypes.FALL.location().getPath(), 10.0F, 0.0F).setOther(2.0F, 0.25F));
        addSet(setStats, "bismuth_helmet", "bismuth_plate", "bismuth_legs", "bismuth_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 2.0F, 0.15F).addCategory(DamageResistanceHandler.CATEGORY_FIRE, 5.0F, 0.5F).addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 5.0F, 0.25F).addExact(DamageTypes.FALL.location().getPath(), 0.0F, 1.0F).setOther(2.0F, 0.25F));
        addSet(setStats, "fau_helmet", "fau_plate", "fau_legs", "fau_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 100.0F, 0.99F).addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 50.0F, 0.95F).addCategory(DamageResistanceHandler.CATEGORY_FIRE, 100.0F, 1.0F).addExact(DamageClass.LASER.name().toLowerCase(Locale.US), 25.0F, 0.95F).addExact(DamageTypes.FALL.location().getPath(), 0.0F, 1.0F).setOther(100.0F, 0.99F));
        addSet(setStats, "dns_helmet", "dns_plate", "dns_legs", "dns_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 1000.0F, 1.0F).addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 100.0F, 0.99F).addCategory(DamageResistanceHandler.CATEGORY_FIRE, 0.0F, 1.0F).setOther(1000.0F, 1.0F));
        addSet(setStats, "taurun_helmet", "taurun_plate", "taurun_legs", "taurun_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 2.0F, 0.15F).addCategory(DamageResistanceHandler.CATEGORY_FIRE, 0.0F, 0.25F).addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 0.0F, 0.25F).addExact(DamageTypes.FALL.location().getPath(), 4.0F, 0.5F).setOther(2.0F, 0.1F));
        addSet(setStats, "trenchmaster_helmet", "trenchmaster_plate", "trenchmaster_legs", "trenchmaster_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_PHYSICAL, 5.0F, 0.5F).addCategory(DamageResistanceHandler.CATEGORY_FIRE, 5.0F, 0.5F).addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 5.0F, 0.25F).addExact(DamageClass.LASER.name().toLowerCase(Locale.US), 15.0F, 0.9F).addExact(DamageTypes.FALL.location().getPath(), 10.0F, 0.5F).setOther(5.0F, 0.25F));
        addSet(setStats, "euphemium_helmet", "euphemium_plate", "euphemium_legs", "euphemium_boots", new DamageResistanceStats().setOther(1_000_000.0F, 1.0F));
        addSet(setStats, "hazmat_helmet", "hazmat_plate", "hazmat_legs", "hazmat_boots", new DamageResistanceStats());
        addSet(setStats, "hazmat_helmet_red", "hazmat_plate_red", "hazmat_legs_red", "hazmat_boots_red", new DamageResistanceStats());
        addSet(setStats, "hazmat_helmet_grey", "hazmat_plate_grey", "hazmat_legs_grey", "hazmat_boots_grey", new DamageResistanceStats());
        addSet(setStats, "liquidator_helmet", "liquidator_plate", "liquidator_legs", "liquidator_boots", new DamageResistanceStats());
        addSet(setStats, "hazmat_paa_helmet", "hazmat_paa_plate", "hazmat_paa_legs", "hazmat_paa_boots", new DamageResistanceStats());
        addSet(setStats, "asbestos_helmet", "asbestos_plate", "asbestos_legs", "asbestos_boots", new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_FIRE, 10.0F, 0.9F));

        if (output != null) {
            output.add("itemStats", itemStats);
            output.add("setStats", setStats);
            output.add("entityStats", entityStats);
        }
        if (!templateOnly) {
            return readConfig(object(itemStats, setStats, entityStats));
        }
        return new ConfigStats();
    }

    private static JsonObject object(JsonArray itemStats, JsonArray setStats, JsonArray entityStats) {
        JsonObject object = new JsonObject();
        object.add("itemStats", itemStats);
        object.add("setStats", setStats);
        object.add("entityStats", entityStats);
        return object;
    }

    private static void addItem(JsonArray array, String itemName, DamageResistanceStats stats) {
        JsonArray entry = new JsonArray();
        entry.add(HbmNtm.MOD_ID + ":" + itemName);
        entry.add(serialize(stats));
        array.add(entry);
    }

    private static void addSet(JsonArray array, String helmet, String chest, String legs, String boots, DamageResistanceStats stats) {
        JsonArray entry = new JsonArray();
        entry.add(HbmNtm.MOD_ID + ":" + helmet);
        entry.add(HbmNtm.MOD_ID + ":" + chest);
        entry.add(HbmNtm.MOD_ID + ":" + legs);
        entry.add(HbmNtm.MOD_ID + ":" + boots);
        entry.add(serialize(stats));
        array.add(entry);
    }

    private static void addEntity(JsonArray array, String className, DamageResistanceStats stats) {
        JsonArray entry = new JsonArray();
        entry.add(className);
        entry.add(serialize(stats));
        array.add(entry);
    }

    private static JsonObject serialize(DamageResistanceStats stats) {
        JsonObject object = new JsonObject();
        if (!stats.exactResistances().isEmpty()) {
            JsonArray exact = new JsonArray();
            for (Map.Entry<String, DamageResistance> entry : stats.exactResistances().entrySet()) {
                exact.add(resistance(entry.getKey(), entry.getValue()));
            }
            object.add("exact", exact);
        }
        if (!stats.categoryResistances().isEmpty()) {
            JsonArray category = new JsonArray();
            for (Map.Entry<String, DamageResistance> entry : stats.categoryResistances().entrySet()) {
                category.add(resistance(entry.getKey(), entry.getValue()));
            }
            object.add("category", category);
        }
        if (stats.otherResistance() != null) {
            JsonArray other = new JsonArray();
            other.add(stats.otherResistance().threshold());
            other.add(stats.otherResistance().resistance());
            object.add("other", other);
        }
        return object;
    }

    private static JsonArray resistance(String key, DamageResistance resistance) {
        JsonArray array = new JsonArray();
        array.add(key);
        array.add(resistance.threshold());
        array.add(resistance.resistance());
        return array;
    }

    private static DamageResistanceStats deserialize(JsonObject json) {
        DamageResistanceStats stats = new DamageResistanceStats();
        for (JsonElement element : array(json, "exact")) {
            JsonArray array = element.getAsJsonArray();
            stats.addExact(array.get(0).getAsString(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
        }
        for (JsonElement element : array(json, "category")) {
            JsonArray array = element.getAsJsonArray();
            stats.addCategory(array.get(0).getAsString(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
        }
        if (json.has("other")) {
            JsonArray other = json.getAsJsonArray("other");
            stats.setOther(other.get(0).getAsFloat(), other.get(1).getAsFloat());
        }
        return stats;
    }

    private static JsonArray array(JsonObject json, String key) {
        return json != null && json.has(key) && json.get(key).isJsonArray() ? json.getAsJsonArray(key) : new JsonArray();
    }

    private static Item itemOrNull(JsonElement element) {
        return element == null || element.isJsonNull() ? null : item(element.getAsString());
    }

    private static Item item(String id) {
        String path = id.contains(":") ? new ResourceLocation(id).getPath() : id;
        RegistryObject<Item> legacy = ModItems.legacyItem(path);
        if (legacy != null && legacy.isPresent()) {
            return legacy.get();
        }
        ResourceLocation location = id.contains(":") ? new ResourceLocation(id) : new ResourceLocation(HbmNtm.MOD_ID, id);
        Item item = ForgeRegistries.ITEMS.getValue(location);
        return item == null || item == net.minecraft.world.item.Items.AIR ? null : item;
    }

    private static String simpleName(String className) {
        int index = className.lastIndexOf('.');
        return index < 0 ? className : className.substring(index + 1);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends net.minecraft.world.entity.Entity> entityClass(String className) {
        try {
            Class<?> type = Class.forName(className);
            if (net.minecraft.world.entity.Entity.class.isAssignableFrom(type)) {
                return (Class<? extends net.minecraft.world.entity.Entity>) type;
            }
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }

    public record LoadReport(boolean externalConfig, int itemStats, int setStats, int entityStats, int skippedItems,
                             int skippedSets, List<String> missingIds) {
        public String summary() {
            return externalConfig
                    ? "damage resistance config: " + itemStats + " item entries, " + setStats + " armor sets, " + entityStats + " entity entries"
                    + skippedSuffix()
                    : "damage resistance defaults loaded; template written if missing" + skippedSuffix();
        }

        public List<String> warnings() {
            return missingIds;
        }

        private String skippedSuffix() {
            int skipped = skippedItems() + skippedSets();
            return skipped <= 0 ? "" : ", skipped " + skipped + " entries for missing migrated items";
        }
    }

    private static final class ConfigStats {
        private static final int MAX_MISSING_IDS = 20;

        private int itemStats;
        private int setStats;
        private int entityStats;
        private int skippedItems;
        private int skippedSets;
        private final List<String> missingIds = new ArrayList<>();

        private void addMissing(String id) {
            if (missingIds.size() < MAX_MISSING_IDS) {
                missingIds.add(id);
            }
        }

        private void addMissingSet(JsonArray entry) {
            for (int i = 0; i < 4; i++) {
                String id = entry.get(i).getAsString();
                if (item(id) == null) {
                    addMissing("set component " + id);
                }
            }
        }
    }

    private DamageResistanceConfig() {
    }
}
