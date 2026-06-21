package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.FractionSpacerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FractionSpacerBlock extends LegacyVisibleMultiblockMachineBlock {
    public FractionSpacerBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FractionSpacerBlockEntity(pos, state);
    }
}
