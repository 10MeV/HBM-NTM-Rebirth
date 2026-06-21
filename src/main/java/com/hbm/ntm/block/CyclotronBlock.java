package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.CyclotronBlockEntity;
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
public class CyclotronBlock extends LegacyVisibleMultiblockMachineBlock {
    public CyclotronBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CyclotronBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(resolveCoreBlockEntity(level, pos) instanceof CyclotronBlockEntity cyclotron)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() && cyclotron.tryInstallPlug(player, held)) {
            return InteractionResult.CONSUME;
        }
        if (!player.isShiftKeyDown()) {
            NetworkHooks.openScreen(serverPlayer, cyclotron, cyclotron.getBlockPos());
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.CYCLOTRON.get()) {
            return null;
        }
        return level.isClientSide
                ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        CyclotronBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (CyclotronBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof CyclotronBlockEntity cyclotron) {
            for (ItemStack stack : cyclotron.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
