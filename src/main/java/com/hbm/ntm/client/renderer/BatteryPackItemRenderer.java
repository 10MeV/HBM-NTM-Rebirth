package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BatteryPackItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation(HbmNtm.MOD_ID, "models/machines/battery.obj");
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/battery_lead.png");
    private static final LegacyWavefrontModel MODEL = new LegacyWavefrontModel(MODEL_LOCATION, DEFAULT_TEXTURE).asVBO();

    public static final BatteryPackItemRenderer INSTANCE = new BatteryPackItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private BatteryPackItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof HbmBatteryPackItem batteryPack)) {
            return;
        }

        poseStack.pushPose();
        applyDisplay(displayContext, poseStack);

        ResourceLocation texture = new ResourceLocation(HbmNtm.MOD_ID,
                "textures/models/machines/" + batteryPack.getLegacyTextureName() + ".png");
        MODEL.renderPart(batteryPack.isCapacitor() ? "Capacitor" : "Battery",
                texture,
                poseStack,
                buffer,
                packedLight,
                packedOverlay);

        poseStack.popPose();
    }

    private static void applyDisplay(ItemDisplayContext displayContext, PoseStack poseStack) {
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.scale(-0.36F, 0.36F, -0.36F);
            poseStack.translate(0.0D, -1.0D, 0.0D);
            return;
        }

        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.32F, 0.32F, 0.32F);
        poseStack.translate(0.0D, -1.0D, 0.0D);

        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.scale(1.5F, 1.5F, 1.5F);
        } else if (displayContext.firstPerson()) {
            poseStack.scale(0.9F, 0.9F, 0.9F);
        }
    }
}
