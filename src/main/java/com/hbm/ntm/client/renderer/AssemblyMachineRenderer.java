package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class AssemblyMachineRenderer implements BlockEntityRenderer<AssemblyMachineBlockEntity> {
    static final LegacyWavefrontModel MODEL = ObjMachineModels.ASSEMBLY_MACHINE_LEGACY;
    private static final LegacyWavefrontModel.SelectionHandle BASE =
            MODEL.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle FRAME =
            MODEL.prepareRenderOnlyInCallOrder("Frame");
    private static final LegacyWavefrontModel.SelectionHandle RING =
            MODEL.prepareRenderOnlyInCallOrder("Ring");
    private static final LegacyWavefrontModel.SelectionHandle RING_2 =
            MODEL.prepareRenderOnlyInCallOrder("Ring2");
    private static final LegacyWavefrontModel.SelectionHandle ARM_LOWER_1 =
            MODEL.prepareRenderOnlyInCallOrder("ArmLower1");
    private static final LegacyWavefrontModel.SelectionHandle ARM_LOWER_2 =
            MODEL.prepareRenderOnlyInCallOrder("ArmLower2");
    private static final LegacyWavefrontModel.SelectionHandle ARM_UPPER_1 =
            MODEL.prepareRenderOnlyInCallOrder("ArmUpper1");
    private static final LegacyWavefrontModel.SelectionHandle ARM_UPPER_2 =
            MODEL.prepareRenderOnlyInCallOrder("ArmUpper2");
    private static final LegacyWavefrontModel.SelectionHandle HEAD_1 =
            MODEL.prepareRenderOnlyInCallOrder("Head1");
    private static final LegacyWavefrontModel.SelectionHandle HEAD_2 =
            MODEL.prepareRenderOnlyInCallOrder("Head2");
    private static final LegacyWavefrontModel.SelectionHandle SPIKE_1 =
            MODEL.prepareRenderOnlyInCallOrder("Spike1");
    private static final LegacyWavefrontModel.SelectionHandle SPIKE_2 =
            MODEL.prepareRenderOnlyInCallOrder("Spike2");

    public AssemblyMachineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(AssemblyMachineBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(AssemblyMachineBlockEntity assembler, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(assembler, getViewDistance())) {
            return;
        }
        LegacyBlockEntityRenderCulling.recordMachineSubmission(assembler);
        BlockState state = assembler.getBlockState();
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(assembler, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F + blockstateModelYRotation(state)));

        renderModelPart("Base", poseStack, buffer, modelLight, packedOverlay);
        if (assembler.shouldRenderFrame()) {
            renderModelPart("Frame", poseStack, buffer, modelLight, packedOverlay);
        }

        poseStack.pushPose();
        LegacyTileRenderPlans.AssemblyMachinePlan plan = LegacyTileRenderPlans.assemblyMachinePlan(
                assembler.getRing(partialTick),
                assembler.getArm(0).getPositions(partialTick),
                assembler.getArm(1).getPositions(partialTick));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) plan.ringDegrees()));
        renderModelPart("Ring", poseStack, buffer, modelLight, packedOverlay);
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
            int packedOverlay,
            LegacyTileRenderPlans.AssemblyArmPlan arm) {
        poseStack.pushPose();
        for (LegacyTileRenderPlans.PivotedModelPartPlan part : arm.rotations()) {
            applyPivot(poseStack, part);
            renderModelPart(part.partName(), poseStack, buffer, packedLight, packedOverlay);
        }
        LegacyTileRenderPlans.TranslatedModelPartPlan tool = arm.tool();
        if (tool != null && tool.active()) {
            poseStack.translate(tool.translateX(), tool.translateY(), tool.translateZ());
            renderModelPart(tool.partName(), poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void applyPivot(PoseStack poseStack, LegacyTileRenderPlans.PivotedModelPartPlan part) {
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        poseStack.mulPose(Axis.XP.rotationDegrees((float) part.angleDegrees()));
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
    }

    private static void renderModelPart(String partName, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        renderModelPart(MODEL, partName, ObjMachineModels.ASSEMBLY_MACHINE_TEXTURE, poseStack, buffer,
                packedLight, packedOverlay);
    }

    private static void renderModelPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderModelPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    static void renderModelPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle = sameModel(model) ? handle(partName) : null;
        if (handle != null) {
            model.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle, renderMode);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, false, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
    }

    private static boolean sameModel(LegacyWavefrontModel model) {
        return model == MODEL || model.modelLocation().equals(MODEL.modelLocation());
    }

    private static LegacyWavefrontModel.SelectionHandle handle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Base" -> BASE;
            case "Frame" -> FRAME;
            case "Ring" -> RING;
            case "Ring2" -> RING_2;
            case "ArmLower1" -> ARM_LOWER_1;
            case "ArmLower2" -> ARM_LOWER_2;
            case "ArmUpper1" -> ARM_UPPER_1;
            case "ArmUpper2" -> ARM_UPPER_2;
            case "Head1" -> HEAD_1;
            case "Head2" -> HEAD_2;
            case "Spike1" -> SPIKE_1;
            case "Spike2" -> SPIKE_2;
            default -> null;
        };
    }

    private static float blockstateModelYRotation(BlockState state) {
        if (!state.hasProperty(HorizontalMachineBlock.FACING)) {
            return 0.0F;
        }
        Direction facing = state.getValue(HorizontalMachineBlock.FACING);
        return facing.toYRot();
    }
}
