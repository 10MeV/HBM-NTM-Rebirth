package com.hbm.ntm.particle;

import com.hbm.ntm.network.ModMessages;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;

public final class ParticleUtil {
    public static final String TYPE_GAS_FLAME = "gasfire";
    public static final String TYPE_DEBUG_LINE = "debugline";
    public static final String TYPE_DEBUG_DRONE = "debugdrone";
    public static final String TYPE_WATER_SPLASH = "waterSplash";
    public static final String TYPE_CLOUD_FX_2 = "cloudFX2";
    public static final String TYPE_SMOKE = "smoke";
    public static final String TYPE_VANILLA_EXT = "vanillaExt";
    public static final String TYPE_VANILLA_BURST = "vanillaburst";
    public static final String TYPE_LAUNCH_SMOKE = "launchSmoke";
    public static final String TYPE_MISSILE_CONTRAIL = "missileContrail";
    public static final String TYPE_ABM_CONTRAIL = "ABMContrail";
    public static final String TYPE_EX_KEROSENE = "exKerosene";
    public static final String TYPE_EX_SOLID = "exSolid";
    public static final String TYPE_EX_HYDROGEN = "exHydrogen";
    public static final String TYPE_EX_BALEFIRE = "exBalefire";
    public static final String TYPE_EXHAUST = "exhaust";
    public static final String TYPE_EXPLOSION_LARGE = "explosionLarge";
    public static final String TYPE_EXPLOSION_SMALL = "explosionSmall";
    public static final String TYPE_VNT_EXPLOSION = "vntExplosion";
    public static final String TYPE_BLACK_POWDER = "blackPowder";
    public static final String TYPE_ASHES = "ashes";
    public static final String TYPE_CASING = "casingNT";
    public static final String TYPE_LEGACY_CASING = "casing";
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
    public static final String TYPE_RADIATION_FOG = "radiationfog";
    public static final String TYPE_RADIATION_FOG_SNAKE = "radiation_fog";
    public static final String TYPE_RAD_FOG = "radFog";
    public static final String TYPE_SCHRAB_FOG = "schrabfog";
    public static final String TYPE_SWEAT = "sweat";
    public static final String TYPE_VOMIT = "vomit";
    public static final String TYPE_VANISH = "vanish";
    public static final String TYPE_MARKER = "marker";
    public static final String TYPE_FROZEN = "frozen";
    public static final String VOMIT_NORMAL = "normal";
    public static final String VOMIT_BLOOD = "blood";
    public static final String VOMIT_SMOKE = "smoke";
    public static final String SMOKE_CLOUD = "cloud";
    public static final String SMOKE_RADIAL = "radial";
    public static final String SMOKE_RADIAL_DIGAMMA = "radialDigamma";
    public static final String SMOKE_SHOCK = "shock";
    public static final String SMOKE_SHOCK_RANDOM = "shockRand";
    public static final String SMOKE_WAVE = "wave";
    public static final String SMOKE_FOAM_SPLASH = "foamSplash";
    public static final String VANILLA_FLAME = "flame";
    public static final String VANILLA_SMOKE = "smoke";
    public static final String VANILLA_CLOUD = "cloud";
    public static final String VANILLA_RED_DUST = "reddust";
    public static final String VANILLA_BLUE_DUST = "bluedust";
    public static final String VANILLA_GREEN_DUST = "greendust";
    public static final String VANILLA_BLOCK_DUST = "blockdust";
    public static final String VANILLA_COLOR_DUST = "colordust";
    public static final String VANILLA_FIREWORKS = "fireworks";
    public static final String VANILLA_LARGE_EXPLODE = "largeexplode";
    public static final String VANILLA_TOWN_AURA = "townaura";
    public static final String VANILLA_VOLCANO = "volcano";
    public static final String EXHAUST_SOYUZ = "soyuz";
    public static final String EXHAUST_METEOR = "meteor";
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
        spawnAux(level, x, y, z, data, 300.0D);
    }

    public static void spawnExhaustSoyuz(Level level, double x, double y, double z, int count, double width) {
        spawnExhaust(level, x, y, z, EXHAUST_SOYUZ, count, width);
    }

    public static void spawnExhaustMeteor(Level level, double x, double y, double z, int count, double width) {
        spawnExhaust(level, x, y, z, EXHAUST_METEOR, count, width);
    }

    public static void spawnExhaust(Level level, double x, double y, double z, String mode, int count, double width) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_EXHAUST);
        data.putString("mode", mode == null || mode.isEmpty() ? EXHAUST_SOYUZ : mode);
        data.putInt("count", count);
        data.putDouble("width", width);
        spawnAux(level, x, y, z, data, 350.0D);
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

    public static void spawnWaterSplash(Level level, double x, double y, double z) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_WATER_SPLASH);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnCloudFx2(Level level, double x, double y, double z) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_CLOUD_FX_2);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnLaunchSmoke(Level level, double x, double y, double z, double motionX, double motionY, double motionZ) {
        CompoundTag data = motionParticleTag(TYPE_LAUNCH_SMOKE, motionX, motionY, motionZ);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnMissileContrail(Level level, double x, double y, double z, double motionX, double motionY, double motionZ) {
        spawnMissileContrail(level, x, y, z, motionX, motionY, motionZ, 1.0F, -1);
    }

    public static void spawnMissileContrail(Level level, double x, double y, double z, double motionX, double motionY, double motionZ,
            float scale, int maxAge) {
        CompoundTag data = motionParticleTag(TYPE_MISSILE_CONTRAIL, motionX, motionY, motionZ);
        if (scale > 0.0F) {
            data.putFloat("scale", scale);
        }
        if (maxAge > 0) {
            data.putInt("maxAge", maxAge);
        }
        spawnAux(level, x, y, z, data, 350.0D);
    }

    public static void spawnAbmContrail(Level level, double x, double y, double z) {
        spawnStaticContrail(level, x, y, z, TYPE_ABM_CONTRAIL);
    }

    public static void spawnExKerosene(Level level, double x, double y, double z) {
        spawnStaticContrail(level, x, y, z, TYPE_EX_KEROSENE);
    }

    public static void spawnExSolid(Level level, double x, double y, double z) {
        spawnStaticContrail(level, x, y, z, TYPE_EX_SOLID);
    }

    public static void spawnExHydrogen(Level level, double x, double y, double z) {
        spawnStaticContrail(level, x, y, z, TYPE_EX_HYDROGEN);
    }

    public static void spawnExBalefire(Level level, double x, double y, double z) {
        spawnStaticContrail(level, x, y, z, TYPE_EX_BALEFIRE);
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

    public static void spawnRadiationFog(Level level, double x, double y, double z) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_RAD_FOG);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnSchrabFog(Level level, double x, double y, double z) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_SCHRAB_FOG);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnVanish(Level level, Entity entity) {
        if (entity == null) {
            return;
        }
        spawnVanish(level, entity.getX(), entity.getY(), entity.getZ(), entity.getId());
    }

    public static void spawnVanish(Level level, double x, double y, double z, int entityId) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_VANISH);
        data.putInt("ent", entityId);
        spawnAuxThreaded(level, x, y, z, data, 150.0D);
    }

    public static void spawnMarker(Level level, double x, double y, double z, int color, int expiresMillis, double maxDistance, String label) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_MARKER);
        data.putInt("color", color);
        data.putInt("expires", Math.max(0, expiresMillis));
        data.putDouble("dist", maxDistance);
        if (label != null && !label.isEmpty()) {
            data.putString("label", label);
        }
        spawnAuxThreaded(level, x, y, z, data, Math.max(0.0D, maxDistance));
    }

    public static void spawnFrozen(Level level, Entity player) {
        if (player == null) {
            return;
        }
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_FROZEN);
        spawnAux(level, player.getX(), player.getY(), player.getZ(), data, 0.0D);
    }

    public static void spawnSweat(Entity entity, Block block, int count) {
        spawnSweat(entity, block, 0, count);
    }

    public static void spawnSweat(Entity entity, Block block, int meta, int count) {
        if (block == null) {
            return;
        }
        spawnSweat(entity, block.defaultBlockState(), meta, count);
    }

    public static void spawnSweat(Entity entity, BlockState state, int count) {
        spawnSweat(entity, state, 0, count);
    }

    private static void spawnSweat(Entity entity, BlockState state, int meta, int count) {
        if (entity == null) {
            return;
        }
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_SWEAT);
        data.putInt("count", Math.max(0, count));
        putBlockState(data, state);
        data.putInt("meta", meta);
        data.putInt("entity", entity.getId());
        spawnAuxThreaded(entity.level(), entity.getX(), entity.getY(), entity.getZ(), data, 25.0D);
    }

    public static void spawnSweat(Entity entity, String legacyBlockName, int meta, int count) {
        if (entity == null || legacyBlockName == null || legacyBlockName.isBlank()) {
            return;
        }
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_SWEAT);
        data.putInt("count", Math.max(0, count));
        putLegacyBlockName(data, legacyBlockName, meta);
        data.putInt("entity", entity.getId());
        spawnAuxThreaded(entity.level(), entity.getX(), entity.getY(), entity.getZ(), data, 25.0D);
    }

    public static void spawnVomit(Entity entity, String mode, int count) {
        if (entity == null) {
            return;
        }
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_VOMIT);
        data.putString("mode", mode == null || mode.isEmpty() ? VOMIT_NORMAL : mode);
        data.putInt("count", Math.max(0, count));
        data.putInt("entity", entity.getId());
        spawnAuxThreaded(entity.level(), entity.getX(), entity.getY(), entity.getZ(), data, 25.0D);
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

    public static void spawnSmoke(Level level, double x, double y, double z, String mode, int count) {
        CompoundTag data = smokeTag(mode, count);
        spawnAux(level, x, y, z, data, 250.0D);
    }

    public static void spawnSmokeShock(Level level, double x, double y, double z, int count, double strength, boolean randomStrength) {
        CompoundTag data = smokeTag(randomStrength ? SMOKE_SHOCK_RANDOM : SMOKE_SHOCK, count);
        data.putDouble("strength", strength);
        spawnAux(level, x, y, z, data, 250.0D);
    }

    public static void spawnSmokeRing(Level level, double x, double y, double z, String mode, int count, double range) {
        CompoundTag data = smokeTag(mode, count);
        data.putDouble("range", range);
        spawnAux(level, x, y, z, data, 250.0D);
    }

    public static void spawnVanillaBurst(Level level, double x, double y, double z, String mode, int count, double motion) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_VANILLA_BURST);
        data.putString("mode", mode);
        data.putInt("count", count);
        data.putDouble("motion", motion);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnVanillaBlockDustBurst(Level level, double x, double y, double z, int count, double motion, Block block) {
        if (block == null) {
            return;
        }
        spawnVanillaBlockDustBurst(level, x, y, z, count, motion, block.defaultBlockState());
    }

    public static void spawnVanillaBlockDustBurst(Level level, double x, double y, double z, int count, double motion, BlockState state) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_VANILLA_BURST);
        data.putString("mode", VANILLA_BLOCK_DUST);
        data.putInt("count", count);
        data.putDouble("motion", motion);
        putBlockState(data, state);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnVanillaLegacyBlockDustBurst(Level level, double x, double y, double z, int count, double motion,
            String legacyBlockName, int meta) {
        if (legacyBlockName == null || legacyBlockName.isBlank()) {
            return;
        }
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_VANILLA_BURST);
        data.putString("mode", VANILLA_BLOCK_DUST);
        data.putInt("count", count);
        data.putDouble("motion", motion);
        putLegacyBlockName(data, legacyBlockName, meta);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnVanillaExt(Level level, double x, double y, double z, String mode, double motionX, double motionY, double motionZ) {
        CompoundTag data = vanillaExtTag(mode, motionX, motionY, motionZ);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnVanillaExt(Level level, double x, double y, double z, String mode,
            double motionX, double motionY, double motionZ, int overrideAge, boolean noClip) {
        CompoundTag data = vanillaExtTag(mode, motionX, motionY, motionZ);
        if (overrideAge > 0) {
            data.putInt("overrideAge", overrideAge);
        }
        if (noClip) {
            data.putBoolean("noclip", true);
        }
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnVanillaExtLargeExplode(Level level, double x, double y, double z, float size, int count) {
        CompoundTag data = vanillaExtTag(VANILLA_LARGE_EXPLODE, 0.0D, 0.0D, 0.0D);
        data.putFloat("size", size);
        data.putByte("count", (byte) Math.max(0, Math.min(127, count)));
        spawnAux(level, x, y, z, data, 100.0D);
    }

    public static void spawnVanillaExtColoredCloud(Level level, double x, double y, double z, float red, float green, float blue) {
        CompoundTag data = vanillaExtTag(VANILLA_CLOUD, 0.0D, 0.0D, 0.0D);
        data.putFloat("r", red);
        data.putFloat("g", green);
        data.putFloat("b", blue);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnVanillaExtBlockDust(Level level, double x, double y, double z,
            double motionX, double motionY, double motionZ, Block block) {
        if (block == null) {
            return;
        }
        spawnVanillaExtBlockDust(level, x, y, z, motionX, motionY, motionZ, block.defaultBlockState());
    }

    public static void spawnVanillaExtBlockDust(Level level, double x, double y, double z,
            double motionX, double motionY, double motionZ, BlockState state) {
        CompoundTag data = vanillaExtTag(VANILLA_BLOCK_DUST, motionX, motionY, motionZ);
        putBlockState(data, state);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnVanillaExtLegacyBlockDust(Level level, double x, double y, double z,
            double motionX, double motionY, double motionZ, String legacyBlockName, int meta) {
        if (legacyBlockName == null || legacyBlockName.isBlank()) {
            return;
        }
        CompoundTag data = vanillaExtTag(VANILLA_BLOCK_DUST, motionX, motionY, motionZ);
        putLegacyBlockName(data, legacyBlockName, meta);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnVanillaExtColorDust(Level level, double x, double y, double z,
            double motionX, double motionY, double motionZ, float red, float green, float blue) {
        CompoundTag data = vanillaExtTag(VANILLA_COLOR_DUST, motionX, motionY, motionZ);
        data.putFloat("r", red);
        data.putFloat("g", green);
        data.putFloat("b", blue);
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

    public static void spawnLegacyCasing(Level level, double x, double y, double z, int ejectorId, String name,
            float pitchRadians, float yawRadians, boolean crouched) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_LEGACY_CASING);
        data.putInt("ej", ejectorId);
        data.putString("name", name);
        data.putFloat("pitch", pitchRadians);
        data.putFloat("yaw", yawRadians);
        data.putBoolean("crouched", crouched);
        spawnAuxThreaded(level, x, y, z, data, 50.0D);
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

    private static CompoundTag smokeTag(String mode, int count) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_SMOKE);
        data.putString("mode", mode);
        data.putInt("count", count);
        return data;
    }

    private static CompoundTag vanillaExtTag(String mode, double motionX, double motionY, double motionZ) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_VANILLA_EXT);
        data.putString("mode", mode);
        data.putDouble("mX", motionX);
        data.putDouble("mY", motionY);
        data.putDouble("mZ", motionZ);
        return data;
    }

    private static CompoundTag motionParticleTag(String type, double motionX, double motionY, double motionZ) {
        CompoundTag data = new CompoundTag();
        data.putString("type", type);
        data.putDouble("moX", motionX);
        data.putDouble("moY", motionY);
        data.putDouble("moZ", motionZ);
        return data;
    }

    private static void spawnStaticContrail(Level level, double x, double y, double z, String type) {
        CompoundTag data = new CompoundTag();
        data.putString("type", type);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void putBlockState(CompoundTag data, BlockState state) {
        LegacyBlockStateMappings.putState(data, state);
    }

    public static void putLegacyBlockName(CompoundTag data, String legacyBlockName, int meta) {
        LegacyBlockStateMappings.putLegacyName(data, legacyBlockName, meta);
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
