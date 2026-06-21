package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class HexafluorideTankBlockEntity extends BlockEntity {
    public HexafluorideTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEXAFLUORIDE_TANK.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(2.0D);
    }
}
