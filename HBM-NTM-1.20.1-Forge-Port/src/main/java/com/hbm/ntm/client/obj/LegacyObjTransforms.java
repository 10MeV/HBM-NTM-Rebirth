package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;

public final class LegacyObjTransforms {
    public static float yawRadians(Direction direction) {
        return switch (direction) {
            case NORTH -> (float) Math.PI * 0.5F;
            case SOUTH -> (float) Math.PI * -0.5F;
            case WEST -> (float) Math.PI;
            default -> 0.0F;
        };
    }

    public static float pitchRadians(Direction direction) {
        return switch (direction) {
            case UP -> (float) Math.PI * -0.5F;
            case DOWN -> (float) Math.PI * 0.5F;
            default -> 0.0F;
        };
    }

    public static float yawDegrees(Direction direction) {
        return (float) Math.toDegrees(yawRadians(direction));
    }

    public static float pitchDegrees(Direction direction) {
        return (float) Math.toDegrees(pitchRadians(direction));
    }

    public static void applyObjUtilRotation(PoseStack poseStack, float yawRadians, float pitchRadians, float rollRadians) {
        poseStack.mulPose(Axis.XP.rotation(rollRadians));
        poseStack.mulPose(Axis.ZP.rotation(pitchRadians));
        poseStack.mulPose(Axis.YP.rotation(yawRadians));
    }

    public static void applyObjUtilRotation(PoseStack poseStack, Direction direction) {
        applyObjUtilRotation(poseStack, yawRadians(direction), pitchRadians(direction), 0.0F);
    }

    public static void rotateAroundBlockCenterY(PoseStack poseStack, float degrees) {
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(degrees));
        poseStack.translate(-0.5D, 0.0D, -0.5D);
    }

    public static void applySixFaceAttachmentRotation(PoseStack poseStack, Direction face) {
        switch (face) {
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            case NORTH -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case WEST -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
            case EAST -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(270.0F));
            }
            default -> {
            }
        }
    }

    private LegacyObjTransforms() {
    }
}
