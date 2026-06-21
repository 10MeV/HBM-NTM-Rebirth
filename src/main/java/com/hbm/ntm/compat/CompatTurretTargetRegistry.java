package com.hbm.ntm.compat;

import com.hbm.ntm.api.entity.LegacyMissileRadarDetectable;
import com.hbm.ntm.api.entity.RadarContext;
import com.hbm.ntm.api.entity.RadarDetectable;
import com.hbm.ntm.turret.TurretBlockEntityBase;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class CompatTurretTargetRegistry {
    private static final List<Predicate<Entity>> BLACKLIST = new CopyOnWriteArrayList<>();
    private static final List<BiFunction<Entity, TurretBlockEntityBase, TargetDecision>> CONDITIONS =
            new CopyOnWriteArrayList<>();
    private static final List<Predicate<Entity>> FRIENDLY = new CopyOnWriteArrayList<>();
    private static final List<Predicate<Entity>> HOSTILE = new CopyOnWriteArrayList<>();
    private static final List<Predicate<Entity>> MACHINE = new CopyOnWriteArrayList<>();
    private static final List<Predicate<Entity>> PLAYER = new CopyOnWriteArrayList<>();
    private static final List<Class<? extends Entity>> BLACKLIST_CLASSES = new CopyOnWriteArrayList<>();
    private static final List<ClassCondition> CLASS_CONDITIONS = new CopyOnWriteArrayList<>();
    private static final List<Class<? extends Entity>> FRIENDLY_CLASSES = new CopyOnWriteArrayList<>();
    private static final List<Class<? extends Entity>> HOSTILE_CLASSES = new CopyOnWriteArrayList<>();
    private static final List<Class<? extends Entity>> MACHINE_CLASSES = new CopyOnWriteArrayList<>();
    private static final List<Class<? extends Entity>> PLAYER_CLASSES = new CopyOnWriteArrayList<>();

    static {
        registerFriendly(entity -> entity instanceof Animal
                || entity instanceof Npc
                || entity instanceof TamableAnimal
                || (entity instanceof Enemy
                && !(entity instanceof EnderDragon)
                && !(entity instanceof EnderDragonPart)));
        registerHostile(entity -> entity instanceof Enemy);
        registerMachine(entity -> entity instanceof AbstractMinecart);
        registerPlayer(entity -> entity instanceof Player && !(entity instanceof FakePlayer));
    }

    private CompatTurretTargetRegistry() {
    }

    public static void registerBlacklist(Predicate<Entity> predicate) {
        BLACKLIST.add(predicate);
    }

    public static void registerBlacklist(Class<? extends Entity> clazz) {
        BLACKLIST_CLASSES.add(clazz);
    }

    public static void registerCondition(BiFunction<Entity, TurretBlockEntityBase, TargetDecision> condition) {
        CONDITIONS.add(condition);
    }

    public static void registerCondition(Class<? extends Entity> clazz, BiFunction<Entity, Object, Integer> condition) {
        CLASS_CONDITIONS.add(new ClassCondition(clazz, condition));
    }

    public static void registerSimple(Class<? extends Entity> clazz, int type) {
        if (type < 0 || type > 3) {
            return;
        }
        registerSimple(clazz, TargetType.fromLegacyInt(type));
    }

    public static void registerSimple(Class<? extends Entity> clazz, TargetType type) {
        switch (type) {
            case FRIENDLY -> FRIENDLY_CLASSES.add(clazz);
            case HOSTILE -> HOSTILE_CLASSES.add(clazz);
            case MACHINE -> MACHINE_CLASSES.add(clazz);
            case PLAYER -> PLAYER_CLASSES.add(clazz);
        }
    }

    public static void registerFriendly(Predicate<Entity> predicate) {
        FRIENDLY.add(predicate);
    }

    public static void registerHostile(Predicate<Entity> predicate) {
        HOSTILE.add(predicate);
    }

    public static void registerMachine(Predicate<Entity> predicate) {
        MACHINE.add(predicate);
    }

    public static void registerPlayer(Predicate<Entity> predicate) {
        PLAYER.add(predicate);
    }

    public static boolean isBlacklisted(Entity entity) {
        return BLACKLIST.stream().anyMatch(predicate -> predicate.test(entity))
                || BLACKLIST_CLASSES.stream().anyMatch(clazz -> clazz.isAssignableFrom(entity.getClass()));
    }

    public static TargetDecision evaluateConditions(Entity entity, TurretBlockEntityBase turret) {
        for (ClassCondition condition : CLASS_CONDITIONS) {
            if (condition.clazz().isAssignableFrom(entity.getClass())) {
                int result = condition.condition().apply(entity, turret);
                if (result < 0) {
                    return TargetDecision.REJECT;
                }
                if (result > 0) {
                    return TargetDecision.ACCEPT;
                }
            }
        }
        for (BiFunction<Entity, TurretBlockEntityBase, TargetDecision> condition : CONDITIONS) {
            TargetDecision decision = condition.apply(entity, turret);
            if (decision != null && decision != TargetDecision.PASS) {
                return decision;
            }
        }
        return TargetDecision.PASS;
    }

    public static boolean isFriendly(Entity entity) {
        return FRIENDLY.stream().anyMatch(predicate -> predicate.test(entity))
                || FRIENDLY_CLASSES.stream().anyMatch(clazz -> clazz.isAssignableFrom(entity.getClass()));
    }

    public static boolean isHostile(Entity entity) {
        return HOSTILE.stream().anyMatch(predicate -> predicate.test(entity))
                || HOSTILE_CLASSES.stream().anyMatch(clazz -> clazz.isAssignableFrom(entity.getClass()));
    }

    public static boolean isMachine(Entity entity) {
        return isMachine(entity, null);
    }

    public static boolean isMachine(Entity entity, TurretBlockEntityBase turret) {
        if (turret != null && entity instanceof RadarDetectable detectable
                && turret.getLevel() instanceof ServerLevel serverLevel
                && !detectable.canBeSeenBy(RadarContext.legacy(serverLevel, turret.getBlockPos()))) {
            return false;
        }
        if (entity instanceof LegacyMissileRadarDetectable missile) {
            return missile.radarVerticalMotion() < 0.0D;
        }
        return MACHINE.stream().anyMatch(predicate -> predicate.test(entity))
                || MACHINE_CLASSES.stream().anyMatch(clazz -> clazz.isAssignableFrom(entity.getClass()));
    }

    public static boolean isPlayer(Entity entity) {
        if (entity instanceof FakePlayer) {
            return false;
        }
        return PLAYER.stream().anyMatch(predicate -> predicate.test(entity))
                || PLAYER_CLASSES.stream().anyMatch(clazz -> clazz.isAssignableFrom(entity.getClass()));
    }

    public static Diagnostics diagnostics() {
        return new Diagnostics(
                BLACKLIST.size() + BLACKLIST_CLASSES.size(),
                CONDITIONS.size() + CLASS_CONDITIONS.size(),
                FRIENDLY.size() + FRIENDLY_CLASSES.size(),
                HOSTILE.size() + HOSTILE_CLASSES.size(),
                MACHINE.size() + MACHINE_CLASSES.size(),
                PLAYER.size() + PLAYER_CLASSES.size());
    }

    public enum TargetDecision {
        ACCEPT,
        REJECT,
        PASS
    }

    public enum TargetType {
        FRIENDLY,
        HOSTILE,
        MACHINE,
        PLAYER;

        private static TargetType fromLegacyInt(int type) {
            return switch (type) {
                case 0 -> PLAYER;
                case 1 -> FRIENDLY;
                case 2 -> HOSTILE;
                case 3 -> MACHINE;
                default -> throw new IllegalArgumentException("Invalid turret target type: " + type);
            };
        }
    }

    public record Diagnostics(int blacklists, int conditions, int friendly, int hostile, int machine, int player) {
        public int totalRegistrations() {
            return blacklists + conditions + friendly + hostile + machine + player;
        }

        public String summary() {
            return "turret target hooks: blacklist=" + blacklists
                    + " conditions=" + conditions
                    + " friendly=" + friendly
                    + " hostile=" + hostile
                    + " machine=" + machine
                    + " player=" + player
                    + " total=" + totalRegistrations();
        }
    }

    private record ClassCondition(Class<? extends Entity> clazz, BiFunction<Entity, Object, Integer> condition) {
    }
}
