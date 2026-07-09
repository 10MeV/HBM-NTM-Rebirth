package com.hbm.util.fauxpointtwelve;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 1.7.10 method-name bridge over the modern compound tag.
 */
@Deprecated(forRemoval = false)
public class NBTTagCompound extends CompoundTag {
    private final Map<String, Tag> rawTags;

    public NBTTagCompound() {
        this(new HashMap<>());
    }

    private NBTTagCompound(Map<String, Tag> rawTags) {
        super(rawTags);
        this.rawTags = rawTags;
    }

    public static NBTTagCompound copyOf(CompoundTag source) {
        if (source instanceof NBTTagCompound legacy) {
            return legacy;
        }
        NBTTagCompound copy = new NBTTagCompound();
        if (source != null) {
            for (String key : source.getAllKeys()) {
                copy.setTag(key, copyTagForLegacy(source.get(key)));
            }
        }
        return copy;
    }

    static Tag copyTagForLegacy(Tag tag) {
        return promote(tag.copy());
    }

    static Tag promote(Tag tag) {
        if (tag instanceof CompoundTag compound && !(tag instanceof NBTTagCompound)) {
            return copyOf(compound);
        }
        if (tag instanceof ListTag list && !(tag instanceof NBTTagList)) {
            return NBTTagList.copyOf(list);
        }
        return tag;
    }

    static String legacyStringValue(Tag tag) {
        if (tag == null) {
            return "null";
        }
        if (tag instanceof StringTag stringTag) {
            return "\"" + stringTag.getAsString() + "\"";
        }
        if (tag instanceof ByteArrayTag byteArrayTag) {
            return "[" + byteArrayTag.getAsByteArray().length + " bytes]";
        }
        if (tag instanceof IntArrayTag intArrayTag) {
            String result = "[";
            for (int value : intArrayTag.getAsIntArray()) {
                result = result + value + ",";
            }
            return result + "]";
        }
        if (tag instanceof CompoundTag compound) {
            return copyOf(compound).toString();
        }
        if (tag instanceof ListTag list) {
            return NBTTagList.copyOf(list).toString();
        }
        return tag.toString();
    }

    public Set<String> func_150296_c() {
        return rawTags.keySet();
    }

    public void setTag(String key, Tag tag) {
        rawTags.put(key, tag);
    }

    public void setByte(String key, byte value) {
        putByte(key, value);
    }

    public void setShort(String key, short value) {
        putShort(key, value);
    }

    public void setInteger(String key, int value) {
        putInt(key, value);
    }

    public void setLong(String key, long value) {
        putLong(key, value);
    }

    public void setFloat(String key, float value) {
        putFloat(key, value);
    }

    public void setDouble(String key, double value) {
        putDouble(key, value);
    }

    public void setString(String key, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Empty string not allowed");
        }
        putString(key, value);
    }

    public void setByteArray(String key, byte[] value) {
        putByteArray(key, value);
    }

    public void setIntArray(String key, int[] value) {
        putIntArray(key, value);
    }

    public void setBoolean(String key, boolean value) {
        setByte(key, (byte) (value ? 1 : 0));
    }

    @Override
    public Tag get(String key) {
        Tag tag = rawTag(key);
        Tag promoted = promote(tag);
        if (promoted != tag && promoted != null) {
            setTag(key, promoted);
        }
        return promoted;
    }

    public Tag getTag(String key) {
        return get(key);
    }

    public byte func_150299_b(String key) {
        Tag tag = rawTag(key);
        return tag != null ? tag.getId() : 0;
    }

    public boolean hasKey(String key) {
        return rawTags.containsKey(key);
    }

    public boolean hasKey(String key, int type) {
        byte actualType = func_150299_b(key);
        return actualType == type || type == 99 && actualType >= Tag.TAG_BYTE && actualType <= Tag.TAG_DOUBLE;
    }

    public byte getByte(String key) {
        try {
            return !rawTags.containsKey(key) ? 0 : ((NumericTag) rawTag(key)).getAsByte();
        } catch (ClassCastException ignored) {
            return 0;
        }
    }

    public short getShort(String key) {
        try {
            return !rawTags.containsKey(key) ? 0 : ((NumericTag) rawTag(key)).getAsShort();
        } catch (ClassCastException ignored) {
            return 0;
        }
    }

    public int getInteger(String key) {
        try {
            return !rawTags.containsKey(key) ? 0 : ((NumericTag) rawTag(key)).getAsInt();
        } catch (ClassCastException ignored) {
            return 0;
        }
    }

    public long getLong(String key) {
        try {
            return !rawTags.containsKey(key) ? 0L : ((NumericTag) rawTag(key)).getAsLong();
        } catch (ClassCastException ignored) {
            return 0L;
        }
    }

    public float getFloat(String key) {
        try {
            return !rawTags.containsKey(key) ? 0.0F : ((NumericTag) rawTag(key)).getAsFloat();
        } catch (ClassCastException ignored) {
            return 0.0F;
        }
    }

    public double getDouble(String key) {
        try {
            return !rawTags.containsKey(key) ? 0.0D : ((NumericTag) rawTag(key)).getAsDouble();
        } catch (ClassCastException ignored) {
            return 0.0D;
        }
    }

    public String getString(String key) {
        try {
            if (!rawTags.containsKey(key)) {
                return "";
            }
            Tag tag = get(key);
            if (tag == null) {
                return tag.toString();
            }
            return tag instanceof StringTag ? tag.getAsString() : legacyStringValue(tag);
        } catch (ClassCastException ignored) {
            return "";
        }
    }

    public byte[] getByteArray(String key) {
        if (!rawTags.containsKey(key)) {
            return new byte[0];
        }
        try {
            return ((ByteArrayTag) get(key)).getAsByteArray();
        } catch (ClassCastException ignored) {
            throw createTypeMismatchReport(key, Tag.TAG_BYTE_ARRAY, rawTag(key));
        }
    }

    public int[] getIntArray(String key) {
        if (!rawTags.containsKey(key)) {
            return new int[0];
        }
        try {
            return ((IntArrayTag) get(key)).getAsIntArray();
        } catch (ClassCastException ignored) {
            throw createTypeMismatchReport(key, Tag.TAG_INT_ARRAY, rawTag(key));
        }
    }

    public NBTTagCompound getCompoundTag(String key) {
        if (!rawTags.containsKey(key)) {
            return new NBTTagCompound();
        }
        Tag tag = get(key);
        if (tag == null) {
            return null;
        }
        if (tag instanceof CompoundTag compound) {
            NBTTagCompound legacy = copyOf(compound);
            if (legacy != tag) {
                setTag(key, legacy);
            }
            return legacy;
        }
        throw createTypeMismatchReport(key, Tag.TAG_COMPOUND, tag);
    }

    public NBTTagList getTagList(String key, int type) {
        if (func_150299_b(key) != Tag.TAG_LIST) {
            return new NBTTagList();
        }
        Tag tag = get(key);
        if (!(tag instanceof ListTag list)) {
            throw createTypeMismatchReport(key, Tag.TAG_LIST, tag);
        }
        NBTTagList legacy = NBTTagList.copyOf(list);
        if (legacy != tag) {
            setTag(key, legacy);
        }
        return legacy.tagCount() > 0 && legacy.func_150303_d() != type ? new NBTTagList() : legacy;
    }

    public boolean getBoolean(String key) {
        return getByte(key) != 0;
    }

    public void removeTag(String key) {
        rawTags.remove(key);
    }

    public boolean hasNoTags() {
        return rawTags.isEmpty();
    }

    @Override
    public String toString() {
        String result = "{";
        for (String key : rawTags.keySet()) {
            result = result + key + ':' + legacyStringValue(rawTag(key)) + ',';
        }
        return result + "}";
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof NBTTagCompound other && rawTags.entrySet().equals(other.rawTags.entrySet());
    }

    @Override
    public int hashCode() {
        return Tag.TAG_COMPOUND ^ rawTags.hashCode();
    }

    private ReportedException createTypeMismatchReport(String key, int expectedType, Tag actualTag) {
        String expectedName = TagTypes.getType(expectedType).getName();
        String actualClass = actualTag == null ? "null" : actualTag.getClass().getName();
        ClassCastException exception = new ClassCastException(actualClass + " cannot be cast to " + expectedName);
        CrashReport report = CrashReport.forThrowable(exception, "Reading NBT data");
        CrashReportCategory category = report.addCategory("Corrupt NBT tag", 1);
        category.setDetail("Tag type found", () -> {
            Tag tag = rawTag(key);
            return tag == null ? "UNKNOWN" : tag.getType().getName();
        });
        category.setDetail("Tag type expected", () -> expectedName);
        category.setDetail("Tag name", key);
        return new ReportedException(report);
    }

    private Tag rawTag(String key) {
        return rawTags.get(key);
    }

    @Override
    public NBTTagCompound copy() {
        NBTTagCompound copy = new NBTTagCompound();
        for (String key : rawTags.keySet()) {
            copy.setTag(key, copyTagForLegacy(rawTag(key)));
        }
        return copy;
    }
}
