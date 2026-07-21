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

public final class HbmNodespace<K, N extends HbmNetworkNode, T extends HbmNodeNet<?, ?, N>> {
    private final Map<ResourceKey<Level>, NodeWorld<K, N, T>> worlds = new HashMap<>();
    private final Function<N, Collection<K>> keyFactory;
    private final ConnectionKeyFactory<K, N> connectionKeyFactory;
    private final Function<N, HbmNetworkProvider<N, T>> networkProviderFactory;
    private final Consumer<T> resetNetwork;
    private final Consumer<T> updateNetwork;
    private final Function<K, BlockPos> keyPosition;

    public HbmNodespace(
            Function<N, Collection<K>> keyFactory,
            ConnectionKeyFactory<K, N> connectionKeyFactory,
            Function<N, HbmNetworkProvider<N, T>> networkProviderFactory,
            Consumer<T> resetNetwork,
            Consumer<T> updateNetwork,
            Function<K, BlockPos> keyPosition) {
        this.keyFactory = keyFactory;
        this.connectionKeyFactory = connectionKeyFactory;
        this.networkProviderFactory = networkProviderFactory;
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
        // 1.7.10 UniNodeWorld#pushNode only overwrote map entries. It did not
        // implicitly pop an existing node, destroy its network, revive it, or
        // change its recentlyChanged state.
        for (K key : keyFactory.apply(node)) {
            nodeWorld.nodes.put(key, node);
        }
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

    /**
     * Removes the exact node instance, including every key it owns. This is the
     * direct equivalent of 1.7.10 {@code UniNodespace.destroyNode(World, GenNode)}.
     */
    public void destroyNode(Level level, N node) {
        if (node == null) {
            return;
        }
        NodeWorld<K, N, T> nodeWorld = worlds.get(level.dimension());
        if (nodeWorld == null) {
            return;
        }
        T net = castNet(node.getNet());
        popNode(nodeWorld, node);
        rebuildNetworkAfterRemoval(nodeWorld, net);
        markConnectionNeighborsChanged(nodeWorld, node);
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

    /**
     * Mirrors 1.7.10 {@code CommandReapNetworks}: expire linked nodes and
     * clear mappings/active networks without calling {@link HbmNodeNet#destroy()}.
     * A real level unload deliberately remains the stronger lifecycle path.
     */
    public void reapLevel(Level level) {
        NodeWorld<K, N, T> nodeWorld = worlds.remove(level.dimension());
        if (nodeWorld == null) {
            return;
        }
        for (T net : new ArrayList<>(nodeWorld.activeNetworks)) {
            net.reapLegacy();
        }
        nodeWorld.nodes.clear();
        nodeWorld.activeNetworks.clear();
    }

    public void tick(ServerLevel level) {
        NodeWorld<K, N, T> nodeWorld = worlds.get(level.dimension());
        if (nodeWorld == null) {
            return;
        }

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

    public int rebuildChanged(Level level) {
        NodeWorld<K, N, T> nodeWorld = worlds.get(level.dimension());
        if (nodeWorld == null) {
            return 0;
        }
        int rebuilt = 0;
        for (N node : new LinkedHashSet<>(nodeWorld.nodes.values())) {
            if (!containsNode(nodeWorld, node)) {
                continue;
            }
            if (!node.hasValidNet() || node.isRecentlyChanged()) {
                checkNodeConnection(nodeWorld, node);
                node.clearRecentlyChanged();
                rebuilt++;
            }
        }
        return rebuilt;
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
            updateNetwork.accept(net);
        }

        if (nodeWorld.reapTimer <= 0) {
            // 1.7.10 UniNodespace#updateNetworks only drops expired links and
            // removes empty networks from its active set.  It deliberately
            // does not call NodeNet#destroy here: an externally retained
            // network (and its subscriptions/caches) keeps its own state.
            for (T net : nodeWorld.activeNetworks) {
                net.links.removeIf(HbmNetworkNode::isExpired);
            }
            nodeWorld.activeNetworks.removeIf(net -> net.links.isEmpty());
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
            HbmNetworkProvider<N, T> provider = networkProviderFactory.apply(node);
            T net = provider.provideNetwork();
            net.setInvalidationHook(() -> nodeWorld.activeNetworks.remove(net));
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
                joinNetworks(originNet, connectionNet);
                nodeWorld.activeNetworks.remove(connectionNet);
            } else {
                joinNetworks(connectionNet, originNet);
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
            // UniNodeWorld#popNode removes every held-node key unconditionally,
            // including a key a later pushNode call has overwritten.
            nodeWorld.nodes.remove(key);
        }
    }

    /**
     * The legacy {@code UniNodespace} stores every provider's network behind
     * one raw {@code NodeNet} type before invoking {@code joinNetworks}.
     * This is the equivalent erasure boundary: both values are the same
     * nodespace-owned {@code T}, while the public net still retains typed
     * receiver/provider state for ordinary callers.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void joinNetworks(T receiving, T absorbed) {
        ((HbmNodeNet) receiving).joinNetworks((HbmNodeNet) absorbed);
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
    private T castNet(HbmNodeNet<?, ?, ?> net) {
        return (T) net;
    }

    @FunctionalInterface
    public interface ConnectionKeyFactory<K, N extends HbmNetworkNode> {
        K keyForConnection(N node, HbmNetworkNode.NodeConnection connection);
    }

    private static final class NodeWorld<K, N extends HbmNetworkNode, T extends HbmNodeNet<?, ?, N>> {
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
