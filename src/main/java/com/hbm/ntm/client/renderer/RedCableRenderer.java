package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.HbmEnergyNodeBlock;
import com.hbm.ntm.block.RedCableBlock;
import com.hbm.ntm.block.RedCableBoxBlock;
import com.hbm.ntm.block.RedWireCoatedBlock;
import com.hbm.ntm.blockentity.RedCableBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class RedCableRenderer implements BlockEntityRenderer<RedCableBlockEntity> {
    public static final ResourceLocation CABLE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/legacy_blocks/cable_neo.png");
    private static final String[] ITEM_PARTS = {"Core", "posX", "negX", "posZ", "negZ"};
    private static final String[][] WORLD_PARTS_BY_MASK = buildCablePartsByMask(true);
    private static final String[][] ARM_PARTS_BY_MASK = buildCablePartsByMask(false);
    private static final LegacyWavefrontModel.SelectionHandle ITEM_HANDLE =
            ObjBlockModels.CABLE_NEO.prepareRenderOnlyInCallOrder(ITEM_PARTS);
    private static final LegacyWavefrontModel.SelectionHandle[] WORLD_HANDLES = buildHandles(WORLD_PARTS_BY_MASK);
    private static final LegacyWavefrontModel.SelectionHandle[] ARM_HANDLES = buildHandles(ARM_PARTS_BY_MASK);
    private static final BoxCableTextures[] BOX_CABLE_TEXTURES_BY_SIZE = buildBoxCableTextures();
    private static final BoxCableBounds[] BOX_CABLE_BOUNDS_BY_SIZE = buildBoxCableBounds();
    private static final TextureAtlasSprite COATED_BASE = sprite("red_wire_coated");
    private static final TextureAtlasSprite COATED_CT = sprite("red_wire_coated_ct");
    private static final CtSpriteFragment[] COATED_FRAGMENTS = buildCoatedFragments();

    public RedCableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RedCableBlockEntity cable, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = cable.getBlockState();
        int light = LegacyRenderLighting.resolveMultiblockLight(cable, packedLight);
        if (state.getBlock() instanceof RedCableBoxBlock) {
            renderBoxCable(state, poseStack, buffer, light, packedOverlay);
            return;
        }
        if (state.getBlock() instanceof RedWireCoatedBlock) {
            renderCoatedCable(cable, state, poseStack, buffer, light, packedOverlay);
            return;
        }
        if (!(state.getBlock() instanceof RedCableBlock block) || !block.usesBlockEntityRenderer(state)) {
            return;
        }

        boolean posX = state.getValue(HbmEnergyNodeBlock.EAST);
        boolean negX = state.getValue(HbmEnergyNodeBlock.WEST);
        boolean posY = state.getValue(HbmEnergyNodeBlock.UP);
        boolean negY = state.getValue(HbmEnergyNodeBlock.DOWN);
        boolean posZ = state.getValue(HbmEnergyNodeBlock.SOUTH);
        boolean negZ = state.getValue(HbmEnergyNodeBlock.NORTH);

        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, light, packedOverlay);
        renderWorldCable(context, posX, negX, posY, negY, posZ, negZ);
    }

    static void renderItemCable(ObjRenderContext context) {
        ObjBlockModels.CABLE_NEO.renderOnlyInCallOrder(CABLE_TEXTURE, context, ITEM_HANDLE);
    }

    static void renderCableArms(ObjRenderContext context,
            boolean posX, boolean negX, boolean posY, boolean negY, boolean posZ, boolean negZ) {
        renderCableSelection(context, ARM_HANDLES[connectionMask(posX, negX, posY, negY, posZ, negZ)]);
    }

    private static void renderWorldCable(ObjRenderContext context,
            boolean posX, boolean negX, boolean posY, boolean negY, boolean posZ, boolean negZ) {
        renderCableSelection(context, WORLD_HANDLES[connectionMask(posX, negX, posY, negY, posZ, negZ)]);
    }

    private static void renderCableSelection(ObjRenderContext context, LegacyWavefrontModel.SelectionHandle handle) {
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        ObjBlockModels.CABLE_NEO.renderOnlyInCallOrder(CABLE_TEXTURE, context, handle);
        poseStack.popPose();
    }

    private static void renderBoxCable(BlockState state, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        int size = state.hasProperty(RedCableBoxBlock.SIZE) ? state.getValue(RedCableBoxBlock.SIZE) : 0;
        int clampedSize = clampBoxCableSize(size);
        BoxCableTextures textures = BOX_CABLE_TEXTURES_BY_SIZE[clampedSize];
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay);
        boolean north = state.getValue(HbmEnergyNodeBlock.NORTH);
        boolean east = state.getValue(HbmEnergyNodeBlock.EAST);
        boolean south = state.getValue(HbmEnergyNodeBlock.SOUTH);
        boolean west = state.getValue(HbmEnergyNodeBlock.WEST);
        boolean up = state.getValue(HbmEnergyNodeBlock.UP);
        boolean down = state.getValue(HbmEnergyNodeBlock.DOWN);
        BoxCableBounds bounds = BOX_CABLE_BOUNDS_BY_SIZE[clampedSize];
        int mask = (east ? 32 : 0)
                | (west ? 16 : 0)
                | (up ? 8 : 0)
                | (down ? 4 : 0)
                | (south ? 2 : 0)
                | (north ? 1 : 0);
        int count = (north ? 1 : 0) + (east ? 1 : 0) + (south ? 1 : 0) + (west ? 1 : 0)
                + (up ? 1 : 0) + (down ? 1 : 0);

        if (mask == 0) {
            renderBoxCableCube(textures.junction(), context, bounds.lower(), bounds.lower(), bounds.lower(),
                    bounds.upper(), bounds.upper(), bounds.upper());
        } else if ((mask & 0b001111) == 0) {
            renderBoxCableStraightX(textures, context, 0.0D, bounds.lower(), bounds.lower(),
                    1.0D, bounds.upper(), bounds.upper());
        } else if ((mask & 0b111100) == 0) {
            renderBoxCableStraightZ(textures, context, bounds.lower(), bounds.lower(), 0.0D,
                    bounds.upper(), bounds.upper(), 1.0D);
        } else if ((mask & 0b110011) == 0) {
            renderBoxCableStraightY(textures, context, bounds.lower(), 0.0D, bounds.lower(),
                    bounds.upper(), 1.0D, bounds.upper());
        } else {
            boolean curve = count == 2;
            renderBoxCableConnected(textures, context, curve, north, east, south, west, up, down,
                    bounds.lower(), bounds.lower(), bounds.lower(), bounds.upper(), bounds.upper(), bounds.upper());
            renderBoxCableArms(textures, context, bounds, curve, north, east, south, west, up, down);
        }
    }

    private static void renderBoxCableArms(BoxCableTextures textures, ObjRenderContext context, BoxCableBounds bounds,
            boolean curve, boolean north, boolean east, boolean south, boolean west, boolean up, boolean down) {
        if (north) {
            renderBoxCableConnected(textures, context, curve, north, east, south, west, up, down,
                    bounds.lower(), bounds.lower(), 0.0D, bounds.upper(), bounds.upper(), bounds.lower());
        }
        if (east) {
            renderBoxCableConnected(textures, context, curve, north, east, south, west, up, down,
                    bounds.upper(), bounds.lower(), bounds.lower(), 1.0D, bounds.upper(), bounds.upper());
        }
        if (south) {
            renderBoxCableConnected(textures, context, curve, north, east, south, west, up, down,
                    bounds.lower(), bounds.lower(), bounds.upper(), bounds.upper(), bounds.upper(), 1.0D);
        }
        if (west) {
            renderBoxCableConnected(textures, context, curve, north, east, south, west, up, down,
                    0.0D, bounds.lower(), bounds.lower(), bounds.lower(), bounds.upper(), bounds.upper());
        }
        if (up) {
            renderBoxCableConnected(textures, context, curve, north, east, south, west, up, down,
                    bounds.lower(), bounds.upper(), bounds.lower(), bounds.upper(), 1.0D, bounds.upper());
        }
        if (down) {
            renderBoxCableConnected(textures, context, curve, north, east, south, west, up, down,
                    bounds.lower(), 0.0D, bounds.lower(), bounds.upper(), bounds.lower(), bounds.upper());
        }
    }

    private static void renderBoxCableStraightX(BoxCableTextures textures, ObjRenderContext context, double minX,
            double minY, double minZ, double maxX, double maxY, double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(textures.straight(), textures.straight(), textures.straight(),
                textures.straight(), textures.end(), textures.end(), context, minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static void renderBoxCableStraightY(BoxCableTextures textures, ObjRenderContext context, double minX,
            double minY, double minZ, double maxX, double maxY, double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(textures.end(), textures.end(), textures.straight(),
                textures.straight(), textures.straight(), textures.straight(), context, minX, minY, minZ, maxX, maxY,
                maxZ);
    }

    private static void renderBoxCableStraightZ(BoxCableTextures textures, ObjRenderContext context, double minX,
            double minY, double minZ, double maxX, double maxY, double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(textures.straight(), textures.straight(), textures.end(),
                textures.end(), textures.straight(), textures.straight(), context, minX, minY, minZ, maxX, maxY,
                maxZ);
    }

    private static void renderBoxCableConnected(BoxCableTextures textures, ObjRenderContext context, boolean curve,
            boolean north, boolean east, boolean south, boolean west, boolean up, boolean down, double minX,
            double minY, double minZ, double maxX, double maxY, double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(
                boxCableFaceTexture(textures, Direction.UP, curve, north, east, south, west, up, down),
                boxCableFaceTexture(textures, Direction.DOWN, curve, north, east, south, west, up, down),
                boxCableFaceTexture(textures, Direction.NORTH, curve, north, east, south, west, up, down),
                boxCableFaceTexture(textures, Direction.SOUTH, curve, north, east, south, west, up, down),
                boxCableFaceTexture(textures, Direction.EAST, curve, north, east, south, west, up, down),
                boxCableFaceTexture(textures, Direction.WEST, curve, north, east, south, west, up, down),
                context, minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static void renderBoxCableCube(TextureAtlasSprite sprite, ObjRenderContext context, double minX,
            double minY, double minZ, double maxX, double maxY, double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(sprite, context, minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static TextureAtlasSprite boxCableFaceTexture(BoxCableTextures textures, Direction face, boolean curve,
            boolean north, boolean east, boolean south, boolean west, boolean up, boolean down) {
        if (!curve) {
            return textures.junction();
        }
        if ((face == Direction.DOWN && down) || (face == Direction.UP && up)
                || (face == Direction.NORTH && north) || (face == Direction.SOUTH && south)
                || (face == Direction.WEST && west) || (face == Direction.EAST && east)) {
            return textures.end();
        }
        if ((face == Direction.UP && down) || (face == Direction.DOWN && up)
                || (face == Direction.SOUTH && north) || (face == Direction.NORTH && south)
                || (face == Direction.EAST && west) || (face == Direction.WEST && east)) {
            return textures.straight();
        }

        if (down && south) return face == Direction.WEST ? textures.curveBR() : textures.curveBL();
        if (down && north) return face == Direction.EAST ? textures.curveBR() : textures.curveBL();
        if (down && east) return face == Direction.SOUTH ? textures.curveBR() : textures.curveBL();
        if (down && west) return face == Direction.NORTH ? textures.curveBR() : textures.curveBL();
        if (up && south) return face == Direction.WEST ? textures.curveTR() : textures.curveTL();
        if (up && north) return face == Direction.EAST ? textures.curveTR() : textures.curveTL();
        if (up && east) return face == Direction.SOUTH ? textures.curveTR() : textures.curveTL();
        if (up && west) return face == Direction.NORTH ? textures.curveTR() : textures.curveTL();
        if (east && north) return textures.curveTR();
        if (east && south) return textures.curveBR();
        if (west && north) return textures.curveTL();
        if (west && south) return textures.curveBL();
        return textures.junction();
    }

    private static void renderCoatedCable(RedCableBlockEntity cable, BlockState state, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay);
        BlockGetter level = cable.getLevel();
        BlockPos pos = cable.getBlockPos();
        for (Direction face : Direction.values()) {
            if (level != null && level.getBlockState(pos.relative(face)).is(state.getBlock())) {
                continue;
            }
            CoatedCtFace ctFace = coatedCtFace(level, pos, state, face);
            renderCoatedFace(context, face, ctFace);
        }
    }

    private static CoatedCtFace coatedCtFace(BlockGetter level, BlockPos pos, BlockState state, Direction face) {
        boolean[] cons = new boolean[8];
        int[][] dirs = ctAccess(face);
        for (int i = 0; i < dirs.length; i++) {
            int[] offset = dirs[i];
            BlockPos neighbor = pos.offset(offset[0], offset[1], offset[2]);
            cons[i] = level != null && level.getBlockState(neighbor).is(state.getBlock());
        }
        int tl = 0 | 0 | cornerType(cons[3], cons[0], cons[1]);
        int tr = 0 | 1 | cornerType(cons[4], cons[2], cons[1]);
        int bl = 2 | 0 | cornerType(cons[3], cons[5], cons[6]);
        int br = 2 | 1 | cornerType(cons[4], cons[7], cons[6]);
        return new CoatedCtFace(tl, tr, bl, br);
    }

    private static int[][] ctAccess(Direction face) {
        return switch (face) {
            case DOWN -> lexicalCoordinates(Direction.SOUTH, Direction.WEST);
            case UP -> lexicalCoordinates(Direction.NORTH, Direction.WEST);
            case NORTH -> lexicalCoordinates(Direction.UP, Direction.EAST);
            case SOUTH -> lexicalCoordinates(Direction.UP, Direction.WEST);
            case WEST -> lexicalCoordinates(Direction.UP, Direction.NORTH);
            case EAST -> lexicalCoordinates(Direction.UP, Direction.SOUTH);
        };
    }

    private static int[][] lexicalCoordinates(Direction up, Direction left) {
        Direction down = up.getOpposite();
        Direction right = left.getOpposite();
        return new int[][] {
                coordinatesFromSides(up, left),
                coordinatesFromSides(up),
                coordinatesFromSides(up, right),
                coordinatesFromSides(left),
                coordinatesFromSides(right),
                coordinatesFromSides(down, left),
                coordinatesFromSides(down),
                coordinatesFromSides(down, right)
        };
    }

    private static int[] coordinatesFromSides(Direction... directions) {
        int x = 0;
        int y = 0;
        int z = 0;
        for (Direction direction : directions) {
            x += direction.getStepX();
            y += direction.getStepY();
            z += direction.getStepZ();
        }
        return new int[] { x, y, z };
    }

    private static int cornerType(boolean horizontal, boolean corner, boolean vertical) {
        if (vertical && horizontal && corner) {
            return 4;
        } else if (vertical && horizontal) {
            return 8;
        } else if (vertical) {
            return 16;
        } else if (horizontal) {
            return 12;
        }
        return 0;
    }

    private static void renderCoatedFace(ObjRenderContext context, Direction face, CoatedCtFace ctFace) {
        FaceVertices vertices = faceVertices(face);
        Vec3d topCenter = vertices.topLeft().average(vertices.topRight());
        Vec3d bottomCenter = vertices.bottomLeft().average(vertices.bottomRight());
        Vec3d centerLeft = vertices.topLeft().average(vertices.bottomLeft());
        Vec3d centerRight = vertices.topRight().average(vertices.bottomRight());
        Vec3d center = topCenter.average(bottomCenter);
        drawCoatedSubFace(context, face, vertices.topLeft(), topCenter, centerLeft, center, ctFace.topLeft());
        drawCoatedSubFace(context, face, topCenter, vertices.topRight(), center, centerRight, ctFace.topRight());
        drawCoatedSubFace(context, face, centerLeft, center, vertices.bottomLeft(), bottomCenter, ctFace.bottomLeft());
        drawCoatedSubFace(context, face, center, centerRight, bottomCenter, vertices.bottomRight(), ctFace.bottomRight());
    }

    private static void drawCoatedSubFace(ObjRenderContext context, Direction face, Vec3d topLeft, Vec3d topRight,
            Vec3d bottomLeft, Vec3d bottomRight, int fragment) {
        CtSpriteFragment sprite = ctSpriteFragment(fragment);
        LegacyTexturedQuadRenderer.spriteQuad(sprite.sprite(), context,
                face.getStepX(), face.getStepY(), face.getStepZ(),
                LegacyTexturedQuadRenderer.spritePixelVertex(topRight.x(), topRight.y(), topRight.z(),
                        sprite.maxU(), sprite.minV()),
                LegacyTexturedQuadRenderer.spritePixelVertex(topLeft.x(), topLeft.y(), topLeft.z(),
                        sprite.minU(), sprite.minV()),
                LegacyTexturedQuadRenderer.spritePixelVertex(bottomLeft.x(), bottomLeft.y(), bottomLeft.z(),
                        sprite.minU(), sprite.maxV()),
                LegacyTexturedQuadRenderer.spritePixelVertex(bottomRight.x(), bottomRight.y(), bottomRight.z(),
                        sprite.maxU(), sprite.maxV()));
    }

    private static FaceVertices faceVertices(Direction face) {
        return switch (face) {
            case EAST -> new FaceVertices(
                    new Vec3d(1.0D, 1.0D, 1.0D),
                    new Vec3d(1.0D, 1.0D, 0.0D),
                    new Vec3d(1.0D, 0.0D, 1.0D),
                    new Vec3d(1.0D, 0.0D, 0.0D));
            case WEST -> new FaceVertices(
                    new Vec3d(0.0D, 1.0D, 0.0D),
                    new Vec3d(0.0D, 1.0D, 1.0D),
                    new Vec3d(0.0D, 0.0D, 0.0D),
                    new Vec3d(0.0D, 0.0D, 1.0D));
            case UP -> new FaceVertices(
                    new Vec3d(0.0D, 1.0D, 0.0D),
                    new Vec3d(1.0D, 1.0D, 0.0D),
                    new Vec3d(0.0D, 1.0D, 1.0D),
                    new Vec3d(1.0D, 1.0D, 1.0D));
            case DOWN -> new FaceVertices(
                    new Vec3d(0.0D, 0.0D, 1.0D),
                    new Vec3d(1.0D, 0.0D, 1.0D),
                    new Vec3d(0.0D, 0.0D, 0.0D),
                    new Vec3d(1.0D, 0.0D, 0.0D));
            case SOUTH -> new FaceVertices(
                    new Vec3d(0.0D, 1.0D, 1.0D),
                    new Vec3d(1.0D, 1.0D, 1.0D),
                    new Vec3d(0.0D, 0.0D, 1.0D),
                    new Vec3d(1.0D, 0.0D, 1.0D));
            case NORTH -> new FaceVertices(
                    new Vec3d(1.0D, 1.0D, 0.0D),
                    new Vec3d(0.0D, 1.0D, 0.0D),
                    new Vec3d(1.0D, 0.0D, 0.0D),
                    new Vec3d(0.0D, 0.0D, 0.0D));
        };
    }

    private static CtSpriteFragment ctSpriteFragment(int type) {
        return COATED_FRAGMENTS[Math.max(0, Math.min(COATED_FRAGMENTS.length - 1, type))];
    }

    private static CtSpriteFragment createCtSpriteFragment(int type) {
        boolean base = type < 4;
        TextureAtlasSprite sprite = coatedSprite(type);
        double sub = base ? 2.0D : 4.0D;
        double len = 16.0D / sub;
        double minU = 0.0D;
        double minV = 0.0D;
        if (!base) {
            if (type >= 16 || (type >= 8 && type < 12)) {
                minU += len * 2.0D;
            }
            if ((type >= 12 && type < 16) || (type >= 8 && type < 12)) {
                minV += len * 2.0D;
            }
        }
        if ((type & 1) != 0) {
            minU += len;
        }
        if ((type & 2) != 0) {
            minV += len;
        }
        return new CtSpriteFragment(sprite, minU, minU + len, minV, minV + len);
    }

    private static TextureAtlasSprite coatedSprite(int type) {
        return type < 4 ? COATED_BASE : COATED_CT;
    }

    private static TextureAtlasSprite sprite(String texture) {
        return LegacyTexturedQuadRenderer.blockSprite(new ResourceLocation(HbmNtm.MOD_ID, "block/" + texture));
    }

    private static int clampBoxCableSize(int size) {
        return Math.max(0, Math.min(4, size));
    }

    private static LegacyWavefrontModel.SelectionHandle[] buildHandles(String[][] partsByMask) {
        LegacyWavefrontModel.SelectionHandle[] handles = new LegacyWavefrontModel.SelectionHandle[partsByMask.length];
        for (int mask = 0; mask < partsByMask.length; mask++) {
            handles[mask] = ObjBlockModels.CABLE_NEO.prepareRenderOnlyInCallOrder(partsByMask[mask]);
        }
        return handles;
    }

    private static String[][] buildCablePartsByMask(boolean includeCoreAndStraight) {
        String[][] parts = new String[64][];
        for (int mask = 0; mask < parts.length; mask++) {
            parts[mask] = buildCableParts(mask, includeCoreAndStraight);
        }
        return parts;
    }

    private static String[] buildCableParts(int mask, boolean includeCoreAndStraight) {
        boolean posX = (mask & 32) != 0;
        boolean negX = (mask & 16) != 0;
        boolean posY = (mask & 8) != 0;
        boolean negY = (mask & 4) != 0;
        boolean posZ = (mask & 2) != 0;
        boolean negZ = (mask & 1) != 0;

        if (includeCoreAndStraight) {
            if (posX && negX && !posY && !negY && !posZ && !negZ) {
                return new String[]{"CX"};
            }
            if (!posX && !negX && posY && negY && !posZ && !negZ) {
                return new String[]{"CY"};
            }
            if (!posX && !negX && !posY && !negY && posZ && negZ) {
                return new String[]{"CZ"};
            }
        }

        List<String> parts = new ArrayList<>(includeCoreAndStraight ? 7 : 6);
        if (includeCoreAndStraight) {
            parts.add("Core");
        }
        if (posX) parts.add("posX");
        if (negX) parts.add("negX");
        if (posY) parts.add("posY");
        if (negY) parts.add("negY");
        if (negZ) parts.add("posZ");
        if (posZ) parts.add("negZ");
        return parts.toArray(String[]::new);
    }

    private static int connectionMask(boolean posX, boolean negX, boolean posY, boolean negY, boolean posZ, boolean negZ) {
        return (posX ? 32 : 0)
                | (negX ? 16 : 0)
                | (posY ? 8 : 0)
                | (negY ? 4 : 0)
                | (posZ ? 2 : 0)
                | (negZ ? 1 : 0);
    }

    private static BoxCableTextures[] buildBoxCableTextures() {
        BoxCableTextures[] textures = new BoxCableTextures[5];
        for (int size = 0; size < textures.length; size++) {
            textures[size] = BoxCableTextures.create(size);
        }
        return textures;
    }

    private static BoxCableBounds[] buildBoxCableBounds() {
        BoxCableBounds[] bounds = new BoxCableBounds[5];
        for (int size = 0; size < bounds.length; size++) {
            bounds[size] = BoxCableBounds.create(size);
        }
        return bounds;
    }

    private static CtSpriteFragment[] buildCoatedFragments() {
        CtSpriteFragment[] fragments = new CtSpriteFragment[20];
        for (int type = 0; type < fragments.length; type++) {
            fragments[type] = createCtSpriteFragment(type);
        }
        return fragments;
    }

    private record BoxCableTextures(TextureAtlasSprite straight, TextureAtlasSprite end, TextureAtlasSprite curveTL,
                                    TextureAtlasSprite curveTR, TextureAtlasSprite curveBL,
                                    TextureAtlasSprite curveBR, TextureAtlasSprite junction) {
        static BoxCableTextures create(int size) {
            int clamped = clampBoxCableSize(size);
            return new BoxCableTextures(
                    sprite("boxduct_cable_straight"),
                    sprite("boxduct_cable_end_" + clamped),
                    sprite("boxduct_cable_curve_tl"),
                    sprite("boxduct_cable_curve_tr"),
                    sprite("boxduct_cable_curve_bl"),
                    sprite("boxduct_cable_curve_br"),
                    sprite("boxduct_cable_junction"));
        }
    }

    private record BoxCableBounds(double lower, double upper) {
        static BoxCableBounds create(int size) {
            int clamped = clampBoxCableSize(size);
            double lower = 0.125D + clamped * 0.0625D;
            double upper = 0.875D - clamped * 0.0625D;
            return new BoxCableBounds(lower, upper);
        }
    }

    private record CoatedCtFace(int topLeft, int topRight, int bottomLeft, int bottomRight) {
    }

    private record CtSpriteFragment(TextureAtlasSprite sprite, double minU, double maxU, double minV, double maxV) {
    }

    private record FaceVertices(Vec3d topLeft, Vec3d topRight, Vec3d bottomLeft, Vec3d bottomRight) {
    }

    private record Vec3d(double x, double y, double z) {
        Vec3d average(Vec3d other) {
            return new Vec3d((x + other.x) * 0.5D, (y + other.y) * 0.5D, (z + other.z) * 0.5D);
        }
    }
}
