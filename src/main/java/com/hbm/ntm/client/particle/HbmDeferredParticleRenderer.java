package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public final class HbmDeferredParticleRenderer {
    public static final ParticleRenderType DEFERRED_RENDER_TYPE = ParticleRenderType.CUSTOM;

    private static final RenderStateShard.TransparencyStateShard NORMAL_ALPHA_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard("hbm_deferred_particle_alpha_transparency",
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
    private static final RenderStateShard.TransparencyStateShard ADDITIVE_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard("hbm_deferred_particle_additive_transparency",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                                GlStateManager.DestFactor.ONE);
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    });
    private static final RenderStateShard.DepthTestStateShard LEQUAL_DEPTH_TEST =
            new RenderStateShard.DepthTestStateShard("hbm_deferred_particle_lequal_depth_test", 515);
    private static final RenderType PARTICLE_SHEET_DEPTH_WRITE = createRenderType(
            "hbm_deferred_particle_sheet_depth_write", TextureAtlas.LOCATION_PARTICLES,
            NORMAL_ALPHA_TRANSPARENCY, true);
    private static final RenderType PARTICLE_SHEET_ADDITIVE_NO_DEPTH_WRITE = createRenderType(
            "hbm_deferred_particle_sheet_additive_no_depth_write", TextureAtlas.LOCATION_PARTICLES,
            ADDITIVE_TRANSPARENCY, false);
    private static final Map<ResourceLocation, RenderType> TEXTURED_DEPTH_WRITE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> TEXTURED_NO_DEPTH_WRITE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> TEXTURED_ADDITIVE_NO_DEPTH_WRITE = new ConcurrentHashMap<>();
    private static final List<Entry> QUEUE = new ArrayList<>();
    private static final Set<DeferredParticle> SEEN =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private static long enqueuedParticles;
    private static long duplicateSkips;
    private static long renderPasses;
    private static long renderedParticles;
    private static long clearCalls;
    private static long peakQueueSize;
    private static long lastRenderQueuedParticles;
    private static long lastRenderSubmittedParticles;
    private static long lastClearQueuedParticles;
    private static long directTexturedNoDepthWriteQuads;
    private static long directTexturedAdditiveNoDepthWriteQuads;

    private HbmDeferredParticleRenderer() {
    }

    public static void enqueue(DeferredParticle particle, Camera camera, double x, double y, double z) {
        if (!SEEN.add(particle)) {
            duplicateSkips++;
            return;
        }
        QUEUE.add(new Entry(particle, camera.getPosition().distanceToSqr(x, y, z)));
        enqueuedParticles++;
        recordPeakQueueSize(QUEUE.size());
    }

    public static void renderAfterLevel(Camera camera, float partialTick, MultiBufferSource.BufferSource buffer) {
        if (QUEUE.isEmpty()) {
            lastRenderQueuedParticles = 0L;
            lastRenderSubmittedParticles = 0L;
            return;
        }

        List<Entry> entries = new ArrayList<>(QUEUE);
        QUEUE.clear();
        SEEN.clear();
        renderPasses++;
        renderedParticles += entries.size();
        lastRenderQueuedParticles = entries.size();

        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        modelView.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        modelView.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        RenderSystem.applyModelViewMatrix();
        try {
            entries.sort(Comparator.comparingDouble((Entry entry) -> entry.distanceToCameraSqr).reversed());
            for (Entry entry : entries) {
                entry.particle.renderDeferred(buffer, camera, partialTick);
            }
            lastRenderSubmittedParticles = entries.size();
            endDeferredBatches(buffer);
        } finally {
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    public static RenderType particleSheetDepthWrite() {
        return PARTICLE_SHEET_DEPTH_WRITE;
    }

    public static RenderType texturedDepthWrite(ResourceLocation texture) {
        return TEXTURED_DEPTH_WRITE.computeIfAbsent(texture,
                key -> createRenderType("hbm_deferred_particle_depth_write_" + sanitize(key), key,
                        NORMAL_ALPHA_TRANSPARENCY, true));
    }

    public static RenderType texturedNoDepthWrite(ResourceLocation texture) {
        return TEXTURED_NO_DEPTH_WRITE.computeIfAbsent(texture,
                key -> createRenderType("hbm_deferred_particle_no_depth_write_" + sanitize(key), key,
                        NORMAL_ALPHA_TRANSPARENCY, false));
    }

    public static RenderType particleSheetAdditiveNoDepthWrite() {
        return PARTICLE_SHEET_ADDITIVE_NO_DEPTH_WRITE;
    }

    public static RenderType texturedAdditiveNoDepthWrite(ResourceLocation texture) {
        return TEXTURED_ADDITIVE_NO_DEPTH_WRITE.computeIfAbsent(texture,
                key -> createRenderType("hbm_deferred_particle_additive_no_depth_write_" + sanitize(key), key,
                        ADDITIVE_TRANSPARENCY, false));
    }

    public static boolean renderTexturedNoDepthWriteQuad(ResourceLocation texture, MultiBufferSource buffer,
            int packedLight, float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, float u0, float v0,
            double x1, double y1, double z1, float u1, float v1,
            double x2, double y2, double z2, float u2, float v2,
            double x3, double y3, double z3, float u3, float v3,
            int color, int alpha) {
        VertexConsumer consumer = buffer.getBuffer(texturedNoDepthWrite(texture));
        emitTexturedParticleQuad(consumer, packedLight,
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                color, alpha);
        directTexturedNoDepthWriteQuads++;
        return true;
    }

    public static boolean renderTexturedAdditiveNoDepthWriteQuad(ResourceLocation texture, MultiBufferSource buffer,
            int packedLight, float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, float u0, float v0,
            double x1, double y1, double z1, float u1, float v1,
            double x2, double y2, double z2, float u2, float v2,
            double x3, double y3, double z3, float u3, float v3,
            int color, int alpha) {
        VertexConsumer consumer = buffer.getBuffer(texturedAdditiveNoDepthWrite(texture));
        emitTexturedParticleQuad(consumer, packedLight,
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                color, alpha);
        directTexturedAdditiveNoDepthWriteQuads++;
        return true;
    }

    public static void emitParticleSheetQuad(VertexConsumer consumer, int packedLight,
            Vector3f v0, float u0, float texV0,
            Vector3f v1, float u1, float texV1,
            Vector3f v2, float u2, float texV2,
            Vector3f v3, float u3, float texV3,
            float red, float green, float blue, float alpha) {
        emitParticleSheetQuad(consumer, packedLight,
                v0.x(), v0.y(), v0.z(), u0, texV0,
                v1.x(), v1.y(), v1.z(), u1, texV1,
                v2.x(), v2.y(), v2.z(), u2, texV2,
                v3.x(), v3.y(), v3.z(), u3, texV3,
                red, green, blue, alpha);
    }

    public static void emitParticleSheetQuad(VertexConsumer consumer, int packedLight,
            double x0, double y0, double z0, float u0, float v0,
            double x1, double y1, double z1, float u1, float v1,
            double x2, double y2, double z2, float u2, float v2,
            double x3, double y3, double z3, float u3, float v3,
            float red, float green, float blue, float alpha) {
        emitParticleSheetVertex(consumer, packedLight, x0, y0, z0, u0, v0, red, green, blue, alpha);
        emitParticleSheetVertex(consumer, packedLight, x1, y1, z1, u1, v1, red, green, blue, alpha);
        emitParticleSheetVertex(consumer, packedLight, x2, y2, z2, u2, v2, red, green, blue, alpha);
        emitParticleSheetVertex(consumer, packedLight, x3, y3, z3, u3, v3, red, green, blue, alpha);
    }

    public static void emitTextureSheetParticleQuad(VertexConsumer consumer, Camera camera, float partialTick,
            double xo, double yo, double zo, double x, double y, double z,
            float oldRoll, float roll, float quadSize,
            float u0, float u1, float v0, float v1,
            float red, float green, float blue, float alpha, int packedLight) {
        Vec3 cameraPos = camera.getPosition();
        float renderX = (float) (Mth.lerp(partialTick, xo, x) - cameraPos.x());
        float renderY = (float) (Mth.lerp(partialTick, yo, y) - cameraPos.y());
        float renderZ = (float) (Mth.lerp(partialTick, zo, z) - cameraPos.z());
        Quaternionf rotation = roll == 0.0F
                ? camera.rotation()
                : new Quaternionf(camera.rotation()).rotateZ(Mth.lerp(partialTick, oldRoll, roll));
        Vector3f[] corners = {
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        for (Vector3f corner : corners) {
            corner.rotate(rotation).mul(quadSize).add(renderX, renderY, renderZ);
        }
        emitParticleSheetQuad(consumer, packedLight,
                corners[0], u1, v1,
                corners[1], u1, v0,
                corners[2], u0, v0,
                corners[3], u0, v1,
                red, green, blue, alpha);
    }

    public static void clear() {
        clearCalls++;
        lastClearQueuedParticles = QUEUE.size();
        QUEUE.clear();
        SEEN.clear();
    }

    public static DeferredParticleSnapshot snapshot() {
        return new DeferredParticleSnapshot(
                QUEUE.size(),
                SEEN.size(),
                enqueuedParticles,
                duplicateSkips,
                renderPasses,
                renderedParticles,
                clearCalls,
                peakQueueSize,
                lastRenderQueuedParticles,
                lastRenderSubmittedParticles,
                lastClearQueuedParticles,
                TEXTURED_DEPTH_WRITE.size(),
                TEXTURED_NO_DEPTH_WRITE.size(),
                TEXTURED_ADDITIVE_NO_DEPTH_WRITE.size(),
                directTexturedNoDepthWriteQuads,
                directTexturedAdditiveNoDepthWriteQuads);
    }

    private static void recordPeakQueueSize(int size) {
        if (size > peakQueueSize) {
            peakQueueSize = size;
        }
    }

    private static void endDeferredBatches(MultiBufferSource.BufferSource buffer) {
        buffer.endBatch(PARTICLE_SHEET_DEPTH_WRITE);
        buffer.endBatch(PARTICLE_SHEET_ADDITIVE_NO_DEPTH_WRITE);
        for (RenderType renderType : TEXTURED_DEPTH_WRITE.values()) {
            buffer.endBatch(renderType);
        }
        for (RenderType renderType : TEXTURED_NO_DEPTH_WRITE.values()) {
            buffer.endBatch(renderType);
        }
        for (RenderType renderType : TEXTURED_ADDITIVE_NO_DEPTH_WRITE.values()) {
            buffer.endBatch(renderType);
        }
    }

    private static void emitParticleSheetVertex(VertexConsumer consumer, int packedLight,
            double x, double y, double z, float u, float v,
            float red, float green, float blue, float alpha) {
        consumer.vertex(x, y, z)
                .uv(u, v)
                .color(red, green, blue, alpha)
                .uv2(packedLight)
                .endVertex();
    }

    private static void emitTexturedParticleQuad(VertexConsumer consumer, int packedLight,
            double x0, double y0, double z0, float u0, float v0,
            double x1, double y1, double z1, float u1, float v1,
            double x2, double y2, double z2, float u2, float v2,
            double x3, double y3, double z3, float u3, float v3,
            int color, int alpha) {
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float alphaF = Mth.clamp(alpha, 0, 255) / 255.0F;
        emitParticleSheetQuad(consumer, packedLight,
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                red, green, blue, alphaF);
    }

    private static RenderType createRenderType(String name, ResourceLocation texture,
            RenderStateShard.TransparencyStateShard transparency, boolean depthWrite) {
        return RenderType.create(name, DefaultVertexFormat.PARTICLE, VertexFormat.Mode.QUADS, 256,
                false, true, RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getParticleShader))
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(transparency)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setCullState(new RenderStateShard.CullStateShard(false))
                        .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                        .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, depthWrite))
                        .createCompositeState(false));
    }

    private static String sanitize(ResourceLocation texture) {
        return texture.toString().replace(':', '_').replace('/', '_').replace('.', '_');
    }

    public interface DeferredParticle {
        void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick);
    }

    public record DeferredParticleSnapshot(
            int queuedParticles,
            int seenParticles,
            long enqueuedParticles,
            long duplicateSkips,
            long renderPasses,
            long renderedParticles,
            long clearCalls,
            long peakQueueSize,
            long lastRenderQueuedParticles,
            long lastRenderSubmittedParticles,
            long lastClearQueuedParticles,
            int texturedDepthWriteTypes,
            int texturedNoDepthWriteTypes,
            int texturedAdditiveNoDepthWriteTypes,
            long directTexturedNoDepthWriteQuads,
            long directTexturedAdditiveNoDepthWriteQuads) {
    }

    private record Entry(DeferredParticle particle, double distanceToCameraSqr) {
    }
}
