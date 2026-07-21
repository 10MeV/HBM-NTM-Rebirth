package com.hbm.ntm.uninos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class HbmNetworkNode {
    private final Set<BlockPos> positions;
    private Set<Direction> connections;
    private Set<NodeConnection> connectionPoints;
    /** Live legacy {@code GenNode#net} state. */
    public HbmNodeNet<?, ?, ?> net;
    /** Live legacy {@code GenNode#expired} state. */
    public boolean expired;
    /** Live legacy {@code GenNode#recentlyChanged} state. */
    public boolean recentlyChanged = true;

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
        // GenNode stores positions in a BlockPos[] and callers can observe
        // its first element.  Preserve the supplied insertion order instead
        // of Set.copyOf's unspecified iteration order.
        this.positions = Collections.unmodifiableSet(immutablePositions);
        this.connectionPoints = copyConnectionPoints(connectionPoints);
        this.connections = this.connectionPoints.stream()
                .filter(connection -> !connection.direct())
                .map(NodeConnection::direction)
                .filter(Objects::nonNull)
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
        return getConnections().contains(direction);
    }

    public boolean connectsTo(NodeConnection incoming) {
        for (NodeConnection ownConnection : getConnectionPoints()) {
            if (ownConnection.connectsBackTo(incoming)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Modern equivalent of the direction-only convenience used by node callers.
     * Explicit {@code DirPos} overloads below retain the legacy connection
     * coordinates instead of deriving them from this node's primary position.
     */
    public HbmNetworkNode setConnections(Direction... directions) {
        EnumSet<Direction> configuredDirections = EnumSet.noneOf(Direction.class);
        if (directions != null) {
            for (Direction direction : directions) {
                if (direction != null) {
                    configuredDirections.add(direction);
                }
            }
        }
        replaceConnectionPoints(createStandardConnectionPoints(getPos(), configuredDirections));
        return this;
    }

    /** Compatibility equivalent of 1.7.10 {@code GenNode#setConnections(DirPos...)}. */
    public HbmNetworkNode setConnections(com.hbm.ntm.util.fauxpointtwelve.DirPos... configuredConnections) {
        LinkedHashSet<NodeConnection> points = new LinkedHashSet<>();
        if (configuredConnections != null) {
            for (com.hbm.ntm.util.fauxpointtwelve.DirPos connection : configuredConnections) {
                if (connection != null) {
                    points.add(connectionPoint(connection));
                }
            }
        }
        replaceConnectionPoints(points);
        return this;
    }

    /** Modern-coordinate overload of the legacy fluent connection API. */
    public HbmNetworkNode setConnections(com.hbm.ntm.world.DirPos... configuredConnections) {
        LinkedHashSet<NodeConnection> points = new LinkedHashSet<>();
        if (configuredConnections != null) {
            for (com.hbm.ntm.world.DirPos connection : configuredConnections) {
                if (connection != null) {
                    points.add(connectionPoint(connection));
                }
            }
        }
        replaceConnectionPoints(points);
        return this;
    }

    /** Compatibility equivalent of 1.7.10 {@code GenNode#setStandardConnections}. */
    public HbmNetworkNode setStandardConnections(int xCoord, int yCoord, int zCoord) {
        return setConnections(
                new com.hbm.ntm.util.fauxpointtwelve.DirPos(xCoord + 1, yCoord, zCoord, Direction.EAST),
                new com.hbm.ntm.util.fauxpointtwelve.DirPos(xCoord - 1, yCoord, zCoord, Direction.WEST),
                new com.hbm.ntm.util.fauxpointtwelve.DirPos(xCoord, yCoord + 1, zCoord, Direction.UP),
                new com.hbm.ntm.util.fauxpointtwelve.DirPos(xCoord, yCoord - 1, zCoord, Direction.DOWN),
                new com.hbm.ntm.util.fauxpointtwelve.DirPos(xCoord, yCoord, zCoord + 1, Direction.SOUTH),
                new com.hbm.ntm.util.fauxpointtwelve.DirPos(xCoord, yCoord, zCoord - 1, Direction.NORTH));
    }

    /** Compatibility equivalent of 1.7.10 {@code GenNode#addConnection}. */
    public HbmNetworkNode addConnection(com.hbm.ntm.util.fauxpointtwelve.DirPos connection) {
        if (connection != null) {
            appendConnectionPoint(connectionPoint(connection));
        }
        return this;
    }

    /** Modern-coordinate overload of the legacy fluent connection API. */
    public HbmNetworkNode addConnection(com.hbm.ntm.world.DirPos connection) {
        if (connection != null) {
            appendConnectionPoint(connectionPoint(connection));
        }
        return this;
    }

    public HbmNodeNet<?, ?, ?> getNet() {
        return net;
    }

    public boolean hasValidNet() {
        return net != null && net.isValid();
    }

    public void setNet(HbmNodeNet<?, ?, ?> net) {
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

    /**
     * Lets typed legacy facades replace their endpoints without losing the
     * common nodespace matching contract.
     */
    protected final void replaceConnectionPoints(Set<NodeConnection> points) {
        this.connectionPoints = copyConnectionPoints(points);
        this.connections = directionsFor(this.connectionPoints);
        markRecentlyChanged();
    }

    private void appendConnectionPoint(NodeConnection point) {
        LinkedHashSet<NodeConnection> points = new LinkedHashSet<>(getConnectionPoints());
        points.add(point);
        replaceConnectionPoints(points);
    }

    private static BlockPos primaryPosition(Set<BlockPos> positions) {
        if (positions == null || positions.isEmpty()) {
            throw new IllegalArgumentException("HBM network node requires at least one position");
        }
        return positions.iterator().next();
    }

    private static Set<NodeConnection> createStandardConnectionPoints(BlockPos pos, Set<Direction> directions) {
        Set<NodeConnection> points = new LinkedHashSet<>();
        if (directions != null) {
            for (Direction direction : directions) {
                if (direction != null) {
                    points.add(new NodeConnection(pos.relative(direction), direction));
                }
            }
        }
        return points;
    }

    private static NodeConnection connectionPoint(com.hbm.ntm.util.fauxpointtwelve.DirPos pos) {
        return pos.getDir() == null ? NodeConnection.point(pos.immutable()) : new NodeConnection(pos.immutable(), pos.getDir());
    }

    private static NodeConnection connectionPoint(com.hbm.ntm.world.DirPos pos) {
        return pos.getDir() == null ? NodeConnection.point(pos.immutable()) : new NodeConnection(pos.immutable(), pos.getDir());
    }

    private static Set<Direction> directionsFor(Set<NodeConnection> points) {
        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);
        for (NodeConnection point : points) {
            if (!point.direct() && point.direction() != null) {
                result.add(point.direction());
            }
        }
        return result;
    }

    private static Set<NodeConnection> copyConnectionPoints(Set<NodeConnection> points) {
        if (points == null || points.isEmpty()) {
            return Set.of();
        }
        Set<NodeConnection> copy = new LinkedHashSet<>();
        for (NodeConnection point : points) {
            copy.add(new NodeConnection(point.pos(), point.direction(), point.direct(), point.directOrigin()));
        }
        // Explicit DirPos endpoint order is the modern carrier of legacy
        // GenNode#connections array order.
        return Collections.unmodifiableSet(copy);
    }

    public record NodeConnection(BlockPos pos, Direction direction, boolean direct, BlockPos directOrigin) {
        public NodeConnection(BlockPos pos, Direction direction) {
            this(pos, direction, false, null);
        }

        public static NodeConnection point(BlockPos pos) {
            return new NodeConnection(pos, null, false, null);
        }

        public static NodeConnection direct(BlockPos target, BlockPos origin) {
            return new NodeConnection(target, Direction.UP, true, origin);
        }

        public NodeConnection {
            Objects.requireNonNull(pos, "pos");
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
            if (direction == null || incoming.direction() == null) {
                return direction == null && incoming.direction() == null && pos.equals(incoming.pos());
            }
            return pos.relative(direction.getOpposite()).equals(incoming.pos())
                    && direction == incoming.direction().getOpposite();
        }
    }
}
