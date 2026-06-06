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

    public static List<EnergyPort> factoryRecipeEnergyPorts(Direction facing, boolean topChemicalPorts) {
        return factoryMachinePorts(facing, topChemicalPorts, EnergyPort::of);
    }

    public static List<FluidPort> factoryRecipeFluidPorts(Direction facing, boolean topChemicalPorts) {
        return factoryMachinePorts(facing, topChemicalPorts, FluidPort::of);
    }

    public static List<EnergyPort> factoryCoolingEnergyPorts(Direction facing) {
        return factoryCoolingPorts(facing, EnergyPort::of);
    }

    public static List<FluidPort> factoryCoolingFluidPorts(Direction facing) {
        return factoryCoolingPorts(facing, FluidPort::of);
    }

    public static List<EnergyPort> combineEnergyPorts(Iterable<EnergyPort> first, Iterable<EnergyPort> second) {
        return combinePorts(first, second);
    }

    public static List<FluidPort> combineFluidPorts(Iterable<FluidPort> first, Iterable<FluidPort> second) {
        return combinePorts(first, second);
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

    private static <T> List<T> factoryMachinePorts(Direction facing, boolean topChemicalPorts, PortFactory<T> factory) {
        Direction rot = facing.getClockWise();
        List<T> ports = new ArrayList<>();
        ports.addAll(factoryFloorPorts(factory));
        if (topChemicalPorts) {
            ports.addAll(factoryTopChemicalPorts(facing, rot, factory));
        }
        ports.addAll(factoryRecipeIoPorts(facing, rot, factory));
        return List.copyOf(ports);
    }

    private static <T> List<T> factoryFloorPorts(PortFactory<T> factory) {
        List<T> ports = new ArrayList<>();
        for (int z = -2; z <= 2; z += 2) {
            ports.add(factory.create(3, 0, z, Direction.EAST));
        }
        for (int z = -2; z <= 2; z += 2) {
            ports.add(factory.create(-3, 0, z, Direction.WEST));
        }
        for (int x = -2; x <= 2; x += 2) {
            ports.add(factory.create(x, 0, 3, Direction.SOUTH));
        }
        for (int x = -2; x <= 2; x += 2) {
            ports.add(factory.create(x, 0, -3, Direction.NORTH));
        }
        return ports;
    }

    private static <T> List<T> factoryTopChemicalPorts(Direction facing, Direction rot, PortFactory<T> factory) {
        List<T> ports = new ArrayList<>();
        for (int forward = 2; forward >= -2; forward--) {
            ports.add(factory.create(
                    facing.getStepX() * forward + rot.getStepX() * 2,
                    3,
                    facing.getStepZ() * forward + rot.getStepZ() * 2,
                    Direction.UP));
        }
        for (int forward = 2; forward >= -2; forward--) {
            ports.add(factory.create(
                    facing.getStepX() * forward - rot.getStepX() * 2,
                    3,
                    facing.getStepZ() * forward - rot.getStepZ() * 2,
                    Direction.UP));
        }
        return ports;
    }

    private static <T> List<T> factoryRecipeIoPorts(Direction facing, Direction rot, PortFactory<T> factory) {
        return List.of(
                factory.create(facing.getStepX() + rot.getStepX() * 3, 0,
                        facing.getStepZ() + rot.getStepZ() * 3, rot),
                factory.create(-facing.getStepX() + rot.getStepX() * 3, 0,
                        -facing.getStepZ() + rot.getStepZ() * 3, rot),
                factory.create(facing.getStepX() - rot.getStepX() * 3, 0,
                        facing.getStepZ() - rot.getStepZ() * 3, rot.getOpposite()),
                factory.create(-facing.getStepX() - rot.getStepX() * 3, 0,
                        -facing.getStepZ() - rot.getStepZ() * 3, rot.getOpposite()));
    }

    private static <T> List<T> factoryCoolingPorts(Direction facing, PortFactory<T> factory) {
        Direction rot = facing.getClockWise();
        return List.of(
                factory.create(rot.getStepX() + facing.getStepX() * 3, 0,
                        rot.getStepZ() + facing.getStepZ() * 3, facing),
                factory.create(-rot.getStepX() + facing.getStepX() * 3, 0,
                        -rot.getStepZ() + facing.getStepZ() * 3, facing),
                factory.create(rot.getStepX() - facing.getStepX() * 3, 0,
                        rot.getStepZ() - facing.getStepZ() * 3, facing.getOpposite()),
                factory.create(-rot.getStepX() - facing.getStepX() * 3, 0,
                        -rot.getStepZ() - facing.getStepZ() * 3, facing.getOpposite()));
    }

    private static <T> List<T> combinePorts(Iterable<T> first, Iterable<T> second) {
        List<T> ports = new ArrayList<>();
        for (T port : first) {
            ports.add(port);
        }
        for (T port : second) {
            ports.add(port);
        }
        return List.copyOf(ports);
    }

    @FunctionalInterface
    private interface PortFactory<T> {
        T create(int x, int y, int z, Direction direction);
    }
}
