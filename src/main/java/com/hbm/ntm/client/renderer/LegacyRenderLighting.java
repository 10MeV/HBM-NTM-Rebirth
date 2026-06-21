package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class LegacyRenderLighting {
    private static final double INSIDE_EPSILON = 1.0E-4D;

    public static int resolveBlockEntityLight(BlockEntity blockEntity, int packedLight) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return packedLight;
        }
        BlockPos pos = blockEntity.getBlockPos();
        return brightest(packedLight,
                LevelRenderer.getLightColor(level, pos),
                LevelRenderer.getLightColor(level, pos.above()));
    }

    public static int resolveMultiblockLight(BlockEntity blockEntity, int packedLight) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return packedLight;
        }
        BlockState state = blockEntity.getBlockState();
        if (state.getBlock() instanceof MultiblockCoreBlock coreBlock) {
            BlockPos pos = blockEntity.getBlockPos();
            LegacyMultiblockLayout layout = coreBlock.getMultiblockLayout(state, level, pos);
            if (layout != null) {
                return resolveBoundsLight(blockEntity, layout.structureBoundingBox(pos, 0.0D), packedLight);
            }
        }
        return resolveBlockEntityLight(blockEntity, packedLight);
    }

    public static int resolveMachineLight(BlockEntity blockEntity, BlockState state,
            LegacyMachineDefinition definition, int packedLight) {
        return resolveBoundsLight(blockEntity, definition.lightingBoundingBox(state, blockEntity.getBlockPos()),
                packedLight);
    }

    public static int resolveBoundsLight(BlockEntity blockEntity, AABB bounds, int packedLight) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return packedLight;
        }

        BlockPos pos = blockEntity.getBlockPos();
        double centerX = (bounds.minX + bounds.maxX) * 0.5D;
        double centerY = (bounds.minY + bounds.maxY) * 0.5D;
        double centerZ = (bounds.minZ + bounds.maxZ) * 0.5D;
        double sampleTopY = insideMax(bounds.maxY);

        return brightest(packedLight,
                LevelRenderer.getLightColor(level, pos),
                LevelRenderer.getLightColor(level, pos.above()),
                LevelRenderer.getLightColor(level, BlockPos.containing(centerX, bounds.minY, centerZ)),
                LevelRenderer.getLightColor(level, BlockPos.containing(centerX, centerY, centerZ)),
                LevelRenderer.getLightColor(level, BlockPos.containing(centerX, sampleTopY, centerZ)),
                LevelRenderer.getLightColor(level, BlockPos.containing(bounds.minX, sampleTopY, bounds.minZ)),
                LevelRenderer.getLightColor(level, BlockPos.containing(insideMax(bounds.maxX), sampleTopY, bounds.minZ)),
                LevelRenderer.getLightColor(level, BlockPos.containing(bounds.minX, sampleTopY, insideMax(bounds.maxZ))),
                LevelRenderer.getLightColor(level, BlockPos.containing(insideMax(bounds.maxX), sampleTopY, insideMax(bounds.maxZ))));
    }

    private static double insideMax(double max) {
        return Math.nextAfter(max, Double.NEGATIVE_INFINITY) - INSIDE_EPSILON;
    }

    private static int brightest(int first, int... candidates) {
        int sky = LightTexture.sky(first);
        int block = LightTexture.block(first);
        for (int candidate : candidates) {
            sky = Math.max(sky, LightTexture.sky(candidate));
            block = Math.max(block, LightTexture.block(candidate));
        }
        return LightTexture.pack(block, sky);
    }

    private LegacyRenderLighting() {
    }
}
