package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.SoyuzLauncherBlockEntity;
import com.hbm.ntm.client.obj.ObjLaunchModels;
import com.hbm.ntm.client.obj.ObjSoyuzModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class SoyuzLauncherRenderer implements BlockEntityRenderer<SoyuzLauncherBlockEntity> {
    public SoyuzLauncherRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(SoyuzLauncherBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(SoyuzLauncherBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int modelLight = blockEntity.getBlockState().getBlock() instanceof LegacyVisibleMultiblockMachineBlock machine
                ? LegacyRenderLighting.resolveMachineLight(blockEntity, blockEntity.getBlockState(),
                        machine.definition(), packedLight)
                : LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, -4.0D, 0.5D);
        renderLauncher(blockEntity.getTowerRotation(partialTick), poseStack, buffer, modelLight);
        if (blockEntity.getRocketType() >= 0) {
            poseStack.translate(0.0D, 5.0D, 0.0D);
            ObjSoyuzModels.renderSoyuz(ObjSoyuzModels.textureSetForSkin(blockEntity.getRocketType()), poseStack, buffer,
                    modelLight, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
    }

    private static void renderLauncher(float rotation, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ObjLaunchModels.renderSoyuzLauncher(rotation, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
    }
}
