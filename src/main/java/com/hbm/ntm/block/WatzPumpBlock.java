package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.WatzPumpBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WatzPumpBlock extends LegacyVisibleMultiblockMachineBlock {
    public WatzPumpBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WatzPumpBlockEntity(pos, state);
    }
}
