package com.hbm.ntm.neutron;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class RBMKFuelRodRegistry {
    public static final double COLD_CRAFTING_HEAT_LIMIT = 50.0D;
    public static final double FUEL_POOL_RETURN_YIELD_RATIO = 0.2D;
    public static final int DISASSEMBLY_PELLET_COUNT = 8;

    private static final int TINT_URANIUM = 0x868D82;
    private static final int TINT_THORIUM = 0x665448;
    private static final int TINT_PLUTONIUM = 0x656E6B;
    private static final int TINT_AMERICIUM = 0xA88A8F;
    private static final int TINT_NEPTUNIUM = 0x757E73;
    private static final int TINT_SCHRABIDIUM = 0x2D9A94;
    private static final int TINT_AUSTRALIUM = 0xFFEE00;
    private static final int TINT_POLONIUM = 0x563A26;
    private static final int TINT_RADIUM = 0xB3B6AD;
    private static final int TINT_FLASHGOLD = 0xDC9613;
    private static final int TINT_FLASHLEAD = 0x7B7B87;
    private static final int TINT_BALEFIRE = 0xB2FF1B;
    private static final int TINT_ZIRCONIUM = 0xAAA36A;
    private static final int TINT_DRX = 0xD77276;

    private static final Map<String, Entry> BY_LEGACY_ID = buildEntries();
    private static final List<Entry> ALL = List.copyOf(BY_LEGACY_ID.values());
    private static final List<Entry> CRAFTABLE = ALL.stream().filter(Entry::craftable).toList();

    private RBMKFuelRodRegistry() {
    }

    public static List<Entry> all() {
        return ALL;
    }

    public static List<Entry> craftable() {
        return CRAFTABLE;
    }

    public static Optional<Entry> find(String legacyRodId) {
        return Optional.ofNullable(BY_LEGACY_ID.get(legacyRodId));
    }

    public static Optional<RBMKFuelRodSpec> spec(String legacyRodId) {
        return find(legacyRodId).map(Entry::spec);
    }

    public static RBMKFuelRodState freshState(String legacyRodId) {
        return find(legacyRodId)
                .map(entry -> RBMKFuelRodState.fresh(entry.spec()))
                .orElseGet(RBMKFuelRodState::new);
    }

    public static int pelletDepletionMeta(RBMKFuelRodSpec spec, RBMKFuelRodState state) {
        if (spec == null || state == null || spec.totalYield() <= 0.0D) {
            return 4;
        }
        return 4 - clamp((int) Math.ceil(state.enrichment(spec) * 5.0D - 1.0D), 0, 4);
    }

    public static int pelletMeta(RBMKFuelRodSpec spec, RBMKFuelRodState state, boolean highXenon) {
        return pelletDepletionMeta(spec, state) + (highXenon ? 5 : 0);
    }

    public static RodDisassemblyPlan planDisassembly(String legacyRodId, RBMKFuelRodState state) {
        Optional<Entry> optional = find(legacyRodId);
        if (optional.isEmpty() || state == null) {
            return RodDisassemblyPlan.reject(DisassemblyFailure.UNKNOWN_ROD);
        }

        Entry entry = optional.get();
        if (!entry.craftable() || entry.legacyPelletId().isEmpty()) {
            return RodDisassemblyPlan.reject(DisassemblyFailure.NOT_CRAFTABLE);
        }
        if (!isColdForCrafting(state)) {
            return RodDisassemblyPlan.reject(DisassemblyFailure.TOO_HOT);
        }
        if (state.enrichment(entry.spec()) > 0.99D) {
            return RodDisassemblyPlan.reject(DisassemblyFailure.NOT_DEPLETED);
        }

        boolean highXenon = entry.pelletXenonOverlay() && state.xenonLevel() >= 0.5D;
        return new RodDisassemblyPlan(
                true,
                null,
                entry.legacyPelletId(),
                DISASSEMBLY_PELLET_COUNT,
                pelletMeta(entry.spec(), state, highXenon));
    }

    public static CoolingPoolPlan planCoolingPoolOutput(String legacyRodId) {
        Optional<Entry> optional = find(legacyRodId);
        if (optional.isEmpty() || !optional.get().craftable()) {
            return CoolingPoolPlan.reject();
        }
        Entry entry = optional.get();
        RBMKFuelRodState state = RBMKFuelRodState.fresh(entry.spec());
        state.setRemainingYield(entry.spec().totalYield() * FUEL_POOL_RETURN_YIELD_RATIO);
        return new CoolingPoolPlan(true, entry.legacyRodId(), state);
    }

    public static boolean isColdForCrafting(RBMKFuelRodState state) {
        return state != null
                && state.hullHeat() < COLD_CRAFTING_HEAT_LIMIT
                && state.coreHeat() < COLD_CRAFTING_HEAT_LIMIT;
    }

    private static Map<String, Entry> buildEntries() {
        LinkedHashMap<String, Entry> entries = new LinkedHashMap<>();

        add(entries, "rbmk_fuel_ueu", "rbmk_pellet_ueu", "Unenriched Uranium", true, true,
                spec(100_000_000D).stats(15D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.LOG_TEN)
                        .depletionFunction(RBMKFuelRodRuntime.DepletionFunction.RAISING_SLOPE)
                        .heat(0.65D, 0.02D, 2865D)
                        .colorTint(TINT_URANIUM));
        add(entries, "rbmk_fuel_meu", "rbmk_pellet_meu", "Medium Enriched Uranium-235", true, true,
                spec(100_000_000D).stats(20D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.LOG_TEN)
                        .depletionFunction(RBMKFuelRodRuntime.DepletionFunction.RAISING_SLOPE)
                        .heat(0.65D, 0.02D, 2865D)
                        .colorTint(TINT_URANIUM));
        add(entries, "rbmk_fuel_heu233", "rbmk_pellet_heu233", "Highly Enriched Uranium-233", true, true,
                spec(100_000_000D).stats(27.5D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.LINEAR)
                        .heat(1.25D, 0.02D, 2865D)
                        .colorTint(TINT_URANIUM));
        add(entries, "rbmk_fuel_heu235", "rbmk_pellet_heu235", "Highly Enriched Uranium-235", true, true,
                spec(100_000_000D).stats(50D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.SQUARE_ROOT)
                        .heat(1.0D, 0.02D, 2865D)
                        .colorTint(TINT_URANIUM));
        add(entries, "rbmk_fuel_uzh", "rbmk_pellet_uzh", "Uranium Zirconium Hydride", true, true,
                spec(50_000_000D).stats(30D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.LOG_TEN)
                        .depletionFunction(RBMKFuelRodRuntime.DepletionFunction.GENTLE_SLOPE)
                        .heat(0.75D, 0.1D, 1845D)
                        .heatCoefficient(1000D, 500D)
                        .colorTint(0x7077AF));
        add(entries, "rbmk_fuel_thmeu", "rbmk_pellet_thmeu", "Thorium with MEU Driver Fuel", true, true,
                spec(100_000_000D).stats(20D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.PLATEU)
                        .depletionFunction(RBMKFuelRodRuntime.DepletionFunction.BOOSTED_SLOPE)
                        .heat(0.65D, 0.02D, 3350D)
                        .colorTint(TINT_THORIUM));
        add(entries, "rbmk_fuel_lep", "rbmk_pellet_lep", "Low Enriched Plutonium-239", true, true,
                spec(100_000_000D).stats(35D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.LOG_TEN)
                        .depletionFunction(RBMKFuelRodRuntime.DepletionFunction.RAISING_SLOPE)
                        .heat(0.75D, 0.02D, 2744D)
                        .colorTint(TINT_PLUTONIUM));
        add(entries, "rbmk_fuel_mep", "rbmk_pellet_mep", "Medium Enriched Plutonium-239", true, true,
                spec(100_000_000D).stats(35D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.SQUARE_ROOT)
                        .heat(1.0D, 0.02D, 2744D)
                        .colorTint(TINT_PLUTONIUM));
        add(entries, "rbmk_fuel_hep239", "rbmk_pellet_hep239", "Highly Enriched Plutonium-239", true, true,
                spec(100_000_000D).stats(30D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.LINEAR)
                        .heat(1.25D, 0.02D, 2744D)
                        .colorTint(TINT_PLUTONIUM));
        add(entries, "rbmk_fuel_hep241", "rbmk_pellet_hep241", "Highly Enriched Plutonium-241", true, true,
                spec(100_000_000D).stats(40D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.LINEAR)
                        .heat(1.75D, 0.02D, 2744D)
                        .colorTint(TINT_PLUTONIUM));
        add(entries, "rbmk_fuel_lea", "rbmk_pellet_lea", "Low Enriched Americium-242", true, true,
                spec(100_000_000D).stats(60D, 10D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.SQUARE_ROOT)
                        .depletionFunction(RBMKFuelRodRuntime.DepletionFunction.RAISING_SLOPE)
                        .heat(1.5D, 0.02D, 2386D)
                        .colorTint(TINT_AMERICIUM));
        add(entries, "rbmk_fuel_mea", "rbmk_pellet_mea", "Medium Enriched Americium-242", true, true,
                spec(100_000_000D).stats(35D, 20D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.ARCH)
                        .heat(1.75D, 0.02D, 2386D)
                        .colorTint(TINT_AMERICIUM));
        add(entries, "rbmk_fuel_hea241", "rbmk_pellet_hea241", "Highly Enriched Americium-241", true, true,
                spec(100_000_000D).stats(65D, 15D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.SQUARE_ROOT)
                        .heat(1.85D, 0.02D, 2386D)
                        .neutronTypes(RBMKFluxReceiver.NType.FAST, RBMKFluxReceiver.NType.FAST)
                        .colorTint(TINT_AMERICIUM));
        add(entries, "rbmk_fuel_hea242", "rbmk_pellet_hea242", "Highly Enriched Americium-242", true, true,
                spec(100_000_000D).stats(45D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.LINEAR)
                        .heat(2.0D, 0.02D, 2386D)
                        .colorTint(TINT_AMERICIUM));
        add(entries, "rbmk_fuel_men", "rbmk_pellet_men", "Medium Enriched Neptunium-237", true, true,
                spec(100_000_000D).stats(30D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.SQUARE_ROOT)
                        .depletionFunction(RBMKFuelRodRuntime.DepletionFunction.RAISING_SLOPE)
                        .heat(0.75D, 0.02D, 2800D)
                        .neutronTypes(RBMKFluxReceiver.NType.ANY, RBMKFluxReceiver.NType.FAST)
                        .colorTint(TINT_NEPTUNIUM));
        add(entries, "rbmk_fuel_hen", "rbmk_pellet_hen", "Highly Enriched Neptunium-237", true, true,
                spec(100_000_000D).stats(40D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.SQUARE_ROOT)
                        .heat(1.0D, 0.02D, 2800D)
                        .neutronTypes(RBMKFluxReceiver.NType.FAST, RBMKFluxReceiver.NType.FAST)
                        .colorTint(TINT_NEPTUNIUM));
        add(entries, "rbmk_fuel_mox", "rbmk_pellet_mox", "Mixed MEU & LEP Oxide", true, true,
                spec(100_000_000D).stats(40D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.LOG_TEN)
                        .depletionFunction(RBMKFuelRodRuntime.DepletionFunction.RAISING_SLOPE)
                        .heat(1.0D, 0.02D, 2815D)
                        .colorTint(TINT_URANIUM));
        add(entries, "rbmk_fuel_les", "rbmk_pellet_les", "Low Enriched Schrabidium-326", true, true,
                spec(100_000_000D).stats(50D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.SQUARE_ROOT)
                        .heat(1.25D, 0.02D, 2500D)
                        .neutronTypes(RBMKFluxReceiver.NType.SLOW, RBMKFluxReceiver.NType.SLOW)
                        .colorTint(TINT_SCHRABIDIUM));
        add(entries, "rbmk_fuel_mes", "rbmk_pellet_mes", "Medium Enriched Schrabidium-326", true, true,
                spec(100_000_000D).stats(75D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.ARCH)
                        .heat(1.5D, 0.02D, 2750D)
                        .colorTint(TINT_SCHRABIDIUM));
        add(entries, "rbmk_fuel_hes", "rbmk_pellet_hes", "Highly Enriched Schrabidium-326", true, true,
                spec(100_000_000D).stats(90D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.LINEAR)
                        .depletionFunction(RBMKFuelRodRuntime.DepletionFunction.LINEAR)
                        .heat(1.75D, 0.02D, 3000D)
                        .colorTint(TINT_SCHRABIDIUM));
        add(entries, "rbmk_fuel_leaus", "rbmk_pellet_leaus", "Low Enriched Australium (Tasmanite)", true, true,
                spec(100_000_000D).stats(30D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.SIGMOID)
                        .depletionFunction(RBMKFuelRodRuntime.DepletionFunction.LINEAR)
                        .xenon(0.05D, 50D)
                        .heat(1.5D, 0.02D, 7029D)
                        .colorTint(TINT_AUSTRALIUM));
        add(entries, "rbmk_fuel_heaus", "rbmk_pellet_heaus", "Highly Enriched Australium (Ayerite)", true, true,
                spec(100_000_000D).stats(35D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.LINEAR)
                        .xenon(0.05D, 50D)
                        .heat(1.5D, 0.02D, 5211D)
                        .colorTint(TINT_AUSTRALIUM));
        add(entries, "rbmk_fuel_po210be", "rbmk_pellet_po210be", "Polonium-210 & Beryllium Neutron Source",
                true, false,
                spec(25_000_000D).stats(0D, 50D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.PASSIVE)
                        .depletionFunction(RBMKFuelRodRuntime.DepletionFunction.LINEAR)
                        .xenon(0.0D, 50D)
                        .heat(0.1D, 0.05D, 1287D)
                        .neutronTypes(RBMKFluxReceiver.NType.SLOW, RBMKFluxReceiver.NType.SLOW)
                        .colorTint(TINT_POLONIUM));
        add(entries, "rbmk_fuel_ra226be", "rbmk_pellet_ra226be", "Radium-226 & Beryllium Neutron Source",
                true, false,
                spec(100_000_000D).stats(0D, 20D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.PASSIVE)
                        .depletionFunction(RBMKFuelRodRuntime.DepletionFunction.LINEAR)
                        .xenon(0.0D, 50D)
                        .heat(0.035D, 0.5D, 700D)
                        .neutronTypes(RBMKFluxReceiver.NType.SLOW, RBMKFluxReceiver.NType.SLOW)
                        .colorTint(TINT_RADIUM));
        add(entries, "rbmk_fuel_pu238be", "rbmk_pellet_pu238be", "Plutonium-238 & Beryllium Neutron Source",
                true, true,
                spec(50_000_000D).stats(40D, 40D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.SQUARE_ROOT)
                        .heat(0.1D, 0.05D, 1287D)
                        .neutronTypes(RBMKFluxReceiver.NType.SLOW, RBMKFluxReceiver.NType.SLOW)
                        .colorTint(TINT_PLUTONIUM));
        add(entries, "rbmk_fuel_balefire_gold", "rbmk_pellet_balefire_gold",
                "Antihydrogen in a Magnetized Gold-198 Lattice", true, false,
                spec(100_000_000D).stats(50D, 10D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.ARCH)
                        .depletionFunction(RBMKFuelRodRuntime.DepletionFunction.LINEAR)
                        .xenon(0.0D, 50D)
                        .heat(1.0D, 0.02D, 2000D)
                        .colorTint(TINT_FLASHGOLD));
        add(entries, "rbmk_fuel_flashlead", "rbmk_pellet_flashlead",
                "Antihydrogen confined by a Magnetized Gold-198 and Lead-209 Lattice", true, false,
                spec(250_000_000D).stats(40D, 50D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.ARCH)
                        .depletionFunction(RBMKFuelRodRuntime.DepletionFunction.LINEAR)
                        .xenon(0.0D, 50D)
                        .heat(1.0D, 0.02D, 2050D)
                        .colorTint(TINT_FLASHLEAD));
        add(entries, "rbmk_fuel_balefire", "rbmk_pellet_balefire", "Draconic Flames", true, false,
                spec(100_000_000D).stats(100D, 35D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.LINEAR)
                        .xenon(0.0D, 50D)
                        .heat(3.0D, 0.02D, 3652D)
                        .colorTint(TINT_BALEFIRE));
        add(entries, "rbmk_fuel_zfb_bismuth", "rbmk_pellet_zfb_bismuth",
                "Zirconium Fast Breeder - LEU/HEP-241#Bi", true, true,
                spec(50_000_000D).stats(20D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.SQUARE_ROOT)
                        .heat(1.75D, 0.02D, 2744D)
                        .colorTint(TINT_ZIRCONIUM));
        add(entries, "rbmk_fuel_zfb_pu241", "rbmk_pellet_zfb_pu241",
                "Zirconium Fast Breeder - HEU-235/HEP-240#Pu-241", true, true,
                spec(50_000_000D).stats(20D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.SQUARE_ROOT)
                        .heat(1.0D, 0.02D, 2865D)
                        .colorTint(TINT_ZIRCONIUM));
        add(entries, "rbmk_fuel_zfb_am_mix", "rbmk_pellet_zfb_am_mix",
                "Zirconium Fast Breeder - HEP-241#MEA", true, true,
                spec(50_000_000D).stats(20D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.LINEAR)
                        .heat(1.75D, 0.02D, 2744D)
                        .colorTint(TINT_ZIRCONIUM));
        add(entries, "rbmk_fuel_drx", "rbmk_pellet_drx", "can't you hear, can't you hear the thunder?",
                true, true,
                spec(10_000_000D).stats(1000D, 10D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.QUADRATIC)
                        .heat(0.1D, 0.02D, 100_000D)
                        .colorTint(TINT_DRX));
        add(entries, "rbmk_fuel_test", "", "THE VOICES", false, true,
                spec(1_000_000D).stats(100D, 0D)
                        .burnFunction(RBMKFuelRodRuntime.BurnFunction.EXPERIMENTAL)
                        .heat(1.0D, 0.02D, 100_000D)
                        .colorTint(0x304825));

        return Collections.unmodifiableMap(entries);
    }

    private static RBMKFuelRodSpec.Builder spec(double yield) {
        return RBMKFuelRodSpec.builder(yield);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private static void add(
            Map<String, Entry> entries,
            String legacyRodId,
            String legacyPelletId,
            String fullName,
            boolean craftable,
            boolean pelletXenonOverlay,
            RBMKFuelRodSpec.Builder builder) {
        entries.put(legacyRodId, new Entry(
                legacyRodId,
                legacyPelletId,
                fullName,
                craftable,
                pelletXenonOverlay,
                builder.build()));
    }

    public record Entry(
            String legacyRodId,
            String legacyPelletId,
            String fullName,
            boolean craftable,
            boolean pelletXenonOverlay,
            RBMKFuelRodSpec spec) {
    }

    public enum DisassemblyFailure {
        UNKNOWN_ROD,
        NOT_CRAFTABLE,
        TOO_HOT,
        NOT_DEPLETED
    }

    public record RodDisassemblyPlan(
            boolean accepted,
            DisassemblyFailure failure,
            String legacyPelletId,
            int pelletCount,
            int pelletMeta) {
        private static RodDisassemblyPlan reject(DisassemblyFailure failure) {
            return new RodDisassemblyPlan(false, failure, "", 0, 0);
        }
    }

    public record CoolingPoolPlan(boolean accepted, String legacyRodId, RBMKFuelRodState outputState) {
        private static CoolingPoolPlan reject() {
            return new CoolingPoolPlan(false, "", null);
        }
    }
}
