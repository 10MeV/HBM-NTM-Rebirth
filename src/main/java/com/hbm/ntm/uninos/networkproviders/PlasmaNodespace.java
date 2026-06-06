package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.uninos.HbmNodespace;
import com.hbm.ntm.uninos.HbmUninosDiagnostics;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class PlasmaNodespace {
    private static final HbmNodespace<BlockPos, PlasmaNode, PlasmaNetwork> NODESPACE =
            new HbmNodespace<BlockPos, PlasmaNode, PlasmaNetwork>(
                    PlasmaNode::getPositions,
                    (node, connection) -> connection.pos(),
                    node -> new PlasmaNetwork(),
                    PlasmaNetwork::resetTrackers,
                    PlasmaNetwork::update,
                    BlockPos::immutable);

    public static PlasmaNode getNode(Level level, BlockPos pos) {
        return NODESPACE.getNode(level, pos);
    }

    public static PlasmaNode createNode(Level level, PlasmaNode node) {
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
        for (PlasmaNetwork network : NODESPACE.getNetworks(level)) {
            providers += network.getProviderCount();
            receivers += network.getReceiverCount();
        }
        return new HbmUninosDiagnostics.Entry("plasma", NODESPACE.getDiagnostics(level), providers, receivers);
    }

    private PlasmaNodespace() {
    }
}
