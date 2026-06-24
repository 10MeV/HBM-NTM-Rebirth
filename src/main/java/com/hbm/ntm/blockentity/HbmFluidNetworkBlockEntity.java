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
import java.util.ArrayList;
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
        refreshFluidNodeStateReport();
    }

    protected FluidNodeStateReport refreshFluidNodeStateReport() {
        if (level == null || level.isClientSide) {
            return FluidNodeStateReport.skippedReport();
        }
        if (!shouldCreateFluidNode()) {
            Set<FluidType> trackedTypes = getTrackedFluidNodeTypes();
            removeFluidNode();
            return FluidNodeStateReport.removedAll(trackedTypes);
        }
        List<FluidType> nodeTypes = getFluidNodeTypes();
        List<FluidNodeStateDetail> details = new ArrayList<>();
        int reusedTypes = 0;
        int createdTypes = 0;
        int expiredRecreatedTypes = 0;
        int remoteNodes = 0;
        int localNodes = 0;
        for (FluidType type : nodeTypes) {
            HbmFluidNode existing = getFluidNode(type);
            boolean existingExpired = existing != null && existing.isExpired();
            boolean remote = shouldUseRemotePortFluidNode(type);
            if (existing == null || existing.isExpired()) {
                HbmFluidNode created = createFluidNode(type);
                HbmFluidNode node = HbmFluidNodespace.createNode(level, created);
                setFluidNode(node);
                createdTypes++;
                if (existingExpired) {
                    expiredRecreatedTypes++;
                }
                if (remote) {
                    remoteNodes++;
                } else {
                    localNodes++;
                }
                details.add(FluidNodeStateDetail.created(type, existing != null, existingExpired, remote, node));
            } else {
                reusedTypes++;
                if (remote) {
                    remoteNodes++;
                } else {
                    localNodes++;
                }
                details.add(FluidNodeStateDetail.reused(type, remote, existing));
            }
        }
        FluidNodeObsoleteRemovalReport obsolete = removeObsoleteFluidNodesReport(new HashSet<>(nodeTypes));
        return new FluidNodeStateReport(
                false,
                false,
                nodeTypes.size(),
                reusedTypes,
                createdTypes,
                expiredRecreatedTypes,
                remoteNodes,
                localNodes,
                obsolete.removedTypes().size(),
                details,
                obsolete.removedTypes());
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
        return refreshFluidNetworkSubscriptionsDetailedReport().summary();
    }

    protected NetworkFluidSubscriptionDetailReport refreshFluidNetworkSubscriptionsDetailedReport() {
        if (level == null || level.isClientSide) {
            return NetworkFluidSubscriptionDetailReport.empty();
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
        NetworkFluidSubscriptionDetachDetailReport providerDetach =
                detachObsoleteNetworkProviderSubscriptionsDetailedReport(activeProviderTypes, provider);
        NetworkFluidSubscriptionDetachDetailReport receiverDetach =
                detachObsoleteNetworkReceiverSubscriptionsDetailedReport(activeReceiverTypes, receiver);

        int localProviderSubscriptions = 0;
        int localReceiverSubscriptions = 0;
        int remoteProviderPorts = 0;
        int remoteReceiverPorts = 0;
        List<NetworkProviderSubscriptionDetail> providerDetails = new ArrayList<>();
        List<NetworkReceiverSubscriptionDetail> receiverDetails = new ArrayList<>();
        for (FluidType type : nodeTypes) {
            HbmFluidNet fluidNet = getFluidNet(type);
            boolean hasLocalNet = fluidNet != null && fluidNet.isValid();
            if (activeProviderTypes.contains(type)) {
                boolean localSubscribed = false;
                if (hasLocalNet && provider != null) {
                    fluidNet.addProvider(provider);
                    localProviderSubscriptions++;
                    localSubscribed = true;
                }
                HbmFluidUtil.PortSubscribeDetailReport remote =
                        HbmFluidUtil.subscribeProviderToPortsDetailedReport(
                                level, worldPosition, getNetworkFluidPorts(type), type, provider);
                remoteProviderPorts += remote.subscribedPorts();
                providerDetails.add(new NetworkProviderSubscriptionDetail(
                        type, hasLocalNet, provider != null, localSubscribed, remote));
            }
            if (activeReceiverTypes.contains(type)) {
                boolean localSubscribed = false;
                if (hasLocalNet && receiver != null) {
                    fluidNet.addReceiver(receiver);
                    localReceiverSubscriptions++;
                    localSubscribed = true;
                }
                HbmFluidUtil.PortSubscribeDetailReport remote =
                        HbmFluidUtil.subscribeReceiverToPortsDetailedReport(
                                level, worldPosition, getNetworkFluidPorts(type), type, receiver);
                remoteReceiverPorts += remote.subscribedPorts();
                receiverDetails.add(new NetworkReceiverSubscriptionDetail(
                        type, hasLocalNet, receiver != null, localSubscribed, remote));
            }
        }

        networkProviderSubscriptions.clear();
        networkProviderSubscriptions.addAll(activeProviderTypes);
        networkReceiverSubscriptions.clear();
        networkReceiverSubscriptions.addAll(activeReceiverTypes);
        NetworkFluidSubscriptionReport summary = new NetworkFluidSubscriptionReport(
                activeProviderTypes.size(),
                activeReceiverTypes.size(),
                localProviderSubscriptions,
                localReceiverSubscriptions,
                remoteProviderPorts,
                remoteReceiverPorts,
                providerDetach.summary().types(),
                providerDetach.summary().ports(),
                receiverDetach.summary().types(),
                receiverDetach.summary().ports());
        return new NetworkFluidSubscriptionDetailReport(summary, providerDetails, receiverDetails, providerDetach, receiverDetach);
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
        return detachObsoleteNetworkProviderSubscriptionsDetailedReport(activeTypes, provider).summary();
    }

    private NetworkFluidSubscriptionDetachDetailReport detachObsoleteNetworkProviderSubscriptionsDetailedReport(
            Set<FluidType> activeTypes, HbmFluidProvider provider) {
        Set<FluidType> staleTypes = new HashSet<>(networkProviderSubscriptions);
        staleTypes.removeAll(activeTypes);
        int detachedTypes = 0;
        int detachedPorts = 0;
        List<NetworkProviderDetachDetail> details = new ArrayList<>();
        for (FluidType type : staleTypes) {
            HbmFluidNet fluidNet = getFluidNet(type);
            boolean localPresent = fluidNet != null && provider != null && fluidNet.isProvider(provider);
            if (fluidNet != null && provider != null) {
                if (localPresent) {
                    detachedTypes++;
                }
                fluidNet.removeProvider(provider);
            }
            HbmFluidUtil.PortDetachDetailReport remote =
                    HbmFluidUtil.unsubscribeProviderFromPortsDetailedReport(
                            level, worldPosition, getNetworkFluidPorts(type), type, provider);
            detachedPorts += remote.unsubscribedPorts();
            details.add(new NetworkProviderDetachDetail(type, localPresent, remote));
        }
        return new NetworkFluidSubscriptionDetachDetailReport(
                new NetworkFluidSubscriptionDetachReport(staleTypes.size(), detachedTypes, detachedPorts),
                details,
                List.of());
    }

    private NetworkFluidSubscriptionDetachReport detachObsoleteNetworkReceiverSubscriptions(
            Set<FluidType> activeTypes, HbmFluidReceiver receiver) {
        return detachObsoleteNetworkReceiverSubscriptionsDetailedReport(activeTypes, receiver).summary();
    }

    private NetworkFluidSubscriptionDetachDetailReport detachObsoleteNetworkReceiverSubscriptionsDetailedReport(
            Set<FluidType> activeTypes, HbmFluidReceiver receiver) {
        Set<FluidType> staleTypes = new HashSet<>(networkReceiverSubscriptions);
        staleTypes.removeAll(activeTypes);
        int detachedTypes = 0;
        int detachedPorts = 0;
        List<NetworkReceiverDetachDetail> details = new ArrayList<>();
        for (FluidType type : staleTypes) {
            HbmFluidNet fluidNet = getFluidNet(type);
            boolean localPresent = fluidNet != null && receiver != null && fluidNet.isSubscribed(receiver);
            if (fluidNet != null && receiver != null) {
                if (localPresent) {
                    detachedTypes++;
                }
                fluidNet.removeReceiver(receiver);
            }
            HbmFluidUtil.PortDetachDetailReport remote =
                    HbmFluidUtil.unsubscribeReceiverFromPortsDetailedReport(
                            level, worldPosition, getNetworkFluidPorts(type), type, receiver);
            detachedPorts += remote.unsubscribedPorts();
            details.add(new NetworkReceiverDetachDetail(type, localPresent, remote));
        }
        return new NetworkFluidSubscriptionDetachDetailReport(
                new NetworkFluidSubscriptionDetachReport(staleTypes.size(), detachedTypes, detachedPorts),
                List.of(),
                details);
    }

    private void clearNetworkFluidSubscriptions() {
        detachObsoleteNetworkProviderSubscriptions(Set.of(), getNetworkFluidProvider());
        detachObsoleteNetworkReceiverSubscriptions(Set.of(), getNetworkFluidReceiver());
        networkProviderSubscriptions.clear();
        networkReceiverSubscriptions.clear();
    }

    private FluidNodeObsoleteRemovalReport removeObsoleteFluidNodesReport(Set<FluidType> activeTypes) {
        Set<FluidType> active = activeTypes == null ? Set.of() : activeTypes;
        List<FluidType> removedTypes = new ArrayList<>();
        for (FluidType type : getTrackedFluidNodeTypes()) {
            if (!active.contains(type)) {
                HbmFluidNodespace.destroyNode(level, worldPosition, type);
                removeFluidNode(type);
                removedTypes.add(type);
            }
        }
        return new FluidNodeObsoleteRemovalReport(removedTypes);
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
    public void onChunkUnloaded() {
        removeFluidNode();
        super.onChunkUnloaded();
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return side != null && type != null && type != HbmFluids.NONE;
    }

    protected record NetworkFluidSubscriptionDetachReport(
            int staleTypes,
            int types,
            int ports) {
    }

    protected record NetworkFluidSubscriptionDetachDetailReport(
            NetworkFluidSubscriptionDetachReport summary,
            List<NetworkProviderDetachDetail> providerDetails,
            List<NetworkReceiverDetachDetail> receiverDetails) {
        protected NetworkFluidSubscriptionDetachDetailReport {
            summary = summary == null ? new NetworkFluidSubscriptionDetachReport(0, 0, 0) : summary;
            providerDetails = providerDetails == null ? List.of() : List.copyOf(providerDetails);
            receiverDetails = receiverDetails == null ? List.of() : List.copyOf(receiverDetails);
        }

        public static NetworkFluidSubscriptionDetachDetailReport empty() {
            return new NetworkFluidSubscriptionDetachDetailReport(
                    new NetworkFluidSubscriptionDetachReport(0, 0, 0), List.of(), List.of());
        }
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

    protected record NetworkFluidSubscriptionDetailReport(
            NetworkFluidSubscriptionReport summary,
            List<NetworkProviderSubscriptionDetail> providerDetails,
            List<NetworkReceiverSubscriptionDetail> receiverDetails,
            NetworkFluidSubscriptionDetachDetailReport providerDetach,
            NetworkFluidSubscriptionDetachDetailReport receiverDetach) {
        protected NetworkFluidSubscriptionDetailReport {
            summary = summary == null ? NetworkFluidSubscriptionReport.empty() : summary;
            providerDetails = providerDetails == null ? List.of() : List.copyOf(providerDetails);
            receiverDetails = receiverDetails == null ? List.of() : List.copyOf(receiverDetails);
            providerDetach = providerDetach == null ? NetworkFluidSubscriptionDetachDetailReport.empty() : providerDetach;
            receiverDetach = receiverDetach == null ? NetworkFluidSubscriptionDetachDetailReport.empty() : receiverDetach;
        }

        public static NetworkFluidSubscriptionDetailReport empty() {
            return new NetworkFluidSubscriptionDetailReport(
                    NetworkFluidSubscriptionReport.empty(),
                    List.of(),
                    List.of(),
                    NetworkFluidSubscriptionDetachDetailReport.empty(),
                    NetworkFluidSubscriptionDetachDetailReport.empty());
        }
    }

    protected record NetworkProviderSubscriptionDetail(
            FluidType type,
            boolean localNetworkPresent,
            boolean providerPresent,
            boolean localSubscribed,
            HbmFluidUtil.PortSubscribeDetailReport remoteSubscription) {
    }

    protected record NetworkReceiverSubscriptionDetail(
            FluidType type,
            boolean localNetworkPresent,
            boolean receiverPresent,
            boolean localSubscribed,
            HbmFluidUtil.PortSubscribeDetailReport remoteSubscription) {
    }

    protected record NetworkProviderDetachDetail(
            FluidType type,
            boolean localSubscriptionPresent,
            HbmFluidUtil.PortDetachDetailReport remoteDetach) {
    }

    protected record NetworkReceiverDetachDetail(
            FluidType type,
            boolean localSubscriptionPresent,
            HbmFluidUtil.PortDetachDetailReport remoteDetach) {
    }

    protected record FluidNodeStateReport(
            boolean skipped,
            boolean removedAll,
            int activeTypes,
            int reusedTypes,
            int createdTypes,
            int expiredRecreatedTypes,
            int remoteNodes,
            int localNodes,
            int obsoleteTypes,
            List<FluidNodeStateDetail> details,
            List<FluidType> removedTypes) {
        protected FluidNodeStateReport {
            details = details == null ? List.of() : List.copyOf(details);
            removedTypes = removedTypes == null ? List.of() : List.copyOf(removedTypes);
        }

        public static FluidNodeStateReport skippedReport() {
            return new FluidNodeStateReport(true, false, 0, 0, 0, 0, 0, 0, 0, List.of(), List.of());
        }

        public static FluidNodeStateReport removedAll(Set<FluidType> trackedTypes) {
            List<FluidType> removed = trackedTypes == null ? List.of() : List.copyOf(trackedTypes);
            return new FluidNodeStateReport(false, true, 0, 0, 0, 0, 0, 0, removed.size(), List.of(), removed);
        }
    }

    protected record FluidNodeStateDetail(
            FluidType type,
            boolean previouslyTracked,
            boolean expiredRecreated,
            boolean created,
            boolean remoteNode,
            int positions,
            int connections,
            int connectionPoints) {
        private static FluidNodeStateDetail created(
                FluidType type, boolean previouslyTracked, boolean expiredRecreated, boolean remoteNode,
                HbmFluidNode node) {
            return fromNode(type, previouslyTracked, expiredRecreated, true, remoteNode, node);
        }

        private static FluidNodeStateDetail reused(FluidType type, boolean remoteNode, HbmFluidNode node) {
            return fromNode(type, true, false, false, remoteNode, node);
        }

        private static FluidNodeStateDetail fromNode(
                FluidType type, boolean previouslyTracked, boolean expiredRecreated, boolean created,
                boolean remoteNode, HbmFluidNode node) {
            return new FluidNodeStateDetail(
                    type,
                    previouslyTracked,
                    expiredRecreated,
                    created,
                    remoteNode,
                    node == null ? 0 : node.getPositions().size(),
                    node == null ? 0 : node.getConnections().size(),
                    node == null ? 0 : node.getConnectionPoints().size());
        }
    }

    private record FluidNodeObsoleteRemovalReport(List<FluidType> removedTypes) {
        private FluidNodeObsoleteRemovalReport {
            removedTypes = removedTypes == null ? List.of() : List.copyOf(removedTypes);
        }
    }
}
