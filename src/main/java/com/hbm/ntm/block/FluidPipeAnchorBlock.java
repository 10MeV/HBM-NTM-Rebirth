package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.FluidPipeAnchorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FluidPipeAnchorBlock extends FluidPipeBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public FluidPipeAnchorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return (state == null ? defaultBlockState() : state).setValue(FACING, context.getClickedFace());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidPipeAnchorBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && !level.isClientSide
                && level.getBlockEntity(pos) instanceof FluidPipeAnchorBlockEntity anchor) {
            anchor.disconnectAllRemotePartners();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static Direction attachedSide(BlockState state) {
        return state.hasProperty(FACING) ? state.getValue(FACING).getOpposite() : Direction.DOWN;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForFacing(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForFacing(state.getValue(FACING));
    }

    private static VoxelShape shapeForFacing(Direction facing) {
        Direction side = facing.getOpposite();
        double min = 4.0D;
        double max = 12.0D;
        double minX = side == Direction.WEST ? 0.0D : min;
        double maxX = side == Direction.EAST ? 16.0D : max;
        double minY = side == Direction.DOWN ? 0.0D : min;
        double maxY = side == Direction.UP ? 16.0D : max;
        double minZ = side == Direction.NORTH ? 0.0D : min;
        double maxZ = side == Direction.SOUTH ? 16.0D : max;
        return box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }
}
