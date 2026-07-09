package com.hbm.main;

import com.hbm.ntm.event.CommonForgeEvents;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Legacy impact event facade. Modern impact climate ticking is centralized in
 * {@link CommonForgeEvents#onServerTick(TickEvent.ServerTickEvent)}.
 */
@Deprecated(forRemoval = false)
public class ModEventHandlerImpact {
    @SubscribeEvent
    public void worldTick(TickEvent.ServerTickEvent event) {
        CommonForgeEvents.onServerTick(event);
    }

    @SubscribeEvent
    public void extinction(MobSpawnEvent.PositionCheck event) {
        CommonForgeEvents.onMobPositionCheck(event);
    }
}
