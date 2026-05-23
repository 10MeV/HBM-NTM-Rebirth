package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

@SuppressWarnings("deprecation")
public class LegacyWasteLeavesBlock extends LeavesBlock {
    public LegacyWasteLeavesBlock(Properties properties) {
        super(properties.randomTicks());
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(30) == 0) {
            level.removeBlock(pos, false);
            if (level.getBlockState(pos.below()).isAir()) {
                BlockState fallingState = ModBlocks.LEAVES_LAYER.get().defaultBlockState();
                FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, fallingState);
                falling.time = 2;
                falling.dropItem = false;
            }
        }
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, net.minecraft.core.Direction direction) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, direction);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return !state.getValue(WATERLOGGED);
    }

    @Override
    public net.minecraft.world.level.material.FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(7) == 0 && level.getBlockState(pos.below()).isAir()) {
            level.addParticle(ModParticleTypes.DEAD_LEAF.get(),
                    pos.getX() + random.nextDouble(),
                    pos.getY() - 0.05D,
                    pos.getZ() + random.nextDouble(),
                    0.0D,
                    0.0D,
                    0.0D);
        }
    }
}
