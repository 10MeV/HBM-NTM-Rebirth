package com.hbm.ntm.block;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.blockentity.FoundryTankBlockEntity;
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
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FoundryTankBlock extends Block implements EntityBlock, ICrucibleAcceptor {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty OUTLET_NORTH = BooleanProperty.create("outlet_north");
    public static final BooleanProperty OUTLET_EAST = BooleanProperty.create("outlet_east");
    public static final BooleanProperty OUTLET_SOUTH = BooleanProperty.create("outlet_south");
    public static final BooleanProperty OUTLET_WEST = BooleanProperty.create("outlet_west");

    public FoundryTankBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(OUTLET_NORTH, false)
                .setValue(OUTLET_EAST, false)
                .setValue(OUTLET_SOUTH, false)
                .setValue(OUTLET_WEST, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundryTankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.FOUNDRY_TANK.get() && !level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                FoundryTankBlockEntity.serverTick(tickLevel, tickPos, tickState, (FoundryTankBlockEntity) blockEntity)
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
        return connectionState(level, pos);
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
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN,
                OUTLET_NORTH, OUTLET_EAST, OUTLET_SOUTH, OUTLET_WEST);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FoundryTankBlockEntity tank)
                || !(player.getItemInHand(hand).getItem() instanceof ShovelItem)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            ItemStack scrap = tank.drainAsScrap();
            if (!scrap.isEmpty() && !player.addItem(scrap.copy())) {
                player.drop(scrap.copy(), false);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public boolean isFaceSturdy(BlockState state, BlockGetter level, BlockPos pos, Direction direction,
            SupportType supportType) {
        return direction != Direction.UP;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    private BlockState connectionState(BlockGetter level, BlockPos pos) {
        return defaultBlockState()
                .setValue(NORTH, isTank(level, pos.relative(Direction.NORTH)))
                .setValue(EAST, isTank(level, pos.relative(Direction.EAST)))
                .setValue(SOUTH, isTank(level, pos.relative(Direction.SOUTH)))
                .setValue(WEST, isTank(level, pos.relative(Direction.WEST)))
                .setValue(UP, isTank(level, pos.above()))
                .setValue(DOWN, isTank(level, pos.below()))
                .setValue(OUTLET_NORTH, isOutletFacing(level, pos.relative(Direction.NORTH), Direction.NORTH))
                .setValue(OUTLET_EAST, isOutletFacing(level, pos.relative(Direction.EAST), Direction.EAST))
                .setValue(OUTLET_SOUTH, isOutletFacing(level, pos.relative(Direction.SOUTH), Direction.SOUTH))
                .setValue(OUTLET_WEST, isOutletFacing(level, pos.relative(Direction.WEST), Direction.WEST));
    }

    public void refreshConnectionState(Level level, BlockPos pos, BlockState state) {
        BlockState next = connectionState(level, pos);
        if (!next.equals(state)) {
            level.setBlock(pos, next, Block.UPDATE_CLIENTS);
        }
    }

    private static boolean isTank(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.FOUNDRY_TANK.get());
    }

    private static boolean isOutletFacing(BlockGetter level, BlockPos pos, Direction direction) {
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlocks.FOUNDRY_OUTLET.get())
                && state.hasProperty(FoundryOutletBlock.FACING)
                && state.getValue(FoundryOutletBlock.FACING) == direction;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof FoundryTankBlockEntity tank) {
            ItemStack scrap = tank.drainAsScrap();
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
