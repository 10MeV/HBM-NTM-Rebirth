package com.hbm.ntm.item;

import net.minecraft.world.item.Item;

public class WeaponModItem extends Item {
    private final Spec spec;

    public WeaponModItem(Properties properties, Spec spec) {
        super(properties.stacksTo(1));
        this.spec = spec;
    }

    public Spec spec() {
        return spec;
    }

    public String texturePath() {
        return spec.legacyFamily() + "." + spec.legacyEntry();
    }

    @Override
    public String getDescriptionId() {
        return spec.translationKey();
    }

    public record Spec(String modernName, String legacyFamily, String legacyEntry, int legacyMeta,
            boolean creativeTab) {
        public static Spec test(String entry, int legacyMeta) {
            return new Spec("weapon_mod_test_" + entry, "weapon_mod_test", entry, legacyMeta, false);
        }

        public static Spec generic(String entry, int legacyMeta) {
            return new Spec("weapon_mod_generic_" + entry, "weapon_mod_generic", entry, legacyMeta, true);
        }

        public static Spec special(String entry, int legacyMeta) {
            return new Spec("weapon_mod_special_" + entry, "weapon_mod_special", entry, legacyMeta, true);
        }

        public static Spec caliber(String entry, int legacyMeta) {
            return new Spec("weapon_mod_caliber_" + entry, "weapon_mod_caliber", entry, legacyMeta, true);
        }

        public String legacyStackKey() {
            return legacyFamily + ":" + legacyEntry.toUpperCase();
        }

        public String translationKey() {
            return "item." + legacyFamily + "." + legacyEntry + ".name";
        }
    }
}
