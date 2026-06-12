package com.hbm.ntm.bullet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class SednaWeaponModEvaluator {
    public static final String KEY_MOD_LIST = "KEY_MOD_LIST_";

    public static final int ID_TEST_FIRERATE = 0;
    public static final int ID_TEST_DAMAGE = 1;
    public static final int ID_TEST_MULTI = 2;
    public static final int ID_TEST_OVERRIDE_2_5 = 3;
    public static final int ID_TEST_OVERRIDE_5 = 4;
    public static final int ID_TEST_OVERRIDE_7_5 = 5;
    public static final int ID_TEST_OVERRIDE_10 = 6;
    public static final int ID_TEST_OVERRIDE_12_5 = 7;
    public static final int ID_TEST_OVERRIDE_15 = 8;
    public static final int ID_TEST_OVERRIDE_20 = 9;
    public static final int ID_IRON_DAMAGE = 100;
    public static final int ID_IRON_DURABILITY = 101;
    public static final int ID_STEEL_DAMAGE = 102;
    public static final int ID_STEEL_DURABILITY = 103;
    public static final int ID_DURA_DAMAGE = 104;
    public static final int ID_DURA_DURABILITY = 105;
    public static final int ID_DESH_DAMAGE = 106;
    public static final int ID_DESH_DURABILITY = 107;
    public static final int ID_WSTEEL_DAMAGE = 108;
    public static final int ID_WSTEEL_DURABILITY = 109;
    public static final int ID_FERRO_DAMAGE = 110;
    public static final int ID_FERRO_DURABILITY = 111;
    public static final int ID_TCALLOY_DAMAGE = 112;
    public static final int ID_TCALLOY_DURABILITY = 113;
    public static final int ID_BIGMT_DAMAGE = 114;
    public static final int ID_BIGMT_DURABILITY = 115;
    public static final int ID_BRONZE_DAMAGE = 116;
    public static final int ID_BRONZE_DURABILITY = 117;

    public static final int ID_LIBERATOR_SPEEDLOADER = 200;
    public static final int ID_SILENCER = 201;
    public static final int ID_SCOPE = 202;
    public static final int ID_SAWED_OFF = 203;
    public static final int ID_NO_SHIELD = 204;
    public static final int ID_NO_STOCK = 205;
    public static final int ID_GREASEGUN_CLEAN = 206;
    public static final int ID_MINIGUN_SLOWDOWN = 207;
    public static final int ID_MINIGUN_SPEED = 208;
    public static final int ID_SHREDDER_SPEED = 209;
    public static final int ID_CHOKE = 210;
    public static final int ID_FURNITURE_GREEN = 211;
    public static final int ID_FURNITURE_BLACK = 212;
    public static final int ID_MAS_BAYONET = 213;
    public static final int ID_STACK_MAG = 214;
    public static final int ID_UZI_SATURN = 215;
    public static final int ID_LAS_SHOTGUN = 216;
    public static final int ID_LAS_CAPACITOR = 217;
    public static final int ID_LAS_AUTO = 218;
    public static final int ID_CARBINE_BAYONET = 219;
    public static final int ID_NI4NI_NICKEL = 220;
    public static final int ID_NI4NI_DOUBLOONS = 221;
    public static final int ID_DRILL_HSS = 222;
    public static final int ID_DRILL_WSTEEL = 223;
    public static final int ID_DRILL_TCALLOY = 224;
    public static final int ID_DRILL_SATURN = 225;
    public static final int ID_ENGINE_DIESEL = 226;
    public static final int ID_ENGINE_AVIATION = 227;
    public static final int ID_ENGINE_ELECTRIC = 228;
    public static final int ID_ENGINE_TURBO = 229;
    public static final int ID_DRILL_MAGNET = 230;
    public static final int ID_DRILL_SIFTER = 231;
    public static final int ID_CANISTERS = 232;

    public static int[] installedModIds(ItemStack stack, int configIndex) {
        CompoundTag tag = stack.getTag();
        int[] ids = tag == null ? new int[0] : tag.getIntArray(KEY_MOD_LIST + configIndex);
        sortByLegacyPriority(ids);
        return ids;
    }

    public static boolean hasUpgrade(ItemStack stack, int configIndex, int id) {
        for (int installed : installedModIds(stack, configIndex)) {
            if (installed == id) {
                return true;
            }
        }
        return false;
    }

    public static SednaMagazineConfig effectiveMagazine(ItemStack stack, String ownerName, int configIndex,
            SednaMagazineConfig base) {
        SednaMagazineConfig current = base;
        for (int id : installedModIds(stack, configIndex)) {
            current = applyMagazineMod(current, ownerName, id);
        }
        return current;
    }

    public static SednaGunConfig.GunModeConfig effectiveMode(ItemStack stack, String ownerName,
            SednaGunConfig.GunModeConfig mode) {
        float durability = mode.durability();
        int drawDuration = mode.drawDuration();
        int inspectDuration = mode.inspectDuration();
        boolean inspectCancel = mode.inspectCancel();
        SednaGunConfig.Crosshair crosshair = mode.crosshair();
        boolean hideCrosshair = mode.hideCrosshair();
        String scopeTexture = mode.scopeTexture();
        for (int id : installedModIds(stack, mode.configIndex())) {
            if (targetsDurabilityMod(id, ownerName)) {
                durability *= 2.0F;
                continue;
            }
            switch (id) {
                case ID_SCOPE -> {
                    if (owner(ownerName, "gun_heavy_revolver", "gun_carbine", "gun_g3", "gun_mas36",
                            "gun_charge_thrower")) {
                        scopeTexture = scopeTextureFor(ownerName);
                        hideCrosshair = true;
                    }
                }
                case ID_SAWED_OFF -> {
                    if (owner(ownerName, "gun_maresleg")) {
                        drawDuration = 5;
                    }
                }
                case ID_NO_SHIELD -> {
                    if (owner(ownerName, "gun_panzerschreck")) {
                        drawDuration = 5;
                    }
                }
                case ID_NO_STOCK -> {
                    if (owner(ownerName, "gun_g3", "gun_g3_zebra")) {
                        drawDuration = 5;
                    }
                }
                case ID_GREASEGUN_CLEAN -> {
                    if (owner(ownerName, "gun_greasegun")) {
                        durability *= 3.0F;
                    }
                }
                case ID_UZI_SATURN -> {
                    if (owner(ownerName, "gun_uzi", "gun_uzi_akimbo")) {
                        durability *= 5.0F;
                    }
                }
                case ID_LAS_SHOTGUN -> {
                    if (owner(ownerName, "gun_lasrifle")) {
                        crosshair = SednaGunConfig.Crosshair.L_CIRCLE;
                    }
                }
                case ID_LAS_AUTO -> {
                    if (owner(ownerName, "gun_lasrifle")) {
                        scopeTexture = "";
                    }
                }
                case ID_MAS_BAYONET -> {
                    if (owner(ownerName, "gun_mas36")) {
                        inspectDuration = 30;
                        inspectCancel = false;
                    }
                }
                case ID_CARBINE_BAYONET -> {
                    if (owner(ownerName, "gun_carbine")) {
                        inspectDuration = 30;
                        inspectCancel = false;
                    }
                }
                default -> {
                }
            }
        }
        if (durability == mode.durability()
                && drawDuration == mode.drawDuration()
                && inspectDuration == mode.inspectDuration()
                && inspectCancel == mode.inspectCancel()
                && crosshair == mode.crosshair()
                && hideCrosshair == mode.hideCrosshair()
                && scopeTexture.equals(mode.scopeTexture())) {
            return mode;
        }
        return copyMode(mode, durability, drawDuration, inspectDuration, inspectCancel, crosshair, hideCrosshair,
                scopeTexture);
    }

    public static SednaReceiverConfig effectiveReceiver(ItemStack stack, String ownerName, int configIndex,
            SednaReceiverConfig receiver) {
        float baseDamage = receiver.baseDamage();
        int delayAfterFire = receiver.delayAfterFire();
        int delayAfterDryFire = receiver.delayAfterDryFire();
        int roundsPerCycle = receiver.roundsPerCycle();
        float splitProjectiles = receiver.splitProjectiles();
        float spreadInnate = receiver.spreadInnate();
        float spreadAmmoMultiplier = receiver.spreadAmmoMultiplier();
        float spreadHipfire = receiver.spreadHipfire();
        boolean refireOnHold = receiver.refireOnHold();
        String fireSoundName = receiver.fireSoundName();
        for (int id : installedModIds(stack, configIndex)) {
            LegacySednaMagazineConfigs.MagazineModifier modifier = modifierForId(id);
            if (modifier != null && targetsOwner(modifier, ownerName)) {
                if (modifier.type() == LegacySednaMagazineConfigs.ModifierType.CALIBER_REPLACEMENT) {
                    baseDamage = modifier.replacementBaseDamage();
                } else if (id == ID_LAS_CAPACITOR) {
                    baseDamage *= 1.05F;
                } else if (id == ID_ENGINE_DIESEL) {
                    delayAfterFire = 15;
                } else if (id == ID_ENGINE_AVIATION) {
                    delayAfterFire = 10;
                } else if (id == ID_ENGINE_ELECTRIC) {
                    delayAfterFire = 15;
                } else if (id == ID_ENGINE_TURBO) {
                    delayAfterFire = 5;
                }
                continue;
            }
            if (!targetsReceiverMod(id, ownerName)) {
                continue;
            }
            switch (id) {
                case ID_TEST_FIRERATE -> delayAfterFire = Math.max(delayAfterFire / 2, 1);
                case ID_TEST_DAMAGE -> baseDamage *= 1.5F;
                case ID_TEST_MULTI -> roundsPerCycle *= 3;
                case ID_TEST_OVERRIDE_2_5 -> baseDamage = 2.5F;
                case ID_TEST_OVERRIDE_5 -> baseDamage = 5.0F;
                case ID_TEST_OVERRIDE_7_5 -> baseDamage = 7.5F;
                case ID_TEST_OVERRIDE_10 -> baseDamage = 10.0F;
                case ID_TEST_OVERRIDE_12_5 -> baseDamage = 125.0F;
                case ID_TEST_OVERRIDE_15 -> baseDamage = 15.0F;
                case ID_TEST_OVERRIDE_20 -> baseDamage = 20.0F;
                case ID_IRON_DAMAGE, ID_STEEL_DAMAGE, ID_DURA_DAMAGE, ID_DESH_DAMAGE, ID_WSTEEL_DAMAGE,
                        ID_FERRO_DAMAGE, ID_TCALLOY_DAMAGE, ID_BIGMT_DAMAGE, ID_BRONZE_DAMAGE ->
                        baseDamage *= 1.15F;
                case ID_SILENCER -> fireSoundName = ownerName.equals("gun_amat")
                        ? "NTMSounds.GUN_AMAT_SILENCER"
                        : "NTMSounds.GUN_RIFLE_SILENCER";
                case ID_SAWED_OFF -> {
                    spreadInnate = Math.max(0.025F, spreadInnate);
                    spreadAmmoMultiplier *= 1.5F;
                    baseDamage *= 1.35F;
                }
                case ID_GREASEGUN_CLEAN -> {
                    baseDamage += 2.0F;
                    spreadInnate = 0.0F;
                    delayAfterFire /= 2;
                }
                case ID_MINIGUN_SLOWDOWN -> {
                    delayAfterFire *= 2;
                    spreadInnate = 0.0F;
                }
                case ID_MINIGUN_SPEED -> {
                    roundsPerCycle *= 3;
                    spreadInnate *= 1.5F;
                }
                case ID_SHREDDER_SPEED -> {
                    delayAfterFire /= 2;
                    delayAfterDryFire /= 2;
                }
                case ID_CHOKE -> spreadAmmoMultiplier *= 0.5F;
                case ID_UZI_SATURN -> baseDamage += 3.0F;
                case ID_LAS_SHOTGUN -> {
                    baseDamage *= 0.35F;
                    splitProjectiles *= 3.0F;
                    spreadInnate += 3.0F;
                    spreadHipfire = 0.0F;
                }
                case ID_LAS_AUTO -> {
                    baseDamage *= 0.66F;
                    refireOnHold = true;
                    delayAfterFire = 5;
                }
                case ID_DRILL_HSS -> baseDamage *= 1.25F;
                case ID_DRILL_WSTEEL -> baseDamage *= 1.5F;
                case ID_DRILL_TCALLOY -> baseDamage *= 2.0F;
                case ID_DRILL_SATURN -> baseDamage *= 3.0F;
                default -> {
                }
            }
        }
        if (baseDamage == receiver.baseDamage()
                && delayAfterFire == receiver.delayAfterFire()
                && delayAfterDryFire == receiver.delayAfterDryFire()
                && roundsPerCycle == receiver.roundsPerCycle()
                && splitProjectiles == receiver.splitProjectiles()
                && spreadInnate == receiver.spreadInnate()
                && spreadAmmoMultiplier == receiver.spreadAmmoMultiplier()
                && spreadHipfire == receiver.spreadHipfire()
                && refireOnHold == receiver.refireOnHold()
                && fireSoundName.equals(receiver.fireSoundName())) {
            return receiver;
        }
        return copyReceiver(receiver, baseDamage, delayAfterFire, delayAfterDryFire, roundsPerCycle, splitProjectiles,
                spreadInnate, spreadAmmoMultiplier, spreadHipfire, refireOnHold, fireSoundName);
    }

    public static DrillStats effectiveDrillStats(ItemStack stack, int configIndex, DrillStats base) {
        DrillStats stats = base;
        for (int id : installedModIds(stack, configIndex)) {
            stats = switch (id) {
                case ID_DRILL_HSS -> stats.withCombat(3.0F, 0.15F)
                        .withHarvestLevel(3);
                case ID_DRILL_WSTEEL -> stats.withCombat(5.0F, 0.2F)
                        .withArea(2)
                        .withHarvestLevel(3);
                case ID_DRILL_TCALLOY -> stats.withReach(stats.reach() * 2.0D)
                        .withCombat(7.5F, 0.2F)
                        .withArea(3)
                        .withHarvestLevel(4);
                case ID_DRILL_SATURN -> stats.withReach(stats.reach() * 2.0D)
                        .withCombat(10.0F, 0.25F)
                        .withArea(3)
                        .withHarvestLevel(5);
                case ID_DRILL_MAGNET -> stats.withFortuneBonus(stats.fortuneBonus() + 2);
                case ID_DRILL_SIFTER -> stats.withFortuneBonus(stats.fortuneBonus() + 1);
                default -> stats;
            };
        }
        return stats;
    }

    private static SednaMagazineConfig applyMagazineMod(SednaMagazineConfig current, String ownerName, int id) {
        LegacySednaMagazineConfigs.MagazineModifier modifier = modifierForId(id);
        if (modifier == null || !targetsOwner(modifier, ownerName)) {
            return current;
        }
        return switch (modifier.type()) {
            case REPLACE_MAGAZINE -> LegacySednaMagazineConfigs.byKey(modifier.replacementMagazineKey())
                    .orElse(current);
            case CAPACITY_MULTIPLIER -> withCapacity(current,
                    current.capacity() * modifier.capacityNumerator() / modifier.capacityDenominator());
            case CALIBER_REPLACEMENT -> withCapacityAndAcceptedBullets(current,
                    effectiveCaliberCapacity(current, modifier), modifier.acceptedBulletConfigNames());
        };
    }

    private static int effectiveCaliberCapacity(SednaMagazineConfig current,
            LegacySednaMagazineConfigs.MagazineModifier modifier) {
        if (current.kind() == SednaMagazineConfig.Kind.BELT) {
            return current.capacity();
        }
        return modifier.replacementCapacity();
    }

    private static boolean targetsOwner(LegacySednaMagazineConfigs.MagazineModifier modifier, String ownerName) {
        return modifier.targetOwnerNames().contains(ownerName);
    }

    private static boolean targetsReceiverMod(int id, String ownerName) {
        return switch (id) {
            case ID_TEST_FIRERATE, ID_TEST_DAMAGE, ID_TEST_MULTI, ID_TEST_OVERRIDE_2_5, ID_TEST_OVERRIDE_5,
                    ID_TEST_OVERRIDE_7_5, ID_TEST_OVERRIDE_10, ID_TEST_OVERRIDE_12_5, ID_TEST_OVERRIDE_15,
                    ID_TEST_OVERRIDE_20 -> true;
            case ID_IRON_DAMAGE -> owner(ownerName, "gun_pepperbox");
            case ID_STEEL_DAMAGE -> owner(ownerName, "gun_light_revolver", "gun_light_revolver_atlas",
                    "gun_henry", "gun_henry_lincoln", "gun_greasegun", "gun_maresleg", "gun_maresleg_akimbo",
                    "gun_flaregun");
            case ID_DURA_DAMAGE -> owner(ownerName, "gun_am180", "gun_liberator", "gun_congolake",
                    "gun_flamer", "gun_flamer_topaz");
            case ID_DESH_DAMAGE -> owner(ownerName, "gun_heavy_revolver", "gun_carbine", "gun_uzi",
                    "gun_uzi_akimbo", "gun_spas12", "gun_panzerschreck");
            case ID_WSTEEL_DAMAGE -> owner(ownerName, "gun_star_f", "gun_star_f_akimbo", "gun_g3",
                    "gun_g3_zebra", "gun_mk108", "gun_chemthrower");
            case ID_FERRO_DAMAGE -> owner(ownerName, "gun_amat", "gun_m2", "gun_autoshotgun",
                    "gun_autoshotgun_shredder", "gun_quadro");
            case ID_TCALLOY_DAMAGE -> owner(ownerName, "gun_lag", "gun_minigun", "gun_missile_launcher",
                    "gun_tesla_cannon");
            case ID_BIGMT_DAMAGE -> owner(ownerName, "gun_laser_pistol", "gun_laser_pistol_pew_pew",
                    "gun_stg77", "gun_fatman", "gun_tau");
            case ID_BRONZE_DAMAGE, ID_LAS_SHOTGUN, ID_LAS_AUTO -> owner(ownerName, "gun_lasrifle");
            case ID_SILENCER -> owner(ownerName, "gun_am180", "gun_uzi", "gun_uzi_akimbo", "gun_star_f",
                    "gun_star_f_akimbo", "gun_g3", "gun_amat");
            case ID_SAWED_OFF -> owner(ownerName, "gun_maresleg", "gun_double_barrel");
            case ID_GREASEGUN_CLEAN -> owner(ownerName, "gun_greasegun");
            case ID_MINIGUN_SLOWDOWN, ID_MINIGUN_SPEED -> owner(ownerName, "gun_minigun", "gun_minigun_dual");
            case ID_SHREDDER_SPEED -> owner(ownerName, "gun_autoshotgun", "gun_autoshotgun_shredder",
                    "gun_mk108");
            case ID_CHOKE -> owner(ownerName, "gun_pepperbox", "gun_maresleg", "gun_double_barrel",
                    "gun_liberator", "gun_spas12", "gun_autoshotgun_sexy", "gun_autoshotgun_heretic");
            case ID_UZI_SATURN -> owner(ownerName, "gun_uzi", "gun_uzi_akimbo");
            case ID_DRILL_HSS, ID_DRILL_WSTEEL, ID_DRILL_TCALLOY, ID_DRILL_SATURN -> owner(ownerName,
                    "gun_drill");
            default -> false;
        };
    }

    private static boolean owner(String ownerName, String... owners) {
        for (String owner : owners) {
            if (owner.equals(ownerName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean targetsDurabilityMod(int id, String ownerName) {
        return switch (id) {
            case ID_IRON_DURABILITY -> owner(ownerName, "gun_pepperbox");
            case ID_STEEL_DURABILITY -> owner(ownerName, "gun_light_revolver", "gun_light_revolver_atlas",
                    "gun_henry", "gun_henry_lincoln", "gun_greasegun", "gun_maresleg", "gun_maresleg_akimbo",
                    "gun_flaregun");
            case ID_DURA_DURABILITY -> owner(ownerName, "gun_am180", "gun_liberator", "gun_congolake",
                    "gun_flamer", "gun_flamer_topaz");
            case ID_DESH_DURABILITY -> owner(ownerName, "gun_heavy_revolver", "gun_carbine", "gun_uzi",
                    "gun_uzi_akimbo", "gun_spas12", "gun_panzerschreck");
            case ID_WSTEEL_DURABILITY -> owner(ownerName, "gun_star_f", "gun_star_f_akimbo", "gun_g3",
                    "gun_g3_zebra", "gun_mk108", "gun_chemthrower");
            case ID_FERRO_DURABILITY -> owner(ownerName, "gun_amat", "gun_m2", "gun_autoshotgun",
                    "gun_autoshotgun_shredder", "gun_quadro");
            case ID_TCALLOY_DURABILITY -> owner(ownerName, "gun_lag", "gun_minigun", "gun_missile_launcher",
                    "gun_tesla_cannon");
            case ID_BIGMT_DURABILITY -> owner(ownerName, "gun_laser_pistol", "gun_laser_pistol_pew_pew",
                    "gun_stg77", "gun_fatman", "gun_tau");
            case ID_BRONZE_DURABILITY -> owner(ownerName, "gun_lasrifle");
            default -> false;
        };
    }

    private static String scopeTextureFor(String ownerName) {
        if (ownerName.equals("gun_heavy_revolver")) {
            return "hbm:textures/misc/scope_44.png";
        }
        if (ownerName.equals("gun_charge_thrower")) {
            return "hbm:textures/misc/scope_tool.png";
        }
        return "hbm:textures/misc/scope_bolt.png";
    }

    private static LegacySednaMagazineConfigs.MagazineModifier modifierForId(int id) {
        return switch (id) {
            case ID_LIBERATOR_SPEEDLOADER -> modifier("weapon_mod_liberator_speedloader");
            case ID_STACK_MAG -> modifier("weapon_mod_stack_mag");
            case ID_LAS_CAPACITOR -> modifier("weapon_mod_las_capacitor");
            case ID_ENGINE_DIESEL -> modifier("weapon_mod_engine_diesel");
            case ID_ENGINE_AVIATION -> modifier("weapon_mod_engine_aviation");
            case ID_ENGINE_ELECTRIC -> modifier("weapon_mod_engine_electric");
            case ID_ENGINE_TURBO -> modifier("weapon_mod_engine_turbo");
            case ID_CANISTERS -> modifier("weapon_mod_canisters");
            case 300 -> modifier("caliber_p9_henry");
            case 301 -> modifier("caliber_p9_star_f");
            case 302 -> modifier("caliber_p9_star_f_akimbo");
            case 310 -> modifier("caliber_p45_henry");
            case 311 -> modifier("caliber_p45_greasegun");
            case 312 -> modifier("caliber_p45_uzi");
            case 313 -> modifier("caliber_p45_uzi_akimbo");
            case 314 -> modifier("caliber_p45_lag");
            case 320 -> modifier("caliber_p22_henry");
            case 321 -> modifier("caliber_p22_uzi");
            case 322 -> modifier("caliber_p22_uzi_akimbo");
            case 330 -> modifier("caliber_m357_henry");
            case 331 -> modifier("caliber_m357_lag");
            case 340 -> modifier("caliber_m44_lag");
            case 350 -> modifier("caliber_r556_henry");
            case 351 -> modifier("caliber_r556_carbine");
            case 352 -> modifier("caliber_r556_miniguns");
            case 360 -> modifier("caliber_r762_henry");
            case 361 -> modifier("caliber_r762_g3");
            case 370 -> modifier("caliber_bmg50_henry");
            case 371 -> modifier("caliber_bmg50_miniguns");
            default -> null;
        };
    }

    private static void sortByLegacyPriority(int[] ids) {
        for (int i = 1; i < ids.length; i++) {
            int id = ids[i];
            int j = i - 1;
            while (j >= 0 && priority(ids[j]) < priority(id)) {
                ids[j + 1] = ids[j];
                j--;
            }
            ids[j + 1] = id;
        }
    }

    private static int priority(int id) {
        if (isCaliber(id) || id == ID_ENGINE_DIESEL || id == ID_ENGINE_AVIATION
                || id == ID_ENGINE_ELECTRIC || id == ID_ENGINE_TURBO || id == ID_LAS_AUTO
                || isTestOverride(id) || isDrillHead(id)) {
            return 1_000_000;
        }
        if (id == ID_LAS_CAPACITOR || id == ID_LAS_SHOTGUN || isGenericDamage(id)) {
            return 1_000;
        }
        if (id == ID_GREASEGUN_CLEAN || id == ID_UZI_SATURN) {
            return 500;
        }
        if (id == ID_DRILL_MAGNET || id == ID_DRILL_SIFTER) {
            return 500;
        }
        if (id == ID_CANISTERS || id == ID_TEST_FIRERATE || id == ID_TEST_DAMAGE || id == ID_TEST_MULTI) {
            return -1;
        }
        return 0;
    }

    private static boolean isCaliber(int id) {
        return id == 300 || id == 301 || id == 302
                || id == 310 || id == 311 || id == 312 || id == 313 || id == 314
                || id == 320 || id == 321 || id == 322
                || id == 330 || id == 331
                || id == 340
                || id == 350 || id == 351 || id == 352
                || id == 360 || id == 361
                || id == 370 || id == 371;
    }

    private static boolean isGenericDamage(int id) {
        return id == ID_IRON_DAMAGE || id == ID_STEEL_DAMAGE || id == ID_DURA_DAMAGE || id == ID_DESH_DAMAGE
                || id == ID_WSTEEL_DAMAGE || id == ID_FERRO_DAMAGE || id == ID_TCALLOY_DAMAGE
                || id == ID_BIGMT_DAMAGE || id == ID_BRONZE_DAMAGE;
    }

    private static boolean isTestOverride(int id) {
        return id >= ID_TEST_OVERRIDE_2_5 && id <= ID_TEST_OVERRIDE_20;
    }

    private static boolean isDrillHead(int id) {
        return id == ID_DRILL_HSS || id == ID_DRILL_WSTEEL || id == ID_DRILL_TCALLOY || id == ID_DRILL_SATURN;
    }

    private static LegacySednaMagazineConfigs.MagazineModifier modifier(String key) {
        return LegacySednaMagazineConfigs.modifier(key).orElse(null);
    }

    private static SednaMagazineConfig withCapacity(SednaMagazineConfig base, int capacity) {
        return withCapacityAndAcceptedBullets(base, capacity, base.acceptedBulletConfigNames());
    }

    private static SednaMagazineConfig withCapacityAndAcceptedBullets(SednaMagazineConfig base, int capacity,
            List<String> acceptedBulletConfigNames) {
        return new SednaMagazineConfig(base.legacyKey(), base.legacyOwnerName(), base.sourceClassName(), base.kind(),
                base.index(), capacity, acceptedBulletConfigNames, base.acceptedFluidNames(), base.amountUnit(),
                base.hudStyle(), base.consumptionMode(), base.nbtTypeKey(), base.nbtCountKey(),
                base.nbtBeforeReloadKey(), base.nbtAfterReloadKey(), base.acceptsAmmoBag(),
                base.acceptsInfiniteAmmoBag(), base.returnsCasingsToBag(), base.affectedByTrenchmaster(),
                base.notes());
    }

    private static SednaReceiverConfig copyReceiver(SednaReceiverConfig base, float baseDamage, int delayAfterFire,
            int delayAfterDryFire, int roundsPerCycle, float splitProjectiles, float spreadInnate,
            float spreadAmmoMultiplier, float spreadHipfire, boolean refireOnHold, String fireSoundName) {
        return new SednaReceiverConfig(base.legacyKey(), base.receiverIndex(), base.sourceClassName(),
                base.magazineKey(), baseDamage, delayAfterFire, delayAfterDryFire, roundsPerCycle,
                splitProjectiles, spreadInnate, spreadAmmoMultiplier, spreadHipfire,
                base.spreadDurability(), refireOnHold, base.refireAfterDry(), base.doesDryFire(),
                base.doesDryFireAfterAuto(), base.ejectOnFire(), base.reloadOnEmpty(),
                base.reloadCockOnEmptyPre(), base.reloadBeginDuration(), base.reloadCycleDuration(),
                base.reloadEndDuration(), base.reloadCockOnEmptyPost(), base.jamDuration(), fireSoundName,
                base.fireVolume(), base.firePitch(), base.projectileOffset(), base.projectileOffsetScoped(),
                base.canFireHandlerName(), base.fireHandlerName(), base.recoilHandlerName(), base.notes());
    }

    private static SednaGunConfig.GunModeConfig copyMode(SednaGunConfig.GunModeConfig base, float durability,
            int drawDuration, int inspectDuration, boolean inspectCancel, SednaGunConfig.Crosshair crosshair,
            boolean hideCrosshair, String scopeTexture) {
        return new SednaGunConfig.GunModeConfig(base.configIndex(), durability, drawDuration, inspectDuration,
                inspectCancel, crosshair, hideCrosshair, base.thermalSights(), base.reloadRequiresTypeChange(),
                base.reloadAnimationsSequential(), scopeTexture, base.smokeHandlerName(), base.orchestraName(),
                base.pressPrimaryHandlerName(), base.pressSecondaryHandlerName(), base.pressTertiaryHandlerName(),
                base.pressReloadHandlerName(), base.releasePrimaryHandlerName(), base.releaseSecondaryHandlerName(),
                base.releaseTertiaryHandlerName(), base.releaseReloadHandlerName(), base.deciderName(),
                base.animationProfileName(), base.hudComponentNames(), base.receivers(), base.notes());
    }

    public record DrillStats(
            double reach,
            float dtNegation,
            float piercing,
            int area,
            int harvestLevel,
            int fortuneBonus) {

        public DrillStats {
            reach = Math.max(0.0D, reach);
            dtNegation = Math.max(0.0F, dtNegation);
            piercing = Math.max(0.0F, piercing);
            area = Math.max(0, area);
            harvestLevel = Math.max(0, harvestLevel);
            fortuneBonus = Math.max(0, fortuneBonus);
        }

        private DrillStats withReach(double reach) {
            return new DrillStats(reach, dtNegation, piercing, area, harvestLevel, fortuneBonus);
        }

        private DrillStats withCombat(float dtNegation, float piercing) {
            return new DrillStats(reach, dtNegation, piercing, area, harvestLevel, fortuneBonus);
        }

        private DrillStats withArea(int area) {
            return new DrillStats(reach, dtNegation, piercing, area, harvestLevel, fortuneBonus);
        }

        private DrillStats withHarvestLevel(int harvestLevel) {
            return new DrillStats(reach, dtNegation, piercing, area, harvestLevel, fortuneBonus);
        }

        private DrillStats withFortuneBonus(int fortuneBonus) {
            return new DrillStats(reach, dtNegation, piercing, area, harvestLevel, fortuneBonus);
        }
    }

    private SednaWeaponModEvaluator() {
    }
}
