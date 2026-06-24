package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.SteamEngineBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjRenderContext;
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
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(SteamEngineBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
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

        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay);
        renderPlan(MODEL, plan, context, poseStack);

        poseStack.popPose();
    }

    static void renderPlan(LegacyWavefrontModel model, LegacyTileRenderPlans.SteamEnginePlan plan,
            ObjRenderContext context, PoseStack poseStack) {
        model.renderOnlyInCallOrder(context, BASE);
        renderRotatingPart(model, plan.flywheel(), FLYWHEEL, context, poseStack);
        renderRotatingPart(model, plan.shaft(), SHAFT, context, poseStack);
        renderTransmission(model, plan.transmission(), context, poseStack);
        renderTranslatedPart(model, plan.piston(), PISTON, context, poseStack);
    }

    private static void renderRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, LegacyWavefrontModel.SelectionHandle handle,
            ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        model.renderOnlyInCallOrder(context, handle);
        poseStack.popPose();
    }

    private static void renderTransmission(LegacyWavefrontModel model,
            LegacyTileRenderPlans.SteamEngineTransmissionPlan transmission, ObjRenderContext context,
            PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(transmission.translateX(), transmission.translateY(), 0.0D);
        poseStack.translate(transmission.pivotX(), transmission.pivotY(), 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) -transmission.angleDegrees()));
        poseStack.translate(-transmission.pivotX(), -transmission.pivotY(), 0.0D);
        model.renderOnlyInCallOrder(context, TRANSMISSION);
        poseStack.popPose();
    }

    private static void renderTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, LegacyWavefrontModel.SelectionHandle handle,
            ObjRenderContext context, PoseStack poseStack) {
        if (!part.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        model.renderOnlyInCallOrder(context, handle);
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
