package com.hbm.ntm.client.model;

import com.hbm.ntm.block.FluidPipeBlock;
import com.hbm.ntm.block.HbmFluidNodeBlock;
import com.hbm.ntm.fluid.HbmFluidDuctVariants;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidPipeBakedModel implements BakedModel {
    public static final int FLUID_TINT_INDEX = 1;
    private static final float LEGACY_WORLD_AMBIENT = 0.7F;
    private static final float LEGACY_WORLD_UP_WEIGHT = 0.3F;
    private static final float LEGACY_WORLD_X_PENALTY = 0.1F;
    private static final float LEGACY_WORLD_Z_BONUS = 0.1F;
    private static final float LEGACY_WORLD_MIN_LIGHT = 0.45F;

    private static final int ITEM_KEY = HbmFluidDuctVariants.STANDARD_STYLE_COUNT * 64;
    private static final String[] ITEM_PARTS = {"pX", "nX", "pZ", "nZ"};

    private final LayerModels[] styles;
    private final TextureAtlasSprite particleSprite;
    private final ItemTransforms transforms;
    private final Map<Integer, List<BakedQuad>> quadCache = new ConcurrentHashMap<>();

    public FluidPipeBakedModel(LayerModels[] styles, TextureAtlasSprite particleSprite, ItemTransforms transforms) {
        this.styles = styles.clone();
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
        if (state == null || !state.hasProperty(FluidPipeBlock.LEGACY_STYLE)) {
            return ITEM_KEY;
        }
        int style = FluidPipeBlock.clampLegacyStyle(state.getValue(FluidPipeBlock.LEGACY_STYLE));
        int mask = (state.getValue(HbmFluidNodeBlock.EAST) ? 32 : 0)
                | (state.getValue(HbmFluidNodeBlock.WEST) ? 16 : 0)
                | (state.getValue(HbmFluidNodeBlock.UP) ? 8 : 0)
                | (state.getValue(HbmFluidNodeBlock.DOWN) ? 4 : 0)
                | (state.getValue(HbmFluidNodeBlock.SOUTH) ? 2 : 0)
                | (state.getValue(HbmFluidNodeBlock.NORTH) ? 1 : 0);
        return style * 64 + mask;
    }

    private List<BakedQuad> buildQuads(int key) {
        int style = key == ITEM_KEY ? 0 : key / 64;
        String[] parts = key == ITEM_KEY ? ITEM_PARTS : partsForMask(key & 63);
        LayerModels layer = styles[FluidPipeBlock.clampLegacyStyle(style)];
        List<BakedQuad> quads = new ArrayList<>(parts.length * 64);
        boolean world = key != ITEM_KEY;
        for (String part : parts) {
            addPartQuads(quads, layer.base(part), false, world);
            addPartQuads(quads, layer.overlay(part), true, world);
        }
        return List.copyOf(quads);
    }

    private static void addPartQuads(List<BakedQuad> quads, @Nullable BakedModel model, boolean tint,
            boolean world) {
        if (model == null) {
            return;
        }
        RandomSource random = BakedModelQuadRandom.seeded();
        List<BakedQuad> partQuads = model.getQuads(null, null, random, ModelData.EMPTY, RenderType.cutout());
        if (partQuads.isEmpty()) {
            random = BakedModelQuadRandom.seeded();
            partQuads = model.getQuads(null, null, random, ModelData.EMPTY, null);
        }
        for (BakedQuad quad : partQuads) {
            int[] vertices = quad.getVertices().clone();
            if (world) {
                float light = legacyWorldFaceLight(vertices);
                for (int vertex = 0; vertex < 4; vertex++) {
                    int colorIndex = vertex * IQuadTransformer.STRIDE + IQuadTransformer.COLOR;
                    vertices[colorIndex] = multiplyAbgr(vertices[colorIndex], light);
                }
            }
            quads.add(new BakedQuad(vertices, tint ? FLUID_TINT_INDEX : quad.getTintIndex(),
                    quad.getDirection(), quad.getSprite(), false, false));
        }
    }

    /**
     * Matches 1.7.10 RenderTestPipe/ObjUtil world shading. That path used one
     * geometric face normal and baked its result into Tessellator color; it did
     * not use HFR's smooth corner normals or standard item-lighting formula.
     */
    private static float legacyWorldFaceLight(int[] vertices) {
        int stride = IQuadTransformer.STRIDE;
        int position = IQuadTransformer.POSITION;
        float ax = Float.intBitsToFloat(vertices[position]);
        float ay = Float.intBitsToFloat(vertices[position + 1]);
        float az = Float.intBitsToFloat(vertices[position + 2]);
        float bx = Float.intBitsToFloat(vertices[stride + position]);
        float by = Float.intBitsToFloat(vertices[stride + position + 1]);
        float bz = Float.intBitsToFloat(vertices[stride + position + 2]);
        float cx = Float.intBitsToFloat(vertices[2 * stride + position]);
        float cy = Float.intBitsToFloat(vertices[2 * stride + position + 1]);
        float cz = Float.intBitsToFloat(vertices[2 * stride + position + 2]);

        float abx = bx - ax;
        float aby = by - ay;
        float abz = bz - az;
        float acx = cx - ax;
        float acy = cy - ay;
        float acz = cz - az;
        float nx = aby * acz - abz * acy;
        float ny = abz * acx - abx * acz;
        float nz = abx * acy - aby * acx;
        float lengthSquared = nx * nx + ny * ny + nz * nz;
        if (lengthSquared > 1.0E-10F && Float.isFinite(lengthSquared)) {
            float inverseLength = (float) (1.0D / Math.sqrt(lengthSquared));
            nx *= inverseLength;
            ny *= inverseLength;
            nz *= inverseLength;
        } else {
            nx = 0.0F;
            ny = 0.0F;
            nz = 0.0F;
        }

        float light = ny * LEGACY_WORLD_UP_WEIGHT + LEGACY_WORLD_AMBIENT
                - Math.abs(nx) * LEGACY_WORLD_X_PENALTY
                + Math.abs(nz) * LEGACY_WORLD_Z_BONUS;
        return Math.max(LEGACY_WORLD_MIN_LIGHT, Math.min(1.0F, light));
    }

    private static int multiplyAbgr(int abgr, float light) {
        int red = Math.round((abgr & 0xFF) * light);
        int green = Math.round(((abgr >>> 8) & 0xFF) * light);
        int blue = Math.round(((abgr >>> 16) & 0xFF) * light);
        return (abgr & 0xFF000000)
                | (Math.max(0, Math.min(255, blue)) << 16)
                | (Math.max(0, Math.min(255, green)) << 8)
                | Math.max(0, Math.min(255, red));
    }

    private static String[] partsForMask(int mask) {
        boolean east = (mask & 32) != 0;
        boolean west = (mask & 16) != 0;
        boolean up = (mask & 8) != 0;
        boolean down = (mask & 4) != 0;
        boolean south = (mask & 2) != 0;
        boolean north = (mask & 1) != 0;

        if (mask == 0) {
            return new String[]{"pX", "nX", "pY", "nY", "pZ", "nZ"};
        }
        if ((east || west) && !up && !down && !south && !north) {
            return new String[]{"pX", "nX"};
        }
        if ((up || down) && !east && !west && !south && !north) {
            return new String[]{"pY", "nY"};
        }
        if ((south || north) && !east && !west && !up && !down) {
            return new String[]{"pZ", "nZ"};
        }

        List<String> parts = new ArrayList<>(14);
        if (east) parts.add("pX");
        if (west) parts.add("nX");
        if (up) parts.add("pY");
        if (down) parts.add("nY");
        if (south) parts.add("nZ");
        if (north) parts.add("pZ");

        if (!east && !up && !south) parts.add("ppn");
        if (!east && !up && !north) parts.add("ppp");
        if (!west && !up && !south) parts.add("npn");
        if (!west && !up && !north) parts.add("npp");
        if (!east && !down && !south) parts.add("pnn");
        if (!east && !down && !north) parts.add("pnp");
        if (!west && !down && !south) parts.add("nnn");
        if (!west && !down && !north) parts.add("nnp");
        return parts.toArray(String[]::new);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state) {
        return false;
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state, RenderType renderType) {
        return false;
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

    public record LayerModels(Map<String, BakedModel> base, Map<String, BakedModel> overlay) {
        @Nullable
        BakedModel base(String part) {
            return base.get(part);
        }

        @Nullable
        BakedModel overlay(String part) {
            return overlay.get(part);
        }
    }
}
