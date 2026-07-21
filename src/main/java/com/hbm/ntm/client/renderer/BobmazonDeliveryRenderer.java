package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.entity.missile.BobmazonDeliveryEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BobmazonDeliveryRenderer extends EntityRenderer<BobmazonDeliveryEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/bobmazon.png");

    public BobmazonDeliveryRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(BobmazonDeliveryEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        RenderSystem.disableCull();
        LegacyPoseRotations.rotateXDegrees(poseStack, 180.0F);
        ObjMissilePartModels.MINER_ROCKET.renderAll(TEXTURE, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        RenderSystem.enableCull();
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BobmazonDeliveryEntity entity) {
        return TEXTURE;
    }
}
