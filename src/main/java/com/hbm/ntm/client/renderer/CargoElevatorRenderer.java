package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.CargoElevatorBlockEntity;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CargoElevatorRenderer implements BlockEntityRenderer<CargoElevatorBlockEntity> {
    public static final ResourceLocation TEXTURE = ObjMachineModels.ELEVATOR_TEXTURE;

    public CargoElevatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(CargoElevatorBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void render(CargoElevatorBlockEntity elevator, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int modelLight = LegacyRenderLighting.resolveBoundsLight(elevator, elevator.getRenderBoundingBox(),
                packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);

        LegacyTileRenderPlans.CargoElevatorPlan plan = LegacyTileRenderPlans.cargoElevatorPlan(
                elevator.shouldRenderPlatform(), elevator.getPrevExtension(), elevator.getExtension(),
                partialTick, elevator.getHeight());
        if (plan.renderPlatform()) {
            ObjMachineModels.ELEVATOR_LEGACY.renderPart("Base", TEXTURE, poseStack, buffer, modelLight, packedOverlay);
            renderTranslatedParts(plan.platformParts(), poseStack, buffer, modelLight, packedOverlay);
        }

        renderTranslatedParts(plan.guides(), poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderTranslatedParts(
            Iterable<LegacyTileRenderPlans.TranslatedModelPartPlan> parts, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        for (LegacyTileRenderPlans.TranslatedModelPartPlan part : parts) {
            if (!part.active()) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
            ObjMachineModels.ELEVATOR_LEGACY.renderPart(part.partName(), TEXTURE, poseStack, buffer,
                    packedLight, packedOverlay);
            poseStack.popPose();
        }
    }
}
