package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.MassStorageBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MassStorageMenu extends AbstractContainerMenu {
    private final MassStorageBlockEntity blockEntity;
    private int stockpile;
    private int capacity;
    private boolean output;

    public MassStorageMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public MassStorageMenu(int containerId, Inventory playerInventory, MassStorageBlockEntity blockEntity) {
        super(ModMenuTypes.MASS_STORAGE.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.items(), MassStorageBlockEntity.SLOT_INPUT, 61, 17));
        addSlot(HbmInventoryMenuHelper.patternSlot(blockEntity.items(), MassStorageBlockEntity.SLOT_FILTER, 61, 53));
        addSlot(HbmInventoryMenuHelper.takeOnlySlot(blockEntity.items(), MassStorageBlockEntity.SLOT_OUTPUT, 61, 89));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 139, 197);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::stockpile, value -> stockpile = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::capacity, value -> capacity = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::output, value -> output = value);
        blockEntity.playOpenSound();
    }

    public MassStorageBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int stockpile() {
        return stockpile;
    }

    public int capacity() {
        return Math.max(1, capacity);
    }

    public boolean output() {
        return output;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        if (index == MassStorageBlockEntity.SLOT_OUTPUT && !slots.get(index).hasItem()) {
            ItemStack extracted = blockEntity.quickExtract();
            if (!extracted.isEmpty()) {
                slots.get(index).set(extracted);
            }
        }
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == MassStorageBlockEntity.SLOT_INPUT || index == MassStorageBlockEntity.SLOT_OUTPUT) {
                if (!moveItemStackTo(stack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 3) {
                if (!blockEntity.quickInsert(stack) && !moveItemStackTo(stack, MassStorageBlockEntity.SLOT_INPUT,
                        MassStorageBlockEntity.SLOT_INPUT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
            HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        }
        return result;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == MassStorageBlockEntity.SLOT_FILTER && clickType == ClickType.PICKUP) {
            blockEntity.setFilter(getCarried());
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        blockEntity.playCloseSound();
    }

    private static MassStorageBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof MassStorageBlockEntity storage) {
            return storage;
        }
        throw new IllegalStateException("Expected mass storage block entity at " + pos);
    }
}
