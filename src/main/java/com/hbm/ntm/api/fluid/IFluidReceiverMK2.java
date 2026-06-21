package com.hbm.ntm.api.fluid;

import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidReceiver;
import com.hbm.ntm.fluid.HbmFluidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Legacy-name bridge for Fluid MK2 receivers.
 */
@Deprecated(forRemoval = false)
public interface IFluidReceiverMK2 extends IFluidUserMK2, HbmFluidReceiver {
    @Override
    long transferFluid(FluidType type, int pressure, long amount);

    @Override
    long getDemand(FluidType type, int pressure);

    @Override
    default long getReceiverSpeed(FluidType type, int pressure) {
        return HbmFluidReceiver.super.getReceiverSpeed(type, pressure);
    }

    @Override
    default int[] getReceivingPressureRange(FluidType type) {
        return IFluidUserMK2.DEFAULT_PRESSURE_RANGE;
    }

    default boolean trySubscribe(FluidType type, Level level, BlockPos connectorPos, Direction directionFromReceiver) {
        Direction connectorSide = directionFromReceiver == null ? null : directionFromReceiver.getOpposite();
        return HbmFluidUtil.subscribeReceiverToNetwork(level, connectorPos, connectorSide, type, this);
    }

    @Override
    default HbmEnergyReceiver.ConnectionPriority getFluidPriority() {
        return HbmFluidReceiver.super.getFluidPriority();
    }
}
