package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import java.util.Optional;
import java.util.function.Consumer;

public final class RadarScreenDisplayProfile {
    public static final int SERVER_SYNC_INTERVAL_TICKS = 100;
    public static final int LINKED_SYNC_DELAY_TICKS = 25;
    public static final double MAX_RENDER_DISTANCE_SQUARED = 65_536.0D;
    public static final int VIEW_DISTANCE = 256;
    private static final int WORLD_SWEEP_PERIOD_TICKS = 56;
    private static final double WORLD_SWEEP_SPEED_DIVISOR = 30.0D;
    private static final long NOISE_TIME_MULTIPLIER = 31L;

    public static boolean shouldSyncBeforeReset(RadarScreenSnapshot snapshot) {
        return snapshot != null && (snapshot.linked() || !snapshot.entries().isEmpty());
    }

    public static RadarScreenSnapshot resetAfterServerTick(RadarScreenSnapshot snapshot) {
        return RadarScreenSnapshot.UNLINKED;
    }

    public static ServerTickPlan serverTick(RadarScreenSnapshot snapshot) {
        return new ServerTickPlan(shouldSyncBeforeReset(snapshot), resetAfterServerTick(snapshot));
    }

    public static double worldSweepOffset(long gameTime, float partialTick) {
        return ((gameTime % WORLD_SWEEP_PERIOD_TICKS) + partialTick) / WORLD_SWEEP_SPEED_DIVISOR;
    }

    public static long worldNoiseSeed(long gameTime, BlockPos pos) {
        return gameTime * NOISE_TIME_MULTIPLIER + (pos != null ? pos.asLong() : 0L);
    }

    public static Optional<BlockPos> linkedRadarPos(RadarScreenSnapshot snapshot) {
        if (snapshot == null || !snapshot.linked()) {
            return Optional.empty();
        }
        return Optional.of(snapshot.refPos());
    }

    public static WorldOverlay overlay(RadarScreenSnapshot snapshot, long gameTime, float partialTick,
            BlockPos screenPos) {
        RadarScreenSnapshot safeSnapshot = snapshot != null ? snapshot : RadarScreenSnapshot.UNLINKED;
        return safeSnapshot.linked()
                ? WorldOverlay.linked(worldSweepOffset(gameTime, partialTick), safeSnapshot)
                : WorldOverlay.noise(worldNoiseSeed(gameTime, screenPos));
    }

    public static void forEachWorldBlip(RadarScreenSnapshot snapshot, Consumer<WorldBlip> consumer) {
        if (snapshot == null || consumer == null) {
            return;
        }
        for (RadarEntry entry : snapshot.entries()) {
            consumer.accept(new WorldBlip(entry, snapshot.refPos(), snapshot.range()));
        }
    }

    public static AABB renderBoundingBox(BlockPos pos) {
        return new AABB(
                pos.getX() - 1.0D,
                pos.getY(),
                pos.getZ() - 1.0D,
                pos.getX() + 2.0D,
                pos.getY() + 2.0D,
                pos.getZ() + 2.0D);
    }

    public record ServerTickPlan(boolean syncBeforeReset, RadarScreenSnapshot nextSnapshot) {
    }

    public record WorldOverlay(boolean linked, double sweepOffset, long noiseSeed, RadarScreenSnapshot snapshot) {
        public static WorldOverlay linked(double sweepOffset, RadarScreenSnapshot snapshot) {
            return new WorldOverlay(true, sweepOffset, 0L, snapshot);
        }

        public static WorldOverlay noise(long noiseSeed) {
            return new WorldOverlay(false, 0.0D, noiseSeed, RadarScreenSnapshot.UNLINKED);
        }
    }

    public record WorldBlip(RadarEntry entry, BlockPos reference, int range) {
    }

    private RadarScreenDisplayProfile() {
    }
}
