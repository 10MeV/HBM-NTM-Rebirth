package com.hbm.ntm.particle;

import com.hbm.ntm.network.ModMessages;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class ParticleUtil {
    public static final String TYPE_GAS_FLAME = "gasfire";
    public static final String TYPE_DEBUG_LINE = "debugline";
    public static final String TYPE_DEBUG_DRONE = "debugdrone";
    public static final String TYPE_EXPLOSION_LARGE = "explosionLarge";
    public static final String TYPE_EXPLOSION_SMALL = "explosionSmall";
    public static final String TYPE_VNT_EXPLOSION = "vntExplosion";
    public static final String TYPE_BLACK_POWDER = "blackPowder";
    public static final String TYPE_ASHES = "ashes";
    public static final String TYPE_CASING = "casingNT";
    public static final String TYPE_SKELETON = "skeleton";
    public static final String TYPE_GIBLETS = "giblets";
    public static final String TYPE_TAU = "tau";
    public static final String TYPE_HADRON = "hadron";
    public static final String TYPE_AMAT_FLASH = "amat";
    public static final String TYPE_RBMK_FLAME = "rbmkflame";
    public static final String TYPE_RBMK_STEAM = "rbmksteam";
    public static final String TYPE_RBMK_MUSH = "rbmkmush";
    public static final String TYPE_COOLING_TOWER = "tower";
    public static final String TYPE_SPLASH = "splash";
    public static final String TYPE_RIFT = "rift";
    public static final String TYPE_DEAD_LEAF = "deadleaf";
    public static final String TYPE_FLUID_FILL = "fluidfill";
    public static final String TYPE_FOUNDRY = "foundry";
    public static final String TYPE_DEBUG_TEXT = "debug";
    public static final String TYPE_NETWORK = "network";
    public static final String TYPE_FIREWORKS = "fireworks";
    public static final String TYPE_HAZE = "haze";
    public static final String TYPE_PLASMA_BLAST = "plasmablast";
    public static final String TYPE_JUST_TILT = "justTilt";
    public static final String TYPE_PROPER_JOLT = "properJolt";
    public static final String TYPE_MUKE = "muke";
    public static final String TYPE_TINY_TOT = "tinytot";
    public static final String TYPE_UFO = "ufo";
    public static final String TYPE_BALEFIRE_CLOUD = "bf";
    public static final String TYPE_JETPACK = "jetpack";
    public static final String TYPE_BNUUY = "bnuuy";
    public static final String TYPE_JETPACK_BJ = "jetpack_bj";
    public static final String TYPE_JETPACK_DNS = "jetpack_dns";
    public static final String TYPE_RADIATION = "radiation";
    public static final int GIBLET_MEAT = 0;
    public static final int GIBLET_SLIME = 1;
    public static final int GIBLET_METAL = 2;

    public static void spawnGasFlame(Level level, double x, double y, double z, double motionX, double motionY, double motionZ) {
        spawnGasFlame(level, x, y, z, motionX, motionY, motionZ, 6.5F);
    }

    public static void spawnGasFlame(Level level, double x, double y, double z, double motionX, double motionY, double motionZ, float scale) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_GAS_FLAME);
        data.putDouble("mX", motionX);
        data.putDouble("mY", motionY);
        data.putDouble("mZ", motionZ);
        if (scale > 0.0F) {
            data.putFloat("scale", scale);
        }
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

    public static void spawnDebugText(Level level, double x, double y, double z, String text, int color, float scale) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_DEBUG_TEXT);
        data.putString("text", text == null ? "" : text);
        data.putInt("color", color);
        data.putFloat("scale", scale);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnPowerNetworkDebug(Level level, double x, double y, double z, double motionX, double motionY, double motionZ) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_NETWORK);
        data.putString("mode", "power");
        data.putDouble("mX", motionX);
        data.putDouble("mY", motionY);
        data.putDouble("mZ", motionZ);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnFluidNetworkDebug(Level level, double x, double y, double z, double motionX, double motionY, double motionZ, int color) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_NETWORK);
        data.putString("mode", "fluid");
        data.putDouble("mX", motionX);
        data.putDouble("mY", motionY);
        data.putDouble("mZ", motionZ);
        data.putInt("color", color);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnFireworks(Level level, double x, double y, double z, int color, char character) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_FIREWORKS);
        data.putInt("color", color);
        data.putInt("char", character);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnHaze(Level level, double x, double y, double z) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_HAZE);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnPlasmaBlast(Level level, double x, double y, double z, float red, float green, float blue,
            float pitch, float yaw, float scale) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_PLASMA_BLAST);
        data.putFloat("r", red);
        data.putFloat("g", green);
        data.putFloat("b", blue);
        data.putFloat("pitch", pitch);
        data.putFloat("yaw", yaw);
        data.putFloat("scale", scale);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnJustTilt(Level level, int time) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_JUST_TILT);
        data.putInt("time", time);
        spawnAux(level, 0.0D, 0.0D, 0.0D, data, 0.0D);
    }

    public static void spawnProperJolt(Level level, int time, int maxTime) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_PROPER_JOLT);
        data.putInt("time", time);
        data.putInt("maxTime", maxTime);
        spawnAux(level, 0.0D, 0.0D, 0.0D, data, 0.0D);
    }

    public static void spawnMuke(Level level, double x, double y, double z, boolean balefire) {
        spawnNuclearBurstVisual(level, x, y, z, TYPE_MUKE, balefire);
    }

    public static void spawnTinyTot(Level level, double x, double y, double z) {
        spawnNuclearBurstVisual(level, x, y, z, TYPE_TINY_TOT, false);
    }

    public static void spawnNuclearBurstVisual(Level level, double x, double y, double z, String particle, boolean balefire) {
        if (particle == null || particle.isEmpty()) {
            return;
        }
        CompoundTag data = new CompoundTag();
        data.putString("type", particle);
        if (TYPE_MUKE.equals(particle) && balefire) {
            data.putBoolean("balefire", true);
        }
        spawnAux(level, x, y, z, data, 250.0D);
    }

    public static void spawnUfoCloud(Level level, double x, double y, double z, double motion) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_UFO);
        data.putDouble("motion", motion);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnBalefireCloud(Level level, double x, double y, double z) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_BALEFIRE_CLOUD);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnJetpack(Level level, Entity player, int mode) {
        if (player == null) {
            return;
        }
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_JETPACK);
        data.putInt("player", player.getId());
        data.putInt("mode", mode);
        spawnAux(level, player.getX(), player.getY(), player.getZ(), data, 150.0D);
    }

    public static void spawnBnuuy(Level level, Entity player) {
        spawnPlayerBackpackEffect(level, player, TYPE_BNUUY);
    }

    public static void spawnJetpackBj(Level level, Entity player) {
        spawnPlayerBackpackEffect(level, player, TYPE_JETPACK_BJ);
    }

    public static void spawnJetpackDns(Level level, Entity player) {
        spawnPlayerBackpackEffect(level, player, TYPE_JETPACK_DNS);
    }

    public static void spawnRadiationAura(Level level, int count) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_RADIATION);
        data.putInt("count", count);
        spawnAux(level, 0.0D, 0.0D, 0.0D, data, 0.0D);
    }

    private static void spawnPlayerBackpackEffect(Level level, Entity player, String type) {
        if (player == null) {
            return;
        }
        CompoundTag data = new CompoundTag();
        data.putString("type", type);
        data.putInt("player", player.getId());
        spawnAux(level, player.getX(), player.getY(), player.getZ(), data, 150.0D);
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

    public static void spawnVntExplosion(Level level, double x, double y, double z, float size, long[] affectedBlocks) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_VNT_EXPLOSION);
        data.putFloat("size", size);
        data.putLongArray("blocks", affectedBlocks);
        spawnAux(level, x, y, z, data, 250.0D);
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

    public static void spawnGiblets(Entity entity, int gibType) {
        spawnGiblets(entity, gibType, 0);
    }

    public static void spawnGiblets(Entity entity, int gibType, int countDivisor) {
        if (entity == null) {
            return;
        }
        spawnGiblets(entity.level(), entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(), entity.getId(), gibType, countDivisor);
    }

    public static void spawnGiblets(Level level, double x, double y, double z, int entityId, int gibType, int countDivisor) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_GIBLETS);
        data.putInt("ent", entityId);
        data.putInt("gibType", gibType);
        if (countDivisor > 0) {
            data.putInt("cDiv", countDivisor);
        }
        spawnAuxThreaded(level, x, y, z, data, 150.0D);
    }

    public static void spawnTau(Level level, double x, double y, double z, int count, boolean small) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_TAU);
        data.putByte("count", (byte) Math.max(0, Math.min(127, count)));
        data.putBoolean("small", small);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnHadron(Level level, double x, double y, double z) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_HADRON);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnAmatFlash(Level level, double x, double y, double z, float scale) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_AMAT_FLASH);
        data.putFloat("scale", scale);
        spawnAux(level, x, y, z, data, 200.0D);
    }

    public static void spawnRbmkFlame(Level level, double x, double y, double z, int maxAge) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_RBMK_FLAME);
        data.putInt("maxAge", maxAge);
        spawnAux(level, x, y, z, data, 100.0D);
    }

    public static void spawnRbmkSteam(Level level, double x, double y, double z) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_RBMK_STEAM);
        spawnAux(level, x, y, z, data, 100.0D);
    }

    public static void spawnRbmkMush(Level level, double x, double y, double z, float scale) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_RBMK_MUSH);
        data.putFloat("scale", scale);
        spawnAux(level, x, y, z, data, 250.0D);
    }

    public static void spawnCoolingTower(Level level, double x, double y, double z, float lift, float baseScale, float maxScale, int lifetime) {
        spawnCoolingTower(level, x, y, z, lift, baseScale, maxScale, lifetime, false, 0.075F, 0.25F, -1);
    }

    public static void spawnCoolingTower(Level level, double x, double y, double z, float lift, float baseScale, float maxScale, int lifetime,
            boolean noWind, float strafe, float alpha, int color) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_COOLING_TOWER);
        data.putFloat("lift", lift);
        data.putFloat("base", baseScale);
        data.putFloat("max", maxScale);
        data.putInt("life", lifetime);
        data.putFloat("strafe", strafe);
        data.putFloat("alpha", alpha);
        if (noWind) {
            data.putBoolean("noWind", true);
        }
        if (color >= 0) {
            data.putInt("color", color);
        }
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnSplash(Level level, double x, double y, double z, int color) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_SPLASH);
        if (color >= 0) {
            data.putInt("color", color);
        }
        spawnAux(level, x, y, z, data, 100.0D);
    }

    public static void spawnRift(Level level, double x, double y, double z) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_RIFT);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnDeadLeaf(Level level, double x, double y, double z) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_DEAD_LEAF);
        spawnAux(level, x, y, z, data, 64.0D);
    }

    public static void spawnFluidFill(Level level, double x, double y, double z, double motionX, double motionY, double motionZ, int color) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_FLUID_FILL);
        data.putDouble("mX", motionX);
        data.putDouble("mY", motionY);
        data.putDouble("mZ", motionZ);
        if (color >= 0) {
            data.putInt("color", color);
        }
        spawnAux(level, x, y, z, data, 100.0D);
    }

    public static void spawnFoundry(Level level, double x, double y, double z, int color, int direction, float length, float base, float offset) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_FOUNDRY);
        data.putInt("color", color);
        data.putByte("dir", (byte) direction);
        data.putFloat("len", length);
        data.putFloat("base", base);
        data.putFloat("off", offset);
        spawnAux(level, x, y, z, data, 96.0D);
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
