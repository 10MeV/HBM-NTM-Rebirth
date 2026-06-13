package com.hbm.inventory.fluid.trait;

/**
 * Legacy package facade for the 1.7.10 fluid trait release-type enum.
 */
@Deprecated(forRemoval = false)
public final class FluidTrait {
    public enum FluidReleaseType {
        VOID,
        BURN,
        SPILL;

        public com.hbm.ntm.fluid.FluidReleaseType modern() {
            return switch (this) {
                case VOID -> com.hbm.ntm.fluid.FluidReleaseType.VOID;
                case BURN -> com.hbm.ntm.fluid.FluidReleaseType.BURN;
                case SPILL -> com.hbm.ntm.fluid.FluidReleaseType.SPILL;
            };
        }

        public static FluidReleaseType fromModern(com.hbm.ntm.fluid.FluidReleaseType type) {
            if (type == null) {
                return SPILL;
            }
            return switch (type) {
                case VOID -> VOID;
                case BURN -> BURN;
                case SPILL -> SPILL;
            };
        }
    }

    private FluidTrait() {
    }
}
