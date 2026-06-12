package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.CustomNukeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class CustomNukeBlock extends HorizontalMachineBlock implements EntityBlock, RemoteDetonatableBlock {
    public CustomNukeBlock(Properties properties) {
        super(properties, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CustomNukeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof CustomNukeBlockEntity customNuke) {
                CustomNukeBlockEntity.serverTick(tickerLevel, tickerPos, tickerState, customNuke);
            }
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof CustomNukeBlockEntity blockEntity) {
            NetworkHooks.openScreen(serverPlayer, blockEntity, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        if (!level.isClientSide() && level.hasNeighborSignal(pos)) {
            detonate(level, pos);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (!oldState.is(state.getBlock()) && !level.isClientSide() && level.hasNeighborSignal(pos)) {
            detonate(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof CustomNukeBlockEntity blockEntity) {
            for (ItemStack stack : blockEntity.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CustomNukeBlockEntity blockEntity ? blockEntity : null;
    }

    public boolean detonate(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()
                || !(level.getBlockEntity(pos) instanceof CustomNukeBlockEntity blockEntity)) {
            return false;
        }

        CustomNukeBlockEntity.CustomNukeStats stats = blockEntity.getStats();
        if (stats.isEmpty()) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        blockEntity.clearSlots();
        level.removeBlock(pos, false);
        if (stats.falling()) {
            stats.spawnFalling(level, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, legacyFacingMeta(state));
        } else {
            level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                    1.0F, 0.9F + level.random.nextFloat() * 0.1F);
            level.gameEvent(null, GameEvent.EXPLODE, pos);
            stats.explode(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        }
        return true;
    }

    private static byte legacyFacingMeta(BlockState state) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
        return switch (facing) {
            case EAST -> 3;
            case SOUTH -> 4;
            case WEST -> 2;
            default -> 5;
        };
    }

    @Override
    public BombReturnCode detonateFromRemote(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return BombReturnCode.UNDEFINED;
        }
        if (level.getBlockState(pos).getBlock() != this) {
            return BombReturnCode.ERROR_NO_BOMB;
        }
        if (!(level.getBlockEntity(pos) instanceof CustomNukeBlockEntity blockEntity)
                || blockEntity.getStats().isEmpty()) {
            return BombReturnCode.ERROR_MISSING_COMPONENT;
        }
        return detonate(level, pos) ? BombReturnCode.DETONATED : BombReturnCode.ERROR_INCOMPATIBLE;
    }
}
