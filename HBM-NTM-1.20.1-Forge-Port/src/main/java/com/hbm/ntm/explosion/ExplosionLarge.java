package com.hbm.ntm.explosion;

import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.particle.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
        CompoundTag data = smokeTag("shock", count);
        data.putDouble("strength", strength);
        ParticleUtil.spawnAux(level, x, y + 0.5D, z, data, 250.0D);
    }

    public static void spawnBurst(Level level, double x, double y, double z, int count, double strength) {
        if (level == null) {
            return;
        }
        for (int i = 0; i < count; i++) {
            double yaw = level.random.nextDouble() * Math.PI * 2.0D;
            double pitch = (level.random.nextDouble() - 0.5D) * Math.PI;
            double speed = strength * (0.35D + level.random.nextDouble() * 0.65D);
            double motionX = Math.cos(yaw) * Math.cos(pitch) * speed;
            double motionY = Math.sin(pitch) * speed;
            double motionZ = Math.sin(yaw) * Math.cos(pitch) * speed;
            ParticleUtil.spawnGasFlame(level, x, y, z, motionX, motionY, motionZ);
        }
    }

    public static void spawnRubble(Level level, double x, double y, double z, int count) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                    x, y, z, count, 1.0D, 1.0D, 1.0D, 0.35D);
        }
    }

    public static void spawnShrapnels(Level level, double x, double y, double z, int count, float motion) {
        spawnShrapnelVisuals(level, x, y, z, count, motion);
    }

    public static void spawnShrapnels(Level level, double x, double y, double z, int count) {
        spawnShrapnels(level, x, y, z, count, 1.0F);
    }

    public static void spawnTracers(Level level, double x, double y, double z, int count, float motion) {
        spawnShrapnelVisuals(level, x, y, z, count, motion);
    }

    public static void spawnShrapnelShower(Level level, double x, double y, double z, int count, float motion) {
        spawnShrapnelVisuals(level, x, y, z, count, motion);
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

    public static void explode(Level level, double x, double y, double z, float strength, boolean cloud, boolean rubble, boolean shrapnel) {
        explode(level, x, y, z, strength, cloud, rubble, shrapnel, null);
    }

    public static void explode(Level level, double x, double y, double z, float strength, boolean cloud, boolean rubble,
            boolean shrapnel, @Nullable Entity source) {
        WeaponExplosionUtil.explodeStandard(level, x, y, z, strength, source, true, false);
        spawnLegacyExtras(level, x, y, z, strength, cloud, rubble, shrapnel);
    }

    public static void explodeFire(Level level, double x, double y, double z, float strength, boolean cloud, boolean rubble,
            boolean shrapnel, @Nullable Entity source) {
        WeaponExplosionUtil.explodeStandard(level, x, y, z, strength, source, true, true);
        spawnLegacyExtras(level, x, y, z, strength, cloud, rubble, shrapnel);
    }

    public static void buster(Level level, double x, double y, double z, Vec3 direction, float strength, int depth) {
        buster(level, x, y, z, direction, strength, depth, null);
    }

    public static void buster(Level level, double x, double y, double z, Vec3 direction, float strength, int depth, @Nullable Entity source) {
        if (direction.lengthSqr() <= 1.0E-7D) {
            return;
        }
        Vec3 step = direction.normalize();
        for (int i = 0; i < depth; i++) {
            WeaponExplosionUtil.explodeStandard(level, x + step.x * i, y + step.y * i, z + step.z * i, strength, source, true, false);
        }
    }

    public static void jolt(Level level, double x, double y, double z, int strength, int count, double velocity) {
        if (!(level instanceof ServerLevel serverLevel) || strength <= 0 || count <= 0) {
            return;
        }
        for (int c = 0; c < count; c++) {
            Vec3 direction = randomDirection(level);
            for (int i = 0; i < strength; i++) {
                BlockPos pos = BlockPos.containing(x + direction.x * i, y + direction.y * i, z + direction.z * i);
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
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 12,
                        Math.max(0.1D, velocity), Math.max(0.1D, velocity), Math.max(0.1D, velocity), 0.2D);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                break;
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static boolean hasHighExplosionResistance(BlockState state) {
        return state.getBlock().getExplosionResistance() > 70.0F;
    }

    public static int cloudFunction(int strength) {
        return (int) (850.0D * (1.0D - Math.exp(-strength / 15.0D)) + 15.0D);
    }

    public static int rubbleFunction(int strength) {
        return strength / 10;
    }

    public static int shrapnelFunction(int strength) {
        return strength / 3;
    }

    private static void spawnLegacyExtras(Level level, double x, double y, double z, float strength, boolean cloud, boolean rubble, boolean shrapnel) {
        int intStrength = Math.max(0, Math.round(strength));
        if (cloud) {
            spawnParticles(level, x, y, z, cloudFunction(intStrength));
        }
        if (rubble) {
            spawnRubble(level, x, y, z, rubbleFunction(intStrength));
        }
        if (shrapnel) {
            spawnShrapnels(level, x, y, z, shrapnelFunction(intStrength), strength * 0.1F);
        }
    }

    private static void spawnDebrisItem(Level level, double x, double y, double z, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemEntity item = new ItemEntity(level, x, y, z, stack.copy());
        item.setDeltaMovement(randomDirection(level).scale(0.2D + level.random.nextDouble() * 0.6D));
        level.addFreshEntity(item);
    }

    private static void spawnShrapnelVisuals(Level level, double x, double y, double z, int count, float motion) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, x, y, z, count, 1.0D, 1.0D, 1.0D, motion);
        }
    }

    private static void spawnSmokeMode(Level level, double x, double y, double z, String mode, int count) {
        ParticleUtil.spawnAux(level, x, y, z, smokeTag(mode, count), 250.0D);
    }

    private static CompoundTag smokeTag(String mode, int count) {
        CompoundTag data = new CompoundTag();
        data.putString("type", "smoke");
        data.putString("mode", mode);
        data.putInt("count", count);
        return data;
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
