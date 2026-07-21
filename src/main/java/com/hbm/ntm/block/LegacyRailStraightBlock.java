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

/**
 * Source-backed straight custom rails from the 1.7.10 rail-car system.
 *
 * <p>The three variants deliberately share only the formula that was shared
 * by their legacy {@code snapAndMove} implementations: placement extents,
 * gauge, vertical snapping height and endpoint distances remain variant data.</p>
 */
@SuppressWarnings("deprecation")
public final class LegacyRailStraightBlock extends LegacyXrMultiblockBlock implements HbmRail {
    private static final VoxelShape RAIL_SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public enum Variant {
        NARROW_FIVE(new int[] {0, 0, 2, 2, 0, 0}, 2, TrackGauge.NARROW, -2, 3, 3, 0.0D, true),
        STANDARD_FIVE(new int[] {0, 0, 2, 2, 1, 0}, 2, TrackGauge.STANDARD, -2, 3, 3, 0.1875D, false),
        STANDARD_ONE(new int[] {0, 0, 0, 0, 1, 0}, 0, TrackGauge.STANDARD, 0, 1, 1, 0.1875D, false);

        private final int[] dimensions;
        private final int placementOffset;
        private final TrackGauge gauge;
        private final int minimum;
        private final int maximum;
        private final int endpoint;
        private final double railHeight;
        private final boolean visibleDummies;

        Variant(int[] dimensions, int placementOffset, TrackGauge gauge, int minimum, int maximum, int endpoint,
                double railHeight, boolean visibleDummies) {
            this.dimensions = dimensions;
            this.placementOffset = placementOffset;
            this.gauge = gauge;
            this.minimum = minimum;
            this.maximum = maximum;
            this.endpoint = endpoint;
            this.railHeight = railHeight;
            this.visibleDummies = visibleDummies;
        }
    }

    private final Variant variant;

    public LegacyRailStraightBlock(Properties properties, Variant variant) {
        super(properties);
        this.variant = variant;
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return variant.dimensions;
    }

    @Override
    protected int getLegacyOffset() {
        return variant.placementOffset;
    }

    @Override
    protected BlockState getLegacyDummyState(BlockState coreState, BlockPos offset) {
        if (variant.visibleDummies) {
            return ModBlocks.RAIL_NARROW_DUMMY.get().defaultBlockState()
                    .setValue(RailDummyBlock.FACING, coreState.getValue(FACING));
        }
        return ModBlocks.RAIL_DUMMY.get().defaultBlockState()
                .setValue(RailDummyBlock.FACING, coreState.getValue(FACING));
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
        boolean alongX = direction.getAxis() == Direction.Axis.X;
        double coreX = corePos.getX();
        double coreZ = corePos.getZ();
        double railY = railPos.getY() + variant.railHeight;

        if (alongX) {
            double target = trainPosition.x + (motion.x > 0.0D ? speed : -speed);
            context.yaw(motion.x > 0.0D ? -90.0F : 90.0F);
            double clamped = Mth.clamp(target, coreX + variant.minimum, coreX + variant.maximum);
            context.overshoot(Math.abs(target - clamped) * Math.signum(speed));
            context.nextRailPos(new BlockPos(corePos.getX() + (motion.x * speed > 0.0D
                    ? variant.endpoint : -variant.endpoint), railPos.getY(), corePos.getZ()));
            return new Vec3(clamped, railY, coreZ + 0.5D + rotation.getStepZ() * 0.5D);
        }

        double target = trainPosition.z + (motion.z > 0.0D ? speed : -speed);
        context.yaw(motion.z > 0.0D ? 0.0F : 180.0F);
        double clamped = Mth.clamp(target, coreZ + variant.minimum, coreZ + variant.maximum);
        context.overshoot(Math.abs(target - clamped) * Math.signum(speed));
        context.nextRailPos(new BlockPos(corePos.getX(), railPos.getY(), corePos.getZ()
                + (motion.z * speed > 0.0D ? variant.endpoint : -variant.endpoint)));
        return new Vec3(coreX + 0.5D + rotation.getStepX() * 0.5D, railY, clamped);
    }
}
