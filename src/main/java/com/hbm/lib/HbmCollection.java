package com.hbm.lib;

import com.hbm.ntm.util.HbmTranslationKeys;

/**
 * Legacy 1.7.10 package bridge for frequently reused translation keys.
 */
@Deprecated(forRemoval = false)
public final class HbmCollection {
    public static final String ammo = HbmTranslationKeys.AMMO;
    public static final String ammoMag = HbmTranslationKeys.AMMO_MAG;
    public static final String ammoBelt = HbmTranslationKeys.AMMO_BELT;
    public static final String ammoEnergy = HbmTranslationKeys.AMMO_ENERGY;
    public static final String altAmmoEnergy = HbmTranslationKeys.ALT_AMMO_ENERGY;
    public static final String ammoType = HbmTranslationKeys.AMMO_TYPE;
    public static final String altAmmoType = HbmTranslationKeys.ALT_AMMO_TYPE;
    public static final String gunName = HbmTranslationKeys.GUN_NAME;
    public static final String gunMaker = HbmTranslationKeys.GUN_MAKER;
    public static final String gunDamage = HbmTranslationKeys.GUN_DAMAGE;
    public static final String gunPellets = HbmTranslationKeys.GUN_PELLETS;
    public static final String capacity = HbmTranslationKeys.CAPACITY;
    public static final String durability = HbmTranslationKeys.DURABILITY;
    public static final String meltPoint = HbmTranslationKeys.MELT_POINT;
    public static final String lctrl = HbmTranslationKeys.LCTRL;
    public static final String lshift = HbmTranslationKeys.LSHIFT;

    private HbmCollection() {
    }

    public enum EnumGunManufacturer {
        ARMALITE,
        AUTO_ORDINANCE,
        BAE,
        BENELLI,
        BLACK_MESA,
        CERIX,
        COLT,
        COMBINE,
        CUBE,
        DRG,
        ENZINGER,
        EQUESTRIA,
        F_PRICE,
        F_STRONG,
        FLIMFLAM,
        GLORIA,
        H_AND_K,
        H_AND_R,
        HASBRO,
        IF,
        IMI,
        IMI_BIGMT,
        LANGFORD,
        MAGNUM_R_IMI,
        MANN,
        MAXIM,
        METRO,
        MWT,
        NAWS,
        ERFURT,
        NONE,
        OXFORD,
        LUNA,
        RAYTHEON,
        REMINGTON,
        ROCKWELL,
        ROCKWELL_U,
        RYAN,
        SAAB,
        SACO,
        TULSKY,
        UAC,
        UNKNOWN,
        WESTTEK,
        WGW,
        WINCHESTER,
        WINCHESTER_BIGMT;

        public String getKey() {
            return "gun.make." + name();
        }

        public HbmTranslationKeys.GunManufacturer toModern() {
            return HbmTranslationKeys.GunManufacturer.valueOf(name());
        }

        public static EnumGunManufacturer fromModern(HbmTranslationKeys.GunManufacturer manufacturer) {
            return valueOf(manufacturer.name());
        }
    }
}
