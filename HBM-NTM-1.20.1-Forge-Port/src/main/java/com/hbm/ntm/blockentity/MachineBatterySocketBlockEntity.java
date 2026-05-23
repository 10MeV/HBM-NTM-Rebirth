package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.block.MachineBatterySocketBlock;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.energy.HbmBatteryTransfer;
import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.energy.HbmEnergyDischargeEffects;
import com.hbm.ntm.energy.HbmEnergyProvider;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmNetworkNode;
import com.hbm.ntm.energy.HbmSelfChargingBatteryItem;
import com.hbm.ntm.menu.MachineBatterySocketMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

public class MachineBatterySocketBlockEntity extends HbmEnergyNetworkBlockEntity implements MenuProvider, RORValueProvider, RORInteractive {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_RED_LOW = "redLow";
    private static final String TAG_RED_HIGH = "redHigh";
    private static final String TAG_LAST_REDSTONE = "lastRedstone";
    private static final String TAG_PRIORITY = "priority";
    private static final String TAG_DELTA = "Delta";
    private static final String TAG_POWER_LOG = "PowerLog";
    private static final String TAG_FRAME = "Frame";
    private static final String TAG_CONTROL = "control";
    private static final String TAG_DAMAGE_TIMER = "damageTimer";
    private static final String TAG_DAMAGE_TARGET = "damageTarget";
    private static final String TAG_SC_POWER_MULT = "scPowerMult";

    public static final int MODE_INPUT = 0;
    public static final int MODE_BUFFER = 1;
    public static final int MODE_OUTPUT = 2;
    public static final int MODE_NONE = 3;
    public static final int SLOT_BATTERY = 0;
    public static final int CONTROL_RED_LOW = 0;
    public static final int CONTROL_RED_HIGH = 1;
    public static final int CONTROL_PRIORITY = 2;

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == SLOT_BATTERY && HbmBatteryTransfer.isHbmBattery(stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack stack = getStackInSlot(slot);
            if (!HbmBatteryTransfer.isFullBattery(stack)) {
                return ItemStack.EMPTY;
            }
            return super.extractItem(slot, amount, simulate);
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final SocketEnergyStorage socketEnergy;
    private final long[] powerLog = new long[20];
    private long delta;
    private short redLow = MODE_INPUT;
    private short redHigh = MODE_OUTPUT;
    private boolean lastRedstone;
    private boolean frame;
    private int lastMode = MODE_NONE;
    private int damageTimer;
    private int damageTarget;
    private double scPowerMult = 1.0D;

    public MachineBatterySocketBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new SocketEnergyStorage());
    }

    private MachineBatterySocketBlockEntity(BlockPos pos, BlockState state, SocketEnergyStorage energy) {
        super(ModBlockEntities.MACHINE_BATTERY_SOCKET.get(), pos, state, energy);
        this.socketEnergy = energy;
        this.socketEnergy.bind(this::getBatteryStack);
        this.socketEnergy.bindSelfChargingMultiplier(() -> scPowerMult);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineBatterySocketBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        long previousPower = blockEntity.getPower();
        boolean previousRedstone = blockEntity.lastRedstone;
        int currentMode = blockEntity.getCurrentMode();
        blockEntity.handleModeTransition(currentMode);
        blockEntity.lastRedstone = blockEntity.hasPortRedstoneSignal();

        HbmEnergyNetworkBlockEntity.serverTick(level, pos, state, blockEntity);
        blockEntity.tickSelfChargingBattery();
        blockEntity.updatePowerLog(previousPower);

        if (previousPower != blockEntity.getPower() || previousRedstone != blockEntity.lastRedstone) {
            blockEntity.setChanged();
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, MachineBatterySocketBlockEntity blockEntity) {
        if (level.getGameTime() % 20L == 0L) {
            blockEntity.frame = !level.getBlockState(pos.above(2)).isAir();
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ItemStack getBatteryStack() {
        return items.getStackInSlot(SLOT_BATTERY);
    }

    public boolean hasLoadedSelfChargingBattery() {
        return getBatteryStack().getItem() instanceof HbmSelfChargingBatteryItem battery && battery.isLoaded();
    }

    public ItemStack removeBatteryForDrop() {
        return items.extractItem(SLOT_BATTERY, items.getSlotLimit(SLOT_BATTERY), false);
    }

    public boolean hasFrame() {
        return frame;
    }

    public long getPower() {
        return socketEnergy.getPower();
    }

    public long getMaxPower() {
        return socketEnergy.getMaxPower();
    }

    public long getDeltaPerSecond() {
        return delta;
    }

    public Object[] getEnergyInfo() {
        return new Object[]{getPower(), getMaxPower(), delta};
    }

    public Object[] getPackInfo() {
        ItemStack stack = getBatteryStack();
        if (!(stack.getItem() instanceof HbmBatteryItem battery)) {
            return new Object[]{"", 0L, 0L};
        }
        return new Object[]{stack.getDescriptionId(), battery.getChargeRate(stack), battery.getDischargeRate(stack)};
    }

    public Object[] getModeInfo() {
        return new Object[]{redLow, redHigh, getBatteryPriority().ordinal() - 1};
    }

    public Object[] getInfo() {
        Object[] energyInfo = getEnergyInfo();
        Object[] modeInfo = getModeInfo();
        Object[] packInfo = getPackInfo();
        return new Object[]{
                energyInfo[0],
                energyInfo[1],
                energyInfo[2],
                modeInfo[0],
                modeInfo[1],
                modeInfo[2],
                packInfo[0],
                packInfo[1],
                packInfo[2]
        };
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
            return Long.toString(getPower() * 100L / Math.max(getMaxPower(), 1L));
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
            setChangedAndSync();
            return null;
        }
        if ((RORInfo.PREFIX_FUNCTION + "setredmode").equals(name) && params.length > 0) {
            short mode = (short) RORInteractive.parseInt(params[0], MODE_INPUT, MODE_NONE);
            if (mode != redHigh) {
                redHigh = mode;
            } else if (params.length > 1) {
                redHigh = (short) RORInteractive.parseInt(params[1], MODE_INPUT, MODE_NONE);
            }
            setChangedAndSync();
            return null;
        }
        if ((RORInfo.PREFIX_FUNCTION + "setpriority").equals(name) && params.length > 0) {
            setPriorityByLegacyOrdinal(RORInteractive.parseInt(params[0], 0, 2) + 1);
            setChangedAndSync();
            return null;
        }
        return null;
    }

    public short getRedLow() {
        return redLow;
    }

    public short getRedHigh() {
        return redHigh;
    }

    public HbmEnergyReceiver.ConnectionPriority getBatteryPriority() {
        return socketEnergy.getPriority();
    }

    public int getPowerBarHeight(int maxHeight) {
        return getMaxPower() <= 0L ? 0 : (int) (getPower() * maxHeight / getMaxPower());
    }

    public int getComparatorPower() {
        long maxPower = Math.max(getMaxPower(), 1L);
        return Mth.clamp((int) Math.round((double) getPower() / (double) maxPower * 15.0D), 0, 15);
    }

    public void cycleRedLowMode() {
        redLow = cycleMode(redLow);
        setChangedAndSync();
    }

    public void cycleRedHighMode() {
        redHigh = cycleMode(redHigh);
        setChangedAndSync();
    }

    public void cyclePriority() {
        socketEnergy.setPriority(switch (socketEnergy.getPriority()) {
            case LOW -> HbmEnergyReceiver.ConnectionPriority.NORMAL;
            case NORMAL -> HbmEnergyReceiver.ConnectionPriority.HIGH;
            default -> HbmEnergyReceiver.ConnectionPriority.LOW;
        });
        setChangedAndSync();
    }

    private void setPriorityByLegacyOrdinal(int ordinal) {
        HbmEnergyReceiver.ConnectionPriority[] values = HbmEnergyReceiver.ConnectionPriority.values();
        socketEnergy.setPriority(ordinal >= 0 && ordinal < values.length ? values[ordinal] : HbmEnergyReceiver.ConnectionPriority.LOW);
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
    protected boolean shouldCreateEnergyNode() {
        return level != null;
    }

    @Override
    protected boolean shouldSubscribeAsProvider() {
        int mode = getCurrentMode();
        return mode == MODE_OUTPUT || mode == MODE_BUFFER;
    }

    @Override
    protected boolean shouldSubscribeAsReceiver() {
        int mode = getCurrentMode();
        return mode == MODE_INPUT || mode == MODE_BUFFER;
    }

    @Override
    protected HbmEnergyNode createEnergyNode() {
        Direction facing = getFacing();
        Set<BlockPos> positions = new LinkedHashSet<>();
        for (BlockPos offset : MachineBatterySocketBlock.socketOffsets(facing)) {
            positions.add(worldPosition.offset(offset));
        }
        Set<HbmNetworkNode.NodeConnection> connections = new LinkedHashSet<>();
        Direction rot = facing.getClockWise();
        addConnection(connections, facing, facing);
        addConnection(connections, facing, rot, facing);
        addConnection(connections, facing.getOpposite(), facing.getOpposite(), 2);
        addConnection(connections, facing.getOpposite(), facing.getOpposite(), rot, 2);
        addConnection(connections, rot, rot, 2);
        addConnection(connections, rot, rot, facing.getOpposite(), 2);
        addConnection(connections, rot.getOpposite(), rot.getOpposite());
        addConnection(connections, rot.getOpposite(), rot.getOpposite(), facing.getOpposite());
        return HbmEnergyNode.withConnectionPoints(positions, connections);
    }

    private void handleModeTransition(int currentMode) {
        if (currentMode == lastMode) {
            return;
        }
        if (getPowerNet() != null) {
            getPowerNet().removeProvider(socketEnergy);
            getPowerNet().removeReceiver(socketEnergy);
        }
        lastMode = currentMode;
    }

    private int getCurrentMode() {
        return clampMode(hasPortRedstoneSignal() ? redHigh : redLow);
    }

    private boolean hasPortRedstoneSignal() {
        if (level == null) {
            return false;
        }
        for (BlockPos offset : MachineBatterySocketBlock.socketOffsets(getFacing())) {
            if (level.hasNeighborSignal(worldPosition.offset(offset))) {
                return true;
            }
        }
        return false;
    }

    private Direction getFacing() {
        BlockState state = getBlockState();
        return state.hasProperty(MachineBatterySocketBlock.FACING) ? state.getValue(MachineBatterySocketBlock.FACING) : Direction.SOUTH;
    }

    private void addConnection(Set<HbmNetworkNode.NodeConnection> connections, Direction direction, Direction offset) {
        connections.add(new HbmNetworkNode.NodeConnection(worldPosition.relative(offset), direction));
    }

    private void addConnection(Set<HbmNetworkNode.NodeConnection> connections, Direction direction, Direction offsetA, Direction offsetB) {
        connections.add(new HbmNetworkNode.NodeConnection(worldPosition.relative(offsetA).relative(offsetB), direction));
    }

    private void addConnection(Set<HbmNetworkNode.NodeConnection> connections, Direction direction, Direction offset, int distance) {
        connections.add(new HbmNetworkNode.NodeConnection(worldPosition.relative(offset, distance), direction));
    }

    private void addConnection(Set<HbmNetworkNode.NodeConnection> connections, Direction direction, Direction offsetA, Direction offsetB, int distanceA) {
        connections.add(new HbmNetworkNode.NodeConnection(worldPosition.relative(offsetA, distanceA).relative(offsetB), direction));
    }

    private void updatePowerLog(long previousPower) {
        long currentPower = getPower();
        long average = (currentPower + previousPower) / 2L;
        delta = average - powerLog[0];
        System.arraycopy(powerLog, 1, powerLog, 0, powerLog.length - 1);
        powerLog[powerLog.length - 1] = average;
    }

    private void tickSelfChargingBattery() {
        if (!hasLoadedSelfChargingBattery()) {
            damageTimer = 0;
            damageTarget = 0;
            scPowerMult = 1.0D;
            return;
        }
        if (damageTarget == 0) {
            pickNewSelfChargingTarget();
        }
        damageTimer++;
        if (damageTimer >= damageTarget) {
            dischargeSelfChargingBattery();
        }
        double step = 1.0D / 100.0D;
        scPowerMult += step * (level.random.nextDouble() * 2.0D - 1.0D);
        scPowerMult = Mth.clamp(scPowerMult, 0.1D, 1.0D);
    }

    private void pickNewSelfChargingTarget() {
        damageTimer = 0;
        damageTarget = 1200 + level.random.nextInt(2400);
        setChanged();
    }

    private void dischargeSelfChargingBattery() {
        pickNewSelfChargingTarget();
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            HbmEnergyDischargeEffects.dischargeSelfChargingSocket(serverLevel, worldPosition, getFacing());
        }
    }

    public static CompoundTag controlTag(int control) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_CONTROL, control);
        return tag;
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        switch (tag.getInt(TAG_CONTROL)) {
            case CONTROL_RED_LOW -> cycleRedLowMode();
            case CONTROL_RED_HIGH -> cycleRedHighMode();
            case CONTROL_PRIORITY -> cyclePriority();
            default -> {
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.batterySocket");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MachineBatterySocketMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveSocketData(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound(TAG_INVENTORY));
        redLow = clampMode(tag.getShort(TAG_RED_LOW));
        redHigh = clampMode(tag.getShort(TAG_RED_HIGH));
        lastRedstone = tag.getBoolean(TAG_LAST_REDSTONE);
        delta = tag.getLong(TAG_DELTA);
        frame = tag.getBoolean(TAG_FRAME);
        if (tag.contains(TAG_POWER_LOG)) {
            long[] storedLog = tag.getLongArray(TAG_POWER_LOG);
            Arrays.fill(powerLog, 0L);
            System.arraycopy(storedLog, 0, powerLog, 0, Math.min(storedLog.length, powerLog.length));
        }
        damageTimer = tag.getInt(TAG_DAMAGE_TIMER);
        damageTarget = tag.getInt(TAG_DAMAGE_TARGET);
        scPowerMult = tag.contains(TAG_SC_POWER_MULT) ? tag.getDouble(TAG_SC_POWER_MULT) : 1.0D;
        if (tag.contains(TAG_PRIORITY)) {
            socketEnergy.setPriority(readLegacyPriority(tag));
        }
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        saveSocketData(tag);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        data.putString(CompatEnergyControl.KEY_EUTYPE, "HE");
        data.putLong(CompatEnergyControl.L_ENERGY_HE, getPower());
        data.putLong(CompatEnergyControl.L_CAPACITY_HE, getMaxPower());
        data.putLong(CompatEnergyControl.L_DIFF_HE, (powerLog[0] - powerLog[powerLog.length - 1]) / 20L);
    }

    private void saveSocketData(CompoundTag tag) {
        tag.put(TAG_INVENTORY, items.serializeNBT());
        tag.putShort(TAG_RED_LOW, redLow);
        tag.putShort(TAG_RED_HIGH, redHigh);
        tag.putBoolean(TAG_LAST_REDSTONE, lastRedstone);
        tag.putString(TAG_PRIORITY, socketEnergy.getPriority().name());
        tag.putLong(TAG_DELTA, delta);
        tag.putLongArray(TAG_POWER_LOG, powerLog);
        tag.putBoolean(TAG_FRAME, frame);
        tag.putInt(TAG_DAMAGE_TIMER, damageTimer);
        tag.putInt(TAG_DAMAGE_TARGET, damageTarget);
        tag.putDouble(TAG_SC_POWER_MULT, scPowerMult);
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
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

    private static final class SocketEnergyStorage extends HbmEnergyStorage {
        private Supplier<ItemStack> batterySupplier = () -> ItemStack.EMPTY;
        private HbmEnergyReceiver.ConnectionPriority priority = HbmEnergyReceiver.ConnectionPriority.LOW;
        private Supplier<Double> selfChargingMultiplierSupplier = () -> 1.0D;

        private SocketEnergyStorage() {
            super(0L);
        }

        private void bind(Supplier<ItemStack> batterySupplier) {
            this.batterySupplier = batterySupplier == null ? () -> ItemStack.EMPTY : batterySupplier;
        }

        private void bindSelfChargingMultiplier(Supplier<Double> selfChargingMultiplierSupplier) {
            this.selfChargingMultiplierSupplier = selfChargingMultiplierSupplier == null ? () -> 1.0D : selfChargingMultiplierSupplier;
        }

        @Override
        public long getPower() {
            ItemStack stack = batterySupplier.get();
            if (!(stack.getItem() instanceof HbmBatteryItem battery)) {
                return 0L;
            }
            long power = battery.getCharge(stack);
            if (stack.getItem() instanceof HbmSelfChargingBatteryItem sc && sc.isLoaded()) {
                power = (long) (power * selfChargingMultiplierSupplier.get());
            }
            return power;
        }

        @Override
        public void setPower(long power) {
            ItemStack stack = batterySupplier.get();
            if (stack.getItem() instanceof HbmBatteryItem battery) {
                battery.setCharge(stack, power);
            }
        }

        @Override
        public long getMaxPower() {
            ItemStack stack = batterySupplier.get();
            return stack.getItem() instanceof HbmBatteryItem battery ? battery.getMaxCharge(stack) : 0L;
        }

        @Override
        public long getReceiverSpeed() {
            ItemStack stack = batterySupplier.get();
            return stack.getItem() instanceof HbmBatteryItem battery ? battery.getChargeRate(stack) : 0L;
        }

        @Override
        public long getProviderSpeed() {
            ItemStack stack = batterySupplier.get();
            return stack.getItem() instanceof HbmBatteryItem battery ? battery.getDischargeRate(stack) : 0L;
        }

        @Override
        public long transferPower(long power) {
            long limitedPower = Math.min(power, getReceiverSpeed());
            if (limitedPower <= 0L) {
                return Math.max(0L, power);
            }
            long accepted = Math.min(limitedPower, Math.max(0L, getMaxPower() - getPower()));
            if (accepted > 0L) {
                setPower(getPower() + accepted);
            }
            return power - accepted;
        }

        @Override
        public long usePower(long power) {
            long limitedPower = Math.min(power, getProviderSpeed());
            if (limitedPower <= 0L) {
                return 0L;
            }
            long used = Math.min(limitedPower, getPower());
            if (used > 0L) {
                setPower(getPower() - used);
            }
            return used;
        }

        @Override
        public boolean allowDirectProvision() {
            return false;
        }

        @Override
        public HbmEnergyReceiver.ConnectionPriority getPriority() {
            return priority;
        }

        private void setPriority(HbmEnergyReceiver.ConnectionPriority priority) {
            this.priority = sanitizeBatteryPriority(priority);
        }

        @Override
        public CompoundTag serializeNBT() {
            return new CompoundTag();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
        }
    }
}
