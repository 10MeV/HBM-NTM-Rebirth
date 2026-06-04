package com.hbm.ntm.client.particle;

import com.hbm.ntm.client.ClientForgeEvents;
import com.hbm.ntm.client.render.HbmRenderEffects;
import com.hbm.ntm.client.render.HbmOverheadMarkers;
import com.hbm.ntm.particle.LegacyCasingEjectors;
import com.hbm.ntm.particle.LegacyBlockStateMappings;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.client.sound.HbmDelayedSounds;
import com.hbm.ntm.registry.ModParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public final class HbmParticleEffects {
    public static void handleAux(CompoundTag data) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || data == null) {
            return;
        }

        double x = data.getDouble("posX");
        double y = data.getDouble("posY");
        double z = data.getDouble("posZ");
        String type = data.getString("type");
        if (ParticleUtil.TYPE_GAS_FLAME.equals(type)) {
            spawnGasFlame(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_DEBUG_DRONE.equals(type) || ParticleUtil.TYPE_DEBUG_LINE.equals(type)) {
            spawnDebugLine(level, x, y, z, data.getDouble("mX"), data.getDouble("mY"), data.getDouble("mZ"), data.getInt("color"));
        } else if (ParticleUtil.TYPE_DEBUG_TEXT.equals(type)) {
            spawnDebugText(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_NETWORK.equals(type)) {
            spawnNetworkDebug(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_WATER_SPLASH.equals(type)) {
            spawnWaterSplash(level, x, y, z);
        } else if (ParticleUtil.TYPE_CLOUD_FX_2.equals(type)) {
            level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0D, 0.1D, 0.0D);
        } else if ("vanilla".equals(type)) {
            spawnNamedVanilla(level, data.getString("mode"), x, y, z, data.getDouble("mX"), data.getDouble("mY"), data.getDouble("mZ"));
        } else if (ParticleUtil.TYPE_VANILLA_BURST.equals(type)) {
            spawnVanillaBurst(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_VANILLA_EXT.equals(type)) {
            spawnVanillaExt(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_SMOKE.equals(type)) {
            spawnSmoke(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_LAUNCH_SMOKE.equals(type)) {
            level.addParticle(ModParticleTypes.SMOKE_PLUME.get(), x, y, z, data.getDouble("moX"), data.getDouble("moY"), data.getDouble("moZ"));
        } else if (ParticleUtil.TYPE_MISSILE_CONTRAIL.equals(type) || ParticleUtil.TYPE_ABM_CONTRAIL.equals(type)
                || ParticleUtil.TYPE_EX_KEROSENE.equals(type) || ParticleUtil.TYPE_EX_SOLID.equals(type)
                || ParticleUtil.TYPE_EX_HYDROGEN.equals(type) || ParticleUtil.TYPE_EX_BALEFIRE.equals(type)) {
            spawnContrail(level, data, x, y, z, type);
        } else if (ParticleUtil.TYPE_EXHAUST.equals(type)) {
            spawnExhaust(level, data, x, y, z);
        } else if ("flamethrower".equals(type)) {
            spawnFlamethrower(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_EXPLOSION_LARGE.equals(type)) {
            spawnExplosionLarge(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_EXPLOSION_SMALL.equals(type)) {
            spawnExplosionSmall(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_VNT_EXPLOSION.equals(type)) {
            spawnVntExplosion(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_BLACK_POWDER.equals(type)) {
            spawnBlackPowder(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_ASHES.equals(type)) {
            spawnAshes(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_CASING.equals(type) || ParticleUtil.TYPE_LEGACY_CASING.equals(type)) {
            spawnCasing(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_SKELETON.equals(type)) {
            spawnSkeleton(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_GIBLETS.equals(type)) {
            spawnGiblets(level, data, x, y, z);
        } else if ("sweat".equals(type)) {
            spawnEntitySweat(level, data);
        } else if ("vomit".equals(type)) {
            spawnEntityVomit(level, data);
        } else if (ParticleUtil.TYPE_RADIATION_FOG.equals(type) || ParticleUtil.TYPE_RADIATION_FOG_SNAKE.equals(type)) {
            level.addParticle(ModParticleTypes.RADIATION_FOG.get(), x, y, z, 0.0D, 0.0D, 0.0D);
        } else if (ParticleUtil.TYPE_RAD_FOG.equals(type)) {
            level.addParticle(ModParticleTypes.RADIATION_FOG.get(), x, y, z, 0.0D, 0.0D, 0.0D);
        } else if (ParticleUtil.TYPE_SCHRAB_FOG.equals(type)) {
            level.addParticle(ModParticleTypes.SCHRAB_FOG.get(), x, y, z, 0.0D, 0.0D, 0.0D);
        } else if ("weaponExplosion".equals(type)) {
            spawnWeaponExplosion(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_TAU.equals(type)) {
            spawnTau(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_HADRON.equals(type)) {
            spawnHadron(level, x, y, z, false);
        } else if (ParticleUtil.TYPE_AMAT_FLASH.equals(type)) {
            spawnAmat(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_RBMK_FLAME.equals(type)) {
            spawnRbmkFlame(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_RBMK_STEAM.equals(type)) {
            spawnRbmkSteam(level, x, y, z);
        } else if (ParticleUtil.TYPE_RBMK_MUSH.equals(type)) {
            spawnRbmkMush(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_COOLING_TOWER.equals(type)) {
            spawnCoolingTower(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_SPLASH.equals(type)) {
            spawnSplash(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_RIFT.equals(type)) {
            spawnRift(level, x, y, z);
        } else if (ParticleUtil.TYPE_DEAD_LEAF.equals(type)) {
            if (legacyVisibleParticleSetting(level)) {
                level.addParticle(ModParticleTypes.DEAD_LEAF.get(), x, y, z, 0.0D, 0.0D, 0.0D);
            }
        } else if (ParticleUtil.TYPE_FLUID_FILL.equals(type)) {
            spawnFluidFill(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_FOUNDRY.equals(type)) {
            spawnFoundry(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_FIREWORKS.equals(type)) {
            spawnFireworks(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_HAZE.equals(type)) {
            spawnHaze(level, x, y, z);
        } else if (ParticleUtil.TYPE_PLASMA_BLAST.equals(type)) {
            spawnPlasmaBlast(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_JUST_TILT.equals(type)) {
            applyClientJolt(data.getInt("time"), data.getInt("time"));
        } else if (ParticleUtil.TYPE_PROPER_JOLT.equals(type)) {
            applyClientJolt(data.getInt("time"), data.getInt("maxTime"));
        } else if (ParticleUtil.TYPE_JETPACK.equals(type)) {
            spawnJetpack(level, data);
        } else if (ParticleUtil.TYPE_BNUUY.equals(type)) {
            spawnBnuuy(level, data);
        } else if (ParticleUtil.TYPE_JETPACK_BJ.equals(type) || ParticleUtil.TYPE_JETPACK_DNS.equals(type)) {
            spawnColoredJetpack(level, data, ParticleUtil.TYPE_JETPACK_DNS.equals(type));
        } else if (ParticleUtil.TYPE_RADIATION.equals(type)) {
            spawnRadiationAura(level, data);
        } else if (ParticleUtil.TYPE_VANISH.equals(type)) {
            ClientForgeEvents.vanishEntity(data.getInt("ent"));
        } else if (ParticleUtil.TYPE_MARKER.equals(type)) {
            HbmOverheadMarkers.queue(x, y, z, data.getInt("color"), data.getInt("expires"), data.getDouble("dist"), data.getString("label"));
        } else if (ParticleUtil.TYPE_FROZEN.equals(type)) {
            applyClientFrozen();
        } else if (ParticleUtil.TYPE_MUKE.equals(type) || ParticleUtil.TYPE_TINY_TOT.equals(type)) {
            spawnMuke(level, data, x, y, z, ParticleUtil.TYPE_TINY_TOT.equals(type));
        } else if (ParticleUtil.TYPE_UFO.equals(type)) {
            spawnUfoCloud(level, data, x, y, z);
        } else if (ParticleUtil.TYPE_BALEFIRE_CLOUD.equals(type)) {
            MukeCloudParticle.add(level, x, y, z, 0.0D, 0.0D, 0.0D, true);
        } else if ("chaosCloud".equals(type)) {
            spawnChaosCloud(level, data, x, y, z);
        }
    }

    public static void burst(BlockPos pos, BlockState state) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && state != null) {
            minecraft.particleEngine.destroy(pos, state);
        }
    }

    private static void spawnDebugLine(ClientLevel level, double x, double y, double z, double lineX, double lineY, double lineZ, int color) {
        Minecraft.getInstance().particleEngine.add(new DebugLineParticle(level, x, y, z, lineX, lineY, lineZ, color));
    }

    private static void spawnGasFlame(ClientLevel level, CompoundTag data, double x, double y, double z) {
        if (GasFlameParticle.sharedSprites() == null) {
            level.addParticle(ModParticleTypes.GAS_FLAME.get(), x, y, z, data.getDouble("mX"), data.getDouble("mY"), data.getDouble("mZ"));
            return;
        }
        Minecraft.getInstance().particleEngine.add(new GasFlameParticle(level, x, y, z,
                data.getDouble("mX"), data.getDouble("mY"), data.getDouble("mZ"),
                GasFlameParticle.sharedSprites(), data.contains("scale") ? data.getFloat("scale") : 6.5F));
    }

    private static void spawnDebugText(ClientLevel level, CompoundTag data, double x, double y, double z) {
        Minecraft.getInstance().particleEngine.add(new DebugTextParticle(level, x, y, z,
                data.getInt("color"),
                data.getString("text"),
                Math.max(0.1F, getFloat(data, "scale", 1.0F))));
    }

    private static void spawnNetworkDebug(ClientLevel level, CompoundTag data, double x, double y, double z) {
        Particle particle = null;
        double motionX = data.getDouble("mX");
        double motionY = data.getDouble("mY");
        double motionZ = data.getDouble("mZ");
        if ("power".equals(data.getString("mode"))) {
            particle = NetworkDebugParticle.power(level, x, y, z, motionX, motionY, motionZ);
        } else if ("fluid".equals(data.getString("mode"))) {
            particle = NetworkDebugParticle.fluid(level, x, y, z, motionX, motionY, motionZ, data.getInt("color"));
        }
        if (particle != null) {
            Minecraft.getInstance().particleEngine.add(particle);
        }
    }

    private static void spawnFireworks(ClientLevel level, CompoundTag data, double x, double y, double z) {
        int color = data.getInt("color");
        Minecraft.getInstance().particleEngine.add(new FireworkLetterParticle(level, x, y, z, color, (char) data.getInt("char")));
        RandomSource random = level.random;
        for (int i = 0; i < 50; i++) {
            Particle spark = Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.FIREWORK, x, y, z,
                    random.nextGaussian() * 0.4D,
                    random.nextGaussian() * 0.4D,
                    random.nextGaussian() * 0.4D);
            if (spark != null) {
                setParticleColor(spark, color);
            }
        }
    }

    private static void spawnHaze(ClientLevel level, double x, double y, double z) {
        Particle particle = HazeParticle.create(level, x, y, z);
        if (particle != null) {
            Minecraft.getInstance().particleEngine.add(particle);
        }
    }

    private static void spawnPlasmaBlast(ClientLevel level, CompoundTag data, double x, double y, double z) {
        Particle particle = PlasmaBlastParticle.create(level, x, y, z,
                getFloat(data, "r", 1.0F),
                getFloat(data, "g", 1.0F),
                getFloat(data, "b", 1.0F),
                getFloat(data, "pitch", 0.0F),
                getFloat(data, "yaw", 0.0F),
                Math.max(0.1F, getFloat(data, "scale", 1.0F)));
        if (particle != null) {
            Minecraft.getInstance().particleEngine.add(particle);
        }
    }

    private static void applyClientJolt(int time, int maxTime) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.hurtTime = time;
            Minecraft.getInstance().player.hurtDuration = Math.max(time, maxTime);
        }
    }

    private static void applyClientFrozen() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(0.0D, Math.min(motion.y, 0.0D), 0.0D);
        if (player.input != null) {
            player.input.forwardImpulse = 0.0F;
            player.input.leftImpulse = 0.0F;
        }
        player.zza = 0.0F;
        player.xxa = 0.0F;
    }

    private static void spawnJetpack(ClientLevel level, CompoundTag data) {
        if (minimalParticleSetting()) {
            return;
        }
        Entity entity = level.getEntity(data.getInt("player"));
        if (entity == null) {
            return;
        }
        JetpackPose pose = jetpackPose(entity, 0.25D, 0.125D, entity.getEyeHeight() - 1.0D);
        int mode = data.getInt("mode");
        Vec3 thrust = Vec3.ZERO;
        if (mode == 0) {
            thrust = new Vec3(0.0D, -0.2D, 0.0D);
        } else if (mode == 1) {
            thrust = entity.getLookAngle().scale(-0.1D);
        }
        Vec3 motion2 = clamp(entity.getDeltaMovement().add(thrust.scale(2.0D)), 5.0D);
        Vec3 motion3 = clamp(entity.getDeltaMovement().add(thrust.scale(2.0D)), 10.0D);
        spawnDual(level, ParticleTypes.FLAME, pose, motion2);
        if (allParticleSetting()) {
            spawnGroundKick(level, pose.center(), thrust.normalize());
            spawnDual(level, ParticleTypes.SMOKE, pose, motion3);
        }
    }

    private static void spawnBnuuy(ClientLevel level, CompoundTag data) {
        if (minimalParticleSetting()) {
            return;
        }
        Entity entity = level.getEntity(data.getInt("player"));
        if (entity == null) {
            return;
        }
        JetpackPose pose = jetpackPose(entity, 0.6D, 0.275D, entity.getEyeHeight() - 0.6D);
        Vec3 backward = pose.backward().normalize().scale(0.025D);
        spawnDualScaled(level, ParticleTypes.SMOKE, pose, new Vec3(backward.x(), 0.0D, backward.z()), 0.5F);
    }

    private static void spawnColoredJetpack(ClientLevel level, CompoundTag data, boolean dns) {
        if (minimalParticleSetting()) {
            return;
        }
        Entity entity = level.getEntity(data.getInt("player"));
        if (entity == null) {
            return;
        }
        JetpackPose pose = dns
                ? jetpackPose(entity, 0.0D, 0.125D, -0.5D)
                : jetpackPose(entity, 0.3125D, 0.125D, entity.getEyeHeight() - 0.9375D);
        if (allParticleSetting()) {
            spawnGroundKick(level, pose.center(), new Vec3(0.0D, -1.0D, 0.0D));
        }
        Vec3 motion = entity.getDeltaMovement();
        ParticleOptions dust = dns
                ? new DustParticleOptions(new Vector3f(0.01F, 1.0F, 1.0F), 1.0F)
                : new DustParticleOptions(new Vector3f(0.8F, 0.5F, 1.0F), 1.0F);
        spawnDual(level, dust, pose, motion);
    }

    private static void spawnRadiationAura(ClientLevel level, CompoundTag data) {
        Entity player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        int count = Math.max(1, data.getInt("count"));
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            addLegacyAura(level,
                    player.getX() + random.nextGaussian() * 4.0D,
                    player.getY() + random.nextGaussian() * 2.0D,
                    player.getZ() + random.nextGaussian() * 4.0D,
                    random.nextGaussian(), random.nextGaussian(), random.nextGaussian(),
                    0.0F, 0.75F, 1.0F);
        }
    }

    private static void addLegacyAura(ClientLevel level, double x, double y, double z,
            double motionX, double motionY, double motionZ, float red, float green, float blue) {
        Particle particle = LegacyAuraParticle.create(level, x, y, z, motionX, motionY, motionZ, red, green, blue);
        if (particle != null) {
            Minecraft.getInstance().particleEngine.add(particle);
        } else {
            level.addParticle(ModParticleTypes.TOWN_AURA.get(), x, y, z, motionX, motionY, motionZ);
        }
    }

    private static JetpackPose jetpackPose(Entity entity, double backwardOffset, double sideOffset, double yOffset) {
        double yaw = Math.toRadians(entity.getYRot());
        Vec3 backward = new Vec3(Math.sin(yaw) * backwardOffset, 0.0D, -Math.cos(yaw) * backwardOffset);
        Vec3 side = new Vec3(Math.cos(yaw) * sideOffset, 0.0D, Math.sin(yaw) * sideOffset);
        Vec3 center = new Vec3(entity.getX(), entity.getY() + yOffset, entity.getZ()).add(backward);
        return new JetpackPose(center, side, backward);
    }

    private static void spawnDual(ClientLevel level, ParticleOptions particle, JetpackPose pose, Vec3 motion) {
        Vec3 left = pose.center().add(pose.side());
        Vec3 right = pose.center().subtract(pose.side());
        level.addParticle(particle, left.x(), left.y(), left.z(), motion.x(), motion.y(), motion.z());
        level.addParticle(particle, right.x(), right.y(), right.z(), motion.x(), motion.y(), motion.z());
    }

    private static void spawnDualScaled(ClientLevel level, ParticleOptions particle, JetpackPose pose, Vec3 motion, float scale) {
        Vec3 left = pose.center().add(pose.side());
        Vec3 right = pose.center().subtract(pose.side());
        spawnScaled(level, particle, left, motion, scale);
        spawnScaled(level, particle, right, motion, scale);
    }

    private static void spawnScaled(ClientLevel level, ParticleOptions particle, Vec3 position, Vec3 motion, float scale) {
        Particle created = Minecraft.getInstance().particleEngine.createParticle(particle,
                position.x(), position.y(), position.z(),
                motion.x(), motion.y(), motion.z());
        if (created != null && scale != 1.0F) {
            created.scale(scale);
        }
    }

    private static void spawnGroundKick(ClientLevel level, Vec3 origin, Vec3 thrust) {
        if (thrust.lengthSqr() < 1.0E-6D) {
            return;
        }
        Vec3 target = origin.add(thrust.normalize().scale(10.0D));
        BlockHitResult hit = level.clip(new ClipContext(origin, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, Minecraft.getInstance().player));
        if (hit.getType() != HitResult.Type.BLOCK || hit.getDirection() != Direction.UP) {
            return;
        }
        BlockState state = level.getBlockState(hit.getBlockPos());
        Vec3 delta = origin.subtract(hit.getLocation());
        int count = Math.max(0, (int) (10.0D - delta.length()));
        for (int i = 0; i < count; i++) {
            double theta = level.random.nextDouble() * Math.PI * 2.0D;
            double speed = 0.75D - delta.length() * 0.075D;
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    hit.getLocation().x(), hit.getLocation().y() + 0.1D, hit.getLocation().z(),
                    Math.cos(theta) * speed, 0.1D, Math.sin(theta) * speed);
        }
    }

    private static Vec3 clamp(Vec3 vec, double max) {
        return new Vec3(Mth.clamp(vec.x(), -max, max), Mth.clamp(vec.y(), -max, max), Mth.clamp(vec.z(), -max, max));
    }

    private record JetpackPose(Vec3 center, Vec3 side, Vec3 backward) {
    }

    private static void spawnVanillaBurst(ClientLevel level, CompoundTag data, double x, double y, double z) {
        double motion = data.getDouble("motion");
        int count = Math.max(1, data.getInt("count"));
        String mode = data.getString("mode");
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            double motionX = random.nextGaussian() * motion;
            double motionY = random.nextGaussian() * motion;
            double motionZ = random.nextGaussian() * motion;
            if ("blockdust".equals(mode)) {
                Particle particle = new TerrainParticle(level, x, y, z, motionX, motionY + 0.2D, motionZ,
                        LegacyBlockStateMappings.fromParticleData(data));
                addWithLifetime(particle, 50 + random.nextInt(50));
            } else {
                spawnNamedVanilla(level, mode, x, y, z, motionX, motionY, motionZ);
            }
        }
    }

    private static void spawnVanillaExt(ClientLevel level, CompoundTag data, double x, double y, double z) {
        String mode = data.getString("mode");
        double motionX = data.getDouble("mX");
        double motionY = data.getDouble("mY");
        double motionZ = data.getDouble("mZ");
        if ("volcano".equals(mode)) {
            addExSmoke(level, x, y, z,
                    level.random.nextGaussian() * 0.2D,
                    2.5D + level.random.nextDouble(),
                    level.random.nextGaussian() * 0.2D,
                    100.0F,
                    200 + level.random.nextInt(50),
                    0.35F,
                    0.35F,
                    0.35F);
            return;
        }
        if ("largeexplode".equals(mode)) {
            int count = Math.max(0, data.getByte("count"));
            Particle primary = LargeExplodeParticle.primary(level, x, y, z, data.getFloat("size"));
            if (primary != null) {
                Minecraft.getInstance().particleEngine.add(primary);
            }
            for (int i = 0; i < count; i++) {
                Particle secondary = LargeExplodeParticle.secondary(level, x, y, z, i + 1.0F);
                if (secondary != null) {
                    Minecraft.getInstance().particleEngine.add(secondary);
                }
            }
            return;
        }
        if ("townaura".equals(mode)) {
            addTownAuraWithVelocity(level, x, y, z, motionX, motionY, motionZ);
            return;
        }
        Particle particle = createVanillaExtParticle(level, data, x, y, z, motionX, motionY, motionZ);
        if (particle != null) {
            if (data.getInt("overrideAge") > 0) {
                particle.setLifetime(data.getInt("overrideAge"));
            }
            Minecraft.getInstance().particleEngine.add(particle);
        } else {
            spawnNamedVanilla(level, mode, x, y, z, motionX, motionY, motionZ);
        }
    }

    private static void spawnSmoke(ClientLevel level, CompoundTag data, double x, double y, double z) {
        String mode = data.getString("mode");
        int count = Math.max(1, data.getInt("count"));
        RandomSource random = level.random;
        if ("cloud".equals(mode) || "radial".equals(mode)) {
            double ySpread = "cloud".equals(mode) ? 1.0D + count / 100 : 1.0D + count / 50;
            double xzSpread = "cloud".equals(mode) ? 1.0D + count / 150 : 1.0D + count / 50;
            for (int i = 0; i < count; i++) {
                double motionY = random.nextGaussian() * ySpread;
                if ("cloud".equals(mode) && random.nextBoolean()) {
                    motionY = Math.abs(motionY);
                }
                level.addParticle(ModParticleTypes.EX_SMOKE.get(), x, y, z,
                        random.nextGaussian() * xzSpread, motionY, random.nextGaussian() * xzSpread);
            }
        } else if ("radialDigamma".equals(mode)) {
            spawnRadialDigamma(level, x, y, z, count);
        } else if ("shock".equals(mode)) {
            spawnRadial(level, ModParticleTypes.EX_SMOKE.get(), x, y, z, count, Math.max(0.1D, data.getDouble("strength")));
        } else if ("shockRand".equals(mode)) {
            spawnRadialRandom(level, ModParticleTypes.EX_SMOKE.get(), x, y, z, count, Math.max(0.1D, data.getDouble("strength")));
        } else if ("wave".equals(mode)) {
            spawnRing(level, ModParticleTypes.EX_SMOKE.get(), x, y, z, count, Math.max(0.1D, data.getDouble("range")), 50);
        } else if ("foamSplash".equals(mode)) {
            spawnRing(level, ModParticleTypes.FOAM.get(), x, y, z, count, Math.max(0.1D, data.getDouble("range")), 50);
        }
    }

    private static void spawnContrail(ClientLevel level, CompoundTag data, double x, double y, double z, String type) {
        double motionX = data.contains("moX") ? data.getDouble("moX") : 0.0D;
        double motionY = data.contains("moY") ? data.getDouble("moY") : 0.0D;
        double motionZ = data.contains("moZ") ? data.getDouble("moZ") : 0.0D;
        if (ParticleUtil.TYPE_MISSILE_CONTRAIL.equals(type)) {
            spawnMissileContrail(level, data, x, y, z, motionX, motionY, motionZ);
            return;
        }
        if (!spawnLegacyContrail(level, x, y, z, 0.0D, 0.0D, 0.0D, type)) {
            level.addParticle(ModParticleTypes.CONTRAIL.get(), x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void spawnMissileContrail(ClientLevel level, CompoundTag data, double x, double y, double z,
            double motionX, double motionY, double motionZ) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.position().distanceTo(new Vec3(x, y, z)) > 350.0D) {
            return;
        }
        float scale = data.contains("scale") ? data.getFloat("scale") : 1.0F;
        int lifetime = data.contains("maxAge") ? Math.max(1, data.getInt("maxAge")) : 300 + level.random.nextInt(50);
        RocketFlameParticle particle = RocketFlameParticle.createLegacy(level, x, y, z, motionX, motionY, motionZ, scale, lifetime);
        if (particle != null) {
            Minecraft.getInstance().particleEngine.add(particle);
        } else {
            level.addParticle(ModParticleTypes.ROCKET_FLAME.get(), x, y, z, motionX, motionY, motionZ);
        }
    }

    private static boolean spawnLegacyContrail(ClientLevel level, double x, double y, double z,
            double motionX, double motionY, double motionZ, String type) {
        float red = 0.0F;
        float green = 0.0F;
        float blue = 0.0F;
        if (ParticleUtil.TYPE_EX_SOLID.equals(type)) {
            red = 0.3F;
            green = 0.2F;
            blue = 0.05F;
        } else if (ParticleUtil.TYPE_EX_HYDROGEN.equals(type)) {
            red = 0.7F;
            green = 0.7F;
            blue = 0.7F;
        } else if (ParticleUtil.TYPE_EX_BALEFIRE.equals(type)) {
            red = 0.2F;
            green = 0.7F;
            blue = 0.2F;
        }
        LegacyContrailParticle particle = LegacyContrailParticle.create(level, x, y, z, motionX, motionY, motionZ, red, green, blue, 1.0F);
        if (particle == null) {
            return false;
        }
        Minecraft.getInstance().particleEngine.add(particle);
        return true;
    }

    private static void spawnWaterSplash(ClientLevel level, double x, double y, double z) {
        RandomSource random = level.random;
        for (int i = 0; i < 10; i++) {
            level.addParticle(ParticleTypes.CLOUD, x + random.nextGaussian(), y + random.nextGaussian(), z + random.nextGaussian(),
                    0.0D, 0.0D, 0.0D);
        }
    }

    private static void spawnExhaust(ClientLevel level, CompoundTag data, double x, double y, double z) {
        String mode = data.getString("mode");
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.position().distanceTo(new Vec3(x, y, z)) > 350.0D) {
            return;
        }
        int count = Math.max(1, data.getInt("count"));
        double width = data.getDouble("width");
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            if (ParticleUtil.EXHAUST_SOYUZ.equals(mode)) {
                level.addParticle(ModParticleTypes.ROCKET_FLAME.get(),
                        x + random.nextGaussian() * width, y, z + random.nextGaussian() * width,
                        0.0D, -0.75D + random.nextDouble() * 0.5D, 0.0D);
            } else if (ParticleUtil.EXHAUST_METEOR.equals(mode)) {
                level.addParticle(ModParticleTypes.ROCKET_FLAME.get(),
                        x + random.nextGaussian() * width,
                        y + random.nextGaussian() * width,
                        z + random.nextGaussian() * width,
                        0.0D, 0.0D, 0.0D);
            }
        }
    }

    private static void spawnFlamethrower(ClientLevel level, CompoundTag data, double x, double y, double z) {
        int meta = data.getInt("meta");
        ParticleOptions particle = switch (meta) {
            case FlamethrowerParticle.META_BALEFIRE -> ModParticleTypes.FLAMETHROWER_BALEFIRE.get();
            case FlamethrowerParticle.META_DIGAMMA -> ModParticleTypes.FLAMETHROWER_DIGAMMA.get();
            case FlamethrowerParticle.META_OXY -> ModParticleTypes.FLAMETHROWER_OXY.get();
            case FlamethrowerParticle.META_BLACK -> ModParticleTypes.FLAMETHROWER_BLACK.get();
            default -> ModParticleTypes.FLAMETHROWER.get();
        };
        level.addParticle(particle, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    private static void spawnEntitySweat(ClientLevel level, CompoundTag data) {
        Entity entity = level.getEntity(data.getInt("entity"));
        if (entity == null) {
            return;
        }
        BlockState state = LegacyBlockStateMappings.fromParticleData(data);
        int count = Math.max(1, data.getInt("count"));
        for (int i = 0; i < count; i++) {
            double x = entity.getBoundingBox().minX - 0.2D + (entity.getBoundingBox().getXsize() + 0.4D) * level.random.nextDouble();
            double y = entity.getBoundingBox().minY + (entity.getBoundingBox().getYsize() + 0.2D) * level.random.nextDouble();
            double z = entity.getBoundingBox().minZ - 0.2D + (entity.getBoundingBox().getZsize() + 0.4D) * level.random.nextDouble();
            Particle particle = new TerrainParticle(level, x, y, z, 0.0D, 0.0D, 0.0D, state);
            addWithLifetime(particle, 150 + level.random.nextInt(50));
        }
    }

    private static void spawnEntityVomit(ClientLevel level, CompoundTag data) {
        Entity entity = level.getEntity(data.getInt("entity"));
        if (entity == null) {
            return;
        }
        int count = Math.max(1, data.getInt("count")) / (particleSettingDivisor());
        String mode = data.getString("mode");
        if (count <= 0) {
            return;
        }

        Vec3 look = entity.getLookAngle();
        double x = entity.getX();
        double y = entity.getY() - entity.getMyRidingOffset() + entity.getEyeHeight() + (entity instanceof Player ? 1.0D : 0.0D);
        double z = entity.getZ();
        for (int i = 0; i < count; i++) {
            double randomX = level.random.nextGaussian();
            double randomY = level.random.nextGaussian();
            double randomZ = level.random.nextGaussian();

            if (ParticleUtil.VOMIT_SMOKE.equals(mode)) {
                if (HbmSmokeParticle.exSmokeSprites() != null) {
                    double motionX = (look.x + randomX * 0.1D) * 0.05D;
                    double motionY = (look.y + randomY * 0.1D) * 0.05D;
                    double motionZ = (look.z + randomZ * 0.1D) * 0.05D;
                    Particle particle = new HbmSmokeParticle(level, x, y, z,
                            motionX, motionY, motionZ,
                            HbmSmokeParticle.exSmokeSprites(), 0.2F, 10 + level.random.nextInt(10));
                    particle.setParticleSpeed(motionX, motionY, motionZ);
                    Minecraft.getInstance().particleEngine.add(particle);
                }
            } else {
                BlockState state = ParticleUtil.VOMIT_BLOOD.equals(mode)
                        ? Blocks.REDSTONE_BLOCK.defaultBlockState()
                        : (level.random.nextBoolean() ? Blocks.LIME_TERRACOTTA : Blocks.GREEN_TERRACOTTA).defaultBlockState();
                double motionX = (look.x + randomX * 0.2D) * 0.2D;
                double motionY = (look.y + randomY * 0.2D) * 0.2D;
                double motionZ = (look.z + randomZ * 0.2D) * 0.2D;
                Particle particle = new TerrainParticle(level, x, y, z,
                        motionX, motionY, motionZ,
                        state);
                particle.setParticleSpeed(motionX, motionY, motionZ);
                addWithLifetime(particle, 150 + level.random.nextInt(50));
            }
        }
    }

    private static int particleSettingDivisor() {
        ParticleStatus status = Minecraft.getInstance().options.particles().get();
        return switch (status) {
            case DECREASED -> 2;
            case MINIMAL -> 3;
            default -> 1;
        };
    }

    private static boolean minimalParticleSetting() {
        return Minecraft.getInstance().options.particles().get() == ParticleStatus.MINIMAL;
    }

    private static boolean allParticleSetting() {
        return Minecraft.getInstance().options.particles().get() == ParticleStatus.ALL;
    }

    private static boolean legacyVisibleParticleSetting(ClientLevel level) {
        ParticleStatus status = Minecraft.getInstance().options.particles().get();
        if (status == ParticleStatus.MINIMAL) {
            return false;
        }
        return status == ParticleStatus.ALL || level.random.nextBoolean();
    }

    private static void addWithLifetime(Particle particle, int lifetime) {
        if (particle == null) {
            return;
        }
        particle.setLifetime(lifetime);
        Minecraft.getInstance().particleEngine.add(particle);
    }

    private static void setParticleColor(Particle particle, int color) {
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        particle.setColor(red, green, blue);
    }

    private static void spawnWeaponExplosion(ClientLevel level, CompoundTag data, double x, double y, double z) {
        int count = Math.max(1, data.getInt("count"));
        double scale = Math.max(0.25D, data.getFloat("scale"));
        double speed = Math.max(0.05D, data.getFloat("speed"));
        level.addParticle(ParticleTypes.EXPLOSION, x, y, z, 0.0D, 0.0D, 0.0D);
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            double mx = random.nextGaussian() * 0.15D * speed;
            double my = Math.abs(random.nextGaussian()) * 0.08D * speed;
            double mz = random.nextGaussian() * 0.15D * speed;
            level.addParticle(ParticleTypes.LARGE_SMOKE,
                    x + random.nextGaussian() * scale * 0.2D,
                    y + random.nextDouble() * scale * 0.25D,
                    z + random.nextGaussian() * scale * 0.2D,
                    mx, my, mz);
            if (i % 2 == 0) {
                level.addParticle(ParticleTypes.FLAME, x, y, z, mx * 0.5D, my * 0.5D, mz * 0.5D);
            }
        }
    }

    private static void spawnAmat(ClientLevel level, CompoundTag data, double x, double y, double z) {
        Minecraft.getInstance().particleEngine.add(new AmatFlashParticle(level, x, y, z, Math.max(0.1F, data.getFloat("scale"))));
    }

    private static void spawnTau(ClientLevel level, CompoundTag data, double x, double y, double z) {
        boolean small = data.getBoolean("small");
        int count = data.contains("count") ? Byte.toUnsignedInt(data.getByte("count")) : 1;
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            Minecraft.getInstance().particleEngine.add(new TauSparkParticle(level, x, y, z,
                    random.nextGaussian() * 0.05D, 0.05D, random.nextGaussian() * 0.05D, small));
        }
        spawnHadron(level, x, y, z, small);
    }

    private static void spawnHadron(ClientLevel level, double x, double y, double z, boolean small) {
        Particle particle = HadronParticle.create(level, x, y, z, small);
        if (particle != null) {
            Minecraft.getInstance().particleEngine.add(particle);
        }
    }

    private static void spawnRbmkFlame(ClientLevel level, CompoundTag data, double x, double y, double z) {
        Particle particle = RbmkAnimatedParticle.flame(level, x, y, z, Math.max(1, getInt(data, "maxAge", 40)));
        if (particle != null) {
            Minecraft.getInstance().particleEngine.add(particle);
        }
    }

    private static void spawnRbmkSteam(ClientLevel level, double x, double y, double z) {
        Particle particle = RbmkAnimatedParticle.steam(level, x, y, z);
        if (particle != null) {
            Minecraft.getInstance().particleEngine.add(particle);
        }
    }

    private static void spawnRbmkMush(ClientLevel level, CompoundTag data, double x, double y, double z) {
        Particle particle = RbmkAnimatedParticle.mush(level, x, y, z, Math.max(0.1F, getFloat(data, "scale", 1.0F)));
        if (particle != null) {
            Minecraft.getInstance().particleEngine.add(particle);
        }
    }

    private static void spawnCoolingTower(ClientLevel level, CompoundTag data, double x, double y, double z) {
        if (!legacyVisibleParticleSetting(level)) {
            return;
        }
        int lifetime = Math.max(1, getInt(data, "life", 80) / particleSettingDivisor());
        Particle particle = CoolingTowerParticle.create(level, x, y, z,
                getFloat(data, "lift", 0.3F),
                getFloat(data, "base", 1.0F),
                getFloat(data, "max", 1.0F),
                lifetime,
                !data.contains("noWind"),
                getFloat(data, "strafe", 0.075F),
                getFloat(data, "alpha", 0.25F),
                data.contains("color") ? data.getInt("color") : -1);
        if (particle != null) {
            Minecraft.getInstance().particleEngine.add(particle);
        }
    }

    private static void spawnSplash(ClientLevel level, CompoundTag data, double x, double y, double z) {
        if (!legacyVisibleParticleSetting(level)) {
            return;
        }
        Particle particle = LegacySplashParticle.create(level, x, y, z, data.contains("color") ? data.getInt("color") : -1);
        if (particle != null) {
            Minecraft.getInstance().particleEngine.add(particle);
        }
    }

    private static void spawnRift(ClientLevel level, double x, double y, double z) {
        Minecraft.getInstance().particleEngine.add(new RiftParticle(level, x, y, z));
    }

    private static void spawnFluidFill(ClientLevel level, CompoundTag data, double x, double y, double z) {
        Particle particle = FluidFillParticle.create(level, x, y, z,
                data.getDouble("mX"), data.getDouble("mY"), data.getDouble("mZ"),
                data.contains("color") ? data.getInt("color") : -1);
        if (particle != null) {
            Minecraft.getInstance().particleEngine.add(particle);
        }
    }

    private static void spawnFoundry(ClientLevel level, CompoundTag data, double x, double y, double z) {
        Minecraft.getInstance().particleEngine.add(new FoundryParticle(level, x, y, z,
                data.getInt("color"),
                data.getByte("dir"),
                getFloat(data, "len", 1.0F),
                getFloat(data, "base", 0.0F),
                getFloat(data, "off", 0.0F)));
    }

    private static void spawnMuke(ClientLevel level, CompoundTag data, double x, double y, double z, boolean tiny) {
        boolean balefire = data.getBoolean("balefire");

        Particle wave = MukeWaveParticle.create(level, x, y, z, 45.0F, 25);
        if (wave != null) {
            Minecraft.getInstance().particleEngine.add(wave);
            HbmRenderEffects.spawnNuclearWarpShockwave(x, y, z, 45.0F, 25);
        }
        if (!tiny) {
            Minecraft.getInstance().particleEngine.add(new MukeFlashParticle(level, x, y, z, balefire));
            applyClientJolt(15, 15);
            return;
        }

        RandomSource random = level.random;
        for (double d = 0.0D; d <= 1.6D + 1.0E-9D; d += 0.1D) {
            spawnMukeCloud(level, x, y, z,
                    random.nextGaussian() * 0.05D,
                    d + random.nextGaussian() * 0.02D,
                    random.nextGaussian() * 0.05D,
                    false);
        }
        for (int i = 0; i < 50; i++) {
            spawnMukeCloud(level, x, y + 0.5D, z,
                    random.nextGaussian() * 0.5D,
                    random.nextInt(5) == 0 ? 0.02D : 0.0D,
                    random.nextGaussian() * 0.5D,
                    false);
        }
        for (int i = 0; i < 15; i++) {
            double motionX = random.nextGaussian() * 0.2D;
            double motionZ = random.nextGaussian() * 0.2D;
            if (motionX * motionX + motionZ * motionZ > 0.75D) {
                motionX *= 0.5D;
                motionZ *= 0.5D;
            }
            double motionY = 1.6D + (random.nextDouble() * 2.0D - 1.0D) * (0.75D - (motionX * motionX + motionZ * motionZ)) * 0.5D;
            spawnMukeCloud(level, x, y, z, motionX, motionY + random.nextGaussian() * 0.02D, motionZ, false);
        }
        applyClientJolt(15, 15);
    }

    private static void spawnMukeCloud(ClientLevel level, double x, double y, double z,
            double motionX, double motionY, double motionZ, boolean balefire) {
        MukeCloudParticle.add(level, x, y, z, motionX, motionY, motionZ, balefire);
    }

    private static void spawnUfoCloud(ClientLevel level, CompoundTag data, double x, double y, double z) {
        double motion = data.getDouble("motion");
        RandomSource random = level.random;
        spawnMukeCloud(level, x, y, z, random.nextGaussian() * motion, 0.0D, random.nextGaussian() * motion, false);
    }

    private static void spawnChaosCloud(ClientLevel level, CompoundTag data, double x, double y, double z) {
        ParticleOptions particle = switch (data.getString("mode")) {
            case "green" -> new DustParticleOptions(new Vector3f(0.35F, 0.9F, 0.15F), 1.3F);
            case "pink" -> new DustParticleOptions(new Vector3f(0.95F, 0.25F, 0.85F), 1.3F);
            default -> new DustParticleOptions(new Vector3f(1.0F, 0.45F, 0.05F), 1.3F);
        };
        level.addParticle(particle, x, y, z, data.getDouble("mX"), data.getDouble("mY"), data.getDouble("mZ"));
        level.addParticle(ParticleTypes.CLOUD, x, y, z, data.getDouble("mX") * 0.35D, data.getDouble("mY") * 0.35D, data.getDouble("mZ") * 0.35D);
    }

    private static void spawnExplosionLarge(ClientLevel level, CompoundTag data, double x, double y, double z) {
        RandomSource random = level.random;
        int cloudCount = Math.max(1, getInt(data, "cloudCount", 15));
        int debrisCount = Math.max(0, getInt(data, "debrisCount", 10));
        float cloudScale = Math.max(0.25F, getFloat(data, "cloudScale", 5.0F));
        float cloudSpeedMult = Math.max(0.1F, getFloat(data, "cloudSpeedMult", 1.0F));
        float waveScale = Math.max(4.0F, getFloat(data, "waveScale", 45.0F));
        float soundRange = Math.max(1.0F, getFloat(data, "soundRange", 200.0F));

        HbmDelayedSounds.playExplosionLarge(x, y, z, soundRange);
        level.addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 0.0D, 0.0D, 0.0D);
        Particle wave = MukeWaveParticle.create(level, x, y + 2.0D, z, waveScale, Math.max(1, (int) (25.0F * waveScale / 45.0F)));
        if (wave != null) {
            Minecraft.getInstance().particleEngine.add(wave);
            HbmRenderEffects.spawnNuclearWarpShockwave(x, y + 2.0D, z, waveScale, Math.max(1, (int) (25.0F * waveScale / 45.0F)));
        }

        for (int i = 0; i < cloudCount; i++) {
            double motionX = random.nextGaussian() * 0.5D * cloudSpeedMult;
            double motionY = random.nextDouble() * 3.0D * cloudSpeedMult;
            double motionZ = random.nextGaussian() * 0.5D * cloudSpeedMult;
            Particle particle = RocketFlameParticle.createLegacy(level, x, y, z,
                    motionX, motionY, motionZ, cloudScale, 70 + random.nextInt(20));
            if (particle != null) {
                Minecraft.getInstance().particleEngine.add(particle);
            } else {
                level.addParticle(ModParticleTypes.ROCKET_FLAME.get(), x, y, z, motionX, motionY, motionZ);
            }
        }

        BlockState debrisState = nearbyBlockState(level, x, y, z);
        for (int i = 0; i < debrisCount; i++) {
            double motionX = random.nextGaussian() * 0.25D;
            double motionY = 0.35D + random.nextDouble() * 0.75D;
            double motionZ = random.nextGaussian() * 0.25D;
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, debrisState),
                    x + random.nextGaussian() * 1.5D,
                    y + random.nextDouble() * 0.8D,
                    z + random.nextGaussian() * 1.5D,
                    motionX, motionY, motionZ);
        }
    }

    private static void spawnExplosionSmall(ClientLevel level, CompoundTag data, double x, double y, double z) {
        RandomSource random = level.random;
        int cloudCount = Math.max(1, getInt(data, "cloudCount", 10));
        int debris = Math.max(0, getInt(data, "debris", 15));
        float cloudScale = Math.max(0.25F, getFloat(data, "cloudScale", 2.0F));
        float cloudSpeedMult = Math.max(0.1F, getFloat(data, "cloudSpeedMult", 0.5F));

        HbmDelayedSounds.playExplosionSmall(x, y, z);
        level.addParticle(ParticleTypes.EXPLOSION, x, y, z, 0.0D, 0.0D, 0.0D);
        for (int i = 0; i < cloudCount; i++) {
            Particle particle = ExplosionSmallParticle.create(level, x, y, z, cloudScale, cloudSpeedMult);
            if (particle != null) {
                Minecraft.getInstance().particleEngine.add(particle);
            }
        }

        BlockState debrisState = nearbyBlockState(level, x, y, z);
        for (int i = 0; i < debris; i++) {
            Particle particle = new TerrainParticle(level, x, y + 0.1D, z,
                    random.nextGaussian() * 0.2D,
                    0.5D + random.nextDouble() * 0.7D,
                    random.nextGaussian() * 0.2D,
                    debrisState);
            particle.scale(2.0F);
            addWithLifetime(particle, 50 + random.nextInt(20));
        }
    }

    private static void spawnVntExplosion(ClientLevel level, CompoundTag data, double x, double y, double z) {
        RandomSource random = level.random;
        float size = Math.max(0.0F, getFloat(data, "size", 0.0F));
        if (size >= 2.0F) {
            level.addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1.0D, 0.0D, 0.0D);
        } else {
            level.addParticle(ParticleTypes.EXPLOSION, x, y, z, 1.0D, 0.0D, 0.0D);
        }

        long[] blocks = data.getLongArray("blocks");
        for (long packed : blocks) {
            BlockPos pos = BlockPos.of(packed);
            double originX = pos.getX() + random.nextFloat();
            double originY = pos.getY() + random.nextFloat();
            double originZ = pos.getZ() + random.nextFloat();
            double motionX = originX - x;
            double motionY = originY - y;
            double motionZ = originZ - z;
            double distance = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            if (distance < 1.0E-5D) {
                distance = 1.0D;
            }
            motionX /= distance;
            motionY /= distance;
            motionZ /= distance;
            double modifier = 0.5D / (distance / Math.max(size, 0.1D) + 0.1D);
            modifier *= random.nextFloat() * random.nextFloat() + 0.3F;
            motionX *= modifier;
            motionY *= modifier;
            motionZ *= modifier;

            level.addParticle(ParticleTypes.POOF, (originX + x) * 0.5D, (originY + y) * 0.5D, (originZ + z) * 0.5D,
                    motionX, motionY, motionZ);
            level.addParticle(ParticleTypes.SMOKE, originX, originY, originZ, motionX, motionY, motionZ);
        }
    }

    private static void spawnBlackPowder(ClientLevel level, CompoundTag data, double x, double y, double z) {
        RandomSource random = level.random;
        double headingX = data.getDouble("hX");
        double headingY = data.getDouble("hY");
        double headingZ = data.getDouble("hZ");
        double length = Math.sqrt(headingX * headingX + headingY * headingY + headingZ * headingZ);
        if (length < 1.0E-5D) {
            headingY = 1.0D;
            length = 1.0D;
        }
        headingX /= length;
        headingY /= length;
        headingZ /= length;

        int cloudCount = Math.max(0, getInt(data, "cloudCount", 8));
        int sparkCount = Math.max(0, getInt(data, "sparkCount", 12));
        float cloudSpeedMult = Math.max(0.0F, getFloat(data, "cloudSpeedMult", 0.35F));
        float sparkSpeedMult = Math.max(0.0F, getFloat(data, "sparkSpeedMult", 0.5F));

        for (int i = 0; i < cloudCount; i++) {
            double speed = 0.85D + random.nextDouble() * 0.3D;
            Particle particle = BlackPowderSmokeParticle.create(level, x, y, z,
                    headingX * cloudSpeedMult * speed + random.nextGaussian() * 0.05D,
                    headingY * cloudSpeedMult * speed + random.nextGaussian() * 0.05D,
                    headingZ * cloudSpeedMult * speed + random.nextGaussian() * 0.05D,
                    getFloat(data, "cloudScale", 1.0F));
            if (particle != null) {
                Minecraft.getInstance().particleEngine.add(particle);
            }
        }
        for (int i = 0; i < sparkCount; i++) {
            double speed = 0.85D + random.nextDouble() * 0.3D;
            level.addParticle(ModParticleTypes.BLACK_POWDER_SPARK.get(), x, y, z,
                    headingX * sparkSpeedMult * speed + random.nextGaussian() * 0.02D,
                    headingY * sparkSpeedMult * speed + random.nextGaussian() * 0.02D,
                    headingZ * sparkSpeedMult * speed + random.nextGaussian() * 0.02D);
        }
    }

    private static void spawnAshes(ClientLevel level, CompoundTag data, double x, double y, double z) {
        Entity entity = level.getEntity(data.getInt("entityID"));
        if (entity != null) {
            ClientForgeEvents.vanishEntity(entity.getId());
        }
        double centerX = entity == null ? x : entity.getX();
        double centerY = entity == null ? y : entity.getY();
        double centerZ = entity == null ? z : entity.getZ();
        double width = entity == null ? 0.6D : entity.getBbWidth();
        double height = entity == null ? 1.8D : entity.getBbHeight();
        int count = Math.max(1, getInt(data, "ashesCount", 16));
        float scale = Math.max(0.1F, getFloat(data, "ashesScale", 1.0F));
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            double px = centerX + (width + scale * 2.0D) * (random.nextDouble() - 0.5D);
            double py = centerY + height * random.nextDouble();
            double pz = centerZ + (width + scale * 2.0D) * (random.nextDouble() - 0.5D);
            Particle particle = AshesParticle.create(level, px, py, pz, scale);
            if (particle != null) {
                Minecraft.getInstance().particleEngine.add(particle);
            }
            level.addParticle(ParticleTypes.FLAME, px, py, pz, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void spawnCasing(ClientLevel level, CompoundTag data, double x, double y, double z) {
        if (ParticleUtil.TYPE_LEGACY_CASING.equals(data.getString("type"))) {
            spawnLegacyCasing(level, data, x, y, z);
            return;
        }
        double motionX = data.getDouble("mX");
        double motionY = data.getDouble("mY");
        double motionZ = data.getDouble("mZ");
        Particle particle = new SpentCasingParticle(level, x, y, z, motionX, motionY, motionZ,
                getFloat(data, "yaw", 0.0F),
                getFloat(data, "pitch", 0.0F),
                getFloat(data, "mPitch", 5.0F),
                getFloat(data, "mYaw", 10.0F),
                data.contains("name") ? data.getString("name") : "default",
                data.getBoolean("smoking"),
                getInt(data, "smokeLife", 0),
                data.contains("smokeLift") ? data.getDouble("smokeLift") : 0.5D,
                getInt(data, "nodeLife", 30));
        Minecraft.getInstance().particleEngine.add(particle);
    }

    private static void spawnLegacyCasing(ClientLevel level, CompoundTag data, double x, double y, double z) {
        LegacyCasingEjectors.LegacyCasingEjector ejector = LegacyCasingEjectors.byId(data.getInt("ej"));
        if (ejector == null) {
            return;
        }
        String name = data.contains("name") ? data.getString("name") : "default";
        float pitch = getFloat(data, "pitch", 0.0F);
        float yaw = getFloat(data, "yaw", 0.0F);
        boolean crouched = data.getBoolean("crouched");
        int amount = Math.max(1, ejector.amount());
        for (int i = 0; i < amount; i++) {
            Vec3 motion = ejector.motion(pitch, yaw, level.random);
            Vec3 offset = ejector.positionOffset(pitch, yaw, crouched);
            Particle particle = new SpentCasingParticle(level,
                    x + offset.x(), y + offset.y(), z + offset.z(),
                    motion.x(), motion.y(), motion.z(),
                    (float) Math.toDegrees(yaw),
                    (float) Math.toDegrees(pitch),
                    (float) (level.random.nextGaussian() * 5.0D),
                    (float) (level.random.nextGaussian() * 10.0D),
                    name, false, 0, 0.0D, 0);
            Minecraft.getInstance().particleEngine.add(particle);
        }
    }

    private static void spawnSkeleton(ClientLevel level, CompoundTag data, double x, double y, double z) {
        Entity entity = level.getEntity(data.getInt("entityID"));
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        ClientForgeEvents.vanishEntity(entity.getId());
        boolean gib = data.getBoolean("gib");
        boolean skeletonEntity = living instanceof AbstractSkeleton || "SkeletonSoldier".equals(living.getClass().getSimpleName());
        float force = Math.max(0.0F, getFloat(data, "force", 0.15F));
        float brightness = Mth.clamp(getFloat(data, "brightness", 1.0F), 0.0F, 1.0F);
        RandomSource random = level.random;
        for (BoneDefinition bone : boneDefinitions(living)) {
            if (gib && random.nextBoolean() && !skeletonEntity) {
                continue;
            }
            SkeletonParticle particle = new SkeletonParticle(level, bone.x(), bone.y(), bone.z(), brightness,
                    bone.type(), bone.yaw(), bone.pitch());
            if (gib) {
                particle.makeGib(skeletonEntity);
                particle.setParticleSpeed(random.nextGaussian() * force, (random.nextGaussian() + 1.0D) * force, random.nextGaussian() * force);
            }
            Minecraft.getInstance().particleEngine.add(particle);
        }
    }

    private static void spawnGiblets(ClientLevel level, CompoundTag data, double x, double y, double z) {
        Entity entity = level.getEntity(data.getInt("ent"));
        if (entity == null) {
            return;
        }
        ClientForgeEvents.vanishEntity(entity.getId());
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        int gridWidth = (int) (width / 0.25F);
        int gridHeight = (int) (height / 0.25F);
        int count = (int) (gridWidth * 1.5D * gridHeight);
        if (data.contains("cDiv")) {
            count = (int) Math.ceil(count / (double) Math.max(1, data.getInt("cDiv")));
        }
        if (count <= 0) {
            return;
        }
        int gibType = Mth.clamp(data.getInt("gibType"), ParticleUtil.GIBLET_MEAT, ParticleUtil.GIBLET_METAL);
        ParticleOptions particle = switch (gibType) {
            case ParticleUtil.GIBLET_SLIME -> ModParticleTypes.GIBLET_SLIME.get();
            case ParticleUtil.GIBLET_METAL -> ModParticleTypes.GIBLET_METAL.get();
            default -> ModParticleTypes.GIBLET_MEAT.get();
        };
        RandomSource random = level.random;
        double multiplier = random.nextInt(15) == 0 ? 10.0D : 1.0D;
        for (int i = 0; i < count; i++) {
            level.addParticle(particle, x, y, z,
                    random.nextGaussian() * 0.25D * multiplier,
                    random.nextDouble() * multiplier,
                    random.nextGaussian() * 0.25D * multiplier);
        }
    }

    private static BoneDefinition[] boneDefinitions(LivingEntity entity) {
        if (usesVillagerSkeleton(entity)) {
            return villagerBones(entity);
        }
        if (usesZombieSkeleton(entity)) {
            return zombieBones(entity);
        }
        return bipedBones(entity);
    }

    private static BoneDefinition[] bipedBones(LivingEntity entity) {
        float bodyYaw = entity.yBodyRot;
        Vec3 leftArm = rotateLegacyY(0.375D, 0.0D, -bodyYaw);
        Vec3 leftLeg = rotateLegacyY(0.125D, 0.0D, -bodyYaw);
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        return new BoneDefinition[] {
                new BoneDefinition(SkeletonParticle.BoneType.SKULL, -entity.yHeadRot, entity.getXRot(), x, y + 1.75D, z),
                new BoneDefinition(SkeletonParticle.BoneType.TORSO, -bodyYaw, 0.0F, x, y + 1.125D, z),
                new BoneDefinition(SkeletonParticle.BoneType.LIMB, -bodyYaw, 0.0F, x + leftArm.x, y + 1.125D, z + leftArm.z),
                new BoneDefinition(SkeletonParticle.BoneType.LIMB, -bodyYaw, 0.0F, x - leftArm.x, y + 1.125D, z - leftArm.z),
                new BoneDefinition(SkeletonParticle.BoneType.LIMB, -bodyYaw, 0.0F, x + leftLeg.x, y + 0.375D, z + leftLeg.z),
                new BoneDefinition(SkeletonParticle.BoneType.LIMB, -bodyYaw, 0.0F, x - leftLeg.x, y + 0.375D, z - leftLeg.z)
        };
    }

    private static BoneDefinition[] zombieBones(LivingEntity entity) {
        float bodyYaw = entity.yBodyRot;
        Vec3 leftArm = rotateLegacyY(0.375D, 0.0D, -bodyYaw);
        Vec3 forward = rotateLegacyY(0.0D, 0.25D, -bodyYaw);
        Vec3 leftLeg = rotateLegacyY(0.125D, 0.0D, -bodyYaw);
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        return new BoneDefinition[] {
                new BoneDefinition(SkeletonParticle.BoneType.SKULL, -entity.yHeadRot, entity.getXRot(), x, y + 1.75D, z),
                new BoneDefinition(SkeletonParticle.BoneType.TORSO, -bodyYaw, 0.0F, x, y + 1.125D, z),
                new BoneDefinition(SkeletonParticle.BoneType.LIMB, -bodyYaw, -90.0F, x + leftArm.x + forward.x, y + 1.375D, z + leftArm.z + forward.z),
                new BoneDefinition(SkeletonParticle.BoneType.LIMB, -bodyYaw, -90.0F, x - leftArm.x + forward.x, y + 1.375D, z - leftArm.z + forward.z),
                new BoneDefinition(SkeletonParticle.BoneType.LIMB, -bodyYaw, 0.0F, x + leftLeg.x, y + 0.375D, z + leftLeg.z),
                new BoneDefinition(SkeletonParticle.BoneType.LIMB, -bodyYaw, 0.0F, x - leftLeg.x, y + 0.375D, z - leftLeg.z)
        };
    }

    private static BoneDefinition[] villagerBones(LivingEntity entity) {
        float bodyYaw = entity.yBodyRot;
        Vec3 leftArm = rotateLegacyY(0.375D, 0.0D, -bodyYaw);
        Vec3 forward = rotateLegacyY(0.0D, 0.25D, -bodyYaw);
        Vec3 leftLeg = rotateLegacyY(0.125D, 0.0D, -bodyYaw);
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        return new BoneDefinition[] {
                new BoneDefinition(SkeletonParticle.BoneType.SKULL_VILLAGER, -entity.yHeadRot, entity.getXRot(), x, y + 1.6875D, z),
                new BoneDefinition(SkeletonParticle.BoneType.TORSO, -bodyYaw, 0.0F, x, y + 1.0D, z),
                new BoneDefinition(SkeletonParticle.BoneType.LIMB, -bodyYaw, -45.0F, x + leftArm.x + forward.x, y + 1.125D, z + leftArm.z + forward.z),
                new BoneDefinition(SkeletonParticle.BoneType.LIMB, -bodyYaw, -45.0F, x - leftArm.x + forward.x, y + 1.125D, z - leftArm.z + forward.z),
                new BoneDefinition(SkeletonParticle.BoneType.LIMB, -bodyYaw, 0.0F, x + leftLeg.x, y + 0.375D, z + leftLeg.z),
                new BoneDefinition(SkeletonParticle.BoneType.LIMB, -bodyYaw, 0.0F, x - leftLeg.x, y + 0.375D, z - leftLeg.z)
        };
    }

    private static boolean usesZombieSkeleton(LivingEntity entity) {
        String name = entity.getClass().getSimpleName();
        return entity instanceof Zombie || entity instanceof AbstractSkeleton || entity instanceof ZombifiedPiglin
                || "EntityUndeadSoldier".equals(name)
                || "ArmySoldier".equals(name)
                || "PsychoSteve".equals(name)
                || "SkeletonSoldier".equals(name)
                || "ZombieFarmer".equals(name)
                || "ZombieMiner".equals(name)
                || "ZombiePigmanSoldier".equals(name)
                || "ZombieSoldier".equals(name);
    }

    private static boolean usesVillagerSkeleton(LivingEntity entity) {
        return entity instanceof Villager || entity instanceof Witch;
    }

    private static Vec3 rotateLegacyY(double x, double z, double degrees) {
        double radians = degrees / 180.0D * Math.PI;
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(x * cos + z * sin, 0.0D, z * cos - x * sin);
    }

    private static void spawnNamedVanilla(ClientLevel level, String mode, double x, double y, double z, double motionX, double motionY, double motionZ) {
        ParticleOptions particle = switch (mode) {
            case "flame" -> ParticleTypes.FLAME;
            case "smoke" -> ParticleTypes.SMOKE;
            case "cloud" -> ParticleTypes.CLOUD;
            case "townaura" -> ModParticleTypes.TOWN_AURA.get();
            case "reddust" -> DustParticleOptions.REDSTONE;
            case "bluedust" -> new DustParticleOptions(new Vector3f(0.01F, 0.01F, 1.0F), 1.0F);
            case "greendust" -> new DustParticleOptions(new Vector3f(0.01F, 0.5F, 0.1F), 1.0F);
            case "fireworks" -> ParticleTypes.FIREWORK;
            case "blockdust" -> new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState());
            case "colordust" -> DustParticleOptions.REDSTONE;
            default -> ParticleTypes.POOF;
        };
        level.addParticle(particle, x, y, z, motionX, motionY, motionZ);
    }

    private static Particle createVanillaExtParticle(ClientLevel level, CompoundTag data, double x, double y, double z,
            double motionX, double motionY, double motionZ) {
        String mode = data.getString("mode");
        if ("cloud".equals(mode) && data.contains("r")) {
            float rng = level.random.nextFloat() * 0.1F;
            return createExSmoke(level, x, y, z, 0.0D, 0.0D, 0.0D, 7.5F, 100 + level.random.nextInt(40),
                    data.getFloat("r") + rng, data.getFloat("g") + rng, data.getFloat("b") + rng);
        }
        if ("blockdust".equals(mode)) {
            Particle particle = new TerrainParticle(level, x, y, z, motionX, motionY + 0.2D, motionZ,
                    LegacyBlockStateMappings.fromParticleData(data));
            particle.setLifetime(10 + level.random.nextInt(20));
            return particle;
        }
        if ("colordust".equals(mode)) {
            Particle particle = new TerrainParticle(level, x, y, z, motionX, motionY + 0.2D, motionZ,
                    Blocks.WHITE_WOOL.defaultBlockState());
            particle.setColor(data.getFloat("r"), data.getFloat("g"), data.getFloat("b"));
            particle.setLifetime(10 + level.random.nextInt(20));
            return particle;
        }
        return null;
    }

    private static void burstSimple(ClientLevel level, ParticleOptions particle, double x, double y, double z, int count, double motion) {
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            level.addParticle(particle, x + random.nextGaussian(), y + random.nextGaussian(), z + random.nextGaussian(),
                    random.nextGaussian() * motion, random.nextGaussian() * motion, random.nextGaussian() * motion);
        }
    }

    private static Particle addExSmoke(ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ,
            float scale, int lifetime, float red, float green, float blue) {
        Particle particle = createExSmoke(level, x, y, z, motionX, motionY, motionZ, scale, lifetime, red, green, blue);
        if (particle != null) {
            Minecraft.getInstance().particleEngine.add(particle);
            return particle;
        }
        level.addParticle(ModParticleTypes.EX_SMOKE.get(), x, y, z, motionX, motionY, motionZ);
        return null;
    }

    private static Particle createExSmoke(ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ,
            float scale, int lifetime, float red, float green, float blue) {
        if (HbmSmokeParticle.exSmokeSprites() == null) {
            return null;
        }
        HbmSmokeParticle particle = new HbmSmokeParticle(level, x, y, z, motionX, motionY, motionZ,
                HbmSmokeParticle.exSmokeSprites(), scale, lifetime, true);
        particle.setColor(red, green, blue);
        return particle;
    }

    private static void addTownAuraWithVelocity(ClientLevel level, double x, double y, double z,
            double motionX, double motionY, double motionZ) {
        float color = 0.5F + level.random.nextFloat() * 0.5F;
        addLegacyAura(level, x, y, z, motionX, motionY, motionZ, 0.8F * color, 0.9F * color, color);
    }

    private static void spawnRadial(ClientLevel level, ParticleOptions particle, double x, double y, double z, int count, double strength) {
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        for (int i = 0; i < count; i++) {
            double theta = angle + Math.PI * 2.0D * i / count;
            level.addParticle(particle, x, y, z, Math.cos(theta) * strength, 0.0D, Math.sin(theta) * strength);
        }
    }

    private static void spawnRadialRandom(ClientLevel level, ParticleOptions particle, double x, double y, double z, int count, double strength) {
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        for (int i = 0; i < count; i++) {
            double theta = angle + Math.PI * 2.0D * i / count;
            double multiplier = level.random.nextDouble();
            level.addParticle(particle, x, y, z,
                    Math.cos(theta) * strength * multiplier,
                    0.0D,
                    Math.sin(theta) * strength * multiplier);
        }
    }

    private static void spawnRadialDigamma(ClientLevel level, double x, double y, double z, int count) {
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        for (int i = 0; i < count; i++) {
            double theta = angle + Math.PI * 2.0D * i / count;
            level.addParticle(ModParticleTypes.DIGAMMA_SMOKE.get(), x, y, z, Math.cos(theta) * 2.0D, 0.0D, Math.sin(theta) * 2.0D);
        }
    }

    private static void spawnRing(ClientLevel level, ParticleOptions particle, double x, double y, double z, int count, double range, int lifetime) {
        for (int i = 0; i < count; i++) {
            double theta = level.random.nextDouble() * Math.PI * 2.0D;
            if (particle instanceof net.minecraft.core.particles.SimpleParticleType simpleParticle && simpleParticle == ModParticleTypes.EX_SMOKE.get()
                    && HbmSmokeParticle.exSmokeSprites() != null) {
                addExSmoke(level, x + Math.cos(theta) * range, y, z + Math.sin(theta) * range,
                        0.0D, 0.0D, 0.0D, 1.0F, lifetime, 0.35F, 0.35F, 0.35F);
            } else {
                Particle created = Minecraft.getInstance().particleEngine.createParticle(particle,
                        x + Math.cos(theta) * range, y, z + Math.sin(theta) * range, 0.0D, 0.0D, 0.0D);
                if (created != null) {
                    created.setLifetime(lifetime);
                }
            }
        }
    }

    private static BlockState nearbyBlockState(ClientLevel level, double x, double y, double z) {
        BlockPos pos = BlockPos.containing(x, y, z);
        BlockState state = level.getBlockState(pos);
        if (!state.isAir()) {
            return state;
        }
        for (Direction direction : Direction.values()) {
            state = level.getBlockState(pos.relative(direction));
            if (!state.isAir()) {
                return state;
            }
        }
        return Blocks.STONE.defaultBlockState();
    }

    private static int getInt(CompoundTag data, String key, int fallback) {
        return data.contains(key) ? data.getInt(key) : fallback;
    }

    private static float getFloat(CompoundTag data, String key, float fallback) {
        return data.contains(key) ? data.getFloat(key) : fallback;
    }

    private record BoneDefinition(SkeletonParticle.BoneType type, float yaw, float pitch, double x, double y, double z) {
    }

    private HbmParticleEffects() {
    }
}
