package com.hbm.ntm.satellite;

import com.hbm.ntm.world.saveddata.WorldSavedDataHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.nbt.Tag;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class SatelliteSavedData extends SavedData {
    public static final String DATA_NAME = "satellites";
    private static final String TAG_SAT_COUNT = "satCount";
    private static final String TAG_SAT_ID = "sat_id_";
    private static final String TAG_SAT_DATA = "sat_data_";
    private static final String TAG_SAT_FREQ = "sat_freq_";
    private static final String TAG_ENTRIES = "entries";
    private static final String TAG_FREQUENCY = "frequency";
    private static final String TAG_LEGACY_ID = "legacyId";
    private static final String TAG_LEGACY_NAME = "legacyName";
    private static final String TAG_DATA = "data";

    private final Map<Integer, Satellite> satellites = new HashMap<>();
    private LoadDiagnostics loadDiagnostics = LoadDiagnostics.empty();
    private List<EntryLoadDiagnostics> legacyEntryLoadDiagnostics = List.of();
    private List<EntryLoadDiagnostics> modernEntryLoadDiagnostics = List.of();

    public SatelliteSavedData() {
        setDirty();
    }

    public static SatelliteSavedData load(CompoundTag tag) {
        SatelliteSavedData data = new SatelliteSavedData();
        data.satellites.clear();
        boolean hasLegacyCountTag = tag.contains(TAG_SAT_COUNT, Tag.TAG_INT);
        int count = tag.getInt(TAG_SAT_COUNT);
        int legacyLoaded = 0;
        int legacyMissingIds = 0;
        int legacyMissingData = 0;
        int legacyMissingFrequencies = 0;
        int legacyUnknownIds = 0;
        int legacyDuplicateFrequencies = 0;
        List<EntryLoadDiagnostics> legacyEntryDiagnostics = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            boolean hasLegacyId = tag.contains(TAG_SAT_ID + i, Tag.TAG_INT);
            boolean hasData = tag.contains(TAG_SAT_DATA + i, Tag.TAG_COMPOUND);
            boolean hasFrequency = tag.contains(TAG_SAT_FREQ + i, Tag.TAG_INT);
            if (!hasLegacyId) {
                legacyMissingIds++;
            }
            if (!hasData) {
                legacyMissingData++;
            }
            if (!hasFrequency) {
                legacyMissingFrequencies++;
            }
            int legacyId = tag.getInt(TAG_SAT_ID + i);
            int frequency = tag.getInt(TAG_SAT_FREQ + i);
            Satellite satellite = Satellite.load(legacyId, tag.getCompound(TAG_SAT_DATA + i));
            boolean duplicateFrequency = satellite != null && data.satellites.containsKey(frequency);
            if (satellite != null) {
                if (duplicateFrequency) {
                    legacyDuplicateFrequencies++;
                }
                data.satellites.put(frequency, satellite);
                legacyLoaded++;
            } else {
                legacyUnknownIds++;
            }
            legacyEntryDiagnostics.add(new EntryLoadDiagnostics("legacy", i, hasLegacyId, hasData, hasFrequency,
                    legacyId, frequency, satellite != null, satellite == null, duplicateFrequency));
        }
        boolean usedModernEntriesFallback = false;
        boolean hasModernEntriesTag = tag.contains(TAG_ENTRIES, Tag.TAG_LIST);
        int modernEntriesRead = 0;
        int modernEntriesLoaded = 0;
        int modernMissingIds = 0;
        int modernMissingData = 0;
        int modernMissingFrequencies = 0;
        int modernUnknownIds = 0;
        int modernDuplicateFrequencies = 0;
        List<EntryLoadDiagnostics> modernEntryDiagnostics = new ArrayList<>();
        if (data.satellites.isEmpty() && hasModernEntriesTag) {
            usedModernEntriesFallback = true;
            ListTag entries = tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
            modernEntriesRead = entries.size();
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag entry = entries.getCompound(i);
                boolean hasLegacyId = entry.contains(TAG_LEGACY_ID, Tag.TAG_INT);
                boolean hasData = entry.contains(TAG_DATA, Tag.TAG_COMPOUND);
                boolean hasFrequency = entry.contains(TAG_FREQUENCY, Tag.TAG_INT);
                if (!hasLegacyId) {
                    modernMissingIds++;
                }
                if (!hasData) {
                    modernMissingData++;
                }
                if (!hasFrequency) {
                    modernMissingFrequencies++;
                }
                int legacyId = entry.getInt(TAG_LEGACY_ID);
                int frequency = entry.getInt(TAG_FREQUENCY);
                Satellite satellite = Satellite.load(legacyId, entry.getCompound(TAG_DATA));
                boolean duplicateFrequency = satellite != null && data.satellites.containsKey(frequency);
                if (satellite != null) {
                    if (duplicateFrequency) {
                        modernDuplicateFrequencies++;
                    }
                    data.satellites.put(frequency, satellite);
                    modernEntriesLoaded++;
                } else {
                    modernUnknownIds++;
                }
                modernEntryDiagnostics.add(new EntryLoadDiagnostics("modern", i, hasLegacyId, hasData,
                        hasFrequency, legacyId, frequency, satellite != null, satellite == null,
                        duplicateFrequency));
            }
        }
        data.loadDiagnostics = new LoadDiagnostics(hasLegacyCountTag, count, legacyLoaded, legacyMissingIds,
                legacyMissingData, legacyMissingFrequencies, legacyUnknownIds, legacyDuplicateFrequencies,
                hasModernEntriesTag, usedModernEntriesFallback, modernEntriesRead, modernEntriesLoaded,
                modernMissingIds, modernMissingData, modernMissingFrequencies, modernUnknownIds,
                modernDuplicateFrequencies);
        data.legacyEntryLoadDiagnostics = List.copyOf(legacyEntryDiagnostics);
        data.modernEntryLoadDiagnostics = List.copyOf(modernEntryDiagnostics);
        data.setDirty(false);
        return data;
    }

    public static SatelliteSavedData get(ServerLevel level) {
        return WorldSavedDataHelper.get(level, DATA_NAME, SatelliteSavedData::load, SatelliteSavedData::new);
    }

    public static Optional<SatelliteSavedData> get(Level level) {
        return WorldSavedDataHelper.get(level, DATA_NAME, SatelliteSavedData::load, SatelliteSavedData::new);
    }

    public static Optional<SatelliteSavedData> getExisting(ServerLevel level) {
        return WorldSavedDataHelper.getExisting(level, DATA_NAME, SatelliteSavedData::load);
    }

    public static Optional<SatelliteSavedData> getExisting(MinecraftServer server) {
        return WorldSavedDataHelper.getExisting(server, DATA_NAME, SatelliteSavedData::load);
    }

    public static Optional<SatelliteSavedData> getExisting(MinecraftServer server, ResourceKey<Level> dimension) {
        return WorldSavedDataHelper.getExisting(server, dimension, DATA_NAME, SatelliteSavedData::load);
    }

    public static Optional<SatelliteSavedData> getExisting(Level level) {
        return WorldSavedDataHelper.getExisting(level, DATA_NAME, SatelliteSavedData::load);
    }

    public static SatelliteSavedData getData(ServerLevel level) {
        return get(level);
    }

    public static Optional<SatelliteSavedData> getData(Level level) {
        return get(level);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt(TAG_SAT_COUNT, satellites.size());
        int index = 0;
        for (Map.Entry<Integer, Satellite> entry : satellites.entrySet()) {
            Satellite satellite = entry.getValue();
            tag.putInt(TAG_SAT_ID + index, satellite.legacyId());
            tag.put(TAG_SAT_DATA + index, satellite.saveData());
            tag.putInt(TAG_SAT_FREQ + index, entry.getKey());
            index++;
        }
        tag.put(TAG_ENTRIES, entriesTag());
        return tag;
    }

    public boolean isFreqTaken(int frequency) {
        return getSatFromFreq(frequency) != null;
    }

    public boolean isFrequencyTaken(int frequency) {
        return isFreqTaken(frequency);
    }

    public Satellite getSatFromFreq(int frequency) {
        return satellites.get(frequency);
    }

    public Satellite getSatellite(int frequency) {
        return getSatFromFreq(frequency);
    }

    public void putSatellite(int frequency, Satellite satellite) {
        if (satellite == null) {
            return;
        }
        satellites.put(frequency, satellite);
        setDirty();
    }

    public boolean descendSatellite(int frequency) {
        return removeSatellite(frequency);
    }

    public boolean removeSatellite(int frequency) {
        if (satellites.remove(frequency) != null) {
            setDirty();
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return satellites.isEmpty();
    }

    public int size() {
        return satellites.size();
    }

    public List<Map.Entry<Integer, Satellite>> entriesSnapshot() {
        return satellites.entrySet().stream()
                .<Map.Entry<Integer, Satellite>>map(entry -> new AbstractMap.SimpleImmutableEntry<>(
                        entry.getKey(), entry.getValue()))
                .toList();
    }

    public Set<Integer> frequenciesSnapshot() {
        return new TreeSet<>(satellites.keySet());
    }

    public List<Integer> frequenciesSnapshot(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return frequenciesSnapshot().stream().limit(limit).toList();
    }

    public Map<Integer, Satellite> satellitesSnapshot() {
        return Map.copyOf(satellites);
    }

    public LoadDiagnostics loadDiagnostics() {
        return loadDiagnostics;
    }

    public List<EntryLoadDiagnostics> legacyEntryLoadDiagnosticsSnapshot() {
        return legacyEntryLoadDiagnostics;
    }

    public List<EntryLoadDiagnostics> modernEntryLoadDiagnosticsSnapshot() {
        return modernEntryLoadDiagnostics;
    }

    public List<EntryLoadDiagnostics> problemEntryLoadDiagnosticsSnapshot() {
        List<EntryLoadDiagnostics> problems = new ArrayList<>();
        legacyEntryLoadDiagnostics.stream()
                .filter(diagnostics -> !diagnostics.clean())
                .forEach(problems::add);
        modernEntryLoadDiagnostics.stream()
                .filter(diagnostics -> !diagnostics.clean())
                .forEach(problems::add);
        return List.copyOf(problems);
    }

    public Map<LegacySatelliteType, Integer> typeCounts() {
        EnumMap<LegacySatelliteType, Integer> counts = new EnumMap<>(LegacySatelliteType.class);
        for (Satellite satellite : satellites.values()) {
            counts.merge(satellite.type(), 1, Integer::sum);
        }
        return Map.copyOf(counts);
    }

    public List<SatelliteSummary> satelliteSummariesSnapshot(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return satellites.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(limit)
                .map(entry -> SatelliteSummary.of(entry.getKey(), entry.getValue()))
                .toList();
    }

    public void clearSatellites() {
        if (!satellites.isEmpty()) {
            satellites.clear();
            setDirty();
        }
    }

    public void markDirty() {
        setDirty();
    }

    private ListTag entriesTag() {
        ListTag entries = new ListTag();
        for (Map.Entry<Integer, Satellite> entry : satellites.entrySet()) {
            Satellite satellite = entry.getValue();
            CompoundTag tag = new CompoundTag();
            tag.putInt(TAG_FREQUENCY, entry.getKey());
            tag.putInt(TAG_LEGACY_ID, satellite.legacyId());
            tag.putString(TAG_LEGACY_NAME, satellite.legacyName());
            tag.put(TAG_DATA, satellite.saveData());
            entries.add(tag);
        }
        return entries;
    }

    public record SatelliteSummary(int frequency, int legacyId, String legacyName,
                                   Satellite.SatelliteInterface satelliteInterface,
                                   Set<Satellite.InterfaceAction> interfaceActions,
                                   Set<Satellite.CoordAction> coordActions,
                                   Optional<String> cargoPool, long lastOperationMillis) {
        public SatelliteSummary {
            legacyName = legacyName == null ? "" : legacyName;
            satelliteInterface = satelliteInterface == null ? Satellite.SatelliteInterface.NONE : satelliteInterface;
            interfaceActions = interfaceActions == null ? Set.of() : Set.copyOf(interfaceActions);
            coordActions = coordActions == null ? Set.of() : Set.copyOf(coordActions);
            cargoPool = cargoPool == null ? Optional.empty() : cargoPool;
        }

        private static SatelliteSummary of(int frequency, Satellite satellite) {
            return new SatelliteSummary(frequency, satellite.legacyId(), satellite.legacyName(),
                    satellite.satelliteInterface(), satellite.interfaceActions(), satellite.coordActions(),
                    satellite.cargoPool(), satellite.lastOperationMillis());
        }
    }

    public record EntryLoadDiagnostics(String format, int entryIndex, boolean hasLegacyId, boolean hasData,
                                       boolean hasFrequency, int legacyId, int frequency, boolean loaded,
                                       boolean unknownId, boolean duplicateFrequency) {
        public EntryLoadDiagnostics {
            format = format == null ? "" : format;
        }

        public boolean clean() {
            return hasLegacyId
                    && hasData
                    && hasFrequency
                    && loaded
                    && !unknownId
                    && !duplicateFrequency;
        }

        public int problemCount() {
            return (hasLegacyId ? 0 : 1)
                    + (hasData ? 0 : 1)
                    + (hasFrequency ? 0 : 1)
                    + (unknownId ? 1 : 0)
                    + (duplicateFrequency ? 1 : 0);
        }

        public List<String> issues() {
            List<String> issues = new ArrayList<>();
            if (!hasLegacyId) {
                issues.add("missing_legacy_id");
            }
            if (!hasData) {
                issues.add("missing_data");
            }
            if (!hasFrequency) {
                issues.add("missing_frequency");
            }
            if (unknownId) {
                issues.add("unknown_id");
            }
            if (duplicateFrequency) {
                issues.add("duplicate_frequency");
            }
            return List.copyOf(issues);
        }

        public String summary() {
            return "format=" + format
                    + " entryIndex=" + entryIndex
                    + " hasLegacyId=" + hasLegacyId
                    + " hasData=" + hasData
                    + " hasFrequency=" + hasFrequency
                    + " legacyId=" + legacyId
                    + " frequency=" + frequency
                    + " loaded=" + loaded
                    + " unknownId=" + unknownId
                    + " duplicateFrequency=" + duplicateFrequency
                    + " problems=" + problemCount()
                    + " issues=" + issues()
                    + " clean=" + clean();
        }
    }

    public record LoadDiagnostics(boolean hasLegacyCountTag, int legacyEntriesRead, int legacyEntriesLoaded,
                                  int legacyMissingIds, int legacyMissingData, int legacyMissingFrequencies,
                                  int legacyUnknownIds, int legacyDuplicateFrequencies,
                                  boolean hasModernEntriesTag, boolean usedModernEntriesFallback,
                                  int modernEntriesRead, int modernEntriesLoaded,
                                  int modernMissingIds, int modernMissingData, int modernMissingFrequencies,
                                  int modernUnknownIds, int modernDuplicateFrequencies) {
        public static LoadDiagnostics empty() {
            return new LoadDiagnostics(false, 0, 0, 0, 0, 0, 0, 0, false, false, 0, 0, 0, 0, 0, 0, 0);
        }

        public boolean clean() {
            return (hasLegacyCountTag || hasModernEntriesTag)
                    && legacyMissingIds == 0
                    && legacyMissingData == 0
                    && legacyMissingFrequencies == 0
                    && legacyUnknownIds == 0
                    && legacyDuplicateFrequencies == 0
                    && modernMissingIds == 0
                    && modernMissingData == 0
                    && modernMissingFrequencies == 0
                    && modernUnknownIds == 0
                    && modernDuplicateFrequencies == 0;
        }

        public int problemCount() {
            return (hasLegacyCountTag || hasModernEntriesTag ? 0 : 1)
                    + legacyMissingIds
                    + legacyMissingData
                    + legacyMissingFrequencies
                    + legacyUnknownIds
                    + legacyDuplicateFrequencies
                    + modernMissingIds
                    + modernMissingData
                    + modernMissingFrequencies
                    + modernUnknownIds
                    + modernDuplicateFrequencies;
        }

        public List<String> issues() {
            List<String> issues = new ArrayList<>();
            if (!hasLegacyCountTag && !hasModernEntriesTag) {
                issues.add("missing_satellite_root");
            }
            if (legacyMissingIds > 0) {
                issues.add("legacy_missing_ids=" + legacyMissingIds);
            }
            if (legacyMissingData > 0) {
                issues.add("legacy_missing_data=" + legacyMissingData);
            }
            if (legacyMissingFrequencies > 0) {
                issues.add("legacy_missing_frequencies=" + legacyMissingFrequencies);
            }
            if (legacyUnknownIds > 0) {
                issues.add("legacy_unknown_ids=" + legacyUnknownIds);
            }
            if (legacyDuplicateFrequencies > 0) {
                issues.add("legacy_duplicate_frequencies=" + legacyDuplicateFrequencies);
            }
            if (modernMissingIds > 0) {
                issues.add("modern_missing_ids=" + modernMissingIds);
            }
            if (modernMissingData > 0) {
                issues.add("modern_missing_data=" + modernMissingData);
            }
            if (modernMissingFrequencies > 0) {
                issues.add("modern_missing_frequencies=" + modernMissingFrequencies);
            }
            if (modernUnknownIds > 0) {
                issues.add("modern_unknown_ids=" + modernUnknownIds);
            }
            if (modernDuplicateFrequencies > 0) {
                issues.add("modern_duplicate_frequencies=" + modernDuplicateFrequencies);
            }
            return List.copyOf(issues);
        }

        public String summary() {
            return "hasLegacyCount=" + hasLegacyCountTag
                    + " legacyRead=" + legacyEntriesRead
                    + " legacyLoaded=" + legacyEntriesLoaded
                    + " legacyMissingIds=" + legacyMissingIds
                    + " legacyMissingData=" + legacyMissingData
                    + " legacyMissingFreqs=" + legacyMissingFrequencies
                    + " legacyUnknownIds=" + legacyUnknownIds
                    + " legacyDuplicateFreqs=" + legacyDuplicateFrequencies
                    + " hasModernEntries=" + hasModernEntriesTag
                    + " usedModernFallback=" + usedModernEntriesFallback
                    + " modernRead=" + modernEntriesRead
                    + " modernLoaded=" + modernEntriesLoaded
                    + " modernMissingIds=" + modernMissingIds
                    + " modernMissingData=" + modernMissingData
                    + " modernMissingFreqs=" + modernMissingFrequencies
                    + " modernUnknownIds=" + modernUnknownIds
                    + " modernDuplicateFreqs=" + modernDuplicateFrequencies
                    + " problems=" + problemCount()
                    + " issues=" + issues()
                    + " clean=" + clean();
        }
    }
}
