package com.hbm.ntm.item;

import com.hbm.ntm.client.ClientInformMessages;
import com.hbm.ntm.drone.DroneLinkable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Exact two-click chaining contract of 1.7.10 ItemDroneLinker. */
public class DroneLinkerItem extends Item {
    private static final String X = "x", Y = "y", Z = "z";
    /** Legacy ServerProxy.ID_DRONE; keeps the held-link position in one replacing info slot. */
    private static final int DRONE_NOTICE_ID = 4;
    private static final int DRONE_NOTICE_MILLIS = 1_000;
    public DroneLinkerItem(Properties properties) { super(properties); }

    @Override public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof DroneLinkable target)) return InteractionResult.PASS;
        if (!level.isClientSide && context.getPlayer() instanceof ServerPlayer player) {
            ItemStack stack = context.getItemInHand();
            CompoundTag tag = stack.getTag();
            BlockPos current = context.getClickedPos();
            // ItemDroneLinker used hasTagCompound(), not a coordinate-key probe: any
            // existing NBT enters the second-click state and reads missing coordinates as 0.
            if (tag == null) {
                tag = stack.getOrCreateTag(); tag.putInt(X, current.getX()); tag.putInt(Y, current.getY()); tag.putInt(Z, current.getZ());
                player.sendSystemMessage(message("Set initial position!", ChatFormatting.AQUA));
            } else {
                BlockPos previous = new BlockPos(tag.getInt(X), tag.getInt(Y), tag.getInt(Z));
                if (level.getBlockEntity(previous) instanceof DroneLinkable source) {
                    source.setNextDroneTarget(target.dronePoint());
                    player.sendSystemMessage(message("Link set!", ChatFormatting.AQUA));
                } else {
                    player.sendSystemMessage(message("Previous link lost!", ChatFormatting.RED));
                }
                tag.putInt(X, current.getX()); tag.putInt(Y, current.getY()); tag.putInt(Z, current.getZ());
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && stack.hasTag()) {
            stack.setTag(null);
            player.sendSystemMessage(message("Position cleared!", ChatFormatting.GREEN));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        // ItemDroneLinker#onUpdate: only the actively held linked tool refreshes this one-second notice.
        if (!level.isClientSide || !selected || !stack.hasTag()) {
            return;
        }
        CompoundTag tag = stack.getTag();
        ClientInformMessages.show(Component.literal("Prev pos: " + tag.getInt(X) + " / " + tag.getInt(Y) + " / "
                        + tag.getInt(Z)),
                DRONE_NOTICE_ID, DRONE_NOTICE_MILLIS);
    }

    private Component message(String text, ChatFormatting color) {
        return Component.literal("[").withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.translatable(getDescriptionId()).withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal(text).withStyle(color));
    }
}
