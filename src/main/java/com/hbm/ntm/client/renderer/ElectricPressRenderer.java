package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.ElectricPressBlockEntity;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ElectricPressRenderer implements BlockEntityRenderer<ElectricPressBlockEntity> {
    public ElectricPressRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ElectricPressBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(ElectricPressBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(ElectricPressBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        float yRotation = 270.0F - facing.toYRot();
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                double progress = normalizedPress(blockEntity.getInterpolatedPress(partialTick),
                        ElectricPressBlockEntity.MAX_PRESS);
                double headTranslateY = LegacyTileRenderPlans.ELECTRIC_PRESS_HEAD_BASE_Y
                        + (1.0D - progress) * LegacyTileRenderPlans.ELECTRIC_PRESS_HEAD_TRAVEL;
                poseStack.pushPose();
                poseStack.translate(0.5D, headTranslateY, 0.5D);
                poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
                ObjMachineModels.EPRESS_HEAD.renderAll(ObjMachineModels.EPRESS_HEAD_TEXTURE,
                        poseStack, buffer, modelLight, packedOverlay);
                poseStack.popPose();
            }
        }

        renderInputItem(blockEntity, yRotation, poseStack, buffer, packedLight);
    }

    private static double normalizedPress(double press, double maxPress) {
        if (maxPress <= 0.0D) {
            return 0.0D;
        }
        return Math.max(0.0D, Math.min(1.0D, press / maxPress));
    }

    private static void renderInputItem(ElectricPressBlockEntity blockEntity, float yRotation, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        ItemStack stack = blockEntity.getRenderInputStack();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.translate(1.0D, 1.0D - 0.0625D * 165.0D / 100.0D, 0.0D);
        poseStack.translate(-1.0D, -1.15D, 0.0D);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                blockEntity.getLevel(),
                0);
        poseStack.popPose();
    }
}
