package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.CombustionEngineBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.PistonSetItem;
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

public class CombustionEngineMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 5;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final CombustionEngineBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData tank;
    private long power;
    private long maxPower;
    private boolean on;
    private int throttle;
    private int fuelUsedTenths;
    private long lastPowerProduced;

    public CombustionEngineMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public CombustionEngineMenu(int containerId, Inventory playerInventory, CombustionEngineBlockEntity blockEntity) {
        super(ModMenuTypes.COMBUSTION_ENGINE.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                CombustionEngineBlockEntity.SLOT_FLUID_INPUT, 17, 17));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(playerInventory.player, blockEntity.getItems(),
                CombustionEngineBlockEntity.SLOT_FLUID_OUTPUT, 17, 53));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                CombustionEngineBlockEntity.SLOT_PISTON, 88, 71));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                CombustionEngineBlockEntity.SLOT_ENERGY_OUTPUT, 143, 71));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                CombustionEngineBlockEntity.SLOT_IDENTIFIER, 35, 71));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 121, 179);
        addDataSlots();
    }

    public CombustionEngineBlockEntity getBlockEntity() {
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

    public int getThrottle() {
        return throttle;
    }

    public int getFuelUsedTenths() {
        return fuelUsedTenths;
    }

    public long getLastPowerProduced() {
        return lastPowerProduced;
    }

    public HbmFluidGuiHelper.TankData getTankData() {
        return tank;
    }

    public List<Component> getTankTooltip(boolean showHidden) {
        return tank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 625.0D);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        blockEntity.closeMenu(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < MACHINE_SLOT_COUNT) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END);
        }
        ItemStack stack = slots.get(index).getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (HbmInventoryMenuHelper.isLegacyBatteryItem(stack)) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    CombustionEngineBlockEntity.SLOT_ENERGY_OUTPUT,
                    CombustionEngineBlockEntity.SLOT_ENERGY_OUTPUT + 1);
        }
        if (stack.getItem() instanceof IFluidIdentifierItem) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    CombustionEngineBlockEntity.SLOT_IDENTIFIER,
                    CombustionEngineBlockEntity.SLOT_IDENTIFIER + 1);
        }
        if (stack.getItem() instanceof PistonSetItem) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    CombustionEngineBlockEntity.SLOT_PISTON,
                    CombustionEngineBlockEntity.SLOT_PISTON + 1);
        }
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                CombustionEngineBlockEntity.SLOT_FLUID_INPUT,
                CombustionEngineBlockEntity.SLOT_FLUID_INPUT + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isOn, value -> on = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getThrottle, value -> throttle = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getFuelUsedTenths, value -> fuelUsedTenths = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getLastPowerProduced,
                () -> lastPowerProduced, value -> lastPowerProduced = value);
        tank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getTank());
    }

    private static CombustionEngineBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof CombustionEngineBlockEntity engine) {
            return engine;
        }
        throw new IllegalStateException("Expected combustion engine block entity at " + pos);
    }
}
