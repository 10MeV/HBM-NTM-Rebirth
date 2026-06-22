package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.DiFurnaceBlockEntity;
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

public class DiFurnaceMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = DiFurnaceBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final DiFurnaceBlockEntity blockEntity;
    private int progress;
    private int fuel;
    private int sideFuel;
    private int sideUpper;
    private int sideLower;
    private boolean processing;

    public DiFurnaceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public DiFurnaceMenu(int containerId, Inventory playerInventory, DiFurnaceBlockEntity blockEntity) {
        super(ModMenuTypes.DIFURNACE.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                DiFurnaceBlockEntity.SLOT_UPPER, 80, 18));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                DiFurnaceBlockEntity.SLOT_LOWER, 80, 54));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                DiFurnaceBlockEntity.SLOT_FUEL, 8, 36));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(playerInventory.player, blockEntity.getItems(),
                DiFurnaceBlockEntity.SLOT_OUTPUT, 134, 36));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
        addDataSlots();
    }

    public int getProgressPixels(int width) {
        return Math.max(0, Math.min(width, progress * width / DiFurnaceBlockEntity.PROCESSING_SPEED));
    }

    public int getFuelPixels(int height) {
        return Math.max(0, Math.min(height, fuel * height / DiFurnaceBlockEntity.MAX_FUEL));
    }

    public int getProgress() {
        return progress;
    }

    public int getFuel() {
        return fuel;
    }

    public int getSideForSlot(int slot) {
        return switch (slot) {
            case DiFurnaceBlockEntity.SLOT_UPPER -> sideUpper;
            case DiFurnaceBlockEntity.SLOT_LOWER -> sideLower;
            case DiFurnaceBlockEntity.SLOT_FUEL -> sideFuel;
            default -> -1;
        };
    }

    public boolean isProcessing() {
        return processing;
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
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, DiFurnaceBlockEntity.SLOT_UPPER,
                DiFurnaceBlockEntity.SLOT_FUEL + 1, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < 3 && button == 1 && clickType == ClickType.PICKUP
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

    private void addDataSlots() {
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getFuel, value -> fuel = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getSideFuel(), value -> sideFuel = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getSideUpper(), value -> sideUpper = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getSideLower(), value -> sideLower = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isProcessing, value -> processing = value);
    }

    private static DiFurnaceBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof DiFurnaceBlockEntity furnace) {
            return furnace;
        }
        throw new IllegalStateException("Expected difurnace block entity at " + pos);
    }
}
