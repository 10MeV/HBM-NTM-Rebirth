package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;

public final class RadarDisplayProjection {
    public static final int GUI_AREA_SIZE = 200;
    public static final int GUI_BLIP_SIZE = 8;
    public static final int GUI_BLIP_RANGE = GUI_AREA_SIZE - GUI_BLIP_SIZE;
    public static final double WORLD_BLIP_RANGE = 0.875D;
    public static final double WORLD_BLIP_SIZE = 0.0625D;

    public static ScreenOffset guiBlipOffset(BlockPos entry, BlockPos center, int range) {
        ScreenOffset hitOffset = guiBlipHitOffset(entry, center, range);
        return new ScreenOffset(hitOffset.x() - GUI_BLIP_SIZE / 2.0D, hitOffset.z() - GUI_BLIP_SIZE / 2.0D);
    }

    public static ScreenOffset guiBlipHitOffset(BlockPos entry, BlockPos center, int range) {
        double divisor = (double) range * 2.0D + 1.0D;
        double x = (entry.getX() - center.getX()) / divisor * GUI_BLIP_RANGE;
        double z = (entry.getZ() - center.getZ()) / divisor * GUI_BLIP_RANGE;
        return new ScreenOffset(x, z);
    }

    public static int guiTargetX(double screenOffsetFromCenter, BlockPos center, int range) {
        return (int) (screenOffsetFromCenter * ((double) range * 2.0D + 1.0D) / GUI_BLIP_RANGE + center.getX());
    }

    public static int guiTargetZ(double screenOffsetFromCenter, BlockPos center, int range) {
        return (int) (screenOffsetFromCenter * ((double) range * 2.0D + 1.0D) / GUI_BLIP_RANGE + center.getZ());
    }

    public static WorldOffset worldBlipOffset(BlockPos entry, BlockPos reference, int range) {
        double divisor = (double) range + 1.0D;
        double x = (entry.getX() - reference.getX()) / divisor * WORLD_BLIP_RANGE;
        double z = (entry.getZ() - reference.getZ()) / divisor * WORLD_BLIP_RANGE;
        return new WorldOffset(x, z);
    }

    public record ScreenOffset(double x, double z) {
    }

    public record WorldOffset(double x, double z) {
    }

    private RadarDisplayProjection() {
    }
}
