package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjSoyuzModels;
import com.hbm.ntm.entity.missile.SoyuzCapsuleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SoyuzCapsuleRenderer extends EntityRenderer<SoyuzCapsuleEntity> {
    public SoyuzCapsuleRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.8F;
    }

    @Override
    public void render(SoyuzCapsuleEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float wobble = (float) Math.sin((entity.tickCount + partialTick) * 0.05F) * 7.0F;
        poseStack.mulPose(Axis.ZP.rotationDegrees(wobble));
        poseStack.mulPose(Axis.XP.rotationDegrees(wobble));
        ObjSoyuzModels.renderLanderCapsule(false, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        ObjSoyuzModels.renderLanderChute(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SoyuzCapsuleEntity entity) {
        return ObjSoyuzModels.LANDER_TEXTURE;
    }
}
