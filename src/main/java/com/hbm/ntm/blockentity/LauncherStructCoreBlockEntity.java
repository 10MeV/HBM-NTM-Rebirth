package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.LauncherStructCoreBlock;
import com.hbm.ntm.multiblock.LegacyMultiblockPlaceable;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class LauncherStructCoreBlockEntity extends BlockEntity {
    public LauncherStructCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAUNCHER_STRUCT_CORE.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (getBlockState().getBlock() instanceof LauncherStructCoreBlock block
                && block.kind() == LauncherStructCoreBlock.Kind.LAUNCH_TABLE) {
            return new AABB(worldPosition.offset(-4, 0, -4), worldPosition.offset(5, 12, 5));
        }
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 1, 2));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            LauncherStructCoreBlockEntity blockEntity) {
        if (level.getGameTime() % 20L != 0L
                || !(state.getBlock() instanceof LauncherStructCoreBlock coreBlock)) {
            return;
        }
        if (coreBlock.kind() == LauncherStructCoreBlock.Kind.COMPACT) {
            if (isCompact(level, pos)) {
                assembleCompact(level, pos);
            }
        } else {
            Direction facing = tableFacing(level, pos);
            if (facing != null) {
                assembleTable(level, pos, facing);
            }
        }
    }

    private static boolean isCompact(Level level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if ((x != 0 || z != 0) && !isLauncher(level, pos.offset(x, 0, z))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Direction tableFacing(Level level, BlockPos pos) {
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                if ((x != 0 || z != 0) && !isLauncher(level, pos.offset(x, 0, z))) {
                    return null;
                }
            }
        }
        if (isScaffoldColumn(level, pos, 3, 0)) {
            return Direction.EAST;
        }
        if (isScaffoldColumn(level, pos, -3, 0)) {
            return Direction.WEST;
        }
        if (isScaffoldColumn(level, pos, 0, 3)) {
            return Direction.SOUTH;
        }
        if (isScaffoldColumn(level, pos, 0, -3)) {
            return Direction.NORTH;
        }
        return null;
    }

    private static boolean isScaffoldColumn(Level level, BlockPos pos, int x, int z) {
        for (int y = 1; y < 12; y++) {
            if (!level.getBlockState(pos.offset(x, y, z)).is(ModBlocks.STRUCT_SCAFFOLD.get())) {
                return false;
            }
        }
        return true;
    }

    private static void assembleCompact(Level level, BlockPos corePos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x != 0 || z != 0) {
                    level.removeBlock(corePos.offset(x, 0, z), false);
                }
            }
        }
        BlockState launcherState = ModBlocks.COMPACT_LAUNCHER.get().defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.setBlock(corePos, launcherState, Block.UPDATE_ALL);
        completeMultiblock(level, corePos, launcherState);
    }

    private static void assembleTable(Level level, BlockPos corePos, Direction facing) {
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                if (x != 0 || z != 0) {
                    level.removeBlock(corePos.offset(x, 0, z), false);
                }
            }
        }
        Direction scaffoldDirection = facing;
        for (int y = 1; y < 12; y++) {
            level.removeBlock(corePos.relative(scaffoldDirection, 3).above(y), false);
        }
        BlockState launcherState = ModBlocks.LAUNCH_TABLE.get().defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, facing);
        level.setBlock(corePos, launcherState, Block.UPDATE_ALL);
        completeMultiblock(level, corePos, launcherState);
    }

    private static void completeMultiblock(Level level, BlockPos corePos, BlockState state) {
        if (state.getBlock() instanceof LegacyMultiblockPlaceable multiblock) {
            multiblock.completeDirectMultiblockPlacement(level, corePos, state, null, ItemStack.EMPTY);
        }
    }

    private static boolean isLauncher(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.STRUCT_LAUNCHER.get());
    }
}
