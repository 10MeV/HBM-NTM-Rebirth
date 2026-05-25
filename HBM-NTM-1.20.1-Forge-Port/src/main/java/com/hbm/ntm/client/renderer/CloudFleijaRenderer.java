package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjEffectModels;
import com.hbm.ntm.entity.effect.CloudFleijaEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CloudFleijaRenderer extends EntityRenderer<CloudFleijaEntity> {
    public CloudFleijaRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(CloudFleijaEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        double baseScale = (entity.getAge() + partialTick) * 2.0D;
        double ageScale = baseScale / entity.getMaxAge();

        poseStack.pushPose();
        double scale = ageScale * 1.2D;
        if (scale > 1.0D) {
            scale = Math.max(1.0D - (scale - 1.0D) * 5.0D, 0.0D);
        }
        scale *= 2.0D * baseScale;
        poseStack.scale((float) scale, (float) scale, (float) scale);
        ObjEffectModels.SPHERE_NEW.renderAllUntextured(poseStack, buffer, 0, 255, 255, 255);

        double outerScale = 1.05D;
        for (int i = 0; i < 3; i++) {
            poseStack.scale((float) outerScale, (float) outerScale, (float) outerScale);
            ObjEffectModels.SPHERE_NEW.renderAllUntextured(poseStack, buffer, 0, 32, 32, 255, true);
        }
        poseStack.popPose();

        poseStack.pushPose();
        double shockwave = 5.0D * baseScale;
        poseStack.scale((float) shockwave, (float) shockwave, (float) shockwave);
        int shockTint = Math.max(0, Math.min(255, (int) ((1.0D - ageScale) * 0.75D * 255.0D)));
        ObjEffectModels.SPHERE_NEW.renderAllUntextured(poseStack, buffer, shockTint, shockTint, shockTint, 255, true);
        poseStack.popPose();

        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CloudFleijaEntity entity) {
        return null;
    }
}
