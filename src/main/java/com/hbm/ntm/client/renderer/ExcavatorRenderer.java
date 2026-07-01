package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.ExcavatorBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class ExcavatorRenderer implements BlockEntityRenderer<ExcavatorBlockEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/mining_drill.png");
    private static final ResourceLocation COBBLE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/cobblestone.png");
    private static final ResourceLocation GRAVEL =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/gravel.png");
    private static final LegacyWavefrontModel MODEL = ObjMachineModels.MINING_DRILL;
    private static final LegacyWavefrontModel.SelectionHandle MAIN = MODEL.prepareRenderOnlyInCallOrder("Main");
    private static final LegacyWavefrontModel.SelectionHandle CRUSHER_1 = MODEL.prepareRenderOnlyInCallOrder("Crusher1");
    private static final LegacyWavefrontModel.SelectionHandle CRUSHER_2 = MODEL.prepareRenderOnlyInCallOrder("Crusher2");
    private static final LegacyWavefrontModel.SelectionHandle DRILLBIT = MODEL.prepareRenderOnlyInCallOrder("Drillbit");
    private static final LegacyWavefrontModel.SelectionHandle SHAFT = MODEL.prepareRenderOnlyInCallOrder("Shaft");

    public ExcavatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(ExcavatorBlockEntity excavator, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(excavator, getViewDistance())) {
            return;
        }
        BlockState state = excavator.getBlockState();
        int light = LegacyRenderLighting.resolveBoundsLight(excavator, excavator.getRenderBoundingBox(), packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F + state.getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING).toYRot()));
        poseStack.translate(0.0D, -3.0D, 0.0D);

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(excavator)) {
            MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, light, packedOverlay, MAIN);
            renderCrusher(excavator, partialTick, poseStack, buffer, light, packedOverlay);
            renderDrill(excavator, partialTick, poseStack, buffer, light, packedOverlay);
        }
        renderChute(excavator, poseStack, buffer, light, packedOverlay);
        poseStack.popPose();
    }

    private static void renderCrusher(ExcavatorBlockEntity excavator, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float crusher = excavator.getCrusherRotation(partialTick);
        poseStack.pushPose();
        poseStack.translate(0.0F, 2.0F, 2.8125F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-crusher));
        poseStack.translate(0.0F, -2.0F, -2.8125F);
        MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, packedOverlay, CRUSHER_1);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, 2.0F, 2.1875F);
        poseStack.mulPose(Axis.XP.rotationDegrees(crusher));
        poseStack.translate(0.0F, -2.0F, -2.1875F);
        MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, packedOverlay, CRUSHER_2);
        poseStack.popPose();
    }

    private static void renderDrill(ExcavatorBlockEntity excavator, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(excavator.getDrillRotation(partialTick)));
        float extension = excavator.getDrillExtension(partialTick);
        poseStack.translate(0.0D, -extension, 0.0D);
        MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, packedOverlay, DRILLBIT);
        float shaft = extension;
        while (shaft >= -1.5F) {
            MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, packedOverlay, SHAFT);
            poseStack.translate(0.0D, 2.0D, 0.0D);
            shaft -= 2.0F;
        }
        poseStack.popPose();
    }

    private static void renderChute(ExcavatorBlockEntity excavator, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (excavator.getChuteTimer() <= 0) {
            return;
        }
        LegacyTileRenderPlans.ExcavatorChutePlan plan =
                LegacyTileRenderPlans.excavatorChutePlan(true, excavator.isCrusherEnabled(), System.currentTimeMillis());
        for (LegacyTileRenderPlans.NormalTexturedQuadPlan quad : plan.upperStream()) {
            renderQuad(COBBLE, quad, poseStack, buffer, packedLight, packedOverlay);
        }
        ResourceLocation lowerTexture = excavator.isCrusherEnabled() ? GRAVEL : COBBLE;
        for (LegacyTileRenderPlans.NormalTexturedQuadPlan quad : plan.lowerStream()) {
            renderQuad(lowerTexture, quad, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderQuad(ResourceLocation texture, LegacyTileRenderPlans.NormalTexturedQuadPlan quad,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        var v = quad.vertices();
        LegacyTexturedQuadRenderer.quad(texture, poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, quad.normalX(), quad.normalY(), quad.normalZ(),
                LegacyTexturedQuadRenderer.vertex(v.get(0).x(), v.get(0).y(), v.get(0).z(), v.get(0).u(), v.get(0).v()),
                LegacyTexturedQuadRenderer.vertex(v.get(1).x(), v.get(1).y(), v.get(1).z(), v.get(1).u(), v.get(1).v()),
                LegacyTexturedQuadRenderer.vertex(v.get(2).x(), v.get(2).y(), v.get(2).z(), v.get(2).u(), v.get(2).v()),
                LegacyTexturedQuadRenderer.vertex(v.get(3).x(), v.get(3).y(), v.get(3).z(), v.get(3).u(), v.get(3).v()));
    }
}
