package com.hbm.ntm.radiation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class RadiationData {
    private static final String TAG_ROOT = "hbm_radiation";
    private static final String TAG_RADIATION = "hfr_radiation";
    private static final String TAG_RAD_ENV = "hfr_rad_env";
    private static final String TAG_RAD_BUF = "hfr_rad_buf";

    public static float getRadiation(LivingEntity entity) {
        return getTag(entity).getFloat(TAG_RADIATION);
    }

    public static void setRadiation(LivingEntity entity, float radiation) {
        getTag(entity).putFloat(TAG_RADIATION, clampPlayerRadiation(radiation));
    }

    public static void incrementRadiation(LivingEntity entity, float amount) {
        setRadiation(entity, getRadiation(entity) + amount);
    }

    public static float getRadEnv(LivingEntity entity) {
        return getTag(entity).getFloat(TAG_RAD_ENV);
    }

    public static void setRadEnv(LivingEntity entity, float radiation) {
        getTag(entity).putFloat(TAG_RAD_ENV, Math.max(0.0F, radiation));
    }

    public static float getRadBuf(LivingEntity entity) {
        return getTag(entity).getFloat(TAG_RAD_BUF);
    }

    public static void setRadBuf(LivingEntity entity, float radiation) {
        getTag(entity).putFloat(TAG_RAD_BUF, Math.max(0.0F, radiation));
    }

    public static void copyForRespawn(LivingEntity original, LivingEntity replacement) {
        if (original.getPersistentData().contains(TAG_ROOT)) {
            replacement.getPersistentData().put(TAG_ROOT, original.getPersistentData().getCompound(TAG_ROOT).copy());
        }
    }

    private static CompoundTag getTag(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        if (!persistentData.contains(TAG_ROOT)) {
            persistentData.put(TAG_ROOT, new CompoundTag());
        }
        return persistentData.getCompound(TAG_ROOT);
    }

    private static float clampPlayerRadiation(float value) {
        return Mth.clamp(value, 0.0F, RadiationConstants.MAX_PLAYER_RADIATION);
    }

    private RadiationData() {
    }
}
