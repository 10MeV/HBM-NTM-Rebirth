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
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidPipeBakedModel implements BakedModel {
    public static final int FLUID_TINT_INDEX = 1;

    private static final long RANDOM_SEED = 42L;
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
        RandomSource random = RandomSource.create(RANDOM_SEED);
        for (String part : parts) {
            addPartQuads(quads, layer.base(part), random, false);
            addPartQuads(quads, layer.overlay(part), random, true);
        }
        return List.copyOf(quads);
    }

    private static void addPartQuads(List<BakedQuad> quads, @Nullable BakedModel model, RandomSource random,
            boolean tint) {
        if (model == null) {
            return;
        }
        random.setSeed(RANDOM_SEED);
        List<BakedQuad> partQuads = model.getQuads(null, null, random, ModelData.EMPTY, RenderType.cutout());
        if (partQuads.isEmpty()) {
            random.setSeed(RANDOM_SEED);
            partQuads = model.getQuads(null, null, random, ModelData.EMPTY, null);
        }
        if (!tint) {
            quads.addAll(partQuads);
            return;
        }
        for (BakedQuad quad : partQuads) {
            quads.add(new BakedQuad(quad.getVertices().clone(), FLUID_TINT_INDEX, quad.getDirection(),
                    quad.getSprite(), quad.isShade()));
        }
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
