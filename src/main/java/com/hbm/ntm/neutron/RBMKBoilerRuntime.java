package com.hbm.ntm.neutron;

public final class RBMKBoilerRuntime {
    private RBMKBoilerRuntime() {
    }

    public static BoilerTickResult tickBoiler(
            RBMKRuntimeSettings settings,
            RBMKThermalState thermalState,
            RBMKBoilerState boilerState) {
        boilerState.setConsumption(0);
        boilerState.setOutput(0);
        if (boilerState.ventDelay() > 0) {
            boilerState.setVentDelay(boilerState.ventDelay() - 1);
        }

        SteamGrade grade = boilerState.steamGrade();
        double heatProvided = thermalState.heat() - grade.minimumHeat();
        if (heatProvided <= 0.0D || settings.boilerHeatConsumption() <= 0.0D) {
            return new BoilerTickResult(0, 0, false);
        }

        int waterUsed;
        int steamProduced;
        if (grade == SteamGrade.ULTRAHOTSTEAM) {
            steamProduced = (int) Math.floor((heatProvided / settings.boilerHeatConsumption())
                    * 100.0D / grade.productionFactor());
            waterUsed = (int) Math.floor(steamProduced / 100.0D * grade.productionFactor());
            if (boilerState.feedFill() < waterUsed) {
                steamProduced = (int) Math.floor(boilerState.feedFill() * 100.0D / grade.productionFactor());
                waterUsed = (int) Math.floor(steamProduced / 100.0D * grade.productionFactor());
            }
        } else {
            waterUsed = (int) Math.floor(heatProvided / settings.boilerHeatConsumption());
            waterUsed = Math.min(waterUsed, boilerState.feedFill());
            steamProduced = (int) Math.floor((waterUsed * 100.0D) / grade.productionFactor());
        }

        boilerState.setConsumption(waterUsed);
        boilerState.setOutput(steamProduced);
        boilerState.setFeedFill(boilerState.feedFill() - waterUsed);
        boilerState.setSteamFill(boilerState.steamFill() + steamProduced);

        boolean vented = false;
        if (boilerState.steamFill() >= boilerState.steamMax()) {
            boilerState.setSteamFill(boilerState.steamMax());
            if (boilerState.ventDelay() <= 0) {
                boilerState.setVentDelay(20);
                vented = true;
            }
        }

        thermalState.setHeat(thermalState.heat() - waterUsed * settings.boilerHeatConsumption());
        return new BoilerTickResult(waterUsed, steamProduced, vented);
    }

    public static void cycleCompressor(RBMKBoilerState state) {
        switch (state.steamGrade()) {
            case STEAM -> {
                state.setSteamGrade(SteamGrade.HOTSTEAM);
                state.setSteamFill(state.steamFill() / 10);
            }
            case HOTSTEAM -> {
                state.setSteamGrade(SteamGrade.SUPERHOTSTEAM);
                state.setSteamFill(state.steamFill() / 10);
            }
            case SUPERHOTSTEAM -> {
                state.setSteamGrade(SteamGrade.ULTRAHOTSTEAM);
                state.setSteamFill(state.steamFill() / 10);
            }
            case ULTRAHOTSTEAM -> {
                state.setSteamGrade(SteamGrade.STEAM);
                state.setSteamFill(Math.min(state.steamFill() * 1000, state.steamMax()));
            }
        }
    }

    public enum SteamGrade {
        STEAM(100.0D, 1.0D),
        HOTSTEAM(300.0D, 10.0D),
        SUPERHOTSTEAM(450.0D, 100.0D),
        ULTRAHOTSTEAM(600.0D, 1000.0D);

        private final double minimumHeat;
        private final double productionFactor;

        SteamGrade(double minimumHeat, double productionFactor) {
            this.minimumHeat = minimumHeat;
            this.productionFactor = productionFactor;
        }

        public double minimumHeat() {
            return minimumHeat;
        }

        public double productionFactor() {
            return productionFactor;
        }
    }

    public record BoilerTickResult(int waterUsed, int steamProduced, boolean vented) {
    }
}
