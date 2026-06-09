package com.hbm.ntm.bullet;

import com.hbm.ntm.damage.DamageClass;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class LegacySednaBulletConfigs {
    private static final Map<String, SednaBulletConfig> BY_NAME = new LinkedHashMap<>();

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
    public static final SednaBulletConfig R556_INC_SP = register(incendiary("r556_inc_sp", R556_SP));
    public static final SednaBulletConfig R556_INC_FMJ = register(incendiary("r556_inc_fmj", R556_FMJ));
    public static final SednaBulletConfig R556_INC_JHP = register(incendiary("r556_inc_jhp", R556_JHP));
    public static final SednaBulletConfig R556_INC_AP = register(incendiary("r556_inc_ap", R556_AP));

    public static final SednaBulletConfig R762_SP = register(sp("r762_sp", "R762_SP", "SMALL", 6, "r762"));
    public static final SednaBulletConfig R762_FMJ = register(fmj("r762_fmj", "R762_FMJ", "SMALL", 6, "r762fmj", 5.0F));
    public static final SednaBulletConfig R762_JHP = register(jhp("r762_jhp", "R762_JHP", "SMALL", 6, "r762jhp"));
    public static final SednaBulletConfig R762_AP = register(ap("r762_ap", "R762_AP", "SMALL_STEEL", 6, "r762ap", 12.5F));
    public static final SednaBulletConfig R762_DU = register(du("r762_du", "R762_DU", "SMALL_STEEL", 6, "r762du", 15.0F));
    public static final SednaBulletConfig R762_HE = register(base("r762_he", "R762_HE", "SMALL_STEEL", 6, "r762he")
            .damage(1.75F).ballistics(10.0F, 0.0F, 3.0F, 1, 1)
            .behavior(SednaBehaviorTag.TINY_EXPLODE));

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
            .damage(1.5F).armor(3.0F, 0.1F).penetration(true).ballistics(10.0F, 0.0F, 1.5F, 1, 1));
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
            "BUCKSHOT", 6, "12GA_FLECHETTE", 8, 1.0F / 8.0F, 0.025F, 5.0F).armor(5.0F, 0.2F));
    public static final SednaBulletConfig G12_MAGNUM = register(shotgun("g12_magnum", "G12_MAGNUM",
            "BUCKSHOT_ADVANCED", 6, "12GA_MAGNUM", 4, 2.0F / 4.0F, 0.015F, 15.0F).armor(4.0F, 0.0F));
    public static final SednaBulletConfig G12_EXPLOSIVE = register(base("g12_explosive", "G12_EXPLOSIVE",
            "BUCKSHOT_ADVANCED", 6, "12GA_EXPLOSIVE").damage(2.5F).spread(0.0F)
            .ricochet(15.0F, 2).behavior(SednaBehaviorTag.STANDARD_EXPLODE));
    public static final SednaBulletConfig G12_PHOSPHORUS = register(shotgun("g12_phosphorus", "G12_PHOSPHORUS",
            "BUCKSHOT_ADVANCED", 6, "12GA_PHOSPHORUS", 8, 1.0F / 8.0F, 0.015F, 15.0F)
            .behavior(SednaBehaviorTag.INCENDIARY_PHOSPHORUS));
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
            .penetration(true).damageFalloffByPenetration(false));
    public static final SednaBulletConfig G10_SLUG = register(base("g10_slug", "G10_SLUG", "BUCKSHOT_ADVANCED", 4,
            "10GASlug").ricochet(15.0F, 2).armor(10.0F, 0.1F).penetration(true));
    public static final SednaBulletConfig G10_EXPLOSIVE = register(shotgun("g10_explosive", "G10_EXPLOSIVE",
            "BUCKSHOT_ADVANCED", 4, "10GAEXP", 10, 1.0F / 4.0F, 0.035F, 5.0F)
            .ballistics(10.0F, 0.035F, 3.0F, 10, 10).behavior(SednaBehaviorTag.TINY_EXPLODE));

    public static final SednaBulletConfig M357_BP = register(base("m357_bp", "M357_BP", "SMALL", 16, "")
            .damage(0.75F).blackPowder(true));
    public static final SednaBulletConfig M357_SP = register(sp("m357_sp", "M357_SP", "SMALL", 8, ""));
    public static final SednaBulletConfig M357_FMJ = register(fmj("m357_fmj", "M357_FMJ", "SMALL", 8, "", 2.0F));
    public static final SednaBulletConfig M357_JHP = register(jhp("m357_jhp", "M357_JHP", "SMALL", 8, ""));
    public static final SednaBulletConfig M357_AP = register(ap("m357_ap", "M357_AP", "SMALL_STEEL", 8, "", 5.0F));
    public static final SednaBulletConfig M357_EXPRESS = register(base("m357_express", "M357_EXPRESS",
            "SMALL", 8, "").damage(1.5F).armor(2.0F, 0.1F).penetration(true).wear(1.5F));

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
            12, "bmg50he").wear(3.0F).damage(1.75F).behavior(SednaBehaviorTag.TINY_EXPLODE));
    public static final SednaBulletConfig BMG50_SM = register(base("bmg50_sm", "BMG50_SM", "LARGE_STEEL",
            6, "bmg50sm").wear(10.0F).penetration(true).damageFalloffByPenetration(false)
            .damage(2.5F).armor(30.0F, 0.35F));
    public static final SednaBulletConfig BMG50_BLACK = register(secret("bmg50_black", "BMG50_BLACK",
            "bmg50black").wear(5.0F).penetration(true).damageFalloffByPenetration(false).spectral(true)
            .damage(1.5F).headshot(3.0F).armor(30.0F, 0.35F));
    public static final SednaBulletConfig BMG50_EQUESTRIAN = register(secret("bmg50_equestrian",
            "BMG50_EQUESTRIAN", "bmg50equestrian").damage(0.0F).behavior(SednaBehaviorTag.BUILDING_IMPACT));

    public static final SednaBulletConfig B75 = register(standard("b75", "B75", "b75")
            .behavior(SednaBehaviorTag.TINY_EXPLODE));
    public static final SednaBulletConfig B75_INC = register(standard("b75_inc", "B75_INC", "b75inc")
            .damage(0.8F).armor(0.0F, 0.1F).behavior(SednaBehaviorTag.INCENDIARY_PHOSPHORUS));
    public static final SednaBulletConfig B75_EXP = register(standard("b75_exp", "B75_EXP", "b75exp")
            .damage(1.5F).armor(0.0F, -0.25F).behavior(SednaBehaviorTag.STANDARD_EXPLODE));

    public static final SednaBulletConfig G26_FLARE = register(flare("g26_flare", "G26_FLARE",
            "g26Flare").behavior(SednaBehaviorTag.ENTITY_IGNITE));
    public static final SednaBulletConfig G26_FLARE_SUPPLY = register(flare("g26_flare_supply",
            "G26_FLARE_SUPPLY", "g26FlareSupply").behavior(SednaBehaviorTag.ENTITY_IGNITE)
            .behavior(SednaBehaviorTag.AIRDROP_SUPPLIES));
    public static final SednaBulletConfig G26_FLARE_WEAPON = register(flare("g26_flare_weapon",
            "G26_FLARE_WEAPON", "g26FlareWeapon").behavior(SednaBehaviorTag.ENTITY_IGNITE)
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
            DamageClass.ELECTRIC, true).behavior(SednaBehaviorTag.LIGHTNING_BEAM_HIT));
    public static final SednaBulletConfig ENERGY_TESLA_OVERCHARGE = register(energyBeam("energy_tesla_overcharge",
            "CAPACITOR_OVERCHARGE", DamageClass.ELECTRIC, true).damage(1.5F)
            .behavior(SednaBehaviorTag.LIGHTNING_BEAM_HIT));
    public static final SednaBulletConfig ENERGY_TESLA_IR = register(energyBeam("energy_tesla_ir", "CAPACITOR_IR",
            DamageClass.ELECTRIC, false).damage(0.8F).behavior(SednaBehaviorTag.LIGHTNING_BEAM_SPLIT));
    public static final SednaBulletConfig ENERGY_TESLA_IR_SUB = register(SednaBulletConfig.builder(
            "energy_tesla_ir_sub").standardAmmo("CAPACITOR_IR").beam().damageClass(DamageClass.ELECTRIC)
            .spread(0.0F).wear(3.0F).physics(0.0D, 3).renderRotations(false).penetration(true)
            .damage(0.5F).behavior(SednaBehaviorTag.STANDARD_BEAM_HIT));
    public static final SednaBulletConfig ENERGY_LAS = register(energyBeam("energy_las", "CAPACITOR",
            DamageClass.LASER, false).behavior(SednaBehaviorTag.STANDARD_BEAM_HIT));
    public static final SednaBulletConfig ENERGY_LAS_OVERCHARGE = register(energyBeam("energy_las_overcharge",
            "CAPACITOR_OVERCHARGE", DamageClass.LASER, true).behavior(SednaBehaviorTag.STANDARD_BEAM_HIT));
    public static final SednaBulletConfig ENERGY_LAS_IR = register(energyBeam("energy_las_ir", "CAPACITOR_IR",
            DamageClass.FIRE, false).behavior(SednaBehaviorTag.INFRARED_BEAM_HIT));
    public static final SednaBulletConfig ENERGY_EMERALD = register(ENERGY_LAS.toBuilder("energy_emerald")
            .armor(10.0F, 0.5F));
    public static final SednaBulletConfig ENERGY_EMERALD_OVERCHARGE = register(ENERGY_LAS_OVERCHARGE.toBuilder(
            "energy_emerald_overcharge").armor(15.0F, 0.5F));
    public static final SednaBulletConfig ENERGY_EMERALD_IR = register(ENERGY_LAS_IR.toBuilder("energy_emerald_ir")
            .armor(10.0F, 0.5F));

    public static final SednaBulletConfig TAU_URANIUM = register(SednaBulletConfig.builder("tau_uranium")
            .standardAmmo("TAU_URANIUM").casingItem("plate_lead", 2, 16).beam().damageClass(DamageClass.SUBATOMIC)
            .physics(0.0D, 5).renderRotations(false).penetration(true).damageFalloffByPenetration(false)
            .behavior(SednaBehaviorTag.BEAM_HIT));
    public static final SednaBulletConfig TAU_URANIUM_CHARGE = register(TAU_URANIUM.toBuilder(
            "tau_uranium_charge").spectral(true));
    public static final SednaBulletConfig COIL_TUNGSTEN = register(SednaBulletConfig.builder("coil_tungsten")
            .standardAmmo("COIL_TUNGSTEN").ballistics(7.5F, 0.0F, 1.0F, 1, 1).physics(0.0D, 50)
            .penetration(true).damageFalloffByPenetration(false).spectral(true)
            .behavior(SednaBehaviorTag.COIL_BREAK_WEAK_BLOCKS));
    public static final SednaBulletConfig COIL_FERROURANIUM = register(SednaBulletConfig.builder("coil_ferrouranium")
            .standardAmmo("COIL_FERROURANIUM").ballistics(7.5F, 0.0F, 1.0F, 1, 1).physics(0.0D, 50)
            .penetration(true).damageFalloffByPenetration(false).spectral(true)
            .behavior(SednaBehaviorTag.COIL_BREAK_STRONGER_BLOCKS));
    public static final SednaBulletConfig NI4NI_ARC = register(SednaBulletConfig.builder("ni4ni_arc")
            .beam().damageClass(DamageClass.PHYSICAL).physics(0.0D, 5).armor(10.0F, 0.2F)
            .renderRotations(false).penetration(false).behavior(SednaBehaviorTag.BEAM_HIT));

    public static final SednaBulletConfig NUKE_STANDARD = register(miniNuke("nuke_standard", "NUKE_STANDARD",
            3.0F, 0.0F, 1, 1.0F).behavior(SednaBehaviorTag.MINI_NUKE_STANDARD));
    public static final SednaBulletConfig NUKE_DEMO = register(miniNuke("nuke_demo", "NUKE_DEMO",
            3.0F, 0.0F, 1, 1.0F).behavior(SednaBehaviorTag.MINI_NUKE_DEMO));
    public static final SednaBulletConfig NUKE_HIGH = register(miniNuke("nuke_high", "NUKE_HIGH",
            3.0F, 0.0F, 1, 1.0F).behavior(SednaBehaviorTag.MINI_NUKE_HIGH));
    public static final SednaBulletConfig NUKE_TOTS = register(miniNuke("nuke_tots", "NUKE_TOTS",
            3.0F, 0.1F, 8, 0.35F).behavior(SednaBehaviorTag.MINI_NUKE_TINYTOT));
    public static final SednaBulletConfig NUKE_HIVE = register(miniNuke("nuke_hive", "NUKE_HIVE",
            1.0F, 0.15F, 12, 0.25F).behavior(SednaBehaviorTag.MINI_NUKE_HIVE));
    public static final SednaBulletConfig NUKE_BALEFIRE = register(miniNuke("nuke_balefire", "NUKE_BALEFIRE",
            3.0F, 0.0F, 1, 2.5F).behavior(SednaBehaviorTag.MINI_NUKE_BALEFIRE));
    public static final SednaBulletConfig CLUSTER_SUBMUNITION = register(SednaBulletConfig.builder(
            "cluster_submunition").physics(0.025D, 1_200).behavior(SednaBehaviorTag.DEMO_EXPLODE));

    public static final SednaBulletConfig FOLLY_SM = register(SednaBulletConfig.builder("folly_sm")
            .secretAmmo("FOLLY_SM").beam().damageClass(DamageClass.SUBATOMIC).ballistics(2.0F, 0.0F, 1.0F, 1, 1)
            .physics(0.015D, 100).renderRotations(false).spectral(true).penetration(true)
            .behavior(SednaBehaviorTag.FOLLY_SUPERMATTER_BEAM));
    public static final SednaBulletConfig FOLLY_NUKE = register(SednaBulletConfig.builder("folly_nuke")
            .secretAmmo("FOLLY_NUKE").chunkloadingBullet().ballistics(4.0F, 0.0F, 1.0F, 1, 1)
            .physics(0.015D, 600).behavior(SednaBehaviorTag.FOLLY_NUKE_IMPACT));

    public static Optional<SednaBulletConfig> byName(String legacyName) {
        return Optional.ofNullable(BY_NAME.get(legacyName));
    }

    public static Collection<SednaBulletConfig> all() {
        return Collections.unmodifiableCollection(BY_NAME.values());
    }

    private static SednaBulletConfig.Builder base(String legacyName, String ammoName, String casingItem,
            int casingAmount, String spentCasing) {
        return SednaBulletConfig.builder(legacyName)
                .standardAmmo(ammoName)
                .casingItem(casingItem, casingAmount)
                .spentCasing(spentCasing);
    }

    private static SednaBulletConfig.Builder standard(String legacyName, String ammoName, String spentCasing) {
        return SednaBulletConfig.builder(legacyName)
                .standardAmmo(ammoName)
                .spentCasing(spentCasing);
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
                .damage(1.25F).armor(threshold, 0.15F).penetration(true)
                .damageFalloffByPenetration(false);
    }

    private static SednaBulletConfig.Builder du(String legacyName, String ammoName, String casingItem,
            int casingAmount, String spentCasing, float threshold) {
        return base(legacyName, ammoName, casingItem, casingAmount, spentCasing)
                .damage(1.5F).armor(threshold, 0.25F).penetration(true)
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
                .spentCasing(spentCasing);
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
                .physics(0.035D, 200);
    }

    private static SednaBulletConfig.Builder rocket(String legacyName, String ammoName) {
        return standardNoCasing(legacyName, ammoName)
                .ballistics(0.0F, 0.0F, 1.0F, 1, 1)
                .physics(0.0D, 300)
                .selfDamageDelay(10)
                .clearBehavior(SednaBehaviorTag.STANDARD_RICOCHET)
                .clearBehavior(SednaBehaviorTag.STANDARD_ENTITY_HIT)
                .behavior(SednaBehaviorTag.ROCKET_ACCELERATE);
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
                .penetration(penetrates);
    }

    private static SednaBulletConfig.Builder miniNuke(String legacyName, String ammoName, float velocity,
            float spread, int projectiles, float damage) {
        return standardNoCasing(legacyName, ammoName)
                .ballistics(velocity, spread, 1.0F, projectiles, projectiles)
                .physics(0.025D, 300)
                .damage(damage);
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
                .behavior(SednaBehaviorTag.STANDARD_BEAM_HIT)
                .build();
    }

    private static SednaBulletConfig incendiary(String legacyName, SednaBulletConfig base) {
        return base.toBuilder(legacyName)
                .behavior(SednaBehaviorTag.INCENDIARY_PHOSPHORUS)
                .build();
    }

    private static SednaBulletConfig register(SednaBulletConfig.Builder builder) {
        return register(builder.build());
    }

    private static SednaBulletConfig register(SednaBulletConfig config) {
        BY_NAME.put(config.legacyName(), config);
        return config;
    }

    private LegacySednaBulletConfigs() {
    }
}
