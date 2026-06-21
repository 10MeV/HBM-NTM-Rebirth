package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.DiFurnaceBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
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
public class DiFurnaceBlock extends HorizontalMachineBlock implements EntityBlock {
    public DiFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DiFurnaceBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof DiFurnaceBlockEntity furnace) {
            NetworkHooks.openScreen(serverPlayer, furnace, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.DIFURNACE.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                DiFurnaceBlockEntity.clientTick(tickLevel, tickPos, tickState,
                        (DiFurnaceBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                DiFurnaceBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (DiFurnaceBlockEntity) blockEntity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof DiFurnaceBlockEntity furnace) || !furnace.isProcessing()) {
            return;
        }
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.SOUTH;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.25D + random.nextDouble() * 0.375D;
        double z = pos.getZ() + 0.5D;
        double sideOff = 0.52D;
        double sideRand = random.nextDouble() * 0.5D - 0.25D;
        double smokeX = pos.getX() + random.nextDouble() * 0.375D + 0.3125D;
        double smokeZ = pos.getZ() + random.nextDouble() * 0.375D + 0.3125D;
        double smokeY = pos.getY() + (furnace.hasExtension() ? 2.0D : 1.0D);

        switch (facing) {
            case WEST -> level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                    x - sideOff, y, z + sideRand, 0.0D, 0.0D, 0.0D);
            case EAST -> level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                    x + sideOff, y, z + sideRand, 0.0D, 0.0D, 0.0D);
            case NORTH -> level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                    x + sideRand, y, z - sideOff, 0.0D, 0.0D, 0.0D);
            default -> level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                    x + sideRand, y, z + sideOff, 0.0D, 0.0D, 0.0D);
        }
        level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                smokeX, smokeY, smokeZ, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof DiFurnaceBlockEntity furnace) {
                for (ItemStack stack : furnace.getDrops()) {
                    Block.popResource(level, pos, stack);
                }
            }
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
