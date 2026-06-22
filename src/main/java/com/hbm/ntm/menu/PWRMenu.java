package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.PWRControllerBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.recipe.PWRFuelRuntime;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PWRMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = PWRControllerBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final PWRControllerBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData coolantTank;
    private final HbmFluidGuiHelper.TankData hotCoolantTank;
    private long coreHeat;
    private long coreHeatCapacity;
    private long hullHeat;
    private double flux;
    private double rodLevel;
    private double rodTarget;
    private int typeLoaded;
    private int amountLoaded;
    private double progress;
    private double processTime;
    private int rodCount;
    private int heatexCount;
    private int channelCount;
    private int heatsinkCount;
    private int sourceCount;
    private boolean assembled;

    public PWRMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public PWRMenu(int containerId, Inventory playerInventory, PWRControllerBlockEntity blockEntity) {
        super(ModMenuTypes.PWR.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                PWRControllerBlockEntity.SLOT_FUEL_INPUT, 53, 5));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(playerInventory.player, blockEntity.getItems(),
                PWRControllerBlockEntity.SLOT_HOT_OUTPUT, 89, 32));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                PWRControllerBlockEntity.SLOT_IDENTIFIER, 8, 59));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 106, 164);
        coolantTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getCoolantTank());
        hotCoolantTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getHotCoolantTank());
        addDataSlots();
    }

    public PWRControllerBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public HbmFluidGuiHelper.TankData getCoolantTank() {
        return coolantTank;
    }

    public HbmFluidGuiHelper.TankData getHotCoolantTank() {
        return hotCoolantTank;
    }

    public long getCoreHeat() {
        return coreHeat;
    }

    public long getCoreHeatCapacity() {
        return coreHeatCapacity;
    }

    public long getHullHeat() {
        return hullHeat;
    }

    public int getFluxScaled() {
        return (int) Math.round(flux * 1000.0D);
    }

    public double getFlux() {
        return flux;
    }

    public int getRodLevel() {
        return (int) Math.round(rodLevel);
    }

    public int getRodTarget() {
        return (int) Math.round(rodTarget);
    }

    public double getRodLevelExact() {
        return rodLevel;
    }

    public double getRodTargetExact() {
        return rodTarget;
    }

    public int getTypeLoaded() {
        return typeLoaded;
    }

    public int getAmountLoaded() {
        return amountLoaded;
    }

    public double getProgress() {
        return progress;
    }

    public double getProcessTime() {
        return processTime;
    }

    public int getRodCount() {
        return rodCount;
    }

    public int getHeatexCount() {
        return heatexCount;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public int getHeatsinkCount() {
        return heatsinkCount;
    }

    public int getSourceCount() {
        return sourceCount;
    }

    public boolean isAssembled() {
        return assembled;
    }

    public int getCoreHeatScaled(int max) {
        return coreHeatCapacity <= 0L ? 0 : (int) Math.min(max, coreHeat * max / coreHeatCapacity);
    }

    public int getHullHeatScaled(int max) {
        return (int) Math.min(max, hullHeat * max / PWRControllerBlockEntity.HULL_HEAT_CAPACITY_BASE);
    }

    public int getProgressScaled(int max) {
        return (int) (progress * max / processTime);
    }

    public int getRodLevelScaled(int max) {
        return (int) (rodLevel * max / 100.0D);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity,
                PWRControllerBlockEntity.USE_DISTANCE_SQR);
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
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (PWRFuelRuntime.isFuel(stack)) {
            if (!moveItemStackTo(stack, PWRControllerBlockEntity.SLOT_FUEL_INPUT,
                    PWRControllerBlockEntity.SLOT_FUEL_INPUT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof IFluidIdentifierItem) {
            if (!moveItemStackTo(stack, PWRControllerBlockEntity.SLOT_IDENTIFIER,
                    PWRControllerBlockEntity.SLOT_IDENTIFIER + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, PWRControllerBlockEntity.SLOT_FUEL_INPUT,
                PWRControllerBlockEntity.SLOT_FUEL_INPUT + 1, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getCoreHeat, () -> coreHeat, value -> coreHeat = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getCoreHeatCapacity,
                () -> coreHeatCapacity, value -> coreHeatCapacity = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getHullHeat, () -> hullHeat, value -> hullHeat = value);
        HbmMenuDataSlots.addDouble(this::addDataSlot, blockEntity::getFlux, () -> flux, value -> flux = value);
        HbmMenuDataSlots.addDouble(this::addDataSlot, blockEntity::getRodLevel, () -> rodLevel, value -> rodLevel = value);
        HbmMenuDataSlots.addDouble(this::addDataSlot, blockEntity::getRodTarget, () -> rodTarget, value -> rodTarget = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getTypeLoaded, value -> typeLoaded = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getAmountLoaded, value -> amountLoaded = value);
        HbmMenuDataSlots.addDouble(this::addDataSlot, blockEntity::getProgress, () -> progress, value -> progress = value);
        HbmMenuDataSlots.addDouble(this::addDataSlot, blockEntity::getProcessTime,
                () -> processTime, value -> processTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getRodCount, value -> rodCount = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHeatexCount, value -> heatexCount = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getChannelCount, value -> channelCount = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHeatsinkCount, value -> heatsinkCount = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getSourceCount, value -> sourceCount = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isAssembled, value -> assembled = value);
    }

    private static PWRControllerBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof PWRControllerBlockEntity pwr) {
            return pwr;
        }
        throw new IllegalStateException("Expected PWR controller block entity at " + pos);
    }
}
