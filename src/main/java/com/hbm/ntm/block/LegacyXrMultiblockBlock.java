package com.hbm.ntm.block;

import com.hbm.ntm.api.multiblock.LegacyMultiblock;
import com.hbm.ntm.multiblock.LegacyMultiblockPlaceable;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.hbm.ntm.multiblock.MultiblockExtents;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public abstract class LegacyXrMultiblockBlock extends HorizontalMachineBlock implements MultiblockCoreBlock, LegacyMultiblockPlaceable, LegacyMultiblock {
    private static final ThreadLocal<Boolean> RELOCATING = ThreadLocal.withInitial(() -> false);
    private final Map<BlockState, LegacyMultiblockLayout> layoutCache = new ConcurrentHashMap<>();

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

    /**
     * Legacy {@code BlockDummyable} subclasses normally used the shared dummy
     * carrier. Blocks with source-backed per-dummy state (for example narrow
     * rail segments) can override this without changing other multiblocks.
     */
    protected BlockState getLegacyDummyState(BlockState coreState, BlockPos offset) {
        return com.hbm.ntm.registry.ModBlocks.DUMMY_BLOCK.get().defaultBlockState();
    }

    protected MultiblockExtents getExtents(BlockState state) {
        return MultiblockExtents.ofLegacyXr(getLegacyXrDimensions(), state.getValue(FACING));
    }

    protected LegacyMultiblockLayout getLayout(BlockState state) {
        return LegacyMultiblockLayout.ofLegacyXrChecked(getLegacyXrDimensions(), state.getValue(FACING),
                proxyOffsets(state));
    }

    private LegacyMultiblockLayout cachedLayout(BlockState state) {
        return layoutCache.computeIfAbsent(state, this::getLayout);
    }

    protected Direction getFacingForPlacement(BlockPlaceContext context) {
        return modifyPlacementFacing(context.getHorizontalDirection().getOpposite());
    }

    @Override
    public LegacyMultiblockLayout getMultiblockLayout(BlockState state, BlockGetter level, BlockPos corePos) {
        return cachedLayout(state);
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
        return MultiblockHelper.legacyCoreFromPlacement(context.getClickedPos().above(getLegacyHeightOffset()),
                state.getValue(FACING), getLegacyOffset());
    }

    @Override
    public boolean canPlaceDirectMultiblock(Level level, BlockPos corePos, BlockPos temporaryPos) {
        return canPlaceDirectMultiblock(level, corePos, temporaryPos, defaultBlockState());
    }

    @Override
    public boolean canPlaceDirectMultiblock(Level level, BlockPos corePos, BlockPos temporaryPos, BlockState state) {
        return MultiblockHelper.checkLayout(level, corePos, cachedLayout(state), temporaryPos);
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
        BlockPos corePos = pos;
        if (!level.isClientSide) {
            corePos = MultiblockHelper.legacyCoreFromPlacement(pos, state.getValue(FACING), getLegacyOffset());
            if (getLegacyHeightOffset() != 0) {
                corePos = corePos.above(getLegacyHeightOffset());
            }
            if (!corePos.equals(pos)) {
                RELOCATING.set(true);
                level.removeBlock(pos, false);
                RELOCATING.set(false);
                level.setBlock(corePos, state, Block.UPDATE_ALL);
            }
            if (!fillLayout(level, corePos, state) && requiresCompleteLegacyLayout(state)) {
                return;
            }
        }
        super.setPlacedBy(level, corePos, state, placer, stack);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!RELOCATING.get() && !state.is(newState.getBlock()) && !level.isClientSide) {
            MultiblockHelper.removeLayout(level, pos, cachedLayout(state));
            onCoreRemoved(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
    }

    protected boolean requiresCompleteLegacyLayout(BlockState state) {
        return false;
    }

    protected void onIncompleteLegacyLayout(Level level, BlockPos corePos, BlockState state) {
        level.destroyBlock(corePos, false);
    }

    @Nullable
    protected static BlockEntity resolveCoreBlockEntity(Level level, BlockPos pos) {
        return MultiblockHelper.resolveCoreBlockEntity(level, pos);
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

    /**
     * Fills the legacy footprint after the core has moved into its source-backed
     * position. A very small number of old {@code BlockDummyable} subclasses
     * deliberately wrote additional blocks that were not part of their
     * placement preflight; they override {@link #usesUncheckedLegacyDummyFill}
     * to retain that behaviour.
     */
    protected boolean fillLayout(Level level, BlockPos corePos, BlockState state) {
        LegacyMultiblockLayout layout = cachedLayout(state);
        boolean filled = usesUncheckedLegacyDummyFill(state)
                ? MultiblockHelper.fillOffsetsWithLegacySourceOverwrite(level, corePos, layout.offsets(),
                        offset -> getLegacyDummyState(state, offset), layout::proxyMode, layout::isLegacyExtraOffset)
                : MultiblockHelper.fillOffsetsWithProxyModes(level, corePos, layout.offsets(),
                        offset -> getLegacyDummyState(state, offset), layout::proxyMode, layout::isLegacyExtraOffset);
        boolean complete = filled && MultiblockHelper.isLayoutComplete(level, corePos, layout);
        if (!complete && requiresCompleteLegacyLayout(state)) {
            onIncompleteLegacyLayout(level, corePos, state);
        }
        return complete;
    }

    /** Source-backed opt-in for legacy fill routines that wrote unchecked dummies. */
    protected boolean usesUncheckedLegacyDummyFill(BlockState state) {
        return false;
    }
}
