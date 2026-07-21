package com.hbm.ntm.block;

import com.hbm.ntm.api.conveyor.ConveyorMath;
import com.hbm.ntm.api.conveyor.IConveyorBelt;
import com.hbm.ntm.blockentity.CraneLogisticsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;

/**
 * Legacy CranePartitioner is one of the crane variants that is itself a conveyor belt.
 */
public final class CranePartitionerBlock extends CraneLogisticsBlock implements IConveyorBelt {
    private static final VoxelShape SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public CranePartitionerBlock(Properties properties) {
        super(properties, CraneLogisticsBlockEntity.Kind.PARTITIONER);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean canItemStay(Level level, BlockPos pos, Vec3 itemPos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {
        Direction direction = stateFacing(level.getBlockState(pos));
        Vec3 snap = getClosestSnappingPosition(level, pos, itemPos);
        return ConveyorMath.travelLocation(pos, itemPos, direction, snap, speed);
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {
        return ConveyorMath.closestSnappingPosition(pos, itemPos, stateFacing(level.getBlockState(pos)));
    }

    public static float legacyRenderRotationDegrees(BlockState state) {
        Direction facing = stateFacing(state);
        return switch (facing) {
            case NORTH -> 90.0F;
            case WEST -> 180.0F;
            case SOUTH -> 270.0F;
            default -> 0.0F;
        };
    }
}
