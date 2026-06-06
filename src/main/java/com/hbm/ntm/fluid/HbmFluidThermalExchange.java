package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.trait.CoolableFluidTrait;
import com.hbm.ntm.fluid.trait.CoolableFluidTrait.CoolingType;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingStep;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingType;

public final class HbmFluidThermalExchange {

    public static ThermalResult heat(HbmFluidTank input, HbmFluidTank output, HeatingType heatingType, int availableHeat, boolean simulate) {
        return heat(input, output, heatingType, availableHeat, 1.0D, Integer.MAX_VALUE, simulate);
    }

    public static ThermalResult heat(HbmFluidTank input, HbmFluidTank output, HeatingType heatingType, int availableHeat, double machineEfficiency, int maxInputMb, boolean simulate) {
        if (input == null || output == null || heatingType == null || availableHeat <= 0 || maxInputMb <= 0) {
            return ThermalResult.empty();
        }
        FluidType inputType = input.getTankType();
        HeatableFluidTrait trait = inputType.getTrait(HeatableFluidTrait.class);
        if (trait == null || trait.getEfficiency(heatingType) <= 0.0D || machineEfficiency <= 0.0D) {
            return ThermalResult.empty();
        }
        HeatingStep step = trait.getFirstStep();
        if (step == null || step.amountRequired() <= 0 || step.amountProduced() <= 0 || step.heatRequired() <= 0) {
            return ThermalResult.empty();
        }

        int heatPerOperation = Math.max((int) Math.ceil(step.heatRequired() / (trait.getEfficiency(heatingType) * machineEfficiency)), 1);
        int operations = Math.min(input.getFill() / step.amountRequired(), output.getSpaceFor(step.producedType()) / step.amountProduced());
        operations = Math.min(operations, availableHeat / heatPerOperation);
        operations = Math.min(operations, maxInputMb / step.amountRequired());
        if (operations <= 0) {
            return ThermalResult.empty(inputType, step.producedType(), 0, 0, 0, 0.0D);
        }

        int inputUsed = operations * step.amountRequired();
        int outputProduced = operations * step.amountProduced();
        int heatUsed = operations * heatPerOperation;
        if (!simulate) {
            input.drain(inputUsed, false);
            output.fill(step.producedType(), outputProduced, output.getPressure(), false);
        }
        return new ThermalResult(inputType, step.producedType(), inputUsed, outputProduced, heatUsed, 0.0D);
    }

    public static ThermalResult cool(HbmFluidTank input, HbmFluidTank output, CoolingType coolingType, boolean simulate) {
        return cool(input, output, coolingType, 1.0D, Integer.MAX_VALUE, simulate);
    }

    public static ThermalResult cool(HbmFluidTank input, HbmFluidTank output, CoolingType coolingType, double machineEfficiency, int maxInputMb, boolean simulate) {
        if (input == null || output == null || coolingType == null || maxInputMb <= 0) {
            return ThermalResult.empty();
        }
        FluidType inputType = input.getTankType();
        CoolableFluidTrait trait = inputType.getTrait(CoolableFluidTrait.class);
        if (trait == null || trait.getEfficiency(coolingType) <= 0.0D || machineEfficiency <= 0.0D) {
            return ThermalResult.empty();
        }
        if (trait.getAmountRequired() <= 0 || trait.getAmountProduced() <= 0) {
            return ThermalResult.empty();
        }

        int operations = Math.min(input.getFill() / trait.getAmountRequired(), output.getSpaceFor(trait.getCoolsTo()) / trait.getAmountProduced());
        operations = Math.min(operations, maxInputMb / trait.getAmountRequired());
        if (operations <= 0) {
            return ThermalResult.empty(inputType, trait.getCoolsTo(), 0, 0, 0, 0.0D);
        }

        int inputUsed = operations * trait.getAmountRequired();
        int outputProduced = operations * trait.getAmountProduced();
        double heatProduced = operations * trait.getHeatEnergy() * trait.getEfficiency(coolingType) * machineEfficiency;
        if (!simulate) {
            input.drain(inputUsed, false);
            output.fill(trait.getCoolsTo(), outputProduced, output.getPressure(), false);
        }
        return new ThermalResult(inputType, trait.getCoolsTo(), inputUsed, outputProduced, 0, heatProduced);
    }

    private HbmFluidThermalExchange() {
    }

    public record ThermalResult(FluidType inputType, FluidType outputType, int inputUsed, int outputProduced, int heatUsed, double heatProduced) {
        public static ThermalResult empty() {
            return empty(HbmFluids.NONE, HbmFluids.NONE, 0, 0, 0, 0.0D);
        }

        private static ThermalResult empty(FluidType inputType, FluidType outputType, int inputUsed, int outputProduced, int heatUsed, double heatProduced) {
            return new ThermalResult(inputType, outputType, inputUsed, outputProduced, heatUsed, heatProduced);
        }

        public boolean converted() {
            return inputUsed > 0 && outputProduced > 0;
        }
    }
}
