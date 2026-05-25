package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.trait.CoolableFluidTrait;
import com.hbm.ntm.fluid.trait.CoolableFluidTrait.CoolingType;

public final class HbmTurbineConversion {
    public static TurbineResult run(HbmFluidTank input, HbmFluidTank output, double efficiency, int maxInputMb,
            boolean simulate) {
        if (input == null || output == null || efficiency <= 0.0D || maxInputMb <= 0) {
            return TurbineResult.empty();
        }
        FluidType inputType = input.getTankType();
        CoolableFluidTrait trait = inputType.getTrait(CoolableFluidTrait.class);
        if (trait == null || trait.getEfficiency(CoolingType.TURBINE) <= 0.0D) {
            return TurbineResult.empty(inputType, HbmFluids.NONE, 0, 0, 0L);
        }
        prepareOutputTank(input, output);
        HbmFluidThermalExchange.ThermalResult thermal = HbmFluidThermalExchange.cool(input, output, CoolingType.TURBINE,
                efficiency, maxInputMb, simulate);
        long powerProduced = Math.max(0L, (long) thermal.heatProduced());
        return new TurbineResult(thermal.inputType(), thermal.outputType(), thermal.inputUsed(), thermal.outputProduced(),
                powerProduced);
    }

    public static void prepareOutputTank(HbmFluidTank input, HbmFluidTank output) {
        if (input == null || output == null) {
            return;
        }
        CoolableFluidTrait trait = input.getTankType().getTrait(CoolableFluidTrait.class);
        if (trait == null || trait.getEfficiency(CoolingType.TURBINE) <= 0.0D || trait.getAmountRequired() <= 0
                || trait.getAmountProduced() <= 0) {
            output.setTankType(HbmFluids.NONE);
            return;
        }
        output.setTankType(trait.getCoolsTo());
    }

    private HbmTurbineConversion() {
    }

    public record TurbineResult(FluidType inputType, FluidType outputType, int inputUsed, int outputProduced,
            long powerProduced) {
        public static TurbineResult empty() {
            return empty(HbmFluids.NONE, HbmFluids.NONE, 0, 0, 0L);
        }

        public static TurbineResult empty(FluidType inputType, FluidType outputType, int inputUsed, int outputProduced,
                long powerProduced) {
            return new TurbineResult(inputType, outputType, inputUsed, outputProduced, powerProduced);
        }

        public boolean converted() {
            return inputUsed > 0 && outputProduced > 0;
        }
    }
}
