package com.hbm.ntm.api.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public interface RadarDetectable {
    int TIER0 = 0;
    int TIER1 = 1;
    int TIER2 = 2;
    int TIER3 = 3;
    int TIER4 = 4;
    int TIER10 = 5;
    int TIER10_15 = 6;
    int TIER15 = 7;
    int TIER15_20 = 8;
    int TIER20 = 9;
    int TIER_AB = 10;
    int PLAYER = 11;
    int ARTY = 12;
    int SPECIAL = 13;

    String TARGET_TIER0 = "radar.target.tier0";
    String TARGET_TIER1 = "radar.target.tier1";
    String TARGET_TIER2 = "radar.target.tier2";
    String TARGET_TIER3 = "radar.target.tier3";
    String TARGET_TIER4 = "radar.target.tier4";
    String TARGET_CUSTOM_10 = "radar.target.custom10";
    String TARGET_CUSTOM_10_15 = "radar.target.custom1015";
    String TARGET_CUSTOM_15 = "radar.target.custom15";
    String TARGET_CUSTOM_15_20 = "radar.target.custom1520";
    String TARGET_CUSTOM_20 = "radar.target.custom20";
    String TARGET_ABM = "radar.target.abm";
    String TARGET_DOOMSDAY = "radar.target.doomsday";
    String TARGET_SHUTTLE = "radar.target.shuttle";

    String getRadarName();

    int getBlipLevel();

    boolean canBeSeenBy(RadarContext radar);

    boolean paramsApplicable(RadarScanParams params);

    boolean suppliesRedstone(RadarScanParams params);

    record RadarScanParams(boolean scanMissiles, boolean scanShells, boolean scanPlayers, boolean smartMode) {
        public static final RadarScanParams DEFAULT = new RadarScanParams(true, true, true, true);

        private static final String TAG_SCAN_MISSILES = "scanMissiles";
        private static final String TAG_SCAN_SHELLS = "scanShells";
        private static final String TAG_SCAN_PLAYERS = "scanPlayers";
        private static final String TAG_SMART_MODE = "smartMode";

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            writeTo(tag);
            return tag;
        }

        public void writeTo(CompoundTag tag) {
            tag.putBoolean(TAG_SCAN_MISSILES, scanMissiles);
            tag.putBoolean(TAG_SCAN_SHELLS, scanShells);
            tag.putBoolean(TAG_SCAN_PLAYERS, scanPlayers);
            tag.putBoolean(TAG_SMART_MODE, smartMode);
        }

        public static RadarScanParams fromTag(CompoundTag tag) {
            return fromTag(tag, DEFAULT);
        }

        public static RadarScanParams fromTag(CompoundTag tag, RadarScanParams fallback) {
            RadarScanParams defaults = fallback != null ? fallback : DEFAULT;
            if (tag == null) {
                return defaults;
            }
            return new RadarScanParams(
                    booleanOrDefault(tag, TAG_SCAN_MISSILES, defaults.scanMissiles()),
                    booleanOrDefault(tag, TAG_SCAN_SHELLS, defaults.scanShells()),
                    booleanOrDefault(tag, TAG_SCAN_PLAYERS, defaults.scanPlayers()),
                    booleanOrDefault(tag, TAG_SMART_MODE, defaults.smartMode()));
        }

        public void writeLegacyWire(FriendlyByteBuf buffer) {
            buffer.writeBoolean(scanMissiles);
            buffer.writeBoolean(scanShells);
            buffer.writeBoolean(scanPlayers);
            buffer.writeBoolean(smartMode);
        }

        public static RadarScanParams readLegacyWire(FriendlyByteBuf buffer) {
            return new RadarScanParams(
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean());
        }

        private static boolean booleanOrDefault(CompoundTag tag, String key, boolean fallback) {
            return tag.contains(key, Tag.TAG_BYTE) ? tag.getBoolean(key) : fallback;
        }
    }
}
