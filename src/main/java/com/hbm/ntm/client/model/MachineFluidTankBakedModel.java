package com.hbm.ntm.client.model;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MachineFluidTankBakedModel implements BakedModel {
    public static final String NORMAL_FRAME = "normal.Frame";
    public static final String NORMAL_TANK = "normal.Tank";
    public static final String EXPLODED_FRAME = "exploded.Frame";
    public static final String EXPLODED_TANK_INNER = "exploded.TankInner";
    public static final String EXPLODED_TANK = "exploded.Tank";
    public static final int TANK_TINT_INDEX = 1;
    public static final ResourceLocation DEFAULT_TANK_SPRITE =
            new ResourceLocation(HbmNtm.MOD_ID, "block/tank/tank_none");

    private final Map<String, BakedModel> parts;
    private final ItemTransforms transforms;
    private final Map<TankModelCacheKey, List<BakedQuad>> quadCache = new ConcurrentHashMap<>();
    private TextureAtlasSprite particleIcon;

    public MachineFluidTankBakedModel(Map<String, BakedModel> parts, ItemTransforms transforms) {
        this.parts = Map.copyOf(parts);
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
        boolean exploded = modelData.has(com.hbm.ntm.blockentity.FluidTankBlockEntity.SMALL_TANK_EXPLODED_PROPERTY)
                && Boolean.TRUE.equals(modelData.get(
                com.hbm.ntm.blockentity.FluidTankBlockEntity.SMALL_TANK_EXPLODED_PROPERTY));
        ResourceLocation tankSprite =
                modelData.get(com.hbm.ntm.blockentity.FluidTankBlockEntity.SMALL_TANK_TEXTURE_PROPERTY);
        if (tankSprite == null) {
            tankSprite = DEFAULT_TANK_SPRITE;
        }

        TankModelCacheKey cacheKey = new TankModelCacheKey(exploded, tankSprite, state, side, renderType);
        return quadCache.computeIfAbsent(cacheKey, this::buildQuads);
    }

    private List<BakedQuad> buildQuads(TankModelCacheKey cacheKey) {
        List<BakedQuad> quads = new ArrayList<>();
        if (cacheKey.exploded()) {
            addPart(quads, EXPLODED_FRAME, cacheKey.state(), cacheKey.side(), cacheKey.renderType());
            addPart(quads, EXPLODED_TANK_INNER, cacheKey.state(), cacheKey.side(), cacheKey.renderType());
            addTankPart(quads, EXPLODED_TANK, cacheKey.tankSprite(), cacheKey.state(), cacheKey.side(),
                    cacheKey.renderType());
        } else {
            addPart(quads, NORMAL_FRAME, cacheKey.state(), cacheKey.side(), cacheKey.renderType());
            addTankPart(quads, NORMAL_TANK, cacheKey.tankSprite(), cacheKey.state(), cacheKey.side(),
                    cacheKey.renderType());
        }
        return List.copyOf(quads);
    }

    private void addPart(List<BakedQuad> quads, String key, @Nullable BlockState state, @Nullable Direction side,
            @Nullable RenderType renderType) {
        BakedModel part = parts.get(key);
        if (part != null) {
            quads.addAll(partQuads(part, state, side, renderType));
        }
    }

    private void addTankPart(List<BakedQuad> quads, String key, ResourceLocation tankSprite,
            @Nullable BlockState state, @Nullable Direction side, @Nullable RenderType renderType) {
        BakedModel part = parts.get(key);
        if (part == null) {
            return;
        }
        quads.addAll(retextureTankQuads(partQuads(part, state, side, renderType), tankSprite));
    }

    private static List<BakedQuad> partQuads(BakedModel part, @Nullable BlockState state,
            @Nullable Direction side, @Nullable RenderType renderType) {
        RandomSource random = BakedModelQuadRandom.seeded();
        if (state == null && part instanceof LegacyLitObjBakedModel legacyLitPart) {
            return legacyLitPart.getItemQuads(null, side, random, ModelData.EMPTY, renderType);
        }
        return part.getQuads(state, side, random, ModelData.EMPTY, renderType);
    }

    private static List<BakedQuad> retextureTankQuads(List<BakedQuad> originalQuads, ResourceLocation tankSprite) {
        if (originalQuads.isEmpty()) {
            return List.of();
        }
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(tankSprite);
        List<BakedQuad> quads = new ArrayList<>(originalQuads.size());
        for (BakedQuad quad : originalQuads) {
            quads.add(retextureTankQuad(quad, sprite));
        }
        return List.copyOf(quads);
    }

    private static BakedQuad retextureTankQuad(BakedQuad original, TextureAtlasSprite newSprite) {
        TextureAtlasSprite oldSprite = original.getSprite();
        if (oldSprite == null) {
            return new BakedQuad(original.getVertices().clone(), TANK_TINT_INDEX,
                    original.getDirection(), newSprite, false, false);
        }
        float oldUSize = oldSprite.getU1() - oldSprite.getU0();
        float oldVSize = oldSprite.getV1() - oldSprite.getV0();
        float newUSize = newSprite.getU1() - newSprite.getU0();
        float newVSize = newSprite.getV1() - newSprite.getV0();
        int[] data = original.getVertices().clone();
        if (oldUSize != 0.0F && oldVSize != 0.0F) {
            int stride = data.length / 4;
            for (int vertex = 0; vertex < 4; vertex++) {
                int offset = vertex * stride;
                float oldU = Float.intBitsToFloat(data[offset + 4]);
                float oldV = Float.intBitsToFloat(data[offset + 5]);
                float normalizedU = (oldU - oldSprite.getU0()) / oldUSize;
                float normalizedV = (oldV - oldSprite.getV0()) / oldVSize;
                data[offset + 4] = Float.floatToRawIntBits(newSprite.getU0() + normalizedU * newUSize);
                data[offset + 5] = Float.floatToRawIntBits(newSprite.getV0() + normalizedV * newVSize);
            }
        }
        return new BakedQuad(data, TANK_TINT_INDEX, original.getDirection(), newSprite, false, false);
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
        return getParticleIcon(ModelData.EMPTY);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        if (particleIcon == null) {
            BakedModel frame = parts.get(NORMAL_FRAME);
            particleIcon = frame == null
                    ? Minecraft.getInstance().getModelManager().getMissingModel().getParticleIcon(data)
                    : frame.getParticleIcon(data);
        }
        return particleIcon;
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

    private record TankModelCacheKey(boolean exploded, ResourceLocation tankSprite, @Nullable BlockState state,
            @Nullable Direction side, @Nullable RenderType renderType) {
    }
}
