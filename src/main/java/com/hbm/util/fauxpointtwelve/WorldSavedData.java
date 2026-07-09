package com.hbm.util.fauxpointtwelve;

import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 1.7.10-shaped base class for legacy HBM saved-data classes.
 */
@Deprecated(forRemoval = false)
public abstract class WorldSavedData extends SavedData {
    public final String mapName;

    public WorldSavedData(String mapName) {
        this.mapName = mapName;
    }

    public abstract void readFromNBT(NBTTagCompound tag);

    public abstract void writeToNBT(NBTTagCompound tag);

    public void readFromNBT(CompoundTag tag) {
        readFromNBT(NBTTagCompound.copyOf(Objects.requireNonNull(tag, "tag")));
    }

    public void writeToNBT(CompoundTag tag) {
        Objects.requireNonNull(tag, "tag");
        if (tag instanceof NBTTagCompound legacyTag) {
            writeToNBT(legacyTag);
            return;
        }
        NBTTagCompound legacyTag = NBTTagCompound.copyOf(tag);
        writeToNBT(legacyTag);
        replaceWithLegacy(tag, legacyTag);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        Objects.requireNonNull(tag, "tag");
        if (tag instanceof NBTTagCompound legacyTag) {
            writeToNBT(legacyTag);
            return tag;
        }
        NBTTagCompound legacyTag = NBTTagCompound.copyOf(tag);
        writeToNBT(legacyTag);
        replaceWithLegacy(tag, legacyTag);
        return tag;
    }

    private static void replaceWithLegacy(CompoundTag target, NBTTagCompound source) {
        for (String key : new ArrayList<>(target.getAllKeys())) {
            target.remove(key);
        }
        target.merge(source);
    }

    public void markDirty() {
        setDirty();
    }

    @Override
    public void setDirty(boolean dirty) {
        super.setDirty(dirty);
    }

    @Override
    public boolean isDirty() {
        return super.isDirty();
    }
}
