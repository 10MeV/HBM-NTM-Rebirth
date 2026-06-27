package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.FusionMHDTBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjFusionModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class FusionMHDTRenderer implements BlockEntityRenderer<FusionMHDTBlockEntity> {
    public FusionMHDTRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(FusionMHDTBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(FusionMHDTBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(FusionBreederRenderer.rotation(state)));
        ObjFusionModels.renderMhdtPart(ObjFusionModels.MHDT_LEGACY, ObjFusionModels.MHDT_TEXTURE,
                poseStack, buffer, light, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, "Turbine");

        poseStack.pushPose();
        float rotor = blockEntity.getRotor(partialTick) % 15.0F;
        poseStack.translate(0.0D, 1.5D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(rotor));
        poseStack.translate(0.0D, -1.5D, 0.0D);
        ObjFusionModels.renderMhdtPart(ObjFusionModels.MHDT_LEGACY, ObjFusionModels.MHDT_TEXTURE,
                poseStack, buffer, light, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, "Coils");
        poseStack.popPose();
        poseStack.popPose();
    }
}
