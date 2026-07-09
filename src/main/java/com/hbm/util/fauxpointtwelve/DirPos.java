package com.hbm.util.fauxpointtwelve;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Legacy package bridge for the old faux-1.12 position plus side helper.
 */
@Deprecated(forRemoval = false)
public class DirPos extends BlockPos {
    protected ForgeDirection dir;

    public DirPos(int x, int y, int z, ForgeDirection dir) {
        super(x, y, z);
        this.dir = dir;
    }

    public DirPos(BlockEntity blockEntity, ForgeDirection dir) {
        super(blockEntity);
        this.dir = dir;
    }

    public DirPos(double x, double y, double z, ForgeDirection dir) {
        super(x, y, z);
        this.dir = dir;
    }

    public DirPos(net.minecraft.core.BlockPos pos, ForgeDirection dir) {
        super(pos);
        this.dir = dir;
    }

    public DirPos(int x, int y, int z, Direction dir) {
        this(x, y, z, ForgeDirection.fromDirection(dir));
    }

    public DirPos(BlockEntity blockEntity, Direction dir) {
        this(blockEntity, ForgeDirection.fromDirection(dir));
    }

    public DirPos(double x, double y, double z, Direction dir) {
        this(x, y, z, ForgeDirection.fromDirection(dir));
    }

    public DirPos(net.minecraft.core.BlockPos pos, Direction dir) {
        this(pos, ForgeDirection.fromDirection(dir));
    }

    @Override
    public DirPos rotate(Rotation rotation) {
        return switch (rotation) {
            case NONE -> this;
            case CLOCKWISE_90 -> new DirPos(-getZ(), getY(), getX(), getDir().getRotation(ForgeDirection.UP));
            case CLOCKWISE_180 -> new DirPos(-getX(), getY(), -getZ(), getDir().getOpposite());
            case COUNTERCLOCKWISE_90 -> new DirPos(getZ(), getY(), -getX(), getDir().getRotation(ForgeDirection.DOWN));
        };
    }

    @Override
    public DirPos rotate(com.hbm.ntm.util.fauxpointtwelve.Rotation rotation) {
        return rotate(Rotation.fromModern(rotation));
    }

    public ForgeDirection getDir() {
        return dir;
    }

    public Direction modernDir() {
        return dir.toDirection();
    }
}
