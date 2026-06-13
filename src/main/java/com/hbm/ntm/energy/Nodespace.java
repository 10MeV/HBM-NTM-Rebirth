package com.hbm.ntm.energy;

import com.hbm.ntm.util.fauxpointtwelve.DirPos;
import com.hbm.ntm.uninos.HbmNetworkNode.NodeConnection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Legacy-name facade for Energy MK2 nodespace access.
 */
@Deprecated(forRemoval = false)
public final class Nodespace {
    public static HbmEnergyNode getNode(Level level, int x, int y, int z) {
        return HbmEnergyNodespace.getNode(level, new BlockPos(x, y, z));
    }

    public static HbmEnergyNode getNode(Level level, BlockPos pos) {
        return HbmEnergyNodespace.getNode(level, pos);
    }

    public static HbmEnergyNode createNode(Level level, HbmEnergyNode node) {
        return HbmEnergyNodespace.createNode(level, node);
    }

    public static void destroyNode(Level level, int x, int y, int z) {
        HbmEnergyNodespace.destroyNode(level, new BlockPos(x, y, z));
    }

    public static void destroyNode(Level level, BlockPos pos) {
        HbmEnergyNodespace.destroyNode(level, pos);
    }

    public static void destroyNode(Level level, HbmEnergyNode node) {
        HbmEnergyNodespace.destroyNode(level, node);
    }

    public static class PowerNode extends HbmEnergyNode {
        private Set<Direction> mutableConnections;
        private Set<NodeConnection> mutableConnectionPoints;

        public PowerNode(BlockPos... positions) {
            this(positions(positions), EnumSet.noneOf(Direction.class));
        }

        public PowerNode(com.hbm.ntm.util.fauxpointtwelve.BlockPos... positions) {
            this(legacyPositions(positions), EnumSet.noneOf(Direction.class));
        }

        public PowerNode(Set<BlockPos> positions, Set<Direction> connections) {
            super(positions(positions), connections(connections));
        }

        public PowerNode setConnections(Direction... connections) {
            mutableConnections = connections(directions(connections));
            mutableConnectionPoints = standardConnectionPoints(getPos(), mutableConnections);
            markRecentlyChanged();
            return this;
        }

        public PowerNode setConnections(DirPos... connections) {
            mutableConnectionPoints = connectionPoints(connections);
            mutableConnections = directionsFromConnectionPoints(mutableConnectionPoints);
            markRecentlyChanged();
            return this;
        }

        public PowerNode setConnections(com.hbm.ntm.world.DirPos... connections) {
            mutableConnectionPoints = worldConnectionPoints(connections);
            mutableConnections = directionsFromConnectionPoints(mutableConnectionPoints);
            markRecentlyChanged();
            return this;
        }

        public PowerNode addConnection(DirPos connection) {
            if (connection == null) {
                return this;
            }
            mutableConnectionPoints = appendConnectionPoint(getConnectionPoints(), connectionPoint(connection));
            mutableConnections = directionsFromConnectionPoints(mutableConnectionPoints);
            markRecentlyChanged();
            return this;
        }

        public PowerNode addConnection(com.hbm.ntm.world.DirPos connection) {
            if (connection == null) {
                return this;
            }
            mutableConnectionPoints = appendConnectionPoint(getConnectionPoints(), worldConnectionPoint(connection));
            mutableConnections = directionsFromConnectionPoints(mutableConnectionPoints);
            markRecentlyChanged();
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

    private static Set<NodeConnection> connectionPoints(DirPos... connections) {
        if (connections == null || connections.length == 0) {
            return Set.of();
        }
        LinkedHashSet<NodeConnection> result = new LinkedHashSet<>();
        for (DirPos pos : connections) {
            if (pos != null) {
                result.add(connectionPoint(pos));
            }
        }
        return Set.copyOf(result);
    }

    private static NodeConnection connectionPoint(DirPos pos) {
        return pos.getDir() == null
                ? NodeConnection.point(pos.immutable())
                : new NodeConnection(pos.immutable(), pos.getDir());
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
        return pos.getDir() == null
                ? NodeConnection.point(pos.immutable())
                : new NodeConnection(pos.immutable(), pos.getDir());
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

    private Nodespace() {
    }
}
