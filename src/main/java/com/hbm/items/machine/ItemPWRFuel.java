package com.hbm.items.machine;

import com.hbm.ntm.item.PWRFuelItem;
import com.hbm.ntm.recipe.PWRFuelRuntime;
import com.hbm.util.EnumUtil;
import com.hbm.util.function.Function;
import com.hbm.util.function.Function.FunctionLogarithmic;
import com.hbm.util.function.Function.FunctionSqrt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy 1.7.10 package bridge for active PWR fuel items.
 */
@Deprecated(forRemoval = false)
public class ItemPWRFuel extends PWRFuelItem {
    private final EnumPWRFuel legacyType;

    public ItemPWRFuel() {
        this(new Item.Properties(), EnumPWRFuel.MEU);
    }

    public ItemPWRFuel(Item.Properties properties, PWRFuelRuntime.Type type) {
        this(properties, EnumPWRFuel.fromModern(type));
    }

    public ItemPWRFuel(Item.Properties properties, EnumPWRFuel type) {
        super(properties, type.toModern());
        this.legacyType = type;
    }

    public EnumPWRFuel legacyType() {
        return legacyType;
    }

    public static EnumPWRFuel typeFor(ItemStack stack) {
        return PWRFuelRuntime.typeFor(stack)
                .map(EnumPWRFuel::fromModern)
                .orElse(EnumPWRFuel.byMeta(stack.getDamageValue()));
    }

    public enum EnumPWRFuel {
        MEU(5.0D, new FunctionLogarithmic(20.0D * 30.0D).withDiv(2_500.0D)),
        HEU233(7.5D, new FunctionSqrt(25.0D)),
        HEU235(7.5D, new FunctionSqrt(22.5D)),
        MEN(7.5D, new FunctionLogarithmic(22.5D * 30.0D).withDiv(2_500.0D)),
        HEN237(7.5D, new FunctionSqrt(27.5D)),
        MOX(7.5D, new FunctionLogarithmic(20.0D * 30.0D).withDiv(2_500.0D)),
        MEP(7.5D, new FunctionLogarithmic(22.5D * 30.0D).withDiv(2_500.0D)),
        HEP239(10.0D, new FunctionSqrt(22.5D)),
        HEP241(10.0D, new FunctionSqrt(25.0D)),
        MEA(7.5D, new FunctionLogarithmic(25.0D * 30.0D).withDiv(2_500.0D)),
        HEA242(10.0D, new FunctionSqrt(25.0D)),
        HES326(12.5D, new FunctionSqrt(27.5D)),
        HES327(12.5D, new FunctionSqrt(30.0D)),
        BFB_AM_MIX(2.5D, new FunctionSqrt(15.0D), 250_000_000.0D),
        BFB_PU241(2.5D, new FunctionSqrt(15.0D), 250_000_000.0D);

        public double yield = PWRFuelRuntime.DEFAULT_YIELD;
        public double heatEmission;
        public Function function;

        EnumPWRFuel(double heatEmission, Function function, double ignoredYield) {
            this.heatEmission = heatEmission;
            this.function = function;
        }

        EnumPWRFuel(double heatEmission, Function function) {
            this(heatEmission, function, PWRFuelRuntime.DEFAULT_YIELD);
        }

        public PWRFuelRuntime.Type toModern() {
            return PWRFuelRuntime.Type.values()[ordinal()];
        }

        public static EnumPWRFuel fromModern(PWRFuelRuntime.Type type) {
            return values()[type.ordinal()];
        }

        public static EnumPWRFuel byMeta(int meta) {
            return EnumUtil.grabEnumSafely(EnumPWRFuel.class, meta);
        }
    }
}
