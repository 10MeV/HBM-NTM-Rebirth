package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.HeaterHeatexBlockEntity;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
public class HeaterHeatexBlock extends LegacyVisibleMultiblockMachineBlock {
    public HeaterHeatexBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeaterHeatexBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide
                && MultiblockHelper.resolveCoreBlockEntity(level, pos) instanceof HeaterHeatexBlockEntity heatex) {
            if (player.isShiftKeyDown()) {
                ItemStack held = player.getItemInHand(hand);
                var report = HbmFluidItemTransfer.setTankTypeFromIdentifierStackReport(
                        held, heatex.getInputTank(), level, heatex.getBlockPos());
                if (report.changed()) {
                    heatex.onIdentifierFluidChanged();
                    player.displayClientMessage(Component.literal("Changed type to ")
                            .withStyle(ChatFormatting.YELLOW)
                            .append(report.identifier().selectedType().getDisplayName())
                            .append(Component.literal("!").withStyle(ChatFormatting.YELLOW)), false);
                }
                return InteractionResult.SUCCESS;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, heatex, heatex.getBlockPos());
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.HEATER_HEATEX.get()) {
            return null;
        }
        return level.isClientSide
                ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        HeaterHeatexBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (HeaterHeatexBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof HeaterHeatexBlockEntity heatex) {
            for (ItemStack stack : heatex.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
