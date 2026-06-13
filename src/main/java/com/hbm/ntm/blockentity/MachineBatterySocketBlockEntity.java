package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.common.CopiableSettings;
import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORDispatcher;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.api.tile.ControlReceiver;
import com.hbm.ntm.block.MachineBatterySocketBlock;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.HbmBatteryTransfer;
import com.hbm.ntm.energy.HbmChargeableItem;
import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.energy.HbmEnergyDischargeEffects;
import com.hbm.ntm.energy.HbmEnergyProvider;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmSelfChargingBatteryItem;
import com.hbm.ntm.menu.MachineBatterySocketMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.world.DirPos;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class MachineBatterySocketBlockEntity extends HbmEnergyNetworkBlockEntity implements MenuProvider, RORValueProvider,
        RORInteractive, LegacyLookOverlayProvider, ControlReceiver, CopiableSettings {
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
    };
    private final RORDispatcher ror;
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> sidedItemHandler = LazyOptional.of(() -> new SocketSidedItemHandler(items));
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
        this.socketEnergy.bindModeSupplier(this::getCurrentMode);
        this.socketEnergy.bindSelfChargingMultiplier(() -> scPowerMult);
        this.ror = createRorDispatcher();
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
            blockEntity.updateComparatorOutputs();
            if (level.getGameTime() % 15L == 0L) {
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            }
        }

        blockEntity.networkPackNT(100);
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

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        ItemStack stack = getBatteryStack();
        if (stack.isEmpty()) {
            return null;
        }
        return LegacyLookOverlay.withTitle(stack.getHoverName(),
                LegacyLookOverlayLines.energyStorage(getPower(), getMaxPower()));
    }

    public long getDeltaPerSecond() {
        return delta;
    }

    public Object[] getEnergyInfo() {
        return new Object[]{getPower(), getMaxPower(), delta};
    }

    public Object[] getPackInfo() {
        ItemStack stack = getBatteryStack();
        if (!(stack.getItem() instanceof HbmChargeableItem battery)) {
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
        return ror.getFunctionInfo();
    }

    @Override
    public String provideRORValue(String name) {
        return ror.provideValue(name);
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        return ror.runFunction(name, params);
    }

    private RORDispatcher createRorDispatcher() {
        return RORDispatcher.builder()
                .value("fill", () -> Long.toString(getPower()))
                .value("fillpercent", () -> Long.toString(getPower() * 100L / Math.max(getMaxPower(), 1L)))
                .value("delta", () -> Long.toString(delta))
                .function("setmode", this::runRorSetMode,
                        "mode (0-3)",
                        "mode" + RORInteractive.PARAM_SEPARATOR + "fallback (0-3)")
                .function("setredmode", this::runRorSetRedMode,
                        "mode (0-3)",
                        "mode" + RORInteractive.PARAM_SEPARATOR + "fallback (0-3)")
                .function("setpriority", this::runRorSetPriority, "priority (0-2)")
                .build();
    }

    private String runRorSetMode(String[] params) {
        if (params.length > 0) {
            short mode = (short) RORInteractive.parseInt(params[0], MODE_INPUT, MODE_NONE);
            if (mode != redLow) {
                redLow = mode;
            } else if (params.length > 1) {
                redLow = (short) RORInteractive.parseInt(params[1], MODE_INPUT, MODE_NONE);
            }
            setChangedAndSync();
        }
        return null;
    }

    private String runRorSetRedMode(String[] params) {
        if (params.length > 0) {
            short mode = (short) RORInteractive.parseInt(params[0], MODE_INPUT, MODE_NONE);
            if (mode != redHigh) {
                redHigh = mode;
            } else if (params.length > 1) {
                redHigh = (short) RORInteractive.parseInt(params[1], MODE_INPUT, MODE_NONE);
            }
            setChangedAndSync();
        }
        return null;
    }

    private String runRorSetPriority(String[] params) {
        if (params.length > 0) {
            setPriorityByLegacyOrdinal(RORInteractive.parseInt(params[0], 0, 2) + 1);
            setChangedAndSync();
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
        long maxPower = getMaxPower();
        return maxPower <= 0L ? 0 : Mth.clamp((int) Math.floor((double) getPower() / (double) maxPower * maxHeight), 0, maxHeight);
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

    private void markSettingsChanged() {
        setChangedAndSync();
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
        List<DirPos> connections = new ArrayList<>();
        Direction rot = facing.getClockWise();
        addConnection(connections, facing, facing);
        addConnection(connections, facing, rot, facing);
        addConnection(connections, facing.getOpposite(), facing.getOpposite(), 2);
        addConnection(connections, facing.getOpposite(), facing.getOpposite(), rot, 2);
        addConnection(connections, rot, rot, 2);
        addConnection(connections, rot, rot, facing.getOpposite(), 2);
        addConnection(connections, rot.getOpposite(), rot.getOpposite());
        addConnection(connections, rot.getOpposite(), rot.getOpposite(), facing.getOpposite());
        return HbmEnergyNode.withLegacyConnectionPoints(positions, connections);
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

    private void addConnection(List<DirPos> connections, Direction direction, Direction offset) {
        connections.add(new DirPos(worldPosition.relative(offset), direction));
    }

    private void addConnection(List<DirPos> connections, Direction direction, Direction offsetA, Direction offsetB) {
        connections.add(new DirPos(worldPosition.relative(offsetA).relative(offsetB), direction));
    }

    private void addConnection(List<DirPos> connections, Direction direction, Direction offset, int distance) {
        connections.add(new DirPos(worldPosition.relative(offset, distance), direction));
    }

    private void addConnection(List<DirPos> connections, Direction direction, Direction offsetA, Direction offsetB, int distanceA) {
        connections.add(new DirPos(worldPosition.relative(offsetA, distanceA).relative(offsetB), direction));
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
            default -> receiveControl(player, tag);
        }
    }

    @Override
    public boolean hasPermission(Player player) {
        return player != null && !isRemoved();
    }

    @Override
    public void receiveControl(CompoundTag data) {
        boolean changed = false;
        if (data.contains("low")) {
            redLow = cycleMode(redLow);
            changed = true;
        }
        if (data.contains("high")) {
            redHigh = cycleMode(redHigh);
            changed = true;
        }
        if (data.contains("priority")) {
            socketEnergy.setPriority(switch (socketEnergy.getPriority()) {
                case LOW -> HbmEnergyReceiver.ConnectionPriority.NORMAL;
                case NORMAL -> HbmEnergyReceiver.ConnectionPriority.HIGH;
                default -> HbmEnergyReceiver.ConnectionPriority.LOW;
            });
            changed = true;
        }
        if (changed) {
            markSettingsChanged();
        }
    }

    @Override
    public void receiveControl(Player player, CompoundTag data) {
        if (hasPermission(player)) {
            receiveControl(data);
        }
    }

    @Override
    public CompoundTag getSettings(Level level, BlockPos pos) {
        CompoundTag data = new CompoundTag();
        data.putShort(TAG_RED_LOW, redLow);
        data.putShort(TAG_RED_HIGH, redHigh);
        data.putByte(TAG_PRIORITY, (byte) socketEnergy.getPriority().ordinal());
        return data;
    }

    @Override
    public void pasteSettings(CompoundTag tag, int index, Level level, Player player, BlockPos pos) {
        if (tag.contains(TAG_RED_LOW)) {
            redLow = clampMode(tag.getShort(TAG_RED_LOW));
        }
        if (tag.contains(TAG_RED_HIGH)) {
            redHigh = clampMode(tag.getShort(TAG_RED_HIGH));
        }
        if (tag.contains(TAG_PRIORITY)) {
            socketEnergy.setPriority(readLegacyPriority(tag));
        }
        markSettingsChanged();
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
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
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
        scPowerMult = tag.getDouble(TAG_SC_POWER_MULT);
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
        super.provideExtraInfo(data);
        data.putLong(CompatEnergyControl.L_DIFF_HE, (powerLog[0] - powerLog[powerLog.length - 1]) / 20L);
    }

    private void saveSocketData(CompoundTag tag) {
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
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
            if (!level.isClientSide) {
                updateComparatorOutputs();
            }
        }
    }

    private void updateComparatorOutputs() {
        if (level == null || level.isClientSide) {
            return;
        }
        Block block = getBlockState().getBlock();
        for (BlockPos offset : MachineBatterySocketBlock.socketOffsets(getFacing())) {
            level.updateNeighbourForOutputSignal(worldPosition.offset(offset), block);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        sidedItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return side == null ? itemHandler.cast() : sidedItemHandler.cast();
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

    private static final class SocketSidedItemHandler implements IItemHandler {
        private final ItemStackHandler items;

        private SocketSidedItemHandler(ItemStackHandler items) {
            this.items = items;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot == SLOT_BATTERY ? items.getStackInSlot(SLOT_BATTERY) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot == SLOT_BATTERY && items.isItemValid(SLOT_BATTERY, stack)
                    ? items.insertItem(SLOT_BATTERY, stack, simulate)
                    : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != SLOT_BATTERY || !HbmBatteryTransfer.isFullBattery(items.getStackInSlot(SLOT_BATTERY))) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(SLOT_BATTERY, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SLOT_BATTERY ? items.getSlotLimit(SLOT_BATTERY) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == SLOT_BATTERY && items.isItemValid(SLOT_BATTERY, stack);
        }
    }

    private static final class SocketEnergyStorage extends HbmEnergyStorage {
        private Supplier<ItemStack> batterySupplier = () -> ItemStack.EMPTY;
        private HbmEnergyReceiver.ConnectionPriority priority = HbmEnergyReceiver.ConnectionPriority.LOW;
        private IntSupplier modeSupplier = () -> MODE_NONE;
        private Supplier<Double> selfChargingMultiplierSupplier = () -> 1.0D;

        private SocketEnergyStorage() {
            super(0L);
        }

        private void bind(Supplier<ItemStack> batterySupplier) {
            this.batterySupplier = batterySupplier == null ? () -> ItemStack.EMPTY : batterySupplier;
        }

        private void bindModeSupplier(IntSupplier modeSupplier) {
            this.modeSupplier = modeSupplier == null ? () -> MODE_NONE : modeSupplier;
        }

        private void bindSelfChargingMultiplier(Supplier<Double> selfChargingMultiplierSupplier) {
            this.selfChargingMultiplierSupplier = selfChargingMultiplierSupplier == null ? () -> 1.0D : selfChargingMultiplierSupplier;
        }

        @Override
        public long getPower() {
            ItemStack stack = batterySupplier.get();
            if (!(stack.getItem() instanceof HbmChargeableItem battery)) {
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
            if (stack.getItem() instanceof HbmChargeableItem battery) {
                battery.setCharge(stack, power);
            }
        }

        @Override
        public long getMaxPower() {
            ItemStack stack = batterySupplier.get();
            return stack.getItem() instanceof HbmChargeableItem battery ? battery.getMaxCharge(stack) : 0L;
        }

        @Override
        public long getReceiverSpeed() {
            int mode = modeSupplier.getAsInt();
            if (mode != MODE_INPUT && mode != MODE_BUFFER) {
                return 0L;
            }
            ItemStack stack = batterySupplier.get();
            return stack.getItem() instanceof HbmChargeableItem battery ? battery.getChargeRate(stack) : 0L;
        }

        @Override
        public long getProviderSpeed() {
            int mode = modeSupplier.getAsInt();
            if (mode != MODE_OUTPUT && mode != MODE_BUFFER) {
                return 0L;
            }
            ItemStack stack = batterySupplier.get();
            return stack.getItem() instanceof HbmChargeableItem battery ? battery.getDischargeRate(stack) : 0L;
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
