package com.hbm.ntm.bullet;

import net.minecraft.world.entity.Entity;

public final class BulletHomingStateUtil {
    public static final String LEGACY_TARGET_TAG = "homingTarget";
    public static final int NO_TARGET_ID = 0;

    public static boolean shouldResetTargetAfterEntityHurt(BulletConfig config) {
        return config != null && config.hasBehavior(BulletBehaviorTag.PENETRATION_HOMING_RESET);
    }

    public static int targetId(Entity target) {
        return target == null ? NO_TARGET_ID : target.getId();
    }

    private BulletHomingStateUtil() {
    }
}
