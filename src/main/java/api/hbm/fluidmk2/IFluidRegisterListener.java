package api.hbm.fluidmk2;

/**
 * Legacy 1.7.10 package bridge for old fluid type registration callbacks. The
 * modern compat facade keeps the import shape but does not invoke callbacks.
 */
@Deprecated(forRemoval = false)
@FunctionalInterface
public interface IFluidRegisterListener extends com.hbm.ntm.api.fluid.IFluidRegisterListener {
}
