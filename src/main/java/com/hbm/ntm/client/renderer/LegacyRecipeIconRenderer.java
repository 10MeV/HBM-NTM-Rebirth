package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.IdentityHashMap;
import java.util.Map;

public final class LegacyRecipeIconRenderer {
    private static final double RECIPE_ICON_RANGE = 35.0D * 35.0D;
    private static final Map<Block, Boolean> BLOCK_ITEM_GUI_3D = new IdentityHashMap<>();

    private LegacyRecipeIconRenderer() {
    }

    public static void clearModelCache() {
        BLOCK_ITEM_GUI_3D.clear();
    }

    static boolean shouldRender(BlockEntity blockEntity) {
        return shouldRenderAtDistance(playerDistanceSq(blockEntity));
    }

    static boolean shouldRenderWithin(BlockEntity blockEntity, double range) {
        return shouldRenderWithinDistance(playerDistanceSq(blockEntity), range);
    }

    static double playerDistanceSq(BlockEntity blockEntity) {
        if (blockEntity == null || blockEntity.getLevel() == null) {
            return Double.POSITIVE_INFINITY;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return Double.POSITIVE_INFINITY;
        }
        BlockPos pos = blockEntity.getBlockPos();
        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
    }

    static boolean shouldRenderAtDistance(double distanceSq) {
        return distanceSq < RECIPE_ICON_RANGE;
    }

    static boolean shouldRenderWithinDistance(double distanceSq, double range) {
        return distanceSq < range * range;
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
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.translate(0.0D, 1.0625D, 0.0D);
        if (stack.getItem() instanceof BlockItem blockItem) {
            if (blockItemGui3d(minecraft, blockItem)) {
                poseStack.translate(0.0D, -0.0625D, 0.0D);
            } else {
                poseStack.translate(0.0D, -0.125D, 0.0D);
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
        } else {
            LegacyPoseRotations.rotateXDegrees(poseStack, -90.0F);
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

    static void renderPlasmaForgeIcon(GenericMachineRecipe recipe, Level level, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, float partialTick) {
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
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.translate(0.0D, 1.75D, 0.0D);
        if (stack.getItem() instanceof BlockItem blockItem) {
            if (blockItemGui3d(minecraft, blockItem)) {
                poseStack.translate(0.0D, -0.0625D, 0.0D);
            } else {
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
        } else {
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        }
        float ticks = minecraft.player == null ? level.getGameTime() + partialTick
                : minecraft.player.tickCount + partialTick;
        poseStack.translate(0.0D, Math.sin(ticks * 0.1D) * 0.0625D, 0.0D);
        poseStack.scale(1.5F, 1.5F, 1.5F);
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

    private static boolean blockItemGui3d(Minecraft minecraft, BlockItem blockItem) {
        Block block = blockItem.getBlock();
        Boolean cached = BLOCK_ITEM_GUI_3D.get(block);
        if (cached != null) {
            return cached;
        }
        BakedModel blockModel = minecraft.getBlockRenderer().getBlockModel(block.defaultBlockState());
        boolean gui3d = blockModel.isGui3d();
        BLOCK_ITEM_GUI_3D.put(block, gui3d);
        return gui3d;
    }
}
