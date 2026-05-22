package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public final class LegacyHorseRenderer {
    public static final int ID_HEAD = 0;
    public static final int ID_LEFT_FRONT_LEG = 1;
    public static final int ID_RIGHT_FRONT_LEG = 2;
    public static final int ID_LEFT_BACK_LEG = 3;
    public static final int ID_RIGHT_BACK_LEG = 4;
    public static final int ID_TAIL = 5;
    public static final int ID_BODY = 6;
    public static final int ID_POSITION = 7;

    public static final ResourceLocation HORSE_DEMO_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/horse/horse_demo.png");
    public static final ResourceLocation SUNBURST_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/horse/sunburst.png");
    public static final ResourceLocation DYX_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/horse/dyx.png");
    public static final ResourceLocation NUMBER_NINE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/horse/numbernine.png");

    private static final ResourceLocation HORSE_MODEL =
            new ResourceLocation(HbmNtm.MOD_ID, "models/block/mobs/horse.obj");
    private static final LegacyWavefrontModel MODEL = new LegacyWavefrontModel(HORSE_MODEL, HORSE_DEMO_TEXTURE);
    private static final double[][] OFFSETS = new double[][] {
            { 0.0D, 1.125D, 0.375D },
            { 0.125D, 0.75D, 0.3125D },
            { -0.125D, 0.75D, 0.3125D },
            { 0.125D, 0.75D, -0.25D },
            { -0.125D, 0.75D, -0.25D },
            { 0.0D, 1.125D, -0.4375D },
            { 0.0D, 0.0D, 0.0D },
            { 0.0D, 0.0D, 0.0D }
    };

    private final double[][] pose = new double[8][3];
    private boolean wings;
    private boolean horn;
    private boolean maleSnoot;

    public void reset() {
        wings = false;
        horn = false;
        maleSnoot = false;
        for (double[] angles : pose) {
            angles[0] = 0.0D;
            angles[1] = 0.0D;
            angles[2] = 0.0D;
        }
    }

    public void enableHorn() {
        horn = true;
    }

    public void enableWings() {
        wings = true;
    }

    public void setMaleSnoot() {
        maleSnoot = true;
    }

    public void setAlicorn() {
        enableHorn();
        enableWings();
    }

    public void poseStandardSit() {
        double r = 60.0D;
        pose(ID_BODY, 0.0D, -r, 0.0D);
        pose(ID_TAIL, 0.0D, 45.0D, 90.0D);
        pose(ID_LEFT_BACK_LEG, 0.0D, -90.0D + r, 35.0D);
        pose(ID_RIGHT_BACK_LEG, 0.0D, -90.0D + r, -35.0D);
        pose(ID_LEFT_FRONT_LEG, 0.0D, r - 10.0D, 5.0D);
        pose(ID_RIGHT_FRONT_LEG, 0.0D, r - 10.0D, -5.0D);
        pose(ID_HEAD, 0.0D, r, 0.0D);
    }

    public void pose(int id, double yaw, double pitch, double roll) {
        if (id < 0 || id >= pose.length) {
            return;
        }
        pose[id][0] = yaw;
        pose[id][1] = pitch;
        pose[id][2] = roll;
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation texture,
            int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyTransform(poseStack, ID_BODY);
        MODEL.renderPart("Body", texture, poseStack, buffer, packedLight, packedOverlay);

        if (horn) {
            renderWithTransform(poseStack, buffer, texture, packedLight, packedOverlay,
                    ID_HEAD, "Head", "Mane", maleSnoot ? "NoseMale" : "NoseFemale", "HornPointy");
        } else {
            renderWithTransform(poseStack, buffer, texture, packedLight, packedOverlay,
                    ID_HEAD, "Head", "Mane", maleSnoot ? "NoseMale" : "NoseFemale");
        }

        renderWithTransform(poseStack, buffer, texture, packedLight, packedOverlay, ID_LEFT_FRONT_LEG, "LeftFrontLeg");
        renderWithTransform(poseStack, buffer, texture, packedLight, packedOverlay, ID_RIGHT_FRONT_LEG, "RightFrontLeg");
        renderWithTransform(poseStack, buffer, texture, packedLight, packedOverlay, ID_LEFT_BACK_LEG, "LeftBackLeg");
        renderWithTransform(poseStack, buffer, texture, packedLight, packedOverlay, ID_RIGHT_BACK_LEG, "RightBackLeg");
        renderWithTransform(poseStack, buffer, texture, packedLight, packedOverlay, ID_TAIL, "Tail");

        if (wings) {
            MODEL.renderPart("LeftWing", texture, poseStack, buffer, packedLight, packedOverlay);
            MODEL.renderPart("RightWing", texture, poseStack, buffer, packedLight, packedOverlay);
        }

        poseStack.popPose();
    }

    private void renderWithTransform(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation texture,
            int packedLight, int packedOverlay, int id, String... parts) {
        poseStack.pushPose();
        applyTransform(poseStack, id);
        for (String part : parts) {
            MODEL.renderPart(part, texture, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private void applyTransform(PoseStack poseStack, int id) {
        double[] rotation = pose[id];
        double[] offset = OFFSETS[id];
        poseStack.translate(offset[0], offset[1], offset[2]);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) rotation[0]));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) rotation[1]));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) rotation[2]));
        poseStack.translate(-offset[0], -offset[1], -offset[2]);
    }
}
