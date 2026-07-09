package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjMissilePartModels;
import net.minecraft.world.phys.AABB;

import java.util.LinkedHashMap;
import java.util.Map;

final class MissilePartRenderCache {
    private static final double DEFAULT_PART_GUI_HEIGHT = 4.0D;
    private static final double DEFAULT_CUSTOM_MISSILE_HEIGHT = 4.0D;
    private static final AABB DEFAULT_CUSTOM_MISSILE_BOUNDS =
            new AABB(0.0D, 0.0D, 0.0D, 1.0D, 4.0D, 1.0D);
    private static final Map<String, PartRenderSpec> BY_LEGACY_NAME = specsByName();
    private static final Map<ObjMissilePartModels.LegacyMissilePart, PartRenderSpec> BY_PART = specsByPart();

    private MissilePartRenderCache() {
    }

    static PartRenderSpec spec(String legacyItemName) {
        return BY_LEGACY_NAME.get(legacyItemName);
    }

    static PartRenderSpec spec(ObjMissilePartModels.LegacyMissilePart part) {
        return part == null ? null : BY_PART.get(part);
    }

    static float fitScale(float targetSize, double maxSize, double min, double max) {
        return (float) Math.max(min, Math.min(max, targetSize / Math.max(1.0D, maxSize)));
    }

    private static Map<String, PartRenderSpec> specsByName() {
        Map<String, PartRenderSpec> specs = new LinkedHashMap<>();
        for (ObjMissilePartModels.LegacyMissilePart part : ObjMissilePartModels.parts().values()) {
            specs.put(part.legacyItemName(), build(part));
        }
        return Map.copyOf(specs);
    }

    private static Map<ObjMissilePartModels.LegacyMissilePart, PartRenderSpec> specsByPart() {
        Map<ObjMissilePartModels.LegacyMissilePart, PartRenderSpec> specs = new LinkedHashMap<>();
        for (PartRenderSpec spec : BY_LEGACY_NAME.values()) {
            specs.put(spec.part(), spec);
        }
        return Map.copyOf(specs);
    }

    private static PartRenderSpec build(ObjMissilePartModels.LegacyMissilePart part) {
        AABB bounds = part.model().boundsAll();
        double guiHeight = part.guiHeight() > 0.0D ? part.guiHeight() : DEFAULT_PART_GUI_HEIGHT;
        double maxSize = Math.max(guiHeight,
                Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize())));
        return new PartRenderSpec(part, bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.maxZ,
                (bounds.minX + bounds.maxX) * 0.5D,
                (bounds.minY + bounds.maxY) * 0.5D,
                (bounds.minZ + bounds.maxZ) * 0.5D,
                maxSize);
    }

    static CustomMissileFit customMissileFit(PartRenderSpec thruster, PartRenderSpec fins, PartRenderSpec fuselage,
            PartRenderSpec warhead, float guiTargetSize, float worldTargetSize) {
        BoundsAccumulator bounds = new BoundsAccumulator();
        double y = 0.0D;
        if (thruster != null) {
            bounds.include(thruster, y);
            y += thruster.part().height();
        }
        if (fuselage != null) {
            if (fins != null) {
                bounds.include(fins, y);
            }
            bounds.include(fuselage, y);
            y += fuselage.part().height();
        }
        if (warhead != null) {
            bounds.include(warhead, y);
        }

        if (!bounds.found) {
            bounds.include(DEFAULT_CUSTOM_MISSILE_BOUNDS);
        }

        double height = ObjMissilePartModels.missileHeight(
                part(thruster), part(fuselage), part(warhead));
        if (height <= 0.0D) {
            height = DEFAULT_CUSTOM_MISSILE_HEIGHT;
        }
        double maxSize = Math.max(height, Math.max(bounds.maxX - bounds.minX,
                Math.max(bounds.maxY - bounds.minY, bounds.maxZ - bounds.minZ)));
        return new CustomMissileFit(
                (bounds.minX + bounds.maxX) * 0.5D,
                (bounds.minY + bounds.maxY) * 0.5D,
                (bounds.minZ + bounds.maxZ) * 0.5D,
                fitScale(guiTargetSize, maxSize, 0.03D, 0.42D),
                fitScale(worldTargetSize, maxSize, 0.03D, 0.42D));
    }

    private static ObjMissilePartModels.LegacyMissilePart part(PartRenderSpec spec) {
        return spec == null ? null : spec.part();
    }

    record PartRenderSpec(
            ObjMissilePartModels.LegacyMissilePart part,
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ,
            double centerX,
            double centerY,
            double centerZ,
            double maxSize) {
    }

    record CustomMissileFit(double centerX, double centerY, double centerZ, float guiFitScale, float worldFitScale) {
    }

    private static final class BoundsAccumulator {
        private boolean found;
        private double minX;
        private double minY;
        private double minZ;
        private double maxX;
        private double maxY;
        private double maxZ;

        void include(PartRenderSpec spec, double yOffset) {
            include(spec.minX(), spec.minY() + yOffset, spec.minZ(), spec.maxX(), spec.maxY() + yOffset, spec.maxZ());
        }

        void include(AABB bounds) {
            include(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.maxZ);
        }

        private void include(double partMinX, double partMinY, double partMinZ,
                double partMaxX, double partMaxY, double partMaxZ) {
            if (!found) {
                found = true;
                minX = partMinX;
                minY = partMinY;
                minZ = partMinZ;
                maxX = partMaxX;
                maxY = partMaxY;
                maxZ = partMaxZ;
                return;
            }
            minX = Math.min(minX, partMinX);
            minY = Math.min(minY, partMinY);
            minZ = Math.min(minZ, partMinZ);
            maxX = Math.max(maxX, partMaxX);
            maxY = Math.max(maxY, partMaxY);
            maxZ = Math.max(maxZ, partMaxZ);
        }
    }
}
