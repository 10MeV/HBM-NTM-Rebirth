package com.hbm.ntm.api.entity;

import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class RadarEnergyConnectionProfile {
    private RadarEnergyConnectionProfile() {
    }

    public static HbmEnergySideMode sideMode(@Nullable Direction side) {
        return side == null || side.getAxis().isHorizontal() ? HbmEnergySideMode.INPUT : HbmEnergySideMode.NONE;
    }

    public static Iterable<Direction> directConnectionSides(int energyPortDistance) {
        return energyPortDistance <= 1 ? Direction.Plane.HORIZONTAL : List.of();
    }

    public static Iterable<EnergyPort> energyPorts(int energyPortDistance) {
        if (energyPortDistance <= 1) {
            return List.of();
        }
        return List.of(
                port(Direction.EAST, energyPortDistance),
                port(Direction.WEST, energyPortDistance),
                port(Direction.SOUTH, energyPortDistance),
                port(Direction.NORTH, energyPortDistance));
    }

    private static EnergyPort port(Direction direction, int distance) {
        return new EnergyPort(new BlockPos(
                direction.getStepX() * distance,
                0,
                direction.getStepZ() * distance), direction);
    }
}
