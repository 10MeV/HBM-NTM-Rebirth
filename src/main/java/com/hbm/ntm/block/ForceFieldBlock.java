package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.ForceFieldBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class ForceFieldBlock extends BaseEntityBlock {
    public ForceFieldBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ForceFieldBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof ForceFieldBlockEntity forceField) {
            NetworkHooks.openScreen(serverPlayer, forceField, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.FORCE_FIELD.get()) {
            return null;
        }
        return level.isClientSide
                ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        ForceFieldBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (ForceFieldBlockEntity) blockEntity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof ForceFieldBlockEntity forceField)) {
            return;
        }
        if (forceField.isOn() && forceField.getCooldown() == 0 && forceField.getPower() > 0L) {
            for (int i = 0; i < 4; i++) {
                double x = pos.getX() + random.nextFloat();
                double y = pos.getY() + 2.0D;
                double z = pos.getZ() + random.nextFloat();
                if (forceField.getColor() == 0xFF0000) {
                    level.addParticle(ParticleTypes.LAVA, x, y, z, 0.0D, 0.0D, 0.0D);
                } else {
                    level.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0.0D, 0.0D, 0.0D);
                }
            }
        } else if (forceField.getCooldown() > 0) {
            for (int i = 0; i < 4; i++) {
                level.addParticle(ParticleTypes.SMOKE, pos.getX() + random.nextFloat(), pos.getY() + 2.0D,
                        pos.getZ() + random.nextFloat(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof ForceFieldBlockEntity forceField) {
                for (ItemStack stack : forceField.getDrops()) {
                    Block.popResource(level, pos, stack);
                }
            }
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
