package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.culling.HbmRenderFrameCulling;
import com.hbm.ntm.util.HbmModelRenderDistances;
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
        // The argument remains to keep renderer call sites stable, but model culling is a
        // global 512-block contract rather than a renderer-local override.
        return HbmRenderFrameCulling.shouldRender(blockEntity, bounds, HbmModelRenderDistances.SQUARED_BLOCKS);
    }

    static void recordMachineSubmission(BlockEntity blockEntity) {
        HbmRenderFrameCulling.recordMachineRendererSubmission(blockEntity, 0);
    }

    static HbmRenderFrameCulling.MachineRendererSubmissionScope recordMachineSubmissionScope(BlockEntity blockEntity) {
        recordMachineSubmission(blockEntity);
        return HbmRenderFrameCulling.pushMachineRendererSubmissionScope(blockEntity);
    }

    static HbmRenderFrameCulling.MachineRendererSubmissionScope animatedModelFadeScope(BlockEntity blockEntity) {
        return HbmRenderFrameCulling.pushAnimatedMachineRendererSubmissionScope(blockEntity);
    }

    static float currentStaticModelFade() {
        return HbmRenderFrameCulling.currentStaticModelFade();
    }

    static int fadedStaticAlpha(int alpha) {
        float fade = currentStaticModelFade();
        if (fade < 0.0F) {
            return 0;
        }
        if (fade >= 1.0F) {
            return alpha;
        }
        return Math.max(0, Math.min(255, Math.round(alpha * fade)));
    }
}
