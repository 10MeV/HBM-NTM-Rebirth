package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.IndustrialSteamTurbineBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class SteamTurbineMultiblockBlock extends LegacyVisibleMultiblockMachineBlock implements EntityBlock {
    private final Kind kind;

    public SteamTurbineMultiblockBlock(Properties properties, LegacyMachineDefinition definition, Kind kind) {
        super(properties, definition);
        this.kind = kind;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IndustrialSteamTurbineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (kind == Kind.INDUSTRIAL && type == ModBlockEntities.INDUSTRIAL_STEAM_TURBINE.get()) {
            if (level.isClientSide) {
                return (tickLevel, tickPos, tickState, blockEntity) ->
                        IndustrialSteamTurbineBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (IndustrialSteamTurbineBlockEntity) blockEntity);
            }
            return (tickLevel, tickPos, tickState, blockEntity) ->
                    IndustrialSteamTurbineBlockEntity.serverTick(tickLevel, tickPos, tickState,
                            (IndustrialSteamTurbineBlockEntity) blockEntity);
        }
        return null;
    }

    public enum Kind {
        INDUSTRIAL
    }
}
