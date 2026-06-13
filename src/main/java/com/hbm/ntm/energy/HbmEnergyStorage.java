package com.hbm.ntm.energy;

import java.util.function.BooleanSupplier;
import net.minecraft.nbt.CompoundTag;

public class HbmEnergyStorage implements HbmEnergyProvider, HbmEnergyReceiver, HbmLoadedEnergy {
    public static final String DEFAULT_POWER_TAG = "Power";

    private long maxPower;
    private long maxReceive;
    private long maxExtract;
    private long power;
    private BooleanSupplier loadedCheck;

    public HbmEnergyStorage(long maxPower) {
        this(maxPower, maxPower, maxPower);
    }

    public HbmEnergyStorage(long maxPower, long maxTransfer) {
        this(maxPower, maxTransfer, maxTransfer);
    }

    public HbmEnergyStorage(long maxPower, long maxReceive, long maxExtract) {
        this.maxPower = Math.max(0L, maxPower);
        this.maxReceive = Math.max(0L, maxReceive);
        this.maxExtract = Math.max(0L, maxExtract);
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.power = clampPower(power);
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    public void setMaxPower(long maxPower) {
        this.maxPower = Math.max(0L, maxPower);
        setPower(power);
    }

    public void setTransferRates(long maxReceive, long maxExtract) {
        this.maxReceive = Math.max(0L, maxReceive);
        this.maxExtract = Math.max(0L, maxExtract);
    }

    public HbmEnergyStorage setLoadedCheck(BooleanSupplier loadedCheck) {
        this.loadedCheck = loadedCheck;
        return this;
    }

    @Override
    public boolean isEnergyLoaded() {
        return loadedCheck == null || loadedCheck.getAsBoolean();
    }

    @Override
    public long getReceiverSpeed() {
        return maxReceive;
    }

    @Override
    public long getProviderSpeed() {
        return maxExtract;
    }

    @Override
    public long transferPower(long power) {
        if (power <= 0L) {
            return 0L;
        }
        long cappedTransfer = Math.min(power, maxReceive);
        long cappedRemainder = HbmEnergyReceiver.super.transferPower(cappedTransfer);
        return power - cappedTransfer + cappedRemainder;
    }

    @Override
    public long usePower(long power) {
        return HbmEnergyProvider.super.usePower(Math.min(power, maxExtract));
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(DEFAULT_POWER_TAG, power);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        setPower(tag.getLong(DEFAULT_POWER_TAG));
    }
}
