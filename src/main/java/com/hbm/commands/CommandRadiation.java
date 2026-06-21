package com.hbm.commands;

import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.radiation.RadiationConstants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * Legacy /ntmrad facade. The real dispatcher registration lives in
 * ModCommands; this class keeps the old command class boundary available for
 * source migrations.
 */
public class CommandRadiation {
    private static final int LEGACY_PERMISSION_LEVEL = 4;

    public String getCommandName() {
        return "ntmrad";
    }

    public String getCommandUsage(CommandSourceStack source) {
        return "/ntmrad <set/clear>";
    }

    public int processCommand(CommandSourceStack source, String[] args) {
        if (source == null) {
            return 0;
        }
        if (args != null && args.length == 1 && "clear".equals(args[0])) {
            return clear(source);
        }
        if (args != null && args.length == 2 && "set".equals(args[0])) {
            Float amount = parseAmount(args[1]);
            if (amount == null) {
                source.sendFailure(Component.literal(getCommandUsage(source)));
                return 0;
            }
            return set(source, amount);
        }
        source.sendFailure(Component.literal(getCommandUsage(source)));
        return 0;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ntmrad")
                .requires(source -> source.hasPermission(LEGACY_PERMISSION_LEVEL))
                .then(Commands.literal("clear")
                        .executes(context -> clear(context.getSource())))
                .then(Commands.literal("set")
                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F, RadiationConstants.MAX_CHUNK_RADIATION))
                                .executes(context -> set(context.getSource(), FloatArgumentType.getFloat(context, "amount"))))));
    }

    private static int clear(CommandSourceStack source) {
        ChunkRadiationManager.clear(source.getLevel());
        source.sendSuccess(() -> Component.literal("Cleared radiation data!"), true);
        return 1;
    }

    private static int set(CommandSourceStack source, float amount) {
        ChunkRadiationManager.setRadiation(source.getLevel(), BlockPos.containing(source.getPosition()), amount);
        source.sendSuccess(() -> Component.literal("Radiation set."), true);
        return Math.round(amount);
    }

    private static Float parseAmount(String value) {
        try {
            float amount = Float.parseFloat(value);
            if (amount < 0.0F || amount > RadiationConstants.MAX_CHUNK_RADIATION) {
                return null;
            }
            return amount;
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
