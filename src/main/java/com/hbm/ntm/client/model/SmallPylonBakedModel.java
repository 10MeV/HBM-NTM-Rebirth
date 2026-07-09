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

public class SmallPylonBakedModel implements BakedModel {
    private static final ResourceLocation MODEL_ID = new ResourceLocation(HbmNtm.MOD_ID, "block/red_pylon");
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final float TEXTURE_WIDTH = 64.0F;
    private static final float TEXTURE_HEIGHT = 128.0F;

    private final TextureAtlasSprite sprite;
    private final TextureAtlasSprite particleSprite;
    private final ItemTransforms transforms;
    private final List<BakedQuad> quads;

    public SmallPylonBakedModel(TextureAtlasSprite sprite, TextureAtlasSprite particleSprite,
            ItemTransforms transforms) {
        this.sprite = sprite;
        this.particleSprite = particleSprite;
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
        if (side != null || renderType != null && renderType != RenderType.cutout()) {
            return List.of();
        }
        return quads;
    }

    private List<BakedQuad> buildQuads() {
        List<BakedQuad> result = new ArrayList<>(24);
        addBox(result, 0, 96, 16, 16, 16, 0, 0, 0, 16, 16, 16);
        addBox(result, 1, 1, 4, 73, 4, 6, 16, 6, 10, 89, 10);
        addBox(result, 24, 1, 6, 4, 6, 5, 80, 5, 11, 84, 11);
        addBox(result, 25, 17, 6, 2, 6, 5, 86, 5, 11, 88, 11);
        return List.copyOf(result);
    }

    private void addBox(List<BakedQuad> result, float texU, float texV, float width, float height,
            float depth, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        Vector3f from = new Vector3f(minX, minY, minZ);
        Vector3f to = new Vector3f(maxX, maxY, maxZ);
        for (Direction direction : Direction.values()) {
            result.add(bakeFace(from, to, direction, uv(direction, texU, texV, width, height, depth)));
        }
    }

    private BakedQuad bakeFace(Vector3f from, Vector3f to, Direction direction, float[] uv) {
        BlockElementFace face = new BlockElementFace(null, -1, "#pylon", new BlockFaceUV(uv, 0));
        return FACE_BAKERY.bakeQuad(from, to, face, sprite, direction, BlockModelRotation.X0_Y0,
                null, true, MODEL_ID);
    }

    private static float[] uv(Direction direction, float u, float v, float width, float height, float depth) {
        return switch (direction) {
            case WEST -> scaled(u + depth + width, v + depth, u + depth + width + depth, v + depth + height);
            case EAST -> scaled(u, v + depth, u + depth, v + depth + height);
            case UP -> scaled(u + depth, v, u + depth + width, v + depth);
            case DOWN -> scaled(u + depth + width, v, u + depth + width + width, v + depth);
            case NORTH -> scaled(u + depth, v + depth, u + depth + width, v + depth + height);
            case SOUTH -> scaled(u + depth + width + depth, v + depth,
                    u + depth + width + depth + width, v + depth + height);
        };
    }

    private static float[] scaled(float minU, float minV, float maxU, float maxV) {
        return new float[] {
                minU * 16.0F / TEXTURE_WIDTH,
                minV * 16.0F / TEXTURE_HEIGHT,
                maxU * 16.0F / TEXTURE_WIDTH,
                maxV * 16.0F / TEXTURE_HEIGHT
        };
    }

    @Override
    public boolean useAmbientOcclusion() {
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
}
