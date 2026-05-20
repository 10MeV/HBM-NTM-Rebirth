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
    private static final Direction[] DIRECTIONS = Direction.values();

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
        if (legacyShadow) {
            red *= legacyShadowFactor(poseStack);
            green *= legacyShadowFactor(poseStack);
            blue *= legacyShadowFactor(poseStack);
        }
        RandomSource random = RandomSource.create(42L);

        for (RenderType renderType : model.getRenderTypes(state, random, ModelData.EMPTY)) {
            RenderType effectiveType = overrideRenderType != null
                    ? overrideRenderType
                    : RenderTypeHelper.getEntityRenderType(renderType, false);
            modelRenderer.renderModel(
                    poseStack.last(),
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
        RandomSource random = RandomSource.create(42L);

        for (Direction direction : DIRECTIONS) {
            renderQuadList(poseStack.last(), consumer, model.getQuads(dummyState, direction, random), packedLight, packedOverlay, lightMultiplier, colorOverride, hasColorOverride, legacyShadow);
        }
        renderQuadList(poseStack.last(), consumer, model.getQuads(dummyState, null, random), packedLight, packedOverlay, lightMultiplier, colorOverride, hasColorOverride, legacyShadow);
    }

    private static void renderQuadList(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            List<BakedQuad> quads,
            int packedLight,
            int packedOverlay,
            float lightMultiplier,
            int colorOverride,
            boolean hasColorOverride,
            boolean legacyShadow) {
        for (BakedQuad quad : quads) {
            float shadow = legacyShadow ? legacyShadowFactor(pose, quad.getDirection()) : 1.0F;
            float red = hasColorOverride ? (float) (colorOverride >> 16 & 255) / 255.0F : 1.0F;
            float green = hasColorOverride ? (float) (colorOverride >> 8 & 255) / 255.0F : 1.0F;
            float blue = hasColorOverride ? (float) (colorOverride & 255) / 255.0F : 1.0F;
            red = Mth.clamp(red * lightMultiplier * shadow, 0.0F, 1.0F);
            green = Mth.clamp(green * lightMultiplier * shadow, 0.0F, 1.0F);
            blue = Mth.clamp(blue * lightMultiplier * shadow, 0.0F, 1.0F);
            consumer.putBulkData(pose, quad, red, green, blue, packedLight, packedOverlay);
        }
    }

    private static float legacyShadowFactor(PoseStack poseStack) {
        return legacyShadowFactor(poseStack.last(), Direction.UP);
    }

    private static float legacyShadowFactor(PoseStack.Pose pose, Direction direction) {
        Vector3f normal = direction.step();
        normal.mul(pose.normal());
        float brightness = (normal.y() + 0.7F) * 0.9F - Math.abs(normal.x()) * 0.1F + Math.abs(normal.z()) * 0.1F;
        return Math.max(0.45F, brightness);
    }

    private ObjRenderUtils() {
    }
}
