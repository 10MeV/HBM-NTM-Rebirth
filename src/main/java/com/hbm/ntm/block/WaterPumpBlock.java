package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.WaterPumpBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class WaterPumpBlock extends LegacyVisibleMultiblockMachineBlock {
    public WaterPumpBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WaterPumpBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.WATER_PUMP.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        WaterPumpBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (WaterPumpBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        WaterPumpBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (WaterPumpBlockEntity) blockEntity);
    }
}
