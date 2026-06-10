package com.hbm.ntm.util;

/**
 * Legacy-name facade for expression evaluation.
 */
@Deprecated(forRemoval = false)
public final class Calculator {
    private Calculator() {
    }

    public static double evaluateExpression(String input) {
        return HbmCalculator.evaluateExpression(input);
    }
}
