package com.hbm.ntm.particle;

import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.AuxParticlePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class ParticleUtil {
    public static final String TYPE_GAS_FLAME = "gasfire";
    public static final String TYPE_DEBUG_DRONE = "debugdrone";

    public static void spawnGasFlame(Level level, double x, double y, double z, double motionX, double motionY, double motionZ) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_GAS_FLAME);
        data.putDouble("mX", motionX);
        data.putDouble("mY", motionY);
        data.putDouble("mZ", motionZ);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnDroneLine(Level level, double x, double y, double z, double targetX, double targetY, double targetZ, int color) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_DEBUG_DRONE);
        data.putDouble("mX", targetX);
        data.putDouble("mY", targetY);
        data.putDouble("mZ", targetZ);
        data.putInt("color", color);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnAux(Level level, double x, double y, double z, CompoundTag data, double range) {
        if (level == null) {
            return;
        }
        CompoundTag payload = data.copy();
        payload.putDouble("posX", x);
        payload.putDouble("posY", y);
        payload.putDouble("posZ", z);
        if (level.isClientSide()) {
            ClientParticleBridge.handleAux(payload);
        } else if (level instanceof ServerLevel serverLevel) {
            ModMessages.sendToTracking(new AuxParticlePacket(payload), serverLevel, x, y, z, range);
        }
    }

    private ParticleUtil() {
    }
}
