package com.hbm.ntm.block;

import com.hbm.ntm.multiblock.LegacyMultiblockPlaceable;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.hbm.ntm.multiblock.MultiblockExtents;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

@SuppressWarnings("deprecation")
public abstract class LegacyXrMultiblockBlock extends HorizontalMachineBlock implements MultiblockCoreBlock, LegacyMultiblockPlaceable {
    private static final ThreadLocal<Boolean> RELOCATING = ThreadLocal.withInitial(() -> false);

    protected LegacyXrMultiblockBlock(Properties properties) {
        super(properties, false);
    }

    protected abstract int[] getLegacyXrDimensions();

    protected abstract int getLegacyOffset();

    protected int getLegacyHeightOffset() {
        return 0;
    }

    protected Direction modifyPlacementFacing(Direction facing) {
        return facing;
    }

    protected Predicate<BlockPos> proxyOffsets(BlockState state) {
        return offset -> false;
    }

    protected MultiblockExtents getExtents(BlockState state) {
        return MultiblockExtents.ofLegacyXr(getLegacyXrDimensions(), state.getValue(FACING));
    }

    protected LegacyMultiblockLayout getLayout(BlockState state) {
        return LegacyMultiblockLayout.ofExtents(getExtents(state)).withProxyPredicate(proxyOffsets(state));
    }

    protected Direction getFacingForPlacement(BlockPlaceContext context) {
        return modifyPlacementFacing(context.getHorizontalDirection().getOpposite());
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
        return MultiblockHelper.legacyCoreFromPlacement(context.getClickedPos().above(getLegacyHeightOffset()),
                state.getValue(FACING), getLegacyOffset());
    }

    @Override
    public boolean canPlaceDirectMultiblock(Level level, BlockPos corePos, BlockPos temporaryPos) {
        return canPlaceDirectMultiblock(level, corePos, temporaryPos, defaultBlockState());
    }

    @Override
    public boolean canPlaceDirectMultiblock(Level level, BlockPos corePos, BlockPos temporaryPos, BlockState state) {
        return MultiblockHelper.checkSpace(level, corePos, getLayout(state).checkOffsets(), temporaryPos);
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
            BlockPos corePos = MultiblockHelper.legacyCoreFromPlacement(pos, state.getValue(FACING), getLegacyOffset());
            if (getLegacyHeightOffset() != 0) {
                corePos = corePos.above(getLegacyHeightOffset());
            }
            if (!corePos.equals(pos)) {
                RELOCATING.set(true);
                level.removeBlock(pos, false);
                RELOCATING.set(false);
                level.setBlock(corePos, state, Block.UPDATE_ALL);
            }
            fillLayout(level, corePos, state);
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!RELOCATING.get() && !state.is(newState.getBlock()) && !level.isClientSide) {
            MultiblockHelper.removeOffsets(level, pos, getLayout(state).offsets());
            onCoreRemoved(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
    }

    private void fillLayout(Level level, BlockPos corePos, BlockState state) {
        LegacyMultiblockLayout layout = getLayout(state);
        MultiblockHelper.fillOffsetsWithProxyModes(level, corePos, layout.offsets(), layout::proxyMode);
    }
}
