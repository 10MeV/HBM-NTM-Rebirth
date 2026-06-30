package com.hbm.ntm.fluid;

import com.hbm.ntm.block.HbmFluidNodeBlock;
import com.hbm.ntm.network.LoadedTileAccessCache;
import com.hbm.ntm.util.HbmMachinePerformanceCounters;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public final class HbmFluidUtil {
    private static final Map<ResourceKey<Level>, Map<VisualPortKey, IdentityHashMap<Object, EnumSet<VisualRole>>>>
            REMOTE_VISUAL_PORTS = new HashMap<>();

    private HbmFluidUtil() {
    }

    /**
     * A 1.7.10-style remote fluid port. The direction is the old DirPos
     * direction from the owning machine toward/out of the connector position.
     */
    public record FluidPort(BlockPos offset, Direction direction) {
        public FluidPort {
            Objects.requireNonNull(offset, "offset");
            Objects.requireNonNull(direction, "direction");
        }

        public static FluidPort of(int x, int y, int z, Direction direction) {
            return new FluidPort(new BlockPos(x, y, z), direction);
        }

        public BlockPos connectorPos(BlockPos origin) {
            return origin.offset(offset);
        }

        public Direction connectorSide() {
            return direction.getOpposite();
        }
    }

    public static boolean subscribeProviderToPort(Level level, BlockPos origin, FluidPort port, FluidType type,
            HbmFluidProvider provider) {
        if (level == null || origin == null || port == null || type == null || type == HbmFluids.NONE || provider == null) {
            return false;
        }
        if (!isLoadedPort(level, origin, port)) {
            return false;
        }
        return subscribeProviderToNetwork(level, port.connectorPos(origin), port.connectorSide(), type, provider);
    }

    public static boolean subscribeReceiverToPort(Level level, BlockPos origin, FluidPort port, FluidType type,
            HbmFluidReceiver receiver) {
        if (level == null || origin == null || port == null || type == null || type == HbmFluids.NONE || receiver == null) {
            return false;
        }
        if (!isLoadedPort(level, origin, port)) {
            return false;
        }
        return subscribeReceiverToNetwork(level, port.connectorPos(origin), port.connectorSide(), type, receiver);
    }

    public static int subscribeProviderToPorts(Level level, BlockPos origin, Iterable<FluidPort> ports, FluidType type,
            HbmFluidProvider provider) {
        if (ports == null) {
            return 0;
        }
        int subscribed = 0;
        for (FluidPort port : ports) {
            HbmMachinePerformanceCounters.fluidPortCheck();
            boolean added = subscribeProviderToPort(level, origin, port, type, provider);
            HbmMachinePerformanceCounters.fluidPortSubscription(added);
            if (added) {
                subscribed++;
            }
        }
        return subscribed;
    }

    public static PortSubscribeReport subscribeProviderToPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, FluidType type, HbmFluidProvider provider) {
        if (ports == null) {
            return PortSubscribeReport.empty();
        }
        int attempted = 0;
        int subscribed = 0;
        for (FluidPort port : ports) {
            attempted++;
            HbmMachinePerformanceCounters.fluidPortCheck();
            boolean added = subscribeProviderToPort(level, origin, port, type, provider);
            HbmMachinePerformanceCounters.fluidPortSubscription(added);
            if (added) {
                subscribed++;
            }
        }
        return new PortSubscribeReport(attempted, subscribed);
    }

    public static PortSubscribeDetailReport subscribeProviderToPortsDetailedReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, FluidType type, HbmFluidProvider provider) {
        if (ports == null) {
            return PortSubscribeDetailReport.empty();
        }
        List<PortSubscribeDetail> details = new ArrayList<>();
        for (FluidPort port : ports) {
            details.add(subscribeProviderToPortDetailedReport(level, origin, port, type, provider));
        }
        return PortSubscribeDetailReport.of(details);
    }

    public static int subscribeReceiverToPorts(Level level, BlockPos origin, Iterable<FluidPort> ports, FluidType type,
            HbmFluidReceiver receiver) {
        if (ports == null) {
            return 0;
        }
        int subscribed = 0;
        for (FluidPort port : ports) {
            HbmMachinePerformanceCounters.fluidPortCheck();
            boolean added = subscribeReceiverToPort(level, origin, port, type, receiver);
            HbmMachinePerformanceCounters.fluidPortSubscription(added);
            if (added) {
                subscribed++;
            }
        }
        return subscribed;
    }

    public static PortSubscribeReport subscribeReceiverToPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, FluidType type, HbmFluidReceiver receiver) {
        if (ports == null) {
            return PortSubscribeReport.empty();
        }
        int attempted = 0;
        int subscribed = 0;
        for (FluidPort port : ports) {
            attempted++;
            HbmMachinePerformanceCounters.fluidPortCheck();
            boolean added = subscribeReceiverToPort(level, origin, port, type, receiver);
            HbmMachinePerformanceCounters.fluidPortSubscription(added);
            if (added) {
                subscribed++;
            }
        }
        return new PortSubscribeReport(attempted, subscribed);
    }

    public static PortSubscribeDetailReport subscribeReceiverToPortsDetailedReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, FluidType type, HbmFluidReceiver receiver) {
        if (ports == null) {
            return PortSubscribeDetailReport.empty();
        }
        List<PortSubscribeDetail> details = new ArrayList<>();
        for (FluidPort port : ports) {
            details.add(subscribeReceiverToPortDetailedReport(level, origin, port, type, receiver));
        }
        return PortSubscribeDetailReport.of(details);
    }

    public static boolean subscribeProviderToNetwork(Level level, BlockPos connectorPos, Direction connectorSide,
            FluidType type, HbmFluidProvider provider) {
        HbmFluidNet fluidNet = getConnectableFluidNet(level, connectorPos, connectorSide, type);
        if (provider == null || fluidNet == null) {
            return false;
        }
        fluidNet.addProvider(provider);
        rememberRemoteVisualPort(level, connectorPos, connectorSide, type, provider, VisualRole.PROVIDER);
        return true;
    }

    public static boolean subscribeReceiverToNetwork(Level level, BlockPos connectorPos, Direction connectorSide,
            FluidType type, HbmFluidReceiver receiver) {
        HbmFluidNet fluidNet = getConnectableFluidNet(level, connectorPos, connectorSide, type);
        if (receiver == null || fluidNet == null) {
            return false;
        }
        fluidNet.addReceiver(receiver);
        rememberRemoteVisualPort(level, connectorPos, connectorSide, type, receiver, VisualRole.RECEIVER);
        return true;
    }

    public static boolean unsubscribeProviderFromPort(Level level, BlockPos origin, FluidPort port, FluidType type,
            HbmFluidProvider provider) {
        if (level == null || origin == null || port == null || provider == null) {
            return false;
        }
        if (!isLoadedPort(level, origin, port)) {
            return false;
        }
        return unsubscribeProviderFromNetwork(level, port.connectorPos(origin), port.connectorSide(), type, provider);
    }

    public static boolean unsubscribeReceiverFromPort(Level level, BlockPos origin, FluidPort port, FluidType type,
            HbmFluidReceiver receiver) {
        if (level == null || origin == null || port == null || receiver == null) {
            return false;
        }
        if (!isLoadedPort(level, origin, port)) {
            return false;
        }
        return unsubscribeReceiverFromNetwork(level, port.connectorPos(origin), port.connectorSide(), type, receiver);
    }

    public static int unsubscribeProviderFromPorts(Level level, BlockPos origin, Iterable<FluidPort> ports, FluidType type,
            HbmFluidProvider provider) {
        if (ports == null) {
            return 0;
        }
        int unsubscribed = 0;
        for (FluidPort port : ports) {
            HbmMachinePerformanceCounters.fluidPortCheck();
            if (unsubscribeProviderFromPort(level, origin, port, type, provider)) {
                unsubscribed++;
            }
        }
        return unsubscribed;
    }

    public static PortDetachReport unsubscribeProviderFromPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, FluidType type, HbmFluidProvider provider) {
        if (ports == null) {
            return PortDetachReport.empty();
        }
        int attempted = 0;
        int unsubscribed = 0;
        for (FluidPort port : ports) {
            attempted++;
            HbmMachinePerformanceCounters.fluidPortCheck();
            if (unsubscribeProviderFromPort(level, origin, port, type, provider)) {
                unsubscribed++;
            }
        }
        return new PortDetachReport(attempted, unsubscribed);
    }

    public static PortDetachDetailReport unsubscribeProviderFromPortsDetailedReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, FluidType type, HbmFluidProvider provider) {
        if (ports == null) {
            return PortDetachDetailReport.empty();
        }
        List<PortDetachDetail> details = new ArrayList<>();
        for (FluidPort port : ports) {
            details.add(unsubscribeProviderFromPortDetailedReport(level, origin, port, type, provider));
        }
        return PortDetachDetailReport.of(details);
    }

    public static int unsubscribeReceiverFromPorts(Level level, BlockPos origin, Iterable<FluidPort> ports, FluidType type,
            HbmFluidReceiver receiver) {
        if (ports == null) {
            return 0;
        }
        int unsubscribed = 0;
        for (FluidPort port : ports) {
            HbmMachinePerformanceCounters.fluidPortCheck();
            if (unsubscribeReceiverFromPort(level, origin, port, type, receiver)) {
                unsubscribed++;
            }
        }
        return unsubscribed;
    }

    public static PortDetachReport unsubscribeReceiverFromPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, FluidType type, HbmFluidReceiver receiver) {
        if (ports == null) {
            return PortDetachReport.empty();
        }
        int attempted = 0;
        int unsubscribed = 0;
        for (FluidPort port : ports) {
            attempted++;
            HbmMachinePerformanceCounters.fluidPortCheck();
            if (unsubscribeReceiverFromPort(level, origin, port, type, receiver)) {
                unsubscribed++;
            }
        }
        return new PortDetachReport(attempted, unsubscribed);
    }

    public static PortDetachDetailReport unsubscribeReceiverFromPortsDetailedReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, FluidType type, HbmFluidReceiver receiver) {
        if (ports == null) {
            return PortDetachDetailReport.empty();
        }
        List<PortDetachDetail> details = new ArrayList<>();
        for (FluidPort port : ports) {
            details.add(unsubscribeReceiverFromPortDetailedReport(level, origin, port, type, receiver));
        }
        return PortDetachDetailReport.of(details);
    }

    public static boolean unsubscribeProviderFromNetwork(Level level, BlockPos connectorPos, Direction connectorSide,
            FluidType type, HbmFluidProvider provider) {
        forgetRemoteVisualPort(level, connectorPos, connectorSide, type, provider, VisualRole.PROVIDER);
        HbmFluidNet fluidNet = getConnectableFluidNet(level, connectorPos, connectorSide, type);
        if (provider == null || fluidNet == null || !fluidNet.isProvider(provider)) {
            return false;
        }
        fluidNet.removeProvider(provider);
        return true;
    }

    public static boolean unsubscribeReceiverFromNetwork(Level level, BlockPos connectorPos, Direction connectorSide,
            FluidType type, HbmFluidReceiver receiver) {
        forgetRemoteVisualPort(level, connectorPos, connectorSide, type, receiver, VisualRole.RECEIVER);
        HbmFluidNet fluidNet = getConnectableFluidNet(level, connectorPos, connectorSide, type);
        if (receiver == null || fluidNet == null || !fluidNet.isSubscribed(receiver)) {
            return false;
        }
        fluidNet.removeReceiver(receiver);
        return true;
    }

    public static int tryProvideToPorts(Level level, BlockPos origin, Iterable<FluidPort> ports, FluidType type,
            int pressure, HbmFluidProvider provider) {
        return tryProvideToPortsReport(level, origin, ports, type, pressure, provider).touchedPorts();
    }

    public static PortTransferReport tryProvideToPortsReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            FluidType type, int pressure, HbmFluidProvider provider) {
        if (ports == null) {
            return PortTransferReport.empty();
        }
        int touched = 0;
        int subscribed = 0;
        long transferred = 0L;
        for (FluidPort port : ports) {
            HbmMachinePerformanceCounters.fluidPortCheck();
            boolean added = subscribeProviderToPort(level, origin, port, type, provider);
            HbmMachinePerformanceCounters.fluidPortSubscription(added);
            long moved = provideDirectlyToPort(level, origin, port, type, pressure, provider);
            HbmMachinePerformanceCounters.fluidPortTransfer(moved);
            if (added) {
                subscribed++;
            }
            if (added || moved > 0L) {
                touched++;
            }
            transferred += moved;
        }
        return new PortTransferReport(touched, subscribed, transferred);
    }

    public static PortTransferDetailReport tryProvideToPortsDetailedReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, FluidType type, int pressure, HbmFluidProvider provider) {
        if (ports == null) {
            return PortTransferDetailReport.empty();
        }
        List<PortTransferDetail> details = new ArrayList<>();
        for (FluidPort port : ports) {
            details.add(tryProvideToPortDetailedReport(level, origin, port, type, pressure, provider));
        }
        return PortTransferDetailReport.of(details);
    }

    public static PortSetSnapshot inspectPorts(Level level, BlockPos origin, Iterable<FluidPort> ports, FluidType type) {
        int total = 0;
        int connectable = 0;
        int withNetwork = 0;
        int links = 0;
        int providers = 0;
        int receivers = 0;
        if (ports != null) {
            for (FluidPort port : ports) {
                PortSnapshot snapshot = inspectPort(level, origin, port, type);
                total++;
                if (snapshot.connectable()) {
                    connectable++;
                }
                if (snapshot.networkPresent()) {
                    withNetwork++;
                    links += snapshot.links();
                    providers += snapshot.providers();
                    receivers += snapshot.receivers();
                }
            }
        }
        return new PortSetSnapshot(total, connectable, withNetwork, links, providers, receivers);
    }

    public static PortSnapshot inspectPort(Level level, BlockPos origin, FluidPort port, FluidType type) {
        FluidType normalizedType = type == null ? HbmFluids.NONE : type;
        if (level == null || origin == null || port == null || normalizedType == HbmFluids.NONE) {
            return PortSnapshot.missing(BlockPos.ZERO, Direction.NORTH, normalizedType);
        }
        BlockPos connectorPos = port.connectorPos(origin);
        Direction connectorSide = port.connectorSide();
        if (!isLoadedPort(level, origin, port)) {
            return new PortSnapshot(connectorPos, connectorSide, normalizedType.getName(),
                    false, false, false, false, 0, 0, 0);
        }
        FluidEndpointSnapshot endpoint = inspectEndpoint(level, connectorPos, connectorSide, normalizedType);
        return new PortSnapshot(connectorPos, connectorSide, normalizedType.getName(),
                endpoint.connectorPresent(), endpoint.connectable(), endpoint.nodePresent(), endpoint.networkPresent(),
                endpoint.links(), endpoint.providers(), endpoint.receivers());
    }

    public static FluidEndpointSnapshot inspectEndpoint(Level level, BlockPos pos, Direction side, FluidType type) {
        FluidType normalizedType = type == null ? HbmFluids.NONE : type;
        if (level == null || pos == null) {
            return FluidEndpointSnapshot.missing(BlockPos.ZERO, side, normalizedType);
        }
        boolean loaded = isLoadedBlock(level, pos);
        if (!loaded) {
            return new FluidEndpointSnapshot(pos, side, normalizedType.getName(), false,
                    false, false, false, false, false, false, 0, 0, 0);
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        boolean blockEntityPresent = blockEntity != null;
        boolean connectorPresent = blockEntity instanceof HbmFluidConnector;
        boolean connectable = normalizedType != HbmFluids.NONE
                && connectorPresent
                && ((HbmFluidConnector) blockEntity).canConnectFluid(normalizedType, side);
        HbmFluidNode node = connectable ? HbmFluidNodespace.getNode(level, pos, normalizedType) : null;
        HbmFluidNet fluidNet = node == null ? null : node.getFluidNet();
        boolean networkPresent = fluidNet != null && fluidNet.isValid();
        HbmFluidNet.DebugSnapshot network = networkPresent ? fluidNet.createDebugSnapshot() : null;
        boolean forgeHandlerPresent = blockEntityPresent
                && blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, side).resolve().isPresent();
        return new FluidEndpointSnapshot(pos, side, normalizedType.getName(), true,
                blockEntityPresent, connectorPresent, connectable, node != null, networkPresent,
                forgeHandlerPresent,
                network == null ? 0 : network.links(),
                network == null ? 0 : network.providers(),
                network == null ? 0 : network.receivers());
    }

    public static HbmFluidNet getConnectableFluidNet(Level level, BlockPos connectorPos, Direction connectorSide,
            FluidType type) {
        if (level == null || connectorPos == null || connectorSide == null || type == null || type == HbmFluids.NONE) {
            return null;
        }
        if (!isLoadedBlock(level, connectorPos)) {
            return null;
        }
        BlockEntity connector = LoadedTileAccessCache.getBlockEntity(level, connectorPos);
        if (!(connector instanceof HbmFluidConnector fluidConnector) || !fluidConnector.canConnectFluid(type, connectorSide)) {
            return null;
        }
        HbmFluidNode node = HbmFluidNodespace.getNode(level, connectorPos, type);
        HbmFluidNet fluidNet = node == null ? null : node.getFluidNet();
        return fluidNet != null && fluidNet.isValid() ? fluidNet : null;
    }

    public static boolean hasRemoteVisualPortConnection(BlockGetter level, BlockPos pos, FluidType type,
            Direction side) {
        if (!(level instanceof Level realLevel) || pos == null || side == null) {
            return false;
        }
        FluidType normalizedType = type == null ? HbmFluids.NONE : type;
        if (normalizedType == HbmFluids.NONE) {
            return false;
        }
        Map<VisualPortKey, IdentityHashMap<Object, EnumSet<VisualRole>>> worldPorts =
                REMOTE_VISUAL_PORTS.get(realLevel.dimension());
        if (worldPorts == null) {
            return false;
        }
        IdentityHashMap<Object, EnumSet<VisualRole>> owners =
                worldPorts.get(new VisualPortKey(pos, side, normalizedType));
        return owners != null && !owners.isEmpty();
    }

    public static void clearRemoteVisualPortConnections(Level level) {
        if (level != null) {
            REMOTE_VISUAL_PORTS.remove(level.dimension());
        }
    }

    private static void rememberRemoteVisualPort(Level level, BlockPos connectorPos, Direction connectorSide,
            FluidType type, Object owner, VisualRole role) {
        if (level == null || connectorPos == null || connectorSide == null || type == null
                || type == HbmFluids.NONE || owner == null || role == null) {
            return;
        }
        Map<VisualPortKey, IdentityHashMap<Object, EnumSet<VisualRole>>> worldPorts =
                REMOTE_VISUAL_PORTS.computeIfAbsent(level.dimension(), ignored -> new HashMap<>());
        VisualPortKey key = new VisualPortKey(connectorPos, connectorSide, type);
        IdentityHashMap<Object, EnumSet<VisualRole>> owners =
                worldPorts.computeIfAbsent(key, ignored -> new IdentityHashMap<>());
        EnumSet<VisualRole> roles = owners.computeIfAbsent(owner, ignored -> EnumSet.noneOf(VisualRole.class));
        if (roles.add(role)) {
            refreshRemoteVisualPortBlock(level, connectorPos);
        }
    }

    private static void forgetRemoteVisualPort(Level level, BlockPos connectorPos, Direction connectorSide,
            FluidType type, Object owner, VisualRole role) {
        if (level == null || connectorPos == null || connectorSide == null || type == null
                || type == HbmFluids.NONE || owner == null || role == null) {
            return;
        }
        Map<VisualPortKey, IdentityHashMap<Object, EnumSet<VisualRole>>> worldPorts =
                REMOTE_VISUAL_PORTS.get(level.dimension());
        if (worldPorts == null) {
            return;
        }
        VisualPortKey key = new VisualPortKey(connectorPos, connectorSide, type);
        IdentityHashMap<Object, EnumSet<VisualRole>> owners = worldPorts.get(key);
        if (owners == null) {
            return;
        }
        EnumSet<VisualRole> roles = owners.get(owner);
        if (roles == null || !roles.remove(role)) {
            return;
        }
        if (roles.isEmpty()) {
            owners.remove(owner);
        }
        if (owners.isEmpty()) {
            worldPorts.remove(key);
        }
        if (worldPorts.isEmpty()) {
            REMOTE_VISUAL_PORTS.remove(level.dimension());
        }
        refreshRemoteVisualPortBlock(level, connectorPos);
    }

    private static void refreshRemoteVisualPortBlock(Level level, BlockPos connectorPos) {
        if (level == null || level.isClientSide || connectorPos == null) {
            return;
        }
        Block block = level.getBlockState(connectorPos).getBlock();
        if (block instanceof HbmFluidNodeBlock nodeBlock) {
            nodeBlock.updateFluidConnectionGraph(level, connectorPos);
        }
    }

    public static ForgeFluidTransferReport previewProvideToForgeHandler(BlockEntity target, Direction targetSide,
            FluidType type, int pressure, HbmFluidProvider provider) {
        return provideToForgeHandlerReport(target, targetSide, type, pressure, provider, true);
    }

    public static ForgeFluidTransferReport tryProvideToForgeHandlerReport(BlockEntity target, Direction targetSide,
            FluidType type, int pressure, HbmFluidProvider provider) {
        return provideToForgeHandlerReport(target, targetSide, type, pressure, provider, false);
    }

    public static HbmFluidTransferReport tryProvideToReceiverReport(FluidType type, int pressure,
            HbmFluidProvider provider, HbmFluidReceiver receiver) {
        FluidType normalizedType = type == null ? HbmFluids.NONE : type;
        if (normalizedType == HbmFluids.NONE || provider == null || receiver == null || receiver == provider) {
            return new HbmFluidTransferReport(normalizedType, pressure, provider != null, receiver != null,
                    receiver == provider, 0L, 0L, 0L, 0L, 0L);
        }
        long providerAvailable = Math.max(0L, provider.getFluidAvailable(normalizedType, pressure));
        long providerSpeed = Math.max(0L, provider.getProviderSpeed(normalizedType, pressure));
        long receiverDemand = Math.max(0L, receiver.getDemand(normalizedType, pressure));
        long receiverSpeed = Math.max(0L, receiver.getReceiverSpeed(normalizedType, pressure));
        long offered = Math.min(providerAvailable, providerSpeed);
        long requested = Math.min(offered, Math.min(receiverDemand, receiverSpeed));
        if (requested <= 0L) {
            return new HbmFluidTransferReport(normalizedType, pressure, true, true, false,
                    providerAvailable, providerSpeed, receiverDemand, receiverSpeed, 0L);
        }
        long accepted = requested - Math.max(0L, receiver.transferFluid(normalizedType, pressure, requested));
        if (accepted > 0L) {
            provider.useUpFluid(normalizedType, pressure, accepted);
        }
        return new HbmFluidTransferReport(normalizedType, pressure, true, true, false,
                providerAvailable, providerSpeed, receiverDemand, receiverSpeed, accepted);
    }

    private static PortSubscribeDetail subscribeProviderToPortDetailedReport(Level level, BlockPos origin,
            FluidPort port, FluidType type, HbmFluidProvider provider) {
        FluidType normalizedType = type == null ? HbmFluids.NONE : type;
        if (level == null || origin == null || port == null || normalizedType == HbmFluids.NONE || provider == null) {
            return PortSubscribeDetail.empty(port, normalizedType);
        }
        boolean loadedPort = isLoadedPort(level, origin, port);
        if (!loadedPort) {
            FluidEndpointSnapshot endpoint = inspectEndpoint(level, port.connectorPos(origin), port.connectorSide(),
                    normalizedType);
            return new PortSubscribeDetail(port, endpoint, false, false);
        }
        boolean subscribed = subscribeProviderToNetwork(
                level, port.connectorPos(origin), port.connectorSide(), normalizedType, provider);
        FluidEndpointSnapshot endpoint = inspectEndpoint(level, port.connectorPos(origin), port.connectorSide(),
                normalizedType);
        return new PortSubscribeDetail(port, endpoint, true, subscribed);
    }

    private static PortSubscribeDetail subscribeReceiverToPortDetailedReport(Level level, BlockPos origin,
            FluidPort port, FluidType type, HbmFluidReceiver receiver) {
        FluidType normalizedType = type == null ? HbmFluids.NONE : type;
        if (level == null || origin == null || port == null || normalizedType == HbmFluids.NONE || receiver == null) {
            return PortSubscribeDetail.empty(port, normalizedType);
        }
        boolean loadedPort = isLoadedPort(level, origin, port);
        if (!loadedPort) {
            FluidEndpointSnapshot endpoint = inspectEndpoint(level, port.connectorPos(origin), port.connectorSide(),
                    normalizedType);
            return new PortSubscribeDetail(port, endpoint, false, false);
        }
        boolean subscribed = subscribeReceiverToNetwork(
                level, port.connectorPos(origin), port.connectorSide(), normalizedType, receiver);
        FluidEndpointSnapshot endpoint = inspectEndpoint(level, port.connectorPos(origin), port.connectorSide(),
                normalizedType);
        return new PortSubscribeDetail(port, endpoint, true, subscribed);
    }

    private static PortDetachDetail unsubscribeProviderFromPortDetailedReport(Level level, BlockPos origin,
            FluidPort port, FluidType type, HbmFluidProvider provider) {
        FluidType normalizedType = type == null ? HbmFluids.NONE : type;
        if (level == null || origin == null || port == null || provider == null) {
            return PortDetachDetail.empty(port, normalizedType);
        }
        boolean loadedPort = isLoadedPort(level, origin, port);
        if (!loadedPort) {
            FluidEndpointSnapshot endpoint = inspectEndpoint(level, port.connectorPos(origin), port.connectorSide(),
                    normalizedType);
            return new PortDetachDetail(port, endpoint, false, false);
        }
        boolean detached = unsubscribeProviderFromNetwork(
                level, port.connectorPos(origin), port.connectorSide(), normalizedType, provider);
        FluidEndpointSnapshot endpoint = inspectEndpoint(level, port.connectorPos(origin), port.connectorSide(),
                normalizedType);
        return new PortDetachDetail(port, endpoint, true, detached);
    }

    private static PortDetachDetail unsubscribeReceiverFromPortDetailedReport(Level level, BlockPos origin,
            FluidPort port, FluidType type, HbmFluidReceiver receiver) {
        FluidType normalizedType = type == null ? HbmFluids.NONE : type;
        if (level == null || origin == null || port == null || receiver == null) {
            return PortDetachDetail.empty(port, normalizedType);
        }
        boolean loadedPort = isLoadedPort(level, origin, port);
        if (!loadedPort) {
            FluidEndpointSnapshot endpoint = inspectEndpoint(level, port.connectorPos(origin), port.connectorSide(),
                    normalizedType);
            return new PortDetachDetail(port, endpoint, false, false);
        }
        boolean detached = unsubscribeReceiverFromNetwork(
                level, port.connectorPos(origin), port.connectorSide(), normalizedType, receiver);
        FluidEndpointSnapshot endpoint = inspectEndpoint(level, port.connectorPos(origin), port.connectorSide(),
                normalizedType);
        return new PortDetachDetail(port, endpoint, true, detached);
    }

    private static PortTransferDetail tryProvideToPortDetailedReport(Level level, BlockPos origin, FluidPort port,
            FluidType type, int pressure, HbmFluidProvider provider) {
        FluidType normalizedType = type == null ? HbmFluids.NONE : type;
        if (level == null || origin == null || port == null || normalizedType == HbmFluids.NONE || provider == null) {
            return PortTransferDetail.empty(port, normalizedType);
        }
        if (!isLoadedPort(level, origin, port)) {
            FluidEndpointSnapshot endpoint = inspectEndpoint(level, port.connectorPos(origin), port.connectorSide(),
                    normalizedType);
            return new PortTransferDetail(port, endpoint, false, false, false, 0L, null, null);
        }
        boolean subscribed = subscribeProviderToPort(level, origin, port, normalizedType, provider);
        return provideDirectlyToPortReport(level, origin, port, normalizedType, pressure, provider, subscribed);
    }

    private static PortTransferDetail provideDirectlyToPortReport(Level level, BlockPos origin, FluidPort port,
            FluidType type, int pressure, HbmFluidProvider provider, boolean subscribed) {
        FluidEndpointSnapshot endpoint = inspectEndpoint(
                level, port.connectorPos(origin), port.connectorSide(), type);
        if (!endpoint.loaded() || type == null || type == HbmFluids.NONE || provider == null) {
            return new PortTransferDetail(port, endpoint, subscribed, false, false, 0L, null, null);
        }
        BlockEntity target = LoadedTileAccessCache.getBlockEntity(level, endpoint.pos());
        boolean hbmReceiver = target instanceof HbmFluidReceiver;
        boolean connectorTarget = target instanceof HbmFluidConnector;
        if (connectorTarget && !((HbmFluidConnector) target).canConnectFluid(type, endpoint.side())) {
            return new PortTransferDetail(port, endpoint, subscribed, false, false, 0L, null, null);
        }
        if (!hbmReceiver) {
            ForgeFluidTransferReport forgeTransfer = tryProvideToForgeHandlerReport(
                    target, endpoint.side(), type, pressure, provider);
            return new PortTransferDetail(port, endpoint, subscribed, false, true,
                    forgeTransfer.acceptedMb(), null, forgeTransfer);
        }
        HbmFluidReceiver receiver = (HbmFluidReceiver) target;
        if (receiver == provider || (!connectorTarget && endpoint.side() == null)) {
            return new PortTransferDetail(port, endpoint, subscribed, false, false, 0L, null, null);
        }
        HbmFluidTransferReport hbmTransfer = tryProvideToReceiverReport(type, pressure, provider, receiver);
        return new PortTransferDetail(port, endpoint, subscribed, true, false,
                hbmTransfer.acceptedMb(), hbmTransfer, null);
    }

    private static long provideDirectlyToPort(Level level, BlockPos origin, FluidPort port, FluidType type, int pressure,
            HbmFluidProvider provider) {
        FluidType normalizedType = type == null ? HbmFluids.NONE : type;
        if (level == null || origin == null || port == null || normalizedType == HbmFluids.NONE || provider == null) {
            return 0L;
        }
        if (!isLoadedPort(level, origin, port)) {
            return 0L;
        }
        BlockPos targetPos = port.connectorPos(origin);
        Direction targetSide = port.connectorSide();
        BlockEntity target = LoadedTileAccessCache.getBlockEntity(level, targetPos);
        if (target instanceof HbmFluidConnector connector && !connector.canConnectFluid(normalizedType, targetSide)) {
            return 0L;
        }
        if (target instanceof HbmFluidReceiver receiver) {
            if (receiver == provider || (!(target instanceof HbmFluidConnector) && targetSide == null)) {
                return 0L;
            }
            return tryProvideToReceiverReport(normalizedType, pressure, provider, receiver).acceptedMb();
        }
        return tryProvideToForgeHandlerReport(target, targetSide, normalizedType, pressure, provider).acceptedMb();
    }

    private static ForgeFluidTransferReport provideToForgeHandlerReport(BlockEntity target, Direction targetSide,
            FluidType type, int pressure, HbmFluidProvider provider, boolean simulate) {
        FluidType normalizedType = type == null ? HbmFluids.NONE : type;
        boolean targetPresent = target != null;
        boolean standardPressure = HbmForgeFluidInterop.isStandardPressure(pressure);
        boolean exportMapped = normalizedType != HbmFluids.NONE && HbmFluidForgeMappings.canExport(normalizedType);
        int amount = 0;
        if (provider != null && normalizedType != HbmFluids.NONE) {
            amount = (int) Math.min(Integer.MAX_VALUE, Math.min(
                    Math.max(0L, provider.getFluidAvailable(normalizedType, pressure)),
                    Math.max(0L, provider.getProviderSpeed(normalizedType, pressure))));
        }
        if (!targetPresent || provider == null || normalizedType == HbmFluids.NONE || !standardPressure || !exportMapped
                || amount <= 0) {
            return new ForgeFluidTransferReport(normalizedType, pressure, simulate, targetPresent, standardPressure,
                    exportMapped, false, amount, 0);
        }
        FluidStack stack = HbmFluidForgeMappings.toForge(normalizedType, amount);
        if (stack.isEmpty()) {
            return new ForgeFluidTransferReport(normalizedType, pressure, simulate, true, standardPressure,
                    true, false, amount, 0);
        }
        final FluidType reportType = normalizedType;
        final int reportPressure = pressure;
        final boolean reportSimulated = simulate;
        final boolean reportStandardPressure = standardPressure;
        final int offeredAmount = amount;
        final FluidStack offeredStack = stack;
        final IFluidHandler.FluidAction action = reportSimulated
                ? IFluidHandler.FluidAction.SIMULATE
                : IFluidHandler.FluidAction.EXECUTE;
        ForgeFluidTransferReport report = target.getCapability(ForgeCapabilities.FLUID_HANDLER, targetSide)
                .map(handler -> new ForgeFluidTransferReport(reportType, reportPressure, reportSimulated, true,
                        reportStandardPressure, true, true, offeredAmount, handler.fill(offeredStack, action)))
                .orElse(new ForgeFluidTransferReport(reportType, reportPressure, reportSimulated, true,
                        reportStandardPressure, true, false, offeredAmount, 0));
        if (!reportSimulated && report.acceptedMb() > 0) {
            provider.useUpFluid(reportType, reportPressure, report.acceptedMb());
        }
        return report;
    }

    public static boolean isLoadedPort(Level level, BlockPos origin, FluidPort port) {
        if (level == null || origin == null || port == null) {
            return false;
        }
        BlockPos connectorPos = port.connectorPos(origin);
        return isLoadedBlock(level, connectorPos)
                && isLoadedBlock(level, connectorPos.relative(port.direction().getOpposite()));
    }

    public static boolean isLoadedBlock(Level level, BlockPos pos) {
        return level != null && pos != null && level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public record PortSnapshot(
            BlockPos connectorPos,
            Direction connectorSide,
            String fluid,
            boolean connectorPresent,
            boolean connectable,
            boolean nodePresent,
            boolean networkPresent,
            int links,
            int providers,
            int receivers) {
        private static PortSnapshot missing(BlockPos connectorPos, Direction connectorSide, FluidType type) {
            FluidType normalizedType = type == null ? HbmFluids.NONE : type;
            return new PortSnapshot(connectorPos, connectorSide, normalizedType.getName(),
                    false, false, false, false, 0, 0, 0);
        }
    }

    private enum VisualRole {
        PROVIDER,
        RECEIVER
    }

    private record VisualPortKey(BlockPos pos, Direction side, FluidType type) {
        private VisualPortKey {
            Objects.requireNonNull(pos, "pos");
            Objects.requireNonNull(side, "side");
            Objects.requireNonNull(type, "type");
            pos = pos.immutable();
        }
    }

    public record PortSetSnapshot(
            int totalPorts,
            int connectablePorts,
            int networkedPorts,
            int links,
            int providers,
            int receivers) {
    }

    public record FluidEndpointSnapshot(
            BlockPos pos,
            Direction side,
            String fluid,
            boolean loaded,
            boolean blockEntityPresent,
            boolean connectorPresent,
            boolean connectable,
            boolean nodePresent,
            boolean networkPresent,
            boolean forgeHandlerPresent,
            int links,
            int providers,
            int receivers) {
        private static FluidEndpointSnapshot missing(BlockPos pos, Direction side, FluidType type) {
            FluidType normalizedType = type == null ? HbmFluids.NONE : type;
            return new FluidEndpointSnapshot(pos, side, normalizedType.getName(), false,
                    false, false, false, false, false, false, 0, 0, 0);
        }
    }

    public record PortTransferReport(
            int touchedPorts,
            int subscribedPorts,
            long transferredMb) {
        public static PortTransferReport empty() {
            return new PortTransferReport(0, 0, 0L);
        }
    }

    public record PortTransferDetailReport(
            int attemptedPorts,
            int touchedPorts,
            int subscribedPorts,
            long transferredMb,
            List<PortTransferDetail> details) {
        public static PortTransferDetailReport empty() {
            return new PortTransferDetailReport(0, 0, 0, 0L, List.of());
        }

        private static PortTransferDetailReport of(List<PortTransferDetail> details) {
            if (details == null || details.isEmpty()) {
                return empty();
            }
            int touched = 0;
            int subscribed = 0;
            long transferred = 0L;
            for (PortTransferDetail detail : details) {
                if (detail == null) {
                    continue;
                }
                if (detail.touched()) {
                    touched++;
                }
                if (detail.subscribed()) {
                    subscribed++;
                }
                transferred += detail.transferredMb();
            }
            return new PortTransferDetailReport(details.size(), touched, subscribed, transferred, List.copyOf(details));
        }

        public PortTransferReport summary() {
            return new PortTransferReport(touchedPorts, subscribedPorts, transferredMb);
        }
    }

    public record PortTransferDetail(
            FluidPort port,
            FluidEndpointSnapshot endpoint,
            boolean subscribed,
            boolean hbmReceiverPath,
            boolean forgeFallbackPath,
            long transferredMb,
            HbmFluidTransferReport hbmTransfer,
            ForgeFluidTransferReport forgeTransfer) {
        private static PortTransferDetail empty(FluidPort port, FluidType type) {
            return new PortTransferDetail(port, FluidEndpointSnapshot.missing(BlockPos.ZERO, Direction.NORTH, type),
                    false, false, false, 0L, null, null);
        }

        public boolean touched() {
            return subscribed || transferredMb > 0L;
        }
    }

    public record HbmFluidTransferReport(
            FluidType type,
            int pressure,
            boolean providerPresent,
            boolean receiverPresent,
            boolean selfTransfer,
            long providerAvailableMb,
            long providerSpeedMb,
            long receiverDemandMb,
            long receiverSpeedMb,
            long acceptedMb) {
        public boolean moved() {
            return acceptedMb > 0L;
        }
    }

    public record ForgeFluidTransferReport(
            FluidType type,
            int pressure,
            boolean simulated,
            boolean targetPresent,
            boolean standardPressure,
            boolean exportMapped,
            boolean handlerPresent,
            int offeredMb,
            int acceptedMb) {
        public boolean moved() {
            return acceptedMb > 0;
        }
    }

    public record PortSubscribeReport(
            int attemptedPorts,
            int subscribedPorts) {
        public static PortSubscribeReport empty() {
            return new PortSubscribeReport(0, 0);
        }
    }

    public record PortSubscribeDetailReport(
            int attemptedPorts,
            int subscribedPorts,
            List<PortSubscribeDetail> details) {
        public static PortSubscribeDetailReport empty() {
            return new PortSubscribeDetailReport(0, 0, List.of());
        }

        private static PortSubscribeDetailReport of(List<PortSubscribeDetail> details) {
            if (details == null || details.isEmpty()) {
                return empty();
            }
            int subscribed = 0;
            for (PortSubscribeDetail detail : details) {
                if (detail != null && detail.subscribed()) {
                    subscribed++;
                }
            }
            return new PortSubscribeDetailReport(details.size(), subscribed, List.copyOf(details));
        }

        public PortSubscribeReport summary() {
            return new PortSubscribeReport(attemptedPorts, subscribedPorts);
        }
    }

    public record PortSubscribeDetail(
            FluidPort port,
            FluidEndpointSnapshot endpoint,
            boolean loadedPort,
            boolean subscribed) {
        private static PortSubscribeDetail empty(FluidPort port, FluidType type) {
            return new PortSubscribeDetail(port, FluidEndpointSnapshot.missing(BlockPos.ZERO, Direction.NORTH, type),
                    false, false);
        }
    }

    public record PortDetachReport(
            int attemptedPorts,
            int unsubscribedPorts) {
        public static PortDetachReport empty() {
            return new PortDetachReport(0, 0);
        }
    }

    public record PortDetachDetailReport(
            int attemptedPorts,
            int unsubscribedPorts,
            List<PortDetachDetail> details) {
        public static PortDetachDetailReport empty() {
            return new PortDetachDetailReport(0, 0, List.of());
        }

        private static PortDetachDetailReport of(List<PortDetachDetail> details) {
            if (details == null || details.isEmpty()) {
                return empty();
            }
            int unsubscribed = 0;
            for (PortDetachDetail detail : details) {
                if (detail != null && detail.unsubscribed()) {
                    unsubscribed++;
                }
            }
            return new PortDetachDetailReport(details.size(), unsubscribed, List.copyOf(details));
        }

        public PortDetachReport summary() {
            return new PortDetachReport(attemptedPorts, unsubscribedPorts);
        }
    }

    public record PortDetachDetail(
            FluidPort port,
            FluidEndpointSnapshot endpoint,
            boolean loadedPort,
            boolean unsubscribed) {
        private static PortDetachDetail empty(FluidPort port, FluidType type) {
            return new PortDetachDetail(port, FluidEndpointSnapshot.missing(BlockPos.ZERO, Direction.NORTH, type),
                    false, false);
        }
    }
}
