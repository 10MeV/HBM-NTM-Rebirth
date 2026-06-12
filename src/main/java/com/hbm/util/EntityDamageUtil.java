package com.hbm.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Legacy 1.7.10 package bridge for HBM entity damage and ray helpers.
 */
@Deprecated(forRemoval = false)
public final class EntityDamageUtil {
    private EntityDamageUtil() {
    }

    public static boolean attackEntityFromIgnoreIFrame(Entity victim, DamageSource source, float damage) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromIgnoreIFrame(victim, source, damage);
    }

    public static boolean attackEntityFromIgnoreIFrame(Entity victim, ResourceKey<DamageType> type, float damage) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromIgnoreIFrame(victim, type, damage);
    }

    public static boolean attackEntityFromIgnoreIFrame(Entity victim, String legacyTypeOrId, float damage) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromIgnoreIFrame(victim, legacyTypeOrId, damage);
    }

    public static boolean attackEntityFromNT(Entity entity, DamageSource source, float amount) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(entity, source, amount);
    }

    public static boolean attackEntityFromNT(Entity entity, ResourceKey<DamageType> type, float amount) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(entity, type, amount);
    }

    public static boolean attackEntityFromNT(Entity entity, String legacyTypeOrId, float amount) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(entity, legacyTypeOrId, amount);
    }

    public static boolean attackEntityFromNT(Entity entity, DamageSource source, float amount, boolean ignoreIFrame) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(entity, source, amount, ignoreIFrame);
    }

    public static boolean attackEntityFromNT(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(entity, type, amount, ignoreIFrame);
    }

    public static boolean attackEntityFromNT(Entity entity, String legacyTypeOrId, float amount, boolean ignoreIFrame) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(entity, legacyTypeOrId, amount, ignoreIFrame);
    }

    public static boolean attackEntityFromNT(Entity entity, DamageSource source, float amount, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(entity, source, amount, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, ResourceKey<DamageType> type, float amount,
            float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(entity, type, amount, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, String legacyTypeOrId, float amount,
            float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(entity, legacyTypeOrId, amount, pierceDt,
                pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, DamageSource source, float amount, boolean ignoreIFrame,
            float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(entity, source, amount, ignoreIFrame,
                pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(entity, type, amount, ignoreIFrame,
                pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, String legacyTypeOrId, float amount,
            boolean ignoreIFrame, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(entity, legacyTypeOrId, amount, ignoreIFrame,
                pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, DamageSource source, float amount, boolean ignoreIFrame,
            double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(entity, source, amount, ignoreIFrame,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(entity, type, amount, ignoreIFrame,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, String legacyTypeOrId, float amount,
            boolean ignoreIFrame, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(entity, legacyTypeOrId, amount, ignoreIFrame,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(LivingEntity living, DamageSource source, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(living, source, amount, ignoreIFrame,
                allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(LivingEntity living, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(living, type, amount, ignoreIFrame,
                allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(LivingEntity living, String legacyTypeOrId, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNT(living, legacyTypeOrId, amount, ignoreIFrame,
                allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(entity, source, amount);
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(entity, type, amount);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(entity, legacyTypeOrId, amount);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, boolean ignoreIFrame) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(entity, source, amount, ignoreIFrame);
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(entity, type, amount, ignoreIFrame);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount, boolean ignoreIFrame) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(entity, legacyTypeOrId, amount, ignoreIFrame);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(entity, source, amount, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount,
            float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(entity, type, amount, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount,
            float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(entity, legacyTypeOrId, amount, pierceDt,
                pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, boolean ignoreIFrame,
            float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(entity, source, amount, ignoreIFrame,
                pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(entity, type, amount, ignoreIFrame,
                pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount,
            boolean ignoreIFrame, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(entity, legacyTypeOrId, amount, ignoreIFrame,
                pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, boolean ignoreIFrame,
            double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(entity, source, amount, ignoreIFrame,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(entity, type, amount, ignoreIFrame,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount,
            boolean ignoreIFrame, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(entity, legacyTypeOrId, amount, ignoreIFrame,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(LivingEntity living, DamageSource source, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(living, source, amount, ignoreIFrame,
                allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(LivingEntity living, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(living, type, amount, ignoreIFrame,
                allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(LivingEntity living, String legacyTypeOrId, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.util.EntityDamageUtil.attackEntityFromNt(living, legacyTypeOrId, amount, ignoreIFrame,
                allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static void damageArmorNT(LivingEntity living, float amount) {
        com.hbm.ntm.util.EntityDamageUtil.damageArmorNT(living, amount);
    }

    public static void damageArmorNt(LivingEntity living, float amount) {
        com.hbm.ntm.util.EntityDamageUtil.damageArmorNt(living, amount);
    }

    public static HitResult getMouseOver(Player attacker, double reach) {
        return com.hbm.ntm.util.EntityDamageUtil.getMouseOver(attacker, reach);
    }

    public static HitResult getMouseOver(Player attacker, double reach, double threshold) {
        return com.hbm.ntm.util.EntityDamageUtil.getMouseOver(attacker, reach, threshold);
    }

    public static BlockHitResult rayTrace(Player player, double distance, float partialTick) {
        return com.hbm.ntm.util.EntityDamageUtil.rayTrace(player, distance, partialTick);
    }

    public static Vec3 getPosition(Player player) {
        return com.hbm.ntm.util.EntityDamageUtil.getPosition(player);
    }
}
