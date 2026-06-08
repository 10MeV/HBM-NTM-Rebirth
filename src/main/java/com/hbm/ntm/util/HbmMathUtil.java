package com.hbm.ntm.util;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.function.ToIntFunction;

public final class HbmMathUtil {
    private HbmMathUtil() {
    }

    public static int min(int... nums) {
        int smallest = Integer.MAX_VALUE;
        for (int num : nums) {
            if (num < smallest) {
                smallest = num;
            }
        }
        return smallest;
    }

    public static int max(int... nums) {
        int largest = Integer.MIN_VALUE;
        for (int num : nums) {
            if (num > largest) {
                largest = num;
            }
        }
        return largest;
    }

    public static long min(long... nums) {
        long smallest = Long.MAX_VALUE;
        for (long num : nums) {
            if (num < smallest) {
                smallest = num;
            }
        }
        return smallest;
    }

    public static long max(long... nums) {
        long largest = Long.MIN_VALUE;
        for (long num : nums) {
            if (num > largest) {
                largest = num;
            }
        }
        return largest;
    }

    public static float min(float... nums) {
        float smallest = Float.MAX_VALUE;
        for (float num : nums) {
            if (num < smallest) {
                smallest = num;
            }
        }
        return smallest;
    }

    public static float max(float... nums) {
        float largest = -Float.MAX_VALUE;
        for (float num : nums) {
            if (num > largest) {
                largest = num;
            }
        }
        return largest;
    }

    public static double min(double... nums) {
        double smallest = Double.MAX_VALUE;
        for (double num : nums) {
            if (num < smallest) {
                smallest = num;
            }
        }
        return smallest;
    }

    public static double max(double... nums) {
        double largest = -Double.MAX_VALUE;
        for (double num : nums) {
            if (num > largest) {
                largest = num;
            }
        }
        return largest;
    }

    public static double safeClamp(double value, double min, double max) {
        double clamped = Mth.clamp(value, min, max);
        return Double.isNaN(clamped) ? (min + max) / 2.0D : clamped;
    }

    public static Vec3 interpVec(Vec3 first, Vec3 second, double partialTick) {
        return new Vec3(
                interp(first.x, second.x, partialTick),
                interp(first.y, second.y, partialTick),
                interp(first.z, second.z, partialTick));
    }

    public static double interp(double first, double second, double partialTick) {
        return first + (second - first) * partialTick;
    }

    public static double smoothstep(double value, double edge0, double edge1) {
        double t = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0D, 1.0D);
        return t * t * (3.0D - 2.0D * t);
    }

    public static float smoothstep(float value, float edge0, float edge1) {
        float t = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    public static double getAngleFrom2DVecs(double x1, double z1, double x2, double z2) {
        double dot = x1 * x2 + z1 * z2;
        double length = Math.sqrt(x1 * x1 + z1 * z1) * Math.sqrt(x2 * x2 + z2 * z2);
        double result = Math.toDegrees(Math.acos(dot / length));
        return result >= 180.0D ? result - 180.0D : result;
    }

    public static double getCrossAngle(Vec3 velocity, Vec3 relative) {
        Vec3 velocityNorm = velocity.normalize();
        Vec3 relativeNorm = relative.normalize();
        double dot = relativeNorm.dot(velocityNorm);
        double length = relativeNorm.length() * velocityNorm.length();
        double angle = Math.acos(dot / length) * 180.0D / Math.PI;
        return angle >= 180.0D ? angle - 180.0D : angle;
    }

    public static float remap(float value, float min1, float max1, float min2, float max2) {
        return ((value - min1) / (max1 - min1)) * (max2 - min2) + min2;
    }

    public static float remap01(float value, float min1, float max1) {
        return (value - min1) / (max1 - min1);
    }

    public static float remap01Clamp(float value, float min1, float max1) {
        return Mth.clamp((value - min1) / (max1 - min1), 0.0F, 1.0F);
    }

    public static Direction[] getShuffledDirections() {
        Direction[] directions = Direction.values();
        Collections.shuffle(Arrays.asList(directions));
        return directions;
    }

    public static String toPercentage(float amount, float total) {
        return NumberFormat.getPercentInstance().format(amount / total);
    }

    public static String[] ticksToDate(long ticks) {
        int tickDay = 48000;
        int tickYear = tickDay * 100;
        String[] dateOut = new String[3];
        long year = Math.floorDiv(ticks, tickYear);
        byte day = (byte) Math.floorDiv(ticks - tickYear * year, tickDay);
        float time = ticks - (tickYear * year + tickDay * day);
        time = (float) convertScale(time, 0, tickDay, 0, 10.0F);
        dateOut[0] = String.valueOf(year);
        dateOut[1] = String.valueOf(day);
        dateOut[2] = String.valueOf(time);
        return dateOut;
    }

    public static double convertScale(double value, double oldMin, double oldMax, double newMin, double newMax) {
        double previousRange = oldMax - oldMin;
        double newRange = newMax - newMin;
        return (((value - oldMin) * newRange) / previousRange) + newMin;
    }

    public static double roundDecimal(double value, int digits) {
        if (digits < 0) {
            throw new IllegalArgumentException("Attempted negative number in non-negative field! Attempted value: " + digits);
        }
        return new BigDecimal(value).setScale(digits, RoundingMode.HALF_UP).doubleValue();
    }

    public static String format(Number amount) {
        return String.format(Locale.US, "%,d", amount);
    }

    public static boolean getBlink() {
        return System.currentTimeMillis() % 1000L < 500L;
    }

    public static String getShortNumber(long value) {
        double result;
        String suffix;
        long abs = Math.abs(value);
        if (abs >= Math.pow(10, 18)) {
            result = value / Math.pow(10, 18);
            suffix = "E";
        } else if (abs >= Math.pow(10, 15)) {
            result = value / Math.pow(10, 15);
            suffix = "P";
        } else if (abs >= Math.pow(10, 12)) {
            result = value / Math.pow(10, 12);
            suffix = "T";
        } else if (abs >= Math.pow(10, 9)) {
            result = value / Math.pow(10, 9);
            suffix = "G";
        } else if (abs >= Math.pow(10, 6)) {
            result = value / Math.pow(10, 6);
            suffix = "M";
        } else if (abs >= Math.pow(10, 3)) {
            result = value / Math.pow(10, 3);
            suffix = "k";
        } else {
            return Long.toString(value);
        }

        if (result <= -100.0D) {
            result = Math.round(result * 10.0D) / 10.0D;
        } else {
            result = Math.round(result * 100.0D) / 100.0D;
        }
        return result + suffix;
    }

    public static double squirt(double value) {
        return Math.sqrt(value + 1.0D / ((value + 2.0D) * (value + 2.0D))) - 1.0D / (value + 2.0D);
    }

    public static double angularDifference(double alpha, double beta) {
        double delta = (beta - alpha + 180.0D) % 360.0D - 180.0D;
        return delta < -180.0D ? delta + 360.0D : delta;
    }

    public static int[] intCollectionToArray(Collection<Integer> input) {
        return intCollectionToArray(input, value -> (int) value);
    }

    public static int[] intCollectionToArray(Collection<Integer> input, ToIntFunction<? super Object> mapper) {
        return Arrays.stream(input.toArray()).mapToInt(mapper).toArray();
    }

    public static int[] collectionToIntArray(Collection<?> input, ToIntFunction<? super Object> mapper) {
        return Arrays.stream(input.toArray()).mapToInt(mapper).toArray();
    }

    public static void shuffleIntArray(int[] array) {
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int r = random.nextInt(i + 1);
            int temp = array[r];
            array[r] = array[i];
            array[i] = temp;
        }
    }

    public static void reverseIntArray(int[] array) {
        int length = array.length;
        for (int i = 0; i < length / 2; i++) {
            int temp = array[i];
            array[i] = array[length - 1 - i];
            array[length - 1 - i] = temp;
        }
    }

    public static double sps(double value) {
        return Math.sin(Math.PI / 2.0D * Math.cos(value));
    }

    public static double sws(double value, double squarination) {
        double sine = Math.sin(value);
        return Math.pow(Math.abs(sine), 2.0D - squarination) / sine;
    }
}
