package com.hbm.ntm.block;

import com.hbm.ntm.api.block.IBomb;
import com.hbm.ntm.blockentity.BlastDoorBlockEntity;
import com.hbm.ntm.item.PadlockItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** The original six-block vertical, timer-driven {@code BlastDoor}; not a DoorDecl carrier. */
@SuppressWarnings("deprecation")
public class BlastDoorBlock extends BaseEntityBlock implements IBomb {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    public BlastDoorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 1.7.10 only stored metadata 2/3: both yaw pairs are the same two axes.
        Direction playerFacing = context.getHorizontalDirection();
        return defaultBlockState().setValue(FACING,
                playerFacing.getAxis() == Direction.Axis.X ? Direction.EAST : Direction.NORTH);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BlastDoorBlockEntity door) {
            door.createInitialFrameOrRemoveCore();
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        // BlockDoor always returned true on the old client, including while crouching or holding a lock tool.
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof PadlockItem) {
            return level.getBlockEntity(pos) instanceof BlastDoorBlockEntity door
                    && door.tryApplyPadlock(player, held) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (held.is(ModItems.KEY_KIT.get())) {
            return InteractionResult.PASS;
        }
        if (!player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof BlastDoorBlockEntity door) {
            door.tryToggle(player);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level == null || level.isClientSide || !(level.getBlockEntity(pos) instanceof BlastDoorBlockEntity door)) {
            return BombReturnCode.UNDEFINED;
        }
        if (door.isLocked()) {
            return BombReturnCode.ERROR_INCOMPATIBLE;
        }
        door.tryToggle(null);
        return BombReturnCode.TRIGGERED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlastDoorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.BLAST_DOOR.get()
                ? (tickLevel, tickPos, tickState, blockEntity) -> BlastDoorBlockEntity.tick(tickLevel, tickPos,
                        tickState, (BlastDoorBlockEntity) blockEntity)
                : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof BlastDoorBlockEntity door) {
            door.removeAllDummies();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
