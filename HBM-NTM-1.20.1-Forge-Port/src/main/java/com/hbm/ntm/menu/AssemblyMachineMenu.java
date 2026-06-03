package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

import java.util.List;

public class AssemblyMachineMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 17;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final AssemblyMachineBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int progress;
    private HbmFluidGuiHelper.TankData inputTank;
    private HbmFluidGuiHelper.TankData outputTank;

    public AssemblyMachineMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public AssemblyMachineMenu(int containerId, Inventory playerInventory, AssemblyMachineBlockEntity blockEntity) {
        super(ModMenuTypes.ASSEMBLY_MACHINE.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(new SlotItemHandler(blockEntity.getItems(), AssemblyMachineBlockEntity.SLOT_BATTERY, 152, 81));
        addSlot(new SlotItemHandler(blockEntity.getItems(), AssemblyMachineBlockEntity.SLOT_BLUEPRINT, 35, 126));
        addSlot(new SlotItemHandler(blockEntity.getItems(), AssemblyMachineBlockEntity.SLOT_UPGRADE_START, 152, 108));
        addSlot(new SlotItemHandler(blockEntity.getItems(), AssemblyMachineBlockEntity.SLOT_UPGRADE_END, 152, 126));
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 3; column++) {
                int slot = AssemblyMachineBlockEntity.SLOT_INPUT_START + column + row * 3;
                addSlot(new SlotItemHandler(blockEntity.getItems(), slot, 8 + column * 18, 18 + row * 18));
            }
        }
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), AssemblyMachineBlockEntity.SLOT_OUTPUT, 98, 45));
        addPlayerInventory(playerInventory);
        addDataSlots();
    }

    public AssemblyMachineBlockEntity getBlockEntity() {
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

    public int getInputTankWidth(int maxWidth) {
        return inputTank.scaledFill(maxWidth);
    }

    public int getOutputTankWidth(int maxWidth) {
        return outputTank.scaledFill(maxWidth);
    }

    public int getInputTankTint() {
        return inputTank.guiTint();
    }

    public int getOutputTankTint() {
        return outputTank.guiTint();
    }

    public HbmFluidGuiHelper.TankData getInputTankData() {
        return inputTank;
    }

    public HbmFluidGuiHelper.TankData getOutputTankData() {
        return outputTank;
    }

    public Component getInputTankInfo() {
        return inputTank.info();
    }

    public Component getOutputTankInfo() {
        return outputTank.info();
    }

    public List<Component> getInputTankTooltip() {
        return inputTank.tooltip();
    }

    public List<Component> getOutputTankTooltip() {
        return outputTank.tooltip();
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index, MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START,
                HOTBAR_END,
                AssemblyMachineBlockEntity.SLOT_INPUT_START, AssemblyMachineBlockEntity.SLOT_INPUT_END + 1,
                AssemblyMachineBlockEntity.SLOT_BATTERY, AssemblyMachineBlockEntity.SLOT_BATTERY + 1);
    }

    private void addPlayerInventory(Inventory inventory) {
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 174, 232);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getPower(), () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getMaxPower(), () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addProgress(this::addDataSlot, () -> blockEntity.getProgress(), value -> progress = value);
        inputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getInputTank());
        outputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getOutputTank());
    }

    private static AssemblyMachineBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof AssemblyMachineBlockEntity assembler) {
            return assembler;
        }
        throw new IllegalStateException("Expected assembly machine block entity at " + pos);
    }

}
