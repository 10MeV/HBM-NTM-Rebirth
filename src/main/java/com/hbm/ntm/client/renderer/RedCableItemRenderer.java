package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class RedCableItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final RedCableItemRenderer INSTANCE = new RedCableItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private RedCableItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyDisplay(displayContext, poseStack);
        RedCableRenderer.renderItemCable(new ObjRenderContext(poseStack, buffer, Blocks.AIR.defaultBlockState(),
                packedLight, packedOverlay));
        poseStack.popPose();
    }

    private static void applyDisplay(ItemDisplayContext displayContext, PoseStack poseStack) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.scale(0.72F, 0.72F, 0.72F);
            return;
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.7F, 0.7F, 0.7F);
        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.scale(0.8F, 0.8F, 0.8F);
        } else if (displayContext.firstPerson()) {
            poseStack.scale(0.9F, 0.9F, 0.9F);
        }
    }
}
