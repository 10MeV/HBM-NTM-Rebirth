package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;

@SuppressWarnings("deprecation")
public class LegacyBurningEarthBlock extends Block {
    public LegacyBurningEarthBlock(Properties properties) {
        super(properties.randomTicks());
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (entity instanceof LivingEntity living) {
            living.setSecondsOnFire(5);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        double x = pos.getX() + random.nextFloat();
        double y = pos.getY() + 1.1F;
        double z = pos.getZ() + random.nextFloat();
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (random.nextInt(5) == 0) {
            for (BlockPos target : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                if (!level.isLoaded(target) || level.isOutsideBuildHeight(target)) {
                    continue;
                }

                BlockState targetState = level.getBlockState(target);
                BlockPos targetAbove = target.above();
                BlockState targetAboveState = level.getBlockState(targetAbove);

                if (!targetAboveState.isSolidRender(level, targetAbove)
                        && isLegacyBurningEarthSpreadTarget(targetState)
                        && !level.isRainingAt(pos)) {
                    level.setBlock(target, ModBlocks.BURNING_EARTH.get().defaultBlockState(), Block.UPDATE_ALL);
                }
                if (isLegacyLeavesOrBush(targetState)) {
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
                if (targetState.is(ModBlocks.FROZEN_DIRT.get())) {
                    level.setBlock(target, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
                }
                if (targetAboveState.isFlammable(level, pos, Direction.UP)
                        && !isLegacyLeavesOrBush(targetAboveState)
                        && level.getBlockState(pos.above()).isAir()) {
                    level.setBlock(pos.above(), Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        level.setBlock(pos, ModBlocks.IMPACT_DIRT.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos,
            boolean moving) {
        super.neighborChanged(state, level, pos, block, neighborPos, moving);
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.getBlock() instanceof LiquidBlock || !aboveState.getFluidState().isEmpty()
                || aboveState.isSolidRender(level, above)) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing,
            IPlantable plantable) {
        return false;
    }

    public static boolean isLegacyPlantDeathTarget(BlockState state) {
        return isLegacyLeavesOrBush(state)
                || state.getBlock() instanceof VineBlock;
    }

    public static boolean isLegacyLeavesOrBush(BlockState state) {
        return state.is(BlockTags.LEAVES)
                || state.getBlock() instanceof BushBlock;
    }

    public static boolean isLegacyBurningEarthSpreadTarget(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.MYCELIUM)
                || state.is(ModBlocks.WASTE_EARTH.get())
                || state.is(ModBlocks.FROZEN_GRASS.get())
                || state.is(ModBlocks.WASTE_MYCELIUM.get());
    }
}
