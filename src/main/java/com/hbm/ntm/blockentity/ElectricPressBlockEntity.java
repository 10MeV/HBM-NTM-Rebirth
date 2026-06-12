package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ElectricPressBlockEntity extends BlockEntity {
    public ElectricPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_PRESS.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return LegacyMachineRenderBounds.visibleMultiblockOr(this, super.getRenderBoundingBox());
    }
}
