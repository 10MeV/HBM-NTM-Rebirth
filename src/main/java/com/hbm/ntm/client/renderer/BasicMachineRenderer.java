package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.BasicMachineBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.shader.HbmShaderCompatibilityDetector;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

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
    public void render(BasicMachineBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        ItemStack stack = blockEntity.getRenderStack();
        LegacyTileRenderPlans.BasicPressPlan plan = LegacyTileRenderPlans.basicPressPlan(!stack.isEmpty(),
                blockEntity.getInterpolatedPress(partialTick), BasicMachineBlockEntity.MAX_PRESS);

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity);
                LegacyRenderLighting.ModelViewSamplingScope ignored =
                LegacyRenderLighting.pushModelViewSampling(blockEntity, poseStack.last().pose())) {
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                poseStack.pushPose();
                LegacyTileRenderPlans.TranslatedModelPartPlan head = plan.head();
                poseStack.translate(0.5D, 0.0D, 0.5D);
                poseStack.scale(0.99F, 1.0F, 0.99F);
                poseStack.translate(head.translateX(), head.translateY(), head.translateZ());
                ObjMachineModels.PRESS_HEAD_LEGACY.renderAll(ObjMachineModels.PRESS_HEAD_TEXTURE, poseStack, buffer,
                        modelLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
                poseStack.popPose();
            }
        }

        if (!plan.item().active()) {
            return;
        }

        poseStack.pushPose();
        applyItemTransform(plan.item(), poseStack);
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

    private static void applyItemTransform(LegacyTileRenderPlans.ItemTransformPlan plan, PoseStack poseStack) {
        poseStack.translate(plan.translateX(), plan.translateY(), plan.translateZ());
        poseStack.mulPose(Axis.YP.rotationDegrees((float) plan.rotateYDegrees()));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) plan.rotateXDegrees()));
        if (plan.rotateZDegrees() != 0.0D) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) plan.rotateZDegrees()));
        }
        poseStack.scale((float) plan.scale(), (float) plan.scale(), (float) plan.scale());
    }
}
