package com.hbm.ntm.neutron;

public final class RBMKHeaterRuntime {
    private RBMKHeaterRuntime() {
    }

    public static HeaterTickResult tickHeater(
            RBMKThermalState thermalState,
            RBMKHeaterState heaterState,
            HeatingStepSpec step) {
        if (step == null || step.efficiency() <= 0.0D) {
            heaterState.setValidHeatableFluid(false);
            return new HeaterTickResult(0, 0, 0.0D, false);
        }

        heaterState.setValidHeatableFluid(true);
        double temperatureRange = thermalState.heat() - step.producedTemperature();
        if (temperatureRange <= 0.0D) {
            return new HeaterTickResult(0, 0, 0.0D, true);
        }

        double thermalUnitsPerDegree = 2_000.0D * step.efficiency();
        int inputOps = heaterState.feedFill() / step.inputAmount();
        int outputOps = (heaterState.outputMax() - heaterState.outputFill()) / step.outputAmount();
        int temperatureOps = (int) Math.floor((temperatureRange * thermalUnitsPerDegree) / step.heatRequired());
        int operations = Math.min(inputOps, Math.min(outputOps, temperatureOps));
        if (operations <= 0) {
            return new HeaterTickResult(0, 0, 0.0D, true);
        }

        int inputUsed = step.inputAmount() * operations;
        int outputProduced = step.outputAmount() * operations;
        double heatUsed = (step.heatRequired() * operations / thermalUnitsPerDegree) * step.efficiency();

        heaterState.setFeedFill(heaterState.feedFill() - inputUsed);
        heaterState.setOutputFill(heaterState.outputFill() + outputProduced);
        thermalState.setHeat(thermalState.heat() - heatUsed);
        return new HeaterTickResult(inputUsed, outputProduced, heatUsed, true);
    }

    public record HeatingStepSpec(
            int inputAmount,
            int outputAmount,
            double heatRequired,
            double producedTemperature,
            double efficiency) {
        public HeatingStepSpec {
            inputAmount = Math.max(1, inputAmount);
            outputAmount = Math.max(1, outputAmount);
            heatRequired = heatRequired <= 0.0D ? 1.0D : heatRequired;
            efficiency = Math.max(0.0D, efficiency);
        }
    }

    public record HeaterTickResult(
            int inputUsed,
            int outputProduced,
            double heatUsed,
            boolean validHeatableFluid) {
    }
}
