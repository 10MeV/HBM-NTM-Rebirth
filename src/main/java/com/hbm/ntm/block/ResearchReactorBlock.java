package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.ResearchReactorBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
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

@SuppressWarnings("deprecation")
public class ResearchReactorBlock extends LegacyVisibleMultiblockMachineBlock {
    public ResearchReactorBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResearchReactorBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(level, pos);
        if (!(blockEntity instanceof ResearchReactorBlockEntity reactor)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, reactor, reactor.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.RESEARCH_REACTOR.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                ResearchReactorBlockEntity.tick(tickLevel, tickPos, tickState, (ResearchReactorBlockEntity) blockEntity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos side = pos.relative(direction);
            if (!level.getFluidState(side).is(net.minecraft.tags.FluidTags.WATER)) {
                continue;
            }
            double x = pos.getX() + 0.5D + direction.getStepX() + random.nextDouble() - 0.5D;
            double y = pos.getY() + 0.5D + random.nextDouble() - 0.5D;
            double z = pos.getZ() + 0.5D + direction.getStepZ() + random.nextDouble() - 0.5D;
            if (direction.getStepX() != 0) {
                x = pos.getX() + 0.5D + direction.getStepX() * 0.5D
                        + random.nextDouble() * 0.125D * direction.getStepX();
            }
            if (direction.getStepZ() != 0) {
                z = pos.getZ() + 0.5D + direction.getStepZ() * 0.5D
                        + random.nextDouble() * 0.125D * direction.getStepZ();
            }
            level.addParticle(ParticleTypes.BUBBLE, x, y, z, 0.0D, 0.2D, 0.0D);
        }
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ResearchReactorBlockEntity reactor) {
            for (ItemStack stack : reactor.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
