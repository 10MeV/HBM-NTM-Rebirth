package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class AssemblyMachineRenderer implements BlockEntityRenderer<AssemblyMachineBlockEntity> {
    static final LegacyWavefrontModel MODEL = ObjMachineModels.ASSEMBLY_MACHINE_LEGACY;

    public AssemblyMachineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(AssemblyMachineBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(AssemblyMachineBlockEntity assembler, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = assembler.getBlockState();
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(assembler, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F + blockstateModelYRotation(state)));

        MODEL.renderPart("Base", poseStack, buffer, modelLight, packedOverlay);
        if (assembler.shouldRenderFrame()) {
            MODEL.renderPart("Frame", poseStack, buffer, modelLight, packedOverlay);
        }

        poseStack.pushPose();
        LegacyTileRenderPlans.AssemblyMachinePlan plan = LegacyTileRenderPlans.assemblyMachinePlan(
                assembler.getRing(partialTick),
                assembler.getArm(0).getPositions(partialTick),
                assembler.getArm(1).getPositions(partialTick));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) plan.ringDegrees()));
        MODEL.renderPart("Ring", poseStack, buffer, modelLight, packedOverlay);
        for (LegacyTileRenderPlans.AssemblyArmPlan arm : plan.arms()) {
            renderArmPlan(poseStack, buffer, modelLight, packedOverlay, arm);
        }
        poseStack.popPose();

        if (LegacyRecipeIconRenderer.shouldRender(assembler)) {
            LegacyRecipeIconRenderer.renderInLegacyMachineSpace(assembler.getSelectedRecipeDefinition(),
                    assembler.getLevel(), poseStack, buffer, packedLight);
        }

        poseStack.popPose();
    }

    private static void renderArmPlan(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, LegacyTileRenderPlans.AssemblyArmPlan arm) {
        poseStack.pushPose();
        for (LegacyTileRenderPlans.PivotedModelPartPlan part : arm.rotations()) {
            applyPivot(poseStack, part);
            MODEL.renderPart(part.partName(), poseStack, buffer, packedLight, packedOverlay);
        }
        LegacyTileRenderPlans.TranslatedModelPartPlan tool = arm.tool();
        if (tool != null && tool.active()) {
            poseStack.translate(tool.translateX(), tool.translateY(), tool.translateZ());
            MODEL.renderPart(tool.partName(), poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void applyPivot(PoseStack poseStack, LegacyTileRenderPlans.PivotedModelPartPlan part) {
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        poseStack.mulPose(Axis.XP.rotationDegrees((float) part.angleDegrees()));
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
    }

    private static float blockstateModelYRotation(BlockState state) {
        if (!state.hasProperty(HorizontalMachineBlock.FACING)) {
            return 0.0F;
        }
        Direction facing = state.getValue(HorizontalMachineBlock.FACING);
        return facing.toYRot();
    }
}
