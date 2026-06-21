package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.IndustrialSteamTurbineBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjRenderContext;
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

    public IndustrialSteamTurbineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(IndustrialSteamTurbineBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(IndustrialSteamTurbineBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
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

        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay);
        context = context.withRenderMode(LegacyMachinePartRenderContexts.renderMode(definition.renderMode()));
        renderPlan(MODEL, plan, context, poseStack);

        poseStack.popPose();
    }

    static void renderPlan(LegacyWavefrontModel model, LegacyTileRenderPlans.IndustrialTurbinePlan plan,
            ObjRenderContext context, PoseStack poseStack) {
        model.renderPart("Turbine", context);
        renderRotatingPart(model, plan.gauge(), context, poseStack);
        renderRotatingPart(model, plan.flywheel(), context, poseStack);
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
            LegacyTileRenderPlans.RotatingModelPartPlan part, ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        model.renderPart(part.partName(), context);
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
