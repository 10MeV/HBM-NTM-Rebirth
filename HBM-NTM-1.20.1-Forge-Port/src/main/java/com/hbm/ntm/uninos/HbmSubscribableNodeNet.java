package com.hbm.ntm.uninos;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HbmSubscribableNodeNet<R, P, L extends HbmNetworkNode> extends HbmNodeNet<L> {
    public static final long DEFAULT_TIMEOUT_MS = 3_000L;

    public final Map<R, Long> receiverEntries = new LinkedHashMap<>();
    public final Map<P, Long> providerEntries = new LinkedHashMap<>();

    private final long timeoutMs;

    public HbmSubscribableNodeNet() {
        this(DEFAULT_TIMEOUT_MS);
    }

    public HbmSubscribableNodeNet(long timeoutMs) {
        this.timeoutMs = Math.max(0L, timeoutMs);
    }

    public boolean isSubscribed(R receiver) {
        return receiverEntries.containsKey(receiver);
    }

    public void addReceiver(R receiver) {
        if (receiver != null) {
            receiverEntries.put(receiver, System.currentTimeMillis());
        }
    }

    public void removeReceiver(R receiver) {
        receiverEntries.remove(receiver);
    }

    public boolean isProvider(P provider) {
        return providerEntries.containsKey(provider);
    }

    public void addProvider(P provider) {
        if (provider != null) {
            providerEntries.put(provider, System.currentTimeMillis());
        }
    }

    public void removeProvider(P provider) {
        providerEntries.remove(provider);
    }

    public int getReceiverCount() {
        pruneStale(System.currentTimeMillis());
        return receiverEntries.size();
    }

    public int getProviderCount() {
        pruneStale(System.currentTimeMillis());
        return providerEntries.size();
    }

    public void resetTrackers() {
    }

    public void update() {
        pruneStale(System.currentTimeMillis());
    }

    public void clearSubscriptions() {
        receiverEntries.clear();
        providerEntries.clear();
    }

    @Override
    public void destroy() {
        super.destroy();
        clearSubscriptions();
    }

    @Override
    public void joinNetwork(HbmNodeNet<L> network) {
        if (!(network instanceof HbmSubscribableNodeNet<?, ?, ?> subscribableNet) || subscribableNet == this) {
            super.joinNetwork(network);
            return;
        }

        List<?> receivers = new ArrayList<>(subscribableNet.receiverEntries.keySet());
        List<?> providers = new ArrayList<>(subscribableNet.providerEntries.keySet());
        super.joinNetwork(network);
        for (Object receiver : receivers) {
            @SuppressWarnings("unchecked")
            R typedReceiver = (R) receiver;
            addReceiver(typedReceiver);
        }
        for (Object provider : providers) {
            @SuppressWarnings("unchecked")
            P typedProvider = (P) provider;
            addProvider(typedProvider);
        }
    }

    protected void pruneStale(long timestamp) {
        receiverEntries.entrySet().removeIf(entry -> isExpired(timestamp, entry.getValue()) || !isValidSubscriber(entry.getKey()));
        providerEntries.entrySet().removeIf(entry -> isExpired(timestamp, entry.getValue()) || !isValidSubscriber(entry.getKey()));
    }

    protected boolean isValidSubscriber(Object subscriber) {
        return subscriber != null && (!(subscriber instanceof BlockEntity blockEntity) || !blockEntity.isRemoved());
    }

    private boolean isExpired(long timestamp, long lastSeen) {
        return timeoutMs > 0L && timestamp - lastSeen > timeoutMs;
    }
}
