package com.hbm.ntm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
        HbmEnergyNode oldNode = nodeWorld.nodes.put(node.getPos(), node);
        if (oldNode != null && oldNode != node) {
            HbmPowerNet oldNet = oldNode.getPowerNet();
            popNode(nodeWorld, oldNode);
            rebuildNetworkAfterRemoval(nodeWorld, oldNet);
        }
        checkNodeConnection(nodeWorld, node);
        return node;
    }

    public static void destroyNode(Level level, BlockPos pos) {
        EnergyNodeWorld nodeWorld = WORLDS.get(level.dimension());
        if (nodeWorld == null) {
            return;
        }
        HbmEnergyNode node = nodeWorld.nodes.remove(pos);
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

        for (HbmEnergyNode node : new ArrayList<>(nodeWorld.nodes.values())) {
            if (!node.hasValidNet() || node.isRecentlyChanged()) {
                checkNodeConnection(nodeWorld, node);
                node.clearRecentlyChanged();
            }
        }

        updateNetworks(nodeWorld);
    }

    public static int getNodeCount(Level level) {
        EnergyNodeWorld nodeWorld = WORLDS.get(level.dimension());
        return nodeWorld == null ? 0 : nodeWorld.nodes.size();
    }

    public static int getNetworkCount(Level level) {
        EnergyNodeWorld nodeWorld = WORLDS.get(level.dimension());
        return nodeWorld == null ? 0 : nodeWorld.activePowerNets.size();
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
        for (Direction direction : node.getConnections()) {
            HbmEnergyNode neighbor = nodeWorld.nodes.get(node.getPos().relative(direction));
            if (neighbor == null || !neighbor.connects(direction.getOpposite())) {
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
            if (nodeWorld.nodes.get(link.getPos()) == link) {
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
    }

    private static void removeNetwork(HbmPowerNet net) {
        for (EnergyNodeWorld nodeWorld : WORLDS.values()) {
            nodeWorld.activePowerNets.remove(net);
        }
    }

    private static final class EnergyNodeWorld {
        private final Map<BlockPos, HbmEnergyNode> nodes = new LinkedHashMap<>();
        private final Set<HbmPowerNet> activePowerNets = new LinkedHashSet<>();
        private int reapTimer;
    }
}
