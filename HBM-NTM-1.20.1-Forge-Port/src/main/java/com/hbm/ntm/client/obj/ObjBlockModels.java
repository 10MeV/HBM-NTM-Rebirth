package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjBlockModels {
    public static final LegacyWavefrontModel SCAFFOLD = model("scaffold", "scaffold_steel").noSmooth();
    public static final LegacyWavefrontModel BEAM = model("beam", "steel_beam").noSmooth();
    public static final LegacyWavefrontModel BARREL = model("barrel", "barrel_plastic").noSmooth();
    public static final LegacyWavefrontModel POLE = model("pole", "steel_beam").noSmooth();
    public static final LegacyWavefrontModel PIPE = model("pipe", "pipe_side").noSmooth();
    public static final LegacyWavefrontModel TAPE_RECORDER = model("taperecorder", "steel_beam").noSmooth();
    public static final LegacyWavefrontModel BARBED_WIRE = model("barbed_wire", "barbed_wire_model").noSmooth();
    public static final LegacyWavefrontModel SPIKES = model("spikes", "spikes").noSmooth();
    public static final LegacyWavefrontModel ANTENNA_TOP = model("antenna_top", "steel_beam").noSmooth();
    public static final LegacyWavefrontModel CONSERVE_CRATE = model("conservecrate", "steel_beam").noSmooth();
    public static final LegacyWavefrontModel PIPE_RIM = model("pipe_rim", "pipe_side").noSmooth();
    public static final LegacyWavefrontModel PIPE_QUAD = model("pipe_quad", "pipe_side").noSmooth();
    public static final LegacyWavefrontModel PIPE_FRAME = model("pipe_frame", "pipe_frame").noSmooth();
    public static final LegacyWavefrontModel RTTY = model("rtty", "rtty_controller").noSmooth();
    public static final LegacyWavefrontModel CRT = model("crt", "crt_clean").noSmooth();
    public static final LegacyWavefrontModel TOASTER = model("toaster", "toaster_iron").noSmooth();
    public static final LegacyWavefrontModel DECO_COMPUTER = model("puter", "crt_clean").noSmooth();
    public static final LegacyWavefrontModel HEV_BATTERY = model("battery", "battery_top").noSmooth();
    public static final LegacyWavefrontModel SKELETON_HOLDER = model("skeleton_holder", "skeleton").noSmooth();
    public static final LegacyWavefrontModel ANVIL = model("anvil", "anvil_iron").noSmooth();
    public static final LegacyWavefrontModel CRYSTAL_POWER = model("crystals_power", "crystal_pulsar").noSmooth();
    public static final LegacyWavefrontModel CRYSTAL_ENERGY = model("crystals_energy", "crystal_pulsar").noSmooth();
    public static final LegacyWavefrontModel CRYSTAL_ROBUST = model("crystals_robust", "crystal_hardened").noSmooth();
    public static final LegacyWavefrontModel CRYSTAL_TRIXITE = model("crystals_trixite", "crystal_virus").noSmooth();
    public static final LegacyWavefrontModel CABLE_NEO = model("cable_neo", "cable_neo").noSmooth();
    public static final LegacyWavefrontModel DIFURNACE_EXTENSION = model("difurnace_extension", "difurnace_extension").noSmooth();
    public static final LegacyWavefrontModel SPLITTER = model("splitter", "crane_splitter_inner").noSmooth();
    public static final LegacyWavefrontModel CRANE_BUFFER = model("crane_buffer", "crane_box").noSmooth();
    public static final LegacyWavefrontModel RAIL_NARROW_STRAIGHT = model("rail_narrow", "rail_narrow").noSmooth();
    public static final LegacyWavefrontModel RAIL_NARROW_CURVE = model("rail_narrow_bend", "rail_narrow_turned").noSmooth();
    public static final LegacyWavefrontModel RAIL_STANDARD_STRAIGHT = model("rail_standard", "rail_standard_straight").noSmooth();
    public static final LegacyWavefrontModel RAIL_STANDARD_STRAIGHT_SHORT = model("rail_standard_short", "rail_standard_straight").noSmooth();
    public static final LegacyWavefrontModel RAIL_STANDARD_CURVE = model("rail_standard_bend", "rail_normal_turned").noSmooth();
    public static final LegacyWavefrontModel RAIL_STANDARD_CURVE_WIDE7 = model("rail_standard_bend_wide", "rail_normal_turned").noSmooth();
    public static final LegacyWavefrontModel RAIL_STANDARD_CURVE_WIDE9 = model("rail_standard_bend_wide9", "rail_normal_turned").noSmooth();
    public static final LegacyWavefrontModel RAIL_STANDARD_RAMP = model("rail_standard_ramp", "rail_standard_straight").noSmooth();
    public static final LegacyWavefrontModel RAIL_STANDARD_BUFFER = model("rail_standard_buffer", "rail_standard_buffer").noSmooth();
    public static final LegacyWavefrontModel RAIL_STANDARD_SWITCH = model("rail_standard_switch", "rail_switch_sign").noSmooth();
    public static final LegacyWavefrontModel RAIL_STANDARD_SWITCH_FLIPPED = model("rail_standard_switch_flipped", "rail_switch_sign_flipped").noSmooth();
    public static final LegacyWavefrontModel CAPACITOR = model("capacitor", "capacitor_copper_top").noSmooth();
    public static final LegacyWavefrontModel FUNNEL = model("funnel", "steel_beam").noSmooth();
    public static final LegacyWavefrontModel CHARGE_DYNAMITE = model("charge_dynamite", "charge_dynamite").noSmooth();
    public static final LegacyWavefrontModel CHARGE_C4 = model("charge_c4", "charge_c4").noSmooth();

    public static LegacyWavefrontModel model(String name, String textureName) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/legacy_blocks/" + name + ".obj"),
                texture(textureName));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/legacy_blocks/" + name + ".png");
    }

    private ObjBlockModels() {
    }
}
