package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.ChemicalFactoryBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.ItemBlueprints;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
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

public class ChemicalFactoryMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 32;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final ChemicalFactoryBlockEntity blockEntity;
    private final int[] progress = new int[4];
    private final HbmFluidGuiHelper.TankData[][] inputTanks = new HbmFluidGuiHelper.TankData[4][3];
    private final HbmFluidGuiHelper.TankData[][] outputTanks = new HbmFluidGuiHelper.TankData[4][3];
    private HbmFluidGuiHelper.TankData waterTank;
    private HbmFluidGuiHelper.TankData spentSteamTank;
    private long power;
    private long maxPower;
    private int canCool;

    public ChemicalFactoryMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ChemicalFactoryMenu(int containerId, Inventory playerInventory, ChemicalFactoryBlockEntity blockEntity) {
        super(ModMenuTypes.CHEMICAL_FACTORY.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                ChemicalFactoryBlockEntity.SLOT_BATTERY, 224, 88));
        HbmInventoryMenuHelper.addUpgradeSlots(this::addSlot, blockEntity.getItems(),
                ChemicalFactoryBlockEntity.SLOT_UPGRADE_START, 206, 125, 1, 3);
        for (int module = 0; module < 4; module++) {
            int y = 20 + module * 22;
            addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                    ChemicalFactoryBlockEntity.blueprintSlot(module), 93, y));
            for (int i = 0; i < 3; i++) {
                addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                        ChemicalFactoryBlockEntity.inputStart(module) + i, 10 + i * 16, y));
            }
            for (int i = 0; i < 3; i++) {
                addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                        ChemicalFactoryBlockEntity.outputStart(module) + i, 139 + i * 16, y));
            }
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 26, 134, 192);
        addDataSlots();
    }

    public ChemicalFactoryBlockEntity getBlockEntity() {
        return blockEntity;
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

    public int getProgressWidth(int module, int maxWidth) {
        return progress[module] * maxWidth / 10_000;
    }

    public boolean canCool() {
        return canCool != 0;
    }

    public HbmFluidGuiHelper.TankData getInputTankData(int module, int tank) {
        return inputTanks[module][tank];
    }

    public HbmFluidGuiHelper.TankData getOutputTankData(int module, int tank) {
        return outputTanks[module][tank];
    }

    public HbmFluidGuiHelper.TankData getWaterTankData() {
        return waterTank;
    }

    public HbmFluidGuiHelper.TankData getSpentSteamTankData() {
        return spentSteamTank;
    }

    public List<Component> getInputTankTooltip(int module, int tank, boolean showHidden) {
        return inputTanks[module][tank].tooltip(showHidden);
    }

    public List<Component> getOutputTankTooltip(int module, int tank, boolean showHidden) {
        return outputTanks[module][tank].tooltip(showHidden);
    }

    public List<Component> getWaterTankTooltip(boolean showHidden) {
        return waterTank.tooltip(showHidden);
    }

    public List<Component> getSpentSteamTankTooltip(boolean showHidden) {
        return spentSteamTank.tooltip(showHidden);
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
        if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    ChemicalFactoryBlockEntity.SLOT_BATTERY, ChemicalFactoryBlockEntity.SLOT_BATTERY + 1);
        }
        if (stack.getItem() instanceof ItemBlueprints) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    ChemicalFactoryBlockEntity.blueprintSlot(0), ChemicalFactoryBlockEntity.blueprintSlot(0) + 1,
                    ChemicalFactoryBlockEntity.blueprintSlot(1), ChemicalFactoryBlockEntity.blueprintSlot(1) + 1,
                    ChemicalFactoryBlockEntity.blueprintSlot(2), ChemicalFactoryBlockEntity.blueprintSlot(2) + 1,
                    ChemicalFactoryBlockEntity.blueprintSlot(3), ChemicalFactoryBlockEntity.blueprintSlot(3) + 1);
        }
        if (stack.getItem() instanceof ItemMachineUpgrade) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    ChemicalFactoryBlockEntity.SLOT_UPGRADE_START, ChemicalFactoryBlockEntity.SLOT_UPGRADE_END + 1);
        }
        return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                ChemicalFactoryBlockEntity.inputStart(0), ChemicalFactoryBlockEntity.inputEnd(0) + 1,
                ChemicalFactoryBlockEntity.inputStart(1), ChemicalFactoryBlockEntity.inputEnd(1) + 1,
                ChemicalFactoryBlockEntity.inputStart(2), ChemicalFactoryBlockEntity.inputEnd(2) + 1,
                ChemicalFactoryBlockEntity.inputStart(3), ChemicalFactoryBlockEntity.inputEnd(3) + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        for (int module = 0; module < 4; module++) {
            int capturedModule = module;
            HbmMenuDataSlots.addProgress(this::addDataSlot, () -> blockEntity.getProgress(capturedModule),
                    value -> progress[capturedModule] = value);
            for (int tank = 0; tank < 3; tank++) {
                inputTanks[module][tank] = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getInputTank(module, tank));
                outputTanks[module][tank] = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getOutputTank(module, tank));
            }
        }
        waterTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getWaterTank());
        spentSteamTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getSpentSteamTank());
        HbmMenuDataSlots.addBoolean(this::addDataSlot,
                () -> blockEntity.getWaterTank().getFill() >= 100
                        && blockEntity.getSpentSteamTank().getFill() <= blockEntity.getSpentSteamTank().getMaxFill() - 100,
                value -> canCool = value ? 1 : 0);
    }

    private static ChemicalFactoryBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof ChemicalFactoryBlockEntity factory) {
            return factory;
        }
        throw new IllegalStateException("Expected chemical factory block entity at " + pos);
    }
}
