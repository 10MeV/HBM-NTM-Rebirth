package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.TeslaBlockEntity;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjEntityModels;
import com.hbm.ntm.entity.mob.EntityTeslaCrab;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class TeslaCrabRenderer extends EntityRenderer<EntityTeslaCrab> {
    private static final LegacyWavefrontModel MODEL = ObjEntityModels.TESLACRAB;
    private static final ResourceLocation TEXTURE = ObjEntityModels.TESLACRAB_TEXTURE;
    private static final LegacyWavefrontModel.SelectionHandle BODY = MODEL.prepareRenderOnly("Body");
    private static final LegacyWavefrontModel.SelectionHandle FRONT = MODEL.prepareRenderOnly("Front");
    private static final LegacyWavefrontModel.SelectionHandle BACK = MODEL.prepareRenderOnly("Back");

    public TeslaCrabRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 1.0F;
    }

    @Override
    public void render(EntityTeslaCrab entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        renderBeams(entity, partialTick, poseStack, buffer);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.translate(0.0D, -1.5D, 0.0D);
        float legRot = legRotation(entity, partialTick);
        MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, BODY);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(legRot));
        MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, FRONT);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(legRot));
        MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, BACK);
        poseStack.popPose();
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTeslaCrab entity) {
        return TEXTURE;
    }

    private static float legRotation(EntityTeslaCrab entity, float partialTick) {
        float limbSwing = entity.walkAnimation.position(partialTick);
        float limbSwingAmount = Math.min(entity.walkAnimation.speed(partialTick), 1.0F);
        return -(Mth.cos(limbSwing * 0.6662F * 2.0F) * 0.4F) * limbSwingAmount * 57.3F;
    }

    private static void renderBeams(EntityTeslaCrab entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer) {
        Level level = entity.level();
        List<TeslaBlockEntity.TeslaTarget> targets = entity.getTeslaTargets();
        if (targets.isEmpty()) {
            return;
        }
        double sourceY = entity.getY() + 1.0D;
        int start = (int) (level.getGameTime() % 1000L) + 1;
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);
        LegacyBeamRenderer.DirectSolidBeamBatch beamBatch =
                LegacyBeamRenderer.directSolidBeamBatch(poseStack, buffer, false);
        for (TeslaBlockEntity.TeslaTarget target : targets) {
            double dx = target.x() - entity.getX();
            double dy = target.y() - sourceY;
            double dz = target.z() - entity.getZ();
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            LegacyBeamRenderer.solidBeam(beamBatch, dx, dy, dz, LegacyBeamRenderer.WaveType.RANDOM,
                    LegacyTileRenderPlans.TESLA_BEAM_COLOR, LegacyTileRenderPlans.TESLA_BEAM_COLOR,
                    start, (int) (length * 5.0D),
                    LegacyTileRenderPlans.TESLA_BEAM_SIZE, LegacyTileRenderPlans.TESLA_BEAM_LAYERS,
                    LegacyTileRenderPlans.TESLA_BEAM_THICKNESS);
        }
        poseStack.popPose();
    }
}
