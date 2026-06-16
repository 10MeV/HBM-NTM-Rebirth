package com.hbm.ntm.util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.function.ToIntFunction;

/**
 * Legacy-name facade for the old BobMathUtil helpers.
 */
@Deprecated(forRemoval = false)
public final class BobMathUtil {
    private BobMathUtil() {
    }

    public static int min(int... nums) {
        return HbmMathUtil.min(nums);
    }

    public static int max(int... nums) {
        return HbmMathUtil.max(nums);
    }

    public static long min(long... nums) {
        return HbmMathUtil.min(nums);
    }

    public static long max(long... nums) {
        return HbmMathUtil.max(nums);
    }

    public static float min(float... nums) {
        return HbmMathUtil.min(nums);
    }

    public static float max(float... nums) {
        return HbmMathUtil.max(nums);
    }

    public static double min(double... nums) {
        return HbmMathUtil.min(nums);
    }

    public static double max(double... nums) {
        return HbmMathUtil.max(nums);
    }

    public static double safeClamp(double value, double min, double max) {
        return HbmMathUtil.safeClamp(value, min, max);
    }

    public static Vec3 interpVec(Vec3 first, Vec3 second, float partialTick) {
        return HbmMathUtil.interpVec(first, second, partialTick);
    }

    public static double interp(double first, double second, float partialTick) {
        return HbmMathUtil.interp(first, second, partialTick);
    }

    public static double interp(double first, double second, double partialTick) {
        return HbmMathUtil.interp(first, second, partialTick);
    }

    public static double getAngleFrom2DVecs(double x1, double z1, double x2, double z2) {
        return HbmMathUtil.getAngleFrom2DVecs(x1, z1, x2, z2);
    }

    public static double getCrossAngle(Vec3 velocity, Vec3 relative) {
        return HbmMathUtil.getCrossAngle(velocity, relative);
    }

    public static float remap(float value, float min1, float max1, float min2, float max2) {
        return HbmMathUtil.remap(value, min1, max1, min2, max2);
    }

    public static float remap01(float value, float min1, float max1) {
        return HbmMathUtil.remap01(value, min1, max1);
    }

    public static float remap01_clamp(float value, float min1, float max1) {
        return HbmMathUtil.remap01Clamp(value, min1, max1);
    }

    public static Direction[] getShuffledDirs() {
        return HbmMathUtil.getShuffledDirections();
    }

    public static String toPercentage(float amount, float total) {
        return HbmMathUtil.toPercentage(amount, total);
    }

    public static String[] ticksToDate(long ticks) {
        return HbmMathUtil.ticksToDate(ticks);
    }

    public static double convertScale(double value, double oldMin, double oldMax, double newMin, double newMax) {
        return HbmMathUtil.convertScale(value, oldMin, oldMax, newMin, newMax);
    }

    public static double roundDecimal(double value, int digits) {
        return HbmMathUtil.roundDecimal(value, digits);
    }

    public static String format(Number amount) {
        return HbmMathUtil.format(amount);
    }

    public static boolean getBlink() {
        return HbmMathUtil.getBlink();
    }

    public static String getShortNumber(long value) {
        return HbmMathUtil.getShortNumber(value);
    }

    public static double squirt(double value) {
        return HbmMathUtil.squirt(value);
    }

    public static void setPi(double pi) {
        HbmMathUtil.setPi(pi);
    }

    public static double angularDifference(double alpha, double beta) {
        return HbmMathUtil.angularDifference(alpha, beta);
    }

    public static int[] intCollectionToArray(Collection<Integer> input) {
        return HbmMathUtil.intCollectionToArray(input);
    }

    public static int[] intCollectionToArray(Collection<Integer> input, ToIntFunction<? super Object> mapper) {
        return HbmMathUtil.intCollectionToArray(input, mapper);
    }

    public static int[] collectionToIntArray(Collection<?> input, ToIntFunction<? super Object> mapper) {
        return HbmMathUtil.collectionToIntArray(input, mapper);
    }

    public static void shuffleIntArray(int[] array) {
        HbmMathUtil.shuffleIntArray(array);
    }

    public static void reverseIntArray(int[] array) {
        HbmMathUtil.reverseIntArray(array);
    }

    public static double sps(double value) {
        return HbmMathUtil.sps(value);
    }

    public static double sws(double value, double squarination) {
        return HbmMathUtil.sws(value, squarination);
    }
}
