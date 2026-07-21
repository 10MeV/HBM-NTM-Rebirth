package com.hbm.ntm.client.model;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.PneumaticTubePaintableBlock;
import com.hbm.ntm.blockentity.PaintableDuctBlockEntity;
import com.hbm.ntm.blockentity.PneumaticTubeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.EnumMap;
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

/** Rebuilds the six-part geometry and endpoint collars from RenderPneumoTube. */
public class PneumaticTubeBakedModel implements BakedModel {
    private static final ResourceLocation MODEL_ID = new ResourceLocation(HbmNtm.MOD_ID, "block/pneumatic_tube");
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final int FULL_MASK = 63;

    private final TextureAtlasSprite base;
    private final TextureAtlasSprite straight;
    private final TextureAtlasSprite input;
    private final TextureAtlasSprite output;
    private final TextureAtlasSprite connector;
    private final TextureAtlasSprite overlay;
    private final TextureAtlasSprite overlayInput;
    private final TextureAtlasSprite overlayOutput;
    private final boolean paintable;
    private final ItemTransforms transforms;
    private final Map<RenderKey, List<BakedQuad>> quadCache = new ConcurrentHashMap<>();

    public PneumaticTubeBakedModel(TextureAtlasSprite base, TextureAtlasSprite straight, TextureAtlasSprite input,
            TextureAtlasSprite output, TextureAtlasSprite connector, TextureAtlasSprite overlay,
            TextureAtlasSprite overlayInput, TextureAtlasSprite overlayOutput, boolean paintable,
            ItemTransforms transforms) {
        this.base = base;
        this.straight = straight;
        this.input = input;
        this.output = output;
        this.connector = connector;
        this.overlay = overlay;
        this.overlayInput = overlayInput;
        this.overlayOutput = overlayOutput;
        this.paintable = paintable;
        this.transforms = transforms;
    }

    @Override
    @Deprecated
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return getQuads(state, side, random, ModelData.EMPTY, null);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
            @NotNull RandomSource random, @NotNull ModelData data, @Nullable RenderType renderType) {
        if (side != null || renderType != null && renderType != RenderType.cutout()) {
            return List.of();
        }
        if (state == null) {
            return quadCache.computeIfAbsent(RenderKey.item(paintable), this::buildCachedQuads);
        }

        PneumaticTubeBlockEntity.PneumaticTubeRenderData tubeData =
                data.get(PneumaticTubeBlockEntity.RENDER_DATA_PROPERTY);
        if (tubeData == null) {
            tubeData = new PneumaticTubeBlockEntity.PneumaticTubeRenderData(0, 0, null, null);
        }

        BlockState painted = paintable ? data.get(PaintableDuctBlockEntity.PAINTED_STATE_PROPERTY) : null;
        boolean overlayEnabled = paintable && state.hasProperty(PneumaticTubePaintableBlock.OVERLAY)
                && state.getValue(PneumaticTubePaintableBlock.OVERLAY);
        RenderKey key = new RenderKey(tubeData.pneumaticMask(), tubeData.airMask(), tubeData.insertion(),
                tubeData.ejection(), false, overlayEnabled);
        if (painted == null) {
            return quadCache.computeIfAbsent(key, this::buildCachedQuads);
        }

        List<BakedQuad> quads = new ArrayList<>();
        addTubeGeometry(quads, tubeData, paintedSprites(painted, random), false);
        if (overlayEnabled) {
            addTubeGeometry(quads, tubeData, overlaySprites(tubeData), true);
        }
        return List.copyOf(quads);
    }

    private List<BakedQuad> buildCachedQuads(RenderKey key) {
        if (key.item() && !paintable) {
            List<BakedQuad> quads = new ArrayList<>();
            addCuboid(quads, uniformSprites(straight), 5.0F, 5.0F, 0.0F, 11.0F, 11.0F, 16.0F, FULL_MASK);
            return List.copyOf(quads);
        }
        PneumaticTubeBlockEntity.PneumaticTubeRenderData data = key.item()
                ? new PneumaticTubeBlockEntity.PneumaticTubeRenderData(0, 0, null, null)
                : new PneumaticTubeBlockEntity.PneumaticTubeRenderData(key.pneumaticMask(), key.airMask(),
                        key.insertion(), key.ejection());
        List<BakedQuad> quads = new ArrayList<>();
        addTubeGeometry(quads, data, uniformSprites(key.item() ? straight : base), false);
        if (paintable && key.overlayEnabled()) {
            addTubeGeometry(quads, data, overlaySprites(data), true);
        }
        return List.copyOf(quads);
    }

    private void addTubeGeometry(List<BakedQuad> quads, PneumaticTubeBlockEntity.PneumaticTubeRenderData data,
            FaceSprites body, boolean overlayPass) {
        int pneumaticMask = data.pneumaticMask();
        if (!paintable && !overlayPass && !data.hasEndpoint() && isStraight(pneumaticMask)) {
            addStraight(quads, pneumaticMask);
            return;
        }

        addCuboid(quads, body, 5.0F, 5.0F, 5.0F, 11.0F, 11.0F, 11.0F, FULL_MASK & ~pneumaticMask);
        for (Direction direction : Direction.values()) {
            if ((pneumaticMask & mask(direction)) != 0) {
                addDirectArm(quads, body, direction, FULL_MASK & ~axisMask(direction));
            }
        }
        addEndpoint(quads, data.insertion(), body, overlayPass ? body : endpointSprites(input), overlayPass);
        addEndpoint(quads, data.ejection(), body, overlayPass ? body : endpointSprites(output), overlayPass);
        for (Direction direction : Direction.values()) {
            if ((data.airMask() & mask(direction)) != 0) {
                addEndpoint(quads, direction, body, overlayPass ? body : endpointSprites(connector), overlayPass);
            }
        }
    }

    private void addStraight(List<BakedQuad> quads, int mask) {
        if ((mask & 0b001111) == 0) {
            addCuboid(quads, uniformSprites(straight), 0.0F, 5.0F, 5.0F, 16.0F, 11.0F, 11.0F,
                    FULL_MASK & ~axisMask(Direction.EAST));
        } else if ((mask & 0b111100) == 0) {
            addCuboid(quads, uniformSprites(straight), 5.0F, 5.0F, 0.0F, 11.0F, 11.0F, 16.0F,
                    FULL_MASK & ~axisMask(Direction.SOUTH));
        } else {
            addCuboid(quads, uniformSprites(straight), 5.0F, 0.0F, 5.0F, 11.0F, 16.0F, 11.0F,
                    FULL_MASK & ~axisMask(Direction.UP));
        }
    }

    private void addEndpoint(List<BakedQuad> quads, @Nullable Direction direction, FaceSprites body,
            FaceSprites outer, boolean overlayPass) {
        if (direction == null) {
            return;
        }
        addInnerConnector(quads, body, direction, FULL_MASK & ~axisMask(direction));
        addOuterConnector(quads, outer, direction, overlayPass ? FULL_MASK & ~axisMask(direction) : FULL_MASK);
    }

    private static void addDirectArm(List<BakedQuad> quads, FaceSprites sprites, Direction direction,
            int visibleFaces) {
        switch (direction) {
            case NORTH -> addCuboid(quads, sprites, 5.0F, 5.0F, 0.0F, 11.0F, 11.0F, 5.0F, visibleFaces);
            case EAST -> addCuboid(quads, sprites, 11.0F, 5.0F, 5.0F, 16.0F, 11.0F, 11.0F, visibleFaces);
            case SOUTH -> addCuboid(quads, sprites, 5.0F, 5.0F, 11.0F, 11.0F, 11.0F, 16.0F, visibleFaces);
            case WEST -> addCuboid(quads, sprites, 0.0F, 5.0F, 5.0F, 5.0F, 11.0F, 11.0F, visibleFaces);
            case UP -> addCuboid(quads, sprites, 5.0F, 11.0F, 5.0F, 11.0F, 16.0F, 11.0F, visibleFaces);
            case DOWN -> addCuboid(quads, sprites, 5.0F, 0.0F, 5.0F, 11.0F, 5.0F, 11.0F, visibleFaces);
        }
    }

    private static void addInnerConnector(List<BakedQuad> quads, FaceSprites sprites, Direction direction,
            int visibleFaces) {
        switch (direction) {
            case NORTH -> addCuboid(quads, sprites, 5.0F, 5.0F, 4.0F, 11.0F, 11.0F, 5.0F, visibleFaces);
            case EAST -> addCuboid(quads, sprites, 11.0F, 5.0F, 5.0F, 12.0F, 11.0F, 11.0F, visibleFaces);
            case SOUTH -> addCuboid(quads, sprites, 5.0F, 5.0F, 11.0F, 11.0F, 11.0F, 12.0F, visibleFaces);
            case WEST -> addCuboid(quads, sprites, 4.0F, 5.0F, 5.0F, 5.0F, 11.0F, 11.0F, visibleFaces);
            case UP -> addCuboid(quads, sprites, 5.0F, 11.0F, 5.0F, 11.0F, 12.0F, 11.0F, visibleFaces);
            case DOWN -> addCuboid(quads, sprites, 5.0F, 4.0F, 5.0F, 11.0F, 5.0F, 11.0F, visibleFaces);
        }
    }

    private static void addOuterConnector(List<BakedQuad> quads, FaceSprites sprites, Direction direction,
            int visibleFaces) {
        switch (direction) {
            case NORTH -> addCuboid(quads, sprites, 4.0F, 4.0F, 0.0F, 12.0F, 12.0F, 4.0F, visibleFaces);
            case EAST -> addCuboid(quads, sprites, 12.0F, 4.0F, 4.0F, 16.0F, 12.0F, 12.0F, visibleFaces);
            case SOUTH -> addCuboid(quads, sprites, 4.0F, 4.0F, 12.0F, 12.0F, 12.0F, 16.0F, visibleFaces);
            case WEST -> addCuboid(quads, sprites, 0.0F, 4.0F, 4.0F, 4.0F, 12.0F, 12.0F, visibleFaces);
            case UP -> addCuboid(quads, sprites, 4.0F, 12.0F, 4.0F, 12.0F, 16.0F, 12.0F, visibleFaces);
            case DOWN -> addCuboid(quads, sprites, 4.0F, 0.0F, 4.0F, 12.0F, 4.0F, 12.0F, visibleFaces);
        }
    }

    private static void addCuboid(List<BakedQuad> quads, FaceSprites sprites, float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ, int visibleFaces) {
        Vector3f from = new Vector3f(minX, minY, minZ);
        Vector3f to = new Vector3f(maxX, maxY, maxZ);
        for (Direction face : Direction.values()) {
            if ((visibleFaces & mask(face)) != 0) {
                BlockElementFace elementFace = new BlockElementFace(null, -1, "#tube",
                        new BlockFaceUV(uv(face, from, to), 0));
                quads.add(FACE_BAKERY.bakeQuad(from, to, elementFace, sprites.sprite(face), face,
                        BlockModelRotation.X0_Y0, null, true, MODEL_ID));
            }
        }
    }

    private FaceSprites paintedSprites(BlockState painted, RandomSource random) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(painted);
        TextureAtlasSprite fallback = model.getParticleIcon(ModelData.EMPTY);
        Map<Direction, TextureAtlasSprite> sprites = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            List<BakedQuad> quads = model.getQuads(painted, direction, random, ModelData.EMPTY, RenderType.cutout());
            if (quads.isEmpty()) {
                quads = model.getQuads(painted, direction, random, ModelData.EMPTY, null);
            }
            sprites.put(direction, quads.isEmpty() ? fallback : quads.get(0).getSprite());
        }
        return new FaceSprites(sprites);
    }

    private FaceSprites overlaySprites(PneumaticTubeBlockEntity.PneumaticTubeRenderData data) {
        Map<Direction, TextureAtlasSprite> sprites = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            sprites.put(direction, direction == data.ejection() ? overlayInput
                    : direction == data.insertion() ? overlayOutput : overlay);
        }
        return new FaceSprites(sprites);
    }

    private FaceSprites endpointSprites(TextureAtlasSprite sprite) {
        return uniformSprites(sprite);
    }

    private static FaceSprites uniformSprites(TextureAtlasSprite sprite) {
        Map<Direction, TextureAtlasSprite> sprites = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            sprites.put(direction, sprite);
        }
        return new FaceSprites(sprites);
    }

    private static boolean isStraight(int mask) {
        return mask == 0b110000 || mask == 0b000011 || mask == 0b001100;
    }

    private static int axisMask(Direction direction) {
        return mask(direction) | mask(direction.getOpposite());
    }

    private static int mask(Direction direction) {
        return switch (direction) {
            case EAST -> 32;
            case WEST -> 16;
            case UP -> 8;
            case DOWN -> 4;
            case SOUTH -> 2;
            case NORTH -> 1;
        };
    }

    private static float[] uv(Direction direction, Vector3f from, Vector3f to) {
        return switch (direction) {
            case UP, DOWN -> new float[] {from.x(), from.z(), to.x(), to.z()};
            case NORTH, SOUTH -> new float[] {from.x(), 16.0F - to.y(), to.x(), 16.0F - from.y()};
            case EAST, WEST -> new float[] {from.z(), 16.0F - to.y(), to.z(), 16.0F - from.y()};
        };
    }

    @Override public boolean useAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean usesBlockLight() { return true; }
    @Override public boolean isCustomRenderer() { return false; }
    @Override @Deprecated public TextureAtlasSprite getParticleIcon() { return base; }
    @Override public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) { return base; }
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

    private record FaceSprites(Map<Direction, TextureAtlasSprite> sprites) {
        TextureAtlasSprite sprite(Direction direction) { return sprites.get(direction); }
    }

    private record RenderKey(int pneumaticMask, int airMask, @Nullable Direction insertion,
                             @Nullable Direction ejection, boolean item, boolean overlayEnabled) {
        static RenderKey item(boolean paintable) { return new RenderKey(0, 0, null, null, true, paintable); }
    }
}
