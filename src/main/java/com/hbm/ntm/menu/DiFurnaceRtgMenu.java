package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.DiFurnaceRtgBlockEntity;
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

public class DiFurnaceRtgMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 9;
    private static final int PLAYER_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_END = PLAYER_START + 36;
    private final DiFurnaceRtgBlockEntity blockEntity;
    private int progress;
    private int speed;
    private int sideUpper;
    private int sideLower;

    public DiFurnaceRtgMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public DiFurnaceRtgMenu(int containerId, Inventory playerInventory, DiFurnaceRtgBlockEntity blockEntity) {
        super(ModMenuTypes.DIFURNACE_RTG.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 0, 80, 18));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 1, 80, 54));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), 2, 134, 36));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 3, 22, 18));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 4, 40, 18));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 5, 22, 36));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 6, 40, 36));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 7, 22, 54));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 8, 40, 54));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getSpeed, value -> speed = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getSideUpper(), value -> sideUpper = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getSideLower(), value -> sideLower = value);
    }

    public int getProgressWidth(int width) {
        return progress * width / DiFurnaceRtgBlockEntity.PROCESS_TIME;
    }

    public int getProgress() {
        return progress;
    }

    public int getSpeed() {
        return speed;
    }

    public int getSideForSlot(int slot) {
        return slot == 0 ? sideUpper : slot == 1 ? sideLower : -1;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 3, 9, false) && !moveItemStackTo(stack, 0, 2, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if ((slotId == 0 || slotId == 1) && button == 1 && clickType == ClickType.PICKUP
                && getCarried().isEmpty()) {
            Slot slot = slots.get(slotId);
            if (slot != null && !slot.hasItem()) {
                if (!player.level().isClientSide) {
                    blockEntity.cycleSideMode(slotId);
                }
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    private static DiFurnaceRtgBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof DiFurnaceRtgBlockEntity furnace) {
            return furnace;
        }
        throw new IllegalStateException("Expected rtg difurnace at " + pos);
    }
}
