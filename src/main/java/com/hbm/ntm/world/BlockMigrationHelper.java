package com.hbm.ntm.world;

import com.hbm.ntm.HbmNtm;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.fml.ModList;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class BlockMigrationHelper {
    public static final String NBT_KEY_BUILD_NUMBER = "hfr_migrations_version";
    private static final int UNKNOWN_BUILD_NUMBER = -1;
    private static int cachedBuildNumber = UNKNOWN_BUILD_NUMBER;
    private static final AtomicLong FRESH_CHUNKS = new AtomicLong();
    private static final AtomicLong UP_TO_DATE_CHUNKS = new AtomicLong();
    private static final AtomicLong MIGRATED_CHUNKS = new AtomicLong();
    private static final AtomicLong CURRENT_BUILD_UNKNOWN_CHUNKS = new AtomicLong();
    private static final AtomicLong FUTURE_MARKER_CHUNKS = new AtomicLong();
    private static final AtomicLong SAVED_CHUNKS = new AtomicLong();
    private static volatile MigrationResult lastLoadResult = MigrationResult.unknown();

    public static int buildNumber() {
        if (cachedBuildNumber != UNKNOWN_BUILD_NUMBER) {
            return cachedBuildNumber;
        }
        cachedBuildNumber = ModList.get().getModContainerById(HbmNtm.MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString())
                .flatMap(BlockMigrationHelper::parseBuildNumber)
                .orElse(UNKNOWN_BUILD_NUMBER);
        return cachedBuildNumber;
    }

    public static MigrationResult load(ChunkAccess chunk, CompoundTag tag) {
        MigrationMarker marker = inspectTag(tag);
        if (!marker.hasLegacyMarker()) {
            return remember(MigrationResult.freshChunk());
        }
        if (!marker.currentBuildKnown()) {
            return remember(MigrationResult.currentBuildUnknown(marker.previousBuild(), marker.currentBuild()));
        }
        if (marker.futureMarker()) {
            return remember(MigrationResult.futureMarker(marker.previousBuild(), marker.currentBuild()));
        }
        if (!marker.needsMigration()) {
            return remember(MigrationResult.upToDate(marker.previousBuild(), marker.currentBuild()));
        }
        doMigration(chunk, marker.previousBuild(), marker.currentBuild());
        return remember(MigrationResult.migrated(marker.previousBuild(), marker.currentBuild()));
    }

    public static void save(CompoundTag tag) {
        tag.putInt(NBT_KEY_BUILD_NUMBER, buildNumber());
        SAVED_CHUNKS.incrementAndGet();
    }

    public static void doMigration(ChunkAccess chunk, int previousBuild, int currentBuild) {
        // Legacy BlockMigrations had the chunk scan hook but no concrete replacement rules.
    }

    public static MigrationMarker inspectTag(CompoundTag tag) {
        if (tag == null || !tag.contains(NBT_KEY_BUILD_NUMBER)) {
            return inspectMarker(null);
        }
        return inspectMarker(tag.getInt(NBT_KEY_BUILD_NUMBER));
    }

    public static MigrationMarker inspectMarker(Integer previousBuild) {
        int currentBuild = buildNumber();
        if (previousBuild == null) {
            return new MigrationMarker(false, UNKNOWN_BUILD_NUMBER, currentBuild,
                    currentBuild != UNKNOWN_BUILD_NUMBER, false, false, false, "fresh_or_unmarked");
        }
        if (currentBuild == UNKNOWN_BUILD_NUMBER) {
            return new MigrationMarker(true, previousBuild, currentBuild, false, false, false, false,
                    "current_build_unknown");
        }
        if (previousBuild == currentBuild) {
            return new MigrationMarker(true, previousBuild, currentBuild, true, false, true, false, "up_to_date");
        }
        if (previousBuild > currentBuild) {
            return new MigrationMarker(true, previousBuild, currentBuild, true, false, false, true,
                    "future_marker");
        }
        return new MigrationMarker(true, previousBuild, currentBuild, true, true, false, false, "needs_migration");
    }

    public static MigrationDiagnostics diagnostics() {
        return new MigrationDiagnostics(
                buildNumber(),
                FRESH_CHUNKS.get(),
                UP_TO_DATE_CHUNKS.get(),
                MIGRATED_CHUNKS.get(),
                CURRENT_BUILD_UNKNOWN_CHUNKS.get(),
                FUTURE_MARKER_CHUNKS.get(),
                SAVED_CHUNKS.get(),
                lastLoadResult);
    }

    public static void resetDiagnostics() {
        FRESH_CHUNKS.set(0L);
        UP_TO_DATE_CHUNKS.set(0L);
        MIGRATED_CHUNKS.set(0L);
        CURRENT_BUILD_UNKNOWN_CHUNKS.set(0L);
        FUTURE_MARKER_CHUNKS.set(0L);
        SAVED_CHUNKS.set(0L);
        lastLoadResult = MigrationResult.unknown();
    }

    private static MigrationResult remember(MigrationResult result) {
        if (!result.hadLegacyMarker()) {
            FRESH_CHUNKS.incrementAndGet();
        } else if (result.migrated()) {
            MIGRATED_CHUNKS.incrementAndGet();
        } else if (result.futureMarker()) {
            FUTURE_MARKER_CHUNKS.incrementAndGet();
        } else if (!result.currentBuildKnown()) {
            CURRENT_BUILD_UNKNOWN_CHUNKS.incrementAndGet();
        } else if (result.upToDate()) {
            UP_TO_DATE_CHUNKS.incrementAndGet();
        } else {
            UP_TO_DATE_CHUNKS.incrementAndGet();
        }
        lastLoadResult = result;
        return result;
    }

    private static Optional<Integer> parseBuildNumber(String version) {
        int open = version.indexOf('(');
        int close = version.indexOf(')', open + 1);
        if (open >= 0 && close > open) {
            return parseInteger(version.substring(open + 1, close));
        }
        return parseInteger(version);
    }

    private static Optional<Integer> parseInteger(String value) {
        try {
            return Optional.of(Integer.parseInt(value.trim()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private BlockMigrationHelper() {
    }

    public record MigrationResult(boolean hadLegacyMarker, boolean migrated, int previousBuild, int currentBuild,
                                  String detail) {
        public MigrationResult {
            detail = detail == null || detail.isBlank() ? "unknown" : detail;
        }

        static MigrationResult unknown() {
            return new MigrationResult(false, false, UNKNOWN_BUILD_NUMBER, UNKNOWN_BUILD_NUMBER, "unknown");
        }

        static MigrationResult freshChunk() {
            return new MigrationResult(false, false, UNKNOWN_BUILD_NUMBER, buildNumber(), "fresh_or_unmarked");
        }

        static MigrationResult upToDate(int previousBuild, int currentBuild) {
            return new MigrationResult(true, false, previousBuild, currentBuild, "up_to_date");
        }

        static MigrationResult currentBuildUnknown(int previousBuild, int currentBuild) {
            return new MigrationResult(true, false, previousBuild, currentBuild, "current_build_unknown");
        }

        static MigrationResult futureMarker(int previousBuild, int currentBuild) {
            return new MigrationResult(true, false, previousBuild, currentBuild, "future_marker");
        }

        static MigrationResult migrated(int previousBuild, int currentBuild) {
            return new MigrationResult(true, true, previousBuild, currentBuild, "migrated");
        }

        public boolean isFreshChunk() {
            return !hadLegacyMarker && previousBuild == UNKNOWN_BUILD_NUMBER;
        }

        public boolean currentBuildKnown() {
            return currentBuild != UNKNOWN_BUILD_NUMBER;
        }

        public boolean upToDate() {
            return "up_to_date".equals(detail);
        }

        public boolean futureMarker() {
            return "future_marker".equals(detail);
        }

        public String summary() {
            return "detail=" + detail()
                    + " hadMarker=" + hadLegacyMarker
                    + " migrated=" + migrated
                    + " previousBuild=" + previousBuild
                    + " currentBuild=" + currentBuild;
        }
    }

    public record MigrationDiagnostics(int currentBuild, long freshChunks, long upToDateChunks, long migratedChunks,
                                       long currentBuildUnknownChunks, long futureMarkerChunks, long savedChunks,
                                       MigrationResult lastLoadResult) {
        public long loadedChunks() {
            return freshChunks + upToDateChunks + migratedChunks + currentBuildUnknownChunks + futureMarkerChunks;
        }

        public long skippedChunks() {
            return currentBuildUnknownChunks + futureMarkerChunks;
        }

        public String summary() {
            return "currentBuild=" + currentBuild
                    + " loaded=" + loadedChunks()
                    + " fresh=" + freshChunks
                    + " upToDate=" + upToDateChunks
                    + " migrated=" + migratedChunks
                    + " skipped=" + skippedChunks()
                    + " currentBuildUnknown=" + currentBuildUnknownChunks
                    + " futureMarker=" + futureMarkerChunks
                    + " saved=" + savedChunks;
        }
    }

    public record MigrationMarker(boolean hasLegacyMarker, int previousBuild, int currentBuild,
                                  boolean currentBuildKnown, boolean needsMigration,
                                  boolean upToDate, boolean futureMarker, String detail) {
        public MigrationMarker {
            detail = detail == null || detail.isBlank() ? "unknown" : detail;
        }

        public String summary() {
            return "detail=" + detail
                    + " hasMarker=" + hasLegacyMarker
                    + " previousBuild=" + previousBuild
                    + " currentBuild=" + currentBuild
                    + " currentKnown=" + currentBuildKnown
                    + " needsMigration=" + needsMigration
                    + " futureMarker=" + futureMarker
                    + " upToDate=" + upToDate;
        }
    }
}
