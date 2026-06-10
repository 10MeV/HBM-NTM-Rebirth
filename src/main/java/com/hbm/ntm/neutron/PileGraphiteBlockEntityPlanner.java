package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public final class PileGraphiteBlockEntityPlanner {
    public static final double FAN_COOLING_RATE = 0.025D;
    public static final String LEGACY_NODE_REMOVAL_REASON_INVALIDATE = "TileEntityPileBase.invalidate";
    public static final String LEGACY_NODE_REMOVAL_REASON_CHUNK_UNLOAD = "TileEntityPileBase.onChunkUnload";

    private PileGraphiteBlockEntityPlanner() {
    }

    public static FuelBlockEntityTickPlan planFuelTick(
            BlockPos pos,
            int meta,
            PileFuelState state,
            RandomSource random) {
        if (pos == null || state == null) {
            return FuelBlockEntityTickPlan.empty();
        }
        boolean windscale = PileGraphiteMetadata.hasAluminum(meta);
        boolean pu239Rich = PileGraphiteMetadata.isActive(meta);
        PileFuelRuntime.FuelTickResult tick =
                PileFuelRuntime.tickFuel(state, windscale, pu239Rich, random);
        PileGraphiteLifecyclePlanner.FuelLifecyclePlan lifecycle =
                PileGraphiteLifecyclePlanner.planFuelLifecycle(pos, meta, tick);
        return new FuelBlockEntityTickPlan(
                tick,
                lifecycle,
                rayRequests(pos, lifecycle.raysToCast(), lifecycle.rayFlux()),
                lifecycle.notifyRedstone()
                        ? List.of(new RedstoneUpdatePlan(pos, comparatorSignalForFuel(state, meta)))
                        : List.of(),
                diagnosticSnapshot(PileGraphiteInsertionPlanner.GraphiteBlockKind.FUEL, state, null, meta));
    }

    public static BreedingFuelBlockEntityTickPlan planBreedingFuelTick(
            BlockPos pos,
            int meta,
            PileFuelState state) {
        if (pos == null || state == null) {
            return BreedingFuelBlockEntityTickPlan.empty();
        }
        PileFuelRuntime.BreedingFuelTickResult tick = PileFuelRuntime.tickBreedingFuel(state);
        PileGraphiteLifecyclePlanner.BreedingFuelLifecyclePlan lifecycle =
                PileGraphiteLifecyclePlanner.planBreedingFuelLifecycle(pos, meta, tick);
        return new BreedingFuelBlockEntityTickPlan(
                tick,
                lifecycle,
                rayRequests(pos, lifecycle.raysToCast(), lifecycle.rayFlux()),
                diagnosticSnapshot(PileGraphiteInsertionPlanner.GraphiteBlockKind.LITHIUM, state, null, meta));
    }

    public static SourceBlockEntityTickPlan planSourceTick(
            BlockPos pos,
            PileGraphiteInsertionPlanner.GraphiteBlockKind blockKind) {
        if (pos == null || blockKind == null) {
            return SourceBlockEntityTickPlan.empty();
        }
        PileGraphiteLifecyclePlanner.SourceLifecyclePlan lifecycle =
                PileGraphiteLifecyclePlanner.planSourceLifecycle(blockKind);
        return new SourceBlockEntityTickPlan(lifecycle, rayRequests(pos, lifecycle.raysToCast(), lifecycle.rayFlux()));
    }

    public static DetectorBlockEntityTickPlan planDetectorTick(
            BlockPos pos,
            int meta,
            PileNeutronDetectorState state,
            PileGraphiteTogglePlanner.ChainStateLookup chainLookup) {
        if (pos == null || state == null) {
            return DetectorBlockEntityTickPlan.empty();
        }
        PileNeutronDetectorState.DetectorTickResult tick = state.tick(PileGraphiteMetadata.isActive(meta));
        PileGraphiteLifecyclePlanner.DetectorLifecyclePlan lifecycle =
                PileGraphiteLifecyclePlanner.planDetectorLifecycle(pos, meta, tick, chainLookup);
        return new DetectorBlockEntityTickPlan(
                tick,
                lifecycle,
                lifecycle.togglePlan().hasMutations()
                        ? List.of(new SoundPlan(PileGraphiteInteractionPlanner.LEGACY_SOUND_TECH_BLEEP, pos, 0.02F, 1.0F))
                        : List.of(),
                diagnosticSnapshot(PileGraphiteInsertionPlanner.GraphiteBlockKind.DETECTOR, null, state, meta));
    }

    public static int comparatorSignalForFuel(PileFuelState state, int meta) {
        if (state == null) {
            return 0;
        }
        int max = PileFuelRuntime.FUEL_MAX_PROGRESS - PileFuelRuntime.SOURCE_PROGRESS_MARGIN;
        if (max <= 0) {
            return 0;
        }
        int signal = (state.progress() * 15) / max;
        return Math.max(0, Math.min(15, signal));
    }

    public static FanCoolingPlan planFanCooling(BlockPos pos, PileFuelState state) {
        if (pos == null || state == null) {
            return new FanCoolingPlan(pos, 0, 0, false);
        }
        int oldHeat = state.heat();
        int newHeat = (int) (oldHeat - oldHeat * FAN_COOLING_RATE);
        return new FanCoolingPlan(pos, oldHeat, Math.max(0, newHeat), oldHeat != newHeat);
    }

    public static LifecycleNodePlan planInvalidate(BlockPos pos) {
        return new LifecycleNodePlan(pos, LEGACY_NODE_REMOVAL_REASON_INVALIDATE, true);
    }

    public static LifecycleNodePlan planChunkUnload(BlockPos pos) {
        return new LifecycleNodePlan(pos, LEGACY_NODE_REMOVAL_REASON_CHUNK_UNLOAD, true);
    }

    public static ToolUseExecutionPlan planToolUse(
            BlockPos pos,
            PileGraphiteInsertionPlanner.GraphiteBlockKind blockKind,
            int meta,
            Direction side,
            ToolAction tool,
            boolean playerSneaking,
            PileFuelState fuelState,
            PileNeutronDetectorState detectorState) {
        if (pos == null || tool == null) {
            return ToolUseExecutionPlan.empty();
        }
        return switch (tool) {
            case HAND_DRILL -> planHandDrillTool(pos, blockKind, meta, side, fuelState);
            case SCREWDRIVER -> planScrewdriverTool(pos, blockKind, meta, side, playerSneaking);
            case DEFUSER -> planDefuserTool(pos, playerSneaking, detectorState);
        };
    }

    private static ToolUseExecutionPlan planHandDrillTool(
            BlockPos pos,
            PileGraphiteInsertionPlanner.GraphiteBlockKind blockKind,
            int meta,
            Direction side,
            PileFuelState fuelState) {
        if (blockKind == null) {
            return new ToolUseExecutionPlan(
                    PileGraphiteInteractionPlanner.planHandDrillGraphite(pos, side),
                    null,
                    null);
        }
        if (blockKind == PileGraphiteInsertionPlanner.GraphiteBlockKind.FUEL) {
            return new ToolUseExecutionPlan(
                    PileGraphiteInteractionPlanner.planFuelHandDrillDiagnostic(
                            pos,
                            PileGraphiteMetadata.isActive(meta)),
                    diagnosticSnapshot(blockKind, fuelState, null, meta),
                    null);
        }
        if (blockKind == PileGraphiteInsertionPlanner.GraphiteBlockKind.LITHIUM) {
            return new ToolUseExecutionPlan(
                    PileGraphiteInteractionPlanner.planBreedingFuelHandDrillDiagnostic(pos),
                    diagnosticSnapshot(blockKind, fuelState, null, meta),
                    null);
        }
        return ToolUseExecutionPlan.empty();
    }

    private static ToolUseExecutionPlan planScrewdriverTool(
            BlockPos pos,
            PileGraphiteInsertionPlanner.GraphiteBlockKind blockKind,
            int meta,
            Direction side,
            boolean playerSneaking) {
        if (blockKind == null) {
            return ToolUseExecutionPlan.empty();
        }
        return new ToolUseExecutionPlan(
                PileGraphiteInteractionPlanner.planScrewdriver(pos, blockKind, meta, side, playerSneaking),
                null,
                null);
    }

    private static ToolUseExecutionPlan planDefuserTool(
            BlockPos pos,
            boolean playerSneaking,
            PileNeutronDetectorState detectorState) {
        if (detectorState == null) {
            return ToolUseExecutionPlan.empty();
        }
        PileGraphiteInteractionPlanner.InteractionPlan interaction =
                PileGraphiteInteractionPlanner.planDetectorDefuser(
                        pos,
                        playerSneaking,
                        detectorState.maxNeutrons());
        DetectorThresholdPlan threshold = new DetectorThresholdPlan(
                detectorState.maxNeutrons(),
                detectorState.maxNeutrons() + interaction.detectorThresholdDelta());
        return new ToolUseExecutionPlan(interaction, null, threshold);
    }

    private static List<RayCastRequest> rayRequests(BlockPos pos, int count, int flux) {
        if (pos == null || count <= 0 || flux <= 0) {
            return List.of();
        }
        List<RayCastRequest> requests = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            requests.add(new RayCastRequest(pos, flux));
        }
        return List.copyOf(requests);
    }

    private static DiagnosticSnapshot diagnosticSnapshot(
            PileGraphiteInsertionPlanner.GraphiteBlockKind blockKind,
            PileFuelState fuelState,
            PileNeutronDetectorState detectorState,
            int meta) {
        if (fuelState != null) {
            return new DiagnosticSnapshot(
                    blockKind,
                    fuelState.heat(),
                    PileFuelRuntime.FUEL_MAX_HEAT,
                    fuelState.progress(),
                    blockKind == PileGraphiteInsertionPlanner.GraphiteBlockKind.LITHIUM
                            ? PileFuelRuntime.BREEDING_FUEL_MAX_PROGRESS
                            : PileFuelRuntime.FUEL_MAX_PROGRESS,
                    fuelState.lastNeutrons(),
                    PileGraphiteMetadata.isActive(meta),
                    0);
        }
        if (detectorState != null) {
            return new DiagnosticSnapshot(
                    blockKind,
                    0,
                    0,
                    0,
                    0,
                    detectorState.lastNeutrons(),
                    PileGraphiteMetadata.isActive(meta),
                    detectorState.maxNeutrons());
        }
        return null;
    }

    public enum ToolAction {
        HAND_DRILL,
        SCREWDRIVER,
        DEFUSER
    }

    public record FuelBlockEntityTickPlan(
            PileFuelRuntime.FuelTickResult tickResult,
            PileGraphiteLifecyclePlanner.FuelLifecyclePlan lifecycle,
            List<RayCastRequest> rayCasts,
            List<RedstoneUpdatePlan> redstoneUpdates,
            DiagnosticSnapshot diagnosticSnapshot) {
        private static FuelBlockEntityTickPlan empty() {
            return new FuelBlockEntityTickPlan(null, null, List.of(), List.of(), null);
        }

        public FuelBlockEntityTickPlan {
            rayCasts = List.copyOf(rayCasts);
            redstoneUpdates = List.copyOf(redstoneUpdates);
        }
    }

    public record BreedingFuelBlockEntityTickPlan(
            PileFuelRuntime.BreedingFuelTickResult tickResult,
            PileGraphiteLifecyclePlanner.BreedingFuelLifecyclePlan lifecycle,
            List<RayCastRequest> rayCasts,
            DiagnosticSnapshot diagnosticSnapshot) {
        private static BreedingFuelBlockEntityTickPlan empty() {
            return new BreedingFuelBlockEntityTickPlan(null, null, List.of(), null);
        }

        public BreedingFuelBlockEntityTickPlan {
            rayCasts = List.copyOf(rayCasts);
        }
    }

    public record SourceBlockEntityTickPlan(
            PileGraphiteLifecyclePlanner.SourceLifecyclePlan lifecycle,
            List<RayCastRequest> rayCasts) {
        private static SourceBlockEntityTickPlan empty() {
            return new SourceBlockEntityTickPlan(null, List.of());
        }

        public SourceBlockEntityTickPlan {
            rayCasts = List.copyOf(rayCasts);
        }
    }

    public record DetectorBlockEntityTickPlan(
            PileNeutronDetectorState.DetectorTickResult tickResult,
            PileGraphiteLifecyclePlanner.DetectorLifecyclePlan lifecycle,
            List<SoundPlan> sounds,
            DiagnosticSnapshot diagnosticSnapshot) {
        private static DetectorBlockEntityTickPlan empty() {
            return new DetectorBlockEntityTickPlan(null, null, List.of(), null);
        }

        public DetectorBlockEntityTickPlan {
            sounds = List.copyOf(sounds);
        }
    }

    public record RayCastRequest(BlockPos origin, int flux) {
    }

    public record RedstoneUpdatePlan(BlockPos pos, int comparatorSignal) {
    }

    public record SoundPlan(String legacySoundId, BlockPos pos, float volume, float pitch) {
    }

    public record FanCoolingPlan(BlockPos pos, int oldHeat, int newHeat, boolean changed) {
    }

    public record LifecycleNodePlan(BlockPos pos, String legacyReason, boolean removeNeutronNode) {
    }

    public record DetectorThresholdPlan(int oldThreshold, int newThreshold) {
    }

    public record ToolUseExecutionPlan(
            PileGraphiteInteractionPlanner.InteractionPlan interaction,
            DiagnosticSnapshot diagnosticSnapshot,
            DetectorThresholdPlan detectorThresholdPlan) {
        private static ToolUseExecutionPlan empty() {
            return new ToolUseExecutionPlan(null, null, null);
        }
    }

    public record DiagnosticSnapshot(
            PileGraphiteInsertionPlanner.GraphiteBlockKind blockKind,
            int heat,
            int maxHeat,
            int progress,
            int maxProgress,
            int lastNeutrons,
            boolean metadataBit8Set,
            int detectorMaxNeutrons) {
    }
}
