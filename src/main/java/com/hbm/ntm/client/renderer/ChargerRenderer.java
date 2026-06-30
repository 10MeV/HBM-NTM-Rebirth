package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.ChargerBlock;
import com.hbm.ntm.blockentity.ChargerBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class ChargerRenderer implements BlockEntityRenderer<ChargerBlockEntity> {
    private static final LegacyWavefrontModel.SelectionHandle BASE =
            ObjMachineModels.CHARGER.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle LEFT =
            ObjMachineModels.CHARGER.prepareRenderOnlyInCallOrder("Left");
    private static final LegacyWavefrontModel.SelectionHandle RIGHT =
            ObjMachineModels.CHARGER.prepareRenderOnlyInCallOrder("Right");
    private static final LegacyWavefrontModel.SelectionHandle SLIDE =
            ObjMachineModels.CHARGER.prepareRenderOnlyInCallOrder("Slide");
    private static final LegacyWavefrontModel.SelectionHandle LIGHT =
            ObjMachineModels.CHARGER.prepareRenderOnlyInCallOrder("Light");

    public ChargerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ChargerBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(ChargerBlockEntity charger, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(charger, getViewDistance())) {
            return;
        }
        LegacyBlockEntityRenderCulling.recordMachineSubmission(charger);
        BlockState state = charger.getBlockState();
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(charger, packedLight);
        try (LegacyRenderLighting.ModelViewSamplingScope ignored =
                LegacyRenderLighting.pushModelViewSampling(charger, poseStack.last().pose())) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.5D, 0.5D);
            orient(poseStack, state.hasProperty(ChargerBlock.FACING) ? state.getValue(ChargerBlock.FACING) : Direction.NORTH);
            poseStack.translate(-0.5D, -0.5D, -0.5D);
            float time = charger.getSlide(partialTick);
            double extend = Math.min(1.0D, time * 2.0D);
            double swivel = Math.max(0.0D, (time - 0.5D) * 2.0D);

            renderPart(BASE, poseStack, buffer, modelLight, packedOverlay);
            poseStack.pushPose();
            applySlideFrame(poseStack, extend);
            renderArm(poseStack, buffer, modelLight, packedOverlay, LEFT, 30.0D * swivel);
            renderArm(poseStack, buffer, modelLight, packedOverlay, RIGHT, -30.0D * swivel);
            poseStack.popPose();

            ObjMachineModels.CHARGER.renderOnlyUntextured(poseStack, buffer, 255, 191, 0, 255, LIGHT);

            poseStack.pushPose();
            applySlideFrame(poseStack, extend);
            renderPart(SLIDE, poseStack, buffer, modelLight, packedOverlay);
            poseStack.popPose();
            poseStack.popPose();
        }
    }

    private static void applySlideFrame(PoseStack poseStack, double extend) {
        poseStack.translate(-0.34375D, 0.25D, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(10.0F));
        poseStack.translate(0.34375D, -0.25D, 0.0D);
        poseStack.translate(0.0D, -0.25D * extend, 0.0D);
    }

    private static void renderArm(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyWavefrontModel.SelectionHandle part, double angle) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.28D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) angle));
        poseStack.translate(0.0D, -0.28D, 0.0D);
        renderPart(part, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderPart(LegacyWavefrontModel.SelectionHandle handle, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjMachineModels.CHARGER.renderOnlyInCallOrder(ObjMachineModels.CHARGER_TEXTURE, poseStack, buffer,
                packedLight, packedOverlay, handle);
    }

    private static void orient(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case SOUTH -> poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            case WEST -> {
                poseStack.mulPose(Axis.ZN.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case EAST -> {
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            }
            default -> {
            }
        }
    }
}
