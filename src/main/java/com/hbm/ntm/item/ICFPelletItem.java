package com.hbm.ntm.item;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmTextUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ICFPelletItem extends Item {
    private static final String TAG_DEPLETION = "depletion";
    private static final String TAG_TYPE_1 = "type1";
    private static final String TAG_TYPE_2 = "type2";
    private static final String TAG_MUON = "muon";
    private static final long BASE_MAX_DEPLETION = 50_000_000_000L;
    private static final long BASE_FUSING_DIFFICULTY = 10_000_000L;

    private static final Map<FluidType, FuelType> FLUID_FUELS = new HashMap<>();
    private static final Map<NTMMaterial, FuelType> MATERIAL_FUELS = new HashMap<>();

    public ICFPelletItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static void addCreativeStacks(CreativeModeTab.Output output) {
        output.accept(setup(FuelType.DEUTERIUM, FuelType.TRITIUM, false));
        output.accept(setup(FuelType.HELIUM3, FuelType.HELIUM4, false));
        output.accept(setup(FuelType.LITHIUM, FuelType.OXYGEN, false));
        output.accept(setup(FuelType.SODIUM, FuelType.CHLORINE, true));
        output.accept(setup(FuelType.BERYLLIUM, FuelType.CALCIUM, true));
    }

    public static ItemStack setup(FuelType first, FuelType second, boolean muon) {
        ItemStack stack = new ItemStack(ModItems.ICF_PELLET.get());
        setup(stack, first, second, muon);
        return stack;
    }

    public static void setup(ItemStack stack, FuelType first, FuelType second, boolean muon) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putByte(TAG_TYPE_1, (byte) first.ordinal());
        tag.putByte(TAG_TYPE_2, (byte) second.ordinal());
        tag.putBoolean(TAG_MUON, muon);
    }

    public static FuelType type(ItemStack stack, boolean first) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return first ? FuelType.DEUTERIUM : FuelType.TRITIUM;
        }
        int ordinal = stack.getTag().getByte(first ? TAG_TYPE_1 : TAG_TYPE_2);
        FuelType[] values = FuelType.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : first ? FuelType.DEUTERIUM : FuelType.TRITIUM;
    }

    public static long getMaxDepletion(ItemStack stack) {
        long base = BASE_MAX_DEPLETION;
        base /= type(stack, true).depletionSpeed;
        base /= type(stack, false).depletionSpeed;
        return base;
    }

    public static long getFusingDifficulty(ItemStack stack) {
        long base = BASE_FUSING_DIFFICULTY;
        base *= type(stack, true).fusingDifficulty * type(stack, false).fusingDifficulty;
        return stack.hasTag() && stack.getTag().getBoolean(TAG_MUON) ? base / 4L : base;
    }

    public static long getDepletion(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getLong(TAG_DEPLETION) : 0L;
    }

    public static long react(ItemStack stack, long laserPower) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong(TAG_DEPLETION, tag.getLong(TAG_DEPLETION) + laserPower);
        return (long) (laserPower * type(stack, true).reactionMultiplier * type(stack, false).reactionMultiplier);
    }

    public static boolean isMuonCatalyzed(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(TAG_MUON);
    }

    public static FuelType fuelFromFluid(FluidType type) {
        initMaps();
        return FLUID_FUELS.get(type);
    }

    public static FuelType fuelFromItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        initMaps();
        List<MaterialStack> materials = Mats.getMaterialsFromItem(stack);
        if (materials == null || materials.size() != 1) {
            return null;
        }
        MaterialStack material = materials.get(0);
        if (material.amount != MaterialShapes.INGOT.q(1)) {
            return null;
        }
        return MATERIAL_FUELS.get(material.material);
    }

    public static int tint(ItemStack stack, int tintIndex) {
        if (tintIndex != 0) {
            return 0xFFFFFF;
        }
        int a = type(stack, true).color;
        int b = type(stack, false).color;
        int r = (((a & 0xFF0000) >> 16) + ((b & 0xFF0000) >> 16)) / 2;
        int g = (((a & 0x00FF00) >> 8) + ((b & 0x00FF00) >> 8)) / 2;
        int blue = ((a & 0x0000FF) + (b & 0x0000FF)) / 2;
        return r << 16 | g << 8 | blue;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getDepletion(stack) > 0L;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - Math.min(13.0F, 13.0F * getDepletion(stack) / (float) getMaxDepletion(stack)));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x55FF55;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        double depleted = getMaxDepletion(stack) <= 0L ? 0.0D : getDepletion(stack) * 100.0D / getMaxDepletion(stack);
        tooltip.add(Component.literal(String.format(Locale.US, "Depletion: %.1f%%", depleted)).withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("Fuel: ")
                .append(Component.translatable(fuelTranslationKey(type(stack, true))))
                .append(Component.literal(" / "))
                .append(Component.translatable(fuelTranslationKey(type(stack, false))))
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Heat required: " + HbmTextUtil.shortNumber(getFusingDifficulty(stack)) + "TU").withStyle(ChatFormatting.YELLOW));
        double multiplier = type(stack, true).reactionMultiplier * type(stack, false).reactionMultiplier;
        tooltip.add(Component.literal("Reactivity multiplier: x" + ((int) (multiplier * 100.0D)) / 100.0D).withStyle(ChatFormatting.YELLOW));
        if (isMuonCatalyzed(stack)) {
            tooltip.add(Component.literal("Muon catalyzed!").withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    private static String fuelTranslationKey(FuelType type) {
        return "icffuel." + type.name().toLowerCase(Locale.US);
    }

    private static void initMaps() {
        if (!FLUID_FUELS.isEmpty() && !MATERIAL_FUELS.isEmpty()) {
            return;
        }
        FLUID_FUELS.put(HbmFluids.HYDROGEN, FuelType.HYDROGEN);
        FLUID_FUELS.put(HbmFluids.DEUTERIUM, FuelType.DEUTERIUM);
        FLUID_FUELS.put(HbmFluids.TRITIUM, FuelType.TRITIUM);
        FLUID_FUELS.put(HbmFluids.HELIUM3, FuelType.HELIUM3);
        FLUID_FUELS.put(HbmFluids.HELIUM4, FuelType.HELIUM4);
        FLUID_FUELS.put(HbmFluids.OXYGEN, FuelType.OXYGEN);
        FLUID_FUELS.put(HbmFluids.CHLORINE, FuelType.CHLORINE);
        MATERIAL_FUELS.put(Mats.MAT_LITHIUM, FuelType.LITHIUM);
        MATERIAL_FUELS.put(Mats.MAT_BERYLLIUM, FuelType.BERYLLIUM);
        MATERIAL_FUELS.put(Mats.MAT_BORON, FuelType.BORON);
        MATERIAL_FUELS.put(Mats.MAT_GRAPHITE, FuelType.CARBON);
        MATERIAL_FUELS.put(Mats.MAT_SODIUM, FuelType.SODIUM);
        MATERIAL_FUELS.put(Mats.MAT_CALCIUM, FuelType.CALCIUM);
    }

    public enum FuelType {
        HYDROGEN(0x4040FF, 1.00D, 0.85D, 1.00D, "Hydrogen"),
        DEUTERIUM(0x2828CB, 1.25D, 1.00D, 1.00D, "Deuterium"),
        TRITIUM(0x000092, 1.50D, 1.00D, 1.05D, "Tritium"),
        HELIUM3(0xFFF09F, 1.75D, 1.00D, 1.25D, "Helium-3"),
        HELIUM4(0xFF9B60, 2.00D, 1.00D, 1.50D, "Helium-4"),
        LITHIUM(0xE9E9E9, 1.25D, 0.85D, 2.00D, "Lithium"),
        BERYLLIUM(0xA79D80, 2.00D, 1.00D, 2.50D, "Beryllium"),
        BORON(0x697F89, 3.00D, 0.50D, 3.50D, "Boron"),
        CARBON(0x454545, 2.00D, 1.00D, 5.00D, "Carbon"),
        OXYGEN(0xB4E2FF, 1.25D, 1.50D, 7.50D, "Oxygen"),
        SODIUM(0xDFE4E7, 3.00D, 0.75D, 8.75D, "Sodium"),
        CHLORINE(0xDAE598, 2.50D, 1.00D, 9.25D, "Chlorine"),
        CALCIUM(0xD2C7A9, 3.00D, 1.00D, 9.75D, "Calcium");

        private final int color;
        private final double reactionMultiplier;
        private final double depletionSpeed;
        private final double fusingDifficulty;
        private final String display;

        FuelType(int color, double reactionMultiplier, double depletionSpeed, double fusingDifficulty,
                String display) {
            this.color = color;
            this.reactionMultiplier = reactionMultiplier;
            this.depletionSpeed = depletionSpeed;
            this.fusingDifficulty = fusingDifficulty;
            this.display = display;
        }
    }
}
