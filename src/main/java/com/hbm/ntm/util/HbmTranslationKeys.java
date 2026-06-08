package com.hbm.ntm.util;

public final class HbmTranslationKeys {
    public static final String AMMO = "desc.item.gun.ammo";
    public static final String AMMO_MAG = "desc.item.gun.ammoMag";
    public static final String AMMO_BELT = "desc.item.gun.ammoBelt";
    public static final String AMMO_ENERGY = "desc.item.gun.ammoEnergy";
    public static final String ALT_AMMO_ENERGY = "desc.item.gun.ammoEnergyAlt";
    public static final String AMMO_TYPE = "desc.item.gun.ammoType";
    public static final String ALT_AMMO_TYPE = "desc.item.gun.ammoTypeAlt";
    public static final String GUN_NAME = "desc.item.gun.name";
    public static final String GUN_MAKER = "desc.item.gun.manufacturer";
    public static final String GUN_DAMAGE = "desc.item.gun.damage";
    public static final String GUN_PELLETS = "desc.item.gun.pellets";
    public static final String CAPACITY = "desc.block.barrel.capacity";
    public static final String DURABILITY = "desc.item.durability";
    public static final String MELT_POINT = "desc.misc.meltPoint";
    public static final String LCTRL = "desc.misc.lctrl";
    public static final String LSHIFT = "desc.misc.lshift";

    private HbmTranslationKeys() {
    }

    public enum GunManufacturer {
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
    }
}
