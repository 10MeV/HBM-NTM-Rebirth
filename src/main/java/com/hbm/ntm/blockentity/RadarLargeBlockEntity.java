package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.entity.RadarContext;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class RadarLargeBlockEntity extends RadarBlockEntity {
    public RadarLargeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_RADAR_LARGE.get(), pos, state, RadarContext.LEGACY_LARGE_RANGE, 2,
                new AABB(-5.0D, 0.0D, -5.0D, 6.0D, 10.0D, 6.0D));
    }
}
