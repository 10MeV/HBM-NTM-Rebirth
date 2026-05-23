package com.hbm.ntm.energy;

import com.hbm.ntm.particle.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

        CompoundTag data = new CompoundTag();
        data.putString("type", ParticleUtil.TYPE_DEBUG_DRONE);
        data.putDouble("mX", dx * 8.0D);
        data.putDouble("mY", dy * 8.0D);
        data.putDouble("mZ", dz * 8.0D);
        data.putString("role", provider ? "provider" : "receiver");
        data.putBoolean("connected", connected);
        data.putString("side", side.getName());
        data.putInt("color", connected ? (provider ? 0x66FF66 : 0x66AAFF) : 0x808080);
        ParticleUtil.spawnAux(serverLevel, x, y, z, data, 25.0D);
    }
}
