package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RBMKItemPlanner {
    public static final String STANDARD_LID_ITEM_ID = "rbmk_lid";
    public static final String GLASS_LID_ITEM_ID = "rbmk_lid_glass";
    public static final String STANDARD_LID_SOUND_BLOCK_ID = "concrete_smooth";
    public static final String GLASS_LID_SOUND_BLOCK_ID = "minecraft:glass";
    public static final float LID_SOUND_PITCH_MULTIPLIER = 0.8F;
    public static final double WASTE_COOLING_HEAT_THRESHOLD = 50.0D;
    public static final String PELLET_RECYCLING_LITERAL = "Pellet for recycling";
    public static final String PELLET_HIGH_XENON_LITERAL = "High Xenon Poison";

    private RBMKItemPlanner() {
    }

    public static LidUsePlan planLidUse(
            BlockPos core,
            LidItemType lidItemType,
            boolean targetIsRBMKBase,
            boolean foundCore,
            boolean hasRBMKBaseTile,
            boolean hasLid) {
        if (!targetIsRBMKBase) {
            return LidUsePlan.reject(LidUseFailure.TARGET_NOT_RBMK_BASE);
        }
        if (!foundCore) {
            return LidUsePlan.reject(LidUseFailure.CORE_NOT_FOUND);
        }
        if (!hasRBMKBaseTile) {
            return LidUsePlan.reject(LidUseFailure.MISSING_RBMK_BASE_TILE);
        }
        if (hasLid) {
            return LidUsePlan.reject(LidUseFailure.COLUMN_ALREADY_HAS_LID);
        }

        LidItemType safeType = lidItemType == null ? LidItemType.STANDARD : lidItemType;
        RBMKColumnLifecyclePlanner.LidType lidType = safeType == LidItemType.GLASS
                ? RBMKColumnLifecyclePlanner.LidType.GLASS
                : RBMKColumnLifecyclePlanner.LidType.STANDARD;
        return new LidUsePlan(
                true,
                null,
                core == null ? BlockPos.ZERO : core,
                safeType,
                lidType,
                RBMKColumnLifecyclePlanner.legacyMetaForLid(lidType),
                true,
                true,
                true,
                safeType == LidItemType.GLASS ? GLASS_LID_SOUND_BLOCK_ID : STANDARD_LID_SOUND_BLOCK_ID,
                LID_SOUND_PITCH_MULTIPLIER);
    }

    public static FuelRodDefaultNbtPlan fuelRodDefaultNbt(String legacyRodId) {
        return RBMKFuelRodRegistry.find(legacyRodId)
                .map(entry -> fuelRodDefaultNbt(entry.legacyRodId(), entry.spec()))
                .orElseGet(() -> FuelRodDefaultNbtPlan.reject(legacyRodId));
    }

    public static FuelRodDefaultNbtPlan fuelRodDefaultNbt(String legacyRodId, RBMKFuelRodSpec spec) {
        if (spec == null) {
            return FuelRodDefaultNbtPlan.reject(legacyRodId);
        }
        return new FuelRodDefaultNbtPlan(
                true,
                legacyRodId == null ? "" : legacyRodId,
                spec.totalYield(),
                RBMKFuelRodState.DEFAULT_HEAT,
                RBMKFuelRodState.DEFAULT_HEAT,
                0.0D,
                false,
                List.of(
                        RBMKFuelRodState.TAG_YIELD,
                        RBMKFuelRodState.TAG_CORE_HEAT,
                        RBMKFuelRodState.TAG_HULL_HEAT),
                List.of(
                        RBMKFuelRodState.TAG_YIELD,
                        RBMKFuelRodState.TAG_XENON,
                        RBMKFuelRodState.TAG_CORE_HEAT,
                        RBMKFuelRodState.TAG_HULL_HEAT));
    }

    public static RBMKFuelRodState fuelRodFreshState(RBMKFuelRodSpec spec) {
        return spec == null ? new RBMKFuelRodState() : RBMKFuelRodState.fresh(spec);
    }

    public static double rectifyFuelRodHeat(double value) {
        return RBMKFuelRodState.rectify(value);
    }

    public static FuelRodDurabilityPlan fuelRodDurability(RBMKFuelRodSpec spec, RBMKFuelRodState state) {
        if (spec == null || state == null) {
            return new FuelRodDurabilityPlan(false, 0.0D);
        }
        double display = 1.0D - state.enrichment(spec);
        return new FuelRodDurabilityPlan(display > 0.0D, display);
    }

    public static FuelRodTooltipPlan fuelRodTooltip(String legacyRodId, RBMKFuelRodState state) {
        return RBMKFuelRodRegistry.find(legacyRodId)
                .map(entry -> fuelRodTooltip(entry, state))
                .orElseGet(() -> FuelRodTooltipPlan.reject(legacyRodId));
    }

    public static FuelRodTooltipPlan fuelRodTooltip(RBMKFuelRodRegistry.Entry entry, RBMKFuelRodState state) {
        if (entry == null) {
            return FuelRodTooltipPlan.reject("");
        }

        RBMKFuelRodSpec spec = entry.spec();
        RBMKFuelRodState safeState = state == null ? RBMKFuelRodState.fresh(spec) : state;
        boolean drx = "rbmk_fuel_drx".equals(entry.legacyRodId());
        String prefix = drx ? "trait.rbmx." : "trait.rbmk.";
        String neutronSuffix = drx ? ".x" : "";
        String temperatureUnit = drx ? "m" : "\u00B0C";

        List<TooltipLine> lines = new ArrayList<>();
        lines.add(TooltipLine.literal(TooltipStyle.ITALIC, entry.fullName()));

        if (safeState.hullHeat() >= WASTE_COOLING_HEAT_THRESHOLD
                || safeState.coreHeat() >= WASTE_COOLING_HEAT_THRESHOLD) {
            lines.add(TooltipLine.translation(TooltipStyle.GOLD, "desc.item.wasteCooling"));
        }
        if (spec.selfRate() > 0.0D || spec.burnFunction() == RBMKFuelRodRuntime.BurnFunction.SIGMOID) {
            lines.add(TooltipLine.translation(TooltipStyle.RED, prefix + "source"));
        }

        lines.add(TooltipLine.translation(TooltipStyle.GREEN, prefix + "depletion",
                depletionPercent(spec, safeState)));
        lines.add(TooltipLine.translation(TooltipStyle.DARK_PURPLE, prefix + "xenon",
                truncated(safeState.xenon(), 1000.0D) + "%"));
        lines.add(TooltipLine.translationValueKey(TooltipStyle.BLUE, prefix + "splitsWith",
                spec.inputType().translationKey() + neutronSuffix));
        lines.add(TooltipLine.translationValueKey(TooltipStyle.BLUE, prefix + "splitsInto",
                spec.outputType().translationKey() + neutronSuffix));
        lines.add(TooltipLine.translation(TooltipStyle.YELLOW, prefix + "fluxFunc",
                fuelRodFluxFunctionDescription(spec, safeState)));
        lines.add(TooltipLine.translation(TooltipStyle.YELLOW, prefix + "funcType",
                burnFunctionTitle(spec.burnFunction())));
        lines.add(TooltipLine.translation(TooltipStyle.YELLOW, prefix + "xenonGen",
                "x * " + spec.xenonGeneration()));
        lines.add(TooltipLine.translation(TooltipStyle.YELLOW, prefix + "xenonBurn",
                "x\u00B2 / " + spec.xenonBurnDivisor()));
        lines.add(TooltipLine.translation(TooltipStyle.GOLD, prefix + "heat",
                spec.heatPerFlux() + "\u00B0C"));
        lines.add(TooltipLine.translation(TooltipStyle.GOLD, prefix + "diffusion",
                spec.diffusion() + "\u00B9/\u00B2"));
        lines.add(TooltipLine.translation(TooltipStyle.RED, prefix + "skinTemp",
                truncated(safeState.hullHeat(), 10.0D) + temperatureUnit));
        lines.add(TooltipLine.translation(TooltipStyle.RED, prefix + "coreTemp",
                truncated(safeState.coreHeat(), 10.0D) + temperatureUnit));
        lines.add(TooltipLine.translation(TooltipStyle.DARK_RED, prefix + "melt",
                spec.meltingPoint() + temperatureUnit));

        return new FuelRodTooltipPlan(true, entry.legacyRodId(), drx, lines);
    }

    public static String fuelRodFluxFunctionDescription(RBMKFuelRodSpec spec, RBMKFuelRodState state) {
        if (spec == null || state == null) {
            return "";
        }

        String function = switch (spec.burnFunction()) {
            case PASSIVE -> Double.toString(spec.selfRate());
            case LOG_TEN -> "log10(%1$s + 1) * 0.5 * %2$s";
            case PLATEU -> "(1 - e^(-%1$s / 25)) * %2$s";
            case ARCH -> "(%1$s - %1$s\u00B2 / 10000) / 100 * %2$s [0;infinity]";
            case SIGMOID -> "%2$s / (1 + e^(-(%1$s - 50) / 10))";
            case SQUARE_ROOT -> "sqrt(%1$s) * %2$s / 10";
            case LINEAR -> "%1$s / 100 * %2$s";
            case QUADRATIC -> "%1$s\u00B2 / 10000 * %2$s";
            case EXPERIMENTAL -> "%1$s * (sin(%1$s) + 1) * %2$s";
        };

        double enrichment = state.enrichment(spec);
        String input = spec.selfRate() > 0.0D ? "(x + " + spec.selfRate() + ")" : "x";
        if (enrichment < 1.0D) {
            double modified = RBMKFuelRodRuntime.reactivityModifierByEnrichment(spec.depletionFunction(), enrichment);
            String reactivity = Double.toString(truncated(spec.reactivity() * modified, 1000.0D));
            String enrichmentPercent = " (" + truncated(modified * 100.0D, 10.0D) + "%)";
            return String.format(Locale.US, function, input, reactivity).concat(enrichmentPercent);
        }
        return String.format(Locale.US, function, input, Double.toString(spec.reactivity()));
    }

    public static String burnFunctionTitle(RBMKFuelRodRuntime.BurnFunction function) {
        return switch (function == null ? RBMKFuelRodRuntime.BurnFunction.LOG_TEN : function) {
            case PASSIVE -> "SAFE / PASSIVE";
            case LOG_TEN -> "MEDIUM / LOGARITHMIC";
            case PLATEU -> "SAFE / EULER";
            case ARCH -> "DANGEROUS / NEGATIVE-QUADRATIC";
            case SIGMOID -> "SAFE / SIGMOID";
            case SQUARE_ROOT -> "MEDIUM / SQUARE ROOT";
            case LINEAR -> "DANGEROUS / LINEAR";
            case QUADRATIC -> "DANGEROUS / QUADRATIC";
            case EXPERIMENTAL -> "EXPERIMENTAL / SINE SLOPE";
        };
    }

    public static PelletMetaPlan pelletMeta(int meta, boolean xenonEnabled) {
        int rectified = rectifyPelletMeta(meta);
        boolean highXenon = pelletHasXenon(meta);
        return new PelletMetaPlan(
                meta,
                rectified,
                rectified % 5,
                highXenon,
                xenonEnabled,
                xenonEnabled ? 10 : 5,
                highXenon ? 3 : 2);
    }

    public static int rectifyPelletMeta(int meta) {
        return Math.abs(meta) % 10;
    }

    public static boolean pelletHasXenon(int meta) {
        return rectifyPelletMeta(meta) >= 5;
    }

    public static PelletRenderPlan pelletRenderPlan(int meta, boolean xenonEnabled) {
        PelletMetaPlan metaPlan = pelletMeta(meta, xenonEnabled);
        List<PelletRenderPass> passes = new ArrayList<>();
        passes.add(new PelletRenderPass(0, PelletRenderLayer.BASE, ""));
        passes.add(new PelletRenderPass(1, PelletRenderLayer.ENRICHMENT_OVERLAY,
                "rbmk_pellet_overlay_e" + metaPlan.depletionMeta()));
        if (metaPlan.metadataHasXenon()) {
            passes.add(new PelletRenderPass(2, PelletRenderLayer.XENON_OVERLAY,
                    "rbmk_pellet_overlay_xenon"));
        }
        return new PelletRenderPlan(metaPlan, passes);
    }

    public static PelletTooltipPlan pelletTooltip(String fullName, int meta, boolean xenonEnabled) {
        PelletMetaPlan metaPlan = pelletMeta(meta, xenonEnabled);
        List<TooltipLine> lines = new ArrayList<>();
        lines.add(TooltipLine.literal(TooltipStyle.ITALIC, fullName == null ? "" : fullName));
        lines.add(TooltipLine.literal(TooltipStyle.DARK_GRAY_ITALIC, PELLET_RECYCLING_LITERAL));
        lines.add(TooltipLine.literal(pelletDepletionStyle(metaPlan.depletionMeta()),
                pelletDepletionLiteral(metaPlan.depletionMeta())));
        if (metaPlan.metadataHasXenon()) {
            lines.add(TooltipLine.literal(TooltipStyle.DARK_PURPLE, PELLET_HIGH_XENON_LITERAL));
        }
        return new PelletTooltipPlan(true, true, metaPlan, lines);
    }

    private static String depletionPercent(RBMKFuelRodSpec spec, RBMKFuelRodState state) {
        if (spec.totalYield() <= 0.0D) {
            return "0.0%";
        }
        return truncated(((spec.totalYield() - state.remainingYield()) / spec.totalYield()) * 100.0D, 1000.0D) + "%";
    }

    private static String pelletDepletionLiteral(int depletionMeta) {
        return switch (depletionMeta) {
            case 0 -> "Brand New";
            case 1 -> "Barely Depleted";
            case 2 -> "Moderately Depleted";
            case 3 -> "Highly Depleted";
            default -> "Fully Depleted";
        };
    }

    private static TooltipStyle pelletDepletionStyle(int depletionMeta) {
        return switch (depletionMeta) {
            case 0 -> TooltipStyle.GOLD;
            case 1 -> TooltipStyle.YELLOW;
            case 2 -> TooltipStyle.GREEN;
            case 3 -> TooltipStyle.DARK_GREEN;
            default -> TooltipStyle.DARK_GRAY;
        };
    }

    private static double truncated(double value, double scale) {
        return ((int) (value * scale)) / scale;
    }

    public enum LidItemType {
        STANDARD(STANDARD_LID_ITEM_ID),
        GLASS(GLASS_LID_ITEM_ID);

        private final String legacyItemId;

        LidItemType(String legacyItemId) {
            this.legacyItemId = legacyItemId;
        }

        public String legacyItemId() {
            return legacyItemId;
        }
    }

    public enum LidUseFailure {
        TARGET_NOT_RBMK_BASE,
        CORE_NOT_FOUND,
        MISSING_RBMK_BASE_TILE,
        COLUMN_ALREADY_HAS_LID
    }

    public enum TooltipStyle {
        ITALIC,
        DARK_GRAY_ITALIC,
        GOLD,
        RED,
        GREEN,
        DARK_PURPLE,
        BLUE,
        YELLOW,
        DARK_RED,
        DARK_GREEN,
        DARK_GRAY
    }

    public enum PelletRenderLayer {
        BASE,
        ENRICHMENT_OVERLAY,
        XENON_OVERLAY
    }

    public record LidUsePlan(
            boolean accepted,
            LidUseFailure failure,
            BlockPos core,
            LidItemType lidItemType,
            RBMKColumnLifecyclePlanner.LidType lidType,
            int newLegacyCoreMeta,
            boolean addNeutronNodeLid,
            boolean suppressExplodeOnBrokenDuringMutation,
            boolean consumeHeldStack,
            String legacySoundBlockId,
            float soundPitchMultiplier) {
        private static LidUsePlan reject(LidUseFailure failure) {
            return new LidUsePlan(
                    false,
                    failure,
                    BlockPos.ZERO,
                    LidItemType.STANDARD,
                    RBMKColumnLifecyclePlanner.LidType.NONE,
                    RBMKColumnLifecyclePlanner.legacyMetaForLid(RBMKColumnLifecyclePlanner.LidType.NONE),
                    false,
                    false,
                    false,
                    "",
                    0.0F);
        }
    }

    public record FuelRodDefaultNbtPlan(
            boolean accepted,
            String legacyRodId,
            double remainingYield,
            double coreHeat,
            double hullHeat,
            double xenonDefault,
            boolean writesXenonTagOnDefault,
            List<String> defaultWrittenKeys,
            List<String> knownKeys) {
        private static FuelRodDefaultNbtPlan reject(String legacyRodId) {
            return new FuelRodDefaultNbtPlan(
                    false,
                    legacyRodId == null ? "" : legacyRodId,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D,
                    false,
                    List.of(),
                    List.of());
        }
    }

    public record FuelRodDurabilityPlan(boolean showBar, double displayValue) {
    }

    public record FuelRodTooltipPlan(boolean accepted, String legacyRodId, boolean drx, List<TooltipLine> lines) {
        private static FuelRodTooltipPlan reject(String legacyRodId) {
            return new FuelRodTooltipPlan(false, legacyRodId == null ? "" : legacyRodId, false, List.of());
        }
    }

    public record TooltipLine(
            TooltipStyle style,
            String literal,
            String translationKey,
            String argument,
            String argumentTranslationKey) {
        private static TooltipLine literal(TooltipStyle style, String literal) {
            return new TooltipLine(style, literal == null ? "" : literal, "", "", "");
        }

        private static TooltipLine translation(TooltipStyle style, String translationKey) {
            return new TooltipLine(style, "", translationKey == null ? "" : translationKey, "", "");
        }

        private static TooltipLine translation(TooltipStyle style, String translationKey, String argument) {
            return new TooltipLine(
                    style,
                    "",
                    translationKey == null ? "" : translationKey,
                    argument == null ? "" : argument,
                    "");
        }

        private static TooltipLine translationValueKey(
                TooltipStyle style,
                String translationKey,
                String argumentTranslationKey) {
            return new TooltipLine(
                    style,
                    "",
                    translationKey == null ? "" : translationKey,
                    "",
                    argumentTranslationKey == null ? "" : argumentTranslationKey);
        }
    }

    public record PelletMetaPlan(
            int inputMeta,
            int rectifiedMeta,
            int depletionMeta,
            boolean metadataHasXenon,
            boolean itemXenonEnabled,
            int creativeSubItemCount,
            int renderPasses) {
    }

    public record PelletRenderPass(int pass, PelletRenderLayer layer, String legacyIconName) {
    }

    public record PelletRenderPlan(PelletMetaPlan meta, List<PelletRenderPass> passes) {
    }

    public record PelletTooltipPlan(
            boolean accepted,
            boolean nuclearWasteTooltipRunsFirst,
            PelletMetaPlan meta,
            List<TooltipLine> lines) {
    }
}
