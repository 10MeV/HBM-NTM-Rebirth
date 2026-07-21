package com.hbm.ntm.block;

import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.rail.HbmRail;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Source-backed standard-gauge buffer stop with bogie-aware end collision. */
@SuppressWarnings("deprecation")
public final class LegacyRailBufferBlock extends LegacyXrMultiblockBlock implements HbmRail {
    private static final VoxelShape RAIL_SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public LegacyRailBufferBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return new int[] {0, 0, 2, 2, 1, 0};
    }

    @Override
    protected int getLegacyOffset() {
        return 2;
    }

    @Override
    protected BlockState getLegacyDummyState(BlockState coreState, BlockPos offset) {
        return ModBlocks.RAIL_DUMMY.get().defaultBlockState().setValue(RailDummyBlock.FACING, coreState.getValue(FACING));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return RAIL_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return RAIL_SHAPE;
    }

    @Override
    public VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return RAIL_SHAPE;
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return RAIL_SHAPE;
    }

    @Override
    public boolean usesLocalDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesLocalDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public Vec3 getSnappingPosition(Level level, BlockPos railPos, Vec3 trainPosition) {
        return snapAndMove(level, railPos, trainPosition, Vec3.ZERO, 0.0D, new RailContext(),
                new MoveContext(RailCheckType.OTHER, 0.0D));
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos railPos, Vec3 trainPosition, Vec3 motion,
            double speed, RailContext context, MoveContext moveContext) {
        return snapAndMove(level, railPos, trainPosition, motion, speed, context, moveContext);
    }

    @Override
    public TrackGauge getGauge(Level level, BlockPos railPos) {
        return TrackGauge.STANDARD;
    }

    private Vec3 snapAndMove(Level level, BlockPos railPos, Vec3 trainPosition, Vec3 motion, double speed,
            RailContext context, MoveContext moveContext) {
        BlockPos corePos = MultiblockHelper.resolveCorePos(level, railPos);
        BlockState coreState = level.getBlockState(corePos);
        if (coreState.getBlock() != this) {
            return trainPosition;
        }
        Direction direction = coreState.getValue(FACING);
        Direction rotation = direction.getClockWise();
        if (direction.getAxis() == Direction.Axis.X) {
            double target = trainPosition.x + (motion.x > 0.0D ? speed : -speed);
            context.yaw(motion.x > 0.0D ? -90.0F : 90.0F);
            double railPosition = Mth.clamp(target, corePos.getX() - 2.0D, corePos.getX() + 3.0D);
            double negativeLimit = direction == Direction.EAST ? -1.0D - moveContext.collisionBogieDistance() : 2.0D;
            double positiveLimit = direction == Direction.WEST ? -moveContext.collisionBogieDistance() : 3.0D;
            double buffered = Mth.clamp(target, corePos.getX() - negativeLimit, corePos.getX() + positiveLimit);
            if (buffered != railPosition) {
                moveContext.markCollision(Math.abs(buffered - railPosition));
                return new Vec3(buffered, railPos.getY() + 0.1875D,
                        corePos.getZ() + 0.5D + rotation.getStepZ() * 0.5D);
            }
            context.overshoot(Math.abs(target - railPosition) * Math.signum(speed))
                    .nextRailPos(new BlockPos(corePos.getX() + (motion.x * speed > 0.0D ? 3 : -3),
                            railPos.getY(), corePos.getZ()));
            return new Vec3(railPosition, railPos.getY() + 0.1875D,
                    corePos.getZ() + 0.5D + rotation.getStepZ() * 0.5D);
        }
        double target = trainPosition.z + (motion.z > 0.0D ? speed : -speed);
        context.yaw(motion.z > 0.0D ? 0.0F : 180.0F);
        double railPosition = Mth.clamp(target, corePos.getZ() - 2.0D, corePos.getZ() + 3.0D);
        double negativeLimit = direction == Direction.SOUTH ? -1.0D - moveContext.collisionBogieDistance() : 2.0D;
        double positiveLimit = direction == Direction.NORTH ? -moveContext.collisionBogieDistance() : 3.0D;
        double buffered = Mth.clamp(target, corePos.getZ() - negativeLimit, corePos.getZ() + positiveLimit);
        // This compares against X exactly as RailStandardBuffer did before it
        // wrote vec.zCoord; it is an intentional source contract, not a typo fix.
        double fixedX = corePos.getX() + 0.5D + rotation.getStepX() * 0.5D;
        if (buffered != fixedX) {
            moveContext.markCollision(Math.abs(buffered - railPosition));
            return new Vec3(fixedX, railPos.getY() + 0.1875D, buffered);
        }
        context.overshoot(Math.abs(target - railPosition) * Math.signum(speed))
                .nextRailPos(new BlockPos(corePos.getX(), railPos.getY(),
                        corePos.getZ() + (motion.z * speed > 0.0D ? 3 : -3)));
        return new Vec3(fixedX, railPos.getY() + 0.1875D, railPosition);
    }
}
