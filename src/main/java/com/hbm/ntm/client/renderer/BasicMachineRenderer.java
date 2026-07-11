package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.blockentity.BasicMachineBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.shader.HbmShaderCompatibilityDetector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class BasicMachineRenderer implements BlockEntityRenderer<BasicMachineBlockEntity> {
    public BasicMachineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(BasicMachineBlockEntity blockEntity) {
        return HbmShaderCompatibilityDetector.shouldRenderBlockEntityOffScreen();
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(BasicMachineBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(BasicMachineBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        ItemStack stack = blockEntity.getRenderStack();
        double progress = normalizedPress(blockEntity.getInterpolatedPress(partialTick),
                BasicMachineBlockEntity.MAX_PRESS);
        double headTranslateY = (1.0D - progress) * LegacyTileRenderPlans.BASIC_PRESS_HEAD_TRAVEL;

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity);
                LegacyRenderLighting.ModelViewSamplingScope ignored =
                LegacyRenderLighting.pushModelViewSampling(blockEntity, poseStack.last().pose())) {
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                poseStack.pushPose();
                poseStack.translate(0.5D, 0.0D, 0.5D);
                poseStack.scale(0.99F, 1.0F, 0.99F);
                poseStack.translate(0.0D, headTranslateY, 0.0D);
                ObjMachineModels.PRESS_HEAD_LEGACY.renderAll(ObjMachineModels.PRESS_HEAD_TEXTURE, poseStack, buffer,
                        modelLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
                poseStack.popPose();
            }
        }

        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        applyBasicPressItemTransform(poseStack);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                blockEntity.getLevel(),
                0
        );
        poseStack.popPose();
    }

    private static double normalizedPress(double press, double maxPress) {
        if (maxPress <= 0.0D) {
            return 0.0D;
        }
        return Math.max(0.0D, Math.min(1.0D, press / maxPress));
    }

    private static void applyBasicPressItemTransform(PoseStack poseStack) {
        poseStack.translate(LegacyTileRenderPlans.BASIC_PRESS_ITEM_TRANSLATE_X,
                LegacyTileRenderPlans.BASIC_PRESS_ITEM_TRANSLATE_Y,
                LegacyTileRenderPlans.BASIC_PRESS_ITEM_TRANSLATE_Z);
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) LegacyTileRenderPlans.BASIC_PRESS_ITEM_ROTATION_Y);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) LegacyTileRenderPlans.BASIC_PRESS_ITEM_ROTATION_X);
        float scale = (float) LegacyTileRenderPlans.BASIC_PRESS_ITEM_SCALE;
        poseStack.scale(scale, scale, scale);
    }
}
