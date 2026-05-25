package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjEffectModels;
import com.hbm.ntm.entity.effect.CloudFleijaRainbowEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CloudFleijaRainbowRenderer extends EntityRenderer<CloudFleijaRainbowEntity> {
    public CloudFleijaRainbowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(CloudFleijaRainbowEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        float scale = entity.getAge();

        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);

        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderRandomSphere(entity, poseStack, buffer, 255, false);
        poseStack.popPose();

        for (float outer = 0.6F; outer <= 1.001F; outer += 0.1F) {
            poseStack.pushPose();
            poseStack.scale(outer, outer, outer);
            renderRandomSphere(entity, poseStack, buffer, 255, true);
            poseStack.popPose();
        }

        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void renderRandomSphere(CloudFleijaRainbowEntity entity, PoseStack poseStack,
            MultiBufferSource buffer, int alpha, boolean additive) {
        int red = entity.level().random.nextInt(0x100);
        int green = entity.level().random.nextInt(0x100);
        int blue = entity.level().random.nextInt(0x100);
        ObjEffectModels.SPHERE.renderAllUntextured(poseStack, buffer, red, green, blue, alpha, additive);
    }

    @Override
    public ResourceLocation getTextureLocation(CloudFleijaRainbowEntity entity) {
        return null;
    }
}
