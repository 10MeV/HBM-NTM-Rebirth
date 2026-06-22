package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.AssemblyFactoryBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class AssemblyFactoryRenderer implements BlockEntityRenderer<AssemblyFactoryBlockEntity> {
    private static final int MODULES = 4;
    private static final LegacyWavefrontModel MODEL = ObjMachineModels.ASSEMBLY_FACTORY;

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

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        MODEL.renderPart("Base", definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
        if (blockEntity.shouldRenderFrame()) {
            MODEL.renderPart("Frame", definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
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
            renderArmPlan(poseStack, buffer, modelLight, packedOverlay, slider);
        }

        if (LegacyRecipeIconRenderer.shouldRender(blockEntity)) {
            renderRecipeIcons(blockEntity, poseStack, buffer, packedLight);
            renderSparks(blockEntity, partialTick, poseStack, buffer, state, packedLight, packedOverlay,
                    slide1, slide2, arm2, arm4);
        }

        poseStack.popPose();
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

    private static void renderArmPlan(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay,
            LegacyTileRenderPlans.AssemblyArmPlan arm) {
        poseStack.pushPose();
        LegacyTileRenderPlans.TranslatedModelPartPlan slider = arm.slider();
        if (slider != null && slider.active()) {
            poseStack.translate(slider.translateX(), slider.translateY(), slider.translateZ());
            MODEL.renderPart(slider.partName(), poseStack, buffer, light, overlay);
        }
        for (LegacyTileRenderPlans.PivotedModelPartPlan part : arm.rotations()) {
            applyPivot(poseStack, part);
            MODEL.renderPart(part.partName(), poseStack, buffer, light, overlay);
        }
        LegacyTileRenderPlans.TranslatedModelPartPlan tool = arm.tool();
        if (tool != null && tool.active()) {
            poseStack.translate(tool.translateX(), tool.translateY(), tool.translateZ());
            MODEL.renderPart(tool.partName(), poseStack, buffer, light, overlay);
        }
        LegacyTileRenderPlans.PivotedModelPartPlan blade = arm.blade();
        if (blade != null) {
            applyPivot(poseStack, blade);
            MODEL.renderPart(blade.partName(), poseStack, buffer, light, overlay);
        }
        poseStack.popPose();
    }

    private static void applyPivot(PoseStack poseStack, LegacyTileRenderPlans.PivotedModelPartPlan part) {
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        poseStack.mulPose(Axis.XP.rotationDegrees((float) part.angleDegrees()));
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
    }

    private static void renderSparks(AssemblyFactoryBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, BlockState state, int packedLight, int packedOverlay,
            double slide1, double slide2, double[] arm2, double[] arm4) {
        LegacyTileRenderPlans.AssemblySparkRenderPlan plan = LegacyTileRenderPlans.assemblySparkPlan(
                blockEntity.getLevel().getGameTime(), partialTick, slide1, slide2, arm2[2], arm2[3],
                arm4[2], arm4[3]);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay);
        LegacyAssemblySparkRenderer.renderPlan(ObjMachineModels.ASSEMBLY_FACTORY_SPARKS_TEXTURE, context, plan);
    }
}
