package com.hbm.ntm.neutron;

import java.util.function.BiFunction;

public record RBMKFuelRodSpec(
        double reactivity,
        double selfRate,
        RBMKFuelRodRuntime.BurnFunction burnFunction,
        RBMKFuelRodRuntime.DepletionFunction depletionFunction,
        double xenonGeneration,
        double xenonBurnDivisor,
        double heatPerFlux,
        double totalYield,
        double meltingPoint,
        double diffusion,
        RBMKFluxReceiver.NType inputType,
        RBMKFluxReceiver.NType outputType,
        int colorTint,
        double heatCoefficientStart,
        double heatCoefficientLength,
        BiFunction<Double, Double, Double> outputRatioCurve,
        BiFunction<Double, Double, Double> inputFluxCurve) {
    public static Builder builder(double totalYield) {
        return new Builder(totalYield);
    }

    public RBMKFuelRodSpec {
        burnFunction = burnFunction == null ? RBMKFuelRodRuntime.BurnFunction.LOG_TEN : burnFunction;
        depletionFunction = depletionFunction == null ? RBMKFuelRodRuntime.DepletionFunction.GENTLE_SLOPE : depletionFunction;
        xenonBurnDivisor = xenonBurnDivisor <= 0.0D ? 50.0D : xenonBurnDivisor;
        totalYield = Math.max(0.0D, totalYield);
        meltingPoint = meltingPoint <= 0.0D ? 1000.0D : meltingPoint;
        diffusion = Math.max(0.0D, diffusion);
        inputType = inputType == null ? RBMKFluxReceiver.NType.SLOW : inputType;
        outputType = outputType == null ? RBMKFluxReceiver.NType.FAST : outputType;
        heatCoefficientLength = Math.max(0.0D, heatCoefficientLength);
    }

    public double outputRatio(double fluxFastRatioIn, double enrichment) {
        if (outputRatioCurve != null) {
            return clamp01(outputRatioCurve.apply(fluxFastRatioIn, enrichment));
        }
        return outputType == RBMKFluxReceiver.NType.SLOW ? 0.0D : 1.0D;
    }

    public double inputFlux(RBMKRodFluxState fluxState, double enrichment) {
        if (inputFluxCurve != null) {
            return Math.max(0.0D, inputFluxCurve.apply(fluxState.fluxQuantity(), fluxState.fluxFastRatio()));
        }
        return fluxState.fluxFromType(inputType);
    }

    private static double clamp01(double value) {
        if (value < 0.0D || Double.isNaN(value)) {
            return 0.0D;
        }
        if (value > 1.0D) {
            return 1.0D;
        }
        return value;
    }

    public static final class Builder {
        private double reactivity;
        private double selfRate;
        private RBMKFuelRodRuntime.BurnFunction burnFunction = RBMKFuelRodRuntime.BurnFunction.LOG_TEN;
        private RBMKFuelRodRuntime.DepletionFunction depletionFunction =
                RBMKFuelRodRuntime.DepletionFunction.GENTLE_SLOPE;
        private double xenonGeneration = 0.5D;
        private double xenonBurnDivisor = 50.0D;
        private double heatPerFlux = 1.0D;
        private final double totalYield;
        private double meltingPoint = 1000.0D;
        private double diffusion = 0.02D;
        private RBMKFluxReceiver.NType inputType = RBMKFluxReceiver.NType.SLOW;
        private RBMKFluxReceiver.NType outputType = RBMKFluxReceiver.NType.FAST;
        private int colorTint = 0x304825;
        private double heatCoefficientStart;
        private double heatCoefficientLength;
        private BiFunction<Double, Double, Double> outputRatioCurve;
        private BiFunction<Double, Double, Double> inputFluxCurve;

        private Builder(double totalYield) {
            this.totalYield = totalYield;
        }

        public Builder stats(double reactivity, double selfRate) {
            this.reactivity = reactivity;
            this.selfRate = selfRate;
            return this;
        }

        public Builder burnFunction(RBMKFuelRodRuntime.BurnFunction burnFunction) {
            this.burnFunction = burnFunction;
            return this;
        }

        public Builder depletionFunction(RBMKFuelRodRuntime.DepletionFunction depletionFunction) {
            this.depletionFunction = depletionFunction;
            return this;
        }

        public Builder xenon(double generation, double burnDivisor) {
            this.xenonGeneration = generation;
            this.xenonBurnDivisor = burnDivisor;
            return this;
        }

        public Builder heat(double heatPerFlux, double diffusion, double meltingPoint) {
            this.heatPerFlux = heatPerFlux;
            this.diffusion = diffusion;
            this.meltingPoint = meltingPoint;
            return this;
        }

        public Builder neutronTypes(RBMKFluxReceiver.NType inputType, RBMKFluxReceiver.NType outputType) {
            this.inputType = inputType;
            this.outputType = outputType;
            return this;
        }

        public Builder colorTint(int colorTint) {
            this.colorTint = colorTint;
            return this;
        }

        public Builder heatCoefficient(double start, double length) {
            this.heatCoefficientStart = start;
            this.heatCoefficientLength = length;
            return this;
        }

        public Builder specialFluxCurves(
                BiFunction<Double, Double, Double> outputRatioCurve,
                BiFunction<Double, Double, Double> inputFluxCurve) {
            this.outputRatioCurve = outputRatioCurve;
            this.inputFluxCurve = inputFluxCurve;
            return this;
        }

        public RBMKFuelRodSpec build() {
            return new RBMKFuelRodSpec(
                    reactivity,
                    selfRate,
                    burnFunction,
                    depletionFunction,
                    xenonGeneration,
                    xenonBurnDivisor,
                    heatPerFlux,
                    totalYield,
                    meltingPoint,
                    diffusion,
                    inputType,
                    outputType,
                    colorTint,
                    heatCoefficientStart,
                    heatCoefficientLength,
                    outputRatioCurve,
                    inputFluxCurve);
        }
    }
}
