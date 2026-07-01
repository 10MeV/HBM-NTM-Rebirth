package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.FusionPlasmaForgeBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjFusionModels;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class FusionPlasmaForgeRenderer implements BlockEntityRenderer<FusionPlasmaForgeBlockEntity> {
    public FusionPlasmaForgeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(FusionPlasmaForgeBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(FusionPlasmaForgeBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(FusionBreederRenderer.rotation(state)));
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            if (blockEntity.isConnected()) {
                poseStack.pushPose();
                poseStack.translate(-2.0D, 0.0D, 0.0D);
                ObjFusionModels.renderTorusPart(ObjFusionModels.TORUS_TEXTURE, poseStack, buffer, light,
                        packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, "Bolts1");
                poseStack.popPose();
            }
            renderForgePart(poseStack, buffer, light, packedOverlay, "Body");
            renderLegacyDormantPlasma(blockEntity, poseStack, buffer);
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                renderArticulatedParts(blockEntity, poseStack, buffer, light, packedOverlay, partialTick);
            }
            renderLegacyActivePlasma(blockEntity, poseStack, buffer, packedOverlay);
        }
        GenericMachineRecipe recipe = blockEntity.getSelectedRecipeDefinition();
        if (LegacyRecipeIconRenderer.shouldRenderWithin(blockEntity, 35.0D)) {
            LegacyRecipeIconRenderer.renderPlasmaForgeIcon(recipe, blockEntity.getLevel(), poseStack, buffer,
                    packedLight, partialTick);
        }
        if (recipe != null && LegacyRecipeIconRenderer.shouldRenderWithin(blockEntity, 50.0D)) {
            double stellarFluxOffset = stellarFluxOffset(partialTick);
            LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                    queuedPose -> renderLegacyStellarFluxBeam(queuedPose, buffer, packedOverlay, stellarFluxOffset));
        }
        renderArticulatedEffects(blockEntity, poseStack, buffer, partialTick);
        poseStack.popPose();
    }

    private static void renderLegacyDormantPlasma(FusionPlasmaForgeBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource buffer) {
        if (blockEntity.getPlasmaEnergySync() <= 0L) {
            ObjFusionModels.renderPlasmaForgePartUntextured(poseStack, buffer, 0, 0, 0, 255,
                    LegacyTexturedRenderMode.CUTOUT_CULL, "Plasma");
        }
    }

    private static void renderLegacyActivePlasma(FusionPlasmaForgeBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource buffer, int packedOverlay) {
        if (blockEntity.getPlasmaEnergySync() <= 0L) {
            return;
        }
        long time = System.currentTimeMillis() + blockEntity.getTimeOffset();
        float alpha = 0.5F + (float) Math.sin(time / 500.0D) * 0.25F;
        float mainOffset = (float) (sps(time / 750.0D) % 1.0D);
        float glowOffsetA = (float) ((Math.sin(time / 1000.0D) + time / 10000.0D) % 1.0D);
        float glowOffsetB = (float) ((Math.sin(time / 600.0D + 2.0D) + time / 5000.0D) % 1.0D);
        float red = blockEntity.getPlasmaR();
        float green = blockEntity.getPlasmaG();
        float blue = blockEntity.getPlasmaB();
        renderPlasmaForgeLayer(ObjFusionModels.PLASMA_TEXTURE, poseStack, buffer, packedOverlay,
                red * alpha, green * alpha, blue * alpha, 1.0F, LegacyTexturedRenderMode.CUTOUT_CULL,
                0.0F, mainOffset);
        float glowRed = red * 2.0F;
        float glowGreen = green * 2.0F;
        float glowBlue = blue * 2.0F;
        LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, queuedPose -> {
            renderPlasmaForgeLayer(ObjFusionModels.PLASMA_GLOW_TEXTURE, queuedPose, buffer, packedOverlay,
                    glowRed, glowGreen, glowBlue, 1.0F,
                    LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, 0.0F, glowOffsetA);
            renderPlasmaForgeLayer(ObjFusionModels.PLASMA_GLOW_TEXTURE, queuedPose, buffer, packedOverlay,
                    glowRed, glowGreen, glowBlue, 1.0F,
                    LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, 0.0F, glowOffsetB);
        });
    }

    private static void renderArticulatedParts(FusionPlasmaForgeBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, float partialTick) {
        double[] striker = blockEntity.getStrikerPositions(partialTick);
        double[] jet = blockEntity.getJetPositions(partialTick);
        double rotor = blockEntity.getRotor(partialTick);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) rotor));
        poseStack.pushPose();
        renderForgePart(poseStack, buffer, packedLight, packedOverlay, "SliderStriker");
        rotateAtZ(poseStack, -2.75D, 2.5D, 0.0D, -striker[0]);
        renderForgePart(poseStack, buffer, packedLight, packedOverlay, "ArmLowerStriker");
        rotateAtZ(poseStack, -2.75D, 3.75D, 0.0D, -striker[1]);
        renderForgePart(poseStack, buffer, packedLight, packedOverlay, "ArmUpperStriker");
        rotateAtZ(poseStack, -1.5D, 3.75D, 0.0D, -striker[2]);
        renderForgePart(poseStack, buffer, packedLight, packedOverlay, "StrikerMount");
        poseStack.pushPose();
        rotateAtX(poseStack, 0.0D, 3.375D, 0.5D, striker[3]);
        renderForgePart(poseStack, buffer, packedLight, packedOverlay, "StrikerRight");
        poseStack.translate(0.0D, -striker[4], 0.0D);
        renderForgePart(poseStack, buffer, packedLight, packedOverlay, "PistonRight");
        poseStack.popPose();
        poseStack.pushPose();
        rotateAtX(poseStack, 0.0D, 3.375D, -0.5D, -striker[3]);
        renderForgePart(poseStack, buffer, packedLight, packedOverlay, "StrikerLeft");
        poseStack.translate(0.0D, -striker[5], 0.0D);
        renderForgePart(poseStack, buffer, packedLight, packedOverlay, "PistonLeft");
        poseStack.popPose();
        poseStack.popPose();

        poseStack.pushPose();
        renderForgePart(poseStack, buffer, packedLight, packedOverlay, "SliderJet");
        rotateAtZ(poseStack, 2.75D, 2.5D, 0.0D, jet[0]);
        renderForgePart(poseStack, buffer, packedLight, packedOverlay, "ArmLowerJet");
        rotateAtZ(poseStack, 2.75D, 3.75D, 0.0D, jet[1]);
        renderForgePart(poseStack, buffer, packedLight, packedOverlay, "ArmUpperJet");
        rotateAtZ(poseStack, 1.5D, 3.75D, 0.0D, jet[2]);
        renderForgePart(poseStack, buffer, packedLight, packedOverlay, "Jet");
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderForgePart(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, String part) {
        ObjFusionModels.renderPlasmaForgePart(ObjFusionModels.PLASMA_FORGE_TEXTURE, poseStack, buffer,
                packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, part);
    }

    private static void renderArticulatedEffects(FusionPlasmaForgeBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource buffer, float partialTick) {
        if (!blockEntity.didProcess() || !blockEntity.isJetStableAwayFromHome()) {
            return;
        }
        double[] jet = blockEntity.getJetPositions(partialTick);
        double rotor = blockEntity.getRotor(partialTick);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) rotor));
        rotateAtZ(poseStack, 2.75D, 2.5D, 0.0D, jet[0]);
        rotateAtZ(poseStack, 2.75D, 3.75D, 0.0D, jet[1]);
        rotateAtZ(poseStack, 1.5D, 3.75D, 0.0D, jet[2]);
        float red = blockEntity.getPlasmaR();
        float green = blockEntity.getPlasmaG();
        float blue = blockEntity.getPlasmaB();
        double outerLen = 1.0D + Math.random() * 0.125D;
        LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                queuedPose -> renderLegacyJet(queuedPose, buffer, red, green, blue, outerLen));
        poseStack.popPose();
    }

    private static void rotateAtZ(PoseStack poseStack, double x, double y, double z, double degrees) {
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) degrees));
        poseStack.translate(-x, -y, -z);
    }

    private static void renderPlasmaForgeLayer(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedOverlay, float red, float green, float blue, float alpha, LegacyTexturedRenderMode renderMode,
            float uOffset, float vOffset) {
        ObjFusionModels.renderPlasmaForgePart(texture, poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay,
                colorByte(red), colorByte(green), colorByte(blue), colorByte(alpha), false, renderMode,
                uvScroll(uOffset, vOffset), "Plasma");
    }

    private static LegacyWavefrontModel.UvTransform uvScroll(float uOffset, float vOffset) {
        return LegacyWavefrontModel.UvTransform.dynamic(1.0F, 0.0F, 0.0F, 1.0F, uOffset, vOffset, 0.0F);
    }

    private static int colorByte(float value) {
        return Math.max(0, Math.min(255, Math.round(value * 255.0F)));
    }

    private static void rotateAtX(PoseStack poseStack, double x, double y, double z, double degrees) {
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) degrees));
        poseStack.translate(-x, -y, -z);
    }

    private static void renderLegacyJet(PoseStack poseStack, MultiBufferSource buffer,
            float red, float green, float blue, double outerLen) {
        renderLegacyJetLayer(poseStack, buffer, red, green, blue, outerLen, 0.01D, 0.125D);
        renderLegacyJetLayer(poseStack, buffer, red, green, blue, outerLen * 1.5D, 0.0625D * 1.5D, 0.125D);
    }

    private static void renderLegacyJetLayer(PoseStack poseStack, MultiBufferSource buffer,
            float red, float green, float blue, double outerLen, double narrow, double side) {
        double near = 1.375D;
        double far = 1.625D;
        double y = 3.0D;
        double tipY = y - outerLen;
        LegacyUntexturedQuadRenderer.quadRgbaF(poseStack, buffer, LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                near, y, side, far, y, side, far - narrow, tipY, side - narrow, near + narrow, tipY, side - narrow,
                red, green, blue, 1.0F, 1.0F, 0.0F, 0.0F);
        LegacyUntexturedQuadRenderer.quadRgbaF(poseStack, buffer, LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                near, y, -side, far, y, -side, far - narrow, tipY, -side + narrow, near + narrow, tipY, -side + narrow,
                red, green, blue, 1.0F, 1.0F, 0.0F, 0.0F);
        LegacyUntexturedQuadRenderer.quadRgbaF(poseStack, buffer, LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                near, y, side, near, y, -side, near + narrow, tipY, -side + narrow, near + narrow, tipY, side - narrow,
                red, green, blue, 1.0F, 1.0F, 0.0F, 0.0F);
        LegacyUntexturedQuadRenderer.quadRgbaF(poseStack, buffer, LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                far, y, side, far, y, -side, far - narrow, tipY, -side + narrow, far - narrow, tipY, side - narrow,
                red, green, blue, 1.0F, 1.0F, 0.0F, 0.0F);
    }

    private static double sps(double value) {
        return Math.sin(Math.PI / 2.0D * Math.cos(value));
    }

    private static double stellarFluxOffset(float partialTick) {
        double ticks = Minecraft.getInstance().player == null
                ? System.currentTimeMillis() / 50.0D
                : Minecraft.getInstance().player.tickCount;
        return ((ticks + partialTick) / 15.0D) % 1.0D;
    }

    private static void renderLegacyStellarFluxBeam(PoseStack poseStack,
            MultiBufferSource buffer, int packedOverlay, double offset) {
        double in = 0.4375D;
        double bottom = 1.0D;
        double beamHeight = 1.5D;
        double top = bottom + beamHeight;
        LegacyTexturedQuadRenderer.quadWithComputedNormalAndVertexAlpha(HbmFluids.STELLAR_FLUX.getTexture(),
                poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                -in, bottom, in, offset + beamHeight, 0.0D, 255,
                -in, top, in, offset, 0.0D, 0,
                -in, top, -in, offset, 1.0D, 0,
                -in, bottom, -in, offset + beamHeight, 1.0D, 255,
                0xFFFFFF);
        LegacyTexturedQuadRenderer.quadWithComputedNormalAndVertexAlpha(HbmFluids.STELLAR_FLUX.getTexture(),
                poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                in, top, in, offset, 0.0D, 0,
                in, bottom, in, offset + beamHeight, 0.0D, 255,
                in, bottom, -in, offset + beamHeight, 1.0D, 255,
                in, top, -in, offset, 1.0D, 0,
                0xFFFFFF);
        LegacyTexturedQuadRenderer.quadWithComputedNormalAndVertexAlpha(HbmFluids.STELLAR_FLUX.getTexture(),
                poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                in, bottom, in, offset + beamHeight, 0.0D, 255,
                in, top, in, offset, 0.0D, 0,
                -in, top, in, offset, 1.0D, 0,
                -in, bottom, in, offset + beamHeight, 1.0D, 255,
                0xFFFFFF);
        LegacyTexturedQuadRenderer.quadWithComputedNormalAndVertexAlpha(HbmFluids.STELLAR_FLUX.getTexture(),
                poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                in, top, -in, offset, 0.0D, 0,
                in, bottom, -in, offset + beamHeight, 0.0D, 255,
                -in, bottom, -in, offset + beamHeight, 1.0D, 255,
                -in, top, -in, offset, 1.0D, 0,
                0xFFFFFF);
    }
}
