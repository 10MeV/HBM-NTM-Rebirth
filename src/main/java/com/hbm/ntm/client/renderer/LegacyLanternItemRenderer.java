package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyLanternBlock;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.client.obj.ObjTrinketModels;
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

public class LegacyLanternItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final LegacyLanternItemRenderer INSTANCE = new LegacyLanternItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());
    private static final LegacyWavefrontModel MODEL = ObjTrinketModels.LANTERN;
    private static final LegacyWavefrontModel.SelectionHandle LANTERN =
            MODEL.prepareRenderOnlyInCallOrder("Lantern");
    private static final LegacyWavefrontModel.SelectionHandle LIGHT =
            MODEL.prepareRenderOnlyInCallOrder("Light");

    private LegacyLanternItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof LegacyLanternBlock)) {
            return;
        }

        poseStack.pushPose();
        applyDisplay(displayContext, poseStack);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, Blocks.AIR.defaultBlockState(),
                packedLight, packedOverlay);
        MODEL.renderOnlyInCallOrder(ObjTrinketModels.LANTERN_TEXTURE, context, LANTERN);

        LegacyTileRenderPlans.LanternLightPlan lightPlan =
                LegacyTileRenderPlans.lanternLightPlan(System.currentTimeMillis());
        MODEL.renderOnlyUntextured(
                context.withRgb(lightPlan.red(), lightPlan.green(), lightPlan.blue()),
                LIGHT);
        poseStack.popPose();
    }

    private static void applyDisplay(ItemDisplayContext displayContext, PoseStack poseStack) {
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.scale(-0.19F, 0.19F, -0.19F);
            poseStack.translate(0.0D, -2.625D, 0.0D);
            return;
        }

        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext != ItemDisplayContext.FIXED) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
        float scale = displayContext == ItemDisplayContext.GROUND ? 0.12F : 0.18F;
        if (displayContext.firstPerson()) {
            scale = 0.14F;
        }
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0D, -2.625D, 0.0D);
    }
}
