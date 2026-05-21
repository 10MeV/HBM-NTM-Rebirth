package com.hbm.ntm.fluid;

import java.util.List;

public interface HbmFluidUser {
    int HIGHEST_VALID_PRESSURE = HbmFluidTank.HIGHEST_VALID_PRESSURE;
    int[] DEFAULT_PRESSURE_RANGE = new int[] {0, 0};

    List<HbmFluidTank> getAllTanks();
}
