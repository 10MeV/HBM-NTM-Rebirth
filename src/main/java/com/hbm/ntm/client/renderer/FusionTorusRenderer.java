package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.FusionTorusBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjFusionModels;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class FusionTorusRenderer implements BlockEntityRenderer<FusionTorusBlockEntity> {
    private static final double EXTRA_PLASMA_LAYER_DISTANCE = 100.0D;

    public FusionTorusRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(FusionTorusBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(FusionTorusBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);

        try (LegacyRenderLighting.ModelViewSamplingScope ignored =
                LegacyRenderLighting.pushModelViewSampling(blockEntity, poseStack.last().pose())) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            if (blockEntity.isTilted()) {
                poseStack.translate(0.0D, -1.0D, 0.0D);
                poseStack.mulPose(Axis.ZP.rotationDegrees(10.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(5.0F));
            }
            ObjFusionModels.renderTorusPart(ObjFusionModels.TORUS_LEGACY, ObjFusionModels.TORUS_TEXTURE,
                    poseStack, buffer, light, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, "Torus");

            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(blockEntity.getMagnet(partialTick)));
            ObjFusionModels.renderTorusPart(ObjFusionModels.TORUS_LEGACY, ObjFusionModels.TORUS_TEXTURE,
                    poseStack, buffer, light, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, "Magnet");
            poseStack.popPose();

            if (blockEntity.getConnection(0)) {
                renderSolidPart(poseStack, buffer, light, packedOverlay, "Bolts2");
            }
            if (blockEntity.getConnection(1)) {
                renderSolidPart(poseStack, buffer, light, packedOverlay, "Bolts4");
            }
            if (blockEntity.getConnection(2)) {
                renderSolidPart(poseStack, buffer, light, packedOverlay, "Bolts3");
            }
            if (blockEntity.getConnection(3)) {
                renderSolidPart(poseStack, buffer, light, packedOverlay, "Bolts1");
            }

            if (blockEntity.getPlasmaEnergy() > 0L) {
                long time = Util.getMillis() + visualTimeOffset(blockEntity);
                float alpha = 0.35F + (float) Math.sin(time / 1000.0D) * 0.25F;
                float mainOsc = positiveUnit(sps(time / 1000.0D));
                float glowOsc = positiveUnit(Math.sin(time / 2000.0D));
                float glowExtra = positiveUnit(time / 10000.0D);
                float sparkleSpin = positiveUnit(time / 500.0D * -1.0D);
                float sparkleOsc = positiveUnit(Math.sin(time / 1000.0D) * 0.5D);
                float red = blockEntity.getPlasmaR();
                float green = blockEntity.getPlasmaG();
                float blue = blockEntity.getPlasmaB();
                boolean renderExtraLayers = shouldRenderExtraPlasmaLayers(blockEntity);
                LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, queuedPose -> {
                    renderPlasmaLayer(ObjFusionModels.PLASMA_TEXTURE, queuedPose, buffer, packedOverlay,
                            red, green, blue, alpha, 0.0F, mainOsc);
                    if (renderExtraLayers) {
                        renderPlasmaLayer(ObjFusionModels.PLASMA_GLOW_TEXTURE, queuedPose, buffer, packedOverlay,
                                red * 2.0F, green * 2.0F, blue * 2.0F, alpha * 2.0F,
                                0.0F, positiveUnit(glowOsc + glowExtra));
                        renderPlasmaLayer(ObjFusionModels.PLASMA_SPARKLE_TEXTURE, queuedPose, buffer, packedOverlay,
                                red * 2.0F, green * 2.0F, blue * 2.0F, 0.75F, sparkleSpin, sparkleOsc);
                    }
                });
            }
            poseStack.popPose();
        }
    }

    private static void renderSolidPart(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, String partName) {
        ObjFusionModels.renderTorusPart(ObjFusionModels.TORUS_LEGACY, ObjFusionModels.TORUS_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, partName);
    }

    private static void renderPlasmaLayer(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedOverlay, float red, float green, float blue, float alpha, float uOffset, float vOffset) {
        ObjFusionModels.renderTorusPart(texture, poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay,
                colorByte(red), colorByte(green), colorByte(blue), colorByte(alpha), false,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, uvScroll(uOffset, vOffset), "Plasma");
    }

    private static LegacyWavefrontModel.UvTransform uvScroll(float uOffset, float vOffset) {
        return LegacyWavefrontModel.UvTransform.dynamic(1.0F, 0.0F, 0.0F, 1.0F, uOffset, vOffset, 0.0F);
    }

    private static int colorByte(float value) {
        return Math.max(0, Math.min(255, Math.round(value * 255.0F)));
    }

    private static long visualTimeOffset(FusionTorusBlockEntity blockEntity) {
        return Math.floorMod(blockEntity.getBlockPos().asLong(), 30_000L);
    }

    private static boolean shouldRenderExtraPlasmaLayers(FusionTorusBlockEntity blockEntity) {
        return LegacyRenderDistanceGates.isPlayerWithinOr(blockEntity, 2.5D,
                EXTRA_PLASMA_LAYER_DISTANCE, true);
    }

    private static float positiveUnit(double value) {
        return (float) (value - Math.floor(value));
    }

    private static double sps(double value) {
        return Math.sin(Math.PI / 2.0D * Math.cos(value));
    }
}
