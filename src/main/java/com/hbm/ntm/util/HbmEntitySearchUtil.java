package com.hbm.ntm.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public final class HbmEntitySearchUtil {
    private HbmEntitySearchUtil() {
    }

    public static <T extends Entity> T getClosestEntity(Level level, Vec3 position, double radius, Class<T> type,
            Predicate<? super T> predicate) {
        if (level == null || position == null || type == null) {
            return null;
        }
        Predicate<? super T> filter = predicate == null ? entity -> true : predicate;
        AABB bounds = radius < 0.0D
                ? new AABB(position, position).inflate(Double.MAX_VALUE / 4.0D)
                : new AABB(position, position).inflate(radius);
        List<T> entities = level.getEntitiesOfClass(type, bounds, entity -> entity.isAlive() && filter.test(entity));
        double closestDistance = -1.0D;
        T closest = null;
        double radiusSquared = radius * radius;
        for (T entity : entities) {
            double distance = entity.distanceToSqr(position);
            if ((radius < 0.0D || distance < radiusSquared) && (closestDistance == -1.0D || distance < closestDistance)) {
                closestDistance = distance;
                closest = entity;
            }
        }
        return closest;
    }

    public static Player getClosestPlayerForSound(Level level, Vec3 position, double radius) {
        return getClosestEntity(level, position, radius, Player.class, player -> true);
    }

    public static LivingEntity getClosestVulnerableLiving(Level level, Vec3 position, double radius,
            Predicate<? super LivingEntity> predicate) {
        return getClosestEntity(level, position, radius, LivingEntity.class,
                entity -> !(entity instanceof Player player && player.getAbilities().invulnerable)
                        && (predicate == null || predicate.test(entity)));
    }
}
