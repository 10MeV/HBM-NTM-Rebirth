package com.hbm.ntm.explosion.vnt.interfaces;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

@FunctionalInterface
public interface PlayerProcessor {
    void process(ExplosionVnt explosion, ServerLevel level, Vec3 position, Map<Player, Vec3> affectedPlayers);
}
