package com.hbm.config;

import java.util.HashMap;

/**
 * Legacy package facade for client config reads that have modern consumers.
 *
 * <p>The old hbmClient.json persistence surface is intentionally not restored;
 * persisted values live in the Forge client config.</p>
 */
@Deprecated(forRemoval = false)
public final class ClientConfig extends RunningConfig {
    public static final HashMap<String, ConfigWrapper> configMap = new HashMap<>();

    public static final ConfigWrapper<Integer> GEIGER_OFFSET_HORIZONTAL = wrap(0, ClientConfig::geigerOffsetHorizontal);
    public static final ConfigWrapper<Integer> GEIGER_OFFSET_VERTICAL = wrap(0, ClientConfig::geigerOffsetVertical);
    public static final ConfigWrapper<Integer> INFO_OFFSET_HORIZONTAL = wrap(0, ClientConfig::infoOffsetHorizontal);
    public static final ConfigWrapper<Integer> INFO_OFFSET_VERTICAL = wrap(0, ClientConfig::infoOffsetVertical);
    public static final ConfigWrapper<Integer> INFO_POSITION = wrap(0, ClientConfig::infoPosition);
    public static final ConfigWrapper<Boolean> GUN_ANIMS_LEGACY = wrap(false, ClientConfig::gunAnimsLegacy);
    public static final ConfigWrapper<Boolean> GUN_MODEL_FOV = wrap(false, ClientConfig::gunModelFov);
    public static final ConfigWrapper<Boolean> GUN_VISUAL_RECOIL = wrap(true, ClientConfig::gunVisualRecoil);
    public static final ConfigWrapper<Double> GUN_ANIMATION_SPEED = wrap(1.0D, ClientConfig::gunAnimationSpeed);
    public static final ConfigWrapper<Boolean> ITEM_TOOLTIP_SHOW_OREDICT = wrap(true, ClientConfig::itemTooltipShowOredict);
    public static final ConfigWrapper<Boolean> ITEM_TOOLTIP_SHOW_CUSTOM_NUKE = wrap(true, ClientConfig::itemTooltipShowCustomNuke);
    public static final ConfigWrapper<Boolean> MAIN_MENU_WACKY_SPLASHES = wrap(true, ClientConfig::mainMenuWackySplashes);
    public static final ConfigWrapper<Boolean> DODD_RBMK_DIAGNOSTIC = wrap(true, ClientConfig::doddRbmkDiagnostic);
    public static final ConfigWrapper<Boolean> RENDER_CABLE_HANG = wrap(true, ClientConfig::renderCableHang);
    public static final ConfigWrapper<Boolean> NUKE_HUD_FLASH = wrap(true, ClientConfig::nukeHudFlash);
    public static final ConfigWrapper<Boolean> NUKE_HUD_SHAKE = wrap(true, ClientConfig::nukeHudShake);
    public static final ConfigWrapper<Boolean> RENDER_REEDS = wrap(true, ClientConfig::renderReeds);
    public static final ConfigWrapper<Boolean> NEI_HIDE_SECRETS = wrap(true, ClientConfig::neiHideSecrets);
    public static final ConfigWrapper<Boolean> COOLING_TOWER_PARTICLES = wrap(true, ClientConfig::coolingTowerParticles);
    public static final ConfigWrapper<Boolean> RENDER_REBAR_SIMPLE = wrap(false, ClientConfig::renderRebarSimple);
    public static final ConfigWrapper<Integer> RENDER_HELIOSTAT_BEAM_LIMIT = wrap(250, ClientConfig::renderHeliostatBeamLimit);
    public static final ConfigWrapper<Integer> RENDER_REBAR_LIMIT = wrap(250, ClientConfig::renderRebarLimit);
    public static final ConfigWrapper<Integer> TOOL_HUD_INDICATOR_X = wrap(0, ClientConfig::toolHudIndicatorX);
    public static final ConfigWrapper<Integer> TOOL_HUD_INDICATOR_Y = wrap(0, ClientConfig::toolHudIndicatorY);
    public static final ConfigWrapper<Boolean> SHOW_BLOCK_META_OVERLAY = wrap(false, ClientConfig::showBlockMetaOverlay);
    public static final ConfigWrapper<Boolean> BADGES_HUD = wrap(true, ClientConfig::badgesHud);

    static {
        initDefaults();
    }

    private static void initDefaults() {
        configMap.put("GEIGER_OFFSET_HORIZONTAL", GEIGER_OFFSET_HORIZONTAL);
        configMap.put("GEIGER_OFFSET_VERTICAL", GEIGER_OFFSET_VERTICAL);
        configMap.put("INFO_OFFSET_HORIZONTAL", INFO_OFFSET_HORIZONTAL);
        configMap.put("INFO_OFFSET_VERTICAL", INFO_OFFSET_VERTICAL);
        configMap.put("INFO_POSITION", INFO_POSITION);
        configMap.put("GUN_ANIMS_LEGACY", GUN_ANIMS_LEGACY);
        configMap.put("GUN_MODEL_FOV", GUN_MODEL_FOV);
        configMap.put("GUN_VISUAL_RECOIL", GUN_VISUAL_RECOIL);
        configMap.put("GUN_ANIMATION_SPEED", GUN_ANIMATION_SPEED);
        configMap.put("ITEM_TOOLTIP_SHOW_OREDICT", ITEM_TOOLTIP_SHOW_OREDICT);
        configMap.put("ITEM_TOOLTIP_SHOW_CUSTOM_NUKE", ITEM_TOOLTIP_SHOW_CUSTOM_NUKE);
        configMap.put("MAIN_MENU_WACKY_SPLASHES", MAIN_MENU_WACKY_SPLASHES);
        configMap.put("DODD_RBMK_DIAGNOSTIC", DODD_RBMK_DIAGNOSTIC);
        configMap.put("RENDER_CABLE_HANG", RENDER_CABLE_HANG);
        configMap.put("NUKE_HUD_FLASH", NUKE_HUD_FLASH);
        configMap.put("NUKE_HUD_SHAKE", NUKE_HUD_SHAKE);
        configMap.put("RENDER_REEDS", RENDER_REEDS);
        configMap.put("NEI_HIDE_SECRETS", NEI_HIDE_SECRETS);
        configMap.put("COOLING_TOWER_PARTICLES", COOLING_TOWER_PARTICLES);
        configMap.put("RENDER_REBAR_SIMPLE", RENDER_REBAR_SIMPLE);
        configMap.put("RENDER_HELIOSTAT_BEAM_LIMIT", RENDER_HELIOSTAT_BEAM_LIMIT);
        configMap.put("RENDER_REBAR_LIMIT", RENDER_REBAR_LIMIT);
        configMap.put("TOOL_HUD_INDICATOR_X", TOOL_HUD_INDICATOR_X);
        configMap.put("TOOL_HUD_INDICATOR_Y", TOOL_HUD_INDICATOR_Y);
        configMap.put("SHOW_BLOCK_META_OVERLAY", SHOW_BLOCK_META_OVERLAY);
        configMap.put("BADGES_HUD", BADGES_HUD);
    }

    public static void syncFromModern() {
        for (ConfigWrapper wrapper : configMap.values()) {
            wrapper.get();
        }
    }

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
        syncFromModern();
    }

    public static void refresh() {
        syncFromModern();
    }

    public static void reload() {
        syncFromModern();
    }

    private static <T> ConfigWrapper<T> wrap(T fallback, java.util.function.Supplier<T> getter) {
        return new ConfigWrapper<>(fallback, getter);
    }

    private ClientConfig() {
    }
}
