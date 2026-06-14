package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RBMKAutoloaderBlockEntity;
import com.hbm.ntm.neutron.RBMKAutoloaderPlanner;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RBMKAutoloaderMenu extends AbstractContainerMenu {
    private final RBMKAutoloaderBlockEntity blockEntity;
    private int cycle = RBMKAutoloaderPlanner.DEFAULT_CYCLE;

    public RBMKAutoloaderMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public RBMKAutoloaderMenu(int containerId, Inventory inventory, RBMKAutoloaderBlockEntity blockEntity) {
        super(ModMenuTypes.RBMK_AUTOLOADER.get(), containerId);
        this.blockEntity = blockEntity;
        HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.items(),
                RBMKAutoloaderPlanner.INPUT_SLOT_START, 18, 3, 3, 3);
        HbmInventoryMenuHelper.addTakeOnlySlots(this::addSlot, blockEntity.items(),
                RBMKAutoloaderPlanner.OUTPUT_SLOT_START, 107, 18, 3, 3);
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 100, 158);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::cycle, value -> cycle = value);
    }

    public RBMKAutoloaderBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getCycle() {
        return cycle;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < RBMKAutoloaderPlanner.SLOT_COUNT) {
                if (!moveItemStackTo(stack, RBMKAutoloaderPlanner.SLOT_COUNT, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, RBMKAutoloaderPlanner.INPUT_SLOT_START,
                    RBMKAutoloaderPlanner.INPUT_SLOT_END + 1, false)) {
                return ItemStack.EMPTY;
            }
            HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        }
        return result;
    }

    private static RBMKAutoloaderBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof RBMKAutoloaderBlockEntity autoloader) {
            return autoloader;
        }
        throw new IllegalStateException("Expected RBMK autoloader block entity at " + pos);
    }
}
