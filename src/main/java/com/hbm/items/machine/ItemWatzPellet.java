package com.hbm.items.machine;

import com.hbm.ntm.item.WatzPelletItem;
import com.hbm.util.function.Function;
import com.hbm.util.function.Function.FunctionLinear;
import com.hbm.util.function.Function.FunctionQuadratic;
import com.hbm.util.function.Function.FunctionSqrt;
import com.hbm.util.function.Function.FunctionSqrtFalling;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy 1.7.10 package bridge for Watz pellet constants and NBT helpers.
 *
 * <p>The modern runtime splits the old metadata item into per-type items. This
 * facade intentionally does not register or instantiate a second item family.
 */
@Deprecated(forRemoval = false)
public final class ItemWatzPellet {
    private static final String LEGACY_YIELD_KEY = "yield";

    private ItemWatzPellet() {
    }

    public enum EnumWatzType {
        SCHRABIDIUM(0x32FFFF, 0x005C5C, 2_000.0D, 20.0D, 0.01D,
                new FunctionLinear(1.5D), new FunctionSqrtFalling(10.0D), null),
        HES(0x66DCD6, 0x023933, 1_750.0D, 20.0D, 0.005D,
                new FunctionLinear(1.25D), new FunctionSqrtFalling(15.0D), null),
        MES(0xCBEADF, 0x28473C, 1_500.0D, 15.0D, 0.0025D,
                new FunctionLinear(1.15D), new FunctionSqrtFalling(15.0D), null),
        LES(0xABB4A8, 0x0C1105, 1_250.0D, 15.0D, 0.00125D,
                new FunctionLinear(1.0D), new FunctionSqrtFalling(20.0D), null),
        HEN(0xA6B2A6, 0x030F03, 0.0D, 10.0D, 0.0005D,
                new FunctionSqrt(100.0D), new FunctionSqrtFalling(10.0D), null),
        MEU(0xC1C7BD, 0x2B3227, 0.0D, 10.0D, 0.0005D,
                new FunctionSqrt(75.0D), new FunctionSqrtFalling(10.0D), null),
        MEP(0x9AA3A0, 0x111A17, 0.0D, 15.0D, 0.0005D,
                new FunctionSqrt(150.0D), new FunctionSqrtFalling(10.0D), null),
        LEAD(0xA6A6B2, 0x03030F, 0.0D, 0.0D, 0.0025D,
                null, null, new FunctionSqrt(10.0D)),
        BORON(0xBDC8D2, 0x29343E, 0.0D, 0.0D, 0.0025D,
                null, null, new FunctionLinear(10.0D)),
        DU(0xC1C7BD, 0x2B3227, 0.0D, 0.0D, 0.0025D,
                null, null, new FunctionQuadratic(1.0D, 1.0D).withDiv(100.0D)),
        NQD(0x4B4B4B, 0x121212, 2_000.0D, 20.0D, 0.01D,
                new FunctionLinear(2.0D), new FunctionSqrt(1.0D / 25.0D).withOff(25.0D * 25.0D), null),
        NQR(0x2D2D2D, 0x0B0B0B, 2_500.0D, 30.0D, 0.01D,
                new FunctionLinear(1.5D), new FunctionSqrt(1.0D / 25.0D).withOff(25.0D * 25.0D), null);

        public double yield = 500_000_000.0D;
        public int colorLight;
        public int colorDark;
        public double mudContent;
        public double passive;
        public double heatEmission;
        public Function burnFunc;
        public Function heatDiv;
        public Function absorbFunc;

        private EnumWatzType(int colorLight, int colorDark, double passive, double heatEmission, double mudContent,
                Function burnFunction, Function heatDivisor, Function absorbFunction) {
            this.colorLight = colorLight;
            this.colorDark = colorDark;
            this.passive = passive;
            this.heatEmission = heatEmission;
            this.mudContent = mudContent / 2.0D;
            this.burnFunc = burnFunction;
            this.heatDiv = heatDivisor;
            this.absorbFunc = absorbFunction;
        }
    }

    public static int desaturate(int color) {
        int r = (color & 0xff0000) >> 16;
        int g = (color & 0x00ff00) >> 8;
        int b = (color & 0x0000ff);

        int avg = (r + g + b) / 3;
        double approach = 0.9D;
        double mult = 0.75D;

        r -= (r - avg) * approach;
        g -= (g - avg) * approach;
        b -= (b - avg) * approach;

        r *= mult;
        g *= mult;
        b *= mult;

        return (r << 16) | (g << 8) | b;
    }

    public static double getEnrichment(ItemStack stack) {
        EnumWatzType num = typeFromStack(stack);
        return getYield(stack) / num.yield;
    }

    public static double getYield(ItemStack stack) {
        return getDouble(stack, LEGACY_YIELD_KEY);
    }

    public static void setYield(ItemStack stack, double yield) {
        setDouble(stack, LEGACY_YIELD_KEY, yield);
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
        stack.getOrCreateTag().putDouble(LEGACY_YIELD_KEY, typeFromStack(stack).yield);
    }

    private static EnumWatzType typeFromStack(ItemStack stack) {
        if (stack.getItem() instanceof WatzPelletItem pellet) {
            int ordinal = pellet.type().ordinal();
            if (ordinal >= 0 && ordinal < EnumWatzType.values().length) {
                return EnumWatzType.values()[ordinal];
            }
        }
        return EnumWatzType.SCHRABIDIUM;
    }
}
