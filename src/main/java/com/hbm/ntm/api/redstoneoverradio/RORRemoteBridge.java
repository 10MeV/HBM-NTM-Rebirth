package com.hbm.ntm.api.redstoneoverradio;

import net.minecraft.world.level.Level;

public final class RORRemoteBridge {
    public static String readValue(RORValueProvider provider, String name) {
        if (provider == null || name == null || name.isEmpty()) {
            return null;
        }
        return provider.provideRORValue(ROR.valueName(name));
    }

    public static boolean broadcastValue(Level level, String channel, RORValueProvider provider, String name,
            boolean polling, String previousValue) {
        String value = readValue(provider, name);
        if (value == null) {
            return false;
        }
        if (polling || !value.equals(previousValue)) {
            RTTYSystem.broadcast(level, channel, value);
            return true;
        }
        return false;
    }

    public static String runCommand(RORInteractive interactive, String command) {
        if (interactive == null || command == null || command.isEmpty()) {
            return null;
        }
        return interactive.runRORFunction(ROR.functionName(RORInteractive.getCommand(command)),
                RORInteractive.getParams(command));
    }

    public static boolean shouldRunControllerCommand(RTTYSystem.RTTYChannel channel, long currentGameTime,
            boolean polling, String received, String previous) {
        return channel != null && ((polling && channel.timeStamp() >= currentGameTime - 1)
                || (received != null && !received.equals(previous)));
    }

    private RORRemoteBridge() {
    }
}
