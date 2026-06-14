package com.hbm.util.fauxpointtwelve;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Legacy package bridge for the old faux-1.12 mutable BlockPos helper.
 */
@Deprecated(forRemoval = false)
public class BlockPos extends com.hbm.ntm.util.fauxpointtwelve.BlockPos {
    public BlockPos(int x, int y, int z) {
        super(x, y, z);
    }

    public BlockPos(BlockEntity blockEntity) {
        super(blockEntity);
    }

    public BlockPos(net.minecraft.core.BlockPos pos) {
        super(pos);
    }

    public BlockPos(double x, double y, double z) {
        super(x, y, z);
    }

    @Override
    public BlockPos mutate(int x, int y, int z) {
        super.mutate(x, y, z);
        return this;
    }

    @Override
    public BlockPos add(int x, int y, int z) {
        return x == 0 && y == 0 && z == 0 ? this : new BlockPos(getX() + x, getY() + y, getZ() + z);
    }

    @Override
    public BlockPos add(double x, double y, double z) {
        return x == 0.0D && y == 0.0D && z == 0.0D
                ? this
                : new BlockPos(getX() + x, getY() + y, getZ() + z);
    }

    public BlockPos add(BlockPos vec) {
        return add(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public BlockPos add(com.hbm.ntm.util.fauxpointtwelve.BlockPos vec) {
        return add(vec.getX(), vec.getY(), vec.getZ());
    }

    public BlockPos rotate(Rotation rotation) {
        return switch (rotation == null ? Rotation.NONE : rotation) {
            case NONE -> this;
            case CLOCKWISE_90 -> new BlockPos(-getZ(), getY(), getX());
            case CLOCKWISE_180 -> new BlockPos(-getX(), getY(), -getZ());
            case COUNTERCLOCKWISE_90 -> new BlockPos(getZ(), getY(), -getX());
        };
    }

    @Override
    public BlockPos rotate(com.hbm.ntm.util.fauxpointtwelve.Rotation rotation) {
        return rotate(Rotation.fromModern(rotation));
    }

    @Override
    public BlockPos offset(Direction direction) {
        return offset(direction, 1);
    }

    @Override
    public BlockPos offset(Direction direction, int distance) {
        return new BlockPos(getX() + direction.getStepX() * distance,
                getY() + direction.getStepY() * distance,
                getZ() + direction.getStepZ() * distance);
    }

    @Override
    public BlockPos clone() {
        return (BlockPos) super.clone();
    }
}
