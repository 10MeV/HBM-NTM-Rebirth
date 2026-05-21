package com.hbm.ntm.fluid;

import com.hbm.ntm.energy.HbmNetworkNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class HbmFluidNodespace {
    private static final Map<ResourceKey<Level>, FluidNodeWorld> WORLDS = new HashMap<>();

    public static HbmFluidNode getNode(Level level, BlockPos pos, FluidType type) {
        FluidNodeWorld nodeWorld = WORLDS.get(level.dimension());
        return nodeWorld == null ? null : nodeWorld.nodes.get(new NodeKey(pos, type));
    }

    public static HbmFluidNode createNode(Level level, HbmFluidNode node) {
        FluidNodeWorld nodeWorld = WORLDS.computeIfAbsent(level.dimension(), ignored -> new FluidNodeWorld());
        NodeKey key = new NodeKey(node.getPos(), node.getFluidType());
        HbmFluidNode oldNode = nodeWorld.nodes.put(key, node);
        if (oldNode != null && oldNode != node) {
            HbmFluidNet oldNet = oldNode.getFluidNet();
            popNode(nodeWorld, oldNode);
            rebuildNetworkAfterRemoval(nodeWorld, oldNet);
        }
        checkNodeConnection(nodeWorld, node);
        return node;
    }

    public static void destroyNode(Level level, BlockPos pos, FluidType type) {
        FluidNodeWorld nodeWorld = WORLDS.get(level.dimension());
        if (nodeWorld == null) {
            return;
        }
        HbmFluidNode node = nodeWorld.nodes.remove(new NodeKey(pos, type));
        if (node != null) {
            HbmFluidNet net = node.getFluidNet();
            popNode(nodeWorld, node);
            rebuildNetworkAfterRemoval(nodeWorld, net);
            markNeighborsChanged(nodeWorld, node.getPos(), node.getFluidType());
        }
    }

    public static void unloadLevel(Level level) {
        FluidNodeWorld nodeWorld = WORLDS.remove(level.dimension());
        if (nodeWorld == null) {
            return;
        }
        for (HbmFluidNode node : new ArrayList<>(nodeWorld.nodes.values())) {
            popNode(nodeWorld, node);
        }
        nodeWorld.nodes.clear();
        nodeWorld.activeFluidNets.clear();
    }

    public static void tick(ServerLevel level) {
        FluidNodeWorld nodeWorld = WORLDS.get(level.dimension());
        if (nodeWorld == null) {
            return;
        }

        pruneUnloadedChunks(level, nodeWorld);

        for (HbmFluidNode node : new ArrayList<>(nodeWorld.nodes.values())) {
            if (!node.hasValidNet() || node.isRecentlyChanged()) {
                checkNodeConnection(nodeWorld, node);
                node.clearRecentlyChanged();
            }
        }

        updateNetworks(nodeWorld);
    }

    public static int getNodeCount(Level level) {
        FluidNodeWorld nodeWorld = WORLDS.get(level.dimension());
        return nodeWorld == null ? 0 : nodeWorld.nodes.size();
    }

    public static int getNetworkCount(Level level) {
        FluidNodeWorld nodeWorld = WORLDS.get(level.dimension());
        return nodeWorld == null ? 0 : nodeWorld.activeFluidNets.size();
    }

    public static int getNetworkLinkCount(Level level, BlockPos pos, FluidType type) {
        HbmFluidNet fluidNet = getFluidNet(level, pos, type);
        return fluidNet == null ? 0 : fluidNet.linkCount();
    }

    public static int getNetworkProviderCount(Level level, BlockPos pos, FluidType type) {
        HbmFluidNet fluidNet = getFluidNet(level, pos, type);
        return fluidNet == null ? 0 : fluidNet.getProviderCount();
    }

    public static int getNetworkReceiverCount(Level level, BlockPos pos, FluidType type) {
        HbmFluidNet fluidNet = getFluidNet(level, pos, type);
        return fluidNet == null ? 0 : fluidNet.getReceiverCount();
    }

    public static long getNetworkFluidTracker(Level level, BlockPos pos, FluidType type) {
        HbmFluidNet fluidNet = getFluidNet(level, pos, type);
        return fluidNet == null ? 0L : fluidNet.getFluidTracker();
    }

    public static boolean hasValidNetwork(Level level, BlockPos pos, FluidType type) {
        HbmFluidNet fluidNet = getFluidNet(level, pos, type);
        return fluidNet != null && fluidNet.isValid();
    }

    public static NetworkDebugSnapshot getNetworkDebugSnapshot(Level level, BlockPos pos, FluidType type) {
        HbmFluidNode node = getNode(level, pos, type);
        if (node == null) {
            return NetworkDebugSnapshot.missing(pos, type);
        }
        HbmFluidNet fluidNet = node.getFluidNet();
        if (fluidNet == null) {
            return NetworkDebugSnapshot.noNetwork(pos, type, describeConnections(node), node.isRecentlyChanged());
        }
        return NetworkDebugSnapshot.present(
                pos,
                type,
                describeConnections(node),
                node.isRecentlyChanged(),
                fluidNet.createDebugSnapshot());
    }

    public static String describeNodeConnections(Level level, BlockPos pos, FluidType type) {
        HbmFluidNode node = getNode(level, pos, type);
        if (node == null) {
            return "none";
        }
        return node.getConnections().stream()
                .map(direction -> direction.getName().toLowerCase())
                .sorted()
                .collect(Collectors.joining(","));
    }

    public static void unloadChunk(Level level, ChunkPos chunkPos) {
        FluidNodeWorld nodeWorld = WORLDS.get(level.dimension());
        if (nodeWorld == null) {
            return;
        }
        ArrayList<NodeKey> toRemove = new ArrayList<>();
        for (NodeKey key : nodeWorld.nodes.keySet()) {
            if (new ChunkPos(key.pos()).equals(chunkPos)) {
                toRemove.add(key);
            }
        }
        for (NodeKey key : toRemove) {
            HbmFluidNode node = nodeWorld.nodes.remove(key);
            if (node != null) {
                HbmFluidNet net = node.getFluidNet();
                popNode(nodeWorld, node);
                rebuildNetworkAfterRemoval(nodeWorld, net);
                markNeighborsChanged(nodeWorld, key.pos(), key.type());
            }
        }
    }

    private static HbmFluidNet getFluidNet(Level level, BlockPos pos, FluidType type) {
        HbmFluidNode node = getNode(level, pos, type);
        return node == null ? null : node.getFluidNet();
    }

    private static String describeConnections(HbmFluidNode node) {
        String connections = node.getConnections().stream()
                .map(direction -> direction.getName().toLowerCase())
                .sorted()
                .collect(Collectors.joining(","));
        return connections.isEmpty() ? "none" : connections;
    }

    private static void updateNetworks(FluidNodeWorld nodeWorld) {
        for (HbmFluidNet net : nodeWorld.activeFluidNets) {
            net.resetTrackers();
        }
        for (HbmFluidNet net : new ArrayList<>(nodeWorld.activeFluidNets)) {
            if (net.isValid()) {
                net.update();
            }
        }

        if (nodeWorld.reapTimer <= 0) {
            Iterator<HbmFluidNet> iterator = nodeWorld.activeFluidNets.iterator();
            while (iterator.hasNext()) {
                HbmFluidNet net = iterator.next();
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

    private static void checkNodeConnection(FluidNodeWorld nodeWorld, HbmFluidNode node) {
        for (Direction direction : node.getConnections()) {
            HbmFluidNode neighbor = nodeWorld.nodes.get(new NodeKey(node.getPos().relative(direction), node.getFluidType()));
            if (neighbor == null || !neighbor.connects(direction.getOpposite())) {
                continue;
            }
            connectToNode(node, neighbor);
        }

        if (!node.hasValidNet()) {
            HbmFluidNet net = new HbmFluidNet(node.getFluidType());
            nodeWorld.activeFluidNets.add(net);
            net.joinLink(node);
        }
    }

    private static void connectToNode(HbmFluidNode origin, HbmFluidNode connection) {
        HbmFluidNet originNet = origin.getFluidNet();
        HbmFluidNet connectionNet = connection.getFluidNet();

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

    private static void markNeighborsChanged(FluidNodeWorld nodeWorld, BlockPos pos, FluidType type) {
        for (Direction direction : Direction.values()) {
            HbmFluidNode neighbor = nodeWorld.nodes.get(new NodeKey(pos.relative(direction), type));
            if (neighbor != null) {
                neighbor.markRecentlyChanged();
            }
        }
    }

    private static void rebuildNetworkAfterRemoval(FluidNodeWorld nodeWorld, HbmFluidNet oldNet) {
        if (oldNet == null) {
            return;
        }
        Set<HbmFluidNode> oldLinks = new LinkedHashSet<>(oldNet.getLinks());
        oldLinks.removeIf(HbmNetworkNode::isExpired);
        oldNet.destroy();
        nodeWorld.activeFluidNets.remove(oldNet);

        for (HbmFluidNode link : oldLinks) {
            if (nodeWorld.nodes.get(new NodeKey(link.getPos(), link.getFluidType())) == link) {
                link.markRecentlyChanged();
            }
        }
    }

    private static void popNode(FluidNodeWorld nodeWorld, HbmFluidNode node) {
        HbmFluidNet net = node.getFluidNet();
        if (net != null) {
            net.leaveLink(node);
            if (net.linkCount() <= 0) {
                net.destroy();
                nodeWorld.activeFluidNets.remove(net);
            }
        }
        node.setExpired(true);
        node.setNet(null);
    }

    private static void removeNetwork(HbmFluidNet net) {
        for (FluidNodeWorld nodeWorld : WORLDS.values()) {
            nodeWorld.activeFluidNets.remove(net);
        }
    }

    private static void pruneUnloadedChunks(ServerLevel level, FluidNodeWorld nodeWorld) {
        ArrayList<NodeKey> toRemove = new ArrayList<>();
        for (NodeKey key : nodeWorld.nodes.keySet()) {
            if (!level.hasChunkAt(key.pos())) {
                toRemove.add(key);
            }
        }
        for (NodeKey key : toRemove) {
            HbmFluidNode node = nodeWorld.nodes.remove(key);
            if (node != null) {
                HbmFluidNet net = node.getFluidNet();
                popNode(nodeWorld, node);
                rebuildNetworkAfterRemoval(nodeWorld, net);
                markNeighborsChanged(nodeWorld, key.pos(), key.type());
            }
        }
    }

    private record NodeKey(BlockPos pos, FluidType type) {
        private NodeKey {
            pos = pos.immutable();
            type = type == null ? HbmFluids.NONE : type;
        }
    }

    private static final class FluidNodeWorld {
        private final Map<NodeKey, HbmFluidNode> nodes = new LinkedHashMap<>();
        private final Set<HbmFluidNet> activeFluidNets = new LinkedHashSet<>();
        private int reapTimer;
    }

    public record NetworkDebugSnapshot(
            BlockPos pos,
            String fluid,
            boolean nodePresent,
            String nodeConnections,
            boolean recentlyChanged,
            boolean networkPresent,
            HbmFluidNet.DebugSnapshot network) {
        private static NetworkDebugSnapshot missing(BlockPos pos, FluidType type) {
            return new NetworkDebugSnapshot(pos, type.getName(), false, "none", false, false, null);
        }

        private static NetworkDebugSnapshot noNetwork(BlockPos pos, FluidType type, String connections, boolean recentlyChanged) {
            return new NetworkDebugSnapshot(pos, type.getName(), true, connections, recentlyChanged, false, null);
        }

        private static NetworkDebugSnapshot present(BlockPos pos, FluidType type, String connections, boolean recentlyChanged, HbmFluidNet.DebugSnapshot network) {
            return new NetworkDebugSnapshot(pos, type.getName(), true, connections, recentlyChanged, true, network);
        }
    }

    private HbmFluidNodespace() {
    }
}
