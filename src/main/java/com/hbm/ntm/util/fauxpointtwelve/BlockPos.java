package com.hbm.ntm.util.fauxpointtwelve;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Legacy 1.12-style mutable block position helper.
 */
@Deprecated(forRemoval = false)
public class BlockPos implements Cloneable {
    private int x;
    private int y;
    private int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos(BlockEntity blockEntity) {
        this(blockEntity.getBlockPos());
    }

    public BlockPos(net.minecraft.core.BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPos(double x, double y, double z) {
        this(Mth.floor(x), Mth.floor(y), Mth.floor(z));
    }

    public BlockPos mutate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public boolean compare(int x, int y, int z) {
        return this.x == x && this.y == y && this.z == z;
    }

    public BlockPos add(int x, int y, int z) {
        return x == 0 && y == 0 && z == 0 ? this : new BlockPos(this.x + x, this.y + y, this.z + z);
    }

    public BlockPos add(double x, double y, double z) {
        return x == 0.0D && y == 0.0D && z == 0.0D
                ? this
                : new BlockPos(this.x + x, this.y + y, this.z + z);
    }

    public BlockPos add(BlockPos vec) {
        return add(vec.getX(), vec.getY(), vec.getZ());
    }

    public BlockPos rotate(Rotation rotation) {
        return switch (rotation) {
            case NONE -> this;
            case CLOCKWISE_90 -> new BlockPos(-z, y, x);
            case CLOCKWISE_180 -> new BlockPos(-x, y, -z);
            case COUNTERCLOCKWISE_90 -> new BlockPos(z, y, -x);
        };
    }

    public BlockPos offset(Direction direction) {
        return offset(direction, 1);
    }

    public BlockPos offset(Direction direction, int distance) {
        return new BlockPos(x + direction.getStepX() * distance,
                y + direction.getStepY() * distance,
                z + direction.getStepZ() * distance);
    }

    public net.minecraft.core.BlockPos immutable() {
        return new net.minecraft.core.BlockPos(x, y, z);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public int hashCode() {
        return getIdentity(x, y, z);
    }

    public static int getIdentity(int x, int y, int z) {
        return (y + z * 27644437) * 27644437 + x;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof BlockPos pos)) {
            return false;
        }
        return x == pos.x && y == pos.y && z == pos.z;
    }

    @Override
    public BlockPos clone() {
        try {
            return (BlockPos) super.clone();
        } catch (CloneNotSupportedException ignored) {
            return null;
        }
    }
}
