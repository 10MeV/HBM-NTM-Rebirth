package com.hbm.ntm.satellite;

import com.hbm.ntm.util.AchievementHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class SatelliteRelay extends Satellite {
    public SatelliteRelay() {
        setSatelliteInterface(Interfaces.NONE);
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.RELAY;
    }

    @Override
    public void onOrbit(ServerLevel level, double x, double y, double z) {
        // 1.7.10 SatelliteRelay awards every player in the launch world when
        // the FOEQ relay reaches orbit; this is intentionally not limited to
        // the player who operated the Soyuz launcher.
        for (ServerPlayer player : level.players()) {
            AchievementHandler.award(player, AchievementHandler.FOEQ);
        }
    }
}
