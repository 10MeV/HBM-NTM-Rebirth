package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.uninos.HbmNetworkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Set;

public class FoundryNode extends HbmNetworkNode {
    public FoundryNode(BlockPos pos) {
        super(pos);
    }

    public FoundryNode(BlockPos pos, Set<Direction> connections) {
        super(pos, connections);
    }

    public FoundryNode(Set<BlockPos> positions, Set<Direction> connections) {
        super(positions, connections);
    }

    public FoundryNetwork getFoundryNet() {
        return getNet() instanceof FoundryNetwork foundryNetwork ? foundryNetwork : null;
    }
}
