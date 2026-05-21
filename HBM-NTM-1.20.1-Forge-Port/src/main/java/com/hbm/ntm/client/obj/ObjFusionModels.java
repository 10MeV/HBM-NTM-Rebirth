package com.hbm.ntm.client.obj;

import net.minecraft.client.renderer.RenderType;

public final class ObjFusionModels {
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
            .legacyOrder("Plasma", "Bolts4", "Bolts3", "Bolts2", "Bolts1", "Magnet", "Torus");

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
            .legacyOrder("Plasma", "SliderStriker", "ArmLowerStriker", "ArmUpperStriker", "StrikerMount",
                    "StrikerLeft", "StrikerRight", "PistonLeft", "PistonRight", "SliderJet", "ArmLowerJet",
                    "ArmUpperJet", "Jet", "Body");

    public static ObjModelPart part(String name) {
        return ObjModelLibrary.blockPart("fusion/" + name, RenderType.cutout());
    }

    public static ObjModelPart translucentPart(String name) {
        return ObjModelLibrary.blockPart("fusion/" + name, RenderType.translucent());
    }

    private ObjFusionModels() {
    }
}
