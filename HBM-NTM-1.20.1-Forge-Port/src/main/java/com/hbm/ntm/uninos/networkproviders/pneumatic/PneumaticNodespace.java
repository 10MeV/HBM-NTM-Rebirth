package com.hbm.ntm.uninos.networkproviders.pneumatic;

import com.hbm.ntm.uninos.HbmNodespace;
import com.hbm.ntm.uninos.HbmUninosDiagnostics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.stream.Collectors;

public final class PneumaticNodespace {
    private static final HbmNodespace<BlockPos, PneumaticNode, PneumaticNetwork> NODESPACE =
            new HbmNodespace<BlockPos, PneumaticNode, PneumaticNetwork>(
                    PneumaticNode::getPositions,
                    (node, connection) -> connection.pos(),
                    PneumaticNetworkProvider.THE_PROVIDER,
                    PneumaticNetwork::resetTrackers,
                    PneumaticNetwork::update,
                    BlockPos::immutable);

    public static PneumaticNode getNode(Level level, BlockPos pos) {
        return NODESPACE.getNode(level, pos);
    }

    public static PneumaticNetwork getNetwork(Level level, BlockPos pos) {
        return NODESPACE.getNetwork(level, pos);
    }

    public static PneumaticNode createNode(Level level, PneumaticNode node) {
        return NODESPACE.createNode(level, node);
    }

    public static void destroyNode(Level level, BlockPos pos) {
        NODESPACE.destroyNode(level, pos);
    }

    public static void unloadLevel(Level level) {
        NODESPACE.unloadLevel(level);
    }

    public static void unloadChunk(Level level, ChunkPos chunkPos) {
        NODESPACE.unloadChunk(level, chunkPos);
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

    public static NetworkDebugSnapshot getNetworkDebugSnapshot(Level level, BlockPos pos) {
        PneumaticNode node = getNode(level, pos);
        if (node == null) {
            return NetworkDebugSnapshot.missing(pos);
        }
        PneumaticNetwork network = node.getPneumaticNet();
        if (network == null) {
            return NetworkDebugSnapshot.noNetwork(pos, describeConnections(node), node.isRecentlyChanged());
        }
        return NetworkDebugSnapshot.present(
                pos,
                describeConnections(node),
                node.isRecentlyChanged(),
                network.createDebugSnapshot());
    }

    public static boolean hasValidNetwork(Level level, BlockPos pos) {
        PneumaticNetwork network = getNetwork(level, pos);
        return network != null && network.isValid();
    }

    public static String describeNodeConnections(Level level, BlockPos pos) {
        PneumaticNode node = getNode(level, pos);
        if (node == null) {
            return "none";
        }
        return describeConnections(node);
    }

    public static HbmUninosDiagnostics.Entry diagnostics(Level level) {
        int receivers = 0;
        int accessors = 0;
        int storages = 0;
        for (PneumaticNetwork network : NODESPACE.getNetworks(level)) {
            PneumaticNetwork.DebugSnapshot snapshot = network.createDebugSnapshot();
            receivers += snapshot.receivers();
            accessors += snapshot.accessors();
            storages += snapshot.storages();
        }
        return new HbmUninosDiagnostics.Entry("pneumatic", NODESPACE.getDiagnostics(level), storages + accessors, receivers);
    }

    private static String describeConnections(PneumaticNode node) {
        String connections = node.getConnections().stream()
                .map(Direction::getName)
                .map(String::toLowerCase)
                .sorted()
                .collect(Collectors.joining(","));
        return connections.isEmpty() ? "none" : connections;
    }

    private PneumaticNodespace() {
    }

    public record NetworkDebugSnapshot(
            BlockPos pos,
            boolean nodePresent,
            String nodeConnections,
            boolean recentlyChanged,
            boolean networkPresent,
            PneumaticNetwork.DebugSnapshot network) {
        private static NetworkDebugSnapshot missing(BlockPos pos) {
            return new NetworkDebugSnapshot(pos, false, "none", false, false, null);
        }

        private static NetworkDebugSnapshot noNetwork(BlockPos pos, String connections, boolean recentlyChanged) {
            return new NetworkDebugSnapshot(pos, true, connections, recentlyChanged, false, null);
        }

        private static NetworkDebugSnapshot present(BlockPos pos, String connections, boolean recentlyChanged, PneumaticNetwork.DebugSnapshot network) {
            return new NetworkDebugSnapshot(pos, true, connections, recentlyChanged, true, network);
        }
    }
}
