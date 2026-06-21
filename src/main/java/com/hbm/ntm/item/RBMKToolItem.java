package com.hbm.ntm.item;

import com.hbm.ntm.block.RBMKPanelBlock;
import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.blockentity.RBMKConsoleBlockEntity;
import com.hbm.ntm.blockentity.RBMKCraneConsoleBlockEntity;
import com.hbm.ntm.blockentity.RBMKPanelBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.neutron.RBMKPanelPlanner;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RBMKToolItem extends Item {
    private static final String TAG_POS_X = "posX";
    private static final String TAG_POS_Y = "posY";
    private static final String TAG_POS_Z = "posZ";

    public RBMKToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        BlockEntity blockEntity = MultiblockHelper.resolveOperationalCoreBlockEntity(level, clickedPos);
        BlockPos resolvedPos = blockEntity == null ? clickedPos : blockEntity.getBlockPos();
        if (level.getBlockState(resolvedPos).getBlock() instanceof RBMKPanelBlock panelBlock
                && panelBlock.panelType() == RBMKPanelPlanner.PanelType.DISPLAY
                && hasStoredTarget(stack)) {
            if (!level.isClientSide && blockEntity instanceof RBMKPanelBlockEntity panel) {
                if (!panel.setDisplayTarget(storedTarget(stack))) {
                    return InteractionResult.PASS;
                }
                sendStatus(context.getPlayer(), "item.rbmk_tool.set");
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (blockEntity instanceof RBMKCraneConsoleBlockEntity console && hasStoredTarget(stack)) {
            if (!level.isClientSide && !MultiblockHelper.ensureOperationalCoreLayoutComplete(level, resolvedPos)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                if (!console.setTarget(storedTarget(stack))) {
                    return InteractionResult.PASS;
                }
                sendStatus(context.getPlayer(), "item.rbmk_tool.set");
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (blockEntity instanceof RBMKConsoleBlockEntity console && hasStoredTarget(stack)) {
            if (!level.isClientSide && !MultiblockHelper.ensureOperationalCoreLayoutComplete(level, resolvedPos)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                if (!console.setTarget(storedTarget(stack))) {
                    return InteractionResult.PASS;
                }
                sendStatus(context.getPlayer(), "item.rbmk_tool.set");
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!(blockEntity instanceof RBMKColumnBlockEntity)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && !MultiblockHelper.ensureOperationalCoreLayoutComplete(level, resolvedPos)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            storeTarget(stack, resolvedPos);
            sendStatus(context.getPlayer(), "item.rbmk_tool.linked");
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.rbmk_tool.desc").withStyle(ChatFormatting.YELLOW));
        if (hasStoredTarget(stack)) {
            BlockPos pos = storedTarget(stack);
            tooltip.add(Component.literal("X: " + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ()));
        }
    }

    private static boolean hasStoredTarget(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null
                && tag.contains(TAG_POS_X, Tag.TAG_INT)
                && tag.contains(TAG_POS_Y, Tag.TAG_INT)
                && tag.contains(TAG_POS_Z, Tag.TAG_INT);
    }

    private static BlockPos storedTarget(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return new BlockPos(tag.getInt(TAG_POS_X), tag.getInt(TAG_POS_Y), tag.getInt(TAG_POS_Z));
    }

    private static void storeTarget(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_POS_X, pos.getX());
        tag.putInt(TAG_POS_Y, pos.getY());
        tag.putInt(TAG_POS_Z, pos.getZ());
    }

    private static void sendStatus(@Nullable Player player, String key) {
        if (player != null) {
            player.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.YELLOW));
        }
    }
}
