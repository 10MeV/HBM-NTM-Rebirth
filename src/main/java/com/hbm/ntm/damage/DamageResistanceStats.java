package com.hbm.ntm.damage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hbm.ntm.radiation.ModDamageSources;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DamageResistanceStats {
    private final Map<String, DamageResistance> exactResistances = new HashMap<>();
    private final Map<String, DamageResistance> categoryResistances = new HashMap<>();
    private DamageResistance otherResistance;

    public DamageResistance getResistance(DamageSource source) {
        ResistanceMatch match = match(source);
        return match == null ? null : match.resistance();
    }

    public DamageResistance getResistance(ResourceKey<DamageType> type) {
        ResistanceMatch match = match(type);
        return match == null ? null : match.resistance();
    }

    public DamageResistance getResistance(String legacyTypeOrId) {
        ResistanceMatch match = match(legacyTypeOrId);
        return match == null ? null : match.resistance();
    }

    public DamageResistance getResistance(DamageClass damageClass) {
        ResistanceMatch match = match(damageClass);
        return match == null ? null : match.resistance();
    }

    public ResistanceMatch match(DamageSource source) {
        if (source == null) {
            return null;
        }
        String exactKey = DamageResistanceHandler.exactTypeKey(source);
        String registryKey = DamageResistanceHandler.registryTypeKey(source);
        String categoryKey = DamageResistanceHandler.typeToCategory(source);
        return matchKeys(exactKey, registryKey, categoryKey,
                DamageResistanceHandler.isUnblockableForLegacyResistance(source));
    }

    public ResistanceMatch match(ResourceKey<DamageType> type) {
        String exactKey = ModDamageSources.legacyDamageType(type)
                .map(ModDamageSources.LegacyDamageType::expectedMessageId)
                .map(DamageResistanceHandler::exactTypeKey)
                .orElseGet(() -> DamageResistanceHandler.exactTypeKey(type));
        String registryKey = DamageResistanceHandler.exactTypeKey(type);
        return matchKeys(exactKey, registryKey, DamageResistanceHandler.typeToCategory(type),
                DamageResistanceHandler.isUnblockableForLegacyResistance(type));
    }

    public ResistanceMatch match(String legacyTypeOrId) {
        if (legacyTypeOrId == null || legacyTypeOrId.isBlank()) {
            return null;
        }
        ResourceKey<DamageType> type = ModDamageSources.legacyKey(legacyTypeOrId).orElse(null);
        if (type != null) {
            String exactKey = ModDamageSources.legacyDamageType(type)
                    .map(ModDamageSources.LegacyDamageType::expectedMessageId)
                    .map(DamageResistanceHandler::exactTypeKey)
                    .orElseGet(() -> DamageResistanceHandler.exactTypeKey(legacyTypeOrId));
            String registryKey = DamageResistanceHandler.exactTypeKey(type);
            return matchKeys(exactKey, registryKey, DamageResistanceHandler.typeToCategory(type),
                    DamageResistanceHandler.isUnblockableForLegacyResistance(type));
        }
        String exactKey = DamageResistanceHandler.exactTypeKey(legacyTypeOrId);
        return matchKeys(exactKey, null, DamageResistanceHandler.typeToCategory(legacyTypeOrId),
                DamageResistanceHandler.isUnblockableForLegacyResistance(legacyTypeOrId));
    }

    public ResistanceMatch match(DamageClass damageClass) {
        return match(ModDamageSources.damageClassKey(damageClass));
    }

    ResistanceMatch matchKeys(String exactKey, String registryKey, String categoryKey, boolean unblockable) {
        DamageResistance exact = exactResistances.get(exactKey);
        if (exact != null) {
            return new ResistanceMatch("exact", exactKey, exact);
        }
        if (registryKey != null && !registryKey.equals(exactKey)) {
            exact = exactResistances.get(registryKey);
            if (exact != null) {
                return new ResistanceMatch("exact", registryKey, exact);
            }
        }
        DamageResistance category = categoryResistances.get(categoryKey);
        if (category != null) {
            return new ResistanceMatch("category", categoryKey, category);
        }
        if (unblockable || otherResistance == null) {
            return null;
        }
        return new ResistanceMatch("other", "other", otherResistance);
    }

    public DamageResistanceStats addExact(String type, float threshold, float resistance) {
        exactResistances.put(DamageResistanceHandler.exactTypeKey(type), new DamageResistance(threshold, resistance));
        return this;
    }

    public DamageResistanceStats addExact(ResourceKey<DamageType> type, float threshold, float resistance) {
        exactResistances.put(DamageResistanceHandler.exactTypeKey(type), new DamageResistance(threshold, resistance));
        return this;
    }

    public DamageResistanceStats addExact(DamageClass type, float threshold, float resistance) {
        exactResistances.put(DamageResistanceHandler.exactTypeKey(type), new DamageResistance(threshold, resistance));
        return this;
    }

    public DamageResistanceStats addCategory(String type, float threshold, float resistance) {
        categoryResistances.put(DamageResistanceHandler.categoryKey(type), new DamageResistance(threshold, resistance));
        return this;
    }

    public DamageResistanceStats addCategory(ResourceKey<DamageType> type, float threshold, float resistance) {
        categoryResistances.put(DamageResistanceHandler.typeToCategory(type), new DamageResistance(threshold, resistance));
        return this;
    }

    public DamageResistanceStats addCategory(DamageClass type, float threshold, float resistance) {
        categoryResistances.put(DamageResistanceHandler.categoryKey(type), new DamageResistance(threshold, resistance));
        return this;
    }

    public DamageResistanceStats setOther(float threshold, float resistance) {
        otherResistance = new DamageResistance(threshold, resistance);
        return this;
    }

    public Map<String, DamageResistance> exactResistances() {
        return Map.copyOf(exactResistances);
    }

    public Map<String, DamageResistance> categoryResistances() {
        return Map.copyOf(categoryResistances);
    }

    public DamageResistance otherResistance() {
        return otherResistance;
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        if (!exactResistances.isEmpty()) {
            JsonArray exact = new JsonArray();
            for (Map.Entry<String, DamageResistance> entry : exactResistances.entrySet()) {
                exact.add(resistance(entry.getKey(), entry.getValue()));
            }
            object.add("exact", exact);
        }
        if (!categoryResistances.isEmpty()) {
            JsonArray category = new JsonArray();
            for (Map.Entry<String, DamageResistance> entry : categoryResistances.entrySet()) {
                category.add(resistance(entry.getKey(), entry.getValue()));
            }
            object.add("category", category);
        }
        if (otherResistance != null) {
            JsonArray other = new JsonArray();
            other.add(otherResistance.threshold());
            other.add(otherResistance.resistance());
            object.add("other", other);
        }
        return object;
    }

    public static DamageResistanceStats fromJson(JsonElement statsElement) {
        return parseJson(statsElement, "damage resistance stats").stats();
    }

    public static JsonParseResult parseJson(JsonElement statsElement, String context) {
        List<String> warnings = new ArrayList<>();
        if (statsElement == null || !statsElement.isJsonObject()) {
            warnings.add("invalid resistance object in " + context);
            return new JsonParseResult(null, List.copyOf(warnings));
        }
        JsonObject json = statsElement.getAsJsonObject();
        DamageResistanceStats stats = new DamageResistanceStats();
        int index = 0;
        for (JsonElement exactElement : array(json, "exact")) {
            if (!isArray(exactElement, 3)) {
                warnings.add("invalid exact resistance in " + context + " #" + index);
                index++;
                continue;
            }
            JsonArray array = exactElement.getAsJsonArray();
            String key = stringValue(array.get(0));
            Float threshold = floatValue(array.get(1));
            Float resistance = floatValue(array.get(2));
            if (key == null || threshold == null || resistance == null) {
                warnings.add("invalid exact resistance values in " + context + " #" + index);
            } else {
                stats.addExact(key, threshold, resistance);
            }
            index++;
        }
        index = 0;
        for (JsonElement categoryElement : array(json, "category")) {
            if (!isArray(categoryElement, 3)) {
                warnings.add("invalid category resistance in " + context + " #" + index);
                index++;
                continue;
            }
            JsonArray array = categoryElement.getAsJsonArray();
            String key = stringValue(array.get(0));
            Float threshold = floatValue(array.get(1));
            Float resistance = floatValue(array.get(2));
            if (key == null || threshold == null || resistance == null) {
                warnings.add("invalid category resistance values in " + context + " #" + index);
            } else {
                stats.addCategory(key, threshold, resistance);
            }
            index++;
        }
        if (json.has("other")) {
            if (!isArray(json.get("other"), 2)) {
                warnings.add("invalid other resistance in " + context);
            } else {
                JsonArray other = json.getAsJsonArray("other");
                Float threshold = floatValue(other.get(0));
                Float resistance = floatValue(other.get(1));
                if (threshold == null || resistance == null) {
                    warnings.add("invalid other resistance values in " + context);
                } else {
                    stats.setOther(threshold, resistance);
                }
            }
        }
        return new JsonParseResult(stats, List.copyOf(warnings));
    }

    private static JsonArray resistance(String key, DamageResistance resistance) {
        JsonArray array = new JsonArray();
        array.add(key);
        array.add(resistance.threshold());
        array.add(resistance.resistance());
        return array;
    }

    private static JsonArray array(JsonObject json, String key) {
        return json != null && json.has(key) && json.get(key).isJsonArray() ? json.getAsJsonArray(key) : new JsonArray();
    }

    private static boolean isArray(JsonElement element, int minSize) {
        return element != null && element.isJsonArray() && element.getAsJsonArray().size() >= minSize;
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

    public record ResistanceMatch(String kind, String key, DamageResistance resistance) {
    }

    public record JsonParseResult(DamageResistanceStats stats, List<String> warnings) {
    }
}
