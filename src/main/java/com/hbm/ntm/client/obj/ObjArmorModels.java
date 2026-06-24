package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public final class ObjArmorModels {
    public static final LegacyWavefrontModel BJ = model("bj", "bj_chest").asVBO();
    public static final LegacyWavefrontModel HEV = model("hev", "hev_chest").asVBO();
    public static final LegacyWavefrontModel AJR = model("ajr", "ajr_chest").asVBO();
    public static final LegacyWavefrontModel T51 = model("t51", "t51_chest").asVBO();
    public static final LegacyWavefrontModel HAT = model("hat").asVBO();
    public static final LegacyWavefrontModel NO9 = model("no9").asVBO();
    public static final LegacyWavefrontModel GOGGLES = model("goggles").asVBO();
    public static final LegacyWavefrontModel FAU = model("fau", "fau_chest").asVBO();
    public static final LegacyWavefrontModel DNT = model("dnt", "dnt_chest").asVBO();
    public static final LegacyWavefrontModel STEAMSUIT = model("steamsuit", "steamsuit_chest").asVBO();
    public static final LegacyWavefrontModel DIESELSUIT = model("bnuuy", "bnuuy_chest").asVBO();
    public static final LegacyWavefrontModel REMNANT = model("remnant", "rpa_chest").asVBO();
    public static final LegacyWavefrontModel NCR = model("ncrpa", "ncrpa_chest").asVBO();
    public static final LegacyWavefrontModel BISMUTH = model("bismuth").asVBO();
    public static final LegacyWavefrontModel MOD_TESLA = model("mod_tesla").asVBO();
    public static final LegacyWavefrontModel WINGS = model("murk", "wings_murk").asVBO();
    public static final LegacyWavefrontModel AXEPACK = model("wings_pheo", "axepack").asVBO();
    public static final LegacyWavefrontModel TAIL = model("tail_peep").asVBO();
    public static final LegacyWavefrontModel PLAYER_FEM = model("player_fem", playerTexture("player_fem")).noSmooth().asVBO();
    public static final LegacyWavefrontModel ENVSUIT = model("envsuit", "envsuit_chest").asVBO();
    public static final LegacyWavefrontModel TAURUN = model("taurun", "taurun_chest").asVBO();
    public static final LegacyWavefrontModel TRENCHMASTER = model("trenchmaster", "trenchmaster_chest").asVBO();
    private static final Map<LegacyWavefrontModel, Map<String, LegacyWavefrontModel.SelectionHandle>> SELECTIONS =
            Map.ofEntries(
                    entry(BJ, "Jetpack", "Head", "RightArm", "LeftArm", "Body", "RightLeg", "LeftLeg",
                            "RightFoot", "LeftFoot"),
                    entry(HEV, "Head", "RightArm", "LeftArm", "Body", "RightLeg", "LeftLeg",
                            "RightFoot", "LeftFoot"),
                    entry(AJR, "RocketBox", "Head", "RightArm", "LeftArm", "Body", "RightLeg", "LeftLeg",
                            "RightBoot", "LeftBoot"),
                    entry(T51, "Helmet", "Chest", "LeftArm", "RightArm", "LeftLeg", "RightLeg",
                            "LeftBoot", "RightBoot"),
                    entry(HAT, "Cube_Cube.001"),
                    entry(NO9, "Helmet", "Insignia", "Flame"),
                    entry(GOGGLES, "Cube"),
                    entry(FAU, "Head", "Body", "LeftArm", "RightArm", "LeftLeg", "RightLeg",
                            "LeftBoot", "RightBoot", "Cassette"),
                    entry(DNT, "Head", "Body", "LeftArm", "RightArm", "LeftLeg", "RightLeg",
                            "LeftBoot", "RightBoot"),
                    entry(STEAMSUIT, "Head", "Body", "LeftArm", "RightArm", "LeftLeg", "RightLeg",
                            "LeftBoot", "RightBoot"),
                    entry(DIESELSUIT, "Head", "Body", "LeftArm", "RightArm", "LeftLeg", "RightLeg",
                            "LeftBoot", "RightBoot"),
                    entry(REMNANT, "Head", "Body", "LeftArm", "RightArm", "LeftLeg", "RightLeg",
                            "LeftBoot", "RightBoot", "Glow", "Fan"),
                    entry(NCR, "Helmet", "Chest", "LeftArm", "RightArm", "LeftLeg", "RightLeg",
                            "LeftBoot", "RightBoot", "Eyes"),
                    entry(BISMUTH, "Head", "Body", "LeftArm", "RightArm", "LeftLeg", "RightLeg",
                            "LeftFoot", "RightFoot"),
                    entry(WINGS, "LeftBase", "LeftTip", "RightBase", "RightTip"),
                    entry(ENVSUIT, "Helmet", "Chest", "LeftArm", "RightArm", "LeftLeg", "RightLeg",
                            "LeftFoot", "RightFoot", "Lamps", "Tail"),
                    entry(TAURUN, "Helmet", "Chest", "LeftArm", "RightArm", "LeftLeg", "RightLeg",
                            "LeftBoot", "RightBoot"),
                    entry(TRENCHMASTER, "Helmet", "Chest", "LeftArm", "RightArm", "LeftLeg", "RightLeg",
                            "LeftBoot", "RightBoot", "Light"));

    public static final ResourceLocation BJ_EYEPATCH_TEXTURE = texture("bj_eyepatch");
    public static final ResourceLocation BJ_LEG_TEXTURE = texture("bj_leg");
    public static final ResourceLocation BJ_CHEST_TEXTURE = texture("bj_chest");
    public static final ResourceLocation BJ_JETPACK_TEXTURE = texture("bj_jetpack");
    public static final ResourceLocation BJ_ARM_TEXTURE = texture("bj_arm");

    public static final ResourceLocation ENVSUIT_HELMET_TEXTURE = texture("envsuit_helmet");
    public static final ResourceLocation ENVSUIT_LEG_TEXTURE = texture("envsuit_leg");
    public static final ResourceLocation ENVSUIT_CHEST_TEXTURE = texture("envsuit_chest");
    public static final ResourceLocation ENVSUIT_ARM_TEXTURE = texture("envsuit_arm");
    public static final ResourceLocation ENVSUIT_TAIL_TEXTURE = texture("envsuit_tail");

    public static final ResourceLocation HEV_HELMET_TEXTURE = texture("hev_helmet");
    public static final ResourceLocation HEV_LEG_TEXTURE = texture("hev_leg");
    public static final ResourceLocation HEV_CHEST_TEXTURE = texture("hev_chest");
    public static final ResourceLocation HEV_ARM_TEXTURE = texture("hev_arm");

    public static final ResourceLocation AJR_HELMET_TEXTURE = texture("ajr_helmet");
    public static final ResourceLocation AJR_LEG_TEXTURE = texture("ajr_leg");
    public static final ResourceLocation AJR_CHEST_TEXTURE = texture("ajr_chest");
    public static final ResourceLocation AJR_ARM_TEXTURE = texture("ajr_arm");

    public static final ResourceLocation AJRO_HELMET_TEXTURE = texture("ajro_helmet");
    public static final ResourceLocation AJRO_LEG_TEXTURE = texture("ajro_leg");
    public static final ResourceLocation AJRO_CHEST_TEXTURE = texture("ajro_chest");
    public static final ResourceLocation AJRO_ARM_TEXTURE = texture("ajro_arm");

    public static final ResourceLocation T51_HELMET_TEXTURE = texture("t51_helmet");
    public static final ResourceLocation T51_LEG_TEXTURE = texture("t51_leg");
    public static final ResourceLocation T51_CHEST_TEXTURE = texture("t51_chest");
    public static final ResourceLocation T51_ARM_TEXTURE = texture("t51_arm");

    public static final ResourceLocation FAU_HELMET_TEXTURE = texture("fau_helmet");
    public static final ResourceLocation FAU_LEG_TEXTURE = texture("fau_leg");
    public static final ResourceLocation FAU_CHEST_TEXTURE = texture("fau_chest");
    public static final ResourceLocation FAU_CASSETTE_TEXTURE = texture("fau_cassette");
    public static final ResourceLocation FAU_ARM_TEXTURE = texture("fau_arm");

    public static final ResourceLocation DNT_HELMET_TEXTURE = texture("dnt_helmet");
    public static final ResourceLocation DNT_LEG_TEXTURE = texture("dnt_leg");
    public static final ResourceLocation DNT_CHEST_TEXTURE = texture("dnt_chest");
    public static final ResourceLocation DNT_ARM_TEXTURE = texture("dnt_arm");

    public static final ResourceLocation STEAMSUIT_HELMET_TEXTURE = texture("steamsuit_helmet");
    public static final ResourceLocation STEAMSUIT_LEG_TEXTURE = texture("steamsuit_leg");
    public static final ResourceLocation STEAMSUIT_CHEST_TEXTURE = texture("steamsuit_chest");
    public static final ResourceLocation STEAMSUIT_ARM_TEXTURE = texture("steamsuit_arm");

    public static final ResourceLocation DIESELSUIT_HELMET_TEXTURE = texture("bnuuy_helmet");
    public static final ResourceLocation DIESELSUIT_LEG_TEXTURE = texture("bnuuy_leg");
    public static final ResourceLocation DIESELSUIT_CHEST_TEXTURE = texture("bnuuy_chest");
    public static final ResourceLocation DIESELSUIT_ARM_TEXTURE = texture("bnuuy_arm");

    public static final ResourceLocation RPA_HELMET_TEXTURE = texture("rpa_helmet");
    public static final ResourceLocation RPA_LEG_TEXTURE = texture("rpa_leg");
    public static final ResourceLocation RPA_CHEST_TEXTURE = texture("rpa_chest");
    public static final ResourceLocation RPA_ARM_TEXTURE = texture("rpa_arm");

    public static final ResourceLocation NCRPA_HELMET_TEXTURE = texture("ncrpa_helmet");
    public static final ResourceLocation NCRPA_LEG_TEXTURE = texture("ncrpa_leg");
    public static final ResourceLocation NCRPA_CHEST_TEXTURE = texture("ncrpa_chest");
    public static final ResourceLocation NCRPA_ARM_TEXTURE = texture("ncrpa_arm");

    public static final ResourceLocation TAURUN_HELMET_TEXTURE = texture("taurun_helmet");
    public static final ResourceLocation TAURUN_LEG_TEXTURE = texture("taurun_leg");
    public static final ResourceLocation TAURUN_CHEST_TEXTURE = texture("taurun_chest");
    public static final ResourceLocation TAURUN_ARM_TEXTURE = texture("taurun_arm");

    public static final ResourceLocation TRENCHMASTER_HELMET_TEXTURE = texture("trenchmaster_helmet");
    public static final ResourceLocation TRENCHMASTER_LEG_TEXTURE = texture("trenchmaster_leg");
    public static final ResourceLocation TRENCHMASTER_CHEST_TEXTURE = texture("trenchmaster_chest");
    public static final ResourceLocation TRENCHMASTER_ARM_TEXTURE = texture("trenchmaster_arm");

    public static final ResourceLocation MOD_TESLA_TEXTURE = texture("mod_tesla");
    public static final ResourceLocation BISMUTH_TEXTURE = texture("bismuth");
    public static final ResourceLocation WINGS_MURK_TEXTURE = texture("wings_murk");
    public static final ResourceLocation WINGS_BOB_TEXTURE = texture("wings_bob");
    public static final ResourceLocation WINGS_BLACK_TEXTURE = texture("wings_black");
    public static final ResourceLocation WINGS_PHEO_TEXTURE = texture("axepack");
    public static final ResourceLocation TAIL_PEEP_TEXTURE = texture("tail_peep");
    public static final ResourceLocation HAT_TEXTURE = texture("hat");
    public static final ResourceLocation NO9_TEXTURE = texture("no9");
    public static final ResourceLocation NO9_INSIGNIA_TEXTURE = texture("no9_insignia");
    public static final ResourceLocation GOGGLES_TEXTURE = texture("goggles");
    public static final ResourceLocation PLAYER_FEM_TEXTURE = playerTexture("player_fem");

    public static LegacyWavefrontModel model(String name) {
        return model(name, name);
    }

    public static LegacyWavefrontModel model(String modelName, ResourceLocation texture) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/armor/" + modelName + ".obj"),
                texture);
    }

    public static LegacyWavefrontModel model(String modelName, String textureName) {
        return model(modelName, texture(textureName));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/armor/" + name + ".png");
    }

    public static ResourceLocation playerTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/" + name + ".png");
    }

    public static void renderPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyWavefrontModel.SelectionHandle handle = handle(model, partName);
        if (handle != null) {
            model.renderOnlyInCallOrder(texture,
                    new ObjRenderContext(poseStack, buffer, null, packedLight, packedOverlay), handle);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            int red, int green, int blue, int alpha) {
        renderPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    public static void renderPartTranslucent(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            int red, int green, int blue, int alpha) {
        renderPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE);
    }

    public static void renderPartAdditive(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            int red, int green, int blue, int alpha) {
        renderPart(model, partName, texture, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE);
    }

    public static void renderPartUntextured(LegacyWavefrontModel model, String partName, ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle = handle(model, partName);
        if (handle != null) {
            model.renderOnlyUntextured(context, handle);
            return;
        }
        model.renderPartUntextured(partName, context);
    }

    public static void renderPartUntexturedAdditive(LegacyWavefrontModel model, String partName,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle = handle(model, partName);
        ObjRenderContext additive = context.withRenderMode(LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE);
        if (handle != null) {
            model.renderOnlyUntextured(additive, handle);
            return;
        }
        model.renderPartUntexturedAdditive(partName, context);
    }

    private static void renderPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle = handle(model, partName);
        if (handle != null) {
            ObjRenderContext context = new ObjRenderContext(poseStack, buffer, null, packedLight, packedOverlay)
                    .withRgba(red, green, blue, alpha)
                    .withRenderMode(renderMode);
            model.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        if (renderMode == LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE) {
            model.renderPartTranslucent(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha);
        } else if (renderMode == LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE) {
            model.renderPartAdditive(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha);
        } else {
            model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha);
        }
    }

    private static LegacyWavefrontModel.SelectionHandle handle(LegacyWavefrontModel model, String partName) {
        Map<String, LegacyWavefrontModel.SelectionHandle> selections = SELECTIONS.get(model);
        return selections == null || partName == null ? null : selections.get(partName);
    }

    private static Map.Entry<LegacyWavefrontModel, Map<String, LegacyWavefrontModel.SelectionHandle>> entry(
            LegacyWavefrontModel model, String... partNames) {
        java.util.LinkedHashMap<String, LegacyWavefrontModel.SelectionHandle> handles = new java.util.LinkedHashMap<>();
        for (String partName : partNames) {
            handles.put(partName, model.prepareRenderOnlyInCallOrder(partName));
        }
        return Map.entry(model, Map.copyOf(handles));
    }

    private ObjArmorModels() {
    }
}
