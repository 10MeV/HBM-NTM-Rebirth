package com.hbm.ntm.radiation;

import com.hbm.ntm.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class RadiationUtil {
    public static void contaminate(LivingEntity entity, float amount, boolean bypassResistance) {
        if (amount <= 0.0F) {
            return;
        }

        float modifier = bypassResistance ? 1.0F : RadiationResistance.calculateRadiationModifier(entity);
        RadiationData.setRadEnv(entity, RadiationData.getRadEnv(entity) + amount);
        RadiationData.incrementRadiation(entity, amount * modifier);
    }

    public static void applyRadiationEffect(LivingEntity entity, int amplifier) {
        contaminate(entity, (amplifier + 1.0F) * 0.05F, false);
    }

    public static void applyRadaway(LivingEntity entity, float amount) {
        RadiationData.incrementRadiation(entity, -amount);
        RadiationData.setRadEnv(entity, Math.max(RadiationData.getRadEnv(entity) - amount, 0.0F));
        entity.removeEffect(ModEffects.RADIATION.get());
    }

    public static void printGeigerData(Player player) {
        float playerRad = RadiationData.getRadiation(player);
        float envRad = RadiationData.getRadBuf(player);
        float chunkRad = ChunkRadiationManager.getRadiation(player.level(), player.blockPosition());
        float resistance = (1.0F - RadiationResistance.calculateRadiationModifier(player)) * 100.0F;

        player.displayClientMessage(Component.translatable("geiger.title"), false);
        player.displayClientMessage(Component.translatable("geiger.chunkRad", round(chunkRad)), false);
        player.displayClientMessage(Component.translatable("geiger.envRad", round(envRad)), false);
        player.displayClientMessage(Component.translatable("geiger.playerRad", round(playerRad)), false);
        player.displayClientMessage(Component.translatable("geiger.playerRes", round(resistance)), false);
    }

    public static void addRadiationPoisoning(LivingEntity entity, int durationTicks, int amplifier) {
        entity.addEffect(new MobEffectInstance(ModEffects.RADIATION.get(), durationTicks, amplifier));
    }

    private static float round(float value) {
        return Math.round(value * 10.0F) / 10.0F;
    }

    private RadiationUtil() {
    }
}
