package com.hbm.ntm.item;

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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ItemCoordinateBase extends Item {
    private static final String TAG_POS_X = "posX";
    private static final String TAG_POS_Y = "posY";
    private static final String TAG_POS_Z = "posZ";

    protected ItemCoordinateBase(Properties properties) {
        super(properties);
    }

    @Nullable
    public static BlockPos getPosition(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_POS_X, Tag.TAG_INT) || !tag.contains(TAG_POS_Z, Tag.TAG_INT)) {
            return null;
        }
        return new BlockPos(tag.getInt(TAG_POS_X), tag.getInt(TAG_POS_Y), tag.getInt(TAG_POS_Z));
    }

    public static boolean hasPosition(ItemStack stack) {
        return getPosition(stack) != null;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        if (!canGrabCoordinateHere(level, clickedPos)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            BlockPos pos = getCoordinates(level, clickedPos);
            CompoundTag tag = context.getItemInHand().getOrCreateTag();
            tag.putInt(TAG_POS_X, pos.getX());
            if (includeY()) {
                tag.putInt(TAG_POS_Y, pos.getY());
            }
            tag.putInt(TAG_POS_Z, pos.getZ());
            onTargetSet(level, pos, context.getPlayer());
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public abstract boolean canGrabCoordinateHere(Level level, BlockPos pos);

    public boolean includeY() {
        return true;
    }

    public BlockPos getCoordinates(Level level, BlockPos pos) {
        return pos;
    }

    public void onTargetSet(Level level, BlockPos pos, @Nullable Player player) {
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        BlockPos pos = getPosition(stack);
        if (pos == null) {
            tooltip.add(Component.literal("No position set!").withStyle(ChatFormatting.RED));
            return;
        }
        tooltip.add(Component.literal("X: " + pos.getX()));
        if (includeY()) {
            tooltip.add(Component.literal("Y: " + pos.getY()));
        }
        tooltip.add(Component.literal("Z: " + pos.getZ()));
    }
}
