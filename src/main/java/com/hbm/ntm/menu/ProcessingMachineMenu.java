package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.ProcessingMachineBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class ProcessingMachineMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 8;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final ProcessingMachineBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private long consumption;
    private int progress;
    private int duration;
    private int isOn;
    private HbmFluidGuiHelper.TankData tankData;

    public ProcessingMachineMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ProcessingMachineMenu(int containerId, Inventory playerInventory, ProcessingMachineBlockEntity blockEntity) {
        super(ModMenuTypes.PROCESSING_MACHINE.get(), containerId);
        this.blockEntity = blockEntity;
        if (blockEntity.kind() == ProcessingMachineBlockEntity.Kind.CENTRIFUGE) {
            addCentrifugeSlots(playerInventory);
        } else {
            addCrystallizerSlots(playerInventory);
        }
        addDataSlots();
    }

    private void addCentrifugeSlots(Inventory playerInventory) {
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 0, 36, 50));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 1, 9, 50));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), 2, 63, 50));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), 3, 83, 50));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), 4, 103, 50));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), 5, 123, 50));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), 6, 149, 22));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), 7, 149, 40));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 104, 162);
    }

    private void addCrystallizerSlots(Inventory playerInventory) {
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 0, 62, 45));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 1, 152, 72));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), 2, 113, 45));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 3, 17, 18));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), 4, 17, 54));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), 5, 80, 18));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), 6, 98, 18));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 7, 35, 72));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 122, 180);
    }

    public ProcessingMachineBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public long getConsumption() {
        return consumption;
    }

    public boolean isOn() {
        return isOn != 0;
    }

    public int getProgressWidth(int maxWidth) {
        return duration <= 0 ? 0 : progress * maxWidth / duration;
    }

    public int getProgressHeight(int maxHeight) {
        return duration <= 0 ? 0 : progress * maxHeight / duration;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public HbmFluidGuiHelper.TankData getTankData() {
        return tankData;
    }

    public List<Component> getTankTooltip(boolean showHidden) {
        return tankData.tooltip(showHidden);
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
        if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 1, 2);
        }
        if (stack.getItem() instanceof ItemMachineUpgrade) {
            return blockEntity.kind() == ProcessingMachineBlockEntity.Kind.CENTRIFUGE
                    ? HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 6, 8)
                    : HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 5, 7);
        }
        if (blockEntity.kind() == ProcessingMachineBlockEntity.Kind.CRYSTALLIZER
                && stack.getItem() instanceof IFluidIdentifierItem) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 7, 8);
        }
        if (blockEntity.kind() == ProcessingMachineBlockEntity.Kind.CRYSTALLIZER) {
            if (HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 3, 4)) {
                return true;
            }
        }
        return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 0, 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getPower(),
                () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getMaxPower(),
                () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getConsumption(),
                () -> consumption, value -> consumption = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getProgress(), value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getDuration(), value -> duration = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.isOn() ? 1 : 0, value -> isOn = value);
        tankData = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getCrystallizerTank());
    }

    private static ProcessingMachineBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof ProcessingMachineBlockEntity machine) {
            return machine;
        }
        throw new IllegalStateException("Expected processing machine block entity at " + pos);
    }
}
