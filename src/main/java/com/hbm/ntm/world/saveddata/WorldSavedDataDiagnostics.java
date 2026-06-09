package com.hbm.ntm.world.saveddata;

import com.hbm.ntm.satellite.SatelliteSavedData;
import com.hbm.ntm.world.BlockMigrationHelper;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public final class WorldSavedDataDiagnostics {
    public static LevelStatus inspect(ServerLevel level) {
        Optional<TomImpactSavedData> tom = TomImpactSavedData.getExisting(level);
        Optional<AnnihilatorSavedData> annihilator = AnnihilatorSavedData.getExisting(level);
        Optional<SatelliteSavedData> satellites = SatelliteSavedData.getExisting(level);
        return new LevelStatus(
                level.dimension().location(),
                tom.isPresent(),
                tom.map(TomImpactSavedData::snapshot).orElse(TomImpactSavedData.Snapshot.EMPTY),
                annihilator.isPresent(),
                annihilator.map(AnnihilatorSavedData::poolCount).orElse(0),
                annihilator.map(AnnihilatorSavedData::poolEntryCount).orElse(0),
                annihilator.map(AnnihilatorSavedData::totalAmount).orElse(BigInteger.ZERO),
                satellites.isPresent(),
                satellites.map(SatelliteSavedData::size).orElse(0));
    }

    public static ServerStatus inspect(MinecraftServer server) {
        List<LevelStatus> levels = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            levels.add(inspect(level));
        }
        return new ServerStatus(levels, BlockMigrationHelper.diagnostics());
    }

    public static Optional<LevelStatus> inspect(MinecraftServer server, ResourceKey<Level> dimension) {
        ServerLevel level = server.getLevel(dimension);
        return level == null ? Optional.empty() : Optional.of(inspect(level));
    }

    public static List<KnownDataStatus> knownData(ServerLevel level) {
        ResourceLocation dimension = level.dimension().location();
        return List.of(
                knownData(level, dimension, TomImpactSavedData.DATA_NAME, "tom_impact",
                        TomImpactSavedData::load,
                        data -> {
                            TomImpactSavedData.Snapshot snapshot = data.snapshot();
                            return "dust=" + snapshot.dust()
                                    + " fire=" + snapshot.fire()
                                    + " impact=" + snapshot.impact()
                                    + " climate=" + snapshot.hasClimate();
                        }),
                knownData(level, dimension, AnnihilatorSavedData.DATA_NAME, "annihilator",
                        AnnihilatorSavedData::load,
                        data -> "pools=" + data.poolCount()
                                + " entries=" + data.poolEntryCount()
                                + " total=" + data.totalAmount()),
                knownData(level, dimension, SatelliteSavedData.DATA_NAME, "satellite",
                        SatelliteSavedData::load,
                        data -> "entries=" + data.size()));
    }

    public static Optional<List<KnownDataStatus>> knownData(MinecraftServer server, ResourceKey<Level> dimension) {
        ServerLevel level = server.getLevel(dimension);
        return level == null ? Optional.empty() : Optional.of(knownData(level));
    }

    public static ServerKnownDataStatus knownData(MinecraftServer server) {
        List<KnownDataStatus> entries = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            entries.addAll(knownData(level));
        }
        return new ServerKnownDataStatus(entries);
    }

    private static <T extends SavedData> KnownDataStatus knownData(ServerLevel level, ResourceLocation dimension,
                                                                  String name, String type,
                                                                  Function<CompoundTag, T> loader,
                                                                  Function<T, String> summary) {
        Optional<WorldSavedDataHelper.ExistingDataLookup<T>> lookup =
                WorldSavedDataHelper.findExistingWithFallback(level, name, loader);
        if (lookup.isEmpty()) {
            return KnownDataStatus.absent(dimension, name, type);
        }
        WorldSavedDataHelper.ExistingDataLookup<T> result = lookup.get();
        return new KnownDataStatus(dimension, name, type, true, result.foundName(), result.primary(),
                summary.apply(result.data()));
    }

    private WorldSavedDataDiagnostics() {
    }

    public record LevelStatus(ResourceLocation dimension, boolean hasTomImpact,
                              TomImpactSavedData.Snapshot tomImpact,
                              boolean hasAnnihilator, int annihilatorPools,
                              int annihilatorEntries, BigInteger annihilatorTotalAmount,
                              boolean hasSatellites, int satelliteCount) {
        public int presentDataCount() {
            return (hasTomImpact ? 1 : 0) + (hasAnnihilator ? 1 : 0) + (hasSatellites ? 1 : 0);
        }

        public boolean hasAnyData() {
            return presentDataCount() > 0;
        }

        public String summary() {
            return "dimension=" + dimension
                    + " impactData=" + (hasTomImpact ? "present" : "absent")
                    + " annihilator=" + (hasAnnihilator ? "present" : "absent")
                    + " satellites=" + (hasSatellites ? "present" : "absent");
        }
    }

    public record ServerStatus(List<LevelStatus> levels,
                               BlockMigrationHelper.MigrationDiagnostics migrations) {
        public int presentDataCount() {
            return levels.stream().mapToInt(LevelStatus::presentDataCount).sum();
        }

        public long levelsWithDataCount() {
            return levels.stream().filter(level -> level.presentDataCount() > 0).count();
        }
    }

    public record KnownDataStatus(ResourceLocation dimension, String name, String type, boolean present,
                                  String foundName, boolean primary, String detail) {
        public KnownDataStatus {
            foundName = foundName == null ? "" : foundName;
            detail = detail == null ? "" : detail;
        }

        public static KnownDataStatus absent(ResourceLocation dimension, String name, String type) {
            return new KnownDataStatus(dimension, name, type, false, "", false, "");
        }

        public String summary() {
            return "dimension=" + dimension
                    + " name=" + name
                    + " type=" + type
                    + " present=" + present
                    + (present ? " found=" + foundName + " primary=" + primary + " " + detail : "");
        }
    }

    public record ServerKnownDataStatus(List<KnownDataStatus> entries) {
        public long presentCount() {
            return entries.stream().filter(KnownDataStatus::present).count();
        }

        public long dimensionCount() {
            return entries.stream().map(KnownDataStatus::dimension).distinct().count();
        }
    }
}
