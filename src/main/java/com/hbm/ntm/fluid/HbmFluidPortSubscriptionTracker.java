package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.HbmFluidPortMachine.PortMachineDetachReport;
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
        Set<FluidType> activeReceivers = receiverTypes(receivingTanks);
        PortMachineDetachReport detach = detachObsoleteReceivers(
                level, origin, ports, activeReceivers, receiver);
        PortMachineRefreshReport refresh = HbmFluidPortMachine.refreshReceiverPortsReport(
                level, origin, ports, receivingTanks, receiver);
        receiverTypes.clear();
        receiverTypes.addAll(activeReceivers);
        return new TrackedPortRefreshReport(detach, refresh);
    }

    public TrackedPortRefreshReport refreshProvider(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        Set<FluidType> activeProviders = providerTypes(sendingTanks);
        PortMachineDetachReport detach = detachObsoleteProviders(
                level, origin, ports, activeProviders, provider);
        PortMachineRefreshReport refresh = HbmFluidPortMachine.refreshProviderPortsReport(
                level, origin, ports, sendingTanks, provider);
        providerTypes.clear();
        providerTypes.addAll(activeProviders);
        return new TrackedPortRefreshReport(detach, refresh);
    }

    public TrackedPortRefreshReport refreshTransceiver(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        Set<FluidType> activeReceivers = receiverTypes(receivingTanks);
        Set<FluidType> activeProviders = providerTypes(sendingTanks);
        PortMachineDetachReport detach = detachObsoleteReceivers(level, origin, ports, activeReceivers, transceiver)
                .merge(detachObsoleteProviders(level, origin, ports, activeProviders, transceiver));
        PortMachineRefreshReport refresh = HbmFluidPortMachine.refreshTransceiverPortsReport(
                level, origin, ports, receivingTanks, sendingTanks, transceiver);
        receiverTypes.clear();
        receiverTypes.addAll(activeReceivers);
        providerTypes.clear();
        providerTypes.addAll(activeProviders);
        return new TrackedPortRefreshReport(detach, refresh);
    }

    public PortMachineDetachReport detachAll(Level level, BlockPos origin, Iterable<FluidPort> ports,
            HbmFluidReceiver receiver, HbmFluidProvider provider) {
        PortMachineDetachReport detach = HbmFluidPortMachine.PortMachineDetachReport.empty();
        if (!receiverTypes.isEmpty() && receiver != null) {
            detach = detach.merge(HbmFluidPortMachine.detachReceiverPortsForTypesReport(
                    level, origin, ports, receiverTypes, receiver));
        }
        if (!providerTypes.isEmpty() && provider != null) {
            detach = detach.merge(HbmFluidPortMachine.detachProviderPortsForTypesReport(
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
        Set<FluidType> staleTypes = new HashSet<>(receiverTypes);
        staleTypes.removeAll(activeTypes);
        return staleTypes.isEmpty()
                ? PortMachineDetachReport.empty()
                : HbmFluidPortMachine.detachReceiverPortsForTypesReport(level, origin, ports, staleTypes, receiver);
    }

    private PortMachineDetachReport detachObsoleteProviders(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Set<FluidType> activeTypes, HbmFluidProvider provider) {
        Set<FluidType> staleTypes = new HashSet<>(providerTypes);
        staleTypes.removeAll(activeTypes);
        return staleTypes.isEmpty()
                ? PortMachineDetachReport.empty()
                : HbmFluidPortMachine.detachProviderPortsForTypesReport(level, origin, ports, staleTypes, provider);
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
}
