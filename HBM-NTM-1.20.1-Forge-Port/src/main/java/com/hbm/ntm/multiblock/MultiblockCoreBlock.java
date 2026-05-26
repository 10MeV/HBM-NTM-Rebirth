package com.hbm.ntm.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Marker for blocks that own dummy blocks placed by {@link MultiblockHelper}.
 */
public interface MultiblockCoreBlock {
    default boolean ownsMultiblockDummy(BlockState state, BlockGetter level, BlockPos corePos, BlockPos dummyPos) {
        return true;
    }

    default VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos, CollisionContext context) {
        return Shapes.block();
    }

    default VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return getMultiblockShape(state, level, corePos, context);
    }
}
