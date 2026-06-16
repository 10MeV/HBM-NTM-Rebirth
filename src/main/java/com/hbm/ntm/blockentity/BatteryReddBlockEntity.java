package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmBatteryTransfer;
import com.hbm.ntm.energy.HbmEnergyHandler;
import com.hbm.ntm.energy.HbmEnergyProvider;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.menu.BatteryReddMenu;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BatteryReddBlockEntity extends HbmEnergyNetworkBlockEntity
        implements MenuProvider, HbmLegacyControlReceiver, LegacyLookOverlayProvider, HbmPersistentBlockState {
    public static final int SLOT_DISCHARGE = 0;
    public static final int SLOT_CHARGE = 1;
    public static final int SLOT_COUNT = 2;

    public static final int MODE_INPUT = 0;
    public static final int MODE_BUFFER = 1;
    public static final int MODE_OUTPUT = 2;
    public static final int MODE_NONE = 3;

    public static final int CONTROL_RED_LOW = 0;
    public static final int CONTROL_RED_HIGH = 1;
    public static final int CONTROL_PRIORITY = 2;

    private static final long NETWORK_MAX_POWER = Long.MAX_VALUE / 100L;
    private static final BigInteger NETWORK_PROVIDER_CAP =
            BigInteger.valueOf(NETWORK_MAX_POWER / 2L);
    private static final String TAG_ITEMS = "items";
    private static final String TAG_POWER = "power";
    private static final String TAG_DELTA = "delta";
    private static final String TAG_RED_LOW = "redLow";
    private static final String TAG_RED_HIGH = "redHigh";
    private static final String TAG_LAST_REDSTONE = "lastRedstone";
    private static final String TAG_PRIORITY = "priority";
    private static final String TAG_CONTROL = "control";
    private static final String TAG_MUFFLED = "muffled";

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return HbmBatteryTransfer.isHbmBattery(stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final BigEnergy bigEnergy = new BigEnergy();
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<IEnergyStorage> forgeEnergy = LazyOptional.of(() -> new ForgeEnergyAdapter(bigEnergy));
    private final LazyOptional<IEnergyStorage> forgeEnergyInput =
            LazyOptional.of(() -> new ForgeEnergyAdapter(bigEnergy, true, false));
    private final LazyOptional<IEnergyStorage> forgeEnergyOutput =
            LazyOptional.of(() -> new ForgeEnergyAdapter(bigEnergy, false, true));

    private BigInteger power = BigInteger.ZERO;
    private BigInteger delta = BigInteger.ZERO;
    private final BigInteger[] log = new BigInteger[20];
    private short redLow = MODE_INPUT;
    private short redHigh = MODE_OUTPUT;
    private boolean lastRedstone;
    private HbmEnergyReceiver.ConnectionPriority priority = HbmEnergyReceiver.ConnectionPriority.LOW;
    private boolean muffled;
    private float previousRotation;
    private float rotation;
    private Object audioLoop;

    public BatteryReddBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY_REDD.get(), pos, state, new HbmEnergyStorage(0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BatteryReddBlockEntity battery) {
        if (level.isClientSide) {
            return;
        }
        BigInteger previousPower = battery.power;
        boolean previousRedstone = battery.lastRedstone;
        short oldLow = battery.redLow;
        short oldHigh = battery.redHigh;
        HbmEnergyReceiver.ConnectionPriority oldPriority = battery.priority;

        battery.lastRedstone = battery.hasLegacyPortRedstoneSignal();
        HbmEnergyNetworkBlockEntity.serverTick(level, pos, state, battery);

        boolean inventoryChanged = false;
        inventoryChanged |= battery.chargeItemFromStorage();
        inventoryChanged |= battery.chargeStorageFromItem();

        int mode = battery.getCurrentMode();
        if (mode == MODE_OUTPUT) {
            HbmEnergyUtil.tryProvideToPorts(level, pos, battery.getEnergyPorts(), battery.bigEnergy);
        }

        battery.updatePowerLog(previousPower);

        boolean changed = inventoryChanged
                || !previousPower.equals(battery.power)
                || !battery.delta.equals(BigInteger.ZERO)
                || previousRedstone != battery.lastRedstone
                || oldLow != battery.redLow
                || oldHigh != battery.redHigh
                || oldPriority != battery.priority;
        battery.networkPackNT(100);
        if (changed || level.getGameTime() % 20L == 0L) {
            battery.setChanged();
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BatteryReddBlockEntity battery) {
        battery.previousRotation = battery.rotation;
        battery.rotation += battery.getSpeed();
        if (battery.rotation >= 360.0F) {
            battery.rotation -= 360.0F;
            battery.previousRotation -= 360.0F;
        }
        float pitch = 0.5F + battery.getSpeed() / 15.0F * 1.5F;
        battery.audioLoop = LegacyMachineAudioBridge.updateLoop(battery.audioLoop, battery,
                "hbm:block.fensuHum", !battery.muffled && battery.previousRotation != battery.rotation,
                25.0D, 25.0F, 1.5F, pitch);
    }

    private boolean chargeItemFromStorage() {
        long before = networkPower();
        long after = HbmBatteryTransfer.chargeItemsFromPower(items.getStackInSlot(SLOT_CHARGE),
                before, NETWORK_MAX_POWER);
        long removed = Math.max(0L, before - after);
        if (removed > 0L) {
            subtractPower(BigInteger.valueOf(removed));
        }
        return removed > 0L;
    }

    private boolean chargeStorageFromItem() {
        long before = networkPower();
        long after = HbmBatteryTransfer.chargePowerFromItem(items.getStackInSlot(SLOT_DISCHARGE),
                before, NETWORK_MAX_POWER);
        long added = Math.max(0L, after - before);
        if (added > 0L) {
            addPower(BigInteger.valueOf(added));
        }
        return added > 0L;
    }

    private void updatePowerLog(BigInteger previousPower) {
        BigInteger average = power.add(previousPower).divide(BigInteger.valueOf(2L));
        delta = average.subtract(log[0] == null ? BigInteger.ZERO : log[0]);
        System.arraycopy(log, 1, log, 0, log.length - 1);
        log[log.length - 1] = average;
    }

    private boolean hasLegacyPortRedstoneSignal() {
        if (level == null) {
            return false;
        }
        for (BlockPos offset : portOffsets()) {
            if (level.hasNeighborSignal(worldPosition.offset(offset))) {
                return true;
            }
        }
        return level.hasNeighborSignal(worldPosition);
    }

    public int getComparatorPower() {
        long exposed = networkPower();
        if (exposed <= 0L) {
            return 0;
        }
        return Mth.clamp((int) Math.round((double) exposed / (double) NETWORK_MAX_POWER * 15.0D), 0, 15);
    }

    public float getInterpolatedRotation(float partialTick) {
        return previousRotation + (rotation - previousRotation) * partialTick;
    }

    public float getSpeed() {
        return (float) Math.min(Math.pow(Math.log(power.doubleValue() * 0.05D + 1.0D) * 0.05D, 5.0D), 15.0D);
    }

    public BigInteger getBigPower() {
        return power;
    }

    public BigInteger getDelta() {
        return delta;
    }

    @Override
    public long getPower() {
        return networkPower();
    }

    @Override
    public void setPower(long power) {
        this.power = BigInteger.valueOf(Math.max(0L, power));
    }

    @Override
    public long getMaxPower() {
        return NETWORK_MAX_POWER;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public short getRedLow() {
        return redLow;
    }

    public short getRedHigh() {
        return redHigh;
    }

    public HbmEnergyReceiver.ConnectionPriority getBatteryPriority() {
        return priority;
    }

    public int getCurrentMode() {
        return clampMode(lastRedstone ? redHigh : redLow);
    }

    private long networkPower() {
        return power.min(NETWORK_PROVIDER_CAP).max(BigInteger.ZERO).longValue();
    }

    private void addPower(BigInteger amount) {
        if (amount.signum() > 0) {
            power = power.add(amount);
        }
    }

    private BigInteger subtractPower(BigInteger amount) {
        if (amount.signum() <= 0 || power.signum() <= 0) {
            return BigInteger.ZERO;
        }
        BigInteger used = power.min(amount);
        power = power.subtract(used);
        return used;
    }

    @Override
    protected boolean shouldUseRemotePortEnergyNode() {
        return true;
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
    protected HbmEnergyProvider getNetworkEnergyProvider() {
        return bigEnergy;
    }

    @Override
    protected HbmEnergyReceiver getNetworkEnergyReceiver() {
        return bigEnergy;
    }

    @Override
    protected Iterable<HbmEnergyUtil.EnergyPort> getEnergyPorts() {
        Direction facing = facing();
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                port(facing, side, 3, 2, facing),
                port(facing, side, 3, -2, facing),
                port(facing, side, -3, 2, facing.getOpposite()),
                port(facing, side, -3, -2, facing.getOpposite()),
                port(facing, side, 0, 5, side),
                port(facing, side, 0, -5, side.getOpposite()));
    }

    private List<BlockPos> portOffsets() {
        Direction facing = facing();
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, side, 2, 2, 0),
                LegacyMultiblockOffsets.relative(facing, side, 2, -2, 0),
                LegacyMultiblockOffsets.relative(facing, side, -2, 2, 0),
                LegacyMultiblockOffsets.relative(facing, side, -2, -2, 0),
                LegacyMultiblockOffsets.relative(facing, side, 0, 4, 0),
                LegacyMultiblockOffsets.relative(facing, side, 0, -4, 0));
    }

    private static HbmEnergyUtil.EnergyPort port(Direction facing, Direction side, int forward, int sideOffset,
            Direction direction) {
        BlockPos offset = LegacyMultiblockOffsets.relative(facing, side, forward, sideOffset, 0);
        return new HbmEnergyUtil.EnergyPort(offset, direction);
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(LegacyVisibleMultiblockMachineBlock.FACING)
                ? state.getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                : Direction.SOUTH;
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
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                Component.literal(power + " HE").withStyle(ChatFormatting.GREEN),
                Component.literal((delta.signum() >= 0 ? "+" : "") + delta + " HE/s")
                        .withStyle(delta.signum() > 0 ? ChatFormatting.GREEN
                                : delta.signum() < 0 ? ChatFormatting.RED : ChatFormatting.YELLOW)));
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-4, 0, -4), worldPosition.offset(5, 10, 5));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.batteryREDD", "FEnSU");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new BatteryReddMenu(containerId, inventory, this);
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player != null && player.distanceToSqr(worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 256.0D;
    }

    @Override
    public void receiveControl(ServerPlayer player, CompoundTag data) {
        if (data.getInt(TAG_CONTROL) == CONTROL_RED_LOW || data.contains("low")) {
            redLow = cycleMode(redLow);
        } else if (data.getInt(TAG_CONTROL) == CONTROL_RED_HIGH || data.contains("high")) {
            redHigh = cycleMode(redHigh);
        } else if (data.getInt(TAG_CONTROL) == CONTROL_PRIORITY || data.contains("priority")) {
            priority = switch (priority) {
                case LOW -> HbmEnergyReceiver.ConnectionPriority.NORMAL;
                case NORMAL -> HbmEnergyReceiver.ConnectionPriority.HIGH;
                default -> HbmEnergyReceiver.ConnectionPriority.LOW;
            };
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public static CompoundTag controlTag(int control) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_CONTROL, control);
        return tag;
    }

    private static short cycleMode(short mode) {
        short next = (short) (clampMode(mode) + 1);
        return next > MODE_NONE ? MODE_INPUT : next;
    }

    private static short clampMode(short mode) {
        return mode >= MODE_INPUT && mode <= MODE_NONE ? mode : MODE_INPUT;
    }

    private static HbmEnergyReceiver.ConnectionPriority sanitizePriority(HbmEnergyReceiver.ConnectionPriority value) {
        if (value == null
                || value == HbmEnergyReceiver.ConnectionPriority.LOWEST
                || value == HbmEnergyReceiver.ConnectionPriority.HIGHEST) {
            return HbmEnergyReceiver.ConnectionPriority.LOW;
        }
        return value;
    }

    private static HbmEnergyReceiver.ConnectionPriority readPriority(CompoundTag tag) {
        if (tag.getTagType(TAG_PRIORITY) == Tag.TAG_STRING) {
            try {
                return sanitizePriority(HbmEnergyReceiver.ConnectionPriority.valueOf(tag.getString(TAG_PRIORITY)));
            } catch (IllegalArgumentException ignored) {
                return HbmEnergyReceiver.ConnectionPriority.LOW;
            }
        }
        HbmEnergyReceiver.ConnectionPriority[] values = HbmEnergyReceiver.ConnectionPriority.values();
        int ordinal = tag.getInt(TAG_PRIORITY);
        return ordinal >= 0 && ordinal < values.length ? sanitizePriority(values[ordinal])
                : HbmEnergyReceiver.ConnectionPriority.LOW;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putByteArray(TAG_POWER, power.toByteArray());
        tag.putByteArray(TAG_DELTA, delta.toByteArray());
        tag.putShort(TAG_RED_LOW, redLow);
        tag.putShort(TAG_RED_HIGH, redHigh);
        tag.putBoolean(TAG_LAST_REDSTONE, lastRedstone);
        tag.putByte(TAG_PRIORITY, (byte) priority.ordinal());
        tag.putBoolean(TAG_MUFFLED, muffled);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        power = readBigInteger(tag, TAG_POWER);
        delta = readBigInteger(tag, TAG_DELTA);
        redLow = clampMode(tag.getShort(TAG_RED_LOW));
        redHigh = clampMode(tag.getShort(TAG_RED_HIGH));
        lastRedstone = tag.getBoolean(TAG_LAST_REDSTONE);
        if (tag.contains(TAG_PRIORITY)) {
            priority = readPriority(tag);
        }
        muffled = tag.getBoolean(TAG_MUFFLED);
    }

    private static BigInteger readBigInteger(CompoundTag tag, String key) {
        byte[] bytes = tag.getByteArray(key);
        return bytes.length == 0 ? BigInteger.ZERO : new BigInteger(bytes);
    }

    @Override
    public void writePersistentState(CompoundTag persistent) {
        persistent.putByteArray(TAG_POWER, power.toByteArray());
        persistent.putBoolean(TAG_MUFFLED, muffled);
    }

    @Override
    public void readPersistentState(CompoundTag persistent) {
        power = readBigInteger(persistent, TAG_POWER);
        muffled = persistent.getBoolean(TAG_MUFFLED);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        forgeEnergy.invalidate();
        forgeEnergyInput.invalidate();
        forgeEnergyOutput.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        if (capability == ForgeCapabilities.ENERGY && canAccessEnergy(side)) {
            boolean receive = canReceiveEnergy(side);
            boolean extract = canExtractEnergy(side);
            if (receive && extract) {
                return forgeEnergy.cast();
            }
            if (receive) {
                return forgeEnergyInput.cast();
            }
            if (extract) {
                return forgeEnergyOutput.cast();
            }
        }
        return super.getCapability(capability, side);
    }

    private final class BigEnergy implements HbmEnergyHandler, HbmEnergyProvider, HbmEnergyReceiver {
        @Override
        public long getPower() {
            return networkPower();
        }

        @Override
        public void setPower(long value) {
            BatteryReddBlockEntity.this.setPower(value);
        }

        @Override
        public long getMaxPower() {
            return NETWORK_MAX_POWER;
        }

        @Override
        public long getProviderSpeed() {
            int mode = getCurrentMode();
            return mode == MODE_OUTPUT || mode == MODE_BUFFER ? NETWORK_MAX_POWER : 0L;
        }

        @Override
        public long getReceiverSpeed() {
            int mode = getCurrentMode();
            return mode == MODE_INPUT || mode == MODE_BUFFER ? NETWORK_MAX_POWER : 0L;
        }

        @Override
        public long usePower(long amount) {
            return subtractPower(BigInteger.valueOf(Math.max(0L, amount))).longValue();
        }

        @Override
        public long transferPower(long amount) {
            if (amount > 0L) {
                addPower(BigInteger.valueOf(amount));
            }
            return 0L;
        }

        @Override
        public boolean allowDirectProvision() {
            return false;
        }

        @Override
        public ConnectionPriority getPriority() {
            return priority;
        }
    }
}
