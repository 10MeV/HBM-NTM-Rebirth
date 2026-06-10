package com.hbm.ntm.item;

import com.hbm.ntm.energy.HbmLegacyPowerNodeShapes;
import com.hbm.ntm.energy.HbmLegacyWireNode;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LegacyWiringItem extends Item {
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";

    public LegacyWiringItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        WireNodeLookup target = resolveWireNode(level, clickedPos);
        if (target == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getTag();
        if (!hasStoredPos(tag)) {
            tag = stack.getOrCreateTag();
            tag.putInt(TAG_X, target.pos().getX());
            tag.putInt(TAG_Y, target.pos().getY());
            tag.putInt(TAG_Z, target.pos().getZ());
            player.sendSystemMessage(Component.literal("Wire start"));
        } else {
            BlockPos startPos = new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z));
            WireNodeLookup start = resolveWireNode(level, startPos);
            if (start == null) {
                player.sendSystemMessage(Component.literal("Wire error"));
            } else {
                HbmLegacyPowerNodeShapes.WireConnectionResult result = start.node().connectWireTo(target.node());
                player.sendSystemMessage(Component.literal(messageFor(result)));
            }
            stack.setTag(null);
        }

        player.swing(context.getHand(), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (hasStoredPos(tag)) {
            tooltip.add(Component.literal("Wire start x: " + tag.getInt(TAG_X)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Wire start y: " + tag.getInt(TAG_Y)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Wire start z: " + tag.getInt(TAG_Z)).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Right-click poles to connect").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide || !(entity instanceof Player player) || !hasStoredPos(stack.getTag())) {
            return;
        }
        if (!selected && player.getOffhandItem() != stack) {
            return;
        }
        CompoundTag tag = stack.getTag();
        double dx = entity.getX() - tag.getInt(TAG_X);
        double dy = entity.getY() - tag.getInt(TAG_Y);
        double dz = entity.getZ() - tag.getInt(TAG_Z);
        int distance = (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
        player.displayClientMessage(Component.literal(stack.getHoverName().getString() + ": " + distance + "m"), true);
    }

    private static boolean hasStoredPos(@Nullable CompoundTag tag) {
        return tag != null && tag.contains(TAG_X) && tag.contains(TAG_Y) && tag.contains(TAG_Z);
    }

    @Nullable
    private static WireNodeLookup resolveWireNode(Level level, BlockPos pos) {
        BlockPos corePos = MultiblockHelper.resolveCorePos(level, pos);
        BlockEntity blockEntity = level.getBlockEntity(corePos);
        if (blockEntity instanceof HbmLegacyWireNode wireNode) {
            return new WireNodeLookup(corePos, wireNode);
        }
        return null;
    }

    private static String messageFor(HbmLegacyPowerNodeShapes.WireConnectionResult result) {
        return switch (result) {
            case OK -> "Wire end";
            case TYPE_MISMATCH -> "Wire error - Pylons are not the same type";
            case SAME_NODE -> "Wire error - Cannot connect to the same pylon";
            case TOO_FAR -> "Wire error - Pylon is too far away";
        };
    }

    private record WireNodeLookup(BlockPos pos, HbmLegacyWireNode node) {
    }
}
