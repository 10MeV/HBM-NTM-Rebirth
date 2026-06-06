package com.hbm.ntm.util;

import com.hbm.ntm.energy.HbmBatteryItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public final class HbmInventoryMenuHelper {
    public static final String LEGACY_ITEMS_TAG = "items";
    public static final String LEGACY_SLOT_TAG = "slot";

    private HbmInventoryMenuHelper() {
    }

    public static SlotItemHandler outputSlot(IItemHandler items, int slot, int x, int y) {
        return new SlotItemHandler(items, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        };
    }

    public static SlotItemHandler deprecatedSlot(IItemHandler items, int slot, int x, int y) {
        return new SlotItemHandler(items, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }

            @Override
            public boolean isActive() {
                return false;
            }
        };
    }

    public static void addPlayerInventory(SlotSink sink, Inventory inventory, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                sink.add(new Slot(inventory, column + row * 9 + 9, x + column * 18, y + row * 18));
            }
        }
    }

    public static void addHotbar(SlotSink sink, Inventory inventory, int x, int y) {
        for (int column = 0; column < 9; column++) {
            sink.add(new Slot(inventory, column, x + column * 18, y));
        }
    }

    public static void addPlayerInventoryAndHotbar(SlotSink sink, Inventory inventory,
            int x, int inventoryY, int hotbarY) {
        addPlayerInventory(sink, inventory, x, inventoryY);
        addHotbar(sink, inventory, x, hotbarY);
    }

    public static boolean stillValidBlockEntity(Player player, net.minecraft.world.level.block.entity.BlockEntity blockEntity,
            double maxDistanceSqr) {
        return !blockEntity.isRemoved() && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= maxDistanceSqr;
    }

    public static ItemStack moveMachineStack(java.util.List<Slot> slots, StackMover mover, int index, int machineSlotCount,
            int playerInventoryStart, int playerSlotEnd, int... machineInsertionRanges) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        if (index < machineSlotCount) {
            if (!mover.move(stack, playerInventoryStart, playerSlotEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveToAnyRange(slots, stack, machineInsertionRanges)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    public static boolean moveStackToAnyRange(java.util.List<Slot> slots, ItemStack stack, int... ranges) {
        return moveToAnyRange(slots, stack, ranges);
    }

    public static boolean isBatteryLike(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof HbmBatteryItem
                || stack.getCapability(ForgeCapabilities.ENERGY, null).isPresent());
    }

    public static CompoundTag saveLegacyItems(ItemStackHandler items) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(items.getSlots(), ItemStack.EMPTY);
        for (int slot = 0; slot < items.getSlots(); slot++) {
            stacks.set(slot, items.getStackInSlot(slot));
        }
        return saveLegacyItems(stacks);
    }

    public static CompoundTag saveLegacyItems(NonNullList<ItemStack> items) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty()) {
                CompoundTag stackTag = new CompoundTag();
                stackTag.putByte(LEGACY_SLOT_TAG, (byte) slot);
                stack.save(stackTag);
                list.add(stackTag);
            }
        }
        tag.put(LEGACY_ITEMS_TAG, list);
        return tag;
    }

    public static void loadLegacyItems(CompoundTag tag, ItemStackHandler items) {
        NonNullList<ItemStack> stacks = loadLegacyItems(tag, items.getSlots());
        for (int slot = 0; slot < stacks.size(); slot++) {
            items.setStackInSlot(slot, stacks.get(slot));
        }
    }

    public static NonNullList<ItemStack> loadLegacyItems(CompoundTag tag, int slotCount) {
        NonNullList<ItemStack> items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        if (tag == null || slotCount <= 0 || !tag.contains(LEGACY_ITEMS_TAG, Tag.TAG_LIST)) {
            return items;
        }
        ListTag list = tag.getList(LEGACY_ITEMS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag stackTag = list.getCompound(i);
            int slot = stackTag.getByte(LEGACY_SLOT_TAG) & 255;
            if (slot >= 0 && slot < slotCount) {
                items.set(slot, ItemStack.of(stackTag));
            }
        }
        return items;
    }

    public static java.util.List<ItemStack> clearToDrops(ItemStackHandler items) {
        java.util.List<ItemStack> drops = new java.util.ArrayList<>();
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        return drops;
    }

    private static boolean moveToAnyRange(java.util.List<Slot> slots, ItemStack stack, int... ranges) {
        if (ranges == null || ranges.length % 2 != 0) {
            return false;
        }
        boolean moved = false;
        for (int i = 0; i < ranges.length; i += 2) {
            moved |= legacyMergeItemStack(slots, stack, ranges[i], ranges[i + 1], false);
            if (stack.isEmpty()) {
                break;
            }
        }
        return moved;
    }

    private static boolean legacyMergeItemStack(java.util.List<Slot> slots, ItemStack stack, int start, int end,
            boolean reverse) {
        boolean moved = false;
        if (stack.isStackable()) {
            int index = reverse ? end - 1 : start;
            while (!stack.isEmpty() && inRange(index, start, end, reverse)) {
                Slot slot = slots.get(index);
                ItemStack current = slot.getItem();
                if (!current.isEmpty() && ItemStack.isSameItemSameTags(stack, current)) {
                    int max = Math.min(slot.getMaxStackSize(stack), stack.getMaxStackSize());
                    int transfer = Math.min(stack.getCount(), max - current.getCount());
                    if (transfer > 0 && slot.mayPlace(stack.copyWithCount(transfer))) {
                        stack.shrink(transfer);
                        current.grow(transfer);
                        slot.setByPlayer(current);
                        slot.setChanged();
                        moved = true;
                    }
                }
                index += reverse ? -1 : 1;
            }
        }

        if (!stack.isEmpty()) {
            int index = reverse ? end - 1 : start;
            while (!stack.isEmpty() && inRange(index, start, end, reverse)) {
                Slot slot = slots.get(index);
                ItemStack current = slot.getItem();
                if (current.isEmpty()) {
                    int transfer = Math.min(stack.getCount(),
                            Math.min(slot.getMaxStackSize(stack), stack.getMaxStackSize()));
                    if (transfer > 0 && slot.mayPlace(stack.copyWithCount(transfer))) {
                        slot.setByPlayer(stack.split(transfer));
                        slot.setChanged();
                        moved = true;
                    }
                }
                index += reverse ? -1 : 1;
            }
        }
        return moved;
    }

    private static boolean inRange(int index, int start, int end, boolean reverse) {
        return reverse ? index >= start : index < end;
    }

    @FunctionalInterface
    public interface SlotSink {
        void add(Slot slot);
    }

    @FunctionalInterface
    public interface StackMover {
        boolean move(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection);
    }
}
