package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Arrays;

public final class RadarMap {
    public static final int WIDTH = 200;
    public static final int SIZE = WIDTH * WIDTH;
    public static final int SLICE_COUNT = 400;
    public static final int SLICE_SIZE = 100;
    public static final int MIN_DISPLAY_HEIGHT = 50;
    public static final int MAX_DISPLAY_HEIGHT = 128;
    public static final String TAG_FULL_MAP = "map";

    public static byte[] normalize(byte[] map) {
        return map == null || map.length != SIZE ? new byte[SIZE] : map;
    }

    public static byte[] copy(byte[] map) {
        return Arrays.copyOf(normalize(map), SIZE);
    }

    public static void writeTo(CompoundTag tag, byte[] map) {
        tag.putByteArray(TAG_FULL_MAP, copy(map));
    }

    public static byte[] readFrom(CompoundTag tag, byte[] fallback) {
        if (tag.contains(TAG_FULL_MAP, Tag.TAG_BYTE_ARRAY)) {
            return normalize(tag.getByteArray(TAG_FULL_MAP));
        }
        return normalize(fallback);
    }

    public static int sliceForGameTime(long gameTime) {
        return (int) Math.floorMod(gameTime, SLICE_COUNT);
    }

    public static int sliceStart(int slice) {
        return slice * SLICE_SIZE;
    }

    public static byte[] copySlice(byte[] map, int slice) {
        byte[] normalized = normalize(map);
        if (slice < 0 || slice >= SLICE_COUNT) {
            return new byte[0];
        }
        int start = sliceStart(slice);
        return Arrays.copyOfRange(normalized, start, start + SLICE_SIZE);
    }

    public static int gridX(int index) {
        return index % WIDTH;
    }

    public static int gridZ(int index) {
        return index / WIDTH;
    }

    public static int sampleX(BlockPos origin, int range, int index) {
        return origin.getX() - range + gridX(index) * range * 2 / WIDTH;
    }

    public static int sampleZ(BlockPos origin, int range, int index) {
        return origin.getZ() - range + gridZ(index) * range * 2 / WIDTH;
    }

    public static byte sampleHeight(ServerLevel level, int x, int z) {
        int height = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        return (byte) Mth.clamp(height, MIN_DISPLAY_HEIGHT, MAX_DISPLAY_HEIGHT);
    }

    public static int green(byte height) {
        int value = height & 0xFF;
        return Mth.clamp((value - MIN_DISPLAY_HEIGHT) * 255 / (MAX_DISPLAY_HEIGHT - MIN_DISPLAY_HEIGHT), 0, 255);
    }

    public static boolean validSliceData(int slice, byte[] data) {
        return slice >= 0 && slice < SLICE_COUNT && data != null && data.length == SLICE_SIZE;
    }

    private RadarMap() {
    }
}
