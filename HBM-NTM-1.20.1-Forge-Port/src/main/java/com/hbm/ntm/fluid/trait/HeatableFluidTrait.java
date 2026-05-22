package com.hbm.ntm.fluid.trait;

import com.hbm.ntm.fluid.FluidType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class HeatableFluidTrait extends FluidTrait {
    private final List<HeatingStep> steps = new ArrayList<>();
    private final Map<HeatingType, Double> efficiencies = new EnumMap<>(HeatingType.class);

    public HeatableFluidTrait addStep(int heatRequired, int amountRequired, FluidType producedType, int amountProduced) {
        steps.add(new HeatingStep(amountRequired, heatRequired, producedType, amountProduced));
        return this;
    }

    public HeatableFluidTrait setEfficiency(HeatingType type, double efficiency) {
        efficiencies.put(type, efficiency);
        return this;
    }

    public double getEfficiency(HeatingType type) {
        return efficiencies.getOrDefault(type, 0.0D);
    }

    public List<HeatingStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public HeatingStep getFirstStep() {
        return steps.isEmpty() ? null : steps.get(0);
    }

    public record HeatingStep(int amountRequired, int heatRequired, FluidType producedType, int amountProduced) {
    }

    public enum HeatingType {
        BOILER,
        HEATEXCHANGER,
        PWR,
        ICF,
        PA
    }
}
