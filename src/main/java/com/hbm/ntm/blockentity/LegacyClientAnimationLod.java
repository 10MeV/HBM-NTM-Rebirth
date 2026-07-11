package com.hbm.ntm.blockentity;

import com.hbm.ntm.config.HbmClientConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

final class LegacyClientAnimationLod {
    private LegacyClientAnimationLod() {
    }

    static boolean shouldSkipAnimationUpdate(Level level, BlockPos pos) {
        if (level == null || pos == null || !level.isClientSide) {
            return false;
        }
        int maxBlocks = HbmClientConfig.modelUpdateDistanceBlocks();
        if (maxBlocks <= 0) {
            return false;
        }
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        double maxDistanceSq = (double) maxBlocks * (double) maxBlocks;
        for (Player player : level.players()) {
            if (player != null && player.distanceToSqr(x, y, z) <= maxDistanceSq) {
                return false;
            }
        }
        return true;
    }
}
