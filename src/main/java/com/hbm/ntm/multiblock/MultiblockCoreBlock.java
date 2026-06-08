package com.hbm.ntm.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Marker for blocks that own dummy blocks placed by {@link MultiblockHelper}.
 */
public interface MultiblockCoreBlock {
    @Nullable
    default LegacyMultiblockLayout getMultiblockLayout(BlockState state, BlockGetter level, BlockPos corePos) {
        return null;
    }

    default boolean ownsMultiblockDummy(BlockState state, BlockGetter level, BlockPos corePos, BlockPos dummyPos) {
        LegacyMultiblockLayout layout = getMultiblockLayout(state, level, corePos);
        if (layout != null) {
            return layout.containsOffset(dummyPos.subtract(corePos));
        }
        return true;
    }

    default VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos, CollisionContext context) {
        return Shapes.block();
    }

    default VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return getMultiblockShape(state, level, corePos, context);
    }

    default boolean usesForwardedDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return false;
    }

    default boolean usesForwardedDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return false;
    }

    default BlockState multiblockParticleState(BlockState state, BlockGetter level, BlockPos corePos) {
        return MultiblockHelper.steelParticleState();
    }
}
