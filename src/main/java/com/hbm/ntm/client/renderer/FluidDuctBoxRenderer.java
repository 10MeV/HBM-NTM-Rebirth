package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.FluidDuctBoxBlock;
import com.hbm.ntm.block.FluidDuctExhaustBlock;
import com.hbm.ntm.block.HbmFluidNodeBlock;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.util.ColorUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FluidDuctBoxRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private static final String[] MATERIALS = {"silver", "copper", "white"};

    public FluidDuctBoxRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void render(T duct, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        BlockState state = duct.getBlockState();
        if (!(state.getBlock() instanceof FluidDuctBoxBlock)) {
            return;
        }

        int metadata = state.hasProperty(FluidDuctBoxBlock.LEGACY_METADATA)
                ? state.getValue(FluidDuctBoxBlock.LEGACY_METADATA)
                : 0;
        String prefix = state.getBlock() instanceof FluidDuctExhaustBlock
                ? "boxduct_exhaust"
                : "boxduct_" + MATERIALS[FluidDuctBoxBlock.rectifyLegacyMaterial(metadata)];
        TextureSet textures = TextureSet.create(prefix, metadata);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state,
                LegacyRenderLighting.resolveBlockEntityLight(duct, packedLight), packedOverlay);
        if (!(state.getBlock() instanceof FluidDuctExhaustBlock)
                && FluidDuctBoxBlock.rectifyLegacyMaterial(metadata) == 2
                && duct instanceof FluidPipeBlockEntity pipe
                && pipe.getFluidType() != HbmFluids.NONE) {
            context = context.withColor(ColorUtil.lightenColor(pipe.getFluidType().getColor(), 0.25D));
        }

        boolean north = state.getValue(HbmFluidNodeBlock.NORTH);
        boolean east = state.getValue(HbmFluidNodeBlock.EAST);
        boolean south = state.getValue(HbmFluidNodeBlock.SOUTH);
        boolean west = state.getValue(HbmFluidNodeBlock.WEST);
        boolean up = state.getValue(HbmFluidNodeBlock.UP);
        boolean down = state.getValue(HbmFluidNodeBlock.DOWN);
        FluidDuctBoxBlock.DuctBounds bounds = FluidDuctBoxBlock.boundsFor(metadata);
        int mask = (east ? 32 : 0)
                | (west ? 16 : 0)
                | (up ? 8 : 0)
                | (down ? 4 : 0)
                | (south ? 2 : 0)
                | (north ? 1 : 0);
        int count = (north ? 1 : 0) + (east ? 1 : 0) + (south ? 1 : 0) + (west ? 1 : 0)
                + (up ? 1 : 0) + (down ? 1 : 0);

        if (mask == 0) {
            renderBox(textures.junction(), context, bounds.junctionLower(), bounds.junctionLower(),
                    bounds.junctionLower(), bounds.junctionUpper(), bounds.junctionUpper(), bounds.junctionUpper());
        } else if ((mask & 0b001111) == 0) {
            renderStraightX(textures, context, 0.0D, bounds.lower(), bounds.lower(),
                    1.0D, bounds.upper(), bounds.upper());
        } else if ((mask & 0b111100) == 0) {
            renderStraightZ(textures, context, bounds.lower(), bounds.lower(), 0.0D,
                    bounds.upper(), bounds.upper(), 1.0D);
        } else if ((mask & 0b110011) == 0) {
            renderStraightY(textures, context, bounds.lower(), 0.0D, bounds.lower(),
                    bounds.upper(), 1.0D, bounds.upper());
        } else {
            boolean simpleCurve = count == 2;
            double coreMin = simpleCurve ? bounds.lower() : bounds.junctionLower();
            double coreMax = simpleCurve ? bounds.upper() : bounds.junctionUpper();
            renderConnectedBox(textures, context, simpleCurve, north, east, south, west, up, down,
                    coreMin, coreMin, coreMin, coreMax, coreMax, coreMax);
            renderArms(textures, context, bounds, coreMin, coreMax, simpleCurve, north, east, south, west, up, down);
        }
    }

    private static void renderArms(TextureSet textures, ObjRenderContext context, FluidDuctBoxBlock.DuctBounds bounds,
            double coreMin, double coreMax, boolean curve, boolean north, boolean east, boolean south, boolean west,
            boolean up, boolean down) {
        if (north) {
            renderConnectedBox(textures, context, curve, north, east, south, west, up, down,
                    bounds.lower(), bounds.lower(), 0.0D, bounds.upper(), bounds.upper(), coreMin);
        }
        if (east) {
            renderConnectedBox(textures, context, curve, north, east, south, west, up, down,
                    coreMax, bounds.lower(), bounds.lower(), 1.0D, bounds.upper(), bounds.upper());
        }
        if (south) {
            renderConnectedBox(textures, context, curve, north, east, south, west, up, down,
                    bounds.lower(), bounds.lower(), coreMax, bounds.upper(), bounds.upper(), 1.0D);
        }
        if (west) {
            renderConnectedBox(textures, context, curve, north, east, south, west, up, down,
                    0.0D, bounds.lower(), bounds.lower(), coreMin, bounds.upper(), bounds.upper());
        }
        if (up) {
            renderConnectedBox(textures, context, curve, north, east, south, west, up, down,
                    bounds.lower(), coreMax, bounds.lower(), bounds.upper(), 1.0D, bounds.upper());
        }
        if (down) {
            renderConnectedBox(textures, context, curve, north, east, south, west, up, down,
                    bounds.lower(), 0.0D, bounds.lower(), bounds.upper(), coreMin, bounds.upper());
        }
    }

    private static void renderStraightX(TextureSet textures, ObjRenderContext context, double minX, double minY,
            double minZ, double maxX, double maxY, double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(textures.straight(), textures.straight(), textures.straight(),
                textures.straight(), textures.end(), textures.end(), context, minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static void renderStraightY(TextureSet textures, ObjRenderContext context, double minX, double minY,
            double minZ, double maxX, double maxY, double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(textures.end(), textures.end(), textures.straight(),
                textures.straight(), textures.straight(), textures.straight(), context, minX, minY, minZ, maxX, maxY,
                maxZ);
    }

    private static void renderStraightZ(TextureSet textures, ObjRenderContext context, double minX, double minY,
            double minZ, double maxX, double maxY, double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(textures.straight(), textures.straight(), textures.end(),
                textures.end(), textures.straight(), textures.straight(), context, minX, minY, minZ, maxX, maxY,
                maxZ);
    }

    private static void renderConnectedBox(TextureSet textures, ObjRenderContext context, boolean curve,
            boolean north, boolean east, boolean south, boolean west, boolean up, boolean down, double minX,
            double minY, double minZ, double maxX, double maxY, double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(
                faceTexture(textures, Direction.UP, curve, north, east, south, west, up, down),
                faceTexture(textures, Direction.DOWN, curve, north, east, south, west, up, down),
                faceTexture(textures, Direction.NORTH, curve, north, east, south, west, up, down),
                faceTexture(textures, Direction.SOUTH, curve, north, east, south, west, up, down),
                faceTexture(textures, Direction.EAST, curve, north, east, south, west, up, down),
                faceTexture(textures, Direction.WEST, curve, north, east, south, west, up, down),
                context, minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static void renderBox(TextureAtlasSprite sprite, ObjRenderContext context, double minX, double minY,
            double minZ, double maxX, double maxY, double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(sprite, context, minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static TextureAtlasSprite faceTexture(TextureSet textures, Direction face, boolean curve, boolean north,
            boolean east, boolean south, boolean west, boolean up, boolean down) {
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

    private record TextureSet(TextureAtlasSprite straight, TextureAtlasSprite end, TextureAtlasSprite curveTL,
                              TextureAtlasSprite curveTR, TextureAtlasSprite curveBL, TextureAtlasSprite curveBR,
                              TextureAtlasSprite junction) {
        static TextureSet create(String prefix, int metadata) {
            int junction = FluidDuctBoxBlock.legacySizeStep(metadata);
            return new TextureSet(
                    sprite(prefix + "_straight"),
                    sprite(prefix + "_end"),
                    sprite(prefix + "_curve_tl"),
                    sprite(prefix + "_curve_tr"),
                    sprite(prefix + "_curve_bl"),
                    sprite(prefix + "_curve_br"),
                    sprite(prefix + "_junction_" + junction));
        }
    }

    private static TextureAtlasSprite sprite(String texture) {
        return LegacyTexturedQuadRenderer.blockSprite(new ResourceLocation(HbmNtm.MOD_ID, "block/" + texture));
    }
}
