package com.hbm.ntm.item;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.RemoteDetonatableBlock;
import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Source-backed port of 1.7.10 {@code ItemMultiDetonator}. */
public final class MultiDetonatorItem extends Item {
    private static final String X_VALUES = "xValues";
    private static final String Y_VALUES = "yValues";
    private static final String Z_VALUES = "zValues";

    public MultiDetonatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Shift right-click block to add position,"));
        tooltip.add(Component.literal("right-click to detonate!"));
        tooltip.add(Component.literal("Shift right-click in the air to clear positions."));
        int[][] locations = locations(stack);
        if (locations == null) {
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.detonator.no_position").withStyle(ChatFormatting.RED));
            return;
        }
        for (int i = 0; i < locations[0].length; i++) {
            tooltip.add(Component.literal(locations[0][i] + " / " + locations[1][i] + " / " + locations[2][i])
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        addLocation(context.getItemInHand(), pos);
        LegacySoundPlayer.playLegacyTechBoop(player, 2.0F, 1.0F);
        if (!context.getLevel().isClientSide()) {
            player.displayClientMessage(prefixed(Component.literal("Position added!").withStyle(ChatFormatting.GREEN)), false);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int[][] locations = locations(stack);
        if (locations == null) {
            if (!level.isClientSide()) {
                player.displayClientMessage(prefixed(Component.translatable("msg.hbm_ntm_rebirth.detonator.no_position")
                        .withStyle(ChatFormatting.RED)), false);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (player.isShiftKeyDown()) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putIntArray(X_VALUES, new int[0]);
            tag.putIntArray(Y_VALUES, new int[0]);
            tag.putIntArray(Z_VALUES, new int[0]);
            LegacySoundPlayer.playLegacyTechBoop(player, 2.0F, 1.0F);
            if (!level.isClientSide()) {
                player.displayClientMessage(prefixed(Component.literal("Locations cleared!").withStyle(ChatFormatting.RED)), false);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        int successes = 0;
        if (!level.isClientSide()) {
            for (int i = 0; i < locations[0].length; i++) {
                BlockPos pos = new BlockPos(locations[0][i], locations[1][i], locations[2][i]);
                if (level.getBlockState(pos).getBlock() instanceof RemoteDetonatableBlock detonatable) {
                    if (detonatable.detonateFromRemote(level, pos).wasSuccessful()) {
                        successes++;
                    }
                    if (HbmCommonConfig.extendedLoggingEnabled()) {
                        HbmNtm.LOGGER.info("[DET] Tried to detonate block at {} / {} / {} by {}.", pos.getX(), pos.getY(),
                                pos.getZ(), player.getGameProfile().getName());
                    }
                }
            }
            player.displayClientMessage(prefixed(Component.literal("Triggered " + successes + "/" + locations[0].length + "!")
                    .withStyle(ChatFormatting.YELLOW)), false);
        }
        LegacySoundPlayer.playLegacyTechBleep(player, 1.0F, 1.0F);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static void addLocation(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        int[] xs = tag.getIntArray(X_VALUES);
        int[] ys = tag.getIntArray(Y_VALUES);
        int[] zs = tag.getIntArray(Z_VALUES);
        tag.putIntArray(X_VALUES, append(xs, pos.getX()));
        tag.putIntArray(Y_VALUES, append(ys, pos.getY()));
        tag.putIntArray(Z_VALUES, append(zs, pos.getZ()));
    }

    @Nullable
    private static int[][] locations(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return null;
        }
        int[] xs = tag.getIntArray(X_VALUES);
        int[] ys = tag.getIntArray(Y_VALUES);
        int[] zs = tag.getIntArray(Z_VALUES);
        if (xs.length == 0 || ys.length == 0 || zs.length == 0) {
            return null;
        }
        int length = Math.min(xs.length, Math.min(ys.length, zs.length));
        if (length == 0) {
            return null;
        }
        return new int[][] { trim(xs, length), trim(ys, length), trim(zs, length) };
    }

    private static int[] append(int[] values, int value) {
        int[] appended = java.util.Arrays.copyOf(values, values.length + 1);
        appended[values.length] = value;
        return appended;
    }

    private static int[] trim(int[] values, int length) {
        return values.length == length ? values : java.util.Arrays.copyOf(values, length);
    }

    private static MutableComponent prefixed(Component message) {
        return Component.literal("[").withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.translatable("item.hbm_ntm_rebirth.detonator_multi").withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_AQUA))
                .append(message);
    }
}
