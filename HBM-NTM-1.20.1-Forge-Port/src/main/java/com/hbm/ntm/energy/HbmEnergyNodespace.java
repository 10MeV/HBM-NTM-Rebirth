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
    private static final Set<HbmPowerNet> ACTIVE_POWER_NETS = new LinkedHashSet<>();
    private static int reapTimer;

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
            popNode(oldNode);
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
            popNode(node);
            markNeighborsChanged(nodeWorld, pos);
        }
    }

    public static void unloadLevel(Level level) {
        EnergyNodeWorld nodeWorld = WORLDS.remove(level.dimension());
        if (nodeWorld == null) {
            return;
        }
        for (HbmEnergyNode node : new ArrayList<>(nodeWorld.nodes.values())) {
            popNode(node);
        }
        nodeWorld.nodes.clear();
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

        updateNetworks();
    }

    private static void updateNetworks() {
        for (HbmPowerNet net : ACTIVE_POWER_NETS) {
            net.resetTrackers();
        }
        for (HbmPowerNet net : new ArrayList<>(ACTIVE_POWER_NETS)) {
            if (net.isValid()) {
                net.update();
            }
        }

        if (reapTimer <= 0) {
            Iterator<HbmPowerNet> iterator = ACTIVE_POWER_NETS.iterator();
            while (iterator.hasNext()) {
                HbmPowerNet net = iterator.next();
                if (!net.isValid() || net.linkCount() <= 0) {
                    net.destroy();
                    iterator.remove();
                }
            }
            reapTimer = 5 * 60 * 20;
        } else {
            reapTimer--;
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
            ACTIVE_POWER_NETS.add(net);
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
                ACTIVE_POWER_NETS.remove(connectionNet);
            } else {
                connectionNet.joinNetwork(originNet);
                ACTIVE_POWER_NETS.remove(originNet);
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

    private static void popNode(HbmEnergyNode node) {
        HbmPowerNet net = node.getPowerNet();
        if (net != null) {
            net.leaveLink(node);
            if (net.linkCount() <= 0) {
                net.destroy();
                ACTIVE_POWER_NETS.remove(net);
            }
        }
        node.setExpired(true);
        node.setNet(null);
    }

    private static final class EnergyNodeWorld {
        private final Map<BlockPos, HbmEnergyNode> nodes = new LinkedHashMap<>();
    }
}
