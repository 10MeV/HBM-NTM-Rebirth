package com.hbm.ntm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.EnumSet;
import java.util.Set;

public class HbmNetworkNode {
    private final BlockPos pos;
    private final Set<Direction> connections;
    private HbmNodeNet<?> net;
    private boolean expired;
    private boolean recentlyChanged = true;

    public HbmNetworkNode(BlockPos pos) {
        this(pos, EnumSet.allOf(Direction.class));
    }

    public HbmNetworkNode(BlockPos pos, Set<Direction> connections) {
        this.pos = pos.immutable();
        this.connections = connections.isEmpty() ? EnumSet.noneOf(Direction.class) : EnumSet.copyOf(connections);
    }

    public BlockPos getPos() {
        return pos;
    }

    public Set<Direction> getConnections() {
        return Set.copyOf(connections);
    }

    public boolean connects(Direction direction) {
        return connections.contains(direction);
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
}
