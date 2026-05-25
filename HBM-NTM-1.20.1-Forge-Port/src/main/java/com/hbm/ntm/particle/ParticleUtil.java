package com.hbm.ntm.particle;

import com.hbm.ntm.network.ModMessages;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class ParticleUtil {
    public static final String TYPE_GAS_FLAME = "gasfire";
    public static final String TYPE_DEBUG_LINE = "debugline";
    public static final String TYPE_DEBUG_DRONE = "debugdrone";
    public static final String TYPE_EXPLOSION_LARGE = "explosionLarge";
    public static final String TYPE_EXPLOSION_SMALL = "explosionSmall";
    public static final String TYPE_BLACK_POWDER = "blackPowder";
    public static final String TYPE_ASHES = "ashes";
    public static final String TYPE_CASING = "casingNT";
    public static final String TYPE_SKELETON = "skeleton";

    public static void spawnGasFlame(Level level, double x, double y, double z, double motionX, double motionY, double motionZ) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_GAS_FLAME);
        data.putDouble("mX", motionX);
        data.putDouble("mY", motionY);
        data.putDouble("mZ", motionZ);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnDroneLine(Level level, double x, double y, double z, double lineX, double lineY, double lineZ, int color) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_DEBUG_DRONE);
        data.putDouble("mX", lineX);
        data.putDouble("mY", lineY);
        data.putDouble("mZ", lineZ);
        data.putInt("color", color);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnDebugLine(Level level, double x, double y, double z, double lineX, double lineY, double lineZ, int color) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_DEBUG_LINE);
        data.putDouble("mX", lineX);
        data.putDouble("mY", lineY);
        data.putDouble("mZ", lineZ);
        data.putInt("color", color);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnExplosionLarge(Level level, double x, double y, double z, int cloudCount, float cloudScale, float cloudSpeedMult,
            float waveScale, int debrisCount) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_EXPLOSION_LARGE);
        data.putInt("cloudCount", cloudCount);
        data.putFloat("cloudScale", cloudScale);
        data.putFloat("cloudSpeedMult", cloudSpeedMult);
        data.putFloat("waveScale", waveScale);
        data.putInt("debrisCount", debrisCount);
        spawnAux(level, x, y, z, data, Math.max(300.0D, waveScale * 6.0D));
    }

    public static void spawnExplosionSmall(Level level, double x, double y, double z, int cloudCount, float cloudScale, float cloudSpeedMult) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_EXPLOSION_SMALL);
        data.putInt("cloudCount", cloudCount);
        data.putFloat("cloudScale", cloudScale);
        data.putFloat("cloudSpeedMult", cloudSpeedMult);
        data.putInt("debris", 15);
        spawnAux(level, x, y, z, data, 200.0D);
    }

    public static void spawnBlackPowder(Level level, double x, double y, double z, double headingX, double headingY, double headingZ,
            int cloudCount, float cloudScale, float cloudSpeedMult, int sparkCount, float sparkSpeedMult) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_BLACK_POWDER);
        data.putDouble("hX", headingX);
        data.putDouble("hY", headingY);
        data.putDouble("hZ", headingZ);
        data.putInt("cloudCount", cloudCount);
        data.putFloat("cloudScale", cloudScale);
        data.putFloat("cloudSpeedMult", cloudSpeedMult);
        data.putInt("sparkCount", sparkCount);
        data.putFloat("sparkSpeedMult", sparkSpeedMult);
        spawnAux(level, x, y, z, data, 200.0D);
    }

    public static void spawnAshes(Level level, double x, double y, double z, int entityId, int ashesCount, float ashesScale) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_ASHES);
        data.putInt("entityID", entityId);
        data.putInt("ashesCount", ashesCount);
        data.putFloat("ashesScale", ashesScale);
        spawnAux(level, x, y, z, data, 100.0D);
    }

    public static void spawnCasing(Level level, double x, double y, double z, double motionX, double motionY, double motionZ,
            boolean smoking, int smokeLife, double smokeLift) {
        spawnCasing(level, x, y, z, motionX, motionY, motionZ, 0.0F, 0.0F, 5.0F, 10.0F, "default", smoking, smokeLife, smokeLift, 30);
    }

    public static void spawnCasing(Level level, double x, double y, double z, double motionX, double motionY, double motionZ,
            float yaw, float pitch, float momentumPitch, float momentumYaw, String name, boolean smoking, int smokeLife,
            double smokeLift, int nodeLife) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_CASING);
        data.putDouble("mX", motionX);
        data.putDouble("mY", motionY);
        data.putDouble("mZ", motionZ);
        data.putFloat("yaw", yaw);
        data.putFloat("pitch", pitch);
        data.putFloat("mPitch", momentumPitch);
        data.putFloat("mYaw", momentumYaw);
        data.putString("name", name);
        data.putBoolean("smoking", smoking);
        data.putInt("smokeLife", smokeLife);
        data.putDouble("smokeLift", smokeLift);
        data.putInt("nodeLife", nodeLife);
        spawnAux(level, x, y, z, data, 50.0D);
    }

    public static void spawnSkeleton(Level level, double x, double y, double z, int entityId, float brightness, boolean gib, float force) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_SKELETON);
        data.putInt("entityID", entityId);
        data.putFloat("brightness", brightness);
        data.putBoolean("gib", gib);
        data.putFloat("force", force);
        spawnAux(level, x, y, z, data, 100.0D);
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
            ModMessages.sendAuxParticle(serverLevel, x, y, z, payload, range);
        }
    }

    public static void spawnAuxThreaded(Level level, double x, double y, double z, CompoundTag data, double range) {
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
            ModMessages.sendAuxParticleThreaded(serverLevel, x, y, z, payload, range);
        }
    }

    private ParticleUtil() {
    }
}
