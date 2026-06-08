package com.hbm.ntm.client.obj;

import net.minecraft.resources.ResourceLocation;

public final class LegacyTexturedLineRenderer {
    public static final double PYLON_WIRE_GIRTH = 0.03125D;
    public static final double PYLON_WIRE_U_WRAP_PER_BLOCK = 8.0D;

    public static void pylonLineSegment(ResourceLocation texture, ObjRenderContext context,
            double x0, double y0, double z0, double x1, double y1, double z1) {
        pylonLineSegment(texture, context, x0, y0, z0, x1, y1, z1, PYLON_WIRE_GIRTH);
    }

    public static void pylonLineSegment(ResourceLocation texture, ObjRenderContext context,
            double x0, double y0, double z0, double x1, double y1, double z1, int color) {
        pylonLineSegment(texture, context.withColor(color, context.alpha()), x0, y0, z0, x1, y1, z1);
    }

    public static void pylonLineSegment(ResourceLocation texture, ObjRenderContext context,
            double x0, double y0, double z0, double x1, double y1, double z1, double girth) {
        double deltaX = x0 - x1;
        double deltaY = y0 - y1;
        double deltaZ = z0 - z1;
        double horizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double yaw = Math.atan2(deltaX, deltaZ);
        double pitch = Math.atan2(deltaY, horizontal);
        double newPitch = pitch + Math.PI * 0.5D;
        double newYaw = yaw + Math.PI * 0.5D;
        double iZ = Math.cos(yaw) * Math.cos(newPitch) * girth;
        double iX = Math.sin(yaw) * Math.cos(newPitch) * girth;
        double iY = Math.sin(newPitch) * girth;
        double jZ = Math.cos(newYaw) * girth;
        double jX = Math.sin(newYaw) * girth;
        wrappedLineSegment(texture, context, x0, y0, z0, x1, y1, z1, iX, iY, iZ, jX, jZ, PYLON_WIRE_U_WRAP_PER_BLOCK);
    }

    public static void wrappedLineSegment(ResourceLocation texture, ObjRenderContext context,
            double x0, double y0, double z0, double x1, double y1, double z1,
            double iX, double iY, double iZ, double jX, double jZ) {
        wrappedLineSegment(texture, context, x0, y0, z0, x1, y1, z1, iX, iY, iZ, jX, jZ, PYLON_WIRE_U_WRAP_PER_BLOCK);
    }

    public static void wrappedLineSegment(ResourceLocation texture, ObjRenderContext context,
            double x0, double y0, double z0, double x1, double y1, double z1,
            double iX, double iY, double iZ, double jX, double jZ, double uWrapPerBlock) {
        double deltaX = x1 - x0;
        double deltaY = y1 - y0;
        double deltaZ = z1 - z0;
        double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        double wrap = Math.ceil(length * uWrapPerBlock);

        if (deltaX + deltaZ < 0.0D) {
            wrap *= -1.0D;
            jX *= -1.0D;
            jZ *= -1.0D;
        }

        LegacyTexturedQuadRenderer.quadWithComputedNormal(texture, context,
                LegacyTexturedQuadRenderer.vertex(x0 + iX, y0 + iY, z0 + iZ, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.vertex(x0 - iX, y0 - iY, z0 - iZ, 0.0D, 1.0D),
                LegacyTexturedQuadRenderer.vertex(x1 - iX, y1 - iY, z1 - iZ, wrap, 1.0D),
                LegacyTexturedQuadRenderer.vertex(x1 + iX, y1 + iY, z1 + iZ, wrap, 0.0D));
        LegacyTexturedQuadRenderer.quadWithComputedNormal(texture, context,
                LegacyTexturedQuadRenderer.vertex(x0 + jX, y0, z0 + jZ, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.vertex(x0 - jX, y0, z0 - jZ, 0.0D, 1.0D),
                LegacyTexturedQuadRenderer.vertex(x1 - jX, y1, z1 - jZ, wrap, 1.0D),
                LegacyTexturedQuadRenderer.vertex(x1 + jX, y1, z1 + jZ, wrap, 0.0D));
    }

    private LegacyTexturedLineRenderer() {
    }
}
