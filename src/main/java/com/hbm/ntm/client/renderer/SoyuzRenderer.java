package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjSoyuzModels;
import com.hbm.ntm.client.obj.ObjSoyuzModels.SoyuzTextureSet;
import com.hbm.ntm.entity.missile.SoyuzEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SoyuzRenderer extends EntityRenderer<SoyuzEntity> {
    public SoyuzRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 2.5F;
    }

    @Override
    public void render(SoyuzEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        ObjSoyuzModels.renderSoyuz(textureSet(entity.skin()), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SoyuzEntity entity) {
        return textureSet(entity.skin()).engineBlock();
    }

    private static SoyuzTextureSet textureSet(int skin) {
        return switch (skin) {
            case 1 -> ObjSoyuzModels.LUNA_TEXTURES;
            case 2 -> ObjSoyuzModels.AUTHENTIC_TEXTURES;
            default -> ObjSoyuzModels.SOYUZ_TEXTURES;
        };
    }
}
