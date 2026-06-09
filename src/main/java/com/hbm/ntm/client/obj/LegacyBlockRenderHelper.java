package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Formula-only bridge for old RenderBlocksNT/IconUtil helpers.
 */
public final class LegacyBlockRenderHelper {
    public static final List<InventoryFace> STANDARD_INVENTORY_FACE_ORDER = List.of(
            new InventoryFace(Direction.DOWN, 0.0F, -1.0F, 0.0F, 0),
            new InventoryFace(Direction.UP, 0.0F, 1.0F, 0.0F, 1),
            new InventoryFace(Direction.NORTH, 0.0F, 0.0F, -1.0F, 2),
            new InventoryFace(Direction.SOUTH, 0.0F, 0.0F, 1.0F, 3),
            new InventoryFace(Direction.WEST, -1.0F, 0.0F, 0.0F, 4),
            new InventoryFace(Direction.EAST, 1.0F, 0.0F, 0.0F, 5));

    public static ResourceLocation legacyBlockTexture(String iconName) {
        if (iconName == null || iconName.isBlank()) {
            return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/missing.png");
        }
        int separator = iconName.indexOf(':');
        String namespace = separator >= 0 ? iconName.substring(0, separator) : HbmNtm.MOD_ID;
        String path = separator >= 0 ? iconName.substring(separator + 1) : iconName;
        if ("hbm".equals(namespace)) {
            namespace = HbmNtm.MOD_ID;
        }
        return new ResourceLocation(namespace, "textures/block/" + path + ".png");
    }

    public static int colorMultiplier(int rgb, boolean anaglyph) {
        return anaglyph ? LegacyRenderColor.anaglyph(rgb) : rgb & 0xFFFFFF;
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

    public record CuboidBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        public static CuboidBounds unit() {
            return new CuboidBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
        }
    }

    public record InventoryFace(Direction direction, float normalX, float normalY, float normalZ, int legacySide) {
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
