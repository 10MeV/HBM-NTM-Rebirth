package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.uninos.HbmNodespace;
import com.hbm.ntm.uninos.HbmUninosDiagnostics;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class FoundryNodespace {
    private static final HbmNodespace<BlockPos, FoundryNode, FoundryNetwork> NODESPACE =
            new HbmNodespace<BlockPos, FoundryNode, FoundryNetwork>(
                    FoundryNode::getPositions,
                    (node, connection) -> connection.pos(),
                    node -> new FoundryNetwork(),
                    FoundryNetwork::resetTrackers,
                    FoundryNetwork::update,
                    BlockPos::immutable);

    public static FoundryNode getNode(Level level, BlockPos pos) {
        return NODESPACE.getNode(level, pos);
    }

    public static FoundryNode createNode(Level level, FoundryNode node) {
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

    public static HbmUninosDiagnostics.Entry diagnostics(Level level) {
        int providers = 0;
        int receivers = 0;
        for (FoundryNetwork network : NODESPACE.getNetworks(level)) {
            providers += network.getProviderCount();
            receivers += network.getReceiverCount();
        }
        return new HbmUninosDiagnostics.Entry("foundry", NODESPACE.getDiagnostics(level), providers, receivers);
    }

    private FoundryNodespace() {
    }
}
