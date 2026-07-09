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
import org.joml.Matrix4f;

public final class LegacyUntexturedQuadRenderer {
    private static final int LEGACY_EFFECT_BUFFER_SIZE = 262_144;
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
            LEGACY_EFFECT_BUFFER_SIZE,
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
            LEGACY_EFFECT_BUFFER_SIZE,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLightningShader))
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, true))
                    .createCompositeState(false));

    private static final RenderType LEGACY_ADDITIVE_CULL = RenderType.create(
            "hbm_legacy_additive_cull",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            LEGACY_EFFECT_BUFFER_SIZE,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLightningShader))
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setCullState(new RenderStateShard.CullStateShard(true))
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .createCompositeState(false));

    private static final RenderType LEGACY_TRANSLUCENT_NO_CULL = RenderType.create(
            "hbm_legacy_translucent_no_cull",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            LEGACY_EFFECT_BUFFER_SIZE,
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
            LEGACY_EFFECT_BUFFER_SIZE,
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
            LEGACY_EFFECT_BUFFER_SIZE,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, true))
                    .createCompositeState(false));

    private static final RenderType LEGACY_SOLID_CULL = RenderType.create(
            "hbm_legacy_solid_cull",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            LEGACY_EFFECT_BUFFER_SIZE,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                    .setCullState(new RenderStateShard.CullStateShard(true))
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, true))
                    .createCompositeState(false));

    private static final RenderType LEGACY_ADDITIVE_NO_CULL_TRIANGLES = createType(
            "hbm_legacy_additive_no_cull_triangles",
            new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLightningShader),
            LIGHTNING_TRANSPARENCY,
            false,
            true,
            VertexFormat.Mode.TRIANGLES);
    private static final RenderType LEGACY_ADDITIVE_DEPTH_WRITE_NO_CULL_TRIANGLES = createType(
            "hbm_legacy_additive_depth_write_no_cull_triangles",
            new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLightningShader),
            LIGHTNING_TRANSPARENCY,
            true,
            true,
            VertexFormat.Mode.TRIANGLES);
    private static final RenderType LEGACY_ADDITIVE_CULL_TRIANGLES = createType(
            "hbm_legacy_additive_cull_triangles",
            new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLightningShader),
            LIGHTNING_TRANSPARENCY,
            false,
            true,
            VertexFormat.Mode.TRIANGLES,
            true);
    private static final RenderType LEGACY_TRANSLUCENT_NO_CULL_TRIANGLES = createType(
            "hbm_legacy_translucent_no_cull_triangles",
            new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader),
            NORMAL_ALPHA_TRANSPARENCY,
            false,
            true,
            VertexFormat.Mode.TRIANGLES);
    private static final RenderType LEGACY_TRANSLUCENT_DEPTH_WRITE_NO_CULL_TRIANGLES = createType(
            "hbm_legacy_translucent_depth_write_no_cull_triangles",
            new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader),
            NORMAL_ALPHA_TRANSPARENCY,
            true,
            true,
            VertexFormat.Mode.TRIANGLES);
    private static final RenderType LEGACY_SOLID_NO_CULL_TRIANGLES = createType(
            "hbm_legacy_solid_no_cull_triangles",
            new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader),
            null,
            true,
            false,
            VertexFormat.Mode.TRIANGLES);
    private static final RenderType LEGACY_SOLID_CULL_TRIANGLES = createType(
            "hbm_legacy_solid_cull_triangles",
            new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader),
            null,
            true,
            false,
            VertexFormat.Mode.TRIANGLES,
            true);

    public static VertexConsumer lightning(MultiBufferSource buffer) {
        return buffer.getBuffer(LEGACY_ADDITIVE_NO_CULL);
    }

    public static VertexConsumer additiveNoCull(MultiBufferSource buffer) {
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

    public static RenderType solidCullType() {
        return LEGACY_SOLID_CULL;
    }

    public static RenderType type(boolean additive, int alpha) {
        return type(additive, alpha, VertexFormat.Mode.QUADS);
    }

    public static RenderType type(boolean additive, int alpha, VertexFormat.Mode drawMode) {
        boolean triangles = drawMode == VertexFormat.Mode.TRIANGLES;
        if (additive) {
            return triangles ? LEGACY_ADDITIVE_NO_CULL_TRIANGLES : LEGACY_ADDITIVE_NO_CULL;
        }
        return triangles ? LEGACY_SOLID_NO_CULL_TRIANGLES : LEGACY_SOLID_NO_CULL;
    }

    public static RenderType type(LegacyTexturedRenderMode renderMode, int alpha) {
        return type(renderMode, alpha, VertexFormat.Mode.QUADS);
    }

    public static RenderType type(LegacyTexturedRenderMode renderMode, int alpha, VertexFormat.Mode drawMode) {
        boolean triangles = drawMode == VertexFormat.Mode.TRIANGLES;
        return switch (renderMode.withAlpha(alpha)) {
            case ADDITIVE_DEPTH_WRITE -> triangles ? LEGACY_ADDITIVE_DEPTH_WRITE_NO_CULL_TRIANGLES : LEGACY_ADDITIVE_DEPTH_WRITE_NO_CULL;
            case ADDITIVE_CULL_NO_DEPTH_WRITE -> triangles ? LEGACY_ADDITIVE_CULL_TRIANGLES : LEGACY_ADDITIVE_CULL;
            case ADDITIVE_NO_DEPTH_WRITE -> triangles ? LEGACY_ADDITIVE_NO_CULL_TRIANGLES : LEGACY_ADDITIVE_NO_CULL;
            case TRANSLUCENT_DEPTH_WRITE -> triangles ? LEGACY_TRANSLUCENT_DEPTH_WRITE_NO_CULL_TRIANGLES : LEGACY_TRANSLUCENT_DEPTH_WRITE_NO_CULL;
            case TRANSLUCENT, TRANSLUCENT_NO_DEPTH_WRITE -> triangles ? LEGACY_TRANSLUCENT_NO_CULL_TRIANGLES : LEGACY_TRANSLUCENT_NO_CULL;
            case CUTOUT_REVERSED_CULL, CUTOUT_CULL -> triangles ? LEGACY_SOLID_CULL_TRIANGLES : LEGACY_SOLID_CULL;
            case CUTOUT_NO_CULL, CUTOUT_DOUBLE_SIDED -> triangles ? LEGACY_SOLID_NO_CULL_TRIANGLES : LEGACY_SOLID_NO_CULL;
            case GLINT_NO_DEPTH_WRITE, GLINT_EQUAL_DEPTH -> triangles ? LEGACY_ADDITIVE_NO_CULL_TRIANGLES : LEGACY_ADDITIVE_NO_CULL;
        };
    }

    private static RenderType createType(String name, RenderStateShard.ShaderStateShard shader,
            RenderStateShard.TransparencyStateShard transparency, boolean depthWrite, boolean sortOnUpload,
            VertexFormat.Mode drawMode) {
        return createType(name, shader, transparency, depthWrite, sortOnUpload, drawMode, false);
    }

    private static RenderType createType(String name, RenderStateShard.ShaderStateShard shader,
            RenderStateShard.TransparencyStateShard transparency, boolean depthWrite, boolean sortOnUpload,
            VertexFormat.Mode drawMode, boolean cull) {
        RenderType.CompositeState.CompositeStateBuilder builder = RenderType.CompositeState.builder()
                .setShaderState(shader)
                .setCullState(new RenderStateShard.CullStateShard(cull))
                .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, depthWrite));
        if (transparency != null) {
            builder.setTransparencyState(transparency);
        }
        return RenderType.create(name, DefaultVertexFormat.POSITION_COLOR, drawMode, LEGACY_EFFECT_BUFFER_SIZE, false, sortOnUpload,
                builder.createCompositeState(false));
    }

    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z,
                              int red, int green, int blue, int alpha) {
        vertex(consumer, pose.pose(), x, y, z, red, green, blue, alpha);
    }

    public static void vertex(VertexConsumer consumer, Matrix4f pose, double x, double y, double z,
                              int red, int green, int blue, int alpha) {
        consumer.vertex(pose, (float) x, (float) y, (float) z)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z,
            int color, int alpha) {
        vertex(consumer, pose, x, y, z, color >> 16 & 255, color >> 8 & 255, color & 255, alpha);
    }

    public static void vertex(VertexConsumer consumer, Matrix4f pose, double x, double y, double z,
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
        quad(consumer, pose.pose(), x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                red, green, blue, alpha0, alpha1, alpha2, alpha3);
    }

    public static void quad(VertexConsumer consumer, Matrix4f pose,
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
        quad(consumer, pose.pose(), x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                color, alpha0, alpha1, alpha2, alpha3);
    }

    public static void quad(VertexConsumer consumer, Matrix4f pose,
                            double x0, double y0, double z0,
                            double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            double x3, double y3, double z3,
                            int color,
                            int alpha0, int alpha1, int alpha2, int alpha3) {
        quad(consumer, pose, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                color >> 16 & 255, color >> 8 & 255, color & 255, alpha0, alpha1, alpha2, alpha3);
    }

    public static void quad(PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                            double x0, double y0, double z0,
                            double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            double x3, double y3, double z3,
                            int color,
                            int alpha0, int alpha1, int alpha2, int alpha3) {
        if (alpha0 == alpha1 && alpha0 == alpha2 && alpha0 == alpha3
                && LegacyWavefrontModel.renderUntexturedTransientQuad(poseStack, buffer, renderMode,
                x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, color, clampAlpha(alpha0))) {
            return;
        }
        if (LegacyWavefrontModel.renderUntexturedVertexColorTransientQuad(poseStack, buffer, renderMode,
                x0, y0, z0, color, clampAlpha(alpha0),
                x1, y1, z1, color, clampAlpha(alpha1),
                x2, y2, z2, color, clampAlpha(alpha2),
                x3, y3, z3, color, clampAlpha(alpha3))) {
            return;
        }
        quad(consumer(buffer, renderMode, minimumAlpha(alpha0, alpha1, alpha2, alpha3)), poseStack.last(),
                x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                color, clampAlpha(alpha0), clampAlpha(alpha1), clampAlpha(alpha2), clampAlpha(alpha3));
    }

    public static void quadRgbaF(PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                            double x0, double y0, double z0,
                            double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            double x3, double y3, double z3,
                            float red, float green, float blue,
                            float alpha0, float alpha1, float alpha2, float alpha3) {
        quad(poseStack, buffer, renderMode, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                rgb(red, green, blue), alpha(alpha0), alpha(alpha1), alpha(alpha2), alpha(alpha3));
    }

    public static QuadBatch quadBatch(PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode) {
        return new QuadBatch(poseStack, buffer, renderMode);
    }

    public static void quad(QuadBatch batch,
                            double x0, double y0, double z0,
                            double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            double x3, double y3, double z3,
                            int color,
                            int alpha0, int alpha1, int alpha2, int alpha3) {
        int a0 = clampAlpha(alpha0);
        int a1 = clampAlpha(alpha1);
        int a2 = clampAlpha(alpha2);
        int a3 = clampAlpha(alpha3);
        if (a0 == a1 && a0 == a2 && a0 == a3
                && LegacyWavefrontModel.renderUntexturedTransientQuad(batch.poseStack, batch.buffer, batch.renderMode,
                x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, color, a0)) {
            return;
        }
        if (LegacyWavefrontModel.renderUntexturedVertexColorTransientQuad(batch.poseStack, batch.buffer,
                batch.renderMode,
                x0, y0, z0, color, a0,
                x1, y1, z1, color, a1,
                x2, y2, z2, color, a2,
                x3, y3, z3, color, a3)) {
            return;
        }
        int minimumAlpha = minimumAlpha(a0, a1, a2, a3);
        quad(batch.fallbackConsumer(minimumAlpha), batch.pose(),
                x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                color, a0, a1, a2, a3);
    }

    public static final class QuadBatch {
        private final PoseStack poseStack;
        private final MultiBufferSource buffer;
        private final LegacyTexturedRenderMode renderMode;
        private VertexConsumer fallbackConsumer;
        private PoseStack.Pose fallbackPose;
        private int fallbackAlpha = -1;

        private QuadBatch(PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode) {
            this.poseStack = poseStack;
            this.buffer = buffer;
            this.renderMode = renderMode;
        }

        private VertexConsumer fallbackConsumer(int alpha) {
            if (this.fallbackConsumer == null || this.fallbackAlpha != alpha) {
                this.fallbackConsumer = consumer(this.buffer, this.renderMode, alpha);
                this.fallbackAlpha = alpha;
            }
            return this.fallbackConsumer;
        }

        private PoseStack.Pose pose() {
            if (this.fallbackPose == null) {
                this.fallbackPose = this.poseStack.last();
            }
            return this.fallbackPose;
        }
    }

    public static DirectQuadBatch directQuadBatch(PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode) {
        return directQuadBatch(poseStack, buffer, renderMode, 255);
    }

    public static DirectQuadBatch directQuadBatch(PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, int renderAlpha) {
        return new DirectQuadBatch(poseStack, buffer, renderMode, renderAlpha);
    }

    public static final class DirectQuadBatch {
        private final VertexConsumer consumer;
        private final PoseStack.Pose pose;

        private DirectQuadBatch(PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                int renderAlpha) {
            this.consumer = consumer(buffer, renderMode, renderAlpha);
            this.pose = poseStack.last();
        }
    }

    public static DirectTriangleBatch directTriangleBatch(PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode) {
        return directTriangleBatch(poseStack, buffer, renderMode, 255);
    }

    public static DirectTriangleBatch directTriangleBatch(PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, int renderAlpha) {
        return new DirectTriangleBatch(poseStack, buffer, renderMode, renderAlpha);
    }

    public static final class DirectTriangleBatch {
        private final VertexConsumer consumer;
        private final PoseStack.Pose pose;

        private DirectTriangleBatch(PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                int renderAlpha) {
            this.consumer = buffer.getBuffer(type(renderMode, renderAlpha, VertexFormat.Mode.TRIANGLES));
            this.pose = poseStack.last();
        }
    }

    public static void quadDirect(DirectQuadBatch batch,
                            double x0, double y0, double z0, int color0, int alpha0,
                            double x1, double y1, double z1, int color1, int alpha1,
                            double x2, double y2, double z2, int color2, int alpha2,
                            double x3, double y3, double z3, int color3, int alpha3) {
        vertex(batch.consumer, batch.pose, x0, y0, z0, color0, clampAlpha(alpha0));
        vertex(batch.consumer, batch.pose, x1, y1, z1, color1, clampAlpha(alpha1));
        vertex(batch.consumer, batch.pose, x2, y2, z2, color2, clampAlpha(alpha2));
        vertex(batch.consumer, batch.pose, x3, y3, z3, color3, clampAlpha(alpha3));
    }

    public static void quadDirect(DirectQuadBatch batch,
                            double x0, double y0, double z0,
                            double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            double x3, double y3, double z3,
                            int color, int alpha0, int alpha1, int alpha2, int alpha3) {
        quadDirect(batch,
                x0, y0, z0, color, alpha0,
                x1, y1, z1, color, alpha1,
                x2, y2, z2, color, alpha2,
                x3, y3, z3, color, alpha3);
    }

    public static void quadRgbaFDirect(DirectQuadBatch batch,
                            double x0, double y0, double z0,
                            double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            double x3, double y3, double z3,
                            float red, float green, float blue,
                            float alpha0, float alpha1, float alpha2, float alpha3) {
        int color = rgb(red, green, blue);
        quadDirect(batch,
                x0, y0, z0, color, alpha(alpha0),
                x1, y1, z1, color, alpha(alpha1),
                x2, y2, z2, color, alpha(alpha2),
                x3, y3, z3, color, alpha(alpha3));
    }

    public static void quadDirect(PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                            double x0, double y0, double z0, int color0, int alpha0,
                            double x1, double y1, double z1, int color1, int alpha1,
                            double x2, double y2, double z2, int color2, int alpha2,
                            double x3, double y3, double z3, int color3, int alpha3) {
        int a0 = clampAlpha(alpha0);
        int a1 = clampAlpha(alpha1);
        int a2 = clampAlpha(alpha2);
        int a3 = clampAlpha(alpha3);
        VertexConsumer consumer = consumer(buffer, renderMode, minimumAlpha(a0, a1, a2, a3));
        PoseStack.Pose pose = poseStack.last();
        vertex(consumer, pose, x0, y0, z0, color0, a0);
        vertex(consumer, pose, x1, y1, z1, color1, a1);
        vertex(consumer, pose, x2, y2, z2, color2, a2);
        vertex(consumer, pose, x3, y3, z3, color3, a3);
    }

    public static void quadDirect(PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                            double x0, double y0, double z0,
                            double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            double x3, double y3, double z3,
                            int color, int alpha0, int alpha1, int alpha2, int alpha3) {
        quadDirect(poseStack, buffer, renderMode,
                x0, y0, z0, color, alpha0,
                x1, y1, z1, color, alpha1,
                x2, y2, z2, color, alpha2,
                x3, y3, z3, color, alpha3);
    }

    public static void quadRgbaFDirect(PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                            double x0, double y0, double z0,
                            double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            double x3, double y3, double z3,
                            float red, float green, float blue,
                            float alpha0, float alpha1, float alpha2, float alpha3) {
        int color = rgb(red, green, blue);
        quadDirect(poseStack, buffer, renderMode,
                x0, y0, z0, color, alpha(alpha0),
                x1, y1, z1, color, alpha(alpha1),
                x2, y2, z2, color, alpha(alpha2),
                x3, y3, z3, color, alpha(alpha3));
    }

    public static void triangleDirect(PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                            double x0, double y0, double z0, int color0, int alpha0,
                            double x1, double y1, double z1, int color1, int alpha1,
                            double x2, double y2, double z2, int color2, int alpha2) {
        int a0 = clampAlpha(alpha0);
        int a1 = clampAlpha(alpha1);
        int a2 = clampAlpha(alpha2);
        VertexConsumer consumer = buffer.getBuffer(type(renderMode, minimumAlpha(a0, a1, a2),
                VertexFormat.Mode.TRIANGLES));
        PoseStack.Pose pose = poseStack.last();
        vertex(consumer, pose, x0, y0, z0, color0, a0);
        vertex(consumer, pose, x1, y1, z1, color1, a1);
        vertex(consumer, pose, x2, y2, z2, color2, a2);
    }

    public static void triangleDirect(DirectTriangleBatch batch,
                            double x0, double y0, double z0, int color0, int alpha0,
                            double x1, double y1, double z1, int color1, int alpha1,
                            double x2, double y2, double z2, int color2, int alpha2) {
        vertex(batch.consumer, batch.pose, x0, y0, z0, color0, clampAlpha(alpha0));
        vertex(batch.consumer, batch.pose, x1, y1, z1, color1, clampAlpha(alpha1));
        vertex(batch.consumer, batch.pose, x2, y2, z2, color2, clampAlpha(alpha2));
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

    public static void doubleSidedQuad(PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                                       double x0, double y0, double z0,
                                       double x1, double y1, double z1,
                                       double x2, double y2, double z2,
                                       double x3, double y3, double z3,
                                       int color,
                                       int alpha0, int alpha1, int alpha2, int alpha3) {
        if (alpha0 == alpha1 && alpha0 == alpha2 && alpha0 == alpha3) {
            int alpha = clampAlpha(alpha0);
            boolean front = LegacyWavefrontModel.renderUntexturedTransientQuad(poseStack, buffer, renderMode,
                    x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, color, alpha);
            boolean back = LegacyWavefrontModel.renderUntexturedTransientQuad(poseStack, buffer, renderMode,
                    x3, y3, z3, x2, y2, z2, x1, y1, z1, x0, y0, z0, color, alpha);
            if (front && back) {
                return;
            }
        }
        if (LegacyWavefrontModel.renderDoubleSidedUntexturedVertexColorTransientQuad(poseStack, buffer, renderMode,
                x0, y0, z0, color, clampAlpha(alpha0),
                x1, y1, z1, color, clampAlpha(alpha1),
                x2, y2, z2, color, clampAlpha(alpha2),
                x3, y3, z3, color, clampAlpha(alpha3))) {
            return;
        }
        VertexConsumer consumer = consumer(buffer, renderMode, minimumAlpha(alpha0, alpha1, alpha2, alpha3));
        PoseStack.Pose pose = poseStack.last();
        doubleSidedQuad(consumer, pose, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                color >> 16 & 255, color >> 8 & 255, color & 255,
                clampAlpha(alpha0), clampAlpha(alpha1), clampAlpha(alpha2), clampAlpha(alpha3));
    }

    public static void doubleSidedQuadDirect(PoseStack poseStack, MultiBufferSource buffer,
                                       LegacyTexturedRenderMode renderMode,
                                       double x0, double y0, double z0,
                                       double x1, double y1, double z1,
                                       double x2, double y2, double z2,
                                       double x3, double y3, double z3,
                                       int color,
                                       int alpha0, int alpha1, int alpha2, int alpha3) {
        int a0 = clampAlpha(alpha0);
        int a1 = clampAlpha(alpha1);
        int a2 = clampAlpha(alpha2);
        int a3 = clampAlpha(alpha3);
        VertexConsumer consumer = consumer(buffer, renderMode, minimumAlpha(a0, a1, a2, a3));
        doubleSidedQuad(consumer, poseStack.last(), x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                color >> 16 & 255, color >> 8 & 255, color & 255, a0, a1, a2, a3);
    }

    public static void horizontalQuad(PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
            double y, double minX, double minZ, double maxX, double maxZ, int color, int alpha) {
        quad(poseStack, buffer, renderMode,
                minX, y, minZ,
                minX, y, maxZ,
                maxX, y, maxZ,
                maxX, y, minZ,
                color, alpha, alpha, alpha, alpha);
    }

    public static void horizontalQuad(DirectQuadBatch batch,
            double y, double minX, double minZ, double maxX, double maxZ, int color, int alpha) {
        quadDirect(batch,
                minX, y, minZ,
                minX, y, maxZ,
                maxX, y, maxZ,
                maxX, y, minZ,
                color, alpha, alpha, alpha, alpha);
    }

    public static void horizontalSlices(PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, double minX, double minZ, double maxX, double maxZ,
            double minY, double maxY, double step, int color, int alpha) {
        if (step <= 0.0D || maxY < minY) {
            return;
        }
        for (double y = minY; y <= maxY + 1.0E-6D; y += step) {
            horizontalQuad(poseStack, buffer, renderMode, y, minX, minZ, maxX, maxZ, color, alpha);
        }
    }

    public static void horizontalSlices(DirectQuadBatch batch,
            double minX, double minZ, double maxX, double maxZ,
            double minY, double maxY, double step, int color, int alpha) {
        if (step <= 0.0D || maxY < minY) {
            return;
        }
        for (double y = minY; y <= maxY + 1.0E-6D; y += step) {
            horizontalQuad(batch, y, minX, minZ, maxX, maxZ, color, alpha);
        }
    }

    public static void xPlaneCenteredRect(PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, double x, double y, double z, double halfY, double halfZ,
            int color, int alpha) {
        quad(poseStack, buffer, renderMode,
                x, y + halfY, z - halfZ,
                x, y + halfY, z + halfZ,
                x, y - halfY, z + halfZ,
                x, y - halfY, z - halfZ,
                color, alpha, alpha, alpha, alpha);
    }

    public static void xPlaneCenteredRect(DirectQuadBatch batch,
            double x, double y, double z, double halfY, double halfZ, int color, int alpha) {
        quadDirect(batch,
                x, y + halfY, z - halfZ,
                x, y + halfY, z + halfZ,
                x, y - halfY, z + halfZ,
                x, y - halfY, z - halfZ,
                color, alpha, alpha, alpha, alpha);
    }

    public static void xPlaneDot(PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
            double x, double y, double z, double width, double edge, int color, int alpha) {
        quad(poseStack, buffer, renderMode,
                x, y + width, z,
                x, y + edge, z + edge,
                x, y, z + width,
                x, y - edge, z + edge,
                color, alpha, alpha, alpha, alpha);
        quad(poseStack, buffer, renderMode,
                x, y + edge, z - edge,
                x, y + width, z,
                x, y - edge, z - edge,
                x, y, z - width,
                color, alpha, alpha, alpha, alpha);
        quad(poseStack, buffer, renderMode,
                x, y + width, z,
                x, y - edge, z + edge,
                x, y - width, z,
                x, y - edge, z - edge,
                color, alpha, alpha, alpha, alpha);
    }

    public static void xPlaneDot(DirectQuadBatch batch,
            double x, double y, double z, double width, double edge, int color, int alpha) {
        quadDirect(batch,
                x, y + width, z,
                x, y + edge, z + edge,
                x, y, z + width,
                x, y - edge, z + edge,
                color, alpha, alpha, alpha, alpha);
        quadDirect(batch,
                x, y + edge, z - edge,
                x, y + width, z,
                x, y - edge, z - edge,
                x, y, z - width,
                color, alpha, alpha, alpha, alpha);
        quadDirect(batch,
                x, y + width, z,
                x, y - edge, z + edge,
                x, y - width, z,
                x, y - edge, z - edge,
                color, alpha, alpha, alpha, alpha);
    }

    private static int minimumAlpha(int... vertexAlphas) {
        int alpha = 255;
        for (int vertexAlpha : vertexAlphas) {
            alpha = Math.min(alpha, clampAlpha(vertexAlpha));
        }
        return alpha;
    }

    private static int minimumAlpha(int alpha0, int alpha1, int alpha2, int alpha3) {
        return Math.min(Math.min(clampAlpha(alpha0), clampAlpha(alpha1)),
                Math.min(clampAlpha(alpha2), clampAlpha(alpha3)));
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
