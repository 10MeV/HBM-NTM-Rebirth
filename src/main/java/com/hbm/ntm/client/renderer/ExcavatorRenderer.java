package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.blockentity.ExcavatorBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

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
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(ExcavatorBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
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
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F + state.getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING).toYRot());
        poseStack.translate(0.0D, -3.0D, 0.0D);

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(excavator)) {
            if (LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
                MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, light, packedOverlay, MAIN);
            }
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
        LegacyPoseRotations.rotateXDegrees(poseStack, -crusher);
        poseStack.translate(0.0F, -2.0F, -2.8125F);
        MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, packedOverlay, CRUSHER_1);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, 2.0F, 2.1875F);
        LegacyPoseRotations.rotateXDegrees(poseStack, crusher);
        poseStack.translate(0.0F, -2.0F, -2.1875F);
        MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, packedOverlay, CRUSHER_2);
        poseStack.popPose();
    }

    private static void renderDrill(ExcavatorBlockEntity excavator, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, -excavator.getDrillRotation(partialTick));
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
        double dropU = -((double) (System.currentTimeMillis()
                % (long) LegacyTileRenderPlans.EXCAVATOR_DROP_PERIOD_MILLIS)
                / LegacyTileRenderPlans.EXCAVATOR_DROP_PERIOD_MILLIS);
        double dropL = dropU + 4.0D;
        renderFallingColumn(COBBLE, LegacyTileRenderPlans.EXCAVATOR_UPPER_HALF_WIDTH,
                LegacyTileRenderPlans.EXCAVATOR_UPPER_HALF_WIDTH, 3.0D, 2.0D, 1.0D, 1.0D,
                dropU, dropL, poseStack, buffer, packedLight, packedOverlay);
        boolean crusherEnabled = excavator.isCrusherEnabled();
        renderFallingColumn(crusherEnabled ? GRAVEL : COBBLE, crusherEnabled ? 0.5D : 0.25D,
                LegacyTileRenderPlans.EXCAVATOR_LOWER_HALF_DEPTH, 2.0D, 1.0D,
                crusherEnabled ? 4.0D : 2.0D, 0.5D, dropU, dropL, poseStack, buffer, packedLight,
                packedOverlay);
    }

    private static void renderFallingColumn(ResourceLocation texture, double halfX, double halfZ,
            double topY, double bottomY, double uMax, double sideUMax, double dropU, double dropL,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        VertexConsumer consumer = LegacyTexturedQuadRenderer.vertexAlphaConsumer(texture, buffer,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        PoseStack.Pose pose = poseStack.last();
        double zPos = LegacyTileRenderPlans.EXCAVATOR_CHUTE_CENTER_Z + halfZ;
        double zNeg = LegacyTileRenderPlans.EXCAVATOR_CHUTE_CENTER_Z - halfZ;
        renderQuad(consumer, pose, 0.0F, 0.0F, 1.0F,
                halfX, topY, zPos, 0.0D, dropU,
                -halfX, topY, zPos, uMax, dropU,
                -halfX, bottomY, zPos, uMax, dropL,
                halfX, bottomY, zPos, 0.0D, dropL,
                packedLight, packedOverlay);
        renderQuad(consumer, pose, 0.0F, 0.0F, -1.0F,
                -halfX, topY, zNeg, uMax, dropU,
                halfX, topY, zNeg, 0.0D, dropU,
                halfX, bottomY, zNeg, 0.0D, dropL,
                -halfX, bottomY, zNeg, uMax, dropL,
                packedLight, packedOverlay);
        renderQuad(consumer, pose, -1.0F, 0.0F, 0.0F,
                -halfX, topY, zPos, 0.0D, dropU,
                -halfX, topY, zNeg, sideUMax, dropU,
                -halfX, bottomY, zNeg, sideUMax, dropL,
                -halfX, bottomY, zPos, 0.0D, dropL,
                packedLight, packedOverlay);
        renderQuad(consumer, pose, 1.0F, 0.0F, 0.0F,
                halfX, topY, zNeg, sideUMax, dropU,
                halfX, topY, zPos, 0.0D, dropU,
                halfX, bottomY, zPos, 0.0D, dropL,
                halfX, bottomY, zNeg, sideUMax, dropL,
                packedLight, packedOverlay);
    }

    private static void renderQuad(VertexConsumer consumer, PoseStack.Pose pose,
            float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3,
            int packedLight, int packedOverlay) {
        LegacyTexturedQuadRenderer.quadWithVertexAlpha(consumer, pose, packedLight, packedOverlay,
                normalX, normalY, normalZ,
                x0, y0, z0, u0, v0, 255,
                x1, y1, z1, u1, v1, 255,
                x2, y2, z2, u2, v2, 255,
                x3, y3, z3, u3, v3, 255,
                0xFFFFFF);
    }
}
