package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class ObjFusionModels {
    public static final LegacyWavefrontModel TORUS_LEGACY = legacyModel("torus").asVBO();
    public static final LegacyWavefrontModel KLYSTRON_LEGACY = legacyModel("klystron").asVBO();
    public static final LegacyWavefrontModel BREEDER_LEGACY = legacyModel("breeder").asVBO();
    public static final LegacyWavefrontModel COLLECTOR_LEGACY = legacyModel("collector").asVBO();
    public static final LegacyWavefrontModel BOILER_LEGACY = legacyModel("boiler").asVBO();
    public static final LegacyWavefrontModel MHDT_LEGACY = legacyModel("mhdt").asVBO();
    public static final LegacyWavefrontModel COUPLER_LEGACY = legacyModel("coupler").asVBO();
    public static final LegacyWavefrontModel PLASMA_FORGE_LEGACY = legacyModel("plasma_forge").asVBO();

    public static final ResourceLocation TORUS_TEXTURE = texture("torus");
    public static final ResourceLocation PLASMA_TEXTURE = texture("plasma");
    public static final ResourceLocation PLASMA_GLOW_TEXTURE = texture("plasma_glow");
    public static final ResourceLocation PLASMA_SPARKLE_TEXTURE = texture("plasma_sparkle");
    public static final ResourceLocation KLYSTRON_TEXTURE = texture("klystron");
    public static final ResourceLocation KLYSTRON_CREATIVE_TEXTURE = texture("klystron_creative");
    public static final ResourceLocation BREEDER_TEXTURE = texture("breeder");
    public static final ResourceLocation COLLECTOR_TEXTURE = texture("collector");
    public static final ResourceLocation BOILER_TEXTURE = texture("boiler");
    public static final ResourceLocation MHDT_TEXTURE = texture("mhdt");
    public static final ResourceLocation COUPLER_TEXTURE = texture("coupler");
    public static final ResourceLocation PLASMA_FORGE_TEXTURE = texture("plasma_forge");
    private static final LegacyWavefrontModel.SelectionHandle TORUS_BODY_HANDLE =
            TORUS_LEGACY.prepareRenderOnlyInCallOrder("Torus");
    private static final LegacyWavefrontModel.SelectionHandle TORUS_MAGNET_HANDLE =
            TORUS_LEGACY.prepareRenderOnlyInCallOrder("Magnet");
    private static final LegacyWavefrontModel.SelectionHandle TORUS_BOLTS_1_HANDLE =
            TORUS_LEGACY.prepareRenderOnlyInCallOrder("Bolts1");
    private static final LegacyWavefrontModel.SelectionHandle TORUS_BOLTS_2_HANDLE =
            TORUS_LEGACY.prepareRenderOnlyInCallOrder("Bolts2");
    private static final LegacyWavefrontModel.SelectionHandle TORUS_BOLTS_3_HANDLE =
            TORUS_LEGACY.prepareRenderOnlyInCallOrder("Bolts3");
    private static final LegacyWavefrontModel.SelectionHandle TORUS_BOLTS_4_HANDLE =
            TORUS_LEGACY.prepareRenderOnlyInCallOrder("Bolts4");
    private static final LegacyWavefrontModel.SelectionHandle TORUS_PLASMA_HANDLE =
            TORUS_LEGACY.prepareRenderOnlyInCallOrder("Plasma");
    private static final LegacyWavefrontModel.SelectionHandle KLYSTRON_BODY_HANDLE =
            KLYSTRON_LEGACY.prepareRenderOnlyInCallOrder("Klystron");
    private static final LegacyWavefrontModel.SelectionHandle KLYSTRON_ROTOR_HANDLE =
            KLYSTRON_LEGACY.prepareRenderOnlyInCallOrder("Rotor");
    private static final LegacyWavefrontModel.SelectionHandle BREEDER_BODY_HANDLE =
            BREEDER_LEGACY.prepareRenderOnlyInCallOrder("Breeder");
    private static final LegacyWavefrontModel.SelectionHandle MHDT_TURBINE_HANDLE =
            MHDT_LEGACY.prepareRenderOnlyInCallOrder("Turbine");
    private static final LegacyWavefrontModel.SelectionHandle MHDT_COILS_HANDLE =
            MHDT_LEGACY.prepareRenderOnlyInCallOrder("Coils");
    private static final LegacyWavefrontModel.SelectionHandle PLASMA_FORGE_BODY_HANDLE =
            PLASMA_FORGE_LEGACY.prepareRenderOnlyInCallOrder("Body");
    private static final LegacyWavefrontModel.SelectionHandle PLASMA_FORGE_PLASMA_HANDLE =
            PLASMA_FORGE_LEGACY.prepareRenderOnlyInCallOrder("Plasma");
    private static final LegacyWavefrontModel.SelectionHandle PLASMA_FORGE_SLIDER_STRIKER_HANDLE =
            PLASMA_FORGE_LEGACY.prepareRenderOnlyInCallOrder("SliderStriker");
    private static final LegacyWavefrontModel.SelectionHandle PLASMA_FORGE_ARM_LOWER_STRIKER_HANDLE =
            PLASMA_FORGE_LEGACY.prepareRenderOnlyInCallOrder("ArmLowerStriker");
    private static final LegacyWavefrontModel.SelectionHandle PLASMA_FORGE_ARM_UPPER_STRIKER_HANDLE =
            PLASMA_FORGE_LEGACY.prepareRenderOnlyInCallOrder("ArmUpperStriker");
    private static final LegacyWavefrontModel.SelectionHandle PLASMA_FORGE_STRIKER_MOUNT_HANDLE =
            PLASMA_FORGE_LEGACY.prepareRenderOnlyInCallOrder("StrikerMount");
    private static final LegacyWavefrontModel.SelectionHandle PLASMA_FORGE_STRIKER_LEFT_HANDLE =
            PLASMA_FORGE_LEGACY.prepareRenderOnlyInCallOrder("StrikerLeft");
    private static final LegacyWavefrontModel.SelectionHandle PLASMA_FORGE_STRIKER_RIGHT_HANDLE =
            PLASMA_FORGE_LEGACY.prepareRenderOnlyInCallOrder("StrikerRight");
    private static final LegacyWavefrontModel.SelectionHandle PLASMA_FORGE_PISTON_LEFT_HANDLE =
            PLASMA_FORGE_LEGACY.prepareRenderOnlyInCallOrder("PistonLeft");
    private static final LegacyWavefrontModel.SelectionHandle PLASMA_FORGE_PISTON_RIGHT_HANDLE =
            PLASMA_FORGE_LEGACY.prepareRenderOnlyInCallOrder("PistonRight");
    private static final LegacyWavefrontModel.SelectionHandle PLASMA_FORGE_SLIDER_JET_HANDLE =
            PLASMA_FORGE_LEGACY.prepareRenderOnlyInCallOrder("SliderJet");
    private static final LegacyWavefrontModel.SelectionHandle PLASMA_FORGE_ARM_LOWER_JET_HANDLE =
            PLASMA_FORGE_LEGACY.prepareRenderOnlyInCallOrder("ArmLowerJet");
    private static final LegacyWavefrontModel.SelectionHandle PLASMA_FORGE_ARM_UPPER_JET_HANDLE =
            PLASMA_FORGE_LEGACY.prepareRenderOnlyInCallOrder("ArmUpperJet");
    private static final LegacyWavefrontModel.SelectionHandle PLASMA_FORGE_JET_HANDLE =
            PLASMA_FORGE_LEGACY.prepareRenderOnlyInCallOrder("Jet");
    private static final LegacyWavefrontModel.SelectionHandle PLASMA_FORGE_ITEM_BODY_HANDLE =
            PLASMA_FORGE_LEGACY.prepareRenderAllExcept("Plasma");

    public static final ObjModelPart TORUS = part("fusion_torus");
    public static final ObjModelPart TORUS_BODY = part("fusion_torus_torus");
    public static final ObjModelPart TORUS_MAGNET = part("fusion_torus_magnet");
    public static final ObjModelPart TORUS_BOLTS_1 = part("fusion_torus_bolts_1");
    public static final ObjModelPart TORUS_BOLTS_2 = part("fusion_torus_bolts_2");
    public static final ObjModelPart TORUS_BOLTS_3 = part("fusion_torus_bolts_3");
    public static final ObjModelPart TORUS_BOLTS_4 = part("fusion_torus_bolts_4");
    public static final ObjModelPart TORUS_PLASMA = translucentPart("fusion_torus_plasma");
    public static final ObjModelPart TORUS_PLASMA_GLOW = translucentPart("fusion_torus_plasma_glow");
    public static final ObjModelPart TORUS_PLASMA_SPARKLE = translucentPart("fusion_torus_plasma_sparkle");
    public static final ObjPartModel TORUS_PARTS = new ObjPartModel()
            .part("Plasma", TORUS_PLASMA)
            .part("Bolts4", TORUS_BOLTS_4)
            .part("Bolts3", TORUS_BOLTS_3)
            .part("Bolts2", TORUS_BOLTS_2)
            .part("Bolts1", TORUS_BOLTS_1)
            .part("Magnet", TORUS_MAGNET)
            .part("Torus", TORUS_BODY)
            .legacyOrder("Torus", "Magnet", "Bolts1", "Bolts2", "Bolts3", "Bolts4", "Plasma");

    public static final ObjModelPart KLYSTRON = part("fusion_klystron");
    public static final ObjModelPart KLYSTRON_PIPES = part("fusion_klystron_pipes");
    public static final ObjModelPart KLYSTRON_ROTOR = part("fusion_klystron_rotor");
    public static final ObjModelPart KLYSTRON_BODY = part("fusion_klystron_body");
    public static final ObjPartModel KLYSTRON_PARTS = new ObjPartModel()
            .part("Pipes", KLYSTRON_PIPES)
            .part("Rotor", KLYSTRON_ROTOR)
            .part("Klystron", KLYSTRON_BODY)
            .legacyOrder("Pipes", "Rotor", "Klystron");
    public static final ObjModelPart KLYSTRON_CREATIVE = part("fusion_klystron_creative");
    public static final ObjModelPart KLYSTRON_CREATIVE_PIPES = part("fusion_klystron_creative_pipes");
    public static final ObjModelPart KLYSTRON_CREATIVE_ROTOR = part("fusion_klystron_creative_rotor");
    public static final ObjModelPart KLYSTRON_CREATIVE_BODY = part("fusion_klystron_creative_body");
    public static final ObjPartModel KLYSTRON_CREATIVE_PARTS = new ObjPartModel()
            .part("Pipes", KLYSTRON_CREATIVE_PIPES)
            .part("Rotor", KLYSTRON_CREATIVE_ROTOR)
            .part("Klystron", KLYSTRON_CREATIVE_BODY)
            .legacyOrder("Pipes", "Rotor", "Klystron");

    public static final ObjModelPart BREEDER = part("fusion_breeder");
    public static final ObjModelPart BREEDER_ALT = part("fusion_breeder_alt");
    public static final ObjModelPart BREEDER_BODY = part("fusion_breeder_body");
    public static final ObjPartModel BREEDER_PARTS = new ObjPartModel()
            .part("BreederAlt", BREEDER_ALT)
            .part("Breeder", BREEDER_BODY)
            .legacyOrder("BreederAlt", "Breeder");

    public static final ObjModelPart COLLECTOR = part("fusion_collector");
    public static final ObjModelPart BOILER = part("fusion_boiler");
    public static final ObjModelPart MHDT = part("fusion_mhdt");
    public static final ObjModelPart MHDT_COILS = part("fusion_mhdt_coils");
    public static final ObjModelPart MHDT_TURBINE = part("fusion_mhdt_turbine");
    public static final ObjPartModel MHDT_PARTS = new ObjPartModel()
            .part("Coils", MHDT_COILS)
            .part("Turbine", MHDT_TURBINE)
            .legacyOrder("Coils", "Turbine");

    public static final ObjModelPart COUPLER = part("fusion_coupler");
    public static final ObjModelPart PLASMA_FORGE = part("fusion_plasma_forge");
    public static final ObjModelPart PLASMA_FORGE_BODY = part("fusion_plasma_forge_body");
    public static final ObjModelPart PLASMA_FORGE_PLASMA = translucentPart("fusion_plasma_forge_plasma");
    public static final ObjModelPart PLASMA_FORGE_PLASMA_GLOW = translucentPart("fusion_plasma_forge_plasma_glow");
    public static final ObjModelPart PLASMA_FORGE_SLIDER_STRIKER = part("fusion_plasma_forge_slider_striker");
    public static final ObjModelPart PLASMA_FORGE_ARM_LOWER_STRIKER = part("fusion_plasma_forge_arm_lower_striker");
    public static final ObjModelPart PLASMA_FORGE_ARM_UPPER_STRIKER = part("fusion_plasma_forge_arm_upper_striker");
    public static final ObjModelPart PLASMA_FORGE_STRIKER_MOUNT = part("fusion_plasma_forge_striker_mount");
    public static final ObjModelPart PLASMA_FORGE_STRIKER_LEFT = part("fusion_plasma_forge_striker_left");
    public static final ObjModelPart PLASMA_FORGE_STRIKER_RIGHT = part("fusion_plasma_forge_striker_right");
    public static final ObjModelPart PLASMA_FORGE_PISTON_LEFT = part("fusion_plasma_forge_piston_left");
    public static final ObjModelPart PLASMA_FORGE_PISTON_RIGHT = part("fusion_plasma_forge_piston_right");
    public static final ObjModelPart PLASMA_FORGE_SLIDER_JET = part("fusion_plasma_forge_slider_jet");
    public static final ObjModelPart PLASMA_FORGE_ARM_LOWER_JET = part("fusion_plasma_forge_arm_lower_jet");
    public static final ObjModelPart PLASMA_FORGE_ARM_UPPER_JET = part("fusion_plasma_forge_arm_upper_jet");
    public static final ObjModelPart PLASMA_FORGE_JET = part("fusion_plasma_forge_jet");
    public static final ObjPartModel PLASMA_FORGE_PARTS = new ObjPartModel()
            .part("Plasma", PLASMA_FORGE_PLASMA)
            .part("SliderStriker", PLASMA_FORGE_SLIDER_STRIKER)
            .part("ArmLowerStriker", PLASMA_FORGE_ARM_LOWER_STRIKER)
            .part("ArmUpperStriker", PLASMA_FORGE_ARM_UPPER_STRIKER)
            .part("StrikerMount", PLASMA_FORGE_STRIKER_MOUNT)
            .part("StrikerLeft", PLASMA_FORGE_STRIKER_LEFT)
            .part("StrikerRight", PLASMA_FORGE_STRIKER_RIGHT)
            .part("PistonLeft", PLASMA_FORGE_PISTON_LEFT)
            .part("PistonRight", PLASMA_FORGE_PISTON_RIGHT)
            .part("SliderJet", PLASMA_FORGE_SLIDER_JET)
            .part("ArmLowerJet", PLASMA_FORGE_ARM_LOWER_JET)
            .part("ArmUpperJet", PLASMA_FORGE_ARM_UPPER_JET)
            .part("Jet", PLASMA_FORGE_JET)
            .part("Body", PLASMA_FORGE_BODY)
            .legacyOrder("Body", "SliderStriker", "ArmLowerStriker", "ArmUpperStriker", "StrikerMount",
                    "StrikerLeft", "StrikerRight", "PistonLeft", "PistonRight", "SliderJet", "ArmLowerJet",
                    "ArmUpperJet", "Jet", "Plasma");

    public static ObjModelPart part(String name) {
        return ObjModelLibrary.blockPart("fusion/" + name, RenderType.cutout());
    }

    public static ObjModelPart translucentPart(String name) {
        return ObjModelLibrary.blockTranslucentPart("fusion/" + name);
    }

    public static LegacyWavefrontModel legacyModel(String name) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/fusion/" + name + ".obj"),
                texture(name)).asVBO();
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/fusion/" + name + ".png");
    }

    public static void renderTorusPart(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode, String partName) {
        renderTorusPart(TORUS_LEGACY, texture, poseStack, buffer, packedLight, packedOverlay, renderMode, partName);
    }

    public static void renderTorusPart(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, LegacyWavefrontModel.UvTransform uvTransform, String partName) {
        renderTorusPart(TORUS_LEGACY, texture, poseStack, buffer, packedLight, packedOverlay, red, green, blue,
                alpha, legacyShadow, renderMode, uvTransform, partName);
    }

    public static void renderTorusPart(LegacyWavefrontModel model, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            String partName) {
        LegacyWavefrontModel.SelectionHandle handle = sameModel(model, TORUS_LEGACY) ? torusHandle(partName) : null;
        if (handle != null) {
            TORUS_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle,
                    renderMode);
            return;
        }
        renderOnly(model, texture, poseStack, buffer, packedLight, packedOverlay, renderMode, partName);
    }

    public static void renderTorusPart(LegacyWavefrontModel model, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue, int alpha,
            boolean legacyShadow, LegacyTexturedRenderMode renderMode, LegacyWavefrontModel.UvTransform uvTransform,
            String partName) {
        LegacyWavefrontModel.SelectionHandle handle = sameModel(model, TORUS_LEGACY) ? torusHandle(partName) : null;
        if (handle != null) {
            TORUS_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha, legacyShadow, renderMode, uvTransform, handle);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, legacyShadow, renderMode, uvTransform);
    }

    public static void renderKlystronPart(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode, String partName) {
        renderKlystronPart(KLYSTRON_LEGACY, texture, poseStack, buffer, packedLight, packedOverlay, renderMode,
                partName);
    }

    public static void renderKlystronPart(LegacyWavefrontModel model, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            String partName) {
        LegacyWavefrontModel.SelectionHandle handle = sameModel(model, KLYSTRON_LEGACY) ? klystronHandle(partName) : null;
        if (handle != null) {
            KLYSTRON_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle,
                    renderMode);
            return;
        }
        renderOnly(model, texture, poseStack, buffer, packedLight, packedOverlay, renderMode, partName);
    }

    public static void renderBreederPart(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode, String partName) {
        renderBreederPart(BREEDER_LEGACY, texture, poseStack, buffer, packedLight, packedOverlay, renderMode,
                partName);
    }

    public static void renderBreederPart(LegacyWavefrontModel model, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            String partName) {
        LegacyWavefrontModel.SelectionHandle handle = sameModel(model, BREEDER_LEGACY) ? breederHandle(partName) : null;
        if (handle != null) {
            BREEDER_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle,
                    renderMode);
            return;
        }
        renderOnly(model, texture, poseStack, buffer, packedLight, packedOverlay, renderMode, partName);
    }

    public static void renderMhdtPart(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode, String partName) {
        renderMhdtPart(MHDT_LEGACY, texture, poseStack, buffer, packedLight, packedOverlay, renderMode, partName);
    }

    public static void renderMhdtPart(LegacyWavefrontModel model, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            String partName) {
        LegacyWavefrontModel.SelectionHandle handle = sameModel(model, MHDT_LEGACY) ? mhdtHandle(partName) : null;
        if (handle != null) {
            MHDT_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle,
                    renderMode);
            return;
        }
        renderOnly(model, texture, poseStack, buffer, packedLight, packedOverlay, renderMode, partName);
    }

    public static void renderPlasmaForgePart(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode, String partName) {
        renderPlasmaForgePart(PLASMA_FORGE_LEGACY, texture, poseStack, buffer, packedLight, packedOverlay,
                renderMode, partName);
    }

    public static void renderPlasmaForgePart(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int red, int green, int blue, int alpha, boolean legacyShadow,
            LegacyTexturedRenderMode renderMode, LegacyWavefrontModel.UvTransform uvTransform, String partName) {
        renderPlasmaForgePart(PLASMA_FORGE_LEGACY, texture, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, legacyShadow, renderMode, uvTransform, partName);
    }

    private static void renderOnly(LegacyWavefrontModel model, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            String partName) {
        model.renderOnly(texture, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255, false,
                renderMode, LegacyWavefrontModel.UvTransform.DEFAULT, partName);
    }

    public static void renderPlasmaForgeItemBody(ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        PLASMA_FORGE_LEGACY.renderAllExcept(texture, poseStack, buffer, packedLight, packedOverlay,
                PLASMA_FORGE_ITEM_BODY_HANDLE, renderMode);
    }

    public static void renderPlasmaForgePart(LegacyWavefrontModel model, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode, String partName) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, PLASMA_FORGE_LEGACY) ? plasmaForgeHandle(partName) : null;
        if (handle != null) {
            PLASMA_FORGE_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle,
                    renderMode);
            return;
        }
        renderOnly(model, texture, poseStack, buffer, packedLight, packedOverlay, renderMode, partName);
    }

    public static void renderPlasmaForgePart(LegacyWavefrontModel model, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            int red, int green, int blue, int alpha, boolean legacyShadow, LegacyTexturedRenderMode renderMode,
            LegacyWavefrontModel.UvTransform uvTransform, String partName) {
        LegacyWavefrontModel.SelectionHandle handle =
                sameModel(model, PLASMA_FORGE_LEGACY) ? plasmaForgeHandle(partName) : null;
        if (handle != null) {
            PLASMA_FORGE_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha, legacyShadow, renderMode, uvTransform, handle);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, legacyShadow, renderMode, uvTransform);
    }

    public static void renderPlasmaForgePartUntextured(PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode, String partName) {
        LegacyWavefrontModel.SelectionHandle handle = plasmaForgeHandle(partName);
        if (handle != null) {
            PLASMA_FORGE_LEGACY.renderOnlyUntextured(poseStack, buffer, red, green, blue, alpha, renderMode, handle);
            return;
        }
        PLASMA_FORGE_LEGACY.renderPartUntextured(partName, poseStack, buffer, red, green, blue, alpha, renderMode);
    }

    private static LegacyWavefrontModel.SelectionHandle torusHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Torus" -> TORUS_BODY_HANDLE;
            case "Magnet" -> TORUS_MAGNET_HANDLE;
            case "Bolts1" -> TORUS_BOLTS_1_HANDLE;
            case "Bolts2" -> TORUS_BOLTS_2_HANDLE;
            case "Bolts3" -> TORUS_BOLTS_3_HANDLE;
            case "Bolts4" -> TORUS_BOLTS_4_HANDLE;
            case "Plasma" -> TORUS_PLASMA_HANDLE;
            default -> null;
        };
    }

    private static LegacyWavefrontModel.SelectionHandle klystronHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Klystron" -> KLYSTRON_BODY_HANDLE;
            case "Rotor" -> KLYSTRON_ROTOR_HANDLE;
            default -> null;
        };
    }

    private static LegacyWavefrontModel.SelectionHandle breederHandle(String partName) {
        return "Breeder".equals(partName) ? BREEDER_BODY_HANDLE : null;
    }

    private static LegacyWavefrontModel.SelectionHandle mhdtHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Turbine" -> MHDT_TURBINE_HANDLE;
            case "Coils" -> MHDT_COILS_HANDLE;
            default -> null;
        };
    }

    private static LegacyWavefrontModel.SelectionHandle plasmaForgeHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Body" -> PLASMA_FORGE_BODY_HANDLE;
            case "Plasma" -> PLASMA_FORGE_PLASMA_HANDLE;
            case "SliderStriker" -> PLASMA_FORGE_SLIDER_STRIKER_HANDLE;
            case "ArmLowerStriker" -> PLASMA_FORGE_ARM_LOWER_STRIKER_HANDLE;
            case "ArmUpperStriker" -> PLASMA_FORGE_ARM_UPPER_STRIKER_HANDLE;
            case "StrikerMount" -> PLASMA_FORGE_STRIKER_MOUNT_HANDLE;
            case "StrikerLeft" -> PLASMA_FORGE_STRIKER_LEFT_HANDLE;
            case "StrikerRight" -> PLASMA_FORGE_STRIKER_RIGHT_HANDLE;
            case "PistonLeft" -> PLASMA_FORGE_PISTON_LEFT_HANDLE;
            case "PistonRight" -> PLASMA_FORGE_PISTON_RIGHT_HANDLE;
            case "SliderJet" -> PLASMA_FORGE_SLIDER_JET_HANDLE;
            case "ArmLowerJet" -> PLASMA_FORGE_ARM_LOWER_JET_HANDLE;
            case "ArmUpperJet" -> PLASMA_FORGE_ARM_UPPER_JET_HANDLE;
            case "Jet" -> PLASMA_FORGE_JET_HANDLE;
            default -> null;
        };
    }

    private static boolean sameModel(LegacyWavefrontModel model, LegacyWavefrontModel expected) {
        return model == expected || model.modelLocation().equals(expected.modelLocation());
    }

    private ObjFusionModels() {
    }
}
