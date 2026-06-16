package com.hbm.config;

/**
 * Legacy package facade for client config reads that have modern consumers.
 *
 * <p>The old hbmClient.json RunningConfig surface is not restored here; methods delegate to the
 * Forge client config or return documented legacy defaults for features without a modern consumer.</p>
 */
@Deprecated(forRemoval = false)
public final class ClientConfig {
    public static int geigerOffsetHorizontal() {
        return com.hbm.ntm.config.HbmClientConfig.geigerOffsetHorizontal();
    }

    public static int geigerOffsetVertical() {
        return com.hbm.ntm.config.HbmClientConfig.geigerOffsetVertical();
    }

    public static int infoOffsetHorizontal() {
        return com.hbm.ntm.config.HbmClientConfig.infoOffsetHorizontal();
    }

    public static int infoOffsetVertical() {
        return com.hbm.ntm.config.HbmClientConfig.infoOffsetVertical();
    }

    public static int infoPosition() {
        return com.hbm.ntm.config.HbmClientConfig.infoPosition();
    }

    public static double gunAnimationSpeed() {
        return 1.0D / com.hbm.ntm.config.HbmClientConfig.legacyGunAnimationTimeMultiplier();
    }

    public static boolean itemTooltipShowOredict() {
        return com.hbm.ntm.config.HbmClientConfig.itemTagTooltips();
    }

    public static boolean itemTooltipShowCustomNuke() {
        return com.hbm.ntm.config.HbmClientConfig.customNukeTooltips();
    }

    public static boolean doddRbmkDiagnostic() {
        return com.hbm.ntm.config.HbmClientConfig.legacyLookOverlay();
    }

    public static boolean coolingTowerParticles() {
        return com.hbm.ntm.config.HbmClientConfig.coolingTowerParticles();
    }

    public static boolean nukeHudFlash() {
        return com.hbm.ntm.config.HbmClientConfig.nukeHudFlash();
    }

    public static boolean nukeHudShake() {
        return com.hbm.ntm.config.HbmClientConfig.nukeHudShake();
    }

    public static int toolHudIndicatorX() {
        return com.hbm.ntm.config.HbmClientConfig.toolHudIndicatorX();
    }

    public static int toolHudIndicatorY() {
        return com.hbm.ntm.config.HbmClientConfig.toolHudIndicatorY();
    }

    public static boolean showBlockMetaOverlay() {
        return com.hbm.ntm.config.HbmClientConfig.showBlockStateOverlay();
    }

    public static boolean neiHideSecrets() {
        return com.hbm.ntm.config.HbmClientConfig.hideSecretJeiRecipes();
    }

    public static boolean gunAnimsLegacy() {
        return false;
    }

    public static boolean gunModelFov() {
        return false;
    }

    public static boolean gunVisualRecoil() {
        return true;
    }

    public static boolean mainMenuWackySplashes() {
        return true;
    }

    public static boolean renderCableHang() {
        return true;
    }

    public static boolean renderReeds() {
        return true;
    }

    public static boolean renderRebarSimple() {
        return false;
    }

    public static int renderHeliostatBeamLimit() {
        return 250;
    }

    public static int renderRebarLimit() {
        return 250;
    }

    public static boolean badgesHud() {
        return true;
    }

    public static void initConfig() {
    }

    public static void refresh() {
    }

    public static void reload() {
    }

    private ClientConfig() {
    }
}
