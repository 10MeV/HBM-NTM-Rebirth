package com.hbm.util;

import java.util.Collection;
import java.util.function.ToIntFunction;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * Legacy 1.7.10 package bridge for math helpers.
 */
@Deprecated(forRemoval = false)
public final class BobMathUtil {
    private BobMathUtil() {
    }

    public static int min(int... nums) {
        return com.hbm.ntm.util.BobMathUtil.min(nums);
    }

    public static int max(int... nums) {
        return com.hbm.ntm.util.BobMathUtil.max(nums);
    }

    public static long min(long... nums) {
        return com.hbm.ntm.util.BobMathUtil.min(nums);
    }

    public static long max(long... nums) {
        return com.hbm.ntm.util.BobMathUtil.max(nums);
    }

    public static float min(float... nums) {
        return com.hbm.ntm.util.BobMathUtil.min(nums);
    }

    public static float max(float... nums) {
        return com.hbm.ntm.util.BobMathUtil.max(nums);
    }

    public static double min(double... nums) {
        return com.hbm.ntm.util.BobMathUtil.min(nums);
    }

    public static double max(double... nums) {
        return com.hbm.ntm.util.BobMathUtil.max(nums);
    }

    public static double safeClamp(double value, double min, double max) {
        return com.hbm.ntm.util.BobMathUtil.safeClamp(value, min, max);
    }

    public static Vec3 interpVec(Vec3 first, Vec3 second, float partialTick) {
        return com.hbm.ntm.util.BobMathUtil.interpVec(first, second, partialTick);
    }

    public static double interp(double first, double second, float partialTick) {
        return com.hbm.ntm.util.BobMathUtil.interp(first, second, partialTick);
    }

    public static double interp(double first, double second, double partialTick) {
        return com.hbm.ntm.util.BobMathUtil.interp(first, second, partialTick);
    }

    public static double getAngleFrom2DVecs(double x1, double z1, double x2, double z2) {
        return com.hbm.ntm.util.BobMathUtil.getAngleFrom2DVecs(x1, z1, x2, z2);
    }

    public static double getCrossAngle(Vec3 velocity, Vec3 relative) {
        return com.hbm.ntm.util.BobMathUtil.getCrossAngle(velocity, relative);
    }

    public static float remap(float value, float min1, float max1, float min2, float max2) {
        return com.hbm.ntm.util.BobMathUtil.remap(value, min1, max1, min2, max2);
    }

    public static float remap01(float value, float min1, float max1) {
        return com.hbm.ntm.util.BobMathUtil.remap01(value, min1, max1);
    }

    public static float remap01_clamp(float value, float min1, float max1) {
        return com.hbm.ntm.util.BobMathUtil.remap01_clamp(value, min1, max1);
    }

    public static Direction[] getShuffledDirs() {
        return com.hbm.ntm.util.BobMathUtil.getShuffledDirs();
    }

    public static String toPercentage(float amount, float total) {
        return com.hbm.ntm.util.BobMathUtil.toPercentage(amount, total);
    }

    public static String[] ticksToDate(long ticks) {
        return com.hbm.ntm.util.BobMathUtil.ticksToDate(ticks);
    }

    public static double convertScale(double value, double oldMin, double oldMax, double newMin, double newMax) {
        return com.hbm.ntm.util.BobMathUtil.convertScale(value, oldMin, oldMax, newMin, newMax);
    }

    public static double roundDecimal(double value, int digits) {
        return com.hbm.ntm.util.BobMathUtil.roundDecimal(value, digits);
    }

    public static String format(Number amount) {
        return com.hbm.ntm.util.BobMathUtil.format(amount);
    }

    public static boolean getBlink() {
        return com.hbm.ntm.util.BobMathUtil.getBlink();
    }

    public static String getShortNumber(long value) {
        return com.hbm.ntm.util.BobMathUtil.getShortNumber(value);
    }

    public static double squirt(double value) {
        return com.hbm.ntm.util.BobMathUtil.squirt(value);
    }

    public static double angularDifference(double alpha, double beta) {
        return com.hbm.ntm.util.BobMathUtil.angularDifference(alpha, beta);
    }

    public static int[] intCollectionToArray(Collection<Integer> input) {
        return com.hbm.ntm.util.BobMathUtil.intCollectionToArray(input);
    }

    public static int[] intCollectionToArray(Collection<Integer> input, ToIntFunction<? super Object> mapper) {
        return com.hbm.ntm.util.BobMathUtil.intCollectionToArray(input, mapper);
    }

    public static int[] collectionToIntArray(Collection<?> input, ToIntFunction<? super Object> mapper) {
        return com.hbm.ntm.util.BobMathUtil.collectionToIntArray(input, mapper);
    }

    public static void shuffleIntArray(int[] array) {
        com.hbm.ntm.util.BobMathUtil.shuffleIntArray(array);
    }

    public static void reverseIntArray(int[] array) {
        com.hbm.ntm.util.BobMathUtil.reverseIntArray(array);
    }

    public static double sps(double value) {
        return com.hbm.ntm.util.BobMathUtil.sps(value);
    }

    public static double sws(double value, double squarination) {
        return com.hbm.ntm.util.BobMathUtil.sws(value, squarination);
    }
}
