package com.hbm.ntm.client.model;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.CableDiodeBlock;
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

public class CableDiodeBodyBakedModel implements BakedModel {
    private static final ResourceLocation MODEL_ID = new ResourceLocation(HbmNtm.MOD_ID, "block/cable_diode");
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final float SLAB_THICKNESS = 2.0F;
    private static final float PAD_MIN = 2.0F;
    private static final float PAD_MAX = 14.0F;

    private final TextureAtlasSprite plateSprite;
    private final TextureAtlasSprite padSprite;
    private final TextureAtlasSprite particleSprite;
    private final ItemTransforms transforms;
    private final Map<Direction, List<BakedQuad>> quadCache = new ConcurrentHashMap<>();

    public CableDiodeBodyBakedModel(TextureAtlasSprite plateSprite, TextureAtlasSprite padSprite,
            TextureAtlasSprite particleSprite, ItemTransforms transforms) {
        this.plateSprite = plateSprite;
        this.padSprite = padSprite;
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
        return quadCache.computeIfAbsent(outputDirection(state), this::buildQuads);
    }

    private List<BakedQuad> buildQuads(Direction output) {
        List<BakedQuad> quads = new ArrayList<>(12);
        Bounds slab = slabBounds(output);
        addCuboid(quads, plateSprite, slab.minX(), slab.minY(), slab.minZ(), slab.maxX(), slab.maxY(),
                slab.maxZ());
        addCuboid(quads, padSprite, PAD_MIN, PAD_MIN, PAD_MIN, PAD_MAX, PAD_MAX, PAD_MAX);
        return List.copyOf(quads);
    }

    private static Direction outputDirection(@Nullable BlockState state) {
        if (state != null && state.hasProperty(CableDiodeBlock.FACING)) {
            return state.getValue(CableDiodeBlock.FACING).getOpposite();
        }
        return Direction.UP;
    }

    private static Bounds slabBounds(Direction direction) {
        float minX = 0.0F;
        float minY = 0.0F;
        float minZ = 0.0F;
        float maxX = 16.0F;
        float maxY = 16.0F;
        float maxZ = 16.0F;

        switch (direction) {
            case DOWN -> maxY = SLAB_THICKNESS;
            case UP -> minY = 16.0F - SLAB_THICKNESS;
            case NORTH -> maxZ = SLAB_THICKNESS;
            case SOUTH -> minZ = 16.0F - SLAB_THICKNESS;
            case WEST -> maxX = SLAB_THICKNESS;
            case EAST -> minX = 16.0F - SLAB_THICKNESS;
        }
        return new Bounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private void addCuboid(List<BakedQuad> quads, TextureAtlasSprite sprite, float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ) {
        Vector3f from = new Vector3f(minX, minY, minZ);
        Vector3f to = new Vector3f(maxX, maxY, maxZ);
        quads.add(bakeFace(from, to, Direction.UP, sprite));
        quads.add(bakeFace(from, to, Direction.DOWN, sprite));
        quads.add(bakeFace(from, to, Direction.NORTH, sprite));
        quads.add(bakeFace(from, to, Direction.SOUTH, sprite));
        quads.add(bakeFace(from, to, Direction.EAST, sprite));
        quads.add(bakeFace(from, to, Direction.WEST, sprite));
    }

    private BakedQuad bakeFace(Vector3f from, Vector3f to, Direction direction, TextureAtlasSprite sprite) {
        BlockElementFace face = new BlockElementFace(null, -1, "#body", new BlockFaceUV(uv(direction, from, to), 0));
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

    private record Bounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    }
}
