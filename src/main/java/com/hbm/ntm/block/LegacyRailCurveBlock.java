package com.hbm.ntm.block;

import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.rail.HbmRail;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10 custom rail curves. The three standard-gauge variants deliberately
 * retain their irregular source layouts instead of being approximated as
 * rectangular multiblocks.
 */
@SuppressWarnings("deprecation")
public final class LegacyRailCurveBlock extends LegacyXrMultiblockBlock implements HbmRail {
    private static final VoxelShape RAIL_SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    private static final Segment[] STANDARD_FIVE = {
            segment(1, 0, Axis.D), segment(0, 1, Axis.R), segment(1, 1, Axis.R), segment(1, 2, Axis.R),
            segment(2, 1, Axis.D), segment(2, 2, Axis.D), segment(3, 1, Axis.D), segment(3, 2, Axis.D),
            segment(2, 3, Axis.R), segment(3, 3, Axis.R), segment(4, 3, Axis.D), segment(3, 4, Axis.R),
            segment(4, 4, Axis.D)
    };
    private static final Segment[] STANDARD_SEVEN = {
            segment(1, 0, Axis.D), segment(2, 0, Axis.D), segment(0, 1, Axis.R), segment(1, 1, Axis.R),
            segment(2, 1, Axis.R), segment(3, 1, Axis.D), segment(4, 1, Axis.D), segment(2, 2, Axis.R),
            segment(3, 2, Axis.R), segment(4, 2, Axis.R), segment(5, 2, Axis.D), segment(3, 3, Axis.R),
            segment(4, 3, Axis.R), segment(5, 3, Axis.R), segment(4, 4, Axis.R), segment(5, 4, Axis.R),
            segment(6, 4, Axis.D), segment(5, 5, Axis.R), segment(5, 6, Axis.R), segment(6, 5, Axis.D),
            segment(6, 6, Axis.D)
    };
    private static final Segment[] STANDARD_NINE = {
            segment(1, 0, Axis.D), segment(2, 0, Axis.D), segment(0, 1, Axis.R), segment(1, 1, Axis.D),
            segment(2, 1, Axis.D), segment(3, 1, Axis.D), segment(4, 1, Axis.D), segment(2, 2, Axis.R),
            segment(3, 2, Axis.R), segment(4, 2, Axis.R), segment(5, 2, Axis.D), segment(4, 3, Axis.R),
            segment(5, 3, Axis.R), segment(5, 4, Axis.R), segment(6, 3, Axis.D), segment(6, 4, Axis.D),
            segment(7, 4, Axis.D), segment(6, 5, Axis.R), segment(7, 5, Axis.R), segment(6, 6, Axis.R),
            segment(7, 6, Axis.R), segment(7, 7, Axis.R), segment(7, 8, Axis.R), segment(8, 6, Axis.D),
            segment(8, 7, Axis.D), segment(8, 8, Axis.D)
    };

    public enum Variant {
        NARROW(4.5D, TrackGauge.NARROW, 0.0D, null, false),
        STANDARD_FIVE(4.0D, TrackGauge.STANDARD, 0.1875D, LegacyRailCurveBlock.STANDARD_FIVE, false),
        STANDARD_SEVEN(6.0D, TrackGauge.STANDARD, 0.1875D, LegacyRailCurveBlock.STANDARD_SEVEN, true),
        STANDARD_NINE(8.0D, TrackGauge.STANDARD, 0.1875D, LegacyRailCurveBlock.STANDARD_NINE, false);

        private final double radius;
        private final TrackGauge gauge;
        private final double railHeight;
        private final Segment[] segments;
        private final boolean sourceUncheckedFill;

        Variant(double radius, TrackGauge gauge, double railHeight, Segment[] segments, boolean sourceUncheckedFill) {
            this.radius = radius;
            this.gauge = gauge;
            this.railHeight = railHeight;
            this.segments = segments;
            this.sourceUncheckedFill = sourceUncheckedFill;
        }
    }

    private enum Axis { D, R }

    private record Segment(int d, int r, Axis axis) {
    }

    private final Variant variant;

    public LegacyRailCurveBlock(Properties properties, Variant variant) {
        super(properties);
        this.variant = variant;
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return variant == Variant.NARROW ? new int[] {0, 0, 4, 0, 4, 0} : new int[] {0, 0, 0, 0, 0, 0};
    }

    @Override
    protected int getLegacyOffset() {
        return 0;
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        if (variant.segments == null) {
            return super.getLayout(state);
        }
        Direction direction = state.getValue(FACING);
        Direction backward = direction.getOpposite();
        Direction rotation = direction.getClockWise();
        List<BlockPos> offsets = new ArrayList<>();
        offsets.add(BlockPos.ZERO);
        for (Segment segment : variant.segments) {
            offsets.add(offset(backward, rotation, segment));
        }
        return LegacyMultiblockLayout.ofOffsets(offsets);
    }

    @Override
    public boolean canPlaceDirectMultiblock(Level level, BlockPos corePos, BlockPos temporaryPos, BlockState state) {
        if (variant != Variant.STANDARD_SEVEN) {
            return super.canPlaceDirectMultiblock(level, corePos, temporaryPos, state);
        }
        // RailStandardCurveWide7 inherited RailStandardCurveBase#checkRequirement:
        // it only prechecked the old five-metre coordinate set, then filled all
        // of its own seven-metre segments under BlockDummyable.safeRem.
        Direction direction = state.getValue(FACING);
        Direction backward = direction.getOpposite();
        Direction rotation = direction.getClockWise();
        List<BlockPos> checkedOffsets = new ArrayList<>();
        for (Segment segment : STANDARD_FIVE) {
            checkedOffsets.add(offset(backward, rotation, segment));
        }
        return MultiblockHelper.checkSpace(level, corePos, checkedOffsets, temporaryPos);
    }

    @Override
    protected boolean usesUncheckedLegacyDummyFill(BlockState state) {
        return variant.sourceUncheckedFill;
    }

    @Override
    protected BlockState getLegacyDummyState(BlockState coreState, BlockPos offset) {
        Direction direction = coreState.getValue(FACING);
        Direction dummyFacing = direction;
        if (variant.segments != null) {
            Direction backward = direction.getOpposite();
            Direction rotation = direction.getClockWise();
            for (Segment segment : variant.segments) {
                if (offset(backward, rotation, segment).equals(offset)) {
                    dummyFacing = segment.axis == Axis.D ? backward : rotation;
                    break;
                }
            }
        }
        return ModBlocks.RAIL_DUMMY.get().defaultBlockState().setValue(RailDummyBlock.FACING, dummyFacing);
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
        return snapAndMove(level, railPos, trainPosition, Vec3.ZERO, 0.0D, new RailContext());
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos railPos, Vec3 trainPosition, Vec3 motion,
            double speed, RailContext context, MoveContext moveContext) {
        return snapAndMove(level, railPos, trainPosition, motion, speed, context);
    }

    @Override
    public TrackGauge getGauge(Level level, BlockPos railPos) {
        return variant.gauge;
    }

    private Vec3 snapAndMove(Level level, BlockPos railPos, Vec3 trainPosition, Vec3 motion, double speed,
            RailContext context) {
        BlockPos corePos = MultiblockHelper.resolveCorePos(level, railPos);
        BlockState coreState = level.getBlockState(corePos);
        if (coreState.getBlock() != this) {
            return trainPosition;
        }

        Direction direction = coreState.getValue(FACING);
        Direction rotation = direction.getClockWise();
        double axisDistance = variant.gauge == TrackGauge.NARROW ? variant.radius : variant.radius + 0.5D;
        double axisX = corePos.getX() + 0.5D + direction.getStepX() * 0.5D + rotation.getStepX() * axisDistance;
        double axisZ = corePos.getZ() + 0.5D + direction.getStepZ() * 0.5D + rotation.getStepZ() * axisDistance;
        Vec3 distance = new Vec3(trainPosition.x - axisX, 0.0D, trainPosition.z - axisZ).normalize().scale(variant.radius);
        double moveAngle = Math.atan2(motion.x, motion.z) * 180.0D / Math.PI + 90.0D;

        if (speed == 0.0D) {
            context.overshoot(0.0D).nextRailPos(railPos).yaw((float) moveAngle);
            return new Vec3(axisX + distance.x, railPos.getY() + variant.railHeight, axisZ + distance.z);
        }

        double angle = Math.atan2(distance.x, distance.z) * 180.0D / Math.PI + 90.0D;
        if (direction == Direction.WEST) {
            angle -= 90.0D;
        } else if (direction == Direction.EAST) {
            angle += 90.0D;
        } else if (direction == Direction.SOUTH) {
            angle += 180.0D;
        }
        angle = Mth.wrapDegrees(angle);
        double quarterLength = variant.radius * Math.PI / 2.0D;
        double angularChange = speed / quarterLength * 90.0D;
        Direction movementDirection = Math.abs(motion.x) > Math.abs(motion.z)
                ? (motion.x > 0.0D ? Direction.EAST : Direction.WEST)
                : (motion.z > 0.0D ? Direction.SOUTH : Direction.NORTH);
        if (movementDirection == direction || movementDirection == rotation.getOpposite()) {
            angularChange *= -1.0D;
        }

        double effectiveAngle = angle + angularChange;
        moveAngle += angularChange;
        double railY = railPos.getY() + variant.railHeight;
        if (effectiveAngle > 90.0D) {
            double angleOvershoot = effectiveAngle - 90.0D;
            moveAngle -= angleOvershoot;
            double lengthOvershoot = angleOvershoot * quarterLength / 90.0D;
            context.overshoot(lengthOvershoot * Math.signum(speed * angularChange))
                    .nextRailPos(corePos.offset(-direction.getStepX() * (int) variant.radius
                                    + rotation.getStepX() * ((int) variant.radius + 1), 0,
                            -direction.getStepZ() * (int) variant.radius
                                    + rotation.getStepZ() * ((int) variant.radius + 1)))
                    .yaw((float) moveAngle);
            return new Vec3(axisX - direction.getStepX() * variant.radius, railY,
                    axisZ - direction.getStepZ() * variant.radius);
        }
        if (effectiveAngle < 0.0D) {
            double angleOvershoot = -effectiveAngle;
            moveAngle -= angleOvershoot;
            double lengthOvershoot = angleOvershoot * quarterLength / 90.0D;
            context.overshoot(-lengthOvershoot * Math.signum(speed * angularChange))
                    .nextRailPos(corePos.relative(direction)).yaw((float) moveAngle);
            return new Vec3(axisX - rotation.getStepX() * variant.radius, railY,
                    axisZ - rotation.getStepZ() * variant.radius);
        }
        Vec3 turned = distance.yRot((float) Math.toRadians(angularChange));
        return new Vec3(axisX + turned.x, railY, axisZ + turned.z);
    }

    private static Segment segment(int d, int r, Axis axis) {
        return new Segment(d, r, axis);
    }

    private static BlockPos offset(Direction backward, Direction rotation, Segment segment) {
        return new BlockPos(backward.getStepX() * segment.d + rotation.getStepX() * segment.r, 0,
                backward.getStepZ() * segment.d + rotation.getStepZ() * segment.r);
    }
}
