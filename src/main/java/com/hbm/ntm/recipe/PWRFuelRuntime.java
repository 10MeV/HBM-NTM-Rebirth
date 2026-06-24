package com.hbm.ntm.recipe;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmMathUtil;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class PWRFuelRuntime {
    public static final long DEFAULT_YIELD = 1_000_000_000L;

    private PWRFuelRuntime() {
    }

    public static boolean isFuel(ItemStack stack) {
        return indexOf(ModItems.PWR_FUEL_ITEMS, stack.getItem()) >= 0;
    }

    public static int fuelIndex(ItemStack stack) {
        return indexOf(ModItems.PWR_FUEL_ITEMS, stack.getItem());
    }

    public static ItemStack hotProduct(int index) {
        return stackFrom(ModItems.PWR_FUEL_HOT_ITEMS, index);
    }

    public static ItemStack depletedProduct(int index) {
        return stackFrom(ModItems.PWR_FUEL_DEPLETED_ITEMS, index);
    }

    public static Type type(int index) {
        if (index < 0 || index >= Type.values().length) {
            return Type.MEU;
        }
        return Type.values()[index];
    }

    public static Optional<Type> typeFor(ItemStack stack) {
        int index = fuelIndex(stack);
        return index < 0 ? Optional.empty() : Optional.of(type(index));
    }

    public static List<DisplayFuel> displayFuels() {
        return ModItems.PWR_FUEL_ITEMS.stream()
                .map(item -> {
                    ItemStack input = new ItemStack(item.get());
                    int index = fuelIndex(input);
                    return new DisplayFuel(input, hotProduct(index), depletedProduct(index), type(index));
                })
                .toList();
    }

    private static int indexOf(List<? extends net.minecraftforge.registries.RegistryObject<Item>> items, Item item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).get() == item) {
                return i;
            }
        }
        return -1;
    }

    private static ItemStack stackFrom(List<? extends net.minecraftforge.registries.RegistryObject<Item>> items, int index) {
        if (index < 0 || index >= items.size()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(items.get(index).get());
    }

    public enum Type {
        MEU("meu", 5.0D, Curve.logarithmic(20.0D * 30.0D).withDiv(2_500.0D), DEFAULT_YIELD),
        HEU233("heu233", 7.5D, Curve.sqrt(25.0D), DEFAULT_YIELD),
        HEU235("heu235", 7.5D, Curve.sqrt(22.5D), DEFAULT_YIELD),
        MEN("men", 7.5D, Curve.logarithmic(22.5D * 30.0D).withDiv(2_500.0D), DEFAULT_YIELD),
        HEN237("hen237", 7.5D, Curve.sqrt(27.5D), DEFAULT_YIELD),
        MOX("mox", 7.5D, Curve.logarithmic(20.0D * 30.0D).withDiv(2_500.0D), DEFAULT_YIELD),
        MEP("mep", 7.5D, Curve.logarithmic(22.5D * 30.0D).withDiv(2_500.0D), DEFAULT_YIELD),
        HEP239("hep239", 10.0D, Curve.sqrt(22.5D), DEFAULT_YIELD),
        HEP241("hep241", 10.0D, Curve.sqrt(25.0D), DEFAULT_YIELD),
        MEA("mea", 7.5D, Curve.logarithmic(25.0D * 30.0D).withDiv(2_500.0D), DEFAULT_YIELD),
        HEA242("hea242", 10.0D, Curve.sqrt(25.0D), DEFAULT_YIELD),
        HES326("hes326", 12.5D, Curve.sqrt(27.5D), DEFAULT_YIELD),
        HES327("hes327", 12.5D, Curve.sqrt(30.0D), DEFAULT_YIELD),
        BFB_AM_MIX("bfb_am_mix", 2.5D, Curve.sqrt(15.0D), DEFAULT_YIELD),
        BFB_PU241("bfb_pu241", 2.5D, Curve.sqrt(15.0D), DEFAULT_YIELD);

        private final String suffix;
        private final double heatEmission;
        private final Curve curve;
        private final long yield;

        Type(String suffix, double heatEmission, Curve curve, long yield) {
            this.suffix = suffix;
            this.heatEmission = heatEmission;
            this.curve = curve;
            this.yield = yield;
        }

        public String suffix() {
            return suffix;
        }

        public double heatEmission() {
            return heatEmission;
        }

        public Curve curve() {
            return curve;
        }

        public long yield() {
            return yield;
        }

        public String displayName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public record DisplayFuel(ItemStack input, ItemStack hot, ItemStack depleted, Type type) {
    }

    public static final class Curve {
        private final Kind kind;
        private final double level;
        private final double div;
        private final double off;

        private Curve(Kind kind, double level, double div, double off) {
            this.kind = kind;
            this.level = level;
            this.div = div == 0.0D ? 1.0D : div;
            this.off = off;
        }

        public static Curve sqrt(double level) {
            return new Curve(Kind.SQRT, level, 1.0D, 0.0D);
        }

        public static Curve logarithmic(double level) {
            return new Curve(Kind.LOGARITHMIC, level, 1.0D, 1.0D);
        }

        public Curve withDiv(double div) {
            return new Curve(kind, level, div, off);
        }

        public double eval(double flux) {
            double x = flux / div + off;
            return switch (kind) {
                case SQRT -> HbmMathUtil.squirt(x) * level;
                case LOGARITHMIC -> Math.log10(x) * level;
            };
        }

        public String fuelLabel() {
            return switch (kind) {
                case SQRT -> "sqrt(" + xName(false) + ") * " + String.format(Locale.US, "%,.3f", level);
                case LOGARITHMIC -> "log10(" + xName(false) + ") * " + String.format(Locale.US, "%,.1f", level);
            };
        }

        public String dangerLabel() {
            return switch (kind) {
                case SQRT -> "MEDIUM / SQUARE ROOT";
                case LOGARITHMIC -> "MEDIUM / LOGARITHMIC";
            };
        }

        private String xName(boolean brackets) {
            String x = "x";
            boolean modified = false;
            if (div != 1.0D) {
                x += " / " + String.format(Locale.US, "%,.1f", div);
                modified = true;
            }
            if (off != 0.0D) {
                x += " + " + String.format(Locale.US, "%,.1f", off);
                modified = true;
            }
            return modified && brackets ? "(" + x + ")" : x;
        }

        private enum Kind {
            SQRT,
            LOGARITHMIC
        }
    }
}
