package com.hbm.ntm.energy;

import com.hbm.ntm.world.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.LinkedHashSet;
import java.util.Set;

public class HbmEnergyNode extends HbmNetworkNode {
    public HbmEnergyNode(BlockPos pos) {
        super(pos);
    }

    public HbmEnergyNode(BlockPos pos, Set<Direction> connections) {
        super(pos, connections);
    }

    public HbmEnergyNode(Set<BlockPos> positions, Set<Direction> connections) {
        super(positions, connections);
    }

    private HbmEnergyNode(Set<BlockPos> positions, Set<NodeConnection> connectionPoints, boolean directConnectionPoints) {
        super(positions, connectionPoints, directConnectionPoints);
    }

    public static HbmEnergyNode withConnectionPoints(Set<BlockPos> positions, Set<NodeConnection> connectionPoints) {
        return new HbmEnergyNode(positions, connectionPoints, true);
    }

    public static HbmEnergyNode withStandardLegacyConnections(BlockPos pos) {
        return withLegacyConnectionPoints(pos, HbmEnergyConnectionUtil.standardLegacyConnectionPoints(pos));
    }

    public static HbmEnergyNode withLegacyConnectionPoints(BlockPos pos, Iterable<DirPos> connectionPoints) {
        if (pos == null) {
            return new HbmEnergyNode(BlockPos.ZERO);
        }
        return withLegacyConnectionPoints(Set.of(pos.immutable()), connectionPoints);
    }

    public static HbmEnergyNode withLegacyConnectionPoints(Iterable<BlockPos> positions, Iterable<DirPos> connectionPoints) {
        Set<BlockPos> nodePositions = new LinkedHashSet<>();
        if (positions != null) {
            for (BlockPos position : positions) {
                if (position != null) {
                    nodePositions.add(position.immutable());
                }
            }
        }
        return withLegacyConnectionPoints(nodePositions, connectionPoints);
    }

    public static HbmEnergyNode withLegacyConnectionPoints(Set<BlockPos> positions, Iterable<DirPos> connectionPoints) {
        Set<NodeConnection> connections = new LinkedHashSet<>();
        if (connectionPoints != null) {
            for (DirPos point : connectionPoints) {
                if (point == null) {
                    continue;
                }
                connections.add(point.getDir() == null
                        ? NodeConnection.point(point)
                        : new NodeConnection(point, point.getDir()));
            }
        }
        return withConnectionPoints(positions, connections);
    }

    public HbmPowerNet getPowerNet() {
        return getNet() instanceof HbmPowerNet powerNet ? powerNet : null;
    }
}
