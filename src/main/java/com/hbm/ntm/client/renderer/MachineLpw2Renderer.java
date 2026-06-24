package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.MachineLpw2BlockEntity;
import com.hbm.ntm.client.obj.ObjReactorModels;
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
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(MachineLpw2BlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        double time = renderTime(blockEntity.getLevel(), partialTick);
        LegacyTileRenderPlans.Lpw2Plan plan = LegacyTileRenderPlans.lpw2Plan(time);
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation(facing)));

        ObjReactorModels.renderLpw2Part("Frame", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);
        renderMainAssembly(plan, poseStack, buffer, modelLight, packedOverlay);
        renderRotating(plan.wireLeft(), poseStack, buffer, modelLight, packedOverlay);
        renderRotating(plan.wireRight(), poseStack, buffer, modelLight, packedOverlay);
        renderTranslated(plan.coverPart(), poseStack, buffer, modelLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 3.5D);
        poseStack.scale(1.0F, 1.0F,
                (float) ((3.0D + plan.cover() * LegacyTileRenderPlans.LPW2_COVER_TRAVEL) / 3.0D));
        poseStack.translate(0.0D, 0.0D, -3.5D);
        ObjReactorModels.renderLpw2Part("SuspensionCoverFront", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -5.5D);
        poseStack.scale(1.0F, 1.0F,
                (float) ((1.5D - plan.cover() * LegacyTileRenderPlans.LPW2_COVER_TRAVEL) / 1.5D));
        poseStack.translate(0.0D, 0.0D, 5.5D);
        ObjReactorModels.renderLpw2Part("SuspensionCoverBack", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -9.0D);
        poseStack.scale(1.0F, 1.0F,
                (float) ((1.25D - plan.sway() * LegacyTileRenderPlans.LPW2_COVER_TRAVEL) / 1.25D));
        poseStack.translate(0.0D, 0.0D, 9.0D);
        ObjReactorModels.renderLpw2Part("SuspensionBackOuter", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -9.5D);
        poseStack.scale(1.0F, 1.0F,
                (float) ((1.75D - plan.sway() * LegacyTileRenderPlans.LPW2_COVER_TRAVEL) / 1.75D));
        poseStack.translate(0.0D, 0.0D, 9.5D);
        ObjReactorModels.renderLpw2Part("SuspensionBackCenter", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        renderServers(plan.servers(), poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderServers(LegacyTileRenderPlans.Lpw2ServerPlan plan, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        for (LegacyTileRenderPlans.TranslatedModelPartPlan server : plan.servers()) {
            renderTranslated(server, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.pushPose();
        poseStack.translate(plan.monitor().translateX(), plan.monitor().translateY(), plan.monitor().translateZ());
        ObjReactorModels.renderLpw2Part("Monitor", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        LegacyTileRenderPlans.TextureMatrixPartPlan screen = plan.errorScreen();
        ObjReactorModels.renderLpw2PartWithLegacyTextureMatrixCull("Screen", ObjReactorModels.LPW2_TERM_ERROR_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay,
                screen.color().redByte(), screen.color().greenByte(), screen.color().blueByte(),
                screen.color().alphaByte(), (float) screen.textureMatrix().scaleU(),
                (float) screen.textureMatrix().scaleV(), (float) screen.textureMatrix().translateU(),
                (float) screen.textureMatrix().translateV());
        poseStack.popPose();
    }

    private static void renderMainAssembly(LegacyTileRenderPlans.Lpw2Plan plan, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(plan.center().translateX(), plan.center().translateY(), plan.center().translateZ());
        ObjReactorModels.renderLpw2Part(plan.center().partName(), ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.0D, 3.5D, 0.0D);

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZN.rotationDegrees((float) (plan.rotor() * 360.0D)));
        poseStack.translate(0.0D, -3.5D, 0.0D);
        ObjReactorModels.renderLpw2Part("Rotor", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (plan.turbine() * 360.0D)));
        poseStack.translate(0.0D, -3.5D, 0.0D);
        ObjReactorModels.renderLpw2Part("TurbineFront", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZN.rotationDegrees((float) (plan.turbine() * 360.0D)));
        poseStack.translate(0.0D, -3.5D, 0.0D);
        ObjReactorModels.renderLpw2Part("TurbineBack", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.popPose();

        renderTranslated(plan.pistonPart(), poseStack, buffer, packedLight, packedOverlay);

        renderBell(plan.bell(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        renderShroud(plan.shroud(), poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderBell(LegacyTileRenderPlans.Lpw2BellPlan plan, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.0D, LegacyTileRenderPlans.LPW2_ENGINE_PIVOT_Y,
                LegacyTileRenderPlans.LPW2_ENGINE_PIVOT_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) (plan.vertical() * plan.rotationMagnitude())));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) (plan.horizontal() * plan.rotationMagnitude())));
        poseStack.translate(0.0D, -LegacyTileRenderPlans.LPW2_ENGINE_PIVOT_Y,
                -LegacyTileRenderPlans.LPW2_ENGINE_PIVOT_Z);
        ObjReactorModels.renderLpw2Part("Engine", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderShroud(LegacyTileRenderPlans.Lpw2ShroudPlan plan, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double h = plan.horizontal();
        double v = plan.vertical();
        poseStack.pushPose();
        poseStack.translate(0.0D, -h * plan.magnitude(), 0.0D);
        ObjReactorModels.renderLpw2Part("ShroudH", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(90.0D + 22.5D, plan.flapRotationScale() * v + plan.flapRotationOffset(),
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(90.0D - 22.5D, plan.flapRotationScale() * v + plan.flapRotationOffset(),
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(270.0D + 22.5D, plan.flapRotationScale() * -v + plan.flapRotationOffset(),
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(270.0D - 22.5D, plan.flapRotationScale() * -v + plan.flapRotationOffset(),
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(v * plan.magnitude(), 0.0D, 0.0D);
        ObjReactorModels.renderLpw2Part("ShroudV", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(22.5D, plan.flapRotationScale() * h + plan.flapRotationOffset(),
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(-22.5D, plan.flapRotationScale() * h + plan.flapRotationOffset(),
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(180.0D + 22.5D, plan.flapRotationScale() * -h + plan.flapRotationOffset(),
                poseStack, buffer, packedLight, packedOverlay);
        renderFlap(180.0D - 22.5D, plan.flapRotationScale() * -h + plan.flapRotationOffset(),
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        double length = plan.suspensionLength();
        poseStack.pushPose();
        poseStack.translate(-2.625D, 0.0D, 0.0D);
        poseStack.scale((float) ((length + v * plan.magnitude()) / length), 1.0F, 1.0F);
        poseStack.translate(2.625D, 0.0D, 0.0D);
        ObjReactorModels.renderLpw2Part("SuspensionLeft", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(2.625D, 0.0D, 0.0D);
        poseStack.scale((float) ((length - v * plan.magnitude()) / length), 1.0F, 1.0F);
        poseStack.translate(-2.625D, 0.0D, 0.0D);
        ObjReactorModels.renderLpw2Part("SuspensionRight", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 6.125D, 0.0D);
        poseStack.scale(1.0F, (float) ((length + h * plan.magnitude()) / length), 1.0F);
        poseStack.translate(0.0D, -6.125D, 0.0D);
        ObjReactorModels.renderLpw2Part("SuspensionTop", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.875D, 0.0D);
        poseStack.scale(1.0F, (float) ((length - h * plan.magnitude()) / length), 1.0F);
        poseStack.translate(0.0D, -0.875D, 0.0D);
        ObjReactorModels.renderLpw2Part("SuspensionBottom", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderFlap(double position, double rotation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 3.5D, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) position));
        poseStack.translate(0.0D, -3.5D, 0.0D);
        poseStack.translate(0.0D, LegacyTileRenderPlans.LPW2_FLAP_PIVOT_Y,
                LegacyTileRenderPlans.LPW2_FLAP_PIVOT_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) rotation));
        poseStack.translate(0.0D, -LegacyTileRenderPlans.LPW2_FLAP_PIVOT_Y,
                -LegacyTileRenderPlans.LPW2_FLAP_PIVOT_Z);
        ObjReactorModels.renderLpw2Part("Flap", ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderTranslated(LegacyTileRenderPlans.TranslatedModelPartPlan part, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        ObjReactorModels.renderLpw2Part(part.partName(), ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderRotating(LegacyTileRenderPlans.RotatingModelPartPlan part, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        poseStack.mulPose(Axis.YP.rotationDegrees((float) part.angleDegrees()));
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        ObjReactorModels.renderLpw2Part(part.partName(), ObjReactorModels.LPW2_TEXTURE,
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
