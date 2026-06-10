package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Collection;
import java.util.List;

public record RadarScreenSnapshot(boolean linked, BlockPos refPos, int range, List<RadarEntry> entries) {
    private static final String TAG_LINKED = "linked";
    private static final String TAG_REF = "ref";
    private static final String TAG_REF_X = "refX";
    private static final String TAG_REF_Y = "refY";
    private static final String TAG_REF_Z = "refZ";
    private static final String TAG_RANGE = "range";
    private static final String TAG_ENTRIES = "Entries";

    public static final RadarScreenSnapshot UNLINKED =
            new RadarScreenSnapshot(false, BlockPos.ZERO, 0, List.of());

    public RadarScreenSnapshot {
        refPos = refPos != null ? refPos : BlockPos.ZERO;
        range = Math.max(0, range);
        entries = List.copyOf(entries != null ? entries : List.of());
    }

    public static RadarScreenSnapshot linked(BlockPos refPos, int range, Collection<RadarEntry> entries) {
        return new RadarScreenSnapshot(true, refPos, range,
                entries != null ? List.copyOf(entries) : List.of());
    }

    public RadarScanResult scanResult() {
        return new RadarScanResult(entries, false);
    }

    public CompoundTag toTag(boolean includeEntries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_LINKED, linked);
        tag.putLong(TAG_REF, refPos.asLong());
        tag.putInt(TAG_REF_X, refPos.getX());
        tag.putInt(TAG_REF_Y, refPos.getY());
        tag.putInt(TAG_REF_Z, refPos.getZ());
        tag.putInt(TAG_RANGE, range);
        if (includeEntries) {
            tag.put(TAG_ENTRIES, RadarEntry.writeList(entries));
        }
        return tag;
    }

    public static RadarScreenSnapshot fromTag(CompoundTag tag) {
        boolean linked = tag.getBoolean(TAG_LINKED);
        BlockPos refPos = refPosFromTag(tag);
        int range = tag.getInt(TAG_RANGE);
        List<RadarEntry> entries = tag.contains(TAG_ENTRIES, Tag.TAG_LIST)
                ? RadarEntry.readList(tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND))
                : List.of();
        return new RadarScreenSnapshot(linked, refPos, range, entries);
    }

    public void writeLegacyWire(FriendlyByteBuf buffer) {
        buffer.writeBoolean(linked);
        buffer.writeInt(refPos.getX());
        buffer.writeInt(refPos.getY());
        buffer.writeInt(refPos.getZ());
        buffer.writeInt(range);
        buffer.writeInt(entries.size());
        for (RadarEntry entry : entries) {
            entry.writeLegacyWire(buffer);
        }
    }

    public static RadarScreenSnapshot readLegacyWire(FriendlyByteBuf buffer) {
        boolean linked = buffer.readBoolean();
        BlockPos refPos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        int range = buffer.readInt();
        int count = Math.max(0, buffer.readInt());
        java.util.ArrayList<RadarEntry> entries = new java.util.ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            entries.add(RadarEntry.readLegacyWire(buffer));
        }
        return new RadarScreenSnapshot(linked, refPos, range, entries);
    }

    private static BlockPos refPosFromTag(CompoundTag tag) {
        if (tag.contains(TAG_REF, Tag.TAG_LONG)) {
            return BlockPos.of(tag.getLong(TAG_REF));
        }
        if (tag.contains(TAG_REF_X, Tag.TAG_INT) || tag.contains(TAG_REF_Y, Tag.TAG_INT)
                || tag.contains(TAG_REF_Z, Tag.TAG_INT)) {
            return new BlockPos(tag.getInt(TAG_REF_X), tag.getInt(TAG_REF_Y), tag.getInt(TAG_REF_Z));
        }
        return BlockPos.ZERO;
    }
}
