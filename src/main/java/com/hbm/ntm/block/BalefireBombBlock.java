package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.BalefireBombBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class BalefireBombBlock extends HorizontalMachineBlock implements EntityBlock, RemoteDetonatableBlock {
    public BalefireBombBlock(Properties properties) {
        super(properties, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BalefireBombBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof BalefireBombBlockEntity balefireBomb) {
                BalefireBombBlockEntity.serverTick(tickerLevel, tickerPos, tickerState, balefireBomb);
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof BalefireBombBlockEntity blockEntity) {
            NetworkHooks.openScreen(serverPlayer, blockEntity, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
            BlockPos fromPos, boolean moving) {
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.hasNeighborSignal(pos)) {
            detonate(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof BalefireBombBlockEntity blockEntity) {
            blockEntity.spillDrops(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BalefireBombBlockEntity blockEntity ? blockEntity : null;
    }

    public BombReturnCode explode(Level level, BlockPos pos) {
        return detonate(level, pos);
    }

    public BombReturnCode detonate(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return BombReturnCode.UNDEFINED;
        }
        if (level.getBlockState(pos).getBlock() != this) {
            return BombReturnCode.ERROR_INCOMPATIBLE;
        }
        if (!(level.getBlockEntity(pos) instanceof BalefireBombBlockEntity blockEntity)) {
            return BombReturnCode.UNDEFINED;
        }
        if (!blockEntity.isLoaded()) {
            return BombReturnCode.ERROR_MISSING_COMPONENT;
        }

        blockEntity.explode();
        return BombReturnCode.DETONATED;
    }

    @Override
    public BombReturnCode detonateFromRemote(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return BombReturnCode.UNDEFINED;
        }
        if (level.getBlockState(pos).getBlock() != this) {
            return BombReturnCode.ERROR_NO_BOMB;
        }
        if (!(level.getBlockEntity(pos) instanceof BalefireBombBlockEntity blockEntity) || !blockEntity.isLoaded()) {
            return BombReturnCode.ERROR_MISSING_COMPONENT;
        }
        BombReturnCode result = detonate(level, pos);
        return result.wasSuccessful() ? result : BombReturnCode.ERROR_INCOMPATIBLE;
    }
}
