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
    public static List<KnownDataDefinition> knownDataDefinitions() {
        return List.of(
                new KnownDataDefinition(TomImpactSavedData.DATA_NAME, "tom_impact", List.of(), true, false),
                new KnownDataDefinition(AnnihilatorSavedData.DATA_NAME, "annihilator", List.of(), true, false),
                new KnownDataDefinition(SatelliteSavedData.DATA_NAME, "satellite", List.of(), true, false),
                new KnownDataDefinition(PollutionSavedData.DATA_NAME, "pollution",
                        List.of(PollutionSavedData.MODERN_COMPAT_DATA_NAME), true, true),
                new KnownDataDefinition(RadiationSavedData.DATA_NAME, "chunk_radiation", List.of(), true, false),
                new KnownDataDefinition(CraterRadiationData.DATA_NAME, "crater_radiation", List.of(), true, false));
    }

    public static LevelStatus inspect(ServerLevel level) {
        Optional<TomImpactSavedData> tom = TomImpactSavedData.getExisting(level);
        Optional<AnnihilatorSavedData> annihilator = AnnihilatorSavedData.getExisting(level);
        Optional<SatelliteSavedData> satellites = SatelliteSavedData.getExisting(level);
        Optional<WorldSavedDataHelper.ExistingDataLookup<PollutionSavedData>> pollution =
                WorldSavedDataHelper.findExistingWithFallback(level, PollutionSavedData.DATA_NAME,
                        PollutionSavedData::load, PollutionSavedData.MODERN_COMPAT_DATA_NAME);
        Optional<RadiationSavedData> radiation = WorldSavedDataHelper.getExisting(level,
                RadiationSavedData.DATA_NAME, RadiationSavedData::load);
        Optional<CraterRadiationData> crater = CraterRadiationData.getExisting(level);
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
                satellites.map(SatelliteSummary::of).orElseGet(SatelliteSummary::absent),
                pollution.isPresent(),
                pollution.map(PollutionSummary::of).orElseGet(PollutionSummary::absent),
                radiation.isPresent(),
                radiation.map(data -> ChunkRadiationSummary.of(level, data))
                        .orElseGet(ChunkRadiationSummary::absent),
                crater.isPresent(),
                crater.map(data -> CraterRadiationSummary.of(level, data))
                        .orElseGet(CraterRadiationSummary::absent));
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
        List<KnownDataStatus> entries = new ArrayList<>();
        for (KnownDataDescriptor<?> descriptor : knownDataDescriptors(level)) {
            entries.add(descriptor.knownData(level, dimension));
        }
        return entries;
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

    public static SavedDataPromotionResult promotePollutionFallback(ServerLevel level) {
        Optional<WorldSavedDataHelper.ExistingDataLookup<PollutionSavedData>> lookup =
                WorldSavedDataHelper.findExistingWithFallback(level, PollutionSavedData.DATA_NAME,
                        PollutionSavedData::load, PollutionSavedData.MODERN_COMPAT_DATA_NAME);
        if (lookup.isEmpty()) {
            return SavedDataPromotionResult.absent(level.dimension().location(), PollutionSavedData.DATA_NAME);
        }
        WorldSavedDataHelper.ExistingDataLookup<PollutionSavedData> result = lookup.get();
        boolean promoted = WorldSavedDataHelper.promoteLookup(level, PollutionSavedData.DATA_NAME, result);
        return SavedDataPromotionResult.of(level.dimension().location(), result, promoted);
    }

    public static ServerSavedDataPromotionResult promotePollutionFallback(MinecraftServer server) {
        List<SavedDataPromotionResult> levels = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            levels.add(promotePollutionFallback(level));
        }
        return new ServerSavedDataPromotionResult(levels);
    }

    public static List<SavedDataNormalizationResult> normalizeKnownData(ServerLevel level) {
        ResourceLocation dimension = level.dimension().location();
        List<SavedDataNormalizationResult> entries = new ArrayList<>();
        for (KnownDataDescriptor<?> descriptor : knownDataDescriptors(level)) {
            entries.add(descriptor.normalize(level, dimension));
        }
        return entries;
    }

    public static ServerSavedDataNormalizationResult normalizeKnownData(MinecraftServer server) {
        List<SavedDataNormalizationResult> entries = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            entries.addAll(normalizeKnownData(level));
        }
        return new ServerSavedDataNormalizationResult(entries);
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
        int annihilatorProblemPools = status.hasAnnihilator()
                ? status.annihilatorSummary().problemPools() : 0;
        int satelliteProblems = status.hasSatellites()
                ? status.satelliteSummary().loadDiagnostics().problemCount() : 0;
        int satelliteProblemEntries = status.hasSatellites()
                ? status.satelliteSummary().problemEntries() : 0;
        int pollutionProblems = status.hasPollution()
                ? status.pollutionSummary().loadDiagnostics().problemCount() : 0;
        int radiationProblems = status.hasChunkRadiation()
                ? status.chunkRadiationSummary().loadDiagnostics().problemCount() : 0;
        int craterProblems = status.hasCraterRadiation()
                ? status.craterRadiationSummary().loadDiagnostics().problemCount() : 0;
        List<String> issues = new ArrayList<>();
        if (status.hasTomImpact()) {
            status.tomImpactSummary().loadDiagnostics().issues()
                    .forEach(issue -> issues.add("impactData:" + issue));
        }
        if (status.hasAnnihilator()) {
            status.annihilatorSummary().loadDiagnostics().issues()
                    .forEach(issue -> issues.add("annihilator:" + issue));
            if (annihilatorProblemPools > 0) {
                issues.add("annihilator:problem_pools=" + annihilatorProblemPools);
            }
        }
        if (status.hasSatellites()) {
            status.satelliteSummary().loadDiagnostics().issues()
                    .forEach(issue -> issues.add("satellites:" + issue));
            if (satelliteProblemEntries > 0) {
                issues.add("satellites:problem_entries=" + satelliteProblemEntries);
            }
        }
        if (status.hasPollution()) {
            status.pollutionSummary().loadDiagnostics().issues()
                    .forEach(issue -> issues.add("hbmpollution:" + issue));
        }
        if (status.hasChunkRadiation()) {
            status.chunkRadiationSummary().loadDiagnostics().issues()
                    .forEach(issue -> issues.add("hbm_chunk_radiation:" + issue));
        }
        if (status.hasCraterRadiation()) {
            status.craterRadiationSummary().loadDiagnostics().issues()
                    .forEach(issue -> issues.add("hbm_crater_radiation:" + issue));
        }
        return new LevelHealthStatus(status.dimension(), status.hasTomImpact(), tomProblems,
                status.hasAnnihilator(), annihilatorProblems, annihilatorProblemPools,
                status.hasSatellites(), satelliteProblems, satelliteProblemEntries,
                status.hasPollution(), pollutionProblems, status.hasChunkRadiation(), radiationProblems,
                status.hasCraterRadiation(), craterProblems, issues);
    }

    private static List<KnownDataDescriptor<?>> knownDataDescriptors(ServerLevel level) {
        return List.of(
                new KnownDataDescriptor<>(TomImpactSavedData.DATA_NAME, "tom_impact", TomImpactSavedData::load,
                        data -> TomImpactSummary.of(data).detail(), List.of()),
                new KnownDataDescriptor<>(AnnihilatorSavedData.DATA_NAME, "annihilator",
                        AnnihilatorSavedData::load, data -> AnnihilatorSummary.of(data).detail(), List.of()),
                new KnownDataDescriptor<>(SatelliteSavedData.DATA_NAME, "satellite", SatelliteSavedData::load,
                        data -> SatelliteSummary.of(data).detail(), List.of()),
                new KnownDataDescriptor<>(PollutionSavedData.DATA_NAME, "pollution", PollutionSavedData::load,
                        WorldSavedDataDiagnostics::pollutionDetail,
                        List.of(PollutionSavedData.MODERN_COMPAT_DATA_NAME)),
                new KnownDataDescriptor<>(RadiationSavedData.DATA_NAME, "chunk_radiation",
                        RadiationSavedData::load, data -> radiationDetail(level, data), List.of()),
                new KnownDataDescriptor<>(CraterRadiationData.DATA_NAME, "crater_radiation",
                        CraterRadiationData::load, data -> craterRadiationDetail(level, data), List.of()));
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
                              SatelliteSummary satelliteSummary,
                              boolean hasPollution, PollutionSummary pollutionSummary,
                              boolean hasChunkRadiation, ChunkRadiationSummary chunkRadiationSummary,
                              boolean hasCraterRadiation, CraterRadiationSummary craterRadiationSummary) {
        public LevelStatus {
            tomImpactSummary = tomImpactSummary == null ? TomImpactSummary.absent() : tomImpactSummary;
            annihilatorSummary = annihilatorSummary == null ? AnnihilatorSummary.absent() : annihilatorSummary;
            satelliteSummary = satelliteSummary == null ? SatelliteSummary.absent() : satelliteSummary;
            pollutionSummary = pollutionSummary == null ? PollutionSummary.absent() : pollutionSummary;
            chunkRadiationSummary = chunkRadiationSummary == null ? ChunkRadiationSummary.absent()
                    : chunkRadiationSummary;
            craterRadiationSummary = craterRadiationSummary == null ? CraterRadiationSummary.absent()
                    : craterRadiationSummary;
        }

        public int presentDataCount() {
            return (hasTomImpact ? 1 : 0)
                    + (hasAnnihilator ? 1 : 0)
                    + (hasSatellites ? 1 : 0)
                    + (hasPollution ? 1 : 0)
                    + (hasChunkRadiation ? 1 : 0)
                    + (hasCraterRadiation ? 1 : 0);
        }

        public boolean hasAnyData() {
            return presentDataCount() > 0;
        }

        public String summary() {
            return "dimension=" + dimension
                    + " impactData=" + (hasTomImpact ? "present" : "absent")
                    + " annihilator=" + (hasAnnihilator ? "present" : "absent")
                    + " satellites=" + (hasSatellites ? "present" : "absent")
                    + " hbmpollution=" + pollutionSummary.source()
                    + " hbm_chunk_radiation=" + (hasChunkRadiation ? "present" : "absent")
                    + " hbm_crater_radiation=" + (hasCraterRadiation ? "present" : "absent");
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
                                     int problemPools,
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
                    0, AnnihilatorSavedData.LoadDiagnostics.empty());
        }

        public static AnnihilatorSummary of(AnnihilatorSavedData data) {
            return new AnnihilatorSummary(true, data.poolCount(), data.poolEntryCount(), data.totalAmount(),
                    data.keyKindCounts(), data.keyKindTotals(), data.topPoolSummariesSnapshot(8),
                    data.problemPoolLoadDiagnosticsSnapshot().size(), data.loadDiagnostics());
        }

        public String detail() {
            return "pools=" + pools
                    + " entries=" + entries
                    + " total=" + totalAmount
                    + " keys=" + keyKindCounts
                    + " kindTotals=" + keyKindTotals
                    + " problemPools=" + problemPools
                    + " load={" + loadDiagnostics.summary() + "}";
        }
    }

    public record SatelliteSummary(boolean present, int entries,
                                   Map<LegacySatelliteType, Integer> typeCounts,
                                   Map<LegacySatelliteType, Integer> cargoTypeCounts,
                                   Map<String, Integer> cargoPoolCounts,
                                   List<Integer> frequencies,
                                   List<Integer> cargoFrequencies,
                                   List<SatelliteSavedData.SatelliteSummary> satellites,
                                   int legacyEntryDiagnostics,
                                   int modernEntryDiagnostics,
                                   int problemEntries,
                                   SatelliteSavedData.LoadDiagnostics loadDiagnostics,
                                   SatelliteSavedData.SatelliteStats stats) {
        public SatelliteSummary {
            typeCounts = typeCounts == null ? Map.of() : Map.copyOf(typeCounts);
            cargoTypeCounts = cargoTypeCounts == null ? Map.of() : Map.copyOf(cargoTypeCounts);
            cargoPoolCounts = cargoPoolCounts == null ? Map.of() : Map.copyOf(cargoPoolCounts);
            frequencies = frequencies == null ? List.of() : List.copyOf(frequencies);
            cargoFrequencies = cargoFrequencies == null ? List.of() : List.copyOf(cargoFrequencies);
            satellites = satellites == null ? List.of() : List.copyOf(satellites);
            loadDiagnostics = loadDiagnostics == null ? SatelliteSavedData.LoadDiagnostics.empty()
                    : loadDiagnostics;
            stats = stats == null ? SatelliteSavedData.SatelliteStats.empty() : stats;
        }

        public static SatelliteSummary absent() {
            return new SatelliteSummary(false, 0, Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(),
                    0, 0, 0, SatelliteSavedData.LoadDiagnostics.empty(), SatelliteSavedData.SatelliteStats.empty());
        }

        public static SatelliteSummary of(SatelliteSavedData data) {
            SatelliteSavedData.SatelliteStats stats = data.statsSnapshot(16, 8);
            return new SatelliteSummary(true, stats.entries(), stats.typeCounts(), stats.cargoTypeCounts(),
                    stats.cargoPoolCounts(), stats.frequencies(), stats.cargoFrequencies(), stats.satellites(),
                    stats.legacyEntryDiagnostics(), stats.modernEntryDiagnostics(), stats.problemEntries(),
                    stats.loadDiagnostics(), stats);
        }

        public String detail() {
            return stats.detail();
        }
    }

    public record PollutionSummary(boolean present, String foundName, boolean primary,
                                   int entries, int positiveEntries, int storedEntries,
                                   float totalPollution, float maxPollution, String totals,
                                   PollutionSavedData.LoadDiagnostics loadDiagnostics) {
        public PollutionSummary {
            foundName = foundName == null ? "" : foundName;
            totals = totals == null ? "" : totals;
            loadDiagnostics = loadDiagnostics == null ? PollutionSavedData.LoadDiagnostics.empty()
                    : loadDiagnostics;
        }

        public static PollutionSummary absent() {
            return new PollutionSummary(false, "", false, 0, 0, 0, 0.0F, 0.0F, "",
                    PollutionSavedData.LoadDiagnostics.empty());
        }

        public static PollutionSummary of(WorldSavedDataHelper.ExistingDataLookup<PollutionSavedData> lookup) {
            PollutionSavedData data = lookup.data();
            PollutionSavedData.Stats stats = data.stats(null);
            return new PollutionSummary(true, lookup.foundName(), lookup.primary(), stats.totalEntries(),
                    stats.positiveEntries(), stats.storedEntries(), stats.totalPollution(), stats.maxPollution(),
                    stats.formatTotals(), data.loadDiagnostics());
        }

        public String source() {
            if (!present) {
                return "absent";
            }
            return primary ? "primary" : "fallback";
        }

        public String detail() {
            return "source=" + source()
                    + " found=" + foundName
                    + " entries=" + entries
                    + " positive=" + positiveEntries
                    + " stored=" + storedEntries
                    + " total=" + totalPollution
                    + " max=" + maxPollution
                    + " totals=" + totals
                    + " load={" + loadDiagnostics.summary() + "}";
        }
    }

    public record ChunkRadiationSummary(boolean present, int entries, int loadedEntries,
                                        int positiveEntries, int loadedPositiveEntries,
                                        float totalRadiation, float loadedRadiation,
                                        float maxRadiation, float loadedMaxRadiation,
                                        RadiationSavedData.LoadDiagnostics loadDiagnostics) {
        public ChunkRadiationSummary {
            loadDiagnostics = loadDiagnostics == null ? RadiationSavedData.LoadDiagnostics.empty()
                    : loadDiagnostics;
        }

        public static ChunkRadiationSummary absent() {
            return new ChunkRadiationSummary(false, 0, 0, 0, 0, 0.0F, 0.0F, 0.0F, 0.0F,
                    RadiationSavedData.LoadDiagnostics.empty());
        }

        public static ChunkRadiationSummary of(ServerLevel level, RadiationSavedData data) {
            RadiationSavedData.Stats stats = data.stats(level);
            return new ChunkRadiationSummary(true, stats.totalEntries(), stats.loadedEntries(),
                    stats.positiveEntries(), stats.loadedPositiveEntries(), stats.totalRadiation(),
                    stats.loadedRadiation(), stats.maxRadiation(), stats.loadedMaxRadiation(),
                    data.loadDiagnostics());
        }

        public String detail() {
            return "entries=" + entries
                    + " loaded=" + loadedEntries
                    + " positive=" + positiveEntries
                    + " loadedPositive=" + loadedPositiveEntries
                    + " total=" + totalRadiation
                    + " loadedTotal=" + loadedRadiation
                    + " max=" + maxRadiation
                    + " loadedMax=" + loadedMaxRadiation
                    + " load={" + loadDiagnostics.summary() + "}";
        }
    }

    public record CraterRadiationSummary(boolean present, int markers, int loadedMarkers,
                                         int outerMarkers, int craterMarkers, int innerMarkers,
                                         int loadedOuterMarkers, int loadedCraterMarkers, int loadedInnerMarkers,
                                         CraterRadiationData.LoadDiagnostics loadDiagnostics) {
        public CraterRadiationSummary {
            loadDiagnostics = loadDiagnostics == null ? CraterRadiationData.LoadDiagnostics.empty()
                    : loadDiagnostics;
        }

        public static CraterRadiationSummary absent() {
            return new CraterRadiationSummary(false, 0, 0, 0, 0, 0, 0, 0, 0,
                    CraterRadiationData.LoadDiagnostics.empty());
        }

        public static CraterRadiationSummary of(ServerLevel level, CraterRadiationData data) {
            CraterRadiationData.Stats stats = data.statsSnapshot(level);
            return new CraterRadiationSummary(true, stats.totalMarkers(), stats.loadedMarkers(),
                    stats.outerMarkers(), stats.craterMarkers(), stats.innerMarkers(),
                    stats.loadedOuterMarkers(), stats.loadedCraterMarkers(), stats.loadedInnerMarkers(),
                    data.loadDiagnostics());
        }

        public String detail() {
            return "markers=" + markers
                    + " loaded=" + loadedMarkers
                    + " outer=" + outerMarkers
                    + " crater=" + craterMarkers
                    + " inner=" + innerMarkers
                    + " loadedOuter=" + loadedOuterMarkers
                    + " loadedCrater=" + loadedCraterMarkers
                    + " loadedInner=" + loadedInnerMarkers
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

        public int totalAnnihilatorProblemPools() {
            return levels.stream().mapToInt(level -> level.annihilatorSummary().problemPools()).sum();
        }

        public BigInteger totalAnnihilatorAmount() {
            return levels.stream()
                    .map(LevelStatus::annihilatorTotalAmount)
                    .reduce(BigInteger.ZERO, BigInteger::add);
        }

        public int totalSatelliteCount() {
            return levels.stream().mapToInt(LevelStatus::satelliteCount).sum();
        }

        public int totalSatelliteProblemEntries() {
            return levels.stream().mapToInt(level -> level.satelliteSummary().problemEntries()).sum();
        }

        public int totalPollutionEntries() {
            return levels.stream().mapToInt(level -> level.pollutionSummary().entries()).sum();
        }

        public int totalPositivePollutionEntries() {
            return levels.stream().mapToInt(level -> level.pollutionSummary().positiveEntries()).sum();
        }

        public float totalPollution() {
            return (float) levels.stream()
                    .mapToDouble(level -> level.pollutionSummary().totalPollution())
                    .sum();
        }

        public int totalChunkRadiationEntries() {
            return levels.stream().mapToInt(level -> level.chunkRadiationSummary().entries()).sum();
        }

        public float totalChunkRadiation() {
            return (float) levels.stream()
                    .mapToDouble(level -> level.chunkRadiationSummary().totalRadiation())
                    .sum();
        }

        public int totalCraterRadiationMarkers() {
            return levels.stream().mapToInt(level -> level.craterRadiationSummary().markers()).sum();
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

    public record KnownDataDefinition(String name, String type, List<String> fallbackNames,
                                      boolean normalizableWhenPresent, boolean explicitPromotionSupported) {
        public KnownDataDefinition {
            fallbackNames = fallbackNames == null ? List.of() : List.copyOf(fallbackNames);
        }

        public boolean hasFallbacks() {
            return !fallbackNames.isEmpty();
        }

        public String summary() {
            return "name=" + name
                    + " type=" + type
                    + " fallbacks=" + fallbackNames
                    + " normalizableWhenPresent=" + normalizableWhenPresent
                    + " explicitPromotionSupported=" + explicitPromotionSupported;
        }
    }

    private record KnownDataDescriptor<T extends SavedData>(String name, String type,
                                                            Function<CompoundTag, T> loader,
                                                            Function<T, String> detail,
                                                            List<String> fallbackNames) {
        private KnownDataDescriptor {
            fallbackNames = fallbackNames == null ? List.of() : List.copyOf(fallbackNames);
        }

        private KnownDataStatus knownData(ServerLevel level, ResourceLocation dimension) {
            Optional<WorldSavedDataHelper.ExistingDataLookup<T>> lookup =
                    WorldSavedDataHelper.findExistingWithFallback(level, name, loader, fallbackNamesArray());
            if (lookup.isEmpty()) {
                return KnownDataStatus.absent(dimension, name, type);
            }
            WorldSavedDataHelper.ExistingDataLookup<T> result = lookup.get();
            return new KnownDataStatus(dimension, name, type, true, result.foundName(), result.primary(),
                    detail.apply(result.data()));
        }

        private SavedDataNormalizationResult normalize(ServerLevel level, ResourceLocation dimension) {
            Optional<WorldSavedDataHelper.ExistingDataLookup<T>> lookup =
                    WorldSavedDataHelper.findExistingWithFallback(level, name, loader, fallbackNamesArray());
            if (lookup.isEmpty()) {
                return SavedDataNormalizationResult.absent(dimension, name, type);
            }
            WorldSavedDataHelper.ExistingDataLookup<T> result = lookup.get();
            boolean promoted = WorldSavedDataHelper.promoteLookup(level, name, result);
            result.data().setDirty();
            return SavedDataNormalizationResult.of(dimension, type, result, promoted);
        }

        private String[] fallbackNamesArray() {
            return fallbackNames.toArray(String[]::new);
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

    public record SavedDataPromotionResult(ResourceLocation dimension, String name, boolean present,
                                           String foundName, boolean primary, boolean promoted) {
        public SavedDataPromotionResult {
            foundName = foundName == null ? "" : foundName;
        }

        public static SavedDataPromotionResult absent(ResourceLocation dimension, String name) {
            return new SavedDataPromotionResult(dimension, name, false, "", false, false);
        }

        public static <T extends SavedData> SavedDataPromotionResult of(ResourceLocation dimension,
                                                                        WorldSavedDataHelper.ExistingDataLookup<T> lookup,
                                                                        boolean promoted) {
            return new SavedDataPromotionResult(dimension, lookup.requestedName(), true, lookup.foundName(),
                    lookup.primary(), promoted);
        }

        public boolean fallbackFound() {
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
                    + " present=" + present
                    + " source=" + source()
                    + (present ? " found=" + foundName + " primary=" + primary : "")
                    + " promoted=" + promoted;
        }
    }

    public record ServerSavedDataPromotionResult(List<SavedDataPromotionResult> levels) {
        public ServerSavedDataPromotionResult {
            levels = levels == null ? List.of() : List.copyOf(levels);
        }

        public long promotedCount() {
            return levels.stream().filter(SavedDataPromotionResult::promoted).count();
        }

        public long primaryCount() {
            return levels.stream().filter(result -> result.present() && result.primary()).count();
        }

        public long fallbackFoundCount() {
            return levels.stream().filter(SavedDataPromotionResult::fallbackFound).count();
        }

        public long absentCount() {
            return levels.stream().filter(result -> !result.present()).count();
        }
    }

    public record SavedDataNormalizationResult(ResourceLocation dimension, String name, String type, boolean present,
                                               String foundName, boolean primary, boolean promoted,
                                               boolean markedDirty) {
        public SavedDataNormalizationResult {
            foundName = foundName == null ? "" : foundName;
        }

        public static SavedDataNormalizationResult absent(ResourceLocation dimension, String name, String type) {
            return new SavedDataNormalizationResult(dimension, name, type, false, "", false, false, false);
        }

        public static <T extends SavedData> SavedDataNormalizationResult of(ResourceLocation dimension, String type,
                                                                            WorldSavedDataHelper.ExistingDataLookup<T> lookup,
                                                                            boolean promoted) {
            return new SavedDataNormalizationResult(dimension, lookup.requestedName(), type, true,
                    lookup.foundName(), lookup.primary(), promoted, true);
        }

        public boolean fallbackFound() {
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
                    + (present ? " found=" + foundName + " primary=" + primary : "")
                    + " promoted=" + promoted
                    + " markedDirty=" + markedDirty;
        }
    }

    public record ServerSavedDataNormalizationResult(List<SavedDataNormalizationResult> entries) {
        public ServerSavedDataNormalizationResult {
            entries = entries == null ? List.of() : List.copyOf(entries);
        }

        public long presentCount() {
            return entries.stream().filter(SavedDataNormalizationResult::present).count();
        }

        public long promotedCount() {
            return entries.stream().filter(SavedDataNormalizationResult::promoted).count();
        }

        public long markedDirtyCount() {
            return entries.stream().filter(SavedDataNormalizationResult::markedDirty).count();
        }

        public long fallbackFoundCount() {
            return entries.stream().filter(SavedDataNormalizationResult::fallbackFound).count();
        }

        public long absentCount() {
            return entries.stream().filter(result -> !result.present()).count();
        }

        public long dimensionCount() {
            return entries.stream().map(SavedDataNormalizationResult::dimension).distinct().count();
        }
    }

    public record LevelHealthStatus(ResourceLocation dimension,
                                    boolean hasTomImpact, int tomProblems,
                                    boolean hasAnnihilator, int annihilatorProblems, int annihilatorProblemPools,
                                    boolean hasSatellites, int satelliteProblems, int satelliteProblemEntries,
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

        public int totalDetailProblems() {
            return annihilatorProblemPools + satelliteProblemEntries;
        }

        public boolean clean() {
            return totalProblems() == 0 && totalDetailProblems() == 0;
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
                    + " detailProblems={annihilatorProblemPools=" + annihilatorProblemPools
                    + ", satelliteProblemEntries=" + satelliteProblemEntries + "}"
                    + " totalProblems=" + totalProblems()
                    + " totalDetailProblems=" + totalDetailProblems()
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

        public int totalDetailProblems() {
            return levels.stream().mapToInt(LevelHealthStatus::totalDetailProblems).sum();
        }

        public int tomProblems() {
            return levels.stream().mapToInt(LevelHealthStatus::tomProblems).sum();
        }

        public int annihilatorProblems() {
            return levels.stream().mapToInt(LevelHealthStatus::annihilatorProblems).sum();
        }

        public int annihilatorProblemPools() {
            return levels.stream().mapToInt(LevelHealthStatus::annihilatorProblemPools).sum();
        }

        public int satelliteProblems() {
            return levels.stream().mapToInt(LevelHealthStatus::satelliteProblems).sum();
        }

        public int satelliteProblemEntries() {
            return levels.stream().mapToInt(LevelHealthStatus::satelliteProblemEntries).sum();
        }

        public int pollutionProblems() {
            return levels.stream().mapToInt(LevelHealthStatus::pollutionProblems).sum();
        }

        public int chunkRadiationProblems() {
            return levels.stream().mapToInt(LevelHealthStatus::chunkRadiationProblems).sum();
        }

        public int craterRadiationProblems() {
            return levels.stream().mapToInt(LevelHealthStatus::craterRadiationProblems).sum();
        }

        public long cleanDimensions() {
            return levels.stream().filter(LevelHealthStatus::clean).count();
        }

        public long problemDimensions() {
            return levels.stream().filter(level -> !level.clean()).count();
        }
    }
}
