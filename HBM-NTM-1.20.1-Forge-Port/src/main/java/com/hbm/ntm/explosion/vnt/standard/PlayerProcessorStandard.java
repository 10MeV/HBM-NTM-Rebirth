package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.PlayerProcessor;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class PlayerProcessorStandard implements PlayerProcessor {
    @Override
    public void process(ExplosionVnt explosion, ServerLevel level, Vec3 position, Map<Player, Vec3> affectedPlayers) {
        for (Map.Entry<Player, Vec3> entry : affectedPlayers.entrySet()) {
            if (entry.getKey() instanceof ServerPlayer player) {
                ModMessages.sendExplosionKnockback(player, entry.getValue());
            }
        }
    }
}
