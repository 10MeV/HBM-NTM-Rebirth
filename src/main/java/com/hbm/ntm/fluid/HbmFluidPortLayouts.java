package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * 1.7.10 DirPos-style fluid port layout helpers.
 */
public final class HbmFluidPortLayouts {
    private HbmFluidPortLayouts() {
    }

    public static FluidPort adjacent(Direction direction) {
        Objects.requireNonNull(direction, "direction");
        return new FluidPort(new BlockPos(direction.getStepX(), direction.getStepY(), direction.getStepZ()), direction);
    }

    public static List<FluidPort> allAdjacent() {
        List<FluidPort> ports = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            ports.add(adjacent(direction));
        }
        return List.copyOf(ports);
    }

    public static List<FluidPort> horizontalAdjacent() {
        return List.of(
                adjacent(Direction.EAST),
                adjacent(Direction.WEST),
                adjacent(Direction.SOUTH),
                adjacent(Direction.NORTH));
    }

    public static List<FluidPort> cardinal(int radius) {
        return cardinal(radius, 0);
    }

    public static List<FluidPort> cardinal(int radius, int y) {
        return List.of(
                FluidPort.of(radius, y, 0, Direction.EAST),
                FluidPort.of(-radius, y, 0, Direction.WEST),
                FluidPort.of(0, y, radius, Direction.SOUTH),
                FluidPort.of(0, y, -radius, Direction.NORTH));
    }

    public static List<FluidPort> squareSidesWithoutCorners(int radius) {
        return squareSidesWithoutCorners(radius, 0);
    }

    public static List<FluidPort> squareSidesWithoutCorners(int radius, int y) {
        List<FluidPort> ports = new ArrayList<>();
        for (int z = -radius + 1; z <= radius - 1; z++) {
            ports.add(FluidPort.of(radius, y, z, Direction.EAST));
        }
        for (int z = -radius + 1; z <= radius - 1; z++) {
            ports.add(FluidPort.of(-radius, y, z, Direction.WEST));
        }
        for (int x = -radius + 1; x <= radius - 1; x++) {
            ports.add(FluidPort.of(x, y, radius, Direction.SOUTH));
        }
        for (int x = -radius + 1; x <= radius - 1; x++) {
            ports.add(FluidPort.of(x, y, -radius, Direction.NORTH));
        }
        return List.copyOf(ports);
    }

    public static List<FluidPort> fluidTank() {
        return List.of(
                FluidPort.of(2, 0, -1, Direction.EAST),
                FluidPort.of(2, 0, 1, Direction.EAST),
                FluidPort.of(-2, 0, -1, Direction.WEST),
                FluidPort.of(-2, 0, 1, Direction.WEST),
                FluidPort.of(-1, 0, 2, Direction.SOUTH),
                FluidPort.of(1, 0, 2, Direction.SOUTH),
                FluidPort.of(-1, 0, -2, Direction.NORTH),
                FluidPort.of(1, 0, -2, Direction.NORTH));
    }

    public static FluidPort legacy(Direction facing, int forward, int side, Direction portDirection) {
        return legacy(facing, forward, side, 0, portDirection);
    }

    public static FluidPort legacy(Direction facing, int forward, int side, int y, Direction portDirection) {
        return legacy(facing, LegacyMultiblockOffsets.legacyUpSide(facing), forward, side, y, portDirection);
    }

    public static FluidPort legacy(Direction facing, Direction sideAxis, int forward, int side, int y,
            Direction portDirection) {
        Objects.requireNonNull(portDirection, "portDirection");
        return new FluidPort(LegacyMultiblockOffsets.relative(facing, sideAxis, forward, side, y), portDirection);
    }

    public static List<FluidPort> legacy(Direction facing, LegacyPort... ports) {
        return legacy(facing, LegacyMultiblockOffsets.legacyUpSide(facing), ports);
    }

    public static List<FluidPort> legacy(Direction facing, Direction sideAxis, LegacyPort... ports) {
        Objects.requireNonNull(facing, "facing");
        Objects.requireNonNull(sideAxis, "sideAxis");
        if (ports == null || ports.length == 0) {
            return List.of();
        }
        List<FluidPort> result = new ArrayList<>();
        for (LegacyPort port : ports) {
            if (port != null) {
                result.add(legacy(facing, sideAxis, port.forward(), port.side(), port.y(), port.direction()));
            }
        }
        return List.copyOf(result);
    }

    public static List<FluidPort> withDirection(Iterable<BlockPos> offsets, Direction portDirection) {
        Objects.requireNonNull(portDirection, "portDirection");
        if (offsets == null) {
            return List.of();
        }
        List<FluidPort> ports = new ArrayList<>();
        for (BlockPos offset : offsets) {
            if (offset != null) {
                ports.add(new FluidPort(offset, portDirection));
            }
        }
        return List.copyOf(ports);
    }

    public record LegacyPort(int forward, int side, int y, Direction direction) {
        public LegacyPort {
            Objects.requireNonNull(direction, "direction");
        }

        public static LegacyPort of(int forward, int side, Direction direction) {
            return new LegacyPort(forward, side, 0, direction);
        }

        public static LegacyPort of(int forward, int side, int y, Direction direction) {
            return new LegacyPort(forward, side, y, direction);
        }
    }
}
