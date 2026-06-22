package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.ElectricFurnaceBlockEntity;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.multiblock.MultiblockHelper;
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
import net.minecraftforge.items.ItemStackHandler;

public class ElectricFurnaceMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = ElectricFurnaceBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final ElectricFurnaceBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int progress;
    private int maxProgress;
    private int consumption;
    private int active;

    public ElectricFurnaceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ElectricFurnaceMenu(int containerId, Inventory playerInventory, ElectricFurnaceBlockEntity blockEntity) {
        super(ModMenuTypes.ELECTRIC_FURNACE.get(), containerId);
        this.blockEntity = blockEntity;
        addMachineSlots(playerInventory, blockEntity.getItems());
        addDataSlots();
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getProgressWidth(int maxWidth) {
        return maxProgress <= 0 ? 0 : progress * maxWidth / maxProgress;
    }

    public int getConsumption() {
        return consumption;
    }

    public boolean isActive() {
        return active != 0;
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
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (ElectricFurnaceBlockEntity.isLegacyBattery(stack)) {
            if (!moveItemStackTo(stack, ElectricFurnaceBlockEntity.SLOT_BATTERY,
                    ElectricFurnaceBlockEntity.SLOT_BATTERY + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof ItemMachineUpgrade) {
            if (!moveItemStackTo(stack, ElectricFurnaceBlockEntity.SLOT_UPGRADE,
                    ElectricFurnaceBlockEntity.SLOT_UPGRADE + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, ElectricFurnaceBlockEntity.SLOT_INPUT,
                ElectricFurnaceBlockEntity.SLOT_INPUT + 1, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private void addMachineSlots(Inventory playerInventory, ItemStackHandler items) {
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, ElectricFurnaceBlockEntity.SLOT_BATTERY, 56, 53));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, ElectricFurnaceBlockEntity.SLOT_INPUT, 56, 17));
        addSlot(HbmInventoryMenuHelper.smeltingOutputSlot(playerInventory.player, items,
                ElectricFurnaceBlockEntity.SLOT_OUTPUT, 116, 35, null));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(items, ElectricFurnaceBlockEntity.SLOT_UPGRADE, 147, 34));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getPower(),
                () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getMaxPower(),
                () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMaxProgress, value -> maxProgress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getConsumption, value -> consumption = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.isActive() ? 1 : 0, value -> active = value);
    }

    private static ElectricFurnaceBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof ElectricFurnaceBlockEntity furnace) {
            return furnace;
        }
        throw new IllegalStateException("Expected electric furnace block entity at " + pos);
    }
}
