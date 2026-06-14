package com.hbm.lib;

import com.hbm.ntm.HbmNtm;

/**
 * Legacy 1.7.10 package bridge for mod identity constants.
 */
@Deprecated(forRemoval = false)
public final class RefStrings {
    public static final String LEGACY_MODID = "hbm";
    public static final String MODID = HbmNtm.MOD_ID;
    public static final String NAME = "HBM-NTM: Rebirth";
    public static final String VERSION = "0.1.7beta";
    public static final String CLIENTSIDE = "com.hbm.main.ClientProxy";
    public static final String SERVERSIDE = "com.hbm.main.ServerProxy";

    private RefStrings() {
    }
}
