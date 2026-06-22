package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.TurbineGasBlockEntity;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.fluid.FluidType;
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

public class TurbineGasMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = TurbineGasBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final TurbineGasBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData fuelTank;
    private HbmFluidGuiHelper.TankData lubricantTank;
    private HbmFluidGuiHelper.TankData waterTank;
    private HbmFluidGuiHelper.TankData steamTank;
    private long power;
    private long maxPower;
    private int rpm;
    private int temperature;
    private int sliderPos;
    private int throttle;
    private boolean autoMode;
    private int state;
    private int counter;
    private int instantPowerOutput;

    public TurbineGasMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public TurbineGasMenu(int containerId, Inventory playerInventory, TurbineGasBlockEntity blockEntity) {
        super(ModMenuTypes.TURBINE_GAS.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                TurbineGasBlockEntity.SLOT_BATTERY, 8, 109));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                TurbineGasBlockEntity.SLOT_IDENTIFIER, 36, 17));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 141, 199);
        addDataSlots();
    }

    public TurbineGasBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getPowerBarWidth(int maxWidth) {
        return maxPower <= 0L ? 0 : (int) (power * maxWidth / maxPower);
    }

    public int getRpm() {
        return rpm;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getSliderPos() {
        return sliderPos;
    }

    public int getThrottle() {
        return throttle;
    }

    public boolean isAutoMode() {
        return autoMode;
    }

    public int getState() {
        return state;
    }

    public int getCounter() {
        return counter;
    }

    public int getInstantPowerOutput() {
        return instantPowerOutput;
    }

    public HbmFluidGuiHelper.TankData getFuelTank() {
        return fuelTank;
    }

    public HbmFluidGuiHelper.TankData getLubricantTank() {
        return lubricantTank;
    }

    public HbmFluidGuiHelper.TankData getWaterTank() {
        return waterTank;
    }

    public HbmFluidGuiHelper.TankData getSteamTank() {
        return steamTank;
    }

    public List<Component> getFuelTooltip(boolean showHidden) {
        return fuelTank.tooltip(showHidden);
    }

    public List<Component> getLubricantTooltip(boolean showHidden) {
        return lubricantTank.tooltip(showHidden);
    }

    public List<Component> getWaterTooltip(boolean showHidden) {
        return waterTank.tooltip(showHidden);
    }

    public List<Component> getSteamTooltip(boolean showHidden) {
        return steamTank.tooltip(showHidden);
    }

    public double fuelConsumptionPerSecond() {
        if (state != 1) {
            return 0.0D;
        }
        double consumption = TurbineGasBlockEntity.maxFuelConsumption(fuelTank.type());
        return 20.0D * (consumption * 0.05D + consumption * throttle / 100.0D);
    }

    public boolean hasLowFuelOrLube() {
        return fuelTank.fill() < 5_000 || lubricantTank.fill() < 1_000;
    }

    public boolean hasNoFuelOrLube() {
        return fuelTank.fill() == 0 || lubricantTank.fill() == 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 625.0D);
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
        if (stack.getItem() instanceof HbmBatteryItem) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    TurbineGasBlockEntity.SLOT_BATTERY, TurbineGasBlockEntity.SLOT_BATTERY + 1);
        }
        if (stack.getItem() instanceof IFluidIdentifierItem identifier) {
            FluidType type = identifier.getIdentifiedFluid(player.level(), blockEntity.getBlockPos(), stack);
            if (TurbineGasBlockEntity.isGasFuel(type)) {
                return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                        MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                        TurbineGasBlockEntity.SLOT_IDENTIFIER, TurbineGasBlockEntity.SLOT_IDENTIFIER + 1);
            }
        }
        return ItemStack.EMPTY;
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getRpm, value -> rpm = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getTemperature, value -> temperature = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getSliderPos, value -> sliderPos = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getThrottle, value -> throttle = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isAutoMode, value -> autoMode = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getState, value -> state = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getCounter, value -> counter = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getInstantPowerOutput,
                value -> instantPowerOutput = value);
        fuelTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getFuelTank());
        lubricantTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getLubricantTank());
        waterTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getWaterTank());
        steamTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getSteamTank());
    }

    private static TurbineGasBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof TurbineGasBlockEntity turbine) {
            return turbine;
        }
        throw new IllegalStateException("Expected gas turbine block entity at " + pos);
    }
}
