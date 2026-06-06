package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LargeCoolingTowerBlockEntity;
import com.hbm.ntm.blockentity.SmallCoolingTowerBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class CoolingTowerBlock extends LegacyVisibleMultiblockMachineBlock {
    private final Kind kind;

    public CoolingTowerBlock(Properties properties, LegacyMachineDefinition definition, Kind kind) {
        super(properties, definition);
        this.kind = kind;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (kind) {
            case SMALL -> new SmallCoolingTowerBlockEntity(pos, state);
            case LARGE -> new LargeCoolingTowerBlockEntity(pos, state);
        };
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        if (kind == Kind.SMALL && type == ModBlockEntities.SMALL_COOLING_TOWER.get()) {
            return (tickLevel, tickPos, tickState, blockEntity) ->
                    SmallCoolingTowerBlockEntity.serverTick(tickLevel, tickPos, tickState,
                            (SmallCoolingTowerBlockEntity) blockEntity);
        }
        if (kind == Kind.LARGE && type == ModBlockEntities.LARGE_COOLING_TOWER.get()) {
            return (tickLevel, tickPos, tickState, blockEntity) ->
                    LargeCoolingTowerBlockEntity.serverTick(tickLevel, tickPos, tickState,
                            (LargeCoolingTowerBlockEntity) blockEntity);
        }
        return null;
    }

    public enum Kind {
        SMALL,
        LARGE
    }
}
