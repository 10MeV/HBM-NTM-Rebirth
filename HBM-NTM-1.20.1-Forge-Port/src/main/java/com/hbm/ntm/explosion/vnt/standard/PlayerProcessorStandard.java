package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.PlayerProcessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class PlayerProcessorStandard implements PlayerProcessor {
    @Override
    public void process(ExplosionVnt explosion, ServerLevel level, Vec3 position, Map<Player, Vec3> affectedPlayers) {
        // Modern vanilla already syncs velocity changes for tracked players; the map is kept for compatibility.
    }
}
