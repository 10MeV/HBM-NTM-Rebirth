package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjSoyuzModels;
import com.hbm.ntm.entity.missile.SoyuzEntity;
import com.hbm.ntm.satellite.SoyuzRocketItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SoyuzRenderer extends EntityRenderer<SoyuzEntity> {
    public SoyuzRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(SoyuzEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        if (SoyuzRocketItem.isValidSkin(entity.skin())) {
            poseStack.pushPose();
            ObjSoyuzModels.renderSoyuz(ObjSoyuzModels.textureSetForSkin(entity.skin()), poseStack, buffer, packedLight,
                    OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SoyuzEntity entity) {
        // Legacy RenderSoyuz returns the base payload texture here.  The
        // renderer itself still selects every skin-specific part texture.
        return ObjSoyuzModels.SOYUZ_TEXTURES.payload();
    }
}
