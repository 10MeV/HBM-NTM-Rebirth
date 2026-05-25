package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjEffectModels;
import com.hbm.ntm.entity.effect.CloudSoliniumEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CloudSoliniumRenderer extends EntityRenderer<CloudSoliniumEntity> {
    private static final int RED = 0x27;
    private static final int GREEN = 0xFF;
    private static final int BLUE = 0xDA;

    public CloudSoliniumRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(CloudSoliniumEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        float scale = entity.getAge() + partialTick;
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        ObjEffectModels.SPHERE_NEW.renderAllUntextured(poseStack, buffer, RED, GREEN, BLUE, 255);

        double outerScale = 1.025D;
        for (int i = 0; i < 3; i++) {
            poseStack.scale((float) outerScale, (float) outerScale, (float) outerScale);
            ObjEffectModels.SPHERE_NEW.renderAllUntextured(poseStack, buffer, RED, GREEN, BLUE, 32, true);
        }
        poseStack.popPose();

        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CloudSoliniumEntity entity) {
        return null;
    }
}
