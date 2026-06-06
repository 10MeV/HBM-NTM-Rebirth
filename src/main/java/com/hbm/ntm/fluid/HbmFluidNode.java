package com.hbm.ntm.fluid;

import com.hbm.ntm.energy.HbmNetworkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Set;

public class HbmFluidNode extends HbmNetworkNode {
    private final FluidType type;

    public HbmFluidNode(BlockPos pos, FluidType type) {
        super(pos);
        this.type = type == null ? HbmFluids.NONE : type;
    }

    public HbmFluidNode(BlockPos pos, FluidType type, Set<Direction> connections) {
        super(pos, connections);
        this.type = type == null ? HbmFluids.NONE : type;
    }

    public HbmFluidNode(Set<BlockPos> positions, FluidType type, Set<Direction> connections) {
        super(positions, connections);
        this.type = type == null ? HbmFluids.NONE : type;
    }

    private HbmFluidNode(Set<BlockPos> positions, FluidType type, Set<NodeConnection> connectionPoints,
            boolean directConnectionPoints) {
        super(positions, connectionPoints, directConnectionPoints);
        this.type = type == null ? HbmFluids.NONE : type;
    }

    public static HbmFluidNode withConnectionPoints(Set<BlockPos> positions, FluidType type,
            Set<NodeConnection> connectionPoints) {
        return new HbmFluidNode(positions, type, connectionPoints, true);
    }

    public FluidType getFluidType() {
        return type;
    }

    public HbmFluidNet getFluidNet() {
        return getNet() instanceof HbmFluidNet fluidNet ? fluidNet : null;
    }
}
