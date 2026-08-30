package com.hbm.ntm.menu;

import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class LegacyItemBagMenu extends AbstractContainerMenu {
    private static final int DEFAULT_SLOT_STACK_LIMIT = 64;

    protected final Inventory playerInventory;
    protected final InteractionHand hand;
    protected final ItemBackedBagContainer bagInventory;
    protected final int bagSlotCount;
    private final int slotStackLimit;

    protected LegacyItemBagMenu(MenuType<?> menuType, int containerId, Inventory playerInventory,
            FriendlyByteBuf data, int bagSlotCount, Predicate<ItemStack> slotValidator) {
        this(menuType, containerId, playerInventory, data, bagSlotCount, DEFAULT_SLOT_STACK_LIMIT,
                slotValidator);
    }

    protected LegacyItemBagMenu(MenuType<?> menuType, int containerId, Inventory playerInventory,
            FriendlyByteBuf data, int bagSlotCount, int slotStackLimit, Predicate<ItemStack> slotValidator) {
        this(menuType, containerId, playerInventory, data.readEnum(InteractionHand.class), bagSlotCount,
                slotStackLimit, slotValidator);
    }

    protected LegacyItemBagMenu(MenuType<?> menuType, int containerId, Inventory playerInventory,
            InteractionHand hand, int bagSlotCount, Predicate<ItemStack> slotValidator) {
        this(menuType, containerId, playerInventory, hand, bagSlotCount, DEFAULT_SLOT_STACK_LIMIT,
                slotValidator);
    }

    protected LegacyItemBagMenu(MenuType<?> menuType, int containerId, Inventory playerInventory,
            InteractionHand hand, int bagSlotCount, int slotStackLimit, Predicate<ItemStack> slotValidator) {
        super(menuType, containerId);
        this.playerInventory = playerInventory;
        this.hand = hand;
        this.bagSlotCount = bagSlotCount;
        this.slotStackLimit = Math.max(1, slotStackLimit);
        this.bagInventory = new ItemBackedBagContainer(getBagStack(), bagSlotCount, this.slotStackLimit,
                slotValidator);
    }

    protected abstract Supplier<? extends Item> bagItem();

    protected ItemStack getBagStack() {
        return playerInventory.player.getItemInHand(hand);
    }

    protected void addBagSlots(int x, int y, int rows, int columns) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int slot = column + row * columns;
                addSlot(createBagSlot(slot, x + column * 18, y + row * 18));
            }
        }
    }

    protected Slot createBagSlot(int slot, int x, int y) {
        if (slotStackLimit >= DEFAULT_SLOT_STACK_LIMIT) {
            return HbmInventoryMenuHelper.legacyContainerSlot(bagInventory, slot, x, y);
        }
        return new Slot(bagInventory, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return bagInventory.canPlaceItem(slot, stack);
            }

            @Override
            public int getMaxStackSize() {
                return slotStackLimit;
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                return slotStackLimit;
            }
        };
    }

    protected void addLegacyPlayerInventory(int inventoryY, int hotbarY) {
        addVanillaPlayerInventory(inventoryY);
        addVanillaHotbar(hotbarY, hand == InteractionHand.MAIN_HAND ? playerInventory.selected : -1);
    }

    private void addVanillaPlayerInventory(int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, y + row * 18));
            }
        }
    }

    private void addVanillaHotbar(int y, int lockedHotbarSlot) {
        for (int column = 0; column < 9; column++) {
            Slot slot = column == lockedHotbarSlot
                    ? HbmInventoryMenuHelper.lockedPlayerSlot(playerInventory, column, 8 + column * 18, y)
                    : new Slot(playerInventory, column, 8 + column * 18, y);
            addSlot(slot);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player == playerInventory.player && getBagStack().is(bagItem().get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        if (index < 0 || index >= slots.size()) {
            return result;
        }
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < bagSlotCount) {
                if (!moveItemStackTo(stack, bagSlotCount, bagSlotCount + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, bagSlotCount, false)) {
                return ItemStack.EMPTY;
            }
            HbmInventoryMenuHelper.finishQuickMove(slot, stack);
            bagInventory.save();
        }
        return result;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (hand == InteractionHand.MAIN_HAND
                && HbmInventoryMenuHelper.shouldBlockOpenItemContainerClick(slotId, button, clickType,
                        playerInventory, bagSlotCount)) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
        bagInventory.save();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        bagInventory.save();
    }

    protected static class ItemBackedBagContainer extends SimpleContainer {
        private final ItemStack bagStack;
        private final int slotStackLimit;
        private final Predicate<ItemStack> slotValidator;
        private boolean loading = true;

        private ItemBackedBagContainer(ItemStack bagStack, int slotCount, int slotStackLimit,
                Predicate<ItemStack> slotValidator) {
            super(slotCount);
            this.bagStack = bagStack;
            this.slotStackLimit = slotStackLimit;
            this.slotValidator = slotValidator;
            NonNullList<ItemStack> stacks = HbmItemStackUtil.readStacksFromNbt(bagStack, slotCount);
            for (int slot = 0; slot < Math.min(slotCount, stacks.size()); slot++) {
                super.setItem(slot, stacks.get(slot));
            }
            this.loading = false;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return slotValidator.test(stack);
        }

        @Override
        public int getMaxStackSize() {
            return slotStackLimit;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (!loading) {
                save();
            }
        }

        private void save() {
            if (!bagStack.isEmpty()) {
                HbmItemStackUtil.setStacksToNbt(bagStack, getItems(), false);
            }
        }

        private NonNullList<ItemStack> getItems() {
            NonNullList<ItemStack> stacks = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
            for (int slot = 0; slot < getContainerSize(); slot++) {
                stacks.set(slot, getItem(slot));
            }
            return stacks;
        }
    }
}
