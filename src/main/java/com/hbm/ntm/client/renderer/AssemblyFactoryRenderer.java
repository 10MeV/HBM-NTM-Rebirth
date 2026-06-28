package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.AssemblyFactoryBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class AssemblyFactoryRenderer implements BlockEntityRenderer<AssemblyFactoryBlockEntity> {
    private static final int MODULES = 4;
    private static final LegacyWavefrontModel MODEL = ObjMachineModels.ASSEMBLY_FACTORY;
    private static final LegacyWavefrontModel.SelectionHandle BASE =
            MODEL.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle FRAME =
            MODEL.prepareRenderOnlyInCallOrder("Frame");
    private static final LegacyWavefrontModel.SelectionHandle SLIDER_1 =
            MODEL.prepareRenderOnlyInCallOrder("Slider1");
    private static final LegacyWavefrontModel.SelectionHandle SLIDER_2 =
            MODEL.prepareRenderOnlyInCallOrder("Slider2");
    private static final LegacyWavefrontModel.SelectionHandle SLIDER_3 =
            MODEL.prepareRenderOnlyInCallOrder("Slider3");
    private static final LegacyWavefrontModel.SelectionHandle SLIDER_4 =
            MODEL.prepareRenderOnlyInCallOrder("Slider4");
    private static final LegacyWavefrontModel.SelectionHandle ARM_LOWER_1 =
            MODEL.prepareRenderOnlyInCallOrder("ArmLower1");
    private static final LegacyWavefrontModel.SelectionHandle ARM_LOWER_2 =
            MODEL.prepareRenderOnlyInCallOrder("ArmLower2");
    private static final LegacyWavefrontModel.SelectionHandle ARM_LOWER_3 =
            MODEL.prepareRenderOnlyInCallOrder("ArmLower3");
    private static final LegacyWavefrontModel.SelectionHandle ARM_LOWER_4 =
            MODEL.prepareRenderOnlyInCallOrder("ArmLower4");
    private static final LegacyWavefrontModel.SelectionHandle ARM_UPPER_1 =
            MODEL.prepareRenderOnlyInCallOrder("ArmUpper1");
    private static final LegacyWavefrontModel.SelectionHandle ARM_UPPER_2 =
            MODEL.prepareRenderOnlyInCallOrder("ArmUpper2");
    private static final LegacyWavefrontModel.SelectionHandle ARM_UPPER_3 =
            MODEL.prepareRenderOnlyInCallOrder("ArmUpper3");
    private static final LegacyWavefrontModel.SelectionHandle ARM_UPPER_4 =
            MODEL.prepareRenderOnlyInCallOrder("ArmUpper4");
    private static final LegacyWavefrontModel.SelectionHandle HEAD_1 =
            MODEL.prepareRenderOnlyInCallOrder("Head1");
    private static final LegacyWavefrontModel.SelectionHandle HEAD_2 =
            MODEL.prepareRenderOnlyInCallOrder("Head2");
    private static final LegacyWavefrontModel.SelectionHandle HEAD_3 =
            MODEL.prepareRenderOnlyInCallOrder("Head3");
    private static final LegacyWavefrontModel.SelectionHandle HEAD_4 =
            MODEL.prepareRenderOnlyInCallOrder("Head4");
    private static final LegacyWavefrontModel.SelectionHandle STRIKER_1 =
            MODEL.prepareRenderOnlyInCallOrder("Striker1");
    private static final LegacyWavefrontModel.SelectionHandle STRIKER_2 =
            MODEL.prepareRenderOnlyInCallOrder("Striker2");
    private static final LegacyWavefrontModel.SelectionHandle STRIKER_3 =
            MODEL.prepareRenderOnlyInCallOrder("Striker3");
    private static final LegacyWavefrontModel.SelectionHandle STRIKER_4 =
            MODEL.prepareRenderOnlyInCallOrder("Striker4");
    private static final LegacyWavefrontModel.SelectionHandle BLADE_2 =
            MODEL.prepareRenderOnlyInCallOrder("Blade2");
    private static final LegacyWavefrontModel.SelectionHandle BLADE_4 =
            MODEL.prepareRenderOnlyInCallOrder("Blade4");

    public AssemblyFactoryRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(AssemblyFactoryBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(AssemblyFactoryBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }
        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);

        try (LegacyRenderLighting.ModelViewSamplingScope ignored =
                LegacyRenderLighting.pushModelViewSampling(blockEntity, poseStack.last().pose())) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
            Vec3 translation = definition.modelTranslation(state);
            poseStack.translate(translation.x, translation.y, translation.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));
            ResourceLocation texture = definition.textureLocation();

            renderModelPart("Base", texture, poseStack, buffer, modelLight, packedOverlay);
            if (blockEntity.shouldRenderFrame()) {
                renderModelPart("Frame", texture, poseStack, buffer, modelLight, packedOverlay);
            }

            double slide1 = blockEntity.getAnimation(0).getSlider(partialTick);
            double slide2 = blockEntity.getAnimation(1).getSlider(partialTick);
            double[] arm1 = blockEntity.getAnimation(0).striker.getPositions(partialTick);
            double[] arm2 = blockEntity.getAnimation(0).saw.getPositions(partialTick);
            double[] arm3 = blockEntity.getAnimation(1).striker.getPositions(partialTick);
            double[] arm4 = blockEntity.getAnimation(1).saw.getPositions(partialTick);

            LegacyTileRenderPlans.AssemblyFactoryPlan plan = LegacyTileRenderPlans.assemblyFactoryPlan(
                    slide1, slide2, arm1, arm2, arm3, arm4);
            for (LegacyTileRenderPlans.AssemblyArmPlan slider : plan.sliders()) {
                renderArmPlan(poseStack, texture, buffer, modelLight, packedOverlay, slider);
            }

            if (LegacyRecipeIconRenderer.shouldRender(blockEntity)) {
                renderRecipeIcons(blockEntity, poseStack, buffer, packedLight);
                renderSparks(blockEntity, partialTick, poseStack, buffer, packedLight, packedOverlay,
                        slide1, slide2, arm2, arm4);
            }

            poseStack.popPose();
        }
    }

    private static void renderRecipeIcons(AssemblyFactoryBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        for (int module = 0; module < MODULES; module++) {
            poseStack.pushPose();
            poseStack.translate(1.5D - module, 0.0D, 0.0D);
            LegacyRecipeIconRenderer.renderInLegacyMachineSpace(blockEntity.getSelectedRecipeDefinition(module),
                    blockEntity.getLevel(), poseStack, buffer, packedLight);
            poseStack.popPose();
        }
    }

    private static void renderArmPlan(PoseStack poseStack, ResourceLocation texture, MultiBufferSource buffer,
            int packedLight, int packedOverlay,
            LegacyTileRenderPlans.AssemblyArmPlan arm) {
        poseStack.pushPose();
        LegacyTileRenderPlans.TranslatedModelPartPlan slider = arm.slider();
        if (slider != null && slider.active()) {
            poseStack.translate(slider.translateX(), slider.translateY(), slider.translateZ());
            renderModelPart(slider.partName(), texture, poseStack, buffer, packedLight, packedOverlay);
        }
        for (LegacyTileRenderPlans.PivotedModelPartPlan part : arm.rotations()) {
            applyPivot(poseStack, part);
            renderModelPart(part.partName(), texture, poseStack, buffer, packedLight, packedOverlay);
        }
        LegacyTileRenderPlans.TranslatedModelPartPlan tool = arm.tool();
        if (tool != null && tool.active()) {
            poseStack.translate(tool.translateX(), tool.translateY(), tool.translateZ());
            renderModelPart(tool.partName(), texture, poseStack, buffer, packedLight, packedOverlay);
        }
        LegacyTileRenderPlans.PivotedModelPartPlan blade = arm.blade();
        if (blade != null) {
            applyPivot(poseStack, blade);
            renderModelPart(blade.partName(), texture, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void applyPivot(PoseStack poseStack, LegacyTileRenderPlans.PivotedModelPartPlan part) {
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        poseStack.mulPose(Axis.XP.rotationDegrees((float) part.angleDegrees()));
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
    }

    private static void renderModelPart(String partName, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyWavefrontModel.SelectionHandle handle = handle(partName);
        if (handle != null) {
            MODEL.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle);
            return;
        }
        MODEL.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay);
    }

    private static LegacyWavefrontModel.SelectionHandle handle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Base" -> BASE;
            case "Frame" -> FRAME;
            case "Slider1" -> SLIDER_1;
            case "Slider2" -> SLIDER_2;
            case "Slider3" -> SLIDER_3;
            case "Slider4" -> SLIDER_4;
            case "ArmLower1" -> ARM_LOWER_1;
            case "ArmLower2" -> ARM_LOWER_2;
            case "ArmLower3" -> ARM_LOWER_3;
            case "ArmLower4" -> ARM_LOWER_4;
            case "ArmUpper1" -> ARM_UPPER_1;
            case "ArmUpper2" -> ARM_UPPER_2;
            case "ArmUpper3" -> ARM_UPPER_3;
            case "ArmUpper4" -> ARM_UPPER_4;
            case "Head1" -> HEAD_1;
            case "Head2" -> HEAD_2;
            case "Head3" -> HEAD_3;
            case "Head4" -> HEAD_4;
            case "Striker1" -> STRIKER_1;
            case "Striker2" -> STRIKER_2;
            case "Striker3" -> STRIKER_3;
            case "Striker4" -> STRIKER_4;
            case "Blade2" -> BLADE_2;
            case "Blade4" -> BLADE_4;
            default -> null;
        };
    }

    private static void renderSparks(AssemblyFactoryBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay,
            double slide1, double slide2, double[] arm2, double[] arm4) {
        LegacyTileRenderPlans.AssemblySparkRenderPlan plan = LegacyTileRenderPlans.assemblySparkPlan(
                blockEntity.getLevel().getGameTime(), partialTick, slide1, slide2, arm2[2], arm2[3],
                arm4[2], arm4[3]);
        LegacyAssemblySparkRenderer.renderPlan(ObjMachineModels.ASSEMBLY_FACTORY_SPARKS_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, plan);
    }
}
