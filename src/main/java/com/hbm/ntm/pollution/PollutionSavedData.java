package com.hbm.ntm.pollution;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PollutionSavedData extends SavedData {
    public static final String DATA_NAME = "hbmpollution";
    public static final String MODERN_COMPAT_DATA_NAME = "hbm_pollution";
    public static final String TAG_PERMA_SYNC = "pollution";
    public static final String TAG_SOOT = "soot";
    public static final String TAG_POISON = "poison";
    public static final String TAG_HEAVYMETAL = "heavymetal";
    public static final String TAG_FALLOUT = "fallout";
    private static final String TAG_ENTRIES = "entries";
    private static final String TAG_CHUNK_X = "chunkX";
    private static final String TAG_CHUNK_Z = "chunkZ";

    private final Map<Long, PollutionSample> pollution = new HashMap<>();
    private LoadDiagnostics loadDiagnostics = LoadDiagnostics.empty();

    public static PollutionSavedData load(CompoundTag tag) {
        PollutionSavedData data = new PollutionSavedData();
        if (tag == null) {
            return data;
        }
        ListTag entries = tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            PollutionGridPos pos = new PollutionGridPos(entry.getInt(TAG_CHUNK_X), entry.getInt(TAG_CHUNK_Z));
            PollutionSample sample = PollutionSample.fromTag(entry);
            if (sample.hasAnyStoredValue()) {
                data.pollution.put(pos.toLong(), sample);
            }
        }
        data.loadDiagnostics = LoadDiagnostics.inspect(tag);
        return data;
    }

    public static boolean hasLegacyRootEntries(CompoundTag tag) {
        return tag != null && tag.contains(TAG_ENTRIES, Tag.TAG_LIST);
    }

    public static String tagName(PollutionType type) {
        if (type == null) {
            return "";
        }
        return switch (type) {
            case SOOT -> TAG_SOOT;
            case POISON -> TAG_POISON;
            case HEAVYMETAL -> TAG_HEAVYMETAL;
            case FALLOUT -> TAG_FALLOUT;
        };
    }

    public static CompoundTag writeSampleTag(PollutionSample sample) {
        CompoundTag tag = new CompoundTag();
        if (sample != null) {
            sample.toTag(tag);
        }
        return tag;
    }

    public static void appendPermaSyncData(CompoundTag data, PollutionSample sample) {
        if (data == null) {
            return;
        }
        data.put(TAG_PERMA_SYNC, writeSampleTag(sample));
    }

    public static PollutionSample readPermaSyncData(CompoundTag data) {
        CompoundTag source = data == null ? new CompoundTag() : data.getCompound(TAG_PERMA_SYNC);
        return PollutionSample.fromTag(source);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        for (Map.Entry<Long, PollutionSample> entry : pollution.entrySet()) {
            PollutionSample sample = entry.getValue().copy();
            if (!sample.hasAnyStoredValue()) {
                continue;
            }
            PollutionGridPos pos = PollutionGridPos.of(entry.getKey());
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt(TAG_CHUNK_X, pos.x());
            entryTag.putInt(TAG_CHUNK_Z, pos.z());
            sample.toTag(entryTag);
            entries.add(entryTag);
        }
        tag.put(TAG_ENTRIES, entries);
        return tag;
    }

    public PollutionSample get(PollutionGridPos pos) {
        if (pos == null) {
            return new PollutionSample();
        }
        PollutionSample sample = pollution.get(pos.toLong());
        return sample == null ? new PollutionSample() : sample.copy();
    }

    @Nullable
    public PollutionSample getOrNull(PollutionGridPos pos) {
        if (pos == null) {
            return null;
        }
        PollutionSample sample = pollution.get(pos.toLong());
        return sample == null ? null : sample.copy();
    }

    public float get(PollutionGridPos pos, PollutionType type) {
        if (pos == null || type == null) {
            return 0.0F;
        }
        PollutionSample sample = pollution.get(pos.toLong());
        return sample == null ? 0.0F : sample.get(type);
    }

    public void set(PollutionGridPos pos, PollutionType type, float value) {
        if (pos == null || type == null) {
            return;
        }
        PollutionSample sample = pollution.computeIfAbsent(pos.toLong(), key -> new PollutionSample());
        sample.set(type, value);
        if (!sample.hasAnyStoredValue()) {
            pollution.remove(pos.toLong());
        }
        setDirty();
    }

    public void set(PollutionGridPos pos, PollutionSample value) {
        if (pos == null) {
            return;
        }
        PollutionSample sample = value == null ? new PollutionSample() : value.copy();
        if (sample.hasAnyStoredValue()) {
            pollution.put(pos.toLong(), sample);
        } else {
            pollution.remove(pos.toLong());
        }
        setDirty();
    }

    public void add(PollutionGridPos pos, PollutionType type, float amount) {
        if (pos == null || type == null || amount == 0.0F) {
            return;
        }
        PollutionSample sample = pollution.computeIfAbsent(pos.toLong(), key -> new PollutionSample());
        sample.addClamped(type, amount);
        if (!sample.hasAnyPollution()) {
            pollution.remove(pos.toLong());
        }
        setDirty();
    }

    public void addClamped(PollutionGridPos pos, PollutionSample amounts) {
        if (pos == null || amounts == null || !amounts.hasAnyStoredValue()) {
            return;
        }
        PollutionSample sample = pollution.computeIfAbsent(pos.toLong(), key -> new PollutionSample());
        for (PollutionType type : PollutionType.orderedValues()) {
            sample.addClamped(type, amounts.get(type));
        }
        if (!sample.hasAnyPollution()) {
            pollution.remove(pos.toLong());
        }
        setDirty();
    }

    public Map<PollutionGridPos, PollutionSample> pollutionSnapshot() {
        Map<PollutionGridPos, PollutionSample> snapshot = new HashMap<>();
        for (Map.Entry<Long, PollutionSample> entry : pollution.entrySet()) {
            snapshot.put(PollutionGridPos.of(entry.getKey()), entry.getValue().copy());
        }
        return snapshot;
    }

    public void replaceAll(Map<PollutionGridPos, PollutionSample> values) {
        pollution.clear();
        if (values != null) {
            for (Map.Entry<PollutionGridPos, PollutionSample> entry : values.entrySet()) {
                PollutionGridPos pos = entry.getKey();
                PollutionSample sample = entry.getValue() == null ? new PollutionSample() : entry.getValue().copy();
                if (pos != null && sample.hasAnyStoredValue()) {
                    pollution.put(pos.toLong(), sample);
                }
            }
        }
        setDirty();
    }

    public void addClamped(Map<PollutionGridPos, PollutionSample> amounts) {
        if (amounts == null || amounts.isEmpty()) {
            return;
        }
        for (Map.Entry<PollutionGridPos, PollutionSample> entry : amounts.entrySet()) {
            addClamped(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        if (!pollution.isEmpty()) {
            pollution.clear();
            setDirty();
        }
    }

    public List<Map.Entry<Long, PollutionSample>> entriesSnapshot() {
        List<Map.Entry<Long, PollutionSample>> entries = new ArrayList<>();
        for (Map.Entry<Long, PollutionSample> entry : pollution.entrySet()) {
            entries.add(new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entry.getValue().copy()));
        }
        return entries;
    }

    public List<EntrySnapshot> gridEntriesSnapshot() {
        List<EntrySnapshot> entries = new ArrayList<>();
        for (Map.Entry<Long, PollutionSample> entry : pollution.entrySet()) {
            entries.add(new EntrySnapshot(PollutionGridPos.of(entry.getKey()), entry.getValue().copy()));
        }
        return entries;
    }

    public List<EntrySnapshot> positiveEntriesSnapshot(int limit) {
        return positiveEntriesSnapshotFrom(pollution, null, limit);
    }

    public List<EntrySnapshot> positiveEntriesSnapshot(@Nullable PollutionType type, int limit) {
        return positiveEntriesSnapshotFrom(pollution, type, limit);
    }

    private static List<EntrySnapshot> positiveEntriesSnapshotFrom(Map<Long, PollutionSample> source,
                                                                   @Nullable PollutionType type,
                                                                   int limit) {
        return source.entrySet().stream()
                .map(entry -> new EntrySnapshot(PollutionGridPos.of(entry.getKey()), entry.getValue().copy()))
                .filter(entry -> type == null ? entry.hasAnyPollution() : entry.get(type) > 0.0F)
                .sorted((left, right) -> type == null
                        ? EntrySnapshot.compareTotalDescending(left, right)
                        : EntrySnapshot.compareTypeDescending(left, right, type))
                .limit(Math.max(0, limit))
                .toList();
    }

    public DiffusionPreview previewDiffusion(PollutionGridPredicate predicate) {
        Map<Long, PollutionSample> next = computeDiffusion();
        return new DiffusionPreview(stats(pollution, predicate), stats(next, predicate));
    }

    public List<EntrySnapshot> positiveDiffusionPreviewEntries(int limit) {
        return positiveEntriesSnapshotFrom(computeDiffusion(), null, limit);
    }

    public List<EntrySnapshot> positiveDiffusionPreviewEntries(@Nullable PollutionType type, int limit) {
        return positiveEntriesSnapshotFrom(computeDiffusion(), type, limit);
    }

    public List<EntryDelta> diffusionDeltaEntries(int limit) {
        return diffusionDeltaEntries(computeDiffusion(), null, limit);
    }

    public List<EntryDelta> diffusionDeltaEntries(@Nullable PollutionType type, int limit) {
        return diffusionDeltaEntries(computeDiffusion(), type, limit);
    }

    public EntryDelta diffusionDeltaAt(PollutionGridPos pos) {
        return diffusionDeltaAt(computeDiffusion(), pos);
    }

    public List<EntryDelta> diffusionNeighborDeltas(PollutionGridPos center) {
        if (center == null) {
            return List.of();
        }
        Map<Long, PollutionSample> after = computeDiffusion();
        List<EntryDelta> entries = new ArrayList<>();
        entries.add(diffusionDeltaAt(after, center));
        for (PollutionGridPos neighbor : center.cardinalNeighbors()) {
            entries.add(diffusionDeltaAt(after, neighbor));
        }
        return entries;
    }

    public void updateDiffusion() {
        if (pollution.isEmpty()) {
            return;
        }

        Map<Long, PollutionSample> next = computeDiffusion();
        pollution.clear();
        for (Map.Entry<Long, PollutionSample> entry : next.entrySet()) {
            PollutionSample sample = entry.getValue().copy();
            if (sample.hasAnyPollution()) {
                pollution.put(entry.getKey(), sample);
            }
        }
        setDirty();
    }

    private Map<Long, PollutionSample> computeDiffusion() {
        Map<Long, PollutionSample> next = new HashMap<>();
        for (Map.Entry<Long, PollutionSample> entry : pollution.entrySet()) {
            PollutionGridPos pos = PollutionGridPos.of(entry.getKey());
            PollutionSample current = entry.getValue().copy();
            PollutionSample spread = new PollutionSample();

            float soot = current.get(PollutionType.SOOT);
            if (soot > 10.0F) {
                spread.set(PollutionType.SOOT, soot * 0.05F);
                current.set(PollutionType.SOOT, soot * 0.8F);
            }
            current.set(PollutionType.SOOT, current.get(PollutionType.SOOT) * 0.99F);
            current.set(PollutionType.HEAVYMETAL, current.get(PollutionType.HEAVYMETAL) * 0.9995F);

            float poison = current.get(PollutionType.POISON);
            if (poison > 10.0F) {
                spread.set(PollutionType.POISON, poison * 0.025F);
                current.set(PollutionType.POISON, poison * 0.9F);
            } else {
                current.set(PollutionType.POISON, poison * 0.995F);
            }

            merge(next, pos, current);
            if (spread.hasAnyPollution()) {
                for (PollutionGridPos neighbor : pos.cardinalNeighbors()) {
                    merge(next, neighbor, spread);
                }
            }
        }
        return next;
    }

    private List<EntryDelta> diffusionDeltaEntries(Map<Long, PollutionSample> after, @Nullable PollutionType type,
                                                   int limit) {
        Map<Long, PollutionSample> before = pollution;
        List<EntryDelta> entries = new ArrayList<>();
        for (Map.Entry<Long, PollutionSample> entry : before.entrySet()) {
            long key = entry.getKey();
            PollutionSample beforeSample = entry.getValue().copy();
            PollutionSample afterSample = after.getOrDefault(key, new PollutionSample()).copy();
            EntryDelta delta = EntryDelta.of(PollutionGridPos.of(key), beforeSample, afterSample);
            if (type == null ? delta.hasAnyDelta() : delta.hasDelta(type)) {
                entries.add(delta);
            }
        }
        for (Map.Entry<Long, PollutionSample> entry : after.entrySet()) {
            if (before.containsKey(entry.getKey())) {
                continue;
            }
            EntryDelta delta = EntryDelta.of(PollutionGridPos.of(entry.getKey()), new PollutionSample(),
                    entry.getValue().copy());
            if (type == null ? delta.hasAnyDelta() : delta.hasDelta(type)) {
                entries.add(delta);
            }
        }
        entries.sort((left, right) -> type == null
                ? Float.compare(right.absoluteTotalDelta(), left.absoluteTotalDelta())
                : Float.compare(right.absoluteTypeDelta(type), left.absoluteTypeDelta(type)));
        return entries.stream().limit(Math.max(0, limit)).toList();
    }

    private EntryDelta diffusionDeltaAt(Map<Long, PollutionSample> after, PollutionGridPos pos) {
        PollutionGridPos safePos = pos == null ? new PollutionGridPos(0, 0) : pos;
        PollutionSample beforeSample = pollution.getOrDefault(safePos.toLong(), new PollutionSample()).copy();
        PollutionSample afterSample = after.getOrDefault(safePos.toLong(), new PollutionSample()).copy();
        return EntryDelta.of(safePos, beforeSample, afterSample);
    }

    private static void merge(Map<Long, PollutionSample> target, PollutionGridPos pos, PollutionSample source) {
        if (!source.hasAnyPollution()) {
            return;
        }
        PollutionSample sample = target.computeIfAbsent(pos.toLong(), key -> new PollutionSample());
        for (PollutionType type : PollutionType.orderedValues()) {
            sample.add(type, source.get(type));
        }
    }

    public int pruneLoaded(PollutionGridPredicate predicate) {
        if (predicate == null) {
            return 0;
        }
        boolean changed = false;
        int removed = 0;
        Iterator<Map.Entry<Long, PollutionSample>> iterator = pollution.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, PollutionSample> entry = iterator.next();
            if (!predicate.isLoaded(PollutionGridPos.of(entry.getKey()))) {
                iterator.remove();
                removed++;
                changed = true;
            }
        }
        if (changed) {
            setDirty();
        }
        return removed;
    }

    public Stats stats(PollutionGridPredicate predicate) {
        return stats(pollution, predicate);
    }

    private static Stats stats(Map<Long, PollutionSample> source, PollutionGridPredicate predicate) {
        int total = 0;
        int loaded = 0;
        int positive = 0;
        int loadedPositive = 0;
        int stored = 0;
        int loadedStored = 0;
        PollutionGridPos minGrid = null;
        PollutionGridPos maxGrid = null;
        PollutionGridPos loadedMinGrid = null;
        PollutionGridPos loadedMaxGrid = null;
        float totalPollution = 0.0F;
        float loadedPollution = 0.0F;
        float maxPollution = 0.0F;
        float loadedMaxPollution = 0.0F;
        float[] totals = new float[PollutionType.count()];
        float[] loadedTotals = new float[PollutionType.count()];

        for (Map.Entry<Long, PollutionSample> entry : source.entrySet()) {
            PollutionGridPos pos = PollutionGridPos.of(entry.getKey());
            PollutionSample sample = entry.getValue();
            float sum = sample.sum();
            total++;
            minGrid = min(minGrid, pos);
            maxGrid = max(maxGrid, pos);
            if (sample.hasAnyPollution()) {
                positive++;
            }
            if (sample.hasAnyStoredValue()) {
                stored++;
            }
            totalPollution += sum;
            maxPollution = Math.max(maxPollution, sample.max());
            for (PollutionType type : PollutionType.orderedValues()) {
                totals[type.ordinal()] += sample.get(type);
            }

            if (predicate != null && predicate.isLoaded(pos)) {
                loaded++;
                loadedMinGrid = min(loadedMinGrid, pos);
                loadedMaxGrid = max(loadedMaxGrid, pos);
                if (sample.hasAnyPollution()) {
                    loadedPositive++;
                }
                if (sample.hasAnyStoredValue()) {
                    loadedStored++;
                }
                loadedPollution += sum;
                loadedMaxPollution = Math.max(loadedMaxPollution, sample.max());
                for (PollutionType type : PollutionType.orderedValues()) {
                    loadedTotals[type.ordinal()] += sample.get(type);
                }
            }
        }

        return new Stats(total, loaded, totalPollution, loadedPollution, maxPollution, loadedMaxPollution,
                totals, loadedTotals, positive, loadedPositive, stored, loadedStored, minGrid, maxGrid,
                loadedMinGrid, loadedMaxGrid);
    }

    @Nullable
    private static PollutionGridPos min(@Nullable PollutionGridPos first, PollutionGridPos second) {
        if (first == null) {
            return second;
        }
        return new PollutionGridPos(Math.min(first.x(), second.x()), Math.min(first.z(), second.z()));
    }

    @Nullable
    private static PollutionGridPos max(@Nullable PollutionGridPos first, PollutionGridPos second) {
        if (first == null) {
            return second;
        }
        return new PollutionGridPos(Math.max(first.x(), second.x()), Math.max(first.z(), second.z()));
    }

    public boolean isEmpty() {
        return pollution.isEmpty();
    }

    public LoadDiagnostics loadDiagnostics() {
        return loadDiagnostics;
    }

    public record LoadDiagnostics(boolean hasEntriesTag, int entries, int missingCoordinates,
                                  int nonFiniteValues, int duplicateCells) {
        public static LoadDiagnostics empty() {
            return new LoadDiagnostics(false, 0, 0, 0, 0);
        }

        public static LoadDiagnostics inspect(CompoundTag tag) {
            if (tag == null) {
                return empty();
            }
            boolean hasEntries = tag.contains(TAG_ENTRIES, Tag.TAG_LIST);
            ListTag entries = tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
            int missingCoordinates = 0;
            int nonFiniteValues = 0;
            int duplicateCells = 0;
            Set<Long> seen = new HashSet<>();
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag entry = entries.getCompound(i);
                if (!entry.contains(TAG_CHUNK_X) || !entry.contains(TAG_CHUNK_Z)) {
                    missingCoordinates++;
                }
                long key = new PollutionGridPos(entry.getInt(TAG_CHUNK_X), entry.getInt(TAG_CHUNK_Z)).toLong();
                if (!seen.add(key)) {
                    duplicateCells++;
                }
                for (PollutionType type : PollutionType.orderedValues()) {
                    String name = tagName(type);
                    if (entry.contains(name) && !Float.isFinite(entry.getFloat(name))) {
                        nonFiniteValues++;
                    }
                }
            }
            return new LoadDiagnostics(hasEntries, entries.size(), missingCoordinates, nonFiniteValues,
                    duplicateCells);
        }

        public int problemCount() {
            return (hasEntriesTag ? 0 : 1) + missingCoordinates + nonFiniteValues + duplicateCells;
        }

        public boolean clean() {
            return problemCount() == 0;
        }

        public List<String> issues() {
            List<String> issues = new ArrayList<>();
            if (!hasEntriesTag) {
                issues.add("missing_entries");
            }
            if (missingCoordinates > 0) {
                issues.add("missing_coordinates=" + missingCoordinates);
            }
            if (nonFiniteValues > 0) {
                issues.add("non_finite_values=" + nonFiniteValues);
            }
            if (duplicateCells > 0) {
                issues.add("duplicate_cells=" + duplicateCells);
            }
            return List.copyOf(issues);
        }

        public String summary() {
            return "hasEntries=" + hasEntriesTag
                    + " entries=" + entries
                    + " missingCoordinates=" + missingCoordinates
                    + " nonFiniteValues=" + nonFiniteValues
                    + " duplicateCells=" + duplicateCells
                    + " problems=" + problemCount()
                    + " issues=" + issues()
                    + " clean=" + clean();
        }
    }

    @FunctionalInterface
    public interface PollutionGridPredicate {
        boolean isLoaded(PollutionGridPos pos);
    }

    public record DiffusionPreview(Stats before, Stats after) {
        public static DiffusionPreview empty() {
            Stats empty = Stats.empty();
            return new DiffusionPreview(empty, empty);
        }

        public int entryDelta() {
            return (after == null ? 0 : after.totalEntries()) - (before == null ? 0 : before.totalEntries());
        }

        public float totalPollutionDelta() {
            return (after == null ? 0.0F : after.totalPollution())
                    - (before == null ? 0.0F : before.totalPollution());
        }
    }

    public record EntryDelta(PollutionGridPos pos, PollutionSample before, PollutionSample after,
                             PollutionSample delta) {
        public static EntryDelta of(PollutionGridPos pos, PollutionSample before, PollutionSample after) {
            PollutionSample safeBefore = before == null ? new PollutionSample() : before.copy();
            PollutionSample safeAfter = after == null ? new PollutionSample() : after.copy();
            PollutionSample delta = new PollutionSample();
            for (PollutionType type : PollutionType.orderedValues()) {
                delta.set(type, safeAfter.get(type) - safeBefore.get(type));
            }
            return new EntryDelta(pos, safeBefore, safeAfter, delta);
        }

        public float totalBefore() {
            return before == null ? 0.0F : before.sum();
        }

        public float totalAfter() {
            return after == null ? 0.0F : after.sum();
        }

        public float totalDelta() {
            return totalAfter() - totalBefore();
        }

        public float absoluteTotalDelta() {
            return Math.abs(totalDelta());
        }

        public boolean hasAnyDelta() {
            return delta != null && delta.hasAnyStoredValue();
        }

        public boolean hasDelta(PollutionType type) {
            return type != null && delta != null && delta.get(type) != 0.0F;
        }

        public float typeBefore(PollutionType type) {
            return before == null ? 0.0F : before.get(type);
        }

        public float typeAfter(PollutionType type) {
            return after == null ? 0.0F : after.get(type);
        }

        public float typeDelta(PollutionType type) {
            return delta == null ? 0.0F : delta.get(type);
        }

        public float absoluteTypeDelta(PollutionType type) {
            return Math.abs(typeDelta(type));
        }

        public String formatDeltaValues() {
            return delta == null ? new PollutionSample().formatValues() : delta.formatValues();
        }
    }

    public record EntrySnapshot(PollutionGridPos pos, PollutionSample sample) {
        public float totalPollution() {
            return sample == null ? 0.0F : sample.sum();
        }

        public float maxPollution() {
            return sample == null ? 0.0F : sample.max();
        }

        public boolean hasAnyPollution() {
            return sample != null && sample.hasAnyPollution();
        }

        public boolean hasAnyStoredValue() {
            return sample != null && sample.hasAnyStoredValue();
        }

        public float get(PollutionType type) {
            return sample == null ? 0.0F : sample.get(type);
        }

        @Nullable
        public PollutionType dominantType() {
            return sample == null ? null : sample.dominantType();
        }

        public String formatValues() {
            return sample == null ? new PollutionSample().formatValues() : sample.formatValues();
        }

        public int compareTotalDescending(EntrySnapshot other) {
            return compareTotalDescending(this, other);
        }

        public static int compareTotalDescending(EntrySnapshot left, EntrySnapshot right) {
            return Float.compare(right == null ? 0.0F : right.totalPollution(),
                    left == null ? 0.0F : left.totalPollution());
        }

        public static int compareTypeDescending(EntrySnapshot left, EntrySnapshot right, PollutionType type) {
            int typeCompare = Float.compare(right == null ? 0.0F : right.get(type),
                    left == null ? 0.0F : left.get(type));
            return typeCompare != 0 ? typeCompare : compareTotalDescending(left, right);
        }
    }

    public record Stats(int totalEntries, int loadedEntries, float totalPollution, float loadedPollution,
                        float maxPollution, float loadedMaxPollution, float[] totals, float[] loadedTotals,
                        int positiveEntries, int loadedPositiveEntries, int storedEntries, int loadedStoredEntries,
                        @Nullable PollutionGridPos minGrid, @Nullable PollutionGridPos maxGrid,
                        @Nullable PollutionGridPos loadedMinGrid, @Nullable PollutionGridPos loadedMaxGrid) {
        public static Stats empty() {
            return new Stats(0, 0, 0.0F, 0.0F, 0.0F, 0.0F,
                    new float[PollutionType.count()], new float[PollutionType.count()], 0, 0, 0, 0,
                    null, null, null, null);
        }

        public float total(PollutionType type) {
            return valueOrZero(totals, type);
        }

        public float total(int ordinal) {
            return total(PollutionType.byOrdinal(ordinal));
        }

        public float loadedTotal(PollutionType type) {
            return valueOrZero(loadedTotals, type);
        }

        public float loadedTotal(int ordinal) {
            return loadedTotal(PollutionType.byOrdinal(ordinal));
        }

        public float[] totalsCopy() {
            return copy(totals);
        }

        public float[] loadedTotalsCopy() {
            return copy(loadedTotals);
        }

        public String formatTotals() {
            return PollutionSample.fromArray(totals).formatValues();
        }

        public String formatLoadedTotals() {
            return PollutionSample.fromArray(loadedTotals).formatValues();
        }

        public String formatGridBounds() {
            return formatBounds(minGrid, maxGrid);
        }

        public String formatLoadedGridBounds() {
            return formatBounds(loadedMinGrid, loadedMaxGrid);
        }

        public String formatBlockBounds() {
            return formatBlockBounds(minGrid, maxGrid);
        }

        public String formatLoadedBlockBounds() {
            return formatBlockBounds(loadedMinGrid, loadedMaxGrid);
        }

        private static String formatBounds(@Nullable PollutionGridPos min, @Nullable PollutionGridPos max) {
            return min == null || max == null ? "none" : min.x() + "," + min.z() + ".." + max.x() + "," + max.z();
        }

        private static String formatBlockBounds(@Nullable PollutionGridPos min, @Nullable PollutionGridPos max) {
            return min == null || max == null ? "none" : min.minBlockX() + "," + min.minBlockZ()
                    + ".." + max.maxBlockX() + "," + max.maxBlockZ();
        }

        private static float valueOrZero(float[] values, PollutionType type) {
            return values != null && type != null && type.ordinal() < values.length ? values[type.ordinal()] : 0.0F;
        }

        private static float[] copy(float[] values) {
            float[] copy = new float[PollutionType.count()];
            if (values != null) {
                System.arraycopy(values, 0, copy, 0, Math.min(values.length, copy.length));
            }
            return copy;
        }
    }

    public record PollutionGridPos(int x, int z) {
        public static final int BLOCK_SIZE = 64;
        public static final int CHUNK_SPAN = BLOCK_SIZE >> 4;

        public static PollutionGridPos ofBlock(int blockX, int blockZ) {
            return new PollutionGridPos(blockX >> 6, blockZ >> 6);
        }

        public static PollutionGridPos ofBlock(BlockPos pos) {
            Objects.requireNonNull(pos, "pos");
            return ofBlock(pos.getX(), pos.getZ());
        }

        public static PollutionGridPos ofChunk(int chunkX, int chunkZ) {
            return new PollutionGridPos(chunkX >> 2, chunkZ >> 2);
        }

        public static PollutionGridPos of(long packed) {
            return new PollutionGridPos((int) (packed >> 32), (int) packed);
        }

        public long toLong() {
            return ((long) x << 32) | (z & 0xffffffffL);
        }

        public PollutionGridPos offset(int xOffset, int zOffset) {
            return new PollutionGridPos(x + xOffset, z + zOffset);
        }

        public List<PollutionGridPos> cardinalNeighbors() {
            return List.of(offset(1, 0), offset(-1, 0), offset(0, 1), offset(0, -1));
        }

        public int minBlockX() {
            return x << 6;
        }

        public int minBlockZ() {
            return z << 6;
        }

        public int maxBlockX() {
            return minBlockX() + BLOCK_SIZE - 1;
        }

        public int maxBlockZ() {
            return minBlockZ() + BLOCK_SIZE - 1;
        }

        public int minChunkX() {
            return x << 2;
        }

        public int minChunkZ() {
            return z << 2;
        }

        public int maxChunkX() {
            return minChunkX() + CHUNK_SPAN - 1;
        }

        public int maxChunkZ() {
            return minChunkZ() + CHUNK_SPAN - 1;
        }

        public String formatBlockBounds() {
            return minBlockX() + "," + minBlockZ() + ".." + maxBlockX() + "," + maxBlockZ();
        }

        public String formatChunkBounds() {
            return minChunkX() + "," + minChunkZ() + ".." + maxChunkX() + "," + maxChunkZ();
        }

        public String formatLabel() {
            return x + "," + z + " blocks=" + formatBlockBounds();
        }

        public boolean containsBlock(int blockX, int blockZ) {
            return blockX >= minBlockX() && blockX <= maxBlockX()
                    && blockZ >= minBlockZ() && blockZ <= maxBlockZ();
        }

        public boolean containsChunk(int chunkX, int chunkZ) {
            return chunkX >= minChunkX() && chunkX <= maxChunkX()
                    && chunkZ >= minChunkZ() && chunkZ <= maxChunkZ();
        }

        public int randomBlockX(RandomSource random) {
            return minBlockX() + (random == null ? 0 : random.nextInt(BLOCK_SIZE));
        }

        public int randomBlockZ(RandomSource random) {
            return minBlockZ() + (random == null ? 0 : random.nextInt(BLOCK_SIZE));
        }
    }

    public static class PollutionSample {
        private final float[] values = new float[PollutionType.count()];

        public static PollutionSample fromTag(CompoundTag tag) {
            PollutionSample sample = new PollutionSample();
            if (tag != null) {
                for (PollutionType type : PollutionType.orderedValues()) {
                    sample.set(type, tag.getFloat(tagName(type)));
                }
            }
            return sample;
        }

        public static PollutionSample fromArray(float[] values) {
            PollutionSample sample = new PollutionSample();
            if (values != null) {
                System.arraycopy(values, 0, sample.values, 0, Math.min(values.length, sample.values.length));
            }
            return sample;
        }

        public void toTag(CompoundTag tag) {
            if (tag == null) {
                return;
            }
            for (PollutionType type : PollutionType.orderedValues()) {
                tag.putFloat(tagName(type), get(type));
            }
        }

        public float get(PollutionType type) {
            if (type == null) {
                return 0.0F;
            }
            return values[type.ordinal()];
        }

        public float get(int ordinal) {
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : 0.0F;
        }

        public boolean hasAtLeast(PollutionType type, float amount) {
            return type != null && Float.isFinite(amount) && amount >= 0.0F && get(type) >= amount;
        }

        public void set(PollutionType type, float value) {
            if (type == null) {
                return;
            }
            values[type.ordinal()] = value;
        }

        public void set(int ordinal, float value) {
            if (ordinal >= 0 && ordinal < values.length) {
                values[ordinal] = value;
            }
        }

        public void add(PollutionType type, float amount) {
            set(type, get(type) + amount);
        }

        public void add(int ordinal, float amount) {
            set(ordinal, get(ordinal) + amount);
        }

        public void addClamped(PollutionType type, float amount) {
            set(type, clamp(get(type) + amount));
        }

        public void addClamped(int ordinal, float amount) {
            set(ordinal, clamp(get(ordinal) + amount));
        }

        public float[] toArray() {
            float[] copy = new float[values.length];
            copyInto(copy);
            return copy;
        }

        public void copyInto(float[] target) {
            if (target != null) {
                System.arraycopy(values, 0, target, 0, Math.min(values.length, target.length));
            }
        }

        public int size() {
            return values.length;
        }

        public PollutionSample copy() {
            PollutionSample copy = new PollutionSample();
            System.arraycopy(values, 0, copy.values, 0, values.length);
            return copy;
        }

        public PollutionSample copyClamped() {
            PollutionSample copy = new PollutionSample();
            for (PollutionType type : PollutionType.orderedValues()) {
                copy.set(type, clamp(get(type)));
            }
            return copy;
        }

        public void add(PollutionSample sample) {
            if (sample == null) {
                return;
            }
            for (PollutionType type : PollutionType.orderedValues()) {
                add(type, sample.get(type));
            }
        }

        public void addClamped(PollutionSample sample) {
            if (sample == null) {
                return;
            }
            for (PollutionType type : PollutionType.orderedValues()) {
                addClamped(type, sample.get(type));
            }
        }

        public void clear() {
            for (int i = 0; i < values.length; i++) {
                values[i] = 0.0F;
            }
        }

        public int positiveTypeCount() {
            int count = 0;
            for (PollutionType type : PollutionType.orderedValues()) {
                if (get(type) > 0.0F) {
                    count++;
                }
            }
            return count;
        }

        public int storedTypeCount() {
            int count = 0;
            for (PollutionType type : PollutionType.orderedValues()) {
                if (get(type) != 0.0F) {
                    count++;
                }
            }
            return count;
        }

        @Nullable
        public PollutionType dominantType() {
            PollutionType dominant = null;
            float max = 0.0F;
            for (PollutionType type : PollutionType.orderedValues()) {
                float value = get(type);
                if (value > max) {
                    max = value;
                    dominant = type;
                }
            }
            return dominant;
        }

        public String formatValues() {
            return formatValues(PollutionType.orderedValues());
        }

        public float formatValue(PollutionType type) {
            return roundForDisplay(get(type));
        }

        public String formatValues(Iterable<PollutionType> types) {
            List<String> parts = new ArrayList<>();
            Iterable<PollutionType> source = types == null ? PollutionType.orderedValues() : types;
            for (PollutionType type : source) {
                if (type != null) {
                    parts.add(type.id() + "=" + roundForDisplay(get(type)));
                }
            }
            return String.join(" ", parts);
        }

        public boolean hasAnyPollution() {
            for (float value : values) {
                if (value > 0.0F) {
                    return true;
                }
            }
            return false;
        }

        public boolean hasAnyStoredValue() {
            for (float value : values) {
                if (value != 0.0F) {
                    return true;
                }
            }
            return false;
        }

        public float sum() {
            float sum = 0.0F;
            for (float value : values) {
                sum += Math.max(0.0F, value);
            }
            return sum;
        }

        public float max() {
            float max = 0.0F;
            for (float value : values) {
                max = Math.max(max, value);
            }
            return max;
        }
    }

    private static float clamp(float value) {
        return Mth.clamp(value, 0.0F, PollutionManager.MAX_POLLUTION);
    }

    private static float roundForDisplay(float value) {
        return ((int) (value * 100.0F)) / 100.0F;
    }
}
