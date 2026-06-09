package com.hbm.ntm.bullet;

import com.hbm.ntm.damage.DamageClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class LegacySednaBulletConfigs {
    private static final Map<String, SednaBulletConfig> BY_NAME = new LinkedHashMap<>();
    private static final Map<String, List<SednaBulletConfig>> BY_AMMO = new LinkedHashMap<>();

    private static final String R_STANDARD_BULLET = "RENDER_STANDARD_BULLET";
    private static final String R_AP_BULLET = "RENDER_AP_BULLET";
    private static final String R_DU_BULLET = "RENDER_DU_BULLET";
    private static final String R_EXPRESS_BULLET = "RENDER_EXPRESS_BULLET";
    private static final String R_HE_BULLET = "RENDER_HE_BULLET";
    private static final String R_SM_BULLET = "RENDER_SM_BULLET";
    private static final String R_BLACK_BULLET = "RENDER_BLACK_BULLET";
    private static final String R_LEGENDARY_BULLET = "RENDER_LEGENDARY_BULLET";
    private static final String R_FLECHETTE_BULLET = "RENDER_FLECHETTE_BULLET";
    private static final String R_GRENADE = "RENDER_GRENADE";
    private static final String R_FLARE = "RENDER_FLARE";
    private static final String R_FLARE_SUPPLY = "RENDER_FLARE_SUPPLY";
    private static final String R_FLARE_WEAPON = "RENDER_FLARE_WEAPON";
    private static final String R_RPZB = "RENDER_RPZB";
    private static final String R_QD = "RENDER_QD";
    private static final String R_ML = "RENDER_ML";
    private static final String R_NUKE = "RENDER_NUKE";
    private static final String R_NUKE_BALEFIRE = "RENDER_NUKE_BALEFIRE";
    private static final String R_HIVE = "RENDER_HIVE";
    private static final String R_BOMB = "RENDER_BOMB";
    private static final String R_BIG_NUKE = "RENDER_BIG_NUKE";
    private static final String R_FRAGMENTATION = "RENDER_FRAGMENTATION";
    private static final String R_CT_HOOK = "RENDER_CT_HOOK";
    private static final String R_CT_MORTAR = "RENDER_CT_MORTAR";
    private static final String R_CT_MORTAR_CHARGE = "RENDER_CT_MORTAR_CHARGE";
    private static final String BR_STANDARD = "RENDER_STANDARD_BEAM";
    private static final String BR_LIGHTNING = "RENDER_LIGHTNING";
    private static final String BR_LIGHTNING_SUB = "RENDER_LIGHTNING_SUB";
    private static final String BR_TAU = "RENDER_TAU";
    private static final String BR_TAU_CHARGE = "RENDER_TAU_CHARGE";
    private static final String BR_LASER_RED = "RENDER_LASER_RED";
    private static final String BR_LASER_PURPLE = "RENDER_LASER_PURPLE";
    private static final String BR_LASER_EMERALD = "RENDER_LASER_EMERALD";
    private static final String BR_LASER_CYAN = "RENDER_LASER_CYAN";
    private static final String BR_FOLLY = "RENDER_FOLLY";
    private static final String BR_CRACKLE = "RENDER_CRACKLE";
    private static final String BR_BLACK_LIGHTNING = "RENDER_BLACK_LIGHTNING";
    private static final String BR_NI4NI_BOLT = "RENDER_NI4NI_BOLT";

    public static final SednaBulletConfig P22_SP = register(sp("p22_sp", "P22_SP", "SMALL", 24, "p22").knockback(0.0F));
    public static final SednaBulletConfig P22_FMJ = register(fmj("p22_fmj", "P22_FMJ", "SMALL", 24, "p22fmj", 1.0F));
    public static final SednaBulletConfig P22_JHP = register(jhp("p22_jhp", "P22_JHP", "SMALL", 24, "p22jhp"));
    public static final SednaBulletConfig P22_AP = register(ap("p22_ap", "P22_AP", "SMALL_STEEL", 24, "p22ap", 2.5F)
            .knockback(0.0F));

    public static final SednaBulletConfig P9_SP = register(sp("p9_sp", "P9_SP", "SMALL", 12, "p9"));
    public static final SednaBulletConfig P9_FMJ = register(fmj("p9_fmj", "P9_FMJ", "SMALL", 12, "p9fmj", 2.0F));
    public static final SednaBulletConfig P9_JHP = register(jhp("p9_jhp", "P9_JHP", "SMALL", 12, "p9jhp"));
    public static final SednaBulletConfig P9_AP = register(ap("p9_ap", "P9_AP", "SMALL_STEEL", 12, "p9ap", 5.0F));

    public static final SednaBulletConfig R556_SP = register(sp("r556_sp", "R556_SP", "SMALL", 8, "r556"));
    public static final SednaBulletConfig R556_FMJ = register(fmj("r556_fmj", "R556_FMJ", "SMALL", 8, "r556fmj", 4.0F));
    public static final SednaBulletConfig R556_JHP = register(jhp("r556_jhp", "R556_JHP", "SMALL", 8, "r556jhp"));
    public static final SednaBulletConfig R556_AP = register(ap("r556_ap", "R556_AP", "SMALL_STEEL", 8, "r556ap", 10.0F));
    public static final SednaBulletConfig R556_INC_SP = register(incendiary("r556_inc_sp", R556_SP).renderer(R_AP_BULLET));
    public static final SednaBulletConfig R556_INC_FMJ = register(incendiary("r556_inc_fmj", R556_FMJ).renderer(R_AP_BULLET));
    public static final SednaBulletConfig R556_INC_JHP = register(incendiary("r556_inc_jhp", R556_JHP).renderer(R_AP_BULLET));
    public static final SednaBulletConfig R556_INC_AP = register(incendiary("r556_inc_ap", R556_AP).renderer(R_AP_BULLET));

    public static final SednaBulletConfig R762_SP = register(sp("r762_sp", "R762_SP", "SMALL", 6, "r762"));
    public static final SednaBulletConfig R762_FMJ = register(fmj("r762_fmj", "R762_FMJ", "SMALL", 6, "r762fmj", 5.0F));
    public static final SednaBulletConfig R762_JHP = register(jhp("r762_jhp", "R762_JHP", "SMALL", 6, "r762jhp"));
    public static final SednaBulletConfig R762_AP = register(ap("r762_ap", "R762_AP", "SMALL_STEEL", 6, "r762ap", 12.5F));
    public static final SednaBulletConfig R762_DU = register(du("r762_du", "R762_DU", "SMALL_STEEL", 6, "r762du", 15.0F));
    public static final SednaBulletConfig R762_HE = register(base("r762_he", "R762_HE", "SMALL_STEEL", 6, "r762he")
            .damage(1.75F).ballistics(10.0F, 0.0F, 3.0F, 1, 1)
            .renderer(R_HE_BULLET).behavior(SednaBehaviorTag.TINY_EXPLODE));

    public static final SednaBulletConfig ENERGY_LACUNAE = register(energy("energy_lacunae", "CAPACITOR",
            DamageClass.LASER, false));
    public static final SednaBulletConfig ENERGY_LACUNAE_OVERCHARGE = register(energy("energy_lacunae_overcharge",
            "CAPACITOR_OVERCHARGE", DamageClass.LASER, true));
    public static final SednaBulletConfig ENERGY_LACUNAE_IR = register(energy("energy_lacunae_ir",
            "CAPACITOR_IR", DamageClass.FIRE, false));

    public static final SednaBulletConfig M44_BP = register(base("m44_bp", "M44_BP", "SMALL", 12, "m44bp")
            .damage(0.75F).blackPowder(true));
    public static final SednaBulletConfig M44_SP = register(sp("m44_sp", "M44_SP", "SMALL", 6, "m44"));
    public static final SednaBulletConfig M44_FMJ = register(fmj("m44_fmj", "M44_FMJ", "SMALL", 6, "m44fmj", 3.0F));
    public static final SednaBulletConfig M44_JHP = register(jhp("m44_jhp", "M44_JHP", "SMALL", 6, "m44jhp"));
    public static final SednaBulletConfig M44_AP = register(ap("m44_ap", "M44_AP", "SMALL_STEEL", 6, "m44ap", 7.5F));
    public static final SednaBulletConfig M44_EXPRESS = register(base("m44_express", "M44_EXPRESS", "SMALL", 6, "m44express")
            .damage(1.5F).armor(3.0F, 0.1F).penetration(true).ballistics(10.0F, 0.0F, 1.5F, 1, 1)
            .renderer(R_EXPRESS_BULLET));
    public static final SednaBulletConfig M44_EQUESTRIAN_PIP = register(secret("m44_equestrian_pip", "M44_EQUESTRIAN",
            "m44equestrianPip").damage(0.0F).behavior(SednaBehaviorTag.BOXCAR_IMPACT));
    public static final SednaBulletConfig M44_EQUESTRIAN_MN7 = register(secret("m44_equestrian_mn7", "M44_EQUESTRIAN",
            "m44equestrianMn7").damage(0.0F).behavior(SednaBehaviorTag.TORPEDO_IMPACT));

    public static final SednaBulletConfig G12_BP = register(shotgun("g12_bp", "G12_BP", "SHOTSHELL", 12,
            "12GA_BP", 8, 0.75F / 8.0F, 0.035F, 15.0F).blackPowder(true));
    public static final SednaBulletConfig G12_BP_MAGNUM = register(shotgun("g12_bp_magnum", "G12_BP_MAGNUM",
            "SHOTSHELL", 12, "12GA_BP_MAGNUM", 4, 0.75F / 4.0F, 0.035F, 25.0F).blackPowder(true));
    public static final SednaBulletConfig G12_BP_SLUG = register(base("g12_bp_slug", "G12_BP_SLUG", "SHOTSHELL",
            12, "12GA_BP_SLUG").damage(0.75F).ballistics(10.0F, 0.01F, 1.0F, 1, 1)
            .ricochet(5.0F, 2).blackPowder(true));
    public static final SednaBulletConfig G12 = register(shotgun("g12", "G12", "BUCKSHOT", 6,
            "12GA", 8, 1.0F / 8.0F, 0.035F, 15.0F).armor(2.0F, 0.0F));
    public static final SednaBulletConfig G12_SLUG = register(base("g12_slug", "G12_SLUG", "BUCKSHOT", 6,
            "12GA_SLUG").headshot(1.5F).ballistics(10.0F, 0.0F, 1.0F, 1, 1)
            .ricochet(25.0F, 2).armor(4.0F, 0.15F));
    public static final SednaBulletConfig G12_FLECHETTE = register(shotgun("g12_flechette", "G12_FLECHETTE",
            "BUCKSHOT", 6, "12GA_FLECHETTE", 8, 1.0F / 8.0F, 0.025F, 5.0F).armor(5.0F, 0.2F)
            .renderer(R_FLECHETTE_BULLET));
    public static final SednaBulletConfig G12_MAGNUM = register(shotgun("g12_magnum", "G12_MAGNUM",
            "BUCKSHOT_ADVANCED", 6, "12GA_MAGNUM", 4, 2.0F / 4.0F, 0.015F, 15.0F).armor(4.0F, 0.0F));
    public static final SednaBulletConfig G12_EXPLOSIVE = register(base("g12_explosive", "G12_EXPLOSIVE",
            "BUCKSHOT_ADVANCED", 6, "12GA_EXPLOSIVE").damage(2.5F).spread(0.0F)
            .ricochet(15.0F, 2).renderer(R_EXPRESS_BULLET).behavior(SednaBehaviorTag.STANDARD_EXPLODE));
    public static final SednaBulletConfig G12_PHOSPHORUS = register(shotgun("g12_phosphorus", "G12_PHOSPHORUS",
            "BUCKSHOT_ADVANCED", 6, "12GA_PHOSPHORUS", 8, 1.0F / 8.0F, 0.015F, 15.0F)
            .renderer(R_AP_BULLET).behavior(SednaBehaviorTag.INCENDIARY_PHOSPHORUS));
    public static final SednaBulletConfig G12_EQUESTRIAN_BJ = register(secret("g12_equestrian_bj",
            "G12_EQUESTRIAN", "12gaEquestrianBJ").damage(0.0F).behavior(SednaBehaviorTag.BOAT_IMPACT));
    public static final SednaBulletConfig G12_EQUESTRIAN_TKR = register(secret("g12_equestrian_tkr",
            "G12_EQUESTRIAN", "12gaEquestrianTKR").damage(0.0F));

    public static final SednaBulletConfig G10 = register(shotgun("g10", "G10", "BUCKSHOT_ADVANCED", 4,
            "10GA", 10, 1.0F / 10.0F, 0.035F, 15.0F).armor(5.0F, 0.0F));
    public static final SednaBulletConfig G10_SHRAPNEL = register(shotgun("g10_shrapnel", "G10_SHRAPNEL",
            "BUCKSHOT_ADVANCED", 4, "10GAShrapnel", 10, 1.0F / 10.0F, 0.035F, 90.0F)
            .armor(5.0F, 0.0F).ricochet(90.0F, 15));
    public static final SednaBulletConfig G10_DU = register(shotgun("g10_du", "G10_DU", "BUCKSHOT_ADVANCED", 4,
            "10GADU", 10, 1.0F / 4.0F, 0.035F, 15.0F).armor(10.0F, 0.2F)
            .penetration(true).damageFalloffByPenetration(false).renderer(R_DU_BULLET));
    public static final SednaBulletConfig G10_SLUG = register(base("g10_slug", "G10_SLUG", "BUCKSHOT_ADVANCED", 4,
            "10GASlug").ricochet(15.0F, 2).armor(10.0F, 0.1F).penetration(true));
    public static final SednaBulletConfig G10_EXPLOSIVE = register(shotgun("g10_explosive", "G10_EXPLOSIVE",
            "BUCKSHOT_ADVANCED", 4, "10GAEXP", 10, 1.0F / 4.0F, 0.035F, 5.0F)
            .ballistics(10.0F, 0.035F, 3.0F, 10, 10).renderer(R_HE_BULLET)
            .behavior(SednaBehaviorTag.TINY_EXPLODE));

    public static final SednaBulletConfig M357_BP = register(base("m357_bp", "M357_BP", "SMALL", 16, "")
            .damage(0.75F).blackPowder(true));
    public static final SednaBulletConfig M357_SP = register(sp("m357_sp", "M357_SP", "SMALL", 8, ""));
    public static final SednaBulletConfig M357_FMJ = register(fmj("m357_fmj", "M357_FMJ", "SMALL", 8, "", 2.0F));
    public static final SednaBulletConfig M357_JHP = register(jhp("m357_jhp", "M357_JHP", "SMALL", 8, ""));
    public static final SednaBulletConfig M357_AP = register(ap("m357_ap", "M357_AP", "SMALL_STEEL", 8, "", 5.0F));
    public static final SednaBulletConfig M357_EXPRESS = register(base("m357_express", "M357_EXPRESS",
            "SMALL", 8, "").damage(1.5F).armor(2.0F, 0.1F).penetration(true).wear(1.5F)
            .renderer(R_EXPRESS_BULLET));

    public static final SednaBulletConfig P45_SP = register(sp("p45_sp", "P45_SP", "SMALL", 8, "p45"));
    public static final SednaBulletConfig P45_FMJ = register(fmj("p45_fmj", "P45_FMJ", "SMALL", 8,
            "p45fmj", 2.0F));
    public static final SednaBulletConfig P45_JHP = register(jhp("p45_jhp", "P45_JHP", "SMALL", 8, "p45jhp"));
    public static final SednaBulletConfig P45_AP = register(ap("p45_ap", "P45_AP", "SMALL_STEEL", 8,
            "p45ap", 5.0F));
    public static final SednaBulletConfig P45_DU = register(du("p45_du", "P45_DU", "SMALL_STEEL", 8,
            "p45du", 15.0F).damage(2.5F));

    public static final SednaBulletConfig BMG50_SP = register(base("bmg50_sp", "BMG50_SP", "LARGE", 12,
            "bmg50"));
    public static final SednaBulletConfig BMG50_FMJ = register(fmj("bmg50_fmj", "BMG50_FMJ", "LARGE", 12,
            "bmg50fmj", 7.0F));
    public static final SednaBulletConfig BMG50_JHP = register(jhp("bmg50_jhp", "BMG50_JHP", "LARGE", 12,
            "bmg50jhp"));
    public static final SednaBulletConfig BMG50_AP = register(ap("bmg50_ap", "BMG50_AP", "LARGE_STEEL", 12,
            "bmg50ap", 17.5F));
    public static final SednaBulletConfig BMG50_DU = register(du("bmg50_du", "BMG50_DU", "LARGE_STEEL", 12,
            "bmg50du", 21.0F));
    public static final SednaBulletConfig BMG50_HE = register(base("bmg50_he", "BMG50_HE", "LARGE_STEEL",
            12, "bmg50he").wear(3.0F).damage(1.75F).renderer(R_HE_BULLET)
            .behavior(SednaBehaviorTag.TINY_EXPLODE));
    public static final SednaBulletConfig BMG50_SM = register(base("bmg50_sm", "BMG50_SM", "LARGE_STEEL",
            6, "bmg50sm").wear(10.0F).penetration(true).damageFalloffByPenetration(false)
            .damage(2.5F).armor(30.0F, 0.35F).renderer(R_SM_BULLET));
    public static final SednaBulletConfig BMG50_BLACK = register(secret("bmg50_black", "BMG50_BLACK",
            "bmg50black").wear(5.0F).penetration(true).damageFalloffByPenetration(false).spectral(true)
            .damage(1.5F).headshot(3.0F).armor(30.0F, 0.35F).renderer(R_BLACK_BULLET));
    public static final SednaBulletConfig BMG50_EQUESTRIAN = register(secret("bmg50_equestrian",
            "BMG50_EQUESTRIAN", "bmg50equestrian").damage(0.0F).behavior(SednaBehaviorTag.BUILDING_IMPACT));

    public static final SednaBulletConfig B75 = register(standard("b75", "B75", "b75")
            .renderer(R_AP_BULLET).behavior(SednaBehaviorTag.TINY_EXPLODE));
    public static final SednaBulletConfig B75_INC = register(standard("b75_inc", "B75_INC", "b75inc")
            .damage(0.8F).armor(0.0F, 0.1F).renderer(R_AP_BULLET)
            .behavior(SednaBehaviorTag.INCENDIARY_PHOSPHORUS));
    public static final SednaBulletConfig B75_EXP = register(standard("b75_exp", "B75_EXP", "b75exp")
            .damage(1.5F).armor(0.0F, -0.25F).renderer(R_EXPRESS_BULLET)
            .behavior(SednaBehaviorTag.STANDARD_EXPLODE));

    public static final SednaBulletConfig G26_FLARE = register(flare("g26_flare", "G26_FLARE",
            "g26Flare").renderer(R_FLARE).behavior(SednaBehaviorTag.ENTITY_IGNITE));
    public static final SednaBulletConfig G26_FLARE_SUPPLY = register(flare("g26_flare_supply",
            "G26_FLARE_SUPPLY", "g26FlareSupply").renderer(R_FLARE_SUPPLY).behavior(SednaBehaviorTag.ENTITY_IGNITE)
            .behavior(SednaBehaviorTag.AIRDROP_SUPPLIES));
    public static final SednaBulletConfig G26_FLARE_WEAPON = register(flare("g26_flare_weapon",
            "G26_FLARE_WEAPON", "g26FlareWeapon").renderer(R_FLARE_WEAPON).behavior(SednaBehaviorTag.ENTITY_IGNITE)
            .behavior(SednaBehaviorTag.AIRDROP_WEAPONS));

    public static final SednaBulletConfig G40_HE = register(grenade40("g40_he", "G40_HE", "g40")
            .behavior(SednaBehaviorTag.STANDARD_EXPLODE));
    public static final SednaBulletConfig G40_HEAT = register(grenade40("g40_heat", "G40_HEAT", "g40heat")
            .damage(0.5F).behavior(SednaBehaviorTag.HEAT_EXPLODE));
    public static final SednaBulletConfig G40_DEMO = register(grenade40("g40_demo", "G40_DEMO", "g40demo")
            .damage(0.75F).behavior(SednaBehaviorTag.DEMO_EXPLODE));
    public static final SednaBulletConfig G40_INC = register(grenade40("g40_inc", "G40_INC", "g40inc")
            .damage(0.75F).behavior(SednaBehaviorTag.INCENDIARY_EXPLODE));
    public static final SednaBulletConfig G40_PHOSPHORUS = register(grenade40("g40_phosphorus",
            "G40_PHOSPHORUS", "g40phos").damage(0.75F).behavior(SednaBehaviorTag.PHOSPHORUS_EXPLODE));

    public static final SednaBulletConfig ROCKET_HE = register(rocket("rocket_he", "ROCKET_HE")
            .behavior(SednaBehaviorTag.STANDARD_EXPLODE));
    public static final SednaBulletConfig ROCKET_HEAT = register(rocket("rocket_heat", "ROCKET_HEAT")
            .damage(0.5F).behavior(SednaBehaviorTag.HEAT_EXPLODE));
    public static final SednaBulletConfig ROCKET_DEMO = register(rocket("rocket_demo", "ROCKET_DEMO")
            .damage(0.75F).behavior(SednaBehaviorTag.DEMO_EXPLODE));
    public static final SednaBulletConfig ROCKET_INC = register(rocket("rocket_inc", "ROCKET_INC")
            .damage(0.75F).behavior(SednaBehaviorTag.INCENDIARY_EXPLODE));
    public static final SednaBulletConfig ROCKET_PHOSPHORUS = register(rocket("rocket_phosphorus",
            "ROCKET_PHOSPHORUS").damage(0.75F).behavior(SednaBehaviorTag.PHOSPHORUS_EXPLODE));
    public static final SednaBulletConfig ROCKET_RPZB_HE = register(rocketClone("rocket_rpzb_he", ROCKET_HE,
            R_RPZB, false));
    public static final SednaBulletConfig ROCKET_RPZB_HEAT = register(rocketClone("rocket_rpzb_heat", ROCKET_HEAT,
            R_RPZB, false));
    public static final SednaBulletConfig ROCKET_RPZB_DEMO = register(rocketClone("rocket_rpzb_demo", ROCKET_DEMO,
            R_RPZB, false));
    public static final SednaBulletConfig ROCKET_RPZB_INC = register(rocketClone("rocket_rpzb_inc", ROCKET_INC,
            R_RPZB, false));
    public static final SednaBulletConfig ROCKET_RPZB_PHOSPHORUS = register(rocketClone("rocket_rpzb_phosphorus",
            ROCKET_PHOSPHORUS, R_RPZB, false));
    public static final SednaBulletConfig ROCKET_QD_HE = register(rocketClone("rocket_qd_he", ROCKET_HE, R_QD, true));
    public static final SednaBulletConfig ROCKET_QD_HEAT = register(rocketClone("rocket_qd_heat", ROCKET_HEAT,
            R_QD, true));
    public static final SednaBulletConfig ROCKET_QD_DEMO = register(rocketClone("rocket_qd_demo", ROCKET_DEMO,
            R_QD, true));
    public static final SednaBulletConfig ROCKET_QD_INC = register(rocketClone("rocket_qd_inc", ROCKET_INC,
            R_QD, true));
    public static final SednaBulletConfig ROCKET_QD_PHOSPHORUS = register(rocketClone("rocket_qd_phosphorus",
            ROCKET_PHOSPHORUS, R_QD, true));
    public static final SednaBulletConfig ROCKET_ML_HE = register(rocketClone("rocket_ml_he", ROCKET_HE, R_ML,
            false));
    public static final SednaBulletConfig ROCKET_ML_HEAT = register(rocketClone("rocket_ml_heat", ROCKET_HEAT,
            R_ML, false));
    public static final SednaBulletConfig ROCKET_ML_DEMO = register(rocketClone("rocket_ml_demo", ROCKET_DEMO,
            R_ML, false));
    public static final SednaBulletConfig ROCKET_ML_INC = register(rocketClone("rocket_ml_inc", ROCKET_INC,
            R_ML, false));
    public static final SednaBulletConfig ROCKET_ML_PHOSPHORUS = register(rocketClone("rocket_ml_phosphorus",
            ROCKET_PHOSPHORUS, R_ML, false));
    public static final SednaBulletConfig ROCKET_NCRPA_STEER_HE = register(rocketClone("rocket_ncrpa_steer_he",
            ROCKET_HE, R_RPZB, true));
    public static final SednaBulletConfig ROCKET_NCRPA_STEER_HEAT = register(rocketClone("rocket_ncrpa_steer_heat",
            ROCKET_HEAT, R_RPZB, true));
    public static final SednaBulletConfig ROCKET_NCRPA_STEER_DEMO = register(rocketClone("rocket_ncrpa_steer_demo",
            ROCKET_DEMO, R_RPZB, true));
    public static final SednaBulletConfig ROCKET_NCRPA_STEER_INC = register(rocketClone("rocket_ncrpa_steer_inc",
            ROCKET_INC, R_RPZB, true));
    public static final SednaBulletConfig ROCKET_NCRPA_STEER_PHOSPHORUS = register(rocketClone(
            "rocket_ncrpa_steer_phosphorus", ROCKET_PHOSPHORUS, R_RPZB, true));
    public static final SednaBulletConfig ROCKET_NCRPA_HE = register(rocketClone("rocket_ncrpa_he", ROCKET_HE,
            R_RPZB, false));
    public static final SednaBulletConfig ROCKET_NCRPA_HEAT = register(rocketClone("rocket_ncrpa_heat", ROCKET_HEAT,
            R_RPZB, false));
    public static final SednaBulletConfig ROCKET_NCRPA_DEMO = register(rocketClone("rocket_ncrpa_demo", ROCKET_DEMO,
            R_RPZB, false));
    public static final SednaBulletConfig ROCKET_NCRPA_INC = register(rocketClone("rocket_ncrpa_inc", ROCKET_INC,
            R_RPZB, false));
    public static final SednaBulletConfig ROCKET_NCRPA_PHOSPHORUS = register(rocketClone("rocket_ncrpa_phosphorus",
            ROCKET_PHOSPHORUS, R_RPZB, false));

    public static final SednaBulletConfig FLAME_DIESEL = register(flame("flame_diesel", "FLAME_DIESEL",
            1.0F, 0.0F, 1, 100, 0.02D, SednaBehaviorTag.FLAME_LINGER_DIESEL));
    public static final SednaBulletConfig FLAME_GAS = register(flame("flame_gas", "FLAME_GAS",
            1.0F, 0.05F, 1, 10, 0.0D, SednaBehaviorTag.FLAME_LINGER_GAS));
    public static final SednaBulletConfig FLAME_NAPALM = register(flame("flame_napalm", "FLAME_NAPALM",
            1.0F, 0.0F, 1, 200, 0.02D, SednaBehaviorTag.FLAME_LINGER_NAPALM));
    public static final SednaBulletConfig FLAME_BALEFIRE = register(flame("flame_balefire", "FLAME_BALEFIRE",
            1.0F, 0.0F, 1, 200, 0.02D, SednaBehaviorTag.FLAME_LINGER_BALEFIRE)
            .clearBehavior(SednaBehaviorTag.FLAME_VISUAL).behavior(SednaBehaviorTag.BALEFIRE_VISUAL));
    public static final SednaBulletConfig FLAME_NOGRAV = register(FLAME_DIESEL.toBuilder("flame_nograv")
            .physics(0.0D, 100));
    public static final SednaBulletConfig FLAME_NOGRAV_BF = register(FLAME_BALEFIRE.toBuilder("flame_nograv_bf")
            .physics(0.0D, 100));
    public static final SednaBulletConfig FLAME_TOPAZ_DIESEL = register(FLAME_DIESEL.toBuilder("flame_topaz_diesel")
            .projectiles(2).spread(0.05F).physics(0.0D, 60));
    public static final SednaBulletConfig FLAME_TOPAZ_GAS = register(FLAME_GAS.toBuilder("flame_topaz_gas")
            .projectiles(2).spread(0.05F));
    public static final SednaBulletConfig FLAME_TOPAZ_NAPALM = register(FLAME_NAPALM.toBuilder("flame_topaz_napalm")
            .projectiles(2).spread(0.05F).physics(0.0D, 60));
    public static final SednaBulletConfig FLAME_TOPAZ_BALEFIRE = register(FLAME_BALEFIRE.toBuilder("flame_topaz_balefire")
            .projectiles(2).spread(0.05F).physics(0.0D, 60));
    public static final SednaBulletConfig FLAME_DAYBREAKER_DIESEL = register(FLAME_DIESEL.toBuilder(
            "flame_daybreaker_diesel").ballistics(2.0F, 0.0F, 1.0F, 1, 1).physics(0.035D, 200)
            .behavior(SednaBehaviorTag.STANDARD_EXPLODE));
    public static final SednaBulletConfig FLAME_DAYBREAKER_GAS = register(FLAME_GAS.toBuilder(
            "flame_daybreaker_gas").ballistics(2.0F, 0.05F, 1.0F, 1, 1).physics(0.035D, 200)
            .behavior(SednaBehaviorTag.STANDARD_EXPLODE));
    public static final SednaBulletConfig FLAME_DAYBREAKER_NAPALM = register(FLAME_NAPALM.toBuilder(
            "flame_daybreaker_napalm").ballistics(2.0F, 0.0F, 1.0F, 1, 1).physics(0.035D, 200)
            .behavior(SednaBehaviorTag.STANDARD_EXPLODE));
    public static final SednaBulletConfig FLAME_DAYBREAKER_BALEFIRE = register(FLAME_BALEFIRE.toBuilder(
            "flame_daybreaker_balefire").ballistics(2.0F, 0.0F, 1.0F, 1, 1).physics(0.035D, 200)
            .behavior(SednaBehaviorTag.STANDARD_EXPLODE));

    public static final SednaBulletConfig ENERGY_TESLA = register(energyBeam("energy_tesla", "CAPACITOR",
            DamageClass.ELECTRIC, true).beamRenderer(BR_LIGHTNING).behavior(SednaBehaviorTag.LIGHTNING_BEAM_HIT));
    public static final SednaBulletConfig ENERGY_TESLA_OVERCHARGE = register(energyBeam("energy_tesla_overcharge",
            "CAPACITOR_OVERCHARGE", DamageClass.ELECTRIC, true).damage(1.5F)
            .beamRenderer(BR_LIGHTNING).behavior(SednaBehaviorTag.LIGHTNING_BEAM_HIT));
    public static final SednaBulletConfig ENERGY_TESLA_IR = register(energyBeam("energy_tesla_ir", "CAPACITOR_IR",
            DamageClass.ELECTRIC, false).damage(0.8F).beamRenderer(BR_LIGHTNING)
            .behavior(SednaBehaviorTag.LIGHTNING_BEAM_SPLIT));
    public static final SednaBulletConfig ENERGY_TESLA_IR_SUB = register(SednaBulletConfig.builder(
            "energy_tesla_ir_sub").standardAmmo("CAPACITOR_IR").beam().damageClass(DamageClass.ELECTRIC)
            .spread(0.0F).wear(3.0F).physics(0.0D, 3).renderRotations(false).penetration(true)
            .damage(0.5F).beamRenderer(BR_LIGHTNING_SUB).behavior(SednaBehaviorTag.STANDARD_BEAM_HIT));
    public static final SednaBulletConfig BATTERY_SOCKET_DISCHARGE = register(SednaBulletConfig.builder(
            "battery_socket_discharge").itemAmmo("TileEntityBatterySocket.discharge").beam()
            .damageClass(DamageClass.ELECTRIC).spread(0.0F).physics(0.0D, 3).renderRotations(false)
            .armor(20.0F, 0.5F).penetration(true).beamRenderer(BR_LIGHTNING_SUB)
            .behavior(SednaBehaviorTag.BATTERY_SOCKET_DISCHARGE_BEAM));
    public static final SednaBulletConfig ENERGY_LAS = register(energyBeam("energy_las", "CAPACITOR",
            DamageClass.LASER, false).beamRenderer(BR_LASER_RED).behavior(SednaBehaviorTag.STANDARD_BEAM_HIT));
    public static final SednaBulletConfig ENERGY_LAS_OVERCHARGE = register(energyBeam("energy_las_overcharge",
            "CAPACITOR_OVERCHARGE", DamageClass.LASER, true).beamRenderer(BR_LASER_RED)
            .behavior(SednaBehaviorTag.STANDARD_BEAM_HIT));
    public static final SednaBulletConfig ENERGY_LAS_IR = register(energyBeam("energy_las_ir", "CAPACITOR_IR",
            DamageClass.FIRE, false).beamRenderer(BR_LASER_RED).behavior(SednaBehaviorTag.INFRARED_BEAM_HIT));
    public static final SednaBulletConfig ENERGY_EMERALD = register(ENERGY_LAS.toBuilder("energy_emerald")
            .armor(10.0F, 0.5F).beamRenderer(BR_LASER_EMERALD));
    public static final SednaBulletConfig ENERGY_EMERALD_OVERCHARGE = register(ENERGY_LAS_OVERCHARGE.toBuilder(
            "energy_emerald_overcharge").armor(15.0F, 0.5F).beamRenderer(BR_LASER_EMERALD));
    public static final SednaBulletConfig ENERGY_EMERALD_IR = register(ENERGY_LAS_IR.toBuilder("energy_emerald_ir")
            .armor(10.0F, 0.5F).beamRenderer(BR_LASER_EMERALD));

    public static final SednaBulletConfig TAU_URANIUM = register(SednaBulletConfig.builder("tau_uranium")
            .standardAmmo("TAU_URANIUM").casingItem("plate_lead", 2, 16).beam().damageClass(DamageClass.SUBATOMIC)
            .physics(0.0D, 5).renderRotations(false).penetration(true).damageFalloffByPenetration(false)
            .beamRenderer(BR_TAU).behavior(SednaBehaviorTag.BEAM_HIT));
    public static final SednaBulletConfig TAU_URANIUM_CHARGE = register(TAU_URANIUM.toBuilder(
            "tau_uranium_charge").spectral(true).beamRenderer(BR_TAU_CHARGE));
    public static final SednaBulletConfig COIL_TUNGSTEN = register(SednaBulletConfig.builder("coil_tungsten")
            .standardAmmo("COIL_TUNGSTEN").ballistics(7.5F, 0.0F, 1.0F, 1, 1).physics(0.0D, 50)
            .penetration(true).damageFalloffByPenetration(false).spectral(true)
            .renderer(R_AP_BULLET).behavior(SednaBehaviorTag.COIL_BREAK_WEAK_BLOCKS));
    public static final SednaBulletConfig COIL_FERROURANIUM = register(SednaBulletConfig.builder("coil_ferrouranium")
            .standardAmmo("COIL_FERROURANIUM").ballistics(7.5F, 0.0F, 1.0F, 1, 1).physics(0.0D, 50)
            .penetration(true).damageFalloffByPenetration(false).spectral(true)
            .renderer(R_AP_BULLET).behavior(SednaBehaviorTag.COIL_BREAK_STRONGER_BLOCKS));
    public static final SednaBulletConfig NI4NI_ARC = register(SednaBulletConfig.builder("ni4ni_arc")
            .beam().damageClass(DamageClass.PHYSICAL).physics(0.0D, 5).armor(10.0F, 0.2F)
            .renderRotations(false).penetration(false).beamRenderer(BR_NI4NI_BOLT)
            .behavior(SednaBehaviorTag.BEAM_HIT));

    public static final SednaBulletConfig NUKE_STANDARD = register(miniNuke("nuke_standard", "NUKE_STANDARD",
            3.0F, 0.0F, 1, 1.0F).renderer(R_NUKE).behavior(SednaBehaviorTag.MINI_NUKE_STANDARD));
    public static final SednaBulletConfig NUKE_DEMO = register(miniNuke("nuke_demo", "NUKE_DEMO",
            3.0F, 0.0F, 1, 1.0F).renderer(R_NUKE).behavior(SednaBehaviorTag.MINI_NUKE_DEMO));
    public static final SednaBulletConfig NUKE_HIGH = register(miniNuke("nuke_high", "NUKE_HIGH",
            3.0F, 0.0F, 1, 1.0F).renderer(R_NUKE).behavior(SednaBehaviorTag.MINI_NUKE_HIGH));
    public static final SednaBulletConfig NUKE_TOTS = register(miniNuke("nuke_tots", "NUKE_TOTS",
            3.0F, 0.1F, 8, 0.35F).renderer(R_GRENADE).behavior(SednaBehaviorTag.MINI_NUKE_TINYTOT));
    public static final SednaBulletConfig NUKE_HIVE = register(miniNuke("nuke_hive", "NUKE_HIVE",
            1.0F, 0.15F, 12, 0.25F).renderer(R_HIVE).behavior(SednaBehaviorTag.MINI_NUKE_HIVE));
    public static final SednaBulletConfig NUKE_BALEFIRE = register(miniNuke("nuke_balefire", "NUKE_BALEFIRE",
            3.0F, 0.0F, 1, 2.5F).renderer(R_NUKE_BALEFIRE).behavior(SednaBehaviorTag.MINI_NUKE_BALEFIRE));
    public static final SednaBulletConfig CLUSTER_SUBMUNITION = register(SednaBulletConfig.builder(
            "cluster_submunition").physics(0.025D, 1_200).renderer(R_BOMB)
            .behavior(SednaBehaviorTag.DEMO_EXPLODE));

    public static final SednaBulletConfig FOLLY_SM = register(SednaBulletConfig.builder("folly_sm")
            .secretAmmo("FOLLY_SM").beam().damageClass(DamageClass.SUBATOMIC).ballistics(2.0F, 0.0F, 1.0F, 1, 1)
            .physics(0.015D, 100).renderRotations(false).spectral(true).penetration(true)
            .beamRenderer(BR_FOLLY).behavior(SednaBehaviorTag.FOLLY_SUPERMATTER_BEAM));
    public static final SednaBulletConfig FOLLY_NUKE = register(SednaBulletConfig.builder("folly_nuke")
            .secretAmmo("FOLLY_NUKE").chunkloadingBullet().ballistics(4.0F, 0.0F, 1.0F, 1, 1)
            .physics(0.015D, 600).renderer(R_BIG_NUKE).behavior(SednaBehaviorTag.FOLLY_NUKE_IMPACT));

    public static final SednaBulletConfig DEBUG = register(SednaBulletConfig.builder("ammo_debug")
            .itemAmmo("ammo_debug").spread(0.01F).ricochet(45.0F, 2).spentCasing("DEBUG0")
            .renderer(R_STANDARD_BULLET));
    public static final SednaBulletConfig DEBUG_SHOT = register(SednaBulletConfig.builder("ammo_debug_shot")
            .itemAmmo("ammo_debug").spread(0.05F).projectiles(6).ricochet(45.0F, 2).spentCasing("DEBUG1")
            .renderer(R_STANDARD_BULLET));
    public static final SednaBulletConfig GRENADE_FRAGMENTATION = register(SednaBulletConfig.builder(
            "grenade_fragmentation").itemAmmo("ItemGrenadeFilling.fragmentation").physics(0.0D, 3)
            .armor(5.0F, 0.0F).ricochet(90.0F, 2).renderer(R_FRAGMENTATION)
            .behavior(SednaBehaviorTag.GRENADE_FRAGMENTATION));
    public static final SednaBulletConfig GRENADE_PELLETS = register(SednaBulletConfig.builder("grenade_pellets")
            .itemAmmo("ItemGrenadeFilling.pellets").ballistics(1.5F, 0.0F, 1.0F, 1, 1).physics(0.04D, 100)
            .renderer(R_FRAGMENTATION).behavior(SednaBehaviorTag.TINY_EXPLODE)
            .behavior(SednaBehaviorTag.GRENADE_TINY_EXPLOSIVE_PELLET));
    public static final SednaBulletConfig GRENADE_PELLETS_HEAVY = register(SednaBulletConfig.builder(
            "grenade_pellets_heavy").itemAmmo("ItemGrenadeFilling.pellets_heavy")
            .ballistics(1.5F, 0.0F, 1.0F, 1, 1).physics(0.04D, 100).renderer(R_FRAGMENTATION)
            .behavior(SednaBehaviorTag.STANDARD_EXPLODE)
            .behavior(SednaBehaviorTag.GRENADE_HEAVY_EXPLOSIVE_PELLET));
    public static final SednaBulletConfig GRENADE_LASER = register(SednaBulletConfig.builder("grenade_laser")
            .itemAmmo("ItemGrenadeFilling.laser").beam().damageClass(DamageClass.LASER).physics(0.0D, 3)
            .renderRotations(false).armor(10.0F, 0.0F).beamRenderer(BR_LASER_RED)
            .behavior(SednaBehaviorTag.STANDARD_BEAM_HIT)
            .behavior(SednaBehaviorTag.GRENADE_LASER_BEAM_HIT));

    public static final SednaBulletConfig STONE = register(SednaBulletConfig.builder("stone")
            .standardAmmo("STONE").blackPowder(true).headshot(1.0F).spread(0.025F).ricochet(15.0F, 2)
            .renderer(R_STANDARD_BULLET));
    public static final SednaBulletConfig FLINT = register(SednaBulletConfig.builder("flint")
            .standardAmmo("STONE_AP").blackPowder(true).headshot(1.0F).spread(0.01F).ricochet(5.0F, 2)
            .penetration(true).damage(1.5F).renderer(R_STANDARD_BULLET));
    public static final SednaBulletConfig IRON = register(SednaBulletConfig.builder("iron")
            .standardAmmo("STONE_IRON").blackPowder(true).headshot(1.0F).spread(0.0F).ricochet(90.0F, 5)
            .penetration(true).damageFalloffByPenetration(false).damage(1.5F).renderer(R_STANDARD_BULLET));
    public static final SednaBulletConfig SHOT = register(SednaBulletConfig.builder("shot")
            .standardAmmo("STONE_SHOT").blackPowder(true).headshot(1.0F).spread(0.1F).ricochet(45.0F, 2)
            .projectiles(6).damage(1.0F / 6.0F).renderer(R_STANDARD_BULLET));

    public static final SednaBulletConfig G12_SUB = register(shredderSubmunition("g12_sub", G12));
    public static final SednaBulletConfig G12_SUB_SLUG = register(shredderSubmunition("g12_sub_slug", G12_SLUG));
    public static final SednaBulletConfig G12_SUB_FLECHETTE = register(shredderSubmunition("g12_sub_flechette",
            G12_FLECHETTE));
    public static final SednaBulletConfig G12_SUB_MAGNUM = register(shredderSubmunition("g12_sub_magnum",
            G12_MAGNUM));
    public static final SednaBulletConfig G12_SUB_EXPLOSIVE = register(shredderSubmunition("g12_sub_explosive",
            G12_EXPLOSIVE));
    public static final SednaBulletConfig G12_SUB_PHOSPHORUS = register(shredderSubmunition("g12_sub_phosphorus",
            G12_PHOSPHORUS));
    public static final SednaBulletConfig G12_SHREDDER = register(shredderBeam("g12_shredder", G12));
    public static final SednaBulletConfig G12_SHREDDER_SLUG = register(shredderBeam("g12_shredder_slug", G12_SLUG));
    public static final SednaBulletConfig G12_SHREDDER_FLECHETTE = register(shredderBeam("g12_shredder_flechette",
            G12_FLECHETTE));
    public static final SednaBulletConfig G12_SHREDDER_MAGNUM = register(shredderBeam("g12_shredder_magnum",
            G12_MAGNUM));
    public static final SednaBulletConfig G12_SHREDDER_EXPLOSIVE = register(shredderBeam("g12_shredder_explosive",
            G12_EXPLOSIVE));
    public static final SednaBulletConfig G12_SHREDDER_PHOSPHORUS = register(shredderBeam("g12_shredder_phosphorus",
            G12_PHOSPHORUS));

    public static final SednaBulletConfig P35800 = register(SednaBulletConfig.builder("p35800")
            .secretAmmo("P35_800").beam().spread(0.0F).physics(0.0D, 3).renderRotations(false)
            .armor(50.0F, 0.5F).spentCasing("35-800").beamRenderer(BR_CRACKLE)
            .behavior(SednaBehaviorTag.STANDARD_BEAM_HIT));
    public static final SednaBulletConfig P35800_BL = register(P35800.toBuilder("p35800_bl")
            .secretAmmo("P35_800_BL").beamRenderer(BR_BLACK_LIGHTNING)
            .behavior(SednaBehaviorTag.BLACK_FIRE_BEAM_HIT));

    public static final SednaBulletConfig DGK_NORMAL = register(SednaBulletConfig.builder("dgk_normal")
            .itemAmmo("ammo_dgk"));
    public static final SednaBulletConfig SHELL_NORMAL = register(shell240("shell_normal", "ammo_shell:STOCK",
            "240standard").damage(1.0F).behavior(SednaBehaviorTag.TURRET_240_STANDARD_EXPLODE));
    public static final SednaBulletConfig SHELL_EXPLOSIVE = register(shell240("shell_explosive",
            "ammo_shell:EXPLOSIVE", "240ext").damage(1.5F).behavior(SednaBehaviorTag.TURRET_240_VNT_EXPLODE));
    public static final SednaBulletConfig SHELL_AP = register(shell240("shell_ap", "ammo_shell:APFSDS_T",
            "240w").damage(2.0F).penetration(true));
    public static final SednaBulletConfig SHELL_DU = register(shell240("shell_du", "ammo_shell:APFSDS_DU",
            "240u").damage(2.5F).penetration(true).damageFalloffByPenetration(false));
    public static final SednaBulletConfig SHELL_W9 = register(shell240("shell_w9", "ammo_shell:W9",
            "240n").damage(2.5F).behavior(SednaBehaviorTag.MINI_NUKE_STANDARD));

    public static final SednaBulletConfig FEXT_WATER = register(fireExt("fext_water", "ammo_fireext:0", 0.025F)
            .behavior(SednaBehaviorTag.FIRE_EXTINGUISH_WATER)
            .behavior(SednaBehaviorTag.FIRE_EXTINGUISH_WATER_VISUAL));
    public static final SednaBulletConfig FEXT_FOAM = register(fireExt("fext_foam", "ammo_fireext:1", 0.05F)
            .behavior(SednaBehaviorTag.FIRE_EXTINGUISH_FOAM)
            .behavior(SednaBehaviorTag.FIRE_EXTINGUISH_FOAM_VISUAL));
    public static final SednaBulletConfig FEXT_SAND = register(fireExt("fext_sand", "ammo_fireext:2", 0.05F)
            .behavior(SednaBehaviorTag.FIRE_EXTINGUISH_SAND)
            .behavior(SednaBehaviorTag.FIRE_EXTINGUISH_SAND_VISUAL));

    public static final SednaBulletConfig CT_HOOK = register(standardNoCasing("ct_hook", "CT_HOOK")
            .ballistics(3.0F, 0.0F, 1.0F, 1, 1).physics(0.035D, 6_000).renderRotations(false)
            .penetration(true).damageFalloffByPenetration(false).renderer(R_CT_HOOK)
            .behavior(SednaBehaviorTag.CHARGE_HOOK_STICK));
    public static final SednaBulletConfig CT_MORTAR = register(standardNoCasing("ct_mortar", "CT_MORTAR")
            .damage(2.5F).ballistics(3.0F, 0.0F, 1.0F, 1, 1).physics(0.035D, 200)
            .renderer(R_CT_MORTAR).behavior(SednaBehaviorTag.CHARGE_MORTAR_EXPLODE));
    public static final SednaBulletConfig CT_MORTAR_CHARGE = register(standardNoCasing("ct_mortar_charge",
            "CT_MORTAR_CHARGE").damage(5.0F).ballistics(3.0F, 0.0F, 1.0F, 1, 1).physics(0.035D, 200)
            .renderer(R_CT_MORTAR_CHARGE).behavior(SednaBehaviorTag.CHARGE_MORTAR_CHARGE_EXPLODE));

    public static Optional<SednaBulletConfig> byName(String legacyName) {
        return Optional.ofNullable(BY_NAME.get(legacyName));
    }

    public static Collection<SednaBulletConfig> byAmmo(SednaBulletConfig.AmmoKind ammoKind, String ammoName) {
        String key = ammoKey(ammoKind, ammoName);
        List<SednaBulletConfig> configs = BY_AMMO.get(key);
        return configs == null ? List.of() : Collections.unmodifiableList(configs);
    }

    public static Collection<SednaBulletConfig> standardAmmo(String ammoName) {
        return byAmmo(SednaBulletConfig.AmmoKind.STANDARD, ammoName);
    }

    public static Collection<SednaBulletConfig> secretAmmo(String ammoName) {
        return byAmmo(SednaBulletConfig.AmmoKind.SECRET, ammoName);
    }

    public static Collection<SednaBulletConfig> itemAmmo(String ammoName) {
        return byAmmo(SednaBulletConfig.AmmoKind.ITEM, ammoName);
    }

    public static Collection<SednaBulletConfig> all() {
        return Collections.unmodifiableCollection(BY_NAME.values());
    }

    private static SednaBulletConfig.Builder base(String legacyName, String ammoName, String casingItem,
            int casingAmount, String spentCasing) {
        return SednaBulletConfig.builder(legacyName)
                .standardAmmo(ammoName)
                .casingItem(casingItem, casingAmount)
                .spentCasing(spentCasing)
                .renderer(R_STANDARD_BULLET);
    }

    private static SednaBulletConfig.Builder standard(String legacyName, String ammoName, String spentCasing) {
        return SednaBulletConfig.builder(legacyName)
                .standardAmmo(ammoName)
                .spentCasing(spentCasing)
                .renderer(R_STANDARD_BULLET);
    }

    private static SednaBulletConfig.Builder standardNoCasing(String legacyName, String ammoName) {
        return SednaBulletConfig.builder(legacyName)
                .standardAmmo(ammoName);
    }

    private static SednaBulletConfig.Builder sp(String legacyName, String ammoName, String casingItem,
            int casingAmount, String spentCasing) {
        return base(legacyName, ammoName, casingItem, casingAmount, spentCasing);
    }

    private static SednaBulletConfig.Builder fmj(String legacyName, String ammoName, String casingItem,
            int casingAmount, String spentCasing, float threshold) {
        return base(legacyName, ammoName, casingItem, casingAmount, spentCasing)
                .damage(0.8F).armor(threshold, 0.1F);
    }

    private static SednaBulletConfig.Builder jhp(String legacyName, String ammoName, String casingItem,
            int casingAmount, String spentCasing) {
        return base(legacyName, ammoName, casingItem, casingAmount, spentCasing)
                .damage(1.5F).headshot(1.5F).armor(0.0F, -0.25F);
    }

    private static SednaBulletConfig.Builder ap(String legacyName, String ammoName, String casingItem,
            int casingAmount, String spentCasing, float threshold) {
        return base(legacyName, ammoName, casingItem, casingAmount, spentCasing)
                .damage(1.25F).armor(threshold, 0.15F).penetration(true).renderer(R_AP_BULLET)
                .damageFalloffByPenetration(false);
    }

    private static SednaBulletConfig.Builder du(String legacyName, String ammoName, String casingItem,
            int casingAmount, String spentCasing, float threshold) {
        return base(legacyName, ammoName, casingItem, casingAmount, spentCasing)
                .damage(1.5F).armor(threshold, 0.25F).penetration(true).renderer(R_DU_BULLET)
                .damageFalloffByPenetration(false);
    }

    private static SednaBulletConfig.Builder shotgun(String legacyName, String ammoName, String casingItem,
            int casingAmount, String spentCasing, int projectiles, float damage, float spread, float ricochetAngle) {
        return base(legacyName, ammoName, casingItem, casingAmount, spentCasing)
                .projectiles(projectiles).damage(damage).spread(spread).ricochet(ricochetAngle, 2);
    }

    private static SednaBulletConfig.Builder secret(String legacyName, String ammoName, String spentCasing) {
        return SednaBulletConfig.builder(legacyName)
                .secretAmmo(ammoName)
                .spentCasing(spentCasing)
                .renderer(R_LEGENDARY_BULLET);
    }

    private static SednaBulletConfig.Builder flare(String legacyName, String ammoName, String spentCasing) {
        return base(legacyName, ammoName, "LARGE", 4, spentCasing)
                .ballistics(2.0F, 0.0F, 1.0F, 1, 1)
                .physics(0.015D, 100)
                .renderRotations(false);
    }

    private static SednaBulletConfig.Builder grenade40(String legacyName, String ammoName, String spentCasing) {
        return base(legacyName, ammoName, "LARGE", 4, spentCasing)
                .ballistics(2.0F, 0.0F, 1.0F, 1, 1)
                .physics(0.035D, 200)
                .renderer(R_GRENADE);
    }

    private static SednaBulletConfig.Builder rocket(String legacyName, String ammoName) {
        return standardNoCasing(legacyName, ammoName)
                .ballistics(0.0F, 0.0F, 1.0F, 1, 1)
                .physics(0.0D, 300)
                .selfDamageDelay(10)
                .renderer(R_RPZB)
                .clearBehavior(SednaBehaviorTag.STANDARD_RICOCHET)
                .clearBehavior(SednaBehaviorTag.STANDARD_ENTITY_HIT)
                .behavior(SednaBehaviorTag.ROCKET_ACCELERATE);
    }

    private static SednaBulletConfig.Builder rocketClone(String legacyName, SednaBulletConfig original,
            String rendererName, boolean steering) {
        SednaBulletConfig.Builder builder = original.toBuilder(legacyName)
                .renderer(rendererName);
        if (steering) {
            builder.physics(original.gravity(), 400)
                    .behavior(SednaBehaviorTag.ROCKET_STEER);
        }
        return builder;
    }

    private static SednaBulletConfig.Builder flame(String legacyName, String ammoName, float velocity, float spread,
            int projectiles, int expires, double gravity, SednaBehaviorTag lingerBehavior) {
        return SednaBulletConfig.builder(legacyName)
                .standardAmmo(ammoName)
                .casingItem("plate_steel", 2, 500)
                .reloadCount(500)
                .damageClass(DamageClass.FIRE)
                .ballistics(velocity, spread, 1.0F, projectiles, projectiles)
                .physics(gravity, expires)
                .selfDamageDelay(20)
                .knockback(0.0F)
                .clearBehavior(SednaBehaviorTag.STANDARD_RICOCHET)
                .behavior(SednaBehaviorTag.ENTITY_IGNITE)
                .behavior(SednaBehaviorTag.FLAME_VISUAL)
                .behavior(lingerBehavior);
    }

    private static SednaBulletConfig.Builder energyBeam(String legacyName, String ammoName, DamageClass damageClass,
            boolean penetrates) {
        return SednaBulletConfig.builder(legacyName)
                .standardAmmo(ammoName)
                .casingItem("ingot_polymer", 2, 4)
                .beam()
                .damageClass(damageClass)
                .spread(0.0F)
                .physics(0.0D, 5)
                .renderRotations(false)
                .beamRenderer(BR_STANDARD)
                .penetration(penetrates);
    }

    private static SednaBulletConfig.Builder miniNuke(String legacyName, String ammoName, float velocity,
            float spread, int projectiles, float damage) {
        return standardNoCasing(legacyName, ammoName)
                .ballistics(velocity, spread, 1.0F, projectiles, projectiles)
                .physics(0.025D, 300)
                .damage(damage);
    }

    private static SednaBulletConfig.Builder shredderBeam(String legacyName, SednaBulletConfig original) {
        return original.toBuilder(legacyName)
                .beam()
                .damageClass(DamageClass.LASER)
                .damage(original.damageMultiplier() * original.projectilesMax())
                .projectiles(1)
                .physics(0.0D, 5)
                .renderRotations(false)
                .beamRenderer(BR_LASER_CYAN)
                .behavior(SednaBehaviorTag.SHREDDER_BEAM_SPLIT);
    }

    private static SednaBulletConfig.Builder shredderSubmunition(String legacyName, SednaBulletConfig original) {
        return original.toBuilder(legacyName)
                .damageClass(DamageClass.PLASMA)
                .ballistics(0.5F, original.spread(), original.wear(), original.projectilesMin(),
                        original.projectilesMax())
                .physics(0.0D, 50)
                .ricochet(90.0F, 3)
                .clearBehavior(SednaBehaviorTag.STANDARD_RICOCHET)
                .behavior(SednaBehaviorTag.SHREDDER_RICOCHET);
    }

    private static SednaBulletConfig.Builder shell240(String legacyName, String ammoName, String spentCasing) {
        return SednaBulletConfig.builder(legacyName)
                .itemAmmo(ammoName)
                .spentCasing(spentCasing)
                .renderer(R_GRENADE);
    }

    private static SednaBulletConfig.Builder fireExt(String legacyName, String ammoName, float spread) {
        return SednaBulletConfig.builder(legacyName)
                .itemAmmo(ammoName)
                .reloadCount(300)
                .ballistics(0.75F, spread, 1.0F, 1, 1)
                .physics(0.04D, 100)
                .behavior(SednaBehaviorTag.FIRE_EXTINGUISH_ENTITY);
    }

    private static SednaBulletConfig energy(String legacyName, String ammoName, DamageClass damageClass,
            boolean penetrates) {
        return SednaBulletConfig.builder(legacyName)
                .standardAmmo(ammoName)
                .casingItem("ingot_polymer", 2, 4 * 40)
                .reloadCount(40)
                .beam()
                .damageClass(damageClass)
                .spread(0.0F)
                .physics(0.0D, 5)
                .renderRotations(false)
                .penetration(penetrates)
                .beamRenderer(BR_LASER_PURPLE)
                .behavior(SednaBehaviorTag.STANDARD_BEAM_HIT)
                .build();
    }

    private static SednaBulletConfig.Builder incendiary(String legacyName, SednaBulletConfig base) {
        return base.toBuilder(legacyName)
                .behavior(SednaBehaviorTag.INCENDIARY_PHOSPHORUS);
    }

    private static SednaBulletConfig register(SednaBulletConfig.Builder builder) {
        return register(builder.build());
    }

    private static SednaBulletConfig register(SednaBulletConfig config) {
        BY_NAME.put(config.legacyName(), config);
        BY_AMMO.computeIfAbsent(ammoKey(config.ammoKind(), config.ammoName()), ignored -> new ArrayList<>()).add(config);
        return config;
    }

    private static String ammoKey(SednaBulletConfig.AmmoKind ammoKind, String ammoName) {
        SednaBulletConfig.AmmoKind kind = ammoKind == null ? SednaBulletConfig.AmmoKind.STANDARD : ammoKind;
        return kind.name() + ":" + (ammoName == null ? "" : ammoName);
    }

    private LegacySednaBulletConfigs() {
    }
}
