package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.OilDrillBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class OilDrillRenderer implements BlockEntityRenderer<OilDrillBlockEntity> {
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();
    private static final String[] FRACKING_PIPE_PARTS = { "pX", "nX", "pZ", "nZ" };
    private static final LegacyWavefrontModel.SelectionHandle FRACKING_PIPE_HANDLE =
            ObjBlockModels.PIPE_NEO.prepareRenderOnlyInCallOrder(FRACKING_PIPE_PARTS);
    private static final LegacyWavefrontModel.SelectionHandle PUMPJACK_BASE =
            ObjMachineModels.PUMPJACK_LEGACY.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle PUMPJACK_ROTOR =
            ObjMachineModels.PUMPJACK_LEGACY.prepareRenderOnlyInCallOrder("Rotor");
    private static final LegacyWavefrontModel.SelectionHandle PUMPJACK_HEAD =
            ObjMachineModels.PUMPJACK_LEGACY.prepareRenderOnlyInCallOrder("Head");
    private static final LegacyWavefrontModel.SelectionHandle PUMPJACK_CARRIAGE =
            ObjMachineModels.PUMPJACK_LEGACY.prepareRenderOnlyInCallOrder("Carriage");

    public OilDrillRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(OilDrillBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(OilDrillBlockEntity drill, Vec3 cameraPos) {
        if (drill.getKind() == OilDrillBlockEntity.Kind.WELL
                && !LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
            return false;
        }
        return BlockEntityRenderer.super.shouldRender(drill, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(drill, getViewDistance());
    }

    @Override
    public void render(OilDrillBlockEntity drill, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(drill, getViewDistance())) {
            return;
        }
        boolean berStaticFallback = LegacyMachineRenderShapes.renderChunkBakedStaticsInBer();
        if (drill.getKind() == OilDrillBlockEntity.Kind.WELL && !berStaticFallback) {
            return;
        }
        BlockState state = drill.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(drill, state, definition, packedLight);
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()).asVBO());

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(drill)) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            LegacyPoseRotations.rotateYDegrees(poseStack, definition.yRotation(state));
            Vec3 translation = definition.modelTranslation(state);
            poseStack.translate(translation.x, translation.y, translation.z);
            LegacyPoseRotations.rotateYDegrees(poseStack, definition.postModelYRotation(state));

            if (drill.getKind() == OilDrillBlockEntity.Kind.PUMPJACK) {
                renderPumpjack(drill, partialTick, poseStack, buffer, modelLight, packedLight, packedOverlay,
                        definition, model, berStaticFallback);
            } else {
                // Fracking Tower remains a complete BER model because it has no chunk-baked body. Oil Well's
                // derrick is chunk-baked normally and must be submitted here only in the explicit BER fallback.
                if (drill.getKind() == OilDrillBlockEntity.Kind.FRACKING_TOWER || berStaticFallback) {
                    LegacyTexturedRenderMode bodyRenderMode = drill.getKind() == OilDrillBlockEntity.Kind.WELL
                            ? LegacyTexturedRenderMode.CUTOUT_CULL
                            : LegacyTexturedRenderMode.CUTOUT_NO_CULL;
                    if (definition.renderAll()) {
                        model.renderAll(definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay,
                                bodyRenderMode);
                    } else {
                        for (String part : definition.renderParts()) {
                            renderModelPart(model, part, definition.textureLocation(), poseStack, buffer, modelLight,
                                    packedOverlay, bodyRenderMode);
                        }
                    }
                }
                if (drill.getKind() == OilDrillBlockEntity.Kind.FRACKING_TOWER) {
                    renderFrackingPipes(state, poseStack, buffer, modelLight, packedOverlay);
                }
            }

            poseStack.popPose();
        }
    }

    private static void renderFrackingPipes(BlockState state, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 0.0D);
        ObjBlockModels.PIPE_NEO.renderOnlyInCallOrder(ObjBlockModels.PIPE_SILVER_TEXTURE, poseStack, buffer,
                packedLight, packedOverlay, FRACKING_PIPE_HANDLE);
        poseStack.popPose();
    }

    private static void renderPumpjack(OilDrillBlockEntity drill, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int modelLight, int activityLight, int packedOverlay,
            LegacyMachineDefinition definition, LegacyWavefrontModel model, boolean berStaticFallback) {
        // RenderPumpjack submits Base before Rotor, Head, Carriage and rods. Base belongs to the chunk model
        // normally; the prepared SelectionHandle restores it when chunk-baked machine statics are disabled.
        if (berStaticFallback) {
            renderModelPart(model, "Base", definition.textureLocation(), poseStack, buffer, modelLight,
                    packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
        }
        float rotation = Mth.lerp(partialTick, drill.getPreviousPumpjackRotation(), drill.getPumpjackRotation());
        double radians = rotation * LegacyTileRenderPlans.DEG_TO_RAD;
        double sin = Math.sin(radians);
        double headRadians = -sin * LegacyTileRenderPlans.PUMPJACK_HEAD_ROTATION_SCALE;

        try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(drill)) {
            renderRotatingPart(model, "Rotor", definition.textureLocation(),
                    0.0D, LegacyTileRenderPlans.PUMPJACK_ROTOR_PIVOT_Y,
                    LegacyTileRenderPlans.PUMPJACK_ROTOR_PIVOT_Z,
                    1.0F, 0.0F, 0.0F, rotation - 90.0D, poseStack, buffer, activityLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_CULL);
            renderRotatingPart(model, "Head", definition.textureLocation(),
                    0.0D, LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Y,
                    LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Z,
                    1.0F, 0.0F, 0.0F,
                    sin * LegacyTileRenderPlans.RAD_TO_DEG * LegacyTileRenderPlans.PUMPJACK_HEAD_ROTATION_SCALE,
                    poseStack, buffer, activityLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
            renderTranslatedPart(model, "Carriage", definition.textureLocation(),
                    0.0D, -sin, 0.0D, poseStack, buffer, activityLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_CULL);
            renderPumpjackRods(rotation, sin, headRadians, poseStack, buffer);
        }
    }

    private static void renderRotatingPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            double pivotX, double pivotY, double pivotZ, float axisX, float axisY, float axisZ,
            double angleDegrees, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderRotatingPart(model, partName, texture, pivotX, pivotY, pivotZ, axisX, axisY, axisZ, angleDegrees,
                poseStack, buffer, packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    private static void renderRotatingPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            double pivotX, double pivotY, double pivotZ, float axisX, float axisY, float axisZ,
            double angleDegrees, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        poseStack.translate(pivotX, pivotY, pivotZ);
        if (axisX != 0.0F) {
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) (angleDegrees * axisX));
        }
        if (axisY != 0.0F) {
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) (angleDegrees * axisY));
        }
        if (axisZ != 0.0F) {
            LegacyPoseRotations.rotateZDegrees(poseStack, (float) (angleDegrees * axisZ));
        }
        poseStack.translate(-pivotX, -pivotY, -pivotZ);
        renderModelPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderRotatingPart(model, part.partName(), texture, part.pivotX(), part.pivotY(), part.pivotZ(),
                part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees(), poseStack, buffer, packedLight,
                packedOverlay);
    }

    private static void renderTranslatedPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            double translateX, double translateY, double translateZ, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        renderTranslatedPart(model, partName, texture, translateX, translateY, translateZ, poseStack, buffer,
                packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    private static void renderTranslatedPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            double translateX, double translateY, double translateZ, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        renderModelPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!part.active()) {
            return;
        }
        renderTranslatedPart(model, part.partName(), texture, part.translateX(), part.translateY(),
                part.translateZ(), poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderModelPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderModelPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    private static void renderModelPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle = pumpjackHandle(model, partName);
        if (handle != null) {
            model.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle, renderMode);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, false, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT);
    }

    private static LegacyWavefrontModel.SelectionHandle pumpjackHandle(LegacyWavefrontModel model, String partName) {
        if (!isPumpjackModel(model)) {
            return null;
        }
        return switch (partName) {
            case "Base" -> PUMPJACK_BASE;
            case "Rotor" -> PUMPJACK_ROTOR;
            case "Head" -> PUMPJACK_HEAD;
            case "Carriage" -> PUMPJACK_CARRIAGE;
            default -> null;
        };
    }

    private static boolean isPumpjackModel(LegacyWavefrontModel model) {
        return model == ObjMachineModels.PUMPJACK_LEGACY
                || model.modelLocation().equals(ObjMachineModels.PUMPJACK_LEGACY.modelLocation());
    }

    private static void renderPumpjackRods(double rotationDegrees, double sin, double headRadians,
            PoseStack poseStack, MultiBufferSource buffer) {
        LegacyUntexturedQuadRenderer.DirectQuadBatch batch =
                LegacyUntexturedQuadRenderer.directQuadBatch(poseStack, buffer,
                        LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        double headSin = Math.sin(headRadians);
        double headCos = Math.cos(headRadians);
        double backY = -2.0D * headSin;
        double backZ = -2.0D * headCos;
        double rotorRadians = -(rotationDegrees - 90.0D) * LegacyTileRenderPlans.DEG_TO_RAD;
        double rotorY = 0.5D * Math.cos(rotorRadians);
        double rotorZ = -0.5D * Math.sin(rotorRadians);
        for (int side = -1; side <= 1; side += 2) {
            renderRodQuad(batch,
                    LegacyTileRenderPlans.PUMPJACK_BACK_ROD_X * side,
                    LegacyTileRenderPlans.PUMPJACK_ROTOR_PIVOT_Y + rotorY,
                    LegacyTileRenderPlans.PUMPJACK_ROTOR_PIVOT_Z + rotorZ
                            - LegacyTileRenderPlans.PUMPJACK_BACK_ROD_Z_WIDTH,
                    LegacyTileRenderPlans.PUMPJACK_BACK_ROD_X * side,
                    LegacyTileRenderPlans.PUMPJACK_ROTOR_PIVOT_Y + rotorY,
                    LegacyTileRenderPlans.PUMPJACK_ROTOR_PIVOT_Z + rotorZ
                            + LegacyTileRenderPlans.PUMPJACK_BACK_ROD_Z_WIDTH,
                    LegacyTileRenderPlans.PUMPJACK_BACK_ROD_X * side,
                    LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Y + backY,
                    LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Z + backZ
                            + LegacyTileRenderPlans.PUMPJACK_BACK_ROD_Z_WIDTH,
                    LegacyTileRenderPlans.PUMPJACK_BACK_ROD_X * side,
                    LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Y + backY,
                    LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Z + backZ
                            - LegacyTileRenderPlans.PUMPJACK_BACK_ROD_Z_WIDTH,
                    LegacyTileRenderPlans.PUMPJACK_BACK_ROD_COLOR);
        }

        double height = -sin;
        double frontPosY = headSin;
        double frontPosZ = headCos;
        double frontRadius = LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_RADIUS
                + LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_DIST;
        double frontCutletRadians = LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_CUTLET
                * LegacyTileRenderPlans.DEG_TO_RAD;
        double initialFrontRadiusAngle = headRadians + frontCutletRadians * 3.0D;
        for (int side = -1; side <= 1; side += 2) {
            double frontRadiusAngle = initialFrontRadiusAngle;
            for (int segment = 0; segment < LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_SEGMENTS; segment++) {
                double startRadY = frontRadius * Math.sin(frontRadiusAngle);
                double startRadZ = frontRadius * Math.cos(frontRadiusAngle);
                double startY = fixedPumpjackFrontRodY(frontPosY, startRadY);
                double startZ = fixedPumpjackFrontRodZ(frontPosZ, startRadY, startRadZ);
                frontRadiusAngle -= frontCutletRadians;
                double endRadY = frontRadius * Math.sin(frontRadiusAngle);
                double endRadZ = frontRadius * Math.cos(frontRadiusAngle);
                double endY = fixedPumpjackFrontRodY(frontPosY, endRadY);
                double endZ = fixedPumpjackFrontRodZ(frontPosZ, endRadY, endRadZ);
                renderFrontRodQuad(batch, side, startY, startZ, endY, endZ);
            }

            double tailRadY = frontRadius * Math.sin(frontRadiusAngle);
            double tailRadZ = frontRadius * Math.cos(frontRadiusAngle);
            double tailY = fixedPumpjackFrontRodY(frontPosY, tailRadY);
            double tailZ = fixedPumpjackFrontRodZ(frontPosZ, tailRadY, tailRadZ);
            renderRodQuad(batch,
                    (LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_WIDTH
                            + LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_HALF_THICKNESS) * side,
                    LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Y + tailY,
                    LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Z + tailZ,
                    (LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_WIDTH
                            - LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_HALF_THICKNESS) * side,
                    LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Y + tailY,
                    LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Z + tailZ,
                    (LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_WIDTH
                            - LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_HALF_THICKNESS) * side,
                    2.0D + height, 0.0D,
                    (LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_WIDTH
                            + LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_HALF_THICKNESS) * side,
                    2.0D + height, 0.0D,
                    LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_COLOR);
        }

        double p = LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_HALF_THICKNESS;
        renderRodQuad(batch,
                p, height + 1.5D, p,
                -p, height + 1.5D, -p,
                -p, 0.75D, -p,
                p, 0.75D, p,
                LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_COLOR);
        renderRodQuad(batch,
                -p, height + 1.5D, p,
                p, height + 1.5D, -p,
                p, 0.75D, -p,
                -p, 0.75D, p,
                LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_COLOR);
    }

    private static void renderFrontRodQuad(LegacyUntexturedQuadRenderer.DirectQuadBatch batch, int side,
            double startY, double startZ, double endY, double endZ) {
        renderRodQuad(batch,
                (LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_WIDTH
                        - LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_HALF_THICKNESS) * side,
                LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Y + startY,
                LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Z + startZ,
                (LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_WIDTH
                        + LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_HALF_THICKNESS) * side,
                LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Y + startY,
                LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Z + startZ,
                (LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_WIDTH
                        + LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_HALF_THICKNESS) * side,
                LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Y + endY,
                LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Z + endZ,
                (LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_WIDTH
                        - LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_HALF_THICKNESS) * side,
                LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Y + endY,
                LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Z + endZ,
                LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_COLOR);
    }

    private static double fixedPumpjackFrontRodY(double frontPosY, double frontRadY) {
        return frontPosY + frontRadY;
    }

    private static double fixedPumpjackFrontRodZ(double frontPosZ, double frontRadY, double frontRadZ) {
        if (frontRadY < 0.0D) {
            return -LegacyTileRenderPlans.PUMPJACK_HEAD_PIVOT_Z
                    + LegacyTileRenderPlans.PUMPJACK_FRONT_ROD_DIST * 0.5D;
        }
        return frontPosZ + frontRadZ;
    }

    private static void renderRodQuad(LegacyUntexturedQuadRenderer.DirectQuadBatch batch,
            double x0, double y0, double z0,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            double x3, double y3, double z3,
            int color) {
        LegacyUntexturedQuadRenderer.quadDirect(batch,
                x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                color, 255, 255, 255, 255);
    }
}
