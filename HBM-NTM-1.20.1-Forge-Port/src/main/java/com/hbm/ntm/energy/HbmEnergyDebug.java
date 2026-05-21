package com.hbm.ntm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class HbmEnergyDebug {
    private static volatile boolean particleDebug;

    private HbmEnergyDebug() {
    }

    public static boolean isParticleDebugEnabled() {
        return particleDebug;
    }

    public static boolean setParticleDebugEnabled(boolean enabled) {
        particleDebug = enabled;
        return particleDebug;
    }

    public static boolean toggleParticleDebug() {
        particleDebug = !particleDebug;
        return particleDebug;
    }

    public static void spawnProviderSubscription(Level level, BlockPos providerPos, Direction side, boolean connected) {
        spawn(level, providerPos, side, connected, true);
    }

    public static void spawnReceiverSubscription(Level level, BlockPos receiverPos, Direction side, boolean connected) {
        spawn(level, receiverPos, side, connected, false);
    }

    public static void spawnDirectTransfer(Level level, BlockPos providerPos, Direction side, long transferred) {
        if (transferred > 0L) {
            spawn(level, providerPos, side, true, true);
        }
    }

    private static void spawn(Level level, BlockPos origin, Direction side, boolean connected, boolean provider) {
        if (!particleDebug || !(level instanceof ServerLevel serverLevel) || origin == null || side == null) {
            return;
        }

        double sign = provider ? 1.0D : -1.0D;
        double x = origin.getX() + 0.5D + side.getStepX() * 0.5D + (serverLevel.random.nextDouble() - 0.5D) * 0.3D;
        double y = origin.getY() + 0.5D + side.getStepY() * 0.5D + (serverLevel.random.nextDouble() - 0.5D) * 0.3D;
        double z = origin.getZ() + 0.5D + side.getStepZ() * 0.5D + (serverLevel.random.nextDouble() - 0.5D) * 0.3D;
        double dx = side.getStepX() * sign * 0.05D;
        double dy = side.getStepY() * sign * 0.05D;
        double dz = side.getStepZ() * sign * 0.05D;

        serverLevel.sendParticles(
                connected ? ParticleTypes.END_ROD : ParticleTypes.SMOKE,
                x, y, z,
                1,
                dx, dy, dz,
                connected ? 0.02D : 0.08D);
    }
}
