package com.hbm.ntm.api.entity;

import com.hbm.ntm.player.HbmLivingProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
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
        RadarScanVolume volume = context.volume();
        if (!volume.isOperationalAltitude()) {
            return RadarScanResult.EMPTY;
        }

        Predicate<LivingEntity> jammer = jammerPredicate != null ? jammerPredicate : living -> false;
        List<RadarEntry> entries = new ArrayList<>();

        for (Entity entity : candidateEntities(volume)) {
            if (entity.isRemoved()) {
                continue;
            }
            if (!volume.contains(entity)) {
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
        return context.volume().bounds();
    }

    public static boolean isWithinLegacyRadarVolume(Entity entity, RadarContext context) {
        return context.volume().contains(entity);
    }

    public static int proximityRedstone(List<RadarEntry> entries, BlockPos origin, int range) {
        return RadarRedstoneMode.PROXIMITY.power(entries, origin, range);
    }

    public static int tierRedstone(List<RadarEntry> entries) {
        return RadarRedstoneMode.TIER.power(entries, null, 0);
    }

    public static int redstonePower(List<RadarEntry> entries, BlockPos origin, int range, boolean proximityMode) {
        return RadarRedstoneMode.fromLegacyFlag(proximityMode).power(entries, origin, range);
    }

    @SuppressWarnings("deprecation")
    private static Iterable<Entity> candidateEntities(RadarScanVolume volume) {
        if (matchingEntitiesInitialized) {
            return matchingEntities;
        }
        return volume.level().getEntities((Entity) null, volume.bounds(), RadarScanner::isRadarCandidateClass);
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
