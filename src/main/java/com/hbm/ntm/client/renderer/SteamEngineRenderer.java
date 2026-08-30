package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.SteamEngineBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SteamEngineRenderer implements BlockEntityRenderer<SteamEngineBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_STEAM_ENGINE;
    private static final LegacyWavefrontModel.SelectionHandle BASE =
            MODEL.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle FLYWHEEL =
            MODEL.prepareRenderOnlyInCallOrder("Flywheel");
    private static final LegacyWavefrontModel.SelectionHandle SHAFT =
            MODEL.prepareRenderOnlyInCallOrder("Shaft");
    private static final LegacyWavefrontModel.SelectionHandle TRANSMISSION =
            MODEL.prepareRenderOnlyInCallOrder("Transmission");
    private static final LegacyWavefrontModel.SelectionHandle PISTON =
            MODEL.prepareRenderOnlyInCallOrder("Piston");

    public SteamEngineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(SteamEngineBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(SteamEngineBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(SteamEngineBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        float rotor = blockEntity.getRotor();
        float previousRotor = blockEntity.getLastRotor();
        double rotorDegrees = previousRotor + (rotor - previousRotor) * partialTick;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, definition.yRotation(state));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        LegacyPoseRotations.rotateYDegrees(poseStack, definition.postModelYRotation(state));

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                renderRotatingPart(MODEL, FLYWHEEL,
                        LegacyTileRenderPlans.STEAM_ENGINE_FLYWHEEL_PIVOT_X,
                        LegacyTileRenderPlans.STEAM_ENGINE_FLYWHEEL_PIVOT_Y, 0.0D,
                        0.0F, 0.0F, -1.0F, rotorDegrees, poseStack, buffer, packedLight, packedOverlay);
                renderRotatingPart(MODEL, SHAFT,
                        0.0D, LegacyTileRenderPlans.STEAM_ENGINE_SHAFT_PIVOT_Y,
                        LegacyTileRenderPlans.STEAM_ENGINE_SHAFT_PIVOT_Z,
                        1.0F, 0.0F, 0.0F, rotorDegrees * 2.0D, poseStack, buffer, packedLight,
                        packedOverlay);

                double radians = rotorDegrees * LegacyTileRenderPlans.DEG_TO_RAD;
                double sin = Math.sin(radians) * LegacyTileRenderPlans.STEAM_ENGINE_CRANK_RADIUS
                        + LegacyTileRenderPlans.STEAM_ENGINE_CRANK_SIN_OFFSET;
                double cos = Math.cos(radians) * LegacyTileRenderPlans.STEAM_ENGINE_CRANK_RADIUS;
                double transmissionAngle = Math.acos(cos / LegacyTileRenderPlans.STEAM_ENGINE_ROD_LENGTH)
                        * LegacyTileRenderPlans.RAD_TO_DEG - 90.0D;
                renderTransmission(MODEL, sin, cos, transmissionAngle, poseStack, buffer, packedLight, packedOverlay);

                double cath = Math.sqrt(LegacyTileRenderPlans.STEAM_ENGINE_PISTON_CATH_SQUARED
                        - (cos * cos) / 2.0D);
                renderTranslatedPart(MODEL, LegacyTileRenderPlans.STEAM_ENGINE_ROD_LENGTH - cath + sin,
                        0.0D, 0.0D, PISTON, poseStack, buffer, packedLight, packedOverlay);
            }
        }

        poseStack.popPose();
    }

    static void renderPlan(LegacyWavefrontModel model, LegacyTileRenderPlans.SteamEnginePlan plan,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        model.renderOnlyInCallOrder(poseStack, buffer, packedLight, packedOverlay, BASE);
        renderRotatingPart(model, plan.flywheel(), FLYWHEEL, poseStack, buffer, packedLight, packedOverlay);
        renderRotatingPart(model, plan.shaft(), SHAFT, poseStack, buffer, packedLight, packedOverlay);
        renderTransmission(model, plan.transmission(), poseStack, buffer, packedLight, packedOverlay);
        renderTranslatedPart(model, plan.piston(), PISTON, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, LegacyWavefrontModel.SelectionHandle handle,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderRotatingPart(model, handle, part.pivotX(), part.pivotY(), part.pivotZ(),
                part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees(), poseStack, buffer, packedLight,
                packedOverlay);
    }

    private static void renderRotatingPart(LegacyWavefrontModel model, LegacyWavefrontModel.SelectionHandle handle,
            double pivotX, double pivotY, double pivotZ, float axisX, float axisY, float axisZ,
            double angleDegrees, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(pivotX, pivotY, pivotZ);
        rotate(poseStack, axisX, axisY, axisZ, angleDegrees);
        poseStack.translate(-pivotX, -pivotY, -pivotZ);
        model.renderOnlyInCallOrder(poseStack, buffer, packedLight, packedOverlay, handle);
        poseStack.popPose();
    }

    private static void renderTransmission(LegacyWavefrontModel model,
            LegacyTileRenderPlans.SteamEngineTransmissionPlan transmission, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderTransmission(model, transmission.translateX(), transmission.translateY(), transmission.angleDegrees(),
                poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderTransmission(LegacyWavefrontModel model, double translateX, double translateY,
            double angleDegrees, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, 0.0D);
        poseStack.translate(LegacyTileRenderPlans.STEAM_ENGINE_TRANSMISSION_PIVOT_X,
                LegacyTileRenderPlans.STEAM_ENGINE_TRANSMISSION_PIVOT_Y, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) -angleDegrees);
        poseStack.translate(-LegacyTileRenderPlans.STEAM_ENGINE_TRANSMISSION_PIVOT_X,
                -LegacyTileRenderPlans.STEAM_ENGINE_TRANSMISSION_PIVOT_Y, 0.0D);
        model.renderOnlyInCallOrder(poseStack, buffer, packedLight, packedOverlay, TRANSMISSION);
        poseStack.popPose();
    }

    private static void renderTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, LegacyWavefrontModel.SelectionHandle handle,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!part.active()) {
            return;
        }
        renderTranslatedPart(model, part.translateX(), part.translateY(), part.translateZ(), handle, poseStack,
                buffer, packedLight, packedOverlay);
    }

    private static void renderTranslatedPart(LegacyWavefrontModel model, double translateX, double translateY,
            double translateZ, LegacyWavefrontModel.SelectionHandle handle, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        model.renderOnlyInCallOrder(poseStack, buffer, packedLight, packedOverlay, handle);
        poseStack.popPose();
    }

    private static void rotate(PoseStack poseStack, float axisX, float axisY, float axisZ, double degrees) {
        if (axisX != 0.0F) {
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) (degrees * axisX));
        }
        if (axisY != 0.0F) {
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) (degrees * axisY));
        }
        if (axisZ != 0.0F) {
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) (degrees * axisZ));
        }
    }

}
