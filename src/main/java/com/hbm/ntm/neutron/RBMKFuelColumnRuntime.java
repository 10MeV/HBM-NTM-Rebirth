package com.hbm.ntm.neutron;

public final class RBMKFuelColumnRuntime {
    public static final double DEFAULT_COLUMN_MAX_HEAT = 1500.0D;
    public static final double COLUMN_HEAT_CAP = 10_000.0D;
    public static final double AUTOLOADER_SAFE_HULL_HEAT = 1_000.0D;
    public static final double MANUAL_SAFE_HULL_HEAT = 200.0D;

    private RBMKFuelColumnRuntime() {
    }

    public static FuelColumnTickResult tickFuelRod(
            RBMKRuntimeSettings settings,
            RBMKThermalState thermalState,
            RBMKRodFluxState fluxState,
            RBMKFuelRodSpec fuelSpec,
            RBMKFuelRodState fuelState,
            boolean hasLid,
            double columnMaxHeat) {
        double enrichment = fuelState.enrichment(fuelSpec);
        double outgoingFluxRatio = fuelSpec.outputRatio(fluxState.fluxFastRatio(), enrichment);
        double incomingFlux = fuelSpec.inputFlux(fluxState, enrichment);
        double outgoingFluxQuantity = RBMKFuelRodRuntime.burn(settings, fuelSpec, fuelState, incomingFlux);

        RBMKFuelRodRuntime.updateHeat(settings, fuelSpec, fuelState, 1.0D);
        thermalState.setHeat(thermalState.heat()
                + RBMKFuelRodRuntime.provideHeat(settings, fuelSpec, fuelState, thermalState.heat(), 1.0D));

        double leakRadiation = hasLid ? 0.0D : fluxState.fluxQuantity() * 0.05D;
        fluxState.setRodColor(fuelSpec.colorTint());

        if (thermalState.heat() > columnMaxHeat) {
            fluxState.resetFluxAfterRodOverheat();
            return new FuelColumnTickResult(
                    0.0D,
                    0.0D,
                    leakRadiation,
                    true,
                    settings.meltdownsDisabled(),
                    false);
        }

        if (thermalState.heat() > COLUMN_HEAT_CAP) {
            thermalState.setHeat(COLUMN_HEAT_CAP);
        }

        fluxState.commitActiveRodTick();
        return new FuelColumnTickResult(
                outgoingFluxQuantity,
                outgoingFluxRatio,
                leakRadiation,
                false,
                false,
                outgoingFluxQuantity != 0.0D);
    }

    public static FuelColumnTickResult tickFuelRod(
            RBMKRuntimeSettings settings,
            RBMKThermalState thermalState,
            RBMKRodFluxState fluxState,
            RBMKFuelRodSpec fuelSpec,
            RBMKFuelRodState fuelState,
            boolean hasLid) {
        return tickFuelRod(settings, thermalState, fluxState, fuelSpec, fuelState, hasLid, DEFAULT_COLUMN_MAX_HEAT);
    }

    public static void tickEmptyRodSlot(RBMKRodFluxState fluxState) {
        fluxState.clearRodTick();
    }

    public static boolean coldEnoughForAutoloader(RBMKFuelRodState fuelState) {
        return fuelState == null || fuelState.hullHeat() <= AUTOLOADER_SAFE_HULL_HEAT;
    }

    public static boolean coldEnoughForManual(RBMKFuelRodState fuelState) {
        return fuelState == null || fuelState.hullHeat() <= MANUAL_SAFE_HULL_HEAT;
    }

    public record FuelColumnTickResult(
            double outgoingFluxQuantity,
            double outgoingFluxRatio,
            double leakRadiation,
            boolean overheated,
            boolean meltdownSuppressed,
            boolean shouldSpreadFlux) {
    }
}
