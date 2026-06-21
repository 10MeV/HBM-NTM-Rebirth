package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.SolarBoilerBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class SolarBoilerBlock extends LegacyVisibleMultiblockMachineBlock {
    public SolarBoilerBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarBoilerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.SOLAR_BOILER.get()) {
            return null;
        }
        if (level.isClientSide) {
            return (tickLevel, tickPos, tickState, blockEntity) ->
                    SolarBoilerBlockEntity.clientTick(tickLevel, tickPos, tickState,
                            (SolarBoilerBlockEntity) blockEntity);
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                SolarBoilerBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (SolarBoilerBlockEntity) blockEntity);
    }
}
