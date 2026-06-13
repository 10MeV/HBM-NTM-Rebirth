package com.hbm.ntm.fluid;

import com.hbm.ntm.uninos.HbmNodespace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collection;
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

    public static NetworkPressureBalanceSnapshot getNetworkPressureBalanceSnapshot(
            Level level, BlockPos pos, FluidType type) {
        HbmFluidNode node = getNode(level, pos, type);
        if (node == null) {
            return NetworkPressureBalanceSnapshot.missing(pos, normalize(type));
        }
        HbmFluidNet fluidNet = node.getFluidNet();
        if (fluidNet == null) {
            return NetworkPressureBalanceSnapshot.noNetwork(
                    pos, normalize(type), describeConnections(node), node.isRecentlyChanged());
        }
        return NetworkPressureBalanceSnapshot.present(
                pos,
                normalize(type),
                describeConnections(node),
                node.isRecentlyChanged(),
                fluidNet.createPressureBalanceSnapshot());
    }

    public static OverpressureDamageResult damageNetworkFromOverpressure(Level level, BlockPos pos, FluidType type) {
        HbmFluidNode node = getNode(level, pos, type);
        FluidType normalized = normalize(type);
        if (node == null) {
            return OverpressureDamageResult.missing(pos, normalized);
        }
        HbmFluidNet fluidNet = node.getFluidNet();
        if (fluidNet == null) {
            return OverpressureDamageResult.noNetwork(pos, normalized);
        }

        Set<HbmFluidNode> links = fluidNet.getLinks();
        int damagedPipes = HbmFluidOverpressure.damagePipeNodes(level, links);
        int damagedReceivers = fluidNet.damageSubscribedReceiversFromOverpressure();
        return OverpressureDamageResult.present(pos, normalized, links.size(), damagedPipes, damagedReceivers);
    }

    public static OverpressureDamageResult damageNetworkFromOverpressure(Level level, BlockPos pos, FluidType type, int maxPipeNodes) {
        HbmFluidNode node = getNode(level, pos, type);
        FluidType normalized = normalize(type);
        if (node == null) {
            return OverpressureDamageResult.missing(pos, normalized);
        }
        HbmFluidNet fluidNet = node.getFluidNet();
        if (fluidNet == null) {
            return OverpressureDamageResult.noNetwork(pos, normalized);
        }

        Set<HbmFluidNode> links = fluidNet.getLinks();
        int damagedPipes = HbmFluidOverpressure.damagePipeNodes(level, links, maxPipeNodes);
        int damagedReceivers = fluidNet.damageSubscribedReceiversFromOverpressure();
        return OverpressureDamageResult.present(pos, normalized, links.size(), damagedPipes, damagedReceivers);
    }

    public static OverpressureBatchDamageResult damageAllNetworksFromOverpressure(Level level, BlockPos pos) {
        Set<HbmFluidNet> fluidNets = new LinkedHashSet<>();
        int fluidNodes = 0;
        for (FluidType type : HbmFluids.all()) {
            if (type == HbmFluids.NONE) {
                continue;
            }
            HbmFluidNode node = getNode(level, pos, type);
            if (node == null) {
                continue;
            }
            fluidNodes++;
            HbmFluidNet fluidNet = node.getFluidNet();
            if (fluidNet != null) {
                fluidNets.add(fluidNet);
            }
        }
        return damageCollectedNetworksFromOverpressure(level, pos, fluidNodes, fluidNets);
    }

    public static OverpressureBatchDamageResult damageNetworksFromOverpressure(Level level, BlockPos pos, Collection<FluidType> types) {
        if (types == null || types.isEmpty()) {
            return OverpressureBatchDamageResult.missing(pos);
        }
        Set<HbmFluidNet> fluidNets = new LinkedHashSet<>();
        int fluidNodes = 0;
        for (FluidType type : types) {
            FluidType normalized = normalize(type);
            if (normalized == HbmFluids.NONE) {
                continue;
            }
            HbmFluidNode node = getNode(level, pos, normalized);
            if (node == null) {
                continue;
            }
            fluidNodes++;
            HbmFluidNet fluidNet = node.getFluidNet();
            if (fluidNet != null) {
                fluidNets.add(fluidNet);
            }
        }
        return damageCollectedNetworksFromOverpressure(level, pos, fluidNodes, fluidNets);
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

    private static OverpressureBatchDamageResult damageCollectedNetworksFromOverpressure(
            Level level, BlockPos pos, int fluidNodes, Collection<HbmFluidNet> fluidNets) {
        if (fluidNodes <= 0) {
            return OverpressureBatchDamageResult.missing(pos);
        }
        if (fluidNets == null || fluidNets.isEmpty()) {
            return OverpressureBatchDamageResult.noNetworks(pos, fluidNodes);
        }

        Set<HbmFluidNode> pipeNodes = new LinkedHashSet<>();
        Set<BlockEntity> receivers = new LinkedHashSet<>();
        for (HbmFluidNet fluidNet : fluidNets) {
            if (fluidNet == null) {
                continue;
            }
            pipeNodes.addAll(fluidNet.getLinks());
            for (HbmFluidReceiver receiver : fluidNet.getSubscribedReceivers()) {
                if (receiver instanceof BlockEntity blockEntity) {
                    receivers.add(blockEntity);
                }
            }
        }

        int damagedPipes = HbmFluidOverpressure.damagePipeNodes(level, pipeNodes);
        int damagedReceivers = HbmFluidOverpressure.damageReceivers(receivers);
        return OverpressureBatchDamageResult.present(
                pos,
                fluidNodes,
                fluidNets.size(),
                pipeNodes.size(),
                damagedPipes,
                receivers.size(),
                damagedReceivers);
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

    public record NetworkPressureBalanceSnapshot(
            BlockPos pos,
            String fluid,
            boolean nodePresent,
            String nodeConnections,
            boolean recentlyChanged,
            boolean networkPresent,
            HbmFluidNet.PressureBalanceSnapshot network) {
        private static NetworkPressureBalanceSnapshot missing(BlockPos pos, FluidType type) {
            return new NetworkPressureBalanceSnapshot(pos, type.getName(), false, "none", false, false, null);
        }

        private static NetworkPressureBalanceSnapshot noNetwork(
                BlockPos pos, FluidType type, String connections, boolean recentlyChanged) {
            return new NetworkPressureBalanceSnapshot(
                    pos, type.getName(), true, connections, recentlyChanged, false, null);
        }

        private static NetworkPressureBalanceSnapshot present(
                BlockPos pos,
                FluidType type,
                String connections,
                boolean recentlyChanged,
                HbmFluidNet.PressureBalanceSnapshot network) {
            return new NetworkPressureBalanceSnapshot(
                    pos, type.getName(), true, connections, recentlyChanged, true, network);
        }
    }

    public record OverpressureDamageResult(
            BlockPos pos,
            String fluid,
            boolean nodePresent,
            boolean networkPresent,
            int pipeNodes,
            int pipeNodesDamaged,
            int receiversDamaged) {
        private static OverpressureDamageResult missing(BlockPos pos, FluidType type) {
            return new OverpressureDamageResult(pos, type.getName(), false, false, 0, 0, 0);
        }

        private static OverpressureDamageResult noNetwork(BlockPos pos, FluidType type) {
            return new OverpressureDamageResult(pos, type.getName(), true, false, 0, 0, 0);
        }

        private static OverpressureDamageResult present(BlockPos pos, FluidType type, int pipeNodes, int pipeNodesDamaged, int receiversDamaged) {
            return new OverpressureDamageResult(pos, type.getName(), true, true, pipeNodes, pipeNodesDamaged, receiversDamaged);
        }
    }

    public record OverpressureBatchDamageResult(
            BlockPos pos,
            boolean nodePresent,
            boolean networkPresent,
            int fluidNodes,
            int networks,
            int pipeNodes,
            int pipeNodesDamaged,
            int receivers,
            int receiversDamaged) {
        private static OverpressureBatchDamageResult missing(BlockPos pos) {
            return new OverpressureBatchDamageResult(pos, false, false, 0, 0, 0, 0, 0, 0);
        }

        private static OverpressureBatchDamageResult noNetworks(BlockPos pos, int fluidNodes) {
            return new OverpressureBatchDamageResult(pos, true, false, fluidNodes, 0, 0, 0, 0, 0);
        }

        private static OverpressureBatchDamageResult present(
                BlockPos pos,
                int fluidNodes,
                int networks,
                int pipeNodes,
                int pipeNodesDamaged,
                int receivers,
                int receiversDamaged) {
            return new OverpressureBatchDamageResult(
                    pos, true, true, fluidNodes, networks, pipeNodes, pipeNodesDamaged, receivers, receiversDamaged);
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
