package com.hbm.ntm.client.model;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
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

/** Recreates CraneRouter's legacy seven-pass colored face overlay. */
public class CraneRouterBakedModel implements BakedModel {
    public static final int DOWN_TINT = 0;
    public static final int UP_TINT = 1;
    public static final int NORTH_TINT = 2;
    public static final int SOUTH_TINT = 3;
    public static final int WEST_TINT = 4;
    public static final int EAST_TINT = 5;

    private static final ResourceLocation MODEL_ID = new ResourceLocation(HbmNtm.MOD_ID, "block/crane_router");
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final Vector3f BODY_FROM = new Vector3f(0.0F, 0.0F, 0.0F);
    private static final Vector3f BODY_TO = new Vector3f(16.0F, 16.0F, 16.0F);
    private static final float OVERLAY_OFFSET = 0.01F;

    private final TextureAtlasSprite baseSprite;
    private final TextureAtlasSprite overlaySprite;
    private final ItemTransforms transforms;
    private final List<BakedQuad> quads;

    public CraneRouterBakedModel(TextureAtlasSprite baseSprite, TextureAtlasSprite overlaySprite,
            ItemTransforms transforms) {
        this.baseSprite = baseSprite;
        this.overlaySprite = overlaySprite;
        this.transforms = transforms;
        this.quads = buildQuads();
    }

    @Override
    @Deprecated
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return getQuads(state, side, random, ModelData.EMPTY, null);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
            @NotNull RandomSource random, @NotNull ModelData modelData, @Nullable RenderType renderType) {
        return side == null && (renderType == null || renderType == RenderType.cutout()) ? quads : List.of();
    }

    private List<BakedQuad> buildQuads() {
        List<BakedQuad> result = new ArrayList<>(12);
        for (Direction face : Direction.values()) {
            result.add(bakeFace(BODY_FROM, BODY_TO, face, baseSprite, -1));
            Bounds overlay = overlayBounds(face);
            result.add(bakeFace(overlay.from(), overlay.to(), face, overlaySprite, tintIndex(face)));
        }
        return List.copyOf(result);
    }

    private static Bounds overlayBounds(Direction face) {
        return switch (face) {
            case DOWN -> new Bounds(new Vector3f(0.0F, -OVERLAY_OFFSET, 0.0F), new Vector3f(16.0F, 0.0F, 16.0F));
            case UP -> new Bounds(new Vector3f(0.0F, 16.0F, 0.0F), new Vector3f(16.0F, 16.0F + OVERLAY_OFFSET, 16.0F));
            case NORTH -> new Bounds(new Vector3f(0.0F, 0.0F, -OVERLAY_OFFSET), new Vector3f(16.0F, 16.0F, 0.0F));
            case SOUTH -> new Bounds(new Vector3f(0.0F, 0.0F, 16.0F), new Vector3f(16.0F, 16.0F, 16.0F + OVERLAY_OFFSET));
            case WEST -> new Bounds(new Vector3f(-OVERLAY_OFFSET, 0.0F, 0.0F), new Vector3f(0.0F, 16.0F, 16.0F));
            case EAST -> new Bounds(new Vector3f(16.0F, 0.0F, 0.0F), new Vector3f(16.0F + OVERLAY_OFFSET, 16.0F, 16.0F));
        };
    }

    private static int tintIndex(Direction face) {
        return switch (face) {
            case DOWN -> DOWN_TINT;
            case UP -> UP_TINT;
            case NORTH -> NORTH_TINT;
            case SOUTH -> SOUTH_TINT;
            case WEST -> WEST_TINT;
            case EAST -> EAST_TINT;
        };
    }

    private static BakedQuad bakeFace(Vector3f from, Vector3f to, Direction face, TextureAtlasSprite sprite,
            int tintIndex) {
        BlockElementFace elementFace = new BlockElementFace(null, tintIndex, "#router",
                new BlockFaceUV(new float[] {0.0F, 0.0F, 16.0F, 16.0F}, 0));
        return FACE_BAKERY.bakeQuad(from, to, elementFace, sprite, face, BlockModelRotation.X0_Y0, null, true,
                MODEL_ID);
    }

    @Override public boolean useAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean usesBlockLight() { return true; }
    @Override public boolean isCustomRenderer() { return false; }
    @Override @Deprecated public TextureAtlasSprite getParticleIcon() { return baseSprite; }
    @Override public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) { return baseSprite; }
    @Override public ItemTransforms getTransforms() { return transforms; }
    @Override public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack,
            boolean applyLeftHandTransform) {
        transforms.getTransform(transformType).apply(applyLeftHandTransform, poseStack);
        return this;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource random,
            @NotNull ModelData data) {
        return ChunkRenderTypeSet.of(RenderType.cutout());
    }

    private record Bounds(Vector3f from, Vector3f to) {
    }
}
