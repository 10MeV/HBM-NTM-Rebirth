package com.hbm.ntm.neutron;

import net.minecraft.nbt.CompoundTag;

public class RBMKOutgasserState {
    public static final String TAG_PROGRESS = "progress";
    public static final int DURATION = 10_000;

    private double progress;

    public double progress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = Math.max(0.0D, progress);
    }

    public void tick(boolean canProcess) {
        if (!canProcess) {
            progress = 0.0D;
        }
    }

    public OutgasserFluxResult receiveFlux(
            RBMKNeutronHandler.RBMKNeutronStream stream,
            RBMKRuntimeSettings settings,
            boolean canProcess) {
        if (!canProcess) {
            progress = 0.0D;
            return new OutgasserFluxResult(0.0D, false);
        }

        double efficiency = Math.min(1.0D - stream.getFluxRatio() * 0.8D, 1.0D);
        double addedProgress = stream.getFluxQuantity() * efficiency * settings.outgasserSpeedModifier();
        progress += addedProgress;
        boolean shouldProcess = progress > DURATION;
        if (shouldProcess) {
            progress = 0.0D;
        }
        return new OutgasserFluxResult(addedProgress, shouldProcess);
    }

    public void load(CompoundTag tag) {
        setProgress(tag.getDouble(TAG_PROGRESS));
    }

    public void save(CompoundTag tag) {
        tag.putDouble(TAG_PROGRESS, progress);
    }

    public record OutgasserFluxResult(double addedProgress, boolean shouldProcessRecipe) {
    }
}
