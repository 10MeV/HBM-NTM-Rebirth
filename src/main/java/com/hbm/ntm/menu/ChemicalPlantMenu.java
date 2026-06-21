package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.ChemicalPlantBlockEntity;
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

public class ChemicalPlantMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = ChemicalPlantBlockEntity.ITEM_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final ChemicalPlantBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int progress;
    private final HbmFluidGuiHelper.TankData[] inputTanks = new HbmFluidGuiHelper.TankData[3];
    private final HbmFluidGuiHelper.TankData[] outputTanks = new HbmFluidGuiHelper.TankData[3];

    public ChemicalPlantMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ChemicalPlantMenu(int containerId, Inventory playerInventory, ChemicalPlantBlockEntity blockEntity) {
        super(ModMenuTypes.CHEMICAL_PLANT.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                ChemicalPlantBlockEntity.SLOT_BATTERY, 152, 81));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                ChemicalPlantBlockEntity.SLOT_BLUEPRINT, 35, 126));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), ChemicalPlantBlockEntity.SLOT_UPGRADE_START, 152, 108));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), ChemicalPlantBlockEntity.SLOT_UPGRADE_END, 152, 126));

        for (int i = 0; i < 3; i++) {
            addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                    ChemicalPlantBlockEntity.SLOT_ITEM_INPUT_START + i, 8 + i * 18, 99));
        }
        for (int i = 0; i < 3; i++) {
            addOutputSlot(ChemicalPlantBlockEntity.SLOT_ITEM_OUTPUT_START + i, 80 + i * 18, 99);
        }
        for (int i = 0; i < 3; i++) {
            addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                    ChemicalPlantBlockEntity.SLOT_FLUID_INPUT_START + i, 8 + i * 18, 54));
        }
        for (int i = 0; i < 3; i++) {
            addOutputSlot(ChemicalPlantBlockEntity.SLOT_FLUID_INPUT_RETURN_START + i, 8 + i * 18, 72);
        }
        for (int i = 0; i < 3; i++) {
            addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                    ChemicalPlantBlockEntity.SLOT_FLUID_OUTPUT_START + i, 80 + i * 18, 54));
        }
        for (int i = 0; i < 3; i++) {
            addOutputSlot(ChemicalPlantBlockEntity.SLOT_FLUID_OUTPUT_RETURN_START + i, 80 + i * 18, 72);
        }

        addPlayerInventory(playerInventory);
        addDataSlots();
    }

    public ChemicalPlantBlockEntity getBlockEntity() {
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

    public int getInputTankFillHeight(int index, int maxHeight) {
        return inputTanks[index].scaledFill(maxHeight);
    }

    public int getOutputTankFillHeight(int index, int maxHeight) {
        return outputTanks[index].scaledFill(maxHeight);
    }

    public int getInputTankTint(int index) {
        return inputTanks[index].guiTint();
    }

    public int getOutputTankTint(int index) {
        return outputTanks[index].guiTint();
    }

    public HbmFluidGuiHelper.TankData getInputTankData(int index) {
        return inputTanks[index];
    }

    public HbmFluidGuiHelper.TankData getOutputTankData(int index) {
        return outputTanks[index];
    }

    public Component getInputTankInfo(int index) {
        return inputTanks[index].info();
    }

    public Component getOutputTankInfo(int index) {
        return outputTanks[index].info();
    }

    public List<Component> getInputTankTooltip(int index) {
        return inputTanks[index].tooltip();
    }

    public List<Component> getOutputTankTooltip(int index) {
        return outputTanks[index].tooltip();
    }

    public List<Component> getInputTankTooltip(int index, boolean showHidden) {
        return inputTanks[index].tooltip(showHidden);
    }

    public List<Component> getOutputTankTooltip(int index, boolean showHidden) {
        return outputTanks[index].tooltip(showHidden);
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
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    ChemicalPlantBlockEntity.SLOT_BLUEPRINT, ChemicalPlantBlockEntity.SLOT_BLUEPRINT + 1);
        }
        if (stack.getItem() instanceof ItemMachineUpgrade) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    ChemicalPlantBlockEntity.SLOT_UPGRADE_START, ChemicalPlantBlockEntity.SLOT_UPGRADE_END + 1);
        }
        if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    ChemicalPlantBlockEntity.SLOT_BATTERY, ChemicalPlantBlockEntity.SLOT_BATTERY + 1);
        }
        return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                ChemicalPlantBlockEntity.SLOT_ITEM_INPUT_START, ChemicalPlantBlockEntity.SLOT_ITEM_INPUT_END + 1);
    }

    private void addOutputSlot(int slot, int x, int y) {
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), slot, x, y));
    }

    private void addPlayerInventory(Inventory inventory) {
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 174, 232);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getPower(), () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getMaxPower(), () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addProgress(this::addDataSlot, () -> blockEntity.getProgress(), value -> progress = value);
        for (int i = 0; i < 3; i++) {
            inputTanks[i] = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getInputTank(i));
            outputTanks[i] = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getOutputTank(i));
        }
    }

    private static ChemicalPlantBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof ChemicalPlantBlockEntity chemicalPlant) {
            return chemicalPlant;
        }
        throw new IllegalStateException("Expected chemical plant block entity at " + pos);
    }

}
