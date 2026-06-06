package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.HbmBatteryTransfer;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.menu.MachineBatteryMenu;
import com.hbm.ntm.network.HbmTileSyncable;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MachineBatteryBlockEntity extends HbmEnergyNetworkBlockEntity implements MenuProvider, HbmTileSyncable,
        RORValueProvider, RORInteractive, LegacyLookOverlayProvider {
    private static final String TAG_INVENTORY = "Inventory";
    private static final long MAX_POWER = 1_000_000L;
    private static final long MAX_RECEIVE = MAX_POWER / 200L;
    private static final long MAX_EXTRACT = MAX_POWER / 600L;

    public static final int MODE_INPUT = 0;
    public static final int MODE_BUFFER = 1;
    public static final int MODE_OUTPUT = 2;
    public static final int MODE_NONE = 3;

    private static final String TAG_RED_LOW = "redLow";
    private static final String TAG_RED_HIGH = "redHigh";
    private static final String TAG_LAST_REDSTONE = "lastRedstone";
    private static final String TAG_PRIORITY = "priority";
    private static final String TAG_CONTROL = "control";

    public static final int SLOT_DISCHARGE = 0;
    public static final int SLOT_CHARGE = 1;
    public static final int CONTROL_RED_LOW = 0;
    public static final int CONTROL_RED_HIGH = 1;
    public static final int CONTROL_PRIORITY = 2;
    private static final int[] SLOTS_TOP = new int[]{SLOT_DISCHARGE};
    private static final int[] SLOTS_BOTTOM = new int[]{SLOT_DISCHARGE, SLOT_CHARGE};
    private static final int[] SLOTS_SIDE = new int[]{SLOT_CHARGE};

    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return (slot == SLOT_DISCHARGE || slot == SLOT_CHARGE) && HbmBatteryTransfer.isHbmBattery(stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> topItemHandler = LazyOptional.of(() -> new BatterySidedItemHandler(items, SLOTS_TOP));
    private final LazyOptional<IItemHandler> bottomItemHandler = LazyOptional.of(() -> new BatterySidedItemHandler(items, SLOTS_BOTTOM));
    private final LazyOptional<IItemHandler> sideItemHandler = LazyOptional.of(() -> new BatterySidedItemHandler(items, SLOTS_SIDE));
    private final BatteryEnergyStorage batteryEnergy;
    private final long[] powerLog = new long[20];
    private long delta;
    private long previousPowerState;
    private short redLow = MODE_INPUT;
    private short redHigh = MODE_OUTPUT;
    private boolean lastRedstone;
    private int lastMode = MODE_NONE;

    public MachineBatteryBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new BatteryEnergyStorage(MAX_POWER, MAX_RECEIVE, MAX_EXTRACT));
    }

    private MachineBatteryBlockEntity(BlockPos pos, BlockState state, BatteryEnergyStorage energy) {
        super(ModBlockEntities.MACHINE_BATTERY.get(), pos, state, energy);
        this.batteryEnergy = energy;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineBatteryBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        long previousPower = blockEntity.energy.getPower();
        boolean previousRedstone = blockEntity.lastRedstone;
        blockEntity.lastRedstone = level.hasNeighborSignal(pos);
        int currentMode = blockEntity.getCurrentMode();
        blockEntity.handleModeTransition(currentMode);
        boolean inventoryChanged = false;

        inventoryChanged |= HbmBatteryTransfer.chargeItemFromStorage(
                blockEntity.items.getStackInSlot(SLOT_CHARGE),
                blockEntity.energy,
                blockEntity.energy.getMaxPower()) > 0L;

        switch (currentMode) {
            case MODE_INPUT -> {
                blockEntity.subscribeEnergyReceiverToAllSides();
            }
            case MODE_BUFFER -> {
                blockEntity.refreshEnergyNodeState();
                blockEntity.refreshEnergyNetworkSubscriptions();
            }
            case MODE_OUTPUT -> HbmEnergyUtil.tryProvideToAllNeighbors(level, pos, blockEntity.energy);
            default -> {
            }
        }

        inventoryChanged |= HbmBatteryTransfer.chargeStorageFromItem(
                blockEntity.items.getStackInSlot(SLOT_DISCHARGE),
                blockEntity.energy,
                blockEntity.energy.getMaxPower()) > 0L;

        blockEntity.updatePowerLog(previousPower);

        if (inventoryChanged || previousPower != blockEntity.energy.getPower() || previousRedstone != blockEntity.lastRedstone) {
            blockEntity.setChanged();
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
            if (level.getGameTime() % 15L == 0L) {
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            }
        }
    }

    public int getComparatorPower() {
        long power = energy.getPower();
        if (power <= 0L || energy.getMaxPower() <= 0L) {
            return 0;
        }
        return Mth.clamp((int) (power * 15L / energy.getMaxPower()) + 1, 0, 15);
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return switch (getCurrentMode()) {
            case MODE_INPUT -> HbmEnergySideMode.INPUT;
            case MODE_BUFFER -> HbmEnergySideMode.BOTH;
            case MODE_OUTPUT -> HbmEnergySideMode.OUTPUT;
            default -> HbmEnergySideMode.NONE;
        };
    }

    @Override
    protected void refreshEnergyNetworkSubscriptions() {
        if (getCurrentMode() == MODE_BUFFER) {
            super.refreshEnergyNetworkSubscriptions();
        }
    }

    @Override
    protected boolean shouldCreateEnergyNode() {
        return level != null && getCurrentMode() == MODE_BUFFER;
    }

    @Override
    protected boolean shouldSubscribeAsProvider() {
        return getCurrentMode() == MODE_BUFFER;
    }

    @Override
    protected boolean shouldSubscribeAsReceiver() {
        return getCurrentMode() == MODE_BUFFER;
    }

    private int getCurrentMode() {
        boolean powered = level != null && level.hasNeighborSignal(worldPosition);
        return clampMode(powered ? redHigh : redLow);
    }

    private void handleModeTransition(int currentMode) {
        if (currentMode == lastMode) {
            return;
        }
        unsubscribeEnergyProviderFromAllSides();
        unsubscribeEnergyReceiverFromAllSides();
        if (currentMode != MODE_BUFFER) {
            removeEnergyNode();
        }
        lastMode = currentMode;
    }

    private static short clampMode(short mode) {
        return mode >= MODE_INPUT && mode <= MODE_NONE ? mode : MODE_INPUT;
    }

    private static short cycleMode(short mode) {
        short next = (short) (clampMode(mode) + 1);
        return next > MODE_NONE ? MODE_INPUT : next;
    }

    private static HbmEnergyReceiver.ConnectionPriority readLegacyPriority(CompoundTag tag) {
        if (tag.getTagType(TAG_PRIORITY) == Tag.TAG_STRING) {
            try {
                return sanitizeBatteryPriority(HbmEnergyReceiver.ConnectionPriority.valueOf(tag.getString(TAG_PRIORITY)));
            } catch (IllegalArgumentException ignored) {
                return HbmEnergyReceiver.ConnectionPriority.LOW;
            }
        }
        HbmEnergyReceiver.ConnectionPriority[] values = HbmEnergyReceiver.ConnectionPriority.values();
        int ordinal = tag.getInt(TAG_PRIORITY);
        return ordinal >= 0 && ordinal < values.length ? sanitizeBatteryPriority(values[ordinal]) : HbmEnergyReceiver.ConnectionPriority.LOW;
    }

    private static HbmEnergyReceiver.ConnectionPriority sanitizeBatteryPriority(HbmEnergyReceiver.ConnectionPriority priority) {
        if (priority == null
                || priority == HbmEnergyReceiver.ConnectionPriority.LOWEST
                || priority == HbmEnergyReceiver.ConnectionPriority.HIGHEST) {
            return HbmEnergyReceiver.ConnectionPriority.LOW;
        }
        return priority;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        return drops;
    }

    public long getDelta() {
        return delta;
    }

    public long getPower() {
        return energy.getPower();
    }

    public long getMaxPower() {
        return energy.getMaxPower();
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, LegacyLookOverlayLines.energyStorage(getPower(), getMaxPower()));
    }

    public short getRedLow() {
        return redLow;
    }

    public short getRedHigh() {
        return redHigh;
    }

    public HbmEnergyReceiver.ConnectionPriority getBatteryPriority() {
        return batteryEnergy.getPriority();
    }

    public void cycleRedLowMode() {
        redLow = cycleMode(redLow);
        setChanged();
    }

    public void cycleRedHighMode() {
        redHigh = cycleMode(redHigh);
        setChanged();
    }

    public void cyclePriority() {
        batteryEnergy.setPriority(switch (batteryEnergy.getPriority()) {
            case LOW -> HbmEnergyReceiver.ConnectionPriority.NORMAL;
            case NORMAL -> HbmEnergyReceiver.ConnectionPriority.HIGH;
            default -> HbmEnergyReceiver.ConnectionPriority.LOW;
        });
        setChanged();
    }

    private void setPriorityByLegacyOrdinal(int ordinal) {
        HbmEnergyReceiver.ConnectionPriority[] values = HbmEnergyReceiver.ConnectionPriority.values();
        batteryEnergy.setPriority(ordinal >= 0 && ordinal < values.length ? values[ordinal] : HbmEnergyReceiver.ConnectionPriority.LOW);
    }

    public int getPowerBarHeight(int maxHeight) {
        return energy.getMaxPower() <= 0L ? 0 : (int) (energy.getPower() * maxHeight / energy.getMaxPower());
    }

    public long getDeltaPerSecond() {
        return delta;
    }

    public Object[] getEnergyInfo() {
        return new Object[]{getPower(), getMaxPower()};
    }

    public Object[] getInfo() {
        return new Object[]{getPower(), getMaxPower()};
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[]{
                RORInfo.PREFIX_VALUE + "fill",
                RORInfo.PREFIX_VALUE + "fillpercent",
                RORInfo.PREFIX_VALUE + "delta",
                RORInfo.PREFIX_FUNCTION + "setmode" + RORInteractive.NAME_SEPARATOR + "mode (0-3)",
                RORInfo.PREFIX_FUNCTION + "setmode" + RORInteractive.NAME_SEPARATOR + "mode" + RORInteractive.PARAM_SEPARATOR + "fallback (0-3)",
                RORInfo.PREFIX_FUNCTION + "setredmode" + RORInteractive.NAME_SEPARATOR + "mode (0-3)",
                RORInfo.PREFIX_FUNCTION + "setredmode" + RORInteractive.NAME_SEPARATOR + "mode" + RORInteractive.PARAM_SEPARATOR + "fallback (0-3)",
                RORInfo.PREFIX_FUNCTION + "setpriority" + RORInteractive.NAME_SEPARATOR + "priority (0-2)"
        };
    }

    @Override
    public String provideRORValue(String name) {
        if ((RORInfo.PREFIX_VALUE + "fill").equals(name)) {
            return Long.toString(getPower());
        }
        if ((RORInfo.PREFIX_VALUE + "fillpercent").equals(name)) {
            return Long.toString(getPowerBarHeight(100));
        }
        if ((RORInfo.PREFIX_VALUE + "delta").equals(name)) {
            return Long.toString(delta);
        }
        return null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        if ((RORInfo.PREFIX_FUNCTION + "setmode").equals(name) && params.length > 0) {
            short mode = (short) RORInteractive.parseInt(params[0], MODE_INPUT, MODE_NONE);
            if (mode != redLow) {
                redLow = mode;
            } else if (params.length > 1) {
                redLow = (short) RORInteractive.parseInt(params[1], MODE_INPUT, MODE_NONE);
            }
            setChanged();
            return null;
        }
        if ((RORInfo.PREFIX_FUNCTION + "setredmode").equals(name) && params.length > 0) {
            short mode = (short) RORInteractive.parseInt(params[0], MODE_INPUT, MODE_NONE);
            if (mode != redHigh) {
                redHigh = mode;
            } else if (params.length > 1) {
                redHigh = (short) RORInteractive.parseInt(params[1], MODE_INPUT, MODE_NONE);
            }
            setChanged();
            return null;
        }
        if ((RORInfo.PREFIX_FUNCTION + "setpriority").equals(name) && params.length > 0) {
            setPriorityByLegacyOrdinal(RORInteractive.parseInt(params[0], 0, 2) + 1);
            setChanged();
            return null;
        }
        return null;
    }

    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        switch (tag.getInt(TAG_CONTROL)) {
            case CONTROL_RED_LOW -> cycleRedLowMode();
            case CONTROL_RED_HIGH -> cycleRedHighMode();
            case CONTROL_PRIORITY -> cyclePriority();
            default -> {
            }
        }
    }

    public static CompoundTag controlTag(int control) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_CONTROL, control);
        return tag;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm_ntm_rebirth.battery");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MachineBatteryMenu(containerId, inventory, this);
    }

    private void updatePowerLog(long previousPower) {
        long currentPower = energy.getPower();
        long average = (currentPower + previousPower) / 2L;
        delta = average - powerLog[0];
        System.arraycopy(powerLog, 1, powerLog, 0, powerLog.length - 1);
        powerLog[powerLog.length - 1] = average;
        previousPowerState = currentPower;
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        super.provideExtraInfo(data);
        data.putLong(CompatEnergyControl.L_DIFF_HE, (powerLog[0] - powerLog[powerLog.length - 1]) / 20L);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_INVENTORY, items.serializeNBT());
        tag.putShort(TAG_RED_LOW, redLow);
        tag.putShort(TAG_RED_HIGH, redHigh);
        tag.putBoolean(TAG_LAST_REDSTONE, lastRedstone);
        tag.putLong("Delta", delta);
        tag.putLong("prevPowerState", previousPowerState);
        tag.putLongArray("PowerLog", powerLog);
        tag.putInt("lastMode", lastMode);
        tag.putString(TAG_PRIORITY, batteryEnergy.getPriority().name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound(TAG_INVENTORY));
        if (tag.contains("power")) {
            energy.setPower(tag.getLong("power"));
        }
        redLow = clampMode(tag.getShort(TAG_RED_LOW));
        redHigh = clampMode(tag.getShort(TAG_RED_HIGH));
        lastRedstone = tag.getBoolean(TAG_LAST_REDSTONE);
        delta = tag.getLong("Delta");
        previousPowerState = tag.getLong("prevPowerState");
        if (tag.contains("PowerLog")) {
            long[] storedLog = tag.getLongArray("PowerLog");
            Arrays.fill(powerLog, 0L);
            System.arraycopy(storedLog, 0, powerLog, 0, Math.min(storedLog.length, powerLog.length));
        }
        if (tag.contains("lastMode")) {
            lastMode = clampMode((short) tag.getInt("lastMode"));
        }
        if (tag.contains(TAG_PRIORITY)) {
            batteryEnergy.setPriority(readLegacyPriority(tag));
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        topItemHandler.invalidate();
        bottomItemHandler.invalidate();
        sideItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return getItemHandler(side).cast();
        }
        return super.getCapability(capability, side);
    }

    private LazyOptional<IItemHandler> getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return itemHandler;
        }
        if (side == Direction.UP) {
            return topItemHandler;
        }
        if (side == Direction.DOWN) {
            return bottomItemHandler;
        }
        return sideItemHandler;
    }

    private static final class BatterySidedItemHandler implements IItemHandler {
        private final ItemStackHandler items;
        private final int[] slots;

        private BatterySidedItemHandler(ItemStackHandler items, int[] slots) {
            this.items = items;
            this.slots = slots;
        }

        @Override
        public int getSlots() {
            return slots.length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return items.getStackInSlot(mapSlot(slot));
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mappedSlot = mapSlot(slot);
            if (!items.isItemValid(mappedSlot, stack)) {
                return stack;
            }
            return items.insertItem(mappedSlot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mappedSlot = mapSlot(slot);
            ItemStack stack = items.getStackInSlot(mappedSlot);
            if (!canExtract(mappedSlot, stack)) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(mappedSlot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.getSlotLimit(mapSlot(slot));
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return items.isItemValid(mapSlot(slot), stack);
        }

        private int mapSlot(int slot) {
            if (slot < 0 || slot >= slots.length) {
                throw new RuntimeException("Slot " + slot + " not in valid range - [0," + slots.length + ")");
            }
            return slots[slot];
        }

        private static boolean canExtract(int slot, ItemStack stack) {
            return switch (slot) {
                case SLOT_DISCHARGE -> HbmBatteryTransfer.isEmptyBattery(stack);
                case SLOT_CHARGE -> HbmBatteryTransfer.isFullBattery(stack);
                default -> false;
            };
        }
    }

    private static final class BatteryEnergyStorage extends HbmEnergyStorage {
        private HbmEnergyReceiver.ConnectionPriority priority = HbmEnergyReceiver.ConnectionPriority.LOW;

        private BatteryEnergyStorage(long maxPower, long maxReceive, long maxExtract) {
            super(maxPower, maxReceive, maxExtract);
        }

        @Override
        public HbmEnergyReceiver.ConnectionPriority getPriority() {
            return priority;
        }

        private void setPriority(HbmEnergyReceiver.ConnectionPriority priority) {
            this.priority = sanitizeBatteryPriority(priority);
        }
    }
}
