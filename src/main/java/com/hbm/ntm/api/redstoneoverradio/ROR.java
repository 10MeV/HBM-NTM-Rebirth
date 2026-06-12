package com.hbm.ntm.api.redstoneoverradio;

public final class ROR {
    public static String value(String name) {
        return RORInfo.PREFIX_VALUE + name;
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

    public static String run(RORInteractive interactive, String command) {
        if (interactive == null) {
            throw new RORFunctionException(RORInteractive.EX_NULL);
        }
        return interactive.runRORFunction(functionName(RORInteractive.getCommand(command)), RORInteractive.getParams(command));
    }

    private ROR() {
    }
}
