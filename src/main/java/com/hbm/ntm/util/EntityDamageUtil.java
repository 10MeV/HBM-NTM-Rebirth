package com.hbm.ntm.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Legacy-name entity damage/ray helper facade.
 */
@Deprecated(forRemoval = false)
public final class EntityDamageUtil {
    private EntityDamageUtil() {
    }

    public static boolean attackEntityFromIgnoreIFrame(Entity victim, DamageSource source, float damage) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromIgnoreIFrame(victim, source, damage);
    }

    public static boolean attackEntityFromIgnoreIFrame(Entity victim, ResourceKey<DamageType> type, float damage) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromIgnoreIFrame(victim, type, damage);
    }

    public static boolean attackEntityFromIgnoreIFrame(Entity victim, DamageResistanceHandler.DamageClass damageClass,
            float damage) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromIgnoreIFrame(victim, damageClass.modern(), damage);
    }

    public static boolean attackEntityFromIgnoreIFrame(Entity victim, String legacyTypeOrId, float damage) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromIgnoreIFrame(victim, legacyTypeOrId, damage);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, source, amount);
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, type, amount);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageResistanceHandler.DamageClass damageClass,
            float amount) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, damageClass.modern(), amount);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, legacyTypeOrId, amount);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, boolean ignoreIFrame) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, source, amount, ignoreIFrame);
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, type, amount, ignoreIFrame);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageResistanceHandler.DamageClass damageClass,
            float amount, boolean ignoreIFrame) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, damageClass.modern(), amount,
                ignoreIFrame);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount, boolean ignoreIFrame) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, legacyTypeOrId, amount, ignoreIFrame);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, source, amount, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount,
            float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, type, amount, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageResistanceHandler.DamageClass damageClass,
            float amount, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, damageClass.modern(), amount, pierceDt,
                pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount,
            float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, legacyTypeOrId, amount, pierceDt,
                pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, boolean ignoreIFrame,
            float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, source, amount, ignoreIFrame,
                pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, type, amount, ignoreIFrame,
                pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageResistanceHandler.DamageClass damageClass,
            float amount, boolean ignoreIFrame, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, damageClass.modern(), amount,
                ignoreIFrame, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount,
            boolean ignoreIFrame, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, legacyTypeOrId, amount, ignoreIFrame,
                pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, boolean ignoreIFrame,
            double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, source, amount, ignoreIFrame,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, type, amount, ignoreIFrame,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageResistanceHandler.DamageClass damageClass,
            float amount, boolean ignoreIFrame, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, damageClass.modern(), amount,
                ignoreIFrame, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount,
            boolean ignoreIFrame, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(entity, legacyTypeOrId, amount, ignoreIFrame,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(LivingEntity living, DamageSource source, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(living, source, amount, ignoreIFrame,
                allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(LivingEntity living, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(living, type, amount, ignoreIFrame,
                allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(LivingEntity living, DamageResistanceHandler.DamageClass damageClass,
            float amount, boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt,
            float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(living, damageClass.modern(), amount,
                ignoreIFrame, allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(LivingEntity living, String legacyTypeOrId, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNt(living, legacyTypeOrId, amount, ignoreIFrame,
                allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static com.hbm.ntm.damage.EntityDamageUtil.DamageApplication attackEntityFromNtDetailed(
            Entity entity, DamageSource source, float amount, boolean ignoreIFrame, boolean allowSpecialCancel,
            double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNtDetailed(entity, source, amount, ignoreIFrame,
                allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static com.hbm.ntm.damage.EntityDamageUtil.DamageApplication attackEntityFromNtDetailed(
            Entity entity, ResourceKey<DamageType> type, float amount, boolean ignoreIFrame, boolean allowSpecialCancel,
            double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNtDetailed(entity, type, amount, ignoreIFrame,
                allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static com.hbm.ntm.damage.EntityDamageUtil.DamageApplication attackEntityFromNtDetailed(
            Entity entity, DamageResistanceHandler.DamageClass damageClass, float amount, boolean ignoreIFrame,
            boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNtDetailed(entity, damageClass.modern(), amount,
                ignoreIFrame, allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static com.hbm.ntm.damage.EntityDamageUtil.DamageApplication attackEntityFromNtDetailed(
            Entity entity, String legacyTypeOrId, float amount, boolean ignoreIFrame, boolean allowSpecialCancel,
            double knockbackMultiplier, float pierceDt, float pierceDr) {
        return com.hbm.ntm.damage.EntityDamageUtil.attackEntityFromNtDetailed(entity, legacyTypeOrId, amount,
                ignoreIFrame, allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static com.hbm.ntm.damage.EntityDamageUtil.DamageApplication attackEntityFromNTDetailed(
            Entity entity, DamageSource source, float amount, boolean ignoreIFrame, boolean allowSpecialCancel,
            double knockbackMultiplier, float pierceDt, float pierceDr) {
        return attackEntityFromNtDetailed(entity, source, amount, ignoreIFrame, allowSpecialCancel,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static com.hbm.ntm.damage.EntityDamageUtil.DamageApplication attackEntityFromNTDetailed(
            Entity entity, ResourceKey<DamageType> type, float amount, boolean ignoreIFrame, boolean allowSpecialCancel,
            double knockbackMultiplier, float pierceDt, float pierceDr) {
        return attackEntityFromNtDetailed(entity, type, amount, ignoreIFrame, allowSpecialCancel,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static com.hbm.ntm.damage.EntityDamageUtil.DamageApplication attackEntityFromNTDetailed(
            Entity entity, DamageResistanceHandler.DamageClass damageClass, float amount, boolean ignoreIFrame,
            boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return attackEntityFromNtDetailed(entity, damageClass, amount, ignoreIFrame, allowSpecialCancel,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static com.hbm.ntm.damage.EntityDamageUtil.DamageApplication attackEntityFromNTDetailed(
            Entity entity, String legacyTypeOrId, float amount, boolean ignoreIFrame, boolean allowSpecialCancel,
            double knockbackMultiplier, float pierceDt, float pierceDr) {
        return attackEntityFromNtDetailed(entity, legacyTypeOrId, amount, ignoreIFrame, allowSpecialCancel,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, DamageSource source, float amount) {
        return attackEntityFromNt(entity, source, amount);
    }

    public static boolean attackEntityFromNT(Entity entity, ResourceKey<DamageType> type, float amount) {
        return attackEntityFromNt(entity, type, amount);
    }

    public static boolean attackEntityFromNT(Entity entity, DamageResistanceHandler.DamageClass damageClass,
            float amount) {
        return attackEntityFromNt(entity, damageClass, amount);
    }

    public static boolean attackEntityFromNT(Entity entity, String legacyTypeOrId, float amount) {
        return attackEntityFromNt(entity, legacyTypeOrId, amount);
    }

    public static boolean attackEntityFromNT(Entity entity, DamageSource source, float amount, boolean ignoreIFrame) {
        return attackEntityFromNt(entity, source, amount, ignoreIFrame);
    }

    public static boolean attackEntityFromNT(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame) {
        return attackEntityFromNt(entity, type, amount, ignoreIFrame);
    }

    public static boolean attackEntityFromNT(Entity entity, DamageResistanceHandler.DamageClass damageClass,
            float amount, boolean ignoreIFrame) {
        return attackEntityFromNt(entity, damageClass, amount, ignoreIFrame);
    }

    public static boolean attackEntityFromNT(Entity entity, String legacyTypeOrId, float amount, boolean ignoreIFrame) {
        return attackEntityFromNt(entity, legacyTypeOrId, amount, ignoreIFrame);
    }

    public static boolean attackEntityFromNT(Entity entity, DamageSource source, float amount, float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, source, amount, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, ResourceKey<DamageType> type, float amount,
            float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, type, amount, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, DamageResistanceHandler.DamageClass damageClass,
            float amount, float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, damageClass, amount, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, String legacyTypeOrId, float amount,
            float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, legacyTypeOrId, amount, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, DamageSource source, float amount, boolean ignoreIFrame,
            float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, source, amount, ignoreIFrame, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame, float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, type, amount, ignoreIFrame, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, DamageResistanceHandler.DamageClass damageClass,
            float amount, boolean ignoreIFrame, float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, damageClass, amount, ignoreIFrame, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, String legacyTypeOrId, float amount,
            boolean ignoreIFrame, float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, legacyTypeOrId, amount, ignoreIFrame, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, DamageSource source, float amount, boolean ignoreIFrame,
            double knockbackMultiplier, float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, source, amount, ignoreIFrame, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, type, amount, ignoreIFrame, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, DamageResistanceHandler.DamageClass damageClass,
            float amount, boolean ignoreIFrame, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, damageClass, amount, ignoreIFrame, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(Entity entity, String legacyTypeOrId, float amount,
            boolean ignoreIFrame, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, legacyTypeOrId, amount, ignoreIFrame, knockbackMultiplier, pierceDt,
                pierceDr);
    }

    public static boolean attackEntityFromNT(LivingEntity living, DamageSource source, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return attackEntityFromNt(living, source, amount, ignoreIFrame, allowSpecialCancel, knockbackMultiplier,
                pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(LivingEntity living, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return attackEntityFromNt(living, type, amount, ignoreIFrame, allowSpecialCancel, knockbackMultiplier,
                pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(LivingEntity living, DamageResistanceHandler.DamageClass damageClass,
            float amount, boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt,
            float pierceDr) {
        return attackEntityFromNt(living, damageClass, amount, ignoreIFrame, allowSpecialCancel, knockbackMultiplier,
                pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNT(LivingEntity living, String legacyTypeOrId, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return attackEntityFromNt(living, legacyTypeOrId, amount, ignoreIFrame, allowSpecialCancel,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static void damageArmorNT(LivingEntity living, float amount) {
        // Legacy 1.7.10 method body is intentionally empty.
    }

    public static void damageArmorNt(LivingEntity living, float amount) {
        damageArmorNT(living, amount);
    }

    public static HitResult getMouseOver(Player attacker, double reach) {
        return RayTraceUtil.getMouseOver(attacker, reach);
    }

    public static HitResult getMouseOver(Player attacker, double reach, double threshold) {
        return RayTraceUtil.getMouseOver(attacker, reach, threshold);
    }

    public static BlockHitResult rayTrace(Player player, double distance, float partialTick) {
        return RayTraceUtil.rayTrace(player, distance, partialTick);
    }

    public static Vec3 getPosition(Player player) {
        return RayTraceUtil.getPosition(player, 1.0F);
    }
}
