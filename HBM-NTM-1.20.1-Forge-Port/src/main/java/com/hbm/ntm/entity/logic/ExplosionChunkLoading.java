package com.hbm.ntm.entity.logic;

import com.hbm.ntm.HbmNtm;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.UUID;

public final class ExplosionChunkLoading {
    public static void registerValidationCallback() {
        ForgeChunkManager.setForcedChunkLoadingCallback(HbmNtm.MOD_ID, ExplosionChunkLoading::validateTickets);
    }

    private static void validateTickets(ServerLevel level, ForgeChunkManager.TicketHelper ticketHelper) {
        for (UUID owner : ticketHelper.getEntityTickets().keySet()) {
            Entity entity = level.getEntity(owner);
            if (!(entity instanceof ExplosionChunkLoadingEntity)) {
                ticketHelper.removeAllTickets(owner);
            }
        }
    }

    private ExplosionChunkLoading() {
    }
}
