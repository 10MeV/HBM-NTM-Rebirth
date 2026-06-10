package com.hbm.ntm.client.obj;

import net.minecraft.core.Direction;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Data plans for old ISimpleBlockRenderingHandler shapes that hand-wrote Tessellator vertices.
 */
public final class LegacyIsbrhBlockPlans {
    public static final double CHAIN_WALL_OFFSET = 0.05D;
    public static final double CHAIN_SCALE = 0.0D;
    public static final double GRATE_THICKNESS = 0.125D;
    public static final double STEEL_WALL_THICKNESS = 0.125D;
    public static final double STEEL_CORNER_THICKNESS = 0.125D;
    public static final double STEEL_CORNER_JOIN_MIN = 0.75D;
    public static final double FENCE_CENTER_MIN = 0.4375D;
    public static final double FENCE_CENTER_MAX = 0.5625D;
    public static final double FENCE_POST_MIN = 0.375D;
    public static final double FENCE_POST_MAX = 0.625D;
    public static final TranslationPlan PEDESTAL_INVENTORY_TRANSLATION =
            new TranslationPlan(-0.5D, -0.5D, -0.5D);
    public static final LegacyAtlasCuboidRenderer.CuboidBounds STEEL_WALL_INVENTORY_BOUNDS =
            new LegacyAtlasCuboidRenderer.CuboidBounds(0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D);
    public static final TranslationPlan OBJ_INVENTORY_TRANSLATION = new TranslationPlan(0.0D, -0.5D, 0.0D);
    public static final TranslationPlan STEEL_WALL_INVENTORY_TRANSLATION =
            new TranslationPlan(-0.5D, -0.5D, -0.9375D);
    public static final TranslationPlan STEEL_CORNER_INVENTORY_TRANSLATION =
            new TranslationPlan(-0.5D, -0.5D, -0.5D);
    public static final List<Direction> STEEL_WALL_INVENTORY_FACE_ORDER =
            List.of(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH);
    public static final List<Direction> STEEL_CORNER_INVENTORY_FACE_ORDER =
            List.of(Direction.UP, Direction.DOWN, Direction.WEST, Direction.SOUTH, Direction.NORTH,
                    Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH,
                    Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST, Direction.NORTH);

    public static ChainRenderPlan chainPlan(int metadata, double x, double y, double z) {
        return new ChainRenderPlan(metadata, CHAIN_SCALE, CHAIN_WALL_OFFSET, false,
                List.of(whiteColorPlan()), chainQuads(metadata, x, y, z));
    }

    public static GrateRenderPlan gratePlan(int metadata, float legacyY) {
        int blockYOffset = 0;
        float localY = legacyY;
        if (localY < 0.0F) {
            localY += 1.0F;
            blockYOffset = -1;
        } else if (localY >= 1.0F) {
            localY -= 1.0F;
            blockYOffset = 1;
        }
        LegacyAtlasCuboidRenderer.CuboidBounds bounds = new LegacyAtlasCuboidRenderer.CuboidBounds(
                0.0D, localY, 0.0D, 1.0D, localY + GRATE_THICKNESS, 1.0D);
        return new GrateRenderPlan(metadata, legacyY, localY, blockYOffset, GRATE_THICKNESS, bounds, true);
    }

    public static SteelWallRenderPlan steelWallPlan(int metadata) {
        return new SteelWallRenderPlan(metadata, steelWallBounds(metadata),
                STEEL_WALL_INVENTORY_BOUNDS, STEEL_WALL_INVENTORY_TRANSLATION,
                STEEL_WALL_INVENTORY_FACE_ORDER, true, true);
    }

    public static LegacyAtlasCuboidRenderer.CuboidBounds steelWallBounds(int metadata) {
        return switch (metadata) {
            case 2 -> new LegacyAtlasCuboidRenderer.CuboidBounds(0.0D, 0.0D, 1.0D - STEEL_WALL_THICKNESS,
                    1.0D, 1.0D, 1.0D);
            case 3 -> new LegacyAtlasCuboidRenderer.CuboidBounds(0.0D, 0.0D, 0.0D,
                    1.0D, 1.0D, STEEL_WALL_THICKNESS);
            case 4 -> new LegacyAtlasCuboidRenderer.CuboidBounds(1.0D - STEEL_WALL_THICKNESS, 0.0D, 0.0D,
                    1.0D, 1.0D, 1.0D);
            case 5 -> new LegacyAtlasCuboidRenderer.CuboidBounds(0.0D, 0.0D, 0.0D,
                    STEEL_WALL_THICKNESS, 1.0D, 1.0D);
            default -> STEEL_WALL_INVENTORY_BOUNDS;
        };
    }

    public static SteelCornerRenderPlan steelCornerPlan(int metadata) {
        return new SteelCornerRenderPlan(metadata, steelCornerWorldBounds(metadata),
                steelCornerInventoryBounds(), STEEL_CORNER_INVENTORY_TRANSLATION,
                STEEL_CORNER_INVENTORY_FACE_ORDER, true, true, true);
    }

    public static List<LegacyAtlasCuboidRenderer.CuboidBounds> steelCornerInventoryBounds() {
        return List.of(
                new LegacyAtlasCuboidRenderer.CuboidBounds(0.0D, 0.0D, 1.0D - STEEL_CORNER_THICKNESS,
                        STEEL_CORNER_JOIN_MIN, 1.0D, 1.0D),
                new LegacyAtlasCuboidRenderer.CuboidBounds(STEEL_CORNER_JOIN_MIN, 0.0D, STEEL_CORNER_JOIN_MIN,
                        1.0D, 1.0D, 1.0D),
                new LegacyAtlasCuboidRenderer.CuboidBounds(1.0D - STEEL_CORNER_THICKNESS, 0.0D, 0.0D,
                        1.0D, 1.0D, STEEL_CORNER_JOIN_MIN));
    }

    public static List<LegacyAtlasCuboidRenderer.CuboidBounds> steelCornerWorldBounds(int metadata) {
        return switch (metadata) {
            case 2 -> List.of(
                    new LegacyAtlasCuboidRenderer.CuboidBounds(0.25D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D),
                    new LegacyAtlasCuboidRenderer.CuboidBounds(0.0D, 0.0D, 0.75D, 0.25D, 1.0D, 1.0D),
                    new LegacyAtlasCuboidRenderer.CuboidBounds(0.0D, 0.0D, 0.0D, 0.125D, 1.0D, 0.75D));
            case 3 -> List.of(
                    new LegacyAtlasCuboidRenderer.CuboidBounds(0.0D, 0.0D, 0.0D, 0.75D, 1.0D, 0.125D),
                    new LegacyAtlasCuboidRenderer.CuboidBounds(0.75D, 0.0D, 0.0D, 1.0D, 1.0D, 0.25D),
                    new LegacyAtlasCuboidRenderer.CuboidBounds(0.875D, 0.0D, 0.25D, 1.0D, 1.0D, 1.0D));
            case 4 -> List.of(
                    new LegacyAtlasCuboidRenderer.CuboidBounds(0.875D, 0.0D, 0.0D, 1.0D, 1.0D, 0.75D),
                    new LegacyAtlasCuboidRenderer.CuboidBounds(0.75D, 0.0D, 0.75D, 1.0D, 1.0D, 1.0D),
                    new LegacyAtlasCuboidRenderer.CuboidBounds(0.0D, 0.0D, 0.875D, 0.75D, 1.0D, 1.0D));
            case 5 -> List.of(
                    new LegacyAtlasCuboidRenderer.CuboidBounds(0.0D, 0.0D, 0.25D, 0.125D, 1.0D, 1.0D),
                    new LegacyAtlasCuboidRenderer.CuboidBounds(0.0D, 0.0D, 0.0D, 0.25D, 1.0D, 0.25D),
                    new LegacyAtlasCuboidRenderer.CuboidBounds(0.25D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D));
            default -> List.of();
        };
    }

    public static ObjIconModelRenderPlan steelBeamInventoryPlan(boolean overrideTexture) {
        return new ObjIconModelRenderPlan("beam", 0, OBJ_INVENTORY_TRANSLATION,
                0.0F, 0.0F, false, overrideTexture, true);
    }

    public static ObjIconModelRenderPlan steelBeamWorldPlan(int metadata, double x, double y, double z, boolean overrideTexture) {
        return new ObjIconModelRenderPlan("beam", metadata, new TranslationPlan(x + 0.5D, y, z + 0.5D),
                0.0F, 0.0F, true, overrideTexture, true);
    }

    public static ObjIconModelRenderPlan scaffoldInventoryPlan(int metadata, boolean overrideTexture) {
        return new ObjIconModelRenderPlan("scaffold", metadata, OBJ_INVENTORY_TRANSLATION,
                0.0F, 0.0F, false, overrideTexture, true);
    }

    public static ObjIconModelRenderPlan scaffoldWorldPlan(int metadata, double x, double y, double z, boolean overrideTexture) {
        double ox = x + 0.5D;
        double oy = y;
        double oz = z + 0.5D;
        float rotation = (float) Math.PI * -0.5F;
        float pitch = 0.0F;
        if (metadata >= 12) {
            pitch = (float) Math.PI * 0.5F;
            rotation = (float) -Math.PI;
            ox = x + 1.0D;
            oy = y + 0.5D;
        } else if (metadata >= 8) {
            rotation = (float) -Math.PI;
        } else if (metadata >= 4) {
            pitch = (float) Math.PI * 0.5F;
            oy = y + 0.5D;
            oz = z;
        }
        return new ObjIconModelRenderPlan("scaffold", metadata, new TranslationPlan(ox, oy, oz),
                rotation, pitch, true, overrideTexture, true);
    }

    public static ScaledObjIconModelRenderPlan antennaTopInventoryPlan(boolean overrideTexture) {
        return scaledObjPlan("antenna_top", 0, OBJ_INVENTORY_TRANSLATION, 0.75F,
                0.0F, 0.0F, false, overrideTexture, true);
    }

    public static ScaledObjIconModelRenderPlan antennaTopWorldPlan(int metadata, double x, double y, double z,
            boolean overrideTexture) {
        return scaledObjPlan("antenna_top", metadata, new TranslationPlan(x + 0.5D, y, z + 0.5D), 1.0F,
                0.0F, 0.0F, true, overrideTexture, true);
    }

    public static ObjIconPartRenderPlan rtgInventoryPlan(boolean overrideTexture) {
        return new ObjIconPartRenderPlan("rtg", "Gen", 0, OBJ_INVENTORY_TRANSLATION,
                0.0F, 0.0F, false, overrideTexture, true, false);
    }

    public static ScaledObjIconModelRenderPlan crtInventoryPlan(int metadata, boolean overrideTexture) {
        return scaledObjPlan("crt", metadata * 4, OBJ_INVENTORY_TRANSLATION, 1.0F,
                (float) Math.PI * -0.5F, 0.0F, false, overrideTexture, true);
    }

    public static CrtRenderPlan crtWorldPlan(int metadata, double x, double y, double z, boolean overrideTexture) {
        float rotation = horizontalMetadataRotation(metadata);
        TranslationPlan translation = new TranslationPlan(x + 0.5D, y, z + 0.5D);
        return new CrtRenderPlan(metadata, metadata % 4, metadata >= 8, translation, rotation,
                new ObjIconPartRenderPlan("crt", "Monitor", metadata, translation, rotation, 0.0F,
                        true, overrideTexture, true, false),
                new ObjIconPartRenderPlan("crt", "Screen", metadata, translation, rotation, 0.0F,
                        true, overrideTexture, true, metadata >= 8));
    }

    public static ScaledObjIconModelRenderPlan toasterInventoryPlan(int metadata, boolean overrideTexture) {
        return scaledObjPlan("toaster", metadata * 4, new TranslationPlan(0.0D, -0.25D, 0.0D), 1.0F,
                (float) Math.PI * -0.5F, 0.0F, false, overrideTexture, true);
    }

    public static ScaledObjIconModelRenderPlan toasterWorldPlan(int metadata, double x, double y, double z,
            boolean overrideTexture) {
        return scaledObjPlan("toaster", metadata, new TranslationPlan(x + 0.5D, y, z + 0.5D), 1.0F,
                horizontalMetadataRotation(metadata), 0.0F, true, overrideTexture, true);
    }

    public static ScaledObjIconModelRenderPlan conserveInventoryPlan(boolean overrideTexture) {
        return scaledObjPlan("conservecrate", 0, OBJ_INVENTORY_TRANSLATION, 1.0F,
                0.0F, 0.0F, false, overrideTexture, true);
    }

    public static ScaledObjIconModelRenderPlan conserveWorldPlan(int metadata, double x, double y, double z,
            boolean overrideTexture) {
        return scaledObjPlan("conservecrate", metadata, new TranslationPlan(x + 0.5D, y, z + 0.5D), 1.0F,
                0.0F, 0.0F, true, overrideTexture, true);
    }

    public static ObjIconModelRenderPlan spikesInventoryPlan(boolean overrideTexture) {
        return new ObjIconModelRenderPlan("spikes", 0, OBJ_INVENTORY_TRANSLATION,
                0.0F, 0.0F, false, overrideTexture, true);
    }

    public static ObjIconModelRenderPlan spikesWorldPlan(int metadata, double x, double y, double z,
            boolean overrideTexture) {
        return new ObjIconModelRenderPlan("spikes", metadata, new TranslationPlan(x + 0.5D, y, z + 0.5D),
                0.0F, 0.0F, true, overrideTexture, true);
    }

    public static ScaledObjIconModelRenderPlan hevBatteryInventoryPlan(boolean overrideTexture) {
        return scaledObjPlan("hev_battery", 0, new TranslationPlan(0.0D, -0.625D, 0.0D), 3.0F,
                0.0F, 0.0F, false, overrideTexture, true);
    }

    public static ScaledObjIconModelRenderPlan hevBatteryWorldPlan(int metadata, double x, double y, double z,
            boolean overrideTexture) {
        return scaledObjPlan("hev_battery", metadata, new TranslationPlan(x + 0.5D, y, z + 0.5D), 1.0F,
                0.0F, 0.0F, true, overrideTexture, true);
    }

    public static ObjIconPartRenderPlan fluidBarrelInventoryPlan(boolean overrideTexture) {
        return new ObjIconPartRenderPlan("barrel", "Barrel", 0, OBJ_INVENTORY_TRANSLATION,
                0.0F, 0.0F, false, overrideTexture, true, false);
    }

    public static ObjIconPartRenderPlan fluidBarrelWorldPlan(int metadata, double x, double y, double z,
            boolean overrideTexture) {
        return new ObjIconPartRenderPlan("barrel", "Barrel", metadata,
                new TranslationPlan(x + 0.5D, y, z + 0.5D), 0.0F, 0.0F,
                true, overrideTexture, true, false);
    }

    public static MultiPartObjRenderPlan capacitorInventoryPlan() {
        return new MultiPartObjRenderPlan(0, new TranslationPlan(0.0D, 0.0D, 0.0D),
                0.0F, 0.0F, 1.0F, false, true, capacitorParts(0.0F, 0.0F, false));
    }

    public static MultiPartObjRenderPlan capacitorWorldPlan(int metadata, double x, double y, double z) {
        OrientationPlan orientation = capacitorLikeOrientation(metadata);
        return new MultiPartObjRenderPlan(metadata, new TranslationPlan(x + 0.5D, y + 0.5D, z + 0.5D),
                orientation.yawRadians(), orientation.pitchRadians(), 1.0F, true, true,
                capacitorParts(orientation.yawRadians(), orientation.pitchRadians(), true));
    }

    public static MultiPartObjRenderPlan rttyWorldPlan(int metadata, boolean active, double x, double y, double z) {
        OrientationPlan orientation = capacitorLikeOrientation(metadata);
        return new MultiPartObjRenderPlan(metadata, new TranslationPlan(x + 0.5D, y + 0.5D, z + 0.5D),
                orientation.yawRadians(), orientation.pitchRadians(), 1.0F, false, false,
                List.of(new NamedIconPartPlan("rtty", "ALL", active ? "active" : "inactive",
                        active ? 1 : 0, 0, orientation.yawRadians(), orientation.pitchRadians(), false)));
    }

    public static SplitterRenderPlan splitterInventoryPlan() {
        return new SplitterRenderPlan(0, true, new TranslationPlan(0.0D, -0.5D, 0.5D),
                (float) Math.PI * -0.5F, 0.0F, 0.625F, false, true,
                splitterParts(true, 0.0F, false), new TranslationPlan(0.0D, 0.0D, -1.0D),
                splitterParts(false, 0.0F, false));
    }

    public static SplitterRenderPlan splitterWorldPlan(int metadata, double x, double y, double z) {
        float rotation = splitterRotation(metadata);
        boolean left = metadata >= 12;
        List<NamedIconPartPlan> parts = splitterParts(left, rotation, true);
        return new SplitterRenderPlan(metadata, left, new TranslationPlan(x + 0.5D, y, z + 0.5D),
                rotation, 0.0F, 1.0F, true, true, parts,
                new TranslationPlan(0.0D, 0.0D, 0.0D), List.of());
    }

    public static DiodeRenderPlan diodeInventoryPlan() {
        return new DiodeRenderPlan(0, new TranslationPlan(-0.5D, -0.625D, -0.5D),
                (float) Math.PI * 0.5F, true, diodeBaseBounds(0),
                diodeCableParts(true, true, false, true, true, true, 0.0F, false));
    }

    public static DiodeRenderPlan diodeWorldPlan(int metadata, double x, double y, double z,
            boolean posX, boolean negX, boolean posY, boolean negY, boolean posZ, boolean negZ) {
        return new DiodeRenderPlan(metadata, new TranslationPlan(x + 0.5D, y + 0.5D, z + 0.5D),
                0.0F, true, diodeBaseBounds(metadata),
                diodeCableParts(posX, negX, posY, negY, negZ, posZ, 0.0F, true));
    }

    public static ConveyorRenderPlan conveyorInventoryPlan() {
        return new ConveyorRenderPlan(2, 2, false, new TranslationPlan(-0.5D, -0.25D, -0.5D),
                true, List.of(conveyorBeltCuboid(conveyorUvRotations(2, false))));
    }

    public static ConveyorRenderPlan conveyorWorldPlan(int metadata) {
        ConveyorMetadataPlan metadataPlan = conveyorMetadataPlan(metadata);
        return new ConveyorRenderPlan(metadata, metadataPlan.normalizedMetadata(), metadataPlan.bent(),
                new TranslationPlan(0.0D, 0.0D, 0.0D), true,
                List.of(conveyorBeltCuboid(conveyorUvRotations(metadataPlan.normalizedMetadata(), metadataPlan.bent()))));
    }

    public static ConveyorChuteRenderPlan conveyorChuteInventoryPlan() {
        UvRotationPlan beltUv = conveyorUvRotations(2, false);
        List<CuboidUvPlan> belt = List.of(
                cuboid("belt_center", bounds(0.25D, 0.0D, 0.0D, 0.75D, 0.25D, 1.0D),
                        "block", 2, beltUv, false),
                cuboid("belt_west", bounds(0.0D, 0.0D, 0.25D, 0.25D, 0.25D, 0.75D),
                        "block", 2, beltUv, false),
                cuboid("belt_east", bounds(0.75D, 0.0D, 0.25D, 1.0D, 0.25D, 0.75D),
                        "block", 2, beltUv, false));
        return new ConveyorChuteRenderPlan(2, new TranslationPlan(-0.5D, -0.5D, -0.5D),
                true, true, false, false, false, false, false,
                false, belt, chuteFrameCuboids(), chuteGlassCuboids(true, false, false, false, false));
    }

    public static ConveyorChuteRenderPlan conveyorChuteWorldPlan(int metadata, boolean hasBelow,
            boolean belowConveyorOrEnterable, boolean negXConveyor, boolean posXConveyor,
            boolean negZConveyor, boolean posZConveyor) {
        boolean belt = hasBelow && !belowConveyorOrEnterable;
        List<CuboidUvPlan> beltCuboids = new ArrayList<>();
        if (belt) {
            UvRotationPlan beltUv = conveyorUvRotations(metadata, false);
            beltCuboids.add(cuboid("belt_center", bounds(0.25D, 0.0D, 0.0D, 0.75D, 0.25D, 1.0D),
                    "block", metadata, beltUv, false));
            beltCuboids.add(cuboid("belt_west", bounds(0.0D, 0.0D, 0.25D, 0.25D, 0.25D, 0.75D),
                    "block", metadata, beltUv, false));
            beltCuboids.add(cuboid("belt_east", bounds(0.75D, 0.0D, 0.25D, 1.0D, 0.25D, 0.75D),
                    "block", metadata, beltUv, false));
        } else if (hasBelow) {
            if (negXConveyor) {
                beltCuboids.add(cuboid("belt_stub_neg_x", bounds(0.0D, 0.0D, 0.25D, 0.125D, 0.25D, 0.75D),
                        "block", metadata, uv(1, 0, 0, 0, 0, 0), false));
            }
            if (posXConveyor) {
                beltCuboids.add(cuboid("belt_stub_pos_x", bounds(0.875D, 0.0D, 0.25D, 1.0D, 0.25D, 0.75D),
                        "block", metadata, uv(2, 0, 0, 0, 0, 0), false));
            }
            if (negZConveyor) {
                beltCuboids.add(cuboid("belt_stub_neg_z", bounds(0.25D, 0.0D, 0.0D, 0.75D, 0.25D, 0.125D),
                        "block", metadata, uv(3, 0, 0, 0, 0, 0), false));
            }
            if (posZConveyor) {
                beltCuboids.add(cuboid("belt_stub_pos_z", bounds(0.25D, 0.0D, 0.875D, 0.75D, 0.25D, 1.0D),
                        "block", metadata, uv(0, 0, 0, 0, 0, 0), false));
            }
        }
        return new ConveyorChuteRenderPlan(metadata, new TranslationPlan(0.0D, 0.0D, 0.0D),
                true, belt, hasBelow, belowConveyorOrEnterable, negXConveyor, posXConveyor, negZConveyor,
                posZConveyor, beltCuboids, chuteFrameCuboids(),
                chuteGlassCuboids(false, belt, negXConveyor, posXConveyor, negZConveyor, posZConveyor, metadata));
    }

    public static ConveyorLiftRenderPlan conveyorLiftWorldPlan(int metadata, boolean hasBelow,
            boolean belowConveyor, boolean hasAbove, boolean aboveConveyor, boolean aboveEnterable) {
        boolean bottom = hasBelow && !belowConveyor;
        boolean top = hasAbove && !aboveConveyor && !bottom && !aboveEnterable;
        List<CuboidUvPlan> cuboids = new ArrayList<>();
        if (bottom) {
            cuboids.addAll(conveyorLiftBottomBeltCuboids(metadata));
        }
        cuboids.addAll(conveyorLiftWallCuboids(metadata, top));
        if (bottom) {
            cuboids.add(cuboid("bottom_iron_cap",
                    bounds(0.25D + (metadata == 5 ? 0.125D : 0.0D), 0.0D,
                            0.25D + (metadata == 3 ? 0.125D : 0.0D),
                            0.75D - (metadata == 4 ? 0.125D : 0.0D), 0.25D,
                            0.75D - (metadata == 2 ? 0.125D : 0.0D)),
                    "iron_block", 0, uv(), true));
        }
        return new ConveyorLiftRenderPlan(metadata, bottom, top, hasBelow, hasAbove,
                belowConveyor, aboveConveyor, aboveEnterable, false, cuboids);
    }

    public static BoxDuctRenderPlan fluidBoxDuctInventoryPlan(int metadata, int rectifiedType) {
        DuctBoundsPlan bounds = fluidBoxDuctBounds(metadata);
        return new BoxDuctRenderPlan("fluid_box", metadata, rectifiedType,
                new TranslationPlan(-0.5D, -0.5D, -0.5D), emptyConnections(),
                bounds.lower(), bounds.upper(), bounds.junctionLower(), bounds.junctionUpper(),
                0xFFFFFF, false, true,
                List.of(faceCuboid("inventory_z", bounds(bounds.lower(), bounds.lower(), 0.0D,
                                bounds.upper(), bounds.upper(), 1.0D),
                        "iconStraight", "iconStraight", "iconEnd", "iconEnd",
                        "iconStraight", "iconStraight", rectifiedType,
                        uv(0, 0, 1, 2, 0, 0), false)),
                List.of());
    }

    public static BoxDuctRenderPlan powerBoxDuctInventoryPlan(int metadata) {
        DuctBoundsPlan bounds = powerBoxDuctBounds(metadata);
        return new BoxDuctRenderPlan("power_box", metadata, metadata,
                new TranslationPlan(-0.5D, -0.5D, -0.5D), emptyConnections(),
                bounds.lower(), bounds.upper(), bounds.junctionLower(), bounds.junctionUpper(),
                0xFFFFFF, false, true,
                List.of(faceCuboid("inventory_z", bounds(bounds.lower(), bounds.lower(), 0.0D,
                                bounds.upper(), bounds.upper(), 1.0D),
                        "iconStraight", "iconStraight", "iconEnd", "iconEnd",
                        "iconStraight", "iconStraight", metadata,
                        uv(0, 0, 1, 2, 0, 0), false)),
                List.of());
    }

    public static BoxDuctRenderPlan fluidBoxDuctWorldPlan(int metadata, boolean hasPipeType, int fluidColor,
            boolean posX, boolean negX, boolean posY, boolean negY, boolean posZ, boolean negZ) {
        ConnectionMaskPlan connections = connections(posX, negX, posY, negY, posZ, negZ);
        DuctBoundsPlan bounds = fluidBoxDuctBounds(metadata);
        boolean lightened = hasPipeType && metadata % 3 == 2;
        int cachedColor = lightened ? lightenColor(fluidColor, 0.25D) : 0xFFFFFF;
        return new BoxDuctRenderPlan("fluid_box", metadata, metadata, new TranslationPlan(0.0D, 0.0D, 0.0D),
                connections, bounds.lower(), bounds.upper(), bounds.junctionLower(), bounds.junctionUpper(),
                cachedColor, lightened, true, List.of(), boxDuctWorldCuboids(connections, bounds));
    }

    public static BoxDuctRenderPlan powerBoxDuctWorldPlan(int metadata,
            boolean posX, boolean negX, boolean posY, boolean negY, boolean posZ, boolean negZ) {
        ConnectionMaskPlan connections = connections(posX, negX, posY, negY, posZ, negZ);
        DuctBoundsPlan bounds = powerBoxDuctBounds(metadata);
        return new BoxDuctRenderPlan("power_box", metadata, metadata, new TranslationPlan(0.0D, 0.0D, 0.0D),
                connections, bounds.lower(), bounds.upper(), bounds.junctionLower(), bounds.junctionUpper(),
                0xFFFFFF, false, true, List.of(), boxDuctWorldCuboids(connections, bounds));
    }

    public static ObjConnectionRenderPlan cableNeoInventoryPlan(boolean overrideTexture) {
        return new ObjConnectionRenderPlan("cable_neo", 0, new TranslationPlan(0.0D, 0.0D, 0.0D),
                (float) Math.PI, 0.0F, 1.25F, false, overrideTexture, true, emptyConnections(),
                cableNeoParts(false, "Core", "posX", "negX", "posZ", "negZ"));
    }

    public static ObjConnectionRenderPlan cableNeoWorldPlan(boolean overrideTexture,
            boolean posX, boolean negX, boolean posY, boolean negY, boolean posZ, boolean negZ) {
        ConnectionMaskPlan connections = connections(posX, negX, posY, negY, posZ, negZ);
        return new ObjConnectionRenderPlan("cable_neo", 0, new TranslationPlan(0.5D, 0.5D, 0.5D),
                0.0F, 0.0F, 1.0F, true, overrideTexture, true, connections,
                cableNeoWorldParts(connections, false));
    }

    public static ObjConnectionRenderPlan detCordInventoryPlan(boolean overrideTexture) {
        return new ObjConnectionRenderPlan("cable_neo", 0, new TranslationPlan(0.0D, 0.0D, 0.0D),
                (float) Math.PI, 0.0F, 1.25F, false, overrideTexture, true, emptyConnections(),
                cableNeoParts(false, "CZ"));
    }

    public static ObjConnectionRenderPlan detCordWorldPlan(boolean overrideTexture,
            boolean posX, boolean negX, boolean posY, boolean negY, boolean posZ, boolean negZ) {
        ConnectionMaskPlan connections = connections(posX, negX, posY, negY, posZ, negZ);
        return new ObjConnectionRenderPlan("cable_neo", 0, new TranslationPlan(0.5D, 0.5D, 0.5D),
                0.0F, 0.0F, 1.0F, true, overrideTexture, true, connections,
                cableNeoWorldParts(connections, true));
    }

    public static PipeNeoRenderPlan pipeNeoInventoryPlan(int metadata, int emptyFluidColor, boolean overrideTexture) {
        return new PipeNeoRenderPlan(metadata, new TranslationPlan(0.0D, 0.0D, 0.0D),
                (float) Math.PI, 0.0F, 1.25F, false, overrideTexture, true, emptyConnections(),
                emptyFluidColor, pipeNeoLayeredParts(metadata, emptyFluidColor, false,
                List.of("pX", "nX", "pZ", "nZ")));
    }

    public static PipeNeoRenderPlan pipeNeoWorldPlan(int metadata, int fluidColor, boolean overrideTexture,
            boolean posX, boolean negX, boolean posY, boolean negY, boolean posZ, boolean negZ) {
        ConnectionMaskPlan connections = connections(posX, negX, posY, negY, posZ, negZ);
        return new PipeNeoRenderPlan(metadata, new TranslationPlan(0.5D, 0.5D, 0.5D),
                0.0F, 0.0F, 1.0F, true, overrideTexture, true, connections,
                fluidColor, pipeNeoWorldParts(metadata, fluidColor, connections));
    }

    public static LegacyPipeModelRenderPlan legacyPipeInventoryPlan(int renderType) {
        return new LegacyPipeModelRenderPlan(renderType, 0, new TranslationPlan(0.0D, 0.0D, 0.0D),
                0.0F, 0.0F, false, true, legacyPipeParts(renderType, 0.0F, 0.0F, false));
    }

    public static LegacyPipeModelRenderPlan legacyPipeWorldPlan(int renderType, int metadata,
            double x, double y, double z) {
        OrientationPlan orientation = legacyPipeOrientation(metadata);
        return new LegacyPipeModelRenderPlan(renderType, metadata, new TranslationPlan(x + 0.5D, y + 0.5D, z + 0.5D),
                orientation.yawRadians(), orientation.pitchRadians(), true, true,
                legacyPipeParts(renderType, orientation.yawRadians(), orientation.pitchRadians(), true));
    }

    public static PneumoTubeRenderPlan pneumoTubeInventoryPlan() {
        return new PneumoTubeRenderPlan(new TranslationPlan(-0.5D, -0.5D, -0.5D), emptyConnections(),
                false, null, null, List.of(), true,
                List.of(faceCuboid("inventory_z", bounds(0.3125D, 0.3125D, 0.0D, 0.6875D, 0.6875D, 1.0D),
                        "iconStraight", "iconStraight", "iconConnector", "iconConnector",
                        "iconStraight", "iconStraight", 0, uv(2, 1, 0, 0, 0, 0), false)));
    }

    public static PneumoTubeRenderPlan pneumoTubeWorldPlan(boolean posX, boolean negX, boolean posY,
            boolean negY, boolean posZ, boolean negZ, boolean compressorOrEndpoint,
            Direction insertionDirection, Direction ejectionDirection, List<Direction> airConnectors) {
        ConnectionMaskPlan connections = connections(posX, negX, posY, negY, posZ, negZ);
        List<FaceIconCuboidPlan> cuboids = new ArrayList<>(pneumoTubeCoreCuboids(connections, compressorOrEndpoint));
        if (insertionDirection != null) {
            cuboids.addAll(pneumoTubeConnectorCuboids(insertionDirection, "iconIn"));
        }
        if (ejectionDirection != null) {
            cuboids.addAll(pneumoTubeConnectorCuboids(ejectionDirection, "iconOut"));
        }
        for (Direction direction : airConnectors) {
            cuboids.addAll(pneumoTubeConnectorCuboids(direction, "iconConnector"));
        }
        return new PneumoTubeRenderPlan(new TranslationPlan(0.0D, 0.0D, 0.0D), connections,
                compressorOrEndpoint, insertionDirection, ejectionDirection, List.copyOf(airConnectors),
                true, List.copyOf(cuboids));
    }

    public static FoundryOpenVesselRenderPlan foundryBasinInventoryPlan() {
        return new FoundryOpenVesselRenderPlan("basin", new TranslationPlan(-0.5D, -0.5D, -0.5D),
                null, true, false, foundryOpenVesselFaces(-0.875D),
                foundryColorPlan(0xFFFFFF, false), List.of());
    }

    public static FoundryOpenVesselRenderPlan foundryBasinWorldPlan(int colorMultiplier, boolean anaglyph) {
        return new FoundryOpenVesselRenderPlan("basin", new TranslationPlan(0.0D, 0.0D, 0.0D),
                null, true, true, foundryOpenVesselFaces(-0.875D),
                foundryColorPlan(colorMultiplier, anaglyph), List.of());
    }

    public static FoundryOpenVesselRenderPlan foundryMoldInventoryPlan() {
        return new FoundryOpenVesselRenderPlan("mold", new TranslationPlan(-0.5D, -0.5D, -0.5D),
                bounds(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D), true, false,
                foundryOpenVesselFaces(-0.375D), foundryColorPlan(0xFFFFFF, false), List.of());
    }

    public static FoundryOpenVesselRenderPlan foundryMoldWorldPlan(int colorMultiplier, boolean anaglyph) {
        return new FoundryOpenVesselRenderPlan("mold", new TranslationPlan(0.0D, 0.0D, 0.0D),
                null, true, true, foundryOpenVesselFaces(-0.375D),
                foundryColorPlan(colorMultiplier, anaglyph), List.of());
    }

    public static FoundryOutletRenderPlan foundryOutletInventoryPlan() {
        return new FoundryOutletRenderPlan(3, new TranslationPlan(-0.5D, -0.5D, -0.5D),
                foundryOutletBounds(3), false, false, true, false,
                foundryColorPlan(0xFFFFFF, false), foundryOutletInventoryFaces());
    }

    public static FoundryOutletRenderPlan foundryOutletWorldPlan(int metadata, boolean hasFilter, boolean closed,
            int colorMultiplier, boolean anaglyph) {
        return foundryOutletPlan(metadata, hasFilter, closed, new TranslationPlan(0.0D, 0.0D, 0.0D),
                colorMultiplier, anaglyph, true);
    }

    public static FoundryTankRenderPlan foundryTankWorldPlan(int colorMultiplier, boolean anaglyph,
            boolean connectedPosX, boolean connectedNegX, boolean connectedPosZ, boolean connectedNegZ,
            boolean connectedPosY, boolean connectedNegY, boolean outletPosX, boolean outletNegX,
            boolean outletPosZ, boolean outletNegZ, int amount, int capacity, int moltenColor) {
        FoundryConnectionPlan connections = new FoundryConnectionPlan(connectedPosX, connectedNegX,
                connectedPosY, connectedNegY, connectedPosZ, connectedNegZ);
        FoundryOutletConnectionPlan outlets = new FoundryOutletConnectionPlan(outletPosX, outletNegX,
                outletPosZ, outletNegZ);
        FoundryFluidStatePlan fluid = foundryTankFluidState(amount, capacity, moltenColor, connectedNegY, connectedPosY);
        return new FoundryTankRenderPlan(new TranslationPlan(0.0D, 0.0D, 0.0D), connections, outlets,
                foundryColorPlan(colorMultiplier, anaglyph), fluid,
                foundryTankShellFaces(connections, outlets), foundryTankFluidSurfaces(connections, fluid));
    }

    public static FoundryChannelRenderPlan foundryChannelInventoryPlan() {
        FoundryConnectionPlan connections = new FoundryConnectionPlan(true, true, false, false, true, true);
        return new FoundryChannelRenderPlan(new TranslationPlan(-0.5D, -0.5D, -0.5D), connections,
                true, true, foundryColorPlan(0xFFFFFF, false),
                new FoundryFluidStatePlan(false, 0, 0, 0, 0.0D, 0.0D, 0xFFFFFF, 0.125D),
                foundryChannelShellFaces(connections), List.of());
    }

    public static FoundryChannelRenderPlan foundryChannelWorldPlan(int colorMultiplier, boolean anaglyph,
            boolean posX, boolean negX, boolean posZ, boolean negZ, int amount, int capacity, int moltenColor) {
        FoundryConnectionPlan connections = new FoundryConnectionPlan(posX, negX, false, false, posZ, negZ);
        FoundryFluidStatePlan fluid = foundryChannelFluidState(amount, capacity, moltenColor);
        return new FoundryChannelRenderPlan(new TranslationPlan(0.0D, 0.0D, 0.0D), connections,
                true, true, foundryColorPlan(colorMultiplier, anaglyph), fluid,
                foundryChannelShellFaces(connections), foundryChannelFluidSurfaces(connections, fluid));
    }

    public static PartitionerRenderPlan partitionerInventoryPlan() {
        return new PartitionerRenderPlan(0, OBJ_INVENTORY_TRANSLATION, (float) Math.PI * 0.5F,
                0.0F, false, true, partitionerParts(0.0F, false));
    }

    public static PartitionerRenderPlan partitionerWorldPlan(int metadata, double x, double y, double z) {
        float rotation = partitionerRotation(metadata);
        return new PartitionerRenderPlan(metadata, new TranslationPlan(x + 0.5D, y, z + 0.5D), 0.0F,
                rotation, true, true, partitionerParts(rotation, true));
    }

    public static SpotlightPartRenderPlan spotlightInventoryPlan(SpotlightModelKind kind, boolean overrideTexture) {
        return spotlightInventoryPlan(kind.legacyResourceModel(), kind.partName(0), overrideTexture);
    }

    public static SpotlightPartRenderPlan spotlightInventoryPlan(String legacyResourceModel, String partName,
            boolean overrideTexture) {
        return new SpotlightPartRenderPlan(legacyResourceModel, partName, 0,
                new TranslationPlan(0.0D, 0.0D, 0.0D), (float) Math.PI * -0.5F,
                0.0F, 0.0F, 1.5F, false, overrideTexture, true,
                Direction.EAST, 0, null, List.of());
    }

    public static SpotlightPartRenderPlan spotlightWorldPlan(SpotlightModelKind kind, Direction direction,
            List<Direction> connectableDirections, double x, double y, double z) {
        SpotlightConnectionPlan connectionPlan = spotlightConnectionPlan(direction, connectableDirections);
        return spotlightWorldPlan(kind.legacyResourceModel(), kind.partName(connectionPlan.connectionCount()),
                direction, connectionPlan, x, y, z);
    }

    public static SpotlightPartRenderPlan spotlightWorldPlan(String legacyResourceModel, List<String> partNames,
            Direction direction, List<Direction> connectableDirections, double x, double y, double z) {
        SpotlightConnectionPlan connectionPlan = spotlightConnectionPlan(direction, connectableDirections);
        String partName = partNames.get(Math.min(connectionPlan.connectionCount(), partNames.size() - 1));
        return spotlightWorldPlan(legacyResourceModel, partName, direction, connectionPlan, x, y, z);
    }

    public static PedestalRenderPlan pedestalPlan(int metadata) {
        return new PedestalRenderPlan(metadata, pedestalBounds(), PEDESTAL_INVENTORY_TRANSLATION,
                true, true, LegacyBlockRenderHelper.STANDARD_INVENTORY_FACE_ORDER);
    }

    public static List<LegacyAtlasCuboidRenderer.CuboidBounds> pedestalBounds() {
        return List.of(
                new LegacyAtlasCuboidRenderer.CuboidBounds(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D),
                new LegacyAtlasCuboidRenderer.CuboidBounds(0.0D, 0.75D, 0.0D, 1.0D, 1.0D, 1.0D),
                new LegacyAtlasCuboidRenderer.CuboidBounds(0.125D, 0.25D, 0.125D, 0.875D, 0.75D, 0.875D));
    }

    public static AnvilRenderPlan anvilInventoryPlan() {
        return new AnvilRenderPlan(0, OBJ_INVENTORY_TRANSLATION, (float) Math.PI, 0.0F,
                false, true, anvilParts(0.0F, false));
    }

    public static AnvilRenderPlan anvilWorldPlan(int metadata, double x, double y, double z) {
        float rotation = tapeRotation(metadata);
        return new AnvilRenderPlan(metadata, new TranslationPlan(x + 0.5D, y, z + 0.5D), rotation, 0.0F,
                true, true, anvilParts(rotation, true));
    }

    public static ObjIconModelRenderPlan barbedWireInventoryPlan(boolean overrideTexture) {
        return new ObjIconModelRenderPlan("barbed_wire", 0, OBJ_INVENTORY_TRANSLATION,
                0.0F, 0.0F, false, overrideTexture, true);
    }

    public static ObjIconModelRenderPlan barbedWireWorldPlan(int metadata, double x, double y, double z, boolean overrideTexture) {
        float rotation = metadata < 4 ? (float) Math.PI * -0.5F : (float) -Math.PI;
        return new ObjIconModelRenderPlan("barbed_wire", metadata, new TranslationPlan(x + 0.5D, y, z + 0.5D),
                rotation, 0.0F, true, overrideTexture, true);
    }

    public static ObjIconModelRenderPlan tapeRecorderInventoryPlan(boolean overrideTexture) {
        return tapeInventoryPlan("taperecorder", overrideTexture);
    }

    public static ObjIconModelRenderPlan steelPoleInventoryPlan(boolean overrideTexture) {
        return tapeInventoryPlan("pole", overrideTexture);
    }

    public static ObjIconModelRenderPlan tapeRecorderWorldPlan(int metadata, double x, double y, double z, boolean overrideTexture) {
        return new ObjIconModelRenderPlan("taperecorder", metadata, new TranslationPlan(x + 0.5D, y, z + 0.5D),
                tapeRotation(metadata), 0.0F, true, overrideTexture, true);
    }

    public static ObjIconModelRenderPlan steelPoleWorldPlan(int metadata, double x, double y, double z, boolean overrideTexture) {
        return new ObjIconModelRenderPlan("pole", metadata, new TranslationPlan(x + 0.5D, y, z + 0.5D),
                tapeRotation(metadata) - (float) Math.PI * 0.5F, 0.0F, true, overrideTexture, true);
    }

    public static MetalFenceRenderPlan metalFencePlan(int metadata, boolean xNeg, boolean xPos, boolean zNeg, boolean zPos) {
        boolean hasX = xNeg || xPos;
        boolean hasZ = zNeg || zPos;
        boolean straightX = !hasZ && xNeg && xPos;
        boolean straightZ = !hasX && zNeg && zPos;
        boolean showPost = metadata == 1 || (!straightX && !straightZ);
        boolean forcedX = false;
        if (!hasX && !hasZ) {
            hasX = true;
            forcedX = true;
        }

        double minX = xNeg ? 0.0D : FENCE_CENTER_MIN;
        double maxX = xPos ? 1.0D : FENCE_CENTER_MAX;
        double minZ = zNeg ? 0.0D : FENCE_CENTER_MIN;
        double maxZ = zPos ? 1.0D : FENCE_CENTER_MAX;
        LegacyAtlasCuboidRenderer.CuboidBounds xBounds = new LegacyAtlasCuboidRenderer.CuboidBounds(
                minX, 0.0D, 0.5D, maxX, 1.0D, 0.5D);
        LegacyAtlasCuboidRenderer.CuboidBounds zBounds = new LegacyAtlasCuboidRenderer.CuboidBounds(
                0.5D, 0.0D, minZ, 0.5D, 1.0D, maxZ);
        LegacyAtlasCuboidRenderer.CuboidBounds postBounds = new LegacyAtlasCuboidRenderer.CuboidBounds(
                FENCE_POST_MIN, 0.0D, FENCE_POST_MIN, FENCE_POST_MAX, 1.0D, FENCE_POST_MAX);
        return new MetalFenceRenderPlan(metadata, xNeg, xPos, zNeg, zPos, hasX, hasZ,
                straightX, straightZ, showPost, forcedX, true, xBounds, zBounds, postBounds);
    }

    private static ObjIconModelRenderPlan tapeInventoryPlan(String legacyResourceModel, boolean overrideTexture) {
        return new ObjIconModelRenderPlan(legacyResourceModel, 0, OBJ_INVENTORY_TRANSLATION,
                0.0F, 0.0F, false, overrideTexture, true);
    }

    private static ConnectionMaskPlan emptyConnections() {
        return connections(false, false, false, false, false, false);
    }

    private static ConnectionMaskPlan connections(boolean posX, boolean negX, boolean posY,
            boolean negY, boolean posZ, boolean negZ) {
        int mask = (posX ? 32 : 0) + (negX ? 16 : 0) + (posY ? 8 : 0)
                + (negY ? 4 : 0) + (posZ ? 2 : 0) + (negZ ? 1 : 0);
        int count = (posX ? 1 : 0) + (negX ? 1 : 0) + (posY ? 1 : 0)
                + (negY ? 1 : 0) + (posZ ? 1 : 0) + (negZ ? 1 : 0);
        return new ConnectionMaskPlan(posX, negX, posY, negY, posZ, negZ, mask, count);
    }

    private static DuctBoundsPlan fluidBoxDuctBounds(int metadata) {
        double lower = 0.125D;
        double upper = 0.875D;
        double junctionLower = 0.0625D;
        double junctionUpper = 0.9375D;
        for (int i = 2; i < 13; i += 3) {
            if (metadata > i) {
                lower += 0.0625D;
                upper -= 0.0625D;
                junctionLower += 0.0625D;
                junctionUpper -= 0.0625D;
            }
        }
        return new DuctBoundsPlan(lower, upper, junctionLower, junctionUpper);
    }

    private static DuctBoundsPlan powerBoxDuctBounds(int metadata) {
        double lower = 0.125D;
        double upper = 0.875D;
        double junctionLower = lower;
        double junctionUpper = upper;
        for (int i = 0; i < 5; i++) {
            if (metadata > i) {
                lower += 0.0625D;
                upper -= 0.0625D;
                junctionLower += 0.0625D;
                junctionUpper -= 0.0625D;
            }
        }
        return new DuctBoundsPlan(lower, upper, junctionLower, junctionUpper);
    }

    private static List<CuboidUvPlan> boxDuctWorldCuboids(ConnectionMaskPlan con, DuctBoundsPlan duct) {
        double lower = duct.lower();
        double upper = duct.upper();
        double junctionLower = duct.junctionLower();
        double junctionUpper = duct.junctionUpper();
        List<CuboidUvPlan> cuboids = new ArrayList<>();
        if ((con.mask() & 0b001111) == 0 && con.mask() > 0) {
            cuboids.add(cuboid("straight_x", bounds(0.0D, lower, lower, 1.0D, upper, upper),
                    "block", 0, uv(1, 1, 0, 0, 2, 1), false));
        } else if ((con.mask() & 0b111100) == 0 && con.mask() > 0) {
            cuboids.add(cuboid("straight_z", bounds(lower, lower, 0.0D, upper, upper, 1.0D),
                    "block", 0, uv(0, 0, 1, 2, 0, 0), false));
        } else if ((con.mask() & 0b110011) == 0 && con.mask() > 0) {
            cuboids.add(cuboid("straight_y", bounds(lower, 0.0D, lower, upper, 1.0D, upper),
                    "block", 0, uv(), false));
        } else if (con.count() == 2) {
            UvRotationPlan curveUv = boxDuctCurveUv(con);
            cuboids.add(cuboid("curve_core", bounds(lower, lower, lower, upper, upper, upper),
                    "block", 0, curveUv, false));
            addDuctArms(cuboids, con, lower, upper, lower, upper);
        } else {
            cuboids.add(cuboid("junction_core", bounds(junctionLower, junctionLower, junctionLower,
                    junctionUpper, junctionUpper, junctionUpper), "block", 0, uv(), false));
            addDuctArms(cuboids, con, lower, upper, junctionLower, junctionUpper);
        }
        return List.copyOf(cuboids);
    }

    private static UvRotationPlan boxDuctCurveUv(ConnectionMaskPlan con) {
        if ((con.negY() || con.posY()) && (con.posX() || con.negX())) {
            return uv(1, 1, 0, 0, 0, 0);
        }
        if (!con.negY() && !con.posY()) {
            return uv(0, 0, 1, 2, 2, 1);
        }
        return uv();
    }

    private static void addDuctArms(List<CuboidUvPlan> cuboids, ConnectionMaskPlan con,
            double lower, double upper, double junctionLower, double junctionUpper) {
        if (con.negY()) {
            cuboids.add(cuboid("arm_neg_y", bounds(lower, 0.0D, lower, upper, junctionLower, upper),
                    "block", 0, uv(), false));
        }
        if (con.posY()) {
            cuboids.add(cuboid("arm_pos_y", bounds(lower, junctionUpper, lower, upper, 1.0D, upper),
                    "block", 0, uv(), false));
        }
        if (con.negX()) {
            cuboids.add(cuboid("arm_neg_x", bounds(0.0D, lower, lower, junctionLower, upper, upper),
                    "block", 0, uv(), false));
        }
        if (con.posX()) {
            cuboids.add(cuboid("arm_pos_x", bounds(junctionUpper, lower, lower, 1.0D, upper, upper),
                    "block", 0, uv(), false));
        }
        if (con.negZ()) {
            cuboids.add(cuboid("arm_neg_z", bounds(lower, lower, 0.0D, upper, upper, junctionLower),
                    "block", 0, uv(), false));
        }
        if (con.posZ()) {
            cuboids.add(cuboid("arm_pos_z", bounds(lower, lower, junctionUpper, upper, upper, 1.0D),
                    "block", 0, uv(), false));
        }
    }

    private static List<NamedIconPartPlan> cableNeoParts(boolean shadeNormals, String... parts) {
        List<NamedIconPartPlan> plans = new ArrayList<>();
        for (String part : parts) {
            plans.add(new NamedIconPartPlan("cable_neo", part, "block", 0, 0, 0.0F, 0.0F, shadeNormals));
        }
        return List.copyOf(plans);
    }

    private static List<NamedIconPartPlan> cableNeoWorldParts(ConnectionMaskPlan con, boolean detCordRules) {
        if (detCordRules) {
            if (con.mask() == 0b110000 || con.mask() == 0b100000 || con.mask() == 0b010000) {
                return cableNeoParts(true, "CX");
            }
            if (con.mask() == 0b001100 || con.mask() == 0b001000 || con.mask() == 0b000100) {
                return cableNeoParts(true, "CY");
            }
            if (con.mask() == 0b000011 || con.mask() == 0b000010 || con.mask() == 0b000001) {
                return cableNeoParts(true, "CZ");
            }
        } else {
            if (con.posX() && con.negX() && !con.posY() && !con.negY() && !con.posZ() && !con.negZ()) {
                return cableNeoParts(true, "CX");
            }
            if (!con.posX() && !con.negX() && con.posY() && con.negY() && !con.posZ() && !con.negZ()) {
                return cableNeoParts(true, "CY");
            }
            if (!con.posX() && !con.negX() && !con.posY() && !con.negY() && con.posZ() && con.negZ()) {
                return cableNeoParts(true, "CZ");
            }
        }
        List<NamedIconPartPlan> parts = new ArrayList<>(cableNeoParts(true, "Core"));
        if (con.posX()) {
            parts.addAll(cableNeoParts(true, "posX"));
        }
        if (con.negX()) {
            parts.addAll(cableNeoParts(true, "negX"));
        }
        if (con.posY()) {
            parts.addAll(cableNeoParts(true, "posY"));
        }
        if (con.negY()) {
            parts.addAll(cableNeoParts(true, "negY"));
        }
        if (con.negZ()) {
            parts.addAll(cableNeoParts(true, "posZ"));
        }
        if (con.posZ()) {
            parts.addAll(cableNeoParts(true, "negZ"));
        }
        return List.copyOf(parts);
    }

    private static List<LayeredObjPartPlan> pipeNeoWorldParts(int metadata, int fluidColor,
            ConnectionMaskPlan con) {
        if (con.mask() == 0) {
            return pipeNeoLayeredParts(metadata, fluidColor, true, List.of("pX", "nX", "pY", "nY", "pZ", "nZ"));
        }
        if (con.mask() == 0b100000 || con.mask() == 0b010000) {
            return pipeNeoLayeredParts(metadata, fluidColor, true, List.of("pX", "nX"));
        }
        if (con.mask() == 0b001000 || con.mask() == 0b000100) {
            return pipeNeoLayeredParts(metadata, fluidColor, true, List.of("pY", "nY"));
        }
        if (con.mask() == 0b000010 || con.mask() == 0b000001) {
            return pipeNeoLayeredParts(metadata, fluidColor, true, List.of("pZ", "nZ"));
        }
        List<String> parts = new ArrayList<>();
        if (con.posX()) {
            parts.add("pX");
        }
        if (con.negX()) {
            parts.add("nX");
        }
        if (con.posY()) {
            parts.add("pY");
        }
        if (con.negY()) {
            parts.add("nY");
        }
        if (con.posZ()) {
            parts.add("nZ");
        }
        if (con.negZ()) {
            parts.add("pZ");
        }
        if (!con.posX() && !con.posY() && !con.posZ()) {
            parts.add("ppn");
        }
        if (!con.posX() && !con.posY() && !con.negZ()) {
            parts.add("ppp");
        }
        if (!con.negX() && !con.posY() && !con.posZ()) {
            parts.add("npn");
        }
        if (!con.negX() && !con.posY() && !con.negZ()) {
            parts.add("npp");
        }
        if (!con.posX() && !con.negY() && !con.posZ()) {
            parts.add("pnn");
        }
        if (!con.posX() && !con.negY() && !con.negZ()) {
            parts.add("pnp");
        }
        if (!con.negX() && !con.negY() && !con.posZ()) {
            parts.add("nnn");
        }
        if (!con.negX() && !con.negY() && !con.negZ()) {
            parts.add("nnp");
        }
        return pipeNeoLayeredParts(metadata, fluidColor, true, parts);
    }

    private static List<LayeredObjPartPlan> pipeNeoLayeredParts(int metadata, int fluidColor,
            boolean shadeNormals, List<String> parts) {
        List<LayeredObjPartPlan> plans = new ArrayList<>();
        for (String part : parts) {
            plans.add(new LayeredObjPartPlan("pipe_neo", part, "base", "overlay",
                    0, 1, metadata, fluidColor, 0.0F, 0.0F, shadeNormals));
        }
        return List.copyOf(plans);
    }

    private static OrientationPlan legacyPipeOrientation(int metadata) {
        if (metadata == 8) {
            return new OrientationPlan((float) Math.PI * 0.5F, (float) Math.PI * 0.5F);
        }
        if (metadata == 4) {
            return new OrientationPlan(0.0F, (float) Math.PI * 0.5F);
        }
        return new OrientationPlan(0.0F, 0.0F);
    }

    private static List<NamedIconPartPlan> legacyPipeParts(int renderType, float yawRadians,
            float pitchRadians, boolean shadeNormals) {
        String model = switch (renderType) {
            case 1, 3 -> "pipe_rim";
            case 2 -> "pipe_quad";
            default -> "pipe";
        };
        List<NamedIconPartPlan> parts = new ArrayList<>();
        parts.add(new NamedIconPartPlan(model, "Top", "top", 0, 0, yawRadians, pitchRadians, shadeNormals));
        parts.add(new NamedIconPartPlan(model, "Side", "side", 4, 0, yawRadians, pitchRadians, shadeNormals));
        if (renderType == 3) {
            parts.add(new NamedIconPartPlan("pipe_frame", "Frame", "frame", 0, 0,
                    yawRadians, pitchRadians, shadeNormals));
            parts.add(new NamedIconPartPlan("pipe_frame", "Mesh", "mesh", 0, 0,
                    yawRadians, pitchRadians, shadeNormals));
        }
        return List.copyOf(parts);
    }

    private static List<FaceIconCuboidPlan> pneumoTubeCoreCuboids(ConnectionMaskPlan con,
            boolean compressorOrEndpoint) {
        double lower = 0.3125D;
        double upper = 0.6875D;
        if (con.mask() == 0b110000 && !compressorOrEndpoint) {
            return List.of(faceCuboid("straight_x", bounds(0.0D, lower, lower, 1.0D, upper, upper),
                    "iconStraight", "iconStraight", "iconStraight", "iconStraight",
                    "iconStraight", "iconStraight", 0, uv(), false));
        }
        if (con.mask() == 0b000011 && !compressorOrEndpoint) {
            return List.of(faceCuboid("straight_z", bounds(lower, lower, 0.0D, upper, upper, 1.0D),
                    "iconStraight", "iconStraight", "iconStraight", "iconStraight",
                    "iconStraight", "iconStraight", 0, uv(2, 1, 0, 0, 0, 0), false));
        }
        if (con.mask() == 0b001100 && !compressorOrEndpoint) {
            return List.of(faceCuboid("straight_y", bounds(lower, 0.0D, lower, upper, 1.0D, upper),
                    "iconStraight", "iconStraight", "iconStraight", "iconStraight",
                    "iconStraight", "iconStraight", 0, uv(0, 0, 2, 2, 2, 2), false));
        }
        List<FaceIconCuboidPlan> cuboids = new ArrayList<>();
        cuboids.add(faceCuboid("core", bounds(lower, lower, lower, upper, upper, upper),
                "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon",
                0, uv(), false));
        if (con.posX()) {
            cuboids.add(faceCuboid("arm_pos_x", bounds(upper, lower, lower, 1.0D, upper, upper),
                    "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", 0, uv(), false));
        }
        if (con.negX()) {
            cuboids.add(faceCuboid("arm_neg_x", bounds(0.0D, lower, lower, lower, upper, upper),
                    "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", 0, uv(), false));
        }
        if (con.posY()) {
            cuboids.add(faceCuboid("arm_pos_y", bounds(lower, upper, lower, upper, 1.0D, upper),
                    "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", 0, uv(), false));
        }
        if (con.negY()) {
            cuboids.add(faceCuboid("arm_neg_y", bounds(lower, 0.0D, lower, upper, lower, upper),
                    "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", 0, uv(), false));
        }
        if (con.posZ()) {
            cuboids.add(faceCuboid("arm_pos_z", bounds(lower, lower, upper, upper, upper, 1.0D),
                    "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", 0, uv(), false));
        }
        if (con.negZ()) {
            cuboids.add(faceCuboid("arm_neg_z", bounds(lower, lower, 0.0D, upper, upper, lower),
                    "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", 0, uv(), false));
        }
        return List.copyOf(cuboids);
    }

    private static List<FaceIconCuboidPlan> pneumoTubeConnectorCuboids(Direction direction, String iconRole) {
        double lower = 0.3125D;
        double upper = 0.6875D;
        double connectorLower = 0.25D;
        double connectorUpper = 0.75D;
        double neckLower = 0.25D;
        double neckUpper = 0.75D;
        return switch (direction) {
            case EAST -> List.of(
                    faceCuboid("connector_neck_pos_x", bounds(upper, lower, lower, connectorUpper, upper, upper),
                            "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", 0, uv(), false),
                    faceCuboid("connector_pos_x", bounds(connectorUpper, neckLower, neckLower, 1.0D, neckUpper, neckUpper),
                            iconRole, iconRole, iconRole, iconRole, iconRole, iconRole, 0, uv(), false));
            case WEST -> List.of(
                    faceCuboid("connector_neck_neg_x", bounds(connectorLower, lower, lower, lower, upper, upper),
                            "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", 0, uv(), false),
                    faceCuboid("connector_neg_x", bounds(0.0D, neckLower, neckLower, neckLower, neckUpper, neckUpper),
                            iconRole, iconRole, iconRole, iconRole, iconRole, iconRole, 0, uv(), false));
            case UP -> List.of(
                    faceCuboid("connector_neck_pos_y", bounds(lower, upper, lower, upper, connectorUpper, upper),
                            "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", 0, uv(), false),
                    faceCuboid("connector_pos_y", bounds(neckLower, connectorUpper, neckLower, neckUpper, 1.0D, neckUpper),
                            iconRole, iconRole, iconRole, iconRole, iconRole, iconRole, 0, uv(), false));
            case DOWN -> List.of(
                    faceCuboid("connector_neck_neg_y", bounds(lower, connectorLower, lower, upper, lower, upper),
                            "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", 0, uv(), false),
                    faceCuboid("connector_neg_y", bounds(neckLower, 0.0D, neckLower, neckUpper, neckLower, neckUpper),
                            iconRole, iconRole, iconRole, iconRole, iconRole, iconRole, 0, uv(), false));
            case SOUTH -> List.of(
                    faceCuboid("connector_neck_pos_z", bounds(lower, lower, upper, upper, upper, connectorUpper),
                            "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", 0, uv(), false),
                    faceCuboid("connector_pos_z", bounds(neckLower, neckLower, connectorUpper, neckUpper, neckUpper, 1.0D),
                            iconRole, iconRole, iconRole, iconRole, iconRole, iconRole, 0, uv(), false));
            case NORTH -> List.of(
                    faceCuboid("connector_neck_neg_z", bounds(lower, lower, connectorLower, upper, upper, lower),
                            "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", "baseIcon", 0, uv(), false),
                    faceCuboid("connector_neg_z", bounds(neckLower, neckLower, 0.0D, neckUpper, neckUpper, neckLower),
                            iconRole, iconRole, iconRole, iconRole, iconRole, iconRole, 0, uv(), false));
        };
    }

    private static SpotlightPartRenderPlan spotlightWorldPlan(String legacyResourceModel, String partName,
            Direction direction, SpotlightConnectionPlan connectionPlan, double x, double y, double z) {
        TranslationPlan offset = spotlightWorldOffset(direction);
        TranslationPlan translation = new TranslationPlan(x + offset.x(), y + offset.y(), z + offset.z());
        return new SpotlightPartRenderPlan(legacyResourceModel, partName, 0, translation,
                LegacyObjTransforms.yawRadians(direction), LegacyObjTransforms.pitchRadians(direction),
                connectionPlan.rollRadians(), 1.0F, false, false, true,
                direction, connectionPlan.connectionCount(), connectionPlan.connectionDirection(),
                connectionPlan.connectableDirections());
    }

    private static TranslationPlan spotlightWorldOffset(Direction direction) {
        return new TranslationPlan(
                0.5D - direction.getStepX() * 0.5D,
                0.5D - direction.getStepY() * 0.5D,
                0.5D - direction.getStepZ() * 0.5D);
    }

    public static SpotlightConnectionPlan spotlightConnectionPlan(Direction axis, List<Direction> connectableDirections) {
        Direction connectionDirection = null;
        int connectionCount = 0;
        for (Direction availableDirection : Direction.values()) {
            if (availableDirection == axis || availableDirection == axis.getOpposite()) {
                continue;
            }
            if (connectableDirections.contains(availableDirection)) {
                connectionDirection = availableDirection;
                connectionCount = 1;
                break;
            }
        }
        if (connectionDirection != null && connectableDirections.contains(connectionDirection.getOpposite())) {
            connectionCount++;
        }
        float roll = connectionDirection == null ? 0.0F : spotlightRoll(connectionDirection, axis);
        return new SpotlightConnectionPlan(axis, connectionDirection, connectionCount, roll,
                List.copyOf(connectableDirections));
    }

    public static float spotlightRoll(Direction direction, Direction axis) {
        float flipX = axis == Direction.DOWN || axis == Direction.NORTH || axis == Direction.WEST ? -0.5F : 0.5F;
        float addX = axis == Direction.NORTH || axis == Direction.SOUTH ? -0.5F : 0.0F;
        boolean flipNS = axis == Direction.WEST;
        return switch (direction) {
            case NORTH -> flipNS ? (float) Math.PI : 0.0F;
            case SOUTH -> !flipNS ? (float) Math.PI : 0.0F;
            case EAST -> (float) Math.PI * (flipX + addX);
            case WEST -> (float) Math.PI * (-flipX + addX);
            case UP -> (float) Math.PI * -0.5F;
            case DOWN -> (float) Math.PI * 0.5F;
        };
    }

    private static ScaledObjIconModelRenderPlan scaledObjPlan(String legacyResourceModel, int iconMetadata,
            TranslationPlan translation, float scale, float yawRadians, float pitchRadians,
            boolean shadow, boolean overrideTexture, boolean renders3DInInventory) {
        return new ScaledObjIconModelRenderPlan(legacyResourceModel, iconMetadata, translation,
                scale, scale, scale, yawRadians, pitchRadians, shadow, overrideTexture, renders3DInInventory);
    }

    private static float tapeRotation(int metadata) {
        return switch (metadata) {
            case 2 -> (float) Math.PI * 0.5F;
            case 3 -> (float) Math.PI * 1.5F;
            case 4 -> (float) Math.PI;
            default -> 0.0F;
        };
    }

    private static float horizontalMetadataRotation(int metadata) {
        return switch (metadata % 4) {
            case 1 -> 0.0F;
            case 2 -> (float) Math.PI * 1.5F;
            case 3 -> (float) Math.PI;
            default -> (float) Math.PI * 0.5F;
        };
    }

    private static float partitionerRotation(int metadata) {
        return switch (metadata) {
            case 2 -> (float) Math.PI * 0.5F;
            case 3 -> (float) Math.PI * 1.5F;
            case 4 -> (float) Math.PI;
            default -> 0.0F;
        };
    }

    private static OrientationPlan capacitorLikeOrientation(int metadata) {
        float pitch = metadata == 0 ? (float) Math.PI : 0.0F;
        float rotation = switch (metadata) {
            case 2 -> (float) Math.PI * 0.5F;
            case 3 -> (float) Math.PI * 1.5F;
            case 4 -> (float) Math.PI;
            default -> 0.0F;
        };
        if (rotation != 0.0F || metadata == 5) {
            pitch = (float) Math.PI * 0.5F;
        }
        return new OrientationPlan(rotation, pitch);
    }

    private static float splitterRotation(int metadata) {
        return switch (metadata) {
            case 4, 12 -> (float) Math.PI * 0.5F;
            case 5, 13 -> (float) Math.PI * 1.5F;
            case 3, 14 -> (float) Math.PI;
            default -> 0.0F;
        };
    }

    private static ConveyorMetadataPlan conveyorMetadataPlan(int metadata) {
        boolean bent = metadata > 5;
        int normalized = metadata;
        if (normalized > 9) {
            normalized -= 8;
        }
        if (normalized > 5) {
            normalized -= 4;
        }
        return new ConveyorMetadataPlan(metadata, normalized, bent);
    }

    private static UvRotationPlan conveyorUvRotations(int metadata, boolean bent) {
        return switch (metadata) {
            case 2 -> uv(3, 0, 0, 0, 0, bent ? 0 : 3);
            case 3 -> uv(0, 3, 0, 0, bent ? 0 : 3, 0);
            case 4 -> uv(1, 1, 0, bent ? 0 : 3, 0, 0);
            case 5 -> uv(2, 2, bent ? 0 : 3, 0, 0, 0);
            default -> uv();
        };
    }

    private static CuboidUvPlan conveyorBeltCuboid(UvRotationPlan uvRotations) {
        return cuboid("belt", bounds(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D),
                "block", 2, uvRotations, false);
    }

    private static List<CuboidUvPlan> chuteFrameCuboids() {
        return List.of(
                cuboid("frame_north_west", bounds(0.0D, 0.0D, 0.0D, 0.25D, 1.0D, 0.25D),
                        "concrete_smooth", 0, uv(), true),
                cuboid("frame_north_east", bounds(0.75D, 0.0D, 0.0D, 1.0D, 1.0D, 0.25D),
                        "concrete_smooth", 0, uv(), true),
                cuboid("frame_south_west", bounds(0.0D, 0.0D, 0.75D, 0.25D, 1.0D, 1.0D),
                        "concrete_smooth", 0, uv(), true),
                cuboid("frame_south_east", bounds(0.75D, 0.0D, 0.75D, 1.0D, 1.0D, 1.0D),
                        "concrete_smooth", 0, uv(), true));
    }

    private static List<CuboidUvPlan> chuteGlassCuboids(boolean inventory, boolean belt, boolean negXConveyor,
            boolean posXConveyor, boolean negZConveyor) {
        return chuteGlassCuboids(inventory, belt, negXConveyor, posXConveyor, negZConveyor, false, 2);
    }

    private static List<CuboidUvPlan> chuteGlassCuboids(boolean inventory, boolean belt, boolean negXConveyor,
            boolean posXConveyor, boolean negZConveyor, boolean posZConveyor, int metadata) {
        double minInner = 0.25D;
        double maxInner = 0.75D;
        double glassMin = 0.125D;
        double glassMax = 0.875D;
        double minY = belt ? 0.25D : 0.0D;
        List<CuboidUvPlan> panels = new ArrayList<>();
        if (inventory || (!negXConveyor && (!belt || metadata != 5))) {
            panels.add(cuboid("glass_west", bounds(glassMin, minY, minInner, glassMin, 1.0D, maxInner),
                    "steel_grate", 2, uv(), true));
        }
        if (inventory || (!posXConveyor && (!belt || metadata != 4))) {
            panels.add(cuboid("glass_east", bounds(glassMax, minY, minInner, glassMax, 1.0D, maxInner),
                    "steel_grate", 2, uv(), true));
        }
        if (inventory || (!negZConveyor && (!belt || metadata != 3))) {
            panels.add(cuboid("glass_north", bounds(minInner, minY, glassMin, maxInner, 1.0D, glassMin),
                    "steel_grate", 2, uv(), true));
        }
        if (!inventory && !posZConveyor && (!belt || metadata != 2)) {
            panels.add(cuboid("glass_south", bounds(minInner, minY, glassMax, maxInner, 1.0D, glassMax),
                    "steel_grate", 2, uv(), true));
        }
        return List.copyOf(panels);
    }

    private static List<CuboidUvPlan> conveyorLiftBottomBeltCuboids(int metadata) {
        List<CuboidUvPlan> cuboids = new ArrayList<>();
        if (metadata != 5) {
            cuboids.add(cuboid("bottom_belt_west", bounds(0.0D, 0.0D, 0.25D, 0.25D, 0.25D, 0.75D),
                    "block", metadata, uv(1, 1, 0, 0, 0, 0), true));
        }
        if (metadata != 4) {
            cuboids.add(cuboid("bottom_belt_east", bounds(0.75D, 0.0D, 0.25D, 1.0D, 0.25D, 0.75D),
                    "block", metadata, uv(2, 2, 0, 0, 0, 0), true));
        }
        if (metadata != 3) {
            cuboids.add(cuboid("bottom_belt_north", bounds(0.25D, 0.0D, 0.0D, 0.75D, 0.25D, 0.25D),
                    "block", metadata, uv(3, 0, 0, 0, 0, 0), true));
        }
        if (metadata != 2) {
            cuboids.add(cuboid("bottom_belt_south", bounds(0.25D, 0.0D, 0.75D, 0.75D, 0.25D, 1.0D),
                    "block", metadata, uv(0, 3, 0, 0, 0, 0), true));
        }
        return List.copyOf(cuboids);
    }

    private static List<CuboidUvPlan> conveyorLiftWallCuboids(int metadata, boolean top) {
        List<CuboidUvPlan> cuboids = new ArrayList<>();
        double minOuter = 0.0D;
        double maxOuter = 1.0D;
        double minInner = 0.25D;
        double maxInner = 0.75D;
        if (!top) {
            if (metadata == 2) {
                cuboids.add(cuboid("wall_north_west", bounds(minOuter, 0.0D, minOuter, minInner, 1.0D, minInner),
                        "concrete_smooth", 0, uv(), true));
                cuboids.add(cuboid("wall_north_east", bounds(maxInner, 0.0D, minOuter, maxOuter, 1.0D, minInner),
                        "concrete_smooth", 0, uv(), true));
                cuboids.add(cuboid("wall_south", bounds(minOuter, 0.0D, maxInner, maxOuter, 1.0D, maxOuter),
                        "concrete_smooth", 0, uv(), true));
                cuboids.add(cuboid("vertical_belt_south", bounds(minInner, 0.0D, maxInner - 0.125D, maxInner, 1.0D, maxInner),
                        "block", metadata, uv(3, 0, 0, 0, 0, 0), true));
            } else if (metadata == 3) {
                cuboids.add(cuboid("wall_north", bounds(minOuter, 0.0D, minOuter, maxOuter, 1.0D, minInner),
                        "concrete_smooth", 0, uv(), true));
                cuboids.add(cuboid("wall_south_west", bounds(minOuter, 0.0D, maxInner, minInner, 1.0D, maxOuter),
                        "concrete_smooth", 0, uv(), true));
                cuboids.add(cuboid("wall_south_east", bounds(maxInner, 0.0D, maxInner, maxOuter, 1.0D, maxOuter),
                        "concrete_smooth", 0, uv(), true));
                cuboids.add(cuboid("vertical_belt_north", bounds(minInner, 0.0D, minInner, maxInner, 1.0D, minInner + 0.125D),
                        "block", metadata, uv(0, 0, 0, 0, 0, 0), true));
            } else if (metadata == 4) {
                cuboids.add(cuboid("wall_north_west", bounds(minOuter, 0.0D, minOuter, minInner, 1.0D, minInner),
                        "concrete_smooth", 0, uv(), true));
                cuboids.add(cuboid("wall_south_west", bounds(minOuter, 0.0D, maxInner, minInner, 1.0D, maxOuter),
                        "concrete_smooth", 0, uv(), true));
                cuboids.add(cuboid("wall_east", bounds(maxInner, 0.0D, minOuter, maxOuter, 1.0D, maxOuter),
                        "concrete_smooth", 0, uv(), true));
                cuboids.add(cuboid("vertical_belt_east", bounds(maxInner - 0.125D, 0.0D, minInner, maxInner, 1.0D, maxInner),
                        "block", metadata, uv(1, 0, 0, 0, 0, 0), true));
            } else if (metadata == 5) {
                cuboids.add(cuboid("wall_north_east", bounds(maxInner, 0.0D, minOuter, maxOuter, 1.0D, minInner),
                        "concrete_smooth", 0, uv(), true));
                cuboids.add(cuboid("wall_south_east", bounds(maxInner, 0.0D, maxInner, maxOuter, 1.0D, maxOuter),
                        "concrete_smooth", 0, uv(), true));
                cuboids.add(cuboid("wall_west", bounds(minOuter, 0.0D, minOuter, minInner, 1.0D, maxOuter),
                        "concrete_smooth", 0, uv(), true));
                cuboids.add(cuboid("vertical_belt_west", bounds(minInner, 0.0D, minInner, minInner + 0.125D, 1.0D, maxInner),
                        "block", metadata, uv(2, 0, 0, 0, 0, 0), true));
            }
        } else {
            cuboids.addAll(conveyorLiftTopCuboids(metadata));
        }
        return List.copyOf(cuboids);
    }

    private static List<CuboidUvPlan> conveyorLiftTopCuboids(int metadata) {
        List<CuboidUvPlan> cuboids = new ArrayList<>();
        if (metadata == 2 || metadata == 3) {
            cuboids.add(cuboid("top_half_west", bounds(0.0D, 0.0D, 0.0D, 0.25D, 0.5D, 1.0D),
                    "concrete_smooth", 0, uv(), true));
            cuboids.add(cuboid("top_half_east", bounds(0.75D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D),
                    "concrete_smooth", 0, uv(), true));
            cuboids.add(cuboid(metadata == 2 ? "top_belt_south" : "top_belt_north",
                    metadata == 2
                            ? bounds(0.25D, 0.0D, 0.625D, 0.75D, 0.25D, 1.0D)
                            : bounds(0.25D, 0.0D, 0.0D, 0.75D, 0.25D, 0.375D),
                    "block", metadata, metadata == 2 ? uv(3, 0, 0, 0, 0, 3) : uv(0, 0, 0, 0, 3, 0), false));
        } else if (metadata == 4 || metadata == 5) {
            cuboids.add(cuboid("top_half_north", bounds(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 0.25D),
                    "concrete_smooth", 0, uv(), true));
            cuboids.add(cuboid("top_half_south", bounds(0.0D, 0.0D, 0.75D, 1.0D, 0.5D, 1.0D),
                    "concrete_smooth", 0, uv(), true));
            cuboids.add(cuboid(metadata == 4 ? "top_belt_east" : "top_belt_west",
                    metadata == 4
                            ? bounds(0.625D, 0.0D, 0.25D, 1.0D, 0.25D, 0.75D)
                            : bounds(0.0D, 0.0D, 0.25D, 0.375D, 0.25D, 0.75D),
                    "block", metadata, metadata == 4 ? uv(1, 0, 0, 3, 0, 0) : uv(2, 0, 3, 0, 0, 0), false));
        }
        return List.copyOf(cuboids);
    }

    private static CuboidUvPlan cuboid(String role, LegacyAtlasCuboidRenderer.CuboidBounds bounds,
            String iconRole, int iconMetadata, UvRotationPlan uvRotations, boolean overrideTexture) {
        return new CuboidUvPlan(role, bounds, iconRole, iconMetadata, uvRotations, overrideTexture);
    }

    private static FaceIconCuboidPlan faceCuboid(String role, LegacyAtlasCuboidRenderer.CuboidBounds bounds,
            String topIconRole, String bottomIconRole, String northIconRole, String southIconRole,
            String eastIconRole, String westIconRole, int iconMetadata, UvRotationPlan uvRotations,
            boolean overrideTexture) {
        return new FaceIconCuboidPlan(role, bounds, topIconRole, bottomIconRole, northIconRole,
                southIconRole, eastIconRole, westIconRole, iconMetadata, uvRotations, overrideTexture);
    }

    private static LegacyAtlasCuboidRenderer.CuboidBounds bounds(double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ) {
        return new LegacyAtlasCuboidRenderer.CuboidBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static UvRotationPlan uv() {
        return uv(0, 0, 0, 0, 0, 0);
    }

    private static UvRotationPlan uv(int top, int bottom, int north, int south, int east, int west) {
        return new UvRotationPlan(top, bottom, north, south, east, west, true);
    }

    private static List<FoundryFaceDrawPlan> foundryOpenVesselFaces(double bottomFaceYOffset) {
        return List.of(
                foundryFace(Direction.UP, "iconTop", 0.0D, 0.0D, 0.0D, "top", false, false),
                foundryFace(Direction.UP, "iconBottom", 0.0D, bottomFaceYOffset, 0.0D, "top", false, false),
                foundryFace(Direction.DOWN, "iconBottom", 0.0D, 0.0D, 0.0D, "bottom", false, false),
                foundryFace(Direction.EAST, "iconSide", 0.0D, 0.0D, 0.0D, "x", false, false),
                foundryFace(Direction.EAST, "iconInner", -0.875D, 0.0D, 0.0D, "x", false, false),
                foundryFace(Direction.WEST, "iconSide", 0.0D, 0.0D, 0.0D, "x", false, false),
                foundryFace(Direction.WEST, "iconInner", 0.875D, 0.0D, 0.0D, "x", false, false),
                foundryFace(Direction.SOUTH, "iconSide", 0.0D, 0.0D, 0.0D, "z", false, false),
                foundryFace(Direction.SOUTH, "iconInner", 0.0D, 0.0D, -0.875D, "z", false, false),
                foundryFace(Direction.NORTH, "iconSide", 0.0D, 0.0D, 0.0D, "z", false, false),
                foundryFace(Direction.NORTH, "iconInner", 0.0D, 0.0D, 0.875D, "z", false, false));
    }

    private static FoundryColorPlan foundryColorPlan(int colorMultiplier, boolean anaglyph) {
        int color = LegacyBlockRenderHelper.colorMultiplier(colorMultiplier, anaglyph);
        return new FoundryColorPlan(colorMultiplier & 0xFFFFFF, anaglyph, color,
                red(color), green(color), blue(color), 1.0F, 0.5F, 0.8F, 0.6F);
    }

    private static FoundryOutletRenderPlan foundryOutletPlan(int metadata, boolean hasFilter, boolean closed,
            TranslationPlan translation, int colorMultiplier, boolean anaglyph, boolean world) {
        LegacyAtlasCuboidRenderer.CuboidBounds core = foundryOutletBounds(metadata);
        List<FoundryFaceDrawPlan> faces = new ArrayList<>(foundryOutletCoreFaces(metadata));
        if (hasFilter) {
            faces.add(foundryOutletPanel(metadata, true));
        }
        if (closed) {
            faces.add(foundryOutletPanel(metadata, false));
        }
        return new FoundryOutletRenderPlan(metadata, translation, core, hasFilter, closed,
                true, world, foundryColorPlan(colorMultiplier, anaglyph), List.copyOf(faces));
    }

    private static LegacyAtlasCuboidRenderer.CuboidBounds foundryOutletBounds(int metadata) {
        return switch (metadata) {
            case 4 -> bounds(0.625D, 0.0D, 0.3125D, 1.0D, 0.5D, 0.6875D);
            case 5 -> bounds(0.0D, 0.0D, 0.3125D, 0.375D, 0.5D, 0.6875D);
            case 2 -> bounds(0.3125D, 0.0D, 0.625D, 0.6875D, 0.5D, 1.0D);
            default -> bounds(0.3125D, 0.0D, 0.0D, 0.6875D, 0.5D, 0.375D);
        };
    }

    private static List<FoundryFaceDrawPlan> foundryOutletCoreFaces(int metadata) {
        if (metadata == 4 || metadata == 5) {
            return List.of(
                    foundryFace(Direction.DOWN, "iconBottom", 0.0D, 0.0D, 0.0D, "bottom", false, false),
                    foundryFace(Direction.UP, "iconTop", 0.0D, 0.0D, 0.0D, "top", false, false),
                    foundryFace(Direction.UP, "iconBottom", 0.0D, -0.375D, 0.0D, "top", false, false),
                    foundryFace(Direction.SOUTH, "iconSide", 0.0D, 0.0D, 0.0D, "z", false, false),
                    foundryFace(Direction.SOUTH, "iconInner", 0.0D, 0.0D, -0.3125D, "z", false, false),
                    foundryFace(Direction.NORTH, "iconSide", 0.0D, 0.0D, 0.0D, "z", true, false),
                    foundryFace(Direction.NORTH, "iconInner", 0.0D, 0.0D, 0.3125D, "z", true, false),
                    foundryFace(Direction.EAST, "iconFront", 0.0D, 0.0D, 0.0D, "x", false, false),
                    foundryFace(Direction.WEST, "iconFront", 0.0D, 0.0D, 0.0D, "x", false, false));
        }
        return List.of(
                foundryFace(Direction.DOWN, "iconBottom", 0.0D, 0.0D, 0.0D, "bottom", false, false),
                foundryFace(Direction.UP, "iconTop", 0.0D, 0.0D, 0.0D, "top", false, false),
                foundryFace(Direction.UP, "iconBottom", 0.0D, -0.375D, 0.0D, "top", false, false),
                foundryFace(Direction.EAST, "iconSide", 0.0D, 0.0D, 0.0D, "x", true, false),
                foundryFace(Direction.EAST, "iconInner", -0.3125D, 0.0D, 0.0D, "x", true, false),
                foundryFace(Direction.WEST, "iconSide", 0.0D, 0.0D, 0.0D, "x", false, false),
                foundryFace(Direction.WEST, "iconInner", 0.3125D, 0.0D, 0.0D, "x", false, false),
                foundryFace(Direction.SOUTH, "iconFront", 0.0D, 0.0D, 0.0D, "z", false, false),
                foundryFace(Direction.NORTH, "iconFront", 0.0D, 0.0D, 0.0D, "z", false, false));
    }

    private static List<FoundryFaceDrawPlan> foundryOutletInventoryFaces() {
        return List.of(
                foundryFace(Direction.UP, "iconTop", 0.0D, 0.0D, 0.0D, "top", false, false),
                foundryFace(Direction.UP, "iconBottom", 0.0D, -0.375D, 0.0D, "top", false, false),
                foundryFace(Direction.DOWN, "iconBottom", 0.0D, 0.0D, 0.0D, "bottom", false, false),
                foundryFace(Direction.EAST, "iconSide", 0.0D, 0.0D, 0.0D, "x", true, false),
                foundryFace(Direction.EAST, "iconInner", -0.3125D, 0.0D, 0.0D, "x", true, false),
                foundryFace(Direction.WEST, "iconSide", 0.0D, 0.0D, 0.0D, "x", false, false),
                foundryFace(Direction.WEST, "iconInner", 0.3125D, 0.0D, 0.0D, "x", false, false),
                foundryFace(Direction.SOUTH, "iconFront", 0.0D, 0.0D, 0.0D, "z", false, false));
    }

    private static FoundryFaceDrawPlan foundryOutletPanel(int metadata, boolean filter) {
        String icon = filter ? "iconFilter" : "iconLock";
        double near = filter ? 0.03125D : 0.0625D;
        double far = filter ? 0.96875D : 0.9375D;
        return switch (metadata) {
            case 4 -> new FoundryFaceDrawPlan(Direction.EAST, icon,
                    bounds(far, 0.0625D, 0.375D, far, 0.5D, 0.625D),
                    0.0D, 0.0D, 0.0D, "x", false, false);
            case 5 -> new FoundryFaceDrawPlan(Direction.WEST, icon,
                    bounds(near, 0.0625D, 0.375D, near, 0.5D, 0.625D),
                    0.0D, 0.0D, 0.0D, "x", false, false);
            case 2 -> new FoundryFaceDrawPlan(Direction.SOUTH, icon,
                    bounds(0.375D, 0.0625D, far, 0.625D, 0.5D, far),
                    0.0D, 0.0D, 0.0D, "z", false, false);
            default -> new FoundryFaceDrawPlan(Direction.NORTH, icon,
                    bounds(0.375D, 0.0625D, near, 0.625D, 0.5D, near),
                    0.0D, 0.0D, 0.0D, "z", false, false);
        };
    }

    private static FoundryFaceDrawPlan foundryFace(Direction direction, String iconRole,
            double offsetX, double offsetY, double offsetZ, String colorRole,
            boolean alternateUResetRequired, boolean fullBright) {
        return new FoundryFaceDrawPlan(direction, iconRole, null, offsetX, offsetY, offsetZ,
                colorRole, alternateUResetRequired, fullBright);
    }

    private static FoundryFaceDrawPlan foundryBoundFace(Direction direction, String iconRole,
            LegacyAtlasCuboidRenderer.CuboidBounds bounds, String colorRole, boolean fullBright) {
        return new FoundryFaceDrawPlan(direction, iconRole, bounds, 0.0D, 0.0D, 0.0D,
                colorRole, false, fullBright);
    }

    private static FoundryFluidStatePlan foundryTankFluidState(int amount, int capacity, int moltenColor,
            boolean connectedNegY, boolean connectedPosY) {
        double max = 0.75D + (connectedNegY ? 0.125D : 0.0D) + (connectedPosY ? 0.125D : 0.0D);
        double level = amount > 0 && capacity > 0 ? amount * max / capacity : 0.0D;
        double startY = connectedNegY ? 0.0D : 0.125D;
        return new FoundryFluidStatePlan(amount > 0 && capacity > 0, amount, capacity, moltenColor,
                max, level, foundryMoltenDisplayColor(moltenColor), startY);
    }

    private static FoundryFluidStatePlan foundryChannelFluidState(int amount, int capacity, int moltenColor) {
        double level = amount > 0 && capacity > 0 ? amount * 0.25D / capacity : 0.0D;
        return new FoundryFluidStatePlan(amount > 0 && capacity > 0, amount, capacity, moltenColor,
                0.25D, level, foundryMoltenDisplayColor(moltenColor), 0.125D);
    }

    private static int foundryMoltenDisplayColor(int moltenColor) {
        Color bright = new Color(moltenColor).brighter();
        double brightener = 0.7D;
        int red = (int) (255.0D - (255.0D - bright.getRed()) * brightener);
        int green = (int) (255.0D - (255.0D - bright.getGreen()) * brightener);
        int blue = (int) (255.0D - (255.0D - bright.getBlue()) * brightener);
        return red << 16 | green << 8 | blue;
    }

    private static int lightenColor(int color, double percent) {
        Color awtColor = new Color(color);
        int red = (int) (awtColor.getRed() + (255 - awtColor.getRed()) * percent);
        int green = (int) (awtColor.getGreen() + (255 - awtColor.getGreen()) * percent);
        int blue = (int) (awtColor.getBlue() + (255 - awtColor.getBlue()) * percent);
        return new Color(red, green, blue).getRGB();
    }

    private static List<FoundryFaceDrawPlan> foundryTankShellFaces(FoundryConnectionPlan con,
            FoundryOutletConnectionPlan outlets) {
        List<FoundryFaceDrawPlan> faces = new ArrayList<>();
        if (!con.negY()) {
            LegacyAtlasCuboidRenderer.CuboidBounds bottom = bounds(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
            faces.add(foundryBoundFace(Direction.UP, "iconBottom", bottom, "top", false));
            faces.add(foundryBoundFace(Direction.DOWN, "iconBottom", bottom, "bottom", false));
        }
        if (!con.posX()) {
            addFoundryTankSideFaces(faces, Direction.EAST, bounds(0.875D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D),
                    con, outlets.posX(), "x");
        }
        if (!con.negX()) {
            addFoundryTankSideFaces(faces, Direction.WEST, bounds(0.0D, 0.0D, 0.0D, 0.125D, 1.0D, 1.0D),
                    con, outlets.negX(), "x");
        }
        if (!con.posZ()) {
            addFoundryTankSideFaces(faces, Direction.SOUTH, bounds(0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D),
                    con, outlets.posZ(), "z");
        }
        if (!con.negZ()) {
            addFoundryTankSideFaces(faces, Direction.NORTH, bounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D),
                    con, outlets.negZ(), "z");
        }
        return List.copyOf(faces);
    }

    private static void addFoundryTankSideFaces(List<FoundryFaceDrawPlan> faces, Direction outward,
            LegacyAtlasCuboidRenderer.CuboidBounds bounds, FoundryConnectionPlan con,
            boolean outlet, String colorRole) {
        String outerIcon = con.negY()
                ? (outlet ? "iconSideUpperOutlet" : "iconSideUpper")
                : (outlet ? "iconSideOutlet" : "iconSide");
        faces.add(foundryBoundFace(outward, outerIcon, bounds, colorRole, false));
        faces.add(foundryBoundFace(outward.getOpposite(), con.posY() ? "iconBottom" : "iconInner",
                bounds, colorRole, false));
        faces.add(foundryBoundFace(Direction.UP, "iconTop", bounds, "top", false));
    }

    private static List<FoundryFluidSurfacePlan> foundryTankFluidSurfaces(FoundryConnectionPlan con,
            FoundryFluidStatePlan fluid) {
        if (!fluid.present()) {
            return List.of();
        }
        LegacyAtlasCuboidRenderer.CuboidBounds fluidBounds = bounds(0.0D, fluid.startY(), 0.0D,
                1.0D, fluid.startY() + fluid.level(), 1.0D);
        List<FoundryFluidSurfacePlan> surfaces = new ArrayList<>();
        surfaces.add(new FoundryFluidSurfacePlan("tank_top", fluidBounds, "iconLava",
                fluid.displayColor(), true));
        if (con.posX()) {
            surfaces.add(new FoundryFluidSurfacePlan("tank_pos_x", fluidBounds, "iconLava",
                    fluid.displayColor(), true));
        }
        if (con.negX()) {
            surfaces.add(new FoundryFluidSurfacePlan("tank_neg_x", fluidBounds, "iconLava",
                    fluid.displayColor(), true));
        }
        if (con.posZ()) {
            surfaces.add(new FoundryFluidSurfacePlan("tank_pos_z", fluidBounds, "iconLava",
                    fluid.displayColor(), true));
        }
        if (con.negZ()) {
            surfaces.add(new FoundryFluidSurfacePlan("tank_neg_z", fluidBounds, "iconLava",
                    fluid.displayColor(), true));
        }
        return List.copyOf(surfaces);
    }

    private static List<FoundryFaceDrawPlan> foundryChannelShellFaces(FoundryConnectionPlan con) {
        List<FoundryFaceDrawPlan> faces = new ArrayList<>();
        LegacyAtlasCuboidRenderer.CuboidBounds centerTop = bounds(0.375D, 0.0D, 0.375D, 0.625D, 0.125D, 0.625D);
        LegacyAtlasCuboidRenderer.CuboidBounds centerBottom = bounds(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.125D, 0.6875D);
        faces.add(foundryBoundFace(Direction.UP, "iconBottom", centerTop, "top", false));
        faces.add(foundryBoundFace(Direction.DOWN, "iconBottom", centerBottom, "bottom", false));
        addFoundryChannelArm(faces, "pos_x", con.posX(),
                bounds(0.625D, 0.0D, 0.3125D, 1.0D, 0.125D, 0.6875D),
                bounds(0.625D, 0.0D, 0.3125D, 1.0D, 0.5D, 0.375D),
                bounds(0.625D, 0.0D, 0.625D, 1.0D, 0.5D, 0.6875D),
                bounds(0.625D, 0.0D, 0.3125D, 0.6875D, 0.5D, 0.6875D), Direction.EAST);
        addFoundryChannelArm(faces, "neg_x", con.negX(),
                bounds(0.0D, 0.0D, 0.3125D, 0.375D, 0.125D, 0.6875D),
                bounds(0.0D, 0.0D, 0.3125D, 0.375D, 0.5D, 0.375D),
                bounds(0.0D, 0.0D, 0.625D, 0.375D, 0.5D, 0.6875D),
                bounds(0.3125D, 0.0D, 0.3125D, 0.375D, 0.5D, 0.6875D), Direction.WEST);
        addFoundryChannelArm(faces, "pos_z", con.posZ(),
                bounds(0.3125D, 0.0D, 0.625D, 0.6875D, 0.125D, 1.0D),
                bounds(0.3125D, 0.0D, 0.625D, 0.375D, 0.5D, 1.0D),
                bounds(0.625D, 0.0D, 0.625D, 0.6875D, 0.5D, 1.0D),
                bounds(0.3125D, 0.0D, 0.625D, 0.6875D, 0.5D, 0.6875D), Direction.SOUTH);
        addFoundryChannelArm(faces, "neg_z", con.negZ(),
                bounds(0.3125D, 0.0D, 0.0D, 0.6875D, 0.125D, 0.375D),
                bounds(0.3125D, 0.0D, 0.0D, 0.375D, 0.5D, 0.375D),
                bounds(0.625D, 0.0D, 0.0D, 0.6875D, 0.5D, 0.375D),
                bounds(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.5D, 0.375D), Direction.NORTH);
        return List.copyOf(faces);
    }

    private static void addFoundryChannelArm(List<FoundryFaceDrawPlan> faces, String role, boolean connected,
            LegacyAtlasCuboidRenderer.CuboidBounds bottom, LegacyAtlasCuboidRenderer.CuboidBounds sideA,
            LegacyAtlasCuboidRenderer.CuboidBounds sideB, LegacyAtlasCuboidRenderer.CuboidBounds cap,
            Direction outward) {
        if (connected) {
            faces.add(foundryBoundFace(Direction.UP, "iconBottom", bottom, "top", false));
            faces.add(foundryBoundFace(Direction.DOWN, "iconBottom", bottom, "bottom", false));
            faces.add(foundryBoundFace(Direction.UP, "iconTop", sideA, "top", false));
            faces.add(foundryBoundFace(Direction.UP, "iconTop", sideB, "top", false));
            faces.add(foundryBoundFace(outward, "iconSide", bottom, role.contains("_x") ? "x" : "z", false));
        } else {
            faces.add(foundryBoundFace(Direction.UP, "iconTop", cap, "top", false));
            faces.add(foundryBoundFace(outward, "iconSide", cap, role.contains("_x") ? "x" : "z", false));
        }
    }

    private static List<FoundryFluidSurfacePlan> foundryChannelFluidSurfaces(FoundryConnectionPlan con,
            FoundryFluidStatePlan fluid) {
        if (!fluid.present()) {
            return List.of();
        }
        List<FoundryFluidSurfacePlan> surfaces = new ArrayList<>();
        surfaces.add(new FoundryFluidSurfacePlan("channel_center",
                bounds(0.375D, 0.125D, 0.375D, 0.625D, 0.125D + fluid.level(), 0.625D),
                "iconLava", fluid.displayColor(), true));
        if (con.posX()) {
            surfaces.add(new FoundryFluidSurfacePlan("channel_pos_x",
                    bounds(0.625D, 0.125D, 0.3125D, 1.0D, 0.125D + fluid.level(), 0.6875D),
                    "iconLava", fluid.displayColor(), true));
        }
        if (con.negX()) {
            surfaces.add(new FoundryFluidSurfacePlan("channel_neg_x",
                    bounds(0.0D, 0.125D, 0.3125D, 0.375D, 0.125D + fluid.level(), 0.6875D),
                    "iconLava", fluid.displayColor(), true));
        }
        if (con.posZ()) {
            surfaces.add(new FoundryFluidSurfacePlan("channel_pos_z",
                    bounds(0.3125D, 0.125D, 0.625D, 0.6875D, 0.125D + fluid.level(), 1.0D),
                    "iconLava", fluid.displayColor(), true));
        }
        if (con.negZ()) {
            surfaces.add(new FoundryFluidSurfacePlan("channel_neg_z",
                    bounds(0.3125D, 0.125D, 0.0D, 0.6875D, 0.125D + fluid.level(), 0.375D),
                    "iconLava", fluid.displayColor(), true));
        }
        return List.copyOf(surfaces);
    }

    private static List<AnvilPartPlan> anvilParts(float yawRadians, boolean shadow) {
        return List.of(
                new AnvilPartPlan("Top", 1, 0, yawRadians, 0.0F, shadow),
                new AnvilPartPlan("Bottom", 0, 0, yawRadians, 0.0F, shadow),
                new AnvilPartPlan("Front", 0, 0, yawRadians, 0.0F, shadow),
                new AnvilPartPlan("Back", 0, 0, yawRadians, 0.0F, shadow),
                new AnvilPartPlan("Left", 0, 0, yawRadians, 0.0F, shadow),
                new AnvilPartPlan("Right", 0, 0, yawRadians, 0.0F, shadow));
    }

    private static List<NamedIconPartPlan> partitionerParts(float yawRadians, boolean shadeNormals) {
        return List.of(
                new NamedIconPartPlan("crane_buffer", "Side", "side", 0, 0, yawRadians, 0.0F, shadeNormals),
                new NamedIconPartPlan("crane_buffer", "Back", "iconBack", 0, 0, yawRadians, 0.0F, shadeNormals),
                new NamedIconPartPlan("crane_buffer", "Top_Top.001", "iconTop", 0, 0, yawRadians, 0.0F, shadeNormals),
                new NamedIconPartPlan("crane_buffer", "Inner", "iconInner", 0, 0, yawRadians, 0.0F, shadeNormals),
                new NamedIconPartPlan("crane_buffer", "InnerSide", "iconInnerSide", 0, 0, yawRadians, 0.0F, shadeNormals),
                new NamedIconPartPlan("crane_buffer", "Belt", "iconBelt", 0, 0, yawRadians, 0.0F, shadeNormals));
    }

    private static List<NamedIconPartPlan> capacitorParts(float yawRadians, float pitchRadians, boolean shadeNormals) {
        return List.of(
                new NamedIconPartPlan("capacitor", "Top", "iconTop", 0, 0, yawRadians, pitchRadians, shadeNormals),
                new NamedIconPartPlan("capacitor", "Side", "iconSide", 0, 0, yawRadians, pitchRadians, shadeNormals),
                new NamedIconPartPlan("capacitor", "Bottom", "iconBottom", 0, 0, yawRadians, pitchRadians, shadeNormals),
                new NamedIconPartPlan("capacitor", "InnerTop", "iconInnerTop", 0, 0, yawRadians, pitchRadians, shadeNormals),
                new NamedIconPartPlan("capacitor", "InnerSide", "iconInnerSide", 0, 0, yawRadians, pitchRadians, shadeNormals));
    }

    private static List<NamedIconPartPlan> splitterParts(boolean left, float yawRadians, boolean shadeNormals) {
        return List.of(
                new NamedIconPartPlan("splitter", "Top", left ? "iconTopLeft" : "iconTopRight",
                        0, 0, yawRadians, 0.0F, shadeNormals),
                new NamedIconPartPlan("splitter", "Bottom", left ? "iconTopRight" : "iconTopLeft",
                        0, 0, yawRadians, 0.0F, shadeNormals),
                new NamedIconPartPlan("splitter", left ? "Left" : "Right", left ? "iconLeft" : "iconRight",
                        0, 0, yawRadians, 0.0F, shadeNormals),
                new NamedIconPartPlan("splitter", "Back", left ? "iconBackLeft" : "iconBackRight",
                        0, 0, yawRadians, 0.0F, shadeNormals),
                new NamedIconPartPlan("splitter", "Front", left ? "iconFrontLeft" : "iconFrontRight",
                        0, 0, yawRadians, 0.0F, shadeNormals),
                new NamedIconPartPlan("splitter", "Inner", "iconInner", 0, 0, yawRadians, 0.0F, shadeNormals),
                new NamedIconPartPlan("splitter", "InnerLeft", "iconInnerSide", 0, 0, yawRadians, 0.0F, shadeNormals),
                new NamedIconPartPlan("splitter", "InnerRight", "iconInnerSide", 0, 0, yawRadians, 0.0F, shadeNormals),
                new NamedIconPartPlan("splitter", "InnerTop", "iconInnerSide", 0, 0, yawRadians, 0.0F, shadeNormals),
                new NamedIconPartPlan("splitter", "InnerBottom", "iconBelt", 0, 0, yawRadians, 0.0F, shadeNormals));
    }

    private static DiodeBoundsPlan diodeBaseBounds(int metadata) {
        double width = 0.875D;
        LegacyAtlasCuboidRenderer.CuboidBounds directionalBounds =
                new LegacyAtlasCuboidRenderer.CuboidBounds(
                        metadata == 4 ? width : 0.0D,
                        metadata == 0 ? width : 0.0D,
                        metadata == 2 ? width : 0.0D,
                        metadata == 5 ? 1.0D - width : 1.0D,
                        metadata == 1 ? 1.0D - width : 1.0D,
                        metadata == 3 ? 1.0D - width : 1.0D);
        double radius = 0.375D;
        LegacyAtlasCuboidRenderer.CuboidBounds padBounds =
                new LegacyAtlasCuboidRenderer.CuboidBounds(
                        0.5D - radius, 0.5D - radius, 0.5D - radius,
                        0.5D + radius, 0.5D + radius, 0.5D + radius);
        return new DiodeBoundsPlan(directionalBounds, padBounds, "hadron_coil_alloy", true);
    }

    private static List<NamedIconPartPlan> diodeCableParts(boolean posX, boolean negX, boolean posY,
            boolean negY, boolean posZ, boolean negZ, float yawRadians, boolean shadeNormals) {
        List<NamedIconPartPlan> parts = new ArrayList<>();
        if (posX) {
            parts.add(new NamedIconPartPlan("cable_neo", "posX", "red_cable",
                    0, 0, yawRadians, 0.0F, shadeNormals));
        }
        if (negX) {
            parts.add(new NamedIconPartPlan("cable_neo", "negX", "red_cable",
                    0, 0, yawRadians, 0.0F, shadeNormals));
        }
        if (posY) {
            parts.add(new NamedIconPartPlan("cable_neo", "posY", "red_cable",
                    0, 0, yawRadians, 0.0F, shadeNormals));
        }
        if (negY) {
            parts.add(new NamedIconPartPlan("cable_neo", "negY", "red_cable",
                    0, 0, yawRadians, 0.0F, shadeNormals));
        }
        if (posZ) {
            parts.add(new NamedIconPartPlan("cable_neo", "posZ", "red_cable",
                    0, 0, yawRadians, 0.0F, shadeNormals));
        }
        if (negZ) {
            parts.add(new NamedIconPartPlan("cable_neo", "negZ", "red_cable",
                    0, 0, yawRadians, 0.0F, shadeNormals));
        }
        return List.copyOf(parts);
    }

    private static List<LegacyQuadPlan> chainQuads(int metadata, double x, double y, double z) {
        double minU = 0.0D;
        double minV = 0.0D;
        double maxU = 1.0D;
        double maxV = 1.0D;
        double scale = CHAIN_SCALE;
        double wallOffset = CHAIN_WALL_OFFSET;
        return switch (metadata) {
            case 0 -> List.of(
                    quad("chain_cross_diagonal_a_front",
                            vertex(x, y + 1.0D, z, minU, minV),
                            vertex(x, y, z, minU, maxV),
                            vertex(x + 1.0D, y, z + 1.0D, maxU, maxV),
                            vertex(x + 1.0D, y + 1.0D, z + 1.0D, maxU, minV)),
                    quad("chain_cross_diagonal_a_back",
                            vertex(x + 1.0D, y + 1.0D, z + 1.0D, minU, minV),
                            vertex(x + 1.0D, y, z + 1.0D, minU, maxV),
                            vertex(x, y, z, maxU, maxV),
                            vertex(x, y + 1.0D, z, maxU, minV)),
                    quad("chain_cross_diagonal_b_front",
                            vertex(x, y + 1.0D, z + 1.0D, minU, minV),
                            vertex(x, y, z + 1.0D, minU, maxV),
                            vertex(x + 1.0D, y, z, maxU, maxV),
                            vertex(x + 1.0D, y + 1.0D, z, maxU, minV)),
                    quad("chain_cross_diagonal_b_back",
                            vertex(x + 1.0D, y + 1.0D, z, minU, minV),
                            vertex(x + 1.0D, y, z, minU, maxV),
                            vertex(x, y, z + 1.0D, maxU, maxV),
                            vertex(x, y + 1.0D, z + 1.0D, maxU, minV)));
            case 5 -> List.of(
                    quad("chain_wall_west_front",
                            vertex(x + wallOffset, y + 1.0D + scale, z + 1.0D + scale, minU, minV),
                            vertex(x + wallOffset, y - scale, z + 1.0D + scale, minU, maxV),
                            vertex(x + wallOffset, y - scale, z - scale, maxU, maxV),
                            vertex(x + wallOffset, y + 1.0D + scale, z - scale, maxU, minV)),
                    quad("chain_wall_west_back",
                            vertex(x + wallOffset, y - scale, z + 1.0D + scale, minU, maxV),
                            vertex(x + wallOffset, y + 1.0D + scale, z + 1.0D + scale, minU, minV),
                            vertex(x + wallOffset, y + 1.0D + scale, z - scale, maxU, minV),
                            vertex(x + wallOffset, y - scale, z - scale, maxU, maxV)));
            case 4 -> List.of(
                    quad("chain_wall_east_front",
                            vertex(x + 1.0D - wallOffset, y - scale, z + 1.0D + scale, maxU, maxV),
                            vertex(x + 1.0D - wallOffset, y + 1.0D + scale, z + 1.0D + scale, maxU, minV),
                            vertex(x + 1.0D - wallOffset, y + 1.0D + scale, z - scale, minU, minV),
                            vertex(x + 1.0D - wallOffset, y - scale, z - scale, minU, maxV)),
                    quad("chain_wall_east_back",
                            vertex(x + 1.0D - wallOffset, y + 1.0D + scale, z + 1.0D + scale, maxU, minV),
                            vertex(x + 1.0D - wallOffset, y - scale, z + 1.0D + scale, maxU, maxV),
                            vertex(x + 1.0D - wallOffset, y - scale, z - scale, minU, maxV),
                            vertex(x + 1.0D - wallOffset, y + 1.0D + scale, z - scale, minU, minV)));
            case 3 -> List.of(
                    quad("chain_wall_north_front",
                            vertex(x + 1.0D + scale, y - scale, z + wallOffset, maxU, maxV),
                            vertex(x + 1.0D + scale, y + 1.0D + scale, z + wallOffset, maxU, minV),
                            vertex(x - scale, y + 1.0D + scale, z + wallOffset, minU, minV),
                            vertex(x - scale, y - scale, z + wallOffset, minU, maxV)),
                    quad("chain_wall_north_back",
                            vertex(x + 1.0D + scale, y + 1.0D + scale, z + wallOffset, maxU, minV),
                            vertex(x + 1.0D + scale, y - scale, z + wallOffset, maxU, maxV),
                            vertex(x - scale, y - scale, z + wallOffset, minU, maxV),
                            vertex(x - scale, y + 1.0D + scale, z + wallOffset, minU, minV)));
            case 2 -> List.of(
                    quad("chain_wall_south_front",
                            vertex(x + 1.0D + scale, y + 1.0D + scale, z + 1.0D - wallOffset, minU, minV),
                            vertex(x + 1.0D + scale, y - scale, z + 1.0D - wallOffset, minU, maxV),
                            vertex(x - scale, y - scale, z + 1.0D - wallOffset, maxU, maxV),
                            vertex(x - scale, y + 1.0D + scale, z + 1.0D - wallOffset, maxU, minV)),
                    quad("chain_wall_south_back",
                            vertex(x + 1.0D + scale, y - scale, z + 1.0D - wallOffset, minU, maxV),
                            vertex(x + 1.0D + scale, y + 1.0D + scale, z + 1.0D - wallOffset, minU, minV),
                            vertex(x - scale, y + 1.0D + scale, z + 1.0D - wallOffset, maxU, minV),
                            vertex(x - scale, y - scale, z + 1.0D - wallOffset, maxU, maxV)));
            default -> List.of();
        };
    }

    private static ColorPlan whiteColorPlan() {
        return new ColorPlan(0xFFFFFF, 1.0F, 1.0F, 1.0F);
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

    private static LegacyQuadPlan quad(String role, LegacyVertex v0, LegacyVertex v1, LegacyVertex v2, LegacyVertex v3) {
        return new LegacyQuadPlan(role, List.of(v0, v1, v2, v3));
    }

    private static LegacyVertex vertex(double x, double y, double z, double u, double v) {
        return new LegacyVertex(x, y, z, u, v);
    }

    public record ChainRenderPlan(int metadata, double scale, double wallOffset, boolean renders3DInInventory,
                                  List<ColorPlan> colorPlans, List<LegacyQuadPlan> quads) {
    }

    public record GrateRenderPlan(int metadata, float legacyY, float localY, int blockYOffset,
                                  double thickness, LegacyAtlasCuboidRenderer.CuboidBounds renderBounds,
                                  boolean rendersStandardBlock) {
    }

    public record SteelWallRenderPlan(int metadata, LegacyAtlasCuboidRenderer.CuboidBounds worldBounds,
                                      LegacyAtlasCuboidRenderer.CuboidBounds inventoryBounds,
                                      TranslationPlan inventoryTranslation, List<Direction> inventoryFaceOrder,
                                      boolean renders3DInInventory, boolean rendersStandardBlock) {
    }

    public record SteelCornerRenderPlan(int metadata, List<LegacyAtlasCuboidRenderer.CuboidBounds> worldBounds,
                                        List<LegacyAtlasCuboidRenderer.CuboidBounds> inventoryBounds,
                                        TranslationPlan inventoryTranslation, List<Direction> inventoryFaceOrder,
                                        boolean usesRenderBlocksNt, boolean renders3DInInventory,
                                        boolean rendersStandardBlock) {
    }

    public record ObjIconModelRenderPlan(String legacyResourceModel, int iconMetadata, TranslationPlan translation,
                                         float yawRadians, float pitchRadians, boolean shadow,
                                         boolean usesOverrideTexture, boolean renders3DInInventory) {
    }

    public record ScaledObjIconModelRenderPlan(String legacyResourceModel, int iconMetadata,
                                               TranslationPlan translation, float scaleX, float scaleY, float scaleZ,
                                               float yawRadians, float pitchRadians, boolean shadow,
                                               boolean usesOverrideTexture, boolean renders3DInInventory) {
    }

    public record ObjIconPartRenderPlan(String legacyResourceModel, String partName, int iconMetadata,
                                        TranslationPlan translation, float yawRadians, float pitchRadians,
                                        boolean shadow, boolean usesOverrideTexture,
                                        boolean renders3DInInventory, boolean fullBright) {
    }

    public record CrtRenderPlan(int metadata, int horizontalMetadata, boolean screenFullBright,
                                TranslationPlan translation, float yawRadians,
                                ObjIconPartRenderPlan monitor, ObjIconPartRenderPlan screen) {
    }

    public record PedestalRenderPlan(int metadata, List<LegacyAtlasCuboidRenderer.CuboidBounds> bounds,
                                     TranslationPlan inventoryTranslation, boolean renders3DInInventory,
                                     boolean rendersStandardBlock,
                                     List<LegacyBlockRenderHelper.InventoryFace> inventoryFaceOrder) {
    }

    public record AnvilRenderPlan(int metadata, TranslationPlan translation, float yawRadians, float pitchRadians,
                                  boolean shadow, boolean renders3DInInventory, List<AnvilPartPlan> parts) {
    }

    public record AnvilPartPlan(String partName, int iconSide, int iconMetadata,
                                float yawRadians, float pitchRadians, boolean shadow) {
    }

    public record OrientationPlan(float yawRadians, float pitchRadians) {
    }

    public record MultiPartObjRenderPlan(int metadata, TranslationPlan translation,
                                         float yawRadians, float pitchRadians, float scale,
                                         boolean shadeNormals, boolean renders3DInInventory,
                                         List<NamedIconPartPlan> parts) {
    }

    public record SplitterRenderPlan(int metadata, boolean left, TranslationPlan translation,
                                     float yawRadians, float pitchRadians, float scale,
                                     boolean shadeNormals, boolean renders3DInInventory,
                                     List<NamedIconPartPlan> primaryParts,
                                     TranslationPlan secondaryInventoryTranslation,
                                     List<NamedIconPartPlan> secondaryInventoryParts) {
    }

    public record DiodeRenderPlan(int metadata, TranslationPlan translation, float inventoryYawRadians,
                                  boolean renders3DInInventory, DiodeBoundsPlan bounds,
                                  List<NamedIconPartPlan> cableParts) {
    }

    public record DiodeBoundsPlan(LegacyAtlasCuboidRenderer.CuboidBounds directionalBounds,
                                  LegacyAtlasCuboidRenderer.CuboidBounds padBounds,
                                  String padIconRole, boolean clearOverrideTextureAfterPad) {
    }

    public record ConveyorMetadataPlan(int originalMetadata, int normalizedMetadata, boolean bent) {
    }

    public record ConveyorRenderPlan(int metadata, int normalizedMetadata, boolean bent,
                                     TranslationPlan translation, boolean renders3DInInventory,
                                     List<CuboidUvPlan> cuboids) {
    }

    public record ConveyorChuteRenderPlan(int metadata, TranslationPlan translation,
                                          boolean renders3DInInventory, boolean beltRendered,
                                          boolean hasBelow, boolean belowConveyorOrEnterable,
                                          boolean negXConveyor, boolean posXConveyor,
                                          boolean negZConveyor, boolean posZConveyor,
                                          List<CuboidUvPlan> beltCuboids, List<CuboidUvPlan> frameCuboids,
                                          List<CuboidUvPlan> glassCuboids) {
    }

    public record ConveyorLiftRenderPlan(int metadata, boolean bottom, boolean top,
                                         boolean hasBelow, boolean hasAbove,
                                         boolean belowConveyor, boolean aboveConveyor,
                                         boolean aboveEnterable, boolean renders3DInInventory,
                                         List<CuboidUvPlan> cuboids) {
    }

    public record BoxDuctRenderPlan(String kind, int metadata, int iconType, TranslationPlan translation,
                                    ConnectionMaskPlan connections, double lower, double upper,
                                    double junctionLower, double junctionUpper, int cachedColor,
                                    boolean lightenedFluidColor, boolean renders3DInInventory,
                                    List<FaceIconCuboidPlan> inventoryCuboids,
                                    List<CuboidUvPlan> worldCuboids) {
    }

    public record ObjConnectionRenderPlan(String legacyResourceModel, int metadata, TranslationPlan translation,
                                          float yawRadians, float pitchRadians, float scale,
                                          boolean shadeNormals, boolean usesOverrideTexture,
                                          boolean renders3DInInventory, ConnectionMaskPlan connections,
                                          List<NamedIconPartPlan> parts) {
    }

    public record PipeNeoRenderPlan(int metadata, TranslationPlan translation,
                                    float yawRadians, float pitchRadians, float scale,
                                    boolean shadeNormals, boolean usesOverrideTexture,
                                    boolean renders3DInInventory, ConnectionMaskPlan connections,
                                    int overlayColor, List<LayeredObjPartPlan> parts) {
    }

    public record LegacyPipeModelRenderPlan(int renderType, int metadata, TranslationPlan translation,
                                            float yawRadians, float pitchRadians, boolean shadeNormals,
                                            boolean renders3DInInventory, List<NamedIconPartPlan> parts) {
    }

    public record PneumoTubeRenderPlan(TranslationPlan translation, ConnectionMaskPlan connections,
                                       boolean compressorOrEndpoint, Direction insertionDirection,
                                       Direction ejectionDirection, List<Direction> airConnectors,
                                       boolean renders3DInInventory, List<FaceIconCuboidPlan> cuboids) {
    }

    public record DuctBoundsPlan(double lower, double upper, double junctionLower, double junctionUpper) {
    }

    public record ConnectionMaskPlan(boolean posX, boolean negX, boolean posY, boolean negY,
                                     boolean posZ, boolean negZ, int mask, int count) {
    }

    public record CuboidUvPlan(String role, LegacyAtlasCuboidRenderer.CuboidBounds bounds,
                               String iconRole, int iconMetadata, UvRotationPlan uvRotations,
                               boolean overrideTexture) {
    }

    public record FaceIconCuboidPlan(String role, LegacyAtlasCuboidRenderer.CuboidBounds bounds,
                                     String topIconRole, String bottomIconRole,
                                     String northIconRole, String southIconRole,
                                     String eastIconRole, String westIconRole,
                                     int iconMetadata, UvRotationPlan uvRotations,
                                     boolean overrideTexture) {
    }

    public record UvRotationPlan(int top, int bottom, int north, int south, int east, int west,
                                 boolean resetAfterRender) {
    }

    public record FoundryOpenVesselRenderPlan(String kind, TranslationPlan translation,
                                              LegacyAtlasCuboidRenderer.CuboidBounds inventoryBounds,
                                              boolean renders3DInInventory, boolean worldRender,
                                              List<FoundryFaceDrawPlan> faces,
                                              FoundryColorPlan colorPlan,
                                              List<FoundryFluidSurfacePlan> fluidSurfaces) {
    }

    public record FoundryOutletRenderPlan(int metadata, TranslationPlan translation,
                                          LegacyAtlasCuboidRenderer.CuboidBounds coreBounds,
                                          boolean hasFilter, boolean closed,
                                          boolean renders3DInInventory, boolean worldRender,
                                          FoundryColorPlan colorPlan,
                                          List<FoundryFaceDrawPlan> faces) {
    }

    public record FoundryTankRenderPlan(TranslationPlan translation, FoundryConnectionPlan connections,
                                        FoundryOutletConnectionPlan outletConnections,
                                        FoundryColorPlan colorPlan, FoundryFluidStatePlan fluidState,
                                        List<FoundryFaceDrawPlan> shellFaces,
                                        List<FoundryFluidSurfacePlan> fluidSurfaces) {
    }

    public record FoundryChannelRenderPlan(TranslationPlan translation, FoundryConnectionPlan connections,
                                           boolean renders3DInInventory, boolean worldRender,
                                           FoundryColorPlan colorPlan, FoundryFluidStatePlan fluidState,
                                           List<FoundryFaceDrawPlan> shellFaces,
                                           List<FoundryFluidSurfacePlan> fluidSurfaces) {
    }

    public record FoundryConnectionPlan(boolean posX, boolean negX, boolean posY, boolean negY,
                                        boolean posZ, boolean negZ) {
    }

    public record FoundryOutletConnectionPlan(boolean posX, boolean negX, boolean posZ, boolean negZ) {
    }

    public record FoundryFaceDrawPlan(Direction direction, String iconRole,
                                      LegacyAtlasCuboidRenderer.CuboidBounds boundsOverride,
                                      double offsetX, double offsetY, double offsetZ,
                                      String colorRole, boolean alternateUResetRequired,
                                      boolean fullBright) {
    }

    public record FoundryColorPlan(int originalColor, boolean anaglyph, int color,
                                   float red, float green, float blue,
                                   float topMultiplier, float bottomMultiplier,
                                   float zMultiplier, float xMultiplier) {
    }

    public record FoundryFluidStatePlan(boolean present, int amount, int capacity, int moltenColor,
                                        double maxLevel, double level, int displayColor, double startY) {
    }

    public record FoundryFluidSurfacePlan(String role, LegacyAtlasCuboidRenderer.CuboidBounds bounds,
                                          String iconRole, int color, boolean fullBright) {
    }

    public record PartitionerRenderPlan(int metadata, TranslationPlan translation, float inventoryYawRadians,
                                        float yawRadians, boolean shadeNormals, boolean renders3DInInventory,
                                        List<NamedIconPartPlan> parts) {
    }

    public record NamedIconPartPlan(String legacyResourceModel, String partName, String iconRole,
                                    int iconSide, int iconMetadata, float yawRadians, float pitchRadians,
                                    boolean shadeNormals) {
    }

    public record LayeredObjPartPlan(String legacyResourceModel, String partName,
                                     String baseIconRole, String overlayIconRole,
                                     int baseIconSide, int overlayIconSide, int iconMetadata,
                                     int overlayColor, float yawRadians, float pitchRadians,
                                     boolean shadeNormals) {
    }

    public record SpotlightPartRenderPlan(String legacyResourceModel, String partName, int iconMetadata,
                                          TranslationPlan translation, float yawRadians, float pitchRadians,
                                          float rollRadians, float scale, boolean shadow,
                                          boolean usesOverrideTexture, boolean renders3DInInventory,
                                          Direction axis, int connectionCount, Direction connectionDirection,
                                          List<Direction> connectableDirections) {
    }

    public record SpotlightConnectionPlan(Direction axis, Direction connectionDirection, int connectionCount,
                                          float rollRadians, List<Direction> connectableDirections) {
    }

    public enum SpotlightModelKind {
        INCANDESCENT("cage_lamp", List.of("CageLamp")),
        FLUORESCENT("fluorescent_lamp", List.of("CageLamp")),
        HALOGEN("flood_lamp", List.of("FloodLamp")),
        FLUORESCENT_MODULAR("fluorescent_lamp", List.of("FluoroSingle", "FluoroCap", "FluoroMid"));

        private final String legacyResourceModel;
        private final List<String> partNames;

        SpotlightModelKind(String legacyResourceModel, List<String> partNames) {
            this.legacyResourceModel = legacyResourceModel;
            this.partNames = partNames;
        }

        public String legacyResourceModel() {
            return legacyResourceModel;
        }

        public String partName(int connectionCount) {
            return partNames.get(Math.min(connectionCount, partNames.size() - 1));
        }

        public List<String> partNames() {
            return partNames;
        }
    }

    public record MetalFenceRenderPlan(int metadata, boolean xNeg, boolean xPos, boolean zNeg, boolean zPos,
                                       boolean hasX, boolean hasZ, boolean straightX, boolean straightZ,
                                       boolean showPost, boolean forcedXWhenIsolated,
                                       boolean alternateUResetRequired,
                                       LegacyAtlasCuboidRenderer.CuboidBounds xRailBounds,
                                       LegacyAtlasCuboidRenderer.CuboidBounds zRailBounds,
                                       LegacyAtlasCuboidRenderer.CuboidBounds postBounds) {
    }

    public record LegacyQuadPlan(String role, List<LegacyVertex> vertices) {
    }

    public record LegacyVertex(double x, double y, double z, double u, double v) {
    }

    public record TranslationPlan(double x, double y, double z) {
    }

    public record ColorPlan(int color, float red, float green, float blue) {
    }

    private LegacyIsbrhBlockPlans() {
    }
}
