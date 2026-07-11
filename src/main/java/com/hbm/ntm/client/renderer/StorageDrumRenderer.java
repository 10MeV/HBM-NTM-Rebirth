package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.blockentity.StorageDrumBlockEntity;
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
        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext == ItemDisplayContext.GUI) {
            LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
            poseStack.scale(0.7F, 0.7F, 0.7F);
        } else {
            poseStack.scale(0.9F, 0.9F, 0.9F);
        }
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        render(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjMachineModels.DRUM.render(poseStack, buffer, packedLight, packedOverlay, 0xFFFFFF);
    }
}
