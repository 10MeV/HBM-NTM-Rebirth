package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.SoyuzLauncherBlockEntity;
import com.hbm.ntm.client.obj.ObjLaunchModels;
import com.hbm.ntm.client.obj.ObjSoyuzModels;
import com.hbm.ntm.satellite.SoyuzRocketItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

public class SoyuzLauncherRenderer implements BlockEntityRenderer<SoyuzLauncherBlockEntity> {
    public SoyuzLauncherRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(SoyuzLauncherBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(SoyuzLauncherBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(SoyuzLauncherBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        int modelLight = blockEntity.getBlockState().getBlock() instanceof LegacyVisibleMultiblockMachineBlock machine
                ? LegacyRenderLighting.resolveMachineLight(blockEntity, blockEntity.getBlockState(),
                        machine.definition(), packedLight)
                : LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, -4.0D, 0.5D);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity);
                var lightingScope = LegacyRenderLighting.pushModelViewSampling(blockEntity,
                        poseStack.last().pose())) {
            renderLauncher(blockEntity.getTowerRotation(partialTick), poseStack, buffer, modelLight, packedLight);
            if (SoyuzRocketItem.isValidSkin(blockEntity.getRocketType())) {
                poseStack.translate(0.0D, 5.0D, 0.0D);
                ObjSoyuzModels.renderSoyuz(ObjSoyuzModels.textureSetForSkin(blockEntity.getRocketType()), poseStack,
                        buffer, packedLight, OverlayTexture.NO_OVERLAY);
            }
        }
        poseStack.popPose();
    }

    private static void renderLauncher(float rotation, PoseStack poseStack, MultiBufferSource buffer, int fixedLight,
            int activityLight) {
        // Keep SoyuzLauncherPronter's exact fixed/moving call order on the existing
        // noSmooth prepared-VBO models.  ObjLaunchModels' default route is no-cull.
        ObjLaunchModels.renderSoyuzLauncher(rotation, poseStack, buffer, fixedLight, activityLight,
                OverlayTexture.NO_OVERLAY);
    }
}
