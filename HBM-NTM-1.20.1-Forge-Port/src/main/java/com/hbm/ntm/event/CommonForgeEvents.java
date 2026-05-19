package com.hbm.ntm.event;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.PlayerRadiationSyncPacket;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.radiation.HazmatRegistry;
import com.hbm.ntm.radiation.ItemRadiationRegistry;
import com.hbm.ntm.radiation.RadiationData;
import com.hbm.ntm.radiation.RadiationResistance;
import com.hbm.ntm.radiation.RadiationUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonForgeEvents {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer() == null) {
            return;
        }

        for (ServerLevel level : event.getServer().getAllLevels()) {
            ChunkRadiationManager.tick(level);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        if (player.tickCount % 20 != 0) {
            return;
        }

        float itemRadiation = 0.0F;
        for (ItemStack stack : player.getInventory().items) {
            itemRadiation += ItemRadiationRegistry.getRadiation(stack) * stack.getCount();
        }
        for (ItemStack stack : player.getInventory().armor) {
            itemRadiation += ItemRadiationRegistry.getRadiation(stack) * stack.getCount();
        }
        for (ItemStack stack : player.getInventory().offhand) {
            itemRadiation += ItemRadiationRegistry.getRadiation(stack) * stack.getCount();
        }

        float chunkRadiation = ChunkRadiationManager.getRadiation(player.level(), player.blockPosition());
        float effectiveRadiation = (itemRadiation + chunkRadiation) * RadiationResistance.calculateRadiationModifier(player);
        RadiationData.setRadBuf(player, effectiveRadiation);
        if (effectiveRadiation > 0.0F) {
            RadiationData.incrementRadiation(player, effectiveRadiation);
        }

        float totalRadiation = RadiationData.getRadiation(player);
        if (totalRadiation >= 1000.0F) {
            player.hurt(player.damageSources().magic(), 6.0F);
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 6, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 8, 1));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 8, 0));
        } else if (totalRadiation >= 800.0F) {
            player.hurt(player.damageSources().magic(), 3.0F);
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 6, 0));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 10, 1));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 6, 0));
        } else if (totalRadiation >= 600.0F) {
            player.hurt(player.damageSources().magic(), 1.0F);
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 8, 0));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 8, 0));
            if (player.getRandom().nextInt(3) == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 5, 0));
            }
        } else if (totalRadiation >= 200.0F) {
            RadiationUtil.addRadiationPoisoning(player, 20 * 10, 0);
            if (player.getRandom().nextInt(5) == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 5, 0));
            }
            if (player.getRandom().nextInt(8) == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 4, 0));
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            ModMessages.sendToPlayer(new PlayerRadiationSyncPacket(
                    totalRadiation,
                    effectiveRadiation,
                    chunkRadiation,
                    HazmatRegistry.getResistance(player)), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        RadiationData.copyForRespawn(event.getOriginal(), event.getEntity());
    }

    private CommonForgeEvents() {
    }
}
