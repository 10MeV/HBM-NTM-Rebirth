package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.hbm.ntm.entity.missile.MinerRocketEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MinerRocketRenderer extends EntityRenderer<MinerRocketEntity> {
    public MinerRocketRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.5F;
    }

    @Override
    public void render(MinerRocketEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        RenderSystem.disableCull();
        ObjMissilePartModels.MINER_ROCKET.renderAll(ObjMissilePartModels.MINER_ROCKET_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        RenderSystem.enableCull();
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MinerRocketEntity entity) {
        return ObjMissilePartModels.MINER_ROCKET_TEXTURE;
    }
}
