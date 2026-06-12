package com.hbm.ntm.network;

import com.hbm.ntm.api.tile.ILoadedTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Common 1.7.10 TileEntityLoadedBase state carried by many machine tiles.
 */
public interface HbmLegacyLoadedTile extends HbmLegacyBufPacketReceiver, HbmTileSyncable, ILoadedTile {
    String TAG_LEGACY_MUFFLED = "muffled";
    String TAG_LEGACY_TILTED = "tilted";

    HbmLegacyLoadedTileState getLegacyLoadedTileState();

    default boolean isMuffled() {
        return getLegacyLoadedTileState().isMuffled();
    }

    default void setMuffled(boolean muffled) {
        getLegacyLoadedTileState().setMuffled(muffled);
    }

    default boolean isTilted() {
        return getLegacyLoadedTileState().isTilted();
    }

    default void setTilted(boolean tilted) {
        getLegacyLoadedTileState().setTilted(tilted);
    }

    @Override
    default boolean isLoaded() {
        return !(this instanceof BlockEntity blockEntity)
                || blockEntity.getLevel() != null && !blockEntity.isRemoved();
    }

    default float getVolume(float baseVolume) {
        return getLegacyLoadedTileState().volume(baseVolume);
    }

    default boolean muffle() {
        if (!getLegacyLoadedTileState().muffle()) {
            return false;
        }
        markChanged();
        return true;
    }

    default void markChanged() {
        if (this instanceof BlockEntity blockEntity) {
            blockEntity.setChanged();
        }
    }

    default void markChangedAndUpdate() {
        if (this instanceof BlockEntity blockEntity) {
            HbmGuiControlSecurity.markChangedAndUpdate(blockEntity);
        }
    }

    default void writeLegacyLoadedTileNbt(CompoundTag tag) {
        getLegacyLoadedTileState().writeToNbt(tag);
    }

    default void readLegacyLoadedTileNbt(CompoundTag tag) {
        getLegacyLoadedTileState().readFromNbt(tag);
    }

    default CompoundTag legacyLoadedTileClientTag() {
        CompoundTag tag = new CompoundTag();
        writeLegacyLoadedTileNbt(tag);
        return tag;
    }

    default void writeLegacyLoadedTileClientTag(CompoundTag tag) {
        writeLegacyLoadedTileNbt(tag);
    }

    default void readLegacyLoadedTileClientTag(CompoundTag tag) {
        readLegacyLoadedTileNbt(tag);
    }

    default void writeLegacyLoadedTileBinary(FriendlyByteBuf data) {
        getLegacyLoadedTileState().writeToNetwork(data);
    }

    default void readLegacyLoadedTileBinary(FriendlyByteBuf data) {
        getLegacyLoadedTileState().readFromNetwork(data);
    }

    @Override
    default CompoundTag getClientSyncTag() {
        return legacyLoadedTileClientTag();
    }

    @Override
    default void handleClientSyncTag(CompoundTag tag) {
        readLegacyLoadedTileClientTag(tag);
    }

    @Override
    default void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
    }

    @Override
    default void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
    }
}
