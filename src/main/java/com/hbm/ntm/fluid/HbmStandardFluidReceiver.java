package com.hbm.ntm.fluid;

import java.util.List;

public interface HbmStandardFluidReceiver extends HbmFluidReceiver {
    List<HbmFluidTank> getReceivingTanks();

    @Override
    default long getDemand(FluidType type, int pressure) {
        long amount = 0L;
        for (HbmFluidTank tank : getReceivingTanks()) {
            if (tank.getTankType() == type && tank.getPressure() == pressure) {
                amount += tank.getSpace();
            }
        }
        return amount;
    }

    @Override
    default long transferFluid(FluidType type, int pressure, long amount) {
        int matchingTanks = 0;
        for (HbmFluidTank tank : getReceivingTanks()) {
            if (tank.getTankType() == type && tank.getPressure() == pressure) {
                matchingTanks++;
            }
        }
        if (matchingTanks > 1) {
            int firstRound = (int) Math.floor((double) amount / (double) matchingTanks);
            for (HbmFluidTank tank : getReceivingTanks()) {
                if (tank.getTankType() == type && tank.getPressure() == pressure) {
                    int accepted = tank.fill(type, firstRound, pressure, false);
                    amount -= accepted;
                }
            }
        }
        if (amount > 0L) {
            for (HbmFluidTank tank : getReceivingTanks()) {
                if (tank.getTankType() == type && tank.getPressure() == pressure) {
                    int accepted = tank.fill(type, (int) Math.min(Integer.MAX_VALUE, amount), pressure, false);
                    amount -= accepted;
                }
            }
        }
        return amount;
    }

    @Override
    default int[] getReceivingPressureRange(FluidType type) {
        int lowest = HIGHEST_VALID_PRESSURE;
        int highest = 0;
        for (HbmFluidTank tank : getReceivingTanks()) {
            if (tank.getTankType() == type) {
                lowest = Math.min(lowest, tank.getPressure());
                highest = Math.max(highest, tank.getPressure());
            }
        }
        return lowest <= highest ? new int[] {lowest, highest} : DEFAULT_PRESSURE_RANGE;
    }
}
