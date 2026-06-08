package com.hbm.ntm.uninos;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public final class HbmNodespace<K, N extends HbmNetworkNode, T extends HbmNodeNet<N>> {
    private final Map<ResourceKey<Level>, NodeWorld<K, N, T>> worlds = new HashMap<>();
    private final Function<N, Collection<K>> keyFactory;
    private final ConnectionKeyFactory<K, N> connectionKeyFactory;
    private final HbmNetworkProvider<N, T> networkProvider;
    private final Consumer<T> resetNetwork;
    private final Consumer<T> updateNetwork;
    private final Function<K, BlockPos> keyPosition;

    public HbmNodespace(
            Function<N, Collection<K>> keyFactory,
            ConnectionKeyFactory<K, N> connectionKeyFactory,
            HbmNetworkProvider<N, T> networkProvider,
            Consumer<T> resetNetwork,
            Consumer<T> updateNetwork,
            Function<K, BlockPos> keyPosition) {
        this.keyFactory = keyFactory;
        this.connectionKeyFactory = connectionKeyFactory;
        this.networkProvider = networkProvider;
        this.resetNetwork = resetNetwork;
        this.updateNetwork = updateNetwork;
        this.keyPosition = keyPosition;
    }

    public N getNode(Level level, K key) {
        NodeWorld<K, N, T> nodeWorld = worlds.get(level.dimension());
        return nodeWorld == null ? null : nodeWorld.nodes.get(key);
    }

    public T getNetwork(Level level, K key) {
        N node = getNode(level, key);
        if (node == null || !node.hasValidNet()) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T network = (T) node.getNet();
        return network;
    }

    public N createNode(Level level, N node) {
        NodeWorld<K, N, T> nodeWorld = worlds.computeIfAbsent(level.dimension(), ignored -> new NodeWorld<>());
        Set<N> replacedNodes = new LinkedHashSet<>();
        for (K key : keyFactory.apply(node)) {
            N oldNode = nodeWorld.nodes.get(key);
            if (oldNode != null && oldNode != node) {
                replacedNodes.add(oldNode);
            }
        }
        for (N oldNode : replacedNodes) {
            T oldNet = castNet(oldNode.getNet());
            popNode(nodeWorld, oldNode);
            rebuildNetworkAfterRemoval(nodeWorld, oldNet);
        }
        node.setExpired(false);
        for (K key : keyFactory.apply(node)) {
            nodeWorld.nodes.put(key, node);
        }
        checkNodeConnection(nodeWorld, node);
        return node;
    }

    public void destroyNode(Level level, K key) {
        NodeWorld<K, N, T> nodeWorld = worlds.get(level.dimension());
        if (nodeWorld == null) {
            return;
        }
        N node = nodeWorld.nodes.get(key);
        if (node != null) {
            T net = castNet(node.getNet());
            popNode(nodeWorld, node);
            rebuildNetworkAfterRemoval(nodeWorld, net);
            markConnectionNeighborsChanged(nodeWorld, node);
        }
    }

    public void unloadLevel(Level level) {
        NodeWorld<K, N, T> nodeWorld = worlds.remove(level.dimension());
        if (nodeWorld == null) {
            return;
        }
        for (N node : new LinkedHashSet<>(nodeWorld.nodes.values())) {
            popNode(nodeWorld, node);
        }
        nodeWorld.nodes.clear();
        nodeWorld.activeNetworks.clear();
    }

    public void tick(ServerLevel level) {
        NodeWorld<K, N, T> nodeWorld = worlds.get(level.dimension());
        if (nodeWorld == null) {
            return;
        }

        pruneUnloadedChunks(level, nodeWorld);

        for (N node : new LinkedHashSet<>(nodeWorld.nodes.values())) {
            if (!node.hasValidNet() || node.isRecentlyChanged()) {
                checkNodeConnection(nodeWorld, node);
                node.clearRecentlyChanged();
            }
        }

        updateNetworks(nodeWorld);
    }

    public int getNodePositionCount(Level level) {
        NodeWorld<K, N, T> nodeWorld = worlds.get(level.dimension());
        return nodeWorld == null ? 0 : nodeWorld.nodes.size();
    }

    public int getUniqueNodeCount(Level level) {
        NodeWorld<K, N, T> nodeWorld = worlds.get(level.dimension());
        return nodeWorld == null ? 0 : new LinkedHashSet<>(nodeWorld.nodes.values()).size();
    }

    public int getNetworkCount(Level level) {
        NodeWorld<K, N, T> nodeWorld = worlds.get(level.dimension());
        return nodeWorld == null ? 0 : nodeWorld.activeNetworks.size();
    }

    public Set<T> getNetworks(Level level) {
        NodeWorld<K, N, T> nodeWorld = worlds.get(level.dimension());
        return nodeWorld == null ? Set.of() : Set.copyOf(nodeWorld.activeNetworks);
    }

    public boolean markNodeAndConnectionNeighborsChanged(Level level, K key) {
        NodeWorld<K, N, T> nodeWorld = worlds.get(level.dimension());
        if (nodeWorld == null) {
            return false;
        }
        boolean marked = false;
        N node = nodeWorld.nodes.get(key);
        if (node != null) {
            node.markRecentlyChanged();
            marked = true;
            marked = markConnectionNeighborsChanged(nodeWorld, node) || marked;
        }
        return marked;
    }

    public void unloadChunk(Level level, ChunkPos chunkPos) {
        NodeWorld<K, N, T> nodeWorld = worlds.get(level.dimension());
        if (nodeWorld == null) {
            return;
        }
        Set<N> toRemove = new LinkedHashSet<>();
        for (Map.Entry<K, N> entry : nodeWorld.nodes.entrySet()) {
            if (new ChunkPos(keyPosition.apply(entry.getKey())).equals(chunkPos)) {
                toRemove.add(entry.getValue());
            }
        }
        for (N node : toRemove) {
            T net = castNet(node.getNet());
            popNode(nodeWorld, node);
            rebuildNetworkAfterRemoval(nodeWorld, net);
            markConnectionNeighborsChanged(nodeWorld, node);
        }
    }

    public ForceRebuildResult forceRebuild(Level level) {
        NodeWorld<K, N, T> nodeWorld = worlds.get(level.dimension());
        if (nodeWorld == null) {
            return new ForceRebuildResult(0, 0, 0, 0);
        }
        LinkedHashSet<N> nodes = new LinkedHashSet<>(nodeWorld.nodes.values());
        int oldNetworks = nodeWorld.activeNetworks.size();
        for (T net : new ArrayList<>(nodeWorld.activeNetworks)) {
            net.destroy();
        }
        nodeWorld.activeNetworks.clear();

        int reusableNodes = 0;
        for (N node : nodes) {
            if (!containsNode(nodeWorld, node)) {
                continue;
            }
            node.setExpired(false);
            node.setNet(null);
            node.markRecentlyChanged();
            reusableNodes++;
        }
        for (N node : nodes) {
            if (!containsNode(nodeWorld, node)) {
                continue;
            }
            checkNodeConnection(nodeWorld, node);
            node.clearRecentlyChanged();
        }
        return new ForceRebuildResult(reusableNodes, oldNetworks, nodeWorld.activeNetworks.size(), nodeWorld.reapTimer);
    }

    public Diagnostics getDiagnostics(Level level) {
        NodeWorld<K, N, T> nodeWorld = worlds.get(level.dimension());
        if (nodeWorld == null) {
            return Diagnostics.empty();
        }

        LinkedHashSet<N> uniqueNodes = new LinkedHashSet<>(nodeWorld.nodes.values());
        int dirtyNodes = 0;
        int expiredNodes = 0;
        int orphanNodes = 0;
        int linkRefs = 0;
        int invalidNetworks = 0;
        for (N node : uniqueNodes) {
            if (node.isRecentlyChanged()) {
                dirtyNodes++;
            }
            if (node.isExpired()) {
                expiredNodes++;
            }
            if (!node.hasValidNet()) {
                orphanNodes++;
            }
        }
        for (T net : nodeWorld.activeNetworks) {
            if (!net.isValid()) {
                invalidNetworks++;
            }
            linkRefs += net.linkCount();
        }

        return new Diagnostics(
                nodeWorld.nodes.size(),
                uniqueNodes.size(),
                nodeWorld.activeNetworks.size(),
                invalidNetworks,
                linkRefs,
                dirtyNodes,
                expiredNodes,
                orphanNodes,
                nodeWorld.reapTimer);
    }

    public ChunkDiagnostics getChunkDiagnostics(Level level, ChunkPos chunkPos) {
        NodeWorld<K, N, T> nodeWorld = worlds.get(level.dimension());
        if (nodeWorld == null || chunkPos == null) {
            return ChunkDiagnostics.empty();
        }

        LinkedHashSet<N> uniqueNodes = new LinkedHashSet<>();
        int nodePositions = 0;
        for (Map.Entry<K, N> entry : nodeWorld.nodes.entrySet()) {
            if (new ChunkPos(keyPosition.apply(entry.getKey())).equals(chunkPos)) {
                nodePositions++;
                uniqueNodes.add(entry.getValue());
            }
        }

        int dirtyNodes = 0;
        int expiredNodes = 0;
        int orphanNodes = 0;
        LinkedHashSet<T> networks = new LinkedHashSet<>();
        for (N node : uniqueNodes) {
            if (node.isRecentlyChanged()) {
                dirtyNodes++;
            }
            if (node.isExpired()) {
                expiredNodes++;
            }
            if (!node.hasValidNet()) {
                orphanNodes++;
                continue;
            }
            T net = castNet(node.getNet());
            if (net != null) {
                networks.add(net);
            }
        }

        int invalidNetworks = 0;
        int linkRefs = 0;
        for (T net : networks) {
            if (!net.isValid()) {
                invalidNetworks++;
            }
            linkRefs += net.linkCount();
        }

        return new ChunkDiagnostics(
                nodePositions,
                uniqueNodes.size(),
                networks.size(),
                invalidNetworks,
                linkRefs,
                dirtyNodes,
                expiredNodes,
                orphanNodes);
    }

    private void updateNetworks(NodeWorld<K, N, T> nodeWorld) {
        for (T net : nodeWorld.activeNetworks) {
            resetNetwork.accept(net);
        }
        for (T net : new ArrayList<>(nodeWorld.activeNetworks)) {
            if (net.isValid()) {
                updateNetwork.accept(net);
            }
        }

        if (nodeWorld.reapTimer <= 0) {
            nodeWorld.activeNetworks.removeIf(net -> {
                if (!net.isValid() || net.linkCount() <= 0) {
                    net.destroy();
                    return true;
                }
                return false;
            });
            nodeWorld.reapTimer = 5 * 60 * 20;
        } else {
            nodeWorld.reapTimer--;
        }
    }

    private void checkNodeConnection(NodeWorld<K, N, T> nodeWorld, N node) {
        for (HbmNetworkNode.NodeConnection connection : node.getConnectionPoints()) {
            N neighbor = nodeWorld.nodes.get(connectionKeyFactory.keyForConnection(node, connection));
            if (neighbor == null || !neighbor.connectsTo(connection)) {
                continue;
            }
            connectToNode(nodeWorld, node, neighbor);
        }

        if (!node.hasValidNet()) {
            T net = networkProvider.provideNetwork(node);
            nodeWorld.activeNetworks.add(net);
            net.joinLink(node);
        }
    }

    private void connectToNode(NodeWorld<K, N, T> nodeWorld, N origin, N connection) {
        T originNet = castNet(origin.getNet());
        T connectionNet = castNet(connection.getNet());

        if (originNet != null && originNet.isValid() && connectionNet != null && connectionNet.isValid()) {
            if (originNet == connectionNet) {
                return;
            }
            if (originNet.linkCount() > connectionNet.linkCount()) {
                originNet.joinNetwork(connectionNet);
                nodeWorld.activeNetworks.remove(connectionNet);
            } else {
                connectionNet.joinNetwork(originNet);
                nodeWorld.activeNetworks.remove(originNet);
            }
        } else if ((originNet == null || !originNet.isValid()) && connectionNet != null && connectionNet.isValid()) {
            connectionNet.joinLink(origin);
        } else if (originNet != null && originNet.isValid() && (connectionNet == null || !connectionNet.isValid())) {
            originNet.joinLink(connection);
        }
    }

    private boolean markConnectionNeighborsChanged(NodeWorld<K, N, T> nodeWorld, N node) {
        boolean marked = false;
        for (HbmNetworkNode.NodeConnection connection : node.getConnectionPoints()) {
            N neighbor = nodeWorld.nodes.get(connectionKeyFactory.keyForConnection(node, connection));
            if (neighbor != null) {
                neighbor.markRecentlyChanged();
                marked = true;
            }
        }
        return marked;
    }

    private void rebuildNetworkAfterRemoval(NodeWorld<K, N, T> nodeWorld, T oldNet) {
        if (oldNet == null) {
            return;
        }
        Set<N> oldLinks = new LinkedHashSet<>(oldNet.getLinks());
        oldLinks.removeIf(HbmNetworkNode::isExpired);
        oldNet.destroy();
        nodeWorld.activeNetworks.remove(oldNet);

        for (N link : oldLinks) {
            if (containsNode(nodeWorld, link)) {
                link.markRecentlyChanged();
            }
        }
    }

    private void popNode(NodeWorld<K, N, T> nodeWorld, N node) {
        T net = castNet(node.getNet());
        if (net != null) {
            net.leaveLink(node);
            if (net.linkCount() <= 0) {
                net.destroy();
                nodeWorld.activeNetworks.remove(net);
            }
        }
        node.setExpired(true);
        node.setNet(null);
        for (K key : keyFactory.apply(node)) {
            if (nodeWorld.nodes.get(key) == node) {
                nodeWorld.nodes.remove(key);
            }
        }
    }

    private void pruneUnloadedChunks(ServerLevel level, NodeWorld<K, N, T> nodeWorld) {
        Set<N> toRemove = new LinkedHashSet<>();
        for (Map.Entry<K, N> entry : nodeWorld.nodes.entrySet()) {
            BlockPos pos = keyPosition.apply(entry.getKey());
            if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                toRemove.add(entry.getValue());
            }
        }
        for (N node : toRemove) {
            T net = castNet(node.getNet());
            popNode(nodeWorld, node);
            rebuildNetworkAfterRemoval(nodeWorld, net);
            markConnectionNeighborsChanged(nodeWorld, node);
        }
    }

    private boolean containsNode(NodeWorld<K, N, T> nodeWorld, N node) {
        for (K key : keyFactory.apply(node)) {
            if (nodeWorld.nodes.get(key) == node) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private T castNet(HbmNodeNet<?> net) {
        return (T) net;
    }

    @FunctionalInterface
    public interface ConnectionKeyFactory<K, N extends HbmNetworkNode> {
        K keyForConnection(N node, HbmNetworkNode.NodeConnection connection);
    }

    private static final class NodeWorld<K, N extends HbmNetworkNode, T extends HbmNodeNet<N>> {
        private final Map<K, N> nodes = new LinkedHashMap<>();
        private final Set<T> activeNetworks = new LinkedHashSet<>();
        private int reapTimer;
    }

    public record Diagnostics(
            int nodePositions,
            int uniqueNodes,
            int networks,
            int invalidNetworks,
            int linkRefs,
            int dirtyNodes,
            int expiredNodes,
            int orphanNodes,
            int reapTimer) {
        private static Diagnostics empty() {
            return new Diagnostics(0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
    }

    public record ChunkDiagnostics(
            int nodePositions,
            int uniqueNodes,
            int networks,
            int invalidNetworks,
            int linkRefs,
            int dirtyNodes,
            int expiredNodes,
            int orphanNodes) {
        private static ChunkDiagnostics empty() {
            return new ChunkDiagnostics(0, 0, 0, 0, 0, 0, 0, 0);
        }
    }

    public record ForceRebuildResult(
            int nodes,
            int oldNetworks,
            int newNetworks,
            int reapTimer) {
    }
}
