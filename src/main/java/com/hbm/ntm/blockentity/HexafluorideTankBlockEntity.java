package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class HexafluorideTankBlockEntity extends BlockEntity {
    private static final int RENDER_MIN_XZ = -1;
    private static final int RENDER_MAX_XZ = 2;
    private static final int RENDER_MAX_Y = 3;

    public HexafluorideTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEXAFLUORIDE_TANK.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.offset(RENDER_MIN_XZ, 0, RENDER_MIN_XZ),
                worldPosition.offset(RENDER_MAX_XZ, RENDER_MAX_Y, RENDER_MAX_XZ));
    }
}
