package com.hbm.ntm.multiblock;

import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public final class LegacyMultiblockPorts {
    private LegacyMultiblockPorts() {
    }

    public static List<EnergyPort> xrFloorRingEnergyPorts(int radius) {
        return xrFloorRingPorts(radius, EnergyPort::of);
    }

    public static List<FluidPort> xrFloorRingFluidPorts(int radius) {
        return xrFloorRingPorts(radius, FluidPort::of);
    }

    private static <T> List<T> xrFloorRingPorts(int radius, PortFactory<T> factory) {
        List<T> ports = new ArrayList<>();
        for (int z = -radius + 1; z <= radius - 1; z++) {
            ports.add(factory.create(radius, 0, z, Direction.EAST));
        }
        for (int z = -radius + 1; z <= radius - 1; z++) {
            ports.add(factory.create(-radius, 0, z, Direction.WEST));
        }
        for (int x = -radius + 1; x <= radius - 1; x++) {
            ports.add(factory.create(x, 0, radius, Direction.SOUTH));
        }
        for (int x = -radius + 1; x <= radius - 1; x++) {
            ports.add(factory.create(x, 0, -radius, Direction.NORTH));
        }
        return List.copyOf(ports);
    }

    @FunctionalInterface
    private interface PortFactory<T> {
        T create(int x, int y, int z, Direction direction);
    }
}
