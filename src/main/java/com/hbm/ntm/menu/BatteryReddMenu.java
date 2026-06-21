package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.BatteryReddBlockEntity;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import java.math.BigInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BatteryReddMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = BatteryReddBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final BatteryReddBlockEntity blockEntity;
    private int redLow;
    private int redHigh;
    private int priorityOrdinal;

    public BatteryReddMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public BatteryReddMenu(int containerId, Inventory inventory, BatteryReddBlockEntity blockEntity) {
        super(ModMenuTypes.BATTERY_REDD.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                BatteryReddBlockEntity.SLOT_DISCHARGE, 26, 53));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                BatteryReddBlockEntity.SLOT_CHARGE, 80, 53));
        addPlayerInventory(inventory);
        addDataSlots();
    }

    public BatteryReddBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public BigInteger getPowerBig() {
        return blockEntity.getBigPower();
    }

    public BigInteger getDeltaBig() {
        return blockEntity.getDelta();
    }

    public int getRedLow() {
        return redLow;
    }

    public int getRedHigh() {
        return redHigh;
    }

    public HbmEnergyReceiver.ConnectionPriority getPriority() {
        HbmEnergyReceiver.ConnectionPriority[] values = HbmEnergyReceiver.ConnectionPriority.values();
        return priorityOrdinal >= 0 && priorityOrdinal < values.length
                ? values[priorityOrdinal]
                : HbmEnergyReceiver.ConnectionPriority.LOW;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index, MACHINE_SLOT_COUNT,
                PLAYER_INVENTORY_START, HOTBAR_END, 0, MACHINE_SLOT_COUNT);
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 99 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 157));
        }
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getRedLow, value -> redLow = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getRedHigh, value -> redHigh = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getBatteryPriority().ordinal(),
                value -> priorityOrdinal = value);
    }

    private static BatteryReddBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof BatteryReddBlockEntity battery) {
            return battery;
        }
        throw new IllegalStateException("Expected REDD battery block entity at " + pos);
    }
}
