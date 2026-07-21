package com.hbm.ntm.uninos.networkproviders;

import com.hbm.inventory.material.NTMMaterial;
import com.hbm.ntm.uninos.HbmNetworkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class FoundryNode extends HbmNetworkNode {
    @Nullable
    private NTMMaterial material;

    public FoundryNode(BlockPos pos) {
        super(pos);
    }

    public FoundryNode(BlockPos pos, Set<Direction> connections) {
        super(pos, connections);
    }

    public FoundryNode(Set<BlockPos> positions, Set<Direction> connections) {
        super(positions, connections);
    }

    @Override
    public FoundryNode setConnections(com.hbm.ntm.util.fauxpointtwelve.DirPos... connections) {
        super.setConnections(connections);
        return this;
    }

    public FoundryNetwork getFoundryNet() {
        return getNet() instanceof FoundryNetwork foundryNetwork ? foundryNetwork : null;
    }

    @Nullable
    public NTMMaterial getMaterial() {
        return material;
    }

    public void setMaterial(@Nullable NTMMaterial material) {
        this.material = material;
    }
}
