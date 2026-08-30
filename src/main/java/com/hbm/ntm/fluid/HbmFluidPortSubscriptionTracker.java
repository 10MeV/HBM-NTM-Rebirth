package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.HbmFluidPortMachine.PortMachineDetachReport;
import com.hbm.ntm.fluid.HbmFluidPortMachine.PortMachineDetachDetailReport;
import com.hbm.ntm.fluid.HbmFluidPortMachine.PortMachineRefreshDetailReport;
import com.hbm.ntm.fluid.HbmFluidPortMachine.PortMachineRefreshReport;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.util.HbmMachinePerformanceCounters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Tracks the remote-port fluid types and exact immutable ports a machine
 * advertised last tick, then detaches obsolete subscriptions before
 * refreshing the current 1.7.10-style provider/receiver loops.
 */
public final class HbmFluidPortSubscriptionTracker {
    private final Set<FluidType> receiverTypes = new HashSet<>();
    private final Set<FluidType> providerTypes = new HashSet<>();
    private final Set<ProviderKey> providerKeys = new HashSet<>();
    private final List<FluidPort> receiverPorts = new ArrayList<>();
    private final List<FluidPort> providerPorts = new ArrayList<>();
    private final Set<FluidType> scratchReceiverTypes = new HashSet<>();
    private final Set<ProviderKey> scratchProviderKeys = new HashSet<>();
    private final Set<FluidType> scratchProviderTypes = new HashSet<>();
    private final Set<FluidType> scratchStaleTypes = new HashSet<>();
    private final List<FluidPort> scratchReceiverPorts = new ArrayList<>();
    private final List<FluidPort> scratchProviderPorts = new ArrayList<>();
    private final List<FluidPort> scratchRemovedPorts = new ArrayList<>();

    public void refreshReceiverNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        copyPorts(ports, scratchReceiverPorts);
        scratchReceiverTypes.clear();
        receiverTypes(receivingTanks, scratchReceiverTypes);
        detachObsoleteReceiversNoReport(level, origin, scratchReceiverPorts, scratchReceiverTypes, receiver);
        refreshReceiverPortsForTypesNoReport(level, origin, scratchReceiverPorts, scratchReceiverTypes, receiver);
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        replacePorts(receiverPorts, scratchReceiverPorts);
        scratchReceiverTypes.clear();
        scratchReceiverPorts.clear();
    }

    public void refreshReceiverNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            HbmFluidTank receivingTank, HbmFluidReceiver receiver) {
        copyPorts(ports, scratchReceiverPorts);
        scratchReceiverTypes.clear();
        receiverType(receivingTank, scratchReceiverTypes);
        detachObsoleteReceiversNoReport(level, origin, scratchReceiverPorts, scratchReceiverTypes, receiver);
        refreshReceiverPortsForTypesNoReport(level, origin, scratchReceiverPorts, scratchReceiverTypes, receiver);
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        replacePorts(receiverPorts, scratchReceiverPorts);
        scratchReceiverTypes.clear();
        scratchReceiverPorts.clear();
    }

    public void refreshReceiverNoReport(Level level, BlockPos origin, FluidPort port,
            HbmFluidTank receivingTank, HbmFluidReceiver receiver) {
        copyPort(port, scratchReceiverPorts);
        scratchReceiverTypes.clear();
        receiverType(receivingTank, scratchReceiverTypes);
        detachObsoleteReceiversNoReport(level, origin, scratchReceiverPorts, scratchReceiverTypes, receiver);
        refreshReceiverPortsForTypesNoReport(level, origin, scratchReceiverPorts, scratchReceiverTypes, receiver);
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        replacePorts(receiverPorts, scratchReceiverPorts);
        scratchReceiverTypes.clear();
        scratchReceiverPorts.clear();
    }

    public TrackedPortRefreshReport refreshReceiver(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        copyPorts(ports, scratchReceiverPorts);
        scratchReceiverTypes.clear();
        receiverTypes(receivingTanks, scratchReceiverTypes);
        PortMachineDetachReport detach = detachObsoleteReceivers(
                level, origin, scratchReceiverPorts, scratchReceiverTypes, receiver);
        PortMachineRefreshReport refresh = refreshReceiverPortsForTypes(
                level, origin, scratchReceiverPorts, scratchReceiverTypes, receiver);
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        replacePorts(receiverPorts, scratchReceiverPorts);
        scratchReceiverTypes.clear();
        scratchReceiverPorts.clear();
        return new TrackedPortRefreshReport(detach, refresh);
    }

    public TrackedPortRefreshReport refreshReceiver(Level level, BlockPos origin, Iterable<FluidPort> ports,
            HbmFluidTank receivingTank, HbmFluidReceiver receiver) {
        copyPorts(ports, scratchReceiverPorts);
        scratchReceiverTypes.clear();
        receiverType(receivingTank, scratchReceiverTypes);
        PortMachineDetachReport detach = detachObsoleteReceivers(
                level, origin, scratchReceiverPorts, scratchReceiverTypes, receiver);
        PortMachineRefreshReport refresh = refreshReceiverPortsForTypes(
                level, origin, scratchReceiverPorts, scratchReceiverTypes, receiver);
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        replacePorts(receiverPorts, scratchReceiverPorts);
        scratchReceiverTypes.clear();
        scratchReceiverPorts.clear();
        return new TrackedPortRefreshReport(detach, refresh);
    }

    public TrackedPortRefreshReport refreshReceiver(Level level, BlockPos origin, FluidPort port,
            HbmFluidTank receivingTank, HbmFluidReceiver receiver) {
        copyPort(port, scratchReceiverPorts);
        scratchReceiverTypes.clear();
        receiverType(receivingTank, scratchReceiverTypes);
        PortMachineDetachReport detach = detachObsoleteReceivers(
                level, origin, scratchReceiverPorts, scratchReceiverTypes, receiver);
        PortMachineRefreshReport refresh = refreshReceiverPortsForTypes(
                level, origin, scratchReceiverPorts, scratchReceiverTypes, receiver);
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        replacePorts(receiverPorts, scratchReceiverPorts);
        scratchReceiverTypes.clear();
        scratchReceiverPorts.clear();
        return new TrackedPortRefreshReport(detach, refresh);
    }

    public TrackedPortRefreshDetailReport refreshReceiverDetailed(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        copyPorts(ports, scratchReceiverPorts);
        Set<FluidType> activeReceivers = receiverTypes(receivingTanks);
        PortMachineDetachDetailReport detach = detachObsoleteReceiversDetailed(
                level, origin, scratchReceiverPorts, activeReceivers, receiver);
        PortMachineRefreshDetailReport refresh = HbmFluidPortMachine.refreshReceiverPortsDetailedReport(
                level, origin, scratchReceiverPorts, receivingTanks, receiver);
        receiverTypes.clear();
        receiverTypes.addAll(activeReceivers);
        replacePorts(receiverPorts, scratchReceiverPorts);
        scratchReceiverPorts.clear();
        return new TrackedPortRefreshDetailReport(detach, refresh, activeReceivers, providerKeys);
    }

    public void refreshProviderNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        copyPorts(ports, scratchProviderPorts);
        scratchProviderKeys.clear();
        providerKeys(sendingTanks, scratchProviderKeys);
        refreshProviderNoReportPrepared(level, origin, provider);
    }

    public void refreshProviderNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            HbmFluidTank sendingTank, HbmFluidProvider provider) {
        copyPorts(ports, scratchProviderPorts);
        scratchProviderKeys.clear();
        providerKey(sendingTank, scratchProviderKeys);
        refreshProviderNoReportPrepared(level, origin, provider);
    }

    public void refreshProviderNoReport(Level level, BlockPos origin, FluidPort port,
            HbmFluidTank sendingTank, HbmFluidProvider provider) {
        copyPort(port, scratchProviderPorts);
        scratchProviderKeys.clear();
        providerKey(sendingTank, scratchProviderKeys);
        refreshProviderNoReportPrepared(level, origin, provider);
    }

    private void refreshProviderNoReportPrepared(Level level, BlockPos origin, HbmFluidProvider provider) {
        scratchProviderTypes.clear();
        providerTypes(scratchProviderKeys, scratchProviderTypes);
        detachObsoleteProvidersNoReport(level, origin, scratchProviderPorts, scratchProviderTypes, provider);
        refreshProviderPortsForKeysNoReport(level, origin, scratchProviderPorts, scratchProviderKeys, provider);
        providerTypes.clear();
        providerTypes.addAll(scratchProviderTypes);
        providerKeys.clear();
        providerKeys.addAll(scratchProviderKeys);
        replacePorts(providerPorts, scratchProviderPorts);
        scratchProviderKeys.clear();
        scratchProviderTypes.clear();
        scratchProviderPorts.clear();
    }

    public TrackedPortRefreshReport refreshProvider(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        copyPorts(ports, scratchProviderPorts);
        scratchProviderKeys.clear();
        providerKeys(sendingTanks, scratchProviderKeys);
        return refreshProviderPrepared(level, origin, provider);
    }

    public TrackedPortRefreshReport refreshProvider(Level level, BlockPos origin, Iterable<FluidPort> ports,
            HbmFluidTank sendingTank, HbmFluidProvider provider) {
        copyPorts(ports, scratchProviderPorts);
        scratchProviderKeys.clear();
        providerKey(sendingTank, scratchProviderKeys);
        return refreshProviderPrepared(level, origin, provider);
    }

    public TrackedPortRefreshReport refreshProvider(Level level, BlockPos origin, FluidPort port,
            HbmFluidTank sendingTank, HbmFluidProvider provider) {
        copyPort(port, scratchProviderPorts);
        scratchProviderKeys.clear();
        providerKey(sendingTank, scratchProviderKeys);
        return refreshProviderPrepared(level, origin, provider);
    }

    private TrackedPortRefreshReport refreshProviderPrepared(Level level, BlockPos origin,
            HbmFluidProvider provider) {
        scratchProviderTypes.clear();
        providerTypes(scratchProviderKeys, scratchProviderTypes);
        PortMachineDetachReport detach = detachObsoleteProviders(
                level, origin, scratchProviderPorts, scratchProviderTypes, provider);
        PortMachineRefreshReport refresh = refreshProviderPortsForKeys(
                level, origin, scratchProviderPorts, scratchProviderKeys, provider);
        providerTypes.clear();
        providerTypes.addAll(scratchProviderTypes);
        providerKeys.clear();
        providerKeys.addAll(scratchProviderKeys);
        replacePorts(providerPorts, scratchProviderPorts);
        scratchProviderKeys.clear();
        scratchProviderTypes.clear();
        scratchProviderPorts.clear();
        return new TrackedPortRefreshReport(detach, refresh);
    }

    /**
     * Refreshes a legacy sender whose network presence is controlled by its
     * mode, rather than by the amount currently buffered.  Most modern
     * machines deliberately avoid advertising an empty output tank, but
     * {@code IFluidStandardSenderMK2#tryProvide} registered its provider
     * before checking available fluid.  Keep this narrow entry point for
     * source-backed callers with that contract.
     */
    public TrackedPortRefreshReport refreshProviderIncludingEmpty(Level level, BlockPos origin, FluidPort port,
            HbmFluidTank sendingTank, HbmFluidProvider provider) {
        copyPort(port, scratchProviderPorts);
        scratchProviderKeys.clear();
        providerKeyIncludingEmpty(sendingTank, scratchProviderKeys);
        return refreshProviderPrepared(level, origin, provider);
    }

    public TrackedPortRefreshDetailReport refreshProviderDetailed(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        copyPorts(ports, scratchProviderPorts);
        Set<ProviderKey> activeProviderKeys = providerKeys(sendingTanks);
        Set<FluidType> activeProviders = providerTypes(activeProviderKeys);
        PortMachineDetachDetailReport detach = detachObsoleteProvidersDetailed(
                level, origin, scratchProviderPorts, activeProviders, provider);
        PortMachineRefreshDetailReport refresh = HbmFluidPortMachine.refreshProviderPortsDetailedReport(
                level, origin, scratchProviderPorts, sendingTanks, provider);
        providerTypes.clear();
        providerTypes.addAll(activeProviders);
        providerKeys.clear();
        providerKeys.addAll(activeProviderKeys);
        replacePorts(providerPorts, scratchProviderPorts);
        scratchProviderPorts.clear();
        return new TrackedPortRefreshDetailReport(detach, refresh, receiverTypes, activeProviderKeys);
    }

    public void refreshTransceiverNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        copyTransceiverPorts(ports);
        scratchReceiverTypes.clear();
        receiverTypes(receivingTanks, scratchReceiverTypes);
        scratchProviderKeys.clear();
        providerKeys(sendingTanks, scratchProviderKeys);
        refreshTransceiverNoReportPrepared(level, origin, transceiver);
    }

    public void refreshTransceiverNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            HbmFluidTank receivingTank, HbmFluidTank sendingTank, HbmStandardFluidTransceiver transceiver) {
        copyTransceiverPorts(ports);
        scratchReceiverTypes.clear();
        receiverType(receivingTank, scratchReceiverTypes);
        scratchProviderKeys.clear();
        providerKey(sendingTank, scratchProviderKeys);
        refreshTransceiverNoReportPrepared(level, origin, transceiver);
    }

    private void refreshTransceiverNoReportPrepared(Level level, BlockPos origin,
            HbmStandardFluidTransceiver transceiver) {
        scratchProviderTypes.clear();
        providerTypes(scratchProviderKeys, scratchProviderTypes);
        detachObsoleteReceiversNoReport(
                level, origin, scratchReceiverPorts, scratchReceiverTypes, transceiver);
        detachObsoleteProvidersNoReport(
                level, origin, scratchProviderPorts, scratchProviderTypes, transceiver);
        refreshReceiverPortsForTypesNoReport(
                level, origin, scratchReceiverPorts, scratchReceiverTypes, transceiver);
        refreshProviderPortsForKeysNoReport(
                level, origin, scratchProviderPorts, scratchProviderKeys, transceiver);
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        providerTypes.clear();
        providerTypes.addAll(scratchProviderTypes);
        providerKeys.clear();
        providerKeys.addAll(scratchProviderKeys);
        replacePorts(receiverPorts, scratchReceiverPorts);
        replacePorts(providerPorts, scratchProviderPorts);
        scratchReceiverTypes.clear();
        scratchProviderKeys.clear();
        scratchProviderTypes.clear();
        scratchReceiverPorts.clear();
        scratchProviderPorts.clear();
    }

    public TrackedPortRefreshReport refreshTransceiver(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        copyTransceiverPorts(ports);
        scratchReceiverTypes.clear();
        receiverTypes(receivingTanks, scratchReceiverTypes);
        scratchProviderKeys.clear();
        providerKeys(sendingTanks, scratchProviderKeys);
        return refreshTransceiverPrepared(level, origin, transceiver);
    }

    public TrackedPortRefreshReport refreshTransceiver(Level level, BlockPos origin, Iterable<FluidPort> ports,
            HbmFluidTank receivingTank, HbmFluidTank sendingTank, HbmStandardFluidTransceiver transceiver) {
        copyTransceiverPorts(ports);
        scratchReceiverTypes.clear();
        receiverType(receivingTank, scratchReceiverTypes);
        scratchProviderKeys.clear();
        providerKey(sendingTank, scratchProviderKeys);
        return refreshTransceiverPrepared(level, origin, transceiver);
    }

    private TrackedPortRefreshReport refreshTransceiverPrepared(Level level, BlockPos origin,
            HbmStandardFluidTransceiver transceiver) {
        scratchProviderTypes.clear();
        providerTypes(scratchProviderKeys, scratchProviderTypes);
        PortMachineDetachReport detach = detachObsoleteReceivers(
                level, origin, scratchReceiverPorts, scratchReceiverTypes, transceiver)
                .merge(detachObsoleteProviders(
                        level, origin, scratchProviderPorts, scratchProviderTypes, transceiver));
        PortMachineRefreshReport refresh = refreshReceiverPortsForTypes(
                level, origin, scratchReceiverPorts, scratchReceiverTypes, transceiver)
                .merge(refreshProviderPortsForKeys(
                        level, origin, scratchProviderPorts, scratchProviderKeys, transceiver));
        receiverTypes.clear();
        receiverTypes.addAll(scratchReceiverTypes);
        providerTypes.clear();
        providerTypes.addAll(scratchProviderTypes);
        providerKeys.clear();
        providerKeys.addAll(scratchProviderKeys);
        replacePorts(receiverPorts, scratchReceiverPorts);
        replacePorts(providerPorts, scratchProviderPorts);
        scratchReceiverTypes.clear();
        scratchProviderKeys.clear();
        scratchProviderTypes.clear();
        scratchReceiverPorts.clear();
        scratchProviderPorts.clear();
        return new TrackedPortRefreshReport(detach, refresh);
    }

    public TrackedPortRefreshDetailReport refreshTransceiverDetailed(Level level, BlockPos origin,
            Iterable<FluidPort> ports, Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        copyTransceiverPorts(ports);
        Set<FluidType> activeReceivers = receiverTypes(receivingTanks);
        Set<ProviderKey> activeProviderKeys = providerKeys(sendingTanks);
        Set<FluidType> activeProviders = providerTypes(activeProviderKeys);
        PortMachineDetachDetailReport detach = detachObsoleteReceiversDetailed(
                level, origin, scratchReceiverPorts, activeReceivers, transceiver)
                .merge(detachObsoleteProvidersDetailed(
                        level, origin, scratchProviderPorts, activeProviders, transceiver));
        PortMachineRefreshDetailReport refresh = HbmFluidPortMachine.refreshTransceiverPortsDetailedReport(
                level, origin, scratchReceiverPorts, receivingTanks, sendingTanks, transceiver);
        receiverTypes.clear();
        receiverTypes.addAll(activeReceivers);
        providerTypes.clear();
        providerTypes.addAll(activeProviders);
        providerKeys.clear();
        providerKeys.addAll(activeProviderKeys);
        replacePorts(receiverPorts, scratchReceiverPorts);
        replacePorts(providerPorts, scratchProviderPorts);
        scratchReceiverPorts.clear();
        scratchProviderPorts.clear();
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
        clearTracking();
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
        clearTracking();
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
        clearTracking();
        return detach;
    }

    /**
     * Detaches only subscriptions recorded by successful refresh calls.  This
     * lifecycle-safe path deliberately has no port/topology argument: unload
     * handlers must never invoke a machine's dynamic {@code getFluidPorts()}
     * while its chunk is being removed.
     */
    public PortMachineDetachReport detachAllTracked(Level level, BlockPos origin,
            HbmFluidReceiver receiver, HbmFluidProvider provider) {
        PortMachineDetachReport detach = PortMachineDetachReport.empty();
        if (!receiverTypes.isEmpty() && !receiverPorts.isEmpty() && receiver != null) {
            detach = detach.merge(HbmFluidPortMachine.detachReceiverPortsForTypesReport(
                    level, origin, receiverPorts, receiverTypes, receiver));
        }
        if (!providerTypes.isEmpty() && !providerPorts.isEmpty() && provider != null) {
            detach = detach.merge(HbmFluidPortMachine.detachProviderPortsForTypesReport(
                    level, origin, providerPorts, providerTypes, provider));
        }
        clearTracking();
        return detach;
    }

    public PortMachineDetachDetailReport detachAllTrackedDetailed(Level level, BlockPos origin,
            HbmFluidReceiver receiver, HbmFluidProvider provider) {
        PortMachineDetachDetailReport detach = PortMachineDetachDetailReport.empty();
        if (!receiverTypes.isEmpty() && !receiverPorts.isEmpty() && receiver != null) {
            detach = detach.merge(HbmFluidPortMachine.detachReceiverPortsForTypesDetailedReport(
                    level, origin, receiverPorts, receiverTypes, receiver));
        }
        if (!providerTypes.isEmpty() && !providerPorts.isEmpty() && provider != null) {
            detach = detach.merge(HbmFluidPortMachine.detachProviderPortsForTypesDetailedReport(
                    level, origin, providerPorts, providerTypes, provider));
        }
        clearTracking();
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

    public List<FluidPort> getTrackedReceiverPorts() {
        return List.copyOf(receiverPorts);
    }

    public List<FluidPort> getTrackedProviderPorts() {
        return List.copyOf(providerPorts);
    }

    private void detachObsoleteReceiversNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Set<FluidType> activeTypes, HbmFluidReceiver receiver) {
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(receiverTypes);
        scratchStaleTypes.removeAll(activeTypes);
        if (!scratchStaleTypes.isEmpty()) {
            HbmFluidPortMachine.detachReceiverPortsForTypes(
                    level, origin, receiverPorts, scratchStaleTypes, receiver);
        }
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(receiverTypes);
        scratchStaleTypes.retainAll(activeTypes);
        copyPorts(receiverPorts, scratchRemovedPorts);
        removePorts(scratchRemovedPorts, ports);
        if (!scratchRemovedPorts.isEmpty() && !scratchStaleTypes.isEmpty()) {
            HbmFluidPortMachine.detachReceiverPortsForTypes(
                    level, origin, scratchRemovedPorts, scratchStaleTypes, receiver);
        }
        scratchStaleTypes.clear();
        scratchRemovedPorts.clear();
    }

    private PortMachineDetachReport detachObsoleteReceivers(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Set<FluidType> activeTypes, HbmFluidReceiver receiver) {
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(receiverTypes);
        scratchStaleTypes.removeAll(activeTypes);
        PortMachineDetachReport detach = PortMachineDetachReport.empty();
        if (!scratchStaleTypes.isEmpty()) {
            detach = HbmFluidPortMachine.detachReceiverPortsForTypesReport(
                    level, origin, receiverPorts, scratchStaleTypes, receiver);
        }
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(receiverTypes);
        scratchStaleTypes.retainAll(activeTypes);
        copyPorts(receiverPorts, scratchRemovedPorts);
        removePorts(scratchRemovedPorts, ports);
        if (!scratchRemovedPorts.isEmpty() && !scratchStaleTypes.isEmpty()) {
            detach = detach.merge(HbmFluidPortMachine.detachReceiverPortsForTypesReport(
                    level, origin, scratchRemovedPorts, scratchStaleTypes, receiver));
        }
        scratchStaleTypes.clear();
        scratchRemovedPorts.clear();
        return detach;
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
        PortMachineDetachDetailReport detach = staleTypes.isEmpty()
                ? PortMachineDetachDetailReport.empty()
                : HbmFluidPortMachine.detachReceiverPortsForTypesDetailedReport(
                        level, origin, receiverPorts, staleTypes, receiver);
        List<FluidPort> removedPorts = new ArrayList<>(receiverPorts);
        removePorts(removedPorts, ports);
        Set<FluidType> retainedTypes = new HashSet<>(receiverTypes);
        retainedTypes.retainAll(activeTypes);
        return removedPorts.isEmpty() || retainedTypes.isEmpty()
                ? detach
                : detach.merge(HbmFluidPortMachine.detachReceiverPortsForTypesDetailedReport(
                        level, origin, removedPorts, retainedTypes, receiver));
    }

    private void detachObsoleteProvidersNoReport(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Set<FluidType> activeTypes, HbmFluidProvider provider) {
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(providerTypes);
        scratchStaleTypes.removeAll(activeTypes);
        if (!scratchStaleTypes.isEmpty()) {
            HbmFluidPortMachine.detachProviderPortsForTypes(
                    level, origin, providerPorts, scratchStaleTypes, provider);
        }
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(providerTypes);
        scratchStaleTypes.retainAll(activeTypes);
        copyPorts(providerPorts, scratchRemovedPorts);
        removePorts(scratchRemovedPorts, ports);
        if (!scratchRemovedPorts.isEmpty() && !scratchStaleTypes.isEmpty()) {
            HbmFluidPortMachine.detachProviderPortsForTypes(
                    level, origin, scratchRemovedPorts, scratchStaleTypes, provider);
        }
        scratchStaleTypes.clear();
        scratchRemovedPorts.clear();
    }

    private PortMachineDetachReport detachObsoleteProviders(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Set<FluidType> activeTypes, HbmFluidProvider provider) {
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(providerTypes);
        scratchStaleTypes.removeAll(activeTypes);
        PortMachineDetachReport detach = PortMachineDetachReport.empty();
        if (!scratchStaleTypes.isEmpty()) {
            detach = HbmFluidPortMachine.detachProviderPortsForTypesReport(
                    level, origin, providerPorts, scratchStaleTypes, provider);
        }
        scratchStaleTypes.clear();
        scratchStaleTypes.addAll(providerTypes);
        scratchStaleTypes.retainAll(activeTypes);
        copyPorts(providerPorts, scratchRemovedPorts);
        removePorts(scratchRemovedPorts, ports);
        if (!scratchRemovedPorts.isEmpty() && !scratchStaleTypes.isEmpty()) {
            detach = detach.merge(HbmFluidPortMachine.detachProviderPortsForTypesReport(
                    level, origin, scratchRemovedPorts, scratchStaleTypes, provider));
        }
        scratchStaleTypes.clear();
        scratchRemovedPorts.clear();
        return detach;
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
        PortMachineDetachDetailReport detach = staleTypes.isEmpty()
                ? PortMachineDetachDetailReport.empty()
                : HbmFluidPortMachine.detachProviderPortsForTypesDetailedReport(
                        level, origin, providerPorts, staleTypes, provider);
        List<FluidPort> removedPorts = new ArrayList<>(providerPorts);
        removePorts(removedPorts, ports);
        Set<FluidType> retainedTypes = new HashSet<>(providerTypes);
        retainedTypes.retainAll(activeTypes);
        return removedPorts.isEmpty() || retainedTypes.isEmpty()
                ? detach
                : detach.merge(HbmFluidPortMachine.detachProviderPortsForTypesDetailedReport(
                        level, origin, removedPorts, retainedTypes, provider));
    }

    private void copyTransceiverPorts(Iterable<FluidPort> ports) {
        copyPorts(ports, scratchReceiverPorts);
        copyPorts(scratchReceiverPorts, scratchProviderPorts);
    }

    private static void copyPort(FluidPort port, List<FluidPort> target) {
        target.clear();
        if (port != null) {
            target.add(immutablePort(port));
        }
    }

    private static void copyPorts(Iterable<FluidPort> ports, List<FluidPort> target) {
        target.clear();
        if (ports == null) {
            return;
        }
        for (FluidPort port : ports) {
            if (port != null) {
                target.add(immutablePort(port));
            }
        }
    }

    private static FluidPort immutablePort(FluidPort port) {
        BlockPos offset = port.offset();
        BlockPos immutableOffset = offset.immutable();
        return immutableOffset == offset
                ? port
                : new FluidPort(immutableOffset, port.direction());
    }

    private static void removePorts(List<FluidPort> target, Iterable<FluidPort> ports) {
        if (ports == null) {
            return;
        }
        for (FluidPort port : ports) {
            target.remove(port);
        }
    }

    private static void replacePorts(List<FluidPort> tracked, List<FluidPort> current) {
        if (tracked.equals(current)) {
            return;
        }
        tracked.clear();
        tracked.addAll(current);
    }

    private void clearTracking() {
        receiverTypes.clear();
        providerTypes.clear();
        providerKeys.clear();
        receiverPorts.clear();
        providerPorts.clear();
        scratchReceiverTypes.clear();
        scratchProviderTypes.clear();
        scratchProviderKeys.clear();
        scratchStaleTypes.clear();
        scratchReceiverPorts.clear();
        scratchProviderPorts.clear();
        scratchRemovedPorts.clear();
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

    private static void providerKeyIncludingEmpty(HbmFluidTank tank, Set<ProviderKey> keys) {
        if (tank != null && tank.getTankType() != HbmFluids.NONE) {
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
