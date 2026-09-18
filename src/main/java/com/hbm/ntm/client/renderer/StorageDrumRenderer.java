package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.blockentity.StorageDrumBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public class StorageDrumRenderer implements BlockEntityRenderer<StorageDrumBlockEntity> {
    public StorageDrumRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(StorageDrumBlockEntity blockEntity, Vec3 cameraPos) {
        return false;
    }

    @Override
    public void render(StorageDrumBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // World geometry is baked into the chunk mesh; keep this class for the item BEWLR helper.
    }

    public static void renderItem(ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            // 1.7.10 ItemRenderBase inventory projection, with the project-wide
            // opt-in centered GUI baseline requested for this item. The drum's
            // own transform remains T(0,-3,0) -> S(5) -> common S(2).
            poseStack.translate(0.5D, 0.375D, 0.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
            poseStack.scale(0.0625F, 0.0625F, 0.0625F);
            poseStack.translate(0.0D, -3.0D, 0.0D);
            poseStack.scale(5.0F, 5.0F, 5.0F);
            poseStack.scale(2.0F, 2.0F, 2.0F);
        } else {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.scale(0.9F, 0.9F, 0.9F);
            poseStack.translate(-0.5D, -0.5D, -0.5D);
        }
        render(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjMachineModels.DRUM_LEGACY.renderAll(ObjMachineModels.DRUM_GRAY_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
    }
}
