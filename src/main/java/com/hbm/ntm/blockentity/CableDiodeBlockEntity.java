package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.tile.InfoProviderEC;
import com.hbm.ntm.block.CableDiodeBlock;
import com.hbm.ntm.energy.HbmEnergyConnector;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmLoadedEnergy;
import com.hbm.ntm.energy.HbmPowerNet;
import com.hbm.ntm.menu.CableDiodeMenu;
import com.hbm.ntm.network.HbmTileSyncable;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CableDiodeBlockEntity extends BlockEntity
        implements HbmEnergyReceiver, HbmEnergyConnector, HbmLoadedEnergy, InfoProviderEC, LegacyLookOverlayProvider,
        MenuProvider, HbmTileSyncable {
    public static final String CONTROL_LIMIT = "limit";
    public static final String CONTROL_PRIORITY = "priority";
    public static final long MAX_THROUGHPUT = 10_000_000_000L;
    private static final String TAG_LEVEL = "level";
    private static final String TAG_PRIORITY = "p";
    private static final int MAX_PULSES = 10;

    private long throughputLimit = 1_000L;
    private HbmEnergyReceiver.ConnectionPriority priority = HbmEnergyReceiver.ConnectionPriority.NORMAL;
    private long power;
    private boolean recursionBrake;
    private int pulses;
    private Direction lastSubscribedOutput;

    public CableDiodeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CABLE_DIODE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CableDiodeBlockEntity diode) {
        if (level.isClientSide) {
            return;
        }
        Direction output = diode.getOutputDirection();
        // TileEntityDiode retries every non-output input on every server tick.
        // Do not let a modern signature/keepalive delay recovery after a cable
        // is replaced at the same position.
        for (Direction side : Direction.values()) {
            if (side != output) {
                HbmEnergyUtil.subscribeReceiverToNeighborNetwork(level, pos, side, diode);
            }
        }
        diode.lastSubscribedOutput = output;
        diode.pulses = 0;
        diode.setPower(0L);
    }

    public long getThroughputLimit() {
        return throughputLimit;
    }

    public void setThroughputLimit(long limit) {
        long clamped = clampThroughput(limit);
        if (throughputLimit != clamped) {
            throughputLimit = clamped;
            syncChanged();
        }
    }

    public HbmEnergyReceiver.ConnectionPriority getConfiguredPriority() {
        return priority;
    }

    public void setConfiguredPriority(HbmEnergyReceiver.ConnectionPriority priority) {
        if (priority != null && this.priority != priority) {
            this.priority = priority;
            syncChanged();
        }
    }

    public void applyConfiguration(long limit, int priorityOrdinal) {
        long clampedLimit = clampThroughput(limit);
        HbmEnergyReceiver.ConnectionPriority[] values = HbmEnergyReceiver.ConnectionPriority.values();
        HbmEnergyReceiver.ConnectionPriority clampedPriority = priorityOrdinal >= 0 && priorityOrdinal < values.length
                ? values[priorityOrdinal]
                : HbmEnergyReceiver.ConnectionPriority.NORMAL;
        if (throughputLimit != clampedLimit || priority != clampedPriority) {
            throughputLimit = clampedLimit;
            priority = clampedPriority;
            syncChanged();
        }
    }

    public Direction getOutputDirection() {
        BlockState state = getBlockState();
        Direction legacyFacing = state.hasProperty(CableDiodeBlock.FACING)
                ? state.getValue(CableDiodeBlock.FACING)
                : Direction.NORTH;
        return legacyFacing.getOpposite();
    }

    @Override
    public boolean canConnectEnergy(@Nullable Direction side) {
        return side != null && side != getOutputDirection();
    }

    @Override
    public long transferPower(long power) {
        if (power <= 0L) {
            return 0L;
        }
        if (recursionBrake) {
            return power;
        }
        pulses++;
        if (getPower() >= getMaxPower() || pulses > MAX_PULSES) {
            return power;
        }

        recursionBrake = true;
        try {
            Direction output = getOutputDirection();
            BlockPos outputPos = worldPosition.relative(output);
            Direction outputSide = output.getOpposite();

            HbmPowerNet outputNet = HbmEnergyUtil.getConnectablePowerNet(level, outputPos, outputSide);
            if (outputNet != null) {
                long toTransfer = Math.min(power, getReceiverSpeed());
                long remainder = outputNet.sendPowerDiode(toTransfer);
                long transferred = toTransfer - remainder;
                if (transferred > 0L) {
                    this.power += transferred;
                }
                return power - transferred;
            }

            if (level != null && level.getBlockEntity(outputPos) instanceof HbmEnergyReceiver receiver
                    && level.getBlockEntity(outputPos) instanceof HbmEnergyConnector connector
                    && receiver != this
                    && connector.canConnectEnergy(outputSide)) {
                long toTransfer = Math.min(power, receiver.getReceiverSpeed());
                long remainder = receiver.transferPower(toTransfer);
                long transferred = toTransfer - remainder;
                if (transferred > 0L) {
                    this.power += transferred;
                }
                return power - transferred;
            }

            return power;
        } finally {
            recursionBrake = false;
        }
    }

    @Override
    public long getReceiverSpeed() {
        return Math.max(0L, getMaxPower() - getPower());
    }

    @Override
    public long getMaxPower() {
        return throughputLimit;
    }

    @Override
    public long getPower() {
        return Math.min(Math.max(0L, power), getMaxPower());
    }

    @Override
    public void setPower(long power) {
        this.power = Math.max(0L, Math.min(power, getMaxPower()));
    }

    @Override
    public HbmEnergyReceiver.ConnectionPriority getPriority() {
        return priority;
    }

    @Override
    public boolean isEnergyLoaded() {
        return level != null && !isRemoved();
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.maxRate(getMaxPower(), "HE/t"),
                LegacyLookOverlayLines.plainPriority(priority)));
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        data.putLong(CONTROL_LIMIT, throughputLimit);
        data.putString("priority", priority.name());
        data.putLong("maxRate", getMaxPower());
        data.putLong("transferredThisTick", getPower());
        data.putString("output", getOutputDirection().getName());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(CONTROL_LIMIT, throughputLimit);
        tag.putByte(TAG_PRIORITY, (byte) priority.ordinal());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(CONTROL_LIMIT)) {
            throughputLimit = clampThroughput(tag.getLong(CONTROL_LIMIT));
        } else if (tag.contains(TAG_LEVEL)) {
            throughputLimit = legacyLevelLimit(tag.getInt(TAG_LEVEL));
        } else {
            throughputLimit = 1_000L;
        }
        if (tag.contains(TAG_PRIORITY)) {
            HbmEnergyReceiver.ConnectionPriority[] values = HbmEnergyReceiver.ConnectionPriority.values();
            int ordinal = tag.getByte(TAG_PRIORITY);
            priority = ordinal >= 0 && ordinal < values.length
                    ? values[ordinal]
                    : HbmEnergyReceiver.ConnectionPriority.NORMAL;
        }
        setPower(power);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CableDiodeMenu(containerId, inventory, this);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(CONTROL_LIMIT, throughputLimit);
        tag.putByte(TAG_PRIORITY, (byte) priority.ordinal());
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return tag != null && tag.contains(CONTROL_LIMIT) && tag.contains(CONTROL_PRIORITY)
                && player.containerMenu instanceof CableDiodeMenu menu
                && menu.getBlockEntity() == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        applyConfiguration(tag.getLong(CONTROL_LIMIT), tag.getInt(CONTROL_PRIORITY));
    }

    private static long legacyLevelLimit(int level) {
        int boundedLevel = Mth.clamp(level, 0, 10);
        long result = 1L;
        for (int index = 0; index < boundedLevel; index++) {
            result *= 10L;
        }
        return result;
    }

    private static long clampThroughput(long limit) {
        return Math.max(0L, Math.min(limit, MAX_THROUGHPUT));
    }

    private void syncChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
