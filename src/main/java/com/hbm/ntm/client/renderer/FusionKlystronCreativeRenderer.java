package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.FusionKlystronCreativeBlockEntity;
import com.hbm.ntm.client.obj.ObjFusionModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class FusionKlystronCreativeRenderer implements BlockEntityRenderer<FusionKlystronCreativeBlockEntity> {
    public FusionKlystronCreativeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(FusionKlystronCreativeBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(FusionKlystronCreativeBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, light, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(FusionBreederRenderer.rotation(state)));
        poseStack.translate(-1.0D, 0.0D, 0.0D);
        ObjFusionModels.KLYSTRON_LEGACY.renderOnly(ObjFusionModels.KLYSTRON_CREATIVE_TEXTURE, context, "Klystron");

        poseStack.pushPose();
        poseStack.translate(0.0D, 2.5D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(blockEntity.getFan(partialTick)));
        poseStack.translate(0.0D, -2.5D, 0.0D);
        ObjFusionModels.KLYSTRON_LEGACY.renderOnly(ObjFusionModels.KLYSTRON_CREATIVE_TEXTURE, context, "Rotor");
        poseStack.popPose();
        poseStack.popPose();
    }
}
