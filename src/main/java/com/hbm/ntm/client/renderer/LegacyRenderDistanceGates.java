package com.hbm.ntm.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;

final class LegacyRenderDistanceGates {
    private LegacyRenderDistanceGates() {
    }

    static boolean isPlayerWithin(BlockEntity blockEntity, double yOffset, double range) {
        return isPlayerWithinOr(blockEntity, yOffset, range, false);
    }

    static boolean isPlayerWithinOr(BlockEntity blockEntity, double yOffset, double range,
            boolean missingPlayerFallback) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return missingPlayerFallback;
        }
        return minecraft.player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + yOffset,
                blockEntity.getBlockPos().getZ() + 0.5D) < range * range;
    }
}
