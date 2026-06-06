package com.hbm.ntm.client.anim;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import java.util.Arrays;

public final class LegacyBusAnimationTransforms {
    public static final int SIZE = 15;
    private static final double[] IDENTITY = {
            0.0D, 0.0D, 0.0D,
            0.0D, 0.0D, 0.0D,
            1.0D, 1.0D, 1.0D,
            0.0D, 0.0D, 0.0D,
            0.0D, 1.0D, 2.0D
    };

    public static double[] identity() {
        return Arrays.copyOf(IDENTITY, IDENTITY.length);
    }

    public static void apply(PoseStack poseStack, double[] transform) {
        if (transform == null || transform.length < SIZE) {
            transform = IDENTITY;
        }

        poseStack.translate(transform[0], transform[1], transform[2]);
        for (int i = 0; i < 3; i++) {
            int axis = (int) transform[12 + i];
            float degrees = (float) transform[3 + axis];
            if (axis == 0) {
                poseStack.mulPose(Axis.XP.rotationDegrees(degrees));
            } else if (axis == 1) {
                poseStack.mulPose(Axis.YP.rotationDegrees(degrees));
            } else if (axis == 2) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(degrees));
            }
        }
        poseStack.translate(-transform[9], -transform[10], -transform[11]);
        poseStack.scale((float) transform[6], (float) transform[7], (float) transform[8]);
    }

    private LegacyBusAnimationTransforms() {
    }
}
