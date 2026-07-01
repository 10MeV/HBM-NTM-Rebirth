package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LegacyGenericSelectorMachineBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class LegacyGenericSelectorMachineBlock extends LegacyVisibleMultiblockMachineBlock {
    private final LegacyGenericSelectorMachineBlockEntity.Kind kind;

    public LegacyGenericSelectorMachineBlock(Properties properties, LegacyMachineDefinition definition,
            LegacyGenericSelectorMachineBlockEntity.Kind kind) {
        super(properties, definition);
        this.kind = kind;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LegacyGenericSelectorMachineBlockEntity(pos, state, kind);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && MultiblockHelper.resolveCoreBlockEntity(level, pos) instanceof LegacyGenericSelectorMachineBlockEntity machine) {
            NetworkHooks.openScreen(serverPlayer, machine, machine.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.LEGACY_GENERIC_SELECTOR_MACHINE.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                LegacyGenericSelectorMachineBlockEntity.clientTick(tickLevel, tickPos, tickState,
                        (LegacyGenericSelectorMachineBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                LegacyGenericSelectorMachineBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (LegacyGenericSelectorMachineBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof LegacyGenericSelectorMachineBlockEntity machine) {
            for (ItemStack stack : machine.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
