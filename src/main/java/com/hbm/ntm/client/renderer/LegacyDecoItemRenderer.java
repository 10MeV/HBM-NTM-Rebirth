package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyUvAnimation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared plans from the old RenderDecoItem TESR item renderer.
 */
public final class LegacyDecoItemRenderer {
    public static final ResourceLocation VANILLA_ITEM_GLINT =
            new ResourceLocation("minecraft", "textures/misc/enchanted_item_glint.png");
    public static final int GLINT_COLOR = 0x8040CC;
    public static final float BLOCK_ALPHA_THRESHOLD_TRANSLUCENT = 0.1F;
    public static final float BLOCK_ALPHA_THRESHOLD_SOLID = 0.5F;
    public static final float BLOCK_ALPHA_THRESHOLD_RESET = 0.1F;
    public static final float POLYGON_OFFSET_FACTOR = -1.0F;
    public static final float POLYGON_OFFSET_UNITS = -1.0F;

    public static boolean shouldUseBlockGuiPath(boolean itemSpriteIsBlock, boolean blockRendersIn3d) {
        return itemSpriteIsBlock && blockRendersIn3d;
    }

    public static RenderPassState renderPassState(int blockRenderPass) {
        boolean translucent = blockRenderPass != 0;
        return new RenderPassState(translucent,
                translucent ? BLOCK_ALPHA_THRESHOLD_TRANSLUCENT : BLOCK_ALPHA_THRESHOLD_SOLID,
                BLOCK_ALPHA_THRESHOLD_RESET);
    }

    public static GuiBlockTransform guiBlockTransform(int x, int y, float zLevel) {
        return new GuiBlockTransform(x - 2.0F, y + 3.0F, zLevel - 3.0F,
                10.0F, 10.0F, 10.0F,
                1.0F, 0.5F, 1.0F,
                1.0F, 1.0F, -1.0F,
                210.0F, 45.0F, -90.0F);
    }

    public static List<FlatGlintPass> flatGlintPasses(int x, int y, int width, int height, long currentMillis) {
        List<FlatGlintPass> passes = new ArrayList<>(2);
        for (int pass = 0; pass < 2; pass++) {
            double period = 3000.0D + pass * 1873.0D;
            double animation = LegacyUvAnimation.wrappedFraction(currentMillis, period) * 256.0D;
            double horizontalShear = pass == 0 ? 4.0D : -1.0D;
            LegacyUvAnimation.UnitQuadUv uv = LegacyUvAnimation.flatItemGlintUv(currentMillis, pass, width, height);
            passes.add(new FlatGlintPass(pass, x, y, width, height, animation, horizontalShear, uv));
        }
        return List.copyOf(passes);
    }

    public static void renderFlatGlint(GuiGraphics graphics, int x, int y, int width, int height, long currentMillis) {
        for (FlatGlintPass pass : flatGlintPasses(x, y, width, height, currentMillis)) {
            LegacyScreenQuadRenderer.shearedPixelQuad(VANILLA_ITEM_GLINT, graphics,
                    pass.x(), pass.y(), pass.animationPixels(), 0.0D, pass.width(), pass.height(),
                    256.0D, 256.0D, pass.horizontalShear(), GLINT_COLOR, 255,
                    LegacyScreenQuadRenderer.BlendMode.GLINT);
        }
    }

    public record RenderPassState(boolean translucent, float alphaThreshold, float resetAlphaThreshold) {
    }

    public record GuiBlockTransform(float translateX, float translateY, float translateZ,
            float scaleX, float scaleY, float scaleZ,
            float secondTranslateX, float secondTranslateY, float secondTranslateZ,
            float secondScaleX, float secondScaleY, float secondScaleZ,
            float rotateXDegrees, float rotateYDegrees, float finalRotateYDegrees) {
    }

    public record FlatGlintPass(int pass, int x, int y, int width, int height,
            double animationPixels, double horizontalShear, LegacyUvAnimation.UnitQuadUv uv) {
    }

    private LegacyDecoItemRenderer() {
    }
}
