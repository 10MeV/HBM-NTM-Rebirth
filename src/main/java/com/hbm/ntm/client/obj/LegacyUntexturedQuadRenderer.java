package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public final class LegacyUntexturedQuadRenderer {
    private static final RenderStateShard.TransparencyStateShard LIGHTNING_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard("hbm_legacy_lightning_transparency",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    });
    private static final RenderStateShard.TransparencyStateShard NORMAL_ALPHA_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard("hbm_legacy_untextured_alpha_transparency",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                GlStateManager.SourceFactor.ONE,
                                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    });

    private static final RenderType LEGACY_ADDITIVE_NO_CULL = RenderType.create(
            "hbm_legacy_additive_no_cull",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLightningShader))
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .createCompositeState(false));

    private static final RenderType LEGACY_ADDITIVE_DEPTH_WRITE_NO_CULL = RenderType.create(
            "hbm_legacy_additive_depth_write_no_cull",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLightningShader))
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, true))
                    .createCompositeState(false));

    private static final RenderType LEGACY_TRANSLUCENT_NO_CULL = RenderType.create(
            "hbm_legacy_translucent_no_cull",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                    .setTransparencyState(NORMAL_ALPHA_TRANSPARENCY)
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .createCompositeState(false));

    private static final RenderType LEGACY_TRANSLUCENT_DEPTH_WRITE_NO_CULL = RenderType.create(
            "hbm_legacy_translucent_depth_write_no_cull",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                    .setTransparencyState(NORMAL_ALPHA_TRANSPARENCY)
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, true))
                    .createCompositeState(false));

    private static final RenderType LEGACY_SOLID_NO_CULL = RenderType.create(
            "hbm_legacy_solid_no_cull",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, true))
                    .createCompositeState(false));

    public static VertexConsumer lightning(MultiBufferSource buffer) {
        return buffer.getBuffer(LEGACY_ADDITIVE_NO_CULL);
    }

    public static VertexConsumer solid(MultiBufferSource buffer) {
        return buffer.getBuffer(LEGACY_SOLID_NO_CULL);
    }

    public static VertexConsumer translucent(MultiBufferSource buffer) {
        return buffer.getBuffer(LEGACY_TRANSLUCENT_NO_CULL);
    }

    public static VertexConsumer consumer(MultiBufferSource buffer, LegacyTexturedRenderMode renderMode, int alpha) {
        return buffer.getBuffer(type(renderMode, alpha));
    }

    public static VertexConsumer consumer(ObjRenderContext context) {
        return consumer(context.buffer(), context.renderMode(), context.alpha());
    }

    public static VertexConsumer consumer(ObjRenderContext context, int... vertexAlphas) {
        return consumer(context.buffer(), context.renderMode(), minimumMultipliedAlpha(context, vertexAlphas));
    }

    public static RenderType additiveNoCullType() {
        return LEGACY_ADDITIVE_NO_CULL;
    }

    public static RenderType additiveDepthWriteNoCullType() {
        return LEGACY_ADDITIVE_DEPTH_WRITE_NO_CULL;
    }

    public static RenderType translucentNoCullType() {
        return LEGACY_TRANSLUCENT_NO_CULL;
    }

    public static RenderType translucentDepthWriteNoCullType() {
        return LEGACY_TRANSLUCENT_DEPTH_WRITE_NO_CULL;
    }

    public static RenderType solidNoCullType() {
        return LEGACY_SOLID_NO_CULL;
    }

    public static RenderType type(boolean additive, int alpha) {
        if (additive) {
            return LEGACY_ADDITIVE_NO_CULL;
        }
        return alpha < 255 ? LEGACY_TRANSLUCENT_NO_CULL : LEGACY_SOLID_NO_CULL;
    }

    public static RenderType type(LegacyTexturedRenderMode renderMode, int alpha) {
        return switch (renderMode.withAlpha(alpha)) {
            case ADDITIVE_DEPTH_WRITE -> LEGACY_ADDITIVE_DEPTH_WRITE_NO_CULL;
            case ADDITIVE_NO_DEPTH_WRITE -> LEGACY_ADDITIVE_NO_CULL;
            case TRANSLUCENT_DEPTH_WRITE -> LEGACY_TRANSLUCENT_DEPTH_WRITE_NO_CULL;
            case TRANSLUCENT, TRANSLUCENT_NO_DEPTH_WRITE -> LEGACY_TRANSLUCENT_NO_CULL;
            case CUTOUT_NO_CULL -> alpha < 255 ? LEGACY_TRANSLUCENT_NO_CULL : LEGACY_SOLID_NO_CULL;
            case GLINT_NO_DEPTH_WRITE, GLINT_EQUAL_DEPTH -> LEGACY_ADDITIVE_NO_CULL;
        };
    }

    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z,
                              int red, int green, int blue, int alpha) {
        consumer.vertex(pose.pose(), (float) x, (float) y, (float) z)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z,
            int color, int alpha) {
        vertex(consumer, pose, x, y, z, color >> 16 & 255, color >> 8 & 255, color & 255, alpha);
    }

    public static void vertexRgbaF(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z,
            float red, float green, float blue, float alpha) {
        vertex(consumer, pose, x, y, z, rgb(red, green, blue), alpha(alpha));
    }

    public static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                            double x0, double y0, double z0,
                            double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            double x3, double y3, double z3,
                            int red, int green, int blue,
                            int alpha0, int alpha1, int alpha2, int alpha3) {
        vertex(consumer, pose, x0, y0, z0, red, green, blue, alpha0);
        vertex(consumer, pose, x1, y1, z1, red, green, blue, alpha1);
        vertex(consumer, pose, x2, y2, z2, red, green, blue, alpha2);
        vertex(consumer, pose, x3, y3, z3, red, green, blue, alpha3);
    }

    public static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                            double x0, double y0, double z0,
                            double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            double x3, double y3, double z3,
                            int color,
                            int alpha0, int alpha1, int alpha2, int alpha3) {
        quad(consumer, pose, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                color >> 16 & 255, color >> 8 & 255, color & 255, alpha0, alpha1, alpha2, alpha3);
    }

    public static void quad(ObjRenderContext context,
                            double x0, double y0, double z0,
                            double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            double x3, double y3, double z3,
                            int color,
                            int alpha0, int alpha1, int alpha2, int alpha3) {
        quad(consumer(context, alpha0, alpha1, alpha2, alpha3), context.poseStack().last(),
                x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                multipliedColor(context, color), multipliedAlpha(context, alpha0), multipliedAlpha(context, alpha1),
                multipliedAlpha(context, alpha2), multipliedAlpha(context, alpha3));
    }

    public static void quadRgbaF(ObjRenderContext context,
                            double x0, double y0, double z0,
                            double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            double x3, double y3, double z3,
                            float red, float green, float blue,
                            float alpha0, float alpha1, float alpha2, float alpha3) {
        quad(context, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                rgb(red, green, blue), alpha(alpha0), alpha(alpha1), alpha(alpha2), alpha(alpha3));
    }

    public static void doubleSidedQuad(VertexConsumer consumer, PoseStack.Pose pose,
                                       double x0, double y0, double z0,
                                       double x1, double y1, double z1,
                                       double x2, double y2, double z2,
                                       double x3, double y3, double z3,
                                       int red, int green, int blue,
                                       int alpha0, int alpha1, int alpha2, int alpha3) {
        quad(consumer, pose, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, red, green, blue, alpha0, alpha1, alpha2, alpha3);
        quad(consumer, pose, x3, y3, z3, x2, y2, z2, x1, y1, z1, x0, y0, z0, red, green, blue, alpha3, alpha2, alpha1, alpha0);
    }

    public static void doubleSidedQuad(ObjRenderContext context,
                                       double x0, double y0, double z0,
                                       double x1, double y1, double z1,
                                       double x2, double y2, double z2,
                                       double x3, double y3, double z3,
                                       int color,
                                       int alpha0, int alpha1, int alpha2, int alpha3) {
        VertexConsumer consumer = consumer(context, alpha0, alpha1, alpha2, alpha3);
        PoseStack.Pose pose = context.poseStack().last();
        int multipliedColor = multipliedColor(context, color);
        doubleSidedQuad(consumer, pose, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                multipliedColor >> 16 & 255, multipliedColor >> 8 & 255, multipliedColor & 255,
                multipliedAlpha(context, alpha0), multipliedAlpha(context, alpha1),
                multipliedAlpha(context, alpha2), multipliedAlpha(context, alpha3));
    }

    public static void doubleSidedQuadRgbaF(ObjRenderContext context,
                                       double x0, double y0, double z0,
                                       double x1, double y1, double z1,
                                       double x2, double y2, double z2,
                                       double x3, double y3, double z3,
                                       float red, float green, float blue,
                                       float alpha0, float alpha1, float alpha2, float alpha3) {
        doubleSidedQuad(context, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                rgb(red, green, blue), alpha(alpha0), alpha(alpha1), alpha(alpha2), alpha(alpha3));
    }

    private static int multipliedColor(ObjRenderContext context, int color) {
        if (!context.hasColor()) {
            return color & 0xFFFFFF;
        }
        int contextColor = context.color();
        int red = (contextColor >> 16 & 255) * (color >> 16 & 255) / 255;
        int green = (contextColor >> 8 & 255) * (color >> 8 & 255) / 255;
        int blue = (contextColor & 255) * (color & 255) / 255;
        return red << 16 | green << 8 | blue;
    }

    private static int multipliedAlpha(ObjRenderContext context, int alpha) {
        return clampAlpha(context.alpha() * clampAlpha(alpha) / 255);
    }

    private static int minimumMultipliedAlpha(ObjRenderContext context, int... vertexAlphas) {
        int alpha = context.alpha();
        for (int vertexAlpha : vertexAlphas) {
            alpha = Math.min(alpha, multipliedAlpha(context, vertexAlpha));
        }
        return alpha;
    }

    private static int clampAlpha(int alpha) {
        return Math.max(0, Math.min(255, alpha));
    }

    private static int clampColor(float value) {
        return clampAlpha(Math.round(value * 255.0F));
    }

    public static int alpha(float alpha) {
        return clampColor(alpha);
    }

    public static int rgb(float red, float green, float blue) {
        return clampColor(red) << 16 | clampColor(green) << 8 | clampColor(blue);
    }

    private LegacyUntexturedQuadRenderer() {
    }
}
