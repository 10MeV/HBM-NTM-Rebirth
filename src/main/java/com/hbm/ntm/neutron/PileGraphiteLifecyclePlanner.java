package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class PileGraphiteLifecyclePlanner {
    public static final String LEGACY_BLOCK_DENSE_RADON = "gas_radon_dense";
    public static final int LEGACY_FUEL_EXPLOSION_RADIUS = 4;
    public static final String LEGACY_SMOKE_PARTICLE = "AuxParticlePacketNT(vanillaExt,smoke)";

    private PileGraphiteLifecyclePlanner() {
    }

    public static DropPlan planDrops(PileGraphiteInsertionPlanner.GraphiteBlockKind blockKind, int meta) {
        if (blockKind == null) {
            return new DropPlan(List.of());
        }

        List<PileGraphiteInteractionPlanner.LegacyItemStack> drops = new ArrayList<>();
        drops.add(new PileGraphiteInteractionPlanner.LegacyItemStack("ingot_graphite", 8, ""));
        if (PileGraphiteMetadata.hasAluminum(meta)) {
            drops.add(PileGraphiteInteractionPlanner.LegacyItemStack.aluminumShell());
        }

        PileGraphiteInsertionPlanner.InsertedItem insertedItem = blockKind.insertedItem(meta);
        if (insertedItem != PileGraphiteInsertionPlanner.InsertedItem.NONE) {
            drops.add(PileGraphiteInteractionPlanner.LegacyItemStack.insertedItem(insertedItem));
        }
        return new DropPlan(drops);
    }

    public static FuelLifecyclePlan planFuelLifecycle(
            BlockPos pos,
            int oldMeta,
            PileFuelRuntime.FuelTickResult tickResult) {
        if (pos == null || tickResult == null) {
            return FuelLifecyclePlan.empty();
        }

        List<PileGraphiteInteractionPlanner.BlockMutation> mutations = new ArrayList<>();
        if (tickResult.markSource()) {
            mutations.add(new PileGraphiteInteractionPlanner.BlockMutation(
                    pos,
                    PileGraphiteInsertionPlanner.GraphiteBlockKind.FUEL.legacyBlockId(),
                    oldMeta | PileGraphiteMetadata.ACTIVE_MASK));
        }
        if (tickResult.explode()) {
            mutations.add(new PileGraphiteInteractionPlanner.BlockMutation(pos, LEGACY_BLOCK_DENSE_RADON, 0));
        }
        if (tickResult.transmuteToProduct()) {
            mutations.add(new PileGraphiteInteractionPlanner.BlockMutation(
                    pos,
                    PileGraphiteInsertionPlanner.GraphiteBlockKind.PLUTONIUM.legacyBlockId(),
                    oldMeta & ~PileGraphiteMetadata.ACTIVE_MASK));
        }

        return new FuelLifecyclePlan(
                mutations,
                tickResult.explode() ? new ExplosionPlan(pos, LEGACY_FUEL_EXPLOSION_RADIUS, true, true) : null,
                tickResult.smoke() ? new ParticlePlan(LEGACY_SMOKE_PARTICLE, pos) : null,
                tickResult.raysToCast(),
                tickResult.rayFlux(),
                tickResult.redstoneChanged());
    }

    public static BreedingFuelLifecyclePlan planBreedingFuelLifecycle(
            BlockPos pos,
            int oldMeta,
            PileFuelRuntime.BreedingFuelTickResult tickResult) {
        if (pos == null || tickResult == null) {
            return BreedingFuelLifecyclePlan.empty();
        }

        List<PileGraphiteInteractionPlanner.BlockMutation> mutations = tickResult.transmuteToProduct()
                ? List.of(new PileGraphiteInteractionPlanner.BlockMutation(
                pos,
                PileGraphiteInsertionPlanner.GraphiteBlockKind.TRITIUM.legacyBlockId(),
                oldMeta))
                : List.of();
        return new BreedingFuelLifecyclePlan(mutations, tickResult.raysToCast(), tickResult.rayFlux());
    }

    public static SourceLifecyclePlan planSourceLifecycle(PileGraphiteInsertionPlanner.GraphiteBlockKind blockKind) {
        int rayFlux = blockKind == PileGraphiteInsertionPlanner.GraphiteBlockKind.PLUTONIUM ? 2 : 1;
        return new SourceLifecyclePlan(12, rayFlux);
    }

    public static DetectorLifecyclePlan planDetectorLifecycle(
            BlockPos pos,
            int oldMeta,
            PileNeutronDetectorState.DetectorTickResult tickResult,
            PileGraphiteTogglePlanner.ChainStateLookup chainLookup) {
        if (pos == null || tickResult == null || !tickResult.triggerRods()) {
            return new DetectorLifecyclePlan(PileGraphiteTogglePlanner.TogglePlan.empty());
        }
        return new DetectorLifecyclePlan(PileGraphiteTogglePlanner.detectorTriggeredRodToggle(pos, oldMeta, chainLookup));
    }

    public record DropPlan(List<PileGraphiteInteractionPlanner.LegacyItemStack> drops) {
        public DropPlan {
            drops = List.copyOf(drops);
        }
    }

    public record ExplosionPlan(BlockPos pos, int radius, boolean flaming, boolean smoking) {
    }

    public record ParticlePlan(String legacyParticleId, BlockPos pos) {
    }

    public record FuelLifecyclePlan(
            List<PileGraphiteInteractionPlanner.BlockMutation> blockMutations,
            ExplosionPlan explosion,
            ParticlePlan smokeParticle,
            int raysToCast,
            int rayFlux,
            boolean notifyRedstone) {
        private static FuelLifecyclePlan empty() {
            return new FuelLifecyclePlan(List.of(), null, null, 0, 0, false);
        }

        public FuelLifecyclePlan {
            blockMutations = List.copyOf(blockMutations);
        }
    }

    public record BreedingFuelLifecyclePlan(
            List<PileGraphiteInteractionPlanner.BlockMutation> blockMutations,
            int raysToCast,
            int rayFlux) {
        private static BreedingFuelLifecyclePlan empty() {
            return new BreedingFuelLifecyclePlan(List.of(), 0, 0);
        }

        public BreedingFuelLifecyclePlan {
            blockMutations = List.copyOf(blockMutations);
        }
    }

    public record SourceLifecyclePlan(int raysToCast, int rayFlux) {
    }

    public record DetectorLifecyclePlan(PileGraphiteTogglePlanner.TogglePlan togglePlan) {
    }
}
