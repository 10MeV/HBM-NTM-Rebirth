package com.hbm.ntm.menu;

import com.hbm.ntm.entity.train.CargoTramTrailerEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** 5×9 cargo menu for {@link CargoTramTrailerEntity}. */
public final class CargoTramTrailerMenu extends AbstractContainerMenu {
    private final CargoTramTrailerEntity trailer;

    public CargoTramTrailerMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getTrailer(inventory, data.readInt()));
    }

    public CargoTramTrailerMenu(int containerId, Inventory inventory, CargoTramTrailerEntity trailer) {
        super(ModMenuTypes.CARGO_TRAM_TRAILER.get(), containerId);
        this.trailer = trailer;
        HbmInventoryMenuHelper.addSlots(this::addSlot, trailer.items(), 0, 8, 18, 5, 9, 18);
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 140, 198);
    }

    public CargoTramTrailerEntity trailer() {
        return trailer;
    }

    @Override
    public boolean stillValid(Player player) {
        return trailer.stillValid(player);
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
        if (index < CargoTramTrailerEntity.SLOT_COUNT) {
            if (!moveItemStackTo(stack, CargoTramTrailerEntity.SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, CargoTramTrailerEntity.SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return result;
    }

    private static CargoTramTrailerEntity getTrailer(Inventory inventory, int entityId) {
        Entity entity = inventory.player.level().getEntity(entityId);
        if (entity instanceof CargoTramTrailerEntity trailer) {
            return trailer;
        }
        throw new IllegalStateException("Expected cargo tram trailer entity " + entityId);
    }
}
