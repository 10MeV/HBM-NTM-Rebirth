package com.hbm.ntm.block;

import com.hbm.ntm.api.multiblock.LegacyMultiblock;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyMultiblockPlaceable;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public abstract class LegacyOffsetMultiblockBlock extends HorizontalMachineBlock implements MultiblockCoreBlock, LegacyMultiblockPlaceable, LegacyMultiblock {
    protected LegacyOffsetMultiblockBlock(Properties properties) {
        super(properties, false);
    }

    protected abstract LegacyMultiblockLayout getLayout(BlockState state);

    protected Direction getFacingForPlacement(BlockPlaceContext context) {
        return context.getHorizontalDirection().getOpposite();
    }

    protected BlockPos getCoreFromPlacement(BlockPlaceContext context, BlockState state) {
        return context.getClickedPos();
    }

    @Override
    public LegacyMultiblockLayout getMultiblockLayout(BlockState state, BlockGetter level, BlockPos corePos) {
        return getLayout(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getMultiblockCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = getDirectPlacementState(context);
        if (state == null) {
            return null;
        }
        BlockPos corePos = getDirectPlacementCore(context, state);
        return canPlaceDirectMultiblock(context.getLevel(), corePos, context.getClickedPos(), state) ? state : null;
    }

    @Nullable
    @Override
    public BlockState getDirectPlacementState(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, getFacingForPlacement(context));
    }

    @Override
    public BlockPos getDirectPlacementCore(BlockPlaceContext context, BlockState state) {
        return getCoreFromPlacement(context, state);
    }

    @Override
    public boolean canPlaceDirectMultiblock(Level level, BlockPos corePos, BlockPos temporaryPos) {
        return canPlaceDirectMultiblock(level, corePos, temporaryPos, defaultBlockState());
    }

    @Override
    public boolean canPlaceDirectMultiblock(Level level, BlockPos corePos, BlockPos temporaryPos, BlockState state) {
        return MultiblockHelper.checkLayout(level, corePos, getLayout(state), temporaryPos);
    }

    @Override
    public void afterDirectCorePlaced(Level level, BlockPos corePos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, corePos, state, placer, stack);
    }

    @Override
    public void completeDirectMultiblockPlacement(Level level, BlockPos corePos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            fillLayout(level, corePos, state);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            fillLayout(level, pos, state);
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            MultiblockHelper.removeLayout(level, pos, getLayout(state));
            onCoreRemoved(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                manager.destroy(pos, MultiblockHelper.steelParticleState());
                return true;
            }
        });
    }

    private void fillLayout(Level level, BlockPos corePos, BlockState state) {
        MultiblockHelper.fillLayout(level, corePos, getLayout(state));
    }
}
