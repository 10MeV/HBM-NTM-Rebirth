package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.PileBreedingFuelBlockEntity;
import com.hbm.ntm.neutron.PileGraphiteInsertionPlanner;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PileGraphiteBreedingFuelBlock extends PileGraphiteDrilledBaseBlock {
    public PileGraphiteBreedingFuelBlock(Properties properties) {
        super(properties, PileGraphiteInsertionPlanner.GraphiteBlockKind.LITHIUM);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PileBreedingFuelBlockEntity(pos, state);
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
                ModBlockEntities.PILE_BREEDING_FUEL.get(),
                PileBreedingFuelBlockEntity::serverTick);
    }
}
