package com.hbm.ntm.client.model;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.HbmEnergyNodeBlock;
import com.hbm.ntm.block.RedCableBoxBlock;
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

public class RedCableBoxBakedModel implements BakedModel {
    private static final ResourceLocation MODEL_ID = new ResourceLocation(HbmNtm.MOD_ID, "block/red_cable_box");
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final int ITEM_KEY = 5 * 64;

    private final TextureAtlasSprite straightSprite;
    private final TextureAtlasSprite[] endSprites;
    private final TextureAtlasSprite curveTlSprite;
    private final TextureAtlasSprite curveTrSprite;
    private final TextureAtlasSprite curveBlSprite;
    private final TextureAtlasSprite curveBrSprite;
    private final TextureAtlasSprite junctionSprite;
    private final TextureAtlasSprite particleSprite;
    private final ItemTransforms transforms;
    private final Map<Integer, List<BakedQuad>> quadCache = new ConcurrentHashMap<>();

    public RedCableBoxBakedModel(TextureAtlasSprite straightSprite, TextureAtlasSprite[] endSprites,
            TextureAtlasSprite curveTlSprite, TextureAtlasSprite curveTrSprite, TextureAtlasSprite curveBlSprite,
            TextureAtlasSprite curveBrSprite, TextureAtlasSprite junctionSprite, TextureAtlasSprite particleSprite,
            ItemTransforms transforms) {
        this.straightSprite = straightSprite;
        this.endSprites = endSprites.clone();
        this.curveTlSprite = curveTlSprite;
        this.curveTrSprite = curveTrSprite;
        this.curveBlSprite = curveBlSprite;
        this.curveBrSprite = curveBrSprite;
        this.junctionSprite = junctionSprite;
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
        if (state == null || !state.hasProperty(RedCableBoxBlock.SIZE)) {
            return ITEM_KEY;
        }
        int size = clampSize(state.getValue(RedCableBoxBlock.SIZE));
        int mask = (state.getValue(HbmEnergyNodeBlock.EAST) ? 32 : 0)
                | (state.getValue(HbmEnergyNodeBlock.WEST) ? 16 : 0)
                | (state.getValue(HbmEnergyNodeBlock.UP) ? 8 : 0)
                | (state.getValue(HbmEnergyNodeBlock.DOWN) ? 4 : 0)
                | (state.getValue(HbmEnergyNodeBlock.SOUTH) ? 2 : 0)
                | (state.getValue(HbmEnergyNodeBlock.NORTH) ? 1 : 0);
        return size * 64 + mask;
    }

    private List<BakedQuad> buildQuads(int key) {
        if (key == ITEM_KEY) {
            List<BakedQuad> itemQuads = new ArrayList<>(6);
            BoxBounds bounds = boundsForSize(0);
            addStraightZ(itemQuads, bounds.lower(), bounds.lower(), 0.0F, bounds.upper(), bounds.upper(), 16.0F, 0);
            return List.copyOf(itemQuads);
        }

        int size = key / 64;
        int mask = key & 63;
        boolean north = (mask & 1) != 0;
        boolean south = (mask & 2) != 0;
        boolean down = (mask & 4) != 0;
        boolean up = (mask & 8) != 0;
        boolean west = (mask & 16) != 0;
        boolean east = (mask & 32) != 0;
        int count = Integer.bitCount(mask);
        BoxBounds bounds = boundsForSize(size);
        List<BakedQuad> quads = new ArrayList<>(42);

        if (mask == 0) {
            addCuboid(quads, junctionSprite, junctionSprite, junctionSprite, junctionSprite, junctionSprite,
                    junctionSprite, bounds.lower(), bounds.lower(), bounds.lower(), bounds.upper(), bounds.upper(),
                    bounds.upper());
        } else if ((mask & 0b001111) == 0) {
            addStraightX(quads, 0.0F, bounds.lower(), bounds.lower(), 16.0F, bounds.upper(), bounds.upper(), size);
        } else if ((mask & 0b111100) == 0) {
            addStraightZ(quads, bounds.lower(), bounds.lower(), 0.0F, bounds.upper(), bounds.upper(), 16.0F, size);
        } else if ((mask & 0b110011) == 0) {
            addStraightY(quads, bounds.lower(), 0.0F, bounds.lower(), bounds.upper(), 16.0F, bounds.upper(), size);
        } else {
            boolean curve = count == 2;
            addConnected(quads, curve, north, east, south, west, up, down,
                    bounds.lower(), bounds.lower(), bounds.lower(), bounds.upper(), bounds.upper(), bounds.upper(),
                    size);
            addArms(quads, curve, north, east, south, west, up, down, bounds, size);
        }

        return List.copyOf(quads);
    }

    private void addArms(List<BakedQuad> quads, boolean curve, boolean north, boolean east, boolean south,
            boolean west, boolean up, boolean down, BoxBounds bounds, int size) {
        if (north) {
            addConnected(quads, curve, north, east, south, west, up, down,
                    bounds.lower(), bounds.lower(), 0.0F, bounds.upper(), bounds.upper(), bounds.lower(), size);
        }
        if (east) {
            addConnected(quads, curve, north, east, south, west, up, down,
                    bounds.upper(), bounds.lower(), bounds.lower(), 16.0F, bounds.upper(), bounds.upper(), size);
        }
        if (south) {
            addConnected(quads, curve, north, east, south, west, up, down,
                    bounds.lower(), bounds.lower(), bounds.upper(), bounds.upper(), bounds.upper(), 16.0F, size);
        }
        if (west) {
            addConnected(quads, curve, north, east, south, west, up, down,
                    0.0F, bounds.lower(), bounds.lower(), bounds.lower(), bounds.upper(), bounds.upper(), size);
        }
        if (up) {
            addConnected(quads, curve, north, east, south, west, up, down,
                    bounds.lower(), bounds.upper(), bounds.lower(), bounds.upper(), 16.0F, bounds.upper(), size);
        }
        if (down) {
            addConnected(quads, curve, north, east, south, west, up, down,
                    bounds.lower(), 0.0F, bounds.lower(), bounds.upper(), bounds.lower(), bounds.upper(), size);
        }
    }

    private void addStraightX(List<BakedQuad> quads, float minX, float minY, float minZ, float maxX, float maxY,
            float maxZ, int size) {
        addCuboid(quads, straightSprite, straightSprite, straightSprite, straightSprite, endSprite(size),
                endSprite(size), minX, minY, minZ, maxX, maxY, maxZ);
    }

    private void addStraightY(List<BakedQuad> quads, float minX, float minY, float minZ, float maxX, float maxY,
            float maxZ, int size) {
        addCuboid(quads, endSprite(size), endSprite(size), straightSprite, straightSprite, straightSprite,
                straightSprite, minX, minY, minZ, maxX, maxY, maxZ);
    }

    private void addStraightZ(List<BakedQuad> quads, float minX, float minY, float minZ, float maxX, float maxY,
            float maxZ, int size) {
        addCuboid(quads, straightSprite, straightSprite, endSprite(size), endSprite(size), straightSprite,
                straightSprite, minX, minY, minZ, maxX, maxY, maxZ);
    }

    private void addConnected(List<BakedQuad> quads, boolean curve, boolean north, boolean east, boolean south,
            boolean west, boolean up, boolean down, float minX, float minY, float minZ, float maxX, float maxY,
            float maxZ, int size) {
        addCuboid(quads,
                faceTexture(Direction.UP, curve, north, east, south, west, up, down, size),
                faceTexture(Direction.DOWN, curve, north, east, south, west, up, down, size),
                faceTexture(Direction.NORTH, curve, north, east, south, west, up, down, size),
                faceTexture(Direction.SOUTH, curve, north, east, south, west, up, down, size),
                faceTexture(Direction.EAST, curve, north, east, south, west, up, down, size),
                faceTexture(Direction.WEST, curve, north, east, south, west, up, down, size),
                minX, minY, minZ, maxX, maxY, maxZ);
    }

    private TextureAtlasSprite faceTexture(Direction face, boolean curve, boolean north, boolean east, boolean south,
            boolean west, boolean up, boolean down, int size) {
        if (!curve) {
            return junctionSprite;
        }
        if ((face == Direction.DOWN && down) || (face == Direction.UP && up)
                || (face == Direction.NORTH && north) || (face == Direction.SOUTH && south)
                || (face == Direction.WEST && west) || (face == Direction.EAST && east)) {
            return endSprite(size);
        }
        if ((face == Direction.UP && down) || (face == Direction.DOWN && up)
                || (face == Direction.SOUTH && north) || (face == Direction.NORTH && south)
                || (face == Direction.EAST && west) || (face == Direction.WEST && east)) {
            return straightSprite;
        }

        if (down && south) return face == Direction.WEST ? curveBrSprite : curveBlSprite;
        if (down && north) return face == Direction.EAST ? curveBrSprite : curveBlSprite;
        if (down && east) return face == Direction.SOUTH ? curveBrSprite : curveBlSprite;
        if (down && west) return face == Direction.NORTH ? curveBrSprite : curveBlSprite;
        if (up && south) return face == Direction.WEST ? curveTrSprite : curveTlSprite;
        if (up && north) return face == Direction.EAST ? curveTrSprite : curveTlSprite;
        if (up && east) return face == Direction.SOUTH ? curveTrSprite : curveTlSprite;
        if (up && west) return face == Direction.NORTH ? curveTrSprite : curveTlSprite;
        if (east && north) return curveTrSprite;
        if (east && south) return curveBrSprite;
        if (west && north) return curveTlSprite;
        if (west && south) return curveBlSprite;
        return junctionSprite;
    }

    private void addCuboid(List<BakedQuad> quads, TextureAtlasSprite top, TextureAtlasSprite bottom,
            TextureAtlasSprite north, TextureAtlasSprite south, TextureAtlasSprite east, TextureAtlasSprite west,
            float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        Vector3f from = new Vector3f(minX, minY, minZ);
        Vector3f to = new Vector3f(maxX, maxY, maxZ);
        quads.add(bakeFace(from, to, Direction.UP, top));
        quads.add(bakeFace(from, to, Direction.DOWN, bottom));
        quads.add(bakeFace(from, to, Direction.NORTH, north));
        quads.add(bakeFace(from, to, Direction.SOUTH, south));
        quads.add(bakeFace(from, to, Direction.EAST, east));
        quads.add(bakeFace(from, to, Direction.WEST, west));
    }

    private BakedQuad bakeFace(Vector3f from, Vector3f to, Direction direction, TextureAtlasSprite sprite) {
        BlockElementFace face = new BlockElementFace(null, -1, "#box", new BlockFaceUV(uv(direction, from, to), 0));
        return FACE_BAKERY.bakeQuad(from, to, face, sprite, direction, BlockModelRotation.X0_Y0, null, true,
                MODEL_ID);
    }

    private static float[] uv(Direction direction, Vector3f from, Vector3f to) {
        return switch (direction) {
            case UP, DOWN -> new float[] {from.x(), from.z(), to.x(), to.z()};
            case NORTH, SOUTH -> new float[] {from.x(), 16.0F - to.y(), to.x(), 16.0F - from.y()};
            case EAST, WEST -> new float[] {from.z(), 16.0F - to.y(), to.z(), 16.0F - from.y()};
        };
    }

    private TextureAtlasSprite endSprite(int size) {
        return endSprites[clampSize(size)];
    }

    private static BoxBounds boundsForSize(int size) {
        int clamped = clampSize(size);
        return new BoxBounds(2.0F + clamped, 14.0F - clamped);
    }

    private static int clampSize(int size) {
        return Math.max(0, Math.min(4, size));
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

    private record BoxBounds(float lower, float upper) {
    }
}
