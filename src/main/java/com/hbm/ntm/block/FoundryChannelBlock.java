package com.hbm.ntm.block;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.blockentity.FoundryChannelBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FoundryChannelBlock extends Block implements EntityBlock, ICrucibleAcceptor {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    private static final VoxelShape CENTER = box(5, 0, 5, 11, 8, 11);
    private static final VoxelShape POS_X = box(11, 0, 5, 16, 8, 11);
    private static final VoxelShape NEG_X = box(0, 0, 5, 5, 8, 11);
    private static final VoxelShape POS_Z = box(5, 0, 11, 11, 8, 16);
    private static final VoxelShape NEG_Z = box(5, 0, 0, 11, 8, 5);

    public FoundryChannelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundryChannelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.FOUNDRY_CHANNEL.get() && !level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                FoundryChannelBlockEntity.serverTick(tickLevel, tickPos, tickState, (FoundryChannelBlockEntity) blockEntity)
                : null;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connectionState(context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction.getAxis().isHorizontal() ? connectionState(level, pos) : state;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean movedByPiston) {
        BlockState next = connectionState(level, pos);
        if (!next.equals(state)) {
            level.setBlock(pos, next, Block.UPDATE_CLIENTS);
        }
        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FoundryChannelBlockEntity channel)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof ShovelItem)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            ItemStack scrap = channel.drainAsScrap();
            if (!scrap.isEmpty() && !player.addItem(scrap.copy())) {
                player.drop(scrap.copy(), false);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CENTER;
        if (state.getValue(EAST)) shape = Shapes.or(shape, POS_X);
        if (state.getValue(WEST)) shape = Shapes.or(shape, NEG_X);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, POS_Z);
        if (state.getValue(NORTH)) shape = Shapes.or(shape, NEG_Z);
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    public boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction) {
        if (direction.getAxis().isVertical()) {
            return false;
        }
        BlockPos target = pos.relative(direction);
        BlockState state = level.getBlockState(target);
        if ((state.is(ModBlocks.FOUNDRY_OUTLET.get()) || state.is(ModBlocks.FOUNDRY_SLAGTAP.get()))
                && state.hasProperty(FoundryOutletBlock.FACING)) {
            return state.getValue(FoundryOutletBlock.FACING) == direction;
        }
        return state.is(ModBlocks.FOUNDRY_CHANNEL.get()) || state.is(ModBlocks.FOUNDRY_MOLD.get());
    }

    private BlockState connectionState(BlockGetter level, BlockPos pos) {
        return defaultBlockState()
                .setValue(NORTH, canConnectTo(level, pos, Direction.NORTH))
                .setValue(EAST, canConnectTo(level, pos, Direction.EAST))
                .setValue(SOUTH, canConnectTo(level, pos, Direction.SOUTH))
                .setValue(WEST, canConnectTo(level, pos, Direction.WEST));
    }

    public void refreshConnectionState(Level level, BlockPos pos, BlockState state) {
        BlockState next = connectionState(level, pos);
        if (!next.equals(state)) {
            level.setBlock(pos, next, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof FoundryChannelBlockEntity channel) {
            ItemStack scrap = channel.drainAsScrap();
            if (!scrap.isEmpty()) {
                Block.popResource(level, pos, scrap);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        return level.getBlockEntity(pos) instanceof ICrucibleAcceptor acceptor
                && acceptor.canAcceptPartialPour(level, pos, hit, side, stack);
    }

    @Override
    public MaterialStack pour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        return level.getBlockEntity(pos) instanceof ICrucibleAcceptor acceptor
                ? acceptor.pour(level, pos, hit, side, stack)
                : stack;
    }

    @Override
    public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        return level.getBlockEntity(pos) instanceof ICrucibleAcceptor acceptor
                && acceptor.canAcceptPartialFlow(level, pos, side, stack);
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        return level.getBlockEntity(pos) instanceof ICrucibleAcceptor acceptor
                ? acceptor.flow(level, pos, side, stack)
                : stack;
    }
}
