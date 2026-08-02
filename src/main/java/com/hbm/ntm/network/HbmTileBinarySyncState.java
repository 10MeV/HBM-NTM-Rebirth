package com.hbm.ntm.network;

import java.util.Arrays;

public final class HbmTileBinarySyncState {
    public static final long LEGACY_FORCED_RESEND_INTERVAL_TICKS = 20L;

    private byte[] lastPayload = new byte[0];
    private boolean hasPayload;
    private long lastSentGameTime = Long.MIN_VALUE;
    private long totalSent;
    private long skippedDuplicates;

    public boolean shouldSend(byte[] payload, long gameTime) {
        return shouldSend(payload, gameTime, LEGACY_FORCED_RESEND_INTERVAL_TICKS, 0L);
    }

    public boolean shouldSend(byte[] payload, long gameTime, long forcedResendIntervalTicks) {
        return shouldSend(payload, gameTime, forcedResendIntervalTicks, 0L);
    }

    public boolean shouldSend(byte[] payload, long gameTime, long forcedResendIntervalTicks,
                              long forcedResendPhase) {
        byte[] safePayload = payload == null ? new byte[0] : payload;
        boolean changed = !hasPayload || !Arrays.equals(lastPayload, safePayload);
        boolean forced = hasPayload && forcedResendIntervalTicks > 0L
                && (gameTime < lastSentGameTime
                || (gameTime != lastSentGameTime
                && Math.floorMod(gameTime, forcedResendIntervalTicks)
                == Math.floorMod(forcedResendPhase, forcedResendIntervalTicks)));
        if (!changed && !forced) {
            skippedDuplicates++;
            return false;
        }
        // writeTileBinaryPayload already hands us a detached byte array. Keep our
        // own copy only when the contents change; duplicate checks used to clone
        // the complete machine payload on every server tick.
        if (changed) {
            lastPayload = Arrays.copyOf(safePayload, safePayload.length);
        }
        hasPayload = true;
        lastSentGameTime = gameTime;
        totalSent++;
        return true;
    }

    public void reset() {
        lastPayload = new byte[0];
        hasPayload = false;
        lastSentGameTime = Long.MIN_VALUE;
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
