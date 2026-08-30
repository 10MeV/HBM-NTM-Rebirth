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
import com.hbm.ntm.util.HbmMachinePerformanceCounters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class HbmFluidNetworkBlockEntity extends HbmFluidBlockEntity implements HbmFluidConnector {
    private static final int FLUID_NODE_KEEPALIVE_TICKS = 40;
    private static final int FLUID_SUBSCRIPTION_KEEPALIVE_TICKS = 20;

    private final Set<FluidType> networkProviderSubscriptions = new HashSet<>();
    private final Set<FluidType> networkReceiverSubscriptions = new HashSet<>();
    private final Map<FluidType, List<FluidPort>> networkProviderPorts = new HashMap<>();
    private final Map<FluidType, List<FluidPort>> networkReceiverPorts = new HashMap<>();
    private boolean fluidNodeStateDirty = true;
    private boolean fluidSubscriptionDirty = true;
    private int lastFluidNodeTypesSignature = Integer.MIN_VALUE;
    private int lastFluidPortShapeSignature = Integer.MIN_VALUE;
    private int lastFluidProviderSubscriptionSignature = Integer.MIN_VALUE;
    private int lastFluidReceiverSubscriptionSignature = Integer.MIN_VALUE;

    protected HbmFluidNetworkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, java.util.List<HbmFluidTank> tanks) {
        super(type, pos, state, tanks);
    }

    public static <T extends HbmFluidNetworkBlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (!level.isClientSide) {
            HbmMachinePerformanceCounters.fluidNetworkTick();
            if (blockEntity.shouldRefreshFluidNodeStateNow()) {
                blockEntity.refreshFluidNodeState();
            }
            if (blockEntity.shouldRefreshFluidNetworkSubscriptionsNow()) {
                blockEntity.refreshFluidNetworkSubscriptionsNoReport();
            }
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
        List<FluidType> types = new ArrayList<>();
        List<HbmFluidTank> tanks = getAllTanks();
        for (int i = 0; i < tanks.size(); i++) {
            FluidType type = tanks.get(i).getTankType();
            if (type != HbmFluids.NONE && !types.contains(type)) {
                types.add(type);
            }
        }
        return types;
    }

    protected HbmFluidNode createRemotePortFluidNode(FluidType type) {
        Set<BlockPos> positions = new LinkedHashSet<>();
        positions.add(worldPosition.immutable());
        Set<HbmNetworkNode.NodeConnection> connections = new LinkedHashSet<>();
        for (FluidPort port : getFluidPorts()) {
            if (port == null) {
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
            updateFluidNodeRefreshBookkeeping();
            HbmMachinePerformanceCounters.fluidNodeRefresh();
            return;
        }
        List<HbmFluidTank> tanks = getAllTanks();
        for (int i = 0; i < tanks.size(); i++) {
            FluidType type = tanks.get(i).getTankType();
            if (type == HbmFluids.NONE || containsFluidTypeBefore(tanks, i, type)) {
                continue;
            }
            HbmFluidNode existing = getFluidNode(type);
            if (existing == null || existing.isExpired()) {
                HbmFluidNode created = createFluidNode(type);
                HbmFluidNode node = HbmFluidNodespace.createNode(level, created);
                setFluidNode(node);
            }
        }
        removeObsoleteFluidNodes(tanks);
        updateFluidNodeRefreshBookkeeping();
        HbmMachinePerformanceCounters.fluidNodeRefresh();
    }

    protected FluidNodeStateReport refreshFluidNodeStateReport() {
        if (level == null || level.isClientSide) {
            return FluidNodeStateReport.skippedReport();
        }
        if (!shouldCreateFluidNode()) {
            Set<FluidType> trackedTypes = getTrackedFluidNodeTypes();
            removeFluidNode();
            updateFluidNodeRefreshBookkeeping();
            HbmMachinePerformanceCounters.fluidNodeRefresh();
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
        updateFluidNodeRefreshBookkeeping();
        HbmMachinePerformanceCounters.fluidNodeRefresh();
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
        markTankTypesDirty();
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

    protected void refreshFluidNetworkSubscriptionsNoReport() {
        if (level == null || level.isClientSide) {
            return;
        }
        HbmFluidProvider provider = getNetworkFluidProvider();
        HbmFluidReceiver receiver = getNetworkFluidReceiver();
        List<HbmFluidTank> tanks = getAllTanks();
        detachObsoleteNetworkProviderSubscriptionsNoReport(provider, tanks);
        detachObsoleteNetworkReceiverSubscriptionsNoReport(receiver, tanks);

        for (int i = 0; i < tanks.size(); i++) {
            FluidType type = tanks.get(i).getTankType();
            if (type == HbmFluids.NONE || containsFluidTypeBefore(tanks, i, type)) {
                continue;
            }
            HbmFluidNet fluidNet = getFluidNet(type);
            boolean hasLocalNet = fluidNet != null && fluidNet.isValid();
            if (shouldSubscribeAsFluidProvider(type)) {
                List<FluidPort> ports = snapshotNetworkFluidPorts(type);
                detachRemovedNetworkProviderPortsNoReport(type, ports, provider);
                if (hasLocalNet && provider != null) {
                    fluidNet.addProvider(provider);
                }
                HbmFluidUtil.subscribeProviderToPorts(
                        level, worldPosition, ports, type, provider);
                networkProviderSubscriptions.add(type);
                networkProviderPorts.put(type, ports);
            }
            if (shouldSubscribeAsFluidReceiver(type)) {
                List<FluidPort> ports = snapshotNetworkFluidPorts(type);
                detachRemovedNetworkReceiverPortsNoReport(type, ports, receiver);
                if (hasLocalNet && receiver != null) {
                    fluidNet.addReceiver(receiver);
                }
                HbmFluidUtil.subscribeReceiverToPorts(
                        level, worldPosition, ports, type, receiver);
                networkReceiverSubscriptions.add(type);
                networkReceiverPorts.put(type, ports);
            }
        }

        updateFluidSubscriptionRefreshBookkeeping();
        HbmMachinePerformanceCounters.fluidSubscriptionRefresh();
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
                List<FluidPort> ports = snapshotNetworkFluidPorts(type);
                providerDetach = mergeDetachReports(
                        providerDetach, detachRemovedNetworkProviderPorts(type, ports, provider));
                if (hasLocalNet && provider != null) {
                    fluidNet.addProvider(provider);
                    localProviderSubscriptions++;
                }
                remoteProviderPorts += HbmFluidUtil.subscribeProviderToPorts(
                        level, worldPosition, ports, type, provider);
                networkProviderPorts.put(type, ports);
            }
            if (activeReceiverTypes.contains(type)) {
                List<FluidPort> ports = snapshotNetworkFluidPorts(type);
                receiverDetach = mergeDetachReports(
                        receiverDetach, detachRemovedNetworkReceiverPorts(type, ports, receiver));
                if (hasLocalNet && receiver != null) {
                    fluidNet.addReceiver(receiver);
                    localReceiverSubscriptions++;
                }
                remoteReceiverPorts += HbmFluidUtil.subscribeReceiverToPorts(
                        level, worldPosition, ports, type, receiver);
                networkReceiverPorts.put(type, ports);
            }
        }

        networkProviderSubscriptions.clear();
        networkProviderSubscriptions.addAll(activeProviderTypes);
        networkReceiverSubscriptions.clear();
        networkReceiverSubscriptions.addAll(activeReceiverTypes);
        updateFluidSubscriptionRefreshBookkeeping(activeProviderTypes, activeReceiverTypes);
        HbmMachinePerformanceCounters.fluidSubscriptionRefresh();
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
                List<FluidPort> ports = snapshotNetworkFluidPorts(type);
                providerDetach = mergeDetachDetailReports(
                        providerDetach, detachRemovedNetworkProviderPortsDetailed(type, ports, provider));
                boolean localSubscribed = false;
                if (hasLocalNet && provider != null) {
                    fluidNet.addProvider(provider);
                    localProviderSubscriptions++;
                    localSubscribed = true;
                }
                HbmFluidUtil.PortSubscribeDetailReport remote =
                        HbmFluidUtil.subscribeProviderToPortsDetailedReport(
                                level, worldPosition, ports, type, provider);
                remoteProviderPorts += remote.subscribedPorts();
                networkProviderPorts.put(type, ports);
                providerDetails.add(new NetworkProviderSubscriptionDetail(
                        type, hasLocalNet, provider != null, localSubscribed, remote));
            }
            if (activeReceiverTypes.contains(type)) {
                List<FluidPort> ports = snapshotNetworkFluidPorts(type);
                receiverDetach = mergeDetachDetailReports(
                        receiverDetach, detachRemovedNetworkReceiverPortsDetailed(type, ports, receiver));
                boolean localSubscribed = false;
                if (hasLocalNet && receiver != null) {
                    fluidNet.addReceiver(receiver);
                    localReceiverSubscriptions++;
                    localSubscribed = true;
                }
                HbmFluidUtil.PortSubscribeDetailReport remote =
                        HbmFluidUtil.subscribeReceiverToPortsDetailedReport(
                                level, worldPosition, ports, type, receiver);
                remoteReceiverPorts += remote.subscribedPorts();
                networkReceiverPorts.put(type, ports);
                receiverDetails.add(new NetworkReceiverSubscriptionDetail(
                        type, hasLocalNet, receiver != null, localSubscribed, remote));
            }
        }

        networkProviderSubscriptions.clear();
        networkProviderSubscriptions.addAll(activeProviderTypes);
        networkReceiverSubscriptions.clear();
        networkReceiverSubscriptions.addAll(activeReceiverTypes);
        updateFluidSubscriptionRefreshBookkeeping(activeProviderTypes, activeReceiverTypes);
        HbmMachinePerformanceCounters.fluidSubscriptionRefresh();
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

    protected void markPortsDirty() {
        fluidNodeStateDirty = true;
        fluidSubscriptionDirty = true;
    }

    protected void markTankTypesDirty() {
        fluidNodeStateDirty = true;
        fluidSubscriptionDirty = true;
    }

    protected void markFluidSubscriptionDirty() {
        fluidSubscriptionDirty = true;
    }

    protected boolean shouldRefreshFluidNodeStateNow() {
        if (fluidNodeStateDirty || hasExpiredFluidNode()) {
            return true;
        }
        int nodeTypesSignature = fluidNodeTypesSignature();
        int portShapeSignature = fluidPortShapeSignature(getFluidPorts());
        if (nodeTypesSignature != lastFluidNodeTypesSignature || portShapeSignature != lastFluidPortShapeSignature) {
            return true;
        }
        return isStaggeredKeepalive(FLUID_NODE_KEEPALIVE_TICKS);
    }

    protected boolean shouldRefreshFluidNetworkSubscriptionsNow() {
        if (usesLegacyTwentyTickFluidPortSubscriptionCadence()) {
            // These old hosts retried remote Fluid Mk2 subscriptions only from
            // their exact `% 20 == 0` pass.  In particular, a same-position
            // duct replacement must not be picked up by the generic dirty or
            // staggered keepalive route before that legacy boundary.
            return level != null && level.getGameTime() % 20L == 0L;
        }
        if (shouldRefreshFluidNetworkSubscriptionsEveryTick() || fluidSubscriptionDirty) {
            return true;
        }
        int providerSignature = activeProviderSubscriptionSignature();
        int receiverSignature = activeReceiverSubscriptionSignature();
        if (providerSignature != lastFluidProviderSubscriptionSignature
                || receiverSignature != lastFluidReceiverSubscriptionSignature) {
            return true;
        }
        return isStaggeredKeepalive(FLUID_SUBSCRIPTION_KEEPALIVE_TICKS);
    }

    /**
     * Opt in only for legacy hosts which explicitly called their remote fluid
     * subscription routine every server tick.  Ordinary machines retain the
     * dirty/signature/keepalive refresh policy above.
     */
    protected boolean shouldRefreshFluidNetworkSubscriptionsEveryTick() {
        return false;
    }

    /**
     * Opt in only for legacy hosts whose remote Fluid Mk2 receiver pass ran on
     * the world's exact twenty-tick boundary. Direct per-tick providers stay
     * on their own production call path.
     */
    protected boolean usesLegacyTwentyTickFluidPortSubscriptionCadence() {
        return false;
    }

    private boolean hasExpiredFluidNode() {
        for (FluidType type : getTrackedFluidNodeTypesView()) {
            HbmFluidNode node = getFluidNode(type);
            if (node == null || node.isExpired()) {
                return true;
            }
        }
        return false;
    }

    private boolean isStaggeredKeepalive(int interval) {
        return level != null
                && interval > 0
                && Math.floorMod(level.getGameTime() + worldPosition.hashCode(), interval) == 0L;
    }

    private void updateFluidNodeRefreshBookkeeping() {
        lastFluidNodeTypesSignature = fluidNodeTypesSignature();
        lastFluidPortShapeSignature = fluidPortShapeSignature(getFluidPorts());
        fluidNodeStateDirty = false;
        fluidSubscriptionDirty = true;
    }

    private void updateFluidSubscriptionRefreshBookkeeping(Set<FluidType> activeProviderTypes,
            Set<FluidType> activeReceiverTypes) {
        lastFluidProviderSubscriptionSignature = activeProviderTypes == null ? 0 : activeProviderTypes.hashCode();
        lastFluidReceiverSubscriptionSignature = activeReceiverTypes == null ? 0 : activeReceiverTypes.hashCode();
        fluidSubscriptionDirty = false;
    }

    private void updateFluidSubscriptionRefreshBookkeeping() {
        lastFluidProviderSubscriptionSignature = networkProviderSubscriptions.hashCode();
        lastFluidReceiverSubscriptionSignature = networkReceiverSubscriptions.hashCode();
        fluidSubscriptionDirty = false;
    }

    private int fluidNodeTypesSignature() {
        int signature = 1;
        List<HbmFluidTank> tanks = getAllTanks();
        for (int i = 0; i < tanks.size(); i++) {
            HbmFluidTank tank = tanks.get(i);
            FluidType type = tank.getTankType();
            if (type != HbmFluids.NONE && !containsFluidTypeBefore(tanks, i, type)) {
                signature = 31 * signature + type.hashCode();
            }
        }
        return signature;
    }

    private static int fluidPortShapeSignature(Iterable<FluidPort> ports) {
        int signature = 1;
        if (ports != null) {
            for (FluidPort port : ports) {
                signature = 31 * signature + (port == null ? 0 : port.hashCode());
            }
        }
        return signature;
    }

    private int activeProviderSubscriptionSignature() {
        int signature = 0;
        List<HbmFluidTank> tanks = getAllTanks();
        for (int i = 0; i < tanks.size(); i++) {
            FluidType type = tanks.get(i).getTankType();
            if (type != HbmFluids.NONE && !containsFluidTypeBefore(tanks, i, type)
                    && shouldSubscribeAsFluidProvider(type)) {
                signature += type.hashCode();
            }
        }
        return signature;
    }

    private int activeReceiverSubscriptionSignature() {
        int signature = 0;
        List<HbmFluidTank> tanks = getAllTanks();
        for (int i = 0; i < tanks.size(); i++) {
            FluidType type = tanks.get(i).getTankType();
            if (type != HbmFluids.NONE && !containsFluidTypeBefore(tanks, i, type)
                    && shouldSubscribeAsFluidReceiver(type)) {
                signature += type.hashCode();
            }
        }
        return signature;
    }

    private static boolean containsFluidTypeBefore(List<HbmFluidTank> tanks, int beforeIndex, FluidType type) {
        for (int i = 0; i < beforeIndex; i++) {
            if (tanks.get(i).getTankType() == type) {
                return true;
            }
        }
        return false;
    }

    private boolean hasActiveProviderSubscriptionType(FluidType type, List<HbmFluidTank> tanks) {
        if (type == null || type == HbmFluids.NONE || !shouldSubscribeAsFluidProvider(type)) {
            return false;
        }
        return hasTankType(tanks, type);
    }

    private boolean hasActiveReceiverSubscriptionType(FluidType type, List<HbmFluidTank> tanks) {
        if (type == null || type == HbmFluids.NONE || !shouldSubscribeAsFluidReceiver(type)) {
            return false;
        }
        return hasTankType(tanks, type);
    }

    private static boolean hasTankType(List<HbmFluidTank> tanks, FluidType type) {
        for (int i = 0; i < tanks.size(); i++) {
            if (tanks.get(i).getTankType() == type) {
                return true;
            }
        }
        return false;
    }

    private List<FluidPort> snapshotNetworkFluidPorts(FluidType type) {
        Iterable<FluidPort> current = getNetworkFluidPorts(type);
        if (current == null) {
            return List.of();
        }
        List<FluidPort> snapshot = new ArrayList<>();
        for (FluidPort port : current) {
            if (port == null) {
                continue;
            }
            BlockPos offset = port.offset();
            BlockPos immutableOffset = offset.immutable();
            snapshot.add(immutableOffset == offset
                    ? port
                    : new FluidPort(immutableOffset, port.direction()));
        }
        return List.copyOf(snapshot);
    }

    private static List<FluidPort> trackedNetworkPorts(
            Map<FluidType, List<FluidPort>> portsByType, FluidType type) {
        List<FluidPort> ports = portsByType.get(type);
        return ports == null ? List.of() : ports;
    }

    private static List<FluidPort> removedNetworkPorts(
            Map<FluidType, List<FluidPort>> portsByType, FluidType type, List<FluidPort> currentPorts) {
        List<FluidPort> removed = new ArrayList<>(trackedNetworkPorts(portsByType, type));
        for (FluidPort port : currentPorts) {
            removed.remove(port);
        }
        return removed;
    }

    private void detachRemovedNetworkProviderPortsNoReport(
            FluidType type, List<FluidPort> currentPorts, HbmFluidProvider provider) {
        List<FluidPort> removed = removedNetworkPorts(networkProviderPorts, type, currentPorts);
        if (!removed.isEmpty()) {
            HbmFluidUtil.unsubscribeProviderFromPorts(level, worldPosition, removed, type, provider);
        }
    }

    private void detachRemovedNetworkReceiverPortsNoReport(
            FluidType type, List<FluidPort> currentPorts, HbmFluidReceiver receiver) {
        List<FluidPort> removed = removedNetworkPorts(networkReceiverPorts, type, currentPorts);
        if (!removed.isEmpty()) {
            HbmFluidUtil.unsubscribeReceiverFromPorts(level, worldPosition, removed, type, receiver);
        }
    }

    private NetworkFluidSubscriptionDetachReport detachRemovedNetworkProviderPorts(
            FluidType type, List<FluidPort> currentPorts, HbmFluidProvider provider) {
        List<FluidPort> removed = removedNetworkPorts(networkProviderPorts, type, currentPorts);
        int detached = removed.isEmpty() ? 0 : HbmFluidUtil.unsubscribeProviderFromPorts(
                level, worldPosition, removed, type, provider);
        return new NetworkFluidSubscriptionDetachReport(0, 0, detached);
    }

    private NetworkFluidSubscriptionDetachReport detachRemovedNetworkReceiverPorts(
            FluidType type, List<FluidPort> currentPorts, HbmFluidReceiver receiver) {
        List<FluidPort> removed = removedNetworkPorts(networkReceiverPorts, type, currentPorts);
        int detached = removed.isEmpty() ? 0 : HbmFluidUtil.unsubscribeReceiverFromPorts(
                level, worldPosition, removed, type, receiver);
        return new NetworkFluidSubscriptionDetachReport(0, 0, detached);
    }

    private NetworkFluidSubscriptionDetachDetailReport detachRemovedNetworkProviderPortsDetailed(
            FluidType type, List<FluidPort> currentPorts, HbmFluidProvider provider) {
        List<FluidPort> removed = removedNetworkPorts(networkProviderPorts, type, currentPorts);
        if (removed.isEmpty()) {
            return NetworkFluidSubscriptionDetachDetailReport.empty();
        }
        HbmFluidUtil.PortDetachDetailReport remote = HbmFluidUtil.unsubscribeProviderFromPortsDetailedReport(
                level, worldPosition, removed, type, provider);
        return new NetworkFluidSubscriptionDetachDetailReport(
                new NetworkFluidSubscriptionDetachReport(0, 0, remote.unsubscribedPorts()),
                List.of(new NetworkProviderDetachDetail(type, false, remote)),
                List.of());
    }

    private NetworkFluidSubscriptionDetachDetailReport detachRemovedNetworkReceiverPortsDetailed(
            FluidType type, List<FluidPort> currentPorts, HbmFluidReceiver receiver) {
        List<FluidPort> removed = removedNetworkPorts(networkReceiverPorts, type, currentPorts);
        if (removed.isEmpty()) {
            return NetworkFluidSubscriptionDetachDetailReport.empty();
        }
        HbmFluidUtil.PortDetachDetailReport remote = HbmFluidUtil.unsubscribeReceiverFromPortsDetailedReport(
                level, worldPosition, removed, type, receiver);
        return new NetworkFluidSubscriptionDetachDetailReport(
                new NetworkFluidSubscriptionDetachReport(0, 0, remote.unsubscribedPorts()),
                List.of(),
                List.of(new NetworkReceiverDetachDetail(type, false, remote)));
    }

    private static NetworkFluidSubscriptionDetachReport mergeDetachReports(
            NetworkFluidSubscriptionDetachReport first, NetworkFluidSubscriptionDetachReport second) {
        return new NetworkFluidSubscriptionDetachReport(
                first.staleTypes() + second.staleTypes(),
                first.types() + second.types(),
                first.ports() + second.ports());
    }

    private static NetworkFluidSubscriptionDetachDetailReport mergeDetachDetailReports(
            NetworkFluidSubscriptionDetachDetailReport first,
            NetworkFluidSubscriptionDetachDetailReport second) {
        List<NetworkProviderDetachDetail> providers = new ArrayList<>(first.providerDetails());
        providers.addAll(second.providerDetails());
        List<NetworkReceiverDetachDetail> receivers = new ArrayList<>(first.receiverDetails());
        receivers.addAll(second.receiverDetails());
        return new NetworkFluidSubscriptionDetachDetailReport(
                mergeDetachReports(first.summary(), second.summary()), providers, receivers);
    }

    private void detachObsoleteNetworkProviderSubscriptionsNoReport(
            HbmFluidProvider provider, List<HbmFluidTank> tanks) {
        if (networkProviderSubscriptions.isEmpty()) {
            return;
        }
        Iterator<FluidType> iterator = networkProviderSubscriptions.iterator();
        while (iterator.hasNext()) {
            FluidType type = iterator.next();
            if (hasActiveProviderSubscriptionType(type, tanks)) {
                continue;
            }
            HbmFluidNet fluidNet = getFluidNet(type);
            if (fluidNet != null && provider != null) {
                fluidNet.removeProvider(provider);
            }
            HbmFluidUtil.unsubscribeProviderFromPorts(
                    level, worldPosition, trackedNetworkPorts(networkProviderPorts, type), type, provider);
            networkProviderPorts.remove(type);
            iterator.remove();
        }
    }

    private void detachObsoleteNetworkReceiverSubscriptionsNoReport(
            HbmFluidReceiver receiver, List<HbmFluidTank> tanks) {
        if (networkReceiverSubscriptions.isEmpty()) {
            return;
        }
        Iterator<FluidType> iterator = networkReceiverSubscriptions.iterator();
        while (iterator.hasNext()) {
            FluidType type = iterator.next();
            if (hasActiveReceiverSubscriptionType(type, tanks)) {
                continue;
            }
            HbmFluidNet fluidNet = getFluidNet(type);
            if (fluidNet != null && receiver != null) {
                fluidNet.removeReceiver(receiver);
            }
            HbmFluidUtil.unsubscribeReceiverFromPorts(
                    level, worldPosition, trackedNetworkPorts(networkReceiverPorts, type), type, receiver);
            networkReceiverPorts.remove(type);
            iterator.remove();
        }
    }

    private NetworkFluidSubscriptionDetachReport detachObsoleteNetworkProviderSubscriptions(
            Set<FluidType> activeTypes, HbmFluidProvider provider) {
        if (networkProviderSubscriptions.isEmpty()) {
            return new NetworkFluidSubscriptionDetachReport(0, 0, 0);
        }
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
                    level, worldPosition, trackedNetworkPorts(networkProviderPorts, type), type, provider);
            networkProviderPorts.remove(type);
        }
        return new NetworkFluidSubscriptionDetachReport(staleTypes.size(), detachedTypes, detachedPorts);
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
                            level, worldPosition, trackedNetworkPorts(networkProviderPorts, type), type, provider);
            detachedPorts += remote.unsubscribedPorts();
            networkProviderPorts.remove(type);
            details.add(new NetworkProviderDetachDetail(type, localPresent, remote));
        }
        return new NetworkFluidSubscriptionDetachDetailReport(
                new NetworkFluidSubscriptionDetachReport(staleTypes.size(), detachedTypes, detachedPorts),
                details,
                List.of());
    }

    private NetworkFluidSubscriptionDetachReport detachObsoleteNetworkReceiverSubscriptions(
            Set<FluidType> activeTypes, HbmFluidReceiver receiver) {
        if (networkReceiverSubscriptions.isEmpty()) {
            return new NetworkFluidSubscriptionDetachReport(0, 0, 0);
        }
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
                    level, worldPosition, trackedNetworkPorts(networkReceiverPorts, type), type, receiver);
            networkReceiverPorts.remove(type);
        }
        return new NetworkFluidSubscriptionDetachReport(staleTypes.size(), detachedTypes, detachedPorts);
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
                            level, worldPosition, trackedNetworkPorts(networkReceiverPorts, type), type, receiver);
            detachedPorts += remote.unsubscribedPorts();
            networkReceiverPorts.remove(type);
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
        networkProviderPorts.clear();
        networkReceiverPorts.clear();
        markFluidSubscriptionDirty();
    }

    private FluidNodeObsoleteRemovalReport removeObsoleteFluidNodesReport(Set<FluidType> activeTypes) {
        Set<FluidType> active = activeTypes == null ? Set.of() : activeTypes;
        List<FluidType> removedTypes = new ArrayList<>();
        for (FluidType type : getTrackedFluidNodeTypes()) {
            if (!active.contains(type)) {
                destroyTrackedFluidNode(type);
                removeFluidNode(type);
                removedTypes.add(type);
            }
        }
        return new FluidNodeObsoleteRemovalReport(removedTypes);
    }

    private int removeObsoleteFluidNodes(List<HbmFluidTank> activeTanks) {
        int removedTypes = 0;
        List<HbmFluidTank> active = activeTanks == null ? List.of() : activeTanks;
        Iterator<FluidType> iterator = getTrackedFluidNodeTypeIterator();
        while (iterator.hasNext()) {
            FluidType type = iterator.next();
            if (!hasTankType(active, type)) {
                destroyTrackedFluidNode(type);
                iterator.remove();
                removedTypes++;
            }
        }
        return removedTypes;
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
            destroyTrackedFluidNode(type);
        }
        super.removeFluidNode();
    }

    private void destroyTrackedFluidNode(FluidType type) {
        HbmFluidNode node = getFluidNode(type);
        if (node != null) {
            HbmFluidNodespace.destroyNode(level, node);
        } else {
            HbmFluidNodespace.destroyNode(level, worldPosition, type);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        markTankTypesDirty();
        refreshFluidNodeState();
    }

    @Override
    public void setRemoved() {
        removeFluidNode();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
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
