package com.hbm.ntm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Set;

public class HbmNetworkNode extends com.hbm.ntm.uninos.HbmNetworkNode {
    public HbmNetworkNode(BlockPos pos) {
        super(pos);
    }

    public HbmNetworkNode(BlockPos pos, Set<Direction> connections) {
        super(pos, connections);
    }

    public HbmNetworkNode(Set<BlockPos> positions, Set<Direction> connections) {
        super(positions, connections);
    }

    protected HbmNetworkNode(Set<BlockPos> positions, Set<NodeConnection> connectionPoints, boolean directConnectionPoints) {
        super(positions, connectionPoints, directConnectionPoints);
    }

    public static HbmNetworkNode withConnectionPoints(Set<BlockPos> positions, Set<NodeConnection> connectionPoints) {
        return new HbmNetworkNode(positions, connectionPoints, true);
    }
}
