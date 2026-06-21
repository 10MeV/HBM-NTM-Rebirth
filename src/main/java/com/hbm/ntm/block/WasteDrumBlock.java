package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.WasteDrumBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.core.particles.ParticleTypes;
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
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class WasteDrumBlock extends Block implements EntityBlock {
    public WasteDrumBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WasteDrumBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof WasteDrumBlockEntity drum) {
            NetworkHooks.openScreen(serverPlayer, drum, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.WASTE_DRUM.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) -> WasteDrumBlockEntity.serverTick(
                tickLevel, tickPos, tickState, (WasteDrumBlockEntity) blockEntity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN || !level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
                continue;
            }
            double x = pos.getX() + 0.5D + direction.getStepX() + random.nextDouble() - 0.5D;
            double y = pos.getY() + 0.5D + direction.getStepY() + random.nextDouble() - 0.5D;
            double z = pos.getZ() + 0.5D + direction.getStepZ() + random.nextDouble() - 0.5D;
            if (direction.getStepX() != 0) {
                x = pos.getX() + 0.5D + direction.getStepX() * 0.5D
                        + random.nextDouble() * 0.125D * direction.getStepX();
            }
            if (direction.getStepY() != 0) {
                y = pos.getY() + 0.5D + direction.getStepY() * 0.5D
                        + random.nextDouble() * 0.125D * direction.getStepY();
            }
            if (direction.getStepZ() != 0) {
                z = pos.getZ() + 0.5D + direction.getStepZ() * 0.5D
                        + random.nextDouble() * 0.125D * direction.getStepZ();
            }
            level.addParticle(ParticleTypes.BUBBLE, x, y, z, 0.0D, 0.2D, 0.0D);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof WasteDrumBlockEntity drum) {
                for (ItemStack stack : drum.getDrops()) {
                    Block.popResource(level, pos, stack);
                }
            }
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
