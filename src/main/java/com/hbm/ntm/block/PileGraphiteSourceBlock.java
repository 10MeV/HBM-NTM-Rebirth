package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.PileSourceBlockEntity;
import com.hbm.ntm.neutron.PileGraphiteInsertionPlanner;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PileGraphiteSourceBlock extends PileGraphiteDrilledBaseBlock {
    public PileGraphiteSourceBlock(Properties properties, PileGraphiteInsertionPlanner.GraphiteBlockKind graphiteKind) {
        super(properties, graphiteKind);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PileSourceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.PILE_SOURCE.get(), PileSourceBlockEntity::serverTick);
    }
}
