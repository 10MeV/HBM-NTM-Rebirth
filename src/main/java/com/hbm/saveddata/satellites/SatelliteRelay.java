package com.hbm.saveddata.satellites;

import com.hbm.ntm.satellite.LegacySatelliteType;
import com.hbm.ntm.util.AchievementHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class SatelliteRelay extends Satellite {
    public SatelliteRelay() {
        this.satIface = Interfaces.NONE;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.RELAY;
    }

    @Override
    public void onOrbit(ServerLevel level, double x, double y, double z) {
        for (ServerPlayer player : level.players()) {
            AchievementHandler.award(player, AchievementHandler.FOEQ);
        }
    }
}
