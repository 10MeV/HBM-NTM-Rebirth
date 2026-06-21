package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.BlastFurnaceBlockEntity;
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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeHooks;

public class BlastFurnaceMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = BlastFurnaceBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final BlastFurnaceBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData airblastTank;
    private HbmFluidGuiHelper.TankData flueTank;
    private int progressScaled;
    private int speedPercent;
    private int fuel;
    private boolean progressing;

    public BlastFurnaceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public BlastFurnaceMenu(int containerId, Inventory playerInventory, BlastFurnaceBlockEntity blockEntity) {
        super(ModMenuTypes.BLAST_FURNACE.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                BlastFurnaceBlockEntity.SLOT_FUEL, 80, 81));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                BlastFurnaceBlockEntity.SLOT_INPUT_1, 80, 27));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                BlastFurnaceBlockEntity.SLOT_INPUT_2, 80, 45));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(playerInventory.player, blockEntity.getItems(),
                BlastFurnaceBlockEntity.SLOT_OUTPUT_1, 134, 72));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(playerInventory.player, blockEntity.getItems(),
                BlastFurnaceBlockEntity.SLOT_OUTPUT_2, 134, 90));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 140, 198);
        addDataSlots();
    }

    public BlastFurnaceBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getProgressScaled() {
        return progressScaled;
    }

    public double getProgressRatio() {
        return Math.max(0.0D, Math.min(1.0D, progressScaled / 10_000.0D));
    }

    public int getSpeedPercent() {
        return speedPercent;
    }

    public int getFuel() {
        return fuel;
    }

    public int getFuelPixels() {
        return Math.max(0, Math.min(26, Math.round(fuel * 26.0F / BlastFurnaceBlockEntity.MAX_FUEL)));
    }

    public boolean isProgressing() {
        return progressing;
    }

    public HbmFluidGuiHelper.TankData getAirblastTank() {
        return airblastTank;
    }

    public HbmFluidGuiHelper.TankData getFlueTank() {
        return flueTank;
    }

    public List<Component> getAirblastTooltip(boolean showHidden) {
        return airblastTank.tooltip(showHidden);
    }

    public List<Component> getFlueTooltip(boolean showHidden) {
        return flueTank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < MACHINE_SLOT_COUNT) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END);
        }
        ItemStack stack = slots.get(index).getItem();
        if (ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    BlastFurnaceBlockEntity.SLOT_FUEL, BlastFurnaceBlockEntity.SLOT_FUEL + 1);
        }
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                BlastFurnaceBlockEntity.SLOT_INPUT_1, BlastFurnaceBlockEntity.SLOT_INPUT_2 + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgressScaled,
                value -> progressScaled = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getSpeedPercent,
                value -> speedPercent = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getFuel, value -> fuel = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isProgressing,
                value -> progressing = value);
        airblastTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getAirblastTank());
        flueTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getFlueTank());
    }

    private static BlastFurnaceBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof BlastFurnaceBlockEntity furnace) {
            return furnace;
        }
        throw new IllegalStateException("Expected blast furnace block entity at " + pos);
    }
}
