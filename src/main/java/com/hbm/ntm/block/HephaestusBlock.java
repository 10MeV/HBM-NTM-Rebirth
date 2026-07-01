package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.HephaestusBlockEntity;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class HephaestusBlock extends LegacyVisibleMultiblockMachineBlock {
    public HephaestusBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HephaestusBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown()
                && resolveCoreBlockEntity(level, pos) instanceof HephaestusBlockEntity hephaestus) {
            ItemStack held = player.getItemInHand(hand);
            var report = HbmFluidItemTransfer.setTankTypeFromIdentifierStackReport(
                    held, hephaestus.getInputTank(), level, hephaestus.getBlockPos());
            if (!report.identifier().identifierItem() || report.identifier().selectedNone()) {
                return InteractionResult.PASS;
            }
            if (report.changed()) {
                hephaestus.onIdentifierFluidChanged();
                player.displayClientMessage(Component.literal("Changed type to ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(report.identifier().selectedType().getDisplayName())
                        .append(Component.literal("!").withStyle(ChatFormatting.YELLOW)), false);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.HEPHAESTUS.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        HephaestusBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (HephaestusBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        HephaestusBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (HephaestusBlockEntity) blockEntity);
    }
}
