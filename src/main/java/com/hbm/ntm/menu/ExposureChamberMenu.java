package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.ExposureChamberBlockEntity;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ExposureChamberMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 7;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final ExposureChamberBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int progress;
    private int processTime;
    private int consumption;
    private int savedParticles;

    public ExposureChamberMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ExposureChamberMenu(int containerId, Inventory playerInventory, ExposureChamberBlockEntity blockEntity) {
        super(ModMenuTypes.EXPOSURE_CHAMBER.get(), containerId);
        this.blockEntity = blockEntity;
        addMachineSlots(playerInventory);
        addDataSlots();
    }

    private void addMachineSlots(Inventory playerInventory) {
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), ExposureChamberBlockEntity.SLOT_PARTICLE, 8, 18));
        addSlot(HbmInventoryMenuHelper.takeOnlySlot(blockEntity.getItems(), ExposureChamberBlockEntity.SLOT_CONTAINER, 8, 54));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), ExposureChamberBlockEntity.SLOT_INGREDIENT, 80, 36));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), ExposureChamberBlockEntity.SLOT_OUTPUT, 116, 36));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), ExposureChamberBlockEntity.SLOT_BATTERY, 152, 54));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), ExposureChamberBlockEntity.SLOT_UPGRADE_0, 44, 54));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), ExposureChamberBlockEntity.SLOT_UPGRADE_1, 62, 54));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 104, 162);
    }

    public ExposureChamberBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getConsumption() {
        return consumption;
    }

    public int getSavedParticles() {
        return savedParticles;
    }

    public int getProgressWidth(int maxWidth) {
        return processTime <= 0 ? 0 : progress * maxWidth / (processTime + 1);
    }

    public int getParticleHeight(int maxHeight) {
        return savedParticles * maxHeight / ExposureChamberBlockEntity.MAX_PARTICLES;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public boolean hasEnoughPower() {
        return power >= consumption;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
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
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!movePlayerStackToMachine(stack)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private boolean movePlayerStackToMachine(ItemStack stack) {
        if (stack.getItem() instanceof ItemMachineUpgrade) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 5, 7);
        }
        if (HbmInventoryMenuHelper.isLegacyBatteryItem(stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 4, 5);
        }
        if (blockEntity.getItems().isItemValid(ExposureChamberBlockEntity.SLOT_PARTICLE, stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 0, 1);
        }
        if (blockEntity.getItems().isItemValid(ExposureChamberBlockEntity.SLOT_INGREDIENT, stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 2, 3);
        }
        return false;
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower,
                () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower,
                () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProcessTime, value -> processTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getConsumption, value -> consumption = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getSavedParticles, value -> savedParticles = value);
    }

    private static ExposureChamberBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof ExposureChamberBlockEntity chamber) {
            return chamber;
        }
        throw new IllegalStateException("Expected exposure chamber block entity at " + pos);
    }
}
