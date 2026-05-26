package com.hbm.ntm.multiblock;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.MultiblockDummyBlockEntity;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public final class MultiblockHelper {
    private static final ThreadLocal<Set<ClearingKey>> CLEARING = ThreadLocal.withInitial(HashSet::new);

    public static final MultiblockExtents IGEN_DIMENSION_NORTH = MultiblockExtents.ofLegacy(new int[] { 1, 1, 2, 0, 3, 2 });
    public static final MultiblockExtents IGEN_DIMENSION_EAST = MultiblockExtents.ofLegacy(new int[] { 2, 3, 2, 0, 1, 1 });
    public static final MultiblockExtents IGEN_DIMENSION_SOUTH = MultiblockExtents.ofLegacy(new int[] { 1, 1, 2, 0, 2, 3 });
    public static final MultiblockExtents IGEN_DIMENSION_WEST = MultiblockExtents.ofLegacy(new int[] { 3, 2, 2, 0, 1, 1 });
    public static final MultiblockExtents CENT_DIMENSION = MultiblockExtents.ofLegacy(new int[] { 0, 0, 2, 0, 0, 0 });
    public static final MultiblockExtents CYCL_DIMENSION = MultiblockExtents.ofLegacy(new int[] { 1, 1, 5, 0, 1, 1 });
    public static final MultiblockExtents WELL_DIMENSION = MultiblockExtents.ofLegacy(new int[] { 1, 1, 5, 0, 1, 1 });
    public static final MultiblockExtents FLARE_DIMENSION = MultiblockExtents.ofLegacy(new int[] { 1, 1, 9, 0, 1, 1 });
    public static final MultiblockExtents DRILL_DIMENSION = MultiblockExtents.ofLegacy(new int[] { 1, 1, 3, 0, 1, 1 });
    public static final MultiblockExtents ASSEMBLER_DIMENSION_NORTH = MultiblockExtents.ofLegacy(new int[] { 2, 1, 1, 0, 1, 2 });
    public static final MultiblockExtents ASSEMBLER_DIMENSION_EAST = MultiblockExtents.ofLegacy(new int[] { 2, 1, 1, 0, 2, 1 });
    public static final MultiblockExtents ASSEMBLER_DIMENSION_SOUTH = MultiblockExtents.ofLegacy(new int[] { 1, 2, 1, 0, 2, 1 });
    public static final MultiblockExtents ASSEMBLER_DIMENSION_WEST = MultiblockExtents.ofLegacy(new int[] { 1, 2, 1, 0, 1, 2 });
    public static final MultiblockExtents CHEMPLANT_DIMENSION_NORTH = MultiblockExtents.ofLegacy(new int[] { 2, 1, 2, 0, 1, 2 });
    public static final MultiblockExtents CHEMPLANT_DIMENSION_EAST = MultiblockExtents.ofLegacy(new int[] { 2, 1, 2, 0, 2, 1 });
    public static final MultiblockExtents CHEMPLANT_DIMENSION_SOUTH = MultiblockExtents.ofLegacy(new int[] { 1, 2, 2, 0, 2, 1 });
    public static final MultiblockExtents CHEMPLANT_DIMENSION_WEST = MultiblockExtents.ofLegacy(new int[] { 1, 2, 2, 0, 1, 2 });
    public static final MultiblockExtents FLUID_TANK_DIMENSION_NS = MultiblockExtents.ofLegacy(new int[] { 1, 1, 2, 0, 2, 2 });
    public static final MultiblockExtents FLUID_TANK_DIMENSION_EW = MultiblockExtents.ofLegacy(new int[] { 2, 2, 2, 0, 1, 1 });
    public static final MultiblockExtents REFINERY_DIMENSIONS = MultiblockExtents.ofLegacy(new int[] { 1, 1, 8, 0, 1, 1 });
    public static final MultiblockExtents PUMPJACK_DIMENSION_NORTH = MultiblockExtents.ofLegacy(new int[] { 1, 1, 4, 0, 6, 0 });
    public static final MultiblockExtents PUMPJACK_DIMENSION_EAST = MultiblockExtents.ofLegacy(new int[] { 0, 6, 4, 0, 1, 1 });
    public static final MultiblockExtents PUMPJACK_DIMENSION_SOUTH = MultiblockExtents.ofLegacy(new int[] { 1, 1, 4, 0, 0, 6 });
    public static final MultiblockExtents PUMPJACK_DIMENSION_WEST = MultiblockExtents.ofLegacy(new int[] { 6, 0, 4, 0, 1, 1 });
    public static final MultiblockExtents TURBOFAN_DIMENSION_NORTH = MultiblockExtents.ofLegacy(new int[] { 1, 1, 2, 0, 3, 3 });
    public static final MultiblockExtents TURBOFAN_DIMENSION_EAST = MultiblockExtents.ofLegacy(new int[] { 3, 3, 2, 0, 1, 1 });
    public static final MultiblockExtents TURBOFAN_DIMENSION_SOUTH = MultiblockExtents.ofLegacy(new int[] { 1, 1, 2, 0, 3, 3 });
    public static final MultiblockExtents TURBOFAN_DIMENSION_WEST = MultiblockExtents.ofLegacy(new int[] { 3, 3, 2, 0, 1, 1 });
    public static final MultiblockExtents AMS_LIMITER_DIMENSION_NORTH = MultiblockExtents.ofLegacy(new int[] { 0, 0, 5, 0, 2, 2 });
    public static final MultiblockExtents AMS_LIMITER_DIMENSION_EAST = MultiblockExtents.ofLegacy(new int[] { 2, 2, 5, 0, 0, 0 });
    public static final MultiblockExtents AMS_LIMITER_DIMENSION_SOUTH = MultiblockExtents.ofLegacy(new int[] { 0, 0, 5, 0, 2, 2 });
    public static final MultiblockExtents AMS_LIMITER_DIMENSION_WEST = MultiblockExtents.ofLegacy(new int[] { 2, 2, 5, 0, 0, 0 });
    public static final MultiblockExtents AMS_EMITTER_DIMENSION = MultiblockExtents.ofLegacy(new int[] { 2, 2, 5, 0, 2, 2 });
    public static final MultiblockExtents AMS_BASE_DIMENSION = MultiblockExtents.ofLegacy(new int[] { 1, 1, 1, 0, 1, 1 });
    public static final MultiblockExtents RAD_GEN_DIMENSION_NORTH = MultiblockExtents.ofLegacy(new int[] { 4, 1, 2, 0, 1, 1 });
    public static final MultiblockExtents RAD_GEN_DIMENSION_EAST = MultiblockExtents.ofLegacy(new int[] { 1, 1, 2, 0, 4, 1 });
    public static final MultiblockExtents RAD_GEN_DIMENSION_SOUTH = MultiblockExtents.ofLegacy(new int[] { 1, 4, 2, 0, 1, 1 });
    public static final MultiblockExtents RAD_GEN_DIMENSION_WEST = MultiblockExtents.ofLegacy(new int[] { 1, 1, 2, 0, 1, 4 });
    public static final MultiblockExtents REACTOR_SMALL_DIMENSION = MultiblockExtents.ofLegacy(new int[] { 0, 0, 2, 0, 0, 0 });
    public static final MultiblockExtents UF6_DIMENSION = MultiblockExtents.ofLegacy(new int[] { 0, 0, 1, 0, 0, 0 });

    public static boolean checkSpace(Level level, BlockPos corePos, MultiblockExtents extents) {
        return checkSpace(level, corePos, extents, null);
    }

    public static boolean checkSpace(Level level, BlockPos corePos, MultiblockExtents extents, @Nullable BlockPos temporaryPos) {
        return checkSpace(level, corePos, extents.offsets(), temporaryPos);
    }

    public static boolean checkSpace(Level level, BlockPos corePos, Iterable<BlockPos> offsets) {
        return checkSpace(level, corePos, offsets, null);
    }

    public static boolean checkSpace(Level level, BlockPos corePos, Iterable<BlockPos> offsets, @Nullable BlockPos temporaryPos) {
        if (!isReplaceableOrTemporary(level, corePos, temporaryPos)) {
            return false;
        }
        for (BlockPos offset : offsets) {
            if (offset.equals(BlockPos.ZERO)) {
                continue;
            }
            BlockPos pos = corePos.offset(offset);
            if (!isReplaceableOrTemporary(level, pos, temporaryPos)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkLayout(Level level, BlockPos corePos, LegacyMultiblockLayout layout,
            @Nullable BlockPos temporaryPos) {
        return checkSpace(level, corePos, layout.checkOffsets(), temporaryPos);
    }

    public static BlockPos legacyCoreFromPlacement(BlockPos placedPos, Direction facing, int legacyOffset) {
        return legacyOffset == 0 ? placedPos : placedPos.relative(facing.getOpposite(), legacyOffset);
    }

    public static BlockState steelParticleState() {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(HbmNtm.MOD_ID, "block_steel"));
        return block == null ? Blocks.IRON_BLOCK.defaultBlockState() : block.defaultBlockState();
    }

    public static boolean fillUp(Level level, BlockPos corePos, MultiblockExtents extents) {
        return fillUp(level, corePos, extents, ModBlocks.DUMMY_BLOCK.get().defaultBlockState());
    }

    public static boolean fillUp(Level level, BlockPos corePos, MultiblockExtents extents, BlockState dummyState) {
        return fillUp(level, corePos, extents, dummyState, offset -> false);
    }

    public static boolean fillUp(Level level, BlockPos corePos, MultiblockExtents extents, Predicate<BlockPos> proxyOffsets) {
        return fillUp(level, corePos, extents, ModBlocks.DUMMY_BLOCK.get().defaultBlockState(), proxyOffsets);
    }

    public static boolean fillUp(Level level, BlockPos corePos, MultiblockExtents extents, BlockState dummyState,
            Predicate<BlockPos> proxyOffsets) {
        return fillOffsets(level, corePos, extents.offsets(), dummyState, proxyOffsets);
    }

    public static boolean fillUpWithProxyModes(Level level, BlockPos corePos, MultiblockExtents extents,
            BlockState dummyState, Function<BlockPos, LegacyProxyMode> proxyModes) {
        return fillOffsetsWithProxyModes(level, corePos, extents.offsets(), dummyState, proxyModes);
    }

    public static boolean fillUpWithProxyModes(Level level, BlockPos corePos, MultiblockExtents extents,
            BlockState dummyState, Function<BlockPos, LegacyProxyMode> proxyModes,
            Predicate<BlockPos> legacyExtraOffsets) {
        return fillOffsetsWithProxyModes(level, corePos, extents.offsets(), dummyState, proxyModes, legacyExtraOffsets);
    }

    public static boolean fillOffsets(Level level, BlockPos corePos, Iterable<BlockPos> offsets) {
        return fillOffsets(level, corePos, offsets, ModBlocks.DUMMY_BLOCK.get().defaultBlockState());
    }

    public static boolean fillOffsets(Level level, BlockPos corePos, Iterable<BlockPos> offsets, BlockState dummyState) {
        return fillOffsets(level, corePos, offsets, dummyState, offset -> false);
    }

    public static boolean fillOffsets(Level level, BlockPos corePos, Iterable<BlockPos> offsets, Predicate<BlockPos> proxyOffsets) {
        return fillOffsets(level, corePos, offsets, ModBlocks.DUMMY_BLOCK.get().defaultBlockState(), proxyOffsets);
    }

    public static boolean fillOffsets(Level level, BlockPos corePos, Iterable<BlockPos> offsets, BlockState dummyState,
            Predicate<BlockPos> proxyOffsets) {
        return fillOffsetsWithProxyModes(level, corePos, offsets, dummyState,
                offset -> proxyOffsets.test(offset) ? LegacyProxyMode.fullCombo() : LegacyProxyMode.none());
    }

    public static boolean fillOffsetsWithProxyModes(Level level, BlockPos corePos, Iterable<BlockPos> offsets,
            Function<BlockPos, LegacyProxyMode> proxyModes) {
        return fillOffsetsWithProxyModes(level, corePos, offsets, ModBlocks.DUMMY_BLOCK.get().defaultBlockState(), proxyModes);
    }

    public static boolean fillOffsetsWithProxyModes(Level level, BlockPos corePos, Iterable<BlockPos> offsets,
            Function<BlockPos, LegacyProxyMode> proxyModes, Predicate<BlockPos> legacyExtraOffsets) {
        return fillOffsetsWithProxyModes(level, corePos, offsets, ModBlocks.DUMMY_BLOCK.get().defaultBlockState(),
                proxyModes, legacyExtraOffsets);
    }

    public static boolean fillOffsetsWithProxyModes(Level level, BlockPos corePos, Iterable<BlockPos> offsets,
            BlockState dummyState, Function<BlockPos, LegacyProxyMode> proxyModes) {
        return fillOffsetsWithProxyModes(level, corePos, offsets, dummyState, proxyModes, offset -> false);
    }

    public static boolean fillOffsetsWithProxyModes(Level level, BlockPos corePos, Iterable<BlockPos> offsets,
            BlockState dummyState, Function<BlockPos, LegacyProxyMode> proxyModes,
            Predicate<BlockPos> legacyExtraOffsets) {
        if (level.isClientSide) {
            return false;
        }
        for (BlockPos offset : offsets) {
            if (offset.equals(BlockPos.ZERO)) {
                continue;
            }
            BlockPos pos = corePos.offset(offset);
            level.setBlock(pos, dummyState, Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity dummy) {
                dummy.setCorePos(corePos);
                dummy.setProxyMode(proxyModes.apply(offset));
                dummy.setLegacyExtra(legacyExtraOffsets.test(offset));
            }
        }
        return true;
    }

    public static boolean fillLayout(Level level, BlockPos corePos, LegacyMultiblockLayout layout) {
        return fillOffsetsWithProxyModes(level, corePos, layout.offsets(), layout::proxyMode,
                layout::isLegacyExtraOffset);
    }

    public static boolean removeAll(Level level, BlockPos corePos, MultiblockExtents extents) {
        return removeOffsets(level, corePos, extents.offsets());
    }

    public static boolean removeLayout(Level level, BlockPos corePos, LegacyMultiblockLayout layout) {
        return removeOffsets(level, corePos, layout.offsets());
    }

    public static boolean removeOffsets(Level level, BlockPos corePos, Iterable<BlockPos> offsets) {
        if (level.isClientSide) {
            return false;
        }
        return withClearing(level, corePos, () -> {
            for (BlockPos offset : offsets) {
                if (offset.equals(BlockPos.ZERO)) {
                    continue;
                }
                BlockPos pos = corePos.offset(offset);
                if (level.getBlockState(pos).getBlock() instanceof DummyBlock) {
                    level.removeBlock(pos, false);
                }
            }
        });
    }

    public static boolean makeLegacyExtra(Level level, BlockPos pos, LegacyProxyMode proxyMode) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity dummy)) {
            return false;
        }
        dummy.setLegacyExtra(true);
        dummy.setProxyMode(proxyMode);
        return true;
    }

    public static boolean removeLegacyExtra(Level level, BlockPos pos) {
        return removeLegacyExtra(level, pos, LegacyProxyMode.none());
    }

    public static boolean removeLegacyExtra(Level level, BlockPos pos, LegacyProxyMode restoredProxyMode) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity dummy)) {
            return false;
        }
        if (!dummy.isLegacyExtra()) {
            return false;
        }
        dummy.setLegacyExtra(false);
        dummy.setProxyMode(restoredProxyMode);
        return true;
    }

    public static boolean isLegacyExtra(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity dummy && dummy.isLegacyExtra();
    }

    @Nullable
    public static CoreLookup findCore(BlockGetter level, @Nullable BlockPos pos) {
        CoreLookup directCore = findCoreAt(level, pos);
        if (directCore != null) {
            return directCore;
        }
        if (pos == null) {
            return null;
        }
        if (!(level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity dummy) || dummy.getCorePos() == null) {
            return null;
        }
        CoreLookup core = findCoreAt(level, dummy.getCorePos());
        return ownsDummy(level, core, pos) ? core : null;
    }

    @Nullable
    public static CoreLookup findCoreAt(BlockGetter level, @Nullable BlockPos corePos) {
        if (corePos == null) {
            return null;
        }
        if (level instanceof Level worldLevel && !worldLevel.hasChunkAt(corePos)) {
            return null;
        }
        BlockState coreState = level.getBlockState(corePos);
        return coreState.getBlock() instanceof MultiblockCoreBlock ? new CoreLookup(corePos.immutable(), coreState) : null;
    }

    public static boolean ownsDummy(BlockGetter level, @Nullable CoreLookup core, BlockPos dummyPos) {
        if (core == null || !(core.state().getBlock() instanceof MultiblockCoreBlock coreBlock)) {
            return false;
        }
        return coreBlock.ownsMultiblockDummy(core.state(), level, core.pos(), dummyPos);
    }

    @Nullable
    public static BlockEntity resolveCoreBlockEntity(BlockEntity blockEntity) {
        if (blockEntity == null || blockEntity.getLevel() == null) {
            return blockEntity;
        }
        return resolveCoreBlockEntity(blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity);
    }

    @Nullable
    public static BlockEntity resolveCoreBlockEntity(BlockGetter level, @Nullable BlockPos pos) {
        return resolveCoreBlockEntity(level, pos, pos == null ? null : level.getBlockEntity(pos));
    }

    @Nullable
    private static BlockEntity resolveCoreBlockEntity(BlockGetter level, @Nullable BlockPos pos,
            @Nullable BlockEntity fallback) {
        if (pos == null) {
            return fallback;
        }
        CoreLookup core = findCore(level, pos);
        if (core == null) {
            return fallback;
        }
        BlockEntity coreEntity = level.getBlockEntity(core.pos());
        return coreEntity == null ? fallback : coreEntity;
    }

    private static boolean isReplaceableOrTemporary(Level level, BlockPos pos, @Nullable BlockPos temporaryPos) {
        return (pos.equals(temporaryPos) && level.hasChunkAt(pos))
                || (level.hasChunkAt(pos) && level.getBlockState(pos).canBeReplaced());
    }

    public static boolean withClearing(Level level, BlockPos corePos, Runnable action) {
        ClearingKey key = new ClearingKey(level, corePos.immutable());
        Set<ClearingKey> active = CLEARING.get();
        if (!active.add(key)) {
            return false;
        }
        try {
            action.run();
            return true;
        } finally {
            active.remove(key);
            if (active.isEmpty()) {
                CLEARING.remove();
            }
        }
    }

    public static boolean isClearing(Level level, BlockPos dummyPos) {
        if (!(level.getBlockEntity(dummyPos) instanceof MultiblockDummyBlockEntity dummy) || dummy.getCorePos() == null) {
            return false;
        }
        return CLEARING.get().contains(new ClearingKey(level, dummy.getCorePos()));
    }

    private record ClearingKey(Level level, BlockPos corePos) {
    }

    public record CoreLookup(BlockPos pos, BlockState state) {
    }

    private MultiblockHelper() {
    }
}
