package com.hbm.ntm.bullet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class LegacySednaMagazineConfigs {
    private static final Map<String, SednaMagazineConfig> BY_KEY = new LinkedHashMap<>();
    private static final Map<String, List<SednaMagazineConfig>> BY_OWNER = new LinkedHashMap<>();
    private static final Map<String, MagazineModifier> MODIFIERS_BY_KEY = new LinkedHashMap<>();
    private static final Map<String, DefaultAmmo> DEFAULT_AMMO_BY_OWNER = new LinkedHashMap<>();

    private static final List<String> P22 = cfg("p22_sp", "p22_fmj", "p22_jhp", "p22_ap");
    private static final List<String> P9 = cfg("p9_sp", "p9_fmj", "p9_jhp", "p9_ap");
    private static final List<String> P45 = cfg("p45_sp", "p45_fmj", "p45_jhp", "p45_ap", "p45_du");
    private static final List<String> M357 = cfg("m357_bp", "m357_sp", "m357_fmj", "m357_jhp", "m357_ap", "m357_express");
    private static final List<String> M357_CALIBER = cfg("m357_sp", "m357_fmj", "m357_jhp", "m357_ap", "m357_express");
    private static final List<String> M44 = cfg("m44_bp", "m44_sp", "m44_fmj", "m44_jhp", "m44_ap", "m44_express");
    private static final List<String> M44_CALIBER = cfg("m44_sp", "m44_fmj", "m44_jhp", "m44_ap", "m44_express");
    private static final List<String> R556 = cfg("r556_sp", "r556_fmj", "r556_jhp", "r556_ap");
    private static final List<String> R556_INC = cfg("r556_inc_sp", "r556_inc_fmj", "r556_inc_jhp", "r556_inc_ap");
    private static final List<String> R762 = cfg("r762_sp", "r762_fmj", "r762_jhp", "r762_ap", "r762_du", "r762_he");
    private static final List<String> BMG50 = cfg("bmg50_sp", "bmg50_fmj", "bmg50_jhp", "bmg50_ap", "bmg50_du", "bmg50_sm", "bmg50_he");
    private static final List<String> BMG50_BELT = cfg("bmg50_sp", "bmg50_fmj", "bmg50_jhp", "bmg50_ap", "bmg50_du", "bmg50_he");
    private static final List<String> B75 = cfg("b75", "b75_inc", "b75_exp");
    private static final List<String> G10 = cfg("g10", "g10_shrapnel", "g10_du", "g10_slug", "g10_explosive");
    private static final List<String> G12 = cfg("g12_bp", "g12_bp_magnum", "g12_bp_slug", "g12", "g12_slug",
            "g12_flechette", "g12_magnum", "g12_explosive", "g12_phosphorus");
    private static final List<String> G12_BROKEN = cfg("g12_equestrian_tkr", "g12_bp", "g12_bp_magnum",
            "g12_bp_slug", "g12", "g12_slug", "g12_flechette", "g12_magnum", "g12_explosive",
            "g12_phosphorus");
    private static final List<String> G12_SEXY = cfg("g12_equestrian_bj", "g12_bp", "g12_bp_magnum",
            "g12_bp_slug", "g12", "g12_slug", "g12_flechette", "g12_magnum", "g12_explosive",
            "g12_phosphorus");
    private static final List<String> G12_SHREDDER = cfg("g12_shredder", "g12_shredder_slug",
            "g12_shredder_flechette", "g12_shredder_magnum", "g12_shredder_explosive",
            "g12_shredder_phosphorus");
    private static final List<String> G26 = cfg("g26_flare", "g26_flare_supply", "g26_flare_weapon");
    private static final List<String> G40 = cfg("g40_he", "g40_heat", "g40_demo", "g40_inc", "g40_phosphorus");
    private static final List<String> ROCKET_RPZB = cfg("rocket_rpzb_he", "rocket_rpzb_heat", "rocket_rpzb_demo",
            "rocket_rpzb_inc", "rocket_rpzb_phosphorus");
    private static final List<String> ROCKET_QD = cfg("rocket_qd_he", "rocket_qd_heat", "rocket_qd_demo",
            "rocket_qd_inc", "rocket_qd_phosphorus");
    private static final List<String> ROCKET_ML = cfg("rocket_ml_he", "rocket_ml_heat", "rocket_ml_demo",
            "rocket_ml_inc", "rocket_ml_phosphorus");
    private static final List<String> FLAME = cfg("flame_diesel", "flame_gas", "flame_napalm", "flame_balefire");
    private static final List<String> FLAME_TOPAZ = cfg("flame_topaz_diesel", "flame_topaz_gas",
            "flame_topaz_napalm", "flame_topaz_balefire");
    private static final List<String> FLAME_DAYBREAKER = cfg("flame_daybreaker_diesel", "flame_daybreaker_gas",
            "flame_daybreaker_napalm", "flame_daybreaker_balefire");
    private static final List<String> ENERGY_TESLA = cfg("energy_tesla", "energy_tesla_overcharge",
            "energy_tesla_ir");
    private static final List<String> ENERGY_LAS = cfg("energy_las", "energy_las_overcharge", "energy_las_ir");
    private static final List<String> ENERGY_EMERALD = cfg("energy_emerald", "energy_emerald_overcharge",
            "energy_emerald_ir");
    private static final List<String> ENERGY_LACUNAE = cfg("energy_lacunae", "energy_lacunae_overcharge",
            "energy_lacunae_ir");
    private static final List<String> TAU = cfg("tau_uranium");
    private static final List<String> TAU_CHARGE = cfg("tau_uranium_charge");
    private static final List<String> COIL = cfg("coil_tungsten", "coil_ferrouranium");
    private static final List<String> NUKE = cfg("nuke_standard", "nuke_demo", "nuke_high", "nuke_tots",
            "nuke_hive", "nuke_balefire");
    private static final List<String> FOLLY = cfg("folly_sm", "folly_nuke");
    private static final List<String> P35800 = cfg("p35800", "p35800_bl");
    private static final List<String> BLACK_POWDER = cfg("stone", "flint", "iron", "shot");
    private static final List<String> FIRE_EXT = cfg("fext_water", "fext_foam", "fext_sand");
    private static final List<String> CHARGE_THROWER = cfg("ct_hook", "ct_mortar", "ct_mortar_charge");

    public static final SednaMagazineConfig GUN_DEBUG_PRIMARY = register(full("gun_debug.receiver0",
            "gun_debug", "GunFactory", 0, 12, cfg("ammo_debug")));
    public static final SednaMagazineConfig GUN_DEBUG_SECONDARY = register(full("gun_debug.receiver1",
            "gun_debug", "GunFactory", 1, 12, cfg("ammo_debug_shot")));

    public static final SednaMagazineConfig GUN_PEPPERBOX = register(full("gun_pepperbox.receiver0",
            "gun_pepperbox", "XFactoryBlackPowder", 0, 6, BLACK_POWDER));

    public static final SednaMagazineConfig GUN_LIGHT_REVOLVER = register(full("gun_light_revolver.receiver0",
            "gun_light_revolver", "XFactory357", 0, 6, M357));
    public static final SednaMagazineConfig GUN_LIGHT_REVOLVER_ATLAS = register(full(
            "gun_light_revolver_atlas.receiver0", "gun_light_revolver_atlas", "XFactory357", 0, 6, M357));
    public static final SednaMagazineConfig GUN_LIGHT_REVOLVER_DANI_PRIMARY = register(full(
            "gun_light_revolver_dani.receiver0", "gun_light_revolver_dani", "XFactory357", 0, 6, M357));
    public static final SednaMagazineConfig GUN_LIGHT_REVOLVER_DANI_SECONDARY = register(full(
            "gun_light_revolver_dani.receiver1", "gun_light_revolver_dani", "XFactory357", 1, 6, M357));

    public static final SednaMagazineConfig GUN_HENRY = register(single("gun_henry.receiver0", "gun_henry",
            "XFactory44", 0, 14, M44));
    public static final SednaMagazineConfig GUN_HENRY_LINCOLN = register(single("gun_henry_lincoln.receiver0",
            "gun_henry_lincoln", "XFactory44", 0, 14, M44));
    public static final SednaMagazineConfig GUN_HEAVY_REVOLVER = register(full("gun_heavy_revolver.receiver0",
            "gun_heavy_revolver", "XFactory44", 0, 6, M44));
    public static final SednaMagazineConfig GUN_HEAVY_REVOLVER_LILMAC = register(full(
            "gun_heavy_revolver_lilmac.receiver0", "gun_heavy_revolver_lilmac", "XFactory44", 0, 6,
            prepend("m44_equestrian_pip", M44)));
    public static final SednaMagazineConfig GUN_HEAVY_REVOLVER_PROTEGE = register(full(
            "gun_heavy_revolver_protege.receiver0", "gun_heavy_revolver_protege", "XFactory44", 0, 6,
            prepend("m44_equestrian_mn7", M44)));
    public static final SednaMagazineConfig GUN_HANGMAN = register(full("gun_hangman.receiver0",
            "gun_hangman", "XFactory44", 0, 8, M44));

    public static final SednaMagazineConfig GUN_GREASEGUN = register(full("gun_greasegun.receiver0",
            "gun_greasegun", "XFactory9mm", 0, 30, P9));
    public static final SednaMagazineConfig GUN_LAG = register(full("gun_lag.receiver0", "gun_lag",
            "XFactory9mm", 0, 17, P9));
    public static final SednaMagazineConfig GUN_UZI = register(full("gun_uzi.receiver0", "gun_uzi",
            "XFactory9mm", 0, 30, P9));
    public static final SednaMagazineConfig GUN_UZI_AKIMBO_PRIMARY = register(full("gun_uzi_akimbo.receiver0",
            "gun_uzi_akimbo", "XFactory9mm", 0, 30, P9));
    public static final SednaMagazineConfig GUN_UZI_AKIMBO_SECONDARY = register(full("gun_uzi_akimbo.receiver1",
            "gun_uzi_akimbo", "XFactory9mm", 1, 30, P9));

    public static final SednaMagazineConfig GUN_AM180 = register(full("gun_am180.receiver0", "gun_am180",
            "XFactory22lr", 0, 177, P22));
    public static final SednaMagazineConfig GUN_STAR_F = register(full("gun_star_f.receiver0", "gun_star_f",
            "XFactory22lr", 0, 15, P22));
    public static final SednaMagazineConfig GUN_STAR_F_AKIMBO_PRIMARY = register(full("gun_star_f_akimbo.receiver0",
            "gun_star_f_akimbo", "XFactory22lr", 0, 15, P22));
    public static final SednaMagazineConfig GUN_STAR_F_AKIMBO_SECONDARY = register(full("gun_star_f_akimbo.receiver1",
            "gun_star_f_akimbo", "XFactory22lr", 1, 15, P22));

    public static final SednaMagazineConfig GUN_G3 = register(full("gun_g3.receiver0", "gun_g3",
            "XFactory556mm", 0, 30, R556));
    public static final SednaMagazineConfig GUN_G3_ZEBRA = register(full("gun_g3_zebra.receiver0",
            "gun_g3_zebra", "XFactory556mm", 0, 30, R556_INC));
    public static final SednaMagazineConfig GUN_STG77 = register(full("gun_stg77.receiver0", "gun_stg77",
            "XFactory556mm", 0, 30, R556));

    public static final SednaMagazineConfig GUN_CARBINE = register(full("gun_carbine.receiver0", "gun_carbine",
            "XFactory762mm", 0, 14, R762));
    public static final SednaMagazineConfig GUN_MINIGUN = register(belt("gun_minigun.receiver0", "gun_minigun",
            "XFactory762mm", R762, "Inventory-scanning belt magazine."));
    public static final SednaMagazineConfig GUN_MINIGUN_LACUNAE = register(full("gun_minigun_lacunae.receiver0",
            "gun_minigun_lacunae", "XFactory762mm", 0, 200, ENERGY_LACUNAE));
    public static final SednaMagazineConfig GUN_MINIGUN_DUAL_PRIMARY = register(belt("gun_minigun_dual.receiver0",
            "gun_minigun_dual", "XFactory762mm", R762, "First belt receiver."));
    public static final SednaMagazineConfig GUN_MINIGUN_DUAL_SECONDARY = register(belt("gun_minigun_dual.receiver1",
            "gun_minigun_dual", "XFactory762mm", R762, "Second belt receiver."));
    public static final SednaMagazineConfig GUN_MAS36 = register(full("gun_mas36.receiver0", "gun_mas36",
            "XFactory762mm", 0, 7, R762));

    public static final SednaMagazineConfig GUN_MARESLEG = register(single("gun_maresleg.receiver0",
            "gun_maresleg", "XFactory12ga", 0, 6, G12));
    public static final SednaMagazineConfig GUN_MARESLEG_AKIMBO_PRIMARY = register(single(
            "gun_maresleg_akimbo.receiver0", "gun_maresleg_akimbo", "XFactory12ga", 0, 6, G12));
    public static final SednaMagazineConfig GUN_MARESLEG_AKIMBO_SECONDARY = register(single(
            "gun_maresleg_akimbo.receiver1", "gun_maresleg_akimbo", "XFactory12ga", 1, 6, G12));
    public static final SednaMagazineConfig GUN_MARESLEG_BROKEN = register(single("gun_maresleg_broken.receiver0",
            "gun_maresleg_broken", "XFactory12ga", 0, 6, G12_BROKEN));
    public static final SednaMagazineConfig GUN_LIBERATOR = register(single("gun_liberator.receiver0",
            "gun_liberator", "XFactory12ga", 0, 4, G12));
    public static final SednaMagazineConfig GUN_SPAS12 = register(single("gun_spas12.receiver0",
            "gun_spas12", "XFactory12ga", 0, 8, G12));
    public static final SednaMagazineConfig GUN_AUTOSHOTGUN = register(full("gun_autoshotgun.receiver0",
            "gun_autoshotgun", "XFactory12ga", 0, 20, G12));
    public static final SednaMagazineConfig GUN_AUTOSHOTGUN_SHREDDER = register(belt(
            "gun_autoshotgun_shredder.receiver0", "gun_autoshotgun_shredder", "XFactory12ga", G12_SHREDDER,
            "Shredder belt scans inventory for beam configs."));
    public static final SednaMagazineConfig GUN_AUTOSHOTGUN_SEXY = register(full(
            "gun_autoshotgun_sexy.receiver0", "gun_autoshotgun_sexy", "XFactory12ga", 0, 100, G12_SEXY));

    public static final SednaMagazineConfig GUN_DOUBLE_BARREL = register(full("gun_double_barrel.receiver0",
            "gun_double_barrel", "XFactory10ga", 0, 2, G10));
    public static final SednaMagazineConfig GUN_DOUBLE_BARREL_SACRED_DRAGON = register(full(
            "gun_double_barrel_sacred_dragon.receiver0", "gun_double_barrel_sacred_dragon", "XFactory10ga", 0, 2,
            G10));
    public static final SednaMagazineConfig GUN_AUTOSHOTGUN_HERETIC = register(full(
            "gun_autoshotgun_heretic.receiver0", "gun_autoshotgun_heretic", "XFactory10ga", 0, 250, G10));

    public static final SednaMagazineConfig GUN_FLAREGUN = register(single("gun_flaregun.receiver0",
            "gun_flaregun", "XFactory40mm", 0, 1, G26));
    public static final SednaMagazineConfig GUN_CONGOLAKE = register(single("gun_congolake.receiver0",
            "gun_congolake", "XFactory40mm", 0, 4, G40));
    public static final SednaMagazineConfig GUN_MK108 = register(full("gun_mk108.receiver0",
            "gun_mk108", "XFactory40mm", 0, 30, G40));

    public static final SednaMagazineConfig GUN_AMAT = register(full("gun_amat.receiver0", "gun_amat",
            "XFactory50", 0, 7, BMG50));
    public static final SednaMagazineConfig GUN_AMAT_SUBTLETY = register(full("gun_amat_subtlety.receiver0",
            "gun_amat_subtlety", "XFactory50", 0, 7, prepend("bmg50_equestrian", BMG50)));
    public static final SednaMagazineConfig GUN_AMAT_PENANCE = register(full("gun_amat_penance.receiver0",
            "gun_amat_penance", "XFactory50", 0, 7, append(BMG50, "bmg50_black")));
    public static final SednaMagazineConfig GUN_M2 = register(belt("gun_m2.receiver0", "gun_m2",
            "XFactory50", BMG50_BELT, ".50 BMG inventory belt."));

    public static final SednaMagazineConfig GUN_BOLTER = register(full("gun_bolter.receiver0", "gun_bolter",
            "XFactory75Bolt", 0, 30, B75));
    public static final SednaMagazineConfig GUN_ABERRATOR = register(full("gun_aberrator.receiver0",
            "gun_aberrator", "XFactory35800", 0, 5, P35800));
    public static final SednaMagazineConfig GUN_ABERRATOR_EOTT_PRIMARY = register(full(
            "gun_aberrator_eott.receiver0", "gun_aberrator_eott", "XFactory35800", 0, 5, P35800));
    public static final SednaMagazineConfig GUN_ABERRATOR_EOTT_SECONDARY = register(full(
            "gun_aberrator_eott.receiver1", "gun_aberrator_eott", "XFactory35800", 1, 5, P35800));

    public static final SednaMagazineConfig GUN_PANZERSCHRECK = register(single("gun_panzerschreck.receiver0",
            "gun_panzerschreck", "XFactoryRocket", 0, 1, ROCKET_RPZB));
    public static final SednaMagazineConfig GUN_STINGER = register(single("gun_stinger.receiver0",
            "gun_stinger", "XFactoryRocket", 0, 1, ROCKET_RPZB));
    public static final SednaMagazineConfig GUN_QUADRO = register(full("gun_quadro.receiver0",
            "gun_quadro", "XFactoryRocket", 0, 4, ROCKET_QD));
    public static final SednaMagazineConfig GUN_MISSILE_LAUNCHER = register(single("gun_missile_launcher.receiver0",
            "gun_missile_launcher", "XFactoryRocket", 0, 1, ROCKET_ML));

    public static final SednaMagazineConfig GUN_FLAMER = register(full("gun_flamer.receiver0",
            "gun_flamer", "XFactoryFlamer", 0, 300, FLAME));
    public static final SednaMagazineConfig GUN_FLAMER_TOPAZ = register(full("gun_flamer_topaz.receiver0",
            "gun_flamer_topaz", "XFactoryFlamer", 0, 500, FLAME_TOPAZ));
    public static final SednaMagazineConfig GUN_FLAMER_DAYBREAKER = register(full("gun_flamer_daybreaker.receiver0",
            "gun_flamer_daybreaker", "XFactoryFlamer", 0, 50, FLAME_DAYBREAKER));
    public static final SednaMagazineConfig GUN_CHEMTHROWER = register(SednaMagazineConfig.fluid(
            "gun_chemthrower.receiver0", "gun_chemthrower", "XFactoryFlamer", 0, 3000,
            "ItemGunChemthrower fills this MagazineFluid from fluid containers and stores fluid id in magtype0."));

    public static final SednaMagazineConfig GUN_TESLA_CANNON = register(belt("gun_tesla_cannon.receiver0",
            "gun_tesla_cannon", "XFactoryEnergy", ENERGY_TESLA, "Tesla cannon scans capacitor stacks in inventory."));
    public static final SednaMagazineConfig GUN_LASER_PISTOL = register(full("gun_laser_pistol.receiver0",
            "gun_laser_pistol", "XFactoryEnergy", 0, 30, ENERGY_LAS));
    public static final SednaMagazineConfig GUN_LASER_PISTOL_PEW_PEW = register(full(
            "gun_laser_pistol_pew_pew.receiver0", "gun_laser_pistol_pew_pew", "XFactoryEnergy", 0, 10,
            ENERGY_LAS));
    public static final SednaMagazineConfig GUN_LASER_PISTOL_MORNING_GLORY = register(full(
            "gun_laser_pistol_morning_glory.receiver0", "gun_laser_pistol_morning_glory", "XFactoryEnergy", 0, 20,
            ENERGY_EMERALD));
    public static final SednaMagazineConfig GUN_LASRIFLE = register(full("gun_lasrifle.receiver0",
            "gun_lasrifle", "XFactoryEnergy", 0, 24, ENERGY_LAS));

    public static final SednaMagazineConfig GUN_TAU = register(belt("gun_tau.receiver0", "gun_tau",
            "XFactoryAccelerator", TAU, "Primary Tau belt scans TAU_URANIUM."));
    public static final SednaMagazineConfig GUN_TAU_CHARGE = register(belt("gun_tau.charge",
            "gun_tau", "XFactoryAccelerator", TAU_CHARGE,
            "Secondary release uses static tauChargeMag instead of the receiver magazine."));
    public static final SednaMagazineConfig GUN_COILGUN = register(single("gun_coilgun.receiver0",
            "gun_coilgun", "XFactoryAccelerator", 0, 1, COIL));
    public static final SednaMagazineConfig GUN_NI4NI = register(SednaMagazineConfig.infinite(
            "gun_n_i_4_n_i.receiver0", "gun_n_i_4_n_i", "XFactoryAccelerator", "ni4ni_arc"));

    public static final SednaMagazineConfig GUN_FATMAN = register(single("gun_fatman.receiver0",
            "gun_fatman", "XFactoryCatapult", 0, 1, NUKE));
    public static final SednaMagazineConfig GUN_FOLLY = register(single("gun_folly.receiver0",
            "gun_folly", "XFactoryFolly", 0, 1, FOLLY));
    public static final SednaMagazineConfig GUN_FIREEXT = register(full("gun_fireext.receiver0",
            "gun_fireext", "XFactoryTool", 0, 300, FIRE_EXT));
    public static final SednaMagazineConfig GUN_CHARGE_THROWER = register(full("gun_charge_thrower.receiver0",
            "gun_charge_thrower", "XFactoryTool", 0, 1, CHARGE_THROWER));

    public static final SednaMagazineConfig GUN_DRILL_BASE = register(SednaMagazineConfig.liquidEngine(
            "gun_drill.receiver0", "gun_drill", "XFactoryDrill", 0, 4000,
            cfg("GASOLINE", "GASOLINE_LEADED", "COALGAS", "COALGAS_LEADED"),
            "Base drill engine consumes liquid fuel and does not store a magtype key."));
    public static final SednaMagazineConfig MOD_ENGINE_DIESEL = register(SednaMagazineConfig.liquidEngine(
            "weapon_mod_engine_diesel.magazine", "weapon_mod_special:ENGINE_DIESEL", "WeaponModEngine", 0, 4000,
            cfg("DIESEL", "DIESEL_CRACK", "LIGHTOIL"), "Replacement engine magazine; delayAfterFire=15."));
    public static final SednaMagazineConfig MOD_ENGINE_AVIATION = register(SednaMagazineConfig.liquidEngine(
            "weapon_mod_engine_aviation.magazine", "weapon_mod_special:ENGINE_AVIATION", "WeaponModEngine", 0,
            4000, cfg("KEROSENE", "LPG"), "Replacement engine magazine; delayAfterFire=10."));
    public static final SednaMagazineConfig MOD_ENGINE_ELECTRIC = register(SednaMagazineConfig.electricEngine(
            "weapon_mod_engine_electric.magazine", "weapon_mod_special:ENGINE_ELECTRIC", "WeaponModEngine", 0,
            1_000_000, "Replacement electric engine magazine; delayAfterFire=15."));
    public static final SednaMagazineConfig MOD_ENGINE_TURBO = register(SednaMagazineConfig.liquidEngine(
            "weapon_mod_engine_turbo.magazine", "weapon_mod_special:ENGINE_TURBO", "WeaponModEngine", 0, 4000,
            cfg("KEROSENE_REFORM", "REFORMATE"), "Replacement engine magazine; delayAfterFire=5."));
    public static final SednaMagazineConfig MOD_LIBERATOR_SPEEDLOADER = register(full(
            "weapon_mod_liberator_speedloader.magazine", "weapon_mod_special:SPEEDLOADER",
            "WeaponModLiberatorSpeedloader", 0, 4, G12));

    static {
        registerDefaultAmmo("gun_maresleg", "G12", 12, false);
        registerDefaultAmmo("gun_maresleg_akimbo", "G12", 24, false);
        registerDefaultAmmo("gun_maresleg_broken", "G12_MAGNUM", 24, false);
        registerDefaultAmmo("gun_liberator", "G12", 12, false);
        registerDefaultAmmo("gun_spas12", "G12", 16, false);
        registerDefaultAmmo("gun_autoshotgun", "G12", 20, false);
        registerDefaultAmmo("gun_autoshotgun_shredder", "G12", 20, false);
        registerDefaultAmmo("gun_autoshotgun_sexy", "G12_MAGNUM", 50, false);
        registerDefaultAmmo("gun_double_barrel", "G10", 6, false);
        registerDefaultAmmo("gun_double_barrel_sacred_dragon", "G10_DU", 6, false);
        registerDefaultAmmo("gun_autoshotgun_heretic", "G10", 50, false);
        registerDefaultAmmo("gun_amat", "BMG50_SP", 7, false);
        registerDefaultAmmo("gun_amat_subtlety", "BMG50_JHP", 7, false);
        registerDefaultAmmo("gun_amat_penance", "BMG50_JHP", 7, false);
        registerDefaultAmmo("gun_m2", "BMG50_FMJ", 25, false);
        registerDefaultAmmo("gun_henry", "M44_SP", 14, false);
        registerDefaultAmmo("gun_henry_lincoln", "M44_JHP", 14, false);
        registerDefaultAmmo("gun_heavy_revolver", "M44_SP", 12, false);
        registerDefaultAmmo("gun_heavy_revolver_lilmac", "M44_JHP", 12, false);
        registerDefaultAmmo("gun_heavy_revolver_protege", "M44_JHP", 12, false);
        registerDefaultAmmo("gun_hangman", "M44_FMJ", 16, false);
        registerDefaultAmmo("gun_flaregun", "G26_FLARE", 3, false);
        registerDefaultAmmo("gun_congolake", "G40_HE", 8, false);
        registerDefaultAmmo("gun_mk108", "G40_HE", 50, false);
        registerDefaultAmmo("gun_light_revolver", "M357_SP", 12, false);
        registerDefaultAmmo("gun_light_revolver_atlas", "M357_JHP", 12, false);
        registerDefaultAmmo("gun_light_revolver_dani", "M357_EXPRESS", 24, false);
        registerDefaultAmmo("gun_am180", "P22_SP", 35, false);
        registerDefaultAmmo("gun_star_f", "P22_SP", 15, false);
        registerDefaultAmmo("gun_star_f_akimbo", "P22_SP", 30, false);
        registerDefaultAmmo("gun_fatman", "NUKE_STANDARD", 1, true);
        registerDefaultAmmo("gun_carbine", "R762_SP", 14, false);
        registerDefaultAmmo("gun_minigun", "R762_FMJ", 30, false);
        registerDefaultAmmo("gun_minigun_lacunae", "CAPACITOR", 15, false);
        registerDefaultAmmo("gun_minigun_dual", "R762_SP", 50, false);
        registerDefaultAmmo("gun_mas36", "R762_AP", 14, false);
        registerDefaultAmmo("gun_pepperbox", "STONE", 12, false);
        registerDefaultAmmo("gun_bolter", "B75", 15, false);
        registerDefaultAmmo("gun_charge_thrower", "CT_MORTAR", 3, false);
        registerDefaultAmmo("gun_tesla_cannon", "CAPACITOR", 15, false);
        registerDefaultAmmo("gun_laser_pistol", "CAPACITOR", 15, false);
        registerDefaultAmmo("gun_laser_pistol_pew_pew", "CAPACITOR_OVERCHARGE", 10, false);
        registerDefaultAmmo("gun_laser_pistol_morning_glory", "CAPACITOR_OVERCHARGE", 20, false);
        registerDefaultAmmo("gun_lasrifle", "CAPACITOR", 24, false);
        registerDefaultAmmo("gun_tau", "TAU_URANIUM", 15, false);
        registerDefaultAmmo("gun_coilgun", "COIL_TUNGSTEN", 5, false);
        registerDefaultAmmo("gun_g3", "R556_SP", 30, false);
        registerDefaultAmmo("gun_g3_zebra", "R556_JHP", 30, false);
        registerDefaultAmmo("gun_stg77", "R556_FMJ", 30, false);
        registerDefaultAmmo("gun_greasegun", "P9_SP", 30, false);
        registerDefaultAmmo("gun_lag", "P9_JHP", 17, false);
        registerDefaultAmmo("gun_uzi", "P9_SP", 30, false);
        registerDefaultAmmo("gun_uzi_akimbo", "P9_SP", 60, false);
        registerDefaultAmmo("gun_panzerschreck", "ROCKET_HE", 3, false);
        registerDefaultAmmo("gun_stinger", "ROCKET_HEAT", 3, false);
        registerDefaultAmmo("gun_quadro", "ROCKET_HE", 4, false);
        registerDefaultAmmo("gun_missile_launcher", "ROCKET_HEAT", 5, false);
        registerDefaultAmmo("gun_flamer", "FLAME_DIESEL", 1, false);
        registerDefaultAmmo("gun_flamer_topaz", "FLAME_DIESEL", 1, false);
        registerDefaultAmmo("gun_flamer_daybreaker", "FLAME_DIESEL", 1, false);

        registerModifier(MagazineModifier.multiplier("weapon_mod_stack_mag", ModifierType.CAPACITY_MULTIPLIER,
                owners("gun_greasegun", "gun_uzi", "gun_uzi_akimbo", "gun_aberrator", "gun_aberrator_eott"),
                3, 2, true, "WeaponModStackMag multiplies single/full reload capacity by 3/2."));
        registerModifier(MagazineModifier.multiplier("weapon_mod_las_capacitor", ModifierType.CAPACITY_MULTIPLIER,
                owners("gun_lasrifle"), 3, 2, false,
                "WeaponModLasCapacitor also multiplies base damage by 1.05 in Receiver.F_BASEDAMAGE."));
        registerModifier(MagazineModifier.replacement("weapon_mod_liberator_speedloader", ModifierType.REPLACE_MAGAZINE,
                owners("gun_liberator"), MOD_LIBERATOR_SPEEDLOADER.legacyKey(), true,
                "Replaces the Liberator single reload magazine with a shared MagazineFullReload(0, 4)."));
        registerModifier(MagazineModifier.replacement("weapon_mod_engine_diesel", ModifierType.REPLACE_MAGAZINE,
                owners("gun_drill"), MOD_ENGINE_DIESEL.legacyKey(), true, "delayAfterFire=15"));
        registerModifier(MagazineModifier.replacement("weapon_mod_engine_aviation", ModifierType.REPLACE_MAGAZINE,
                owners("gun_drill"), MOD_ENGINE_AVIATION.legacyKey(), true, "delayAfterFire=10"));
        registerModifier(MagazineModifier.replacement("weapon_mod_engine_electric", ModifierType.REPLACE_MAGAZINE,
                owners("gun_drill"), MOD_ENGINE_ELECTRIC.legacyKey(), true, "delayAfterFire=15"));
        registerModifier(MagazineModifier.replacement("weapon_mod_engine_turbo", ModifierType.REPLACE_MAGAZINE,
                owners("gun_drill"), MOD_ENGINE_TURBO.legacyKey(), true, "delayAfterFire=5"));
        registerModifier(MagazineModifier.multiplier("weapon_mod_canisters", ModifierType.CAPACITY_MULTIPLIER,
                owners("gun_drill"), 3, 1, true,
                "WeaponModCanisters triples liquid/electric engine capacity."));

        registerCaliber("caliber_p9_henry", owners("gun_henry"), 28, 10.0F, P9);
        registerCaliber("caliber_p9_star_f", owners("gun_star_f"), 12, 15.0F, P9);
        registerCaliber("caliber_p9_star_f_akimbo", owners("gun_star_f_akimbo"), 12, 15.0F, P9);
        registerCaliber("caliber_p45_henry", owners("gun_henry"), 28, 10.0F, P45);
        registerCaliber("caliber_p45_greasegun", owners("gun_greasegun"), 24, 3.0F, P45);
        registerCaliber("caliber_p45_uzi", owners("gun_uzi"), 24, 3.0F, P45);
        registerCaliber("caliber_p45_uzi_akimbo", owners("gun_uzi_akimbo"), 24, 3.0F, P45);
        registerCaliber("caliber_p45_lag", owners("gun_lag"), 15, 25.0F, P45);
        registerCaliber("caliber_p22_henry", owners("gun_henry"), 28, 10.0F, P22);
        registerCaliber("caliber_p22_uzi", owners("gun_uzi"), 40, 3.0F, P22);
        registerCaliber("caliber_p22_uzi_akimbo", owners("gun_uzi_akimbo"), 40, 3.0F, P22);
        registerCaliber("caliber_m357_henry", owners("gun_henry"), 20, 10.0F, M357_CALIBER);
        registerCaliber("caliber_m357_lag", owners("gun_lag"), 15, 25.0F, M357_CALIBER);
        registerCaliber("caliber_m44_lag", owners("gun_lag"), 13, 25.0F, M44_CALIBER);
        registerCaliber("caliber_r556_henry", owners("gun_henry"), 10, 10.0F, R556);
        registerCaliber("caliber_r556_carbine", owners("gun_carbine"), 20, 15.0F, R556);
        registerCaliber("caliber_r556_miniguns", owners("gun_minigun", "gun_minigun_dual"), 0, 6.0F, R556);
        registerCaliber("caliber_r762_henry", owners("gun_henry"), 8, 10.0F, R762);
        registerCaliber("caliber_r762_g3", owners("gun_g3"), 24, 5.0F, R762);
        registerCaliber("caliber_bmg50_henry", owners("gun_henry"), 5, 10.0F, BMG50_BELT);
        registerCaliber("caliber_bmg50_miniguns", owners("gun_minigun", "gun_minigun_dual"), 0, 6.0F,
                BMG50_BELT);
    }

    public static Optional<SednaMagazineConfig> byKey(String legacyKey) {
        return Optional.ofNullable(BY_KEY.get(legacyKey));
    }

    public static Collection<SednaMagazineConfig> byOwner(String legacyOwnerName) {
        List<SednaMagazineConfig> configs = BY_OWNER.get(legacyOwnerName);
        return configs == null ? List.of() : Collections.unmodifiableList(configs);
    }

    public static Collection<SednaMagazineConfig> all() {
        return Collections.unmodifiableCollection(BY_KEY.values());
    }

    public static Optional<DefaultAmmo> defaultAmmo(String legacyOwnerName) {
        return Optional.ofNullable(DEFAULT_AMMO_BY_OWNER.get(legacyOwnerName));
    }

    public static Collection<DefaultAmmo> allDefaultAmmo() {
        return Collections.unmodifiableCollection(DEFAULT_AMMO_BY_OWNER.values());
    }

    public static Optional<MagazineModifier> modifier(String legacyKey) {
        return Optional.ofNullable(MODIFIERS_BY_KEY.get(legacyKey));
    }

    public static Collection<MagazineModifier> allModifiers() {
        return Collections.unmodifiableCollection(MODIFIERS_BY_KEY.values());
    }

    public static List<String> missingAcceptedBulletConfigNames() {
        List<String> missing = new ArrayList<>();
        for (SednaMagazineConfig config : BY_KEY.values()) {
            missing.addAll(config.missingBulletConfigNames());
        }
        for (MagazineModifier modifier : MODIFIERS_BY_KEY.values()) {
            for (String name : modifier.acceptedBulletConfigNames()) {
                if (LegacySednaBulletConfigs.byName(name).isEmpty()) {
                    missing.add(name);
                }
            }
        }
        return List.copyOf(missing);
    }

    private static SednaMagazineConfig full(String key, String owner, String sourceClassName, int index, int capacity,
            List<String> configs) {
        return SednaMagazineConfig.fullReload(key, owner, sourceClassName, index, capacity, configs);
    }

    private static SednaMagazineConfig single(String key, String owner, String sourceClassName, int index, int capacity,
            List<String> configs) {
        return SednaMagazineConfig.singleReload(key, owner, sourceClassName, index, capacity, configs);
    }

    private static SednaMagazineConfig belt(String key, String owner, String sourceClassName, List<String> configs,
            String notes) {
        return SednaMagazineConfig.belt(key, owner, sourceClassName, configs, notes);
    }

    private static SednaMagazineConfig register(SednaMagazineConfig config) {
        BY_KEY.put(config.legacyKey(), config);
        if (!config.legacyOwnerName().isEmpty()) {
            BY_OWNER.computeIfAbsent(config.legacyOwnerName(), ignored -> new ArrayList<>()).add(config);
        }
        return config;
    }

    private static MagazineModifier registerModifier(MagazineModifier modifier) {
        MODIFIERS_BY_KEY.put(modifier.legacyKey(), modifier);
        return modifier;
    }

    private static void registerCaliber(String key, List<String> owners, int replacementCapacity,
            float replacementBaseDamage, List<String> acceptedBulletConfigNames) {
        registerModifier(MagazineModifier.caliber(key, owners, replacementCapacity, replacementBaseDamage,
                acceptedBulletConfigNames));
    }

    private static void registerDefaultAmmo(String owner, String ammoName, int amount, boolean expensive) {
        DEFAULT_AMMO_BY_OWNER.put(owner, new DefaultAmmo(owner, SednaBulletConfig.AmmoKind.STANDARD, ammoName,
                "ammo_standard", amount, expensive));
    }

    private static List<String> cfg(String... values) {
        return List.of(values);
    }

    private static List<String> owners(String... values) {
        return List.of(values);
    }

    private static List<String> prepend(String first, List<String> values) {
        List<String> result = new ArrayList<>();
        result.add(first);
        result.addAll(values);
        return List.copyOf(result);
    }

    private static List<String> append(List<String> values, String last) {
        List<String> result = new ArrayList<>(values);
        result.add(last);
        return List.copyOf(result);
    }

    public enum ModifierType {
        CAPACITY_MULTIPLIER,
        REPLACE_MAGAZINE,
        CALIBER_REPLACEMENT
    }

    public record MagazineModifier(
            String legacyKey,
            ModifierType type,
            List<String> targetOwnerNames,
            String replacementMagazineKey,
            int capacityNumerator,
            int capacityDenominator,
            int replacementCapacity,
            float replacementBaseDamage,
            List<String> acceptedBulletConfigNames,
            boolean clearsLoadedRounds,
            String notes) {

        public MagazineModifier {
            legacyKey = legacyKey == null ? "" : legacyKey;
            type = type == null ? ModifierType.CAPACITY_MULTIPLIER : type;
            targetOwnerNames = targetOwnerNames == null ? List.of() : List.copyOf(targetOwnerNames);
            replacementMagazineKey = replacementMagazineKey == null ? "" : replacementMagazineKey;
            capacityNumerator = Math.max(0, capacityNumerator);
            capacityDenominator = capacityDenominator <= 0 ? 1 : capacityDenominator;
            replacementCapacity = Math.max(0, replacementCapacity);
            acceptedBulletConfigNames = acceptedBulletConfigNames == null ? List.of()
                    : List.copyOf(acceptedBulletConfigNames);
            notes = notes == null ? "" : notes;
        }

        private static MagazineModifier multiplier(String key, ModifierType type, List<String> owners, int numerator,
                int denominator, boolean clearsLoadedRounds, String notes) {
            return new MagazineModifier(key, type, owners, "", numerator, denominator, 0, 0.0F, List.of(),
                    clearsLoadedRounds, notes);
        }

        private static MagazineModifier replacement(String key, ModifierType type, List<String> owners,
                String replacementMagazineKey, boolean clearsLoadedRounds, String notes) {
            return new MagazineModifier(key, type, owners, replacementMagazineKey, 1, 1, 0, 0.0F, List.of(),
                    clearsLoadedRounds, notes);
        }

        private static MagazineModifier caliber(String key, List<String> owners, int replacementCapacity,
                float replacementBaseDamage, List<String> configs) {
            return new MagazineModifier(key, ModifierType.CALIBER_REPLACEMENT, owners, "", 1, 1,
                    replacementCapacity, replacementBaseDamage, configs, true,
                    "WeaponModCaliber replaces accepted bullets and capacity, then clears loaded rounds.");
        }
    }

    public record DefaultAmmo(
            String legacyOwnerName,
            SednaBulletConfig.AmmoKind ammoKind,
            String ammoName,
            String legacyItemName,
            int amount,
            boolean expensiveFlag) {

        public Optional<LegacySednaAmmoCatalog.AmmoEntry> ammoEntry() {
            return LegacySednaAmmoCatalog.byName(ammoName);
        }
    }

    private LegacySednaMagazineConfigs() {
    }
}
