package com.hbm.ntm.block;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.GasFlareBlockEntity;
import com.hbm.ntm.fluid.FluidType;
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
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class GasFlareBlock extends LegacyVisibleMultiblockMachineBlock {
    public GasFlareBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GasFlareBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer
                && level.getBlockEntity(pos) instanceof GasFlareBlockEntity gasFlare) {
            ItemStack held = player.getItemInHand(hand);
            if (player.isShiftKeyDown() && held.getItem() instanceof IFluidIdentifierItem identifier) {
                FluidType type = identifier.getIdentifiedFluid(level, pos, held);
                if (type != null && type != gasFlare.getTank().getTankType()) {
                    gasFlare.getTank().setTankType(type);
                    gasFlare.setChanged();
                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.GAS_FLARE.get()) {
            return null;
        }
        return level.isClientSide
                ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                GasFlareBlockEntity.serverTick(tickLevel, tickPos, tickState, (GasFlareBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof GasFlareBlockEntity gasFlare) {
            for (ItemStack stack : gasFlare.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
