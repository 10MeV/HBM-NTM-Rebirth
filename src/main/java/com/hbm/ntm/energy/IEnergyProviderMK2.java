package com.hbm.ntm.energy;

import com.hbm.ntm.world.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Legacy-name bridge for Energy MK2 providers.
 */
@Deprecated(forRemoval = false)
public interface IEnergyProviderMK2 extends IEnergyHandlerMK2, HbmEnergyProvider {
    /**
     * Legacy default has no sanity checking. Callers are expected to request no
     * more power than is available and the raw subtraction is externally visible.
     */
    @Override
    default long usePower(long power) {
        setPower(getPower() - power);
        return power;
    }

    default boolean tryProvide(Level level, BlockPos conductorPos, Direction directionFromProvider) {
        if (level == null || conductorPos == null || directionFromProvider == null) {
            return false;
        }
        return tryProvide(level, new DirPos(conductorPos, directionFromProvider));
    }

    default boolean tryProvide(Level level, DirPos conductorPort) {
        return HbmEnergyUtil.tryProvideToPort(level, conductorPort, this);
    }

    default boolean tryProvide(Level level, int x, int y, int z, Direction directionFromProvider) {
        return tryProvide(level, new DirPos(x, y, z, directionFromProvider));
    }
}
