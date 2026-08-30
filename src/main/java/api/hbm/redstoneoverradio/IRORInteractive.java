package api.hbm.redstoneoverradio;

import java.util.Locale;

/**
 * Legacy 1.7.10 package bridge for Redstone-over-Radio command endpoints.
 */
@Deprecated(forRemoval = false)
public interface IRORInteractive extends IRORInfo, com.hbm.ntm.api.redstoneoverradio.IRORInteractive {
    static String getCommand(String input) {
        if (input == null || input.isEmpty()) {
            throw new RORFunctionException(EX_NULL);
        }
        String[] parts = input.split(NAME_SEPARATOR);
        if (parts.length <= 0 || parts.length > 2) {
            throw new RORFunctionException(EX_NAME);
        }
        if (parts[0].isEmpty()) {
            throw new RORFunctionException(EX_NULL);
        }
        return parts[0].toLowerCase(Locale.US);
    }

    static String[] getParams(String input) {
        if (input == null || input.isEmpty()) {
            throw new RORFunctionException(EX_NULL);
        }
        String[] parts = input.split(NAME_SEPARATOR);
        if (parts.length <= 0 || parts.length > 2) {
            throw new RORFunctionException(EX_NAME);
        }
        if (parts.length == 1) {
            return new String[0];
        }
        return parts[1].split(PARAM_SEPARATOR);
    }

    static int parseInt(String value) {
        return parseInt(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    static int parseInt(String value, int min, int max) {
        int result;
        try {
            result = Integer.parseInt(value);
        } catch (Exception ex) {
            try {
                result = (int) Math.round(Double.parseDouble(value));
            } catch (Exception decimalEx) {
                throw new RORFunctionException(EX_FORMAT);
            }
        }
        if (result < min || result > max) {
            throw new RORFunctionException(EX_FORMAT);
        }
        return result;
    }
}
