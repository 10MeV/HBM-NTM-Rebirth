package com.hbm.ntm.energy;

import com.hbm.ntm.world.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10 TileEntityPylonBase wire connection payload: color, conCount, conN.
 */
public class HbmLegacyWireConnections {
    public static final String TAG_CONNECTION_COUNT = "conCount";
    public static final String TAG_COLOR = "color";
    public static final String TAG_CONNECTION_PREFIX = "con";

    private final List<BlockPos> connected = new ArrayList<>();
    private int color;

    public List<BlockPos> connected() {
        return List.copyOf(connected);
    }

    public int size() {
        return connected.size();
    }

    public boolean isEmpty() {
        return connected.isEmpty();
    }

    public boolean contains(BlockPos pos) {
        return pos != null && connected.contains(pos);
    }

    public List<DirPos> remoteUnknownConnectionPoints() {
        List<DirPos> points = new ArrayList<>();
        for (BlockPos pos : connected) {
            points.add(HbmLegacyPowerNodeShapes.unknownPoint(pos));
        }
        return points;
    }

    public void add(BlockPos pos) {
        if (pos != null) {
            connected.add(pos.immutable());
        }
    }

    public void add(int x, int y, int z) {
        add(new BlockPos(x, y, z));
    }

    public boolean remove(BlockPos pos) {
        if (pos == null) {
            return false;
        }
        return connected.removeIf(pos::equals);
    }

    public boolean remove(int x, int y, int z) {
        return remove(new BlockPos(x, y, z));
    }

    public void clear() {
        connected.clear();
    }

    public int color() {
        return color;
    }

    public boolean setColor(int color) {
        if (color == 0 || color == this.color) {
            return false;
        }
        this.color = color;
        return true;
    }

    public void save(CompoundTag tag) {
        tag.putInt(TAG_CONNECTION_COUNT, connected.size());
        tag.putInt(TAG_COLOR, color);
        for (int i = 0; i < connected.size(); i++) {
            BlockPos pos = connected.get(i);
            tag.putIntArray(TAG_CONNECTION_PREFIX + i, new int[]{pos.getX(), pos.getY(), pos.getZ()});
        }
    }

    public void load(CompoundTag tag) {
        connected.clear();
        color = tag.getInt(TAG_COLOR);
        int count = tag.getInt(TAG_CONNECTION_COUNT);
        for (int i = 0; i < count; i++) {
            int[] pos = tag.getIntArray(TAG_CONNECTION_PREFIX + i);
            if (pos.length >= 3) {
                connected.add(new BlockPos(pos[0], pos[1], pos[2]));
            }
        }
    }
}
