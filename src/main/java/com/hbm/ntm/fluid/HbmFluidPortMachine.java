package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluidUtil.PortDetachReport;
import com.hbm.ntm.fluid.HbmFluidUtil.PortSubscribeReport;
import com.hbm.ntm.fluid.HbmFluidUtil.PortTransferReport;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

/**
 * Shared 1.7.10-style remote fluid port helpers for machines that are not
 * full fluid network nodes.
 */
public final class HbmFluidPortMachine {
    private HbmFluidPortMachine() {
    }

    public static int refreshReceiverPorts(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        return refreshReceiverPortsReport(level, origin, ports, receivingTanks, receiver).receiverPorts();
    }

    public static PortMachineRefreshReport refreshReceiverPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        if (level == null || level.isClientSide || receivingTanks == null || receiver == null) {
            return PortMachineRefreshReport.empty();
        }
        int touched = 0;
        Set<FluidType> seenTypes = new HashSet<>();
        for (HbmFluidTank tank : receivingTanks) {
            if (tank != null && tank.getTankType() != HbmFluids.NONE && seenTypes.add(tank.getTankType())) {
                PortSubscribeReport report = HbmFluidUtil.subscribeReceiverToPortsReport(
                        level, origin, ports, tank.getTankType(), receiver);
                touched += report.subscribedPorts();
            }
        }
        return new PortMachineRefreshReport(seenTypes.size(), touched, 0, 0, 0L);
    }

    public static int refreshProviderPorts(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        return refreshProviderPortsReport(level, origin, ports, sendingTanks, provider).providerPorts();
    }

    public static PortMachineRefreshReport refreshProviderPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        if (level == null || level.isClientSide || sendingTanks == null || provider == null) {
            return PortMachineRefreshReport.empty();
        }
        int touched = 0;
        long transferred = 0L;
        Set<FluidKey> seenKeys = new HashSet<>();
        for (HbmFluidTank tank : sendingTanks) {
            if (tank != null
                    && tank.getTankType() != HbmFluids.NONE
                    && tank.getFill() > 0
                    && seenKeys.add(new FluidKey(tank.getTankType(), tank.getPressure()))) {
                PortTransferReport report = HbmFluidUtil.tryProvideToPortsReport(level, origin, ports,
                        tank.getTankType(), tank.getPressure(), provider);
                touched += report.touchedPorts();
                transferred += report.transferredMb();
            }
        }
        return new PortMachineRefreshReport(0, 0, seenKeys.size(), touched, transferred);
    }

    public static int refreshTransceiverPorts(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        return refreshTransceiverPortsReport(level, origin, ports, receivingTanks, sendingTanks, transceiver)
                .touchedPorts();
    }

    public static PortMachineRefreshReport refreshTransceiverPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        return refreshReceiverPortsReport(level, origin, ports, receivingTanks, transceiver)
                .merge(refreshProviderPortsReport(level, origin, ports, sendingTanks, transceiver));
    }

    public static int detachReceiverPorts(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        return detachReceiverPortsReport(level, origin, ports, receivingTanks, receiver).receiverPorts();
    }

    public static PortMachineDetachReport detachReceiverPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        return detachReceiverPortsForTypesReport(level, origin, ports, fluidTypes(receivingTanks), receiver);
    }

    public static PortMachineDetachReport detachReceiverPortsForTypesReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<FluidType> types, HbmFluidReceiver receiver) {
        if (level == null || level.isClientSide || types == null || receiver == null) {
            return PortMachineDetachReport.empty();
        }
        int receiverPorts = 0;
        Set<FluidType> seenTypes = new HashSet<>();
        for (FluidType type : types) {
            if (type != null && type != HbmFluids.NONE && seenTypes.add(type)) {
                PortDetachReport report = HbmFluidUtil.unsubscribeReceiverFromPortsReport(
                        level, origin, ports, type, receiver);
                receiverPorts += report.unsubscribedPorts();
            }
        }
        return new PortMachineDetachReport(seenTypes.size(), receiverPorts, 0, 0);
    }

    public static int detachProviderPorts(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        return detachProviderPortsReport(level, origin, ports, sendingTanks, provider).providerPorts();
    }

    public static PortMachineDetachReport detachProviderPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        return detachProviderPortsForTypesReport(level, origin, ports, fluidTypes(sendingTanks), provider);
    }

    public static PortMachineDetachReport detachProviderPortsForTypesReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<FluidType> types, HbmFluidProvider provider) {
        if (level == null || level.isClientSide || types == null || provider == null) {
            return PortMachineDetachReport.empty();
        }
        int providerPorts = 0;
        Set<FluidType> seenTypes = new HashSet<>();
        for (FluidType type : types) {
            if (type != null && type != HbmFluids.NONE && seenTypes.add(type)) {
                PortDetachReport report = HbmFluidUtil.unsubscribeProviderFromPortsReport(
                        level, origin, ports, type, provider);
                providerPorts += report.unsubscribedPorts();
            }
        }
        return new PortMachineDetachReport(0, 0, seenTypes.size(), providerPorts);
    }

    public static int detachTransceiverPorts(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        return detachTransceiverPortsReport(level, origin, ports, receivingTanks, sendingTanks, transceiver)
                .touchedPorts();
    }

    public static PortMachineDetachReport detachTransceiverPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        return detachReceiverPortsReport(level, origin, ports, receivingTanks, transceiver)
                .merge(detachProviderPortsReport(level, origin, ports, sendingTanks, transceiver));
    }

    private static Set<FluidType> fluidTypes(Iterable<HbmFluidTank> tanks) {
        Set<FluidType> types = new HashSet<>();
        if (tanks != null) {
            for (HbmFluidTank tank : tanks) {
                if (tank != null && tank.getTankType() != HbmFluids.NONE) {
                    types.add(tank.getTankType());
                }
            }
        }
        return types;
    }

    private record FluidKey(FluidType type, int pressure) {
    }

    public record PortMachineRefreshReport(
            int receiverTypes,
            int receiverPorts,
            int providerKeys,
            int providerPorts,
            long directTransferMb) {
        public static PortMachineRefreshReport empty() {
            return new PortMachineRefreshReport(0, 0, 0, 0, 0L);
        }

        public int touchedPorts() {
            return receiverPorts + providerPorts;
        }

        public PortMachineRefreshReport merge(PortMachineRefreshReport other) {
            if (other == null) {
                return this;
            }
            return new PortMachineRefreshReport(
                    receiverTypes + other.receiverTypes,
                    receiverPorts + other.receiverPorts,
                    providerKeys + other.providerKeys,
                    providerPorts + other.providerPorts,
                    directTransferMb + other.directTransferMb);
        }
    }

    public record PortMachineDetachReport(
            int receiverTypes,
            int receiverPorts,
            int providerTypes,
            int providerPorts) {
        public static PortMachineDetachReport empty() {
            return new PortMachineDetachReport(0, 0, 0, 0);
        }

        public int touchedPorts() {
            return receiverPorts + providerPorts;
        }

        public PortMachineDetachReport merge(PortMachineDetachReport other) {
            if (other == null) {
                return this;
            }
            return new PortMachineDetachReport(
                    receiverTypes + other.receiverTypes,
                    receiverPorts + other.receiverPorts,
                    providerTypes + other.providerTypes,
                    providerPorts + other.providerPorts);
        }
    }
}
