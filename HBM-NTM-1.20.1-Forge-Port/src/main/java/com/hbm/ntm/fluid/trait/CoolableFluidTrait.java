package com.hbm.ntm.fluid.trait;

import com.hbm.ntm.fluid.FluidType;
import java.util.EnumMap;
import java.util.Map;

public class CoolableFluidTrait extends FluidTrait {
    private final FluidType coolsTo;
    private final int amountRequired;
    private final int amountProduced;
    private final int heatEnergy;
    private final Map<CoolingType, Double> efficiencies = new EnumMap<>(CoolingType.class);

    public CoolableFluidTrait(FluidType coolsTo, int amountRequired, int amountProduced, int heatEnergy) {
        this.coolsTo = coolsTo;
        this.amountRequired = amountRequired;
        this.amountProduced = amountProduced;
        this.heatEnergy = heatEnergy;
    }

    public CoolableFluidTrait setEfficiency(CoolingType type, double efficiency) {
        efficiencies.put(type, efficiency);
        return this;
    }

    public double getEfficiency(CoolingType type) {
        return efficiencies.getOrDefault(type, 0.0D);
    }

    public FluidType getCoolsTo() {
        return coolsTo;
    }

    public int getAmountRequired() {
        return amountRequired;
    }

    public int getAmountProduced() {
        return amountProduced;
    }

    public int getHeatEnergy() {
        return heatEnergy;
    }

    public enum CoolingType {
        TURBINE,
        HEATEXCHANGER
    }
}
