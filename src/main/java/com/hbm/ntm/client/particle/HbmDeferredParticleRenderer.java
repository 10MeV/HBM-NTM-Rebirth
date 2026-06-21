package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

    private HbmDeferredParticleRenderer() {
    }

    public static void enqueue(DeferredParticle particle, Camera camera, double x, double y, double z) {
        if (!SEEN.add(particle)) {
            return;
        }
        QUEUE.add(new Entry(particle, camera.getPosition().distanceToSqr(x, y, z)));
    }

    public static void renderAfterLevel(Camera camera, float partialTick, MultiBufferSource.BufferSource buffer) {
        if (QUEUE.isEmpty()) {
            return;
        }

        List<Entry> entries = new ArrayList<>(QUEUE);
        QUEUE.clear();
        SEEN.clear();

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

    public static void clear() {
        QUEUE.clear();
        SEEN.clear();
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

    private record Entry(DeferredParticle particle, double distanceToCameraSqr) {
    }
}
