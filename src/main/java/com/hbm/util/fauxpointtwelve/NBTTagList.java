package com.hbm.util.fauxpointtwelve;

import java.io.DataOutput;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/**
 * 1.7.10 method-name bridge over the modern list tag.
 */
@Deprecated(forRemoval = false)
public class NBTTagList extends ListTag {
    private static final String MISMATCH_WARNING = "WARNING: Adding mismatching tag types to tag list";
    private static final String INDEX_WARNING = "WARNING: index out of bounds to set tag in tag list";

    @SuppressWarnings("rawtypes")
    public List tagList;
    public byte tagType = Tag.TAG_END;
    private final List<Object> rawTags;

    public NBTTagList() {
        this(new ArrayList<>(), Tag.TAG_END);
    }

    private NBTTagList(List<Object> rawTags, byte tagType) {
        super();
        this.rawTags = rawTags;
        this.tagList = new LegacyTagListView();
        this.tagType = tagType;
    }

    public static NBTTagList copyOf(ListTag source) {
        if (source instanceof NBTTagList legacy) {
            return legacy;
        }
        NBTTagList copy = new NBTTagList();
        if (source != null) {
            for (int i = 0; i < source.size(); i++) {
                Tag tag = source.get(i);
                copy.addTag(copy.size(), NBTTagCompound.copyTagForLegacy(tag));
            }
        }
        copy.tagType = source == null ? Tag.TAG_END : source.getElementType();
        return copy;
    }

    private static void warnMismatch() {
        System.err.println(MISMATCH_WARNING);
    }

    private boolean canAccept(Tag tag) {
        byte id = tag.getId();
        return tagType == Tag.TAG_END || tagType == id;
    }

    public void appendTag(Tag tag) {
        if (!addTag(size(), tag)) {
            warnMismatch();
        }
    }

    public void func_150304_a(int index, Tag tag) {
        if (index >= 0 && index < size()) {
            if (!setTag(index, tag)) {
                warnMismatch();
            }
        } else {
            System.err.println(INDEX_WARNING);
        }
    }

    public Tag removeTag(int index) {
        return remove(index);
    }

    public NBTTagCompound getCompoundTagAt(int index) {
        if (index < 0 || index >= size()) {
            return new NBTTagCompound();
        }
        Tag tag = get(index);
        if (tag.getId() == Tag.TAG_COMPOUND) {
            return NBTTagCompound.copyOf((CompoundTag) tag);
        }
        return new NBTTagCompound();
    }

    public int[] func_150306_c(int index) {
        if (index < 0 || index >= size()) {
            return new int[0];
        }
        Tag tag = get(index);
        return tag.getId() == Tag.TAG_INT_ARRAY ? ((IntArrayTag) tag).getAsIntArray() : new int[0];
    }

    public double func_150309_d(int index) {
        if (index < 0 || index >= size()) {
            return 0.0D;
        }
        Tag tag = get(index);
        return tag.getId() == Tag.TAG_DOUBLE ? ((DoubleTag) tag).getAsDouble() : 0.0D;
    }

    public float func_150308_e(int index) {
        if (index < 0 || index >= size()) {
            return 0.0F;
        }
        Tag tag = get(index);
        return tag.getId() == Tag.TAG_FLOAT ? ((FloatTag) tag).getAsFloat() : 0.0F;
    }

    public String getStringTagAt(int index) {
        if (index < 0 || index >= size()) {
            return "";
        }
        Tag tag = get(index);
        return tag.getId() == Tag.TAG_STRING ? tag.getAsString() : NBTTagCompound.legacyStringValue(tag);
    }

    public int tagCount() {
        return size();
    }

    public int func_150303_d() {
        return tagType;
    }

    @Override
    public String toString() {
        String result = "[";
        for (int i = 0; i < size(); i++) {
            result = result + i + ':' + NBTTagCompound.legacyStringValue((Tag) rawTag(i)) + ',';
        }
        return result + "]";
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof NBTTagList other && tagType == other.tagType && rawList().equals(other.rawList());
    }

    @Override
    public int hashCode() {
        return Tag.TAG_LIST ^ rawList().hashCode();
    }

    @Override
    public Tag get(int index) {
        Tag tag = (Tag) rawList().get(index);
        Tag promoted = NBTTagCompound.promote(tag);
        if (promoted != tag && promoted != null) {
            rawList().set(index, promoted);
        }
        return promoted;
    }

    @Override
    public Tag set(int index, Tag tag) {
        Tag old = get(index);
        if (!setTag(index, tag)) {
            warnMismatch();
        }
        return old;
    }

    @Override
    public void add(int index, Tag tag) {
        if (!addTag(index, tag)) {
            warnMismatch();
        }
    }

    @Override
    public boolean addTag(int index, Tag tag) {
        Tag promoted = NBTTagCompound.promote(tag);
        if (!canAccept(promoted)) {
            return false;
        }
        rawList().add(index, promoted);
        tagType = promoted.getId();
        return true;
    }

    @Override
    public boolean setTag(int index, Tag tag) {
        Tag promoted = NBTTagCompound.promote(tag);
        if (!canAccept(promoted)) {
            return false;
        }
        rawList().set(index, promoted);
        tagType = promoted.getId();
        return true;
    }

    @Override
    public Tag remove(int index) {
        return (Tag) rawList().remove(index);
    }

    private Object rawTag(int index) {
        return rawList().get(index);
    }

    @SuppressWarnings("unchecked")
    private List<Object> rawList() {
        return tagList instanceof LegacyTagListView ? rawTags : (List<Object>) tagList;
    }

    @Override
    public void clear() {
        rawList().clear();
    }

    @Override
    public int size() {
        return rawList().size();
    }

    @Override
    public boolean isEmpty() {
        return rawList().isEmpty();
    }

    @Override
    public byte getElementType() {
        return tagType;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        List<Object> list = rawList();
        if (!list.isEmpty()) {
            tagType = ((Tag) list.get(0)).getId();
        } else {
            tagType = Tag.TAG_END;
        }

        output.writeByte(tagType);
        output.writeInt(list.size());

        for (Object tag : list) {
            ((Tag) tag).write(output);
        }
    }

    @Override
    public NBTTagList copy() {
        NBTTagList copy = new NBTTagList(new ArrayList<>(), tagType);
        for (int i = 0; i < size(); i++) {
            Tag tag = (Tag) rawTag(i);
            copy.rawTags.add(NBTTagCompound.copyTagForLegacy(tag));
        }
        return copy;
    }

    private final class LegacyTagListView extends AbstractList<Object> {
        @Override
        public Object get(int index) {
            Object value = rawTags.get(index);
            if (value instanceof Tag tag) {
                Tag promoted = NBTTagCompound.promote(tag);
                if (promoted != tag) {
                    rawTags.set(index, promoted);
                }
                return promoted;
            }
            return value;
        }

        @Override
        public int size() {
            return rawTags.size();
        }

        @Override
        public void add(int index, Object element) {
            rawTags.add(index, promoteRaw(element));
        }

        @Override
        public Object set(int index, Object element) {
            return rawTags.set(index, promoteRaw(element));
        }

        @Override
        public Object remove(int index) {
            return rawTags.remove(index);
        }

        @Override
        public void clear() {
            rawTags.clear();
        }
    }

    private static Object promoteRaw(Object element) {
        return element instanceof Tag tag ? NBTTagCompound.promote(tag) : element;
    }
}
