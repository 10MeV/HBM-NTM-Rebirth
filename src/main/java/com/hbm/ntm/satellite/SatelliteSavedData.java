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

    public SatelliteSavedData() {
        setDirty();
    }

    public static SatelliteSavedData load(CompoundTag tag) {
        SatelliteSavedData data = new SatelliteSavedData();
        data.satellites.clear();
        int count = tag.getInt(TAG_SAT_COUNT);
        int legacyLoaded = 0;
        int legacyUnknownIds = 0;
        int legacyDuplicateFrequencies = 0;
        for (int i = 0; i < count; i++) {
            int legacyId = tag.getInt(TAG_SAT_ID + i);
            int frequency = tag.getInt(TAG_SAT_FREQ + i);
            Satellite satellite = Satellite.load(legacyId, tag.getCompound(TAG_SAT_DATA + i));
            if (satellite != null) {
                if (data.satellites.containsKey(frequency)) {
                    legacyDuplicateFrequencies++;
                }
                data.satellites.put(frequency, satellite);
                legacyLoaded++;
            } else {
                legacyUnknownIds++;
            }
        }
        boolean usedModernEntriesFallback = false;
        int modernEntriesRead = 0;
        int modernEntriesLoaded = 0;
        int modernUnknownIds = 0;
        int modernDuplicateFrequencies = 0;
        if (data.satellites.isEmpty() && tag.contains(TAG_ENTRIES, Tag.TAG_LIST)) {
            usedModernEntriesFallback = true;
            ListTag entries = tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
            modernEntriesRead = entries.size();
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag entry = entries.getCompound(i);
                int legacyId = entry.getInt(TAG_LEGACY_ID);
                int frequency = entry.getInt(TAG_FREQUENCY);
                Satellite satellite = Satellite.load(legacyId, entry.getCompound(TAG_DATA));
                if (satellite != null) {
                    if (data.satellites.containsKey(frequency)) {
                        modernDuplicateFrequencies++;
                    }
                    data.satellites.put(frequency, satellite);
                    modernEntriesLoaded++;
                } else {
                    modernUnknownIds++;
                }
            }
        }
        data.loadDiagnostics = new LoadDiagnostics(count, legacyLoaded, legacyUnknownIds,
                legacyDuplicateFrequencies, usedModernEntriesFallback, modernEntriesRead,
                modernEntriesLoaded, modernUnknownIds, modernDuplicateFrequencies);
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

    public record LoadDiagnostics(int legacyEntriesRead, int legacyEntriesLoaded,
                                  int legacyUnknownIds, int legacyDuplicateFrequencies,
                                  boolean usedModernEntriesFallback,
                                  int modernEntriesRead, int modernEntriesLoaded,
                                  int modernUnknownIds, int modernDuplicateFrequencies) {
        public static LoadDiagnostics empty() {
            return new LoadDiagnostics(0, 0, 0, 0, false, 0, 0, 0, 0);
        }

        public boolean clean() {
            return legacyUnknownIds == 0
                    && legacyDuplicateFrequencies == 0
                    && modernUnknownIds == 0
                    && modernDuplicateFrequencies == 0;
        }

        public int problemCount() {
            return legacyUnknownIds + legacyDuplicateFrequencies + modernUnknownIds + modernDuplicateFrequencies;
        }

        public List<String> issues() {
            List<String> issues = new ArrayList<>();
            if (legacyUnknownIds > 0) {
                issues.add("legacy_unknown_ids=" + legacyUnknownIds);
            }
            if (legacyDuplicateFrequencies > 0) {
                issues.add("legacy_duplicate_frequencies=" + legacyDuplicateFrequencies);
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
            return "legacyRead=" + legacyEntriesRead
                    + " legacyLoaded=" + legacyEntriesLoaded
                    + " legacyUnknownIds=" + legacyUnknownIds
                    + " legacyDuplicateFreqs=" + legacyDuplicateFrequencies
                    + " usedModernFallback=" + usedModernEntriesFallback
                    + " modernRead=" + modernEntriesRead
                    + " modernLoaded=" + modernEntriesLoaded
                    + " modernUnknownIds=" + modernUnknownIds
                    + " modernDuplicateFreqs=" + modernDuplicateFrequencies
                    + " problems=" + problemCount()
                    + " issues=" + issues()
                    + " clean=" + clean();
        }
    }
}
