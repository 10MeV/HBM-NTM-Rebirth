package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.HbmFluidPortMachine.PortMachineDetachReport;
import com.hbm.ntm.fluid.HbmFluidPortMachine.PortMachineDetachDetailReport;
import com.hbm.ntm.fluid.HbmFluidPortMachine.PortMachineRefreshDetailReport;
import com.hbm.ntm.fluid.HbmFluidPortMachine.PortMachineRefreshReport;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Tracks the remote-port fluid types a machine advertised last tick and
 * detaches obsolete subscriptions before refreshing the current 1.7.10-style
 * provider/receiver loops.
 */
public final class HbmFluidPortSubscriptionTracker {
    private final Set<FluidType> receiverTypes = new HashSet<>();
    private final Set<FluidType> providerTypes = new HashSet<>();

    public TrackedPortRefreshReport refreshReceiver(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        return refreshReceiverDetailed(level, origin, ports, receivingTanks, receiver).summary();
    }

    public TrackedPortRefreshDetailReport refreshReceiverDetailed(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        Set<FluidType> activeReceivers = receiverTypes(receivingTanks);
        PortMachineDetachDetailReport detach = detachObsoleteReceiversDetailed(
                level, origin, ports, activeReceivers, receiver);
        PortMachineRefreshDetailReport refresh = HbmFluidPortMachine.refreshReceiverPortsDetailedReport(
                level, origin, ports, receivingTanks, receiver);
        receiverTypes.clear();
        receiverTypes.addAll(activeReceivers);
        return new TrackedPortRefreshDetailReport(detach, refresh);
    }

    public TrackedPortRefreshReport refreshProvider(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        return refreshProviderDetailed(level, origin, ports, sendingTanks, provider).summary();
    }

    public TrackedPortRefreshDetailReport refreshProviderDetailed(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        Set<FluidType> activeProviders = providerTypes(sendingTanks);
        PortMachineDetachDetailReport detach = detachObsoleteProvidersDetailed(
                level, origin, ports, activeProviders, provider);
        PortMachineRefreshDetailReport refresh = HbmFluidPortMachine.refreshProviderPortsDetailedReport(
                level, origin, ports, sendingTanks, provider);
        providerTypes.clear();
        providerTypes.addAll(activeProviders);
        return new TrackedPortRefreshDetailReport(detach, refresh);
    }

    public TrackedPortRefreshReport refreshTransceiver(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        return refreshTransceiverDetailed(level, origin, ports, receivingTanks, sendingTanks, transceiver).summary();
    }

    public TrackedPortRefreshDetailReport refreshTransceiverDetailed(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        Set<FluidType> activeReceivers = receiverTypes(receivingTanks);
        Set<FluidType> activeProviders = providerTypes(sendingTanks);
        PortMachineDetachDetailReport detach = detachObsoleteReceiversDetailed(
                level, origin, ports, activeReceivers, transceiver)
                .merge(detachObsoleteProvidersDetailed(level, origin, ports, activeProviders, transceiver));
        PortMachineRefreshDetailReport refresh = HbmFluidPortMachine.refreshTransceiverPortsDetailedReport(
                level, origin, ports, receivingTanks, sendingTanks, transceiver);
        receiverTypes.clear();
        receiverTypes.addAll(activeReceivers);
        providerTypes.clear();
        providerTypes.addAll(activeProviders);
        return new TrackedPortRefreshDetailReport(detach, refresh);
    }

    public PortMachineDetachReport detachAll(Level level, BlockPos origin, Iterable<FluidPort> ports,
            HbmFluidReceiver receiver, HbmFluidProvider provider) {
        return detachAllDetailed(level, origin, ports, receiver, provider).summary();
    }

    public PortMachineDetachDetailReport detachAllDetailed(Level level, BlockPos origin, Iterable<FluidPort> ports,
            HbmFluidReceiver receiver, HbmFluidProvider provider) {
        PortMachineDetachDetailReport detach = HbmFluidPortMachine.PortMachineDetachDetailReport.empty();
        if (!receiverTypes.isEmpty() && receiver != null) {
            detach = detach.merge(HbmFluidPortMachine.detachReceiverPortsForTypesDetailedReport(
                    level, origin, ports, receiverTypes, receiver));
        }
        if (!providerTypes.isEmpty() && provider != null) {
            detach = detach.merge(HbmFluidPortMachine.detachProviderPortsForTypesDetailedReport(
                    level, origin, ports, providerTypes, provider));
        }
        receiverTypes.clear();
        providerTypes.clear();
        return detach;
    }

    public Set<FluidType> getTrackedReceiverTypes() {
        return Set.copyOf(receiverTypes);
    }

    public Set<FluidType> getTrackedProviderTypes() {
        return Set.copyOf(providerTypes);
    }

    private PortMachineDetachReport detachObsoleteReceivers(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Set<FluidType> activeTypes, HbmFluidReceiver receiver) {
        return detachObsoleteReceiversDetailed(level, origin, ports, activeTypes, receiver).summary();
    }

    private PortMachineDetachDetailReport detachObsoleteReceiversDetailed(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Set<FluidType> activeTypes, HbmFluidReceiver receiver) {
        Set<FluidType> staleTypes = new HashSet<>(receiverTypes);
        staleTypes.removeAll(activeTypes);
        return staleTypes.isEmpty()
                ? PortMachineDetachDetailReport.empty()
                : HbmFluidPortMachine.detachReceiverPortsForTypesDetailedReport(
                        level, origin, ports, staleTypes, receiver);
    }

    private PortMachineDetachReport detachObsoleteProviders(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Set<FluidType> activeTypes, HbmFluidProvider provider) {
        return detachObsoleteProvidersDetailed(level, origin, ports, activeTypes, provider).summary();
    }

    private PortMachineDetachDetailReport detachObsoleteProvidersDetailed(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Set<FluidType> activeTypes, HbmFluidProvider provider) {
        Set<FluidType> staleTypes = new HashSet<>(providerTypes);
        staleTypes.removeAll(activeTypes);
        return staleTypes.isEmpty()
                ? PortMachineDetachDetailReport.empty()
                : HbmFluidPortMachine.detachProviderPortsForTypesDetailedReport(
                        level, origin, ports, staleTypes, provider);
    }

    private static Set<FluidType> receiverTypes(Iterable<HbmFluidTank> tanks) {
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

    private static Set<FluidType> providerTypes(Iterable<HbmFluidTank> tanks) {
        Set<FluidType> types = new HashSet<>();
        if (tanks != null) {
            for (HbmFluidTank tank : tanks) {
                if (tank != null && tank.getTankType() != HbmFluids.NONE && tank.getFill() > 0) {
                    types.add(tank.getTankType());
                }
            }
        }
        return types;
    }

    public record TrackedPortRefreshReport(
            PortMachineDetachReport detach,
            PortMachineRefreshReport refresh) {
        public static TrackedPortRefreshReport empty() {
            return new TrackedPortRefreshReport(
                    PortMachineDetachReport.empty(),
                    PortMachineRefreshReport.empty());
        }

        public int touchedPorts() {
            return detach.touchedPorts() + refresh.touchedPorts();
        }
    }

    public record TrackedPortRefreshDetailReport(
            PortMachineDetachDetailReport detach,
            PortMachineRefreshDetailReport refresh) {
        public TrackedPortRefreshDetailReport {
            detach = detach == null ? PortMachineDetachDetailReport.empty() : detach;
            refresh = refresh == null ? PortMachineRefreshDetailReport.empty() : refresh;
        }

        public static TrackedPortRefreshDetailReport empty() {
            return new TrackedPortRefreshDetailReport(
                    PortMachineDetachDetailReport.empty(),
                    PortMachineRefreshDetailReport.empty());
        }

        public TrackedPortRefreshReport summary() {
            return new TrackedPortRefreshReport(detach.summary(), refresh.summary());
        }

        public int touchedPorts() {
            return detach.touchedPorts() + refresh.touchedPorts();
        }
    }
}
