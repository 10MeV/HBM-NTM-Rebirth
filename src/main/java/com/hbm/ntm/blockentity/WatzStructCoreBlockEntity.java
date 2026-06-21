package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.WatzEndBlock;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class WatzStructCoreBlockEntity extends BlockEntity {
    public WatzStructCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WATZ_STRUCT_CORE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WatzStructCoreBlockEntity blockEntity) {
        if (level.getGameTime() % 20L != 0L || !blockEntity.isCompleteStructure(level, pos)) {
            return;
        }
        assemble(level, pos);
    }

    private boolean isCompleteStructure(Level level, BlockPos pos) {
        if (!isBlock(level, pos.offset(0, 1, 0), ModBlocks.WATZ_COOLER.get())) return false;
        if (!isBlock(level, pos.offset(0, 2, 0), ModBlocks.WATZ_COOLER.get())) return false;

        for (int y = 0; y < 3; y++) {
            if (!checkLayer(level, pos, y)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkLayer(Level level, BlockPos pos, int y) {
        int[][] elements = {
                {1, 0}, {2, 0}, {0, 1}, {0, 2}, {-1, 0}, {-2, 0}, {0, -1}, {0, -2},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };
        int[][] coolers = {
                {2, 1}, {2, -1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {1, -2}, {-1, -2}
        };
        for (int[] offset : elements) {
            if (!isBlock(level, pos.offset(offset[0], y, offset[1]), ModBlocks.WATZ_ELEMENT.get())) return false;
        }
        for (int[] offset : coolers) {
            if (!isBlock(level, pos.offset(offset[0], y, offset[1]), ModBlocks.WATZ_COOLER.get())) return false;
        }
        for (int z = -1; z < 2; z++) {
            if (!isRivetedEnd(level, pos.offset(3, y, z))) return false;
            if (!isRivetedEnd(level, pos.offset(z, y, 3))) return false;
            if (!isRivetedEnd(level, pos.offset(-3, y, z))) return false;
            if (!isRivetedEnd(level, pos.offset(z, y, -3))) return false;
        }
        return isRivetedEnd(level, pos.offset(2, y, 2))
                && isRivetedEnd(level, pos.offset(2, y, -2))
                && isRivetedEnd(level, pos.offset(-2, y, 2))
                && isRivetedEnd(level, pos.offset(-2, y, -2));
    }

    private static boolean isBlock(Level level, BlockPos pos, Block block) {
        return level.getBlockState(pos).is(block);
    }

    private static boolean isRivetedEnd(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlocks.WATZ_END.get()) && state.getValue(WatzEndBlock.RIVETED);
    }

    private static void assemble(Level level, BlockPos corePos) {
        for (BlockPos pos : structurePositions(corePos)) {
            if (!pos.equals(corePos)) {
                level.removeBlock(pos, false);
            }
        }
        BlockState coreState = ModBlocks.WATZ.get().defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.NORTH);
        level.setBlock(corePos, coreState, Block.UPDATE_ALL);
        MultiblockHelper.fillLayout(level, corePos, watzLayout());
    }

    private static Set<BlockPos> structurePositions(BlockPos corePos) {
        Set<BlockPos> positions = new LinkedHashSet<>();
        positions.add(corePos);
        positions.add(corePos.offset(0, 1, 0));
        positions.add(corePos.offset(0, 2, 0));
        for (int y = 0; y < 3; y++) {
            int[][] elements = {
                    {1, 0}, {2, 0}, {0, 1}, {0, 2}, {-1, 0}, {-2, 0}, {0, -1}, {0, -2},
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
                    {2, 1}, {2, -1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {1, -2}, {-1, -2}
            };
            for (int[] offset : elements) {
                positions.add(corePos.offset(offset[0], y, offset[1]));
            }
            for (int z = -1; z < 2; z++) {
                positions.add(corePos.offset(3, y, z));
                positions.add(corePos.offset(z, y, 3));
                positions.add(corePos.offset(-3, y, z));
                positions.add(corePos.offset(z, y, -3));
            }
            positions.add(corePos.offset(2, y, 2));
            positions.add(corePos.offset(2, y, -2));
            positions.add(corePos.offset(-2, y, 2));
            positions.add(corePos.offset(-2, y, -2));
        }
        return positions;
    }

    private static LegacyMultiblockLayout watzLayout() {
        Direction facing = Direction.NORTH;
        BlockPos center = BlockPos.ZERO;
        return LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 3, 3, 1, 1 }, facing)
                .withLegacyXrCheckedFill(new int[] { 2, 0, 2, 2, 2, -2 }, facing, center)
                .withLegacyXrCheckedFill(new int[] { 2, 0, 2, 2, -2, 2 }, facing, center)
                .withLegacyXrCheckedFill(new int[] { 2, 0, 1, 1, 3, -3 }, facing, center)
                .withLegacyXrCheckedFill(new int[] { 2, 0, 1, 1, -3, 3 }, facing, center)
                .withExtraProxyOffsets(List.of(
                        center.offset(2, 0, 0),
                        center.offset(-2, 0, 0),
                        center.offset(0, 0, 2),
                        center.offset(0, 0, -2),
                        center.offset(2, 2, 0),
                        center.offset(-2, 2, 0),
                        center.offset(0, 2, 2),
                        center.offset(0, 2, -2),
                        center.offset(0, 2, 0)),
                        LegacyProxyMode.combo(true, false, true));
    }
}
