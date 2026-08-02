package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjEntityModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.entity.mob.EntityBlockSpider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Renders the legacy animated OBJ legs and its separately synced block-state body. */
public final class BlockSpiderRenderer extends EntityRenderer<EntityBlockSpider> {
    private static final LegacyWavefrontModel MODEL = ObjEntityModels.BLOCKSPIDER;
    private static final ResourceLocation TEXTURE = ObjEntityModels.BLOCKSPIDER_TEXTURE;
    private static final LegacyWavefrontModel.SelectionHandle ODD_LEGS =
            MODEL.prepareRenderOnly("Leg1", "Leg3", "Leg5", "Leg7");
    private static final LegacyWavefrontModel.SelectionHandle EVEN_LEGS =
            MODEL.prepareRenderOnly("Leg2", "Leg4", "Leg6", "Leg8");
    private final BlockRenderDispatcher blockRenderer;

    public BlockSpiderRenderer(EntityRendererProvider.Context context) {
        super(context);
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
        shadowRadius = 1.0F;
    }

    @Override
    public void render(EntityBlockSpider spider, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        float limbSwing = spider.walkAnimation.position(partialTick);
        float limbSwingAmount = Math.min(spider.walkAnimation.speed(partialTick), 1.0F);
        float rotation = -(Mth.cos(limbSwing * 0.6662F * 2.0F) * 0.4F) * limbSwingAmount * 57.3F;

        poseStack.pushPose();
        // RenderLiving's inherited corpse yaw, followed by ModelBlockSpider's exact local transform.
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F - Mth.rotLerp(partialTick, spider.yBodyRotO, spider.yBodyRot));
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, 180.0F);
        poseStack.translate(0.0D, -1.5D, 0.0D);

        poseStack.pushPose();
        poseStack.translate(0.0D, rotation * 0.005D, 0.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, rotation);
        MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, ODD_LEGS);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, rotation * -0.005D, 0.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, -rotation);
        MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, EVEN_LEGS);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.75D, 0.0D);
        HbmClientRenderUtil.renderSingleBlock(blockRenderer, spider.bodyState(), poseStack, buffer, packedLight);
        poseStack.popPose();
        poseStack.popPose();
        super.render(spider, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBlockSpider spider) {
        return TEXTURE;
    }
}
