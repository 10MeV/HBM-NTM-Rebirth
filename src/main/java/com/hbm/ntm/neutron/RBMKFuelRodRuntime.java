package com.hbm.ntm.neutron;

public final class RBMKFuelRodRuntime {
    private RBMKFuelRodRuntime() {
    }

    public static double burn(
            RBMKRuntimeSettings settings,
            RBMKFuelRodSpec spec,
            RBMKFuelRodState state,
            double inFlux) {
        inFlux += spec.selfRate();

        if (settings.xenonEnabled()) {
            double xenon = state.xenon();
            xenon -= xenonBurn(spec, inFlux);
            inFlux *= (1.0D - state.xenonLevel());
            xenon += xenonGenerated(spec, inFlux);
            state.setXenon(xenon);
        }

        double multiplier = heatCoefficientMultiplier(spec, state.coreHeat());
        double outFlux = reactivity(spec, inFlux, state.enrichment(spec) * multiplier)
                * settings.reactivityModifier();

        if (settings.depletionEnabled()) {
            state.setRemainingYield(state.remainingYield() - inFlux);
        }
        state.setCoreHeat(state.coreHeat() + outFlux * spec.heatPerFlux());
        return outFlux;
    }

    public static void updateHeat(
            RBMKRuntimeSettings settings,
            RBMKFuelRodSpec spec,
            RBMKFuelRodState state,
            double modifier) {
        double coreHeat = state.coreHeat();
        double hullHeat = state.hullHeat();
        if (coreHeat <= hullHeat) {
            return;
        }

        double mid = (coreHeat - hullHeat) / 2.0D;
        double transfer = mid * spec.diffusion() * settings.fuelDiffusionModifier() * modifier;
        state.setCoreHeat(coreHeat - transfer);
        state.setHullHeat(hullHeat + transfer);
    }

    public static double provideHeat(
            RBMKRuntimeSettings settings,
            RBMKFuelRodSpec spec,
            RBMKFuelRodState state,
            double columnHeat,
            double modifier) {
        double hullHeat = state.hullHeat();
        if (hullHeat > spec.meltingPoint()) {
            double average = (columnHeat + hullHeat + state.coreHeat()) / 3.0D;
            state.setCoreHeat(average);
            state.setHullHeat(average);
            return average - columnHeat;
        }

        if (hullHeat <= columnHeat) {
            return 0.0D;
        }

        double provided = (hullHeat - columnHeat) / 2.0D;
        provided *= settings.fuelHeatProvision() * modifier;
        state.setHullHeat(hullHeat - provided);
        return provided;
    }

    public static double reactivity(RBMKFuelRodSpec spec, double inFlux, double enrichment) {
        double flux = inFlux * reactivityModifierByEnrichment(spec.depletionFunction(), enrichment);
        return switch (spec.burnFunction()) {
            case PASSIVE -> spec.selfRate() * enrichment;
            case LOG_TEN -> Math.log10(flux + 1.0D) * 0.5D * spec.reactivity();
            case PLATEU -> (1.0D - Math.pow(Math.E, -flux / 25.0D)) * spec.reactivity();
            case ARCH -> Math.max((flux - (flux * flux / 10000.0D)) / 100.0D * spec.reactivity(), 0.0D);
            case SIGMOID -> spec.reactivity() / (1.0D + Math.pow(Math.E, -(flux - 50.0D) / 10.0D));
            case SQUARE_ROOT -> Math.sqrt(flux) * spec.reactivity() / 10.0D;
            case LINEAR -> flux / 100.0D * spec.reactivity();
            case QUADRATIC -> flux * flux / 10000.0D * spec.reactivity();
            case EXPERIMENTAL -> flux * (Math.sin(flux) + 1.0D) * spec.reactivity();
        };
    }

    public static double reactivityModifierByEnrichment(DepletionFunction function, double enrichment) {
        return switch (function) {
            case LINEAR -> enrichment;
            case STATIC -> 1.0D;
            case BOOSTED_SLOPE -> enrichment + Math.sin((enrichment - 1.0D) * (enrichment - 1.0D) * Math.PI);
            case RAISING_SLOPE -> enrichment + (Math.sin(enrichment * Math.PI) / 2.0D);
            case GENTLE_SLOPE -> enrichment + (Math.sin(enrichment * Math.PI) / 3.0D);
        };
    }

    public static double xenonGenerated(RBMKFuelRodSpec spec, double flux) {
        return flux * spec.xenonGeneration();
    }

    public static double xenonBurn(RBMKFuelRodSpec spec, double flux) {
        return (flux * flux) / spec.xenonBurnDivisor();
    }

    private static double heatCoefficientMultiplier(RBMKFuelRodSpec spec, double coreHeat) {
        if (spec.heatCoefficientStart() == 0.0D || spec.heatCoefficientLength() <= 0.0D
                || coreHeat < spec.heatCoefficientStart()) {
            return 1.0D;
        }
        double progress = (coreHeat - spec.heatCoefficientStart()) / spec.heatCoefficientLength();
        if (progress > 1.0D) {
            progress = 1.0D;
        }
        return Math.sin((progress * Math.PI + Math.PI) / 2.0D);
    }

    public enum BurnFunction {
        PASSIVE,
        LOG_TEN,
        PLATEU,
        ARCH,
        SIGMOID,
        SQUARE_ROOT,
        LINEAR,
        QUADRATIC,
        EXPERIMENTAL
    }

    public enum DepletionFunction {
        LINEAR,
        RAISING_SLOPE,
        BOOSTED_SLOPE,
        GENTLE_SLOPE,
        STATIC
    }
}
