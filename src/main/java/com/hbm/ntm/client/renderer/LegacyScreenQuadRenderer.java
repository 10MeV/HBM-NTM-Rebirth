package com.hbm.ntm.client.renderer;

import com.hbm.ntm.api.item.Crosshair;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class LegacyScreenQuadRenderer {
    public static final ResourceLocation VANILLA_ITEM_GLINT =
            new ResourceLocation("minecraft", "textures/misc/enchanted_item_glint.png");

    public static void blit(ResourceLocation texture, GuiGraphics graphics,
            int x, int y, int u, int v, int width, int height) {
        graphics.blit(texture, x, y, u, v, width, height);
    }

    public static void blit(ResourceLocation texture, GuiGraphics graphics,
            int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        graphics.blit(texture, x, y, (float) u, (float) v, width, height, textureWidth, textureHeight);
    }

    public static void blitColored(ResourceLocation texture, GuiGraphics graphics,
            int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight,
            float red, float green, float blue, float alpha) {
        graphics.setColor(red, green, blue, alpha);
        graphics.blit(texture, x, y, (float) u, (float) v, width, height, textureWidth, textureHeight);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void pixelQuad(ResourceLocation texture, GuiGraphics graphics,
            double x, double y, double u, double v, double width, double height,
            double textureWidth, double textureHeight, int color, int alpha, BlendMode blendMode) {
        if (textureWidth == 0.0D || textureHeight == 0.0D) {
            return;
        }
        unitQuad(texture, graphics, x, y, width, height,
                u / textureWidth, (v + height) / textureHeight,
                (u + width) / textureWidth, (v + height) / textureHeight,
                (u + width) / textureWidth, v / textureHeight,
                u / textureWidth, v / textureHeight,
                color, alpha, blendMode);
    }

    public static int scaled(double current, double max, int scale) {
        if (max <= 0.0D || scale <= 0) {
            return 0;
        }
        return Math.max(0, (int) Math.min(current / max * scale, scale));
    }

    public static void blitHorizontalProgress(ResourceLocation texture, GuiGraphics graphics,
            int x, int y, int u, int v, int width, int height, int progressWidth) {
        int clampedWidth = Math.max(0, Math.min(width, progressWidth));
        if (clampedWidth <= 0 || height <= 0) {
            return;
        }
        graphics.blit(texture, x, y, u, v, clampedWidth, height);
    }

    public static int blitHorizontalScaled(ResourceLocation texture, GuiGraphics graphics,
            int x, int y, int u, int v, int width, int height, double current, double max) {
        int progress = scaled(current, max, width);
        blitHorizontalProgress(texture, graphics, x, y, u, v, width, height, progress);
        return progress;
    }

    public static RadCounterPlan radCounterPlan(int screenHeight, int horizontalOffset, int verticalOffset,
            double radiation, double radiationRate) {
        int barLength = 74;
        int x = 16 + horizontalOffset;
        int y = screenHeight - 20 - verticalOffset;
        int bar = scaled(radiation, 1000.0D, barLength);
        RadWarningPlan warning = radWarningPlan(x + barLength + 2, y - 18, radiationRate);
        String label = "";
        if (radiationRate > 1000.0D) {
            label = ">1000 RAD/s";
        } else if (radiationRate >= 1.0D) {
            label = Math.round(radiationRate) + " RAD/s";
        } else if (radiationRate > 0.0D) {
            label = "<1 RAD/s";
        }
        return new RadCounterPlan(
                new ScreenRect(x, y, 94, 18),
                new TextureRect(0, 0, 94, 18),
                new ScreenRect(x + 1, y + 1, bar, 16),
                new TextureRect(1, 19, bar, 16),
                warning,
                label,
                x,
                y - 8,
                0xFF0000);
    }

    public static RadWarningPlan radWarningPlan(int x, int y, double radiationRate) {
        if (radiationRate >= 25.0D) {
            return new RadWarningPlan(true, new ScreenRect(x, y, 18, 18), new TextureRect(36, 36, 18, 18));
        }
        if (radiationRate >= 10.0D) {
            return new RadWarningPlan(true, new ScreenRect(x, y, 18, 18), new TextureRect(18, 36, 18, 18));
        }
        if (radiationRate >= 2.5D) {
            return new RadWarningPlan(true, new ScreenRect(x, y, 18, 18), new TextureRect(0, 36, 18, 18));
        }
        return RadWarningPlan.hidden();
    }

    public static void scope16x9(ResourceLocation texture, GuiGraphics graphics, int screenWidth, int screenHeight) {
        ScopeUvPlan plan = scope16x9Plan(screenWidth, screenHeight);
        unitQuad(texture, graphics, 0, 0, screenWidth, screenHeight,
                plan.uMin(), plan.vMin(), plan.uMax(), plan.vMax(),
                0xFFFFFF, 255, BlendMode.NORMAL);
    }

    public static ScopeUvPlan scope16x9Plan(int screenWidth, int screenHeight) {
        double width = screenWidth;
        double height = screenHeight;
        double smallest = Math.min(width, height);
        double divisor = smallest / (9.0D / 16.0D);
        smallest = 9.0D / 16.0D;
        double largest = Math.max(width, height) / divisor;

        double vMin = height < width ? 0.5D - smallest / 2.0D : 0.5D - largest / 2.0D;
        double vMax = height < width ? 0.5D + smallest / 2.0D : 0.5D + largest / 2.0D;
        double uMin = width < height ? 0.5D - smallest / 2.0D : 0.5D - largest / 2.0D;
        double uMax = width < height ? 0.5D + smallest / 2.0D : 0.5D + largest / 2.0D;

        return new ScopeUvPlan(uMin, vMin, uMax, vMax, -300.0D);
    }

    public static CrosshairPlan crosshairPlan(int screenWidth, int screenHeight, Crosshair crosshair) {
        if (crosshair == null || crosshair == Crosshair.NONE) {
            return CrosshairPlan.hidden();
        }
        int size = crosshair.size();
        return new CrosshairPlan(true, screenWidth / 2 - size / 2, screenHeight / 2 - size / 2,
                crosshair.textureX(), crosshair.textureY(), size);
    }

    public static void renderCrosshair(ResourceLocation texture, GuiGraphics graphics,
            int screenWidth, int screenHeight, Crosshair crosshair) {
        CrosshairPlan plan = crosshairPlan(screenWidth, screenHeight, crosshair);
        if (!plan.visible()) {
            return;
        }
        pixelQuad(texture, graphics, plan.x(), plan.y(), plan.u(), plan.v(), plan.size(), plan.size(),
                256.0D, 256.0D, 0xFFFFFF, 255, BlendMode.INVERT_CROSSHAIR);
    }

    public static StingerLockonPlan stingerLockonPlan(int screenWidth, int screenHeight, float lockon) {
        int progress = Mth.clamp((int) (lockon * 28.0F), 0, 28);
        int x = screenWidth / 2 - 15;
        int y = screenHeight / 2 + 18;
        return new StingerLockonPlan(
                new ScreenRect(x, y, 30, 10),
                new TextureRect(146, 18, 30, 10),
                new ScreenRect(x + 1, y + 1, progress, 8),
                new TextureRect(147, 29, progress, 8));
    }

    public static void renderStingerLockon(ResourceLocation texture, GuiGraphics graphics,
            int screenWidth, int screenHeight, float lockon) {
        StingerLockonPlan plan = stingerLockonPlan(screenWidth, screenHeight, lockon);
        blit(texture, graphics, plan.frame().x(), plan.frame().y(), plan.frameTexture().u(), plan.frameTexture().v(),
                plan.frame().width(), plan.frame().height());
        if (plan.progress().width() > 0) {
            blit(texture, graphics, plan.progress().x(), plan.progress().y(),
                    plan.progressTexture().u(), plan.progressTexture().v(),
                    plan.progress().width(), plan.progress().height());
        }
    }

    public static AmmoHudPlan ammoHudPlan(int screenWidth, int screenHeight, int durabilityLoss, boolean renderCount) {
        int x = screenWidth / 2 + 62 + 36;
        int y = screenHeight - 21;
        int progress = Mth.clamp(50 - durabilityLoss, 0, 50);
        return new AmmoHudPlan(new ScreenRect(x, y, 16, 16),
                new ScreenRect(x, y + 16, 52, 3),
                new TextureRect(94, 0, 52, 3),
                new ScreenRect(x + 1, y + 16, progress, 3),
                new TextureRect(95, 3, progress, 3),
                x + 16, y + 6, renderCount);
    }

    public static AmmoAltHudPlan ammoAltHudPlan(int screenWidth, int screenHeight) {
        int x = screenWidth / 2 + 62 + 36 + 18;
        int y = screenHeight - 21 - 16;
        return new AmmoAltHudPlan(new ScreenRect(x, y, 16, 16), x + 16, y + 6);
    }

    public static ItemStackPopPlan itemStackPopPlan(int x, int y, float animationsToGo, float partialTick, boolean renderEffect) {
        float pop = animationsToGo - partialTick;
        if (pop <= 0.0F) {
            return new ItemStackPopPlan(x, y, renderEffect, false, x + 8.0F, y + 12.0F, 1.0F, 1.0F, true);
        }
        float scale = 1.0F + pop / 5.0F;
        return new ItemStackPopPlan(x, y, renderEffect, true, x + 8.0F, y + 12.0F,
                1.0F / scale, (scale + 1.0F) / 2.0F, true);
    }

    public static DashLayout dashLayout(int screenHeight) {
        return new DashLayout(16, screenHeight - 40 - 2, 30, 10, 2, 12);
    }

    public static List<DashBarSegment> dashBarSegments(int screenHeight, int stamina, int dashCount) {
        if (dashCount <= 0) {
            return List.of();
        }
        DashLayout layout = dashLayout(screenHeight);
        int safeStamina = Math.max(0, stamina);
        int staminaDiv = safeStamina / layout.barWidth();
        int staminaMod = safeStamina % layout.barWidth();
        List<DashBarSegment> segments = new ArrayList<>(dashCount);
        for (int barId = 0; barId < dashCount; barId++) {
            int column = barId % 3;
            int row = barId / 3;
            int x = layout.x() + (layout.barWidth() + layout.gap()) * column;
            int y = layout.y() - layout.rowHeight() * row;
            int status = 1;
            int fillWidth = layout.barWidth();
            if (staminaDiv < barId) {
                status = 3;
            } else if (staminaDiv == barId) {
                status = barId == 0 ? 0 : 2;
                fillWidth = (int) (staminaMod * (layout.barWidth() / 30.0F));
            }
            segments.add(new DashBarSegment(barId, x, y, status, Mth.clamp(fillWidth, 0, layout.barWidth()),
                    staminaDiv == barId && staminaMod >= 27));
        }
        return List.copyOf(segments);
    }

    public static DashFadePlan dashFadePlan(int screenHeight, int stamina, int dashCount) {
        if (dashCount <= 0) {
            return DashFadePlan.hidden();
        }
        DashLayout layout = dashLayout(screenHeight);
        int safeStamina = Math.max(0, stamina);
        int staminaDiv = safeStamina / layout.barWidth();
        int staminaMod = safeStamina % layout.barWidth();
        int previousBar = staminaDiv - 1;
        if (previousBar < 0) {
            return DashFadePlan.hidden();
        }
        int fadeBar = staminaMod >= 25 ? previousBar + 1 : previousBar;
        if (fadeBar < 0 || fadeBar >= dashCount) {
            return DashFadePlan.hidden();
        }
        int column = fadeBar % 3;
        int row = fadeBar / 3;
        return new DashFadePlan(true, layout.x() + (layout.barWidth() + layout.gap()) * column,
                layout.y() - layout.rowHeight() * row, fadeBar);
    }

    public static ShieldBarPlan shieldBarPlan(int screenWidth, int screenHeight, int leftHeight,
            float shield, float maxShield) {
        int left = screenWidth / 2 - 91;
        int top = screenHeight - leftHeight;
        int fill = maxShield <= 0.0F ? 0 : Mth.clamp((int) Math.ceil(shield * 79.0F / maxShield), 0, 79);
        String label = "" + ((int) (shield * 10.0F)) / 10.0D;
        return new ShieldBarPlan(new ScreenRect(left, top, 81, 9), new TextureRect(146, 0, 81, 9),
                new ScreenRect(left + 1, top, fill, 9), new TextureRect(147, 9, fill, 9),
                label, left + 40, top + 1, 10);
    }

    public static OverlayStatePlan legacyOverlayStatePlan() {
        return new OverlayStatePlan(true, false, false, false,
                LegacyTexturedRenderMode.BlendFunction.NORMAL_ALPHA, 1.0F, 1.0F, 1.0F, 1.0F,
                true, true, true, true);
    }

    public static BadgeHudPlan badgeHudPlan(boolean true528, boolean trueExp, boolean trueRam) {
        int offsetX = 2;
        int offsetY = 2;
        int stepWidth = 26;
        List<BadgePlan> badges = new ArrayList<>(4);
        if (true528) {
            badges.add(new BadgePlan(BadgeKind.TRUE_528, new ScreenRect(offsetX, offsetY, 24, 8),
                    new TextureRect(0, 218, 24, 8)));
            offsetX += stepWidth;
        }
        if (trueExp) {
            badges.add(new BadgePlan(BadgeKind.TRUE_EXP, new ScreenRect(offsetX, offsetY, 24, 8),
                    new TextureRect(0, 226, 24, 8)));
            offsetX += stepWidth;
        }
        if (trueRam) {
            badges.add(new BadgePlan(BadgeKind.TRUE_RAM, new ScreenRect(offsetX, offsetY, 24, 8),
                    new TextureRect(0, 234, 24, 8)));
            offsetX += stepWidth;
        }
        if (true528 && trueExp && trueRam) {
            badges.add(new BadgePlan(BadgeKind.TRUE_328, new ScreenRect(offsetX, offsetY, 24, 8),
                    new TextureRect(0, 242, 24, 8)));
        }
        return new BadgeHudPlan(2, 2, stepWidth, List.copyOf(badges), legacyOverlayStatePlan());
    }

    public static void itemGlint(GuiGraphics graphics, int x, int y) {
        itemGlint(graphics, x, y, 16, 16);
    }

    public static void itemGlint(GuiGraphics graphics, int x, int y, int width, int height) {
        itemGlint(graphics, x, y, width, height, 0x8040CC);
    }

    public static void itemGlint(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        itemGlint(graphics, x, y, width, height, color, System.currentTimeMillis());
    }

    public static void itemGlint(GuiGraphics graphics, int x, int y, int width, int height, int color, long currentMillis) {
        for (int i = 0; i < 2; i++) {
            LegacyUvAnimation.UnitQuadUv uv = LegacyUvAnimation.flatItemGlintPlan(currentMillis, i, width, height).uv();
            unitQuad(VANILLA_ITEM_GLINT, graphics, x, y, width, height,
                    uv.bottomLeftU(), uv.bottomLeftV(),
                    uv.bottomRightU(), uv.bottomRightV(),
                    uv.topRightU(), uv.topRightV(),
                    uv.topLeftU(), uv.topLeftV(),
                    color, 255, BlendMode.GLINT);
        }
    }

    public static void shearedPixelQuad(ResourceLocation texture, GuiGraphics graphics,
            double x, double y, double u, double v, double width, double height,
            double textureWidth, double textureHeight, double horizontalShear,
            int color, int alpha, BlendMode blendMode) {
        if (textureWidth == 0.0D || textureHeight == 0.0D) {
            return;
        }
        unitQuad(texture, graphics, x, y, width, height,
                (u + height * horizontalShear) / textureWidth, (v + height) / textureHeight,
                (u + width + height * horizontalShear) / textureWidth, (v + height) / textureHeight,
                (u + width) / textureWidth, v / textureHeight,
                u / textureWidth, v / textureHeight,
                color, alpha, blendMode);
    }

    public static void unitQuad(ResourceLocation texture, GuiGraphics graphics,
            int x, int y, int width, int height, double uMin, double vMin, double uMax, double vMax,
            int color, int alpha, BlendMode blendMode) {
        unitQuad(texture, graphics, (double) x, (double) y, (double) width, (double) height,
                uMin, vMax, uMax, vMax, uMax, vMin, uMin, vMin, color, alpha, blendMode);
    }

    public static void unitQuad(ResourceLocation texture, GuiGraphics graphics,
            double x, double y, double width, double height, double uMin, double vMin, double uMax, double vMax,
            int color, int alpha, BlendMode blendMode) {
        unitQuad(texture, graphics, x, y, width, height,
                uMin, vMax, uMax, vMax, uMax, vMin, uMin, vMin, color, alpha, blendMode);
    }

    public static void unitQuad(ResourceLocation texture, GuiGraphics graphics,
            double x, double y, double width, double height,
            double bottomLeftU, double bottomLeftV,
            double bottomRightU, double bottomRightV,
            double topRightU, double topRightV,
            double topLeftU, double topLeftV,
            int color, int alpha, BlendMode blendMode) {
        if (width <= 0 || height <= 0) {
            return;
        }

        blendMode.apply();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        int clampedAlpha = clamp(alpha);
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        float z = 0.0F;

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, (float) x, (float) (y + height), z).uv((float) bottomLeftU, (float) bottomLeftV)
                .color(red, green, blue, clampedAlpha).endVertex();
        buffer.vertex(matrix, (float) (x + width), (float) (y + height), z).uv((float) bottomRightU, (float) bottomRightV)
                .color(red, green, blue, clampedAlpha).endVertex();
        buffer.vertex(matrix, (float) (x + width), (float) y, z).uv((float) topRightU, (float) topRightV)
                .color(red, green, blue, clampedAlpha).endVertex();
        buffer.vertex(matrix, (float) x, (float) y, z).uv((float) topLeftU, (float) topLeftV)
                .color(red, green, blue, clampedAlpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        blendMode.clear();
    }

    public static void unitQuad(ResourceLocation texture, GuiGraphics graphics,
            int x, int y, int width, int height,
            double bottomLeftU, double bottomLeftV,
            double bottomRightU, double bottomRightV,
            double topRightU, double topRightV,
            double topLeftU, double topLeftV,
            int color, int alpha, BlendMode blendMode) {
        unitQuad(texture, graphics, (double) x, (double) y, (double) width, (double) height,
                bottomLeftU, bottomLeftV, bottomRightU, bottomRightV, topRightU, topRightV, topLeftU, topLeftV,
                color, alpha, blendMode);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public enum BlendMode {
        NONE {
            @Override
            void apply() {
                RenderSystem.disableBlend();
            }
        },
        NORMAL {
            @Override
            void apply() {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
            }
        },
        ADDITIVE {
            @Override
            void apply() {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            }
        },
        GLINT {
            @Override
            void apply() {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
            }
        },
        INVERT_CROSSHAIR {
            @Override
            void apply() {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
            }
        };

        abstract void apply();

        void clear() {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public record ScopeUvPlan(double uMin, double vMin, double uMax, double vMax, double depth) {
    }

    public record ScreenRect(int x, int y, int width, int height) {
    }

    public record TextureRect(int u, int v, int width, int height) {
    }

    public record RadCounterPlan(ScreenRect frame, TextureRect frameTexture,
            ScreenRect fill, TextureRect fillTexture, RadWarningPlan warning,
            String label, int labelX, int labelY, int labelColor) {
    }

    public record RadWarningPlan(boolean visible, ScreenRect rect, TextureRect texture) {
        public static RadWarningPlan hidden() {
            return new RadWarningPlan(false, new ScreenRect(0, 0, 0, 0), new TextureRect(0, 0, 0, 0));
        }
    }

    public record CrosshairPlan(boolean visible, int x, int y, int u, int v, int size) {
        public static CrosshairPlan hidden() {
            return new CrosshairPlan(false, 0, 0, 0, 0, 0);
        }
    }

    public record StingerLockonPlan(ScreenRect frame, TextureRect frameTexture,
            ScreenRect progress, TextureRect progressTexture) {
    }

    public record AmmoHudPlan(ScreenRect item, ScreenRect durabilityBackground, TextureRect durabilityBackgroundTexture,
            ScreenRect durabilityFill, TextureRect durabilityFillTexture, int countTextX, int countTextY,
            boolean renderCount) {
    }

    public record AmmoAltHudPlan(ScreenRect item, int countTextX, int countTextY) {
    }

    public record ItemStackPopPlan(int x, int y, boolean renderEffect, boolean popTransform,
            float pivotX, float pivotY, float scaleX, float scaleY, boolean renderSecondPass) {
    }

    public record DashLayout(int x, int y, int barWidth, int barHeight, int gap, int rowHeight) {
    }

    public record DashBarSegment(int barId, int x, int y, int status, int fillWidth, boolean triggerFade) {
    }

    public record DashFadePlan(boolean visible, int x, int y, int barId) {
        public static DashFadePlan hidden() {
            return new DashFadePlan(false, 0, 0, -1);
        }
    }

    public record ShieldBarPlan(ScreenRect frame, TextureRect frameTexture, ScreenRect fill, TextureRect fillTexture,
            String label, int labelCenterX, int labelY, int consumedLeftHeight) {
    }

    public record OverlayStatePlan(boolean blendEnabled, boolean depthTestEnabled, boolean depthWrite,
                                   boolean alphaTestEnabled, LegacyTexturedRenderMode.BlendFunction blendFunction,
                                   float red, float green, float blue, float alpha,
                                   boolean restoreDepthWrite, boolean restoreDepthTest,
                                   boolean restoreAlphaTest, boolean restoreColor) {
    }

    public enum BadgeKind {
        TRUE_528,
        TRUE_EXP,
        TRUE_RAM,
        TRUE_328
    }

    public record BadgePlan(BadgeKind kind, ScreenRect rect, TextureRect texture) {
    }

    public record BadgeHudPlan(int startX, int startY, int stepWidth, List<BadgePlan> badges,
                               OverlayStatePlan state) {
    }

    private LegacyScreenQuadRenderer() {
    }
}
