package api.hbm.fluidmk2;

import com.hbm.ntm.fluid.FluidType;

/**
 * Legacy 1.7.10 package bridge for the Fluid MK2 network facade.
 */
@Deprecated(forRemoval = false)
public class FluidNetMK2 extends com.hbm.ntm.api.fluid.FluidNetMK2 {
    public FluidNetMK2(FluidType type) {
        super(type);
    }

    public FluidNetMK2(FluidType type, long timeoutMs) {
        super(type, timeoutMs);
    }
}
