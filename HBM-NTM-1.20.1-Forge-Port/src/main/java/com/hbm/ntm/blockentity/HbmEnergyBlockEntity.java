package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.tile.InfoProviderEC;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyConnector;
import com.hbm.ntm.energy.HbmEnergyHandler;
import com.hbm.ntm.energy.HbmLoadedEnergy;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.network.HbmTileSyncable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.Connection;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class HbmEnergyBlockEntity extends BlockEntity implements HbmEnergyConnector, HbmEnergyHandler, HbmLoadedEnergy, InfoProviderEC, HbmTileSyncable {
    private static final String TAG_ENERGY = "Energy";

    protected final HbmEnergyStorage energy;
    private final LazyOptional<IEnergyStorage> forgeEnergy;
    private final LazyOptional<IEnergyStorage> forgeEnergyInput;
    private final LazyOptional<IEnergyStorage> forgeEnergyOutput;

    protected HbmEnergyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, HbmEnergyStorage energy) {
        super(type, pos, state);
        this.energy = energy;
        this.forgeEnergy = LazyOptional.of(() -> new ForgeEnergyAdapter(this.energy));
        this.forgeEnergyInput = LazyOptional.of(() -> new ForgeEnergyAdapter(this.energy, true, false));
        this.forgeEnergyOutput = LazyOptional.of(() -> new ForgeEnergyAdapter(this.energy, false, true));
    }

    public HbmEnergyStorage getEnergyStorage() {
        return energy;
    }

    @Override
    public long getPower() {
        return energy.getPower();
    }

    @Override
    public void setPower(long power) {
        energy.setPower(power);
    }

    @Override
    public long getMaxPower() {
        return energy.getMaxPower();
    }

    protected boolean canAccessEnergy(@Nullable Direction side) {
        return getEnergySideMode(side) != HbmEnergySideMode.NONE;
    }

    protected boolean canReceiveEnergy(@Nullable Direction side) {
        return getEnergySideMode(side).canReceive();
    }

    protected boolean canExtractEnergy(@Nullable Direction side) {
        return getEnergySideMode(side).canExtract();
    }

    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.BOTH;
    }

    protected Iterable<EnergyPort> getEnergyPorts() {
        return List.of();
    }

    @Override
    public boolean canConnectEnergy(@Nullable Direction side) {
        return canAccessEnergy(side);
    }

    protected long pullEnergyFromSide(Direction side, long maxTransfer) {
        if (level == null || level.isClientSide || !canReceiveEnergy(side)) {
            return 0L;
        }
        return HbmEnergyUtil.pullFromNeighbor(level, worldPosition, side, energy, maxTransfer);
    }

    protected long pushEnergyToSide(Direction side, long maxTransfer) {
        if (level == null || level.isClientSide || !canExtractEnergy(side)) {
            return 0L;
        }
        return HbmEnergyUtil.pushToNeighbor(level, worldPosition, side, energy, maxTransfer);
    }

    protected long pullEnergyFromAllSides(long totalMaxTransfer) {
        if (level == null || level.isClientSide || totalMaxTransfer <= 0L) {
            return 0L;
        }
        long transferred = 0L;
        for (Direction side : Direction.values()) {
            long remaining = totalMaxTransfer - transferred;
            if (remaining <= 0L) {
                break;
            }
            transferred += pullEnergyFromSide(side, remaining);
        }
        return transferred;
    }

    protected long pushEnergyToAllSides(long totalMaxTransfer) {
        if (level == null || level.isClientSide || totalMaxTransfer <= 0L) {
            return 0L;
        }
        long transferred = 0L;
        for (Direction side : Direction.values()) {
            long remaining = totalMaxTransfer - transferred;
            if (remaining <= 0L) {
                break;
            }
            transferred += pushEnergyToSide(side, remaining);
        }
        return transferred;
    }

    protected boolean subscribeEnergyProviderToSide(Direction side) {
        return level != null
                && !level.isClientSide
                && canExtractEnergy(side)
                && HbmEnergyUtil.subscribeProviderToNeighborNetwork(level, worldPosition, side, energy);
    }

    protected boolean subscribeEnergyReceiverToSide(Direction side) {
        return level != null
                && !level.isClientSide
                && canReceiveEnergy(side)
                && HbmEnergyUtil.subscribeReceiverToNeighborNetwork(level, worldPosition, side, energy);
    }

    protected int subscribeEnergyProviderToAllSides() {
        int subscribed = 0;
        for (Direction side : Direction.values()) {
            if (subscribeEnergyProviderToSide(side)) {
                subscribed++;
            }
        }
        return subscribed;
    }

    protected int subscribeEnergyReceiverToAllSides() {
        int subscribed = 0;
        for (Direction side : Direction.values()) {
            if (subscribeEnergyReceiverToSide(side)) {
                subscribed++;
            }
        }
        return subscribed;
    }

    protected boolean subscribeEnergyProviderToPort(EnergyPort port) {
        return level != null
                && !level.isClientSide
                && port != null
                && HbmEnergyUtil.subscribeProviderToPort(level, worldPosition, port, energy);
    }

    protected boolean subscribeEnergyReceiverToPort(EnergyPort port) {
        return level != null
                && !level.isClientSide
                && port != null
                && HbmEnergyUtil.subscribeReceiverToPort(level, worldPosition, port, energy);
    }

    protected int subscribeEnergyProviderToPorts() {
        return level == null || level.isClientSide
                ? 0
                : HbmEnergyUtil.subscribeProviderToPorts(level, worldPosition, getEnergyPorts(), energy);
    }

    protected int subscribeEnergyReceiverToPorts() {
        return level == null || level.isClientSide
                ? 0
                : HbmEnergyUtil.subscribeReceiverToPorts(level, worldPosition, getEnergyPorts(), energy);
    }

    protected int tryProvideEnergyToPorts() {
        return level == null || level.isClientSide
                ? 0
                : HbmEnergyUtil.tryProvideToPorts(level, worldPosition, getEnergyPorts(), energy);
    }

    protected boolean unsubscribeEnergyProviderFromSide(Direction side) {
        return level != null
                && !level.isClientSide
                && HbmEnergyUtil.unsubscribeProviderFromNeighborNetwork(level, worldPosition, side, energy);
    }

    protected boolean unsubscribeEnergyReceiverFromSide(Direction side) {
        return level != null
                && !level.isClientSide
                && HbmEnergyUtil.unsubscribeReceiverFromNeighborNetwork(level, worldPosition, side, energy);
    }

    protected int unsubscribeEnergyProviderFromAllSides() {
        int unsubscribed = 0;
        for (Direction side : Direction.values()) {
            if (unsubscribeEnergyProviderFromSide(side)) {
                unsubscribed++;
            }
        }
        return unsubscribed;
    }

    protected int unsubscribeEnergyReceiverFromAllSides() {
        int unsubscribed = 0;
        for (Direction side : Direction.values()) {
            if (unsubscribeEnergyReceiverFromSide(side)) {
                unsubscribed++;
            }
        }
        return unsubscribed;
    }

    protected boolean unsubscribeEnergyProviderFromPort(EnergyPort port) {
        return level != null
                && !level.isClientSide
                && port != null
                && HbmEnergyUtil.unsubscribeProviderFromPort(level, worldPosition, port, energy);
    }

    protected boolean unsubscribeEnergyReceiverFromPort(EnergyPort port) {
        return level != null
                && !level.isClientSide
                && port != null
                && HbmEnergyUtil.unsubscribeReceiverFromPort(level, worldPosition, port, energy);
    }

    protected int unsubscribeEnergyProviderFromPorts() {
        if (level == null || level.isClientSide) {
            return 0;
        }
        int unsubscribed = 0;
        for (EnergyPort port : getEnergyPorts()) {
            if (unsubscribeEnergyProviderFromPort(port)) {
                unsubscribed++;
            }
        }
        return unsubscribed;
    }

    protected int unsubscribeEnergyReceiverFromPorts() {
        if (level == null || level.isClientSide) {
            return 0;
        }
        int unsubscribed = 0;
        for (EnergyPort port : getEnergyPorts()) {
            if (unsubscribeEnergyReceiverFromPort(port)) {
                unsubscribed++;
            }
        }
        return unsubscribed;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_ENERGY, energy.serializeNBT());
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        tag.put(TAG_ENERGY, energy.serializeNBT());
        return tag;
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
    public void handleClientSyncTag(CompoundTag tag) {
        if (tag.contains(TAG_ENERGY)) {
            energy.deserializeNBT(tag.getCompound(TAG_ENERGY));
        }
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        CompatEnergyControl.getEnergyData(this, data);
    }

    @Override
    public boolean isEnergyLoaded() {
        return level != null && !isRemoved();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.deserializeNBT(tag.getCompound(TAG_ENERGY));
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        forgeEnergy.invalidate();
        forgeEnergyInput.invalidate();
        forgeEnergyOutput.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY && canAccessEnergy(side)) {
            boolean canReceive = canReceiveEnergy(side);
            boolean canExtract = canExtractEnergy(side);
            if (canReceive && canExtract) {
                return forgeEnergy.cast();
            }
            if (canReceive) {
                return forgeEnergyInput.cast();
            }
            if (canExtract) {
                return forgeEnergyOutput.cast();
            }
        }
        return super.getCapability(capability, side);
    }
}
