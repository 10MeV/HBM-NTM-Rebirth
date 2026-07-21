package com.hbm.ntm.client.model;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.CraneLogisticsBlock;
import com.hbm.ntm.blockentity.CraneLogisticsBlockEntity;
import com.hbm.ntm.blockentity.CraneLogisticsBlockEntity.CraneRenderData;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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

/** Recreates the six-face icon selection in legacy BlockCraneBase#getIcon. */
public class CraneLogisticsBakedModel implements BakedModel {
    private static final ResourceLocation MODEL_ID = new ResourceLocation(HbmNtm.MOD_ID, "block/crane_logistics");
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final Vector3f FROM = new Vector3f(0.0F, 0.0F, 0.0F);
    private static final Vector3f TO = new Vector3f(16.0F, 16.0F, 16.0F);

    private final Style style;
    private final Map<String, TextureAtlasSprite> sprites;
    private final ItemTransforms transforms;
    private final Map<RenderKey, List<BakedQuad>> quadCache = new ConcurrentHashMap<>();

    public CraneLogisticsBakedModel(Style style, Map<String, TextureAtlasSprite> sprites, ItemTransforms transforms) {
        this.style = style;
        this.sprites = sprites;
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
        CraneRenderData data = modelData.get(CraneLogisticsBlockEntity.RENDER_DATA_PROPERTY);
        Direction input = data == null ? stateInput(state) : data.input();
        Direction output = data == null ? input.getOpposite() : data.output();
        return quadCache.computeIfAbsent(new RenderKey(input, output), this::buildQuads);
    }

    private List<BakedQuad> buildQuads(RenderKey key) {
        List<BakedQuad> quads = new ArrayList<>(6);
        for (Direction face : Direction.values()) {
            String texture = textureFor(face, key.input(), key.output());
            quads.add(bakeFace(face, sprites.get(texture), face == Direction.UP
                    ? topRotation(key.input(), key.output()) * 90 : 0));
        }
        return List.copyOf(quads);
    }

    private String textureFor(Direction face, Direction input, Direction output) {
        boolean overridden = output.getOpposite() != input;
        Direction turn = leftHandRotation(input, output);
        if (face.getAxis().isVertical()) {
            if (face == output) return style.out;
            if (face == input) return style.in;
            if (face == Direction.UP) {
                if (overridden && turn == Direction.UP) return style.topLeft;
                if (overridden && turn == Direction.DOWN) return style.topRight;
                if (!overridden) return style.directional;
            }
            return style.baseTop;
        }
        if (face == output) return style.sideOut;
        if (face == input) return style.sideIn;
        if (overridden && turn != null) {
            if (face == turn) {
                if (output == Direction.UP) return style.sideLeftTurnUp;
                if (output == Direction.DOWN) return style.sideRightTurnDown;
                if (input == Direction.UP) return style.sideUpTurnRight;
                if (input == Direction.DOWN) return style.sideDownTurnLeft;
            }
            if (face == turn.getOpposite()) {
                if (output == Direction.UP) return style.sideRightTurnUp;
                if (output == Direction.DOWN) return style.sideLeftTurnDown;
                if (input == Direction.UP) return style.sideUpTurnLeft;
                if (input == Direction.DOWN) return style.sideDownTurnRight;
            }
        } else if (!overridden) {
            if (output == Direction.UP) return style.directionalUp;
            if (output == Direction.DOWN) return style.directionalDown;
        }
        return style.baseSide;
    }

    private int topRotation(Direction input, Direction output) {
        if (input.getAxis().isVertical()) return 0;
        Direction turn = leftHandRotation(input, output);
        if (style.turnAwareTopRotation && output.getOpposite() != input) {
            if (turn == Direction.UP) {
                return switch (input) {
                    case NORTH -> 2;
                    case SOUTH -> 1;
                    case WEST -> 3;
                    case EAST -> 0;
                    default -> 0;
                };
            }
            if (turn == Direction.DOWN) {
                return switch (input) {
                    case NORTH -> 1;
                    case SOUTH -> 2;
                    case WEST -> 0;
                    case EAST -> 3;
                    default -> 0;
                };
            }
        }
        return switch (input) {
            case NORTH -> 3;
            case SOUTH -> 0;
            case WEST -> 1;
            case EAST -> 2;
            default -> 0;
        };
    }

    private BakedQuad bakeFace(Direction face, TextureAtlasSprite sprite, int rotation) {
        BlockElementFace elementFace = new BlockElementFace(null, -1, "#crane",
                new BlockFaceUV(uv(face), rotation));
        return FACE_BAKERY.bakeQuad(FROM, TO, elementFace, sprite, face, BlockModelRotation.X0_Y0, null, true,
                MODEL_ID);
    }

    private static float[] uv(Direction face) {
        return switch (face) {
            case UP, DOWN -> new float[] {0.0F, 0.0F, 16.0F, 16.0F};
            case NORTH, SOUTH, EAST, WEST -> new float[] {0.0F, 0.0F, 16.0F, 16.0F};
        };
    }

    private static Direction stateInput(@Nullable BlockState state) {
        return state != null && state.hasProperty(CraneLogisticsBlock.FACING)
                ? state.getValue(CraneLogisticsBlock.FACING) : Direction.DOWN;
    }

    @Nullable
    private static Direction leftHandRotation(Direction input, Direction output) {
        int x = output.getStepY() * input.getStepZ() - output.getStepZ() * input.getStepY();
        int y = output.getStepZ() * input.getStepX() - output.getStepX() * input.getStepZ();
        int z = output.getStepX() * input.getStepY() - output.getStepY() * input.getStepX();
        return x == 0 && y == 0 && z == 0 ? null : Direction.getNearest(x, y, z);
    }

    @Override public boolean useAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean usesBlockLight() { return true; }
    @Override public boolean isCustomRenderer() { return false; }
    @Override @Deprecated public TextureAtlasSprite getParticleIcon() { return sprites.get(style.baseTop); }
    @Override public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) { return getParticleIcon(); }
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

    private record RenderKey(Direction input, Direction output) {
    }

    public enum Style {
        EXTRACTOR("crane_in", "crane_side_in", "crane_out", "crane_side_out", "crane_out", true, true),
        INSERTER("crane_in", "crane_side_in", "crane_out", "crane_side_out", "crane_in", false, false),
        GRABBER("crane_pull", "crane_side_pull", "crane_out", "crane_side_out", "crane_grabber", false, false),
        BOXER("crane_in", "crane_side_in", "crane_box", "crane_side_box", "crane_boxer", false, false),
        UNBOXER("crane_in", "crane_side_in", "crane_box", "crane_side_box", "crane_unboxer", true, true);

        private final String baseTop = "crane_top";
        private final String baseSide = "crane_side";
        private final String in;
        private final String sideIn;
        private final String out;
        private final String sideOut;
        private final String directional;
        private final String directionalUp;
        private final String directionalDown;
        private final String topLeft;
        private final String topRight;
        private final String sideLeftTurnUp;
        private final String sideRightTurnUp;
        private final String sideLeftTurnDown;
        private final String sideRightTurnDown;
        private final String sideUpTurnLeft;
        private final String sideUpTurnRight;
        private final String sideDownTurnLeft;
        private final String sideDownTurnRight;
        private final boolean turnAwareTopRotation;
        private final List<String> textures;

        Style(String in, String sideIn, String out, String sideOut, String prefix, boolean reverseVertical,
                boolean turnAwareTopRotation) {
            this.in = in;
            this.sideIn = sideIn;
            this.out = out;
            this.sideOut = sideOut;
            this.directional = prefix + "_top";
            this.directionalUp = prefix + "_side_" + (reverseVertical ? "down" : "up");
            this.directionalDown = prefix + "_side_" + (reverseVertical ? "up" : "down");
            this.topLeft = prefix + "_top_" + (reverseVertical ? "right" : "left");
            this.topRight = prefix + "_top_" + (reverseVertical ? "left" : "right");
            this.sideLeftTurnUp = prefix + "_side_" + (reverseVertical ? "up_turn_left" : "left_turn_up");
            this.sideRightTurnUp = prefix + "_side_" + (reverseVertical ? "up_turn_right" : "right_turn_up");
            this.sideLeftTurnDown = prefix + "_side_" + (reverseVertical ? "down_turn_left" : "left_turn_down");
            this.sideRightTurnDown = prefix + "_side_" + (reverseVertical ? "down_turn_right" : "right_turn_down");
            this.sideUpTurnLeft = prefix + "_side_" + (reverseVertical ? "left_turn_up" : "up_turn_left");
            this.sideUpTurnRight = prefix + "_side_" + (reverseVertical ? "right_turn_up" : "up_turn_right");
            this.sideDownTurnLeft = prefix + "_side_" + (reverseVertical ? "left_turn_down" : "down_turn_left");
            this.sideDownTurnRight = prefix + "_side_" + (reverseVertical ? "right_turn_down" : "down_turn_right");
            this.turnAwareTopRotation = turnAwareTopRotation;
            this.textures = List.copyOf(Arrays.asList(baseTop, baseSide, in, sideIn, out, sideOut, directional,
                    directionalUp, directionalDown, topLeft, topRight, sideLeftTurnUp, sideRightTurnUp,
                    sideLeftTurnDown, sideRightTurnDown, sideUpTurnLeft, sideUpTurnRight, sideDownTurnLeft,
                    sideDownTurnRight));
        }

        public List<String> textures() { return textures; }

        public static Style byName(String name) {
            try {
                return valueOf(name.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Unknown crane logistics model style: " + name, exception);
            }
        }
    }
}
