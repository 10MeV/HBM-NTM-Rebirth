package com.hbm.ntm.client.renderer;

import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

final class LegacyRecipeIconRenderer {
    private static final double RECIPE_ICON_RANGE = 35.0D * 35.0D;

    private LegacyRecipeIconRenderer() {
    }

    static boolean shouldRender(BlockEntity blockEntity) {
        Minecraft minecraft = Minecraft.getInstance();
        return blockEntity.getLevel() != null
                && minecraft.player != null
                && minecraft.player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 1.0D,
                blockEntity.getBlockPos().getZ() + 0.5D) < RECIPE_ICON_RANGE;
    }

    static void renderInLegacyMachineSpace(GenericMachineRecipe recipe, Level level, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        if (recipe == null || level == null) {
            return;
        }
        ItemStack stack = recipe.getIcon();
        if (stack.isEmpty()) {
            return;
        }
        stack.setCount(1);

        Minecraft minecraft = Minecraft.getInstance();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.translate(0.0D, 1.0625D, 0.0D);
        if (stack.getItem() instanceof BlockItem blockItem) {
            BakedModel blockModel = minecraft.getBlockRenderer().getBlockModel(blockItem.getBlock().defaultBlockState());
            if (blockModel.isGui3d()) {
                poseStack.translate(0.0D, -0.0625D, 0.0D);
            } else {
                poseStack.translate(0.0D, -0.125D, 0.0D);
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.translate(0.0D, -0.25D, 0.0D);
        }
        poseStack.scale(1.25F, 1.25F, 1.25F);
        minecraft.getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                level,
                0);
        poseStack.popPose();
    }
}
