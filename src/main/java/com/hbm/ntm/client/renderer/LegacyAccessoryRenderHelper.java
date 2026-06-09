package com.hbm.ntm.client.renderer;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * Shared formulas from the old RenderAccessoryUtility wearable renderers.
 */
public final class LegacyAccessoryRenderHelper {
    public static final float BIPED_MODEL_SCALE = 0.0625F;

    public static AccessoryAngles accessoryAngles(LivingEntity entity, float partialTick) {
        float headYaw = Mth.lerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float bodyYaw = Mth.lerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float yaw = headYaw - bodyYaw;
        float wrappedYaw = Mth.wrapDegrees(yaw);
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        return new AccessoryAngles(headYaw, bodyYaw, yaw, wrappedYaw, pitch);
    }

    public static boolean shouldSneakModel(LivingEntity entity) {
        return entity != null && entity.isShiftKeyDown();
    }

    public static boolean validWingMode(int mode) {
        return mode >= 0 && mode < 10;
    }

    public record AccessoryAngles(float headYaw, float bodyYaw, float yaw, float wrappedYaw, float pitch) {
    }

    private LegacyAccessoryRenderHelper() {
    }
}
