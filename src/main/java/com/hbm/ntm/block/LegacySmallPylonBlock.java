package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.HbmLegacyWireNodeBlockEntity;
import com.hbm.ntm.blockentity.SmallPylonBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.ColorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LegacySmallPylonBlock extends HbmLegacyWireNodeBlock {
    public LegacySmallPylonBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmallPylonBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.RED_PYLON.get()) {
            return null;
        }
        return level.isClientSide ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        HbmLegacyWireNodeBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (HbmLegacyWireNodeBlockEntity) blockEntity);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer) || !(level.getBlockEntity(pos) instanceof HbmLegacyWireNodeBlockEntity pylon)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        int color = ColorUtil.getColorFromDye(stack);
        if (!pylon.setWireColor(color)) {
            return InteractionResult.PASS;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        appendLegacyWireTooltip(tooltip, "Single", 25);
    }
}
