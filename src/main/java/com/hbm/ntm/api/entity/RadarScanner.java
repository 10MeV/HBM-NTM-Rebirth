package com.hbm.ntm.api.entity;

import com.hbm.ntm.player.HbmLivingProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class RadarScanner {
    public static final double LEGACY_DIGAMMA_JAM_THRESHOLD = 0.001D;
    private static final List<Class<?>> candidateTypes = new ArrayList<>();
    private static final List<RadarConverter> converters = new ArrayList<>();
    private static final List<Entity> matchingEntities = new ArrayList<>();
    private static boolean matchingEntitiesInitialized;

    static {
        registerLegacyCandidateTypes();
        registerLegacyConverters();
    }

    public static RadarScanResult scan(RadarContext context) {
        return scan(context, RadarScanner::isLegacyDigammaJammer);
    }

    public static RadarScanResult scan(RadarContext context, Predicate<LivingEntity> jammerPredicate) {
        if (context.origin().getY() < context.minimumAltitude()) {
            return RadarScanResult.EMPTY;
        }

        Predicate<LivingEntity> jammer = jammerPredicate != null ? jammerPredicate : living -> false;
        List<RadarEntry> entries = new ArrayList<>();

        for (Entity entity : candidateEntities(context)) {
            if (entity.isRemoved()) {
                continue;
            }
            if (!isWithinLegacyRadarVolume(entity, context)) {
                continue;
            }
            if (entity instanceof LivingEntity living && jammer.test(living)) {
                return new RadarScanResult(List.of(), true);
            }

            RadarEntry entry = entryFor(entity, context);
            if (entry != null) {
                entries.add(entry);
            }
        }

        return new RadarScanResult(entries, false);
    }

    public static void registerCandidateType(Class<?> type) {
        if (type != null && !candidateTypes.contains(type)) {
            candidateTypes.add(type);
        }
    }

    public static List<Class<?>> candidateTypesSnapshot() {
        return List.copyOf(candidateTypes);
    }

    public static void registerConverter(RadarConverter converter) {
        if (converter != null && !converters.contains(converter)) {
            converters.add(converter);
        }
    }

    public static List<RadarConverter> convertersSnapshot() {
        return List.copyOf(converters);
    }

    public static void updateSystem(Iterable<ServerLevel> levels) {
        matchingEntities.clear();
        if (levels == null) {
            matchingEntitiesInitialized = false;
            return;
        }

        for (ServerLevel level : levels) {
            for (Entity entity : level.getAllEntities()) {
                if (!entity.isRemoved() && isRadarCandidateClass(entity)) {
                    matchingEntities.add(entity);
                }
            }
        }
        matchingEntitiesInitialized = true;
    }

    public static List<Entity> matchingEntitiesSnapshot() {
        return List.copyOf(matchingEntities);
    }

    public static AABB scanBounds(RadarContext context) {
        BlockPos origin = context.origin();
        double centerX = origin.getX() + 0.5D;
        double centerZ = origin.getZ() + 0.5D;
        return new AABB(
                centerX - context.range(),
                origin.getY() + context.verticalBuffer(),
                centerZ - context.range(),
                centerX + context.range(),
                context.level().getMaxBuildHeight(),
                centerZ + context.range());
    }

    public static boolean isWithinLegacyRadarVolume(Entity entity, RadarContext context) {
        BlockPos origin = context.origin();
        return entity.level() == context.level()
                && Math.abs(entity.getX() - (origin.getX() + 0.5D)) <= context.range()
                && Math.abs(entity.getZ() - (origin.getZ() + 0.5D)) <= context.range()
                && entity.getY() - origin.getY() > context.verticalBuffer();
    }

    public static int proximityRedstone(List<RadarEntry> entries, BlockPos origin, int range) {
        if (entries.isEmpty() || range <= 0) {
            return 0;
        }

        double maxRange = range * Math.sqrt(2.0D);
        int power = 0;
        for (RadarEntry entry : entries) {
            if (!entry.redstone()) {
                continue;
            }
            double dist = Math.sqrt(Math.pow(entry.pos().getX() - origin.getX(), 2.0D)
                    + Math.pow(entry.pos().getZ() - origin.getZ(), 2.0D));
            int candidate = 15 - (int) Math.floor(dist / maxRange * 15.0D);
            power = Math.max(power, Mth.clamp(candidate, 0, 15));
        }
        return power;
    }

    public static int tierRedstone(List<RadarEntry> entries) {
        int power = 0;
        for (RadarEntry entry : entries) {
            if (entry.redstone()) {
                power = Math.max(power, entry.blipLevel() + 1);
            }
        }
        return Mth.clamp(power, 0, 15);
    }

    public static int redstonePower(List<RadarEntry> entries, BlockPos origin, int range, boolean proximityMode) {
        return proximityMode ? proximityRedstone(entries, origin, range) : tierRedstone(entries);
    }

    @SuppressWarnings("deprecation")
    private static Iterable<Entity> candidateEntities(RadarContext context) {
        if (matchingEntitiesInitialized) {
            return matchingEntities;
        }
        return context.level().getEntities((Entity) null, scanBounds(context), RadarScanner::isRadarCandidateClass);
    }

    @SuppressWarnings("deprecation")
    private static boolean isRadarCandidateClass(Entity entity) {
        for (Class<?> type : candidateTypes) {
            if (type.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }

    private static RadarEntry entryFor(Entity entity, RadarContext context) {
        ConversionContext conversion = new ConversionContext(entity, context, context.params());
        for (RadarConverter converter : converters) {
            RadarEntry entry = converter.convert(conversion);
            if (entry != null) {
                return entry;
            }
        }
        return null;
    }

    private static boolean isLegacyDigammaJammer(LivingEntity entity) {
        return HbmLivingProperties.getDigamma(entity) > LEGACY_DIGAMMA_JAM_THRESHOLD;
    }

    private static void registerLegacyCandidateTypes() {
        registerCandidateType(RadarDetectable.class);
        registerCandidateType(LegacyRadarDetectable.class);
        registerCandidateType(Player.class);
    }

    @SuppressWarnings("deprecation")
    private static void registerLegacyConverters() {
        registerConverter(conversion -> {
            Entity entity = conversion.entity();
            RadarDetectable.RadarScanParams params = conversion.params();
            RadarContext radar = conversion.radar();
            if (entity instanceof RadarDetectable detectable
                    && detectable.canBeSeenBy(radar)
                    && detectable.paramsApplicable(params)) {
                return RadarEntry.of(detectable, entity, detectable.suppliesRedstone(params));
            }
            return null;
        });
        registerConverter(conversion -> {
            Entity entity = conversion.entity();
            RadarDetectable.RadarScanParams params = conversion.params();
            if (entity instanceof LegacyRadarDetectable legacy
                    && params.scanMissiles()
                    && legacy.canBeDetectedByLegacyRadar()) {
                return RadarEntry.of(legacy, entity);
            }
            return null;
        });
        registerConverter(conversion -> {
            Entity entity = conversion.entity();
            RadarDetectable.RadarScanParams params = conversion.params();
            if (entity instanceof Player player && params.scanPlayers()) {
                return RadarEntry.of(player);
            }
            return null;
        });
    }

    public record ConversionContext(Entity entity, RadarContext radar, RadarDetectable.RadarScanParams params) {
    }

    @FunctionalInterface
    public interface RadarConverter {
        RadarEntry convert(ConversionContext context);
    }

    private RadarScanner() {
    }
}
