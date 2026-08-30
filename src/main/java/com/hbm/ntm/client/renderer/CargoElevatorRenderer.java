package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.blockentity.CargoElevatorBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class CargoElevatorRenderer implements BlockEntityRenderer<CargoElevatorBlockEntity> {
    public static final ResourceLocation TEXTURE = ObjMachineModels.ELEVATOR_TEXTURE;
    private static final LegacyWavefrontModel MODEL = ObjMachineModels.ELEVATOR_LEGACY;
    private static final LegacyWavefrontModel.SelectionHandle BASE =
            MODEL.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle PLATFORM =
            MODEL.prepareRenderOnlyInCallOrder("Platform");
    private static final LegacyWavefrontModel.SelectionHandle PISTON =
            MODEL.prepareRenderOnlyInCallOrder("Piston");
    private static final LegacyWavefrontModel.SelectionHandle GUIDES =
            MODEL.prepareRenderOnlyInCallOrder("Guides");

    public CargoElevatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(CargoElevatorBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(CargoElevatorBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(CargoElevatorBlockEntity elevator, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(elevator, getViewDistance())) {
            return;
        }
        int modelLight = LegacyRenderLighting.resolveBoundsLight(elevator, elevator.getRenderBoundingBox(),
                packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);

        double extension = elevator.getPrevExtension()
                + (elevator.getExtension() - elevator.getPrevExtension()) * partialTick;
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(elevator)) {
            if (elevator.shouldRenderPlatform()) {
                if (LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
                    renderPart(BASE, poseStack, buffer, modelLight, packedOverlay,
                            LegacyTexturedRenderMode.CUTOUT_CULL);
                }
                try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(elevator)) {
                    renderTranslatedPart(PLATFORM, 0.0D, extension, 0.0D, poseStack, buffer, packedLight,
                            packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
                    for (int i = 0; i < extension + 1.0D; i++) {
                        renderTranslatedPart(PISTON, 0.0D,
                                extension + LegacyTileRenderPlans.CARGO_ELEVATOR_PISTON_STEP_Y * i, 0.0D,
                                poseStack, buffer, packedLight, packedOverlay,
                                LegacyTexturedRenderMode.CUTOUT_CULL);
                    }
                }
            }

            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(elevator)) {
                for (int i = 0; i <= Math.max(0, elevator.getHeight()); i++) {
                    renderTranslatedPart(GUIDES, 0.0D, LegacyTileRenderPlans.CARGO_ELEVATOR_GUIDE_STEP_Y * i,
                            0.0D, poseStack, buffer, packedLight, packedOverlay,
                            LegacyTexturedRenderMode.CUTOUT_CULL);
                }
            }
        }
        poseStack.popPose();
    }

    private static void renderTranslatedPart(LegacyWavefrontModel.SelectionHandle handle,
            double translateX, double translateY, double translateZ, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        renderPart(handle, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    static void renderModelPart(String partName, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        LegacyWavefrontModel.SelectionHandle handle = handle(partName);
        if (handle != null) {
            MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, packedOverlay, handle);
            return;
        }
        MODEL.renderPart(partName, TEXTURE, poseStack, buffer, packedLight, packedOverlay);
    }

    private static LegacyWavefrontModel.SelectionHandle handle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Base" -> BASE;
            case "Platform" -> PLATFORM;
            case "Piston" -> PISTON;
            case "Guides" -> GUIDES;
            default -> null;
        };
    }

    private static void renderPart(LegacyWavefrontModel.SelectionHandle handle, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, packedOverlay, handle, renderMode);
    }
}
