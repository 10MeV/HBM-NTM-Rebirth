package com.hbm.ntm.client.obj;

import com.hbm.ntm.energy.HbmLegacyWireRenderMath;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class LegacyTexturedLineRenderer {
    public static final double PYLON_WIRE_GIRTH = 0.03125D;
    public static final double PYLON_WIRE_U_WRAP_PER_BLOCK = 8.0D;
    public static final int PYLON_HANG_SEGMENTS = HbmLegacyWireRenderMath.PYLON_HANG_SEGMENTS;
    public static final double PYLON_MAX_HANG = HbmLegacyWireRenderMath.PYLON_MAX_HANG;
    public static final double PYLON_HANG_DIVISOR = HbmLegacyWireRenderMath.PYLON_HANG_DIVISOR;

    public static void pylonLine(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, double x0, double y0, double z0,
            double x1, double y1, double z1, boolean hang, int color) {
        pylonLine(texture, poseStack, buffer, packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL,
                x0, y0, z0, x1, y1, z1, hang, color, 255);
    }

    public static void pylonLine(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            double x0, double y0, double z0, double x1, double y1, double z1,
            boolean hang, int color, int alpha) {
        WireOffsets offsets = pylonWireOffsets(x0, y0, z0, x1, y1, z1, PYLON_WIRE_GIRTH);
        if (!hang) {
            wrappedLineSegment(texture, poseStack, buffer, packedLight, packedOverlay, renderMode,
                    x0, y0, z0, x1, y1, z1,
                    offsets.iX(), offsets.iY(), offsets.iZ(), offsets.jX(), offsets.jZ(),
                    PYLON_WIRE_U_WRAP_PER_BLOCK, color, alpha);
            return;
        }
        for (WireSubSegment segment : saggedPylonSegments(x0, y0, z0, x1, y1, z1, PYLON_HANG_SEGMENTS)) {
            wrappedLineSegment(texture, poseStack, buffer, packedLight, packedOverlay, renderMode,
                    segment.x0(), segment.y0(), segment.z0(), segment.x1(), segment.y1(), segment.z1(),
                    offsets.iX(), offsets.iY(), offsets.iZ(), offsets.jX(), offsets.jZ(),
                    PYLON_WIRE_U_WRAP_PER_BLOCK, color, alpha);
        }
    }

    public static WireOffsets pylonWireOffsets(double x0, double y0, double z0,
            double x1, double y1, double z1, double girth) {
        double deltaX = x0 - x1;
        double deltaY = y0 - y1;
        double deltaZ = z0 - z1;
        double horizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double yaw = Math.atan2(deltaX, deltaZ);
        double pitch = Math.atan2(deltaY, horizontal);
        double newPitch = pitch + Math.PI * 0.5D;
        double newYaw = yaw + Math.PI * 0.5D;
        double iZ = Math.cos(yaw) * Math.cos(newPitch) * girth;
        double iX = Math.sin(yaw) * Math.cos(newPitch) * girth;
        double iY = Math.sin(newPitch) * girth;
        double jZ = Math.cos(newYaw) * girth;
        double jX = Math.sin(newYaw) * girth;
        return new WireOffsets(iX, iY, iZ, jX, jZ);
    }

    public static List<WireSubSegment> saggedPylonSegments(double x0, double y0, double z0,
            double x1, double y1, double z1, int count) {
        int safeCount = Math.max(1, count);
        double deltaX = x1 - x0;
        double deltaY = y1 - y0;
        double deltaZ = z1 - z0;
        double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        double hang = HbmLegacyWireRenderMath.pylonHang(length);
        List<WireSubSegment> segments = new ArrayList<>(safeCount);
        for (int i = 0; i < safeCount; i++) {
            double j = i;
            double k = i + 1.0D;
            double sagJ = pylonSag(j, safeCount, hang);
            double sagK = pylonSag(k, safeCount, hang);
            double sagMean = (sagJ + sagK) / 2.0D;
            double sampleT = (j + 0.5D) / safeCount;
            segments.add(new WireSubSegment(
                    x0 + deltaX * j / safeCount,
                    y0 + deltaY * j / safeCount - sagJ,
                    z0 + deltaZ * j / safeCount,
                    x0 + deltaX * k / safeCount,
                    y0 + deltaY * k / safeCount - sagK,
                    z0 + deltaZ * k / safeCount,
                    x0 + deltaX * sampleT,
                    y0 + deltaY * sampleT - sagMean,
                    z0 + deltaZ * sampleT));
        }
        return segments;
    }

    public static double pylonSag(double segmentIndex, double count, double hang) {
        return Math.sin(segmentIndex / count * Math.PI * 0.5D) * hang;
    }

    public static int pylonSecondMountIndex(int line, int secondMountCount, int lineCount,
            int firstLegacyMetadata, int secondLegacyMetadata) {
        return HbmLegacyWireRenderMath.pylonSecondMountIndex(line, secondMountCount, lineCount,
                firstLegacyMetadata, secondLegacyMetadata);
    }

    public static boolean crossesLegacyFourWirePylons(int firstLegacyMetadata, int secondLegacyMetadata) {
        return HbmLegacyWireRenderMath.crossesLegacyFourWirePylons(firstLegacyMetadata, secondLegacyMetadata);
    }

    public static void wrappedLineSegment(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            double x0, double y0, double z0, double x1, double y1, double z1,
            double iX, double iY, double iZ, double jX, double jZ, double uWrapPerBlock,
            int color, int alpha) {
        double deltaX = x1 - x0;
        double deltaY = y1 - y0;
        double deltaZ = z1 - z0;
        double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        double wrap = Math.ceil(length * uWrapPerBlock);

        if (deltaX + deltaZ < 0.0D) {
            wrap *= -1.0D;
            jX *= -1.0D;
            jZ *= -1.0D;
        }

        int rgb = color & 0xFFFFFF;
        LegacyTexturedQuadRenderer.quadWithComputedNormal(texture, poseStack, buffer, packedLight, packedOverlay,
                renderMode,
                LegacyTexturedQuadRenderer.vertex(x0 + iX, y0 + iY, z0 + iZ, 0.0D, 0.0D, rgb, alpha),
                LegacyTexturedQuadRenderer.vertex(x0 - iX, y0 - iY, z0 - iZ, 0.0D, 1.0D, rgb, alpha),
                LegacyTexturedQuadRenderer.vertex(x1 - iX, y1 - iY, z1 - iZ, wrap, 1.0D, rgb, alpha),
                LegacyTexturedQuadRenderer.vertex(x1 + iX, y1 + iY, z1 + iZ, wrap, 0.0D, rgb, alpha));
        LegacyTexturedQuadRenderer.quadWithComputedNormal(texture, poseStack, buffer, packedLight, packedOverlay,
                renderMode,
                LegacyTexturedQuadRenderer.vertex(x0 + jX, y0, z0 + jZ, 0.0D, 0.0D, rgb, alpha),
                LegacyTexturedQuadRenderer.vertex(x0 - jX, y0, z0 - jZ, 0.0D, 1.0D, rgb, alpha),
                LegacyTexturedQuadRenderer.vertex(x1 - jX, y1, z1 - jZ, wrap, 1.0D, rgb, alpha),
                LegacyTexturedQuadRenderer.vertex(x1 + jX, y1, z1 + jZ, wrap, 0.0D, rgb, alpha));
    }

    public static WireWrap wireWrap(double x0, double y0, double z0, double x1, double y1, double z1,
            double uWrapPerBlock) {
        double deltaX = x1 - x0;
        double deltaY = y1 - y0;
        double deltaZ = z1 - z0;
        double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        double wrap = Math.ceil(length * uWrapPerBlock);
        boolean flipped = deltaX + deltaZ < 0.0D;
        return new WireWrap(flipped ? -wrap : wrap, flipped);
    }

    public record WireOffsets(double iX, double iY, double iZ, double jX, double jZ) {
    }

    public record WireWrap(double wrap, boolean flipped) {
    }

    public record WireSubSegment(
            double x0, double y0, double z0,
            double x1, double y1, double z1,
            double sampleX, double sampleY, double sampleZ) {
    }

    private LegacyTexturedLineRenderer() {
    }
}
