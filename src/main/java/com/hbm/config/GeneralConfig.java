package com.hbm.config;

import com.hbm.ntm.HbmNtm;

/**
 * Legacy package facade for general config reads that have modern consumers.
 */
@Deprecated(forRemoval = false)
public final class GeneralConfig {
    public static boolean enableThermosPreventer = false;
    public static boolean enablePacketThreading = true;
    public static int packetThreadingCoreCount = 1;
    public static int packetThreadingMaxCount = 1;
    public static boolean packetThreadingErrorBypass = false;
    public static boolean enableDebugMode = false;
    public static boolean enableMycelium = false;
    public static boolean enablePlutoniumOre = false;
    public static int enableDungeons = 2;
    public static boolean enableMDOres = true;
    public static boolean enableMines = true;
    public static boolean enableRad = true;
    public static boolean enableBomberShortMode = false;
    public static boolean enableVaults = true;
    public static boolean enableCataclysm = false;
    public static boolean enableExtendedLogging = false;
    public static boolean enableGuns = true;
    public static boolean enableVirus = false;
    public static boolean enableCrosshairs = true;
    public static boolean enableRenderDistCheck = true;
    public static boolean enableSilentCompStackErrors = false;
    public static boolean enableSkyboxes = true;
    public static boolean enableImpactWorldProvider = true;
    public static boolean enableStatReRegistering = true;
    public static boolean enableKeybindOverlap = true;
    public static boolean enableFluidContainerCompat = true;
    public static boolean enableMOTD = true;
    public static boolean enableGuideBook = true;
    public static boolean enableSoundExtension = true;
    public static boolean enableMekanismChanges = true;
    public static boolean enableServerRecipeSync = false;
    public static boolean enableLoadScreenReplacement = true;
    public static boolean enableMachineGravity = false;
    public static int normalSoundChannels = 100;
    public static boolean enableExpensiveMode = false;
    public static boolean enable528 = false;
    public static boolean enable528ReasimBoilers = false;
    public static boolean enable528ColtanDeposit = false;
    public static boolean enable528ColtanSpawn = false;
    public static boolean enable528BosniaSimulator = false;
    public static boolean enable528NetherBurn = false;
    public static boolean enable528PressurizedRecipes = false;
    public static boolean enable528ExplosiveEnergistics = false;
    public static boolean enable528MachineGravity = false;
    public static int coltanRate = 2;
    public static boolean enableLBSM = false;
    public static boolean enableLBSMFullSchrab = false;
    public static boolean enableLBSMShorterDecay = false;
    public static boolean enableLBSMSimpleArmorRecipes = false;
    public static boolean enableLBSMSimpleToolRecipes = false;
    public static boolean enableLBSMSimpleAlloy = false;
    public static boolean enableLBSMSimpleChemsitry = false;
    public static boolean enableLBSMSimpleCentrifuge = false;
    public static boolean enableLBSMUnlockAnvil = false;
    public static boolean enableLBSMSimpleCrafting = false;
    public static boolean enableLBSMSimpleMedicineRecipes = false;
    public static boolean enableLBSMSafeCrates = false;
    public static boolean enableLBSMSafeMEDrives = false;
    public static boolean enableLBSMIGen = false;
    public static int schrabRate = 20;
    public static String[] preferredOutputMod = new String[] { HbmNtm.MOD_ID };

    static {
        syncFromModern();
    }

    public static void syncFromModern() {
        try {
            enablePacketThreading = com.hbm.ntm.config.NetworkConfig.packetThreadingEnabled();
            packetThreadingErrorBypass = com.hbm.ntm.config.NetworkConfig.packetThreadingErrorBypass();
            enableExtendedLogging = com.hbm.ntm.config.HbmCommonConfig.extendedLoggingEnabled();
            enableMycelium = com.hbm.ntm.config.RadiationConfig.myceliumSpreadEnabled();
            enableGuns = com.hbm.ntm.config.WeaponConfig.gunsEnabled();
            enableVirus = com.hbm.ntm.config.HbmCommonConfig.crystalVirusSpreadingEnabled();
            enableCrosshairs = com.hbm.ntm.config.HbmClientConfig.customCrosshairsEnabled();
            enableMOTD = com.hbm.ntm.config.HbmCommonConfig.motdEnabled();
        } catch (IllegalStateException ignored) {
            // Keep legacy defaults until Forge finishes loading the modern config.
        }
    }

    public static boolean enablePacketThreading() {
        syncFromModern();
        return enablePacketThreading;
    }

    public static int packetThreadingCoreCount() {
        return packetThreadingCoreCount;
    }

    public static int packetThreadingMaxCount() {
        return packetThreadingMaxCount;
    }

    public static boolean packetThreadingErrorBypass() {
        syncFromModern();
        return packetThreadingErrorBypass;
    }

    public static boolean enableExtendedLogging() {
        syncFromModern();
        return enableExtendedLogging;
    }

    public static boolean enableMycelium() {
        syncFromModern();
        return enableMycelium;
    }

    public static boolean enableGuns() {
        syncFromModern();
        return enableGuns;
    }

    public static boolean enableVirus() {
        syncFromModern();
        return enableVirus;
    }

    public static boolean enableCrosshairs() {
        syncFromModern();
        return enableCrosshairs;
    }

    public static boolean enableMOTD() {
        syncFromModern();
        return enableMOTD;
    }

    public static boolean enableDebugMode() {
        return enableDebugMode;
    }

    public static boolean enableRad() {
        return enableRad;
    }

    public static boolean enableRenderDistCheck() {
        return enableRenderDistCheck;
    }

    public static boolean enableSilentCompStackErrors() {
        return enableSilentCompStackErrors;
    }

    public static boolean enableSkyboxes() {
        return enableSkyboxes;
    }

    public static boolean enableImpactWorldProvider() {
        return enableImpactWorldProvider;
    }

    public static boolean enableStatReRegistering() {
        return enableStatReRegistering;
    }

    public static boolean enableKeybindOverlap() {
        return enableKeybindOverlap;
    }

    public static boolean enableFluidContainerCompat() {
        return enableFluidContainerCompat;
    }

    public static boolean enableGuideBook() {
        return enableGuideBook;
    }

    public static boolean enableSoundExtension() {
        return enableSoundExtension;
    }

    public static boolean enableMekanismChanges() {
        return enableMekanismChanges;
    }

    public static boolean enableServerRecipeSync() {
        return enableServerRecipeSync;
    }

    public static boolean enableLoadScreenReplacement() {
        return enableLoadScreenReplacement;
    }

    public static boolean enableMachineGravity() {
        return enableMachineGravity;
    }

    public static int normalSoundChannels() {
        return normalSoundChannels;
    }

    public static boolean enableExpensiveMode() {
        return enableExpensiveMode;
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
        syncFromModern();
    }

    private GeneralConfig() {
    }
}
