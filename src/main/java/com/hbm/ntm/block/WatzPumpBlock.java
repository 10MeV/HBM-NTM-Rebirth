package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.WatzPumpBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class WatzPumpBlock extends LegacyVisibleMultiblockMachineBlock {
    private static final VoxelShape TOP_DUMMY_SUPPORT = Shapes.box(0.0D, 0.875D, 0.0D, 1.0D, 1.0D, 1.0D);

    public WatzPumpBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WatzPumpBlockEntity(pos, state);
    }

    @Override
    public boolean usesMultiblockDummySupportShapeOverride(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public VoxelShape getMultiblockDummySupportShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos) {
        return dummyPos.equals(corePos.above()) ? TOP_DUMMY_SUPPORT : Shapes.empty();
    }
}
