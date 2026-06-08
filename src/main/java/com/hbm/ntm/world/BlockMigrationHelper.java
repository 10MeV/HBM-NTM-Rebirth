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
        if (!tag.contains(NBT_KEY_BUILD_NUMBER)) {
            return remember(MigrationResult.freshChunk());
        }
        int previousBuild = tag.getInt(NBT_KEY_BUILD_NUMBER);
        int currentBuild = buildNumber();
        if (currentBuild == UNKNOWN_BUILD_NUMBER || previousBuild == currentBuild) {
            return remember(MigrationResult.upToDate(previousBuild, currentBuild));
        }
        doMigration(chunk, previousBuild, currentBuild);
        return remember(MigrationResult.migrated(previousBuild, currentBuild));
    }

    public static void save(CompoundTag tag) {
        tag.putInt(NBT_KEY_BUILD_NUMBER, buildNumber());
        SAVED_CHUNKS.incrementAndGet();
    }

    public static void doMigration(ChunkAccess chunk, int previousBuild, int currentBuild) {
        // Legacy BlockMigrations had the chunk scan hook but no concrete replacement rules.
    }

    public static MigrationDiagnostics diagnostics() {
        return new MigrationDiagnostics(
                buildNumber(),
                FRESH_CHUNKS.get(),
                UP_TO_DATE_CHUNKS.get(),
                MIGRATED_CHUNKS.get(),
                SAVED_CHUNKS.get(),
                lastLoadResult);
    }

    public static void resetDiagnostics() {
        FRESH_CHUNKS.set(0L);
        UP_TO_DATE_CHUNKS.set(0L);
        MIGRATED_CHUNKS.set(0L);
        SAVED_CHUNKS.set(0L);
        lastLoadResult = MigrationResult.unknown();
    }

    private static MigrationResult remember(MigrationResult result) {
        if (!result.hadLegacyMarker()) {
            FRESH_CHUNKS.incrementAndGet();
        } else if (result.migrated()) {
            MIGRATED_CHUNKS.incrementAndGet();
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

    public record MigrationResult(boolean hadLegacyMarker, boolean migrated, int previousBuild, int currentBuild) {
        static MigrationResult unknown() {
            return new MigrationResult(false, false, UNKNOWN_BUILD_NUMBER, UNKNOWN_BUILD_NUMBER);
        }

        static MigrationResult freshChunk() {
            return new MigrationResult(false, false, UNKNOWN_BUILD_NUMBER, buildNumber());
        }

        static MigrationResult upToDate(int previousBuild, int currentBuild) {
            return new MigrationResult(true, false, previousBuild, currentBuild);
        }

        static MigrationResult migrated(int previousBuild, int currentBuild) {
            return new MigrationResult(true, true, previousBuild, currentBuild);
        }
    }

    public record MigrationDiagnostics(int currentBuild, long freshChunks, long upToDateChunks, long migratedChunks,
                                       long savedChunks, MigrationResult lastLoadResult) {
        public long loadedChunks() {
            return freshChunks + upToDateChunks + migratedChunks;
        }
    }
}
