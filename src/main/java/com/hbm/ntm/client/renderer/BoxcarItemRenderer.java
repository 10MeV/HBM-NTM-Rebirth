package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.BoxcarBlock;
import com.hbm.ntm.client.obj.ObjEntityModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Modern BEWLR equivalent of ItemRenderLibrary's boxcar entry. */
public final class BoxcarItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final BoxcarItemRenderer INSTANCE = new BoxcarItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private BoxcarItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof BoxcarBlock)) {
            return;
        }
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack);
        ObjEntityModels.BOXCAR.renderAll(ObjEntityModels.BOXCAR_TEXTURE, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void applyDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
        if (displayContext == ItemDisplayContext.GUI) {
            // ItemRenderBase inventory transform, then its boxcar-specific Y rotation / offset / scales.
            poseStack.translate(0.5D, 0.625D, 0.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
            poseStack.scale(0.0625F, 0.0625F, 0.0625F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
            poseStack.translate(0.0D, -1.0D, 0.0D);
            poseStack.scale(2.0F, 2.0F, 2.0F);
            return;
        }

        poseStack.translate(0.5D, 0.25D, 0.5D);
        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.scale(0.375F, 0.375F, 0.375F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
            return;
        }

        poseStack.scale(0.125F, 0.125F, 0.125F);
        if (displayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                && displayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        }
    }
}
