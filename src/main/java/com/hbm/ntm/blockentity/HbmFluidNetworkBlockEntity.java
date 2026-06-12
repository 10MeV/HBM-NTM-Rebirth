package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmNetworkNode;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnectionUtil;
import com.hbm.ntm.fluid.HbmFluidConnector;
import com.hbm.ntm.fluid.HbmFluidNet;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.fluid.HbmFluidProvider;
import com.hbm.ntm.fluid.HbmFluidReceiver;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmFluidUtil;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.LinkedHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class HbmFluidNetworkBlockEntity extends HbmFluidBlockEntity implements HbmFluidConnector {
    private final Set<FluidType> networkProviderSubscriptions = new HashSet<>();
    private final Set<FluidType> networkReceiverSubscriptions = new HashSet<>();

    protected HbmFluidNetworkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, java.util.List<HbmFluidTank> tanks) {
        super(type, pos, state, tanks);
    }

    public static <T extends HbmFluidNetworkBlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (!level.isClientSide) {
            blockEntity.refreshFluidNodeState();
            blockEntity.refreshFluidNetworkSubscriptions();
        }
    }

    @Override
    public HbmFluidNode getFluidNode() {
        return super.getFluidNode();
    }

    public HbmFluidNet getFluidNet() {
        HbmFluidNode node = getFluidNode();
        return node == null ? null : node.getFluidNet();
    }

    public HbmFluidNet getFluidNet(FluidType type) {
        HbmFluidNode node = getFluidNode(type);
        return node == null ? null : node.getFluidNet();
    }

    protected Set<Direction> getFluidConnections(FluidType type) {
        return level == null
                ? Set.of()
                : HbmFluidConnectionUtil.collectNodeConnections(level, worldPosition, type, this);
    }

    protected List<FluidType> getFluidNodeTypes() {
        return getAllTanks().stream()
                .map(HbmFluidTank::getTankType)
                .filter(type -> type != HbmFluids.NONE)
                .distinct()
                .toList();
    }

    protected HbmFluidNode createRemotePortFluidNode(FluidType type) {
        Set<BlockPos> positions = new LinkedHashSet<>();
        positions.add(worldPosition.immutable());
        Set<HbmNetworkNode.NodeConnection> connections = new LinkedHashSet<>();
        for (FluidPort port : getFluidPorts()) {
            if (port == null) {
                continue;
            }
            if (!HbmFluidUtil.isLoadedPort(level, worldPosition, port)) {
                continue;
            }
            BlockPos connectorPos = port.connectorPos(worldPosition);
            positions.add(connectorPos.relative(port.direction().getOpposite()));
            connections.add(new HbmNetworkNode.NodeConnection(connectorPos, port.direction()));
        }
        return HbmFluidNode.withConnectionPoints(positions, type, connections);
    }

    protected boolean shouldUseRemotePortFluidNode(FluidType type) {
        return false;
    }

    protected void refreshFluidNodeState() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (!shouldCreateFluidNode()) {
            removeFluidNode();
            return;
        }
        for (FluidType type : getFluidNodeTypes()) {
            HbmFluidNode existing = getFluidNode(type);
            if (existing == null || existing.isExpired()) {
                HbmFluidNode node = HbmFluidNodespace.createNode(level, createFluidNode(type));
                setFluidNode(node);
            }
        }
        removeObsoleteFluidNodes();
    }

    protected boolean shouldCreateFluidNode() {
        return true;
    }

    @Override
    public void onFluidSettingsPasted() {
        super.onFluidSettingsPasted();
        refreshFluidNodeState();
    }

    protected boolean shouldSubscribeAsFluidProvider() {
        return false;
    }

    protected boolean shouldSubscribeAsFluidReceiver() {
        return false;
    }

    protected HbmFluidProvider getNetworkFluidProvider() {
        return this instanceof HbmFluidProvider provider ? provider : null;
    }

    protected HbmFluidReceiver getNetworkFluidReceiver() {
        return this instanceof HbmFluidReceiver receiver ? receiver : null;
    }

    protected NetworkFluidSubscriptionReport refreshFluidNetworkSubscriptions() {
        if (level == null || level.isClientSide) {
            return NetworkFluidSubscriptionReport.empty();
        }
        List<FluidType> nodeTypes = getFluidNodeTypes();
        Set<FluidType> activeProviderTypes = new HashSet<>();
        Set<FluidType> activeReceiverTypes = new HashSet<>();
        for (FluidType type : nodeTypes) {
            if (shouldSubscribeAsFluidProvider(type)) {
                activeProviderTypes.add(type);
            }
            if (shouldSubscribeAsFluidReceiver(type)) {
                activeReceiverTypes.add(type);
            }
        }

        HbmFluidProvider provider = getNetworkFluidProvider();
        HbmFluidReceiver receiver = getNetworkFluidReceiver();
        NetworkFluidSubscriptionDetachReport providerDetach =
                detachObsoleteNetworkProviderSubscriptions(activeProviderTypes, provider);
        NetworkFluidSubscriptionDetachReport receiverDetach =
                detachObsoleteNetworkReceiverSubscriptions(activeReceiverTypes, receiver);

        int localProviderSubscriptions = 0;
        int localReceiverSubscriptions = 0;
        int remoteProviderPorts = 0;
        int remoteReceiverPorts = 0;
        for (FluidType type : nodeTypes) {
            HbmFluidNet fluidNet = getFluidNet(type);
            boolean hasLocalNet = fluidNet != null && fluidNet.isValid();
            if (activeProviderTypes.contains(type)) {
                if (hasLocalNet && provider != null) {
                    fluidNet.addProvider(provider);
                    localProviderSubscriptions++;
                }
                remoteProviderPorts += HbmFluidUtil.subscribeProviderToPorts(
                        level, worldPosition, getNetworkFluidPorts(type), type, provider);
            }
            if (activeReceiverTypes.contains(type)) {
                if (hasLocalNet && receiver != null) {
                    fluidNet.addReceiver(receiver);
                    localReceiverSubscriptions++;
                }
                remoteReceiverPorts += HbmFluidUtil.subscribeReceiverToPorts(
                        level, worldPosition, getNetworkFluidPorts(type), type, receiver);
            }
        }

        networkProviderSubscriptions.clear();
        networkProviderSubscriptions.addAll(activeProviderTypes);
        networkReceiverSubscriptions.clear();
        networkReceiverSubscriptions.addAll(activeReceiverTypes);
        return new NetworkFluidSubscriptionReport(
                activeProviderTypes.size(),
                activeReceiverTypes.size(),
                localProviderSubscriptions,
                localReceiverSubscriptions,
                remoteProviderPorts,
                remoteReceiverPorts,
                providerDetach.types(),
                providerDetach.ports(),
                receiverDetach.types(),
                receiverDetach.ports());
    }

    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return shouldSubscribeAsFluidProvider();
    }

    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return shouldSubscribeAsFluidReceiver();
    }

    protected Iterable<FluidPort> getNetworkFluidPorts(FluidType type) {
        return getFluidPorts();
    }

    private NetworkFluidSubscriptionDetachReport detachObsoleteNetworkProviderSubscriptions(
            Set<FluidType> activeTypes, HbmFluidProvider provider) {
        Set<FluidType> staleTypes = new HashSet<>(networkProviderSubscriptions);
        staleTypes.removeAll(activeTypes);
        int detachedTypes = 0;
        int detachedPorts = 0;
        for (FluidType type : staleTypes) {
            HbmFluidNet fluidNet = getFluidNet(type);
            if (fluidNet != null && provider != null) {
                if (fluidNet.isProvider(provider)) {
                    detachedTypes++;
                }
                fluidNet.removeProvider(provider);
            }
            detachedPorts += HbmFluidUtil.unsubscribeProviderFromPorts(
                    level, worldPosition, getNetworkFluidPorts(type), type, provider);
        }
        return new NetworkFluidSubscriptionDetachReport(staleTypes.size(), detachedTypes, detachedPorts);
    }

    private NetworkFluidSubscriptionDetachReport detachObsoleteNetworkReceiverSubscriptions(
            Set<FluidType> activeTypes, HbmFluidReceiver receiver) {
        Set<FluidType> staleTypes = new HashSet<>(networkReceiverSubscriptions);
        staleTypes.removeAll(activeTypes);
        int detachedTypes = 0;
        int detachedPorts = 0;
        for (FluidType type : staleTypes) {
            HbmFluidNet fluidNet = getFluidNet(type);
            if (fluidNet != null && receiver != null) {
                if (fluidNet.isSubscribed(receiver)) {
                    detachedTypes++;
                }
                fluidNet.removeReceiver(receiver);
            }
            detachedPorts += HbmFluidUtil.unsubscribeReceiverFromPorts(
                    level, worldPosition, getNetworkFluidPorts(type), type, receiver);
        }
        return new NetworkFluidSubscriptionDetachReport(staleTypes.size(), detachedTypes, detachedPorts);
    }

    private void clearNetworkFluidSubscriptions() {
        detachObsoleteNetworkProviderSubscriptions(Set.of(), getNetworkFluidProvider());
        detachObsoleteNetworkReceiverSubscriptions(Set.of(), getNetworkFluidReceiver());
        networkProviderSubscriptions.clear();
        networkReceiverSubscriptions.clear();
    }

    private void removeObsoleteFluidNodes() {
        Set<FluidType> activeTypes = new HashSet<>(getFluidNodeTypes());
        for (FluidType type : getTrackedFluidNodeTypes()) {
            if (!activeTypes.contains(type)) {
                HbmFluidNodespace.destroyNode(level, worldPosition, type);
                removeFluidNode(type);
            }
        }
    }

    protected HbmFluidNode createFluidNode(FluidType type) {
        return shouldUseRemotePortFluidNode(type)
                ? createRemotePortFluidNode(type)
                : new HbmFluidNode(worldPosition, type, getFluidConnections(type));
    }

    @Override
    public void removeFluidNode() {
        clearNetworkFluidSubscriptions();
        if (level == null || level.isClientSide) {
            super.removeFluidNode();
            return;
        }
        for (FluidType type : getTrackedFluidNodeTypes()) {
            HbmFluidNodespace.destroyNode(level, worldPosition, type);
        }
        super.removeFluidNode();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshFluidNodeState();
    }

    @Override
    public void setRemoved() {
        removeFluidNode();
        super.setRemoved();
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return side != null && type != null && getFluidNodeTypes().contains(type);
    }

    protected record NetworkFluidSubscriptionDetachReport(
            int staleTypes,
            int types,
            int ports) {
    }

    protected record NetworkFluidSubscriptionReport(
            int providerTypes,
            int receiverTypes,
            int localProviderSubscriptions,
            int localReceiverSubscriptions,
            int remoteProviderPorts,
            int remoteReceiverPorts,
            int detachedProviderTypes,
            int detachedProviderPorts,
            int detachedReceiverTypes,
            int detachedReceiverPorts) {
        public static NetworkFluidSubscriptionReport empty() {
            return new NetworkFluidSubscriptionReport(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        public int activeTypes() {
            return providerTypes + receiverTypes;
        }

        public int subscriptions() {
            return localProviderSubscriptions + localReceiverSubscriptions
                    + remoteProviderPorts + remoteReceiverPorts;
        }

        public int detached() {
            return detachedProviderTypes + detachedProviderPorts
                    + detachedReceiverTypes + detachedReceiverPorts;
        }
    }
}
