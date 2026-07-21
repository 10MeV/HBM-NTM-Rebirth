package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.SoyuzLauncherBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class SoyuzStructBlockEntity extends BlockEntity {
    private static final int BUILD_INTERVAL = 20;
    private int age;

    public SoyuzStructBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOYUZ_STRUCT.get(), pos, state);
    }

    /**
     * Direct port of TileEntitySoyuzStruct#updateEntity. The old structure core
     * checked its assembly only every twenty server ticks, then converted it into
     * the fixed-east Soyuz launcher footprint.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, SoyuzStructBlockEntity struct) {
        if (level.isClientSide || ++struct.age < BUILD_INTERVAL) {
            return;
        }
        struct.age = 0;
        if (!struct.matchesStructure(level, pos)) {
            return;
        }

        struct.clearScaffolding(level, pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        BlockPos launcherPos = pos.above(4);
        BlockState launcherState = ModBlocks.SOYUZ_LAUNCHER.get().defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.EAST);
        level.setBlock(launcherPos, launcherState, Block.UPDATE_ALL);
        ((SoyuzLauncherBlock) launcherState.getBlock()).completeDirectMultiblockPlacement(level, launcherPos,
                launcherState, null, ItemStack.EMPTY);
    }

    private boolean matchesStructure(Level level, BlockPos pos) {
        return matches(level, pos, ModBlocks.STRUCT_LAUNCHER.get(), -6, 6, 3, 4, -6, 6)
                && matches(level, pos, ModBlocks.STRUCT_LAUNCHER.get(), -1, 1, 3, 4, -8, -7)
                && matches(level, pos, ModBlocks.STRUCT_LAUNCHER.get(), -2, 2, 3, 4, 7, 9)
                && matches(level, pos, ModBlocks.STRUCT_LAUNCHER.get(), -2, 2, 51, 51, 5, 9)
                && matches(level, pos, ModBlocks.STRUCT_LAUNCHER.get(), -1, 1, 38, 38, -8, -6)
                && matchesLegs(level, pos, 3, 6, 0, 2, 3, 6)
                && matchesLegs(level, pos, -6, -3, 0, 2, 3, 6)
                && matchesLegs(level, pos, -6, -3, 0, 2, -6, -3)
                && matchesLegs(level, pos, 3, 6, 0, 2, -6, -3)
                && matchesLegs(level, pos, -1, 1, 0, 2, -8, -6)
                && matchesLegs(level, pos, -2, 2, 0, 2, 5, 9)
                && matches(level, pos, ModBlocks.STRUCT_SCAFFOLD.get(), -1, 1, 5, 50, 6, 8)
                && matches(level, pos, ModBlocks.STRUCT_SCAFFOLD.get(), 0, 0, 5, 37, -7, -7);
    }

    private static boolean matches(Level level, BlockPos origin, Block expected,
            int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!level.getBlockState(origin.offset(x, y, z)).is(expected)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean matchesLegs(Level level, BlockPos origin,
            int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        Block concrete = ModBlocks.legacyBlock("concrete").get();
        Block smoothConcrete = ModBlocks.legacyBlock("concrete_smooth").get();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockState state = level.getBlockState(origin.offset(x, y, z));
                    if (!state.is(concrete) && !state.is(smoothConcrete)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void clearScaffolding(Level level, BlockPos pos) {
        clear(level, pos, -2, 2, 51, 51, 5, 9);
        clear(level, pos, -1, 1, 38, 38, -8, -6);
        clear(level, pos, -2, 2, 0, 2, 5, 9);
        clear(level, pos, -1, 1, 5, 50, 6, 8);
        clear(level, pos, 0, 0, 5, 37, -7, -7);
    }

    private static void clear(Level level, BlockPos origin,
            int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-8, 0, -8), worldPosition.offset(8, 52, 10));
    }
}
