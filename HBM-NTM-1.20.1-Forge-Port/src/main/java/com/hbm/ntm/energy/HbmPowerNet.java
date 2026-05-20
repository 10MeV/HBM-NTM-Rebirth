package com.hbm.ntm.energy;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class HbmPowerNet extends HbmNodeNet<HbmEnergyNode> {
    public static final long DEFAULT_TIMEOUT_MS = 3_000L;

    private static final Random RANDOM = new Random();

    private final Map<HbmEnergyReceiver, Long> receiverEntries = new LinkedHashMap<>();
    private final Map<HbmEnergyProvider, Long> providerEntries = new LinkedHashMap<>();
    private final long timeoutMs;

    private long energyTracker;

    public HbmPowerNet() {
        this(DEFAULT_TIMEOUT_MS);
    }

    public HbmPowerNet(long timeoutMs) {
        this.timeoutMs = Math.max(0L, timeoutMs);
    }

    public void resetTrackers() {
        energyTracker = 0L;
    }

    public long getEnergyTracker() {
        return energyTracker;
    }

    public boolean isSubscribed(HbmEnergyReceiver receiver) {
        return receiverEntries.containsKey(receiver);
    }

    public void addReceiver(HbmEnergyReceiver receiver) {
        if (receiver != null) {
            receiverEntries.put(receiver, System.currentTimeMillis());
        }
    }

    public void removeReceiver(HbmEnergyReceiver receiver) {
        receiverEntries.remove(receiver);
    }

    public boolean isProvider(HbmEnergyProvider provider) {
        return providerEntries.containsKey(provider);
    }

    public void addProvider(HbmEnergyProvider provider) {
        if (provider != null) {
            providerEntries.put(provider, System.currentTimeMillis());
        }
    }

    public void removeProvider(HbmEnergyProvider provider) {
        providerEntries.remove(provider);
    }

    public void clearSubscriptions() {
        receiverEntries.clear();
        providerEntries.clear();
        energyTracker = 0L;
    }

    @Override
    public void destroy() {
        super.destroy();
        clearSubscriptions();
    }

    public long update() {
        if (providerEntries.isEmpty() || receiverEntries.isEmpty()) {
            pruneExpired(System.currentTimeMillis());
            return 0L;
        }

        long timestamp = System.currentTimeMillis();
        List<Entry<HbmEnergyProvider>> providers = collectProviders(timestamp);
        if (providers.isEmpty()) {
            return 0L;
        }

        long powerAvailable = sumEntries(providers);
        ReceiverDemand receiverDemand = collectReceivers(timestamp);
        if (receiverDemand.totalDemand <= 0L) {
            return 0L;
        }

        long energyUsed = distributeToReceivers(receiverDemand, Math.min(powerAvailable, receiverDemand.totalDemand));
        if (energyUsed <= 0L) {
            return 0L;
        }

        removeFromProviders(providers, powerAvailable, energyUsed);
        energyTracker += energyUsed;
        return energyUsed;
    }

    public long sendPowerDiode(long power) {
        if (power <= 0L || receiverEntries.isEmpty()) {
            return Math.max(0L, power);
        }

        ReceiverDemand receiverDemand = collectReceivers(System.currentTimeMillis());
        if (receiverDemand.totalDemand <= 0L) {
            return power;
        }

        long energyUsed = distributeToReceivers(receiverDemand, Math.min(power, receiverDemand.totalDemand));
        energyTracker += energyUsed;
        return power - energyUsed;
    }

    private List<Entry<HbmEnergyProvider>> collectProviders(long timestamp) {
        List<Entry<HbmEnergyProvider>> providers = new ArrayList<>();
        Iterator<Map.Entry<HbmEnergyProvider, Long>> iterator = providerEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<HbmEnergyProvider, Long> entry = iterator.next();
            if (isExpired(timestamp, entry.getValue()) || !isValidProvider(entry.getKey())) {
                iterator.remove();
                continue;
            }
            long available = Math.min(entry.getKey().getPower(), entry.getKey().getProviderSpeed());
            if (available > 0L) {
                providers.add(new Entry<>(entry.getKey(), available));
            }
        }
        return providers;
    }

    private ReceiverDemand collectReceivers(long timestamp) {
        ReceiverDemand result = new ReceiverDemand();
        Iterator<Map.Entry<HbmEnergyReceiver, Long>> iterator = receiverEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<HbmEnergyReceiver, Long> entry = iterator.next();
            if (isExpired(timestamp, entry.getValue()) || !isValidReceiver(entry.getKey())) {
                iterator.remove();
                continue;
            }
            HbmEnergyReceiver receiver = entry.getKey();
            long demand = Math.min(Math.max(0L, receiver.getMaxPower() - receiver.getPower()), receiver.getReceiverSpeed());
            if (demand > 0L) {
                HbmEnergyReceiver.ConnectionPriority priority = receiver.getPriority();
                result.receivers.get(priority).add(new Entry<>(receiver, demand));
                result.demand.put(priority, result.demand.get(priority) + demand);
                result.totalDemand += demand;
            }
        }
        return result;
    }

    private long distributeToReceivers(ReceiverDemand receiverDemand, long toTransfer) {
        long energyUsed = 0L;
        HbmEnergyReceiver.ConnectionPriority[] priorities = HbmEnergyReceiver.ConnectionPriority.values();
        for (int i = priorities.length - 1; i >= 0; i--) {
            HbmEnergyReceiver.ConnectionPriority priority = priorities[i];
            List<Entry<HbmEnergyReceiver>> receivers = receiverDemand.receivers.get(priority);
            long priorityDemand = receiverDemand.demand.get(priority);
            if (receivers.isEmpty() || priorityDemand <= 0L || toTransfer <= 0L) {
                continue;
            }

            long priorityUsed = 0L;
            for (Entry<HbmEnergyReceiver> entry : receivers) {
                double weight = (double) entry.amount / (double) priorityDemand;
                long toSend = (long) Math.min(Math.max(toTransfer * weight, 0D), entry.amount);
                long accepted = toSend - entry.value.transferPower(toSend);
                priorityUsed += accepted;
            }

            energyUsed += priorityUsed;
            toTransfer -= priorityUsed;
        }
        return energyUsed;
    }

    private void removeFromProviders(List<Entry<HbmEnergyProvider>> providers, long powerAvailable, long energyUsed) {
        long leftover = energyUsed;
        for (Entry<HbmEnergyProvider> entry : providers) {
            double weight = powerAvailable > 0L ? (double) entry.amount / (double) powerAvailable : 0D;
            long toUse = (long) Math.max(energyUsed * weight, 0D);
            long used = entry.value.usePower(toUse);
            leftover -= used;
        }

        int iterationsLeft = 100;
        while (iterationsLeft > 0 && leftover > 0L && !providers.isEmpty()) {
            iterationsLeft--;
            HbmEnergyProvider provider = providers.get(RANDOM.nextInt(providers.size())).value;
            long used = provider.usePower(Math.min(leftover, provider.getPower()));
            leftover -= used;
            if (used <= 0L) {
                break;
            }
        }
    }

    private void pruneExpired(long timestamp) {
        providerEntries.entrySet().removeIf(entry -> isExpired(timestamp, entry.getValue()));
        receiverEntries.entrySet().removeIf(entry -> isExpired(timestamp, entry.getValue()));
    }

    private boolean isExpired(long timestamp, long lastSeen) {
        return timeoutMs > 0L && timestamp - lastSeen > timeoutMs;
    }

    protected boolean isValidProvider(HbmEnergyProvider provider) {
        return provider != null;
    }

    protected boolean isValidReceiver(HbmEnergyReceiver receiver) {
        return receiver != null;
    }

    private static long sumEntries(List<Entry<HbmEnergyProvider>> entries) {
        long sum = 0L;
        for (Entry<HbmEnergyProvider> entry : entries) {
            sum += entry.amount;
        }
        return sum;
    }

    private static final class Entry<T> {
        private final T value;
        private final long amount;

        private Entry(T value, long amount) {
            this.value = value;
            this.amount = amount;
        }
    }

    private static final class ReceiverDemand {
        private final Map<HbmEnergyReceiver.ConnectionPriority, List<Entry<HbmEnergyReceiver>>> receivers =
                new EnumMap<>(HbmEnergyReceiver.ConnectionPriority.class);
        private final Map<HbmEnergyReceiver.ConnectionPriority, Long> demand =
                new EnumMap<>(HbmEnergyReceiver.ConnectionPriority.class);
        private long totalDemand;

        private ReceiverDemand() {
            for (HbmEnergyReceiver.ConnectionPriority priority : HbmEnergyReceiver.ConnectionPriority.values()) {
                receivers.put(priority, new ArrayList<>());
                demand.put(priority, 0L);
            }
        }
    }
}
