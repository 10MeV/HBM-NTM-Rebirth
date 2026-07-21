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

public class ChuteConveyorBlock extends ConveyorBlock {
    public static final BooleanProperty FREE_BOTTOM = BooleanProperty.create("free_bottom");
    public static final BooleanProperty WEST_BELT = BooleanProperty.create("west_belt");
    public static final BooleanProperty EAST_BELT = BooleanProperty.create("east_belt");
    public static final BooleanProperty NORTH_BELT = BooleanProperty.create("north_belt");
    public static final BooleanProperty SOUTH_BELT = BooleanProperty.create("south_belt");
    private static final VoxelShape FULL_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public ChuteConveyorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FREE_BOTTOM, false)
                .setValue(WEST_BELT, false)
                .setValue(EAST_BELT, false)
                .setValue(NORTH_BELT, false)
                .setValue(SOUTH_BELT, false));
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return updateVisualState(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(state.getBlock())) {
            BlockState updated = updateVisualState(state, level, pos);
            if (updated != state) {
                level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return updateVisualState(super.updateShape(state, direction, neighborState, level, pos, neighborPos), level, pos);
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {
        return ConveyorMath.chuteTravelLocation(level, pos, legacyMetadata(level.getBlockState(pos)), itemPos, speed);
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {
        return ConveyorMath.chuteSnappingPosition(level, pos, legacyMetadata(level.getBlockState(pos)), itemPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FULL_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FULL_SHAPE;
    }

    private static BlockState updateVisualState(BlockState state, LevelAccessor level, BlockPos pos) {
        boolean hasBelow = pos.getY() > level.getMinBuildHeight();
        return withVisualState(state, hasBelow, ConveyorMath.isConveyorOrEnterable(level, pos.below()),
                ConveyorMath.isConveyor(level, pos.west()), ConveyorMath.isConveyor(level, pos.east()),
                ConveyorMath.isConveyor(level, pos.north()), ConveyorMath.isConveyor(level, pos.south()));
    }

    /** Shared by placed blocks and the wand's planned-route preview. */
    public static BlockState withVisualState(BlockState state, boolean hasBelow, boolean conveyorOrEnterableBelow,
            boolean westBelt, boolean eastBelt, boolean northBelt, boolean southBelt) {
        return state
                .setValue(FREE_BOTTOM, hasBelow && !conveyorOrEnterableBelow)
                .setValue(WEST_BELT, westBelt)
                .setValue(EAST_BELT, eastBelt)
                .setValue(NORTH_BELT, northBelt)
                .setValue(SOUTH_BELT, southBelt);
    }

    @Override
    public Direction getInputDirection(BlockState state) {
        return Direction.UP;
    }

    @Override
    public Direction getOutputDirection(BlockState state) {
        return Direction.DOWN;
    }

    @Override
    public boolean supportsWandEdgeSnapping(BlockState state) {
        return false;
    }

    @Override
    protected BlockState nextScrewdriverState(BlockState state, int metadata, int baseMetadata,
            ConveyorPathType path, boolean sneaking) {
        if (sneaking) {
            return ((ConveyorBlock) ModBlocks.CONVEYOR.get()).stateFromLegacyMetadata(baseMetadata);
        }
        // BlockConveyorChute#onScrew likewise reset bend metadata before rotating.
        return stateFromLegacyMetadata(ConveyorMath.legacyHorizontalDirection(baseMetadata)
                .getClockWise().get3DDataValue());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FREE_BOTTOM, WEST_BELT, EAST_BELT, NORTH_BELT, SOUTH_BELT);
    }
}
