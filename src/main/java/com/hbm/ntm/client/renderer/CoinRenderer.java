package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.entity.projectile.CoinEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CoinRenderer extends EntityRenderer<CoinEntity> {
    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation(HbmNtm.MOD_ID,
            "models/trinkets/chip.obj");
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/trinkets/chip_gold.png");
    private static final LegacyWavefrontModel MODEL = new LegacyWavefrontModel(MODEL_LOCATION, TEXTURE).asVBO();

    public CoinRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(CoinEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, -(entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTick
                - 90.0F));
        LegacyPoseRotations.rotateZDegrees(poseStack, (entity.tickCount + partialTick) * 45.0F);
        poseStack.scale(0.125F, 0.125F, 0.125F);
        MODEL.renderAll(TEXTURE, poseStack, buffer, packedLight, 0);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CoinEntity entity) {
        return TEXTURE;
    }
}
