package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.render.HbmInstancedGlCompat;
import com.hbm.ntm.client.render.HbmOptimizedRenderShaders;
import com.hbm.ntm.client.render.HbmGlVaoSafety;
import com.hbm.ntm.client.render.HbmMdiRenderDiag;
import com.hbm.ntm.client.render.HbmRenderFrameLight;
import com.hbm.ntm.client.renderer.LegacyItemRenderContext;
import com.hbm.ntm.client.render.HbmRenderFrameFlags;
import com.hbm.ntm.client.render.HbmRenderBackendDiagnostics;
import com.hbm.ntm.client.render.culling.HbmRenderFrameCulling;
import com.hbm.ntm.client.render.shader.HbmIrisExtendedShaderAccess;
import com.hbm.ntm.client.render.shader.HbmIrisRenderBatch;
import com.hbm.ntm.client.render.shader.HbmShaderCompatibilityDetector;
import com.hbm.ntm.client.renderer.LegacyRenderLighting;
import com.hbm.ntm.config.HbmClientConfig;
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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.ARBDrawIndirect;
import org.lwjgl.opengl.ARBMultiDrawIndirect;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Minimal modern carrier for the old HFRWavefrontObject group rendering path.
 */
public final class LegacyWavefrontModel {
    private static final Set<LegacyWavefrontModel> ALL_MODELS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final int SELECTION_CACHE_LIMIT = 256;
    private static final String NO_FALLBACK_DETAIL = "none";
    private static final int MAX_FALLBACK_DETAIL_LENGTH = 240;
    private static final Matrix4f IDENTITY_POSITION = new Matrix4f();
    private static final Matrix3f IDENTITY_NORMAL = new Matrix3f();
    private static final RenderBackend RENDER_BACKEND = new ExperimentalGpuPreparedRenderBackend(new CpuPreparedRenderBackend());
    private static final CacheMetrics CACHE_METRICS = new CacheMetrics();

    private final ResourceLocation modelLocation;
    private final ResourceLocation textureLocation;
    private final Map<String, List<Group>> groupsByName = new LinkedHashMap<>();
    private final List<Group> groupOrder = new ArrayList<>();
    private final Map<SelectionCacheKey, SelectionCacheEntry> selectionCache = new LinkedHashMap<>();
    private final Set<String> missingPartWarnings = new LinkedHashSet<>();
    private volatile boolean smoothing = true;
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

    public void renderPart(String partName, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay);
    }

    public void renderPart(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255);
    }

    public void renderPart(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false);
    }

    public void renderPartTranslucent(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false,
                LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE, UvTransform.DEFAULT);
    }

    public void renderPartAdditive(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, UvTransform.DEFAULT);
    }

    public void renderPartWithUvScroll(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float uOffset, float vOffset) {
        renderPartWithUvScroll(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255, uOffset, vOffset);
    }

    public void renderPartWithUvScroll(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, float uOffset, float vOffset) {
        renderPartWithUvTransform(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, 1.0F, 1.0F, uOffset, vOffset);
    }

    public void renderPartWithUvTransform(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            float uScale, float vScale, float uOffset, float vOffset) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, new UvTransform(uScale, 0.0F, 0.0F, vScale, uOffset, vOffset, 0.0F));
    }

    public void renderPartWithLegacyTextureMatrix(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            float uScale, float vScale, float uTranslate, float vTranslate) {
        renderPartWithUvTransform(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, uScale, vScale, uTranslate * uScale, vTranslate * vScale);
    }

    public void renderPartWithLegacyTextureMatrix(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            float uScale, float vScale, float rotationDegrees, float uTranslate, float vTranslate) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, legacyTextureMatrix(uScale, vScale, rotationDegrees, uTranslate, vTranslate));
    }

    public void renderPartWithLegacyTextureMatrixCull(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            float uScale, float vScale, float uTranslate, float vTranslate) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false,
                LegacyTexturedRenderMode.CUTOUT_CULL, legacyTextureMatrix(uScale, vScale, 0.0F, uTranslate, vTranslate));
    }

    public void renderPartGlintWithLegacyTextureMatrix(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            float uScale, float vScale, float rotationDegrees, float uTranslate, float vTranslate) {
        renderPart(partName, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false,
                LegacyTexturedRenderMode.GLINT_EQUAL_DEPTH, legacyTextureMatrix(uScale, vScale, rotationDegrees, uTranslate, vTranslate));
    }

    public void renderPart(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
        PreparedBatch batch = selectedPartBatch(partName);
        if (batch == null) {
            return;
        }
        renderPreparedBatch(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue,
                alpha, legacyShadow, smoothing, renderMode, uvTransform);
    }

    public void renderPart(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow) {
        PreparedBatch batch = selectedPartBatch(partName);
        if (batch == null) {
            return;
        }
        renderPreparedBatch(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue,
                alpha, legacyShadow, smoothing, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    public void renderWithSprite(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float yawRadians, float pitchRadians, float rollRadians, boolean legacyShadow) {
        renderWithSprite(sprite, poseStack, buffer, packedLight, packedOverlay, yawRadians, pitchRadians, rollRadians,
                255, 255, 255, 255, legacyShadow);
    }

    public void renderWithSprite(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float yawRadians, float pitchRadians, float rollRadians,
            int red, int green, int blue, int alpha, boolean legacyShadow) {
        renderWithSprite(sprite, poseStack, buffer, packedLight, packedOverlay, yawRadians, pitchRadians, rollRadians,
                red, green, blue, alpha, legacyShadow, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    public void renderWithSprite(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float yawRadians, float pitchRadians, float rollRadians,
            int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
        PreparedBatch batch = allPreparedBatchForRender(false);
        if (batch == null) {
            return;
        }
        poseStack.pushPose();
        LegacyObjTransforms.applyObjUtilRotation(poseStack, yawRadians, pitchRadians, rollRadians);
        renderPreparedBatchWithSprite(batch, sprite, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, legacyShadow, false, renderMode, uvTransform);
        poseStack.popPose();
    }

    public void renderPartWithSprite(String partName, TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float yawRadians, float pitchRadians, float rollRadians, boolean legacyShadow) {
        renderPartWithSprite(partName, sprite, poseStack, buffer, packedLight, packedOverlay, yawRadians, pitchRadians, rollRadians,
                255, 255, 255, 255, legacyShadow);
    }

    public void renderPartWithSprite(String partName, TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float yawRadians, float pitchRadians, float rollRadians,
            int red, int green, int blue, int alpha, boolean legacyShadow) {
        renderPartWithSprite(partName, sprite, poseStack, buffer, packedLight, packedOverlay, yawRadians, pitchRadians, rollRadians,
                red, green, blue, alpha, legacyShadow, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    public void renderPartWithSprite(String partName, TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float yawRadians, float pitchRadians, float rollRadians,
            int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
        PreparedBatch batch = selectedLastGroupBatch(partName);
        if (batch == null) {
            return;
        }
        poseStack.pushPose();
        LegacyObjTransforms.applyObjUtilRotation(poseStack, yawRadians, pitchRadians, rollRadians);
        renderPreparedBatchWithSprite(batch, sprite, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, true, renderMode, uvTransform);
        poseStack.popPose();
    }

    public void renderPartClipped(String name, ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
            double clipX, double clipY, double clipZ, double clipD) {
        PreparedBatch batch = clippedPartBatch(name, clipX, clipY, clipZ, clipD);
        if (batch == null) {
            return;
        }
        RENDER_BACKEND.renderTexturedTransient(batch, textureLocation, poseStack, buffer, packedLight,
                packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform,
                RenderBackendFallbackReason.TEXTURED_CLIPPED);
    }

    public void renderPartUntextured(String partName, PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha) {
        renderPartUntextured(partName, poseStack, buffer, red, green, blue, alpha, false);
    }

    public void renderPartUntextured(String partName, PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, boolean additive) {
        renderPartUntextured(partName, poseStack, buffer, red, green, blue, alpha,
                additive ? LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE : LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    public void renderPartUntextured(String partName, PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
        PreparedBatch batch = selectedPartBatch(partName);
        if (batch == null) {
            return;
        }
        renderPreparedBatchUntextured(batch, poseStack, buffer, red, green, blue, alpha, renderMode);
    }

    public void renderAll(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderAll(textureLocation, poseStack, buffer, packedLight, packedOverlay);
    }

    public void renderAll(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderAll(textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255);
    }

    public void renderAll(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha) {
        renderAll(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, false);
    }

    public void renderAll(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow) {
        renderAll(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    public void renderAll(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, LegacyTexturedRenderMode renderMode) {
        renderAll(textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255, false,
                renderMode, UvTransform.DEFAULT);
    }

    public void renderAll(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        renderAll(textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255, false,
                renderMode, UvTransform.DEFAULT);
    }

    public void renderAll(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean translucent, float uOffset, float vOffset) {
        renderAll(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                renderMode(translucent), uvTransform(uOffset, vOffset));
    }

    public void renderAll(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
        PreparedBatch batch = allPreparedBatchForRender(true);
        if (batch == null) {
            return;
        }
        renderPreparedBatch(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, smoothing, renderMode, uvTransform);
    }

    public boolean prepareTexturedAllSequence(TexturedPreparedSequence sequence, ResourceLocation textureLocation,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
            RenderBackendFallbackReason fallbackReason) {
        if (sequence == null) {
            throw new IllegalArgumentException("sequence");
        }
        PreparedBatch batch = allPreparedBatchForRender(true);
        if (batch == null || batch.empty()) {
            sequence.clear();
            return false;
        }
        return RENDER_BACKEND.prepareTexturedCpuSequence(sequence, batch, textureLocation, buffer, packedLight,
                packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform,
                fallbackReason);
    }

    public void renderAllUntextured(PoseStack poseStack, MultiBufferSource buffer, int red, int green, int blue, int alpha) {
        renderAllUntextured(poseStack, buffer, red, green, blue, alpha, false);
    }

    public void renderAllUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, boolean additive) {
        renderAllUntextured(poseStack, buffer, red, green, blue, alpha,
                additive ? LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE : LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    private void renderAllUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
        PreparedBatch batch = allPreparedBatchForRender(true);
        if (batch == null) {
            return;
        }
        renderPreparedBatchUntextured(batch, poseStack, buffer, red, green, blue, alpha, renderMode);
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
        LegacyTexturedRenderMode resolvedRenderMode = renderModeForPose(renderMode, poseStack.last().pose());
        RENDER_BACKEND.renderUntexturedVertexColorTransient(quad, poseStack, buffer, resolvedRenderMode,
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
        LegacyTexturedRenderMode resolvedRenderMode = renderModeForPose(renderMode, poseStack.last().pose());
        RENDER_BACKEND.renderUntexturedVertexColorTransientTriangle(triangle, poseStack, buffer, resolvedRenderMode,
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
        LegacyTexturedRenderMode resolvedRenderMode = renderModeForPose(renderMode, poseStack.last().pose());
        RENDER_BACKEND.renderUntexturedVertexColorTransientTriangles(triangles, poseStack, buffer,
                resolvedRenderMode,
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
        LegacyTexturedRenderMode resolvedRenderMode = renderModeForPose(renderMode, poseStack.last().pose());
        RENDER_BACKEND.renderUntexturedLineTransient(lines, poseStack, buffer, resolvedRenderMode, lineWidth,
                RenderBackendFallbackReason.UNTEXTURED_DIRECT_LINES);
        return true;
    }

    public static boolean renderDoubleSidedUntexturedVertexColorTransientQuad(PoseStack poseStack,
            MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
            double x0, double y0, double z0, int color0, int alpha0,
            double x1, double y1, double z1, int color1, int alpha1,
            double x2, double y2, double z2, int color2, int alpha2,
            double x3, double y3, double z3, int color3, int alpha3) {
        LegacyTexturedRenderMode resolvedRenderMode = renderModeForPose(renderMode, poseStack.last().pose());
        RENDER_BACKEND.renderUntexturedVertexColorTransient(new UntexturedVertexColorTransientQuad(
                vertexColor(x0, y0, z0, color0, alpha0),
                vertexColor(x1, y1, z1, color1, alpha1),
                vertexColor(x2, y2, z2, color2, alpha2),
                vertexColor(x3, y3, z3, color3, alpha3)), poseStack, buffer, resolvedRenderMode,
                RenderBackendFallbackReason.UNTEXTURED_DIRECT_VERTEX_COLOR_QUAD);
        RENDER_BACKEND.renderUntexturedVertexColorTransient(new UntexturedVertexColorTransientQuad(
                vertexColor(x3, y3, z3, color3, alpha3),
                vertexColor(x2, y2, z2, color2, alpha2),
                vertexColor(x1, y1, z1, color1, alpha1),
                vertexColor(x0, y0, z0, color0, alpha0)), poseStack, buffer, resolvedRenderMode,
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

    public void renderOnly(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, String... groupNames) {
        renderOnly(textureLocation, poseStack, buffer, packedLight, packedOverlay, groupNames);
    }

    public void renderOnly(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, String... groupNames) {
        renderOnly(textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255, groupNames);
    }

    public void renderOnly(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, String... groupNames) {
        PreparedBatch batch = selectedBatchForRender(SelectionCacheMode.ONLY, true, true, groupNames);
        if (batch == null) {
            return;
        }
        renderPreparedBatch(batch, textureLocation, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, false, smoothing,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    public void renderOnlyUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, String... groupNames) {
        renderOnlyUntextured(poseStack, buffer, red, green, blue, alpha, false, groupNames);
    }

    public void renderOnlyUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, boolean additive, String... groupNames) {
        renderOnlyUntextured(poseStack, buffer, red, green, blue, alpha,
                additive ? LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE : LegacyTexturedRenderMode.CUTOUT_NO_CULL,
                groupNames);
    }

    public void renderOnlyUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode, String... groupNames) {
        PreparedBatch batch = selectedBatchForRender(SelectionCacheMode.ONLY, true, true, groupNames);
        if (batch == null) {
            return;
        }
        renderPreparedBatchUntextured(batch, poseStack, buffer, red, green, blue, alpha, renderMode);
    }

    public void renderOnlyUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, SelectionHandle selection) {
        renderSelectionUntextured(poseStack, buffer, red, green, blue, alpha,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, selection);
    }

    public void renderOnlyUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode, SelectionHandle selection) {
        renderSelectionUntextured(poseStack, buffer, red, green, blue, alpha, renderMode, selection);
    }

    public void renderOnlyUntexturedClipped(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode, SelectionHandle selection,
            double clipX, double clipY, double clipZ, double clipD) {
        renderSelectionUntexturedClipped(poseStack, buffer, red, green, blue, alpha, renderMode,
                selection, clipX, clipY, clipZ, clipD);
    }

    public void renderOnly(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, String... groupNames) {
        renderOnly(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT, groupNames);
    }

    public void renderOnly(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean translucent, float uOffset, float vOffset, String... groupNames) {
        renderOnly(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, renderMode(translucent), uvTransform(uOffset, vOffset), groupNames);
    }

    public void renderOnly(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform, String... groupNames) {
        PreparedBatch batch = selectedBatchForRender(SelectionCacheMode.ONLY, true, true, groupNames);
        if (batch == null) {
            return;
        }
        renderPreparedBatch(batch, textureLocation, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
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

    public void renderOnlyInCallOrder(PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, SelectionHandle selection) {
        renderOnlyInCallOrder(textureLocation, poseStack, buffer, packedLight, packedOverlay, selection);
    }

    public void renderOnlyInCallOrder(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, SelectionHandle selection) {
        renderSelection(textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255,
                false, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT, selection);
    }

    public void renderOnlyInCallOrder(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, SelectionHandle selection,
            LegacyTexturedRenderMode renderMode) {
        renderSelection(textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255,
                false, renderMode, UvTransform.DEFAULT, selection);
    }

    /**
     * Submits old fixed-function lightmap texture coordinates through the
     * shared prepared-OBJ CPU/static-shader path. It is not a packed-light API.
     */
    public void renderOnlyInCallOrderRawLightmap(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, LegacyRawLightmapCoordinates rawLightmap, int packedOverlay,
            SelectionHandle selection) {
        if (rawLightmap == null) {
            throw new IllegalArgumentException("rawLightmap");
        }
        renderSelection(textureLocation, poseStack, buffer, rawLightmap.vertexUv2(), packedOverlay,
                255, 255, 255, 255, false, LegacyTexturedRenderMode.RAW_LIGHTMAP_CUTOUT_CULL,
                UvTransform.DEFAULT, selection);
    }

    public void renderOnlyInCallOrder(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
            SelectionHandle selection) {
        renderSelection(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, renderMode, uvTransform, selection);
    }

    public void renderOnlyInCallOrder(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue,
            int alpha, boolean legacyShadow, SelectionHandle selection) {
        renderSelection(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT, selection);
    }

    public void renderOnlyInCallOrderWithSprite(TextureAtlasSprite sprite, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, SelectionHandle selection) {
        renderSelectionWithSprite(sprite, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255,
                false, true, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT, selection);
    }

    public void renderOnlyInCallOrder(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, String... groupNames) {
        renderOnlyInCallOrder(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT, groupNames);
    }

    public void renderOnlyInCallOrder(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean translucent, float uOffset, float vOffset, String... groupNames) {
        renderOnlyInCallOrder(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, renderMode(translucent), uvTransform(uOffset, vOffset), groupNames);
    }

    public void renderOnlyInCallOrder(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform, String... groupNames) {
        PreparedBatch batch = selectedBatchForRender(SelectionCacheMode.CALL_ORDER, true, true, groupNames);
        if (batch == null) {
            return;
        }
        renderPreparedBatch(batch, textureLocation, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
    }

    private void renderSelection(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform, SelectionHandle selection) {
        PreparedBatch batch = selectedBatch(selection);
        if (batch == null) {
            return;
        }
        renderPreparedBatch(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
        warnMissingNamedGroups(selection.rawNames());
    }

    private void renderSelectionWithSprite(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean partBrightness, LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
            SelectionHandle selection) {
        PreparedBatch batch = selectedBatch(selection);
        if (batch == null) {
            return;
        }
        renderPreparedBatchWithSprite(batch, sprite, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, legacyShadow, partBrightness, renderMode, uvTransform);
        warnMissingNamedGroups(selection.rawNames());
    }

    private void renderSelectionUntextured(PoseStack poseStack, MultiBufferSource buffer, int red, int green,
            int blue, int alpha, LegacyTexturedRenderMode renderMode, SelectionHandle selection) {
        PreparedBatch batch = selectedBatch(selection);
        if (batch == null) {
            return;
        }
        renderPreparedBatchUntextured(batch, poseStack, buffer, red, green, blue, alpha, renderMode);
        warnMissingNamedGroups(selection.rawNames());
    }

    private void renderSelectionUntexturedClipped(PoseStack poseStack, MultiBufferSource buffer, int red, int green,
            int blue, int alpha, LegacyTexturedRenderMode renderMode, SelectionHandle selection,
            double clipX, double clipY, double clipZ, double clipD) {
        List<Group> groups = selectedGroups(selection);
        if (groups == null) {
            return;
        }
        if (groups.isEmpty()) {
            warnMissingNamedGroups(selection.rawNames());
            return;
        }
        PreparedBatch batch = PreparedBatch.clippedFrom(groups, "clipped-selection", clipX, clipY, clipZ, clipD);
        if (batch.empty()) {
            warnMissingNamedGroups(selection.rawNames());
            return;
        }
        RENDER_BACKEND.renderUntexturedTransient(batch, poseStack, buffer, red, green, blue, alpha, renderMode,
                RenderBackendFallbackReason.UNTEXTURED_CLIPPED);
        warnMissingNamedGroups(selection.rawNames());
    }

    public void renderAllExcept(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, String... excludedGroupNames) {
        renderAllExcept(textureLocation, poseStack, buffer, packedLight, packedOverlay, excludedGroupNames);
    }

    public void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, String... excludedGroupNames) {
        renderAllExcept(textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255, excludedGroupNames);
    }

    public void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, String... excludedGroupNames) {
        PreparedBatch batch = selectedBatchForRender(SelectionCacheMode.ALL_EXCEPT, true, false, excludedGroupNames);
        if (batch == null) {
            return;
        }
        renderPreparedBatch(batch, textureLocation, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, false, smoothing,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    public void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, String... excludedGroupNames) {
        renderAllExcept(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT, excludedGroupNames);
    }

    public void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean translucent, float uOffset, float vOffset, String... excludedGroupNames) {
        renderAllExcept(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, renderMode(translucent), uvTransform(uOffset, vOffset), excludedGroupNames);
    }

    public void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, UvTransform uvTransform, String... excludedGroupNames) {
        PreparedBatch batch = selectedBatchForRender(SelectionCacheMode.ALL_EXCEPT, true, false, excludedGroupNames);
        if (batch == null) {
            return;
        }
        renderPreparedBatch(batch, textureLocation, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
    }

    public void renderAllExcept(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, SelectionHandle selection) {
        renderAllExcept(textureLocation, poseStack, buffer, packedLight, packedOverlay, selection);
    }

    public void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, SelectionHandle selection) {
        renderAllExcept(textureLocation, poseStack, buffer, packedLight, packedOverlay, selection,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    public void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, SelectionHandle selection,
            LegacyTexturedRenderMode renderMode) {
        renderAllExcept(textureLocation, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, false, renderMode, UvTransform.DEFAULT, selection);
    }

    public void renderAllExcept(ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
            SelectionHandle selection) {
        renderSelection(textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, renderMode, uvTransform, selection);
    }

    public void tessellateAll(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        PreparedBatch batch = allPreparedBatchForRender(false);
        if (batch == null) {
            return;
        }
        renderPreparedBatch(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, false, smoothing, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    public void tessellateOnly(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, String... groupNames) {
        PreparedBatch batch = selectedBatchForRender(SelectionCacheMode.ONLY, false, true, groupNames);
        if (batch == null) {
            return;
        }
        renderPreparedBatch(batch, textureLocation, poseStack, buffer,
                packedLight, packedOverlay, 255, 255, 255, 255, false, smoothing,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    public void tessellatePart(String partName, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        PreparedBatch batch = selectedPartBatch(partName);
        if (batch == null) {
            return;
        }
        renderPreparedBatch(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255,
                255, false, smoothing, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    public void tessellateAllExcept(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, String... excludedGroupNames) {
        PreparedBatch batch = selectedBatchForRender(SelectionCacheMode.ALL_EXCEPT, false, false, excludedGroupNames);
        if (batch == null) {
            return;
        }
        renderPreparedBatch(batch, textureLocation, poseStack, buffer,
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

    public static RenderBackendInstancingSnapshot renderBackendInstancingSnapshot() {
        return RENDER_BACKEND.instancingSnapshot();
    }

    public static RenderBackendIrisSnapshot renderBackendIrisSnapshot() {
        return RENDER_BACKEND.irisSnapshot();
    }

    public static void endRenderBackendFrame() {
        RENDER_BACKEND.endFrame();
        HbmRenderBackendDiagnostics.logEndFrameIfEnabled();
    }

    public static void clearRenderBackend(RenderBackendClearReason reason) {
        HbmRenderFrameLight.invalidateCaches();
        RENDER_BACKEND.clear(reason);
    }

    public static void invalidateIrisCompanionShaderAttributeCaches() {
        RENDER_BACKEND.invalidateIrisCompanionShaderAttributeCaches();
    }

    public static void flushRenderBackend(RenderBackendFlushStage stage) {
        RENDER_BACKEND.flush(stage);
    }

    public static void flushRenderBackend(RenderBackendFlushStage stage, Matrix4f projectionMatrix) {
        RENDER_BACKEND.flush(stage, projectionMatrix);
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
        if (batch == null || batch.empty()) {
            return;
        }
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
        LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, position).withAlpha(alpha);
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
        if (batch == null || batch.empty()) {
            return;
        }
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
        LegacyTexturedRenderMode resolvedRenderMode = renderModeForPose(renderMode, position);
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
        if (batch == null || batch.empty()) {
            return;
        }
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

    private synchronized void ensureLoaded() {
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
                normalizeOrZero(interpolate(start.normal(), end.normal(), clamped)));
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

    private static Vector3f normalizeOrZero(Vector3f value) {
        float lengthSquared = value.lengthSquared();
        if (!(lengthSquared >= 1.0E-8F) || !Float.isFinite(lengthSquared)) {
            // 1.7.10 Vec3#normalize keeps a degenerate normal directionless. The
            // legacy lighting shaders deliberately map zero to ambient 0.40.
            return new Vector3f();
        }
        return value.normalize();
    }

    private static int parseObjIndex(String value, int size) {
        int index = Integer.parseInt(value);
        return index > 0 ? index - 1 : size + index;
    }

    private static Vector3f calculateFaceNormal(List<Vector3f> vertices) {
        Vector3f edgeA = new Vector3f(vertices.get(1)).sub(vertices.get(0));
        Vector3f edgeB = new Vector3f(vertices.get(2)).sub(vertices.get(0));
        Vector3f calculated = edgeA.cross(edgeB);
        float lengthSquared = calculated.lengthSquared();
        if (!(lengthSquared >= 1.0E-8F) || !Float.isFinite(lengthSquared)) {
            // S_Face#calculateFaceNormal used Vec3#normalize; a degenerate face
            // therefore supplied zero, not an invented +Y normal.
            return new Vector3f();
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
        List<PreparedVertex> copied = List.copyOf(vertices);
        return PreparedBatch.of("direct-untextured-quads:" + quads.size(), copied, List.of());
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
        return PreparedBatch.of("direct-textured-quad", vertices, List.of());
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

    private synchronized PreparedBatch allPreparedBatch() {
        if (allPreparedBatch == null) {
            allPreparedBatch = PreparedBatch.from(groupOrder, stableAllBatchKey());
            CACHE_METRICS.recordAllPreparedBatchBuild();
        }
        return allPreparedBatch;
    }

    private synchronized List<Group> selectedGroups(SelectionCacheMode mode, String... names) {
        return selectedEntry(mode, names).groups();
    }

    private synchronized PreparedBatch selectedBatch(SelectionCacheMode mode, String... names) {
        return selectedEntry(mode, names).batch();
    }

    private synchronized PreparedBatch selectedBatch(SelectionHandle selection) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return null;
        }
        return selection.batch(this);
    }

    private synchronized List<Group> selectedGroups(SelectionHandle selection) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return null;
        }
        return selection.groups(this);
    }

    private synchronized PreparedBatch allPreparedBatchForRender(boolean rejectMixedMode) {
        if (rejectMixedMode) {
            rejectMixedModeDirectRender();
        }
        ensureLoaded();
        if (failed) {
            return null;
        }
        return allPreparedBatch();
    }

    private synchronized PreparedBatch selectedBatchForRender(SelectionCacheMode mode, boolean rejectMixedMode,
            boolean warnMissing, String... names) {
        if (rejectMixedMode) {
            rejectMixedModeDirectRender();
        }
        ensureLoaded();
        if (failed) {
            return null;
        }
        PreparedBatch batch = selectedBatch(mode, names);
        if (warnMissing) {
            warnMissingNamedGroups(names);
        }
        return batch;
    }

    private synchronized PreparedBatch selectedPartBatch(String partName) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return null;
        }
        List<Group> groups = groupsByName.get(normalize(partName));
        boolean rendered = groups != null && !groups.isEmpty();
        if (!rendered) {
            warnMissingPart(partName);
            return null;
        }
        return selectedBatch(SelectionCacheMode.ONLY, partName);
    }

    private synchronized PreparedBatch selectedLastGroupBatch(String partName) {
        ensureLoaded();
        if (failed) {
            return null;
        }
        List<Group> groups = groupsByName.get(normalize(partName));
        boolean rendered = groups != null && !groups.isEmpty();
        if (!rendered) {
            warnMissingPart(partName);
            return null;
        }
        return selectedBatch(SelectionCacheMode.LAST_GROUP, partName);
    }

    private synchronized PreparedBatch clippedPartBatch(String partName, double clipX, double clipY, double clipZ,
            double clipD) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return null;
        }
        List<Group> groups = groupsByName.get(normalize(partName));
        boolean rendered = groups != null && !groups.isEmpty();
        if (!rendered) {
            warnMissingPart(partName);
            return null;
        }
        return PreparedBatch.clippedFrom(groups, "clipped:" + normalize(partName), clipX, clipY, clipZ, clipD);
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

    private static LegacyTexturedRenderMode renderModeForPose(LegacyTexturedRenderMode renderMode,
            Matrix4f position) {
        // PoseStack transforms normals with the cofactor matrix for reflected,
        // non-uniform scales. Its determinant can therefore stay positive for
        // a one-axis mirror even though the vertex winding is reversed. Culling
        // must follow the actual position transform instead.
        float determinant = position.m00() * (position.m11() * position.m22() - position.m12() * position.m21())
                - position.m01() * (position.m10() * position.m22() - position.m12() * position.m20())
                + position.m02() * (position.m10() * position.m21() - position.m11() * position.m20());
        if (determinant >= 0.0F) {
            return renderMode;
        }
        return switch (renderMode) {
            case CUTOUT_CULL -> LegacyTexturedRenderMode.CUTOUT_REVERSED_CULL;
            case CUTOUT_REVERSED_CULL -> LegacyTexturedRenderMode.CUTOUT_CULL;
            case RAW_LIGHTMAP_CUTOUT_CULL -> LegacyTexturedRenderMode.RAW_LIGHTMAP_CUTOUT_REVERSED_CULL;
            case RAW_LIGHTMAP_CUTOUT_REVERSED_CULL -> LegacyTexturedRenderMode.RAW_LIGHTMAP_CUTOUT_CULL;
            case ADDITIVE_CULL_NO_DEPTH_WRITE -> LegacyTexturedRenderMode.ADDITIVE_REVERSED_CULL_NO_DEPTH_WRITE;
            case ADDITIVE_REVERSED_CULL_NO_DEPTH_WRITE -> LegacyTexturedRenderMode.ADDITIVE_CULL_NO_DEPTH_WRITE;
            case TRANSLUCENT_CULL_DEPTH_WRITE -> LegacyTexturedRenderMode.TRANSLUCENT_REVERSED_CULL_DEPTH_WRITE;
            case TRANSLUCENT_REVERSED_CULL_DEPTH_WRITE -> LegacyTexturedRenderMode.TRANSLUCENT_CULL_DEPTH_WRITE;
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
        float radians = rotationDegrees * Mth.DEG_TO_RAD;
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

    private synchronized void warnMissingParts(Set<String> requested, Set<String> rendered) {
        for (String key : requested) {
            if (!rendered.contains(key)) {
                warnMissingPart(key);
            }
        }
    }

    private synchronized void warnMissingNamedGroups(String... names) {
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

    private synchronized void warnMissingPart(String partName) {
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
            String lastFallbackDetail,
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

    public record RenderBackendInstancingSnapshot(
            long optimizedFlushCalls,
            long currentFrameOptimizedFlushCalls,
            long lastFrameOptimizedFlushCalls,
            long optimizedDuplicateFlushCalls,
            long currentFrameOptimizedDuplicateFlushCalls,
            long lastFrameOptimizedDuplicateFlushCalls,
            long optimizedDuplicatePresentSkips,
            long currentFrameOptimizedDuplicatePresentSkips,
            long lastFrameOptimizedDuplicatePresentSkips,
            long optimizedFlushNanos,
            long currentFrameOptimizedFlushNanos,
            long lastFrameOptimizedFlushNanos,
            long optimizedDrawStateRestoreFailures,
            long currentFrameOptimizedDrawStateRestoreFailures,
            long lastFrameOptimizedDrawStateRestoreFailures,
            long duplicateInstances,
            long currentFrameDuplicateInstances,
            long lastFrameDuplicateInstances,
            long staleInstancedBatches,
            long staleInstancedInstances,
            long staleIrisCompanionBatches,
            long staleIrisCompanionInstances,
            long currentFrameStaleInstancedBatches,
            long currentFrameStaleInstancedInstances,
            long currentFrameStaleIrisCompanionBatches,
            long currentFrameStaleIrisCompanionInstances,
            long lastFrameStaleInstancedBatches,
            long lastFrameStaleInstancedInstances,
            long lastFrameStaleIrisCompanionBatches,
            long lastFrameStaleIrisCompanionInstances,
            long mdiPartialDrawFailures,
            long currentFrameMdiPartialDrawFailures,
            long lastFrameMdiPartialDrawFailures,
            long mdiStalePreparedGroups,
            long currentFrameMdiStalePreparedGroups,
            long lastFrameMdiStalePreparedGroups,
            long mdiStalePreparedCommands,
            long currentFrameMdiStalePreparedCommands,
            long lastFrameMdiStalePreparedCommands,
            boolean mdiDispatchDisabled,
            long mdiDispatchDisableEvents,
            long currentFrameMdiDispatchDisableEvents,
            long lastFrameMdiDispatchDisableEvents,
            long currentFrameMdiMultiDrawCalls,
            long lastFrameMdiMultiDrawCalls,
            long mdiAtlasRepackFailures,
            long currentFrameMdiAtlasRepackFailures,
            long lastFrameMdiAtlasRepackFailures,
            long mdiAtlasInitFailures,
            long currentFrameMdiAtlasInitFailures,
            long lastFrameMdiAtlasInitFailures) {

        public static final RenderBackendInstancingSnapshot EMPTY =
                new RenderBackendInstancingSnapshot(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                        0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                        0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, false, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                        0L);
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
            long lightmapStorageFailures,
            long currentFrameLightmapStorageFailures,
            long lastFrameLightmapStorageFailures,
            long lightmapSlotReuses,
            long currentFrameLightmapSlotReuses,
            long lastFrameLightmapSlotReuses,
            long lightmapSlotUploads,
            long currentFrameLightmapSlotUploads,
            long lastFrameLightmapSlotUploads,
            long lightmapStagingFallbacks,
            long currentFrameLightmapStagingFallbacks,
            long lastFrameLightmapStagingFallbacks,
            IrisCompanionShaderSnapshot shaderAttributes,
            HbmIrisRenderBatch.Snapshot persistentBatch,
            IrisCompanionQueueSnapshot queuedFlush) {
        public static final RenderBackendIrisSnapshot EMPTY = new RenderBackendIrisSnapshot(
                0, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L,
                0L, 0L, 0L,
                0L, 0L, 0L,
                0L, 0L, 0L,
                0L, 0L, 0L,
                0L, 0L, 0L,
                IrisCompanionShaderSnapshot.EMPTY,
                HbmIrisRenderBatch.Snapshot.EMPTY,
                IrisCompanionQueueSnapshot.EMPTY);
    }

    public record IrisCompanionShaderSnapshot(
            long cacheHits,
            long currentFrameCacheHits,
            long lastFrameCacheHits,
            long cacheMisses,
            long currentFrameCacheMisses,
            long lastFrameCacheMisses,
            long generationInvalidations,
            long currentFrameGenerationInvalidations,
            long lastFrameGenerationInvalidations,
            long primedAttributeSkips,
            long currentFramePrimedAttributeSkips,
            long lastFramePrimedAttributeSkips,
            long vaoBindFailures,
            long currentFrameVaoBindFailures,
            long lastFrameVaoBindFailures) {
        public static final IrisCompanionShaderSnapshot EMPTY = new IrisCompanionShaderSnapshot(
                0L, 0L, 0L,
                0L, 0L, 0L,
                0L, 0L, 0L,
                0L, 0L, 0L,
                0L, 0L, 0L);
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
            long duplicateInstances,
            long currentFrameDuplicateInstances,
            long lastFrameDuplicateInstances,
            long lastFrameQueuedBatches,
            long lastFrameQueuedInstances,
            long lastFrameFlushes,
            long lastFrameDrawCalls,
            long lastFrameFallbackBatches,
            long lastFrameFallbackInstances) {
        public static final IrisCompanionQueueSnapshot EMPTY = new IrisCompanionQueueSnapshot(
                0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L,
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

    public static final class TexturedPreparedSequence {
        private ExperimentalGpuPreparedRenderBackend optimizedBackend;
        private CpuPreparedRenderBackend cpuBackend;
        private PreparedBatch batch;
        private ResourceLocation textureLocation;
        private MultiBufferSource buffer;
        private int packedLight;
        private int packedOverlay;
        private int red;
        private int green;
        private int blue;
        private int alpha;
        private boolean legacyShadow;
        private boolean smoothing;
        private LegacyTexturedRenderMode renderMode;
        private UvTransform uvTransform;
        private RenderBackendFallbackReason fallbackReason = RenderBackendFallbackReason.NONE;
        private LegacyTexturedRenderMode cachedAlphaMode;
        private VertexConsumer quadConsumer;
        private VertexConsumer triangleConsumer;

        public void render(PoseStack poseStack) {
            if (batch == null || batch.empty()) {
                return;
            }
            PoseStack.Pose pose = poseStack.last();
            Matrix4f position = pose.pose();
            Matrix3f normal = pose.normal();
            LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, position).withAlpha(alpha);
            if (alphaMode != cachedAlphaMode) {
                cachedAlphaMode = alphaMode;
                quadConsumer = null;
                triangleConsumer = null;
            }
            if (optimizedBackend != null && fallbackReason != RenderBackendFallbackReason.NONE) {
                optimizedBackend.recordGpuFallback(fallbackReason, batch.vertexCount(),
                        ExperimentalGpuPreparedRenderBackend.fallbackDetail("gpu-textured-sequence",
                                fallbackReason, batch, textureLocation, renderMode, 1));
            } else {
                cpuBackend.recordCpuFallback(fallbackReason, batch.vertexCount());
            }
            cpuBackend.recordTexturedBatch(batch, textureLocation, alphaMode);
            List<PreparedVertex> quadVertices = batch.quadVertices();
            if (!quadVertices.isEmpty()) {
                cpuBackend.recordTexturedDrawCall();
                if (quadConsumer == null) {
                    quadConsumer = buffer.getBuffer(alphaMode.renderType(textureLocation, VertexFormat.Mode.QUADS));
                }
                emitPreparedVertices(quadVertices, quadConsumer, position, normal, packedLight, packedOverlay,
                        red, green, blue, alpha, legacyShadow, smoothing, uvTransform);
            }
            List<PreparedVertex> triangleVertices = batch.triangleVertices();
            if (!triangleVertices.isEmpty()) {
                cpuBackend.recordTexturedDrawCall();
                if (triangleConsumer == null) {
                    triangleConsumer = buffer.getBuffer(alphaMode.renderType(textureLocation,
                            VertexFormat.Mode.TRIANGLES));
                }
                emitPreparedVertices(triangleVertices, triangleConsumer, position, normal, packedLight,
                        packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, uvTransform);
            }
        }

        public void clear() {
            optimizedBackend = null;
            cpuBackend = null;
            batch = null;
            textureLocation = null;
            buffer = null;
            renderMode = null;
            uvTransform = null;
            fallbackReason = RenderBackendFallbackReason.NONE;
            cachedAlphaMode = null;
            quadConsumer = null;
            triangleConsumer = null;
        }

        private void prepare(ExperimentalGpuPreparedRenderBackend optimizedBackend,
                CpuPreparedRenderBackend cpuBackend, PreparedBatch batch, ResourceLocation textureLocation,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue,
                int alpha, boolean legacyShadow, boolean smoothing, LegacyTexturedRenderMode renderMode,
                UvTransform uvTransform, RenderBackendFallbackReason fallbackReason) {
            this.optimizedBackend = optimizedBackend;
            this.cpuBackend = cpuBackend;
            this.batch = batch;
            this.textureLocation = textureLocation;
            this.buffer = buffer;
            this.packedLight = packedLight;
            this.packedOverlay = packedOverlay;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            this.legacyShadow = legacyShadow;
            this.smoothing = smoothing;
            this.renderMode = renderMode;
            this.uvTransform = uvTransform;
            this.fallbackReason = fallbackReason == null ? RenderBackendFallbackReason.NONE : fallbackReason;
            this.cachedAlphaMode = null;
            this.quadConsumer = null;
            this.triangleConsumer = null;
        }
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
        GPU_RAW_LIGHTMAP_COORDINATES,
        GPU_LEGACY_SHADOW,
        GPU_SPRITE_UNSUPPORTED,
        GPU_UNTEXTURED_UNSUPPORTED,
        GPU_UPLOAD_FAILED,
        GPU_DRAW_FAILED,
        IRIS_SHADER_UNAVAILABLE,
        IRIS_COMPANION_UNSUPPORTED,
        IRIS_COMPANION_UPLOAD_FAILED,
        IRIS_COMPANION_DRAW_FAILED,
        MDI_UNAVAILABLE,
        MDI_NO_SLOT,
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
        private long groupPreparedBuilds;
        private long groupPreparedVertices;
        private long allPreparedBatchBuilds;
        private long selectionCacheHits;
        private long selectionCacheMisses;
        private long selectionCacheClears;
        private long selectionCacheEmptyBuilds;
        private long selectionCachePreparedBatchBuilds;
        private long selectionHandleRefreshes;
        private long selectionHandleEmptyBuilds;
        private long selectionHandlePreparedBatchBuilds;

        private void recordGroupPrepared(int vertices) {
            groupPreparedBuilds++;
            groupPreparedVertices += vertices;
        }

        private void recordAllPreparedBatchBuild() {
            allPreparedBatchBuilds++;
        }

        private void recordSelectionCacheHit() {
            selectionCacheHits++;
        }

        private void recordSelectionCacheMiss() {
            selectionCacheMisses++;
        }

        private void recordSelectionCacheClear() {
            selectionCacheClears++;
        }

        private void recordSelectionCacheEmptyBuild() {
            selectionCacheEmptyBuilds++;
        }

        private void recordSelectionCachePreparedBatchBuild() {
            selectionCachePreparedBatchBuilds++;
        }

        private void recordSelectionHandleRefresh() {
            selectionHandleRefreshes++;
        }

        private void recordSelectionHandleEmptyBuild() {
            selectionHandleEmptyBuilds++;
        }

        private void recordSelectionHandlePreparedBatchBuild() {
            selectionHandlePreparedBatchBuilds++;
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

        default boolean prepareTexturedCpuSequence(TexturedPreparedSequence sequence, PreparedBatch batch,
                ResourceLocation textureLocation, MultiBufferSource buffer, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
                RenderBackendFallbackReason fallbackReason) {
            sequence.clear();
            return false;
        }

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

        default void flush(RenderBackendFlushStage stage, Matrix4f projectionMatrix) {
            flush(stage);
        }

        default void recordCpuFallback(RenderBackendFallbackReason fallback, int vertices) {
        }

        default void endFrame() {
        }

        default void invalidateIrisCompanionShaderAttributeCaches() {
        }

        RenderBackendSnapshot snapshot();

        default RenderBackendAdditiveSnapshot additiveSnapshot() {
            return RenderBackendAdditiveSnapshot.EMPTY;
        }

        default RenderBackendInstancingSnapshot instancingSnapshot() {
            return RenderBackendInstancingSnapshot.EMPTY;
        }

        default RenderBackendIrisSnapshot irisSnapshot() {
            return RenderBackendIrisSnapshot.EMPTY;
        }
    }

    private static final class ExperimentalGpuPreparedRenderBackend implements RenderBackend {
        private static final int MAX_GPU_MESHES = 512;
        private static final int INSTANCED_VERTEX_STRIDE_BYTES = 44;
        private static final int[] NO_LIGHTMAP_SLOTS = new int[0];
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
        private final MdiDrawArraysAtlas mdiAtlas = new MdiDrawArraysAtlas();
        private final Map<InstancedMeshKey, InstancedMesh> instancedMeshes = Collections.synchronizedMap(
                new LinkedHashMap<InstancedMeshKey, InstancedMesh>(MAX_GPU_MESHES + 1, 0.75F, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<InstancedMeshKey, InstancedMesh> eldest) {
                        if (size() <= MAX_GPU_MESHES) {
                            return false;
                        }
                        closeInstancedAndEvictMdiLater(eldest.getValue());
                        return true;
                    }
                });
        private final Set<InstancedMeshKey> failedInstancedKeys = ConcurrentHashMap.newKeySet();
        private final Map<InstancedBatchKey, InstancedBatch> pendingInstancedBatches = new LinkedHashMap<>();
        private final Map<IrisCompanionQueueKey, IrisCompanionQueuedBatch> pendingIrisCompanionBatches =
                new LinkedHashMap<>();
        private final List<InstancedBatch> instancedFlushScratch = new ArrayList<>();
        private final List<IrisCompanionQueuedBatch> irisCompanionFlushScratch = new ArrayList<>();
        private final List<IrisCompanionQueuedDraw> irisNormalAlphaDrawScratch = new ArrayList<>();
        private final List<MdiPreparedGroup> mdiPreparedGroupScratch = new ArrayList<>();
        private final List<InstancedBatch> mdiPreparedBatchScratch = new ArrayList<>();
        private final List<InstancedBatch> mdiNoSlotBatchScratch = new ArrayList<>();
        private final List<InstancedBatch> mdiEligibleBatchScratch = new ArrayList<>();
        private final Map<MdiDrawGroupKey, MdiDrawGroup> mdiDrawGroupScratch = new LinkedHashMap<>();
        private final List<MdiDrawGroup> mdiDrawGroupPool = new ArrayList<>();
        private final Set<InstancedInstance> instancedDuplicateScratch = new HashSet<>();
        private final Set<IrisCompanionQueuedInstance> irisCompanionDuplicateScratch = new HashSet<>();
        private final Set<InstancedBatch> mdiDrawnBatchScratch = new HashSet<>();
        private PreparedBatch lastInstancedMeshBatch;
        private VertexFormat.Mode lastInstancedMeshSourceMode;
        private boolean lastInstancedMeshSmoothing;
        private GpuMeshKind lastInstancedMeshKind;
        private TextureAtlasSprite lastInstancedMeshSprite;
        private List<PreparedVertex> lastInstancedMeshVertices;
        private InstancedMesh lastInstancedMesh;
        private InstancedBatch lastPendingInstancedBatch;
        private InstancedMesh lastPendingInstancedMesh;
        private ResourceLocation lastPendingInstancedTexture;
        private LegacyTexturedRenderMode lastPendingInstancedRenderMode;
        private IrisCompanionQueuedBatch lastPendingIrisCompanionBatch;
        private GpuMeshKind lastPendingIrisCompanionKind;
        private String lastPendingIrisCompanionStablePartKey;
        private PreparedGeometryKey lastPendingIrisCompanionGeometryKey;
        private TextureAtlasSprite lastPendingIrisCompanionSprite;
        private int lastPendingIrisCompanionSourceVertices;
        private VertexFormat.Mode lastPendingIrisCompanionSourceMode;
        private boolean lastPendingIrisCompanionSmoothing;
        private UvTransform lastPendingIrisCompanionUvTransform;
        private ResourceLocation lastPendingIrisCompanionTexture;
        private LegacyTexturedRenderMode lastPendingIrisCompanionRenderMode;
        private int mdiDrawGroupPoolCursor;
        private int[] irisQueuedLightmapSlotScratch = NO_LIGHTMAP_SLOTS;
        private ByteBuffer instancedUploadScratch;
        private final AtomicLong gpuBufferBytes = new AtomicLong();
        private final AtomicLong gpuUploadAttempts = new AtomicLong();
        private final AtomicLong gpuUploadFailures = new AtomicLong();
        private long gpuDrawCalls;
        private long gpuFallbackBatches;
        private long gpuFallbackVertices;
        private final AtomicLong irisMeshBytes = new AtomicLong();
        private long irisEligibleBatches;
        private long irisDrawCalls;
        private long irisShadowDrawCalls;
        private long irisFallbackBatches;
        private long irisFallbackVertices;
        private final AtomicLong irisUploadAttempts = new AtomicLong();
        private final AtomicLong irisUploadFailures = new AtomicLong();
        private long irisLightmapStorageFailures;
        private long irisLightmapSlotReuses;
        private long irisLightmapSlotUploads;
        private long irisLightmapStagingFallbacks;
        private long irisShaderAttributeCacheHits;
        private long irisShaderAttributeCacheMisses;
        private long irisShaderAttributeGenerationInvalidations;
        private long irisShaderAttributePrimedSkips;
        private long irisShaderAttributeVaoBindFailures;
        private long currentFrameIrisEligibleBatches;
        private long currentFrameIrisDrawCalls;
        private long currentFrameIrisShadowDrawCalls;
        private long currentFrameIrisFallbackBatches;
        private long currentFrameIrisFallbackVertices;
        private long currentFrameIrisLightmapStorageFailures;
        private long currentFrameIrisLightmapSlotReuses;
        private long currentFrameIrisLightmapSlotUploads;
        private long currentFrameIrisLightmapStagingFallbacks;
        private long currentFrameIrisShaderAttributeCacheHits;
        private long currentFrameIrisShaderAttributeCacheMisses;
        private long currentFrameIrisShaderAttributeGenerationInvalidations;
        private long currentFrameIrisShaderAttributePrimedSkips;
        private long currentFrameIrisShaderAttributeVaoBindFailures;
        private long lastFrameIrisEligibleBatches;
        private long lastFrameIrisDrawCalls;
        private long lastFrameIrisShadowDrawCalls;
        private long lastFrameIrisFallbackBatches;
        private long lastFrameIrisFallbackVertices;
        private long lastFrameIrisLightmapStorageFailures;
        private long lastFrameIrisLightmapSlotReuses;
        private long lastFrameIrisLightmapSlotUploads;
        private long lastFrameIrisLightmapStagingFallbacks;
        private long lastFrameIrisShaderAttributeCacheHits;
        private long lastFrameIrisShaderAttributeCacheMisses;
        private long lastFrameIrisShaderAttributeGenerationInvalidations;
        private long lastFrameIrisShaderAttributePrimedSkips;
        private long lastFrameIrisShaderAttributeVaoBindFailures;
        private long irisQueuedBatches;
        private long irisQueuedInstances;
        private long irisQueuedFlushes;
        private long irisQueuedDrawCalls;
        private long irisQueuedFallbackBatches;
        private long irisQueuedFallbackInstances;
        private long irisQueuedDuplicateInstances;
        private long currentFrameIrisQueuedBatches;
        private long currentFrameIrisQueuedInstances;
        private long currentFrameIrisQueuedFlushes;
        private long currentFrameIrisQueuedDrawCalls;
        private long currentFrameIrisQueuedFallbackBatches;
        private long currentFrameIrisQueuedFallbackInstances;
        private long currentFrameIrisQueuedDuplicateInstances;
        private long lastFrameIrisQueuedBatches;
        private long lastFrameIrisQueuedInstances;
        private long lastFrameIrisQueuedFlushes;
        private long lastFrameIrisQueuedDrawCalls;
        private long lastFrameIrisQueuedFallbackBatches;
        private long lastFrameIrisQueuedFallbackInstances;
        private long lastFrameIrisQueuedDuplicateInstances;
        private long instancedQueuedBatches;
        private long instancedQueuedInstances;
        private long instancedFlushes;
        private long instancedDrawCalls;
        private long instancedFallbackBatches;
        private long instancedFallbackInstances;
        private long instancedOverflowBatches;
        private long instancedOverflowInstances;
        private long instancedDuplicateInstances;
        private long optimizedFlushCalls;
        private long optimizedDuplicateFlushCalls;
        private long optimizedDuplicatePresentSkips;
        private long optimizedFlushNanos;
        private long optimizedDrawStateRestoreFailures;
        private long staleInstancedBatches;
        private long staleInstancedInstances;
        private long staleIrisCompanionBatches;
        private long staleIrisCompanionInstances;
        private long instancedAdditiveQueuedBatches;
        private long instancedAdditiveQueuedInstances;
        private long instancedAdditiveDrawCalls;
        private long instancedAdditiveFallbackBatches;
        private long instancedAdditiveFallbackInstances;
        private long instancedAdditiveOverflowBatches;
        private long instancedAdditiveOverflowInstances;
        private long mdiEligibleFlushes;
        private long mdiEligibleBatches;
        private long mdiFallbackFlushes;
        private long mdiFallbackBatches;
        private long mdiDrawCalls;
        private long mdiMultiDrawCalls;
        private long mdiIndirectCommands;
        private long mdiNoSlotBatches;
        private long mdiNoSlotInstances;
        private long mdiPartialDrawFailures;
        private long mdiStalePreparedGroups;
        private long mdiStalePreparedCommands;
        private long mdiDispatchDisableEvents;
        private long mdiAtlasRepackFailures;
        private long mdiAtlasInitFailures;
        private long mdiAdditiveEligibleBatches;
        private long mdiAdditiveFallbackBatches;
        private long mdiAdditiveDrawCalls;
        private long mdiAdditiveIndirectCommands;
        private long mdiAdditiveNoSlotBatches;
        private long mdiAdditiveNoSlotInstances;
        private long currentFrameMdiEligibleFlushes;
        private long currentFrameMdiEligibleBatches;
        private long currentFrameMdiFallbackFlushes;
        private long currentFrameMdiFallbackBatches;
        private long currentFrameMdiDrawCalls;
        private long currentFrameMdiMultiDrawCalls;
        private long currentFrameMdiIndirectCommands;
        private long currentFrameMdiNoSlotBatches;
        private long currentFrameMdiNoSlotInstances;
        private long currentFrameMdiPartialDrawFailures;
        private long currentFrameMdiStalePreparedGroups;
        private long currentFrameMdiStalePreparedCommands;
        private long currentFrameMdiDispatchDisableEvents;
        private long currentFrameMdiAtlasRepackFailures;
        private long currentFrameMdiAtlasInitFailures;
        private long currentFrameOptimizedDrawStateRestoreFailures;
        private long currentFrameMdiAdditiveEligibleBatches;
        private long currentFrameMdiAdditiveFallbackBatches;
        private long currentFrameMdiAdditiveDrawCalls;
        private long currentFrameMdiAdditiveIndirectCommands;
        private long currentFrameMdiAdditiveNoSlotBatches;
        private long currentFrameMdiAdditiveNoSlotInstances;
        private long lastFrameMdiEligibleFlushes;
        private long lastFrameMdiEligibleBatches;
        private long lastFrameMdiFallbackFlushes;
        private long lastFrameMdiFallbackBatches;
        private long lastFrameMdiDrawCalls;
        private long lastFrameMdiMultiDrawCalls;
        private long lastFrameMdiIndirectCommands;
        private long lastFrameMdiNoSlotBatches;
        private long lastFrameMdiNoSlotInstances;
        private long lastFrameMdiPartialDrawFailures;
        private long lastFrameMdiStalePreparedGroups;
        private long lastFrameMdiStalePreparedCommands;
        private long lastFrameMdiDispatchDisableEvents;
        private long lastFrameMdiAtlasRepackFailures;
        private long lastFrameMdiAtlasInitFailures;
        private long lastFrameOptimizedDrawStateRestoreFailures;
        private long lastFrameMdiAdditiveEligibleBatches;
        private long lastFrameMdiAdditiveFallbackBatches;
        private long lastFrameMdiAdditiveDrawCalls;
        private long lastFrameMdiAdditiveIndirectCommands;
        private long lastFrameMdiAdditiveNoSlotBatches;
        private long lastFrameMdiAdditiveNoSlotInstances;
        private long currentFrameInstancedQueuedBatches;
        private long currentFrameInstancedQueuedInstances;
        private long currentFrameInstancedDrawCalls;
        private long currentFrameInstancedFallbackBatches;
        private long currentFrameInstancedFallbackInstances;
        private long currentFrameInstancedOverflowBatches;
        private long currentFrameInstancedOverflowInstances;
        private long currentFrameInstancedDuplicateInstances;
        private long currentFrameOptimizedFlushCalls;
        private long currentFrameOptimizedDuplicateFlushCalls;
        private long currentFrameOptimizedDuplicatePresentSkips;
        private long currentFrameOptimizedAfterBlockEntityPresents;
        private long currentFrameOptimizedFlushNanos;
        private long currentFrameStaleInstancedBatches;
        private long currentFrameStaleInstancedInstances;
        private long currentFrameStaleIrisCompanionBatches;
        private long currentFrameStaleIrisCompanionInstances;
        private long currentFrameInstancedAdditiveQueuedBatches;
        private long currentFrameInstancedAdditiveQueuedInstances;
        private long currentFrameInstancedAdditiveDrawCalls;
        private long currentFrameInstancedAdditiveFallbackBatches;
        private long currentFrameInstancedAdditiveFallbackInstances;
        private long currentFrameInstancedAdditiveOverflowBatches;
        private long currentFrameInstancedAdditiveOverflowInstances;
        private long lastFrameInstancedQueuedBatches;
        private long lastFrameInstancedQueuedInstances;
        private long lastFrameInstancedDrawCalls;
        private long lastFrameInstancedFallbackBatches;
        private long lastFrameInstancedFallbackInstances;
        private long lastFrameInstancedOverflowBatches;
        private long lastFrameInstancedOverflowInstances;
        private long lastFrameInstancedDuplicateInstances;
        private long lastFrameOptimizedFlushCalls;
        private long lastFrameOptimizedDuplicateFlushCalls;
        private long lastFrameOptimizedDuplicatePresentSkips;
        private long lastFrameOptimizedFlushNanos;
        private long lastFrameStaleInstancedBatches;
        private long lastFrameStaleInstancedInstances;
        private long lastFrameStaleIrisCompanionBatches;
        private long lastFrameStaleIrisCompanionInstances;
        private long lastFrameInstancedAdditiveQueuedBatches;
        private long lastFrameInstancedAdditiveQueuedInstances;
        private long lastFrameInstancedAdditiveDrawCalls;
        private long lastFrameInstancedAdditiveFallbackBatches;
        private long lastFrameInstancedAdditiveFallbackInstances;
        private long lastFrameInstancedAdditiveOverflowBatches;
        private long lastFrameInstancedAdditiveOverflowInstances;
        private long currentFrameGpuDrawCalls;
        private long currentFrameGpuFallbackBatches;
        private long currentFrameGpuFallbackVertices;
        private long lastFrameGpuDrawCalls;
        private long lastFrameGpuFallbackBatches;
        private long lastFrameGpuFallbackVertices;
        private volatile RenderBackendFallbackReason lastGpuFallbackReason = RenderBackendFallbackReason.NONE;
        private volatile String lastGpuFallbackDetail = NO_FALLBACK_DETAIL;
        private volatile boolean mdiCapsResolved;
        private volatile boolean mdiDrawIndirectSupported;
        private volatile boolean mdiMultiDrawIndirectSupported;
        private volatile boolean mdiBaseInstanceSupported;
        private volatile boolean mdiCoreDrawArraysIndirectSupported;
        private volatile boolean mdiArbDrawArraysIndirectSupported;
        private volatile boolean mdiCoreMultiDrawArraysIndirectSupported;
        private volatile boolean mdiArbMultiDrawArraysIndirectSupported;
        private volatile boolean mdiCommandMemoryBarrierSupported;
        private volatile boolean mdiDispatchDisabled;
        private ShaderInstance optimizedUniformShader;
        private int optimizedUniformProgram = -1;
        private long optimizedUniformPipelineGeneration = -1L;
        private Uniform optimizedProjMatUniform;
        private Uniform optimizedModelViewUniform;
        private Uniform optimizedFogStartUniform;
        private Uniform optimizedFogEndUniform;
        private Uniform optimizedFogColorUniform;
        private Uniform optimizedFadeAlphaUniform;
        private ShaderInstance optimizedUniformValueShader;
        private int optimizedUniformValueProgram = -1;
        private long optimizedUniformValuePipelineGeneration = -1L;
        private final Matrix4f optimizedLastProjectionMatrix = new Matrix4f();
        private boolean optimizedProjectionUniformValid;
        private boolean optimizedModelViewIdentityUniformValid;
        private boolean optimizedFogStartUniformValid;
        private boolean optimizedFogEndUniformValid;
        private boolean optimizedFogColorUniformValid;
        private boolean optimizedFadeAlphaOneUniformValid;
        private float optimizedLastFogStart;
        private float optimizedLastFogEnd;
        private int optimizedLastFogColorRed = Integer.MIN_VALUE;
        private int optimizedLastFogColorGreen = Integer.MIN_VALUE;
        private int optimizedLastFogColorBlue = Integer.MIN_VALUE;
        private int optimizedLastFogColorAlpha = Integer.MIN_VALUE;
        private RenderBackendFallbackReason lastInstancedQueueFallbackReason = RenderBackendFallbackReason.NONE;
        private RenderBackendFallbackReason lastIrisCompanionFallbackReason = RenderBackendFallbackReason.NONE;
        private boolean warnedGpuMeshOffThreadUpload;
        private boolean warnedGpuMeshGlUnavailableUpload;
        private boolean warnedInstancedOffThreadUpload;
        private boolean warnedInstancedGlUnavailableUpload;
        private boolean warnedIrisCompanionOffThreadUpload;
        private boolean warnedIrisCompanionGlUnavailableUpload;
        private static volatile boolean irisUntexturedWhiteTextureRegistered;

        private ExperimentalGpuPreparedRenderBackend(RenderBackend cpuFallback) {
            this.cpuFallback = cpuFallback;
        }

        @Override
        public boolean prepareTexturedCpuSequence(TexturedPreparedSequence sequence, PreparedBatch batch,
                ResourceLocation textureLocation, MultiBufferSource buffer, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
                RenderBackendFallbackReason fallbackReason) {
            if (cpuFallback instanceof CpuPreparedRenderBackend cpu) {
                sequence.prepare(this, cpu, batch, textureLocation, buffer, packedLight, packedOverlay, red, green,
                        blue, alpha, legacyShadow, smoothing, renderMode, uvTransform, fallbackReason);
                return true;
            }
            sequence.clear();
            return false;
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
                renderTexturedCpuFallback(fallback, batch, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                        red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
                return;
            }
            LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, poseStack.last().pose()).withAlpha(alpha);
            boolean instancedOnlyMode = requiresInstancedGpuPath(alphaMode);
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            try {
                if (canUseIrisGlintTransientCompanionPath(flags, alphaMode, alpha, legacyShadow)) {
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
                    renderTexturedIrisCompanionCpuFallback(irisCompanionFallbackReason(
                            RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED), batch,
                            textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                            legacyShadow, smoothing, renderMode, uvTransform);
                    return;
                }
                if (canUseIrisDynamicUvTransientCompanionPath(flags, alphaMode, alpha, legacyShadow, uvTransform)) {
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
                    renderTexturedIrisCompanionCpuFallback(irisCompanionFallbackReason(
                            RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED), batch,
                            textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                            legacyShadow, smoothing, renderMode, uvTransform);
                    return;
                }
                if (canQueueIrisCompanionPath(flags, alphaMode, alpha, legacyShadow, uvTransform)) {
                    if (queueIrisCompanion(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                            red, green, blue, alpha, smoothing, alphaMode, uvTransform)) {
                        return;
                    }
                    renderTexturedIrisCompanionCpuFallback(RenderBackendFallbackReason.IRIS_COMPANION_UPLOAD_FAILED, batch,
                            textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                            legacyShadow, smoothing, renderMode, uvTransform);
                    return;
                }
                if (canUseIrisSingleMeshPath(flags, alphaMode, alpha, legacyShadow, uvTransform)) {
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
                    renderTexturedIrisCompanionCpuFallback(irisCompanionFallbackReason(
                            RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED), batch,
                            textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                            legacyShadow, smoothing, renderMode, uvTransform);
                    return;
                }
                if (canUseTransientTexturedSingleMeshPath(flags, alphaMode, alpha, legacyShadow, uvTransform)) {
                    try {
                        if (drawTransientTexturedSingleMesh(batch, textureLocation, poseStack, packedLight,
                                packedOverlay, red, green, blue, alpha, smoothing, alphaMode, uvTransform)) {
                            return;
                        }
                    } catch (RuntimeException exception) {
                        renderTexturedStaticFadeCpuFallback(RenderBackendFallbackReason.GPU_DRAW_FAILED, batch,
                                textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue,
                                alpha, legacyShadow, smoothing, renderMode, uvTransform);
                        return;
                    }
                    renderTexturedStaticFadeCpuFallback(RenderBackendFallbackReason.GPU_UPLOAD_FAILED, batch,
                            textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                            legacyShadow, smoothing, renderMode, uvTransform);
                    return;
                }
                if (flags.instancingEnabled() && canUseInstancedPath(alphaMode)) {
                    if (queueInstanced(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue,
                            alpha, smoothing, alphaMode, uvTransform, flags.useSlicedLight())) {
                        return;
                    }
                }
                if (!uvTransform.gpuMeshCacheable()) {
                    renderTexturedCpuFallback(RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM, batch,
                            textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                            legacyShadow, smoothing, renderMode, uvTransform);
                    return;
                }
                if (instancedOnlyMode) {
                    renderTexturedCpuFallback(instancedQueueFallbackReason(
                            RenderBackendFallbackReason.INSTANCING_UPLOAD_FAILED), batch, textureLocation,
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
                renderSpriteCpuFallback(fallback, batch, sprite, poseStack, buffer, packedLight, packedOverlay,
                        red, green, blue, alpha, legacyShadow, partBrightness, renderMode, uvTransform);
                return;
            }
            LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, poseStack.last().pose())
                    .withAlpha(alpha);
            boolean instancedOnlyMode = requiresInstancedGpuPath(alphaMode);
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            try {
                if (canUseIrisDynamicUvTransientCompanionPath(flags, alphaMode, alpha, legacyShadow, uvTransform)) {
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
                    renderSpriteIrisCompanionCpuFallback(irisCompanionFallbackReason(
                            RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED), batch, sprite,
                            poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                            partBrightness, renderMode, uvTransform);
                    return;
                }
                if (canQueueIrisCompanionPath(flags, alphaMode, alpha, legacyShadow, uvTransform)) {
                    if (queueIrisCompanionSprite(batch, sprite, poseStack, buffer, packedLight, packedOverlay,
                            red, green, blue, alpha, alphaMode, uvTransform)) {
                        return;
                    }
                    renderSpriteIrisCompanionCpuFallback(RenderBackendFallbackReason.IRIS_COMPANION_UPLOAD_FAILED, batch, sprite,
                            poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                            partBrightness, renderMode, uvTransform);
                    return;
                }
                if (canUseIrisSingleMeshPath(flags, alphaMode, alpha, legacyShadow, uvTransform)) {
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
                    renderSpriteIrisCompanionCpuFallback(irisCompanionFallbackReason(
                            RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED), batch, sprite,
                            poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                            partBrightness, renderMode, uvTransform);
                    return;
                }
                if (canUseTransientTexturedSingleMeshPath(flags, alphaMode, alpha, legacyShadow, uvTransform)) {
                    try {
                        if (drawTransientSpriteSingleMesh(batch, sprite, poseStack, packedLight, packedOverlay,
                                red, green, blue, alpha, alphaMode, uvTransform)) {
                            return;
                        }
                    } catch (RuntimeException exception) {
                        renderSpriteStaticFadeCpuFallback(RenderBackendFallbackReason.GPU_DRAW_FAILED, batch,
                                sprite, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                                legacyShadow, partBrightness, renderMode, uvTransform);
                        return;
                    }
                    renderSpriteStaticFadeCpuFallback(RenderBackendFallbackReason.GPU_UPLOAD_FAILED, batch,
                            sprite, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                            legacyShadow, partBrightness, renderMode, uvTransform);
                    return;
                }
                if (flags.instancingEnabled() && canUseInstancedPath(alphaMode)) {
                    if (queueInstancedSprite(batch, sprite, poseStack, buffer, packedLight, packedOverlay, red, green,
                            blue, alpha, alphaMode, uvTransform, flags.useSlicedLight())) {
                        return;
                    }
                }
                if (!uvTransform.gpuMeshCacheable()) {
                    renderSpriteCpuFallback(RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM, batch, sprite,
                            poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, legacyShadow,
                            partBrightness, renderMode, uvTransform);
                    return;
                }
                if (instancedOnlyMode) {
                    renderSpriteCpuFallback(instancedQueueFallbackReason(
                            RenderBackendFallbackReason.INSTANCING_UPLOAD_FAILED), batch, sprite,
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
            LegacyTexturedRenderMode resolvedRenderMode = renderModeForPose(renderMode, poseStack.last().pose());
            RenderBackendFallbackReason fallback = unsupportedUntexturedReason(batch, alpha, resolvedRenderMode);
            if (fallback != RenderBackendFallbackReason.NONE) {
                renderUntexturedCpuFallback(fallback, batch, poseStack, buffer, red, green, blue, alpha, renderMode);
                return;
            }
            try {
                HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
                if (canQueueIrisUntexturedCompanionPath(flags, resolvedRenderMode, alpha)) {
                    if (queueIrisCompanionUntextured(batch, poseStack, buffer, red, green, blue, alpha,
                            resolvedRenderMode)) {
                        return;
                    }
                    renderUntexturedIrisCompanionCpuFallback(RenderBackendFallbackReason.IRIS_COMPANION_UPLOAD_FAILED, batch,
                            poseStack, buffer, red, green, blue, alpha, renderMode);
                    return;
                }
                if (canUseIrisUntexturedSingleMeshPath(flags, resolvedRenderMode, alpha)) {
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
                    renderUntexturedIrisCompanionCpuFallback(irisCompanionFallbackReason(
                            RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED), batch,
                            poseStack, buffer, red, green, blue, alpha, renderMode);
                    return;
                }
                if (flags.instancingEnabled() && canUseInstancedUntexturedPath(resolvedRenderMode, alpha)) {
                    if (queueInstancedUntextured(batch, poseStack, buffer, red, green, blue, alpha,
                            resolvedRenderMode, flags.useSlicedLight())) {
                        return;
                    }
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
            LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, poseStack.last().pose())
                    .withAlpha(alpha);
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            if (unsupported == RenderBackendFallbackReason.GPU_SHADER_ACTIVE
                    && canUseIrisTexturedTransientCompanionPath(flags, alphaMode, alpha, legacyShadow,
                            uvTransform)) {
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
                renderTexturedIrisCompanionCpuFallback(irisCompanionFallbackReason(
                        RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED), batch,
                        textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                        legacyShadow, smoothing, renderMode, uvTransform);
                return;
            }
            if (unsupported != RenderBackendFallbackReason.NONE) {
                if (unsupported == RenderBackendFallbackReason.GPU_DISABLED) {
                    renderTexturedTransientGpuDisabledCpuFallback(batch, textureLocation, poseStack, buffer,
                            packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing,
                            renderMode, uvTransform, fallbackReason);
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
                        safeClose(mesh, "transient textured quad mesh");
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
                        safeClose(mesh, "transient textured triangle mesh");
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
            LegacyTexturedRenderMode resolvedRenderMode = renderModeForPose(renderMode, poseStack.last().pose());
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            if (unsupported == RenderBackendFallbackReason.GPU_SHADER_ACTIVE
                    && canUseIrisUntexturedSingleMeshPath(flags, resolvedRenderMode, alpha)) {
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
                renderUntexturedIrisCompanionCpuFallback(irisCompanionFallbackReason(
                        RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED), batch,
                        poseStack, buffer, red, green, blue, alpha, renderMode);
                return;
            }
            if (unsupported != RenderBackendFallbackReason.NONE) {
                if (unsupported == RenderBackendFallbackReason.GPU_DISABLED) {
                    renderUntexturedTransientGpuDisabledCpuFallback(batch, poseStack, buffer, red, green, blue,
                            alpha, renderMode, fallbackReason);
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
                        safeClose(mesh, "transient untextured quad mesh");
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
                        safeClose(mesh, "transient untextured triangle mesh");
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
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            if (unsupported == RenderBackendFallbackReason.GPU_SHADER_ACTIVE
                    && canUseIrisVertexColorTransientCompanionPath(flags, renderMode, quad.minimumAlpha())) {
                try {
                    if (drawIrisTransientVertexColorCompanion(quad, poseStack, renderMode)) {
                        return;
                    }
                } catch (RuntimeException exception) {
                    recordIrisCompanionFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED, 4,
                            "iris-transient-vertex-color-quad: " + exception.getClass().getSimpleName());
                    HbmNtm.LOGGER.debug("Failed to draw transient Iris/Oculus vertex-color quad companion", exception);
                }
            }
            if (unsupported != RenderBackendFallbackReason.NONE) {
                if (unsupported == RenderBackendFallbackReason.GPU_DISABLED) {
                    renderUntexturedVertexColorTransientGpuDisabledCpuFallback(quad, poseStack, buffer, renderMode,
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
                    safeClose(mesh, "transient vertex-color quad mesh");
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
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            if (unsupported == RenderBackendFallbackReason.GPU_SHADER_ACTIVE
                    && canUseIrisVertexColorTransientCompanionPath(flags, renderMode, triangle.minimumAlpha())) {
                try {
                    if (drawIrisTransientVertexColorCompanion(triangle, poseStack, renderMode)) {
                        return;
                    }
                } catch (RuntimeException exception) {
                    recordIrisCompanionFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED, 3,
                            "iris-transient-vertex-color-triangle: " + exception.getClass().getSimpleName());
                    HbmNtm.LOGGER.debug("Failed to draw transient Iris/Oculus vertex-color triangle companion",
                            exception);
                }
            }
            if (unsupported != RenderBackendFallbackReason.NONE) {
                if (unsupported == RenderBackendFallbackReason.GPU_DISABLED) {
                    renderUntexturedVertexColorTransientTriangleGpuDisabledCpuFallback(triangle, poseStack, buffer,
                            renderMode,
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
                    safeClose(mesh, "transient vertex-color triangle mesh");
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
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            if (unsupported == RenderBackendFallbackReason.GPU_SHADER_ACTIVE
                    && canUseIrisVertexColorTransientCompanionPath(flags, renderMode,
                            minimumTriangleAlpha(triangles))) {
                try {
                    if (drawIrisTransientVertexColorTriangleBatchCompanion(triangles, poseStack, renderMode)) {
                        return;
                    }
                } catch (RuntimeException exception) {
                    recordIrisCompanionFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED,
                            triangles.size() * 3,
                            "iris-transient-vertex-color-triangle-batch: " + exception.getClass().getSimpleName());
                    HbmNtm.LOGGER.debug(
                            "Failed to draw transient Iris/Oculus vertex-color triangle batch companion", exception);
                }
            }
            if (unsupported != RenderBackendFallbackReason.NONE) {
                if (unsupported == RenderBackendFallbackReason.GPU_DISABLED) {
                    renderUntexturedVertexColorTransientTrianglesGpuDisabledCpuFallback(triangles, poseStack, buffer,
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
                    safeClose(mesh, "transient vertex-color triangle batch mesh");
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
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            if (unsupported == RenderBackendFallbackReason.GPU_SHADER_ACTIVE
                    && canUseIrisLineTransientCompanionPath(flags, renderMode, minimumLineAlpha(lines))) {
                try {
                    if (drawIrisTransientLineCompanion(lines, poseStack, renderMode, lineWidth)) {
                        return;
                    }
                } catch (RuntimeException exception) {
                    recordIrisCompanionFallback(RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED,
                            lines.size() * 2,
                            "iris-transient-lines: " + exception.getClass().getSimpleName());
                    HbmNtm.LOGGER.debug("Failed to draw transient Iris/Oculus LINE companion", exception);
                }
            }
            if (unsupported != RenderBackendFallbackReason.NONE) {
                if (unsupported == RenderBackendFallbackReason.GPU_DISABLED) {
                    renderUntexturedLineGpuDisabledCpuFallback(lines, poseStack, buffer, renderMode, lineWidth,
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
                    safeClose(mesh, "transient line mesh");
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
            // The old fixed-function path could disable directional lights while retaining the world
            // lightmap.  The optimized OBJ GPU path always applies legacyStandardLight, so this material
            // must stay on the CPU/static RenderType path backed by the lightmap-only text shader.
            if (renderMode == LegacyTexturedRenderMode.LIGHTMAP_ONLY_CUTOUT_NO_CULL) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_RENDER_MODE;
            }
            if (LegacyTexturedRenderMode.rawLightmapCoordinates(renderMode)) {
                return RenderBackendFallbackReason.GPU_RAW_LIGHTMAP_COORDINATES;
            }
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            RenderBackendFallbackReason immediateFallback = safeImmediateSingleMeshFallbackReason(flags);
            boolean safeImmediateSingleMesh = immediateFallback == RenderBackendFallbackReason.NONE
                    && flags.frameGeneration() == 0L;
            if (immediateFallback != RenderBackendFallbackReason.NONE) {
                return immediateFallback;
            }
            if (!flags.experimentalGpuBackendEnabled() && !safeImmediateSingleMesh) {
                return RenderBackendFallbackReason.GPU_DISABLED;
            }
            if (!safeImmediateSingleMesh && (flags.shaderPackDetected() || flags.shaderShadowPass())) {
                return canUseIrisSingleMeshPath(flags, renderMode, alpha, legacyShadow, uvTransform)
                        || canQueueIrisCompanionPath(flags, renderMode, alpha, legacyShadow, uvTransform)
                        || canUseIrisDynamicUvTransientCompanionPath(flags, renderMode, alpha, legacyShadow,
                                uvTransform)
                        || canUseIrisGlintTransientCompanionPath(flags, renderMode, alpha, legacyShadow)
                        ? RenderBackendFallbackReason.NONE
                        : irisCompanionMaterialFallbackReason(renderMode, alpha, legacyShadow, uvTransform);
            }
            if (!flags.gpuBackendAllowed() && !safeImmediateSingleMesh) {
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
            RenderBackendFallbackReason immediateFallback = safeImmediateSingleMeshFallbackReason(flags);
            boolean safeImmediateSingleMesh = immediateFallback == RenderBackendFallbackReason.NONE
                    && flags.frameGeneration() == 0L;
            if (immediateFallback != RenderBackendFallbackReason.NONE) {
                return immediateFallback;
            }
            if (!flags.experimentalGpuBackendEnabled() && !safeImmediateSingleMesh) {
                return RenderBackendFallbackReason.GPU_DISABLED;
            }
            if (!safeImmediateSingleMesh && (flags.shaderPackDetected() || flags.shaderShadowPass())) {
                return canUseIrisSingleMeshPath(flags, renderMode, alpha, legacyShadow, uvTransform)
                        || canQueueIrisCompanionPath(flags, renderMode, alpha, legacyShadow, uvTransform)
                        || canUseIrisDynamicUvTransientCompanionPath(flags, renderMode, alpha, legacyShadow,
                                uvTransform)
                        ? RenderBackendFallbackReason.NONE
                        : irisCompanionMaterialFallbackReason(renderMode, alpha, legacyShadow, uvTransform);
            }
            if (!flags.gpuBackendAllowed() && !safeImmediateSingleMesh) {
                return RenderBackendFallbackReason.GPU_SHADER_ACTIVE;
            }
            if (!RenderSystem.isOnRenderThread()) {
                return RenderBackendFallbackReason.GPU_NOT_RENDER_THREAD;
            }
            if (canUseTransientTexturedSingleMeshPath(flags, renderMode, alpha, legacyShadow, uvTransform)) {
                return RenderBackendFallbackReason.NONE;
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
            RenderBackendFallbackReason immediateFallback = safeImmediateSingleMeshFallbackReason(flags);
            boolean safeImmediateSingleMesh = immediateFallback == RenderBackendFallbackReason.NONE
                    && flags.frameGeneration() == 0L;
            if (immediateFallback != RenderBackendFallbackReason.NONE) {
                return immediateFallback;
            }
            if (!flags.experimentalGpuBackendEnabled() && !safeImmediateSingleMesh) {
                return RenderBackendFallbackReason.GPU_DISABLED;
            }
            if (!safeImmediateSingleMesh && (flags.shaderPackDetected() || flags.shaderShadowPass())) {
                return canUseIrisUntexturedSingleMeshPath(flags, renderMode, alpha)
                        || canQueueIrisUntexturedCompanionPath(flags, renderMode, alpha)
                        ? RenderBackendFallbackReason.NONE
                        : untexturedMaterialFallbackReason(renderMode, alpha);
            }
            if (!flags.gpuBackendAllowed() && !safeImmediateSingleMesh) {
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

        /**
         * Item/hand rendering happens after {@code AFTER_LEVEL}; other
         * immediate render stages can also sit outside the optimized BER frame
         * window. At that point there is no later instancing flush. Keep the
         * conservative no-shader static OBJ route available there, but only
         * for immediate cached single-mesh draws. The initial snapshot must
         * not be trusted for shader state, so sample Iris/Oculus directly.
         */
        private static RenderBackendFallbackReason safeImmediateSingleMeshFallbackReason(
                HbmRenderFrameFlags.Snapshot flags) {
            // Every BEWLR context must submit to the caller-owned MultiBufferSource. GUI,
            // dropped-item, item-frame and hand rendering all have an ItemRenderer-managed
            // buffer lifecycle, and the non-GUI calls can occur outside the block-entity
            // backend's queued-flush window. A direct/queued GPU draw here can therefore be
            // cleared, reordered or never presented, making builtin/entity OBJs disappear.
            if (LegacyItemRenderContext.isActive()) {
                return RenderBackendFallbackReason.GPU_DISABLED;
            }
            if (flags.frameGeneration() != 0L) {
                return RenderBackendFallbackReason.NONE;
            }
            if (!HbmClientConfig.safeObjStaticBatching()) {
                return RenderBackendFallbackReason.GPU_DISABLED;
            }
            if (!RenderSystem.isOnRenderThread()) {
                return RenderBackendFallbackReason.GPU_NOT_RENDER_THREAD;
            }
            if (HbmShaderCompatibilityDetector.isExternalShaderActive()
                    || HbmShaderCompatibilityDetector.isRenderingShadowPass()) {
                return RenderBackendFallbackReason.GPU_SHADER_ACTIVE;
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
            if (isGlintRenderMode(renderMode)) {
                return RenderBackendFallbackReason.GPU_UNSUPPORTED_GLINT;
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

        private static boolean canUseIrisLineTransientCompanionPath(HbmRenderFrameFlags.Snapshot flags,
                LegacyTexturedRenderMode renderMode, int minimumAlpha) {
            return canUseIrisUntexturedSingleMeshPath(flags, renderMode, minimumAlpha);
        }

        private static boolean canUseIrisVertexColorTransientCompanionPath(HbmRenderFrameFlags.Snapshot flags,
                LegacyTexturedRenderMode renderMode, int minimumAlpha) {
            return canUseIrisUntexturedSingleMeshPath(flags, renderMode, minimumAlpha);
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
                    && isGlintRenderMode(renderMode)
                    && alpha == 255
                    && !legacyShadow
                    && RenderSystem.isOnRenderThread()
                    && !HbmShaderCompatibilityDetector.isRenderingShadowPass();
        }

        private static boolean canUseTransientTexturedSingleMeshPath(HbmRenderFrameFlags.Snapshot flags,
                LegacyTexturedRenderMode renderMode, int alpha, boolean legacyShadow, UvTransform uvTransform) {
            boolean transientOnlyMaterial = !uvTransform.gpuMeshCacheable() || isGlintRenderMode(renderMode);
            boolean supportedAlpha = alpha == 255 || renderMode.translucent();
            return flags.experimentalGpuBackendEnabled()
                    && flags.gpuBackendAllowed()
                    && !flags.shaderPackDetected()
                    && !flags.shaderShadowPass()
                    && transientOnlyMaterial
                    && supportedAlpha
                    && !legacyShadow
                    && RenderSystem.isOnRenderThread();
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

        private static boolean isGlintRenderMode(LegacyTexturedRenderMode renderMode) {
            return renderMode == LegacyTexturedRenderMode.GLINT_NO_DEPTH_WRITE
                    || renderMode == LegacyTexturedRenderMode.GLINT_EQUAL_DEPTH;
        }

        private static boolean supportsInstancedTail(LegacyTexturedRenderMode renderMode) {
            return supportsInstancedAdditiveTail(renderMode)
                    || supportsInstancedNormalAlphaTail(renderMode);
        }

        private static boolean isInstancedAdditiveMode(LegacyTexturedRenderMode renderMode) {
            return supportsInstancedAdditiveTail(renderMode);
        }

        private static boolean isInstancedNormalAlphaMode(LegacyTexturedRenderMode renderMode) {
            return supportsInstancedNormalAlphaTail(renderMode);
        }

        private static boolean supportsInstancedAlpha(LegacyTexturedRenderMode renderMode) {
            return supportsInstancedTail(renderMode);
        }

        private static boolean requiresInstancedGpuPath(LegacyTexturedRenderMode renderMode) {
            return supportsInstancedTail(renderMode);
        }

        private void renderTexturedTransientGpuDisabledCpuFallback(PreparedBatch batch,
                ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
                RenderBackendFallbackReason originalFallbackReason) {
            recordGpuFallback(RenderBackendFallbackReason.GPU_DISABLED, batch.vertexCount(),
                    fallbackDetail("gpu-textured-transient:" + originalFallbackReason,
                            RenderBackendFallbackReason.GPU_DISABLED, batch, textureLocation, renderMode, 1));
            cpuFallback.renderTextured(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
        }

        private void renderUntexturedTransientGpuDisabledCpuFallback(PreparedBatch batch,
                PoseStack poseStack, MultiBufferSource buffer, int red, int green, int blue, int alpha,
                LegacyTexturedRenderMode renderMode, RenderBackendFallbackReason originalFallbackReason) {
            recordGpuFallback(RenderBackendFallbackReason.GPU_DISABLED, batch.vertexCount(),
                    fallbackDetail("gpu-untextured-transient:" + originalFallbackReason,
                            RenderBackendFallbackReason.GPU_DISABLED, batch, InventoryMenu.BLOCK_ATLAS,
                            renderMode, 1));
            cpuFallback.renderUntextured(batch, poseStack, buffer, red, green, blue, alpha, renderMode);
        }

        private void renderUntexturedVertexColorTransientGpuDisabledCpuFallback(
                UntexturedVertexColorTransientQuad quad, PoseStack poseStack, MultiBufferSource buffer,
                LegacyTexturedRenderMode renderMode, RenderBackendFallbackReason originalFallbackReason) {
            recordGpuFallback(RenderBackendFallbackReason.GPU_DISABLED, 4,
                    fallbackDetail("gpu-vertex-color-transient:" + originalFallbackReason,
                            RenderBackendFallbackReason.GPU_DISABLED, "direct_quad", InventoryMenu.BLOCK_ATLAS,
                            renderMode, 4, 1));
            renderUntexturedVertexColorTransientCpu(quad, poseStack, buffer, renderMode);
        }

        private void renderUntexturedVertexColorTransientTriangleGpuDisabledCpuFallback(
                UntexturedVertexColorTransientTriangle triangle, PoseStack poseStack, MultiBufferSource buffer,
                LegacyTexturedRenderMode renderMode, RenderBackendFallbackReason originalFallbackReason) {
            recordGpuFallback(RenderBackendFallbackReason.GPU_DISABLED, 3,
                    fallbackDetail("gpu-vertex-color-triangle-transient:" + originalFallbackReason,
                            RenderBackendFallbackReason.GPU_DISABLED, "direct_triangle",
                            InventoryMenu.BLOCK_ATLAS, renderMode, 3, 1));
            renderUntexturedVertexColorTransientCpu(triangle, poseStack, buffer, renderMode);
        }

        private void renderUntexturedVertexColorTransientTrianglesGpuDisabledCpuFallback(
                List<UntexturedVertexColorTransientTriangle> triangles, PoseStack poseStack,
                MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                RenderBackendFallbackReason originalFallbackReason) {
            int vertices = triangles.size() * 3;
            recordGpuFallback(RenderBackendFallbackReason.GPU_DISABLED, vertices,
                    fallbackDetail("gpu-vertex-color-triangle-batch-transient:" + originalFallbackReason,
                            RenderBackendFallbackReason.GPU_DISABLED, "direct_triangle_batch",
                            InventoryMenu.BLOCK_ATLAS, renderMode, vertices, 1));
            renderUntexturedVertexColorTransientTrianglesCpu(triangles, poseStack, buffer, renderMode);
        }

        private void renderUntexturedLineGpuDisabledCpuFallback(List<UntexturedLineTransient> lines,
                PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode, float lineWidth,
                RenderBackendFallbackReason originalFallbackReason) {
            int vertices = lines.size() * 2;
            recordGpuFallback(RenderBackendFallbackReason.GPU_DISABLED, vertices,
                    fallbackDetail("gpu-line-transient:" + originalFallbackReason,
                            RenderBackendFallbackReason.GPU_DISABLED, "direct_lines", InventoryMenu.BLOCK_ATLAS,
                            renderMode, vertices, 1));
            renderUntexturedLineTransientCpu(lines, poseStack, buffer, renderMode, lineWidth);
        }

        private void renderTexturedCpuFallback(RenderBackendFallbackReason reason, PreparedBatch batch,
                ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            recordGpuFallback(reason, batch.vertexCount(),
                    fallbackDetail("gpu-textured", reason, batch, textureLocation, renderMode, 1));
            cpuFallback.renderTextured(batch, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
        }

        private void renderTexturedIrisCompanionCpuFallback(RenderBackendFallbackReason reason, PreparedBatch batch,
                ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            renderTexturedStaticFadeCpuFallback(reason, batch, textureLocation, poseStack, buffer, packedLight,
                    packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
        }

        private void renderTexturedStaticFadeCpuFallback(RenderBackendFallbackReason reason, PreparedBatch batch,
                ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            float fadeAlpha = HbmRenderFrameCulling.currentStaticModelFade();
            if (fadeAlpha < 0.0F) {
                return;
            }
            boolean faded = fadeAlpha < 0.999F;
            LegacyTexturedRenderMode effectiveRenderMode = fadeRenderMode(renderMode, faded);
            renderTexturedCpuFallback(reason, batch, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, fadedAlpha(alpha, fadeAlpha), legacyShadow, smoothing, effectiveRenderMode,
                    uvTransform);
        }

        private void renderSpriteCpuFallback(RenderBackendFallbackReason reason, PreparedBatch batch,
                TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
                boolean partBrightness, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            recordGpuFallback(reason, batch.vertexCount(),
                    fallbackDetail("gpu-sprite", reason, batch, InventoryMenu.BLOCK_ATLAS, renderMode, 1));
            cpuFallback.renderSprite(batch, sprite, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha, legacyShadow, partBrightness, renderMode, uvTransform);
        }

        private void renderSpriteIrisCompanionCpuFallback(RenderBackendFallbackReason reason, PreparedBatch batch,
                TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
                boolean partBrightness, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            renderSpriteStaticFadeCpuFallback(reason, batch, sprite, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha, legacyShadow, partBrightness, renderMode, uvTransform);
        }

        private void renderSpriteStaticFadeCpuFallback(RenderBackendFallbackReason reason, PreparedBatch batch,
                TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
                boolean partBrightness, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            float fadeAlpha = HbmRenderFrameCulling.currentStaticModelFade();
            if (fadeAlpha < 0.0F) {
                return;
            }
            boolean faded = fadeAlpha < 0.999F;
            LegacyTexturedRenderMode effectiveRenderMode = fadeRenderMode(renderMode, faded);
            renderSpriteCpuFallback(reason, batch, sprite, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, fadedAlpha(alpha, fadeAlpha), legacyShadow, partBrightness,
                    effectiveRenderMode, uvTransform);
        }

        private void renderUntexturedCpuFallback(RenderBackendFallbackReason reason, PreparedBatch batch,
                PoseStack poseStack, MultiBufferSource buffer, int red, int green, int blue, int alpha,
                LegacyTexturedRenderMode renderMode) {
            recordGpuFallback(reason, batch.vertexCount(),
                    fallbackDetail("gpu-untextured", reason, batch, InventoryMenu.BLOCK_ATLAS, renderMode, 1));
            cpuFallback.renderUntextured(batch, poseStack, buffer, red, green, blue, alpha, renderMode);
        }

        private void renderUntexturedIrisCompanionCpuFallback(RenderBackendFallbackReason reason, PreparedBatch batch,
                PoseStack poseStack, MultiBufferSource buffer, int red, int green, int blue, int alpha,
                LegacyTexturedRenderMode renderMode) {
            float fadeAlpha = HbmRenderFrameCulling.currentStaticModelFade();
            if (fadeAlpha < 0.0F) {
                return;
            }
            boolean faded = fadeAlpha < 0.999F;
            LegacyTexturedRenderMode effectiveRenderMode = fadeRenderMode(renderMode, faded);
            renderUntexturedCpuFallback(reason, batch, poseStack, buffer, red, green, blue,
                    fadedAlpha(alpha, fadeAlpha), effectiveRenderMode);
        }

        private boolean drawTransientTexturedSingleMesh(PreparedBatch batch, ResourceLocation textureLocation,
                PoseStack poseStack, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            float fadeAlpha = HbmRenderFrameCulling.currentStaticModelFade();
            if (fadeAlpha < 0.0F) {
                return true;
            }
            boolean faded = fadeAlpha < 0.999F;
            LegacyTexturedRenderMode effectiveRenderMode = fadeRenderMode(renderMode, faded);
            int effectiveAlpha = fadedAlpha(alpha, fadeAlpha);
            boolean drew = false;
            if (!batch.quadVertices().isEmpty()) {
                GpuMesh mesh = uploadTransientTexturedMesh(VertexFormat.Mode.QUADS, packedLight, packedOverlay,
                        red, green, blue, effectiveAlpha, smoothing, uvTransform, batch.quadVertices());
                try {
                    drawMesh(mesh, effectiveRenderMode.renderType(textureLocation, VertexFormat.Mode.QUADS),
                            poseStack);
                } finally {
                    safeClose(mesh, "transient textured quad mesh");
                }
                drew = true;
            }
            if (!batch.triangleVertices().isEmpty()) {
                GpuMesh mesh = uploadTransientTexturedMesh(VertexFormat.Mode.TRIANGLES, packedLight,
                        packedOverlay, red, green, blue, effectiveAlpha, smoothing, uvTransform,
                        batch.triangleVertices());
                try {
                    drawMesh(mesh, effectiveRenderMode.renderType(textureLocation, VertexFormat.Mode.TRIANGLES),
                            poseStack);
                } finally {
                    safeClose(mesh, "transient textured triangle mesh");
                }
                drew = true;
            }
            return drew;
        }

        private boolean drawTransientSpriteSingleMesh(PreparedBatch batch, TextureAtlasSprite sprite,
                PoseStack poseStack, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            float fadeAlpha = HbmRenderFrameCulling.currentStaticModelFade();
            if (fadeAlpha < 0.0F) {
                return true;
            }
            boolean faded = fadeAlpha < 0.999F;
            LegacyTexturedRenderMode effectiveRenderMode = fadeRenderMode(renderMode, faded);
            int effectiveAlpha = fadedAlpha(alpha, fadeAlpha);
            boolean drew = false;
            if (!batch.quadVertices().isEmpty()) {
                GpuMesh mesh = uploadTransientSpriteMesh(VertexFormat.Mode.QUADS, sprite, packedLight,
                        packedOverlay, red, green, blue, effectiveAlpha, uvTransform, batch.quadVertices());
                try {
                    drawMesh(mesh, effectiveRenderMode.renderType(InventoryMenu.BLOCK_ATLAS,
                            VertexFormat.Mode.QUADS), poseStack);
                } finally {
                    safeClose(mesh, "transient sprite quad mesh");
                }
                drew = true;
            }
            if (!batch.triangleVertices().isEmpty()) {
                GpuMesh mesh = uploadTransientSpriteMesh(VertexFormat.Mode.TRIANGLES, sprite, packedLight,
                        packedOverlay, red, green, blue, effectiveAlpha, uvTransform,
                        batch.triangleVertices());
                try {
                    drawMesh(mesh, effectiveRenderMode.renderType(InventoryMenu.BLOCK_ATLAS,
                            VertexFormat.Mode.TRIANGLES), poseStack);
                } finally {
                    safeClose(mesh, "transient sprite triangle mesh");
                }
                drew = true;
            }
            return drew;
        }

        private boolean queueInstanced(PreparedBatch batch, ResourceLocation textureLocation, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
                boolean useSlicedLight) {
            clearInstancedQueueFallback();
            float fadeAlpha = HbmRenderFrameCulling.currentStaticModelFade();
            if (fadeAlpha < 0.0F) {
                return true;
            }
            if (!UvTransform.DEFAULT.equals(uvTransform)) {
                recordInstancedQueueFallback(RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM, 1, renderMode,
                        fallbackDetail("instanced-queue-textured",
                                RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM, batch, textureLocation,
                                renderMode, 1));
                return false;
            }
            if (HbmOptimizedRenderShaders.blockLitInstancedShader() == null) {
                recordInstancedQueueFallback(RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE, 1, renderMode,
                        fallbackDetail("instanced-queue-textured",
                                RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE, batch, textureLocation,
                                renderMode, 1));
                return false;
            }
            RenderBackendFallbackReason uploadReason = instancedUploadUnavailableReason();
            if (uploadReason != RenderBackendFallbackReason.NONE) {
                recordInstancedQueueFallback(uploadReason, 1, renderMode,
                        fallbackDetail("instanced-queue-textured", uploadReason, batch, textureLocation,
                                renderMode, 1));
                return false;
            }
            boolean queued = false;
            if (!batch.quadVertices().isEmpty()) {
                InstancedMesh mesh = instancedMeshFor(batch, VertexFormat.Mode.QUADS, smoothing,
                        GpuMeshKind.TEXTURED, null,
                        batch.quadVertices());
                queueInstanced(mesh, textureLocation, renderMode, poseStack, buffer, packedLight, packedOverlay,
                        red, green, blue, alpha, uvTransform, fadeAlpha, useSlicedLight);
                queued = true;
            }
            if (!batch.triangleVertices().isEmpty()) {
                InstancedMesh mesh = instancedMeshFor(batch, VertexFormat.Mode.TRIANGLES, smoothing,
                        GpuMeshKind.TEXTURED, null,
                        batch.triangleVertices());
                queueInstanced(mesh, textureLocation, renderMode, poseStack, buffer, packedLight, packedOverlay,
                        red, green, blue, alpha, uvTransform, fadeAlpha, useSlicedLight);
                queued = true;
            }
            if (queued) {
                clearInstancedQueueFallback();
            }
            return queued;
        }

        private boolean queueInstancedSprite(PreparedBatch batch, TextureAtlasSprite sprite, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform, boolean useSlicedLight) {
            clearInstancedQueueFallback();
            float fadeAlpha = HbmRenderFrameCulling.currentStaticModelFade();
            if (fadeAlpha < 0.0F) {
                return true;
            }
            if (!UvTransform.DEFAULT.equals(uvTransform)) {
                recordInstancedQueueFallback(RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM, 1, renderMode,
                        fallbackDetail("instanced-queue-sprite",
                                RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM, batch,
                                InventoryMenu.BLOCK_ATLAS, renderMode, 1));
                return false;
            }
            if (HbmOptimizedRenderShaders.blockLitInstancedShader() == null) {
                recordInstancedQueueFallback(RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE, 1, renderMode,
                        fallbackDetail("instanced-queue-sprite",
                                RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE, batch,
                                InventoryMenu.BLOCK_ATLAS, renderMode, 1));
                return false;
            }
            RenderBackendFallbackReason uploadReason = instancedUploadUnavailableReason();
            if (uploadReason != RenderBackendFallbackReason.NONE) {
                recordInstancedQueueFallback(uploadReason, 1, renderMode,
                        fallbackDetail("instanced-queue-sprite", uploadReason, batch,
                                InventoryMenu.BLOCK_ATLAS, renderMode, 1));
                return false;
            }
            boolean queued = false;
            if (!batch.quadVertices().isEmpty()) {
                InstancedMesh mesh = instancedMeshFor(batch, VertexFormat.Mode.QUADS, false,
                        GpuMeshKind.SPRITE, sprite,
                        batch.quadVertices());
                queueInstanced(mesh, InventoryMenu.BLOCK_ATLAS, renderMode, poseStack, buffer, packedLight,
                        packedOverlay, red, green, blue, alpha, uvTransform, fadeAlpha, useSlicedLight);
                queued = true;
            }
            if (!batch.triangleVertices().isEmpty()) {
                InstancedMesh mesh = instancedMeshFor(batch, VertexFormat.Mode.TRIANGLES, false,
                        GpuMeshKind.SPRITE, sprite,
                        batch.triangleVertices());
                queueInstanced(mesh, InventoryMenu.BLOCK_ATLAS, renderMode, poseStack, buffer, packedLight,
                        packedOverlay, red, green, blue, alpha, uvTransform, fadeAlpha, useSlicedLight);
                queued = true;
            }
            if (queued) {
                clearInstancedQueueFallback();
            }
            return queued;
        }

        private boolean queueInstancedUntextured(PreparedBatch batch, PoseStack poseStack, MultiBufferSource buffer,
                int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode,
                boolean useSlicedLight) {
            clearInstancedQueueFallback();
            float fadeAlpha = HbmRenderFrameCulling.currentStaticModelFade();
            if (fadeAlpha < 0.0F) {
                return true;
            }
            if (HbmOptimizedRenderShaders.blockUntexturedInstancedShader() == null) {
                recordInstancedQueueFallback(RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE, 1, renderMode,
                        fallbackDetail("instanced-queue-untextured",
                                RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE, batch,
                                InventoryMenu.BLOCK_ATLAS, renderMode, 1));
                return false;
            }
            RenderBackendFallbackReason uploadReason = instancedUploadUnavailableReason();
            if (uploadReason != RenderBackendFallbackReason.NONE) {
                recordInstancedQueueFallback(uploadReason, 1, renderMode,
                        fallbackDetail("instanced-queue-untextured", uploadReason, batch,
                                InventoryMenu.BLOCK_ATLAS, renderMode, 1));
                return false;
            }
            boolean queued = false;
            if (!batch.quadVertices().isEmpty()) {
                InstancedMesh mesh = instancedMeshFor(batch, VertexFormat.Mode.QUADS, false,
                        GpuMeshKind.UNTEXTURED, null, batch.quadVertices());
                queueInstanced(mesh, InventoryMenu.BLOCK_ATLAS, renderMode, poseStack, buffer,
                        LightTexture.FULL_BRIGHT, 0, red, green, blue, alpha, UvTransform.DEFAULT, fadeAlpha,
                        useSlicedLight);
                queued = true;
            }
            if (!batch.triangleVertices().isEmpty()) {
                InstancedMesh mesh = instancedMeshFor(batch, VertexFormat.Mode.TRIANGLES, false,
                        GpuMeshKind.UNTEXTURED, null, batch.triangleVertices());
                queueInstanced(mesh, InventoryMenu.BLOCK_ATLAS, renderMode, poseStack, buffer,
                        LightTexture.FULL_BRIGHT, 0, red, green, blue, alpha, UvTransform.DEFAULT, fadeAlpha,
                        useSlicedLight);
                queued = true;
            }
            if (queued) {
                clearInstancedQueueFallback();
            }
            return queued;
        }

        private void queueInstanced(InstancedMesh mesh, ResourceLocation textureLocation, LegacyTexturedRenderMode renderMode,
                PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, UvTransform uvTransform, float fadeAlpha,
                boolean useSlicedLight) {
            if (fadeAlpha < 0.0F) {
                return;
            }
            boolean faded = fadeAlpha < 0.999F;
            LegacyTexturedRenderMode effectiveRenderMode = fadeRenderMode(renderMode, faded);
            InstancedBatch batch = pendingInstancedBatch(mesh, textureLocation, effectiveRenderMode);
            boolean newBatch = batch.instances().isEmpty();
            PoseStack.Pose pose = poseStack.last();
            Matrix4f modelView = pose.pose();
            boolean sampleSlicedLight = packedLight != LightTexture.FULL_BRIGHT && useSlicedLight;
            batch.instances().add(InstancedInstance.from(pose, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha, uvTransform, fadeAlpha,
                    mesh.sampleInstanceLightProbe(modelView, packedLight, sampleSlicedLight)));
            instancedQueuedInstances++;
            currentFrameInstancedQueuedInstances++;
            if (isInstancedAdditiveMode(effectiveRenderMode)) {
                instancedAdditiveQueuedInstances++;
                currentFrameInstancedAdditiveQueuedInstances++;
            }
            if (HbmRenderFrameCulling.shouldRecordObjInstancedHotParts()) {
                HbmRenderFrameCulling.recordObjInstancedQueue(
                        instancedHotPartKey(mesh, textureLocation, effectiveRenderMode),
                        mesh.vertexCount(), 1, newBatch, faded);
            } else {
                HbmRenderFrameCulling.recordObjInstancedQueue(1, newBatch, faded);
            }
            if (newBatch) {
                instancedQueuedBatches++;
                currentFrameInstancedQueuedBatches++;
                if (isInstancedAdditiveMode(effectiveRenderMode)) {
                    instancedAdditiveQueuedBatches++;
                    currentFrameInstancedAdditiveQueuedBatches++;
                }
            }
        }

        private static String instancedHotPartKey(InstancedMesh mesh, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode) {
            InstancedMeshKey key = mesh.key();
            return key.stablePartKey() + "|tex=" + textureLocation + "|mode=" + renderMode;
        }

        private static LegacyTexturedRenderMode fadeRenderMode(LegacyTexturedRenderMode renderMode, boolean faded) {
            if (!faded || renderMode.translucent()) {
                return renderMode;
            }
            return LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE;
        }

        private boolean queueIrisCompanion(PreparedBatch batch, ResourceLocation textureLocation, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            float fadeAlpha = HbmRenderFrameCulling.currentStaticModelFade();
            if (fadeAlpha < 0.0F) {
                return true;
            }
            boolean queued = false;
            if (!batch.quadVertices().isEmpty()) {
                queueIrisCompanionPart(GpuMeshKind.TEXTURED, batch.stableKey(), batch.quadVertices(),
                        batch.geometryKey(), VertexFormat.Mode.QUADS, null, textureLocation, renderMode,
                        poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, smoothing,
                        uvTransform, fadeAlpha);
                queued = true;
            }
            if (!batch.triangleVertices().isEmpty()) {
                queueIrisCompanionPart(GpuMeshKind.TEXTURED, batch.stableKey(), batch.triangleVertices(),
                        batch.geometryKey(), VertexFormat.Mode.TRIANGLES, null, textureLocation, renderMode,
                        poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha, smoothing,
                        uvTransform, fadeAlpha);
                queued = true;
            }
            return queued;
        }

        private boolean queueIrisCompanionSprite(PreparedBatch batch, TextureAtlasSprite sprite, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            float fadeAlpha = HbmRenderFrameCulling.currentStaticModelFade();
            if (fadeAlpha < 0.0F) {
                return true;
            }
            boolean queued = false;
            if (!batch.quadVertices().isEmpty()) {
                queueIrisCompanionPart(GpuMeshKind.SPRITE, batch.stableKey(), batch.quadVertices(),
                        batch.geometryKey(), VertexFormat.Mode.QUADS, sprite, InventoryMenu.BLOCK_ATLAS,
                        renderMode, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                        false, uvTransform, fadeAlpha);
                queued = true;
            }
            if (!batch.triangleVertices().isEmpty()) {
                queueIrisCompanionPart(GpuMeshKind.SPRITE, batch.stableKey(), batch.triangleVertices(),
                        batch.geometryKey(), VertexFormat.Mode.TRIANGLES, sprite, InventoryMenu.BLOCK_ATLAS,
                        renderMode, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                        false, uvTransform, fadeAlpha);
                queued = true;
            }
            return queued;
        }

        private boolean queueIrisCompanionUntextured(PreparedBatch batch, PoseStack poseStack,
                MultiBufferSource buffer, int red, int green, int blue, int alpha,
                LegacyTexturedRenderMode renderMode) {
            float fadeAlpha = HbmRenderFrameCulling.currentStaticModelFade();
            if (fadeAlpha < 0.0F) {
                return true;
            }
            ResourceLocation textureLocation = irisUntexturedWhiteTexture();
            boolean queued = false;
            if (!batch.quadVertices().isEmpty()) {
                queueIrisCompanionPart(GpuMeshKind.UNTEXTURED, batch.stableKey(), batch.quadVertices(),
                        batch.geometryKey(), VertexFormat.Mode.QUADS, null, textureLocation, renderMode,
                        poseStack, buffer,
                        LightTexture.FULL_BRIGHT, 0, red, green, blue, alpha, false, UvTransform.DEFAULT,
                        fadeAlpha);
                queued = true;
            }
            if (!batch.triangleVertices().isEmpty()) {
                queueIrisCompanionPart(GpuMeshKind.UNTEXTURED, batch.stableKey(), batch.triangleVertices(),
                        batch.geometryKey(), VertexFormat.Mode.TRIANGLES, null, textureLocation, renderMode,
                        poseStack, buffer,
                        LightTexture.FULL_BRIGHT, 0, red, green, blue, alpha, false, UvTransform.DEFAULT,
                        fadeAlpha);
                queued = true;
            }
            return queued;
        }

        private void queueIrisCompanionPart(GpuMeshKind kind, String stablePartKey, List<PreparedVertex> vertices,
                PreparedGeometryKey geometryKey, VertexFormat.Mode sourceMode, TextureAtlasSprite sprite,
                ResourceLocation textureLocation, LegacyTexturedRenderMode renderMode, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue,
                int alpha, boolean smoothing, UvTransform uvTransform, float fadeAlpha) {
            if (fadeAlpha < 0.0F) {
                return;
            }
            boolean faded = fadeAlpha < 0.999F;
            LegacyTexturedRenderMode effectiveRenderMode = fadeRenderMode(renderMode, faded);
            IrisCompanionQueuedBatch batch = pendingIrisCompanionBatch(kind, stablePartKey, geometryKey, sprite,
                    vertices.size(), sourceMode, smoothing, uvTransform, textureLocation, effectiveRenderMode,
                    vertices);
            boolean newBatch = batch.instances().isEmpty();
            batch.instances().add(IrisCompanionQueuedInstance.from(poseStack.last(), buffer, packedLight,
                    packedOverlay, red, green, blue, alpha, fadeAlpha));
            irisQueuedInstances++;
            currentFrameIrisQueuedInstances++;
            if (HbmRenderFrameCulling.shouldRecordObjInstancedHotParts()) {
                HbmRenderFrameCulling.recordObjInstancedQueue(
                        stablePartKey + "|tex=" + textureLocation + "|mode=" + effectiveRenderMode,
                        vertices.size(), 1, newBatch, faded);
            } else {
                HbmRenderFrameCulling.recordObjInstancedQueue(1, newBatch, faded);
            }
            if (newBatch) {
                irisQueuedBatches++;
                currentFrameIrisQueuedBatches++;
            }
        }

        private InstancedMesh instancedMeshFor(PreparedBatch batch, VertexFormat.Mode sourceMode, boolean smoothing,
                GpuMeshKind kind, TextureAtlasSprite sprite, List<PreparedVertex> vertices) {
            InstancedMesh cached = lastInstancedMesh(batch, sourceMode, smoothing, kind, sprite, vertices);
            if (cached != null) {
                return cached;
            }
            InstancedMeshKey key = new InstancedMeshKey(kind, batch.stableKey(), batch.geometryKey(), sprite,
                    vertices.size(), sourceMode, smoothing);
            if (failedInstancedKeys.contains(key)) {
                throw new IllegalStateException("Instanced mesh upload previously failed for " + key);
            }
            synchronized (instancedMeshes) {
                InstancedMesh existing = instancedMeshes.get(key);
                if (existing != null) {
                    rememberLastInstancedMesh(batch, sourceMode, smoothing, kind, sprite, vertices, existing);
                    return existing;
                }
                gpuUploadAttempts.incrementAndGet();
                try {
                    InstancedMesh created = uploadInstancedMesh(key, sourceMode, smoothing, vertices);
                    instancedMeshes.put(key, created);
                    gpuBufferBytes.addAndGet(created.byteSize());
                    rememberLastInstancedMesh(batch, sourceMode, smoothing, kind, sprite, vertices, created);
                    return created;
                } catch (RuntimeException exception) {
                    if (!HbmInstancedGlCompat.isInstancingUnavailable(exception)) {
                        failedInstancedKeys.add(key);
                    }
                    gpuUploadFailures.incrementAndGet();
                    throw exception;
                }
            }
        }

        private InstancedMesh lastInstancedMesh(PreparedBatch batch, VertexFormat.Mode sourceMode, boolean smoothing,
                GpuMeshKind kind, TextureAtlasSprite sprite, List<PreparedVertex> vertices) {
            InstancedMesh mesh = lastInstancedMesh;
            if (mesh == null || mesh.closed().get()) {
                return null;
            }
            return lastInstancedMeshBatch == batch
                    && lastInstancedMeshSourceMode == sourceMode
                    && lastInstancedMeshSmoothing == smoothing
                    && lastInstancedMeshKind == kind
                    && lastInstancedMeshSprite == sprite
                    && lastInstancedMeshVertices == vertices
                    ? mesh
                    : null;
        }

        private void rememberLastInstancedMesh(PreparedBatch batch, VertexFormat.Mode sourceMode, boolean smoothing,
                GpuMeshKind kind, TextureAtlasSprite sprite, List<PreparedVertex> vertices, InstancedMesh mesh) {
            lastInstancedMeshBatch = batch;
            lastInstancedMeshSourceMode = sourceMode;
            lastInstancedMeshSmoothing = smoothing;
            lastInstancedMeshKind = kind;
            lastInstancedMeshSprite = sprite;
            lastInstancedMeshVertices = vertices;
            lastInstancedMesh = mesh;
        }

        private void clearLastInstancedMesh() {
            lastInstancedMeshBatch = null;
            lastInstancedMeshSourceMode = null;
            lastInstancedMeshSmoothing = false;
            lastInstancedMeshKind = null;
            lastInstancedMeshSprite = null;
            lastInstancedMeshVertices = null;
            lastInstancedMesh = null;
        }

        private InstancedBatch pendingInstancedBatch(InstancedMesh mesh, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode) {
            InstancedBatch cached = lastPendingInstancedBatch;
            if (cached != null
                    && lastPendingInstancedMesh == mesh
                    && lastPendingInstancedRenderMode == renderMode
                    && sameNullable(lastPendingInstancedTexture, textureLocation)) {
                return cached;
            }
            InstancedBatchKey key = new InstancedBatchKey(mesh.key(), textureLocation, renderMode);
            InstancedBatch batch = pendingInstancedBatches.get(key);
            if (batch == null) {
                batch = new InstancedBatch(key, mesh, textureLocation, renderMode);
                pendingInstancedBatches.put(key, batch);
            }
            lastPendingInstancedBatch = batch;
            lastPendingInstancedMesh = mesh;
            lastPendingInstancedTexture = textureLocation;
            lastPendingInstancedRenderMode = renderMode;
            return batch;
        }

        private void clearPendingInstancedBatches() {
            pendingInstancedBatches.clear();
            lastPendingInstancedBatch = null;
            lastPendingInstancedMesh = null;
            lastPendingInstancedTexture = null;
            lastPendingInstancedRenderMode = null;
        }

        private IrisCompanionQueuedBatch pendingIrisCompanionBatch(GpuMeshKind kind, String stablePartKey,
                PreparedGeometryKey geometryKey, TextureAtlasSprite sprite, int sourceVertices,
                VertexFormat.Mode sourceMode,
                boolean smoothing, UvTransform uvTransform, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode, List<PreparedVertex> vertices) {
            IrisCompanionQueuedBatch cached = lastPendingIrisCompanionBatch;
            if (cached != null
                    && lastPendingIrisCompanionKind == kind
                    && sameNullable(lastPendingIrisCompanionGeometryKey, geometryKey)
                    && lastPendingIrisCompanionSprite == sprite
                    && lastPendingIrisCompanionSourceVertices == sourceVertices
                    && lastPendingIrisCompanionSourceMode == sourceMode
                    && lastPendingIrisCompanionSmoothing == smoothing
                    && sameNullable(lastPendingIrisCompanionStablePartKey, stablePartKey)
                    && sameNullable(lastPendingIrisCompanionUvTransform, uvTransform)
                    && sameNullable(lastPendingIrisCompanionTexture, textureLocation)
                    && lastPendingIrisCompanionRenderMode == renderMode) {
                return cached;
            }
            IrisCompanionQueueKey key = new IrisCompanionQueueKey(kind, stablePartKey, geometryKey, sprite,
                    sourceVertices, sourceMode, smoothing, uvTransform, textureLocation, renderMode);
            IrisCompanionQueuedBatch batch = pendingIrisCompanionBatches.get(key);
            if (batch == null) {
                batch = new IrisCompanionQueuedBatch(key, vertices);
                pendingIrisCompanionBatches.put(key, batch);
            }
            lastPendingIrisCompanionBatch = batch;
            lastPendingIrisCompanionKind = kind;
            lastPendingIrisCompanionStablePartKey = stablePartKey;
            lastPendingIrisCompanionGeometryKey = geometryKey;
            lastPendingIrisCompanionSprite = sprite;
            lastPendingIrisCompanionSourceVertices = sourceVertices;
            lastPendingIrisCompanionSourceMode = sourceMode;
            lastPendingIrisCompanionSmoothing = smoothing;
            lastPendingIrisCompanionUvTransform = uvTransform;
            lastPendingIrisCompanionTexture = textureLocation;
            lastPendingIrisCompanionRenderMode = renderMode;
            return batch;
        }

        private static boolean sameNullable(Object left, Object right) {
            return left == right || (left != null && left.equals(right));
        }

        private void clearPendingIrisCompanionBatches() {
            pendingIrisCompanionBatches.clear();
            lastPendingIrisCompanionBatch = null;
            lastPendingIrisCompanionKind = null;
            lastPendingIrisCompanionStablePartKey = null;
            lastPendingIrisCompanionGeometryKey = null;
            lastPendingIrisCompanionSprite = null;
            lastPendingIrisCompanionSourceVertices = 0;
            lastPendingIrisCompanionSourceMode = null;
            lastPendingIrisCompanionSmoothing = false;
            lastPendingIrisCompanionUvTransform = null;
            lastPendingIrisCompanionTexture = null;
            lastPendingIrisCompanionRenderMode = null;
        }

        private boolean canAttemptGpuMeshUpload() {
            if (!RenderSystem.isOnRenderThread()) {
                if (!warnedGpuMeshOffThreadUpload) {
                    warnedGpuMeshOffThreadUpload = true;
                    HbmNtm.LOGGER.warn(
                            "Legacy OBJ GPU mesh upload requested off render thread; using fallback for this frame");
                }
                return false;
            }
            if (HbmInstancedGlCompat.currentCapabilities() == null) {
                if (!warnedGpuMeshGlUnavailableUpload) {
                    warnedGpuMeshGlUnavailableUpload = true;
                    HbmNtm.LOGGER.warn(
                            "Legacy OBJ GPU mesh upload skipped because current GL context is unavailable");
                }
                return false;
            }
            return true;
        }

        private RenderBackendFallbackReason instancedUploadUnavailableReason() {
            if (!RenderSystem.isOnRenderThread()) {
                if (!warnedInstancedOffThreadUpload) {
                    warnedInstancedOffThreadUpload = true;
                    HbmNtm.LOGGER.warn(
                            "Legacy OBJ instanced upload requested off render thread; using fallback for this frame");
                }
                return RenderBackendFallbackReason.GPU_NOT_RENDER_THREAD;
            }
            if (!HbmRenderFrameFlags.current().instancingGlReady()) {
                if (!warnedInstancedGlUnavailableUpload) {
                    warnedInstancedGlUnavailableUpload = true;
                    HbmNtm.LOGGER.warn(
                            "Legacy OBJ instanced upload skipped because current GL context lacks instancing entrypoints");
                }
                return RenderBackendFallbackReason.INSTANCING_UPLOAD_FAILED;
            }
            return RenderBackendFallbackReason.NONE;
        }

        private boolean canAttemptIrisCompanionUpload() {
            if (!RenderSystem.isOnRenderThread()) {
                if (!warnedIrisCompanionOffThreadUpload) {
                    warnedIrisCompanionOffThreadUpload = true;
                    HbmNtm.LOGGER.warn(
                            "Legacy OBJ Iris companion upload requested off render thread; using fallback for this frame");
                }
                return false;
            }
            if (HbmInstancedGlCompat.currentCapabilities() == null) {
                if (!warnedIrisCompanionGlUnavailableUpload) {
                    warnedIrisCompanionGlUnavailableUpload = true;
                    HbmNtm.LOGGER.warn(
                            "Legacy OBJ Iris companion upload skipped because current GL context is unavailable");
                }
                return false;
            }
            return true;
        }

        private InstancedMesh uploadInstancedMesh(InstancedMeshKey key, VertexFormat.Mode sourceMode,
                boolean smoothing, List<PreparedVertex> vertices) {
            InstancedMeshBounds bounds = InstancedMeshBounds.of(vertices);
            ByteBuffer vertexBytes = buildInstancedVertexBytes(key.kind(), sourceMode, vertices, smoothing,
                    key.sprite(), bounds);
            int vao = 0;
            int vbo = 0;
            int instanceVbo = 0;
            int previousVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
            int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
            try {
                vao = GL30.glGenVertexArrays();
                vbo = GL15.glGenBuffers();
                instanceVbo = GL15.glGenBuffers();
                if (vao == 0 || vbo == 0 || instanceVbo == 0) {
                    throw new IllegalStateException("Failed to allocate instanced OBJ GL buffers");
                }
                HbmGlVaoSafety.bindVertexArray(vao);
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
                    HbmInstancedGlCompat.vertexAttribDivisor(attribute, 1);
                }
                return new InstancedMesh(key, sourceMode, List.copyOf(vertices), bounds, lightSampleKey(key),
                        vao, vbo, instanceVbo,
                        vertexBytes.limit() / vertexStride, vertexBytes.limit(), new AtomicInteger(),
                        new AtomicBoolean());
            } catch (RuntimeException exception) {
                Throwable cleanupFailure = null;
                if (vao != 0) {
                    int targetVao = vao;
                    cleanupFailure = GlObjectDeleteGuard.restoreStep(cleanupFailure,
                            () -> GL30.glDeleteVertexArrays(targetVao));
                }
                if (vbo != 0) {
                    int targetVbo = vbo;
                    cleanupFailure = GlObjectDeleteGuard.restoreStep(cleanupFailure,
                            () -> GL15.glDeleteBuffers(targetVbo));
                }
                if (instanceVbo != 0) {
                    int targetInstanceVbo = instanceVbo;
                    cleanupFailure = GlObjectDeleteGuard.restoreStep(cleanupFailure,
                            () -> GL15.glDeleteBuffers(targetInstanceVbo));
                }
                if (cleanupFailure != null) {
                    exception.addSuppressed(cleanupFailure);
                    HbmNtm.LOGGER.error("Legacy OBJ instanced mesh upload cleanup failed", cleanupFailure);
                }
                throw exception;
            } finally {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
                HbmGlVaoSafety.bindVertexArray(previousVao);
            }
        }

        private ByteBuffer buildInstancedVertexBytes(GpuMeshKind kind, VertexFormat.Mode sourceMode,
                List<PreparedVertex> vertices, boolean smoothing, TextureAtlasSprite sprite,
                InstancedMeshBounds bounds) {
            int outputVertices = sourceMode == VertexFormat.Mode.QUADS ? vertices.size() / 4 * 6 : vertices.size();
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
            // Selection batches are evictable.  An identity hash does not retain the batch and may be reused for a
            // different selection later, which would make this cache return an unrelated VBO with valid-looking
            // counts.  Keep the ordinary GPU path on the same stable geometry identity used by instancing/MDI.
            GpuMeshKey key = new GpuMeshKey(GpuMeshKind.TEXTURED, batch.stableKey(), batch.geometryKey(), null,
                    vertices.size(), drawMode, packedLight, packedOverlay, red, green, blue, alpha,
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
                    if (!isGpuMeshTransientUploadFailure(exception)) {
                        failedKeys.add(key);
                    }
                    gpuUploadFailures.incrementAndGet();
                    throw exception;
                }
            }
        }

        private GpuMesh meshForAtlasSprite(PreparedBatch batch, VertexFormat.Mode drawMode, TextureAtlasSprite sprite,
                int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                UvTransform uvTransform, List<PreparedVertex> vertices) {
            GpuMeshKey key = new GpuMeshKey(GpuMeshKind.SPRITE, batch.stableKey(), batch.geometryKey(), sprite,
                    vertices.size(), drawMode, packedLight, packedOverlay, red, green, blue, alpha,
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
                    if (!isGpuMeshTransientUploadFailure(exception)) {
                        failedKeys.add(key);
                    }
                    gpuUploadFailures.incrementAndGet();
                    throw exception;
                }
            }
        }

        private GpuMesh meshForUntextured(PreparedBatch batch, VertexFormat.Mode drawMode, int red, int green,
                int blue, int alpha, List<PreparedVertex> vertices) {
            GpuMeshKey key = new GpuMeshKey(GpuMeshKind.UNTEXTURED, batch.stableKey(), batch.geometryKey(), null,
                    vertices.size(), drawMode, 0, 0, red, green, blue, alpha, UvTransform.DEFAULT, false);
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
                    if (!isGpuMeshTransientUploadFailure(exception)) {
                        failedKeys.add(key);
                    }
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

        private GpuMesh uploadTransientSpriteMesh(VertexFormat.Mode drawMode, TextureAtlasSprite sprite,
                int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                UvTransform uvTransform, List<PreparedVertex> vertices) {
            gpuUploadAttempts.incrementAndGet();
            try {
                return uploadMeshWithSprite(drawMode, sprite, packedLight, packedOverlay, red, green, blue,
                        alpha, uvTransform, vertices);
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
                if (!canAttemptGpuMeshUpload()) {
                    throw new GpuMeshTemporarilyUnavailableException(
                            "GPU mesh upload unavailable in the current render context");
                }
                BufferBuilder builder = new BufferBuilder(256);
                builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                emitUntexturedVertexColorIdentity(builder, quad.v0());
                emitUntexturedVertexColorIdentity(builder, quad.v1());
                emitUntexturedVertexColorIdentity(builder, quad.v2());
                emitUntexturedVertexColorIdentity(builder, quad.v3());
                BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
                return uploadVertexBuffer(renderedBuffer, 64L, "transient untextured vertex-color quad mesh");
            } catch (RuntimeException exception) {
                gpuUploadFailures.incrementAndGet();
                throw exception;
            }
        }

        private GpuMesh uploadTransientUntexturedVertexColorMesh(UntexturedVertexColorTransientTriangle triangle) {
            gpuUploadAttempts.incrementAndGet();
            try {
                if (!canAttemptGpuMeshUpload()) {
                    throw new GpuMeshTemporarilyUnavailableException(
                            "GPU mesh upload unavailable in the current render context");
                }
                BufferBuilder builder = new BufferBuilder(256);
                builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
                emitUntexturedVertexColorIdentity(builder, triangle.v0());
                emitUntexturedVertexColorIdentity(builder, triangle.v1());
                emitUntexturedVertexColorIdentity(builder, triangle.v2());
                BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
                return uploadVertexBuffer(renderedBuffer, 48L, "transient untextured vertex-color triangle mesh");
            } catch (RuntimeException exception) {
                gpuUploadFailures.incrementAndGet();
                throw exception;
            }
        }

        private GpuMesh uploadTransientUntexturedVertexColorMesh(
                List<UntexturedVertexColorTransientTriangle> triangles) {
            gpuUploadAttempts.incrementAndGet();
            try {
                if (!canAttemptGpuMeshUpload()) {
                    throw new GpuMeshTemporarilyUnavailableException(
                            "GPU mesh upload unavailable in the current render context");
                }
                BufferBuilder builder = new BufferBuilder(Math.max(256, triangles.size() * 48));
                builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
                for (UntexturedVertexColorTransientTriangle triangle : triangles) {
                    emitUntexturedVertexColorIdentity(builder, triangle.v0());
                    emitUntexturedVertexColorIdentity(builder, triangle.v1());
                    emitUntexturedVertexColorIdentity(builder, triangle.v2());
                }
                BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
                return uploadVertexBuffer(renderedBuffer, triangles.size() * 48L,
                        "transient untextured vertex-color triangle batch mesh");
            } catch (RuntimeException exception) {
                gpuUploadFailures.incrementAndGet();
                throw exception;
            }
        }

        private GpuMesh uploadTransientUntexturedLineMesh(List<UntexturedLineTransient> lines) {
            gpuUploadAttempts.incrementAndGet();
            try {
                if (!canAttemptGpuMeshUpload()) {
                    throw new GpuMeshTemporarilyUnavailableException(
                            "GPU mesh upload unavailable in the current render context");
                }
                BufferBuilder builder = new BufferBuilder(Math.max(256, lines.size() * 48));
                builder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
                for (UntexturedLineTransient line : lines) {
                    emitLineVertexIdentity(builder, line.x0(), line.y0(), line.z0(), line.color(), line.alpha(),
                            line.normalX(), line.normalY(), line.normalZ());
                    emitLineVertexIdentity(builder, line.x1(), line.y1(), line.z1(), line.color(), line.alpha(),
                            line.normalX(), line.normalY(), line.normalZ());
                }
                BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
                return uploadVertexBuffer(renderedBuffer, Math.max(0L, (long) lines.size() * 48L),
                        "transient line mesh");
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
            if (!canAttemptGpuMeshUpload()) {
                throw new GpuMeshTemporarilyUnavailableException(
                        "GPU mesh upload unavailable in the current render context");
            }
            BufferBuilder builder = new BufferBuilder(Math.max(256, vertices.size() * 40));
            builder.begin(drawMode, DefaultVertexFormat.NEW_ENTITY);
            emitPreparedVertices(vertices, builder, IDENTITY_POSITION, IDENTITY_NORMAL, packedLight, packedOverlay,
                    red, green, blue, alpha, false, smoothing, uvTransform);
            BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
            return uploadVertexBuffer(renderedBuffer, Math.max(0L, (long) vertices.size() * 40L),
                    "cached textured mesh");
        }

        private GpuMesh uploadMeshWithSprite(VertexFormat.Mode drawMode, TextureAtlasSprite sprite, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, UvTransform uvTransform,
                List<PreparedVertex> vertices) {
            if (!canAttemptGpuMeshUpload()) {
                throw new GpuMeshTemporarilyUnavailableException(
                        "GPU mesh upload unavailable in the current render context");
            }
            BufferBuilder builder = new BufferBuilder(Math.max(256, vertices.size() * 40));
            builder.begin(drawMode, DefaultVertexFormat.NEW_ENTITY);
            emitPreparedVerticesWithSprite(vertices, sprite, builder, IDENTITY_POSITION, IDENTITY_NORMAL,
                    packedLight, packedOverlay, red, green, blue, alpha, false, false, uvTransform);
            BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
            return uploadVertexBuffer(renderedBuffer, Math.max(0L, (long) vertices.size() * 40L),
                    "cached sprite mesh");
        }

        private GpuMesh uploadMeshUntextured(VertexFormat.Mode drawMode, int red, int green, int blue, int alpha,
                List<PreparedVertex> vertices) {
            if (!canAttemptGpuMeshUpload()) {
                throw new GpuMeshTemporarilyUnavailableException(
                        "GPU mesh upload unavailable in the current render context");
            }
            BufferBuilder builder = new BufferBuilder(Math.max(256, vertices.size() * 16));
            builder.begin(drawMode, DefaultVertexFormat.POSITION_COLOR);
            emitPreparedVerticesUntextured(vertices, builder, IDENTITY_POSITION, red, green, blue, alpha);
            BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
            return uploadVertexBuffer(renderedBuffer, Math.max(0L, (long) vertices.size() * 16L),
                    "cached untextured mesh");
        }

        private GpuMesh uploadVertexBuffer(BufferBuilder.RenderedBuffer renderedBuffer, long byteSize, String context) {
            VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            GlObjectDeleteGuard guard = GlObjectDeleteGuard.snapshot();
            boolean uploaded = false;
            Throwable uploadFailure = null;
            try {
                vertexBuffer.bind();
                vertexBuffer.upload(renderedBuffer);
                uploaded = true;
                int arrayObjectId = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
                int vertexBufferId = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                return new GpuMesh(vertexBuffer, byteSize, arrayObjectId, vertexBufferId, new AtomicBoolean());
            } catch (RuntimeException | Error throwable) {
                uploadFailure = throwable;
                throw throwable;
            } finally {
                Throwable cleanupFailure = null;
                cleanupFailure = GlObjectDeleteGuard.restoreStep(cleanupFailure, VertexBuffer::unbind);
                if (!uploaded) {
                    cleanupFailure = GlObjectDeleteGuard.restoreStep(cleanupFailure, vertexBuffer::close);
                }
                guard.restoreAfterDeleting(0);
                if (cleanupFailure != null) {
                    if (uploadFailure != null) {
                        uploadFailure.addSuppressed(cleanupFailure);
                        HbmNtm.LOGGER.error("Legacy OBJ {} upload cleanup failed", context, cleanupFailure);
                    } else {
                        HbmNtm.LOGGER.error("Legacy OBJ {} upload state cleanup failed", context, cleanupFailure);
                    }
                }
            }
        }

        private void drawMesh(GpuMesh mesh, RenderType renderType, PoseStack poseStack) {
            OptimizedDrawStateGuard stateGuard = OptimizedDrawStateGuard.snapshot(this, false);
            boolean renderStateSet = false;
            try {
                renderType.setupRenderState();
                renderStateSet = true;
                HbmRenderFrameLight.ensureLightTextureUpdated();
                ShaderInstance shader = RenderSystem.getShader();
                if (shader == null) {
                    throw new IllegalStateException("No shader bound for legacy OBJ GPU mesh");
                }
                mesh.vertexBuffer().bind();
                mesh.vertexBuffer().drawWithShader(poseStack.last().pose(), RenderSystem.getProjectionMatrix(), shader);
            } finally {
                try {
                    VertexBuffer.unbind();
                } finally {
                    if (renderStateSet) {
                        renderType.clearRenderState();
                    }
                    stateGuard.close();
                }
            }
            gpuDrawCalls++;
            currentFrameGpuDrawCalls++;
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
            clearIrisCompanionFallback();
            float fadeAlpha = HbmRenderFrameCulling.currentStaticModelFade();
            if (fadeAlpha < 0.0F) {
                return true;
            }
            LegacyTexturedRenderMode effectiveRenderMode = fadeRenderMode(renderMode, fadeAlpha < 0.999F);
            ShaderInstance shader = HbmIrisExtendedShaderAccess.getBlockEntityShader(shadowPass);
            if (shader == null) {
                recordIrisCompanionFallback(RenderBackendFallbackReason.IRIS_SHADER_UNAVAILABLE, batch.vertexCount(),
                        fallbackDetail("iris-single-shader", RenderBackendFallbackReason.IRIS_SHADER_UNAVAILABLE,
                                batch, textureLocation, effectiveRenderMode, 1));
                return false;
            }
            boolean drew = false;
            irisEligibleBatches++;
            currentFrameIrisEligibleBatches++;
            if (!batch.quadVertices().isEmpty()) {
                IrisCompanionMesh mesh = irisCompanionMeshFor(batch, VertexFormat.Mode.QUADS, kind, sprite,
                        packedLight, packedOverlay, red, green, blue, alpha, smoothing, uvTransform,
                        batch.quadVertices());
                drawIrisCompanionMesh(mesh, textureLocation, effectiveRenderMode, poseStack, packedLight,
                        packedOverlay, red, green, blue, alpha, shader, shadowPass, fadeAlpha);
                drew = true;
            }
            if (!batch.triangleVertices().isEmpty()) {
                IrisCompanionMesh mesh = irisCompanionMeshFor(batch, VertexFormat.Mode.TRIANGLES, kind, sprite,
                        packedLight, packedOverlay, red, green, blue, alpha, smoothing, uvTransform,
                        batch.triangleVertices());
                drawIrisCompanionMesh(mesh, textureLocation, effectiveRenderMode, poseStack, packedLight,
                        packedOverlay, red, green, blue, alpha, shader, shadowPass, fadeAlpha);
                drew = true;
            }
            if (drew) {
                clearIrisCompanionFallback();
            }
            return drew;
        }

        private boolean drawIrisTransientCompanionBatch(PreparedBatch batch, TextureAtlasSprite sprite,
                ResourceLocation textureLocation, PoseStack poseStack, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, boolean smoothing, LegacyTexturedRenderMode renderMode,
                UvTransform uvTransform, GpuMeshKind kind) {
            clearIrisCompanionFallback();
            float fadeAlpha = HbmRenderFrameCulling.currentStaticModelFade();
            if (fadeAlpha < 0.0F) {
                return true;
            }
            LegacyTexturedRenderMode effectiveRenderMode = fadeRenderMode(renderMode, fadeAlpha < 0.999F);
            boolean shadowPass = HbmShaderCompatibilityDetector.isRenderingShadowPass();
            ShaderInstance shader = HbmIrisExtendedShaderAccess.getBlockEntityShader(shadowPass);
            if (shader == null) {
                recordIrisCompanionFallback(RenderBackendFallbackReason.IRIS_SHADER_UNAVAILABLE, batch.vertexCount(),
                        fallbackDetail("iris-transient-shader", RenderBackendFallbackReason.IRIS_SHADER_UNAVAILABLE,
                                batch, textureLocation, effectiveRenderMode, 1));
                return false;
            }
            boolean drew = false;
            irisEligibleBatches++;
            currentFrameIrisEligibleBatches++;
            if (!batch.quadVertices().isEmpty()) {
                drawIrisTransientCompanionPart(batch, VertexFormat.Mode.QUADS, kind, sprite, textureLocation,
                        poseStack, packedLight, packedOverlay, red, green, blue, alpha, smoothing, renderMode,
                        uvTransform, shader, shadowPass, fadeAlpha, batch.quadVertices());
                drew = true;
            }
            if (!batch.triangleVertices().isEmpty()) {
                drawIrisTransientCompanionPart(batch, VertexFormat.Mode.TRIANGLES, kind, sprite, textureLocation,
                        poseStack, packedLight, packedOverlay, red, green, blue, alpha, smoothing, renderMode,
                        uvTransform, shader, shadowPass, fadeAlpha, batch.triangleVertices());
                drew = true;
            }
            if (drew) {
                clearIrisCompanionFallback();
            }
            return drew;
        }

        private boolean drawIrisTransientLineCompanion(List<UntexturedLineTransient> lines,
                PoseStack poseStack, LegacyTexturedRenderMode renderMode, float lineWidth) {
            clearIrisCompanionFallback();
            boolean shadowPass = HbmShaderCompatibilityDetector.isRenderingShadowPass();
            ShaderInstance shader = HbmIrisExtendedShaderAccess.getBlockEntityShader(shadowPass);
            if (shader == null) {
                recordIrisCompanionFallback(RenderBackendFallbackReason.IRIS_SHADER_UNAVAILABLE, lines.size() * 2,
                        "iris-transient-lines: shader unavailable");
                return false;
            }
            IrisCompanionMesh mesh;
            irisEligibleBatches++;
            currentFrameIrisEligibleBatches++;
            try {
                mesh = uploadIrisTransientLineCompanionMesh(lines);
            } catch (RuntimeException exception) {
                irisUploadFailures.incrementAndGet();
                throw exception;
            }
            try {
                drawIrisCompanionMesh(mesh, irisUntexturedWhiteTexture(), renderMode, poseStack,
                        LightTexture.FULL_BRIGHT, 0, 255, 255, 255, 255, shader, shadowPass);
                clearIrisCompanionFallback();
                return true;
            } finally {
                safeClose(mesh, "transient Iris LINE companion mesh");
            }
        }

        private boolean drawIrisTransientVertexColorCompanion(UntexturedVertexColorTransientQuad quad,
                PoseStack poseStack, LegacyTexturedRenderMode renderMode) {
            return drawIrisTransientVertexColorCompanion(List.of(
                    quad.v0(), quad.v1(), quad.v2(), quad.v2(), quad.v3(), quad.v0()),
                    poseStack, renderMode, "quad");
        }

        private boolean drawIrisTransientVertexColorCompanion(UntexturedVertexColorTransientTriangle triangle,
                PoseStack poseStack, LegacyTexturedRenderMode renderMode) {
            return drawIrisTransientVertexColorCompanion(List.of(triangle.v0(), triangle.v1(), triangle.v2()),
                    poseStack, renderMode, "triangle");
        }

        private boolean drawIrisTransientVertexColorTriangleBatchCompanion(
                List<UntexturedVertexColorTransientTriangle> triangles,
                PoseStack poseStack, LegacyTexturedRenderMode renderMode) {
            List<UntexturedVertexColor> vertices = new ArrayList<>(triangles.size() * 3);
            for (UntexturedVertexColorTransientTriangle triangle : triangles) {
                vertices.add(triangle.v0());
                vertices.add(triangle.v1());
                vertices.add(triangle.v2());
            }
            return drawIrisTransientVertexColorCompanion(vertices, poseStack, renderMode, "triangle-batch");
        }

        private boolean drawIrisTransientVertexColorCompanion(List<UntexturedVertexColor> vertices,
                PoseStack poseStack, LegacyTexturedRenderMode renderMode, String detail) {
            clearIrisCompanionFallback();
            boolean shadowPass = HbmShaderCompatibilityDetector.isRenderingShadowPass();
            ShaderInstance shader = HbmIrisExtendedShaderAccess.getBlockEntityShader(shadowPass);
            if (shader == null) {
                recordIrisCompanionFallback(RenderBackendFallbackReason.IRIS_SHADER_UNAVAILABLE, vertices.size(),
                        "iris-transient-vertex-color-" + detail + ": shader unavailable");
                return false;
            }
            IrisCompanionMesh mesh;
            irisEligibleBatches++;
            currentFrameIrisEligibleBatches++;
            try {
                mesh = uploadIrisTransientVertexColorCompanionMesh(vertices, detail);
            } catch (RuntimeException exception) {
                irisUploadFailures.incrementAndGet();
                throw exception;
            }
            try {
                drawIrisCompanionMesh(mesh, irisUntexturedWhiteTexture(), renderMode, poseStack,
                        LightTexture.FULL_BRIGHT, 0, 255, 255, 255, 255, shader, shadowPass);
                clearIrisCompanionFallback();
                return true;
            } finally {
                safeClose(mesh, "transient Iris vertex-color companion mesh");
            }
        }

        private void drawIrisTransientCompanionPart(PreparedBatch batch, VertexFormat.Mode sourceMode,
                GpuMeshKind kind, TextureAtlasSprite sprite, ResourceLocation textureLocation, PoseStack poseStack,
                int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean smoothing,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform, ShaderInstance shader,
                boolean shadowPass, float fadeAlpha, List<PreparedVertex> vertices) {
            LegacyTexturedRenderMode effectiveRenderMode = fadeRenderMode(renderMode, fadeAlpha < 0.999F);
            IrisCompanionMeshKey key = new IrisCompanionMeshKey(kind, "transient:" + batch.stableKey(),
                    batch.geometryKey(), sprite, vertices.size(), sourceMode, smoothing, uvTransform);
            irisUploadAttempts.incrementAndGet();
            IrisCompanionMesh mesh;
            try {
                if (!canAttemptIrisCompanionUpload()) {
                    throw new IrisCompanionTemporarilyUnavailableException(
                            "Iris companion upload unavailable in the current render context");
                }
                mesh = uploadIrisCompanionMesh(key, sourceMode, packedLight, packedOverlay, red, green, blue, alpha,
                        smoothing, vertices);
            } catch (RuntimeException exception) {
                irisUploadFailures.incrementAndGet();
                throw exception;
            }
            try {
                drawIrisCompanionMesh(mesh, textureLocation, effectiveRenderMode, poseStack, packedLight,
                        packedOverlay, red, green, blue, alpha, shader, shadowPass, fadeAlpha);
            } finally {
                safeClose(mesh, "transient Iris companion mesh");
            }
        }

        private IrisCompanionMesh irisCompanionMeshFor(PreparedBatch batch, VertexFormat.Mode sourceMode,
                GpuMeshKind kind, TextureAtlasSprite sprite, int packedLight, int packedOverlay, int red, int green,
                int blue, int alpha, boolean smoothing, UvTransform uvTransform, List<PreparedVertex> vertices) {
            IrisCompanionMeshKey key = new IrisCompanionMeshKey(kind, batch.stableKey(), batch.geometryKey(), sprite,
                    vertices.size(), sourceMode, smoothing, uvTransform);
            return irisCompanionMeshFor(key, sourceMode, packedLight, packedOverlay, red, green, blue, alpha,
                    smoothing, vertices);
        }

        private IrisCompanionMesh irisCompanionMeshFor(IrisCompanionQueueKey queueKey,
                IrisCompanionQueuedInstance instance, List<PreparedVertex> vertices) {
            IrisCompanionMeshKey key = new IrisCompanionMeshKey(queueKey.kind(), queueKey.stablePartKey(),
                    queueKey.geometryKey(), queueKey.sprite(), queueKey.sourceVertices(), queueKey.sourceMode(),
                    queueKey.smoothing(), queueKey.uvTransform());
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
                    if (!canAttemptIrisCompanionUpload()) {
                        throw new IrisCompanionTemporarilyUnavailableException(
                                "Iris companion upload unavailable in the current render context");
                    }
                    IrisCompanionMesh created = uploadIrisCompanionMesh(key, sourceMode, packedLight, packedOverlay,
                            red, green, blue, alpha, smoothing, vertices);
                    irisMeshes.put(key, created);
                    irisMeshBytes.addAndGet(created.byteSize());
                    return created;
                } catch (RuntimeException exception) {
                    if (!isIrisCompanionTransientUploadFailure(exception)) {
                        failedIrisKeys.add(key);
                    }
                    irisUploadFailures.incrementAndGet();
                    throw exception;
                }
            }
        }

        private IrisCompanionMesh uploadIrisTransientLineCompanionMesh(List<UntexturedLineTransient> lines) {
            irisUploadAttempts.incrementAndGet();
            if (!canAttemptIrisCompanionUpload()) {
                throw new IrisCompanionTemporarilyUnavailableException(
                        "Iris companion upload unavailable in the current render context");
            }
            IrisCompanionMeshKey key = new IrisCompanionMeshKey(GpuMeshKind.UNTEXTURED,
                    "transient-lines", PreparedGeometryKey.fromLines(lines), null, lines.size() * 2,
                    VertexFormat.Mode.LINES, false, UvTransform.DEFAULT);
            BufferBuilder builder = new BufferBuilder(Math.max(256, lines.size() * 112));
            builder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.NEW_ENTITY);
            for (UntexturedLineTransient line : lines) {
                emitIrisLineVertex(builder, line.x0(), line.y0(), line.z0(), line.color(), line.alpha(),
                        line.normalX(), line.normalY(), line.normalZ());
                emitIrisLineVertex(builder, line.x1(), line.y1(), line.z1(), line.color(), line.alpha(),
                        line.normalX(), line.normalY(), line.normalZ());
            }
            BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
            InstancedMeshBounds bounds = InstancedMeshBounds.ofLines(lines);
            return createIrisCompanionMesh(key, renderedBuffer, bounds,
                    new float[lines.size() * 6], GL11.GL_LINES, true);
        }

        private IrisCompanionMesh uploadIrisTransientVertexColorCompanionMesh(
                List<UntexturedVertexColor> vertices, String detail) {
            irisUploadAttempts.incrementAndGet();
            if (!canAttemptIrisCompanionUpload()) {
                throw new IrisCompanionTemporarilyUnavailableException(
                        "Iris companion upload unavailable in the current render context");
            }
            if (vertices.isEmpty() || vertices.size() % 3 != 0) {
                throw new IllegalArgumentException("Vertex-color Iris companion requires complete triangles");
            }
            IrisCompanionMeshKey key = new IrisCompanionMeshKey(GpuMeshKind.UNTEXTURED,
                    "transient-vertex-color:" + detail, PreparedGeometryKey.fromVertexColors(vertices), null,
                    vertices.size(),
                    VertexFormat.Mode.TRIANGLES, false, UvTransform.DEFAULT);
            BufferBuilder builder = new BufferBuilder(Math.max(256, vertices.size() * 56));
            builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.NEW_ENTITY);
            float[] normal = new float[3];
            for (int index = 0; index < vertices.size(); index += 3) {
                UntexturedVertexColor v0 = vertices.get(index);
                UntexturedVertexColor v1 = vertices.get(index + 1);
                UntexturedVertexColor v2 = vertices.get(index + 2);
                vertexColorTriangleNormal(v0, v1, v2, normal);
                emitIrisVertexColorVertex(builder, v0, normal);
                emitIrisVertexColorVertex(builder, v1, normal);
                emitIrisVertexColorVertex(builder, v2, normal);
            }
            BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
            InstancedMeshBounds bounds = InstancedMeshBounds.ofVertexColors(vertices);
            return createIrisCompanionMesh(key, renderedBuffer, bounds,
                    new float[vertices.size() * 3], GL11.GL_TRIANGLES, true);
        }

        private static void vertexColorTriangleNormal(UntexturedVertexColor v0, UntexturedVertexColor v1,
                UntexturedVertexColor v2, float[] output) {
            double ax = v1.x() - v0.x();
            double ay = v1.y() - v0.y();
            double az = v1.z() - v0.z();
            double bx = v2.x() - v0.x();
            double by = v2.y() - v0.y();
            double bz = v2.z() - v0.z();
            double normalX = ay * bz - az * by;
            double normalY = az * bx - ax * bz;
            double normalZ = ax * by - ay * bx;
            double length = Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
            if (length <= 1.0E-6D) {
                output[0] = 0.0F;
                output[1] = 1.0F;
                output[2] = 0.0F;
                return;
            }
            output[0] = (float) (normalX / length);
            output[1] = (float) (normalY / length);
            output[2] = (float) (normalZ / length);
        }

        private static void emitIrisVertexColorVertex(VertexConsumer consumer, UntexturedVertexColor vertex,
                float[] normal) {
            consumer.vertex((float) vertex.x(), (float) vertex.y(), (float) vertex.z())
                    .color(vertex.red(), vertex.green(), vertex.blue(), vertex.alpha())
                    .uv(0.0F, 0.0F)
                    .overlayCoords(0)
                    .uv2(LightTexture.FULL_BRIGHT)
                    .normal(normal[0], normal[1], normal[2])
                    .endVertex();
        }

        private static void emitIrisLineVertex(VertexConsumer consumer, double x, double y, double z,
                int color, int alpha, float normalX, float normalY, float normalZ) {
            consumer.vertex((float) x, (float) y, (float) z)
                    .color(color >> 16 & 255, color >> 8 & 255, color & 255, clampColor(alpha))
                    .uv(0.0F, 0.0F)
                    .overlayCoords(0)
                    .uv2(LightTexture.FULL_BRIGHT)
                    .normal(normalX, normalY, normalZ)
                    .endVertex();
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
            if (key.kind() == GpuMeshKind.UNTEXTURED) {
                emitPreparedVertices(triangleVertices, builder, IDENTITY_POSITION, IDENTITY_NORMAL,
                        LightTexture.FULL_BRIGHT, 0, 255, 255, 255, 255, false, false, UvTransform.DEFAULT);
            } else if (key.sprite() == null) {
                emitPreparedVertices(triangleVertices, builder, IDENTITY_POSITION, IDENTITY_NORMAL, packedLight,
                        packedOverlay, red, green, blue, alpha, false, smoothing, key.uvTransform());
            } else {
                emitPreparedVerticesWithSprite(triangleVertices, key.sprite(), builder, IDENTITY_POSITION,
                        IDENTITY_NORMAL, packedLight, packedOverlay, red, green, blue, alpha, false, false,
                        key.uvTransform());
            }
            BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
            InstancedMeshBounds bounds = InstancedMeshBounds.of(triangleVertices);
            return createIrisCompanionMesh(key, renderedBuffer, bounds,
                    buildIrisCompanionLightWeights(triangleVertices, bounds),
                    GL11.GL_TRIANGLES, false);
        }

        private IrisCompanionMesh createIrisCompanionMesh(IrisCompanionMeshKey key,
                BufferBuilder.RenderedBuffer renderedBuffer, InstancedMeshBounds bounds, float[] lightWeights,
                int glDrawMode, boolean preserveVertexColors) {
            BufferBuilder.DrawState drawState = renderedBuffer.drawState();
            VertexFormat actualFormat = drawState.format();
            ByteBuffer vertexBytes = renderedBuffer.vertexBuffer();
            int vao = 0;
            int vbo = 0;
            int previousVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
            int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
            try {
                vao = GL30.glGenVertexArrays();
                vbo = GL15.glGenBuffers();
                if (vao == 0 || vbo == 0) {
                    throw new IllegalStateException("Failed to allocate Iris companion GL buffers");
                }
                HbmGlVaoSafety.bindVertexArray(vao);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBytes, GL15.GL_STATIC_DRAW);
                IrisCompanionMesh mesh = new IrisCompanionMesh(key, vao, vbo, drawState.vertexCount(),
                        Math.max(0L, vertexBytes.limit()), actualFormat, lightWeights, bounds, lightSampleKey(key),
                        glDrawMode, preserveVertexColors,
                        this::recordIrisLightmapSlotReuse, this::recordIrisLightmapSlotUpload,
                        this::recordIrisLightmapStagingFallback, this::recordIrisShaderAttributeCacheHit,
                        this::recordIrisShaderAttributeCacheMiss,
                        this::recordIrisShaderAttributeGenerationInvalidation,
                        this::recordIrisShaderAttributePrimedSkip,
                        this::recordIrisShaderAttributeVaoBindFailure);
                mesh.bindStandardAttributes();
                return mesh;
            } catch (RuntimeException exception) {
                Throwable cleanupFailure = null;
                if (vbo != 0) {
                    int targetVbo = vbo;
                    cleanupFailure = GlObjectDeleteGuard.restoreStep(cleanupFailure,
                            () -> GL15.glDeleteBuffers(targetVbo));
                }
                if (vao != 0) {
                    int targetVao = vao;
                    cleanupFailure = GlObjectDeleteGuard.restoreStep(cleanupFailure,
                            () -> GL30.glDeleteVertexArrays(targetVao));
                }
                if (cleanupFailure != null) {
                    exception.addSuppressed(cleanupFailure);
                    HbmNtm.LOGGER.error("Legacy OBJ Iris companion mesh upload cleanup failed", cleanupFailure);
                }
                throw exception;
            } finally {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
                HbmGlVaoSafety.bindVertexArray(previousVao);
            }
        }

        private static float[] buildIrisCompanionLightWeights(List<PreparedVertex> vertices,
                InstancedMeshBounds bounds) {
            if (vertices.isEmpty()) {
                return new float[0];
            }
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
                    packedOverlay, red, green, blue, alpha, shader, shadowPass, -1, 1.0F);
        }

        private void drawIrisCompanionMesh(IrisCompanionMesh mesh, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode, PoseStack poseStack, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, ShaderInstance shader, boolean shadowPass,
                float fadeAlpha) {
            drawIrisCompanionMesh(mesh, textureLocation, renderMode, poseStack.last().pose(), packedLight,
                    packedOverlay, red, green, blue, alpha, shader, shadowPass, -1, fadeAlpha);
        }

        private void drawIrisCompanionMesh(IrisCompanionMesh mesh, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode, Matrix4f modelView, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, ShaderInstance shader, boolean shadowPass) {
            drawIrisCompanionMesh(mesh, textureLocation, renderMode, modelView, packedLight, packedOverlay,
                    red, green, blue, alpha, shader, shadowPass, -1, 1.0F);
        }

        private void drawIrisCompanionMesh(IrisCompanionMesh mesh, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode, Matrix4f modelView, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, ShaderInstance shader, boolean shadowPass,
                int preparedLightmapSlot) {
            drawIrisCompanionMesh(mesh, textureLocation, renderMode, modelView, packedLight, packedOverlay,
                    red, green, blue, alpha, shader, shadowPass, preparedLightmapSlot, null, 1.0F);
        }

        private void drawIrisCompanionMesh(IrisCompanionMesh mesh, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode, Matrix4f modelView, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, ShaderInstance shader, boolean shadowPass,
                int preparedLightmapSlot, float fadeAlpha) {
            drawIrisCompanionMesh(mesh, textureLocation, renderMode, modelView, packedLight, packedOverlay,
                    red, green, blue, alpha, shader, shadowPass, preparedLightmapSlot, null, fadeAlpha);
        }

        private void drawIrisCompanionMesh(IrisCompanionMesh mesh, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode, Matrix4f modelView, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, ShaderInstance shader, boolean shadowPass,
                int preparedLightmapSlot, Matrix4f projectionMatrix) {
            drawIrisCompanionMesh(mesh, textureLocation, renderMode, modelView, packedLight, packedOverlay,
                    red, green, blue, alpha, shader, shadowPass, preparedLightmapSlot, projectionMatrix, 1.0F);
        }

        private void drawIrisCompanionMesh(IrisCompanionMesh mesh, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode, Matrix4f modelView, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, ShaderInstance shader, boolean shadowPass,
                int preparedLightmapSlot, Matrix4f projectionMatrix, float fadeAlpha) {
            RenderType renderType = mesh.linePrimitive()
                    ? LegacyLineRenderer.type(LegacyLineRenderer.DEFAULT_LINE_WIDTH, renderMode, alpha)
                    : renderMode.renderType(textureLocation, VertexFormat.Mode.TRIANGLES);
            int previousVao = HbmGlVaoSafety.currentBinding();
            int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
            IrisRenderBatchKey batchKey = new IrisRenderBatchKey(textureLocation, renderMode, shadowPass,
                    mesh.linePrimitive());
            boolean success = false;
            try {
                if (!shadowPass) {
                    HbmRenderFrameLight.ensureLightTextureUpdated();
                }
                if (!HbmIrisRenderBatch.begin(batchKey, renderType, shader, projectionMatrix, shadowPass,
                        textureLocation)) {
                    throw new IllegalStateException("Iris/Oculus ExtendedShader batch begin failed");
                }
                if (shader.MODEL_VIEW_MATRIX != null) {
                    shader.MODEL_VIEW_MATRIX.set(modelView);
                }
                mesh.bind();
                mesh.prepareForShader(shader);
                int lightmapSlot = preparedLightmapSlot;
                if (!shadowPass && lightmapSlot < 0 && requiresPerVertexLightmap(packedLight)) {
                    lightmapSlot = mesh.preparePerVertexLightmapSlot(modelView, packedLight,
                            HbmRenderFrameFlags.current().useSlicedLight());
                    if (mesh.consumeLightmapStorageFailureFlag()) {
                        recordIrisLightmapStorageFailure();
                    }
                }
                if (!shadowPass) {
                    mesh.finishPreparedLightmapWrites();
                }
                mesh.applyDrawAttributes(packedLight, packedOverlay, red, green, blue, alpha, !shadowPass,
                        lightmapSlot);
                if (!HbmIrisRenderBatch.prepareCompanionDraw()) {
                    throw new IllegalStateException("Iris/Oculus companion shader restore failed before draw");
                }
                setIrisCompanionFadeAlpha(shader, fadeAlpha);
                HbmIrisRenderBatch.uploadDrawMatrices(modelView);
                mesh.bindVaoIfNeeded();
                GL11.glDrawArrays(mesh.glDrawMode(), 0, mesh.vertexCount());
                HbmIrisRenderBatch.recordDraw(shadowPass);
                irisDrawCalls++;
                currentFrameIrisDrawCalls++;
                if (shadowPass) {
                    irisShadowDrawCalls++;
                    currentFrameIrisShadowDrawCalls++;
                }
                gpuDrawCalls++;
                currentFrameGpuDrawCalls++;
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

        private void flushInstancedBatches(Matrix4f projectionMatrix) {
            if (pendingInstancedBatches.isEmpty()) {
                return;
            }
            List<InstancedBatch> batches = instancedFlushScratch;
            batches.clear();
            batches.addAll(pendingInstancedBatches.values());
            clearPendingInstancedBatches();
            try {
                instancedFlushes++;
                coalesceDuplicateInstancedInstances(batches);
                sortInstancedTailBatches(batches);
                if (drawMdiBatchesIfAvailable(batches, projectionMatrix)) {
                    return;
                }
                for (InstancedBatch batch : batches) {
                    if (batch.instances().isEmpty()) {
                        continue;
                    }
                    try {
                        if (!drawInstancedBatch(batch, projectionMatrix)) {
                            drawInstancedCpuFallback(batch,
                                    RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE);
                        }
                    } catch (InstancedBatchDrawException exception) {
                        int fallbackStart = exception.fallbackStartIndex();
                        int fallbackInstances = instancedFallbackCount(batch, fallbackStart);
                        recordInstancedFallback(RenderBackendFallbackReason.INSTANCING_DRAW_FAILED,
                                fallbackInstances, batch.renderMode());
                        drawInstancedCpuFallback(batch, RenderBackendFallbackReason.INSTANCING_DRAW_FAILED,
                                fallbackStart);
                        HbmNtm.LOGGER.debug(
                                "Failed to draw legacy OBJ instanced batch {} after {} submitted instance(s); CPU fallback covers remaining {} instance(s)",
                                batch.key(), Math.min(fallbackStart, batch.instances().size()), fallbackInstances,
                                exception.getCause() != null ? exception.getCause() : exception);
                    } catch (RuntimeException exception) {
                        recordInstancedFallback(RenderBackendFallbackReason.INSTANCING_DRAW_FAILED,
                                batch.instances().size(), batch.renderMode());
                        drawInstancedCpuFallback(batch, RenderBackendFallbackReason.INSTANCING_DRAW_FAILED);
                        HbmNtm.LOGGER.debug("Failed to draw legacy OBJ instanced batch {}", batch.key(), exception);
                    }
                }
            } finally {
                batches.clear();
            }
        }

        private void flushIrisCompanionBatches(Matrix4f projectionMatrix) {
            if (pendingIrisCompanionBatches.isEmpty()) {
                return;
            }
            List<IrisCompanionQueuedBatch> batches = irisCompanionFlushScratch;
            batches.clear();
            batches.addAll(pendingIrisCompanionBatches.values());
            clearPendingIrisCompanionBatches();
            try {
                coalesceDuplicateIrisCompanionInstances(batches);
                irisQueuedFlushes++;
                currentFrameIrisQueuedFlushes++;
                boolean shadowPass = HbmShaderCompatibilityDetector.isRenderingShadowPass();
                ShaderInstance shader = HbmIrisExtendedShaderAccess.getBlockEntityShader(shadowPass);
                if (shader == null) {
                    drawIrisQueuedCpuFallbacks(batches, RenderBackendFallbackReason.IRIS_SHADER_UNAVAILABLE);
                    return;
                }
                List<IrisCompanionQueuedDraw> normalAlphaDraws = null;
                for (IrisCompanionQueuedBatch batch : batches) {
                    if (batch.instances().isEmpty()) {
                        continue;
                    }
                    if (isIrisCompanionNormalAlphaMode(batch.key().renderMode())) {
                        recordIrisEligibleBatch();
                        try {
                            if (normalAlphaDraws == null) {
                                normalAlphaDraws = irisNormalAlphaDrawScratch;
                                normalAlphaDraws.clear();
                            }
                            collectIrisQueuedDrawsWithPreparedLightmapSlots(batch, normalAlphaDraws, !shadowPass);
                        } catch (RuntimeException exception) {
                            drawIrisQueuedCpuFallback(batch, RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED);
                            HbmNtm.LOGGER.debug(
                                    "Failed to prepare legacy OBJ queued Iris normal-alpha companion batch {}",
                                    batch.key(), exception);
                        }
                        continue;
                    }
                    try {
                        drawIrisQueuedBatch(batch, shader, projectionMatrix, shadowPass);
                    } catch (IrisCompanionQueuedBatchDrawException exception) {
                        int fallbackStart = exception.fallbackStartIndex();
                        List<IrisCompanionQueuedInstance> fallbackInstances =
                                irisQueuedFallbackInstances(batch, fallbackStart);
                        drawIrisQueuedCpuFallback(batch, fallbackInstances,
                                RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED);
                        HbmNtm.LOGGER.debug(
                                "Failed to draw legacy OBJ queued Iris companion batch {} after {} submitted instance(s); CPU fallback covers remaining {} instance(s)",
                                batch.key(), Math.min(fallbackStart, batch.instances().size()),
                                fallbackInstances.size(),
                                exception.getCause() != null ? exception.getCause() : exception);
                    } catch (RuntimeException exception) {
                        drawIrisQueuedCpuFallback(batch, RenderBackendFallbackReason.IRIS_COMPANION_DRAW_FAILED);
                        HbmNtm.LOGGER.debug("Failed to draw legacy OBJ queued Iris companion batch {}",
                                batch.key(), exception);
                    }
                }
                drawIrisNormalAlphaQueuedDraws(normalAlphaDraws, shader, projectionMatrix, shadowPass);
            } finally {
                irisNormalAlphaDrawScratch.clear();
                batches.clear();
            }
        }

        private void drawIrisQueuedBatch(IrisCompanionQueuedBatch batch, ShaderInstance shader,
                Matrix4f projectionMatrix, boolean shadowPass) {
            recordIrisEligibleBatch();
            int submittedInstances = 0;
            try {
                IrisCompanionMesh mesh = irisCompanionMeshFor(batch.key(), batch.instances().get(0),
                        batch.sourceVertices());
                int[] lightmapSlots = prepareIrisQueuedLightmapSlots(mesh, batch.instances(), !shadowPass);
                for (int i = 0; i < batch.instances().size(); i++) {
                    drawIrisQueuedInstance(batch, batch.instances().get(i), shader, mesh,
                            preparedLightmapSlot(lightmapSlots, i),
                            projectionMatrix, shadowPass);
                    submittedInstances = i + 1;
                }
            } catch (RuntimeException exception) {
                throw new IrisCompanionQueuedBatchDrawException(exception, submittedInstances);
            }
        }

        private int[] prepareIrisQueuedLightmapSlots(IrisCompanionMesh mesh,
                List<IrisCompanionQueuedInstance> instances, boolean preparePerVertexLightmap) {
            if (!preparePerVertexLightmap) {
                return NO_LIGHTMAP_SLOTS;
            }
            int firstPerVertexLightmapIndex = -1;
            for (int i = 0; i < instances.size(); i++) {
                if (requiresPerVertexLightmap(instances.get(i).packedLight())) {
                    firstPerVertexLightmapIndex = i;
                    break;
                }
            }
            if (firstPerVertexLightmapIndex < 0) {
                return NO_LIGHTMAP_SLOTS;
            }
            if (!mesh.ensureLightmapSlotStorage(instances.size())) {
                recordIrisLightmapStorageFailure();
                return NO_LIGHTMAP_SLOTS;
            }
            int[] slots = irisQueuedLightmapSlotScratch(instances.size());
            boolean useSlicedLight = HbmRenderFrameFlags.current().useSlicedLight();
            for (int i = firstPerVertexLightmapIndex; i < instances.size(); i++) {
                IrisCompanionQueuedInstance instance = instances.get(i);
                if (!requiresPerVertexLightmap(instance.packedLight())) {
                    continue;
                }
                slots[i] = mesh.preparePerVertexLightmapSlot(instance.position(), instance.packedLight(),
                        instances.size(), useSlicedLight);
                if (mesh.consumeLightmapStorageFailureFlag()) {
                    recordIrisLightmapStorageFailure();
                }
            }
            mesh.finishPreparedLightmapWrites();
            return slots;
        }

        private int[] irisQueuedLightmapSlotScratch(int count) {
            if (irisQueuedLightmapSlotScratch.length < count) {
                irisQueuedLightmapSlotScratch = new int[count];
            }
            Arrays.fill(irisQueuedLightmapSlotScratch, 0, count, -1);
            return irisQueuedLightmapSlotScratch;
        }

        private static int preparedLightmapSlot(int[] slots, int index) {
            return slots.length == 0 ? -1 : slots[index];
        }

        private void recordIrisEligibleBatch() {
            irisEligibleBatches++;
            currentFrameIrisEligibleBatches++;
        }

        private static boolean requiresPerVertexLightmap(int packedLight) {
            return packedLight != LightTexture.FULL_BRIGHT;
        }

        private void collectIrisQueuedDraws(IrisCompanionQueuedBatch batch, List<IrisCompanionQueuedDraw> draws) {
            for (IrisCompanionQueuedInstance instance : batch.instances()) {
                draws.add(new IrisCompanionQueuedDraw(batch, instance, null, -1));
            }
        }

        private void collectIrisQueuedDrawsWithPreparedLightmapSlots(IrisCompanionQueuedBatch batch,
                List<IrisCompanionQueuedDraw> draws, boolean preparePerVertexLightmap) {
            if (batch.instances().isEmpty()) {
                return;
            }
            IrisCompanionMesh mesh = irisCompanionMeshFor(batch.key(), batch.instances().get(0),
                    batch.sourceVertices());
            int[] lightmapSlots = prepareIrisQueuedLightmapSlots(mesh, batch.instances(), preparePerVertexLightmap);
            for (int i = 0; i < batch.instances().size(); i++) {
                draws.add(new IrisCompanionQueuedDraw(batch, batch.instances().get(i), mesh,
                        preparedLightmapSlot(lightmapSlots, i)));
            }
        }

        private void drawIrisNormalAlphaQueuedDraws(List<IrisCompanionQueuedDraw> draws, ShaderInstance shader,
                Matrix4f projectionMatrix, boolean shadowPass) {
            if (draws == null || draws.isEmpty()) {
                return;
            }
            draws.sort((left, right) -> Float.compare(right.instance().sortDepthSq(), left.instance().sortDepthSq()));
            for (IrisCompanionQueuedDraw draw : draws) {
                try {
                    if (draw.mesh() != null) {
                        drawIrisQueuedInstance(draw.batch(), draw.instance(), shader, draw.mesh(),
                                draw.preparedLightmapSlot(), projectionMatrix, shadowPass);
                    } else {
                        drawIrisQueuedInstance(draw.batch(), draw.instance(), shader, projectionMatrix, shadowPass);
                    }
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
                ShaderInstance shader, Matrix4f projectionMatrix) {
            IrisCompanionMesh mesh = irisCompanionMeshFor(batch.key(), instance, batch.sourceVertices());
            drawIrisQueuedInstance(batch, instance, shader, mesh, -1, projectionMatrix,
                    HbmShaderCompatibilityDetector.isRenderingShadowPass());
        }

        private void drawIrisQueuedInstance(IrisCompanionQueuedBatch batch, IrisCompanionQueuedInstance instance,
                ShaderInstance shader, Matrix4f projectionMatrix, boolean shadowPass) {
            IrisCompanionMesh mesh = irisCompanionMeshFor(batch.key(), instance, batch.sourceVertices());
            drawIrisQueuedInstance(batch, instance, shader, mesh, -1, projectionMatrix, shadowPass);
        }

        private void drawIrisQueuedInstance(IrisCompanionQueuedBatch batch, IrisCompanionQueuedInstance instance,
                ShaderInstance shader, IrisCompanionMesh mesh, int preparedLightmapSlot) {
            drawIrisQueuedInstance(batch, instance, shader, mesh, preparedLightmapSlot, null);
        }

        private void drawIrisQueuedInstance(IrisCompanionQueuedBatch batch, IrisCompanionQueuedInstance instance,
                ShaderInstance shader, IrisCompanionMesh mesh, int preparedLightmapSlot, Matrix4f projectionMatrix) {
            drawIrisQueuedInstance(batch, instance, shader, mesh, preparedLightmapSlot, projectionMatrix,
                    HbmShaderCompatibilityDetector.isRenderingShadowPass());
        }

        private void drawIrisQueuedInstance(IrisCompanionQueuedBatch batch, IrisCompanionQueuedInstance instance,
                ShaderInstance shader, IrisCompanionMesh mesh, int preparedLightmapSlot, Matrix4f projectionMatrix,
                boolean shadowPass) {
            drawIrisCompanionMesh(mesh, batch.key().textureLocation(), batch.key().renderMode(),
                    instance.position(), instance.packedLight(), instance.packedOverlay(), instance.red(),
                    instance.green(), instance.blue(), instance.alpha(), shader, shadowPass,
                    preparedLightmapSlot, projectionMatrix, instance.fadeAlpha());
            irisQueuedDrawCalls++;
            currentFrameIrisQueuedDrawCalls++;
        }

        private void drawIrisQueuedCpuFallbacks(List<IrisCompanionQueuedBatch> batches,
                RenderBackendFallbackReason reason) {
            List<IrisCompanionQueuedDraw> normalAlphaDraws = null;
            for (IrisCompanionQueuedBatch batch : batches) {
                if (batch.instances().isEmpty()) {
                    continue;
                }
                if (isIrisCompanionNormalAlphaMode(batch.key().renderMode())) {
                    if (normalAlphaDraws == null) {
                        normalAlphaDraws = irisNormalAlphaDrawScratch;
                        normalAlphaDraws.clear();
                    }
                    collectIrisQueuedDraws(batch, normalAlphaDraws);
                    continue;
                }
                drawIrisQueuedCpuFallback(batch, reason);
            }
            if (normalAlphaDraws == null || normalAlphaDraws.isEmpty()) {
                return;
            }
            try {
                normalAlphaDraws.sort((left, right) -> Float.compare(right.instance().sortDepthSq(),
                        left.instance().sortDepthSq()));
                for (IrisCompanionQueuedDraw draw : normalAlphaDraws) {
                    drawIrisQueuedCpuFallback(draw.batch(), draw.instance(), reason);
                }
            } finally {
                irisNormalAlphaDrawScratch.clear();
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
            String detail = fallbackDetail("iris-queued-cpu", reason, batch, instances.size());
            recordIrisFallback(reason, fallbackVertices, detail);
            recordGpuFallback(reason, fallbackVertices, detail);
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
                int fadedAlpha = fadedAlpha(instance.alpha(), instance.fadeAlpha());
                if (untextured) {
                    emitPreparedVerticesUntextured(batch.sourceVertices(), consumer, instance.position(),
                            instance.red(), instance.green(), instance.blue(), fadedAlpha);
                } else if (sprite == null) {
                    emitPreparedVertices(batch.sourceVertices(), consumer, instance.position(), instance.normal(),
                            instance.packedLight(), instance.packedOverlay(), instance.red(), instance.green(),
                            instance.blue(), fadedAlpha, false, batch.key().smoothing(),
                            batch.key().uvTransform());
                } else {
                    emitPreparedVerticesWithSprite(batch.sourceVertices(), sprite, consumer, instance.position(),
                            instance.normal(), instance.packedLight(), instance.packedOverlay(), instance.red(),
                            instance.green(), instance.blue(), fadedAlpha, false, false,
                            batch.key().uvTransform());
                }
            }
        }

        private static List<IrisCompanionQueuedInstance> irisQueuedFallbackInstances(
                IrisCompanionQueuedBatch batch, int startIndex) {
            int start = Math.max(0, Math.min(startIndex, batch.instances().size()));
            if (start >= batch.instances().size()) {
                return List.of();
            }
            return batch.instances().subList(start, batch.instances().size());
        }

        private boolean drawMdiBatchesIfAvailable(List<InstancedBatch> batches, Matrix4f projectionMatrix) {
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            if (!flags.mdiEnabled() || batches.isEmpty()) {
                return false;
            }
            List<InstancedBatch> eligible = null;
            try {
                boolean allEligible = true;
                int eligibleBatches = 0;
                int eligibleInstances = 0;
                int additiveEligibleBatches = 0;
                int additiveEligibleInstances = 0;
                boolean needsTexturedShader = false;
                boolean needsUntexturedShader = false;
                for (int i = 0; i < batches.size(); i++) {
                    InstancedBatch batch = batches.get(i);
                    if (isMdiEligibleBatch(batch)) {
                        eligibleBatches++;
                        int instances = batch.instances().size();
                        eligibleInstances += instances;
                        if (isInstancedAdditiveMode(batch.renderMode())) {
                            additiveEligibleBatches++;
                            additiveEligibleInstances += instances;
                        }
                        if (batch.mesh().key().kind() == GpuMeshKind.UNTEXTURED) {
                            needsUntexturedShader = true;
                        } else {
                            needsTexturedShader = true;
                        }
                        if (!allEligible) {
                            if (eligible == null) {
                                eligible = mdiEligibleScratch();
                            }
                            eligible.add(batch);
                        }
                    } else if (allEligible) {
                        allEligible = false;
                        if (eligibleBatches > 0) {
                            eligible = mdiEligibleScratch();
                            eligible.addAll(batches.subList(0, i));
                        }
                    }
                }
                if (eligibleBatches <= 0) {
                    return false;
                }
                List<InstancedBatch> mdiBatches = allEligible ? batches : eligible;
                mdiEligibleFlushes++;
                mdiEligibleBatches += eligibleBatches;
                currentFrameMdiEligibleFlushes++;
                currentFrameMdiEligibleBatches += eligibleBatches;
                recordMdiAdditiveEligible(additiveEligibleBatches);
                ensureMdiCapabilities();
                if (!mdiAvailable()) {
                    recordMdiFallback(eligibleBatches, mdiBatches,
                            RenderBackendFallbackReason.MDI_UNAVAILABLE, "mdi-unavailable", eligibleInstances);
                    recordMdiAdditiveFallback(additiveEligibleBatches);
                    return false;
                }
                try {
                    try {
                        MdiDrawResult drawResult = drawMdiBatches(mdiBatches, projectionMatrix, flags,
                                needsTexturedShader, needsUntexturedShader, eligibleInstances,
                                additiveEligibleInstances);
                        if (!drawResult.drew()) {
                            recordMdiFallback(eligibleBatches);
                            recordMdiAdditiveFallback(additiveEligibleBatches);
                            return false;
                        }
                        removeDrawnMdiBatches(batches, drawResult.drawnBatches());
                        return batches.isEmpty();
                    } finally {
                        clearMdiPrepareScratch();
                    }
                } catch (MdiPartialDrawException exception) {
                    recordMdiPartialDrawFailure();
                    disableMdiDispatchAfterFailure();
                    HbmNtm.LOGGER.error(
                            "Legacy OBJ MDI dispatch failed after draw submission; suppressing fallback only for submitted MDI groups and using ordinary instancing for remaining batches until backend clear",
                            exception);
                    removeDrawnMdiBatches(batches, exception.suppressFallbackBatches());
                    return batches.isEmpty();
                } catch (RuntimeException exception) {
                    disableMdiDispatchAfterFailure();
                    recordMdiFallback(eligibleBatches, mdiBatches,
                            RenderBackendFallbackReason.MDI_UNAVAILABLE, "mdi-dispatch", eligibleInstances);
                    recordMdiAdditiveFallback(additiveEligibleBatches);
                    HbmNtm.LOGGER.error(
                            "Legacy OBJ MDI dispatch failed before draw submission; using ordinary instancing for future flushes until backend clear",
                            exception);
                    return false;
                }
            } finally {
                clearMdiEligibleScratch();
                clearMdiDrawGroupScratch();
            }
        }

        private List<InstancedBatch> mdiEligibleScratch() {
            mdiEligibleBatchScratch.clear();
            return mdiEligibleBatchScratch;
        }

        private void clearMdiEligibleScratch() {
            mdiEligibleBatchScratch.clear();
        }

        private void clearMdiPrepareScratch() {
            mdiPreparedGroupScratch.clear();
            mdiPreparedBatchScratch.clear();
            mdiNoSlotBatchScratch.clear();
        }

        private void clearMdiDrawGroupScratch() {
            mdiDrawGroupScratch.clear();
            for (int i = 0; i < mdiDrawGroupPoolCursor; i++) {
                mdiDrawGroupPool.get(i).clear();
            }
            mdiDrawGroupPoolCursor = 0;
        }

        private static boolean isMdiEligibleBatch(InstancedBatch batch) {
            return !batch.instances().isEmpty()
                    && !isInstancedNormalAlphaMode(batch.renderMode())
                    && !isGlintRenderMode(batch.renderMode());
        }

        private void removeDrawnMdiBatches(List<InstancedBatch> batches, List<InstancedBatch> drawnBatches) {
            if (batches.isEmpty() || drawnBatches.isEmpty()) {
                return;
            }
            if (batches == drawnBatches) {
                batches.clear();
                return;
            }
            if (batches.size() == drawnBatches.size()) {
                boolean sameOrder = true;
                for (int i = 0; i < batches.size(); i++) {
                    if (batches.get(i) != drawnBatches.get(i)) {
                        sameOrder = false;
                        break;
                    }
                }
                if (sameOrder) {
                    batches.clear();
                    return;
                }
            }
            Set<InstancedBatch> drawn = mdiDrawnBatchScratch;
            try {
                drawn.clear();
                drawn.addAll(drawnBatches);
                batches.removeIf(drawn::contains);
            } finally {
                drawn.clear();
            }
        }

        private MdiDrawResult drawMdiBatches(List<InstancedBatch> batches, Matrix4f projectionMatrix,
                HbmRenderFrameFlags.Snapshot flags, boolean needsTexturedShader, boolean needsUntexturedShader,
                int totalInstances, int additiveInstances) {
            if (!allMdiShadersReady(needsTexturedShader, needsUntexturedShader)) {
                recordInstancedFallback(RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE,
                        totalInstances,
                        fallbackDetail("mdi-shader", RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE,
                                batches, totalInstances));
                recordInstancedAdditiveFallback(additiveInstances);
                return MdiDrawResult.FAILED;
            }
            MdiDrawGroups drawGroups = collectMdiDrawGroups(batches, totalInstances);
            clearMdiPrepareScratch();
            List<MdiPreparedGroup> preparedGroups = mdiPreparedGroupScratch;
            List<InstancedBatch> preparedBatches = mdiPreparedBatchScratch;
            List<InstancedBatch> noSlotBatches = mdiNoSlotBatchScratch;
            if (!prepareMdiGroups(drawGroups, preparedGroups, preparedBatches, noSlotBatches)) {
                if (!noSlotBatches.isEmpty()) {
                    recordMdiNoSlot(noSlotBatches);
                } else {
                    recordMdiFallbackDetail(RenderBackendFallbackReason.MDI_UNAVAILABLE, "mdi-prepare",
                            batches, totalInstances);
                }
                return MdiDrawResult.FAILED;
            }
            if (recordStaleMdiPreparedGroups(preparedGroups, "prepare")) {
                preparedGroups.clear();
                preparedBatches.clear();
                noSlotBatches.clear();
                if (!prepareMdiGroups(drawGroups, preparedGroups, preparedBatches, noSlotBatches)) {
                    if (!noSlotBatches.isEmpty()) {
                        recordMdiNoSlot(noSlotBatches);
                    } else {
                        recordMdiFallbackDetail(RenderBackendFallbackReason.MDI_UNAVAILABLE, "mdi-reprepare",
                                batches, totalInstances);
                    }
                    return MdiDrawResult.FAILED;
                }
                if (recordStaleMdiPreparedGroups(preparedGroups, "reprepare")) {
                    recordMdiNoSlot(preparedBatches);
                    recordMdiNoSlot(noSlotBatches);
                    return MdiDrawResult.FAILED;
                }
            }
            if (preparedBatches.isEmpty()) {
                return MdiDrawResult.FAILED;
            }
            recordMdiNoSlot(noSlotBatches);
            int pendingBatchCount = preparedBatches.size() + noSlotBatches.size();
            int droppedNoSlotCount = noSlotBatches.size();
            boolean drew = false;
            int completedGroups = 0;
            for (int groupIndex = 0; groupIndex < preparedGroups.size(); groupIndex++) {
                MdiPreparedGroup prepared = preparedGroups.get(groupIndex);
                try {
                    drawMdiPreparedGroup(prepared, projectionMatrix, pendingBatchCount, droppedNoSlotCount, flags);
                    drew = true;
                    completedGroups++;
                } catch (MdiPreparedGroupDrawException exception) {
                    if (drew || exception.drawSubmitted()) {
                        List<InstancedBatch> suppressFallbackBatches = submittedMdiBatches(preparedGroups,
                                completedGroups, prepared, exception.submittedCommandCount());
                        throw new MdiPartialDrawException(exception, suppressFallbackBatches);
                    }
                    throw exception;
                }
            }
            return drew ? new MdiDrawResult(true, preparedBatches) : MdiDrawResult.FAILED;
        }

        private MdiDrawGroups collectMdiDrawGroups(List<InstancedBatch> batches, int totalInstances) {
            clearMdiDrawGroupScratch();
            if (batches.isEmpty()) {
                return MdiDrawGroups.EMPTY;
            }
            MdiDrawGroupKey firstKey = mdiDrawGroupKey(batches.get(0));
            int firstGroupInstances = batches.get(0).instances().size();
            Map<MdiDrawGroupKey, MdiDrawGroup> groups = null;
            for (int i = 1; i < batches.size(); i++) {
                InstancedBatch batch = batches.get(i);
                int batchInstances = batch.instances().size();
                if (groups == null && sameMdiDrawGroup(batch, firstKey)) {
                    firstGroupInstances += batchInstances;
                    continue;
                }
                MdiDrawGroupKey key = mdiDrawGroupKey(batch);
                if (groups == null) {
                    groups = mdiDrawGroupScratch;
                    MdiDrawGroup firstGroup = acquireMdiDrawGroup(firstKey);
                    firstGroup.addAll(batches, 0, i, firstGroupInstances);
                    groups.put(firstKey, firstGroup);
                }
                MdiDrawGroup group = groups.get(key);
                if (group == null) {
                    group = acquireMdiDrawGroup(key);
                    groups.put(key, group);
                }
                group.add(batch, batchInstances);
            }
            if (groups == null) {
                return new MdiDrawGroups(new MdiDrawGroup(firstKey, batches, totalInstances), Map.of());
            }
            return new MdiDrawGroups(null, groups);
        }

        private MdiDrawGroup acquireMdiDrawGroup(MdiDrawGroupKey key) {
            MdiDrawGroup group;
            if (mdiDrawGroupPoolCursor < mdiDrawGroupPool.size()) {
                group = mdiDrawGroupPool.get(mdiDrawGroupPoolCursor);
            } else {
                group = new MdiDrawGroup();
                mdiDrawGroupPool.add(group);
            }
            mdiDrawGroupPoolCursor++;
            group.reset(key);
            return group;
        }

        private static MdiDrawGroupKey mdiDrawGroupKey(InstancedBatch batch) {
            return new MdiDrawGroupKey(batch.mesh().key().kind(), batch.textureLocation(), batch.renderMode());
        }

        private static boolean sameMdiDrawGroup(InstancedBatch batch, MdiDrawGroupKey key) {
            return batch.mesh().key().kind() == key.kind()
                    && batch.textureLocation().equals(key.textureLocation())
                    && batch.renderMode() == key.renderMode();
        }

        private boolean prepareMdiGroups(MdiDrawGroups drawGroups,
                List<MdiPreparedGroup> preparedGroups, List<InstancedBatch> preparedBatches,
                List<InstancedBatch> noSlotBatches) {
            mdiAtlas.beginPreparePass();
            if (drawGroups.isEmpty()) {
                return true;
            }
            if (drawGroups.isSingleGroup()) {
                return prepareMdiGroup(drawGroups.singleGroup(), preparedGroups, preparedBatches, noSlotBatches);
            }
            for (MdiDrawGroup group : drawGroups.groupedBatches().values()) {
                if (!prepareMdiGroup(group, preparedGroups, preparedBatches, noSlotBatches)) {
                    return false;
                }
            }
            return true;
        }

        private boolean prepareMdiGroup(MdiDrawGroup group,
                List<MdiPreparedGroup> preparedGroups, List<InstancedBatch> preparedBatches,
                List<InstancedBatch> noSlotBatches) {
            int noSlotStart = noSlotBatches.size();
            MdiPreparedGroup prepared = mdiAtlas.prepare(group.key(), group.batches(), group.instances(),
                    noSlotBatches);
            if (prepared == null || prepared.commandCount() <= 0) {
                if (mdiAtlas.initializationFailed()) {
                    return false;
                }
                if (noSlotBatches.size() == noSlotStart) {
                    noSlotBatches.addAll(group.batches());
                }
                return true;
            }
            preparedGroups.add(prepared);
            preparedBatches.addAll(prepared.batches());
            return true;
        }

        private boolean recordStaleMdiPreparedGroups(List<MdiPreparedGroup> preparedGroups, String phase) {
            int layoutGeneration = mdiAtlas.layoutGeneration();
            int staleGroups = 0;
            int staleCommands = 0;
            for (MdiPreparedGroup prepared : preparedGroups) {
                if (prepared.atlasLayoutGeneration() != layoutGeneration) {
                    staleGroups++;
                    staleCommands += Math.max(0, prepared.commandCount());
                }
            }
            if (staleGroups <= 0) {
                return false;
            }
            mdiStalePreparedGroups += staleGroups;
            mdiStalePreparedCommands += staleCommands;
            currentFrameMdiStalePreparedGroups += staleGroups;
            currentFrameMdiStalePreparedCommands += staleCommands;
            logMdiStalePreparedGroups(preparedGroups, phase, layoutGeneration, staleGroups, staleCommands);
            return true;
        }

        private void logMdiStalePreparedGroups(List<MdiPreparedGroup> preparedGroups, String phase,
                int layoutGeneration, int staleGroups, int staleCommands) {
            if (!HbmMdiRenderDiag.shouldLogDispatchSummary()) {
                return;
            }
            HbmMdiRenderDiag.logBannerOnce();
            long gameTime = currentClientGameTime();
            HbmNtm.LOGGER.warn(
                    "Legacy OBJ MDI stale prepared groups gameTime={} phase={} staleGroups={} staleCommands={} atlasGeneration={}",
                    gameTime < 0L ? "?" : Long.toString(gameTime), phase, staleGroups, staleCommands,
                    layoutGeneration);
            if (!HbmMdiRenderDiag.isVerboseSubdrawsEnabled()) {
                return;
            }
            for (MdiPreparedGroup prepared : preparedGroups) {
                if (prepared.atlasLayoutGeneration() == layoutGeneration) {
                    continue;
                }
                HbmNtm.LOGGER.warn(
                        "Legacy OBJ MDI stale group kind={} renderMode={} preparedGeneration={} atlasGeneration={} commands={} instances={}",
                        prepared.key().kind(), prepared.key().renderMode(), prepared.atlasLayoutGeneration(),
                        layoutGeneration, prepared.commandCount(), prepared.instanceCount());
                for (MdiSubDraw subDraw : prepared.subDraws()) {
                    HbmNtm.LOGGER.warn(
                            "Legacy OBJ MDI stale subdraw command={} kind={} part={} firstVertex={} vertexCount={} baseInstance={} instanceCount={}",
                            subDraw.commandIndex(), subDraw.meshKey().kind(), subDraw.meshKey().stablePartKey(),
                            subDraw.firstVertex(), subDraw.vertexCount(), subDraw.baseInstance(),
                            subDraw.instanceCount());
                }
            }
        }

        private boolean allMdiShadersReady(boolean needsTexturedShader, boolean needsUntexturedShader) {
            return (!needsTexturedShader || HbmOptimizedRenderShaders.blockLitInstancedShader() != null)
                    && (!needsUntexturedShader
                            || HbmOptimizedRenderShaders.blockUntexturedInstancedShader() != null);
        }

        private void drawMdiPreparedGroup(MdiPreparedGroup prepared, Matrix4f projectionMatrix,
                int pendingBatchCount, int droppedNoSlotCount, HbmRenderFrameFlags.Snapshot flags) {
            boolean untextured = prepared.key().kind() == GpuMeshKind.UNTEXTURED;
            RenderType renderType = untextured
                    ? LegacyUntexturedQuadRenderer.type(prepared.key().renderMode(), 255,
                            VertexFormat.Mode.TRIANGLES)
                    : prepared.key().renderMode().renderType(prepared.key().textureLocation(),
                            VertexFormat.Mode.TRIANGLES);
            OptimizedDrawStateGuard stateGuard = OptimizedDrawStateGuard.snapshot(this, true);
            boolean renderStateSet = false;
            boolean multiDraw = false;
            int drawCalls = prepared.commandCount();
            ShaderInstance shader = null;
            boolean drawSubmitted = false;
            int submittedCommandCount = 0;
            try {
                renderType.setupRenderState();
                renderStateSet = true;
                if (untextured) {
                    RenderSystem.setShader(HbmOptimizedRenderShaders::blockUntexturedInstancedShader);
                } else {
                    RenderSystem.setShader(HbmOptimizedRenderShaders::blockLitInstancedShader);
                }
                shader = RenderSystem.getShader();
                if (shader == null) {
                    throw new IllegalStateException("No legacy OBJ instanced shader bound for MDI");
                }
                setupOptimizedInstancedShader(shader, !untextured, projectionMatrix,
                        prepared.key().textureLocation());
                RenderSystem.setupShaderLights(shader);
                shader.apply();
                if (!untextured) {
                    HbmRenderFrameLight.bindBlockLitSamplerTextures(shader, prepared.key().textureLocation());
                }
                OptimizedDrawStateGuard.setPrimitiveRestart(false);
                HbmGlVaoSafety.bindVertexArray(mdiAtlas.vaoId());
                mdiAtlas.enableVertexAttribArraysOnBoundVao();
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, mdiAtlas.instanceVboId());
                mdiAtlas.ensureInstanceCapacity(prepared.instanceBytes().limit());
                mdiAtlas.orphanInstanceBufferIfConfigured(flags);
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, prepared.instanceBytes());
                GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, mdiAtlas.indirectBufferId());
                mdiAtlas.ensureIndirectCapacity(prepared.commandBytes().limit());
                mdiAtlas.orphanIndirectBufferIfConfigured(flags);
                GL15.glBufferSubData(GL40.GL_DRAW_INDIRECT_BUFFER, 0L, prepared.commandBytes());
                barrierMdiIndirectCommandsIfAvailable();
                boolean canMultiDraw = mdiCoreMultiDrawArraysIndirectSupported
                        || mdiArbMultiDrawArraysIndirectSupported;
                boolean canSingleDraw = mdiCoreDrawArraysIndirectSupported
                        || mdiArbDrawArraysIndirectSupported;
                if (canMultiDraw) {
                    if (mdiCoreMultiDrawArraysIndirectSupported) {
                        GL43.glMultiDrawArraysIndirect(GL11.GL_TRIANGLES, 0L, prepared.commandCount(),
                                MdiPreparedGroup.COMMAND_STRIDE_BYTES);
                    } else {
                        ARBMultiDrawIndirect.glMultiDrawArraysIndirect(GL11.GL_TRIANGLES, 0L,
                                prepared.commandCount(), MdiPreparedGroup.COMMAND_STRIDE_BYTES);
                    }
                    drawSubmitted = true;
                    submittedCommandCount = prepared.commandCount();
                    multiDraw = true;
                    drawCalls = 1;
                } else if (canSingleDraw) {
                    for (int i = 0; i < prepared.commandCount(); i++) {
                        long commandOffset = (long) i * MdiPreparedGroup.COMMAND_STRIDE_BYTES;
                        if (mdiCoreDrawArraysIndirectSupported) {
                            GL40.glDrawArraysIndirect(GL11.GL_TRIANGLES, commandOffset);
                        } else {
                            ARBDrawIndirect.glDrawArraysIndirect(GL11.GL_TRIANGLES, commandOffset);
                        }
                        drawSubmitted = true;
                        submittedCommandCount = i + 1;
                    }
                } else {
                    throw new IllegalStateException("Draw arrays indirect unavailable during MDI dispatch");
                }
                recordMdiDraw(drawCalls, prepared.commandCount(), multiDraw, prepared.key().renderMode());
                logMdiDispatchDiagnostics(prepared, multiDraw ? "MULTI" : "IND_LOOP", drawCalls,
                        pendingBatchCount, droppedNoSlotCount);
            } catch (RuntimeException exception) {
                if (!multiDraw && submittedCommandCount > 0 && submittedCommandCount < prepared.commandCount()) {
                    recordMdiDraw(submittedCommandCount, submittedCommandCount, false, prepared.key().renderMode());
                }
                throw new MdiPreparedGroupDrawException(exception, drawSubmitted, submittedCommandCount);
            } finally {
                if (shader != null) {
                    try {
                        shader.clear();
                    } catch (RuntimeException exception) {
                        HbmNtm.LOGGER.debug("Failed to clear legacy OBJ MDI shader after draw", exception);
                    }
                }
                if (renderStateSet) {
                    try {
                        renderType.clearRenderState();
                    } catch (RuntimeException exception) {
                        HbmNtm.LOGGER.debug("Failed to clear legacy OBJ MDI render state after draw", exception);
                    }
                }
                try {
                    stateGuard.close();
                } catch (RuntimeException exception) {
                    HbmNtm.LOGGER.debug("Failed to restore legacy OBJ MDI draw state", exception);
                }
            }
        }

        private void recordMdiPartialDrawFailure() {
            mdiPartialDrawFailures++;
            currentFrameMdiPartialDrawFailures++;
        }

        private void disableMdiDispatchAfterFailure() {
            if (mdiDispatchDisabled) {
                return;
            }
            mdiDispatchDisabled = true;
            mdiDispatchDisableEvents++;
            currentFrameMdiDispatchDisableEvents++;
        }

        private void recordMdiAtlasRepackFailure() {
            mdiAtlasRepackFailures++;
            currentFrameMdiAtlasRepackFailures++;
        }

        private void recordMdiAtlasInitFailure() {
            mdiAtlasInitFailures++;
            currentFrameMdiAtlasInitFailures++;
        }

        private static List<InstancedBatch> submittedMdiBatches(MdiPreparedGroup prepared,
                int submittedCommandCount) {
            int count = Math.max(0, Math.min(submittedCommandCount, prepared.batches().size()));
            if (count <= 0) {
                return List.of();
            }
            return prepared.batches().subList(0, count);
        }

        private static List<InstancedBatch> submittedMdiBatches(List<MdiPreparedGroup> preparedGroups,
                int completedGroups, MdiPreparedGroup failedGroup, int submittedCommandCount) {
            int completed = Math.max(0, Math.min(completedGroups, preparedGroups.size()));
            List<InstancedBatch> submittedInFailedGroup = submittedMdiBatches(failedGroup, submittedCommandCount);
            if (completed <= 0) {
                return submittedInFailedGroup;
            }
            List<InstancedBatch> submitted = new ArrayList<>(completed + submittedInFailedGroup.size());
            for (int i = 0; i < completed; i++) {
                submitted.addAll(preparedGroups.get(i).batches());
            }
            submitted.addAll(submittedInFailedGroup);
            return submitted;
        }

        private void barrierMdiIndirectCommandsIfAvailable() {
            if (!mdiCommandMemoryBarrierSupported) {
                return;
            }
            try {
                GL42.glMemoryBarrier(GL42.GL_COMMAND_BARRIER_BIT);
            } catch (RuntimeException ignored) {
                // Optional command-fetch barrier; unsupported drivers use the existing dispatch path.
            }
        }

        private static final class OptimizedDrawStateGuard implements AutoCloseable {
            private final ExperimentalGpuPreparedRenderBackend owner;
            private final int previousVao;
            private final int previousArrayBuffer;
            private final int previousDrawIndirectBuffer;
            private final int previousActiveTexture;
            private final boolean restoreDrawIndirectBuffer;
            private final boolean previousCullEnabled;
            private final boolean previousDepthTestEnabled;
            private final boolean previousDepthMask;
            private final int previousDepthFunc;
            private final boolean previousBlendEnabled;
            private final int previousBlendSrcRgb;
            private final int previousBlendDstRgb;
            private final int previousBlendSrcAlpha;
            private final int previousBlendDstAlpha;
            private final boolean previousPrimitiveRestartEnabled;

            private OptimizedDrawStateGuard(ExperimentalGpuPreparedRenderBackend owner,
                    boolean includeDrawIndirectBuffer) {
                this.owner = owner;
                this.previousVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
                this.previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                this.previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
                this.restoreDrawIndirectBuffer = includeDrawIndirectBuffer;
                this.previousDrawIndirectBuffer = includeDrawIndirectBuffer
                        ? GL11.glGetInteger(GL40.GL_DRAW_INDIRECT_BUFFER_BINDING)
                        : 0;
                this.previousCullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
                this.previousDepthTestEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
                this.previousDepthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
                this.previousDepthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
                this.previousBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
                this.previousBlendSrcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
                this.previousBlendDstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
                this.previousBlendSrcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
                this.previousBlendDstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
                this.previousPrimitiveRestartEnabled = isPrimitiveRestartEnabled();
            }

            private static OptimizedDrawStateGuard snapshot(ExperimentalGpuPreparedRenderBackend owner,
                    boolean includeDrawIndirectBuffer) {
                return new OptimizedDrawStateGuard(owner, includeDrawIndirectBuffer);
            }

            @Override
            public void close() {
                Throwable failure = null;
                failure = restoreStep(failure, () -> setPrimitiveRestart(previousPrimitiveRestartEnabled));
                if (restoreDrawIndirectBuffer) {
                    failure = restoreStep(failure,
                            () -> GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, previousDrawIndirectBuffer));
                }
                failure = restoreStep(failure, () -> GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer));
                failure = restoreStep(failure, () -> HbmGlVaoSafety.bindVertexArray(previousVao));
                failure = restoreStep(failure, () -> {
                    if (previousCullEnabled) {
                        RenderSystem.enableCull();
                    } else {
                        RenderSystem.disableCull();
                    }
                });
                failure = restoreStep(failure, () -> {
                    if (previousDepthTestEnabled) {
                        RenderSystem.enableDepthTest();
                    } else {
                        RenderSystem.disableDepthTest();
                    }
                });
                failure = restoreStep(failure, () -> RenderSystem.depthMask(previousDepthMask));
                failure = restoreStep(failure, () -> RenderSystem.depthFunc(previousDepthFunc));
                failure = restoreStep(failure, () -> RenderSystem.blendFuncSeparate(previousBlendSrcRgb,
                        previousBlendDstRgb, previousBlendSrcAlpha, previousBlendDstAlpha));
                failure = restoreStep(failure, () -> {
                    if (previousBlendEnabled) {
                        RenderSystem.enableBlend();
                    } else {
                        RenderSystem.disableBlend();
                    }
                });
                failure = restoreStep(failure, () -> RenderSystem.setShader(GameRenderer::getRendertypeSolidShader));
                failure = restoreStep(failure, () -> RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS));
                failure = restoreStep(failure, () -> RenderSystem.activeTexture(previousActiveTexture));
                if (failure != null) {
                    owner.recordOptimizedDrawStateRestoreFailure();
                    HbmNtm.LOGGER.error("Legacy OBJ optimized draw state restore failed", failure);
                }
            }

            private static Throwable restoreStep(Throwable failure, Runnable action) {
                try {
                    action.run();
                } catch (Throwable throwable) {
                    if (failure == null) {
                        return throwable;
                    }
                    failure.addSuppressed(throwable);
                }
                return failure;
            }

            private static boolean isPrimitiveRestartEnabled() {
                try {
                    return GL11.glIsEnabled(GL31.GL_PRIMITIVE_RESTART);
                } catch (RuntimeException ignored) {
                    return false;
                }
            }

            private static void setPrimitiveRestart(boolean enabled) {
                try {
                    if (enabled) {
                        GL11.glEnable(GL31.GL_PRIMITIVE_RESTART);
                    } else {
                        GL11.glDisable(GL31.GL_PRIMITIVE_RESTART);
                    }
                } catch (RuntimeException ignored) {
                    // Optional GL state on this path; keep the draw/fallback decision elsewhere.
                }
            }
        }

        private static int totalInstances(List<InstancedBatch> batches) {
            int total = 0;
            for (InstancedBatch batch : batches) {
                total += batch.instances().size();
            }
            return total;
        }

        private void coalesceDuplicateInstancedInstances(List<InstancedBatch> batches) {
            long removed = 0L;
            Set<InstancedInstance> seen = instancedDuplicateScratch;
            try {
                for (InstancedBatch batch : batches) {
                    removed += batch.removeDuplicateInstances(seen);
                }
            } finally {
                seen.clear();
            }
            if (removed <= 0L) {
                return;
            }
            instancedDuplicateInstances += removed;
            currentFrameInstancedDuplicateInstances += removed;
            if (HbmClientConfig.renderBackendDiagnostics()) {
                HbmNtm.LOGGER.debug("Coalesced {} duplicate legacy OBJ instanced submissions in current frame",
                        removed);
            }
        }

        private void coalesceDuplicateIrisCompanionInstances(List<IrisCompanionQueuedBatch> batches) {
            long removed = 0L;
            Set<IrisCompanionQueuedInstance> seen = irisCompanionDuplicateScratch;
            try {
                for (IrisCompanionQueuedBatch batch : batches) {
                    removed += batch.removeDuplicateInstances(seen);
                }
            } finally {
                seen.clear();
            }
            if (removed <= 0L) {
                return;
            }
            irisQueuedDuplicateInstances += removed;
            currentFrameIrisQueuedDuplicateInstances += removed;
            if (HbmClientConfig.renderBackendDiagnostics()) {
                HbmNtm.LOGGER.debug("Coalesced {} duplicate legacy OBJ Iris companion submissions in current frame",
                        removed);
            }
        }

        private static void sortInstancedTailBatches(List<InstancedBatch> batches) {
            int previousPriority = 0;
            int normalAlphaBatches = 0;
            boolean needsBatchSort = false;
            for (InstancedBatch batch : batches) {
                int priority = instancedTailPriority(batch.renderMode());
                if (priority < previousPriority) {
                    needsBatchSort = true;
                }
                previousPriority = priority;
                if (isInstancedNormalAlphaMode(batch.renderMode())) {
                    normalAlphaBatches++;
                }
            }
            if (normalAlphaBatches > 1) {
                needsBatchSort = true;
            }
            if (needsBatchSort) {
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
            }
            if (normalAlphaBatches > 0) {
                for (InstancedBatch batch : batches) {
                    if (isInstancedNormalAlphaMode(batch.renderMode())) {
                        batch.sortBackToFront();
                    }
                }
            }
        }

        private void recordMdiFallback(int eligibleBatches) {
            mdiFallbackFlushes++;
            mdiFallbackBatches += eligibleBatches;
            currentFrameMdiFallbackFlushes++;
            currentFrameMdiFallbackBatches += eligibleBatches;
        }

        private void recordMdiFallback(int eligibleBatches, List<InstancedBatch> batches,
                RenderBackendFallbackReason reason, String path) {
            recordMdiFallback(eligibleBatches, batches, reason, path, totalInstances(batches));
        }

        private void recordMdiFallback(int eligibleBatches, List<InstancedBatch> batches,
                RenderBackendFallbackReason reason, String path, int instances) {
            recordMdiFallback(eligibleBatches);
            recordMdiFallbackDetail(reason, path, batches, instances);
        }

        private void recordMdiFallbackDetail(RenderBackendFallbackReason reason, String path,
                List<InstancedBatch> batches, int instances) {
            lastGpuFallbackReason = reason == null ? RenderBackendFallbackReason.NONE : reason;
            lastGpuFallbackDetail = fallbackDetail(path, lastGpuFallbackReason, batches, instances);
        }

        private void recordMdiDraw(int drawCalls, int commandCount, boolean multiDraw,
                LegacyTexturedRenderMode renderMode) {
            int safeDrawCalls = Math.max(1, drawCalls);
            mdiDrawCalls += safeDrawCalls;
            mdiIndirectCommands += commandCount;
            currentFrameMdiDrawCalls += safeDrawCalls;
            currentFrameMdiIndirectCommands += commandCount;
            instancedDrawCalls += safeDrawCalls;
            currentFrameInstancedDrawCalls += safeDrawCalls;
            gpuDrawCalls += safeDrawCalls;
            currentFrameGpuDrawCalls += safeDrawCalls;
            if (isInstancedAdditiveMode(renderMode)) {
                mdiAdditiveDrawCalls += safeDrawCalls;
                mdiAdditiveIndirectCommands += commandCount;
                currentFrameMdiAdditiveDrawCalls += safeDrawCalls;
                currentFrameMdiAdditiveIndirectCommands += commandCount;
                instancedAdditiveDrawCalls += safeDrawCalls;
                currentFrameInstancedAdditiveDrawCalls += safeDrawCalls;
            }
            if (multiDraw) {
                mdiMultiDrawCalls++;
                currentFrameMdiMultiDrawCalls++;
            }
        }

        private void logMdiDispatchDiagnostics(MdiPreparedGroup prepared, String mode, int drawCalls,
                int pendingBatchCount, int droppedNoSlotCount) {
            if (!HbmMdiRenderDiag.shouldLogDispatchSummary()) {
                return;
            }
            HbmMdiRenderDiag.logBannerOnce();
            long gameTime = currentClientGameTime();
            String message = String.format(Locale.ROOT,
                    "Legacy OBJ MDI dispatch gameTime=%s mode=%s drawCalls=%d draws=%d/%d droppedNoSlot=%d commands=%d instances=%d atlasParts=%d atlasCapacityBytes=%d kind=%s renderMode=%s",
                    gameTime < 0L ? "?" : Long.toString(gameTime),
                    mode,
                    Math.max(1, drawCalls),
                    prepared.commandCount(),
                    Math.max(0, pendingBatchCount),
                    Math.max(0, droppedNoSlotCount),
                    prepared.commandCount(),
                    prepared.instanceCount(),
                    mdiAtlas.partCount(),
                    mdiAtlas.byteCapacity(),
                    prepared.key().kind(),
                    prepared.key().renderMode());
            HbmNtm.LOGGER.info(message);
            boolean verbose = HbmMdiRenderDiag.isVerboseSubdrawsEnabled();
            if (!verbose) {
                return;
            }
            for (MdiSubDraw subDraw : prepared.subDraws()) {
                String subDrawMessage = String.format(Locale.ROOT,
                        "Legacy OBJ MDI subdraw command=%d kind=%s part=%s sourceMode=%s sourceVertices=%d firstVertex=%d vertexCount=%d baseInstance=%d instanceCount=%d",
                        subDraw.commandIndex(),
                        subDraw.meshKey().kind(),
                        subDraw.meshKey().stablePartKey(),
                        subDraw.meshKey().sourceMode(),
                        subDraw.meshKey().sourceVertices(),
                        subDraw.firstVertex(),
                        subDraw.vertexCount(),
                        subDraw.baseInstance(),
                        subDraw.instanceCount());
                HbmNtm.LOGGER.info(subDrawMessage);
            }
        }

        private static long currentClientGameTime() {
            try {
                Minecraft minecraft = Minecraft.getInstance();
                return minecraft.level == null ? -1L : minecraft.level.getGameTime();
            } catch (RuntimeException ignored) {
                return -1L;
            }
        }

        private void recordMdiAdditiveEligible(int additiveBatches) {
            if (additiveBatches <= 0) {
                return;
            }
            mdiAdditiveEligibleBatches += additiveBatches;
            currentFrameMdiAdditiveEligibleBatches += additiveBatches;
        }

        private void recordMdiAdditiveFallback(int additiveBatches) {
            if (additiveBatches <= 0) {
                return;
            }
            mdiAdditiveFallbackBatches += additiveBatches;
            currentFrameMdiAdditiveFallbackBatches += additiveBatches;
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
            recordMdiFallbackDetail(RenderBackendFallbackReason.MDI_NO_SLOT, "mdi-no-slot",
                    batches, instanceCount);
            mdiNoSlotBatches += batchCount;
            mdiNoSlotInstances += instanceCount;
            currentFrameMdiNoSlotBatches += batchCount;
            currentFrameMdiNoSlotInstances += instanceCount;
            if (additiveBatchCount > 0) {
                mdiAdditiveNoSlotBatches += additiveBatchCount;
                mdiAdditiveNoSlotInstances += additiveInstanceCount;
                currentFrameMdiAdditiveNoSlotBatches += additiveBatchCount;
                currentFrameMdiAdditiveNoSlotInstances += additiveInstanceCount;
            }
        }

        private boolean mdiAvailable() {
            ensureMdiCapabilities();
            return !mdiDispatchDisabled && mdiDrawIndirectSupported && mdiBaseInstanceSupported;
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
                    GLCapabilities capabilities = HbmInstancedGlCompat.currentCapabilities();
                    if (capabilities == null) {
                        return;
                    }
                    boolean coreDrawIndirect = capabilities.glDrawArraysIndirect != 0L;
                    boolean arbDrawIndirect = capabilities.GL_ARB_draw_indirect;
                    boolean coreMultiDrawIndirect = capabilities.glMultiDrawArraysIndirect != 0L;
                    boolean arbMultiDrawIndirect = capabilities.GL_ARB_multi_draw_indirect;
                    boolean singleDrawIndirect = coreDrawIndirect || arbDrawIndirect;
                    boolean multiDrawIndirect = coreMultiDrawIndirect || arbMultiDrawIndirect;
                    mdiDrawIndirectSupported = singleDrawIndirect || multiDrawIndirect;
                    mdiMultiDrawIndirectSupported = multiDrawIndirect;
                    mdiBaseInstanceSupported = capabilities.glDrawArraysInstancedBaseInstance != 0L
                            || capabilities.GL_ARB_base_instance;
                    mdiCoreDrawArraysIndirectSupported = coreDrawIndirect;
                    mdiArbDrawArraysIndirectSupported = arbDrawIndirect;
                    mdiCoreMultiDrawArraysIndirectSupported = coreMultiDrawIndirect;
                    mdiArbMultiDrawArraysIndirectSupported = arbMultiDrawIndirect;
                    mdiCommandMemoryBarrierSupported = capabilities.glMemoryBarrier != 0L;
                } catch (RuntimeException exception) {
                    clearMdiCapabilityCache();
                }
                mdiCapsResolved = true;
            }
        }

        private void clearMdiCapabilityCache() {
            mdiCapsResolved = false;
            mdiDrawIndirectSupported = false;
            mdiMultiDrawIndirectSupported = false;
            mdiBaseInstanceSupported = false;
            mdiCoreDrawArraysIndirectSupported = false;
            mdiArbDrawArraysIndirectSupported = false;
            mdiCoreMultiDrawArraysIndirectSupported = false;
            mdiArbMultiDrawArraysIndirectSupported = false;
            mdiCommandMemoryBarrierSupported = false;
        }

        private boolean drawInstancedBatch(InstancedBatch batch, Matrix4f projectionMatrix) {
            HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
            boolean untextured = batch.mesh().key().kind() == GpuMeshKind.UNTEXTURED;
            ShaderInstance shader = untextured
                    ? HbmOptimizedRenderShaders.blockUntexturedInstancedShader()
                    : HbmOptimizedRenderShaders.blockLitInstancedShader();
            if (shader == null) {
                recordInstancedFallback(RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE,
                        batch.instances().size(), batch.renderMode(),
                        fallbackDetail("instanced-shader", RenderBackendFallbackReason.INSTANCING_SHADER_UNAVAILABLE,
                                batch, batch.instances().size()));
                return false;
            }
            InstancedMesh mesh = batch.mesh();
            int instanceCount = batch.instances().size();
            int maxInstancesPerDraw = Math.max(1, flags.maxInstancedInstancesPerDraw());
            if (instanceCount > maxInstancesPerDraw) {
                recordInstancedOverflow(instanceCount, maxInstancesPerDraw, batch.renderMode());
            }
            RenderType renderType = untextured
                    ? LegacyUntexturedQuadRenderer.type(batch.renderMode(), 255, VertexFormat.Mode.TRIANGLES)
                    : batch.renderMode().renderType(batch.textureLocation(), VertexFormat.Mode.TRIANGLES);
            OptimizedDrawStateGuard stateGuard = OptimizedDrawStateGuard.snapshot(this, false);
            boolean renderStateSet = false;
            ShaderInstance boundShader = null;
            int submittedInstances = 0;
            try {
                renderType.setupRenderState();
                renderStateSet = true;
                if (untextured) {
                    RenderSystem.setShader(HbmOptimizedRenderShaders::blockUntexturedInstancedShader);
                } else {
                    RenderSystem.setShader(HbmOptimizedRenderShaders::blockLitInstancedShader);
                }
                boundShader = RenderSystem.getShader();
                if (boundShader == null) {
                    throw new IllegalStateException("No legacy OBJ instanced shader bound");
                }
                setupOptimizedInstancedShader(boundShader, !untextured, projectionMatrix, batch.textureLocation());
                RenderSystem.setupShaderLights(boundShader);
                boundShader.apply();
                if (!untextured) {
                    HbmRenderFrameLight.bindBlockLitSamplerTextures(boundShader, batch.textureLocation());
                }
                HbmGlVaoSafety.bindVertexArray(mesh.vaoId());
                mesh.enableVertexAttribArraysOnBoundVao();
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.instanceVboId());
                for (int start = 0; start < instanceCount; start += maxInstancesPerDraw) {
                    int end = Math.min(start + maxInstancesPerDraw, instanceCount);
                    ByteBuffer instanceBytes = instancedSliceBytes(batch, start, end);
                    uploadInstancedSliceToBoundVbo(mesh, instanceBytes, flags);
                    HbmInstancedGlCompat.drawArraysInstanced(GL11.GL_TRIANGLES, 0, mesh.vertexCount(), end - start);
                    submittedInstances = end;
                    recordInstancedDrawCall(batch.renderMode());
                }
            } catch (RuntimeException exception) {
                throw new InstancedBatchDrawException(exception, submittedInstances);
            } finally {
                if (boundShader != null) {
                    try {
                        boundShader.clear();
                    } catch (RuntimeException exception) {
                        HbmNtm.LOGGER.debug("Failed to clear legacy OBJ instanced shader after draw", exception);
                    }
                }
                if (renderStateSet) {
                    try {
                        renderType.clearRenderState();
                    } catch (RuntimeException exception) {
                        HbmNtm.LOGGER.debug("Failed to clear legacy OBJ instanced render state after draw", exception);
                    }
                }
                try {
                    stateGuard.close();
                } catch (RuntimeException exception) {
                    HbmNtm.LOGGER.debug("Failed to restore legacy OBJ instanced draw state", exception);
                }
            }
            return true;
        }

        private void uploadInstancedSliceToBoundVbo(InstancedMesh mesh, ByteBuffer instanceBytes,
                HbmRenderFrameFlags.Snapshot flags) {
            int requiredBytes = instanceBytes.limit();
            int capacityBytes = mesh.instanceCapacityBytes().get();
            if (capacityBytes < requiredBytes) {
                capacityBytes = instancedVboTargetCapacityBytes(requiredBytes, capacityBytes);
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) capacityBytes, GL15.GL_STREAM_DRAW);
                mesh.instanceCapacityBytes().set(capacityBytes);
            } else if (flags.instanceVboOrphanBeforeUpload()) {
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) capacityBytes, GL15.GL_STREAM_DRAW);
            }
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, instanceBytes);
        }

        private static int instancedVboTargetCapacityBytes(int requiredBytes, int currentCapacityBytes) {
            int minimumBytes = 64 * InstancedInstance.FLOATS * 4;
            int targetBytes = Math.max(minimumBytes, currentCapacityBytes > 0 ? currentCapacityBytes : minimumBytes);
            while (targetBytes < requiredBytes && targetBytes < (1 << 30)) {
                targetBytes <<= 1;
            }
            return Math.max(targetBytes, requiredBytes);
        }

        private void setupOptimizedInstancedShader(ShaderInstance shader, boolean textured,
                Matrix4f projectionMatrix, ResourceLocation baseTexture) {
            if (textured) {
                HbmRenderFrameLight.prepareBlockLitSamplers(shader, baseTexture);
            }
            updateOptimizedUniformCache(shader);
            updateOptimizedUniformValueOwner(shader);
            Matrix4f resolvedProjection = projectionMatrix != null ? projectionMatrix
                    : RenderSystem.getProjectionMatrix();
            if (optimizedProjMatUniform != null) {
                setOptimizedProjectionUniformIfChanged(optimizedProjMatUniform, resolvedProjection);
            } else if (shader.PROJECTION_MATRIX != null) {
                setOptimizedProjectionUniformIfChanged(shader.PROJECTION_MATRIX, resolvedProjection);
            }
            if (optimizedModelViewUniform != null) {
                setOptimizedIdentityModelViewUniformIfNeeded(optimizedModelViewUniform);
            } else if (shader.MODEL_VIEW_MATRIX != null) {
                setOptimizedIdentityModelViewUniformIfNeeded(shader.MODEL_VIEW_MATRIX);
            }
            if (optimizedFogStartUniform != null) {
                setOptimizedFogStartUniformIfChanged(RenderSystem.getShaderFogStart());
            }
            if (optimizedFogEndUniform != null) {
                setOptimizedFogEndUniformIfChanged(RenderSystem.getShaderFogEnd());
            }
            if (optimizedFogColorUniform != null) {
                float[] color = RenderSystem.getShaderFogColor();
                setOptimizedFogColorUniformIfChanged(color);
            }
            if (optimizedFadeAlphaUniform != null) {
                setOptimizedFadeAlphaOneUniformIfNeeded();
            }
        }

        private void updateOptimizedUniformCache(ShaderInstance shader) {
            int program = shader == null ? -1 : shader.getId();
            long generation = HbmShaderCompatibilityDetector.pipelineGeneration();
            if (optimizedUniformShader == shader
                    && optimizedUniformProgram == program
                    && optimizedUniformPipelineGeneration == generation
                    && optimizedUniformShader != null) {
                return;
            }
            optimizedUniformShader = shader;
            optimizedUniformProgram = program;
            optimizedUniformPipelineGeneration = generation;
            optimizedProjMatUniform = shader == null ? null : shader.getUniform("ProjMat");
            optimizedModelViewUniform = shader == null ? null : shader.getUniform("ModelViewMat");
            optimizedFogStartUniform = shader == null ? null : shader.getUniform("FogStart");
            optimizedFogEndUniform = shader == null ? null : shader.getUniform("FogEnd");
            optimizedFogColorUniform = shader == null ? null : shader.getUniform("FogColor");
            optimizedFadeAlphaUniform = shader == null ? null : shader.getUniform("FadeAlpha");
        }

        private void updateOptimizedUniformValueOwner(ShaderInstance shader) {
            int program = shader == null ? -1 : shader.getId();
            long generation = HbmShaderCompatibilityDetector.pipelineGeneration();
            if (optimizedUniformValueShader == shader
                    && optimizedUniformValueProgram == program
                    && optimizedUniformValuePipelineGeneration == generation
                    && optimizedUniformValueShader != null) {
                return;
            }
            optimizedUniformValueShader = shader;
            optimizedUniformValueProgram = program;
            optimizedUniformValuePipelineGeneration = generation;
            clearOptimizedUniformValueCache();
        }

        private void setOptimizedProjectionUniformIfChanged(Uniform uniform, Matrix4f projectionMatrix) {
            if (!optimizedProjectionUniformValid || !optimizedLastProjectionMatrix.equals(projectionMatrix)) {
                uniform.set(projectionMatrix);
                optimizedLastProjectionMatrix.set(projectionMatrix);
                optimizedProjectionUniformValid = true;
            }
        }

        private void setOptimizedIdentityModelViewUniformIfNeeded(Uniform uniform) {
            if (!optimizedModelViewIdentityUniformValid) {
                uniform.set(OPTIMIZED_SHADER_IDENTITY);
                optimizedModelViewIdentityUniformValid = true;
            }
        }

        private void setOptimizedFogStartUniformIfChanged(float fogStart) {
            if (!optimizedFogStartUniformValid
                    || Float.floatToIntBits(optimizedLastFogStart) != Float.floatToIntBits(fogStart)) {
                optimizedFogStartUniform.set(fogStart);
                optimizedLastFogStart = fogStart;
                optimizedFogStartUniformValid = true;
            }
        }

        private void setOptimizedFogEndUniformIfChanged(float fogEnd) {
            if (!optimizedFogEndUniformValid
                    || Float.floatToIntBits(optimizedLastFogEnd) != Float.floatToIntBits(fogEnd)) {
                optimizedFogEndUniform.set(fogEnd);
                optimizedLastFogEnd = fogEnd;
                optimizedFogEndUniformValid = true;
            }
        }

        private void setOptimizedFogColorUniformIfChanged(float[] color) {
            int red = Float.floatToIntBits(color[0]);
            int green = Float.floatToIntBits(color[1]);
            int blue = Float.floatToIntBits(color[2]);
            int alpha = Float.floatToIntBits(color[3]);
            if (!optimizedFogColorUniformValid
                    || optimizedLastFogColorRed != red
                    || optimizedLastFogColorGreen != green
                    || optimizedLastFogColorBlue != blue
                    || optimizedLastFogColorAlpha != alpha) {
                optimizedFogColorUniform.set(color[0], color[1], color[2], color[3]);
                optimizedLastFogColorRed = red;
                optimizedLastFogColorGreen = green;
                optimizedLastFogColorBlue = blue;
                optimizedLastFogColorAlpha = alpha;
                optimizedFogColorUniformValid = true;
            }
        }

        private void setOptimizedFadeAlphaOneUniformIfNeeded() {
            if (!optimizedFadeAlphaOneUniformValid) {
                optimizedFadeAlphaUniform.set(1.0F);
                optimizedFadeAlphaOneUniformValid = true;
            }
        }

        private void clearOptimizedUniformValueCache() {
            optimizedProjectionUniformValid = false;
            optimizedModelViewIdentityUniformValid = false;
            optimizedFogStartUniformValid = false;
            optimizedFogEndUniformValid = false;
            optimizedFogColorUniformValid = false;
            optimizedFadeAlphaOneUniformValid = false;
            optimizedLastFogStart = 0.0F;
            optimizedLastFogEnd = 0.0F;
            optimizedLastFogColorRed = Integer.MIN_VALUE;
            optimizedLastFogColorGreen = Integer.MIN_VALUE;
            optimizedLastFogColorBlue = Integer.MIN_VALUE;
            optimizedLastFogColorAlpha = Integer.MIN_VALUE;
        }

        private ByteBuffer instancedSliceBytes(InstancedBatch batch, int start, int end) {
            int requiredBytes = (end - start) * InstancedInstance.FLOATS * 4;
            ByteBuffer instanceBytes = ensureInstancedUploadScratch(requiredBytes);
            long baseAddress = MemoryUtil.memAddress(instanceBytes);
            for (int i = start; i < end; i++) {
                batch.instances().get(i).write(baseAddress + (long) (i - start) * InstancedInstance.FLOATS * 4L);
            }
            instanceBytes.position(requiredBytes);
            instanceBytes.flip();
            return instanceBytes;
        }

        private ByteBuffer ensureInstancedUploadScratch(int requiredBytes) {
            int capacity = Math.max(requiredBytes, InstancedInstance.FLOATS * 4);
            ByteBuffer scratch = instancedUploadScratch;
            if (scratch == null || scratch.capacity() < capacity) {
                freeNativeScratch(scratch, "legacy OBJ instanced upload scratch resize");
                scratch = MemoryUtil.memAlloc(capacity).order(ByteOrder.nativeOrder());
                instancedUploadScratch = scratch;
            }
            scratch.clear();
            return scratch;
        }

        private void clearInstancedUploadScratch() {
            ByteBuffer scratch = instancedUploadScratch;
            instancedUploadScratch = null;
            freeNativeScratch(scratch, "legacy OBJ instanced upload scratch clear");
        }

        private static void freeNativeScratch(ByteBuffer scratch, String context) {
            if (scratch == null) {
                return;
            }
            try {
                MemoryUtil.memFree(scratch);
            } catch (RuntimeException exception) {
                HbmNtm.LOGGER.debug("Failed to free {}", context, exception);
            }
        }

        private void recordInstancedDrawCall(LegacyTexturedRenderMode renderMode) {
            instancedDrawCalls++;
            currentFrameInstancedDrawCalls++;
            gpuDrawCalls++;
            currentFrameGpuDrawCalls++;
            if (isInstancedAdditiveMode(renderMode)) {
                instancedAdditiveDrawCalls++;
                currentFrameInstancedAdditiveDrawCalls++;
            }
        }

        private void recordInstancedOverflow(int instances, int maxInstancesPerDraw,
                LegacyTexturedRenderMode renderMode) {
            int overflow = Math.max(0, instances - Math.max(1, maxInstancesPerDraw));
            if (overflow <= 0) {
                return;
            }
            instancedOverflowBatches++;
            instancedOverflowInstances += overflow;
            currentFrameInstancedOverflowBatches++;
            currentFrameInstancedOverflowInstances += overflow;
            if (isInstancedAdditiveMode(renderMode)) {
                instancedAdditiveOverflowBatches++;
                instancedAdditiveOverflowInstances += overflow;
                currentFrameInstancedAdditiveOverflowBatches++;
                currentFrameInstancedAdditiveOverflowInstances += overflow;
            }
        }

        private void drawInstancedCpuFallback(InstancedBatch batch, RenderBackendFallbackReason reason) {
            drawInstancedCpuFallback(batch, reason, 0);
        }

        private void drawInstancedCpuFallback(InstancedBatch batch, RenderBackendFallbackReason reason,
                int startIndex) {
            InstancedMesh mesh = batch.mesh();
            int start = clampedFallbackStart(batch, startIndex);
            int fallbackCount = batch.instances().size() - start;
            if (fallbackCount <= 0) {
                return;
            }
            VertexConsumer consumer = null;
            MultiBufferSource activeBuffer = null;
            boolean untextured = mesh.key().kind() == GpuMeshKind.UNTEXTURED;
            RenderType renderType = untextured
                    ? LegacyUntexturedQuadRenderer.type(batch.renderMode(), 255, mesh.sourceMode())
                    : batch.renderMode().renderType(batch.textureLocation(), mesh.sourceMode());
            TextureAtlasSprite sprite = mesh.key().sprite();
            for (int i = start; i < batch.instances().size(); i++) {
                InstancedInstance fallback = batch.instances().get(i);
                if (consumer == null || fallback.buffer() != activeBuffer) {
                    activeBuffer = fallback.buffer();
                    consumer = fallback.buffer().getBuffer(renderType);
                }
                int fadedAlpha = fadedAlpha(fallback.alpha(), fallback.fadeAlpha());
                if (untextured) {
                    emitPreparedVerticesUntextured(mesh.sourceVertices(), consumer, fallback.position(),
                            fallback.red(), fallback.green(), fallback.blue(), fadedAlpha);
                } else if (sprite == null) {
                    emitPreparedVertices(mesh.sourceVertices(), consumer, fallback.position(), fallback.normal(),
                            fallback.packedLight(), fallback.packedOverlay(), fallback.red(), fallback.green(),
                            fallback.blue(), fadedAlpha, false, mesh.key().smoothing(), fallback.uvTransform());
                } else {
                    emitPreparedVerticesWithSprite(mesh.sourceVertices(), sprite, consumer, fallback.position(),
                            fallback.normal(), fallback.packedLight(), fallback.packedOverlay(), fallback.red(),
                            fallback.green(), fallback.blue(), fadedAlpha, false, false,
                            fallback.uvTransform());
                }
            }
            recordGpuFallback(reason, mesh.sourceVertices().size() * fallbackCount,
                    fallbackDetail("instanced-cpu", reason, batch, fallbackCount));
        }

        private static int instancedFallbackCount(InstancedBatch batch, int startIndex) {
            int start = clampedFallbackStart(batch, startIndex);
            return Math.max(0, batch.instances().size() - start);
        }

        private static int clampedFallbackStart(InstancedBatch batch, int startIndex) {
            return Math.max(0, Math.min(startIndex, batch.instances().size()));
        }

        private static int fadedAlpha(int alpha, float fadeAlpha) {
            float clampedFade = clampedFadeAlpha(fadeAlpha);
            return Mth.clamp(Math.round(alpha * clampedFade), 0, 255);
        }

        private static void setIrisCompanionFadeAlpha(ShaderInstance shader, float fadeAlpha) {
            Uniform uniform = shader == null ? null : shader.getUniform("FadeAlpha");
            if (uniform != null) {
                uniform.set(clampedFadeAlpha(fadeAlpha));
            }
        }

        private static float clampedFadeAlpha(float fadeAlpha) {
            return Float.isFinite(fadeAlpha) ? Mth.clamp(fadeAlpha, 0.0F, 1.0F) : 1.0F;
        }

        private void recordGpuFallback(RenderBackendFallbackReason reason, int vertices) {
            recordGpuFallback(reason, vertices, null);
        }

        private void recordGpuFallback(RenderBackendFallbackReason reason, int vertices, String detail) {
            if (vertices <= 0) {
                return;
            }
            lastGpuFallbackReason = reason == null ? RenderBackendFallbackReason.NONE : reason;
            lastGpuFallbackDetail = sanitizeFallbackDetail(detail);
            gpuFallbackBatches++;
            gpuFallbackVertices += vertices;
            currentFrameGpuFallbackBatches++;
            currentFrameGpuFallbackVertices += vertices;
        }

        private void recordIrisFallback(RenderBackendFallbackReason reason, int vertices) {
            recordIrisFallback(reason, vertices, null);
        }

        private void recordIrisFallback(RenderBackendFallbackReason reason, int vertices, String detail) {
            if (vertices <= 0) {
                return;
            }
            lastGpuFallbackReason = reason == null ? RenderBackendFallbackReason.NONE : reason;
            lastGpuFallbackDetail = sanitizeFallbackDetail(detail);
            irisFallbackBatches++;
            irisFallbackVertices += vertices;
            currentFrameIrisFallbackBatches++;
            currentFrameIrisFallbackVertices += vertices;
        }

        private void clearIrisCompanionFallback() {
            lastIrisCompanionFallbackReason = RenderBackendFallbackReason.NONE;
        }

        private RenderBackendFallbackReason irisCompanionFallbackReason(
                RenderBackendFallbackReason defaultReason) {
            return lastIrisCompanionFallbackReason == RenderBackendFallbackReason.NONE
                    ? defaultReason
                    : lastIrisCompanionFallbackReason;
        }

        private void recordIrisCompanionFallback(RenderBackendFallbackReason reason, int vertices,
                String detail) {
            lastIrisCompanionFallbackReason = reason == null ? RenderBackendFallbackReason.NONE : reason;
            recordIrisFallback(reason, vertices, detail);
        }

        private void recordIrisQueuedFallback(int instances) {
            if (instances <= 0) {
                return;
            }
            irisQueuedFallbackBatches++;
            irisQueuedFallbackInstances += instances;
            currentFrameIrisQueuedFallbackBatches++;
            currentFrameIrisQueuedFallbackInstances += instances;
        }

        private void recordIrisLightmapStorageFailure() {
            irisLightmapStorageFailures++;
            currentFrameIrisLightmapStorageFailures++;
        }

        private void recordIrisLightmapSlotReuse() {
            irisLightmapSlotReuses++;
            currentFrameIrisLightmapSlotReuses++;
        }

        private void recordIrisLightmapSlotUpload() {
            irisLightmapSlotUploads++;
            currentFrameIrisLightmapSlotUploads++;
        }

        private void recordIrisLightmapStagingFallback() {
            irisLightmapStagingFallbacks++;
            currentFrameIrisLightmapStagingFallbacks++;
        }

        private void recordIrisShaderAttributeCacheHit() {
            irisShaderAttributeCacheHits++;
            currentFrameIrisShaderAttributeCacheHits++;
        }

        private void recordIrisShaderAttributeCacheMiss() {
            irisShaderAttributeCacheMisses++;
            currentFrameIrisShaderAttributeCacheMisses++;
        }

        private void recordIrisShaderAttributeGenerationInvalidation() {
            irisShaderAttributeGenerationInvalidations++;
            currentFrameIrisShaderAttributeGenerationInvalidations++;
        }

        private void recordIrisShaderAttributePrimedSkip() {
            irisShaderAttributePrimedSkips++;
            currentFrameIrisShaderAttributePrimedSkips++;
        }

        private void recordIrisShaderAttributeVaoBindFailure() {
            irisShaderAttributeVaoBindFailures++;
            currentFrameIrisShaderAttributeVaoBindFailures++;
        }

        private void clearInstancedQueueFallback() {
            lastInstancedQueueFallbackReason = RenderBackendFallbackReason.NONE;
        }

        private RenderBackendFallbackReason instancedQueueFallbackReason(
                RenderBackendFallbackReason defaultReason) {
            return lastInstancedQueueFallbackReason == RenderBackendFallbackReason.NONE
                    ? defaultReason
                    : lastInstancedQueueFallbackReason;
        }

        private void recordInstancedQueueFallback(RenderBackendFallbackReason reason, int instances,
                LegacyTexturedRenderMode renderMode, String detail) {
            lastInstancedQueueFallbackReason = reason == null ? RenderBackendFallbackReason.NONE : reason;
            recordInstancedFallback(reason, instances, renderMode, detail);
        }

        private void recordInstancedFallback(RenderBackendFallbackReason reason, int instances) {
            recordInstancedFallback(reason, instances, false);
        }

        private void recordInstancedFallback(RenderBackendFallbackReason reason, int instances, String detail) {
            recordInstancedFallback(reason, instances, false, detail);
        }

        private void recordInstancedFallback(RenderBackendFallbackReason reason, int instances,
                LegacyTexturedRenderMode renderMode) {
            recordInstancedFallback(reason, instances, renderMode, null);
        }

        private void recordInstancedFallback(RenderBackendFallbackReason reason, int instances,
                LegacyTexturedRenderMode renderMode, String detail) {
            recordInstancedFallback(reason, instances, isInstancedAdditiveMode(renderMode), detail);
        }

        private void recordInstancedFallback(RenderBackendFallbackReason reason, int instances, boolean additive) {
            recordInstancedFallback(reason, instances, additive, null);
        }

        private void recordInstancedFallback(RenderBackendFallbackReason reason, int instances, boolean additive,
                String detail) {
            if (instances <= 0) {
                return;
            }
            lastGpuFallbackReason = reason == null ? RenderBackendFallbackReason.NONE : reason;
            lastGpuFallbackDetail = sanitizeFallbackDetail(detail);
            instancedFallbackBatches++;
            instancedFallbackInstances += instances;
            currentFrameInstancedFallbackBatches++;
            currentFrameInstancedFallbackInstances += instances;
            if (additive) {
                instancedAdditiveFallbackBatches++;
                instancedAdditiveFallbackInstances += instances;
                currentFrameInstancedAdditiveFallbackBatches++;
                currentFrameInstancedAdditiveFallbackInstances += instances;
            }
        }

        private static String fallbackDetail(String path, RenderBackendFallbackReason reason, PreparedBatch batch,
                ResourceLocation textureLocation, LegacyTexturedRenderMode renderMode, int instances) {
            return fallbackDetail(path, reason, batch.stableKey(), textureLocation, renderMode, batch.vertexCount(),
                    instances);
        }

        private static String fallbackDetail(String path, RenderBackendFallbackReason reason, InstancedBatch batch,
                int instances) {
            InstancedMeshKey key = batch.mesh().key();
            return fallbackDetail(path, reason, key.stablePartKey(), batch.textureLocation(), batch.renderMode(),
                    key.sourceVertices(), instances);
        }

        private static String fallbackDetail(String path, RenderBackendFallbackReason reason,
                List<InstancedBatch> batches, int instances) {
            if (batches == null || batches.isEmpty()) {
                return fallbackDetail(path, reason, "none", InventoryMenu.BLOCK_ATLAS,
                        LegacyTexturedRenderMode.CUTOUT_NO_CULL, 0, instances);
            }
            InstancedBatch batch = batches.get(0);
            InstancedMeshKey key = batch.mesh().key();
            return fallbackDetail(path, reason, key.stablePartKey(), batch.textureLocation(), batch.renderMode(),
                    key.sourceVertices(), instances);
        }

        private static String fallbackDetail(String path, RenderBackendFallbackReason reason,
                IrisCompanionQueuedBatch batch, int instances) {
            IrisCompanionQueueKey key = batch.key();
            return fallbackDetail(path, reason, key.stablePartKey(), key.textureLocation(), key.renderMode(),
                    batch.sourceVertices().size(), instances);
        }

        private static String fallbackDetail(String path, RenderBackendFallbackReason reason,
                String stablePartKey, ResourceLocation textureLocation, LegacyTexturedRenderMode renderMode,
                int vertices, int instances) {
            return sanitizeFallbackDetail(path + "{reason=" + reason
                    + ",part=" + stablePartKey
                    + ",texture=" + textureLocation
                    + ",mode=" + renderMode
                    + ",vertices=" + vertices
                    + ",instances=" + instances + "}");
        }

        private static String sanitizeFallbackDetail(String detail) {
            if (detail == null || detail.isBlank()) {
                return NO_FALLBACK_DETAIL;
            }
            String compact = detail.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ').trim();
            if (compact.isEmpty()) {
                return NO_FALLBACK_DETAIL;
            }
            return compact.length() <= MAX_FALLBACK_DETAIL_LENGTH
                    ? compact
                    : compact.substring(0, MAX_FALLBACK_DETAIL_LENGTH - 3) + "...";
        }

        private void recordInstancedAdditiveFallback(int instances) {
            if (instances <= 0) {
                return;
            }
            instancedAdditiveFallbackBatches++;
            instancedAdditiveFallbackInstances += instances;
            currentFrameInstancedAdditiveFallbackBatches++;
            currentFrameInstancedAdditiveFallbackInstances += instances;
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
                clearLastInstancedMesh();
                clearPendingInstancedBatches();
                clearPendingIrisCompanionBatches();
                clearInstancedUploadScratch();
            }
            closeLater(toClose);
            closeIrisLater(irisToClose);
            closeInstancedLater(instancedToClose);
            clearOptimizedUniformCache();
            clearMdiCapabilityCache();
            mdiDispatchDisabled = false;
            mdiAtlas.resetLater();
            cpuFallback.clear(reason);
        }

        private void clearOptimizedUniformCache() {
            optimizedUniformShader = null;
            optimizedUniformProgram = -1;
            optimizedUniformPipelineGeneration = -1L;
            optimizedProjMatUniform = null;
            optimizedModelViewUniform = null;
            optimizedFogStartUniform = null;
            optimizedFogEndUniform = null;
            optimizedFogColorUniform = null;
            optimizedFadeAlphaUniform = null;
            optimizedUniformValueShader = null;
            optimizedUniformValueProgram = -1;
            optimizedUniformValuePipelineGeneration = -1L;
            clearOptimizedUniformValueCache();
        }

        @Override
        public void invalidateIrisCompanionShaderAttributeCaches() {
            synchronized (irisMeshes) {
                for (IrisCompanionMesh mesh : irisMeshes.values()) {
                    if (mesh.invalidateShaderAttributeCache()) {
                        recordIrisShaderAttributeGenerationInvalidation();
                    }
                }
            }
        }

        @Override
        public void flush(RenderBackendFlushStage stage) {
            flush(stage, null);
        }

        @Override
        public void flush(RenderBackendFlushStage stage, Matrix4f projectionMatrix) {
            if (stage == RenderBackendFlushStage.AFTER_BLOCK_ENTITIES || stage == RenderBackendFlushStage.MANUAL) {
                long startedNanos = System.nanoTime();
                boolean presentAllowed = recordOptimizedFlushAttempt(stage);
                if (presentAllowed) {
                    try {
                        HbmIrisRenderBatch.closePersistentIfActive();
                        Matrix4f resolvedProjection = null;
                        if (!pendingIrisCompanionBatches.isEmpty()) {
                            resolvedProjection = resolveProjectionMatrix(projectionMatrix);
                            flushIrisCompanionBatches(resolvedProjection);
                        }
                        HbmIrisRenderBatch.closePersistentIfActive();
                        if (!pendingInstancedBatches.isEmpty()) {
                            if (resolvedProjection == null) {
                                resolvedProjection = resolveProjectionMatrix(projectionMatrix);
                            }
                            flushInstancedBatches(resolvedProjection);
                        }
                    } catch (Throwable throwable) {
                        discardPendingBackendFlush(stage, throwable);
                    } finally {
                        recordOptimizedFlushDuration(System.nanoTime() - startedNanos);
                    }
                } else {
                    try {
                        HbmIrisRenderBatch.closePersistentIfActive();
                    } catch (Throwable closeFailure) {
                        HbmNtm.LOGGER.error("Legacy OBJ Iris/Oculus batch close after duplicate present skip failed",
                                closeFailure);
                    }
                }
            }
            try {
                cpuFallback.flush(stage);
            } catch (Throwable throwable) {
                HbmNtm.LOGGER.error("Legacy OBJ CPU fallback flush failed at {}", stage, throwable);
            }
        }

        private static Matrix4f resolveProjectionMatrix(Matrix4f projectionMatrix) {
            return projectionMatrix != null ? projectionMatrix : RenderSystem.getProjectionMatrix();
        }

        private boolean recordOptimizedFlushAttempt(RenderBackendFlushStage stage) {
            optimizedFlushCalls++;
            long previousFrameFlushes = currentFrameOptimizedFlushCalls++;
            if (previousFrameFlushes > 0L) {
                optimizedDuplicateFlushCalls++;
                currentFrameOptimizedDuplicateFlushCalls++;
            }
            if (stage == RenderBackendFlushStage.AFTER_BLOCK_ENTITIES) {
                long previousFramePresents = currentFrameOptimizedAfterBlockEntityPresents++;
                if (previousFramePresents > 0L) {
                    optimizedDuplicatePresentSkips++;
                    currentFrameOptimizedDuplicatePresentSkips++;
                    return false;
                }
            }
            return true;
        }

        private void recordOptimizedFlushDuration(long nanos) {
            if (nanos <= 0L) {
                return;
            }
            optimizedFlushNanos += nanos;
            currentFrameOptimizedFlushNanos += nanos;
        }

        private void recordOptimizedDrawStateRestoreFailure() {
            optimizedDrawStateRestoreFailures++;
            currentFrameOptimizedDrawStateRestoreFailures++;
        }

        private void discardPendingBackendFlush(RenderBackendFlushStage stage, Throwable throwable) {
            HbmNtm.LOGGER.error(
                    "Legacy OBJ render backend flush failed at {}; discarding pending instanced/companion batches",
                    stage, throwable);
            clearPendingInstancedBatches();
            clearPendingIrisCompanionBatches();
            try {
                HbmIrisRenderBatch.closePersistentIfActive();
            } catch (Throwable closeFailure) {
                HbmNtm.LOGGER.error("Legacy OBJ Iris/Oculus batch close after flush failure failed", closeFailure);
            }
        }

        private void discardStaleOptimizedQueuesAtFrameEnd() {
            if (pendingInstancedBatches.isEmpty() && pendingIrisCompanionBatches.isEmpty()) {
                return;
            }
            int instancedBatches = pendingInstancedBatches.size();
            long instancedInstances = countPendingInstancedInstances();
            int irisBatches = pendingIrisCompanionBatches.size();
            long irisInstances = countPendingIrisCompanionInstances();

            clearPendingInstancedBatches();
            clearPendingIrisCompanionBatches();
            staleInstancedBatches += instancedBatches;
            staleInstancedInstances += instancedInstances;
            staleIrisCompanionBatches += irisBatches;
            staleIrisCompanionInstances += irisInstances;
            currentFrameStaleInstancedBatches += instancedBatches;
            currentFrameStaleInstancedInstances += instancedInstances;
            currentFrameStaleIrisCompanionBatches += irisBatches;
            currentFrameStaleIrisCompanionInstances += irisInstances;

            String message = String.format(Locale.ROOT,
                    "Legacy OBJ render backend discarded stale frame-end queues: instanced=%d/%d, irisCompanion=%d/%d",
                    instancedBatches, instancedInstances, irisBatches, irisInstances);
            if (HbmClientConfig.renderBackendDiagnostics()) {
                HbmNtm.LOGGER.warn(message);
            } else {
                HbmNtm.LOGGER.debug(message);
            }
        }

        private long countPendingInstancedInstances() {
            long instances = 0L;
            for (InstancedBatch batch : pendingInstancedBatches.values()) {
                instances += batch.instances().size();
            }
            return instances;
        }

        private long countPendingIrisCompanionInstances() {
            long instances = 0L;
            for (IrisCompanionQueuedBatch batch : pendingIrisCompanionBatches.values()) {
                instances += batch.instances().size();
            }
            return instances;
        }

        @Override
        public void recordCpuFallback(RenderBackendFallbackReason fallback, int vertices) {
            cpuFallback.recordCpuFallback(fallback, vertices);
        }

        @Override
        public void endFrame() {
            discardStaleOptimizedQueuesAtFrameEnd();
            lastFrameInstancedQueuedBatches = currentFrameInstancedQueuedBatches;
            currentFrameInstancedQueuedBatches = 0L;
            lastFrameInstancedQueuedInstances = currentFrameInstancedQueuedInstances;
            currentFrameInstancedQueuedInstances = 0L;
            lastFrameInstancedDrawCalls = currentFrameInstancedDrawCalls;
            currentFrameInstancedDrawCalls = 0L;
            lastFrameInstancedFallbackBatches = currentFrameInstancedFallbackBatches;
            currentFrameInstancedFallbackBatches = 0L;
            lastFrameInstancedFallbackInstances = currentFrameInstancedFallbackInstances;
            currentFrameInstancedFallbackInstances = 0L;
            lastFrameInstancedOverflowBatches = currentFrameInstancedOverflowBatches;
            currentFrameInstancedOverflowBatches = 0L;
            lastFrameInstancedOverflowInstances = currentFrameInstancedOverflowInstances;
            currentFrameInstancedOverflowInstances = 0L;
            lastFrameInstancedDuplicateInstances = currentFrameInstancedDuplicateInstances;
            currentFrameInstancedDuplicateInstances = 0L;
            lastFrameOptimizedFlushCalls = currentFrameOptimizedFlushCalls;
            currentFrameOptimizedFlushCalls = 0L;
            lastFrameOptimizedDuplicateFlushCalls = currentFrameOptimizedDuplicateFlushCalls;
            currentFrameOptimizedDuplicateFlushCalls = 0L;
            lastFrameOptimizedDuplicatePresentSkips = currentFrameOptimizedDuplicatePresentSkips;
            currentFrameOptimizedDuplicatePresentSkips = 0L;
            currentFrameOptimizedAfterBlockEntityPresents = 0L;
            lastFrameOptimizedFlushNanos = currentFrameOptimizedFlushNanos;
            currentFrameOptimizedFlushNanos = 0L;
            lastFrameOptimizedDrawStateRestoreFailures = currentFrameOptimizedDrawStateRestoreFailures;
            currentFrameOptimizedDrawStateRestoreFailures = 0L;
            lastFrameStaleInstancedBatches = currentFrameStaleInstancedBatches;
            currentFrameStaleInstancedBatches = 0L;
            lastFrameStaleInstancedInstances = currentFrameStaleInstancedInstances;
            currentFrameStaleInstancedInstances = 0L;
            lastFrameStaleIrisCompanionBatches = currentFrameStaleIrisCompanionBatches;
            currentFrameStaleIrisCompanionBatches = 0L;
            lastFrameStaleIrisCompanionInstances = currentFrameStaleIrisCompanionInstances;
            currentFrameStaleIrisCompanionInstances = 0L;
            lastFrameInstancedAdditiveQueuedBatches = currentFrameInstancedAdditiveQueuedBatches;
            currentFrameInstancedAdditiveQueuedBatches = 0L;
            lastFrameInstancedAdditiveQueuedInstances = currentFrameInstancedAdditiveQueuedInstances;
            currentFrameInstancedAdditiveQueuedInstances = 0L;
            lastFrameInstancedAdditiveDrawCalls = currentFrameInstancedAdditiveDrawCalls;
            currentFrameInstancedAdditiveDrawCalls = 0L;
            lastFrameInstancedAdditiveFallbackBatches = currentFrameInstancedAdditiveFallbackBatches;
            currentFrameInstancedAdditiveFallbackBatches = 0L;
            lastFrameInstancedAdditiveFallbackInstances = currentFrameInstancedAdditiveFallbackInstances;
            currentFrameInstancedAdditiveFallbackInstances = 0L;
            lastFrameInstancedAdditiveOverflowBatches = currentFrameInstancedAdditiveOverflowBatches;
            currentFrameInstancedAdditiveOverflowBatches = 0L;
            lastFrameInstancedAdditiveOverflowInstances = currentFrameInstancedAdditiveOverflowInstances;
            currentFrameInstancedAdditiveOverflowInstances = 0L;
            lastFrameMdiEligibleFlushes = currentFrameMdiEligibleFlushes;
            currentFrameMdiEligibleFlushes = 0L;
            lastFrameMdiEligibleBatches = currentFrameMdiEligibleBatches;
            currentFrameMdiEligibleBatches = 0L;
            lastFrameMdiFallbackFlushes = currentFrameMdiFallbackFlushes;
            currentFrameMdiFallbackFlushes = 0L;
            lastFrameMdiFallbackBatches = currentFrameMdiFallbackBatches;
            currentFrameMdiFallbackBatches = 0L;
            lastFrameMdiDrawCalls = currentFrameMdiDrawCalls;
            currentFrameMdiDrawCalls = 0L;
            lastFrameMdiMultiDrawCalls = currentFrameMdiMultiDrawCalls;
            currentFrameMdiMultiDrawCalls = 0L;
            lastFrameMdiIndirectCommands = currentFrameMdiIndirectCommands;
            currentFrameMdiIndirectCommands = 0L;
            lastFrameMdiNoSlotBatches = currentFrameMdiNoSlotBatches;
            currentFrameMdiNoSlotBatches = 0L;
            lastFrameMdiNoSlotInstances = currentFrameMdiNoSlotInstances;
            currentFrameMdiNoSlotInstances = 0L;
            lastFrameMdiPartialDrawFailures = currentFrameMdiPartialDrawFailures;
            currentFrameMdiPartialDrawFailures = 0L;
            lastFrameMdiStalePreparedGroups = currentFrameMdiStalePreparedGroups;
            currentFrameMdiStalePreparedGroups = 0L;
            lastFrameMdiStalePreparedCommands = currentFrameMdiStalePreparedCommands;
            currentFrameMdiStalePreparedCommands = 0L;
            lastFrameMdiDispatchDisableEvents = currentFrameMdiDispatchDisableEvents;
            currentFrameMdiDispatchDisableEvents = 0L;
            lastFrameMdiAtlasRepackFailures = currentFrameMdiAtlasRepackFailures;
            currentFrameMdiAtlasRepackFailures = 0L;
            lastFrameMdiAtlasInitFailures = currentFrameMdiAtlasInitFailures;
            currentFrameMdiAtlasInitFailures = 0L;
            lastFrameMdiAdditiveEligibleBatches = currentFrameMdiAdditiveEligibleBatches;
            currentFrameMdiAdditiveEligibleBatches = 0L;
            lastFrameMdiAdditiveFallbackBatches = currentFrameMdiAdditiveFallbackBatches;
            currentFrameMdiAdditiveFallbackBatches = 0L;
            lastFrameMdiAdditiveDrawCalls = currentFrameMdiAdditiveDrawCalls;
            currentFrameMdiAdditiveDrawCalls = 0L;
            lastFrameMdiAdditiveIndirectCommands = currentFrameMdiAdditiveIndirectCommands;
            currentFrameMdiAdditiveIndirectCommands = 0L;
            lastFrameMdiAdditiveNoSlotBatches = currentFrameMdiAdditiveNoSlotBatches;
            currentFrameMdiAdditiveNoSlotBatches = 0L;
            lastFrameMdiAdditiveNoSlotInstances = currentFrameMdiAdditiveNoSlotInstances;
            currentFrameMdiAdditiveNoSlotInstances = 0L;
            lastFrameGpuDrawCalls = currentFrameGpuDrawCalls;
            currentFrameGpuDrawCalls = 0L;
            lastFrameGpuFallbackBatches = currentFrameGpuFallbackBatches;
            currentFrameGpuFallbackBatches = 0L;
            lastFrameGpuFallbackVertices = currentFrameGpuFallbackVertices;
            currentFrameGpuFallbackVertices = 0L;
            lastFrameIrisEligibleBatches = currentFrameIrisEligibleBatches;
            currentFrameIrisEligibleBatches = 0L;
            lastFrameIrisDrawCalls = currentFrameIrisDrawCalls;
            currentFrameIrisDrawCalls = 0L;
            lastFrameIrisShadowDrawCalls = currentFrameIrisShadowDrawCalls;
            currentFrameIrisShadowDrawCalls = 0L;
            lastFrameIrisFallbackBatches = currentFrameIrisFallbackBatches;
            currentFrameIrisFallbackBatches = 0L;
            lastFrameIrisFallbackVertices = currentFrameIrisFallbackVertices;
            currentFrameIrisFallbackVertices = 0L;
            lastFrameIrisLightmapStorageFailures = currentFrameIrisLightmapStorageFailures;
            currentFrameIrisLightmapStorageFailures = 0L;
            lastFrameIrisLightmapSlotReuses = currentFrameIrisLightmapSlotReuses;
            currentFrameIrisLightmapSlotReuses = 0L;
            lastFrameIrisLightmapSlotUploads = currentFrameIrisLightmapSlotUploads;
            currentFrameIrisLightmapSlotUploads = 0L;
            lastFrameIrisLightmapStagingFallbacks = currentFrameIrisLightmapStagingFallbacks;
            currentFrameIrisLightmapStagingFallbacks = 0L;
            lastFrameIrisShaderAttributeCacheHits = currentFrameIrisShaderAttributeCacheHits;
            currentFrameIrisShaderAttributeCacheHits = 0L;
            lastFrameIrisShaderAttributeCacheMisses = currentFrameIrisShaderAttributeCacheMisses;
            currentFrameIrisShaderAttributeCacheMisses = 0L;
            lastFrameIrisShaderAttributeGenerationInvalidations =
                    currentFrameIrisShaderAttributeGenerationInvalidations;
            currentFrameIrisShaderAttributeGenerationInvalidations = 0L;
            lastFrameIrisShaderAttributePrimedSkips = currentFrameIrisShaderAttributePrimedSkips;
            currentFrameIrisShaderAttributePrimedSkips = 0L;
            lastFrameIrisShaderAttributeVaoBindFailures = currentFrameIrisShaderAttributeVaoBindFailures;
            currentFrameIrisShaderAttributeVaoBindFailures = 0L;
            lastFrameIrisQueuedBatches = currentFrameIrisQueuedBatches;
            currentFrameIrisQueuedBatches = 0L;
            lastFrameIrisQueuedInstances = currentFrameIrisQueuedInstances;
            currentFrameIrisQueuedInstances = 0L;
            lastFrameIrisQueuedFlushes = currentFrameIrisQueuedFlushes;
            currentFrameIrisQueuedFlushes = 0L;
            lastFrameIrisQueuedDrawCalls = currentFrameIrisQueuedDrawCalls;
            currentFrameIrisQueuedDrawCalls = 0L;
            lastFrameIrisQueuedFallbackBatches = currentFrameIrisQueuedFallbackBatches;
            currentFrameIrisQueuedFallbackBatches = 0L;
            lastFrameIrisQueuedFallbackInstances = currentFrameIrisQueuedFallbackInstances;
            currentFrameIrisQueuedFallbackInstances = 0L;
            lastFrameIrisQueuedDuplicateInstances = currentFrameIrisQueuedDuplicateInstances;
            currentFrameIrisQueuedDuplicateInstances = 0L;
            HbmIrisRenderBatch.endFrame();
            cpuFallback.endFrame();
        }

        @Override
        public RenderBackendSnapshot snapshot() {
            RenderBackendSnapshot cpu = cpuFallback.snapshot();
            boolean experimentalEnabled = HbmRenderFrameFlags.current().experimentalGpuBackendEnabled();
            long totalDrawCalls = cpu.estimatedDrawCalls() + gpuDrawCalls;
            long currentDrawCalls = cpu.currentFrameEstimatedDrawCalls() + currentFrameGpuDrawCalls;
            long lastDrawCalls = cpu.lastFrameEstimatedDrawCalls() + lastFrameGpuDrawCalls;
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
                    cpu.texturedDrawCalls() + gpuDrawCalls,
                    cpu.spriteDrawCalls(),
                    cpu.untexturedDrawCalls(),
                    cpu.textureSwitches(),
                    cpu.renderModeSwitches(),
                    cpu.cpuFallbackBatches() + gpuFallbackBatches,
                    cpu.cpuFallbackVertices() + gpuFallbackVertices,
                    cpu.texturedClippedFallbackBatches(),
                    cpu.texturedClippedFallbackVertices(),
                    cpu.untexturedClippedFallbackBatches(),
                    cpu.untexturedClippedFallbackVertices(),
                    lastGpuFallbackReason == RenderBackendFallbackReason.NONE ? cpu.lastFallbackReason() : lastGpuFallbackReason,
                    NO_FALLBACK_DETAIL.equals(lastGpuFallbackDetail) ? cpu.lastFallbackDetail() : lastGpuFallbackDetail,
                    cpu.frameGeneration(),
                    cpu.currentFrameTexturedBatches(),
                    cpu.currentFrameTexturedVertices(),
                    cpu.currentFrameSpriteBatches(),
                    cpu.currentFrameSpriteVertices(),
                    cpu.currentFrameUntexturedBatches(),
                    cpu.currentFrameUntexturedVertices(),
                    cpu.currentFrameCpuFallbackBatches() + currentFrameGpuFallbackBatches,
                    cpu.currentFrameCpuFallbackVertices() + currentFrameGpuFallbackVertices,
                    cpu.currentFrameBackendFlushes(),
                    currentDrawCalls,
                    cpu.currentFrameTextureSwitches(),
                    gpuMeshCount(),
                    gpuBufferBytes.get() + irisMeshBytes.get() + mdiAtlas.byteCapacity(),
                    gpuUploadAttempts.get(),
                    gpuUploadFailures.get(),
                    gpuDrawCalls,
                    gpuFallbackBatches,
                    gpuFallbackVertices,
                    instancedQueuedBatches,
                    instancedQueuedInstances,
                    instancedFlushes,
                    instancedDrawCalls,
                    instancedFallbackBatches,
                    instancedFallbackInstances,
                    instancedOverflowBatches,
                    instancedOverflowInstances,
                    experimentalEnabled && mdiAvailable(),
                    experimentalEnabled && mdiDrawIndirectSupported,
                    experimentalEnabled && mdiMultiDrawIndirectSupported,
                    experimentalEnabled && mdiBaseInstanceSupported,
                    mdiEligibleFlushes,
                    mdiEligibleBatches,
                    mdiFallbackFlushes,
                    mdiFallbackBatches,
                    mdiDrawCalls,
                    mdiMultiDrawCalls,
                    mdiIndirectCommands,
                    mdiAtlas.partCount(),
                    mdiAtlas.byteCapacity(),
                    mdiNoSlotBatches,
                    mdiNoSlotInstances,
                    currentFrameMdiEligibleFlushes,
                    currentFrameMdiEligibleBatches,
                    currentFrameMdiFallbackFlushes,
                    currentFrameMdiFallbackBatches,
                    currentFrameMdiDrawCalls,
                    currentFrameMdiIndirectCommands,
                    currentFrameMdiNoSlotBatches,
                    currentFrameMdiNoSlotInstances,
                    lastFrameMdiEligibleFlushes,
                    lastFrameMdiEligibleBatches,
                    lastFrameMdiFallbackFlushes,
                    lastFrameMdiFallbackBatches,
                    lastFrameMdiDrawCalls,
                    lastFrameMdiIndirectCommands,
                    lastFrameMdiNoSlotBatches,
                    lastFrameMdiNoSlotInstances,
                    currentFrameInstancedQueuedBatches,
                    currentFrameInstancedQueuedInstances,
                    currentFrameInstancedDrawCalls,
                    currentFrameInstancedOverflowBatches,
                    currentFrameInstancedOverflowInstances,
                    lastFrameInstancedQueuedBatches,
                    lastFrameInstancedQueuedInstances,
                    lastFrameInstancedDrawCalls,
                    lastFrameInstancedOverflowBatches,
                    lastFrameInstancedOverflowInstances,
                    currentFrameGpuDrawCalls,
                    currentFrameGpuFallbackBatches,
                    currentFrameGpuFallbackVertices,
                    lastFrameGpuDrawCalls,
                    lastFrameGpuFallbackBatches,
                    lastFrameGpuFallbackVertices,
                    cpu.lastFrameTexturedBatches(),
                    cpu.lastFrameTexturedVertices(),
                    cpu.lastFrameSpriteBatches(),
                    cpu.lastFrameSpriteVertices(),
                    cpu.lastFrameUntexturedBatches(),
                    cpu.lastFrameUntexturedVertices(),
                    cpu.lastFrameCpuFallbackBatches() + lastFrameGpuFallbackBatches,
                    cpu.lastFrameCpuFallbackVertices() + lastFrameGpuFallbackVertices,
                    cpu.lastFrameBackendFlushes(),
                    lastDrawCalls,
                    cpu.lastFrameTextureSwitches());
        }

        @Override
        public RenderBackendAdditiveSnapshot additiveSnapshot() {
            return new RenderBackendAdditiveSnapshot(
                    new InstancedAdditiveSnapshot(
                            instancedAdditiveQueuedBatches,
                            instancedAdditiveQueuedInstances,
                            instancedAdditiveDrawCalls,
                            instancedAdditiveFallbackBatches,
                            instancedAdditiveFallbackInstances,
                            instancedAdditiveOverflowBatches,
                            instancedAdditiveOverflowInstances,
                            currentFrameInstancedAdditiveQueuedBatches,
                            currentFrameInstancedAdditiveQueuedInstances,
                            currentFrameInstancedAdditiveDrawCalls,
                            currentFrameInstancedAdditiveFallbackBatches,
                            currentFrameInstancedAdditiveFallbackInstances,
                            currentFrameInstancedAdditiveOverflowBatches,
                            currentFrameInstancedAdditiveOverflowInstances,
                            lastFrameInstancedAdditiveQueuedBatches,
                            lastFrameInstancedAdditiveQueuedInstances,
                            lastFrameInstancedAdditiveDrawCalls,
                            lastFrameInstancedAdditiveFallbackBatches,
                            lastFrameInstancedAdditiveFallbackInstances,
                            lastFrameInstancedAdditiveOverflowBatches,
                            lastFrameInstancedAdditiveOverflowInstances),
                    new MdiAdditiveSnapshot(
                            mdiAdditiveEligibleBatches,
                            mdiAdditiveFallbackBatches,
                            mdiAdditiveDrawCalls,
                            mdiAdditiveIndirectCommands,
                            mdiAdditiveNoSlotBatches,
                            mdiAdditiveNoSlotInstances,
                            currentFrameMdiAdditiveEligibleBatches,
                            currentFrameMdiAdditiveFallbackBatches,
                            currentFrameMdiAdditiveDrawCalls,
                            currentFrameMdiAdditiveIndirectCommands,
                            currentFrameMdiAdditiveNoSlotBatches,
                            currentFrameMdiAdditiveNoSlotInstances,
                            lastFrameMdiAdditiveEligibleBatches,
                            lastFrameMdiAdditiveFallbackBatches,
                            lastFrameMdiAdditiveDrawCalls,
                            lastFrameMdiAdditiveIndirectCommands,
                            lastFrameMdiAdditiveNoSlotBatches,
                            lastFrameMdiAdditiveNoSlotInstances));
        }

        @Override
        public RenderBackendInstancingSnapshot instancingSnapshot() {
            return new RenderBackendInstancingSnapshot(
                    optimizedFlushCalls,
                    currentFrameOptimizedFlushCalls,
                    lastFrameOptimizedFlushCalls,
                    optimizedDuplicateFlushCalls,
                    currentFrameOptimizedDuplicateFlushCalls,
                    lastFrameOptimizedDuplicateFlushCalls,
                    optimizedDuplicatePresentSkips,
                    currentFrameOptimizedDuplicatePresentSkips,
                    lastFrameOptimizedDuplicatePresentSkips,
                    optimizedFlushNanos,
                    currentFrameOptimizedFlushNanos,
                    lastFrameOptimizedFlushNanos,
                    optimizedDrawStateRestoreFailures,
                    currentFrameOptimizedDrawStateRestoreFailures,
                    lastFrameOptimizedDrawStateRestoreFailures,
                    instancedDuplicateInstances,
                    currentFrameInstancedDuplicateInstances,
                    lastFrameInstancedDuplicateInstances,
                    staleInstancedBatches,
                    staleInstancedInstances,
                    staleIrisCompanionBatches,
                    staleIrisCompanionInstances,
                    currentFrameStaleInstancedBatches,
                    currentFrameStaleInstancedInstances,
                    currentFrameStaleIrisCompanionBatches,
                    currentFrameStaleIrisCompanionInstances,
                    lastFrameStaleInstancedBatches,
                    lastFrameStaleInstancedInstances,
                    lastFrameStaleIrisCompanionBatches,
                    lastFrameStaleIrisCompanionInstances,
                    mdiPartialDrawFailures,
                    currentFrameMdiPartialDrawFailures,
                    lastFrameMdiPartialDrawFailures,
                    mdiStalePreparedGroups,
                    currentFrameMdiStalePreparedGroups,
                    lastFrameMdiStalePreparedGroups,
                    mdiStalePreparedCommands,
                    currentFrameMdiStalePreparedCommands,
                    lastFrameMdiStalePreparedCommands,
                    mdiDispatchDisabled,
                    mdiDispatchDisableEvents,
                    currentFrameMdiDispatchDisableEvents,
                    lastFrameMdiDispatchDisableEvents,
                    currentFrameMdiMultiDrawCalls,
                    lastFrameMdiMultiDrawCalls,
                    mdiAtlasRepackFailures,
                    currentFrameMdiAtlasRepackFailures,
                    lastFrameMdiAtlasRepackFailures,
                    mdiAtlasInitFailures,
                    currentFrameMdiAtlasInitFailures,
                    lastFrameMdiAtlasInitFailures);
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
                        irisEligibleBatches,
                        irisDrawCalls,
                        irisFallbackBatches,
                        irisFallbackVertices,
                        irisUploadAttempts.get(),
                        irisUploadFailures.get(),
                        currentFrameIrisEligibleBatches,
                        currentFrameIrisDrawCalls,
                        currentFrameIrisFallbackBatches,
                        currentFrameIrisFallbackVertices,
                        lastFrameIrisEligibleBatches,
                        lastFrameIrisDrawCalls,
                        lastFrameIrisFallbackBatches,
                        lastFrameIrisFallbackVertices,
                        irisShadowDrawCalls,
                        currentFrameIrisShadowDrawCalls,
                        lastFrameIrisShadowDrawCalls,
                        irisLightmapStorageFailures,
                        currentFrameIrisLightmapStorageFailures,
                        lastFrameIrisLightmapStorageFailures,
                        irisLightmapSlotReuses,
                        currentFrameIrisLightmapSlotReuses,
                        lastFrameIrisLightmapSlotReuses,
                        irisLightmapSlotUploads,
                        currentFrameIrisLightmapSlotUploads,
                        lastFrameIrisLightmapSlotUploads,
                        irisLightmapStagingFallbacks,
                        currentFrameIrisLightmapStagingFallbacks,
                        lastFrameIrisLightmapStagingFallbacks,
                        new IrisCompanionShaderSnapshot(
                                irisShaderAttributeCacheHits,
                                currentFrameIrisShaderAttributeCacheHits,
                                lastFrameIrisShaderAttributeCacheHits,
                                irisShaderAttributeCacheMisses,
                                currentFrameIrisShaderAttributeCacheMisses,
                                lastFrameIrisShaderAttributeCacheMisses,
                                irisShaderAttributeGenerationInvalidations,
                                currentFrameIrisShaderAttributeGenerationInvalidations,
                                lastFrameIrisShaderAttributeGenerationInvalidations,
                                irisShaderAttributePrimedSkips,
                                currentFrameIrisShaderAttributePrimedSkips,
                                lastFrameIrisShaderAttributePrimedSkips,
                                irisShaderAttributeVaoBindFailures,
                                currentFrameIrisShaderAttributeVaoBindFailures,
                                lastFrameIrisShaderAttributeVaoBindFailures),
                        HbmIrisRenderBatch.snapshot(),
                        new IrisCompanionQueueSnapshot(
                                irisQueuedBatches,
                                irisQueuedInstances,
                                irisQueuedFlushes,
                                irisQueuedDrawCalls,
                                irisQueuedFallbackBatches,
                                irisQueuedFallbackInstances,
                                currentFrameIrisQueuedBatches,
                                currentFrameIrisQueuedInstances,
                                currentFrameIrisQueuedFlushes,
                                currentFrameIrisQueuedDrawCalls,
                                currentFrameIrisQueuedFallbackBatches,
                                currentFrameIrisQueuedFallbackInstances,
                                irisQueuedDuplicateInstances,
                                currentFrameIrisQueuedDuplicateInstances,
                                lastFrameIrisQueuedDuplicateInstances,
                                lastFrameIrisQueuedBatches,
                                lastFrameIrisQueuedInstances,
                                lastFrameIrisQueuedFlushes,
                                lastFrameIrisQueuedDrawCalls,
                                lastFrameIrisQueuedFallbackBatches,
                                lastFrameIrisQueuedFallbackInstances));
            }
        }

        private record IrisCompanionQueuedDraw(IrisCompanionQueuedBatch batch, IrisCompanionQueuedInstance instance,
                                               IrisCompanionMesh mesh, int preparedLightmapSlot) {
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
            private final List<ByteBuffer> instancePrepareScratch = new ArrayList<>();
            private final List<ByteBuffer> commandPrepareScratch = new ArrayList<>();
            private int prepareScratchCursor;
            private int layoutGeneration;
            private boolean initializationFailed;
            private boolean offThreadInitWarningLogged;

            private synchronized void beginPreparePass() {
                prepareScratchCursor = 0;
            }

            private synchronized MdiPreparedGroup prepare(MdiDrawGroupKey key, List<InstancedBatch> batches,
                    int totalInstances, List<InstancedBatch> noSlotBatches) {
                if (batches.isEmpty()) {
                    return null;
                }
                if (!ensureReady()) {
                    return null;
                }
                if (totalInstances <= 0) {
                    return null;
                }
                long instanceBytesRequired = (long) totalInstances * InstancedInstance.FLOATS * 4L;
                long commandBytesRequired = (long) batches.size() * MdiPreparedGroup.COMMAND_STRIDE_BYTES;
                if (instanceBytesRequired > MAX_INSTANCE_BYTES || commandBytesRequired > MAX_INDIRECT_BYTES) {
                    return null;
                }
                int scratchIndex = prepareScratchCursor++;
                ByteBuffer instanceBytes = prepareScratch(instancePrepareScratch, scratchIndex,
                        (int) instanceBytesRequired);
                ByteBuffer commandBytes = prepareScratch(commandPrepareScratch, scratchIndex,
                        (int) commandBytesRequired);
                boolean captureSubDraws = HbmMdiRenderDiag.isVerboseSubdrawsEnabled();
                List<MdiSubDraw> subDraws = captureSubDraws ? new ArrayList<>(batches.size()) : List.of();
                List<InstancedBatch> preparedBatches = null;
                int baseInstance = 0;
                int commandCount = 0;
                int preparedInstances = 0;
                long instanceBaseAddress = MemoryUtil.memAddress(instanceBytes);
                long commandBaseAddress = MemoryUtil.memAddress(commandBytes);
                for (int batchIndex = 0; batchIndex < batches.size(); batchIndex++) {
                    InstancedBatch batch = batches.get(batchIndex);
                    if (batch.instances().isEmpty()) {
                        preparedBatches = prepareFilteredMdiBatches(batches, preparedBatches, batchIndex);
                        continue;
                    }
                    MdiSlot slot = slotFor(batch.mesh());
                    if (slot == null) {
                        noSlotBatches.add(batch);
                        preparedBatches = prepareFilteredMdiBatches(batches, preparedBatches, batchIndex);
                        continue;
                    }
                    int batchInstances = batch.instances().size();
                    for (InstancedInstance instance : batch.instances()) {
                        instance.write(instanceBaseAddress
                                + (long) preparedInstances * InstancedInstance.FLOATS * 4L);
                        preparedInstances++;
                    }
                    MdiPreparedGroup.writeDrawArraysIndirectCommand(commandBaseAddress
                            + (long) commandCount * MdiPreparedGroup.COMMAND_STRIDE_BYTES,
                            slot.vertexCount(), batchInstances, slot.firstVertex(), baseInstance);
                    if (captureSubDraws) {
                        subDraws.add(new MdiSubDraw(commandCount, batch.mesh().key(), slot.firstVertex(),
                                slot.vertexCount(), baseInstance, batchInstances));
                    }
                    if (preparedBatches != null) {
                        preparedBatches.add(batch);
                    }
                    baseInstance += batchInstances;
                    commandCount++;
                }
                if (commandCount <= 0 || preparedInstances <= 0) {
                    return null;
                }
                instanceBytes.position((int) ((long) preparedInstances * InstancedInstance.FLOATS * 4L));
                instanceBytes.flip();
                commandBytes.position(commandCount * MdiPreparedGroup.COMMAND_STRIDE_BYTES);
                commandBytes.flip();
                return new MdiPreparedGroup(key, preparedBatches != null ? preparedBatches : batches,
                        instanceBytes, commandBytes,
                        commandCount, preparedInstances, layoutGeneration, subDraws);
            }

            private static List<InstancedBatch> prepareFilteredMdiBatches(List<InstancedBatch> batches,
                    List<InstancedBatch> preparedBatches, int skippedBatchIndex) {
                if (preparedBatches != null) {
                    return preparedBatches;
                }
                List<InstancedBatch> filtered = new ArrayList<>(batches.size());
                filtered.addAll(batches.subList(0, skippedBatchIndex));
                return filtered;
            }

            private ByteBuffer prepareScratch(List<ByteBuffer> buffers, int index, int requiredBytes) {
                int capacity = Math.max(requiredBytes, 1);
                while (buffers.size() <= index) {
                    buffers.add(null);
                }
                ByteBuffer scratch = buffers.get(index);
                if (scratch == null || scratch.capacity() < capacity) {
                    freeNativeScratch(scratch, "legacy OBJ MDI prepare scratch resize");
                    scratch = MemoryUtil.memAlloc(capacity).order(ByteOrder.nativeOrder());
                    buffers.set(index, scratch);
                }
                scratch.clear();
                return scratch;
            }

            private boolean ensureReady() {
                if (initializationFailed) {
                    return false;
                }
                if (vaoId != 0) {
                    return true;
                }
                if (!RenderSystem.isOnRenderThread()) {
                    if (!offThreadInitWarningLogged) {
                        offThreadInitWarningLogged = true;
                        HbmNtm.LOGGER.warn(
                                "Legacy OBJ MDI atlas init requested off render thread; using instanced fallback");
                    }
                    return false;
                }
                MdiBindingGuard guard = MdiBindingGuard.snapshot(true);
                try {
                    vaoId = GL30.glGenVertexArrays();
                    vertexVboId = GL15.glGenBuffers();
                    instanceVboId = GL15.glGenBuffers();
                    indirectBufferId = GL15.glGenBuffers();
                    if (vaoId == 0 || vertexVboId == 0 || instanceVboId == 0 || indirectBufferId == 0) {
                        throw new IllegalStateException("Failed to allocate legacy OBJ MDI atlas buffers");
                    }
                    HbmGlVaoSafety.bindVertexArray(vaoId);
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
                        HbmInstancedGlCompat.vertexAttribDivisor(attribute, 1);
                    }

                    GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, indirectBufferId);
                    indirectCapacityBytes = INITIAL_INDIRECT_BYTES;
                    GL15.glBufferData(GL40.GL_DRAW_INDIRECT_BUFFER, indirectCapacityBytes, GL15.GL_STREAM_DRAW);
                    return true;
                } catch (RuntimeException exception) {
                    resetNow();
                    initializationFailed = true;
                    recordMdiAtlasInitFailure();
                    HbmNtm.LOGGER.error(
                            "Legacy OBJ MDI atlas init failed; disabling MDI atlas until backend reset", exception);
                    return false;
                } finally {
                    guard.restore();
                }
            }

            private MdiSlot slotFor(InstancedMesh mesh) {
                MdiSlot existing = slots.get(mesh.key());
                if (existing != null) {
                    return existing;
                }
                ByteBuffer vertexBytes = buildInstancedVertexBytes(mesh.key().kind(), mesh.sourceMode(),
                        mesh.sourceVertices(), mesh.key().smoothing(), mesh.key().sprite(), mesh.bounds());
                ByteBuffer retainedVertexBytes = null;
                MdiBindingGuard guard = MdiBindingGuard.snapshot(false);
                try {
                    int byteSize = vertexBytes.limit();
                    if (byteSize <= 0 || byteSize % INSTANCED_VERTEX_STRIDE_BYTES != 0) {
                        throw new IllegalStateException("Invalid legacy OBJ MDI slot vertex bytes: " + byteSize);
                    }
                    if (!ensureVertexCapacity(byteSize)) {
                        return null;
                    }
                    retainedVertexBytes = copyMdiVertexBytes(vertexBytes);
                    int firstVertex = (int) (vertexUsedBytes / INSTANCED_VERTEX_STRIDE_BYTES);
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVboId);
                    GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, vertexUsedBytes, retainedVertexBytes.duplicate());
                    MdiSlot created = new MdiSlot(mesh, firstVertex,
                            byteSize / INSTANCED_VERTEX_STRIDE_BYTES, byteSize, retainedVertexBytes);
                    retainedVertexBytes = null;
                    slots.put(mesh.key(), created);
                    vertexUsedBytes += byteSize;
                    return created;
                } catch (RuntimeException exception) {
                    freeNativeScratch(retainedVertexBytes, "legacy OBJ MDI retained vertex copy after slot failure");
                    throw exception;
                } finally {
                    guard.restore();
                }
            }

            private ByteBuffer copyMdiVertexBytes(ByteBuffer vertexBytes) {
                ByteBuffer copy = MemoryUtil.memAlloc(vertexBytes.limit()).order(ByteOrder.nativeOrder());
                try {
                    copy.put(vertexBytes.duplicate());
                    copy.flip();
                    return copy;
                } catch (RuntimeException exception) {
                    freeNativeScratch(copy, "legacy OBJ MDI retained vertex copy after copy failure");
                    throw exception;
                }
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
                MdiBindingGuard guard = MdiBindingGuard.snapshot(false);
                try {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVboId);
                    return repackSlotsIntoBoundVertexBuffer("vertex capacity grow", newCapacity);
                } finally {
                    guard.restore();
                }
            }

            private void evictLater(InstancedMesh mesh) {
                if (mesh == null) {
                    return;
                }
                Runnable evict = () -> evictNow(mesh);
                if (RenderSystem.isOnRenderThread()) {
                    evict.run();
                } else {
                    RenderSystem.recordRenderCall(evict::run);
                }
            }

            private synchronized void evictNow(InstancedMesh mesh) {
                MdiSlot removed = slots.remove(mesh.key());
                if (removed == null) {
                    return;
                }
                removed.close();
                if (slots.isEmpty()) {
                    vertexUsedBytes = 0L;
                    layoutGeneration++;
                    return;
                }
                if (vertexVboId == 0 || vertexCapacityBytes <= 0L) {
                    clearSlots();
                    vertexUsedBytes = 0L;
                    layoutGeneration++;
                    return;
                }
                MdiBindingGuard guard = MdiBindingGuard.snapshot(false);
                try {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVboId);
                    repackSlotsIntoBoundVertexBuffer("instanced mesh eviction", vertexCapacityBytes);
                } finally {
                    guard.restore();
                }
            }

            private boolean repackSlotsIntoBoundVertexBuffer(String context, long targetCapacityBytes) {
                List<MdiRepackEntry> entries = new ArrayList<>(slots.size());
                long usedBytes = 0L;
                try {
                    for (MdiSlot slot : slots.values()) {
                        ByteBuffer bytes = slot.vertexBytes();
                        int byteSize = slot.byteSize();
                        if (byteSize <= 0 || byteSize % INSTANCED_VERTEX_STRIDE_BYTES != 0) {
                            throw new IllegalStateException("Invalid legacy OBJ MDI repack vertex bytes: "
                                    + byteSize);
                        }
                        int firstVertex = (int) (usedBytes / INSTANCED_VERTEX_STRIDE_BYTES);
                        entries.add(new MdiRepackEntry(slot, bytes, firstVertex,
                                byteSize / INSTANCED_VERTEX_STRIDE_BYTES, byteSize, usedBytes));
                        usedBytes += byteSize;
                    }
                    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, targetCapacityBytes, GL15.GL_STATIC_DRAW);
                    vertexCapacityBytes = targetCapacityBytes;
                    for (MdiRepackEntry entry : entries) {
                        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, entry.byteOffset(), entry.vertexBytes().duplicate());
                    }
                    for (MdiRepackEntry entry : entries) {
                        entry.slot().update(entry.firstVertex(), entry.vertexCount(), entry.byteSize());
                    }
                    vertexUsedBytes = usedBytes;
                    layoutGeneration++;
                    return true;
                } catch (RuntimeException exception) {
                    recordMdiAtlasRepackFailure();
                    clearSlots();
                    vertexUsedBytes = 0L;
                    layoutGeneration++;
                    HbmNtm.LOGGER.warn(
                            "Legacy OBJ MDI atlas repack failed during {}; clearing atlas slots for instanced fallback",
                            context, exception);
                    return false;
                }
            }

            private void clearSlots() {
                for (MdiSlot slot : slots.values()) {
                    slot.close();
                }
                slots.clear();
            }

            private record MdiRepackEntry(MdiSlot slot, ByteBuffer vertexBytes, int firstVertex,
                                           int vertexCount, int byteSize, long byteOffset) {
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
                MdiBindingGuard guard = MdiBindingGuard.snapshot(false);
                try {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceVboId);
                    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, newCapacity, GL15.GL_STREAM_DRAW);
                    instanceCapacityBytes = newCapacity;
                } finally {
                    guard.restore();
                }
            }

            private void orphanInstanceBufferIfConfigured(HbmRenderFrameFlags.Snapshot flags) {
                if (!flags.instanceVboOrphanBeforeUpload()) {
                    return;
                }
                // drawMdiPreparedGroup binds instanceVboId immediately before this call.
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
                MdiBindingGuard guard = MdiBindingGuard.snapshot(true);
                try {
                    GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, indirectBufferId);
                    GL15.glBufferData(GL40.GL_DRAW_INDIRECT_BUFFER, newCapacity, GL15.GL_STREAM_DRAW);
                    indirectCapacityBytes = newCapacity;
                } finally {
                    guard.restore();
                }
            }

            private void orphanIndirectBufferIfConfigured(HbmRenderFrameFlags.Snapshot flags) {
                if (!flags.instanceVboOrphanBeforeUpload()) {
                    return;
                }
                // drawMdiPreparedGroup binds indirectBufferId immediately before this call.
                GL15.glBufferData(GL40.GL_DRAW_INDIRECT_BUFFER, indirectCapacityBytes, GL15.GL_STREAM_DRAW);
            }

            private static final class MdiBindingGuard {
                private final int previousVao;
                private final int previousArrayBuffer;
                private final int previousElementArrayBuffer;
                private final int previousDrawIndirectBuffer;
                private final boolean restoreDrawIndirectBuffer;

                private MdiBindingGuard(boolean includeDrawIndirectBuffer) {
                    this.previousVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
                    this.previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                    this.previousElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
                    this.restoreDrawIndirectBuffer = includeDrawIndirectBuffer;
                    this.previousDrawIndirectBuffer = includeDrawIndirectBuffer
                            ? GL11.glGetInteger(GL40.GL_DRAW_INDIRECT_BUFFER_BINDING)
                            : 0;
                }

                private static MdiBindingGuard snapshot(boolean includeDrawIndirectBuffer) {
                    return new MdiBindingGuard(includeDrawIndirectBuffer);
                }

                private void restore() {
                    restore(previousVao, previousArrayBuffer, previousElementArrayBuffer,
                            previousDrawIndirectBuffer);
                }

                private void restoreAfterDeleting(int deletedVao, int deletedArrayBufferA, int deletedArrayBufferB,
                        int deletedDrawIndirectBuffer) {
                    boolean deletedPreviousVao = previousVao == deletedVao;
                    int restoredArrayBuffer = GlObjectDeleteGuard.deletedBufferBinding(previousArrayBuffer,
                            deletedArrayBufferA, deletedArrayBufferB, deletedDrawIndirectBuffer);
                    int restoredElementArrayBuffer = deletedPreviousVao
                            ? 0
                            : GlObjectDeleteGuard.deletedBufferBinding(previousElementArrayBuffer, deletedArrayBufferA,
                                    deletedArrayBufferB, deletedDrawIndirectBuffer);
                    int restoredDrawIndirectBuffer = GlObjectDeleteGuard.deletedBufferBinding(previousDrawIndirectBuffer,
                            deletedArrayBufferA, deletedArrayBufferB, deletedDrawIndirectBuffer);
                    restore(deletedPreviousVao ? 0 : previousVao, restoredArrayBuffer, restoredElementArrayBuffer,
                            restoredDrawIndirectBuffer);
                }

                private void restore(int restoredVao, int restoredArrayBuffer, int restoredElementArrayBuffer,
                        int restoredDrawIndirectBuffer) {
                    Throwable failure = null;
                    if (restoreDrawIndirectBuffer) {
                        failure = OptimizedDrawStateGuard.restoreStep(failure,
                                () -> GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, restoredDrawIndirectBuffer));
                    }
                    failure = OptimizedDrawStateGuard.restoreStep(failure,
                            () -> GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, restoredArrayBuffer));
                    failure = OptimizedDrawStateGuard.restoreStep(failure,
                            () -> HbmGlVaoSafety.bindVertexArray(restoredVao));
                    failure = OptimizedDrawStateGuard.restoreStep(failure,
                            () -> GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, restoredElementArrayBuffer));
                    if (failure != null) {
                        HbmNtm.LOGGER.error("Legacy OBJ MDI binding restore failed", failure);
                    }
                }
            }

            private int vaoId() {
                return vaoId;
            }

            private void enableVertexAttribArraysOnBoundVao() {
                if (vaoId == 0) {
                    return;
                }
                for (int attribute = 0; attribute <= 13; attribute++) {
                    GL20.glEnableVertexAttribArray(attribute);
                }
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

            private synchronized int layoutGeneration() {
                return layoutGeneration;
            }

            private synchronized long byteCapacity() {
                return vertexCapacityBytes + instanceCapacityBytes + indirectCapacityBytes;
            }

            private synchronized boolean initializationFailed() {
                return initializationFailed;
            }

            private void resetLater() {
                Runnable reset = this::resetNow;
                if (RenderSystem.isOnRenderThread()) {
                    reset.run();
                } else {
                    RenderSystem.recordRenderCall(reset::run);
                }
            }

            private void clearPrepareScratch() {
                freePrepareScratch(instancePrepareScratch, "legacy OBJ MDI instance prepare scratch clear");
                freePrepareScratch(commandPrepareScratch, "legacy OBJ MDI command prepare scratch clear");
                instancePrepareScratch.clear();
                commandPrepareScratch.clear();
                prepareScratchCursor = 0;
            }

            private void freePrepareScratch(List<ByteBuffer> buffers, String context) {
                for (ByteBuffer buffer : buffers) {
                    freeNativeScratch(buffer, context);
                }
            }

            private synchronized void resetNow() {
                if (!RenderSystem.isOnRenderThread()) {
                    RenderSystem.recordRenderCall(this::resetNow);
                    return;
                }
                MdiBindingGuard guard = MdiBindingGuard.snapshot(true);
                int deletedVao = vaoId;
                int deletedVertexVbo = vertexVboId;
                int deletedInstanceVbo = instanceVboId;
                int deletedIndirectBuffer = indirectBufferId;
                clearSlots();
                clearPrepareScratch();
                initializationFailed = false;
                offThreadInitWarningLogged = false;
                vertexUsedBytes = 0L;
                vertexCapacityBytes = 0L;
                instanceCapacityBytes = 0L;
                indirectCapacityBytes = 0L;
                layoutGeneration++;
                Throwable failure = null;
                try {
                    if (deletedVao != 0) {
                        failure = OptimizedDrawStateGuard.restoreStep(failure,
                                () -> GL30.glDeleteVertexArrays(deletedVao));
                        vaoId = 0;
                    }
                    if (deletedVertexVbo != 0) {
                        failure = OptimizedDrawStateGuard.restoreStep(failure,
                                () -> GL15.glDeleteBuffers(deletedVertexVbo));
                        vertexVboId = 0;
                    }
                    if (deletedInstanceVbo != 0) {
                        failure = OptimizedDrawStateGuard.restoreStep(failure,
                                () -> GL15.glDeleteBuffers(deletedInstanceVbo));
                        instanceVboId = 0;
                    }
                    if (deletedIndirectBuffer != 0) {
                        failure = OptimizedDrawStateGuard.restoreStep(failure,
                                () -> GL15.glDeleteBuffers(deletedIndirectBuffer));
                        indirectBufferId = 0;
                    }
                } finally {
                    guard.restoreAfterDeleting(deletedVao, deletedVertexVbo, deletedInstanceVbo,
                            deletedIndirectBuffer);
                }
                if (failure != null) {
                    HbmNtm.LOGGER.error("Legacy OBJ MDI atlas reset cleanup failed", failure);
                }
            }
        }

        private static final class MdiSlot {
            private final InstancedMesh mesh;
            private int firstVertex;
            private int vertexCount;
            private long byteSize;
            private ByteBuffer vertexBytes;

            private MdiSlot(InstancedMesh mesh, int firstVertex, int vertexCount, long byteSize,
                    ByteBuffer vertexBytes) {
                this.mesh = mesh;
                this.firstVertex = firstVertex;
                this.vertexCount = vertexCount;
                this.byteSize = byteSize;
                this.vertexBytes = vertexBytes;
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

            private int byteSize() {
                return (int) byteSize;
            }

            private ByteBuffer vertexBytes() {
                if (vertexBytes == null) {
                    throw new IllegalStateException("Legacy OBJ MDI slot vertex bytes already freed");
                }
                vertexBytes.position(0);
                vertexBytes.limit((int) byteSize);
                return vertexBytes;
            }

            private void update(int firstVertex, int vertexCount, long byteSize) {
                this.firstVertex = firstVertex;
                this.vertexCount = vertexCount;
                this.byteSize = byteSize;
            }

            private void close() {
                ByteBuffer retained = vertexBytes;
                vertexBytes = null;
                freeNativeScratch(retained, "legacy OBJ MDI retained vertex copy clear");
            }
        }

        private static void closeLater(List<GpuMesh> meshes) {
            if (meshes.isEmpty()) {
                return;
            }
            Runnable closer = () -> {
                for (GpuMesh mesh : meshes) {
                    safeClose(mesh, "cached GPU mesh");
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
                    safeClose(mesh, "instanced mesh");
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

        private void closeInstancedAndEvictMdiLater(InstancedMesh mesh) {
            if (mesh == null) {
                return;
            }
            if (lastInstancedMesh == mesh) {
                clearLastInstancedMesh();
            }
            Runnable closer = () -> {
                mdiAtlas.evictNow(mesh);
                safeClose(mesh, "instanced mesh");
            };
            if (RenderSystem.isOnRenderThread()) {
                closer.run();
            } else {
                RenderSystem.recordRenderCall(closer::run);
            }
        }

        private static void closeIrisLater(List<IrisCompanionMesh> meshes) {
            if (meshes.isEmpty()) {
                return;
            }
            Runnable closer = () -> {
                for (IrisCompanionMesh mesh : meshes) {
                    safeClose(mesh, "Iris companion mesh");
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

        private static void safeClose(GpuMesh mesh, String context) {
            if (mesh == null) {
                return;
            }
            try {
                mesh.close();
            } catch (Throwable throwable) {
                HbmNtm.LOGGER.error("Legacy OBJ {} cleanup failed", context, throwable);
            }
        }

        private static void safeClose(InstancedMesh mesh, String context) {
            if (mesh == null) {
                return;
            }
            try {
                mesh.close();
            } catch (Throwable throwable) {
                HbmNtm.LOGGER.error("Legacy OBJ {} cleanup failed", context, throwable);
            }
        }

        private static void safeClose(IrisCompanionMesh mesh, String context) {
            if (mesh == null) {
                return;
            }
            try {
                mesh.close();
            } catch (Throwable throwable) {
                HbmNtm.LOGGER.error("Legacy OBJ {} cleanup failed", context, throwable);
            }
        }

        private static boolean isGpuMeshTransientUploadFailure(Throwable throwable) {
            Throwable current = throwable;
            while (current != null) {
                if (current instanceof GpuMeshTemporarilyUnavailableException) {
                    return true;
                }
                current = current.getCause();
            }
            return !RenderSystem.isOnRenderThread() || HbmInstancedGlCompat.currentCapabilities() == null;
        }

        private static final class GpuMeshTemporarilyUnavailableException extends RuntimeException {
            private GpuMeshTemporarilyUnavailableException(String message) {
                super(message);
            }
        }

        private static boolean isIrisCompanionTransientUploadFailure(Throwable throwable) {
            Throwable current = throwable;
            while (current != null) {
                if (current instanceof IrisCompanionTemporarilyUnavailableException) {
                    return true;
                }
                current = current.getCause();
            }
            return !RenderSystem.isOnRenderThread() || HbmInstancedGlCompat.currentCapabilities() == null;
        }

        private static final class IrisCompanionTemporarilyUnavailableException extends RuntimeException {
            private IrisCompanionTemporarilyUnavailableException(String message) {
                super(message);
            }
        }

        private static final class IrisCompanionMesh {
            private static final int CACHED_PROGRAM_SLOTS = 4;
            private static final int MIN_LIGHTMAP_SLOT_CAPACITY = 64;
            private static final int CLIENT_MAPPED_BUFFER_BARRIER_BIT = 0x00004000;
            private static final int IRIS_ATTRIB_ENTITY = 11;
            private static final int IRIS_ATTRIB_MID_TEX = 12;
            private static final int IRIS_ATTRIB_TANGENT = 13;

            private final IrisCompanionMeshKey key;
            private int vaoId;
            private int vboId;
            private final int vertexCount;
            private final long byteSize;
            private final VertexFormat format;
            private final float[] lightWeights;
            private final InstancedMeshBounds bounds;
            private final long lightSampleKey;
            private final int glDrawMode;
            private final boolean preserveVertexColors;
            private final Runnable lightmapSlotReuseRecorder;
            private final Runnable lightmapSlotUploadRecorder;
            private final Runnable lightmapStagingFallbackRecorder;
            private final Runnable shaderAttributeCacheHitRecorder;
            private final Runnable shaderAttributeCacheMissRecorder;
            private final Runnable shaderAttributeGenerationInvalidationRecorder;
            private final Runnable shaderAttributePrimedSkipRecorder;
            private final Runnable shaderAttributeVaoBindFailureRecorder;
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
            private ShortBuffer lightmapShortView;
            private ByteBuffer lightmapStagingMapped;
            private short[] lightmapSlotScratch = new short[0];
            private long lightmapStagingFence;
            private boolean lightmapPersistentMapped;
            private boolean lightmapStagingAvailable;
            private boolean lightmapStagingAttempted;
            private boolean lightmapPersistentDirty;
            private boolean lightmapStorageFailure;
            private int lightmapDirtyMinSlot = Integer.MAX_VALUE;
            private int lightmapDirtyMaxSlot = -1;
            private int lightmapAllocatedSlots;
            private boolean perVertexLightmapActive;
            private int lightmapCurrentSlot = -1;
            private final it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap lightmapSlotByKey =
                    new it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap();
            private long[] lightmapSlotKeys = new long[0];
            private int lightmapEvictCursor;
            private long cachedProgramGeneration = -1L;
            private int nextProgramSlot;
            private boolean irisExtendedAttributesPrimed;
            private boolean closed;

            private IrisCompanionMesh(IrisCompanionMeshKey key, int vaoId, int vboId, int vertexCount,
                    long byteSize, VertexFormat format, float[] lightWeights, InstancedMeshBounds bounds,
                    long lightSampleKey, int glDrawMode, boolean preserveVertexColors,
                    Runnable lightmapSlotReuseRecorder, Runnable lightmapSlotUploadRecorder,
                    Runnable lightmapStagingFallbackRecorder, Runnable shaderAttributeCacheHitRecorder,
                    Runnable shaderAttributeCacheMissRecorder,
                    Runnable shaderAttributeGenerationInvalidationRecorder,
                    Runnable shaderAttributePrimedSkipRecorder,
                    Runnable shaderAttributeVaoBindFailureRecorder) {
                this.key = key;
                this.vaoId = vaoId;
                this.vboId = vboId;
                this.vertexCount = vertexCount;
                this.byteSize = byteSize;
                this.format = format;
                this.lightWeights = lightWeights;
                this.bounds = bounds;
                this.lightSampleKey = lightSampleKey;
                this.glDrawMode = glDrawMode;
                this.preserveVertexColors = preserveVertexColors;
                this.lightmapSlotReuseRecorder = lightmapSlotReuseRecorder;
                this.lightmapSlotUploadRecorder = lightmapSlotUploadRecorder;
                this.lightmapStagingFallbackRecorder = lightmapStagingFallbackRecorder;
                this.shaderAttributeCacheHitRecorder = shaderAttributeCacheHitRecorder;
                this.shaderAttributeCacheMissRecorder = shaderAttributeCacheMissRecorder;
                this.shaderAttributeGenerationInvalidationRecorder = shaderAttributeGenerationInvalidationRecorder;
                this.shaderAttributePrimedSkipRecorder = shaderAttributePrimedSkipRecorder;
                this.shaderAttributeVaoBindFailureRecorder = shaderAttributeVaoBindFailureRecorder;
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
                        bindStandardAttribute(location, element, stride, offset);
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
                primeIrisExtendedAttributes(stride);
            }

            private void bind() {
                HbmGlVaoSafety.bindVertexArray(vaoId);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
            }

            private void bindVaoIfNeeded() {
                if (!ensureCompanionVaoBound()) {
                    HbmNtm.LOGGER.warn("Legacy OBJ Iris companion mesh failed to bind VAO {}", vaoId);
                }
            }

            private boolean ensureCompanionVaoBound() {
                if (vaoId <= 0) {
                    return false;
                }
                HbmGlVaoSafety.bindVertexArray(vaoId);
                return HbmGlVaoSafety.currentBinding() == vaoId;
            }

            private void prepareForShader(ShaderInstance shader) {
                if (shader == null || shader.getId() <= 0) {
                    return;
                }
                long generation = HbmShaderCompatibilityDetector.pipelineGeneration();
                if (generation != cachedProgramGeneration) {
                    if (cachedProgramGeneration >= 0L) {
                        shaderAttributeGenerationInvalidationRecorder.run();
                    }
                    Arrays.fill(cachedPrograms, -1);
                    cachedProgramGeneration = generation;
                    nextProgramSlot = 0;
                }
                int programId = shader.getId();
                for (int cached : cachedPrograms) {
                    if (cached == programId) {
                        shaderAttributeCacheHitRecorder.run();
                        return;
                    }
                }
                shaderAttributeCacheMissRecorder.run();
                if (!irisExtendedAttributesPrimed && !ensureCompanionVaoBound()) {
                    shaderAttributeVaoBindFailureRecorder.run();
                    HbmNtm.LOGGER.warn("Legacy OBJ Iris companion mesh failed to bind VAO {} for shader attributes",
                            vaoId);
                    return;
                }
                int stride = format.getVertexSize();
                if (irisExtendedAttributesPrimed) {
                    shaderAttributePrimedSkipRecorder.run();
                } else {
                    int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                    try {
                        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
                        bindIrisAttribute(programId, "iris_Entity", stride);
                        bindIrisAttribute(programId, "mc_midTexCoord", stride);
                        bindIrisAttribute(programId, "at_tangent", stride);
                    } finally {
                        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
                    }
                }
                cachedPrograms[nextProgramSlot] = programId;
                nextProgramSlot = (nextProgramSlot + 1) % CACHED_PROGRAM_SLOTS;
            }

            private boolean invalidateShaderAttributeCache() {
                boolean hadCachedState = cachedProgramGeneration >= 0L;
                Arrays.fill(cachedPrograms, -1);
                cachedProgramGeneration = -1L;
                nextProgramSlot = 0;
                return hadCachedState;
            }

            private void primeIrisExtendedAttributes(int stride) {
                if (!elementOffsets.containsKey("iris_Entity")) {
                    irisExtendedAttributesPrimed = false;
                    return;
                }
                int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                try {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
                    bindIrisAttributeAtLocation(IRIS_ATTRIB_ENTITY, "iris_Entity", stride);
                    bindIrisAttributeAtLocation(IRIS_ATTRIB_MID_TEX, "mc_midTexCoord", stride);
                    bindIrisAttributeAtLocation(IRIS_ATTRIB_TANGENT, "at_tangent", stride);
                    irisExtendedAttributesPrimed = true;
                } finally {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
                }
            }

            private void applyDrawAttributes(int packedLight, int packedOverlay, int red, int green, int blue,
                    int alpha, boolean allowPerVertexLightmap) {
                applyDrawAttributes(packedLight, packedOverlay, red, green, blue, alpha, allowPerVertexLightmap, -1);
            }

            private void applyDrawAttributes(int packedLight, int packedOverlay, int red, int green, int blue,
                    int alpha, boolean allowPerVertexLightmap, int preparedLightmapSlot) {
                if (colorLocation >= 0) {
                    if (preserveVertexColors) {
                        GL20.glEnableVertexAttribArray(colorLocation);
                    } else {
                        HbmIrisRenderBatch.applyConstantColor(colorLocation, red, green, blue, alpha);
                    }
                }
                if (uv1Location >= 0) {
                    HbmIrisRenderBatch.applyConstantOverlay(uv1Location, packedOverlay);
                }
                if (uv2Location >= 0) {
                    if (allowPerVertexLightmap && preparedLightmapSlot >= 0) {
                        bindLightmapSlot(preparedLightmapSlot);
                    } else {
                        GL20.glDisableVertexAttribArray(uv2Location);
                        HbmIrisRenderBatch.applyConstantLightmap(uv2Location, packedLight);
                    }
                }
            }

            private int glDrawMode() {
                return glDrawMode;
            }

            private boolean linePrimitive() {
                return glDrawMode == GL11.GL_LINES;
            }

            private int preparePerVertexLightmapSlot(Matrix4f modelView, int packedLight,
                    boolean useSlicedLight) {
                return preparePerVertexLightmapSlot(modelView, packedLight, MIN_LIGHTMAP_SLOT_CAPACITY,
                        useSlicedLight);
            }

            private int preparePerVertexLightmapSlot(Matrix4f modelView, int packedLight, int requiredSlots,
                    boolean useSlicedLight) {
                return preparePerVertexLightmapSlot(samplePerVertexLightProbe(modelView, packedLight, useSlicedLight),
                        requiredSlots);
            }

            private LegacyRenderLighting.SlicedLightProbe samplePerVertexLightProbe(Matrix4f modelView,
                    int packedLight, boolean useSlicedLight) {
                if (packedLight == LightTexture.FULL_BRIGHT) {
                    return LegacyRenderLighting.SlicedLightProbe.uniform(packedLight);
                }
                if (useSlicedLight) {
                    return bounds.sampleSlicedLightProbe(modelView, lightSampleKey, packedLight);
                }
                return LegacyRenderLighting.SlicedLightProbe.from(bounds.sampleLightProbe(modelView, lightSampleKey,
                        packedLight));
            }

            private int preparePerVertexLightmapSlot(LegacyRenderLighting.SlicedLightProbe probe) {
                return preparePerVertexLightmapSlot(probe, MIN_LIGHTMAP_SLOT_CAPACITY);
            }

            private int preparePerVertexLightmapSlot(LegacyRenderLighting.SlicedLightProbe probe,
                    int requiredSlots) {
                if (uv2Location < 0 || vertexCount <= 0 || lightWeights.length < vertexCount * 3) {
                    return -1;
                }
                if (!ensureLightmapSlotStorage(requiredSlots)) {
                    lightmapStorageFailure = true;
                    return -1;
                }
                long allocation = allocateLightmapSlot(lightmapKey(probe));
                int slot = (int) (allocation & 0xFFFF_FFFFL);
                boolean reused = (allocation >>> 32) != 0L;
                if (reused) {
                    lightmapSlotReuseRecorder.run();
                } else {
                    uploadLightmapSlot(slot, probe);
                    lightmapSlotUploadRecorder.run();
                }
                return slot;
            }

            private boolean ensureLightmapSlotStorage(int requiredSlots) {
                lightmapStorageFailure = false;
                int targetSlots = lightmapTargetSlotCapacity(requiredSlots, lightmapAllocatedSlots);
                if (lightmapAllocatedSlots >= targetSlots && lightmapVboId != 0
                        && (lightmapPersistentMapped ? lightmapMapped != null : lightmapScratch != null)) {
                    return true;
                }
                int perSlotBytes = vertexCount * 4;
                long totalBytes = (long) perSlotBytes * targetSlots;
                if (perSlotBytes <= 0 || totalBytes > Integer.MAX_VALUE) {
                    lightmapStorageFailure = true;
                    return false;
                }
                closeLightmapTargetBuffer();
                releaseLightmapScratch();
                closeLightmapStaging();
                lightmapStagingAvailable = false;
                lightmapStagingAttempted = false;
                int byteSize = (int) totalBytes;
                int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                try {
                    lightmapVboId = GL15.glGenBuffers();
                    if (lightmapVboId == 0) {
                        lightmapStorageFailure = true;
                        resetLightmapSlotStorageAfterFailure();
                        return false;
                    }
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
                                lightmapShortView = lightmapMapped.asShortBuffer();
                                lightmapPersistentMapped = true;
                            }
                        } catch (Throwable ignored) {
                            lightmapMapped = null;
                            lightmapShortView = null;
                            lightmapPersistentMapped = false;
                        }
                    }
                    if (!lightmapPersistentMapped) {
                        closeLightmapTargetBuffer();
                        lightmapVboId = GL15.glGenBuffers();
                        if (lightmapVboId == 0) {
                            lightmapStorageFailure = true;
                            resetLightmapSlotStorageAfterFailure();
                            return false;
                        }
                        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightmapVboId);
                        if (lightmapScratch == null || lightmapScratch.capacity() < byteSize) {
                            releaseLightmapScratch();
                            try {
                                lightmapScratch = MemoryUtil.memAlloc(byteSize).order(ByteOrder.nativeOrder());
                                lightmapShortView = lightmapScratch.asShortBuffer();
                            } catch (Throwable allocationFailure) {
                                lightmapStorageFailure = true;
                                if (lightmapVboId != 0) {
                                    GL15.glDeleteBuffers(lightmapVboId);
                                    lightmapVboId = 0;
                                }
                                resetLightmapSlotStorageAfterFailure();
                                return false;
                            }
                        }
                        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, totalBytes, GL15.GL_STREAM_DRAW);
                        if (canUsePersistentLightmapMapping()) {
                            tryCreateLightmapStaging(totalBytes);
                        }
                    }
                } catch (Throwable allocationFailure) {
                    lightmapStorageFailure = true;
                    resetLightmapSlotStorageAfterFailure();
                    return false;
                } finally {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,
                            previousArrayBuffer == lightmapVboId ? 0 : previousArrayBuffer);
                }
                lightmapAllocatedSlots = targetSlots;
                perVertexLightmapActive = false;
                lightmapCurrentSlot = -1;
                lightmapSlotByKey.clear();
                lightmapSlotKeys = new long[targetSlots];
                lightmapEvictCursor = 0;
                clearLightmapDirtyRange();
                lightmapPersistentDirty = false;
                return true;
            }

            private boolean consumeLightmapStorageFailureFlag() {
                boolean failure = lightmapStorageFailure;
                lightmapStorageFailure = false;
                return failure;
            }

            private void closeLightmapTargetBuffer() {
                int targetVbo = lightmapVboId;
                if (targetVbo != 0) {
                    GlObjectDeleteGuard guard = GlObjectDeleteGuard.snapshot();
                    try {
                        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, targetVbo);
                        if (lightmapPersistentMapped && lightmapMapped != null) {
                            GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
                        }
                    } catch (Throwable ignored) {
                    } finally {
                        try {
                            GL15.glDeleteBuffers(targetVbo);
                        } catch (Throwable ignored) {
                        }
                        lightmapVboId = 0;
                        guard.restoreAfterDeleting(0, targetVbo);
                    }
                }
                lightmapMapped = null;
                lightmapPersistentMapped = false;
                lightmapShortView = null;
                perVertexLightmapActive = false;
                lightmapCurrentSlot = -1;
                HbmIrisRenderBatch.invalidateLightmapAttributeCache();
            }

            private void resetLightmapSlotStorageAfterFailure() {
                closeLightmapTargetBuffer();
                releaseLightmapScratch();
                closeLightmapStaging();
                lightmapAllocatedSlots = 0;
                perVertexLightmapActive = false;
                lightmapCurrentSlot = -1;
                lightmapSlotByKey.clear();
                lightmapSlotKeys = new long[0];
                lightmapEvictCursor = 0;
                clearLightmapDirtyRange();
                lightmapPersistentDirty = false;
                lightmapStagingAvailable = false;
                lightmapStagingAttempted = false;
            }

            private static int lightmapTargetSlotCapacity(int requiredSlots, int currentSlots) {
                int targetSlots = Math.max(MIN_LIGHTMAP_SLOT_CAPACITY,
                        currentSlots > 0 ? currentSlots : MIN_LIGHTMAP_SLOT_CAPACITY);
                int required = Math.max(MIN_LIGHTMAP_SLOT_CAPACITY, requiredSlots);
                while (targetSlots < required && targetSlots < (1 << 30)) {
                    targetSlots <<= 1;
                }
                return Math.max(targetSlots, required);
            }

            private void releaseLightmapScratch() {
                ByteBuffer scratch = lightmapScratch;
                lightmapScratch = null;
                if (!lightmapPersistentMapped) {
                    lightmapShortView = null;
                }
                if (scratch != null) {
                    MemoryUtil.memFree(scratch);
                }
            }

            private static boolean canUsePersistentLightmapMapping() {
                try {
                    GLCapabilities capabilities = HbmInstancedGlCompat.currentCapabilities();
                    return capabilities != null && (capabilities.OpenGL44 || capabilities.GL_ARB_buffer_storage);
                } catch (Throwable ignored) {
                    return false;
                }
            }

            private void tryCreateLightmapStaging(long totalBytes) {
                lightmapStagingAttempted = true;
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

            private long allocateLightmapSlot(long lightmapKey) {
                if (lightmapKey == 0L) {
                    lightmapKey = 1L;
                }
                int cachedSlot = lightmapSlotByKey.getOrDefault(lightmapKey, -1);
                int capacity = Math.max(MIN_LIGHTMAP_SLOT_CAPACITY, lightmapAllocatedSlots);
                if (cachedSlot >= 0 && cachedSlot < capacity) {
                    return (1L << 32) | (cachedSlot & 0xFFFF_FFFFL);
                }
                int slot;
                if (lightmapSlotByKey.size() < capacity) {
                    slot = lightmapSlotByKey.size();
                } else {
                    slot = lightmapEvictCursor++;
                    if (lightmapEvictCursor >= capacity) {
                        lightmapEvictCursor = 0;
                    }
                    long evictedKey = slot < lightmapSlotKeys.length ? lightmapSlotKeys[slot] : 0L;
                    if (evictedKey != 0L) {
                        lightmapSlotByKey.remove(evictedKey);
                    }
                }
                if (slot >= lightmapSlotKeys.length) {
                    lightmapSlotKeys = Arrays.copyOf(lightmapSlotKeys, Math.max(capacity, slot + 1));
                }
                lightmapSlotKeys[slot] = lightmapKey;
                lightmapSlotByKey.put(lightmapKey, slot);
                return slot & 0xFFFF_FFFFL;
            }

            private static long lightmapKey(LegacyRenderLighting.SlicedLightProbe probe) {
                long key = 1469598103934665603L;
                for (int i = 0; i < 16; i++) {
                    int packedLight = probe.probe(i);
                    key ^= packedLight & 0xFFFFFFFFL;
                    key *= 1099511628211L;
                }
                return key == 0L ? 1L : key;
            }

            private void uploadLightmapSlot(int slot, LegacyRenderLighting.SlicedLightProbe probe) {
                ShortBuffer slotShorts = lightmapShortView;
                if (slotShorts == null) {
                    return;
                }
                int requiredShorts = vertexCount * 2;
                if (requiredShorts <= 0) {
                    return;
                }
                int shortOffset = slot * requiredShorts;
                if (shortOffset < 0 || shortOffset + requiredShorts > slotShorts.capacity()) {
                    return;
                }
                if (lightmapSlotScratch.length < requiredShorts) {
                    lightmapSlotScratch = new short[requiredShorts];
                }
                for (int vertex = 0; vertex < vertexCount; vertex++) {
                    int offset = vertex * 3;
                    float x = lightWeights[offset];
                    float y = lightWeights[offset + 1];
                    float z = lightWeights[offset + 2];
                    int target = vertex * 2;
                    lightmapSlotScratch[target] = (short) interpolateBlockLight(probe, x, y, z);
                    lightmapSlotScratch[target + 1] = (short) interpolateSkyLight(probe, x, y, z);
                }
                ShortBuffer slotView = slotShorts.duplicate();
                slotView.position(shortOffset);
                slotView.put(lightmapSlotScratch, 0, requiredShorts);
                if (lightmapPersistentMapped) {
                    lightmapPersistentDirty = true;
                } else {
                    markLightmapSlotDirty(slot);
                }
            }

            private void markLightmapSlotDirty(int slot) {
                lightmapDirtyMinSlot = Math.min(lightmapDirtyMinSlot, slot);
                lightmapDirtyMaxSlot = Math.max(lightmapDirtyMaxSlot, slot);
            }

            private void finishPreparedLightmapWrites() {
                if (lightmapPersistentMapped) {
                    if (lightmapPersistentDirty) {
                        flushPersistentLightmapWrites();
                        lightmapPersistentDirty = false;
                    }
                    return;
                }
                if (lightmapDirtyMaxSlot < lightmapDirtyMinSlot || lightmapScratch == null || lightmapVboId == 0) {
                    return;
                }
                int perSlotBytes = vertexCount * 4;
                int firstSlot = lightmapDirtyMinSlot;
                int slotCount = lightmapDirtyMaxSlot - lightmapDirtyMinSlot + 1;
                int byteOffset = firstSlot * perSlotBytes;
                int byteSize = slotCount * perSlotBytes;
                if (byteOffset < 0 || byteOffset + byteSize > lightmapScratch.capacity()) {
                    clearLightmapDirtyRange();
                    lightmapScratch.clear();
                    return;
                }
                ByteBuffer rangeBytes = lightmapScratch.duplicate().order(ByteOrder.nativeOrder());
                rangeBytes.position(byteOffset);
                rangeBytes.limit(byteOffset + byteSize);
                rangeBytes = rangeBytes.slice().order(ByteOrder.nativeOrder());
                int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                int previousCopyReadBuffer = GL11.glGetInteger(GL31.GL_COPY_READ_BUFFER);
                int previousCopyWriteBuffer = GL11.glGetInteger(GL31.GL_COPY_WRITE_BUFFER);
                try {
                    if (!uploadLightmapRangeViaStaging(byteOffset, byteSize, rangeBytes)) {
                        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightmapVboId);
                        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, byteOffset, rangeBytes);
                    }
                } finally {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
                    GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, previousCopyReadBuffer);
                    GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, previousCopyWriteBuffer);
                    lightmapScratch.clear();
                    clearLightmapDirtyRange();
                }
            }

            private void clearLightmapDirtyRange() {
                lightmapDirtyMinSlot = Integer.MAX_VALUE;
                lightmapDirtyMaxSlot = -1;
            }

            private boolean uploadLightmapRangeViaStaging(int byteOffset, int byteSize, ByteBuffer rangeBytes) {
                if (!lightmapStagingAvailable || lightmapStagingVboId == 0 || lightmapStagingMapped == null) {
                    if (lightmapStagingAttempted) {
                        lightmapStagingFallbackRecorder.run();
                    }
                    return false;
                }
                if (lightmapStagingFence != 0L) {
                    int wait = GL32.glClientWaitSync(lightmapStagingFence, 0, 0L);
                    if (wait == GL32.GL_TIMEOUT_EXPIRED) {
                        lightmapStagingFallbackRecorder.run();
                        return false;
                    }
                    GL32.glDeleteSync(lightmapStagingFence);
                    lightmapStagingFence = 0L;
                }
                try {
                    ByteBuffer source = rangeBytes.duplicate().order(ByteOrder.nativeOrder());
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
                    lightmapStagingFallbackRecorder.run();
                    return false;
                }
            }

            private static void flushPersistentLightmapWrites() {
                try {
                    GLCapabilities capabilities = HbmInstancedGlCompat.currentCapabilities();
                    if (capabilities != null && capabilities.OpenGL42) {
                        GL42.glMemoryBarrier(CLIENT_MAPPED_BUFFER_BARRIER_BIT);
                    }
                } catch (Throwable ignored) {
                }
            }

            private boolean activatePerVertexLightmap() {
                if (uv2Location < 0 || lightmapVboId == 0 || lightmapAllocatedSlots <= 0) {
                    return false;
                }
                if (!ensureCompanionVaoBound()) {
                    HbmNtm.LOGGER.warn("Legacy OBJ Iris companion mesh failed to bind VAO {} for lightmap slot",
                            vaoId);
                    return false;
                }
                if (perVertexLightmapActive) {
                    return true;
                }
                int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                try {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightmapVboId);
                    GL30.glVertexAttribIPointer(uv2Location, 2, GL11.GL_UNSIGNED_SHORT, 4, 0L);
                    GL20.glEnableVertexAttribArray(uv2Location);
                } finally {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
                }
                perVertexLightmapActive = true;
                lightmapCurrentSlot = 0;
                HbmIrisRenderBatch.invalidateLightmapAttributeCache();
                return true;
            }

            private void bindLightmapSlot(int slot) {
                if (uv2Location < 0 || slot < 0 || slot >= lightmapAllocatedSlots || lightmapVboId == 0) {
                    return;
                }
                if (!activatePerVertexLightmap()) {
                    return;
                }
                if (slot == lightmapCurrentSlot) {
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
                lightmapCurrentSlot = slot;
                HbmIrisRenderBatch.invalidateLightmapAttributeCache();
            }

            private void restoreConstantLightmap() {
                if (uv2Location < 0 || !perVertexLightmapActive) {
                    return;
                }
                int previousVao = HbmGlVaoSafety.currentBinding();
                try {
                    if (ensureCompanionVaoBound()) {
                        GL20.glDisableVertexAttribArray(uv2Location);
                    } else {
                        HbmNtm.LOGGER.warn(
                                "Legacy OBJ Iris companion mesh failed to bind VAO {} for lightmap restore",
                                vaoId);
                    }
                } finally {
                    HbmGlVaoSafety.bindVertexArray(previousVao);
                    perVertexLightmapActive = false;
                    lightmapCurrentSlot = -1;
                    HbmIrisRenderBatch.invalidateLightmapAttributeCache();
                }
            }

            private static int interpolateBlockLight(LegacyRenderLighting.SlicedLightProbe probe, float x, float y,
                    float z) {
                return clampLight(Math.round(interpolateSlicedLight(probe, x, y, z, true) * 16.0F));
            }

            private static int interpolateSkyLight(LegacyRenderLighting.SlicedLightProbe probe, float x, float y,
                    float z) {
                return clampLight(Math.round(interpolateSlicedLight(probe, x, y, z, false) * 16.0F));
            }

            private static float interpolateSlicedLight(LegacyRenderLighting.SlicedLightProbe probe,
                    float x, float y, float z, boolean block) {
                float scaledY = clampUnit(y) * 3.0F;
                int slice0 = Math.max(0, Math.min(3, (int) Math.floor(scaledY)));
                int slice1 = Math.min(slice0 + 1, 3);
                float sliceWeight = clampUnit(scaledY - slice0);
                float v0 = interpolateSlicedLayer(probe, slice0, x, z, block);
                float v1 = interpolateSlicedLayer(probe, slice1, x, z, block);
                return v0 + (v1 - v0) * sliceWeight;
            }

            private static float interpolateSlicedLayer(LegacyRenderLighting.SlicedLightProbe probe,
                    int slice, float x, float z, boolean block) {
                int base = slice * 4;
                float c00 = lightComponent(probe.probe(base), block);
                float c10 = lightComponent(probe.probe(base + 1), block);
                float c01 = lightComponent(probe.probe(base + 2), block);
                float c11 = lightComponent(probe.probe(base + 3), block);
                float z0 = c00 + (c01 - c00) * clampUnit(z);
                float z1 = c10 + (c11 - c10) * clampUnit(z);
                return z0 + (z1 - z0) * clampUnit(x);
            }

            private static int lightComponent(int packedLight, boolean block) {
                return block ? LightTexture.block(packedLight) : LightTexture.sky(packedLight);
            }

            private static int clampLight(int value) {
                return Math.max(0, Math.min(240, value));
            }

            private static float clampUnit(float value) {
                return Math.max(0.0F, Math.min(1.0F, value));
            }

            private void bindIrisAttribute(int programId, String name, int stride) {
                Integer offset = elementOffsets.get(name);
                VertexFormatElement element = elementByName.get(name);
                if (offset == null || element == null) {
                    return;
                }
                if (vaoId <= 0 || vboId <= 0) {
                    shaderAttributeVaoBindFailureRecorder.run();
                    return;
                }
                if (!ensureCompanionVaoBound()) {
                    shaderAttributeVaoBindFailureRecorder.run();
                    HbmNtm.LOGGER.warn(
                            "Legacy OBJ Iris companion mesh failed to bind VAO {} for Iris attribute {}",
                            vaoId, name);
                    return;
                }
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
                int location = GL20.glGetAttribLocation(programId, name);
                if (location < 0) {
                    return;
                }
                bindIrisAttributeAtLocation(location, name, stride);
            }

            private void bindIrisAttributeAtLocation(int location, String name, int stride) {
                Integer offset = elementOffsets.get(name);
                VertexFormatElement element = elementByName.get(name);
                if (offset == null || element == null) {
                    return;
                }
                int type = element.getType().getGlType();
                int count = element.getCount();
                if (isIrisIntegerAttribute(name, element, type)) {
                    GL30.glVertexAttribIPointer(location, count, type, stride, offset);
                } else {
                    GL20.glVertexAttribPointer(location, count, type, shouldNormalizeIrisAttribute(name, element),
                            stride, offset);
                }
                GL20.glEnableVertexAttribArray(location);
            }

            private static void bindStandardAttribute(int location, VertexFormatElement element, int stride,
                    int offset) {
                int type = element.getType().getGlType();
                int count = element.getCount();
                if (isStandardIntegerAttribute(element)) {
                    GL30.glVertexAttribIPointer(location, count, type, stride, offset);
                } else {
                    GL20.glVertexAttribPointer(location, count, type, shouldNormalize(element), stride, offset);
                }
                GL20.glEnableVertexAttribArray(location);
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

            private static boolean shouldNormalizeIrisAttribute(String name, VertexFormatElement element) {
                return "at_tangent".equals(name) || element.getUsage() == VertexFormatElement.Usage.NORMAL;
            }

            private static boolean isStandardIntegerAttribute(VertexFormatElement element) {
                return element.getUsage() == VertexFormatElement.Usage.UV
                        && (element.getIndex() == 1 || element.getIndex() == 2);
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

            private static boolean isIrisIntegerAttribute(String name, VertexFormatElement element, int type) {
                if ("at_tangent".equals(name)) {
                    return false;
                }
                return isIntegerAttribute(element, type);
            }

            private int vertexCount() {
                return vertexCount;
            }

            private long byteSize() {
                return byteSize;
            }

            private void close() {
                if (closed) {
                    return;
                }
                closed = true;
                GlObjectDeleteGuard guard = GlObjectDeleteGuard.snapshot();
                int deletedVao = vaoId;
                int deletedVbo = vboId;
                int deletedLightmapVbo = lightmapVboId;
                int deletedStagingVbo = lightmapStagingVboId;
                ByteBuffer scratchToFree = lightmapScratch;
                vaoId = 0;
                vboId = 0;
                lightmapScratch = null;
                lightmapShortView = null;
                Throwable failure = null;
                try {
                    if (lightmapVboId != 0) {
                        int targetLightmapVbo = lightmapVboId;
                        if (lightmapPersistentMapped) {
                            int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                            try {
                                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, targetLightmapVbo);
                                failure = GlObjectDeleteGuard.restoreStep(failure,
                                        () -> GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER));
                            } finally {
                                int restoreArrayBuffer = previousArrayBuffer;
                                failure = GlObjectDeleteGuard.restoreStep(failure,
                                        () -> GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, restoreArrayBuffer));
                            }
                        }
                        failure = GlObjectDeleteGuard.restoreStep(failure,
                                () -> GL15.glDeleteBuffers(targetLightmapVbo));
                        lightmapVboId = 0;
                    }
                    failure = closeLightmapStaging(failure);
                    lightmapMapped = null;
                    lightmapPersistentMapped = false;
                    lightmapStagingAvailable = false;
                    lightmapAllocatedSlots = 0;
                    perVertexLightmapActive = false;
                    lightmapCurrentSlot = -1;
                    HbmIrisRenderBatch.invalidateLightmapAttributeCache();
                    lightmapSlotByKey.clear();
                    lightmapSlotKeys = new long[0];
                    lightmapEvictCursor = 0;
                    clearLightmapDirtyRange();
                    lightmapPersistentDirty = false;
                    if (deletedVbo != 0) {
                        failure = GlObjectDeleteGuard.restoreStep(failure, () -> GL15.glDeleteBuffers(deletedVbo));
                    }
                    if (deletedVao != 0) {
                        failure = GlObjectDeleteGuard.restoreStep(failure,
                                () -> GL30.glDeleteVertexArrays(deletedVao));
                    }
                } finally {
                    guard.restoreAfterDeleting(deletedVao, deletedVbo, deletedLightmapVbo, deletedStagingVbo);
                    if (scratchToFree != null) {
                        MemoryUtil.memFree(scratchToFree);
                    }
                }
                if (failure != null) {
                    throw new IllegalStateException("Legacy OBJ Iris companion mesh cleanup failed", failure);
                }
            }

            @Override
            public String toString() {
                return key.toString();
            }

            private void closeLightmapStaging() {
                Throwable failure = closeLightmapStaging(null);
                if (failure != null) {
                    throw new IllegalStateException("Legacy OBJ Iris companion lightmap staging cleanup failed",
                            failure);
                }
            }

            private Throwable closeLightmapStaging(Throwable failure) {
                if (lightmapStagingFence != 0L) {
                    long fence = lightmapStagingFence;
                    failure = GlObjectDeleteGuard.restoreStep(failure, () -> GL32.glDeleteSync(fence));
                    lightmapStagingFence = 0L;
                }
                if (lightmapStagingVboId != 0) {
                    GlObjectDeleteGuard guard = GlObjectDeleteGuard.snapshot();
                    int deletedStagingVbo = lightmapStagingVboId;
                    int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                    try {
                        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lightmapStagingVboId);
                        if (lightmapStagingMapped != null) {
                            failure = GlObjectDeleteGuard.restoreStep(failure,
                                    () -> GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER));
                        }
                    } finally {
                        int restoreArrayBuffer = previousArrayBuffer;
                        failure = GlObjectDeleteGuard.restoreStep(failure,
                                () -> GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, restoreArrayBuffer));
                    }
                    try {
                        int targetStagingVbo = lightmapStagingVboId;
                        failure = GlObjectDeleteGuard.restoreStep(failure,
                                () -> GL15.glDeleteBuffers(targetStagingVbo));
                        lightmapStagingVboId = 0;
                    } finally {
                        guard.restoreAfterDeleting(0, deletedStagingVbo);
                    }
                }
                lightmapStagingMapped = null;
                lightmapStagingAvailable = false;
                return failure;
            }

        }

        private static int instancedTailPriority(LegacyTexturedRenderMode renderMode) {
            if (isInstancedNormalAlphaMode(renderMode)) {
                return 3;
            }
            if (isInstancedAdditiveMode(renderMode)) {
                return 2;
            }
            if (isGlintRenderMode(renderMode)) {
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
        private long texturedBatches;
        private long texturedVertices;
        private long spriteBatches;
        private long spriteVertices;
        private long untexturedBatches;
        private long untexturedVertices;
        private long texturedDrawCalls;
        private long spriteDrawCalls;
        private long untexturedDrawCalls;
        private long textureSwitches;
        private long renderModeSwitches;
        private long texturedClippedFallbackBatches;
        private long texturedClippedFallbackVertices;
        private long untexturedClippedFallbackBatches;
        private long untexturedClippedFallbackVertices;
        private final AtomicLong frameGeneration = new AtomicLong();
        private long currentFrameTexturedBatches;
        private long currentFrameTexturedVertices;
        private long currentFrameSpriteBatches;
        private long currentFrameSpriteVertices;
        private long currentFrameUntexturedBatches;
        private long currentFrameUntexturedVertices;
        private long currentFrameCpuFallbackBatches;
        private long currentFrameCpuFallbackVertices;
        private long currentFrameBackendFlushes;
        private long currentFrameEstimatedDrawCalls;
        private long currentFrameTextureSwitches;
        private long lastFrameTexturedBatches;
        private long lastFrameTexturedVertices;
        private long lastFrameSpriteBatches;
        private long lastFrameSpriteVertices;
        private long lastFrameUntexturedBatches;
        private long lastFrameUntexturedVertices;
        private long lastFrameCpuFallbackBatches;
        private long lastFrameCpuFallbackVertices;
        private long lastFrameBackendFlushes;
        private long lastFrameEstimatedDrawCalls;
        private long lastFrameTextureSwitches;
        private volatile ResourceLocation lastTextureLocation;
        private volatile TextureAtlasSprite lastSprite;
        private volatile LegacyTexturedRenderMode lastRenderMode;
        private volatile RenderBackendFallbackReason lastFallbackReason = RenderBackendFallbackReason.NONE;

        @Override
        public boolean prepareTexturedCpuSequence(TexturedPreparedSequence sequence, PreparedBatch batch,
                ResourceLocation textureLocation, MultiBufferSource buffer, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing,
                LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
                RenderBackendFallbackReason fallbackReason) {
            sequence.prepare(null, this, batch, textureLocation, buffer, packedLight, packedOverlay, red, green,
                    blue, alpha, legacyShadow, smoothing, renderMode, uvTransform, fallbackReason);
            return true;
        }

        @Override
        public void renderTextured(PreparedBatch batch, ResourceLocation textureLocation, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                boolean legacyShadow, boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
            if (batch.empty()) {
                return;
            }
            texturedBatches++;
            texturedVertices += batch.vertexCount();
            currentFrameTexturedBatches++;
            currentFrameTexturedVertices += batch.vertexCount();
            VertexConsumer quadConsumer = null;
            VertexConsumer triangleConsumer = null;
            PoseStack.Pose pose = poseStack.last();
            Matrix4f position = pose.pose();
            Matrix3f normal = pose.normal();
            LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, position).withAlpha(alpha);
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

        private void recordTexturedBatch(PreparedBatch batch, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode) {
            texturedBatches++;
            texturedVertices += batch.vertexCount();
            currentFrameTexturedBatches++;
            currentFrameTexturedVertices += batch.vertexCount();
            recordTextureAndMode(textureLocation, renderMode);
        }

        @Override
        public void renderSprite(PreparedBatch batch, TextureAtlasSprite sprite, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
                boolean legacyShadow, boolean partBrightness, LegacyTexturedRenderMode renderMode,
                UvTransform uvTransform) {
            if (batch.empty()) {
                return;
            }
            spriteBatches++;
            spriteVertices += batch.vertexCount();
            currentFrameSpriteBatches++;
            currentFrameSpriteVertices += batch.vertexCount();
            VertexConsumer quadConsumer = null;
            VertexConsumer triangleConsumer = null;
            PoseStack.Pose pose = poseStack.last();
            Matrix4f position = pose.pose();
            Matrix3f normal = pose.normal();
            LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, position).withAlpha(alpha);
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
            untexturedBatches++;
            untexturedVertices += batch.vertexCount();
            currentFrameUntexturedBatches++;
            currentFrameUntexturedVertices += batch.vertexCount();
            PoseStack.Pose pose = poseStack.last();
            Matrix4f position = pose.pose();
            LegacyTexturedRenderMode resolvedRenderMode = renderModeForPose(renderMode, position);
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
            currentFrameBackendFlushes++;
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
                    texturedClippedFallbackBatches++;
                    texturedClippedFallbackVertices += vertices;
                }
                case UNTEXTURED_CLIPPED -> {
                    untexturedClippedFallbackBatches++;
                    untexturedClippedFallbackVertices += vertices;
                }
                case NONE -> {
                }
            }
            currentFrameCpuFallbackBatches++;
            currentFrameCpuFallbackVertices += vertices;
        }

        private void recordTextureAndMode(ResourceLocation textureLocation, LegacyTexturedRenderMode renderMode) {
            ResourceLocation previousTexture = lastTextureLocation;
            if (previousTexture == null || !previousTexture.equals(textureLocation)) {
                textureSwitches++;
                currentFrameTextureSwitches++;
                lastTextureLocation = textureLocation;
            }
            recordRenderMode(renderMode);
        }

        private void recordSpriteAndMode(TextureAtlasSprite sprite, LegacyTexturedRenderMode renderMode) {
            TextureAtlasSprite previousSprite = lastSprite;
            if (previousSprite != sprite) {
                textureSwitches++;
                currentFrameTextureSwitches++;
                lastSprite = sprite;
            }
            recordRenderMode(renderMode);
        }

        private void recordRenderMode(LegacyTexturedRenderMode renderMode) {
            LegacyTexturedRenderMode previousRenderMode = lastRenderMode;
            if (previousRenderMode != renderMode) {
                renderModeSwitches++;
                lastRenderMode = renderMode;
            }
        }

        private void recordTexturedDrawCall() {
            texturedDrawCalls++;
            currentFrameEstimatedDrawCalls++;
        }

        private void recordSpriteDrawCall() {
            spriteDrawCalls++;
            currentFrameEstimatedDrawCalls++;
        }

        private void recordUntexturedDrawCall() {
            untexturedDrawCalls++;
            currentFrameEstimatedDrawCalls++;
        }

        @Override
        public void endFrame() {
            lastFrameTexturedBatches = currentFrameTexturedBatches;
            currentFrameTexturedBatches = 0L;
            lastFrameTexturedVertices = currentFrameTexturedVertices;
            currentFrameTexturedVertices = 0L;
            lastFrameSpriteBatches = currentFrameSpriteBatches;
            currentFrameSpriteBatches = 0L;
            lastFrameSpriteVertices = currentFrameSpriteVertices;
            currentFrameSpriteVertices = 0L;
            lastFrameUntexturedBatches = currentFrameUntexturedBatches;
            currentFrameUntexturedBatches = 0L;
            lastFrameUntexturedVertices = currentFrameUntexturedVertices;
            currentFrameUntexturedVertices = 0L;
            lastFrameCpuFallbackBatches = currentFrameCpuFallbackBatches;
            currentFrameCpuFallbackBatches = 0L;
            lastFrameCpuFallbackVertices = currentFrameCpuFallbackVertices;
            currentFrameCpuFallbackVertices = 0L;
            lastFrameBackendFlushes = currentFrameBackendFlushes;
            currentFrameBackendFlushes = 0L;
            lastFrameEstimatedDrawCalls = currentFrameEstimatedDrawCalls;
            currentFrameEstimatedDrawCalls = 0L;
            lastFrameTextureSwitches = currentFrameTextureSwitches;
            currentFrameTextureSwitches = 0L;
            frameGeneration.incrementAndGet();
        }

        @Override
        public RenderBackendSnapshot snapshot() {
            long texturedClippedBatches = texturedClippedFallbackBatches;
            long texturedClippedVertices = texturedClippedFallbackVertices;
            long untexturedClippedBatches = untexturedClippedFallbackBatches;
            long untexturedClippedVertices = untexturedClippedFallbackVertices;
            return new RenderBackendSnapshot(
                    "cpu_prepared_vertex_consumer",
                    false,
                    true,
                    CAPABILITIES,
                    modelCacheSnapshot(),
                    CACHE_METRICS.groupPreparedBuilds,
                    CACHE_METRICS.groupPreparedVertices,
                    CACHE_METRICS.allPreparedBatchBuilds,
                    CACHE_METRICS.selectionCacheHits,
                    CACHE_METRICS.selectionCacheMisses,
                    CACHE_METRICS.selectionCacheClears,
                    CACHE_METRICS.selectionCacheEmptyBuilds,
                    CACHE_METRICS.selectionCachePreparedBatchBuilds,
                    CACHE_METRICS.selectionHandleRefreshes,
                    CACHE_METRICS.selectionHandleEmptyBuilds,
                    CACHE_METRICS.selectionHandlePreparedBatchBuilds,
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
                    texturedBatches,
                    texturedVertices,
                    spriteBatches,
                    spriteVertices,
                    untexturedBatches,
                    untexturedVertices,
                    texturedDrawCalls + spriteDrawCalls + untexturedDrawCalls,
                    texturedDrawCalls,
                    spriteDrawCalls,
                    untexturedDrawCalls,
                    textureSwitches,
                    renderModeSwitches,
                    texturedClippedBatches + untexturedClippedBatches,
                    texturedClippedVertices + untexturedClippedVertices,
                    texturedClippedBatches,
                    texturedClippedVertices,
                    untexturedClippedBatches,
                    untexturedClippedVertices,
                    lastFallbackReason,
                    NO_FALLBACK_DETAIL,
                    frameGeneration.get(),
                    currentFrameTexturedBatches,
                    currentFrameTexturedVertices,
                    currentFrameSpriteBatches,
                    currentFrameSpriteVertices,
                    currentFrameUntexturedBatches,
                    currentFrameUntexturedVertices,
                    currentFrameCpuFallbackBatches,
                    currentFrameCpuFallbackVertices,
                    currentFrameBackendFlushes,
                    currentFrameEstimatedDrawCalls,
                    currentFrameTextureSwitches,
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
                    lastFrameTexturedBatches,
                    lastFrameTexturedVertices,
                    lastFrameSpriteBatches,
                    lastFrameSpriteVertices,
                    lastFrameUntexturedBatches,
                    lastFrameUntexturedVertices,
                    lastFrameCpuFallbackBatches,
                    lastFrameCpuFallbackVertices,
                    lastFrameBackendFlushes,
                    lastFrameEstimatedDrawCalls,
                    lastFrameTextureSwitches);
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

    private record PreparedBatch(String stableKey, PreparedGeometryKey geometryKey,
                                 List<PreparedVertex> quadVertices,
                                 List<PreparedVertex> triangleVertices) {
        private static final PreparedBatch EMPTY = of("empty", List.of(), List.of());

        private static PreparedBatch of(String stableKey, List<PreparedVertex> quads,
                List<PreparedVertex> triangles) {
            List<PreparedVertex> copiedQuads = List.copyOf(quads);
            List<PreparedVertex> copiedTriangles = List.copyOf(triangles);
            return new PreparedBatch(stableKey, new PreparedGeometryKey(copiedQuads, copiedTriangles),
                    copiedQuads, copiedTriangles);
        }

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
            List<PreparedVertex> copiedQuads = List.copyOf(quads);
            List<PreparedVertex> copiedTriangles = List.copyOf(triangles);
            return of(stableKey, copiedQuads, copiedTriangles);
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
            List<PreparedVertex> copiedQuads = List.copyOf(quads);
            List<PreparedVertex> copiedTriangles = List.copyOf(triangles);
            return of(stableKey, copiedQuads, copiedTriangles);
        }

        private boolean empty() {
            return quadVertices.isEmpty() && triangleVertices.isEmpty();
        }

        private int vertexCount() {
            return quadVertices.size() + triangleVertices.size();
        }

        private int geometryHash() {
            return geometryKey.hashCode();
        }
    }

    /**
     * Exact immutable identity for the emitted OBJ vertex stream.
     *
     * <p>The cached 32-bit hash is only a map-bucket accelerator. Equality still compares every emitted corner in
     * order, including position, UV, smooth normal, face normal and average UV using the exact float bit pattern.
     * This prevents two clipped/selected batches with the same stable route and a Java hash collision from reusing
     * one another's VBO. The key retains the immutable prepared lists until the bounded GPU cache evicts the entry;
     * render-backend clear/reload then releases the key together with its mesh.</p>
     */
    private static final class PreparedGeometryKey {
        private final List<PreparedVertex> quads;
        private final List<PreparedVertex> triangles;
        private final long[] transientPayload;
        private final int hashCode;
        private final long lightSampleHash;

        private PreparedGeometryKey(List<PreparedVertex> quads, List<PreparedVertex> triangles) {
            this.quads = quads;
            this.triangles = triangles;
            this.transientPayload = null;
            this.hashCode = geometryHash(quads, triangles);
            this.lightSampleHash = geometryLightSampleHash(quads, triangles);
        }

        private PreparedGeometryKey(long[] transientPayload) {
            this.quads = List.of();
            this.triangles = List.of();
            this.transientPayload = transientPayload.clone();
            this.hashCode = Arrays.hashCode(this.transientPayload);
            this.lightSampleHash = transientLightSampleHash(this.transientPayload);
        }

        private static PreparedGeometryKey fromLines(List<UntexturedLineTransient> lines) {
            long[] payload = new long[1 + lines.size() * 11];
            payload[0] = 0x4C494E45534CL;
            int index = 1;
            for (UntexturedLineTransient line : lines) {
                payload[index++] = Double.doubleToRawLongBits(line.x0());
                payload[index++] = Double.doubleToRawLongBits(line.y0());
                payload[index++] = Double.doubleToRawLongBits(line.z0());
                payload[index++] = Double.doubleToRawLongBits(line.x1());
                payload[index++] = Double.doubleToRawLongBits(line.y1());
                payload[index++] = Double.doubleToRawLongBits(line.z1());
                payload[index++] = line.color() & 0xFFFFFFFFL;
                payload[index++] = line.alpha() & 0xFFFFFFFFL;
                payload[index++] = Float.floatToRawIntBits(line.normalX()) & 0xFFFFFFFFL;
                payload[index++] = Float.floatToRawIntBits(line.normalY()) & 0xFFFFFFFFL;
                payload[index++] = Float.floatToRawIntBits(line.normalZ()) & 0xFFFFFFFFL;
            }
            return new PreparedGeometryKey(payload);
        }

        private static PreparedGeometryKey fromVertexColors(List<UntexturedVertexColor> vertices) {
            long[] payload = new long[1 + vertices.size() * 7];
            payload[0] = 0x56455254455843L;
            int index = 1;
            for (UntexturedVertexColor vertex : vertices) {
                payload[index++] = Double.doubleToRawLongBits(vertex.x());
                payload[index++] = Double.doubleToRawLongBits(vertex.y());
                payload[index++] = Double.doubleToRawLongBits(vertex.z());
                payload[index++] = vertex.red() & 0xFFFFFFFFL;
                payload[index++] = vertex.green() & 0xFFFFFFFFL;
                payload[index++] = vertex.blue() & 0xFFFFFFFFL;
                payload[index++] = vertex.alpha() & 0xFFFFFFFFL;
            }
            return new PreparedGeometryKey(payload);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof PreparedGeometryKey other) || hashCode != other.hashCode) {
                return false;
            }
            if (transientPayload != null || other.transientPayload != null) {
                return transientPayload != null && other.transientPayload != null
                        && Arrays.equals(transientPayload, other.transientPayload);
            }
            return sameVertexListBits(quads, other.quads)
                    && sameVertexListBits(triangles, other.triangles);
        }

        private long lightSampleHash() {
            return lightSampleHash;
        }

        private static boolean sameVertexListBits(List<PreparedVertex> left, List<PreparedVertex> right) {
            if (left.size() != right.size()) {
                return false;
            }
            for (int index = 0; index < left.size(); index++) {
                if (!sameVertexBits(left.get(index), right.get(index))) {
                    return false;
                }
            }
            return true;
        }

        private static boolean sameVertexBits(PreparedVertex left, PreparedVertex right) {
            return sameVectorBits(left.position(), right.position())
                    && sameUvBits(left.uv(), right.uv())
                    && sameVectorBits(left.smoothNormal(), right.smoothNormal())
                    && sameVectorBits(left.faceNormal(), right.faceNormal())
                    && sameUvBits(left.averageUv(), right.averageUv());
        }

        private static boolean sameVectorBits(Vector3f left, Vector3f right) {
            return sameFloatBits(left.x(), right.x())
                    && sameFloatBits(left.y(), right.y())
                    && sameFloatBits(left.z(), right.z());
        }

        private static boolean sameUvBits(UV left, UV right) {
            return sameFloatBits(left.u(), right.u()) && sameFloatBits(left.v(), right.v());
        }

        private static boolean sameFloatBits(float left, float right) {
            return Float.floatToRawIntBits(left) == Float.floatToRawIntBits(right);
        }

        private static int geometryHash(List<PreparedVertex> quads, List<PreparedVertex> triangles) {
            int hash = 1;
            hash = mixVertexListHash(hash, quads);
            hash = mixVertexListHash(hash, triangles);
            return hash;
        }

        private static int mixVertexListHash(int hash, List<PreparedVertex> vertices) {
            hash = 31 * hash + vertices.size();
            for (PreparedVertex vertex : vertices) {
                hash = mixVectorHash(hash, vertex.position());
                hash = mixUvHash(hash, vertex.uv());
                hash = mixVectorHash(hash, vertex.smoothNormal());
                hash = mixVectorHash(hash, vertex.faceNormal());
                hash = mixUvHash(hash, vertex.averageUv());
            }
            return hash;
        }

        private static int mixVectorHash(int hash, Vector3f vector) {
            hash = 31 * hash + Float.floatToRawIntBits(vector.x());
            hash = 31 * hash + Float.floatToRawIntBits(vector.y());
            return 31 * hash + Float.floatToRawIntBits(vector.z());
        }

        private static int mixUvHash(int hash, UV uv) {
            hash = 31 * hash + Float.floatToRawIntBits(uv.u());
            return 31 * hash + Float.floatToRawIntBits(uv.v());
        }

        private static long geometryLightSampleHash(List<PreparedVertex> quads,
                List<PreparedVertex> triangles) {
            long hash = 0x6A09E667F3BCC909L;
            hash = mixGeometryLong(hash, 0x5155414453L);
            hash = mixGeometryVertices(hash, quads);
            hash = mixGeometryLong(hash, 0x545249414E474C45L);
            return mixGeometryVertices(hash, triangles);
        }

        private static long mixGeometryVertices(long hash, List<PreparedVertex> vertices) {
            hash = mixGeometryLong(hash, vertices.size());
            for (PreparedVertex vertex : vertices) {
                hash = mixGeometryVector(hash, vertex.position());
                hash = mixGeometryUv(hash, vertex.uv());
                hash = mixGeometryVector(hash, vertex.smoothNormal());
                hash = mixGeometryVector(hash, vertex.faceNormal());
                hash = mixGeometryUv(hash, vertex.averageUv());
            }
            return hash;
        }

        private static long mixGeometryVector(long hash, Vector3f vector) {
            hash = mixGeometryLong(hash, Float.floatToRawIntBits(vector.x()) & 0xFFFFFFFFL);
            hash = mixGeometryLong(hash, Float.floatToRawIntBits(vector.y()) & 0xFFFFFFFFL);
            return mixGeometryLong(hash, Float.floatToRawIntBits(vector.z()) & 0xFFFFFFFFL);
        }

        private static long mixGeometryUv(long hash, UV uv) {
            hash = mixGeometryLong(hash, Float.floatToRawIntBits(uv.u()) & 0xFFFFFFFFL);
            return mixGeometryLong(hash, Float.floatToRawIntBits(uv.v()) & 0xFFFFFFFFL);
        }

        private static long transientLightSampleHash(long[] payload) {
            long hash = 0xBB67AE8584CAA73BL;
            for (long word : payload) {
                hash = mixGeometryLong(hash, word);
            }
            return hash;
        }

        private static long mixGeometryLong(long hash, long value) {
            hash ^= value + 0x9E3779B97F4A7C15L + (hash << 6) + (hash >>> 2);
            hash ^= hash >>> 29;
            hash *= 0xBF58476D1CE4E5B9L;
            hash ^= hash >>> 32;
            return hash;
        }
    }

    private enum GpuMeshKind {
        TEXTURED,
        SPRITE,
        UNTEXTURED
    }

    private static final class GlObjectDeleteGuard {
        private final int previousVao;
        private final int previousArrayBuffer;
        private final int previousElementArrayBuffer;
        private final int previousCopyReadBuffer;
        private final int previousCopyWriteBuffer;

        private GlObjectDeleteGuard() {
            this.previousVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
            this.previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
            this.previousElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
            this.previousCopyReadBuffer = GL11.glGetInteger(GL31.GL_COPY_READ_BUFFER);
            this.previousCopyWriteBuffer = GL11.glGetInteger(GL31.GL_COPY_WRITE_BUFFER);
        }

        private static GlObjectDeleteGuard snapshot() {
            return new GlObjectDeleteGuard();
        }

        private void restoreAfterDeleting(int deletedVao, int... deletedBuffers) {
            Throwable failure = null;
            int restoredVao = previousVao == deletedVao ? 0 : previousVao;
            failure = restoreStep(failure, () -> GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,
                    deletedBufferBinding(previousArrayBuffer, deletedBuffers)));
            failure = restoreStep(failure, () -> GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER,
                    deletedBufferBinding(previousCopyReadBuffer, deletedBuffers)));
            failure = restoreStep(failure, () -> GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER,
                    deletedBufferBinding(previousCopyWriteBuffer, deletedBuffers)));
            failure = restoreStep(failure, () -> HbmGlVaoSafety.bindVertexArray(restoredVao));
            int restoredElementArrayBuffer = previousVao == deletedVao
                    ? 0
                    : deletedBufferBinding(previousElementArrayBuffer, deletedBuffers);
            failure = restoreStep(failure,
                    () -> GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, restoredElementArrayBuffer));
            if (failure != null) {
                HbmNtm.LOGGER.error("Legacy OBJ GL object delete binding restore failed", failure);
            }
        }

        private static int deletedBufferBinding(int previousBinding, int... deletedBuffers) {
            for (int deletedBuffer : deletedBuffers) {
                if (deletedBuffer != 0 && previousBinding == deletedBuffer) {
                    return 0;
                }
            }
            return previousBinding;
        }

        private static Throwable restoreStep(Throwable failure, Runnable action) {
            try {
                action.run();
            } catch (Throwable throwable) {
                if (failure == null) {
                    return throwable;
                }
                failure.addSuppressed(throwable);
            }
            return failure;
        }
    }

    private record GpuMeshKey(GpuMeshKind kind, String stablePartKey, PreparedGeometryKey geometryKey,
                              TextureAtlasSprite sprite, int sourceVertices,
                              VertexFormat.Mode drawMode, int packedLight, int packedOverlay, int red, int green,
                              int blue, int alpha, UvTransform uvTransform, boolean smoothing) {
    }

    private record GpuMesh(VertexBuffer vertexBuffer, long byteSize, int arrayObjectId, int vertexBufferId,
                           AtomicBoolean closed) {
        private void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            GlObjectDeleteGuard guard = GlObjectDeleteGuard.snapshot();
            Throwable failure = null;
            try {
                failure = GlObjectDeleteGuard.restoreStep(failure, vertexBuffer::close);
            } finally {
                guard.restoreAfterDeleting(arrayObjectId, vertexBufferId);
            }
            if (failure != null) {
                throw new IllegalStateException("Legacy OBJ cached GPU mesh cleanup failed", failure);
            }
        }
    }

    private static long lightSampleKey(InstancedMeshKey key) {
        long hash = 0xD6E8FEB86659FD93L;
        hash = mixLightSampleKey(hash, key.kind().ordinal());
        hash = mixLightSampleKey(hash, key.stablePartKey() == null ? 0 : key.stablePartKey().hashCode());
        hash = mixLightSampleKey(hash, key.geometryKey().lightSampleHash());
        hash = mixLightSampleKey(hash, System.identityHashCode(key.sprite()));
        hash = mixLightSampleKey(hash, key.sourceVertices());
        hash = mixLightSampleKey(hash, key.sourceMode().ordinal());
        hash = mixLightSampleKey(hash, key.smoothing() ? 1 : 0);
        return hash == 0L ? 1L : hash;
    }

    private static long lightSampleKey(IrisCompanionMeshKey key) {
        long hash = 0xA0761D6478BD642FL;
        hash = mixLightSampleKey(hash, key.kind().ordinal());
        hash = mixLightSampleKey(hash, key.stablePartKey() == null ? 0 : key.stablePartKey().hashCode());
        hash = mixLightSampleKey(hash, key.geometryKey().lightSampleHash());
        hash = mixLightSampleKey(hash, System.identityHashCode(key.sprite()));
        hash = mixLightSampleKey(hash, key.sourceVertices());
        hash = mixLightSampleKey(hash, key.sourceMode().ordinal());
        hash = mixLightSampleKey(hash, key.smoothing() ? 1 : 0);
        hash = mixLightSampleKey(hash, key.uvTransform().hashCode());
        return hash == 0L ? 1L : hash;
    }

    private static long mixLightSampleKey(long hash, long value) {
        hash ^= value + 0x9E3779B97F4A7C15L + (hash << 6) + (hash >>> 2);
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >>> 33;
        return hash;
    }

    private record IrisCompanionMeshKey(GpuMeshKind kind, String stablePartKey, PreparedGeometryKey geometryKey,
                                        TextureAtlasSprite sprite, int sourceVertices,
                                        VertexFormat.Mode sourceMode, boolean smoothing, UvTransform uvTransform) {
    }

    private record IrisRenderBatchKey(ResourceLocation textureLocation, LegacyTexturedRenderMode renderMode,
                                      boolean shadowPass, boolean linePrimitive) {
    }

    private record IrisCompanionQueueKey(GpuMeshKind kind, String stablePartKey, PreparedGeometryKey geometryKey,
                                         TextureAtlasSprite sprite, int sourceVertices,
                                         VertexFormat.Mode sourceMode, boolean smoothing, UvTransform uvTransform,
                                         ResourceLocation textureLocation, LegacyTexturedRenderMode renderMode) {
    }

    private record InstancedMeshKey(GpuMeshKind kind, String stablePartKey, PreparedGeometryKey geometryKey,
                                    TextureAtlasSprite sprite, int sourceVertices, VertexFormat.Mode sourceMode,
                                    boolean smoothing) {
    }

    private record InstancedBatchKey(InstancedMeshKey meshKey, ResourceLocation textureLocation,
                                     LegacyTexturedRenderMode renderMode) {
    }

    private record MdiDrawGroupKey(GpuMeshKind kind, ResourceLocation textureLocation,
                                   LegacyTexturedRenderMode renderMode) {
    }

    private record MdiDrawGroups(MdiDrawGroup singleGroup,
                                 Map<MdiDrawGroupKey, MdiDrawGroup> groupedBatches) {
        private static final MdiDrawGroups EMPTY = new MdiDrawGroups(null, Map.of());

        private boolean isEmpty() {
            return singleGroup == null && groupedBatches.isEmpty();
        }

        private boolean isSingleGroup() {
            return singleGroup != null;
        }
    }

    private static final class MdiDrawGroup {
        private final List<InstancedBatch> ownedBatches = new ArrayList<>();
        private MdiDrawGroupKey key;
        private List<InstancedBatch> batches = ownedBatches;
        private int instances;

        private MdiDrawGroup() {
        }

        private MdiDrawGroup(MdiDrawGroupKey key) {
            reset(key);
        }

        private MdiDrawGroup(MdiDrawGroupKey key, List<InstancedBatch> batches, int instances) {
            resetBorrowed(key, batches, instances);
        }

        private void reset(MdiDrawGroupKey key) {
            this.key = key;
            this.batches = ownedBatches;
            this.ownedBatches.clear();
            this.instances = 0;
        }

        private void resetBorrowed(MdiDrawGroupKey key, List<InstancedBatch> batches, int instances) {
            this.key = key;
            this.batches = batches;
            this.instances = Math.max(0, instances);
        }

        private void clear() {
            key = null;
            instances = 0;
            if (batches == ownedBatches) {
                ownedBatches.clear();
            } else {
                batches = ownedBatches;
                ownedBatches.clear();
            }
        }

        private void add(InstancedBatch batch, int instances) {
            batches.add(batch);
            this.instances += Math.max(0, instances);
        }

        private void addAll(List<InstancedBatch> source, int fromIndex, int toIndex, int instances) {
            int from = Math.max(0, fromIndex);
            int to = Math.max(from, Math.min(toIndex, source.size()));
            for (int i = from; i < to; i++) {
                batches.add(source.get(i));
            }
            this.instances += Math.max(0, instances);
        }

        private MdiDrawGroupKey key() {
            return key;
        }

        private List<InstancedBatch> batches() {
            return batches;
        }

        private int instances() {
            return instances;
        }
    }

    private record MdiSubDraw(int commandIndex, InstancedMeshKey meshKey, int firstVertex, int vertexCount,
                              int baseInstance, int instanceCount) {
    }

    private record MdiPreparedGroup(MdiDrawGroupKey key, List<InstancedBatch> batches, ByteBuffer instanceBytes,
                                    ByteBuffer commandBytes, int commandCount, int instanceCount,
                                    int atlasLayoutGeneration, List<MdiSubDraw> subDraws) {
        private static final int COMMAND_PACKED_BYTES = 16;
        private static final int COMMAND_STRIDE_BYTES = 32;

        private static void writeDrawArraysIndirectCommand(long address, int count, int primCount,
                int first, int baseInstance) {
            MemoryUtil.memPutInt(address, count);
            MemoryUtil.memPutInt(address + 4L, primCount);
            MemoryUtil.memPutInt(address + 8L, first);
            MemoryUtil.memPutInt(address + 12L, baseInstance);
            MemoryUtil.memPutLong(address + COMMAND_PACKED_BYTES, 0L);
            MemoryUtil.memPutLong(address + COMMAND_PACKED_BYTES + 8L, 0L);
        }
    }

    private record MdiDrawResult(boolean drew, List<InstancedBatch> drawnBatches) {
        private static final MdiDrawResult FAILED = new MdiDrawResult(false, List.of());
    }

    private static final class MdiPreparedGroupDrawException extends RuntimeException {
        private final boolean drawSubmitted;
        private final int submittedCommandCount;

        private MdiPreparedGroupDrawException(RuntimeException cause, boolean drawSubmitted,
                int submittedCommandCount) {
            super(cause);
            this.drawSubmitted = drawSubmitted;
            this.submittedCommandCount = Math.max(0, submittedCommandCount);
        }

        private boolean drawSubmitted() {
            return drawSubmitted;
        }

        private int submittedCommandCount() {
            return submittedCommandCount;
        }
    }

    private static final class InstancedBatchDrawException extends RuntimeException {
        private final int fallbackStartIndex;

        private InstancedBatchDrawException(RuntimeException cause, int fallbackStartIndex) {
            super(cause);
            this.fallbackStartIndex = Math.max(0, fallbackStartIndex);
        }

        private int fallbackStartIndex() {
            return fallbackStartIndex;
        }
    }

    private static final class IrisCompanionQueuedBatchDrawException extends RuntimeException {
        private final int fallbackStartIndex;

        private IrisCompanionQueuedBatchDrawException(RuntimeException cause, int fallbackStartIndex) {
            super(cause);
            this.fallbackStartIndex = Math.max(0, fallbackStartIndex);
        }

        private int fallbackStartIndex() {
            return fallbackStartIndex;
        }
    }

    private static final class MdiPartialDrawException extends RuntimeException {
        private final List<InstancedBatch> suppressFallbackBatches;

        private MdiPartialDrawException(Throwable cause, List<InstancedBatch> suppressFallbackBatches) {
            super(cause);
            this.suppressFallbackBatches = List.copyOf(suppressFallbackBatches);
        }

        private List<InstancedBatch> suppressFallbackBatches() {
            return suppressFallbackBatches;
        }
    }

    private static <T> int removeDuplicateEntries(List<T> entries, Set<T> seen) {
        int size = entries.size();
        if (size <= 1) {
            return 0;
        }
        if (size <= 4) {
            return removeDuplicateEntriesSmall(entries);
        }
        seen.clear();
        int write = 0;
        int removed = 0;
        try {
            for (int read = 0; read < size; read++) {
                T entry = entries.get(read);
                if (seen.add(entry)) {
                    if (write != read) {
                        entries.set(write, entry);
                    }
                    write++;
                } else {
                    removed++;
                }
            }
            if (removed > 0) {
                entries.subList(write, size).clear();
            }
            return removed;
        } finally {
            seen.clear();
        }
    }

    private static <T> int removeDuplicateEntriesSmall(List<T> entries) {
        int size = entries.size();
        int write = 0;
        int removed = 0;
        for (int read = 0; read < size; read++) {
            T entry = entries.get(read);
            boolean duplicate = false;
            for (int i = 0; i < write; i++) {
                if (entry.equals(entries.get(i))) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) {
                removed++;
                continue;
            }
            if (write != read) {
                entries.set(write, entry);
            }
            write++;
        }
        if (removed > 0) {
            entries.subList(write, size).clear();
        }
        return removed;
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

        private int removeDuplicateInstances(Set<IrisCompanionQueuedInstance> seen) {
            return removeDuplicateEntries(instances, seen);
        }
    }

    private static final class IrisCompanionQueuedInstance {
        private final float p0;
        private final float p1;
        private final float p2;
        private final float p3;
        private final float p4;
        private final float p5;
        private final float p6;
        private final float p7;
        private final float p8;
        private final float p9;
        private final float p10;
        private final float p11;
        private final float p12;
        private final float p13;
        private final float p14;
        private final float p15;
        private final float n0;
        private final float n1;
        private final float n2;
        private final float n3;
        private final float n4;
        private final float n5;
        private final float n6;
        private final float n7;
        private final float n8;
        private final MultiBufferSource buffer;
        private final int packedLight;
        private final int packedOverlay;
        private final int red;
        private final int green;
        private final int blue;
        private final int alpha;
        private final float fadeAlpha;
        private int contentHash;
        private boolean contentHashComputed;
        private final float sortDepthSq;

        private IrisCompanionQueuedInstance(PoseStack.Pose pose, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, float fadeAlpha) {
            Matrix4f position = pose.pose();
            Matrix3f normal = pose.normal();
            this.p0 = position.m00();
            this.p1 = position.m01();
            this.p2 = position.m02();
            this.p3 = position.m03();
            this.p4 = position.m10();
            this.p5 = position.m11();
            this.p6 = position.m12();
            this.p7 = position.m13();
            this.p8 = position.m20();
            this.p9 = position.m21();
            this.p10 = position.m22();
            this.p11 = position.m23();
            this.p12 = position.m30();
            this.p13 = position.m31();
            this.p14 = position.m32();
            this.p15 = position.m33();
            this.n0 = normal.m00();
            this.n1 = normal.m01();
            this.n2 = normal.m02();
            this.n3 = normal.m10();
            this.n4 = normal.m11();
            this.n5 = normal.m12();
            this.n6 = normal.m20();
            this.n7 = normal.m21();
            this.n8 = normal.m22();
            this.buffer = buffer;
            this.packedLight = packedLight;
            this.packedOverlay = packedOverlay;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            this.fadeAlpha = Float.isFinite(fadeAlpha) ? Mth.clamp(fadeAlpha, 0.0F, 1.0F) : 1.0F;
            this.sortDepthSq = viewSortDepthSq(position);
        }

        private static IrisCompanionQueuedInstance from(PoseStack.Pose pose, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, float fadeAlpha) {
            return new IrisCompanionQueuedInstance(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                    fadeAlpha);
        }

        private Matrix4f position() {
            return new Matrix4f(p0, p1, p2, p3, p4, p5, p6, p7,
                    p8, p9, p10, p11, p12, p13, p14, p15);
        }

        private Matrix3f normal() {
            return new Matrix3f(n0, n1, n2, n3, n4, n5, n6, n7, n8);
        }

        private MultiBufferSource buffer() {
            return buffer;
        }

        private int packedLight() {
            return packedLight;
        }

        private int packedOverlay() {
            return packedOverlay;
        }

        private int red() {
            return red;
        }

        private int green() {
            return green;
        }

        private int blue() {
            return blue;
        }

        private int alpha() {
            return alpha;
        }

        private float fadeAlpha() {
            return fadeAlpha;
        }

        private float sortDepthSq() {
            return sortDepthSq;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof IrisCompanionQueuedInstance other)) {
                return false;
            }
            return packedLight == other.packedLight
                    && packedOverlay == other.packedOverlay
                    && red == other.red
                    && green == other.green
                    && blue == other.blue
                    && alpha == other.alpha
                    && Float.floatToIntBits(fadeAlpha) == Float.floatToIntBits(other.fadeAlpha)
                    && same(p0, other.p0)
                    && same(p1, other.p1)
                    && same(p2, other.p2)
                    && same(p3, other.p3)
                    && same(p4, other.p4)
                    && same(p5, other.p5)
                    && same(p6, other.p6)
                    && same(p7, other.p7)
                    && same(p8, other.p8)
                    && same(p9, other.p9)
                    && same(p10, other.p10)
                    && same(p11, other.p11)
                    && same(p12, other.p12)
                    && same(p13, other.p13)
                    && same(p14, other.p14)
                    && same(p15, other.p15)
                    && same(n0, other.n0)
                    && same(n1, other.n1)
                    && same(n2, other.n2)
                    && same(n3, other.n3)
                    && same(n4, other.n4)
                    && same(n5, other.n5)
                    && same(n6, other.n6)
                    && same(n7, other.n7)
                    && same(n8, other.n8);
        }

        @Override
        public int hashCode() {
            if (!contentHashComputed) {
                contentHash = computeContentHash();
                contentHashComputed = true;
            }
            return contentHash;
        }

        private int computeContentHash() {
            int hash = matrix4Hash();
            hash = 31 * hash + matrix3Hash();
            hash = 31 * hash + packedLight;
            hash = 31 * hash + packedOverlay;
            hash = 31 * hash + red;
            hash = 31 * hash + green;
            hash = 31 * hash + blue;
            hash = 31 * hash + alpha;
            hash = 31 * hash + Float.floatToIntBits(fadeAlpha);
            return hash;
        }

        private int matrix4Hash() {
            int hash = Float.floatToIntBits(p0);
            hash = appendHash(hash, p1);
            hash = appendHash(hash, p2);
            hash = appendHash(hash, p3);
            hash = appendHash(hash, p4);
            hash = appendHash(hash, p5);
            hash = appendHash(hash, p6);
            hash = appendHash(hash, p7);
            hash = appendHash(hash, p8);
            hash = appendHash(hash, p9);
            hash = appendHash(hash, p10);
            hash = appendHash(hash, p11);
            hash = appendHash(hash, p12);
            hash = appendHash(hash, p13);
            hash = appendHash(hash, p14);
            hash = appendHash(hash, p15);
            return hash;
        }

        private int matrix3Hash() {
            int hash = Float.floatToIntBits(n0);
            hash = appendHash(hash, n1);
            hash = appendHash(hash, n2);
            hash = appendHash(hash, n3);
            hash = appendHash(hash, n4);
            hash = appendHash(hash, n5);
            hash = appendHash(hash, n6);
            hash = appendHash(hash, n7);
            hash = appendHash(hash, n8);
            return hash;
        }

        private static int appendHash(int hash, float value) {
            return 31 * hash + Float.floatToIntBits(value);
        }

        private static boolean same(float left, float right) {
            return Float.floatToIntBits(left) == Float.floatToIntBits(right);
        }
    }

    private static final class InstancedBatch {
        private final InstancedBatchKey key;
        private final InstancedMesh mesh;
        private final ResourceLocation textureLocation;
        private final LegacyTexturedRenderMode renderMode;
        private final List<InstancedInstance> instances = new ArrayList<>();

        private InstancedBatch(InstancedBatchKey key, InstancedMesh mesh, ResourceLocation textureLocation,
                LegacyTexturedRenderMode renderMode) {
            this.key = key;
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
            instances.sort((left, right) -> Float.compare(right.sortDepthSq(), left.sortDepthSq()));
        }

        private int removeDuplicateInstances(Set<InstancedInstance> seen) {
            return removeDuplicateEntries(instances, seen);
        }
    }

    private static final class InstancedInstance {
        private static final int FLOATS = 40;

        private final float f0;
        private final float f1;
        private final float f2;
        private final float f3;
        private final float f4;
        private final float f5;
        private final float f6;
        private final float f7;
        private final float f8;
        private final float f9;
        private final float f10;
        private final float f11;
        private final float f12;
        private final float f13;
        private final float f14;
        private final float f15;
        private final float f16;
        private final float f17;
        private final float f18;
        private final float f19;
        private final float f20;
        private final float f21;
        private final float f22;
        private final float f23;
        private final float f24;
        private final float f25;
        private final float f26;
        private final float f27;
        private final float f28;
        private final float f29;
        private final float f30;
        private final float f31;
        private final float f32;
        private final float f33;
        private final float f34;
        private final float f35;
        private final float f36;
        private final float f37;
        private final float f38;
        private final float f39;
        private final float n0;
        private final float n1;
        private final float n2;
        private final float n3;
        private final float n4;
        private final float n5;
        private final float n6;
        private final float n7;
        private final float n8;
        private final MultiBufferSource buffer;
        private final int packedLight;
        private final int packedOverlay;
        private final int red;
        private final int green;
        private final int blue;
        private final int alpha;
        private final UvTransform uvTransform;
        private int dataHash;
        private boolean dataHashComputed;
        private final float sortDepthSq;

        private InstancedInstance(PoseStack.Pose pose, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, UvTransform uvTransform,
                float fadeAlpha, LegacyRenderLighting.SlicedLightProbe lightProbe) {
            Matrix4f modelView = pose.pose();
            Matrix3f normal = pose.normal();
            this.f0 = finiteOrDefault(modelView.m00(), 1.0F);
            this.f1 = finiteOrDefault(modelView.m01(), 0.0F);
            this.f2 = finiteOrDefault(modelView.m02(), 0.0F);
            this.f3 = finiteOrDefault(modelView.m03(), 0.0F);
            this.f4 = finiteOrDefault(modelView.m10(), 0.0F);
            this.f5 = finiteOrDefault(modelView.m11(), 1.0F);
            this.f6 = finiteOrDefault(modelView.m12(), 0.0F);
            this.f7 = finiteOrDefault(modelView.m13(), 0.0F);
            this.f8 = finiteOrDefault(modelView.m20(), 0.0F);
            this.f9 = finiteOrDefault(modelView.m21(), 0.0F);
            this.f10 = finiteOrDefault(modelView.m22(), 1.0F);
            this.f11 = finiteOrDefault(modelView.m23(), 0.0F);
            this.f12 = finiteOrDefault(modelView.m30(), 0.0F);
            this.f13 = finiteOrDefault(modelView.m31(), 0.0F);
            this.f14 = finiteOrDefault(modelView.m32(), 0.0F);
            this.f15 = finiteOrDefault(modelView.m33(), 1.0F);
            this.f16 = packedLightFloat(lightProbe, 0);
            this.f17 = packedLightFloat(lightProbe, 1);
            this.f18 = packedLightFloat(lightProbe, 2);
            this.f19 = packedLightFloat(lightProbe, 3);
            this.f20 = packedLightFloat(lightProbe, 4);
            this.f21 = packedLightFloat(lightProbe, 5);
            this.f22 = packedLightFloat(lightProbe, 6);
            this.f23 = packedLightFloat(lightProbe, 7);
            this.f24 = packedLightFloat(lightProbe, 8);
            this.f25 = packedLightFloat(lightProbe, 9);
            this.f26 = packedLightFloat(lightProbe, 10);
            this.f27 = packedLightFloat(lightProbe, 11);
            this.f28 = packedLightFloat(lightProbe, 12);
            this.f29 = packedLightFloat(lightProbe, 13);
            this.f30 = packedLightFloat(lightProbe, 14);
            this.f31 = packedLightFloat(lightProbe, 15);
            this.f32 = red / 255.0F;
            this.f33 = green / 255.0F;
            this.f34 = blue / 255.0F;
            this.f35 = alpha / 255.0F;
            this.f36 = packedOverlay & 0xFFFF;
            this.f37 = packedOverlay >>> 16 & 0xFFFF;
            this.f38 = finiteClampedUnitOrDefault(fadeAlpha, 1.0F);
            this.f39 = 0.0F;
            this.n0 = normal.m00();
            this.n1 = normal.m01();
            this.n2 = normal.m02();
            this.n3 = normal.m10();
            this.n4 = normal.m11();
            this.n5 = normal.m12();
            this.n6 = normal.m20();
            this.n7 = normal.m21();
            this.n8 = normal.m22();
            this.buffer = buffer;
            this.packedLight = packedLight;
            this.packedOverlay = packedOverlay;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            this.uvTransform = uvTransform;
            this.sortDepthSq = viewSortDepthSq(modelView);
        }

        private static InstancedInstance from(PoseStack.Pose pose, MultiBufferSource buffer, int packedLight,
                int packedOverlay, int red, int green, int blue, int alpha, UvTransform uvTransform,
                float fadeAlpha, LegacyRenderLighting.SlicedLightProbe lightProbe) {
            return new InstancedInstance(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                    uvTransform, fadeAlpha, lightProbe);
        }

        private static float finiteOrDefault(float value, float fallback) {
            return Float.isFinite(value) ? value : fallback;
        }

        private static float finiteClampedUnitOrDefault(float value, float fallback) {
            if (!Float.isFinite(value)) {
                return fallback;
            }
            return Mth.clamp(value, 0.0F, 1.0F);
        }

        private static float packedLightFloat(LegacyRenderLighting.SlicedLightProbe lightProbe, int index) {
            int packedLight = lightProbe.probe(index);
            return LightTexture.block(packedLight) + LightTexture.sky(packedLight) * 16.0F;
        }

        private void write(long address) {
            MemoryUtil.memPutFloat(address, f0);
            MemoryUtil.memPutFloat(address + 4L, f1);
            MemoryUtil.memPutFloat(address + 8L, f2);
            MemoryUtil.memPutFloat(address + 12L, f3);
            MemoryUtil.memPutFloat(address + 16L, f4);
            MemoryUtil.memPutFloat(address + 20L, f5);
            MemoryUtil.memPutFloat(address + 24L, f6);
            MemoryUtil.memPutFloat(address + 28L, f7);
            MemoryUtil.memPutFloat(address + 32L, f8);
            MemoryUtil.memPutFloat(address + 36L, f9);
            MemoryUtil.memPutFloat(address + 40L, f10);
            MemoryUtil.memPutFloat(address + 44L, f11);
            MemoryUtil.memPutFloat(address + 48L, f12);
            MemoryUtil.memPutFloat(address + 52L, f13);
            MemoryUtil.memPutFloat(address + 56L, f14);
            MemoryUtil.memPutFloat(address + 60L, f15);
            MemoryUtil.memPutFloat(address + 64L, f16);
            MemoryUtil.memPutFloat(address + 68L, f17);
            MemoryUtil.memPutFloat(address + 72L, f18);
            MemoryUtil.memPutFloat(address + 76L, f19);
            MemoryUtil.memPutFloat(address + 80L, f20);
            MemoryUtil.memPutFloat(address + 84L, f21);
            MemoryUtil.memPutFloat(address + 88L, f22);
            MemoryUtil.memPutFloat(address + 92L, f23);
            MemoryUtil.memPutFloat(address + 96L, f24);
            MemoryUtil.memPutFloat(address + 100L, f25);
            MemoryUtil.memPutFloat(address + 104L, f26);
            MemoryUtil.memPutFloat(address + 108L, f27);
            MemoryUtil.memPutFloat(address + 112L, f28);
            MemoryUtil.memPutFloat(address + 116L, f29);
            MemoryUtil.memPutFloat(address + 120L, f30);
            MemoryUtil.memPutFloat(address + 124L, f31);
            MemoryUtil.memPutFloat(address + 128L, f32);
            MemoryUtil.memPutFloat(address + 132L, f33);
            MemoryUtil.memPutFloat(address + 136L, f34);
            MemoryUtil.memPutFloat(address + 140L, f35);
            MemoryUtil.memPutFloat(address + 144L, f36);
            MemoryUtil.memPutFloat(address + 148L, f37);
            MemoryUtil.memPutFloat(address + 152L, f38);
            MemoryUtil.memPutFloat(address + 156L, f39);
        }

        private float sortDepthSq() {
            return sortDepthSq;
        }

        private Matrix4f position() {
            return new Matrix4f(f0, f1, f2, f3, f4, f5, f6, f7,
                    f8, f9, f10, f11, f12, f13, f14, f15);
        }

        private Matrix3f normal() {
            return new Matrix3f(n0, n1, n2, n3, n4, n5, n6, n7, n8);
        }

        private MultiBufferSource buffer() {
            return buffer;
        }

        private int packedLight() {
            return packedLight;
        }

        private int packedOverlay() {
            return packedOverlay;
        }

        private int red() {
            return red;
        }

        private int green() {
            return green;
        }

        private int blue() {
            return blue;
        }

        private int alpha() {
            return alpha;
        }

        private UvTransform uvTransform() {
            return uvTransform;
        }

        private float fadeAlpha() {
            return f38;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof InstancedInstance other)) {
                return false;
            }
            return same(f0, other.f0)
                    && same(f1, other.f1)
                    && same(f2, other.f2)
                    && same(f3, other.f3)
                    && same(f4, other.f4)
                    && same(f5, other.f5)
                    && same(f6, other.f6)
                    && same(f7, other.f7)
                    && same(f8, other.f8)
                    && same(f9, other.f9)
                    && same(f10, other.f10)
                    && same(f11, other.f11)
                    && same(f12, other.f12)
                    && same(f13, other.f13)
                    && same(f14, other.f14)
                    && same(f15, other.f15)
                    && same(f16, other.f16)
                    && same(f17, other.f17)
                    && same(f18, other.f18)
                    && same(f19, other.f19)
                    && same(f20, other.f20)
                    && same(f21, other.f21)
                    && same(f22, other.f22)
                    && same(f23, other.f23)
                    && same(f24, other.f24)
                    && same(f25, other.f25)
                    && same(f26, other.f26)
                    && same(f27, other.f27)
                    && same(f28, other.f28)
                    && same(f29, other.f29)
                    && same(f30, other.f30)
                    && same(f31, other.f31)
                    && same(f32, other.f32)
                    && same(f33, other.f33)
                    && same(f34, other.f34)
                    && same(f35, other.f35)
                    && same(f36, other.f36)
                    && same(f37, other.f37)
                    && same(f38, other.f38)
                    && same(f39, other.f39);
        }

        @Override
        public int hashCode() {
            if (!dataHashComputed) {
                dataHash = computeDataHash();
                dataHashComputed = true;
            }
            return dataHash;
        }

        private int computeDataHash() {
            int result = 1;
            result = appendHash(result, f0);
            result = appendHash(result, f1);
            result = appendHash(result, f2);
            result = appendHash(result, f3);
            result = appendHash(result, f4);
            result = appendHash(result, f5);
            result = appendHash(result, f6);
            result = appendHash(result, f7);
            result = appendHash(result, f8);
            result = appendHash(result, f9);
            result = appendHash(result, f10);
            result = appendHash(result, f11);
            result = appendHash(result, f12);
            result = appendHash(result, f13);
            result = appendHash(result, f14);
            result = appendHash(result, f15);
            result = appendHash(result, f16);
            result = appendHash(result, f17);
            result = appendHash(result, f18);
            result = appendHash(result, f19);
            result = appendHash(result, f20);
            result = appendHash(result, f21);
            result = appendHash(result, f22);
            result = appendHash(result, f23);
            result = appendHash(result, f24);
            result = appendHash(result, f25);
            result = appendHash(result, f26);
            result = appendHash(result, f27);
            result = appendHash(result, f28);
            result = appendHash(result, f29);
            result = appendHash(result, f30);
            result = appendHash(result, f31);
            result = appendHash(result, f32);
            result = appendHash(result, f33);
            result = appendHash(result, f34);
            result = appendHash(result, f35);
            result = appendHash(result, f36);
            result = appendHash(result, f37);
            result = appendHash(result, f38);
            result = appendHash(result, f39);
            return result;
        }

        private static int appendHash(int hash, float value) {
            return 31 * hash + Float.floatToIntBits(value);
        }

        private static boolean same(float left, float right) {
            return Float.floatToIntBits(left) == Float.floatToIntBits(right);
        }
    }

    private static float viewSortDepthSq(Matrix4f matrix) {
        float x = matrix.m30();
        float y = matrix.m31();
        float z = matrix.m32();
        return x * x + y * y + z * z;
    }

    private record InstancedMesh(InstancedMeshKey key, VertexFormat.Mode sourceMode,
                                 List<PreparedVertex> sourceVertices, InstancedMeshBounds bounds,
                                 long lightSampleKey, int vaoId, int vboId, int instanceVboId,
                                 int vertexCount, long byteSize, AtomicInteger instanceCapacityBytes,
                                 AtomicBoolean closed) {
        private LegacyRenderLighting.LightProbe sampleLightProbe(Matrix4f modelView, int packedLight) {
            return bounds.sampleLightProbe(modelView, lightSampleKey, packedLight);
        }

        private LegacyRenderLighting.SlicedLightProbe sampleInstanceLightProbe(Matrix4f modelView, int packedLight,
                boolean useSlicedLight) {
            if (packedLight == LightTexture.FULL_BRIGHT) {
                return LegacyRenderLighting.SlicedLightProbe.uniform(packedLight);
            }
            if (useSlicedLight) {
                return bounds.sampleSlicedLightProbe(modelView, lightSampleKey, packedLight);
            }
            return LegacyRenderLighting.SlicedLightProbe.from(sampleLightProbe(modelView, packedLight));
        }

        private void enableVertexAttribArraysOnBoundVao() {
            if (vaoId == 0) {
                return;
            }
            for (int attribute = 0; attribute <= 13; attribute++) {
                GL20.glEnableVertexAttribArray(attribute);
            }
        }

        private void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            GlObjectDeleteGuard guard = GlObjectDeleteGuard.snapshot();
            Throwable failure = null;
            try {
                if (vaoId != 0) {
                    int targetVao = vaoId;
                    failure = GlObjectDeleteGuard.restoreStep(failure, () -> GL30.glDeleteVertexArrays(targetVao));
                }
                if (vboId != 0) {
                    int targetVbo = vboId;
                    failure = GlObjectDeleteGuard.restoreStep(failure, () -> GL15.glDeleteBuffers(targetVbo));
                }
                if (instanceVboId != 0) {
                    int targetInstanceVbo = instanceVboId;
                    failure = GlObjectDeleteGuard.restoreStep(failure,
                            () -> GL15.glDeleteBuffers(targetInstanceVbo));
                }
            } finally {
                guard.restoreAfterDeleting(vaoId, vboId, instanceVboId);
            }
            if (failure != null) {
                throw new IllegalStateException("Legacy OBJ instanced mesh cleanup failed", failure);
            }
        }
    }

    private record InstancedMeshBounds(float minX, float minY, float minZ,
                                       float maxX, float maxY, float maxZ,
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
                return new InstancedMeshBounds(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
                        0.0F, 0.0F, 0.0F);
            }
            return new InstancedMeshBounds(minX, minY, minZ, maxX, maxY, maxZ,
                    inverseExtent(maxX - minX),
                    inverseExtent(maxY - minY),
                    inverseExtent(maxZ - minZ));
        }

        private static InstancedMeshBounds ofLines(List<UntexturedLineTransient> lines) {
            float minX = Float.POSITIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float minZ = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            float maxZ = Float.NEGATIVE_INFINITY;
            for (UntexturedLineTransient line : lines) {
                minX = (float) Math.min(minX, Math.min(line.x0(), line.x1()));
                minY = (float) Math.min(minY, Math.min(line.y0(), line.y1()));
                minZ = (float) Math.min(minZ, Math.min(line.z0(), line.z1()));
                maxX = (float) Math.max(maxX, Math.max(line.x0(), line.x1()));
                maxY = (float) Math.max(maxY, Math.max(line.y0(), line.y1()));
                maxZ = (float) Math.max(maxZ, Math.max(line.z0(), line.z1()));
            }
            if (!Float.isFinite(minX) || !Float.isFinite(minY) || !Float.isFinite(minZ)
                    || !Float.isFinite(maxX) || !Float.isFinite(maxY) || !Float.isFinite(maxZ)) {
                return new InstancedMeshBounds(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
                        0.0F, 0.0F, 0.0F);
            }
            return new InstancedMeshBounds(minX, minY, minZ, maxX, maxY, maxZ,
                    inverseExtent(maxX - minX),
                    inverseExtent(maxY - minY),
                    inverseExtent(maxZ - minZ));
        }

        private static InstancedMeshBounds ofVertexColors(List<UntexturedVertexColor> vertices) {
            float minX = Float.POSITIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float minZ = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            float maxZ = Float.NEGATIVE_INFINITY;
            for (UntexturedVertexColor vertex : vertices) {
                minX = (float) Math.min(minX, vertex.x());
                minY = (float) Math.min(minY, vertex.y());
                minZ = (float) Math.min(minZ, vertex.z());
                maxX = (float) Math.max(maxX, vertex.x());
                maxY = (float) Math.max(maxY, vertex.y());
                maxZ = (float) Math.max(maxZ, vertex.z());
            }
            if (!Float.isFinite(minX) || !Float.isFinite(minY) || !Float.isFinite(minZ)
                    || !Float.isFinite(maxX) || !Float.isFinite(maxY) || !Float.isFinite(maxZ)) {
                return new InstancedMeshBounds(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
                        0.0F, 0.0F, 0.0F);
            }
            return new InstancedMeshBounds(minX, minY, minZ, maxX, maxY, maxZ,
                    inverseExtent(maxX - minX),
                    inverseExtent(maxY - minY),
                    inverseExtent(maxZ - minZ));
        }

        private LegacyRenderLighting.LightProbe sampleLightProbe(Matrix4f modelView, int packedLight) {
            return LegacyRenderLighting.sampleModelViewLight(modelView, minX, minY, minZ, maxX, maxY, maxZ,
                    packedLight);
        }

        private LegacyRenderLighting.LightProbe sampleLightProbe(Matrix4f modelView, long partIdentityHash,
                int packedLight) {
            return LegacyRenderLighting.sampleModelViewLight(modelView, partIdentityHash, minX, minY, minZ,
                    maxX, maxY, maxZ, packedLight);
        }

        private LegacyRenderLighting.SlicedLightProbe sampleSlicedLightProbe(Matrix4f modelView, long partIdentityHash,
                int packedLight) {
            return LegacyRenderLighting.sampleModelViewSlicedLight(modelView, partIdentityHash, minX, minY, minZ,
                    maxX, maxY, maxZ, packedLight);
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
            if (groups.isEmpty()) {
                return null;
            }
            if (batch == null) {
                batch = PreparedBatch.from(groups, stableBatchKey);
                if (batch.empty()) {
                    CACHE_METRICS.recordSelectionCacheEmptyBuild();
                } else {
                    CACHE_METRICS.recordSelectionCachePreparedBatchBuild();
                }
            }
            return batch.empty() ? null : batch;
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
        private PreparedBatch batch;

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
                CACHE_METRICS.recordSelectionHandleRefresh();
                if (groups.isEmpty()) {
                    batch = PreparedBatch.EMPTY;
                    CACHE_METRICS.recordSelectionHandleEmptyBuild();
                } else {
                    batch = PreparedBatch.from(groups, model.stableSelectionBatchKey(key, groups));
                    if (batch.empty()) {
                        CACHE_METRICS.recordSelectionHandleEmptyBuild();
                        batch = PreparedBatch.EMPTY;
                    } else {
                        CACHE_METRICS.recordSelectionHandlePreparedBatchBuild();
                    }
                }
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
