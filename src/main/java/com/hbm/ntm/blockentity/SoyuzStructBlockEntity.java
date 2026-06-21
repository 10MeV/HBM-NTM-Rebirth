package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class SoyuzStructBlockEntity extends BlockEntity {
    public SoyuzStructBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOYUZ_STRUCT.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-8, 0, -8), worldPosition.offset(8, 52, 10));
    }
}
