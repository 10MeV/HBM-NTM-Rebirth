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

        LevelChunk chunk = level.getChunk(key.getChunkXPos(), key.getChunkZPos());
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

    private static int index(int x, int y, int z) {
        return (y << 8) | (z << 4) | x;
    }
}
