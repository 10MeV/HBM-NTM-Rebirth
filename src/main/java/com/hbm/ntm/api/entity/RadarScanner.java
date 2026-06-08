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

    public static RadarScanResult scan(RadarContext context) {
        return scan(context, RadarScanner::isLegacyDigammaJammer);
    }

    public static RadarScanResult scan(RadarContext context, Predicate<LivingEntity> jammerPredicate) {
        if (context.origin().getY() < context.minimumAltitude()) {
            return RadarScanResult.EMPTY;
        }

        Predicate<LivingEntity> jammer = jammerPredicate != null ? jammerPredicate : living -> false;
        List<RadarEntry> entries = new ArrayList<>();
        ServerLevel level = context.level();

        for (Entity entity : level.getEntities((Entity) null, scanBounds(context),
                candidate -> isRadarCandidate(candidate, context.params()))) {
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

    private static boolean isRadarCandidate(Entity entity, RadarDetectable.RadarScanParams params) {
        return entity instanceof RadarDetectable
                || entity instanceof LegacyRadarDetectable && params.scanMissiles()
                || entity instanceof Player && params.scanPlayers();
    }

    private static RadarEntry entryFor(Entity entity, RadarContext context) {
        RadarDetectable.RadarScanParams params = context.params();

        if (entity instanceof RadarDetectable detectable
                && detectable.canBeSeenBy(context)
                && detectable.paramsApplicable(params)) {
            return RadarEntry.of(detectable, entity, detectable.suppliesRedstone(params));
        }

        if (entity instanceof LegacyRadarDetectable legacy && params.scanMissiles()) {
            return RadarEntry.of(legacy, entity);
        }

        if (entity instanceof Player player && params.scanPlayers()) {
            return RadarEntry.of(player);
        }

        return null;
    }

    private static boolean isLegacyDigammaJammer(LivingEntity entity) {
        return HbmLivingProperties.getDigamma(entity) > LEGACY_DIGAMMA_JAM_THRESHOLD;
    }

    private RadarScanner() {
    }
}
