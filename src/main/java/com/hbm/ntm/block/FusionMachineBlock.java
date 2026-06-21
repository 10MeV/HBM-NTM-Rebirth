package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.FusionBoilerBlockEntity;
import com.hbm.ntm.blockentity.FusionBreederBlockEntity;
import com.hbm.ntm.blockentity.FusionCollectorBlockEntity;
import com.hbm.ntm.blockentity.FusionCouplerBlockEntity;
import com.hbm.ntm.blockentity.FusionKlystronBlockEntity;
import com.hbm.ntm.blockentity.FusionKlystronCreativeBlockEntity;
import com.hbm.ntm.blockentity.FusionMHDTBlockEntity;
import com.hbm.ntm.blockentity.FusionPlasmaForgeBlockEntity;
import com.hbm.ntm.blockentity.FusionTorusBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FusionMachineBlock extends LegacyVisibleMultiblockMachineBlock {
    private final Kind kind;

    public FusionMachineBlock(Properties properties, LegacyMachineDefinition definition, Kind kind) {
        super(properties, definition);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (kind) {
            case TORUS -> new FusionTorusBlockEntity(pos, state);
            case KLYSTRON -> new FusionKlystronBlockEntity(pos, state);
            case KLYSTRON_CREATIVE -> new FusionKlystronCreativeBlockEntity(pos, state);
            case BREEDER -> new FusionBreederBlockEntity(pos, state);
            case COLLECTOR -> new FusionCollectorBlockEntity(pos, state);
            case BOILER -> new FusionBoilerBlockEntity(pos, state);
            case COUPLER -> new FusionCouplerBlockEntity(pos, state);
            case MHDT -> new FusionMHDTBlockEntity(pos, state);
            case PLASMA_FORGE -> new FusionPlasmaForgeBlockEntity(pos, state);
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity resolved = MultiblockHelper.resolveCoreBlockEntity(level, pos);
            if (resolved instanceof MenuProvider menuProvider) {
                NetworkHooks.openScreen(serverPlayer, menuProvider, resolved.getBlockPos());
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (kind == Kind.TORUS && type == ModBlockEntities.FUSION_TORUS.get()) {
            return level.isClientSide
                    ? (tickLevel, tickPos, tickState, blockEntity) -> FusionTorusBlockEntity.clientTick(
                            tickLevel, tickPos, tickState, (FusionTorusBlockEntity) blockEntity)
                    : (tickLevel, tickPos, tickState, blockEntity) -> FusionTorusBlockEntity.serverTick(
                            tickLevel, tickPos, tickState, (FusionTorusBlockEntity) blockEntity);
        }
        if (kind == Kind.KLYSTRON && type == ModBlockEntities.FUSION_KLYSTRON.get()) {
            return level.isClientSide
                    ? (tickLevel, tickPos, tickState, blockEntity) -> FusionKlystronBlockEntity.clientTick(
                            tickLevel, tickPos, tickState, (FusionKlystronBlockEntity) blockEntity)
                    : (tickLevel, tickPos, tickState, blockEntity) -> FusionKlystronBlockEntity.serverTick(
                            tickLevel, tickPos, tickState, (FusionKlystronBlockEntity) blockEntity);
        }
        if (kind == Kind.KLYSTRON_CREATIVE && type == ModBlockEntities.FUSION_KLYSTRON_CREATIVE.get()) {
            return level.isClientSide
                    ? (tickLevel, tickPos, tickState, blockEntity) -> FusionKlystronCreativeBlockEntity.clientTick(
                            tickLevel, tickPos, tickState, (FusionKlystronCreativeBlockEntity) blockEntity)
                    : (tickLevel, tickPos, tickState, blockEntity) -> FusionKlystronCreativeBlockEntity.serverTick(
                            tickLevel, tickPos, tickState, (FusionKlystronCreativeBlockEntity) blockEntity);
        }
        if (!level.isClientSide && kind == Kind.BREEDER && type == ModBlockEntities.FUSION_BREEDER.get()) {
            return (tickLevel, tickPos, tickState, blockEntity) -> FusionBreederBlockEntity.serverTick(
                    tickLevel, tickPos, tickState, (FusionBreederBlockEntity) blockEntity);
        }
        if (!level.isClientSide && kind == Kind.COLLECTOR && type == ModBlockEntities.FUSION_COLLECTOR.get()) {
            return (tickLevel, tickPos, tickState, blockEntity) -> FusionCollectorBlockEntity.serverTick(
                    tickLevel, tickPos, tickState, (FusionCollectorBlockEntity) blockEntity);
        }
        if (!level.isClientSide && kind == Kind.BOILER && type == ModBlockEntities.FUSION_BOILER.get()) {
            return (tickLevel, tickPos, tickState, blockEntity) -> FusionBoilerBlockEntity.serverTick(
                    tickLevel, tickPos, tickState, (FusionBoilerBlockEntity) blockEntity);
        }
        if (!level.isClientSide && kind == Kind.COUPLER && type == ModBlockEntities.FUSION_COUPLER.get()) {
            return (tickLevel, tickPos, tickState, blockEntity) -> FusionCouplerBlockEntity.serverTick(
                    tickLevel, tickPos, tickState, (FusionCouplerBlockEntity) blockEntity);
        }
        if (kind == Kind.MHDT && type == ModBlockEntities.FUSION_MHDT.get()) {
            return level.isClientSide
                    ? (tickLevel, tickPos, tickState, blockEntity) -> FusionMHDTBlockEntity.clientTick(
                            tickLevel, tickPos, tickState, (FusionMHDTBlockEntity) blockEntity)
                    : (tickLevel, tickPos, tickState, blockEntity) -> FusionMHDTBlockEntity.serverTick(
                            tickLevel, tickPos, tickState, (FusionMHDTBlockEntity) blockEntity);
        }
        if (kind == Kind.PLASMA_FORGE && type == ModBlockEntities.FUSION_PLASMA_FORGE.get()) {
            return level.isClientSide
                    ? (tickLevel, tickPos, tickState, blockEntity) -> FusionPlasmaForgeBlockEntity.clientTick(
                            tickLevel, tickPos, tickState, (FusionPlasmaForgeBlockEntity) blockEntity)
                    : (tickLevel, tickPos, tickState, blockEntity) -> FusionPlasmaForgeBlockEntity.serverTick(
                            tickLevel, tickPos, tickState, (FusionPlasmaForgeBlockEntity) blockEntity);
        }
        return null;
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FusionTorusBlockEntity torus) {
            for (ItemStack stack : torus.getDrops()) Block.popResource(level, pos, stack);
        } else if (blockEntity instanceof FusionKlystronBlockEntity klystron) {
            for (ItemStack stack : klystron.getDrops()) Block.popResource(level, pos, stack);
        } else if (blockEntity instanceof FusionBreederBlockEntity breeder) {
            for (ItemStack stack : breeder.getDrops()) Block.popResource(level, pos, stack);
        } else if (blockEntity instanceof FusionPlasmaForgeBlockEntity forge) {
            for (ItemStack stack : forge.getDrops()) Block.popResource(level, pos, stack);
        }
    }

    public enum Kind {
        TORUS,
        KLYSTRON,
        KLYSTRON_CREATIVE,
        BREEDER,
        COLLECTOR,
        BOILER,
        COUPLER,
        MHDT,
        PLASMA_FORGE
    }
}
