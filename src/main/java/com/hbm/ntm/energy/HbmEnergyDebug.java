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

    public static void spawnRemoteProviderSubscription(Level level, BlockPos conductorPos, Direction portDirection, boolean connected) {
        spawnRemote(level, conductorPos, portDirection, connected, true);
    }

    public static void spawnRemoteReceiverSubscription(Level level, BlockPos conductorPos, Direction portDirection, boolean connected) {
        spawnRemote(level, conductorPos, portDirection, connected, false);
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

        spawnLegacyPowerPacket(serverLevel, origin.relative(side), side, connected, provider);
    }

    private static void spawnRemote(Level level, BlockPos conductorPos, Direction portDirection, boolean connected, boolean provider) {
        if (!particleDebug || !(level instanceof ServerLevel serverLevel) || conductorPos == null || portDirection == null) {
            return;
        }

        spawnLegacyPowerPacket(serverLevel, conductorPos, portDirection, connected, provider);
    }

    private static void spawnLegacyPowerPacket(ServerLevel level, BlockPos conductorPos, Direction directionFromMachine,
            boolean connected, boolean provider) {
        double positionSign = provider ? -1.0D : 1.0D;
        double motionSign = provider ? 1.0D : -1.0D;
        double speed = connected ? 0.025D : 0.1D;
        double x = conductorPos.getX() + 0.5D + directionFromMachine.getStepX() * positionSign * 0.5D
                + level.random.nextDouble() * 0.5D - 0.25D;
        double y = conductorPos.getY() + 0.5D + directionFromMachine.getStepY() * positionSign * 0.5D
                + level.random.nextDouble() * 0.5D - 0.25D;
        double z = conductorPos.getZ() + 0.5D + directionFromMachine.getStepZ() * positionSign * 0.5D
                + level.random.nextDouble() * 0.5D - 0.25D;
        double dx = directionFromMachine.getStepX() * motionSign * speed;
        double dy = directionFromMachine.getStepY() * motionSign * speed;
        double dz = directionFromMachine.getStepZ() * motionSign * speed;

        CompoundTag data = new CompoundTag();
        data.putString("type", ParticleUtil.TYPE_NETWORK);
        data.putString("mode", "power");
        data.putDouble("mX", dx);
        data.putDouble("mY", dy);
        data.putDouble("mZ", dz);
        ParticleUtil.spawnAux(level, x, y, z, data, 25.0D);
    }
}
