package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;

public final class ObjRenderUtils {
    private static final long BAKED_MODEL_RANDOM_SEED = 42L;
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final ThreadLocal<RandomSource> BAKED_MODEL_RANDOM =
            ThreadLocal.withInitial(() -> RandomSource.create(BAKED_MODEL_RANDOM_SEED));
    private static final ThreadLocal<Vector3f> LEGACY_SHADOW_NORMAL =
            ThreadLocal.withInitial(Vector3f::new);

    public static void renderBlockModel(
            BakedModel model,
            BlockState state,
            ModelBlockRenderer modelRenderer,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay,
            RenderType overrideRenderType,
            float lightMultiplier) {
        renderBlockModel(model, state, modelRenderer, poseStack, buffer, packedLight, packedOverlay, overrideRenderType, lightMultiplier, 0xFFFFFF, false, false);
    }

    public static void renderBlockModel(
            BakedModel model,
            BlockState state,
            ModelBlockRenderer modelRenderer,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay,
            RenderType overrideRenderType,
            float lightMultiplier,
            int colorOverride,
            boolean hasColorOverride,
            boolean legacyShadow) {
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        int color = hasColorOverride ? colorOverride : blockColors.getColor(state, (BlockAndTintGetter) null, (BlockPos) null, 0);
        float red = Mth.clamp(((float) (color >> 16 & 255) / 255.0F) * lightMultiplier, 0.0F, 1.0F);
        float green = Mth.clamp(((float) (color >> 8 & 255) / 255.0F) * lightMultiplier, 0.0F, 1.0F);
        float blue = Mth.clamp(((float) (color & 255) / 255.0F) * lightMultiplier, 0.0F, 1.0F);
        PoseStack.Pose pose = poseStack.last();
        if (legacyShadow) {
            float shadow = legacyShadowFactor(pose, Direction.UP);
            red *= shadow;
            green *= shadow;
            blue *= shadow;
        }
        RandomSource random = bakedModelRandom();

        for (RenderType renderType : model.getRenderTypes(state, random, ModelData.EMPTY)) {
            RenderType effectiveType = overrideRenderType != null
                    ? overrideRenderType
                    : RenderTypeHelper.getEntityRenderType(renderType, false);
            modelRenderer.renderModel(
                    pose,
                    buffer.getBuffer(effectiveType),
                    state,
                    model,
                    red,
                    green,
                    blue,
                    packedLight,
                    packedOverlay,
                    ModelData.EMPTY,
                    renderType);
        }
    }

    /**
     * Renders selected cull-face groups from a baked block model.  This is for
     * entity-rendered multiblock sections, where vanilla's chunk renderer is
     * not present to discard the faces shared by two adjacent sections.
     */
    public static void renderBlockModelFaces(
            BakedModel model,
            BlockState state,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay,
            boolean renderDown,
            boolean renderUp) {
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        int color = blockColors.getColor(state, (BlockAndTintGetter) null, (BlockPos) null, 0);
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        PoseStack.Pose pose = poseStack.last();
        RandomSource random = bakedModelRandom();

        for (RenderType renderType : model.getRenderTypes(state, random, ModelData.EMPTY)) {
            VertexConsumer consumer = buffer.getBuffer(RenderTypeHelper.getEntityRenderType(renderType, false));
            for (Direction direction : DIRECTIONS) {
                if ((direction == Direction.DOWN && !renderDown) || (direction == Direction.UP && !renderUp)) {
                    continue;
                }
                random.setSeed(BAKED_MODEL_RANDOM_SEED);
                renderQuadListFaces(pose, consumer,
                        model.getQuads(state, direction, random, ModelData.EMPTY, renderType),
                        packedLight, packedOverlay, 1.0F, red, green, blue, false, renderDown, renderUp);
            }
            random.setSeed(BAKED_MODEL_RANDOM_SEED);
            // Block-model JSON normally leaves cullface unset, so its horizontal
            // faces live in this unculled list.  Filter by the baked quad's own
            // direction rather than assuming they were supplied by the loop above.
            renderQuadListFaces(pose, consumer, model.getQuads(state, null, random, ModelData.EMPTY, renderType),
                    packedLight, packedOverlay, 1.0F, red, green, blue, false, renderDown, renderUp);
        }
    }

    public static void renderModel(
            BakedModel model,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay,
            RenderType renderType) {
        renderModel(model, poseStack, buffer, packedLight, packedOverlay, renderType, 1.0F);
    }

    public static void renderModel(
            BakedModel model,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay,
            RenderType renderType,
            float lightMultiplier) {
        renderModel(model, poseStack, buffer, packedLight, packedOverlay, renderType, lightMultiplier, 0xFFFFFF, false, false);
    }

    public static void renderModel(
            BakedModel model,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay,
            RenderType renderType,
            float lightMultiplier,
            int colorOverride,
            boolean hasColorOverride,
            boolean legacyShadow) {
        VertexConsumer consumer = buffer.getBuffer(renderType);
        BlockState dummyState = Blocks.AIR.defaultBlockState();
        RandomSource random = bakedModelRandom();
        PoseStack.Pose pose = poseStack.last();
        float baseRed = hasColorOverride ? (float) (colorOverride >> 16 & 255) / 255.0F : 1.0F;
        float baseGreen = hasColorOverride ? (float) (colorOverride >> 8 & 255) / 255.0F : 1.0F;
        float baseBlue = hasColorOverride ? (float) (colorOverride & 255) / 255.0F : 1.0F;

        for (Direction direction : DIRECTIONS) {
            renderQuadList(pose, consumer, model.getQuads(dummyState, direction, random, ModelData.EMPTY, renderType), packedLight, packedOverlay, lightMultiplier, baseRed, baseGreen, baseBlue, legacyShadow);
        }
        renderQuadList(pose, consumer, model.getQuads(dummyState, null, random, ModelData.EMPTY, renderType), packedLight, packedOverlay, lightMultiplier, baseRed, baseGreen, baseBlue, legacyShadow);
    }

    private static void renderQuadList(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            List<BakedQuad> quads,
            int packedLight,
            int packedOverlay,
            float lightMultiplier,
            float baseRed,
            float baseGreen,
            float baseBlue,
            boolean legacyShadow) {
        for (BakedQuad quad : quads) {
            float shadow = legacyShadow ? legacyShadowFactor(pose, quad.getDirection()) : 1.0F;
            float red = Mth.clamp(baseRed * lightMultiplier * shadow, 0.0F, 1.0F);
            float green = Mth.clamp(baseGreen * lightMultiplier * shadow, 0.0F, 1.0F);
            float blue = Mth.clamp(baseBlue * lightMultiplier * shadow, 0.0F, 1.0F);
            consumer.putBulkData(pose, quad, red, green, blue, packedLight, packedOverlay);
        }
    }

    private static void renderQuadListFaces(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            List<BakedQuad> quads,
            int packedLight,
            int packedOverlay,
            float lightMultiplier,
            float baseRed,
            float baseGreen,
            float baseBlue,
            boolean legacyShadow,
            boolean renderDown,
            boolean renderUp) {
        for (BakedQuad quad : quads) {
            Direction direction = quad.getDirection();
            if ((direction == Direction.DOWN && !renderDown) || (direction == Direction.UP && !renderUp)) {
                continue;
            }
            float shadow = legacyShadow ? legacyShadowFactor(pose, direction) : 1.0F;
            float red = Mth.clamp(baseRed * lightMultiplier * shadow, 0.0F, 1.0F);
            float green = Mth.clamp(baseGreen * lightMultiplier * shadow, 0.0F, 1.0F);
            float blue = Mth.clamp(baseBlue * lightMultiplier * shadow, 0.0F, 1.0F);
            consumer.putBulkData(pose, quad, red, green, blue, packedLight, packedOverlay);
        }
    }

    private static float legacyShadowFactor(PoseStack.Pose pose, Direction direction) {
        Vector3f normal = LEGACY_SHADOW_NORMAL.get()
                .set((float) direction.getStepX(), (float) direction.getStepY(), (float) direction.getStepZ())
                .mul(pose.normal());
        float brightness = (normal.y() + 0.7F) * 0.9F - Math.abs(normal.x()) * 0.1F + Math.abs(normal.z()) * 0.1F;
        return Math.max(0.45F, brightness);
    }

    private static RandomSource bakedModelRandom() {
        RandomSource random = BAKED_MODEL_RANDOM.get();
        random.setSeed(BAKED_MODEL_RANDOM_SEED);
        return random;
    }

    private ObjRenderUtils() {
    }
}
