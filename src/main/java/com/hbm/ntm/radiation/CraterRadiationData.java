package com.hbm.ntm.radiation;

import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.world.saveddata.WorldSavedDataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CraterRadiationData extends SavedData {
    public static final String DATA_NAME = "hbm_crater_radiation";
    private static final String TAG_CELLS = "cells";
    private static final String TAG_ZONES = "zones";

    private final Map<Long, CraterZone> zones = new HashMap<>();
    private LoadDiagnostics loadDiagnostics = LoadDiagnostics.empty();

    public static CraterRadiationData load(CompoundTag tag) {
        CraterRadiationData data = new CraterRadiationData();
        long[] cells = tag.getLongArray(TAG_CELLS);
        byte[] zoneIds = tag.getByteArray(TAG_ZONES);
        int count = Math.min(cells.length, zoneIds.length);
        for (int i = 0; i < count; i++) {
            CraterZone zone = CraterZone.byId(zoneIds[i]);
            if (zone != CraterZone.NONE) {
                data.zones.put(cells[i], zone);
            }
        }
        data.loadDiagnostics = LoadDiagnostics.inspect(tag);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        long[] cells = new long[zones.size()];
        byte[] zoneIds = new byte[zones.size()];
        int index = 0;
        for (Map.Entry<Long, CraterZone> entry : zones.entrySet()) {
            cells[index] = entry.getKey();
            zoneIds[index] = (byte) entry.getValue().id;
            index++;
        }
        tag.putLongArray(TAG_CELLS, cells);
        tag.putByteArray(TAG_ZONES, zoneIds);
        return tag;
    }

    public static boolean setZone(ServerLevel level, int x, int z, CraterZone zone) {
        if (zone == CraterZone.NONE) {
            return false;
        }
        CraterRadiationData data = get(level);
        long key = cellKey(x, z);
        CraterZone current = data.zones.getOrDefault(key, CraterZone.NONE);
        if (current.id >= zone.id) {
            return false;
        }
        data.zones.put(key, zone);
        data.setDirty();
        return true;
    }

    public static float getAmbientRadiation(LivingEntity entity) {
        if (!RadiationConfig.craterBiomeRadiationEnabled() || !(entity.level() instanceof ServerLevel level)) {
            return 0.0F;
        }
        CraterZone zone = get(level).getZone(entity.blockPosition());
        if (zone == CraterZone.NONE) {
            zone = zoneFromBiome(level.getBiome(entity.blockPosition()));
        }
        float radiation = switch (zone) {
            case OUTER -> RadiationConfig.craterBiomeOuterRadiation();
            case CRATER -> RadiationConfig.craterBiomeRadiation();
            case INNER -> RadiationConfig.craterBiomeInnerRadiation();
            case NONE -> 0.0F;
        };
        if (radiation > 0.0F && entity.isInWaterOrRain()) {
            radiation *= RadiationConfig.craterBiomeWaterMultiplier();
        }
        return Math.max(0.0F, radiation);
    }

    public static Stats getStats(ServerLevel level) {
        return get(level).stats(level);
    }

    public static Optional<CraterRadiationData> getExisting(ServerLevel level) {
        return WorldSavedDataHelper.getExisting(level, DATA_NAME, CraterRadiationData::load);
    }

    public static Optional<CraterRadiationData> getExisting(Level level) {
        return WorldSavedDataHelper.getExisting(level, DATA_NAME, CraterRadiationData::load);
    }

    public static ResyncResult resyncLoadedBiomes(ServerLevel level) {
        CraterRadiationData data = get(level);
        int total = 0;
        int loaded = 0;
        int changed = 0;
        Set<ChunkPos> changedChunks = new LinkedHashSet<>();

        for (Map.Entry<Long, CraterZone> entry : data.zones.entrySet()) {
            total++;
            int x = unpackX(entry.getKey());
            int z = unpackZ(entry.getKey());
            ChunkPos chunkPos = new ChunkPos(x >> 4, z >> 4);
            if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
                continue;
            }

            loaded++;
            if (CraterBiomeUtil.setCraterBiome(level, x, z, entry.getValue())) {
                changed++;
                changedChunks.add(chunkPos);
            }
        }

        for (ChunkPos chunkPos : changedChunks) {
            CraterBiomeUtil.resendCraterBiomes(level, chunkPos);
        }
        return new ResyncResult(total, loaded, changed, changedChunks.size());
    }

    public static CraterZone getZone(ServerLevel level, BlockPos pos) {
        CraterZone zone = get(level).getZone(pos);
        return zone == CraterZone.NONE ? zoneFromBiome(level.getBiome(pos)) : zone;
    }

    public CraterZone getZone(BlockPos pos) {
        return zones.getOrDefault(cellKey(pos.getX(), pos.getZ()), CraterZone.NONE);
    }

    private Stats stats(ServerLevel level) {
        int total = 0;
        int loaded = 0;
        int outer = 0;
        int crater = 0;
        int inner = 0;
        int loadedOuter = 0;
        int loadedCrater = 0;
        int loadedInner = 0;

        for (Map.Entry<Long, CraterZone> entry : zones.entrySet()) {
            total++;
            CraterZone zone = entry.getValue();
            outer += zone == CraterZone.OUTER ? 1 : 0;
            crater += zone == CraterZone.CRATER ? 1 : 0;
            inner += zone == CraterZone.INNER ? 1 : 0;

            int x = unpackX(entry.getKey());
            int z = unpackZ(entry.getKey());
            if (level.hasChunk(x >> 4, z >> 4)) {
                loaded++;
                loadedOuter += zone == CraterZone.OUTER ? 1 : 0;
                loadedCrater += zone == CraterZone.CRATER ? 1 : 0;
                loadedInner += zone == CraterZone.INNER ? 1 : 0;
            }
        }

        return new Stats(total, loaded, outer, crater, inner, loadedOuter, loadedCrater, loadedInner);
    }

    public Stats statsSnapshot(ServerLevel level) {
        return stats(level);
    }

    public LoadDiagnostics loadDiagnostics() {
        return loadDiagnostics;
    }

    private static CraterZone zoneFromBiome(Holder<Biome> biome) {
        return biome.unwrapKey()
                .map(CraterRadiationData::zoneFromBiomeKey)
                .orElse(CraterZone.NONE);
    }

    private static CraterZone zoneFromBiomeKey(ResourceKey<Biome> key) {
        if (key.equals(CraterBiomeUtil.CRATER_INNER)) {
            return CraterZone.INNER;
        }
        if (key.equals(CraterBiomeUtil.CRATER)) {
            return CraterZone.CRATER;
        }
        if (key.equals(CraterBiomeUtil.CRATER_OUTER)) {
            return CraterZone.OUTER;
        }
        return CraterZone.NONE;
    }

    private static CraterRadiationData get(ServerLevel level) {
        return WorldSavedDataHelper.get(level, DATA_NAME, CraterRadiationData::load, CraterRadiationData::new);
    }

    private static long cellKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    private static int unpackX(long key) {
        return (int) (key >> 32);
    }

    private static int unpackZ(long key) {
        return (int) key;
    }

    public record Stats(int totalMarkers, int loadedMarkers, int outerMarkers, int craterMarkers, int innerMarkers,
                        int loadedOuterMarkers, int loadedCraterMarkers, int loadedInnerMarkers) {
    }

    public record ResyncResult(int totalMarkers, int loadedMarkers, int changedCells, int changedChunks) {
    }

    public record LoadDiagnostics(boolean hasCellsTag, boolean hasZonesTag, int cells, int zones,
                                  int mismatchedEntries, int unknownZoneIds, int duplicateCells) {
        public static LoadDiagnostics empty() {
            return new LoadDiagnostics(false, false, 0, 0, 0, 0, 0);
        }

        public static LoadDiagnostics inspect(CompoundTag tag) {
            if (tag == null) {
                return empty();
            }
            boolean hasCells = tag.contains(TAG_CELLS);
            boolean hasZones = tag.contains(TAG_ZONES);
            long[] cells = tag.getLongArray(TAG_CELLS);
            byte[] zoneIds = tag.getByteArray(TAG_ZONES);
            int count = Math.min(cells.length, zoneIds.length);
            int unknownZoneIds = 0;
            int duplicateCells = 0;
            Set<Long> seen = new HashSet<>();
            for (int i = 0; i < count; i++) {
                if (zoneIds[i] < CraterZone.NONE.id || zoneIds[i] > CraterZone.INNER.id) {
                    unknownZoneIds++;
                }
                if (!seen.add(cells[i])) {
                    duplicateCells++;
                }
            }
            return new LoadDiagnostics(hasCells, hasZones, cells.length, zoneIds.length,
                    Math.abs(cells.length - zoneIds.length), unknownZoneIds, duplicateCells);
        }

        public int problemCount() {
            return (hasCellsTag ? 0 : 1)
                    + (hasZonesTag ? 0 : 1)
                    + mismatchedEntries
                    + unknownZoneIds
                    + duplicateCells;
        }

        public boolean clean() {
            return problemCount() == 0;
        }

        public List<String> issues() {
            List<String> issues = new ArrayList<>();
            if (!hasCellsTag) {
                issues.add("missing_cells");
            }
            if (!hasZonesTag) {
                issues.add("missing_zones");
            }
            if (mismatchedEntries > 0) {
                issues.add("mismatched_entries=" + mismatchedEntries);
            }
            if (unknownZoneIds > 0) {
                issues.add("unknown_zone_ids=" + unknownZoneIds);
            }
            if (duplicateCells > 0) {
                issues.add("duplicate_cells=" + duplicateCells);
            }
            return List.copyOf(issues);
        }

        public String summary() {
            return "hasCells=" + hasCellsTag
                    + " hasZones=" + hasZonesTag
                    + " cells=" + cells
                    + " zones=" + zones
                    + " mismatchedEntries=" + mismatchedEntries
                    + " unknownZoneIds=" + unknownZoneIds
                    + " duplicateCells=" + duplicateCells
                    + " problems=" + problemCount()
                    + " issues=" + issues()
                    + " clean=" + clean();
        }
    }

    public enum CraterZone {
        NONE(0),
        OUTER(1),
        CRATER(2),
        INNER(3);

        private final int id;

        CraterZone(int id) {
            this.id = id;
        }

        private static CraterZone byId(int id) {
            int clamped = Mth.clamp(id, 0, INNER.id);
            for (CraterZone zone : values()) {
                if (zone.id == clamped) {
                    return zone;
                }
            }
            return NONE;
        }
    }
}
