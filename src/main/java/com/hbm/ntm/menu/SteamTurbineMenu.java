package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.SteamTurbineBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SteamTurbineMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = SteamTurbineBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final SteamTurbineBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData inputTank;
    private HbmFluidGuiHelper.TankData outputTank;
    private long power;
    private long maxPower;
    private long lastPowerProduced;

    public SteamTurbineMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public SteamTurbineMenu(int containerId, Inventory playerInventory, SteamTurbineBlockEntity blockEntity) {
        super(ModMenuTypes.STEAM_TURBINE.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                SteamTurbineBlockEntity.SLOT_IDENTIFIER, 8, 17));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                SteamTurbineBlockEntity.SLOT_IDENTIFIER_OUTPUT, 8, 53));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                SteamTurbineBlockEntity.SLOT_INPUT_CONTAINER, 44, 17));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                SteamTurbineBlockEntity.SLOT_INPUT_CONTAINER_OUTPUT, 44, 53));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                SteamTurbineBlockEntity.SLOT_BATTERY, 98, 53));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                SteamTurbineBlockEntity.SLOT_OUTPUT_CONTAINER, 152, 17));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                SteamTurbineBlockEntity.SLOT_OUTPUT_CONTAINER_OUTPUT, 152, 53));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
        addDataSlots();
    }

    public SteamTurbineBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public long getLastPowerProduced() {
        return lastPowerProduced;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public HbmFluidGuiHelper.TankData getInputTank() {
        return inputTank;
    }

    public HbmFluidGuiHelper.TankData getOutputTank() {
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
        if (index < MACHINE_SLOT_COUNT) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END);
        }
        if (index < 0 || index >= slots.size() || !slots.get(index).hasItem()) {
            return ItemStack.EMPTY;
        }
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                SteamTurbineBlockEntity.SLOT_BATTERY, SteamTurbineBlockEntity.SLOT_BATTERY + 1,
                SteamTurbineBlockEntity.SLOT_INPUT_CONTAINER, SteamTurbineBlockEntity.SLOT_INPUT_CONTAINER + 1,
                SteamTurbineBlockEntity.SLOT_OUTPUT_CONTAINER, SteamTurbineBlockEntity.SLOT_OUTPUT_CONTAINER + 1,
                SteamTurbineBlockEntity.SLOT_IDENTIFIER, SteamTurbineBlockEntity.SLOT_IDENTIFIER + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getLastPowerProduced,
                () -> lastPowerProduced, value -> lastPowerProduced = value);
        inputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getInputTank());
        outputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getOutputTank());
    }

    private static SteamTurbineBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof SteamTurbineBlockEntity turbine) {
            return turbine;
        }
        throw new IllegalStateException("Expected steam turbine block entity at " + pos);
    }
}
