package com.hbm.ntm.compat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import net.minecraft.world.entity.Entity;

/**
 * External turret targeting registry migrated from the 1.7.10 CompatExternal API.
 * The registry is ready for modern turret consumers, but does not target anything
 * by itself.
 */
public final class CompatTurretTargetRegistry {
    private static final Map<TargetType, Set<Class<? extends Entity>>> SIMPLE_TARGETS =
            new EnumMap<>(TargetType.class);
    private static final Set<Class<? extends Entity>> BLACKLIST = ConcurrentHashMap.newKeySet();
    private static final Map<Class<? extends Entity>, BiFunction<Entity, Object, Integer>> CONDITIONS =
            new ConcurrentHashMap<>();

    static {
        for (TargetType type : TargetType.values()) {
            SIMPLE_TARGETS.put(type, ConcurrentHashMap.newKeySet());
        }
    }

    public static void registerSimple(Class<? extends Entity> clazz, TargetType type) {
        if (clazz != null && type != null) {
            SIMPLE_TARGETS.get(type).add(clazz);
        }
    }

    public static void registerSimple(Class<? extends Entity> clazz, int legacyType) {
        TargetType type = TargetType.fromLegacyId(legacyType);
        if (type != null) {
            registerSimple(clazz, type);
        }
    }

    public static void registerBlacklist(Class<? extends Entity> clazz) {
        if (clazz != null) {
            BLACKLIST.add(clazz);
        }
    }

    public static void registerCondition(Class<? extends Entity> clazz,
            BiFunction<Entity, Object, Integer> condition) {
        if (clazz != null && condition != null) {
            CONDITIONS.put(clazz, condition);
        }
    }

    public static boolean isBlacklisted(Entity entity) {
        return entity != null && matchesAny(entity, BLACKLIST);
    }

    public static boolean isSimpleTarget(Entity entity, TargetType type) {
        return entity != null && type != null && matchesAny(entity, SIMPLE_TARGETS.get(type));
    }

    /**
     * Legacy return contract: -1 ignore, 0 continue normal checks, 1 target.
     */
    public static int evaluateCondition(Entity entity, Object turret) {
        if (entity == null) {
            return 0;
        }
        for (Map.Entry<Class<? extends Entity>, BiFunction<Entity, Object, Integer>> entry : CONDITIONS.entrySet()) {
            if (entry.getKey().isAssignableFrom(entity.getClass())) {
                Integer result = entry.getValue().apply(entity, turret);
                if (result != null && result != 0) {
                    return result < 0 ? -1 : 1;
                }
            }
        }
        return 0;
    }

    public static List<Class<? extends Entity>> simpleTargets(TargetType type) {
        return type == null ? List.of() : List.copyOf(SIMPLE_TARGETS.get(type));
    }

    public static List<Class<? extends Entity>> blacklist() {
        return List.copyOf(BLACKLIST);
    }

    public static Map<Class<? extends Entity>, BiFunction<Entity, Object, Integer>> conditions() {
        return Map.copyOf(CONDITIONS);
    }

    public static Diagnostics diagnostics() {
        return new Diagnostics(
                SIMPLE_TARGETS.get(TargetType.PLAYER).size(),
                SIMPLE_TARGETS.get(TargetType.FRIENDLY).size(),
                SIMPLE_TARGETS.get(TargetType.HOSTILE).size(),
                SIMPLE_TARGETS.get(TargetType.MACHINE).size(),
                BLACKLIST.size(),
                CONDITIONS.size());
    }

    private static boolean matchesAny(Entity entity, Set<Class<? extends Entity>> classes) {
        for (Class<? extends Entity> clazz : classes) {
            if (clazz.isAssignableFrom(entity.getClass())) {
                return true;
            }
        }
        return false;
    }

    public enum TargetType {
        PLAYER(0),
        FRIENDLY(1),
        HOSTILE(2),
        MACHINE(3);

        private final int legacyId;

        TargetType(int legacyId) {
            this.legacyId = legacyId;
        }

        public int legacyId() {
            return legacyId;
        }

        public static TargetType fromLegacyId(int legacyId) {
            for (TargetType type : values()) {
                if (type.legacyId == legacyId) {
                    return type;
                }
            }
            return null;
        }
    }

    public record Diagnostics(int playerTargets, int friendlyTargets, int hostileTargets, int machineTargets,
                              int blacklistedTargets, int conditionalTargets) {
        public int totalRegistrations() {
            return playerTargets + friendlyTargets + hostileTargets + machineTargets
                    + blacklistedTargets + conditionalTargets;
        }

        public String summary() {
            return "turret targets player=" + playerTargets
                    + " friendly=" + friendlyTargets
                    + " hostile=" + hostileTargets
                    + " machine=" + machineTargets
                    + " blacklist=" + blacklistedTargets
                    + " conditions=" + conditionalTargets;
        }
    }

    private CompatTurretTargetRegistry() {
    }
}
