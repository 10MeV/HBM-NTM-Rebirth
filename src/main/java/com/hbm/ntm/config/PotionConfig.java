package com.hbm.ntm.config;

import com.hbm.ntm.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeConfigSpec;

public final class PotionConfig {
    public static ForgeConfigSpec.EnumValue<PotionSicknessMode> POTION_SICKNESS;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("potions");
        POTION_SICKNESS = builder
                .comment("Legacy PotionConfig 8.S0_potionSickness: OFF disables it, NORMAL applies the given duration, TERRARIA multiplies duration by 12.")
                .defineEnum("potionSickness", PotionSicknessMode.OFF);
        builder.pop();
    }

    public static PotionSicknessMode potionSicknessMode() {
        try {
            return POTION_SICKNESS == null ? PotionSicknessMode.OFF : POTION_SICKNESS.get();
        } catch (IllegalStateException ignored) {
            return PotionSicknessMode.OFF;
        }
    }

    public static boolean potionSicknessEnabled() {
        return potionSicknessMode() != PotionSicknessMode.OFF;
    }

    public static void applyPotionSickness(LivingEntity entity, int durationSeconds) {
        PotionSicknessMode mode = potionSicknessMode();
        if (entity == null || mode == PotionSicknessMode.OFF || durationSeconds <= 0) {
            return;
        }
        int multiplier = mode == PotionSicknessMode.TERRARIA ? 12 : 1;
        entity.addEffect(new MobEffectInstance(ModEffects.POTION_SICKNESS.get(), durationSeconds * multiplier * 20, 0, false, true));
    }

    public static boolean hasPotionSickness(LivingEntity entity) {
        return entity != null && entity.hasEffect(ModEffects.POTION_SICKNESS.get());
    }

    public enum PotionSicknessMode {
        OFF,
        NORMAL,
        TERRARIA
    }

    private PotionConfig() {
    }
}
