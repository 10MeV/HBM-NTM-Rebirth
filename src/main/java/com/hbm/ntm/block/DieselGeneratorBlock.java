package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.DieselGeneratorBlockEntity;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
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

public class DieselGeneratorBlock extends LegacyVisibleMachineBlock {
    public DieselGeneratorBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DieselGeneratorBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof DieselGeneratorBlockEntity diesel) {
            ItemStack held = player.getItemInHand(hand);
            if (player.isShiftKeyDown()) {
                var identifier = HbmFluidItemTransfer.setTankTypeFromIdentifierStackReport(
                        held, diesel.getTank(), level, diesel.getBlockPos());
                if (identifier.changed()) {
                    diesel.setChanged();
                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                    return InteractionResult.CONSUME;
                }
            }
            NetworkHooks.openScreen(serverPlayer, diesel, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.DIESEL_GENERATOR.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        DieselGeneratorBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (DieselGeneratorBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        DieselGeneratorBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (DieselGeneratorBlockEntity) blockEntity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof DieselGeneratorBlockEntity diesel) || !diesel.isBurning()) {
            return;
        }
        Direction facing = state.getValue(FACING);
        Direction side = facing.getClockWise();
        level.addParticle(ParticleTypes.SMOKE,
                pos.getX() + 0.5D - facing.getStepX() * 0.6D + side.getStepX() * 0.1875D,
                pos.getY() + 0.3125D,
                pos.getZ() + 0.5D - facing.getStepZ() * 0.6D + side.getStepZ() * 0.1875D,
                0.0D, 0.0D, 0.0D);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof DieselGeneratorBlockEntity diesel) {
            for (ItemStack stack : diesel.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
