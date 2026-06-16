package com.hbm.config;

/**
 * Legacy package facade for general config reads that have modern consumers.
 */
@Deprecated(forRemoval = false)
public final class GeneralConfig {
    public static boolean enablePacketThreading() {
        return com.hbm.ntm.config.NetworkConfig.packetThreadingEnabled();
    }

    public static int packetThreadingCoreCount() {
        return 1;
    }

    public static int packetThreadingMaxCount() {
        return 1;
    }

    public static boolean packetThreadingErrorBypass() {
        return com.hbm.ntm.config.NetworkConfig.packetThreadingErrorBypass();
    }

    public static boolean enableExtendedLogging() {
        return com.hbm.ntm.config.HbmCommonConfig.extendedLoggingEnabled();
    }

    public static boolean enableMycelium() {
        return com.hbm.ntm.config.RadiationConfig.myceliumSpreadEnabled();
    }

    public static boolean enableGuns() {
        return com.hbm.ntm.config.WeaponConfig.gunsEnabled();
    }

    public static boolean enableVirus() {
        return com.hbm.ntm.config.HbmCommonConfig.crystalVirusSpreadingEnabled();
    }

    public static boolean enableCrosshairs() {
        return com.hbm.ntm.config.HbmClientConfig.customCrosshairsEnabled();
    }

    public static boolean enableMOTD() {
        return com.hbm.ntm.config.HbmCommonConfig.motdEnabled();
    }

    public static boolean enableDebugMode() {
        return false;
    }

    public static boolean enableRad() {
        return true;
    }

    public static boolean enableRenderDistCheck() {
        return true;
    }

    public static boolean enableSilentCompStackErrors() {
        return false;
    }

    public static boolean enableSkyboxes() {
        return true;
    }

    public static boolean enableImpactWorldProvider() {
        return true;
    }

    public static boolean enableStatReRegistering() {
        return true;
    }

    public static boolean enableKeybindOverlap() {
        return true;
    }

    public static boolean enableFluidContainerCompat() {
        return true;
    }

    public static boolean enableGuideBook() {
        return true;
    }

    public static boolean enableSoundExtension() {
        return true;
    }

    public static boolean enableMekanismChanges() {
        return true;
    }

    public static boolean enableServerRecipeSync() {
        return false;
    }

    public static boolean enableLoadScreenReplacement() {
        return true;
    }

    public static boolean enableMachineGravity() {
        return false;
    }

    public static int normalSoundChannels() {
        return 100;
    }

    public static boolean enableExpensiveMode() {
        return false;
    }

    public static boolean trueExp() {
        return false;
    }

    public static boolean enable528() {
        return false;
    }

    public static boolean true528() {
        return false;
    }

    public static boolean enableLBSM() {
        return false;
    }

    public static void loadFromConfig(Object ignored) {
    }

    private GeneralConfig() {
    }
}
