package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluidUtil.PortDetachDetailReport;
import com.hbm.ntm.fluid.HbmFluidUtil.PortSubscribeDetailReport;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
                touched += HbmFluidUtil.subscribeReceiverToPortsReport(
                        level, origin, ports, tank.getTankType(), receiver).subscribedPorts();
            }
        }
        return new PortMachineRefreshReport(seenTypes.size(), touched, 0, 0, 0L);
    }

    public static PortMachineRefreshDetailReport refreshReceiverPortsDetailedReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        if (level == null || level.isClientSide || receivingTanks == null || receiver == null) {
            return PortMachineRefreshDetailReport.empty();
        }
        int touched = 0;
        Set<FluidType> seenTypes = new HashSet<>();
        List<ReceiverPortRefreshDetail> receiverDetails = new ArrayList<>();
        for (HbmFluidTank tank : receivingTanks) {
            if (tank != null && tank.getTankType() != HbmFluids.NONE && seenTypes.add(tank.getTankType())) {
                PortSubscribeDetailReport report = HbmFluidUtil.subscribeReceiverToPortsDetailedReport(
                        level, origin, ports, tank.getTankType(), receiver);
                touched += report.subscribedPorts();
                receiverDetails.add(new ReceiverPortRefreshDetail(tank.getTankType(), report));
            }
        }
        return new PortMachineRefreshDetailReport(
                new PortMachineRefreshReport(seenTypes.size(), touched, 0, 0, 0L),
                receiverDetails, List.of());
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
                HbmFluidUtil.PortTransferReport report = HbmFluidUtil.tryProvideToPortsReport(
                        level, origin, ports, tank.getTankType(), tank.getPressure(), provider);
                touched += report.touchedPorts();
                transferred += report.transferredMb();
            }
        }
        return new PortMachineRefreshReport(0, 0, seenKeys.size(), touched, transferred);
    }

    public static PortMachineRefreshDetailReport refreshProviderPortsDetailedReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        if (level == null || level.isClientSide || sendingTanks == null || provider == null) {
            return PortMachineRefreshDetailReport.empty();
        }
        int touched = 0;
        long transferred = 0L;
        Set<FluidKey> seenKeys = new HashSet<>();
        List<ProviderPortRefreshDetail> providerDetails = new ArrayList<>();
        for (HbmFluidTank tank : sendingTanks) {
            if (tank != null
                    && tank.getTankType() != HbmFluids.NONE
                    && tank.getFill() > 0
                    && seenKeys.add(new FluidKey(tank.getTankType(), tank.getPressure()))) {
                HbmFluidUtil.PortTransferDetailReport report = HbmFluidUtil.tryProvideToPortsDetailedReport(
                        level, origin, ports, tank.getTankType(), tank.getPressure(), provider);
                touched += report.touchedPorts();
                transferred += report.transferredMb();
                providerDetails.add(new ProviderPortRefreshDetail(tank.getTankType(), tank.getPressure(), report));
            }
        }
        return new PortMachineRefreshDetailReport(
                new PortMachineRefreshReport(0, 0, seenKeys.size(), touched, transferred),
                List.of(), providerDetails);
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

    public static PortMachineRefreshDetailReport refreshTransceiverPortsDetailedReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        return refreshReceiverPortsDetailedReport(level, origin, ports, receivingTanks, transceiver)
                .merge(refreshProviderPortsDetailedReport(level, origin, ports, sendingTanks, transceiver));
    }

    public static int detachReceiverPorts(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        return detachReceiverPortsReport(level, origin, ports, receivingTanks, receiver).receiverPorts();
    }

    public static PortMachineDetachReport detachReceiverPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        return detachReceiverPortsForTypesReport(level, origin, ports, fluidTypes(receivingTanks), receiver);
    }

    public static PortMachineDetachDetailReport detachReceiverPortsDetailedReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        return detachReceiverPortsForTypesDetailedReport(level, origin, ports, fluidTypes(receivingTanks), receiver);
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
                receiverPorts += HbmFluidUtil.unsubscribeReceiverFromPortsReport(
                        level, origin, ports, type, receiver).unsubscribedPorts();
            }
        }
        return new PortMachineDetachReport(seenTypes.size(), receiverPorts, 0, 0);
    }

    public static PortMachineDetachDetailReport detachReceiverPortsForTypesDetailedReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<FluidType> types, HbmFluidReceiver receiver) {
        if (level == null || level.isClientSide || types == null || receiver == null) {
            return PortMachineDetachDetailReport.empty();
        }
        int receiverPorts = 0;
        Set<FluidType> seenTypes = new HashSet<>();
        List<ReceiverPortDetachDetail> receiverDetails = new ArrayList<>();
        for (FluidType type : types) {
            if (type != null && type != HbmFluids.NONE && seenTypes.add(type)) {
                PortDetachDetailReport report = HbmFluidUtil.unsubscribeReceiverFromPortsDetailedReport(
                        level, origin, ports, type, receiver);
                receiverPorts += report.unsubscribedPorts();
                receiverDetails.add(new ReceiverPortDetachDetail(type, report));
            }
        }
        return new PortMachineDetachDetailReport(
                new PortMachineDetachReport(seenTypes.size(), receiverPorts, 0, 0),
                receiverDetails, List.of());
    }

    public static int detachProviderPorts(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        return detachProviderPortsReport(level, origin, ports, sendingTanks, provider).providerPorts();
    }

    public static PortMachineDetachReport detachProviderPortsReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        return detachProviderPortsForTypesReport(level, origin, ports, fluidTypes(sendingTanks), provider);
    }

    public static PortMachineDetachDetailReport detachProviderPortsDetailedReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        return detachProviderPortsForTypesDetailedReport(level, origin, ports, fluidTypes(sendingTanks), provider);
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
                providerPorts += HbmFluidUtil.unsubscribeProviderFromPortsReport(
                        level, origin, ports, type, provider).unsubscribedPorts();
            }
        }
        return new PortMachineDetachReport(0, 0, seenTypes.size(), providerPorts);
    }

    public static PortMachineDetachDetailReport detachProviderPortsForTypesDetailedReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<FluidType> types, HbmFluidProvider provider) {
        if (level == null || level.isClientSide || types == null || provider == null) {
            return PortMachineDetachDetailReport.empty();
        }
        int providerPorts = 0;
        Set<FluidType> seenTypes = new HashSet<>();
        List<ProviderPortDetachDetail> providerDetails = new ArrayList<>();
        for (FluidType type : types) {
            if (type != null && type != HbmFluids.NONE && seenTypes.add(type)) {
                PortDetachDetailReport report = HbmFluidUtil.unsubscribeProviderFromPortsDetailedReport(
                        level, origin, ports, type, provider);
                providerPorts += report.unsubscribedPorts();
                providerDetails.add(new ProviderPortDetachDetail(type, report));
            }
        }
        return new PortMachineDetachDetailReport(
                new PortMachineDetachReport(0, 0, seenTypes.size(), providerPorts),
                List.of(), providerDetails);
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

    public static PortMachineDetachDetailReport detachTransceiverPortsDetailedReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        return detachReceiverPortsDetailedReport(level, origin, ports, receivingTanks, transceiver)
                .merge(detachProviderPortsDetailedReport(level, origin, ports, sendingTanks, transceiver));
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

    public record PortMachineRefreshDetailReport(
            PortMachineRefreshReport summary,
            List<ReceiverPortRefreshDetail> receiverDetails,
            List<ProviderPortRefreshDetail> providerDetails) {
        public PortMachineRefreshDetailReport {
            summary = summary == null ? PortMachineRefreshReport.empty() : summary;
            receiverDetails = receiverDetails == null ? List.of() : List.copyOf(receiverDetails);
            providerDetails = providerDetails == null ? List.of() : List.copyOf(providerDetails);
        }

        public static PortMachineRefreshDetailReport empty() {
            return new PortMachineRefreshDetailReport(PortMachineRefreshReport.empty(), List.of(), List.of());
        }

        public int touchedPorts() {
            return summary.touchedPorts();
        }

        public PortMachineRefreshDetailReport merge(PortMachineRefreshDetailReport other) {
            if (other == null) {
                return this;
            }
            List<ReceiverPortRefreshDetail> mergedReceivers = new ArrayList<>(receiverDetails);
            mergedReceivers.addAll(other.receiverDetails);
            List<ProviderPortRefreshDetail> mergedProviders = new ArrayList<>(providerDetails);
            mergedProviders.addAll(other.providerDetails);
            return new PortMachineRefreshDetailReport(summary.merge(other.summary), mergedReceivers, mergedProviders);
        }
    }

    public record ReceiverPortRefreshDetail(
            FluidType type,
            PortSubscribeDetailReport subscription) {
    }

    public record ProviderPortRefreshDetail(
            FluidType type,
            int pressure,
            HbmFluidUtil.PortTransferDetailReport transfer) {
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

    public record PortMachineDetachDetailReport(
            PortMachineDetachReport summary,
            List<ReceiverPortDetachDetail> receiverDetails,
            List<ProviderPortDetachDetail> providerDetails) {
        public PortMachineDetachDetailReport {
            summary = summary == null ? PortMachineDetachReport.empty() : summary;
            receiverDetails = receiverDetails == null ? List.of() : List.copyOf(receiverDetails);
            providerDetails = providerDetails == null ? List.of() : List.copyOf(providerDetails);
        }

        public static PortMachineDetachDetailReport empty() {
            return new PortMachineDetachDetailReport(PortMachineDetachReport.empty(), List.of(), List.of());
        }

        public int touchedPorts() {
            return summary.touchedPorts();
        }

        public PortMachineDetachDetailReport merge(PortMachineDetachDetailReport other) {
            if (other == null) {
                return this;
            }
            List<ReceiverPortDetachDetail> mergedReceivers = new ArrayList<>(receiverDetails);
            mergedReceivers.addAll(other.receiverDetails);
            List<ProviderPortDetachDetail> mergedProviders = new ArrayList<>(providerDetails);
            mergedProviders.addAll(other.providerDetails);
            return new PortMachineDetachDetailReport(summary.merge(other.summary), mergedReceivers, mergedProviders);
        }
    }

    public record ReceiverPortDetachDetail(
            FluidType type,
            PortDetachDetailReport detach) {
    }

    public record ProviderPortDetachDetail(
            FluidType type,
            PortDetachDetailReport detach) {
    }
}
