package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

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
            ObjRenderContext context, PoseStack poseStack) {
        renderPart(model, "Furnace", context);
        renderTranslatedPart(model, plan.contentsHot(), fullbrightContext(context, plan.fullbright()), poseStack);
        renderTranslatedPart(model, plan.contentsCold(), context, poseStack);

        poseStack.pushPose();
        LegacyTileRenderPlans.TranslatedModelPartPlan lid = plan.lid();
        poseStack.translate(lid.translateX(), lid.translateY(), lid.translateZ());
        renderPart(model, lid.partName(), context);
        for (LegacyTileRenderPlans.ArcElectrodePlan electrode : plan.electrodes()) {
            renderElectrode(model, electrode, plan, context);
        }
        for (LegacyTileRenderPlans.RotatingModelPartPlan cable : plan.cables()) {
            renderRotatingPart(model, cable, context, poseStack);
        }
        poseStack.popPose();
    }

    private static void renderElectrode(LegacyWavefrontModel model,
            LegacyTileRenderPlans.ArcElectrodePlan electrode, LegacyTileRenderPlans.ArcFurnacePlan plan,
            ObjRenderContext context) {
        renderOptionalPart(model, electrode.ringPartName(), context);
        renderOptionalPart(model, electrode.freshPartName(), context);
        ObjRenderContext bright = fullbrightContext(context, plan.fullbright());
        renderOptionalPart(model, electrode.usedHotPartName(), bright);
        renderOptionalPart(model, electrode.depletedShortPartName(), bright);
    }

    private static void renderOptionalPart(LegacyWavefrontModel model, String partName, ObjRenderContext context) {
        if (partName != null) {
            renderPart(model, partName, context);
        }
    }

    private static void renderTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, ObjRenderContext context, PoseStack poseStack) {
        if (part == null || !part.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        renderPart(model, part.partName(), context);
        poseStack.popPose();
    }

    private static void renderRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderPart(model, part.partName(), context);
        poseStack.popPose();
    }

    private static void renderPart(LegacyWavefrontModel model, String partName, ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle = handle(partName);
        if (model == MODEL && handle != null) {
            MODEL.renderOnlyInCallOrder(context, handle);
            return;
        }
        model.renderPart(partName, context);
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

    private static ObjRenderContext fullbrightContext(ObjRenderContext context,
            LegacyTileRenderPlans.FullbrightStatePlan plan) {
        return plan == null ? context : context.withLegacyLightmap(plan.lightmapX(), plan.lightmapY());
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
