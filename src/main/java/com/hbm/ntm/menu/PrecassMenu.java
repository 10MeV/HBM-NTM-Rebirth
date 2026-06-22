package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.LegacyGenericSelectorMachineBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.ItemBlueprints;
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

public class PrecassMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 22;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final LegacyGenericSelectorMachineBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int progress;
    private HbmFluidGuiHelper.TankData inputTank;
    private HbmFluidGuiHelper.TankData outputTank;

    public PrecassMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public PrecassMenu(int containerId, Inventory playerInventory, LegacyGenericSelectorMachineBlockEntity blockEntity) {
        super(ModMenuTypes.PRECASS.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 0, 152, 81));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 1, 35, 126));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), 2, 152, 108));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), 3, 152, 126));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int slot = 4 + column + row * 3;
                addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), slot,
                        8 + column * 18, 27 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int slot = 13 + column + row * 3;
                addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), slot,
                        80 + column * 18, 27 + row * 18));
            }
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 174, 232);
        addDataSlots();
    }

    public LegacyGenericSelectorMachineBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getProgressWidth(int maxWidth) {
        return progress * maxWidth / 10_000;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public HbmFluidGuiHelper.TankData getInputTankData() {
        return inputTank;
    }

    public HbmFluidGuiHelper.TankData getOutputTankData() {
        return outputTank;
    }

    public List<Component> getInputTankTooltip(boolean showHidden) {
        return inputTank.tooltip(showHidden);
    }

    public List<Component> getOutputTankTooltip(boolean showHidden) {
        return outputTank.tooltip(showHidden);
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
        } else if (!movePlayerStackToMachine(stack)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private boolean movePlayerStackToMachine(ItemStack stack) {
        if (stack.getItem() instanceof ItemBlueprints) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 1, 2);
        }
        if (stack.getItem() instanceof ItemMachineUpgrade) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 2, 4);
        }
        if (HbmInventoryMenuHelper.isLegacyBatteryItem(stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 0, 1);
        }
        return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 4, 22);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getPower(), () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getMaxPower(), () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addProgress(this::addDataSlot, () -> blockEntity.getProgress(), value -> progress = value);
        inputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getInputTank(0));
        outputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getOutputTank(0));
    }

    private static LegacyGenericSelectorMachineBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof LegacyGenericSelectorMachineBlockEntity machine
                && machine.kind() == LegacyGenericSelectorMachineBlockEntity.Kind.PRECASS) {
            return machine;
        }
        throw new IllegalStateException("Expected PRECASS block entity at " + pos);
    }
}
