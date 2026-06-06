package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.FluidValveBlock;
import com.hbm.ntm.block.HbmFluidNodeBlock;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FluidValveBlockEntity extends FluidPipeBlockEntity {
    public FluidValveBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.FLUID_VALVE.get(), pos, state);
    }

    protected FluidValveBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    @Override
    public void refreshFluidNode() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (!isOpen() || getFluidType() == HbmFluids.NONE) {
            removeFluidNode();
            return;
        }
        HbmFluidNode existing = getFluidNode();
        if (existing != null) {
            HbmFluidNodespace.destroyNode(level, worldPosition, getFluidType());
            node = null;
        }
        node = HbmFluidNodespace.createNode(level, createNode());
    }

    public boolean isOpen() {
        BlockState state = getBlockState();
        return !state.hasProperty(FluidValveBlock.OPEN) || state.getValue(FluidValveBlock.OPEN);
    }

    public void onValveStateChanged() {
        if (level == null || level.isClientSide) {
            return;
        }
        refreshFluidNode();
        if (getBlockState().getBlock() instanceof HbmFluidNodeBlock nodeBlock) {
            nodeBlock.updateFluidConnectionGraph(level, worldPosition);
        }
    }
}
