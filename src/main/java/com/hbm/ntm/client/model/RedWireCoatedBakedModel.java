package com.hbm.ntm.client.model;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.RedWireCoatedCt;
import com.hbm.ntm.blockentity.RedCableBlockEntity;
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

public class RedWireCoatedBakedModel implements BakedModel {
    private static final ResourceLocation MODEL_ID = new ResourceLocation(HbmNtm.MOD_ID, "block/red_wire_coated");
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final FaceSection[][] FACE_SECTIONS = buildFaceSections();

    private final TextureAtlasSprite baseSprite;
    private final TextureAtlasSprite ctSprite;
    private final TextureAtlasSprite particleSprite;
    private final ItemTransforms transforms;
    private final Map<RedWireCoatedCt.Data, List<BakedQuad>> quadCache = new ConcurrentHashMap<>();

    public RedWireCoatedBakedModel(TextureAtlasSprite baseSprite, TextureAtlasSprite ctSprite,
            TextureAtlasSprite particleSprite, ItemTransforms transforms) {
        this.baseSprite = baseSprite;
        this.ctSprite = ctSprite;
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
        RedWireCoatedCt.Data data = modelData.get(RedCableBlockEntity.RED_WIRE_COATED_CT_PROPERTY);
        if (data == null) {
            data = RedWireCoatedCt.DEFAULT_DATA;
        }
        return quadCache.computeIfAbsent(data, this::buildQuads);
    }

    private List<BakedQuad> buildQuads(RedWireCoatedCt.Data data) {
        List<BakedQuad> quads = new ArrayList<>(24);
        for (Direction face : RedWireCoatedCt.DIRECTIONS) {
            if (!data.isFaceVisible(face)) {
                continue;
            }
            int ctFace = data.face(face);
            FaceSection[] sections = FACE_SECTIONS[face.ordinal()];
            for (int index = 0; index < sections.length; index++) {
                int fragment = RedWireCoatedCt.ctFaceFragment(ctFace, index);
                quads.add(bakeSubFace(face, sections[index], fragment));
            }
        }
        return List.copyOf(quads);
    }

    private BakedQuad bakeSubFace(Direction face, FaceSection section, int fragment) {
        BlockElementFace blockFace = new BlockElementFace(null, -1, "#ct", new BlockFaceUV(fragmentUv(fragment), 0));
        return FACE_BAKERY.bakeQuad(section.from(), section.to(), blockFace, sprite(fragment), face,
                BlockModelRotation.X0_Y0, null, true, MODEL_ID);
    }

    private TextureAtlasSprite sprite(int fragment) {
        return fragment < 4 ? baseSprite : ctSprite;
    }

    private static float[] fragmentUv(int fragment) {
        boolean base = fragment < 4;
        float slices = base ? 2.0F : 4.0F;
        float length = 16.0F / slices;
        float minU = 0.0F;
        float minV = 0.0F;
        if (!base) {
            if (fragment >= 16 || fragment >= 8 && fragment < 12) {
                minU += length * 2.0F;
            }
            if (fragment >= 12 && fragment < 16 || fragment >= 8 && fragment < 12) {
                minV += length * 2.0F;
            }
        }
        if ((fragment & 1) != 0) {
            minU += length;
        }
        if ((fragment & 2) != 0) {
            minV += length;
        }
        return new float[] { minU, minV, minU + length, minV + length };
    }

    private static FaceSection[][] buildFaceSections() {
        FaceSection[][] sections = new FaceSection[Direction.values().length][];
        for (Direction face : RedWireCoatedCt.DIRECTIONS) {
            sections[face.ordinal()] = buildFaceSections(face);
        }
        return sections;
    }

    private static FaceSection[] buildFaceSections(Direction face) {
        return switch (face) {
            case EAST -> buildFaceSections(
                    1.0D, 1.0D, 1.0D,
                    1.0D, 1.0D, 0.0D,
                    1.0D, 0.0D, 1.0D,
                    1.0D, 0.0D, 0.0D);
            case WEST -> buildFaceSections(
                    0.0D, 1.0D, 0.0D,
                    0.0D, 1.0D, 1.0D,
                    0.0D, 0.0D, 0.0D,
                    0.0D, 0.0D, 1.0D);
            case UP -> buildFaceSections(
                    0.0D, 1.0D, 0.0D,
                    1.0D, 1.0D, 0.0D,
                    0.0D, 1.0D, 1.0D,
                    1.0D, 1.0D, 1.0D);
            case DOWN -> buildFaceSections(
                    0.0D, 0.0D, 1.0D,
                    1.0D, 0.0D, 1.0D,
                    0.0D, 0.0D, 0.0D,
                    1.0D, 0.0D, 0.0D);
            case SOUTH -> buildFaceSections(
                    0.0D, 1.0D, 1.0D,
                    1.0D, 1.0D, 1.0D,
                    0.0D, 0.0D, 1.0D,
                    1.0D, 0.0D, 1.0D);
            case NORTH -> buildFaceSections(
                    1.0D, 1.0D, 0.0D,
                    0.0D, 1.0D, 0.0D,
                    1.0D, 0.0D, 0.0D,
                    0.0D, 0.0D, 0.0D);
        };
    }

    private static FaceSection[] buildFaceSections(double topLeftX, double topLeftY, double topLeftZ,
            double topRightX, double topRightY, double topRightZ,
            double bottomLeftX, double bottomLeftY, double bottomLeftZ,
            double bottomRightX, double bottomRightY, double bottomRightZ) {
        double topCenterX = average(topLeftX, topRightX);
        double topCenterY = average(topLeftY, topRightY);
        double topCenterZ = average(topLeftZ, topRightZ);
        double bottomCenterX = average(bottomLeftX, bottomRightX);
        double bottomCenterY = average(bottomLeftY, bottomRightY);
        double bottomCenterZ = average(bottomLeftZ, bottomRightZ);
        double centerLeftX = average(topLeftX, bottomLeftX);
        double centerLeftY = average(topLeftY, bottomLeftY);
        double centerLeftZ = average(topLeftZ, bottomLeftZ);
        double centerRightX = average(topRightX, bottomRightX);
        double centerRightY = average(topRightY, bottomRightY);
        double centerRightZ = average(topRightZ, bottomRightZ);
        double centerX = average(topCenterX, bottomCenterX);
        double centerY = average(topCenterY, bottomCenterY);
        double centerZ = average(topCenterZ, bottomCenterZ);

        return new FaceSection[] {
                section(topLeftX, topLeftY, topLeftZ, topCenterX, topCenterY, topCenterZ,
                        centerLeftX, centerLeftY, centerLeftZ, centerX, centerY, centerZ),
                section(topCenterX, topCenterY, topCenterZ, topRightX, topRightY, topRightZ,
                        centerX, centerY, centerZ, centerRightX, centerRightY, centerRightZ),
                section(centerLeftX, centerLeftY, centerLeftZ, centerX, centerY, centerZ,
                        bottomLeftX, bottomLeftY, bottomLeftZ, bottomCenterX, bottomCenterY, bottomCenterZ),
                section(centerX, centerY, centerZ, centerRightX, centerRightY, centerRightZ,
                        bottomCenterX, bottomCenterY, bottomCenterZ, bottomRightX, bottomRightY, bottomRightZ)
        };
    }

    private static FaceSection section(double ax, double ay, double az,
            double bx, double by, double bz,
            double cx, double cy, double cz,
            double dx, double dy, double dz) {
        float minX = min(ax, bx, cx, dx) * 16.0F;
        float minY = min(ay, by, cy, dy) * 16.0F;
        float minZ = min(az, bz, cz, dz) * 16.0F;
        float maxX = max(ax, bx, cx, dx) * 16.0F;
        float maxY = max(ay, by, cy, dy) * 16.0F;
        float maxZ = max(az, bz, cz, dz) * 16.0F;
        return new FaceSection(new Vector3f(minX, minY, minZ), new Vector3f(maxX, maxY, maxZ));
    }

    private static double average(double a, double b) {
        return (a + b) * 0.5D;
    }

    private static float min(double a, double b, double c, double d) {
        return (float) Math.min(Math.min(a, b), Math.min(c, d));
    }

    private static float max(double a, double b, double c, double d) {
        return (float) Math.max(Math.max(a, b), Math.max(c, d));
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

    private record FaceSection(Vector3f from, Vector3f to) {
    }
}
