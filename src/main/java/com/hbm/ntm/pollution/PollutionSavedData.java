package com.hbm.ntm.pollution;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        for (PollutionType type : PollutionType.values()) {
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

    public void updateDiffusion() {
        if (pollution.isEmpty()) {
            return;
        }

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

        pollution.clear();
        for (Map.Entry<Long, PollutionSample> entry : next.entrySet()) {
            PollutionSample sample = entry.getValue().copy();
            if (sample.hasAnyPollution()) {
                pollution.put(entry.getKey(), sample);
            }
        }
        setDirty();
    }

    private static void merge(Map<Long, PollutionSample> target, PollutionGridPos pos, PollutionSample source) {
        if (!source.hasAnyPollution()) {
            return;
        }
        PollutionSample sample = target.computeIfAbsent(pos.toLong(), key -> new PollutionSample());
        for (PollutionType type : PollutionType.values()) {
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
        int total = 0;
        int loaded = 0;
        float totalPollution = 0.0F;
        float loadedPollution = 0.0F;
        float maxPollution = 0.0F;
        float loadedMaxPollution = 0.0F;
        float[] totals = new float[PollutionType.values().length];
        float[] loadedTotals = new float[PollutionType.values().length];

        for (Map.Entry<Long, PollutionSample> entry : pollution.entrySet()) {
            PollutionSample sample = entry.getValue();
            float sum = sample.sum();
            total++;
            totalPollution += sum;
            maxPollution = Math.max(maxPollution, sample.max());
            for (PollutionType type : PollutionType.values()) {
                totals[type.ordinal()] += sample.get(type);
            }

            if (predicate != null && predicate.isLoaded(PollutionGridPos.of(entry.getKey()))) {
                loaded++;
                loadedPollution += sum;
                loadedMaxPollution = Math.max(loadedMaxPollution, sample.max());
                for (PollutionType type : PollutionType.values()) {
                    loadedTotals[type.ordinal()] += sample.get(type);
                }
            }
        }

        return new Stats(total, loaded, totalPollution, loadedPollution, maxPollution, loadedMaxPollution,
                totals, loadedTotals);
    }

    public boolean isEmpty() {
        return pollution.isEmpty();
    }

    @FunctionalInterface
    public interface PollutionGridPredicate {
        boolean isLoaded(PollutionGridPos pos);
    }

    public record Stats(int totalEntries, int loadedEntries, float totalPollution, float loadedPollution,
                        float maxPollution, float loadedMaxPollution, float[] totals, float[] loadedTotals) {
    }

    public record PollutionGridPos(int x, int z) {
        public static final int BLOCK_SIZE = 64;
        public static final int CHUNK_SPAN = BLOCK_SIZE >> 4;

        public static PollutionGridPos ofBlock(int blockX, int blockZ) {
            return new PollutionGridPos(blockX >> 6, blockZ >> 6);
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
        private final float[] values = new float[PollutionType.values().length];

        public static PollutionSample fromTag(CompoundTag tag) {
            PollutionSample sample = new PollutionSample();
            if (tag != null) {
                for (PollutionType type : PollutionType.values()) {
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
            for (PollutionType type : PollutionType.values()) {
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
            for (PollutionType type : PollutionType.values()) {
                copy.set(type, clamp(get(type)));
            }
            return copy;
        }

        public void add(PollutionSample sample) {
            if (sample == null) {
                return;
            }
            for (PollutionType type : PollutionType.values()) {
                add(type, sample.get(type));
            }
        }

        public void addClamped(PollutionSample sample) {
            if (sample == null) {
                return;
            }
            for (PollutionType type : PollutionType.values()) {
                addClamped(type, sample.get(type));
            }
        }

        public void clear() {
            for (int i = 0; i < values.length; i++) {
                values[i] = 0.0F;
            }
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
}
