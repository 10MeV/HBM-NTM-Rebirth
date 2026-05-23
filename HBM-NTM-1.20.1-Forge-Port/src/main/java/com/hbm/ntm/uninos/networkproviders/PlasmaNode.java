package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.uninos.HbmNetworkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Set;

public class PlasmaNode extends HbmNetworkNode {
    public PlasmaNode(BlockPos pos) {
        super(pos);
    }

    public PlasmaNode(BlockPos pos, Set<Direction> connections) {
        super(pos, connections);
    }

    public PlasmaNode(Set<BlockPos> positions, Set<Direction> connections) {
        super(positions, connections);
    }

    public PlasmaNetwork getPlasmaNet() {
        return getNet() instanceof PlasmaNetwork plasmaNetwork ? plasmaNetwork : null;
    }
}
