package com.hbm.ntm.uninos.typed;

import com.hbm.ntm.uninos.HbmNodespace;
import com.hbm.ntm.uninos.HbmSubscribableNodeNet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.LinkedHashSet;
import java.util.Set;

public final class HbmTypedNodespace<T, N extends HbmTypedNetworkNode<T>, NET extends HbmSubscribableNodeNet<?, ?, N>> {
    private final HbmNodespace<NodeKey<T>, N, NET> nodespace;

    public HbmTypedNodespace(T type, NetworkFactory<T, N, NET> networkFactory) {
        this.nodespace = new HbmNodespace<NodeKey<T>, N, NET>(
                HbmTypedNodespace::keysForNode,
                (node, connection) -> new NodeKey<>(connection.pos(), node.getType()),
                node -> networkFactory.create(type),
                NET::resetTrackers,
                NET::update,
                NodeKey::pos);
    }

    public N getNode(Level level, BlockPos pos, T type) {
        return nodespace.getNode(level, new NodeKey<>(pos, type));
    }

    public N createNode(Level level, N node) {
        return nodespace.createNode(level, node);
    }

    public void destroyNode(Level level, BlockPos pos, T type) {
        nodespace.destroyNode(level, new NodeKey<>(pos, type));
    }

    public void unloadLevel(Level level) {
        nodespace.unloadLevel(level);
    }

    public void tick(ServerLevel level) {
        nodespace.tick(level);
    }

    public void unloadChunk(Level level, ChunkPos chunkPos) {
        nodespace.unloadChunk(level, chunkPos);
    }

    public int getNodeCount(Level level) {
        return nodespace.getUniqueNodeCount(level);
    }

    public int getNetworkCount(Level level) {
        return nodespace.getNetworkCount(level);
    }

    public NET getNetwork(Level level, BlockPos pos, T type) {
        return nodespace.getNetwork(level, new NodeKey<>(pos, type));
    }

    public Set<NET> getNetworks(Level level) {
        return nodespace.getNetworks(level);
    }

    private static <T, N extends HbmTypedNetworkNode<T>> Set<NodeKey<T>> keysForNode(N node) {
        Set<NodeKey<T>> keys = new LinkedHashSet<>();
        for (BlockPos pos : node.getPositions()) {
            keys.add(new NodeKey<>(pos, node.getType()));
        }
        return keys;
    }

    @FunctionalInterface
    public interface NetworkFactory<T, N extends HbmTypedNetworkNode<T>, NET extends HbmSubscribableNodeNet<?, ?, N>> {
        NET create(T type);
    }

    private record NodeKey<T>(BlockPos pos, T type) {
        private NodeKey {
            pos = pos.immutable();
        }
    }
}
