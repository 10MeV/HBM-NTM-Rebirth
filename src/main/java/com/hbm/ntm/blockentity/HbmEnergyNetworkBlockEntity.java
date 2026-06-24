package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.energy.HbmEnergyConnectionUtil;
import com.hbm.ntm.energy.HbmEnergyConnector;
import com.hbm.ntm.energy.HbmEnergyNodeHost;
import com.hbm.ntm.energy.HbmEnergyNodespace;
import com.hbm.ntm.energy.HbmEnergyProvider;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.energy.HbmPowerNet;
import com.hbm.ntm.energy.HbmNetworkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class HbmEnergyNetworkBlockEntity extends HbmEnergyBlockEntity implements HbmEnergyNodeHost, HbmEnergyConnector {
    private HbmEnergyNode energyNode;

    protected HbmEnergyNetworkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, HbmEnergyStorage energy) {
        super(type, pos, state, energy);
    }

    public static <T extends HbmEnergyNetworkBlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (!level.isClientSide) {
            blockEntity.refreshEnergyNodeState();
            blockEntity.refreshEnergyNetworkSubscriptions();
        }
    }

    @Override
    public HbmEnergyNode getEnergyNode() {
        return energyNode;
    }

    public HbmPowerNet getPowerNet() {
        return energyNode == null ? null : energyNode.getPowerNet();
    }

    protected Set<Direction> getEnergyConnections() {
        return level == null
                ? Set.of()
                : HbmEnergyConnectionUtil.collectNodeConnections(level, worldPosition, this);
    }

    @Override
    public boolean canConnectEnergy(Direction side) {
        return canAccessEnergy(side);
    }

    protected HbmEnergyNode createEnergyNode() {
        return shouldUseRemotePortEnergyNode()
                ? createRemotePortEnergyNode()
                : new HbmEnergyNode(worldPosition, getEnergyConnections());
    }

    protected HbmEnergyNode createRemotePortEnergyNode() {
        Set<BlockPos> positions = new LinkedHashSet<>();
        positions.add(worldPosition.immutable());
        Set<HbmNetworkNode.NodeConnection> connections = new LinkedHashSet<>();
        Iterable<EnergyPort> ports = getNetworkEnergyPorts();
        if (ports == null) {
            return HbmEnergyNode.withConnectionPoints(positions, connections);
        }
        for (EnergyPort port : ports) {
            if (port == null) {
                continue;
            }
            if (!HbmEnergyUtil.isLoadedPort(level, worldPosition, port)) {
                continue;
            }
            BlockPos connectorPos = port.conductorPos(worldPosition);
            positions.add(connectorPos.relative(port.direction().getOpposite()));
            connections.add(new HbmNetworkNode.NodeConnection(connectorPos, port.direction()));
        }
        return HbmEnergyNode.withConnectionPoints(positions, connections);
    }

    protected boolean shouldUseRemotePortEnergyNode() {
        return false;
    }

    @Override
    public void refreshEnergyNode() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (!shouldCreateEnergyNode()) {
            removeEnergyNode();
            return;
        }
        if (energyNode != null) {
            HbmEnergyNodespace.destroyNode(level, worldPosition);
            energyNode = null;
        }
        energyNode = HbmEnergyNodespace.createNode(level, createEnergyNode());
    }

    protected void refreshEnergyNetworkSubscriptions() {
        if (level == null || level.isClientSide) {
            return;
        }
        HbmPowerNet powerNet = getPowerNet();
        boolean hasLocalNet = powerNet != null && powerNet.isValid();
        if (shouldSubscribeAsProvider()) {
            if (hasLocalNet) {
                powerNet.addProvider(getNetworkEnergyProvider());
            }
            HbmEnergyUtil.subscribeProviderToPorts(level, worldPosition, getNetworkEnergyPorts(), getNetworkEnergyProvider());
        }
        if (shouldSubscribeAsReceiver()) {
            if (hasLocalNet) {
                powerNet.addReceiver(getNetworkEnergyReceiver());
            }
            HbmEnergyUtil.subscribeReceiverToPorts(level, worldPosition, getNetworkEnergyPorts(), getNetworkEnergyReceiver());
        }
    }

    protected void refreshEnergyNodeState() {
        if (level == null || level.isClientSide) {
            return;
        }
        boolean shouldCreateNode = shouldCreateEnergyNode();
        boolean hasNode = energyNode != null && !energyNode.isExpired();
        if (!shouldCreateNode) {
            if (energyNode != null) {
                removeEnergyNode();
            }
            return;
        }
        if (!hasNode) {
            refreshEnergyNode();
            return;
        }

        HbmEnergyNode currentShape = createEnergyNode();
        if (!energyNode.getPositions().equals(currentShape.getPositions())
                || !energyNode.getConnectionPoints().equals(currentShape.getConnectionPoints())) {
            refreshEnergyNode();
        }
    }

    protected boolean shouldCreateEnergyNode() {
        return true;
    }

    protected boolean shouldSubscribeAsProvider() {
        return false;
    }

    protected boolean shouldSubscribeAsReceiver() {
        return false;
    }

    protected HbmEnergyProvider getNetworkEnergyProvider() {
        return energy;
    }

    protected HbmEnergyReceiver getNetworkEnergyReceiver() {
        return energy;
    }

    protected Iterable<EnergyPort> getNetworkEnergyPorts() {
        return getEnergyPorts();
    }

    protected boolean subscribeEnergyProvider(HbmEnergyProvider provider) {
        HbmPowerNet powerNet = getPowerNet();
        if (provider == null || powerNet == null || !powerNet.isValid()) {
            return false;
        }
        powerNet.addProvider(provider);
        return true;
    }

    protected boolean subscribeEnergyReceiver(HbmEnergyReceiver receiver) {
        HbmPowerNet powerNet = getPowerNet();
        if (receiver == null || powerNet == null || !powerNet.isValid()) {
            return false;
        }
        powerNet.addReceiver(receiver);
        return true;
    }

    @Override
    public void removeEnergyNode() {
        if (level == null || level.isClientSide) {
            energyNode = null;
            return;
        }
        HbmEnergyNodespace.destroyNode(level, worldPosition);
        energyNode = null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshEnergyNodeState();
    }

    @Override
    public void setRemoved() {
        removeEnergyNode();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        removeEnergyNode();
        super.onChunkUnloaded();
    }
}
