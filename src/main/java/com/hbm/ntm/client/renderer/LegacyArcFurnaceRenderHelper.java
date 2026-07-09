package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;

public final class LegacyArcFurnaceRenderHelper {
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_ARC_FURNACE;
    private static final LegacyWavefrontModel.SelectionHandle FURNACE =
            MODEL.prepareRenderOnlyInCallOrder("Furnace");
    private static final LegacyWavefrontModel.SelectionHandle CONTENTS_HOT =
            MODEL.prepareRenderOnlyInCallOrder("ContentsHot");
    private static final LegacyWavefrontModel.SelectionHandle CONTENTS_COLD =
            MODEL.prepareRenderOnlyInCallOrder("ContentsCold");
    private static final LegacyWavefrontModel.SelectionHandle LID =
            MODEL.prepareRenderOnlyInCallOrder("Lid");
    private static final LegacyWavefrontModel.SelectionHandle RING_1 =
            MODEL.prepareRenderOnlyInCallOrder("Ring1");
    private static final LegacyWavefrontModel.SelectionHandle RING_2 =
            MODEL.prepareRenderOnlyInCallOrder("Ring2");
    private static final LegacyWavefrontModel.SelectionHandle RING_3 =
            MODEL.prepareRenderOnlyInCallOrder("Ring3");
    private static final LegacyWavefrontModel.SelectionHandle ELECTRODE_1 =
            MODEL.prepareRenderOnlyInCallOrder("Electrode1");
    private static final LegacyWavefrontModel.SelectionHandle ELECTRODE_2 =
            MODEL.prepareRenderOnlyInCallOrder("Electrode2");
    private static final LegacyWavefrontModel.SelectionHandle ELECTRODE_3 =
            MODEL.prepareRenderOnlyInCallOrder("Electrode3");
    private static final LegacyWavefrontModel.SelectionHandle ELECTRODE_1_HOT =
            MODEL.prepareRenderOnlyInCallOrder("Electrode1Hot");
    private static final LegacyWavefrontModel.SelectionHandle ELECTRODE_2_HOT =
            MODEL.prepareRenderOnlyInCallOrder("Electrode2Hot");
    private static final LegacyWavefrontModel.SelectionHandle ELECTRODE_3_HOT =
            MODEL.prepareRenderOnlyInCallOrder("Electrode3Hot");
    private static final LegacyWavefrontModel.SelectionHandle ELECTRODE_1_SHORT =
            MODEL.prepareRenderOnlyInCallOrder("Electrode1Short");
    private static final LegacyWavefrontModel.SelectionHandle ELECTRODE_2_SHORT =
            MODEL.prepareRenderOnlyInCallOrder("Electrode2Short");
    private static final LegacyWavefrontModel.SelectionHandle ELECTRODE_3_SHORT =
            MODEL.prepareRenderOnlyInCallOrder("Electrode3Short");
    private static final LegacyWavefrontModel.SelectionHandle CABLE_1 =
            MODEL.prepareRenderOnlyInCallOrder("Cable1");
    private static final LegacyWavefrontModel.SelectionHandle CABLE_2 =
            MODEL.prepareRenderOnlyInCallOrder("Cable2");
    private static final LegacyWavefrontModel.SelectionHandle CABLE_3 =
            MODEL.prepareRenderOnlyInCallOrder("Cable3");

    public static void renderPlan(LegacyWavefrontModel model, LegacyTileRenderPlans.ArcFurnacePlan plan,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderStaticShell(model, poseStack, buffer, packedLight, packedOverlay, renderMode);
        renderAnimatedPlan(model, plan, poseStack, buffer, packedLight, packedOverlay, renderMode);
    }

    public static void renderStaticShell(LegacyWavefrontModel model, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        renderPart(model, "Furnace", poseStack, buffer, packedLight, packedOverlay, renderMode);
    }

    public static void renderStaticPreview(LegacyWavefrontModel model, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        renderStaticShell(model, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.pushPose();
        renderPart(model, "Lid", poseStack, buffer, packedLight, packedOverlay, renderMode);
        renderElectrodeDirect(model, 0, (byte) 1, poseStack, buffer, packedLight, packedOverlay, renderMode);
        renderElectrodeDirect(model, 1, (byte) 1, poseStack, buffer, packedLight, packedOverlay, renderMode);
        renderElectrodeDirect(model, 2, (byte) 1, poseStack, buffer, packedLight, packedOverlay, renderMode);
        renderCableDirect(model, 0, (byte) 1, 0.0D, poseStack, buffer, packedLight, packedOverlay, renderMode);
        renderCableDirect(model, 1, (byte) 1, 0.0D, poseStack, buffer, packedLight, packedOverlay, renderMode);
        renderCableDirect(model, 2, (byte) 1, 0.0D, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    public static void renderAnimatedDirect(LegacyWavefrontModel model, double prevLid, double lid,
            boolean progressing, long worldTime, float partialTicks, int liquidAmount, int maxLiquid,
            boolean hasMaterial, byte electrode0, byte electrode1, byte electrode2,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        double time = LegacyUvAnimation.tickTime(worldTime, partialTicks);
        double lift = prevLid + (lid - prevLid) * partialTicks;
        if (liquidAmount > 0 && maxLiquid > 0) {
            poseStack.pushPose();
            poseStack.translate(0.0D, LegacyTileRenderPlans.ARC_FURNACE_CONTENT_BASE_Y
                    + liquidAmount * LegacyTileRenderPlans.ARC_FURNACE_CONTENT_TRAVEL / (double) maxLiquid, 0.0D);
            renderPart(model, "ContentsHot", poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay, renderMode);
            poseStack.popPose();
        } else if (hasMaterial) {
            renderPart(model, "ContentsCold", poseStack, buffer, packedLight, packedOverlay, renderMode);
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, LegacyTileRenderPlans.ARC_FURNACE_LID_TRAVEL * lift,
                progressing ? Math.sin(time) * LegacyTileRenderPlans.ARC_FURNACE_LID_WOBBLE : 0.0D);
        renderPart(model, "Lid", poseStack, buffer, packedLight, packedOverlay, renderMode);
        renderElectrodeDirect(model, 0, electrode0, poseStack, buffer, packedLight, packedOverlay, renderMode);
        renderElectrodeDirect(model, 1, electrode1, poseStack, buffer, packedLight, packedOverlay, renderMode);
        renderElectrodeDirect(model, 2, electrode2, poseStack, buffer, packedLight, packedOverlay, renderMode);
        double cableAngle = progressing
                ? Math.sin(time / 2.0D) * LegacyTileRenderPlans.ARC_FURNACE_CABLE_WOBBLE_DEGREES
                : 0.0D;
        renderCableDirect(model, 0, electrode0, cableAngle, poseStack, buffer, packedLight, packedOverlay, renderMode);
        renderCableDirect(model, 1, electrode1, cableAngle, poseStack, buffer, packedLight, packedOverlay, renderMode);
        renderCableDirect(model, 2, electrode2, cableAngle, poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    public static void renderAnimatedPlan(LegacyWavefrontModel model, LegacyTileRenderPlans.ArcFurnacePlan plan,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderTranslatedPart(model, plan.contentsHot(), poseStack, buffer,
                fullbrightLight(packedLight, plan.fullbright()), packedOverlay, renderMode);
        renderTranslatedPart(model, plan.contentsCold(), poseStack, buffer, packedLight, packedOverlay, renderMode);

        poseStack.pushPose();
        LegacyTileRenderPlans.TranslatedModelPartPlan lid = plan.lid();
        poseStack.translate(lid.translateX(), lid.translateY(), lid.translateZ());
        renderPart(model, lid.partName(), poseStack, buffer, packedLight, packedOverlay, renderMode);
        for (LegacyTileRenderPlans.ArcElectrodePlan electrode : plan.electrodes()) {
            renderElectrode(model, electrode, plan, poseStack, buffer, packedLight, packedOverlay, renderMode);
        }
        for (LegacyTileRenderPlans.RotatingModelPartPlan cable : plan.cables()) {
            renderRotatingPart(model, cable, poseStack, buffer, packedLight, packedOverlay, renderMode);
        }
        poseStack.popPose();
    }

    private static void renderElectrode(LegacyWavefrontModel model,
            LegacyTileRenderPlans.ArcElectrodePlan electrode, LegacyTileRenderPlans.ArcFurnacePlan plan,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        renderOptionalPart(model, electrode.ringPartName(), poseStack, buffer, packedLight, packedOverlay, renderMode);
        renderOptionalPart(model, electrode.freshPartName(), poseStack, buffer, packedLight, packedOverlay, renderMode);
        int brightLight = fullbrightLight(packedLight, plan.fullbright());
        renderOptionalPart(model, electrode.usedHotPartName(), poseStack, buffer, brightLight, packedOverlay, renderMode);
        renderOptionalPart(model, electrode.depletedShortPartName(), poseStack, buffer, brightLight,
                packedOverlay, renderMode);
    }

    private static void renderOptionalPart(LegacyWavefrontModel model, String partName,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        if (partName != null) {
            renderPart(model, partName, poseStack, buffer, packedLight, packedOverlay, renderMode);
        }
    }

    private static void renderElectrodeDirect(LegacyWavefrontModel model, int index, byte state,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        if (state == 0) {
            return;
        }
        int number = index + 1;
        renderPart(model, "Ring" + number, poseStack, buffer, packedLight, packedOverlay, renderMode);
        switch (state) {
            case 1 -> renderPart(model, "Electrode" + number, poseStack, buffer,
                    packedLight, packedOverlay, renderMode);
            case 2 -> renderPart(model, "Electrode" + number + "Hot", poseStack, buffer,
                    LightTexture.FULL_BRIGHT, packedOverlay, renderMode);
            case 3 -> renderPart(model, "Electrode" + number + "Short", poseStack, buffer,
                    LightTexture.FULL_BRIGHT, packedOverlay, renderMode);
            default -> {
            }
        }
    }

    private static void renderCableDirect(LegacyWavefrontModel model, int index, byte state, double angleDegrees,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        if (state == 0) {
            return;
        }
        double pivotZ = switch (index) {
            case 0 -> 0.5D;
            case 2 -> -0.5D;
            default -> 0.0D;
        };
        poseStack.pushPose();
        poseStack.translate(0.0D, LegacyTileRenderPlans.ARC_FURNACE_CABLE_PIVOT_Y, pivotZ);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) angleDegrees));
        poseStack.translate(0.0D, -LegacyTileRenderPlans.ARC_FURNACE_CABLE_PIVOT_Y, -pivotZ);
        renderPart(model, "Cable" + (index + 1), poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        if (part == null || !part.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        renderPart(model, part.partName(), poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderPart(model, part.partName(), poseStack, buffer, packedLight, packedOverlay, renderMode);
        poseStack.popPose();
    }

    private static void renderPart(LegacyWavefrontModel model, String partName, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle = handle(partName);
        if (sameModel(model) && handle != null) {
            MODEL.renderOnlyInCallOrder(model.textureLocation(), poseStack, buffer, packedLight, packedOverlay,
                    handle, renderMode);
            return;
        }
        model.renderPart(partName, model.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
    }

    private static boolean sameModel(LegacyWavefrontModel model) {
        return model == MODEL || model.modelLocation().equals(MODEL.modelLocation());
    }

    private static LegacyWavefrontModel.SelectionHandle handle(String partName) {
        return switch (partName) {
            case "Furnace" -> FURNACE;
            case "ContentsHot" -> CONTENTS_HOT;
            case "ContentsCold" -> CONTENTS_COLD;
            case "Lid" -> LID;
            case "Ring1" -> RING_1;
            case "Ring2" -> RING_2;
            case "Ring3" -> RING_3;
            case "Electrode1" -> ELECTRODE_1;
            case "Electrode2" -> ELECTRODE_2;
            case "Electrode3" -> ELECTRODE_3;
            case "Electrode1Hot" -> ELECTRODE_1_HOT;
            case "Electrode2Hot" -> ELECTRODE_2_HOT;
            case "Electrode3Hot" -> ELECTRODE_3_HOT;
            case "Electrode1Short" -> ELECTRODE_1_SHORT;
            case "Electrode2Short" -> ELECTRODE_2_SHORT;
            case "Electrode3Short" -> ELECTRODE_3_SHORT;
            case "Cable1" -> CABLE_1;
            case "Cable2" -> CABLE_2;
            case "Cable3" -> CABLE_3;
            default -> null;
        };
    }

    private static int fullbrightLight(int packedLight, LegacyTileRenderPlans.FullbrightStatePlan plan) {
        return plan == null ? packedLight : LightTexture.FULL_BRIGHT;
    }

    private static void rotate(PoseStack poseStack, float axisX, float axisY, float axisZ, double degrees) {
        if (axisX != 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) (degrees * axisX)));
        }
        if (axisY != 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (degrees * axisY)));
        }
        if (axisZ != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (degrees * axisZ)));
        }
    }

    private LegacyArcFurnaceRenderHelper() {
    }
}
