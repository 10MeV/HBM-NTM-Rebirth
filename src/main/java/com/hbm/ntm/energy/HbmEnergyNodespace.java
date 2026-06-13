package com.hbm.ntm.energy;

import com.hbm.ntm.uninos.HbmNodespace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.stream.Collectors;

public final class HbmEnergyNodespace {
    private static final HbmNodespace<BlockPos, HbmEnergyNode, HbmPowerNet> NODESPACE =
            new HbmNodespace<BlockPos, HbmEnergyNode, HbmPowerNet>(
            HbmEnergyNode::getPositions,
            (node, connection) -> connection.pos(),
            node -> new PowerNetMK2(),
            HbmPowerNet::resetTrackers,
            HbmPowerNet::update,
            BlockPos::immutable);

    private HbmEnergyNodespace() {
    }

    public static HbmEnergyNode getNode(Level level, BlockPos pos) {
        if (!isLoadedBlock(level, pos)) {
            return null;
        }
        return NODESPACE.getNode(level, pos);
    }

    public static HbmEnergyNode createNode(Level level, HbmEnergyNode node) {
        return NODESPACE.createNode(level, node);
    }

    public static void destroyNode(Level level, BlockPos pos) {
        NODESPACE.destroyNode(level, pos);
    }

    public static void destroyNode(Level level, HbmEnergyNode node) {
        if (level == null || node == null) {
            return;
        }
        for (BlockPos pos : node.getPositions()) {
            NODESPACE.destroyNode(level, pos);
            return;
        }
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
        for (HbmPowerNet net : NODESPACE.getNetworks(level)) {
            HbmPowerNet.DebugSnapshot snapshot = net.createDebugSnapshot();
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

    public static ChunkDiagnostics getChunkDiagnostics(Level level, ChunkPos chunkPos) {
        HbmNodespace.ChunkDiagnostics core = NODESPACE.getChunkDiagnostics(level, chunkPos);
        int providerEntries = 0;
        int receiverEntries = 0;
        for (HbmPowerNet net : NODESPACE.getNetworks(level)) {
            boolean touchesChunk = false;
            for (HbmEnergyNode node : net.getLinks()) {
                for (BlockPos nodePos : node.getPositions()) {
                    if (new ChunkPos(nodePos).equals(chunkPos)) {
                        touchesChunk = true;
                        break;
                    }
                }
                if (touchesChunk) {
                    break;
                }
            }
            if (!touchesChunk) {
                continue;
            }
            HbmPowerNet.DebugSnapshot snapshot = net.createDebugSnapshot();
            providerEntries += snapshot.providers();
            receiverEntries += snapshot.receivers();
        }

        return new ChunkDiagnostics(
                chunkPos,
                level != null && chunkPos != null && level.hasChunk(chunkPos.x, chunkPos.z),
                core.nodePositions(),
                core.uniqueNodes(),
                core.networks(),
                core.invalidNetworks(),
                core.linkRefs(),
                core.dirtyNodes(),
                core.expiredNodes(),
                core.orphanNodes(),
                providerEntries,
                receiverEntries);
    }

    public static ForceRebuildResult forceRebuild(Level level) {
        HbmNodespace.ForceRebuildResult result = NODESPACE.forceRebuild(level);
        return new ForceRebuildResult(result.nodes(), result.oldNetworks(), result.newNetworks(), result.reapTimer());
    }

    public static boolean markNodeAndNeighborsChanged(Level level, BlockPos pos) {
        if (!isLoadedBlock(level, pos)) {
            return false;
        }
        boolean marked = NODESPACE.markNodeAndConnectionNeighborsChanged(level, pos);
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (!isLoadedBlock(level, neighborPos)) {
                continue;
            }
            HbmEnergyNode neighbor = NODESPACE.getNode(level, neighborPos);
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
        NODESPACE.unloadChunk(level, chunkPos);
    }

    private static HbmPowerNet getPowerNet(Level level, BlockPos pos) {
        if (!isLoadedBlock(level, pos)) {
            return null;
        }
        return NODESPACE.getNetwork(level, pos);
    }

    private static boolean isLoadedBlock(Level level, BlockPos pos) {
        return level != null && pos != null && level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private static String describeConnections(HbmEnergyNode node) {
        String connections = node.getConnections().stream()
                .map(direction -> direction.getName().toLowerCase())
                .sorted()
                .collect(Collectors.joining(","));
        return connections.isEmpty() ? "none" : connections;
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

    public record ChunkDiagnostics(
            ChunkPos chunkPos,
            boolean loaded,
            int nodePositions,
            int uniqueNodes,
            int networks,
            int invalidNetworks,
            int linkRefs,
            int dirtyNodes,
            int expiredNodes,
            int orphanNodes,
            int providerEntries,
            int receiverEntries) {
    }

    public record ForceRebuildResult(
            int nodes,
            int oldNetworks,
            int newNetworks,
            int reapTimer) {
    }
}
