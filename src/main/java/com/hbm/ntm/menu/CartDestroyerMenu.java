package com.hbm.ntm.menu;

import com.hbm.ntm.entity.cart.NtmDestroyerMinecartEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class CartDestroyerMenu extends AbstractContainerMenu {
    private static final int FILTER_SLOTS = 18;

    private final NtmDestroyerMinecartEntity cart;

    public CartDestroyerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getCart(playerInventory, data.readInt()));
    }

    public CartDestroyerMenu(int containerId, Inventory playerInventory, NtmDestroyerMinecartEntity cart) {
        super(ModMenuTypes.CART_DESTROYER.get(), containerId);
        this.cart = cart;
        addCartSlots(cart.filters());
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
    }

    public NtmDestroyerMinecartEntity cart() {
        return cart;
    }

    @Override
    public boolean stillValid(Player player) {
        return cart.stillValid(player);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < FILTER_SLOTS && slotId < slots.size()) {
            Slot slot = slots.get(slotId);
            if (HbmInventoryMenuHelper.isLegacyPatternSlot(slot)) {
                slot.set(getCarried());
                broadcastChanges();
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private void addCartSlots(ItemStackHandler filters) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(HbmInventoryMenuHelper.patternSlot(filters, column + row * 3,
                        10 + column * 18, 17 + row * 18));
                addSlot(HbmInventoryMenuHelper.patternSlot(filters, column + row * 3 + 9,
                        114 + column * 18, 17 + row * 18));
            }
        }
    }

    private static NtmDestroyerMinecartEntity getCart(Inventory inventory, int entityId) {
        Entity entity = inventory.player.level().getEntity(entityId);
        if (entity instanceof NtmDestroyerMinecartEntity cart) {
            return cart;
        }
        throw new IllegalStateException("Expected destroyer cart entity " + entityId);
    }
}
