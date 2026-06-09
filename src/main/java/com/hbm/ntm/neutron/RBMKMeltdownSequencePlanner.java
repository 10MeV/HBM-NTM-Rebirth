package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class RBMKMeltdownSequencePlanner {
    public static final String MUSHROOM_PARTICLE_TYPE = "rbmkmush";
    public static final String EXPLOSION_SOUND = "hbm:block.rbmk_explosion";
    public static final int MUSHROOM_PACKET_RANGE = 250;
    public static final int ACHIEVEMENT_RADIUS = 50;
    public static final int PRIBRIS_CONVERSION_RANDOM_DENOMINATOR = 3;
    public static final int MAX_OVERPRESSURE_PIPE_BLOCKS = 100;

    private RBMKMeltdownSequencePlanner() {
    }

    public static MeltdownSequencePlan planSequence(
            List<ColumnRef> columns,
            boolean digamma,
            boolean overpressureEnabled,
            int connectedPipeBlocks,
            int connectedPipeReceivers) {
        List<ColumnRef> safeColumns = columns == null ? List.of() : List.copyOf(columns);
        List<BlockPos> positions = safeColumns.stream().map(ColumnRef::origin).toList();
        RBMKMeltPlanner.MeltdownBounds bounds = RBMKMeltPlanner.bounds(positions);

        List<ColumnReducePlan> reducePlans = new ArrayList<>();
        for (ColumnRef column : safeColumns) {
            reducePlans.add(new ColumnReducePlan(
                    column,
                    RBMKMeltPlanner.edgeReduce(column.origin(), bounds)));
        }

        int smallDim = bounds == RBMKMeltPlanner.MeltdownBounds.EMPTY
                ? 0
                : Math.min(bounds.maxX() - bounds.minX(), bounds.maxZ() - bounds.minZ());
        BlockPos effectOrigin = bounds == RBMKMeltPlanner.MeltdownBounds.EMPTY
                ? BlockPos.ZERO
                : new BlockPos(bounds.centerX(), safeColumns.get(0).origin().getY() + 1, bounds.centerZ());

        return new MeltdownSequencePlan(
                false,
                true,
                bounds,
                reducePlans,
                postCoriumConversions(safeColumns, digamma),
                overpressurePlan(overpressureEnabled, connectedPipeBlocks, connectedPipeReceivers),
                new EffectPlan(MUSHROOM_PARTICLE_TYPE, smallDim, effectOrigin, MUSHROOM_PACKET_RANGE),
                new SoundPlan(EXPLOSION_SOUND, effectOrigin),
                ACHIEVEMENT_RADIUS,
                digamma,
                true);
    }

    private static List<PribrisConversionCandidate> postCoriumConversions(List<ColumnRef> columns, boolean digamma) {
        List<PribrisConversionCandidate> conversions = new ArrayList<>();
        PribrisConversionTarget target = digamma
                ? PribrisConversionTarget.DIGAMMA
                : PribrisConversionTarget.RADIATING;

        for (ColumnRef column : columns) {
            if (!column.coriumBlockAfterMelt()) {
                continue;
            }
            BlockPos origin = column.origin();
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        conversions.add(new PribrisConversionCandidate(
                                origin.offset(x, y, z),
                                PRIBRIS_CONVERSION_RANDOM_DENOMINATOR,
                                target));
                    }
                }
            }
        }
        return conversions;
    }

    private static OverpressurePlan overpressurePlan(boolean enabled, int connectedPipeBlocks,
            int connectedPipeReceivers) {
        if (!enabled) {
            return new OverpressurePlan(false, 0, 0, 0);
        }
        int pipeBlocks = Math.max(0, connectedPipeBlocks);
        int receivers = Math.max(0, connectedPipeReceivers);
        return new OverpressurePlan(
                true,
                pipeBlocks,
                Math.min(pipeBlocks / 5, MAX_OVERPRESSURE_PIPE_BLOCKS),
                receivers);
    }

    public record ColumnRef(
            BlockPos origin,
            RBMKColumnLifecyclePlanner.ColumnKind kind,
            boolean coriumBlockAfterMelt) {
    }

    public record ColumnReducePlan(ColumnRef column, int reduce) {
    }

    public enum PribrisConversionTarget {
        RADIATING,
        DIGAMMA
    }

    public record PribrisConversionCandidate(
            BlockPos pos,
            int randomDenominator,
            PribrisConversionTarget target) {
    }

    public record OverpressurePlan(
            boolean enabled,
            int connectedPipeBlocks,
            int pipeBlocksToBreak,
            int connectedReceiversToExplode) {
    }

    public record EffectPlan(String particleType, int scale, BlockPos origin, int range) {
    }

    public record SoundPlan(String sound, BlockPos origin) {
    }

    public record MeltdownSequencePlan(
            boolean dropLidsDuringColumnMelt,
            boolean restoreDropLidsAfter,
            RBMKMeltPlanner.MeltdownBounds bounds,
            List<ColumnReducePlan> columnReductions,
            List<PribrisConversionCandidate> pribrisConversions,
            OverpressurePlan overpressure,
            EffectPlan mushroomEffect,
            SoundPlan explosionSound,
            int achievementRadius,
            boolean spawnDigammaSpear,
            boolean clearTemporaryCollections) {
    }
}
