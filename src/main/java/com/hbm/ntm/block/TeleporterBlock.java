package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.TeleporterBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class TeleporterBlock extends Block implements EntityBlock {
    public TeleporterBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TeleporterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.TELEPORTER.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                TeleporterBlockEntity.clientTick(tickLevel, tickPos, tickState,
                        (TeleporterBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                TeleporterBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (TeleporterBlockEntity) blockEntity);
    }
}
