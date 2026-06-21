package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.ChimneyBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ChimneyBlock extends LegacyVisibleMultiblockMachineBlock {
    public ChimneyBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChimneyBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.CHIMNEY.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                ChimneyBlockEntity.clientTick(tickLevel, tickPos, tickState, (ChimneyBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                ChimneyBlockEntity.serverTick(tickLevel, tickPos, tickState, (ChimneyBlockEntity) blockEntity);
    }
}
