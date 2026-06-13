package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;

public final class LegacyAssemblySparkRenderer {
    public static final double WIDE = 0.1875D;
    public static final double NARROW = 0.0D;
    public static final double LENGTH = 1.25D;
    public static final double EPSILON = 0.01D;
    public static final double MIRRORED_U_OFFSET = 0.5D;

    public static void renderSparkPair(ResourceLocation texture, ObjRenderContext context,
            LegacyUvAnimation.Range u, double length) {
        renderSparkPair(texture, context, u.min(), u.max(), length);
    }

    public static void renderSparkPair(ResourceLocation texture, ObjRenderContext context,
            double uMin, double uMax, double length) {
        LegacyTexturedQuadRenderer.quad(texture, context,
                LegacyTexturedQuadRenderer.vertex(-EPSILON, -WIDE, length,
                        uMin + MIRRORED_U_OFFSET, 0.0D, 0xFFFFFF, 0),
                LegacyTexturedQuadRenderer.vertex(-EPSILON, WIDE, length,
                        uMin + MIRRORED_U_OFFSET, 1.0D, 0xFFFFFF, 0),
                LegacyTexturedQuadRenderer.vertex(-EPSILON, NARROW, 0.0D,
                        uMax + MIRRORED_U_OFFSET, 1.0D, 0xFFFFFF, 255),
                LegacyTexturedQuadRenderer.vertex(-EPSILON, -NARROW, 0.0D,
                        uMax + MIRRORED_U_OFFSET, 0.0D, 0xFFFFFF, 255));
        LegacyTexturedQuadRenderer.quad(texture, context,
                LegacyTexturedQuadRenderer.vertex(EPSILON, -WIDE, length, uMin, 1.0D, 0xFFFFFF, 0),
                LegacyTexturedQuadRenderer.vertex(EPSILON, WIDE, length, uMin, 0.0D, 0xFFFFFF, 0),
                LegacyTexturedQuadRenderer.vertex(EPSILON, NARROW, 0.0D, uMax, 0.0D, 0xFFFFFF, 255),
                LegacyTexturedQuadRenderer.vertex(EPSILON, -NARROW, 0.0D, uMax, 1.0D, 0xFFFFFF, 255));
    }

    public static void renderPlan(ResourceLocation texture, ObjRenderContext context,
            LegacyTileRenderPlans.AssemblySparkRenderPlan plan) {
        if (!plan.active()) {
            return;
        }
        ObjRenderContext resolved = context;
        if (plan.blend() != null) {
            resolved = resolved.withRenderMode(plan.blend().modernRenderMode());
        }
        if (plan.fullbright() != null) {
            resolved = resolved.withLegacyLightmap(plan.fullbright().lightmapX(), plan.fullbright().lightmapY());
        }
        PoseStack poseStack = context.poseStack();
        for (LegacyTileRenderPlans.AssemblySparkBladePlan blade : plan.blades()) {
            poseStack.pushPose();
            poseStack.translate(blade.translateX(), blade.translateY(), blade.translateZ());
            for (LegacyTileRenderPlans.TexturedQuadPlan quad : blade.quads()) {
                LegacyTexturedQuadRenderer.quad(texture, resolved,
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
