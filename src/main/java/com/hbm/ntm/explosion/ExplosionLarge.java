package com.hbm.ntm.explosion;

import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.entity.projectile.RubbleEntity;
import com.hbm.ntm.entity.projectile.ShrapnelEntity;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.util.HbmBlockStateUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ExplosionLarge {
    public static void spawnParticles(Level level, double x, double y, double z, int count) {
        spawnSmokeMode(level, x, y, z, "cloud", count);
    }

    public static void spawnParticlesRadial(Level level, double x, double y, double z, int count) {
        spawnSmokeMode(level, x, y, z, "radial", count);
    }

    public static void spawnFoam(Level level, double x, double y, double z, int count) {
        spawnSmokeMode(level, x, y, z, "foamSplash", count);
    }

    public static void spawnShock(Level level, double x, double y, double z, int count, double strength) {
        ParticleUtil.spawnSmokeShock(level, x, y + 0.5D, z, count, strength, false);
    }

    public static void spawnBurst(Level level, double x, double y, double z, int count, double strength) {
        if (level == null) {
            return;
        }
        if (count <= 0) {
            return;
        }
        double angle = level.random.nextInt(360);
        int step = 360 / count;
        for (int i = 0; i < count; i++) {
            ParticleUtil.spawnGasFlame(level, x, y, z, Math.cos(angle) * strength, 0.0D, Math.sin(angle) * strength);
            angle += step;
        }
    }

    public static void spawnRubble(Level level, double x, double y, double z, int count) {
        spawnRubble(level, x, y, z, count, null);
    }

    public static void spawnRubble(Level level, double x, double y, double z, int count, @Nullable Entity source) {
        if (!(level instanceof ServerLevel) || count <= 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            RubbleEntity rubble = new RubbleEntity(level);
            rubble.setOwner(source);
            rubble.setBlockState(Blocks.STONE.defaultBlockState());
            rubble.setPos(x, y, z);
            int speedScale = 1 + (count + level.random.nextInt(count * 5)) / 25;
            rubble.setDeltaMovement(
                    level.random.nextGaussian() * 0.75D * (1 + count / 50),
                    0.75D * speedScale,
                    level.random.nextGaussian() * 0.75D * (1 + count / 50));
            level.addFreshEntity(rubble);
        }
    }

    public static void spawnShrapnels(Level level, double x, double y, double z, int count, float motion) {
        spawnShrapnelEntities(level, x, y, z, count, motion, false, null);
    }

    public static void spawnShrapnels(Level level, double x, double y, double z, int count, float motion, @Nullable Entity source) {
        spawnShrapnelEntities(level, x, y, z, count, motion, false, source);
    }

    public static void spawnShrapnels(Level level, double x, double y, double z, int count) {
        spawnShrapnels(level, x, y, z, count, 1.0F);
    }

    public static void spawnTracers(Level level, double x, double y, double z, int count, float motion) {
        spawnTracerEntities(level, x, y, z, count, motion, null);
    }

    public static void spawnTracers(Level level, double x, double y, double z, int count, float motion, @Nullable Entity source) {
        spawnTracerEntities(level, x, y, z, count, motion, source);
    }

    public static void spawnTracers(Level level, double x, double y, double z, int count) {
        spawnTracers(level, x, y, z, count, 1.0F);
    }

    public static void spawnShrapnelShower(Level level, double x, double y, double z, int count, float motion) {
        spawnShrapnelEntities(level, x, y, z, count, motion, false, null);
    }

    public static void spawnShrapnelShower(Level level, double x, double y, double z, int count, float motion, @Nullable Entity source) {
        spawnShrapnelEntities(level, x, y, z, count, motion, false, source);
    }

    public static void spawnShrapnelShower(Level level, double x, double y, double z, double motionX, double motionY,
            double motionZ, int count, double deviation) {
        spawnShrapnelShower(level, x, y, z, motionX, motionY, motionZ, count, deviation, null);
    }

    public static void spawnShrapnelShower(Level level, double x, double y, double z, double motionX, double motionY,
            double motionZ, int count, double deviation, @Nullable Entity source) {
        if (!(level instanceof ServerLevel) || count <= 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            ShrapnelEntity shrapnel = new ShrapnelEntity(level);
            shrapnel.setOwner(source);
            shrapnel.setPos(x, y, z);
            shrapnel.setDeltaMovement(
                    motionX + level.random.nextGaussian() * deviation,
                    motionY + level.random.nextGaussian() * deviation,
                    motionZ + level.random.nextGaussian() * deviation);
            if (level.random.nextInt(3) == 0) {
                shrapnel.setTrail();
            }
            level.addFreshEntity(shrapnel);
        }
    }

    public static void spawnMissileDebris(Level level, double x, double y, double z, List<ItemStack> debris, @Nullable ItemStack rareDrop) {
        if (level == null || level.isClientSide()) {
            return;
        }
        for (ItemStack stack : debris) {
            spawnDebrisItem(level, x, y, z, stack);
        }
        if (rareDrop != null && !rareDrop.isEmpty() && level.random.nextInt(25) == 0) {
            spawnDebrisItem(level, x, y, z, rareDrop);
        }
    }

    public static void spawnMissileDebris(Level level, double x, double y, double z, double motionX, double motionY,
            double motionZ, double deviation, List<ItemStack> debris, @Nullable ItemStack rareDrop) {
        if (level == null || level.isClientSide() || debris == null) {
            return;
        }
        for (ItemStack stack : debris) {
            if (stack.isEmpty()) {
                continue;
            }
            int amount = level.random.nextInt(stack.getCount() + 1);
            for (int i = 0; i < amount; i++) {
                ItemStack copy = stack.copy();
                Vec3 motion = new Vec3(
                        motionX + level.random.nextGaussian() * deviation,
                        motionY + level.random.nextGaussian() * deviation,
                        motionZ + level.random.nextGaussian() * deviation).scale(0.85D);
                spawnDebrisItem(level, x, y, z, copy, motion, true);
            }
        }
        if (rareDrop != null && !rareDrop.isEmpty() && level.random.nextInt(10) == 0) {
            Vec3 motion = new Vec3(
                    motionX + level.random.nextGaussian() * deviation * 0.1D,
                    motionY + level.random.nextGaussian() * deviation * 0.1D,
                    motionZ + level.random.nextGaussian() * deviation * 0.1D);
            spawnDebrisItem(level, x, y, z, rareDrop, motion, false);
        }
    }

    public static void explode(Level level, double x, double y, double z, float strength, boolean cloud, boolean rubble, boolean shrapnel) {
        explode(level, x, y, z, strength, cloud, rubble, shrapnel, null);
    }

    public static void explode(Level level, double x, double y, double z, float strength, boolean cloud, boolean rubble,
            boolean shrapnel, @Nullable Entity source) {
        WeaponExplosionUtil.explodeStandard(level, x, y, z, strength, source, true, false);
        spawnLegacyExtras(level, x, y, z, strength, cloud, rubble, shrapnel, source);
    }

    public static void explodeFire(Level level, double x, double y, double z, float strength, boolean cloud, boolean rubble,
            boolean shrapnel, @Nullable Entity source) {
        WeaponExplosionUtil.explodeStandard(level, x, y, z, strength, source, true, true);
        spawnLegacyExtras(level, x, y, z, strength, cloud, rubble, shrapnel, source);
    }

    public static void explodeFire(Level level, double x, double y, double z, float strength, boolean cloud, boolean rubble,
            boolean shrapnel) {
        explodeFire(level, x, y, z, strength, cloud, rubble, shrapnel, null);
    }

    public static void buster(Level level, double x, double y, double z, Vec3 direction, float strength, int depth) {
        buster(level, x, y, z, direction, strength, depth, null);
    }

    public static void buster(Level level, double x, double y, double z, Vec3 direction, float strength, int depth, @Nullable Entity source) {
        buster(level, x, y, z, direction, strength, (double) depth, 2.0D, source);
    }

    public static void buster(Level level, double x, double y, double z, Vec3 direction, float strength, float depth) {
        buster(level, x, y, z, direction, strength, depth, null);
    }

    public static void buster(Level level, double x, double y, double z, Vec3 direction, float strength, float depth,
            @Nullable Entity source) {
        buster(level, x, y, z, direction, strength, depth, 2.0D, source);
    }

    private static void buster(Level level, double x, double y, double z, Vec3 direction, float strength, double depth,
            double stepLength, @Nullable Entity source) {
        if (direction.lengthSqr() <= 1.0E-7D) {
            return;
        }
        Vec3 step = direction.normalize().scale(stepLength);
        for (double distance = 0.0D; distance < depth; distance += stepLength) {
            double factor = distance / stepLength;
            WeaponExplosionUtil.explodeStandard(level, x + step.x * factor, y + step.y * factor, z + step.z * factor,
                    strength, source, true, false);
        }
    }

    public static void jolt(Level level, double x, double y, double z, int strength, int count, double velocity) {
        jolt(level, x, y, z, (double) strength, count, velocity);
    }

    public static void jolt(Level level, double x, double y, double z, double strength, int count, double velocity) {
        if (!(level instanceof ServerLevel serverLevel) || strength <= 0 || count <= 0) {
            return;
        }
        for (int c = 0; c < count; c++) {
            Vec3 direction = randomDirection(level);
            for (double i = 0.0D; i < strength; i++) {
                double sampleX = x + direction.x * i;
                double sampleY = y + direction.y * i;
                double sampleZ = z + direction.z * i;
                BlockPos pos = new BlockPos((int) sampleX, (int) sampleY, (int) sampleZ);
                BlockState state = level.getBlockState(pos);
                if (!state.getFluidState().isEmpty()) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    continue;
                }
                if (state.isAir()) {
                    continue;
                }
                if (hasHighExplosionResistance(state)) {
                    continue;
                }
                RubbleEntity rubble = new RubbleEntity(level);
                rubble.setBlockState(state);
                rubble.setPos(sampleX + 0.5D, sampleY + 0.5D, sampleZ + 0.5D);
                Vec3 motion = new Vec3(x - rubble.getX(), y - rubble.getY(), z - rubble.getZ());
                rubble.setDeltaMovement(motion.scale(velocity));
                serverLevel.addFreshEntity(rubble);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                break;
            }
        }
    }

    private static boolean hasHighExplosionResistance(BlockState state) {
        return HbmBlockStateUtil.explosionResistance(state) > 70.0F;
    }

    public static int cloudFunction(int strength) {
        return (int) (850.0D * (1.0D - Math.exp(-(strength / 15))) + 15.0D);
    }

    public static int rubbleFunction(int strength) {
        return strength / 10;
    }

    public static int shrapnelFunction(int strength) {
        return strength / 3;
    }

    private static void spawnLegacyExtras(Level level, double x, double y, double z, float strength, boolean cloud, boolean rubble,
            boolean shrapnel, @Nullable Entity source) {
        int intStrength = Math.max(0, Math.round(strength));
        if (cloud) {
            spawnParticles(level, x, y, z, cloudFunction(intStrength));
        }
        if (rubble) {
            spawnRubble(level, x, y, z, rubbleFunction(intStrength), source);
        }
        if (shrapnel) {
            spawnShrapnels(level, x, y, z, shrapnelFunction(intStrength), 1.0F, source);
        }
    }

    private static void spawnDebrisItem(Level level, double x, double y, double z, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        spawnDebrisItem(level, x, y, z, stack, randomDirection(level).scale(0.2D + level.random.nextDouble() * 0.6D), false);
    }

    private static void spawnDebrisItem(Level level, double x, double y, double z, ItemStack stack, Vec3 motion,
            boolean projectFromOrigin) {
        if (stack.isEmpty()) {
            return;
        }
        double itemX = projectFromOrigin ? x + motion.x * 2.0D : x;
        double itemY = projectFromOrigin ? y + motion.y * 2.0D : y;
        double itemZ = projectFromOrigin ? z + motion.z * 2.0D : z;
        ItemEntity item = new ItemEntity(level, itemX, itemY, itemZ, stack.copy());
        item.setDeltaMovement(motion);
        level.addFreshEntity(item);
    }

    private static void spawnShrapnelEntities(Level level, double x, double y, double z, int count, float motion,
            boolean tracer, @Nullable Entity source) {
        if (!(level instanceof ServerLevel) || count <= 0) {
            return;
        }
        double speed = Math.max(0.0D, motion);
        for (int i = 0; i < count; i++) {
            ShrapnelEntity shrapnel = new ShrapnelEntity(level);
            shrapnel.setOwner(source);
            shrapnel.setPos(x, y, z);
            double motionY = ((level.random.nextFloat() * 0.5D) + 0.5D)
                    * (1 + count / (15 + level.random.nextInt(21)))
                    + (level.random.nextFloat() / 50.0D * count);
            double horizontalScale = 1 + count / 50;
            shrapnel.setDeltaMovement(
                    level.random.nextGaussian() * horizontalScale * speed,
                    motionY * speed,
                    level.random.nextGaussian() * horizontalScale * speed);
            if (level.random.nextInt(3) == 0 || tracer) {
                shrapnel.setTrail();
            }
            level.addFreshEntity(shrapnel);
        }
    }

    private static void spawnTracerEntities(Level level, double x, double y, double z, int count, float motion, @Nullable Entity source) {
        if (!(level instanceof ServerLevel) || count <= 0) {
            return;
        }
        double speed = Math.max(0.0D, motion);
        for (int i = 0; i < count; i++) {
            ShrapnelEntity shrapnel = new ShrapnelEntity(level);
            shrapnel.setOwner(source);
            shrapnel.setPos(x, y, z);
            double motionY = ((level.random.nextFloat() * 0.5D) + 0.5D)
                    * (1 + count / (15 + level.random.nextInt(21)))
                    + (level.random.nextFloat() / 50.0D * count) * 0.25D;
            double horizontalScale = (1 + count / 50) * 0.25D;
            shrapnel.setDeltaMovement(
                    level.random.nextGaussian() * horizontalScale * speed,
                    motionY * speed,
                    level.random.nextGaussian() * horizontalScale * speed);
            shrapnel.setTrail();
            level.addFreshEntity(shrapnel);
        }
    }

    private static void spawnSmokeMode(Level level, double x, double y, double z, String mode, int count) {
        ParticleUtil.spawnSmoke(level, x, y, z, mode, count);
    }

    private static Vec3 randomDirection(Level level) {
        double yaw = level.random.nextDouble() * Math.PI * 2.0D;
        double z = level.random.nextDouble() * 2.0D - 1.0D;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - z * z));
        return new Vec3(Math.cos(yaw) * horizontal, z, Math.sin(yaw) * horizontal);
    }

    private ExplosionLarge() {
    }
}
