package com.hbm.ntm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

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

    public HbmPowerNet getPowerNet() {
        return getNet() instanceof HbmPowerNet powerNet ? powerNet : null;
    }
}
