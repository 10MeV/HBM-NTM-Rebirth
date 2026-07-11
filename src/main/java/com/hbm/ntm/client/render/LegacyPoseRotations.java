package com.hbm.ntm.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;

public final class LegacyPoseRotations {
    private LegacyPoseRotations() {
    }

    public static void rotateXDegrees(PoseStack poseStack, float degrees) {
        rotateXRadians(poseStack, degrees * Mth.DEG_TO_RAD);
    }

    public static void rotateYDegrees(PoseStack poseStack, float degrees) {
        rotateYRadians(poseStack, degrees * Mth.DEG_TO_RAD);
    }

    public static void rotateZDegrees(PoseStack poseStack, float degrees) {
        rotateZRadians(poseStack, degrees * Mth.DEG_TO_RAD);
    }

    public static void rotateXRadians(PoseStack poseStack, float radians) {
        PoseStack.Pose pose = poseStack.last();
        pose.pose().rotateX(radians);
        pose.normal().rotateX(radians);
    }

    public static void rotateYRadians(PoseStack poseStack, float radians) {
        PoseStack.Pose pose = poseStack.last();
        pose.pose().rotateY(radians);
        pose.normal().rotateY(radians);
    }

    public static void rotateZRadians(PoseStack poseStack, float radians) {
        PoseStack.Pose pose = poseStack.last();
        pose.pose().rotateZ(radians);
        pose.normal().rotateZ(radians);
    }
}
