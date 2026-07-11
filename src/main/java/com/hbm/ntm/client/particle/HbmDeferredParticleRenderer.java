package com.hbm.ntm.client.particle;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
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
import java.util.HashMap;
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
    private static final List<Entry> DRAIN = new ArrayList<>();
    private static final List<Entry> ENTRY_POOL = new ArrayList<>();
    private static final Comparator<Entry> DISTANCE_DESCENDING =
            Comparator.comparingDouble(Entry::distanceToCameraSqr).reversed();
    private static final Set<DeferredParticle> SEEN =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private static final ThreadLocal<Vector3f[]> TEXTURE_SHEET_CORNERS =
            ThreadLocal.withInitial(HbmDeferredParticleRenderer::newVectorQuad);
    private static final ThreadLocal<Vector3f[]> CAMERA_BILLBOARD_BASIS =
            ThreadLocal.withInitial(() -> new Vector3f[] { new Vector3f(), new Vector3f() });
    private static final ThreadLocal<RenderPassBillboardBasis> RENDER_PASS_BILLBOARD_BASIS =
            ThreadLocal.withInitial(RenderPassBillboardBasis::new);
    private static final ThreadLocal<RenderPassParticleSheetConsumers> RENDER_PASS_PARTICLE_SHEET_CONSUMERS =
            ThreadLocal.withInitial(RenderPassParticleSheetConsumers::new);
    private static final ThreadLocal<Quaternionf> ROLL_ROTATION =
            ThreadLocal.withInitial(Quaternionf::new);
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
        Vec3 cameraPos = camera.getPosition();
        double dx = cameraPos.x() - x;
        double dy = cameraPos.y() - y;
        double dz = cameraPos.z() - z;
        QUEUE.add(acquireEntry(particle, dx * dx + dy * dy + dz * dz));
        enqueuedParticles++;
        recordPeakQueueSize(QUEUE.size());
    }

    public static void renderAfterLevel(Camera camera, float partialTick, MultiBufferSource.BufferSource buffer) {
        if (QUEUE.isEmpty()) {
            lastRenderQueuedParticles = 0L;
            lastRenderSubmittedParticles = 0L;
            return;
        }

        DRAIN.addAll(QUEUE);
        QUEUE.clear();
        SEEN.clear();
        renderPasses++;
        renderedParticles += DRAIN.size();
        lastRenderQueuedParticles = DRAIN.size();

        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        LegacyPoseRotations.rotateXDegrees(modelView, camera.getXRot());
        LegacyPoseRotations.rotateYDegrees(modelView, camera.getYRot() + 180.0F);
        RenderSystem.applyModelViewMatrix();
        beginRenderPassBillboardBasis(camera);
        beginRenderPassParticleSheetConsumers(buffer);
        try {
            DRAIN.sort(DISTANCE_DESCENDING);
            for (Entry entry : DRAIN) {
                entry.particle.renderDeferred(buffer, camera, partialTick);
            }
            lastRenderSubmittedParticles = DRAIN.size();
            endDeferredBatches(buffer);
        } finally {
            endRenderPassParticleSheetConsumers();
            endRenderPassBillboardBasis();
            releaseEntries(DRAIN);
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    public static RenderType particleSheetDepthWrite() {
        return PARTICLE_SHEET_DEPTH_WRITE;
    }

    public static VertexConsumer particleSheetDepthWriteConsumer(MultiBufferSource buffer) {
        RenderPassParticleSheetConsumers consumers = RENDER_PASS_PARTICLE_SHEET_CONSUMERS.get();
        return consumers.depthWriteConsumer(buffer);
    }

    public static RenderType texturedDepthWrite(ResourceLocation texture) {
        return TEXTURED_DEPTH_WRITE.computeIfAbsent(texture,
                key -> createRenderType("hbm_deferred_particle_depth_write_" + sanitize(key), key,
                        NORMAL_ALPHA_TRANSPARENCY, true));
    }

    public static VertexConsumer texturedDepthWriteConsumer(ResourceLocation texture, MultiBufferSource buffer) {
        RenderPassParticleSheetConsumers consumers = RENDER_PASS_PARTICLE_SHEET_CONSUMERS.get();
        return consumers.texturedDepthWriteConsumer(texture, buffer);
    }

    public static RenderType texturedNoDepthWrite(ResourceLocation texture) {
        return TEXTURED_NO_DEPTH_WRITE.computeIfAbsent(texture,
                key -> createRenderType("hbm_deferred_particle_no_depth_write_" + sanitize(key), key,
                        NORMAL_ALPHA_TRANSPARENCY, false));
    }

    public static RenderType particleSheetAdditiveNoDepthWrite() {
        return PARTICLE_SHEET_ADDITIVE_NO_DEPTH_WRITE;
    }

    public static VertexConsumer particleSheetAdditiveNoDepthWriteConsumer(MultiBufferSource buffer) {
        RenderPassParticleSheetConsumers consumers = RENDER_PASS_PARTICLE_SHEET_CONSUMERS.get();
        return consumers.additiveNoDepthWriteConsumer(buffer);
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
        VertexConsumer consumer = texturedNoDepthWriteConsumer(texture, buffer);
        emitTexturedNoDepthWriteQuad(consumer, packedLight,
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                color, alpha);
        return true;
    }

    public static VertexConsumer texturedNoDepthWriteConsumer(ResourceLocation texture, MultiBufferSource buffer) {
        RenderPassParticleSheetConsumers consumers = RENDER_PASS_PARTICLE_SHEET_CONSUMERS.get();
        return consumers.texturedNoDepthWriteConsumer(texture, buffer);
    }

    public static void emitTexturedNoDepthWriteQuad(VertexConsumer consumer, int packedLight,
            double x0, double y0, double z0, float u0, float v0,
            double x1, double y1, double z1, float u1, float v1,
            double x2, double y2, double z2, float u2, float v2,
            double x3, double y3, double z3, float u3, float v3,
            int color, int alpha) {
        emitTexturedParticleQuad(consumer, packedLight,
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                color, alpha);
        directTexturedNoDepthWriteQuads++;
    }

    public static boolean renderTexturedAdditiveNoDepthWriteQuad(ResourceLocation texture, MultiBufferSource buffer,
            int packedLight, float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, float u0, float v0,
            double x1, double y1, double z1, float u1, float v1,
            double x2, double y2, double z2, float u2, float v2,
            double x3, double y3, double z3, float u3, float v3,
            int color, int alpha) {
        VertexConsumer consumer = texturedAdditiveNoDepthWriteConsumer(texture, buffer);
        emitTexturedAdditiveNoDepthWriteQuad(consumer, packedLight,
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                color, alpha);
        return true;
    }

    public static VertexConsumer texturedAdditiveNoDepthWriteConsumer(ResourceLocation texture, MultiBufferSource buffer) {
        RenderPassParticleSheetConsumers consumers = RENDER_PASS_PARTICLE_SHEET_CONSUMERS.get();
        return consumers.texturedAdditiveNoDepthWriteConsumer(texture, buffer);
    }

    public static void emitTexturedAdditiveNoDepthWriteQuad(VertexConsumer consumer, int packedLight,
            double x0, double y0, double z0, float u0, float v0,
            double x1, double y1, double z1, float u1, float v1,
            double x2, double y2, double z2, float u2, float v2,
            double x3, double y3, double z3, float u3, float v3,
            int color, int alpha) {
        emitTexturedParticleQuad(consumer, packedLight,
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                color, alpha);
        directTexturedAdditiveNoDepthWriteQuads++;
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
        if (roll == 0.0F) {
            Vector3f[] basis = cameraBillboardBasis(camera, 1.0F);
            emitCameraUnitParticleSheetQuad(consumer, packedLight, basis[0], basis[1],
                    renderX, renderY, renderZ, quadSize, u0, u1, v0, v1, red, green, blue, alpha);
            return;
        }
        Quaternionf rotation = ROLL_ROTATION.get().set(camera.rotation()).rotateZ(Mth.lerp(partialTick, oldRoll, roll));
        Vector3f[] corners = TEXTURE_SHEET_CORNERS.get();
        corners[0].set(-1.0F, -1.0F, 0.0F).rotate(rotation).mul(quadSize).add(renderX, renderY, renderZ);
        corners[1].set(-1.0F, 1.0F, 0.0F).rotate(rotation).mul(quadSize).add(renderX, renderY, renderZ);
        corners[2].set(1.0F, 1.0F, 0.0F).rotate(rotation).mul(quadSize).add(renderX, renderY, renderZ);
        corners[3].set(1.0F, -1.0F, 0.0F).rotate(rotation).mul(quadSize).add(renderX, renderY, renderZ);
        emitParticleSheetQuad(consumer, packedLight,
                corners[0], u1, v1,
                corners[1], u1, v0,
                corners[2], u0, v0,
                corners[3], u0, v1,
                red, green, blue, alpha);
    }

    static void emitUnitParticleSheetQuad(VertexConsumer consumer, int packedLight, Quaternionf rotation,
            float x, float y, float z, float size,
            float u0, float u1, float v0, float v1,
            float red, float green, float blue, float alpha) {
        emitLocalParticleSheetQuad(consumer, packedLight, rotation, x, y, z,
                -size, -size, 0.0F, u1, v1,
                -size, size, 0.0F, u1, v0,
                size, size, 0.0F, u0, v0,
                size, -size, 0.0F, u0, v1,
                red, green, blue, alpha);
    }

    static void emitCameraUnitParticleSheetQuad(VertexConsumer consumer, Camera camera, int packedLight,
            float x, float y, float z, float size,
            float u0, float u1, float v0, float v1,
            float red, float green, float blue, float alpha) {
        Vector3f[] basis = cameraBillboardBasis(camera, 1.0F);
        emitCameraUnitParticleSheetQuad(consumer, packedLight, basis[0], basis[1],
                x, y, z, size, u0, u1, v0, v1, red, green, blue, alpha);
    }

    static void emitCameraUnitParticleSheetQuad(VertexConsumer consumer, int packedLight,
            Vector3f rightUnit, Vector3f upUnit,
            float x, float y, float z, float size,
            float u0, float u1, float v0, float v1,
            float red, float green, float blue, float alpha) {
        float rightX = rightUnit.x() * size;
        float rightY = rightUnit.y() * size;
        float rightZ = rightUnit.z() * size;
        float upX = upUnit.x() * size;
        float upY = upUnit.y() * size;
        float upZ = upUnit.z() * size;
        emitParticleSheetQuad(consumer, packedLight,
                x - rightX - upX, y - rightY - upY, z - rightZ - upZ, u1, v1,
                x - rightX + upX, y - rightY + upY, z - rightZ + upZ, u1, v0,
                x + rightX + upX, y + rightY + upY, z + rightZ + upZ, u0, v0,
                x + rightX - upX, y + rightY - upY, z + rightZ - upZ, u0, v1,
                red, green, blue, alpha);
    }

    static void emitLocalParticleSheetQuad(VertexConsumer consumer, int packedLight, Quaternionf rotation,
            float x, float y, float z,
            float x0, float y0, float z0, float u0, float v0,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            float red, float green, float blue, float alpha) {
        Vector3f[] corners = TEXTURE_SHEET_CORNERS.get();
        corners[0].set(x0, y0, z0).rotate(rotation).add(x, y, z);
        corners[1].set(x1, y1, z1).rotate(rotation).add(x, y, z);
        corners[2].set(x2, y2, z2).rotate(rotation).add(x, y, z);
        corners[3].set(x3, y3, z3).rotate(rotation).add(x, y, z);
        emitParticleSheetQuad(consumer, packedLight,
                corners[0], u0, v0,
                corners[1], u1, v1,
                corners[2], u2, v2,
                corners[3], u3, v3,
                red, green, blue, alpha);
    }

    static Vector3f[] cameraBillboardBasis(Camera camera, float scale) {
        if (scale == 1.0F) {
            RenderPassBillboardBasis renderPassBasis = RENDER_PASS_BILLBOARD_BASIS.get();
            if (renderPassBasis.valid) {
                return renderPassBasis.basis;
            }
        }
        Quaternionf rotation = camera.rotation();
        Vector3f[] basis = CAMERA_BILLBOARD_BASIS.get();
        basis[0].set(1.0F, 0.0F, 0.0F).rotate(rotation).mul(scale);
        basis[1].set(0.0F, 1.0F, 0.0F).rotate(rotation).mul(scale);
        return basis;
    }

    private static void beginRenderPassBillboardBasis(Camera camera) {
        RENDER_PASS_BILLBOARD_BASIS.get().update(camera);
    }

    private static void endRenderPassBillboardBasis() {
        RENDER_PASS_BILLBOARD_BASIS.get().valid = false;
    }

    private static void beginRenderPassParticleSheetConsumers(MultiBufferSource buffer) {
        RENDER_PASS_PARTICLE_SHEET_CONSUMERS.get().begin(buffer);
    }

    private static void endRenderPassParticleSheetConsumers() {
        RENDER_PASS_PARTICLE_SHEET_CONSUMERS.get().end();
    }

    static Quaternionf scratchRotation() {
        return ROLL_ROTATION.get().identity();
    }

    public static void clear() {
        clearCalls++;
        lastClearQueuedParticles = QUEUE.size() + DRAIN.size();
        releaseEntries(QUEUE);
        releaseEntries(DRAIN);
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

    private static Vector3f[] newVectorQuad() {
        return new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
    }

    private static Entry acquireEntry(DeferredParticle particle, double distanceToCameraSqr) {
        int last = ENTRY_POOL.size() - 1;
        Entry entry = last >= 0 ? ENTRY_POOL.remove(last) : new Entry();
        return entry.set(particle, distanceToCameraSqr);
    }

    private static void releaseEntries(List<Entry> entries) {
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            entry.clear();
            ENTRY_POOL.add(entry);
        }
        entries.clear();
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

    private static final class RenderPassBillboardBasis {
        private final Vector3f[] basis = new Vector3f[] { new Vector3f(), new Vector3f() };
        private boolean valid;

        private void update(Camera camera) {
            Quaternionf rotation = camera.rotation();
            this.basis[0].set(1.0F, 0.0F, 0.0F).rotate(rotation);
            this.basis[1].set(0.0F, 1.0F, 0.0F).rotate(rotation);
            this.valid = true;
        }
    }

    private static final class RenderPassParticleSheetConsumers {
        private MultiBufferSource buffer;
        private VertexConsumer depthWriteConsumer;
        private VertexConsumer additiveNoDepthWriteConsumer;
        private final Map<ResourceLocation, VertexConsumer> texturedDepthWriteConsumers = new HashMap<>();
        private final Map<ResourceLocation, VertexConsumer> texturedNoDepthWriteConsumers = new HashMap<>();
        private final Map<ResourceLocation, VertexConsumer> texturedAdditiveNoDepthWriteConsumers = new HashMap<>();
        private boolean valid;

        private void begin(MultiBufferSource buffer) {
            this.buffer = buffer;
            this.depthWriteConsumer = null;
            this.additiveNoDepthWriteConsumer = null;
            this.texturedDepthWriteConsumers.clear();
            this.texturedNoDepthWriteConsumers.clear();
            this.texturedAdditiveNoDepthWriteConsumers.clear();
            this.valid = true;
        }

        private void end() {
            this.buffer = null;
            this.depthWriteConsumer = null;
            this.additiveNoDepthWriteConsumer = null;
            this.texturedDepthWriteConsumers.clear();
            this.texturedNoDepthWriteConsumers.clear();
            this.texturedAdditiveNoDepthWriteConsumers.clear();
            this.valid = false;
        }

        private VertexConsumer depthWriteConsumer(MultiBufferSource buffer) {
            if (!this.valid || this.buffer != buffer) {
                return buffer.getBuffer(PARTICLE_SHEET_DEPTH_WRITE);
            }
            if (this.depthWriteConsumer == null) {
                this.depthWriteConsumer = buffer.getBuffer(PARTICLE_SHEET_DEPTH_WRITE);
            }
            return this.depthWriteConsumer;
        }

        private VertexConsumer additiveNoDepthWriteConsumer(MultiBufferSource buffer) {
            if (!this.valid || this.buffer != buffer) {
                return buffer.getBuffer(PARTICLE_SHEET_ADDITIVE_NO_DEPTH_WRITE);
            }
            if (this.additiveNoDepthWriteConsumer == null) {
                this.additiveNoDepthWriteConsumer = buffer.getBuffer(PARTICLE_SHEET_ADDITIVE_NO_DEPTH_WRITE);
            }
            return this.additiveNoDepthWriteConsumer;
        }

        private VertexConsumer texturedDepthWriteConsumer(ResourceLocation texture, MultiBufferSource buffer) {
            if (!this.valid || this.buffer != buffer) {
                return buffer.getBuffer(texturedDepthWrite(texture));
            }
            return this.texturedDepthWriteConsumers.computeIfAbsent(texture,
                    key -> buffer.getBuffer(texturedDepthWrite(key)));
        }

        private VertexConsumer texturedNoDepthWriteConsumer(ResourceLocation texture, MultiBufferSource buffer) {
            if (!this.valid || this.buffer != buffer) {
                return buffer.getBuffer(texturedNoDepthWrite(texture));
            }
            return this.texturedNoDepthWriteConsumers.computeIfAbsent(texture,
                    key -> buffer.getBuffer(texturedNoDepthWrite(key)));
        }

        private VertexConsumer texturedAdditiveNoDepthWriteConsumer(ResourceLocation texture, MultiBufferSource buffer) {
            if (!this.valid || this.buffer != buffer) {
                return buffer.getBuffer(texturedAdditiveNoDepthWrite(texture));
            }
            return this.texturedAdditiveNoDepthWriteConsumers.computeIfAbsent(texture,
                    key -> buffer.getBuffer(texturedAdditiveNoDepthWrite(key)));
        }
    }

    private static final class Entry {
        private DeferredParticle particle;
        private double distanceToCameraSqr;

        private Entry set(DeferredParticle particle, double distanceToCameraSqr) {
            this.particle = particle;
            this.distanceToCameraSqr = distanceToCameraSqr;
            return this;
        }

        private void clear() {
            this.particle = null;
            this.distanceToCameraSqr = 0.0D;
        }

        private double distanceToCameraSqr() {
            return this.distanceToCameraSqr;
        }
    }
}
