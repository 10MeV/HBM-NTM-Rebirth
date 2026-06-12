package com.hbm.ntm.block;

import com.hbm.ntm.api.block.ChainExplodable;
import com.hbm.ntm.entity.item.LegacyPrimedExplosiveEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class LegacyDetCordBlock extends Block implements ChainExplodable, DetConnectibleBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final VoxelShape CORE = box(6.0D, 6.0D, 6.0D, 10.0D, 10.0D, 10.0D);
    private static final VoxelShape NORTH_ARM = box(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 6.0D);
    private static final VoxelShape EAST_ARM = box(10.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);
    private static final VoxelShape SOUTH_ARM = box(6.0D, 6.0D, 10.0D, 10.0D, 10.0D, 16.0D);
    private static final VoxelShape WEST_ARM = box(0.0D, 6.0D, 6.0D, 6.0D, 10.0D, 10.0D);
    private static final VoxelShape UP_ARM = box(6.0D, 10.0D, 6.0D, 10.0D, 16.0D, 10.0D);
    private static final VoxelShape DOWN_ARM = box(6.0D, 0.0D, 6.0D, 10.0D, 6.0D, 10.0D);

    public LegacyDetCordBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return updateConnections(context.getLevel(), context.getClickedPos(), defaultBlockState());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(propertyFor(direction), canConnect(level, neighborPos, neighborState, direction.getOpposite()));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        if (!level.isClientSide && level.hasNeighborSignal(pos)) {
            detonate(level, pos);
        }
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        level.removeBlock(pos, false);
        if (!level.isClientSide) {
            level.addFreshEntity(LegacyPrimedExplosiveEntity.create(level,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, this, 0, false));
        }
    }

    @Override
    public void explodeEntity(Level level, Vec3 position, @Nullable Entity source) {
        if (!level.isClientSide) {
            explodeAt(level, position.x, position.y, position.z);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    private void detonate(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() != this) {
            return;
        }
        level.removeBlock(pos, false);
        explodeAt(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    private void explodeAt(Level level, double x, double y, double z) {
        level.explode(null, x, y, z, 1.5F, false, Level.ExplosionInteraction.BLOCK);
    }

    private BlockState updateConnections(BlockGetter level, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            state = state.setValue(propertyFor(direction),
                    canConnect(level, neighborPos, level.getBlockState(neighborPos), direction.getOpposite()));
        }
        return state;
    }

    private static boolean canConnect(BlockGetter level, BlockPos pos, BlockState state, Direction direction) {
        return state.getBlock() instanceof DetConnectibleBlock connectible
                && connectible.canConnectToDetCord(level, pos, state, direction);
    }

    private static BooleanProperty propertyFor(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    private static VoxelShape shapeForState(BlockState state) {
        VoxelShape shape = CORE;
        if (state.getValue(NORTH)) {
            shape = Shapes.or(shape, NORTH_ARM);
        }
        if (state.getValue(EAST)) {
            shape = Shapes.or(shape, EAST_ARM);
        }
        if (state.getValue(SOUTH)) {
            shape = Shapes.or(shape, SOUTH_ARM);
        }
        if (state.getValue(WEST)) {
            shape = Shapes.or(shape, WEST_ARM);
        }
        if (state.getValue(UP)) {
            shape = Shapes.or(shape, UP_ARM);
        }
        if (state.getValue(DOWN)) {
            shape = Shapes.or(shape, DOWN_ARM);
        }
        return shape;
    }
}
