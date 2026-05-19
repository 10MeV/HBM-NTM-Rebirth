package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        int color = blockColors.getColor(state, (BlockAndTintGetter) null, (BlockPos) null, 0);
        float red = Mth.clamp(((float) (color >> 16 & 255) / 255.0F) * lightMultiplier, 0.0F, 1.0F);
        float green = Mth.clamp(((float) (color >> 8 & 255) / 255.0F) * lightMultiplier, 0.0F, 1.0F);
        float blue = Mth.clamp(((float) (color & 255) / 255.0F) * lightMultiplier, 0.0F, 1.0F);
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
        VertexConsumer consumer = buffer.getBuffer(renderType);
        BlockState dummyState = Blocks.AIR.defaultBlockState();
        RandomSource random = RandomSource.create(42L);

        for (Direction direction : DIRECTIONS) {
            renderQuadList(poseStack.last(), consumer, model.getQuads(dummyState, direction, random), packedLight, packedOverlay, lightMultiplier);
        }
        renderQuadList(poseStack.last(), consumer, model.getQuads(dummyState, null, random), packedLight, packedOverlay, lightMultiplier);
    }

    private static void renderQuadList(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            List<BakedQuad> quads,
            int packedLight,
            int packedOverlay,
            float lightMultiplier) {
        for (BakedQuad quad : quads) {
            float red = Mth.clamp((quad.isTinted() ? 1.0F : 1.0F) * lightMultiplier, 0.0F, 1.0F);
            float green = Mth.clamp((quad.isTinted() ? 1.0F : 1.0F) * lightMultiplier, 0.0F, 1.0F);
            float blue = Mth.clamp((quad.isTinted() ? 1.0F : 1.0F) * lightMultiplier, 0.0F, 1.0F);
            consumer.putBulkData(pose, quad, red, green, blue, packedLight, packedOverlay);
        }
    }

    private ObjRenderUtils() {
    }
}
