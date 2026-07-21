package com.hbm.ntm.block;

import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Source-backed five-metre standard-gauge ramp rail. */
@SuppressWarnings("deprecation")
public final class LegacyRailRampBlock extends LegacyXrMultiblockBlock implements HbmRail {
    private static final int[] BASE_DIMENSIONS = {0, 0, 2, 2, 1, 0};
    private static final int[] UPPER_DIMENSIONS = {1, -1, 2, 2, 1, 0};
    private static final double[] HEIGHTS = {0.1D, 0.3D, 0.5D, 0.7D, 0.9D};

    public LegacyRailRampBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return BASE_DIMENSIONS;
    }

    @Override
    protected int getLegacyOffset() {
        return 2;
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        Direction direction = state.getValue(FACING);
        BlockPos upperOrigin = new BlockPos(direction.getStepX() * getLegacyOffset(), 0,
                direction.getStepZ() * getLegacyOffset());
        return super.getLayout(state).withLegacyXrCheckedFill(UPPER_DIMENSIONS, direction, upperOrigin);
    }

    @Override
    protected BlockState getLegacyDummyState(BlockState coreState, BlockPos offset) {
        return ModBlocks.RAIL_DUMMY.get().defaultBlockState().setValue(RailDummyBlock.FACING, coreState.getValue(FACING));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return rampShape(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return rampShape(state.getValue(FACING));
    }

    @Override
    public VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return rampShape(state.getValue(FACING));
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return rampShape(state.getValue(FACING));
    }

    @Override
    public boolean usesForwardedDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesForwardedDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesMultiblockHighlightShape(BlockState state, BlockGetter level, BlockPos corePos) {
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
        return TrackGauge.STANDARD;
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
        if (direction.getAxis() == Direction.Axis.X) {
            double target = trainPosition.x + (motion.x > 0.0D ? speed : -speed);
            context.yaw(motion.x > 0.0D ? -90.0F : 90.0F);
            double slope = (corePos.getX() + 0.5D - target + 2.5D) / 5.0D;
            double clamped = Mth.clamp(target, corePos.getX() - 2.0D, corePos.getX() + 3.0D);
            double y = Mth.clamp(direction == Direction.EAST ? corePos.getY() + slope : corePos.getY() + 1.0D - slope,
                    corePos.getY(), corePos.getY() + 1.0D) + 0.1875D;
            context.overshoot(Math.abs(target - clamped) * Math.signum(speed))
                    .nextRailPos(new BlockPos(corePos.getX() + (motion.x * speed > 0.0D ? 3 : -3),
                            corePos.getY() + ((motion.x * speed > 0.0D) ^ direction == Direction.EAST ? 1 : 0),
                            corePos.getZ()));
            return new Vec3(clamped, y, corePos.getZ() + 0.5D + rotation.getStepZ() * 0.5D);
        }
        double target = trainPosition.z + (motion.z > 0.0D ? speed : -speed);
        context.yaw(motion.z > 0.0D ? 0.0F : 180.0F);
        double slope = (corePos.getZ() + 0.5D - target + 2.5D) / 5.0D;
        double clamped = Mth.clamp(target, corePos.getZ() - 2.0D, corePos.getZ() + 3.0D);
        double y = Mth.clamp(direction == Direction.SOUTH ? corePos.getY() + slope : corePos.getY() + 1.0D - slope,
                corePos.getY(), corePos.getY() + 1.0D) + 0.1875D;
        context.overshoot(Math.abs(target - clamped) * Math.signum(speed))
                .nextRailPos(new BlockPos(corePos.getX(),
                        corePos.getY() + ((motion.z * speed > 0.0D) ^ direction == Direction.SOUTH ? 1 : 0),
                        corePos.getZ() + (motion.z * speed > 0.0D ? 3 : -3)));
        return new Vec3(corePos.getX() + 0.5D + rotation.getStepX() * 0.5D, y, clamped);
    }

    /** Exact legacy BlockDummyable#bounding ramp boxes, rotated around the core centre. */
    private static VoxelShape rampShape(Direction facing) {
        Direction rotation = facing.getClockWise();
        VoxelShape result = Shapes.empty();
        for (int i = 0; i < HEIGHTS.length; i++) {
            double minX = -2.5D + i;
            double maxX = minX + 1.0D;
            double minZ = -1.5D;
            double maxZ = 0.5D;
            double[] rotated = rotate(minX, maxX, minZ, maxZ, rotation);
            result = Shapes.or(result, Shapes.box(rotated[0] + 0.5D, 0.0D, rotated[2] + 0.5D,
                    rotated[1] + 0.5D, HEIGHTS[i], rotated[3] + 0.5D));
        }
        return result.optimize();
    }

    private static double[] rotate(double minX, double maxX, double minZ, double maxZ, Direction direction) {
        return switch (direction) {
            case NORTH -> new double[] {minX, maxX, minZ, maxZ};
            case EAST -> new double[] {-maxZ, -minZ, minX, maxX};
            case SOUTH -> new double[] {-maxX, -minX, -maxZ, -minZ};
            case WEST -> new double[] {minZ, maxZ, -maxX, -minX};
            default -> throw new IllegalArgumentException("Ramp rotation must be horizontal: " + direction);
        };
    }
}
