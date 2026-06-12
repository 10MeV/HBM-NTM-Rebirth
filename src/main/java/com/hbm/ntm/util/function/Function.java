package com.hbm.ntm.util.function;

import com.hbm.ntm.util.HbmMathUtil;
import net.minecraft.ChatFormatting;

import java.util.Locale;

/**
 * Legacy-name fuel response function family.
 */
@Deprecated(forRemoval = false)
public abstract class Function {
    protected double div = 1.0D;
    protected double off = 0.0D;

    public abstract double effonix(double x);

    public abstract String getLabelForFuel();

    public abstract String getDangerFromFuel();

    public Function withDiv(double div) {
        this.div = div;
        return this;
    }

    public Function withOff(double off) {
        this.off = off;
        return this;
    }

    public double getX(double x) {
        return x / div + off;
    }

    public String getXName() {
        return getXName(true);
    }

    public String getXName(boolean brackets) {
        String x = "x";
        boolean mod = false;
        if (div != 1.0D) {
            x += " / " + String.format(Locale.US, "%,.1f", div);
        }
        if (off != 0.0D) {
            x += " + " + String.format(Locale.US, "%,.1f", off);
        }
        if (mod && brackets) {
            x = "(" + x + ")";
        }
        return x;
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
            return String.format(Locale.US, "%,.1f", level);
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
