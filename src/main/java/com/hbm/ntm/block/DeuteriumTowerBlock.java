package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.DeuteriumTowerBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class DeuteriumTowerBlock extends LegacyVisibleMultiblockMachineBlock {
    public DeuteriumTowerBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeuteriumTowerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.DEUTERIUM_TOWER.get()) {
            return null;
        }
        return level.isClientSide
                ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        DeuteriumTowerBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (DeuteriumTowerBlockEntity) blockEntity);
    }
}
