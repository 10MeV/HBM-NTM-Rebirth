package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.MachineBatterySocketBlockEntity;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MachineBatterySocketMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 1;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final MachineBatterySocketBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private long delta;
    private int redLow;
    private int redHigh;
    private int priorityOrdinal;

    public MachineBatterySocketMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public MachineBatterySocketMenu(int containerId, Inventory playerInventory, MachineBatterySocketBlockEntity blockEntity) {
        super(ModMenuTypes.MACHINE_BATTERY_SOCKET.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                MachineBatterySocketBlockEntity.SLOT_BATTERY, 35, 35));
        addPlayerInventory(playerInventory);
        addBatteryDataSlots();
    }

    public MachineBatterySocketBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public long getDelta() {
        return delta;
    }

    public int getRedLow() {
        return redLow;
    }

    public int getRedHigh() {
        return redHigh;
    }

    public HbmEnergyReceiver.ConnectionPriority getPriority() {
        HbmEnergyReceiver.ConnectionPriority[] values = HbmEnergyReceiver.ConnectionPriority.values();
        return priorityOrdinal >= 0 && priorityOrdinal < values.length ? values[priorityOrdinal] : HbmEnergyReceiver.ConnectionPriority.LOW;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : Mth.clamp((int) Math.floor((double) power / (double) maxPower * maxHeight), 0, maxHeight);
    }

    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.isRemoved() && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
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

    private void addBatteryDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getDeltaPerSecond, () -> delta, value -> delta = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getRedLow, value -> redLow = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getRedHigh, value -> redHigh = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getBatteryPriority().ordinal(),
                value -> priorityOrdinal = value);
    }

    private static MachineBatterySocketBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof MachineBatterySocketBlockEntity socket) {
            return socket;
        }
        throw new IllegalStateException("Expected machine battery socket block entity at " + pos);
    }
}
