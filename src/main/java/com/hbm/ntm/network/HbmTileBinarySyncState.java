package com.hbm.ntm.network;

import java.util.Arrays;

public final class HbmTileBinarySyncState {
    public static final long LEGACY_FORCED_RESEND_INTERVAL_TICKS = 20L;

    private byte[] lastPayload = new byte[0];
    private boolean hasPayload;
    private long totalSent;
    private long skippedDuplicates;

    public boolean shouldSend(byte[] payload, long gameTime) {
        return shouldSend(payload, gameTime, LEGACY_FORCED_RESEND_INTERVAL_TICKS);
    }

    public boolean shouldSend(byte[] payload, long gameTime, long forcedResendIntervalTicks) {
        byte[] safePayload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        boolean changed = !hasPayload || !Arrays.equals(lastPayload, safePayload);
        boolean forced = forcedResendIntervalTicks > 0L
                && Math.floorMod(gameTime, forcedResendIntervalTicks) == 0L;
        if (!changed && !forced) {
            skippedDuplicates++;
            return false;
        }
        lastPayload = safePayload;
        hasPayload = true;
        totalSent++;
        return true;
    }

    public void reset() {
        lastPayload = new byte[0];
        hasPayload = false;
        totalSent = 0L;
        skippedDuplicates = 0L;
    }

    public boolean hasPayload() {
        return hasPayload;
    }

    public int lastPayloadBytes() {
        return lastPayload.length;
    }

    public long totalSent() {
        return totalSent;
    }

    public long skippedDuplicates() {
        return skippedDuplicates;
    }
}
