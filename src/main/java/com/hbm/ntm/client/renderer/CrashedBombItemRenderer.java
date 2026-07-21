package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.CrashedBombBlock;
import com.hbm.ntm.block.CrashedBombType;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.CrashedBombBlockItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/** Item bridge for the old metadata-selected dud models. */
public final class CrashedBombItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float GUI_OCCUPANCY = 0.86F;
    public static final CrashedBombItemRenderer INSTANCE = new CrashedBombItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private CrashedBombItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof CrashedBombBlock)) {
            return;
        }
        CrashedBombType type = CrashedBombBlockItem.getType(stack);
        LegacyWavefrontModel model = CrashedBombRenderer.model(type);
        AABB bounds = model.boundsAll();
        double size = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
        float scale = (float) Math.max(0.02D, Math.min(0.32D, GUI_OCCUPANCY / Math.max(1.0D, size)));

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext == ItemDisplayContext.GUI) {
            LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
        } else {
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            if (displayContext.firstPerson()) {
                poseStack.scale(0.85F, 0.85F, 0.85F);
            } else if (displayContext == ItemDisplayContext.GROUND) {
                poseStack.translate(0.0D, -0.25D, 0.0D);
                poseStack.scale(0.8F, 0.8F, 0.8F);
            }
        }
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-(bounds.minX + bounds.maxX) * 0.5D, -(bounds.minY + bounds.maxY) * 0.5D,
                -(bounds.minZ + bounds.maxZ) * 0.5D);
        // RenderCrashedBomb ItemRenderBase: renderCommonWithStack rotates all variants around Y by 90 degrees.
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        if (type == CrashedBombType.CONVENTIONAL) {
            poseStack.translate(0.0D, 0.0D, -0.5D);
        }
        CrashedBombRenderer.renderModel(type, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
