package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class LegacyVisibleMachineBlockEntity extends BlockEntity {
    public LegacyVisibleMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_VISIBLE_MACHINE.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockState state = getBlockState();
        if (state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block) {
            return block.definition().renderBoundingBox(state, worldPosition);
        }
        return super.getRenderBoundingBox();
    }
}
