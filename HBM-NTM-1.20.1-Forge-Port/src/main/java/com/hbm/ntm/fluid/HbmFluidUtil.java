package com.hbm.ntm.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

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
        return subscribeProviderToNetwork(level, port.connectorPos(origin), port.connectorSide(), type, provider);
    }

    public static boolean subscribeReceiverToPort(Level level, BlockPos origin, FluidPort port, FluidType type,
            HbmFluidReceiver receiver) {
        if (level == null || origin == null || port == null || type == null || type == HbmFluids.NONE || receiver == null) {
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
            if (subscribeProviderToPort(level, origin, port, type, provider)) {
                subscribed++;
            }
        }
        return subscribed;
    }

    public static int subscribeReceiverToPorts(Level level, BlockPos origin, Iterable<FluidPort> ports, FluidType type,
            HbmFluidReceiver receiver) {
        if (ports == null) {
            return 0;
        }
        int subscribed = 0;
        for (FluidPort port : ports) {
            if (subscribeReceiverToPort(level, origin, port, type, receiver)) {
                subscribed++;
            }
        }
        return subscribed;
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

    public static int tryProvideToPorts(Level level, BlockPos origin, Iterable<FluidPort> ports, FluidType type,
            int pressure, HbmFluidProvider provider) {
        if (ports == null) {
            return 0;
        }
        int touched = 0;
        for (FluidPort port : ports) {
            boolean subscribed = subscribeProviderToPort(level, origin, port, type, provider);
            long transferred = provideDirectlyToPort(level, origin, port, type, pressure, provider);
            if (subscribed || transferred > 0L) {
                touched++;
            }
        }
        return touched;
    }

    public static HbmFluidNet getConnectableFluidNet(Level level, BlockPos connectorPos, Direction connectorSide,
            FluidType type) {
        if (level == null || connectorPos == null || connectorSide == null || type == null || type == HbmFluids.NONE) {
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
        Direction targetSide = port.connectorSide();
        BlockEntity target = level.getBlockEntity(targetPos);
        if (!(target instanceof HbmFluidReceiver receiver)
                || !(target instanceof HbmFluidConnector connector)
                || receiver == provider
                || !connector.canConnectFluid(type, targetSide)) {
            return 0L;
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
}
