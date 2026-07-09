package com.hbm.util.function;

import com.hbm.ntm.util.HbmMathUtil;
import java.util.Locale;
import net.minecraft.ChatFormatting;

/**
 * Legacy 1.7.10 package bridge for fuel response functions.
 */
@Deprecated(forRemoval = false)
public abstract class Function extends com.hbm.ntm.util.function.Function {
    @Override
    public Function withDiv(double div) {
        super.withDiv(div);
        return this;
    }

    @Override
    public Function withOff(double off) {
        super.withOff(off);
        return this;
    }

    public static abstract class FunctionSingleArg extends Function {
        protected double level;

        public FunctionSingleArg(double level) {
            this.level = level;
        }
    }

    public static abstract class FunctionDoubleArg extends Function {
        protected double level;
        protected double vOff;

        public FunctionDoubleArg(double level, double vOff) {
            this.level = level;
            this.vOff = vOff;
        }
    }

    public static class FunctionLogarithmic extends FunctionSingleArg {
        public FunctionLogarithmic(double level) {
            super(level);
            withOff(1.0D);
        }

        @Override
        public double effonix(double x) {
            return Math.log10(getX(x)) * level;
        }

        @Override
        public String getLabelForFuel() {
            return "log10(" + getXName(false) + ") * " + String.format(Locale.US, "%,.1f", level);
        }

        @Override
        public String getDangerFromFuel() {
            return ChatFormatting.YELLOW + "MEDIUM / LOGARITHMIC";
        }
    }

    public static class FunctionPassive extends FunctionSingleArg {
        public FunctionPassive(double level) {
            super(level);
        }

        @Override
        public double effonix(double x) {
            return level;
        }

        @Override
        public String getLabelForFuel() {
            return "" + String.format(Locale.US, "%,.1f", level);
        }

        @Override
        public String getDangerFromFuel() {
            return ChatFormatting.DARK_GREEN + "SAFE / PASSIVE";
        }
    }

    public static class FunctionSqrt extends FunctionSingleArg {
        public FunctionSqrt(double level) {
            super(level);
        }

        @Override
        public double effonix(double x) {
            return HbmMathUtil.squirt(getX(x)) * level;
        }

        @Override
        public String getLabelForFuel() {
            return "sqrt(" + getXName(false) + ") * " + String.format(Locale.US, "%,.3f", level);
        }

        @Override
        public String getDangerFromFuel() {
            return ChatFormatting.YELLOW + "MEDIUM / SQUARE ROOT";
        }
    }

    public static class FunctionSqrtFalling extends FunctionSqrt {
        public FunctionSqrtFalling(double fallFactor) {
            super(1.0D / fallFactor);
            withOff(fallFactor * fallFactor);
        }
    }

    public static class FunctionLinear extends FunctionSingleArg {
        public FunctionLinear(double level) {
            super(level);
        }

        @Override
        public double effonix(double x) {
            return getX(x) * level;
        }

        @Override
        public String getLabelForFuel() {
            return getXName(true) + " * " + String.format(Locale.US, "%,.1f", level);
        }

        @Override
        public String getDangerFromFuel() {
            return ChatFormatting.RED + "DANGEROUS / LINEAR";
        }
    }

    public static class FunctionQuadratic extends FunctionDoubleArg {
        public FunctionQuadratic(double level) {
            super(level, 0.0D);
        }

        public FunctionQuadratic(double level, double vOff) {
            super(level, vOff);
        }

        @Override
        public double effonix(double x) {
            return getX(x) * getX(x) * level + vOff;
        }

        @Override
        public String getLabelForFuel() {
            return getXName(true) + "^2 * " + String.format(Locale.US, "%,.1f", level)
                    + (vOff != 0.0D ? " + " + String.format(Locale.US, "%,.1f", vOff) : "");
        }

        @Override
        public String getDangerFromFuel() {
            return ChatFormatting.RED + "DANGEROUS / QUADRATIC";
        }
    }
}
