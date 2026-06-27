package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.tile.InfoProviderEC;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyConnector;
import com.hbm.ntm.energy.HbmEnergyHandler;
import com.hbm.ntm.energy.HbmLoadedEnergy;
import com.hbm.ntm.energy.HbmEnergyProvider;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.Connection;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class HbmEnergyBlockEntity extends BlockEntity implements HbmEnergyConnector, HbmEnergyHandler,
        HbmLoadedEnergy, InfoProviderEC, HbmLegacyLoadedTile {
    private static final String TAG_ENERGY = "Energy";
    private static final int ENERGY_PORT_KEEPALIVE_TICKS = 20;

    protected final HbmEnergyStorage energy;
    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private final LazyOptional<IEnergyStorage> forgeEnergy;
    private final LazyOptional<IEnergyStorage> forgeEnergyInput;
    private final LazyOptional<IEnergyStorage> forgeEnergyOutput;
    private boolean energyPortSubscriptionDirty = true;
    private int lastProviderPortSignature = Integer.MIN_VALUE;
    private int lastReceiverPortSignature = Integer.MIN_VALUE;
    private int lastProviderAllSidesSignature = Integer.MIN_VALUE;
    private int lastReceiverAllSidesSignature = Integer.MIN_VALUE;

    protected HbmEnergyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, HbmEnergyStorage energy) {
        super(type, pos, state);
        this.energy = energy;
        this.energy.setLoadedCheck(this::isEnergyLoaded);
        this.forgeEnergy = LazyOptional.of(() -> new ForgeEnergyAdapter(this.energy));
        this.forgeEnergyInput = LazyOptional.of(() -> new ForgeEnergyAdapter(this.energy, true, false));
        this.forgeEnergyOutput = LazyOptional.of(() -> new ForgeEnergyAdapter(this.energy, false, true));
    }

    @Override
    public AABB getRenderBoundingBox() {
        return LegacyMachineRenderBounds.visibleMultiblockOr(this, super.getRenderBoundingBox());
    }

    public HbmEnergyStorage getEnergyStorage() {
        return energy;
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
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

    public HbmEnergyUtil.PortSetSnapshot inspectEnergyPorts() {
        return level == null
                ? new HbmEnergyUtil.PortSetSnapshot(0, 0, 0, 0, 0, 0, 0L, 0L)
                : HbmEnergyUtil.inspectPorts(level, worldPosition, getEnergyPorts());
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
        if (level == null || level.isClientSide || !shouldRefreshProviderAllSides()) {
            return 0;
        }
        int subscribed = 0;
        for (Direction side : Direction.values()) {
            if (subscribeEnergyProviderToSide(side)) {
                subscribed++;
            }
        }
        lastProviderAllSidesSignature = energyAllSidesSignature(true);
        energyPortSubscriptionDirty = false;
        return subscribed;
    }

    protected int subscribeEnergyReceiverToAllSides() {
        if (level == null || level.isClientSide || !shouldRefreshReceiverAllSides()) {
            return 0;
        }
        int subscribed = 0;
        for (Direction side : Direction.values()) {
            if (subscribeEnergyReceiverToSide(side)) {
                subscribed++;
            }
        }
        lastReceiverAllSidesSignature = energyAllSidesSignature(false);
        energyPortSubscriptionDirty = false;
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
        return subscribeEnergyProviderToPorts(getEnergyPorts(), energy);
    }

    protected int subscribeEnergyReceiverToPorts() {
        return subscribeEnergyReceiverToPorts(getEnergyPorts(), energy);
    }

    protected int subscribeEnergyProviderToPorts(Iterable<EnergyPort> ports, HbmEnergyProvider provider) {
        if (level == null || level.isClientSide || provider == null || !shouldRefreshProviderPorts(ports)) {
            return 0;
        }
        int subscribed = HbmEnergyUtil.subscribeProviderToPorts(level, worldPosition, ports, provider);
        lastProviderPortSignature = energyPortSignature(ports);
        energyPortSubscriptionDirty = false;
        return subscribed;
    }

    protected int subscribeEnergyReceiverToPorts(Iterable<EnergyPort> ports, HbmEnergyReceiver receiver) {
        if (level == null || level.isClientSide || receiver == null || !shouldRefreshReceiverPorts(ports)) {
            return 0;
        }
        int subscribed = HbmEnergyUtil.subscribeReceiverToPorts(level, worldPosition, ports, receiver);
        lastReceiverPortSignature = energyPortSignature(ports);
        energyPortSubscriptionDirty = false;
        return subscribed;
    }

    protected int tryProvideEnergyToPorts() {
        return level == null || level.isClientSide
                ? 0
                : HbmEnergyUtil.tryProvideToPorts(level, worldPosition, getEnergyPorts(), energy);
    }

    protected long pushForgeEnergyToPorts(long maxTransfer) {
        return level == null || level.isClientSide
                ? 0L
                : HbmEnergyUtil.pushForgeEnergyToPorts(level, worldPosition, getEnergyPorts(), energy, maxTransfer);
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
        return HbmEnergyUtil.unsubscribeProviderFromPorts(level, worldPosition, getEnergyPorts(), energy);
    }

    protected int unsubscribeEnergyReceiverFromPorts() {
        if (level == null || level.isClientSide) {
            return 0;
        }
        return HbmEnergyUtil.unsubscribeReceiverFromPorts(level, worldPosition, getEnergyPorts(), energy);
    }

    protected void markEnergyPortSubscriptionsDirty() {
        energyPortSubscriptionDirty = true;
    }

    private boolean shouldRefreshProviderPorts(Iterable<EnergyPort> ports) {
        int signature = energyPortSignature(ports);
        return energyPortSubscriptionDirty || signature != lastProviderPortSignature || isEnergyPortKeepalive();
    }

    private boolean shouldRefreshReceiverPorts(Iterable<EnergyPort> ports) {
        int signature = energyPortSignature(ports);
        return energyPortSubscriptionDirty || signature != lastReceiverPortSignature || isEnergyPortKeepalive();
    }

    private boolean shouldRefreshProviderAllSides() {
        int signature = energyAllSidesSignature(true);
        return energyPortSubscriptionDirty || signature != lastProviderAllSidesSignature || isEnergyPortKeepalive();
    }

    private boolean shouldRefreshReceiverAllSides() {
        int signature = energyAllSidesSignature(false);
        return energyPortSubscriptionDirty || signature != lastReceiverAllSidesSignature || isEnergyPortKeepalive();
    }

    protected boolean isEnergyPortKeepalive() {
        return level != null && Math.floorMod(level.getGameTime() + worldPosition.hashCode(), ENERGY_PORT_KEEPALIVE_TICKS) == 0L;
    }

    private static int energyPortSignature(Iterable<EnergyPort> ports) {
        int signature = 1;
        if (ports != null) {
            for (EnergyPort port : ports) {
                signature = 31 * signature + (port == null ? 0 : port.hashCode());
            }
        }
        return signature;
    }

    private int energyAllSidesSignature(boolean provider) {
        int signature = provider ? 17 : 19;
        for (Direction side : Direction.values()) {
            boolean enabled = provider ? canExtractEnergy(side) : canReceiveEnergy(side);
            signature = 31 * signature + side.ordinal();
            signature = 31 * signature + (enabled ? 1 : 0);
        }
        return signature;
    }

    protected void clearEnergySubscriptions() {
        if (level == null || level.isClientSide) {
            markEnergyPortSubscriptionsDirty();
            return;
        }
        unsubscribeEnergyProviderFromAllSides();
        unsubscribeEnergyReceiverFromAllSides();
        unsubscribeEnergyProviderFromPorts();
        unsubscribeEnergyReceiverFromPorts();
        markEnergyPortSubscriptionsDirty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        tag.put(TAG_ENERGY, energy.serializeNBT());
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        writeLegacyLoadedTileClientTag(tag);
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
        readLegacyLoadedTileClientTag(tag);
        if (tag.contains(TAG_ENERGY)) {
            energy.deserializeNBT(tag.getCompound(TAG_ENERGY));
        }
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(saveWithoutMetadata());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            load(tag);
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
        readLegacyLoadedTileNbt(tag);
        energy.deserializeNBT(tag.getCompound(TAG_ENERGY));
        markEnergyPortSubscriptionsDirty();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        forgeEnergy.invalidate();
        forgeEnergyInput.invalidate();
        forgeEnergyOutput.invalidate();
    }

    @Override
    public void setRemoved() {
        clearEnergySubscriptions();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        clearEnergySubscriptions();
        super.onChunkUnloaded();
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
