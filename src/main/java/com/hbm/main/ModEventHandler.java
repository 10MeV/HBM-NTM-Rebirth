package com.hbm.main;

import com.hbm.ntm.event.CommonForgeEvents;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Legacy event-handler facade. The active Forge subscription lives in
 * {@link CommonForgeEvents}; these methods preserve source-migration entry
 * points without registering a second handler.
 */
@Deprecated(forRemoval = false)
public class ModEventHandler {
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        CommonForgeEvents.onPlayerLogin(event);
    }

    @SubscribeEvent
    public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        CommonForgeEvents.onPlayerChangedDimension(event);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        CommonForgeEvents.onPlayerClone(event);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        CommonForgeEvents.onPlayerTick(event);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        CommonForgeEvents.onServerTick(event);
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        CommonForgeEvents.onLivingTick(event);
    }

    @SubscribeEvent
    public void decorateMob(MobSpawnEvent.FinalizeSpawn event) {
        CommonForgeEvents.onMobFinalizeSpawn(event);
    }

    @SubscribeEvent
    public void addAITasks(EntityJoinLevelEvent event) {
        CommonForgeEvents.onEntityJoinLevel(event);
    }

    @SubscribeEvent
    public void onEntityDeathFirst(LivingDeathEvent event) {
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
    }

    @SubscribeEvent
    public void onEntityDeathLast(LivingDeathEvent event) {
    }

    @SubscribeEvent
    public void onLivingDrop(LivingDropsEvent event) {
    }
}
