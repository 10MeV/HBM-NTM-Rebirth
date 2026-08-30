package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyFanBlock;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** ItemRenderBase-equivalent renderer for the former TESR-only fan item. */
public final class LegacyFanItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final LegacyFanItemRenderer INSTANCE = new LegacyFanItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private LegacyFanItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof LegacyFanBlock)) {
            return;
        }
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            // RenderFan.ItemRenderBase: inventory base transform, then translate(0,-2.5,0), scale(5), scale(2).
            poseStack.translate(8.0D, 10.0D, 0.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, -30.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
            poseStack.translate(0.0D, -2.5D, 0.0D);
            poseStack.scale(-10.0F, -10.0F, -10.0F);
        } else {
            if (displayContext == ItemDisplayContext.GROUND) {
                poseStack.scale(1.5F, 1.5F, 1.5F);
            } else {
                poseStack.translate(0.5D, 0.25D, 0.0D);
            }
            poseStack.scale(0.5F, 0.5F, 0.5F);
            if (displayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    && displayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                    && displayContext != ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                    && displayContext != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
            }
        }
        ObjMachineModels.FAN.renderAll(ObjMachineModels.FAN_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
