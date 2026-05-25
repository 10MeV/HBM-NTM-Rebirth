package com.hbm.ntm.fluid.trait;

import com.hbm.ntm.fluid.FluidType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

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

    @Override
    public void addHiddenInfo(List<Component> info) {
        HeatingStep first = getFirstStep();
        if (first != null) {
            info.add(Component.literal("Thermal capacity: " + first.heatRequired() + " TU per "
                    + first.amountRequired() + "mB").withStyle(ChatFormatting.RED));
        }
        for (HeatingType type : HeatingType.values()) {
            double efficiency = getEfficiency(type);
            if (efficiency > 0.0D) {
                info.add(FluidTooltipUtil.efficiency(type.displayName(), efficiency));
            }
        }
    }

    public record HeatingStep(int amountRequired, int heatRequired, FluidType producedType, int amountProduced) {
    }

    public enum HeatingType {
        BOILER("Boilable"),
        HEATEXCHANGER("Heatable"),
        PWR("PWR Coolant"),
        ICF("ICF Coolant"),
        PA("Particle Accelerator Coolant");

        private final String displayName;

        HeatingType(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }
}
