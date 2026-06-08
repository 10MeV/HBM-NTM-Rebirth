package com.hbm.ntm.fluid;

import com.hbm.ntm.uninos.HbmNodespace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class HbmFluidNodespace {
    private static final HbmNodespace<NodeKey, HbmFluidNode, HbmFluidNet> NODESPACE =
            new HbmNodespace<NodeKey, HbmFluidNode, HbmFluidNet>(
            HbmFluidNodespace::keysForNode,
            (node, connection) -> new NodeKey(connection.pos(), node.getFluidType()),
            node -> new HbmFluidNet(node.getFluidType()),
            HbmFluidNet::resetTrackers,
            HbmFluidNet::update,
            NodeKey::pos);

    public static HbmFluidNode getNode(Level level, BlockPos pos, FluidType type) {
        return NODESPACE.getNode(level, new NodeKey(pos, type));
    }

    public static HbmFluidNode createNode(Level level, HbmFluidNode node) {
        return NODESPACE.createNode(level, node);
    }

    public static void destroyNode(Level level, BlockPos pos, FluidType type) {
        NODESPACE.destroyNode(level, new NodeKey(pos, type));
    }

    public static void unloadLevel(Level level) {
        NODESPACE.unloadLevel(level);
    }

    public static void tick(ServerLevel level) {
        NODESPACE.tick(level);
    }

    public static int getNodeCount(Level level) {
        return NODESPACE.getUniqueNodeCount(level);
    }

    public static int getNetworkCount(Level level) {
        return NODESPACE.getNetworkCount(level);
    }

    public static Diagnostics getDiagnostics(Level level) {
        HbmNodespace.Diagnostics core = NODESPACE.getDiagnostics(level);
        int providerEntries = 0;
        int receiverEntries = 0;
        for (HbmFluidNet net : NODESPACE.getNetworks(level)) {
            HbmFluidNet.DebugSnapshot snapshot = net.createDebugSnapshot();
            providerEntries += snapshot.providers();
            receiverEntries += snapshot.receivers();
        }
        return new Diagnostics(
                core.nodePositions(),
                core.uniqueNodes(),
                core.networks(),
                core.invalidNetworks(),
                core.linkRefs(),
                core.dirtyNodes(),
                core.expiredNodes(),
                core.orphanNodes(),
                providerEntries,
                receiverEntries,
                core.reapTimer());
    }

    public static ForceRebuildResult forceRebuild(Level level) {
        HbmNodespace.ForceRebuildResult result = NODESPACE.forceRebuild(level);
        return new ForceRebuildResult(result.nodes(), result.oldNetworks(), result.newNetworks(), result.reapTimer());
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
            return NetworkDebugSnapshot.missing(pos, normalize(type));
        }
        HbmFluidNet fluidNet = node.getFluidNet();
        if (fluidNet == null) {
            return NetworkDebugSnapshot.noNetwork(pos, normalize(type), describeConnections(node), node.isRecentlyChanged());
        }
        return NetworkDebugSnapshot.present(
                pos,
                normalize(type),
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
        NODESPACE.unloadChunk(level, chunkPos);
    }

    private static HbmFluidNet getFluidNet(Level level, BlockPos pos, FluidType type) {
        return NODESPACE.getNetwork(level, new NodeKey(pos, type));
    }

    private static String describeConnections(HbmFluidNode node) {
        String connections = node.getConnections().stream()
                .map(direction -> direction.getName().toLowerCase())
                .sorted()
                .collect(Collectors.joining(","));
        return connections.isEmpty() ? "none" : connections;
    }

    private static Set<NodeKey> keysForNode(HbmFluidNode node) {
        Set<NodeKey> keys = new LinkedHashSet<>();
        for (BlockPos pos : node.getPositions()) {
            keys.add(new NodeKey(pos, node.getFluidType()));
        }
        return keys;
    }

    private static FluidType normalize(FluidType type) {
        return type == null ? HbmFluids.NONE : type;
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
    }

    public record ForceRebuildResult(
            int nodes,
            int oldNetworks,
            int newNetworks,
            int reapTimer) {
    }

    private record NodeKey(BlockPos pos, FluidType type) {
        private NodeKey {
            pos = pos.immutable();
            type = normalize(type);
        }
    }

    private HbmFluidNodespace() {
    }
}
