package com.hbm.ntm.particle;

import com.hbm.ntm.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ParticleUtil {
    public static final String TYPE_GAS_FLAME = "gasfire";
    public static final String TYPE_DEBUG_LINE = "debugline";
    public static final String TYPE_DEBUG_DRONE = "debugdrone";
    public static final String TYPE_WATER_SPLASH = "waterSplash";
    public static final String TYPE_CLOUD_FX_2 = "cloudFX2";
    public static final String TYPE_SMOKE = "smoke";
    public static final String TYPE_VANILLA = "vanilla";
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
    public static final String TYPE_FLAMETHROWER = "flamethrower";
    public static final String TYPE_EXPLOSION_LARGE = "explosionLarge";
    public static final String TYPE_EXPLOSION_SMALL = "explosionSmall";
    public static final String TYPE_WEAPON_EXPLOSION = "weaponExplosion";
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
    public static final String TYPE_CHAOS_CLOUD = "chaosCloud";
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
    public static final String VANILLA_EXPLODE = "explode";
    public static final String VANILLA_LARGE_EXPLODE = "largeexplode";
    public static final String VANILLA_HUGE_EXPLOSION = "hugeexplosion";
    public static final String VANILLA_TOWN_AURA = "townaura";
    public static final String VANILLA_VOLCANO = "volcano";
    public static final String EXHAUST_SOYUZ = "soyuz";
    public static final String CHAOS_CLOUD_ORANGE = "orange";
    public static final String CHAOS_CLOUD_GREEN = "green";
    public static final String CHAOS_CLOUD_PINK = "pink";
    public static final int FLAMETHROWER_META_FIRE = 0;
    public static final int FLAMETHROWER_META_BALEFIRE = 1;
    public static final int FLAMETHROWER_META_DIGAMMA = 2;
    public static final int FLAMETHROWER_META_OXY = 3;
    public static final int FLAMETHROWER_META_BLACK = 4;
    public static final int GIBLET_MEAT = 0;
    public static final int GIBLET_SLIME = 1;
    public static final int GIBLET_METAL = 2;

    public static void spawnGasFlame(Level level, double x, double y, double z, double motionX, double motionY, double motionZ) {
        spawnGasFlame(level, x, y, z, motionX, motionY, motionZ, 6.5F);
    }

    public static void spawnGasFlame(Level level, double x, double y, double z, double motionX, double motionY, double motionZ, float scale) {
        spawnGasFlame(level, x, y, z, motionX, motionY, motionZ, scale, 150.0D);
    }

    public static void spawnGasFlame(Level level, Vec3 position, Vec3 motion) {
        if (position == null || motion == null) {
            return;
        }
        spawnGasFlame(level, position.x, position.y, position.z, motion.x, motion.y, motion.z);
    }

    public static void spawnGasFlame(Level level, Vec3 position, Vec3 motion, float scale) {
        if (position == null || motion == null) {
            return;
        }
        spawnGasFlame(level, position.x, position.y, position.z, motion.x, motion.y, motion.z, scale);
    }

    public static void spawnGeysirGasFlame(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        spawnGasFlame(level, pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D,
                level.random.nextGaussian() * 0.05D, 0.2D, level.random.nextGaussian() * 0.05D,
                6.5F, 75.0D);
    }

    public static void spawnGasFlareBurnFlame(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        spawnGasFlame(level, pos.getX() + 0.5D, pos.getY() + 11.75D, pos.getZ() + 0.5D,
                level.random.nextGaussian() * 0.15D, 0.2D, level.random.nextGaussian() * 0.15D);
    }

    public static void spawnTurbofanAfterburnerFlame(Level level, BlockPos pos, Direction exhaustDirection, int nozzleIndex) {
        if (level == null || pos == null) {
            return;
        }
        Direction dir = horizontalOrNorth(exhaustDirection);
        int nozzle = Math.max(0, Math.min(1, nozzleIndex));
        double speed = 2.0D + level.random.nextDouble() * 3.0D;
        double deviation = level.random.nextGaussian() * 0.2D;
        spawnGasFlame(level,
                pos.getX() + 0.5D - dir.getStepX() * (3.0D - nozzle),
                pos.getY() + 1.5D,
                pos.getZ() + 0.5D - dir.getStepZ() * (3.0D - nozzle),
                -dir.getStepX() * speed + deviation,
                0.0D,
                -dir.getStepZ() * speed + deviation,
                8.0F,
                150.0D);
    }

    public static void spawnTurbofanDamageGasFlame(Level level, BlockPos pos, Direction exhaustDirection) {
        if (level == null || pos == null) {
            return;
        }
        Direction dir = horizontalOrNorth(exhaustDirection);
        Direction rot = dir.getClockWise();
        spawnGasFlame(level,
                pos.getX() + 0.5D + dir.getStepX() * (level.random.nextDouble() * 4.0D - 2.0D)
                        + rot.getStepX() * (level.random.nextDouble() * 2.0D - 1.0D),
                pos.getY() + 1.0D + level.random.nextDouble() * 2.0D,
                pos.getZ() + 0.5D - dir.getStepZ() * (level.random.nextDouble() * 4.0D - 2.0D)
                        + rot.getStepZ() * (level.random.nextDouble() * 2.0D - 1.0D),
                0.0D,
                0.1D * level.random.nextDouble(),
                0.0D,
                4.0F,
                150.0D);
    }

    private static void spawnGasFlame(Level level, double x, double y, double z, double motionX, double motionY, double motionZ,
            float scale, double range) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_GAS_FLAME);
        data.putDouble("mX", motionX);
        data.putDouble("mY", motionY);
        data.putDouble("mZ", motionZ);
        if (scale > 0.0F) {
            data.putFloat("scale", scale);
        }
        spawnAux(level, x, y, z, data, range);
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

    public static void spawnExhaust(Level level, double x, double y, double z, String mode, int count, double width) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_EXHAUST);
        data.putString("mode", mode == null || mode.isEmpty() ? EXHAUST_SOYUZ : mode);
        data.putInt("count", count);
        data.putDouble("width", width);
        spawnAux(level, x, y, z, data, 350.0D);
    }

    public static void spawnFlamethrower(Level level, double x, double y, double z, int meta) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_FLAMETHROWER);
        data.putInt("meta", meta);
        spawnAux(level, x, y, z, data, 50.0D);
    }

    public static void spawnLegacyFlameEffect(Level level, double x, double y, double z, int meta) {
        spawnFlamethrower(level, x, y, z, meta);
    }

    public static void spawnLegacyFlameEffectClient(Level level, double x, double y, double z, int meta) {
        spawnFlamethrower(level, x, y, z, meta);
    }

    public static void spawnFireFlamethrower(Level level, double x, double y, double z) {
        spawnFlamethrower(level, x, y, z, FLAMETHROWER_META_FIRE);
    }

    public static void spawnBalefireFlamethrower(Level level, double x, double y, double z) {
        spawnFlamethrower(level, x, y, z, FLAMETHROWER_META_BALEFIRE);
    }

    public static void spawnDigammaFlamethrower(Level level, double x, double y, double z) {
        spawnFlamethrower(level, x, y, z, FLAMETHROWER_META_DIGAMMA);
    }

    public static void spawnOxyFlamethrower(Level level, double x, double y, double z) {
        spawnFlamethrower(level, x, y, z, FLAMETHROWER_META_OXY);
    }

    public static void spawnBlackFlamethrower(Level level, double x, double y, double z) {
        spawnFlamethrower(level, x, y, z, FLAMETHROWER_META_BLACK);
    }

    public static void spawnHaze(Level level, double x, double y, double z) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_HAZE);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnHaze(Level level, Vec3 position) {
        if (position == null) {
            return;
        }
        spawnHaze(level, position.x, position.y, position.z);
    }

    public static void spawnHazeCloud(Level level, Vec3 center, int count, double horizontalSpread) {
        if (level == null || center == null || count <= 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            spawnHaze(level,
                    center.x + level.random.nextGaussian() * horizontalSpread,
                    center.y,
                    center.z + level.random.nextGaussian() * horizontalSpread);
        }
    }

    public static void spawnLegacyColoredGasCloud(Level level, BlockPos pos, float red, float green, float blue) {
        if (level == null || pos == null) {
            return;
        }
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_VANILLA_EXT);
        data.putString("mode", VANILLA_CLOUD);
        data.putFloat("r", red);
        data.putFloat("g", green);
        data.putFloat("b", blue);
        spawnAux(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, data, 0.0D);
    }

    public static void spawnPlasmaBlast(Level level, double x, double y, double z, float red, float green, float blue,
            float pitch, float yaw, float scale) {
        spawnPlasmaBlast(level, x, y, z, red, green, blue, pitch, yaw, scale, 150.0D);
    }

    public static void spawnPlasmaBlast(Level level, double x, double y, double z, float red, float green, float blue,
            float pitch, float yaw, float scale, double range) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_PLASMA_BLAST);
        data.putFloat("r", red);
        data.putFloat("g", green);
        data.putFloat("b", blue);
        data.putFloat("pitch", pitch);
        data.putFloat("yaw", yaw);
        data.putFloat("scale", scale);
        spawnAux(level, x, y, z, data, range);
    }

    public static void spawnPlasmaBlast(Level level, Vec3 position, float red, float green, float blue,
            float pitch, float yaw, float scale) {
        if (position == null) {
            return;
        }
        spawnPlasmaBlast(level, position.x, position.y, position.z, red, green, blue, pitch, yaw, scale);
    }

    public static void spawnPlasmaBlast(Level level, Vec3 position, int color, Direction direction, float scale) {
        if (position == null) {
            return;
        }
        Direction dir = horizontalOrNorth(direction);
        spawnPlasmaBlast(level, position.x, position.y, position.z,
                legacyColorComponent(color, 16),
                legacyColorComponent(color, 8),
                legacyColorComponent(color, 0),
                legacyPlasmaPitch(dir),
                legacyPlasmaYaw(dir),
                scale);
    }

    public static void spawnEmitterPlasmaBlast(Level level, Vec3 position, int color, Direction direction, float girth) {
        spawnPlasmaBlast(level, position, color, direction, girth * 5.0F);
    }

    public static void spawnArtilleryClusterSplitPlasmaBlast(Level level, Vec3 position) {
        if (position == null) {
            return;
        }
        spawnPlasmaBlast(level, position.x, position.y, position.z,
                1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 50.0F, 500.0D);
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

    public static void spawnLaunchSmoke(Level level, Vec3 position, Vec3 motion) {
        if (position == null || motion == null) {
            return;
        }
        spawnLaunchSmoke(level, position.x, position.y, position.z, motion.x, motion.y, motion.z);
    }

    public static void spawnLaunchPadSmokeBurst(Level level, BlockPos pos, Direction facing, boolean allowSideRotation) {
        spawnLaunchPadSmokeBurst(level, pos, facing, 15, allowSideRotation);
    }

    public static void spawnLaunchPadSmokeBurst(Level level, BlockPos pos, Direction facing, int count, boolean allowSideRotation) {
        if (level == null || pos == null || count <= 0) {
            return;
        }
        Direction base = horizontalOrNorth(facing);
        for (int i = 0; i < count; i++) {
            Direction dir = level.random.nextBoolean() ? base.getOpposite() : base;
            if (allowSideRotation && level.random.nextBoolean()) {
                dir = dir.getClockWise();
            }
            double speed = level.random.nextGaussian() * 0.15D + 0.75D;
            spawnLaunchSmoke(level, pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D,
                    speed * dir.getStepX(), 0.0D, speed * dir.getStepZ());
        }
    }

    public static void spawnLaunchTableSmokeBurst(Level level, BlockPos pos, double horizontalSigma) {
        spawnLaunchTableSmokeBurst(level, pos, 15, horizontalSigma);
    }

    public static void spawnLaunchTableSmokeBurst(Level level, BlockPos pos, int count, double horizontalSigma) {
        if (level == null || pos == null || count <= 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            boolean zAxis = level.random.nextBoolean();
            double motionX = zAxis ? 0.0D : level.random.nextGaussian() * horizontalSigma;
            double motionZ = zAxis ? level.random.nextGaussian() * horizontalSigma : 0.0D;
            spawnLaunchSmoke(level, pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D,
                    motionX, 0.0D, motionZ);
        }
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
        if (player == null) {
            return;
        }
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_BNUUY);
        data.putInt("player", player.getId());
        spawnAux(level, player.getX(), player.getY(), player.getZ(), data, 100.0D);
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

    public static void spawnTownAura(Level level, double x, double y, double z) {
        spawnVanillaExtTownAura(level, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnRandomTownAuraInBlock(Level level, BlockPos pos, RandomSource random) {
        RandomSource rand = randomOrLevel(level, random);
        if (level == null || pos == null || rand == null) {
            return;
        }
        spawnTownAura(level,
                pos.getX() + rand.nextFloat(),
                pos.getY() + rand.nextFloat(),
                pos.getZ() + rand.nextFloat());
    }

    public static void spawnRandomTownAuraAboveBlock(Level level, BlockPos pos, RandomSource random) {
        RandomSource rand = randomOrLevel(level, random);
        if (level == null || pos == null || rand == null) {
            return;
        }
        spawnTownAura(level,
                pos.getX() + rand.nextFloat(),
                pos.getY() + 1.1D,
                pos.getZ() + rand.nextFloat());
    }

    public static void spawnRandomTownAuraOnBarrel(Level level, BlockPos pos, RandomSource random) {
        RandomSource rand = randomOrLevel(level, random);
        if (level == null || pos == null || rand == null) {
            return;
        }
        spawnTownAura(level,
                pos.getX() + rand.nextFloat() * 0.5F + 0.25F,
                pos.getY() + 1.1D,
                pos.getZ() + rand.nextFloat() * 0.5F + 0.25F);
    }

    public static void spawnRandomSmokeInBlock(Level level, BlockPos pos, RandomSource random) {
        RandomSource rand = randomOrLevel(level, random);
        if (level == null || pos == null || rand == null) {
            return;
        }
        spawnVanillaSmoke(level,
                pos.getX() + rand.nextFloat(),
                pos.getY() + rand.nextFloat(),
                pos.getZ() + rand.nextFloat());
    }

    public static void spawnDeadLeafDrop(Level level, BlockPos pos, RandomSource random) {
        RandomSource rand = randomOrLevel(level, random);
        if (level == null || pos == null || rand == null) {
            return;
        }
        spawnDeadLeaf(level,
                pos.getX() + rand.nextDouble(),
                pos.getY() - 0.05D,
                pos.getZ() + rand.nextDouble());
    }

    public static void spawnOutgasTownAuraBurst(Level level, BlockPos pos, RandomSource random, int count) {
        for (int i = 0; i < count; i++) {
            spawnRandomTownAuraAboveBlock(level, pos, random);
        }
    }

    public static void spawnTownAuraOnOpenFaces(Level level, BlockPos pos, RandomSource random) {
        spawnHazardOpenFaceEffect(level, pos, random, false);
    }

    public static void spawnSchrabFogOnOpenFaces(Level level, BlockPos pos, RandomSource random) {
        spawnHazardOpenFaceEffect(level, pos, random, true);
    }

    private static void spawnHazardOpenFaceEffect(Level level, BlockPos pos, RandomSource random, boolean schrab) {
        RandomSource rand = randomOrLevel(level, random);
        if (level == null || pos == null || rand == null) {
            return;
        }
        for (Direction direction : Direction.values()) {
            if (!level.isEmptyBlock(pos.relative(direction))) {
                continue;
            }
            Vec3 point = randomLegacyOpenFacePoint(pos, direction, rand);
            if (schrab) {
                spawnSchrabFog(level, point.x, point.y, point.z);
            } else {
                spawnTownAura(level, point.x, point.y, point.z);
            }
        }
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
        spawnAux(level, player.getX(), player.getY(), player.getZ(), data, 100.0D);
    }

    public static void spawnSmoke(Level level, double x, double y, double z, String mode, int count) {
        CompoundTag data = smokeTag(mode, count);
        spawnAux(level, x, y, z, data, 250.0D);
    }

    public static void spawnSmokeCloud(Level level, double x, double y, double z, int count) {
        spawnSmoke(level, x, y, z, SMOKE_CLOUD, count);
    }

    public static void spawnSmokeRadial(Level level, double x, double y, double z, int count) {
        spawnSmoke(level, x, y, z, SMOKE_RADIAL, count);
    }

    public static void spawnSmokeRadialDigamma(Level level, double x, double y, double z, int count) {
        spawnSmoke(level, x, y, z, SMOKE_RADIAL_DIGAMMA, count);
    }

    public static void spawnSmokeShock(Level level, double x, double y, double z, int count, double strength, boolean randomStrength) {
        CompoundTag data = smokeTag(randomStrength ? SMOKE_SHOCK_RANDOM : SMOKE_SHOCK, count);
        data.putDouble("strength", strength);
        spawnAux(level, x, y, z, data, 250.0D);
    }

    public static void spawnSmokeShock(Level level, double x, double y, double z, int count, double strength) {
        spawnSmokeShock(level, x, y, z, count, strength, false);
    }

    public static void spawnSmokeShockRandom(Level level, double x, double y, double z, int count, double strength) {
        spawnSmokeShock(level, x, y, z, count, strength, true);
    }

    public static void spawnSmokeRing(Level level, double x, double y, double z, String mode, int count, double range) {
        CompoundTag data = smokeTag(mode, count);
        data.putDouble("range", range);
        spawnAux(level, x, y, z, data, 250.0D);
    }

    public static void spawnSmokeWave(Level level, double x, double y, double z, int count, double range) {
        spawnSmokeRing(level, x, y, z, SMOKE_WAVE, count, range);
    }

    public static void spawnSmokeFoamSplash(Level level, double x, double y, double z, int count, double range) {
        spawnSmokeRing(level, x, y, z, SMOKE_FOAM_SPLASH, count, range);
    }

    public static void spawnVanillaBurst(Level level, double x, double y, double z, String mode, int count, double motion) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_VANILLA_BURST);
        data.putString("mode", mode);
        data.putInt("count", count);
        data.putDouble("motion", motion);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnVanillaFlameBurst(Level level, double x, double y, double z, int count, double motion) {
        spawnVanillaBurst(level, x, y, z, VANILLA_FLAME, count, motion);
    }

    public static void spawnVanillaCloudBurst(Level level, double x, double y, double z, int count, double motion) {
        spawnVanillaBurst(level, x, y, z, VANILLA_CLOUD, count, motion);
    }

    public static void spawnVanillaRedDustBurst(Level level, double x, double y, double z, int count, double motion) {
        spawnVanillaBurst(level, x, y, z, VANILLA_RED_DUST, count, motion);
    }

    public static void spawnVanillaBlueDustBurst(Level level, double x, double y, double z, int count, double motion) {
        spawnVanillaBurst(level, x, y, z, VANILLA_BLUE_DUST, count, motion);
    }

    public static void spawnVanillaGreenDustBurst(Level level, double x, double y, double z, int count, double motion) {
        spawnVanillaBurst(level, x, y, z, VANILLA_GREEN_DUST, count, motion);
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

    public static void spawnVanillaRedstoneBlockDustBurst(Level level, double x, double y, double z, int count, double motion) {
        spawnVanillaBlockDustBurst(level, x, y, z, count, motion, Blocks.REDSTONE_BLOCK.defaultBlockState());
    }

    public static void spawnVanillaLegacyBlockDustBurst(Level level, double x, double y, double z, int count, double motion,
            int legacyBlockId, int meta) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_VANILLA_BURST);
        data.putString("mode", VANILLA_BLOCK_DUST);
        data.putInt("count", count);
        data.putDouble("motion", motion);
        putLegacyBlockId(data, legacyBlockId, meta);
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

    public static void spawnVanilla(Level level, double x, double y, double z, String mode, double motionX, double motionY, double motionZ) {
        CompoundTag data = vanillaTag(mode, motionX, motionY, motionZ);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnVanillaFlame(Level level, double x, double y, double z) {
        spawnVanilla(level, x, y, z, VANILLA_FLAME, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnVanillaSmoke(Level level, double x, double y, double z) {
        spawnVanilla(level, x, y, z, VANILLA_SMOKE, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnVanillaCloud(Level level, double x, double y, double z) {
        spawnVanilla(level, x, y, z, VANILLA_CLOUD, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnVanillaCloud(Level level, double x, double y, double z,
            double motionX, double motionY, double motionZ) {
        spawnVanilla(level, x, y, z, VANILLA_CLOUD, motionX, motionY, motionZ);
    }

    public static void spawnVanillaSmoke(Level level, double x, double y, double z,
            double motionX, double motionY, double motionZ) {
        spawnVanilla(level, x, y, z, VANILLA_SMOKE, motionX, motionY, motionZ);
    }

    public static void spawnVanillaExplode(Level level, double x, double y, double z, double motionX, double motionY, double motionZ) {
        spawnVanilla(level, x, y, z, VANILLA_EXPLODE, motionX, motionY, motionZ);
    }

    public static void spawnVanillaLargeExplode(Level level, double x, double y, double z, float size) {
        spawnVanilla(level, x, y, z, VANILLA_LARGE_EXPLODE, size, 0.0D, 0.0D);
    }

    public static void spawnVanillaHugeExplosion(Level level, double x, double y, double z) {
        spawnVanilla(level, x, y, z, VANILLA_HUGE_EXPLOSION, 1.0D, 0.0D, 0.0D);
    }

    public static void spawnVanillaExt(Level level, double x, double y, double z, String mode, double motionX, double motionY, double motionZ) {
        CompoundTag data = vanillaExtTag(mode, motionX, motionY, motionZ);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnVanillaExtFlame(Level level, double x, double y, double z,
            double motionX, double motionY, double motionZ) {
        spawnVanillaExt(level, x, y, z, VANILLA_FLAME, motionX, motionY, motionZ);
    }

    public static void spawnVanillaExtFlame(Level level, double x, double y, double z) {
        spawnVanillaExtFlame(level, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnVanillaExtSmoke(Level level, double x, double y, double z,
            double motionX, double motionY, double motionZ) {
        spawnVanillaExt(level, x, y, z, VANILLA_SMOKE, motionX, motionY, motionZ);
    }

    public static void spawnVanillaExtSmoke(Level level, double x, double y, double z) {
        spawnVanillaExtSmoke(level, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnVanillaExtCloud(Level level, double x, double y, double z,
            double motionX, double motionY, double motionZ) {
        spawnVanillaExt(level, x, y, z, VANILLA_CLOUD, motionX, motionY, motionZ);
    }

    public static void spawnVanillaExtCloud(Level level, double x, double y, double z) {
        spawnVanillaExtCloud(level, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnVanillaExtRedDust(Level level, double x, double y, double z,
            double motionX, double motionY, double motionZ) {
        spawnVanillaExt(level, x, y, z, VANILLA_RED_DUST, motionX, motionY, motionZ);
    }

    public static void spawnVanillaExtRedDust(Level level, double x, double y, double z) {
        spawnVanillaExtRedDust(level, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnVanillaExtBlueDust(Level level, double x, double y, double z) {
        spawnVanillaExt(level, x, y, z, VANILLA_BLUE_DUST, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnVanillaExtGreenDust(Level level, double x, double y, double z) {
        spawnVanillaExt(level, x, y, z, VANILLA_GREEN_DUST, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnVanillaExtFireworks(Level level, double x, double y, double z) {
        spawnVanillaExt(level, x, y, z, VANILLA_FIREWORKS, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnVanillaExtTownAura(Level level, double x, double y, double z,
            double motionX, double motionY, double motionZ) {
        spawnVanillaExt(level, x, y, z, VANILLA_TOWN_AURA, motionX, motionY, motionZ);
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

    public static void spawnVanillaExtSmoke(Level level, double x, double y, double z,
            double motionX, double motionY, double motionZ, int overrideAge, boolean noClip) {
        spawnVanillaExt(level, x, y, z, VANILLA_SMOKE, motionX, motionY, motionZ, overrideAge, noClip);
    }

    public static void spawnGasFlareSmoke(Level level, double x, double y, double z) {
        spawnVanillaExtSmoke(level, x, y, z, 0.0D, 0.0D, 0.0D, 50, true);
    }

    public static void spawnVanillaExtVolcano(Level level, double x, double y, double z) {
        CompoundTag data = vanillaExtTag(VANILLA_VOLCANO, 0.0D, 0.0D, 0.0D);
        spawnAux(level, x, y, z, data, 250.0D);
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
            double motionX, double motionY, double motionZ, int legacyBlockId, int meta) {
        CompoundTag data = vanillaExtTag(VANILLA_BLOCK_DUST, motionX, motionY, motionZ);
        putLegacyBlockId(data, legacyBlockId, meta);
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
        spawnExplosionLarge(level, x, y, z, cloudCount, cloudScale, cloudSpeedMult, waveScale, debrisCount,
                16, 50, 1.0F, 3.0F, -2.0F, 200.0F);
    }

    public static void spawnLegacyExplosionSmall(Level level, double x, double y, double z) {
        spawnExplosionLarge(level, x, y, z, 10, 2.0F, 0.5F, 25.0F, 5, 8, 20, 0.75F, 1.0F, -2.0F, 150.0F);
    }

    public static void spawnLegacyExplosionStandard(Level level, double x, double y, double z) {
        spawnExplosionLarge(level, x, y, z, 15, 5.0F, 1.0F, 45.0F, 10, 16, 50, 1.0F, 3.0F, -2.0F, 200.0F);
    }

    public static void spawnLegacyExplosionLarge(Level level, double x, double y, double z) {
        spawnExplosionLarge(level, x, y, z, 30, 6.5F, 2.0F, 65.0F, 25, 16, 50, 1.25F, 3.0F, -2.0F, 350.0F);
    }

    public static void spawnLegacyShrapnelTrailFlame(Level level, double x, double y, double z) {
        spawnVanillaFlame(level, x, y, z);
    }

    public static void spawnLegacyShrapnelLavaSplash(ServerLevel level, Vec3 position) {
        if (level == null || position == null) {
            return;
        }
        level.sendParticles(ParticleTypes.LAVA, position.x, position.y, position.z,
                5, 0.15D, 0.15D, 0.15D, 0.0D);
    }

    public static void spawnLegacyArtillerySmokeTrail(Level level, double x, double y, double z) {
        spawnVanillaSmoke(level, x, y + 0.5D, z, 0.0D, 0.1D, 0.0D);
    }

    public static void spawnLegacyPrimedSmoke(ServerLevel level, double x, double y, double z) {
        if (level == null) {
            return;
        }
        level.sendParticles(ParticleTypes.SMOKE, x, y + 0.5D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnEmpMachineBurst(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PURPLE_STAINED_GLASS.defaultBlockState()),
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                8, 0.25D, 0.25D, 0.25D, 0.05D);
    }

    public static void spawnPortalParticle(ServerLevel level, double x, double y, double z,
            double motionX, double motionY, double motionZ) {
        if (level == null) {
            return;
        }
        level.sendParticles(ParticleTypes.PORTAL, x, y, z, 1, motionX, motionY, motionZ, 0.0D);
    }

    public static void spawnExplosionLarge(Level level, double x, double y, double z, int cloudCount, float cloudScale, float cloudSpeedMult,
            float waveScale, int debrisCount, int debrisSize, int debrisRetry, float debrisVelocity,
            float debrisHorizontalDeviation, float debrisVerticalOffset, float soundRange) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_EXPLOSION_LARGE);
        data.putInt("cloudCount", cloudCount);
        data.putFloat("cloudScale", cloudScale);
        data.putFloat("cloudSpeedMult", cloudSpeedMult);
        data.putFloat("waveScale", waveScale);
        data.putInt("debrisCount", debrisCount);
        data.putInt("debrisSize", Math.max(0, debrisSize));
        data.putInt("debrisRetry", Math.max(0, debrisRetry));
        data.putFloat("debrisVelocity", debrisVelocity);
        data.putFloat("debrisHorizontalDeviation", debrisHorizontalDeviation);
        data.putFloat("debrisVerticalOffset", debrisVerticalOffset);
        data.putFloat("soundRange", soundRange);
        spawnAux(level, x, y, z, data, Math.max(300.0D, soundRange));
    }

    public static void spawnExplosionSmall(Level level, double x, double y, double z, int cloudCount, float cloudScale, float cloudSpeedMult) {
        spawnExplosionSmall(level, x, y, z, cloudCount, cloudScale, cloudSpeedMult, 15);
    }

    public static void spawnExplosionSmall(Level level, double x, double y, double z, int cloudCount, float cloudScale,
            float cloudSpeedMult, int debris) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_EXPLOSION_SMALL);
        data.putInt("cloudCount", cloudCount);
        data.putFloat("cloudScale", cloudScale);
        data.putFloat("cloudSpeedMult", cloudSpeedMult);
        data.putInt("debris", Math.max(0, debris));
        spawnAux(level, x, y, z, data, 200.0D);
    }

    public static void spawnWeaponExplosion(Level level, double x, double y, double z, int count, float scale, float speed) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_WEAPON_EXPLOSION);
        data.putInt("count", count);
        data.putFloat("scale", scale);
        data.putFloat("speed", speed);
        spawnAux(level, x, y, z, data, Math.max(100.0D, scale * 16.0D));
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

    public static void spawnBlackPowder(Level level, Vec3 position, Vec3 heading, int cloudCount, float cloudScale,
            float cloudSpeedMult, int sparkCount, float sparkSpeedMult) {
        if (position == null || heading == null) {
            return;
        }
        spawnBlackPowder(level, position.x(), position.y(), position.z(), heading.x(), heading.y(), heading.z(),
                cloudCount, cloudScale, cloudSpeedMult, sparkCount, sparkSpeedMult);
    }

    public static void spawnBlackPowder(Entity entity, int cloudCount, float cloudScale, float cloudSpeedMult,
            int sparkCount, float sparkSpeedMult) {
        if (entity == null) {
            return;
        }
        Vec3 motion = entity.getDeltaMovement();
        spawnBlackPowder(entity.level(), entity.getX(), entity.getY(), entity.getZ(), motion.x(), motion.y(), motion.z(),
                cloudCount, cloudScale, cloudSpeedMult, sparkCount, sparkSpeedMult);
    }

    public static void spawnMk4BlackPowder(Entity entity) {
        spawnBlackPowder(entity, 10, 0.25F, 0.5F, 10, 0.25F);
    }

    public static void spawnAshes(Level level, double x, double y, double z, int entityId, int ashesCount, float ashesScale) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_ASHES);
        data.putInt("entityID", entityId);
        data.putInt("ashesCount", ashesCount);
        data.putFloat("ashesScale", ashesScale);
        spawnAux(level, x, y, z, data, 100.0D);
    }

    public static void spawnAshes(Entity entity, int ashesCount, float ashesScale) {
        if (entity == null) {
            return;
        }
        spawnAshes(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity.getId(), ashesCount, ashesScale);
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

    public static void spawnCasingFromView(Level level, double x, double y, double z, float yaw, float pitch,
            double frontMotion, double heightMotion, double sideMotion, double motionVariance,
            float momentumPitch, float momentumYaw, String name, boolean smoking, int smokeLife, double smokeLift, int nodeLife) {
        if (level == null) {
            return;
        }
        Vec3 motion = legacyLocalToWorld(sideMotion, heightMotion, frontMotion, pitch, yaw);
        spawnCasing(level, x, y, z,
                motion.x() + level.random.nextGaussian() * motionVariance,
                motion.y() + level.random.nextGaussian() * motionVariance,
                motion.z() + level.random.nextGaussian() * motionVariance,
                yaw, pitch, momentumPitch, momentumYaw, name, smoking, smokeLife, smokeLift, nodeLife);
    }

    public static void spawnCasing(LivingEntity shooter, double frontOffset, double heightOffset, double sideOffset,
            double frontMotion, double heightMotion, double sideMotion, double motionVariance, String name) {
        spawnCasing(shooter, frontOffset, heightOffset, sideOffset, frontMotion, heightMotion, sideMotion, motionVariance,
                5.0F, 10.0F, name, false, 0, 0.0D, 0);
    }

    public static void spawnCasing(LivingEntity shooter, double frontOffset, double heightOffset, double sideOffset,
            double frontMotion, double heightMotion, double sideMotion, double motionVariance,
            float momentumPitch, float momentumYaw, String name) {
        spawnCasing(shooter, frontOffset, heightOffset, sideOffset, frontMotion, heightMotion, sideMotion, motionVariance,
                momentumPitch, momentumYaw, name, false, 0, 0.0D, 0);
    }

    public static void spawnCasing(LivingEntity shooter, double frontOffset, double heightOffset, double sideOffset,
            double frontMotion, double heightMotion, double sideMotion, double motionVariance,
            float momentumPitch, float momentumYaw, String name, boolean smoking, int smokeLife, double smokeLift, int nodeLife) {
        if (shooter == null) {
            return;
        }
        double adjustedHeightOffset = shooter.isShiftKeyDown() ? heightOffset - 0.075D : heightOffset;
        Vec3 offset = legacyLocalToWorld(sideOffset, adjustedHeightOffset, frontOffset, shooter.getXRot(), shooter.getYRot());
        Vec3 localMotion = legacyLocalToWorld(sideMotion, heightMotion, frontMotion, shooter.getXRot(), shooter.getYRot());
        Vec3 entityMotion = shooter.getDeltaMovement();
        Level level = shooter.level();
        double motionX = entityMotion.x() + localMotion.x() + level.random.nextGaussian() * motionVariance;
        double motionY = entityMotion.y() + localMotion.y() + level.random.nextGaussian() * motionVariance;
        double motionZ = entityMotion.z() + localMotion.z() + level.random.nextGaussian() * motionVariance;
        if (shooter instanceof Player player && player.getAbilities().flying) {
            motionY -= 0.04D;
        }
        spawnCasing(level,
                shooter.getX() + offset.x(),
                shooter.getY() + shooter.getEyeHeight() + offset.y(),
                shooter.getZ() + offset.z(),
                motionX, motionY, motionZ,
                shooter.getYRot(), shooter.getXRot(), momentumPitch, momentumYaw,
                name, smoking, smokeLife, smokeLift, nodeLife);
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

    public static void spawnSkeleton(Entity entity, float brightness) {
        if (entity == null) {
            return;
        }
        spawnSkeleton(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity.getId(), brightness, false, 0.0F);
    }

    public static void spawnSkeletonGib(Entity entity, float force) {
        if (entity == null) {
            return;
        }
        spawnSkeleton(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity.getId(), 1.0F, true, force);
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

    public static void spawnRbmkSteam(Level level, double x, double y, double z,
            double targetX, double targetY, double targetZ) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_RBMK_STEAM);
        spawnAux(level, x, y, z, targetX, targetY, targetZ, data, 100.0D);
    }

    public static void spawnRbmkMush(Level level, double x, double y, double z, float scale) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_RBMK_MUSH);
        data.putFloat("scale", scale);
        spawnAux(level, x, y, z, data, 250.0D);
    }

    public static void spawnRbmkMush(Level level, Vec3 position, float scale) {
        if (position == null) {
            return;
        }
        spawnRbmkMush(level, position.x, position.y, position.z, scale);
    }

    public static void spawnWatzRbmkMush(Level level, BlockPos pos) {
        if (pos == null) {
            return;
        }
        spawnRbmkMush(level, pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D, 5.0F);
    }

    public static void spawnRbmkMushFromBounds(Level level, int minX, int maxX, int minZ, int maxZ, double y) {
        int smallDim = Math.min(maxX - minX, maxZ - minZ);
        int avgX = minX + (maxX - minX) / 2;
        int avgZ = minZ + (maxZ - minZ) / 2;
        spawnRbmkMush(level, avgX + 0.5D, y, avgZ + 0.5D, smallDim);
    }

    public static void spawnArtilleryPhosphorusMush(Level level, Vec3 position) {
        spawnRbmkMush(level, position, 10.0F);
    }

    public static void spawnHimarsPhosphorusMush(Level level, Vec3 position) {
        spawnRbmkMush(level, position, 15.0F);
    }

    public static void spawnHimarsThermobaricMush(Level level, Vec3 position) {
        spawnRbmkMush(level, position, 20.0F);
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

    public static void spawnSmallCoolingTowerSteam(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        spawnCoolingTower(level, pos.getX() + 0.5D, pos.getY() + 18.0D, pos.getZ() + 0.5D,
                1.0F, 0.5F, 4.0F, 250 + level.random.nextInt(250));
    }

    public static void spawnLargeCoolingTowerSteam(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        spawnCoolingTower(level,
                pos.getX() + 0.5D + level.random.nextDouble() * 3.0D - 1.5D,
                pos.getY() + 1.0D,
                pos.getZ() + 0.5D + level.random.nextDouble() * 3.0D - 1.5D,
                0.5F, 1.0F, 10.0F, 750 + level.random.nextInt(250));
    }

    public static void spawnBrickChimneySmoke(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        spawnCoolingTower(level, pos.getX() + 0.5D, pos.getY() + 12.0D, pos.getZ() + 0.5D,
                10.0F, 0.5F, 3.0F, 250 + level.random.nextInt(50), false, 0.075F, 0.25F, 0x404040);
    }

    public static void spawnIndustrialChimneySmoke(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        spawnCoolingTower(level, pos.getX() + 0.5D, pos.getY() + 22.0D, pos.getZ() + 0.5D,
                10.0F, 0.75F, 3.0F, 250 + level.random.nextInt(50), false, 0.075F, 0.25F, 0x404040);
    }

    public static void spawnCokerChimneySmoke(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        spawnCoolingTower(level, pos.getX() + 0.5D, pos.getY() + 22.0D, pos.getZ() + 0.5D,
                10.0F, 0.75F, 3.0F, 200 + level.random.nextInt(50), false, 0.075F, 0.25F, 0x404040);
    }

    public static void spawnGasFlareVentSmoke(Level level, BlockPos pos, int color) {
        if (level == null || pos == null) {
            return;
        }
        spawnCoolingTower(level, pos.getX() + 0.5D, pos.getY() + 11.0D, pos.getZ() + 0.5D,
                1.0F, 0.25F, 3.0F, 150 + level.random.nextInt(20), false, 0.075F, 0.25F, color);
    }

    public static void spawnMachineVentSmoke(Level level, double x, double y, double z, float baseScale, float maxScale, int color) {
        if (level == null) {
            return;
        }
        spawnCoolingTower(level, x, y, z, 10.0F, baseScale, maxScale, 100 + level.random.nextInt(20),
                false, 0.075F, 0.25F, color);
    }

    public static void spawnPyroOvenVentSmoke(Level level, BlockPos pos, Direction ventDirection) {
        if (pos == null) {
            return;
        }
        Direction dir = horizontalOrNorth(ventDirection);
        spawnMachineVentSmoke(level, pos.getX() + 0.5D - dir.getStepX(), pos.getY() + 3.0D,
                pos.getZ() + 0.5D - dir.getStepZ(), 0.25F, 2.5F, 0x202020);
    }

    public static void spawnPyroOvenOperatingClouds(Level level, BlockPos pos, Direction facing, Direction ventDirection) {
        if (level == null || pos == null) {
            return;
        }
        Direction dir = horizontalOrNorth(facing);
        Direction rot = horizontalOrNorth(ventDirection);
        spawnPyroOvenOperatingCloud(level, pos, dir, rot, -0.875D);
        spawnPyroOvenOperatingCloud(level, pos, dir, rot, -2.375D);
        spawnPyroOvenOperatingCloud(level, pos, dir, rot, 0.875D);
        spawnPyroOvenOperatingCloud(level, pos, dir, rot, 2.375D);
    }

    private static void spawnPyroOvenOperatingCloud(Level level, BlockPos pos, Direction facing, Direction ventDirection,
            double forwardOffset) {
        if (level.random.nextInt(20) != 0) {
            return;
        }
        spawnVanillaCloud(level,
                pos.getX() + 0.5D - ventDirection.getStepX() + facing.getStepX() * forwardOffset,
                pos.getY() + 3.0D,
                pos.getZ() + 0.5D - ventDirection.getStepZ() + facing.getStepZ() * forwardOffset,
                0.0D, 0.05D, 0.0D);
    }

    public static void spawnRotaryFurnaceVentSmoke(Level level, BlockPos pos, Direction ventDirection) {
        if (pos == null) {
            return;
        }
        Direction dir = horizontalOrNorth(ventDirection);
        spawnMachineVentSmoke(level, pos.getX() + 0.5D + dir.getStepX(), pos.getY() + 5.0D,
                pos.getZ() + 0.5D + dir.getStepZ(), 0.25F, 2.5F, 0x202020);
    }

    public static void spawnCrucibleSootSmoke(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        spawnCoolingTower(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                10.0F, 0.75F, 3.5F, 100 + level.random.nextInt(20), false, 0.075F, 0.25F, 0x202020);
    }

    public static void spawnMachineDrainDischarge(Level level, BlockPos pos, Direction facing, int color, boolean gaseous) {
        if (level == null || pos == null) {
            return;
        }
        Direction dir = horizontalOrNorth(facing);
        double x = pos.getX() + 0.5D - dir.getStepX() * 2.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D - dir.getStepZ() * 2.5D;
        if (gaseous) {
            spawnCoolingTower(level, x, y, z, 0.5F, 0.375F, 3.0F, 100 + level.random.nextInt(50),
                    false, 0.075F, 0.25F, color);
        } else {
            spawnSplash(level, x, y, z, color);
        }
    }

    public static void spawnLaunchPadFuelVapor(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        double x = pos.getX() + 0.5D + level.random.nextGaussian() * 0.5D;
        double z = pos.getZ() + 0.5D + level.random.nextGaussian() * 0.5D;
        int lifetime = 70 + level.random.nextInt(30);
        for (int i = 0; i < 3; i++) {
            spawnCoolingTower(level, x, pos.getY() + 2.0D, z, 0.0F, 0.5F, 2.0F, lifetime,
                    true, 0.05F, 0.25F, -1);
        }
    }

    public static void spawnSplash(Level level, double x, double y, double z, int color) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_SPLASH);
        if (color >= 0) {
            data.putInt("color", color);
        }
        spawnAux(level, x, y, z, data, 100.0D);
    }

    public static void spawnSplash(Level level, Vec3 position, int color) {
        if (position == null) {
            return;
        }
        spawnSplash(level, position.x, position.y, position.z, color);
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

    public static void spawnFoundry(Level level, Vec3 position, int color, Direction direction, float length, float base, float offset) {
        if (position == null) {
            return;
        }
        spawnFoundry(level, position.x, position.y, position.z, color, legacyDirectionOrdinal(direction), length, base, offset);
    }

    public static void spawnFoundryPour(Level level, Vec3 position, int color, Direction direction, float length) {
        spawnFoundry(level, position, color, direction, length, 0.625F, 0.625F);
    }

    public static void spawnFoundryOutletPour(Level level, Vec3 position, int color, Direction direction, float length) {
        spawnFoundry(level, position, color, direction, length, 0.0F, 0.375F);
    }

    public static void spawnFoundryMachinePour(Level level, BlockPos pos, Direction direction, double horizontalOffset,
            double yOffset, int color, float length) {
        if (pos == null) {
            return;
        }
        Direction dir = horizontalOrNorth(direction);
        spawnFoundryPour(level,
                new Vec3(pos.getX() + 0.5D + dir.getStepX() * horizontalOffset,
                        pos.getY() + yOffset,
                        pos.getZ() + 0.5D + dir.getStepZ() * horizontalOffset),
                color, dir, length);
    }

    public static void spawnFoundryOutletPour(Level level, BlockPos pos, Direction direction, int color, float length) {
        if (pos == null) {
            return;
        }
        Direction dir = horizontalOrNorth(direction);
        spawnFoundryOutletPour(level,
                new Vec3(pos.getX() + 0.5D - dir.getStepX() * 0.125D,
                        pos.getY() + 0.125D,
                        pos.getZ() + 0.5D - dir.getStepZ() * 0.125D),
                color, dir, length);
    }

    public static void spawnChaosCloud(Level level, double x, double y, double z, double motionX, double motionY, double motionZ, String mode) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_CHAOS_CLOUD);
        data.putString("mode", normalizeChaosCloudMode(mode));
        data.putDouble("mX", motionX);
        data.putDouble("mY", motionY);
        data.putDouble("mZ", motionZ);
        spawnAux(level, x, y, z, data, 150.0D);
    }

    public static void spawnChaosCloudBurst(Level level, double x, double y, double z, int count, double speed, String mode) {
        if (level == null || count <= 0) {
            return;
        }
        String resolvedMode = normalizeChaosCloudMode(mode);
        for (int i = 0; i < count; i++) {
            spawnChaosCloud(level, x, y, z,
                    level.random.nextGaussian() * speed,
                    level.random.nextGaussian() * speed,
                    level.random.nextGaussian() * speed,
                    resolvedMode);
        }
    }

    public static void spawnChaosVolley(Level level, double x, double y, double z, int count, double speed) {
        if (level == null || count <= 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            spawnChaosCloud(level, x, y, z,
                    level.random.nextGaussian() * speed,
                    level.random.nextDouble() * speed * 7.5D,
                    level.random.nextGaussian() * speed,
                    CHAOS_CLOUD_ORANGE);
        }
    }

    private static RandomSource randomOrLevel(Level level, RandomSource random) {
        if (random != null) {
            return random;
        }
        return level == null ? null : level.random;
    }

    private static Vec3 randomLegacyOpenFacePoint(BlockPos pos, Direction direction, RandomSource random) {
        double x = pos.getX() + 0.5D + direction.getStepX() + random.nextDouble() * 3.0D - 1.5D;
        double y = pos.getY() + 0.5D + direction.getStepY() + random.nextDouble() * 3.0D - 1.5D;
        double z = pos.getZ() + 0.5D + direction.getStepZ() + random.nextDouble() * 3.0D - 1.5D;
        if (direction.getStepX() != 0) {
            x = pos.getX() + 0.5D + direction.getStepX() * 0.5D + random.nextDouble() * direction.getStepX();
        }
        if (direction.getStepY() != 0) {
            y = pos.getY() + 0.5D + direction.getStepY() * 0.5D + random.nextDouble() * direction.getStepY();
        }
        if (direction.getStepZ() != 0) {
            z = pos.getZ() + 0.5D + direction.getStepZ() * 0.5D + random.nextDouble() * direction.getStepZ();
        }
        return new Vec3(x, y, z);
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

    private static CompoundTag vanillaTag(String mode, double motionX, double motionY, double motionZ) {
        CompoundTag data = new CompoundTag();
        data.putString("type", TYPE_VANILLA);
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

    private static String normalizeChaosCloudMode(String mode) {
        if (CHAOS_CLOUD_GREEN.equals(mode) || CHAOS_CLOUD_PINK.equals(mode)) {
            return mode;
        }
        return CHAOS_CLOUD_ORANGE;
    }

    private static Direction horizontalOrNorth(Direction direction) {
        if (direction == null || direction == Direction.UP || direction == Direction.DOWN) {
            return Direction.NORTH;
        }
        return direction;
    }

    private static int legacyDirectionOrdinal(Direction direction) {
        Direction dir = direction == null ? Direction.NORTH : direction;
        return switch (dir) {
            case DOWN -> 0;
            case UP -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
        };
    }

    private static float legacyColorComponent(int color, int shift) {
        return ((color >> shift) & 255) / 256.0F;
    }

    private static float legacyPlasmaPitch(Direction direction) {
        Direction dir = horizontalOrNorth(direction);
        return dir == Direction.NORTH || dir == Direction.WEST ? 90.0F : -90.0F;
    }

    private static float legacyPlasmaYaw(Direction direction) {
        Direction dir = horizontalOrNorth(direction);
        return dir == Direction.WEST || dir == Direction.EAST ? 90.0F : 0.0F;
    }

    private static Vec3 legacyLocalToWorld(double side, double height, double front, float pitchDegrees, float yawDegrees) {
        double pitch = -Math.toRadians(pitchDegrees);
        double pitchCos = Math.cos(pitch);
        double pitchSin = Math.sin(pitch);
        double yPitch = height * pitchCos + front * pitchSin;
        double zPitch = front * pitchCos - height * pitchSin;

        double yaw = -Math.toRadians(yawDegrees);
        double yawCos = Math.cos(yaw);
        double yawSin = Math.sin(yaw);
        return new Vec3(side * yawCos + zPitch * yawSin, yPitch, zPitch * yawCos - side * yawSin);
    }

    public static void putBlockState(CompoundTag data, BlockState state) {
        LegacyBlockStateMappings.putState(data, state);
    }

    public static void putLegacyBlockName(CompoundTag data, String legacyBlockName, int meta) {
        LegacyBlockStateMappings.putLegacyName(data, legacyBlockName, meta);
    }

    public static void putLegacyBlockId(CompoundTag data, int legacyBlockId, int meta) {
        LegacyBlockStateMappings.putLegacyId(data, legacyBlockId, meta);
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

    public static void spawnAux(Level level, double x, double y, double z,
            double targetX, double targetY, double targetZ, CompoundTag data, double range) {
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
            ModMessages.sendAuxParticle(serverLevel, x, y, z, targetX, targetY, targetZ, payload, range);
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
