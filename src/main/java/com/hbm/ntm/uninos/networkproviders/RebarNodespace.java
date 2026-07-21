package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.uninos.HbmNodespace;
import com.hbm.ntm.uninos.HbmUninosDiagnostics;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class RebarNodespace {
    private static final HbmNodespace<BlockPos, RebarNode, RebarNetwork> NODESPACE =
            new HbmNodespace<BlockPos, RebarNode, RebarNetwork>(
                    RebarNode::getPositions,
                    (node, connection) -> connection.pos(),
                    node -> RebarNetworkProvider.THE_PROVIDER,
                    RebarNetwork::resetTrackers,
                    RebarNetwork::update,
                    BlockPos::immutable);

    public static RebarNode getNode(Level level, BlockPos pos) {
        return NODESPACE.getNode(level, pos);
    }

    public static RebarNode createNode(Level level, RebarNode node) {
        return NODESPACE.createNode(level, node);
    }

    public static void destroyNode(Level level, BlockPos pos) {
        NODESPACE.destroyNode(level, pos);
    }

    /** Compatibility equivalent of {@code UniNodespace.destroyNode(World, GenNode)}. */
    public static void destroyNode(Level level, RebarNode node) {
        NODESPACE.destroyNode(level, node);
    }

    public static void unloadLevel(Level level) {
        NODESPACE.unloadLevel(level);
    }

    public static void reapLevel(Level level) {
        NODESPACE.reapLevel(level);
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
        for (RebarNetwork network : NODESPACE.getNetworks(level)) {
            providers += network.getProviderCount();
            receivers += network.getReceiverCount();
        }
        return new HbmUninosDiagnostics.Entry("rebar", NODESPACE.getDiagnostics(level), providers, receivers);
    }

    private RebarNodespace() {
    }
}
