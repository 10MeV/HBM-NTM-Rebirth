package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.SteamEngineBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class SteamEngineBlock extends LegacyVisibleMultiblockMachineBlock {
    public SteamEngineBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SteamEngineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.STEAM_ENGINE.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                SteamEngineBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (SteamEngineBlockEntity) blockEntity);
    }
}
