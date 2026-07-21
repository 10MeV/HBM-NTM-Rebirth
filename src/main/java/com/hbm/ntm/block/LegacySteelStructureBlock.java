package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable.ToolType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Exact shape and screwdriver-rotation carrier for the legacy DecoBlock steel structures. */
public class LegacySteelStructureBlock extends Block {
    public enum Kind { WALL, CORNER, ROOF }

    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    private static final VoxelShape ROOF = box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
    private static final VoxelShape WALL_NORTH = box(0.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape WALL_SOUTH = box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);
    private static final VoxelShape WALL_EAST = box(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape WALL_WEST = box(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 16.0D);
    private final Kind kind;

    public LegacySteelStructureBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if ((kind == Kind.WALL || kind == Kind.CORNER)
                && ToolType.getType(player.getItemInHand(hand)) == ToolType.SCREWDRIVER) {
            if (!level.isClientSide) {
                Direction facing = state.getValue(FACING);
                level.setBlock(pos, state.setValue(FACING,
                        player.isShiftKeyDown() ? facing.getCounterClockWise() : facing.getClockWise()), Block.UPDATE_ALL);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state.getValue(FACING));
    }

    private VoxelShape shape(Direction facing) {
        if (kind == Kind.ROOF) return ROOF;
        if (kind == Kind.WALL) return switch (facing) {
            case NORTH -> WALL_NORTH;
            case SOUTH -> WALL_SOUTH;
            case EAST -> WALL_EAST;
            case WEST -> WALL_WEST;
            default -> WALL_NORTH;
        };
        return switch (facing) {
            case NORTH -> Shapes.or(box(4, 0, 14, 16, 16, 16), box(0, 0, 12, 4, 16, 16), box(0, 0, 0, 2, 16, 12));
            case SOUTH -> Shapes.or(box(0, 0, 0, 12, 16, 2), box(12, 0, 0, 16, 16, 4), box(14, 0, 4, 16, 16, 16));
            case EAST -> Shapes.or(box(14, 0, 0, 16, 16, 12), box(12, 0, 12, 16, 16, 16), box(0, 0, 14, 12, 16, 16));
            case WEST -> Shapes.or(box(0, 0, 4, 2, 16, 16), box(0, 0, 0, 4, 16, 4), box(4, 0, 0, 16, 16, 2));
            default -> Shapes.empty();
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
