package com.hbm.ntm.fluid;

import java.util.List;

public interface HbmStandardFluidSender extends HbmFluidProvider {
    List<HbmFluidTank> getSendingTanks();

    @Override
    default long getFluidAvailable(FluidType type, int pressure) {
        long amount = 0L;
        for (HbmFluidTank tank : getSendingTanks()) {
            if (tank.getTankType() == type && tank.getPressure() == pressure) {
                amount += tank.getFill();
            }
        }
        return amount;
    }

    @Override
    default void useUpFluid(FluidType type, int pressure, long amount) {
        int matchingTanks = 0;
        for (HbmFluidTank tank : getSendingTanks()) {
            if (tank.getTankType() == type && tank.getPressure() == pressure) {
                matchingTanks++;
            }
        }
        if (matchingTanks > 1) {
            int firstRound = (int) Math.floor((double) amount / (double) matchingTanks);
            for (HbmFluidTank tank : getSendingTanks()) {
                if (tank.getTankType() == type && tank.getPressure() == pressure) {
                    int toRemove = Math.min(firstRound, tank.getFill());
                    tank.setFill(tank.getFill() - toRemove);
                    amount -= toRemove;
                }
            }
        }
        if (amount > 0L) {
            for (HbmFluidTank tank : getSendingTanks()) {
                if (tank.getTankType() == type && tank.getPressure() == pressure) {
                    int toRemove = (int) Math.min(amount, tank.getFill());
                    tank.setFill(tank.getFill() - toRemove);
                    amount -= toRemove;
                }
            }
        }
    }

    @Override
    default int[] getProvidingPressureRange(FluidType type) {
        int lowest = HIGHEST_VALID_PRESSURE;
        int highest = 0;
        for (HbmFluidTank tank : getSendingTanks()) {
            if (tank.getTankType() == type) {
                lowest = Math.min(lowest, tank.getPressure());
                highest = Math.max(highest, tank.getPressure());
            }
        }
        return lowest <= highest ? new int[] {lowest, highest} : DEFAULT_PRESSURE_RANGE;
    }
}
