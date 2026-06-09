package com.hbm.ntm.energy;

import com.hbm.ntm.world.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Legacy-name bridge for Energy MK2 receivers.
 */
@Deprecated(forRemoval = false)
public interface IEnergyReceiverMK2 extends IEnergyHandlerMK2, HbmEnergyReceiver {
    @Override
    default long transferPower(long power) {
        if (power + getPower() <= getMaxPower()) {
            setPower(power + getPower());
            return 0L;
        }
        long capacity = getMaxPower() - getPower();
        long overshoot = power - capacity;
        setPower(getMaxPower());
        return overshoot;
    }

    default boolean trySubscribe(Level level, BlockPos conductorPos, Direction directionFromReceiver) {
        Direction conductorSide = directionFromReceiver == null ? null : directionFromReceiver.getOpposite();
        return HbmEnergyUtil.subscribeReceiverToNetwork(level, conductorPos, conductorSide, this);
    }

    default boolean trySubscribe(Level level, DirPos conductorPort) {
        return HbmEnergyUtil.subscribeReceiverToPort(level, conductorPort, this);
    }

    default boolean trySubscribe(Level level, int x, int y, int z, Direction directionFromReceiver) {
        return trySubscribe(level, new BlockPos(x, y, z), directionFromReceiver);
    }

    default boolean tryUnsubscribe(Level level, BlockPos conductorPos, Direction directionFromReceiver) {
        Direction conductorSide = directionFromReceiver == null ? null : directionFromReceiver.getOpposite();
        return HbmEnergyUtil.unsubscribeReceiverFromNetwork(level, conductorPos, conductorSide, this);
    }

    default boolean tryUnsubscribe(Level level, DirPos conductorPort) {
        return HbmEnergyUtil.unsubscribeReceiverFromPort(level, conductorPort, this);
    }

    default boolean tryUnsubscribe(Level level, BlockPos conductorPos) {
        return HbmEnergyUtil.unsubscribeReceiverFromNetwork(level, conductorPos, this);
    }

    default boolean tryUnsubscribe(Level level, int x, int y, int z) {
        return tryUnsubscribe(level, new BlockPos(x, y, z));
    }
}
