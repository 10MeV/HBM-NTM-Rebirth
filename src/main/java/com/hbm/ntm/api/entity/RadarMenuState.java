package com.hbm.ntm.api.entity;

import net.minecraft.util.Mth;

public record RadarMenuState(long power, long maxPower, RadarDetectable.RadarScanParams scanSettings,
                             RadarRedstoneMode redstoneMode, boolean showMap, boolean jammed,
                             int redstonePower) {
    public static final RadarMenuState DEFAULT = new RadarMenuState(0L, 0L, RadarDetectable.RadarScanParams.DEFAULT,
            RadarRedstoneMode.PROXIMITY, false, false, 0);

    public RadarMenuState {
        power = Math.max(0L, power);
        maxPower = Math.max(0L, maxPower);
        scanSettings = scanSettings == null ? RadarDetectable.RadarScanParams.DEFAULT : scanSettings;
        redstoneMode = redstoneMode == null ? RadarRedstoneMode.PROXIMITY : redstoneMode;
        redstonePower = Mth.clamp(redstonePower, 0, 15);
    }

    public boolean redstoneProximityMode() {
        return redstoneMode.legacyFlag();
    }

    public boolean controlActive(RadarControl control) {
        if (control == null) {
            return false;
        }
        return switch (control) {
            case SCAN_MISSILES -> scanSettings.scanMissiles();
            case SCAN_SHELLS -> scanSettings.scanShells();
            case SCAN_PLAYERS -> scanSettings.scanPlayers();
            case SMART_MODE -> scanSettings.smartMode();
            case REDSTONE_MODE -> redstoneMode.legacyFlag();
            case SHOW_MAP -> showMap;
            case CLEAR_MAP -> false;
        };
    }

    public int powerBarWidth(int maxWidth) {
        if (maxPower <= 0L || maxWidth <= 0) {
            return 0;
        }
        return Mth.clamp((int) (power * maxWidth / maxPower), 0, maxWidth);
    }

    public boolean hasPower() {
        return power > 0L;
    }

    public boolean hasOperatingPower(long consumption) {
        return power >= Math.max(0L, consumption);
    }

    public RadarMenuState withPower(long power) {
        return new RadarMenuState(power, maxPower, scanSettings, redstoneMode, showMap, jammed, redstonePower);
    }

    public RadarMenuState withMaxPower(long maxPower) {
        return new RadarMenuState(power, maxPower, scanSettings, redstoneMode, showMap, jammed, redstonePower);
    }

    public RadarMenuState withScanMissiles(boolean scanMissiles) {
        return withScanSettings(new RadarDetectable.RadarScanParams(scanMissiles, scanSettings.scanShells(),
                scanSettings.scanPlayers(), scanSettings.smartMode()));
    }

    public RadarMenuState withScanShells(boolean scanShells) {
        return withScanSettings(new RadarDetectable.RadarScanParams(scanSettings.scanMissiles(), scanShells,
                scanSettings.scanPlayers(), scanSettings.smartMode()));
    }

    public RadarMenuState withScanPlayers(boolean scanPlayers) {
        return withScanSettings(new RadarDetectable.RadarScanParams(scanSettings.scanMissiles(),
                scanSettings.scanShells(), scanPlayers, scanSettings.smartMode()));
    }

    public RadarMenuState withSmartMode(boolean smartMode) {
        return withScanSettings(new RadarDetectable.RadarScanParams(scanSettings.scanMissiles(),
                scanSettings.scanShells(), scanSettings.scanPlayers(), smartMode));
    }

    public RadarMenuState withScanSettings(RadarDetectable.RadarScanParams scanSettings) {
        return new RadarMenuState(power, maxPower, scanSettings, redstoneMode, showMap, jammed, redstonePower);
    }

    public RadarMenuState withRedstoneMode(RadarRedstoneMode redstoneMode) {
        return new RadarMenuState(power, maxPower, scanSettings, redstoneMode, showMap, jammed, redstonePower);
    }

    public RadarMenuState withShowMap(boolean showMap) {
        return new RadarMenuState(power, maxPower, scanSettings, redstoneMode, showMap, jammed, redstonePower);
    }

    public RadarMenuState withJammed(boolean jammed) {
        return new RadarMenuState(power, maxPower, scanSettings, redstoneMode, showMap, jammed, redstonePower);
    }

    public RadarMenuState withRedstonePower(int redstonePower) {
        return new RadarMenuState(power, maxPower, scanSettings, redstoneMode, showMap, jammed, redstonePower);
    }
}
