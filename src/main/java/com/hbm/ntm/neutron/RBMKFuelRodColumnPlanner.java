package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class RBMKFuelRodColumnPlanner {
    public static final double BREAK_MELTDOWN_HULL_HEAT = 1500.0D;
    public static final String HAND_LOAD_SOUND = "hbm:item.upgradePlug";
    public static final String ROR_PREFIX_VALUE = "VAL:";

    private RBMKFuelRodColumnPlanner() {
    }

    public static FuelRodPacketLayout packetLayout(boolean hasRod) {
        List<PacketField> fields = new ArrayList<>();
        fields.add(new PacketField("lastFluxQuantity", PacketFieldType.DOUBLE, PacketFieldCondition.ALWAYS));
        fields.add(new PacketField("lastFluxRatio", PacketFieldType.DOUBLE, PacketFieldCondition.ALWAYS));
        fields.add(new PacketField("hasRod", PacketFieldType.BOOLEAN, PacketFieldCondition.ALWAYS));
        fields.add(new PacketField("rodColor", PacketFieldType.INT, PacketFieldCondition.ALWAYS));
        fields.add(new PacketField("fuelYield", PacketFieldType.STRING, PacketFieldCondition.IF_HAS_ROD));
        fields.add(new PacketField("fuelXenon", PacketFieldType.STRING, PacketFieldCondition.IF_HAS_ROD));
        fields.add(new PacketField("fuelHeat", PacketFieldType.STRING, PacketFieldCondition.IF_HAS_ROD));
        return new FuelRodPacketLayout(hasRod, List.copyOf(fields));
    }

    public static FuelRodNbtLayout nbtLayout(boolean diagnosticMode) {
        if (diagnosticMode) {
            return new FuelRodNbtLayout(true, new String[] {"fluxSlow", "fluxFast", "hasRod"},
                    "fluxSlow/fluxFast are derived from current buffered fluxQuantity and fluxFastRatio");
        }
        return new FuelRodNbtLayout(false, new String[] {"fluxQuantity", "fluxMod", "hasRod"},
                "normal save writes lastFluxQuantity and lastFluxRatio");
    }

    public static FluxNbtReadPlan planFluxNbtRead(boolean hasLegacySplitFlux, double fluxFast, double fluxSlow,
            double fluxQuantity, double fluxMod, boolean hasRod) {
        if (hasLegacySplitFlux) {
            double quantity = fluxFast + fluxSlow;
            double ratio = quantity > 0.0D ? fluxFast / quantity : 0.0D;
            return new FluxNbtReadPlan(quantity, ratio, hasRod, true);
        }
        return new FluxNbtReadPlan(fluxQuantity, fluxMod, hasRod, false);
    }

    public static FuelRodPacketSnapshot packetSnapshot(RBMKRodFluxState fluxState, RBMKFuelRodSpec spec,
            RBMKFuelRodState state) {
        boolean hasRod = fluxState != null && fluxState.hasRod() && spec != null && state != null;
        return new FuelRodPacketSnapshot(
                fluxState == null ? 0.0D : fluxState.lastFluxQuantity(),
                fluxState == null ? 0.0D : fluxState.lastFluxRatio(),
                hasRod,
                fluxState == null ? 0 : fluxState.rodColor(),
                hasRod ? fuelDiagnostics(spec, state) : FuelDiagnostics.EMPTY);
    }

    public static DiagnosticNbtPlan diagnosticNbtPlan(RBMKRodFluxState fluxState, FuelDiagnostics diagnostics) {
        FuelDiagnostics safeDiagnostics = diagnostics == null ? FuelDiagnostics.EMPTY : diagnostics;
        boolean hasFuelText = !safeDiagnostics.fuelYield().isEmpty()
                && !safeDiagnostics.fuelXenon().isEmpty()
                && !safeDiagnostics.fuelHeat().isEmpty();
        return new DiagnosticNbtPlan(
                fluxState == null ? 0.0D : fluxState.fluxQuantity() * (1.0D - fluxState.fluxFastRatio()),
                fluxState == null ? 0.0D : fluxState.fluxQuantity() * fluxState.fluxFastRatio(),
                fluxState != null && fluxState.hasRod(),
                hasFuelText,
                "f_yield",
                "f_xenon",
                "f_heat",
                safeDiagnostics);
    }

    public static EnergyControlInfo energyControlInfo(RBMKFuelRodSpec spec, RBMKFuelRodState state) {
        if (spec == null || state == null) {
            return EnergyControlInfo.empty();
        }
        return new EnergyControlInfo(
                true,
                (1.0D - state.enrichment(spec)) * 100.0D,
                state.xenon(),
                state.hullHeat(),
                state.coreHeat(),
                spec.meltingPoint());
    }

    public static String[] redstoneRadioFunctionInfo() {
        return new String[] {
                ROR_PREFIX_VALUE + "columnheat",
                ROR_PREFIX_VALUE + "rodheat",
                ROR_PREFIX_VALUE + "depletion",
                ROR_PREFIX_VALUE + "xenon",
                ROR_PREFIX_VALUE + "fastflux",
                ROR_PREFIX_VALUE + "slowflux",
                ROR_PREFIX_VALUE + "flux"
        };
    }

    public static RedstoneRadioValuePlan redstoneRadioValue(String name, RBMKThermalState thermalState,
            RBMKRodFluxState fluxState, RBMKFuelRodSpec spec, RBMKFuelRodState state) {
        RedstoneRadioValues values = redstoneRadioValues(thermalState, fluxState, spec, state);
        String safeName = name == null ? "" : name;
        if ((ROR_PREFIX_VALUE + "columnheat").equals(safeName)) {
            return RedstoneRadioValuePlan.value(values.columnHeat());
        }
        if ((ROR_PREFIX_VALUE + "rodheat").equals(safeName)) {
            return spec == null || state == null
                    ? RedstoneRadioValuePlan.missing()
                    : RedstoneRadioValuePlan.value(values.rodHullHeat());
        }
        if ((ROR_PREFIX_VALUE + "depletion").equals(safeName)) {
            return spec == null || state == null
                    ? RedstoneRadioValuePlan.missing()
                    : RedstoneRadioValuePlan.value(values.depletion());
        }
        if ((ROR_PREFIX_VALUE + "xenon").equals(safeName)) {
            return spec == null || state == null
                    ? RedstoneRadioValuePlan.missing()
                    : RedstoneRadioValuePlan.value(values.xenon());
        }
        if ((ROR_PREFIX_VALUE + "fastflux").equals(safeName)) {
            return RedstoneRadioValuePlan.value(values.fastFlux());
        }
        if ((ROR_PREFIX_VALUE + "slowflux").equals(safeName)) {
            return RedstoneRadioValuePlan.value(values.slowFlux());
        }
        if ((ROR_PREFIX_VALUE + "flux").equals(safeName)) {
            return RedstoneRadioValuePlan.value(values.totalFlux());
        }
        return RedstoneRadioValuePlan.missing();
    }

    public static LoadPlan planHandLoad(String legacyRodId, boolean slotEmpty, boolean creativeMode) {
        Optional<RBMKFuelRodRegistry.Entry> entry = RBMKFuelRodRegistry.find(legacyRodId);
        if (entry.isEmpty()) {
            return LoadPlan.reject(LoadFailure.UNKNOWN_ROD);
        }
        if (!slotEmpty) {
            return LoadPlan.reject(LoadFailure.SLOT_OCCUPIED);
        }
        return new LoadPlan(
                true,
                null,
                entry.get().legacyRodId(),
                RBMKFuelRodState.fresh(entry.get().spec()),
                !creativeMode,
                HAND_LOAD_SOUND);
    }

    public static LoadPlan planAutoloaderLoad(String legacyRodId, boolean slotEmpty) {
        Optional<RBMKFuelRodRegistry.Entry> entry = RBMKFuelRodRegistry.find(legacyRodId);
        if (entry.isEmpty()) {
            return LoadPlan.reject(LoadFailure.UNKNOWN_ROD);
        }
        if (!slotEmpty) {
            return LoadPlan.reject(LoadFailure.SLOT_OCCUPIED);
        }
        return new LoadPlan(true, null, entry.get().legacyRodId(), RBMKFuelRodState.fresh(entry.get().spec()), false, "");
    }

    public static UnloadPlan planManualUnload(String legacyRodId, RBMKFuelRodState fuelState) {
        if (!hasKnownRod(legacyRodId, fuelState)) {
            return UnloadPlan.reject(UnloadFailure.EMPTY);
        }
        if (!RBMKFuelColumnRuntime.coldEnoughForManual(fuelState)) {
            return UnloadPlan.reject(UnloadFailure.TOO_HOT_FOR_MANUAL);
        }
        return new UnloadPlan(true, null, legacyRodId, fuelState);
    }

    public static UnloadPlan planAutoloaderUnload(String legacyRodId, RBMKFuelRodState fuelState) {
        if (!hasKnownRod(legacyRodId, fuelState)) {
            return UnloadPlan.reject(UnloadFailure.EMPTY);
        }
        if (!RBMKFuelColumnRuntime.coldEnoughForAutoloader(fuelState)) {
            return UnloadPlan.reject(UnloadFailure.TOO_HOT_FOR_AUTOLOADER);
        }
        return new UnloadPlan(true, null, legacyRodId, fuelState);
    }

    public static MeltdownTriggerPlan planInvalidateMeltdown(String legacyRodId, RBMKFuelRodState fuelState,
            boolean meltdownsDisabled) {
        return planMeltdownTrigger(legacyRodId, fuelState, meltdownsDisabled, true);
    }

    public static MeltdownTriggerPlan planBreakMeltdown(String legacyRodId, RBMKFuelRodState fuelState,
            boolean meltdownsDisabled, boolean explodeOnBroken) {
        return planMeltdownTrigger(legacyRodId, fuelState, meltdownsDisabled, explodeOnBroken);
    }

    public static ColumnTickPlan planFuelRodTick(
            RBMKRuntimeSettings settings,
            RBMKThermalState thermalState,
            RBMKRodFluxState fluxState,
            String legacyRodId,
            RBMKFuelRodState fuelState,
            boolean hasLid,
            double columnMaxHeat) {
        Optional<RBMKFuelRodRegistry.Entry> entry = RBMKFuelRodRegistry.find(legacyRodId);
        if (entry.isEmpty() || fuelState == null) {
            RBMKFuelColumnRuntime.tickEmptyRodSlot(fluxState);
            return ColumnTickPlan.empty();
        }

        RBMKFuelRodSpec spec = entry.get().spec();
        RBMKFuelColumnRuntime.FuelColumnTickResult tick = RBMKFuelColumnRuntime.tickFuelRod(
                settings,
                thermalState,
                fluxState,
                spec,
                fuelState,
                hasLid,
                columnMaxHeat);
        return new ColumnTickPlan(
                true,
                entry.get().legacyRodId(),
                tick.outgoingFluxQuantity(),
                tick.outgoingFluxRatio(),
                tick.leakRadiation(),
                tick.shouldSpreadFlux(),
                tick.overheated(),
                tick.overheated() && tick.meltdownSuppressed(),
                tick.overheated() && !tick.meltdownSuppressed(),
                spec.colorTint(),
                fuelDiagnostics(spec, fuelState));
    }

    public static ColumnTickPlan planEmptyTick(RBMKRodFluxState fluxState) {
        RBMKFuelColumnRuntime.tickEmptyRodSlot(fluxState);
        return ColumnTickPlan.empty();
    }

    public static FuelDiagnostics fuelDiagnostics(RBMKFuelRodSpec spec, RBMKFuelRodState state) {
        if (spec == null || state == null) {
            return FuelDiagnostics.EMPTY;
        }
        return new FuelDiagnostics(
                state.remainingYield() + " / " + spec.totalYield() + " (" + (state.enrichment(spec) * 100.0D) + "%)",
                state.xenon() + "%",
                String.format(Locale.ROOT, "%.6f / %.6f / %.2f",
                        state.coreHeat(),
                        state.hullHeat(),
                        spec.meltingPoint()));
    }

    public static ConsoleDiagnostics consoleDiagnostics(RBMKFuelRodSpec spec, RBMKFuelRodState state) {
        if (spec == null || state == null) {
            return ConsoleDiagnostics.EMPTY;
        }
        return new ConsoleDiagnostics(
                state.enrichment(spec),
                state.xenon(),
                state.hullHeat(),
                state.coreHeat(),
                spec.meltingPoint());
    }

    public static RedstoneRadioValues redstoneRadioValues(
            RBMKThermalState thermalState,
            RBMKRodFluxState fluxState,
            RBMKFuelRodSpec spec,
            RBMKFuelRodState state) {
        double lastFluxQuantity = fluxState == null ? 0.0D : fluxState.lastFluxQuantity();
        double lastFluxRatio = fluxState == null ? 0.0D : fluxState.lastFluxRatio();
        int fastFlux = (int) (lastFluxQuantity * lastFluxRatio);
        int slowFlux = (int) (lastFluxQuantity * (1.0D - lastFluxRatio));
        return new RedstoneRadioValues(
                thermalState == null ? 0 : (int) thermalState.heat(),
                state == null ? 0 : (int) state.hullHeat(),
                spec == null || state == null ? 0 : (int) (100.0D - state.enrichment(spec) * 100.0D),
                state == null ? 0 : (int) state.xenon(),
                fastFlux,
                slowFlux,
                fastFlux + slowFlux);
    }

    public static RodMeltdownPlan planRodMeltdown(
            BlockPos origin,
            String legacyRodId,
            int columnHeight,
            int reduce,
            boolean randomExtraReduce,
            boolean moderated,
            boolean normalLid) {
        int height = Math.max(1, columnHeight);
        int effectiveReduce = Mth.clamp(reduce, 1, height) + (randomExtraReduce ? 1 : 0);
        boolean hasFuelRod = RBMKFuelRodRegistry.find(legacyRodId).isPresent();

        List<CoriumLayerMutation> coriumLayers = new ArrayList<>();
        RBMKMeltPlanner.StandardMeltPlan standardMelt = null;
        DebrisRange fuelDebris = DebrisRange.none(DebrisType.FUEL);
        if (hasFuelRod) {
            for (int y = height; y >= 0; y--) {
                coriumLayers.add(new CoriumLayerMutation(origin.above(y), 5));
            }
            fuelDebris = new DebrisRange(DebrisType.FUEL, 1, height);
        } else {
            standardMelt = RBMKMeltPlanner.standardMelt(origin, height, reduce, randomExtraReduce);
        }

        List<DebrisRange> debris = new ArrayList<>();
        if (fuelDebris.maxCount() > 0) {
            debris.add(fuelDebris);
        }
        if (moderated) {
            debris.add(new DebrisRange(DebrisType.GRAPHITE, 2, 3));
        }
        debris.add(new DebrisRange(DebrisType.ELEMENT, 1, 1));
        if (normalLid) {
            debris.add(new DebrisRange(DebrisType.LID, 1, 1));
        }

        return new RodMeltdownPlan(
                hasFuelRod,
                "rbmk_fuel_drx".equals(legacyRodId),
                true,
                effectiveReduce,
                coriumLayers,
                standardMelt,
                debris);
    }

    private static MeltdownTriggerPlan planMeltdownTrigger(String legacyRodId, RBMKFuelRodState fuelState,
            boolean meltdownsDisabled, boolean triggerAllowed) {
        if (!triggerAllowed) {
            return MeltdownTriggerPlan.reject(MeltdownTriggerFailure.TRIGGER_NOT_ALLOWED);
        }
        if (meltdownsDisabled) {
            return MeltdownTriggerPlan.reject(MeltdownTriggerFailure.MELTDOWNS_DISABLED);
        }
        if (!hasKnownRod(legacyRodId, fuelState)) {
            return MeltdownTriggerPlan.reject(MeltdownTriggerFailure.NO_FUEL_ROD);
        }
        if (fuelState.hullHeat() < BREAK_MELTDOWN_HULL_HEAT) {
            return MeltdownTriggerPlan.reject(MeltdownTriggerFailure.HULL_BELOW_LIMIT);
        }
        return new MeltdownTriggerPlan(true, null);
    }

    private static boolean hasKnownRod(String legacyRodId, RBMKFuelRodState fuelState) {
        return fuelState != null && RBMKFuelRodRegistry.find(legacyRodId).isPresent();
    }

    public enum PacketFieldType {
        DOUBLE,
        BOOLEAN,
        INT,
        STRING
    }

    public enum PacketFieldCondition {
        ALWAYS,
        IF_HAS_ROD
    }

    public record PacketField(String name, PacketFieldType type, PacketFieldCondition condition) {
    }

    public record FuelRodPacketLayout(boolean hasRod, List<PacketField> fields) {
    }

    public record FuelRodNbtLayout(boolean diagnosticMode, String[] keys, String note) {
    }

    public record FluxNbtReadPlan(
            double fluxQuantity,
            double fluxFastRatio,
            boolean hasRod,
            boolean readLegacySplitFlux) {
    }

    public record FuelRodPacketSnapshot(
            double lastFluxQuantity,
            double lastFluxRatio,
            boolean hasRod,
            int rodColor,
            FuelDiagnostics diagnostics) {
    }

    public record DiagnosticNbtPlan(
            double fluxSlow,
            double fluxFast,
            boolean hasRod,
            boolean writeFuelText,
            String fuelYieldKey,
            String fuelXenonKey,
            String fuelHeatKey,
            FuelDiagnostics diagnostics) {
    }

    public record EnergyControlInfo(
            boolean present,
            double depletionPercent,
            double xenonPercent,
            double skinC,
            double coreC,
            double meltC) {
        public static EnergyControlInfo empty() {
            return new EnergyControlInfo(false, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    public record RedstoneRadioValuePlan(boolean present, String value) {
        public static RedstoneRadioValuePlan value(int value) {
            return new RedstoneRadioValuePlan(true, Integer.toString(value));
        }

        public static RedstoneRadioValuePlan missing() {
            return new RedstoneRadioValuePlan(false, "");
        }
    }

    public enum LoadFailure {
        UNKNOWN_ROD,
        SLOT_OCCUPIED
    }

    public record LoadPlan(
            boolean accepted,
            LoadFailure failure,
            String legacyRodId,
            RBMKFuelRodState loadedState,
            boolean consumeHeldItem,
            String sound) {
        private static LoadPlan reject(LoadFailure failure) {
            return new LoadPlan(false, failure, "", null, false, "");
        }
    }

    public enum UnloadFailure {
        EMPTY,
        TOO_HOT_FOR_MANUAL,
        TOO_HOT_FOR_AUTOLOADER
    }

    public record UnloadPlan(boolean accepted, UnloadFailure failure, String legacyRodId, RBMKFuelRodState providedState) {
        private static UnloadPlan reject(UnloadFailure failure) {
            return new UnloadPlan(false, failure, "", null);
        }
    }

    public enum MeltdownTriggerFailure {
        TRIGGER_NOT_ALLOWED,
        MELTDOWNS_DISABLED,
        NO_FUEL_ROD,
        HULL_BELOW_LIMIT
    }

    public record MeltdownTriggerPlan(boolean shouldMelt, MeltdownTriggerFailure failure) {
        private static MeltdownTriggerPlan reject(MeltdownTriggerFailure failure) {
            return new MeltdownTriggerPlan(false, failure);
        }
    }

    public record ColumnTickPlan(
            boolean hasRod,
            String legacyRodId,
            double outgoingFluxQuantity,
            double outgoingFluxRatio,
            double leakRadiation,
            boolean spreadFlux,
            boolean overheated,
            boolean spawnSuppressedMeltdownFlame,
            boolean meltdown,
            int rodColor,
            FuelDiagnostics fuelDiagnostics) {
        private static ColumnTickPlan empty() {
            return new ColumnTickPlan(false, "", 0.0D, 0.0D, 0.0D, false, false, false, false, 0,
                    FuelDiagnostics.EMPTY);
        }
    }

    public record FuelDiagnostics(String fuelYield, String fuelXenon, String fuelHeat) {
        public static final FuelDiagnostics EMPTY = new FuelDiagnostics("", "", "");
    }

    public record ConsoleDiagnostics(
            double enrichment,
            double xenon,
            double hullHeat,
            double coreHeat,
            double meltingPoint) {
        public static final ConsoleDiagnostics EMPTY = new ConsoleDiagnostics(0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    public record RedstoneRadioValues(
            int columnHeat,
            int rodHullHeat,
            int depletion,
            int xenon,
            int fastFlux,
            int slowFlux,
            int totalFlux) {
    }

    public enum DebrisType {
        FUEL,
        GRAPHITE,
        ELEMENT,
        LID
    }

    public record DebrisRange(DebrisType type, int minCount, int maxCount) {
        private static DebrisRange none(DebrisType type) {
            return new DebrisRange(type, 0, 0);
        }
    }

    public record CoriumLayerMutation(BlockPos pos, int legacyMeta) {
    }

    public record RodMeltdownPlan(
            boolean hadFuelRod,
            boolean setDigamma,
            boolean clearFuelSlot,
            int effectiveReduce,
            List<CoriumLayerMutation> coriumLayers,
            RBMKMeltPlanner.StandardMeltPlan standardMelt,
            List<DebrisRange> debris) {
    }
}
