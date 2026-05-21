package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class AssemblyMachineRenderer implements BlockEntityRenderer<AssemblyMachineBlockEntity> {
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
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F + blockstateModelYRotation(state)));

        ObjMachineModels.ASSEMBLY_MACHINE.renderPart("Base", context);
        if (assembler.shouldRenderFrame()) {
            ObjMachineModels.ASSEMBLY_MACHINE.renderPart("Frame", context);
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) assembler.getRing(partialTick)));
        ObjMachineModels.ASSEMBLY_MACHINE.renderPart("Ring", context);
        renderArm(context, poseStack, assembler.getArm(0).getPositions(partialTick), false);
        renderArm(context, poseStack, assembler.getArm(1).getPositions(partialTick), true);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void renderArm(ObjRenderContext context, PoseStack poseStack, double[] arm, boolean mirrored) {
        double zSign = mirrored ? -1.0D : 1.0D;
        String suffix = mirrored ? "2" : "1";
        double rotationSign = mirrored ? -1.0D : 1.0D;

        poseStack.pushPose();
        rotateAround(poseStack, 0.0D, 1.625D, 0.9375D * zSign, rotationSign * arm[0]);
        ObjMachineModels.ASSEMBLY_MACHINE.renderPart("ArmLower" + suffix, context);

        rotateAround(poseStack, 0.0D, 2.375D, 0.9375D * zSign, rotationSign * arm[1]);
        ObjMachineModels.ASSEMBLY_MACHINE.renderPart("ArmUpper" + suffix, context);

        rotateAround(poseStack, 0.0D, 2.375D, 0.4375D * zSign, rotationSign * arm[2]);
        ObjMachineModels.ASSEMBLY_MACHINE.renderPart("Head" + suffix, context);
        poseStack.translate(0.0D, arm[3], 0.0D);
        ObjMachineModels.ASSEMBLY_MACHINE.renderPart("Spike" + suffix, context);
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
