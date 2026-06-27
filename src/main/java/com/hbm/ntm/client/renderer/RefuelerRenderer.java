package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.RefuelerBlock;
import com.hbm.ntm.blockentity.RefuelerBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class RefuelerRenderer implements BlockEntityRenderer<RefuelerBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjMachineModels.REFUELER;
    private static final LegacyWavefrontModel.SelectionHandle FUELER =
            MODEL.prepareRenderOnlyInCallOrder("Fueler");
    private static final LegacyWavefrontModel.SelectionHandle FLUID =
            MODEL.prepareRenderOnlyInCallOrder("Fluid");

    public RefuelerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(RefuelerBlockEntity refueler, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        BlockState state = refueler.getBlockState();
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(refueler, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        orient(poseStack, state.hasProperty(RefuelerBlock.FACING) ? state.getValue(RefuelerBlock.FACING) : Direction.NORTH);

        MODEL.renderOnlyInCallOrder(ObjMachineModels.REFUELER_TEXTURE, poseStack, buffer, modelLight,
                packedOverlay, FUELER);
        enqueueFluid(refueler, partialTick, poseStack, buffer);
        poseStack.popPose();
    }

    public static void renderItem(BlockState state, net.minecraft.world.item.ItemDisplayContext displayContext,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = MODEL.boundsOnly("Fueler");
        poseStack.pushPose();
        if (displayContext == net.minecraft.world.item.ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.625D, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.scale(0.375F, 0.375F, 0.375F);
            poseStack.translate(0.0D, -3.0D, 0.0D);
        } else {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(0.55F, 0.55F, 0.55F);
            poseStack.translate(-bounds.getCenter().x, -bounds.getCenter().y, -bounds.getCenter().z);
        }
        MODEL.renderOnlyInCallOrder(ObjMachineModels.REFUELER_TEXTURE, poseStack, buffer, packedLight,
                packedOverlay, FUELER);
        poseStack.popPose();
    }

    private static void enqueueFluid(RefuelerBlockEntity refueler, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer) {
        double fillLevel = Math.max(0.0D, Math.min(1.0D, refueler.getInterpolatedFillLevel(partialTick)));
        if (fillLevel <= 1.0E-5D) {
            return;
        }
        int color = refueler.getTank().getTankType().getColor();
        LegacyTileRenderPlans.RefuelerFluidPlan plan = LegacyTileRenderPlans.refuelerFluidPlan(fillLevel, color);
        poseStack.pushPose();
        poseStack.translate(0.0D, plan.translateY(), 0.0D);
        LegacyTileRenderPlans.ClipPlanePlan clip = plan.clipPlane();
        double clipD = clip.d() + clip.y() * plan.translateY();
        LegacyTileRenderPlans.RgbaPlan tint = plan.color();
        LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, queuedPose ->
                MODEL.renderOnlyUntexturedClipped(queuedPose, buffer, tint.redByte(), tint.greenByte(), tint.blueByte(),
                        tint.alphaByte(), LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                        FLUID, clip.x(), clip.y(), clip.z(), clipD));
        poseStack.popPose();
    }

    private static void orient(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case EAST -> {
            }
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            default -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
    }
}
