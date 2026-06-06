package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class LegacyLanternBlockEntity extends BlockEntity {
    public LegacyLanternBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_LANTERN.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 6, 1));
    }
}
