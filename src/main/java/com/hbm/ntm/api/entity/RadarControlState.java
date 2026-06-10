package com.hbm.ntm.api.entity;

import java.util.Collection;

public record RadarControlState(RadarDetectable.RadarScanParams scanSettings, RadarRedstoneMode redstoneMode,
                                boolean showMap) {
    public RadarControlState {
        scanSettings = scanSettings == null ? RadarDetectable.RadarScanParams.DEFAULT : scanSettings;
        redstoneMode = redstoneMode == null ? RadarRedstoneMode.PROXIMITY : redstoneMode;
    }

    public static RadarControlState of(RadarDetectable.RadarScanParams scanSettings, boolean redstoneProximityMode,
            boolean showMap) {
        return new RadarControlState(scanSettings, RadarRedstoneMode.fromLegacyFlag(redstoneProximityMode), showMap);
    }

    public Application apply(Collection<RadarControl> controls) {
        RadarControlState state = this;
        boolean clearMap = false;
        if (controls != null) {
            for (RadarControl control : controls) {
                if (control == null) {
                    continue;
                }
                if (control == RadarControl.CLEAR_MAP) {
                    clearMap = true;
                } else {
                    state = state.apply(control);
                }
            }
        }
        return new Application(state, clearMap);
    }

    public RadarControlState apply(RadarControl control) {
        if (control == null) {
            return this;
        }
        return switch (control) {
            case SCAN_MISSILES -> withScanMissiles(!scanSettings.scanMissiles());
            case SCAN_SHELLS -> withScanShells(!scanSettings.scanShells());
            case SCAN_PLAYERS -> withScanPlayers(!scanSettings.scanPlayers());
            case SMART_MODE -> withSmartMode(!scanSettings.smartMode());
            case REDSTONE_MODE -> withRedstoneMode(redstoneMode == RadarRedstoneMode.PROXIMITY
                    ? RadarRedstoneMode.TIER
                    : RadarRedstoneMode.PROXIMITY);
            case SHOW_MAP -> withShowMap(!showMap);
            case CLEAR_MAP -> this;
        };
    }

    public boolean redstoneProximityMode() {
        return redstoneMode.legacyFlag();
    }

    private RadarControlState withScanMissiles(boolean scanMissiles) {
        return withScanSettings(new RadarDetectable.RadarScanParams(scanMissiles, scanSettings.scanShells(),
                scanSettings.scanPlayers(), scanSettings.smartMode()));
    }

    private RadarControlState withScanShells(boolean scanShells) {
        return withScanSettings(new RadarDetectable.RadarScanParams(scanSettings.scanMissiles(), scanShells,
                scanSettings.scanPlayers(), scanSettings.smartMode()));
    }

    private RadarControlState withScanPlayers(boolean scanPlayers) {
        return withScanSettings(new RadarDetectable.RadarScanParams(scanSettings.scanMissiles(),
                scanSettings.scanShells(), scanPlayers, scanSettings.smartMode()));
    }

    private RadarControlState withSmartMode(boolean smartMode) {
        return withScanSettings(new RadarDetectable.RadarScanParams(scanSettings.scanMissiles(),
                scanSettings.scanShells(), scanSettings.scanPlayers(), smartMode));
    }

    private RadarControlState withScanSettings(RadarDetectable.RadarScanParams scanSettings) {
        return new RadarControlState(scanSettings, redstoneMode, showMap);
    }

    private RadarControlState withRedstoneMode(RadarRedstoneMode redstoneMode) {
        return new RadarControlState(scanSettings, redstoneMode, showMap);
    }

    private RadarControlState withShowMap(boolean showMap) {
        return new RadarControlState(scanSettings, redstoneMode, showMap);
    }

    public record Application(RadarControlState state, boolean clearMap) {
    }
}
