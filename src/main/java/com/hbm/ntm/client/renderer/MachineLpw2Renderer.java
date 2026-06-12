package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.MachineLpw2BlockEntity;
import com.hbm.ntm.client.obj.ObjReactorModels;
import com.hbm.ntm.util.BobMathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MachineLpw2Renderer implements BlockEntityRenderer<MachineLpw2BlockEntity> {
    public MachineLpw2Renderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(MachineLpw2BlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void render(MachineLpw2BlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        double time = renderTime(blockEntity.getLevel(), partialTick);
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight);

        double swayTimer = (time / 3.0D) % (Math.PI * 4.0D);
        double sway = (Math.sin(swayTimer) + Math.sin(swayTimer * 2.0D) + Math.sin(swayTimer * 4.0D) + 2.23255D) * 0.5D;

        double bellTimer = (time / 5.0D) % (Math.PI * 4.0D);
        double h = (Math.sin(bellTimer + Math.PI) + Math.sin(bellTimer * 1.5D)) / 1.90596D;
        double v = (Math.sin(bellTimer) + Math.sin(bellTimer * 1.5D)) / 1.90596D;

        double pistonTimer = (time / 5.0D) % (Math.PI * 2.0D);
        double piston = BobMathUtil.sps(pistonTimer);
        double rotorTimer = (time / 5.0D) % (Math.PI * 16.0D);
        double rotor = (BobMathUtil.sps(rotorTimer) + rotorTimer / 2.0D - 1.0D) / 25.1327412287D;
        double turbine = (time % 100.0D) / 100.0D;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation(facing)));

        ObjReactorModels.LPW2.renderPart("Frame", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);
        renderMainAssembly(sway, h, v, piston, rotor, turbine, poseStack, buffer, modelLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(-2.9375D, 0.0D, 2.375D);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) (sway * 10.0D)));
        poseStack.translate(2.9375D, 0.0D, -2.375D);
        ObjReactorModels.LPW2.renderPart("WireLeft", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(2.9375D, 0.0D, 2.375D);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) (sway * -10.0D)));
        poseStack.translate(-2.9375D, 0.0D, -2.375D);
        ObjReactorModels.LPW2.renderPart("WireRight", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        double coverTimer = (time / 5.0D) % (Math.PI * 4.0D);
        double cover = (Math.sin(coverTimer) + Math.sin(coverTimer * 2.0D) + Math.sin(coverTimer * 4.0D)) * 0.5D;
        renderTranslated("Cover", 0.0D, 0.0D, -cover * 0.125D, poseStack, buffer, modelLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 3.5D);
        poseStack.scale(1.0F, 1.0F, (float) ((3.0D + cover * 0.125D) / 3.0D));
        poseStack.translate(0.0D, 0.0D, -3.5D);
        ObjReactorModels.LPW2.renderPart("SuspensionCoverFront", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -5.5D);
        poseStack.scale(1.0F, 1.0F, (float) ((1.5D - cover * 0.125D) / 1.5D));
        poseStack.translate(0.0D, 0.0D, 5.5D);
        ObjReactorModels.LPW2.renderPart("SuspensionCoverBack", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -9.0D);
        poseStack.scale(1.0F, 1.0F, (float) ((1.25D - sway * 0.125D) / 1.25D));
        poseStack.translate(0.0D, 0.0D, 9.0D);
        ObjReactorModels.LPW2.renderPart("SuspensionBackOuter", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -9.5D);
        poseStack.scale(1.0F, 1.0F, (float) ((1.75D - sway * 0.125D) / 1.75D));
        poseStack.translate(0.0D, 0.0D, 9.5D);
        ObjReactorModels.LPW2.renderPart("SuspensionBackCenter", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        renderServers(time, poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderServers(double time, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        double serverTimer = (time / 2.0D) % (Math.PI * 4.0D);
        double sx = (Math.sin(serverTimer + Math.PI) + Math.sin(serverTimer * 1.5D)) / 1.90596D;
        double sy = (Math.sin(serverTimer) + Math.sin(serverTimer * 1.5D)) / 1.90596D;
        double serverSway = 0.0625D * 0.25D;

        renderTranslated("Server1", sx * serverSway, 0.0D, sy * serverSway, poseStack, buffer, packedLight, packedOverlay);
        renderTranslated("Server2", -sy * serverSway, 0.0D, sx * serverSway, poseStack, buffer, packedLight, packedOverlay);
        renderTranslated("Server3", sy * serverSway, 0.0D, -sx * serverSway, poseStack, buffer, packedLight, packedOverlay);
        renderTranslated("Server4", -sx * serverSway, 0.0D, -sy * serverSway, poseStack, buffer, packedLight, packedOverlay);

        double errorTimer = time / 3.0D;
        poseStack.pushPose();
        poseStack.translate(sy * serverSway, 0.0D, sx * serverSway);
        ObjReactorModels.LPW2.renderPart("Monitor", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        ObjReactorModels.LPW2.renderPartWithLegacyTextureMatrix("Screen", ObjReactorModels.LPW2_TERM_ERROR_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255,
                1.0F, 1.0F, 0.0F, (float) ((BobMathUtil.sps(errorTimer) + errorTimer / 2.0D) % 1.0D));
        poseStack.popPose();
    }

    private static void renderMainAssembly(double sway, double h, double v, double piston, double rotor,
            double turbine, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -sway * 0.125D);
        ObjReactorModels.LPW2.renderPart("Center", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 3.5D, 0.0D);

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZN.rotationDegrees((float) (rotor * 360.0D)));
        poseStack.translate(0.0D, -3.5D, 0.0D);
        ObjReactorModels.LPW2.renderPart("Rotor", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (turbine * 360.0D)));
        poseStack.translate(0.0D, -3.5D, 0.0D);
        ObjReactorModels.LPW2.renderPart("TurbineFront", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZN.rotationDegrees((float) (turbine * 360.0D)));
        poseStack.translate(0.0D, -3.5D, 0.0D);
        ObjReactorModels.LPW2.renderPart("TurbineBack", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.popPose();

        renderTranslated("Piston", 0.0D, 0.0D, piston * 0.375D + 0.375D,
                poseStack, buffer, packedLight, packedOverlay);

        renderBell(h, v, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        renderShroud(h, v, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderBell(double h, double v, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 3.5D, 2.75D);
        double magnitude = 2.0D;
        poseStack.mulPose(Axis.YP.rotationDegrees((float) (v * magnitude)));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) (h * magnitude)));
        poseStack.translate(0.0D, -3.5D, -2.75D);
        ObjReactorModels.LPW2.renderPart("Engine", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderShroud(double h, double v, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        double magnitude = 0.125D;
        double rotation = 5.0D;
        double offset = 10.0D;

        poseStack.pushPose();
        poseStack.translate(0.0D, -h * magnitude, 0.0D);
        ObjReactorModels.LPW2.renderPart("ShroudH", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(90.0D + 22.5D, rotation * v + offset, poseStack, buffer, packedLight, packedOverlay);
        renderFlap(90.0D - 22.5D, rotation * v + offset, poseStack, buffer, packedLight, packedOverlay);
        renderFlap(270.0D + 22.5D, rotation * -v + offset, poseStack, buffer, packedLight, packedOverlay);
        renderFlap(270.0D - 22.5D, rotation * -v + offset, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(v * magnitude, 0.0D, 0.0D);
        ObjReactorModels.LPW2.renderPart("ShroudV", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(22.5D, rotation * h + offset, poseStack, buffer, packedLight, packedOverlay);
        renderFlap(-22.5D, rotation * h + offset, poseStack, buffer, packedLight, packedOverlay);
        renderFlap(180.0D + 22.5D, rotation * -h + offset, poseStack, buffer, packedLight, packedOverlay);
        renderFlap(180.0D - 22.5D, rotation * -h + offset, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        double length = 0.6875D;
        poseStack.pushPose();
        poseStack.translate(-2.625D, 0.0D, 0.0D);
        poseStack.scale((float) ((length + v * magnitude) / length), 1.0F, 1.0F);
        poseStack.translate(2.625D, 0.0D, 0.0D);
        ObjReactorModels.LPW2.renderPart("SuspensionLeft", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(2.625D, 0.0D, 0.0D);
        poseStack.scale((float) ((length - v * magnitude) / length), 1.0F, 1.0F);
        poseStack.translate(-2.625D, 0.0D, 0.0D);
        ObjReactorModels.LPW2.renderPart("SuspensionRight", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 6.125D, 0.0D);
        poseStack.scale(1.0F, (float) ((length + h * magnitude) / length), 1.0F);
        poseStack.translate(0.0D, -6.125D, 0.0D);
        ObjReactorModels.LPW2.renderPart("SuspensionTop", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.875D, 0.0D);
        poseStack.scale(1.0F, (float) ((length - h * magnitude) / length), 1.0F);
        poseStack.translate(0.0D, -0.875D, 0.0D);
        ObjReactorModels.LPW2.renderPart("SuspensionBottom", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderFlap(double position, double rotation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 3.5D, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) position));
        poseStack.translate(0.0D, -3.5D, 0.0D);
        poseStack.translate(0.0D, 6.96875D, 8.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) rotation));
        poseStack.translate(0.0D, -6.96875D, -8.5D);
        ObjReactorModels.LPW2.renderPart("Flap", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderTranslated(String part, double x, double y, double z, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        ObjReactorModels.LPW2.renderPart(part, ObjReactorModels.LPW2_TEXTURE,
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
