package com.hbm.ntm.item;

import com.hbm.ntm.blockentity.TeleporterBlockEntity;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class TeleLinkItem extends Item {
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";
    private static final String TAG_DIM = "dim";

    public TeleLinkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        if (player == null || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        if (level.getBlockEntity(pos) instanceof TeleporterBlockEntity teleporter) {
            CompoundTag tag = stack.getTag();
            if (tag == null) {
                LegacySoundPlayer.playLegacyTechBoop(player, 1.0F, 1.0F);
                player.displayClientMessage(Component.literal("[TeleLink] No destination set!")
                        .withStyle(ChatFormatting.RED), false);
                return InteractionResult.FAIL;
            }
            teleporter.setTarget(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z), tag.getInt(TAG_DIM));
            LegacySoundPlayer.playLegacyTechBleep(player, 1.0F, 1.0F);
            player.displayClientMessage(Component.literal("[TeleLink] Teleporters destination has been set!")
                    .withStyle(ChatFormatting.AQUA), false);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.swing(context.getHand(), true);
            }
            return InteractionResult.SUCCESS;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_X, pos.getX());
        tag.putInt(TAG_Y, pos.getY());
        tag.putInt(TAG_Z, pos.getZ());
        tag.putInt(TAG_DIM, TeleporterBlockEntity.legacyDimensionId(level));
        LegacySoundPlayer.playLegacyTechBleep(player, 1.0F, 1.0F);
        player.displayClientMessage(Component.literal("[TeleLink] Set teleporter exit to "
                + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ".")
                .withStyle(ChatFormatting.AQUA), false);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.swing(context.getHand(), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            tooltip.add(Component.literal("Select exit location first!").withStyle(ChatFormatting.RED));
            return;
        }
        tooltip.add(Component.literal("X: " + tag.getInt(TAG_X)));
        tooltip.add(Component.literal("Y: " + tag.getInt(TAG_Y)));
        tooltip.add(Component.literal("Z: " + tag.getInt(TAG_Z)));
        tooltip.add(Component.literal("D: " + tag.getInt(TAG_DIM)));
    }
}
