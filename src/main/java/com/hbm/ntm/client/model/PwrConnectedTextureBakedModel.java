package com.hbm.ntm.client.model;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.PWRAssembledBlock;
import com.hbm.ntm.blockentity.PwrConnectedTextureData;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/** Exact four-fragment-per-face carrier used by legacy {@code RenderBlocksCT}. */
public final class PwrConnectedTextureBakedModel implements BakedModel {
    private static final ResourceLocation MODEL_ID = new ResourceLocation(HbmNtm.MOD_ID, "block/pwr_block_connected");
    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    private final TextureAtlasSprite block;
    private final TextureAtlasSprite blockCt;
    private final TextureAtlasSprite port;
    private final TextureAtlasSprite portCt;
    private final ItemTransforms transforms;
    private final Map<Long, List<BakedQuad>> quadCache = new ConcurrentHashMap<>();

    public PwrConnectedTextureBakedModel(TextureAtlasSprite block, TextureAtlasSprite blockCt,
            TextureAtlasSprite port, TextureAtlasSprite portCt, ItemTransforms transforms) {
        this.block = block;
        this.blockCt = blockCt;
        this.port = port;
        this.portCt = portCt;
        this.transforms = transforms;
    }

    @Override
    @Deprecated
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return getQuads(state, side, random, ModelData.EMPTY, null);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
            @NotNull RandomSource random, @NotNull ModelData data,
            @Nullable net.minecraft.client.renderer.RenderType renderType) {
        if (side != null || renderType != null && renderType != net.minecraft.client.renderer.RenderType.solid()) return List.of();
        boolean isPort = state != null && state.hasProperty(PWRAssembledBlock.PORT)
                && state.getValue(PWRAssembledBlock.PORT);
        Long connections = data.get(PwrConnectedTextureData.CONNECTION_MASK);
        long key = ((connections == null ? 0L : connections) << 1) | (isPort ? 1L : 0L);
        return quadCache.computeIfAbsent(key, ignored -> buildQuads(isPort, connections == null ? 0L : connections));
    }

    private List<BakedQuad> buildQuads(boolean isPort, long connections) {
        TextureAtlasSprite full = isPort ? port : block;
        TextureAtlasSprite connected = isPort ? portCt : blockCt;
        List<BakedQuad> quads = new ArrayList<>(24);
        for (Direction face : Direction.values()) {
            int neighborhood = (int) (connections >>> (face.ordinal() * 8)) & 0xFF;
            addFace(quads, face, neighborhood, full, connected);
        }
        return List.copyOf(quads);
    }

    private void addFace(List<BakedQuad> quads, Direction face, int neighbors, TextureAtlasSprite full,
            TextureAtlasSprite connected) {
        addFragment(quads, face, 0, fragmentType(bit(neighbors, 3), bit(neighbors, 0), bit(neighbors, 1), false, false), full, connected);
        addFragment(quads, face, 1, fragmentType(bit(neighbors, 4), bit(neighbors, 2), bit(neighbors, 1), true, false), full, connected);
        addFragment(quads, face, 2, fragmentType(bit(neighbors, 3), bit(neighbors, 5), bit(neighbors, 6), false, true), full, connected);
        addFragment(quads, face, 3, fragmentType(bit(neighbors, 4), bit(neighbors, 7), bit(neighbors, 6), true, true), full, connected);
    }

    private static boolean bit(int mask, int bit) { return (mask & (1 << bit)) != 0; }

    private static int fragmentType(boolean horizontal, boolean corner, boolean vertical, boolean right, boolean bottom) {
        int type = vertical && horizontal && corner ? 4 : vertical && horizontal ? 8 : vertical ? 16 : horizontal ? 12 : 0;
        return type | (right ? 1 : 0) | (bottom ? 2 : 0);
    }

    private void addFragment(List<BakedQuad> quads, Direction face, int fragment, int type, TextureAtlasSprite full,
            TextureAtlasSprite connected) {
        Vector3f[] bounds = bounds(face, fragment);
        TextureAtlasSprite sprite = type < 4 ? full : connected;
        BlockElementFace elementFace = new BlockElementFace(null, -1, "#ct", new BlockFaceUV(uv(type), 0));
        quads.add(FACE_BAKERY.bakeQuad(bounds[0], bounds[1], elementFace, sprite, face,
                BlockModelRotation.X0_Y0, null, true, MODEL_ID));
    }

    private static float[] uv(int type) {
        float u = (type >= 16 || type >= 8 && type < 12 ? 8.0F : 0.0F) + ((type & 1) != 0 ? 4.0F : 0.0F);
        float v = (type >= 12 && type < 16 || type >= 8 && type < 12 ? 8.0F : 0.0F) + ((type & 2) != 0 ? 4.0F : 0.0F);
        if (type < 4) {
            u = (type & 1) != 0 ? 8.0F : 0.0F;
            v = (type & 2) != 0 ? 8.0F : 0.0F;
            return new float[] {u, v, u + 8.0F, v + 8.0F};
        }
        return new float[] {u, v, u + 4.0F, v + 4.0F};
    }

    private static Vector3f[] bounds(Direction face, int fragment) {
        boolean right = fragment == 1 || fragment == 3;
        boolean bottom = fragment >= 2;
        float low = 0.0F;
        float high = 16.0F;
        float a0 = right ? 8.0F : 0.0F;
        float a1 = right ? 16.0F : 8.0F;
        float b0 = bottom ? 0.0F : 8.0F;
        float b1 = bottom ? 8.0F : 16.0F;
        return switch (face) {
            case UP -> pair(a0, low, bottom ? 8.0F : 0.0F, a1, high, bottom ? 16.0F : 8.0F);
            case DOWN -> pair(a0, low, bottom ? 0.0F : 8.0F, a1, high, bottom ? 8.0F : 16.0F);
            case SOUTH -> pair(a0, b0, low, a1, b1, high);
            case NORTH -> pair(right ? 0.0F : 8.0F, b0, low, right ? 8.0F : 16.0F, b1, high);
            case WEST -> pair(low, b0, right ? 8.0F : 0.0F, high, b1, right ? 16.0F : 8.0F);
            case EAST -> pair(low, b0, right ? 0.0F : 8.0F, high, b1, right ? 16.0F : 8.0F);
        };
    }

    private static Vector3f[] pair(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return new Vector3f[] {new Vector3f(minX, minY, minZ), new Vector3f(maxX, maxY, maxZ)};
    }

    @Override public boolean useAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean usesBlockLight() { return true; }
    @Override public boolean isCustomRenderer() { return false; }
    @Override @Deprecated public TextureAtlasSprite getParticleIcon() { return block; }
    @Override public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) { return block; }
    @Override public ItemTransforms getTransforms() { return transforms; }
    @Override public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }

    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack, boolean leftHand) {
        transforms.getTransform(context).apply(leftHand, poseStack);
        return this;
    }
}
