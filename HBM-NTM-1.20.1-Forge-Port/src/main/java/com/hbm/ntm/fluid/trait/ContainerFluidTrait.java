package com.hbm.ntm.fluid.trait;

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
}
