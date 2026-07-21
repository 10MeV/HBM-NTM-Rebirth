package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.DecoCrtBlock;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.DecoCrtBlockItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Item equivalent of RenderCRT's old 3D inventory path. */
public final class DecoCrtItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final DecoCrtItemRenderer INSTANCE = new DecoCrtItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private DecoCrtItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof DecoCrtBlockItem item) || !(item.getBlock() instanceof DecoCrtBlock block)) {
            return;
        }
        poseStack.pushPose();
        applyDisplay(displayContext, poseStack);
        DecoCrtRenderer.renderItemModel(block.defaultBlockState(), item.getVariant(stack), poseStack, buffer,
                packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyDisplay(ItemDisplayContext context, PoseStack poseStack) {
        if (context == ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.625D, 0.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
            poseStack.scale(1.45F, 1.45F, 1.45F);
            return;
        }
        poseStack.translate(0.5D, 0.25D, 0.5D);
        poseStack.scale(0.55F, 0.55F, 0.55F);
        if (context != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                && context != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        }
    }
}
