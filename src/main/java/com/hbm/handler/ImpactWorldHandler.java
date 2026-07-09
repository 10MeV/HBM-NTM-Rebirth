package com.hbm.handler;

import com.hbm.ntm.world.TomImpactWorldEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Legacy impact-world facade. Runtime state lives in TomImpactSavedData and
 * ClientTomImpactData; this class intentionally does not keep a second cache.
 */
@Deprecated(forRemoval = false)
public final class ImpactWorldHandler {
    public static void impactEffects(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            TomImpactWorldEffects.impactEffects(serverLevel);
        }
    }

    public static void die(Level level, int x, int y, int z) {
        if (level instanceof ServerLevel serverLevel) {
            TomImpactWorldEffects.die(serverLevel, x, y, z);
        }
    }

    public static void burn(Level level, int x, int y, int z) {
        if (level instanceof ServerLevel serverLevel) {
            TomImpactWorldEffects.burn(serverLevel, x, y, z);
        }
    }

    public static float getFireForClient(Level level) {
        return ClientAccess.getFireForClient(level);
    }

    public static float getDustForClient(Level level) {
        return ClientAccess.getDustForClient(level);
    }

    public static boolean getImpactForClient(Level level) {
        return ClientAccess.getImpactForClient(level);
    }

    private ImpactWorldHandler() {
    }

    private static final class ClientAccess {
        private static float getFireForClient(Level level) {
            return com.hbm.ntm.client.ClientTomImpactData.getFireForClient(level);
        }

        private static float getDustForClient(Level level) {
            return com.hbm.ntm.client.ClientTomImpactData.getDustForClient(level);
        }

        private static boolean getImpactForClient(Level level) {
            return com.hbm.ntm.client.ClientTomImpactData.getImpactForClient(level);
        }
    }
}
