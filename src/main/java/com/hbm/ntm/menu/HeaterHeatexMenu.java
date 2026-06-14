package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.HeaterHeatexBlockEntity;
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

public class HeaterHeatexMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = HeaterHeatexBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final HeaterHeatexBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData inputTank;
    private HbmFluidGuiHelper.TankData outputTank;
    private int amountToCool;
    private int tickDelay;
    private int heatEnergy;
    private int lastInputUsed;
    private int lastOutputProduced;

    public HeaterHeatexMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public HeaterHeatexMenu(int containerId, Inventory playerInventory, HeaterHeatexBlockEntity blockEntity) {
        super(ModMenuTypes.HEATER_HEATEX.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                HeaterHeatexBlockEntity.SLOT_IDENTIFIER, 80, 72));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 122, 180);
        addDataSlots();
    }

    public HeaterHeatexBlockEntity getBlockEntity() {
        return blockEntity;
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

    public int getAmountToCool() {
        return amountToCool;
    }

    public int getTickDelay() {
        return tickDelay;
    }

    public int getHeatEnergy() {
        return heatEnergy;
    }

    public int getLastInputUsed() {
        return lastInputUsed;
    }

    public int getLastOutputProduced() {
        return lastOutputProduced;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 256.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < MACHINE_SLOT_COUNT) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END);
        }
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                HeaterHeatexBlockEntity.SLOT_IDENTIFIER, HeaterHeatexBlockEntity.SLOT_IDENTIFIER + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getAmountToCool, value -> amountToCool = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getTickDelay, value -> tickDelay = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHeatEnergy, value -> heatEnergy = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLastInputUsed, value -> lastInputUsed = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLastOutputProduced,
                value -> lastOutputProduced = value);
        inputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getInputTank());
        outputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getOutputTank());
    }

    private static HeaterHeatexBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof HeaterHeatexBlockEntity heatex) {
            return heatex;
        }
        throw new IllegalStateException("Expected heater heatex block entity at " + pos);
    }
}
