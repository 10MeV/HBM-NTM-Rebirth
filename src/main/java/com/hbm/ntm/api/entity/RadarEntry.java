package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public record RadarEntry(String name, int blipLevel, BlockPos pos, ResourceLocation dimension, int entityId, boolean redstone) {
    private static final String TAG_NAME = "name";
    private static final String TAG_BLIP_LEVEL = "blipLevel";
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";
    private static final String TAG_DIMENSION = "dimension";
    private static final String TAG_ENTITY_ID = "entityId";
    private static final String TAG_REDSTONE = "redstone";

    public static RadarEntry of(RadarDetectable detectable, Entity entity, boolean redstone) {
        return new RadarEntry(
                detectable.getRadarName(),
                detectable.getBlipLevel(),
                entity.blockPosition(),
                entity.level().dimension().location(),
                entity.getId(),
                redstone);
    }

    public static RadarEntry of(LegacyRadarDetectable detectable, Entity entity) {
        LegacyRadarDetectable.RadarTargetType type = detectable.getTargetType();
        return new RadarEntry(
                type.radarName(),
                type.ordinal(),
                entity.blockPosition(),
                entity.level().dimension().location(),
                entity.getId(),
                entity.getDeltaMovement().y < 0.0D);
    }

    public static RadarEntry of(Player player) {
        return new RadarEntry(
                player.getDisplayName().getString(),
                RadarDetectable.PLAYER,
                player.blockPosition(),
                player.level().dimension().location(),
                player.getId(),
                true);
    }

    public boolean isPlayer() {
        return blipLevel == RadarDetectable.PLAYER;
    }

    public int legacyType() {
        return blipLevel;
    }

    public LegacyEntityInfo legacyEntityInfo() {
        return new LegacyEntityInfo(isPlayer(), pos.getX(), pos.getY(), pos.getZ(), legacyType(),
                isPlayer() ? name : null);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_NAME, name);
        tag.putInt(TAG_BLIP_LEVEL, blipLevel);
        tag.putInt(TAG_X, pos.getX());
        tag.putInt(TAG_Y, pos.getY());
        tag.putInt(TAG_Z, pos.getZ());
        tag.putString(TAG_DIMENSION, dimension.toString());
        tag.putInt(TAG_ENTITY_ID, entityId);
        tag.putBoolean(TAG_REDSTONE, redstone);
        return tag;
    }

    public static RadarEntry fromTag(CompoundTag tag) {
        return new RadarEntry(
                tag.getString(TAG_NAME),
                tag.getInt(TAG_BLIP_LEVEL),
                new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z)),
                new ResourceLocation(tag.getString(TAG_DIMENSION)),
                tag.getInt(TAG_ENTITY_ID),
                tag.getBoolean(TAG_REDSTONE));
    }

    public static ListTag writeList(Collection<RadarEntry> entries) {
        ListTag list = new ListTag();
        for (RadarEntry entry : entries) {
            list.add(entry.toTag());
        }
        return list;
    }

    public static void readListInto(ListTag list, Collection<RadarEntry> target) {
        for (Tag rawTag : list) {
            if (rawTag instanceof CompoundTag tag) {
                target.add(fromTag(tag));
            }
        }
    }

    public record LegacyEntityInfo(boolean player, int x, int y, int z, int type, String name) {
    }
}
