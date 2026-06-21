package com.hbm.ntm.radiation;

import com.hbm.ntm.api.RadiationImmune;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.registry.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;

public final class RadiationUtil {
    private static final Set<Class<?>> REGISTERED_IMMUNE_ENTITIES = ConcurrentHashMap.newKeySet();
    @SuppressWarnings("rawtypes")
    private static final HashSet<Class> LEGACY_IMMUNE_ENTITIES = new LegacyImmuneEntitySet();

    public static float calculateRadiationMod(LivingEntity entity) {
        return RadiationResistance.calculateRadiationMod(entity);
    }

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
        if (entity instanceof Player player) {
            if (player.isCreative() || player.tickCount < 200) {
                return;
            }
            if (ArmorUtil.checkForDigamma(player)) {
                return;
            }
        }
        RadiationData.incrementDigamma(entity, amount);
    }

    public static void applyDigammaItemHazard(LivingEntity entity, float level, int stackCount) {
        applyDigammaData(entity, level / 20.0F);
    }

    public static void applyDigammaDirect(LivingEntity entity, float amount) {
        if (isRadiationImmuneMarker(entity)) {
            return;
        }
        if (entity instanceof Player player && player.isCreative()) {
            return;
        }
        RadiationData.incrementDigamma(entity, amount);
    }

    public static boolean isRadiationImmuneMarker(Entity entity) {
        return entity instanceof RadiationImmune;
    }

    @SuppressWarnings("rawtypes")
    public static HashSet<Class> legacyImmuneEntitiesView() {
        return LEGACY_IMMUNE_ENTITIES;
    }

    @SuppressWarnings("rawtypes")
    public static void registerRadImmune(Class type) {
        if (type != null) {
            REGISTERED_IMMUNE_ENTITIES.add(type);
        }
    }

    public static boolean isRegisteredRadImmune(Entity entity) {
        if (entity == null) {
            return false;
        }
        ensureLegacyDefaultImmuneEntities();
        Class<?> entityType = entity.getClass();
        for (Class<?> immuneType : REGISTERED_IMMUNE_ENTITIES) {
            if (immuneType.isAssignableFrom(entityType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRadImmune(LivingEntity entity) {
        return isRegisteredRadImmune(entity)
                || isRadiationImmuneMarker(entity)
                || entity.hasEffect(ModEffects.MUTATION.get())
                || isLegacyImmuneEntityName(entity);
    }

    private static boolean isDigammaDataImmune(LivingEntity entity) {
        return entity.hasEffect(ModEffects.STABILITY.get())
                || entity instanceof Ocelot
                || hasLegacyClassName(entity, "EntityDuck");
    }

    private static boolean isLegacyImmuneEntityName(LivingEntity entity) {
        return hasLegacyClassName(entity, "CreeperNuclear")
                || hasLegacyClassName(entity, "EntityCreeperNuclear")
                || hasLegacyClassName(entity, "EntityCreeperTainted")
                || hasLegacyClassName(entity, "EntityQuackos")
                || hasLegacyClassName(entity, "cyano.lootable.entities.EntityLootableBody");
    }

    public static boolean hasLegacyClassName(Entity entity, String legacyName) {
        if (entity == null || legacyName == null || legacyName.isEmpty()) {
            return false;
        }
        boolean fullName = legacyName.indexOf('.') >= 0;
        Class<?> type = entity.getClass();
        while (type != null) {
            String typeName = type.getName();
            if (fullName ? typeName.equals(legacyName)
                    : type.getSimpleName().equals(legacyName) || typeName.endsWith("." + legacyName)) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
    }

    private static void ensureLegacyDefaultImmuneEntities() {
        if (!REGISTERED_IMMUNE_ENTITIES.isEmpty()) {
            return;
        }
        REGISTERED_IMMUNE_ENTITIES.add(MushroomCow.class);
        REGISTERED_IMMUNE_ENTITIES.add(Zombie.class);
        REGISTERED_IMMUNE_ENTITIES.add(Skeleton.class);
        REGISTERED_IMMUNE_ENTITIES.add(Ocelot.class);
        REGISTERED_IMMUNE_ENTITIES.add(RadiationImmune.class);
    }

    public static void applyRadiationEffect(LivingEntity entity, int amplifier) {
        contaminate(entity, (amplifier + 1.0F) * 0.05F, false);
    }

    public static void applyRadaway(LivingEntity entity, float amount) {
        RadiationData.incrementRadiation(entity, -amount);
    }

    public static boolean applyAsbestos(LivingEntity entity, int amount, int filterDamage) {
        return applyAsbestos(entity, amount, filterDamage, false, true);
    }

    public static boolean applyAsbestosExposure(LivingEntity entity, int amount, int filterDamage) {
        return applyAsbestos(entity, amount, filterDamage, true, false);
    }

    public static boolean applyAsbestosGasExposure(LivingEntity entity, int amount) {
        if (ArmorUtil.hasProtection(entity, 3, HazardClass.PARTICLE_FINE)) {
            return false;
        }
        RadiationData.incrementAsbestos(entity, amount);
        return true;
    }

    private static boolean applyAsbestos(LivingEntity entity, int amount, int filterDamage,
            boolean playerExposureProtection, boolean respectHazardDisable) {
        if (respectHazardDisable && RadiationConfig.asbestosHazardDisabled()) {
            return false;
        }
        if (playerExposureProtection && blocksNewPlayerOrCreative(entity)) {
            return false;
        }
        if (ArmorUtil.hasProtectionAndDamageFilter(entity, 3, HazardClass.PARTICLE_FINE, filterDamage)) {
            return false;
        }
        RadiationData.incrementAsbestos(entity, amount);
        return true;
    }

    public static boolean applyCoalDust(LivingEntity entity, int amount, int filterDamage, int filterDamageChance) {
        return applyCoalDust(entity, amount, filterDamage, filterDamageChance, false, true);
    }

    public static boolean applyCoalDustExposure(LivingEntity entity, int amount, int filterDamage, int filterDamageChance) {
        return applyCoalDust(entity, amount, filterDamage, filterDamageChance, true, false);
    }

    public static boolean applyCoalGasExposure(LivingEntity entity, int amount) {
        if (ArmorUtil.hasProtection(entity, 3, HazardClass.PARTICLE_COARSE)) {
            return false;
        }
        RadiationData.incrementBlackLung(entity, amount);
        return true;
    }

    private static boolean applyCoalDust(LivingEntity entity, int amount, int filterDamage, int filterDamageChance,
            boolean playerExposureProtection, boolean respectHazardDisable) {
        if (respectHazardDisable && RadiationConfig.coalHazardDisabled()) {
            return false;
        }
        if (playerExposureProtection && blocksNewPlayerOrCreative(entity)) {
            return false;
        }
        if (ArmorUtil.hasProtection(entity, 3, HazardClass.PARTICLE_COARSE)) {
            if (entity.getRandom().nextInt(Math.max(filterDamageChance, 1)) == 0) {
                ArmorUtil.damageGasMaskFilter(entity, filterDamage);
            }
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
        float playerRad = truncate1(RadiationData.getRadiation(player));
        float envRad = truncate1(RadiationData.getRadBuf(player));
        float chunkRad = truncate1(ChunkRadiationManager.getRadiation(player.level(), player.blockPosition()));
        float resistanceCoefficient = truncate2(HazmatRegistry.getResistance(player));
        float resistance = truncate2(100.0F - RadiationResistance.calculateRadiationModifier(player) * 100.0F);
        ChatFormatting resistancePrefix = resistanceCoefficient > 0.0F ? ChatFormatting.GREEN : ChatFormatting.WHITE;

        player.displayClientMessage(geigerTitle("geiger.title"), false);
        player.displayClientMessage(geigerLine("geiger.chunkRad", chunkRad + " RAD/s", radiationPrefix(chunkRad)), false);
        player.displayClientMessage(geigerLine("geiger.envRad", envRad + " RAD/s", radiationPrefix(envRad)), false);
        player.displayClientMessage(geigerLine("geiger.playerRad", playerRad + " RAD", storedRadiationPrefix(playerRad)), false);
        player.displayClientMessage(geigerLine("geiger.playerRes",
                resistance + "% (" + resistanceCoefficient + ")", resistancePrefix), false);
    }

    public static void printDosimeterData(Player player) {
        float envRad = truncate1(RadiationData.getRadBuf(player));
        boolean limited = envRad > 3.6F;
        float displayed = limited ? 3.6F : envRad;

        player.displayClientMessage(geigerTitle("geiger.title.dosimeter"), false);
        player.displayClientMessage(geigerLine("geiger.envRad",
                (limited ? ">" : "") + displayed + " RAD/s", radiationPrefix(displayed)), false);
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
        float digamma = truncate2(getDigamma(player));
        float healthInfluence = truncate2((1.0F - (float) Math.pow(0.5D, digamma)) * 100.0F);

        player.displayClientMessage(digammaTitle(), false);
        player.displayClientMessage(digammaLine("digamma.playerDigamma", digamma + " DRX", ChatFormatting.RED), false);
        player.displayClientMessage(digammaLine("digamma.playerHealth", healthInfluence + "%", ChatFormatting.RED), false);
        player.displayClientMessage(digammaLine("digamma.playerRes", "N/A", ChatFormatting.BLUE), false);
    }

    private static MutableComponent geigerTitle(String key) {
        return Component.literal("===== \u2622 ")
                .append(Component.translatable(key))
                .append(Component.literal(" \u2622 ====="))
                .withStyle(ChatFormatting.GOLD);
    }

    private static MutableComponent geigerLine(String key, String value, ChatFormatting valueStyle) {
        return Component.translatable(key)
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" " + value).withStyle(valueStyle));
    }

    private static MutableComponent digammaTitle() {
        return Component.literal("===== \u03DC ")
                .append(Component.translatable("digamma.title"))
                .append(Component.literal(" \u03DC ====="))
                .withStyle(ChatFormatting.DARK_PURPLE);
    }

    private static MutableComponent digammaLine(String key, String value, ChatFormatting valueStyle) {
        return Component.translatable(key)
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(Component.literal(" " + value).withStyle(valueStyle));
    }

    public static void addRadiationPoisoning(LivingEntity entity, int durationTicks, int amplifier) {
        entity.addEffect(new MobEffectInstance(ModEffects.RADIATION.get(), durationTicks, amplifier));
    }

    private static float truncate1(float value) {
        return (int) (value * 10.0F) / 10.0F;
    }

    private static float truncate2(float value) {
        return (int) (value * 100.0F) / 100.0F;
    }

    private RadiationUtil() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class LegacyImmuneEntitySet extends HashSet<Class> {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean add(Class type) {
            if (type == null) {
                return false;
            }
            return REGISTERED_IMMUNE_ENTITIES.add(type);
        }

        @Override
        public boolean remove(Object type) {
            return REGISTERED_IMMUNE_ENTITIES.remove(type);
        }

        @Override
        public boolean contains(Object type) {
            return REGISTERED_IMMUNE_ENTITIES.contains(type);
        }

        @Override
        public Iterator<Class> iterator() {
            return (Iterator) Set.copyOf(REGISTERED_IMMUNE_ENTITIES).iterator();
        }

        @Override
        public Spliterator<Class> spliterator() {
            return (Spliterator) Set.copyOf(REGISTERED_IMMUNE_ENTITIES).spliterator();
        }

        @Override
        public Object[] toArray() {
            return Set.copyOf(REGISTERED_IMMUNE_ENTITIES).toArray();
        }

        @Override
        public <T> T[] toArray(T[] array) {
            return Set.copyOf(REGISTERED_IMMUNE_ENTITIES).toArray(array);
        }

        @Override
        public int size() {
            return REGISTERED_IMMUNE_ENTITIES.size();
        }

        @Override
        public boolean isEmpty() {
            return REGISTERED_IMMUNE_ENTITIES.isEmpty();
        }

        @Override
        public void clear() {
            REGISTERED_IMMUNE_ENTITIES.clear();
        }

        @Override
        public boolean addAll(java.util.Collection<? extends Class> types) {
            boolean changed = false;
            for (Class type : types) {
                changed |= add(type);
            }
            return changed;
        }

        @Override
        public boolean removeAll(java.util.Collection<?> types) {
            return REGISTERED_IMMUNE_ENTITIES.removeAll(types);
        }

        @Override
        public boolean retainAll(java.util.Collection<?> types) {
            return REGISTERED_IMMUNE_ENTITIES.retainAll(types);
        }
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
