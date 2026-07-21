package com.hbm.ntm.api.entity;

@Deprecated
public interface LegacyRadarDetectable {
    RadarTargetType getTargetType();

    default boolean canBeDetectedByLegacyRadar() {
        return true;
    }

    enum RadarTargetType {
        MISSILE_TIER0("Micro Missile"),
        MISSILE_TIER1("Tier 1 Missile"),
        MISSILE_TIER2("Tier 2 Missile"),
        MISSILE_TIER3("Tier 3 Missile"),
        MISSILE_TIER4("Tier 4 Missile"),
        MISSILE_10("Size 10 Custom Missile"),
        MISSILE_10_15("Size 10/15 Custom Missile"),
        MISSILE_15("Size 15 Custom Missile"),
        MISSILE_15_20("Size 15/20 Custom Missile"),
        MISSILE_20("Size 20 Custom Missile"),
        MISSILE_AB("Anti-Ballistic Missile"),
        PLAYER("Player"),
        ARTILLERY("Artillery Shell");

        /**
         * Legacy {@code IRadarDetectable.RadarTargetType} exposed this display value as a public field.
         * Keep that source-facing contract so old callers can use {@code type.name} without a second
         * compatibility enum.
         */
        public String name;

        RadarTargetType(String name) {
            this.name = name;
        }

        public String radarName() {
            return name;
        }
    }
}
