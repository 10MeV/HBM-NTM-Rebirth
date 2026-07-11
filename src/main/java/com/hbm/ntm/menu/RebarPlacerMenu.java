package com.hbm.ntm.menu;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RebarPlacerMenu extends LegacyItemBagMenu {
    public RebarPlacerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        super(ModMenuTypes.REBAR_PLACER.get(), containerId, playerInventory, data, 1, 1, stack -> true);
        addSlots();
    }

    public RebarPlacerMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.REBAR_PLACER.get(), containerId, playerInventory, hand, 1, 1, stack -> true);
        addSlots();
    }

    @Override
    protected Supplier<? extends Item> bagItem() {
        return ModItems.REBAR_PLACER;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (hand == InteractionHand.MAIN_HAND
                && HbmInventoryMenuHelper.shouldBlockOpenItemContainerClick(slotId, button, clickType,
                        playerInventory, bagSlotCount)) {
            return;
        }
        if (slotId != 0 || clickType != ClickType.PICKUP) {
            super.clicked(slotId, button, clickType, player);
            return;
        }

        Slot slot = slots.get(0);
        ItemStack previous = slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
        ItemStack next = getCarried().isEmpty() ? ItemStack.EMPTY : getCarried().copy();
        if (!next.isEmpty()) {
            next.setCount(1);
        }
        slot.set(next);
        setCarried(previous);
        broadcastChanges();
    }

    private void addSlots() {
        addBagSlots(53, 36, 1, 1);
        addLegacyPlayerInventory(100, 158);
    }
}
