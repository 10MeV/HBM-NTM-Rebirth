package com.hbm.ntm.client.model;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.FluidDuctBoxBlock;
import com.hbm.ntm.block.HbmFluidNodeBlock;
import com.hbm.ntm.fluid.HbmFluidDuctVariants;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class FluidDuctBoxBakedModel implements BakedModel {
    public static final int FLUID_TINT_INDEX = 1;

    private static final ResourceLocation MODEL_ID = new ResourceLocation(HbmNtm.MOD_ID, "block/fluid_duct_box");
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final int ITEM_KEY = HbmFluidDuctVariants.BOX_METADATA_COUNT * 64;

    private final TextureSet[] materialTextures;
    private final TextureAtlasSprite particleSprite;
    private final ItemTransforms transforms;
    private final Map<Integer, List<BakedQuad>> quadCache = new ConcurrentHashMap<>();

    public FluidDuctBoxBakedModel(TextureSet[] materialTextures, TextureAtlasSprite particleSprite,
            ItemTransforms transforms) {
        this.materialTextures = materialTextures.clone();
        this.particleSprite = particleSprite;
        this.transforms = transforms;
    }

    @Override
    @Deprecated
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return getQuads(state, side, random, ModelData.EMPTY, null);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
            @NotNull RandomSource random, @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (side != null || renderType != null && renderType != RenderType.cutout()) {
            return List.of();
        }
        return quadCache.computeIfAbsent(cacheKey(state), this::buildQuads);
    }

    private static int cacheKey(@Nullable BlockState state) {
        if (state == null || !state.hasProperty(FluidDuctBoxBlock.LEGACY_METADATA)) {
            return ITEM_KEY;
        }
        int metadata = FluidDuctBoxBlock.clampLegacyMetadata(state.getValue(FluidDuctBoxBlock.LEGACY_METADATA));
        int mask = (state.getValue(HbmFluidNodeBlock.EAST) ? 32 : 0)
                | (state.getValue(HbmFluidNodeBlock.WEST) ? 16 : 0)
                | (state.getValue(HbmFluidNodeBlock.UP) ? 8 : 0)
                | (state.getValue(HbmFluidNodeBlock.DOWN) ? 4 : 0)
                | (state.getValue(HbmFluidNodeBlock.SOUTH) ? 2 : 0)
                | (state.getValue(HbmFluidNodeBlock.NORTH) ? 1 : 0);
        return metadata * 64 + mask;
    }

    private List<BakedQuad> buildQuads(int key) {
        if (key == ITEM_KEY) {
            List<BakedQuad> itemQuads = new ArrayList<>(6);
            addStraightZ(itemQuads, 0, 2.0F, 2.0F, 0.0F, 14.0F, 14.0F, 16.0F);
            return List.copyOf(itemQuads);
        }

        int metadata = key / 64;
        int mask = key & 63;
        boolean north = (mask & 1) != 0;
        boolean south = (mask & 2) != 0;
        boolean down = (mask & 4) != 0;
        boolean up = (mask & 8) != 0;
        boolean west = (mask & 16) != 0;
        boolean east = (mask & 32) != 0;
        int count = Integer.bitCount(mask);
        BoxBounds bounds = boundsForMetadata(metadata);
        List<BakedQuad> quads = new ArrayList<>(42);

        if (mask == 0) {
            TextureSet textures = textures(metadata);
            TextureAtlasSprite junction = textures.junctionSprite(metadata);
            addCuboid(quads, metadata, junction, junction, junction, junction, junction, junction,
                    bounds.junctionLower(), bounds.junctionLower(), bounds.junctionLower(),
                    bounds.junctionUpper(), bounds.junctionUpper(), bounds.junctionUpper());
        } else if ((mask & 0b001111) == 0) {
            addStraightX(quads, metadata, 0.0F, bounds.lower(), bounds.lower(), 16.0F, bounds.upper(),
                    bounds.upper());
        } else if ((mask & 0b111100) == 0) {
            addStraightZ(quads, metadata, bounds.lower(), bounds.lower(), 0.0F, bounds.upper(), bounds.upper(),
                    16.0F);
        } else if ((mask & 0b110011) == 0) {
            addStraightY(quads, metadata, bounds.lower(), 0.0F, bounds.lower(), bounds.upper(), 16.0F,
                    bounds.upper());
        } else {
            boolean curve = count == 2;
            float coreMin = curve ? bounds.lower() : bounds.junctionLower();
            float coreMax = curve ? bounds.upper() : bounds.junctionUpper();
            addConnected(quads, metadata, curve, north, east, south, west, up, down,
                    coreMin, coreMin, coreMin, coreMax, coreMax, coreMax);
            addArms(quads, metadata, curve, north, east, south, west, up, down, bounds);
        }

        return List.copyOf(quads);
    }

    private void addArms(List<BakedQuad> quads, int metadata, boolean curve, boolean north, boolean east,
            boolean south, boolean west, boolean up, boolean down, BoxBounds bounds) {
        float armLower = curve ? bounds.lower() : bounds.junctionLower();
        float armUpper = curve ? bounds.upper() : bounds.junctionUpper();
        if (north) {
            addConnected(quads, metadata, curve, north, east, south, west, up, down,
                    bounds.lower(), bounds.lower(), 0.0F, bounds.upper(), bounds.upper(), armLower);
        }
        if (east) {
            addConnected(quads, metadata, curve, north, east, south, west, up, down,
                    armUpper, bounds.lower(), bounds.lower(), 16.0F, bounds.upper(), bounds.upper());
        }
        if (south) {
            addConnected(quads, metadata, curve, north, east, south, west, up, down,
                    bounds.lower(), bounds.lower(), armUpper, bounds.upper(), bounds.upper(), 16.0F);
        }
        if (west) {
            addConnected(quads, metadata, curve, north, east, south, west, up, down,
                    0.0F, bounds.lower(), bounds.lower(), armLower, bounds.upper(), bounds.upper());
        }
        if (up) {
            addConnected(quads, metadata, curve, north, east, south, west, up, down,
                    bounds.lower(), armUpper, bounds.lower(), bounds.upper(), 16.0F, bounds.upper());
        }
        if (down) {
            addConnected(quads, metadata, curve, north, east, south, west, up, down,
                    bounds.lower(), 0.0F, bounds.lower(), bounds.upper(), armLower, bounds.upper());
        }
    }

    private void addStraightX(List<BakedQuad> quads, int metadata, float minX, float minY, float minZ, float maxX,
            float maxY, float maxZ) {
        TextureSet textures = textures(metadata);
        addCuboid(quads, metadata, textures.straight(), textures.straight(), textures.straight(),
                textures.straight(), textures.end(), textures.end(), minX, minY, minZ, maxX, maxY, maxZ);
    }

    private void addStraightY(List<BakedQuad> quads, int metadata, float minX, float minY, float minZ, float maxX,
            float maxY, float maxZ) {
        TextureSet textures = textures(metadata);
        addCuboid(quads, metadata, textures.end(), textures.end(), textures.straight(), textures.straight(),
                textures.straight(), textures.straight(), minX, minY, minZ, maxX, maxY, maxZ);
    }

    private void addStraightZ(List<BakedQuad> quads, int metadata, float minX, float minY, float minZ, float maxX,
            float maxY, float maxZ) {
        TextureSet textures = textures(metadata);
        addCuboid(quads, metadata, textures.straight(), textures.straight(), textures.end(), textures.end(),
                textures.straight(), textures.straight(), minX, minY, minZ, maxX, maxY, maxZ);
    }

    private void addConnected(List<BakedQuad> quads, int metadata, boolean curve, boolean north, boolean east,
            boolean south, boolean west, boolean up, boolean down, float minX, float minY, float minZ, float maxX,
            float maxY, float maxZ) {
        addCuboid(quads, metadata,
                faceTexture(Direction.UP, metadata, curve, north, east, south, west, up, down),
                faceTexture(Direction.DOWN, metadata, curve, north, east, south, west, up, down),
                faceTexture(Direction.NORTH, metadata, curve, north, east, south, west, up, down),
                faceTexture(Direction.SOUTH, metadata, curve, north, east, south, west, up, down),
                faceTexture(Direction.EAST, metadata, curve, north, east, south, west, up, down),
                faceTexture(Direction.WEST, metadata, curve, north, east, south, west, up, down),
                minX, minY, minZ, maxX, maxY, maxZ);
    }

    private TextureAtlasSprite faceTexture(Direction face, int metadata, boolean curve, boolean north, boolean east,
            boolean south, boolean west, boolean up, boolean down) {
        TextureSet textures = textures(metadata);
        if (!curve) {
            return textures.junctionSprite(metadata);
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

        if (down && south) return face == Direction.WEST ? textures.curveBr() : textures.curveBl();
        if (down && north) return face == Direction.EAST ? textures.curveBr() : textures.curveBl();
        if (down && east) return face == Direction.SOUTH ? textures.curveBr() : textures.curveBl();
        if (down && west) return face == Direction.NORTH ? textures.curveBr() : textures.curveBl();
        if (up && south) return face == Direction.WEST ? textures.curveTr() : textures.curveTl();
        if (up && north) return face == Direction.EAST ? textures.curveTr() : textures.curveTl();
        if (up && east) return face == Direction.SOUTH ? textures.curveTr() : textures.curveTl();
        if (up && west) return face == Direction.NORTH ? textures.curveTr() : textures.curveTl();
        if (east && north) return textures.curveTr();
        if (east && south) return textures.curveBr();
        if (west && north) return textures.curveTl();
        if (west && south) return textures.curveBl();
        return textures.junctionSprite(metadata);
    }

    private void addCuboid(List<BakedQuad> quads, int metadata, TextureAtlasSprite top, TextureAtlasSprite bottom,
            TextureAtlasSprite north, TextureAtlasSprite south, TextureAtlasSprite east, TextureAtlasSprite west,
            float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        Vector3f from = new Vector3f(minX, minY, minZ);
        Vector3f to = new Vector3f(maxX, maxY, maxZ);
        int tintIndex = FluidDuctBoxBlock.rectifyLegacyMaterial(metadata) == 2 ? FLUID_TINT_INDEX : -1;
        quads.add(bakeFace(from, to, Direction.UP, top, tintIndex));
        quads.add(bakeFace(from, to, Direction.DOWN, bottom, tintIndex));
        quads.add(bakeFace(from, to, Direction.NORTH, north, tintIndex));
        quads.add(bakeFace(from, to, Direction.SOUTH, south, tintIndex));
        quads.add(bakeFace(from, to, Direction.EAST, east, tintIndex));
        quads.add(bakeFace(from, to, Direction.WEST, west, tintIndex));
    }

    private BakedQuad bakeFace(Vector3f from, Vector3f to, Direction direction, TextureAtlasSprite sprite,
            int tintIndex) {
        BlockElementFace face = new BlockElementFace(null, tintIndex, "#box",
                new BlockFaceUV(uv(direction, from, to), 0));
        return FACE_BAKERY.bakeQuad(from, to, face, sprite, direction, BlockModelRotation.X0_Y0, null, true,
                MODEL_ID);
    }

    private TextureSet textures(int metadata) {
        return materialTextures[FluidDuctBoxBlock.rectifyLegacyMaterial(metadata)];
    }

    private static float[] uv(Direction direction, Vector3f from, Vector3f to) {
        return switch (direction) {
            case UP, DOWN -> new float[] {from.x(), from.z(), to.x(), to.z()};
            case NORTH, SOUTH -> new float[] {from.x(), 16.0F - to.y(), to.x(), 16.0F - from.y()};
            case EAST, WEST -> new float[] {from.z(), 16.0F - to.y(), to.z(), 16.0F - from.y()};
        };
    }

    private static BoxBounds boundsForMetadata(int metadata) {
        int step = FluidDuctBoxBlock.legacySizeStep(metadata);
        return new BoxBounds(2.0F + step, 14.0F - step, 1.0F + step, 15.0F - step);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    @Deprecated
    public TextureAtlasSprite getParticleIcon() {
        return particleSprite;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return particleSprite;
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack,
            boolean applyLeftHandTransform) {
        getTransforms().getTransform(transformType).apply(applyLeftHandTransform, poseStack);
        return this;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource random,
            @NotNull ModelData data) {
        return ChunkRenderTypeSet.of(RenderType.cutout());
    }

    public record TextureSet(TextureAtlasSprite straight, TextureAtlasSprite end, TextureAtlasSprite curveTl,
                             TextureAtlasSprite curveTr, TextureAtlasSprite curveBl, TextureAtlasSprite curveBr,
                             TextureAtlasSprite[] junctions) {
        TextureAtlasSprite junctionSprite(int metadata) {
            return junctions[FluidDuctBoxBlock.legacySizeStep(metadata)];
        }
    }

    private record BoxBounds(float lower, float upper, float junctionLower, float junctionUpper) {
    }
}
