package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.uninos.HbmNetworkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Set;

public class KlystronNode extends HbmNetworkNode {
    public KlystronNode(BlockPos pos) {
        super(pos);
    }

    public KlystronNode(BlockPos pos, Set<Direction> connections) {
        super(pos, connections);
    }

    public KlystronNode(Set<BlockPos> positions, Set<Direction> connections) {
        super(positions, connections);
    }

    public KlystronNetwork getKlystronNet() {
        return getNet() instanceof KlystronNetwork klystronNetwork ? klystronNetwork : null;
    }
}
