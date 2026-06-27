package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public final class LegacyAssemblySparkRenderer {
    public static final double WIDE = 0.1875D;
    public static final double NARROW = 0.0D;
    public static final double LENGTH = 1.25D;
    public static final double EPSILON = 0.01D;
    public static final double MIRRORED_U_OFFSET = 0.5D;

    public static void renderPlan(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTileRenderPlans.AssemblySparkRenderPlan plan) {
        if (!plan.active()) {
            return;
        }
        LegacyTexturedRenderMode renderMode = plan.blend() == null
                ? LegacyTexturedRenderMode.CUTOUT_NO_CULL
                : plan.blend().modernRenderMode();
        int resolvedLight = plan.fullbright() == null
                ? packedLight
                : LegacyTexturedQuadRenderer.legacyLightmap(plan.fullbright().lightmapX(),
                        plan.fullbright().lightmapY());
        for (LegacyTileRenderPlans.AssemblySparkBladePlan blade : plan.blades()) {
            poseStack.pushPose();
            poseStack.translate(blade.translateX(), blade.translateY(), blade.translateZ());
            for (LegacyTileRenderPlans.TexturedQuadPlan quad : blade.quads()) {
                LegacyTexturedQuadRenderer.quad(texture, poseStack, buffer, resolvedLight, packedOverlay,
                        renderMode, 0.0F, 1.0F, 0.0F,
                        vertex(quad.vertices().get(0)), vertex(quad.vertices().get(1)),
                        vertex(quad.vertices().get(2)), vertex(quad.vertices().get(3)));
            }
            poseStack.popPose();
        }
    }

    private static LegacyTexturedQuadRenderer.Vertex vertex(LegacyTileRenderPlans.QuadVertexPlan vertex) {
        return LegacyTexturedQuadRenderer.vertex(vertex.x(), vertex.y(), vertex.z(),
                vertex.u(), vertex.v(), vertex.color(), vertex.alpha());
    }

    private LegacyAssemblySparkRenderer() {
    }
}
