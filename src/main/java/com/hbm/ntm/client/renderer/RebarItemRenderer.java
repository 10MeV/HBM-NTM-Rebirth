package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Item renderer for the old inventory-only twelve-bar rebar lattice. */
public final class RebarItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final RebarItemRenderer INSTANCE = new RebarItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private RebarItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext == ItemDisplayContext.GUI) {
            LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
            poseStack.scale(0.72F, 0.72F, 0.72F);
        } else {
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            poseStack.scale(displayContext.firstPerson() ? 0.9F : 0.7F,
                    displayContext.firstPerson() ? 0.9F : 0.7F, displayContext.firstPerson() ? 0.9F : 0.7F);
        }
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        RebarBlockEntityRenderer.renderInventory(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
