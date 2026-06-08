package com.hbm.ntm.radiation;

import com.hbm.ntm.api.RadiationImmune;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.registry.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;

public final class RadiationUtil {
    public static float getRads(LivingEntity entity) {
        if (isRadImmune(entity)) {
            return 0.0F;
        }
        return RadiationData.getRadiation(entity);
    }

    public static float getDigamma(LivingEntity entity) {
        return RadiationData.getDigamma(entity);
    }

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
        if (entity.hasEffect(ModEffects.STABILITY.get())) {
            return;
        }
        contaminate(entity, HazardType.DIGAMMA, ContaminationType.DIGAMMA, amount);
    }

    public static void applyDigammaItemHazard(LivingEntity entity, float level, int stackCount) {
        if (level <= 0.0F || stackCount <= 0) {
            return;
        }
        applyDigammaData(entity, level * stackCount / 20.0F);
    }

    public static void applyDigammaDirect(LivingEntity entity, float amount) {
        if (amount <= 0.0F || entity instanceof RadiationImmune) {
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

    public static boolean applyAsbestos(LivingEntity entity, int amount, int filterDamage) {
        return applyAsbestos(entity, amount, filterDamage, false);
    }

    public static boolean applyAsbestosExposure(LivingEntity entity, int amount, int filterDamage) {
        return applyAsbestos(entity, amount, filterDamage, true);
    }

    private static boolean applyAsbestos(LivingEntity entity, int amount, int filterDamage, boolean playerExposureProtection) {
        if (amount <= 0) {
            return false;
        }
        if (RadiationConfig.asbestosHazardDisabled()) {
            return false;
        }
        if (playerExposureProtection && blocksNewPlayerOrCreative(entity)) {
            return false;
        }
        if (ArmorUtil.hasProtectionAndDamageFilter(entity, HazardClass.PARTICLE_FINE, filterDamage)) {
            return false;
        }
        RadiationData.incrementAsbestos(entity, amount);
        return true;
    }

    public static boolean applyCoalDust(LivingEntity entity, int amount, int filterDamage, int filterDamageChance) {
        return applyCoalDust(entity, amount, filterDamage, filterDamageChance, false);
    }

    public static boolean applyCoalDustExposure(LivingEntity entity, int amount, int filterDamage, int filterDamageChance) {
        return applyCoalDust(entity, amount, filterDamage, filterDamageChance, true);
    }

    private static boolean applyCoalDust(LivingEntity entity, int amount, int filterDamage, int filterDamageChance,
            boolean playerExposureProtection) {
        if (amount <= 0) {
            return false;
        }
        if (RadiationConfig.coalHazardDisabled()) {
            return false;
        }
        if (playerExposureProtection && blocksNewPlayerOrCreative(entity)) {
            return false;
        }
        int actualFilterDamage = filterDamage > 0 && entity.getRandom().nextInt(Math.max(filterDamageChance, 1)) == 0
                ? filterDamage
                : 0;
        if (ArmorUtil.hasProtectionAndDamageFilter(entity, HazardClass.PARTICLE_COARSE, actualFilterDamage)) {
            return false;
        }
        RadiationData.incrementBlackLung(entity, amount);
        return true;
    }

    private static boolean blocksNewPlayerOrCreative(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        return player.isCreative() || player.tickCount < 200;
    }

    public static void printGeigerData(Player player) {
        float playerRad = getRads(player);
        float envRad = RadiationData.getRadBuf(player);
        float chunkRad = ChunkRadiationManager.getRadiation(player.level(), player.blockPosition());
        float resistanceCoefficient = HazmatRegistry.getResistance(player);
        float resistance = (1.0F - RadiationResistance.calculateRadiationModifier(player)) * 100.0F;
        ChatFormatting resistancePrefix = resistanceCoefficient > 0.0F ? ChatFormatting.GREEN : ChatFormatting.WHITE;

        player.displayClientMessage(Component.translatable("geiger.title"), false);
        player.displayClientMessage(Component.translatable("geiger.chunkRad",
                Component.literal(String.valueOf(round(chunkRad))).withStyle(radiationPrefix(chunkRad))), false);
        player.displayClientMessage(Component.translatable("geiger.envRad",
                Component.literal(String.valueOf(round(envRad))).withStyle(radiationPrefix(envRad))), false);
        player.displayClientMessage(Component.translatable("geiger.playerRad",
                Component.literal(String.valueOf(round(playerRad))).withStyle(storedRadiationPrefix(playerRad))), false);
        player.displayClientMessage(Component.translatable("geiger.playerRes",
                Component.literal(String.valueOf(round2(resistance))).withStyle(resistancePrefix),
                Component.literal(String.valueOf(round2(resistanceCoefficient))).withStyle(resistancePrefix)), false);
    }

    public static void printDosimeterData(Player player) {
        float envRad = round(RadiationData.getRadBuf(player));
        boolean limited = envRad > 3.6F;
        float displayed = limited ? 3.6F : envRad;

        player.displayClientMessage(Component.translatable("geiger.title.dosimeter"), false);
        player.displayClientMessage(Component.translatable("geiger.envRad",
                Component.literal((limited ? ">" : "") + displayed).withStyle(radiationPrefix(displayed))), false);
    }

    public static ChatFormatting radiationPrefix(double rads) {
        if (rads == 0.0D) {
            return ChatFormatting.GREEN;
        }
        if (rads < 1.0D) {
            return ChatFormatting.YELLOW;
        }
        if (rads < 10.0D) {
            return ChatFormatting.GOLD;
        }
        if (rads < 100.0D) {
            return ChatFormatting.RED;
        }
        if (rads < 1000.0D) {
            return ChatFormatting.DARK_RED;
        }
        return ChatFormatting.DARK_GRAY;
    }

    public static ChatFormatting getPreffixFromRad(double rads) {
        return radiationPrefix(rads);
    }

    public static ChatFormatting storedRadiationPrefix(double rads) {
        if (rads < 200.0D) {
            return ChatFormatting.GREEN;
        }
        if (rads < 400.0D) {
            return ChatFormatting.YELLOW;
        }
        if (rads < 600.0D) {
            return ChatFormatting.GOLD;
        }
        if (rads < 800.0D) {
            return ChatFormatting.RED;
        }
        if (rads < 1000.0D) {
            return ChatFormatting.DARK_RED;
        }
        return ChatFormatting.DARK_GRAY;
    }

    public static void printDiagnosticData(Player player) {
        float digamma = Math.round(getDigamma(player) * 100.0F) / 100.0F;
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

    private static float round2(float value) {
        return Math.round(value * 100.0F) / 100.0F;
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
