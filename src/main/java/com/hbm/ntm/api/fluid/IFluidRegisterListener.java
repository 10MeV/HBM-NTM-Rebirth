package com.hbm.ntm.api.fluid;

/**
 * Legacy-name bridge for old fluid type registration callbacks. The modern
 * compat facade keeps the type for source compatibility but does not call it.
 */
@Deprecated(forRemoval = false)
@FunctionalInterface
public interface IFluidRegisterListener extends HbmFluidRegisterListener {
}
