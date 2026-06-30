package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.culling.HbmRenderFrameCulling;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

final class LegacyBlockEntityRenderCulling {
    private LegacyBlockEntityRenderCulling() {
    }

    static boolean shouldRenderMachine(BlockEntity blockEntity, int viewDistance) {
        if (blockEntity == null) {
            return false;
        }
        AABB bounds = blockEntity.getRenderBoundingBox();
        double maxDistanceSq = (double) viewDistance * (double) viewDistance;
        return HbmRenderFrameCulling.shouldRender(blockEntity, bounds, maxDistanceSq);
    }

    static void recordMachineSubmission(BlockEntity blockEntity) {
        HbmRenderFrameCulling.recordMachineRendererSubmission(blockEntity, 0);
    }
}
