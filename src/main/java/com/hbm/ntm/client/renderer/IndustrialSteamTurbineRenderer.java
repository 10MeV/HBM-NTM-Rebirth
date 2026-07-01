package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.IndustrialSteamTurbineBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class IndustrialSteamTurbineRenderer implements BlockEntityRenderer<IndustrialSteamTurbineBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_INDUSTRIAL_TURBINE;
    private static final LegacyWavefrontModel.SelectionHandle TURBINE =
            MODEL.prepareRenderOnlyInCallOrder("Turbine");
    private static final LegacyWavefrontModel.SelectionHandle GAUGE =
            MODEL.prepareRenderOnlyInCallOrder("Gauge");
    private static final LegacyWavefrontModel.SelectionHandle FLYWHEEL =
            MODEL.prepareRenderOnlyInCallOrder("Flywheel");

    public IndustrialSteamTurbineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(IndustrialSteamTurbineBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(IndustrialSteamTurbineBlockEntity blockEntity, float partialTick, PoseStack poseStack,
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
        LegacyTileRenderPlans.IndustrialTurbinePlan plan = LegacyTileRenderPlans.industrialTurbinePlan(
                gaugeDegrees(blockEntity.getInputTank().getTankType()),
                blockEntity.getLastRotor(), blockEntity.getRotor(), partialTick);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        LegacyTexturedRenderMode renderMode = LegacyMachinePartRenderContexts.renderMode(definition.renderMode());
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            MODEL.renderOnlyInCallOrder(MODEL.textureLocation(), poseStack, buffer, modelLight, packedOverlay,
                    TURBINE, renderMode);
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                renderRotatingPart(MODEL, plan.gauge(), GAUGE, poseStack, buffer, modelLight, packedOverlay,
                        renderMode);
                renderRotatingPart(MODEL, plan.flywheel(), FLYWHEEL, poseStack, buffer, modelLight,
                        packedOverlay, renderMode);
            }
        }

        poseStack.popPose();
    }

    static void renderPlan(LegacyWavefrontModel model, LegacyTileRenderPlans.IndustrialTurbinePlan plan,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        model.renderOnlyInCallOrder(model.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                TURBINE, renderMode);
        renderRotatingPart(model, plan.gauge(), GAUGE, poseStack, buffer, packedLight, packedOverlay, renderMode);
        renderRotatingPart(model, plan.flywheel(), FLYWHEEL, poseStack, buffer, packedLight, packedOverlay, renderMode);
    }

    private static double gaugeDegrees(FluidType type) {
        if (type == HbmFluids.HOTSTEAM) {
            return LegacyTileRenderPlans.INDUSTRIAL_TURBINE_GAUGE_HOTSTEAM;
        }
        if (type == HbmFluids.SUPERHOTSTEAM) {
            return LegacyTileRenderPlans.INDUSTRIAL_TURBINE_GAUGE_SUPERHOTSTEAM;
        }
        if (type == HbmFluids.ULTRAHOTSTEAM) {
            return LegacyTileRenderPlans.INDUSTRIAL_TURBINE_GAUGE_ULTRAHOTSTEAM;
        }
        return LegacyTileRenderPlans.INDUSTRIAL_TURBINE_GAUGE_STEAM;
    }

    private static void renderRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, LegacyWavefrontModel.SelectionHandle handle,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        model.renderOnlyInCallOrder(model.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                handle, renderMode);
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
