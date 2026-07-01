package com.hbm.ntm.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class LegacyWorldItemIdMap {
    private static final String LEVEL_DAT = "level.dat";
    private static final String KEY_FML = "FML";
    private static final String KEY_ITEM_DATA = "ItemData";
    private static final String KEY_MOD_ITEM_DATA = "ModItemData";
    private static final String KEY_REGISTRY_NAME = "K";
    private static final String KEY_NUMERIC_ID = "V";
    private static final String KEY_MOD_ID = "ModId";
    private static final String KEY_ITEM_ID = "ItemId";
    private static final String KEY_FORCED_MOD_ID = "ForcedModId";
    private static final String KEY_FORCED_NAME = "ForcedName";
    private static final LegacyWorldItemIdMap EMPTY = new LegacyWorldItemIdMap(Map.of());

    private final Map<Integer, String> numericToLegacyId;

    private LegacyWorldItemIdMap(Map<Integer, String> numericToLegacyId) {
        this.numericToLegacyId = Collections.unmodifiableMap(new LinkedHashMap<>(numericToLegacyId));
    }

    public static LegacyWorldItemIdMap empty() {
        return EMPTY;
    }

    public static LegacyWorldItemIdMap of(Map<Integer, String> numericToLegacyId) {
        if (numericToLegacyId == null || numericToLegacyId.isEmpty()) {
            return empty();
        }
        Map<Integer, String> normalized = new LinkedHashMap<>();
        for (Map.Entry<Integer, String> entry : numericToLegacyId.entrySet()) {
            String legacyId = normalizeLegacyKey(entry.getValue());
            if (!legacyId.isBlank()) {
                normalized.put(entry.getKey(), legacyId);
            }
        }
        return normalized.isEmpty() ? empty() : new LegacyWorldItemIdMap(normalized);
    }

    public static LegacyWorldItemIdMap fromLevelDatRoot(CompoundTag root) {
        if (root == null) {
            return empty();
        }
        Optional<CompoundTag> fml = fmlTag(root);
        if (fml.isEmpty()) {
            return empty();
        }
        Map<Integer, String> ids = new LinkedHashMap<>();
        if (fml.get().contains(KEY_ITEM_DATA, Tag.TAG_LIST)) {
            ListTag itemData = fml.get().getList(KEY_ITEM_DATA, Tag.TAG_COMPOUND);
            for (Tag entryTag : itemData) {
                if (!(entryTag instanceof CompoundTag entry)
                        || !entry.contains(KEY_REGISTRY_NAME, Tag.TAG_STRING)
                        || !entry.contains(KEY_NUMERIC_ID, Tag.TAG_ANY_NUMERIC)) {
                    continue;
                }
                String legacyId = normalizeLegacyKey(entry.getString(KEY_REGISTRY_NAME));
                if (!legacyId.isBlank()) {
                    ids.put(entry.getInt(KEY_NUMERIC_ID), legacyId);
                }
            }
        } else if (fml.get().contains(KEY_MOD_ITEM_DATA, Tag.TAG_LIST)) {
            ListTag modItemData = fml.get().getList(KEY_MOD_ITEM_DATA, Tag.TAG_COMPOUND);
            for (Tag entryTag : modItemData) {
                if (!(entryTag instanceof CompoundTag entry)
                        || !entry.contains(KEY_ITEM_ID, Tag.TAG_ANY_NUMERIC)
                        || !entry.contains(KEY_FORCED_NAME, Tag.TAG_STRING)) {
                    continue;
                }
                String modId = entry.contains(KEY_FORCED_MOD_ID, Tag.TAG_STRING)
                        ? entry.getString(KEY_FORCED_MOD_ID)
                        : entry.getString(KEY_MOD_ID);
                String forcedName = entry.getString(KEY_FORCED_NAME);
                String legacyId = normalizeLegacyKey("\u0002" + modId + ":" + forcedName);
                if (!legacyId.isBlank()) {
                    ids.put(entry.getInt(KEY_ITEM_ID), legacyId);
                }
            }
        }
        return of(ids);
    }

    public static LoadResult loadFromWorldRoot(Path worldRoot) {
        if (worldRoot == null) {
            return LoadResult.empty(null, "world_root_missing");
        }
        Path levelDat = worldRoot.resolve(LEVEL_DAT);
        if (!Files.isRegularFile(levelDat)) {
            return LoadResult.empty(levelDat, "level_dat_missing");
        }
        try {
            CompoundTag root = NbtIo.readCompressed(levelDat.toFile());
            LegacyWorldItemIdMap map = fromLevelDatRoot(root);
            return new LoadResult(levelDat, map, null, map.isEmpty() ? "fml_item_data_missing" : "loaded");
        } catch (IOException | RuntimeException exception) {
            return new LoadResult(levelDat, empty(), exception, "read_failed");
        }
    }

    public Optional<String> legacyId(int numericId) {
        return Optional.ofNullable(numericToLegacyId.get(numericId));
    }

    public boolean isEmpty() {
        return numericToLegacyId.isEmpty();
    }

    public int size() {
        return numericToLegacyId.size();
    }

    public Map<Integer, String> entries() {
        return numericToLegacyId;
    }

    static String normalizeLegacyKey(String rawKey) {
        if (rawKey == null) {
            return "";
        }
        String key = rawKey.trim();
        while (!key.isEmpty() && key.charAt(0) < ' ') {
            key = key.substring(1);
        }
        return key.toLowerCase(Locale.ROOT);
    }

    private static Optional<CompoundTag> fmlTag(CompoundTag root) {
        if (root.contains(KEY_FML, Tag.TAG_COMPOUND)) {
            return Optional.of(root.getCompound(KEY_FML));
        }
        if (root.contains("Data", Tag.TAG_COMPOUND)) {
            CompoundTag data = root.getCompound("Data");
            if (data.contains(KEY_FML, Tag.TAG_COMPOUND)) {
                return Optional.of(data.getCompound(KEY_FML));
            }
        }
        return Optional.empty();
    }

    public record LoadResult(Path levelDat, LegacyWorldItemIdMap map, Throwable error, String detail) {
        public LoadResult {
            map = map == null ? LegacyWorldItemIdMap.empty() : map;
            detail = detail == null || detail.isBlank() ? "unknown" : detail;
        }

        static LoadResult empty(Path levelDat, String detail) {
            return new LoadResult(levelDat, LegacyWorldItemIdMap.empty(), null, detail);
        }

        public boolean loaded() {
            return !map.isEmpty();
        }

        public String summary() {
            String pathText = levelDat == null ? "<none>" : levelDat.toString();
            String errorText = error == null ? "" : " error=" + error.getClass().getSimpleName();
            return "detail=" + detail + " entries=" + map.size() + " levelDat=" + pathText + errorText;
        }
    }
}
