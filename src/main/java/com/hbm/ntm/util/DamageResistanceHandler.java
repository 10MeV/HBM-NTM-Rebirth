package com.hbm.ntm.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.ntm.damage.DamageResistanceConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Legacy-name damage resistance facade.
 */
@Deprecated(forRemoval = false)
public class DamageResistanceHandler {
    public static float currentPDT = 0.0F;
    public static float currentPDR = 0.0F;

    public static final String CATEGORY_EXPLOSION = com.hbm.ntm.damage.DamageResistanceHandler.CATEGORY_EXPLOSION;
    public static final String CATEGORY_FIRE = com.hbm.ntm.damage.DamageResistanceHandler.CATEGORY_FIRE;
    public static final String CATEGORY_PHYSICAL = com.hbm.ntm.damage.DamageResistanceHandler.CATEGORY_PHYSICAL;
    public static final String CATEGORY_ENERGY = com.hbm.ntm.damage.DamageResistanceHandler.CATEGORY_ENERGY;

    public static final HashMap<Item, ResistanceStats> itemStats = new ItemStatsMap();
    public static final HashMap<Tuple.Quartet<Item, Item, Item, Item>, ResistanceStats> setStats = new SetStatsMap();
    public static final HashMap<Class<? extends Entity>, ResistanceStats> entityStats = new EntityStatsMap();
    public static final HashMap<Item, List<Tuple.Quartet<Item, Item, Item, Item>>> itemInfoSet = new HashMap<>();

    protected DamageResistanceHandler() {
    }

    public static DamageResistanceConfig.LoadReport init() {
        return DamageResistanceConfig.initialize(FMLPaths.CONFIGDIR.get());
    }

    public static void clearSystem() {
        itemStats.clear();
        setStats.clear();
        entityStats.clear();
        itemInfoSet.clear();
        com.hbm.ntm.damage.DamageResistanceHandler.clear();
    }

    public static void registerItem(Item item, ResistanceStats stats) {
        if (item != null && stats != null) {
            itemStats.put(item, stats);
        }
    }

    public static void registerSet(Item helmet, Item plate, Item legs, Item boots, ResistanceStats stats) {
        if (stats == null) {
            return;
        }
        setStats.put(new Tuple.Quartet<>(helmet, plate, legs, boots), stats);
    }

    public static void registerEntity(Class<? extends Entity> entityClass, ResistanceStats stats) {
        if (entityClass != null && stats != null) {
            entityStats.put(entityClass, stats);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void addToListInHashMap(Object key, HashMap map, Object listElement) {
        List list = (List) map.get(key);
        if (list == null) {
            list = new ArrayList();
            map.put(key, list);
        }
        list.add(listElement);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void addInfo(ItemStack stack, List desc) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        com.hbm.ntm.damage.DamageResistanceTooltipUtil.addResistanceInformation(stack, desc);
    }

    public static void setup(float dt, float dr) {
        currentPDT = dt;
        currentPDR = dr;
        com.hbm.ntm.damage.DamageResistanceHandler.setup(dt, dr);
    }

    public static void reset() {
        currentPDT = 0.0F;
        currentPDR = 0.0F;
        com.hbm.ntm.damage.DamageResistanceHandler.reset();
    }

    public static float currentPierceDt() {
        return com.hbm.ntm.damage.DamageResistanceHandler.currentPierceDt();
    }

    public static float currentPierceDr() {
        return com.hbm.ntm.damage.DamageResistanceHandler.currentPierceDr();
    }

    public static String typeToCategory(DamageSource source) {
        return com.hbm.ntm.damage.DamageResistanceHandler.typeToCategory(source);
    }

    public static String categoryKey(String category) {
        return com.hbm.ntm.damage.DamageResistanceHandler.categoryKey(category);
    }

    public static String categoryKey(DamageClass category) {
        return com.hbm.ntm.damage.DamageResistanceHandler.categoryKey(category.modern());
    }

    public static String exactTypeKey(DamageSource source) {
        return com.hbm.ntm.damage.DamageResistanceHandler.exactTypeKey(source);
    }

    public static String exactTypeKey(String type) {
        return com.hbm.ntm.damage.DamageResistanceHandler.exactTypeKey(type);
    }

    public static float calculateDamage(LivingEntity entity, DamageSource damage, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.calculateDamage(entity, damage, amount);
    }

    public static float calculateDamage(LivingEntity entity, DamageSource damage, float amount,
            float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.calculateDamage(entity, damage, amount, pierceDT, pierce);
    }

    public static float[] getDTDR(LivingEntity entity, DamageSource damage, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getDtDr(entity, damage, amount);
    }

    public static float[] getDTDR(LivingEntity entity, DamageSource damage, float amount, float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getDtDr(entity, damage, amount, pierceDT, pierce);
    }

    public static float[] getDtDr(LivingEntity entity, DamageSource damage, float amount) {
        return getDTDR(entity, damage, amount);
    }

    public static float[] getDtDr(LivingEntity entity, DamageSource damage, float amount, float pierceDT, float pierce) {
        return getDTDR(entity, damage, amount, pierceDT, pierce);
    }

    public static void serialize(JsonWriter writer) throws IOException {
        writer.name("itemStats").beginArray();
        for (Map.Entry<Item, ResistanceStats> entry : itemStats.entrySet()) {
            writer.beginArray().setIndent("");
            writeItemId(writer, entry.getKey());
            writer.setIndent("  ");
            writer.beginObject();
            entry.getValue().serialize(writer);
            writer.setIndent("");
            writer.endObject().endArray().setIndent("  ");
        }
        writer.endArray();

        writer.name("setStats").beginArray();
        for (Map.Entry<Tuple.Quartet<Item, Item, Item, Item>, ResistanceStats> entry : setStats.entrySet()) {
            Tuple.Quartet<Item, Item, Item, Item> set = entry.getKey();
            writer.beginArray().setIndent("");
            writeItemId(writer, set.getW());
            writeItemId(writer, set.getX());
            writeItemId(writer, set.getY());
            writeItemId(writer, set.getZ());
            writer.setIndent("  ");
            writer.beginObject();
            entry.getValue().serialize(writer);
            writer.setIndent("");
            writer.endObject().endArray().setIndent("  ");
        }
        writer.endArray();

        writer.name("entityStats").beginArray();
        for (Map.Entry<Class<? extends Entity>, ResistanceStats> entry : entityStats.entrySet()) {
            writer.beginArray().setIndent("");
            writer.value(entry.getKey().getName()).setIndent("  ");
            writer.beginObject();
            entry.getValue().serialize(writer);
            writer.setIndent("");
            writer.endObject().endArray().setIndent("  ");
        }
        writer.endArray();
    }

    public static void deserialize(JsonObject json) {
        clearSystem();
        for (JsonElement element : array(json, "itemStats")) {
            JsonArray statArray = element.getAsJsonArray();
            Item item = item(statArray.get(0));
            if (item != null) {
                registerItem(item, ResistanceStats.deserialize(statArray.get(1).getAsJsonObject()));
            }
        }
        for (JsonElement element : array(json, "setStats")) {
            JsonArray statArray = element.getAsJsonArray();
            registerSet(item(statArray.get(0)), item(statArray.get(1)), item(statArray.get(2)), item(statArray.get(3)),
                    ResistanceStats.deserialize(statArray.get(4).getAsJsonObject()));
        }
        for (JsonElement element : array(json, "entityStats")) {
            JsonArray statArray = element.getAsJsonArray();
            Class<? extends Entity> entityClass = entityClass(statArray.get(0).getAsString());
            if (entityClass != null) {
                registerEntity(entityClass, ResistanceStats.deserialize(statArray.get(1).getAsJsonObject()));
            }
        }
    }

    private static void writeItemId(JsonWriter writer, Item item) throws IOException {
        if (item == null) {
            writer.nullValue();
            return;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null) {
            writer.nullValue();
        } else {
            writer.value(id.toString());
        }
    }

    private static JsonArray array(JsonObject json, String name) {
        JsonElement element = json.get(name);
        return element == null || !element.isJsonArray() ? new JsonArray() : element.getAsJsonArray();
    }

    private static Item item(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(element.getAsString());
        return id == null ? null : ForgeRegistries.ITEMS.getValue(id);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Entity> entityClass(String name) {
        try {
            Class<?> type = Class.forName(name);
            return Entity.class.isAssignableFrom(type) ? (Class<? extends Entity>) type : null;
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    private static void addSetInfo(Tuple.Quartet<Item, Item, Item, Item> set) {
        addSetInfo(set.getW(), set);
        addSetInfo(set.getX(), set);
        addSetInfo(set.getY(), set);
        addSetInfo(set.getZ(), set);
    }

    private static void addSetInfo(Item item, Tuple.Quartet<Item, Item, Item, Item> set) {
        if (item != null) {
            List<Tuple.Quartet<Item, Item, Item, Item>> sets = itemInfoSet.computeIfAbsent(item, ignored -> new ArrayList<>());
            if (!sets.contains(set)) {
                sets.add(set);
            }
        }
    }

    private static final class ItemStatsMap extends HashMap<Item, ResistanceStats> {
        @Override
        public ResistanceStats put(Item item, ResistanceStats stats) {
            ResistanceStats previous = super.put(item, stats);
            if (item != null && stats != null) {
                com.hbm.ntm.damage.DamageResistanceHandler.registerItem(item, stats.modern());
            }
            return previous;
        }
    }

    private static final class SetStatsMap extends HashMap<Tuple.Quartet<Item, Item, Item, Item>, ResistanceStats> {
        @Override
        public ResistanceStats put(Tuple.Quartet<Item, Item, Item, Item> set, ResistanceStats stats) {
            ResistanceStats previous = super.put(set, stats);
            if (set != null && stats != null) {
                addSetInfo(set);
                com.hbm.ntm.damage.DamageResistanceHandler.registerSet(set.getW(), set.getX(), set.getY(),
                        set.getZ(), stats.modern());
            }
            return previous;
        }
    }

    private static final class EntityStatsMap extends HashMap<Class<? extends Entity>, ResistanceStats> {
        @Override
        public ResistanceStats put(Class<? extends Entity> entityClass, ResistanceStats stats) {
            ResistanceStats previous = super.put(entityClass, stats);
            if (entityClass != null && stats != null) {
                com.hbm.ntm.damage.DamageResistanceHandler.registerEntity(entityClass, stats.modern());
            }
            return previous;
        }
    }

    public enum DamageClass {
        PHYSICAL,
        FIRE,
        EXPLOSIVE,
        ELECTRIC,
        PLASMA,
        LASER,
        MICROWAVE,
        SUBATOMIC,
        OTHER;

        public com.hbm.ntm.damage.DamageClass modern() {
            return com.hbm.ntm.damage.DamageClass.valueOf(name());
        }
    }

    public static class ResistanceStats {
        public HashMap<String, Resistance> exactResistances = new HashMap<>();
        public HashMap<String, Resistance> categoryResistances = new HashMap<>();
        public Resistance otherResistance;

        public Resistance getResistance(DamageSource source) {
            com.hbm.ntm.damage.DamageResistance resistance = modern().getResistance(source);
            return resistance == null ? null : Resistance.fromModern(resistance);
        }

        public ResistanceStats addExact(String type, float threshold, float resistance) {
            exactResistances.put(type.toLowerCase(Locale.US), new Resistance(threshold, resistance));
            return this;
        }

        public ResistanceStats addCategory(String type, float threshold, float resistance) {
            categoryResistances.put(categoryKey(type), new Resistance(threshold, resistance));
            return this;
        }

        public ResistanceStats addCategory(DamageClass type, float threshold, float resistance) {
            return addCategory(categoryKey(type), threshold, resistance);
        }

        public ResistanceStats setOther(float threshold, float resistance) {
            otherResistance = new Resistance(threshold, resistance);
            return this;
        }

        public com.hbm.ntm.damage.DamageResistanceStats modern() {
            com.hbm.ntm.damage.DamageResistanceStats stats = new com.hbm.ntm.damage.DamageResistanceStats();
            for (Map.Entry<String, Resistance> entry : exactResistances.entrySet()) {
                stats.addExact(entry.getKey(), entry.getValue().threshold, entry.getValue().resistance);
            }
            for (Map.Entry<String, Resistance> entry : categoryResistances.entrySet()) {
                stats.addCategory(entry.getKey(), entry.getValue().threshold, entry.getValue().resistance);
            }
            if (otherResistance != null) {
                stats.setOther(otherResistance.threshold, otherResistance.resistance);
            }
            return stats;
        }

        public static ResistanceStats fromModern(com.hbm.ntm.damage.DamageResistanceStats modern) {
            ResistanceStats stats = new ResistanceStats();
            for (Map.Entry<String, com.hbm.ntm.damage.DamageResistance> entry : modern.exactResistances().entrySet()) {
                stats.exactResistances.put(entry.getKey(), Resistance.fromModern(entry.getValue()));
            }
            for (Map.Entry<String, com.hbm.ntm.damage.DamageResistance> entry : modern.categoryResistances().entrySet()) {
                stats.categoryResistances.put(entry.getKey(), Resistance.fromModern(entry.getValue()));
            }
            if (modern.otherResistance() != null) {
                stats.otherResistance = Resistance.fromModern(modern.otherResistance());
            }
            return stats;
        }

        public void serialize(JsonWriter writer) throws IOException {
            if (!exactResistances.isEmpty()) {
                writer.name("exact").beginArray();
                for (Map.Entry<String, Resistance> entry : exactResistances.entrySet()) {
                    writer.beginArray().setIndent("");
                    writer.value(entry.getKey()).value(entry.getValue().threshold).value(entry.getValue().resistance);
                    writer.endArray().setIndent("  ");
                }
                writer.endArray();
            }
            if (!categoryResistances.isEmpty()) {
                writer.name("category").beginArray();
                for (Map.Entry<String, Resistance> entry : categoryResistances.entrySet()) {
                    writer.beginArray().setIndent("");
                    writer.value(entry.getKey()).value(entry.getValue().threshold).value(entry.getValue().resistance);
                    writer.endArray().setIndent("  ");
                }
                writer.endArray();
            }
            if (otherResistance != null) {
                writer.name("other").beginArray().setIndent("");
                writer.value(otherResistance.threshold).value(otherResistance.resistance);
                writer.endArray().setIndent("  ");
            }
        }

        public static ResistanceStats deserialize(JsonObject json) {
            ResistanceStats stats = new ResistanceStats();
            for (JsonElement element : array(json, "exact")) {
                JsonArray entry = element.getAsJsonArray();
                stats.exactResistances.put(entry.get(0).getAsString().toLowerCase(Locale.US),
                        new Resistance(entry.get(1).getAsFloat(), entry.get(2).getAsFloat()));
            }
            for (JsonElement element : array(json, "category")) {
                JsonArray entry = element.getAsJsonArray();
                stats.categoryResistances.put(categoryKey(entry.get(0).getAsString()),
                        new Resistance(entry.get(1).getAsFloat(), entry.get(2).getAsFloat()));
            }
            JsonArray other = array(json, "other");
            if (other.size() >= 2) {
                stats.otherResistance = new Resistance(other.get(0).getAsFloat(), other.get(1).getAsFloat());
            }
            return stats;
        }
    }

    public static class Resistance {
        public float threshold;
        public float resistance;

        public Resistance(float threshold, float resistance) {
            this.threshold = threshold;
            this.resistance = resistance;
        }

        public com.hbm.ntm.damage.DamageResistance modern() {
            return new com.hbm.ntm.damage.DamageResistance(threshold, resistance);
        }

        public static Resistance fromModern(com.hbm.ntm.damage.DamageResistance modern) {
            return new Resistance(modern.threshold(), modern.resistance());
        }
    }
}
