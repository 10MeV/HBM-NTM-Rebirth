package com.hbm.ntm.client.model;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.FoundrySlagBlockEntity;
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

public class FoundrySlagBakedModel implements BakedModel {
    public static final int SLAG_TINT_INDEX = 0;
    private static final ResourceLocation MODEL_ID = new ResourceLocation(HbmNtm.MOD_ID, "block/slag");
    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    private final TextureAtlasSprite sprite;
    private final ItemTransforms transforms;
    private final Map<Integer, List<BakedQuad>> quadCache = new ConcurrentHashMap<>();

    public FoundrySlagBakedModel(TextureAtlasSprite sprite, ItemTransforms transforms) {
        this.sprite = sprite;
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
        Integer amount = modelData.get(FoundrySlagBlockEntity.SLAG_AMOUNT_PROPERTY);
        if (amount == null || amount <= 0) {
            return List.of();
        }
        int clamped = Math.min(FoundrySlagBlockEntity.MAX_AMOUNT, amount);
        return quadCache.computeIfAbsent(clamped, this::buildQuads);
    }

    private List<BakedQuad> buildQuads(int amount) {
        float height = FoundrySlagBlockEntity.fillLevelForAmount(amount) * 16.0F;
        Vector3f from = new Vector3f(0.0F, 0.0F, 0.0F);
        Vector3f to = new Vector3f(16.0F, height, 16.0F);
        List<BakedQuad> quads = new ArrayList<>(6);
        for (Direction direction : Direction.values()) {
            quads.add(bakeFace(from, to, direction, uv(direction, height)));
        }
        return List.copyOf(quads);
    }

    private BakedQuad bakeFace(Vector3f from, Vector3f to, Direction direction, float[] uv) {
        BlockElementFace face = new BlockElementFace(null, SLAG_TINT_INDEX, "#slag", new BlockFaceUV(uv, 0));
        return FACE_BAKERY.bakeQuad(from, to, face, sprite, direction, BlockModelRotation.X0_Y0, null, true, MODEL_ID);
    }

    private static float[] uv(Direction direction, float height) {
        if (direction.getAxis().isVertical()) {
            return new float[] {0.0F, 0.0F, 16.0F, 16.0F};
        }
        return new float[] {0.0F, 16.0F - height, 16.0F, 16.0F};
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
        return sprite;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return sprite;
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
