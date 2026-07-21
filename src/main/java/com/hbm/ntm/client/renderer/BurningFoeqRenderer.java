package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjUtilityModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.entity.projectile.BurningFoeqEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Random;

/** Source-shaped RenderFOEQ renderer using the shared prepared-OBJ backend. */
public final class BurningFoeqRenderer extends EntityRenderer<BurningFoeqEntity> {
    public BurningFoeqRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    /**
     * EntityBurningFOEQ returned the 1.7.10 full-bright lightmap (0xF000F0).
     * Modern entity dispatchers derive that packed value from these two renderer
     * light queries, before the shared OBJ backend receives {@code packedLight}.
     */
    @Override
    protected int getBlockLightLevel(BurningFoeqEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(BurningFoeqEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(BurningFoeqEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, -75.0D, 0.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, 180.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, Mth.lerp(partialTick, entity.xRotO, entity.getXRot()));

        ObjUtilityModels.SAT_FOEQ_BURNING.renderAll(ObjUtilityModels.SAT_FOEQ_BURNING_TEXTURE, poseStack, buffer,
                packedLight, OverlayTexture.NO_OVERLAY, 255, 255, 255, 255, false,
                LegacyTexturedRenderMode.CUTOUT_CULL, LegacyWavefrontModel.UvTransform.DEFAULT);

        Random random = new Random(System.currentTimeMillis() / 50L);
        poseStack.scale(1.15F, 0.75F, 1.15F);
        poseStack.translate(0.0D, -0.5D, 0.3D);
        for (int i = 0; i < 10; i++) {
            renderFire(poseStack, buffer, packedLight, random, 255, 191, 64);
            poseStack.translate(0.0D, 2.0D, 0.0D);
            renderFire(poseStack, buffer, packedLight, random, 255, 128, 0);
            poseStack.translate(0.0D, 2.0D, 0.0D);
            renderFire(poseStack, buffer, packedLight, random, 255, 64, 0);
            poseStack.translate(0.0D, 2.0D, 0.0D);
            renderFire(poseStack, buffer, packedLight, random, 255, 38, 0);
            poseStack.translate(0.0D, -3.8D, 0.0D);
            poseStack.scale(0.95F, 1.2F, 0.95F);
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void renderFire(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Random random,
            int red, int green, int blue) {
        LegacyPoseRotations.rotateYDegrees(poseStack, random.nextInt(360));
        ObjUtilityModels.SAT_FOEQ_FIRE.renderAll(ObjUtilityModels.SAT_FOEQ_FIRE_TEXTURE, poseStack, buffer,
                packedLight, OverlayTexture.NO_OVERLAY, red, green, blue, 255, false,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, LegacyWavefrontModel.UvTransform.DEFAULT);
    }

    @Override
    public ResourceLocation getTextureLocation(BurningFoeqEntity entity) {
        return ObjUtilityModels.SAT_FOEQ_BURNING_TEXTURE;
    }
}
