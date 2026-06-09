package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.hbm.ntm.client.obj.ObjRenderContext;
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

    private LegacyAssemblySparkRenderer() {
    }
}
