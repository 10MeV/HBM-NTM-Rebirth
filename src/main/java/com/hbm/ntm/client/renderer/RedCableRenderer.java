package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.HbmEnergyNodeBlock;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.block.RedCableBlock;
import com.hbm.ntm.block.RedCableBoxBlock;
import com.hbm.ntm.block.RedWireCoatedCt;
import com.hbm.ntm.block.RedWireCoatedBlock;
import com.hbm.ntm.blockentity.RedCableBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
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
import net.minecraft.world.phys.Vec3;

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
    private static final Direction[] RENDER_DIRECTIONS = Direction.values();
    private static final TextureAtlasSprite COATED_BASE = sprite("red_wire_coated");
    private static final TextureAtlasSprite COATED_CT = sprite("red_wire_coated_ct");
    private static final CtSpriteFragment[] COATED_FRAGMENTS = buildCoatedFragments();
    private static final CoatedSubFaceDraw[][][] COATED_SUB_FACE_DRAWS_BY_FACE = buildCoatedSubFaceDrawsByFace();

    public RedCableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(RedCableBlockEntity cable, Vec3 cameraPos) {
        return isRenderableCableState(cable.getBlockState())
                && BlockEntityRenderer.super.shouldRender(cable, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(cable, getViewDistance());
    }

    @Override
    public void render(RedCableBlockEntity cable, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = cable.getBlockState();
        if (!isRenderableCableState(state)) {
            return;
        }
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(cable, getViewDistance())) {
            return;
        }
        int light = LegacyRenderLighting.resolveMultiblockLight(cable, packedLight);
        if (state.getBlock() instanceof RedCableBoxBlock) {
            try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(cable)) {
                renderBoxCable(state, poseStack, buffer, light, packedOverlay);
            }
            return;
        }
        if (state.getBlock() instanceof RedWireCoatedBlock) {
            try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(cable)) {
                renderCoatedCable(cable, state, poseStack, buffer, light, packedOverlay);
            }
            return;
        }
        boolean posX = state.getValue(HbmEnergyNodeBlock.EAST);
        boolean negX = state.getValue(HbmEnergyNodeBlock.WEST);
        boolean posY = state.getValue(HbmEnergyNodeBlock.UP);
        boolean negY = state.getValue(HbmEnergyNodeBlock.DOWN);
        boolean posZ = state.getValue(HbmEnergyNodeBlock.SOUTH);
        boolean negZ = state.getValue(HbmEnergyNodeBlock.NORTH);

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(cable)) {
            renderWorldCable(poseStack, buffer, light, packedOverlay, posX, negX, posY, negY, posZ, negZ);
        }
    }

    private static boolean isRenderableCableState(BlockState state) {
        return (state.getBlock() instanceof RedCableBoxBlock
                        && LegacyMachineRenderShapes.renderChunkBakedStaticsInBer())
                || (state.getBlock() instanceof RedWireCoatedBlock
                        && LegacyMachineRenderShapes.renderChunkBakedStaticsInBer())
                || (state.getBlock() instanceof RedCableBlock block && block.usesBlockEntityRenderer(state));
    }

    static void renderItemCable(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjBlockModels.CABLE_NEO.renderOnlyInCallOrder(CABLE_TEXTURE, poseStack, buffer, packedLight, packedOverlay,
                ITEM_HANDLE);
    }

    static void renderItemBoxCable(int size, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        int clampedSize = clampBoxCableSize(size);
        BoxCableTextures textures = BOX_CABLE_TEXTURES_BY_SIZE[clampedSize];
        BoxCableBounds bounds = BOX_CABLE_BOUNDS_BY_SIZE[clampedSize];
        LegacyTexturedQuadRenderer.SpriteQuadBatch batch = LegacyTexturedQuadRenderer.spriteQuadBatch(
                poseStack, buffer, LegacyTexturedRenderMode.CUTOUT_NO_CULL, 255);
        renderBoxCableStraightZ(textures, batch, packedLight, packedOverlay,
                bounds.lower(), bounds.lower(), 0.0D,
                bounds.upper(), bounds.upper(), 1.0D);
    }

    static void renderCableArms(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            boolean posX, boolean negX, boolean posY, boolean negY, boolean posZ, boolean negZ) {
        renderCableSelection(poseStack, buffer, packedLight, packedOverlay,
                ARM_HANDLES[connectionMask(posX, negX, posY, negY, posZ, negZ)]);
    }

    private static void renderWorldCable(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, boolean posX, boolean negX, boolean posY, boolean negY, boolean posZ, boolean negZ) {
        renderCableSelection(poseStack, buffer, packedLight, packedOverlay,
                WORLD_HANDLES[connectionMask(posX, negX, posY, negY, posZ, negZ)]);
    }

    private static void renderCableSelection(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, LegacyWavefrontModel.SelectionHandle handle) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        ObjBlockModels.CABLE_NEO.renderOnlyInCallOrder(CABLE_TEXTURE, poseStack, buffer, packedLight, packedOverlay,
                handle);
        poseStack.popPose();
    }

    private static void renderBoxCable(BlockState state, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        int size = state.hasProperty(RedCableBoxBlock.SIZE) ? state.getValue(RedCableBoxBlock.SIZE) : 0;
        int clampedSize = clampBoxCableSize(size);
        BoxCableTextures textures = BOX_CABLE_TEXTURES_BY_SIZE[clampedSize];
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
        LegacyTexturedQuadRenderer.SpriteQuadBatch batch = LegacyTexturedQuadRenderer.spriteQuadBatch(
                poseStack, buffer, LegacyTexturedRenderMode.CUTOUT_NO_CULL, 255);

        if (mask == 0) {
            renderBoxCableCube(textures.junction(), batch, packedLight, packedOverlay,
                    bounds.lower(), bounds.lower(), bounds.lower(), bounds.upper(), bounds.upper(), bounds.upper());
        } else if ((mask & 0b001111) == 0) {
            renderBoxCableStraightX(textures, batch, packedLight, packedOverlay,
                    0.0D, bounds.lower(), bounds.lower(),
                    1.0D, bounds.upper(), bounds.upper());
        } else if ((mask & 0b111100) == 0) {
            renderBoxCableStraightZ(textures, batch, packedLight, packedOverlay,
                    bounds.lower(), bounds.lower(), 0.0D,
                    bounds.upper(), bounds.upper(), 1.0D);
        } else if ((mask & 0b110011) == 0) {
            renderBoxCableStraightY(textures, batch, packedLight, packedOverlay,
                    bounds.lower(), 0.0D, bounds.lower(),
                    bounds.upper(), 1.0D, bounds.upper());
        } else {
            boolean curve = count == 2;
            renderBoxCableConnected(textures, batch, packedLight, packedOverlay, curve,
                    north, east, south, west, up, down,
                    bounds.lower(), bounds.lower(), bounds.lower(), bounds.upper(), bounds.upper(), bounds.upper());
            renderBoxCableArms(textures, batch, packedLight, packedOverlay, bounds,
                    curve, north, east, south, west, up, down);
        }
    }

    private static void renderBoxCableArms(BoxCableTextures textures, LegacyTexturedQuadRenderer.SpriteQuadBatch batch,
            int packedLight, int packedOverlay, BoxCableBounds bounds, boolean curve, boolean north, boolean east,
            boolean south, boolean west, boolean up, boolean down) {
        if (north) {
            renderBoxCableConnected(textures, batch, packedLight, packedOverlay, curve,
                    north, east, south, west, up, down,
                    bounds.lower(), bounds.lower(), 0.0D, bounds.upper(), bounds.upper(), bounds.lower());
        }
        if (east) {
            renderBoxCableConnected(textures, batch, packedLight, packedOverlay, curve,
                    north, east, south, west, up, down,
                    bounds.upper(), bounds.lower(), bounds.lower(), 1.0D, bounds.upper(), bounds.upper());
        }
        if (south) {
            renderBoxCableConnected(textures, batch, packedLight, packedOverlay, curve,
                    north, east, south, west, up, down,
                    bounds.lower(), bounds.lower(), bounds.upper(), bounds.upper(), bounds.upper(), 1.0D);
        }
        if (west) {
            renderBoxCableConnected(textures, batch, packedLight, packedOverlay, curve,
                    north, east, south, west, up, down,
                    0.0D, bounds.lower(), bounds.lower(), bounds.lower(), bounds.upper(), bounds.upper());
        }
        if (up) {
            renderBoxCableConnected(textures, batch, packedLight, packedOverlay, curve,
                    north, east, south, west, up, down,
                    bounds.lower(), bounds.upper(), bounds.lower(), bounds.upper(), 1.0D, bounds.upper());
        }
        if (down) {
            renderBoxCableConnected(textures, batch, packedLight, packedOverlay, curve,
                    north, east, south, west, up, down,
                    bounds.lower(), 0.0D, bounds.lower(), bounds.upper(), bounds.lower(), bounds.upper());
        }
    }

    private static void renderBoxCableStraightX(BoxCableTextures textures,
            LegacyTexturedQuadRenderer.SpriteQuadBatch batch,
            int packedLight, int packedOverlay, double minX, double minY, double minZ, double maxX, double maxY,
            double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(textures.straight(), textures.straight(), textures.straight(),
                textures.straight(), textures.end(), textures.end(), batch, packedLight, packedOverlay,
                0xFFFFFF, 255, minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static void renderBoxCableStraightY(BoxCableTextures textures,
            LegacyTexturedQuadRenderer.SpriteQuadBatch batch,
            int packedLight, int packedOverlay, double minX, double minY, double minZ, double maxX, double maxY,
            double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(textures.end(), textures.end(), textures.straight(),
                textures.straight(), textures.straight(), textures.straight(), batch, packedLight,
                packedOverlay, 0xFFFFFF, 255, minX, minY, minZ, maxX, maxY,
                maxZ);
    }

    private static void renderBoxCableStraightZ(BoxCableTextures textures,
            LegacyTexturedQuadRenderer.SpriteQuadBatch batch,
            int packedLight, int packedOverlay, double minX, double minY, double minZ, double maxX, double maxY,
            double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(textures.straight(), textures.straight(), textures.end(),
                textures.end(), textures.straight(), textures.straight(), batch, packedLight,
                packedOverlay, 0xFFFFFF, 255, minX, minY, minZ, maxX, maxY,
                maxZ);
    }

    private static void renderBoxCableConnected(BoxCableTextures textures,
            LegacyTexturedQuadRenderer.SpriteQuadBatch batch, int packedLight, int packedOverlay, boolean curve,
            boolean north, boolean east, boolean south, boolean west, boolean up, boolean down, double minX,
            double minY, double minZ, double maxX, double maxY, double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(
                boxCableFaceTexture(textures, Direction.UP, curve, north, east, south, west, up, down),
                boxCableFaceTexture(textures, Direction.DOWN, curve, north, east, south, west, up, down),
                boxCableFaceTexture(textures, Direction.NORTH, curve, north, east, south, west, up, down),
                boxCableFaceTexture(textures, Direction.SOUTH, curve, north, east, south, west, up, down),
                boxCableFaceTexture(textures, Direction.EAST, curve, north, east, south, west, up, down),
                boxCableFaceTexture(textures, Direction.WEST, curve, north, east, south, west, up, down),
                batch, packedLight, packedOverlay, 0xFFFFFF, 255,
                minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static void renderBoxCableCube(TextureAtlasSprite sprite, LegacyTexturedQuadRenderer.SpriteQuadBatch batch,
            int packedLight, int packedOverlay, double minX, double minY, double minZ, double maxX, double maxY,
            double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(sprite, batch, packedLight, packedOverlay,
                0xFFFFFF, 255, minX, minY, minZ, maxX, maxY, maxZ);
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
        BlockGetter level = cable.getLevel();
        BlockPos pos = cable.getBlockPos();
        RedWireCoatedCt.Data data = RedWireCoatedCt.compute(level, pos, state);
        LegacyTexturedQuadRenderer.SpriteQuadBatch batch = null;
        for (Direction face : RENDER_DIRECTIONS) {
            if (!data.isFaceVisible(face)) {
                continue;
            }
            if (batch == null) {
                batch = LegacyTexturedQuadRenderer.spriteQuadBatch(poseStack, buffer,
                        LegacyTexturedRenderMode.CUTOUT_NO_CULL, 255);
            }
            renderCoatedFace(batch, packedLight, packedOverlay, face, data.face(face));
        }
    }

    private static int ctFaceFragment(int ctFace, int index) {
        int fragment = (ctFace >> (index * 5)) & 31;
        return Math.max(0, Math.min(COATED_FRAGMENTS.length - 1, fragment));
    }

    private static void renderCoatedFace(LegacyTexturedQuadRenderer.SpriteQuadBatch batch, int packedLight,
            int packedOverlay, Direction face, int ctFace) {
        CoatedSubFaceDraw[][] draws = COATED_SUB_FACE_DRAWS_BY_FACE[face.ordinal()];
        drawCoatedSubFace(batch, packedLight, packedOverlay, face, draws[0][ctFaceFragment(ctFace, 0)]);
        drawCoatedSubFace(batch, packedLight, packedOverlay, face, draws[1][ctFaceFragment(ctFace, 1)]);
        drawCoatedSubFace(batch, packedLight, packedOverlay, face, draws[2][ctFaceFragment(ctFace, 2)]);
        drawCoatedSubFace(batch, packedLight, packedOverlay, face, draws[3][ctFaceFragment(ctFace, 3)]);
    }

    private static void drawCoatedSubFace(LegacyTexturedQuadRenderer.SpriteQuadBatch batch, int packedLight,
            int packedOverlay, Direction face, CoatedSubFaceDraw draw) {
        LegacyTexturedQuadRenderer.spritePixelQuadDirect(draw.sprite(), batch, packedLight, packedOverlay,
                face.getStepX(), face.getStepY(), face.getStepZ(),
                draw.x0(), draw.y0(), draw.z0(), draw.u0(), draw.v0(),
                draw.x1(), draw.y1(), draw.z1(), draw.u1(), draw.v1(),
                draw.x2(), draw.y2(), draw.z2(), draw.u2(), draw.v2(),
                draw.x3(), draw.y3(), draw.z3(), draw.u3(), draw.v3(),
                0xFFFFFF, 255);
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
        return LegacyTexturedQuadRenderer.blockSprite(HbmNtm.MOD_ID, "block/" + texture);
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

    private static CoatedSubFaceDraw[][][] buildCoatedSubFaceDrawsByFace() {
        CoatedSubFaceDraw[][][] draws = new CoatedSubFaceDraw[Direction.values().length][][];
        for (Direction face : RENDER_DIRECTIONS) {
            draws[face.ordinal()] = buildCoatedSubFaceDraws(face);
        }
        return draws;
    }

    private static CoatedSubFaceDraw[][] buildCoatedSubFaceDraws(Direction face) {
        return switch (face) {
            case EAST -> buildCoatedSubFaceDraws(
                    1.0D, 1.0D, 1.0D,
                    1.0D, 1.0D, 0.0D,
                    1.0D, 0.0D, 1.0D,
                    1.0D, 0.0D, 0.0D);
            case WEST -> buildCoatedSubFaceDraws(
                    0.0D, 1.0D, 0.0D,
                    0.0D, 1.0D, 1.0D,
                    0.0D, 0.0D, 0.0D,
                    0.0D, 0.0D, 1.0D);
            case UP -> buildCoatedSubFaceDraws(
                    0.0D, 1.0D, 0.0D,
                    1.0D, 1.0D, 0.0D,
                    0.0D, 1.0D, 1.0D,
                    1.0D, 1.0D, 1.0D);
            case DOWN -> buildCoatedSubFaceDraws(
                    0.0D, 0.0D, 1.0D,
                    1.0D, 0.0D, 1.0D,
                    0.0D, 0.0D, 0.0D,
                    1.0D, 0.0D, 0.0D);
            case SOUTH -> buildCoatedSubFaceDraws(
                    0.0D, 1.0D, 1.0D,
                    1.0D, 1.0D, 1.0D,
                    0.0D, 0.0D, 1.0D,
                    1.0D, 0.0D, 1.0D);
            case NORTH -> buildCoatedSubFaceDraws(
                    1.0D, 1.0D, 0.0D,
                    0.0D, 1.0D, 0.0D,
                    1.0D, 0.0D, 0.0D,
                    0.0D, 0.0D, 0.0D);
        };
    }

    private static CoatedSubFaceDraw[][] buildCoatedSubFaceDraws(double topLeftX, double topLeftY, double topLeftZ,
            double topRightX, double topRightY, double topRightZ,
            double bottomLeftX, double bottomLeftY, double bottomLeftZ,
            double bottomRightX, double bottomRightY, double bottomRightZ) {
        double topCenterX = average(topLeftX, topRightX);
        double topCenterY = average(topLeftY, topRightY);
        double topCenterZ = average(topLeftZ, topRightZ);
        double bottomCenterX = average(bottomLeftX, bottomRightX);
        double bottomCenterY = average(bottomLeftY, bottomRightY);
        double bottomCenterZ = average(bottomLeftZ, bottomRightZ);
        double centerLeftX = average(topLeftX, bottomLeftX);
        double centerLeftY = average(topLeftY, bottomLeftY);
        double centerLeftZ = average(topLeftZ, bottomLeftZ);
        double centerRightX = average(topRightX, bottomRightX);
        double centerRightY = average(topRightY, bottomRightY);
        double centerRightZ = average(topRightZ, bottomRightZ);
        double centerX = average(topCenterX, bottomCenterX);
        double centerY = average(topCenterY, bottomCenterY);
        double centerZ = average(topCenterZ, bottomCenterZ);

        return new CoatedSubFaceDraw[][] {
                buildCoatedFragmentDraws(topLeftX, topLeftY, topLeftZ,
                        topCenterX, topCenterY, topCenterZ,
                        centerLeftX, centerLeftY, centerLeftZ,
                        centerX, centerY, centerZ),
                buildCoatedFragmentDraws(topCenterX, topCenterY, topCenterZ,
                        topRightX, topRightY, topRightZ,
                        centerX, centerY, centerZ,
                        centerRightX, centerRightY, centerRightZ),
                buildCoatedFragmentDraws(centerLeftX, centerLeftY, centerLeftZ,
                        centerX, centerY, centerZ,
                        bottomLeftX, bottomLeftY, bottomLeftZ,
                        bottomCenterX, bottomCenterY, bottomCenterZ),
                buildCoatedFragmentDraws(centerX, centerY, centerZ,
                        centerRightX, centerRightY, centerRightZ,
                        bottomCenterX, bottomCenterY, bottomCenterZ,
                        bottomRightX, bottomRightY, bottomRightZ)
        };
    }

    private static CoatedSubFaceDraw[] buildCoatedFragmentDraws(double topLeftX, double topLeftY, double topLeftZ,
            double topRightX, double topRightY, double topRightZ,
            double bottomLeftX, double bottomLeftY, double bottomLeftZ,
            double bottomRightX, double bottomRightY, double bottomRightZ) {
        CoatedSubFaceDraw[] draws = new CoatedSubFaceDraw[COATED_FRAGMENTS.length];
        for (int fragment = 0; fragment < draws.length; fragment++) {
            CtSpriteFragment sprite = ctSpriteFragment(fragment);
            draws[fragment] = new CoatedSubFaceDraw(sprite.sprite(),
                    topRightX, topRightY, topRightZ, sprite.maxU(), sprite.minV(),
                    topLeftX, topLeftY, topLeftZ, sprite.minU(), sprite.minV(),
                    bottomLeftX, bottomLeftY, bottomLeftZ, sprite.minU(), sprite.maxV(),
                    bottomRightX, bottomRightY, bottomRightZ, sprite.maxU(), sprite.maxV());
        }
        return draws;
    }

    private static double average(double a, double b) {
        return (a + b) * 0.5D;
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

    private record CoatedSubFaceDraw(TextureAtlasSprite sprite,
                                     double x0, double y0, double z0, double u0, double v0,
                                     double x1, double y1, double z1, double u1, double v1,
                                     double x2, double y2, double z2, double u2, double v2,
                                     double x3, double y3, double z3, double u3, double v3) {
    }

    private record CtSpriteFragment(TextureAtlasSprite sprite, double minU, double maxU, double minV, double maxV) {
    }
}
