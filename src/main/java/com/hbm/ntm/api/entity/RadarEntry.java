package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record RadarEntry(String name, int blipLevel, BlockPos pos, ResourceLocation dimension, int entityId, boolean redstone) {
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
}
