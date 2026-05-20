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

    public HbmPowerNet getPowerNet() {
        return getNet() instanceof HbmPowerNet powerNet ? powerNet : null;
    }
}
