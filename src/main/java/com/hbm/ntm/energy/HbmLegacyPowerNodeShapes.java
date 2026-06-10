package com.hbm.ntm.energy;

import com.hbm.ntm.world.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 1.7.10 PowerNode shape helpers for pylon-like Energy MK2 nodes.
 */
public final class HbmLegacyPowerNodeShapes {
    public static final double PYLON_MAX_WIRE_LENGTH = 25.0D;
    public static final double MEDIUM_PYLON_MAX_WIRE_LENGTH = 45.0D;
    public static final double LARGE_PYLON_MAX_WIRE_LENGTH = 100.0D;
    public static final double CONNECTOR_MAX_WIRE_LENGTH = 10.0D;
    public static final double SUBSTATION_MAX_WIRE_LENGTH = 20.0D;

    private HbmLegacyPowerNodeShapes() {
    }

    public static HbmEnergyNode pylon(BlockPos pos, Iterable<BlockPos> remoteConnections) {
        Objects.requireNonNull(pos, "pos");
        List<DirPos> connections = basePylonConnections(pos, remoteConnections);
        connections.addAll(HbmEnergyConnectionUtil.standardLegacyConnectionPoints(pos));
        return HbmEnergyNode.withLegacyConnectionPoints(pos, connections);
    }

    public static HbmEnergyNode pylon(BlockPos pos, HbmLegacyWireConnections remoteConnections) {
        return pylon(pos, connected(remoteConnections));
    }

    public static HbmEnergyNode remoteOnlyPylon(BlockPos pos, Iterable<BlockPos> remoteConnections) {
        Objects.requireNonNull(pos, "pos");
        return HbmEnergyNode.withLegacyConnectionPoints(pos, basePylonConnections(pos, remoteConnections));
    }

    public static HbmEnergyNode remoteOnlyPylon(BlockPos pos, HbmLegacyWireConnections remoteConnections) {
        return remoteOnlyPylon(pos, connected(remoteConnections));
    }

    public static HbmEnergyNode mediumPylon(BlockPos pos, @Nullable Direction transformerSide,
            Iterable<BlockPos> remoteConnections) {
        Objects.requireNonNull(pos, "pos");
        List<DirPos> connections = basePylonConnections(pos, remoteConnections);
        if (transformerSide != null) {
            connections.add(new DirPos(pos.relative(transformerSide), transformerSide));
        }
        return HbmEnergyNode.withLegacyConnectionPoints(pos, connections);
    }

    public static HbmEnergyNode mediumPylon(BlockPos pos, @Nullable Direction transformerSide,
            HbmLegacyWireConnections remoteConnections) {
        return mediumPylon(pos, transformerSide, connected(remoteConnections));
    }

    public static HbmEnergyNode connector(BlockPos pos, @Nullable Direction connectorSide,
            Iterable<BlockPos> remoteConnections) {
        Objects.requireNonNull(pos, "pos");
        List<DirPos> connections = basePylonConnections(pos, remoteConnections);
        if (connectorSide != null) {
            connections.add(new DirPos(pos.relative(connectorSide), connectorSide));
        }
        return HbmEnergyNode.withLegacyConnectionPoints(pos, connections);
    }

    public static HbmEnergyNode connector(BlockPos pos, @Nullable Direction connectorSide,
            HbmLegacyWireConnections remoteConnections) {
        return connector(pos, connectorSide, connected(remoteConnections));
    }

    public static HbmEnergyNode substation(BlockPos pos, Iterable<BlockPos> remoteConnections) {
        Objects.requireNonNull(pos, "pos");
        Set<BlockPos> positions = new LinkedHashSet<>();
        positions.add(pos.immutable());
        positions.add(pos.offset(1, 0, 1).immutable());
        positions.add(pos.offset(1, 0, -1).immutable());
        positions.add(pos.offset(-1, 0, 1).immutable());
        positions.add(pos.offset(-1, 0, -1).immutable());

        List<DirPos> connections = basePylonConnections(pos, remoteConnections);
        connections.add(new DirPos(pos.offset(2, 0, -1), Direction.EAST));
        connections.add(new DirPos(pos.offset(2, 0, 1), Direction.EAST));
        connections.add(new DirPos(pos.offset(-2, 0, -1), Direction.WEST));
        connections.add(new DirPos(pos.offset(-2, 0, 1), Direction.WEST));
        connections.add(new DirPos(pos.offset(-1, 0, 2), Direction.SOUTH));
        connections.add(new DirPos(pos.offset(1, 0, 2), Direction.SOUTH));
        connections.add(new DirPos(pos.offset(-1, 0, -2), Direction.NORTH));
        connections.add(new DirPos(pos.offset(1, 0, -2), Direction.NORTH));
        return HbmEnergyNode.withLegacyConnectionPoints(positions, connections);
    }

    public static HbmEnergyNode substation(BlockPos pos, HbmLegacyWireConnections remoteConnections) {
        return substation(pos, connected(remoteConnections));
    }

    public static List<DirPos> basePylonConnections(BlockPos pos, Iterable<BlockPos> remoteConnections) {
        Objects.requireNonNull(pos, "pos");
        List<DirPos> connections = new ArrayList<>();
        connections.add(unknownPoint(pos));
        addRemoteUnknownConnections(connections, remoteConnections);
        return connections;
    }

    public static void addRemoteUnknownConnections(List<DirPos> connections, Iterable<BlockPos> remoteConnections) {
        if (connections == null || remoteConnections == null) {
            return;
        }
        for (BlockPos remote : remoteConnections) {
            if (remote != null) {
                connections.add(unknownPoint(remote));
            }
        }
    }

    public static DirPos unknownPoint(BlockPos pos) {
        Objects.requireNonNull(pos, "pos");
        return new DirPos(pos, null);
    }

    public static Vec3 pylonMount(BlockPos pos) {
        return absolute(pos, 0.5D, 5.4D, 0.5D);
    }

    public static List<Vec3> pylonMounts(BlockPos pos) {
        return List.of(pylonMount(pos));
    }

    public static Vec3 connectorMount(BlockPos pos) {
        return absolute(pos, 0.5D, 0.5D, 0.5D);
    }

    public static List<Vec3> connectorMounts(BlockPos pos) {
        return List.of(connectorMount(pos));
    }

    public static List<Vec3> mediumPylonMounts(BlockPos pos, Direction facing) {
        Direction direction = horizontalOrNorth(facing);
        double height = 7.5D;
        return List.of(
                absolute(pos, 0.5D, height, 0.5D),
                absolute(pos, 0.5D + direction.getStepX(), height, 0.5D + direction.getStepZ()),
                absolute(pos, 0.5D + direction.getStepX() * 2.0D, height,
                        0.5D + direction.getStepZ() * 2.0D));
    }

    public static List<Vec3> largePylonMounts(BlockPos pos, Direction facing) {
        double topOff = 0.75D + 0.0625D;
        double sideOff = 3.375D;
        Vec3 offset = rotateLargePylonOffset(horizontalOrNorth(facing), sideOff);
        return List.of(
                absolute(pos, 0.5D + offset.x, 11.5D + topOff, 0.5D + offset.z),
                absolute(pos, 0.5D + offset.x, 11.5D - topOff, 0.5D + offset.z),
                absolute(pos, 0.5D - offset.x, 11.5D + topOff, 0.5D - offset.z),
                absolute(pos, 0.5D - offset.x, 11.5D - topOff, 0.5D - offset.z));
    }

    public static List<Vec3> substationMounts(BlockPos pos, Direction facing) {
        double topOff = 5.25D;
        Vec3 axis = horizontalOrNorth(facing).getAxis() == Direction.Axis.X
                ? new Vec3(0.0D, 0.0D, -1.0D)
                : new Vec3(1.0D, 0.0D, 0.0D);
        return List.of(
                absolute(pos, 0.5D + axis.x * 0.5D, topOff, 0.5D + axis.z * 0.5D),
                absolute(pos, 0.5D + axis.x * 1.5D, topOff, 0.5D + axis.z * 1.5D),
                absolute(pos, 0.5D - axis.x * 0.5D, topOff, 0.5D - axis.z * 0.5D),
                absolute(pos, 0.5D - axis.x * 1.5D, topOff, 0.5D - axis.z * 1.5D));
    }

    public static Vec3 substationConnectionPoint(BlockPos pos) {
        return absolute(pos, 0.5D, 5.25D, 0.5D);
    }

    private static Vec3 absolute(BlockPos pos, double x, double y, double z) {
        Objects.requireNonNull(pos, "pos");
        return new Vec3(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
    }

    private static Direction horizontalOrNorth(@Nullable Direction direction) {
        return direction != null && direction.getAxis().isHorizontal() ? direction : Direction.NORTH;
    }

    private static Vec3 rotateLargePylonOffset(Direction facing, double sideOff) {
        double angle = switch (facing) {
            case WEST -> Math.PI * 0.25D;
            case SOUTH -> Math.PI * 0.5D;
            case EAST -> Math.PI * 0.75D;
            default -> 0.0D;
        };
        double x = sideOff * Math.cos(angle);
        double z = -sideOff * Math.sin(angle);
        return new Vec3(x, 0.0D, z);
    }

    private static Iterable<BlockPos> connected(HbmLegacyWireConnections connections) {
        return connections == null ? List.of() : connections.connected();
    }

    public static WireConnectionResult canConnectWire(
            WireConnectionType firstType,
            BlockPos firstPos,
            Vec3 firstConnectionPoint,
            double firstMaxWireLength,
            WireConnectionType secondType,
            BlockPos secondPos,
            Vec3 secondConnectionPoint,
            double secondMaxWireLength) {
        Objects.requireNonNull(firstType, "firstType");
        Objects.requireNonNull(secondType, "secondType");
        Objects.requireNonNull(firstPos, "firstPos");
        Objects.requireNonNull(secondPos, "secondPos");
        Objects.requireNonNull(firstConnectionPoint, "firstConnectionPoint");
        Objects.requireNonNull(secondConnectionPoint, "secondConnectionPoint");

        if (firstType != secondType) {
            return WireConnectionResult.TYPE_MISMATCH;
        }
        if (firstPos.equals(secondPos)) {
            return WireConnectionResult.SAME_NODE;
        }
        double maxLength = Math.min(firstMaxWireLength, secondMaxWireLength);
        return firstConnectionPoint.distanceTo(secondConnectionPoint) <= maxLength
                ? WireConnectionResult.OK
                : WireConnectionResult.TOO_FAR;
    }

    public enum WireConnectionType {
        SINGLE,
        TRIPLE,
        QUAD
    }

    public enum WireConnectionResult {
        OK(0),
        TYPE_MISMATCH(1),
        SAME_NODE(2),
        TOO_FAR(3);

        private final int legacyCode;

        WireConnectionResult(int legacyCode) {
            this.legacyCode = legacyCode;
        }

        public int legacyCode() {
            return legacyCode;
        }
    }
}
