package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.MachineLpw2BlockEntity;
import com.hbm.ntm.client.obj.LegacyObjTransforms;
import com.hbm.ntm.client.obj.ObjReactorModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MachineLpw2Renderer implements BlockEntityRenderer<MachineLpw2BlockEntity> {
    public MachineLpw2Renderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(MachineLpw2BlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(MachineLpw2BlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(MachineLpw2BlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        double time = renderTime(blockEntity.getLevel(), partialTick);
        double sway = lpw2Sway(time);
        double bellTimer = (time / LegacyTileRenderPlans.LPW2_BELL_TIME_DIVISOR)
                % LegacyTileRenderPlans.LPW2_TIMER_MODULO_4PI;
        double horizontal = (Math.sin(bellTimer + Math.PI) + Math.sin(bellTimer * 1.5D))
                / LegacyTileRenderPlans.LPW2_BELL_DIVISOR;
        double vertical = (Math.sin(bellTimer) + Math.sin(bellTimer * 1.5D))
                / LegacyTileRenderPlans.LPW2_BELL_DIVISOR;
        double pistonTimer = (time / LegacyTileRenderPlans.LPW2_PISTON_TIME_DIVISOR)
                % LegacyTileRenderPlans.LPW2_TIMER_MODULO_2PI;
        double piston = LegacyObjTransforms.softPeakSine(pistonTimer);
        double rotorTimer = (time / LegacyTileRenderPlans.LPW2_ROTOR_TIME_DIVISOR)
                % LegacyTileRenderPlans.LPW2_TIMER_MODULO_16PI;
        double rotor = (LegacyObjTransforms.softPeakSine(rotorTimer) + rotorTimer / 2.0D - 1.0D)
                / LegacyTileRenderPlans.LPW2_ROTOR_DENOMINATOR;
        double turbine = (time % LegacyTileRenderPlans.LPW2_TURBINE_PERIOD)
                / LegacyTileRenderPlans.LPW2_TURBINE_PERIOD;
        double cover = lpw2Cover(time);
        double serverTimer = (time / LegacyTileRenderPlans.LPW2_SERVER_TIME_DIVISOR)
                % LegacyTileRenderPlans.LPW2_TIMER_MODULO_4PI;
        double serverX = (Math.sin(serverTimer + Math.PI) + Math.sin(serverTimer * 1.5D))
                / LegacyTileRenderPlans.LPW2_BELL_DIVISOR;
        double serverY = (Math.sin(serverTimer) + Math.sin(serverTimer * 1.5D))
                / LegacyTileRenderPlans.LPW2_BELL_DIVISOR;
        double errorTimer = time / LegacyTileRenderPlans.LPW2_ERROR_TIME_DIVISOR;
        double errorV = (LegacyObjTransforms.softPeakSine(errorTimer) + errorTimer / 2.0D) % 1.0D;
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, yRotation(facing));

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            ObjReactorModels.renderLpw2Part("Frame", ObjReactorModels.LPW2_TEXTURE,
                    poseStack, buffer, modelLight, packedOverlay);
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                renderMainAssembly(sway, horizontal, vertical, piston, rotor, turbine, poseStack, buffer,
                        modelLight, packedOverlay);
                renderRotatingY("WireLeft", LegacyTileRenderPlans.LPW2_WIRE_LEFT_PIVOT_X,
                        LegacyTileRenderPlans.LPW2_WIRE_PIVOT_Z,
                        sway * LegacyTileRenderPlans.LPW2_WIRE_ROTATION_SCALE, poseStack, buffer,
                        modelLight, packedOverlay);
                renderRotatingY("WireRight", LegacyTileRenderPlans.LPW2_WIRE_RIGHT_PIVOT_X,
                        LegacyTileRenderPlans.LPW2_WIRE_PIVOT_Z,
                        sway * -LegacyTileRenderPlans.LPW2_WIRE_ROTATION_SCALE, poseStack, buffer,
                        modelLight, packedOverlay);
                renderTranslated("Cover", 0.0D, 0.0D, -cover * LegacyTileRenderPlans.LPW2_COVER_TRAVEL,
                        poseStack, buffer, modelLight, packedOverlay);

                renderScaledZ("SuspensionCoverFront", 3.5D,
                        (3.0D + cover * LegacyTileRenderPlans.LPW2_COVER_TRAVEL) / 3.0D,
                        poseStack, buffer, modelLight, packedOverlay);
                renderScaledZ("SuspensionCoverBack", -5.5D,
                        (1.5D - cover * LegacyTileRenderPlans.LPW2_COVER_TRAVEL) / 1.5D,
                        poseStack, buffer, modelLight, packedOverlay);
                renderScaledZ("SuspensionBackOuter", -9.0D,
                        (1.25D - sway * LegacyTileRenderPlans.LPW2_COVER_TRAVEL) / 1.25D,
                        poseStack, buffer, modelLight, packedOverlay);
                renderScaledZ("SuspensionBackCenter", -9.5D,
                        (1.75D - sway * LegacyTileRenderPlans.LPW2_COVER_TRAVEL) / 1.75D,
                        poseStack, buffer, modelLight, packedOverlay);

                renderServers(serverX, serverY, errorV, poseStack, buffer, modelLight, packedOverlay);
            }
        }
        poseStack.popPose();
    }

    private static double lpw2Sway(double time) {
        double swayTimer = (time / LegacyTileRenderPlans.LPW2_SWAY_TIME_DIVISOR)
                % LegacyTileRenderPlans.LPW2_TIMER_MODULO_4PI;
        return (Math.sin(swayTimer) + Math.sin(swayTimer * 2.0D)
                + Math.sin(swayTimer * 4.0D) + LegacyTileRenderPlans.LPW2_SWAY_OFFSET)
                * LegacyTileRenderPlans.LPW2_SWAY_SCALE;
    }

    private static double lpw2Cover(double time) {
        double coverTimer = (time / LegacyTileRenderPlans.LPW2_BELL_TIME_DIVISOR)
                % LegacyTileRenderPlans.LPW2_TIMER_MODULO_4PI;
        return (Math.sin(coverTimer) + Math.sin(coverTimer * 2.0D) + Math.sin(coverTimer * 4.0D))
                * LegacyTileRenderPlans.LPW2_SWAY_SCALE;
    }

    private static void renderServers(double serverX, double serverY, double errorV, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double sway = LegacyTileRenderPlans.LPW2_SERVER_SWAY;
        renderTranslated("Server1", serverX * sway, 0.0D, serverY * sway,
                poseStack, buffer, packedLight, packedOverlay);
        renderTranslated("Server2", -serverY * sway, 0.0D, serverX * sway,
                poseStack, buffer, packedLight, packedOverlay);
        renderTranslated("Server3", serverY * sway, 0.0D, -serverX * sway,
                poseStack, buffer, packedLight, packedOverlay);
        renderTranslated("Server4", -serverX * sway, 0.0D, -serverY * sway,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.pushPose();
        poseStack.translate(serverY * sway, 0.0D, serverX * sway);
        ObjReactorModels.renderLpw2Part("Monitor", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        ObjReactorModels.renderLpw2PartWithLegacyTextureMatrixCull("Screen", ObjReactorModels.LPW2_TERM_ERROR_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, 1.0F, 1.0F, 0.0F, (float) errorV);
        poseStack.popPose();
    }

    private static void renderMainAssembly(double sway, double horizontal, double vertical, double piston,
            double rotor, double turbine, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -sway * LegacyTileRenderPlans.LPW2_CENTER_SWAY_TRAVEL);
        ObjReactorModels.renderLpw2Part("Center", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 3.5D, 0.0D);

        poseStack.pushPose();
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (-rotor * 360.0D));
        poseStack.translate(0.0D, -3.5D, 0.0D);
        ObjReactorModels.renderLpw2Part("Rotor", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (turbine * 360.0D));
        poseStack.translate(0.0D, -3.5D, 0.0D);
        ObjReactorModels.renderLpw2Part("TurbineFront", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (-turbine * 360.0D));
        poseStack.translate(0.0D, -3.5D, 0.0D);
        ObjReactorModels.renderLpw2Part("TurbineBack", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.popPose();

        renderTranslated("Piston", 0.0D, 0.0D,
                piston * LegacyTileRenderPlans.LPW2_PISTON_TRAVEL + LegacyTileRenderPlans.LPW2_PISTON_BASE_Z,
                poseStack, buffer, packedLight, packedOverlay);

        renderBell(horizontal, vertical, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        renderShroud(horizontal, vertical, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderBell(double horizontal, double vertical, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.0D, LegacyTileRenderPlans.LPW2_ENGINE_PIVOT_Y,
                LegacyTileRenderPlans.LPW2_ENGINE_PIVOT_Z);
        LegacyPoseRotations.rotateYDegrees(poseStack,
                (float) (vertical * LegacyTileRenderPlans.LPW2_ENGINE_ROTATION_MAGNITUDE));
        LegacyPoseRotations.rotateXDegrees(poseStack,
                (float) (horizontal * LegacyTileRenderPlans.LPW2_ENGINE_ROTATION_MAGNITUDE));
        poseStack.translate(0.0D, -LegacyTileRenderPlans.LPW2_ENGINE_PIVOT_Y,
                -LegacyTileRenderPlans.LPW2_ENGINE_PIVOT_Z);
        ObjReactorModels.renderLpw2Part("Engine", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderShroud(double horizontal, double vertical, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double h = horizontal;
        double v = vertical;
        poseStack.pushPose();
        poseStack.translate(0.0D, -h * LegacyTileRenderPlans.LPW2_SHROUD_MAGNITUDE, 0.0D);
        ObjReactorModels.renderLpw2Part("ShroudH", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(90.0D + 22.5D,
                LegacyTileRenderPlans.LPW2_FLAP_ROTATION_SCALE * v
                        + LegacyTileRenderPlans.LPW2_FLAP_ROTATION_OFFSET,
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(90.0D - 22.5D,
                LegacyTileRenderPlans.LPW2_FLAP_ROTATION_SCALE * v
                        + LegacyTileRenderPlans.LPW2_FLAP_ROTATION_OFFSET,
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(270.0D + 22.5D,
                LegacyTileRenderPlans.LPW2_FLAP_ROTATION_SCALE * -v
                        + LegacyTileRenderPlans.LPW2_FLAP_ROTATION_OFFSET,
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(270.0D - 22.5D,
                LegacyTileRenderPlans.LPW2_FLAP_ROTATION_SCALE * -v
                        + LegacyTileRenderPlans.LPW2_FLAP_ROTATION_OFFSET,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(v * LegacyTileRenderPlans.LPW2_SHROUD_MAGNITUDE, 0.0D, 0.0D);
        ObjReactorModels.renderLpw2Part("ShroudV", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(22.5D,
                LegacyTileRenderPlans.LPW2_FLAP_ROTATION_SCALE * h
                        + LegacyTileRenderPlans.LPW2_FLAP_ROTATION_OFFSET,
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(-22.5D,
                LegacyTileRenderPlans.LPW2_FLAP_ROTATION_SCALE * h
                        + LegacyTileRenderPlans.LPW2_FLAP_ROTATION_OFFSET,
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(180.0D + 22.5D,
                LegacyTileRenderPlans.LPW2_FLAP_ROTATION_SCALE * -h
                        + LegacyTileRenderPlans.LPW2_FLAP_ROTATION_OFFSET,
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(180.0D - 22.5D,
                LegacyTileRenderPlans.LPW2_FLAP_ROTATION_SCALE * -h
                        + LegacyTileRenderPlans.LPW2_FLAP_ROTATION_OFFSET,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        double length = LegacyTileRenderPlans.LPW2_SUSPENSION_LENGTH;
        poseStack.pushPose();
        poseStack.translate(-2.625D, 0.0D, 0.0D);
        poseStack.scale((float) ((length + v * LegacyTileRenderPlans.LPW2_SHROUD_MAGNITUDE) / length),
                1.0F, 1.0F);
        poseStack.translate(2.625D, 0.0D, 0.0D);
        ObjReactorModels.renderLpw2Part("SuspensionLeft", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(2.625D, 0.0D, 0.0D);
        poseStack.scale((float) ((length - v * LegacyTileRenderPlans.LPW2_SHROUD_MAGNITUDE) / length),
                1.0F, 1.0F);
        poseStack.translate(-2.625D, 0.0D, 0.0D);
        ObjReactorModels.renderLpw2Part("SuspensionRight", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 6.125D, 0.0D);
        poseStack.scale(1.0F,
                (float) ((length + h * LegacyTileRenderPlans.LPW2_SHROUD_MAGNITUDE) / length), 1.0F);
        poseStack.translate(0.0D, -6.125D, 0.0D);
        ObjReactorModels.renderLpw2Part("SuspensionTop", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.875D, 0.0D);
        poseStack.scale(1.0F,
                (float) ((length - h * LegacyTileRenderPlans.LPW2_SHROUD_MAGNITUDE) / length), 1.0F);
        poseStack.translate(0.0D, -0.875D, 0.0D);
        ObjReactorModels.renderLpw2Part("SuspensionBottom", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderFlap(double position, double rotation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 3.5D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) position);
        poseStack.translate(0.0D, -3.5D, 0.0D);
        poseStack.translate(0.0D, LegacyTileRenderPlans.LPW2_FLAP_PIVOT_Y,
                LegacyTileRenderPlans.LPW2_FLAP_PIVOT_Z);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) rotation);
        poseStack.translate(0.0D, -LegacyTileRenderPlans.LPW2_FLAP_PIVOT_Y,
                -LegacyTileRenderPlans.LPW2_FLAP_PIVOT_Z);
        ObjReactorModels.renderLpw2Part("Flap", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderTranslated(String partName, double translateX, double translateY, double translateZ,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        ObjReactorModels.renderLpw2Part(partName, ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderRotatingY(String partName, double pivotX, double pivotZ, double angleDegrees,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(pivotX, 0.0D, pivotZ);
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) angleDegrees);
        poseStack.translate(-pivotX, 0.0D, -pivotZ);
        ObjReactorModels.renderLpw2Part(partName, ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderScaledZ(String partName, double pivotZ, double scaleZ, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, pivotZ);
        poseStack.scale(1.0F, 1.0F, (float) scaleZ);
        poseStack.translate(0.0D, 0.0D, -pivotZ);
        ObjReactorModels.renderLpw2Part(partName, ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static float yRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 90.0F;
            case WEST -> 180.0F;
            case SOUTH -> 270.0F;
            case EAST -> 0.0F;
            default -> 0.0F;
        };
    }

    private static double renderTime(Level level, float partialTick) {
        return level == null ? partialTick : level.getGameTime() + partialTick;
    }
}
