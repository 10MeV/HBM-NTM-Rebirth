package com.hbm.ntm.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DirPos extends BlockPos {
    private final Direction direction;

    public DirPos(int x, int y, int z, Direction direction) {
        super(x, y, z);
        this.direction = direction;
    }

    public DirPos(double x, double y, double z, Direction direction) {
        this(Mth.floor(x), Mth.floor(y), Mth.floor(z), direction);
    }

    public DirPos(BlockEntity blockEntity, Direction direction) {
        this(blockEntity.getBlockPos(), direction);
    }

    public DirPos(BlockPos pos, Direction direction) {
        this(pos.getX(), pos.getY(), pos.getZ(), direction);
    }

    public Direction getDir() {
        return direction;
    }

    public Direction direction() {
        return direction;
    }

    public DirPos rotate(LegacyRotation rotation) {
        return switch (rotation) {
            case NONE -> this;
            case CLOCKWISE_90 -> new DirPos(-getZ(), getY(), getX(), rotateDirection(rotation));
            case CLOCKWISE_180 -> new DirPos(-getX(), getY(), -getZ(), direction.getOpposite());
            case COUNTERCLOCKWISE_90 -> new DirPos(getZ(), getY(), -getX(), rotateDirection(rotation));
        };
    }

    private Direction rotateDirection(LegacyRotation rotation) {
        if (direction.getAxis().isVertical()) {
            return direction;
        }
        return rotation == LegacyRotation.CLOCKWISE_90 ? direction.getClockWise() : direction.getCounterClockWise();
    }
}
