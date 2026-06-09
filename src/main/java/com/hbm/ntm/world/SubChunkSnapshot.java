package com.hbm.ntm.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubChunkSnapshot {
    public static final SubChunkSnapshot EMPTY = new SubChunkSnapshot(
            new BlockState[]{Blocks.AIR.defaultBlockState()}, null);

    private final BlockState[] palette;
    private final short[] data;

    private SubChunkSnapshot(BlockState[] palette, short[] data) {
        this.palette = palette;
        this.data = data;
    }

    public static SubChunkSnapshot getSnapshot(Level level, SubChunkKey key, boolean allowGeneration) {
        if (level == null || key == null) {
            return EMPTY;
        }
        if (!allowGeneration && !level.hasChunk(key.getChunkXPos(), key.getChunkZPos())) {
            return EMPTY;
        }

        LevelChunk chunk;
        try {
            chunk = level.getChunk(key.getChunkXPos(), key.getChunkZPos());
        } catch (RuntimeException ex) {
            return EMPTY;
        }
        int sectionIndex = level.getSectionIndexFromSectionY(key.getSectionY());
        if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) {
            return EMPTY;
        }

        LevelChunkSection section = chunk.getSection(sectionIndex);
        if (section.hasOnlyAir()) {
            return EMPTY;
        }

        short[] data = new short[16 * 16 * 16];
        List<BlockState> palette = new ArrayList<>();
        Map<BlockState, Short> indexes = new HashMap<>();
        BlockState air = Blocks.AIR.defaultBlockState();
        palette.add(air);
        indexes.put(air, (short) 0);
        boolean allAir = true;

        for (int localY = 0; localY < 16; localY++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int localX = 0; localX < 16; localX++) {
                    BlockState state = section.getBlockState(localX, localY, localZ);
                    int index;
                    if (state.isAir()) {
                        index = 0;
                    } else {
                        allAir = false;
                        Short existing = indexes.get(state);
                        if (existing == null) {
                            index = palette.size();
                            indexes.put(state, (short) index);
                            palette.add(state);
                        } else {
                            index = existing;
                        }
                    }
                    data[index(localX, localY, localZ)] = (short) index;
                }
            }
        }

        if (allAir) {
            return EMPTY;
        }
        return new SubChunkSnapshot(palette.toArray(BlockState[]::new), data);
    }

    public static SnapshotStatus inspect(Level level, SubChunkKey key, boolean allowGeneration, int sampleLimit) {
        if (level == null || key == null) {
            return SnapshotStatus.empty(key, WorldUtil.ChunkAccessReport.clientOrUnsupported(0, 0),
                    false, "missing_level_or_key");
        }
        WorldUtil.ChunkAccessReport chunk = WorldUtil.inspectChunk(level, key.getPos());
        int sectionIndex = level.getSectionIndexFromSectionY(key.getSectionY());
        boolean validSection = sectionIndex >= 0 && sectionIndex < level.getSectionsCount();
        if (!validSection) {
            return SnapshotStatus.empty(key, chunk, false, "section_out_of_bounds");
        }
        if (!allowGeneration && !chunk.full()) {
            return SnapshotStatus.empty(key, chunk, true, "chunk_unavailable");
        }

        SubChunkSnapshot snapshot = getSnapshot(level, key, allowGeneration);
        return new SnapshotStatus(key, chunk, true, snapshot.paletteSize(), snapshot.nonAirBlockCount(),
                snapshot.blockStateCounts(), snapshot.nonAirWorldSamples(key, sampleLimit),
                snapshot == EMPTY ? "empty" : "snapshot");
    }

    public static SnapshotBatch inspectAll(Level level, Collection<SubChunkKey> keys, boolean allowGeneration,
                                           int sampleLimitPerSubChunk) {
        if (keys == null || keys.isEmpty()) {
            return new SnapshotBatch(0, 0, 0, 0, 0, List.of());
        }
        int chunkFull = 0;
        int validSections = 0;
        int nonEmpty = 0;
        int nonAirBlocks = 0;
        List<SnapshotStatus> statuses = new ArrayList<>();
        for (SubChunkKey key : keys) {
            SnapshotStatus status = inspect(level, key, allowGeneration, sampleLimitPerSubChunk);
            statuses.add(status);
            if (status.chunk().full()) {
                chunkFull++;
            }
            if (status.validSection()) {
                validSections++;
            }
            if (status.nonAirBlocks() > 0) {
                nonEmpty++;
                nonAirBlocks += status.nonAirBlocks();
            }
        }
        return new SnapshotBatch(statuses.size(), chunkFull, validSections, nonEmpty, nonAirBlocks, statuses);
    }

    public BlockState getBlockState(int x, int y, int z) {
        if (this == EMPTY || data == null) {
            return Blocks.AIR.defaultBlockState();
        }
        if ((x | y | z) < 0 || x > 15 || y > 15 || z > 15) {
            return Blocks.AIR.defaultBlockState();
        }
        short paletteIndex = data[index(x, y, z)];
        return paletteIndex >= 0 && paletteIndex < palette.length ? palette[paletteIndex] : Blocks.AIR.defaultBlockState();
    }

    public Block getBlock(int x, int y, int z) {
        return getBlockState(x, y, z).getBlock();
    }

    public BlockState getBlockState(BlockPos worldPos) {
        return getBlockState(worldPos.getX() & 15, worldPos.getY() & 15, worldPos.getZ() & 15);
    }

    public Block getBlock(BlockPos worldPos) {
        return getBlockState(worldPos).getBlock();
    }

    public boolean isEmpty() {
        return this == EMPTY || data == null;
    }

    public int paletteSize() {
        return palette.length;
    }

    public List<BlockState> paletteSnapshot() {
        return List.copyOf(Arrays.asList(palette));
    }

    public short[] dataCopy() {
        return data == null ? new short[0] : Arrays.copyOf(data, data.length);
    }

    public int nonAirBlockCount() {
        if (this == EMPTY || data == null) {
            return 0;
        }
        int count = 0;
        for (short index : data) {
            if (index != 0) {
                count++;
            }
        }
        return count;
    }

    public boolean containsNonAir() {
        return nonAirBlockCount() > 0;
    }

    public boolean containsBlockState(BlockState state) {
        return blockStateCount(state) > 0;
    }

    public int blockStateCount(BlockState state) {
        if (this == EMPTY || data == null || state == null || state.isAir()) {
            return 0;
        }
        int count = 0;
        for (short index : data) {
            if (index > 0 && index < palette.length && palette[index].equals(state)) {
                count++;
            }
        }
        return count;
    }

    public Map<BlockState, Integer> blockStateCounts() {
        if (this == EMPTY || data == null) {
            return Map.of();
        }
        Map<BlockState, Integer> counts = new HashMap<>();
        for (short index : data) {
            if (index > 0 && index < palette.length) {
                counts.merge(palette[index], 1, Integer::sum);
            }
        }
        return Map.copyOf(counts);
    }

    public List<BlockSample> nonAirSamples(int limit) {
        if (this == EMPTY || data == null || limit <= 0) {
            return List.of();
        }
        List<BlockSample> samples = new ArrayList<>();
        for (int localY = 0; localY < 16 && samples.size() < limit; localY++) {
            for (int localZ = 0; localZ < 16 && samples.size() < limit; localZ++) {
                for (int localX = 0; localX < 16 && samples.size() < limit; localX++) {
                    BlockState state = getBlockState(localX, localY, localZ);
                    if (!state.isAir()) {
                        samples.add(new BlockSample(localX, localY, localZ, state));
                    }
                }
            }
        }
        return List.copyOf(samples);
    }

    public List<WorldBlockSample> nonAirWorldSamples(SubChunkKey key, int limit) {
        if (key == null || this == EMPTY || data == null || limit <= 0) {
            return List.of();
        }
        List<WorldBlockSample> samples = new ArrayList<>();
        for (BlockSample sample : nonAirSamples(limit)) {
            samples.add(new WorldBlockSample(
                    new BlockPos(key.getMinBlockX() + sample.localX(),
                            key.getMinBlockY() + sample.localY(),
                            key.getMinBlockZ() + sample.localZ()),
                    sample.state()));
        }
        return List.copyOf(samples);
    }

    private static int index(int x, int y, int z) {
        return (y << 8) | (z << 4) | x;
    }

    public record BlockSample(int localX, int localY, int localZ, BlockState state) {
    }

    public record WorldBlockSample(BlockPos pos, BlockState state) {
    }

    public record SnapshotStatus(SubChunkKey key, WorldUtil.ChunkAccessReport chunk, boolean validSection,
                                 int paletteSize, int nonAirBlocks, Map<BlockState, Integer> blockStateCounts,
                                 List<WorldBlockSample> samples, String detail) {
        public SnapshotStatus {
            blockStateCounts = blockStateCounts == null ? Map.of() : Map.copyOf(blockStateCounts);
            samples = samples == null ? List.of() : List.copyOf(samples);
            detail = detail == null || detail.isBlank() ? "unknown" : detail;
        }

        public static SnapshotStatus empty(SubChunkKey key, WorldUtil.ChunkAccessReport chunk, boolean validSection,
                                           String detail) {
            return new SnapshotStatus(key, chunk, validSection, 1, 0, Map.of(), List.of(), detail);
        }

        public boolean nonEmpty() {
            return nonAirBlocks > 0;
        }
    }

    public record SnapshotBatch(int requestedSubChunks, int fullChunks, int validSections, int nonEmptySubChunks,
                                int nonAirBlocks, List<SnapshotStatus> statuses) {
        public SnapshotBatch {
            statuses = statuses == null ? List.of() : List.copyOf(statuses);
        }

        public boolean complete() {
            return requestedSubChunks == fullChunks && requestedSubChunks == validSections;
        }

        public Map<BlockState, Integer> blockStateCounts() {
            Map<BlockState, Integer> counts = new HashMap<>();
            for (SnapshotStatus status : statuses) {
                for (Map.Entry<BlockState, Integer> entry : status.blockStateCounts().entrySet()) {
                    counts.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
            }
            return Map.copyOf(counts);
        }
    }
}
