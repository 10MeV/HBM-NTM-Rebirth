package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class HbmEnergyBlockEntity extends BlockEntity {
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

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_ENERGY, energy.serializeNBT());
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
