package com.hbm.ntm.damage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
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

public final class DamageResistanceConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "hbmArmor.json";
    private static final String TEMPLATE_FILE = "_hbmArmor.json";
    private static volatile LoadReport report = new LoadReport(false, 0, 0, 0, 0, 0, 0, 0, List.of());

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
                        defaultStats.skippedItems, defaultStats.skippedSets, defaultStats.skippedEntities, defaultStats.warningCount, defaultStats.warnings));
            }
            try (Reader reader = Files.newBufferedReader(config)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                ConfigStats stats = readConfig(json);
                return remember(new LoadReport(true, stats.itemStats, stats.setStats, stats.entityStats,
                        stats.skippedItems, stats.skippedSets, stats.skippedEntities, stats.warningCount, stats.warnings));
            }
        } catch (IOException | RuntimeException ex) {
            HbmNtm.LOGGER.warn("Failed to load HBM damage resistance config, using defaults.", ex);
            DamageResistanceHandler.clear();
            ConfigStats fallbackStats = registerDefaults(false);
            return remember(new LoadReport(false, fallbackStats.itemStats, fallbackStats.setStats, fallbackStats.entityStats,
                    fallbackStats.skippedItems, fallbackStats.skippedSets, fallbackStats.skippedEntities, fallbackStats.warningCount, fallbackStats.warnings));
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
        return readConfig(json, true);
    }

    private static ConfigStats readConfig(JsonObject json, boolean apply) {
        if (apply) {
            DamageResistanceHandler.clear();
        }
        ConfigStats stats = new ConfigStats();

        JsonArray itemStats = array(json, "itemStats");
        for (int index = 0; index < itemStats.size(); index++) {
            JsonElement element = itemStats.get(index);
            if (!isArray(element, 2)) {
                stats.skippedItems++;
                stats.addWarning("invalid itemStats entry #" + index);
                continue;
            }
            JsonArray entry = element.getAsJsonArray();
            String itemId = stringValue(entry.get(0));
            if (itemId == null) {
                stats.skippedItems++;
                stats.addWarning("invalid itemStats id #" + index);
                continue;
            }
            Item item = item(itemId);
            if (item == null) {
                stats.skippedItems++;
                stats.addWarning("missing migrated item " + itemId);
                continue;
            }
            DamageResistanceStats resistance = deserialize(entry.get(1), stats, "itemStats " + itemId);
            if (resistance == null) {
                stats.skippedItems++;
                stats.addWarning("invalid itemStats resistance for " + itemId);
                continue;
            }
            if (apply) {
                DamageResistanceHandler.registerItem(item, resistance);
            }
            stats.itemStats++;
        }

        JsonArray setStats = array(json, "setStats");
        for (int index = 0; index < setStats.size(); index++) {
            JsonElement element = setStats.get(index);
            if (!isArray(element, 5)) {
                stats.skippedSets++;
                stats.addWarning("invalid setStats entry #" + index);
                continue;
            }
            JsonArray entry = element.getAsJsonArray();
            SetComponent helmet = setComponent(entry.get(0));
            SetComponent chest = setComponent(entry.get(1));
            SetComponent legs = setComponent(entry.get(2));
            SetComponent boots = setComponent(entry.get(3));
            if (!helmet.valid() || !chest.valid() || !legs.valid() || !boots.valid()) {
                stats.skippedSets++;
                stats.addMissingSet(entry, index);
                continue;
            }
            DamageResistanceStats resistance = deserialize(entry.get(4), stats, "setStats #" + index);
            if (resistance == null) {
                stats.skippedSets++;
                stats.addWarning("invalid setStats resistance #" + index);
                continue;
            }
            if (apply) {
                DamageResistanceHandler.registerSet(helmet.item(), chest.item(), legs.item(), boots.item(), resistance);
            }
            stats.setStats++;
        }

        JsonArray entityStats = array(json, "entityStats");
        for (int index = 0; index < entityStats.size(); index++) {
            JsonElement element = entityStats.get(index);
            if (!isArray(element, 2)) {
                stats.skippedEntities++;
                stats.addWarning("invalid entityStats entry #" + index);
                continue;
            }
            JsonArray entry = element.getAsJsonArray();
            String className = stringValue(entry.get(0));
            if (className == null) {
                stats.skippedEntities++;
                stats.addWarning("invalid entityStats class #" + index);
                continue;
            }
            DamageResistanceStats resistance = deserialize(entry.get(1), stats, "entityStats " + className);
            if (resistance == null) {
                stats.skippedEntities++;
                stats.addWarning("invalid entityStats resistance for " + className);
                continue;
            }
            Class<? extends Entity> entityClass = entityClass(className);
            if (!apply) {
                // Parse-only audits should not mutate the live entity resistance registry.
            } else if (className.endsWith(".EntityCreeper") || className.equals(Creeper.class.getName())) {
                DamageResistanceHandler.registerEntity(Creeper.class, resistance);
            } else if (entityClass != null) {
                DamageResistanceHandler.registerEntity(entityClass, resistance);
            } else {
                DamageResistanceHandler.registerEntitySimpleName(simpleName(className), resistance);
            }
            stats.entityStats++;
        }

        if (apply && stats.entityStats == 0) {
            DamageResistanceHandler.registerEntity(Creeper.class, new DamageResistanceStats().addCategory(DamageResistanceHandler.CATEGORY_EXPLOSION, 2.0F, 0.25F));
        }
        return stats;
    }

    public static ConfigAudit configAudit() {
        ConfigStats stats = new ConfigStats();
        List<String> problems = new ArrayList<>();

        JsonObject resistance = new JsonObject();
        JsonArray exact = new JsonArray();
        JsonArray validExact = new JsonArray();
        validExact.add("fall");
        validExact.add(1.0F);
        validExact.add(0.25F);
        exact.add(validExact);
        JsonArray badExact = new JsonArray();
        badExact.add("laser");
        badExact.add("not a number");
        badExact.add(0.75F);
        exact.add(badExact);
        resistance.add("exact", exact);

        JsonArray category = new JsonArray();
        JsonArray badCategory = new JsonArray();
        badCategory.add("");
        badCategory.add(1.0F);
        badCategory.add(0.2F);
        category.add(badCategory);
        resistance.add("category", category);

        JsonArray other = new JsonArray();
        other.add(1.0F);
        other.add("Infinity");
        resistance.add("other", other);

        DamageResistanceStats parsed = deserialize(resistance, stats, "configAudit");
        expect(problems, "valid exact resistance parsed", parsed != null && parsed.exactResistances().containsKey("fall"));
        expect(problems, "bad exact value warned", stats.warnings.stream().anyMatch(warning -> warning.contains("invalid exact resistance values")));
        expect(problems, "bad category value warned", stats.warnings.stream().anyMatch(warning -> warning.contains("invalid category resistance values")));
        expect(problems, "bad other value warned", stats.warnings.stream().anyMatch(warning -> warning.contains("invalid other resistance values")));

        JsonObject root = new JsonObject();
        JsonArray itemStats = new JsonArray();
        itemStats.add("bad item entry");
        JsonArray badItemId = new JsonArray();
        badItemId.add("");
        badItemId.add(new JsonObject());
        itemStats.add(badItemId);
        root.add("itemStats", itemStats);

        JsonArray setStats = new JsonArray();
        JsonArray shortSet = new JsonArray();
        shortSet.add("hbm_ntm_rebirth:missing_helmet");
        setStats.add(shortSet);
        JsonArray partialSet = new JsonArray();
        partialSet.add("minecraft:iron_helmet");
        partialSet.add(com.google.gson.JsonNull.INSTANCE);
        partialSet.add("minecraft:iron_leggings");
        partialSet.add("minecraft:iron_boots");
        JsonObject partialSetResistance = new JsonObject();
        JsonArray partialOther = new JsonArray();
        partialOther.add(3.0F);
        partialOther.add(0.3F);
        partialSetResistance.add("other", partialOther);
        partialSet.add(partialSetResistance);
        setStats.add(partialSet);
        root.add("setStats", setStats);

        JsonArray entityStats = new JsonArray();
        JsonArray badEntityClass = new JsonArray();
        badEntityClass.add("");
        badEntityClass.add(new JsonObject());
        entityStats.add(badEntityClass);
        JsonArray simpleNameEntity = new JsonArray();
        simpleNameEntity.add("com.hbm.entity.mob.EntityCreeperNuclear");
        simpleNameEntity.add(new JsonObject());
        entityStats.add(simpleNameEntity);
        root.add("entityStats", entityStats);

        DamageResistanceHandler.RegistrySnapshot snapshotBefore = DamageResistanceHandler.registrySnapshot();
        ConfigStats parseOnly = readConfig(root, false);
        DamageResistanceHandler.RegistrySnapshot snapshotAfter = DamageResistanceHandler.registrySnapshot();
        expect(problems, "parse-only item skips", parseOnly.skippedItems == 2);
        expect(problems, "parse-only set skips", parseOnly.skippedSets == 1);
        expect(problems, "legacy partial set parsed", parseOnly.setStats == 1);
        expect(problems, "parse-only entity skip", parseOnly.skippedEntities == 1);
        expect(problems, "legacy simple entity parsed", parseOnly.entityStats == 1);
        expect(problems, "parse-only leaves registry unchanged", snapshotBefore.equals(snapshotAfter));

        ConfigStats capped = new ConfigStats();
        for (int i = 0; i < ConfigStats.MAX_WARNINGS + 3; i++) {
            capped.addWarning("warning " + i);
        }
        expect(problems, "warning cap stores first warnings", capped.warnings.size() == ConfigStats.MAX_WARNINGS);
        expect(problems, "warning cap counts all warnings", capped.warningCount == ConfigStats.MAX_WARNINGS + 3);

        return new ConfigAudit(List.copyOf(problems), parseOnly.skippedItems, parseOnly.skippedSets,
                parseOnly.skippedEntities, capped.warningCount, capped.warnings.size());
    }

    public static DefaultAudit defaultAudit() {
        List<String> problems = new ArrayList<>();
        JsonObject defaults = new JsonObject();
        registerDefaults(true, defaults);
        JsonArray itemStats = array(defaults, "itemStats");
        JsonArray setStats = array(defaults, "setStats");
        JsonArray entityStats = array(defaults, "entityStats");

        expect(problems, "legacy default item count", itemStats.size() == 4);
        expect(problems, "legacy default armor set count", setStats.size() == 34);
        expect(problems, "legacy default entity count", entityStats.size() == 2);
        expect(problems, "jackt physical stats",
                hasCategory(statsForItem(itemStats, "jackt"), DamageResistanceHandler.CATEGORY_PHYSICAL, 1.0F, 0.20F));
        expect(problems, "jackt2 physical stats",
                hasCategory(statsForItem(itemStats, "jackt2"), DamageResistanceHandler.CATEGORY_PHYSICAL, 2.0F, 0.25F));
        expect(problems, "nossy hat DT",
                hasOther(statsForItem(itemStats, "nossy_hat"), 2.0F, 0.0F));
        expect(problems, "no9 DT",
                hasOther(statsForItem(itemStats, "no9"), 0.5F, 0.0F));
        expect(problems, "steel set physical stats",
                hasCategory(statsForSet(setStats, "steel_helmet", "steel_plate", "steel_legs", "steel_boots"),
                        DamageResistanceHandler.CATEGORY_PHYSICAL, 2.0F, 0.1F));
        expect(problems, "titanium set physical stats",
                hasCategory(statsForSet(setStats, "titanium_helmet", "titanium_plate", "titanium_legs", "titanium_boots"),
                        DamageResistanceHandler.CATEGORY_PHYSICAL, 3.0F, 0.1F));
        expect(problems, "rpa energy stats",
                hasCategory(statsForSet(setStats, "rpa_helmet", "rpa_plate", "rpa_legs", "rpa_boots"),
                        DamageResistanceHandler.CATEGORY_ENERGY, 25.0F, 0.75F));
        expect(problems, "rpa fall immunity",
                hasExact(statsForSet(setStats, "rpa_helmet", "rpa_plate", "rpa_legs", "rpa_boots"),
                        DamageTypes.FALL.location().getPath(), 0.0F, 1.0F));
        expect(problems, "hev onfire immunity",
                hasExact(statsForSet(setStats, "hev_helmet", "hev_plate", "hev_legs", "hev_boots"),
                        DamageTypes.ON_FIRE.location().getPath(), 0.0F, 1.0F));
        expect(problems, "fau laser resistance",
                hasExact(statsForSet(setStats, "fau_helmet", "fau_plate", "fau_legs", "fau_boots"),
                        DamageClass.LASER.name(), 25.0F, 0.95F));
        expect(problems, "euphemium other immunity",
                hasOther(statsForSet(setStats, "euphemium_helmet", "euphemium_plate", "euphemium_legs", "euphemium_boots"),
                        1_000_000.0F, 1.0F));
        expect(problems, "asbestos fire resistance",
                hasCategory(statsForSet(setStats, "asbestos_helmet", "asbestos_plate", "asbestos_legs", "asbestos_boots"),
                        DamageResistanceHandler.CATEGORY_FIRE, 10.0F, 0.9F));
        expect(problems, "creeper explosion resistance",
                hasCategory(statsForEntity(entityStats, Creeper.class.getName()),
                        DamageResistanceHandler.CATEGORY_EXPLOSION, 2.0F, 0.25F));
        expect(problems, "legacy nuclear creeper explosion resistance",
                hasCategory(statsForEntity(entityStats, "com.hbm.entity.mob.EntityCreeperNuclear"),
                        DamageResistanceHandler.CATEGORY_EXPLOSION, 5.0F, 0.35F));

        return new DefaultAudit(List.copyOf(problems), itemStats.size(), setStats.size(), entityStats.size());
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
        addItem(itemStats, "nossy_hat", new DamageResistanceStats().setOther(2.0F, 0.0F));
        addItem(itemStats, "no9", new DamageResistanceStats().setOther(0.5F, 0.0F));

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
        return stats.toJson();
    }

    private static DamageResistanceStats deserialize(JsonElement statsElement, ConfigStats configStats, String context) {
        DamageResistanceStats.JsonParseResult result = DamageResistanceStats.parseJson(statsElement, context);
        for (String warning : result.warnings()) {
            configStats.addWarning(warning);
        }
        return result.stats();
    }

    private static JsonObject statsForItem(JsonArray itemStats, String itemName) {
        String id = HbmNtm.MOD_ID + ":" + itemName;
        for (JsonElement element : itemStats) {
            if (!isArray(element, 2)) {
                continue;
            }
            JsonArray entry = element.getAsJsonArray();
            if (id.equals(stringValue(entry.get(0))) && entry.get(1).isJsonObject()) {
                return entry.get(1).getAsJsonObject();
            }
        }
        return null;
    }

    private static JsonObject statsForSet(JsonArray setStats, String helmet, String chest, String legs, String boots) {
        String helmetId = HbmNtm.MOD_ID + ":" + helmet;
        String chestId = HbmNtm.MOD_ID + ":" + chest;
        String legsId = HbmNtm.MOD_ID + ":" + legs;
        String bootsId = HbmNtm.MOD_ID + ":" + boots;
        for (JsonElement element : setStats) {
            if (!isArray(element, 5)) {
                continue;
            }
            JsonArray entry = element.getAsJsonArray();
            if (helmetId.equals(stringValue(entry.get(0)))
                    && chestId.equals(stringValue(entry.get(1)))
                    && legsId.equals(stringValue(entry.get(2)))
                    && bootsId.equals(stringValue(entry.get(3)))
                    && entry.get(4).isJsonObject()) {
                return entry.get(4).getAsJsonObject();
            }
        }
        return null;
    }

    private static JsonObject statsForEntity(JsonArray entityStats, String className) {
        for (JsonElement element : entityStats) {
            if (!isArray(element, 2)) {
                continue;
            }
            JsonArray entry = element.getAsJsonArray();
            if (className.equals(stringValue(entry.get(0))) && entry.get(1).isJsonObject()) {
                return entry.get(1).getAsJsonObject();
            }
        }
        return null;
    }

    private static boolean hasExact(JsonObject stats, String key, float threshold, float resistance) {
        return hasResistance(stats, "exact", DamageResistanceHandler.exactTypeKey(key), threshold, resistance);
    }

    private static boolean hasCategory(JsonObject stats, String key, float threshold, float resistance) {
        return hasResistance(stats, "category", DamageResistanceHandler.categoryKey(key), threshold, resistance);
    }

    private static boolean hasOther(JsonObject stats, float threshold, float resistance) {
        if (stats == null || !isArray(stats.get("other"), 2)) {
            return false;
        }
        JsonArray other = stats.getAsJsonArray("other");
        Float actualThreshold = floatValue(other.get(0));
        Float actualResistance = floatValue(other.get(1));
        return actualThreshold != null && actualResistance != null
                && nearly(actualThreshold, threshold)
                && nearly(actualResistance, resistance);
    }

    private static boolean hasResistance(JsonObject stats, String arrayKey, String key, float threshold, float resistance) {
        if (stats == null) {
            return false;
        }
        for (JsonElement element : array(stats, arrayKey)) {
            if (!isArray(element, 3)) {
                continue;
            }
            JsonArray entry = element.getAsJsonArray();
            String actualKey = stringValue(entry.get(0));
            Float actualThreshold = floatValue(entry.get(1));
            Float actualResistance = floatValue(entry.get(2));
            if (actualKey != null
                    && actualThreshold != null
                    && actualResistance != null
                    && actualKey.equals(key)
                    && nearly(actualThreshold, threshold)
                    && nearly(actualResistance, resistance)) {
                return true;
            }
        }
        return false;
    }

    private static JsonArray array(JsonObject json, String key) {
        return json != null && json.has(key) && json.get(key).isJsonArray() ? json.getAsJsonArray(key) : new JsonArray();
    }

    private static boolean isArray(JsonElement element, int minSize) {
        return element != null && element.isJsonArray() && element.getAsJsonArray().size() >= minSize;
    }

    private static SetComponent setComponent(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return new SetComponent(true, null);
        }
        String id = stringValue(element);
        if (id == null) {
            return new SetComponent(false, null);
        }
        Item item = item(id);
        return new SetComponent(item != null, item);
    }

    private static String stringValue(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return null;
        }
        try {
            String value = element.getAsString();
            return value == null || value.isBlank() ? null : value;
        } catch (ClassCastException | IllegalStateException | JsonParseException ignored) {
            return null;
        }
    }

    private static Float floatValue(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return null;
        }
        try {
            float value = element.getAsFloat();
            return Float.isFinite(value) ? value : null;
        } catch (NumberFormatException | ClassCastException | IllegalStateException | JsonParseException ignored) {
            return null;
        }
    }

    private static Item item(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        String path = parsed == null ? id : parsed.getPath();
        RegistryObject<Item> legacy = ModItems.legacyItem(path);
        if (legacy != null) {
            try {
                return legacy.get();
            } catch (IllegalStateException ignored) {
                // During early config/default audits, fall through to the Forge registry lookup.
            }
        }
        ResourceLocation location = parsed == null ? ResourceLocation.tryParse(HbmNtm.MOD_ID + ":" + id) : parsed;
        if (location == null) {
            return null;
        }
        Item item = ForgeRegistries.ITEMS.getValue(location);
        return item == null || item == net.minecraft.world.item.Items.AIR ? null : item;
    }

    private static String simpleName(String className) {
        int index = className.lastIndexOf('.');
        return index < 0 ? className : className.substring(index + 1);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Entity> entityClass(String className) {
        try {
            Class<?> type = Class.forName(className);
            if (Entity.class.isAssignableFrom(type)) {
                return (Class<? extends Entity>) type;
            }
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }

    public record LoadReport(boolean externalConfig, int itemStats, int setStats, int entityStats,
                             int skippedItems, int skippedSets, int skippedEntities, int warningCount,
                             List<String> warnings) {
        public String summary() {
            return externalConfig
                    ? "damage resistance config: " + itemStats + " item entries, " + setStats + " armor sets, " + entityStats + " entity entries"
                    + skippedSuffix() + warningSuffix()
                    : "damage resistance defaults loaded; template written if missing" + skippedSuffix() + warningSuffix();
        }

        private String skippedSuffix() {
            int skipped = skippedItems() + skippedSets() + skippedEntities();
            return skipped <= 0 ? "" : ", skipped " + skipped + " invalid or missing entries";
        }

        public boolean warningsTruncated() {
            return warningCount() > warnings().size();
        }

        private String warningSuffix() {
            return warningCount() <= 0 ? "" : ", warnings " + warnings().size() + "/" + warningCount();
        }
    }

    public record ConfigAudit(List<String> problems, int skippedItems, int skippedSets, int skippedEntities,
                              int totalWarnings, int storedWarnings) {
        public boolean passed() {
            return problems.isEmpty();
        }
    }

    public record DefaultAudit(List<String> problems, int itemEntries, int setEntries, int entityEntries) {
        public boolean passed() {
            return problems.isEmpty();
        }
    }

    private record SetComponent(boolean valid, Item item) {
    }

    private static final class ConfigStats {
        private static final int MAX_WARNINGS = 20;

        private int itemStats;
        private int setStats;
        private int entityStats;
        private int skippedItems;
        private int skippedSets;
        private int skippedEntities;
        private int warningCount;
        private final List<String> warnings = new ArrayList<>();

        private void addWarning(String warning) {
            warningCount++;
            if (warnings.size() < MAX_WARNINGS) {
                warnings.add(warning);
            }
        }

        private void addMissingSet(JsonArray entry, int index) {
            for (int i = 0; i < 4; i++) {
                JsonElement element = entry.get(i);
                if (element == null || element.isJsonNull()) {
                    continue;
                }
                String id = stringValue(entry.get(i));
                if (item(id) == null) {
                    addWarning("missing migrated set component " + (id == null ? "<invalid>" : id) + " in setStats #" + index);
                }
            }
        }
    }

    private static void expect(List<String> problems, String label, boolean ok) {
        if (!ok) {
            problems.add(label);
        }
    }

    private static boolean nearly(float actual, float expected) {
        return Math.abs(actual - expected) < 0.0001F;
    }

    private DamageResistanceConfig() {
    }
}
