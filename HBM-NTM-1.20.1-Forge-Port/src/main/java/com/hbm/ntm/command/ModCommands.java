package com.hbm.ntm.command;

import com.hbm.ntm.api.tile.InfoProviderEC;
import com.hbm.ntm.energy.HbmEnergyNodespace;
import com.hbm.ntm.event.CommonForgeEvents;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.radiation.RadiationConstants;
import com.hbm.ntm.radiation.RadiationData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collection;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

public final class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ntmrad")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("clear")
                        .executes(context -> clearChunkRadiation(context.getSource())))
                .then(Commands.literal("set")
                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F, RadiationConstants.MAX_CHUNK_RADIATION))
                                .executes(context -> setChunkRadiation(context.getSource(), FloatArgumentType.getFloat(context, "amount"))))));

        dispatcher.register(Commands.literal("hbm")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("radiation")
                        .then(Commands.literal("chunk")
                                .then(Commands.literal("get")
                                        .executes(context -> getChunkRadiation(context.getSource())))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F, RadiationConstants.MAX_CHUNK_RADIATION))
                                                .executes(context -> setChunkRadiation(context.getSource(), FloatArgumentType.getFloat(context, "amount")))))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(-RadiationConstants.MAX_CHUNK_RADIATION, RadiationConstants.MAX_CHUNK_RADIATION))
                                                .executes(context -> addChunkRadiation(context.getSource(), FloatArgumentType.getFloat(context, "amount")))))
                                .then(Commands.literal("clear")
                                        .executes(context -> clearChunkRadiation(context.getSource()))))
                        .then(Commands.literal("player")
                                .then(Commands.literal("get")
                                        .executes(context -> getPlayerRadiation(context.getSource(), context.getSource().getPlayerOrException()))
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(context -> getPlayerRadiation(context.getSource(), EntityArgument.getPlayers(context, "targets")))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F, RadiationConstants.MAX_PLAYER_RADIATION))
                                                        .executes(context -> setPlayerRadiation(context.getSource(), EntityArgument.getPlayers(context, "targets"), FloatArgumentType.getFloat(context, "amount"))))))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("amount", FloatArgumentType.floatArg(-RadiationConstants.MAX_PLAYER_RADIATION, RadiationConstants.MAX_PLAYER_RADIATION))
                                                        .executes(context -> addPlayerRadiation(context.getSource(), EntityArgument.getPlayers(context, "targets"), FloatArgumentType.getFloat(context, "amount"))))))
                                .then(Commands.literal("clear")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(context -> setPlayerRadiation(context.getSource(), EntityArgument.getPlayers(context, "targets"), 0.0F)))))
                        .then(Commands.literal("digamma")
                                .then(Commands.literal("get")
                                        .executes(context -> getPlayerDigamma(context.getSource(), context.getSource().getPlayerOrException()))
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(context -> getPlayerDigamma(context.getSource(), EntityArgument.getPlayers(context, "targets")))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F, 10.0F))
                                                        .executes(context -> setPlayerDigamma(context.getSource(), EntityArgument.getPlayers(context, "targets"), FloatArgumentType.getFloat(context, "amount"))))))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("amount", FloatArgumentType.floatArg(-10.0F, 10.0F))
                                                        .executes(context -> addPlayerDigamma(context.getSource(), EntityArgument.getPlayers(context, "targets"), FloatArgumentType.getFloat(context, "amount"))))))
                                .then(Commands.literal("clear")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(context -> setPlayerDigamma(context.getSource(), EntityArgument.getPlayers(context, "targets"), 0.0F)))))
                        .then(energyCommand())
                        .then(statusCommand())
                        .then(contaminationCommand())));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> statusCommand() {
        return Commands.literal("status")
                .then(Commands.literal("get")
                        .then(Commands.argument("field", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(StatusField.NAMES, builder))
                                .executes(context -> getPlayerStatus(context.getSource(), context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "field")))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> getPlayerStatus(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "field"))))))
                .then(Commands.literal("set")
                        .then(Commands.argument("field", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(StatusField.NAMES, builder))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(context -> setPlayerStatus(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "field"), IntegerArgumentType.getInteger(context, "amount")))))))
                .then(Commands.literal("add")
                        .then(Commands.argument("field", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(StatusField.NAMES, builder))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(-1_000_000, 1_000_000))
                                                .executes(context -> addPlayerStatus(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "field"), IntegerArgumentType.getInteger(context, "amount")))))))
                .then(Commands.literal("clear")
                        .then(Commands.argument("field", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(StatusField.NAMES, builder))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> setPlayerStatus(context.getSource(), EntityArgument.getPlayers(context, "targets"), StringArgumentType.getString(context, "field"), 0)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> energyCommand() {
        return Commands.literal("energy")
                .then(Commands.literal("nodespace")
                        .executes(context -> getEnergyNodespace(context.getSource())))
                .then(Commands.literal("network")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> getEnergyNetwork(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                .then(Commands.literal("info")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> getEnergyInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> contaminationCommand() {
        return Commands.literal("contamination")
                .then(Commands.literal("list")
                        .executes(context -> listContamination(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> listContamination(context.getSource(), EntityArgument.getPlayers(context, "targets")))))
                .then(Commands.literal("add")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("maxRad", FloatArgumentType.floatArg(0.0F, RadiationConstants.MAX_PLAYER_RADIATION))
                                        .then(Commands.argument("maxTime", IntegerArgumentType.integer(1))
                                                .executes(context -> addContamination(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        FloatArgumentType.getFloat(context, "maxRad"),
                                                        IntegerArgumentType.getInteger(context, "maxTime"),
                                                        IntegerArgumentType.getInteger(context, "maxTime"),
                                                        false))
                                                .then(Commands.argument("time", IntegerArgumentType.integer(0))
                                                        .executes(context -> addContamination(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "targets"),
                                                                FloatArgumentType.getFloat(context, "maxRad"),
                                                                IntegerArgumentType.getInteger(context, "maxTime"),
                                                                IntegerArgumentType.getInteger(context, "time"),
                                                                false))
                                                        .then(Commands.argument("ignoreArmor", StringArgumentType.word())
                                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"false", "true"}, builder))
                                                                .executes(context -> addContamination(
                                                                        context.getSource(),
                                                                        EntityArgument.getPlayers(context, "targets"),
                                                                        FloatArgumentType.getFloat(context, "maxRad"),
                                                                        IntegerArgumentType.getInteger(context, "maxTime"),
                                                                        IntegerArgumentType.getInteger(context, "time"),
                                                                        parseBoolean(StringArgumentType.getString(context, "ignoreArmor"))))))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                        .executes(context -> removeContamination(context.getSource(), EntityArgument.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "index"))))))
                .then(Commands.literal("clear")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> clearContamination(context.getSource(), EntityArgument.getPlayers(context, "targets")))));
    }

    private static int getChunkRadiation(CommandSourceStack source) {
        float amount = ChunkRadiationManager.getRadiation(source.getLevel(), BlockPos.containing(source.getPosition()));
        source.sendSuccess(() -> Component.literal("Chunk radiation: " + round(amount) + " RAD/s"), false);
        return Math.round(amount);
    }

    private static int setChunkRadiation(CommandSourceStack source, float amount) {
        ChunkRadiationManager.setRadiation(source.getLevel(), BlockPos.containing(source.getPosition()), amount);
        source.sendSuccess(() -> Component.literal("Radiation set."), true);
        return Math.round(amount);
    }

    private static int addChunkRadiation(CommandSourceStack source, float amount) {
        BlockPos pos = BlockPos.containing(source.getPosition());
        ChunkRadiationManager.incrementRadiation(source.getLevel(), pos, amount);
        float current = ChunkRadiationManager.getRadiation(source.getLevel(), pos);
        source.sendSuccess(() -> Component.literal("Chunk radiation: " + round(current) + " RAD/s"), true);
        return Math.round(current);
    }

    private static int clearChunkRadiation(CommandSourceStack source) {
        ChunkRadiationManager.clear(source.getLevel());
        source.sendSuccess(() -> Component.literal("Cleared radiation data!"), true);
        return 1;
    }

    private static int getEnergyNodespace(CommandSourceStack source) {
        int nodes = HbmEnergyNodespace.getNodeCount(source.getLevel());
        int networks = HbmEnergyNodespace.getNetworkCount(source.getLevel());
        source.sendSuccess(() -> Component.literal("Energy nodespace: nodes=" + nodes + " networks=" + networks), false);
        return nodes;
    }

    private static int getEnergyNetwork(CommandSourceStack source, BlockPos pos) {
        boolean valid = HbmEnergyNodespace.hasValidNetwork(source.getLevel(), pos);
        int links = HbmEnergyNodespace.getNetworkLinkCount(source.getLevel(), pos);
        int providers = HbmEnergyNodespace.getNetworkProviderCount(source.getLevel(), pos);
        int receivers = HbmEnergyNodespace.getNetworkReceiverCount(source.getLevel(), pos);
        long tracker = HbmEnergyNodespace.getNetworkEnergyTracker(source.getLevel(), pos);
        source.sendSuccess(() -> Component.literal("Energy network at " + pos.toShortString()
                + ": valid=" + valid
                + " links=" + links
                + " providers=" + providers
                + " receivers=" + receivers
                + " lastTransfer=" + tracker + " HE"), false);
        return links;
    }

    private static int getEnergyInfo(CommandSourceStack source, BlockPos pos) {
        BlockEntity blockEntity = source.getLevel().getBlockEntity(pos);
        if (!(blockEntity instanceof InfoProviderEC provider)) {
            source.sendFailure(Component.literal("No HBM EnergyControl info provider at " + pos.toShortString()));
            return 0;
        }
        CompoundTag data = new CompoundTag();
        provider.provideExtraInfo(data);
        source.sendSuccess(() -> Component.literal("Energy info at " + pos.toShortString() + ": " + data), false);
        return data.size();
    }

    private static int getPlayerRadiation(CommandSourceStack source, Player player) {
        source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " radiation: " + round(RadiationData.getRadiation(player)) + " RAD"), false);
        return Math.round(RadiationData.getRadiation(player));
    }

    private static int getPlayerRadiation(CommandSourceStack source, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            getPlayerRadiation(source, player);
        }
        return players.size();
    }

    private static int setPlayerRadiation(CommandSourceStack source, Collection<ServerPlayer> players, float amount) {
        for (ServerPlayer player : players) {
            RadiationData.setRadiation(player, amount);
        }
        syncPlayers(players);
        source.sendSuccess(() -> Component.literal("Set radiation for " + players.size() + " player(s)."), true);
        return players.size();
    }

    private static int addPlayerRadiation(CommandSourceStack source, Collection<ServerPlayer> players, float amount) {
        for (ServerPlayer player : players) {
            RadiationData.incrementRadiation(player, amount);
        }
        syncPlayers(players);
        source.sendSuccess(() -> Component.literal("Updated radiation for " + players.size() + " player(s)."), true);
        return players.size();
    }

    private static int getPlayerDigamma(CommandSourceStack source, Player player) {
        source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " digamma: " + round(RadiationData.getDigamma(player))), false);
        return Math.round(RadiationData.getDigamma(player));
    }

    private static int getPlayerDigamma(CommandSourceStack source, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            getPlayerDigamma(source, player);
        }
        return players.size();
    }

    private static int setPlayerDigamma(CommandSourceStack source, Collection<ServerPlayer> players, float amount) {
        for (ServerPlayer player : players) {
            RadiationData.setDigamma(player, amount);
        }
        syncPlayers(players);
        source.sendSuccess(() -> Component.literal("Set digamma for " + players.size() + " player(s)."), true);
        return players.size();
    }

    private static int addPlayerDigamma(CommandSourceStack source, Collection<ServerPlayer> players, float amount) {
        for (ServerPlayer player : players) {
            RadiationData.incrementDigamma(player, amount);
        }
        syncPlayers(players);
        source.sendSuccess(() -> Component.literal("Updated digamma for " + players.size() + " player(s)."), true);
        return players.size();
    }

    private static int getPlayerStatus(CommandSourceStack source, ServerPlayer player, String fieldName) {
        StatusField field = StatusField.byName(fieldName);
        if (field == null) {
            return unknownStatusField(source, fieldName);
        }
        source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " " + field.id + ": " + field.get(player)), false);
        return field.get(player);
    }

    private static int getPlayerStatus(CommandSourceStack source, Collection<ServerPlayer> players, String fieldName) {
        StatusField field = StatusField.byName(fieldName);
        if (field == null) {
            return unknownStatusField(source, fieldName);
        }
        for (ServerPlayer player : players) {
            source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " " + field.id + ": " + field.get(player)), false);
        }
        return players.size();
    }

    private static int setPlayerStatus(CommandSourceStack source, Collection<ServerPlayer> players, String fieldName, int amount) {
        StatusField field = StatusField.byName(fieldName);
        if (field == null) {
            return unknownStatusField(source, fieldName);
        }
        for (ServerPlayer player : players) {
            field.set(player, amount);
        }
        syncPlayers(players);
        source.sendSuccess(() -> Component.literal("Set " + field.id + " for " + players.size() + " player(s)."), true);
        return players.size();
    }

    private static int addPlayerStatus(CommandSourceStack source, Collection<ServerPlayer> players, String fieldName, int amount) {
        StatusField field = StatusField.byName(fieldName);
        if (field == null) {
            return unknownStatusField(source, fieldName);
        }
        for (ServerPlayer player : players) {
            field.set(player, field.get(player) + amount);
        }
        syncPlayers(players);
        source.sendSuccess(() -> Component.literal("Updated " + field.id + " for " + players.size() + " player(s)."), true);
        return players.size();
    }

    private static int unknownStatusField(CommandSourceStack source, String fieldName) {
        source.sendFailure(Component.literal("Unknown radiation status '" + fieldName + "'. Valid: " + String.join(", ", StatusField.NAMES)));
        return 0;
    }

    private static int listContamination(CommandSourceStack source, ServerPlayer player) {
        Collection<RadiationData.ContaminationEffect> effects = RadiationData.getContaminationEffects(player);
        source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " contamination entries: " + effects.size()), false);
        int index = 0;
        for (RadiationData.ContaminationEffect effect : effects) {
            int currentIndex = index++;
            source.sendSuccess(() -> Component.literal("#" + currentIndex
                    + " maxRad=" + round(effect.maxRad())
                    + " maxTime=" + effect.maxTime()
                    + " time=" + effect.time()
                    + " currentRad=" + round(effect.currentRadiation())
                    + " ignoreArmor=" + effect.ignoreArmor()), false);
        }
        return effects.size();
    }

    private static int listContamination(CommandSourceStack source, Collection<ServerPlayer> players) {
        int entries = 0;
        for (ServerPlayer player : players) {
            entries += listContamination(source, player);
        }
        return entries;
    }

    private static int addContamination(CommandSourceStack source, Collection<ServerPlayer> players, float maxRad, int maxTime, int time, boolean ignoreArmor) {
        for (ServerPlayer player : players) {
            RadiationData.addContamination(player, maxRad, maxTime, time, ignoreArmor);
        }
        syncPlayers(players);
        source.sendSuccess(() -> Component.literal("Added contamination entry for " + players.size() + " player(s)."), true);
        return players.size();
    }

    private static int removeContamination(CommandSourceStack source, Collection<ServerPlayer> players, int index) {
        int removed = 0;
        for (ServerPlayer player : players) {
            if (RadiationData.removeContamination(player, index)) {
                removed++;
            }
        }
        syncPlayers(players);
        int removedCount = removed;
        source.sendSuccess(() -> Component.literal("Removed contamination entry #" + index + " from " + removedCount + " player(s)."), true);
        return removed;
    }

    private static int clearContamination(CommandSourceStack source, Collection<ServerPlayer> players) {
        int removed = 0;
        for (ServerPlayer player : players) {
            removed += RadiationData.clearContamination(player);
        }
        syncPlayers(players);
        int removedCount = removed;
        source.sendSuccess(() -> Component.literal("Removed " + removedCount + " contamination entrie(s)."), true);
        return removed;
    }

    private static boolean parseBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equals(value);
    }

    private static void syncPlayers(Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            CommonForgeEvents.syncRadiationNow(player);
        }
    }

    private static float round(float value) {
        return Math.round(value * 10.0F) / 10.0F;
    }

    private ModCommands() {
    }

    private enum StatusField {
        ASBESTOS("asbestos", player -> RadiationData.getAsbestos(player), (player, value) -> RadiationData.setAsbestos(player, value)),
        BLACKLUNG("blacklung", player -> RadiationData.getBlackLung(player), (player, value) -> RadiationData.setBlackLung(player, value)),
        BOMB_TIMER("bomb_timer", player -> RadiationData.getBombTimer(player), (player, value) -> RadiationData.setBombTimer(player, value)),
        CONTAGION("contagion", player -> RadiationData.getContagion(player), (player, value) -> RadiationData.setContagion(player, value)),
        OIL("oil", player -> RadiationData.getOil(player), (player, value) -> RadiationData.setOil(player, value)),
        FIRE("fire", player -> RadiationData.getFire(player), (player, value) -> RadiationData.setFire(player, value)),
        PHOSPHORUS("phosphorus", player -> RadiationData.getPhosphorus(player), (player, value) -> RadiationData.setPhosphorus(player, value)),
        BALEFIRE("balefire", player -> RadiationData.getBalefire(player), (player, value) -> RadiationData.setBalefire(player, value)),
        BLACK_FIRE("black_fire", player -> RadiationData.getBlackFire(player), (player, value) -> RadiationData.setBlackFire(player, value));

        private static final String[] NAMES = buildNames();

        private final String id;
        private final ToIntFunction<ServerPlayer> getter;
        private final BiConsumer<ServerPlayer, Integer> setter;

        StatusField(String id, ToIntFunction<ServerPlayer> getter, BiConsumer<ServerPlayer, Integer> setter) {
            this.id = id;
            this.getter = getter;
            this.setter = setter;
        }

        private int get(ServerPlayer player) {
            return getter.applyAsInt(player);
        }

        private void set(ServerPlayer player, int value) {
            setter.accept(player, Math.max(0, value));
        }

        private static StatusField byName(String name) {
            String normalized = name.toLowerCase(Locale.ROOT);
            for (StatusField field : values()) {
                if (field.id.equals(normalized) || field.id.replace("_", "").equals(normalized)) {
                    return field;
                }
            }
            return null;
        }

        private static String[] buildNames() {
            StatusField[] fields = values();
            String[] names = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                names[i] = fields[i].id;
            }
            return names;
        }
    }
}
