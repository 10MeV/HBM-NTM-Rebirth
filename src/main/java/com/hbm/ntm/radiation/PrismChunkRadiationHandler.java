package com.hbm.ntm.radiation;

import com.hbm.ntm.util.HbmBlockStateUtil;
import com.hbm.ntm.world.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class PrismChunkRadiationHandler {
    static final float MAX_RADIATION = 1_000_000.0F;
    private static final String TAG_RADIATION = "hfr_prism_radiation_";
    private static final String TAG_RESISTANCE = "hfr_prism_resistance_";
    private static final String TAG_EXISTS = "hfr_prism_exists_";
    private static final Direction[] DIRECTIONS = Direction.values();

    private static final Map<ResourceKey<Level>, RadPerWorld> PER_LEVEL = new ConcurrentHashMap<>();
    private static int updateTimer;
    private static int cycles;
    private static long lastServerTick = Long.MIN_VALUE;

    static float getRadiation(ServerLevel level, BlockPos pos) {
        RadPerWorld system = system(level);
        SubChunk[] chunk = system.radiation.get(new ChunkPos(pos).toLong());
        if (chunk == null) {
            return 0.0F;
        }
        SubChunk subChunk = chunk[sectionIndex(level, pos.getY())];
        return subChunk == null ? 0.0F : subChunk.radiation;
    }

    static float getChunkRadiation(ServerLevel level, ChunkPos pos) {
        RadPerWorld system = system(level);
        SubChunk[] chunk = system.radiation.get(pos.toLong());
        if (chunk == null) {
            return 0.0F;
        }
        float max = 0.0F;
        for (SubChunk subChunk : chunk) {
            if (subChunk != null) {
                max = Math.max(max, legacyComparisonClamp(subChunk.radiation));
            }
        }
        return max;
    }

    static void setRadiation(ServerLevel level, BlockPos pos, float radiation) {
        ChunkPos chunkPos = new ChunkPos(pos);
        if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
            return;
        }
        RadPerWorld system = system(level);
        SubChunk[] chunk = system.radiation.computeIfAbsent(chunkPos.toLong(), ignored -> new SubChunk[level.getSectionsCount()]);
        int sectionIndex = sectionIndex(level, pos.getY());
        if (chunk[sectionIndex] == null) {
            chunk[sectionIndex] = new SubChunk().rebuild(level, chunkPos, sectionIndex);
        }
        chunk[sectionIndex].radiation = legacySetClamp(radiation);
        markChunkUnsaved(level, chunkPos);
    }

    static void clear(ServerLevel level) {
        RadPerWorld system = system(level);
        for (Long chunkKey : system.radiation.keySet()) {
            markChunkUnsaved(level, new ChunkPos(chunkKey));
        }
        system.radiation.clear();
    }

    static RadiationSavedData.Stats stats(ServerLevel level) {
        int total = 0;
        int loaded = 0;
        int positive = 0;
        int loadedPositive = 0;
        float totalRadiation = 0.0F;
        float loadedRadiation = 0.0F;
        float maxRadiation = 0.0F;
        float loadedMaxRadiation = 0.0F;

        for (Map.Entry<Long, SubChunk[]> entry : system(level).radiation.entrySet()) {
            boolean chunkLoaded = level.hasChunk(new ChunkPos(entry.getKey()).x, new ChunkPos(entry.getKey()).z);
            for (SubChunk subChunk : entry.getValue()) {
                if (subChunk == null) {
                    continue;
                }
                float radiation = legacyComparisonClamp(subChunk.radiation);
                total++;
                if (chunkLoaded) {
                    loaded++;
                }
                if (radiation > 0.0F) {
                    positive++;
                    totalRadiation += radiation;
                    maxRadiation = Math.max(maxRadiation, radiation);
                    if (chunkLoaded) {
                        loadedPositive++;
                        loadedRadiation += radiation;
                        loadedMaxRadiation = Math.max(loadedMaxRadiation, radiation);
                    }
                }
            }
        }

        return new RadiationSavedData.Stats(total, loaded, positive, loadedPositive,
                totalRadiation, loadedRadiation, maxRadiation, loadedMaxRadiation);
    }

    static int pruneUnloaded(ServerLevel level) {
        int removed = 0;
        Iterator<Long> iterator = system(level).radiation.keySet().iterator();
        while (iterator.hasNext()) {
            ChunkPos pos = new ChunkPos(iterator.next());
            if (!level.hasChunk(pos.x, pos.z)) {
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }

    static void tick(ServerLevel level) {
        MinecraftServer server = level.getServer();
        long serverTick = server.overworld().getGameTime();
        if (lastServerTick == serverTick) {
            return;
        }
        lastServerTick = serverTick;
        updateTimer++;
        if (updateTimer >= 20) {
            updateAll(server);
            updateTimer = 0;
        }
    }

    static void updateAll(MinecraftServer server) {
        if (server == null) {
            return;
        }
        cycles++;
        com.hbm.handler.radiation.ChunkRadiationHandlerPRISM.cycles = cycles;
        int cycle = cycles;
        for (ServerLevel level : server.getAllLevels()) {
            updateSystem(level, cycle);
        }
    }

    static int cycles() {
        return cycles;
    }

    static void ensureLevel(ServerLevel level) {
        system(level);
    }

    static void loadChunk(ServerLevel level, ChunkPos pos, CompoundTag tag) {
        RadPerWorld system = system(level);
        int sections = level.getSectionsCount();
        SubChunk[] chunk = new SubChunk[sections];

        for (int i = 0; i < sections; i++) {
            if (!tag.getBoolean(TAG_EXISTS + i)) {
                chunk[i] = new SubChunk().rebuild(level, pos, i);
                continue;
            }
            SubChunk subChunk = new SubChunk();
            subChunk.radiation = tag.getFloat(TAG_RADIATION + i);
            for (int j = 0; j < 16; j++) {
                subChunk.xResist[j] = tag.getFloat(TAG_RESISTANCE + "x_" + j + "_" + i);
                subChunk.yResist[j] = tag.getFloat(TAG_RESISTANCE + "y_" + j + "_" + i);
                subChunk.zResist[j] = tag.getFloat(TAG_RESISTANCE + "z_" + j + "_" + i);
            }
            chunk[i] = subChunk;
        }

        system.radiation.put(pos.toLong(), chunk);
    }

    static void saveChunk(ServerLevel level, ChunkPos pos, CompoundTag tag) {
        SubChunk[] chunk = system(level).radiation.get(pos.toLong());
        int sections = level.getSectionsCount();
        if (chunk == null) {
            return;
        }

        for (int i = 0; i < sections; i++) {
            SubChunk subChunk = i < chunk.length ? chunk[i] : null;
            if (subChunk == null) {
                continue;
            }
            tag.putFloat(TAG_RADIATION + i, subChunk.radiation);
            for (int j = 0; j < 16; j++) {
                tag.putFloat(TAG_RESISTANCE + "x_" + j + "_" + i, subChunk.xResist[j]);
                tag.putFloat(TAG_RESISTANCE + "y_" + j + "_" + i, subChunk.yResist[j]);
                tag.putFloat(TAG_RESISTANCE + "z_" + j + "_" + i, subChunk.zResist[j]);
            }
            tag.putBoolean(TAG_EXISTS + i, true);
        }
    }

    static void unloadChunk(Level level, ChunkPos pos) {
        RadPerWorld system = PER_LEVEL.get(level.dimension());
        if (system != null) {
            system.radiation.remove(pos.toLong());
        }
    }

    static void unloadLevel(Level level) {
        PER_LEVEL.remove(level.dimension());
        if (PER_LEVEL.isEmpty()) {
            updateTimer = 0;
            lastServerTick = Long.MIN_VALUE;
        }
    }

    private static void updateSystem(ServerLevel level, int cycle) {
        RadPerWorld system = system(level);
        int rebuildAllowance = 25;

        for (Map.Entry<Long, SubChunk[]> entry : new ArrayList<>(system.radiation.entrySet())) {
            ChunkPos chunkPos = new ChunkPos(entry.getKey());
            SubChunk[] chunk = entry.getValue();

            for (int i = 0; i < chunk.length; i++) {
                SubChunk subChunk = chunk[i];
                if (subChunk == null) {
                    continue;
                }

                subChunk.prevRadiation = subChunk.radiation;
                subChunk.radiation = 0.0F;

                boolean rebuilt = false;
                if (rebuildAllowance > 0 && subChunk.needsRebuild) {
                    subChunk.rebuild(level, chunkPos, i);
                    if (!subChunk.needsRebuild) {
                        rebuildAllowance--;
                        rebuilt = true;
                        markChunkUnsaved(level, chunkPos);
                    }
                }

                if (!rebuilt
                        && Math.abs(chunkPos.x * chunkPos.z) % 5 == cycle % 5
                        && level.hasChunk(chunkPos.x, chunkPos.z)
                        && checksum(level, chunkPos, i) != subChunk.checksum) {
                    subChunk.rebuild(level, chunkPos, i);
                    markChunkUnsaved(level, chunkPos);
                }
            }
        }

        Map<Long, SubChunk[]> additions = new HashMap<>();
        for (Map.Entry<Long, SubChunk[]> entry : new ArrayList<>(system.radiation.entrySet())) {
            SubChunk[] chunk = entry.getValue();
            if (getPrevChunkRadiation(chunk) <= 0.0F) {
                continue;
            }

            ChunkPos chunkPos = new ChunkPos(entry.getKey());
            for (int i = 0; i < chunk.length; i++) {
                SubChunk subChunk = chunk[i];
                if (subChunk == null || subChunk.prevRadiation <= 0.0F || !Float.isFinite(subChunk.prevRadiation)) {
                    continue;
                }
                float spread = 0.0F;
                for (Direction direction : DIRECTIONS) {
                    spread += spreadRadiation(level, subChunk, i, chunkPos, chunk, system.radiation, additions, direction);
                }
                subChunk.radiation += (subChunk.prevRadiation - spread) * 0.95F;
                subChunk.radiation -= 1.0F;
                subChunk.radiation = legacyComparisonClamp(subChunk.radiation);
                markChunkUnsaved(level, chunkPos);
            }
        }

        system.radiation.putAll(additions);
    }

    private static float spreadRadiation(ServerLevel level, SubChunk source, int sectionIndex, ChunkPos origin,
                                         SubChunk[] chunk, Map<Long, SubChunk[]> radiation,
                                         Map<Long, SubChunk[]> additions, Direction direction) {
        float amount = source.prevRadiation * 0.1F;
        if (amount <= 1.0F) {
            return 0.0F;
        }

        if (direction.getStepY() != 0) {
            int targetSection = sectionIndex + direction.getStepY();
            if (targetSection < 0 || targetSection >= chunk.length) {
                return amount;
            }
            if (chunk[targetSection] == null) {
                chunk[targetSection] = new SubChunk().rebuild(level, origin, targetSection);
            }
            return spreadRadiationTo(source, chunk[targetSection], amount, direction);
        }

        ChunkPos targetPos = new ChunkPos(origin.x + direction.getStepX(), origin.z + direction.getStepZ());
        if (!level.hasChunk(targetPos.x, targetPos.z)) {
            return amount;
        }

        long targetKey = targetPos.toLong();
        SubChunk[] targetChunk = radiation.get(targetKey);
        if (targetChunk == null) {
            targetChunk = additions.computeIfAbsent(targetKey, ignored -> new SubChunk[level.getSectionsCount()]);
        }
        if (targetChunk[sectionIndex] == null) {
            targetChunk[sectionIndex] = new SubChunk().rebuild(level, targetPos, sectionIndex);
        }
        float moved = spreadRadiationTo(source, targetChunk[sectionIndex], amount, direction);
        markChunkUnsaved(level, targetPos);
        return moved;
    }

    private static float spreadRadiationTo(SubChunk from, SubChunk to, float amount, Direction movement) {
        float resistance = from.getResistanceValue(movement.getOpposite()) + to.getResistanceValue(movement);
        double attenuation = Math.exp(-resistance / 10_000.0D);
        float toMove = (float) Math.min(amount * attenuation, amount);
        to.radiation += toMove;
        return toMove;
    }

    private static float getPrevChunkRadiation(SubChunk[] chunk) {
        float radiation = 0.0F;
        for (SubChunk subChunk : chunk) {
            if (subChunk != null) {
                radiation += subChunk.prevRadiation;
            }
        }
        return radiation;
    }

    private static RadPerWorld system(ServerLevel level) {
        return PER_LEVEL.computeIfAbsent(level.dimension(), ignored -> new RadPerWorld());
    }

    private static int sectionIndex(ServerLevel level, int blockY) {
        int sectionY = SectionPos.blockToSectionCoord(blockY);
        return Mth.clamp(WorldUtil.sectionIndex(level, sectionY), 0, level.getSectionsCount() - 1);
    }

    private static int sectionY(ServerLevel level, int sectionIndex) {
        return WorldUtil.sectionYFromIndex(level, sectionIndex);
    }

    private static int checksum(ServerLevel level, ChunkPos chunkPos, int sectionIndex) {
        if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
            return 0;
        }
        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
        int rawSection = level.getSectionIndexFromSectionY(sectionY(level, sectionIndex));
        if (rawSection < 0 || rawSection >= chunk.getSections().length) {
            return 0;
        }
        LevelChunkSection section = chunk.getSection(rawSection);
        if (section.hasOnlyAir()) {
            return 0;
        }
        int checksum = 0;
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    checksum += BuiltInRegistries.BLOCK.getId(section.getBlockState(x, y, z).getBlock());
                }
            }
        }
        return checksum;
    }

    private static float legacySetClamp(float value) {
        if (Float.isNaN(value)) {
            return 0.0F;
        }
        return legacyComparisonClamp(value);
    }

    private static float legacyComparisonClamp(float value) {
        if (value < 0.0F) {
            return 0.0F;
        }
        if (value > MAX_RADIATION) {
            return MAX_RADIATION;
        }
        return value;
    }

    private static void markChunkUnsaved(ServerLevel level, ChunkPos chunkPos) {
        if (level.hasChunk(chunkPos.x, chunkPos.z)) {
            level.getChunk(chunkPos.x, chunkPos.z).setUnsaved(true);
        }
    }

    private static final class RadPerWorld {
        private final Map<Long, SubChunk[]> radiation = new ConcurrentHashMap<>();
    }

    private static final class SubChunk {
        private float prevRadiation;
        private float radiation;
        private final float[] xResist = new float[16];
        private final float[] yResist = new float[16];
        private final float[] zResist = new float[16];
        private boolean needsRebuild;
        private int checksum;

        private SubChunk rebuild(ServerLevel level, ChunkPos chunkPos, int sectionIndex) {
            needsRebuild = true;
            if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
                return this;
            }

            Arrays.fill(xResist, 0.0F);
            Arrays.fill(yResist, 0.0F);
            Arrays.fill(zResist, 0.0F);
            checksum = 0;

            LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
            int sectionY = sectionY(level, sectionIndex);
            int rawSection = level.getSectionIndexFromSectionY(sectionY);
            if (rawSection < 0 || rawSection >= chunk.getSections().length) {
                return this;
            }

            LevelChunkSection section = chunk.getSection(rawSection);
            if (!section.hasOnlyAir()) {
                int baseX = chunkPos.getMinBlockX();
                int baseY = SectionPos.sectionToBlockCoord(sectionY);
                // 1.7.10 used cX for tZ here; keep the source bug for resistance parity.
                int baseZ = chunkPos.getMinBlockX();
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            BlockState state = section.getBlockState(x, y, z);
                            if (state.isAir()) {
                                continue;
                            }
                            BlockPos pos = new BlockPos(baseX + x, baseY + y, baseZ + z);
                            float resistance = Math.min(HbmBlockStateUtil.explosionResistance(state, level, pos), 100.0F);
                            xResist[x] += resistance;
                            yResist[y] += resistance;
                            zResist[z] += resistance;
                            checksum += BuiltInRegistries.BLOCK.getId(state.getBlock());
                        }
                    }
                }
            }

            needsRebuild = false;
            return this;
        }

        private float getResistanceValue(Direction movement) {
            return switch (movement) {
                case EAST -> getResistanceFromArray(xResist, true);
                case WEST -> getResistanceFromArray(xResist, false);
                case UP -> getResistanceFromArray(yResist, true);
                case DOWN -> getResistanceFromArray(yResist, false);
                case SOUTH -> getResistanceFromArray(zResist, true);
                case NORTH -> getResistanceFromArray(zResist, false);
            };
        }

        private static float getResistanceFromArray(float[] resist, boolean reverse) {
            float resistance = 0.0F;
            for (int i = 1; i < 16; i++) {
                int index = reverse ? 15 - i : i;
                resistance += resist[index] / 15.0F * i;
            }
            return resistance;
        }
    }

    private PrismChunkRadiationHandler() {
    }
}
