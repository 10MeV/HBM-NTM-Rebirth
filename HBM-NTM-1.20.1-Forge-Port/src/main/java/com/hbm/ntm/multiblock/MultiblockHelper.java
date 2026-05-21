package com.hbm.ntm.multiblock;

import com.hbm.ntm.blockentity.MultiblockDummyBlockEntity;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

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
        for (BlockPos offset : extents.offsets()) {
            BlockPos pos = corePos.offset(offset);
            if (!level.getBlockState(pos).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    public static boolean fillUp(Level level, BlockPos corePos, MultiblockExtents extents) {
        return fillUp(level, corePos, extents, ModBlocks.DUMMY_BLOCK.get().defaultBlockState());
    }

    public static boolean fillUp(Level level, BlockPos corePos, MultiblockExtents extents, BlockState dummyState) {
        if (level.isClientSide) {
            return false;
        }
        for (BlockPos offset : extents.offsets()) {
            BlockPos pos = corePos.offset(offset);
            level.setBlock(pos, dummyState, Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity dummy) {
                dummy.setCorePos(corePos);
            }
        }
        return true;
    }

    public static boolean removeAll(Level level, BlockPos corePos, MultiblockExtents extents) {
        if (level.isClientSide) {
            return false;
        }
        ClearingKey key = new ClearingKey(level, corePos.immutable());
        Set<ClearingKey> active = CLEARING.get();
        if (!active.add(key)) {
            return false;
        }
        try {
            for (BlockPos offset : extents.offsets()) {
                BlockPos pos = corePos.offset(offset);
                if (level.getBlockState(pos).getBlock() instanceof DummyBlock) {
                    level.removeBlock(pos, false);
                }
            }
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

    private MultiblockHelper() {
    }
}
