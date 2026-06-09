package com.hbm.ntm.damage;

import net.minecraft.world.damagesource.DamageSource;

import java.util.HashMap;
import java.util.Map;

public class DamageResistanceStats {
    private final Map<String, DamageResistance> exactResistances = new HashMap<>();
    private final Map<String, DamageResistance> categoryResistances = new HashMap<>();
    private DamageResistance otherResistance;

    public DamageResistance getResistance(DamageSource source) {
        ResistanceMatch match = match(source);
        return match == null ? null : match.resistance();
    }

    public ResistanceMatch match(DamageSource source) {
        String exactKey = DamageResistanceHandler.exactTypeKey(source);
        String registryKey = DamageResistanceHandler.registryTypeKey(source);
        String categoryKey = DamageResistanceHandler.typeToCategory(source);
        return matchKeys(exactKey, registryKey, categoryKey,
                DamageResistanceHandler.isUnblockableForLegacyResistance(source));
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

    public DamageResistanceStats addCategory(String type, float threshold, float resistance) {
        categoryResistances.put(DamageResistanceHandler.categoryKey(type), new DamageResistance(threshold, resistance));
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

    public record ResistanceMatch(String kind, String key, DamageResistance resistance) {
    }
}
