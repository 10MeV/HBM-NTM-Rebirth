package com.hbm.ntm.neutron;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class PileFuelRuntime {
    public static final int FUEL_MAX_HEAT = 1000;
    public static final int FUEL_MAX_PROGRESS = 50_000;
    public static final int BREEDING_FUEL_MAX_PROGRESS = 30_000;
    public static final int SOURCE_PROGRESS_MARGIN = 1_000;

    private PileFuelRuntime() {
    }

    public static FuelTickResult tickFuel(PileFuelState state, boolean windscale, boolean source, RandomSource random) {
        dissipateHeat(state, windscale);

        int lastProgress = state.progress();
        int reaction = fuelReaction(state);
        boolean redstoneChanged = progressRedstone(lastProgress, FUEL_MAX_PROGRESS)
                != progressRedstone(state.progress(), FUEL_MAX_PROGRESS);

        boolean markSource = transmuteFuel(state, source);
        boolean transmuteToProduct = state.progress() >= FUEL_MAX_PROGRESS;
        boolean explode = state.heat() >= FUEL_MAX_HEAT;
        boolean smoke = random != null && random.nextFloat() * 2.0F <= state.heat() / (float) FUEL_MAX_HEAT;
        int raysToCast = reaction > 0 ? 12 : 0;
        int rayFlux = reaction > 0 ? (int) Math.max(reaction * 0.25D, 1.0D) : 0;

        return new FuelTickResult(
                reaction,
                raysToCast,
                rayFlux,
                redstoneChanged,
                markSource,
                transmuteToProduct,
                explode,
                smoke);
    }

    public static BreedingFuelTickResult tickBreedingFuel(PileFuelState state) {
        state.setLastNeutrons(state.neutrons());
        state.setProgress(state.progress() + state.neutrons());
        state.setNeutrons(0);
        int raysToCast = state.lastNeutrons() > 0 ? 2 : 0;
        return new BreedingFuelTickResult(
                state.lastNeutrons(),
                raysToCast,
                raysToCast > 0 ? 1 : 0,
                state.progress() >= BREEDING_FUEL_MAX_PROGRESS);
    }

    public static int progressRedstone(int progress, int maxProgress) {
        if (maxProgress <= 0) {
            return 0;
        }
        return Mth.clamp((progress * 16) / maxProgress, 0, 15);
    }

    private static void dissipateHeat(PileFuelState state, boolean windscale) {
        double rate = windscale ? 0.065D : 0.05D;
        state.setHeat((int) (state.heat() - state.heat() * rate));
    }

    private static int fuelReaction(PileFuelState state) {
        int reaction = (int) (state.neutrons()
                * (1.0D - (state.heat() / (double) FUEL_MAX_HEAT) * 0.5D));
        state.setLastNeutrons(state.neutrons());
        state.setNeutrons(0);
        state.setProgress(state.progress() + reaction);
        if (reaction > 0) {
            state.setHeat(state.heat() + reaction);
        }
        return reaction;
    }

    private static boolean transmuteFuel(PileFuelState state, boolean source) {
        int sourceThreshold = FUEL_MAX_PROGRESS - SOURCE_PROGRESS_MARGIN;
        if (source) {
            if (state.progress() < sourceThreshold) {
                state.setProgress(sourceThreshold);
            }
            return false;
        }
        return state.progress() >= sourceThreshold;
    }

    public record FuelTickResult(
            int reaction,
            int raysToCast,
            int rayFlux,
            boolean redstoneChanged,
            boolean markSource,
            boolean transmuteToProduct,
            boolean explode,
            boolean smoke) {
    }

    public record BreedingFuelTickResult(
            int absorbedNeutrons,
            int raysToCast,
            int rayFlux,
            boolean transmuteToProduct) {
    }
}
