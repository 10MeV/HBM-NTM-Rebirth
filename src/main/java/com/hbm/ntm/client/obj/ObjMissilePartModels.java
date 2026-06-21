package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ObjMissilePartModels {
    public static final ResourceLocation UNIVERSAL_TEXTURE = modelTexture("thegadget3_");
    public static final ResourceLocation BOXCAR_TEXTURE = modelTexture("boxcar");

    public static final LegacyWavefrontModel MISSILE_V2 = missileModel("missile_v2").asVBO();
    public static final LegacyWavefrontModel MISSILE_ABM = missileModel("missile_abm").asVBO();
    public static final LegacyWavefrontModel MISSILE_STEALTH = missileModel("missile_stealth").noSmooth().asVBO();
    public static final LegacyWavefrontModel MISSILE_STRONG = missileModel("missile_strong").asVBO();
    public static final LegacyWavefrontModel MISSILE_HUGE = missileModel("missile_huge").asVBO();
    public static final LegacyWavefrontModel MISSILE_ATLAS = missileModel("missile_atlas", "missile_atlas_nuclear").asVBO();
    public static final LegacyWavefrontModel MISSILE_MICRO = missileModel("missile_micro").asVBO();
    public static final LegacyWavefrontModel MISSILE_SHUTTLE = missileModel("missile_shuttle").asVBO();
    public static final LegacyWavefrontModel MINER_ROCKET = missileModel("miner_rocket").asVBO();

    public static final ResourceLocation MISSILE_V2_HE_TEXTURE = missileTexture("missile_v2");
    public static final ResourceLocation MISSILE_V2_IN_TEXTURE = missileTexture("missile_v2_inc");
    public static final ResourceLocation MISSILE_V2_CL_TEXTURE = missileTexture("missile_v2_cl");
    public static final ResourceLocation MISSILE_V2_BU_TEXTURE = missileTexture("missile_v2_bu");
    public static final ResourceLocation MISSILE_V2_DECOY_TEXTURE = missileTexture("missile_v2_decoy");
    public static final ResourceLocation MISSILE_ABM_TEXTURE = missileTexture("missile_abm");
    public static final ResourceLocation MISSILE_STEALTH_TEXTURE = missileTexture("missile_stealth");
    public static final ResourceLocation MISSILE_STRONG_HE_TEXTURE = missileTexture("missile_strong");
    public static final ResourceLocation MISSILE_STRONG_EMP_TEXTURE = missileTexture("missile_strong_emp");
    public static final ResourceLocation MISSILE_STRONG_IN_TEXTURE = missileTexture("missile_strong_inc");
    public static final ResourceLocation MISSILE_STRONG_CL_TEXTURE = missileTexture("missile_strong_cl");
    public static final ResourceLocation MISSILE_STRONG_BU_TEXTURE = missileTexture("missile_strong_bu");
    public static final ResourceLocation MISSILE_HUGE_HE_TEXTURE = missileTexture("missile_huge");
    public static final ResourceLocation MISSILE_HUGE_IN_TEXTURE = missileTexture("missile_huge_inc");
    public static final ResourceLocation MISSILE_HUGE_CL_TEXTURE = missileTexture("missile_huge_cl");
    public static final ResourceLocation MISSILE_HUGE_BU_TEXTURE = missileTexture("missile_huge_bu");
    public static final ResourceLocation MISSILE_ATLAS_NUCLEAR_TEXTURE = missileTexture("missile_atlas_nuclear");
    public static final ResourceLocation MISSILE_ATLAS_THERMO_TEXTURE = missileTexture("missile_atlas_thermo");
    public static final ResourceLocation MISSILE_ATLAS_VOLCANO_TEXTURE = missileTexture("missile_atlas_tectonic");
    public static final ResourceLocation MISSILE_ATLAS_DOOMSDAY_TEXTURE = missileTexture("missile_atlas_doomsday");
    public static final ResourceLocation MISSILE_ATLAS_DOOMSDAY_RUSTED_TEXTURE = missileTexture("missile_atlas_doomsday_weathered");
    public static final ResourceLocation MISSILE_MICRO_TEXTURE = missileTexture("missile_micro");
    public static final ResourceLocation MISSILE_MICRO_TAINT_TEXTURE = missileTexture("missile_micro_taint");
    public static final ResourceLocation MISSILE_MICRO_BHOLE_TEXTURE = missileTexture("missile_micro_bhole");
    public static final ResourceLocation MISSILE_MICRO_SCHRAB_TEXTURE = missileTexture("missile_micro_schrab");
    public static final ResourceLocation MISSILE_MICRO_EMP_TEXTURE = missileTexture("missile_micro_emp");
    public static final ResourceLocation MISSILE_MICRO_TEST_TEXTURE = missileTexture("missile_test");
    public static final ResourceLocation MISSILE_SHUTTLE_TEXTURE = missileTexture("missile_shuttle");
    public static final ResourceLocation MINER_ROCKET_TEXTURE = modelTexture("miner_rocket");

    public static final LegacyWavefrontModel MP_T_10_KEROSENE = legacyModel("mp_t_10_kerosene");
    public static final LegacyWavefrontModel MP_T_10_SOLID = legacyModel("mp_t_10_solid");
    public static final LegacyWavefrontModel MP_T_10_XENON = legacyModel("mp_t_10_xenon");
    public static final LegacyWavefrontModel MP_T_15_KEROSENE = legacyModel("mp_t_15_kerosene");
    public static final LegacyWavefrontModel MP_T_15_KEROSENE_DUAL = legacyModel("mp_t_15_kerosene_dual");
    public static final LegacyWavefrontModel MP_T_15_KEROSENE_TRIPLE = legacyModel("mp_t_15_kerosene_triple");
    public static final LegacyWavefrontModel MP_T_15_SOLID = legacyModel("mp_t_15_solid");
    public static final LegacyWavefrontModel MP_T_15_SOLID_HEXDECUPLE = legacyModel("mp_t_15_solid_hexdecuple");
    public static final LegacyWavefrontModel MP_T_15_BALEFIRE_SHORT = legacyModel("mp_t_15_balefire_short");
    public static final LegacyWavefrontModel MP_T_15_BALEFIRE = legacyModel("mp_t_15_balefire");
    public static final LegacyWavefrontModel MP_T_15_BALEFIRE_LARGE = legacyModel("mp_t_15_balefire_large");
    public static final LegacyWavefrontModel MP_T_20_KEROSENE = legacyModel("mp_t_20_kerosene");
    public static final LegacyWavefrontModel MP_T_20_KEROSENE_DUAL = legacyModel("mp_t_20_kerosene_dual");
    public static final LegacyWavefrontModel MP_T_20_KEROSENE_TRIPLE = legacyModel("mp_t_20_kerosene_triple");
    public static final LegacyWavefrontModel MP_T_20_SOLID = legacyModel("mp_t_20_solid");
    public static final LegacyWavefrontModel MP_T_20_SOLID_MULTI = legacyModel("mp_t_20_solid_multi");

    public static final LegacyWavefrontModel MP_S_10_FLAT = legacyModel("mp_s_10_flat");
    public static final LegacyWavefrontModel MP_S_10_CRUISE = legacyModel("mp_s_10_cruise");
    public static final LegacyWavefrontModel MP_S_10_SPACE = legacyModel("mp_s_10_space");
    public static final LegacyWavefrontModel MP_S_15_FLAT = legacyModel("mp_s_15_flat");
    public static final LegacyWavefrontModel MP_S_15_THIN = legacyModel("mp_s_15_thin");
    public static final LegacyWavefrontModel MP_S_15_SOYUZ = legacyModel("mp_s_15_soyuz");
    public static final LegacyWavefrontModel MP_S_20 = legacyModel("mp_s_20");

    public static final LegacyWavefrontModel MP_F_10_KEROSENE = legacyModel("mp_f_10_kerosene");
    public static final LegacyWavefrontModel MP_F_10_LONG_KEROSENE = legacyModel("mp_f_10_long_kerosene");
    public static final LegacyWavefrontModel MP_F_10_15_KEROSENE = legacyModel("mp_f_10_15_kerosene");
    public static final LegacyWavefrontModel MP_F_15_KEROSENE = legacyModel("mp_f_15_kerosene");
    public static final LegacyWavefrontModel MP_F_15_HYDROGEN = legacyModel("mp_f_15_hydrogen");
    public static final LegacyWavefrontModel MP_F_15_20_KEROSENE = legacyModel("mp_f_15_20_kerosene");

    public static final LegacyWavefrontModel MP_W_10_HE = legacyModel("mp_w_10_he");
    public static final LegacyWavefrontModel MP_W_10_INCENDIARY = legacyModel("mp_w_10_incendiary");
    public static final LegacyWavefrontModel MP_W_10_BUSTER = legacyModel("mp_w_10_buster");
    public static final LegacyWavefrontModel MP_W_10_NUCLEAR = legacyModel("mp_w_10_nuclear");
    public static final LegacyWavefrontModel MP_W_10_NUCLEAR_LARGE = legacyModel("mp_w_10_nuclear_large");
    public static final LegacyWavefrontModel MP_W_10_TAINT = legacyModel("mp_w_10_taint");
    public static final LegacyWavefrontModel MP_W_15_HE = legacyModel("mp_w_15_he");
    public static final LegacyWavefrontModel MP_W_15_INCENDIARY = legacyModel("mp_w_15_incendiary");
    public static final LegacyWavefrontModel MP_W_15_NUCLEAR = legacyModel("mp_w_15_nuclear");
    public static final LegacyWavefrontModel MP_W_15_BOXCAR = legacyModel("mp_w_15_boxcar");
    public static final LegacyWavefrontModel MP_W_15_N2 = legacyModel("mp_w_15_n2");
    public static final LegacyWavefrontModel MP_W_15_BALEFIRE = legacyModel("mp_w_15_balefire");
    public static final LegacyWavefrontModel MP_W_15_TURBINE = legacyModel("mp_w_15_turbine");

    private static final Map<String, LegacyWavefrontModel> MODELS = models();
    private static final Map<String, LegacyMissilePart> PARTS = parts();

    public static LegacyWavefrontModel model(String legacyModelName) {
        return MODELS.get(legacyModelName);
    }

    public static Map<String, LegacyWavefrontModel> models() {
        Map<String, LegacyWavefrontModel> models = new LinkedHashMap<>();
        models.put("mp_t_10_kerosene", MP_T_10_KEROSENE);
        models.put("mp_t_10_solid", MP_T_10_SOLID);
        models.put("mp_t_10_xenon", MP_T_10_XENON);
        models.put("mp_t_15_kerosene", MP_T_15_KEROSENE);
        models.put("mp_t_15_kerosene_dual", MP_T_15_KEROSENE_DUAL);
        models.put("mp_t_15_kerosene_triple", MP_T_15_KEROSENE_TRIPLE);
        models.put("mp_t_15_solid", MP_T_15_SOLID);
        models.put("mp_t_15_solid_hexdecuple", MP_T_15_SOLID_HEXDECUPLE);
        models.put("mp_t_15_balefire_short", MP_T_15_BALEFIRE_SHORT);
        models.put("mp_t_15_balefire", MP_T_15_BALEFIRE);
        models.put("mp_t_15_balefire_large", MP_T_15_BALEFIRE_LARGE);
        models.put("mp_t_20_kerosene", MP_T_20_KEROSENE);
        models.put("mp_t_20_kerosene_dual", MP_T_20_KEROSENE_DUAL);
        models.put("mp_t_20_kerosene_triple", MP_T_20_KEROSENE_TRIPLE);
        models.put("mp_t_20_solid", MP_T_20_SOLID);
        models.put("mp_t_20_solid_multi", MP_T_20_SOLID_MULTI);
        models.put("mp_s_10_flat", MP_S_10_FLAT);
        models.put("mp_s_10_cruise", MP_S_10_CRUISE);
        models.put("mp_s_10_space", MP_S_10_SPACE);
        models.put("mp_s_15_flat", MP_S_15_FLAT);
        models.put("mp_s_15_thin", MP_S_15_THIN);
        models.put("mp_s_15_soyuz", MP_S_15_SOYUZ);
        models.put("mp_s_20", MP_S_20);
        models.put("mp_f_10_kerosene", MP_F_10_KEROSENE);
        models.put("mp_f_10_long_kerosene", MP_F_10_LONG_KEROSENE);
        models.put("mp_f_10_15_kerosene", MP_F_10_15_KEROSENE);
        models.put("mp_f_15_kerosene", MP_F_15_KEROSENE);
        models.put("mp_f_15_hydrogen", MP_F_15_HYDROGEN);
        models.put("mp_f_15_20_kerosene", MP_F_15_20_KEROSENE);
        models.put("mp_w_10_he", MP_W_10_HE);
        models.put("mp_w_10_incendiary", MP_W_10_INCENDIARY);
        models.put("mp_w_10_buster", MP_W_10_BUSTER);
        models.put("mp_w_10_nuclear", MP_W_10_NUCLEAR);
        models.put("mp_w_10_nuclear_large", MP_W_10_NUCLEAR_LARGE);
        models.put("mp_w_10_taint", MP_W_10_TAINT);
        models.put("mp_w_15_he", MP_W_15_HE);
        models.put("mp_w_15_incendiary", MP_W_15_INCENDIARY);
        models.put("mp_w_15_nuclear", MP_W_15_NUCLEAR);
        models.put("mp_w_15_boxcar", MP_W_15_BOXCAR);
        models.put("mp_w_15_n2", MP_W_15_N2);
        models.put("mp_w_15_balefire", MP_W_15_BALEFIRE);
        models.put("mp_w_15_turbine", MP_W_15_TURBINE);
        return Collections.unmodifiableMap(models);
    }

    public static LegacyMissilePart part(String legacyItemName) {
        return PARTS.get(legacyItemName);
    }

    public static Map<String, LegacyMissilePart> parts() {
        Map<String, LegacyMissilePart> parts = new LinkedHashMap<>();
        register(parts, "mp_thruster_10_kerosene", PartKind.THRUSTER, 1, 1, MP_T_10_KEROSENE, "thrusters/mp_t_10_kerosene");
        register(parts, "mp_thruster_10_solid", PartKind.THRUSTER, 0.5, 1, MP_T_10_SOLID, "thrusters/mp_t_10_solid");
        register(parts, "mp_thruster_10_xenon", PartKind.THRUSTER, 0.5, 1, MP_T_10_XENON, "thrusters/mp_t_10_xenon");
        register(parts, "mp_thruster_15_kerosene", PartKind.THRUSTER, 1.5, 1.5, MP_T_15_KEROSENE, "thrusters/mp_t_15_kerosene");
        register(parts, "mp_thruster_15_kerosene_dual", PartKind.THRUSTER, 1, 1.5, MP_T_15_KEROSENE_DUAL, "thrusters/mp_t_15_kerosene_dual");
        register(parts, "mp_thruster_15_kerosene_triple", PartKind.THRUSTER, 1, 1.5, MP_T_15_KEROSENE_TRIPLE, "thrusters/mp_t_15_kerosene_dual");
        register(parts, "mp_thruster_15_solid", PartKind.THRUSTER, 0.5, 1, MP_T_15_SOLID, "thrusters/mp_t_15_solid");
        register(parts, "mp_thruster_15_solid_hexdecuple", PartKind.THRUSTER, 0.5, 1, MP_T_15_SOLID_HEXDECUPLE, "thrusters/mp_t_15_solid_hexdecuple");
        register(parts, "mp_thruster_15_hydrogen", PartKind.THRUSTER, 1.5, 1.5, MP_T_15_KEROSENE, "thrusters/mp_t_15_hydrogen");
        register(parts, "mp_thruster_15_hydrogen_dual", PartKind.THRUSTER, 1, 1.5, MP_T_15_KEROSENE_DUAL, "thrusters/mp_t_15_hydrogen_dual");
        register(parts, "mp_thruster_15_balefire_short", PartKind.THRUSTER, 2, 2, MP_T_15_BALEFIRE_SHORT, "thrusters/mp_t_15_balefire_short");
        register(parts, "mp_thruster_15_balefire", PartKind.THRUSTER, 3, 2.5, MP_T_15_BALEFIRE, "thrusters/mp_t_15_balefire");
        register(parts, "mp_thruster_15_balefire_large", PartKind.THRUSTER, 3, 2.5, MP_T_15_BALEFIRE_LARGE, "thrusters/mp_t_15_balefire_large");
        register(parts, "mp_thruster_15_balefire_large_rad", PartKind.THRUSTER, 3, 2.5, MP_T_15_BALEFIRE_LARGE, "thrusters/mp_t_15_balefire_large_rad");
        register(parts, "mp_thruster_20_kerosene", PartKind.THRUSTER, 3, 2.5, MP_T_20_KEROSENE, "thrusters/mp_t_20_kerosene");
        register(parts, "mp_thruster_20_kerosene_dual", PartKind.THRUSTER, 2, 2, MP_T_20_KEROSENE_DUAL, "thrusters/mp_t_20_kerosene_dual");
        register(parts, "mp_thruster_20_kerosene_triple", PartKind.THRUSTER, 2, 2, MP_T_20_KEROSENE_TRIPLE, "thrusters/mp_t_20_kerosene_dual");
        register(parts, "mp_thruster_20_solid", PartKind.THRUSTER, 1, 1.75, MP_T_20_SOLID, "thrusters/mp_t_20_solid");
        register(parts, "mp_thruster_20_solid_multi", PartKind.THRUSTER, 0.5, 1.5, MP_T_20_SOLID_MULTI, "thrusters/mp_t_20_solid_multi");
        register(parts, "mp_thruster_20_solid_multier", PartKind.THRUSTER, 0.5, 1.5, MP_T_20_SOLID_MULTI, "thrusters/mp_t_20_solid_multier");

        register(parts, "mp_stability_10_flat", PartKind.FINS, 0, 2, MP_S_10_FLAT, "stability/mp_s_10_flat");
        register(parts, "mp_stability_10_cruise", PartKind.FINS, 0, 3, MP_S_10_CRUISE, "stability/mp_s_10_cruise");
        register(parts, "mp_stability_10_space", PartKind.FINS, 0, 2, MP_S_10_SPACE, "stability/mp_s_10_space");
        register(parts, "mp_stability_15_flat", PartKind.FINS, 0, 3, MP_S_15_FLAT, "stability/mp_s_15_flat");
        register(parts, "mp_stability_15_thin", PartKind.FINS, 0, 3, MP_S_15_THIN, "stability/mp_s_15_thin");
        register(parts, "mp_stability_15_soyuz", PartKind.FINS, 0, 3, MP_S_15_SOYUZ, "stability/mp_s_15_soyuz");
        register(parts, "mp_stability_20_flat", PartKind.FINS, 0, 3, MP_S_20, UNIVERSAL_TEXTURE);

        register(parts, "mp_fuselage_10_kerosene", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/mp_f_10_kerosene");
        register(parts, "mp_fuselage_10_kerosene_camo", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/mp_f_10_kerosene_camo");
        register(parts, "mp_fuselage_10_kerosene_desert", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/mp_f_10_kerosene_desert");
        register(parts, "mp_fuselage_10_kerosene_sky", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/mp_f_10_kerosene_sky");
        register(parts, "mp_fuselage_10_kerosene_insulation", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/mp_f_10_kerosene_insulation");
        register(parts, "mp_fuselage_10_kerosene_flames", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/mp_f_10_kerosene_flames");
        register(parts, "mp_fuselage_10_kerosene_sleek", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/mp_f_10_kerosene_sleek");
        register(parts, "mp_fuselage_10_kerosene_metal", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/mp_f_10_kerosene_metal");
        register(parts, "mp_fuselage_10_kerosene_taint", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/contest/mp_f_10_kerosene_taint");
        register(parts, "mp_fuselage_10_solid", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/mp_f_10_solid");
        register(parts, "mp_fuselage_10_solid_flames", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/mp_f_10_solid_flames");
        register(parts, "mp_fuselage_10_solid_insulation", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/mp_f_10_solid_insulation");
        register(parts, "mp_fuselage_10_solid_sleek", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/mp_f_10_solid_sleek");
        register(parts, "mp_fuselage_10_solid_soviet_glory", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/mp_f_10_solid_soviet_glory");
        register(parts, "mp_fuselage_10_solid_cathedral", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/contest/mp_f_10_solid_cathedral");
        register(parts, "mp_fuselage_10_solid_moonlit", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/contest/mp_f_10_solid_moonlit");
        register(parts, "mp_fuselage_10_solid_battery", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/contest/mp_f_10_solid_battery");
        register(parts, "mp_fuselage_10_solid_duracell", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/mp_f_10_solid_duracell");
        register(parts, "mp_fuselage_10_xenon", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/mp_f_10_xenon");
        register(parts, "mp_fuselage_10_xenon_bhole", PartKind.FUSELAGE, 4, 3, MP_F_10_KEROSENE, "fuselages/contest/mp_f_10_xenon_bhole");

        register(parts, "mp_fuselage_10_long_kerosene", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/mp_f_10_long_kerosene");
        register(parts, "mp_fuselage_10_long_kerosene_camo", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/mp_f_10_long_kerosene_camo");
        register(parts, "mp_fuselage_10_long_kerosene_desert", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/mp_f_10_long_kerosene_desert");
        register(parts, "mp_fuselage_10_long_kerosene_sky", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/mp_f_10_long_kerosene_sky");
        register(parts, "mp_fuselage_10_long_kerosene_flames", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/mp_f_10_long_kerosene_flames");
        register(parts, "mp_fuselage_10_long_kerosene_insulation", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/mp_f_10_long_kerosene_insulation");
        register(parts, "mp_fuselage_10_long_kerosene_sleek", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/mp_f_10_long_kerosene_sleek");
        register(parts, "mp_fuselage_10_long_kerosene_metal", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/mp_f_10_long_kerosene_metal");
        register(parts, "mp_fuselage_10_long_kerosene_dash", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/contest/mp_f_10_long_kerosene_dash");
        register(parts, "mp_fuselage_10_long_kerosene_taint", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/contest/mp_f_10_long_kerosene_taint");
        register(parts, "mp_fuselage_10_long_kerosene_vap", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/contest/mp_f_10_long_kerosene_vap");
        register(parts, "mp_fuselage_10_long_solid", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/mp_f_10_long_solid");
        register(parts, "mp_fuselage_10_long_solid_flames", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/mp_f_10_long_solid_flames");
        register(parts, "mp_fuselage_10_long_solid_insulation", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/mp_f_10_long_solid_insulation");
        register(parts, "mp_fuselage_10_long_solid_sleek", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/mp_f_10_long_solid_sleek");
        register(parts, "mp_fuselage_10_long_solid_soviet_glory", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/mp_f_10_long_solid_soviet_glory");
        register(parts, "mp_fuselage_10_long_solid_bullet", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/contest/mp_f_10_long_solid_bullet");
        register(parts, "mp_fuselage_10_long_solid_silvermoonlight", PartKind.FUSELAGE, 7, 5, MP_F_10_LONG_KEROSENE, "fuselages/contest/mp_f_10_long_solid_silvermoonlight");

        register(parts, "mp_fuselage_10_15_kerosene", PartKind.FUSELAGE, 9, 5.5, MP_F_10_15_KEROSENE, "fuselages/mp_f_10_15_kerosene");
        register(parts, "mp_fuselage_10_15_solid", PartKind.FUSELAGE, 9, 5.5, MP_F_10_15_KEROSENE, "fuselages/mp_f_10_15_solid");
        register(parts, "mp_fuselage_10_15_hydrogen", PartKind.FUSELAGE, 9, 5.5, MP_F_10_15_KEROSENE, "fuselages/mp_f_10_15_hydrogen");
        register(parts, "mp_fuselage_10_15_balefire", PartKind.FUSELAGE, 9, 5.5, MP_F_10_15_KEROSENE, "fuselages/mp_f_10_15_balefire");

        register(parts, "mp_fuselage_15_kerosene", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_kerosene");
        register(parts, "mp_fuselage_15_kerosene_camo", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_kerosene_camo");
        register(parts, "mp_fuselage_15_kerosene_desert", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_kerosene_desert");
        register(parts, "mp_fuselage_15_kerosene_sky", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_kerosene_sky");
        register(parts, "mp_fuselage_15_kerosene_insulation", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_kerosene_insulation");
        register(parts, "mp_fuselage_15_kerosene_metal", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_kerosene_metal");
        register(parts, "mp_fuselage_15_kerosene_decorated", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_kerosene_decorated");
        register(parts, "mp_fuselage_15_kerosene_steampunk", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_kerosene_steampunk");
        register(parts, "mp_fuselage_15_kerosene_polite", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_kerosene_polite");
        register(parts, "mp_fuselage_15_kerosene_blackjack", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/base/mp_f_15_kerosene_blackjack");
        register(parts, "mp_fuselage_15_kerosene_lambda", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/contest/mp_f_15_kerosene_lambda");
        register(parts, "mp_fuselage_15_kerosene_minuteman", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/contest/mp_f_15_kerosene_minuteman");
        register(parts, "mp_fuselage_15_kerosene_pip", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/contest/mp_f_15_kerosene_pip");
        register(parts, "mp_fuselage_15_kerosene_taint", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/contest/mp_f_15_kerosene_taint");
        register(parts, "mp_fuselage_15_kerosene_yuck", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_kerosene_yuck");
        register(parts, "mp_fuselage_15_solid", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_solid");
        register(parts, "mp_fuselage_15_solid_insulation", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_solid_insulation");
        register(parts, "mp_fuselage_15_solid_desh", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_solid_desh");
        register(parts, "mp_fuselage_15_solid_soviet_glory", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_solid_soviet_glory");
        register(parts, "mp_fuselage_15_solid_soviet_stank", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_solid_soviet_stank");
        register(parts, "mp_fuselage_15_solid_faust", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/contest/mp_f_15_solid_faust");
        register(parts, "mp_fuselage_15_solid_silvermoonlight", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/contest/mp_f_15_solid_silvermoonlight");
        register(parts, "mp_fuselage_15_solid_snowy", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/contest/mp_f_15_solid_snowy");
        register(parts, "mp_fuselage_15_solid_panorama", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_solid_panorama");
        register(parts, "mp_fuselage_15_solid_roses", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_solid_roses");
        register(parts, "mp_fuselage_15_solid_mimi", PartKind.FUSELAGE, 10, 6, MP_F_15_KEROSENE, "fuselages/mp_f_15_solid_mimi");
        register(parts, "mp_fuselage_15_hydrogen", PartKind.FUSELAGE, 10, 6, MP_F_15_HYDROGEN, "fuselages/mp_f_15_hydrogen");
        register(parts, "mp_fuselage_15_hydrogen_cathedral", PartKind.FUSELAGE, 10, 6, MP_F_15_HYDROGEN, "fuselages/contest/mp_f_15_hydrogen_cathedral");
        register(parts, "mp_fuselage_15_balefire", PartKind.FUSELAGE, 10, 6, MP_F_15_HYDROGEN, "fuselages/mp_f_15_balefire");

        register(parts, "mp_fuselage_15_20_kerosene", PartKind.FUSELAGE, 16, 10, MP_F_15_20_KEROSENE, "fuselages/mp_f_15_20_kerosene");
        register(parts, "mp_fuselage_15_20_kerosene_magnusson", PartKind.FUSELAGE, 16, 10, MP_F_15_20_KEROSENE, "fuselages/mp_f_15_20_kerosene_magnusson");
        register(parts, "mp_fuselage_15_20_solid", PartKind.FUSELAGE, 16, 10, MP_F_15_20_KEROSENE, "fuselages/mp_f_15_20_solid");

        register(parts, "mp_warhead_10_he", PartKind.WARHEAD, 2, 1.5, MP_W_10_HE, "warheads/mp_w_10_he");
        register(parts, "mp_warhead_10_incendiary", PartKind.WARHEAD, 2.5, 2, MP_W_10_INCENDIARY, "warheads/mp_w_10_incendiary");
        register(parts, "mp_warhead_10_buster", PartKind.WARHEAD, 0.5, 1, MP_W_10_BUSTER, "warheads/mp_w_10_buster");
        register(parts, "mp_warhead_10_nuclear", PartKind.WARHEAD, 2, 1.5, MP_W_10_NUCLEAR, "warheads/mp_w_10_nuclear");
        register(parts, "mp_warhead_10_nuclear_large", PartKind.WARHEAD, 2.5, 1.5, MP_W_10_NUCLEAR_LARGE, "warheads/mp_w_10_nuclear_large");
        register(parts, "mp_warhead_10_taint", PartKind.WARHEAD, 2.25, 1.5, MP_W_10_TAINT, "warheads/mp_w_10_taint");
        register(parts, "mp_warhead_10_cloud", PartKind.WARHEAD, 2.25, 1.5, MP_W_10_TAINT, "warheads/mp_w_10_cloud");
        register(parts, "mp_warhead_15_he", PartKind.WARHEAD, 2, 1.5, MP_W_15_HE, "warheads/mp_w_15_he");
        register(parts, "mp_warhead_15_incendiary", PartKind.WARHEAD, 2, 1.5, MP_W_15_INCENDIARY, "warheads/mp_w_15_incendiary");
        register(parts, "mp_warhead_15_nuclear", PartKind.WARHEAD, 3.5, 2, MP_W_15_NUCLEAR, "warheads/mp_w_15_nuclear");
        register(parts, "mp_warhead_15_nuclear_shark", PartKind.WARHEAD, 3.5, 2, MP_W_15_NUCLEAR, "warheads/mp_w_15_nuclear_shark");
        register(parts, "mp_warhead_15_nuclear_mimi", PartKind.WARHEAD, 3.5, 2, MP_W_15_NUCLEAR, "warheads/mp_w_15_nuclear_mimi");
        register(parts, "mp_warhead_15_boxcar", PartKind.WARHEAD, 2.25, 7.5, MP_W_15_BOXCAR, BOXCAR_TEXTURE);
        register(parts, "mp_warhead_15_n2", PartKind.WARHEAD, 3, 2, MP_W_15_N2, "warheads/mp_w_15_n2");
        register(parts, "mp_warhead_15_balefire", PartKind.WARHEAD, 2.75, 2, MP_W_15_BALEFIRE, "warheads/mp_w_15_balefire");
        register(parts, "mp_warhead_15_turbine", PartKind.WARHEAD, 2.25, 2, MP_W_15_TURBINE, "warheads/mp_w_15_turbine");
        return Collections.unmodifiableMap(parts);
    }

    public static void renderMissile(LegacyMissilePart thruster, LegacyMissilePart fins, LegacyMissilePart fuselage,
            LegacyMissilePart warhead, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        for (MissileRenderStep step : missileRenderPlan(thruster, fins, fuselage, warhead).steps()) {
            step.part().render(poseStack, buffer, packedLight, packedOverlay);
            if (step.translateAfterY() != 0.0D) {
                poseStack.translate(0.0D, step.translateAfterY(), 0.0D);
            }
        }
        poseStack.popPose();
    }

    public static MissileRenderPlan missileRenderPlan(LegacyMissilePart thruster, LegacyMissilePart fins,
            LegacyMissilePart fuselage, LegacyMissilePart warhead) {
        List<MissileRenderStep> steps = missileSteps(thruster, fins, fuselage, warhead);
        return new MissileRenderPlan(
                isKind(thruster, PartKind.THRUSTER),
                isKind(fins, PartKind.FINS),
                isKind(fuselage, PartKind.FUSELAGE),
                isKind(warhead, PartKind.WARHEAD),
                missileHeight(thruster, fuselage, warhead),
                stackedRenderHeight(steps),
                steps,
                missilePlacementSteps(steps));
    }

    public static List<MissileRenderStep> missileSteps(LegacyMissilePart thruster, LegacyMissilePart fins,
            LegacyMissilePart fuselage, LegacyMissilePart warhead) {
        List<MissileRenderStep> steps = new ArrayList<>(4);
        if (isKind(thruster, PartKind.THRUSTER)) {
            steps.add(new MissileRenderStep(thruster, thruster.height()));
        }
        if (isKind(fuselage, PartKind.FUSELAGE)) {
            if (isKind(fins, PartKind.FINS)) {
                steps.add(new MissileRenderStep(fins, 0.0D));
            }
            steps.add(new MissileRenderStep(fuselage, fuselage.height()));
        }
        if (isKind(warhead, PartKind.WARHEAD)) {
            steps.add(new MissileRenderStep(warhead, 0.0D));
        }
        return Collections.unmodifiableList(steps);
    }

    public static List<MissilePlacementStep> missilePlacementSteps(LegacyMissilePart thruster, LegacyMissilePart fins,
            LegacyMissilePart fuselage, LegacyMissilePart warhead) {
        return missilePlacementSteps(missileSteps(thruster, fins, fuselage, warhead));
    }

    public static List<MissilePlacementStep> missilePlacementSteps(List<MissileRenderStep> steps) {
        List<MissilePlacementStep> placements = new ArrayList<>(steps.size());
        double y = 0.0D;
        for (int index = 0; index < steps.size(); index++) {
            MissileRenderStep step = steps.get(index);
            placements.add(new MissilePlacementStep(index, step.part(), y, step.translateAfterY(), y + step.translateAfterY()));
            y += step.translateAfterY();
        }
        return Collections.unmodifiableList(placements);
    }

    public static double missileHeight(LegacyMissilePart thruster, LegacyMissilePart fuselage, LegacyMissilePart warhead) {
        double height = 0.0D;
        if (isKind(warhead, PartKind.WARHEAD)) {
            height += warhead.height();
        }
        if (isKind(fuselage, PartKind.FUSELAGE)) {
            height += fuselage.height();
        }
        if (isKind(thruster, PartKind.THRUSTER)) {
            height += thruster.height();
        }
        return height;
    }

    public static double stackedRenderHeight(List<MissileRenderStep> steps) {
        double height = 0.0D;
        for (MissileRenderStep step : steps) {
            height += step.translateAfterY();
        }
        return height;
    }

    public static boolean isKind(LegacyMissilePart part, PartKind kind) {
        return part != null && part.kind() == kind;
    }

    private static void register(Map<String, LegacyMissilePart> parts, String legacyItemName, PartKind kind,
            double height, double guiHeight, LegacyWavefrontModel model, String texturePath) {
        register(parts, legacyItemName, kind, height, guiHeight, model, texture(texturePath));
    }

    private static void register(Map<String, LegacyMissilePart> parts, String legacyItemName, PartKind kind,
            double height, double guiHeight, LegacyWavefrontModel model, ResourceLocation texture) {
        parts.put(legacyItemName, new LegacyMissilePart(legacyItemName, kind, height, guiHeight, model, texture));
    }

    private static LegacyWavefrontModel legacyModel(String name) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/missile_parts/" + name + ".obj"),
                UNIVERSAL_TEXTURE).asVBO();
    }

    private static LegacyWavefrontModel missileModel(String name) {
        return missileModel(name, name);
    }

    private static LegacyWavefrontModel missileModel(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/" + modelName + ".obj"),
                missileTexture(textureName));
    }

    public static ResourceLocation missileTexture(String name) {
        return modelTexture("missile/" + name);
    }

    public static ResourceLocation missilePartTexture(String path) {
        return modelTexture("missile_parts/" + path);
    }

    public static ResourceLocation textureForPart(String legacyItemName) {
        LegacyMissilePart part = part(legacyItemName);
        return part == null ? null : part.texture();
    }

    private static ResourceLocation texture(String path) {
        return missilePartTexture(path);
    }

    private static ResourceLocation modelTexture(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/" + path + ".png");
    }

    private ObjMissilePartModels() {
    }

    public enum PartKind {
        THRUSTER,
        FINS,
        FUSELAGE,
        WARHEAD
    }

    public record LegacyMissilePart(
            String legacyItemName,
            PartKind kind,
            double height,
            double guiHeight,
            LegacyWavefrontModel model,
            ResourceLocation texture) {
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
            model.renderAll(texture, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    public record MissileRenderStep(LegacyMissilePart part, double translateAfterY) {
    }

    public record MissilePlacementStep(int index, LegacyMissilePart part, double yBefore,
                                       double translateAfterY, double yAfter) {
    }

    public record MissileRenderPlan(
            boolean hasThruster,
            boolean hasFins,
            boolean hasFuselage,
            boolean hasWarhead,
            double multipartHeight,
            double stackedRenderHeight,
            List<MissileRenderStep> steps,
            List<MissilePlacementStep> placements) {
        public boolean hasCompleteStack() {
            return hasThruster && hasFuselage && hasWarhead;
        }
    }
}
