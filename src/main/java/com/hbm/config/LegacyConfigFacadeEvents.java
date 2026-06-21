package com.hbm.config;

import com.hbm.ntm.HbmNtm;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Keeps legacy static config fields aligned with the modern Forge config values.
 */
@Deprecated(forRemoval = false)
@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class LegacyConfigFacadeEvents {
    @SubscribeEvent
    public static void onConfigLoading(ModConfigEvent.Loading event) {
        sync();
    }

    @SubscribeEvent
    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        sync();
    }

    public static void sync() {
        ClientConfig.syncFromModern();
        GeneralConfig.syncFromModern();
        BombConfig.syncFromModern();
        RadiationConfig.syncFromModern();
        ServerConfig.syncFromModern();
        MobConfig.syncFromModern();
        WorldConfig.syncFromModern();
    }

    private LegacyConfigFacadeEvents() {
    }
}
