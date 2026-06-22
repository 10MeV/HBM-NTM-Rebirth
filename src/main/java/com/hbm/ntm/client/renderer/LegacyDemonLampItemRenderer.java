package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyDemonLampBlock;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjLightModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
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
import net.minecraft.world.level.block.Blocks;

public class LegacyDemonLampItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final LegacyDemonLampItemRenderer INSTANCE = new LegacyDemonLampItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private LegacyDemonLampItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof LegacyDemonLampBlock)) {
            return;
        }

        poseStack.pushPose();
        applyDisplay(displayContext, poseStack);
        ObjLightModels.DEMON_LAMP_LEGACY.renderAll(new ObjRenderContext(poseStack, buffer,
                Blocks.AIR.defaultBlockState(), packedLight, packedOverlay)
                .withRenderMode(LegacyTexturedRenderMode.CUTOUT_CULL));
        poseStack.popPose();
    }

    private static void applyDisplay(ItemDisplayContext displayContext, PoseStack poseStack) {
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.625D, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.scale(-0.0625F, -0.0625F, -0.0625F);
            poseStack.translate(0.0D, -3.0D, 0.0D);
            poseStack.scale(8.0F, 8.0F, 8.0F);
            return;
        }

        poseStack.translate(0.5D, 0.25D, 0.0D);
        if (displayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                && displayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
        float scale = displayContext == ItemDisplayContext.GROUND ? 0.375F : 0.25F;
        if (displayContext.firstPerson()) {
            scale = 0.22F;
        }
        poseStack.scale(scale, scale, scale);
    }
}
