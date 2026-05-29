package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.CatalyticCrackerBlockEntity;
import com.hbm.ntm.blockentity.CatalyticReformerBlockEntity;
import com.hbm.ntm.blockentity.CokerBlockEntity;
import com.hbm.ntm.blockentity.FractionTowerBlockEntity;
import com.hbm.ntm.blockentity.HydrotreaterBlockEntity;
import com.hbm.ntm.blockentity.VacuumDistillBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class RemoteFluidMachineBlock extends LegacyVisibleMultiblockMachineBlock {
    private final Kind kind;

    public RemoteFluidMachineBlock(Properties properties, LegacyMachineDefinition definition, Kind kind) {
        super(properties, definition);
        this.kind = kind;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (kind) {
            case CATALYTIC_CRACKER -> new CatalyticCrackerBlockEntity(pos, state);
            case CATALYTIC_REFORMER -> new CatalyticReformerBlockEntity(pos, state);
            case VACUUM_DISTILL -> new VacuumDistillBlockEntity(pos, state);
            case FRACTION_TOWER -> new FractionTowerBlockEntity(pos, state);
            case HYDROTREATER -> new HydrotreaterBlockEntity(pos, state);
            case COKER -> new CokerBlockEntity(pos, state);
        };
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        if (kind == Kind.CATALYTIC_CRACKER && type == ModBlockEntities.CATALYTIC_CRACKER.get()) {
            return (tickLevel, tickPos, tickState, blockEntity) ->
                    CatalyticCrackerBlockEntity.serverTick(tickLevel, tickPos, tickState,
                            (CatalyticCrackerBlockEntity) blockEntity);
        }
        if (kind == Kind.CATALYTIC_REFORMER && type == ModBlockEntities.CATALYTIC_REFORMER.get()) {
            return (tickLevel, tickPos, tickState, blockEntity) ->
                    CatalyticReformerBlockEntity.serverTick(tickLevel, tickPos, tickState,
                            (CatalyticReformerBlockEntity) blockEntity);
        }
        if (kind == Kind.VACUUM_DISTILL && type == ModBlockEntities.VACUUM_DISTILL.get()) {
            return (tickLevel, tickPos, tickState, blockEntity) ->
                    VacuumDistillBlockEntity.serverTick(tickLevel, tickPos, tickState,
                            (VacuumDistillBlockEntity) blockEntity);
        }
        if (kind == Kind.FRACTION_TOWER && type == ModBlockEntities.FRACTION_TOWER.get()) {
            return (tickLevel, tickPos, tickState, blockEntity) ->
                    FractionTowerBlockEntity.serverTick(tickLevel, tickPos, tickState,
                            (FractionTowerBlockEntity) blockEntity);
        }
        if (kind == Kind.HYDROTREATER && type == ModBlockEntities.HYDROTREATER.get()) {
            return (tickLevel, tickPos, tickState, blockEntity) ->
                    HydrotreaterBlockEntity.serverTick(tickLevel, tickPos, tickState,
                            (HydrotreaterBlockEntity) blockEntity);
        }
        if (kind == Kind.COKER && type == ModBlockEntities.COKER.get()) {
            return (tickLevel, tickPos, tickState, blockEntity) ->
                    CokerBlockEntity.serverTick(tickLevel, tickPos, tickState,
                            (CokerBlockEntity) blockEntity);
        }
        return null;
    }

    public enum Kind {
        CATALYTIC_CRACKER,
        CATALYTIC_REFORMER,
        VACUUM_DISTILL,
        FRACTION_TOWER,
        HYDROTREATER,
        COKER
    }
}
