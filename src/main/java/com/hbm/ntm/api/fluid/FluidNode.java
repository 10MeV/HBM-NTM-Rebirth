package com.hbm.ntm.api.fluid;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.uninos.HbmNetworkNode.NodeConnection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Legacy-name facade for Fluid MK2 network nodes.
 */
@Deprecated(forRemoval = false)
public class FluidNode extends HbmFluidNode {
    private Set<Direction> mutableConnections;
    private Set<NodeConnection> mutableConnectionPoints;

    public FluidNode(FluidType type, BlockPos... positions) {
        this(type, positions(positions), EnumSet.allOf(Direction.class));
    }

    public FluidNode(FluidType type, com.hbm.ntm.util.fauxpointtwelve.BlockPos... positions) {
        this(type, legacyPositions(positions), EnumSet.allOf(Direction.class));
    }

    public FluidNode(FluidType type, Set<BlockPos> positions, Set<Direction> connections) {
        super(positions(positions), type, connections(connections));
    }

    public FluidNode(FluidType type, BlockPos position, Set<Direction> connections) {
        this(type, positions(position), connections);
    }

    public FluidNode setConnections(Direction... connections) {
        mutableConnections = directions(connections);
        mutableConnectionPoints = standardConnectionPoints(getPos(), mutableConnections);
        markRecentlyChanged();
        return this;
    }

    public FluidNode setConnections(com.hbm.ntm.util.fauxpointtwelve.DirPos... connections) {
        mutableConnectionPoints = connectionPoints(connections);
        mutableConnections = directionsFromConnectionPoints(mutableConnectionPoints);
        markRecentlyChanged();
        return this;
    }

    public FluidNode setConnections(com.hbm.ntm.world.DirPos... connections) {
        mutableConnectionPoints = worldConnectionPoints(connections);
        mutableConnections = directionsFromConnectionPoints(mutableConnectionPoints);
        markRecentlyChanged();
        return this;
    }

    /** Compatibility equivalent of 1.7.10 {@code GenNode#setStandardConnections}. */
    public FluidNode setStandardConnections(int xCoord, int yCoord, int zCoord) {
        return setConnections(
                new com.hbm.ntm.util.fauxpointtwelve.DirPos(xCoord + 1, yCoord, zCoord, Direction.EAST),
                new com.hbm.ntm.util.fauxpointtwelve.DirPos(xCoord - 1, yCoord, zCoord, Direction.WEST),
                new com.hbm.ntm.util.fauxpointtwelve.DirPos(xCoord, yCoord + 1, zCoord, Direction.UP),
                new com.hbm.ntm.util.fauxpointtwelve.DirPos(xCoord, yCoord - 1, zCoord, Direction.DOWN),
                new com.hbm.ntm.util.fauxpointtwelve.DirPos(xCoord, yCoord, zCoord + 1, Direction.SOUTH),
                new com.hbm.ntm.util.fauxpointtwelve.DirPos(xCoord, yCoord, zCoord - 1, Direction.NORTH));
    }

    public FluidNode addConnection(com.hbm.ntm.util.fauxpointtwelve.DirPos connection) {
        if (connection != null) {
            mutableConnectionPoints = appendConnectionPoint(getConnectionPoints(), connectionPoint(connection));
            mutableConnections = directionsFromConnectionPoints(mutableConnectionPoints);
            markRecentlyChanged();
        }
        return this;
    }

    public FluidNode addConnection(com.hbm.ntm.world.DirPos connection) {
        if (connection != null) {
            mutableConnectionPoints = appendConnectionPoint(getConnectionPoints(), worldConnectionPoint(connection));
            mutableConnections = directionsFromConnectionPoints(mutableConnectionPoints);
            markRecentlyChanged();
        }
        return this;
    }

    @Override
    public Set<Direction> getConnections() {
        return mutableConnections == null ? super.getConnections() : Set.copyOf(mutableConnections);
    }

    @Override
    public Set<NodeConnection> getConnectionPoints() {
        return mutableConnectionPoints == null ? super.getConnectionPoints() : Set.copyOf(mutableConnectionPoints);
    }

    private static Set<BlockPos> positions(Set<BlockPos> positions) {
        if (positions == null || positions.isEmpty()) {
            return Set.of(BlockPos.ZERO);
        }
        LinkedHashSet<BlockPos> result = new LinkedHashSet<>();
        for (BlockPos pos : positions) {
            if (pos != null) {
                result.add(pos.immutable());
            }
        }
        return result.isEmpty() ? Set.of(BlockPos.ZERO) : result;
    }

    private static Set<BlockPos> positions(BlockPos... positions) {
        if (positions == null || positions.length == 0) {
            return Set.of(BlockPos.ZERO);
        }
        LinkedHashSet<BlockPos> result = new LinkedHashSet<>();
        for (BlockPos pos : positions) {
            if (pos != null) {
                result.add(pos.immutable());
            }
        }
        return result.isEmpty() ? Set.of(BlockPos.ZERO) : result;
    }

    private static Set<BlockPos> legacyPositions(com.hbm.ntm.util.fauxpointtwelve.BlockPos... positions) {
        if (positions == null || positions.length == 0) {
            return Set.of(BlockPos.ZERO);
        }
        LinkedHashSet<BlockPos> result = new LinkedHashSet<>();
        for (com.hbm.ntm.util.fauxpointtwelve.BlockPos pos : positions) {
            if (pos != null) {
                result.add(pos.immutable());
            }
        }
        return result.isEmpty() ? Set.of(BlockPos.ZERO) : result;
    }

    private static Set<Direction> connections(Set<Direction> connections) {
        if (connections == null || connections.isEmpty()) {
            return EnumSet.noneOf(Direction.class);
        }
        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);
        for (Direction direction : connections) {
            if (direction != null) {
                result.add(direction);
            }
        }
        return result;
    }

    private static Set<Direction> directions(Direction... connections) {
        if (connections == null || connections.length == 0) {
            return EnumSet.noneOf(Direction.class);
        }
        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);
        for (Direction direction : connections) {
            if (direction != null) {
                result.add(direction);
            }
        }
        return result;
    }

    private static Set<NodeConnection> standardConnectionPoints(BlockPos pos, Set<Direction> directions) {
        if (pos == null || directions == null || directions.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<NodeConnection> result = new LinkedHashSet<>();
        for (Direction direction : directions) {
            if (direction != null) {
                result.add(new NodeConnection(pos.relative(direction), direction));
            }
        }
        return Set.copyOf(result);
    }

    private static Set<NodeConnection> connectionPoints(com.hbm.ntm.util.fauxpointtwelve.DirPos... connections) {
        if (connections == null || connections.length == 0) {
            return Set.of();
        }
        LinkedHashSet<NodeConnection> result = new LinkedHashSet<>();
        for (com.hbm.ntm.util.fauxpointtwelve.DirPos pos : connections) {
            if (pos != null) {
                result.add(connectionPoint(pos));
            }
        }
        return Set.copyOf(result);
    }

    private static NodeConnection connectionPoint(com.hbm.ntm.util.fauxpointtwelve.DirPos pos) {
        return pos.getDir() == null ? NodeConnection.point(pos.immutable()) : new NodeConnection(pos.immutable(), pos.getDir());
    }

    private static Set<Direction> directions(com.hbm.ntm.util.fauxpointtwelve.DirPos... connections) {
        if (connections == null || connections.length == 0) {
            return EnumSet.noneOf(Direction.class);
        }
        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);
        for (com.hbm.ntm.util.fauxpointtwelve.DirPos pos : connections) {
            if (pos != null && pos.getDir() != null) {
                result.add(pos.getDir());
            }
        }
        return result;
    }

    private static Set<NodeConnection> worldConnectionPoints(com.hbm.ntm.world.DirPos... connections) {
        if (connections == null || connections.length == 0) {
            return Set.of();
        }
        LinkedHashSet<NodeConnection> result = new LinkedHashSet<>();
        for (com.hbm.ntm.world.DirPos pos : connections) {
            if (pos != null) {
                result.add(worldConnectionPoint(pos));
            }
        }
        return Set.copyOf(result);
    }

    private static NodeConnection worldConnectionPoint(com.hbm.ntm.world.DirPos pos) {
        return pos.getDir() == null ? NodeConnection.point(pos.immutable()) : new NodeConnection(pos.immutable(), pos.getDir());
    }

    private static Set<NodeConnection> appendConnectionPoint(Set<NodeConnection> existing, NodeConnection connection) {
        LinkedHashSet<NodeConnection> result = new LinkedHashSet<>();
        if (existing != null) {
            result.addAll(existing);
        }
        if (connection != null) {
            result.add(connection);
        }
        return Set.copyOf(result);
    }

    private static Set<Direction> directionsFromConnectionPoints(Set<NodeConnection> connections) {
        if (connections == null || connections.isEmpty()) {
            return EnumSet.noneOf(Direction.class);
        }
        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);
        for (NodeConnection connection : connections) {
            if (connection != null && !connection.direct() && connection.direction() != null) {
                result.add(connection.direction());
            }
        }
        return result;
    }
}
