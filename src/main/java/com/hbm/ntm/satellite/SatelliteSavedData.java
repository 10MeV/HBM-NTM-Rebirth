package com.hbm.ntm.satellite;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.world.saveddata.WorldSavedDataHelper;
import com.hbm.util.fauxpointtwelve.NBTTagCompound;
import com.hbm.util.fauxpointtwelve.WorldSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SatelliteSavedData extends WorldSavedData {
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

    private static <T> List<T> diagnosticList(Collection<? extends T> source) {
        return source == null ? List.of() : java.util.Collections.unmodifiableList(new ArrayList<>(source));
    }

    private static <K, V> Map<K, V> diagnosticMap(Map<? extends K, ? extends V> source) {
        return source == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    private static <T> Set<T> diagnosticSet(Collection<? extends T> source) {
        return source == null ? Set.of() : java.util.Collections.unmodifiableSet(new LinkedHashSet<>(source));
    }

    public SatelliteSavedData() {
        super(DATA_NAME);
        setDirty();
    }

    public SatelliteSavedData(String name) {
        super(name);
    }

    public static SatelliteSavedData load(CompoundTag tag) {
        SatelliteSavedData data = createLoadedData();
        NBTTagCompound legacyTag = NBTTagCompound.copyOf(Objects.requireNonNull(tag, "tag"));
        LoadInspection inspection = inspectLoad(legacyTag);
        boolean hasLegacyRoot = legacyTag.hasKey(TAG_SAT_COUNT);
        boolean usedModernEntriesFallback = false;
        int modernEntriesLoaded = inspection.modernEntriesLoaded();
        try {
            if (!hasLegacyRoot && inspection.hasModernEntriesTag()) {
                usedModernEntriesFallback = true;
                modernEntriesLoaded = data.readModernEntries(NBTTagCompound.copyOf(tag)
                        .getTagList(TAG_ENTRIES, Tag.TAG_COMPOUND), false);
            } else {
                data.readLegacyEntriesFromTag(legacyTag);
            }
        } catch (Exception exception) {
            HbmNtm.LOGGER.warn(
                    "Keeping partially loaded satellites SavedData after legacy root read failure, matching 1.7.10 MapStorage.",
                    exception);
        }
        data.loadDiagnostics = inspection.toDiagnostics(usedModernEntriesFallback, modernEntriesLoaded);
        data.legacyEntryLoadDiagnostics = inspection.legacyEntryDiagnostics();
        data.modernEntryLoadDiagnostics = inspection.modernEntryDiagnostics();
        data.setDirty(false);
        return data;
    }

    private static LoadInspection inspectLoad(NBTTagCompound tag) {
        List<EntryLoadDiagnostics> legacyEntryDiagnostics = new ArrayList<>();
        int legacyMissingIds = 0;
        int legacyMissingData = 0;
        int legacyMissingFrequencies = 0;
        int legacyUnknownIds = 0;
        int legacyDuplicateFrequencies = 0;
        int legacyLoaded = 0;
        HashMap<Integer, Boolean> loadedFrequencies = new HashMap<>();
        int count = tag.getInteger(TAG_SAT_COUNT);
        for (int i = 0; i < count; i++) {
            boolean hasLegacyId = tag.hasKey(TAG_SAT_ID + i, 99);
            boolean hasData = tag.func_150299_b(TAG_SAT_DATA + i) == Tag.TAG_COMPOUND;
            boolean hasFrequency = tag.hasKey(TAG_SAT_FREQ + i, 99);
            int legacyId = tag.getInteger(TAG_SAT_ID + i);
            int frequency = tag.getInteger(TAG_SAT_FREQ + i);
            boolean loaded = isLegacySatelliteRegistered(legacyId);
            boolean duplicateFrequency = loaded && loadedFrequencies.containsKey(frequency);
            if (!hasLegacyId) {
                legacyMissingIds++;
            }
            if (!hasData) {
                legacyMissingData++;
            }
            if (!hasFrequency) {
                legacyMissingFrequencies++;
            }
            if (loaded) {
                if (duplicateFrequency) {
                    legacyDuplicateFrequencies++;
                }
                loadedFrequencies.put(frequency, Boolean.TRUE);
                legacyLoaded++;
            } else {
                legacyUnknownIds++;
            }
            legacyEntryDiagnostics.add(new EntryLoadDiagnostics("legacy", i, hasLegacyId, hasData, hasFrequency,
                    legacyId, frequency, loaded, !loaded, duplicateFrequency));
        }

        boolean hasModernEntriesTag = tag.func_150299_b(TAG_ENTRIES) == Tag.TAG_LIST;
        int modernEntriesRead = 0;
        int modernEntriesLoaded = 0;
        int modernMissingIds = 0;
        int modernMissingData = 0;
        int modernMissingFrequencies = 0;
        int modernUnknownIds = 0;
        int modernDuplicateFrequencies = 0;
        List<EntryLoadDiagnostics> modernEntryDiagnostics = new ArrayList<>();
        if (hasModernEntriesTag) {
            NBTTagCompound modernTag = tag.copy();
            ListTag entries = modernTag.getTagList(TAG_ENTRIES, Tag.TAG_COMPOUND);
            modernEntriesRead = entries.size();
            HashMap<Integer, Boolean> modernFrequencies = new HashMap<>();
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag entry = entries.getCompound(i);
                boolean hasLegacyId = entry.contains(TAG_LEGACY_ID, Tag.TAG_INT);
                boolean hasData = entry.contains(TAG_DATA, Tag.TAG_COMPOUND);
                boolean hasFrequency = entry.contains(TAG_FREQUENCY, Tag.TAG_INT);
                int legacyId = entry.getInt(TAG_LEGACY_ID);
                int frequency = entry.getInt(TAG_FREQUENCY);
                boolean loaded = isLegacySatelliteRegistered(legacyId) && hasLegacyId && hasData && hasFrequency;
                boolean duplicateFrequency = loaded && modernFrequencies.containsKey(frequency);
                if (!hasLegacyId) {
                    modernMissingIds++;
                }
                if (!hasData) {
                    modernMissingData++;
                }
                if (!hasFrequency) {
                    modernMissingFrequencies++;
                }
                if (loaded) {
                    if (duplicateFrequency) {
                        modernDuplicateFrequencies++;
                    }
                    modernFrequencies.put(frequency, Boolean.TRUE);
                    modernEntriesLoaded++;
                } else {
                    modernUnknownIds++;
                }
                modernEntryDiagnostics.add(new EntryLoadDiagnostics("modern", i, hasLegacyId, hasData,
                        hasFrequency, legacyId, frequency, loaded, !loaded, duplicateFrequency));
            }
        }

        return new LoadInspection(tag.hasKey(TAG_SAT_COUNT), count, legacyLoaded, legacyMissingIds,
                legacyMissingData, legacyMissingFrequencies, legacyUnknownIds, legacyDuplicateFrequencies,
                hasModernEntriesTag, modernEntriesRead, modernEntriesLoaded, modernMissingIds, modernMissingData,
                modernMissingFrequencies, modernUnknownIds, modernDuplicateFrequencies,
                List.copyOf(legacyEntryDiagnostics), List.copyOf(modernEntryDiagnostics));
    }

    private static boolean isLegacySatelliteRegistered(int legacyId) {
        try {
            List<Class<? extends com.hbm.saveddata.satellites.Satellite>> registry =
                    com.hbm.saveddata.satellites.Satellite.satellites;
            return legacyId >= 0 && legacyId < registry.size() && registry.get(legacyId) != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static SatelliteSavedData createData() {
        return new com.hbm.saveddata.SatelliteSavedData();
    }

    private static SatelliteSavedData createLoadedData() {
        return new com.hbm.saveddata.SatelliteSavedData(DATA_NAME);
    }

    public static SatelliteSavedData get(ServerLevel level) {
        return WorldSavedDataHelper.get(level, DATA_NAME, SatelliteSavedData::load, SatelliteSavedData::createData);
    }

    public static Optional<SatelliteSavedData> get(Level level) {
        return WorldSavedDataHelper.get(level, DATA_NAME, SatelliteSavedData::load, SatelliteSavedData::createData);
    }

    public static SatelliteSavedData get(MinecraftServer server) {
        return WorldSavedDataHelper.get(server, DATA_NAME, SatelliteSavedData::load, SatelliteSavedData::createData);
    }

    public static Optional<SatelliteSavedData> get(MinecraftServer server, ResourceKey<Level> dimension) {
        return WorldSavedDataHelper.get(server, dimension, DATA_NAME, SatelliteSavedData::load,
                SatelliteSavedData::createData);
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
        return super.save(tag);
    }

    public void readFromNBT(NBTTagCompound tag) {
        boolean wasDirty = isDirty();
        NBTTagCompound legacyTag = Objects.requireNonNull(tag, "tag");
        LoadInspection inspection = inspectLoad(legacyTag);
        readLegacyEntriesFromTag(legacyTag);
        loadDiagnostics = inspection.toDiagnostics(false, 0);
        legacyEntryLoadDiagnostics = inspection.legacyEntryDiagnostics();
        modernEntryLoadDiagnostics = inspection.modernEntryDiagnostics();
        setDirty(wasDirty);
    }

    public void writeToNBT(NBTTagCompound tag) {
        Objects.requireNonNull(tag, "tag").setInteger(TAG_SAT_COUNT, satellites.size());
        int index = 0;
        for (Map.Entry<Integer, Satellite> entry : satellites.entrySet()) {
            Satellite satellite = entry.getValue();
            NBTTagCompound data = new NBTTagCompound();
            satellite.writeToNBT(data);

            tag.setInteger(TAG_SAT_ID + index, satellite.getID());
            tag.setTag(TAG_SAT_DATA + index, data);
            tag.setInteger(TAG_SAT_FREQ + index, entry.getKey());
            index++;
        }
    }

    private int readLegacyEntriesFromTag(NBTTagCompound tag) {
        int count = tag.getInteger(TAG_SAT_COUNT);
        for (int i = 0; i < count; i++) {
            LegacyLoadedEntry entry = readLegacyLoadedEntry(tag, i);
            satellites.put(entry.frequency(), entry.satellite());
        }
        return count;
    }

    private static LegacyLoadedEntry readLegacyLoadedEntry(NBTTagCompound tag, int index) {
        int legacyId = tag.getInteger(TAG_SAT_ID + index);
        Satellite satellite = Satellite.create(legacyId);
        satellite.readFromNBT((NBTTagCompound) tag.getTag(TAG_SAT_DATA + index));
        int frequency = tag.getInteger(TAG_SAT_FREQ + index);
        return new LegacyLoadedEntry(frequency, legacyId, satellite);
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
        if (satellite == null || type == null || Satellite.getTypeFromSatellite(satellite).orElse(null) != type) {
            return Optional.empty();
        }
        return Optional.of(satellite);
    }

    public Optional<Satellite> getSatelliteOptional(int frequency, Class<? extends Satellite> satelliteClass) {
        Satellite satellite = getSatFromFreq(frequency);
        return Satellite.matchesClass(satellite, satelliteClass) ? Optional.of(satellite) : Optional.empty();
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

    void putSatelliteForOrbit(int frequency, Satellite satellite) {
        if (satellite == null) {
            return;
        }
        ((DirtyTrackingSatelliteMap) sats).putWithoutDirty(frequency, satellite);
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
        int legacyId = Satellite.getLegacyIdFromClass(satelliteClass);
        return legacyId >= 0 && putSatelliteData(frequency, legacyId, data);
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
        int legacyId = Satellite.getLegacyIdFromClass(satelliteClass);
        return legacyId >= 0 && putSatellite(frequency, legacyId);
    }

    public boolean descendSatellite(int frequency) {
        return removeSatellite(frequency);
    }

    public boolean removeSatellite(int frequency) {
        if (satellites.containsKey(frequency)) {
            satellites.remove(frequency);
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
            if (frequency != null && satellites.containsKey(frequency)) {
                satellites.remove(frequency);
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
                .filter(entry -> entry.getValue() != null
                        && Satellite.getTypeFromSatellite(entry.getValue()).orElse(null) == type)
                .<Map.Entry<Integer, Satellite>>map(entry -> new AbstractMap.SimpleImmutableEntry<>(
                        entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<Map.Entry<Integer, Satellite>> entriesSnapshot(Class<? extends Satellite> satelliteClass) {
        if (satelliteClass == null) {
            return List.of();
        }
        return satellites.entrySet().stream()
                .filter(entry -> Satellite.matchesClass(entry.getValue(), satelliteClass))
                .<Map.Entry<Integer, Satellite>>map(entry -> new AbstractMap.SimpleImmutableEntry<>(
                        entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<Map.Entry<Integer, Satellite>> cargoEntriesSnapshot() {
        return satellites.entrySet().stream()
                .filter(entry -> Satellite.hasCargoPool(entry.getValue()))
                .<Map.Entry<Integer, Satellite>>map(entry -> new AbstractMap.SimpleImmutableEntry<>(
                        entry.getKey(), entry.getValue()))
                .toList();
    }

    public Set<Integer> frequenciesSnapshot() {
        return sortedFrequencySet(satellites.keySet());
    }

    public Set<Integer> frequenciesSnapshot(LegacySatelliteType type) {
        List<Integer> frequencies = new ArrayList<>();
        for (Map.Entry<Integer, Satellite> entry : entriesSnapshot(type)) {
            frequencies.add(entry.getKey());
        }
        return sortedFrequencySet(frequencies);
    }

    public Set<Integer> frequenciesSnapshot(Class<? extends Satellite> satelliteClass) {
        List<Integer> frequencies = new ArrayList<>();
        for (Map.Entry<Integer, Satellite> entry : entriesSnapshot(satelliteClass)) {
            frequencies.add(entry.getKey());
        }
        return sortedFrequencySet(frequencies);
    }

    public Set<Integer> cargoFrequenciesSnapshot() {
        List<Integer> frequencies = new ArrayList<>();
        for (Map.Entry<Integer, Satellite> entry : cargoEntriesSnapshot()) {
            frequencies.add(entry.getKey());
        }
        return sortedFrequencySet(frequencies);
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
        if (limit <= 0) {
            return List.of();
        }
        return frequenciesSnapshot(satelliteClass).stream().limit(limit).toList();
    }

    public List<Integer> cargoFrequenciesSnapshot(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return cargoFrequenciesSnapshot().stream().limit(limit).toList();
    }

    public Map<Integer, Satellite> satellitesSnapshot() {
        return java.util.Collections.unmodifiableMap(new HashMap<>(satellites));
    }

    public Map<Integer, Satellite> satellitesSnapshot(LegacySatelliteType type) {
        HashMap<Integer, Satellite> result = new HashMap<>();
        for (Map.Entry<Integer, Satellite> entry : entriesSnapshot(type)) {
            result.put(entry.getKey(), entry.getValue());
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    public Map<Integer, Satellite> satellitesSnapshot(Class<? extends Satellite> satelliteClass) {
        HashMap<Integer, Satellite> result = new HashMap<>();
        for (Map.Entry<Integer, Satellite> entry : entriesSnapshot(satelliteClass)) {
            result.put(entry.getKey(), entry.getValue());
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    public Map<Integer, Satellite> cargoSatellitesSnapshot() {
        HashMap<Integer, Satellite> result = new HashMap<>();
        for (Map.Entry<Integer, Satellite> entry : cargoEntriesSnapshot()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return java.util.Collections.unmodifiableMap(result);
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
                Satellite.getTypeFromSatellite(satellite)
                        .ifPresent(type -> counts.merge(type, 1, Integer::sum));
            }
        }
        return diagnosticMap(counts);
    }

    public int cargoSatelliteCount() {
        return cargoEntriesSnapshot().size();
    }

    public Map<LegacySatelliteType, Integer> cargoTypeCounts() {
        EnumMap<LegacySatelliteType, Integer> counts = new EnumMap<>(LegacySatelliteType.class);
        for (Satellite satellite : satellites.values()) {
            if (Satellite.hasCargoPool(satellite)) {
                Satellite.getTypeFromSatellite(satellite)
                        .ifPresent(type -> counts.merge(type, 1, Integer::sum));
            }
        }
        return diagnosticMap(counts);
    }

    public Map<String, Integer> cargoPoolCounts() {
        HashMap<String, Integer> counts = new HashMap<>();
        for (Satellite satellite : satellites.values()) {
            if (satellite != null) {
                satellite.cargoPool().ifPresent(pool -> counts.merge(pool, 1, Integer::sum));
            }
        }
        return diagnosticMap(counts);
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
                .sorted(frequencyEntryComparator())
                .limit(limit)
                .map(entry -> SatelliteSummary.of(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<SatelliteSummary> satelliteSummariesSnapshot(LegacySatelliteType type, int limit) {
        if (type == null || limit <= 0) {
            return List.of();
        }
        return satellites.entrySet().stream()
                .filter(entry -> entry.getValue() != null
                        && Satellite.getTypeFromSatellite(entry.getValue()).orElse(null) == type)
                .sorted(frequencyEntryComparator())
                .limit(limit)
                .map(entry -> SatelliteSummary.of(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<SatelliteSummary> satelliteSummariesSnapshot(Class<? extends Satellite> satelliteClass, int limit) {
        if (satelliteClass == null || limit <= 0) {
            return List.of();
        }
        return satellites.entrySet().stream()
                .filter(entry -> Satellite.matchesClass(entry.getValue(), satelliteClass))
                .sorted(frequencyEntryComparator())
                .limit(limit)
                .map(entry -> SatelliteSummary.of(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<SatelliteSummary> cargoSatelliteSummariesSnapshot(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return satellites.entrySet().stream()
                .filter(entry -> Satellite.hasCargoPool(entry.getValue()))
                .sorted(frequencyEntryComparator())
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
        if (clearExisting) {
            clearSatellites();
        }
        return readLegacyEntriesFromTag(NBTTagCompound.copyOf(tag));
    }

    public static void writeLegacyEntry(CompoundTag tag, int index, int frequency, Satellite satellite) {
        if (tag == null || satellite == null) {
            return;
        }
        tag.putInt(TAG_SAT_ID + index, satellite.getID());
        tag.put(TAG_SAT_DATA + index, satellite.saveData());
        tag.putInt(TAG_SAT_FREQ + index, frequency);
    }

    public static Optional<SatelliteEntry> readLegacyEntryTag(CompoundTag tag, int index) {
        if (tag == null) {
            return Optional.empty();
        }
        return Optional.of(readLegacyLoadedEntry(NBTTagCompound.copyOf(tag), index).entry());
    }

    public static List<SatelliteEntry> readLegacyEntriesTag(CompoundTag tag) {
        if (tag == null) {
            return List.of();
        }
        NBTTagCompound legacyTag = NBTTagCompound.copyOf(tag);
        List<SatelliteEntry> entries = new ArrayList<>();
        int count = legacyTag.getInteger(TAG_SAT_COUNT);
        for (int i = 0; i < count; i++) {
            entries.add(readLegacyLoadedEntry(legacyTag, i).entry());
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
                .map(entry -> new SatelliteEntry(entry.getKey(), entry.getValue().getID(),
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
                .map(entry -> new SatelliteEntry(entry.getKey(), entry.getValue().getID(),
                        entry.getValue().saveData()))
                .toList());
    }

    public SatelliteEntries satelliteEntriesSnapshot(LegacySatelliteType type) {
        return new SatelliteEntries(entriesSnapshot(type).stream()
                .map(entry -> new SatelliteEntry(entry.getKey(), entry.getValue().getID(),
                        entry.getValue().saveData()))
                .toList());
    }

    public SatelliteEntries satelliteEntriesSnapshot(Class<? extends Satellite> satelliteClass) {
        return new SatelliteEntries(entriesSnapshot(satelliteClass).stream()
                .map(entry -> new SatelliteEntry(entry.getKey(), entry.getValue().getID(),
                        entry.getValue().saveData()))
                .toList());
    }

    public SatelliteEntries cargoSatelliteEntriesSnapshot() {
        return new SatelliteEntries(cargoEntriesSnapshot().stream()
                .map(entry -> new SatelliteEntry(entry.getKey(), entry.getValue().getID(),
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
        tag.putInt(TAG_LEGACY_ID, satellite.getID());
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
        private boolean markIfChanged(boolean changed) {
            if (changed) {
                setDirty();
            }
            return changed;
        }

        private Satellite putWithoutDirty(Integer key, Satellite value) {
            return super.put(key, value);
        }

        @Override
        public Satellite put(Integer key, Satellite value) {
            boolean hadKey = containsKey(key);
            Satellite previous = super.put(key, value);
            if (!hadKey || previous != value) {
                setDirty();
            }
            return previous;
        }

        @Override
        public Satellite putIfAbsent(Integer key, Satellite value) {
            boolean hadKey = containsKey(key);
            Satellite previous = super.get(key);
            Satellite result = super.putIfAbsent(key, value);
            if (hadKey != containsKey(key) || previous != get(key)) {
                setDirty();
            }
            return result;
        }

        @Override
        public Satellite remove(Object key) {
            boolean hadKey = containsKey(key);
            Satellite previous = super.remove(key);
            if (hadKey) {
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
            boolean hadKey = containsKey(key);
            Satellite previous = super.replace(key, value);
            if (hadKey && previous != value) {
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
            Satellite previous = super.get(key);
            Satellite result = super.computeIfAbsent(key, mappingFunction);
            if (hadKey != containsKey(key) || previous != result) {
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

        @Override
        public Set<Integer> keySet() {
            Set<Integer> delegate = super.keySet();
            return new AbstractSet<>() {
                @Override
                public Iterator<Integer> iterator() {
                    Iterator<Integer> iterator = delegate.iterator();
                    return new Iterator<>() {
                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }

                        @Override
                        public Integer next() {
                            return iterator.next();
                        }

                        @Override
                        public void remove() {
                            iterator.remove();
                            setDirty();
                        }
                    };
                }

                @Override
                public int size() {
                    return delegate.size();
                }

                @Override
                public boolean contains(Object object) {
                    return delegate.contains(object);
                }

                @Override
                public boolean remove(Object object) {
                    return markIfChanged(delegate.remove(object));
                }

                @Override
                public boolean removeAll(Collection<?> collection) {
                    return markIfChanged(delegate.removeAll(collection));
                }

                @Override
                public boolean retainAll(Collection<?> collection) {
                    return markIfChanged(delegate.retainAll(collection));
                }

                @Override
                public void clear() {
                    DirtyTrackingSatelliteMap.this.clear();
                }
            };
        }

        @Override
        public Collection<Satellite> values() {
            Collection<Satellite> delegate = super.values();
            return new AbstractCollection<>() {
                @Override
                public Iterator<Satellite> iterator() {
                    Iterator<Satellite> iterator = delegate.iterator();
                    return new Iterator<>() {
                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }

                        @Override
                        public Satellite next() {
                            return iterator.next();
                        }

                        @Override
                        public void remove() {
                            iterator.remove();
                            setDirty();
                        }
                    };
                }

                @Override
                public int size() {
                    return delegate.size();
                }

                @Override
                public boolean contains(Object object) {
                    return delegate.contains(object);
                }

                @Override
                public boolean remove(Object object) {
                    return markIfChanged(delegate.remove(object));
                }

                @Override
                public boolean removeAll(Collection<?> collection) {
                    return markIfChanged(delegate.removeAll(collection));
                }

                @Override
                public boolean retainAll(Collection<?> collection) {
                    return markIfChanged(delegate.retainAll(collection));
                }

                @Override
                public void clear() {
                    DirtyTrackingSatelliteMap.this.clear();
                }
            };
        }

        @Override
        public Set<Map.Entry<Integer, Satellite>> entrySet() {
            Set<Map.Entry<Integer, Satellite>> delegate = super.entrySet();
            return new AbstractSet<>() {
                @Override
                public Iterator<Map.Entry<Integer, Satellite>> iterator() {
                    Iterator<Map.Entry<Integer, Satellite>> iterator = delegate.iterator();
                    return new Iterator<>() {
                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }

                        @Override
                        public Map.Entry<Integer, Satellite> next() {
                            Map.Entry<Integer, Satellite> entry = iterator.next();
                            return new AbstractMap.SimpleEntry<>(entry) {
                                @Override
                                public Satellite setValue(Satellite value) {
                                    Satellite previous = entry.setValue(value);
                                    if (previous != value) {
                                        setDirty();
                                    }
                                    super.setValue(value);
                                    return previous;
                                }
                            };
                        }

                        @Override
                        public void remove() {
                            iterator.remove();
                            setDirty();
                        }
                    };
                }

                @Override
                public int size() {
                    return delegate.size();
                }

                @Override
                public boolean contains(Object object) {
                    return delegate.contains(object);
                }

                @Override
                public boolean remove(Object object) {
                    return markIfChanged(delegate.remove(object));
                }

                @Override
                public boolean removeAll(Collection<?> collection) {
                    return markIfChanged(delegate.removeAll(collection));
                }

                @Override
                public boolean retainAll(Collection<?> collection) {
                    return markIfChanged(delegate.retainAll(collection));
                }

                @Override
                public void clear() {
                    DirtyTrackingSatelliteMap.this.clear();
                }
            };
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

    private static Set<Integer> sortedFrequencySet(Collection<Integer> frequencies) {
        List<Integer> sorted = new ArrayList<>(frequencies);
        sorted.sort(Comparator.nullsFirst(Comparator.naturalOrder()));
        return java.util.Collections.unmodifiableSet(new LinkedHashSet<>(sorted));
    }

    private static Comparator<Map.Entry<Integer, Satellite>> frequencyEntryComparator() {
        return Map.Entry.comparingByKey(Comparator.nullsFirst(Comparator.naturalOrder()));
    }

    private record LegacyLoadedEntry(int frequency, int legacyId, Satellite satellite) {
        private SatelliteEntry entry() {
            return new SatelliteEntry(frequency, satellite.getID(), satellite.saveData());
        }
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
            return Optional.of(new SatelliteEntry(frequency, satellite.getID(), satellite.saveData()));
        }

        public static Optional<SatelliteEntry> of(int frequency, LegacySatelliteType type, CompoundTag data) {
            if (type == null) {
                return Optional.empty();
            }
            return Optional.of(new SatelliteEntry(frequency, type, data));
        }

        public static Optional<SatelliteEntry> of(int frequency, Class<? extends Satellite> satelliteClass,
                                                  CompoundTag data) {
            int legacyId = Satellite.getLegacyIdFromClass(satelliteClass);
            return legacyId >= 0 ? Optional.of(new SatelliteEntry(frequency, legacyId, data)) : Optional.empty();
        }

        public Optional<Satellite> satellite() {
            return Optional.ofNullable(Satellite.load(legacyId, data.copy()));
        }

        public Optional<LegacySatelliteType> type() {
            return satelliteClass().flatMap(Satellite::getTypeFromClass);
        }

        public Optional<Class<? extends Satellite>> satelliteClass() {
            return Satellite.getClassFromLegacyId(legacyId);
        }

        public boolean matches(LegacySatelliteType type) {
            return type != null && type().filter(candidate -> candidate == type).isPresent();
        }

        public boolean matches(Class<? extends Satellite> satelliteClass) {
            int legacyId = Satellite.getLegacyIdFromClass(satelliteClass);
            return legacyId >= 0 && this.legacyId == legacyId;
        }

        public Optional<String> cargoPool() {
            return satelliteClass().flatMap(Satellite::getCargoPoolFromClass);
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
                            satelliteClass().flatMap(Satellite::getCargoPoolFromClass), 0L));
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
            if (satelliteClass == null) {
                return List.of();
            }
            return entries.stream()
                    .filter(entry -> entry.matches(satelliteClass))
                    .toList();
        }

        public SatelliteEntries filter(LegacySatelliteType type) {
            return new SatelliteEntries(entries(type));
        }

        public SatelliteEntries filter(Class<? extends Satellite> satelliteClass) {
            return new SatelliteEntries(entries(satelliteClass));
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
            List<Integer> frequencies = new ArrayList<>();
            for (SatelliteEntry entry : entries) {
                frequencies.add(entry.frequency());
            }
            return sortedFrequencySet(frequencies);
        }

        public Set<Integer> frequencies(LegacySatelliteType type) {
            List<Integer> frequencies = new ArrayList<>();
            for (SatelliteEntry entry : entries(type)) {
                frequencies.add(entry.frequency());
            }
            return sortedFrequencySet(frequencies);
        }

        public Set<Integer> frequencies(Class<? extends Satellite> satelliteClass) {
            List<Integer> frequencies = new ArrayList<>();
            for (SatelliteEntry entry : entries(satelliteClass)) {
                frequencies.add(entry.frequency());
            }
            return sortedFrequencySet(frequencies);
        }

        public Set<Integer> cargoFrequencies() {
            List<Integer> frequencies = new ArrayList<>();
            for (SatelliteEntry entry : cargoEntries()) {
                frequencies.add(entry.frequency());
            }
            return sortedFrequencySet(frequencies);
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
            if (limit <= 0) {
                return List.of();
            }
            return frequencies(satelliteClass).stream().limit(limit).toList();
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
            return diagnosticMap(counts);
        }

        public int cargoSatelliteCount() {
            return cargoEntries().size();
        }

        public Map<LegacySatelliteType, Integer> cargoTypeCounts() {
            EnumMap<LegacySatelliteType, Integer> counts = new EnumMap<>(LegacySatelliteType.class);
            for (SatelliteEntry entry : cargoEntries()) {
                entry.type().ifPresent(type -> counts.merge(type, 1, Integer::sum));
            }
            return diagnosticMap(counts);
        }

        public Map<String, Integer> cargoPoolCounts() {
            HashMap<String, Integer> counts = new HashMap<>();
            for (SatelliteEntry entry : entries) {
                entry.cargoPool().ifPresent(pool -> counts.merge(pool, 1, Integer::sum));
            }
            return diagnosticMap(counts);
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
            if (limit <= 0) {
                return List.of();
            }
            return entries(satelliteClass).stream()
                    .sorted(java.util.Comparator.comparingInt(SatelliteEntry::frequency))
                    .limit(limit)
                    .map(SatelliteEntry::summary)
                    .toList();
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
            SatelliteSavedData data = createData();
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
            typeCounts = diagnosticMap(typeCounts);
            cargoTypeCounts = diagnosticMap(cargoTypeCounts);
            cargoPoolCounts = diagnosticMap(cargoPoolCounts);
            frequencies = diagnosticList(frequencies);
            cargoFrequencies = diagnosticList(cargoFrequencies);
            satellites = diagnosticList(satellites);
            cargoSatellites = diagnosticList(cargoSatellites);
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
                    .map(summary -> summary == null ? "null" : summary.detail())
                    .toList();
        }

        private List<String> cargoSatelliteDetails() {
            return cargoSatellites.stream()
                    .map(summary -> summary == null ? "null" : summary.detail())
                    .toList();
        }
    }

    public record SatelliteSummary(Integer frequency, int legacyId, String legacyName,
                                   Satellite.SatelliteInterface satelliteInterface,
                                   Set<Satellite.InterfaceAction> interfaceActions,
                                   Set<Satellite.CoordAction> coordActions,
                                   Optional<String> cargoPool, long lastOperationMillis) {
        public SatelliteSummary {
            legacyName = legacyName == null ? "" : legacyName;
            satelliteInterface = satelliteInterface == null ? Satellite.SatelliteInterface.NONE : satelliteInterface;
            interfaceActions = diagnosticSet(interfaceActions);
            coordActions = diagnosticSet(coordActions);
            cargoPool = cargoPool == null ? Optional.empty() : cargoPool;
        }

        private static SatelliteSummary of(Integer frequency, Satellite satellite) {
            if (satellite == null) {
                return new SatelliteSummary(frequency, -1, "",
                        Satellite.SatelliteInterface.NONE, Set.of(), Set.of(), Optional.empty(), 0L);
            }
            return new SatelliteSummary(frequency, satellite.getID(), satellite.legacyName(),
                    satellite.satelliteInterface(), satellite.interfaceActions(), satellite.coordActions(),
                    satellite.cargoPool(), satellite.lastOperationMillis());
        }

        public Optional<LegacySatelliteType> type() {
            return satelliteClass().flatMap(Satellite::getTypeFromClass);
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

    private record LoadInspection(boolean hasLegacyCountTag, int legacyEntriesRead, int legacyEntriesLoaded,
                                  int legacyMissingIds, int legacyMissingData, int legacyMissingFrequencies,
                                  int legacyUnknownIds, int legacyDuplicateFrequencies,
                                  boolean hasModernEntriesTag, int modernEntriesRead, int modernEntriesLoaded,
                                  int modernMissingIds, int modernMissingData, int modernMissingFrequencies,
                                  int modernUnknownIds, int modernDuplicateFrequencies,
                                  List<EntryLoadDiagnostics> legacyEntryDiagnostics,
                                  List<EntryLoadDiagnostics> modernEntryDiagnostics) {
        private LoadDiagnostics toDiagnostics(boolean usedModernEntriesFallback, int actualModernEntriesLoaded) {
            return new LoadDiagnostics(hasLegacyCountTag, legacyEntriesRead, legacyEntriesLoaded, legacyMissingIds,
                    legacyMissingData, legacyMissingFrequencies, legacyUnknownIds, legacyDuplicateFrequencies,
                    hasModernEntriesTag, usedModernEntriesFallback, modernEntriesRead, actualModernEntriesLoaded,
                    modernMissingIds, modernMissingData, modernMissingFrequencies, modernUnknownIds,
                    modernDuplicateFrequencies);
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
