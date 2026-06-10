package com.hbm.ntm.world.saveddata;

import com.hbm.ntm.pollution.PollutionSavedData;
import com.hbm.ntm.radiation.CraterRadiationData;
import com.hbm.ntm.radiation.RadiationSavedData;
import com.hbm.ntm.satellite.LegacySatelliteType;
import com.hbm.ntm.satellite.SatelliteSavedData;
import com.hbm.ntm.world.BlockMigrationHelper;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                tom.map(TomImpactSummary::of).orElseGet(TomImpactSummary::absent),
                annihilator.isPresent(),
                annihilator.map(AnnihilatorSavedData::poolCount).orElse(0),
                annihilator.map(AnnihilatorSavedData::poolEntryCount).orElse(0),
                annihilator.map(AnnihilatorSavedData::totalAmount).orElse(BigInteger.ZERO),
                satellites.isPresent(),
                satellites.map(SatelliteSavedData::size).orElse(0),
                annihilator.map(AnnihilatorSummary::of).orElseGet(AnnihilatorSummary::absent),
                satellites.map(SatelliteSummary::of).orElseGet(SatelliteSummary::absent));
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
                        data -> TomImpactSummary.of(data).detail()),
                knownData(level, dimension, AnnihilatorSavedData.DATA_NAME, "annihilator",
                        AnnihilatorSavedData::load,
                        data -> AnnihilatorSummary.of(data).detail()),
                knownData(level, dimension, SatelliteSavedData.DATA_NAME, "satellite",
                        SatelliteSavedData::load,
                        data -> SatelliteSummary.of(data).detail()),
                knownData(level, dimension, PollutionSavedData.DATA_NAME, "pollution",
                        PollutionSavedData::load,
                        WorldSavedDataDiagnostics::pollutionDetail,
                        PollutionSavedData.MODERN_COMPAT_DATA_NAME),
                knownData(level, dimension, RadiationSavedData.DATA_NAME, "chunk_radiation",
                        RadiationSavedData::load,
                        data -> radiationDetail(level, data)),
                knownData(level, dimension, CraterRadiationData.DATA_NAME, "crater_radiation",
                        CraterRadiationData::load,
                        data -> craterRadiationDetail(level, data)));
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

    public static LevelHealthStatus health(ServerLevel level) {
        return health(inspect(level), level);
    }

    public static Optional<LevelHealthStatus> health(MinecraftServer server, ResourceKey<Level> dimension) {
        ServerLevel level = server.getLevel(dimension);
        return level == null ? Optional.empty() : Optional.of(health(level));
    }

    public static ServerHealthStatus health(MinecraftServer server) {
        List<LevelHealthStatus> levels = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            levels.add(health(level));
        }
        return new ServerHealthStatus(levels);
    }

    private static LevelHealthStatus health(LevelStatus status, ServerLevel level) {
        int tomProblems = status.hasTomImpact() ? status.tomImpactSummary().loadDiagnostics().problemCount() : 0;
        int annihilatorProblems = status.hasAnnihilator()
                ? status.annihilatorSummary().loadDiagnostics().problemCount() : 0;
        int satelliteProblems = status.hasSatellites()
                ? status.satelliteSummary().loadDiagnostics().problemCount() : 0;
        Optional<WorldSavedDataHelper.ExistingDataLookup<PollutionSavedData>> pollution =
                WorldSavedDataHelper.findExistingWithFallback(level, PollutionSavedData.DATA_NAME,
                        PollutionSavedData::load, PollutionSavedData.MODERN_COMPAT_DATA_NAME);
        Optional<RadiationSavedData> radiation = WorldSavedDataHelper.getExisting(level,
                RadiationSavedData.DATA_NAME, RadiationSavedData::load);
        Optional<CraterRadiationData> crater = CraterRadiationData.getExisting(level);
        int pollutionProblems = pollution.map(result -> result.data().loadDiagnostics().problemCount()).orElse(0);
        int radiationProblems = radiation.map(data -> data.loadDiagnostics().problemCount()).orElse(0);
        int craterProblems = crater.map(data -> data.loadDiagnostics().problemCount()).orElse(0);
        List<String> issues = new ArrayList<>();
        if (status.hasTomImpact()) {
            status.tomImpactSummary().loadDiagnostics().issues()
                    .forEach(issue -> issues.add("impactData:" + issue));
        }
        if (status.hasAnnihilator()) {
            status.annihilatorSummary().loadDiagnostics().issues()
                    .forEach(issue -> issues.add("annihilator:" + issue));
        }
        if (status.hasSatellites()) {
            status.satelliteSummary().loadDiagnostics().issues()
                    .forEach(issue -> issues.add("satellites:" + issue));
        }
        pollution.ifPresent(result -> result.data().loadDiagnostics().issues()
                .forEach(issue -> issues.add("hbmpollution:" + issue)));
        radiation.ifPresent(data -> data.loadDiagnostics().issues()
                .forEach(issue -> issues.add("hbm_chunk_radiation:" + issue)));
        crater.ifPresent(data -> data.loadDiagnostics().issues()
                .forEach(issue -> issues.add("hbm_crater_radiation:" + issue)));
        return new LevelHealthStatus(status.dimension(), status.hasTomImpact(), tomProblems,
                status.hasAnnihilator(), annihilatorProblems, status.hasSatellites(), satelliteProblems,
                pollution.isPresent(), pollutionProblems, radiation.isPresent(), radiationProblems,
                crater.isPresent(), craterProblems, issues);
    }

    private static <T extends SavedData> KnownDataStatus knownData(ServerLevel level, ResourceLocation dimension,
                                                                  String name, String type,
                                                                  Function<CompoundTag, T> loader,
                                                                  Function<T, String> summary,
                                                                  String... fallbackNames) {
        Optional<WorldSavedDataHelper.ExistingDataLookup<T>> lookup =
                WorldSavedDataHelper.findExistingWithFallback(level, name, loader, fallbackNames);
        if (lookup.isEmpty()) {
            return KnownDataStatus.absent(dimension, name, type);
        }
        WorldSavedDataHelper.ExistingDataLookup<T> result = lookup.get();
        return new KnownDataStatus(dimension, name, type, true, result.foundName(), result.primary(),
                summary.apply(result.data()));
    }

    private static String pollutionDetail(PollutionSavedData data) {
        PollutionSavedData.Stats stats = data.stats(null);
        return "entries=" + stats.totalEntries()
                + " positive=" + stats.positiveEntries()
                + " stored=" + stats.storedEntries()
                + " total=" + stats.totalPollution()
                + " max=" + stats.maxPollution()
                + " totals=" + stats.formatTotals()
                + " load={" + data.loadDiagnostics().summary() + "}";
    }

    private static String radiationDetail(ServerLevel level, RadiationSavedData data) {
        RadiationSavedData.Stats stats = data.stats(level);
        return "entries=" + stats.totalEntries()
                + " loaded=" + stats.loadedEntries()
                + " positive=" + stats.positiveEntries()
                + " loadedPositive=" + stats.loadedPositiveEntries()
                + " total=" + stats.totalRadiation()
                + " loadedTotal=" + stats.loadedRadiation()
                + " max=" + stats.maxRadiation()
                + " loadedMax=" + stats.loadedMaxRadiation()
                + " load={" + data.loadDiagnostics().summary() + "}";
    }

    private static String craterRadiationDetail(ServerLevel level, CraterRadiationData data) {
        CraterRadiationData.Stats stats = data.statsSnapshot(level);
        return "markers=" + stats.totalMarkers()
                + " loaded=" + stats.loadedMarkers()
                + " outer=" + stats.outerMarkers()
                + " crater=" + stats.craterMarkers()
                + " inner=" + stats.innerMarkers()
                + " loadedOuter=" + stats.loadedOuterMarkers()
                + " loadedCrater=" + stats.loadedCraterMarkers()
                + " loadedInner=" + stats.loadedInnerMarkers()
                + " load={" + data.loadDiagnostics().summary() + "}";
    }

    private WorldSavedDataDiagnostics() {
    }

    public record LevelStatus(ResourceLocation dimension, boolean hasTomImpact,
                              TomImpactSavedData.Snapshot tomImpact,
                              TomImpactSummary tomImpactSummary,
                              boolean hasAnnihilator, int annihilatorPools,
                              int annihilatorEntries, BigInteger annihilatorTotalAmount,
                              boolean hasSatellites, int satelliteCount,
                              AnnihilatorSummary annihilatorSummary,
                              SatelliteSummary satelliteSummary) {
        public LevelStatus {
            tomImpactSummary = tomImpactSummary == null ? TomImpactSummary.absent() : tomImpactSummary;
            annihilatorSummary = annihilatorSummary == null ? AnnihilatorSummary.absent() : annihilatorSummary;
            satelliteSummary = satelliteSummary == null ? SatelliteSummary.absent() : satelliteSummary;
        }

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

    public record TomImpactSummary(boolean present, TomImpactSavedData.Snapshot snapshot,
                                   TomImpactSavedData.LoadDiagnostics loadDiagnostics) {
        public TomImpactSummary {
            snapshot = snapshot == null ? TomImpactSavedData.Snapshot.EMPTY : snapshot;
            loadDiagnostics = loadDiagnostics == null ? TomImpactSavedData.LoadDiagnostics.empty()
                    : loadDiagnostics;
        }

        public static TomImpactSummary absent() {
            return new TomImpactSummary(false, TomImpactSavedData.Snapshot.EMPTY,
                    TomImpactSavedData.LoadDiagnostics.empty());
        }

        public static TomImpactSummary of(TomImpactSavedData data) {
            return new TomImpactSummary(true, data.snapshot(), data.loadDiagnostics());
        }

        public String detail() {
            return snapshot.summary()
                    + " load={" + loadDiagnostics.summary() + "}";
        }
    }

    public record AnnihilatorSummary(boolean present, int pools, int entries, BigInteger totalAmount,
                                     Map<AnnihilatorSavedData.Kind, Integer> keyKindCounts,
                                     Map<AnnihilatorSavedData.Kind, BigInteger> keyKindTotals,
                                     List<AnnihilatorSavedData.PoolSummary> topPools,
                                     AnnihilatorSavedData.LoadDiagnostics loadDiagnostics) {
        public AnnihilatorSummary {
            totalAmount = totalAmount == null ? BigInteger.ZERO : totalAmount;
            keyKindCounts = keyKindCounts == null ? Map.of() : Map.copyOf(keyKindCounts);
            keyKindTotals = keyKindTotals == null ? Map.of() : Map.copyOf(keyKindTotals);
            topPools = topPools == null ? List.of() : List.copyOf(topPools);
            loadDiagnostics = loadDiagnostics == null ? AnnihilatorSavedData.LoadDiagnostics.empty()
                    : loadDiagnostics;
        }

        public static AnnihilatorSummary absent() {
            return new AnnihilatorSummary(false, 0, 0, BigInteger.ZERO, Map.of(), Map.of(), List.of(),
                    AnnihilatorSavedData.LoadDiagnostics.empty());
        }

        public static AnnihilatorSummary of(AnnihilatorSavedData data) {
            return new AnnihilatorSummary(true, data.poolCount(), data.poolEntryCount(), data.totalAmount(),
                    data.keyKindCounts(), data.keyKindTotals(), data.topPoolSummariesSnapshot(8),
                    data.loadDiagnostics());
        }

        public String detail() {
            return "pools=" + pools
                    + " entries=" + entries
                    + " total=" + totalAmount
                    + " keys=" + keyKindCounts
                    + " kindTotals=" + keyKindTotals
                    + " load={" + loadDiagnostics.summary() + "}";
        }
    }

    public record SatelliteSummary(boolean present, int entries,
                                   Map<LegacySatelliteType, Integer> typeCounts,
                                   List<Integer> frequencies,
                                   List<SatelliteSavedData.SatelliteSummary> satellites,
                                   SatelliteSavedData.LoadDiagnostics loadDiagnostics) {
        public SatelliteSummary {
            typeCounts = typeCounts == null ? Map.of() : Map.copyOf(typeCounts);
            frequencies = frequencies == null ? List.of() : List.copyOf(frequencies);
            satellites = satellites == null ? List.of() : List.copyOf(satellites);
            loadDiagnostics = loadDiagnostics == null ? SatelliteSavedData.LoadDiagnostics.empty()
                    : loadDiagnostics;
        }

        public static SatelliteSummary absent() {
            return new SatelliteSummary(false, 0, Map.of(), List.of(), List.of(),
                    SatelliteSavedData.LoadDiagnostics.empty());
        }

        public static SatelliteSummary of(SatelliteSavedData data) {
            return new SatelliteSummary(true, data.size(), data.typeCounts(),
                    data.frequenciesSnapshot(16), data.satelliteSummariesSnapshot(8),
                    data.loadDiagnostics());
        }

        public String detail() {
            return "entries=" + entries
                    + " types=" + typeCounts
                    + " frequencies=" + frequencies
                    + " load={" + loadDiagnostics.summary() + "}";
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

        public long tomClimateDimensions() {
            return levels.stream()
                    .filter(level -> level.hasTomImpact() && level.tomImpact().hasClimate())
                    .count();
        }

        public int totalAnnihilatorPools() {
            return levels.stream().mapToInt(LevelStatus::annihilatorPools).sum();
        }

        public int totalAnnihilatorEntries() {
            return levels.stream().mapToInt(LevelStatus::annihilatorEntries).sum();
        }

        public BigInteger totalAnnihilatorAmount() {
            return levels.stream()
                    .map(LevelStatus::annihilatorTotalAmount)
                    .reduce(BigInteger.ZERO, BigInteger::add);
        }

        public int totalSatelliteCount() {
            return levels.stream().mapToInt(LevelStatus::satelliteCount).sum();
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

        public boolean fallback() {
            return present && !primary;
        }

        public String source() {
            if (!present) {
                return "absent";
            }
            return primary ? "primary" : "fallback";
        }

        public String summary() {
            return "dimension=" + dimension
                    + " name=" + name
                    + " type=" + type
                    + " present=" + present
                    + " source=" + source()
                    + (present ? " found=" + foundName + " primary=" + primary + " " + detail : "");
        }
    }

    public record ServerKnownDataStatus(List<KnownDataStatus> entries) {
        public ServerKnownDataStatus {
            entries = entries == null ? List.of() : List.copyOf(entries);
        }

        public long presentCount() {
            return entries.stream().filter(KnownDataStatus::present).count();
        }

        public long primaryCount() {
            return entries.stream().filter(entry -> entry.present() && entry.primary()).count();
        }

        public long fallbackCount() {
            return entries.stream().filter(KnownDataStatus::fallback).count();
        }

        public long absentCount() {
            return entries.stream().filter(entry -> !entry.present()).count();
        }

        public long dimensionCount() {
            return entries.stream().map(KnownDataStatus::dimension).distinct().count();
        }
    }

    public record LevelHealthStatus(ResourceLocation dimension,
                                    boolean hasTomImpact, int tomProblems,
                                    boolean hasAnnihilator, int annihilatorProblems,
                                    boolean hasSatellites, int satelliteProblems,
                                    boolean hasPollution, int pollutionProblems,
                                    boolean hasChunkRadiation, int chunkRadiationProblems,
                                    boolean hasCraterRadiation, int craterRadiationProblems,
                                    List<String> issues) {
        public LevelHealthStatus {
            issues = issues == null ? List.of() : List.copyOf(issues);
        }

        public int totalProblems() {
            return tomProblems + annihilatorProblems + satelliteProblems + pollutionProblems
                    + chunkRadiationProblems + craterRadiationProblems;
        }

        public boolean clean() {
            return totalProblems() == 0;
        }

        public String summary() {
            return "dimension=" + dimension
                    + " present={impactData=" + hasTomImpact
                    + ", annihilator=" + hasAnnihilator
                    + ", satellites=" + hasSatellites
                    + ", hbmpollution=" + hasPollution
                    + ", hbm_chunk_radiation=" + hasChunkRadiation
                    + ", hbm_crater_radiation=" + hasCraterRadiation + "}"
                    + " problems={impactData=" + tomProblems
                    + ", annihilator=" + annihilatorProblems
                    + ", satellites=" + satelliteProblems
                    + ", hbmpollution=" + pollutionProblems
                    + ", hbm_chunk_radiation=" + chunkRadiationProblems
                    + ", hbm_crater_radiation=" + craterRadiationProblems + "}"
                    + " totalProblems=" + totalProblems()
                    + " issues=" + issues
                    + " clean=" + clean();
        }
    }

    public record ServerHealthStatus(List<LevelHealthStatus> levels) {
        public ServerHealthStatus {
            levels = levels == null ? List.of() : List.copyOf(levels);
        }

        public int totalProblems() {
            return levels.stream().mapToInt(LevelHealthStatus::totalProblems).sum();
        }

        public long cleanDimensions() {
            return levels.stream().filter(LevelHealthStatus::clean).count();
        }

        public long problemDimensions() {
            return levels.stream().filter(level -> !level.clean()).count();
        }
    }
}
