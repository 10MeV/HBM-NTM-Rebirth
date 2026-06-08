package com.hbm.ntm.neutron;

public class RBMKHeaterState {
    public static final int DEFAULT_TANK_MAX = 16_000;

    private int feedFill;
    private int feedMax = DEFAULT_TANK_MAX;
    private int outputFill;
    private int outputMax = DEFAULT_TANK_MAX;
    private boolean validHeatableFluid = true;

    public int feedFill() {
        return feedFill;
    }

    public void setFeedFill(int feedFill) {
        this.feedFill = clamp(feedFill, 0, feedMax);
    }

    public int feedMax() {
        return feedMax;
    }

    public void setFeedMax(int feedMax) {
        this.feedMax = Math.max(1, feedMax);
        setFeedFill(feedFill);
    }

    public int outputFill() {
        return outputFill;
    }

    public void setOutputFill(int outputFill) {
        this.outputFill = clamp(outputFill, 0, outputMax);
    }

    public int outputMax() {
        return outputMax;
    }

    public void setOutputMax(int outputMax) {
        this.outputMax = Math.max(1, outputMax);
        setOutputFill(outputFill);
    }

    public boolean validHeatableFluid() {
        return validHeatableFluid;
    }

    public void setValidHeatableFluid(boolean validHeatableFluid) {
        this.validHeatableFluid = validHeatableFluid;
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
