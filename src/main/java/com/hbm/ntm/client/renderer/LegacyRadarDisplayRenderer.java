package com.hbm.ntm.client.renderer;

import com.hbm.ntm.api.entity.RadarDisplayProjection;
import com.hbm.ntm.api.entity.RadarEntry;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public final class LegacyRadarDisplayRenderer {
    public static final double TEXTURE_SIZE = 256.0D;
    public static final int BLIP_U = 216;
    public static final int BLIP_SIZE = 8;
    public static final int GUI_AREA_SIZE = RadarDisplayProjection.GUI_AREA_SIZE;
    public static final int GUI_BLIP_RANGE = RadarDisplayProjection.GUI_BLIP_RANGE;
    public static final double WORLD_PANEL_X = 0.38D;
    public static final double WORLD_PANEL_Y_MIN = 0.125D;
    public static final double WORLD_PANEL_Y_MAX = 1.875D;
    public static final double WORLD_PANEL_Z_MIN = -0.375D;
    public static final double WORLD_PANEL_Z_MAX = 1.375D;
    public static final double WORLD_BLIP_SIZE = RadarDisplayProjection.WORLD_BLIP_SIZE;
    public static final double WORLD_BLIP_RANGE = RadarDisplayProjection.WORLD_BLIP_RANGE;

    public static double worldScanOffset(long gameTime, float partialTick) {
        return ((gameTime % 56L) + partialTick) / 30.0D;
    }

    public static int noiseV(long seed) {
        return 118 + (int) Math.floorMod(seed, 81L);
    }

    public static void renderWorldLinkedSweep(ObjRenderContext context, double offset) {
        LegacyUntexturedQuadRenderer.doubleSidedQuad(context,
                WORLD_PANEL_X, 2.0D - offset, WORLD_PANEL_Z_MAX,
                WORLD_PANEL_X, 2.0D - offset, WORLD_PANEL_Z_MIN,
                WORLD_PANEL_X, 2.0D - offset - 0.125D, WORLD_PANEL_Z_MIN,
                WORLD_PANEL_X, 2.0D - offset - 0.125D, WORLD_PANEL_Z_MAX,
                0x00FF00, 0, 0, 50, 50);
    }

    public static void renderWorldNoise(ResourceLocation texture, ObjRenderContext context, int vOffset) {
        LegacyTexturedQuadRenderer.pixelQuad(texture, context.withoutTranslucency(),
                0.0F, 1.0F, 0.0F, TEXTURE_SIZE, TEXTURE_SIZE,
                WORLD_PANEL_X, WORLD_PANEL_Y_MAX, WORLD_PANEL_Z_MAX, 216.0D, vOffset + 40.0D,
                WORLD_PANEL_X, WORLD_PANEL_Y_MAX, WORLD_PANEL_Z_MIN, 256.0D, vOffset + 40.0D,
                WORLD_PANEL_X, WORLD_PANEL_Y_MIN, WORLD_PANEL_Z_MIN, 256.0D, vOffset,
                WORLD_PANEL_X, WORLD_PANEL_Y_MIN, WORLD_PANEL_Z_MAX, 216.0D, vOffset,
                0xFFFFFF, 255);
    }

    public static void renderWorldBlip(ResourceLocation texture, ObjRenderContext context, RadarEntry entry,
            BlockPos reference, int range) {
        RadarDisplayProjection.WorldOffset offset =
                RadarDisplayProjection.worldBlipOffset(entry.pos(), reference, range);
        int blip = Mth.clamp(entry.blipLevel(), 0, 31);
        double v0 = blip * BLIP_SIZE;
        double v1 = v0 + BLIP_SIZE;
        LegacyTexturedQuadRenderer.pixelQuad(texture, context,
                0.0F, 1.0F, 0.0F, TEXTURE_SIZE, TEXTURE_SIZE,
                WORLD_PANEL_X, 1.0D - offset.z() + WORLD_BLIP_SIZE, 0.5D - offset.x() + WORLD_BLIP_SIZE, BLIP_U, v1,
                WORLD_PANEL_X, 1.0D - offset.z() + WORLD_BLIP_SIZE, 0.5D - offset.x() - WORLD_BLIP_SIZE, BLIP_U + BLIP_SIZE, v1,
                WORLD_PANEL_X, 1.0D - offset.z() - WORLD_BLIP_SIZE, 0.5D - offset.x() - WORLD_BLIP_SIZE, BLIP_U + BLIP_SIZE, v0,
                WORLD_PANEL_X, 1.0D - offset.z() - WORLD_BLIP_SIZE, 0.5D - offset.x() + WORLD_BLIP_SIZE, BLIP_U, v0,
                0xFFFFFF, 255);
    }

    public static ScreenOffset guiBlipOffset(BlockPos entry, BlockPos center, int range) {
        RadarDisplayProjection.ScreenOffset offset = RadarDisplayProjection.guiBlipOffset(entry, center, range);
        return new ScreenOffset(offset.x(), offset.z());
    }

    public static ScreenOffset guiBlipHitOffset(BlockPos entry, BlockPos center, int range) {
        RadarDisplayProjection.ScreenOffset offset = RadarDisplayProjection.guiBlipHitOffset(entry, center, range);
        return new ScreenOffset(offset.x(), offset.z());
    }

    public static int guiTargetX(double screenOffsetFromCenter, BlockPos center, int range) {
        return RadarDisplayProjection.guiTargetX(screenOffsetFromCenter, center, range);
    }

    public static int guiTargetZ(double screenOffsetFromCenter, BlockPos center, int range) {
        return RadarDisplayProjection.guiTargetZ(screenOffsetFromCenter, center, range);
    }

    public static void renderGuiBlip(ResourceLocation texture, GuiGraphics graphics,
            double x, double y, int blipLevel) {
        int blip = Mth.clamp(blipLevel, 0, 31);
        LegacyScreenQuadRenderer.pixelQuad(texture, graphics, x, y, BLIP_U, blip * BLIP_SIZE,
                BLIP_SIZE, BLIP_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, 0xFFFFFF, 255,
                LegacyScreenQuadRenderer.BlendMode.NONE);
    }

    public static void renderGuiNoiseTile(ResourceLocation texture, GuiGraphics graphics,
            int x, int y, long seed) {
        graphics.blit(texture, x, y, 216, noiseV(seed), 40, 40);
    }

    public static void renderGuiSweep(GuiGraphics graphics, double centerX, double centerY, double rotationDegrees) {
        double angle = -Math.toRadians(rotationDegrees + 180.0D);
        double leadAngle = angle + 0.25D;
        double trX = centerX + Math.cos(angle) * 100.0D;
        double trY = centerY + Math.sin(angle) * 100.0D;
        double tlX = centerX + Math.cos(leadAngle) * 100.0D;
        double tlY = centerY + Math.sin(leadAngle) * 100.0D;
        double blX = centerX + Math.sin(angle) * 5.0D;
        double blY = centerY - Math.cos(angle) * 5.0D;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, (float) centerX, (float) centerY, 0.0F).color(0, 255, 0, 0).endVertex();
        buffer.vertex(matrix, (float) trX, (float) trY, 0.0F).color(0, 255, 0, 255).endVertex();
        buffer.vertex(matrix, (float) tlX, (float) tlY, 0.0F).color(0, 255, 0, 0).endVertex();
        buffer.vertex(matrix, (float) blX, (float) blY, 0.0F).color(0, 255, 0, 0).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.disableBlend();
    }

    public record ScreenOffset(double x, double z) {
    }

    private LegacyRadarDisplayRenderer() {
    }
}
