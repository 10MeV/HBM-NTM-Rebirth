package com.hbm.ntm.util.fauxpointtwelve;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Legacy 1.12-style position plus side helper.
 */
@Deprecated(forRemoval = false)
public class DirPos extends BlockPos {
    protected Direction dir;

    public DirPos(int x, int y, int z, Direction dir) {
        super(x, y, z);
        this.dir = dir;
    }

    public DirPos(BlockEntity blockEntity, Direction dir) {
        super(blockEntity);
        this.dir = dir;
    }

    public DirPos(double x, double y, double z, Direction dir) {
        super(x, y, z);
        this.dir = dir;
    }

    public DirPos(net.minecraft.core.BlockPos pos, Direction dir) {
        super(pos);
        this.dir = dir;
    }

    @Override
    public DirPos rotate(Rotation rotation) {
        return switch (rotation) {
            case NONE -> this;
            case CLOCKWISE_90 -> new DirPos(-getZ(), getY(), getX(), rotateClockwise(dir));
            case CLOCKWISE_180 -> new DirPos(-getX(), getY(), -getZ(), dir.getOpposite());
            case COUNTERCLOCKWISE_90 -> new DirPos(getZ(), getY(), -getX(), rotateCounterClockwise(dir));
        };
    }

    public Direction getDir() {
        return dir;
    }

    private static Direction rotateClockwise(Direction direction) {
        return direction.getAxis().isHorizontal() ? direction.getClockWise() : direction;
    }

    private static Direction rotateCounterClockwise(Direction direction) {
        return direction.getAxis().isHorizontal() ? direction.getCounterClockWise() : direction;
    }
}
