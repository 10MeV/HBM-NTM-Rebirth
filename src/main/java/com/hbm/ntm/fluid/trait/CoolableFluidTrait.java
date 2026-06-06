package com.hbm.ntm.fluid.trait;

import com.hbm.ntm.fluid.FluidType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

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

    @Override
    public void addHiddenInfo(List<Component> info) {
        info.add(Component.literal("Thermal capacity: " + heatEnergy + " TU per " + amountRequired + "mB")
                .withStyle(ChatFormatting.RED));
        for (CoolingType type : CoolingType.values()) {
            double efficiency = getEfficiency(type);
            if (efficiency > 0.0D) {
                info.add(FluidTooltipUtil.efficiency(type.displayName(), efficiency));
            }
        }
    }

    public enum CoolingType {
        TURBINE("Turbine Steam"),
        HEATEXCHANGER("Coolable");

        private final String displayName;

        CoolingType(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }
}
