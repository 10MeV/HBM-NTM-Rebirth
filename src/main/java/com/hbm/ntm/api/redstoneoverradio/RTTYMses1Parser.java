package com.hbm.ntm.api.redstoneoverradio;

import com.hbm.ntm.util.HbmCalculator;

import java.util.Locale;

public class RTTYMses1Parser implements RTTYScriptParser {
    @Override
    public StatementReturn eval(ParseContext context, String line) {
        String lower = line.toLowerCase(Locale.US);
        if (line.isEmpty() || lower.startsWith("dest ") || lower.startsWith("# ")) {
            return StatementReturn.SKIP;
        }
        if (lower.equals("nop")) {
            return StatementReturn.OK;
        }
        if (lower.startsWith("clockspeed ")) {
            if (line.length() <= 11) {
                return StatementReturn.PARAMETER_ERROR;
            }
            try {
                int speed = Integer.parseInt(line.substring(11));
                if (speed < 1 || speed > context.maxClockSpeed()) {
                    return StatementReturn.PARAMETER_ERROR;
                }
                context.setClockSpeed(speed);
            } catch (Throwable ex) {
                return StatementReturn.PARAMETER_ERROR;
            }
            return StatementReturn.SKIP;
        }
        if (lower.startsWith("jmp ")) {
            if (line.length() <= 4) {
                return StatementReturn.PARAMETER_ERROR;
            }
            String jumpKey = substitute(context, line.substring(4), false);
            if (context.jumps().containsKey(jumpKey)) {
                context.setCurrent(context.jumps().get(jumpKey));
                return StatementReturn.OK;
            }
            return StatementReturn.PARAMETER_ERROR;
        }
        if (lower.startsWith("jmpif ")) {
            if (line.length() <= 6) {
                return StatementReturn.PARAMETER_ERROR;
            }
            if (!context.buffer().equals("true")) {
                return StatementReturn.OK;
            }
            String jumpKey = substitute(context, line.substring(6), false);
            if (context.jumps().containsKey(jumpKey)) {
                context.setCurrent(context.jumps().get(jumpKey));
                return StatementReturn.OK;
            }
            return StatementReturn.PARAMETER_ERROR;
        }
        if (lower.startsWith("jmpnot ")) {
            if (line.length() <= 7) {
                return StatementReturn.PARAMETER_ERROR;
            }
            if (context.buffer().equals("true")) {
                return StatementReturn.OK;
            }
            String jumpKey = substitute(context, line.substring(7), false);
            if (context.jumps().containsKey(jumpKey)) {
                context.setCurrent(context.jumps().get(jumpKey));
                return StatementReturn.OK;
            }
            return StatementReturn.PARAMETER_ERROR;
        }
        if (lower.equals("endtick")) {
            return StatementReturn.END_TICK;
        }
        if (lower.equals("shutdown")) {
            return StatementReturn.SHUTDOWN;
        }
        if (lower.startsWith("load ")) {
            if (line.length() <= 5) {
                return StatementReturn.PARAMETER_ERROR;
            }
            context.setBuffer(context.variables().getString(line.substring(5)));
            return StatementReturn.OK;
        }
        if (lower.startsWith("save ")) {
            if (line.length() <= 5 || context.buffer().isEmpty()) {
                return StatementReturn.PARAMETER_ERROR;
            }
            context.variables().putString(line.substring(5), context.buffer());
            return StatementReturn.OK;
        }
        if (lower.startsWith("buffer ")) {
            if (line.length() <= 7) {
                return StatementReturn.PARAMETER_ERROR;
            }
            context.setBuffer(line.substring(7));
            return StatementReturn.OK;
        }
        if (lower.startsWith("eval ")) {
            if (line.length() <= 5) {
                return StatementReturn.PARAMETER_ERROR;
            }
            return evaluateToBuffer(context, substitute(context, line.substring(5), true), false);
        }
        if (lower.startsWith("evalr ")) {
            if (line.length() <= 6) {
                return StatementReturn.PARAMETER_ERROR;
            }
            return evaluateToBuffer(context, substitute(context, line.substring(6), true), true);
        }
        if (lower.equals("evalr")) {
            if (context.buffer().isEmpty()) {
                return StatementReturn.PARAMETER_ERROR;
            }
            return evaluateToBuffer(context, substitute(context, context.buffer(), true), true);
        }
        if (lower.equals("rounddown") || lower.equals("floor")) {
            return roundBuffer(context, RoundMode.FLOOR);
        }
        if (lower.equals("roundup") || lower.equals("ceil")) {
            return roundBuffer(context, RoundMode.CEIL);
        }
        if (lower.equals("round") || lower.equals("nearest")) {
            return roundBuffer(context, RoundMode.NEAREST);
        }
        if (lower.startsWith("concat ")) {
            if (line.length() <= 7 || context.buffer().isEmpty()) {
                return StatementReturn.PARAMETER_ERROR;
            }
            context.setBuffer(substitute(context, line.substring(7), false));
            return StatementReturn.OK;
        }
        if (lower.startsWith("eq ")) {
            if (line.length() <= 3 || context.buffer().isEmpty()) {
                return StatementReturn.PARAMETER_ERROR;
            }
            context.setBuffer(context.buffer().equals(substitute(context, line.substring(3), false)) ? "true" : "false");
            return StatementReturn.OK;
        }
        if (lower.startsWith("gtb ")) {
            return compareBuffer(context, line, 3, Comparison.GREATER_THAN_BUFFER);
        }
        if (lower.startsWith("ltb ")) {
            return compareBuffer(context, line, 3, Comparison.LOWER_THAN_BUFFER);
        }
        if (lower.startsWith("geb ")) {
            return compareBuffer(context, line, 3, Comparison.GREATER_EQUAL_BUFFER);
        }
        if (lower.startsWith("leb ")) {
            return compareBuffer(context, line, 3, Comparison.LOWER_EQUAL_BUFFER);
        }
        if (lower.startsWith("send ")) {
            if (line.length() <= 5 || context.buffer().isEmpty()) {
                return StatementReturn.PARAMETER_ERROR;
            }
            RTTYSystem.broadcast(context.level(), substitute(context, line.substring(5), false), context.buffer());
            return StatementReturn.OK;
        }
        if (lower.startsWith("listen ")) {
            if (line.length() <= 7) {
                return StatementReturn.PARAMETER_ERROR;
            }
            RTTYSystem.RTTYChannel channel = RTTYSystem.listen(context.level(), substitute(context, line.substring(7), false));
            if (channel != null) {
                context.setBuffer(channel.signalString());
            }
            return StatementReturn.OK;
        }
        return StatementReturn.UNRECOGNIZED_COMMAND;
    }

    public String substitute(ParseContext context, String statement, boolean forceNumber) {
        if (!statement.contains("$")) {
            return statement;
        }
        StringBuilder joined = new StringBuilder();
        StringBuilder variableName = new StringBuilder();
        boolean readingVariable = false;
        for (char character : statement.toCharArray()) {
            if (character == '$') {
                if (!readingVariable) {
                    readingVariable = true;
                } else {
                    if ("buffer".contentEquals(variableName)) {
                        joined.append(context.buffer());
                    } else {
                        String variable = context.variables().getString(variableName.toString());
                        if (forceNumber && variable.isEmpty()) {
                            variable = "0";
                        }
                        joined.append(variable);
                        variableName.delete(0, variableName.length());
                        readingVariable = false;
                    }
                }
            } else if (readingVariable) {
                variableName.append(character);
            } else {
                joined.append(character);
            }
        }
        return joined.toString();
    }

    @Override
    public void generateJumpPoints(ParseContext context, String line, int index) {
        if (line.startsWith("dest ") && line.length() > 5) {
            context.jumps().put(line.substring(5), index);
        }
    }

    private StatementReturn evaluateToBuffer(ParseContext context, String statement, boolean round) {
        try {
            double result = HbmCalculator.evaluateExpression(statement);
            context.setBuffer(round ? Integer.toString((int) Math.round(result)) : Double.toString(result));
            return StatementReturn.OK;
        } catch (Throwable ex) {
            return StatementReturn.PARAMETER_ERROR;
        }
    }

    private StatementReturn roundBuffer(ParseContext context, RoundMode mode) {
        if (context.buffer().isEmpty()) {
            return StatementReturn.PARAMETER_ERROR;
        }
        try {
            double value = Double.parseDouble(context.buffer());
            int rounded = switch (mode) {
                case FLOOR -> (int) Math.floor(value);
                case CEIL -> (int) Math.ceil(value);
                case NEAREST -> (int) Math.round(value);
            };
            context.setBuffer(Integer.toString(rounded));
            return StatementReturn.OK;
        } catch (Exception ex) {
            return StatementReturn.PARAMETER_ERROR;
        }
    }

    private StatementReturn compareBuffer(ParseContext context, String line, int offset, Comparison comparison) {
        if (line.length() <= offset || context.buffer().isEmpty()) {
            return StatementReturn.PARAMETER_ERROR;
        }
        try {
            double buffer = Double.parseDouble(context.buffer());
            double value = Double.parseDouble(line.substring(offset));
            boolean result = switch (comparison) {
                case GREATER_THAN_BUFFER -> value > buffer;
                case LOWER_THAN_BUFFER -> value < buffer;
                case GREATER_EQUAL_BUFFER -> value >= buffer;
                case LOWER_EQUAL_BUFFER -> value <= buffer;
            };
            context.setBuffer(result ? "true" : "false");
            return StatementReturn.OK;
        } catch (Exception ex) {
            return StatementReturn.PARAMETER_ERROR;
        }
    }

    private enum RoundMode {
        FLOOR,
        CEIL,
        NEAREST
    }

    private enum Comparison {
        GREATER_THAN_BUFFER,
        LOWER_THAN_BUFFER,
        GREATER_EQUAL_BUFFER,
        LOWER_EQUAL_BUFFER
    }
}
