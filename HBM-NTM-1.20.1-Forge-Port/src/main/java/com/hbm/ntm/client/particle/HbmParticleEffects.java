package com.hbm.ntm.client.particle;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
            level.addParticle(ParticleTypes.FLAME, x, y, z, data.getDouble("mX"), data.getDouble("mY"), data.getDouble("mZ"));
            level.addParticle(ParticleTypes.SMOKE, x, y, z, data.getDouble("mX") * 0.5D, data.getDouble("mY") * 0.5D, data.getDouble("mZ") * 0.5D);
        } else if (ParticleUtil.TYPE_DEBUG_DRONE.equals(type)) {
            spawnDebugLine(level, x, y, z, data.getDouble("mX"), data.getDouble("mY"), data.getDouble("mZ"));
        } else if ("waterSplash".equals(type)) {
            burstSimple(level, ParticleTypes.CLOUD, x, y, z, 10, 1.0D);
        } else if ("cloudFX2".equals(type)) {
            level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0D, 0.1D, 0.0D);
        } else if ("vanilla".equals(type)) {
            spawnNamedVanilla(level, data.getString("mode"), x, y, z, data.getDouble("mX"), data.getDouble("mY"), data.getDouble("mZ"));
        } else if ("vanillaburst".equals(type)) {
            spawnVanillaBurst(level, data, x, y, z);
        } else if ("vanillaExt".equals(type)) {
            spawnVanillaExt(level, data, x, y, z);
        } else if ("smoke".equals(type)) {
            spawnSmoke(level, data, x, y, z);
        } else if ("launchSmoke".equals(type)) {
            level.addParticle(ModParticleTypes.LAUNCH_SMOKE.get(), x, y, z, data.getDouble("moX"), data.getDouble("moY"), data.getDouble("moZ"));
        } else if ("missileContrail".equals(type) || "ABMContrail".equals(type) || "exKerosene".equals(type)
                || "exSolid".equals(type) || "exHydrogen".equals(type) || "exBalefire".equals(type)) {
            spawnContrail(level, data, x, y, z, type);
        } else if ("exhaust".equals(type)) {
            spawnExhaust(level, data, x, y, z);
        } else if ("flamethrower".equals(type)) {
            spawnFlamethrower(level, data, x, y, z);
        } else if ("sweat".equals(type)) {
            spawnEntitySweat(level, data);
        } else if ("vomit".equals(type)) {
            spawnEntityVomit(level, data);
        } else if ("radiationfog".equals(type) || "radiation_fog".equals(type)) {
            level.addParticle(ModParticleTypes.RADIATION_FOG.get(), x, y, z, 0.0D, 0.0D, 0.0D);
        } else if ("radFog".equals(type)) {
            level.addParticle(ModParticleTypes.RADIATION_FOG.get(), x, y, z, 0.0D, 0.0D, 0.0D);
        } else if ("weaponExplosion".equals(type)) {
            spawnWeaponExplosion(level, data, x, y, z);
        } else if ("amat".equals(type)) {
            spawnAmat(level, data, x, y, z);
        } else if ("muke".equals(type) || "tinytot".equals(type)) {
            spawnMuke(level, data, x, y, z, "tinytot".equals(type));
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

    private static void spawnDebugLine(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ) {
        int steps = Math.max(1, Mth.ceil(Math.sqrt(BlockPos.containing(x, y, z).distSqr(BlockPos.containing(targetX, targetY, targetZ))) * 2.0D));
        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / (double) steps;
            level.addParticle(ParticleTypes.END_ROD,
                    Mth.lerp(progress, x, targetX),
                    Mth.lerp(progress, y, targetY),
                    Mth.lerp(progress, z, targetZ),
                    0.0D, 0.0D, 0.0D);
        }
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
            spawnNamedVanilla(level, mode, x, y, z, motionX, motionY, motionZ);
        }
    }

    private static void spawnVanillaExt(ClientLevel level, CompoundTag data, double x, double y, double z) {
        String mode = data.getString("mode");
        double motionX = data.getDouble("mX");
        double motionY = data.getDouble("mY");
        double motionZ = data.getDouble("mZ");
        if ("volcano".equals(mode)) {
            for (int i = 0; i < 5; i++) {
                level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, x, y, z,
                        level.random.nextGaussian() * 0.2D, 2.5D + level.random.nextDouble(), level.random.nextGaussian() * 0.2D);
            }
            return;
        }
        if ("largeexplode".equals(mode)) {
            int count = Math.max(1, data.getByte("count"));
            level.addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 0.0D, 0.0D, 0.0D);
            burstSimple(level, ParticleTypes.POOF, x, y, z, count, Math.max(0.15D, data.getFloat("size")));
            return;
        }
        if ("townaura".equals(mode)) {
            level.addParticle(ParticleTypes.MYCELIUM, x, y, z, motionX, motionY, motionZ);
            return;
        }
        spawnNamedVanilla(level, mode, x, y, z, motionX, motionY, motionZ);
    }

    private static void spawnSmoke(ClientLevel level, CompoundTag data, double x, double y, double z) {
        String mode = data.getString("mode");
        int count = Math.max(1, data.getInt("count"));
        RandomSource random = level.random;
        if ("cloud".equals(mode) || "radial".equals(mode)) {
            double ySpread = "cloud".equals(mode) ? 1.0D + count / 100.0D : 1.0D + count / 50.0D;
            double xzSpread = "cloud".equals(mode) ? 1.0D + count / 150.0D : 1.0D + count / 50.0D;
            for (int i = 0; i < count; i++) {
                double motionY = random.nextGaussian() * ySpread;
                if ("cloud".equals(mode) && random.nextBoolean()) {
                    motionY = Math.abs(motionY);
                }
                level.addParticle(ModParticleTypes.EX_SMOKE.get(), x, y, z,
                        random.nextGaussian() * xzSpread, motionY, random.nextGaussian() * xzSpread);
            }
        } else if ("radialDigamma".equals(mode)) {
            spawnRadial(level, ParticleTypes.WITCH, x, y, z, count, 2.0D);
        } else if ("shock".equals(mode) || "shockRand".equals(mode)) {
            spawnRadial(level, ModParticleTypes.EX_SMOKE.get(), x, y, z, count, Math.max(0.1D, data.getDouble("strength")));
        } else if ("wave".equals(mode)) {
            spawnRing(level, ModParticleTypes.EX_SMOKE.get(), x, y, z, count, Math.max(0.1D, data.getDouble("range")));
        } else if ("foamSplash".equals(mode)) {
            spawnRing(level, ModParticleTypes.FOAM.get(), x, y, z, count, Math.max(0.1D, data.getDouble("range")));
        }
    }

    private static void spawnContrail(ClientLevel level, CompoundTag data, double x, double y, double z, String type) {
        double motionX = data.contains("moX") ? data.getDouble("moX") : 0.0D;
        double motionY = data.contains("moY") ? data.getDouble("moY") : 0.0D;
        double motionZ = data.contains("moZ") ? data.getDouble("moZ") : 0.0D;
        ParticleOptions particle = "missileContrail".equals(type) ? ModParticleTypes.ROCKET_FLAME.get() : ModParticleTypes.CONTRAIL.get();
        level.addParticle(particle, x, y, z, motionX, motionY, motionZ);
        if ("missileContrail".equals(type)) {
            level.addParticle(ModParticleTypes.CONTRAIL.get(), x, y, z, motionX * 0.5D, motionY * 0.5D, motionZ * 0.5D);
        } else if ("exSolid".equals(type) || "exHydrogen".equals(type) || "exBalefire".equals(type)) {
            level.addParticle(solidContrailDust(type), x, y, z, motionX * 0.25D, motionY * 0.25D, motionZ * 0.25D);
        }
    }

    private static void spawnExhaust(ClientLevel level, CompoundTag data, double x, double y, double z) {
        String mode = data.getString("mode");
        int count = Math.max(1, data.getInt("count"));
        double width = data.getDouble("width");
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            double px = x + random.nextGaussian() * width;
            double py = "meteor".equals(mode) ? y + random.nextGaussian() * width : y;
            double pz = z + random.nextGaussian() * width;
            level.addParticle(ParticleTypes.FLAME, px, py, pz, 0.0D, -0.75D + random.nextDouble() * 0.5D, 0.0D);
            level.addParticle(ModParticleTypes.ROCKET_FLAME.get(), px, py, pz, 0.0D, -0.25D, 0.0D);
        }
    }

    private static void spawnFlamethrower(ClientLevel level, CompoundTag data, double x, double y, double z) {
        int meta = data.getInt("meta");
        ParticleOptions particle = switch (meta) {
            case 1 -> ParticleTypes.SOUL_FIRE_FLAME;
            case 2 -> ParticleTypes.WITCH;
            case 4 -> ParticleTypes.LARGE_SMOKE;
            default -> ModParticleTypes.FLAMETHROWER.get();
        };
        level.addParticle(particle, x, y, z, level.random.nextGaussian() * 0.015D, 0.02D, level.random.nextGaussian() * 0.015D);
    }

    private static void spawnEntitySweat(ClientLevel level, CompoundTag data) {
        Entity entity = level.getEntity(data.getInt("entity"));
        if (entity == null) {
            return;
        }
        BlockState state = blockStateFromLegacyId(data.getInt("block"));
        int count = Math.max(1, data.getInt("count"));
        for (int i = 0; i < count; i++) {
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    entity.getRandomX(0.5D), entity.getY() + entity.getBbHeight() * level.random.nextDouble(), entity.getRandomZ(0.5D),
                    level.random.nextGaussian() * 0.02D, 0.04D, level.random.nextGaussian() * 0.02D);
        }
    }

    private static void spawnEntityVomit(ClientLevel level, CompoundTag data) {
        Entity entity = level.getEntity(data.getInt("entity"));
        if (entity == null) {
            return;
        }
        int count = Math.max(1, data.getInt("count"));
        String mode = data.getString("mode");
        ParticleOptions particle = switch (mode) {
            case "blood" -> new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState());
            case "smoke" -> ParticleTypes.LARGE_SMOKE;
            default -> ParticleTypes.ITEM_SLIME;
        };
        for (int i = 0; i < count; i++) {
            level.addParticle(particle,
                    entity.getX(), entity.getEyeY() - 0.15D, entity.getZ(),
                    (level.random.nextDouble() - 0.5D) * 0.2D,
                    level.random.nextDouble() * 0.05D,
                    (level.random.nextDouble() - 0.5D) * 0.2D);
        }
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
        int count = Mth.clamp((int) (data.getFloat("scale") * 4.0F), 12, 160);
        spawnRadial(level, ParticleTypes.WITCH, x, y, z, count, Math.max(0.5D, data.getFloat("scale") * 0.12D));
        level.addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    private static void spawnMuke(ClientLevel level, CompoundTag data, double x, double y, double z, boolean tiny) {
        int count = tiny ? 40 : 100;
        double scale = tiny ? 0.8D : 1.8D;
        level.addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 0.0D, 0.0D, 0.0D);
        spawnRadial(level, data.getBoolean("balefire") ? ParticleTypes.WITCH : ModParticleTypes.EX_SMOKE.get(),
                x, y + 0.5D, z, count, scale * 0.25D);
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            double height = random.nextDouble() * scale * 5.0D;
            double width = scale * (0.4D + height * 0.15D);
            ParticleOptions particle = data.getBoolean("balefire") && i % 3 == 0 ? ParticleTypes.SOUL_FIRE_FLAME : ModParticleTypes.EX_SMOKE.get();
            level.addParticle(particle,
                    x + random.nextGaussian() * width,
                    y + height,
                    z + random.nextGaussian() * width,
                    random.nextGaussian() * 0.05D,
                    0.04D + random.nextDouble() * 0.08D,
                    random.nextGaussian() * 0.05D);
        }
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

    private static void spawnNamedVanilla(ClientLevel level, String mode, double x, double y, double z, double motionX, double motionY, double motionZ) {
        ParticleOptions particle = switch (mode) {
            case "flame" -> ParticleTypes.FLAME;
            case "smoke" -> ParticleTypes.SMOKE;
            case "cloud" -> ParticleTypes.CLOUD;
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

    private static ParticleOptions solidContrailDust(String type) {
        return switch (type) {
            case "exSolid" -> new DustParticleOptions(new Vector3f(0.3F, 0.2F, 0.05F), 1.0F);
            case "exHydrogen" -> new DustParticleOptions(new Vector3f(0.7F, 0.7F, 0.7F), 1.0F);
            case "exBalefire" -> new DustParticleOptions(new Vector3f(0.2F, 0.7F, 0.2F), 1.0F);
            default -> DustParticleOptions.REDSTONE;
        };
    }

    private static void burstSimple(ClientLevel level, ParticleOptions particle, double x, double y, double z, int count, double motion) {
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            level.addParticle(particle, x + random.nextGaussian(), y + random.nextGaussian(), z + random.nextGaussian(),
                    random.nextGaussian() * motion, random.nextGaussian() * motion, random.nextGaussian() * motion);
        }
    }

    private static void spawnRadial(ClientLevel level, ParticleOptions particle, double x, double y, double z, int count, double strength) {
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        for (int i = 0; i < count; i++) {
            double theta = angle + Math.PI * 2.0D * i / count;
            level.addParticle(particle, x, y, z, Math.cos(theta) * strength, 0.0D, Math.sin(theta) * strength);
        }
    }

    private static void spawnRing(ClientLevel level, ParticleOptions particle, double x, double y, double z, int count, double range) {
        for (int i = 0; i < count; i++) {
            double theta = level.random.nextDouble() * Math.PI * 2.0D;
            level.addParticle(particle, x + Math.cos(theta) * range, y, z + Math.sin(theta) * range, 0.0D, 0.0D, 0.0D);
        }
    }

    private static BlockState blockStateFromLegacyId(int legacyId) {
        BlockState state = Block.stateById(legacyId);
        return state == null ? Blocks.STONE.defaultBlockState() : state;
    }

    private HbmParticleEffects() {
    }
}
