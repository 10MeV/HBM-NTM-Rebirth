package com.hbm.ntm.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class LegacyBlockPosUtil {
    public static BlockPos fromFloored(double x, double y, double z) {
        return new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
    }

    public static BlockPos fromBlockEntity(BlockEntity blockEntity) {
        return blockEntity.getBlockPos();
    }

    public static BlockPos copy(BlockPos pos) {
        return new BlockPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos clone(BlockPos pos) {
        return copy(pos);
    }

    public static BlockPos.MutableBlockPos mutable(int x, int y, int z) {
        return new BlockPos.MutableBlockPos(x, y, z);
    }

    public static BlockPos.MutableBlockPos mutable(BlockPos pos) {
        return mutable(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos.MutableBlockPos mutate(BlockPos.MutableBlockPos pos, int x, int y, int z) {
        return pos.set(x, y, z);
    }

    public static BlockPos.MutableBlockPos mutate(BlockPos.MutableBlockPos pos, BlockPos value) {
        return mutate(pos, value.getX(), value.getY(), value.getZ());
    }

    public static boolean compare(BlockPos pos, int x, int y, int z) {
        return pos.getX() == x && pos.getY() == y && pos.getZ() == z;
    }

    public static BlockPos add(BlockPos pos, int x, int y, int z) {
        return x == 0 && y == 0 && z == 0 ? pos : pos.offset(x, y, z);
    }

    public static BlockPos add(BlockPos pos, double x, double y, double z) {
        return x == 0.0D && y == 0.0D && z == 0.0D ? pos
                : fromFloored(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
    }

    public static BlockPos add(BlockPos pos, BlockPos offset) {
        return add(pos, offset.getX(), offset.getY(), offset.getZ());
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
