package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyFileCabinetBlock;
import com.hbm.ntm.item.LegacyFileCabinetBlockItem;
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

public class LegacyFileCabinetItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final LegacyFileCabinetItemRenderer INSTANCE = new LegacyFileCabinetItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private LegacyFileCabinetItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof LegacyFileCabinetBlockItem item)
                || !(item.getBlock() instanceof LegacyFileCabinetBlock block)) {
            return;
        }
        poseStack.pushPose();
        applyDisplay(displayContext, poseStack);
        LegacyFileCabinetRenderer.renderItemModel(poseStack, buffer, block.defaultBlockState(), item.getVariant(stack),
                packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyDisplay(ItemDisplayContext displayContext, PoseStack poseStack) {
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.625D, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.scale(0.0625F, 0.0625F, 0.0625F);
            poseStack.translate(-1.0D, 0.5D, -1.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(4.0F, 4.0F, 4.0F);
            poseStack.translate(0.0D, -1.25D, 0.0D);
            poseStack.scale(2.75F, 2.75F, 2.75F);
            return;
        }

        poseStack.translate(0.5D, 0.25D, 0.5D);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        if (displayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                && displayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        }
    }
}
