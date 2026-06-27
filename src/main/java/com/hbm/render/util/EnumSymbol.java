package com.hbm.render.util;

import com.hbm.ntm.fluid.FluidSymbol;

/**
 * Legacy danger-diamond symbol enum used by 1.7.10 fluid render and compat APIs.
 */
@Deprecated(forRemoval = false)
public enum EnumSymbol {
    NONE(0, 0, FluidSymbol.NONE),
    RADIATION(195, 2, FluidSymbol.RADIATION),
    NOWATER(195, 63, FluidSymbol.NOWATER),
    ACID(195, 124, FluidSymbol.ACID),
    ASPHYXIANT(195, 185, FluidSymbol.ASPHYXIANT),
    CROYGENIC(134, 185, FluidSymbol.CRYOGENIC),
    ANTIMATTER(73, 185, FluidSymbol.ANTIMATTER),
    OXIDIZER(12, 185, FluidSymbol.OXIDIZER);

    public final int x;
    public final int y;
    private final FluidSymbol modern;

    EnumSymbol(int x, int y, FluidSymbol modern) {
        this.x = x;
        this.y = y;
        this.modern = modern;
    }

    public FluidSymbol modern() {
        return modern;
    }

    public static EnumSymbol fromModern(FluidSymbol symbol) {
        if (symbol == null) {
            return NONE;
        }
        return switch (symbol) {
            case NONE -> NONE;
            case ACID -> ACID;
            case ASPHYXIANT -> ASPHYXIANT;
            case ANTIMATTER -> ANTIMATTER;
            case CRYOGENIC -> CROYGENIC;
            case NOWATER -> NOWATER;
            case OXIDIZER -> OXIDIZER;
            case RADIATION -> RADIATION;
        };
    }
}
