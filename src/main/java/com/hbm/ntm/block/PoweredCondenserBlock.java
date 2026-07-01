package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.PoweredCondenserBlockEntity;
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
public class PoweredCondenserBlock extends LegacyVisibleMultiblockMachineBlock {
    public PoweredCondenserBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PoweredCondenserBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.POWERED_CONDENSER.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        PoweredCondenserBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (PoweredCondenserBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        PoweredCondenserBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (PoweredCondenserBlockEntity) blockEntity);
    }
}
