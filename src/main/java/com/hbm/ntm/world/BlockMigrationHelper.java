package com.hbm.ntm.world;

import com.hbm.ntm.HbmNtm;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.fml.ModList;

import java.util.Optional;

public final class BlockMigrationHelper {
    public static final String NBT_KEY_BUILD_NUMBER = "hfr_migrations_version";
    private static final int UNKNOWN_BUILD_NUMBER = -1;
    private static int cachedBuildNumber = UNKNOWN_BUILD_NUMBER;

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
            return MigrationResult.freshChunk();
        }
        int previousBuild = tag.getInt(NBT_KEY_BUILD_NUMBER);
        int currentBuild = buildNumber();
        if (currentBuild == UNKNOWN_BUILD_NUMBER || previousBuild == currentBuild) {
            return MigrationResult.upToDate(previousBuild, currentBuild);
        }
        doMigration(chunk, previousBuild, currentBuild);
        return MigrationResult.migrated(previousBuild, currentBuild);
    }

    public static void save(CompoundTag tag) {
        tag.putInt(NBT_KEY_BUILD_NUMBER, buildNumber());
    }

    public static void doMigration(ChunkAccess chunk, int previousBuild, int currentBuild) {
        // Legacy BlockMigrations had the chunk scan hook but no concrete replacement rules.
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
}
