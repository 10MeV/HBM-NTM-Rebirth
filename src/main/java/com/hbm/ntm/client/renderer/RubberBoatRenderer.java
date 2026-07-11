package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.model.RubberBoatModel;
import com.hbm.ntm.entity.item.RubberBoatEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RubberBoatRenderer extends EntityRenderer<RubberBoatEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/boat_rubber.png");

    private final RubberBoatModel model;

    public RubberBoatRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
        model = new RubberBoatModel(RubberBoatModel.createLayer().bakeRoot());
    }

    @Override
    public void render(RubberBoatEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        float hitTime = entity.getTimeSinceHit() - partialTick;
        float damage = entity.getDamageTaken() - partialTick;
        if (damage < 0.0F) {
            damage = 0.0F;
        }
        if (hitTime > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hitTime) * hitTime * damage / 10.0F
                    * entity.getForwardDirection()));
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && entity.hasPassenger(player)) {
            float diff = Mth.wrapDegrees(entity.getYRot() - entity.prevRenderYaw);
            player.setYRot(player.getYRot() + diff);
            player.setYHeadRot(player.getYHeadRot() + diff);
        }
        entity.prevRenderYaw = entity.getYRot();

        float scale = 0.75F;
        poseStack.scale(scale, scale, scale);
        poseStack.scale(1.0F / scale, 1.0F / scale, 1.0F / scale);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        model.setupAnim(entity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F);
        model.renderToBuffer(poseStack, buffer.getBuffer(model.renderType(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RubberBoatEntity entity) {
        return TEXTURE;
    }
}
