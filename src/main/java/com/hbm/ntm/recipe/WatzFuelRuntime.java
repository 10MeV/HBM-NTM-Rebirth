package com.hbm.ntm.recipe;

import com.hbm.ntm.item.WatzPelletItem;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmMathUtil;
import java.util.List;
import java.util.Locale;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class WatzFuelRuntime {
    public static final double INITIAL_YIELD = 500_000_000.0D;

    private WatzFuelRuntime() {
    }

    public static boolean isPellet(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof WatzPelletItem pellet && !pellet.depleted();
    }

    public static boolean isDepletedPellet(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof WatzPelletItem pellet && pellet.depleted();
    }

    public static boolean isAnyWatzPellet(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof WatzPelletItem;
    }

    public static Type type(ItemStack stack) {
        return stack.getItem() instanceof WatzPelletItem pellet ? pellet.type() : Type.SCHRABIDIUM;
    }

    public static ItemStack depletedProduct(ItemStack stack) {
        Type type = type(stack);
        int index = type.ordinal();
        if (index < 0 || index >= ModItems.WATZ_PELLET_DEPLETED_ITEMS.size()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(ModItems.WATZ_PELLET_DEPLETED_ITEMS.get(index).get());
    }

    public static List<DisplayPellet> displayPellets() {
        return ModItems.WATZ_PELLET_ITEMS.stream()
                .map(item -> {
                    ItemStack input = new ItemStack(item.get());
                    return new DisplayPellet(input, depletedProduct(input), type(input));
                })
                .toList();
    }

    public static Type typeForItem(Item item) {
        if (item instanceof WatzPelletItem pellet) {
            return pellet.type();
        }
        return Type.SCHRABIDIUM;
    }

    public enum Type {
        SCHRABIDIUM("schrabidium", 0x32FFFF, 0x005C5C, 2_000.0D, 20.0D, 0.01D, Curve.linear(1.5D), Curve.sqrtFalling(10.0D), null),
        HES("hes", 0x66DCD6, 0x023933, 1_750.0D, 20.0D, 0.005D, Curve.linear(1.25D), Curve.sqrtFalling(15.0D), null),
        MES("mes", 0xCBEADF, 0x28473C, 1_500.0D, 15.0D, 0.0025D, Curve.linear(1.15D), Curve.sqrtFalling(15.0D), null),
        LES("les", 0xABB4A8, 0x0C1105, 1_250.0D, 15.0D, 0.00125D, Curve.linear(1.0D), Curve.sqrtFalling(20.0D), null),
        HEN("hen", 0xA6B2A6, 0x030F03, 0.0D, 10.0D, 0.0005D, Curve.sqrt(100.0D), Curve.sqrtFalling(10.0D), null),
        MEU("meu", 0xC1C7BD, 0x2B3227, 0.0D, 10.0D, 0.0005D, Curve.sqrt(75.0D), Curve.sqrtFalling(10.0D), null),
        MEP("mep", 0x9AA3A0, 0x111A17, 0.0D, 15.0D, 0.0005D, Curve.sqrt(150.0D), Curve.sqrtFalling(10.0D), null),
        LEAD("lead", 0xA6A6B2, 0x03030F, 0.0D, 0.0D, 0.0025D, null, null, Curve.sqrt(10.0D)),
        BORON("boron", 0xBDC8D2, 0x29343E, 0.0D, 0.0D, 0.0025D, null, null, Curve.linear(10.0D)),
        DU("du", 0xC1C7BD, 0x2B3227, 0.0D, 0.0D, 0.0025D, null, null, Curve.quadratic(1.0D, 1.0D).withDiv(100.0D)),
        NQD("nqd", 0x4B4B4B, 0x121212, 2_000.0D, 20.0D, 0.01D, Curve.linear(2.0D), Curve.sqrt(1.0D / 25.0D).withOff(625.0D), null),
        NQR("nqr", 0x2D2D2D, 0x0B0B0B, 2_500.0D, 30.0D, 0.01D, Curve.linear(1.5D), Curve.sqrt(1.0D / 25.0D).withOff(625.0D), null);

        private final String suffix;
        private final int colorLight;
        private final int colorDark;
        private final double passive;
        private final double heatEmission;
        private final double mudContent;
        private final Curve burnFunc;
        private final Curve heatDiv;
        private final Curve absorbFunc;

        Type(String suffix, int colorLight, int colorDark, double passive, double heatEmission, double mudContent,
                Curve burnFunc, Curve heatDiv, Curve absorbFunc) {
            this.suffix = suffix;
            this.colorLight = colorLight;
            this.colorDark = colorDark;
            this.passive = passive;
            this.heatEmission = heatEmission;
            this.mudContent = mudContent / 2.0D;
            this.burnFunc = burnFunc;
            this.heatDiv = heatDiv;
            this.absorbFunc = absorbFunc;
        }

        public String suffix() {
            return suffix;
        }

        public int colorLight() {
            return colorLight;
        }

        public int colorDark() {
            return colorDark;
        }

        public double passive() {
            return passive;
        }

        public double heatEmission() {
            return heatEmission;
        }

        public double mudContent() {
            return mudContent;
        }

        public Curve burnFunc() {
            return burnFunc;
        }

        public Curve heatDiv() {
            return heatDiv;
        }

        public Curve absorbFunc() {
            return absorbFunc;
        }

        public String displayName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public record DisplayPellet(ItemStack input, ItemStack depleted, Type type) {
    }

    public static final class Curve {
        private final Kind kind;
        private final double a;
        private final double b;
        private final double div;
        private final double off;

        private Curve(Kind kind, double a, double b, double div, double off) {
            this.kind = kind;
            this.a = a;
            this.b = b;
            this.div = div;
            this.off = off;
        }

        public static Curve linear(double a) {
            return new Curve(Kind.LINEAR, a, 0.0D, 1.0D, 0.0D);
        }

        public static Curve sqrt(double a) {
            return new Curve(Kind.SQRT, a, 0.0D, 1.0D, 0.0D);
        }

        public static Curve sqrtFalling(double a) {
            return new Curve(Kind.SQRT_FALLING, 1.0D / a, 0.0D, 1.0D, a * a);
        }

        public static Curve quadratic(double a, double b) {
            return new Curve(Kind.QUADRATIC, a, b, 1.0D, 0.0D);
        }

        public Curve withDiv(double div) {
            return new Curve(kind, a, b, div, off);
        }

        public Curve withOff(double off) {
            return new Curve(kind, a, b, div, off);
        }

        public double eval(double x) {
            double shifted = x / div + off;
            return switch (kind) {
                case LINEAR -> shifted * a;
                case SQRT, SQRT_FALLING -> HbmMathUtil.squirt(shifted) * a;
                case QUADRATIC -> shifted * shifted * a + b;
            };
        }

        public String fuelLabel() {
            String x = xName();
            return switch (kind) {
                case LINEAR -> x + " * " + format(a, 1);
                case SQRT, SQRT_FALLING -> "sqrt(" + x + ") * " + format(a, 3);
                case QUADRATIC -> x + "\u00b2 * " + format(a, 1) + (b != 0.0D ? " + " + format(b, 1) : "");
            };
        }

        public String dangerLabel() {
            return switch (kind) {
                case LINEAR -> "DANGEROUS / LINEAR";
                case SQRT, SQRT_FALLING -> "MEDIUM / SQUARE ROOT";
                case QUADRATIC -> "DANGEROUS / QUADRATIC";
            };
        }

        public boolean dangerous() {
            return kind == Kind.LINEAR || kind == Kind.QUADRATIC;
        }

        private String xName() {
            String x = "x";
            if (div != 1.0D) {
                x += " / " + format(div, 1);
            }
            if (off != 0.0D) {
                x += " + " + format(off, 1);
            }
            return x;
        }

        private static String format(double value, int decimals) {
            return String.format(Locale.US, "%,." + decimals + "f", value);
        }

        private enum Kind {
            LINEAR,
            SQRT,
            SQRT_FALLING,
            QUADRATIC
        }
    }
}
