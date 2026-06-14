package com.hbm.util.fauxpointtwelve;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Legacy package bridge for the old faux-1.12 position plus side helper.
 */
@Deprecated(forRemoval = false)
public class DirPos extends com.hbm.ntm.util.fauxpointtwelve.DirPos {
    public DirPos(int x, int y, int z, Direction dir) {
        super(x, y, z, dir);
    }

    public DirPos(BlockEntity blockEntity, Direction dir) {
        super(blockEntity, dir);
    }

    public DirPos(double x, double y, double z, Direction dir) {
        super(x, y, z, dir);
    }

    public DirPos(net.minecraft.core.BlockPos pos, Direction dir) {
        super(pos, dir);
    }

    public DirPos rotate(Rotation rotation) {
        Direction direction = getDir();
        return switch (rotation == null ? Rotation.NONE : rotation) {
            case NONE -> this;
            case CLOCKWISE_90 -> new DirPos(-getZ(), getY(), getX(), rotateClockwise(direction));
            case CLOCKWISE_180 -> new DirPos(-getX(), getY(), -getZ(), direction.getOpposite());
            case COUNTERCLOCKWISE_90 -> new DirPos(getZ(), getY(), -getX(), rotateCounterClockwise(direction));
        };
    }

    @Override
    public DirPos rotate(com.hbm.ntm.util.fauxpointtwelve.Rotation rotation) {
        return rotate(Rotation.fromModern(rotation));
    }

    @Override
    public Direction getDir() {
        return super.getDir();
    }

    private static Direction rotateClockwise(Direction direction) {
        return direction.getAxis().isHorizontal() ? direction.getClockWise() : direction;
    }

    private static Direction rotateCounterClockwise(Direction direction) {
        return direction.getAxis().isHorizontal() ? direction.getCounterClockWise() : direction;
    }
}
