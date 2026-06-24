package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.RefuelerBlock;
import com.hbm.ntm.blockentity.RefuelerBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
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
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        orient(poseStack, state.hasProperty(RefuelerBlock.FACING) ? state.getValue(RefuelerBlock.FACING) : Direction.NORTH);

        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay);
        MODEL.renderOnlyInCallOrder(ObjMachineModels.REFUELER_TEXTURE, context, FUELER);
        renderFluid(refueler, partialTick, poseStack, context);
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
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay);
        MODEL.renderOnlyInCallOrder(ObjMachineModels.REFUELER_TEXTURE, context, FUELER);
        poseStack.popPose();
    }

    private static void renderFluid(RefuelerBlockEntity refueler, float partialTick, PoseStack poseStack,
            ObjRenderContext context) {
        if (refueler.getTank().getFill() <= 0) {
            return;
        }
        int color = refueler.getTank().getTankType().getColor();
        double fillLevel = Math.max(0.0D, Math.min(1.0D, refueler.getInterpolatedFillLevel(partialTick)));
        poseStack.pushPose();
        poseStack.translate(0.0D, (1.0D - fillLevel) * -0.625D, 0.0D);
        MODEL.renderOnlyUntextured(
                context.withRgba(color >>> 16 & 255, color >>> 8 & 255, color & 255, 191)
                        .withAdditiveTranslucency(),
                FLUID);
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
