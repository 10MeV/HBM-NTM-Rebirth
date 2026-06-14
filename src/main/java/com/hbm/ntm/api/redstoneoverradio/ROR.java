package com.hbm.ntm.api.redstoneoverradio;

public final class ROR {
    public static String value(String name) {
        return RORInfo.PREFIX_VALUE + name;
    }

    public static String valueName(String name) {
        return name != null && name.startsWith(RORInfo.PREFIX_VALUE) ? name : value(name);
    }

    public static String function(String name) {
        return RORInfo.PREFIX_FUNCTION + name;
    }

    public static String functionName(String name) {
        return name != null && name.startsWith(RORInfo.PREFIX_FUNCTION) ? name : function(name);
    }

    public static String functionInfo(String name, String params) {
        return params == null || params.isEmpty()
                ? function(name)
                : function(name) + RORInteractive.NAME_SEPARATOR + params;
    }

    public static boolean hasValueInfo(RORInfo info) {
        return info != null && hasInfoWithPrefix(info.getFunctionInfo(), RORInfo.PREFIX_VALUE);
    }

    public static boolean hasValueInfo(RORValueProvider provider) {
        return hasValueInfo((RORInfo) provider);
    }

    public static boolean hasFunctionInfo(RORInfo info) {
        return info != null && hasInfoWithPrefix(info.getFunctionInfo(), RORInfo.PREFIX_FUNCTION);
    }

    public static boolean hasFunctionInfo(RORInteractive interactive) {
        return hasFunctionInfo((RORInfo) interactive);
    }

    public static boolean hasInfoWithPrefix(String[] functionInfo, String prefix) {
        if (functionInfo == null || prefix == null) {
            return false;
        }
        for (String entry : functionInfo) {
            if (entry != null && entry.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static String run(RORInteractive interactive, String command) {
        if (interactive == null) {
            throw new RORFunctionException(RORInteractive.EX_NULL);
        }
        return interactive.runRORFunction(functionName(RORInteractive.getCommand(command)), RORInteractive.getParams(command));
    }

    private ROR() {
    }
}
