package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LegacyLargeTurbineBlockEntity;
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
public class LegacyLargeTurbineBlock extends LegacyVisibleMultiblockMachineBlock {
    public LegacyLargeTurbineBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LegacyLargeTurbineBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && resolveCoreBlockEntity(level, pos) instanceof LegacyLargeTurbineBlockEntity turbine) {
            NetworkHooks.openScreen(serverPlayer, turbine, turbine.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.LEGACY_LARGE_TURBINE.get()) {
            return null;
        }
        if (level.isClientSide) {
            return (tickLevel, tickPos, tickState, blockEntity) ->
                    LegacyLargeTurbineBlockEntity.clientTick(tickLevel, tickPos, tickState,
                            (LegacyLargeTurbineBlockEntity) blockEntity);
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                LegacyLargeTurbineBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (LegacyLargeTurbineBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof LegacyLargeTurbineBlockEntity turbine) {
            for (ItemStack stack : turbine.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
