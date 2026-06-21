package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.BombMultiBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class BombMultiBlock extends HorizontalMachineBlock implements EntityBlock, RemoteDetonatableBlock {
    private static final VoxelShape SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);

    public BombMultiBlock(Properties properties) {
        super(properties, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BombMultiBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof BombMultiBlockEntity blockEntity) {
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
                && level.getBlockEntity(pos) instanceof BombMultiBlockEntity blockEntity) {
            blockEntity.spillDrops(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BombMultiBlockEntity blockEntity ? blockEntity : null;
    }

    public BombReturnCode detonate(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()
                || !(level.getBlockEntity(pos) instanceof BombMultiBlockEntity blockEntity)) {
            return BombReturnCode.UNDEFINED;
        }
        if (!blockEntity.isLoaded()) {
            return BombReturnCode.ERROR_MISSING_COMPONENT;
        }

        BombMultiBlockEntity.BombMultiStats stats = blockEntity.getStats();
        blockEntity.clearSlots();
        level.removeBlock(pos, false);
        stats.explode(level, pos);
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
        if (!(level.getBlockEntity(pos) instanceof BombMultiBlockEntity blockEntity) || !blockEntity.isLoaded()) {
            return BombReturnCode.ERROR_MISSING_COMPONENT;
        }
        BombReturnCode result = detonate(level, pos);
        return result.wasSuccessful() ? result : BombReturnCode.ERROR_INCOMPATIBLE;
    }
}
