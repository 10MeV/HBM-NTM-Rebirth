package com.hbm.items.machine;

import com.hbm.ntm.item.RBMKFuelRodItem;
import com.hbm.ntm.neutron.NeutronHandler;
import com.hbm.ntm.neutron.RBMKFluxReceiver;
import com.hbm.ntm.neutron.RBMKFuelRodRegistry;
import com.hbm.ntm.neutron.RBMKFuelRodRuntime;
import com.hbm.ntm.neutron.RBMKFuelRodSpec;
import com.hbm.ntm.neutron.RBMKFuelRodState;
import com.hbm.ntm.neutron.RBMKItemPlanner;
import com.hbm.ntm.registry.ModItems;
import com.hbm.tileentity.machine.rbmk.IRBMKFluxReceiver.NType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy 1.7.10 package bridge for RBMK fuel rods.
 */
@Deprecated(forRemoval = false)
public class ItemRBMKRod extends RBMKFuelRodItem {
    public ItemRBMKPellet pellet;
    public String fullName = "";
    public double reactivity;
    public double selfRate;
    public EnumBurnFunc function = EnumBurnFunc.LOG_TEN;
    public EnumDepleteFunc depFunc = EnumDepleteFunc.GENTLE_SLOPE;
    public double xGen = 0.5D;
    public double xBurn = 50.0D;
    public double heat = 1.0D;
    public double yield;
    public double meltingPoint = 1000.0D;
    public double diffusion = 0.02D;
    public NType nType = NType.SLOW;
    public NType rType = NType.FAST;
    public int colorTint = 0x304825;
    public double heatCoeffStart;
    public double heatCoeffLength;
    public boolean specialFluxCurve;

    public static final List<ItemRBMKRod> craftableRods = new ArrayList<>();

    private final String legacyRodId;
    private final String legacyPelletId;
    private final boolean craftable;
    private final boolean pelletXenonOverlay;
    private BiFunction<Double, Double, Double> ratioCurve;
    private BiFunction<Double, Double, Double> fluxCurve;

    public ItemRBMKRod(Item.Properties properties, RBMKFuelRodRegistry.Entry entry) {
        this(properties, entry, null);
    }

    public ItemRBMKRod(Item.Properties properties, RBMKFuelRodRegistry.Entry entry, @Nullable ItemRBMKPellet pellet) {
        super(properties, entry);
        this.legacyRodId = entry.legacyRodId();
        this.legacyPelletId = entry.legacyPelletId();
        this.craftable = entry.craftable();
        this.pelletXenonOverlay = entry.pelletXenonOverlay();
        this.pellet = pellet;
        loadLegacyFields(entry.fullName(), entry.spec());
        if (entry.craftable()) {
            craftableRods.add(this);
        }
    }

    public ItemRBMKRod(ItemRBMKPellet pellet) {
        this(new Item.Properties(), entryByPellet(pellet), pellet);
    }

    public ItemRBMKRod(String fullName) {
        this(new Item.Properties(), entryByFullName(fullName));
        this.fullName = fullName;
    }

    @Override
    public String getLegacyRodId() {
        return legacyRodId;
    }

    @Override
    public RBMKFuelRodSpec getSpec() {
        return specFromLegacyFields();
    }

    @Override
    public RBMKFuelRodState getState(ItemStack stack) {
        RBMKFuelRodSpec spec = getSpec();
        RBMKFuelRodState state = RBMKFuelRodState.fresh(spec);
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            state.load(tag, spec);
        }
        return state;
    }

    @Override
    public void setLegacyDefaultState(ItemStack stack) {
        RBMKFuelRodState.fresh(getSpec()).saveLegacyDefaults(stack.getOrCreateTag());
    }

    public ItemRBMKRod setTint(int tint) {
        this.colorTint = tint;
        return this;
    }

    public ItemRBMKRod setYield(double yield) {
        this.yield = yield;
        return this;
    }

    public ItemRBMKRod setStats(double funcEnd) {
        return setStats(funcEnd, 0.0D);
    }

    public ItemRBMKRod setStats(double funcEnd, double selfRate) {
        this.reactivity = funcEnd;
        this.selfRate = selfRate;
        return this;
    }

    public ItemRBMKRod setFunction(EnumBurnFunc func) {
        this.function = func == null ? EnumBurnFunc.LOG_TEN : func;
        return this;
    }

    public ItemRBMKRod setDepletionFunction(EnumDepleteFunc func) {
        this.depFunc = func == null ? EnumDepleteFunc.GENTLE_SLOPE : func;
        return this;
    }

    public ItemRBMKRod setHeatCoeff(double start, double length) {
        this.heatCoeffStart = start;
        this.heatCoeffLength = length;
        return this;
    }

    public ItemRBMKRod setXenon(double gen, double burn) {
        this.xGen = gen;
        this.xBurn = burn;
        return this;
    }

    public ItemRBMKRod setHeat(double heat) {
        this.heat = heat;
        return this;
    }

    public ItemRBMKRod setDiffusion(double diffusion) {
        this.diffusion = diffusion;
        return this;
    }

    public ItemRBMKRod setMeltingPoint(double meltingPoint) {
        this.meltingPoint = meltingPoint;
        return this;
    }

    public ItemRBMKRod setNeutronTypes(NType nType, NType rType) {
        this.nType = nType == null ? NType.SLOW : nType;
        this.rType = rType == null ? NType.FAST : rType;
        return this;
    }

    public double burn(Level level, ItemStack stack, double inFlux) {
        RBMKFuelRodSpec spec = getSpec();
        RBMKFuelRodState state = getState(stack);
        double outFlux = RBMKFuelRodRuntime.burn(NeutronHandler.rbmkRuntimeSettings(level), spec, state, inFlux);
        setState(stack, state);
        return outFlux;
    }

    public void updateHeat(Level level, ItemStack stack, double mod) {
        RBMKFuelRodState state = getState(stack);
        RBMKFuelRodRuntime.updateHeat(NeutronHandler.rbmkRuntimeSettings(level), getSpec(), state, mod);
        setState(stack, state);
    }

    public double provideHeat(Level level, ItemStack stack, double heat, double mod) {
        RBMKFuelRodState state = getState(stack);
        double provided =
                RBMKFuelRodRuntime.provideHeat(NeutronHandler.rbmkRuntimeSettings(level), getSpec(), state, heat, mod);
        setState(stack, state);
        return provided;
    }

    public enum EnumBurnFunc {
        PASSIVE(ChatFormatting.DARK_GREEN + "SAFE / PASSIVE"),
        LOG_TEN(ChatFormatting.YELLOW + "MEDIUM / LOGARITHMIC"),
        PLATEU(ChatFormatting.GREEN + "SAFE / EULER"),
        ARCH(ChatFormatting.RED + "DANGEROUS / NEGATIVE-QUADRATIC"),
        SIGMOID(ChatFormatting.GREEN + "SAFE / SIGMOID"),
        SQUARE_ROOT(ChatFormatting.YELLOW + "MEDIUM / SQUARE ROOT"),
        LINEAR(ChatFormatting.RED + "DANGEROUS / LINEAR"),
        QUADRATIC(ChatFormatting.RED + "DANGEROUS / QUADRATIC"),
        EXPERIMENTAL(ChatFormatting.RED + "EXPERIMENTAL / SINE SLOPE");

        public String title = "";

        EnumBurnFunc(String title) {
            this.title = title;
        }

        private static EnumBurnFunc fromModern(RBMKFuelRodRuntime.BurnFunction function) {
            return values()[function.ordinal()];
        }

        private RBMKFuelRodRuntime.BurnFunction toModern() {
            return RBMKFuelRodRuntime.BurnFunction.values()[ordinal()];
        }
    }

    public double reactivityFunc(double in, double enrichment) {
        double flux = in * reactivityModByEnrichment(enrichment);
        return switch (this.function) {
            case PASSIVE -> selfRate * enrichment;
            case LOG_TEN -> Math.log10(flux + 1.0D) * 0.5D * reactivity;
            case PLATEU -> (1.0D - Math.pow(Math.E, -flux / 25.0D)) * reactivity;
            case ARCH -> Math.max((flux - (flux * flux / 10000.0D)) / 100.0D * reactivity, 0.0D);
            case SIGMOID -> reactivity / (1.0D + Math.pow(Math.E, -(flux - 50.0D) / 10.0D));
            case SQUARE_ROOT -> Math.sqrt(flux) * reactivity / 10.0D;
            case LINEAR -> flux / 100.0D * reactivity;
            case QUADRATIC -> flux * flux / 10000.0D * reactivity;
            case EXPERIMENTAL -> flux * (Math.sin(flux) + 1.0D) * reactivity;
        };
    }

    public String getFuncDescription(ItemStack stack) {
        String function = switch (this.function) {
            case PASSIVE -> ChatFormatting.RED + "" + selfRate;
            case LOG_TEN -> "log10(%1$s + 1) * 0.5 * %2$s";
            case PLATEU -> "(1 - e^(-%1$s / 25)) * %2$s";
            case ARCH -> "(%1$s - %1$s\u00B2 / 10000) / 100 * %2$s [0;\u221E]";
            case SIGMOID -> "%2$s / (1 + e^(-(%1$s - 50) / 10))";
            case SQUARE_ROOT -> "sqrt(%1$s) * %2$s / 10";
            case LINEAR -> "%1$s / 100 * %2$s";
            case QUADRATIC -> "%1$s\u00B2 / 10000 * %2$s";
            case EXPERIMENTAL -> "%1$s * (sin(%1$s) + 1) * %2$s";
        };

        double enrichment = getEnrichment(stack);
        String input = selfRate > 0.0D
                ? "(x" + ChatFormatting.RED + " + " + selfRate + "" + ChatFormatting.WHITE + ")"
                : "x";
        if (enrichment < 1.0D) {
            enrichment = reactivityModByEnrichment(enrichment);
            String reactivityText = ChatFormatting.YELLOW + "" + ((int) (this.reactivity * enrichment * 1000.0D) / 1000.0D)
                    + ChatFormatting.WHITE;
            String enrichmentPercent = ChatFormatting.GOLD + " (" + ((int) (enrichment * 1000.0D) / 10.0D) + "%)";
            return String.format(Locale.US, function, input, reactivityText).concat(enrichmentPercent);
        }
        return String.format(Locale.US, function, input, reactivity);
    }

    public enum EnumDepleteFunc {
        LINEAR,
        RAISING_SLOPE,
        BOOSTED_SLOPE,
        GENTLE_SLOPE,
        STATIC;

        private static EnumDepleteFunc fromModern(RBMKFuelRodRuntime.DepletionFunction function) {
            return values()[function.ordinal()];
        }

        private RBMKFuelRodRuntime.DepletionFunction toModern() {
            return RBMKFuelRodRuntime.DepletionFunction.values()[ordinal()];
        }
    }

    public double reactivityModByEnrichment(double enrichment) {
        return switch (this.depFunc) {
            case LINEAR -> enrichment;
            case STATIC -> 1.0D;
            case BOOSTED_SLOPE -> enrichment + Math.sin((enrichment - 1.0D) * (enrichment - 1.0D) * Math.PI);
            case RAISING_SLOPE -> enrichment + (Math.sin(enrichment * Math.PI) / 2.0D);
            case GENTLE_SLOPE -> enrichment + (Math.sin(enrichment * Math.PI) / 3.0D);
        };
    }

    public double xenonGenFunc(double flux) {
        return flux * xGen;
    }

    public double xenonBurnFunc(double flux) {
        return (flux * flux) / xBurn;
    }

    public static double getEnrichment(ItemStack stack) {
        if (stack.getItem() instanceof ItemRBMKRod rod && rod.yield > 0.0D) {
            return getYield(stack) / rod.yield;
        }
        return 0.0D;
    }

    public static double getPoisonLevel(ItemStack stack) {
        return getPoison(stack) / 100.0D;
    }

    public ItemRBMKRod setFluxCurve(boolean bool) {
        specialFluxCurve = bool;
        return this;
    }

    public ItemRBMKRod setOutputRatioCurve(Function<Double, Double> func) {
        this.ratioCurve = (fluxRatioIn, depletion) -> func.apply(fluxRatioIn);
        return this;
    }

    public ItemRBMKRod setDepletionOutputRatioCurve(BiFunction<Double, Double, Double> func) {
        this.ratioCurve = func;
        return this;
    }

    public ItemRBMKRod setOutputFluxCurve(BiFunction<Double, Double, Double> func) {
        this.fluxCurve = func;
        return this;
    }

    public double fluxRatioOut(double fluxRatioIn, double depletion) {
        return ratioCurve == null ? 0.0D : Mth.clamp(ratioCurve.apply(fluxRatioIn, depletion), 0.0D, 1.0D);
    }

    public double fluxFromRatio(double quantity, double ratio) {
        return fluxCurve == null ? 0.0D : fluxCurve.apply(quantity, ratio);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RBMKItemPlanner.FuelRodTooltipPlan plan = RBMKItemPlanner.fuelRodTooltip(plannerEntry(), getState(stack));
        for (RBMKItemPlanner.TooltipLine line : plan.lines()) {
            tooltip.add(applyStyle(component(line), line.style()));
        }
    }

    public static void setYield(ItemStack stack, double yield) {
        setDouble(stack, RBMKFuelRodState.TAG_YIELD, yield);
    }

    public static double getYield(ItemStack stack) {
        if (stack.getItem() instanceof ItemRBMKRod) {
            return getDouble(stack, RBMKFuelRodState.TAG_YIELD);
        }
        return 0.0D;
    }

    public static void setPoison(ItemStack stack, double xenon) {
        setDouble(stack, RBMKFuelRodState.TAG_XENON, xenon);
    }

    public static double getPoison(ItemStack stack) {
        return getDouble(stack, RBMKFuelRodState.TAG_XENON);
    }

    public static void setCoreHeat(ItemStack stack, double heat) {
        setDouble(stack, RBMKFuelRodState.TAG_CORE_HEAT, heat);
    }

    public static double getCoreHeat(ItemStack stack) {
        return getDouble(stack, RBMKFuelRodState.TAG_CORE_HEAT);
    }

    public static void setHullHeat(ItemStack stack, double heat) {
        setDouble(stack, RBMKFuelRodState.TAG_HULL_HEAT, heat);
    }

    public static double getHullHeat(ItemStack stack) {
        return getDouble(stack, RBMKFuelRodState.TAG_HULL_HEAT);
    }

    public boolean showDurabilityBar(ItemStack stack) {
        return getDurabilityForDisplay(stack) > 0.0D;
    }

    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0D - getEnrichment(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return showDurabilityBar(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round((float) (13.0D * Mth.clamp(getEnrichment(stack), 0.0D, 1.0D)));
    }

    public static void setDouble(ItemStack stack, String key, double yield) {
        if (!stack.hasTag()) {
            setNBTDefaults(stack);
        }
        stack.getOrCreateTag().putDouble(key, yield);
    }

    public static double getDouble(ItemStack stack, String key) {
        if (!stack.hasTag()) {
            setNBTDefaults(stack);
        }
        return stack.getOrCreateTag().getDouble(key);
    }

    private static void setNBTDefaults(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putDouble(RBMKFuelRodState.TAG_YIELD, ((ItemRBMKRod) stack.getItem()).yield);
        tag.putDouble(RBMKFuelRodState.TAG_CORE_HEAT, RBMKFuelRodState.DEFAULT_HEAT);
        tag.putDouble(RBMKFuelRodState.TAG_HULL_HEAT, RBMKFuelRodState.DEFAULT_HEAT);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        setNBTDefaults(stack);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return new ItemStack(ModItems.RBMK_FUEL_EMPTY.get());
    }

    private void loadLegacyFields(String fullName, RBMKFuelRodSpec spec) {
        this.fullName = fullName;
        this.reactivity = spec.reactivity();
        this.selfRate = spec.selfRate();
        this.function = EnumBurnFunc.fromModern(spec.burnFunction());
        this.depFunc = EnumDepleteFunc.fromModern(spec.depletionFunction());
        this.xGen = spec.xenonGeneration();
        this.xBurn = spec.xenonBurnDivisor();
        this.heat = spec.heatPerFlux();
        this.yield = spec.totalYield();
        this.meltingPoint = spec.meltingPoint();
        this.diffusion = spec.diffusion();
        this.nType = fromModern(spec.inputType());
        this.rType = fromModern(spec.outputType());
        this.colorTint = spec.colorTint();
        this.heatCoeffStart = spec.heatCoefficientStart();
        this.heatCoeffLength = spec.heatCoefficientLength();
        this.ratioCurve = spec.outputRatioCurve();
        this.fluxCurve = spec.inputFluxCurve();
        this.specialFluxCurve = ratioCurve != null || fluxCurve != null;
    }

    private RBMKFuelRodSpec specFromLegacyFields() {
        return new RBMKFuelRodSpec(
                reactivity,
                selfRate,
                function.toModern(),
                depFunc.toModern(),
                xGen,
                xBurn,
                heat,
                yield,
                meltingPoint,
                diffusion,
                toModern(nType),
                toModern(rType),
                colorTint,
                heatCoeffStart,
                heatCoeffLength,
                ratioCurve,
                fluxCurve);
    }

    private RBMKFuelRodRegistry.Entry plannerEntry() {
        return new RBMKFuelRodRegistry.Entry(
                legacyRodId,
                legacyPelletId,
                fullName,
                craftable,
                pelletXenonOverlay,
                getSpec());
    }

    private static RBMKFuelRodRegistry.Entry entryByPellet(ItemRBMKPellet pellet) {
        return RBMKFuelRodRegistry.all().stream()
                .filter(entry -> entry.legacyPelletId().equals(pellet.getLegacyPelletId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown RBMK pellet: " + pellet.getLegacyPelletId()));
    }

    private static RBMKFuelRodRegistry.Entry entryByFullName(String fullName) {
        return RBMKFuelRodRegistry.all().stream()
                .filter(entry -> entry.fullName().equals(fullName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown RBMK fuel rod full name: " + fullName));
    }

    private static NType fromModern(RBMKFluxReceiver.NType type) {
        return NType.values()[type.ordinal()];
    }

    private static RBMKFluxReceiver.NType toModern(NType type) {
        return RBMKFluxReceiver.NType.values()[(type == null ? NType.SLOW : type).ordinal()];
    }

    private static MutableComponent component(RBMKItemPlanner.TooltipLine line) {
        if (!line.literal().isEmpty()) {
            return Component.literal(line.literal());
        }
        if (!line.argumentTranslationKey().isEmpty()) {
            return Component.translatable(line.translationKey(), Component.translatable(line.argumentTranslationKey()));
        }
        if (!line.argument().isEmpty()) {
            return Component.translatable(line.translationKey(), line.argument());
        }
        return Component.translatable(line.translationKey());
    }

    private static MutableComponent applyStyle(MutableComponent component, RBMKItemPlanner.TooltipStyle style) {
        return switch (style) {
            case ITALIC -> component.withStyle(ChatFormatting.ITALIC);
            case DARK_GRAY_ITALIC -> component.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
            case GOLD -> component.withStyle(ChatFormatting.GOLD);
            case RED -> component.withStyle(ChatFormatting.RED);
            case GREEN -> component.withStyle(ChatFormatting.GREEN);
            case DARK_PURPLE -> component.withStyle(ChatFormatting.DARK_PURPLE);
            case BLUE -> component.withStyle(ChatFormatting.BLUE);
            case YELLOW -> component.withStyle(ChatFormatting.YELLOW);
            case DARK_RED -> component.withStyle(ChatFormatting.DARK_RED);
            case DARK_GREEN -> component.withStyle(ChatFormatting.DARK_GREEN);
            case DARK_GRAY -> component.withStyle(ChatFormatting.DARK_GRAY);
        };
    }
}
