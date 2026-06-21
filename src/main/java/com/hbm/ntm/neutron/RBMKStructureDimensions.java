package com.hbm.ntm.neutron;

import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.multiblock.DummyBlock;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Names the legacy RBMK height contracts so callers do not collapse a
 * BlockDummyable column into one core block. Legacy RBMKDials#getColumnHeight
 * is the number of dummy/top segments above the core; NeutronHandler adds one
 * more segment when scanning flux hits.
 */
public final class RBMKStructureDimensions {
    private static final int MIN_COLUMN_HEIGHT_ABOVE_CORE = 1;
    private static final int MIN_NEUTRON_SCAN_SEGMENTS = MIN_COLUMN_HEIGHT_ABOVE_CORE + 1;

    private RBMKStructureDimensions() {
    }

    public static int columnHeightAboveCore() {
        return Math.max(MIN_COLUMN_HEIGHT_ABOVE_CORE, RBMKNeutronHandler.settings().columnHeight() - 1);
    }

    public static int neutronScanSegments() {
        return Math.max(MIN_NEUTRON_SCAN_SEGMENTS, RBMKNeutronHandler.settings().columnHeight());
    }

    public static BlockPos columnTop(BlockPos corePos) {
        return corePos.above(columnHeightAboveCore());
    }

    public static BlockPos topFluidPort(BlockPos corePos) {
        return corePos.above(neutronScanSegments());
    }

    @Nullable
    public static MultiblockHelper.CoreLookup findVerticalColumnCore(BlockGetter level, BlockPos segmentPos) {
        if (!(level.getBlockState(segmentPos).getBlock() instanceof DummyBlock)) {
            return null;
        }
        int heightAbove = columnHeightAboveCore();
        for (int segment = 1; segment <= heightAbove; segment++) {
            BlockPos corePos = segmentPos.below(segment);
            BlockState coreState = level.getBlockState(corePos);
            if (!(coreState.getBlock() instanceof RBMKColumnBlock column)) {
                continue;
            }
            LegacyMultiblockLayout layout = column.getMultiblockLayout(coreState, level, corePos);
            BlockPos offset = segmentPos.subtract(corePos);
            if (layout != null && layout.containsOffset(offset)) {
                return new MultiblockHelper.CoreLookup(corePos.immutable(), coreState);
            }
        }
        return null;
    }
}
