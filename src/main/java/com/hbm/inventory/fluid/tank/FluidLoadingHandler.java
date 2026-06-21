package com.hbm.inventory.fluid.tank;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

@Deprecated(forRemoval = false)
public abstract class FluidLoadingHandler {
    public abstract boolean fillItem(ItemStack[] slots, int in, int out, FluidTank tank);

    public abstract boolean emptyItem(ItemStack[] slots, int in, int out, FluidTank tank);

    protected static IItemHandlerModifiable wrap(ItemStack[] slots) {
        return new SlotArrayItemHandler(slots);
    }

    protected static boolean valid(ItemStack[] slots, int... indices) {
        if (slots == null) {
            return false;
        }
        for (int index : indices) {
            if (index < 0 || index >= slots.length) {
                return false;
            }
        }
        return true;
    }

    private static final class SlotArrayItemHandler implements IItemHandlerModifiable {
        private final ItemStack[] slots;

        private SlotArrayItemHandler(ItemStack[] slots) {
            this.slots = slots;
            for (int i = 0; i < slots.length; i++) {
                if (slots[i] == null) {
                    slots[i] = ItemStack.EMPTY;
                }
            }
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            slots[slot] = stack == null ? ItemStack.EMPTY : stack;
        }

        @Override
        public int getSlots() {
            return slots.length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            ItemStack stack = slots[slot];
            return stack == null ? ItemStack.EMPTY : stack;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack current = getStackInSlot(slot);
            if (current.isEmpty()) {
                if (!simulate) {
                    setStackInSlot(slot, stack.copy());
                }
                return ItemStack.EMPTY;
            }
            if (!ItemStack.isSameItemSameTags(current, stack)
                    || current.getCount() >= Math.min(current.getMaxStackSize(), getSlotLimit(slot))) {
                return stack;
            }
            int room = Math.min(current.getMaxStackSize(), getSlotLimit(slot)) - current.getCount();
            int moved = Math.min(room, stack.getCount());
            if (!simulate) {
                current.grow(moved);
                setStackInSlot(slot, current);
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(moved);
            return remainder;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack current = getStackInSlot(slot);
            if (current.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack extracted = current.copy();
            extracted.setCount(Math.min(amount, current.getCount()));
            if (!simulate) {
                ItemStack remaining = current.copy();
                remaining.shrink(extracted.getCount());
                setStackInSlot(slot, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return true;
        }
    }
}
