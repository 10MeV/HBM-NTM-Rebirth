package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachinePartRenderProperties;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import java.util.List;

final class LegacyMachinePartBatchRenderer {
    private LegacyMachinePartBatchRenderer() {
    }

    static void renderRuns(List<LegacyMachinePartRenderSelection.Run> runs, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode defaultRenderMode) {
        for (LegacyMachinePartRenderSelection.Run run : runs) {
            renderRun(run, model, poseStack, buffer, packedLight, packedOverlay, defaultRenderMode);
        }
    }

    private static void renderRun(LegacyMachinePartRenderSelection.Run run, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode defaultRenderMode) {
        List<LegacyMachinePartRenderSelection.Entry> parts = run.entries();
        LegacyMachinePartRenderSelection.Entry first = parts.get(0);
        LegacyMachinePartRenderProperties properties = first.properties();
        LegacyTexturedRenderMode renderMode = properties == null ? defaultRenderMode
                : LegacyMachinePartRenderContexts.renderMode(properties.mode());
        int color = properties != null && properties.hasColor() ? properties.color() : 0xFFFFFF;
        int alpha = properties == null ? 255 : properties.alpha();
        int light = properties != null && properties.fullBright() ? LightTexture.FULL_BRIGHT : packedLight;
        model.renderOnlyInCallOrder(first.texture(), poseStack, buffer, light, packedOverlay,
                color >> 16 & 255, color >> 8 & 255, color & 255, alpha, false, renderMode,
                LegacyWavefrontModel.UvTransform.DEFAULT, run.selectionHandle(model));
    }
}
