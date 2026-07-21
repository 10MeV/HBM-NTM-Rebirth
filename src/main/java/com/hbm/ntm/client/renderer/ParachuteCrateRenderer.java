package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.obj.ObjSoyuzModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.item.ParachuteCrateEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** RenderParachuteCrate's whole-tick chute wobble and crate-before-chute ordering. */
public class ParachuteCrateRenderer extends EntityRenderer<ParachuteCrateEntity> {
    private static final ResourceLocation SUPPLY_CRATE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/crate_can.png");
    public ParachuteCrateRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius = 0.0F; }
    @Override public void render(ParachuteCrateEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        double time = entity.level().getGameTime();
        poseStack.pushPose(); poseStack.translate(0.0D, 7.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (Math.sin(time * 0.05D) * 5.0D));
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (Math.sin(time * 0.05D + Math.PI * 0.5D) * 5.0D));
        poseStack.translate(0.0D, -7.0D, 0.0D);
        ObjBlockModels.CONSERVE_CRATE.renderAll(SUPPLY_CRATE_TEXTURE, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.translate(0.0D, -1.0D, 0.0D);
        ObjSoyuzModels.renderLanderChute(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }
    @Override public ResourceLocation getTextureLocation(ParachuteCrateEntity entity) { return ObjSoyuzModels.LANDER_TEXTURE; }
}
