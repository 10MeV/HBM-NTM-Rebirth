package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class LegacyRenderLighting {
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

    public static int resolveMachineLight(BlockEntity blockEntity, BlockState state,
            LegacyMachineDefinition definition, int packedLight) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return packedLight;
        }

        BlockPos pos = blockEntity.getBlockPos();
        AABB bounds = definition.renderBoundingBox(state, pos);
        double centerX = (bounds.minX + bounds.maxX) * 0.5D;
        double centerZ = (bounds.minZ + bounds.maxZ) * 0.5D;
        BlockPos topCenter = BlockPos.containing(centerX, bounds.maxY + 0.05D, centerZ);

        return brightest(packedLight,
                LevelRenderer.getLightColor(level, pos),
                LevelRenderer.getLightColor(level, pos.above()),
                LevelRenderer.getLightColor(level, topCenter),
                LevelRenderer.getLightColor(level, BlockPos.containing(bounds.minX, bounds.maxY + 0.05D, bounds.minZ)),
                LevelRenderer.getLightColor(level, BlockPos.containing(bounds.maxX, bounds.maxY + 0.05D, bounds.minZ)),
                LevelRenderer.getLightColor(level, BlockPos.containing(bounds.minX, bounds.maxY + 0.05D, bounds.maxZ)),
                LevelRenderer.getLightColor(level, BlockPos.containing(bounds.maxX, bounds.maxY + 0.05D, bounds.maxZ)));
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
