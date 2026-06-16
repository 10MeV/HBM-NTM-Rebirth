package com.hbm.ntm.bullet;

import com.hbm.ntm.damage.DamageClass;
import com.hbm.ntm.radiation.ModDamageSources;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class LegacySednaRuntimeBulletConfigs {
    private static final Map<String, BulletConfig> BY_NAME = new LinkedHashMap<>();

    public static final BulletConfig G12_BP = runtime(LegacySednaBulletConfigs.G12_BP);
    public static final BulletConfig G12_BP_MAGNUM = runtime(LegacySednaBulletConfigs.G12_BP_MAGNUM);
    public static final BulletConfig G12_BP_SLUG = runtime(LegacySednaBulletConfigs.G12_BP_SLUG);
    public static final BulletConfig G12 = runtime(LegacySednaBulletConfigs.G12);
    public static final BulletConfig G12_SLUG = runtime(LegacySednaBulletConfigs.G12_SLUG);
    public static final BulletConfig G12_FLECHETTE = runtime(LegacySednaBulletConfigs.G12_FLECHETTE);
    public static final BulletConfig G12_MAGNUM = runtime(LegacySednaBulletConfigs.G12_MAGNUM);
    public static final BulletConfig G12_EXPLOSIVE = replace(runtime(LegacySednaBulletConfigs.G12_EXPLOSIVE)
            .toBuilder()
            .explosive(2.0F)
            .build());
    public static final BulletConfig G12_PHOSPHORUS = runtime(LegacySednaBulletConfigs.G12_PHOSPHORUS);
    public static final BulletConfig G12_EQUESTRIAN_BJ = runtime(LegacySednaBulletConfigs.G12_EQUESTRIAN_BJ);
    public static final BulletConfig G12_EQUESTRIAN_TKR = runtime(LegacySednaBulletConfigs.G12_EQUESTRIAN_TKR);

    public static final BulletConfig[] GAUNTLET_12GA_ORDER = {
            G12_BP,
            G12_BP_MAGNUM,
            G12_BP_SLUG,
            G12,
            G12_SLUG,
            G12_FLECHETTE,
            G12_MAGNUM,
            G12_EXPLOSIVE,
            G12_PHOSPHORUS
    };

    public static final BulletConfig G10 = runtime(LegacySednaBulletConfigs.G10);
    public static final BulletConfig G10_SHRAPNEL = runtime(LegacySednaBulletConfigs.G10_SHRAPNEL);
    public static final BulletConfig G10_DU = runtime(LegacySednaBulletConfigs.G10_DU);
    public static final BulletConfig G10_SLUG = runtime(LegacySednaBulletConfigs.G10_SLUG);
    public static final BulletConfig G10_EXPLOSIVE = runtime(LegacySednaBulletConfigs.G10_EXPLOSIVE);

    public static final BulletConfig P22_SP = runtime(LegacySednaBulletConfigs.P22_SP);
    public static final BulletConfig P22_FMJ = runtime(LegacySednaBulletConfigs.P22_FMJ);
    public static final BulletConfig P22_JHP = runtime(LegacySednaBulletConfigs.P22_JHP);
    public static final BulletConfig P22_AP = runtime(LegacySednaBulletConfigs.P22_AP);
    public static final BulletConfig P9_SP = runtime(LegacySednaBulletConfigs.P9_SP);
    public static final BulletConfig P9_FMJ = runtime(LegacySednaBulletConfigs.P9_FMJ);
    public static final BulletConfig P9_JHP = runtime(LegacySednaBulletConfigs.P9_JHP);
    public static final BulletConfig P9_AP = runtime(LegacySednaBulletConfigs.P9_AP);
    public static final BulletConfig R556_SP = runtime(LegacySednaBulletConfigs.R556_SP);
    public static final BulletConfig R556_FMJ = runtime(LegacySednaBulletConfigs.R556_FMJ);
    public static final BulletConfig R556_JHP = runtime(LegacySednaBulletConfigs.R556_JHP);
    public static final BulletConfig R556_AP = runtime(LegacySednaBulletConfigs.R556_AP);
    public static final BulletConfig R556_INC_SP = runtime(LegacySednaBulletConfigs.R556_INC_SP);
    public static final BulletConfig R556_INC_FMJ = runtime(LegacySednaBulletConfigs.R556_INC_FMJ);
    public static final BulletConfig R556_INC_JHP = runtime(LegacySednaBulletConfigs.R556_INC_JHP);
    public static final BulletConfig R556_INC_AP = runtime(LegacySednaBulletConfigs.R556_INC_AP);
    public static final BulletConfig R762_SP = runtime(LegacySednaBulletConfigs.R762_SP);
    public static final BulletConfig R762_FMJ = runtime(LegacySednaBulletConfigs.R762_FMJ);
    public static final BulletConfig R762_JHP = runtime(LegacySednaBulletConfigs.R762_JHP);
    public static final BulletConfig R762_AP = runtime(LegacySednaBulletConfigs.R762_AP);
    public static final BulletConfig R762_DU = runtime(LegacySednaBulletConfigs.R762_DU);
    public static final BulletConfig R762_HE = runtime(LegacySednaBulletConfigs.R762_HE);
    public static final BulletConfig M44_BP = runtime(LegacySednaBulletConfigs.M44_BP);
    public static final BulletConfig M44_SP = runtime(LegacySednaBulletConfigs.M44_SP);
    public static final BulletConfig M44_FMJ = runtime(LegacySednaBulletConfigs.M44_FMJ);
    public static final BulletConfig M44_JHP = runtime(LegacySednaBulletConfigs.M44_JHP);
    public static final BulletConfig M44_AP = runtime(LegacySednaBulletConfigs.M44_AP);
    public static final BulletConfig M44_EXPRESS = runtime(LegacySednaBulletConfigs.M44_EXPRESS);
    public static final BulletConfig M44_EQUESTRIAN_PIP = runtime(LegacySednaBulletConfigs.M44_EQUESTRIAN_PIP);
    public static final BulletConfig M44_EQUESTRIAN_MN7 = runtime(LegacySednaBulletConfigs.M44_EQUESTRIAN_MN7);
    public static final BulletConfig M357_BP = runtime(LegacySednaBulletConfigs.M357_BP);
    public static final BulletConfig M357_SP = runtime(LegacySednaBulletConfigs.M357_SP);
    public static final BulletConfig M357_FMJ = runtime(LegacySednaBulletConfigs.M357_FMJ);
    public static final BulletConfig M357_JHP = runtime(LegacySednaBulletConfigs.M357_JHP);
    public static final BulletConfig M357_AP = runtime(LegacySednaBulletConfigs.M357_AP);
    public static final BulletConfig M357_EXPRESS = runtime(LegacySednaBulletConfigs.M357_EXPRESS);
    public static final BulletConfig P45_SP = runtime(LegacySednaBulletConfigs.P45_SP);
    public static final BulletConfig P45_FMJ = runtime(LegacySednaBulletConfigs.P45_FMJ);
    public static final BulletConfig P45_JHP = runtime(LegacySednaBulletConfigs.P45_JHP);
    public static final BulletConfig P45_AP = runtime(LegacySednaBulletConfigs.P45_AP);
    public static final BulletConfig P45_DU = runtime(LegacySednaBulletConfigs.P45_DU);
    public static final BulletConfig BMG50_SP = runtime(LegacySednaBulletConfigs.BMG50_SP);
    public static final BulletConfig BMG50_FMJ = runtime(LegacySednaBulletConfigs.BMG50_FMJ);
    public static final BulletConfig BMG50_JHP = runtime(LegacySednaBulletConfigs.BMG50_JHP);
    public static final BulletConfig BMG50_AP = runtime(LegacySednaBulletConfigs.BMG50_AP);
    public static final BulletConfig BMG50_DU = runtime(LegacySednaBulletConfigs.BMG50_DU);
    public static final BulletConfig BMG50_HE = runtime(LegacySednaBulletConfigs.BMG50_HE);
    public static final BulletConfig BMG50_SM = runtime(LegacySednaBulletConfigs.BMG50_SM);
    public static final BulletConfig BMG50_BLACK = runtime(LegacySednaBulletConfigs.BMG50_BLACK);
    public static final BulletConfig BMG50_EQUESTRIAN = runtime(LegacySednaBulletConfigs.BMG50_EQUESTRIAN);

    public static final BulletConfig B75 = runtime(LegacySednaBulletConfigs.B75);
    public static final BulletConfig B75_INC = runtime(LegacySednaBulletConfigs.B75_INC);
    public static final BulletConfig B75_EXP = runtime(LegacySednaBulletConfigs.B75_EXP);

    public static final BulletConfig G26_FLARE = runtime(LegacySednaBulletConfigs.G26_FLARE);
    public static final BulletConfig G26_FLARE_SUPPLY = runtime(LegacySednaBulletConfigs.G26_FLARE_SUPPLY);
    public static final BulletConfig G26_FLARE_WEAPON = runtime(LegacySednaBulletConfigs.G26_FLARE_WEAPON);
    public static final BulletConfig G40_HE = runtime(LegacySednaBulletConfigs.G40_HE);
    public static final BulletConfig G40_HEAT = runtime(LegacySednaBulletConfigs.G40_HEAT);
    public static final BulletConfig G40_DEMO = runtime(LegacySednaBulletConfigs.G40_DEMO);
    public static final BulletConfig G40_INC = runtime(LegacySednaBulletConfigs.G40_INC);
    public static final BulletConfig G40_PHOSPHORUS = runtime(LegacySednaBulletConfigs.G40_PHOSPHORUS);

    public static final BulletConfig ROCKET_HE = runtime(LegacySednaBulletConfigs.ROCKET_HE);
    public static final BulletConfig ROCKET_HEAT = runtime(LegacySednaBulletConfigs.ROCKET_HEAT);
    public static final BulletConfig ROCKET_DEMO = runtime(LegacySednaBulletConfigs.ROCKET_DEMO);
    public static final BulletConfig ROCKET_INC = runtime(LegacySednaBulletConfigs.ROCKET_INC);
    public static final BulletConfig ROCKET_PHOSPHORUS = runtime(LegacySednaBulletConfigs.ROCKET_PHOSPHORUS);
    public static final BulletConfig ROCKET_RPZB_HE = runtime(LegacySednaBulletConfigs.ROCKET_RPZB_HE);
    public static final BulletConfig ROCKET_RPZB_HEAT = runtime(LegacySednaBulletConfigs.ROCKET_RPZB_HEAT);
    public static final BulletConfig ROCKET_RPZB_DEMO = runtime(LegacySednaBulletConfigs.ROCKET_RPZB_DEMO);
    public static final BulletConfig ROCKET_RPZB_INC = runtime(LegacySednaBulletConfigs.ROCKET_RPZB_INC);
    public static final BulletConfig ROCKET_RPZB_PHOSPHORUS = runtime(
            LegacySednaBulletConfigs.ROCKET_RPZB_PHOSPHORUS);
    public static final BulletConfig ROCKET_QD_HE = runtime(LegacySednaBulletConfigs.ROCKET_QD_HE);
    public static final BulletConfig ROCKET_QD_HEAT = runtime(LegacySednaBulletConfigs.ROCKET_QD_HEAT);
    public static final BulletConfig ROCKET_QD_DEMO = runtime(LegacySednaBulletConfigs.ROCKET_QD_DEMO);
    public static final BulletConfig ROCKET_QD_INC = runtime(LegacySednaBulletConfigs.ROCKET_QD_INC);
    public static final BulletConfig ROCKET_QD_PHOSPHORUS = runtime(LegacySednaBulletConfigs.ROCKET_QD_PHOSPHORUS);
    public static final BulletConfig ROCKET_ML_HE = runtime(LegacySednaBulletConfigs.ROCKET_ML_HE);
    public static final BulletConfig ROCKET_ML_HEAT = runtime(LegacySednaBulletConfigs.ROCKET_ML_HEAT);
    public static final BulletConfig ROCKET_ML_DEMO = runtime(LegacySednaBulletConfigs.ROCKET_ML_DEMO);
    public static final BulletConfig ROCKET_ML_INC = runtime(LegacySednaBulletConfigs.ROCKET_ML_INC);
    public static final BulletConfig ROCKET_ML_PHOSPHORUS = runtime(LegacySednaBulletConfigs.ROCKET_ML_PHOSPHORUS);
    public static final BulletConfig ROCKET_NCRPA_STEER_HE = runtime(
            LegacySednaBulletConfigs.ROCKET_NCRPA_STEER_HE);
    public static final BulletConfig ROCKET_NCRPA_STEER_HEAT = runtime(
            LegacySednaBulletConfigs.ROCKET_NCRPA_STEER_HEAT);
    public static final BulletConfig ROCKET_NCRPA_STEER_DEMO = runtime(
            LegacySednaBulletConfigs.ROCKET_NCRPA_STEER_DEMO);
    public static final BulletConfig ROCKET_NCRPA_STEER_INC = runtime(
            LegacySednaBulletConfigs.ROCKET_NCRPA_STEER_INC);
    public static final BulletConfig ROCKET_NCRPA_STEER_PHOSPHORUS = runtime(
            LegacySednaBulletConfigs.ROCKET_NCRPA_STEER_PHOSPHORUS);
    public static final BulletConfig ROCKET_NCRPA_HE = runtime(LegacySednaBulletConfigs.ROCKET_NCRPA_HE);
    public static final BulletConfig ROCKET_NCRPA_HEAT = runtime(LegacySednaBulletConfigs.ROCKET_NCRPA_HEAT);
    public static final BulletConfig ROCKET_NCRPA_DEMO = runtime(LegacySednaBulletConfigs.ROCKET_NCRPA_DEMO);
    public static final BulletConfig ROCKET_NCRPA_INC = runtime(LegacySednaBulletConfigs.ROCKET_NCRPA_INC);
    public static final BulletConfig ROCKET_NCRPA_PHOSPHORUS = runtime(
            LegacySednaBulletConfigs.ROCKET_NCRPA_PHOSPHORUS);

    public static final BulletConfig FLAME_DIESEL = runtime(LegacySednaBulletConfigs.FLAME_DIESEL);
    public static final BulletConfig FLAME_GAS = runtime(LegacySednaBulletConfigs.FLAME_GAS);
    public static final BulletConfig FLAME_NAPALM = runtime(LegacySednaBulletConfigs.FLAME_NAPALM);
    public static final BulletConfig FLAME_BALEFIRE = runtime(LegacySednaBulletConfigs.FLAME_BALEFIRE);
    public static final BulletConfig FLAME_NOGRAV = runtime(LegacySednaBulletConfigs.FLAME_NOGRAV);
    public static final BulletConfig FLAME_NOGRAV_BF = runtime(LegacySednaBulletConfigs.FLAME_NOGRAV_BF);
    public static final BulletConfig FLAME_TOPAZ_DIESEL = runtime(LegacySednaBulletConfigs.FLAME_TOPAZ_DIESEL);
    public static final BulletConfig FLAME_TOPAZ_GAS = runtime(LegacySednaBulletConfigs.FLAME_TOPAZ_GAS);
    public static final BulletConfig FLAME_TOPAZ_NAPALM = runtime(LegacySednaBulletConfigs.FLAME_TOPAZ_NAPALM);
    public static final BulletConfig FLAME_TOPAZ_BALEFIRE = runtime(LegacySednaBulletConfigs.FLAME_TOPAZ_BALEFIRE);
    public static final BulletConfig FLAME_DAYBREAKER_DIESEL = runtime(
            LegacySednaBulletConfigs.FLAME_DAYBREAKER_DIESEL);
    public static final BulletConfig FLAME_DAYBREAKER_GAS = runtime(LegacySednaBulletConfigs.FLAME_DAYBREAKER_GAS);
    public static final BulletConfig FLAME_DAYBREAKER_NAPALM = runtime(
            LegacySednaBulletConfigs.FLAME_DAYBREAKER_NAPALM);
    public static final BulletConfig FLAME_DAYBREAKER_BALEFIRE = runtime(
            LegacySednaBulletConfigs.FLAME_DAYBREAKER_BALEFIRE);

    public static final BulletConfig ENERGY_TESLA = runtime(LegacySednaBulletConfigs.ENERGY_TESLA);
    public static final BulletConfig ENERGY_TESLA_OVERCHARGE = runtime(
            LegacySednaBulletConfigs.ENERGY_TESLA_OVERCHARGE);
    public static final BulletConfig ENERGY_TESLA_IR = runtime(LegacySednaBulletConfigs.ENERGY_TESLA_IR);
    public static final BulletConfig ENERGY_TESLA_IR_SUB = runtime(LegacySednaBulletConfigs.ENERGY_TESLA_IR_SUB);
    public static final BulletConfig BATTERY_SOCKET_DISCHARGE = runtime(
            LegacySednaBulletConfigs.BATTERY_SOCKET_DISCHARGE);
    public static final BulletConfig ENERGY_LAS = runtime(LegacySednaBulletConfigs.ENERGY_LAS);
    public static final BulletConfig ENERGY_LAS_OVERCHARGE = runtime(LegacySednaBulletConfigs.ENERGY_LAS_OVERCHARGE);
    public static final BulletConfig ENERGY_LAS_IR = runtime(LegacySednaBulletConfigs.ENERGY_LAS_IR);
    public static final BulletConfig ENERGY_EMERALD = runtime(LegacySednaBulletConfigs.ENERGY_EMERALD);
    public static final BulletConfig ENERGY_EMERALD_OVERCHARGE = runtime(
            LegacySednaBulletConfigs.ENERGY_EMERALD_OVERCHARGE);
    public static final BulletConfig ENERGY_EMERALD_IR = runtime(LegacySednaBulletConfigs.ENERGY_EMERALD_IR);
    public static final BulletConfig ENERGY_LACUNAE = runtime(LegacySednaBulletConfigs.ENERGY_LACUNAE);
    public static final BulletConfig ENERGY_LACUNAE_OVERCHARGE = runtime(
            LegacySednaBulletConfigs.ENERGY_LACUNAE_OVERCHARGE);
    public static final BulletConfig ENERGY_LACUNAE_IR = runtime(LegacySednaBulletConfigs.ENERGY_LACUNAE_IR);
    public static final BulletConfig TAU_URANIUM = runtime(LegacySednaBulletConfigs.TAU_URANIUM);
    public static final BulletConfig TAU_URANIUM_CHARGE = runtime(LegacySednaBulletConfigs.TAU_URANIUM_CHARGE);
    public static final BulletConfig COIL_TUNGSTEN = runtime(LegacySednaBulletConfigs.COIL_TUNGSTEN);
    public static final BulletConfig COIL_FERROURANIUM = runtime(LegacySednaBulletConfigs.COIL_FERROURANIUM);
    public static final BulletConfig NI4NI_ARC = runtime(LegacySednaBulletConfigs.NI4NI_ARC);

    public static final BulletConfig NUKE_STANDARD = runtime(LegacySednaBulletConfigs.NUKE_STANDARD);
    public static final BulletConfig NUKE_DEMO = runtime(LegacySednaBulletConfigs.NUKE_DEMO);
    public static final BulletConfig NUKE_HIGH = runtime(LegacySednaBulletConfigs.NUKE_HIGH);
    public static final BulletConfig NUKE_TOTS = runtime(LegacySednaBulletConfigs.NUKE_TOTS);
    public static final BulletConfig NUKE_HIVE = runtime(LegacySednaBulletConfigs.NUKE_HIVE);
    public static final BulletConfig NUKE_BALEFIRE = runtime(LegacySednaBulletConfigs.NUKE_BALEFIRE);
    public static final BulletConfig CLUSTER_SUBMUNITION = runtime(LegacySednaBulletConfigs.CLUSTER_SUBMUNITION);
    public static final BulletConfig FOLLY_SM = runtime(LegacySednaBulletConfigs.FOLLY_SM);
    public static final BulletConfig FOLLY_NUKE = runtime(LegacySednaBulletConfigs.FOLLY_NUKE);
    public static final BulletConfig DEBUG = runtime(LegacySednaBulletConfigs.DEBUG);
    public static final BulletConfig DEBUG_SHOT = runtime(LegacySednaBulletConfigs.DEBUG_SHOT);
    public static final BulletConfig GRENADE_FRAGMENTATION = runtime(LegacySednaBulletConfigs.GRENADE_FRAGMENTATION);
    public static final BulletConfig GRENADE_PELLETS = runtime(LegacySednaBulletConfigs.GRENADE_PELLETS);
    public static final BulletConfig GRENADE_PELLETS_HEAVY = runtime(
            LegacySednaBulletConfigs.GRENADE_PELLETS_HEAVY);
    public static final BulletConfig GRENADE_LASER = runtime(LegacySednaBulletConfigs.GRENADE_LASER);
    public static final BulletConfig STONE = runtime(LegacySednaBulletConfigs.STONE);
    public static final BulletConfig FLINT = runtime(LegacySednaBulletConfigs.FLINT);
    public static final BulletConfig IRON = runtime(LegacySednaBulletConfigs.IRON);
    public static final BulletConfig SHOT = runtime(LegacySednaBulletConfigs.SHOT);

    public static final BulletConfig G12_SUB = runtime(LegacySednaBulletConfigs.G12_SUB);
    public static final BulletConfig G12_SUB_SLUG = runtime(LegacySednaBulletConfigs.G12_SUB_SLUG);
    public static final BulletConfig G12_SUB_FLECHETTE = runtime(LegacySednaBulletConfigs.G12_SUB_FLECHETTE);
    public static final BulletConfig G12_SUB_MAGNUM = runtime(LegacySednaBulletConfigs.G12_SUB_MAGNUM);
    public static final BulletConfig G12_SUB_EXPLOSIVE = runtime(LegacySednaBulletConfigs.G12_SUB_EXPLOSIVE);
    public static final BulletConfig G12_SUB_PHOSPHORUS = runtime(LegacySednaBulletConfigs.G12_SUB_PHOSPHORUS);
    public static final BulletConfig G12_SHREDDER = runtime(LegacySednaBulletConfigs.G12_SHREDDER);
    public static final BulletConfig G12_SHREDDER_SLUG = runtime(LegacySednaBulletConfigs.G12_SHREDDER_SLUG);
    public static final BulletConfig G12_SHREDDER_FLECHETTE = runtime(
            LegacySednaBulletConfigs.G12_SHREDDER_FLECHETTE);
    public static final BulletConfig G12_SHREDDER_MAGNUM = runtime(LegacySednaBulletConfigs.G12_SHREDDER_MAGNUM);
    public static final BulletConfig G12_SHREDDER_EXPLOSIVE = runtime(
            LegacySednaBulletConfigs.G12_SHREDDER_EXPLOSIVE);
    public static final BulletConfig G12_SHREDDER_PHOSPHORUS = runtime(
            LegacySednaBulletConfigs.G12_SHREDDER_PHOSPHORUS);

    public static final BulletConfig P35800 = runtime(LegacySednaBulletConfigs.P35800);
    public static final BulletConfig P35800_BL = runtime(LegacySednaBulletConfigs.P35800_BL);
    public static final BulletConfig DGK_NORMAL = runtime(LegacySednaBulletConfigs.DGK_NORMAL);
    public static final BulletConfig SHELL_NORMAL = runtime(LegacySednaBulletConfigs.SHELL_NORMAL);
    public static final BulletConfig SHELL_EXPLOSIVE = runtime(LegacySednaBulletConfigs.SHELL_EXPLOSIVE);
    public static final BulletConfig SHELL_AP = runtime(LegacySednaBulletConfigs.SHELL_AP);
    public static final BulletConfig SHELL_DU = runtime(LegacySednaBulletConfigs.SHELL_DU);
    public static final BulletConfig SHELL_W9 = runtime(LegacySednaBulletConfigs.SHELL_W9);
    public static final BulletConfig FEXT_WATER = runtime(LegacySednaBulletConfigs.FEXT_WATER);
    public static final BulletConfig FEXT_FOAM = runtime(LegacySednaBulletConfigs.FEXT_FOAM);
    public static final BulletConfig FEXT_SAND = runtime(LegacySednaBulletConfigs.FEXT_SAND);
    public static final BulletConfig CT_HOOK = runtime(LegacySednaBulletConfigs.CT_HOOK);
    public static final BulletConfig CT_MORTAR = runtime(LegacySednaBulletConfigs.CT_MORTAR);
    public static final BulletConfig CT_MORTAR_CHARGE = runtime(LegacySednaBulletConfigs.CT_MORTAR_CHARGE);

    private static final List<BulletConfig> ADDITIONAL_SYNCED = List.of(
            G10, G10_SHRAPNEL, G10_DU, G10_SLUG, G10_EXPLOSIVE,
            G12_EQUESTRIAN_BJ, G12_EQUESTRIAN_TKR,
            P22_SP, P22_FMJ, P22_JHP, P22_AP,
            P9_SP, P9_FMJ, P9_JHP, P9_AP,
            R556_SP, R556_FMJ, R556_JHP, R556_AP, R556_INC_SP, R556_INC_FMJ, R556_INC_JHP, R556_INC_AP,
            R762_SP, R762_FMJ, R762_JHP, R762_AP, R762_DU, R762_HE,
            M44_BP, M44_SP, M44_FMJ, M44_JHP, M44_AP, M44_EXPRESS,
            M44_EQUESTRIAN_PIP, M44_EQUESTRIAN_MN7,
            M357_BP, M357_SP, M357_FMJ, M357_JHP, M357_AP, M357_EXPRESS,
            P45_SP, P45_FMJ, P45_JHP, P45_AP, P45_DU,
            BMG50_SP, BMG50_FMJ, BMG50_JHP, BMG50_AP, BMG50_DU, BMG50_HE, BMG50_SM, BMG50_BLACK,
            BMG50_EQUESTRIAN,
            B75, B75_INC, B75_EXP,
            G26_FLARE, G26_FLARE_SUPPLY, G26_FLARE_WEAPON,
            G40_HE, G40_HEAT, G40_DEMO, G40_INC, G40_PHOSPHORUS,
            ROCKET_HE, ROCKET_HEAT, ROCKET_DEMO, ROCKET_INC, ROCKET_PHOSPHORUS,
            ROCKET_RPZB_HE, ROCKET_RPZB_HEAT, ROCKET_RPZB_DEMO, ROCKET_RPZB_INC,
            ROCKET_RPZB_PHOSPHORUS, ROCKET_QD_HE, ROCKET_QD_HEAT, ROCKET_QD_DEMO, ROCKET_QD_INC,
            ROCKET_QD_PHOSPHORUS, ROCKET_ML_HE, ROCKET_ML_HEAT, ROCKET_ML_DEMO, ROCKET_ML_INC,
            ROCKET_ML_PHOSPHORUS, ROCKET_NCRPA_STEER_HE, ROCKET_NCRPA_STEER_HEAT,
            ROCKET_NCRPA_STEER_DEMO, ROCKET_NCRPA_STEER_INC, ROCKET_NCRPA_STEER_PHOSPHORUS,
            ROCKET_NCRPA_HE, ROCKET_NCRPA_HEAT, ROCKET_NCRPA_DEMO, ROCKET_NCRPA_INC,
            ROCKET_NCRPA_PHOSPHORUS,
            FLAME_DIESEL, FLAME_GAS, FLAME_NAPALM, FLAME_BALEFIRE, FLAME_NOGRAV, FLAME_NOGRAV_BF,
            FLAME_TOPAZ_DIESEL, FLAME_TOPAZ_GAS, FLAME_TOPAZ_NAPALM, FLAME_TOPAZ_BALEFIRE,
            FLAME_DAYBREAKER_DIESEL, FLAME_DAYBREAKER_GAS, FLAME_DAYBREAKER_NAPALM,
            FLAME_DAYBREAKER_BALEFIRE,
            ENERGY_TESLA, ENERGY_TESLA_OVERCHARGE, ENERGY_TESLA_IR, ENERGY_TESLA_IR_SUB,
            BATTERY_SOCKET_DISCHARGE, ENERGY_LAS, ENERGY_LAS_OVERCHARGE, ENERGY_LAS_IR,
            ENERGY_EMERALD, ENERGY_EMERALD_OVERCHARGE, ENERGY_EMERALD_IR,
            ENERGY_LACUNAE, ENERGY_LACUNAE_OVERCHARGE, ENERGY_LACUNAE_IR,
            TAU_URANIUM, TAU_URANIUM_CHARGE, COIL_TUNGSTEN, COIL_FERROURANIUM, NI4NI_ARC,
            NUKE_STANDARD, NUKE_DEMO, NUKE_HIGH, NUKE_TOTS, NUKE_HIVE, NUKE_BALEFIRE,
            CLUSTER_SUBMUNITION, FOLLY_SM, FOLLY_NUKE, DEBUG, DEBUG_SHOT, GRENADE_FRAGMENTATION, GRENADE_PELLETS,
            GRENADE_PELLETS_HEAVY, GRENADE_LASER,
            STONE, FLINT, IRON, SHOT,
            G12_SUB, G12_SUB_SLUG, G12_SUB_FLECHETTE, G12_SUB_MAGNUM, G12_SUB_EXPLOSIVE,
            G12_SUB_PHOSPHORUS, G12_SHREDDER, G12_SHREDDER_SLUG, G12_SHREDDER_FLECHETTE,
            G12_SHREDDER_MAGNUM, G12_SHREDDER_EXPLOSIVE, G12_SHREDDER_PHOSPHORUS,
            P35800, P35800_BL, DGK_NORMAL, SHELL_NORMAL, SHELL_EXPLOSIVE, SHELL_AP, SHELL_DU,
            SHELL_W9, FEXT_WATER, FEXT_FOAM, FEXT_SAND, CT_HOOK, CT_MORTAR, CT_MORTAR_CHARGE);

    public static Optional<BulletConfig> byName(String legacyName) {
        return Optional.ofNullable(BY_NAME.get(legacyName));
    }

    public static Collection<BulletConfig> all() {
        return Collections.unmodifiableCollection(BY_NAME.values());
    }

    public static List<BulletConfig> allAdditionalSynced() {
        return ADDITIONAL_SYNCED;
    }

    private static BulletConfig runtime(SednaBulletConfig config) {
        BulletConfig built = applyLegacyImpactTuning(applyBehaviors(BulletConfig.builder(config.legacyName())
                .ammo(ammo(config))
                .ammoCount(config.ammoReloadCount())
                .damage(config.damageMultiplier(), config.damageMultiplier())
                .armor(config.armorThresholdNegation(), config.armorPiercingPercent())
                .knockback(config.knockbackMultiplier())
                .headshotMultiplier(config.headshotMultiplier())
                .damageFalloffByPenetration(config.damageFalloffByPenetration())
                .physics(config.gravity(), config.expires())
                .chunkloads(config.projectileType() == SednaBulletConfig.ProjectileType.BULLET_CHUNKLOADING)
                .impactsEntities(config.impactsEntities())
                .ricochet(usesRuntimeRicochet(config), config.ricochetAngle(), 100, 0, 1.0D)
                .maxRicochetCount(config.maxRicochetCount())
                .penetration(config.penetrates())
                .spectral(config.spectral())
                .breaksGlass(true)
                .selfDamageDelay(config.selfDamageDelay())
                .blackPowder(config.blackPowder())
                .blockDamage(true)
                .appearance(style(config), trail(config), plink(config), particle(config))
                .spentCasingName(config.spentCasingName())
                .casingItem(config.casingItemName(), config.casingItemStackSize(), config.casingItemAmount())
                .damageType(damageType(config.damageClass()))
                .damageFlags(damageProjectile(config.damageClass()), damageFire(config.damageClass()),
                        damageExplosion(config.damageClass()), damageBypass(config.damageClass())),
                config), config).ballistics(config.velocity(), config.spread(), Math.max(0, Math.round(config.wear())),
                config.projectilesMin(), config.projectilesMax()).build();
        BY_NAME.put(built.legacyName(), built);
        return built;
    }

    private static boolean usesRuntimeRicochet(SednaBulletConfig config) {
        return config.maxRicochetCount() > 0
                && (config.hasBehavior(SednaBehaviorTag.STANDARD_RICOCHET)
                || config.hasBehavior(SednaBehaviorTag.SHREDDER_RICOCHET));
    }

    private static BulletConfig replace(BulletConfig config) {
        BY_NAME.put(config.legacyName(), config);
        return config;
    }

    private static BulletAmmo ammo(SednaBulletConfig config) {
        return switch (config.ammoKind()) {
            case STANDARD -> BulletAmmo.legacyItem("ammo_standard_" + ammoKey(config));
            case SECRET -> BulletAmmo.legacyItem("ammo_secret_" + ammoKey(config));
            case ITEM -> BulletAmmo.legacyItem(config.ammoName().replace(':', '_'));
        };
    }

    private static String ammoKey(SednaBulletConfig config) {
        return config.ammoName().isBlank() ? config.legacyName() : config.ammoName().toLowerCase(java.util.Locale.ROOT);
    }

    private static BulletStyle style(SednaBulletConfig config) {
        if (config.projectileType() == SednaBulletConfig.ProjectileType.BEAM) {
            if (config.beamRendererName().contains("TAU")) {
                return BulletStyle.TAU;
            }
            return BulletStyle.BOLT;
        }
        String renderer = config.rendererName();
        if (renderer.contains("FLARE")) {
            return BulletStyle.NORMAL;
        }
        if (renderer.contains("FLECHETTE_BULLET") || renderer.contains("FRAGMENTATION")) {
            return BulletStyle.NORMAL;
        }
        if (renderer.contains("FLECHETTE")) {
            return BulletStyle.FLECHETTE;
        }
        if (renderer.contains("GRENADE") || renderer.contains("BOMB")
                || renderer.contains("FRAGMENTATION") || renderer.contains("MORTAR")) {
            return BulletStyle.GRENADE;
        }
        if (renderer.contains("RPZB") || renderer.contains("QD") || renderer.contains("ML")
                || renderer.contains("NUKE") || renderer.contains("HIVE")) {
            return BulletStyle.ROCKET;
        }
        if (renderer.contains("CT_HOOK")) {
            return BulletStyle.BOLT;
        }
        return BulletStyle.NORMAL;
    }

    private static int trail(SednaBulletConfig config) {
        String beam = config.beamRendererName();
        if (beam.contains("TAU_CHARGE")) {
            return 1;
        }
        if (beam.contains("TAU")) {
            return 0;
        }
        if (beam.contains("LASER_PURPLE")) {
            return BulletTrail.LACUNAE.legacyId();
        }
        if (beam.contains("LASER_EMERALD")) {
            return BulletTrail.WORM.legacyId();
        }
        if (beam.contains("LASER_CYAN")) {
            return BulletTrail.GLASS_CYAN.legacyId();
        }
        if (beam.contains("LASER_RED")) {
            return BulletTrail.LASER.legacyId();
        }
        if (beam.contains("BLACK_LIGHTNING")) {
            return BulletTrail.SEDNA_BLACK_LIGHTNING.legacyId();
        }
        if (beam.contains("LIGHTNING_SUB")) {
            return BulletTrail.SEDNA_LIGHTNING_SUB.legacyId();
        }
        if (beam.contains("LIGHTNING")) {
            return BulletTrail.SEDNA_LIGHTNING.legacyId();
        }
        if (beam.contains("CRACKLE")) {
            return BulletTrail.SEDNA_CRACKLE.legacyId();
        }
        if (beam.contains("NI4NI")) {
            return BulletTrail.SEDNA_NI4NI.legacyId();
        }
        if (beam.contains("FOLLY")) {
            return BulletTrail.SEDNA_FOLLY.legacyId();
        }
        String renderer = config.rendererName();
        if (renderer.contains("FLARE_WEAPON")) {
            return LegacySednaBulletAppearance.FLARE_WEAPON;
        }
        if (renderer.contains("FLARE_SUPPLY")) {
            return LegacySednaBulletAppearance.FLARE_SUPPLY;
        }
        if (renderer.contains("FLARE")) {
            return LegacySednaBulletAppearance.FLARE;
        }
        if (renderer.contains("NUKE_BALEFIRE")) {
            return LegacySednaBulletAppearance.MINI_NUKE_BALEFIRE;
        }
        if (renderer.contains("BIG_NUKE")) {
            return LegacySednaBulletAppearance.BIG_NUKE_MIRV;
        }
        if (renderer.contains("NUKE")) {
            return LegacySednaBulletAppearance.MINI_NUKE;
        }
        if (renderer.contains("HIVE")) {
            return LegacySednaBulletAppearance.HIVE_ROCKET;
        }
        if (renderer.contains("BOMB")) {
            return LegacySednaBulletAppearance.CLUSTER_BOMB;
        }
        if (renderer.contains("GRENADE")) {
            return LegacySednaBulletAppearance.GRENADE;
        }
        if (renderer.contains("CT_HOOK")) {
            return LegacySednaBulletAppearance.CHARGE_HOOK;
        }
        if (renderer.contains("CT_MORTAR_CHARGE")) {
            return LegacySednaBulletAppearance.CHARGE_MORTAR_CHARGE;
        }
        if (renderer.contains("CT_MORTAR")) {
            return LegacySednaBulletAppearance.CHARGE_MORTAR;
        }
        if (renderer.contains("FRAGMENTATION")) {
            return LegacySednaBulletAppearance.FRAGMENTATION;
        }
        if (renderer.contains("FLECHETTE_BULLET")) {
            return LegacySednaBulletAppearance.FLECHETTE;
        }
        if (renderer.contains("AP_BULLET")) {
            return LegacySednaBulletAppearance.AP;
        }
        if (renderer.contains("EXPRESS_BULLET")) {
            return LegacySednaBulletAppearance.EXPRESS;
        }
        if (renderer.contains("DU_BULLET")) {
            return LegacySednaBulletAppearance.DU;
        }
        if (renderer.contains("HE_BULLET")) {
            return LegacySednaBulletAppearance.HE;
        }
        if (renderer.contains("SM_BULLET")) {
            return LegacySednaBulletAppearance.SM;
        }
        if (renderer.contains("BLACK_BULLET")) {
            return LegacySednaBulletAppearance.BLACK;
        }
        if (renderer.contains("LEGENDARY_BULLET")) {
            return LegacySednaBulletAppearance.LEGENDARY;
        }
        if (renderer.contains("STANDARD_BULLET")) {
            return LegacySednaBulletAppearance.STANDARD;
        }
        if (renderer.contains("RPZB") || renderer.contains("QD") || renderer.contains("ML")) {
            return LegacySednaBulletAppearance.ROCKET_THRUST;
        }
        return 0;
    }

    private static BulletPlink plink(SednaBulletConfig config) {
        BulletStyle style = style(config);
        if (style == BulletStyle.GRENADE || style == BulletStyle.ROCKET) {
            return BulletPlink.GRENADE;
        }
        if (config.projectileType() == SednaBulletConfig.ProjectileType.BEAM) {
            return BulletPlink.ENERGY;
        }
        return BulletPlink.BULLET;
    }

    private static String particle(SednaBulletConfig config) {
        if (config.hasBehavior(SednaBehaviorTag.ROCKET_ACCELERATE)) {
            return "smoke";
        }
        if (config.hasBehavior(SednaBehaviorTag.BALEFIRE_VISUAL)) {
            return "reddust";
        }
        return "";
    }

    private static BulletConfig.Builder applyBehaviors(BulletConfig.Builder builder, SednaBulletConfig config) {
        for (SednaBehaviorTag behavior : config.behaviors()) {
            builder.behavior(mapBehavior(behavior));
        }
        return builder;
    }

    private static BulletConfig.Builder applyLegacyImpactTuning(BulletConfig.Builder builder,
            SednaBulletConfig config) {
        if (config.hasBehavior(SednaBehaviorTag.TINY_EXPLODE)) {
            return builder.explosive(tinyExplosiveRadius(config));
        }
        if (config.hasBehavior(SednaBehaviorTag.GRENADE_TINY_EXPLOSIVE_PELLET)) {
            return builder.explosive(1.5F);
        }
        if (config.hasBehavior(SednaBehaviorTag.STANDARD_EXPLODE)
                || config.hasBehavior(SednaBehaviorTag.TURRET_240_STANDARD_EXPLODE)) {
            return builder.explosive(standardExplosiveRadius(config));
        }
        if (config.hasBehavior(SednaBehaviorTag.HEAT_EXPLODE)) {
            return builder.explosive(3.5F);
        }
        if (config.hasBehavior(SednaBehaviorTag.DEMO_EXPLODE)) {
            return builder.explosive(5.0F);
        }
        if (config.hasBehavior(SednaBehaviorTag.INCENDIARY_EXPLODE)
                || config.hasBehavior(SednaBehaviorTag.PHOSPHORUS_EXPLODE)) {
            boolean phosphorus = config.hasBehavior(SednaBehaviorTag.PHOSPHORUS_EXPLODE);
            boolean rocket = config.hasBehavior(SednaBehaviorTag.ROCKET_ACCELERATE);
            return builder.explosive(3.0F).incendiaryTicks(rocket ? (phosphorus ? 30 : 15) : (phosphorus ? 20 : 10));
        }
        if (config.hasBehavior(SednaBehaviorTag.MINI_NUKE_STANDARD)) {
            return builder.explosive(10.0F);
        }
        if (config.hasBehavior(SednaBehaviorTag.MINI_NUKE_DEMO)) {
            return builder.explosive(15.0F).incendiaryTicks(5);
        }
        if (config.hasBehavior(SednaBehaviorTag.MINI_NUKE_HIGH)) {
            return builder.nuke(35);
        }
        if (config.hasBehavior(SednaBehaviorTag.MINI_NUKE_TINYTOT)
                || config.hasBehavior(SednaBehaviorTag.MINI_NUKE_HIVE)) {
            return builder.explosive(5.0F);
        }
        if (config.hasBehavior(SednaBehaviorTag.MINI_NUKE_BALEFIRE)) {
            return builder.explosive(10.0F).incendiaryTicks(5);
        }
        if (config.hasBehavior(SednaBehaviorTag.FOLLY_NUKE_IMPACT)) {
            return builder.nuke(100);
        }
        if (config.hasBehavior(SednaBehaviorTag.TURRET_240_VNT_EXPLODE)) {
            return builder.explosive(10.0F);
        }
        if (config.hasBehavior(SednaBehaviorTag.CHARGE_MORTAR_EXPLODE)) {
            return builder.explosive(5.0F);
        }
        if (config.hasBehavior(SednaBehaviorTag.CHARGE_MORTAR_CHARGE_EXPLODE)) {
            return builder.explosive(15.0F);
        }
        return builder;
    }

    private static float standardExplosiveRadius(SednaBulletConfig config) {
        if (config.hasBehavior(SednaBehaviorTag.TURRET_240_STANDARD_EXPLODE)) {
            return 10.0F;
        }
        if ("flame_daybreaker_napalm".equals(config.legacyName())) {
            return 7.5F;
        }
        return 5.0F;
    }

    private static float tinyExplosiveRadius(SednaBulletConfig config) {
        return switch (config.legacyName()) {
            case "g10_explosive", "r762_he" -> 1.5F;
            default -> 2.0F;
        };
    }

    private static BulletBehaviorTag mapBehavior(SednaBehaviorTag behavior) {
        return BulletBehaviorTag.valueOf(behavior.name());
    }

    private static ResourceKey<DamageType> damageType(DamageClass damageClass) {
        return ModDamageSources.damageClassKey(damageClass);
    }

    private static boolean damageProjectile(DamageClass damageClass) {
        return ModDamageSources.isProjectile(damageClass);
    }

    private static boolean damageFire(DamageClass damageClass) {
        return ModDamageSources.isFireDamage(damageClass);
    }

    private static boolean damageExplosion(DamageClass damageClass) {
        return ModDamageSources.isExplosion(damageClass);
    }

    private static boolean damageBypass(DamageClass damageClass) {
        return ModDamageSources.isUnblockable(damageClass);
    }

    private LegacySednaRuntimeBulletConfigs() {
    }
}
