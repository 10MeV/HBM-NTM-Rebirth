package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import java.util.ArrayList;
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

public class FusionTorusStructCoreBlockEntity extends BlockEntity {
    public static final int LEGACY_LAYOUT_SIZE = 15;
    public static final int LEGACY_LAYOUT_HEIGHT = 5;
    public static final int LEGACY_LAYOUT_RADIUS = 7;

    private static final int[][][] LEGACY_LAYOUT = {
            {
                    {0,0,0,0,3,3,3,3,3,3,3,0,0,0,0},
                    {0,0,0,3,1,1,1,1,1,1,1,3,0,0,0},
                    {0,0,3,1,1,1,1,1,1,1,1,1,3,0,0},
                    {0,3,1,1,1,1,1,1,1,1,1,1,1,3,0},
                    {3,1,1,1,1,3,3,3,3,3,1,1,1,1,3},
                    {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    {3,1,1,1,1,3,3,3,3,3,1,1,1,1,3},
                    {0,3,1,1,1,1,1,1,1,1,1,1,1,3,0},
                    {0,0,3,1,1,1,1,1,1,1,1,1,3,0,0},
                    {0,0,0,3,1,1,1,1,1,1,1,3,0,0,0},
                    {0,0,0,0,3,3,3,3,3,3,3,0,0,0,0}
            },
            {
                    {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0},
                    {0,0,0,1,1,1,1,1,1,1,1,1,0,0,0},
                    {0,0,1,1,2,2,2,2,2,2,2,1,1,0,0},
                    {0,1,1,2,1,1,1,1,1,1,1,2,1,1,0},
                    {1,1,2,1,1,1,1,1,1,1,1,1,2,1,1},
                    {1,1,2,1,1,3,3,3,3,3,1,1,2,1,1},
                    {3,1,2,1,1,3,3,3,3,3,1,1,2,1,3},
                    {3,1,2,1,1,3,3,3,3,3,1,1,2,1,3},
                    {3,1,2,1,1,3,3,3,3,3,1,1,2,1,3},
                    {1,1,2,1,1,3,3,3,3,3,1,1,2,1,1},
                    {1,1,2,1,1,1,1,1,1,1,1,1,2,1,1},
                    {0,1,1,2,1,1,1,1,1,1,1,2,1,1,0},
                    {0,0,1,1,2,2,2,2,2,2,2,1,1,0,0},
                    {0,0,0,1,1,1,1,1,1,1,1,1,0,0,0},
                    {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0}
            },
            {
                    {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0},
                    {0,0,0,1,2,2,2,2,2,2,2,1,0,0,0},
                    {0,0,1,2,2,2,2,2,2,2,2,2,1,0,0},
                    {0,1,2,2,2,2,2,2,2,2,2,2,2,1,0},
                    {1,2,2,2,1,1,1,1,1,1,1,2,2,2,1},
                    {1,2,2,2,1,3,3,3,3,3,1,2,2,2,1},
                    {3,2,2,2,1,3,3,3,3,3,1,2,2,2,3},
                    {3,2,2,2,1,3,3,3,3,3,1,2,2,2,3},
                    {3,2,2,2,1,3,3,3,3,3,1,2,2,2,3},
                    {1,2,2,2,1,3,3,3,3,3,1,2,2,2,1},
                    {1,2,2,2,1,1,1,1,1,1,1,2,2,2,1},
                    {0,1,2,2,2,2,2,2,2,2,2,2,2,1,0},
                    {0,0,1,2,2,2,2,2,2,2,2,2,1,0,0},
                    {0,0,0,1,2,2,2,2,2,2,2,1,0,0,0},
                    {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0}
            }
    };
    private static final List<BlockPos> EXTRA_PORTS = List.of(
            new BlockPos(0, 4, 0),
            new BlockPos(6, 0, 0), new BlockPos(6, 4, 0), new BlockPos(6, 0, 2),
            new BlockPos(6, 4, 2), new BlockPos(6, 0, -2), new BlockPos(6, 4, -2),
            new BlockPos(-6, 0, 0), new BlockPos(-6, 4, 0), new BlockPos(-6, 0, 2),
            new BlockPos(-6, 4, 2), new BlockPos(-6, 0, -2), new BlockPos(-6, 4, -2),
            new BlockPos(0, 0, 6), new BlockPos(0, 4, 6), new BlockPos(2, 0, 6),
            new BlockPos(2, 4, 6), new BlockPos(-2, 0, 6), new BlockPos(-2, 4, 6),
            new BlockPos(0, 0, -6), new BlockPos(0, 4, -6), new BlockPos(2, 0, -6),
            new BlockPos(2, 4, -6), new BlockPos(-2, 0, -6), new BlockPos(-2, 4, -6));

    public FusionTorusStructCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_TORUS_STRUCT_CORE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            FusionTorusStructCoreBlockEntity blockEntity) {
        if (level.getGameTime() % 20L == 0L && blockEntity.isCompleteStructure(level, pos)) {
            assemble(level, pos);
        }
    }

    public static LegacyMultiblockLayout torusLayout() {
        return LegacyMultiblockLayout.ofOffsets(runtimeOffsets())
                .withExtraProxyOffsets(EXTRA_PORTS, LegacyProxyMode.fullCombo());
    }

    public static int legacyLayoutComponent(int x, int y, int z) {
        if (x < 0 || x >= LEGACY_LAYOUT_SIZE || y < 0 || y >= LEGACY_LAYOUT_HEIGHT || z < 0
                || z >= LEGACY_LAYOUT_SIZE) {
            return 0;
        }
        int layerIndex = y > 2 ? LEGACY_LAYOUT_HEIGHT - 1 - y : y;
        return LEGACY_LAYOUT[layerIndex][x][z];
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-7, 0, -7), worldPosition.offset(8, 5, 8));
    }

    private boolean isCompleteStructure(Level level, BlockPos pos) {
        for (int y = 0; y < LEGACY_LAYOUT_HEIGHT; y++) {
            for (int x = 0; x < LEGACY_LAYOUT_SIZE; x++) {
                for (int z = 0; z < LEGACY_LAYOUT_SIZE; z++) {
                    int component = legacyLayoutComponent(x, y, z);
                    if (component == 0 || x == LEGACY_LAYOUT_RADIUS && y == 0 && z == LEGACY_LAYOUT_RADIUS) {
                        continue;
                    }
                    if (!matchesComponent(level, pos.offset(x - LEGACY_LAYOUT_RADIUS, y, z - LEGACY_LAYOUT_RADIUS),
                            component)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean matchesComponent(Level level, BlockPos pos, int component) {
        BlockState state = level.getBlockState(pos);
        return switch (component) {
            case 1 -> state.is(ModBlocks.FUSION_COMPONENT_BSCCO_WELDED.get());
            case 2 -> state.is(ModBlocks.FUSION_COMPONENT_BLANKET.get());
            case 3 -> state.is(ModBlocks.FUSION_COMPONENT_MOTOR.get());
            default -> false;
        };
    }

    private static void assemble(Level level, BlockPos corePos) {
        for (BlockPos offset : structureOffsets()) {
            if (!offset.equals(BlockPos.ZERO)) {
                level.removeBlock(corePos.offset(offset), false);
            }
        }
        BlockState coreState = ModBlocks.FUSION_TORUS.get().defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.NORTH);
        level.setBlock(corePos, coreState, Block.UPDATE_ALL);
        MultiblockHelper.fillLayout(level, corePos, torusLayout());
    }

    private static List<BlockPos> runtimeOffsets() {
        Set<BlockPos> offsets = new LinkedHashSet<>();
        offsets.add(BlockPos.ZERO);
        offsets.addAll(structureOffsets());
        return List.copyOf(offsets);
    }

    private static List<BlockPos> structureOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        for (int y = 0; y < LEGACY_LAYOUT_HEIGHT; y++) {
            for (int x = 0; x < LEGACY_LAYOUT_SIZE; x++) {
                for (int z = 0; z < LEGACY_LAYOUT_SIZE; z++) {
                    if (legacyLayoutComponent(x, y, z) > 0) {
                        offsets.add(new BlockPos(x - LEGACY_LAYOUT_RADIUS, y, z - LEGACY_LAYOUT_RADIUS));
                    }
                }
            }
        }
        return List.copyOf(offsets);
    }
}
