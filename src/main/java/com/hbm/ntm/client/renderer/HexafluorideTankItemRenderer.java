package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HexafluorideTankBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class HexafluorideTankItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final HexafluorideTankItemRenderer INSTANCE = new HexafluorideTankItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private HexafluorideTankItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof HexafluorideTankBlock tankBlock)) {
            return;
        }
        poseStack.pushPose();
        applyLegacyItemTransform(displayContext, poseStack);
        HexafluorideTankRenderer.renderItemModel(tankBlock.kind(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyLegacyItemTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.625D, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.scale(0.0625F, 0.0625F, 0.0625F);
            poseStack.translate(0.0D, -4.0D, 0.0D);
            poseStack.scale(6.0F, 6.0F, 6.0F);
            return;
        }

        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.scale(1.5F, 1.5F, 1.5F);
        } else {
            poseStack.translate(0.5D, 0.25D, 0.0D);
        }
        poseStack.scale(0.25F, 0.25F, 0.25F);
        if (displayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                && displayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
    }
}
