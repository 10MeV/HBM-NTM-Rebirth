package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.FusionPlasmaForgeBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjFusionModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
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
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(FusionPlasmaForgeBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, light, packedOverlay)
                .withRenderMode(LegacyTexturedRenderMode.CUTOUT_CULL);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(FusionBreederRenderer.rotation(state)));
        if (blockEntity.isConnected()) {
            poseStack.pushPose();
            poseStack.translate(-2.0D, 0.0D, 0.0D);
            ObjFusionModels.TORUS_LEGACY.renderOnly(ObjFusionModels.TORUS_TEXTURE, context, "Bolts1");
            poseStack.popPose();
        }
        renderForgePart(context, "Body");
        renderLegacyDormantPlasma(blockEntity, context);
        renderArticulatedParts(blockEntity, poseStack, context, partialTick);
        GenericMachineRecipe recipe = blockEntity.getSelectedRecipeDefinition();
        if (LegacyRecipeIconRenderer.shouldRenderWithin(blockEntity, 35.0D)) {
            LegacyRecipeIconRenderer.renderPlasmaForgeIcon(recipe, blockEntity.getLevel(), poseStack, buffer,
                    packedLight, partialTick);
        }
        renderLegacyActivePlasma(blockEntity, context);
        if (LegacyRecipeIconRenderer.shouldRenderWithin(blockEntity, 50.0D)) {
            renderLegacyStellarFluxBeam(recipe, context, partialTick);
        }
        renderArticulatedEffects(blockEntity, poseStack, context, partialTick);
        poseStack.popPose();
    }

    private static void renderLegacyDormantPlasma(FusionPlasmaForgeBlockEntity blockEntity, ObjRenderContext context) {
        if (blockEntity.getPlasmaEnergySync() <= 0L) {
            ObjFusionModels.PLASMA_FORGE_LEGACY.renderOnlyUntextured(
                    context.withRenderMode(LegacyTexturedRenderMode.CUTOUT_CULL).withColor(0x000000),
                    "Plasma");
        }
    }

    private static void renderLegacyActivePlasma(FusionPlasmaForgeBlockEntity blockEntity, ObjRenderContext context) {
        if (blockEntity.getPlasmaEnergySync() <= 0L) {
            return;
        }
        long time = System.currentTimeMillis() + blockEntity.getTimeOffset();
        float alpha = 0.5F + (float) Math.sin(time / 500.0D) * 0.25F;
        float mainOffset = (float) (sps(time / 750.0D) % 1.0D);
        float glowOffsetA = (float) ((Math.sin(time / 1000.0D) + time / 10000.0D) % 1.0D);
        float glowOffsetB = (float) ((Math.sin(time / 600.0D + 2.0D) + time / 5000.0D) % 1.0D);
        ObjRenderContext plasma = context.fullBright().withAdditiveTranslucency()
                .withColor(blockEntity.getPlasmaR(), blockEntity.getPlasmaG(), blockEntity.getPlasmaB(), alpha)
                .withUvScroll(0.0F, mainOffset);
        ObjFusionModels.PLASMA_FORGE_LEGACY.renderOnly(ObjFusionModels.PLASMA_TEXTURE, plasma, "Plasma");

        ObjRenderContext glow = context.fullBright().withAdditiveTranslucency()
                .withColor(blockEntity.getPlasmaR(), blockEntity.getPlasmaG(), blockEntity.getPlasmaB(), 0.55F);
        ObjFusionModels.PLASMA_FORGE_LEGACY.renderOnly(ObjFusionModels.PLASMA_GLOW_TEXTURE,
                glow.withUvScroll(0.0F, glowOffsetA), "Plasma");
        ObjFusionModels.PLASMA_FORGE_LEGACY.renderOnly(ObjFusionModels.PLASMA_GLOW_TEXTURE,
                glow.withUvScroll(0.0F, glowOffsetB), "Plasma");
    }

    private static void renderArticulatedParts(FusionPlasmaForgeBlockEntity blockEntity, PoseStack poseStack,
            ObjRenderContext context, float partialTick) {
        double[] striker = blockEntity.getStrikerPositions(partialTick);
        double[] jet = blockEntity.getJetPositions(partialTick);
        double rotor = blockEntity.getRotor(partialTick);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) rotor));
        poseStack.pushPose();
        renderForgePart(context, "SliderStriker");
        rotateAtZ(poseStack, -2.75D, 2.5D, 0.0D, -striker[0]);
        renderForgePart(context, "ArmLowerStriker");
        rotateAtZ(poseStack, -2.75D, 3.75D, 0.0D, -striker[1]);
        renderForgePart(context, "ArmUpperStriker");
        rotateAtZ(poseStack, -1.5D, 3.75D, 0.0D, -striker[2]);
        renderForgePart(context, "StrikerMount");
        poseStack.pushPose();
        rotateAtX(poseStack, 0.0D, 3.375D, 0.5D, striker[3]);
        renderForgePart(context, "StrikerRight");
        poseStack.translate(0.0D, -striker[4], 0.0D);
        renderForgePart(context, "PistonRight");
        poseStack.popPose();
        poseStack.pushPose();
        rotateAtX(poseStack, 0.0D, 3.375D, -0.5D, -striker[3]);
        renderForgePart(context, "StrikerLeft");
        poseStack.translate(0.0D, -striker[5], 0.0D);
        renderForgePart(context, "PistonLeft");
        poseStack.popPose();
        poseStack.popPose();

        poseStack.pushPose();
        renderForgePart(context, "SliderJet");
        rotateAtZ(poseStack, 2.75D, 2.5D, 0.0D, jet[0]);
        renderForgePart(context, "ArmLowerJet");
        rotateAtZ(poseStack, 2.75D, 3.75D, 0.0D, jet[1]);
        renderForgePart(context, "ArmUpperJet");
        rotateAtZ(poseStack, 1.5D, 3.75D, 0.0D, jet[2]);
        renderForgePart(context, "Jet");
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderForgePart(ObjRenderContext context, String part) {
        ObjFusionModels.PLASMA_FORGE_LEGACY.renderOnly(ObjFusionModels.PLASMA_FORGE_TEXTURE, context, part);
    }

    private static void renderArticulatedEffects(FusionPlasmaForgeBlockEntity blockEntity, PoseStack poseStack,
            ObjRenderContext context, float partialTick) {
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
        renderLegacyJet(blockEntity, context);
        poseStack.popPose();
    }

    private static void rotateAtZ(PoseStack poseStack, double x, double y, double z, double degrees) {
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) degrees));
        poseStack.translate(-x, -y, -z);
    }

    private static void rotateAtX(PoseStack poseStack, double x, double y, double z, double degrees) {
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) degrees));
        poseStack.translate(-x, -y, -z);
    }

    private static void renderLegacyJet(FusionPlasmaForgeBlockEntity blockEntity, ObjRenderContext context) {
        ObjRenderContext jet = context.fullBright().withAdditiveTranslucency();
        double outerLen = 1.0D + Math.random() * 0.125D;
        renderLegacyJetLayer(jet, blockEntity, outerLen, 0.01D, 0.125D);
        renderLegacyJetLayer(jet, blockEntity, outerLen * 1.5D, 0.0625D * 1.5D, 0.125D);
    }

    private static void renderLegacyJetLayer(ObjRenderContext context, FusionPlasmaForgeBlockEntity blockEntity,
            double outerLen, double narrow, double side) {
        double near = 1.375D;
        double far = 1.625D;
        double y = 3.0D;
        double tipY = y - outerLen;
        float red = blockEntity.getPlasmaR();
        float green = blockEntity.getPlasmaG();
        float blue = blockEntity.getPlasmaB();
        LegacyUntexturedQuadRenderer.quadRgbaF(context,
                near, y, side, far, y, side, far - narrow, tipY, side - narrow, near + narrow, tipY, side - narrow,
                red, green, blue, 1.0F, 1.0F, 0.0F, 0.0F);
        LegacyUntexturedQuadRenderer.quadRgbaF(context,
                near, y, -side, far, y, -side, far - narrow, tipY, -side + narrow, near + narrow, tipY, -side + narrow,
                red, green, blue, 1.0F, 1.0F, 0.0F, 0.0F);
        LegacyUntexturedQuadRenderer.quadRgbaF(context,
                near, y, side, near, y, -side, near + narrow, tipY, -side + narrow, near + narrow, tipY, side - narrow,
                red, green, blue, 1.0F, 1.0F, 0.0F, 0.0F);
        LegacyUntexturedQuadRenderer.quadRgbaF(context,
                far, y, side, far, y, -side, far - narrow, tipY, -side + narrow, far - narrow, tipY, side - narrow,
                red, green, blue, 1.0F, 1.0F, 0.0F, 0.0F);
    }

    private static double sps(double value) {
        return Math.sin(Math.PI / 2.0D * Math.cos(value));
    }

    private static void renderLegacyStellarFluxBeam(GenericMachineRecipe recipe, ObjRenderContext context,
            float partialTick) {
        if (recipe == null) {
            return;
        }
        double ticks = Minecraft.getInstance().player == null
                ? System.currentTimeMillis() / 50.0D
                : Minecraft.getInstance().player.tickCount;
        double offset = ((ticks + partialTick) / 15.0D) % 1.0D;
        double in = 0.4375D;
        double bottom = 1.0D;
        double beamHeight = 1.5D;
        double top = bottom + beamHeight;
        ObjRenderContext beam = context.fullBright().withAdditiveTranslucency();
        LegacyTexturedQuadRenderer.quadWithComputedNormalAndVertexAlpha(HbmFluids.STELLAR_FLUX.getTexture(), beam,
                -in, bottom, in, offset + beamHeight, 0.0D, 255,
                -in, top, in, offset, 0.0D, 0,
                -in, top, -in, offset, 1.0D, 0,
                -in, bottom, -in, offset + beamHeight, 1.0D, 255,
                0xFFFFFF);
        LegacyTexturedQuadRenderer.quadWithComputedNormalAndVertexAlpha(HbmFluids.STELLAR_FLUX.getTexture(), beam,
                in, top, in, offset, 0.0D, 0,
                in, bottom, in, offset + beamHeight, 0.0D, 255,
                in, bottom, -in, offset + beamHeight, 1.0D, 255,
                in, top, -in, offset, 1.0D, 0,
                0xFFFFFF);
        LegacyTexturedQuadRenderer.quadWithComputedNormalAndVertexAlpha(HbmFluids.STELLAR_FLUX.getTexture(), beam,
                in, bottom, in, offset + beamHeight, 0.0D, 255,
                in, top, in, offset, 0.0D, 0,
                -in, top, in, offset, 1.0D, 0,
                -in, bottom, in, offset + beamHeight, 1.0D, 255,
                0xFFFFFF);
        LegacyTexturedQuadRenderer.quadWithComputedNormalAndVertexAlpha(HbmFluids.STELLAR_FLUX.getTexture(), beam,
                in, top, -in, offset, 0.0D, 0,
                in, bottom, -in, offset + beamHeight, 0.0D, 255,
                -in, bottom, -in, offset + beamHeight, 1.0D, 255,
                -in, top, -in, offset, 1.0D, 0,
                0xFFFFFF);
    }
}
