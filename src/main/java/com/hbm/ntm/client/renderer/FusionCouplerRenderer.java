package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.FusionCouplerBlockEntity;
import com.hbm.ntm.client.obj.ObjFusionModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class FusionCouplerRenderer implements BlockEntityRenderer<FusionCouplerBlockEntity> {
    public FusionCouplerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(FusionCouplerBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(FusionCouplerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(FusionBreederRenderer.rotation(state)));
        ObjFusionModels.COUPLER.render(new ObjRenderContext(poseStack, buffer, state, light, packedOverlay));
        poseStack.popPose();
    }
}
