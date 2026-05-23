package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.uninos.HbmNetworkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Set;

public class RebarNode extends HbmNetworkNode {
    public RebarNode(BlockPos pos) {
        super(pos);
    }

    public RebarNode(BlockPos pos, Set<Direction> connections) {
        super(pos, connections);
    }

    public RebarNode(Set<BlockPos> positions, Set<Direction> connections) {
        super(positions, connections);
    }

    public RebarNetwork getRebarNet() {
        return getNet() instanceof RebarNetwork rebarNetwork ? rebarNetwork : null;
    }
}
