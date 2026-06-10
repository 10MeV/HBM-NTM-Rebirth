package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.PileNeutronDetectorBlockEntity;
import com.hbm.ntm.neutron.PileGraphiteInsertionPlanner;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PileGraphiteNeutronDetectorBlock extends PileGraphiteDrilledBaseBlock {
    public PileGraphiteNeutronDetectorBlock(Properties properties) {
        super(properties, PileGraphiteInsertionPlanner.GraphiteBlockKind.DETECTOR);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PileNeutronDetectorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(
                type,
                ModBlockEntities.PILE_NEUTRON_DETECTOR.get(),
                PileNeutronDetectorBlockEntity::serverTick);
    }
}
