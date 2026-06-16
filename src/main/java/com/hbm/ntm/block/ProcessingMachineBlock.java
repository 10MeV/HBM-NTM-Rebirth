package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.ProcessingMachineBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
public class ProcessingMachineBlock extends LegacyVisibleMultiblockMachineBlock {
    private final ProcessingMachineBlockEntity.Kind kind;

    public ProcessingMachineBlock(Properties properties, LegacyMachineDefinition definition,
            ProcessingMachineBlockEntity.Kind kind) {
        super(properties, definition);
        this.kind = kind;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ProcessingMachineBlockEntity(pos, state, kind);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && resolveCoreBlockEntity(level, pos) instanceof ProcessingMachineBlockEntity machine) {
            NetworkHooks.openScreen(serverPlayer, machine, machine.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.PROCESSING_MACHINE.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                ProcessingMachineBlockEntity.clientTick(tickLevel, tickPos, tickState,
                        (ProcessingMachineBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                ProcessingMachineBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (ProcessingMachineBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ProcessingMachineBlockEntity machine) {
            for (ItemStack stack : machine.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
