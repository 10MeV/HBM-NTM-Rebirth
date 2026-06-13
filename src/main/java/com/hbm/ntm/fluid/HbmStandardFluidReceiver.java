package com.hbm.ntm.fluid;

import java.util.List;

public interface HbmStandardFluidReceiver extends HbmFluidReceiver {
    List<HbmFluidTank> getReceivingTanks();

    @Override
    default long getDemand(FluidType type, int pressure) {
        return inspectReceivingTanks(type, pressure).demandMb();
    }

    @Override
    default long transferFluid(FluidType type, int pressure, long amount) {
        return transferFluidReport(type, pressure, amount).remainderMb();
    }

    @Override
    default int[] getReceivingPressureRange(FluidType type) {
        return HbmFluidTankSet.pressureRange(getReceivingTanks(), type);
    }

    default HbmFluidTankSet.TankSetInspection inspectReceivingTanks(FluidType type, int pressure) {
        return HbmFluidTankSet.inspectReceivingTanks(getReceivingTanks(), type, pressure);
    }

    default HbmFluidTankSet.TankTransferReport previewTransferFluid(FluidType type, int pressure, long amount) {
        return HbmFluidTankSet.previewReceive(getReceivingTanks(), type, pressure, amount);
    }

    default HbmFluidTankSet.TankTransferReport transferFluidReport(FluidType type, int pressure, long amount) {
        return HbmFluidTankSet.receive(getReceivingTanks(), type, pressure, amount, false);
    }
}
