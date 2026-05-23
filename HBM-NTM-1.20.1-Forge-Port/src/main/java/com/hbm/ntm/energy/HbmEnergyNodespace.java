package com.hbm.ntm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class HbmEnergyNodespace {
    private static final Map<ResourceKey<Level>, EnergyNodeWorld> WORLDS = new HashMap<>();

    private HbmEnergyNodespace() {
    }

    public static HbmEnergyNode getNode(Level level, BlockPos pos) {
        EnergyNodeWorld nodeWorld = WORLDS.get(level.dimension());
        return nodeWorld == null ? null : nodeWorld.nodes.get(pos);
    }

    public static HbmEnergyNode createNode(Level level, HbmEnergyNode node) {
        EnergyNodeWorld nodeWorld = WORLDS.computeIfAbsent(level.dimension(), ignored -> new EnergyNodeWorld());
        for (BlockPos pos : node.getPositions()) {
            HbmEnergyNode oldNode = nodeWorld.nodes.get(pos);
            if (oldNode != null && oldNode != node) {
                HbmPowerNet oldNet = oldNode.getPowerNet();
                popNode(nodeWorld, oldNode);
                rebuildNetworkAfterRemoval(nodeWorld, oldNet);
            }
        }
        for (BlockPos pos : node.getPositions()) {
            nodeWorld.nodes.put(pos, node);
        }
        checkNodeConnection(nodeWorld, node);
        return node;
    }

    public static void destroyNode(Level level, BlockPos pos) {
        EnergyNodeWorld nodeWorld = WORLDS.get(level.dimension());
        if (nodeWorld == null) {
            return;
        }
        HbmEnergyNode node = nodeWorld.nodes.get(pos);
        if (node != null) {
            HbmPowerNet net = node.getPowerNet();
            popNode(nodeWorld, node);
            rebuildNetworkAfterRemoval(nodeWorld, net);
            markNeighborsChanged(nodeWorld, pos);
        }
    }

    public static void unloadLevel(Level level) {
        EnergyNodeWorld nodeWorld = WORLDS.remove(level.dimension());
        if (nodeWorld == null) {
            return;
        }
        for (HbmEnergyNode node : new ArrayList<>(nodeWorld.nodes.values())) {
            popNode(nodeWorld, node);
        }
        nodeWorld.nodes.clear();
        nodeWorld.activePowerNets.clear();
    }

    public static void tick(ServerLevel level) {
        EnergyNodeWorld nodeWorld = WORLDS.get(level.dimension());
        if (nodeWorld == null) {
            return;
        }

        pruneUnloadedChunks(level, nodeWorld);

        for (HbmEnergyNode node : new LinkedHashSet<>(nodeWorld.nodes.values())) {
            if (!node.hasValidNet() || node.isRecentlyChanged()) {
                checkNodeConnection(nodeWorld, node);
                node.clearRecentlyChanged();
            }
        }

        updateNetworks(nodeWorld);
    }

    public static int getNodeCount(Level level) {
        EnergyNodeWorld nodeWorld = WORLDS.get(level.dimension());
        return nodeWorld == null ? 0 : new LinkedHashSet<>(nodeWorld.nodes.values()).size();
    }

    public static int getNetworkCount(Level level) {
        EnergyNodeWorld nodeWorld = WORLDS.get(level.dimension());
        return nodeWorld == null ? 0 : nodeWorld.activePowerNets.size();
    }

    public static Diagnostics getDiagnostics(Level level) {
        EnergyNodeWorld nodeWorld = WORLDS.get(level.dimension());
        if (nodeWorld == null) {
            return Diagnostics.empty();
        }

        LinkedHashSet<HbmEnergyNode> uniqueNodes = new LinkedHashSet<>(nodeWorld.nodes.values());
        int dirtyNodes = 0;
        int expiredNodes = 0;
        int orphanNodes = 0;
        int linkRefs = 0;
        int providerEntries = 0;
        int receiverEntries = 0;
        int invalidNetworks = 0;
        for (HbmEnergyNode node : uniqueNodes) {
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
        for (HbmPowerNet net : nodeWorld.activePowerNets) {
            if (!net.isValid()) {
                invalidNetworks++;
            }
            HbmPowerNet.DebugSnapshot snapshot = net.createDebugSnapshot();
            linkRefs += snapshot.links();
            providerEntries += snapshot.providers();
            receiverEntries += snapshot.receivers();
        }

        return new Diagnostics(
                nodeWorld.nodes.size(),
                uniqueNodes.size(),
                nodeWorld.activePowerNets.size(),
                invalidNetworks,
                linkRefs,
                dirtyNodes,
                expiredNodes,
                orphanNodes,
                providerEntries,
                receiverEntries,
                nodeWorld.reapTimer);
    }

    public static ForceRebuildResult forceRebuild(Level level) {
        EnergyNodeWorld nodeWorld = WORLDS.get(level.dimension());
        if (nodeWorld == null) {
            return new ForceRebuildResult(0, 0, 0, 0);
        }
        LinkedHashSet<HbmEnergyNode> nodes = new LinkedHashSet<>(nodeWorld.nodes.values());
        int oldNetworks = nodeWorld.activePowerNets.size();
        for (HbmPowerNet net : new ArrayList<>(nodeWorld.activePowerNets)) {
            net.destroy();
        }
        nodeWorld.activePowerNets.clear();

        int reusableNodes = 0;
        for (HbmEnergyNode node : nodes) {
            if (!containsNode(nodeWorld, node)) {
                continue;
            }
            node.setExpired(false);
            node.setNet(null);
            node.markRecentlyChanged();
            reusableNodes++;
        }
        for (HbmEnergyNode node : nodes) {
            if (!containsNode(nodeWorld, node)) {
                continue;
            }
            checkNodeConnection(nodeWorld, node);
            node.clearRecentlyChanged();
        }
        return new ForceRebuildResult(reusableNodes, oldNetworks, nodeWorld.activePowerNets.size(), nodeWorld.reapTimer);
    }

    public static boolean markNodeAndNeighborsChanged(Level level, BlockPos pos) {
        EnergyNodeWorld nodeWorld = WORLDS.get(level.dimension());
        if (nodeWorld == null) {
            return false;
        }
        boolean marked = false;
        HbmEnergyNode node = nodeWorld.nodes.get(pos);
        if (node != null) {
            node.markRecentlyChanged();
            marked = true;
        }
        for (Direction direction : Direction.values()) {
            HbmEnergyNode neighbor = nodeWorld.nodes.get(pos.relative(direction));
            if (neighbor != null) {
                neighbor.markRecentlyChanged();
                marked = true;
            }
        }
        return marked;
    }

    public static int getNetworkLinkCount(Level level, BlockPos pos) {
        HbmPowerNet powerNet = getPowerNet(level, pos);
        return powerNet == null ? 0 : powerNet.linkCount();
    }

    public static int getNetworkProviderCount(Level level, BlockPos pos) {
        HbmPowerNet powerNet = getPowerNet(level, pos);
        return powerNet == null ? 0 : powerNet.getProviderCount();
    }

    public static int getNetworkReceiverCount(Level level, BlockPos pos) {
        HbmPowerNet powerNet = getPowerNet(level, pos);
        return powerNet == null ? 0 : powerNet.getReceiverCount();
    }

    public static long getNetworkEnergyTracker(Level level, BlockPos pos) {
        HbmPowerNet powerNet = getPowerNet(level, pos);
        return powerNet == null ? 0L : powerNet.getEnergyTracker();
    }

    public static NetworkDebugSnapshot getNetworkDebugSnapshot(Level level, BlockPos pos) {
        HbmEnergyNode node = getNode(level, pos);
        if (node == null) {
            return NetworkDebugSnapshot.missing(pos);
        }
        HbmPowerNet powerNet = node.getPowerNet();
        if (powerNet == null) {
            return NetworkDebugSnapshot.noNetwork(pos, describeConnections(node), node.isRecentlyChanged());
        }
        return NetworkDebugSnapshot.present(
                pos,
                describeConnections(node),
                node.isRecentlyChanged(),
                powerNet.createDebugSnapshot());
    }

    public static boolean hasValidNetwork(Level level, BlockPos pos) {
        HbmPowerNet powerNet = getPowerNet(level, pos);
        return powerNet != null && powerNet.isValid();
    }

    public static String describeNodeConnections(Level level, BlockPos pos) {
        HbmEnergyNode node = getNode(level, pos);
        if (node == null) {
            return "none";
        }
        return node.getConnections().stream()
                .map(direction -> direction.getName().toLowerCase())
                .sorted()
                .collect(Collectors.joining(","));
    }

    public static void unloadChunk(Level level, ChunkPos chunkPos) {
        EnergyNodeWorld nodeWorld = WORLDS.get(level.dimension());
        if (nodeWorld == null) {
            return;
        }
        ArrayList<BlockPos> toRemove = new ArrayList<>();
        for (BlockPos pos : nodeWorld.nodes.keySet()) {
            if (new ChunkPos(pos).equals(chunkPos)) {
                toRemove.add(pos);
            }
        }
        for (BlockPos pos : toRemove) {
            HbmEnergyNode node = nodeWorld.nodes.get(pos);
            if (node != null) {
                HbmPowerNet net = node.getPowerNet();
                popNode(nodeWorld, node);
                rebuildNetworkAfterRemoval(nodeWorld, net);
                markNeighborsChanged(nodeWorld, pos);
            }
        }
    }

    private static HbmPowerNet getPowerNet(Level level, BlockPos pos) {
        HbmEnergyNode node = getNode(level, pos);
        return node == null ? null : node.getPowerNet();
    }

    private static String describeConnections(HbmEnergyNode node) {
        String connections = node.getConnections().stream()
                .map(direction -> direction.getName().toLowerCase())
                .sorted()
                .collect(Collectors.joining(","));
        return connections.isEmpty() ? "none" : connections;
    }

    private static void updateNetworks(EnergyNodeWorld nodeWorld) {
        for (HbmPowerNet net : nodeWorld.activePowerNets) {
            net.resetTrackers();
        }
        for (HbmPowerNet net : new ArrayList<>(nodeWorld.activePowerNets)) {
            if (net.isValid()) {
                net.update();
            }
        }

        if (nodeWorld.reapTimer <= 0) {
            Iterator<HbmPowerNet> iterator = nodeWorld.activePowerNets.iterator();
            while (iterator.hasNext()) {
                HbmPowerNet net = iterator.next();
                if (!net.isValid() || net.linkCount() <= 0) {
                    net.destroy();
                    iterator.remove();
                }
            }
            nodeWorld.reapTimer = 5 * 60 * 20;
        } else {
            nodeWorld.reapTimer--;
        }
    }

    private static void checkNodeConnection(EnergyNodeWorld nodeWorld, HbmEnergyNode node) {
        for (HbmNetworkNode.NodeConnection connection : node.getConnectionPoints()) {
            HbmEnergyNode neighbor = nodeWorld.nodes.get(connection.pos());
            if (neighbor == null || !neighbor.connectsTo(connection)) {
                continue;
            }
            connectToNode(node, neighbor);
        }

        if (!node.hasValidNet()) {
            HbmPowerNet net = new HbmPowerNet();
            nodeWorld.activePowerNets.add(net);
            net.joinLink(node);
        }
    }

    private static void connectToNode(HbmEnergyNode origin, HbmEnergyNode connection) {
        HbmPowerNet originNet = origin.getPowerNet();
        HbmPowerNet connectionNet = connection.getPowerNet();

        if (originNet != null && originNet.isValid() && connectionNet != null && connectionNet.isValid()) {
            if (originNet == connectionNet) {
                return;
            }
            if (originNet.linkCount() > connectionNet.linkCount()) {
                originNet.joinNetwork(connectionNet);
                removeNetwork(connectionNet);
            } else {
                connectionNet.joinNetwork(originNet);
                removeNetwork(originNet);
            }
        } else if ((originNet == null || !originNet.isValid()) && connectionNet != null && connectionNet.isValid()) {
            connectionNet.joinLink(origin);
        } else if (originNet != null && originNet.isValid() && (connectionNet == null || !connectionNet.isValid())) {
            originNet.joinLink(connection);
        }
    }

    private static void markNeighborsChanged(EnergyNodeWorld nodeWorld, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            HbmEnergyNode neighbor = nodeWorld.nodes.get(pos.relative(direction));
            if (neighbor != null) {
                neighbor.markRecentlyChanged();
            }
        }
    }

    private static void rebuildNetworkAfterRemoval(EnergyNodeWorld nodeWorld, HbmPowerNet oldNet) {
        if (oldNet == null) {
            return;
        }
        Set<HbmEnergyNode> oldLinks = new LinkedHashSet<>(oldNet.getLinks());
        oldLinks.removeIf(HbmNetworkNode::isExpired);
        oldNet.destroy();
        nodeWorld.activePowerNets.remove(oldNet);

        for (HbmEnergyNode link : oldLinks) {
            if (containsNode(nodeWorld, link)) {
                link.markRecentlyChanged();
            }
        }
    }

    private static void popNode(EnergyNodeWorld nodeWorld, HbmEnergyNode node) {
        HbmPowerNet net = node.getPowerNet();
        if (net != null) {
            net.leaveLink(node);
            if (net.linkCount() <= 0) {
                net.destroy();
                nodeWorld.activePowerNets.remove(net);
            }
        }
        node.setExpired(true);
        node.setNet(null);
        for (BlockPos pos : node.getPositions()) {
            if (nodeWorld.nodes.get(pos) == node) {
                nodeWorld.nodes.remove(pos);
            }
        }
    }

    private static void removeNetwork(HbmPowerNet net) {
        for (EnergyNodeWorld nodeWorld : WORLDS.values()) {
            nodeWorld.activePowerNets.remove(net);
        }
    }

    private static void pruneUnloadedChunks(ServerLevel level, EnergyNodeWorld nodeWorld) {
        ArrayList<BlockPos> toRemove = new ArrayList<>();
        for (BlockPos pos : nodeWorld.nodes.keySet()) {
            if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                toRemove.add(pos);
            }
        }
        for (BlockPos pos : toRemove) {
            HbmEnergyNode node = nodeWorld.nodes.get(pos);
            if (node != null) {
                HbmPowerNet net = node.getPowerNet();
                popNode(nodeWorld, node);
                rebuildNetworkAfterRemoval(nodeWorld, net);
                markNeighborsChanged(nodeWorld, pos);
            }
        }
    }

    private static boolean containsNode(EnergyNodeWorld nodeWorld, HbmEnergyNode node) {
        for (BlockPos pos : node.getPositions()) {
            if (nodeWorld.nodes.get(pos) == node) {
                return true;
            }
        }
        return false;
    }

    private static final class EnergyNodeWorld {
        private final Map<BlockPos, HbmEnergyNode> nodes = new LinkedHashMap<>();
        private final Set<HbmPowerNet> activePowerNets = new LinkedHashSet<>();
        private int reapTimer;
    }

    public record NetworkDebugSnapshot(
            BlockPos pos,
            boolean nodePresent,
            String nodeConnections,
            boolean recentlyChanged,
            boolean networkPresent,
            HbmPowerNet.DebugSnapshot network) {
        private static NetworkDebugSnapshot missing(BlockPos pos) {
            return new NetworkDebugSnapshot(pos, false, "none", false, false, null);
        }

        private static NetworkDebugSnapshot noNetwork(BlockPos pos, String connections, boolean recentlyChanged) {
            return new NetworkDebugSnapshot(pos, true, connections, recentlyChanged, false, null);
        }

        private static NetworkDebugSnapshot present(BlockPos pos, String connections, boolean recentlyChanged, HbmPowerNet.DebugSnapshot network) {
            return new NetworkDebugSnapshot(pos, true, connections, recentlyChanged, true, network);
        }
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
            int providerEntries,
            int receiverEntries,
            int reapTimer) {
        private static Diagnostics empty() {
            return new Diagnostics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
    }

    public record ForceRebuildResult(
            int nodes,
            int oldNetworks,
            int newNetworks,
            int reapTimer) {
    }
}
