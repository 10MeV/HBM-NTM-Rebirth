package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.FusionKlystronBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjFusionModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class FusionKlystronRenderer implements BlockEntityRenderer<FusionKlystronBlockEntity> {
    public FusionKlystronRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(FusionKlystronBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(FusionKlystronBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, light, packedOverlay)
                .withRenderMode(LegacyTexturedRenderMode.CUTOUT_CULL);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(FusionBreederRenderer.rotation(state)));
        poseStack.translate(-1.0D, 0.0D, 0.0D);
        ObjFusionModels.renderKlystronPart(ObjFusionModels.KLYSTRON_TEXTURE, context, "Klystron");

        poseStack.pushPose();
        poseStack.translate(0.0D, 2.5D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(blockEntity.getFan(partialTick)));
        poseStack.translate(0.0D, -2.5D, 0.0D);
        ObjFusionModels.renderKlystronPart(ObjFusionModels.KLYSTRON_TEXTURE, context, "Rotor");
        poseStack.popPose();
        poseStack.popPose();
    }
}
