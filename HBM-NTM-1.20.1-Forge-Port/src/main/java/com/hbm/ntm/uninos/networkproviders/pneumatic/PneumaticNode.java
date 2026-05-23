package com.hbm.ntm.uninos.networkproviders.pneumatic;

import com.hbm.ntm.uninos.HbmNetworkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Set;

public class PneumaticNode extends HbmNetworkNode {
    public PneumaticNode(BlockPos pos) {
        super(pos);
    }

    public PneumaticNode(BlockPos pos, Set<Direction> connections) {
        super(pos, connections);
    }

    public PneumaticNode(Set<BlockPos> positions, Set<Direction> connections) {
        super(positions, connections);
    }

    public PneumaticNetwork getPneumaticNet() {
        return getNet() instanceof PneumaticNetwork pneumaticNetwork ? pneumaticNetwork : null;
    }
}
