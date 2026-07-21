package com.hbm.ntm.api.entity;

import com.hbm.ntm.satellite.Satellite;
import com.hbm.ntm.satellite.SatelliteHorizons;
import com.hbm.ntm.satellite.SatelliteLaser;
import com.hbm.ntm.satellite.SatelliteResonator;
import com.hbm.ntm.world.WorldUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class RadarSatelliteCommand {
    public static final int LEGACY_DEFAULT_COORD_Y = 60;

    private RadarSatelliteCommand() {
    }

    public static RadarCommandResult dispatch(ServerLevel level, ServerPlayer player, Satellite satellite,
            RadarLaunchCommand command) {
        if (level == null || player == null || satellite == null || command == null || command.targetsEntity()) {
            return RadarCommandResult.ERROR_INCOMPATIBLE;
        }

        int x = command.target().x();
        int z = command.target().z();
        // TileEntityMachineRadarNT did not dispatch to every satellite with a compatible
        // interface. It explicitly recognized these three concrete legacy satellite types.
        // Keep that boundary: public satellite registration must not silently turn a custom
        // SAT_PANEL/SAT_COORD implementation into a radar-relay target.
        if (isLegacyLaser(satellite)) {
            satellite.onClick(level, x, z);
            return RadarCommandResult.TRIGGERED;
        }
        if (isLegacyHorizons(satellite)) {
            satellite.onCoordAction(level, player, x, LEGACY_DEFAULT_COORD_Y, z);
            return RadarCommandResult.TRIGGERED;
        }
        if (isLegacyResonator(satellite)) {
            satellite.onCoordAction(level, player, x, WorldUtil.legacyGetTopSolidOrLiquidBlock(level, x, z), z);
            return RadarCommandResult.TRIGGERED;
        }
        return RadarCommandResult.ERROR_INCOMPATIBLE;
    }

    /**
     * Normal SavedData creation deliberately returns old-package facade instances.
     * Keep Radar's three-concrete-class source boundary, while recognizing each
     * facade and its internal modern counterpart rather than widening control to
     * every satellite with a matching interface or enum type.
     */
    private static boolean isLegacyLaser(Satellite satellite) {
        return satellite instanceof com.hbm.saveddata.satellites.SatelliteLaser
                || satellite instanceof SatelliteLaser;
    }

    private static boolean isLegacyHorizons(Satellite satellite) {
        return satellite instanceof com.hbm.saveddata.satellites.SatelliteHorizons
                || satellite instanceof SatelliteHorizons;
    }

    private static boolean isLegacyResonator(Satellite satellite) {
        return satellite instanceof com.hbm.saveddata.satellites.SatelliteResonator
                || satellite instanceof SatelliteResonator;
    }
}
