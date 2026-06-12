package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class MachineLpw2BlockEntity extends BlockEntity {
    public MachineLpw2BlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_LPW2.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return new AABB(pos.getX() - 10, pos.getY(), pos.getZ() - 10,
                pos.getX() + 11, pos.getY() + 7, pos.getZ() + 11);
    }
}
