package com.hbm.ntm.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public final class HbmLegacyLoadedTileState {
    private boolean muffled;
    private boolean tilted;

    public boolean isMuffled() {
        return muffled;
    }

    public void setMuffled(boolean muffled) {
        this.muffled = muffled;
    }

    public boolean isTilted() {
        return tilted;
    }

    public void setTilted(boolean tilted) {
        this.tilted = tilted;
    }

    public float volume(float baseVolume) {
        return muffled ? baseVolume * 0.1F : baseVolume;
    }

    public boolean muffle() {
        if (muffled) {
            return false;
        }
        muffled = true;
        return true;
    }

    public void writeToNbt(CompoundTag tag) {
        tag.putBoolean(HbmLegacyLoadedTile.TAG_LEGACY_MUFFLED, muffled);
        tag.putBoolean(HbmLegacyLoadedTile.TAG_LEGACY_TILTED, tilted);
    }

    public void readFromNbt(CompoundTag tag) {
        muffled = tag.getBoolean(HbmLegacyLoadedTile.TAG_LEGACY_MUFFLED);
        tilted = tag.getBoolean(HbmLegacyLoadedTile.TAG_LEGACY_TILTED);
    }

    public void writeToNetwork(FriendlyByteBuf data) {
        data.writeBoolean(muffled);
        data.writeBoolean(tilted);
    }

    public void readFromNetwork(FriendlyByteBuf data) {
        muffled = data.readBoolean();
        tilted = data.readBoolean();
    }
}
