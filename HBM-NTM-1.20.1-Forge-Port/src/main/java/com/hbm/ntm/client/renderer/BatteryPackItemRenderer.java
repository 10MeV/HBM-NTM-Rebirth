package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;

public class BatteryPackItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation BATTERY_MODEL = new ResourceLocation(HbmNtm.MOD_ID, "block/machines/battery_pack_battery");
    private static final ResourceLocation CAPACITOR_MODEL = new ResourceLocation(HbmNtm.MOD_ID, "block/machines/battery_pack_capacitor");

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

        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getModelManager().getModel(batteryPack.isCapacitor() ? CAPACITOR_MODEL : BATTERY_MODEL);
        RenderType renderType = RenderType.entityCutoutNoCull(
                new ResourceLocation(HbmNtm.MOD_ID, "textures/block/machines/" + batteryPack.getLegacyTextureName() + ".png"));
        minecraft.getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(renderType),
                null,
                model,
                1.0F,
                1.0F,
                1.0F,
                packedLight,
                packedOverlay,
                ModelData.EMPTY,
                renderType);

        poseStack.popPose();
    }

    private static void applyDisplay(ItemDisplayContext displayContext, PoseStack poseStack) {
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.scale(-5.0F, 5.0F, -5.0F);
            poseStack.translate(-0.5D, -0.5D, -0.5D);
            return;
        }

        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(1.8F, 1.8F, 1.8F);
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(0.5D, 0.0D, 0.5D);
        } else if (displayContext.firstPerson()) {
            poseStack.scale(0.75F, 0.75F, 0.75F);
        }
    }
}
