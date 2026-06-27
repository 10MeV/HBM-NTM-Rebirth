package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.entity.projectile.ChemicalProjectileEntity;
import com.hbm.ntm.entity.projectile.ChemicalProjectileEntity.ChemicalStyle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.Random;

public class ChemicalProjectileRenderer extends EntityRenderer<ChemicalProjectileEntity> {
    private static final ResourceLocation PARTICLE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/particle_base.png");

    public ChemicalProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(ChemicalProjectileEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        ChemicalStyle style = entity.style();
        if (style == ChemicalStyle.AMAT || style == ChemicalStyle.LIGHTNING) {
            renderBeam(entity, partialTick, poseStack, buffer);
        } else if (style == ChemicalStyle.GAS || style == ChemicalStyle.GASFLAME) {
            renderGas(entity, style, partialTick, poseStack, buffer);
        }
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private void renderGas(ChemicalProjectileEntity entity, ChemicalStyle style, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer) {
        float exp = (entity.tickCount + partialTick) / Math.max(1.0F, entity.maxAge());
        float size = style == ChemicalStyle.GAS ? exp * 10.0F : exp * 2.0F;
        int alpha = style == ChemicalStyle.GAS ? (int) Math.max(127.0F * (1.0F - exp), 0.0F)
                : (int) Math.max(255.0F * (1.0F - exp), 0.0F);
        int color = style == ChemicalStyle.GAS
                ? entity.fluid().getColor()
                : Color.getHSBColor(Math.max((60.0F - exp * 100.0F) / 360.0F, 0.0F),
                        1.0F - exp * 0.25F, 1.0F - exp * 0.5F).getRGB();

        Random random = new Random(entity.getId());
        int uMirror = style == ChemicalStyle.GAS ? random.nextInt(2) : 0;
        int vMirror = style == ChemicalStyle.GAS ? random.nextInt(2) : 0;

        poseStack.pushPose();
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        LegacyTexturedQuadRenderer.quad(PARTICLE, poseStack, buffer, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, LegacyTexturedRenderMode.TRANSLUCENT, 0.0F, 0.0F, 1.0F,
                LegacyTexturedQuadRenderer.vertex(-size, -size, 0.0F, 1 - uMirror, 1 - vMirror,
                        color, alpha),
                LegacyTexturedQuadRenderer.vertex(size, -size, 0.0F, uMirror, 1 - vMirror,
                        color, alpha),
                LegacyTexturedQuadRenderer.vertex(size, size, 0.0F, uMirror, vMirror,
                        color, alpha),
                LegacyTexturedQuadRenderer.vertex(-size, size, 0.0F, 1 - uMirror, vMirror,
                        color, alpha));
        poseStack.popPose();
    }

    private static void renderBeam(ChemicalProjectileEntity entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer) {
        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        Vec3 motion = entity.getDeltaMovement();
        double length = motion.length() * (entity.tickCount + partialTick) * 0.75D;
        double size = 0.0625D;
        float alpha = 0.2F;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch - 90.0F));
        beamFace(poseStack, buffer, -size, -size, size, -size, length, alpha);
        beamFace(poseStack, buffer, -size, size, size, size, length, alpha);
        beamFace(poseStack, buffer, -size, -size, -size, size, length, alpha);
        beamFace(poseStack, buffer, size, -size, size, size, length, alpha);
        poseStack.popPose();
    }

    private static void beamFace(PoseStack poseStack, MultiBufferSource buffer, double x0, double z0, double x1, double z1,
            double length, float alpha) {
        int nearAlpha = LegacyUntexturedQuadRenderer.alpha(alpha);
        LegacyUntexturedQuadRenderer.quad(poseStack, buffer, LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                x0, 0.0D, z0,
                x1, 0.0D, z1,
                x1, length, z1,
                x0, length, z0,
                0xFFFFFF, nearAlpha, nearAlpha, 0, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(ChemicalProjectileEntity entity) {
        return PARTICLE;
    }
}
