package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.CargoElevatorBlockEntity;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CargoElevatorRenderer implements BlockEntityRenderer<CargoElevatorBlockEntity> {
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/machines/elevator.png");

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
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(elevator, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);

        if (elevator.shouldRenderPlatform()) {
            double extension = elevator.getPrevExtension()
                    + (elevator.getExtension() - elevator.getPrevExtension()) * partialTick;
            ObjMachineModels.ELEVATOR_LEGACY.renderPart("Base", TEXTURE, poseStack, buffer, modelLight, packedOverlay);

            poseStack.pushPose();
            poseStack.translate(0.0D, extension, 0.0D);
            ObjMachineModels.ELEVATOR_LEGACY.renderPart("Platform", TEXTURE, poseStack, buffer, modelLight,
                    packedOverlay);
            for (int i = 0; i < extension + 1.0D; i++) {
                ObjMachineModels.ELEVATOR_LEGACY.renderPart("Piston", TEXTURE, poseStack, buffer, modelLight,
                        packedOverlay);
                poseStack.translate(0.0D, -1.0D, 0.0D);
            }
            poseStack.popPose();
        }

        poseStack.pushPose();
        for (int i = 0; i <= elevator.getHeight(); i++) {
            ObjMachineModels.ELEVATOR_LEGACY.renderPart("Guides", TEXTURE, poseStack, buffer, modelLight,
                    packedOverlay);
            poseStack.translate(0.0D, 1.0D, 0.0D);
        }
        poseStack.popPose();
        poseStack.popPose();
    }
}
