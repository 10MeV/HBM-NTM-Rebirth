package com.hbm.ntm.bullet;

import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.item.WeaponModItem;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SednaWeaponModInstallManager {
    private SednaWeaponModInstallManager() {
    }

    public static ItemStack[] getUpgradeItems(ItemStack stack, int configIndex) {
        int[] ids = SednaWeaponModEvaluator.installedModIds(stack, configIndex);
        if (ids.length == 0) {
            return new ItemStack[0];
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (int id : ids) {
            ItemStack modStack = stackForId(id);
            if (!modStack.isEmpty()) {
                stacks.add(modStack);
            }
        }
        return stacks.toArray(ItemStack[]::new);
    }

    public static void install(ItemStack gunStack, int configIndex, ItemStack... mods) {
        MagazineSnapshot snapshot = saveMagState(gunStack, configIndex);
        uninstall(gunStack, configIndex);

        if (!(gunStack.getItem() instanceof SednaGunItem gunItem)) {
            return;
        }
        String owner = gunItem.gunConfig().legacyName();
        List<Integer> ids = new ArrayList<>();
        for (ItemStack mod : mods) {
            applicableModId(owner, mod).ifPresent(ids::add);
        }
        if (ids.isEmpty()) {
            return;
        }
        int[] modIds = ids.stream().mapToInt(Integer::intValue).toArray();
        SednaWeaponModEvaluator.sortByLegacyPriority(modIds);
        gunStack.getOrCreateTag().putIntArray(SednaWeaponModEvaluator.KEY_MOD_LIST + configIndex, modIds);
        restoreMagState(gunStack, configIndex, snapshot);
    }

    public static void uninstall(ItemStack gunStack, int configIndex) {
        CompoundTag tag = gunStack.getTag();
        if (tag != null) {
            tag.remove(SednaWeaponModEvaluator.KEY_MOD_LIST + configIndex);
        }
    }

    public static boolean isApplicable(ItemStack gunStack, ItemStack modStack, int configIndex, boolean checkMutex) {
        if (!(gunStack.getItem() instanceof SednaGunItem gunItem)) {
            return false;
        }
        Optional<Integer> newId = applicableModId(gunItem.gunConfig().legacyName(), modStack);
        if (newId.isEmpty()) {
            return false;
        }
        if (!checkMutex) {
            return true;
        }
        String newSlot = slotForId(newId.get());
        for (int installed : SednaWeaponModEvaluator.installedModIds(gunStack, configIndex)) {
            if (!newSlot.isEmpty() && newSlot.equals(slotForId(installed))) {
                return false;
            }
        }
        return true;
    }

    private static Optional<Integer> applicableModId(String owner, ItemStack modStack) {
        if (!(modStack.getItem() instanceof WeaponModItem modItem)) {
            return Optional.empty();
        }
        WeaponModItem.Spec spec = modItem.spec();
        return Optional.ofNullable(switch (spec.legacyFamily()) {
            case "weapon_mod_test" -> testId(spec.legacyEntry());
            case "weapon_mod_generic" -> genericId(owner, spec.legacyEntry());
            case "weapon_mod_special" -> specialId(owner, spec.legacyEntry());
            case "weapon_mod_caliber" -> caliberId(owner, spec.legacyEntry());
            default -> null;
        });
    }

    private static Integer testId(String entry) {
        return switch (entry) {
            case "firerate" -> SednaWeaponModEvaluator.ID_TEST_FIRERATE;
            case "damage" -> SednaWeaponModEvaluator.ID_TEST_DAMAGE;
            case "multi" -> SednaWeaponModEvaluator.ID_TEST_MULTI;
            case "override_2_5" -> SednaWeaponModEvaluator.ID_TEST_OVERRIDE_2_5;
            case "override_5" -> SednaWeaponModEvaluator.ID_TEST_OVERRIDE_5;
            case "override_7_5" -> SednaWeaponModEvaluator.ID_TEST_OVERRIDE_7_5;
            case "override_10" -> SednaWeaponModEvaluator.ID_TEST_OVERRIDE_10;
            case "override_12_5" -> SednaWeaponModEvaluator.ID_TEST_OVERRIDE_12_5;
            case "override_15" -> SednaWeaponModEvaluator.ID_TEST_OVERRIDE_15;
            case "override_20" -> SednaWeaponModEvaluator.ID_TEST_OVERRIDE_20;
            default -> null;
        };
    }

    private static Integer genericId(String owner, String entry) {
        if (owner(owner, "gun_pepperbox")) {
            return switch (entry) {
                case "iron_damage" -> SednaWeaponModEvaluator.ID_IRON_DAMAGE;
                case "iron_dura" -> SednaWeaponModEvaluator.ID_IRON_DURABILITY;
                default -> null;
            };
        }
        if (owner(owner, "gun_light_revolver", "gun_light_revolver_atlas", "gun_henry", "gun_henry_lincoln",
                "gun_greasegun", "gun_maresleg", "gun_maresleg_akimbo", "gun_flaregun")) {
            return genericPair(entry, "steel_damage", SednaWeaponModEvaluator.ID_STEEL_DAMAGE,
                    "steel_dura", SednaWeaponModEvaluator.ID_STEEL_DURABILITY);
        }
        if (owner(owner, "gun_am180", "gun_liberator", "gun_congolake", "gun_flamer", "gun_flamer_topaz")) {
            return genericPair(entry, "dura_damage", SednaWeaponModEvaluator.ID_DURA_DAMAGE,
                    "dura_dura", SednaWeaponModEvaluator.ID_DURA_DURABILITY);
        }
        if (owner(owner, "gun_heavy_revolver", "gun_carbine", "gun_uzi", "gun_uzi_akimbo", "gun_spas12",
                "gun_panzerschreck")) {
            return genericPair(entry, "desh_damage", SednaWeaponModEvaluator.ID_DESH_DAMAGE,
                    "desh_dura", SednaWeaponModEvaluator.ID_DESH_DURABILITY);
        }
        if (owner(owner, "gun_star_f", "gun_star_f_akimbo", "gun_g3", "gun_g3_zebra", "gun_mk108",
                "gun_chemthrower")) {
            return genericPair(entry, "wsteel_damage", SednaWeaponModEvaluator.ID_WSTEEL_DAMAGE,
                    "wsteel_dura", SednaWeaponModEvaluator.ID_WSTEEL_DURABILITY);
        }
        if (owner(owner, "gun_amat", "gun_m2", "gun_autoshotgun", "gun_autoshotgun_shredder", "gun_quadro")) {
            return genericPair(entry, "ferro_damage", SednaWeaponModEvaluator.ID_FERRO_DAMAGE,
                    "ferro_dura", SednaWeaponModEvaluator.ID_FERRO_DURABILITY);
        }
        if (owner(owner, "gun_lag", "gun_minigun", "gun_missile_launcher", "gun_tesla_cannon")) {
            return genericPair(entry, "tcalloy_damage", SednaWeaponModEvaluator.ID_TCALLOY_DAMAGE,
                    "tcalloy_dura", SednaWeaponModEvaluator.ID_TCALLOY_DURABILITY);
        }
        if (owner(owner, "gun_laser_pistol", "gun_laser_pistol_pew_pew", "gun_stg77", "gun_fatman", "gun_tau")) {
            return genericPair(entry, "bigmt_damage", SednaWeaponModEvaluator.ID_BIGMT_DAMAGE,
                    "bigmt_dura", SednaWeaponModEvaluator.ID_BIGMT_DURABILITY);
        }
        if (owner(owner, "gun_lasrifle")) {
            return genericPair(entry, "bronze_damage", SednaWeaponModEvaluator.ID_BRONZE_DAMAGE,
                    "bronze_dura", SednaWeaponModEvaluator.ID_BRONZE_DURABILITY);
        }
        return null;
    }

    private static Integer genericPair(String entry, String damageEntry, int damageId, String durabilityEntry,
            int durabilityId) {
        if (entry.equals(damageEntry)) {
            return damageId;
        }
        if (entry.equals(durabilityEntry)) {
            return durabilityId;
        }
        return null;
    }

    private static Integer specialId(String owner, String entry) {
        return switch (entry) {
            case "speedloader" -> owner(owner, "gun_liberator") ? SednaWeaponModEvaluator.ID_LIBERATOR_SPEEDLOADER : null;
            case "silencer" -> owner(owner, "gun_am180", "gun_uzi", "gun_uzi_akimbo", "gun_star_f",
                    "gun_star_f_akimbo", "gun_g3", "gun_amat") ? SednaWeaponModEvaluator.ID_SILENCER : null;
            case "scope" -> owner(owner, "gun_heavy_revolver", "gun_carbine", "gun_g3", "gun_mas36",
                    "gun_charge_thrower") ? SednaWeaponModEvaluator.ID_SCOPE : null;
            case "saw" -> sawId(owner);
            case "greasegun" -> owner(owner, "gun_greasegun") ? SednaWeaponModEvaluator.ID_GREASEGUN_CLEAN : null;
            case "slowdown" -> owner(owner, "gun_minigun", "gun_minigun_dual") ? SednaWeaponModEvaluator.ID_MINIGUN_SLOWDOWN : null;
            case "speedup" -> speedupId(owner);
            case "choke" -> owner(owner, "gun_pepperbox", "gun_maresleg", "gun_double_barrel", "gun_liberator",
                    "gun_spas12", "gun_autoshotgun_sexy", "gun_autoshotgun_heretic")
                    ? SednaWeaponModEvaluator.ID_CHOKE : null;
            case "furniture_green" -> owner(owner, "gun_g3") ? SednaWeaponModEvaluator.ID_FURNITURE_GREEN : null;
            case "furniture_black" -> owner(owner, "gun_g3") ? SednaWeaponModEvaluator.ID_FURNITURE_BLACK : null;
            case "bayonet" -> bayonetId(owner);
            case "stack_mag" -> owner(owner, "gun_greasegun", "gun_uzi", "gun_uzi_akimbo", "gun_aberrator",
                    "gun_aberrator_eott") ? SednaWeaponModEvaluator.ID_STACK_MAG : null;
            case "skin_saturnite" -> owner(owner, "gun_uzi", "gun_uzi_akimbo") ? SednaWeaponModEvaluator.ID_UZI_SATURN : null;
            case "las_shotgun" -> owner(owner, "gun_lasrifle") ? SednaWeaponModEvaluator.ID_LAS_SHOTGUN : null;
            case "las_capacitor" -> owner(owner, "gun_lasrifle") ? SednaWeaponModEvaluator.ID_LAS_CAPACITOR : null;
            case "las_auto" -> owner(owner, "gun_lasrifle") ? SednaWeaponModEvaluator.ID_LAS_AUTO : null;
            case "nickel" -> owner(owner, "gun_n_i_4_n_i") ? SednaWeaponModEvaluator.ID_NI4NI_NICKEL : null;
            case "doubloons" -> owner(owner, "gun_n_i_4_n_i") ? SednaWeaponModEvaluator.ID_NI4NI_DOUBLOONS : null;
            case "drill_hss" -> owner(owner, "gun_drill") ? SednaWeaponModEvaluator.ID_DRILL_HSS : null;
            case "drill_weaponsteel" -> owner(owner, "gun_drill") ? SednaWeaponModEvaluator.ID_DRILL_WSTEEL : null;
            case "drill_tcalloy" -> owner(owner, "gun_drill") ? SednaWeaponModEvaluator.ID_DRILL_TCALLOY : null;
            case "drill_saturnite" -> owner(owner, "gun_drill") ? SednaWeaponModEvaluator.ID_DRILL_SATURN : null;
            case "engine_diesel" -> owner(owner, "gun_drill") ? SednaWeaponModEvaluator.ID_ENGINE_DIESEL : null;
            case "engine_aviation" -> owner(owner, "gun_drill") ? SednaWeaponModEvaluator.ID_ENGINE_AVIATION : null;
            case "engine_electric" -> owner(owner, "gun_drill") ? SednaWeaponModEvaluator.ID_ENGINE_ELECTRIC : null;
            case "engine_turbo" -> owner(owner, "gun_drill") ? SednaWeaponModEvaluator.ID_ENGINE_TURBO : null;
            case "magnet" -> owner(owner, "gun_drill") ? SednaWeaponModEvaluator.ID_DRILL_MAGNET : null;
            case "sifter" -> owner(owner, "gun_drill") ? SednaWeaponModEvaluator.ID_DRILL_SIFTER : null;
            case "canisters" -> owner(owner, "gun_drill") ? SednaWeaponModEvaluator.ID_CANISTERS : null;
            default -> null;
        };
    }

    private static Integer sawId(String owner) {
        if (owner(owner, "gun_maresleg", "gun_double_barrel")) {
            return SednaWeaponModEvaluator.ID_SAWED_OFF;
        }
        if (owner(owner, "gun_panzerschreck")) {
            return SednaWeaponModEvaluator.ID_NO_SHIELD;
        }
        if (owner(owner, "gun_g3", "gun_g3_zebra")) {
            return SednaWeaponModEvaluator.ID_NO_STOCK;
        }
        return null;
    }

    private static Integer speedupId(String owner) {
        if (owner(owner, "gun_minigun", "gun_minigun_dual")) {
            return SednaWeaponModEvaluator.ID_MINIGUN_SPEED;
        }
        if (owner(owner, "gun_autoshotgun", "gun_autoshotgun_shredder", "gun_mk108")) {
            return SednaWeaponModEvaluator.ID_SHREDDER_SPEED;
        }
        return null;
    }

    private static Integer bayonetId(String owner) {
        if (owner(owner, "gun_mas36")) {
            return SednaWeaponModEvaluator.ID_MAS_BAYONET;
        }
        if (owner(owner, "gun_carbine")) {
            return SednaWeaponModEvaluator.ID_CARBINE_BAYONET;
        }
        return null;
    }

    private static Integer caliberId(String owner, String entry) {
        return switch (entry) {
            case "p9" -> owner(owner, "gun_henry") ? 300
                    : owner(owner, "gun_star_f") ? 301
                    : owner(owner, "gun_star_f_akimbo") ? 302 : null;
            case "p45" -> owner(owner, "gun_henry") ? 310
                    : owner(owner, "gun_greasegun") ? 311
                    : owner(owner, "gun_uzi") ? 312
                    : owner(owner, "gun_uzi_akimbo") ? 313
                    : owner(owner, "gun_lag") ? 314 : null;
            case "p22" -> owner(owner, "gun_henry") ? 320
                    : owner(owner, "gun_uzi") ? 321
                    : owner(owner, "gun_uzi_akimbo") ? 322 : null;
            case "m357" -> owner(owner, "gun_henry") ? 330 : owner(owner, "gun_lag") ? 331 : null;
            case "m44" -> owner(owner, "gun_lag") ? 340 : null;
            case "r556" -> owner(owner, "gun_henry") ? 350
                    : owner(owner, "gun_carbine") ? 351
                    : owner(owner, "gun_minigun", "gun_minigun_dual") ? 352 : null;
            case "r762" -> owner(owner, "gun_henry") ? 360 : owner(owner, "gun_g3") ? 361 : null;
            case "bmg50" -> owner(owner, "gun_henry") ? 370
                    : owner(owner, "gun_minigun", "gun_minigun_dual") ? 371 : null;
            default -> null;
        };
    }

    private static ItemStack stackForId(int id) {
        String name = switch (id) {
            case SednaWeaponModEvaluator.ID_TEST_FIRERATE -> "weapon_mod_test_firerate";
            case SednaWeaponModEvaluator.ID_TEST_DAMAGE -> "weapon_mod_test_damage";
            case SednaWeaponModEvaluator.ID_TEST_MULTI -> "weapon_mod_test_multi";
            case SednaWeaponModEvaluator.ID_TEST_OVERRIDE_2_5 -> "weapon_mod_test_override_2_5";
            case SednaWeaponModEvaluator.ID_TEST_OVERRIDE_5 -> "weapon_mod_test_override_5";
            case SednaWeaponModEvaluator.ID_TEST_OVERRIDE_7_5 -> "weapon_mod_test_override_7_5";
            case SednaWeaponModEvaluator.ID_TEST_OVERRIDE_10 -> "weapon_mod_test_override_10";
            case SednaWeaponModEvaluator.ID_TEST_OVERRIDE_12_5 -> "weapon_mod_test_override_12_5";
            case SednaWeaponModEvaluator.ID_TEST_OVERRIDE_15 -> "weapon_mod_test_override_15";
            case SednaWeaponModEvaluator.ID_TEST_OVERRIDE_20 -> "weapon_mod_test_override_20";
            case SednaWeaponModEvaluator.ID_IRON_DAMAGE -> "weapon_mod_generic_iron_damage";
            case SednaWeaponModEvaluator.ID_IRON_DURABILITY -> "weapon_mod_generic_iron_dura";
            case SednaWeaponModEvaluator.ID_STEEL_DAMAGE -> "weapon_mod_generic_steel_damage";
            case SednaWeaponModEvaluator.ID_STEEL_DURABILITY -> "weapon_mod_generic_steel_dura";
            case SednaWeaponModEvaluator.ID_DURA_DAMAGE -> "weapon_mod_generic_dura_damage";
            case SednaWeaponModEvaluator.ID_DURA_DURABILITY -> "weapon_mod_generic_dura_dura";
            case SednaWeaponModEvaluator.ID_DESH_DAMAGE -> "weapon_mod_generic_desh_damage";
            case SednaWeaponModEvaluator.ID_DESH_DURABILITY -> "weapon_mod_generic_desh_dura";
            case SednaWeaponModEvaluator.ID_WSTEEL_DAMAGE -> "weapon_mod_generic_wsteel_damage";
            case SednaWeaponModEvaluator.ID_WSTEEL_DURABILITY -> "weapon_mod_generic_wsteel_dura";
            case SednaWeaponModEvaluator.ID_FERRO_DAMAGE -> "weapon_mod_generic_ferro_damage";
            case SednaWeaponModEvaluator.ID_FERRO_DURABILITY -> "weapon_mod_generic_ferro_dura";
            case SednaWeaponModEvaluator.ID_TCALLOY_DAMAGE -> "weapon_mod_generic_tcalloy_damage";
            case SednaWeaponModEvaluator.ID_TCALLOY_DURABILITY -> "weapon_mod_generic_tcalloy_dura";
            case SednaWeaponModEvaluator.ID_BIGMT_DAMAGE -> "weapon_mod_generic_bigmt_damage";
            case SednaWeaponModEvaluator.ID_BIGMT_DURABILITY -> "weapon_mod_generic_bigmt_dura";
            case SednaWeaponModEvaluator.ID_BRONZE_DAMAGE -> "weapon_mod_generic_bronze_damage";
            case SednaWeaponModEvaluator.ID_BRONZE_DURABILITY -> "weapon_mod_generic_bronze_dura";
            case SednaWeaponModEvaluator.ID_LIBERATOR_SPEEDLOADER -> "weapon_mod_special_speedloader";
            case SednaWeaponModEvaluator.ID_SILENCER -> "weapon_mod_special_silencer";
            case SednaWeaponModEvaluator.ID_SCOPE -> "weapon_mod_special_scope";
            case SednaWeaponModEvaluator.ID_SAWED_OFF, SednaWeaponModEvaluator.ID_NO_SHIELD,
                    SednaWeaponModEvaluator.ID_NO_STOCK -> "weapon_mod_special_saw";
            case SednaWeaponModEvaluator.ID_GREASEGUN_CLEAN -> "weapon_mod_special_greasegun";
            case SednaWeaponModEvaluator.ID_MINIGUN_SLOWDOWN -> "weapon_mod_special_slowdown";
            case SednaWeaponModEvaluator.ID_MINIGUN_SPEED, SednaWeaponModEvaluator.ID_SHREDDER_SPEED ->
                    "weapon_mod_special_speedup";
            case SednaWeaponModEvaluator.ID_CHOKE -> "weapon_mod_special_choke";
            case SednaWeaponModEvaluator.ID_FURNITURE_GREEN -> "weapon_mod_special_furniture_green";
            case SednaWeaponModEvaluator.ID_FURNITURE_BLACK -> "weapon_mod_special_furniture_black";
            case SednaWeaponModEvaluator.ID_MAS_BAYONET, SednaWeaponModEvaluator.ID_CARBINE_BAYONET ->
                    "weapon_mod_special_bayonet";
            case SednaWeaponModEvaluator.ID_STACK_MAG -> "weapon_mod_special_stack_mag";
            case SednaWeaponModEvaluator.ID_UZI_SATURN -> "weapon_mod_special_skin_saturnite";
            case SednaWeaponModEvaluator.ID_LAS_SHOTGUN -> "weapon_mod_special_las_shotgun";
            case SednaWeaponModEvaluator.ID_LAS_CAPACITOR -> "weapon_mod_special_las_capacitor";
            case SednaWeaponModEvaluator.ID_LAS_AUTO -> "weapon_mod_special_las_auto";
            case SednaWeaponModEvaluator.ID_NI4NI_NICKEL -> "weapon_mod_special_nickel";
            case SednaWeaponModEvaluator.ID_NI4NI_DOUBLOONS -> "weapon_mod_special_doubloons";
            case SednaWeaponModEvaluator.ID_DRILL_HSS -> "weapon_mod_special_drill_hss";
            case SednaWeaponModEvaluator.ID_DRILL_WSTEEL -> "weapon_mod_special_drill_weaponsteel";
            case SednaWeaponModEvaluator.ID_DRILL_TCALLOY -> "weapon_mod_special_drill_tcalloy";
            case SednaWeaponModEvaluator.ID_DRILL_SATURN -> "weapon_mod_special_drill_saturnite";
            case SednaWeaponModEvaluator.ID_ENGINE_DIESEL -> "weapon_mod_special_engine_diesel";
            case SednaWeaponModEvaluator.ID_ENGINE_AVIATION -> "weapon_mod_special_engine_aviation";
            case SednaWeaponModEvaluator.ID_ENGINE_ELECTRIC -> "weapon_mod_special_engine_electric";
            case SednaWeaponModEvaluator.ID_ENGINE_TURBO -> "weapon_mod_special_engine_turbo";
            case SednaWeaponModEvaluator.ID_DRILL_MAGNET -> "weapon_mod_special_magnet";
            case SednaWeaponModEvaluator.ID_DRILL_SIFTER -> "weapon_mod_special_sifter";
            case SednaWeaponModEvaluator.ID_CANISTERS -> "weapon_mod_special_canisters";
            case 300, 301, 302 -> "weapon_mod_caliber_p9";
            case 310, 311, 312, 313, 314 -> "weapon_mod_caliber_p45";
            case 320, 321, 322 -> "weapon_mod_caliber_p22";
            case 330, 331 -> "weapon_mod_caliber_m357";
            case 340 -> "weapon_mod_caliber_m44";
            case 350, 351, 352 -> "weapon_mod_caliber_r556";
            case 360, 361 -> "weapon_mod_caliber_r762";
            case 370, 371 -> "weapon_mod_caliber_bmg50";
            default -> "";
        };
        RegistryObject<Item> item = name.isEmpty() ? null : ModItems.legacyItem(name);
        return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
    }

    private static String slotForId(int id) {
        return switch (id) {
            case SednaWeaponModEvaluator.ID_TEST_FIRERATE -> "FIRERATE";
            case SednaWeaponModEvaluator.ID_TEST_DAMAGE -> "DAMAGE";
            case SednaWeaponModEvaluator.ID_TEST_MULTI -> "MULTI";
            case SednaWeaponModEvaluator.ID_TEST_OVERRIDE_2_5, SednaWeaponModEvaluator.ID_TEST_OVERRIDE_5,
                    SednaWeaponModEvaluator.ID_TEST_OVERRIDE_7_5, SednaWeaponModEvaluator.ID_TEST_OVERRIDE_10,
                    SednaWeaponModEvaluator.ID_TEST_OVERRIDE_12_5, SednaWeaponModEvaluator.ID_TEST_OVERRIDE_15,
                    SednaWeaponModEvaluator.ID_TEST_OVERRIDE_20 -> "OVERRIDE";
            case SednaWeaponModEvaluator.ID_IRON_DAMAGE, SednaWeaponModEvaluator.ID_STEEL_DAMAGE,
                    SednaWeaponModEvaluator.ID_DURA_DAMAGE, SednaWeaponModEvaluator.ID_DESH_DAMAGE,
                    SednaWeaponModEvaluator.ID_WSTEEL_DAMAGE, SednaWeaponModEvaluator.ID_FERRO_DAMAGE,
                    SednaWeaponModEvaluator.ID_TCALLOY_DAMAGE, SednaWeaponModEvaluator.ID_BIGMT_DAMAGE,
                    SednaWeaponModEvaluator.ID_BRONZE_DAMAGE -> "GENERIC_DAMAGE";
            case SednaWeaponModEvaluator.ID_IRON_DURABILITY, SednaWeaponModEvaluator.ID_STEEL_DURABILITY,
                    SednaWeaponModEvaluator.ID_DURA_DURABILITY, SednaWeaponModEvaluator.ID_DESH_DURABILITY,
                    SednaWeaponModEvaluator.ID_WSTEEL_DURABILITY, SednaWeaponModEvaluator.ID_FERRO_DURABILITY,
                    SednaWeaponModEvaluator.ID_TCALLOY_DURABILITY, SednaWeaponModEvaluator.ID_BIGMT_DURABILITY,
                    SednaWeaponModEvaluator.ID_BRONZE_DURABILITY -> "GENERIC_DURABILITY";
            case SednaWeaponModEvaluator.ID_LIBERATOR_SPEEDLOADER -> "SPEEDLOADER";
            case SednaWeaponModEvaluator.ID_SILENCER -> "SILENCER";
            case SednaWeaponModEvaluator.ID_SCOPE -> "SCOPE";
            case SednaWeaponModEvaluator.ID_SAWED_OFF, SednaWeaponModEvaluator.ID_CHOKE,
                    SednaWeaponModEvaluator.ID_LAS_SHOTGUN -> "BARREL";
            case SednaWeaponModEvaluator.ID_NO_SHIELD -> "SHIELD";
            case SednaWeaponModEvaluator.ID_NO_STOCK, SednaWeaponModEvaluator.ID_GREASEGUN_CLEAN,
                    SednaWeaponModEvaluator.ID_FURNITURE_GREEN, SednaWeaponModEvaluator.ID_FURNITURE_BLACK,
                    SednaWeaponModEvaluator.ID_UZI_SATURN -> "FURNITURE";
            case SednaWeaponModEvaluator.ID_MINIGUN_SLOWDOWN, SednaWeaponModEvaluator.ID_MINIGUN_SPEED,
                    SednaWeaponModEvaluator.ID_SHREDDER_SPEED -> "SPEED";
            case SednaWeaponModEvaluator.ID_MAS_BAYONET, SednaWeaponModEvaluator.ID_CARBINE_BAYONET -> "BAYONET";
            case SednaWeaponModEvaluator.ID_STACK_MAG -> "MAG";
            case SednaWeaponModEvaluator.ID_LAS_CAPACITOR -> "UNDERBARREL";
            case SednaWeaponModEvaluator.ID_LAS_AUTO -> "RECEIVER";
            case SednaWeaponModEvaluator.ID_NI4NI_NICKEL -> "COIN1";
            case SednaWeaponModEvaluator.ID_NI4NI_DOUBLOONS -> "COIN2";
            case SednaWeaponModEvaluator.ID_DRILL_HSS, SednaWeaponModEvaluator.ID_DRILL_WSTEEL,
                    SednaWeaponModEvaluator.ID_DRILL_TCALLOY, SednaWeaponModEvaluator.ID_DRILL_SATURN -> "DRILL";
            case SednaWeaponModEvaluator.ID_ENGINE_DIESEL, SednaWeaponModEvaluator.ID_ENGINE_AVIATION,
                    SednaWeaponModEvaluator.ID_ENGINE_ELECTRIC, SednaWeaponModEvaluator.ID_ENGINE_TURBO -> "ENGINE";
            case SednaWeaponModEvaluator.ID_DRILL_MAGNET -> "MAGNET";
            case SednaWeaponModEvaluator.ID_DRILL_SIFTER -> "SIFTER";
            case SednaWeaponModEvaluator.ID_CANISTERS -> "CANISTERS";
            case 300, 301, 302, 310, 311, 312, 313, 314, 320, 321, 322, 330, 331, 340, 350, 351, 352,
                    360, 361, 370, 371 -> "CALIBER";
            default -> "";
        };
    }

    private static MagazineSnapshot saveMagState(ItemStack gunStack, int configIndex) {
        Optional<SednaMagazineConfig> magazine = primaryMagazine(gunStack, configIndex);
        if (magazine.isEmpty()) {
            return MagazineSnapshot.EMPTY;
        }
        SednaMagazineConfig mag = magazine.get();
        CompoundTag tag = gunStack.getTag();
        String type = tag == null || mag.nbtTypeKey().isEmpty() ? "" : tag.getString(mag.nbtTypeKey());
        int count = tag == null || mag.nbtCountKey().isEmpty() ? 0 : tag.getInt(mag.nbtCountKey());
        return new MagazineSnapshot(type, count);
    }

    private static void restoreMagState(ItemStack gunStack, int configIndex, MagazineSnapshot snapshot) {
        if (snapshot == MagazineSnapshot.EMPTY) {
            return;
        }
        Optional<SednaMagazineConfig> magazine = primaryMagazine(gunStack, configIndex);
        if (magazine.isEmpty()) {
            return;
        }
        SednaMagazineConfig mag = magazine.get();
        if (mag.nbtCountKey().isEmpty()) {
            return;
        }
        if (!snapshot.type().isEmpty() && !acceptsType(mag, snapshot.type())) {
            gunStack.getOrCreateTag().putInt(mag.nbtCountKey(), 0);
            return;
        }
        gunStack.getOrCreateTag().putInt(mag.nbtCountKey(), Math.min(snapshot.count(), mag.capacity()));
    }

    private static Optional<SednaMagazineConfig> primaryMagazine(ItemStack gunStack, int configIndex) {
        if (!(gunStack.getItem() instanceof SednaGunItem gunItem)) {
            return Optional.empty();
        }
        Optional<SednaGunConfig.GunModeConfig> mode = gunItem.gunConfig().configs().stream()
                .filter(candidate -> candidate.configIndex() == configIndex)
                .findFirst();
        if (mode.isEmpty() || mode.get().receivers().isEmpty()) {
            return Optional.empty();
        }
        return mode.get().receivers().get(0).magazine()
                .map(magazine -> SednaWeaponModEvaluator.effectiveMagazine(gunStack,
                        gunItem.gunConfig().legacyName(), configIndex, magazine));
    }

    private static boolean acceptsType(SednaMagazineConfig magazine, String type) {
        return magazine.acceptedBulletConfigNames().contains(type) || magazine.acceptedFluidNames().contains(type);
    }

    private static boolean owner(String owner, String... owners) {
        for (String candidate : owners) {
            if (candidate.equals(owner)) {
                return true;
            }
        }
        return false;
    }

    private record MagazineSnapshot(String type, int count) {
        private static final MagazineSnapshot EMPTY = new MagazineSnapshot("", 0);
    }
}
