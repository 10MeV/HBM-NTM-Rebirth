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

    static HbmRenderFrameCulling.MachineRendererSubmissionScope recordMachineSubmissionScope(BlockEntity blockEntity) {
        recordMachineSubmission(blockEntity);
        return HbmRenderFrameCulling.pushMachineRendererSubmissionScope(blockEntity);
    }

    static HbmRenderFrameCulling.MachineRendererSubmissionScope animatedModelFadeScope(BlockEntity blockEntity) {
        float fade = blockEntity == null ? 1.0F : HbmRenderFrameCulling.animatedModelFade(blockEntity.getBlockPos());
        return HbmRenderFrameCulling.pushMachineRendererSubmissionScope(blockEntity, fade);
    }
}
