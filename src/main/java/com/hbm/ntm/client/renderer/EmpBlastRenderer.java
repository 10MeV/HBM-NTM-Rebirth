package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjEffectModels;
import com.hbm.ntm.entity.effect.EmpBlastEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class EmpBlastRenderer extends EntityRenderer<EmpBlastEntity> {
    public EmpBlastRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(EmpBlastEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        float scale = entity.getScale() + partialTick;
        poseStack.pushPose();
        poseStack.scale(scale, 1.0F, scale);
        ObjEffectModels.RING.renderAll(ObjEffectModels.EMP_BLAST_TEXTURE, poseStack, buffer,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                255, 255, 255, 255, false, LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                LegacyWavefrontModel.UvTransform.DEFAULT);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EmpBlastEntity entity) {
        return ObjEffectModels.EMP_BLAST_TEXTURE;
    }
}
