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
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CableDiodeBlockEntity extends BlockEntity
        implements HbmEnergyReceiver, HbmEnergyConnector, HbmLoadedEnergy, InfoProviderEC, LegacyLookOverlayProvider {
    private static final String TAG_LEVEL = "level";
    private static final String TAG_PRIORITY = "p";
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 11;
    private static final int MAX_PULSES = 10;

    private int throughputLevel = MIN_LEVEL;
    private HbmEnergyReceiver.ConnectionPriority priority = HbmEnergyReceiver.ConnectionPriority.NORMAL;
    private long power;
    private boolean recursionBrake;
    private int pulses;

    public CableDiodeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CABLE_DIODE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CableDiodeBlockEntity diode) {
        if (level.isClientSide) {
            return;
        }
        Direction output = diode.getOutputDirection();
        for (Direction side : Direction.values()) {
            if (side != output) {
                HbmEnergyUtil.subscribeReceiverToNeighborNetwork(level, pos, side, diode);
            }
        }
        diode.pulses = 0;
        diode.setPower(0L);
    }

    public int getThroughputLevel() {
        return throughputLevel;
    }

    public void increaseLevel() {
        setLevel(throughputLevel + 1);
    }

    public void decreaseLevel() {
        setLevel(throughputLevel - 1);
    }

    public void setLevel(int level) {
        int clamped = Mth.clamp(level, MIN_LEVEL, MAX_LEVEL);
        if (this.throughputLevel != clamped) {
            this.throughputLevel = clamped;
            syncChanged();
        }
    }

    public void cyclePriority() {
        HbmEnergyReceiver.ConnectionPriority[] values = HbmEnergyReceiver.ConnectionPriority.values();
        int next = priority.ordinal() + 1;
        if (next > HbmEnergyReceiver.ConnectionPriority.HIGHEST.ordinal()) {
            next = HbmEnergyReceiver.ConnectionPriority.LOWEST.ordinal();
        }
        priority = values[next];
        syncChanged();
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
        long max = 1L;
        for (int i = 0; i < throughputLevel; i++) {
            max *= 10L;
        }
        return max;
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
                LegacyLookOverlayLines.priority(priority)));
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        data.putInt("level", throughputLevel);
        data.putString("priority", priority.name());
        data.putLong("maxRate", getMaxPower());
        data.putLong("transferredThisTick", getPower());
        data.putString("output", getOutputDirection().getName());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_LEVEL, throughputLevel);
        tag.putByte(TAG_PRIORITY, (byte) priority.ordinal());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        throughputLevel = tag.contains(TAG_LEVEL) ? Mth.clamp(tag.getInt(TAG_LEVEL), MIN_LEVEL, MAX_LEVEL) : MIN_LEVEL;
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
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
