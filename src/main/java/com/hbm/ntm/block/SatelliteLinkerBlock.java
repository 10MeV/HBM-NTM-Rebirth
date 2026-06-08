package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.SatelliteLinkerBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

@SuppressWarnings("deprecation")
public class SatelliteLinkerBlock extends Block implements EntityBlock {
    public SatelliteLinkerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SatelliteLinkerBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof SatelliteLinkerBlockEntity linker) {
            NetworkHooks.openScreen(serverPlayer, linker, pos);
        }
        return player.isShiftKeyDown() ? InteractionResult.PASS : InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.MACHINE_SATLINKER.get()
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                SatelliteLinkerBlockEntity.serverTick(tickLevel, tickPos, tickState, (SatelliteLinkerBlockEntity) blockEntity)
                : null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof SatelliteLinkerBlockEntity linker) {
            for (ItemStack stack : linker.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
