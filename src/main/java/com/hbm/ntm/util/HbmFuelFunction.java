package com.hbm.ntm.util;

import net.minecraft.ChatFormatting;

import java.util.Locale;

public abstract class HbmFuelFunction {
    protected double div = 1.0D;
    protected double off = 0.0D;

    public abstract double effonix(double x);

    public abstract String getLabelForFuel();

    public abstract String getDangerFromFuel();

    public HbmFuelFunction withDiv(double div) {
        this.div = div;
        return this;
    }

    public HbmFuelFunction withOff(double off) {
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
        boolean modified = false;
        if (div != 1.0D) {
            x += " / " + format(div, 1);
            modified = true;
        }
        if (off != 0.0D) {
            x += " + " + format(off, 1);
            modified = true;
        }
        return modified && brackets ? "(" + x + ")" : x;
    }

    protected static String format(double value, int decimals) {
        return String.format(Locale.US, "%,." + decimals + "f", value);
    }

    public abstract static class SingleArg extends HbmFuelFunction {
        protected final double level;

        protected SingleArg(double level) {
            this.level = level;
        }
    }

    public abstract static class DoubleArg extends HbmFuelFunction {
        protected final double level;
        protected final double vOff;

        protected DoubleArg(double level, double vOff) {
            this.level = level;
            this.vOff = vOff;
        }
    }

    public static class Logarithmic extends SingleArg {
        public Logarithmic(double level) {
            super(level);
            withOff(1.0D);
        }

        @Override
        public double effonix(double x) {
            return Math.log10(getX(x)) * level;
        }

        @Override
        public String getLabelForFuel() {
            return "log10(" + getXName(false) + ") * " + format(level, 1);
        }

        @Override
        public String getDangerFromFuel() {
            return ChatFormatting.YELLOW + "MEDIUM / LOGARITHMIC";
        }
    }

    public static class Passive extends SingleArg {
        public Passive(double level) {
            super(level);
        }

        @Override
        public double effonix(double x) {
            return level;
        }

        @Override
        public String getLabelForFuel() {
            return format(level, 1);
        }

        @Override
        public String getDangerFromFuel() {
            return ChatFormatting.DARK_GREEN + "SAFE / PASSIVE";
        }
    }

    public static class Sqrt extends SingleArg {
        public Sqrt(double level) {
            super(level);
        }

        @Override
        public double effonix(double x) {
            return HbmMathUtil.squirt(getX(x)) * level;
        }

        @Override
        public String getLabelForFuel() {
            return "sqrt(" + getXName(false) + ") * " + format(level, 3);
        }

        @Override
        public String getDangerFromFuel() {
            return ChatFormatting.YELLOW + "MEDIUM / SQUARE ROOT";
        }
    }

    public static class SqrtFalling extends Sqrt {
        public SqrtFalling(double fallFactor) {
            super(1.0D / fallFactor);
            withOff(fallFactor * fallFactor);
        }
    }

    public static class Linear extends SingleArg {
        public Linear(double level) {
            super(level);
        }

        @Override
        public double effonix(double x) {
            return getX(x) * level;
        }

        @Override
        public String getLabelForFuel() {
            return getXName(true) + " * " + format(level, 1);
        }

        @Override
        public String getDangerFromFuel() {
            return ChatFormatting.RED + "DANGEROUS / LINEAR";
        }
    }

    public static class Quadratic extends DoubleArg {
        public Quadratic(double level) {
            this(level, 0.0D);
        }

        public Quadratic(double level, double vOff) {
            super(level, vOff);
        }

        @Override
        public double effonix(double x) {
            return getX(x) * getX(x) * level + vOff;
        }

        @Override
        public String getLabelForFuel() {
            return getXName(true) + "^2 * " + format(level, 1)
                    + (vOff != 0.0D ? " + " + format(vOff, 1) : "");
        }

        @Override
        public String getDangerFromFuel() {
            return ChatFormatting.RED + "DANGEROUS / QUADRATIC";
        }
    }
}
