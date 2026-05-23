package com.hbm.ntm.uninos.typed;

import com.hbm.ntm.uninos.HbmNetworkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Set;

public class HbmTypedNetworkNode<T> extends HbmNetworkNode {
    private final T type;

    public HbmTypedNetworkNode(BlockPos pos, T type) {
        super(pos);
        this.type = type;
    }

    public HbmTypedNetworkNode(BlockPos pos, T type, Set<Direction> connections) {
        super(pos, connections);
        this.type = type;
    }

    public HbmTypedNetworkNode(Set<BlockPos> positions, T type, Set<Direction> connections) {
        super(positions, connections);
        this.type = type;
    }

    public T getType() {
        return type;
    }
}
