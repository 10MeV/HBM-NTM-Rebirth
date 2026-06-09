package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.AssemblyFactoryBlockEntity;
import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class AssemblyFactoryRenderer implements BlockEntityRenderer<AssemblyFactoryBlockEntity> {
    private static final int MODULES = 4;
    private static final LegacyWavefrontModel MODEL = ObjMachineModels.ASSEMBLY_FACTORY;

    public AssemblyFactoryRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(AssemblyFactoryBlockEntity blockEntity) {
        return true;
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

        renderSlider1(poseStack, buffer, modelLight, packedOverlay, slide1, arm1);
        renderSlider2(poseStack, buffer, modelLight, packedOverlay, slide1, arm2);
        renderSlider3(poseStack, buffer, modelLight, packedOverlay, slide2, arm3);
        renderSlider4(poseStack, buffer, modelLight, packedOverlay, slide2, arm4);

        if (LegacyRecipeIconRenderer.shouldRender(blockEntity)) {
            renderRecipeIcons(blockEntity, poseStack, buffer, packedLight);
            renderSparks(blockEntity, partialTick, poseStack, buffer, state, packedOverlay, slide1, slide2, arm2, arm4);
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

    private static void renderSlider1(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay,
            double slide, double[] arm) {
        poseStack.pushPose();
        poseStack.translate(0.5D - slide, 0.0D, 0.0D);
        MODEL.renderPart("Slider1", poseStack, buffer, light, overlay);
        rotateXAround(poseStack, 0.0D, 1.625D, -0.9375D, -arm[0]);
        MODEL.renderPart("ArmLower1", poseStack, buffer, light, overlay);
        rotateXAround(poseStack, 0.0D, 2.375D, -0.9375D, -arm[1]);
        MODEL.renderPart("ArmUpper1", poseStack, buffer, light, overlay);
        rotateXAround(poseStack, 0.0D, 2.375D, -0.4375D, -arm[2]);
        MODEL.renderPart("Head1", poseStack, buffer, light, overlay);
        poseStack.translate(0.0D, arm[3], 0.0D);
        MODEL.renderPart("Striker1", poseStack, buffer, light, overlay);
        poseStack.popPose();
    }

    private static void renderSlider2(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay,
            double slide, double[] arm) {
        poseStack.pushPose();
        poseStack.translate(-0.5D + slide, 0.0D, 0.0D);
        MODEL.renderPart("Slider2", poseStack, buffer, light, overlay);
        rotateXAround(poseStack, 0.0D, 1.625D, 0.9375D, arm[0]);
        MODEL.renderPart("ArmLower2", poseStack, buffer, light, overlay);
        rotateXAround(poseStack, 0.0D, 2.375D, 0.9375D, arm[1]);
        MODEL.renderPart("ArmUpper2", poseStack, buffer, light, overlay);
        rotateXAround(poseStack, 0.0D, 2.375D, 0.4375D, arm[2]);
        MODEL.renderPart("Head2", poseStack, buffer, light, overlay);
        poseStack.translate(0.0D, arm[3], 0.0D);
        MODEL.renderPart("Striker2", poseStack, buffer, light, overlay);
        rotateXAround(poseStack, 0.0D, 1.625D, 0.3125D, -arm[4]);
        MODEL.renderPart("Blade2", poseStack, buffer, light, overlay);
        poseStack.popPose();
    }

    private static void renderSlider3(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay,
            double slide, double[] arm) {
        poseStack.pushPose();
        poseStack.translate(-0.5D + slide, 0.0D, 0.0D);
        MODEL.renderPart("Slider3", poseStack, buffer, light, overlay);
        rotateXAround(poseStack, 0.0D, 1.625D, 0.9375D, arm[0]);
        MODEL.renderPart("ArmLower3", poseStack, buffer, light, overlay);
        rotateXAround(poseStack, 0.0D, 2.375D, 0.9375D, arm[1]);
        MODEL.renderPart("ArmUpper3", poseStack, buffer, light, overlay);
        rotateXAround(poseStack, 0.0D, 2.375D, 0.4375D, arm[2]);
        MODEL.renderPart("Head3", poseStack, buffer, light, overlay);
        poseStack.translate(0.0D, arm[3], 0.0D);
        MODEL.renderPart("Striker3", poseStack, buffer, light, overlay);
        poseStack.popPose();
    }

    private static void renderSlider4(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay,
            double slide, double[] arm) {
        poseStack.pushPose();
        poseStack.translate(0.5D - slide, 0.0D, 0.0D);
        MODEL.renderPart("Slider4", poseStack, buffer, light, overlay);
        rotateXAround(poseStack, 0.0D, 1.625D, -0.9375D, -arm[0]);
        MODEL.renderPart("ArmLower4", poseStack, buffer, light, overlay);
        rotateXAround(poseStack, 0.0D, 2.375D, -0.9375D, -arm[1]);
        MODEL.renderPart("ArmUpper4", poseStack, buffer, light, overlay);
        rotateXAround(poseStack, 0.0D, 2.375D, -0.4375D, -arm[2]);
        MODEL.renderPart("Head4", poseStack, buffer, light, overlay);
        poseStack.translate(0.0D, arm[3], 0.0D);
        MODEL.renderPart("Striker4", poseStack, buffer, light, overlay);
        rotateXAround(poseStack, 0.0D, 1.625D, -0.3125D, arm[4]);
        MODEL.renderPart("Blade4", poseStack, buffer, light, overlay);
        poseStack.popPose();
    }

    private static void rotateXAround(PoseStack poseStack, double x, double y, double z, double degrees) {
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) degrees));
        poseStack.translate(-x, -y, -z);
    }

    private static void renderSparks(AssemblyFactoryBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, BlockState state, int packedOverlay, double slide1, double slide2,
            double[] arm2, double[] arm4) {
        if (arm2[3] > -0.375D && arm4[3] > -0.375D) {
            return;
        }
        LegacyUvAnimation.Range u = LegacyUvAnimation.assemblyFactorySparkU(blockEntity.getLevel().getGameTime(), partialTick);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, 0xF000F0, packedOverlay)
                .withTranslucencyNoDepthWrite();
        if (arm2[3] <= -0.375D) {
            poseStack.pushPose();
            poseStack.translate(0.5D + slide1, 1.0625D, -arm2[2] / 45.0D);
            LegacyAssemblySparkRenderer.renderSparkPair(ObjMachineModels.ASSEMBLY_FACTORY_SPARKS_TEXTURE,
                    context, u, LegacyAssemblySparkRenderer.LENGTH);
            poseStack.popPose();
        }
        if (arm4[3] <= -0.375D) {
            poseStack.pushPose();
            poseStack.translate(-0.5D - slide2, 1.0625D, arm4[2] / 45.0D);
            LegacyAssemblySparkRenderer.renderSparkPair(ObjMachineModels.ASSEMBLY_FACTORY_SPARKS_TEXTURE,
                    context, u, -LegacyAssemblySparkRenderer.LENGTH);
            poseStack.popPose();
        }
    }
}
