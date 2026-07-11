package com.hbm.ntm.menu;

import com.hbm.ntm.entity.cart.NtmCrateMinecartEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CartCrateMenu extends AbstractContainerMenu {
    private static final int CART_SLOTS = NtmCrateMinecartEntity.SLOT_COUNT;

    private final NtmCrateMinecartEntity cart;

    public CartCrateMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getCart(playerInventory, data.readInt()));
    }

    public CartCrateMenu(int containerId, Inventory playerInventory, NtmCrateMinecartEntity cart) {
        super(ModMenuTypes.CART_CRATE.get(), containerId);
        this.cart = cart;
        HbmInventoryMenuHelper.addSlots(this::addSlot, cart.items(), 0, 8, 18, 6, 9, 18);
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 140, 198);
    }

    public NtmCrateMinecartEntity cart() {
        return cart;
    }

    @Override
    public boolean stillValid(Player player) {
        return cart.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < CART_SLOTS) {
                if (!moveItemStackTo(stack, CART_SLOTS, CART_SLOTS + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, CART_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
            HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        }
        return result;
    }

    private static NtmCrateMinecartEntity getCart(Inventory inventory, int entityId) {
        Entity entity = inventory.player.level().getEntity(entityId);
        if (entity instanceof NtmCrateMinecartEntity cart) {
            return cart;
        }
        throw new IllegalStateException("Expected crate cart entity " + entityId);
    }
}
