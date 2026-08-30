package com.hbm.ntm.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Baked OBJ wrapper that carries continuous legacy normal lighting in world vertex color. */
public final class LegacyLitObjBakedModel extends BakedModelWrapper<BakedModel> {
    private static final float LIGHT_POWER = 0.6F;
    private static final float AMBIENT_LIGHT = 0.4F;
    private static final float L0_X = 0.16169041F;
    private static final float L0_Y = 0.80845207F;
    private static final float L0_Z = -0.56591646F;
    private static final float L1_X = -0.16169041F;
    private static final float L1_Y = 0.80845207F;
    private static final float L1_Z = 0.56591646F;

    private final Map<List<BakedQuad>, List<BakedQuad>> worldQuadLists =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private final Map<List<BakedQuad>, List<BakedQuad>> itemQuadLists =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private final Map<List<BakedModel>, List<BakedModel>> itemPassLists =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private final Map<BakedModel, LegacyLitObjBakedModel> itemPassModels =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private final boolean itemPass;
    private final boolean faceNormals;
    private final boolean reverseBakedFaceNormals;
    private final boolean doubleSided;
    private final LegacyLitObjBakedModel itemPassModel;
    private final List<BakedModel> singletonItemPass;

    public LegacyLitObjBakedModel(BakedModel originalModel) {
        this(originalModel, false, false, false, false);
    }

    LegacyLitObjBakedModel(BakedModel originalModel, boolean faceNormals) {
        this(originalModel, false, faceNormals, false, false);
    }

    LegacyLitObjBakedModel(BakedModel originalModel, boolean faceNormals, boolean reverseBakedFaceNormals) {
        this(originalModel, false, faceNormals, reverseBakedFaceNormals, false);
    }

    LegacyLitObjBakedModel(BakedModel originalModel, boolean faceNormals, boolean reverseBakedFaceNormals,
            boolean doubleSided) {
        this(originalModel, false, faceNormals, reverseBakedFaceNormals, doubleSided);
    }

    private LegacyLitObjBakedModel(BakedModel originalModel, boolean itemPass, boolean faceNormals,
            boolean reverseBakedFaceNormals, boolean doubleSided) {
        super(originalModel);
        this.itemPass = itemPass;
        this.faceNormals = faceNormals;
        this.reverseBakedFaceNormals = reverseBakedFaceNormals;
        this.doubleSided = doubleSided;
        if (itemPass) {
            this.itemPassModel = this;
            this.singletonItemPass = List.of(this);
        } else {
            this.itemPassModel = new LegacyLitObjBakedModel(originalModel, true, faceNormals,
                    reverseBakedFaceNormals, doubleSided);
            this.singletonItemPass = List.of(itemPassModel);
            this.itemPassModels.put(originalModel, itemPassModel);
        }
    }

    @Override
    @Deprecated
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return transformList(originalModel.getQuads(state, side, random), itemPass);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
            @NotNull RandomSource random, @NotNull ModelData modelData, @Nullable RenderType renderType) {
        List<BakedQuad> quads = originalModel.getQuads(state, side, random, modelData, renderType);
        return transformList(quads,
                itemPass || LegacyLitObjRenderTypes.isItemRenderType(renderType));
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
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack, boolean leftHand) {
        BakedModel transformed = originalModel.applyTransform(context, poseStack, leftHand);
        return transformed == originalModel ? this
                : new LegacyLitObjBakedModel(transformed, itemPass, faceNormals, reverseBakedFaceNormals,
                        doubleSided);
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        if (itemPass) {
            return singletonItemPass;
        }
        List<BakedModel> passes = originalModel.getRenderPasses(stack, fabulous);
        if (passes.size() == 1 && passes.get(0) == originalModel) {
            return singletonItemPass;
        }
        synchronized (itemPassLists) {
            return itemPassLists.computeIfAbsent(passes, sourcePasses -> {
                List<BakedModel> wrapped = new ArrayList<>(sourcePasses.size());
                for (BakedModel pass : sourcePasses) {
                    synchronized (itemPassModels) {
                        wrapped.add(itemPassModels.computeIfAbsent(pass,
                                model -> new LegacyLitObjBakedModel(model, true, faceNormals,
                                        reverseBakedFaceNormals, doubleSided)));
                    }
                }
                return List.copyOf(wrapped);
            });
        }
    }

    List<BakedQuad> getItemQuads(@Nullable BlockState state, @Nullable Direction side,
            @NotNull RandomSource random, @NotNull ModelData modelData,
            @Nullable RenderType renderType) {
        return itemPassModel.getQuads(state, side, random, modelData, renderType);
    }

    private List<BakedQuad> transformList(List<BakedQuad> source, boolean item) {
        if (source.isEmpty()) {
            return source;
        }
        Map<List<BakedQuad>, List<BakedQuad>> cache = item ? itemQuadLists : worldQuadLists;
        synchronized (cache) {
            return cache.computeIfAbsent(source, quads -> {
                List<BakedQuad> transformed = new ArrayList<>(quads.size() * (doubleSided ? 2 : 1));
                for (BakedQuad quad : quads) {
                    BakedQuad front = transformQuad(quad, item, faceNormals, reverseBakedFaceNormals);
                    transformed.add(front);
                    if (doubleSided) {
                        transformed.add(reverseWinding(front));
                    }
                }
                return List.copyOf(transformed);
            });
        }
    }

    /**
     * Restores legacy {@code GL_CULL_FACE=false} while retaining Forge's chunk-compatible
     * cull-enabled render layers. The back copy keeps the source normal, UV, color and light
     * values; only its winding is reversed, matching fixed-function two-sided visibility.
     */
    private static BakedQuad reverseWinding(BakedQuad source) {
        int[] sourceVertices = source.getVertices();
        int[] reversed = new int[sourceVertices.length];
        int[] order = sameVertex(sourceVertices, 2, 3)
                ? new int[] {0, 2, 1, 1}
                : new int[] {0, 3, 2, 1};
        for (int vertex = 0; vertex < 4; vertex++) {
            System.arraycopy(sourceVertices, order[vertex] * IQuadTransformer.STRIDE,
                    reversed, vertex * IQuadTransformer.STRIDE, IQuadTransformer.STRIDE);
        }
        return new BakedQuad(reversed, source.getTintIndex(), source.getDirection(), source.getSprite(),
                source.isShade(), source.hasAmbientOcclusion());
    }

    private static boolean sameVertex(int[] vertices, int first, int second) {
        int firstBase = first * IQuadTransformer.STRIDE;
        int secondBase = second * IQuadTransformer.STRIDE;
        for (int element = 0; element < IQuadTransformer.STRIDE; element++) {
            if (vertices[firstBase + element] != vertices[secondBase + element]) {
                return false;
            }
        }
        return true;
    }

    private static BakedQuad transformQuad(BakedQuad source, boolean item, boolean faceNormals,
            boolean reverseBakedFaceNormals) {
        int[] vertices = source.getVertices().clone();
        int faceNormal = 0;
        if (faceNormals) {
            faceNormal = calculatePackedFaceNormal(vertices, reverseBakedFaceNormals);
            for (int vertex = 0; vertex < 4; vertex++) {
                vertices[vertex * IQuadTransformer.STRIDE + IQuadTransformer.NORMAL] = faceNormal;
            }
        }
        if (!item) {
            for (int vertex = 0; vertex < 4; vertex++) {
                int base = vertex * IQuadTransformer.STRIDE;
                int normal = faceNormals ? faceNormal : vertices[base + IQuadTransformer.NORMAL];
                float nx = (byte) (normal & 0xFF) / 127.0F;
                float ny = (byte) ((normal >>> 8) & 0xFF) / 127.0F;
                float nz = (byte) ((normal >>> 16) & 0xFF) / 127.0F;
                float light = standardLight(nx, ny, nz);
                int colorIndex = base + IQuadTransformer.COLOR;
                vertices[colorIndex] = multiplyAbgr(vertices[colorIndex], light);
            }
        }
        return new BakedQuad(vertices, source.getTintIndex(), source.getDirection(), source.getSprite(),
                false, false);
    }

    /** Matches the 1.7.10 AdvancedModelLoader/HFR noSmooth v1 x v2 contract. */
    private static int calculatePackedFaceNormal(int[] vertices, boolean reverseBakedFaceNormals) {
        int p0 = IQuadTransformer.POSITION;
        int p1 = IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
        int p2 = IQuadTransformer.STRIDE * 2 + IQuadTransformer.POSITION;
        float x0 = Float.intBitsToFloat(vertices[p0]);
        float y0 = Float.intBitsToFloat(vertices[p0 + 1]);
        float z0 = Float.intBitsToFloat(vertices[p0 + 2]);
        float ax = Float.intBitsToFloat(vertices[p1]) - x0;
        float ay = Float.intBitsToFloat(vertices[p1 + 1]) - y0;
        float az = Float.intBitsToFloat(vertices[p1 + 2]) - z0;
        float bx = Float.intBitsToFloat(vertices[p2]) - x0;
        float by = Float.intBitsToFloat(vertices[p2 + 1]) - y0;
        float bz = Float.intBitsToFloat(vertices[p2 + 2]) - z0;
        float nx = ay * bz - az * by;
        float ny = az * bx - ax * bz;
        float nz = ax * by - ay * bx;
        float lengthSquared = nx * nx + ny * ny + nz * nz;
        if (!(lengthSquared >= 1.0E-8F) || !Float.isFinite(lengthSquared)) {
            // 1.7.10 Vec3#normalize returned the zero vector for a degenerate face.
            return 0;
        }
        float inverseLength = Mth.invSqrt(lengthSquared);
        if (reverseBakedFaceNormals) {
            inverseLength = -inverseLength;
        }
        return packNormal(nx * inverseLength, ny * inverseLength, nz * inverseLength);
    }

    private static int packNormal(float x, float y, float z) {
        int packedX = Mth.clamp(Math.round(x * 127.0F), -127, 127) & 0xFF;
        int packedY = Mth.clamp(Math.round(y * 127.0F), -127, 127) & 0xFF;
        int packedZ = Mth.clamp(Math.round(z * 127.0F), -127, 127) & 0xFF;
        return packedX | (packedY << 8) | (packedZ << 16);
    }

    private static float standardLight(float nx, float ny, float nz) {
        float lengthSquared = nx * nx + ny * ny + nz * nz;
        if (!(lengthSquared > 1.0E-10F) || !Float.isFinite(lengthSquared)) {
            return AMBIENT_LIGHT;
        }
        float inverseLength = Mth.invSqrt(lengthSquared);
        nx *= inverseLength;
        ny *= inverseLength;
        nz *= inverseLength;
        float light0 = Math.max(0.0F, nx * L0_X + ny * L0_Y + nz * L0_Z);
        float light1 = Math.max(0.0F, nx * L1_X + ny * L1_Y + nz * L1_Z);
        return Math.min(1.0F, AMBIENT_LIGHT + LIGHT_POWER * light0 + LIGHT_POWER * light1);
    }

    private static int multiplyAbgr(int abgr, float light) {
        int red = Math.round((abgr & 0xFF) * light);
        int green = Math.round(((abgr >>> 8) & 0xFF) * light);
        int blue = Math.round(((abgr >>> 16) & 0xFF) * light);
        return (abgr & 0xFF000000)
                | (Mth.clamp(blue, 0, 255) << 16)
                | (Mth.clamp(green, 0, 255) << 8)
                | Mth.clamp(red, 0, 255);
    }
}
