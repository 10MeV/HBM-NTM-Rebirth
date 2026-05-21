package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ToolboxItem extends Item {
    private static final String TAG_ITEMS = "items";
    private static final String TAG_SLOT = "slot";
    private static final int HOTBAR_SIZE = 9;
    private static final int TOOLBOX_ROWS = 3;
    private static final int TOOLBOX_ROW_SIZE = 8;
    private static final int TOOLBOX_SIZE = TOOLBOX_ROWS * TOOLBOX_ROW_SIZE;

    public ToolboxItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide && !player.isShiftKeyDown()) {
            moveRows(stack, player);
            player.inventoryMenu.broadcastChanges();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.hbm.toolbox.desc.swap"));
        tooltip.add(Component.translatable("item.hbm.toolbox.desc.open"));
    }

    private static void moveRows(ItemStack box, Player player) {
        ItemStack[] endingHotbar = emptyStacks(HOTBAR_SIZE);
        ItemStack[] stacksToTransferToBox = emptyStacks(TOOLBOX_ROW_SIZE);

        boolean hasToolbox = false;
        int extraToolboxes = 0;
        int selected = player.getInventory().selected;

        for (int i = 0; i < HOTBAR_SIZE; i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (isToolbox(slot) && i != selected) {
                extraToolboxes++;
                player.drop(slot.copy(), true);
                player.getInventory().setItem(i, ItemStack.EMPTY);
            } else if (i == selected) {
                hasToolbox = true;
                endingHotbar[i] = box;
            } else {
                int target = i - (hasToolbox ? 1 : 0);
                if (target >= 0 && target < stacksToTransferToBox.length) {
                    stacksToTransferToBox[target] = slot.copy();
                }
            }
        }

        if (extraToolboxes > 0) {
            player.displayClientMessage(Component.literal(extraToolboxes == 1
                    ? "You can't toolbox a toolbox..."
                    : "You can't toolbox a toolbox... (x" + extraToolboxes + ")"), true);
        }

        ItemStack[] stacks = readStacksFromNBT(box, TOOLBOX_SIZE);
        ItemStack[] endingStacks = emptyStacks(TOOLBOX_SIZE);

        List<Integer> activeRows = getActiveRows(stacks);
        int lowestActiveIndex = Integer.MAX_VALUE;
        int lowestInactiveIndex = Integer.MAX_VALUE;

        for (int row = 0; row < TOOLBOX_ROWS; row++) {
            if (activeRows.contains(row)) {
                lowestActiveIndex = Math.min(row, lowestActiveIndex);
            } else {
                lowestInactiveIndex = Math.min(row, lowestInactiveIndex);
            }
        }

        if (lowestInactiveIndex > TOOLBOX_ROWS - 1) {
            lowestInactiveIndex = TOOLBOX_ROWS - 1;
        } else {
            lowestInactiveIndex = Math.max(0, lowestInactiveIndex - 1);
        }

        for (int activeRowIndex : activeRows) {
            int activeIndex = TOOLBOX_ROW_SIZE * activeRowIndex;
            if (activeRowIndex == lowestActiveIndex) {
                hasToolbox = false;
                for (int i = 0; i < HOTBAR_SIZE; i++) {
                    if (i == selected) {
                        hasToolbox = true;
                        continue;
                    }
                    endingHotbar[i] = stacks[activeIndex + i - (hasToolbox ? 1 : 0)].copy();
                }
            } else {
                int targetIndex = TOOLBOX_ROW_SIZE * (activeRowIndex - 1);
                System.arraycopy(stacks, activeIndex, endingStacks, targetIndex, TOOLBOX_ROW_SIZE);
            }
        }

        System.arraycopy(stacksToTransferToBox, 0, endingStacks, lowestInactiveIndex * TOOLBOX_ROW_SIZE, TOOLBOX_ROW_SIZE);

        for (int i = 0; i < HOTBAR_SIZE; i++) {
            player.getInventory().setItem(i, endingHotbar[i]);
        }

        writeStacksToNBT(box, endingStacks);
    }

    private static boolean isToolbox(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItems.TOOLBOX.get());
    }

    private static List<Integer> getActiveRows(ItemStack[] stacks) {
        List<Integer> activeRows = new ArrayList<>();
        for (int row = 0; row < TOOLBOX_ROWS; row++) {
            for (int slot = 0; slot < TOOLBOX_ROW_SIZE; slot++) {
                if (!stacks[row * TOOLBOX_ROW_SIZE + slot].isEmpty()) {
                    activeRows.add(row);
                    break;
                }
            }
        }
        return activeRows;
    }

    private static ItemStack[] readStacksFromNBT(ItemStack stack, int count) {
        ItemStack[] stacks = emptyStacks(count);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_ITEMS, Tag.TAG_LIST)) {
            return stacks;
        }
        ListTag list = tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag slotTag = list.getCompound(i);
            int slot = slotTag.getByte(TAG_SLOT) & 255;
            if (slot >= 0 && slot < stacks.length) {
                stacks[slot] = ItemStack.of(slotTag);
            }
        }
        return stacks;
    }

    private static void writeStacksToNBT(ItemStack stack, ItemStack[] stacks) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = new ListTag();
        for (int i = 0; i < stacks.length; i++) {
            ItemStack stored = stacks[i];
            if (!stored.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte(TAG_SLOT, (byte) i);
                stored.save(slotTag);
                list.add(slotTag);
            }
        }
        if (list.isEmpty()) {
            tag.remove(TAG_ITEMS);
        } else {
            tag.put(TAG_ITEMS, list);
        }
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    private static ItemStack[] emptyStacks(int size) {
        ItemStack[] stacks = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            stacks[i] = ItemStack.EMPTY;
        }
        return stacks;
    }
}
