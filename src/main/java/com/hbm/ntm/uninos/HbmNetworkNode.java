package com.hbm.ntm.uninos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class HbmNetworkNode {
    private final Set<BlockPos> positions;
    private final Set<Direction> connections;
    private final Set<NodeConnection> connectionPoints;
    private HbmNodeNet<?> net;
    private boolean expired;
    private boolean recentlyChanged = true;

    public HbmNetworkNode(BlockPos pos) {
        this(pos, EnumSet.allOf(Direction.class));
    }

    public HbmNetworkNode(BlockPos pos, Set<Direction> connections) {
        this(Set.of(pos), connections);
    }

    public HbmNetworkNode(Set<BlockPos> positions, Set<Direction> connections) {
        this(positions, createStandardConnectionPoints(primaryPosition(positions), connections), false);
    }

    protected HbmNetworkNode(Set<BlockPos> positions, Set<NodeConnection> connectionPoints, boolean directConnectionPoints) {
        if (positions == null || positions.isEmpty()) {
            throw new IllegalArgumentException("HBM network node requires at least one position");
        }
        LinkedHashSet<BlockPos> immutablePositions = new LinkedHashSet<>();
        for (BlockPos position : positions) {
            immutablePositions.add(position.immutable());
        }
        this.positions = Set.copyOf(immutablePositions);
        this.connectionPoints = copyConnectionPoints(connectionPoints);
        this.connections = this.connectionPoints.stream()
                .filter(connection -> !connection.direct())
                .map(NodeConnection::direction)
                .collect(() -> EnumSet.noneOf(Direction.class), EnumSet::add, EnumSet::addAll);
    }

    public static HbmNetworkNode withConnectionPoints(Set<BlockPos> positions, Set<NodeConnection> connectionPoints) {
        return new HbmNetworkNode(positions, connectionPoints, true);
    }

    public BlockPos getPos() {
        return positions.iterator().next();
    }

    public Set<BlockPos> getPositions() {
        return positions;
    }

    public Set<Direction> getConnections() {
        return Set.copyOf(connections);
    }

    public Set<NodeConnection> getConnectionPoints() {
        return connectionPoints;
    }

    public boolean connects(Direction direction) {
        return connections.contains(direction);
    }

    public boolean connectsTo(NodeConnection incoming) {
        for (NodeConnection ownConnection : connectionPoints) {
            if (ownConnection.connectsBackTo(incoming)) {
                return true;
            }
        }
        return false;
    }

    public HbmNodeNet<?> getNet() {
        return net;
    }

    public boolean hasValidNet() {
        return net != null && net.isValid();
    }

    public void setNet(HbmNodeNet<?> net) {
        this.net = net;
        this.recentlyChanged = true;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isRecentlyChanged() {
        return recentlyChanged;
    }

    public void markRecentlyChanged() {
        this.recentlyChanged = true;
    }

    public void clearRecentlyChanged() {
        this.recentlyChanged = false;
    }

    private static BlockPos primaryPosition(Set<BlockPos> positions) {
        if (positions == null || positions.isEmpty()) {
            throw new IllegalArgumentException("HBM network node requires at least one position");
        }
        return positions.iterator().next();
    }

    private static Set<NodeConnection> createStandardConnectionPoints(BlockPos pos, Set<Direction> directions) {
        Set<NodeConnection> points = new LinkedHashSet<>();
        for (Direction direction : directions) {
            points.add(new NodeConnection(pos.relative(direction), direction));
        }
        return points;
    }

    private static Set<NodeConnection> copyConnectionPoints(Set<NodeConnection> points) {
        if (points == null || points.isEmpty()) {
            return Set.of();
        }
        Set<NodeConnection> copy = new LinkedHashSet<>();
        for (NodeConnection point : points) {
            copy.add(new NodeConnection(point.pos(), point.direction(), point.direct(), point.directOrigin()));
        }
        return Set.copyOf(copy);
    }

    public record NodeConnection(BlockPos pos, Direction direction, boolean direct, BlockPos directOrigin) {
        public NodeConnection(BlockPos pos, Direction direction) {
            this(pos, direction, false, null);
        }

        public static NodeConnection direct(BlockPos target, BlockPos origin) {
            return new NodeConnection(target, Direction.UP, true, origin);
        }

        public NodeConnection {
            pos = pos.immutable();
            if (directOrigin != null) {
                directOrigin = directOrigin.immutable();
            }
        }

        public boolean connectsBackTo(NodeConnection incoming) {
            if (direct || incoming.direct()) {
                return direct && incoming.direct()
                        && directOrigin != null
                        && incoming.directOrigin() != null
                        && pos.equals(incoming.directOrigin())
                        && directOrigin.equals(incoming.pos());
            }
            return pos.relative(direction.getOpposite()).equals(incoming.pos())
                    && direction == incoming.direction().getOpposite();
        }
    }
}
