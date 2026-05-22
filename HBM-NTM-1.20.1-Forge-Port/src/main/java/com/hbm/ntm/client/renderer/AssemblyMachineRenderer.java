package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class AssemblyMachineRenderer implements BlockEntityRenderer<AssemblyMachineBlockEntity> {
    private static final LegacyWavefrontModel MODEL = new LegacyWavefrontModel(
            new ResourceLocation("hbm", "models/block/machines/assembly_machine.obj"),
            new ResourceLocation("hbm", "textures/block/machines/assembly_machine.png"));

    public AssemblyMachineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(AssemblyMachineBlockEntity blockEntity) {
        return true;
    }

    @Override
    public void render(AssemblyMachineBlockEntity assembler, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = assembler.getBlockState();

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F + blockstateModelYRotation(state)));

        MODEL.renderPart("Base", poseStack, buffer, packedLight, packedOverlay);
        if (assembler.shouldRenderFrame()) {
            MODEL.renderPart("Frame", poseStack, buffer, packedLight, packedOverlay);
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) assembler.getRing(partialTick)));
        MODEL.renderPart("Ring", poseStack, buffer, packedLight, packedOverlay);
        renderArm(poseStack, buffer, packedLight, packedOverlay, assembler.getArm(0).getPositions(partialTick), false);
        renderArm(poseStack, buffer, packedLight, packedOverlay, assembler.getArm(1).getPositions(partialTick), true);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void renderArm(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, double[] arm, boolean mirrored) {
        double zSign = mirrored ? -1.0D : 1.0D;
        String suffix = mirrored ? "2" : "1";
        double rotationSign = mirrored ? -1.0D : 1.0D;

        poseStack.pushPose();
        rotateAround(poseStack, 0.0D, 1.625D, 0.9375D * zSign, rotationSign * arm[0]);
        MODEL.renderPart("ArmLower" + suffix, poseStack, buffer, packedLight, packedOverlay);

        rotateAround(poseStack, 0.0D, 2.375D, 0.9375D * zSign, rotationSign * arm[1]);
        MODEL.renderPart("ArmUpper" + suffix, poseStack, buffer, packedLight, packedOverlay);

        rotateAround(poseStack, 0.0D, 2.375D, 0.4375D * zSign, rotationSign * arm[2]);
        MODEL.renderPart("Head" + suffix, poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(0.0D, arm[3], 0.0D);
        MODEL.renderPart("Spike" + suffix, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void rotateAround(PoseStack poseStack, double x, double y, double z, double degrees) {
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) degrees));
        poseStack.translate(-x, -y, -z);
    }

    private static float blockstateModelYRotation(BlockState state) {
        if (!state.hasProperty(HorizontalMachineBlock.FACING)) {
            return 0.0F;
        }
        Direction facing = state.getValue(HorizontalMachineBlock.FACING);
        return facing.toYRot();
    }
}
