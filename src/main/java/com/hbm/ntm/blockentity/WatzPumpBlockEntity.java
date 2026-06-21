package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class WatzPumpBlockEntity extends BlockEntity {
    public WatzPumpBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WATZ_PUMP.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 2, 2));
    }
}
