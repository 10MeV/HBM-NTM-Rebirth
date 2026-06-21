package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.GasFlareBlockEntity;
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

public class GasFlareMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 6;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final GasFlareBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData tank;
    private long power;
    private long maxPower;
    private boolean on;
    private boolean burn;
    private int fluidUsed;
    private int output;

    public GasFlareMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public GasFlareMenu(int containerId, Inventory playerInventory, GasFlareBlockEntity blockEntity) {
        super(ModMenuTypes.GAS_FLARE.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), GasFlareBlockEntity.SLOT_ENERGY_OUTPUT, 143, 71));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), GasFlareBlockEntity.SLOT_FLUID_INPUT, 17, 17));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), GasFlareBlockEntity.SLOT_FLUID_OUTPUT, 17, 53));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), GasFlareBlockEntity.SLOT_IDENTIFIER, 35, 71));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), GasFlareBlockEntity.SLOT_UPGRADE_SPEED, 80, 71));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), GasFlareBlockEntity.SLOT_UPGRADE_EFFECT, 98, 71));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 121, 179);
        addDataSlots();
    }

    public GasFlareBlockEntity getBlockEntity() {
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

    public boolean isOn() {
        return on;
    }

    public boolean doesBurn() {
        return burn;
    }

    public int getFluidUsed() {
        return fluidUsed;
    }

    public int getOutput() {
        return output;
    }

    public HbmFluidGuiHelper.TankData getTankData() {
        return tank;
    }

    public List<Component> getTankTooltip(boolean showHidden) {
        return tank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 256.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                GasFlareBlockEntity.SLOT_IDENTIFIER, GasFlareBlockEntity.SLOT_IDENTIFIER + 1,
                GasFlareBlockEntity.SLOT_ENERGY_OUTPUT, GasFlareBlockEntity.SLOT_ENERGY_OUTPUT + 1,
                GasFlareBlockEntity.SLOT_UPGRADE_SPEED, GasFlareBlockEntity.SLOT_UPGRADE_EFFECT + 1,
                GasFlareBlockEntity.SLOT_FLUID_INPUT, GasFlareBlockEntity.SLOT_FLUID_INPUT + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isOn, value -> on = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::doesBurn, value -> burn = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getFluidUsed, value -> fluidUsed = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLastOutput, value -> output = value);
        tank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getTank());
    }

    private static GasFlareBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof GasFlareBlockEntity gasFlare) {
            return gasFlare;
        }
        throw new IllegalStateException("Expected gas flare block entity at " + pos);
    }
}
