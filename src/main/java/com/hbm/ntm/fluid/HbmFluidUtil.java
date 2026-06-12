package com.hbm.ntm.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Objects;

public final class HbmFluidUtil {
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
        return subscribeProviderToPortsReport(level, origin, ports, type, provider).subscribedPorts();
    }

    public static PortSubscribeReport subscribeProviderToPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, FluidType type, HbmFluidProvider provider) {
        if (ports == null) {
            return PortSubscribeReport.empty();
        }
        int subscribed = 0;
        int attempted = 0;
        for (FluidPort port : ports) {
            attempted++;
            if (subscribeProviderToPort(level, origin, port, type, provider)) {
                subscribed++;
            }
        }
        return new PortSubscribeReport(attempted, subscribed);
    }

    public static int subscribeReceiverToPorts(Level level, BlockPos origin, Iterable<FluidPort> ports, FluidType type,
            HbmFluidReceiver receiver) {
        return subscribeReceiverToPortsReport(level, origin, ports, type, receiver).subscribedPorts();
    }

    public static PortSubscribeReport subscribeReceiverToPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, FluidType type, HbmFluidReceiver receiver) {
        if (ports == null) {
            return PortSubscribeReport.empty();
        }
        int subscribed = 0;
        int attempted = 0;
        for (FluidPort port : ports) {
            attempted++;
            if (subscribeReceiverToPort(level, origin, port, type, receiver)) {
                subscribed++;
            }
        }
        return new PortSubscribeReport(attempted, subscribed);
    }

    public static boolean subscribeProviderToNetwork(Level level, BlockPos connectorPos, Direction connectorSide,
            FluidType type, HbmFluidProvider provider) {
        HbmFluidNet fluidNet = getConnectableFluidNet(level, connectorPos, connectorSide, type);
        if (provider == null || fluidNet == null) {
            return false;
        }
        fluidNet.addProvider(provider);
        return true;
    }

    public static boolean subscribeReceiverToNetwork(Level level, BlockPos connectorPos, Direction connectorSide,
            FluidType type, HbmFluidReceiver receiver) {
        HbmFluidNet fluidNet = getConnectableFluidNet(level, connectorPos, connectorSide, type);
        if (receiver == null || fluidNet == null) {
            return false;
        }
        fluidNet.addReceiver(receiver);
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
        return unsubscribeProviderFromPortsReport(level, origin, ports, type, provider).unsubscribedPorts();
    }

    public static PortDetachReport unsubscribeProviderFromPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, FluidType type, HbmFluidProvider provider) {
        if (ports == null) {
            return PortDetachReport.empty();
        }
        int unsubscribed = 0;
        int attempted = 0;
        for (FluidPort port : ports) {
            attempted++;
            if (unsubscribeProviderFromPort(level, origin, port, type, provider)) {
                unsubscribed++;
            }
        }
        return new PortDetachReport(attempted, unsubscribed);
    }

    public static int unsubscribeReceiverFromPorts(Level level, BlockPos origin, Iterable<FluidPort> ports, FluidType type,
            HbmFluidReceiver receiver) {
        return unsubscribeReceiverFromPortsReport(level, origin, ports, type, receiver).unsubscribedPorts();
    }

    public static PortDetachReport unsubscribeReceiverFromPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, FluidType type, HbmFluidReceiver receiver) {
        if (ports == null) {
            return PortDetachReport.empty();
        }
        int unsubscribed = 0;
        int attempted = 0;
        for (FluidPort port : ports) {
            attempted++;
            if (unsubscribeReceiverFromPort(level, origin, port, type, receiver)) {
                unsubscribed++;
            }
        }
        return new PortDetachReport(attempted, unsubscribed);
    }

    public static boolean unsubscribeProviderFromNetwork(Level level, BlockPos connectorPos, Direction connectorSide,
            FluidType type, HbmFluidProvider provider) {
        HbmFluidNet fluidNet = getConnectableFluidNet(level, connectorPos, connectorSide, type);
        if (provider == null || fluidNet == null || !fluidNet.isProvider(provider)) {
            return false;
        }
        fluidNet.removeProvider(provider);
        return true;
    }

    public static boolean unsubscribeReceiverFromNetwork(Level level, BlockPos connectorPos, Direction connectorSide,
            FluidType type, HbmFluidReceiver receiver) {
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
        int subscribedPorts = 0;
        long transferred = 0L;
        for (FluidPort port : ports) {
            boolean subscribed = subscribeProviderToPort(level, origin, port, type, provider);
            long portTransferred = provideDirectlyToPort(level, origin, port, type, pressure, provider);
            if (subscribed) {
                subscribedPorts++;
            }
            if (subscribed || portTransferred > 0L) {
                touched++;
            }
            transferred += portTransferred;
        }
        return new PortTransferReport(touched, subscribedPorts, transferred);
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
        BlockEntity connector = level.getBlockEntity(connectorPos);
        boolean connectorPresent = connector instanceof HbmFluidConnector;
        boolean connectable = connectorPresent
                && ((HbmFluidConnector) connector).canConnectFluid(normalizedType, connectorSide);
        if (!connectable) {
            return new PortSnapshot(connectorPos, connectorSide, normalizedType.getName(),
                    connectorPresent, false, false, false, 0, 0, 0);
        }
        HbmFluidNode node = HbmFluidNodespace.getNode(level, connectorPos, normalizedType);
        HbmFluidNet fluidNet = node == null ? null : node.getFluidNet();
        boolean networkPresent = fluidNet != null && fluidNet.isValid();
        if (!networkPresent) {
            return new PortSnapshot(connectorPos, connectorSide, normalizedType.getName(),
                    true, true, node != null, false, 0, 0, 0);
        }
        HbmFluidNet.DebugSnapshot snapshot = fluidNet.createDebugSnapshot();
        return new PortSnapshot(connectorPos, connectorSide, normalizedType.getName(),
                true, true, true, true, snapshot.links(), snapshot.providers(), snapshot.receivers());
    }

    public static HbmFluidNet getConnectableFluidNet(Level level, BlockPos connectorPos, Direction connectorSide,
            FluidType type) {
        if (level == null || connectorPos == null || connectorSide == null || type == null || type == HbmFluids.NONE) {
            return null;
        }
        if (!isLoadedBlock(level, connectorPos)) {
            return null;
        }
        BlockEntity connector = level.getBlockEntity(connectorPos);
        if (!(connector instanceof HbmFluidConnector fluidConnector) || !fluidConnector.canConnectFluid(type, connectorSide)) {
            return null;
        }
        HbmFluidNode node = HbmFluidNodespace.getNode(level, connectorPos, type);
        HbmFluidNet fluidNet = node == null ? null : node.getFluidNet();
        return fluidNet != null && fluidNet.isValid() ? fluidNet : null;
    }

    private static long provideDirectlyToPort(Level level, BlockPos origin, FluidPort port, FluidType type, int pressure,
            HbmFluidProvider provider) {
        if (level == null || origin == null || port == null || type == null || type == HbmFluids.NONE || provider == null) {
            return 0L;
        }
        BlockPos targetPos = port.connectorPos(origin);
        if (!isLoadedPort(level, origin, port)) {
            return 0L;
        }
        Direction targetSide = port.connectorSide();
        BlockEntity target = level.getBlockEntity(targetPos);
        if (target instanceof HbmFluidConnector connector && !connector.canConnectFluid(type, targetSide)) {
            return 0L;
        }
        if (!(target instanceof HbmFluidReceiver receiver)
                || receiver == provider
                || !(target instanceof HbmFluidConnector)) {
            return provideDirectlyToForgeHandler(level, target, targetSide, type, pressure, provider);
        }
        long provides = Math.min(Math.max(0L, provider.getFluidAvailable(type, pressure)),
                Math.max(0L, provider.getProviderSpeed(type, pressure)));
        long receives = Math.min(Math.max(0L, receiver.getDemand(type, pressure)),
                Math.max(0L, receiver.getReceiverSpeed(type, pressure)));
        long toTransfer = Math.min(provides, receives);
        if (toTransfer <= 0L) {
            return 0L;
        }
        long accepted = toTransfer - receiver.transferFluid(type, pressure, toTransfer);
        if (accepted > 0L) {
            provider.useUpFluid(type, pressure, accepted);
        }
        return accepted;
    }

    private static long provideDirectlyToForgeHandler(Level level, BlockEntity target, Direction targetSide,
            FluidType type, int pressure, HbmFluidProvider provider) {
        if (target == null || !HbmForgeFluidInterop.isStandardPressure(pressure)
                || !HbmFluidForgeMappings.canExport(type)) {
            return 0L;
        }
        int amount = (int) Math.min(Integer.MAX_VALUE, Math.min(
                Math.max(0L, provider.getFluidAvailable(type, pressure)),
                Math.max(0L, provider.getProviderSpeed(type, pressure))));
        if (amount <= 0) {
            return 0L;
        }
        FluidStack stack = HbmFluidForgeMappings.toForge(type, amount);
        if (stack.isEmpty()) {
            return 0L;
        }
        long accepted = target.getCapability(ForgeCapabilities.FLUID_HANDLER, targetSide)
                .map(handler -> (long) handler.fill(stack, IFluidHandler.FluidAction.EXECUTE))
                .orElse(0L);
        if (accepted > 0L) {
            provider.useUpFluid(type, pressure, accepted);
        }
        return accepted;
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

    public record PortSetSnapshot(
            int totalPorts,
            int connectablePorts,
            int networkedPorts,
            int links,
            int providers,
            int receivers) {
    }

    public record PortTransferReport(
            int touchedPorts,
            int subscribedPorts,
            long transferredMb) {
        public static PortTransferReport empty() {
            return new PortTransferReport(0, 0, 0L);
        }
    }

    public record PortSubscribeReport(
            int attemptedPorts,
            int subscribedPorts) {
        public static PortSubscribeReport empty() {
            return new PortSubscribeReport(0, 0);
        }
    }

    public record PortDetachReport(
            int attemptedPorts,
            int unsubscribedPorts) {
        public static PortDetachReport empty() {
            return new PortDetachReport(0, 0);
        }
    }
}
