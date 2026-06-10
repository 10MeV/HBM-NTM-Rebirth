package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Formula-only bridge for old RenderBlocksNT/IconUtil helpers.
 */
public final class LegacyBlockRenderHelper {
    private static final ResourceLocation FALLBACK_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/absorber.png");

    public static final List<InventoryFace> STANDARD_INVENTORY_FACE_ORDER = List.of(
            new InventoryFace(Direction.DOWN, 0.0F, -1.0F, 0.0F, 0),
            new InventoryFace(Direction.UP, 0.0F, 1.0F, 0.0F, 1),
            new InventoryFace(Direction.NORTH, 0.0F, 0.0F, -1.0F, 2),
            new InventoryFace(Direction.SOUTH, 0.0F, 0.0F, 1.0F, 3),
            new InventoryFace(Direction.WEST, -1.0F, 0.0F, 0.0F, 4),
            new InventoryFace(Direction.EAST, 1.0F, 0.0F, 0.0F, 5));

    public static ResourceLocation legacyBlockTexture(String iconName) {
        if (iconName == null || iconName.isBlank()) {
            return FALLBACK_TEXTURE;
        }
        int separator = iconName.indexOf(':');
        String namespace = separator >= 0 ? iconName.substring(0, separator) : HbmNtm.MOD_ID;
        String path = separator >= 0 ? iconName.substring(separator + 1) : iconName;
        if ("hbm".equals(namespace)) {
            namespace = HbmNtm.MOD_ID;
        }
        return new ResourceLocation(namespace, "textures/block/" + path + ".png");
    }

    public static IconUtilTexturePlan iconUtilTexturePlan(String iconName) {
        if (iconName == null || iconName.isBlank()) {
            return new IconUtilTexturePlan(iconName, "", FALLBACK_TEXTURE, true, false);
        }
        String legacyPath = iconName.length() > 4 ? iconName.substring(4) : "";
        ResourceLocation texture = legacyPath.isBlank()
                ? FALLBACK_TEXTURE
                : new ResourceLocation(HbmNtm.MOD_ID, "textures/block/" + legacyPath + ".png");
        return new IconUtilTexturePlan(iconName, legacyPath, texture, legacyPath.isBlank(), true);
    }

    public static int colorMultiplier(int rgb, boolean anaglyph) {
        return anaglyph ? LegacyRenderColor.anaglyph(rgb) : rgb & 0xFFFFFF;
    }

    public static RenderBlocksNtWorldPlan renderBlocksNtSetWorldPlan(boolean worldPresent) {
        return new RenderBlocksNtWorldPlan(worldPresent, false, false);
    }

    public static RenderBlocksWrapperPlan renderBlocksWrapperPlan(boolean delegatePresent) {
        return new RenderBlocksWrapperPlan(delegatePresent, true);
    }

    public static StandardBlockRenderPlan standardBlockRenderPlan(int colorMultiplier, boolean anaglyph,
            boolean ambientOcclusionEnabled, int blockLightValue, boolean partialRenderBounds) {
        int color = colorMultiplier(colorMultiplier, anaglyph);
        StandardBlockRenderPath path = ambientOcclusionEnabled && blockLightValue == 0
                ? partialRenderBounds ? StandardBlockRenderPath.AMBIENT_OCCLUSION_PARTIAL : StandardBlockRenderPath.AMBIENT_OCCLUSION
                : StandardBlockRenderPath.COLOR_MULTIPLIER;
        return new StandardBlockRenderPlan(colorMultiplier & 0xFFFFFF, anaglyph, ambientOcclusionEnabled,
                blockLightValue, partialRenderBounds, color, red(color), green(color), blue(color), path);
    }

    public static List<InventoryFacePlan> standardInventoryBlockPlan(int legacyMetadata) {
        return STANDARD_INVENTORY_FACE_ORDER.stream()
                .map(face -> new InventoryFacePlan(face, legacyMetadata))
                .toList();
    }

    public static StandardInventoryBlockRenderPlan standardInventoryBlockRenderPlan(int legacyMetadata) {
        List<InventoryFaceDrawPlan> draws = STANDARD_INVENTORY_FACE_ORDER.stream()
                .map(face -> new InventoryFaceDrawPlan(face, legacyMetadata, true, true))
                .toList();
        return new StandardInventoryBlockRenderPlan(legacyMetadata, 0xFFFFFF, 1.0F, 1.0F, 1.0F, draws);
    }

    public static FaceUvPlan zNegativeUvPlan(CuboidBounds bounds, boolean alternateU, boolean flipTexture, int uvRotateEast) {
        return new FaceUvPlan(Direction.NORTH, bounds, alternateU, flipTexture, uvRotateEast,
                uvBranch(uvRotateEast), bounds.minX() < 0.0D || bounds.maxX() > 1.0D,
                bounds.minY() < 0.0D || bounds.maxY() > 1.0D, zNegativeUv(bounds, alternateU, flipTexture, uvRotateEast));
    }

    public static FaceUv zNegativeUv(CuboidBounds bounds, boolean alternateU, boolean flipTexture, int uvRotateEast) {
        double minU = reversePixel(bounds.minX(), bounds.minX(), bounds.maxX(), 16.0D);
        double maxU = reversePixel(bounds.maxX(), bounds.minX(), bounds.maxX(), 0.0D);
        if (alternateU) {
            minU = forwardPixel(bounds.minX(), bounds.minX(), bounds.maxX(), 0.0D);
            maxU = forwardPixel(bounds.maxX(), bounds.minX(), bounds.maxX(), 16.0D);
        }

        double maxV = reversePixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 0.0D);
        double minV = reversePixel(bounds.minY(), bounds.minY(), bounds.maxY(), 16.0D);
        if (flipTexture) {
            double swap = maxU;
            maxU = minU;
            minU = swap;
        }
        if (bounds.minX() < 0.0D || bounds.maxX() > 1.0D) {
            maxU = 0.0D;
            minU = 16.0D;
        }
        if (bounds.minY() < 0.0D || bounds.maxY() > 1.0D) {
            maxV = 0.0D;
            minV = 16.0D;
        }

        double minU2 = minU;
        double maxU2 = maxU;
        double maxV2 = maxV;
        double minV2 = minV;
        if (uvRotateEast == 2) {
            maxU = forwardPixel(bounds.minY(), bounds.minY(), bounds.maxY(), 0.0D);
            minU = forwardPixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 16.0D);
            maxV = reversePixel(bounds.minX(), bounds.minX(), bounds.maxX(), 16.0D);
            minV = reversePixel(bounds.maxX(), bounds.minX(), bounds.maxX(), 0.0D);
            maxV2 = maxV;
            minV2 = minV;
            minU2 = maxU;
            maxU2 = minU;
            maxV = minV;
            minV = maxV2;
        } else if (uvRotateEast == 1) {
            maxU = reversePixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 0.0D);
            minU = reversePixel(bounds.minY(), bounds.minY(), bounds.maxY(), 16.0D);
            maxV = forwardPixel(bounds.maxX(), bounds.minX(), bounds.maxX(), 16.0D);
            minV = forwardPixel(bounds.minX(), bounds.minX(), bounds.maxX(), 0.0D);
            minU2 = minU;
            maxU2 = maxU;
            maxU = minU;
            minU = maxU2;
            maxV2 = minV;
            minV2 = maxV;
        } else if (uvRotateEast == 3) {
            maxU = reversePixel(bounds.minX(), bounds.minX(), bounds.maxX(), 16.0D);
            minU = reversePixel(bounds.maxX(), bounds.minX(), bounds.maxX(), 0.0D);
            maxV = forwardPixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 16.0D);
            minV = forwardPixel(bounds.minY(), bounds.minY(), bounds.maxY(), 0.0D);
            minU2 = minU;
            maxU2 = maxU;
            maxV2 = maxV;
            minV2 = minV;
        }

        return new FaceUv(minU2, maxV2, maxU, maxV, maxU2, minV2, minU, minV);
    }

    public static FaceQuadPlan zNegativeFacePlan(FaceUvPlan uvPlan, double x, double y, double z, boolean renderFromInside) {
        return zNegativeFacePlan(uvPlan.bounds(), x, y, z, uvPlan.alternateU(), uvPlan.flipTexture(), uvPlan.uvRotation(), renderFromInside);
    }

    public static FaceQuadPlan zNegativeFacePlan(CuboidBounds bounds, double x, double y, double z,
            boolean alternateU, boolean flipTexture, int uvRotateEast, boolean renderFromInside) {
        FaceUv uv = zNegativeUv(bounds, alternateU, flipTexture, uvRotateEast);
        double minX = x + bounds.minX();
        double maxX = x + bounds.maxX();
        double minY = y + bounds.minY();
        double maxY = y + bounds.maxY();
        double minZ = z + bounds.minZ();
        if (renderFromInside) {
            double swap = minX;
            minX = maxX;
            maxX = swap;
        }
        return new FaceQuadPlan(Direction.NORTH, 0.0F, 0.0F, -1.0F,
                new FaceVertex(minX, maxY, minZ, uv.u0(), uv.v0()),
                new FaceVertex(maxX, maxY, minZ, uv.u1(), uv.v1()),
                new FaceVertex(maxX, minY, minZ, uv.u2(), uv.v2()),
                new FaceVertex(minX, minY, minZ, uv.u3(), uv.v3()));
    }

    public static FaceUvPlan xPositiveUvPlan(CuboidBounds bounds, boolean alternateU, boolean flipTexture, int uvRotateSouth) {
        return new FaceUvPlan(Direction.EAST, bounds, alternateU, flipTexture, uvRotateSouth,
                uvBranch(uvRotateSouth), bounds.minZ() < 0.0D || bounds.maxZ() > 1.0D,
                bounds.minY() < 0.0D || bounds.maxY() > 1.0D, xPositiveUv(bounds, alternateU, flipTexture, uvRotateSouth));
    }

    public static FaceUv xPositiveUv(CuboidBounds bounds, boolean alternateU, boolean flipTexture, int uvRotateSouth) {
        double minU = reversePixel(bounds.minZ(), bounds.minZ(), bounds.maxZ(), 16.0D);
        double maxU = reversePixel(bounds.maxZ(), bounds.minZ(), bounds.maxZ(), 0.0D);
        if (alternateU) {
            minU = forwardPixel(bounds.minZ(), bounds.minZ(), bounds.maxZ(), 0.0D);
            maxU = forwardPixel(bounds.maxZ(), bounds.minZ(), bounds.maxZ(), 16.0D);
        }

        double maxV = reversePixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 0.0D);
        double minV = reversePixel(bounds.minY(), bounds.minY(), bounds.maxY(), 16.0D);
        if (flipTexture) {
            double swap = maxU;
            maxU = minU;
            minU = swap;
        }
        if (bounds.minZ() < 0.0D || bounds.maxZ() > 1.0D) {
            maxU = 0.0D;
            minU = 16.0D;
        }
        if (bounds.minY() < 0.0D || bounds.maxY() > 1.0D) {
            maxV = 0.0D;
            minV = 16.0D;
        }

        double minU2 = minU;
        double maxU2 = maxU;
        double maxV2 = maxV;
        double minV2 = minV;
        if (uvRotateSouth == 2) {
            maxU = forwardPixel(bounds.minY(), bounds.minY(), bounds.maxY(), 0.0D);
            maxV = reversePixel(bounds.minZ(), bounds.minZ(), bounds.maxZ(), 16.0D);
            minU = forwardPixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 16.0D);
            minV = reversePixel(bounds.maxZ(), bounds.minZ(), bounds.maxZ(), 0.0D);
            maxV2 = maxV;
            minV2 = minV;
            minU2 = maxU;
            maxU2 = minU;
            maxV = minV;
            minV = maxV2;
        } else if (uvRotateSouth == 1) {
            maxU = reversePixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 0.0D);
            maxV = forwardPixel(bounds.maxZ(), bounds.minZ(), bounds.maxZ(), 16.0D);
            minU = reversePixel(bounds.minY(), bounds.minY(), bounds.maxY(), 16.0D);
            minV = forwardPixel(bounds.minZ(), bounds.minZ(), bounds.maxZ(), 0.0D);
            minU2 = minU;
            maxU2 = maxU;
            maxU = minU;
            minU = maxU2;
            maxV2 = minV;
            minV2 = maxV;
        } else if (uvRotateSouth == 3) {
            maxU = reversePixel(bounds.minZ(), bounds.minZ(), bounds.maxZ(), 16.0D);
            minU = reversePixel(bounds.maxZ(), bounds.minZ(), bounds.maxZ(), 0.0D);
            maxV = forwardPixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 16.0D);
            minV = forwardPixel(bounds.minY(), bounds.minY(), bounds.maxY(), 0.0D);
            minU2 = minU;
            maxU2 = maxU;
            maxV2 = maxV;
            minV2 = minV;
        }

        return new FaceUv(maxU2, minV2, minU, minV, minU2, maxV2, maxU, maxV);
    }

    public static FaceQuadPlan xPositiveFacePlan(FaceUvPlan uvPlan, double x, double y, double z, boolean renderFromInside) {
        return xPositiveFacePlan(uvPlan.bounds(), x, y, z, uvPlan.alternateU(), uvPlan.flipTexture(), uvPlan.uvRotation(), renderFromInside);
    }

    public static FaceQuadPlan xPositiveFacePlan(CuboidBounds bounds, double x, double y, double z,
            boolean alternateU, boolean flipTexture, int uvRotateSouth, boolean renderFromInside) {
        FaceUv uv = xPositiveUv(bounds, alternateU, flipTexture, uvRotateSouth);
        double maxX = x + bounds.maxX();
        double minY = y + bounds.minY();
        double maxY = y + bounds.maxY();
        double minZ = z + bounds.minZ();
        double maxZ = z + bounds.maxZ();
        if (renderFromInside) {
            double swap = minZ;
            minZ = maxZ;
            maxZ = swap;
        }
        return new FaceQuadPlan(Direction.EAST, 1.0F, 0.0F, 0.0F,
                new FaceVertex(maxX, minY, maxZ, uv.u0(), uv.v0()),
                new FaceVertex(maxX, minY, minZ, uv.u1(), uv.v1()),
                new FaceVertex(maxX, maxY, minZ, uv.u2(), uv.v2()),
                new FaceVertex(maxX, maxY, maxZ, uv.u3(), uv.v3()));
    }

    private static double forwardPixel(double value, double min, double max, double fallback) {
        if (min < 0.0D || max > 1.0D) {
            return fallback;
        }
        return value * 16.0D;
    }

    private static double reversePixel(double value, double min, double max, double fallback) {
        if (min < 0.0D || max > 1.0D) {
            return fallback;
        }
        return (1.0D - value) * 16.0D;
    }

    private static float red(int color) {
        return (float) (color >> 16 & 255) / 255.0F;
    }

    private static float green(int color) {
        return (float) (color >> 8 & 255) / 255.0F;
    }

    private static float blue(int color) {
        return (float) (color & 255) / 255.0F;
    }

    private static FaceUvBranch uvBranch(int rotation) {
        return switch (rotation) {
            case 1 -> FaceUvBranch.ROTATE_1;
            case 2 -> FaceUvBranch.ROTATE_2;
            case 3 -> FaceUvBranch.ROTATE_3;
            default -> FaceUvBranch.DEFAULT;
        };
    }

    public enum StandardBlockRenderPath {
        AMBIENT_OCCLUSION,
        AMBIENT_OCCLUSION_PARTIAL,
        COLOR_MULTIPLIER
    }

    public enum FaceUvBranch {
        DEFAULT,
        ROTATE_1,
        ROTATE_2,
        ROTATE_3
    }

    public record RenderBlocksNtWorldPlan(boolean worldPresent, boolean field152631FReset, boolean flipTextureReset) {
    }

    public record RenderBlocksWrapperPlan(boolean delegatePresent, boolean delegatesOverrideMethods) {
    }

    public record StandardBlockRenderPlan(int rawColor, boolean anaglyph, boolean ambientOcclusionEnabled,
                                          int blockLightValue, boolean partialRenderBounds,
                                          int color, float red, float green, float blue, StandardBlockRenderPath path) {
    }

    public record IconUtilTexturePlan(String iconName, String strippedLegacyPath, ResourceLocation texture,
                                      boolean missing, boolean usedLegacySubstring) {
    }

    public record CuboidBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        public static CuboidBounds unit() {
            return new CuboidBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
        }
    }

    public record InventoryFace(Direction direction, float normalX, float normalY, float normalZ, int legacySide) {
    }

    public record InventoryFacePlan(InventoryFace face, int legacyMetadata) {
    }

    public record StandardInventoryBlockRenderPlan(int legacyMetadata, int color, float red, float green, float blue,
                                                   List<InventoryFaceDrawPlan> draws) {
    }

    public record InventoryFaceDrawPlan(InventoryFace face, int legacyMetadata,
                                        boolean startsDrawingQuads, boolean drawsImmediately) {
    }

    public record FaceVertex(double x, double y, double z, double u, double v) {
    }

    public record FaceQuadPlan(Direction direction, float normalX, float normalY, float normalZ,
            FaceVertex v0, FaceVertex v1, FaceVertex v2, FaceVertex v3) {
    }

    public record FaceUvPlan(Direction direction, CuboidBounds bounds, boolean alternateU, boolean flipTexture,
                             int uvRotation, FaceUvBranch branch, boolean uBoundsFallback,
                             boolean vBoundsFallback, FaceUv uv) {
    }

    public record FaceUv(double u0, double v0, double u1, double v1,
            double u2, double v2, double u3, double v3) {
        public LegacyTexturedQuadRenderer.Vertex apply0(LegacyTexturedQuadRenderer.Vertex vertex) {
            return new LegacyTexturedQuadRenderer.Vertex(vertex.x(), vertex.y(), vertex.z(),
                    (float) u0, (float) v0, vertex.color(), vertex.alpha(), vertex.packedLight(), vertex.packedOverlay());
        }

        public LegacyTexturedQuadRenderer.Vertex apply1(LegacyTexturedQuadRenderer.Vertex vertex) {
            return new LegacyTexturedQuadRenderer.Vertex(vertex.x(), vertex.y(), vertex.z(),
                    (float) u1, (float) v1, vertex.color(), vertex.alpha(), vertex.packedLight(), vertex.packedOverlay());
        }

        public LegacyTexturedQuadRenderer.Vertex apply2(LegacyTexturedQuadRenderer.Vertex vertex) {
            return new LegacyTexturedQuadRenderer.Vertex(vertex.x(), vertex.y(), vertex.z(),
                    (float) u2, (float) v2, vertex.color(), vertex.alpha(), vertex.packedLight(), vertex.packedOverlay());
        }

        public LegacyTexturedQuadRenderer.Vertex apply3(LegacyTexturedQuadRenderer.Vertex vertex) {
            return new LegacyTexturedQuadRenderer.Vertex(vertex.x(), vertex.y(), vertex.z(),
                    (float) u3, (float) v3, vertex.color(), vertex.alpha(), vertex.packedLight(), vertex.packedOverlay());
        }
    }

    private LegacyBlockRenderHelper() {
    }
}
