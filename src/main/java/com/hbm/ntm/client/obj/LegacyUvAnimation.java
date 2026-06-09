package com.hbm.ntm.client.obj;

public final class LegacyUvAnimation {
    public static double tickTime(long worldTime, float partialTicks) {
        return (double) worldTime + partialTicks;
    }

    public static double scroll(double elapsed, double period) {
        return period == 0.0D ? 0.0D : elapsed / period;
    }

    public static double negativeScroll(double elapsed, double period) {
        return -scroll(elapsed, period);
    }

    public static double wrappedFraction(double value, double period) {
        if (period == 0.0D) {
            return 0.0D;
        }
        double wrapped = value % period;
        if (wrapped < 0.0D) {
            wrapped += period;
        }
        return wrapped / period;
    }

    public static double worldTickFraction(long worldTime, float partialTicks, double periodTicks) {
        return wrappedFraction(tickTime(worldTime, partialTicks), periodTicks);
    }

    public static double negativeWorldTickFraction(long worldTime, float partialTicks, double periodTicks) {
        return -worldTickFraction(worldTime, partialTicks, periodTicks);
    }

    public static double elapsedMillisFraction(long currentMillis, double periodMillis) {
        return wrappedFraction((double) currentMillis, periodMillis);
    }

    public static double negativeElapsedMillisFraction(long currentMillis, double periodMillis) {
        return -elapsedMillisFraction(currentMillis, periodMillis);
    }

    public static Range range(double start, double length) {
        return new Range(start, start + length);
    }

    public static Range assemblyFactorySparkU(long worldTime, float partialTicks) {
        return range((worldTime / 10.0D + partialTicks) % 10.0D, 1.0D);
    }

    public static UnitQuadUv flatItemGlintUv(long currentMillis, int pass, double width, double height) {
        int safePass = Math.max(0, pass);
        double period = 3000.0D + safePass * 1873.0D;
        double animation = wrappedFraction(currentMillis, period) * 256.0D;
        double hScale = safePass == 0 ? 4.0D : -1.0D;
        double uScale = 1.0D / 256.0D;
        double vScale = 1.0D / 256.0D;
        return new UnitQuadUv(
                (animation + height * hScale) * uScale, height * vScale,
                (animation + width + height * hScale) * uScale, height * vScale,
                (animation + width) * uScale, 0.0D,
                animation * uScale, 0.0D);
    }

    public static double textureMatrixGlintOffset(long currentMillis, double periodMillis, double distance) {
        return wrappedFraction(currentMillis, periodMillis) * distance;
    }

    public static double classicGlintMovement(double age, int layer, double speed) {
        int safeLayer = Math.max(0, layer);
        return age * (0.001D + safeLayer * 0.003D) * speed;
    }

    public static double classicGlintRotation(int layer) {
        return 30.0D - Math.max(0, layer) * 60.0D;
    }

    public static double legacyHmfOffset(double currentTime, double modulo, double quotient) {
        return quotient == 0.0D ? 0.0D : (currentTime % modulo) / quotient;
    }

    public static double tomFlameHmfOffset(double currentTime) {
        return legacyHmfOffset(currentTime, 50000.0D, 2500.0D);
    }

    public static double falloutRainSwayLoop(int timer, float partialTicks) {
        return ((timer & 511) + partialTicks) / 512.0D;
    }

    public static double falloutRainU(double side, double fallVariation, double fallSpeed) {
        return side * fallSpeed + fallVariation;
    }

    public static double chemicalPlantFluidU(double animation) {
        return -animation / 100.0D;
    }

    public static double chemicalPlantFluidV(double animation) {
        return LegacyObjTransforms.softPeakSine(animation * 0.1D) * 0.1D - 0.25D;
    }

    public static Range bigAssTankFluidU(long worldTime, float partialTicks) {
        return bigAssTankFluidU(tickTime(worldTime, partialTicks));
    }

    public static Range bigAssTankFluidU(double tickTime) {
        return bigAssTankFluidU(tickTime, 0.5D);
    }

    public static Range bigAssTankFluidU(long worldTime, float partialTicks, double scaleFactor) {
        return bigAssTankFluidU(tickTime(worldTime, partialTicks), scaleFactor);
    }

    public static Range bigAssTankFluidU(double tickTime, double scaleFactor) {
        double minU = -wrappedFraction(tickTime, 250.0D);
        return range(minU, scaleFactor);
    }

    public static double bigAssTankFluidV(double height) {
        return bigAssTankFluidV(height, 0.5D);
    }

    public static double bigAssTankFluidV(double height, double scaleFactor) {
        return -height * 2.0D * scaleFactor;
    }

    public static double annihilatorBeltU(long currentMillis) {
        return negativeElapsedMillisFraction(currentMillis, 3000.0D);
    }

    public static double falloutRainV(double y, double fallLoop, double swayVariation) {
        return falloutRainV(y, fallLoop, swayVariation, 1.0D);
    }

    public static double falloutRainV(double y, double fallLoop, double swayVariation, double fallSpeed) {
        return y * fallSpeed / 4.0D + fallLoop * fallSpeed + swayVariation;
    }

    public record Range(double min, double max) {
        public Range offset(double offset) {
            return new Range(min + offset, max + offset);
        }

        public double length() {
            return max - min;
        }
    }

    public record UnitQuadUv(
            double bottomLeftU, double bottomLeftV,
            double bottomRightU, double bottomRightV,
            double topRightU, double topRightV,
            double topLeftU, double topLeftV) {
    }

    private LegacyUvAnimation() {
    }
}
