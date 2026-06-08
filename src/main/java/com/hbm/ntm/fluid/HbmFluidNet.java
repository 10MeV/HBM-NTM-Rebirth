package com.hbm.ntm.fluid;

import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmNodeNet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.world.level.block.entity.BlockEntity;

public class HbmFluidNet extends HbmNodeNet<HbmFluidNode> {
    public static final long DEFAULT_TIMEOUT_MS = 3_000L;

    private static final Random RANDOM = new Random();

    private final Map<HbmFluidReceiver, Long> receiverEntries = new LinkedHashMap<>();
    private final Map<HbmFluidProvider, Long> providerEntries = new LinkedHashMap<>();
    private final FluidType type;
    private final long timeoutMs;
    private long fluidTracker;
    private final long[] lastAttemptedByPressure = new long[HbmFluidUser.HIGHEST_VALID_PRESSURE + 1];
    private final long[] lastTransferredByPressure = new long[HbmFluidUser.HIGHEST_VALID_PRESSURE + 1];
    private final long[] lastProviderUseByPressure = new long[HbmFluidUser.HIGHEST_VALID_PRESSURE + 1];
    private final long[] lastUnaccountedByPressure = new long[HbmFluidUser.HIGHEST_VALID_PRESSURE + 1];

    public HbmFluidNet(FluidType type) {
        this(type, DEFAULT_TIMEOUT_MS);
    }

    public HbmFluidNet(FluidType type, long timeoutMs) {
        this.type = type == null ? HbmFluids.NONE : type;
        this.timeoutMs = Math.max(0L, timeoutMs);
    }

    public FluidType getFluidType() {
        return type;
    }

    public void resetTrackers() {
        fluidTracker = 0L;
        Arrays.fill(lastAttemptedByPressure, 0L);
        Arrays.fill(lastTransferredByPressure, 0L);
        Arrays.fill(lastProviderUseByPressure, 0L);
        Arrays.fill(lastUnaccountedByPressure, 0L);
    }

    public long getFluidTracker() {
        return fluidTracker;
    }

    public int getReceiverCount() {
        pruneExpired(System.currentTimeMillis());
        return receiverEntries.size();
    }

    public int getProviderCount() {
        pruneExpired(System.currentTimeMillis());
        return providerEntries.size();
    }

    public DebugSnapshot createDebugSnapshot() {
        pruneExpired(System.currentTimeMillis());
        long[] providerAvailable = new long[HbmFluidUser.HIGHEST_VALID_PRESSURE + 1];
        long[] providerRate = new long[HbmFluidUser.HIGHEST_VALID_PRESSURE + 1];
        long[] receiverDemand = new long[HbmFluidUser.HIGHEST_VALID_PRESSURE + 1];
        long[] receiverRate = new long[HbmFluidUser.HIGHEST_VALID_PRESSURE + 1];
        Map<HbmEnergyReceiver.ConnectionPriority, Integer> receiversByPriority =
                new EnumMap<>(HbmEnergyReceiver.ConnectionPriority.class);
        for (HbmEnergyReceiver.ConnectionPriority priority : HbmEnergyReceiver.ConnectionPriority.values()) {
            receiversByPriority.put(priority, 0);
        }

        for (HbmFluidProvider provider : providerEntries.keySet()) {
            if (!isValidProvider(provider)) {
                continue;
            }
            int[] range = pressureRange(provider.getProvidingPressureRange(type));
            for (int pressure = range[0]; pressure <= range[1]; pressure++) {
                providerAvailable[pressure] += Math.max(0L, provider.getFluidAvailable(type, pressure));
                providerRate[pressure] += Math.max(0L, provider.getProviderSpeed(type, pressure));
            }
        }

        for (HbmFluidReceiver receiver : receiverEntries.keySet()) {
            if (!isValidReceiver(receiver)) {
                continue;
            }
            HbmEnergyReceiver.ConnectionPriority priority = receiver.getFluidPriority();
            receiversByPriority.put(priority, receiversByPriority.get(priority) + 1);
            int[] range = pressureRange(receiver.getReceivingPressureRange(type));
            for (int pressure = range[0]; pressure <= range[1]; pressure++) {
                receiverDemand[pressure] += Math.max(0L, receiver.getDemand(type, pressure));
                receiverRate[pressure] += Math.max(0L, receiver.getReceiverSpeed(type, pressure));
            }
        }

        return new DebugSnapshot(
                isValid(),
                type.getName(),
                linkCount(),
                getProviderCount(),
                getReceiverCount(),
                providerAvailable,
                providerRate,
                receiverDemand,
                receiverRate,
                fluidTracker,
                lastAttemptedByPressure.clone(),
                lastTransferredByPressure.clone(),
                lastProviderUseByPressure.clone(),
                lastUnaccountedByPressure.clone(),
                receiversByPriority);
    }

    public void addReceiver(HbmFluidReceiver receiver) {
        if (receiver != null) {
            receiverEntries.put(receiver, System.currentTimeMillis());
        }
    }

    public boolean isSubscribed(HbmFluidReceiver receiver) {
        return receiverEntries.containsKey(receiver);
    }

    public void removeReceiver(HbmFluidReceiver receiver) {
        receiverEntries.remove(receiver);
    }

    public void addProvider(HbmFluidProvider provider) {
        if (provider != null) {
            providerEntries.put(provider, System.currentTimeMillis());
        }
    }

    public boolean isProvider(HbmFluidProvider provider) {
        return providerEntries.containsKey(provider);
    }

    public void removeProvider(HbmFluidProvider provider) {
        providerEntries.remove(provider);
    }

    public void clearSubscriptions() {
        receiverEntries.clear();
        providerEntries.clear();
        fluidTracker = 0L;
    }

    public List<HbmFluidReceiver> getSubscribedReceivers() {
        pruneExpired(System.currentTimeMillis());
        return List.copyOf(receiverEntries.keySet());
    }

    public int damageSubscribedReceiversFromOverpressure() {
        pruneExpired(System.currentTimeMillis());
        int damaged = 0;
        for (HbmFluidReceiver receiver : List.copyOf(receiverEntries.keySet())) {
            if (receiver instanceof BlockEntity blockEntity && HbmFluidOverpressure.damageReceiver(blockEntity)) {
                damaged++;
            }
        }
        return damaged;
    }

    @Override
    public void destroy() {
        super.destroy();
        clearSubscriptions();
    }

    @Override
    public void joinNetwork(com.hbm.ntm.uninos.HbmNodeNet<HbmFluidNode> network) {
        if (!(network instanceof HbmFluidNet fluidNet) || fluidNet == this) {
            super.joinNetwork(network);
            return;
        }

        List<HbmFluidReceiver> receivers = new ArrayList<>(fluidNet.receiverEntries.keySet());
        List<HbmFluidProvider> providers = new ArrayList<>(fluidNet.providerEntries.keySet());
        super.joinNetwork(network);
        for (HbmFluidReceiver receiver : receivers) {
            addReceiver(receiver);
        }
        for (HbmFluidProvider provider : providers) {
            addProvider(provider);
        }
    }

    public long update() {
        if (type == HbmFluids.NONE || providerEntries.isEmpty() || receiverEntries.isEmpty()) {
            pruneExpired(System.currentTimeMillis());
            return 0L;
        }

        long timestamp = System.currentTimeMillis();
        PressureDistribution providers = collectProviders(timestamp);
        PressureDemand receivers = collectReceivers(timestamp);
        long transferred = 0L;

        for (int pressure = 0; pressure <= HbmFluidUser.HIGHEST_VALID_PRESSURE; pressure++) {
            long available = providers.available[pressure];
            if (available <= 0L || receivers.totalDemand[pressure] <= 0L) {
                continue;
            }
            long attempted = Math.min(available, receivers.totalDemand[pressure]);
            lastAttemptedByPressure[pressure] = attempted;
            long used = distributePressure(receivers, pressure, attempted);
            if (used <= 0L) {
                continue;
            }
            ProviderRemovalStats removal = removeFromProviders(providers.providers[pressure], available, used, pressure);
            lastTransferredByPressure[pressure] = used;
            lastProviderUseByPressure[pressure] = removal.requestedUse();
            lastUnaccountedByPressure[pressure] = removal.unaccounted();
            transferred += used;
        }

        fluidTracker += transferred;
        return transferred;
    }

    private PressureDistribution collectProviders(long timestamp) {
        PressureDistribution result = new PressureDistribution();
        Iterator<Map.Entry<HbmFluidProvider, Long>> iterator = providerEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<HbmFluidProvider, Long> entry = iterator.next();
            if (isExpired(timestamp, entry.getValue()) || !isValidProvider(entry.getKey())) {
                iterator.remove();
                continue;
            }
            HbmFluidProvider provider = entry.getKey();
            int[] range = pressureRange(provider.getProvidingPressureRange(type));
            for (int pressure = range[0]; pressure <= range[1]; pressure++) {
                long available = Math.min(Math.max(0L, provider.getFluidAvailable(type, pressure)),
                        Math.max(0L, provider.getProviderSpeed(type, pressure)));
                if (available > 0L) {
                    result.providers[pressure].add(new Entry<>(provider, available));
                    result.available[pressure] += available;
                }
            }
        }
        return result;
    }

    private PressureDemand collectReceivers(long timestamp) {
        PressureDemand result = new PressureDemand();
        Iterator<Map.Entry<HbmFluidReceiver, Long>> iterator = receiverEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<HbmFluidReceiver, Long> entry = iterator.next();
            if (isExpired(timestamp, entry.getValue()) || !isValidReceiver(entry.getKey())) {
                iterator.remove();
                continue;
            }
            HbmFluidReceiver receiver = entry.getKey();
            int[] range = pressureRange(receiver.getReceivingPressureRange(type));
            HbmEnergyReceiver.ConnectionPriority priority = receiver.getFluidPriority();
            for (int pressure = range[0]; pressure <= range[1]; pressure++) {
                long demand = Math.min(Math.max(0L, receiver.getDemand(type, pressure)),
                        Math.max(0L, receiver.getReceiverSpeed(type, pressure)));
                if (demand > 0L) {
                    result.receivers[pressure].get(priority).add(new Entry<>(receiver, demand));
                    result.demand[pressure].put(priority, result.demand[pressure].get(priority) + demand);
                    result.totalDemand[pressure] += demand;
                }
            }
        }
        return result;
    }

    private long distributePressure(PressureDemand receivers, int pressure, long toTransfer) {
        long used = 0L;
        HbmEnergyReceiver.ConnectionPriority[] priorities = HbmEnergyReceiver.ConnectionPriority.values();
        for (int i = priorities.length - 1; i >= 0; i--) {
            HbmEnergyReceiver.ConnectionPriority priority = priorities[i];
            List<Entry<HbmFluidReceiver>> priorityReceivers = receivers.receivers[pressure].get(priority);
            long priorityDemand = receivers.demand[pressure].get(priority);
            if (priorityReceivers.isEmpty() || priorityDemand <= 0L || toTransfer <= 0L) {
                continue;
            }

            long priorityUsed = 0L;
            long priorityBudget = Math.min(toTransfer, priorityDemand);
            for (Entry<HbmFluidReceiver> entry : priorityReceivers) {
                double weight = (double) entry.amount / (double) priorityDemand;
                long toSend = (long) Math.min(Math.max(priorityBudget * weight, 0D), entry.amount);
                long accepted = toSend - entry.value.transferFluid(type, pressure, toSend);
                priorityUsed += accepted;
            }

            long leftover = priorityBudget - priorityUsed;
            int iterationsLeft = 100;
            while (iterationsLeft > 0 && leftover > 0L) {
                iterationsLeft--;
                boolean moved = false;
                for (Entry<HbmFluidReceiver> entry : priorityReceivers) {
                    if (leftover <= 0L) {
                        break;
                    }
                    long demandLeft = Math.min(Math.max(0L, entry.value.getDemand(type, pressure)),
                            Math.max(0L, entry.value.getReceiverSpeed(type, pressure)));
                    if (demandLeft <= 0L) {
                        continue;
                    }
                    long toSend = Math.min(leftover, demandLeft);
                    long accepted = toSend - entry.value.transferFluid(type, pressure, toSend);
                    if (accepted > 0L) {
                        priorityUsed += accepted;
                        leftover -= accepted;
                        moved = true;
                    }
                }
                if (!moved) {
                    break;
                }
            }

            used += priorityUsed;
            toTransfer -= priorityUsed;
        }
        return used;
    }

    private ProviderRemovalStats removeFromProviders(List<Entry<HbmFluidProvider>> providers, long available, long used, int pressure) {
        long leftover = used;
        long requestedUse = 0L;
        for (Entry<HbmFluidProvider> entry : providers) {
            double weight = available > 0L ? (double) entry.amount / (double) available : 0D;
            long toUse = (long) Math.max(used * weight, 0D);
            entry.value.useUpFluid(type, pressure, toUse);
            requestedUse += toUse;
            leftover -= toUse;
        }

        int iterationsLeft = 100;
        while (iterationsLeft > 0 && leftover > 0L && !providers.isEmpty()) {
            iterationsLeft--;
            HbmFluidProvider provider = providers.get(RANDOM.nextInt(providers.size())).value;
            long toUse = Math.min(leftover, provider.getFluidAvailable(type, pressure));
            provider.useUpFluid(type, pressure, toUse);
            requestedUse += toUse;
            leftover -= toUse;
            if (toUse <= 0L) {
                break;
            }
        }
        return new ProviderRemovalStats(requestedUse, Math.max(0L, leftover));
    }

    private void pruneExpired(long timestamp) {
        providerEntries.entrySet().removeIf(entry -> isExpired(timestamp, entry.getValue()));
        receiverEntries.entrySet().removeIf(entry -> isExpired(timestamp, entry.getValue()));
    }

    private boolean isExpired(long timestamp, long lastSeen) {
        return timeoutMs > 0L && timestamp - lastSeen > timeoutMs;
    }

    protected boolean isValidProvider(HbmFluidProvider provider) {
        return provider != null;
    }

    protected boolean isValidReceiver(HbmFluidReceiver receiver) {
        return receiver != null;
    }

    private int[] pressureRange(int[] range) {
        if (range == null || range.length < 2) {
            return HbmFluidUser.DEFAULT_PRESSURE_RANGE;
        }
        int low = HbmFluidTank.clampPressure(Math.min(range[0], range[1]));
        int high = HbmFluidTank.clampPressure(Math.max(range[0], range[1]));
        return new int[] {low, high};
    }

    private static final class Entry<T> {
        private final T value;
        private final long amount;

        private Entry(T value, long amount) {
            this.value = value;
            this.amount = amount;
        }
    }

    public record DebugSnapshot(
            boolean valid,
            String fluid,
            int links,
            int providers,
            int receivers,
            long[] providerAvailable,
            long[] providerRate,
            long[] receiverDemand,
            long[] receiverRate,
            long lastTransfer,
            long[] lastAttemptedByPressure,
            long[] lastTransferredByPressure,
            long[] lastProviderUseByPressure,
            long[] lastUnaccountedByPressure,
            Map<HbmEnergyReceiver.ConnectionPriority, Integer> receiversByPriority) {
    }

    private record ProviderRemovalStats(long requestedUse, long unaccounted) {
    }

    @SuppressWarnings("unchecked")
    private static final class PressureDistribution {
        private final long[] available = new long[HbmFluidUser.HIGHEST_VALID_PRESSURE + 1];
        private final List<Entry<HbmFluidProvider>>[] providers = new List[HbmFluidUser.HIGHEST_VALID_PRESSURE + 1];

        private PressureDistribution() {
            for (int i = 0; i < providers.length; i++) {
                providers[i] = new ArrayList<>();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static final class PressureDemand {
        private final long[] totalDemand = new long[HbmFluidUser.HIGHEST_VALID_PRESSURE + 1];
        private final Map<HbmEnergyReceiver.ConnectionPriority, List<Entry<HbmFluidReceiver>>>[] receivers =
                new Map[HbmFluidUser.HIGHEST_VALID_PRESSURE + 1];
        private final Map<HbmEnergyReceiver.ConnectionPriority, Long>[] demand =
                new Map[HbmFluidUser.HIGHEST_VALID_PRESSURE + 1];

        private PressureDemand() {
            for (int pressure = 0; pressure < receivers.length; pressure++) {
                receivers[pressure] = new EnumMap<>(HbmEnergyReceiver.ConnectionPriority.class);
                demand[pressure] = new EnumMap<>(HbmEnergyReceiver.ConnectionPriority.class);
                for (HbmEnergyReceiver.ConnectionPriority priority : HbmEnergyReceiver.ConnectionPriority.values()) {
                    receivers[pressure].put(priority, new ArrayList<>());
                    demand[pressure].put(priority, 0L);
                }
            }
        }
    }
}
