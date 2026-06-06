package com.hbm.ntm.client;

@FunctionalInterface
public interface ClientMuzzleFlashListener {
    void onClientMuzzleFlash(int entityId, long timestampMillis);
}
