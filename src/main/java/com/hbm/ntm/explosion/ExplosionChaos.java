package com.hbm.ntm.explosion;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class ExplosionChaos {
    private static final Random RANDOM = new Random();

    public static void hardenVirus(Level level, int x, int y, int z, int bombStartStrength) {
        if (level == null || level.isClientSide() || bombStartStrength <= 0) {
            return;
        }
        RegistryObject<? extends Block> virus = ModBlocks.legacyBlock("crystal_virus");
        RegistryObject<? extends Block> hardened = ModBlocks.legacyBlock("crystal_hardened");
        Block virusBlock = Objects.requireNonNull(virus, "Missing legacy block hbm_ntm_rebirth:crystal_virus").get();
        Block hardenedBlock = Objects.requireNonNull(hardened, "Missing legacy block hbm_ntm_rebirth:crystal_hardened").get();

        applyHalfSphere(level, x, y, z, bombStartStrength, pos -> {
            if (level.getBlockState(pos).is(virusBlock)) {
                level.setBlock(pos, hardenedBlock.defaultBlockState(), 3);
            }
        });
    }

    public static void igniteFlammableBlocks(Level level, int x, int y, int z, int bound) {
        if (level == null || level.isClientSide() || bound <= 0) {
            return;
        }
        applyHalfSphere(level, x, y, z, bound, pos -> {
            BlockState state = level.getBlockState(pos);
            BlockPos above = pos.above();
            if (state.isFlammable(level, pos, Direction.UP) && level.getBlockState(above).isAir()) {
                level.setBlock(above, BaseFireBlock.getState(level, above), 3);
            }
        });
    }

    public static void igniteAllBlocks(Level level, int x, int y, int z, int bound) {
        if (level == null || level.isClientSide() || bound <= 0) {
            return;
        }
        applyHalfSphere(level, x, y, z, bound, pos -> {
            BlockState state = level.getBlockState(pos);
            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);
            if (!state.isAir() && (aboveState.isAir() || aboveState.getBlock() instanceof SnowLayerBlock)) {
                level.setBlock(above, BaseFireBlock.getState(level, above), 3);
            }
        });
    }

    public static void spawnPoisonCloud(Level level, double x, double y, double z, int count, double speed, int type) {
        ParticleUtil.spawnChaosCloudBurst(level, x, y, z, count, speed, type == 1
                ? ParticleUtil.CHAOS_CLOUD_GREEN
                : type == 2 ? ParticleUtil.CHAOS_CLOUD_PINK : ParticleUtil.CHAOS_CLOUD_ORANGE);
    }

    public static void spawnVolley(Level level, double x, double y, double z, int count, double speed) {
        ParticleUtil.spawnChaosVolley(level, x, y, z, count, speed);
    }

    public static void poison(Level level, double x, double y, double z, double range) {
        if (level == null || level.isClientSide() || range <= 0.0D) {
            return;
        }
        for (LivingEntity entity : livingInRange(level, x, y, z, range)) {
            if (!ArmorUtil.hasAnyProtectionAndDamageFilter(entity, 1,
                    HazardClass.GAS_LUNG, HazardClass.GAS_BLISTERING)) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 5 * 20, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 20, 2));
                entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 1 * 20, 1));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30 * 20, 1));
                entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 30 * 20, 2));
            }
        }
    }

    public static void pc(Level level, double x, double y, double z, double range) {
        if (level == null || level.isClientSide() || range <= 0.0D) {
            return;
        }
        for (LivingEntity entity : livingInRange(level, x, y, z, range)) {
            ArmorUtil.damageSuitAll(entity, 25);
            EntityDamageUtil.attackEntityFromNt(entity, ModDamageSources.pc(level), 5.0F);
        }
    }

    public static void c(Level level, double x, double y, double z, double range) {
        if (level == null || level.isClientSide() || range <= 0.0D) {
            return;
        }
        for (LivingEntity entity : livingInRange(level, x, y, z, range)) {
            ArmorUtil.damageSuitAll(entity, 25);
            if (ArmorUtil.checkForHazmat(entity)) {
                continue;
            }
            if (entity.hasEffect(ModEffects.TAINT.get())) {
                entity.removeEffect(ModEffects.TAINT.get());
                entity.addEffect(new MobEffectInstance(ModEffects.MUTATION.get(), 60 * 60 * 20, 0, false, true));
            }
            EntityDamageUtil.attackEntityFromNt(entity, ModDamageSources.cloud(level), 5.0F);
        }
    }

    public static void floater(Level level, int x, int y, int z, int radius, int height) {
        if (level == null || level.isClientSide() || radius <= 0 || height == 0) {
            return;
        }
        applyHalfSphere(level, x, y, z, radius, pos -> {
            BlockState state = level.getBlockState(pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            if (state.isAir()) {
                return;
            }
            BlockPos target = pos.above(height);
            if (level.isOutsideBuildHeight(target)) {
                return;
            }
            level.setBlock(target, state, 3);
        });
    }

    public static void move(Level level, int x, int y, int z, int radius, int offsetX, int offsetY, int offsetZ) {
        if (level == null || level.isClientSide() || radius <= 0) {
            return;
        }
        double range = radius;
        AABB bounds = new AABB(x - range - 1.0D, y - range - 1.0D, z - range - 1.0D,
                x + range + 1.0D, y + range + 1.0D, z + range + 1.0D);
        for (Entity entity : level.getEntities(null, bounds)) {
            if (Math.sqrt(entity.distanceToSqr(x, y, z)) / (radius * 2.0D) > 1.0D) {
                continue;
            }
            if (entity instanceof Sheep sheep) {
                sheep.setCustomName(net.minecraft.network.chat.Component.literal("jeb_"));
            } else if (entity instanceof LivingEntity living && !(living instanceof Player)) {
                living.setCustomName(net.minecraft.network.chat.Component.literal(RANDOM.nextBoolean() ? "Dinnerbone" : "Grumm"));
            }
            double dx = entity.getX() - x;
            double dy = entity.getY() + entity.getEyeHeight() - y;
            double dz = entity.getZ() - z;
            if (Math.sqrt(dx * dx + dy * dy + dz * dz) < range) {
                entity.teleportTo(entity.getX() + offsetX, entity.getY() + offsetY, entity.getZ() + offsetZ);
            }
        }
    }

    private static List<LivingEntity> livingInRange(Level level, double x, double y, double z, double range) {
        AABB bounds = new AABB(x - range, y - range, z - range, x + range, y + range, z + range);
        double rangeSquared = range * range;
        return level.getEntitiesOfClass(LivingEntity.class, bounds,
                entity -> entity.distanceToSqr(x, y, z) <= rangeSquared);
    }

    private static void applyHalfSphere(Level level, int x, int y, int z, int radius, PosConsumer consumer) {
        int radiusSquared = radius * radius;
        int threshold = radiusSquared / 2;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -radius; xx < radius; xx++) {
            int xx2 = xx * xx;
            for (int yy = -radius; yy < radius; yy++) {
                int yy2 = xx2 + yy * yy;
                for (int zz = -radius; zz < radius; zz++) {
                    if (yy2 + zz * zz < threshold) {
                        cursor.set(x + xx, y + yy, z + zz);
                        if (!level.isOutsideBuildHeight(cursor)) {
                            consumer.accept(cursor);
                        }
                    }
                }
            }
        }
    }

    @FunctionalInterface
    private interface PosConsumer {
        void accept(BlockPos pos);
    }

    private ExplosionChaos() {
    }
}
