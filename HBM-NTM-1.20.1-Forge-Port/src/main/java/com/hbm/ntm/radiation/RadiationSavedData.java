package com.hbm.ntm.radiation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
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

    public void clear() {
        if (!chunkRadiation.isEmpty()) {
            chunkRadiation.clear();
            setDirty();
        }
    }

    public void updateDiffusion() {
        if (chunkRadiation.isEmpty()) {
            return;
        }

        Map<Long, Float> previous = new HashMap<>(chunkRadiation);
        Map<Long, Float> next = new HashMap<>();

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
                    float spread = value * percent;
                    float nextValue;
                    if (previous.containsKey(target.toLong())) {
                        float current = next.getOrDefault(target.toLong(), 0.0F);
                        nextValue = Math.max((current + spread) * 0.99F - 0.05F, 0.0F);
                    } else {
                        nextValue = spread;
                    }
                    if (nextValue > 0.0F) {
                        next.put(target.toLong(), nextValue);
                    }
                }
            }
        }

        chunkRadiation.clear();
        for (Map.Entry<Long, Float> entry : next.entrySet()) {
            float radiation = clamp(entry.getValue());
            if (radiation > 0.0F) {
                chunkRadiation.put(entry.getKey(), radiation);
            }
        }
        setDirty();
    }

    public Set<Map.Entry<Long, Float>> entries() {
        return Set.copyOf(chunkRadiation.entrySet());
    }

    private static float clamp(float value) {
        return Mth.clamp(value, 0.0F, RadiationConstants.MAX_CHUNK_RADIATION);
    }
}
