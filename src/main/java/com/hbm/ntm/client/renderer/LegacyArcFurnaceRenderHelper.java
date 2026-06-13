package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public final class LegacyArcFurnaceRenderHelper {
    public static void renderPlan(LegacyWavefrontModel model, LegacyTileRenderPlans.ArcFurnacePlan plan,
            ObjRenderContext context, PoseStack poseStack) {
        model.renderPart("Furnace", context);
        renderTranslatedPart(model, plan.contentsHot(), fullbrightContext(context, plan.fullbright()), poseStack);
        renderTranslatedPart(model, plan.contentsCold(), context, poseStack);

        poseStack.pushPose();
        LegacyTileRenderPlans.TranslatedModelPartPlan lid = plan.lid();
        poseStack.translate(lid.translateX(), lid.translateY(), lid.translateZ());
        model.renderPart(lid.partName(), context);
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
            model.renderPart(partName, context);
        }
    }

    private static void renderTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, ObjRenderContext context, PoseStack poseStack) {
        if (part == null || !part.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        model.renderPart(part.partName(), context);
        poseStack.popPose();
    }

    private static void renderRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        model.renderPart(part.partName(), context);
        poseStack.popPose();
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
