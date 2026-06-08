package com.hbm.ntm.fluid.trait;

import com.google.gson.JsonObject;

public class ContainerFluidTrait extends FluidTrait {
    private Integer canisterColor;
    private Integer gasTankBottleColor;
    private Integer gasTankLabelColor;

    public ContainerFluidTrait withCanister(int color) {
        this.canisterColor = color;
        return this;
    }

    public ContainerFluidTrait withGasTank(int bottleColor, int labelColor) {
        this.gasTankBottleColor = bottleColor;
        this.gasTankLabelColor = labelColor;
        return this;
    }

    public boolean hasCanister() {
        return canisterColor != null;
    }

    public int getCanisterColor() {
        return canisterColor == null ? 0xFFFFFF : canisterColor;
    }

    public boolean hasGasTank() {
        return gasTankBottleColor != null && gasTankLabelColor != null;
    }

    public int getGasTankBottleColor() {
        return gasTankBottleColor == null ? 0xFFFFFF : gasTankBottleColor;
    }

    public int getGasTankLabelColor() {
        return gasTankLabelColor == null ? 0xFFFFFF : gasTankLabelColor;
    }

    public int getGasTankBottleColorOr(int fallback) {
        return gasTankBottleColor == null ? fallback : gasTankBottleColor;
    }

    public int getGasTankLabelColorOr(int fallback) {
        return gasTankLabelColor == null ? fallback : gasTankLabelColor;
    }

    @Override
    public void writeJson(JsonObject object) {
        if (canisterColor != null) {
            object.addProperty("canisterColor", canisterColor);
        }
        if (gasTankBottleColor != null && gasTankLabelColor != null) {
            object.addProperty("gasTankBottleColor", gasTankBottleColor);
            object.addProperty("gasTankLabelColor", gasTankLabelColor);
        }
    }
}
