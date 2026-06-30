package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RadiolysisBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.multiblock.MultiblockHelper;
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

public class RadiolysisMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = RadiolysisBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;
    private final RadiolysisBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData inputTank;
    private HbmFluidGuiHelper.TankData outputTank1;
    private HbmFluidGuiHelper.TankData outputTank2;
    private long power;
    private long maxPower;
    private int heat;

    public RadiolysisMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public RadiolysisMenu(int containerId, Inventory playerInventory, RadiolysisBlockEntity blockEntity) {
        super(ModMenuTypes.RADIOLYSIS.get(), containerId);
        this.blockEntity = blockEntity;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), j + i * 5,
                        188 + i * 18, 8 + j * 18));
            }
        }
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                RadiolysisBlockEntity.SLOT_FLUID_ID_INPUT, 34, 17));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                RadiolysisBlockEntity.SLOT_FLUID_ID_OUTPUT, 34, 53));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                RadiolysisBlockEntity.SLOT_STERILIZE_INPUT, 148, 17));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                RadiolysisBlockEntity.SLOT_STERILIZE_OUTPUT, 148, 53));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                RadiolysisBlockEntity.SLOT_BATTERY, 8, 53));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
        addDataSlots();
    }

    public RadiolysisBlockEntity getBlockEntity() {
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

    public int getHeat() {
        return heat;
    }

    public long getProduction() {
        return heat * 10L;
    }

    public HbmFluidGuiHelper.TankData getInputTank() {
        return inputTank;
    }

    public HbmFluidGuiHelper.TankData getOutputTank1() {
        return outputTank1;
    }

    public HbmFluidGuiHelper.TankData getOutputTank2() {
        return outputTank2;
    }

    public List<Component> getTankTooltip(HbmFluidGuiHelper.TankData tank, boolean showHidden) {
        return tank.tooltip(showHidden);
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
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                RadiolysisBlockEntity.SLOT_RTG_START, MACHINE_SLOT_COUNT);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower,
                value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHeat, value -> heat = value);
        inputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getInputTank());
        outputTank1 = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getOutputTank1());
        outputTank2 = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getOutputTank2());
    }

    private static RadiolysisBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof RadiolysisBlockEntity radiolysis) {
            return radiolysis;
        }
        throw new IllegalStateException("Expected radiolysis block entity at " + pos);
    }
}
