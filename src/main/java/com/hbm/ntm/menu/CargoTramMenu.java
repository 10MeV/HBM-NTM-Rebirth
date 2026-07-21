package com.hbm.ntm.menu;

import com.hbm.ntm.energy.HbmChargeableItem;
import com.hbm.ntm.entity.train.CargoTramEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** 4×7 cargo plus legacy charge-slot menu for {@link CargoTramEntity}. */
public final class CargoTramMenu extends AbstractContainerMenu {
    private final CargoTramEntity tram;

    public CargoTramMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getTram(inventory, data.readInt()));
    }

    public CargoTramMenu(int containerId, Inventory inventory, CargoTramEntity tram) {
        super(ModMenuTypes.CARGO_TRAM.get(), containerId);
        this.tram = tram;
        HbmInventoryMenuHelper.addSlots(this::addSlot, tram.items(), 0, 8, 18, 4, 7, 18);
        addSlot(new Slot(tram, CargoTramEntity.CHARGE_SLOT, 152, 72));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 122, 180);
    }

    public CargoTramEntity tram() {
        return tram;
    }

    @Override
    public boolean stillValid(Player player) {
        return tram.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return result;
        }
        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index < CargoTramEntity.SLOT_COUNT) {
            if (!moveItemStackTo(stack, CargoTramEntity.SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof HbmChargeableItem) {
            if (!moveItemStackTo(stack, CargoTramEntity.CHARGE_SLOT,
                    CargoTramEntity.CHARGE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, CargoTramEntity.CHARGE_SLOT, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return result;
    }

    private static CargoTramEntity getTram(Inventory inventory, int entityId) {
        Entity entity = inventory.player.level().getEntity(entityId);
        if (entity instanceof CargoTramEntity tram) {
            return tram;
        }
        throw new IllegalStateException("Expected cargo tram entity " + entityId);
    }
}
