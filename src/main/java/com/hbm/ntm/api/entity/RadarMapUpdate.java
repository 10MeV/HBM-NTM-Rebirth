package com.hbm.ntm.api.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Arrays;

public record RadarMapUpdate(boolean clear, int slice, byte[] data) {
    public static final int NO_SLICE = -1;
    public static final RadarMapUpdate NONE = new RadarMapUpdate(false, NO_SLICE, new byte[0]);
    public static final RadarMapUpdate CLEAR = new RadarMapUpdate(true, NO_SLICE, new byte[0]);

    public static final String TAG_CLEAR = "mapClear";
    public static final String TAG_SLICE = "mapSlice";
    public static final String TAG_SLICE_INDEX = "mapSliceIndex";

    public RadarMapUpdate {
        if (!RadarMap.validSliceData(slice, data)) {
            slice = NO_SLICE;
            data = new byte[0];
        } else {
            data = Arrays.copyOf(data, data.length);
        }
    }

    public static RadarMapUpdate slice(int slice, byte[] data) {
        return new RadarMapUpdate(false, slice, data);
    }

    public static RadarMapUpdate sliceFromMap(int slice, byte[] map) {
        return slice(slice, RadarMap.copySlice(map, slice));
    }

    public boolean hasSlice() {
        return slice != NO_SLICE;
    }

    public boolean isEmpty() {
        return !clear && !hasSlice();
    }

    public void writeTo(CompoundTag tag) {
        if (clear) {
            tag.putBoolean(TAG_CLEAR, true);
        }
        if (hasSlice()) {
            tag.putShort(TAG_SLICE_INDEX, (short) slice);
            tag.putByteArray(TAG_SLICE, data);
        }
    }

    public static RadarMapUpdate fromTag(CompoundTag tag) {
        boolean clear = tag.getBoolean(TAG_CLEAR);
        if (tag.contains(TAG_SLICE_INDEX, Tag.TAG_SHORT) && tag.contains(TAG_SLICE, Tag.TAG_BYTE_ARRAY)) {
            int slice = tag.getShort(TAG_SLICE_INDEX) & 0xFFFF;
            return new RadarMapUpdate(clear, slice, tag.getByteArray(TAG_SLICE));
        }
        return clear ? CLEAR : NONE;
    }

    public void writeLegacyWire(FriendlyByteBuf buffer) {
        buffer.writeBoolean(clear);
        if (clear) {
            return;
        }
        buffer.writeBoolean(hasSlice());
        if (hasSlice()) {
            buffer.writeShort(slice);
            for (byte value : data) {
                buffer.writeByte(value);
            }
        }
    }

    public static RadarMapUpdate readLegacyWire(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            return CLEAR;
        }
        if (!buffer.readBoolean()) {
            return NONE;
        }
        int slice = buffer.readShort() & 0xFFFF;
        byte[] data = new byte[RadarMap.SLICE_SIZE];
        for (int index = 0; index < data.length; index++) {
            data[index] = buffer.readByte();
        }
        return slice(slice, data);
    }

    public byte[] applyTo(byte[] map) {
        byte[] normalized = RadarMap.normalize(map);
        if (clear) {
            Arrays.fill(normalized, (byte) 0);
        }
        if (hasSlice()) {
            System.arraycopy(data, 0, normalized, RadarMap.sliceStart(slice), RadarMap.SLICE_SIZE);
        }
        return normalized;
    }
}
