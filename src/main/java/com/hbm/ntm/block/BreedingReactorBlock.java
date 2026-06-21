package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.BreedingReactorBlockEntity;
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
public class BreedingReactorBlock extends LegacyVisibleMultiblockMachineBlock {
    public BreedingReactorBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BreedingReactorBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer
                && MultiblockHelper.resolveCoreBlockEntity(level, pos) instanceof BreedingReactorBlockEntity breeder) {
            NetworkHooks.openScreen(serverPlayer, breeder, breeder.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.BREEDING_REACTOR.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                BreedingReactorBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (BreedingReactorBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof BreedingReactorBlockEntity breeder) {
            for (ItemStack stack : breeder.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
