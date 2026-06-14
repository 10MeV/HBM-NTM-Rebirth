package com.hbm.ntm.satellite;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class SatelliteResonator extends Satellite {
    public SatelliteResonator() {
        coordAcs.add(CoordActions.HAS_Y);
        setSatelliteInterface(Interfaces.SAT_COORD);
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.RESONATOR;
    }

    @Override
    public void onCoordAction(ServerLevel level, ServerPlayer player, int x, int y, int z) {
        tryCoordAction(level, player, x, y, z);
    }

    @Override
    public boolean tryCoordAction(ServerLevel level, ServerPlayer player, int x, int y, int z) {
        playTeleportSound(level, player);
        player.stopRiding();
        player.teleportTo(level, x + 0.5D, y, z + 0.5D, player.getYRot(), player.getXRot());
        playTeleportSound(level, player);
        return true;
    }
}
