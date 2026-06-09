package com.hbm.ntm.item;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.RemoteDetonatableBlock;
import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DetonatorItem extends Item {
    private static final String X_KEY = "x";
    private static final String Y_KEY = "y";
    private static final String Z_KEY = "z";

    public DetonatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.detonator.set"));
        tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.detonator.trigger"));
        if (!hasLinkedPosition(stack)) {
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.detonator.no_position").withStyle(ChatFormatting.RED));
            return;
        }
        BlockPos pos = linkedPosition(stack);
        tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.detonator.linked", pos.getX(), pos.getY(), pos.getZ())
                .withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(X_KEY, pos.getX());
        tag.putInt(Y_KEY, pos.getY());
        tag.putInt(Z_KEY, pos.getZ());

        Level level = context.getLevel();
        level.playSound(null, player.blockPosition(), ModSounds.TOOL_TECH_BOOP.get(), SoundSource.PLAYERS, 2.0F, 1.0F);
        if (!level.isClientSide()) {
            player.displayClientMessage(prefixed(Component.translatable("msg.hbm_ntm_rebirth.detonator.position_set")
                    .withStyle(ChatFormatting.GREEN)), false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!hasLinkedPosition(stack)) {
            if (!level.isClientSide()) {
                player.displayClientMessage(prefixed(Component.translatable("msg.hbm_ntm_rebirth.detonator.no_position")
                        .withStyle(ChatFormatting.RED)), false);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        BlockPos pos = linkedPosition(stack);
        Block block = level.getBlockState(pos).getBlock();
        if (block instanceof RemoteDetonatableBlock detonatable) {
            level.playSound(null, player.blockPosition(), ModSounds.TOOL_TECH_BLEEP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!level.isClientSide()) {
                RemoteDetonatableBlock.BombReturnCode code = detonatable.detonateFromRemote(level, pos);
                if (extendedLoggingEnabled()) {
                    HbmNtm.LOGGER.info("[DET] Tried to detonate block at {} / {} / {} by {}.",
                            pos.getX(), pos.getY(), pos.getZ(), player.getGameProfile().getName());
                }
                player.displayClientMessage(prefixed(Component.translatable(code.translationKey())
                        .withStyle(code.wasSuccessful() ? ChatFormatting.YELLOW : ChatFormatting.RED)), false);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (!level.isClientSide()) {
            player.displayClientMessage(prefixed(Component.translatable(RemoteDetonatableBlock.BombReturnCode.ERROR_NO_BOMB.translationKey())
                    .withStyle(ChatFormatting.RED)), false);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static boolean hasLinkedPosition(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null
                && tag.contains(X_KEY, Tag.TAG_INT)
                && tag.contains(Y_KEY, Tag.TAG_INT)
                && tag.contains(Z_KEY, Tag.TAG_INT);
    }

    private static BlockPos linkedPosition(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return new BlockPos(tag.getInt(X_KEY), tag.getInt(Y_KEY), tag.getInt(Z_KEY));
    }

    private static MutableComponent prefixed(Component message) {
        return Component.literal("[")
                .withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.translatable("item.hbm_ntm_rebirth.detonator").withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_AQUA))
                .append(message);
    }

    private static boolean extendedLoggingEnabled() {
        return HbmCommonConfig.extendedLoggingEnabled();
    }
}
