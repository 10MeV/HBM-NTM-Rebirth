package com.hbm.ntm.fluid;

import java.util.List;

public interface HbmStandardFluidSender extends HbmFluidProvider {
    List<HbmFluidTank> getSendingTanks();

    @Override
    default long getFluidAvailable(FluidType type, int pressure) {
        return inspectSendingTanks(type, pressure).availableMb();
    }

    @Override
    default void useUpFluid(FluidType type, int pressure, long amount) {
        useUpFluidReport(type, pressure, amount);
    }

    @Override
    default int[] getProvidingPressureRange(FluidType type) {
        return HbmFluidTankSet.pressureRange(getSendingTanks(), type);
    }

    default HbmFluidTankSet.TankSetInspection inspectSendingTanks(FluidType type, int pressure) {
        return HbmFluidTankSet.inspectSendingTanks(getSendingTanks(), type, pressure);
    }

    default HbmFluidTankSet.TankTransferReport previewUseUpFluid(FluidType type, int pressure, long amount) {
        return HbmFluidTankSet.previewUseUp(getSendingTanks(), type, pressure, amount);
    }

    default HbmFluidTankSet.TankTransferReport useUpFluidReport(FluidType type, int pressure, long amount) {
        return HbmFluidTankSet.useUp(getSendingTanks(), type, pressure, amount, false);
    }
}
