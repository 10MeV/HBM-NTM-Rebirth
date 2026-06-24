package com.hbm.ntm.block;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.blockentity.FoundryTankBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FoundryTankBlock extends Block implements EntityBlock, ICrucibleAcceptor {
    public FoundryTankBlock(Properties properties) {
        super(properties);
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
