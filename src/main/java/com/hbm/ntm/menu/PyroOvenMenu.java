package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.PyroOvenBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import java.util.List;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PyroOvenMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = PyroOvenBlockEntity.ITEM_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final PyroOvenBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int progress;
    private int usage;
    private HbmFluidGuiHelper.TankData inputTank;
    private HbmFluidGuiHelper.TankData outputTank;

    public PyroOvenMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public PyroOvenMenu(int containerId, Inventory playerInventory, PyroOvenBlockEntity blockEntity) {
        super(ModMenuTypes.PYRO_OVEN.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), PyroOvenBlockEntity.SLOT_BATTERY, 152, 72));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), PyroOvenBlockEntity.SLOT_INPUT, 35, 45));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), PyroOvenBlockEntity.SLOT_OUTPUT, 89, 45));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), PyroOvenBlockEntity.SLOT_IDENTIFIER, 8, 72));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), PyroOvenBlockEntity.SLOT_UPGRADE_1, 71, 72));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), PyroOvenBlockEntity.SLOT_UPGRADE_2, 89, 72));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 122, 180);
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
        return progress * maxWidth / 10_000;
    }

    public int getUsage() {
        return usage;
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
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                PyroOvenBlockEntity.SLOT_BATTERY, PyroOvenBlockEntity.SLOT_BATTERY + 1,
                PyroOvenBlockEntity.SLOT_IDENTIFIER, PyroOvenBlockEntity.SLOT_IDENTIFIER + 1,
                PyroOvenBlockEntity.SLOT_UPGRADE_1, PyroOvenBlockEntity.SLOT_UPGRADE_2 + 1,
                PyroOvenBlockEntity.SLOT_INPUT, PyroOvenBlockEntity.SLOT_INPUT + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> (int) (blockEntity.getProgressFraction() * 10_000.0F), value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getUsage, value -> usage = value);
        inputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getInputTank());
        outputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getOutputTank());
    }

    private static PyroOvenBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof PyroOvenBlockEntity pyroOven) {
            return pyroOven;
        }
        throw new IllegalStateException("Expected pyro oven block entity at " + pos);
    }
}
