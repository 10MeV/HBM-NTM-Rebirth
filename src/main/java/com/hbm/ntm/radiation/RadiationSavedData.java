package com.hbm.ntm.radiation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RadiationSavedData extends SavedData {
    public static final String DATA_NAME = "hbm_chunk_radiation";
    private static final String TAG_ENTRIES = "entries";
    private static final String TAG_CHUNK = "chunk";
    private static final String TAG_RADIATION = "radiation";

    private final Map<Long, Float> chunkRadiation = new HashMap<>();

    public static RadiationSavedData load(CompoundTag tag) {
        RadiationSavedData data = new RadiationSavedData();
        ListTag entries = tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            float radiation = clamp(entry.getFloat(TAG_RADIATION));
            if (radiation > 0.0F) {
                data.chunkRadiation.put(entry.getLong(TAG_CHUNK), radiation);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        for (Map.Entry<Long, Float> entry : chunkRadiation.entrySet()) {
            float radiation = clamp(entry.getValue());
            if (radiation <= 0.0F) {
                continue;
            }
            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong(TAG_CHUNK, entry.getKey());
            entryTag.putFloat(TAG_RADIATION, radiation);
            entries.add(entryTag);
        }
        tag.put(TAG_ENTRIES, entries);
        return tag;
    }

    public float get(ChunkPos pos) {
        return clamp(chunkRadiation.getOrDefault(pos.toLong(), 0.0F));
    }

    public void set(ChunkPos pos, float radiation) {
        float clamped = clamp(radiation);
        if (clamped <= 0.0F) {
            chunkRadiation.remove(pos.toLong());
        } else {
            chunkRadiation.put(pos.toLong(), clamped);
        }
        setDirty();
    }

    public void loadChunk(ChunkPos pos, float radiation) {
        chunkRadiation.put(pos.toLong(), clamp(radiation));
    }

    public void clear() {
        if (!chunkRadiation.isEmpty()) {
            chunkRadiation.clear();
            setDirty();
        }
    }

    public void remove(ChunkPos pos) {
        if (chunkRadiation.remove(pos.toLong()) != null) {
            setDirty();
        }
    }

    public List<Map.Entry<Long, Float>> loadedEntries(ServerLevel level) {
        List<Map.Entry<Long, Float>> entries = new ArrayList<>();
        for (Map.Entry<Long, Float> entry : chunkRadiation.entrySet()) {
            ChunkPos pos = new ChunkPos(entry.getKey());
            if (level.hasChunk(pos.x, pos.z)) {
                entries.add(new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entry.getValue()));
            }
        }
        return entries;
    }

    public List<Map.Entry<Long, Float>> entriesSnapshot() {
        List<Map.Entry<Long, Float>> entries = new ArrayList<>();
        for (Map.Entry<Long, Float> entry : chunkRadiation.entrySet()) {
            entries.add(new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entry.getValue()));
        }
        return entries;
    }

    public int pruneUnloaded(ServerLevel level) {
        return pruneUnloaded(level, null);
    }

    private int pruneUnloaded(ServerLevel level, List<Map.Entry<Long, Float>> loadedEntries) {
        boolean changed = false;
        int removed = 0;
        Iterator<Map.Entry<Long, Float>> iterator = chunkRadiation.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Float> entry = iterator.next();
            ChunkPos pos = new ChunkPos(entry.getKey());
            if (!level.hasChunk(pos.x, pos.z)) {
                iterator.remove();
                changed = true;
                removed++;
                continue;
            }
            if (loadedEntries != null) {
                loadedEntries.add(new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entry.getValue()));
            }
        }
        if (changed) {
            setDirty();
        }
        return removed;
    }

    public Stats stats(ServerLevel level) {
        int total = 0;
        int loaded = 0;
        int positive = 0;
        int loadedPositive = 0;
        float totalRadiation = 0.0F;
        float loadedRadiation = 0.0F;
        float maxRadiation = 0.0F;
        float loadedMaxRadiation = 0.0F;
        for (Map.Entry<Long, Float> entry : chunkRadiation.entrySet()) {
            float radiation = clamp(entry.getValue());
            total++;
            if (radiation > 0.0F) {
                positive++;
                totalRadiation += radiation;
                maxRadiation = Math.max(maxRadiation, radiation);
            }
            ChunkPos pos = new ChunkPos(entry.getKey());
            if (level.hasChunk(pos.x, pos.z)) {
                loaded++;
                if (radiation > 0.0F) {
                    loadedPositive++;
                    loadedRadiation += radiation;
                    loadedMaxRadiation = Math.max(loadedMaxRadiation, radiation);
                }
            }
        }
        return new Stats(total, loaded, positive, loadedPositive, totalRadiation, loadedRadiation, maxRadiation, loadedMaxRadiation);
    }

    public List<ChunkPos> updateDiffusion(ServerLevel level, float fogThreshold) {
        if (chunkRadiation.isEmpty()) {
            return List.of();
        }

        Map<Long, Float> previous = new HashMap<>(chunkRadiation);
        Map<Long, Float> next = new HashMap<>();
        List<ChunkPos> fogCandidates = new ArrayList<>();

        for (Map.Entry<Long, Float> entry : previous.entrySet()) {
            float value = entry.getValue();
            if (value <= 0.0F) {
                continue;
            }

            ChunkPos origin = new ChunkPos(entry.getKey());

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int type = Math.abs(dx) + Math.abs(dz);
                    float percent = type == 0 ? 0.6F : type == 1 ? 0.075F : 0.025F;
                    ChunkPos target = new ChunkPos(origin.x + dx, origin.z + dz);

                    long targetKey = target.toLong();
                    float spread = value * percent;
                    if (previous.containsKey(targetKey)) {
                        float current = next.getOrDefault(targetKey, 0.0F);
                        float nextValue = Math.max((current + spread) * 0.99F - 0.05F, 0.0F);
                        next.put(targetKey, nextValue);
                        if (nextValue > fogThreshold) {
                            fogCandidates.add(origin);
                        }
                    } else {
                        next.put(targetKey, spread);
                        if (spread > fogThreshold) {
                            fogCandidates.add(origin);
                        }
                    }
                }
            }
        }

        chunkRadiation.clear();
        for (Map.Entry<Long, Float> entry : next.entrySet()) {
            float radiation = clamp(entry.getValue());
            chunkRadiation.put(entry.getKey(), radiation);
        }
        setDirty();
        return fogCandidates;
    }

    public Set<Map.Entry<Long, Float>> entries() {
        return Collections.unmodifiableSet(chunkRadiation.entrySet());
    }

    private static float clamp(float value) {
        return Mth.clamp(value, 0.0F, RadiationConstants.MAX_CHUNK_RADIATION);
    }

    public record Stats(int totalEntries, int loadedEntries, int positiveEntries, int loadedPositiveEntries,
                        float totalRadiation, float loadedRadiation, float maxRadiation, float loadedMaxRadiation) {
    }
}
