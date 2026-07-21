package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry-backed 1.20.1 carrier for the legacy {@code MainRegistry} custom
 * statistics.  Keep all consumers behind these methods so the old global
 * {@code StatBase} ownership remains shared rather than becoming item-local
 * NBT counters.
 */
public final class LegacyHbmStatistics {
    private static final DeferredRegister<ResourceLocation> CUSTOM_STATS =
            DeferredRegister.create(Registries.CUSTOM_STAT, HbmNtm.MOD_ID);

    public static final RegistryObject<ResourceLocation> NTM_BULLETS =
            CUSTOM_STATS.register("ntm_bullets", () -> new ResourceLocation(HbmNtm.MOD_ID, "ntm_bullets"));

    private LegacyHbmStatistics() {
    }

    public static void register(IEventBus modBus) {
        CUSTOM_STATS.register(modBus);
    }

    public static Stat<ResourceLocation> bulletsFired() {
        return Stats.CUSTOM.get(NTM_BULLETS.get());
    }

    /** Source {@code MainRegistry.statBullets}; called once for every legacy Sedna receiver fire. */
    public static void awardBulletFired(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.awardStat(bulletsFired());
        }
    }
}
