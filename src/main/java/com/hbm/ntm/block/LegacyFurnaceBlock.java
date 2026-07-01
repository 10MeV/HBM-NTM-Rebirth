package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LegacyFurnaceBlockEntity;
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
public class LegacyFurnaceBlock extends LegacyVisibleMultiblockMachineBlock {
    private final LegacyFurnaceBlockEntity.Kind kind;

    public LegacyFurnaceBlock(Properties properties, LegacyMachineDefinition definition,
            LegacyFurnaceBlockEntity.Kind kind) {
        super(properties, definition);
        this.kind = kind;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LegacyFurnaceBlockEntity(pos, state, kind);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && resolveCoreBlockEntity(level, pos) instanceof LegacyFurnaceBlockEntity furnace) {
            NetworkHooks.openScreen(serverPlayer, furnace, furnace.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.LEGACY_FURNACE.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                LegacyFurnaceBlockEntity.clientTick(tickLevel, tickPos, tickState,
                        (LegacyFurnaceBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                LegacyFurnaceBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (LegacyFurnaceBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof LegacyFurnaceBlockEntity furnace) {
            for (ItemStack stack : furnace.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
