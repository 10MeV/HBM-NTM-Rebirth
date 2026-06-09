package com.hbm.ntm.bullet;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class LegacySednaAmmoCatalog {
    private static final Map<String, AmmoEntry> BY_KEY = new LinkedHashMap<>();
    private static final Map<String, AmmoEntry> BY_ITEM_META = new LinkedHashMap<>();
    private static final Map<String, AmmoEntry> BY_LEGACY_NAME = new LinkedHashMap<>();

    public static final AmmoEntry STONE = standard("STONE", 0, 0);
    public static final AmmoEntry STONE_AP = standard("STONE_AP", 1, 1);
    public static final AmmoEntry STONE_IRON = standard("STONE_IRON", 2, 2);
    public static final AmmoEntry STONE_SHOT = standard("STONE_SHOT", 3, 3);
    public static final AmmoEntry M357_BP = standard("M357_BP", 4, 4);
    public static final AmmoEntry M357_SP = standard("M357_SP", 5, 5);
    public static final AmmoEntry M357_FMJ = standard("M357_FMJ", 6, 6);
    public static final AmmoEntry M357_JHP = standard("M357_JHP", 7, 7);
    public static final AmmoEntry M357_AP = standard("M357_AP", 8, 8);
    public static final AmmoEntry M357_EXPRESS = standard("M357_EXPRESS", 9, 9);
    public static final AmmoEntry M44_BP = standard("M44_BP", 10, 10);
    public static final AmmoEntry M44_SP = standard("M44_SP", 11, 11);
    public static final AmmoEntry M44_FMJ = standard("M44_FMJ", 12, 12);
    public static final AmmoEntry M44_JHP = standard("M44_JHP", 13, 13);
    public static final AmmoEntry M44_AP = standard("M44_AP", 14, 14);
    public static final AmmoEntry M44_EXPRESS = standard("M44_EXPRESS", 15, 15);
    public static final AmmoEntry P22_SP = standard("P22_SP", 16, 16);
    public static final AmmoEntry P22_FMJ = standard("P22_FMJ", 17, 17);
    public static final AmmoEntry P22_JHP = standard("P22_JHP", 18, 18);
    public static final AmmoEntry P22_AP = standard("P22_AP", 19, 19);
    public static final AmmoEntry P9_SP = standard("P9_SP", 20, 20);
    public static final AmmoEntry P9_FMJ = standard("P9_FMJ", 21, 21);
    public static final AmmoEntry P9_JHP = standard("P9_JHP", 22, 22);
    public static final AmmoEntry P9_AP = standard("P9_AP", 23, 23);
    public static final AmmoEntry R556_SP = standard("R556_SP", 24, 29);
    public static final AmmoEntry R556_FMJ = standard("R556_FMJ", 25, 30);
    public static final AmmoEntry R556_JHP = standard("R556_JHP", 26, 31);
    public static final AmmoEntry R556_AP = standard("R556_AP", 27, 32);
    public static final AmmoEntry R762_SP = standard("R762_SP", 28, 33);
    public static final AmmoEntry R762_FMJ = standard("R762_FMJ", 29, 34);
    public static final AmmoEntry R762_JHP = standard("R762_JHP", 30, 35);
    public static final AmmoEntry R762_AP = standard("R762_AP", 31, 36);
    public static final AmmoEntry R762_DU = standard("R762_DU", 32, 37);
    public static final AmmoEntry BMG50_SP = standard("BMG50_SP", 33, 39);
    public static final AmmoEntry BMG50_FMJ = standard("BMG50_FMJ", 34, 40);
    public static final AmmoEntry BMG50_JHP = standard("BMG50_JHP", 35, 41);
    public static final AmmoEntry BMG50_AP = standard("BMG50_AP", 36, 42);
    public static final AmmoEntry BMG50_DU = standard("BMG50_DU", 37, 43);
    public static final AmmoEntry B75 = standard("B75", 38, 46);
    public static final AmmoEntry B75_INC = standard("B75_INC", 39, 47);
    public static final AmmoEntry B75_EXP = standard("B75_EXP", 40, 48);
    public static final AmmoEntry G12_BP = standard("G12_BP", 41, 49);
    public static final AmmoEntry G12_BP_MAGNUM = standard("G12_BP_MAGNUM", 42, 50);
    public static final AmmoEntry G12_BP_SLUG = standard("G12_BP_SLUG", 43, 51);
    public static final AmmoEntry G12 = standard("G12", 44, 52);
    public static final AmmoEntry G12_SLUG = standard("G12_SLUG", 45, 53);
    public static final AmmoEntry G12_FLECHETTE = standard("G12_FLECHETTE", 46, 54);
    public static final AmmoEntry G12_MAGNUM = standard("G12_MAGNUM", 47, 55);
    public static final AmmoEntry G12_EXPLOSIVE = standard("G12_EXPLOSIVE", 48, 56);
    public static final AmmoEntry G12_PHOSPHORUS = standard("G12_PHOSPHORUS", 49, 57);
    public static final AmmoEntry G26_FLARE = standard("G26_FLARE", 50, 63);
    public static final AmmoEntry G26_FLARE_SUPPLY = standard("G26_FLARE_SUPPLY", 51, 64);
    public static final AmmoEntry G26_FLARE_WEAPON = standard("G26_FLARE_WEAPON", 52, 65);
    public static final AmmoEntry G40_HE = standard("G40_HE", 53, 66);
    public static final AmmoEntry G40_HEAT = standard("G40_HEAT", 54, 67);
    public static final AmmoEntry G40_DEMO = standard("G40_DEMO", 55, 68);
    public static final AmmoEntry G40_INC = standard("G40_INC", 56, 69);
    public static final AmmoEntry G40_PHOSPHORUS = standard("G40_PHOSPHORUS", 57, 70);
    public static final AmmoEntry ROCKET_HE = standard("ROCKET_HE", 58, 71);
    public static final AmmoEntry ROCKET_HEAT = standard("ROCKET_HEAT", 59, 72);
    public static final AmmoEntry ROCKET_DEMO = standard("ROCKET_DEMO", 60, 73);
    public static final AmmoEntry ROCKET_INC = standard("ROCKET_INC", 61, 74);
    public static final AmmoEntry ROCKET_PHOSPHORUS = standard("ROCKET_PHOSPHORUS", 62, 75);
    public static final AmmoEntry FLAME_DIESEL = standard("FLAME_DIESEL", 63, 76);
    public static final AmmoEntry FLAME_GAS = standard("FLAME_GAS", 64, 77);
    public static final AmmoEntry FLAME_NAPALM = standard("FLAME_NAPALM", 65, 78);
    public static final AmmoEntry FLAME_BALEFIRE = standard("FLAME_BALEFIRE", 66, 79);
    public static final AmmoEntry CAPACITOR = standard("CAPACITOR", 67, 80);
    public static final AmmoEntry CAPACITOR_OVERCHARGE = standard("CAPACITOR_OVERCHARGE", 68, 81);
    public static final AmmoEntry CAPACITOR_IR = standard("CAPACITOR_IR", 69, 82);
    public static final AmmoEntry TAU_URANIUM = standard("TAU_URANIUM", 70, 83);
    public static final AmmoEntry COIL_TUNGSTEN = standard("COIL_TUNGSTEN", 71, 84);
    public static final AmmoEntry COIL_FERROURANIUM = standard("COIL_FERROURANIUM", 72, 85);
    public static final AmmoEntry NUKE_STANDARD = standard("NUKE_STANDARD", 73, 86);
    public static final AmmoEntry NUKE_DEMO = standard("NUKE_DEMO", 74, 87);
    public static final AmmoEntry NUKE_HIGH = standard("NUKE_HIGH", 75, 88);
    public static final AmmoEntry NUKE_TOTS = standard("NUKE_TOTS", 76, 89);
    public static final AmmoEntry NUKE_HIVE = standard("NUKE_HIVE", 77, 90);
    public static final AmmoEntry G10 = standard("G10", 78, 58);
    public static final AmmoEntry G10_SHRAPNEL = standard("G10_SHRAPNEL", 79, 59);
    public static final AmmoEntry G10_DU = standard("G10_DU", 80, 60);
    public static final AmmoEntry G10_SLUG = standard("G10_SLUG", 81, 61);
    public static final AmmoEntry R762_HE = standard("R762_HE", 82, 38);
    public static final AmmoEntry BMG50_HE = standard("BMG50_HE", 83, 45);
    public static final AmmoEntry G10_EXPLOSIVE = standard("G10_EXPLOSIVE", 84, 62);
    public static final AmmoEntry P45_SP = standard("P45_SP", 85, 24);
    public static final AmmoEntry P45_FMJ = standard("P45_FMJ", 86, 25);
    public static final AmmoEntry P45_JHP = standard("P45_JHP", 87, 26);
    public static final AmmoEntry P45_AP = standard("P45_AP", 88, 27);
    public static final AmmoEntry P45_DU = standard("P45_DU", 89, 28);
    public static final AmmoEntry CT_HOOK = standard("CT_HOOK", 90, 92);
    public static final AmmoEntry CT_MORTAR = standard("CT_MORTAR", 91, 93);
    public static final AmmoEntry CT_MORTAR_CHARGE = standard("CT_MORTAR_CHARGE", 92, 94);
    public static final AmmoEntry NUKE_BALEFIRE = standard("NUKE_BALEFIRE", 93, 91);
    public static final AmmoEntry BMG50_SM = standard("BMG50_SM", 94, 44);

    public static final AmmoEntry FOLLY_SM = secret("FOLLY_SM", 0);
    public static final AmmoEntry FOLLY_NUKE = secret("FOLLY_NUKE", 1);
    public static final AmmoEntry M44_EQUESTRIAN = secret("M44_EQUESTRIAN", 2);
    public static final AmmoEntry G12_EQUESTRIAN = secret("G12_EQUESTRIAN", 3);
    public static final AmmoEntry BMG50_EQUESTRIAN = secret("BMG50_EQUESTRIAN", 4);
    public static final AmmoEntry P35_800 = secret("P35_800", 5);
    public static final AmmoEntry BMG50_BLACK = secret("BMG50_BLACK", 6);
    public static final AmmoEntry P35_800_BL = secret("P35_800_BL", 7);

    public static Optional<AmmoEntry> byName(String legacyName) {
        return Optional.ofNullable(BY_LEGACY_NAME.get(normalizeName(legacyName)));
    }

    public static Optional<AmmoEntry> byItemMeta(String legacyItemName, int metadata) {
        return Optional.ofNullable(BY_ITEM_META.get(itemMetaKey(legacyItemName, metadata)));
    }

    public static Collection<AmmoEntry> all() {
        return Collections.unmodifiableCollection(BY_KEY.values());
    }

    private static AmmoEntry standard(String legacyName, int ordinal, int creativeOrder) {
        return register(new AmmoEntry(SednaBulletConfig.AmmoKind.STANDARD, legacyName, "ammo_standard", ordinal,
                creativeOrder, false));
    }

    private static AmmoEntry secret(String legacyName, int ordinal) {
        return register(new AmmoEntry(SednaBulletConfig.AmmoKind.SECRET, legacyName, "ammo_secret", ordinal,
                -1, true));
    }

    private static AmmoEntry register(AmmoEntry entry) {
        BY_KEY.put(entry.key(), entry);
        BY_ITEM_META.put(itemMetaKey(entry.legacyItemName(), entry.metadata()), entry);
        BY_LEGACY_NAME.put(normalizeName(entry.legacyName()), entry);
        return entry;
    }

    private static String itemMetaKey(String legacyItemName, int metadata) {
        return (legacyItemName == null ? "" : legacyItemName) + ":" + metadata;
    }

    private static String normalizeName(String legacyName) {
        return legacyName == null ? "" : legacyName.trim().toUpperCase();
    }

    public record AmmoEntry(
            SednaBulletConfig.AmmoKind kind,
            String legacyName,
            String legacyItemName,
            int metadata,
            int creativeOrder,
            boolean hidden) {

        public AmmoEntry {
            kind = kind == null ? SednaBulletConfig.AmmoKind.STANDARD : kind;
            legacyName = legacyName == null ? "" : legacyName;
            legacyItemName = legacyItemName == null ? "" : legacyItemName;
        }

        public String key() {
            return kind.name() + ":" + legacyName;
        }

        public List<SednaBulletConfig> matchingConfigs() {
            return List.copyOf(LegacySednaBulletConfigs.byAmmo(kind, legacyName));
        }
    }

    private LegacySednaAmmoCatalog() {
    }
}
