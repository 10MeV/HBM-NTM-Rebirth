package com.hbm.ntm.client.model;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.FluidDuctPaintableBlock;
import com.hbm.ntm.blockentity.PaintableDuctBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
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

public class FluidDuctPaintableBakedModel implements BakedModel {
    public static final int FLUID_TINT_INDEX = 1;

    private static final ResourceLocation MODEL_ID = new ResourceLocation(HbmNtm.MOD_ID,
            "block/fluid_duct_paintable");
    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    private final TextureAtlasSprite baseSprite;
    private final TextureAtlasSprite overlaySprite;
    private final TextureAtlasSprite colorOverlaySprite;
    private final TextureAtlasSprite particleSprite;
    private final boolean exhaust;
    private final ItemTransforms transforms;
    private final Map<CubeKey, List<BakedQuad>> cubeCache = new ConcurrentHashMap<>();

    public FluidDuctPaintableBakedModel(TextureAtlasSprite baseSprite, TextureAtlasSprite overlaySprite,
            TextureAtlasSprite colorOverlaySprite, TextureAtlasSprite particleSprite, boolean exhaust,
            ItemTransforms transforms) {
        this.baseSprite = baseSprite;
        this.overlaySprite = overlaySprite;
        this.colorOverlaySprite = colorOverlaySprite;
        this.particleSprite = particleSprite;
        this.exhaust = exhaust;
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
        if (renderType != null && renderType != RenderType.cutout()) {
            return List.of();
        }

        BlockState painted = modelData.get(PaintableDuctBlockEntity.PAINTED_STATE_PROPERTY);
        if (state != null && painted != null) {
            List<BakedQuad> quads = new ArrayList<>();
            addPaintedQuads(quads, painted, side, random, renderType);
            if (overlayEnabled(state)) {
                quads.addAll(cubeQuads(overlaySprite, -1, side, false));
            }
            return quads;
        }

        boolean item = state == null;
        if (exhaust) {
            return cubeQuads(baseSprite, -1, side, item);
        }

        List<BakedQuad> quads = new ArrayList<>(item || side != null ? 12 : 0);
        quads.addAll(cubeQuads(baseSprite, -1, side, item));
        quads.addAll(cubeQuads(colorOverlaySprite, FLUID_TINT_INDEX, side, item));
        return quads;
    }

    private static boolean overlayEnabled(BlockState state) {
        return state.hasProperty(FluidDuctPaintableBlock.OVERLAY)
                && state.getValue(FluidDuctPaintableBlock.OVERLAY);
    }

    private static void addPaintedQuads(List<BakedQuad> quads, BlockState painted, @Nullable Direction side,
            RandomSource random, @Nullable RenderType renderType) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(painted);
        List<BakedQuad> paintedQuads = model.getQuads(painted, side, random, ModelData.EMPTY, renderType);
        if (paintedQuads.isEmpty() && renderType != null) {
            paintedQuads = model.getQuads(painted, side, random, ModelData.EMPTY, null);
        }
        quads.addAll(paintedQuads);
    }

    private List<BakedQuad> cubeQuads(TextureAtlasSprite sprite, int tintIndex, @Nullable Direction side,
            boolean item) {
        if (side == null && !item) {
            return List.of();
        }
        return cubeCache.computeIfAbsent(new CubeKey(sprite, tintIndex, side, item), this::buildCubeQuads);
    }

    private List<BakedQuad> buildCubeQuads(CubeKey key) {
        Vector3f from = new Vector3f(0.0F, 0.0F, 0.0F);
        Vector3f to = new Vector3f(16.0F, 16.0F, 16.0F);
        if (key.side() != null) {
            return List.of(bakeFace(from, to, key.side(), key.sprite(), key.tintIndex()));
        }
        List<BakedQuad> quads = new ArrayList<>(Direction.values().length);
        for (Direction direction : Direction.values()) {
            quads.add(bakeFace(from, to, direction, key.sprite(), key.tintIndex()));
        }
        return List.copyOf(quads);
    }

    private static BakedQuad bakeFace(Vector3f from, Vector3f to, Direction direction, TextureAtlasSprite sprite,
            int tintIndex) {
        BlockElementFace face = new BlockElementFace(null, tintIndex, "#paintable",
                new BlockFaceUV(new float[] {0.0F, 0.0F, 16.0F, 16.0F}, 0));
        return FACE_BAKERY.bakeQuad(from, to, face, sprite, direction, BlockModelRotation.X0_Y0, null, true,
                MODEL_ID);
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
        BlockState painted = data.get(PaintableDuctBlockEntity.PAINTED_STATE_PROPERTY);
        if (painted != null) {
            return Minecraft.getInstance().getBlockRenderer().getBlockModel(painted).getParticleIcon(ModelData.EMPTY);
        }
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

    private record CubeKey(TextureAtlasSprite sprite, int tintIndex, @Nullable Direction side, boolean item) {
    }
}
