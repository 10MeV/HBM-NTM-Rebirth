package com.hbm.ntm.energy;

import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyAdapter implements IEnergyStorage {
    private final HbmEnergyHandler handler;
    private final boolean canReceive;
    private final boolean canExtract;

    public ForgeEnergyAdapter(HbmEnergyHandler handler) {
        this(handler, true, true);
    }

    public ForgeEnergyAdapter(HbmEnergyHandler handler, boolean canReceive, boolean canExtract) {
        this.handler = handler;
        this.canReceive = canReceive;
        this.canExtract = canExtract;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive || !(handler instanceof HbmEnergyReceiver receiver) || maxReceive <= 0) {
            return 0;
        }
        long allowed = Math.min((long) maxReceive, receiver.getReceiverSpeed());
        long accepted = Math.min(allowed, Math.max(0L, receiver.getMaxPower() - receiver.getPower()));
        if (!simulate && accepted > 0L) {
            accepted = allowed - receiver.transferPower(allowed);
        }
        return HbmEnergyUtil.toForgeInt(accepted);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract || !(handler instanceof HbmEnergyProvider provider) || maxExtract <= 0) {
            return 0;
        }
        long allowed = Math.min((long) maxExtract, provider.getProviderSpeed());
        long extracted = Math.min(allowed, provider.getPower());
        if (!simulate) {
            extracted = provider.usePower(extracted);
        }
        return HbmEnergyUtil.toForgeInt(extracted);
    }

    @Override
    public int getEnergyStored() {
        return HbmEnergyUtil.toForgeInt(handler.getPower());
    }

    @Override
    public int getMaxEnergyStored() {
        return HbmEnergyUtil.toForgeInt(handler.getMaxPower());
    }

    @Override
    public boolean canExtract() {
        return canExtract && handler instanceof HbmEnergyProvider provider && provider.getProviderSpeed() > 0L;
    }

    @Override
    public boolean canReceive() {
        return canReceive && handler instanceof HbmEnergyReceiver receiver && receiver.getReceiverSpeed() > 0L;
    }
}
