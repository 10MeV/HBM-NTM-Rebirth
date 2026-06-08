package com.hbm.ntm.bullet;

public final class BulletPersistenceUtil {
    public static boolean shouldSaveProjectile(BulletConfig config) {
        return false;
    }

    public static boolean shouldDiscardAfterLoad(BulletConfig config) {
        return true;
    }

    private BulletPersistenceUtil() {
    }
}
