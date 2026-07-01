package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.SteamEngineBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);
        float rotor = blockEntity.getRotor();
        float previousRotor = blockEntity.getLastRotor();
        LegacyTileRenderPlans.SteamEnginePlan plan =
                LegacyTileRenderPlans.steamEnginePlan(previousRotor, rotor, partialTick);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                renderRotatingPart(MODEL, plan.flywheel(), FLYWHEEL, poseStack, buffer, modelLight, packedOverlay);
                renderRotatingPart(MODEL, plan.shaft(), SHAFT, poseStack, buffer, modelLight, packedOverlay);
                renderTransmission(MODEL, plan.transmission(), poseStack, buffer, modelLight, packedOverlay);
                renderTranslatedPart(MODEL, plan.piston(), PISTON, poseStack, buffer, modelLight, packedOverlay);
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
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        model.renderOnlyInCallOrder(poseStack, buffer, packedLight, packedOverlay, handle);
        poseStack.popPose();
    }

    private static void renderTransmission(LegacyWavefrontModel model,
            LegacyTileRenderPlans.SteamEngineTransmissionPlan transmission, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(transmission.translateX(), transmission.translateY(), 0.0D);
        poseStack.translate(transmission.pivotX(), transmission.pivotY(), 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) -transmission.angleDegrees()));
        poseStack.translate(-transmission.pivotX(), -transmission.pivotY(), 0.0D);
        model.renderOnlyInCallOrder(poseStack, buffer, packedLight, packedOverlay, TRANSMISSION);
        poseStack.popPose();
    }

    private static void renderTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, LegacyWavefrontModel.SelectionHandle handle,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!part.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        model.renderOnlyInCallOrder(poseStack, buffer, packedLight, packedOverlay, handle);
        poseStack.popPose();
    }

    private static void rotate(PoseStack poseStack, float axisX, float axisY, float axisZ, double degrees) {
        if (axisX != 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) (degrees * axisX)));
        }
        if (axisY != 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (degrees * axisY)));
        }
        if (axisZ != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (degrees * axisZ)));
        }
    }

}
