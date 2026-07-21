package com.hbm.ntm.block.conveyor;

import com.hbm.ntm.api.conveyor.ConveyorMath;
import com.hbm.ntm.api.conveyor.ConveyorPathType;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiftConveyorBlock extends ConveyorBlock {
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    private static final VoxelShape FULL_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape TOP_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);

    public LiftConveyorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BOTTOM, false).setValue(TOP, false));
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return updateSegment(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(state.getBlock())) {
            BlockState updated = updateSegment(state, level, pos);
            if (updated != state) {
                level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return updateSegment(super.updateShape(state, direction, neighborState, level, pos, neighborPos), level, pos);
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {
        return ConveyorMath.liftTravelLocation(level, pos, legacyMetadata(level.getBlockState(pos)), itemPos, speed);
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {
        return ConveyorMath.liftSnappingPosition(level, pos, legacyMetadata(level.getBlockState(pos)), itemPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return ConveyorMath.isLiftTop(level, pos) ? TOP_SHAPE : FULL_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return ConveyorMath.isLiftTop(level, pos) ? TOP_SHAPE : FULL_SHAPE;
    }

    private static BlockState updateSegment(BlockState state, BlockGetter level, BlockPos pos) {
        return withSegmentState(state,
                ConveyorMath.isConveyor(level, pos.below()),
                ConveyorMath.isConveyor(level, pos.above()),
                ConveyorMath.isEnterable(level, pos.above()));
    }

    /** Shared by placed blocks and the wand's planned-route preview. */
    public static BlockState withSegmentState(BlockState state, boolean conveyorBelow, boolean conveyorAbove,
            boolean enterableAbove) {
        boolean bottom = !conveyorBelow;
        boolean top = !conveyorAbove && !bottom && !enterableAbove;
        return state.setValue(BOTTOM, bottom).setValue(TOP, top);
    }

    @Override
    public Direction getInputDirection(BlockState state) {
        return Direction.DOWN;
    }

    @Override
    public Direction getOutputDirection(BlockState state) {
        return Direction.UP;
    }

    @Override
    public boolean supportsWandEdgeSnapping(BlockState state) {
        return false;
    }

    @Override
    protected BlockState nextScrewdriverState(BlockState state, int metadata, int baseMetadata,
            ConveyorPathType path, boolean sneaking) {
        if (sneaking) {
            return ((ConveyorBlock) ModBlocks.CONVEYOR_CHUTE.get()).stateFromLegacyMetadata(baseMetadata);
        }
        // BlockConveyorLift#onScrew discarded any stored bend metadata before rotating.
        // A lift is never itself a bend, even when reached through an old malformed state.
        return stateFromLegacyMetadata(ConveyorMath.legacyHorizontalDirection(baseMetadata)
                .getClockWise().get3DDataValue());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BOTTOM, TOP);
    }
}
