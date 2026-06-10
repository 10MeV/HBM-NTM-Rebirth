package com.hbm.ntm.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Legacy-name hash/Base64 helper facade.
 */
@Deprecated(forRemoval = false)
public final class ShadyUtil {
    public static String HbMinecraft = HbmShadyUtil.HB_MINECRAFT;
    public static String LPkukin = HbmShadyUtil.LPKUKIN;
    public static String Dafnik = HbmShadyUtil.DAFNIK;
    public static String a20 = HbmShadyUtil.A20;
    public static String LordVertice = HbmShadyUtil.LORD_VERTICE;
    public static String CodeRed_ = HbmShadyUtil.CODE_RED;
    public static String dxmaster769 = HbmShadyUtil.DXMASTER769;
    public static String Dr_Nostalgia = HbmShadyUtil.DR_NOSTALGIA;
    public static String Samino2 = HbmShadyUtil.SAMINO2;
    public static String Hoboy03new = HbmShadyUtil.HOBOY03NEW;
    public static String Dragon59MC = HbmShadyUtil.DRAGON59MC;
    public static String Steelcourage = HbmShadyUtil.STEELCOURAGE;
    public static String ZippySqrl = HbmShadyUtil.ZIPPY_SQRL;
    public static String Schrabby = HbmShadyUtil.SCHRABBY;
    public static String SweatySwiggs = HbmShadyUtil.SWEATY_SWIGGS;
    public static String Drillgon = HbmShadyUtil.DRILLGON;
    public static String Doctor17 = HbmShadyUtil.DOCTOR17;
    public static String Doctor17PH = HbmShadyUtil.DOCTOR17PH;
    public static String ShimmeringBlaze = HbmShadyUtil.SHIMMERING_BLAZE;
    public static String FifeMiner = HbmShadyUtil.FIFE_MINER;
    public static String lag_add = HbmShadyUtil.LAG_ADD;
    public static String Pu_238 = HbmShadyUtil.PU_238;
    public static String Tankish = HbmShadyUtil.TANKISH;
    public static String FrizzleFrazzle = HbmShadyUtil.FRIZZLE_FRAZZLE;
    public static String the_NCR = HbmShadyUtil.THE_NCR;
    public static String Barnaby99_x = HbmShadyUtil.BARNABY99_X;
    public static String Ma118 = HbmShadyUtil.MA118;
    public static String Adam29Adam29 = HbmShadyUtil.ADAM29ADAM29;
    public static String Alcater = HbmShadyUtil.ALCATER;
    public static String ege444 = HbmShadyUtil.EGE444;
    public static String LePeeperSauvage = HbmShadyUtil.LE_PEEPER_SAUVAGE;

    public static final Set<String> hashes = new HashSet<>(HbmShadyUtil.HASHES);
    public static Set<String> contributors = new HashSet<>(HbmShadyUtil.CONTRIBUTORS);

    private ShadyUtil() {
    }

    @Deprecated(forRemoval = false)
    public static String encode(String message) {
        return HbmShadyUtil.encode(message);
    }

    @Deprecated(forRemoval = false)
    public static String decode(String message) {
        return HbmShadyUtil.decode(message);
    }

    public static String smoosh(String s1, String s2, String s3, String s4) {
        return HbmShadyUtil.smoosh(s1, s2, s3, s4);
    }

    public static String getHash(String input) {
        return HbmShadyUtil.getHash(input);
    }
}
