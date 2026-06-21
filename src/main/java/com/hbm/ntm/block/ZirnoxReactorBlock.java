package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.ZirnoxReactorBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class ZirnoxReactorBlock extends LegacyVisibleMultiblockMachineBlock {
    public ZirnoxReactorBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZirnoxReactorBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer
                && MultiblockHelper.resolveCoreBlockEntity(level, pos) instanceof ZirnoxReactorBlockEntity reactor) {
            NetworkHooks.openScreen(serverPlayer, reactor, reactor.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        if (level.isClientSide) {
            return;
        }
        if (MultiblockHelper.resolveCoreBlockEntity(level, pos) instanceof ZirnoxReactorBlockEntity reactor) {
            reactor.setRedstonePowered(scanRedstone(level, reactor.getBlockPos()));
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.ZIRNOX_REACTOR.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                ZirnoxReactorBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (ZirnoxReactorBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ZirnoxReactorBlockEntity reactor) {
            for (ItemStack stack : reactor.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }

    private static boolean scanRedstone(Level level, BlockPos core) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 0; dy <= 4; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx != -2 && dx != 2 && dy != 0 && dy != 4 && dz != -2 && dz != 2) {
                        continue;
                    }
                    if (level.hasNeighborSignal(core.offset(dx, dy, dz))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
