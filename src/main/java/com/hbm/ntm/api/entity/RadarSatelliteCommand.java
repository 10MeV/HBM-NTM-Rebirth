package com.hbm.ntm.api.entity;

import com.hbm.ntm.satellite.Satellite;
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
        if (satellite.satelliteInterface() == Satellite.SatelliteInterface.SAT_PANEL
                && satellite.interfaceActions().contains(Satellite.InterfaceAction.CAN_CLICK)) {
            return satellite.tryClick(level, x, z)
                    ? RadarCommandResult.TRIGGERED
                    : RadarCommandResult.ERROR_INCOMPATIBLE;
        }
        if (satellite.satelliteInterface() == Satellite.SatelliteInterface.SAT_COORD) {
            int y = satellite.coordActions().contains(Satellite.CoordAction.HAS_Y)
                    ? WorldUtil.legacyGetTopSolidOrLiquidBlock(level, x, z)
                    : LEGACY_DEFAULT_COORD_Y;
            return satellite.tryCoordAction(level, player, x, y, z)
                    ? RadarCommandResult.TRIGGERED
                    : RadarCommandResult.ERROR_INCOMPATIBLE;
        }
        return RadarCommandResult.ERROR_INCOMPATIBLE;
    }
}
