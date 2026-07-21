package com.hbm.ntm.uninos;

import com.hbm.ntm.api.tile.LoadedTile;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HbmSubscribableNodeNet<R, P, L extends HbmNetworkNode> extends HbmNodeNet<R, P, L> {
    public static final long DEFAULT_TIMEOUT_MS = 3_000L;

    private final long timeoutMs;

    public HbmSubscribableNodeNet() {
        this(DEFAULT_TIMEOUT_MS);
    }

    public HbmSubscribableNodeNet(long timeoutMs) {
        this.timeoutMs = Math.max(0L, timeoutMs);
    }

    @Override
    public void addReceiver(R receiver) {
        if (receiver != null) {
            receiverEntries.put(receiver, System.currentTimeMillis());
        }
    }

    @Override
    public void removeReceiver(R receiver) {
        receiverEntries.remove(receiver);
    }

    @Override
    public void addProvider(P provider) {
        if (provider != null) {
            providerEntries.put(provider, System.currentTimeMillis());
        }
    }

    @Override
    public void removeProvider(P provider) {
        providerEntries.remove(provider);
    }

    public int getReceiverCount() {
        pruneStale(System.currentTimeMillis());
        return receiverEntries.size();
    }

    public List<R> receiverSnapshot() {
        pruneStale(System.currentTimeMillis());
        return new ArrayList<>(receiverEntries.keySet());
    }

    public int getProviderCount() {
        pruneStale(System.currentTimeMillis());
        return providerEntries.size();
    }

    public List<P> providerSnapshot() {
        pruneStale(System.currentTimeMillis());
        return new ArrayList<>(providerEntries.keySet());
    }

    public void resetTrackers() {
    }

    public long update() {
        pruneStale(System.currentTimeMillis());
        return 0L;
    }

    public void clearSubscriptions() {
        receiverEntries.clear();
        providerEntries.clear();
    }

    @Override
    protected void onLegacyReap() {
        clearSubscriptions();
    }

    protected void pruneStale(long timestamp) {
        receiverEntries.entrySet().removeIf(entry -> isExpired(timestamp, entry.getValue()) || !isValidSubscriber(entry.getKey()));
        providerEntries.entrySet().removeIf(entry -> isExpired(timestamp, entry.getValue()) || !isValidSubscriber(entry.getKey()));
    }

    protected boolean isValidSubscriber(Object subscriber) {
        if (subscriber == null) {
            return false;
        }
        if (subscriber instanceof LoadedTile loadedTile && !loadedTile.isLoaded()) {
            return false;
        }
        return !(subscriber instanceof BlockEntity blockEntity) || (!blockEntity.isRemoved() && blockEntity.getLevel() != null);
    }

    private boolean isExpired(long timestamp, long lastSeen) {
        return timeoutMs > 0L && timestamp - lastSeen > timeoutMs;
    }
}
