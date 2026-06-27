package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.render.HbmOptimizedRenderShaders;
import com.hbm.ntm.client.render.HbmGlVaoSafety;
import com.hbm.ntm.client.render.HbmRenderFrameLight;
import com.hbm.ntm.client.render.HbmRenderFrameFlags;
import com.hbm.ntm.client.render.shader.HbmIrisExtendedShaderAccess;
import com.hbm.ntm.client.render.shader.HbmIrisRenderBatch;
import com.hbm.ntm.client.render.shader.HbmShaderCompatibilityDetector;
import com.hbm.ntm.client.renderer.LegacyRenderLighting;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.ARBDrawIndirect;
import org.lwjgl.opengl.ARBMultiDrawIndirect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Minimal modern carrier for the old HFRWavefrontObject group rendering path.
 */
public final class LegacyWavefrontModel {
    private static final Set<LegacyWavefrontModel> ALL_MODELS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final int SELECTION_CACHE_LIMIT = 256;
    private static final RenderBackend RENDER_BACKEND = new ExperimentalGpuPreparedRenderBackend(new CpuPreparedRenderBackend());
    private static final CacheMetrics CACHE_METRICS = new CacheMetrics();

    private final ResourceLocation modelLocation;
    private final ResourceLocation textureLocation;
    private final Map<String, List<Group>> groupsByName = new LinkedHashMap<>();
    private final List<Group> groupOrder = new ArrayList<>();
    private final Map<SelectionCacheKey, SelectionCacheEntry> selectionCache = new LinkedHashMap<>();
    private final Set<String> missingPartWarnings = new LinkedHashSet<>();
    private boolean smoothing = true;
    private boolean loaded;
    private boolean failed;
    private boolean mixedMode;
    private boolean vboRequested;
    private LegacyWavefrontModel vboView;
    private int selectionGeneration;
    private PreparedBatch allPreparedBatch;

    public LegacyWavefrontModel(ResourceLocation modelLocation, ResourceLocation textureLocation) {
        this(modelLocation, textureLocation, false);
    }

    public LegacyWavefrontModel(ResourceLocation modelLocation, ResourceLocation textureLocation, boolean mixedMode) {
        this.modelLocation = modelLocation;
        this.textureLocation = textureLocation;
        this.mixedMode = mixedMode;
        synchronized (ALL_MODELS) {
            ALL_MODELS.add(this);
        }
    }

    public LegacyWavefrontModel(ResourceLocation modelLocation) {
        this(modelLocation, InventoryMenu.BLOCK_ATLAS);
    }

    public LegacyWavefrontModel(ResourceLocation modelLocation, boolean mixedMode) {
        this(modelLocation, InventoryMenu.BLOCK_ATLAS, mixedMode);
    }

    public ResourceLocation modelLocation() {
        return modelLocation;
    }

    public ResourceLocation textureLocation() {
        return textureLocation;
    }

    public LegacyWavefrontModel noSmooth() {
        this.smoothing = false;
        return this;
    }

    public synchronized LegacyWavefrontModel mixedMode() {
        this.mixedMode = true;
        return this;
    }

    public synchronized LegacyWavefrontModel asVBO() {
        this.vboRequested = true;
        if (loaded && !failed) {
            prepareStaticGeometry();
        }
        if (vboView == null) {
            vboView = this;
        }
        return vboView;
    }

    public synchronized void renderPart(String partName, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay);
    }

    public synchronized void renderPart(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255);
    }

    public synchronized void renderPart(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false);
    }

    public synchronized void renderPartTranslucent(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false,
                LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE, UvTransform.DEFAULT);
    }

    public synchronized void renderPartAdditive(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, UvTransform.DEFAULT);
    }

    public synchronized void renderPartWithUvScroll(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float uOffset, float vOffset) {
        renderPartWithUvScroll(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255, uOffset, vOffset);
    }

    public synchronized void renderPartWithUvScroll(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, float uOffset, float vOffset) {
        renderPartWithUvTransform(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, 1.0F, 1.0F, uOffset, vOffset);
    }

    public synchronized void renderPartWithUvTransform(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            float uScale, float vScale, float uOffset, float vOffset) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, new UvTransform(uScale, 0.0F, 0.0F, vScale, uOffset, vOffset, 0.0F));
    }

    public synchronized void renderPartWithLegacyTextureMatrix(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            float uScale, float vScale, float uTranslate, float vTranslate) {
        renderPartWithUvTransform(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, uScale, vScale, uTranslate * uScale, vTranslate * vScale);
    }

    public synchronized void renderPartWithLegacyTextureMatrix(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            float uScale, float vScale, float rotationDegrees, float uTranslate, float vTranslate) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, legacyTextureMatrix(uScale, vScale, rotationDegrees, uTranslate, vTranslate));
    }

    public synchronized void renderPartWithLegacyTextureMatrixCull(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            float uScale, float vScale, float uTranslate, float vTranslate) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false,
                LegacyTexturedRenderMode.CUTOUT_CULL, legacyTextureMatrix(uScale, vScale, 0.0F, uTranslate, vTranslate));
    }

    public synchronized void renderPartGlintWithLegacyTextureMatrix(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            float uScale, float vScale, float rotationDegrees, float uTranslate, float vTranslate) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false,
                LegacyTexturedRenderMode.GLINT_EQUAL_DEPTH, legacyTextureMatrix(uScale, vScale, rotationDegrees, uTranslate, vTranslate));
    }

    public synchronized void renderPart(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        List<Group> groups = groupsByName.get(normalize(partName));
        boolean rendered = groups != null && !groups.isEmpty();
        if (rendered) {
            renderPreparedBatch(selectedBatch(SelectionCacheMode.ONLY, partName), textureLocation, poseStack, buffer,
                    packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
        }
        if (!rendered) {
            warnMissingPart(partName);
        }
    }

    public synchronized void renderPart(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        List<Group> groups = groupsByName.get(normalize(partName));
        boolean rendered = groups != null && !groups.isEmpty();
        if (rendered) {
            renderPreparedBatch(selectedBatch(SelectionCacheMode.ONLY, partName), textureLocation, poseStack, buffer,
                    packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
        }
        if (!rendered) {
            warnMissingPart(partName);
        }
    }

    public synchronized void renderWithSprite(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float yawRadians, float pitchRadians, float rollRadians, boolean legacyShadow) {
        renderWithSprite(sprite, poseStack, buffer, packedLight, packedOverlay, yawRadians, pitchRadians, rollRadians,
                255, 255, 255, 255, legacyShadow);
    }

    public synchronized void renderWithSprite(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float yawRadians, float pitchRadians, float rollRadians,
            int red, int green, int blue, int alpha, boolean legacyShadow) {
        renderWithSprite(sprite, poseStack, buffer, packedLight, packedOverlay, yawRadians, pitchRadians, rollRadians,
                red, green, blue, alpha, legacyShadow, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    public synchronized void renderWithSprite(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float yawRadians, float pitchRadians, float rollRadians,
            int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
        ensureLoaded();
        if (failed) {
            return;
        }
        poseStack.pushPose();
        LegacyObjTransforms.applyObjUtilRotation(poseStack, yawRadians, pitchRadians, rollRadians);
        renderPreparedBatchWithSprite(allPreparedBatch(), sprite, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, legacyShadow, false, renderMode, uvTransform);
        poseStack.popPose();
    }

    public synchronized void renderPartWithSprite(String partName, TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float yawRadians, float pitchRadians, float rollRadians, boolean legacyShadow) {
        renderPartWithSprite(partName, sprite, poseStack, buffer, packedLight, packedOverlay, yawRadians, pitchRadians, rollRadians,
                255, 255, 255, 255, legacyShadow);
    }

    public synchronized void renderPartWithSprite(String partName, TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float yawRadians, float pitchRadians, float rollRadians,
            int red, int green, int blue, int alpha, boolean legacyShadow) {
        renderPartWithSprite(partName, sprite, poseStack, buffer, packedLight, packedOverlay, yawRadians, pitchRadians, rollRadians,
                red, green, blue, alpha, legacyShadow, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    public synchronized void renderPartWithSprite(String partName, TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float yawRadians, float pitchRadians, float rollRadians,
            int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
        ensureLoaded();
        if (failed) {
            return;
        }
        poseStack.pushPose();
        LegacyObjTransforms.applyObjUtilRotation(poseStack, yawRadians, pitchRadians, rollRadians);
        renderPreparedBatchWithSprite(selectedBatch(SelectionCacheMode.LAST_GROUP, partName), sprite, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, true, renderMode, uvTransform);
        poseStack.popPose();
    }

    public synchronized void renderPartClipped(String name, ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
            double clipX, double clipY, double clipZ, double clipD) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        List<Group> groups = groupsByName.get(normalize(name));
        boolean rendered = groups != null && !groups.isEmpty();
        if (rendered) {
            PreparedBatch batch = PreparedBatch.clippedFrom(groups, "clipped:" + normalize(name),
                    clipX, clipY, clipZ, clipD);
            RENDER_BACKEND.renderTexturedTransient(batch, textureLocation, poseStack, buffer, packedLight,
                    packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform,
                    RenderBackendFallbackReason.TEXTURED_CLIPPED);
        }
        if (!rendered) {
            warnMissingPart(name);
        }
    }

    public synchronized void renderPartUntextured(String partName, PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha) {
        renderPartUntextured(partName, poseStack, buffer, red, green, blue, alpha, false);
    }

    public synchronized void renderPartUntextured(String partName, PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, boolean additive) {
        renderPartUntextured(partName, poseStack, buffer, red, green, blue, alpha,
                additive ? LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE : LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    public synchronized void renderPartUntextured(String partName, PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        List<Group> groups = groupsByName.get(normalize(partName));
        boolean rendered = groups != null && !groups.isEmpty();
        if (rendered) {
            renderPreparedBatchUntextured(selectedBatch(SelectionCacheMode.ONLY, partName), poseStack, buffer,
                    red, green, blue, alpha, renderMode);
        }
        if (!rendered) {
            warnMissingPart(partName);
        }
    }

    public synchronized void renderAll(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderAll(textureLocation, poseStack, buffer, packedLight, packedOverlay);
    }

    public synchronized void renderAll(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderAll(textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255);
    }

    public synchronized void renderAll(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha) {
        renderAll(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false);
    }

    public synchronized void renderAll(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow) {
        renderAll(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    public synchronized void renderAll(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, LegacyTexturedRenderMode renderMode) {
        renderAll(textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255, false,
                renderMode, UvTransform.DEFAULT);
    }

    public synchronized void renderAll(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        renderAll(textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255, false,
                renderMode, UvTransform.DEFAULT);
    }

    public synchronized void renderAll(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean translucent, float uOffset, float vOffset) {
        renderAll(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                renderMode(translucent), uvTransform(uOffset, vOffset));
    }

    public synchronized void renderAll(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        renderPreparedBatch(allPreparedBatch(), textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, smoothing, renderMode, uvTransform);
    }

    public synchronized void renderAllUntextured(PoseStack poseStack, MultiBufferSource buffer, int red, int green, int blue, int alpha) {
        renderAllUntextured(poseStack, buffer, red, green, blue, alpha, false);
    }

    public synchronized void renderAllUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, boolean additive) {
        renderAllUntextured(poseStack, buffer, red, green, blue, alpha,
                additive ? LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE : LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    private synchronized void renderAllUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        renderPreparedBatchUntextured(allPreparedBatch(), poseStack, buffer, red, green, blue, alpha, renderMode);
    }

    public static boolean renderUntexturedTransientQuad(PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode,
            double x0, double y0, double z0,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            double x3, double y3, double z3,
            int color, int alpha) {
        PreparedBatch batch = untexturedTransientQuadBatch(x0, y0, z0, x1, y1, z1, x2, y2, z2,
                x3, y3, z3);
        if (batch.empty()) {
            return false;
        }
        RENDER_BACKEND.renderUntexturedTransient(batch, poseStack, buffer,
                color >> 16 & 255, color >> 8 & 255, color & 255, alpha, renderMode,
                RenderBackendFallbackReason.UNTEXTURED_DIRECT_QUAD);
        return true;
    }

    public static boolean renderUntexturedTransientQuads(PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, List<UntexturedTransientQuad> quads, int color, int alpha) {
        PreparedBatch batch = untexturedTransientQuadBatch(quads);
        if (batch.empty()) {
            return false;
        }
        RENDER_BACKEND.renderUntexturedTransient(batch, poseStack, buffer,
                color >> 16 & 255, color >> 8 & 255, color & 255, alpha, renderMode,
                RenderBackendFallbackReason.UNTEXTURED_DIRECT_QUADS);
        return true;
    }

    public static boolean renderUntexturedVertexColorTransientQuad(PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode,
            double x0, double y0, double z0, int color0, int alpha0,
            double x1, double y1, double z1, int color1, int alpha1,
            double x2, double y2, double z2, int color2, int alpha2,
            double x3, double y3, double z3, int color3, int alpha3) {
        UntexturedVertexColorTransientQuad quad = new UntexturedVertexColorTransientQuad(
                vertexColor(x0, y0, z0, color0, alpha0),
                vertexColor(x1, y1, z1, color1, alpha1),
                vertexColor(x2, y2, z2, color2, alpha2),
                vertexColor(x3, y3, z3, color3, alpha3));
        RENDER_BACKEND.renderUntexturedVertexColorTransient(quad, poseStack, buffer, renderMode,
                RenderBackendFallbackReason.UNTEXTURED_DIRECT_VERTEX_COLOR_QUAD);
        return true;
    }

    public static boolean renderUntexturedVertexColorTransientTriangle(PoseStack poseStack,
            MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
            double x0, double y0, double z0, int color0, int alpha0,
            double x1, double y1, double z1, int color1, int alpha1,
            double x2, double y2, double z2, int color2, int alpha2) {
        UntexturedVertexColorTransientTriangle triangle = new UntexturedVertexColorTransientTriangle(
                vertexColor(x0, y0, z0, color0, alpha0),
                vertexColor(x1, y1, z1, color1, alpha1),
                vertexColor(x2, y2, z2, color2, alpha2));
        RENDER_BACKEND.renderUntexturedVertexColorTransientTriangle(triangle, poseStack, buffer, renderMode,
                RenderBackendFallbackReason.UNTEXTURED_DIRECT_VERTEX_COLOR_TRIANGLE);
        return true;
    }

    public static UntexturedVertexColorTransientTriangle untexturedVertexColorTriangle(
            double x0, double y0, double z0, int color0, int alpha0,
            double x1, double y1, double z1, int color1, int alpha1,
            double x2, double y2, double z2, int color2, int alpha2) {
        return new UntexturedVertexColorTransientTriangle(
                vertexColor(x0, y0, z0, color0, alpha0),
                vertexColor(x1, y1, z1, color1, alpha1),
                vertexColor(x2, y2, z2, color2, alpha2));
    }

    public static boolean renderUntexturedVertexColorTransientTriangles(PoseStack poseStack,
            MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
            List<UntexturedVertexColorTransientTriangle> triangles) {
        if (triangles == null || triangles.isEmpty()) {
            return false;
        }
        RENDER_BACKEND.renderUntexturedVertexColorTransientTriangles(triangles, poseStack, buffer, renderMode,
                RenderBackendFallbackReason.UNTEXTURED_DIRECT_VERTEX_COLOR_TRIANGLE);
        return true;
    }

    public static void emitUntexturedVertexColorTriangleIdentity(VertexConsumer consumer,
            double x0, double y0, double z0, int color0, int alpha0,
            double x1, double y1, double z1, int color1, int alpha1,
            double x2, double y2, double z2, int color2, int alpha2) {
        emitUntexturedVertexColorIdentity(consumer, vertexColor(x0, y0, z0, color0, alpha0));
        emitUntexturedVertexColorIdentity(consumer, vertexColor(x1, y1, z1, color1, alpha1));
        emitUntexturedVertexColorIdentity(consumer, vertexColor(x2, y2, z2, color2, alpha2));
    }

    public static boolean renderUntexturedLineTransientLines(PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, float lineWidth, List<UntexturedLineTransient> lines) {
        if (lines == null || lines.isEmpty()) {
            return false;
        }
        RENDER_BACKEND.renderUntexturedLineTransient(lines, poseStack, buffer, renderMode, lineWidth,
                RenderBackendFallbackReason.UNTEXTURED_DIRECT_LINES);
        return true;
    }

    public static boolean renderDoubleSidedUntexturedVertexColorTransientQuad(PoseStack poseStack,
            MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
            double x0, double y0, double z0, int color0, int alpha0,
            double x1, double y1, double z1, int color1, int alpha1,
            double x2, double y2, double z2, int color2, int alpha2,
            double x3, double y3, double z3, int color3, int alpha3) {
        RENDER_BACKEND.renderUntexturedVertexColorTransient(new UntexturedVertexColorTransientQuad(
                vertexColor(x0, y0, z0, color0, alpha0),
                vertexColor(x1, y1, z1, color1, alpha1),
                vertexColor(x2, y2, z2, color2, alpha2),
                vertexColor(x3, y3, z3, color3, alpha3)), poseStack, buffer, renderMode,
                RenderBackendFallbackReason.UNTEXTURED_DIRECT_VERTEX_COLOR_QUAD);
        RENDER_BACKEND.renderUntexturedVertexColorTransient(new UntexturedVertexColorTransientQuad(
                vertexColor(x3, y3, z3, color3, alpha3),
                vertexColor(x2, y2, z2, color2, alpha2),
                vertexColor(x1, y1, z1, color1, alpha1),
                vertexColor(x0, y0, z0, color0, alpha0)), poseStack, buffer, renderMode,
                RenderBackendFallbackReason.UNTEXTURED_DIRECT_VERTEX_COLOR_QUAD);
        return true;
    }

    public static boolean renderTexturedTransientQuad(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, float u0, float v0,
            double x1, double y1, double z1, float u1, float v1,
            double x2, double y2, double z2, float u2, float v2,
            double x3, double y3, double z3, float u3, float v3,
            int color, int alpha) {
        PreparedBatch batch = texturedTransientQuadBatch(normalX, normalY, normalZ,
                x0, y0, z0, u0, v0, x1, y1, z1, u1, v1, x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3);
        if (batch.empty()) {
            return false;
        }
        RENDER_BACKEND.renderTexturedTransient(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                color >> 16 & 255, color >> 8 & 255, color & 255, alpha, false, false, renderMode,
                UvTransform.DEFAULT, RenderBackendFallbackReason.TEXTURED_DIRECT_QUAD);
        return true;
    }

    public static boolean renderTexturedTransientBillboard(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, float u0, float v0,
            double x1, double y1, double z1, float u1, float v1,
            double x2, double y2, double z2, float u2, float v2,
            double x3, double y3, double z3, float u3, float v3,
            int color, int alpha) {
        PreparedBatch batch = texturedTransientQuadBatch(normalX, normalY, normalZ,
                x0, y0, z0, u0, v0, x1, y1, z1, u1, v1, x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3);
        if (batch.empty()) {
            return false;
        }
        RENDER_BACKEND.renderTexturedTransient(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                color >> 16 & 255, color >> 8 & 255, color & 255, alpha, false, false, renderMode,
                UvTransform.DEFAULT, RenderBackendFallbackReason.BILLBOARD_DIRECT_QUAD);
        return true;
    }

    public static boolean renderSpriteTransientQuad(TextureAtlasSprite sprite, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ, boolean unitUv,
            double x0, double y0, double z0, float u0, float v0,
            double x1, double y1, double z1, float u1, float v1,
            double x2, double y2, double z2, float u2, float v2,
            double x3, double y3, double z3, float u3, float v3,
            int color, int alpha) {
        float pixelScale = unitUv ? 16.0F : 1.0F;
        PreparedBatch batch = texturedTransientQuadBatch(normalX, normalY, normalZ,
                x0, y0, z0, sprite.getU(u0 * pixelScale), sprite.getV(v0 * pixelScale),
                x1, y1, z1, sprite.getU(u1 * pixelScale), sprite.getV(v1 * pixelScale),
                x2, y2, z2, sprite.getU(u2 * pixelScale), sprite.getV(v2 * pixelScale),
                x3, y3, z3, sprite.getU(u3 * pixelScale), sprite.getV(v3 * pixelScale));
        if (batch.empty()) {
            return false;
        }
        RENDER_BACKEND.renderTexturedTransient(batch, InventoryMenu.BLOCK_ATLAS, poseStack, buffer, packedLight,
                packedOverlay, color >> 16 & 255, color >> 8 & 255, color & 255, alpha, false, false,
                renderMode, UvTransform.DEFAULT, RenderBackendFallbackReason.SPRITE_DIRECT_QUAD);
        return true;
    }

    public synchronized void renderAllUntextured(VertexConsumer consumer, Matrix4f position, int red, int green, int blue, int alpha) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        for (Group group : groupOrder) {
            for (Face face : group.faces()) {
                emitFaceUntextured(face, consumer, position, red, green, blue, alpha);
            }
        }
    }

    public synchronized void renderOnly(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, String... groupNames) {
        renderOnly(textureLocation, poseStack, buffer, packedLight, packedOverlay, groupNames);
    }

    public synchronized void renderOnly(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, String... groupNames) {
        renderOnly(textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255, groupNames);
    }

    public synchronized void renderOnly(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, String... groupNames) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        renderPreparedBatch(selectedBatch(SelectionCacheMode.ONLY, groupNames), textureLocation, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, false, smoothing,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
        warnMissingNamedGroups(groupNames);
    }

    public synchronized void renderOnlyUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, String... groupNames) {
        renderOnlyUntextured(poseStack, buffer, red, green, blue, alpha, false, groupNames);
    }

    public synchronized void renderOnlyUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, boolean additive, String... groupNames) {
        renderOnlyUntextured(poseStack, buffer, red, green, blue, alpha,
                additive ? LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE : LegacyTexturedRenderMode.CUTOUT_NO_CULL,
                groupNames);
    }

    public synchronized void renderOnlyUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode, String... groupNames) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        renderPreparedBatchUntextured(selectedBatch(SelectionCacheMode.ONLY, groupNames), poseStack, buffer, red, green, blue, alpha, renderMode);
        warnMissingNamedGroups(groupNames);
    }

    public synchronized void renderOnlyUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, SelectionHandle selection) {
        renderSelectionUntextured(poseStack, buffer, red, green, blue, alpha,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, selection);
    }

    public synchronized void renderOnlyUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode, SelectionHandle selection) {
        renderSelectionUntextured(poseStack, buffer, red, green, blue, alpha, renderMode, selection);
    }

    public synchronized void renderOnlyUntexturedClipped(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode, SelectionHandle selection,
            double clipX, double clipY, double clipZ, double clipD) {
        renderSelectionUntexturedClipped(poseStack, buffer, red, green, blue, alpha, renderMode,
                selection, clipX, clipY, clipZ, clipD);
    }

    public synchronized void renderOnly(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, String... groupNames) {
        renderOnly(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT, groupNames);
    }

    public synchronized void renderOnly(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean translucent, float uOffset, float vOffset, String... groupNames) {
        renderOnly(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, renderMode(translucent), uvTransform(uOffset, vOffset), groupNames);
    }

    public synchronized void renderOnly(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform, String... groupNames) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        renderPreparedBatch(selectedBatch(SelectionCacheMode.ONLY, groupNames), textureLocation, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
        warnMissingNamedGroups(groupNames);
    }

    public SelectionHandle prepareRenderOnlyInCallOrder(String... groupNames) {
        return new SelectionHandle(SelectionCacheMode.CALL_ORDER, requestedNames(groupNames), normalizedList(groupNames),
                groupNames == null ? new String[0] : groupNames.clone());
    }

    public SelectionHandle prepareRenderOnly(String... groupNames) {
        return new SelectionHandle(SelectionCacheMode.ONLY, requestedNames(groupNames), normalizedList(groupNames),
                groupNames == null ? new String[0] : groupNames.clone());
    }

    public SelectionHandle prepareRenderAllExcept(String... excludedGroupNames) {
        return new SelectionHandle(SelectionCacheMode.ALL_EXCEPT, requestedNames(excludedGroupNames),
                normalizedList(excludedGroupNames),
                excludedGroupNames == null ? new String[0] : excludedGroupNames.clone());
    }

    public synchronized void renderOnlyInCallOrder(PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, SelectionHandle selection) {
        renderOnlyInCallOrder(textureLocation, poseStack, buffer, packedLight, packedOverlay, selection);
    }

    public synchronized void renderOnlyInCallOrder(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, SelectionHandle selection) {
        renderSelection(textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255,
                false, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT, selection);
    }

    public synchronized void renderOnlyInCallOrder(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, SelectionHandle selection,
            LegacyTexturedRenderMode renderMode) {
        renderSelection(textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255,
                false, renderMode, UvTransform.DEFAULT, selection);
    }

    public synchronized void renderOnlyInCallOrder(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
            SelectionHandle selection) {
        renderSelection(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, renderMode, uvTransform, selection);
    }

    public synchronized void renderOnlyInCallOrder(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue,
            int alpha, boolean legacyShadow, SelectionHandle selection) {
        renderSelection(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT, selection);
    }

    public synchronized void renderOnlyInCallOrderWithSprite(TextureAtlasSprite sprite, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, SelectionHandle selection) {
        renderSelectionWithSprite(sprite, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255,
                false, true, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT, selection);
    }

    public synchronized void renderOnlyInCallOrder(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, String... groupNames) {
        renderOnlyInCallOrder(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT, groupNames);
    }

    public synchronized void renderOnlyInCallOrder(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean translucent, float uOffset, float vOffset, String... groupNames) {
        renderOnlyInCallOrder(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, renderMode(translucent), uvTransform(uOffset, vOffset), groupNames);
    }

    public synchronized void renderOnlyInCallOrder(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform, String... groupNames) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        renderPreparedBatch(selectedBatch(SelectionCacheMode.CALL_ORDER, groupNames), textureLocation, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
        warnMissingNamedGroups(groupNames);
    }

    private void renderSelection(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform, SelectionHandle selection) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        renderPreparedBatch(selection.batch(this), textureLocation, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
        warnMissingNamedGroups(selection.rawNames());
    }

    private void renderSelectionWithSprite(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean partBrightness, LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
            SelectionHandle selection) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        renderPreparedBatchWithSprite(selection.batch(this), sprite, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, legacyShadow, partBrightness, renderMode, uvTransform);
        warnMissingNamedGroups(selection.rawNames());
    }

    private void renderSelectionUntextured(PoseStack poseStack, MultiBufferSource buffer, int red, int green,
            int blue, int alpha, LegacyTexturedRenderMode renderMode, SelectionHandle selection) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        renderPreparedBatchUntextured(selection.batch(this), poseStack, buffer, red, green, blue, alpha, renderMode);
        warnMissingNamedGroups(selection.rawNames());
    }

    private void renderSelectionUntexturedClipped(PoseStack poseStack, MultiBufferSource buffer, int red, int green,
            int blue, int alpha, LegacyTexturedRenderMode renderMode, SelectionHandle selection,
            double clipX, double clipY, double clipZ, double clipD) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        PreparedBatch batch = PreparedBatch.clippedFrom(selection.groups(this), "clipped-selection",
                clipX, clipY, clipZ, clipD);
        RENDER_BACKEND.renderUntexturedTransient(batch, poseStack, buffer, red, green, blue, alpha, renderMode,
                RenderBackendFallbackReason.UNTEXTURED_CLIPPED);
        warnMissingNamedGroups(selection.rawNames());
    }

    public synchronized void renderAllExcept(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, String... excludedGroupNames) {
        renderAllExcept(textureLocation, poseStack, buffer, packedLight, packedOverlay, excludedGroupNames);
    }

    public synchronized void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, String... excludedGroupNames) {
        renderAllExcept(textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255, excludedGroupNames);
    }

    public synchronized void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, String... excludedGroupNames) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        renderPreparedBatch(selectedBatch(SelectionCacheMode.ALL_EXCEPT, excludedGroupNames), textureLocation, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, false, smoothing,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    public synchronized void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, String... excludedGroupNames) {
        renderAllExcept(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT, excludedGroupNames);
    }

    public synchronized void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean translucent, float uOffset, float vOffset, String... excludedGroupNames) {
        renderAllExcept(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, renderMode(translucent), uvTransform(uOffset, vOffset), excludedGroupNames);
    }

    public synchronized void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform, String... excludedGroupNames) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        renderPreparedBatch(selectedBatch(SelectionCacheMode.ALL_EXCEPT, excludedGroupNames), textureLocation, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
    }

    public synchronized void renderAllExcept(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, SelectionHandle selection) {
        renderAllExcept(textureLocation, poseStack, buffer, packedLight, packedOverlay, selection);
    }

    public synchronized void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, SelectionHandle selection) {
        renderAllExcept(textureLocation, poseStack, buffer, packedLight, packedOverlay, selection,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    public synchronized void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, SelectionHandle selection,
            LegacyTexturedRenderMode renderMode) {
        renderAllExcept(textureLocation, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, false, renderMode, UvTransform.DEFAULT, selection);
    }

    public synchronized void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
            SelectionHandle selection) {
        renderSelection(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, renderMode, uvTransform, selection);
    }

    public synchronized void tessellateAll(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ensureLoaded();
        if (failed) {
            return;
        }
        renderPreparedBatch(allPreparedBatch(), textureLocation, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, false, smoothing, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    public synchronized void tessellateOnly(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, String... groupNames) {
        ensureLoaded();
        if (failed) {
            return;
        }
        renderPreparedBatch(selectedBatch(SelectionCacheMode.ONLY, groupNames), textureLocation, poseStack, buffer,
                packedLight, packedOverlay, 255, 255, 255, 255, false, smoothing,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
        warnMissingNamedGroups(groupNames);
    }

    public synchronized void tessellatePart(String partName, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ensureLoaded();
        if (failed) {
            return;
        }
        List<Group> groups = groupsByName.get(normalize(partName));
        boolean rendered = groups != null && !groups.isEmpty();
        if (rendered) {
            renderPreparedBatch(selectedBatch(SelectionCacheMode.ONLY, partName), textureLocation, poseStack, buffer,
                    packedLight, packedOverlay, 255, 255, 255, 255, false, smoothing,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
        }
        if (!rendered) {
            warnMissingPart(partName);
        }
    }

    public synchronized void tessellateAllExcept(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, String... excludedGroupNames) {
        ensureLoaded();
        if (failed) {
            return;
        }
        renderPreparedBatch(selectedBatch(SelectionCacheMode.ALL_EXCEPT, excludedGroupNames), textureLocation, poseStack, buffer,
                packedLight, packedOverlay, 255, 255, 255, 255, false, smoothing,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    public synchronized List<String> getPartNames() {
        ensureLoaded();
        if (failed) {
            return List.of();
        }
        return Collections.unmodifiableList(groupOrder.stream().map(Group::name).toList());
    }

    public synchronized boolean hasPart(String name) {
        ensureLoaded();
        return !failed && groupsByName.containsKey(normalize(name));
    }

    public synchronized boolean isLoaded() {
        return loaded;
    }

    public synchronized boolean hasFailed() {
        ensureLoaded();
        return failed;
    }

    public synchronized boolean smoothingEnabled() {
        return smoothing;
    }

    public synchronized boolean mixedModeEnabled() {
        return mixedMode;
    }

    public synchronized boolean vboRequested() {
        return vboRequested;
    }

    public static RenderBackendSnapshot renderBackendSnapshot() {
        return RENDER_BACKEND.snapshot();
    }

    public static RenderBackendAdditiveSnapshot renderBackendAdditiveSnapshot() {
        return RENDER_BACKEND.additiveSnapshot();
    }

    public static RenderBackendIrisSnapshot renderBackendIrisSnapshot() {
        return RENDER_BACKEND.irisSnapshot();
    }

    public static void endRenderBackendFrame() {
        RENDER_BACKEND.endFrame();
    }

    public static void clearRenderBackend(RenderBackendClearReason reason) {
        RENDER_BACKEND.clear(reason);
    }

    public static void flushRenderBackend(RenderBackendFlushStage stage) {
        RENDER_BACKEND.flush(stage);
    }

    private static ModelCacheSnapshot modelCacheSnapshot() {
        List<LegacyWavefrontModel> models;
        synchronized (ALL_MODELS) {
            models = new ArrayList<>(ALL_MODELS);
        }
        int loadedModels = 0;
        int failedModels = 0;
        int vboRequestedModels = 0;
        int mixedModeModels = 0;
        int smoothingDisabledModels = 0;
        int allPreparedBatchModels = 0;
        int groupCount = 0;
        int faceCount = 0;
        int faceVertices = 0;
        int selectionCacheEntries = 0;
        int selectionCacheEmptyEntries = 0;
        int selectionCachePreparedEntries = 0;
        int missingPartWarningEntries = 0;

        for (LegacyWavefrontModel model : models) {
            synchronized (model) {
                if (model.loaded) {
                    loadedModels++;
                }
                if (model.failed) {
                    failedModels++;
                }
                if (model.vboRequested) {
                    vboRequestedModels++;
                }
                if (model.mixedMode) {
                    mixedModeModels++;
                }
                if (!model.smoothing) {
                    smoothingDisabledModels++;
                }
                if (model.allPreparedBatch != null) {
                    allPreparedBatchModels++;
                }
                groupCount += model.groupOrder.size();
                for (Group group : model.groupOrder) {
                    faceCount += group.faces().size();
                    for (Face face : group.faces()) {
                        faceVertices += face.vertices().size();
                    }
                }
                selectionCacheEntries += model.selectionCache.size();
                for (SelectionCacheEntry entry : model.selectionCache.values()) {
                    if (entry.groups.isEmpty()) {
                        selectionCacheEmptyEntries++;
                    }
                    if (entry.batch != null) {
                        selectionCachePreparedEntries++;
                    }
                }
                missingPartWarningEntries += model.missingPartWarnings.size();
            }
        }

        return new ModelCacheSnapshot(
                models.size(),
                loadedModels,
                models.size() - loadedModels - failedModels,
                failedModels,
                vboRequestedModels,
                models.size() - vboRequestedModels,
                mixedModeModels,
                smoothingDisabledModels,
                allPreparedBatchModels,
                groupCount,
                faceCount,
                faceVertices,
                selectionCacheEntries,
                selectionCacheEmptyEntries,
                selectionCachePreparedEntries,
                missingPartWarningEntries);
    }

    public synchronized SelectionPlan renderAllPlan() {
        return selectionPlan(SelectionMode.ALL);
    }

    public synchronized SelectionPlan renderOnlyPlan(String... groupNames) {
        return selectionPlan(SelectionMode.ONLY, groupNames);
    }

    public synchronized SelectionPlan renderPartPlan(String partName) {
        return selectionPlan(SelectionMode.PART, partName);
    }

    public synchronized SelectionPlan renderAllExceptPlan(String... excludedGroupNames) {
        return selectionPlan(SelectionMode.ALL_EXCEPT, excludedGroupNames);
    }

    public synchronized SelectionPlan tessellateAllPlan() {
        return selectionPlan(SelectionMode.TESSELLATE_ALL);
    }

    public synchronized SelectionPlan tessellateOnlyPlan(String... groupNames) {
        return selectionPlan(SelectionMode.TESSELLATE_ONLY, groupNames);
    }

    public synchronized SelectionPlan tessellatePartPlan(String partName) {
        return selectionPlan(SelectionMode.TESSELLATE_PART, partName);
    }

    public synchronized SelectionPlan tessellateAllExceptPlan(String... excludedGroupNames) {
        return selectionPlan(SelectionMode.TESSELLATE_ALL_EXCEPT, excludedGroupNames);
    }

    public synchronized SelectionPlan selectionPlan(SelectionMode mode, String... groupNames) {
        ensureLoaded();
        List<String> requested = requestedNames(groupNames);
        if (failed) {
            return new SelectionPlan(modelLocation, textureLocation, mode, requested, List.of(), requested,
                    emptyBounds(), loaded, true, smoothing, mixedMode, vboRequested, mixedMode && mode.directRender());
        }

        Set<String> requestedKeys = normalizedSet(groupNames);
        List<Group> selectedGroups = switch (mode) {
            case ALL, TESSELLATE_ALL -> List.copyOf(groupOrder);
            case ONLY, PART, TESSELLATE_ONLY, TESSELLATE_PART -> selectedOnly(requestedKeys);
            case ALL_EXCEPT, TESSELLATE_ALL_EXCEPT -> selectedAllExcept(requestedKeys);
        };
        List<String> missing = missingRequested(requested, requestedKeys);
        return new SelectionPlan(modelLocation, textureLocation, mode, requested,
                selectedGroups.stream().map(Group::name).toList(), missing, selectedBounds(selectedGroups),
                loaded, false, smoothing, mixedMode, vboRequested, mixedMode && mode.directRender());
    }

    public synchronized AABB boundsAll() {
        ensureLoaded();
        if (failed) {
            return new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
        }
        return boundsOf(groupOrder);
    }

    public synchronized AABB boundsOnly(String... groupNames) {
        ensureLoaded();
        if (failed) {
            return new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
        }
        Set<String> included = normalizedSet(groupNames);
        List<Group> selected = new ArrayList<>();
        for (Group group : groupOrder) {
            if (included.contains(normalize(group.name()))) {
                selected.add(group);
            }
        }
        return selected.isEmpty() ? boundsAll() : boundsOf(selected);
    }

    public static void reloadAll(ResourceManager resourceManager) {
        List<LegacyWavefrontModel> models;
        synchronized (ALL_MODELS) {
            models = new ArrayList<>(ALL_MODELS);
        }
        LegacyTexturedRenderMode.clearCachedRenderTypes();
        RENDER_BACKEND.clear(RenderBackendClearReason.RESOURCE_RELOAD);
        for (LegacyWavefrontModel model : models) {
            model.reload(resourceManager);
        }
    }

    private static void renderPreparedBatch(PreparedBatch batch, ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
        RENDER_BACKEND.renderTextured(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
    }

    private static int renderGroupClipped(Group group, ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
            double clipX, double clipY, double clipZ, double clipD) {
        int emittedVertices = 0;
        VertexConsumer quadConsumer = null;
        VertexConsumer triangleConsumer = null;
        PoseStack.Pose pose = poseStack.last();
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, normal).withAlpha(alpha);
        for (Face face : group.faces()) {
            Face clipped = clipFace(face, clipX, clipY, clipZ, clipD);
            if (clipped != null) {
                int vertexCount = clipped.vertices().size();
                if (vertexCount == 3) {
                    if (triangleConsumer == null) {
                        triangleConsumer = buffer.getBuffer(alphaMode.renderType(textureLocation, VertexFormat.Mode.TRIANGLES));
                    }
                    emitFace(clipped, triangleConsumer, triangleConsumer, position, normal, packedLight, packedOverlay,
                            red, green, blue, alpha, legacyShadow, smoothing, uvTransform);
                } else if (vertexCount == 4) {
                    if (quadConsumer == null) {
                        quadConsumer = buffer.getBuffer(alphaMode.renderType(textureLocation, VertexFormat.Mode.QUADS));
                    }
                    emitFace(clipped, quadConsumer, quadConsumer, position, normal, packedLight, packedOverlay,
                            red, green, blue, alpha, legacyShadow, smoothing, uvTransform);
                } else {
                    if (triangleConsumer == null) {
                        triangleConsumer = buffer.getBuffer(alphaMode.renderType(textureLocation, VertexFormat.Mode.TRIANGLES));
                    }
                    emitFace(clipped, triangleConsumer, triangleConsumer, position, normal, packedLight, packedOverlay,
                            red, green, blue, alpha, legacyShadow, smoothing, uvTransform);
                }
                emittedVertices += emittedVertexCount(clipped);
            }
        }
        return emittedVertices;
    }

    private static void renderPreparedBatchWithSprite(PreparedBatch batch, TextureAtlasSprite sprite, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, boolean partBrightness, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
        RENDER_BACKEND.renderSprite(batch, sprite, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, legacyShadow, partBrightness, renderMode, uvTransform);
    }

    private static int renderGroupUntexturedClipped(Group group, PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode,
            double clipX, double clipY, double clipZ, double clipD) {
        int emittedVertices = 0;
        VertexConsumer quadConsumer = null;
        VertexConsumer triangleConsumer = null;
        PoseStack.Pose pose = poseStack.last();
        Matrix4f position = pose.pose();
        LegacyTexturedRenderMode resolvedRenderMode = renderModeForPose(renderMode, pose.normal());
        for (Face face : group.faces()) {
            Face clipped = clipFace(face, clipX, clipY, clipZ, clipD);
            if (clipped == null) {
                continue;
            }
            int vertexCount = clipped.vertices().size();
            if (vertexCount == 4) {
                if (quadConsumer == null) {
                    quadConsumer = buffer.getBuffer(
                            LegacyUntexturedQuadRenderer.type(resolvedRenderMode, alpha, VertexFormat.Mode.QUADS));
                }
                emitFaceUntextured(clipped, quadConsumer, triangleConsumer, position, red, green, blue, alpha);
            } else {
                if (triangleConsumer == null) {
                    triangleConsumer = buffer.getBuffer(
                            LegacyUntexturedQuadRenderer.type(resolvedRenderMode, alpha, VertexFormat.Mode.TRIANGLES));
                }
                emitFaceUntextured(clipped, quadConsumer, triangleConsumer, position, red, green, blue, alpha);
            }
            emittedVertices += emittedVertexCount(clipped);
        }
        return emittedVertices;
    }

    private static void renderPreparedBatchUntextured(PreparedBatch batch, PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
        RENDER_BACKEND.renderUntextured(batch, poseStack, buffer, red, green, blue, alpha, renderMode);
    }

    private static void emitPreparedVertices(List<PreparedVertex> vertices, VertexConsumer consumer, Matrix4f position,
            Matrix3f normal, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, boolean smoothing, UvTransform uvTransform) {
        for (PreparedVertex vertex : vertices) {
            emitPreparedVertex(vertex, consumer, position, normal, packedLight, packedOverlay,
                    red, green, blue, alpha, legacyShadow, smoothing, uvTransform);
        }
    }

    private static void emitPreparedVertex(PreparedVertex vertex, VertexConsumer consumer, Matrix4f position,
            Matrix3f normal, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, boolean smoothing, UvTransform uvTransform) {
        Vector3f vertexNormal = smoothing ? vertex.smoothNormal() : vertex.faceNormal();
        float shadow = legacyShadow ? legacyShadowFactor(normal, vertexNormal) : 1.0F;
        UV uv = vertex.uv();
        UV average = vertex.averageUv();
        Vector3f point = vertex.position();
        consumer.vertex(position, point.x(), point.y(), point.z())
                .color(clampColor(red * shadow), clampColor(green * shadow), clampColor(blue * shadow), alpha)
                .uv(transformU(uv, average, uvTransform), transformV(uv, average, uvTransform))
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, vertexNormal.x(), vertexNormal.y(), vertexNormal.z())
                .endVertex();
    }

    private static void emitPreparedVerticesWithSprite(List<PreparedVertex> vertices, TextureAtlasSprite sprite,
            VertexConsumer consumer, Matrix4f position, Matrix3f normal, int packedLight, int packedOverlay,
            int red, int green, int blue, int alpha, boolean legacyShadow, boolean partBrightness,
            UvTransform uvTransform) {
        for (PreparedVertex vertex : vertices) {
            Vector3f vertexNormal = vertex.faceNormal();
            float shadow = legacyShadow ? legacyObjUtilShadowFactor(normal, vertexNormal, partBrightness) : 1.0F;
            UV uv = vertex.uv();
            UV average = vertex.averageUv();
            Vector3f point = vertex.position();
            consumer.vertex(position, point.x(), point.y(), point.z())
                    .color(clampColor(red * shadow), clampColor(green * shadow), clampColor(blue * shadow), alpha)
                    .uv(sprite.getU(transformU(uv, average, uvTransform) * 16.0D),
                            sprite.getV(transformV(uv, average, uvTransform) * 16.0D))
                    .overlayCoords(packedOverlay)
                    .uv2(packedLight)
                    .normal(normal, vertexNormal.x(), vertexNormal.y(), vertexNormal.z())
                    .endVertex();
        }
    }

    private static void emitPreparedVerticesUntextured(List<PreparedVertex> vertices, VertexConsumer consumer,
            Matrix4f position, int red, int green, int blue, int alpha) {
        for (PreparedVertex vertex : vertices) {
            Vector3f point = vertex.position();
            consumer.vertex(position, point.x(), point.y(), point.z())
                    .color(red, green, blue, alpha)
                    .endVertex();
        }
    }
    private static void emitFace(Face face, VertexConsumer consumer, Matrix4f position, Matrix3f normal, int packedLight, int packedOverlay,
            int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing) {
        emitFace(face, consumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, smoothing, UvTransform.DEFAULT);
    }

    private static void emitFace(Face face, VertexConsumer consumer, Matrix4f position, Matrix3f normal, int packedLight, int packedOverlay,
            int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing, float uOffset, float vOffset) {
        emitFace(face, consumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, smoothing, uvTransform(uOffset, vOffset));
    }

    private static void emitFace(Face face, VertexConsumer consumer, Matrix4f position, Matrix3f normal, int packedLight, int packedOverlay,
            int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing, UvTransform uvTransform) {
        emitFace(face, consumer, consumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, smoothing, uvTransform);
    }

    private static int emittedVertexCount(Face face) {
        int vertexCount = face.vertices().size();
        if (vertexCount < 3) {
            return 0;
        }
        if (vertexCount <= 4) {
            return vertexCount;
        }
        return (vertexCount - 2) * 3;
    }

    private static void emitFace(Face face, VertexConsumer quadConsumer, VertexConsumer triangleConsumer, Matrix4f position, Matrix3f normal,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean smoothing, UvTransform uvTransform) {
        if (face.vertices().size() < 3) {
            return;
        }
        UV average = face.averageUv();
        if (face.vertices().size() == 3) {
            emitVertex(face, 0, triangleConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, uvTransform, average);
            emitVertex(face, 1, triangleConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, uvTransform, average);
            emitVertex(face, 2, triangleConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, uvTransform, average);
            return;
        }
        if (face.vertices().size() == 4) {
            emitVertex(face, 0, quadConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, uvTransform, average);
            emitVertex(face, 1, quadConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, uvTransform, average);
            emitVertex(face, 2, quadConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, uvTransform, average);
            emitVertex(face, 3, quadConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, uvTransform, average);
            return;
        }
        for (int i = 1; i + 1 < face.vertices().size(); i++) {
            emitVertex(face, 0, triangleConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, uvTransform, average);
            emitVertex(face, i, triangleConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, uvTransform, average);
            emitVertex(face, i + 1, triangleConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, uvTransform, average);
        }
    }

    private static void emitFaceWithSprite(Face face, TextureAtlasSprite sprite, VertexConsumer consumer, Matrix4f position, Matrix3f normal,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, boolean partBrightness) {
        emitFaceWithSprite(face, sprite, consumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, partBrightness, UvTransform.DEFAULT);
    }

    private static void emitFaceWithSprite(Face face, TextureAtlasSprite sprite, VertexConsumer consumer, Matrix4f position, Matrix3f normal,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean partBrightness, UvTransform uvTransform) {
        emitFaceWithSprite(face, sprite, consumer, consumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, partBrightness, uvTransform);
    }

    private static void emitFaceWithSprite(Face face, TextureAtlasSprite sprite, VertexConsumer quadConsumer, VertexConsumer triangleConsumer,
            Matrix4f position, Matrix3f normal, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, boolean partBrightness, UvTransform uvTransform) {
        if (face.vertices().size() < 3) {
            return;
        }
        UV average = face.averageUv();
        if (face.vertices().size() == 3) {
            emitVertexWithSprite(face, sprite, 0, triangleConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                    legacyShadow, partBrightness, uvTransform, average);
            emitVertexWithSprite(face, sprite, 1, triangleConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                    legacyShadow, partBrightness, uvTransform, average);
            emitVertexWithSprite(face, sprite, 2, triangleConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                    legacyShadow, partBrightness, uvTransform, average);
            return;
        }
        if (face.vertices().size() == 4) {
            emitVertexWithSprite(face, sprite, 0, quadConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                    legacyShadow, partBrightness, uvTransform, average);
            emitVertexWithSprite(face, sprite, 1, quadConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                    legacyShadow, partBrightness, uvTransform, average);
            emitVertexWithSprite(face, sprite, 2, quadConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                    legacyShadow, partBrightness, uvTransform, average);
            emitVertexWithSprite(face, sprite, 3, quadConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                    legacyShadow, partBrightness, uvTransform, average);
            return;
        }
        for (int i = 1; i + 1 < face.vertices().size(); i++) {
            emitVertexWithSprite(face, sprite, 0, triangleConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                    legacyShadow, partBrightness, uvTransform, average);
            emitVertexWithSprite(face, sprite, i, triangleConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                    legacyShadow, partBrightness, uvTransform, average);
            emitVertexWithSprite(face, sprite, i + 1, triangleConsumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                    legacyShadow, partBrightness, uvTransform, average);
        }
    }

    private static void emitFaceUntextured(Face face, VertexConsumer consumer, Matrix4f position,
            int red, int green, int blue, int alpha) {
        emitFaceUntextured(face, consumer, consumer, position, red, green, blue, alpha);
    }

    private static void emitFaceUntextured(Face face, VertexConsumer quadConsumer, VertexConsumer triangleConsumer,
            Matrix4f position, int red, int green, int blue, int alpha) {
        if (face.vertices().size() < 3) {
            return;
        }
        if (face.vertices().size() == 3) {
            emitVertexUntextured(face, 0, triangleConsumer, position, red, green, blue, alpha);
            emitVertexUntextured(face, 1, triangleConsumer, position, red, green, blue, alpha);
            emitVertexUntextured(face, 2, triangleConsumer, position, red, green, blue, alpha);
            return;
        }
        if (face.vertices().size() == 4) {
            emitVertexUntextured(face, 0, quadConsumer, position, red, green, blue, alpha);
            emitVertexUntextured(face, 1, quadConsumer, position, red, green, blue, alpha);
            emitVertexUntextured(face, 2, quadConsumer, position, red, green, blue, alpha);
            emitVertexUntextured(face, 3, quadConsumer, position, red, green, blue, alpha);
            return;
        }
        for (int i = 1; i + 1 < face.vertices().size(); i++) {
            emitVertexUntextured(face, 0, triangleConsumer, position, red, green, blue, alpha);
            emitVertexUntextured(face, i, triangleConsumer, position, red, green, blue, alpha);
            emitVertexUntextured(face, i + 1, triangleConsumer, position, red, green, blue, alpha);
        }
    }

    private static void emitVertex(Face face, int index, VertexConsumer consumer, Matrix4f position, Matrix3f normal,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing) {
        emitVertex(face, index, consumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, smoothing, UvTransform.DEFAULT, UV.ZERO);
    }

    private static void emitVertex(Face face, int index, VertexConsumer consumer, Matrix4f position, Matrix3f normal,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean smoothing, float uOffset, float vOffset) {
        emitVertex(face, index, consumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, smoothing, uvTransform(uOffset, vOffset), face.averageUv());
    }

    private static void emitVertex(Face face, int index, VertexConsumer consumer, Matrix4f position, Matrix3f normal,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean smoothing, UvTransform uvTransform, UV average) {
        Vector3f vertex = face.vertices().get(index);
        UV uv = index < face.uvs().size() ? face.uvs().get(index) : UV.ZERO;
        Vector3f vertexNormal = smoothing && index < face.normals().size() ? face.normals().get(index) : face.faceNormal();
        float shadow = legacyShadow ? legacyShadowFactor(normal, vertexNormal) : 1.0F;
        consumer.vertex(position, vertex.x(), vertex.y(), vertex.z())
                .color(clampColor(red * shadow), clampColor(green * shadow), clampColor(blue * shadow), alpha)
                .uv(transformU(uv, average, uvTransform), transformV(uv, average, uvTransform))
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, vertexNormal.x(), vertexNormal.y(), vertexNormal.z())
                .endVertex();
    }

    private static void emitVertexWithSprite(Face face, TextureAtlasSprite sprite, int index, VertexConsumer consumer, Matrix4f position,
            Matrix3f normal, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, boolean partBrightness) {
        emitVertexWithSprite(face, sprite, index, consumer, position, normal, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, partBrightness, UvTransform.DEFAULT, UV.ZERO);
    }

    private static void emitVertexWithSprite(Face face, TextureAtlasSprite sprite, int index, VertexConsumer consumer, Matrix4f position,
            Matrix3f normal, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, boolean partBrightness, UvTransform uvTransform, UV average) {
        Vector3f vertex = face.vertices().get(index);
        UV uv = index < face.uvs().size() ? face.uvs().get(index) : UV.ZERO;
        Vector3f vertexNormal = face.faceNormal();
        float shadow = legacyShadow ? legacyObjUtilShadowFactor(normal, vertexNormal, partBrightness) : 1.0F;
        consumer.vertex(position, vertex.x(), vertex.y(), vertex.z())
                .color(clampColor(red * shadow), clampColor(green * shadow), clampColor(blue * shadow), alpha)
                .uv(sprite.getU(transformU(uv, average, uvTransform) * 16.0D),
                        sprite.getV(transformV(uv, average, uvTransform) * 16.0D))
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, vertexNormal.x(), vertexNormal.y(), vertexNormal.z())
                .endVertex();
    }

    private static void emitVertexUntextured(Face face, int index, VertexConsumer consumer, Matrix4f position,
            int red, int green, int blue, int alpha) {
        Vector3f vertex = face.vertices().get(index);
        consumer.vertex(position, vertex.x(), vertex.y(), vertex.z())
                .color(red, green, blue, alpha)
                .endVertex();
    }

    private void ensureLoaded() {
        if (loaded || failed) {
            return;
        }
        loadFrom(Minecraft.getInstance().getResourceManager());
    }

    private synchronized void reload(ResourceManager resourceManager) {
        destroy();
        loadFrom(resourceManager);
    }

    private void loadFrom(ResourceManager resourceManager) {
        loaded = true;
        failed = false;
        try (InputStream stream = resourceManager.open(modelLocation)) {
            load(stream);
            if (vboRequested) {
                prepareStaticGeometry();
            }
        } catch (IOException | RuntimeException e) {
            destroy();
            loaded = true;
            failed = true;
            HbmNtm.LOGGER.warn("Unable to load legacy OBJ model {}", modelLocation, e);
        }
    }

    private void destroy() {
        groupsByName.clear();
        groupOrder.clear();
        selectionCache.clear();
        allPreparedBatch = null;
        missingPartWarnings.clear();
        loaded = false;
        failed = false;
        selectionGeneration++;
    }

    private void load(InputStream stream) throws IOException {
        List<Vector3f> vertices = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<UV> uvs = new ArrayList<>();
        Group currentGroup = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\\s+", " ").trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("v ")) {
                    vertices.add(parseVector(line));
                } else if (line.startsWith("vn ")) {
                    normals.add(parseVector(line));
                } else if (line.startsWith("vt ")) {
                    uvs.add(parseUV(line));
                } else if (line.startsWith("g ") || line.startsWith("o ")) {
                    String name = line.substring(2).trim();
                    currentGroup = new Group(name, new ArrayList<>());
                    registerGroup(currentGroup);
                } else if (line.startsWith("f ")) {
                    if (currentGroup == null) {
                        currentGroup = new Group("Default", new ArrayList<>());
                        registerGroup(currentGroup);
                    }
                    currentGroup.faces().add(parseFace(line, vertices, uvs, normals));
                }
            }
        }
    }

    private static Vector3f parseVector(String line) {
        String[] tokens = line.substring(line.indexOf(' ') + 1).split(" ");
        return new Vector3f(
                Float.parseFloat(tokens[0]),
                Float.parseFloat(tokens[1]),
                Float.parseFloat(tokens[2]));
    }

    private static UV parseUV(String line) {
        String[] tokens = line.substring(line.indexOf(' ') + 1).split(" ");
        return new UV(Float.parseFloat(tokens[0]), 1.0F - Float.parseFloat(tokens[1]));
    }

    private static Face parseFace(String line, List<Vector3f> vertices, List<UV> uvs, List<Vector3f> normals) {
        String[] tokens = line.substring(line.indexOf(' ') + 1).split(" ");
        List<Vector3f> faceVertices = new ArrayList<>(tokens.length);
        List<UV> faceUvs = new ArrayList<>(tokens.length);
        List<Vector3f> faceNormals = new ArrayList<>(tokens.length);

        for (String token : tokens) {
            String[] parts = token.split("/", -1);
            faceVertices.add(vertices.get(parseObjIndex(parts[0], vertices.size())));
            if (parts.length > 1 && !parts[1].isEmpty()) {
                faceUvs.add(uvs.get(parseObjIndex(parts[1], uvs.size())));
            }
            if (parts.length > 2 && !parts[2].isEmpty()) {
                faceNormals.add(normals.get(parseObjIndex(parts[2], normals.size())));
            }
        }
        Vector3f faceNormal = calculateFaceNormal(faceVertices);
        return new Face(faceVertices, faceUvs, faceNormals, faceNormal, averageUv(faceUvs));
    }

    private static Face clipFace(Face face, double clipX, double clipY, double clipZ, double clipD) {
        if (face.vertices().size() < 3) {
            return null;
        }
        List<ClippedVertex> input = new ArrayList<>(face.vertices().size());
        for (int index = 0; index < face.vertices().size(); index++) {
            Vector3f vertex = face.vertices().get(index);
            UV uv = index < face.uvs().size() ? face.uvs().get(index) : UV.ZERO;
            Vector3f normal = index < face.normals().size() ? face.normals().get(index) : face.faceNormal();
            input.add(new ClippedVertex(vertex, uv, normal));
        }
        List<ClippedVertex> clipped = clipVertices(input, clipX, clipY, clipZ, clipD);
        if (clipped.size() < 3) {
            return null;
        }
        List<Vector3f> vertices = new ArrayList<>(clipped.size());
        List<UV> uvs = new ArrayList<>(clipped.size());
        List<Vector3f> normals = new ArrayList<>(clipped.size());
        for (ClippedVertex vertex : clipped) {
            vertices.add(vertex.vertex());
            uvs.add(vertex.uv());
            normals.add(vertex.normal());
        }
        return new Face(vertices, uvs, normals, face.faceNormal(), averageUv(uvs));
    }

    private static List<ClippedVertex> clipVertices(List<ClippedVertex> input,
            double clipX, double clipY, double clipZ, double clipD) {
        List<ClippedVertex> output = new ArrayList<>();
        ClippedVertex previous = input.get(input.size() - 1);
        double previousDistance = clipDistance(previous.vertex(), clipX, clipY, clipZ, clipD);
        boolean previousInside = previousDistance >= -1.0E-6D;
        for (ClippedVertex current : input) {
            double currentDistance = clipDistance(current.vertex(), clipX, clipY, clipZ, clipD);
            boolean currentInside = currentDistance >= -1.0E-6D;
            if (currentInside != previousInside) {
                double divisor = previousDistance - currentDistance;
                double t = Math.abs(divisor) < 1.0E-12D ? 0.0D : previousDistance / divisor;
                output.add(interpolate(previous, current, t));
            }
            if (currentInside) {
                output.add(current.copy());
            }
            previous = current;
            previousDistance = currentDistance;
            previousInside = currentInside;
        }
        return output;
    }

    private static double clipDistance(Vector3f vertex, double clipX, double clipY, double clipZ, double clipD) {
        return clipX * vertex.x() + clipY * vertex.y() + clipZ * vertex.z() + clipD;
    }

    private static ClippedVertex interpolate(ClippedVertex start, ClippedVertex end, double t) {
        float clamped = (float) Math.max(0.0D, Math.min(1.0D, t));
        return new ClippedVertex(
                interpolate(start.vertex(), end.vertex(), clamped),
                interpolate(start.uv(), end.uv(), clamped),
                normalizeOrDefault(interpolate(start.normal(), end.normal(), clamped), start.normal()));
    }

    private static Vector3f interpolate(Vector3f start, Vector3f end, float t) {
        return new Vector3f(
                start.x() + (end.x() - start.x()) * t,
                start.y() + (end.y() - start.y()) * t,
                start.z() + (end.z() - start.z()) * t);
    }

    private static UV interpolate(UV start, UV end, float t) {
        return new UV(start.u() + (end.u() - start.u()) * t,
                start.v() + (end.v() - start.v()) * t);
    }

    private static Vector3f normalizeOrDefault(Vector3f value, Vector3f fallback) {
        return value.lengthSquared() < 1.0E-6F ? new Vector3f(fallback) : value.normalize();
    }

    private static int parseObjIndex(String value, int size) {
        int index = Integer.parseInt(value);
        return index > 0 ? index - 1 : size + index;
    }

    private static Vector3f calculateFaceNormal(List<Vector3f> vertices) {
        Vector3f edgeA = new Vector3f(vertices.get(1)).sub(vertices.get(0));
        Vector3f edgeB = new Vector3f(vertices.get(2)).sub(vertices.get(0));
        Vector3f calculated = edgeA.cross(edgeB);
        if (calculated.lengthSquared() < 1.0E-6F) {
            return new Vector3f(0.0F, 1.0F, 0.0F);
        }
        return calculated.normalize();
    }

    private static PreparedBatch untexturedTransientQuadBatch(
            double x0, double y0, double z0,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            double x3, double y3, double z3) {
        return untexturedTransientQuadBatch(List.of(new UntexturedTransientQuad(
                x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3)));
    }

    private static PreparedBatch untexturedTransientQuadBatch(List<UntexturedTransientQuad> quads) {
        if (quads == null || quads.isEmpty()) {
            return PreparedBatch.EMPTY;
        }
        List<PreparedVertex> vertices = new ArrayList<>(quads.size() * 4);
        for (UntexturedTransientQuad quad : quads) {
            addUntexturedTransientQuad(vertices, quad);
        }
        if (vertices.isEmpty()) {
            return PreparedBatch.EMPTY;
        }
        return new PreparedBatch("direct-untextured-quads:" + quads.size(), List.copyOf(vertices), List.of());
    }

    private static void addUntexturedTransientQuad(List<PreparedVertex> target, UntexturedTransientQuad quad) {
        List<Vector3f> positions = List.of(
                new Vector3f((float) quad.x0(), (float) quad.y0(), (float) quad.z0()),
                new Vector3f((float) quad.x1(), (float) quad.y1(), (float) quad.z1()),
                new Vector3f((float) quad.x2(), (float) quad.y2(), (float) quad.z2()),
                new Vector3f((float) quad.x3(), (float) quad.y3(), (float) quad.z3()));
        Vector3f normal = calculateFaceNormal(positions);
        UV averageUv = new UV(0.5F, 0.5F);
        for (Vector3f position : positions) {
            target.add(new PreparedVertex(position, UV.ZERO, normal, normal, averageUv));
        }
    }

    private static PreparedBatch texturedTransientQuadBatch(float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, float u0, float v0,
            double x1, double y1, double z1, float u1, float v1,
            double x2, double y2, double z2, float u2, float v2,
            double x3, double y3, double z3, float u3, float v3) {
        Vector3f normal = new Vector3f(normalX, normalY, normalZ);
        UV averageUv = new UV((u0 + u1 + u2 + u3) * 0.25F, (v0 + v1 + v2 + v3) * 0.25F);
        List<PreparedVertex> vertices = List.of(
                new PreparedVertex(new Vector3f((float) x0, (float) y0, (float) z0), new UV(u0, v0),
                        normal, normal, averageUv),
                new PreparedVertex(new Vector3f((float) x1, (float) y1, (float) z1), new UV(u1, v1),
                        normal, normal, averageUv),
                new PreparedVertex(new Vector3f((float) x2, (float) y2, (float) z2), new UV(u2, v2),
                        normal, normal, averageUv),
                new PreparedVertex(new Vector3f((float) x3, (float) y3, (float) z3), new UV(u3, v3),
                        normal, normal, averageUv));
        return new PreparedBatch("direct-textured-quad", vertices, List.of());
    }

    private static UntexturedVertexColor vertexColor(double x, double y, double z, int color, int alpha) {
        return new UntexturedVertexColor(x, y, z,
                color >> 16 & 255,
                color >> 8 & 255,
                color & 255,
                clampColor(alpha));
    }

    private static void renderUntexturedVertexColorTransientCpu(UntexturedVertexColorTransientQuad quad,
            PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode) {
        int alpha = quad.minimumAlpha();
        VertexConsumer consumer = buffer.getBuffer(LegacyUntexturedQuadRenderer.type(renderMode, alpha));
        Matrix4f position = poseStack.last().pose();
        emitUntexturedVertexColor(consumer, position, quad.v0());
        emitUntexturedVertexColor(consumer, position, quad.v1());
        emitUntexturedVertexColor(consumer, position, quad.v2());
        emitUntexturedVertexColor(consumer, position, quad.v3());
    }

    private static void renderUntexturedVertexColorTransientCpu(UntexturedVertexColorTransientTriangle triangle,
            PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode) {
        int alpha = triangle.minimumAlpha();
        VertexConsumer consumer = buffer.getBuffer(LegacyUntexturedQuadRenderer.type(renderMode, alpha,
                VertexFormat.Mode.TRIANGLES));
        Matrix4f position = poseStack.last().pose();
        emitUntexturedVertexColor(consumer, position, triangle.v0());
        emitUntexturedVertexColor(consumer, position, triangle.v1());
        emitUntexturedVertexColor(consumer, position, triangle.v2());
    }

    private static void renderUntexturedVertexColorTransientTrianglesCpu(
            List<UntexturedVertexColorTransientTriangle> triangles,
            PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode) {
        if (triangles.isEmpty()) {
            return;
        }
        int alpha = minimumTriangleAlpha(triangles);
        VertexConsumer consumer = buffer.getBuffer(LegacyUntexturedQuadRenderer.type(renderMode, alpha,
                VertexFormat.Mode.TRIANGLES));
        Matrix4f position = poseStack.last().pose();
        for (UntexturedVertexColorTransientTriangle triangle : triangles) {
            emitUntexturedVertexColor(consumer, position, triangle.v0());
            emitUntexturedVertexColor(consumer, position, triangle.v1());
            emitUntexturedVertexColor(consumer, position, triangle.v2());
        }
    }

    private static int minimumTriangleAlpha(List<UntexturedVertexColorTransientTriangle> triangles) {
        int alpha = 255;
        for (UntexturedVertexColorTransientTriangle triangle : triangles) {
            alpha = Math.min(alpha, triangle.minimumAlpha());
        }
        return alpha;
    }

    private static void renderUntexturedLineTransientCpu(List<UntexturedLineTransient> lines,
            PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode, float lineWidth) {
        if (lines.isEmpty()) {
            return;
        }
        int alpha = minimumLineAlpha(lines);
        VertexConsumer consumer = buffer.getBuffer(LegacyLineRenderer.type(lineWidth, renderMode, alpha));
        PoseStack.Pose pose = poseStack.last();
        for (UntexturedLineTransient line : lines) {
            LegacyLineRenderer.line(consumer, pose, line.x0(), line.y0(), line.z0(), line.x1(), line.y1(), line.z1(),
                    line.color(), line.alpha());
        }
    }

    private static int minimumLineAlpha(List<UntexturedLineTransient> lines) {
        int alpha = 255;
        for (UntexturedLineTransient line : lines) {
            alpha = Math.min(alpha, line.alpha());
        }
        return alpha;
    }

    private static void emitUntexturedVertexColor(VertexConsumer consumer, Matrix4f position,
            UntexturedVertexColor vertex) {
        consumer.vertex(position, (float) vertex.x(), (float) vertex.y(), (float) vertex.z())
                .color(vertex.red(), vertex.green(), vertex.blue(), vertex.alpha())
                .endVertex();
    }

    private static void emitUntexturedVertexColorIdentity(VertexConsumer consumer, UntexturedVertexColor vertex) {
        consumer.vertex((float) vertex.x(), (float) vertex.y(), (float) vertex.z())
                .color(vertex.red(), vertex.green(), vertex.blue(), vertex.alpha())
                .endVertex();
    }

    private static String normalize(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }

    private void registerGroup(Group group) {
        groupOrder.add(group);
        groupsByName.computeIfAbsent(normalize(group.name()), ignored -> new ArrayList<>()).add(group);
    }

    private void prepareStaticGeometry() {
        for (Group group : groupOrder) {
            group.ensurePrepared();
        }
        allPreparedBatch = PreparedBatch.from(groupOrder, stableAllBatchKey());
        CACHE_METRICS.recordAllPreparedBatchBuild();
    }

    private PreparedBatch allPreparedBatch() {
        if (allPreparedBatch == null) {
            allPreparedBatch = PreparedBatch.from(groupOrder, stableAllBatchKey());
            CACHE_METRICS.recordAllPreparedBatchBuild();
        }
        return allPreparedBatch;
    }

    private List<Group> selectedGroups(SelectionCacheMode mode, String... names) {
        return selectedEntry(mode, names).groups();
    }

    private PreparedBatch selectedBatch(SelectionCacheMode mode, String... names) {
        return selectedEntry(mode, names).batch();
    }

    private SelectionCacheEntry selectedEntry(SelectionCacheMode mode, String... names) {
        SelectionCacheKey key = new SelectionCacheKey(mode, normalizedList(names));
        SelectionCacheEntry cached = selectionCache.get(key);
        if (cached != null) {
            CACHE_METRICS.recordSelectionCacheHit();
            return cached;
        }
        CACHE_METRICS.recordSelectionCacheMiss();
        List<Group> groups = createSelectedGroups(key);
        SelectionCacheEntry selected = new SelectionCacheEntry(groups, stableSelectionBatchKey(key, groups));
        if (selected.groups().isEmpty()) {
            CACHE_METRICS.recordSelectionCacheEmptyBuild();
        }
        if (selectionCache.size() >= SELECTION_CACHE_LIMIT) {
            selectionCache.clear();
            CACHE_METRICS.recordSelectionCacheClear();
        }
        selectionCache.put(key, selected);
        return selected;
    }

    private String stableAllBatchKey() {
        return stableBatchKey("ALL", List.of(), groupOrder);
    }

    private String stableSelectionBatchKey(SelectionCacheKey key, List<Group> groups) {
        return stableBatchKey(key.mode().name(), key.names(), groups);
    }

    private String stableBatchKey(String mode, List<String> requestedNames, List<Group> groups) {
        StringBuilder builder = new StringBuilder(modelLocation.toString())
                .append('|')
                .append(mode)
                .append('|');
        for (String name : requestedNames) {
            builder.append(name).append(',');
        }
        builder.append("|groups=");
        for (Group group : groups) {
            builder.append(group.name()).append(',');
        }
        return builder.toString();
    }

    private List<Group> createSelectedGroups(SelectionCacheKey key) {
        return switch (key.mode()) {
            case ONLY -> selectedOnly(new LinkedHashSet<>(key.names()));
            case ALL_EXCEPT -> selectedAllExcept(new LinkedHashSet<>(key.names()));
            case CALL_ORDER -> selectedInCallOrder(key.names());
            case LAST_GROUP -> selectedLastGroup(key.names());
        };
    }

    private List<Group> selectedLastGroup(List<String> names) {
        if (names.isEmpty()) {
            return List.of();
        }
        List<Group> groups = groupsByName.get(names.get(0));
        if (groups == null || groups.isEmpty()) {
            return List.of();
        }
        return List.of(groups.get(groups.size() - 1));
    }

    private List<Group> selectedInCallOrder(List<String> names) {
        List<Group> selected = new ArrayList<>();
        for (String key : names) {
            List<Group> groups = groupsByName.get(key);
            if (groups != null && !groups.isEmpty()) {
                selected.addAll(groups);
            }
        }
        return List.copyOf(selected);
    }

    private static List<String> normalizedList(String... names) {
        if (names == null || names.length == 0) {
            return List.of();
        }
        return Arrays.stream(names).map(LegacyWavefrontModel::normalize).toList();
    }

    private static Set<String> normalizedSet(String... names) {
        Set<String> normalized = new LinkedHashSet<>();
        if (names == null) {
            return normalized;
        }
        for (String name : names) {
            normalized.add(normalize(name));
        }
        return normalized;
    }

    private static LegacyTexturedRenderMode renderMode(boolean translucent) {
        return translucent ? LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE : LegacyTexturedRenderMode.CUTOUT_NO_CULL;
    }

    private static LegacyTexturedRenderMode renderModeForPose(LegacyTexturedRenderMode renderMode, Matrix3f normal) {
        if (normal.determinant() >= 0.0F) {
            return renderMode;
        }
        return switch (renderMode) {
            case CUTOUT_CULL -> LegacyTexturedRenderMode.CUTOUT_REVERSED_CULL;
            case CUTOUT_REVERSED_CULL -> LegacyTexturedRenderMode.CUTOUT_CULL;
            case ADDITIVE_CULL_NO_DEPTH_WRITE -> LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE;
            default -> renderMode;
        };
    }

    private static UvTransform uvTransform(float uOffset, float vOffset) {
        if (uOffset == 0.0F && vOffset == 0.0F) {
            return UvTransform.DEFAULT;
        }
        return new UvTransform(1.0F, 0.0F, 0.0F, 1.0F, uOffset, vOffset, 0.0F);
    }

    public static UvTransform legacyTextureMatrix(float uScale, float vScale, float uTranslate, float vTranslate) {
        return legacyTextureMatrix(uScale, vScale, 0.0F, uTranslate, vTranslate);
    }

    public static UvTransform legacyTextureMatrix(float uScale, float vScale, float rotationDegrees, float uTranslate, float vTranslate) {
        return legacyTextureMatrix(uScale, vScale, rotationDegrees, uTranslate, vTranslate, true);
    }

    public static UvTransform legacyTextureMatrixDynamic(float uScale, float vScale, float uTranslate, float vTranslate) {
        return legacyTextureMatrixDynamic(uScale, vScale, 0.0F, uTranslate, vTranslate);
    }

    public static UvTransform legacyTextureMatrixDynamic(float uScale, float vScale, float rotationDegrees,
            float uTranslate, float vTranslate) {
        return legacyTextureMatrix(uScale, vScale, rotationDegrees, uTranslate, vTranslate, false);
    }

    private static UvTransform legacyTextureMatrix(float uScale, float vScale, float rotationDegrees,
            float uTranslate, float vTranslate, boolean gpuMeshCacheable) {
        float radians = (float) Math.toRadians(rotationDegrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        return new UvTransform(
                uScale * cos,
                -uScale * sin,
                vScale * sin,
                vScale * cos,
                uScale * (cos * uTranslate - sin * vTranslate),
                vScale * (sin * uTranslate + cos * vTranslate),
                0.0F,
                gpuMeshCacheable);
    }

    private static UV averageUv(List<UV> uvs) {
        if (uvs.isEmpty()) {
            return UV.ZERO;
        }
        float u = 0.0F;
        float v = 0.0F;
        for (UV uv : uvs) {
            u += uv.u();
            v += uv.v();
        }
        return new UV(u / uvs.size(), v / uvs.size());
    }

    private static float transformU(UV uv, UV average, UvTransform transform) {
        return uv.u() * transform.uScale()
                + uv.v() * transform.uFromV()
                + transform.uOffset()
                + legacyTextureOffset(uv.u(), average.u(), transform.textureOffset());
    }

    private static float transformV(UV uv, UV average, UvTransform transform) {
        return uv.u() * transform.vFromU()
                + uv.v() * transform.vScale()
                + transform.vOffset()
                + legacyTextureOffset(uv.v(), average.v(), transform.textureOffset());
    }

    private static float legacyTextureOffset(float value, float average, float textureOffset) {
        if (textureOffset == 0.0F) {
            return 0.0F;
        }
        return value > average ? -textureOffset : textureOffset;
    }

    private void rejectMixedModeDirectRender() {
        if (mixedMode) {
            throw new UnsupportedOperationException("Rendering of mixed-mode model " + modelLocation + " is not supported!");
        }
    }

    private void warnMissingParts(Set<String> requested, Set<String> rendered) {
        for (String key : requested) {
            if (!rendered.contains(key)) {
                warnMissingPart(key);
            }
        }
    }

    private void warnMissingNamedGroups(String... names) {
        if (names == null) {
            return;
        }
        for (String name : names) {
            String key = normalize(name);
            if (!key.isEmpty() && !groupsByName.containsKey(key)) {
                warnMissingPart(name);
            }
        }
    }

    private void warnMissingPart(String partName) {
        String key = normalize(partName);
        if (key.isEmpty() || missingPartWarnings.contains(key)) {
            return;
        }
        missingPartWarnings.add(key);
        HbmNtm.LOGGER.warn("Legacy OBJ model {} has no group '{}'. Known groups: {}", modelLocation, partName, getPartNames());
    }

    private List<Group> selectedOnly(Set<String> included) {
        List<Group> selected = new ArrayList<>();
        for (Group group : groupOrder) {
            if (included.contains(normalize(group.name()))) {
                selected.add(group);
            }
        }
        return List.copyOf(selected);
    }

    private List<Group> selectedAllExcept(Set<String> excluded) {
        List<Group> selected = new ArrayList<>();
        for (Group group : groupOrder) {
            if (!excluded.contains(normalize(group.name()))) {
                selected.add(group);
            }
        }
        return List.copyOf(selected);
    }

    private List<String> missingRequested(List<String> requested, Set<String> requestedKeys) {
        if (requestedKeys.isEmpty()) {
            return List.of();
        }
        Set<String> found = new LinkedHashSet<>();
        for (Group group : groupOrder) {
            String key = normalize(group.name());
            if (requestedKeys.contains(key)) {
                found.add(key);
            }
        }
        List<String> missing = new ArrayList<>();
        for (String name : requested) {
            String key = normalize(name);
            if (!key.isEmpty() && !found.contains(key)) {
                missing.add(name);
            }
        }
        return List.copyOf(missing);
    }

    private static List<String> requestedNames(String... names) {
        if (names == null || names.length == 0) {
            return List.of();
        }
        List<String> requested = new ArrayList<>(names.length);
        for (String name : names) {
            requested.add(name);
        }
        return List.copyOf(requested);
    }

    private static AABB selectedBounds(List<Group> selectedGroups) {
        return selectedGroups.isEmpty() ? emptyBounds() : boundsOf(selectedGroups);
    }

    private static AABB emptyBounds() {
        return new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static AABB boundsOf(List<Group> groups) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        boolean found = false;

        for (Group group : groups) {
            for (Face face : group.faces()) {
                for (Vector3f vertex : face.vertices()) {
                    minX = Math.min(minX, vertex.x());
                    minY = Math.min(minY, vertex.y());
                    minZ = Math.min(minZ, vertex.z());
                    maxX = Math.max(maxX, vertex.x());
                    maxY = Math.max(maxY, vertex.y());
                    maxZ = Math.max(maxZ, vertex.z());
                    found = true;
                }
            }
        }

        return found ? new AABB(minX, minY, minZ, maxX, maxY, maxZ) : new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    }

    private static int clampColor(float value) {
        return Math.max(0, Math.min(255, Math.round(value)));
    }

    private static float legacyShadowFactor(Matrix3f normalMatrix, Vector3f faceNormal) {
        Vector3f transformed = new Vector3f(faceNormal).mul(normalMatrix);
        float brightness = (transformed.y() + 0.7F) * 0.9F - Math.abs(transformed.x()) * 0.1F + Math.abs(transformed.z()) * 0.1F;
        return Math.max(0.45F, brightness);
    }

    private static float legacyObjUtilShadowFactor(Matrix3f normalMatrix, Vector3f faceNormal, boolean partBrightness) {
        Vector3f transformed = new Vector3f(faceNormal).mul(normalMatrix);
        return partBrightness
                ? LegacyObjTransforms.objUtilPartShadowFactor(transformed)
                : LegacyObjTransforms.objUtilAllShadowFactor(transformed);
    }

    public enum SelectionMode {
        ALL(true),
        ONLY(true),
        PART(true),
        ALL_EXCEPT(true),
        TESSELLATE_ALL(false),
        TESSELLATE_ONLY(false),
        TESSELLATE_PART(false),
        TESSELLATE_ALL_EXCEPT(false);

        private final boolean directRender;

        SelectionMode(boolean directRender) {
            this.directRender = directRender;
        }

        public boolean directRender() {
            return directRender;
        }
    }

    public record SelectionPlan(
            ResourceLocation modelLocation,
            ResourceLocation textureLocation,
            SelectionMode mode,
            List<String> requestedParts,
            List<String> selectedParts,
            List<String> missingParts,
            AABB selectedBounds,
            boolean loaded,
            boolean failed,
            boolean smoothing,
            boolean mixedMode,
            boolean vboRequested,
            boolean mixedModeDirectRenderUnsupported) {
    }

    public record RenderBackendSnapshot(
            String name,
            boolean gpuBacked,
            boolean reloadClearHook,
            RenderBackendCapabilities capabilities,
            ModelCacheSnapshot modelCache,
            long groupPreparedBuilds,
            long groupPreparedVertices,
            long allPreparedBatchBuilds,
            long selectionCacheHits,
            long selectionCacheMisses,
            long selectionCacheClears,
            long selectionCacheEmptyBuilds,
            long selectionCachePreparedBatchBuilds,
            long selectionHandleRefreshes,
            long selectionHandleEmptyBuilds,
            long selectionHandlePreparedBatchBuilds,
            long reloadClears,
            long backendClears,
            long clientDisconnectClears,
            long shaderReloadClears,
            long manualClears,
            long backendClearGeneration,
            RenderBackendClearReason lastClearReason,
            long backendFlushes,
            long afterBlockEntitiesFlushes,
            long manualFlushes,
            long backendFlushGeneration,
            RenderBackendFlushStage lastFlushStage,
            long texturedBatches,
            long texturedVertices,
            long spriteBatches,
            long spriteVertices,
            long untexturedBatches,
            long untexturedVertices,
            long estimatedDrawCalls,
            long texturedDrawCalls,
            long spriteDrawCalls,
            long untexturedDrawCalls,
            long textureSwitches,
            long renderModeSwitches,
            long cpuFallbackBatches,
            long cpuFallbackVertices,
            long texturedClippedFallbackBatches,
            long texturedClippedFallbackVertices,
            long untexturedClippedFallbackBatches,
            long untexturedClippedFallbackVertices,
            RenderBackendFallbackReason lastFallbackReason,
            long frameGeneration,
            long currentFrameTexturedBatches,
            long currentFrameTexturedVertices,
            long currentFrameSpriteBatches,
            long currentFrameSpriteVertices,
            long currentFrameUntexturedBatches,
            long currentFrameUntexturedVertices,
            long currentFrameCpuFallbackBatches,
            long currentFrameCpuFallbackVertices,
            long currentFrameBackendFlushes,
            long currentFrameEstimatedDrawCalls,
            long currentFrameTextureSwitches,
            int gpuMeshEntries,
            long gpuBufferBytes,
            long gpuUploadAttempts,
            long gpuUploadFailures,
            long gpuDrawCalls,
            long gpuFallbackBatches,
            long gpuFallbackVertices,
            long instancedQueuedBatches,
            long instancedQueuedInstances,
            long instancedFlushes,
            long instancedDrawCalls,
            long instancedFallbackBatches,
            long instancedFallbackInstances,
            long instancedOverflowBatches,
            long instancedOverflowInstances,
            boolean mdiAvailable,
            boolean mdiDrawIndirectSupported,
            boolean mdiMultiDrawIndirectSupported,
            boolean mdiBaseInstanceSupported,
            long mdiEligibleFlushes,
            long mdiEligibleBatches,
            long mdiFallbackFlushes,
            long mdiFallbackBatches,
            long mdiDrawCalls,
            long mdiMultiDrawCalls,
            long mdiIndirectCommands,
            int mdiAtlasParts,
            long mdiAtlasBytes,
            long mdiNoSlotBatches,
            long mdiNoSlotInstances,
            long currentFrameMdiEligibleFlushes,
            long currentFrameMdiEligibleBatches,
            long currentFrameMdiFallbackFlushes,
            long currentFrameMdiFallbackBatches,
            long currentFrameMdiDrawCalls,
            long currentFrameMdiIndirectCommands,
            long currentFrameMdiNoSlotBatches,
            long currentFrameMdiNoSlotInstances,
            long lastFrameMdiEligibleFlushes,
            long lastFrameMdiEligibleBatches,
            long lastFrameMdiFallbackFlushes,
            long lastFrameMdiFallbackBatches,
            long lastFrameMdiDrawCalls,
            long lastFrameMdiIndirectCommands,
            long lastFrameMdiNoSlotBatches,
            long lastFrameMdiNoSlotInstances,
            long currentFrameInstancedQueuedBatches,
            long currentFrameInstancedQueuedInstances,
            long currentFrameInstancedDrawCalls,
            long currentFrameInstancedOverflowBatches,
            long currentFrameInstancedOverflowInstances,
            long lastFrameInstancedQueuedBatches,
            long lastFrameInstancedQueuedInstances,
            long lastFrameInstancedDrawCalls,
            long lastFrameInstancedOverflowBatches,
            long lastFrameInstancedOverflowInstances,
            long currentFrameGpuDrawCalls,
            long currentFrameGpuFallbackBatches,
            long currentFrameGpuFallbackVertices,
            long lastFrameGpuDrawCalls,
            long lastFrameGpuFallbackBatches,
            long lastFrameGpuFallbackVertices,
            long lastFrameTexturedBatches,
            long lastFrameTexturedVertices,
            long lastFrameSpriteBatches,
            long lastFrameSpriteVertices,
            long lastFrameUntexturedBatches,
            long lastFrameUntexturedVertices,
            long lastFrameCpuFallbackBatches,
            long lastFrameCpuFallbackVertices,
            long lastFrameBackendFlushes,
            long lastFrameEstimatedDrawCalls,
            long lastFrameTextureSwitches) {
    }

    public record RenderBackendAdditiveSnapshot(
            InstancedAdditiveSnapshot instanced,
            MdiAdditiveSnapshot mdi) {

        public static final RenderBackendAdditiveSnapshot EMPTY = new RenderBackendAdditiveSnapshot(
                InstancedAdditiveSnapshot.EMPTY, MdiAdditiveSnapshot.EMPTY);
    }

    public record RenderBackendIrisSnapshot(
            int meshEntries,
            long meshBytes,
            long eligibleBatches,
            long drawCalls,
            long fallbackBatches,
            long fallbackVertices,
            long uploadAttempts,
            long uploadFailures,
            long currentFrameEligibleBatches,
            long currentFrameDrawCalls,
            long currentFrameFallbackBatches,
            long currentFrameFallbackVertices,
            long lastFrameEligibleBatches,
            long lastFrameDrawCalls,
            long lastFrameFallbackBatches,
            long lastFrameFallbackVertices,
            long shadowDrawCalls,
            long currentFrameShadowDrawCalls,
            long lastFrameShadowDrawCalls,
            HbmIrisRenderBatch.Snapshot persistentBatch,
            IrisCompanionQueueSnapshot queuedFlush) {
        public static final RenderBackendIrisSnapshot EMPTY = new RenderBackendIrisSnapshot(
                0, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L,
                0L, 0L, 0L,
                HbmIrisRenderBatch.Snapshot.EMPTY,
                IrisCompanionQueueSnapshot.EMPTY);
    }

    public record IrisCompanionQueueSnapshot(
            long queuedBatches,
            long queuedInstances,
            long flushes,
            long drawCalls,
            long fallbackBatches,
            long fallbackInstances,
            long currentFrameQueuedBatches,
            long currentFrameQueuedInstances,
            long currentFrameFlushes,
            long currentFrameDrawCalls,
            long currentFrameFallbackBatches,
            long currentFrameFallbackInstances,
            long lastFrameQueuedBatches,
            long lastFrameQueuedInstances,
            long lastFrameFlushes,
            long lastFrameDrawCalls,
            long lastFrameFallbackBatches,
            long lastFrameFallbackInstances) {
        public static final IrisCompanionQueueSnapshot EMPTY = new IrisCompanionQueueSnapshot(
                0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L, 0L);
    }

    public record InstancedAdditiveSnapshot(
            long queuedBatches,
            long queuedInstances,
            long drawCalls,
            long fallbackBatches,
            long fallbackInstances,
            long overflowBatches,
            long overflowInstances,
            long currentFrameQueuedBatches,
            long currentFrameQueuedInstances,
            long currentFrameDrawCalls,
            long currentFrameFallbackBatches,
            long currentFrameFallbackInstances,
            long currentFrameOverflowBatches,
            long currentFrameOverflowInstances,
            long lastFrameQueuedBatches,
            long lastFrameQueuedInstances,
            long lastFrameDrawCalls,
            long lastFrameFallbackBatches,
            long lastFrameFallbackInstances,
            long lastFrameOverflowBatches,
            long lastFrameOverflowInstances) {

        public static final InstancedAdditiveSnapshot EMPTY = new InstancedAdditiveSnapshot(
                0L, 0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L, 0L, 0L);
    }

    public record MdiAdditiveSnapshot(
            long eligibleBatches,
            long fallbackBatches,
            long drawCalls,
            long indirectCommands,
            long noSlotBatches,
            long noSlotInstances,
            long currentFrameEligibleBatches,
            long currentFrameFallbackBatches,
            long currentFrameDrawCalls,
            long currentFrameIndirectCommands,
            long currentFrameNoSlotBatches,
            long currentFrameNoSlotInstances,
            long lastFrameEligibleBatches,
            long lastFrameFallbackBatches,
            long lastFrameDrawCalls,
            long lastFrameIndirectCommands,
            long lastFrameNoSlotBatches,
            long lastFrameNoSlotInstances) {

        public static final MdiAdditiveSnapshot EMPTY = new MdiAdditiveSnapshot(
                0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L, 0L);
    }

    public record ModelCacheSnapshot(
            int registeredModels,
            int loadedModels,
            int unloadedModels,
            int failedModels,
            int vboRequestedModels,
            int rawModelViews,
            int mixedModeModels,
            int smoothingDisabledModels,
            int allPreparedBatchModels,
            int groups,
            int faces,
            int faceVertices,
            int selectionCacheEntries,
            int selectionCacheEmptyEntries,
            int selectionCachePreparedEntries,
            int missingPartWarningEntries) {
    }

    public record RenderBackendCapabilities(
            boolean texturedPreparedBatches,
            boolean spritePreparedBatches,
            boolean untexturedPreparedBatches,
            boolean dynamicTexturePerDraw,
            boolean atlasSpriteRetargeting,
            boolean lightOverlayPerDraw,
            boolean uvTransformPerDraw,
            boolean legacyShadowPerDraw,
            boolean clippedFaces,
            boolean instancedDraws) {
    }

    public enum RenderBackendFallbackReason {
        NONE,
        TEXTURED_CLIPPED,
        TEXTURED_DIRECT_QUAD,
        SPRITE_DIRECT_QUAD,
        BILLBOARD_DIRECT_QUAD,
        UNTEXTURED_CLIPPED,
        UNTEXTURED_DIRECT_QUAD,
        UNTEXTURED_DIRECT_QUADS,
        UNTEXTURED_DIRECT_VERTEX_COLOR_QUAD,
        UNTEXTURED_DIRECT_VERTEX_COLOR_TRIANGLE,
        UNTEXTURED_DIRECT_LINES,
        GPU_DISABLED,
        GPU_SHADER_ACTIVE,
        GPU_NOT_RENDER_THREAD,
        GPU_UNSUPPORTED_RENDER_MODE,
        GPU_UNSUPPORTED_DEPTH_WRITE_TRANSPARENT,
        GPU_UNSUPPORTED_GLINT,
        GPU_UNSUPPORTED_ALPHA,
        GPU_UNSUPPORTED_UV_TRANSFORM,
        GPU_LEGACY_SHADOW,
        GPU_SPRITE_UNSUPPORTED,
        GPU_UNTEXTURED_UNSUPPORTED,
        GPU_UPLOAD_FAILED,
        GPU_DRAW_FAILED,
        IRIS_SHADER_UNAVAILABLE,
        IRIS_COMPANION_UNSUPPORTED,
        IRIS_COMPANION_UPLOAD_FAILED,
        IRIS_COMPANION_DRAW_FAILED,
        INSTANCING_SHADER_UNAVAILABLE,
        INSTANCING_UPLOAD_FAILED,
        INSTANCING_DRAW_FAILED
    }

    public enum RenderBackendClearReason {
        RESOURCE_RELOAD,
        CLIENT_DISCONNECT,
        SHADER_RELOAD,
        MANUAL
    }

    public enum RenderBackendFlushStage {
        AFTER_BLOCK_ENTITIES,
        MANUAL
    }

    private static final class CacheMetrics {
        private final AtomicLong groupPreparedBuilds = new AtomicLong();
        private final AtomicLong groupPreparedVertices = new AtomicLong();
        private final AtomicLong allPreparedBatchBuilds = new AtomicLong();
        private final AtomicLong selectionCacheHits = new AtomicLong();
        private final AtomicLong selectionCacheMisses = new AtomicLong();
        private final AtomicLong selectionCacheClears = new AtomicLong();
        private final AtomicLong selectionCacheEmptyBuilds = new AtomicLong();
        private final AtomicLong selectionCachePreparedBatchBuilds = new AtomicLong();
        private final AtomicLong selectionHandleRefreshes = new AtomicLong();
        private final AtomicLong selectionHandleEmptyBuilds = new AtomicLong();
        private final AtomicLong selectionHandlePreparedBatchBuilds = new AtomicLong();

        private void recordGroupPrepared(int vertices) {
            groupPreparedBuilds.incrementAndGet();
            groupPreparedVertices.addAndGet(vertices);
        }

        private void recordAllPreparedBatchBuild() {
            allPreparedBatchBuilds.incrementAndGet();
        }

        private void recordSelectionCacheHit() {
            selectionCacheHits.incrementAndGet();
        }

        private void recordSelectionCacheMiss() {
            selectionCacheMisses.incrementAndGet();
        }

        private void recordSelectionCacheClear() {
            selectionCacheClears.incrementAndGet();
        }

        private void recordSelectionCacheEmptyBuild() {
            selectionCacheEmptyBuilds.incrementAndGet();
        }

        private void recordSelectionCachePreparedBatchBuild() {
            selectionCachePreparedBatchBuilds.incrementAndGet();
        }

        private void recordSelectionHandleRefresh() {
            selectionHandleRefreshes.incrementAndGet();
        }

        private void recordSelectionHandleEmptyBuild() {
            selectionHandleEmptyBuilds.incrementAndGet();
        }

        private void recordSelectionHandlePreparedBatchBuild() {
            selectionHandlePreparedBatchBuilds.incrementAndGet();
        }
    }

    private interface RenderBackend {
        void renderTextured(PreparedBatch batch, ResourceLocation textureLocation, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                boolean legacyShadow, boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform);

        void renderSprite(PreparedBatch batch, TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
                int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                boolean legacyShadow, boolean partBrightness, LegacyTexturedRenderMode renderMode,
                UvTransform uvTransform);

        void renderUntextured(PreparedBatch batch, PoseStack poseStack, MultiBufferSource buffer,
                int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode);

        default void renderTexturedTransient(PreparedBatch batch, ResourceLocation textureLocation,
                PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
                RenderBackendFallbackReason fallbackReason) {
            recordCpuFallback(fallbackReason, batch.vertexCount());
            renderTextured(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
        }

        default void renderUntexturedTransient(PreparedBatch batch, PoseStack poseStack, MultiBufferSource buffer,
                int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode,
                RenderBackendFallbackReason fallbackReason) {
            recordCpuFallback(fallbackReason, batch.vertexCount());
            renderUntextured(batch, poseStack, buffer, red, green, blue, alpha, renderMode);
        }

        default void renderUntexturedVertexColorTransient(UntexturedVertexColorTransientQuad quad,
                PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                RenderBackendFallbackReason fallbackReason) {
            recordCpuFallback(fallbackReason, 4);
            renderUntexturedVertexColorTransientCpu(quad, poseStack, buffer, renderMode);
        }

        default void renderUntexturedVertexColorTransientTriangle(UntexturedVertexColorTransientTriangle triangle,
                PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                RenderBackendFallbackReason fallbackReason) {
            recordCpuFallback(fallbackReason, 3);
            renderUntexturedVertexColorTransientCpu(triangle, poseStack, buffer, renderMode);
        }

        default void renderUntexturedVertexColorTransientTriangles(
                List<UntexturedVertexColorTransientTriangle> triangles,
                PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                RenderBackendFallbackReason fallbackReason) {
            recordCpuFallback(fallbackReason, triangles.size() * 3);
            renderUntexturedVertexColorTransientTrianglesCpu(triangles, poseStack, buffer, renderMode);
        }

        default void renderUntexturedLineTransient(List<UntexturedLineTransient> lines,
                PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode, float lineWidth,
                RenderBackendFallbackReason fallbackReason) {
            recordCpuFallback(fallbackReason, lines.size() * 2);
            renderUntexturedLineTransientCpu(lines, poseStack, buffer, renderMode, lineWidth);
        }

        default void clear(RenderBackendClearReason reason) {
        }

        default void flush(RenderBackendFlushStage stage) {
        }

        default void recordCpuFallback(RenderBackendFallbackReason fallback, int vertices) {
        }

        default void endFrame() {
        }

        RenderBackendSnapshot snapshot();

        default RenderBackendAdditiveSnapshot additiveSnapshot() {
            return RenderBackendAdditiveSnapshot.EMPTY;
        }

        default RenderBackendIrisSnapshot irisSnapshot() {
            return RenderBackendIrisSnapshot.EMPTY;
        }
    }

    private static final class ExperimentalGpuPreparedRenderBackend implements RenderBackend {
        private static final int MAX_GPU_MESHES = 512;
        private static final int MAX_INSTANCED_INSTANCES_PER_DRAW = 4096;
        private static final int INSTANCED_VERTEX_STRIDE_BYTES = 44;
        private static final Matrix4f OPTIMIZED_SHADER_IDENTITY = new Matrix4f();
        private static final ResourceLocation IRIS_UNTEXTURED_WHITE_TEXTURE =
                new ResourceLocation(HbmNtm.MOD_ID, "dynamic/iris_untextured_white");
        private static final RenderBackendCapabilities CAPABILITIES = new RenderBackendCapabilities(
                true,
                false,
                false,
                true,
                false,
                true,
                false,
                false,
                true,
                true);

        private final RenderBackend cpuFallback;
        private final Map<GpuMeshKey, GpuMesh> meshes = Collections.synchronizedMap(
                new LinkedHashMap<GpuMeshKey, GpuMesh>(MAX_GPU_MESHES + 1, 0.75F, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<GpuMeshKey, GpuMesh> eldest) {
                        if (size() <= MAX_GPU_MESHES) {
                            return false;
                        }
                        closeLater(eldest.getValue());
                        return true;
                    }
                });
        private final Set<GpuMeshKey> failedKeys = ConcurrentHashMap.newKeySet();
        private final Map<IrisCompanionMeshKey, IrisCompanionMesh> irisMeshes = Collections.synchronizedMap(
                new LinkedHashMap<IrisCompanionMeshKey, IrisCompanionMesh>(MAX_GPU_MESHES + 1, 0.75F, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<IrisCompanionMeshKey, IrisCompanionMesh> eldest) {
                        if (size() <= MAX_GPU_MESHES) {
                            return false;
                        }
                        closeIrisLater(eldest.getValue());
                        return true;
                    }
                });
        private final Set<IrisCompanionMeshKey> failedIrisKeys = ConcurrentHashMap.newKeySet();
        private final Map<InstancedMeshKey, InstancedMesh> instancedMeshes = Collections.synchronizedMap(
                new LinkedHashMap<InstancedMeshKey, InstancedMesh>(MAX_GPU_MESHES + 1, 0.75F, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<InstancedMeshKey, InstancedMesh> eldest) {
                        if (size() <= MAX_GPU_MESHES) {
                            return false;
                        }
                        closeInstancedLater(eldest.getValue());
                        return true;
                    }
                });
        private final Set<InstancedMeshKey> failedInstancedKeys = ConcurrentHashMap.newKeySet();
        private final Map<InstancedBatchKey, InstancedBatch> pendingInstancedBatches = new LinkedHashMap<>();
        private final Map<IrisCompanionQueueKey, IrisCompanionQueuedBatch> pendingIrisCompanionBatches =
                new LinkedHashMap<>();
        private final AtomicLong gpuBufferBytes = new AtomicLong();
        private final AtomicLong gpuUploadAttempts = new AtomicLong();
        private final AtomicLong gpuUploadFailures = new AtomicLong();
        private final AtomicLong gpuDrawCalls = new AtomicLong();
        private final AtomicLong gpuFallbackBatches = new AtomicLong();
        private final AtomicLong gpuFallbackVertices = new AtomicLong();
        private final AtomicLong irisMeshBytes = new AtomicLong();
        private final AtomicLong irisEligibleBatches = new AtomicLong();
        private final AtomicLong irisDrawCalls = new AtomicLong();
        private final AtomicLong irisShadowDrawCalls = new AtomicLong();
        private final AtomicLong irisFallbackBatches = new AtomicLong();
        private final AtomicLong irisFallbackVertices = new AtomicLong();
        private final AtomicLong irisUploadAttempts = new AtomicLong();
        private final AtomicLong irisUploadFailures = new AtomicLong();
        private final AtomicLong currentFrameIrisEligibleBatches = new AtomicLong();
        private final AtomicLong currentFrameIrisDrawCalls = new AtomicLong();
        private final AtomicLong currentFrameIrisShadowDrawCalls = new AtomicLong();
        private final AtomicLong currentFrameIrisFallbackBatches = new AtomicLong();
        private final AtomicLong currentFrameIrisFallbackVertices = new AtomicLong();
        private final AtomicLong lastFrameIrisEligibleBatches = new AtomicLong();
        private final AtomicLong lastFrameIrisDrawCalls = new AtomicLong();
        private final AtomicLong lastFrameIrisShadowDrawCalls = new AtomicLong();
        private final AtomicLong lastFrameIrisFallbackBatches = new AtomicLong();
        private final AtomicLong lastFrameIrisFallbackVertices = new AtomicLong();
        private final AtomicLong irisQueuedBatches = new AtomicLong();
        private final AtomicLong irisQueuedInstances = new AtomicLong();
        private final AtomicLong irisQueuedFlushes = new AtomicLong();
        private final AtomicLong irisQueuedDrawCalls = new AtomicLong();
        private final AtomicLong irisQueuedFallbackBatches = new AtomicLong();
        private final AtomicLong irisQueuedFallbackInstances = new AtomicLong();
        private final AtomicLong currentFrameIrisQueuedBatches = new AtomicLong();
        private final AtomicLong currentFrameIrisQueuedInstances = new AtomicLong();
        private final AtomicLong currentFrameIrisQueuedFlushes = new AtomicLong();
        private final AtomicLong currentFrameIrisQueuedDrawCalls = new AtomicLong();
        private final AtomicLong currentFrameIrisQueuedFallbackBatches = new AtomicLong();
        private final AtomicLong currentFrameIrisQueuedFallbackInstances = new AtomicLong();
        private final AtomicLong lastFrameIrisQueuedBatches = new AtomicLong();
        private final AtomicLong lastFrameIrisQueuedInstances = new AtomicLong();
        private final AtomicLong lastFrameIrisQueuedFlushes = new AtomicLong();
        private final AtomicLong lastFrameIrisQueuedDrawCalls = new AtomicLong();
        private final AtomicLong lastFrameIrisQueuedFallbackBatches = new AtomicLong();
        private final AtomicLong lastFrameIrisQueuedFallbackInstances = new AtomicLong();
        private final AtomicLong instancedQueuedBatches = new AtomicLong();
        private final AtomicLong instancedQueuedInstances = new AtomicLong();
        private final AtomicLong instancedFlushes = new AtomicLong();
        private final AtomicLong instancedDrawCalls = new AtomicLong();
        private final AtomicLong instancedFallbackBatches = new AtomicLong();
        private final AtomicLong instancedFallbackInstances = new AtomicLong();
        private final AtomicLong instancedOverflowBatches = new AtomicLong();
        private final AtomicLong instancedOverflowInstances = new AtomicLong();
        private final AtomicLong instancedAdditiveQueuedBatches = new AtomicLong();
        private final AtomicLong instancedAdditiveQueuedInstances = new AtomicLong();
        private final AtomicLong instancedAdditiveDrawCalls = new AtomicLong();
        private final AtomicLong instancedAdditiveFallbackBatches = new AtomicLong();
        private final AtomicLong instancedAdditiveFallbackInstances = new AtomicLong();
        private final AtomicLong instancedAdditiveOverflowBatches = new AtomicLong();
        private final AtomicLong instancedAdditiveOverflowInstances = new AtomicLong();
        private final AtomicLong mdiEligibleFlushes = new AtomicLong();
        private final AtomicLong mdiEligibleBatches = new AtomicLong();
        private final AtomicLong mdiFallbackFlushes = new AtomicLong();
        private final AtomicLong mdiFallbackBatches = new AtomicLong();
        private final AtomicLong mdiDrawCalls = new AtomicLong();
        private final AtomicLong mdiMultiDrawCalls = new AtomicLong();
        private final AtomicLong mdiIndirectCommands = new AtomicLong();
        private final AtomicLong mdiNoSlotBatches = new AtomicLong();
        private final AtomicLong mdiNoSlotInstances = new AtomicLong();
        private final AtomicLong mdiAdditiveEligibleBatches = new AtomicLong();
        private final AtomicLong mdiAdditiveFallbackBatches = new AtomicLong();
        private final AtomicLong mdiAdditiveDrawCalls = new AtomicLong();
        private final AtomicLong mdiAdditiveIndirectCommands = new AtomicLong();
        private final AtomicLong mdiAdditiveNoSlotBatches = new AtomicLong();
        private final AtomicLong mdiAdditiveNoSlotInstances = new AtomicLong();
        private final AtomicLong currentFrameMdiEligibleFlushes = new AtomicLong();
        private final AtomicLong currentFrameMdiEligibleBatches = new AtomicLong();
        private final AtomicLong currentFrameMdiFallbackFlushes = new AtomicLong();
        private final AtomicLong currentFrameMdiFallbackBatches = new AtomicLong();
        private final AtomicLong currentFrameMdiDrawCalls = new AtomicLong();
        private final AtomicLong currentFrameMdiIndirectCommands = new AtomicLong();
        private final AtomicLong currentFrameMdiNoSlotBatches = new AtomicLong();
        private final AtomicLong currentFrameMdiNoSlotInstances = new AtomicLong();
        private final AtomicLong currentFrameMdiAdditiveEligibleBatches = new AtomicLong();
        private final AtomicLong currentFrameMdiAdditiveFallbackBatches = new AtomicLong();
        private final AtomicLong currentFrameMdiAdditiveDrawCalls = new AtomicLong();
        private final AtomicLong currentFrameMdiAdditiveIndirectCommands = new AtomicLong();
        private final AtomicLong currentFrameMdiAdditiveNoSlotBatches = new AtomicLong();
        private final AtomicLong currentFrameMdiAdditiveNoSlotInstances = new AtomicLong();
        private final AtomicLong lastFrameMdiEligibleFlushes = new AtomicLong();
        private final AtomicLong lastFrameMdiEligibleBatches = new AtomicLong();
        private final AtomicLong lastFrameMdiFallbackFlushes = new AtomicLong();
        private final AtomicLong lastFrameMdiFallbackBatches = new AtomicLong();
        private final AtomicLong lastFrameMdiDrawCalls = new AtomicLong();
        private final AtomicLong lastFrameMdiIndirectCommands = new AtomicLong();
        private final AtomicLong lastFrameMdiNoSlotBatches = new AtomicLong();
        private final AtomicLong lastFrameMdiNoSlotInstances = new AtomicLong();
        private final AtomicLong lastFrameMdiAdditiveEligibleBatches = new AtomicLong();
        private final AtomicLong lastFrameMdiAdditiveFallbackBatches = new AtomicLong();
        private final AtomicLong lastFrameMdiAdditiveDrawCalls = new AtomicLong();
        private final AtomicLong lastFrameMdiAdditiveIndirectCommands = new AtomicLong();
        private final AtomicLong lastFrameMdiAdditiveNoSlotBatches = new AtomicLong();
        private final AtomicLong lastFrameMdiAdditiveNoSlotInstances = new AtomicLong();
        private final AtomicLong currentFrameInstancedQueuedBatches = new AtomicLong();
        private final AtomicLong currentFrameInstancedQueuedInstances = new AtomicLong();
        private final AtomicLong currentFrameInstancedDrawCalls = new AtomicLong();
        private final AtomicLong currentFrameInstancedFallbackBatches = new AtomicLong();
        private final AtomicLong currentFrameInstancedFallbackInstances = new AtomicLong();
        private final AtomicLong currentFrameInstancedOverflowBatches = new AtomicLong();
        private final AtomicLong currentFrameInstancedOverflowInstances = new AtomicLong();
        private final AtomicLong currentFrameInstancedAdditiveQueuedBatches = new AtomicLong();
        private final AtomicLong currentFrameInstancedAdditiveQueuedInstances = new AtomicLong();
        private final AtomicLong currentFrameInstancedAdditiveDrawCalls = new AtomicLong();
        private final AtomicLong currentFrameInstancedAdditiveFallbackBatches = new AtomicLong();
        private final AtomicLong currentFrameInstancedAdditiveFallbackInstances = new AtomicLong();
        private final AtomicLong currentFrameInstancedAdditiveOverflowBatches = new AtomicLong();
        private final AtomicLong currentFrameInstancedAdditiveOverflowInstances = new AtomicLong();
        private final AtomicLong lastFrameInstancedQueuedBatches = new AtomicLong();
        private final AtomicLong lastFrameInstancedQueuedInstances = new AtomicLong();
        private final AtomicLong lastFrameInstancedDrawCalls = new AtomicLong();
        private final AtomicLong lastFrameInstancedFallbackBatches = new AtomicLong();
        private final AtomicLong lastFrameInstancedFallbackInstances = new AtomicLong();
        private final AtomicLong lastFrameInstancedOverflowBatches = new AtomicLong();
        private final AtomicLong lastFrameInstancedOverflowInstances = new AtomicLong();
        private final AtomicLong lastFrameInstancedAdditiveQueuedBatches = new AtomicLong();
        private final AtomicLong lastFrameInstancedAdditiveQueuedInstances = new AtomicLong();
        private final AtomicLong lastFrameInstancedAdditiveDrawCalls = new AtomicLong();
        private final AtomicLong lastFrameInstancedAdditiveFallbackBatches = new AtomicLong();
        private final AtomicLong lastFrameInstancedAdditiveFallbackInstances = new AtomicLong();
        private final AtomicLong lastFrameInstancedAdditiveOverflowBatches = new AtomicLong();
        private final AtomicLong lastFrameInstancedAdditiveOverflowInstances = new AtomicLong();
        private final AtomicLong currentFrameGpuDrawCalls = new AtomicLong();
        private final AtomicLong currentFrameGpuFallbackBatches = new AtomicLong();
        private final AtomicLong currentFrameGpuFallbackVertices = new AtomicLong();
        private final AtomicLong lastFrameGpuDrawCalls = new AtomicLong();
        private final AtomicLong lastFrameGpuFallbackBatches = new AtomicLong();
        private final AtomicLong lastFrameGpuFallbackVertices = new AtomicLong();
        private volatile RenderBackendFallbackReason lastGpuFallbackReason = RenderBackendFallbackReason.NONE;
        private volatile boolean mdiCapsResolved;
        private volatile boolean mdiDrawIndirectSupported;
        private volatile boolean mdiMultiDrawIndirectSupported;
        private volatile boolean mdiBaseInstanceSupported;
        private static volatile boolean irisUntexturedWhiteTextureRegistered;
        private final MdiDrawArraysAtlas mdiAtlas = new MdiDrawArraysAtlas();

        private ExperimentalGpuPreparedRenderBackend(RenderBackend cpuFallback) {
            this.cpuFallback = cpuFallback;
        }

        private static ResourceLocation irisUntexturedWhiteTexture() {
            if (!irisUntexturedWhiteTextureRegistered) {
                NativeImage image = new NativeImage(1, 1, false);
                image.setPixelRGBA(0, 0, 0xFFFFFFFF);
                Minecraft.getInstance().getTextureManager().register(IRIS_UNTEXTURED_WHITE_TEXTURE,
                        new DynamicTexture(image));
                irisUntexturedWhiteTextureRegistered = true;
            }
            return IRIS_UNTEXTURED_WHITE_TEXTURE;
        }

        @Override
        public void renderTextured(PreparedBatch batch, ResourceLocation textureLocation, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                boolean legacyShadow, boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            RenderBackendFallbackReason fallback = unsupportedTexturedReason(batch, alpha, legacyShadow, renderMode, uvTransform);
            if (fallback != RenderBackendFallbackReason.NONE) {
                if (fallback == RenderBackendFallbackReason.GPU_DISABLED) {
                    cpuFallback.renderTextured(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                            red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
                    return;
                }
                renderTexturedCpuFallback(fallback, batch, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                        red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
                return;
            }
            LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, poseStack.last().normal()).withAlpha(alpha);
            boolean instancedOnlyMode = requiresInstancedGpuPath(alphaMode);
            try {
                if (canUseIrisGlintTransientCompanionPath(HbmRenderFrameFlags.current(), alphaMode, alpha,
                        legacyShadow)) {
                    try {
                        if (drawIrisTransientCompanionBatch(batch, null, textureLocation, poseStack, packedLight,
                                packedOverlay, red, green, blue, alpha, smoothing, alphaMode, uvTransform,
                                GpuMeshKind.TEXTURED)) {
                            return;
                        }
                    } catch (RuntimeException exception) {
                        recordIrisFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED,
                                batch.vertexCount());
                        HbmNtm.LOGGER.debug("Failed to draw legacy OBJ Iris glint transient companion mesh",
                                exception);
                    }
                    renderTexturedCpuFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED, batch,
                            textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                            legacyShadow, smoothing, renderMode, uvTransform);
                    return;
                }
                if (canUseIrisDynamicUvTransientCompanionPath(HbmRenderFrameFlags.current(), alphaMode, alpha,
                        legacyShadow, uvTransform)) {
                    try {
                        if (drawIrisTransientCompanionBatch(batch, null, textureLocation, poseStack, packedLight,
                                packedOverlay, red, green, blue, alpha, smoothing, alphaMode, uvTransform,
                                GpuMeshKind.TEXTURED)) {
                            return;
                        }
                    } catch (RuntimeException exception) {
                        recordIrisFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED,
                                batch.vertexCount());
                        HbmNtm.LOGGER.debug("Failed to draw legacy OBJ Iris dynamic-UV transient companion mesh",
                                exception);
                    }
                    renderTexturedCpuFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED, batch,
                            textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                            legacyShadow, smoothing, renderMode, uvTransform);
                    return;
                }
                if (canQueueIrisCompanionPath(HbmRenderFrameFlags.current(), alphaMode, alpha, legacyShadow,
                        uvTransform)) {
                    if (queueIrisCompanion(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                            red, green, blue, alpha, smoothing, alphaMode, uvTransform)) {
                        return;
                    }
                    renderTexturedCpuFallback(RenderBackendFallbackReason.IRIS_COMPANION_UPLOAD_FAILED, batch,
                            textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                            legacyShadow, smoothing, renderMode, uvTransform);
                    return;
                }
                if (canUseIrisSingleMeshPath(HbmRenderFrameFlags.current(), alphaMode, alpha, legacyShadow,
                        uvTransform)) {
                    boolean irisShadowPass = HbmShaderCompatibilityDetector.isRenderingShadowPass();
                    try {
                        if (drawIrisCompanionBatch(batch, textureLocation, poseStack, packedLight, packedOverlay,
                                red, green, blue, alpha, smoothing, alphaMode, uvTransform, irisShadowPass)) {
                            return;
                        }
                    } catch (RuntimeException exception) {
                        recordIrisFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED,
                                batch.vertexCount());
                        HbmNtm.LOGGER.debug("Failed to draw legacy OBJ Iris companion mesh", exception);
                    }
                    renderTexturedCpuFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED, batch,
                            textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                            legacyShadow, smoothing, renderMode, uvTransform);
                    return;
                }
                if (HbmRenderFrameFlags.current().instancingEnabled()
                        && canUseInstancedPath(alphaMode)) {
                    if (queueInstanced(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue,
                            alpha, smoothing, alphaMode, uvTransform)) {
                        return;
                    }
                    recordInstancedFallback(RenderBackendFallbackReason.INSTANCING_UPLOAD_FAILED, 1, alphaMode);
                }
                if (!uvTransform.gpuMeshCacheable()) {
                    renderTexturedCpuFallback(RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM, batch,
                            textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                            legacyShadow, smoothing, renderMode, uvTransform);
                    return;
                }
                if (instancedOnlyMode) {
                    renderTexturedCpuFallback(RenderBackendFallbackReason.INSTANCING_UPLOAD_FAILED, batch, textureLocation,
                            poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                            smoothing, renderMode, uvTransform);
                    return;
                }
                boolean drew = false;
                if (!batch.quadVertices().isEmpty()) {
                    GpuMesh mesh = meshFor(batch, VertexFormat.Mode.QUADS, packedLight, packedOverlay, red, green, blue,
                            alpha, smoothing, uvTransform, batch.quadVertices());
                    drawMesh(mesh, alphaMode.renderType(textureLocation, VertexFormat.Mode.QUADS), poseStack);
                    drew = true;
                }
                if (!batch.triangleVertices().isEmpty()) {
                    GpuMesh mesh = meshFor(batch, VertexFormat.Mode.TRIANGLES, packedLight, packedOverlay, red, green,
                            blue, alpha, smoothing, uvTransform, batch.triangleVertices());
                    drawMesh(mesh, alphaMode.renderType(textureLocation, VertexFormat.Mode.TRIANGLES), poseStack);
                    drew = true;
                }
                if (!drew) {
                    renderTexturedCpuFallback(RenderBackendFallbackReason.GPU_UPLOAD_FAILED, batch, textureLocation,
                            poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                            smoothing, renderMode, uvTransform);
                }
            } catch (RuntimeException exception) {
                renderTexturedCpuFallback(RenderBackendFallbackReason.GPU_DRAW_FAILED, batch, textureLocation, poseStack,
                        buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing,
                        renderMode, uvTransform);
            }
        }

        @Override
        public void renderSprite(PreparedBatch batch, TextureAtlasSprite sprite, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                boolean legacyShadow, boolean partBrightness, LegacyTexturedRenderMode renderMode,
                UvTransform uvTransform) {
            if (batch.empty()) {
                return;
            }
            RenderBackendFallbackReason fallback = unsupportedSpriteReason(batch, alpha, legacyShadow, renderMode,
                    uvTransform);
            if (fallback != RenderBackendFallbackReason.NONE) {
                if (fallback == RenderBackendFallbackReason.GPU_DISABLED) {
                    cpuFallback.renderSprite(batch, sprite, poseStack, buffer, packedLight, packedOverlay,
                            red, green, blue, alpha, legacyShadow, partBrightness, renderMode, uvTransform);
                    return;
                }
                renderSpriteCpuFallback(fallback, batch, sprite, poseStack, buffer, packedLight, packedOverlay,
                        red, green, blue, alpha, legacyShadow, partBrightness, renderMode, uvTransform);
                return;
            }
            LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, poseStack.last().normal())
                    .withAlpha(alpha);
            boolean instancedOnlyMode = requiresInstancedGpuPath(alphaMode);
            try {
                if (canUseIrisDynamicUvTransientCompanionPath(HbmRenderFrameFlags.current(), alphaMode, alpha,
                        legacyShadow, uvTransform)) {
                    try {
                        if (drawIrisTransientCompanionBatch(batch, sprite, InventoryMenu.BLOCK_ATLAS, poseStack,
                                packedLight, packedOverlay, red, green, blue, alpha, false, alphaMode, uvTransform,
                                GpuMeshKind.SPRITE)) {
                            return;
                        }
                    } catch (RuntimeException exception) {
                        recordIrisFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED,
                                batch.vertexCount());
                        HbmNtm.LOGGER.debug("Failed to draw legacy OBJ Iris dynamic-UV sprite companion mesh",
                                exception);
                    }
                    renderSpriteCpuFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED, batch, sprite,
                            poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                            partBrightness, renderMode, uvTransform);
                    return;
                }
                if (canQueueIrisCompanionPath(HbmRenderFrameFlags.current(), alphaMode, alpha, legacyShadow,
                        uvTransform)) {
                    if (queueIrisCompanionSprite(batch, sprite, poseStack, buffer, packedLight, packedOverlay,
                            red, green, blue, alpha, alphaMode, uvTransform)) {
                        return;
                    }
                    renderSpriteCpuFallback(RenderBackendFallbackReason.IRIS_COMPANION_UPLOAD_FAILED, batch, sprite,
                            poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                            partBrightness, renderMode, uvTransform);
                    return;
                }
                if (canUseIrisSingleMeshPath(HbmRenderFrameFlags.current(), alphaMode, alpha, legacyShadow,
                        uvTransform)) {
                    boolean irisShadowPass = HbmShaderCompatibilityDetector.isRenderingShadowPass();
                    try {
                        if (drawIrisCompanionBatch(batch, sprite, InventoryMenu.BLOCK_ATLAS, poseStack, packedLight,
                                packedOverlay, red, green, blue, alpha, false, alphaMode, uvTransform,
                                irisShadowPass)) {
                            return;
                        }
                    } catch (RuntimeException exception) {
                        recordIrisFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED,
                                batch.vertexCount());
                        HbmNtm.LOGGER.debug("Failed to draw legacy OBJ Iris sprite companion mesh", exception);
                    }
                    renderSpriteCpuFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED, batch, sprite,
                            poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                            partBrightness, renderMode, uvTransform);
                    return;
                }
                if (HbmRenderFrameFlags.current().instancingEnabled()
                        && canUseInstancedPath(alphaMode)) {
                    if (queueInstancedSprite(batch, sprite, poseStack, buffer, packedLight, packedOverlay, red, green,
                            blue, alpha, alphaMode, uvTransform)) {
                        return;
                    }
                    recordInstancedFallback(RenderBackendFallbackReason.INSTANCING_UPLOAD_FAILED, 1, alphaMode);
                }
                if (!uvTransform.gpuMeshCacheable()) {
                    renderSpriteCpuFallback(RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM, batch, sprite,
                            poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                            partBrightness, renderMode, uvTransform);
                    return;
                }
                if (instancedOnlyMode) {
                    renderSpriteCpuFallback(RenderBackendFallbackReason.INSTANCING_UPLOAD_FAILED, batch, sprite,
                            poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                            partBrightness, renderMode, uvTransform);
                    return;
                }
                boolean drew = false;
                if (!batch.quadVertices().isEmpty()) {
                    GpuMesh mesh = meshForAtlasSprite(batch, VertexFormat.Mode.QUADS, sprite, packedLight, packedOverlay,
                            red, green, blue, alpha, uvTransform, batch.quadVertices());
                    drawMesh(mesh, alphaMode.renderType(InventoryMenu.BLOCK_ATLAS, VertexFormat.Mode.QUADS),
                            poseStack);
                    drew = true;
                }
                if (!batch.triangleVertices().isEmpty()) {
                    GpuMesh mesh = meshForAtlasSprite(batch, VertexFormat.Mode.TRIANGLES, sprite, packedLight,
                            packedOverlay, red, green, blue, alpha, uvTransform, batch.triangleVertices());
                    drawMesh(mesh, alphaMode.renderType(InventoryMenu.BLOCK_ATLAS, VertexFormat.Mode.TRIANGLES),
                            poseStack);
                    drew = true;
                }
                if (!drew) {
                    renderSpriteCpuFallback(RenderBackendFallbackReason.GPU_UPLOAD_FAILED, batch, sprite, poseStack,
                            buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                            partBrightness, renderMode, uvTransform);
                }
            } catch (RuntimeException exception) {
                renderSpriteCpuFallback(RenderBackendFallbackReason.GPU_DRAW_FAILED, batch, sprite, poseStack,
                        buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, partBrightness,
                        renderMode, uvTransform);
            }
        }

        @Override
        public void renderUntextured(PreparedBatch batch, PoseStack poseStack, MultiBufferSource buffer,
                int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
            if (batch.empty()) {
                return;
            }
            LegacyTexturedRenderMode resolvedRenderMode = renderModeForPose(renderMode, poseStack.last().normal());
            RenderBackendFallbackReason fallback = unsupportedUntexturedReason(batch, alpha, resolvedRenderMode);
            if (fallback != RenderBackendFallbackReason.NONE) {
                if (fallback == RenderBackendFallbackReason.GPU_DISABLED) {
                    cpuFallback.renderUntextured(batch, poseStack, buffer, red, green, blue, alpha, renderMode);
                    return;
                }
                renderUntexturedCpuFallback(fallback, batch, poseStack, buffer, red, green, blue, alpha, renderMode);
                return;
            }
            try {
                if (canQueueIrisUntexturedCompanionPath(HbmRenderFrameFlags.current(), resolvedRenderMode, alpha)) {
                    if (queueIrisCompanionUntextured(batch, poseStack, buffer, red, green, blue, alpha,
                            resolvedRenderMode)) {
                        return;
                    }
                    renderUntexturedCpuFallback(RenderBackendFallbackReason.IRIS_COMPANION_UPLOAD_FAILED, batch,
                            poseStack, buffer, red, green, blue, alpha, renderMode);
                    return;
                }
                if (canUseIrisUntexturedSingleMeshPath(HbmRenderFrameFlags.current(), resolvedRenderMode, alpha)) {
                    boolean irisShadowPass = HbmShaderCompatibilityDetector.isRenderingShadowPass();
                    try {
                        if (drawIrisCompanionBatch(batch, null, irisUntexturedWhiteTexture(), poseStack,
                                LightTexture.FULL_BRIGHT, 0, red, green, blue, alpha, false, resolvedRenderMode,
                                UvTransform.DEFAULT, irisShadowPass, GpuMeshKind.UNTEXTURED)) {
                            return;
                        }
                    } catch (RuntimeException exception) {
                        recordIrisFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED,
                                batch.vertexCount());
                        HbmNtm.LOGGER.debug("Failed to draw legacy OBJ Iris untextured companion mesh", exception);
                    }
                    renderUntexturedCpuFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED, batch,
                            poseStack, buffer, red, green, blue, alpha, renderMode);
                    return;
                }
                if (HbmRenderFrameFlags.current().instancingEnabled()
                        && canUseInstancedUntexturedPath(resolvedRenderMode, alpha)) {
                    if (queueInstancedUntextured(batch, poseStack, buffer, red, green, blue, alpha,
                            resolvedRenderMode)) {
                        return;
                    }
                    recordInstancedFallback(RenderBackendFallbackReason.INSTANCING_UPLOAD_FAILED, 1,
                            resolvedRenderMode);
                }
                boolean drew = false;
                if (!batch.quadVertices().isEmpty()) {
                    GpuMesh mesh = meshForUntextured(batch, VertexFormat.Mode.QUADS, red, green, blue, alpha,
                            batch.quadVertices());
                    drawMesh(mesh, LegacyUntexturedQuadRenderer.type(resolvedRenderMode, alpha,
                            VertexFormat.Mode.QUADS), poseStack);
                    drew = true;
                }
                if (!batch.triangleVertices().isEmpty()) {
                    GpuMesh mesh = meshForUntextured(batch, VertexFormat.Mode.TRIANGLES, red, green, blue, alpha,
                            batch.triangleVertices());
                    drawMesh(mesh, LegacyUntexturedQuadRenderer.type(resolvedRenderMode, alpha,
                            VertexFormat.Mode.TRIANGLES), poseStack);
                    drew = true;
                }
                if (!drew) {
                    renderUntexturedCpuFallback(RenderBackendFallbackReason.GPU_UPLOAD_FAILED, batch, poseStack,
                            buffer, red, green, blue, alpha, renderMode);
                }
            } catch (RuntimeException exception) {
                renderUntexturedCpuFallback(RenderBackendFallbackReason.GPU_DRAW_FAILED, batch, poseStack, buffer,
                        red, green, blue, alpha, renderMode);
            }
        }

        @Override
        public void renderTexturedTransient(PreparedBatch batch, ResourceLocation textureLocation,
                PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
                RenderBackendFallbackReason fallbackReason) {
            if (batch.empty()) {
                return;
            }
            RenderBackendFallbackReason unsupported = unsupportedTexturedTransientReason(batch, legacyShadow);
            LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, poseStack.last().normal())
                    .withAlpha(alpha);
            if (unsupported == RenderBackendFallbackReason.GPU_SHADER_ACTIVE
                    && canUseIrisTexturedTransientCompanionPath(HbmRenderFrameFlags.current(), alphaMode, alpha,
                            legacyShadow, uvTransform)) {
                try {
                    if (drawIrisTransientCompanionBatch(batch, null, textureLocation, poseStack, packedLight,
                            packedOverlay, red, green, blue, alpha, smoothing, alphaMode, uvTransform,
                            GpuMeshKind.TEXTURED)) {
                        return;
                    }
                } catch (RuntimeException exception) {
                    recordIrisFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED, batch.vertexCount());
                    HbmNtm.LOGGER.debug("Failed to draw clipped legacy OBJ Iris companion mesh", exception);
                }
                renderTexturedCpuFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED, batch,
                        textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                        legacyShadow, smoothing, renderMode, uvTransform);
                return;
            }
            if (unsupported != RenderBackendFallbackReason.NONE) {
                if (unsupported == RenderBackendFallbackReason.GPU_DISABLED) {
                    cpuFallback.renderTexturedTransient(batch, textureLocation, poseStack, buffer, packedLight,
                            packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, renderMode,
                            uvTransform, fallbackReason);
                    return;
                }
                renderTexturedCpuFallback(unsupported, batch, textureLocation, poseStack, buffer, packedLight,
                        packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
                return;
            }
            try {
                boolean drew = false;
                if (!batch.quadVertices().isEmpty()) {
                    GpuMesh mesh = uploadTransientTexturedMesh(VertexFormat.Mode.QUADS, packedLight, packedOverlay,
                            red, green, blue, alpha, smoothing, uvTransform, batch.quadVertices());
                    try {
                        drawMesh(mesh, alphaMode.renderType(textureLocation, VertexFormat.Mode.QUADS), poseStack);
                    } finally {
                        mesh.close();
                    }
                    drew = true;
                }
                if (!batch.triangleVertices().isEmpty()) {
                    GpuMesh mesh = uploadTransientTexturedMesh(VertexFormat.Mode.TRIANGLES, packedLight,
                            packedOverlay, red, green, blue, alpha, smoothing, uvTransform,
                            batch.triangleVertices());
                    try {
                        drawMesh(mesh, alphaMode.renderType(textureLocation, VertexFormat.Mode.TRIANGLES),
                                poseStack);
                    } finally {
                        mesh.close();
                    }
                    drew = true;
                }
                if (!drew) {
                    renderTexturedCpuFallback(fallbackReason, batch, textureLocation, poseStack, buffer,
                            packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing,
                            renderMode, uvTransform);
                }
            } catch (RuntimeException exception) {
                renderTexturedCpuFallback(RenderBackendFallbackReason.GPU_DRAW_FAILED, batch, textureLocation,
                        poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                        smoothing, renderMode, uvTransform);
            }
        }

        @Override
        public void renderUntexturedTransient(PreparedBatch batch, PoseStack poseStack, MultiBufferSource buffer,
                int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode,
                RenderBackendFallbackReason fallbackReason) {
            if (batch.empty()) {
                return;
            }
            RenderBackendFallbackReason unsupported = unsupportedUntexturedTransientReason(batch);
            LegacyTexturedRenderMode resolvedRenderMode = renderModeForPose(renderMode, poseStack.last().normal());
            if (unsupported == RenderBackendFallbackReason.GPU_SHADER_ACTIVE
                    && canUseIrisUntexturedSingleMeshPath(HbmRenderFrameFlags.current(), resolvedRenderMode,
                            alpha)) {
                try {
                    if (drawIrisTransientCompanionBatch(batch, null, irisUntexturedWhiteTexture(), poseStack,
                            LightTexture.FULL_BRIGHT, 0, red, green, blue, alpha, false, resolvedRenderMode,
                            UvTransform.DEFAULT, GpuMeshKind.UNTEXTURED)) {
                        return;
                    }
                } catch (RuntimeException exception) {
                    recordIrisFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED, batch.vertexCount());
                    HbmNtm.LOGGER.debug("Failed to draw clipped legacy OBJ Iris untextured companion mesh",
                            exception);
                }
                renderUntexturedCpuFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED, batch,
                        poseStack, buffer, red, green, blue, alpha, renderMode);
                return;
            }
            if (unsupported != RenderBackendFallbackReason.NONE) {
                if (unsupported == RenderBackendFallbackReason.GPU_DISABLED) {
                    cpuFallback.renderUntexturedTransient(batch, poseStack, buffer, red, green, blue, alpha,
                            renderMode, fallbackReason);
                    return;
                }
                renderUntexturedCpuFallback(unsupported, batch, poseStack, buffer, red, green, blue, alpha,
                        renderMode);
                return;
            }
            try {
                boolean drew = false;
                if (!batch.quadVertices().isEmpty()) {
                    GpuMesh mesh = uploadTransientUntexturedMesh(VertexFormat.Mode.QUADS, red, green, blue,
                            alpha, batch.quadVertices());
                    try {
                        drawMesh(mesh, LegacyUntexturedQuadRenderer.type(resolvedRenderMode, alpha,
                                VertexFormat.Mode.QUADS), poseStack);
                    } finally {
                        mesh.close();
                    }
                    drew = true;
                }
                if (!batch.triangleVertices().isEmpty()) {
                    GpuMesh mesh = uploadTransientUntexturedMesh(VertexFormat.Mode.TRIANGLES, red, green, blue,
                            alpha, batch.triangleVertices());
                    try {
                        drawMesh(mesh, LegacyUntexturedQuadRenderer.type(resolvedRenderMode, alpha,
                                VertexFormat.Mode.TRIANGLES), poseStack);
                    } finally {
                        mesh.close();
                    }
                    drew = true;
                }
                if (!drew) {
                    renderUntexturedCpuFallback(fallbackReason, batch, poseStack, buffer, red, green, blue, alpha,
                            renderMode);
                }
            } catch (RuntimeException exception) {
                renderUntexturedCpuFallback(RenderBackendFallbackReason.GPU_DRAW_FAILED, batch, poseStack, buffer,
                        red, green, blue, alpha, renderMode);
            }
        }

        @Override
        public void renderUntexturedVertexColorTransient(UntexturedVertexColorTransientQuad quad,
                PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                RenderBackendFallbackReason fallbackReason) {
            RenderBackendFallbackReason unsupported = unsupportedUntexturedVertexColorTransientReason();
            if (unsupported != RenderBackendFallbackReason.NONE) {
                if (unsupported == RenderBackendFallbackReason.GPU_DISABLED) {
                    cpuFallback.renderUntexturedVertexColorTransient(quad, poseStack, buffer, renderMode,
                            fallbackReason);
                    return;
                }
                recordGpuFallback(unsupported, 4);
                renderUntexturedVertexColorTransientCpu(quad, poseStack, buffer, renderMode);
                return;
            }
            try {
                GpuMesh mesh = uploadTransientUntexturedVertexColorMesh(quad);
                try {
                    drawMesh(mesh, LegacyUntexturedQuadRenderer.type(renderMode, quad.minimumAlpha()), poseStack);
                } finally {
                    mesh.close();
                }
            } catch (RuntimeException exception) {
                recordGpuFallback(RenderBackendFallbackReason.GPU_DRAW_FAILED, 4);
                renderUntexturedVertexColorTransientCpu(quad, poseStack, buffer, renderMode);
            }
        }

        @Override
        public void renderUntexturedVertexColorTransientTriangle(UntexturedVertexColorTransientTriangle triangle,
                PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                RenderBackendFallbackReason fallbackReason) {
            RenderBackendFallbackReason unsupported = unsupportedUntexturedVertexColorTransientReason();
            if (unsupported != RenderBackendFallbackReason.NONE) {
                if (unsupported == RenderBackendFallbackReason.GPU_DISABLED) {
                    cpuFallback.renderUntexturedVertexColorTransientTriangle(triangle, poseStack, buffer, renderMode,
                            fallbackReason);
                    return;
                }
                recordGpuFallback(unsupported, 3);
                renderUntexturedVertexColorTransientCpu(triangle, poseStack, buffer, renderMode);
                return;
            }
            try {
                GpuMesh mesh = uploadTransientUntexturedVertexColorMesh(triangle);
                try {
                    drawMesh(mesh, LegacyUntexturedQuadRenderer.type(renderMode, triangle.minimumAlpha(),
                            VertexFormat.Mode.TRIANGLES), poseStack);
                } finally {
                    mesh.close();
                }
            } catch (RuntimeException exception) {
                recordGpuFallback(RenderBackendFallbackReason.GPU_DRAW_FAILED, 3);
                renderUntexturedVertexColorTransientCpu(triangle, poseStack, buffer, renderMode);
            }
        }

        @Override
        public void renderUntexturedVertexColorTransientTriangles(
                List<UntexturedVertexColorTransientTriangle> triangles,
                PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                RenderBackendFallbackReason fallbackReason) {
            RenderBackendFallbackReason unsupported = unsupportedUntexturedVertexColorTransientReason();
            if (unsupported != RenderBackendFallbackReason.NONE) {
                if (unsupported == RenderBackendFallbackReason.GPU_DISABLED) {
                    cpuFallback.renderUntexturedVertexColorTransientTriangles(triangles, poseStack, buffer,
                            renderMode, fallbackReason);
                    return;
                }
                recordGpuFallback(unsupported, triangles.size() * 3);
                renderUntexturedVertexColorTransientTrianglesCpu(triangles, poseStack, buffer, renderMode);
                return;
            }
            try {
                GpuMesh mesh = uploadTransientUntexturedVertexColorMesh(triangles);
                try {
                    drawMesh(mesh, LegacyUntexturedQuadRenderer.type(renderMode, minimumTriangleAlpha(triangles),
                            VertexFormat.Mode.TRIANGLES), poseStack);
                } finally {
                    mesh.close();
                }
            } catch (RuntimeException exception) {
                recordGpuFallback(RenderBackendFallbackReason.GPU_DRAW_FAILED, triangles.size() * 3);
                renderUntexturedVertexColorTransientTrianglesCpu(triangles, poseStack, buffer, renderMode);
            }
        }

        @Override
        public void renderUntexturedLineTransient(List<UntexturedLineTransient> lines,
                PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode, float lineWidth,
                RenderBackendFallbackReason fallbackReason) {
            RenderBackendFallbackReason unsupported = unsupportedUntexturedVertexColorTransientReason();
            if (unsupported != RenderBackendFallbackReason.NONE) {
                if (unsupported == RenderBackendFallbackReason.GPU_DISABLED) {
                    cpuFallback.renderUntexturedLineTransient(lines, poseStack, buffer, renderMode, lineWidth,
                            fallbackReason);
                    return;
                }
                recordGpuFallback(unsupported, lines.size() * 2);
                renderUntexturedLineTransientCpu(lines, poseStack, buffer, renderMode, lineWidth);
                return;
            }
            try {
                GpuMesh mesh = uploadTransientUntexturedLineMesh(lines);
                try {
                    drawMesh(mesh, LegacyLineRenderer.type(lineWidth, renderMode, minimumLineAlpha(lines)), poseStack);
                } finally {
                    mesh.close();
                }
            } catch (RuntimeException exception) {
                recordGpuFallback(RenderBackendFallbackReason.GPU_DRAW_FAILED, lines.size() * 2);
                renderUntexturedLineTransientCpu(lines, poseStack, buffer, renderMode, lineWidth);
            }
        }

        private RenderBackendFallbackReason unsupportedTexturedReason(PreparedBatch batch, int alpha,
                boolean legacyShadow, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            if (batch.empty()) {
                return RenderBackendFallbackReason.NONE;
            }
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            if (!flags.experimentalGpuBackendEnabled()) {
                return RenderBackendFallbackReason.GPU_DISABLED;
            }
            if (flags.shaderPackDetected() || flags.shaderShadowPass()) {
                return canUseIrisSingleMeshPath(flags, renderMode, alpha, legacyShadow, uvTransform)
                        || canQueueIrisCompanionPath(flags, renderMode, alpha, legacyShadow, uvTransform)
                        || canUseIrisDynamicUvTransientCompanionPath(flags, renderMode, alpha, legacyShadow,
                                uvTransform)
                        || canUseIrisGlintTransientCompanionPath(flags, renderMode, alpha, legacyShadow)
                        ? RenderBackendFallbackReason.NONE
                        : irisCompanionMaterialFallbackReason(renderMode, alpha, legacyShadow, uvTransform);
            }
            if (!flags.gpuBackendAllowed()) {
                return RenderBackendFallbackReason.GPU_SHADER_ACTIVE;
            }
            if (!RenderSystem.isOnRenderThread()) {
                return RenderBackendFallbackReason.GPU_NOT_RENDER_THREAD;
            }
            if (renderMode.translucent() && !supportsSingleMeshDepthWriteTransparent(renderMode)) {
                RenderBackendFallbackReason materialReason = instancedTailMaterialFallbackReason(renderMode,
                        flags.instancingEnabled());
                if (materialReason != RenderBackendFallbackReason.NONE) {
                    return materialReason;
                }
            }
            if (alpha != 255 && !supportsInstancedAlpha(renderMode)
                    && !supportsSingleMeshDepthWriteTransparent(renderMode)) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_ALPHA;
            }
            if (!uvTransform.gpuMeshCacheable()
                    && (!flags.instancingEnabled() || !canUseInstancedPath(renderMode))) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM;
            }
            if (!UvTransform.DEFAULT.equals(uvTransform) && renderMode.translucent()
                    && (!flags.instancingEnabled() || !canUseInstancedPath(renderMode))) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM;
            }
            if (legacyShadow) {
                return RenderBackendFallbackReason.GPU_LEGACY_SHADOW;
            }
            return RenderBackendFallbackReason.NONE;
        }

        private RenderBackendFallbackReason unsupportedSpriteReason(PreparedBatch batch, int alpha,
                boolean legacyShadow, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            if (batch.empty()) {
                return RenderBackendFallbackReason.NONE;
            }
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            if (!flags.experimentalGpuBackendEnabled()) {
                return RenderBackendFallbackReason.GPU_DISABLED;
            }
            if (flags.shaderPackDetected() || flags.shaderShadowPass()) {
                return canUseIrisSingleMeshPath(flags, renderMode, alpha, legacyShadow, uvTransform)
                        || canQueueIrisCompanionPath(flags, renderMode, alpha, legacyShadow, uvTransform)
                        || canUseIrisDynamicUvTransientCompanionPath(flags, renderMode, alpha, legacyShadow,
                                uvTransform)
                        ? RenderBackendFallbackReason.NONE
                        : irisCompanionMaterialFallbackReason(renderMode, alpha, legacyShadow, uvTransform);
            }
            if (!flags.gpuBackendAllowed()) {
                return RenderBackendFallbackReason.GPU_SHADER_ACTIVE;
            }
            if (!RenderSystem.isOnRenderThread()) {
                return RenderBackendFallbackReason.GPU_NOT_RENDER_THREAD;
            }
            if (renderMode.translucent() && !supportsSingleMeshDepthWriteTransparent(renderMode)) {
                RenderBackendFallbackReason materialReason = instancedTailMaterialFallbackReason(renderMode,
                        flags.instancingEnabled());
                if (materialReason != RenderBackendFallbackReason.NONE) {
                    return materialReason;
                }
            }
            if (alpha != 255 && !supportsInstancedAlpha(renderMode)
                    && !supportsSingleMeshDepthWriteTransparent(renderMode)) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_ALPHA;
            }
            if (!uvTransform.gpuMeshCacheable()
                    && (!flags.instancingEnabled() || !canUseInstancedPath(renderMode))) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM;
            }
            if (!UvTransform.DEFAULT.equals(uvTransform) && renderMode.translucent()
                    && (!flags.instancingEnabled() || !canUseInstancedPath(renderMode))) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM;
            }
            if (legacyShadow) {
                return RenderBackendFallbackReason.GPU_LEGACY_SHADOW;
            }
            return RenderBackendFallbackReason.NONE;
        }

        private RenderBackendFallbackReason unsupportedUntexturedReason(PreparedBatch batch, int alpha,
                LegacyTexturedRenderMode renderMode) {
            if (batch.empty()) {
                return RenderBackendFallbackReason.NONE;
            }
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            if (!flags.experimentalGpuBackendEnabled()) {
                return RenderBackendFallbackReason.GPU_DISABLED;
            }
            if (flags.shaderPackDetected() || flags.shaderShadowPass()) {
                return canUseIrisUntexturedSingleMeshPath(flags, renderMode, alpha)
                        || canQueueIrisUntexturedCompanionPath(flags, renderMode, alpha)
                        ? RenderBackendFallbackReason.NONE
                        : untexturedMaterialFallbackReason(renderMode, alpha);
            }
            if (!flags.gpuBackendAllowed()) {
                return RenderBackendFallbackReason.GPU_SHADER_ACTIVE;
            }
            if (!RenderSystem.isOnRenderThread()) {
                return RenderBackendFallbackReason.GPU_NOT_RENDER_THREAD;
            }
            if (renderMode.translucent() && !supportsSingleMeshUntexturedTransparent(renderMode)) {
                return untexturedMaterialFallbackReason(renderMode, alpha);
            }
            if (alpha != 255 && !supportsSingleMeshUntexturedTransparent(renderMode)) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_ALPHA;
            }
            return RenderBackendFallbackReason.NONE;
        }

        private RenderBackendFallbackReason unsupportedTexturedTransientReason(PreparedBatch batch,
                boolean legacyShadow) {
            if (batch.empty()) {
                return RenderBackendFallbackReason.NONE;
            }
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            if (!flags.experimentalGpuBackendEnabled()) {
                return RenderBackendFallbackReason.GPU_DISABLED;
            }
            if (flags.shaderPackDetected() || flags.shaderShadowPass() || !flags.gpuBackendAllowed()) {
                return RenderBackendFallbackReason.GPU_SHADER_ACTIVE;
            }
            if (!RenderSystem.isOnRenderThread()) {
                return RenderBackendFallbackReason.GPU_NOT_RENDER_THREAD;
            }
            if (legacyShadow) {
                return RenderBackendFallbackReason.GPU_LEGACY_SHADOW;
            }
            return RenderBackendFallbackReason.NONE;
        }

        private RenderBackendFallbackReason unsupportedUntexturedTransientReason(PreparedBatch batch) {
            if (batch.empty()) {
                return RenderBackendFallbackReason.NONE;
            }
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            if (!flags.experimentalGpuBackendEnabled()) {
                return RenderBackendFallbackReason.GPU_DISABLED;
            }
            if (flags.shaderPackDetected() || flags.shaderShadowPass() || !flags.gpuBackendAllowed()) {
                return RenderBackendFallbackReason.GPU_SHADER_ACTIVE;
            }
            if (!RenderSystem.isOnRenderThread()) {
                return RenderBackendFallbackReason.GPU_NOT_RENDER_THREAD;
            }
            return RenderBackendFallbackReason.NONE;
        }

        private RenderBackendFallbackReason unsupportedUntexturedVertexColorTransientReason() {
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            if (!flags.experimentalGpuBackendEnabled()) {
                return RenderBackendFallbackReason.GPU_DISABLED;
            }
            if (flags.shaderPackDetected() || flags.shaderShadowPass() || !flags.gpuBackendAllowed()) {
                return RenderBackendFallbackReason.GPU_SHADER_ACTIVE;
            }
            if (!RenderSystem.isOnRenderThread()) {
                return RenderBackendFallbackReason.GPU_NOT_RENDER_THREAD;
            }
            return RenderBackendFallbackReason.NONE;
        }

        private static RenderBackendFallbackReason irisCompanionMaterialFallbackReason(
                LegacyTexturedRenderMode renderMode, int alpha, boolean legacyShadow, UvTransform uvTransform) {
            if (!uvTransform.gpuMeshCacheable()) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM;
            }
            if (legacyShadow) {
                return RenderBackendFallbackReason.GPU_LEGACY_SHADOW;
            }
            if (renderMode == LegacyTexturedRenderMode.GLINT_NO_DEPTH_WRITE
                    || renderMode == LegacyTexturedRenderMode.GLINT_EQUAL_DEPTH) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_GLINT;
            }
            if (isDepthWriteTransparentMode(renderMode)) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_DEPTH_WRITE_TRANSPARENT;
            }
            if (alpha != 255 && !supportsIrisCompanionAdditiveTail(renderMode)
                    && !supportsIrisCompanionNormalAlphaTail(renderMode)) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_ALPHA;
            }
            return RenderBackendFallbackReason.IRIS_COMPANION_UNSUPPORTED;
        }

        private static RenderBackendFallbackReason instancedTailMaterialFallbackReason(
                LegacyTexturedRenderMode renderMode, boolean instancingEnabled) {
            if (renderMode == LegacyTexturedRenderMode.GLINT_NO_DEPTH_WRITE
                    || renderMode == LegacyTexturedRenderMode.GLINT_EQUAL_DEPTH) {
                return instancingEnabled ? RenderBackendFallbackReason.NONE
                        : RenderBackendFallbackReason.GPU_UNSUPPORTED_GLINT;
            }
            if (isDepthWriteTransparentMode(renderMode)) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_DEPTH_WRITE_TRANSPARENT;
            }
            if (!supportsInstancedTail(renderMode) || !instancingEnabled) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_RENDER_MODE;
            }
            return RenderBackendFallbackReason.NONE;
        }

        private static RenderBackendFallbackReason untexturedMaterialFallbackReason(
                LegacyTexturedRenderMode renderMode, int alpha) {
            if (renderMode == LegacyTexturedRenderMode.GLINT_NO_DEPTH_WRITE
                    || renderMode == LegacyTexturedRenderMode.GLINT_EQUAL_DEPTH) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_GLINT;
            }
            if (isDepthWriteTransparentMode(renderMode)) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_DEPTH_WRITE_TRANSPARENT;
            }
            if (alpha != 255) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_ALPHA;
            }
            return RenderBackendFallbackReason.GPU_UNTEXTURED_UNSUPPORTED;
        }

        private static boolean isDepthWriteTransparentMode(LegacyTexturedRenderMode renderMode) {
            return renderMode == LegacyTexturedRenderMode.TRANSLUCENT
                    || renderMode == LegacyTexturedRenderMode.TRANSLUCENT_DEPTH_WRITE
                    || renderMode == LegacyTexturedRenderMode.ADDITIVE_DEPTH_WRITE;
        }

        private static boolean supportsSingleMeshDepthWriteTransparent(LegacyTexturedRenderMode renderMode) {
            return isDepthWriteTransparentMode(renderMode);
        }

        private static boolean canUseInstancedPath(LegacyTexturedRenderMode renderMode) {
            return !renderMode.translucent() || supportsInstancedTail(renderMode);
        }

        private static boolean supportsSingleMeshUntexturedTransparent(LegacyTexturedRenderMode renderMode) {
            return renderMode == LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE
                    || renderMode == LegacyTexturedRenderMode.ADDITIVE_CULL_NO_DEPTH_WRITE
                    || renderMode == LegacyTexturedRenderMode.ADDITIVE_DEPTH_WRITE
                    || renderMode == LegacyTexturedRenderMode.TRANSLUCENT
                    || renderMode == LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE
                    || renderMode == LegacyTexturedRenderMode.TRANSLUCENT_DEPTH_WRITE;
        }

        private static boolean canUseInstancedUntexturedPath(LegacyTexturedRenderMode renderMode, int alpha) {
            return !renderMode.translucent() && alpha == 255;
        }

        private static boolean canUseIrisUntexturedSingleMeshPath(HbmRenderFrameFlags.Snapshot flags,
                LegacyTexturedRenderMode renderMode, int alpha) {
            boolean safeSolidCompanion = !renderMode.translucent() && alpha == 255;
            boolean transparentMainPassCompanion = supportsSingleMeshUntexturedTransparent(renderMode)
                    && !flags.shaderShadowPass();
            return flags.experimentalGpuBackendEnabled()
                    && flags.irisExtendedShaderPathEnabled()
                    && flags.shaderPackDetected()
                    && (safeSolidCompanion || transparentMainPassCompanion)
                    && RenderSystem.isOnRenderThread();
        }

        private static boolean canQueueIrisUntexturedCompanionPath(HbmRenderFrameFlags.Snapshot flags,
                LegacyTexturedRenderMode renderMode, int alpha) {
            return canUseIrisUntexturedSingleMeshPath(flags, renderMode, alpha)
                    && !renderMode.translucent()
                    && alpha == 255
                    && !HbmShaderCompatibilityDetector.isRenderingShadowPass();
        }

        private static boolean canUseIrisSingleMeshPath(HbmRenderFrameFlags.Snapshot flags,
                LegacyTexturedRenderMode renderMode, int alpha, boolean legacyShadow, UvTransform uvTransform) {
            boolean safeOpaqueCompanion = !renderMode.translucent() && alpha == 255;
            boolean depthWriteTransparentCompanion = isDepthWriteTransparentMode(renderMode)
                    && !flags.shaderShadowPass();
            return flags.experimentalGpuBackendEnabled()
                    && flags.irisExtendedShaderPathEnabled()
                    && flags.shaderPackDetected()
                    && (safeOpaqueCompanion || depthWriteTransparentCompanion)
                    && !legacyShadow
                    && uvTransform.gpuMeshCacheable()
                    && RenderSystem.isOnRenderThread();
        }

        private static boolean canUseIrisTexturedTransientCompanionPath(HbmRenderFrameFlags.Snapshot flags,
                LegacyTexturedRenderMode renderMode, int alpha, boolean legacyShadow, UvTransform uvTransform) {
            boolean safeOpaqueCompanion = !renderMode.translucent() && alpha == 255;
            boolean transparentMainPassCompanion = !flags.shaderShadowPass()
                    && (isDepthWriteTransparentMode(renderMode)
                    || supportsIrisCompanionAdditiveTail(renderMode)
                    || supportsIrisCompanionNormalAlphaTail(renderMode));
            return flags.experimentalGpuBackendEnabled()
                    && flags.irisExtendedShaderPathEnabled()
                    && flags.shaderPackDetected()
                    && (safeOpaqueCompanion || transparentMainPassCompanion)
                    && !legacyShadow
                    && uvTransform.gpuMeshCacheable()
                    && RenderSystem.isOnRenderThread();
        }

        private static boolean canUseIrisDynamicUvTransientCompanionPath(HbmRenderFrameFlags.Snapshot flags,
                LegacyTexturedRenderMode renderMode, int alpha, boolean legacyShadow, UvTransform uvTransform) {
            return canUseIrisTexturedTransientCompanionPath(flags, renderMode, alpha, legacyShadow,
                    UvTransform.DEFAULT)
                    && !flags.shaderShadowPass()
                    && !uvTransform.gpuMeshCacheable()
                    && !HbmShaderCompatibilityDetector.isRenderingShadowPass();
        }

        private static boolean canQueueIrisCompanionPath(HbmRenderFrameFlags.Snapshot flags,
                LegacyTexturedRenderMode renderMode, int alpha, boolean legacyShadow, UvTransform uvTransform) {
            boolean safeOpaqueCompanion = !renderMode.translucent() && alpha == 255;
            boolean safeAdditiveTail = supportsIrisCompanionAdditiveTail(renderMode);
            boolean safeNormalAlphaTail = supportsIrisCompanionNormalAlphaTail(renderMode);
            return flags.experimentalGpuBackendEnabled()
                    && flags.irisExtendedShaderPathEnabled()
                    && flags.shaderPackDetected()
                    && (safeOpaqueCompanion || safeAdditiveTail || safeNormalAlphaTail)
                    && !legacyShadow
                    && uvTransform.gpuMeshCacheable()
                    && RenderSystem.isOnRenderThread()
                    && !HbmShaderCompatibilityDetector.isRenderingShadowPass();
        }

        private static boolean canUseIrisGlintTransientCompanionPath(HbmRenderFrameFlags.Snapshot flags,
                LegacyTexturedRenderMode renderMode, int alpha, boolean legacyShadow) {
            return flags.experimentalGpuBackendEnabled()
                    && flags.irisExtendedShaderPathEnabled()
                    && flags.shaderPackDetected()
                    && !flags.shaderShadowPass()
                    && supportsInstancedGlintTail(renderMode)
                    && alpha == 255
                    && !legacyShadow
                    && RenderSystem.isOnRenderThread()
                    && !HbmShaderCompatibilityDetector.isRenderingShadowPass();
        }

        private static boolean supportsIrisCompanionAdditiveTail(LegacyTexturedRenderMode renderMode) {
            return renderMode == LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE
                    || renderMode == LegacyTexturedRenderMode.ADDITIVE_CULL_NO_DEPTH_WRITE;
        }

        private static boolean supportsIrisCompanionNormalAlphaTail(LegacyTexturedRenderMode renderMode) {
            return renderMode == LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE;
        }

        private static boolean isIrisCompanionNormalAlphaMode(LegacyTexturedRenderMode renderMode) {
            return supportsIrisCompanionNormalAlphaTail(renderMode);
        }

        private static boolean supportsInstancedAdditiveTail(LegacyTexturedRenderMode renderMode) {
            return renderMode == LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE
                    || renderMode == LegacyTexturedRenderMode.ADDITIVE_CULL_NO_DEPTH_WRITE;
        }

        private static boolean supportsInstancedNormalAlphaTail(LegacyTexturedRenderMode renderMode) {
            return renderMode == LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE;
        }

        private static boolean supportsInstancedGlintTail(LegacyTexturedRenderMode renderMode) {
            return renderMode == LegacyTexturedRenderMode.GLINT_NO_DEPTH_WRITE
                    || renderMode == LegacyTexturedRenderMode.GLINT_EQUAL_DEPTH;
        }

        private static boolean supportsInstancedTail(LegacyTexturedRenderMode renderMode) {
            return supportsInstancedAdditiveTail(renderMode)
                    || supportsInstancedNormalAlphaTail(renderMode)
                    || supportsInstancedGlintTail(renderMode);
        }

        private static boolean isInstancedAdditiveMode(LegacyTexturedRenderMode renderMode) {
            return supportsInstancedAdditiveTail(renderMode);
        }

        private static boolean isInstancedNormalAlphaMode(LegacyTexturedRenderMode renderMode) {
            return supportsInstancedNormalAlphaTail(renderMode);
        }

        private static boolean isInstancedGlintMode(LegacyTexturedRenderMode renderMode) {
            return supportsInstancedGlintTail(renderMode);
        }

        private static boolean supportsInstancedAlpha(LegacyTexturedRenderMode renderMode) {
            return supportsInstancedTail(renderMode);
        }

        private static boolean requiresInstancedGpuPath(LegacyTexturedRenderMode renderMode) {
            return supportsInstancedTail(renderMode);
        }

        private void renderTexturedCpuFallback(RenderBackendFallbackReason reason, PreparedBatch batch,
                ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            recordGpuFallback(reason, batch.vertexCount());
            cpuFallback.renderTextured(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
        }

        private void renderSpriteCpuFallback(RenderBackendFallbackReason reason, PreparedBatch batch,
                TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
                boolean partBrightness, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            recordGpuFallback(reason, batch.vertexCount());
            cpuFallback.renderSprite(batch, sprite, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha, legacyShadow, partBrightness, renderMode, uvTransform);
        }

        private void renderUntexturedCpuFallback(RenderBackendFallbackReason reason, PreparedBatch batch,
                PoseStack poseStack, MultiBufferSource buffer, int red, int green, int blue, int alpha,
                LegacyTexturedRenderMode renderMode) {
            recordGpuFallback(reason, batch.vertexCount());
            cpuFallback.renderUntextured(batch, poseStack, buffer, red, green, blue, alpha, renderMode);
        }

        private boolean queueInstanced(PreparedBatch batch, ResourceLocation textureLocation, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            if (!UvTransform.DEFAULT.equals(uvTransform)) {
                recordInstancedFallback(RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM, 1, renderMode);
                return false;
            }
            if (HbmOptimizedRenderShaders.blockLitInstancedShader() == null) {
                recordInstancedFallback(RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE, 1, renderMode);
                return false;
            }
            List<InstancedMesh> meshesToQueue = new ArrayList<>(2);
            if (!batch.quadVertices().isEmpty()) {
                meshesToQueue.add(instancedMeshFor(batch, VertexFormat.Mode.QUADS, smoothing,
                        GpuMeshKind.TEXTURED, null,
                        batch.quadVertices()));
            }
            if (!batch.triangleVertices().isEmpty()) {
                meshesToQueue.add(instancedMeshFor(batch, VertexFormat.Mode.TRIANGLES, smoothing,
                        GpuMeshKind.TEXTURED, null,
                        batch.triangleVertices()));
            }
            for (InstancedMesh mesh : meshesToQueue) {
                queueInstanced(mesh, textureLocation, renderMode, poseStack, buffer, packedLight, packedOverlay,
                        red, green, blue, alpha, uvTransform);
            }
            return !meshesToQueue.isEmpty();
        }

        private boolean queueInstancedSprite(PreparedBatch batch, TextureAtlasSprite sprite, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            if (!UvTransform.DEFAULT.equals(uvTransform)) {
                recordInstancedFallback(RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM, 1, renderMode);
                return false;
            }
            if (HbmOptimizedRenderShaders.blockLitInstancedShader() == null) {
                recordInstancedFallback(RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE, 1, renderMode);
                return false;
            }
            List<InstancedMesh> meshesToQueue = new ArrayList<>(2);
            if (!batch.quadVertices().isEmpty()) {
                meshesToQueue.add(instancedMeshFor(batch, VertexFormat.Mode.QUADS, false,
                        GpuMeshKind.SPRITE, sprite,
                        batch.quadVertices()));
            }
            if (!batch.triangleVertices().isEmpty()) {
                meshesToQueue.add(instancedMeshFor(batch, VertexFormat.Mode.TRIANGLES, false,
                        GpuMeshKind.SPRITE, sprite,
                        batch.triangleVertices()));
            }
            for (InstancedMesh mesh : meshesToQueue) {
                queueInstanced(mesh, InventoryMenu.BLOCK_ATLAS, renderMode, poseStack, buffer, packedLight,
                        packedOverlay, red, green, blue, alpha, uvTransform);
            }
            return !meshesToQueue.isEmpty();
        }

        private boolean queueInstancedUntextured(PreparedBatch batch, PoseStack poseStack, MultiBufferSource buffer,
                int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
            if (HbmOptimizedRenderShaders.blockUntexturedInstancedShader() == null) {
                recordInstancedFallback(RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE, 1, renderMode);
                return false;
            }
            List<InstancedMesh> meshesToQueue = new ArrayList<>(2);
            if (!batch.quadVertices().isEmpty()) {
                meshesToQueue.add(instancedMeshFor(batch, VertexFormat.Mode.QUADS, false,
                        GpuMeshKind.UNTEXTURED, null, batch.quadVertices()));
            }
            if (!batch.triangleVertices().isEmpty()) {
                meshesToQueue.add(instancedMeshFor(batch, VertexFormat.Mode.TRIANGLES, false,
                        GpuMeshKind.UNTEXTURED, null, batch.triangleVertices()));
            }
            for (InstancedMesh mesh : meshesToQueue) {
                queueInstanced(mesh, InventoryMenu.BLOCK_ATLAS, renderMode, poseStack, buffer,
                        LightTexture.FULL_BRIGHT, 0, red, green, blue, alpha, UvTransform.DEFAULT);
            }
            return !meshesToQueue.isEmpty();
        }

        private void queueInstanced(InstancedMesh mesh, ResourceLocation textureLocation, LegacyTexturedRenderMode renderMode,
                PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, UvTransform uvTransform) {
            InstancedBatchKey key = new InstancedBatchKey(mesh.key(), textureLocation, renderMode);
            InstancedBatch batch = pendingInstancedBatches.computeIfAbsent(key,
                    ignored -> new InstancedBatch(mesh, textureLocation, renderMode));
            batch.instances().add(InstancedInstance.from(poseStack.last().pose(),
                    LegacyRenderLighting.currentInstanceLightProbe(packedLight), packedOverlay, red, green, blue,
                    alpha));
            batch.fallbacks().add(InstancedFallbackInstance.from(poseStack.last(), buffer, packedLight, packedOverlay,
                    red, green, blue, alpha, uvTransform));
            instancedQueuedInstances.incrementAndGet();
            currentFrameInstancedQueuedInstances.incrementAndGet();
            if (isInstancedAdditiveMode(renderMode)) {
                instancedAdditiveQueuedInstances.incrementAndGet();
                currentFrameInstancedAdditiveQueuedInstances.incrementAndGet();
            }
            if (batch.instances().size() == 1) {
                instancedQueuedBatches.incrementAndGet();
                currentFrameInstancedQueuedBatches.incrementAndGet();
                if (isInstancedAdditiveMode(renderMode)) {
                    instancedAdditiveQueuedBatches.incrementAndGet();
                    currentFrameInstancedAdditiveQueuedBatches.incrementAndGet();
                }
            }
        }

        private boolean queueIrisCompanion(PreparedBatch batch, ResourceLocation textureLocation, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            boolean queued = false;
            if (!batch.quadVertices().isEmpty()) {
                queueIrisCompanionPart(GpuMeshKind.TEXTURED, batch.stableKey(), batch.quadVertices(),
                        VertexFormat.Mode.QUADS, null, textureLocation, renderMode, poseStack, buffer, packedLight,
                        packedOverlay, red, green, blue, alpha, smoothing, uvTransform);
                queued = true;
            }
            if (!batch.triangleVertices().isEmpty()) {
                queueIrisCompanionPart(GpuMeshKind.TEXTURED, batch.stableKey(), batch.triangleVertices(),
                        VertexFormat.Mode.TRIANGLES, null, textureLocation, renderMode, poseStack, buffer,
                        packedLight, packedOverlay, red, green, blue, alpha, smoothing, uvTransform);
                queued = true;
            }
            return queued;
        }

        private boolean queueIrisCompanionSprite(PreparedBatch batch, TextureAtlasSprite sprite, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            boolean queued = false;
            if (!batch.quadVertices().isEmpty()) {
                queueIrisCompanionPart(GpuMeshKind.SPRITE, batch.stableKey(), batch.quadVertices(),
                        VertexFormat.Mode.QUADS, sprite, InventoryMenu.BLOCK_ATLAS, renderMode, poseStack, buffer,
                        packedLight, packedOverlay, red, green, blue, alpha, false, uvTransform);
                queued = true;
            }
            if (!batch.triangleVertices().isEmpty()) {
                queueIrisCompanionPart(GpuMeshKind.SPRITE, batch.stableKey(), batch.triangleVertices(),
                        VertexFormat.Mode.TRIANGLES, sprite, InventoryMenu.BLOCK_ATLAS, renderMode, poseStack,
                        buffer, packedLight, packedOverlay, red, green, blue, alpha, false, uvTransform);
                queued = true;
            }
            return queued;
        }

        private boolean queueIrisCompanionUntextured(PreparedBatch batch, PoseStack poseStack,
                MultiBufferSource buffer, int red, int green, int blue, int alpha,
                LegacyTexturedRenderMode renderMode) {
            ResourceLocation textureLocation = irisUntexturedWhiteTexture();
            boolean queued = false;
            if (!batch.quadVertices().isEmpty()) {
                queueIrisCompanionPart(GpuMeshKind.UNTEXTURED, batch.stableKey(), batch.quadVertices(),
                        VertexFormat.Mode.QUADS, null, textureLocation, renderMode, poseStack, buffer,
                        LightTexture.FULL_BRIGHT, 0, red, green, blue, alpha, false, UvTransform.DEFAULT);
                queued = true;
            }
            if (!batch.triangleVertices().isEmpty()) {
                queueIrisCompanionPart(GpuMeshKind.UNTEXTURED, batch.stableKey(), batch.triangleVertices(),
                        VertexFormat.Mode.TRIANGLES, null, textureLocation, renderMode, poseStack, buffer,
                        LightTexture.FULL_BRIGHT, 0, red, green, blue, alpha, false, UvTransform.DEFAULT);
                queued = true;
            }
            return queued;
        }

        private void queueIrisCompanionPart(GpuMeshKind kind, String stablePartKey, List<PreparedVertex> vertices,
                VertexFormat.Mode sourceMode, TextureAtlasSprite sprite, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, boolean smoothing,
                UvTransform uvTransform) {
            IrisCompanionQueueKey key = new IrisCompanionQueueKey(kind, stablePartKey, sprite, vertices.size(),
                    sourceMode, smoothing, uvTransform, textureLocation, renderMode);
            IrisCompanionQueuedBatch batch = pendingIrisCompanionBatches.computeIfAbsent(key,
                    ignored -> new IrisCompanionQueuedBatch(key, List.copyOf(vertices)));
            batch.instances().add(IrisCompanionQueuedInstance.from(poseStack.last(), buffer, packedLight,
                    packedOverlay, red, green, blue, alpha));
            irisQueuedInstances.incrementAndGet();
            currentFrameIrisQueuedInstances.incrementAndGet();
            if (batch.instances().size() == 1) {
                irisQueuedBatches.incrementAndGet();
                currentFrameIrisQueuedBatches.incrementAndGet();
            }
        }

        private InstancedMesh instancedMeshFor(PreparedBatch batch, VertexFormat.Mode sourceMode, boolean smoothing,
                GpuMeshKind kind, TextureAtlasSprite sprite, List<PreparedVertex> vertices) {
            InstancedMeshKey key = new InstancedMeshKey(kind, batch.stableKey(), sprite, vertices.size(),
                    sourceMode, smoothing);
            if (failedInstancedKeys.contains(key)) {
                throw new IllegalStateException("Instanced mesh upload previously failed for " + key);
            }
            synchronized (instancedMeshes) {
                InstancedMesh existing = instancedMeshes.get(key);
                if (existing != null) {
                    return existing;
                }
                gpuUploadAttempts.incrementAndGet();
                try {
                    InstancedMesh created = uploadInstancedMesh(key, sourceMode, smoothing, vertices);
                    instancedMeshes.put(key, created);
                    gpuBufferBytes.addAndGet(created.byteSize());
                    return created;
                } catch (RuntimeException exception) {
                    failedInstancedKeys.add(key);
                    gpuUploadFailures.incrementAndGet();
                    throw exception;
                }
            }
        }

        private InstancedMesh uploadInstancedMesh(InstancedMeshKey key, VertexFormat.Mode sourceMode,
                boolean smoothing, List<PreparedVertex> vertices) {
            ByteBuffer vertexBytes = buildInstancedVertexBytes(key.kind(), sourceMode, vertices, smoothing,
                    key.sprite());
            int vao = 0;
            int vbo = 0;
            int instanceVbo = 0;
            try {
                vao = GL30.glGenVertexArrays();
                vbo = GL15.glGenBuffers();
                instanceVbo = GL15.glGenBuffers();
                if (vao == 0 || vbo == 0 || instanceVbo == 0) {
                    throw new IllegalStateException("Failed to allocate instanced OBJ GL buffers");
                }
                GL30.glBindVertexArray(vao);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBytes, GL15.GL_STATIC_DRAW);
                int vertexStride = INSTANCED_VERTEX_STRIDE_BYTES;
                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, vertexStride, 0L);
                GL20.glEnableVertexAttribArray(1);
                GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, vertexStride, 12L);
                GL20.glEnableVertexAttribArray(2);
                GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, vertexStride, 24L);
                GL20.glEnableVertexAttribArray(13);
                GL20.glVertexAttribPointer(13, 3, GL11.GL_FLOAT, false, vertexStride, 32L);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVbo);
                int instanceStride = InstancedInstance.FLOATS * 4;
                for (int attribute = 3; attribute <= 12; attribute++) {
                    GL20.glEnableVertexAttribArray(attribute);
                    GL20.glVertexAttribPointer(attribute, 4, GL11.GL_FLOAT, false, instanceStride,
                            (long) (attribute - 3) * 16L);
                    GL33.glVertexAttribDivisor(attribute, 1);
                }
                GL30.glBindVertexArray(0);
                return new InstancedMesh(key, sourceMode, List.copyOf(vertices), vao, vbo, instanceVbo,
                        vertexBytes.limit() / vertexStride, vertexBytes.limit());
            } catch (RuntimeException exception) {
                if (vao != 0) {
                    GL30.glDeleteVertexArrays(vao);
                }
                if (vbo != 0) {
                    GL15.glDeleteBuffers(vbo);
                }
                if (instanceVbo != 0) {
                    GL15.glDeleteBuffers(instanceVbo);
                }
                throw exception;
            } finally {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL30.glBindVertexArray(0);
            }
        }

        private ByteBuffer buildInstancedVertexBytes(GpuMeshKind kind, VertexFormat.Mode sourceMode,
                List<PreparedVertex> vertices, boolean smoothing, TextureAtlasSprite sprite) {
            int outputVertices = sourceMode == VertexFormat.Mode.QUADS ? vertices.size() / 4 * 6 : vertices.size();
            InstancedMeshBounds bounds = InstancedMeshBounds.of(vertices);
            ByteBuffer bytes = ByteBuffer.allocateDirect(Math.max(1, outputVertices) * INSTANCED_VERTEX_STRIDE_BYTES)
                    .order(ByteOrder.nativeOrder());
            if (sourceMode == VertexFormat.Mode.QUADS) {
                for (int i = 0; i + 3 < vertices.size(); i += 4) {
                    putInstancedVertex(bytes, vertices.get(i), smoothing, kind, sprite, bounds);
                    putInstancedVertex(bytes, vertices.get(i + 1), smoothing, kind, sprite, bounds);
                    putInstancedVertex(bytes, vertices.get(i + 2), smoothing, kind, sprite, bounds);
                    putInstancedVertex(bytes, vertices.get(i + 2), smoothing, kind, sprite, bounds);
                    putInstancedVertex(bytes, vertices.get(i + 3), smoothing, kind, sprite, bounds);
                    putInstancedVertex(bytes, vertices.get(i), smoothing, kind, sprite, bounds);
                }
            } else {
                for (PreparedVertex vertex : vertices) {
                    putInstancedVertex(bytes, vertex, smoothing, kind, sprite, bounds);
                }
            }
            bytes.flip();
            return bytes;
        }

        private void putInstancedVertex(ByteBuffer bytes, PreparedVertex vertex, boolean smoothing,
                GpuMeshKind kind, TextureAtlasSprite sprite,
                InstancedMeshBounds bounds) {
            Vector3f position = vertex.position();
            Vector3f normal = smoothing ? vertex.smoothNormal() : vertex.faceNormal();
            bytes.putFloat(position.x()).putFloat(position.y()).putFloat(position.z());
            bytes.putFloat(normal.x()).putFloat(normal.y()).putFloat(normal.z());
            UV uv = vertex.uv();
            if (kind == GpuMeshKind.UNTEXTURED) {
                bytes.putFloat(0.0F).putFloat(0.0F);
            } else if (kind == GpuMeshKind.SPRITE && sprite != null) {
                bytes.putFloat(sprite.getU(uv.u() * 16.0D));
                bytes.putFloat(sprite.getV(uv.v() * 16.0D));
            } else {
                bytes.putFloat(uv.u()).putFloat(uv.v());
            }
            bytes.putFloat(bounds.weightX(position.x()));
            bytes.putFloat(bounds.weightY(position.y()));
            bytes.putFloat(bounds.weightZ(position.z()));
        }

        private GpuMesh meshFor(PreparedBatch batch, VertexFormat.Mode drawMode, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, boolean smoothing, UvTransform uvTransform,
                List<PreparedVertex> vertices) {
            GpuMeshKey key = new GpuMeshKey(GpuMeshKind.TEXTURED, System.identityHashCode(batch), null,
                    batch.vertexCount(), drawMode, packedLight, packedOverlay, red, green, blue, alpha,
                    uvTransform, smoothing);
            if (failedKeys.contains(key)) {
                throw new IllegalStateException("GPU mesh upload previously failed for " + key);
            }
            synchronized (meshes) {
                GpuMesh existing = meshes.get(key);
                if (existing != null) {
                    return existing;
                }
                gpuUploadAttempts.incrementAndGet();
                try {
                    GpuMesh created = uploadMesh(drawMode, packedLight, packedOverlay, red, green, blue, alpha,
                            smoothing, uvTransform, vertices);
                    meshes.put(key, created);
                    gpuBufferBytes.addAndGet(created.byteSize());
                    return created;
                } catch (RuntimeException exception) {
                    failedKeys.add(key);
                    gpuUploadFailures.incrementAndGet();
                    throw exception;
                }
            }
        }

        private GpuMesh meshForAtlasSprite(PreparedBatch batch, VertexFormat.Mode drawMode, TextureAtlasSprite sprite,
                int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                UvTransform uvTransform, List<PreparedVertex> vertices) {
            GpuMeshKey key = new GpuMeshKey(GpuMeshKind.SPRITE, System.identityHashCode(batch), sprite,
                    batch.vertexCount(), drawMode, packedLight, packedOverlay, red, green, blue, alpha,
                    uvTransform, false);
            if (failedKeys.contains(key)) {
                throw new IllegalStateException("GPU sprite mesh upload previously failed for " + key);
            }
            synchronized (meshes) {
                GpuMesh existing = meshes.get(key);
                if (existing != null) {
                    return existing;
                }
                gpuUploadAttempts.incrementAndGet();
                try {
                    GpuMesh created = uploadMeshWithSprite(drawMode, sprite, packedLight, packedOverlay,
                            red, green, blue, alpha, uvTransform, vertices);
                    meshes.put(key, created);
                    gpuBufferBytes.addAndGet(created.byteSize());
                    return created;
                } catch (RuntimeException exception) {
                    failedKeys.add(key);
                    gpuUploadFailures.incrementAndGet();
                    throw exception;
                }
            }
        }

        private GpuMesh meshForUntextured(PreparedBatch batch, VertexFormat.Mode drawMode, int red, int green,
                int blue, int alpha, List<PreparedVertex> vertices) {
            GpuMeshKey key = new GpuMeshKey(GpuMeshKind.UNTEXTURED, System.identityHashCode(batch), null,
                    batch.vertexCount(), drawMode, 0, 0, red, green, blue, alpha, UvTransform.DEFAULT, false);
            if (failedKeys.contains(key)) {
                throw new IllegalStateException("GPU untextured mesh upload previously failed for " + key);
            }
            synchronized (meshes) {
                GpuMesh existing = meshes.get(key);
                if (existing != null) {
                    return existing;
                }
                gpuUploadAttempts.incrementAndGet();
                try {
                    GpuMesh created = uploadMeshUntextured(drawMode, red, green, blue, alpha, vertices);
                    meshes.put(key, created);
                    gpuBufferBytes.addAndGet(created.byteSize());
                    return created;
                } catch (RuntimeException exception) {
                    failedKeys.add(key);
                    gpuUploadFailures.incrementAndGet();
                    throw exception;
                }
            }
        }

        private GpuMesh uploadTransientTexturedMesh(VertexFormat.Mode drawMode, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, boolean smoothing, UvTransform uvTransform,
                List<PreparedVertex> vertices) {
            gpuUploadAttempts.incrementAndGet();
            try {
                return uploadMesh(drawMode, packedLight, packedOverlay, red, green, blue, alpha, smoothing,
                        uvTransform, vertices);
            } catch (RuntimeException exception) {
                gpuUploadFailures.incrementAndGet();
                throw exception;
            }
        }

        private GpuMesh uploadTransientUntexturedMesh(VertexFormat.Mode drawMode, int red, int green,
                int blue, int alpha, List<PreparedVertex> vertices) {
            gpuUploadAttempts.incrementAndGet();
            try {
                return uploadMeshUntextured(drawMode, red, green, blue, alpha, vertices);
            } catch (RuntimeException exception) {
                gpuUploadFailures.incrementAndGet();
                throw exception;
            }
        }

        private GpuMesh uploadTransientUntexturedVertexColorMesh(UntexturedVertexColorTransientQuad quad) {
            gpuUploadAttempts.incrementAndGet();
            try {
                BufferBuilder builder = new BufferBuilder(256);
                builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                emitUntexturedVertexColorIdentity(builder, quad.v0());
                emitUntexturedVertexColorIdentity(builder, quad.v1());
                emitUntexturedVertexColorIdentity(builder, quad.v2());
                emitUntexturedVertexColorIdentity(builder, quad.v3());
                BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
                VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                vertexBuffer.bind();
                vertexBuffer.upload(renderedBuffer);
                VertexBuffer.unbind();
                return new GpuMesh(vertexBuffer, 64L);
            } catch (RuntimeException exception) {
                gpuUploadFailures.incrementAndGet();
                throw exception;
            }
        }

        private GpuMesh uploadTransientUntexturedVertexColorMesh(UntexturedVertexColorTransientTriangle triangle) {
            gpuUploadAttempts.incrementAndGet();
            try {
                BufferBuilder builder = new BufferBuilder(256);
                builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
                emitUntexturedVertexColorIdentity(builder, triangle.v0());
                emitUntexturedVertexColorIdentity(builder, triangle.v1());
                emitUntexturedVertexColorIdentity(builder, triangle.v2());
                BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
                VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                vertexBuffer.bind();
                vertexBuffer.upload(renderedBuffer);
                VertexBuffer.unbind();
                return new GpuMesh(vertexBuffer, 48L);
            } catch (RuntimeException exception) {
                gpuUploadFailures.incrementAndGet();
                throw exception;
            }
        }

        private GpuMesh uploadTransientUntexturedVertexColorMesh(
                List<UntexturedVertexColorTransientTriangle> triangles) {
            gpuUploadAttempts.incrementAndGet();
            try {
                BufferBuilder builder = new BufferBuilder(Math.max(256, triangles.size() * 48));
                builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
                for (UntexturedVertexColorTransientTriangle triangle : triangles) {
                    emitUntexturedVertexColorIdentity(builder, triangle.v0());
                    emitUntexturedVertexColorIdentity(builder, triangle.v1());
                    emitUntexturedVertexColorIdentity(builder, triangle.v2());
                }
                BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
                VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                vertexBuffer.bind();
                vertexBuffer.upload(renderedBuffer);
                VertexBuffer.unbind();
                return new GpuMesh(vertexBuffer, triangles.size() * 48L);
            } catch (RuntimeException exception) {
                gpuUploadFailures.incrementAndGet();
                throw exception;
            }
        }

        private GpuMesh uploadTransientUntexturedLineMesh(List<UntexturedLineTransient> lines) {
            gpuUploadAttempts.incrementAndGet();
            try {
                BufferBuilder builder = new BufferBuilder(Math.max(256, lines.size() * 48));
                builder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
                for (UntexturedLineTransient line : lines) {
                    emitLineVertexIdentity(builder, line.x0(), line.y0(), line.z0(), line.color(), line.alpha(),
                            line.normalX(), line.normalY(), line.normalZ());
                    emitLineVertexIdentity(builder, line.x1(), line.y1(), line.z1(), line.color(), line.alpha(),
                            line.normalX(), line.normalY(), line.normalZ());
                }
                BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
                VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                vertexBuffer.bind();
                vertexBuffer.upload(renderedBuffer);
                VertexBuffer.unbind();
                return new GpuMesh(vertexBuffer, Math.max(0L, (long) lines.size() * 48L));
            } catch (RuntimeException exception) {
                gpuUploadFailures.incrementAndGet();
                throw exception;
            }
        }

        private static void emitLineVertexIdentity(VertexConsumer consumer, double x, double y, double z,
                int color, int alpha, float normalX, float normalY, float normalZ) {
            consumer.vertex((float) x, (float) y, (float) z)
                    .color(color >> 16 & 255, color >> 8 & 255, color & 255, clampColor(alpha))
                    .normal(normalX, normalY, normalZ)
                    .endVertex();
        }

        private GpuMesh uploadMesh(VertexFormat.Mode drawMode, int packedLight, int packedOverlay, int red, int green,
                int blue, int alpha, boolean smoothing, UvTransform uvTransform, List<PreparedVertex> vertices) {
            BufferBuilder builder = new BufferBuilder(Math.max(256, vertices.size() * 40));
            builder.begin(drawMode, DefaultVertexFormat.NEW_ENTITY);
            PoseStack identityStack = new PoseStack();
            PoseStack.Pose identity = identityStack.last();
            emitPreparedVertices(vertices, builder, identity.pose(), identity.normal(), packedLight, packedOverlay,
                    red, green, blue, alpha, false, smoothing, uvTransform);
            BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
            VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            vertexBuffer.bind();
            vertexBuffer.upload(renderedBuffer);
            VertexBuffer.unbind();
            return new GpuMesh(vertexBuffer, Math.max(0L, (long) vertices.size() * 40L));
        }

        private GpuMesh uploadMeshWithSprite(VertexFormat.Mode drawMode, TextureAtlasSprite sprite, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, UvTransform uvTransform,
                List<PreparedVertex> vertices) {
            BufferBuilder builder = new BufferBuilder(Math.max(256, vertices.size() * 40));
            builder.begin(drawMode, DefaultVertexFormat.NEW_ENTITY);
            PoseStack identityStack = new PoseStack();
            PoseStack.Pose identity = identityStack.last();
            emitPreparedVerticesWithSprite(vertices, sprite, builder, identity.pose(), identity.normal(),
                    packedLight, packedOverlay, red, green, blue, alpha, false, false, uvTransform);
            BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
            VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            vertexBuffer.bind();
            vertexBuffer.upload(renderedBuffer);
            VertexBuffer.unbind();
            return new GpuMesh(vertexBuffer, Math.max(0L, (long) vertices.size() * 40L));
        }

        private GpuMesh uploadMeshUntextured(VertexFormat.Mode drawMode, int red, int green, int blue, int alpha,
                List<PreparedVertex> vertices) {
            BufferBuilder builder = new BufferBuilder(Math.max(256, vertices.size() * 16));
            builder.begin(drawMode, DefaultVertexFormat.POSITION_COLOR);
            PoseStack identityStack = new PoseStack();
            emitPreparedVerticesUntextured(vertices, builder, identityStack.last().pose(), red, green, blue, alpha);
            BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
            VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            vertexBuffer.bind();
            vertexBuffer.upload(renderedBuffer);
            VertexBuffer.unbind();
            return new GpuMesh(vertexBuffer, Math.max(0L, (long) vertices.size() * 16L));
        }

        private void drawMesh(GpuMesh mesh, RenderType renderType, PoseStack poseStack) {
            renderType.setupRenderState();
            try {
                HbmRenderFrameLight.ensureLightTextureUpdated();
                ShaderInstance shader = RenderSystem.getShader();
                if (shader == null) {
                    throw new IllegalStateException("No shader bound for legacy OBJ GPU mesh");
                }
                mesh.vertexBuffer().bind();
                mesh.vertexBuffer().drawWithShader(poseStack.last().pose(), RenderSystem.getProjectionMatrix(), shader);
                VertexBuffer.unbind();
            } finally {
                renderType.clearRenderState();
            }
            gpuDrawCalls.incrementAndGet();
            currentFrameGpuDrawCalls.incrementAndGet();
        }

        private boolean drawIrisCompanionBatch(PreparedBatch batch, ResourceLocation textureLocation,
                PoseStack poseStack, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
                boolean shadowPass) {
            return drawIrisCompanionBatch(batch, null, textureLocation, poseStack, packedLight, packedOverlay,
                    red, green, blue, alpha, smoothing, renderMode, uvTransform, shadowPass, GpuMeshKind.TEXTURED);
        }

        private boolean drawIrisCompanionBatch(PreparedBatch batch, TextureAtlasSprite sprite,
                ResourceLocation textureLocation, PoseStack poseStack, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, boolean smoothing, LegacyTexturedRenderMode renderMode,
                UvTransform uvTransform, boolean shadowPass) {
            GpuMeshKind kind = sprite == null ? GpuMeshKind.TEXTURED : GpuMeshKind.SPRITE;
            return drawIrisCompanionBatch(batch, sprite, textureLocation, poseStack, packedLight, packedOverlay,
                    red, green, blue, alpha, smoothing, renderMode, uvTransform, shadowPass, kind);
        }

        private boolean drawIrisCompanionBatch(PreparedBatch batch, TextureAtlasSprite sprite,
                ResourceLocation textureLocation, PoseStack poseStack, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, boolean smoothing, LegacyTexturedRenderMode renderMode,
                UvTransform uvTransform, boolean shadowPass, GpuMeshKind kind) {
            ShaderInstance shader = HbmIrisExtendedShaderAccess.getBlockEntityShader(shadowPass);
            if (shader == null) {
                recordIrisFallback(RenderBackendFallbackReason.IRIS_SHADER_UNAVAILABLE, batch.vertexCount());
                return false;
            }
            boolean drew = false;
            irisEligibleBatches.incrementAndGet();
            currentFrameIrisEligibleBatches.incrementAndGet();
            if (!batch.quadVertices().isEmpty()) {
                IrisCompanionMesh mesh = irisCompanionMeshFor(batch, VertexFormat.Mode.QUADS, kind, sprite,
                        packedLight, packedOverlay, red, green, blue, alpha, smoothing, uvTransform,
                        batch.quadVertices());
                drawIrisCompanionMesh(mesh, textureLocation, renderMode, poseStack, packedLight, packedOverlay,
                        red, green, blue, alpha, shader, shadowPass);
                drew = true;
            }
            if (!batch.triangleVertices().isEmpty()) {
                IrisCompanionMesh mesh = irisCompanionMeshFor(batch, VertexFormat.Mode.TRIANGLES, kind, sprite,
                        packedLight, packedOverlay, red, green, blue, alpha, smoothing, uvTransform,
                        batch.triangleVertices());
                drawIrisCompanionMesh(mesh, textureLocation, renderMode, poseStack, packedLight, packedOverlay,
                        red, green, blue, alpha, shader, shadowPass);
                drew = true;
            }
            return drew;
        }

        private boolean drawIrisTransientCompanionBatch(PreparedBatch batch, TextureAtlasSprite sprite,
                ResourceLocation textureLocation, PoseStack poseStack, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, boolean smoothing, LegacyTexturedRenderMode renderMode,
                UvTransform uvTransform, GpuMeshKind kind) {
            boolean shadowPass = HbmShaderCompatibilityDetector.isRenderingShadowPass();
            ShaderInstance shader = HbmIrisExtendedShaderAccess.getBlockEntityShader(shadowPass);
            if (shader == null) {
                recordIrisFallback(RenderBackendFallbackReason.IRIS_SHADER_UNAVAILABLE, batch.vertexCount());
                return false;
            }
            boolean drew = false;
            irisEligibleBatches.incrementAndGet();
            currentFrameIrisEligibleBatches.incrementAndGet();
            if (!batch.quadVertices().isEmpty()) {
                drawIrisTransientCompanionPart(batch, VertexFormat.Mode.QUADS, kind, sprite, textureLocation,
                        poseStack, packedLight, packedOverlay, red, green, blue, alpha, smoothing, renderMode,
                        uvTransform, shader, shadowPass, batch.quadVertices());
                drew = true;
            }
            if (!batch.triangleVertices().isEmpty()) {
                drawIrisTransientCompanionPart(batch, VertexFormat.Mode.TRIANGLES, kind, sprite, textureLocation,
                        poseStack, packedLight, packedOverlay, red, green, blue, alpha, smoothing, renderMode,
                        uvTransform, shader, shadowPass, batch.triangleVertices());
                drew = true;
            }
            return drew;
        }

        private void drawIrisTransientCompanionPart(PreparedBatch batch, VertexFormat.Mode sourceMode,
                GpuMeshKind kind, TextureAtlasSprite sprite, ResourceLocation textureLocation, PoseStack poseStack,
                int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean smoothing,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform, ShaderInstance shader,
                boolean shadowPass, List<PreparedVertex> vertices) {
            IrisCompanionMeshKey key = new IrisCompanionMeshKey(kind, "transient:" + batch.stableKey(), sprite,
                    vertices.size(), sourceMode, smoothing, uvTransform);
            irisUploadAttempts.incrementAndGet();
            IrisCompanionMesh mesh;
            try {
                mesh = uploadIrisCompanionMesh(key, sourceMode, packedLight, packedOverlay, red, green, blue, alpha,
                        smoothing, vertices);
            } catch (RuntimeException exception) {
                irisUploadFailures.incrementAndGet();
                throw exception;
            }
            try {
                drawIrisCompanionMesh(mesh, textureLocation, renderMode, poseStack, packedLight, packedOverlay,
                        red, green, blue, alpha, shader, shadowPass);
            } finally {
                mesh.close();
            }
        }

        private IrisCompanionMesh irisCompanionMeshFor(PreparedBatch batch, VertexFormat.Mode sourceMode,
                GpuMeshKind kind, TextureAtlasSprite sprite, int packedLight, int packedOverlay, int red, int green,
                int blue, int alpha, boolean smoothing, UvTransform uvTransform, List<PreparedVertex> vertices) {
            IrisCompanionMeshKey key = new IrisCompanionMeshKey(kind, batch.stableKey(), sprite, vertices.size(),
                    sourceMode, smoothing, uvTransform);
            return irisCompanionMeshFor(key, sourceMode, packedLight, packedOverlay, red, green, blue, alpha,
                    smoothing, vertices);
        }

        private IrisCompanionMesh irisCompanionMeshFor(IrisCompanionQueueKey queueKey,
                IrisCompanionQueuedInstance instance, List<PreparedVertex> vertices) {
            IrisCompanionMeshKey key = new IrisCompanionMeshKey(queueKey.kind(), queueKey.stablePartKey(), queueKey.sprite(),
                    queueKey.sourceVertices(), queueKey.sourceMode(), queueKey.smoothing(), queueKey.uvTransform());
            return irisCompanionMeshFor(key, queueKey.sourceMode(), instance.packedLight(), instance.packedOverlay(),
                    instance.red(), instance.green(), instance.blue(), instance.alpha(), queueKey.smoothing(),
                    vertices);
        }

        private IrisCompanionMesh irisCompanionMeshFor(IrisCompanionMeshKey key, VertexFormat.Mode sourceMode,
                int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean smoothing,
                List<PreparedVertex> vertices) {
            if (failedIrisKeys.contains(key)) {
                throw new IllegalStateException("Iris companion mesh upload previously failed for " + key);
            }
            synchronized (irisMeshes) {
                IrisCompanionMesh existing = irisMeshes.get(key);
                if (existing != null) {
                    return existing;
                }
                irisUploadAttempts.incrementAndGet();
                try {
                    IrisCompanionMesh created = uploadIrisCompanionMesh(key, sourceMode, packedLight, packedOverlay,
                            red, green, blue, alpha, smoothing, vertices);
                    irisMeshes.put(key, created);
                    irisMeshBytes.addAndGet(created.byteSize());
                    return created;
                } catch (RuntimeException exception) {
                    failedIrisKeys.add(key);
                    irisUploadFailures.incrementAndGet();
                    throw exception;
                }
            }
        }

        private IrisCompanionMesh uploadIrisCompanionMesh(IrisCompanionMeshKey key, VertexFormat.Mode sourceMode,
                int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean smoothing,
                List<PreparedVertex> vertices) {
            List<PreparedVertex> triangleVertices = sourceMode == VertexFormat.Mode.QUADS
                    ? expandedTriangles(vertices)
                    : vertices;
            if (triangleVertices.isEmpty()) {
                throw new IllegalStateException("No vertices for Iris companion mesh");
            }
            BufferBuilder builder = new BufferBuilder(Math.max(256, triangleVertices.size() * 56));
            builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.NEW_ENTITY);
            PoseStack identityStack = new PoseStack();
            PoseStack.Pose identity = identityStack.last();
            if (key.kind() == GpuMeshKind.UNTEXTURED) {
                emitPreparedVertices(triangleVertices, builder, identity.pose(), identity.normal(),
                        LightTexture.FULL_BRIGHT, 0, 255, 255, 255, 255, false, false, UvTransform.DEFAULT);
            } else if (key.sprite() == null) {
                emitPreparedVertices(triangleVertices, builder, identity.pose(), identity.normal(), packedLight,
                        packedOverlay, red, green, blue, alpha, false, smoothing, key.uvTransform());
            } else {
                emitPreparedVerticesWithSprite(triangleVertices, key.sprite(), builder, identity.pose(),
                        identity.normal(), packedLight, packedOverlay, red, green, blue, alpha, false, false,
                        key.uvTransform());
            }
            BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
            BufferBuilder.DrawState drawState = renderedBuffer.drawState();
            VertexFormat actualFormat = drawState.format();
            ByteBuffer vertexBytes = renderedBuffer.vertexBuffer();
            int vao = 0;
            int vbo = 0;
            try {
                vao = GL30.glGenVertexArrays();
                vbo = GL15.glGenBuffers();
                if (vao == 0 || vbo == 0) {
                    throw new IllegalStateException("Failed to allocate Iris companion GL buffers");
                }
                GL30.glBindVertexArray(vao);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBytes, GL15.GL_STATIC_DRAW);
                IrisCompanionMesh mesh = new IrisCompanionMesh(key, vao, vbo, drawState.vertexCount(),
                        Math.max(0L, vertexBytes.limit()), actualFormat,
                        buildIrisCompanionLightWeights(triangleVertices));
                mesh.bindStandardAttributes();
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL30.glBindVertexArray(0);
                return mesh;
            } catch (RuntimeException exception) {
                if (vbo != 0) {
                    GL15.glDeleteBuffers(vbo);
                }
                if (vao != 0) {
                    GL30.glDeleteVertexArrays(vao);
                }
                throw exception;
            } finally {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL30.glBindVertexArray(0);
            }
        }

        private static float[] buildIrisCompanionLightWeights(List<PreparedVertex> vertices) {
            if (vertices.isEmpty()) {
                return new float[0];
            }
            InstancedMeshBounds bounds = InstancedMeshBounds.of(vertices);
            float[] weights = new float[vertices.size() * 3];
            for (int i = 0; i < vertices.size(); i++) {
                Vector3f position = vertices.get(i).position();
                int offset = i * 3;
                weights[offset] = bounds.weightX(position.x());
                weights[offset + 1] = bounds.weightY(position.y());
                weights[offset + 2] = bounds.weightZ(position.z());
            }
            return weights;
        }

        private static List<PreparedVertex> expandedTriangles(List<PreparedVertex> vertices) {
            if (vertices.isEmpty()) {
                return List.of();
            }
            List<PreparedVertex> expanded = new ArrayList<>(vertices.size() / 4 * 6);
            for (int i = 0; i + 3 < vertices.size(); i += 4) {
                expanded.add(vertices.get(i));
                expanded.add(vertices.get(i + 1));
                expanded.add(vertices.get(i + 2));
                expanded.add(vertices.get(i + 2));
                expanded.add(vertices.get(i + 3));
                expanded.add(vertices.get(i));
            }
            return expanded;
        }

        private void drawIrisCompanionMesh(IrisCompanionMesh mesh, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode, PoseStack poseStack, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, ShaderInstance shader, boolean shadowPass) {
            drawIrisCompanionMesh(mesh, textureLocation, renderMode, poseStack.last().pose(), packedLight,
                    packedOverlay, red, green, blue, alpha, shader, shadowPass, -1);
        }

        private void drawIrisCompanionMesh(IrisCompanionMesh mesh, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode, Matrix4f modelView, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, ShaderInstance shader, boolean shadowPass) {
            drawIrisCompanionMesh(mesh, textureLocation, renderMode, modelView, packedLight, packedOverlay,
                    red, green, blue, alpha, shader, shadowPass, -1);
        }

        private void drawIrisCompanionMesh(IrisCompanionMesh mesh, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode, Matrix4f modelView, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, ShaderInstance shader, boolean shadowPass,
                int preparedLightmapSlot) {
            RenderType renderType = renderMode.renderType(textureLocation, VertexFormat.Mode.TRIANGLES);
            int previousVao = HbmGlVaoSafety.currentBinding();
            int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
            IrisRenderBatchKey batchKey = new IrisRenderBatchKey(textureLocation, renderMode, shadowPass);
            boolean success = false;
            try {
                if (!shadowPass) {
                    HbmRenderFrameLight.ensureLightTextureUpdated();
                }
                if (!HbmIrisRenderBatch.begin(batchKey, renderType, shader)) {
                    throw new IllegalStateException("Iris/Oculus ExtendedShader batch begin failed");
                }
                if (shader.MODEL_VIEW_MATRIX != null) {
                    shader.MODEL_VIEW_MATRIX.set(modelView);
                }
                mesh.bind();
                mesh.prepareForShader(shader);
                mesh.applyDrawAttributes(packedLight, packedOverlay, red, green, blue, alpha, !shadowPass,
                        preparedLightmapSlot);
                HbmIrisRenderBatch.uploadDrawMatrices(modelView);
                GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, mesh.vertexCount());
                HbmIrisRenderBatch.recordDraw(shadowPass);
                irisDrawCalls.incrementAndGet();
                currentFrameIrisDrawCalls.incrementAndGet();
                if (shadowPass) {
                    irisShadowDrawCalls.incrementAndGet();
                    currentFrameIrisShadowDrawCalls.incrementAndGet();
                }
                gpuDrawCalls.incrementAndGet();
                currentFrameGpuDrawCalls.incrementAndGet();
                success = true;
            } finally {
                mesh.restoreConstantLightmap();
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
                HbmGlVaoSafety.bindVertexArray(previousVao);
                if (!success) {
                    HbmIrisRenderBatch.endActiveBatch();
                }
            }
        }

        private void flushInstancedBatches() {
            if (pendingInstancedBatches.isEmpty()) {
                return;
            }
            List<InstancedBatch> batches = new ArrayList<>(pendingInstancedBatches.values());
            pendingInstancedBatches.clear();
            instancedFlushes.incrementAndGet();
            sortInstancedTailBatches(batches);
            if (drawMdiBatchesIfAvailable(batches)) {
                return;
            }
            for (InstancedBatch batch : batches) {
                if (batch.instances().isEmpty()) {
                    continue;
                }
                try {
                    if (!drawInstancedBatch(batch)) {
                        drawInstancedCpuFallback(batch, RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE);
                    }
                } catch (RuntimeException exception) {
                    recordInstancedFallback(RenderBackendFallbackReason.INSTANCING_DRAW_FAILED,
                            batch.instances().size(), batch.renderMode());
                    drawInstancedCpuFallback(batch, RenderBackendFallbackReason.INSTANCING_DRAW_FAILED);
                    HbmNtm.LOGGER.debug("Failed to draw legacy OBJ instanced batch {}", batch.key(), exception);
                }
            }
        }

        private void flushIrisCompanionBatches() {
            if (pendingIrisCompanionBatches.isEmpty()) {
                return;
            }
            List<IrisCompanionQueuedBatch> batches = new ArrayList<>(pendingIrisCompanionBatches.values());
            pendingIrisCompanionBatches.clear();
            irisQueuedFlushes.incrementAndGet();
            currentFrameIrisQueuedFlushes.incrementAndGet();
            ShaderInstance shader = HbmIrisExtendedShaderAccess.getBlockEntityShader(false);
            if (shader == null) {
                drawIrisQueuedCpuFallbacks(batches, RenderBackendFallbackReason.IRIS_SHADER_UNAVAILABLE);
                return;
            }
            List<IrisCompanionQueuedDraw> normalAlphaDraws = new ArrayList<>();
            for (IrisCompanionQueuedBatch batch : batches) {
                if (batch.instances().isEmpty()) {
                    continue;
                }
                if (isIrisCompanionNormalAlphaMode(batch.key().renderMode())) {
                    recordIrisEligibleBatch();
                    collectIrisQueuedDraws(batch, normalAlphaDraws);
                    continue;
                }
                try {
                    drawIrisQueuedBatch(batch, shader);
                } catch (RuntimeException exception) {
                    drawIrisQueuedCpuFallback(batch, RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED);
                    HbmNtm.LOGGER.debug("Failed to draw legacy OBJ queued Iris companion batch {}",
                            batch.key(), exception);
                }
            }
            drawIrisNormalAlphaQueuedDraws(normalAlphaDraws, shader);
        }

        private void drawIrisQueuedBatch(IrisCompanionQueuedBatch batch, ShaderInstance shader) {
            recordIrisEligibleBatch();
            IrisCompanionMesh mesh = irisCompanionMeshFor(batch.key(), batch.instances().get(0),
                    batch.sourceVertices());
            int[] lightmapSlots = prepareIrisQueuedLightmapSlots(mesh, batch.instances());
            for (int i = 0; i < batch.instances().size(); i++) {
                drawIrisQueuedInstance(batch, batch.instances().get(i), shader, mesh, lightmapSlots[i]);
            }
        }

        private int[] prepareIrisQueuedLightmapSlots(IrisCompanionMesh mesh,
                List<IrisCompanionQueuedInstance> instances) {
            int[] slots = new int[instances.size()];
            Arrays.fill(slots, -1);
            for (int i = 0; i < instances.size(); i++) {
                slots[i] = mesh.preparePerVertexLightmapSlot(instances.get(i).packedLight());
            }
            return slots;
        }

        private void recordIrisEligibleBatch() {
            irisEligibleBatches.incrementAndGet();
            currentFrameIrisEligibleBatches.incrementAndGet();
        }

        private void collectIrisQueuedDraws(IrisCompanionQueuedBatch batch, List<IrisCompanionQueuedDraw> draws) {
            for (IrisCompanionQueuedInstance instance : batch.instances()) {
                draws.add(new IrisCompanionQueuedDraw(batch, instance));
            }
        }

        private void drawIrisNormalAlphaQueuedDraws(List<IrisCompanionQueuedDraw> draws, ShaderInstance shader) {
            if (draws.isEmpty()) {
                return;
            }
            draws.sort((left, right) -> Float.compare(right.instance().sortDepthSq(), left.instance().sortDepthSq()));
            for (IrisCompanionQueuedDraw draw : draws) {
                try {
                    drawIrisQueuedInstance(draw.batch(), draw.instance(), shader);
                } catch (RuntimeException exception) {
                    drawIrisQueuedCpuFallback(draw.batch(), draw.instance(),
                            RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED);
                    HbmNtm.LOGGER.debug("Failed to draw legacy OBJ queued Iris normal-alpha companion batch {}",
                            draw.batch().key(), exception);
                }
            }
        }

        private void drawIrisQueuedInstance(IrisCompanionQueuedBatch batch, IrisCompanionQueuedInstance instance,
                ShaderInstance shader) {
            IrisCompanionMesh mesh = irisCompanionMeshFor(batch.key(), instance, batch.sourceVertices());
            drawIrisQueuedInstance(batch, instance, shader, mesh, -1);
        }

        private void drawIrisQueuedInstance(IrisCompanionQueuedBatch batch, IrisCompanionQueuedInstance instance,
                ShaderInstance shader, IrisCompanionMesh mesh, int preparedLightmapSlot) {
            drawIrisCompanionMesh(mesh, batch.key().textureLocation(), batch.key().renderMode(),
                    instance.position(), instance.packedLight(), instance.packedOverlay(), instance.red(),
                    instance.green(), instance.blue(), instance.alpha(), shader, false, preparedLightmapSlot);
            irisQueuedDrawCalls.incrementAndGet();
            currentFrameIrisQueuedDrawCalls.incrementAndGet();
        }

        private void drawIrisQueuedCpuFallbacks(List<IrisCompanionQueuedBatch> batches,
                RenderBackendFallbackReason reason) {
            List<IrisCompanionQueuedDraw> normalAlphaDraws = new ArrayList<>();
            for (IrisCompanionQueuedBatch batch : batches) {
                if (batch.instances().isEmpty()) {
                    continue;
                }
                if (isIrisCompanionNormalAlphaMode(batch.key().renderMode())) {
                    collectIrisQueuedDraws(batch, normalAlphaDraws);
                    continue;
                }
                drawIrisQueuedCpuFallback(batch, reason);
            }
            normalAlphaDraws.sort((left, right) -> Float.compare(right.instance().sortDepthSq(),
                    left.instance().sortDepthSq()));
            for (IrisCompanionQueuedDraw draw : normalAlphaDraws) {
                drawIrisQueuedCpuFallback(draw.batch(), draw.instance(), reason);
            }
        }

        private void drawIrisQueuedCpuFallback(IrisCompanionQueuedBatch batch, RenderBackendFallbackReason reason) {
            drawIrisQueuedCpuFallback(batch, batch.instances(), reason);
        }

        private void drawIrisQueuedCpuFallback(IrisCompanionQueuedBatch batch, IrisCompanionQueuedInstance instance,
                RenderBackendFallbackReason reason) {
            drawIrisQueuedCpuFallback(batch, List.of(instance), reason);
        }

        private void drawIrisQueuedCpuFallback(IrisCompanionQueuedBatch batch,
                List<IrisCompanionQueuedInstance> instances, RenderBackendFallbackReason reason) {
            if (instances.isEmpty()) {
                return;
            }
            recordIrisQueuedFallback(instances.size());
            int fallbackVertices = batch.sourceVertices().size() * instances.size();
            recordIrisFallback(reason, fallbackVertices);
            recordGpuFallback(reason, fallbackVertices);
            VertexConsumer consumer = null;
            MultiBufferSource activeBuffer = null;
            boolean untextured = batch.key().kind() == GpuMeshKind.UNTEXTURED;
            RenderType renderType = untextured
                    ? LegacyUntexturedQuadRenderer.type(batch.key().renderMode(), 255, batch.key().sourceMode())
                    : batch.key().renderMode().renderType(batch.key().textureLocation(), batch.key().sourceMode());
            TextureAtlasSprite sprite = batch.key().sprite();
            for (IrisCompanionQueuedInstance instance : instances) {
                if (consumer == null || instance.buffer() != activeBuffer) {
                    activeBuffer = instance.buffer();
                    consumer = instance.buffer().getBuffer(renderType);
                }
                if (untextured) {
                    emitPreparedVerticesUntextured(batch.sourceVertices(), consumer, instance.position(),
                            instance.red(), instance.green(), instance.blue(), instance.alpha());
                } else if (sprite == null) {
                    emitPreparedVertices(batch.sourceVertices(), consumer, instance.position(), instance.normal(),
                            instance.packedLight(), instance.packedOverlay(), instance.red(), instance.green(),
                            instance.blue(), instance.alpha(), false, batch.key().smoothing(),
                            batch.key().uvTransform());
                } else {
                    emitPreparedVerticesWithSprite(batch.sourceVertices(), sprite, consumer, instance.position(),
                            instance.normal(), instance.packedLight(), instance.packedOverlay(), instance.red(),
                            instance.green(), instance.blue(), instance.alpha(), false, false,
                            batch.key().uvTransform());
                }
            }
        }

        private boolean drawMdiBatchesIfAvailable(List<InstancedBatch> batches) {
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            if (!flags.mdiEnabled() || batches.isEmpty()) {
                return false;
            }
            List<InstancedBatch> eligible = new ArrayList<>();
            for (InstancedBatch batch : batches) {
                if (isInstancedNormalAlphaMode(batch.renderMode())
                        || isInstancedGlintMode(batch.renderMode())) {
                    return false;
                }
                if (!batch.instances().isEmpty()) {
                    eligible.add(batch);
                }
            }
            int eligibleBatches = eligible.size();
            if (eligibleBatches <= 0) {
                return false;
            }
            mdiEligibleFlushes.incrementAndGet();
            mdiEligibleBatches.addAndGet(eligibleBatches);
            currentFrameMdiEligibleFlushes.incrementAndGet();
            currentFrameMdiEligibleBatches.addAndGet(eligibleBatches);
            recordMdiAdditiveEligible(eligible);
            ensureMdiCapabilities();
            if (!mdiAvailable()) {
                recordMdiFallback(eligibleBatches);
                recordMdiAdditiveFallback(eligible);
                return false;
            }
            try {
                if (!drawMdiBatches(eligible)) {
                    recordMdiFallback(eligibleBatches);
                    recordMdiAdditiveFallback(eligible);
                    return false;
                }
                return true;
            } catch (RuntimeException exception) {
                recordMdiFallback(eligibleBatches);
                recordMdiAdditiveFallback(eligible);
                HbmNtm.LOGGER.debug("Failed to draw legacy OBJ MDI flush; falling back to instanced draws", exception);
                return false;
            }
        }

        private boolean drawMdiBatches(List<InstancedBatch> batches) {
            if (!allMdiShadersReady(batches)) {
                recordInstancedFallback(RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE,
                        totalInstances(batches));
                recordInstancedAdditiveFallback(totalAdditiveInstances(batches));
                return false;
            }
            Map<MdiDrawGroupKey, List<InstancedBatch>> groups = new LinkedHashMap<>();
            for (InstancedBatch batch : batches) {
                MdiDrawGroupKey key = new MdiDrawGroupKey(batch.mesh().key().kind(), batch.textureLocation(),
                        batch.renderMode());
                groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(batch);
            }
            List<MdiPreparedGroup> preparedGroups = new ArrayList<>();
            for (Map.Entry<MdiDrawGroupKey, List<InstancedBatch>> entry : groups.entrySet()) {
                MdiPreparedGroup prepared = mdiAtlas.prepare(entry.getKey(), entry.getValue());
                if (prepared == null || prepared.commandCount() <= 0) {
                    recordMdiNoSlot(entry.getValue());
                    return false;
                }
                preparedGroups.add(prepared);
            }
            boolean drew = false;
            for (MdiPreparedGroup prepared : preparedGroups) {
                drawMdiPreparedGroup(prepared);
                drew = true;
            }
            return drew;
        }

        private boolean allMdiShadersReady(List<InstancedBatch> batches) {
            boolean needsTextured = false;
            boolean needsUntextured = false;
            for (InstancedBatch batch : batches) {
                if (batch.mesh().key().kind() == GpuMeshKind.UNTEXTURED) {
                    needsUntextured = true;
                } else {
                    needsTextured = true;
                }
            }
            return (!needsTextured || HbmOptimizedRenderShaders.blockLitInstancedShader() != null)
                    && (!needsUntextured || HbmOptimizedRenderShaders.blockUntexturedInstancedShader() != null);
        }

        private void drawMdiPreparedGroup(MdiPreparedGroup prepared) {
            boolean untextured = prepared.key().kind() == GpuMeshKind.UNTEXTURED;
            RenderType renderType = untextured
                    ? LegacyUntexturedQuadRenderer.type(prepared.key().renderMode(), 255,
                            VertexFormat.Mode.TRIANGLES)
                    : prepared.key().renderMode().renderType(prepared.key().textureLocation(),
                            VertexFormat.Mode.TRIANGLES);
            renderType.setupRenderState();
            boolean multiDraw = false;
            int drawCalls = prepared.commandCount();
            ShaderInstance shader = null;
            try {
                if (untextured) {
                    RenderSystem.setShader(HbmOptimizedRenderShaders::blockUntexturedInstancedShader);
                } else {
                    RenderSystem.setShader(HbmOptimizedRenderShaders::blockLitInstancedShader);
                }
                shader = RenderSystem.getShader();
                if (shader == null) {
                    throw new IllegalStateException("No legacy OBJ instanced shader bound for MDI");
                }
                setupOptimizedInstancedShader(shader, !untextured);
                shader.apply();
                GL30.glBindVertexArray(mdiAtlas.vaoId());
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, mdiAtlas.instanceVboId());
                mdiAtlas.ensureInstanceCapacity(prepared.instanceBytes().limit());
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, prepared.instanceBytes());
                GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, mdiAtlas.indirectBufferId());
                mdiAtlas.ensureIndirectCapacity(prepared.commandBytes().limit());
                GL15.glBufferSubData(GL40.GL_DRAW_INDIRECT_BUFFER, 0L, prepared.commandBytes());
                if (mdiMultiDrawIndirectSupported) {
                    if (GL.getCapabilities().glMultiDrawArraysIndirect != 0L) {
                        GL43.glMultiDrawArraysIndirect(GL11.GL_TRIANGLES, 0L, prepared.commandCount(),
                                MdiPreparedGroup.COMMAND_STRIDE_BYTES);
                    } else {
                        ARBMultiDrawIndirect.glMultiDrawArraysIndirect(GL11.GL_TRIANGLES, 0L,
                                prepared.commandCount(), MdiPreparedGroup.COMMAND_STRIDE_BYTES);
                    }
                    multiDraw = true;
                    drawCalls = 1;
                } else if (mdiDrawIndirectSupported) {
                    for (int i = 0; i < prepared.commandCount(); i++) {
                        long commandOffset = (long) i * MdiPreparedGroup.COMMAND_STRIDE_BYTES;
                        if (GL.getCapabilities().glDrawArraysIndirect != 0L) {
                            GL40.glDrawArraysIndirect(GL11.GL_TRIANGLES, commandOffset);
                        } else {
                            ARBDrawIndirect.glDrawArraysIndirect(GL11.GL_TRIANGLES, commandOffset);
                        }
                    }
                } else {
                    throw new IllegalStateException("Draw arrays indirect unavailable during MDI dispatch");
                }
                recordMdiDraw(drawCalls, prepared.commandCount(), multiDraw, prepared.key().renderMode());
            } finally {
                if (shader != null) {
                    shader.clear();
                }
                renderType.clearRenderState();
                GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, 0);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL30.glBindVertexArray(0);
            }
        }

        private static int totalInstances(List<InstancedBatch> batches) {
            int total = 0;
            for (InstancedBatch batch : batches) {
                total += batch.instances().size();
            }
            return total;
        }

        private static int totalAdditiveInstances(List<InstancedBatch> batches) {
            int total = 0;
            for (InstancedBatch batch : batches) {
                if (isInstancedAdditiveMode(batch.renderMode())) {
                    total += batch.instances().size();
                }
            }
            return total;
        }

        private static int countAdditiveBatches(List<InstancedBatch> batches) {
            int total = 0;
            for (InstancedBatch batch : batches) {
                if (isInstancedAdditiveMode(batch.renderMode()) && !batch.instances().isEmpty()) {
                    total++;
                }
            }
            return total;
        }

        private static void sortInstancedTailBatches(List<InstancedBatch> batches) {
            batches.sort((left, right) -> {
                int leftPriority = instancedTailPriority(left.renderMode());
                int rightPriority = instancedTailPriority(right.renderMode());
                if (leftPriority != rightPriority) {
                    return Integer.compare(leftPriority, rightPriority);
                }
                if (isInstancedNormalAlphaMode(left.renderMode())) {
                    return Float.compare(right.maxSortDepthSq(), left.maxSortDepthSq());
                }
                return 0;
            });
            for (InstancedBatch batch : batches) {
                if (isInstancedNormalAlphaMode(batch.renderMode())) {
                    batch.sortBackToFront();
                }
            }
        }

        private void recordMdiFallback(int eligibleBatches) {
            mdiFallbackFlushes.incrementAndGet();
            mdiFallbackBatches.addAndGet(eligibleBatches);
            currentFrameMdiFallbackFlushes.incrementAndGet();
            currentFrameMdiFallbackBatches.addAndGet(eligibleBatches);
        }

        private void recordMdiDraw(int drawCalls, int commandCount, boolean multiDraw,
                LegacyTexturedRenderMode renderMode) {
            int safeDrawCalls = Math.max(1, drawCalls);
            mdiDrawCalls.addAndGet(safeDrawCalls);
            mdiIndirectCommands.addAndGet(commandCount);
            currentFrameMdiDrawCalls.addAndGet(safeDrawCalls);
            currentFrameMdiIndirectCommands.addAndGet(commandCount);
            instancedDrawCalls.addAndGet(safeDrawCalls);
            currentFrameInstancedDrawCalls.addAndGet(safeDrawCalls);
            gpuDrawCalls.addAndGet(safeDrawCalls);
            currentFrameGpuDrawCalls.addAndGet(safeDrawCalls);
            if (isInstancedAdditiveMode(renderMode)) {
                mdiAdditiveDrawCalls.addAndGet(safeDrawCalls);
                mdiAdditiveIndirectCommands.addAndGet(commandCount);
                currentFrameMdiAdditiveDrawCalls.addAndGet(safeDrawCalls);
                currentFrameMdiAdditiveIndirectCommands.addAndGet(commandCount);
                instancedAdditiveDrawCalls.addAndGet(safeDrawCalls);
                currentFrameInstancedAdditiveDrawCalls.addAndGet(safeDrawCalls);
            }
            if (multiDraw) {
                mdiMultiDrawCalls.incrementAndGet();
            }
        }

        private void recordMdiAdditiveEligible(List<InstancedBatch> batches) {
            int additiveBatches = countAdditiveBatches(batches);
            if (additiveBatches <= 0) {
                return;
            }
            mdiAdditiveEligibleBatches.addAndGet(additiveBatches);
            currentFrameMdiAdditiveEligibleBatches.addAndGet(additiveBatches);
        }

        private void recordMdiAdditiveFallback(List<InstancedBatch> batches) {
            int additiveBatches = countAdditiveBatches(batches);
            if (additiveBatches <= 0) {
                return;
            }
            mdiAdditiveFallbackBatches.addAndGet(additiveBatches);
            currentFrameMdiAdditiveFallbackBatches.addAndGet(additiveBatches);
        }

        private void recordMdiNoSlot(List<InstancedBatch> batches) {
            int batchCount = 0;
            int instanceCount = 0;
            int additiveBatchCount = 0;
            int additiveInstanceCount = 0;
            for (InstancedBatch batch : batches) {
                if (!batch.instances().isEmpty()) {
                    batchCount++;
                    instanceCount += batch.instances().size();
                    if (isInstancedAdditiveMode(batch.renderMode())) {
                        additiveBatchCount++;
                        additiveInstanceCount += batch.instances().size();
                    }
                }
            }
            if (batchCount <= 0) {
                return;
            }
            mdiNoSlotBatches.addAndGet(batchCount);
            mdiNoSlotInstances.addAndGet(instanceCount);
            currentFrameMdiNoSlotBatches.addAndGet(batchCount);
            currentFrameMdiNoSlotInstances.addAndGet(instanceCount);
            if (additiveBatchCount > 0) {
                mdiAdditiveNoSlotBatches.addAndGet(additiveBatchCount);
                mdiAdditiveNoSlotInstances.addAndGet(additiveInstanceCount);
                currentFrameMdiAdditiveNoSlotBatches.addAndGet(additiveBatchCount);
                currentFrameMdiAdditiveNoSlotInstances.addAndGet(additiveInstanceCount);
            }
        }

        private boolean mdiAvailable() {
            ensureMdiCapabilities();
            return mdiDrawIndirectSupported && mdiBaseInstanceSupported;
        }

        private void ensureMdiCapabilities() {
            if (mdiCapsResolved) {
                return;
            }
            synchronized (this) {
                if (mdiCapsResolved) {
                    return;
                }
                try {
                    GLCapabilities capabilities = GL.getCapabilities();
                    if (capabilities != null) {
                        mdiDrawIndirectSupported = capabilities.glDrawArraysIndirect != 0L
                                || capabilities.GL_ARB_draw_indirect;
                        mdiMultiDrawIndirectSupported = capabilities.glMultiDrawArraysIndirect != 0L
                                || capabilities.GL_ARB_multi_draw_indirect;
                        mdiBaseInstanceSupported = capabilities.glDrawArraysInstancedBaseInstance != 0L;
                    }
                } catch (RuntimeException exception) {
                    mdiDrawIndirectSupported = false;
                    mdiMultiDrawIndirectSupported = false;
                    mdiBaseInstanceSupported = false;
                }
                mdiCapsResolved = true;
            }
        }

        private boolean drawInstancedBatch(InstancedBatch batch) {
            boolean untextured = batch.mesh().key().kind() == GpuMeshKind.UNTEXTURED;
            ShaderInstance shader = untextured
                    ? HbmOptimizedRenderShaders.blockUntexturedInstancedShader()
                    : HbmOptimizedRenderShaders.blockLitInstancedShader();
            if (shader == null) {
                recordInstancedFallback(RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE,
                        batch.instances().size(), batch.renderMode());
                return false;
            }
            InstancedMesh mesh = batch.mesh();
            int instanceCount = batch.instances().size();
            if (instanceCount > MAX_INSTANCED_INSTANCES_PER_DRAW) {
                recordInstancedOverflow(instanceCount, batch.renderMode());
            }
            RenderType renderType = untextured
                    ? LegacyUntexturedQuadRenderer.type(batch.renderMode(), 255, VertexFormat.Mode.TRIANGLES)
                    : batch.renderMode().renderType(batch.textureLocation(), VertexFormat.Mode.TRIANGLES);
            renderType.setupRenderState();
            ShaderInstance boundShader = null;
            try {
                if (untextured) {
                    RenderSystem.setShader(HbmOptimizedRenderShaders::blockUntexturedInstancedShader);
                } else {
                    RenderSystem.setShader(HbmOptimizedRenderShaders::blockLitInstancedShader);
                }
                boundShader = RenderSystem.getShader();
                if (boundShader == null) {
                    throw new IllegalStateException("No legacy OBJ instanced shader bound");
                }
                setupOptimizedInstancedShader(boundShader, !untextured);
                boundShader.apply();
                if (!untextured) {
                    HbmRenderFrameLight.bindBlockLitSamplerTextures(boundShader);
                }
                GL30.glBindVertexArray(mesh.vaoId());
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.instanceVboId());
                for (int start = 0; start < instanceCount; start += MAX_INSTANCED_INSTANCES_PER_DRAW) {
                    int end = Math.min(start + MAX_INSTANCED_INSTANCES_PER_DRAW, instanceCount);
                    ByteBuffer instanceBytes = instancedSliceBytes(batch, start, end);
                    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, instanceBytes, GL15.GL_STREAM_DRAW);
                    GL31.glDrawArraysInstanced(GL11.GL_TRIANGLES, 0, mesh.vertexCount(), end - start);
                    recordInstancedDrawCall(batch.renderMode());
                }
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL30.glBindVertexArray(0);
            } finally {
                if (boundShader != null) {
                    boundShader.clear();
                }
                renderType.clearRenderState();
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL30.glBindVertexArray(0);
            }
            return true;
        }

        private static void setupOptimizedInstancedShader(ShaderInstance shader, boolean textured) {
            if (textured) {
                HbmRenderFrameLight.prepareBlockLitSamplers(shader);
            }
            if (shader.PROJECTION_MATRIX != null) {
                shader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
            }
            if (shader.MODEL_VIEW_MATRIX != null) {
                shader.MODEL_VIEW_MATRIX.set(OPTIMIZED_SHADER_IDENTITY);
            }
            Uniform fogStart = shader.getUniform("FogStart");
            if (fogStart != null) {
                fogStart.set(RenderSystem.getShaderFogStart());
            }
            Uniform fogEnd = shader.getUniform("FogEnd");
            if (fogEnd != null) {
                fogEnd.set(RenderSystem.getShaderFogEnd());
            }
            Uniform fogColor = shader.getUniform("FogColor");
            if (fogColor != null) {
                float[] color = RenderSystem.getShaderFogColor();
                fogColor.set(color[0], color[1], color[2], color[3]);
            }
            Uniform fadeAlpha = shader.getUniform("FadeAlpha");
            if (fadeAlpha != null) {
                fadeAlpha.set(1.0F);
            }
        }

        private static ByteBuffer instancedSliceBytes(InstancedBatch batch, int start, int end) {
            ByteBuffer instanceBytes = ByteBuffer.allocateDirect((end - start) * InstancedInstance.FLOATS * 4)
                    .order(ByteOrder.nativeOrder());
            for (int i = start; i < end; i++) {
                batch.instances().get(i).write(instanceBytes);
            }
            instanceBytes.flip();
            return instanceBytes;
        }

        private void recordInstancedDrawCall(LegacyTexturedRenderMode renderMode) {
            instancedDrawCalls.incrementAndGet();
            currentFrameInstancedDrawCalls.incrementAndGet();
            gpuDrawCalls.incrementAndGet();
            currentFrameGpuDrawCalls.incrementAndGet();
            if (isInstancedAdditiveMode(renderMode)) {
                instancedAdditiveDrawCalls.incrementAndGet();
                currentFrameInstancedAdditiveDrawCalls.incrementAndGet();
            }
        }

        private void recordInstancedOverflow(int instances, LegacyTexturedRenderMode renderMode) {
            int overflow = Math.max(0, instances - MAX_INSTANCED_INSTANCES_PER_DRAW);
            if (overflow <= 0) {
                return;
            }
            instancedOverflowBatches.incrementAndGet();
            instancedOverflowInstances.addAndGet(overflow);
            currentFrameInstancedOverflowBatches.incrementAndGet();
            currentFrameInstancedOverflowInstances.addAndGet(overflow);
            if (isInstancedAdditiveMode(renderMode)) {
                instancedAdditiveOverflowBatches.incrementAndGet();
                instancedAdditiveOverflowInstances.addAndGet(overflow);
                currentFrameInstancedAdditiveOverflowBatches.incrementAndGet();
                currentFrameInstancedAdditiveOverflowInstances.addAndGet(overflow);
            }
        }

        private void drawInstancedCpuFallback(InstancedBatch batch, RenderBackendFallbackReason reason) {
            InstancedMesh mesh = batch.mesh();
            VertexConsumer consumer = null;
            MultiBufferSource activeBuffer = null;
            boolean untextured = mesh.key().kind() == GpuMeshKind.UNTEXTURED;
            RenderType renderType = untextured
                    ? LegacyUntexturedQuadRenderer.type(batch.renderMode(), 255, mesh.sourceMode())
                    : batch.renderMode().renderType(batch.textureLocation(), mesh.sourceMode());
            TextureAtlasSprite sprite = mesh.key().sprite();
            for (InstancedFallbackInstance fallback : batch.fallbacks()) {
                if (consumer == null || fallback.buffer() != activeBuffer) {
                    activeBuffer = fallback.buffer();
                    consumer = fallback.buffer().getBuffer(renderType);
                }
                if (untextured) {
                    emitPreparedVerticesUntextured(mesh.sourceVertices(), consumer, fallback.position(),
                            fallback.red(), fallback.green(), fallback.blue(), fallback.alpha());
                } else if (sprite == null) {
                    emitPreparedVertices(mesh.sourceVertices(), consumer, fallback.position(), fallback.normal(),
                            fallback.packedLight(), fallback.packedOverlay(), fallback.red(), fallback.green(),
                            fallback.blue(), fallback.alpha(), false, mesh.key().smoothing(), fallback.uvTransform());
                } else {
                    emitPreparedVerticesWithSprite(mesh.sourceVertices(), sprite, consumer, fallback.position(),
                            fallback.normal(), fallback.packedLight(), fallback.packedOverlay(), fallback.red(),
                            fallback.green(), fallback.blue(), fallback.alpha(), false, false,
                            fallback.uvTransform());
                }
            }
            recordGpuFallback(reason, mesh.sourceVertices().size() * batch.fallbacks().size());
        }

        private void recordGpuFallback(RenderBackendFallbackReason reason, int vertices) {
            if (vertices <= 0) {
                return;
            }
            lastGpuFallbackReason = reason == null ? RenderBackendFallbackReason.NONE : reason;
            gpuFallbackBatches.incrementAndGet();
            gpuFallbackVertices.addAndGet(vertices);
            currentFrameGpuFallbackBatches.incrementAndGet();
            currentFrameGpuFallbackVertices.addAndGet(vertices);
        }

        private void recordIrisFallback(RenderBackendFallbackReason reason, int vertices) {
            if (vertices <= 0) {
                return;
            }
            lastGpuFallbackReason = reason == null ? RenderBackendFallbackReason.NONE : reason;
            irisFallbackBatches.incrementAndGet();
            irisFallbackVertices.addAndGet(vertices);
            currentFrameIrisFallbackBatches.incrementAndGet();
            currentFrameIrisFallbackVertices.addAndGet(vertices);
        }

        private void recordIrisQueuedFallback(int instances) {
            if (instances <= 0) {
                return;
            }
            irisQueuedFallbackBatches.incrementAndGet();
            irisQueuedFallbackInstances.addAndGet(instances);
            currentFrameIrisQueuedFallbackBatches.incrementAndGet();
            currentFrameIrisQueuedFallbackInstances.addAndGet(instances);
        }

        private void recordInstancedFallback(RenderBackendFallbackReason reason, int instances) {
            recordInstancedFallback(reason, instances, false);
        }

        private void recordInstancedFallback(RenderBackendFallbackReason reason, int instances,
                LegacyTexturedRenderMode renderMode) {
            recordInstancedFallback(reason, instances, isInstancedAdditiveMode(renderMode));
        }

        private void recordInstancedFallback(RenderBackendFallbackReason reason, int instances, boolean additive) {
            if (instances <= 0) {
                return;
            }
            lastGpuFallbackReason = reason == null ? RenderBackendFallbackReason.NONE : reason;
            instancedFallbackBatches.incrementAndGet();
            instancedFallbackInstances.addAndGet(instances);
            currentFrameInstancedFallbackBatches.incrementAndGet();
            currentFrameInstancedFallbackInstances.addAndGet(instances);
            if (additive) {
                instancedAdditiveFallbackBatches.incrementAndGet();
                instancedAdditiveFallbackInstances.addAndGet(instances);
                currentFrameInstancedAdditiveFallbackBatches.incrementAndGet();
                currentFrameInstancedAdditiveFallbackInstances.addAndGet(instances);
            }
        }

        private void recordInstancedAdditiveFallback(int instances) {
            if (instances <= 0) {
                return;
            }
            instancedAdditiveFallbackBatches.incrementAndGet();
            instancedAdditiveFallbackInstances.addAndGet(instances);
            currentFrameInstancedAdditiveFallbackBatches.incrementAndGet();
            currentFrameInstancedAdditiveFallbackInstances.addAndGet(instances);
        }

        @Override
        public void clear(RenderBackendClearReason reason) {
            HbmIrisRenderBatch.invalidateCaches();
            List<GpuMesh> toClose;
            List<IrisCompanionMesh> irisToClose;
            List<InstancedMesh> instancedToClose;
            synchronized (meshes) {
                toClose = new ArrayList<>(meshes.values());
                meshes.clear();
                failedKeys.clear();
                gpuBufferBytes.set(0L);
            }
            synchronized (irisMeshes) {
                irisToClose = new ArrayList<>(irisMeshes.values());
                irisMeshes.clear();
                failedIrisKeys.clear();
                irisMeshBytes.set(0L);
            }
            synchronized (instancedMeshes) {
                instancedToClose = new ArrayList<>(instancedMeshes.values());
                instancedMeshes.clear();
                failedInstancedKeys.clear();
                pendingInstancedBatches.clear();
                pendingIrisCompanionBatches.clear();
            }
            closeLater(toClose);
            closeIrisLater(irisToClose);
            closeInstancedLater(instancedToClose);
            mdiAtlas.resetLater();
            cpuFallback.clear(reason);
        }

        @Override
        public void flush(RenderBackendFlushStage stage) {
            if (stage == RenderBackendFlushStage.AFTER_BLOCK_ENTITIES || stage == RenderBackendFlushStage.MANUAL) {
                HbmIrisRenderBatch.endActiveBatch();
                flushIrisCompanionBatches();
                HbmIrisRenderBatch.endActiveBatch();
                flushInstancedBatches();
            }
            cpuFallback.flush(stage);
        }

        @Override
        public void recordCpuFallback(RenderBackendFallbackReason fallback, int vertices) {
            cpuFallback.recordCpuFallback(fallback, vertices);
        }

        @Override
        public void endFrame() {
            lastFrameInstancedQueuedBatches.set(currentFrameInstancedQueuedBatches.getAndSet(0L));
            lastFrameInstancedQueuedInstances.set(currentFrameInstancedQueuedInstances.getAndSet(0L));
            lastFrameInstancedDrawCalls.set(currentFrameInstancedDrawCalls.getAndSet(0L));
            lastFrameInstancedFallbackBatches.set(currentFrameInstancedFallbackBatches.getAndSet(0L));
            lastFrameInstancedFallbackInstances.set(currentFrameInstancedFallbackInstances.getAndSet(0L));
            lastFrameInstancedOverflowBatches.set(currentFrameInstancedOverflowBatches.getAndSet(0L));
            lastFrameInstancedOverflowInstances.set(currentFrameInstancedOverflowInstances.getAndSet(0L));
            lastFrameInstancedAdditiveQueuedBatches.set(currentFrameInstancedAdditiveQueuedBatches.getAndSet(0L));
            lastFrameInstancedAdditiveQueuedInstances.set(currentFrameInstancedAdditiveQueuedInstances.getAndSet(0L));
            lastFrameInstancedAdditiveDrawCalls.set(currentFrameInstancedAdditiveDrawCalls.getAndSet(0L));
            lastFrameInstancedAdditiveFallbackBatches.set(currentFrameInstancedAdditiveFallbackBatches.getAndSet(0L));
            lastFrameInstancedAdditiveFallbackInstances.set(currentFrameInstancedAdditiveFallbackInstances.getAndSet(0L));
            lastFrameInstancedAdditiveOverflowBatches.set(currentFrameInstancedAdditiveOverflowBatches.getAndSet(0L));
            lastFrameInstancedAdditiveOverflowInstances.set(currentFrameInstancedAdditiveOverflowInstances.getAndSet(0L));
            lastFrameMdiEligibleFlushes.set(currentFrameMdiEligibleFlushes.getAndSet(0L));
            lastFrameMdiEligibleBatches.set(currentFrameMdiEligibleBatches.getAndSet(0L));
            lastFrameMdiFallbackFlushes.set(currentFrameMdiFallbackFlushes.getAndSet(0L));
            lastFrameMdiFallbackBatches.set(currentFrameMdiFallbackBatches.getAndSet(0L));
            lastFrameMdiDrawCalls.set(currentFrameMdiDrawCalls.getAndSet(0L));
            lastFrameMdiIndirectCommands.set(currentFrameMdiIndirectCommands.getAndSet(0L));
            lastFrameMdiNoSlotBatches.set(currentFrameMdiNoSlotBatches.getAndSet(0L));
            lastFrameMdiNoSlotInstances.set(currentFrameMdiNoSlotInstances.getAndSet(0L));
            lastFrameMdiAdditiveEligibleBatches.set(currentFrameMdiAdditiveEligibleBatches.getAndSet(0L));
            lastFrameMdiAdditiveFallbackBatches.set(currentFrameMdiAdditiveFallbackBatches.getAndSet(0L));
            lastFrameMdiAdditiveDrawCalls.set(currentFrameMdiAdditiveDrawCalls.getAndSet(0L));
            lastFrameMdiAdditiveIndirectCommands.set(currentFrameMdiAdditiveIndirectCommands.getAndSet(0L));
            lastFrameMdiAdditiveNoSlotBatches.set(currentFrameMdiAdditiveNoSlotBatches.getAndSet(0L));
            lastFrameMdiAdditiveNoSlotInstances.set(currentFrameMdiAdditiveNoSlotInstances.getAndSet(0L));
            lastFrameGpuDrawCalls.set(currentFrameGpuDrawCalls.getAndSet(0L));
            lastFrameGpuFallbackBatches.set(currentFrameGpuFallbackBatches.getAndSet(0L));
            lastFrameGpuFallbackVertices.set(currentFrameGpuFallbackVertices.getAndSet(0L));
            lastFrameIrisEligibleBatches.set(currentFrameIrisEligibleBatches.getAndSet(0L));
            lastFrameIrisDrawCalls.set(currentFrameIrisDrawCalls.getAndSet(0L));
            lastFrameIrisShadowDrawCalls.set(currentFrameIrisShadowDrawCalls.getAndSet(0L));
            lastFrameIrisFallbackBatches.set(currentFrameIrisFallbackBatches.getAndSet(0L));
            lastFrameIrisFallbackVertices.set(currentFrameIrisFallbackVertices.getAndSet(0L));
            lastFrameIrisQueuedBatches.set(currentFrameIrisQueuedBatches.getAndSet(0L));
            lastFrameIrisQueuedInstances.set(currentFrameIrisQueuedInstances.getAndSet(0L));
            lastFrameIrisQueuedFlushes.set(currentFrameIrisQueuedFlushes.getAndSet(0L));
            lastFrameIrisQueuedDrawCalls.set(currentFrameIrisQueuedDrawCalls.getAndSet(0L));
            lastFrameIrisQueuedFallbackBatches.set(currentFrameIrisQueuedFallbackBatches.getAndSet(0L));
            lastFrameIrisQueuedFallbackInstances.set(currentFrameIrisQueuedFallbackInstances.getAndSet(0L));
            HbmIrisRenderBatch.endFrame();
            cpuFallback.endFrame();
        }

        @Override
        public RenderBackendSnapshot snapshot() {
            RenderBackendSnapshot cpu = cpuFallback.snapshot();
            boolean experimentalEnabled = HbmRenderFrameFlags.current().experimentalGpuBackendEnabled();
            long totalDrawCalls = cpu.estimatedDrawCalls() + gpuDrawCalls.get();
            long currentDrawCalls = cpu.currentFrameEstimatedDrawCalls() + currentFrameGpuDrawCalls.get();
            long lastDrawCalls = cpu.lastFrameEstimatedDrawCalls() + lastFrameGpuDrawCalls.get();
            return new RenderBackendSnapshot(
                    experimentalEnabled ? "experimental_gpu_mesh_with_cpu_fallback" : cpu.name(),
                    experimentalEnabled,
                    cpu.reloadClearHook(),
                    experimentalEnabled ? CAPABILITIES : cpu.capabilities(),
                    cpu.modelCache(),
                    cpu.groupPreparedBuilds(),
                    cpu.groupPreparedVertices(),
                    cpu.allPreparedBatchBuilds(),
                    cpu.selectionCacheHits(),
                    cpu.selectionCacheMisses(),
                    cpu.selectionCacheClears(),
                    cpu.selectionCacheEmptyBuilds(),
                    cpu.selectionCachePreparedBatchBuilds(),
                    cpu.selectionHandleRefreshes(),
                    cpu.selectionHandleEmptyBuilds(),
                    cpu.selectionHandlePreparedBatchBuilds(),
                    cpu.reloadClears(),
                    cpu.backendClears(),
                    cpu.clientDisconnectClears(),
                    cpu.shaderReloadClears(),
                    cpu.manualClears(),
                    cpu.backendClearGeneration(),
                    cpu.lastClearReason(),
                    cpu.backendFlushes(),
                    cpu.afterBlockEntitiesFlushes(),
                    cpu.manualFlushes(),
                    cpu.backendFlushGeneration(),
                    cpu.lastFlushStage(),
                    cpu.texturedBatches(),
                    cpu.texturedVertices(),
                    cpu.spriteBatches(),
                    cpu.spriteVertices(),
                    cpu.untexturedBatches(),
                    cpu.untexturedVertices(),
                    totalDrawCalls,
                    cpu.texturedDrawCalls() + gpuDrawCalls.get(),
                    cpu.spriteDrawCalls(),
                    cpu.untexturedDrawCalls(),
                    cpu.textureSwitches(),
                    cpu.renderModeSwitches(),
                    cpu.cpuFallbackBatches() + gpuFallbackBatches.get(),
                    cpu.cpuFallbackVertices() + gpuFallbackVertices.get(),
                    cpu.texturedClippedFallbackBatches(),
                    cpu.texturedClippedFallbackVertices(),
                    cpu.untexturedClippedFallbackBatches(),
                    cpu.untexturedClippedFallbackVertices(),
                    lastGpuFallbackReason == RenderBackendFallbackReason.NONE ? cpu.lastFallbackReason() : lastGpuFallbackReason,
                    cpu.frameGeneration(),
                    cpu.currentFrameTexturedBatches(),
                    cpu.currentFrameTexturedVertices(),
                    cpu.currentFrameSpriteBatches(),
                    cpu.currentFrameSpriteVertices(),
                    cpu.currentFrameUntexturedBatches(),
                    cpu.currentFrameUntexturedVertices(),
                    cpu.currentFrameCpuFallbackBatches() + currentFrameGpuFallbackBatches.get(),
                    cpu.currentFrameCpuFallbackVertices() + currentFrameGpuFallbackVertices.get(),
                    cpu.currentFrameBackendFlushes(),
                    currentDrawCalls,
                    cpu.currentFrameTextureSwitches(),
                    gpuMeshCount(),
                    gpuBufferBytes.get() + irisMeshBytes.get() + mdiAtlas.byteCapacity(),
                    gpuUploadAttempts.get(),
                    gpuUploadFailures.get(),
                    gpuDrawCalls.get(),
                    gpuFallbackBatches.get(),
                    gpuFallbackVertices.get(),
                    instancedQueuedBatches.get(),
                    instancedQueuedInstances.get(),
                    instancedFlushes.get(),
                    instancedDrawCalls.get(),
                    instancedFallbackBatches.get(),
                    instancedFallbackInstances.get(),
                    instancedOverflowBatches.get(),
                    instancedOverflowInstances.get(),
                    experimentalEnabled && mdiAvailable(),
                    experimentalEnabled && mdiDrawIndirectSupported,
                    experimentalEnabled && mdiMultiDrawIndirectSupported,
                    experimentalEnabled && mdiBaseInstanceSupported,
                    mdiEligibleFlushes.get(),
                    mdiEligibleBatches.get(),
                    mdiFallbackFlushes.get(),
                    mdiFallbackBatches.get(),
                    mdiDrawCalls.get(),
                    mdiMultiDrawCalls.get(),
                    mdiIndirectCommands.get(),
                    mdiAtlas.partCount(),
                    mdiAtlas.byteCapacity(),
                    mdiNoSlotBatches.get(),
                    mdiNoSlotInstances.get(),
                    currentFrameMdiEligibleFlushes.get(),
                    currentFrameMdiEligibleBatches.get(),
                    currentFrameMdiFallbackFlushes.get(),
                    currentFrameMdiFallbackBatches.get(),
                    currentFrameMdiDrawCalls.get(),
                    currentFrameMdiIndirectCommands.get(),
                    currentFrameMdiNoSlotBatches.get(),
                    currentFrameMdiNoSlotInstances.get(),
                    lastFrameMdiEligibleFlushes.get(),
                    lastFrameMdiEligibleBatches.get(),
                    lastFrameMdiFallbackFlushes.get(),
                    lastFrameMdiFallbackBatches.get(),
                    lastFrameMdiDrawCalls.get(),
                    lastFrameMdiIndirectCommands.get(),
                    lastFrameMdiNoSlotBatches.get(),
                    lastFrameMdiNoSlotInstances.get(),
                    currentFrameInstancedQueuedBatches.get(),
                    currentFrameInstancedQueuedInstances.get(),
                    currentFrameInstancedDrawCalls.get(),
                    currentFrameInstancedOverflowBatches.get(),
                    currentFrameInstancedOverflowInstances.get(),
                    lastFrameInstancedQueuedBatches.get(),
                    lastFrameInstancedQueuedInstances.get(),
                    lastFrameInstancedDrawCalls.get(),
                    lastFrameInstancedOverflowBatches.get(),
                    lastFrameInstancedOverflowInstances.get(),
                    currentFrameGpuDrawCalls.get(),
                    currentFrameGpuFallbackBatches.get(),
                    currentFrameGpuFallbackVertices.get(),
                    lastFrameGpuDrawCalls.get(),
                    lastFrameGpuFallbackBatches.get(),
                    lastFrameGpuFallbackVertices.get(),
                    cpu.lastFrameTexturedBatches(),
                    cpu.lastFrameTexturedVertices(),
                    cpu.lastFrameSpriteBatches(),
                    cpu.lastFrameSpriteVertices(),
                    cpu.lastFrameUntexturedBatches(),
                    cpu.lastFrameUntexturedVertices(),
                    cpu.lastFrameCpuFallbackBatches() + lastFrameGpuFallbackBatches.get(),
                    cpu.lastFrameCpuFallbackVertices() + lastFrameGpuFallbackVertices.get(),
                    cpu.lastFrameBackendFlushes(),
                    lastDrawCalls,
                    cpu.lastFrameTextureSwitches());
        }

        @Override
        public RenderBackendAdditiveSnapshot additiveSnapshot() {
            return new RenderBackendAdditiveSnapshot(
                    new InstancedAdditiveSnapshot(
                            instancedAdditiveQueuedBatches.get(),
                            instancedAdditiveQueuedInstances.get(),
                            instancedAdditiveDrawCalls.get(),
                            instancedAdditiveFallbackBatches.get(),
                            instancedAdditiveFallbackInstances.get(),
                            instancedAdditiveOverflowBatches.get(),
                            instancedAdditiveOverflowInstances.get(),
                            currentFrameInstancedAdditiveQueuedBatches.get(),
                            currentFrameInstancedAdditiveQueuedInstances.get(),
                            currentFrameInstancedAdditiveDrawCalls.get(),
                            currentFrameInstancedAdditiveFallbackBatches.get(),
                            currentFrameInstancedAdditiveFallbackInstances.get(),
                            currentFrameInstancedAdditiveOverflowBatches.get(),
                            currentFrameInstancedAdditiveOverflowInstances.get(),
                            lastFrameInstancedAdditiveQueuedBatches.get(),
                            lastFrameInstancedAdditiveQueuedInstances.get(),
                            lastFrameInstancedAdditiveDrawCalls.get(),
                            lastFrameInstancedAdditiveFallbackBatches.get(),
                            lastFrameInstancedAdditiveFallbackInstances.get(),
                            lastFrameInstancedAdditiveOverflowBatches.get(),
                            lastFrameInstancedAdditiveOverflowInstances.get()),
                    new MdiAdditiveSnapshot(
                            mdiAdditiveEligibleBatches.get(),
                            mdiAdditiveFallbackBatches.get(),
                            mdiAdditiveDrawCalls.get(),
                            mdiAdditiveIndirectCommands.get(),
                            mdiAdditiveNoSlotBatches.get(),
                            mdiAdditiveNoSlotInstances.get(),
                            currentFrameMdiAdditiveEligibleBatches.get(),
                            currentFrameMdiAdditiveFallbackBatches.get(),
                            currentFrameMdiAdditiveDrawCalls.get(),
                            currentFrameMdiAdditiveIndirectCommands.get(),
                            currentFrameMdiAdditiveNoSlotBatches.get(),
                            currentFrameMdiAdditiveNoSlotInstances.get(),
                            lastFrameMdiAdditiveEligibleBatches.get(),
                            lastFrameMdiAdditiveFallbackBatches.get(),
                            lastFrameMdiAdditiveDrawCalls.get(),
                            lastFrameMdiAdditiveIndirectCommands.get(),
                            lastFrameMdiAdditiveNoSlotBatches.get(),
                            lastFrameMdiAdditiveNoSlotInstances.get()));
        }

        private int gpuMeshCount() {
            int instancedCount;
            int irisCount;
            synchronized (instancedMeshes) {
                instancedCount = instancedMeshes.size();
            }
            synchronized (irisMeshes) {
                irisCount = irisMeshes.size();
            }
            synchronized (meshes) {
                return meshes.size() + instancedCount + irisCount;
            }
        }

        @Override
        public RenderBackendIrisSnapshot irisSnapshot() {
            synchronized (irisMeshes) {
                return new RenderBackendIrisSnapshot(
                        irisMeshes.size(),
                        irisMeshBytes.get(),
                        irisEligibleBatches.get(),
                        irisDrawCalls.get(),
                        irisFallbackBatches.get(),
                        irisFallbackVertices.get(),
                        irisUploadAttempts.get(),
                        irisUploadFailures.get(),
                        currentFrameIrisEligibleBatches.get(),
                        currentFrameIrisDrawCalls.get(),
                        currentFrameIrisFallbackBatches.get(),
                        currentFrameIrisFallbackVertices.get(),
                        lastFrameIrisEligibleBatches.get(),
                        lastFrameIrisDrawCalls.get(),
                        lastFrameIrisFallbackBatches.get(),
                        lastFrameIrisFallbackVertices.get(),
                        irisShadowDrawCalls.get(),
                        currentFrameIrisShadowDrawCalls.get(),
                        lastFrameIrisShadowDrawCalls.get(),
                        HbmIrisRenderBatch.snapshot(),
                        new IrisCompanionQueueSnapshot(
                                irisQueuedBatches.get(),
                                irisQueuedInstances.get(),
                                irisQueuedFlushes.get(),
                                irisQueuedDrawCalls.get(),
                                irisQueuedFallbackBatches.get(),
                                irisQueuedFallbackInstances.get(),
                                currentFrameIrisQueuedBatches.get(),
                                currentFrameIrisQueuedInstances.get(),
                                currentFrameIrisQueuedFlushes.get(),
                                currentFrameIrisQueuedDrawCalls.get(),
                                currentFrameIrisQueuedFallbackBatches.get(),
                                currentFrameIrisQueuedFallbackInstances.get(),
                                lastFrameIrisQueuedBatches.get(),
                                lastFrameIrisQueuedInstances.get(),
                                lastFrameIrisQueuedFlushes.get(),
                                lastFrameIrisQueuedDrawCalls.get(),
                                lastFrameIrisQueuedFallbackBatches.get(),
                                lastFrameIrisQueuedFallbackInstances.get()));
            }
        }

        private final class MdiDrawArraysAtlas {
            private static final int INITIAL_VERTEX_BYTES = 256 * 1024;
            private static final int INITIAL_INSTANCE_BYTES = 4096 * InstancedInstance.FLOATS * 4;
            private static final int INITIAL_INDIRECT_BYTES = 4096 * MdiPreparedGroup.COMMAND_STRIDE_BYTES;
            private static final long MAX_VERTEX_BYTES = 32L * 1024L * 1024L;
            private static final long MAX_INSTANCE_BYTES = 16L * 1024L * 1024L;
            private static final long MAX_INDIRECT_BYTES = 1024L * 1024L;

            private final Map<InstancedMeshKey, MdiSlot> slots = new LinkedHashMap<>();
            private int vaoId;
            private int vertexVboId;
            private int instanceVboId;
            private int indirectBufferId;
            private long vertexCapacityBytes;
            private long instanceCapacityBytes;
            private long indirectCapacityBytes;
            private long vertexUsedBytes;

            private synchronized MdiPreparedGroup prepare(MdiDrawGroupKey key, List<InstancedBatch> batches) {
                if (batches.isEmpty()) {
                    return null;
                }
                ensureReady();
                int totalInstances = totalInstances(batches);
                if (totalInstances <= 0) {
                    return null;
                }
                long instanceBytesRequired = (long) totalInstances * InstancedInstance.FLOATS * 4L;
                long commandBytesRequired = (long) batches.size() * MdiPreparedGroup.COMMAND_STRIDE_BYTES;
                if (instanceBytesRequired > MAX_INSTANCE_BYTES || commandBytesRequired > MAX_INDIRECT_BYTES) {
                    return null;
                }
                ByteBuffer instanceBytes = ByteBuffer.allocateDirect((int) instanceBytesRequired)
                        .order(ByteOrder.nativeOrder());
                ByteBuffer commandBytes = ByteBuffer.allocateDirect((int) commandBytesRequired)
                        .order(ByteOrder.nativeOrder());
                int baseInstance = 0;
                int commandCount = 0;
                for (InstancedBatch batch : batches) {
                    if (batch.instances().isEmpty()) {
                        continue;
                    }
                    MdiSlot slot = slotFor(batch.mesh());
                    if (slot == null) {
                        return null;
                    }
                    for (InstancedInstance instance : batch.instances()) {
                        instance.write(instanceBytes);
                    }
                    commandBytes.putInt(slot.vertexCount());
                    commandBytes.putInt(batch.instances().size());
                    commandBytes.putInt(slot.firstVertex());
                    commandBytes.putInt(baseInstance);
                    baseInstance += batch.instances().size();
                    commandCount++;
                }
                instanceBytes.flip();
                commandBytes.flip();
                return new MdiPreparedGroup(key, instanceBytes, commandBytes, commandCount, totalInstances);
            }

            private void ensureReady() {
                if (vaoId != 0) {
                    return;
                }
                vaoId = GL30.glGenVertexArrays();
                vertexVboId = GL15.glGenBuffers();
                instanceVboId = GL15.glGenBuffers();
                indirectBufferId = GL15.glGenBuffers();
                if (vaoId == 0 || vertexVboId == 0 || instanceVboId == 0 || indirectBufferId == 0) {
                    resetNow();
                    throw new IllegalStateException("Failed to allocate legacy OBJ MDI atlas buffers");
                }
                GL30.glBindVertexArray(vaoId);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVboId);
                vertexCapacityBytes = INITIAL_VERTEX_BYTES;
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexCapacityBytes, GL15.GL_STATIC_DRAW);
                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, INSTANCED_VERTEX_STRIDE_BYTES, 0L);
                GL20.glEnableVertexAttribArray(1);
                GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, INSTANCED_VERTEX_STRIDE_BYTES, 12L);
                GL20.glEnableVertexAttribArray(2);
                GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, INSTANCED_VERTEX_STRIDE_BYTES, 24L);
                GL20.glEnableVertexAttribArray(13);
                GL20.glVertexAttribPointer(13, 3, GL11.GL_FLOAT, false, INSTANCED_VERTEX_STRIDE_BYTES, 32L);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVboId);
                instanceCapacityBytes = INITIAL_INSTANCE_BYTES;
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, instanceCapacityBytes, GL15.GL_STREAM_DRAW);
                int instanceStride = InstancedInstance.FLOATS * 4;
                for (int attribute = 3; attribute <= 12; attribute++) {
                    GL20.glEnableVertexAttribArray(attribute);
                    GL20.glVertexAttribPointer(attribute, 4, GL11.GL_FLOAT, false, instanceStride,
                            (long) (attribute - 3) * 16L);
                    GL33.glVertexAttribDivisor(attribute, 1);
                }

                GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, indirectBufferId);
                indirectCapacityBytes = INITIAL_INDIRECT_BYTES;
                GL15.glBufferData(GL40.GL_DRAW_INDIRECT_BUFFER, indirectCapacityBytes, GL15.GL_STREAM_DRAW);

                GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, 0);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL30.glBindVertexArray(0);
            }

            private MdiSlot slotFor(InstancedMesh mesh) {
                MdiSlot existing = slots.get(mesh.key());
                if (existing != null) {
                    return existing;
                }
                ByteBuffer vertexBytes = buildInstancedVertexBytes(mesh.key().kind(), mesh.sourceMode(),
                        mesh.sourceVertices(), mesh.key().smoothing(), mesh.key().sprite());
                if (!ensureVertexCapacity(vertexBytes.limit())) {
                    return null;
                }
                int firstVertex = (int) (vertexUsedBytes / INSTANCED_VERTEX_STRIDE_BYTES);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVboId);
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, vertexUsedBytes, vertexBytes);
                MdiSlot created = new MdiSlot(mesh, firstVertex, vertexBytes.limit() / INSTANCED_VERTEX_STRIDE_BYTES,
                        vertexBytes.limit());
                slots.put(mesh.key(), created);
                vertexUsedBytes += vertexBytes.limit();
                return created;
            }

            private boolean ensureVertexCapacity(int incomingBytes) {
                long requiredBytes = vertexUsedBytes + incomingBytes;
                if (requiredBytes <= vertexCapacityBytes) {
                    return true;
                }
                if (requiredBytes > MAX_VERTEX_BYTES) {
                    return false;
                }
                long newCapacity = Math.max(requiredBytes, Math.max(INITIAL_VERTEX_BYTES, vertexCapacityBytes) * 2L);
                while (newCapacity < requiredBytes) {
                    newCapacity *= 2L;
                }
                if (newCapacity > MAX_VERTEX_BYTES) {
                    newCapacity = MAX_VERTEX_BYTES;
                }
                vertexCapacityBytes = newCapacity;
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVboId);
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexCapacityBytes, GL15.GL_STATIC_DRAW);
                vertexUsedBytes = 0L;
                for (MdiSlot slot : slots.values()) {
                    ByteBuffer bytes = buildInstancedVertexBytes(slot.mesh().key().kind(),
                            slot.mesh().sourceMode(), slot.mesh().sourceVertices(),
                            slot.mesh().key().smoothing(), slot.mesh().key().sprite());
                    int firstVertex = (int) (vertexUsedBytes / INSTANCED_VERTEX_STRIDE_BYTES);
                    GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, vertexUsedBytes, bytes);
                    slot.update(firstVertex, bytes.limit() / INSTANCED_VERTEX_STRIDE_BYTES, bytes.limit());
                    vertexUsedBytes += bytes.limit();
                }
                return true;
            }

            private void ensureInstanceCapacity(int requiredBytes) {
                if (requiredBytes <= instanceCapacityBytes) {
                    return;
                }
                if (requiredBytes > MAX_INSTANCE_BYTES) {
                    throw new IllegalStateException("Legacy OBJ MDI instance buffer request exceeds atlas cap");
                }
                long newCapacity = Math.max(requiredBytes, Math.max(INITIAL_INSTANCE_BYTES, instanceCapacityBytes) * 2L);
                while (newCapacity < requiredBytes) {
                    newCapacity *= 2L;
                }
                if (newCapacity > MAX_INSTANCE_BYTES) {
                    newCapacity = MAX_INSTANCE_BYTES;
                }
                instanceCapacityBytes = newCapacity;
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVboId);
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, instanceCapacityBytes, GL15.GL_STREAM_DRAW);
            }

            private void ensureIndirectCapacity(int requiredBytes) {
                if (requiredBytes <= indirectCapacityBytes) {
                    return;
                }
                if (requiredBytes > MAX_INDIRECT_BYTES) {
                    throw new IllegalStateException("Legacy OBJ MDI indirect command buffer request exceeds atlas cap");
                }
                long newCapacity = Math.max(requiredBytes, Math.max(INITIAL_INDIRECT_BYTES, indirectCapacityBytes) * 2L);
                while (newCapacity < requiredBytes) {
                    newCapacity *= 2L;
                }
                if (newCapacity > MAX_INDIRECT_BYTES) {
                    newCapacity = MAX_INDIRECT_BYTES;
                }
                indirectCapacityBytes = newCapacity;
                GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, indirectBufferId);
                GL15.glBufferData(GL40.GL_DRAW_INDIRECT_BUFFER, indirectCapacityBytes, GL15.GL_STREAM_DRAW);
            }

            private int vaoId() {
                return vaoId;
            }

            private int instanceVboId() {
                return instanceVboId;
            }

            private int indirectBufferId() {
                return indirectBufferId;
            }

            private synchronized int partCount() {
                return slots.size();
            }

            private synchronized long byteCapacity() {
                return vertexCapacityBytes + instanceCapacityBytes + indirectCapacityBytes;
            }

            private void resetLater() {
                Runnable reset = this::resetNow;
                if (RenderSystem.isOnRenderThread()) {
                    reset.run();
                } else {
                    RenderSystem.recordRenderCall(reset::run);
                }
            }

            private synchronized void resetNow() {
                slots.clear();
                vertexUsedBytes = 0L;
                vertexCapacityBytes = 0L;
                instanceCapacityBytes = 0L;
                indirectCapacityBytes = 0L;
                if (vaoId != 0) {
                    GL30.glDeleteVertexArrays(vaoId);
                    vaoId = 0;
                }
                if (vertexVboId != 0) {
                    GL15.glDeleteBuffers(vertexVboId);
                    vertexVboId = 0;
                }
                if (instanceVboId != 0) {
                    GL15.glDeleteBuffers(instanceVboId);
                    instanceVboId = 0;
                }
                if (indirectBufferId != 0) {
                    GL15.glDeleteBuffers(indirectBufferId);
                    indirectBufferId = 0;
                }
            }
        }

        private static final class MdiSlot {
            private final InstancedMesh mesh;
            private int firstVertex;
            private int vertexCount;
            private long byteSize;

            private MdiSlot(InstancedMesh mesh, int firstVertex, int vertexCount, long byteSize) {
                this.mesh = mesh;
                this.firstVertex = firstVertex;
                this.vertexCount = vertexCount;
                this.byteSize = byteSize;
            }

            private InstancedMesh mesh() {
                return mesh;
            }

            private int firstVertex() {
                return firstVertex;
            }

            private int vertexCount() {
                return vertexCount;
            }

            private void update(int firstVertex, int vertexCount, long byteSize) {
                this.firstVertex = firstVertex;
                this.vertexCount = vertexCount;
                this.byteSize = byteSize;
            }
        }

        private static void closeLater(List<GpuMesh> meshes) {
            if (meshes.isEmpty()) {
                return;
            }
            Runnable closer = () -> {
                for (GpuMesh mesh : meshes) {
                    mesh.close();
                }
            };
            if (RenderSystem.isOnRenderThread()) {
                closer.run();
            } else {
                RenderSystem.recordRenderCall(closer::run);
            }
        }

        private static void closeLater(GpuMesh mesh) {
            closeLater(List.of(mesh));
        }

        private static void closeInstancedLater(List<InstancedMesh> meshes) {
            if (meshes.isEmpty()) {
                return;
            }
            Runnable closer = () -> {
                for (InstancedMesh mesh : meshes) {
                    mesh.close();
                }
            };
            if (RenderSystem.isOnRenderThread()) {
                closer.run();
            } else {
                RenderSystem.recordRenderCall(closer::run);
            }
        }

        private static void closeInstancedLater(InstancedMesh mesh) {
            closeInstancedLater(List.of(mesh));
        }

        private static void closeIrisLater(List<IrisCompanionMesh> meshes) {
            if (meshes.isEmpty()) {
                return;
            }
            Runnable closer = () -> {
                for (IrisCompanionMesh mesh : meshes) {
                    mesh.close();
                }
            };
            if (RenderSystem.isOnRenderThread()) {
                closer.run();
            } else {
                RenderSystem.recordRenderCall(closer::run);
            }
        }

        private static void closeIrisLater(IrisCompanionMesh mesh) {
            closeIrisLater(List.of(mesh));
        }

        private static final class IrisCompanionMesh {
            private static final int CACHED_PROGRAM_SLOTS = 4;
            private static final int LIGHTMAP_SLOT_CAPACITY = 64;
            private static final int CLIENT_MAPPED_BUFFER_BARRIER_BIT = 0x00004000;

            private final IrisCompanionMeshKey key;
            private final int vaoId;
            private final int vboId;
            private final int vertexCount;
            private final long byteSize;
            private final VertexFormat format;
            private final float[] lightWeights;
            private final Map<String, Integer> elementOffsets = new LinkedHashMap<>();
            private final Map<String, VertexFormatElement> elementByName = new LinkedHashMap<>();
            private final int[] cachedPrograms = new int[CACHED_PROGRAM_SLOTS];
            private int colorLocation = -1;
            private int uv1Location = -1;
            private int uv2Location = -1;
            private int lightmapVboId;
            private int lightmapStagingVboId;
            private ByteBuffer lightmapScratch;
            private ByteBuffer lightmapMapped;
            private ByteBuffer lightmapStagingMapped;
            private long lightmapStagingFence;
            private boolean lightmapPersistentMapped;
            private boolean lightmapStagingAvailable;
            private int lightmapAllocatedSlots;
            private int lightmapCurrentSlot = -1;
            private final LinkedHashMap<IrisCompanionLightmapKey, Integer> lightmapSlots =
                    new LinkedHashMap<>(16, 0.75F, true);
            private long cachedProgramGeneration = -1L;
            private int nextProgramSlot;

            private IrisCompanionMesh(IrisCompanionMeshKey key, int vaoId, int vboId, int vertexCount,
                    long byteSize, VertexFormat format, float[] lightWeights) {
                this.key = key;
                this.vaoId = vaoId;
                this.vboId = vboId;
                this.vertexCount = vertexCount;
                this.byteSize = byteSize;
                this.format = format;
                this.lightWeights = lightWeights;
                Arrays.fill(this.cachedPrograms, -1);
                captureElementOffsets(format);
            }

            private void captureElementOffsets(VertexFormat format) {
                List<VertexFormatElement> elements = format.getElements();
                List<String> names = format.getElementAttributeNames();
                int offset = 0;
                for (int i = 0; i < elements.size(); i++) {
                    VertexFormatElement element = elements.get(i);
                    String name = names.get(i);
                    elementOffsets.put(name, offset);
                    elementByName.put(name, element);
                    offset += element.getByteSize();
                }
            }

            private void bindStandardAttributes() {
                int stride = format.getVertexSize();
                List<VertexFormatElement> elements = format.getElements();
                int offset = 0;
                int location = 0;
                for (VertexFormatElement element : elements) {
                    if (element.getUsage() != VertexFormatElement.Usage.PADDING && location <= 5) {
                        bindAttribute(location, element, stride, offset);
                        if (element.getUsage() == VertexFormatElement.Usage.COLOR) {
                            colorLocation = location;
                            GL20.glDisableVertexAttribArray(location);
                        } else if (element.getUsage() == VertexFormatElement.Usage.UV && element.getIndex() == 1) {
                            uv1Location = location;
                            GL20.glDisableVertexAttribArray(location);
                        } else if (element.getUsage() == VertexFormatElement.Usage.UV && element.getIndex() == 2) {
                            uv2Location = location;
                            GL20.glDisableVertexAttribArray(location);
                        }
                        location++;
                    }
                    offset += element.getByteSize();
                }
            }

            private void bind() {
                GL30.glBindVertexArray(vaoId);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
            }

            private void prepareForShader(ShaderInstance shader) {
                if (shader == null || shader.getId() <= 0) {
                    return;
                }
                long generation = HbmRenderFrameFlags.current().shaderPipelineGeneration();
                if (generation != cachedProgramGeneration) {
                    Arrays.fill(cachedPrograms, -1);
                    cachedProgramGeneration = generation;
                    nextProgramSlot = 0;
                }
                int programId = shader.getId();
                for (int cached : cachedPrograms) {
                    if (cached == programId) {
                        return;
                    }
                }
                int stride = format.getVertexSize();
                bindIrisAttribute(programId, "iris_Entity", stride);
                bindIrisAttribute(programId, "mc_midTexCoord", stride);
                bindIrisAttribute(programId, "at_tangent", stride);
                cachedPrograms[nextProgramSlot] = programId;
                nextProgramSlot = (nextProgramSlot + 1) % CACHED_PROGRAM_SLOTS;
            }

            private void applyDrawAttributes(int packedLight, int packedOverlay, int red, int green, int blue,
                    int alpha, boolean allowPerVertexLightmap) {
                applyDrawAttributes(packedLight, packedOverlay, red, green, blue, alpha, allowPerVertexLightmap, -1);
            }

            private void applyDrawAttributes(int packedLight, int packedOverlay, int red, int green, int blue,
                    int alpha, boolean allowPerVertexLightmap, int preparedLightmapSlot) {
                if (colorLocation >= 0) {
                    GL20.glVertexAttrib4f(colorLocation, red / 255.0F, green / 255.0F, blue / 255.0F,
                            alpha / 255.0F);
                }
                if (uv1Location >= 0) {
                    GL30.glVertexAttribI2i(uv1Location, packedOverlay & 0xFFFF, packedOverlay >>> 16 & 0xFFFF);
                }
                if (uv2Location >= 0) {
                    if (allowPerVertexLightmap && preparedLightmapSlot >= 0) {
                        bindLightmapSlot(preparedLightmapSlot);
                    } else if (!allowPerVertexLightmap || !applyPerVertexLightmap(
                            LegacyRenderLighting.currentInstanceLightProbe(packedLight))) {
                        GL20.glDisableVertexAttribArray(uv2Location);
                        HbmIrisRenderBatch.applyConstantLightmap(uv2Location, packedLight);
                    }
                }
            }

            private boolean applyPerVertexLightmap(LegacyRenderLighting.LightProbe probe) {
                int slot = preparePerVertexLightmapSlot(probe);
                if (slot < 0) {
                    return false;
                }
                bindLightmapSlot(slot);
                return true;
            }

            private int preparePerVertexLightmapSlot(int packedLight) {
                return preparePerVertexLightmapSlot(LegacyRenderLighting.currentInstanceLightProbe(packedLight));
            }

            private int preparePerVertexLightmapSlot(LegacyRenderLighting.LightProbe probe) {
                if (uv2Location < 0 || vertexCount <= 0 || lightWeights.length < vertexCount * 3) {
                    return -1;
                }
                if (!ensureLightmapSlotStorage()) {
                    return -1;
                }
                IrisCompanionLightmapKey lightmapKey = IrisCompanionLightmapKey.of(probe);
                Integer cachedSlot = lightmapSlots.get(lightmapKey);
                int slot = cachedSlot != null ? cachedSlot : allocateLightmapSlot(lightmapKey);
                if (cachedSlot == null) {
                    uploadLightmapSlot(slot, probe);
                }
                return slot;
            }

            private boolean ensureLightmapSlotStorage() {
                if (lightmapAllocatedSlots == LIGHTMAP_SLOT_CAPACITY && lightmapVboId != 0
                        && (lightmapPersistentMapped ? lightmapMapped != null : lightmapScratch != null)) {
                    return true;
                }
                int perSlotBytes = vertexCount * 4;
                long totalBytes = (long) perSlotBytes * LIGHTMAP_SLOT_CAPACITY;
                if (perSlotBytes <= 0 || totalBytes > Integer.MAX_VALUE) {
                    return false;
                }
                if (lightmapVboId == 0) {
                    lightmapVboId = GL15.glGenBuffers();
                    if (lightmapVboId == 0) {
                        return false;
                    }
                }
                lightmapMapped = null;
                closeLightmapStaging();
                lightmapPersistentMapped = false;
                lightmapStagingAvailable = false;
                int byteSize = (int) totalBytes;
                int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                try {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightmapVboId);
                    if (canUsePersistentLightmapMapping()) {
                        try {
                            int storageFlags = GL44.GL_MAP_WRITE_BIT
                                    | GL44.GL_MAP_PERSISTENT_BIT
                                    | GL44.GL_MAP_COHERENT_BIT
                                    | GL44.GL_DYNAMIC_STORAGE_BIT;
                            GL44.glBufferStorage(GL15.GL_ARRAY_BUFFER, totalBytes, storageFlags);
                            int mapFlags = GL44.GL_MAP_WRITE_BIT
                                    | GL44.GL_MAP_PERSISTENT_BIT
                                    | GL44.GL_MAP_COHERENT_BIT;
                            lightmapMapped = GL30.glMapBufferRange(GL15.GL_ARRAY_BUFFER, 0L, totalBytes, mapFlags);
                            if (lightmapMapped != null) {
                                lightmapMapped.order(ByteOrder.nativeOrder());
                                lightmapPersistentMapped = true;
                            }
                        } catch (Throwable ignored) {
                            lightmapMapped = null;
                            lightmapPersistentMapped = false;
                        }
                    }
                    if (!lightmapPersistentMapped) {
                        if (lightmapVboId != 0) {
                            GL15.glDeleteBuffers(lightmapVboId);
                        }
                        lightmapVboId = GL15.glGenBuffers();
                        if (lightmapVboId == 0) {
                            return false;
                        }
                        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightmapVboId);
                        if (lightmapScratch == null || lightmapScratch.capacity() < byteSize) {
                            lightmapScratch = ByteBuffer.allocateDirect(byteSize).order(ByteOrder.nativeOrder());
                        }
                        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, totalBytes, GL15.GL_STREAM_DRAW);
                        if (canUsePersistentLightmapMapping()) {
                            tryCreateLightmapStaging(totalBytes);
                        }
                    }
                } finally {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
                }
                lightmapAllocatedSlots = LIGHTMAP_SLOT_CAPACITY;
                lightmapCurrentSlot = -1;
                lightmapSlots.clear();
                return true;
            }

            private static boolean canUsePersistentLightmapMapping() {
                try {
                    GLCapabilities capabilities = GL.getCapabilities();
                    return capabilities != null && (capabilities.OpenGL44 || capabilities.GL_ARB_buffer_storage);
                } catch (Throwable ignored) {
                    return false;
                }
            }

            private void tryCreateLightmapStaging(long totalBytes) {
                try {
                    lightmapStagingVboId = GL15.glGenBuffers();
                    if (lightmapStagingVboId == 0) {
                        return;
                    }
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightmapStagingVboId);
                    int storageFlags = GL44.GL_MAP_WRITE_BIT
                            | GL44.GL_MAP_PERSISTENT_BIT
                            | GL44.GL_DYNAMIC_STORAGE_BIT;
                    GL44.glBufferStorage(GL15.GL_ARRAY_BUFFER, totalBytes, storageFlags);
                    int mapFlags = GL44.GL_MAP_WRITE_BIT
                            | GL44.GL_MAP_PERSISTENT_BIT
                            | GL44.GL_MAP_FLUSH_EXPLICIT_BIT;
                    lightmapStagingMapped = GL30.glMapBufferRange(GL15.GL_ARRAY_BUFFER, 0L, totalBytes, mapFlags);
                    if (lightmapStagingMapped != null) {
                        lightmapStagingMapped.order(ByteOrder.nativeOrder());
                        lightmapStagingAvailable = true;
                    }
                } catch (Throwable ignored) {
                    closeLightmapStaging();
                }
            }

            private int allocateLightmapSlot(IrisCompanionLightmapKey lightmapKey) {
                if (lightmapSlots.size() < LIGHTMAP_SLOT_CAPACITY) {
                    int slot = lightmapSlots.size();
                    lightmapSlots.put(lightmapKey, slot);
                    return slot;
                }
                Map.Entry<IrisCompanionLightmapKey, Integer> eldest = lightmapSlots.entrySet().iterator().next();
                int slot = eldest.getValue();
                lightmapSlots.remove(eldest.getKey());
                lightmapSlots.put(lightmapKey, slot);
                return slot;
            }

            private void uploadLightmapSlot(int slot, LegacyRenderLighting.LightProbe probe) {
                int perSlotBytes = vertexCount * 4;
                int byteOffset = slot * perSlotBytes;
                ByteBuffer slotBytes = lightmapSlotBuffer(byteOffset, perSlotBytes);
                if (slotBytes == null) {
                    return;
                }
                for (int vertex = 0; vertex < vertexCount; vertex++) {
                    int offset = vertex * 3;
                    float x = lightWeights[offset];
                    float y = lightWeights[offset + 1];
                    float z = lightWeights[offset + 2];
                    slotBytes.putShort((short) interpolateBlockLight(probe, x, y, z));
                    slotBytes.putShort((short) interpolateSkyLight(probe, x, y, z));
                }
                slotBytes.flip();
                if (lightmapPersistentMapped) {
                    flushPersistentLightmapWrites();
                } else {
                    int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                    try {
                        if (!uploadLightmapSlotViaStaging(byteOffset, perSlotBytes, slotBytes)) {
                            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightmapVboId);
                            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, byteOffset, slotBytes);
                        }
                    } finally {
                        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
                        GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, 0);
                        GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, 0);
                        lightmapScratch.clear();
                    }
                }
            }

            private boolean uploadLightmapSlotViaStaging(int byteOffset, int byteSize, ByteBuffer slotBytes) {
                if (!lightmapStagingAvailable || lightmapStagingVboId == 0 || lightmapStagingMapped == null) {
                    return false;
                }
                if (lightmapStagingFence != 0L) {
                    int wait = GL32.glClientWaitSync(lightmapStagingFence, 0, 0L);
                    if (wait == GL32.GL_TIMEOUT_EXPIRED) {
                        return false;
                    }
                    GL32.glDeleteSync(lightmapStagingFence);
                    lightmapStagingFence = 0L;
                }
                try {
                    ByteBuffer source = slotBytes.duplicate().order(ByteOrder.nativeOrder());
                    ByteBuffer staging = lightmapStagingMapped.duplicate().order(ByteOrder.nativeOrder());
                    staging.position(byteOffset);
                    staging.limit(byteOffset + byteSize);
                    staging.put(source);

                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightmapStagingVboId);
                    GL30.glFlushMappedBufferRange(GL15.GL_ARRAY_BUFFER, byteOffset, byteSize);
                    GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, lightmapStagingVboId);
                    GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, lightmapVboId);
                    GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER,
                            byteOffset, byteOffset, byteSize);
                    lightmapStagingFence = GL32.glFenceSync(GL32.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
                    return true;
                } catch (Throwable ignored) {
                    return false;
                }
            }

            private ByteBuffer lightmapSlotBuffer(int byteOffset, int byteSize) {
                ByteBuffer source = lightmapPersistentMapped ? lightmapMapped : lightmapScratch;
                if (source == null || byteOffset < 0 || byteOffset + byteSize > source.capacity()) {
                    return null;
                }
                ByteBuffer duplicate = source.duplicate().order(ByteOrder.nativeOrder());
                duplicate.position(byteOffset);
                duplicate.limit(byteOffset + byteSize);
                return duplicate.slice().order(ByteOrder.nativeOrder());
            }

            private static void flushPersistentLightmapWrites() {
                try {
                    GLCapabilities capabilities = GL.getCapabilities();
                    if (capabilities != null && capabilities.OpenGL42) {
                        GL42.glMemoryBarrier(CLIENT_MAPPED_BUFFER_BARRIER_BIT);
                    }
                } catch (Throwable ignored) {
                }
            }

            private void bindLightmapSlot(int slot) {
                if (slot == lightmapCurrentSlot) {
                    GL20.glEnableVertexAttribArray(uv2Location);
                    return;
                }
                long byteOffset = (long) slot * vertexCount * 4L;
                int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                try {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightmapVboId);
                    GL30.glVertexAttribIPointer(uv2Location, 2, GL11.GL_UNSIGNED_SHORT, 4, byteOffset);
                } finally {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
                }
                GL20.glEnableVertexAttribArray(uv2Location);
                lightmapCurrentSlot = slot;
                HbmIrisRenderBatch.invalidateLightmapAttributeCache();
            }

            private void restoreConstantLightmap() {
                if (uv2Location < 0 || lightmapCurrentSlot < 0) {
                    return;
                }
                int previousVao = HbmGlVaoSafety.currentBinding();
                try {
                    HbmGlVaoSafety.bindVertexArray(vaoId);
                    GL20.glDisableVertexAttribArray(uv2Location);
                } finally {
                    HbmGlVaoSafety.bindVertexArray(previousVao);
                    lightmapCurrentSlot = -1;
                    HbmIrisRenderBatch.invalidateLightmapAttributeCache();
                }
            }

            private static int interpolateBlockLight(LegacyRenderLighting.LightProbe probe, float x, float y,
                    float z) {
                return clampLight(Math.round(interpolateLight(probe, x, y, z, true) * 16.0F));
            }

            private static int interpolateSkyLight(LegacyRenderLighting.LightProbe probe, float x, float y,
                    float z) {
                return clampLight(Math.round(interpolateLight(probe, x, y, z, false) * 16.0F));
            }

            private static float interpolateLight(LegacyRenderLighting.LightProbe probe, float x, float y, float z,
                    boolean block) {
                float c000 = lightComponent(probe.c000(), block);
                float c100 = lightComponent(probe.c100(), block);
                float c010 = lightComponent(probe.c010(), block);
                float c110 = lightComponent(probe.c110(), block);
                float c001 = lightComponent(probe.c001(), block);
                float c101 = lightComponent(probe.c101(), block);
                float c011 = lightComponent(probe.c011(), block);
                float c111 = lightComponent(probe.c111(), block);
                float x00 = c000 + (c100 - c000) * x;
                float x10 = c010 + (c110 - c010) * x;
                float x01 = c001 + (c101 - c001) * x;
                float x11 = c011 + (c111 - c011) * x;
                float y0 = x00 + (x10 - x00) * y;
                float y1 = x01 + (x11 - x01) * y;
                return y0 + (y1 - y0) * z;
            }

            private static int lightComponent(int packedLight, boolean block) {
                return block ? LightTexture.block(packedLight) : LightTexture.sky(packedLight);
            }

            private static int clampLight(int value) {
                return Math.max(0, Math.min(240, value));
            }

            private void bindIrisAttribute(int programId, String name, int stride) {
                Integer offset = elementOffsets.get(name);
                VertexFormatElement element = elementByName.get(name);
                if (offset == null || element == null) {
                    return;
                }
                int location = GL20.glGetAttribLocation(programId, name);
                if (location < 0) {
                    return;
                }
                bindAttribute(location, element, stride, offset);
            }

            private static void bindAttribute(int location, VertexFormatElement element, int stride, int offset) {
                int type = element.getType().getGlType();
                int count = element.getCount();
                if (isIntegerAttribute(element, type)) {
                    GL30.glVertexAttribIPointer(location, count, type, stride, offset);
                } else {
                    GL20.glVertexAttribPointer(location, count, type, shouldNormalize(element), stride, offset);
                }
                GL20.glEnableVertexAttribArray(location);
            }

            private static boolean shouldNormalize(VertexFormatElement element) {
                return element.getUsage() == VertexFormatElement.Usage.COLOR
                        || element.getUsage() == VertexFormatElement.Usage.NORMAL;
            }

            private static boolean isIntegerAttribute(VertexFormatElement element, int type) {
                if (element.getUsage() == VertexFormatElement.Usage.UV
                        && (element.getIndex() == 1 || element.getIndex() == 2)) {
                    return true;
                }
                return type == GL11.GL_BYTE
                        || type == GL11.GL_UNSIGNED_BYTE
                        || type == GL11.GL_SHORT
                        || type == GL11.GL_UNSIGNED_SHORT
                        || type == GL11.GL_INT
                        || type == GL11.GL_UNSIGNED_INT;
            }

            private int vertexCount() {
                return vertexCount;
            }

            private long byteSize() {
                return byteSize;
            }

            private void close() {
                if (lightmapVboId != 0) {
                    if (lightmapPersistentMapped) {
                        int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                        try {
                            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightmapVboId);
                            GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
                        } catch (Throwable ignored) {
                        } finally {
                            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
                        }
                    }
                    GL15.glDeleteBuffers(lightmapVboId);
                    lightmapVboId = 0;
                }
                closeLightmapStaging();
                lightmapMapped = null;
                lightmapPersistentMapped = false;
                lightmapStagingAvailable = false;
                lightmapAllocatedSlots = 0;
                lightmapCurrentSlot = -1;
                lightmapSlots.clear();
                if (vboId != 0) {
                    GL15.glDeleteBuffers(vboId);
                }
                if (vaoId != 0) {
                    GL30.glDeleteVertexArrays(vaoId);
                }
            }

            @Override
            public String toString() {
                return key.toString();
            }

            private void closeLightmapStaging() {
                if (lightmapStagingFence != 0L) {
                    try {
                        GL32.glDeleteSync(lightmapStagingFence);
                    } catch (Throwable ignored) {
                    }
                    lightmapStagingFence = 0L;
                }
                if (lightmapStagingVboId != 0) {
                    int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                    try {
                        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightmapStagingVboId);
                        if (lightmapStagingMapped != null) {
                            GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
                        }
                    } catch (Throwable ignored) {
                    } finally {
                        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
                    }
                    GL15.glDeleteBuffers(lightmapStagingVboId);
                    lightmapStagingVboId = 0;
                }
                lightmapStagingMapped = null;
                lightmapStagingAvailable = false;
            }

            private record IrisCompanionLightmapKey(int c000, int c100, int c010, int c110,
                                                    int c001, int c101, int c011, int c111) {
                private static IrisCompanionLightmapKey of(LegacyRenderLighting.LightProbe probe) {
                    return new IrisCompanionLightmapKey(
                            probe.c000(), probe.c100(), probe.c010(), probe.c110(),
                            probe.c001(), probe.c101(), probe.c011(), probe.c111());
                }
            }
        }

        private static int instancedTailPriority(LegacyTexturedRenderMode renderMode) {
            if (isInstancedNormalAlphaMode(renderMode)) {
                return 3;
            }
            if (isInstancedAdditiveMode(renderMode)) {
                return 2;
            }
            if (isInstancedGlintMode(renderMode)) {
                return 1;
            }
            return 0;
        }
    }

    private static final class CpuPreparedRenderBackend implements RenderBackend {
        private static final RenderBackendCapabilities CAPABILITIES = new RenderBackendCapabilities(
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                false);

        private final AtomicLong reloadClears = new AtomicLong();
        private final AtomicLong backendClears = new AtomicLong();
        private final AtomicLong clientDisconnectClears = new AtomicLong();
        private final AtomicLong shaderReloadClears = new AtomicLong();
        private final AtomicLong manualClears = new AtomicLong();
        private final AtomicLong backendClearGeneration = new AtomicLong();
        private volatile RenderBackendClearReason lastClearReason = RenderBackendClearReason.MANUAL;
        private final AtomicLong backendFlushes = new AtomicLong();
        private final AtomicLong afterBlockEntitiesFlushes = new AtomicLong();
        private final AtomicLong manualFlushes = new AtomicLong();
        private final AtomicLong backendFlushGeneration = new AtomicLong();
        private volatile RenderBackendFlushStage lastFlushStage = RenderBackendFlushStage.MANUAL;
        private final AtomicLong texturedBatches = new AtomicLong();
        private final AtomicLong texturedVertices = new AtomicLong();
        private final AtomicLong spriteBatches = new AtomicLong();
        private final AtomicLong spriteVertices = new AtomicLong();
        private final AtomicLong untexturedBatches = new AtomicLong();
        private final AtomicLong untexturedVertices = new AtomicLong();
        private final AtomicLong texturedDrawCalls = new AtomicLong();
        private final AtomicLong spriteDrawCalls = new AtomicLong();
        private final AtomicLong untexturedDrawCalls = new AtomicLong();
        private final AtomicLong textureSwitches = new AtomicLong();
        private final AtomicLong renderModeSwitches = new AtomicLong();
        private final AtomicLong texturedClippedFallbackBatches = new AtomicLong();
        private final AtomicLong texturedClippedFallbackVertices = new AtomicLong();
        private final AtomicLong untexturedClippedFallbackBatches = new AtomicLong();
        private final AtomicLong untexturedClippedFallbackVertices = new AtomicLong();
        private final AtomicLong frameGeneration = new AtomicLong();
        private final AtomicLong currentFrameTexturedBatches = new AtomicLong();
        private final AtomicLong currentFrameTexturedVertices = new AtomicLong();
        private final AtomicLong currentFrameSpriteBatches = new AtomicLong();
        private final AtomicLong currentFrameSpriteVertices = new AtomicLong();
        private final AtomicLong currentFrameUntexturedBatches = new AtomicLong();
        private final AtomicLong currentFrameUntexturedVertices = new AtomicLong();
        private final AtomicLong currentFrameCpuFallbackBatches = new AtomicLong();
        private final AtomicLong currentFrameCpuFallbackVertices = new AtomicLong();
        private final AtomicLong currentFrameBackendFlushes = new AtomicLong();
        private final AtomicLong currentFrameEstimatedDrawCalls = new AtomicLong();
        private final AtomicLong currentFrameTextureSwitches = new AtomicLong();
        private final AtomicLong lastFrameTexturedBatches = new AtomicLong();
        private final AtomicLong lastFrameTexturedVertices = new AtomicLong();
        private final AtomicLong lastFrameSpriteBatches = new AtomicLong();
        private final AtomicLong lastFrameSpriteVertices = new AtomicLong();
        private final AtomicLong lastFrameUntexturedBatches = new AtomicLong();
        private final AtomicLong lastFrameUntexturedVertices = new AtomicLong();
        private final AtomicLong lastFrameCpuFallbackBatches = new AtomicLong();
        private final AtomicLong lastFrameCpuFallbackVertices = new AtomicLong();
        private final AtomicLong lastFrameBackendFlushes = new AtomicLong();
        private final AtomicLong lastFrameEstimatedDrawCalls = new AtomicLong();
        private final AtomicLong lastFrameTextureSwitches = new AtomicLong();
        private volatile ResourceLocation lastTextureLocation;
        private volatile TextureAtlasSprite lastSprite;
        private volatile LegacyTexturedRenderMode lastRenderMode;
        private volatile RenderBackendFallbackReason lastFallbackReason = RenderBackendFallbackReason.NONE;

        @Override
        public void renderTextured(PreparedBatch batch, ResourceLocation textureLocation, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                boolean legacyShadow, boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            if (batch.empty()) {
                return;
            }
            texturedBatches.incrementAndGet();
            texturedVertices.addAndGet(batch.vertexCount());
            currentFrameTexturedBatches.incrementAndGet();
            currentFrameTexturedVertices.addAndGet(batch.vertexCount());
            VertexConsumer quadConsumer = null;
            VertexConsumer triangleConsumer = null;
            PoseStack.Pose pose = poseStack.last();
            Matrix4f position = pose.pose();
            Matrix3f normal = pose.normal();
            LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, normal).withAlpha(alpha);
            recordTextureAndMode(textureLocation, alphaMode);
            List<PreparedVertex> quadVertices = batch.quadVertices();
            if (!quadVertices.isEmpty()) {
                recordTexturedDrawCall();
                if (quadConsumer == null) {
                    quadConsumer = buffer.getBuffer(alphaMode.renderType(textureLocation, VertexFormat.Mode.QUADS));
                }
                emitPreparedVertices(quadVertices, quadConsumer, position, normal, packedLight, packedOverlay,
                        red, green, blue, alpha, legacyShadow, smoothing, uvTransform);
            }
            List<PreparedVertex> triangleVertices = batch.triangleVertices();
            if (!triangleVertices.isEmpty()) {
                recordTexturedDrawCall();
                if (triangleConsumer == null) {
                    triangleConsumer = buffer.getBuffer(alphaMode.renderType(textureLocation, VertexFormat.Mode.TRIANGLES));
                }
                emitPreparedVertices(triangleVertices, triangleConsumer, position, normal, packedLight, packedOverlay,
                        red, green, blue, alpha, legacyShadow, smoothing, uvTransform);
            }
        }

        @Override
        public void renderSprite(PreparedBatch batch, TextureAtlasSprite sprite, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                boolean legacyShadow, boolean partBrightness, LegacyTexturedRenderMode renderMode,
                UvTransform uvTransform) {
            if (batch.empty()) {
                return;
            }
            spriteBatches.incrementAndGet();
            spriteVertices.addAndGet(batch.vertexCount());
            currentFrameSpriteBatches.incrementAndGet();
            currentFrameSpriteVertices.addAndGet(batch.vertexCount());
            VertexConsumer quadConsumer = null;
            VertexConsumer triangleConsumer = null;
            PoseStack.Pose pose = poseStack.last();
            Matrix4f position = pose.pose();
            Matrix3f normal = pose.normal();
            LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, normal).withAlpha(alpha);
            recordSpriteAndMode(sprite, alphaMode);
            List<PreparedVertex> quadVertices = batch.quadVertices();
            if (!quadVertices.isEmpty()) {
                recordSpriteDrawCall();
                if (quadConsumer == null) {
                    quadConsumer = buffer.getBuffer(alphaMode.renderType(InventoryMenu.BLOCK_ATLAS, VertexFormat.Mode.QUADS));
                }
                emitPreparedVerticesWithSprite(quadVertices, sprite, quadConsumer, position, normal, packedLight,
                        packedOverlay, red, green, blue, alpha, legacyShadow, partBrightness, uvTransform);
            }
            List<PreparedVertex> triangleVertices = batch.triangleVertices();
            if (!triangleVertices.isEmpty()) {
                recordSpriteDrawCall();
                if (triangleConsumer == null) {
                    triangleConsumer = buffer.getBuffer(alphaMode.renderType(InventoryMenu.BLOCK_ATLAS, VertexFormat.Mode.TRIANGLES));
                }
                emitPreparedVerticesWithSprite(triangleVertices, sprite, triangleConsumer, position, normal, packedLight,
                        packedOverlay, red, green, blue, alpha, legacyShadow, partBrightness, uvTransform);
            }
        }

        @Override
        public void renderUntextured(PreparedBatch batch, PoseStack poseStack, MultiBufferSource buffer,
                int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
            if (batch.empty()) {
                return;
            }
            untexturedBatches.incrementAndGet();
            untexturedVertices.addAndGet(batch.vertexCount());
            currentFrameUntexturedBatches.incrementAndGet();
            currentFrameUntexturedVertices.addAndGet(batch.vertexCount());
            PoseStack.Pose pose = poseStack.last();
            Matrix4f position = pose.pose();
            LegacyTexturedRenderMode resolvedRenderMode = renderModeForPose(renderMode, pose.normal());
            recordRenderMode(resolvedRenderMode);
            VertexConsumer quadConsumer = null;
            VertexConsumer triangleConsumer = null;
            List<PreparedVertex> quadVertices = batch.quadVertices();
            if (!quadVertices.isEmpty()) {
                recordUntexturedDrawCall();
                if (quadConsumer == null) {
                    quadConsumer = buffer.getBuffer(LegacyUntexturedQuadRenderer.type(resolvedRenderMode, alpha,
                            VertexFormat.Mode.QUADS));
                }
                emitPreparedVerticesUntextured(quadVertices, quadConsumer, position, red, green, blue, alpha);
            }
            List<PreparedVertex> triangleVertices = batch.triangleVertices();
            if (!triangleVertices.isEmpty()) {
                recordUntexturedDrawCall();
                if (triangleConsumer == null) {
                    triangleConsumer = buffer.getBuffer(LegacyUntexturedQuadRenderer.type(resolvedRenderMode, alpha,
                            VertexFormat.Mode.TRIANGLES));
                }
                emitPreparedVerticesUntextured(triangleVertices, triangleConsumer, position, red, green, blue, alpha);
            }
        }

        @Override
        public void clear(RenderBackendClearReason reason) {
            RenderBackendClearReason resolvedReason = reason == null ? RenderBackendClearReason.MANUAL : reason;
            backendClears.incrementAndGet();
            backendClearGeneration.incrementAndGet();
            lastClearReason = resolvedReason;
            switch (resolvedReason) {
                case RESOURCE_RELOAD -> reloadClears.incrementAndGet();
                case CLIENT_DISCONNECT -> clientDisconnectClears.incrementAndGet();
                case SHADER_RELOAD -> shaderReloadClears.incrementAndGet();
                case MANUAL -> manualClears.incrementAndGet();
            }
        }

        @Override
        public void flush(RenderBackendFlushStage stage) {
            RenderBackendFlushStage resolvedStage = stage == null ? RenderBackendFlushStage.MANUAL : stage;
            backendFlushes.incrementAndGet();
            backendFlushGeneration.incrementAndGet();
            currentFrameBackendFlushes.incrementAndGet();
            lastFlushStage = resolvedStage;
            switch (resolvedStage) {
                case AFTER_BLOCK_ENTITIES -> afterBlockEntitiesFlushes.incrementAndGet();
                case MANUAL -> manualFlushes.incrementAndGet();
            }
        }

        @Override
        public void recordCpuFallback(RenderBackendFallbackReason fallback, int vertices) {
            if (vertices <= 0) {
                return;
            }
            RenderBackendFallbackReason resolvedFallback = fallback == null ? RenderBackendFallbackReason.NONE : fallback;
            lastFallbackReason = resolvedFallback;
            switch (resolvedFallback) {
                case TEXTURED_CLIPPED -> {
                    texturedClippedFallbackBatches.incrementAndGet();
                    texturedClippedFallbackVertices.addAndGet(vertices);
                }
                case UNTEXTURED_CLIPPED -> {
                    untexturedClippedFallbackBatches.incrementAndGet();
                    untexturedClippedFallbackVertices.addAndGet(vertices);
                }
                case NONE -> {
                }
            }
            currentFrameCpuFallbackBatches.incrementAndGet();
            currentFrameCpuFallbackVertices.addAndGet(vertices);
        }

        private void recordTextureAndMode(ResourceLocation textureLocation, LegacyTexturedRenderMode renderMode) {
            ResourceLocation previousTexture = lastTextureLocation;
            if (previousTexture == null || !previousTexture.equals(textureLocation)) {
                textureSwitches.incrementAndGet();
                currentFrameTextureSwitches.incrementAndGet();
                lastTextureLocation = textureLocation;
            }
            recordRenderMode(renderMode);
        }

        private void recordSpriteAndMode(TextureAtlasSprite sprite, LegacyTexturedRenderMode renderMode) {
            TextureAtlasSprite previousSprite = lastSprite;
            if (previousSprite != sprite) {
                textureSwitches.incrementAndGet();
                currentFrameTextureSwitches.incrementAndGet();
                lastSprite = sprite;
            }
            recordRenderMode(renderMode);
        }

        private void recordRenderMode(LegacyTexturedRenderMode renderMode) {
            LegacyTexturedRenderMode previousRenderMode = lastRenderMode;
            if (previousRenderMode != renderMode) {
                renderModeSwitches.incrementAndGet();
                lastRenderMode = renderMode;
            }
        }

        private void recordTexturedDrawCall() {
            texturedDrawCalls.incrementAndGet();
            currentFrameEstimatedDrawCalls.incrementAndGet();
        }

        private void recordSpriteDrawCall() {
            spriteDrawCalls.incrementAndGet();
            currentFrameEstimatedDrawCalls.incrementAndGet();
        }

        private void recordUntexturedDrawCall() {
            untexturedDrawCalls.incrementAndGet();
            currentFrameEstimatedDrawCalls.incrementAndGet();
        }

        @Override
        public void endFrame() {
            lastFrameTexturedBatches.set(currentFrameTexturedBatches.getAndSet(0L));
            lastFrameTexturedVertices.set(currentFrameTexturedVertices.getAndSet(0L));
            lastFrameSpriteBatches.set(currentFrameSpriteBatches.getAndSet(0L));
            lastFrameSpriteVertices.set(currentFrameSpriteVertices.getAndSet(0L));
            lastFrameUntexturedBatches.set(currentFrameUntexturedBatches.getAndSet(0L));
            lastFrameUntexturedVertices.set(currentFrameUntexturedVertices.getAndSet(0L));
            lastFrameCpuFallbackBatches.set(currentFrameCpuFallbackBatches.getAndSet(0L));
            lastFrameCpuFallbackVertices.set(currentFrameCpuFallbackVertices.getAndSet(0L));
            lastFrameBackendFlushes.set(currentFrameBackendFlushes.getAndSet(0L));
            lastFrameEstimatedDrawCalls.set(currentFrameEstimatedDrawCalls.getAndSet(0L));
            lastFrameTextureSwitches.set(currentFrameTextureSwitches.getAndSet(0L));
            frameGeneration.incrementAndGet();
        }

        @Override
        public RenderBackendSnapshot snapshot() {
            long texturedClippedBatches = texturedClippedFallbackBatches.get();
            long texturedClippedVertices = texturedClippedFallbackVertices.get();
            long untexturedClippedBatches = untexturedClippedFallbackBatches.get();
            long untexturedClippedVertices = untexturedClippedFallbackVertices.get();
            return new RenderBackendSnapshot(
                    "cpu_prepared_vertex_consumer",
                    false,
                    true,
                    CAPABILITIES,
                    modelCacheSnapshot(),
                    CACHE_METRICS.groupPreparedBuilds.get(),
                    CACHE_METRICS.groupPreparedVertices.get(),
                    CACHE_METRICS.allPreparedBatchBuilds.get(),
                    CACHE_METRICS.selectionCacheHits.get(),
                    CACHE_METRICS.selectionCacheMisses.get(),
                    CACHE_METRICS.selectionCacheClears.get(),
                    CACHE_METRICS.selectionCacheEmptyBuilds.get(),
                    CACHE_METRICS.selectionCachePreparedBatchBuilds.get(),
                    CACHE_METRICS.selectionHandleRefreshes.get(),
                    CACHE_METRICS.selectionHandleEmptyBuilds.get(),
                    CACHE_METRICS.selectionHandlePreparedBatchBuilds.get(),
                    reloadClears.get(),
                    backendClears.get(),
                    clientDisconnectClears.get(),
                    shaderReloadClears.get(),
                    manualClears.get(),
                    backendClearGeneration.get(),
                    lastClearReason,
                    backendFlushes.get(),
                    afterBlockEntitiesFlushes.get(),
                    manualFlushes.get(),
                    backendFlushGeneration.get(),
                    lastFlushStage,
                    texturedBatches.get(),
                    texturedVertices.get(),
                    spriteBatches.get(),
                    spriteVertices.get(),
                    untexturedBatches.get(),
                    untexturedVertices.get(),
                    texturedDrawCalls.get() + spriteDrawCalls.get() + untexturedDrawCalls.get(),
                    texturedDrawCalls.get(),
                    spriteDrawCalls.get(),
                    untexturedDrawCalls.get(),
                    textureSwitches.get(),
                    renderModeSwitches.get(),
                    texturedClippedBatches + untexturedClippedBatches,
                    texturedClippedVertices + untexturedClippedVertices,
                    texturedClippedBatches,
                    texturedClippedVertices,
                    untexturedClippedBatches,
                    untexturedClippedVertices,
                    lastFallbackReason,
                    frameGeneration.get(),
                    currentFrameTexturedBatches.get(),
                    currentFrameTexturedVertices.get(),
                    currentFrameSpriteBatches.get(),
                    currentFrameSpriteVertices.get(),
                    currentFrameUntexturedBatches.get(),
                    currentFrameUntexturedVertices.get(),
                    currentFrameCpuFallbackBatches.get(),
                    currentFrameCpuFallbackVertices.get(),
                    currentFrameBackendFlushes.get(),
                    currentFrameEstimatedDrawCalls.get(),
                    currentFrameTextureSwitches.get(),
                    0,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    false,
                    false,
                    false,
                    false,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    lastFrameTexturedBatches.get(),
                    lastFrameTexturedVertices.get(),
                    lastFrameSpriteBatches.get(),
                    lastFrameSpriteVertices.get(),
                    lastFrameUntexturedBatches.get(),
                    lastFrameUntexturedVertices.get(),
                    lastFrameCpuFallbackBatches.get(),
                    lastFrameCpuFallbackVertices.get(),
                    lastFrameBackendFlushes.get(),
                    lastFrameEstimatedDrawCalls.get(),
                    lastFrameTextureSwitches.get());
        }
    }

    private static final class Group {
        private final String name;
        private final List<Face> faces;
        private List<PreparedVertex> quadVertices;
        private List<PreparedVertex> triangleVertices;

        private Group(String name, List<Face> faces) {
            this.name = name;
            this.faces = faces;
        }

        private String name() {
            return name;
        }

        private List<Face> faces() {
            return faces;
        }

        private List<PreparedVertex> quadVertices() {
            ensurePrepared();
            return quadVertices;
        }

        private List<PreparedVertex> triangleVertices() {
            ensurePrepared();
            return triangleVertices;
        }

        private void ensurePrepared() {
            if (quadVertices != null && triangleVertices != null) {
                return;
            }
            List<PreparedVertex> quads = new ArrayList<>();
            List<PreparedVertex> triangles = new ArrayList<>();
            for (Face face : faces) {
                prepareFace(face, quads, triangles);
            }
            quadVertices = List.copyOf(quads);
            triangleVertices = List.copyOf(triangles);
            CACHE_METRICS.recordGroupPrepared(quadVertices.size() + triangleVertices.size());
        }

        private static void prepareFace(Face face, List<PreparedVertex> quads, List<PreparedVertex> triangles) {
            int vertexCount = face.vertices().size();
            if (vertexCount < 3) {
                return;
            }
            if (vertexCount == 3) {
                addPrepared(face, triangles, 0);
                addPrepared(face, triangles, 1);
                addPrepared(face, triangles, 2);
                return;
            }
            if (vertexCount == 4) {
                addPrepared(face, quads, 0);
                addPrepared(face, quads, 1);
                addPrepared(face, quads, 2);
                addPrepared(face, quads, 3);
                return;
            }
            for (int i = 1; i + 1 < vertexCount; i++) {
                addPrepared(face, triangles, 0);
                addPrepared(face, triangles, i);
                addPrepared(face, triangles, i + 1);
            }
        }

        private static void addPrepared(Face face, List<PreparedVertex> target, int index) {
            Vector3f smoothNormal = index < face.normals().size() ? face.normals().get(index) : face.faceNormal();
            target.add(new PreparedVertex(face.vertices().get(index),
                    index < face.uvs().size() ? face.uvs().get(index) : UV.ZERO,
                    smoothNormal, face.faceNormal(), face.averageUv()));
        }
    }

    private record Face(List<Vector3f> vertices, List<UV> uvs, List<Vector3f> normals, Vector3f faceNormal,
                        UV averageUv) {
    }

    private record PreparedVertex(Vector3f position, UV uv, Vector3f smoothNormal, Vector3f faceNormal,
                                  UV averageUv) {
    }

    private record PreparedBatch(String stableKey, List<PreparedVertex> quadVertices,
                                 List<PreparedVertex> triangleVertices) {
        private static final PreparedBatch EMPTY = new PreparedBatch("empty", List.of(), List.of());

        private static PreparedBatch from(List<Group> groups, String stableKey) {
            if (groups.isEmpty()) {
                return EMPTY;
            }
            List<PreparedVertex> quads = new ArrayList<>();
            List<PreparedVertex> triangles = new ArrayList<>();
            for (Group group : groups) {
                quads.addAll(group.quadVertices());
                triangles.addAll(group.triangleVertices());
            }
            if (quads.isEmpty() && triangles.isEmpty()) {
                return EMPTY;
            }
            return new PreparedBatch(stableKey, List.copyOf(quads), List.copyOf(triangles));
        }

        private static PreparedBatch clippedFrom(List<Group> groups, String stableKey,
                double clipX, double clipY, double clipZ, double clipD) {
            if (groups.isEmpty()) {
                return EMPTY;
            }
            List<PreparedVertex> quads = new ArrayList<>();
            List<PreparedVertex> triangles = new ArrayList<>();
            for (Group group : groups) {
                for (Face face : group.faces()) {
                    Face clipped = clipFace(face, clipX, clipY, clipZ, clipD);
                    if (clipped != null) {
                        Group.prepareFace(clipped, quads, triangles);
                    }
                }
            }
            if (quads.isEmpty() && triangles.isEmpty()) {
                return EMPTY;
            }
            return new PreparedBatch(stableKey, List.copyOf(quads), List.copyOf(triangles));
        }

        private boolean empty() {
            return quadVertices.isEmpty() && triangleVertices.isEmpty();
        }

        private int vertexCount() {
            return quadVertices.size() + triangleVertices.size();
        }
    }

    private enum GpuMeshKind {
        TEXTURED,
        SPRITE,
        UNTEXTURED
    }

    private record GpuMeshKey(GpuMeshKind kind, int batchIdentity, TextureAtlasSprite sprite, int batchVertices,
                              VertexFormat.Mode drawMode, int packedLight, int packedOverlay, int red, int green,
                              int blue, int alpha, UvTransform uvTransform, boolean smoothing) {
    }

    private record GpuMesh(VertexBuffer vertexBuffer, long byteSize) {
        private void close() {
            vertexBuffer.close();
        }
    }

    private record IrisCompanionMeshKey(GpuMeshKind kind, String stablePartKey, TextureAtlasSprite sprite,
                                        int sourceVertices, VertexFormat.Mode sourceMode, boolean smoothing,
                                        UvTransform uvTransform) {
    }

    private record IrisRenderBatchKey(ResourceLocation textureLocation, LegacyTexturedRenderMode renderMode,
                                      boolean shadowPass) {
    }

    private record IrisCompanionQueueKey(GpuMeshKind kind, String stablePartKey, TextureAtlasSprite sprite,
                                         int sourceVertices, VertexFormat.Mode sourceMode, boolean smoothing,
                                         UvTransform uvTransform, ResourceLocation textureLocation,
                                         LegacyTexturedRenderMode renderMode) {
    }

    private record InstancedMeshKey(GpuMeshKind kind, String stablePartKey, TextureAtlasSprite sprite, int sourceVertices,
                                    VertexFormat.Mode sourceMode, boolean smoothing) {
    }

    private record InstancedBatchKey(InstancedMeshKey meshKey, ResourceLocation textureLocation,
                                     LegacyTexturedRenderMode renderMode) {
    }

    private record MdiDrawGroupKey(GpuMeshKind kind, ResourceLocation textureLocation,
                                   LegacyTexturedRenderMode renderMode) {
    }

    private record MdiPreparedGroup(MdiDrawGroupKey key, ByteBuffer instanceBytes, ByteBuffer commandBytes,
                                    int commandCount, int instanceCount) {
        private static final int COMMAND_STRIDE_BYTES = 16;
    }

    private static final class IrisCompanionQueuedBatch {
        private final IrisCompanionQueueKey key;
        private final List<PreparedVertex> sourceVertices;
        private final List<IrisCompanionQueuedInstance> instances = new ArrayList<>();

        private IrisCompanionQueuedBatch(IrisCompanionQueueKey key, List<PreparedVertex> sourceVertices) {
            this.key = key;
            this.sourceVertices = sourceVertices;
        }

        private IrisCompanionQueueKey key() {
            return key;
        }

        private List<PreparedVertex> sourceVertices() {
            return sourceVertices;
        }

        private List<IrisCompanionQueuedInstance> instances() {
            return instances;
        }
    }

    private record IrisCompanionQueuedInstance(Matrix4f position, Matrix3f normal, MultiBufferSource buffer,
                                               int packedLight, int packedOverlay, int red, int green, int blue,
                                               int alpha, float sortDepthSq) {
        private static IrisCompanionQueuedInstance from(PoseStack.Pose pose, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha) {
            return new IrisCompanionQueuedInstance(new Matrix4f(pose.pose()), new Matrix3f(pose.normal()), buffer,
                    packedLight, packedOverlay, red, green, blue, alpha, viewSortDepthSq(pose.pose()));
        }
    }

    private record IrisCompanionQueuedDraw(IrisCompanionQueuedBatch batch, IrisCompanionQueuedInstance instance) {
    }

    private static final class InstancedBatch {
        private final InstancedBatchKey key;
        private final InstancedMesh mesh;
        private final ResourceLocation textureLocation;
        private final LegacyTexturedRenderMode renderMode;
        private final List<InstancedInstance> instances = new ArrayList<>();
        private final List<InstancedFallbackInstance> fallbacks = new ArrayList<>();

        private InstancedBatch(InstancedMesh mesh, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode) {
            this.key = new InstancedBatchKey(mesh.key(), textureLocation, renderMode);
            this.mesh = mesh;
            this.textureLocation = textureLocation;
            this.renderMode = renderMode;
        }

        private InstancedBatchKey key() {
            return key;
        }

        private InstancedMesh mesh() {
            return mesh;
        }

        private ResourceLocation textureLocation() {
            return textureLocation;
        }

        private LegacyTexturedRenderMode renderMode() {
            return renderMode;
        }

        private List<InstancedInstance> instances() {
            return instances;
        }

        private List<InstancedFallbackInstance> fallbacks() {
            return fallbacks;
        }

        private float maxSortDepthSq() {
            float max = 0.0F;
            for (InstancedInstance instance : instances) {
                max = Math.max(max, instance.sortDepthSq());
            }
            return max;
        }

        private void sortBackToFront() {
            if (instances.size() <= 1) {
                return;
            }
            List<Integer> order = new ArrayList<>(instances.size());
            for (int i = 0; i < instances.size(); i++) {
                order.add(i);
            }
            order.sort((left, right) -> Float.compare(instances.get(right).sortDepthSq(),
                    instances.get(left).sortDepthSq()));
            List<InstancedInstance> sortedInstances = new ArrayList<>(instances.size());
            List<InstancedFallbackInstance> sortedFallbacks = new ArrayList<>(fallbacks.size());
            for (int index : order) {
                sortedInstances.add(instances.get(index));
                sortedFallbacks.add(fallbacks.get(index));
            }
            instances.clear();
            instances.addAll(sortedInstances);
            fallbacks.clear();
            fallbacks.addAll(sortedFallbacks);
        }
    }

    private record InstancedInstance(float[] data, float sortDepthSq) {
        private static final int FLOATS = 40;

        private static InstancedInstance from(Matrix4f modelView, LegacyRenderLighting.LightProbe lightProbe,
                int packedOverlay,
                int red, int green, int blue, int alpha) {
            float[] data = new float[FLOATS];
            data[0] = modelView.m00();
            data[1] = modelView.m01();
            data[2] = modelView.m02();
            data[3] = modelView.m03();
            data[4] = modelView.m10();
            data[5] = modelView.m11();
            data[6] = modelView.m12();
            data[7] = modelView.m13();
            data[8] = modelView.m20();
            data[9] = modelView.m21();
            data[10] = modelView.m22();
            data[11] = modelView.m23();
            data[12] = modelView.m30();
            data[13] = modelView.m31();
            data[14] = modelView.m32();
            data[15] = modelView.m33();

            writeLightPair(data, 16, lightProbe.c000(), lightProbe.c100());
            writeLightPair(data, 20, lightProbe.c010(), lightProbe.c110());
            writeLightPair(data, 24, lightProbe.c001(), lightProbe.c101());
            writeLightPair(data, 28, lightProbe.c011(), lightProbe.c111());
            data[32] = red / 255.0F;
            data[33] = green / 255.0F;
            data[34] = blue / 255.0F;
            data[35] = alpha / 255.0F;
            data[36] = packedOverlay & 0xFFFF;
            data[37] = packedOverlay >>> 16 & 0xFFFF;
            data[38] = 0.0F;
            data[39] = 0.0F;
            return new InstancedInstance(data, viewSortDepthSq(modelView));
        }

        private static void writeLightPair(float[] data, int offset, int firstPackedLight, int secondPackedLight) {
            data[offset] = LightTexture.block(firstPackedLight) * 16.0F;
            data[offset + 1] = LightTexture.sky(firstPackedLight) * 16.0F;
            data[offset + 2] = LightTexture.block(secondPackedLight) * 16.0F;
            data[offset + 3] = LightTexture.sky(secondPackedLight) * 16.0F;
        }

        private void write(ByteBuffer bytes) {
            for (float value : data) {
                bytes.putFloat(value);
            }
        }
    }

    private record InstancedFallbackInstance(Matrix4f position, Matrix3f normal, MultiBufferSource buffer,
                                             int packedLight, int packedOverlay, int red, int green, int blue,
                                             int alpha, UvTransform uvTransform, float sortDepthSq) {
        private static InstancedFallbackInstance from(PoseStack.Pose pose, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, UvTransform uvTransform) {
            return new InstancedFallbackInstance(new Matrix4f(pose.pose()), new Matrix3f(pose.normal()), buffer,
                    packedLight, packedOverlay, red, green, blue, alpha, uvTransform, viewSortDepthSq(pose.pose()));
        }
    }

    private static float viewSortDepthSq(Matrix4f matrix) {
        float x = matrix.m30();
        float y = matrix.m31();
        float z = matrix.m32();
        return x * x + y * y + z * z;
    }

    private record InstancedMesh(InstancedMeshKey key, VertexFormat.Mode sourceMode,
                                 List<PreparedVertex> sourceVertices, int vaoId, int vboId, int instanceVboId,
                                 int vertexCount, long byteSize) {
        private void close() {
            if (vaoId != 0) {
                GL30.glDeleteVertexArrays(vaoId);
            }
            if (vboId != 0) {
                GL15.glDeleteBuffers(vboId);
            }
            if (instanceVboId != 0) {
                GL15.glDeleteBuffers(instanceVboId);
            }
        }
    }

    private record InstancedMeshBounds(float minX, float minY, float minZ,
                                       float invSizeX, float invSizeY, float invSizeZ) {
        private static final float MIN_EXTENT = 1.0E-5F;

        private static InstancedMeshBounds of(List<PreparedVertex> vertices) {
            float minX = Float.POSITIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float minZ = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            float maxZ = Float.NEGATIVE_INFINITY;
            for (PreparedVertex vertex : vertices) {
                Vector3f position = vertex.position();
                minX = Math.min(minX, position.x());
                minY = Math.min(minY, position.y());
                minZ = Math.min(minZ, position.z());
                maxX = Math.max(maxX, position.x());
                maxY = Math.max(maxY, position.y());
                maxZ = Math.max(maxZ, position.z());
            }
            if (!Float.isFinite(minX) || !Float.isFinite(minY) || !Float.isFinite(minZ)
                    || !Float.isFinite(maxX) || !Float.isFinite(maxY) || !Float.isFinite(maxZ)) {
                return new InstancedMeshBounds(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            }
            return new InstancedMeshBounds(minX, minY, minZ,
                    inverseExtent(maxX - minX),
                    inverseExtent(maxY - minY),
                    inverseExtent(maxZ - minZ));
        }

        private static float inverseExtent(float extent) {
            return Math.abs(extent) < MIN_EXTENT ? 0.0F : 1.0F / extent;
        }

        private float weightX(float x) {
            return clampUnit((x - minX) * invSizeX);
        }

        private float weightY(float y) {
            return clampUnit((y - minY) * invSizeY);
        }

        private float weightZ(float z) {
            return clampUnit((z - minZ) * invSizeZ);
        }

        private static float clampUnit(float value) {
            return Math.max(0.0F, Math.min(1.0F, value));
        }
    }

    private enum SelectionCacheMode {
        ONLY,
        ALL_EXCEPT,
        CALL_ORDER,
        LAST_GROUP
    }

    private record SelectionCacheKey(SelectionCacheMode mode, List<String> names) {
    }

    private static final class SelectionCacheEntry {
        private final List<Group> groups;
        private final String stableBatchKey;
        private PreparedBatch batch;

        private SelectionCacheEntry(List<Group> groups, String stableBatchKey) {
            this.groups = groups;
            this.stableBatchKey = stableBatchKey;
        }

        private List<Group> groups() {
            return groups;
        }

        private PreparedBatch batch() {
            if (batch == null) {
                batch = PreparedBatch.from(groups, stableBatchKey);
                CACHE_METRICS.recordSelectionCachePreparedBatchBuild();
            }
            return batch;
        }
    }

    public static final class SelectionHandle {
        private final SelectionCacheMode mode;
        private final List<String> requestedNames;
        private final List<String> normalizedNames;
        private final String[] rawNames;
        private LegacyWavefrontModel model;
        private int generation = Integer.MIN_VALUE;
        private List<Group> groups = List.of();
        private PreparedBatch batch = PreparedBatch.EMPTY;

        private SelectionHandle(SelectionCacheMode mode, List<String> requestedNames, List<String> normalizedNames,
                String[] rawNames) {
            this.mode = mode;
            this.requestedNames = requestedNames;
            this.normalizedNames = normalizedNames;
            this.rawNames = rawNames;
        }

        private List<Group> groups(LegacyWavefrontModel model) {
            if (this.model != model || generation != model.selectionGeneration) {
                SelectionCacheKey key = new SelectionCacheKey(mode, normalizedNames);
                groups = model.createSelectedGroups(key);
                batch = PreparedBatch.from(groups, model.stableSelectionBatchKey(key, groups));
                CACHE_METRICS.recordSelectionHandleRefresh();
                if (groups.isEmpty()) {
                    CACHE_METRICS.recordSelectionHandleEmptyBuild();
                }
                CACHE_METRICS.recordSelectionHandlePreparedBatchBuild();
                this.model = model;
                generation = model.selectionGeneration;
            }
            return groups;
        }

        private PreparedBatch batch(LegacyWavefrontModel model) {
            groups(model);
            return batch;
        }

        private String[] rawNames() {
            return rawNames;
        }

        public List<String> requestedNames() {
            return requestedNames;
        }
    }

    private record ClippedVertex(Vector3f vertex, UV uv, Vector3f normal) {
        private ClippedVertex copy() {
            return new ClippedVertex(new Vector3f(vertex), uv, new Vector3f(normal));
        }
    }

    public record UvTransform(float uScale, float uFromV, float vFromU, float vScale,
            float uOffset, float vOffset, float textureOffset, boolean gpuMeshCacheable) {
        public static final UvTransform DEFAULT = new UvTransform(1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F);

        public UvTransform(float uScale, float uFromV, float vFromU, float vScale,
                float uOffset, float vOffset, float textureOffset) {
            this(uScale, uFromV, vFromU, vScale, uOffset, vOffset, textureOffset, true);
        }

        public static UvTransform dynamic(float uScale, float uFromV, float vFromU, float vScale,
                float uOffset, float vOffset, float textureOffset) {
            return new UvTransform(uScale, uFromV, vFromU, vScale, uOffset, vOffset, textureOffset, false);
        }
    }

    public record UntexturedTransientQuad(
            double x0, double y0, double z0,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            double x3, double y3, double z3) {
    }

    public record UntexturedLineTransient(double x0, double y0, double z0,
                                          double x1, double y1, double z1,
                                          int color, int alpha,
                                          float normalX, float normalY, float normalZ) {
        public UntexturedLineTransient(double x0, double y0, double z0,
                double x1, double y1, double z1, int color, int alpha) {
            this(x0, y0, z0, x1, y1, z1, color & 0xFFFFFF, clampColor(alpha),
                    normalX(x0, y0, z0, x1, y1, z1),
                    normalY(x0, y0, z0, x1, y1, z1),
                    normalZ(x0, y0, z0, x1, y1, z1));
        }

        private static float normalX(double x0, double y0, double z0, double x1, double y1, double z1) {
            double length = length(x0, y0, z0, x1, y1, z1);
            return length <= 1.0E-6D ? 0.0F : (float) ((x1 - x0) / length);
        }

        private static float normalY(double x0, double y0, double z0, double x1, double y1, double z1) {
            double length = length(x0, y0, z0, x1, y1, z1);
            return length <= 1.0E-6D ? 1.0F : (float) ((y1 - y0) / length);
        }

        private static float normalZ(double x0, double y0, double z0, double x1, double y1, double z1) {
            double length = length(x0, y0, z0, x1, y1, z1);
            return length <= 1.0E-6D ? 0.0F : (float) ((z1 - z0) / length);
        }

        private static double length(double x0, double y0, double z0, double x1, double y1, double z1) {
            double dx = x1 - x0;
            double dy = y1 - y0;
            double dz = z1 - z0;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }

    private record UntexturedVertexColorTransientQuad(UntexturedVertexColor v0, UntexturedVertexColor v1,
                                                      UntexturedVertexColor v2, UntexturedVertexColor v3) {
        private int minimumAlpha() {
            return Math.min(Math.min(v0.alpha(), v1.alpha()), Math.min(v2.alpha(), v3.alpha()));
        }
    }

    private record UntexturedVertexColorTransientTriangle(UntexturedVertexColor v0, UntexturedVertexColor v1,
                                                          UntexturedVertexColor v2) {
        private int minimumAlpha() {
            return Math.min(v0.alpha(), Math.min(v1.alpha(), v2.alpha()));
        }
    }

    private record UntexturedVertexColor(double x, double y, double z, int red, int green, int blue, int alpha) {
    }

    private record UV(float u, float v) {
        private static final UV ZERO = new UV(0.0F, 0.0F);
    }
}
