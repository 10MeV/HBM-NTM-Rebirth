package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.PistonInserterBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** 1.7.10 PistonInserter: no menu, only front-face load/eject and rising-edge actuation. */
public class PistonInserterBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public PistonInserterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PistonInserterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.PISTON_INSERTER.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) -> PistonInserterBlockEntity.clientTick(
                        tickLevel, tickPos, tickState, (PistonInserterBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) -> PistonInserterBlockEntity.serverTick(
                        tickLevel, tickPos, tickState, (PistonInserterBlockEntity) blockEntity);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos neighborPos,
            boolean movedByPiston) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PistonInserterBlockEntity piston) {
            Direction facing = state.getValue(FACING);
            if (!level.getBlockState(pos.relative(facing)).isSolidRender(level, pos.relative(facing))) {
                piston.updateRedstoneState(level.hasNeighborSignal(pos));
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !oldState.is(state.getBlock())
                && level.getBlockEntity(pos) instanceof PistonInserterBlockEntity piston) {
            piston.updateRedstoneState(level.hasNeighborSignal(pos));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (hit.getDirection() != state.getValue(FACING)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof PistonInserterBlockEntity piston)) {
            return InteractionResult.PASS;
        }
        Direction facing = state.getValue(FACING);
        if (player.isShiftKeyDown()) {
            ItemStack ejected = piston.ejectSlot();
            if (!ejected.isEmpty()) {
                ItemEntity entity = new ItemEntity(level,
                        pos.getX() + 0.5D + facing.getStepX() * 0.75D,
                        pos.getY() + 0.5D + facing.getStepY() * 0.75D,
                        pos.getZ() + 0.5D + facing.getStepZ() * 0.75D,
                        ejected);
                entity.setDeltaMovement(facing.getStepX() * 0.25D, facing.getStepY() * 0.25D,
                        facing.getStepZ() * 0.25D);
                level.addFreshEntity(entity);
            }
            return InteractionResult.CONSUME;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty() && piston.loadOne(held)) {
            // PistonInserter#onBlockActivated always used decrStackSize on the
            // selected inventory slot.  It therefore consumed the loaded item
            // for creative players too; do not introduce a modern exemption.
            held.shrink(1);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof PistonInserterBlockEntity piston
                && !piston.getSlot().isEmpty()) {
            Block.popResource(level, pos, piston.getSlot());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public boolean isFaceSturdy(BlockState state, BlockGetter level, BlockPos pos, Direction side,
            SupportType supportType) {
        Direction facing = state.getValue(FACING);
        return side != facing && side != facing.getOpposite();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<net.minecraft.network.chat.Component> tooltip,
            TooltipFlag flag) {
        // Legacy PistonInserter implements ITooltipProvider and calls
        // addStandardInfo, including its Shift-gated four-line description.
        LegacyStandardInfoTooltip.append(tooltip, "piston_inserter");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
