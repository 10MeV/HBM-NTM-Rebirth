package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.world.level.block.state.BlockState;

public record ObjRenderContext(
        PoseStack poseStack,
        MultiBufferSource buffer,
        BlockState state,
        int packedLight,
        int packedOverlay,
        int color,
        int alpha,
        boolean hasColor,
        boolean legacyShadow,
        LegacyTexturedRenderMode renderMode,
        float uScale,
        float uFromV,
        float vFromU,
        float vScale,
        float uOffset,
        float vOffset,
        float legacyTextureOffset
) {
    public ObjRenderContext(PoseStack poseStack, MultiBufferSource buffer, BlockState state, int packedLight, int packedOverlay) {
        this(poseStack, buffer, state, packedLight, packedOverlay, 0xFFFFFF, 255, false, false,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F);
    }

    public BlockRenderDispatcher blockRenderer() {
        return Minecraft.getInstance().getBlockRenderer();
    }

    public ModelBlockRenderer modelRenderer() {
        return blockRenderer().getModelRenderer();
    }

    public ObjRenderContext withColor(int color) {
        return new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay, color & 0xFFFFFF,
                alpha, true, legacyShadow, renderMode, uScale, uFromV, vFromU, vScale, uOffset, vOffset, legacyTextureOffset);
    }

    public ObjRenderContext withRgb(int rgb) {
        return withColor(rgb);
    }

    public ObjRenderContext withRgb(int red, int green, int blue) {
        return withColor(clampAlpha(red) << 16 | clampAlpha(green) << 8 | clampAlpha(blue));
    }

    public ObjRenderContext withColor(int color, int alpha) {
        int clampedAlpha = clampAlpha(alpha);
        return new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay, color & 0xFFFFFF,
                clampedAlpha, true, legacyShadow, renderMode.withAlpha(clampedAlpha), uScale, uFromV, vFromU, vScale, uOffset, vOffset, legacyTextureOffset);
    }

    public ObjRenderContext withRgba(int red, int green, int blue, int alpha) {
        return withColor(clampAlpha(red) << 16 | clampAlpha(green) << 8 | clampAlpha(blue), alpha);
    }

    public ObjRenderContext withArgb(int argb) {
        return withColor(argb & 0xFFFFFF, argb >>> 24 & 255);
    }

    public ObjRenderContext withColor(float red, float green, float blue) {
        return withColor(red, green, blue, alpha / 255.0F);
    }

    public ObjRenderContext withColor(float red, float green, float blue, float alpha) {
        int color = clampColor(red) << 16 | clampColor(green) << 8 | clampColor(blue);
        return withColor(color, clampColor(alpha));
    }

    public ObjRenderContext withAlpha(int alpha) {
        int clampedAlpha = clampAlpha(alpha);
        return new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay, color,
                clampedAlpha, hasColor, legacyShadow, renderMode.withAlpha(clampedAlpha), uScale, uFromV, vFromU, vScale, uOffset, vOffset, legacyTextureOffset);
    }

    public ObjRenderContext withAlpha(float alpha) {
        return withAlpha(clampColor(alpha));
    }

    public ObjRenderContext clearColor() {
        return new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay, 0xFFFFFF,
                255, false, legacyShadow, renderMode, uScale, uFromV, vFromU, vScale, uOffset, vOffset, legacyTextureOffset);
    }

    public ObjRenderContext fullBright() {
        return new ObjRenderContext(poseStack, buffer, state, LightTexture.FULL_BRIGHT, packedOverlay, color,
                alpha, hasColor, legacyShadow, renderMode, uScale, uFromV, vFromU, vScale, uOffset, vOffset, legacyTextureOffset);
    }

    public ObjRenderContext withLegacyShadow() {
        return new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay, color,
                alpha, hasColor, true, renderMode, uScale, uFromV, vFromU, vScale, uOffset, vOffset, legacyTextureOffset);
    }

    public ObjRenderContext withoutLegacyShadow() {
        return new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay, color,
                alpha, hasColor, false, renderMode, uScale, uFromV, vFromU, vScale, uOffset, vOffset, legacyTextureOffset);
    }

    public boolean translucent() {
        return renderMode.translucent();
    }

    public ObjRenderContext withRenderMode(LegacyTexturedRenderMode renderMode) {
        return new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay, color,
                alpha, hasColor, legacyShadow, renderMode, uScale, uFromV, vFromU, vScale, uOffset, vOffset, legacyTextureOffset);
    }

    public ObjRenderContext withTranslucency() {
        return withRenderMode(LegacyTexturedRenderMode.TRANSLUCENT);
    }

    public ObjRenderContext withTranslucencyNoDepthWrite() {
        return withRenderMode(LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE);
    }

    public ObjRenderContext withTranslucencyDepthWrite() {
        return withRenderMode(LegacyTexturedRenderMode.TRANSLUCENT_DEPTH_WRITE);
    }

    public ObjRenderContext withAdditiveTranslucency() {
        return withRenderMode(LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE);
    }

    public ObjRenderContext withAdditiveTranslucencyDepthWrite() {
        return withRenderMode(LegacyTexturedRenderMode.ADDITIVE_DEPTH_WRITE);
    }

    public ObjRenderContext withGlintTranslucency() {
        return withRenderMode(LegacyTexturedRenderMode.GLINT_NO_DEPTH_WRITE);
    }

    public ObjRenderContext withGlintEqualDepth() {
        return withRenderMode(LegacyTexturedRenderMode.GLINT_EQUAL_DEPTH);
    }

    public ObjRenderContext withoutTranslucency() {
        return withRenderMode(LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    public ObjRenderContext withUvTransform(float uScale, float vScale, float uOffset, float vOffset) {
        return withUvMatrix(uScale, 0.0F, 0.0F, vScale, uOffset, vOffset);
    }

    public ObjRenderContext withUvMatrix(float uScale, float uFromV, float vFromU, float vScale, float uOffset, float vOffset) {
        return new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay, color,
                alpha, hasColor, legacyShadow, renderMode, uScale, uFromV, vFromU, vScale, uOffset, vOffset, legacyTextureOffset);
    }

    public ObjRenderContext withLegacyTextureMatrix(float uScale, float vScale, float uTranslate, float vTranslate) {
        return withLegacyTextureMatrix(uScale, vScale, 0.0F, uTranslate, vTranslate);
    }

    public ObjRenderContext withLegacyTextureMatrix(float uScale, float vScale, float rotationDegrees, float uTranslate, float vTranslate) {
        float radians = (float) Math.toRadians(rotationDegrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        return withUvMatrix(
                uScale * cos,
                -uScale * sin,
                vScale * sin,
                vScale * cos,
                uScale * (cos * uTranslate - sin * vTranslate),
                vScale * (sin * uTranslate + cos * vTranslate));
    }

    public ObjRenderContext withUvScroll(float uOffset, float vOffset) {
        return withUvTransform(1.0F, 1.0F, uOffset, vOffset);
    }

    public ObjRenderContext withLegacyHmfAnimation(float currentTime) {
        return withLegacyHmfAnimation(currentTime, 100000.0D, 5000.0D);
    }

    public ObjRenderContext withLegacyHmfAnimation(float currentTime, double modulo, double quotient) {
        if (quotient == 0.0D) {
            return this;
        }
        return withUvMatrix(uScale, uFromV, vFromU, vScale, uOffset, vOffset + (float) (((double) currentTime % modulo) / quotient));
    }

    public ObjRenderContext withLegacyTextureOffset(float textureOffset) {
        return new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay, color,
                alpha, hasColor, legacyShadow, renderMode, uScale, uFromV, vFromU, vScale, uOffset, vOffset, textureOffset);
    }

    public ObjRenderContext clearUvScroll() {
        return withUvTransform(1.0F, 1.0F, 0.0F, 0.0F);
    }

    public ObjRenderContext clearUvTransform() {
        return new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay, color,
                alpha, hasColor, legacyShadow, renderMode, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F);
    }

    private static int clampAlpha(int alpha) {
        return Math.max(0, Math.min(255, alpha));
    }

    private static int clampColor(float value) {
        return clampAlpha(Math.round(value * 255.0F));
    }
}
