package com.hbm.ntm.satellite;

import com.hbm.ntm.world.saveddata.WorldSavedDataHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SatelliteSavedData extends SavedData {
    public static final String DATA_NAME = "satellites";
    public static final String KEY = DATA_NAME;
    public static final String TAG_SAT_COUNT = "satCount";
    public static final String TAG_SAT_ID = "sat_id_";
    public static final String TAG_SAT_DATA = "sat_data_";
    public static final String TAG_SAT_FREQ = "sat_freq_";
    public static final String TAG_ENTRIES = "entries";
    public static final String TAG_FREQUENCY = "frequency";
    public static final String TAG_LEGACY_ID = "legacyId";
    public static final String TAG_LEGACY_NAME = "legacyName";
    public static final String TAG_DATA = "data";
    public static final int LEGACY_RANDOM_FREQUENCY_BOUND = 100000;

    public final HashMap<Integer, Satellite> sats = new DirtyTrackingSatelliteMap();
    private final Map<Integer, Satellite> satellites = sats;
    private LoadDiagnostics loadDiagnostics = LoadDiagnostics.empty();
    private List<EntryLoadDiagnostics> legacyEntryLoadDiagnostics = List.of();
    private List<EntryLoadDiagnostics> modernEntryLoadDiagnostics = List.of();

    public SatelliteSavedData() {
        setDirty();
    }

    public SatelliteSavedData(String name) {
        this();
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
            Optional<SatelliteEntry> legacyEntry = readLegacyEntryTag(tag, i);
            int legacyId = legacyEntry.map(SatelliteEntry::legacyId).orElse(tag.getInt(TAG_SAT_ID + i));
            int frequency = legacyEntry.map(SatelliteEntry::frequency).orElse(tag.getInt(TAG_SAT_FREQ + i));
            Satellite satellite = legacyEntry.flatMap(SatelliteEntry::satellite).orElse(null);
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
                Optional<SatelliteEntry> modernEntry = readModernEntryTag(entry);
                int legacyId = modernEntry.map(SatelliteEntry::legacyId).orElse(entry.getInt(TAG_LEGACY_ID));
                int frequency = modernEntry.map(SatelliteEntry::frequency).orElse(entry.getInt(TAG_FREQUENCY));
                Satellite satellite = modernEntry.flatMap(SatelliteEntry::satellite).orElse(null);
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

    public static SatelliteSavedData get(MinecraftServer server) {
        return WorldSavedDataHelper.get(server, DATA_NAME, SatelliteSavedData::load, SatelliteSavedData::new);
    }

    public static Optional<SatelliteSavedData> get(MinecraftServer server, ResourceKey<Level> dimension) {
        return WorldSavedDataHelper.get(server, dimension, DATA_NAME, SatelliteSavedData::load,
                SatelliteSavedData::new);
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

    public static SatelliteSavedData getData(MinecraftServer server) {
        return get(server);
    }

    public static Optional<SatelliteSavedData> getData(MinecraftServer server, ResourceKey<Level> dimension) {
        return get(server, dimension);
    }

    public static SatelliteSavedData forWorld(ServerLevel level) {
        return get(level);
    }

    public static Optional<SatelliteSavedData> forWorld(Level level) {
        return get(level);
    }

    public static SatelliteSavedData forWorld(MinecraftServer server) {
        return get(server);
    }

    public static Optional<SatelliteSavedData> forWorld(MinecraftServer server, ResourceKey<Level> dimension) {
        return get(server, dimension);
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

    public void readFromNBT(CompoundTag tag) {
        SatelliteSavedData loaded = load(tag == null ? new CompoundTag() : tag);
        satellites.clear();
        satellites.putAll(loaded.satellites);
        loadDiagnostics = loaded.loadDiagnostics;
        legacyEntryLoadDiagnostics = loaded.legacyEntryLoadDiagnostics;
        modernEntryLoadDiagnostics = loaded.modernEntryLoadDiagnostics;
        setDirty(false);
    }

    public void writeToNBT(CompoundTag tag) {
        save(tag);
    }

    public boolean isFreqTaken(int frequency) {
        return getSatFromFreq(frequency) != null;
    }

    public boolean isFrequencyTaken(int frequency) {
        return isFreqTaken(frequency);
    }

    public boolean containsFrequency(int frequency) {
        return isFreqTaken(frequency);
    }

    public boolean containsFreq(int frequency) {
        return isFreqTaken(frequency);
    }

    public Satellite getSatFromFreq(int frequency) {
        return satellites.get(frequency);
    }

    public Satellite getSatellite(int frequency) {
        return getSatFromFreq(frequency);
    }

    public Optional<Satellite> getSatelliteOptional(int frequency) {
        return Optional.ofNullable(getSatFromFreq(frequency));
    }

    public Satellite getSatellite(int frequency, LegacySatelliteType type) {
        return getSatelliteOptional(frequency, type).orElse(null);
    }

    public Satellite getSatellite(int frequency, Class<? extends Satellite> satelliteClass) {
        return getSatelliteOptional(frequency, satelliteClass).orElse(null);
    }

    public Optional<Satellite> getSatelliteOptional(int frequency, LegacySatelliteType type) {
        Satellite satellite = getSatFromFreq(frequency);
        if (satellite == null || type == null || satellite.type() != type) {
            return Optional.empty();
        }
        return Optional.of(satellite);
    }

    public Optional<Satellite> getSatelliteOptional(int frequency, Class<? extends Satellite> satelliteClass) {
        return Satellite.getTypeFromClass(satelliteClass)
                .flatMap(type -> getSatelliteOptional(frequency, type));
    }

    public Satellite getCargoSatellite(int frequency) {
        return getCargoSatelliteOptional(frequency).orElse(null);
    }

    public Optional<Satellite> getCargoSatelliteOptional(int frequency) {
        Satellite satellite = getSatFromFreq(frequency);
        if (!Satellite.hasCargoPool(satellite)) {
            return Optional.empty();
        }
        return Optional.of(satellite);
    }

    public boolean containsFrequency(int frequency, LegacySatelliteType type) {
        return getSatelliteOptional(frequency, type).isPresent();
    }

    public boolean containsFrequency(int frequency, Class<? extends Satellite> satelliteClass) {
        return getSatelliteOptional(frequency, satelliteClass).isPresent();
    }

    public boolean containsFreq(int frequency, LegacySatelliteType type) {
        return containsFrequency(frequency, type);
    }

    public boolean containsFreq(int frequency, Class<? extends Satellite> satelliteClass) {
        return containsFrequency(frequency, satelliteClass);
    }

    public boolean containsCargoFrequency(int frequency) {
        return getCargoSatelliteOptional(frequency).isPresent();
    }

    public boolean containsCargoFreq(int frequency) {
        return containsCargoFrequency(frequency);
    }

    public OptionalInt randomAvailableFrequency(RandomSource random) {
        return randomAvailableFrequency(random, LEGACY_RANDOM_FREQUENCY_BOUND);
    }

    public OptionalInt randomAvailableFrequency(RandomSource random, int bound) {
        if (random == null || bound <= 0) {
            return OptionalInt.empty();
        }
        int frequency = random.nextInt(bound);
        return isFrequencyTaken(frequency) ? OptionalInt.empty() : OptionalInt.of(frequency);
    }

    public OptionalInt randomAvailableFrequency(RandomSource random, int bound, int attempts) {
        if (random == null || bound <= 0 || attempts <= 0) {
            return OptionalInt.empty();
        }
        for (int i = 0; i < attempts; i++) {
            OptionalInt frequency = randomAvailableFrequency(random, bound);
            if (frequency.isPresent()) {
                return frequency;
            }
        }
        return OptionalInt.empty();
    }

    public void putSatellite(int frequency, Satellite satellite) {
        if (satellite == null) {
            return;
        }
        satellites.put(frequency, satellite);
        setDirty();
    }

    public boolean putSatelliteData(int frequency, int legacyId, CompoundTag data) {
        Satellite satellite = Satellite.load(legacyId, data == null ? new CompoundTag() : data);
        if (satellite == null) {
            return false;
        }
        putSatellite(frequency, satellite);
        return true;
    }

    public boolean putSatelliteData(int frequency, LegacySatelliteType type, CompoundTag data) {
        return type != null && putSatelliteData(frequency, type.legacyId(), data);
    }

    public boolean putSatelliteData(int frequency, Class<? extends Satellite> satelliteClass, CompoundTag data) {
        return Satellite.getTypeFromClass(satelliteClass)
                .map(type -> putSatelliteData(frequency, type, data))
                .orElse(false);
    }

    public boolean putSatelliteEntry(SatelliteEntry entry) {
        return entry != null && putSatelliteData(entry.frequency(), entry.legacyId(), entry.data());
    }

    public int putSatelliteEntries(Collection<SatelliteEntry> entries) {
        return readEntries(entries, false);
    }

    public int putSatelliteEntries(SatelliteEntries entries) {
        return entries == null ? 0 : putSatelliteEntries(entries.entries());
    }

    public boolean putSatellite(int frequency, int legacyId) {
        Satellite satellite = Satellite.create(legacyId);
        if (satellite == null) {
            return false;
        }
        putSatellite(frequency, satellite);
        return true;
    }

    public boolean putSatellite(int frequency, LegacySatelliteType type) {
        return type != null && putSatellite(frequency, type.legacyId());
    }

    public boolean putSatellite(int frequency, Class<? extends Satellite> satelliteClass) {
        return Satellite.getTypeFromClass(satelliteClass)
                .map(type -> putSatellite(frequency, type))
                .orElse(false);
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

    public int removeSatellites(Iterable<Integer> frequencies) {
        if (frequencies == null) {
            return 0;
        }
        int removed = 0;
        for (Integer frequency : frequencies) {
            if (frequency != null && satellites.remove(frequency) != null) {
                removed++;
            }
        }
        if (removed > 0) {
            setDirty();
        }
        return removed;
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

    public List<Map.Entry<Integer, Satellite>> entriesSnapshot(LegacySatelliteType type) {
        if (type == null) {
            return List.of();
        }
        return satellites.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().type() == type)
                .<Map.Entry<Integer, Satellite>>map(entry -> new AbstractMap.SimpleImmutableEntry<>(
                        entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<Map.Entry<Integer, Satellite>> entriesSnapshot(Class<? extends Satellite> satelliteClass) {
        return Satellite.getTypeFromClass(satelliteClass)
                .map(this::entriesSnapshot)
                .orElse(List.of());
    }

    public List<Map.Entry<Integer, Satellite>> cargoEntriesSnapshot() {
        return satellites.entrySet().stream()
                .filter(entry -> Satellite.hasCargoPool(entry.getValue()))
                .<Map.Entry<Integer, Satellite>>map(entry -> new AbstractMap.SimpleImmutableEntry<>(
                        entry.getKey(), entry.getValue()))
                .toList();
    }

    public Set<Integer> frequenciesSnapshot() {
        return new TreeSet<>(satellites.keySet());
    }

    public Set<Integer> frequenciesSnapshot(LegacySatelliteType type) {
        TreeSet<Integer> frequencies = new TreeSet<>();
        for (Map.Entry<Integer, Satellite> entry : entriesSnapshot(type)) {
            frequencies.add(entry.getKey());
        }
        return frequencies;
    }

    public Set<Integer> frequenciesSnapshot(Class<? extends Satellite> satelliteClass) {
        return Satellite.getTypeFromClass(satelliteClass)
                .map(this::frequenciesSnapshot)
                .orElse(Set.of());
    }

    public Set<Integer> cargoFrequenciesSnapshot() {
        TreeSet<Integer> frequencies = new TreeSet<>();
        for (Map.Entry<Integer, Satellite> entry : cargoEntriesSnapshot()) {
            frequencies.add(entry.getKey());
        }
        return frequencies;
    }

    public List<Integer> frequenciesSnapshot(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return frequenciesSnapshot().stream().limit(limit).toList();
    }

    public List<Integer> frequenciesSnapshot(LegacySatelliteType type, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return frequenciesSnapshot(type).stream().limit(limit).toList();
    }

    public List<Integer> frequenciesSnapshot(Class<? extends Satellite> satelliteClass, int limit) {
        return Satellite.getTypeFromClass(satelliteClass)
                .map(type -> frequenciesSnapshot(type, limit))
                .orElse(List.of());
    }

    public List<Integer> cargoFrequenciesSnapshot(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return cargoFrequenciesSnapshot().stream().limit(limit).toList();
    }

    public Map<Integer, Satellite> satellitesSnapshot() {
        return Map.copyOf(satellites);
    }

    public Map<Integer, Satellite> satellitesSnapshot(LegacySatelliteType type) {
        return Map.copyOf(entriesSnapshot(type).stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public Map<Integer, Satellite> satellitesSnapshot(Class<? extends Satellite> satelliteClass) {
        return Satellite.getTypeFromClass(satelliteClass)
                .map(this::satellitesSnapshot)
                .orElse(Map.of());
    }

    public Map<Integer, Satellite> cargoSatellitesSnapshot() {
        return Map.copyOf(cargoEntriesSnapshot().stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
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
            if (satellite != null) {
                counts.merge(satellite.type(), 1, Integer::sum);
            }
        }
        return Map.copyOf(counts);
    }

    public int cargoSatelliteCount() {
        return cargoEntriesSnapshot().size();
    }

    public Map<LegacySatelliteType, Integer> cargoTypeCounts() {
        EnumMap<LegacySatelliteType, Integer> counts = new EnumMap<>(LegacySatelliteType.class);
        for (Satellite satellite : satellites.values()) {
            if (Satellite.hasCargoPool(satellite)) {
                counts.merge(satellite.type(), 1, Integer::sum);
            }
        }
        return Map.copyOf(counts);
    }

    public Map<String, Integer> cargoPoolCounts() {
        HashMap<String, Integer> counts = new HashMap<>();
        for (Satellite satellite : satellites.values()) {
            if (satellite != null) {
                satellite.cargoPool().ifPresent(pool -> counts.merge(pool, 1, Integer::sum));
            }
        }
        return Map.copyOf(counts);
    }

    public SatelliteStats statsSnapshot() {
        return statsSnapshot(16, 8);
    }

    public SatelliteStats statsSnapshot(int frequencyLimit, int summaryLimit) {
        return new SatelliteStats(size(), cargoSatelliteCount(), typeCounts(), cargoTypeCounts(), cargoPoolCounts(),
                frequenciesSnapshot(frequencyLimit), cargoFrequenciesSnapshot(frequencyLimit),
                satelliteSummariesSnapshot(summaryLimit), cargoSatelliteSummariesSnapshot(summaryLimit),
                legacyEntryLoadDiagnostics.size(), modernEntryLoadDiagnostics.size(),
                problemEntryLoadDiagnosticsSnapshot().size(), loadDiagnostics);
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

    public List<SatelliteSummary> satelliteSummariesSnapshot(LegacySatelliteType type, int limit) {
        if (type == null || limit <= 0) {
            return List.of();
        }
        return satellites.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().type() == type)
                .sorted(Map.Entry.comparingByKey())
                .limit(limit)
                .map(entry -> SatelliteSummary.of(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<SatelliteSummary> satelliteSummariesSnapshot(Class<? extends Satellite> satelliteClass, int limit) {
        return Satellite.getTypeFromClass(satelliteClass)
                .map(type -> satelliteSummariesSnapshot(type, limit))
                .orElse(List.of());
    }

    public List<SatelliteSummary> cargoSatelliteSummariesSnapshot(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return satellites.entrySet().stream()
                .filter(entry -> Satellite.hasCargoPool(entry.getValue()))
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

    public boolean readLegacyEntry(CompoundTag tag, int index) {
        Optional<SatelliteEntry> entry = readLegacyEntryTag(tag, index);
        return entry.isPresent()
                && putSatelliteData(entry.get().frequency(), entry.get().legacyId(), entry.get().data());
    }

    public boolean readLegacyEntry(CompoundTag tag) {
        return readLegacyEntry(tag, 0);
    }

    public int readLegacyEntries(CompoundTag tag) {
        return readLegacyEntries(tag, true);
    }

    public int readLegacyEntries(CompoundTag tag, boolean clearExisting) {
        if (tag == null) {
            return 0;
        }
        return readEntries(readLegacyEntriesTag(tag), clearExisting);
    }

    public static void writeLegacyEntry(CompoundTag tag, int index, int frequency, Satellite satellite) {
        if (tag == null || satellite == null) {
            return;
        }
        tag.putInt(TAG_SAT_ID + index, satellite.legacyId());
        tag.put(TAG_SAT_DATA + index, satellite.saveData());
        tag.putInt(TAG_SAT_FREQ + index, frequency);
    }

    public static Optional<SatelliteEntry> readLegacyEntryTag(CompoundTag tag, int index) {
        if (tag == null) {
            return Optional.empty();
        }
        if (!tag.contains(TAG_SAT_FREQ + index, Tag.TAG_INT)
                || !tag.contains(TAG_SAT_ID + index, Tag.TAG_INT)
                || !tag.contains(TAG_SAT_DATA + index, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        return Optional.of(new SatelliteEntry(tag.getInt(TAG_SAT_FREQ + index), tag.getInt(TAG_SAT_ID + index),
                tag.getCompound(TAG_SAT_DATA + index)));
    }

    public static List<SatelliteEntry> readLegacyEntriesTag(CompoundTag tag) {
        if (tag == null) {
            return List.of();
        }
        List<SatelliteEntry> entries = new ArrayList<>();
        int count = tag.getInt(TAG_SAT_COUNT);
        for (int i = 0; i < count; i++) {
            readLegacyEntryTag(tag, i).ifPresent(entries::add);
        }
        return List.copyOf(entries);
    }

    public static SatelliteEntries readLegacyEntriesSnapshot(CompoundTag tag) {
        return new SatelliteEntries(readLegacyEntriesTag(tag));
    }

    public static CompoundTag writeLegacyEntryTag(int frequency, Satellite satellite) {
        CompoundTag tag = new CompoundTag();
        writeLegacyEntry(tag, 0, frequency, satellite);
        return tag;
    }

    public static CompoundTag writeLegacyEntryTag(int index, int frequency, Satellite satellite) {
        CompoundTag tag = new CompoundTag();
        writeLegacyEntry(tag, index, frequency, satellite);
        return tag;
    }

    public void writeLegacyEntries(CompoundTag tag) {
        if (tag == null) {
            return;
        }
        writeLegacyEntries(tag, entriesSnapshot().stream()
                .map(entry -> new SatelliteEntry(entry.getKey(), entry.getValue().legacyId(),
                        entry.getValue().saveData()))
                .toList());
    }

    public CompoundTag writeLegacyEntriesTag() {
        CompoundTag tag = new CompoundTag();
        writeLegacyEntries(tag);
        return tag;
    }

    public static void writeLegacyEntries(CompoundTag tag, Collection<SatelliteEntry> entries) {
        if (tag == null) {
            return;
        }
        List<SatelliteEntry> values = entries == null ? List.of()
                : entries.stream().filter(Objects::nonNull).toList();
        tag.putInt(TAG_SAT_COUNT, values.size());
        int index = 0;
        for (SatelliteEntry entry : values) {
            entry.writeLegacy(tag, index);
            index++;
        }
    }

    public static CompoundTag writeLegacyEntriesTag(Collection<SatelliteEntry> entries) {
        CompoundTag tag = new CompoundTag();
        writeLegacyEntries(tag, entries);
        return tag;
    }

    public int readEntries(Collection<SatelliteEntry> entries) {
        return readEntries(entries, true);
    }

    public int readEntries(Collection<SatelliteEntry> entries, boolean clearExisting) {
        if (entries == null) {
            return 0;
        }
        if (clearExisting) {
            clearSatellites();
        }
        int loaded = 0;
        for (SatelliteEntry entry : entries) {
            if (entry != null && putSatelliteData(entry.frequency(), entry.legacyId(), entry.data())) {
                loaded++;
            }
        }
        return loaded;
    }

    public SatelliteEntries satelliteEntriesSnapshot() {
        return new SatelliteEntries(entriesSnapshot().stream()
                .map(entry -> new SatelliteEntry(entry.getKey(), entry.getValue().legacyId(),
                        entry.getValue().saveData()))
                .toList());
    }

    public SatelliteEntries satelliteEntriesSnapshot(LegacySatelliteType type) {
        return new SatelliteEntries(entriesSnapshot(type).stream()
                .map(entry -> new SatelliteEntry(entry.getKey(), entry.getValue().legacyId(),
                        entry.getValue().saveData()))
                .toList());
    }

    public SatelliteEntries satelliteEntriesSnapshot(Class<? extends Satellite> satelliteClass) {
        return Satellite.getTypeFromClass(satelliteClass)
                .map(this::satelliteEntriesSnapshot)
                .orElse(SatelliteEntries.EMPTY);
    }

    public SatelliteEntries cargoSatelliteEntriesSnapshot() {
        return new SatelliteEntries(cargoEntriesSnapshot().stream()
                .map(entry -> new SatelliteEntry(entry.getKey(), entry.getValue().legacyId(),
                        entry.getValue().saveData()))
                .toList());
    }

    public boolean readModernEntry(CompoundTag tag) {
        Optional<SatelliteEntry> entry = readModernEntryTag(tag);
        return entry.isPresent()
                && putSatelliteData(entry.get().frequency(), entry.get().legacyId(), entry.get().data());
    }

    public int readModernEntries(CompoundTag tag) {
        return readModernEntries(tag, true);
    }

    public int readModernEntries(CompoundTag tag, boolean clearExisting) {
        if (tag == null) {
            return 0;
        }
        return readModernEntries(tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND), clearExisting);
    }

    public int readModernEntries(ListTag entries) {
        return readModernEntries(entries, true);
    }

    public int readModernEntries(ListTag entries, boolean clearExisting) {
        if (entries == null) {
            return 0;
        }
        return readEntries(readModernEntriesList(entries), clearExisting);
    }

    public static void writeModernEntry(CompoundTag tag, int frequency, Satellite satellite) {
        if (tag == null || satellite == null) {
            return;
        }
        tag.putInt(TAG_FREQUENCY, frequency);
        tag.putInt(TAG_LEGACY_ID, satellite.legacyId());
        tag.putString(TAG_LEGACY_NAME, satellite.legacyName());
        tag.put(TAG_DATA, satellite.saveData());
    }

    public static Optional<SatelliteEntry> readModernEntryTag(CompoundTag tag) {
        if (tag == null) {
            return Optional.empty();
        }
        if (!tag.contains(TAG_FREQUENCY, Tag.TAG_INT)
                || !tag.contains(TAG_LEGACY_ID, Tag.TAG_INT)
                || !tag.contains(TAG_DATA, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        return Optional.of(new SatelliteEntry(tag.getInt(TAG_FREQUENCY), tag.getInt(TAG_LEGACY_ID),
                tag.getCompound(TAG_DATA)));
    }

    public static List<SatelliteEntry> readModernEntriesList(ListTag entries) {
        if (entries == null) {
            return List.of();
        }
        List<SatelliteEntry> result = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            readModernEntryTag(entries.getCompound(i)).ifPresent(result::add);
        }
        return List.copyOf(result);
    }

    public static SatelliteEntries readModernEntriesSnapshot(CompoundTag tag) {
        if (tag == null) {
            return SatelliteEntries.EMPTY;
        }
        return readModernEntriesSnapshot(tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND));
    }

    public static SatelliteEntries readModernEntriesSnapshot(ListTag entries) {
        return new SatelliteEntries(readModernEntriesList(entries));
    }

    public static CompoundTag writeModernEntryTag(int frequency, Satellite satellite) {
        CompoundTag tag = new CompoundTag();
        writeModernEntry(tag, frequency, satellite);
        return tag;
    }

    public void writeModernEntries(CompoundTag tag) {
        if (tag != null) {
            tag.put(TAG_ENTRIES, writeModernEntriesList());
        }
    }

    public CompoundTag writeModernEntriesTag() {
        CompoundTag tag = new CompoundTag();
        writeModernEntries(tag);
        return tag;
    }

    public ListTag writeModernEntriesList() {
        return entriesTag();
    }

    public static ListTag writeModernEntriesList(Collection<SatelliteEntry> entries) {
        ListTag list = new ListTag();
        if (entries != null) {
            for (SatelliteEntry entry : entries) {
                if (entry != null) {
                    list.add(entry.writeModernTag());
                }
            }
        }
        return list;
    }

    private final class DirtyTrackingSatelliteMap extends HashMap<Integer, Satellite> {
        @Override
        public Satellite put(Integer key, Satellite value) {
            Satellite previous = super.put(key, value);
            if (previous != value) {
                setDirty();
            }
            return previous;
        }

        @Override
        public Satellite putIfAbsent(Integer key, Satellite value) {
            Satellite previous = super.putIfAbsent(key, value);
            if (previous == null && value != null) {
                setDirty();
            }
            return previous;
        }

        @Override
        public Satellite remove(Object key) {
            Satellite previous = super.remove(key);
            if (previous != null) {
                setDirty();
            }
            return previous;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed) {
                setDirty();
            }
            return removed;
        }

        @Override
        public Satellite replace(Integer key, Satellite value) {
            Satellite previous = super.replace(key, value);
            if (previous != null && previous != value) {
                setDirty();
            }
            return previous;
        }

        @Override
        public boolean replace(Integer key, Satellite oldValue, Satellite newValue) {
            boolean replaced = super.replace(key, oldValue, newValue);
            if (replaced && oldValue != newValue) {
                setDirty();
            }
            return replaced;
        }

        @Override
        public void putAll(Map<? extends Integer, ? extends Satellite> map) {
            if (!map.isEmpty()) {
                super.putAll(map);
                setDirty();
            }
        }

        @Override
        public Satellite computeIfAbsent(Integer key,
                                         Function<? super Integer, ? extends Satellite> mappingFunction) {
            boolean hadKey = containsKey(key);
            Satellite result = super.computeIfAbsent(key, mappingFunction);
            if (!hadKey && result != null) {
                setDirty();
            }
            return result;
        }

        @Override
        public Satellite compute(Integer key,
                                 BiFunction<? super Integer, ? super Satellite, ? extends Satellite> remappingFunction) {
            boolean hadKey = containsKey(key);
            Satellite previous = super.get(key);
            Satellite result = super.compute(key, remappingFunction);
            if (hadKey != containsKey(key) || previous != result) {
                setDirty();
            }
            return result;
        }

        @Override
        public Satellite computeIfPresent(Integer key,
                                          BiFunction<? super Integer, ? super Satellite,
                                                  ? extends Satellite> remappingFunction) {
            boolean hadKey = containsKey(key);
            Satellite previous = super.get(key);
            Satellite result = super.computeIfPresent(key, remappingFunction);
            if (hadKey && (previous != result || !containsKey(key))) {
                setDirty();
            }
            return result;
        }

        @Override
        public Satellite merge(Integer key, Satellite value,
                               BiFunction<? super Satellite, ? super Satellite, ? extends Satellite> remappingFunction) {
            boolean hadKey = containsKey(key);
            Satellite previous = super.get(key);
            Satellite result = super.merge(key, value, remappingFunction);
            if (!hadKey || previous != result) {
                setDirty();
            }
            return result;
        }

        @Override
        public void replaceAll(BiFunction<? super Integer, ? super Satellite, ? extends Satellite> function) {
            if (!isEmpty()) {
                super.replaceAll(function);
                setDirty();
            }
        }

        @Override
        public void clear() {
            if (!isEmpty()) {
                super.clear();
                setDirty();
            }
        }
    }

    private ListTag entriesTag() {
        ListTag entries = new ListTag();
        for (Map.Entry<Integer, Satellite> entry : satellites.entrySet()) {
            Satellite satellite = entry.getValue();
            CompoundTag tag = new CompoundTag();
            writeModernEntry(tag, entry.getKey(), satellite);
            entries.add(tag);
        }
        return entries;
    }

    public record SatelliteEntry(int frequency, int legacyId, CompoundTag data) {
        public SatelliteEntry(int frequency, LegacySatelliteType type, CompoundTag data) {
            this(frequency, type == null ? -1 : type.legacyId(), data);
        }

        public SatelliteEntry(int frequency, Class<? extends Satellite> satelliteClass, CompoundTag data) {
            this(frequency, Satellite.getLegacyIdFromClass(satelliteClass), data);
        }

        public SatelliteEntry {
            data = data == null ? new CompoundTag() : data.copy();
        }

        public static Optional<SatelliteEntry> of(int frequency, Satellite satellite) {
            if (satellite == null) {
                return Optional.empty();
            }
            return Optional.of(new SatelliteEntry(frequency, satellite.legacyId(), satellite.saveData()));
        }

        public static Optional<SatelliteEntry> of(int frequency, LegacySatelliteType type, CompoundTag data) {
            if (type == null) {
                return Optional.empty();
            }
            return Optional.of(new SatelliteEntry(frequency, type, data));
        }

        public static Optional<SatelliteEntry> of(int frequency, Class<? extends Satellite> satelliteClass,
                                                  CompoundTag data) {
            return Satellite.getTypeFromClass(satelliteClass)
                    .flatMap(type -> of(frequency, type, data));
        }

        public Optional<Satellite> satellite() {
            return Optional.ofNullable(Satellite.load(legacyId, data.copy()));
        }

        public Optional<LegacySatelliteType> type() {
            return Optional.ofNullable(LegacySatelliteType.byLegacyId(legacyId));
        }

        public Optional<Class<? extends Satellite>> satelliteClass() {
            return Satellite.getClassFromLegacyId(legacyId);
        }

        public boolean matches(LegacySatelliteType type) {
            return type != null && type().filter(candidate -> candidate == type).isPresent();
        }

        public boolean matches(Class<? extends Satellite> satelliteClass) {
            return Satellite.getTypeFromClass(satelliteClass)
                    .filter(this::matches)
                    .isPresent();
        }

        public Optional<String> cargoPool() {
            return type().flatMap(Satellite::cargoPoolForType);
        }

        public boolean hasCargoPool() {
            return cargoPool().isPresent();
        }

        public String legacyName() {
            return type().map(LegacySatelliteType::legacyName).orElse("");
        }

        public SatelliteSummary summary() {
            return satellite()
                    .map(satellite -> SatelliteSummary.of(frequency, satellite))
                    .orElseGet(() -> new SatelliteSummary(frequency, legacyId, legacyName(),
                            Satellite.SatelliteInterface.NONE, Set.of(), Set.of(),
                            type().flatMap(Satellite::cargoPoolForType), 0L));
        }

        public void writeLegacy(CompoundTag tag, int index) {
            if (tag != null) {
                tag.putInt(TAG_SAT_ID + index, legacyId);
                tag.put(TAG_SAT_DATA + index, data.copy());
                tag.putInt(TAG_SAT_FREQ + index, frequency);
            }
        }

        public CompoundTag writeLegacyTag(int index) {
            CompoundTag tag = new CompoundTag();
            writeLegacy(tag, index);
            return tag;
        }

        public void writeModern(CompoundTag tag) {
            if (tag != null) {
                tag.putInt(TAG_FREQUENCY, frequency);
                tag.putInt(TAG_LEGACY_ID, legacyId);
                String name = legacyName();
                if (!name.isBlank()) {
                    tag.putString(TAG_LEGACY_NAME, name);
                }
                tag.put(TAG_DATA, data.copy());
            }
        }

        public CompoundTag writeModernTag() {
            CompoundTag tag = new CompoundTag();
            writeModern(tag);
            return tag;
        }
    }

    public record SatelliteEntries(List<SatelliteEntry> entries) {
        public static final SatelliteEntries EMPTY = new SatelliteEntries(List.of());

        public SatelliteEntries {
            entries = entries == null ? List.of() : entries.stream()
                    .filter(Objects::nonNull)
                    .toList();
        }

        public boolean isEmpty() {
            return entries.isEmpty();
        }

        public int size() {
            return entries.size();
        }

        public List<SatelliteEntry> entries(LegacySatelliteType type) {
            if (type == null) {
                return List.of();
            }
            return entries.stream()
                    .filter(entry -> entry.matches(type))
                    .toList();
        }

        public List<SatelliteEntry> entries(Class<? extends Satellite> satelliteClass) {
            return Satellite.getTypeFromClass(satelliteClass)
                    .map(this::entries)
                    .orElse(List.of());
        }

        public SatelliteEntries filter(LegacySatelliteType type) {
            return new SatelliteEntries(entries(type));
        }

        public SatelliteEntries filter(Class<? extends Satellite> satelliteClass) {
            return Satellite.getTypeFromClass(satelliteClass)
                    .map(this::filter)
                    .orElse(EMPTY);
        }

        public List<SatelliteEntry> cargoEntries() {
            return entries.stream()
                    .filter(SatelliteEntry::hasCargoPool)
                    .toList();
        }

        public SatelliteEntries cargoFilter() {
            return new SatelliteEntries(cargoEntries());
        }

        public Set<Integer> frequencies() {
            TreeSet<Integer> frequencies = new TreeSet<>();
            for (SatelliteEntry entry : entries) {
                frequencies.add(entry.frequency());
            }
            return frequencies;
        }

        public Set<Integer> frequencies(LegacySatelliteType type) {
            TreeSet<Integer> frequencies = new TreeSet<>();
            for (SatelliteEntry entry : entries(type)) {
                frequencies.add(entry.frequency());
            }
            return frequencies;
        }

        public Set<Integer> frequencies(Class<? extends Satellite> satelliteClass) {
            return Satellite.getTypeFromClass(satelliteClass)
                    .map(this::frequencies)
                    .orElse(Set.of());
        }

        public Set<Integer> cargoFrequencies() {
            TreeSet<Integer> frequencies = new TreeSet<>();
            for (SatelliteEntry entry : cargoEntries()) {
                frequencies.add(entry.frequency());
            }
            return frequencies;
        }

        public List<Integer> frequencies(int limit) {
            if (limit <= 0) {
                return List.of();
            }
            return frequencies().stream().limit(limit).toList();
        }

        public List<Integer> frequencies(LegacySatelliteType type, int limit) {
            if (limit <= 0) {
                return List.of();
            }
            return frequencies(type).stream().limit(limit).toList();
        }

        public List<Integer> frequencies(Class<? extends Satellite> satelliteClass, int limit) {
            return Satellite.getTypeFromClass(satelliteClass)
                    .map(type -> frequencies(type, limit))
                    .orElse(List.of());
        }

        public List<Integer> cargoFrequencies(int limit) {
            if (limit <= 0) {
                return List.of();
            }
            return cargoFrequencies().stream().limit(limit).toList();
        }

        public Map<LegacySatelliteType, Integer> typeCounts() {
            EnumMap<LegacySatelliteType, Integer> counts = new EnumMap<>(LegacySatelliteType.class);
            for (SatelliteEntry entry : entries) {
                entry.type().ifPresent(type -> counts.merge(type, 1, Integer::sum));
            }
            return Map.copyOf(counts);
        }

        public int cargoSatelliteCount() {
            return cargoEntries().size();
        }

        public Map<LegacySatelliteType, Integer> cargoTypeCounts() {
            EnumMap<LegacySatelliteType, Integer> counts = new EnumMap<>(LegacySatelliteType.class);
            for (SatelliteEntry entry : cargoEntries()) {
                entry.type().ifPresent(type -> counts.merge(type, 1, Integer::sum));
            }
            return Map.copyOf(counts);
        }

        public Map<String, Integer> cargoPoolCounts() {
            HashMap<String, Integer> counts = new HashMap<>();
            for (SatelliteEntry entry : entries) {
                entry.cargoPool().ifPresent(pool -> counts.merge(pool, 1, Integer::sum));
            }
            return Map.copyOf(counts);
        }

        public List<SatelliteSummary> satelliteSummaries(int limit) {
            if (limit <= 0) {
                return List.of();
            }
            return entries.stream()
                    .sorted(java.util.Comparator.comparingInt(SatelliteEntry::frequency))
                    .limit(limit)
                    .map(SatelliteEntry::summary)
                    .toList();
        }

        public List<SatelliteSummary> satelliteSummaries(LegacySatelliteType type, int limit) {
            if (limit <= 0) {
                return List.of();
            }
            return entries(type).stream()
                    .sorted(java.util.Comparator.comparingInt(SatelliteEntry::frequency))
                    .limit(limit)
                    .map(SatelliteEntry::summary)
                    .toList();
        }

        public List<SatelliteSummary> satelliteSummaries(Class<? extends Satellite> satelliteClass, int limit) {
            return Satellite.getTypeFromClass(satelliteClass)
                    .map(type -> satelliteSummaries(type, limit))
                    .orElse(List.of());
        }

        public List<SatelliteSummary> cargoSatelliteSummaries(int limit) {
            if (limit <= 0) {
                return List.of();
            }
            return cargoEntries().stream()
                    .sorted(java.util.Comparator.comparingInt(SatelliteEntry::frequency))
                    .limit(limit)
                    .map(SatelliteEntry::summary)
                    .toList();
        }

        public SatelliteStats stats(int frequencyLimit, int summaryLimit) {
            return new SatelliteStats(size(), cargoSatelliteCount(), typeCounts(), cargoTypeCounts(), cargoPoolCounts(),
                    frequencies(frequencyLimit), cargoFrequencies(frequencyLimit), satelliteSummaries(summaryLimit),
                    cargoSatelliteSummaries(summaryLimit), 0, 0, 0, LoadDiagnostics.empty());
        }

        public SatelliteSavedData toData() {
            SatelliteSavedData data = new SatelliteSavedData();
            data.satellites.clear();
            data.readEntries(entries, false);
            data.setDirty(false);
            return data;
        }

        public void writeLegacy(CompoundTag tag) {
            writeLegacyEntries(tag, entries);
        }

        public CompoundTag writeLegacyTag() {
            return writeLegacyEntriesTag(entries);
        }

        public void writeModern(CompoundTag tag) {
            if (tag != null) {
                tag.put(TAG_ENTRIES, writeModernList());
            }
        }

        public CompoundTag writeModernTag() {
            CompoundTag tag = new CompoundTag();
            writeModern(tag);
            return tag;
        }

        public ListTag writeModernList() {
            return writeModernEntriesList(entries);
        }
    }

    public record SatelliteStats(int entries, int cargoEntries,
                                 Map<LegacySatelliteType, Integer> typeCounts,
                                 Map<LegacySatelliteType, Integer> cargoTypeCounts,
                                 Map<String, Integer> cargoPoolCounts,
                                 List<Integer> frequencies,
                                 List<Integer> cargoFrequencies,
                                 List<SatelliteSummary> satellites,
                                 List<SatelliteSummary> cargoSatellites,
                                 int legacyEntryDiagnostics,
                                 int modernEntryDiagnostics,
                                 int problemEntries,
                                 LoadDiagnostics loadDiagnostics) {
        public SatelliteStats {
            typeCounts = typeCounts == null ? Map.of() : Map.copyOf(typeCounts);
            cargoTypeCounts = cargoTypeCounts == null ? Map.of() : Map.copyOf(cargoTypeCounts);
            cargoPoolCounts = cargoPoolCounts == null ? Map.of() : Map.copyOf(cargoPoolCounts);
            frequencies = frequencies == null ? List.of() : List.copyOf(frequencies);
            cargoFrequencies = cargoFrequencies == null ? List.of() : List.copyOf(cargoFrequencies);
            satellites = satellites == null ? List.of() : List.copyOf(satellites);
            cargoSatellites = cargoSatellites == null ? List.of() : List.copyOf(cargoSatellites);
            loadDiagnostics = loadDiagnostics == null ? LoadDiagnostics.empty() : loadDiagnostics;
        }

        public static SatelliteStats empty() {
            return new SatelliteStats(0, 0, Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(),
                    List.of(), 0, 0, 0, LoadDiagnostics.empty());
        }

        public String summary() {
            return "entries=" + entries
                    + " cargoEntries=" + cargoEntries
                    + " types=" + typeCounts
                    + " cargoTypes=" + cargoTypeCounts
                    + " cargoPools=" + cargoPoolCounts
                    + " frequencies=" + frequencies
                    + " cargoFrequencies=" + cargoFrequencies
                    + " legacyEntryDiagnostics=" + legacyEntryDiagnostics
                    + " modernEntryDiagnostics=" + modernEntryDiagnostics
                    + " problemEntries=" + problemEntries
                    + " load={" + loadDiagnostics.summary() + "}";
        }

        public String detail() {
            return summary()
                    + " satellites=" + satelliteDetails()
                    + " cargoSatellites=" + cargoSatelliteDetails();
        }

        private List<String> satelliteDetails() {
            return satellites.stream()
                    .map(SatelliteSummary::detail)
                    .toList();
        }

        private List<String> cargoSatelliteDetails() {
            return cargoSatellites.stream()
                    .map(SatelliteSummary::detail)
                    .toList();
        }
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

        public Optional<LegacySatelliteType> type() {
            return Optional.ofNullable(LegacySatelliteType.byLegacyId(legacyId));
        }

        public Optional<Class<? extends Satellite>> satelliteClass() {
            return Satellite.getClassFromLegacyId(legacyId);
        }

        public String typeName() {
            return type().map(LegacySatelliteType::name).orElse("");
        }

        public String className() {
            return satelliteClass().map(Class::getSimpleName).orElse("");
        }

        public String cargoPoolName() {
            return cargoPool.orElse("");
        }

        public boolean hasCargoPool() {
            return cargoPool.isPresent();
        }

        public String detail() {
            return "freq=" + frequency
                    + " id=" + legacyId
                    + " type=" + typeName()
                    + " name=" + legacyName
                    + " class=" + className()
                    + " interface=" + satelliteInterface
                    + " interfaceActions=" + interfaceActions
                    + " coordActions=" + coordActions
                    + " cargo=" + cargoPoolName()
                    + " lastOp=" + lastOperationMillis;
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
