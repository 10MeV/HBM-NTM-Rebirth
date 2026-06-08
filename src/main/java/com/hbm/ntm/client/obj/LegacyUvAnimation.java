package com.hbm.ntm.client.obj;

public final class LegacyUvAnimation {
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
        return wrappedFraction((double) worldTime + partialTicks, periodTicks);
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
        return range(((double) worldTime / 10.0D + partialTicks) % 10.0D, 1.0D);
    }

    public static Range bigAssTankFluidU(long worldTime, float partialTicks) {
        return bigAssTankFluidU(worldTime, partialTicks, 0.5D);
    }

    public static Range bigAssTankFluidU(long worldTime, float partialTicks, double scaleFactor) {
        double minU = negativeWorldTickFraction(worldTime, partialTicks, 250.0D);
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

    public record Range(double min, double max) {
        public Range offset(double offset) {
            return new Range(min + offset, max + offset);
        }

        public double length() {
            return max - min;
        }
    }

    private LegacyUvAnimation() {
    }
}
