package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.FusionPlasmaForgeBlockEntity;
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
        return true;
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
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, light, packedOverlay);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(FusionBreederRenderer.rotation(state)));
        if (blockEntity.isConnected()) {
            poseStack.pushPose();
            poseStack.translate(-2.0D, 0.0D, 0.0D);
            ObjFusionModels.TORUS_BOLTS_1.render(context);
            poseStack.popPose();
        }
        ObjFusionModels.PLASMA_FORGE_BODY.render(context);
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
            ObjFusionModels.PLASMA_FORGE_PLASMA.render(context.withColor(0x000000));
        }
    }

    private static void renderLegacyActivePlasma(FusionPlasmaForgeBlockEntity blockEntity, ObjRenderContext context) {
        if (blockEntity.getPlasmaEnergySync() <= 0L) {
            return;
        }
        long time = System.currentTimeMillis();
        float alpha = 0.5F + (float) Math.sin(time / 500.0D) * 0.25F;
        float mainOffset = (float) ((time / 750.0D) % 1.0D);
        float glowOffsetA = (float) ((Math.sin(time / 1000.0D) + time / 10000.0D) % 1.0D);
        float glowOffsetB = (float) ((Math.sin(time / 600.0D + 2.0D) + time / 5000.0D) % 1.0D);
        ObjRenderContext plasma = context.fullBright().withAdditiveTranslucency()
                .withColor(blockEntity.getPlasmaR(), blockEntity.getPlasmaG(), blockEntity.getPlasmaB(), alpha)
                .withUvScroll(0.0F, mainOffset);
        ObjFusionModels.PLASMA_FORGE_PLASMA.render(plasma);

        ObjRenderContext glow = context.fullBright().withAdditiveTranslucency()
                .withColor(blockEntity.getPlasmaR(), blockEntity.getPlasmaG(), blockEntity.getPlasmaB(), 0.55F);
        ObjFusionModels.PLASMA_FORGE_PLASMA_GLOW.render(glow.withUvScroll(0.0F, glowOffsetA));
        ObjFusionModels.PLASMA_FORGE_PLASMA_GLOW.render(glow.withUvScroll(0.0F, glowOffsetB));
    }

    private static void renderArticulatedParts(FusionPlasmaForgeBlockEntity blockEntity, PoseStack poseStack,
            ObjRenderContext context, float partialTick) {
        double[] striker = blockEntity.getStrikerPositions(partialTick);
        double[] jet = blockEntity.getJetPositions(partialTick);
        double rotor = blockEntity.getRotor(partialTick);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) rotor));
        poseStack.pushPose();
        ObjFusionModels.PLASMA_FORGE_SLIDER_STRIKER.render(context);
        rotateAtZ(poseStack, -2.75D, 2.5D, 0.0D, -striker[0]);
        ObjFusionModels.PLASMA_FORGE_ARM_LOWER_STRIKER.render(context);
        rotateAtZ(poseStack, -2.75D, 3.75D, 0.0D, -striker[1]);
        ObjFusionModels.PLASMA_FORGE_ARM_UPPER_STRIKER.render(context);
        rotateAtZ(poseStack, -1.5D, 3.75D, 0.0D, -striker[2]);
        ObjFusionModels.PLASMA_FORGE_STRIKER_MOUNT.render(context);
        poseStack.pushPose();
        rotateAtX(poseStack, 0.0D, 3.375D, 0.5D, striker[3]);
        ObjFusionModels.PLASMA_FORGE_STRIKER_RIGHT.render(context);
        poseStack.translate(0.0D, -striker[4], 0.0D);
        ObjFusionModels.PLASMA_FORGE_PISTON_RIGHT.render(context);
        poseStack.popPose();
        poseStack.pushPose();
        rotateAtX(poseStack, 0.0D, 3.375D, -0.5D, -striker[3]);
        ObjFusionModels.PLASMA_FORGE_STRIKER_LEFT.render(context);
        poseStack.translate(0.0D, -striker[5], 0.0D);
        ObjFusionModels.PLASMA_FORGE_PISTON_LEFT.render(context);
        poseStack.popPose();
        poseStack.popPose();

        poseStack.pushPose();
        ObjFusionModels.PLASMA_FORGE_SLIDER_JET.render(context);
        rotateAtZ(poseStack, 2.75D, 2.5D, 0.0D, jet[0]);
        ObjFusionModels.PLASMA_FORGE_ARM_LOWER_JET.render(context);
        rotateAtZ(poseStack, 2.75D, 3.75D, 0.0D, jet[1]);
        ObjFusionModels.PLASMA_FORGE_ARM_UPPER_JET.render(context);
        rotateAtZ(poseStack, 1.5D, 3.75D, 0.0D, jet[2]);
        ObjFusionModels.PLASMA_FORGE_JET.render(context);
        if (blockEntity.didProcess() && blockEntity.getPlasmaEnergySync() > 0L && blockEntity.isJetStableAwayFromHome()) {
            renderLegacyJet(blockEntity, context);
        }
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderArticulatedEffects(FusionPlasmaForgeBlockEntity blockEntity, PoseStack poseStack,
            ObjRenderContext context, float partialTick) {
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
