package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.FusionTorusBlockEntity;
import com.hbm.ntm.client.obj.ObjFusionModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class FusionTorusRenderer implements BlockEntityRenderer<FusionTorusBlockEntity> {
    private static final double EXTRA_PLASMA_LAYER_DISTANCE_SQ = 100.0D * 100.0D;

    public FusionTorusRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(FusionTorusBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(FusionTorusBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, blockEntity.getBlockState(), light, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        if (blockEntity.isTilted()) {
            poseStack.translate(0.0D, -1.0D, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(10.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(5.0F));
        }
        ObjFusionModels.TORUS_LEGACY.renderOnly(ObjFusionModels.TORUS_TEXTURE, context, "Torus");

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(blockEntity.getMagnet(partialTick)));
        ObjFusionModels.TORUS_LEGACY.renderOnly(ObjFusionModels.TORUS_TEXTURE, context, "Magnet");
        poseStack.popPose();

        if (blockEntity.getConnection(0)) ObjFusionModels.TORUS_LEGACY.renderOnly(ObjFusionModels.TORUS_TEXTURE, context, "Bolts2");
        if (blockEntity.getConnection(1)) ObjFusionModels.TORUS_LEGACY.renderOnly(ObjFusionModels.TORUS_TEXTURE, context, "Bolts4");
        if (blockEntity.getConnection(2)) ObjFusionModels.TORUS_LEGACY.renderOnly(ObjFusionModels.TORUS_TEXTURE, context, "Bolts3");
        if (blockEntity.getConnection(3)) ObjFusionModels.TORUS_LEGACY.renderOnly(ObjFusionModels.TORUS_TEXTURE, context, "Bolts1");

        if (blockEntity.getPlasmaEnergy() > 0L) {
            long time = Util.getMillis() + visualTimeOffset(blockEntity);
            float alpha = 0.35F + (float) Math.sin(time / 1000.0D) * 0.25F;
            float mainOsc = positiveUnit(sps(time / 1000.0D));
            float glowOsc = positiveUnit(Math.sin(time / 2000.0D));
            float glowExtra = positiveUnit(time / 10000.0D);
            float sparkleSpin = positiveUnit(time / 500.0D * -1.0D);
            float sparkleOsc = positiveUnit(Math.sin(time / 1000.0D) * 0.5D);
            ObjRenderContext plasma = context.fullBright().withColor(
                    blockEntity.getPlasmaR(), blockEntity.getPlasmaG(), blockEntity.getPlasmaB(), alpha);
            ObjFusionModels.TORUS_LEGACY.renderOnly(ObjFusionModels.PLASMA_TEXTURE,
                    plasma.withTranslucencyNoDepthWrite().withUvScroll(0.0F, mainOsc), "Plasma");
            if (shouldRenderExtraPlasmaLayers(blockEntity)) {
                ObjFusionModels.TORUS_LEGACY.renderOnly(ObjFusionModels.PLASMA_GLOW_TEXTURE, plasma.withAdditiveTranslucency()
                        .withColor(blockEntity.getPlasmaR() * 2.0F, blockEntity.getPlasmaG() * 2.0F,
                                blockEntity.getPlasmaB() * 2.0F, alpha * 2.0F)
                        .withUvScroll(0.0F, positiveUnit(glowOsc + glowExtra)), "Plasma");
                ObjFusionModels.TORUS_LEGACY.renderOnly(ObjFusionModels.PLASMA_SPARKLE_TEXTURE, plasma.withAdditiveTranslucency()
                        .withColor(blockEntity.getPlasmaR() * 2.0F, blockEntity.getPlasmaG() * 2.0F,
                                blockEntity.getPlasmaB() * 2.0F, 0.75F)
                        .withUvScroll(sparkleSpin, sparkleOsc), "Plasma");
            }
        }
        poseStack.popPose();
    }

    private static long visualTimeOffset(FusionTorusBlockEntity blockEntity) {
        return Math.floorMod(blockEntity.getBlockPos().asLong(), 30_000L);
    }

    private static boolean shouldRenderExtraPlasmaLayers(FusionTorusBlockEntity blockEntity) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null || minecraft.player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 2.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) < EXTRA_PLASMA_LAYER_DISTANCE_SQ;
    }

    private static float positiveUnit(double value) {
        return (float) (value - Math.floor(value));
    }

    private static double sps(double value) {
        return Math.sin(Math.PI / 2.0D * Math.cos(value));
    }
}
