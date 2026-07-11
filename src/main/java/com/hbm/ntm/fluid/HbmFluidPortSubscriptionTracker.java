package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.HbmFluidPortMachine.PortMachineDetachReport;
import com.hbm.ntm.fluid.HbmFluidPortMachine.PortMachineDetachDetailReport;
import com.hbm.ntm.fluid.HbmFluidPortMachine.PortMachineRefreshDetailReport;
import com.hbm.ntm.fluid.HbmFluidPortMachine.PortMachineRefreshReport;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.util.HbmMachinePerformanceCounters;
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
    private final Set<ProviderKey> providerKeys = new HashSet<>();
    private final Set<FluidType> scratchReceiverTypes = new HashSet<>();
    private final Set<ProviderKey> scratchProviderKeys = new HashSet<>();
    private final Set<FluidType> scratchProviderTypes = new HashSet<>();
    private final Set<FluidType> scratchStaleTypes = new HashSet<>();

    public void refreshReceiverNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        scratchReceiverTypes.clear();
        receiverTypes(receivingTanks, scratchReceiverTypes);
        detachObsoleteReceiversNoReport(level, origin, ports, scratchReceiverTypes, receiver);
        refreshReceiverPortsForTypesNoReport(level, origin, ports, scratchReceiverTypes, receiver);
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        scratchReceiverTypes.clear();
    }

    public void refreshReceiverNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            HbmFluidTank receivingTank, HbmFluidReceiver receiver) {
        scratchReceiverTypes.clear();
        receiverType(receivingTank, scratchReceiverTypes);
        detachObsoleteReceiversNoReport(level, origin, ports, scratchReceiverTypes, receiver);
        refreshReceiverPortsForTypesNoReport(level, origin, ports, scratchReceiverTypes, receiver);
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        scratchReceiverTypes.clear();
    }

    public void refreshReceiverNoReport(Level level, BlockPos origin, FluidPort port,
            HbmFluidTank receivingTank, HbmFluidReceiver receiver) {
        scratchReceiverTypes.clear();
        receiverType(receivingTank, scratchReceiverTypes);
        detachObsoleteReceiversNoReport(level, origin, port, scratchReceiverTypes, receiver);
        refreshReceiverPortForTypesNoReport(level, origin, port, scratchReceiverTypes, receiver);
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        scratchReceiverTypes.clear();
    }

    public TrackedPortRefreshReport refreshReceiver(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        scratchReceiverTypes.clear();
        receiverTypes(receivingTanks, scratchReceiverTypes);
        PortMachineDetachReport detach = detachObsoleteReceivers(level, origin, ports, scratchReceiverTypes, receiver);
        PortMachineRefreshReport refresh = refreshReceiverPortsForTypes(
                level, origin, ports, scratchReceiverTypes, receiver);
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        scratchReceiverTypes.clear();
        return new TrackedPortRefreshReport(detach, refresh);
    }

    public TrackedPortRefreshReport refreshReceiver(Level level, BlockPos origin, Iterable<FluidPort> ports,
            HbmFluidTank receivingTank, HbmFluidReceiver receiver) {
        scratchReceiverTypes.clear();
        receiverType(receivingTank, scratchReceiverTypes);
        PortMachineDetachReport detach = detachObsoleteReceivers(level, origin, ports, scratchReceiverTypes, receiver);
        PortMachineRefreshReport refresh = refreshReceiverPortsForTypes(
                level, origin, ports, scratchReceiverTypes, receiver);
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        scratchReceiverTypes.clear();
        return new TrackedPortRefreshReport(detach, refresh);
    }

    public TrackedPortRefreshReport refreshReceiver(Level level, BlockPos origin, FluidPort port,
            HbmFluidTank receivingTank, HbmFluidReceiver receiver) {
        scratchReceiverTypes.clear();
        receiverType(receivingTank, scratchReceiverTypes);
        PortMachineDetachReport detach = detachObsoleteReceivers(level, origin, port, scratchReceiverTypes, receiver);
        PortMachineRefreshReport refresh = refreshReceiverPortForTypes(level, origin, port, scratchReceiverTypes, receiver);
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        scratchReceiverTypes.clear();
        return new TrackedPortRefreshReport(detach, refresh);
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
        return new TrackedPortRefreshDetailReport(detach, refresh, activeReceivers, providerKeys);
    }

    public void refreshProviderNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        scratchProviderKeys.clear();
        providerKeys(sendingTanks, scratchProviderKeys);
        scratchProviderTypes.clear();
        providerTypes(scratchProviderKeys, scratchProviderTypes);
        detachObsoleteProvidersNoReport(level, origin, ports, scratchProviderTypes, provider);
        refreshProviderPortsForKeysNoReport(level, origin, ports, scratchProviderKeys, provider);
        providerTypes.clear();
        providerTypes.addAll(scratchProviderTypes);
        providerKeys.clear();
        providerKeys.addAll(scratchProviderKeys);
        scratchProviderKeys.clear();
        scratchProviderTypes.clear();
    }

    public void refreshProviderNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            HbmFluidTank sendingTank, HbmFluidProvider provider) {
        scratchProviderKeys.clear();
        providerKey(sendingTank, scratchProviderKeys);
        scratchProviderTypes.clear();
        providerTypes(scratchProviderKeys, scratchProviderTypes);
        detachObsoleteProvidersNoReport(level, origin, ports, scratchProviderTypes, provider);
        refreshProviderPortsForKeysNoReport(level, origin, ports, scratchProviderKeys, provider);
        providerTypes.clear();
        providerTypes.addAll(scratchProviderTypes);
        providerKeys.clear();
        providerKeys.addAll(scratchProviderKeys);
        scratchProviderKeys.clear();
        scratchProviderTypes.clear();
    }

    public void refreshProviderNoReport(Level level, BlockPos origin, FluidPort port,
            HbmFluidTank sendingTank, HbmFluidProvider provider) {
        scratchProviderKeys.clear();
        providerKey(sendingTank, scratchProviderKeys);
        scratchProviderTypes.clear();
        providerTypes(scratchProviderKeys, scratchProviderTypes);
        detachObsoleteProvidersNoReport(level, origin, port, scratchProviderTypes, provider);
        refreshProviderPortForKeysNoReport(level, origin, port, scratchProviderKeys, provider);
        providerTypes.clear();
        providerTypes.addAll(scratchProviderTypes);
        providerKeys.clear();
        providerKeys.addAll(scratchProviderKeys);
        scratchProviderKeys.clear();
        scratchProviderTypes.clear();
    }

    public TrackedPortRefreshReport refreshProvider(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        scratchProviderKeys.clear();
        providerKeys(sendingTanks, scratchProviderKeys);
        scratchProviderTypes.clear();
        providerTypes(scratchProviderKeys, scratchProviderTypes);
        PortMachineDetachReport detach = detachObsoleteProviders(level, origin, ports, scratchProviderTypes, provider);
        PortMachineRefreshReport refresh = refreshProviderPortsForKeys(
                level, origin, ports, scratchProviderKeys, provider);
        providerTypes.clear();
        providerTypes.addAll(scratchProviderTypes);
        providerKeys.clear();
        providerKeys.addAll(scratchProviderKeys);
        scratchProviderKeys.clear();
        scratchProviderTypes.clear();
        return new TrackedPortRefreshReport(detach, refresh);
    }

    public TrackedPortRefreshReport refreshProvider(Level level, BlockPos origin, Iterable<FluidPort> ports,
            HbmFluidTank sendingTank, HbmFluidProvider provider) {
        scratchProviderKeys.clear();
        providerKey(sendingTank, scratchProviderKeys);
        scratchProviderTypes.clear();
        providerTypes(scratchProviderKeys, scratchProviderTypes);
        PortMachineDetachReport detach = detachObsoleteProviders(level, origin, ports, scratchProviderTypes, provider);
        PortMachineRefreshReport refresh = refreshProviderPortsForKeys(
                level, origin, ports, scratchProviderKeys, provider);
        providerTypes.clear();
        providerTypes.addAll(scratchProviderTypes);
        providerKeys.clear();
        providerKeys.addAll(scratchProviderKeys);
        scratchProviderKeys.clear();
        scratchProviderTypes.clear();
        return new TrackedPortRefreshReport(detach, refresh);
    }

    public TrackedPortRefreshReport refreshProvider(Level level, BlockPos origin, FluidPort port,
            HbmFluidTank sendingTank, HbmFluidProvider provider) {
        scratchProviderKeys.clear();
        providerKey(sendingTank, scratchProviderKeys);
        scratchProviderTypes.clear();
        providerTypes(scratchProviderKeys, scratchProviderTypes);
        PortMachineDetachReport detach = detachObsoleteProviders(level, origin, port, scratchProviderTypes, provider);
        PortMachineRefreshReport refresh = refreshProviderPortForKeys(level, origin, port, scratchProviderKeys, provider);
        providerTypes.clear();
        providerTypes.addAll(scratchProviderTypes);
        providerKeys.clear();
        providerKeys.addAll(scratchProviderKeys);
        scratchProviderKeys.clear();
        scratchProviderTypes.clear();
        return new TrackedPortRefreshReport(detach, refresh);
    }

    public TrackedPortRefreshDetailReport refreshProviderDetailed(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        Set<ProviderKey> activeProviderKeys = providerKeys(sendingTanks);
        Set<FluidType> activeProviders = providerTypes(activeProviderKeys);
        PortMachineDetachDetailReport detach = detachObsoleteProvidersDetailed(
                level, origin, ports, activeProviders, provider);
        PortMachineRefreshDetailReport refresh = HbmFluidPortMachine.refreshProviderPortsDetailedReport(
                level, origin, ports, sendingTanks, provider);
        providerTypes.clear();
        providerTypes.addAll(activeProviders);
        providerKeys.clear();
        providerKeys.addAll(activeProviderKeys);
        return new TrackedPortRefreshDetailReport(detach, refresh, receiverTypes, activeProviderKeys);
    }

    public void refreshTransceiverNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        scratchReceiverTypes.clear();
        receiverTypes(receivingTanks, scratchReceiverTypes);
        scratchProviderKeys.clear();
        providerKeys(sendingTanks, scratchProviderKeys);
        scratchProviderTypes.clear();
        providerTypes(scratchProviderKeys, scratchProviderTypes);
        detachObsoleteReceiversNoReport(level, origin, ports, scratchReceiverTypes, transceiver);
        detachObsoleteProvidersNoReport(level, origin, ports, scratchProviderTypes, transceiver);
        refreshReceiverPortsForTypesNoReport(level, origin, ports, scratchReceiverTypes, transceiver);
        refreshProviderPortsForKeysNoReport(level, origin, ports, scratchProviderKeys, transceiver);
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        providerTypes.clear();
        providerTypes.addAll(scratchProviderTypes);
        providerKeys.clear();
        providerKeys.addAll(scratchProviderKeys);
        scratchReceiverTypes.clear();
        scratchProviderKeys.clear();
        scratchProviderTypes.clear();
    }

    public void refreshTransceiverNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            HbmFluidTank receivingTank, HbmFluidTank sendingTank, HbmStandardFluidTransceiver transceiver) {
        scratchReceiverTypes.clear();
        receiverType(receivingTank, scratchReceiverTypes);
        scratchProviderKeys.clear();
        providerKey(sendingTank, scratchProviderKeys);
        scratchProviderTypes.clear();
        providerTypes(scratchProviderKeys, scratchProviderTypes);
        detachObsoleteReceiversNoReport(level, origin, ports, scratchReceiverTypes, transceiver);
        detachObsoleteProvidersNoReport(level, origin, ports, scratchProviderTypes, transceiver);
        refreshReceiverPortsForTypesNoReport(level, origin, ports, scratchReceiverTypes, transceiver);
        refreshProviderPortsForKeysNoReport(level, origin, ports, scratchProviderKeys, transceiver);
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        providerTypes.clear();
        providerTypes.addAll(scratchProviderTypes);
        providerKeys.clear();
        providerKeys.addAll(scratchProviderKeys);
        scratchReceiverTypes.clear();
        scratchProviderKeys.clear();
        scratchProviderTypes.clear();
    }

    public TrackedPortRefreshReport refreshTransceiver(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        scratchReceiverTypes.clear();
        receiverTypes(receivingTanks, scratchReceiverTypes);
        scratchProviderKeys.clear();
        providerKeys(sendingTanks, scratchProviderKeys);
        scratchProviderTypes.clear();
        providerTypes(scratchProviderKeys, scratchProviderTypes);
        PortMachineDetachReport detach = detachObsoleteReceivers(level, origin, ports, scratchReceiverTypes, transceiver)
                .merge(detachObsoleteProviders(level, origin, ports, scratchProviderTypes, transceiver));
        PortMachineRefreshReport refresh = refreshReceiverPortsForTypes(
                level, origin, ports, scratchReceiverTypes, transceiver)
                .merge(refreshProviderPortsForKeys(level, origin, ports, scratchProviderKeys, transceiver));
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        providerTypes.clear();
        providerTypes.addAll(scratchProviderTypes);
        providerKeys.clear();
        providerKeys.addAll(scratchProviderKeys);
        scratchReceiverTypes.clear();
        scratchProviderKeys.clear();
        scratchProviderTypes.clear();
        return new TrackedPortRefreshReport(detach, refresh);
    }

    public TrackedPortRefreshReport refreshTransceiver(Level level, BlockPos origin, Iterable<FluidPort> ports,
            HbmFluidTank receivingTank, HbmFluidTank sendingTank, HbmStandardFluidTransceiver transceiver) {
        scratchReceiverTypes.clear();
        receiverType(receivingTank, scratchReceiverTypes);
        scratchProviderKeys.clear();
        providerKey(sendingTank, scratchProviderKeys);
        scratchProviderTypes.clear();
        providerTypes(scratchProviderKeys, scratchProviderTypes);
        PortMachineDetachReport detach = detachObsoleteReceivers(level, origin, ports, scratchReceiverTypes, transceiver)
                .merge(detachObsoleteProviders(level, origin, ports, scratchProviderTypes, transceiver));
        PortMachineRefreshReport refresh = refreshReceiverPortsForTypes(
                level, origin, ports, scratchReceiverTypes, transceiver)
                .merge(refreshProviderPortsForKeys(level, origin, ports, scratchProviderKeys, transceiver));
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        providerTypes.clear();
        providerTypes.addAll(scratchProviderTypes);
        providerKeys.clear();
        providerKeys.addAll(scratchProviderKeys);
        scratchReceiverTypes.clear();
        scratchProviderKeys.clear();
        scratchProviderTypes.clear();
        return new TrackedPortRefreshReport(detach, refresh);
    }

    public TrackedPortRefreshDetailReport refreshTransceiverDetailed(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        Set<FluidType> activeReceivers = receiverTypes(receivingTanks);
        Set<ProviderKey> activeProviderKeys = providerKeys(sendingTanks);
        Set<FluidType> activeProviders = providerTypes(activeProviderKeys);
        PortMachineDetachDetailReport detach = detachObsoleteReceiversDetailed(
                level, origin, ports, activeReceivers, transceiver)
                .merge(detachObsoleteProvidersDetailed(level, origin, ports, activeProviders, transceiver));
        PortMachineRefreshDetailReport refresh = HbmFluidPortMachine.refreshTransceiverPortsDetailedReport(
                level, origin, ports, receivingTanks, sendingTanks, transceiver);
        receiverTypes.clear();
        receiverTypes.addAll(activeReceivers);
        providerTypes.clear();
        providerTypes.addAll(activeProviders);
        providerKeys.clear();
        providerKeys.addAll(activeProviderKeys);
        return new TrackedPortRefreshDetailReport(detach, refresh, activeReceivers, activeProviderKeys);
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
        providerKeys.clear();
        return detach;
    }

    public PortMachineDetachReport detachAll(Level level, BlockPos origin, FluidPort port,
            HbmFluidReceiver receiver, HbmFluidProvider provider) {
        PortMachineDetachReport detach = HbmFluidPortMachine.PortMachineDetachReport.empty();
        if (!receiverTypes.isEmpty() && receiver != null) {
            int detachedPorts = detachReceiverPortForTypes(level, origin, port, receiverTypes, receiver);
            detach = detach.merge(new PortMachineDetachReport(receiverTypes.size(), detachedPorts, 0, 0));
        }
        if (!providerTypes.isEmpty() && provider != null) {
            int detachedPorts = detachProviderPortForTypes(level, origin, port, providerTypes, provider);
            detach = detach.merge(new PortMachineDetachReport(0, 0, providerTypes.size(), detachedPorts));
        }
        receiverTypes.clear();
        providerTypes.clear();
        providerKeys.clear();
        return detach;
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
        providerKeys.clear();
        return detach;
    }

    public Set<FluidType> getTrackedReceiverTypes() {
        return Set.copyOf(receiverTypes);
    }

    public Set<FluidType> getTrackedProviderTypes() {
        return Set.copyOf(providerTypes);
    }

    public Set<ProviderKey> getTrackedProviderKeys() {
        return Set.copyOf(providerKeys);
    }

    private void detachObsoleteReceiversNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Set<FluidType> activeTypes, HbmFluidReceiver receiver) {
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(receiverTypes);
        scratchStaleTypes.removeAll(activeTypes);
        if (!scratchStaleTypes.isEmpty()) {
            HbmFluidPortMachine.detachReceiverPortsForTypes(level, origin, ports, scratchStaleTypes, receiver);
        }
        scratchStaleTypes.clear();
    }

    private PortMachineDetachReport detachObsoleteReceivers(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Set<FluidType> activeTypes, HbmFluidReceiver receiver) {
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(receiverTypes);
        scratchStaleTypes.removeAll(activeTypes);
        if (scratchStaleTypes.isEmpty()) {
            return PortMachineDetachReport.empty();
        }
        int staleTypes = scratchStaleTypes.size();
        int detachedPorts = HbmFluidPortMachine.detachReceiverPortsForTypes(
                level, origin, ports, scratchStaleTypes, receiver);
        scratchStaleTypes.clear();
        return new PortMachineDetachReport(staleTypes, detachedPorts, 0, 0);
    }

    private void detachObsoleteReceiversNoReport(Level level, BlockPos origin, FluidPort port,
            Set<FluidType> activeTypes, HbmFluidReceiver receiver) {
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(receiverTypes);
        scratchStaleTypes.removeAll(activeTypes);
        if (!scratchStaleTypes.isEmpty()) {
            detachReceiverPortForTypes(level, origin, port, scratchStaleTypes, receiver);
        }
        scratchStaleTypes.clear();
    }

    private PortMachineDetachReport detachObsoleteReceivers(Level level, BlockPos origin, FluidPort port,
            Set<FluidType> activeTypes, HbmFluidReceiver receiver) {
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(receiverTypes);
        scratchStaleTypes.removeAll(activeTypes);
        if (scratchStaleTypes.isEmpty()) {
            return PortMachineDetachReport.empty();
        }
        int staleTypes = scratchStaleTypes.size();
        int detachedPorts = detachReceiverPortForTypes(level, origin, port, scratchStaleTypes, receiver);
        scratchStaleTypes.clear();
        return new PortMachineDetachReport(staleTypes, detachedPorts, 0, 0);
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

    private void detachObsoleteProvidersNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Set<FluidType> activeTypes, HbmFluidProvider provider) {
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(providerTypes);
        scratchStaleTypes.removeAll(activeTypes);
        if (!scratchStaleTypes.isEmpty()) {
            HbmFluidPortMachine.detachProviderPortsForTypes(level, origin, ports, scratchStaleTypes, provider);
        }
        scratchStaleTypes.clear();
    }

    private PortMachineDetachReport detachObsoleteProviders(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Set<FluidType> activeTypes, HbmFluidProvider provider) {
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(providerTypes);
        scratchStaleTypes.removeAll(activeTypes);
        if (scratchStaleTypes.isEmpty()) {
            return PortMachineDetachReport.empty();
        }
        int staleTypes = scratchStaleTypes.size();
        int detachedPorts = HbmFluidPortMachine.detachProviderPortsForTypes(
                level, origin, ports, scratchStaleTypes, provider);
        scratchStaleTypes.clear();
        return new PortMachineDetachReport(0, 0, staleTypes, detachedPorts);
    }

    private void detachObsoleteProvidersNoReport(Level level, BlockPos origin, FluidPort port,
            Set<FluidType> activeTypes, HbmFluidProvider provider) {
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(providerTypes);
        scratchStaleTypes.removeAll(activeTypes);
        if (!scratchStaleTypes.isEmpty()) {
            detachProviderPortForTypes(level, origin, port, scratchStaleTypes, provider);
        }
        scratchStaleTypes.clear();
    }

    private PortMachineDetachReport detachObsoleteProviders(Level level, BlockPos origin, FluidPort port,
            Set<FluidType> activeTypes, HbmFluidProvider provider) {
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(providerTypes);
        scratchStaleTypes.removeAll(activeTypes);
        if (scratchStaleTypes.isEmpty()) {
            return PortMachineDetachReport.empty();
        }
        int staleTypes = scratchStaleTypes.size();
        int detachedPorts = detachProviderPortForTypes(level, origin, port, scratchStaleTypes, provider);
        scratchStaleTypes.clear();
        return new PortMachineDetachReport(0, 0, staleTypes, detachedPorts);
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
        receiverTypes(tanks, types);
        return types;
    }

    private static void receiverTypes(Iterable<HbmFluidTank> tanks, Set<FluidType> types) {
        if (tanks != null) {
            for (HbmFluidTank tank : tanks) {
                receiverType(tank, types);
            }
        }
    }

    private static void receiverType(HbmFluidTank tank, Set<FluidType> types) {
        if (tank != null && tank.getTankType() != HbmFluids.NONE) {
            types.add(tank.getTankType());
        }
    }

    private static Set<ProviderKey> providerKeys(Iterable<HbmFluidTank> tanks) {
        Set<ProviderKey> keys = new HashSet<>();
        providerKeys(tanks, keys);
        return keys;
    }

    private static void providerKeys(Iterable<HbmFluidTank> tanks, Set<ProviderKey> keys) {
        if (tanks != null) {
            for (HbmFluidTank tank : tanks) {
                providerKey(tank, keys);
            }
        }
    }

    private static void providerKey(HbmFluidTank tank, Set<ProviderKey> keys) {
        if (tank != null && tank.getTankType() != HbmFluids.NONE && tank.getFill() > 0) {
            keys.add(new ProviderKey(tank.getTankType(), tank.getPressure()));
        }
    }

    private static Set<FluidType> providerTypes(Iterable<ProviderKey> keys) {
        Set<FluidType> types = new HashSet<>();
        providerTypes(keys, types);
        return types;
    }

    private static void providerTypes(Iterable<ProviderKey> keys, Set<FluidType> types) {
        if (keys != null) {
            for (ProviderKey key : keys) {
                if (key != null && key.type() != HbmFluids.NONE) {
                    types.add(key.type());
                }
            }
        }
    }

    private static void refreshReceiverPortsForTypesNoReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<FluidType> activeTypes, HbmFluidReceiver receiver) {
        if (level == null || level.isClientSide || activeTypes == null || receiver == null) {
            return;
        }
        for (FluidType type : activeTypes) {
            if (type != null && type != HbmFluids.NONE) {
                HbmFluidUtil.subscribeReceiverToPorts(level, origin, ports, type, receiver);
            }
        }
    }

    private static PortMachineRefreshReport refreshReceiverPortsForTypes(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<FluidType> activeTypes, HbmFluidReceiver receiver) {
        if (level == null || level.isClientSide || activeTypes == null || receiver == null) {
            return PortMachineRefreshReport.empty();
        }
        int typeCount = 0;
        int touched = 0;
        for (FluidType type : activeTypes) {
            if (type != null && type != HbmFluids.NONE) {
                typeCount++;
                touched += HbmFluidUtil.subscribeReceiverToPorts(level, origin, ports, type, receiver);
            }
        }
        return new PortMachineRefreshReport(typeCount, touched, 0, 0, 0L);
    }

    private static void refreshReceiverPortForTypesNoReport(Level level, BlockPos origin,
            FluidPort port, Iterable<FluidType> activeTypes, HbmFluidReceiver receiver) {
        if (level == null || level.isClientSide || port == null || activeTypes == null || receiver == null) {
            return;
        }
        for (FluidType type : activeTypes) {
            if (type != null && type != HbmFluids.NONE) {
                HbmMachinePerformanceCounters.fluidPortCheck();
                boolean added = HbmFluidUtil.subscribeReceiverToPort(level, origin, port, type, receiver);
                HbmMachinePerformanceCounters.fluidPortSubscription(added);
            }
        }
    }

    private static PortMachineRefreshReport refreshReceiverPortForTypes(Level level, BlockPos origin,
            FluidPort port, Iterable<FluidType> activeTypes, HbmFluidReceiver receiver) {
        if (level == null || level.isClientSide || port == null || activeTypes == null || receiver == null) {
            return PortMachineRefreshReport.empty();
        }
        int typeCount = 0;
        int touched = 0;
        for (FluidType type : activeTypes) {
            if (type != null && type != HbmFluids.NONE) {
                typeCount++;
                HbmMachinePerformanceCounters.fluidPortCheck();
                boolean added = HbmFluidUtil.subscribeReceiverToPort(level, origin, port, type, receiver);
                HbmMachinePerformanceCounters.fluidPortSubscription(added);
                if (added) {
                    touched++;
                }
            }
        }
        return new PortMachineRefreshReport(typeCount, touched, 0, 0, 0L);
    }

    private static void refreshProviderPortsForKeysNoReport(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<ProviderKey> activeKeys, HbmFluidProvider provider) {
        if (level == null || level.isClientSide || activeKeys == null || provider == null) {
            return;
        }
        for (ProviderKey key : activeKeys) {
            if (key != null && key.type() != HbmFluids.NONE) {
                HbmFluidUtil.tryProvideToPorts(level, origin, ports, key.type(), key.pressure(), provider);
            }
        }
    }

    private static PortMachineRefreshReport refreshProviderPortsForKeys(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<ProviderKey> activeKeys, HbmFluidProvider provider) {
        if (level == null || level.isClientSide || activeKeys == null || provider == null) {
            return PortMachineRefreshReport.empty();
        }
        int keyCount = 0;
        int touched = 0;
        for (ProviderKey key : activeKeys) {
            if (key != null && key.type() != HbmFluids.NONE) {
                keyCount++;
                touched += HbmFluidUtil.tryProvideToPorts(level, origin, ports, key.type(), key.pressure(), provider);
            }
        }
        return new PortMachineRefreshReport(0, 0, keyCount, touched, 0L);
    }

    private static void refreshProviderPortForKeysNoReport(Level level, BlockPos origin,
            FluidPort port, Iterable<ProviderKey> activeKeys, HbmFluidProvider provider) {
        if (level == null || level.isClientSide || port == null || activeKeys == null || provider == null) {
            return;
        }
        for (ProviderKey key : activeKeys) {
            if (key != null && key.type() != HbmFluids.NONE) {
                HbmFluidUtil.tryProvideToPort(level, origin, port, key.type(), key.pressure(), provider);
            }
        }
    }

    private static PortMachineRefreshReport refreshProviderPortForKeys(Level level, BlockPos origin,
            FluidPort port, Iterable<ProviderKey> activeKeys, HbmFluidProvider provider) {
        if (level == null || level.isClientSide || port == null || activeKeys == null || provider == null) {
            return PortMachineRefreshReport.empty();
        }
        int keyCount = 0;
        int touched = 0;
        for (ProviderKey key : activeKeys) {
            if (key != null && key.type() != HbmFluids.NONE) {
                keyCount++;
                if (HbmFluidUtil.tryProvideToPort(level, origin, port, key.type(), key.pressure(), provider)) {
                    touched++;
                }
            }
        }
        return new PortMachineRefreshReport(0, 0, keyCount, touched, 0L);
    }

    private static int detachReceiverPortForTypes(Level level, BlockPos origin,
            FluidPort port, Iterable<FluidType> types, HbmFluidReceiver receiver) {
        if (level == null || level.isClientSide || port == null || types == null || receiver == null) {
            return 0;
        }
        int receiverPorts = 0;
        for (FluidType type : types) {
            if (type != null && type != HbmFluids.NONE) {
                HbmMachinePerformanceCounters.fluidPortCheck();
                if (HbmFluidUtil.unsubscribeReceiverFromPort(level, origin, port, type, receiver)) {
                    receiverPorts++;
                }
            }
        }
        return receiverPorts;
    }

    private static int detachProviderPortForTypes(Level level, BlockPos origin,
            FluidPort port, Iterable<FluidType> types, HbmFluidProvider provider) {
        if (level == null || level.isClientSide || port == null || types == null || provider == null) {
            return 0;
        }
        int providerPorts = 0;
        for (FluidType type : types) {
            if (type != null && type != HbmFluids.NONE) {
                HbmMachinePerformanceCounters.fluidPortCheck();
                if (HbmFluidUtil.unsubscribeProviderFromPort(level, origin, port, type, provider)) {
                    providerPorts++;
                }
            }
        }
        return providerPorts;
    }

    public record ProviderKey(FluidType type, int pressure) {
        public ProviderKey {
            type = type == null ? HbmFluids.NONE : type;
            pressure = HbmFluidTank.clampPressure(pressure);
        }
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
            PortMachineRefreshDetailReport refresh,
            Set<FluidType> receiverTypes,
            Set<ProviderKey> providerKeys) {
        public TrackedPortRefreshDetailReport {
            detach = detach == null ? PortMachineDetachDetailReport.empty() : detach;
            refresh = refresh == null ? PortMachineRefreshDetailReport.empty() : refresh;
            receiverTypes = receiverTypes == null ? Set.of() : Set.copyOf(receiverTypes);
            providerKeys = providerKeys == null ? Set.of() : Set.copyOf(providerKeys);
        }

        public static TrackedPortRefreshDetailReport empty() {
            return new TrackedPortRefreshDetailReport(
                    PortMachineDetachDetailReport.empty(),
                    PortMachineRefreshDetailReport.empty(),
                    Set.of(),
                    Set.of());
        }

        public TrackedPortRefreshReport summary() {
            return new TrackedPortRefreshReport(detach.summary(), refresh.summary());
        }

        public int touchedPorts() {
            return detach.touchedPorts() + refresh.touchedPorts();
        }
    }
}
