package com.hbm.util;

/**
 * Legacy 1.7.10 package bridge for expression evaluation.
 */
@Deprecated(forRemoval = false)
public final class Calculator {
    private Calculator() {
    }

    public static double evaluateExpression(String input) {
        return com.hbm.ntm.util.Calculator.evaluateExpression(input);
    }
}
