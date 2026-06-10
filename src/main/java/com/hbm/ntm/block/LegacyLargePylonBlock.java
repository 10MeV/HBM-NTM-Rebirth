package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LargePylonBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LegacyLargePylonBlock extends LegacyPylonBlock {
    private static final int[] LEGACY_DIMENSIONS = { 13, 0, 1, 1, 1, 1 };

    public LegacyLargePylonBlock(Properties properties) {
        super(properties, LEGACY_DIMENSIONS);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LargePylonBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<?> getPylonBlockEntityType() {
        return ModBlockEntities.RED_PYLON_LARGE.get();
    }
}
