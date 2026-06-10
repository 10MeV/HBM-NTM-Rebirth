package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

public final class LegacyObjTransforms {
    public static final float OBJ_UTIL_MIN_SHADOW = 0.45F;

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

    public static ObjUtilDirectionPlan directionPlan(Direction direction) {
        float yawRadians = yawRadians(direction);
        float pitchRadians = pitchRadians(direction);
        return new ObjUtilDirectionPlan(direction, yawRadians, pitchRadians,
                (float) Math.toDegrees(yawRadians), (float) Math.toDegrees(pitchRadians), Direction.EAST);
    }

    public static ObjUtilRotationPlan objUtilRotationPlan(Direction direction) {
        return objUtilRotationPlan(direction, 0.0F);
    }

    public static ObjUtilRotationPlan objUtilRotationPlan(Direction direction, float rollRadians) {
        return objUtilRotationPlan(yawRadians(direction), pitchRadians(direction), rollRadians);
    }

    public static ObjUtilRotationPlan objUtilRotationPlan(float yawRadians, float pitchRadians, float rollRadians) {
        return new ObjUtilRotationPlan(yawRadians, pitchRadians, rollRadians,
                (float) Math.toDegrees(yawRadians), (float) Math.toDegrees(pitchRadians), (float) Math.toDegrees(rollRadians),
                "X_ROLL_Z_PITCH_Y_YAW", "Z_PITCH_Y_YAW");
    }

    public static void applyObjUtilRotation(PoseStack poseStack, float yawRadians, float pitchRadians, float rollRadians) {
        poseStack.mulPose(Axis.XP.rotation(rollRadians));
        poseStack.mulPose(Axis.ZP.rotation(pitchRadians));
        poseStack.mulPose(Axis.YP.rotation(yawRadians));
    }

    public static void applyObjUtilRotation(PoseStack poseStack, Direction direction) {
        applyObjUtilRotation(poseStack, yawRadians(direction), pitchRadians(direction), 0.0F);
    }

    public static Vector3f rotateObjUtilVertex(Vector3f vertex, float yawRadians, float pitchRadians, float rollRadians) {
        return new Vector3f(vertex).rotateX(rollRadians).rotateZ(pitchRadians).rotateY(yawRadians);
    }

    public static Vector3f rotateObjUtilNormal(Vector3f normal, float yawRadians, float pitchRadians) {
        return new Vector3f(normal).rotateZ(pitchRadians).rotateY(yawRadians);
    }

    public static float objUtilAllShadowFactor(Vector3f rotatedNormal) {
        float brightness = (rotatedNormal.y() + 0.7F) * 0.9F
                - Math.abs(rotatedNormal.x()) * 0.1F
                + Math.abs(rotatedNormal.z()) * 0.1F;
        return Math.max(OBJ_UTIL_MIN_SHADOW, brightness);
    }

    public static float objUtilPartShadowFactor(Vector3f rotatedNormal) {
        float brightness = rotatedNormal.y() * 0.3F + 0.7F
                - Math.abs(rotatedNormal.x()) * 0.1F
                + Math.abs(rotatedNormal.z()) * 0.1F;
        return Math.max(OBJ_UTIL_MIN_SHADOW, brightness);
    }

    public static ObjUtilShadowPlan objUtilShadowPlan(Vector3f normal, float yawRadians, float pitchRadians) {
        Vector3f rotated = rotateObjUtilNormal(normal, yawRadians, pitchRadians);
        return new ObjUtilShadowPlan(normal.x(), normal.y(), normal.z(),
                rotated.x(), rotated.y(), rotated.z(),
                objUtilAllShadowFactor(rotated), objUtilPartShadowFactor(rotated), OBJ_UTIL_MIN_SHADOW);
    }

    public static ObjUtilColorPlan objUtilColorPlan(int color) {
        int rgb = color & 0xFFFFFF;
        return new ObjUtilColorPlan(true, rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, rgb);
    }

    public static ObjUtilColorPlan objUtilColorPlan(int red, int green, int blue) {
        int clampedRed = clampColor(red);
        int clampedGreen = clampColor(green);
        int clampedBlue = clampColor(blue);
        return new ObjUtilColorPlan(true, clampedRed, clampedGreen, clampedBlue,
                clampedRed << 16 | clampedGreen << 8 | clampedBlue);
    }

    public static ObjUtilColorPlan clearObjUtilColorPlan() {
        return new ObjUtilColorPlan(false, 255, 255, 255, 0xFFFFFF);
    }

    public static void rotateAroundBlockCenterY(PoseStack poseStack, float degrees) {
        rotateAroundY(poseStack, 0.5D, 0.0D, 0.5D, degrees);
    }

    public static void rotateAroundY(PoseStack poseStack, double pivotX, double pivotY, double pivotZ, float degrees) {
        poseStack.translate(pivotX, pivotY, pivotZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(degrees));
        poseStack.translate(-pivotX, -pivotY, -pivotZ);
    }

    public static void rotateAroundX(PoseStack poseStack, double pivotX, double pivotY, double pivotZ, float degrees) {
        poseStack.translate(pivotX, pivotY, pivotZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(degrees));
        poseStack.translate(-pivotX, -pivotY, -pivotZ);
    }

    public static void rotateAroundZ(PoseStack poseStack, double pivotX, double pivotY, double pivotZ, float degrees) {
        poseStack.translate(pivotX, pivotY, pivotZ);
        poseStack.mulPose(Axis.ZP.rotationDegrees(degrees));
        poseStack.translate(-pivotX, -pivotY, -pivotZ);
    }

    public static double softPeakSine(double value) {
        return Math.sin(Math.PI / 2.0D * Math.cos(value));
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

    private static int clampColor(int color) {
        return Math.max(0, Math.min(255, color));
    }

    public record ObjUtilDirectionPlan(Direction direction, float yawRadians, float pitchRadians,
                                       float yawDegrees, float pitchDegrees, Direction legacyModelFacing) {
    }

    public record ObjUtilRotationPlan(float yawRadians, float pitchRadians, float rollRadians,
                                      float yawDegrees, float pitchDegrees, float rollDegrees,
                                      String vertexOrder, String normalOrder) {
        public Vector3f rotateVertex(Vector3f vertex) {
            return LegacyObjTransforms.rotateObjUtilVertex(vertex, yawRadians, pitchRadians, rollRadians);
        }

        public Vector3f rotateNormal(Vector3f normal) {
            return LegacyObjTransforms.rotateObjUtilNormal(normal, yawRadians, pitchRadians);
        }
    }

    public record ObjUtilShadowPlan(float normalX, float normalY, float normalZ,
                                    float rotatedNormalX, float rotatedNormalY, float rotatedNormalZ,
                                    float allBrightness, float partBrightness, float minBrightness) {
    }

    public record ObjUtilColorPlan(boolean hasColor, int red, int green, int blue, int color) {
        public int shadedColor(float brightness) {
            return clampColor((int) (red * brightness)) << 16
                    | clampColor((int) (green * brightness)) << 8
                    | clampColor((int) (blue * brightness));
        }
    }
}
