package com.hbm.ntm.explosion;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;

public final class ExplosionThermo {
    public static void freeze(Level level, int x, int y, int z, int bombStartStrength) {
        applyJaggedSphere(level, x, y, z, bombStartStrength * 2, ExplosionThermo::freezeDest);
    }

    public static void snow(Level level, int x, int y, int z, int bound) {
        if (level == null || level.isClientSide() || bound <= 0) {
            return;
        }

        int radiusSquared = bound * bound;
        int halfRadiusSquared = radiusSquared / 2;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -bound; xx < bound; xx++) {
            int worldX = xx + x;
            int xx2 = xx * xx;
            for (int yy = -bound; yy < bound; yy++) {
                int worldY = yy + y;
                int yy2 = xx2 + yy * yy;
                for (int zz = -bound; zz < bound; zz++) {
                    if (yy2 + zz * zz < halfRadiusSquared) {
                        BlockPos pos = cursor.set(worldX, worldY + 1, zz + z);
                        BlockState snow = Blocks.SNOW.defaultBlockState();
                        if ((level.getBlockState(pos).isAir() || level.getBlockState(pos).is(Blocks.FIRE))
                                && snow.canSurvive(level, pos)) {
                            level.setBlock(pos, snow, 3);
                        }
                    }
                }
            }
        }
    }

    public static void scorch(Level level, int x, int y, int z, int bombStartStrength) {
        applyJaggedSphere(level, x, y, z, bombStartStrength * 2, ExplosionThermo::scorchDest);
    }

    public static void scorchLight(Level level, int x, int y, int z, int bombStartStrength) {
        applyJaggedSphere(level, x, y, z, bombStartStrength * 2, ExplosionThermo::scorchDestLight);
    }

    public static void freezeDest(Level level, int x, int y, int z) {
        freezeDest(level, new BlockPos(x, y, z));
    }

    public static void freezeDest(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        BlockState replacement = null;
        if (isLegacy(state, "volcanic_lava_block")) {
            replacement = Blocks.COBBLESTONE.defaultBlockState();
        } else if (state.is(Blocks.GRASS_BLOCK)) {
            replacement = legacyStateOr("frozen_grass", Blocks.SNOW_BLOCK.defaultBlockState());
        } else if (state.is(Blocks.DIRT)) {
            replacement = legacyStateOr("frozen_dirt", Blocks.SNOW_BLOCK.defaultBlockState());
        } else if (state.is(BlockTags.LOGS) || isLegacy(state, "waste_log")) {
            replacement = legacyStateOr("frozen_log", Blocks.PACKED_ICE.defaultBlockState());
        } else if (state.is(BlockTags.PLANKS) || isLegacy(state, "waste_planks")) {
            replacement = legacyStateOr("frozen_planks", Blocks.PACKED_ICE.defaultBlockState());
        } else if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.STONE_BRICKS)) {
            replacement = Blocks.PACKED_ICE.defaultBlockState();
        } else if (state.is(BlockTags.LEAVES)) {
            replacement = Blocks.SNOW_BLOCK.defaultBlockState();
        } else if (state.is(Blocks.LAVA) || state.is(Blocks.OBSIDIAN)) {
            replacement = Blocks.OBSIDIAN.defaultBlockState();
        } else if (state.is(Blocks.WATER)) {
            replacement = Blocks.ICE.defaultBlockState();
        }

        if (replacement != null) {
            level.setBlock(pos, replacement, 3);
        }
    }

    public static void scorchDest(Level level, int x, int y, int z) {
        scorchDest(level, new BlockPos(x, y, z));
    }

    public static void scorchDest(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        BlockState replacement = scorchCommon(level, pos, state, true);
        if (replacement != null) {
            level.setBlock(pos, replacement, 3);
        }
    }

    public static void scorchDestLight(Level level, int x, int y, int z) {
        scorchDestLight(level, new BlockPos(x, y, z));
    }

    public static void scorchDestLight(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        BlockState replacement = scorchCommon(level, pos, state, false);
        if (replacement == null) {
            if (isLegacy(state, "waste_earth")) {
                replacement = Blocks.NETHERRACK.defaultBlockState();
            } else if (state.is(Blocks.OBSIDIAN)) {
                replacement = legacyStateOr("gravel_obsidian", Blocks.CRYING_OBSIDIAN.defaultBlockState());
            } else if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
                replacement = Blocks.GLASS.defaultBlockState();
            } else if (state.is(Blocks.CLAY)) {
                replacement = Blocks.TERRACOTTA.defaultBlockState();
            }
        }
        if (replacement != null) {
            level.setBlock(pos, replacement, 3);
        }
    }

    public static void freezer(Level level, int x, int y, int z, int bombStartStrength) {
        if (level == null || level.isClientSide() || bombStartStrength <= 0) {
            return;
        }

        double entityRadius = bombStartStrength;
        int blockRadius = bombStartStrength * 2;
        AABB bounds = new AABB(x - entityRadius - 1.0D, y - entityRadius - 1.0D, z - entityRadius - 1.0D,
                x + entityRadius + 1.0D, y + entityRadius + 1.0D, z + entityRadius + 1.0D);
        for (Entity entity : level.getEntities(null, bounds)) {
            if (!(entity instanceof LivingEntity living) || entity instanceof Ocelot) {
                continue;
            }
            if (entity.distanceToSqr(x, y, z) > entityRadius * entityRadius) {
                continue;
            }

            BlockPos base = entity.blockPosition();
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int a = -2; a <= 0; a++) {
                for (int b = 0; b < 3; b++) {
                    for (int c = -1; c <= 1; c++) {
                        cursor.set(base.getX() + a, base.getY() + b, base.getZ() + c);
                        level.setBlock(cursor, Blocks.ICE.defaultBlockState(), 3);
                    }
                }
            }

            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 2 * 60 * 20, 4));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 90 * 20, 2));
            living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 3 * 60 * 20, 2));
        }

        bombStartStrength = blockRadius / 2;
    }

    public static void setEntitiesOnFire(Level level, double x, double y, double z, int radius) {
        if (level == null || level.isClientSide() || radius <= 0) {
            return;
        }

        AABB bounds = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        for (Entity entity : level.getEntities(null, bounds)) {
            if (entity.distanceToSqr(x, y, z) > radius * radius || hasAsbestosLikeProtection(entity)) {
                continue;
            }
            if (entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 15 * 20, 4));
            }
            entity.setSecondsOnFire(10);
        }
    }

    private static BlockState scorchCommon(Level level, BlockPos pos, BlockState state, boolean intense) {
        if (state.is(Blocks.GRASS_BLOCK) || isLegacy(state, "frozen_grass")) {
            return Blocks.DIRT.defaultBlockState();
        }
        if (state.is(Blocks.DIRT)) {
            return Blocks.NETHERRACK.defaultBlockState();
        }
        if (isLegacy(state, "frozen_dirt")) {
            return Blocks.DIRT.defaultBlockState();
        }
        if (intense && state.is(Blocks.NETHERRACK)) {
            return Blocks.LAVA.defaultBlockState();
        }
        if (state.is(BlockTags.LOGS) || isLegacy(state, "frozen_log")) {
            return legacyStateOr("waste_log", BaseFireBlock.getState(level, pos));
        }
        if (state.is(BlockTags.PLANKS) || isLegacy(state, "frozen_planks")) {
            return legacyStateOr("waste_planks", BaseFireBlock.getState(level, pos));
        }
        if (intense && (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.STONE_BRICKS) || state.is(Blocks.OBSIDIAN))) {
            return Blocks.LAVA.defaultBlockState();
        }
        if (state.is(BlockTags.LEAVES) || state.is(Blocks.WATER) || state.is(Blocks.ICE)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (state.is(Blocks.PACKED_ICE)) {
            return Blocks.WATER.defaultBlockState();
        }
        if (!intense && state.isFlammable(level, pos, Direction.UP)) {
            return BaseFireBlock.getState(level, pos);
        }
        return null;
    }

    private static void applyJaggedSphere(Level level, int x, int y, int z, int radius, BlockOperation operation) {
        if (level == null || level.isClientSide() || radius <= 0) {
            return;
        }

        int radiusSquared = radius * radius;
        int halfRadiusSquared = radiusSquared / 2;
        int jaggedBound = Math.max(1, halfRadiusSquared / 2);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -radius; xx < radius; xx++) {
            int worldX = xx + x;
            int xx2 = xx * xx;
            for (int yy = -radius; yy < radius; yy++) {
                int worldY = yy + y;
                int yy2 = xx2 + yy * yy;
                for (int zz = -radius; zz < radius; zz++) {
                    if (yy2 + zz * zz < halfRadiusSquared + level.random.nextInt(jaggedBound)) {
                        operation.apply(level, cursor.set(worldX, worldY, zz + z));
                    }
                }
            }
        }
    }

    private static boolean hasAsbestosLikeProtection(Entity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        for (ItemStack stack : player.getArmorSlots()) {
            if (!stack.isEmpty() && stack.getItem().getDescriptionId().toLowerCase(Locale.US).contains("asbestos")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isLegacy(BlockState state, String name) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(name);
        return block != null && state.is(block.get());
    }

    private static BlockState legacyStateOr(String name, BlockState fallback) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(name);
        return block == null ? fallback : block.get().defaultBlockState();
    }

    @FunctionalInterface
    private interface BlockOperation {
        void apply(Level level, BlockPos pos);
    }

    private ExplosionThermo() {
    }
}
