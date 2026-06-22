package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

/**
 * Minimal modern carrier for the old HFRWavefrontObject group rendering path.
 */
public final class LegacyWavefrontModel implements LegacyObjModel {
    private static final Set<LegacyWavefrontModel> ALL_MODELS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final int SELECTION_CACHE_LIMIT = 256;

    private final ResourceLocation modelLocation;
    private final ResourceLocation textureLocation;
    private final Map<String, List<Group>> groupsByName = new LinkedHashMap<>();
    private final List<Group> groupOrder = new ArrayList<>();
    private final Map<SelectionCacheKey, List<Group>> selectionCache = new LinkedHashMap<>();
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

    @Override
    public synchronized LegacyWavefrontModel mixedMode() {
        this.mixedMode = true;
        return this;
    }

    @Override
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

    private synchronized void renderPart(String partName, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
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
            renderGroups(groups, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                    legacyShadow, smoothing, renderMode, uvTransform);
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
            renderGroups(groups, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                    legacyShadow, smoothing, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
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
        renderGroupsWithSprite(groupOrder, sprite, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, false, renderMode, uvTransform);
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
        List<Group> selected = groupsByName.get(normalize(partName));
        if (selected == null || selected.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        LegacyObjTransforms.applyObjUtilRotation(poseStack, yawRadians, pitchRadians, rollRadians);
        Group group = selected.get(selected.size() - 1);
        renderGroupWithSprite(group, sprite, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, true, renderMode, uvTransform);
        poseStack.popPose();
    }

    public synchronized void renderPartWithSprite(String partName, TextureAtlasSprite sprite, ObjRenderContext context,
            float yawRadians, float pitchRadians, float rollRadians) {
        int color = context.hasColor() ? context.color() : 0xFFFFFF;
        renderPartWithSprite(partName, sprite, context.poseStack(), context.buffer(), context.packedLight(), context.packedOverlay(),
                yawRadians, pitchRadians, rollRadians, color >> 16 & 255, color >> 8 & 255, color & 255, context.alpha(),
                context.legacyShadow(), context.renderMode(), uvTransform(context));
    }

    public synchronized void renderWithSprite(TextureAtlasSprite sprite, ObjRenderContext context,
            float yawRadians, float pitchRadians, float rollRadians) {
        int color = context.hasColor() ? context.color() : 0xFFFFFF;
        renderWithSprite(sprite, context.poseStack(), context.buffer(), context.packedLight(), context.packedOverlay(),
                yawRadians, pitchRadians, rollRadians, color >> 16 & 255, color >> 8 & 255, color & 255, context.alpha(),
                context.legacyShadow(), context.renderMode(), uvTransform(context));
    }

    @Override
    public synchronized void renderPart(String name, ObjRenderContext context) {
        renderPart(name, textureLocation, context);
    }

    public synchronized void renderPart(String name, ResourceLocation textureLocation, ObjRenderContext context) {
        int color = context.hasColor() ? context.color() : 0xFFFFFF;
        renderPart(name, textureLocation, context.poseStack(), context.buffer(), context.packedLight(), context.packedOverlay(),
                color >> 16 & 255, color >> 8 & 255, color & 255, context.alpha(), context.legacyShadow(),
                context.renderMode(), uvTransform(context));
    }

    public synchronized void renderPartClipped(String name, ResourceLocation textureLocation, ObjRenderContext context,
            double clipX, double clipY, double clipZ, double clipD) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        List<Group> groups = groupsByName.get(normalize(name));
        boolean rendered = groups != null && !groups.isEmpty();
        if (rendered) {
            int color = context.hasColor() ? context.color() : 0xFFFFFF;
            for (Group group : groups) {
                renderGroupClipped(group, textureLocation, context.poseStack(), context.buffer(),
                        context.packedLight(), context.packedOverlay(), color >> 16 & 255, color >> 8 & 255,
                        color & 255, context.alpha(), context.legacyShadow(), smoothing, context.renderMode(),
                        uvTransform(context), clipX, clipY, clipZ, clipD);
            }
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

    private synchronized void renderPartUntextured(String partName, PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        List<Group> groups = groupsByName.get(normalize(partName));
        boolean rendered = groups != null && !groups.isEmpty();
        if (rendered) {
            renderGroupsUntextured(groups, poseStack, buffer, red, green, blue, alpha, renderMode);
        }
        if (!rendered) {
            warnMissingPart(partName);
        }
    }

    public synchronized void renderPartUntextured(String partName, ObjRenderContext context) {
        int color = context.hasColor() ? context.color() : 0xFFFFFF;
        renderPartUntextured(partName, context.poseStack(), context.buffer(),
                color >> 16 & 255, color >> 8 & 255, color & 255, context.alpha(), context.renderMode());
    }

    public synchronized void renderPartUntexturedAdditive(String partName, ObjRenderContext context) {
        int color = context.hasColor() ? context.color() : 0xFFFFFF;
        renderPartUntextured(partName, context.poseStack(), context.buffer(),
                color >> 16 & 255, color >> 8 & 255, color & 255, context.alpha(), true);
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

    @Override
    public synchronized void renderAll(ObjRenderContext context) {
        renderAll(textureLocation, context);
    }

    public synchronized void renderAll(ResourceLocation textureLocation, ObjRenderContext context) {
        int color = context.hasColor() ? context.color() : 0xFFFFFF;
        renderAll(textureLocation, context.poseStack(), context.buffer(), context.packedLight(), context.packedOverlay(),
                color >> 16 & 255, color >> 8 & 255, color & 255, context.alpha(), context.legacyShadow(),
                context.renderMode(), uvTransform(context));
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
        renderGroupsUntextured(groupOrder, poseStack, buffer, red, green, blue, alpha, renderMode);
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

    public synchronized void renderAllUntextured(ObjRenderContext context) {
        int color = context.hasColor() ? context.color() : 0xFFFFFF;
        renderAllUntextured(context.poseStack(), context.buffer(), color >> 16 & 255, color >> 8 & 255, color & 255, context.alpha(),
                context.renderMode());
    }

    public synchronized void renderAllUntexturedAdditive(ObjRenderContext context) {
        int color = context.hasColor() ? context.color() : 0xFFFFFF;
        renderAllUntextured(context.poseStack(), context.buffer(), color >> 16 & 255, color >> 8 & 255, color & 255, context.alpha(), true);
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
        renderGroups(selectedGroups(SelectionCacheMode.ONLY, groupNames), textureLocation, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, false, smoothing,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
        warnMissingNamedGroups(groupNames);
    }

    @Override
    public synchronized void renderOnly(ObjRenderContext context, String... names) {
        renderOnly(textureLocation, context, names);
    }

    public synchronized void renderOnly(ResourceLocation textureLocation, ObjRenderContext context, String... names) {
        int color = context.hasColor() ? context.color() : 0xFFFFFF;
        renderOnly(textureLocation, context.poseStack(), context.buffer(), context.packedLight(), context.packedOverlay(),
                color >> 16 & 255, color >> 8 & 255, color & 255, context.alpha(), context.legacyShadow(),
                context.renderMode(), uvTransform(context), names);
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

    private synchronized void renderOnlyUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode, String... groupNames) {
        rejectMixedModeDirectRender();
        ensureLoaded();
        if (failed) {
            return;
        }
        renderGroupsUntextured(selectedGroups(SelectionCacheMode.ONLY, groupNames), poseStack, buffer, red, green, blue, alpha, renderMode);
        warnMissingNamedGroups(groupNames);
    }

    public synchronized void renderOnlyUntextured(ObjRenderContext context, String... names) {
        int color = context.hasColor() ? context.color() : 0xFFFFFF;
        renderOnlyUntextured(context.poseStack(), context.buffer(), color >> 16 & 255, color >> 8 & 255, color & 255, context.alpha(),
                context.renderMode(), names);
    }

    public synchronized void renderOnlyUntexturedAdditive(ObjRenderContext context, String... names) {
        int color = context.hasColor() ? context.color() : 0xFFFFFF;
        renderOnlyUntextured(context.poseStack(), context.buffer(), color >> 16 & 255, color >> 8 & 255, color & 255, context.alpha(), true, names);
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
        renderGroups(selectedGroups(SelectionCacheMode.ONLY, groupNames), textureLocation, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
        warnMissingNamedGroups(groupNames);
    }

    @Override
    public synchronized void renderOnlyInCallOrder(ObjRenderContext context, String... names) {
        renderOnlyInCallOrder(textureLocation, context, names);
    }

    public synchronized void renderOnlyInCallOrder(ResourceLocation textureLocation, ObjRenderContext context, String... names) {
        int color = context.hasColor() ? context.color() : 0xFFFFFF;
        renderOnlyInCallOrder(textureLocation, context.poseStack(), context.buffer(), context.packedLight(), context.packedOverlay(),
                color >> 16 & 255, color >> 8 & 255, color & 255, context.alpha(), context.legacyShadow(),
                context.renderMode(), uvTransform(context), names);
    }

    public SelectionHandle prepareRenderOnlyInCallOrder(String... groupNames) {
        return new SelectionHandle(SelectionCacheMode.CALL_ORDER, requestedNames(groupNames), normalizedList(groupNames),
                groupNames == null ? new String[0] : groupNames.clone());
    }

    public SelectionHandle prepareRenderOnly(String... groupNames) {
        return new SelectionHandle(SelectionCacheMode.ONLY, requestedNames(groupNames), normalizedList(groupNames),
                groupNames == null ? new String[0] : groupNames.clone());
    }

    public synchronized void renderOnlyInCallOrder(ResourceLocation textureLocation, ObjRenderContext context,
            SelectionHandle selection) {
        int color = context.hasColor() ? context.color() : 0xFFFFFF;
        renderSelection(textureLocation, context.poseStack(), context.buffer(), context.packedLight(),
                context.packedOverlay(), color >> 16 & 255, color >> 8 & 255, color & 255, context.alpha(),
                context.legacyShadow(), context.renderMode(), uvTransform(context), selection);
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
        renderGroups(selectedGroups(SelectionCacheMode.CALL_ORDER, groupNames), textureLocation, poseStack, buffer,
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
        renderGroups(selectedGroups(SelectionCacheMode.ALL_EXCEPT, excludedGroupNames), textureLocation, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, false, smoothing,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    @Override
    public synchronized void renderAllExcept(ObjRenderContext context, String... excludedNames) {
        renderAllExcept(textureLocation, context, excludedNames);
    }

    public synchronized void renderAllExcept(ResourceLocation textureLocation, ObjRenderContext context, String... excludedNames) {
        int color = context.hasColor() ? context.color() : 0xFFFFFF;
        renderAllExcept(textureLocation, context.poseStack(), context.buffer(), context.packedLight(), context.packedOverlay(),
                color >> 16 & 255, color >> 8 & 255, color & 255, context.alpha(), context.legacyShadow(),
                context.renderMode(), uvTransform(context), excludedNames);
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
        renderGroups(selectedGroups(SelectionCacheMode.ALL_EXCEPT, excludedGroupNames), textureLocation, poseStack, buffer,
                packedLight, packedOverlay, red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
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
        renderGroups(selectedGroups(SelectionCacheMode.ONLY, groupNames), textureLocation, poseStack, buffer,
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
            renderGroups(groups, textureLocation, poseStack, buffer, packedLight, packedOverlay,
                    255, 255, 255, 255, false, smoothing, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
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
        renderGroups(selectedGroups(SelectionCacheMode.ALL_EXCEPT, excludedGroupNames), textureLocation, poseStack, buffer,
                packedLight, packedOverlay, 255, 255, 255, 255, false, smoothing,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    @Override
    public synchronized List<String> getPartNames() {
        ensureLoaded();
        if (failed) {
            return List.of();
        }
        return Collections.unmodifiableList(groupOrder.stream().map(Group::name).toList());
    }

    @Override
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
        for (LegacyWavefrontModel model : models) {
            model.reload(resourceManager);
        }
    }

    private static void renderGroup(Group group, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing) {
        renderGroup(group, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, smoothing, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    private static void renderGroup(Group group, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, boolean smoothing, boolean translucent) {
        renderGroup(group, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, smoothing, renderMode(translucent), UvTransform.DEFAULT);
    }

    private static void renderGroup(Group group, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean smoothing, boolean translucent, float uOffset, float vOffset) {
        renderGroup(group, textureLocation, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, smoothing, renderMode(translucent), uvTransform(uOffset, vOffset));
    }

    private static void renderGroup(Group group, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
        renderGroups(List.of(group), textureLocation, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, legacyShadow, smoothing, renderMode, uvTransform);
    }

    private static void renderGroups(List<Group> groups, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
        if (groups.isEmpty()) {
            return;
        }
        VertexConsumer quadConsumer = null;
        VertexConsumer triangleConsumer = null;
        PoseStack.Pose pose = poseStack.last();
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, normal).withAlpha(alpha);
        for (Group group : groups) {
            List<PreparedVertex> quadVertices = group.quadVertices();
            if (!quadVertices.isEmpty()) {
                if (quadConsumer == null) {
                    quadConsumer = buffer.getBuffer(alphaMode.renderType(textureLocation, VertexFormat.Mode.QUADS));
                }
                emitPreparedVertices(quadVertices, quadConsumer, position, normal, packedLight, packedOverlay,
                        red, green, blue, alpha, legacyShadow, smoothing, uvTransform);
            }
            List<PreparedVertex> triangleVertices = group.triangleVertices();
            if (!triangleVertices.isEmpty()) {
                if (triangleConsumer == null) {
                    triangleConsumer = buffer.getBuffer(alphaMode.renderType(textureLocation, VertexFormat.Mode.TRIANGLES));
                }
                emitPreparedVertices(triangleVertices, triangleConsumer, position, normal, packedLight, packedOverlay,
                        red, green, blue, alpha, legacyShadow, smoothing, uvTransform);
            }
        }
    }

    private static void renderPreparedBatch(PreparedBatch batch, ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
        if (batch.empty()) {
            return;
        }
        VertexConsumer quadConsumer = null;
        VertexConsumer triangleConsumer = null;
        PoseStack.Pose pose = poseStack.last();
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, normal).withAlpha(alpha);
        List<PreparedVertex> quadVertices = batch.quadVertices();
        if (!quadVertices.isEmpty()) {
            if (quadConsumer == null) {
                quadConsumer = buffer.getBuffer(alphaMode.renderType(textureLocation, VertexFormat.Mode.QUADS));
            }
            emitPreparedVertices(quadVertices, quadConsumer, position, normal, packedLight, packedOverlay,
                    red, green, blue, alpha, legacyShadow, smoothing, uvTransform);
        }
        List<PreparedVertex> triangleVertices = batch.triangleVertices();
        if (!triangleVertices.isEmpty()) {
            if (triangleConsumer == null) {
                triangleConsumer = buffer.getBuffer(alphaMode.renderType(textureLocation, VertexFormat.Mode.TRIANGLES));
            }
            emitPreparedVertices(triangleVertices, triangleConsumer, position, normal, packedLight, packedOverlay,
                    red, green, blue, alpha, legacyShadow, smoothing, uvTransform);
        }
    }

    private static void renderGroupClipped(Group group, ResourceLocation textureLocation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, boolean smoothing, LegacyTexturedRenderMode renderMode, UvTransform uvTransform,
            double clipX, double clipY, double clipZ, double clipD) {
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
            }
        }
    }

    private static void renderGroupWithSprite(Group group, TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow, boolean partBrightness) {
        renderGroupWithSprite(group, sprite, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                legacyShadow, partBrightness, LegacyTexturedRenderMode.CUTOUT_NO_CULL, UvTransform.DEFAULT);
    }

    private static void renderGroupWithSprite(Group group, TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean partBrightness, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
        renderGroupsWithSprite(List.of(group), sprite, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, legacyShadow, partBrightness, renderMode, uvTransform);
    }

    private static void renderGroupsWithSprite(List<Group> groups, TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            boolean partBrightness, LegacyTexturedRenderMode renderMode, UvTransform uvTransform) {
        if (groups.isEmpty()) {
            return;
        }
        VertexConsumer quadConsumer = null;
        VertexConsumer triangleConsumer = null;
        PoseStack.Pose pose = poseStack.last();
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        LegacyTexturedRenderMode alphaMode = renderModeForPose(renderMode, normal).withAlpha(alpha);
        for (Group group : groups) {
            List<PreparedVertex> quadVertices = group.quadVertices();
            if (!quadVertices.isEmpty()) {
                if (quadConsumer == null) {
                    quadConsumer = buffer.getBuffer(alphaMode.renderType(InventoryMenu.BLOCK_ATLAS, VertexFormat.Mode.QUADS));
                }
                emitPreparedVerticesWithSprite(quadVertices, sprite, quadConsumer, position, normal, packedLight,
                        packedOverlay, red, green, blue, alpha, legacyShadow, partBrightness, uvTransform);
            }
            List<PreparedVertex> triangleVertices = group.triangleVertices();
            if (!triangleVertices.isEmpty()) {
                if (triangleConsumer == null) {
                    triangleConsumer = buffer.getBuffer(alphaMode.renderType(InventoryMenu.BLOCK_ATLAS, VertexFormat.Mode.TRIANGLES));
                }
                emitPreparedVerticesWithSprite(triangleVertices, sprite, triangleConsumer, position, normal, packedLight,
                        packedOverlay, red, green, blue, alpha, legacyShadow, partBrightness, uvTransform);
            }
        }
    }

    private static void renderGroupUntextured(Group group, PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, boolean additive) {
        renderGroupUntextured(group, poseStack, buffer, red, green, blue, alpha,
                additive ? LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE : LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    private static void renderGroupUntextured(Group group, PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
        renderGroupsUntextured(List.of(group), poseStack, buffer, red, green, blue, alpha, renderMode);
    }

    private static void renderGroupsUntextured(List<Group> groups, PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
        if (groups.isEmpty()) {
            return;
        }
        PoseStack.Pose pose = poseStack.last();
        Matrix4f position = pose.pose();
        LegacyTexturedRenderMode resolvedRenderMode = renderModeForPose(renderMode, pose.normal());
        VertexConsumer quadConsumer = null;
        VertexConsumer triangleConsumer = null;
        for (Group group : groups) {
            List<PreparedVertex> quadVertices = group.quadVertices();
            if (!quadVertices.isEmpty()) {
                if (quadConsumer == null) {
                    quadConsumer = buffer.getBuffer(LegacyUntexturedQuadRenderer.type(resolvedRenderMode, alpha, VertexFormat.Mode.QUADS));
                }
                emitPreparedVerticesUntextured(quadVertices, quadConsumer, position, red, green, blue, alpha);
            }
            List<PreparedVertex> triangleVertices = group.triangleVertices();
            if (!triangleVertices.isEmpty()) {
                if (triangleConsumer == null) {
                    triangleConsumer = buffer.getBuffer(LegacyUntexturedQuadRenderer.type(resolvedRenderMode, alpha, VertexFormat.Mode.TRIANGLES));
                }
                emitPreparedVerticesUntextured(triangleVertices, triangleConsumer, position, red, green, blue, alpha);
            }
        }
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
        allPreparedBatch = PreparedBatch.from(groupOrder);
    }

    private PreparedBatch allPreparedBatch() {
        if (allPreparedBatch == null) {
            allPreparedBatch = PreparedBatch.from(groupOrder);
        }
        return allPreparedBatch;
    }

    private List<Group> selectedGroups(SelectionCacheMode mode, String... names) {
        SelectionCacheKey key = new SelectionCacheKey(mode, normalizedList(names));
        List<Group> cached = selectionCache.get(key);
        if (cached != null) {
            return cached;
        }
        List<Group> selected = createSelectedGroups(key);
        if (selectionCache.size() >= SELECTION_CACHE_LIMIT) {
            selectionCache.clear();
        }
        selectionCache.put(key, selected);
        return selected;
    }

    private List<Group> createSelectedGroups(SelectionCacheKey key) {
        return switch (key.mode()) {
            case ONLY -> selectedOnly(new LinkedHashSet<>(key.names()));
            case ALL_EXCEPT -> selectedAllExcept(new LinkedHashSet<>(key.names()));
            case CALL_ORDER -> selectedInCallOrder(key.names());
        };
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

    private static UvTransform uvTransform(ObjRenderContext context) {
        if (context.uScale() == 1.0F && context.uFromV() == 0.0F && context.vFromU() == 0.0F
                && context.vScale() == 1.0F && context.uOffset() == 0.0F && context.vOffset() == 0.0F
                && context.legacyTextureOffset() == 0.0F) {
            return UvTransform.DEFAULT;
        }
        return new UvTransform(context.uScale(), context.uFromV(), context.vFromU(), context.vScale(),
                context.uOffset(), context.vOffset(), context.legacyTextureOffset());
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
                0.0F);
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
        return selected;
    }

    private List<Group> selectedAllExcept(Set<String> excluded) {
        List<Group> selected = new ArrayList<>();
        for (Group group : groupOrder) {
            if (!excluded.contains(normalize(group.name()))) {
                selected.add(group);
            }
        }
        return selected;
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

    private record PreparedBatch(List<PreparedVertex> quadVertices, List<PreparedVertex> triangleVertices) {
        private static final PreparedBatch EMPTY = new PreparedBatch(List.of(), List.of());

        private static PreparedBatch from(List<Group> groups) {
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
            return new PreparedBatch(List.copyOf(quads), List.copyOf(triangles));
        }

        private boolean empty() {
            return quadVertices.isEmpty() && triangleVertices.isEmpty();
        }
    }

    private enum SelectionCacheMode {
        ONLY,
        ALL_EXCEPT,
        CALL_ORDER
    }

    private record SelectionCacheKey(SelectionCacheMode mode, List<String> names) {
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
                groups = model.createSelectedGroups(new SelectionCacheKey(mode, normalizedNames));
                batch = PreparedBatch.from(groups);
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
            float uOffset, float vOffset, float textureOffset) {
        public static final UvTransform DEFAULT = new UvTransform(1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F);
    }

    private record UV(float u, float v) {
        private static final UV ZERO = new UV(0.0F, 0.0F);
    }
}
