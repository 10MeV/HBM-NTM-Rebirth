package com.hbm.ntm.radiation;

import com.hbm.ntm.api.RadiationImmune;
import com.hbm.ntm.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;

public final class RadiationUtil {
    public static void contaminate(LivingEntity entity, float amount, boolean bypassResistance) {
        contaminate(entity, HazardType.RADIATION, bypassResistance ? ContaminationType.RAD_BYPASS : ContaminationType.CREATIVE, amount);
    }

    public static boolean contaminate(LivingEntity entity, HazardType hazard, ContaminationType contamination, float amount) {
        if (amount <= 0.0F) {
            return false;
        }

        if (hazard == HazardType.RADIATION) {
            RadiationData.setRadEnv(entity, RadiationData.getRadEnv(entity) + amount);
        }

        if (!canContaminate(entity, hazard, contamination)) {
            return false;
        }

        if (hazard == HazardType.RADIATION && isRadImmune(entity)) {
            return false;
        }

        switch (hazard) {
            case RADIATION -> {
                float modifier = contamination == ContaminationType.RAD_BYPASS ? 1.0F : RadiationResistance.calculateRadiationModifier(entity);
                RadiationData.incrementRadiation(entity, amount * modifier);
            }
            case DIGAMMA -> RadiationData.incrementDigamma(entity, amount);
            default -> {
                return false;
            }
        }
        return true;
    }

    private static boolean canContaminate(LivingEntity entity, HazardType hazard, ContaminationType contamination) {
        if (!(entity instanceof Player player)) {
            return true;
        }

        if (blocksPlayerContamination(player, contamination)) {
            return false;
        }

        if (player.isCreative() && contamination != ContaminationType.NONE && contamination != ContaminationType.DIGAMMA2) {
            return false;
        }

        if (player.tickCount < 200) {
            return false;
        }

        return true;
    }

    private static boolean blocksPlayerContamination(Player player, ContaminationType contamination) {
        return switch (contamination) {
            case FARADAY -> ArmorUtil.checkForFaraday(player);
            case HAZMAT -> ArmorUtil.checkForHazmat(player);
            case HAZMAT2 -> ArmorUtil.checkForHaz2(player);
            case DIGAMMA -> ArmorUtil.checkForDigamma(player) || ArmorUtil.checkForDigamma2(player);
            case DIGAMMA2 -> ArmorUtil.checkForDigamma2(player);
            case CREATIVE, RAD_BYPASS, NONE -> false;
        };
    }

    public static void applyDigammaData(LivingEntity entity, float amount) {
        if (isDigammaDataImmune(entity)) {
            return;
        }
        contaminate(entity, HazardType.DIGAMMA, ContaminationType.DIGAMMA, amount);
    }

    public static void applyDigammaDirect(LivingEntity entity, float amount) {
        if (amount <= 0.0F || entity instanceof RadiationImmune || isLegacyImmuneEntityName(entity)) {
            return;
        }
        if (entity instanceof Player player && player.isCreative()) {
            return;
        }
        RadiationData.incrementDigamma(entity, amount);
    }

    public static boolean isRadImmune(LivingEntity entity) {
        return entity instanceof RadiationImmune
                || entity.hasEffect(ModEffects.MUTATION.get())
                || entity instanceof MushroomCow
                || entity instanceof Zombie
                || entity instanceof Skeleton
                || entity instanceof Ocelot
                || isLegacyImmuneEntityName(entity);
    }

    private static boolean isDigammaDataImmune(LivingEntity entity) {
        return entity instanceof Ocelot
                || isLegacyClassName(entity, "EntityDuck");
    }

    private static boolean isLegacyImmuneEntityName(LivingEntity entity) {
        return isLegacyClassName(entity, "CreeperNuclear")
                || isLegacyClassName(entity, "EntityQuackos")
                || isLegacyClassName(entity, "EntityLootableBody");
    }

    private static boolean isLegacyClassName(LivingEntity entity, String simpleName) {
        Class<?> type = entity.getClass();
        while (type != null) {
            if (type.getSimpleName().equals(simpleName) || type.getName().endsWith("." + simpleName)) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
    }

    public static void applyRadiationEffect(LivingEntity entity, int amplifier) {
        contaminate(entity, (amplifier + 1.0F) * 0.05F, false);
    }

    public static void applyRadaway(LivingEntity entity, float amount) {
        RadiationData.incrementRadiation(entity, -amount);
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

    public static void printDiagnosticData(Player player) {
        float digamma = Math.round(RadiationData.getDigamma(player) * 100.0F) / 100.0F;
        float healthInfluence = Math.round((1.0F - (float) Math.pow(0.5D, digamma)) * 10000.0F) / 100.0F;

        player.displayClientMessage(Component.translatable("digamma.title"), false);
        player.displayClientMessage(Component.translatable("digamma.playerDigamma", digamma), false);
        player.displayClientMessage(Component.translatable("digamma.playerHealth", healthInfluence), false);
        player.displayClientMessage(Component.translatable("digamma.playerRes", "N/A"), false);
    }

    public static void addRadiationPoisoning(LivingEntity entity, int durationTicks, int amplifier) {
        entity.addEffect(new MobEffectInstance(ModEffects.RADIATION.get(), durationTicks, amplifier));
    }

    private static float round(float value) {
        return Math.round(value * 10.0F) / 10.0F;
    }

    private RadiationUtil() {
    }

    public enum ContaminationType {
        FARADAY,
        HAZMAT,
        HAZMAT2,
        DIGAMMA,
        DIGAMMA2,
        CREATIVE,
        RAD_BYPASS,
        NONE
    }
}
