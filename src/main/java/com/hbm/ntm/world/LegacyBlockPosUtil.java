package com.hbm.ntm.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class LegacyBlockPosUtil {
    public static BlockPos fromFloored(double x, double y, double z) {
        return new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
    }

    public static boolean compare(BlockPos pos, int x, int y, int z) {
        return pos.getX() == x && pos.getY() == y && pos.getZ() == z;
    }

    public static BlockPos rotate(BlockPos pos, LegacyRotation rotation) {
        return switch (rotation) {
            case NONE -> pos;
            case CLOCKWISE_90 -> new BlockPos(-pos.getZ(), pos.getY(), pos.getX());
            case CLOCKWISE_180 -> new BlockPos(-pos.getX(), pos.getY(), -pos.getZ());
            case COUNTERCLOCKWISE_90 -> new BlockPos(pos.getZ(), pos.getY(), -pos.getX());
        };
    }

    public static BlockPos offset(BlockPos pos, Direction direction) {
        return offset(pos, direction, 1);
    }

    public static BlockPos offset(BlockPos pos, Direction direction, int distance) {
        return pos.offset(direction.getStepX() * distance, direction.getStepY() * distance,
                direction.getStepZ() * distance);
    }

    public static int legacyIdentity(BlockPos pos) {
        return legacyIdentity(pos.getX(), pos.getY(), pos.getZ());
    }

    public static int legacyIdentity(int x, int y, int z) {
        return (y + z * 27644437) * 27644437 + x;
    }

    private LegacyBlockPosUtil() {
    }
}
