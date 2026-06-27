package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ICFStructCoreBlockEntity extends BlockEntity {
    public static final int PREVIEW_LENGTH_MIN = -8;
    public static final int PREVIEW_LENGTH_MAX = 8;
    public static final int PREVIEW_WIDTH_MIN = -2;
    public static final int PREVIEW_WIDTH_MAX = 2;
    public static final int PREVIEW_HEIGHT = 6;
    public static final int PREVIEW_META_SCAFFOLD = 0;
    public static final int PREVIEW_META_VESSEL_WELDED = 2;
    public static final int PREVIEW_META_STRUCTURE_BOLTED = 4;

    public ICFStructCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ICF_STRUCT_CORE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ICFStructCoreBlockEntity blockEntity) {
        if (level.getGameTime() % 20L != 0L || !state.hasProperty(HorizontalMachineBlock.FACING)) {
            return;
        }
        Direction facing = state.getValue(HorizontalMachineBlock.FACING);
        if (blockEntity.isCompleteStructure(level, pos, facing)) {
            assemble(level, pos, facing);
        }
    }

    public static LegacyMultiblockLayout icfLayout(Direction facing) {
        Direction rot = facing.getClockWise();
        return LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 5, 0, 1, 1, 8, 8 }, facing)
                .withLegacyXrCheckedFill(new int[] { 1, 1, -1, 2, 8, 8 }, facing, new BlockPos(0, 3, 0))
                .withLegacyXrCheckedFill(new int[] { 1, 1, 2, -1, 8, 8 }, facing, new BlockPos(0, 3, 0))
                .withExtraProxyOffsets(List.of(
                        new BlockPos(0, 5, 0),
                        new BlockPos(facing.getStepX() * 2 + rot.getStepX() * 6, 3,
                                facing.getStepZ() * 2 + rot.getStepZ() * 6),
                        new BlockPos(facing.getStepX() * 2 - rot.getStepX() * 6, 3,
                                facing.getStepZ() * 2 - rot.getStepZ() * 6),
                        new BlockPos(-facing.getStepX() * 2 + rot.getStepX() * 6, 3,
                                -facing.getStepZ() * 2 + rot.getStepZ() * 6),
                        new BlockPos(-facing.getStepX() * 2 - rot.getStepX() * 6, 3,
                                -facing.getStepZ() * 2 - rot.getStepZ() * 6)),
                        LegacyProxyMode.combo(true, false, true));
    }

    @Override
    public AABB getRenderBoundingBox() {
        return previewRenderBoundingBox(worldPosition, getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH);
    }

    public static int legacyPreviewComponent(int widthwiseOffset, int y, int lengthwiseOffset) {
        if (lengthwiseOffset < PREVIEW_LENGTH_MIN || lengthwiseOffset > PREVIEW_LENGTH_MAX
                || widthwiseOffset < PREVIEW_WIDTH_MIN || widthwiseOffset > PREVIEW_WIDTH_MAX
                || y < 0 || y >= PREVIEW_HEIGHT) {
            return -1;
        }
        if (y == 0) {
            if (widthwiseOffset == 1 || widthwiseOffset == -1
                    || (widthwiseOffset == 0 && lengthwiseOffset != 0)) {
                return PREVIEW_META_SCAFFOLD;
            }
            return -1;
        }
        if (widthwiseOffset == 0 && y == 3) {
            return PREVIEW_META_VESSEL_WELDED;
        }
        int structureBlock = Math.abs(lengthwiseOffset) <= 2
                ? PREVIEW_META_VESSEL_WELDED
                : PREVIEW_META_STRUCTURE_BOLTED;
        return switch (y) {
            case 1, 5 -> Math.abs(widthwiseOffset) <= 1 ? structureBlock : -1;
            case 2, 4 -> structureBlock;
            case 3 -> widthwiseOffset != 0 ? structureBlock : -1;
            default -> -1;
        };
    }

    private boolean isCompleteStructure(Level level, BlockPos pos, Direction facing) {
        Direction rot = facing.getClockWise();
        for (int i = PREVIEW_LENGTH_MIN; i <= PREVIEW_LENGTH_MAX; i++) {
            if (!matches(level, relative(pos, facing, rot, 1, 0, i), ModBlocks.ICF_COMPONENT_SCAFFOLD.get())) {
                return false;
            }
            if (i != 0 && !matches(level, relative(pos, facing, rot, 0, 0, i),
                    ModBlocks.ICF_COMPONENT_SCAFFOLD.get())) {
                return false;
            }
            if (!matches(level, relative(pos, facing, rot, -1, 0, i), ModBlocks.ICF_COMPONENT_SCAFFOLD.get())) {
                return false;
            }
            if (!matches(level, relative(pos, facing, rot, 0, 3, i), ModBlocks.ICF_COMPONENT_VESSEL_WELDED.get())) {
                return false;
            }

            Block structureBlock = Math.abs(i) <= 2
                    ? ModBlocks.ICF_COMPONENT_VESSEL_WELDED.get()
                    : ModBlocks.ICF_COMPONENT_STRUCTURE_BOLTED.get();
            for (int j = -1; j <= 1; j++) {
                if (!matches(level, relative(pos, facing, rot, j, 1, i), structureBlock)) return false;
            }
            for (int j = -2; j <= 2; j++) {
                if (!matches(level, relative(pos, facing, rot, j, 2, i), structureBlock)) return false;
            }
            for (int j = -2; j <= 2; j++) {
                if (j != 0 && !matches(level, relative(pos, facing, rot, j, 3, i), structureBlock)) return false;
            }
            for (int j = -2; j <= 2; j++) {
                if (!matches(level, relative(pos, facing, rot, j, 4, i), structureBlock)) return false;
            }
            for (int j = -1; j <= 1; j++) {
                if (!matches(level, relative(pos, facing, rot, j, 5, i), structureBlock)) return false;
            }
        }
        return true;
    }

    private static boolean matches(Level level, BlockPos pos, Block block) {
        return level.getBlockState(pos).is(block);
    }

    private static void assemble(Level level, BlockPos corePos, Direction facing) {
        for (BlockPos pos : structurePositions(corePos, facing)) {
            if (!pos.equals(corePos)) {
                level.removeBlock(pos, false);
            }
        }
        BlockState reactorState = ModBlocks.ICF.get().defaultBlockState().setValue(HorizontalMachineBlock.FACING, facing);
        level.setBlock(corePos, reactorState, Block.UPDATE_ALL);
        MultiblockHelper.fillLayout(level, corePos, icfLayout(facing));
    }

    private static Set<BlockPos> structurePositions(BlockPos corePos, Direction facing) {
        Direction rot = facing.getClockWise();
        Set<BlockPos> positions = new LinkedHashSet<>();
        positions.add(corePos);
        for (int i = PREVIEW_LENGTH_MIN; i <= PREVIEW_LENGTH_MAX; i++) {
            positions.add(relative(corePos, facing, rot, 1, 0, i));
            if (i != 0) positions.add(relative(corePos, facing, rot, 0, 0, i));
            positions.add(relative(corePos, facing, rot, -1, 0, i));
            positions.add(relative(corePos, facing, rot, 0, 3, i));
            for (int j = -1; j <= 1; j++) positions.add(relative(corePos, facing, rot, j, 1, i));
            for (int j = -2; j <= 2; j++) positions.add(relative(corePos, facing, rot, j, 2, i));
            for (int j = -2; j <= 2; j++) {
                if (j != 0) positions.add(relative(corePos, facing, rot, j, 3, i));
            }
            for (int j = -2; j <= 2; j++) positions.add(relative(corePos, facing, rot, j, 4, i));
            for (int j = -1; j <= 1; j++) positions.add(relative(corePos, facing, rot, j, 5, i));
        }
        return positions;
    }

    public static AABB previewRenderBoundingBox(BlockPos corePos, Direction facing) {
        Direction rot = facing.getClockWise();
        int minX = corePos.getX();
        int minY = corePos.getY();
        int minZ = corePos.getZ();
        int maxX = corePos.getX() + 1;
        int maxY = corePos.getY() + 1;
        int maxZ = corePos.getZ() + 1;
        for (int y = 0; y < PREVIEW_HEIGHT; y++) {
            for (int width = PREVIEW_WIDTH_MIN; width <= PREVIEW_WIDTH_MAX; width++) {
                for (int length = PREVIEW_LENGTH_MIN; length <= PREVIEW_LENGTH_MAX; length++) {
                    if (legacyPreviewComponent(width, y, length) < 0) {
                        continue;
                    }
                    BlockPos block = relative(corePos, facing, rot, width, y, length);
                    minX = Math.min(minX, block.getX());
                    minY = Math.min(minY, block.getY());
                    minZ = Math.min(minZ, block.getZ());
                    maxX = Math.max(maxX, block.getX() + 1);
                    maxY = Math.max(maxY, block.getY() + 1);
                    maxZ = Math.max(maxZ, block.getZ() + 1);
                }
            }
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static BlockPos relative(BlockPos origin, Direction facing, Direction rot, int widthwiseOffset, int y,
            int lengthwiseOffset) {
        return origin.offset(
                facing.getStepX() * widthwiseOffset + rot.getStepX() * lengthwiseOffset,
                y,
                facing.getStepZ() * widthwiseOffset + rot.getStepZ() * lengthwiseOffset);
    }
}
