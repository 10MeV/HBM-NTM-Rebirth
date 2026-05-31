package com.hbm.ntm.command;

import com.hbm.ntm.api.tile.InfoProviderEC;
import com.hbm.ntm.api.redstoneoverradio.RORFunctionException;
import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.blockentity.HbmFluidBlockEntity;
import com.hbm.ntm.client.ClientBinaryData;
import com.hbm.ntm.client.ClientBiomeSyncData;
import com.hbm.ntm.client.ClientInformMessages;
import com.hbm.ntm.client.ClientMuzzleFlashEffects;
import com.hbm.ntm.client.ClientPanelData;
import com.hbm.ntm.client.ClientPermaSyncData;
import com.hbm.ntm.client.ClientPlayerSyncData;
import com.hbm.ntm.client.ClientRadiationData;
import com.hbm.ntm.client.ClientTileBinaryData;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.damage.DamageResistanceConfig;
import com.hbm.ntm.damage.DamageResistance;
import com.hbm.ntm.damage.DamageResistanceHandler;
import com.hbm.ntm.damage.DamageResistanceStats;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.energy.HbmEnergyDebug;
import com.hbm.ntm.energy.HbmEnergyNodespace;
import com.hbm.ntm.event.CommonForgeEvents;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidForgeMappings;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.LegacyNetworkDispatcher;
import com.hbm.ntm.network.ServerTileBinaryControlTransfers;
import com.hbm.ntm.network.ThreadedPacketDispatcher;
import com.hbm.ntm.network.packet.EntitySyncPacket;
import com.hbm.ntm.network.packet.TileSyncPacket;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.radiation.CraterRadiationData;
import com.hbm.ntm.radiation.LegacyFalloutConversions;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.radiation.RadiationConstants;
import com.hbm.ntm.radiation.RadiationData;
import com.hbm.ntm.radiation.RadiationSavedData;
import com.hbm.ntm.uninos.HbmNodespace;
import com.hbm.ntm.uninos.HbmUninosDiagnostics;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNodespace;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                                .then(Commands.literal("stats")
                                        .executes(context -> getChunkRadiationStats(context.getSource())))
                                .then(Commands.literal("prune")
                                        .executes(context -> pruneChunkRadiation(context.getSource())))
                                .then(Commands.literal("clear")
                                        .executes(context -> clearChunkRadiation(context.getSource()))))
                        .then(Commands.literal("crater")
                                .then(Commands.literal("stats")
                                        .executes(context -> getCraterRadiationStats(context.getSource())))
                                .then(Commands.literal("resync")
                                        .executes(context -> resyncCraterBiomes(context.getSource()))))
                        .then(Commands.literal("fallout")
                                .then(Commands.literal("status")
                                        .executes(context -> getFalloutTableStatus(context.getSource())))
                                .then(Commands.literal("reload")
                                        .executes(context -> reloadFalloutTable(context.getSource()))))
                        .then(Commands.literal("fog")
                                .executes(context -> spawnRadiationFog(context.getSource(), BlockPos.containing(context.getSource().getPosition())))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> spawnRadiationFog(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))))
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
                        .then(statusCommand())
                        .then(contaminationCommand()))
                .then(energyCommand())
                .then(damageCommand())
                .then(recipeCommand())
                .then(machineCommand())
                .then(fluidCommand())
                .then(networkCommand()));

        dispatcher.register(legacyPacketThreadingCommand("ntmpackets"));
        dispatcher.register(legacyPacketThreadingCommand("ntmpacket"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> legacyPacketThreadingCommand(String name) {
        return Commands.literal(name)
                .requires(source -> source.hasPermission(2))
                .executes(context -> getPacketThreadingStats(context.getSource()))
                .then(Commands.literal("info")
                        .executes(context -> getPacketThreadingStats(context.getSource())))
                .then(Commands.literal("resetState")
                        .executes(context -> resetPacketThreading(context.getSource())))
                .then(Commands.literal("toggleThreadingStatus")
                        .executes(context -> togglePacketThreading(context.getSource())))
                .then(Commands.literal("config")
                        .executes(context -> getPacketThreadingConfig(context.getSource())))
                .then(Commands.literal("clear")
                        .executes(context -> clearPacketThreading(context.getSource())));
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
                        .executes(context -> getEnergyNodespace(context.getSource()))
                        .then(Commands.literal("rebuild")
                                .executes(context -> rebuildEnergyNodespace(context.getSource()))))
                .then(Commands.literal("node")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> getEnergyNetwork(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                .then(Commands.literal("network")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> getEnergyNetwork(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                .then(Commands.literal("info")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> getEnergyInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                .then(Commands.literal("ror")
                        .then(Commands.literal("functions")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> getRorFunctions(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                        .then(Commands.literal("value")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{
                                                        RORInfo.PREFIX_VALUE + "fill",
                                                        RORInfo.PREFIX_VALUE + "fillpercent",
                                                        RORInfo.PREFIX_VALUE + "delta"
                                                }, builder))
                                                .executes(context -> getRorValue(
                                                        context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                        StringArgumentType.getString(context, "name"))))))
                        .then(Commands.literal("run")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("command", StringArgumentType.greedyString())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{
                                                        RORInfo.PREFIX_FUNCTION + "setmode" + RORInteractive.NAME_SEPARATOR + "0",
                                                        RORInfo.PREFIX_FUNCTION + "setredmode" + RORInteractive.NAME_SEPARATOR + "2",
                                                        RORInfo.PREFIX_FUNCTION + "setpriority" + RORInteractive.NAME_SEPARATOR + "1"
                                                }, builder))
                                                .executes(context -> runRorFunction(
                                                        context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                        StringArgumentType.getString(context, "command")))))))
                .then(Commands.literal("debug")
                        .then(Commands.literal("particles")
                                .executes(context -> toggleEnergyDebugParticles(context.getSource()))
                                .then(Commands.argument("enabled", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"false", "true"}, builder))
                                        .executes(context -> setEnergyDebugParticles(context.getSource(), parseBoolean(StringArgumentType.getString(context, "enabled")))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> damageCommand() {
        return Commands.literal("damage")
                .then(Commands.literal("resistance")
                        .then(Commands.literal("status")
                                .executes(context -> getDamageResistanceStatus(context.getSource())))
                        .then(Commands.literal("reload")
                                .executes(context -> reloadDamageResistanceConfig(context.getSource())))
                        .then(Commands.literal("damageTypes")
                                .executes(context -> auditDamageResistanceDamageTypes(context.getSource()))
                                .then(Commands.literal("list")
                                        .executes(context -> listDamageResistanceDamageTypes(context.getSource())))
                                .then(Commands.literal("resolve")
                                        .then(Commands.argument("damage_type", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(damageTypeSuggestions(), builder))
                                                .executes(context -> resolveDamageResistanceDamageType(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "damage_type"))))))
                        .then(Commands.literal("armor")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> inspectDamageResistanceArmor(
                                                context.getSource(),
                                                EntityArgument.getPlayers(context, "targets")))))
                        .then(Commands.literal("matches")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("damage_type", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(damageTypeSuggestions(), builder))
                                                .executes(context -> inspectDamageResistanceMatches(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        StringArgumentType.getString(context, "damage_type"))))))
                        .then(Commands.literal("probe")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("damage_type", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(damageTypeSuggestions(), builder))
                                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0D))
                                                        .executes(context -> probeDamageResistance(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "targets"),
                                                                StringArgumentType.getString(context, "damage_type"),
                                                                (float) DoubleArgumentType.getDouble(context, "amount"),
                                                                0.0F,
                                                                0.0F))
                                                        .then(Commands.argument("pierce_dt", DoubleArgumentType.doubleArg(0.0D))
                                                                .then(Commands.argument("pierce_dr", DoubleArgumentType.doubleArg(0.0D))
                                                                        .executes(context -> probeDamageResistance(
                                                                                context.getSource(),
                                                                                EntityArgument.getPlayers(context, "targets"),
                                                                                StringArgumentType.getString(context, "damage_type"),
                                                                                (float) DoubleArgumentType.getDouble(context, "amount"),
                                                                                (float) DoubleArgumentType.getDouble(context, "pierce_dt"),
                                                                                (float) DoubleArgumentType.getDouble(context, "pierce_dr"))))))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> fluidCommand() {
        return Commands.literal("fluid")
                .then(Commands.literal("nodespace")
                        .executes(context -> getFluidNodespace(context.getSource())))
                .then(Commands.literal("info")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> getFluidInfo(
                                        context.getSource(),
                                        BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                .then(Commands.literal("node")
                        .then(fluidNetworkArgument()))
                .then(Commands.literal("network")
                        .then(fluidNetworkArgument()))
                .then(Commands.literal("pipe")
                        .then(Commands.literal("set")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("fluid", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        HbmFluids.all().stream().map(FluidType::toPath), builder))
                                                .executes(context -> setFluidPipeType(
                                                        context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                        parseFluid(StringArgumentType.getString(context, "fluid"))))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> networkCommand() {
        return Commands.literal("network")
                .then(Commands.literal("uninos")
                        .executes(context -> getUninosNodespace(context.getSource())))
                .then(Commands.literal("pneumatic")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> getPneumaticNetwork(
                                        context.getSource(),
                                        BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                .then(Commands.literal("protocol")
                        .executes(context -> getNetworkProtocolSummary(context.getSource()))
                        .then(Commands.literal("summary")
                                .executes(context -> getNetworkProtocolSummary(context.getSource())))
                        .then(Commands.literal("progress")
                                .executes(context -> getNetworkProtocolProgress(context.getSource())))
                        .then(Commands.literal("packets")
                                .executes(context -> listNetworkProtocolPackets(context.getSource())))
                        .then(Commands.literal("audit")
                                .executes(context -> auditNetworkProtocol(context.getSource())))
                        .then(Commands.literal("mappings")
                                .executes(context -> listNetworkProtocolMappings(context.getSource())))
                        .then(Commands.literal("legacy")
                                .executes(context -> listLegacyNetworkProtocolPackets(context.getSource()))
                                .then(Commands.argument("packet", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                ModMessages.legacyPacketRegistrations().stream()
                                                        .map(ModMessages.LegacyPacketRegistration::legacyName),
                                                builder))
                                        .executes(context -> queryLegacyNetworkPacket(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "packet")))))
                        .then(Commands.literal("modern")
                                .then(Commands.argument("packet", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                ModMessages.packetRegistrations().stream()
                                                        .map(ModMessages.PacketRegistration::typeName),
                                                builder))
                                        .executes(context -> queryModernNetworkPacket(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "packet")))))
                        .then(Commands.literal("wrapper")
                                .executes(context -> getNetworkLegacyWrapper(context.getSource()))))
                .then(Commands.literal("packetthreading")
                        .then(Commands.literal("stats")
                                .executes(context -> getPacketThreadingStats(context.getSource())))
                        .then(Commands.literal("info")
                                .executes(context -> getPacketThreadingStats(context.getSource())))
                        .then(Commands.literal("config")
                                .executes(context -> getPacketThreadingConfig(context.getSource())))
                        .then(Commands.literal("threads")
                                .executes(context -> listPacketThreadingThreads(context.getSource())))
                        .then(Commands.literal("toggle")
                                .executes(context -> togglePacketThreading(context.getSource())))
                        .then(Commands.literal("toggleThreadingStatus")
                                .executes(context -> togglePacketThreading(context.getSource())))
                        .then(Commands.literal("enable")
                                .executes(context -> setPacketThreading(context.getSource(), true)))
                        .then(Commands.literal("disable")
                                .executes(context -> setPacketThreading(context.getSource(), false)))
                        .then(Commands.literal("clear")
                                .executes(context -> clearPacketThreading(context.getSource())))
                        .then(Commands.literal("reset")
                                .executes(context -> resetPacketThreading(context.getSource())))
                        .then(Commands.literal("resetState")
                                .executes(context -> resetPacketThreading(context.getSource()))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> machineCommand() {
        return Commands.literal("machine")
                .then(Commands.literal("assembly")
                        .then(Commands.literal("recipes")
                                .executes(context -> listAssemblyRecipes(context.getSource())))
                        .then(Commands.literal("info")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> getAssemblyInfo(
                                                context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("recipe", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        GenericMachineRecipeRuntime.recipeNames(
                                                                context.getSource().getLevel(),
                                                                GenericMachineRecipe.Machine.ASSEMBLY_MACHINE),
                                                        builder))
                                                .executes(context -> setAssemblyRecipe(
                                                        context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                        StringArgumentType.getString(context, "recipe"))))))
                        .then(Commands.literal("clear")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> clearAssemblyRecipe(
                                                context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "pos"))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> recipeCommand() {
        return Commands.literal("recipe")
                .then(Commands.literal("audit")
                        .executes(context -> auditRecipes(context.getSource(), null))
                        .then(Commands.argument("machine", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(recipeMachineNames(), builder))
                                .executes(context -> auditRecipesByName(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "machine")))))
                .then(Commands.literal("unresolved")
                        .executes(context -> listUnresolvedRecipeInputs(context.getSource(), null))
                        .then(Commands.argument("machine", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(recipeMachineNames(), builder))
                                .executes(context -> listUnresolvedRecipeInputsByName(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "machine")))))
                .then(Commands.literal("legacyMeta")
                        .executes(context -> listLegacyMetaMappings(context.getSource(), null))
                        .then(Commands.argument("legacyId", StringArgumentType.string())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        LegacyMetaItemMappings.legacyIds().stream().map(ResourceLocation::toString),
                                        builder))
                                .executes(context -> listLegacyMetaMappings(
                                        context.getSource(),
                                        new ResourceLocation(StringArgumentType.getString(context, "legacyId"))))))
                .then(Commands.literal("legacyOre")
                        .then(Commands.argument("oreName", StringArgumentType.word())
                                .executes(context -> queryLegacyOreMapping(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "oreName")))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> fluidNetworkArgument() {
        return Commands.argument("pos", BlockPosArgument.blockPos())
                .then(Commands.argument("fluid", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                HbmFluids.all().stream().map(FluidType::toPath), builder))
                        .executes(context -> getFluidNetwork(
                                context.getSource(),
                                BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                parseFluid(StringArgumentType.getString(context, "fluid")))));
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

    private static int getChunkRadiationStats(CommandSourceStack source) {
        RadiationSavedData.Stats stats = ChunkRadiationManager.getStats(source.getLevel());
        source.sendSuccess(() -> Component.literal("Chunk radiation stats: entries=" + stats.totalEntries()
                + " loaded=" + stats.loadedEntries()
                + " positive=" + stats.positiveEntries()
                + " loadedPositive=" + stats.loadedPositiveEntries()
                + " totalRad=" + round(stats.totalRadiation())
                + " loadedRad=" + round(stats.loadedRadiation())
                + " maxRad=" + round(stats.maxRadiation())
                + " loadedMaxRad=" + round(stats.loadedMaxRadiation())), false);
        return stats.totalEntries();
    }

    private static int pruneChunkRadiation(CommandSourceStack source) {
        int removed = ChunkRadiationManager.pruneUnloaded(source.getLevel());
        source.sendSuccess(() -> Component.literal("Pruned " + removed + " unloaded chunk radiation entrie(s)."), true);
        return removed;
    }

    private static int getCraterRadiationStats(CommandSourceStack source) {
        CraterRadiationData.Stats stats = CraterRadiationData.getStats(source.getLevel());
        source.sendSuccess(() -> Component.literal("Crater radiation markers: entries=" + stats.totalMarkers()
                + " loaded=" + stats.loadedMarkers()
                + " outer=" + stats.outerMarkers()
                + " crater=" + stats.craterMarkers()
                + " inner=" + stats.innerMarkers()
                + " loadedOuter=" + stats.loadedOuterMarkers()
                + " loadedCrater=" + stats.loadedCraterMarkers()
                + " loadedInner=" + stats.loadedInnerMarkers()), false);
        return stats.totalMarkers();
    }

    private static int resyncCraterBiomes(CommandSourceStack source) {
        CraterRadiationData.ResyncResult result = CraterRadiationData.resyncLoadedBiomes(source.getLevel());
        source.sendSuccess(() -> Component.literal("Crater biome resync: markers=" + result.totalMarkers()
                + " loaded=" + result.loadedMarkers()
                + " changedCells=" + result.changedCells()
                + " changedChunks=" + result.changedChunks()), true);
        return result.changedCells();
    }

    private static int getFalloutTableStatus(CommandSourceStack source) {
        LegacyFalloutConversions.LoadReport report = LegacyFalloutConversions.loadReport();
        source.sendSuccess(() -> Component.literal("Fallout conversions: " + report.summary()
                + " config=" + String.valueOf(report.configFile())), false);
        for (String warning : report.warnings()) {
            source.sendSuccess(() -> Component.literal("Fallout warning: " + warning), false);
        }
        return report.entryCount();
    }

    private static int reloadFalloutTable(CommandSourceStack source) {
        LegacyFalloutConversions.LoadReport report = LegacyFalloutConversions.initialize(FMLPaths.CONFIGDIR.get());
        source.sendSuccess(() -> Component.literal("Reloaded " + report.summary()), true);
        for (String warning : report.warnings()) {
            source.sendFailure(Component.literal("Fallout warning: " + warning));
        }
        return report.entryCount();
    }

    private static int getDamageResistanceStatus(CommandSourceStack source) {
        DamageResistanceConfig.LoadReport report = DamageResistanceConfig.loadReport();
        DamageResistanceHandler.RegistrySnapshot snapshot = DamageResistanceHandler.registrySnapshot();
        source.sendSuccess(() -> Component.literal("Damage resistance: " + report.summary()), false);
        source.sendSuccess(() -> Component.literal("Damage resistance registry: itemStats=" + snapshot.itemStats()
                + " armorSets=" + snapshot.setStats()
                + " entityStats=" + snapshot.entityStats()
                + " (class=" + snapshot.entityClassStats()
                + ", simpleName=" + snapshot.entitySimpleNameStats() + ")"), false);
        source.sendSuccess(() -> Component.literal("Damage resistance runtime: allowSpecialCancel="
                + EntityDamageUtil.allowSpecialCancel()
                + " currentPierceDT=" + round(DamageResistanceHandler.currentPierceDt())
                + " currentPierceDR=" + round(DamageResistanceHandler.currentPierceDr() * 100.0F) + "%"), false);
        for (String warning : report.warnings()) {
            source.sendSuccess(() -> Component.literal("Damage resistance warning: missing migrated " + warning), false);
        }
        return report.itemStats() + report.setStats() + report.entityStats();
    }

    private static int reloadDamageResistanceConfig(CommandSourceStack source) {
        DamageResistanceConfig.LoadReport report = DamageResistanceConfig.initialize(FMLPaths.CONFIGDIR.get());
        source.sendSuccess(() -> Component.literal("Reloaded " + report.summary()), true);
        for (String warning : report.warnings()) {
            source.sendFailure(Component.literal("Damage resistance warning: missing migrated " + warning));
        }
        return report.itemStats() + report.setStats() + report.entityStats();
    }

    private static int auditDamageResistanceDamageTypes(CommandSourceStack source) {
        int problems = 0;
        var registry = source.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        for (ModDamageSources.LegacyDamageType legacy : ModDamageSources.legacyDamageTypes()) {
            if (!registry.containsKey(legacy.location())) {
                problems++;
                source.sendFailure(Component.literal("Missing HBM damage type: " + legacy.location()));
                continue;
            }
            DamageSource damageSource = new DamageSource(registry.getHolderOrThrow(legacy.key()));
            problems += checkDamageTypeTag(source, legacy, "projectile", legacy.projectile(), damageSource.is(DamageTypeTags.IS_PROJECTILE));
            problems += checkDamageTypeTag(source, legacy, "explosion", legacy.explosion(), damageSource.is(DamageTypeTags.IS_EXPLOSION));
            problems += checkDamageTypeTag(source, legacy, "fire", legacy.fire(), damageSource.is(DamageTypeTags.IS_FIRE));
            problems += checkDamageTypeTag(source, legacy, "bypassesArmor", legacy.bypassesArmor(), damageSource.is(DamageTypeTags.BYPASSES_ARMOR));
            problems += checkDamageTypeTag(source, legacy, "absolute", legacy.absolute(), damageSource.is(DamageTypeTags.BYPASSES_RESISTANCE));
            problems += checkDamageTypeTag(source, legacy, "creativeAllowed", legacy.creativeAllowed(), damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY));
        }
        int totalProblems = problems;
        source.sendSuccess(() -> Component.literal("HBM damage type audit: entries="
                + ModDamageSources.legacyDamageTypes().size()
                + " problems=" + totalProblems), totalProblems > 0);
        return totalProblems == 0 ? 1 : totalProblems;
    }

    private static int checkDamageTypeTag(CommandSourceStack source, ModDamageSources.LegacyDamageType legacy,
            String name, boolean expected, boolean actual) {
        if (expected == actual) {
            return 0;
        }
        source.sendFailure(Component.literal("Damage type tag mismatch: " + legacy.location()
                + " " + name + " expected=" + expected + " actual=" + actual));
        return 1;
    }

    private static int listDamageResistanceDamageTypes(CommandSourceStack source) {
        var registry = source.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        for (ModDamageSources.LegacyDamageType legacy : ModDamageSources.legacyDamageTypes()) {
            boolean present = registry.containsKey(legacy.location());
            List<String> aliases = ModDamageSources.legacyAliases(legacy.key());
            source.sendSuccess(() -> Component.literal(legacy.location()
                    + " present=" + present
                    + " projectile=" + legacy.projectile()
                    + " explosion=" + legacy.explosion()
                    + " fire=" + legacy.fire()
                    + " bypassesArmor=" + legacy.bypassesArmor()
                    + " absolute=" + legacy.absolute()
                    + " creativeAllowed=" + legacy.creativeAllowed()
                    + (aliases.isEmpty() ? "" : " aliases=" + String.join(",", aliases))), false);
        }
        return ModDamageSources.legacyDamageTypes().size();
    }

    private static int resolveDamageResistanceDamageType(CommandSourceStack source, String damageType) {
        Optional<ResourceKey<DamageType>> resolved = ModDamageSources.legacyKey(damageType);
        if (resolved.isEmpty()) {
            source.sendFailure(Component.literal("Unknown damage type alias: " + damageType));
            return 0;
        }

        ResourceKey<DamageType> key = resolved.get();
        boolean present = source.getLevel().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .containsKey(key.location());
        source.sendSuccess(() -> Component.literal("Damage type alias '" + damageType + "' -> "
                + key.location()
                + " present=" + present
                + " aliases=" + String.join(",", ModDamageSources.legacyAliases(key))), false);
        return present ? 1 : 0;
    }

    private static int inspectDamageResistanceArmor(CommandSourceStack source, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            DamageResistanceHandler.ArmorBreakdown breakdown = DamageResistanceHandler.armorBreakdown(player);
            source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " damage resistance armor:"), false);
            for (DamageResistanceHandler.ArmorSlotBreakdown slot : breakdown.slots()) {
                String item = slot.stack().isEmpty() ? "empty" : itemId(slot.stack());
                source.sendSuccess(() -> Component.literal("  " + slot.slot().getName()
                        + "=" + item
                        + " itemStats=" + summarizeStats(slot.itemStats())), false);
            }
            source.sendSuccess(() -> Component.literal("  matchedSet=" + summarizeStats(breakdown.setStats())), false);
            source.sendSuccess(() -> Component.literal("  innate[" + breakdown.innateKey() + "]="
                    + summarizeStats(breakdown.innateStats())), false);
        }
        return players.size();
    }

    private static int inspectDamageResistanceMatches(CommandSourceStack source, Collection<ServerPlayer> players, String damageType) {
        ResourceLocation location = parseDamageTypeLocation(damageType);
        ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, location);
        if (!source.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).containsKey(location)) {
            source.sendFailure(Component.literal("Unknown damage type '" + damageType + "'."));
            return 0;
        }

        DamageSource damageSource = new DamageSource(source.getLevel().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(key), source.getEntity());
        for (ServerPlayer player : players) {
            List<DamageResistanceHandler.ResistanceContribution> contributions =
                    DamageResistanceHandler.resistanceContributions(player, damageSource, 1.0F, 0.0F, 0.0F);
            source.sendSuccess(() -> Component.literal(player.getGameProfile().getName()
                    + " damage resistance matches: type=" + location
                    + " exact=" + DamageResistanceHandler.exactTypeKey(damageSource)
                    + " category=" + DamageResistanceHandler.typeToCategory(damageSource)), false);
            if (contributions.isEmpty()) {
                source.sendSuccess(() -> Component.literal("  none"), false);
            } else {
                for (DamageResistanceHandler.ResistanceContribution contribution : contributions) {
                    source.sendSuccess(() -> Component.literal("  " + contribution.source()
                            + "[" + contribution.id() + "] "
                            + contribution.matchKind() + ":" + contribution.matchKey()
                            + " DT=" + round(contribution.threshold())
                            + " DR=" + round(contribution.resistance() * 100.0F) + "%"), false);
                }
            }
        }
        return players.size();
    }

    private static int probeDamageResistance(CommandSourceStack source, Collection<ServerPlayer> players, String damageType,
                                             float amount, float pierceDt, float pierceDr) {
        ResourceLocation location = parseDamageTypeLocation(damageType);
        ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, location);
        if (!source.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).containsKey(location)) {
            source.sendFailure(Component.literal("Unknown damage type '" + damageType + "'."));
            return 0;
        }

        DamageSource damageSource = new DamageSource(source.getLevel().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(key), source.getEntity());
        for (ServerPlayer player : players) {
            DamageResistanceHandler.ResistanceBreakdown breakdown = DamageResistanceHandler.breakdown(player, damageSource, amount, pierceDt, pierceDr);
            source.sendSuccess(() -> Component.literal(player.getGameProfile().getName()
                    + " damage resistance probe: type=" + location
                    + " exact=" + breakdown.exactType()
                    + " category=" + breakdown.category()
                    + " amount=" + round(amount)
                    + " DT=" + round(breakdown.rawDt())
                    + " DR=" + round(breakdown.rawDr() * 100.0F) + "%"
                    + " pierceDT=" + round(pierceDt)
                    + " pierceDR=" + round(pierceDr * 100.0F) + "%"
                    + " effectiveDT=" + round(breakdown.effectiveDt())
                    + " effectiveDR=" + round(breakdown.effectiveDr() * 100.0F) + "%"
                    + " result=" + round(breakdown.finalDamage())), false);
            List<DamageResistanceHandler.ResistanceContribution> contributions =
                    DamageResistanceHandler.resistanceContributions(player, damageSource, amount, pierceDt, pierceDr);
            if (contributions.isEmpty()) {
                source.sendSuccess(() -> Component.literal("  contributions: none"), false);
            } else {
                for (DamageResistanceHandler.ResistanceContribution contribution : contributions) {
                    source.sendSuccess(() -> Component.literal("  " + contribution.source()
                            + "[" + contribution.id() + "] "
                            + contribution.matchKind() + ":" + contribution.matchKey()
                            + " DT=" + round(contribution.threshold())
                            + " DR=" + round(contribution.resistance() * 100.0F) + "%"), false);
                }
            }
        }
        return players.size();
    }

    private static String itemId(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? stack.getHoverName().getString() : id.toString();
    }

    private static String summarizeStats(@Nullable DamageResistanceStats stats) {
        if (stats == null) {
            return "none";
        }
        List<String> parts = new ArrayList<>();
        for (var entry : stats.categoryResistances().entrySet()) {
            parts.add(entry.getKey() + "=" + summarizeResistance(entry.getValue()));
        }
        for (var entry : stats.exactResistances().entrySet()) {
            parts.add(entry.getKey() + "=" + summarizeResistance(entry.getValue()));
        }
        DamageResistance other = stats.otherResistance();
        if (other != null) {
            parts.add("other=" + summarizeResistance(other));
        }
        return parts.isEmpty() ? "empty" : String.join(", ", parts);
    }

    private static String summarizeResistance(DamageResistance resistance) {
        return round(resistance.threshold()) + "/" + round(resistance.resistance() * 100.0F) + "%";
    }

    private static ResourceLocation parseDamageTypeLocation(String damageType) {
        return ModDamageSources.legacyKey(damageType)
                .map(ResourceKey::location)
                .orElseGet(() -> damageType.contains(":")
                        ? new ResourceLocation(damageType)
                        : new ResourceLocation(damageType));
    }

    private static String[] damageTypeSuggestions() {
        List<String> suggestions = new ArrayList<>(List.of(
                "minecraft:generic",
                "minecraft:player_attack",
                "minecraft:on_fire",
                "minecraft:fall",
                "hbm:explosion",
                "hbm:nuclear_blast",
                "hbm:laser",
                "hbm:plasma",
                "hbm:microwave",
                "hbm:electric",
                "nuclearBlast",
                "mudPoisoning",
                "tauBlast",
                "revolverBullet",
                "chopperBullet",
                "cmb",
                "subAtomic3",
                "electrified",
                "acidPlayer"));
        for (ModDamageSources.LegacyDamageType legacy : ModDamageSources.legacyDamageTypes()) {
            suggestions.add(legacy.location().toString());
            suggestions.add(legacy.location().getPath());
            suggestions.addAll(ModDamageSources.legacyAliases(legacy.key()));
        }
        return suggestions.stream().distinct().toArray(String[]::new);
    }

    private static int spawnRadiationFog(CommandSourceStack source, BlockPos pos) {
        ChunkRadiationManager.spawnDebugRadiationFog(source.getLevel(), pos);
        source.sendSuccess(() -> Component.literal("Spawned radiation fog at " + pos.toShortString()), false);
        return 1;
    }

    private static int getEnergyNodespace(CommandSourceStack source) {
        HbmEnergyNodespace.Diagnostics diagnostics = HbmEnergyNodespace.getDiagnostics(source.getLevel());
        source.sendSuccess(() -> Component.literal("Energy nodespace: positions=" + diagnostics.nodePositions()
                + " nodes=" + diagnostics.uniqueNodes()
                + " networks=" + diagnostics.networks()
                + " invalidNetworks=" + diagnostics.invalidNetworks()
                + " linkRefs=" + diagnostics.linkRefs()
                + " dirtyNodes=" + diagnostics.dirtyNodes()
                + " expiredNodes=" + diagnostics.expiredNodes()
                + " orphanNodes=" + diagnostics.orphanNodes()
                + " providers=" + diagnostics.providerEntries()
                + " receivers=" + diagnostics.receiverEntries()
                + " reapTimer=" + diagnostics.reapTimer()
                + " debugParticles=" + HbmEnergyDebug.isParticleDebugEnabled()), false);
        return diagnostics.uniqueNodes();
    }

    private static int rebuildEnergyNodespace(CommandSourceStack source) {
        HbmEnergyNodespace.ForceRebuildResult result = HbmEnergyNodespace.forceRebuild(source.getLevel());
        source.sendSuccess(() -> Component.literal("Energy nodespace rebuilt: nodes=" + result.nodes()
                + " oldNetworks=" + result.oldNetworks()
                + " newNetworks=" + result.newNetworks()
                + " reapTimer=" + result.reapTimer()), true);
        return result.newNetworks();
    }

    private static int getEnergyNetwork(CommandSourceStack source, BlockPos pos) {
        HbmEnergyNodespace.NetworkDebugSnapshot snapshot = HbmEnergyNodespace.getNetworkDebugSnapshot(source.getLevel(), pos);
        if (!snapshot.nodePresent()) {
            source.sendFailure(Component.literal("No HBM energy node at " + pos.toShortString()));
            return 0;
        }
        if (!snapshot.networkPresent() || snapshot.network() == null) {
            source.sendSuccess(() -> Component.literal("Energy node at " + pos.toShortString()
                    + ": nodeConnections=" + snapshot.nodeConnections()
                    + " recentlyChanged=" + snapshot.recentlyChanged()
                    + " network=none"), false);
            return 0;
        }
        var network = snapshot.network();
        source.sendSuccess(() -> Component.literal("Energy network at " + pos.toShortString()
                + ": valid=" + network.valid()
                + " nodeConnections=" + snapshot.nodeConnections()
                + " recentlyChanged=" + snapshot.recentlyChanged()
                + " links=" + network.links()
                + " providers=" + network.providers()
                + " receivers=" + network.receivers()
                + " providerPower=" + network.providerPower() + " HE"
                + " providerRate=" + network.providerRate() + " HE/t"
                + " receiverDemand=" + network.receiverDemand() + " HE"
                + " receiverRate=" + network.receiverRate() + " HE/t"
                + " receiverPriorities=" + network.receiversByPriority()
                + " timeout=" + network.timeoutMs() + "ms"
                + " oldestProvider=" + network.oldestProviderAgeMs() + "ms"
                + " oldestReceiver=" + network.oldestReceiverAgeMs() + "ms"
                + " lastTransfer=" + network.lastTransfer() + " HE"), false);
        return network.links();
    }

    private static int getEnergyInfo(CommandSourceStack source, BlockPos pos) {
        BlockEntity blockEntity = CompatEnergyControl.findTileEntity(source.getLevel(), pos);
        if (!(blockEntity instanceof InfoProviderEC)
                && CompatEnergyControl.getAllTanks(blockEntity) == null
                && CompatEnergyControl.getHeat(blockEntity) < 0) {
            source.sendFailure(Component.literal("No HBM EnergyControl info provider at " + pos.toShortString()));
            return 0;
        }
        CompoundTag data = new CompoundTag();
        CompatEnergyControl.getEnergyData(blockEntity, data);
        CompatEnergyControl.getExtraData(blockEntity, data);
        List<Object[]> tanks = CompatEnergyControl.getAllTanks(blockEntity);
        int heat = CompatEnergyControl.getHeat(blockEntity);
        source.sendSuccess(() -> Component.literal("Energy info at " + pos.toShortString()
                + ": " + formatEnergyControlInfo(data)
                + " heat=" + heat
                + " tanks=" + formatEnergyControlTanks(tanks)), false);
        return data.size();
    }

    private static String formatEnergyControlInfo(CompoundTag data) {
        List<String> keys = new ArrayList<>(data.getAllKeys());
        Collections.sort(keys);
        List<String> entries = new ArrayList<>();
        for (String key : keys) {
            entries.add(key + "=" + data.get(key));
        }
        return "{ " + String.join(", ", entries) + " }";
    }

    private static String formatEnergyControlTanks(List<Object[]> tanks) {
        if (tanks == null || tanks.isEmpty()) {
            return "[]";
        }
        List<String> entries = new ArrayList<>();
        for (Object[] tank : tanks) {
            String fluid = String.valueOf(tank[0]);
            ResourceLocation texture = CompatEnergyControl.getFluidTexture(fluid);
            entries.add("{ fluid=" + fluid
                    + ", fill=" + tank[1]
                    + ", capacity=" + tank[2]
                    + ", texture=" + texture
                    + " }");
        }
        return "[" + String.join(", ", entries) + "]";
    }

    private static int getRorFunctions(CommandSourceStack source, BlockPos pos) {
        BlockEntity blockEntity = source.getLevel().getBlockEntity(pos);
        if (!(blockEntity instanceof RORInfo info)) {
            source.sendFailure(Component.literal("No HBM ROR component at " + pos.toShortString()));
            return 0;
        }
        String[] functions = info.getFunctionInfo();
        source.sendSuccess(() -> Component.literal("ROR functions at " + pos.toShortString() + ": " + String.join(", ", functions)), false);
        return functions.length;
    }

    private static int getRorValue(CommandSourceStack source, BlockPos pos, String name) {
        BlockEntity blockEntity = source.getLevel().getBlockEntity(pos);
        if (!(blockEntity instanceof RORValueProvider provider)) {
            source.sendFailure(Component.literal("No HBM ROR value provider at " + pos.toShortString()));
            return 0;
        }
        String value = provider.provideRORValue(name);
        if (value == null) {
            source.sendFailure(Component.literal("No ROR value '" + name + "' at " + pos.toShortString()));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("ROR value " + name + " at " + pos.toShortString() + ": " + value), false);
        return 1;
    }

    private static int runRorFunction(CommandSourceStack source, BlockPos pos, String command) {
        BlockEntity blockEntity = source.getLevel().getBlockEntity(pos);
        if (!(blockEntity instanceof RORInteractive interactive)) {
            source.sendFailure(Component.literal("No HBM ROR interactive component at " + pos.toShortString()));
            return 0;
        }
        try {
            String name = RORInteractive.getCommand(command);
            String[] params = RORInteractive.getParams(command);
            String result = interactive.runRORFunction(name, params);
            source.sendSuccess(() -> Component.literal("ROR function " + name + " at " + pos.toShortString()
                    + (result == null ? ": ok" : ": " + result)), true);
            return 1;
        } catch (RORFunctionException ex) {
            source.sendFailure(Component.literal(ex.getMessage()));
            return 0;
        }
    }

    private static int getFluidNodespace(CommandSourceStack source) {
        int nodes = HbmFluidNodespace.getNodeCount(source.getLevel());
        int networks = HbmFluidNodespace.getNetworkCount(source.getLevel());
        source.sendSuccess(() -> Component.literal("Fluid nodespace: nodes=" + nodes + " networks=" + networks), false);
        return nodes;
    }

    private static int getFluidNetwork(CommandSourceStack source, BlockPos pos, FluidType type) {
        if (type == HbmFluids.NONE) {
            source.sendFailure(Component.literal("Unknown or empty HBM fluid type."));
            return 0;
        }
        HbmFluidNodespace.NetworkDebugSnapshot snapshot = HbmFluidNodespace.getNetworkDebugSnapshot(source.getLevel(), pos, type);
        if (!snapshot.nodePresent()) {
            source.sendFailure(Component.literal("No HBM fluid node for ")
                    .append(type.getDisplayName())
                    .append(" at " + pos.toShortString()));
            return 0;
        }
        if (!snapshot.networkPresent() || snapshot.network() == null) {
            source.sendSuccess(() -> Component.literal("Fluid node at " + pos.toShortString()
                    + " type=" + snapshot.fluid()
                    + ": nodeConnections=" + snapshot.nodeConnections()
                    + " recentlyChanged=" + snapshot.recentlyChanged()
                    + " network=none"), false);
            return 0;
        }
        var network = snapshot.network();
        source.sendSuccess(() -> Component.literal("Fluid network at " + pos.toShortString()
                + " type=" + network.fluid()
                + ": valid=" + network.valid()
                + " nodeConnections=" + snapshot.nodeConnections()
                + " recentlyChanged=" + snapshot.recentlyChanged()
                + " links=" + network.links()
                + " providers=" + network.providers()
                + " receivers=" + network.receivers()
                + " providerAvailable=" + formatPressureArray(network.providerAvailable()) + " mB"
                + " providerRate=" + formatPressureArray(network.providerRate()) + " mB/t"
                + " receiverDemand=" + formatPressureArray(network.receiverDemand()) + " mB"
                + " receiverRate=" + formatPressureArray(network.receiverRate()) + " mB/t"
                + " receiverPriorities=" + network.receiversByPriority()
                + " lastTransfer=" + network.lastTransfer() + " mB"), false);
        return network.links();
    }

    private static int getFluidInfo(CommandSourceStack source, BlockPos pos) {
        BlockEntity blockEntity = CompatEnergyControl.findTileEntity(source.getLevel(), pos);
        if (blockEntity == null) {
            source.sendFailure(Component.literal("No block entity at " + pos.toShortString()));
            return 0;
        }

        int hbmTanks = 0;
        if (blockEntity instanceof HbmFluidBlockEntity fluidBlockEntity) {
            List<HbmFluidTank> tanks = fluidBlockEntity.getAllTanks();
            hbmTanks = tanks.size();
            source.sendSuccess(() -> Component.literal("HBM fluid tanks at " + pos.toShortString()
                    + ": " + tanks.size()), false);
            for (int i = 0; i < tanks.size(); i++) {
                int tankIndex = i;
                HbmFluidTank tank = tanks.get(i);
                source.sendSuccess(() -> formatHbmTank(tankIndex, tank), false);
            }
        }

        List<String> capabilitySides = getFluidCapabilitySides(blockEntity);
        FluidHandlerLookup handlerLookup = getFluidHandler(blockEntity);
        if (handlerLookup.handler() == null) {
            if (hbmTanks == 0) {
                source.sendFailure(Component.literal("No HBM fluid tanks or Forge fluid handler at " + pos.toShortString()));
                return 0;
            }
            source.sendSuccess(() -> Component.literal("Forge fluid capability: none"), false);
            return hbmTanks;
        }

        IFluidHandler handler = handlerLookup.handler();
        source.sendSuccess(() -> Component.literal("Forge fluid capability: sample=" + handlerLookup.source()
                + " sides=" + capabilitySides
                + " tanks=" + handler.getTanks()), false);
        for (int i = 0; i < handler.getTanks(); i++) {
            int tankIndex = i;
            FluidStack stack = handler.getFluidInTank(i);
            int capacity = handler.getTankCapacity(i);
            source.sendSuccess(() -> formatForgeTank(tankIndex, stack, capacity), false);
        }
        return hbmTanks + handler.getTanks();
    }

    private static int getUninosNodespace(CommandSourceStack source) {
        HbmUninosDiagnostics.Totals totals = HbmUninosDiagnostics.totals(source.getLevel());
        source.sendSuccess(() -> Component.literal("UNINOS nodespace: positions=" + totals.nodePositions()
                + " nodes=" + totals.uniqueNodes()
                + " networks=" + totals.networks()
                + " invalidNetworks=" + totals.invalidNetworks()
                + " linkRefs=" + totals.linkRefs()
                + " dirtyNodes=" + totals.dirtyNodes()
                + " expiredNodes=" + totals.expiredNodes()
                + " orphanNodes=" + totals.orphanNodes()
                + " providers=" + totals.providers()
                + " receivers=" + totals.receivers()), false);
        for (HbmUninosDiagnostics.Entry entry : HbmUninosDiagnostics.collect(source.getLevel())) {
            HbmNodespace.Diagnostics core = entry.core();
            source.sendSuccess(() -> Component.literal(" - " + entry.name()
                    + ": positions=" + core.nodePositions()
                    + " nodes=" + core.uniqueNodes()
                    + " networks=" + core.networks()
                    + " links=" + core.linkRefs()
                    + " dirty=" + core.dirtyNodes()
                    + " orphan=" + core.orphanNodes()
                    + " providers=" + entry.providers()
                    + " receivers=" + entry.receivers()), false);
        }
        return totals.uniqueNodes();
    }

    private static int getPneumaticNetwork(CommandSourceStack source, BlockPos pos) {
        PneumaticNodespace.NetworkDebugSnapshot snapshot = PneumaticNodespace.getNetworkDebugSnapshot(source.getLevel(), pos);
        if (!snapshot.nodePresent()) {
            source.sendFailure(Component.literal("No HBM pneumatic node at " + pos.toShortString()));
            return 0;
        }
        if (!snapshot.networkPresent() || snapshot.network() == null) {
            source.sendSuccess(() -> Component.literal("Pneumatic node at " + pos.toShortString()
                    + ": nodeConnections=" + snapshot.nodeConnections()
                    + " recentlyChanged=" + snapshot.recentlyChanged()
                    + " network=none"), false);
            return 0;
        }
        var network = snapshot.network();
        source.sendSuccess(() -> Component.literal("Pneumatic network at " + pos.toShortString()
                + ": valid=" + network.valid()
                + " nodeConnections=" + snapshot.nodeConnections()
                + " recentlyChanged=" + snapshot.recentlyChanged()
                + " links=" + network.links()
                + " receivers=" + network.receivers()
                + " accessors=" + network.accessors()
                + " storages=" + network.storages()
                + " timeout=" + network.timeoutMs() + "ms"
                + " lastTransfer=" + network.lastTransfer() + " items"), false);
        return network.links();
    }

    private static int setFluidPipeType(CommandSourceStack source, BlockPos pos, FluidType type) {
        if (type == HbmFluids.NONE) {
            source.sendFailure(Component.literal("Unknown or empty HBM fluid type."));
            return 0;
        }
        BlockEntity blockEntity = source.getLevel().getBlockEntity(pos);
        if (!(blockEntity instanceof FluidPipeBlockEntity pipe)) {
            source.sendFailure(Component.literal("No HBM fluid pipe at " + pos.toShortString()));
            return 0;
        }
        pipe.setFluidType(type);
        source.sendSuccess(() -> Component.literal("Fluid pipe at " + pos.toShortString()
                + " set to ").append(type.getDisplayName()), true);
        return 1;
    }

    private static int listAssemblyRecipes(CommandSourceStack source) {
        List<String> recipes = GenericMachineRecipeRuntime.recipeNames(source.getLevel(), GenericMachineRecipe.Machine.ASSEMBLY_MACHINE);
        source.sendSuccess(() -> Component.literal("Assembly recipes (" + recipes.size() + "): " + String.join(", ", recipes)), false);
        return recipes.size();
    }

    private static int auditRecipesByName(CommandSourceStack source, String machineName) {
        GenericMachineRecipe.Machine machine = parseRecipeMachine(machineName);
        if (machine == null) {
            source.sendFailure(Component.literal("Unknown HBM recipe machine '" + machineName
                    + "'. Valid: " + String.join(", ", recipeMachineNames())));
            return 0;
        }
        return auditRecipes(source, machine);
    }

    private static int auditRecipes(CommandSourceStack source, @Nullable GenericMachineRecipe.Machine requestedMachine) {
        int problems = 0;
        int machines = 0;
        for (GenericMachineRecipe.Machine machine : GenericMachineRecipe.Machine.values()) {
            if (requestedMachine != null && machine != requestedMachine) {
                continue;
            }
            machines++;
            GenericMachineRecipeRuntime.Index index = GenericMachineRecipeRuntime.index(source.getLevel(), machine);
            GenericMachineRecipeRuntime.Audit audit = index.audit();
            int machineProblems = audit.problemCount();
            problems += machineProblems;
            source.sendSuccess(() -> Component.literal("Recipe audit " + recipeMachineName(machine)
                    + ": recipes=" + index.recipes().size()
                    + ", internalNames=" + index.byInternalName().size()
                    + ", pools=" + index.byPool().size()
                    + ", problems=" + machineProblems), false);
            sendRecipeAuditDetails(source, audit);
        }
        int totalProblems = problems;
        int totalMachines = machines;
        source.sendSuccess(() -> Component.literal("Recipe audit complete: machines=" + totalMachines
                + ", problems=" + totalProblems), totalProblems > 0);
        return totalProblems == 0 ? 1 : totalProblems;
    }

    private static void sendRecipeAuditDetails(CommandSourceStack source, GenericMachineRecipeRuntime.Audit audit) {
        if (!audit.duplicateInternalNames().isEmpty()) {
            source.sendSuccess(() -> Component.literal(" - duplicate internal names: "
                    + String.join(", ", audit.duplicateInternalNames().keySet())), false);
        }
        if (!audit.emptyInputs().isEmpty()) {
            source.sendSuccess(() -> Component.literal(" - empty inputs: " + recipeIds(audit.emptyInputs())), false);
        }
        if (!audit.emptyOutputs().isEmpty()) {
            source.sendSuccess(() -> Component.literal(" - empty outputs: " + recipeIds(audit.emptyOutputs())), false);
        }
        if (!audit.invalidDurations().isEmpty()) {
            source.sendSuccess(() -> Component.literal(" - invalid durations: " + recipeIds(audit.invalidDurations())), false);
        }
        if (!audit.invalidWeightedOutputs().isEmpty()) {
            source.sendSuccess(() -> Component.literal(" - invalid weighted outputs: "
                    + recipeIds(audit.invalidWeightedOutputs())), false);
        }
        if (!audit.overLimit().isEmpty()) {
            source.sendSuccess(() -> Component.literal(" - over machine limits: " + recipeIds(audit.overLimit())), false);
        }
        if (!audit.oversizedItemInputs().isEmpty()) {
            source.sendSuccess(() -> Component.literal(" - oversized item inputs: "
                    + recipeIds(audit.oversizedItemInputs())), false);
        }
        if (!audit.unresolvedItemInputs().isEmpty()) {
            source.sendSuccess(() -> Component.literal(" - unresolved item inputs: "
                    + unresolvedRecipeInputs(audit.unresolvedItemInputs())), false);
        }
    }

    private static int listUnresolvedRecipeInputsByName(CommandSourceStack source, String machineName) {
        GenericMachineRecipe.Machine machine = parseRecipeMachine(machineName);
        if (machine == null) {
            source.sendFailure(Component.literal("Unknown HBM recipe machine '" + machineName
                    + "'. Valid: " + String.join(", ", recipeMachineNames())));
            return 0;
        }
        return listUnresolvedRecipeInputs(source, machine);
    }

    private static int listUnresolvedRecipeInputs(CommandSourceStack source,
            @Nullable GenericMachineRecipe.Machine requestedMachine) {
        int groups = 0;
        int entries = 0;
        for (GenericMachineRecipe.Machine machine : GenericMachineRecipe.Machine.values()) {
            if (requestedMachine != null && machine != requestedMachine) {
                continue;
            }
            GenericMachineRecipeRuntime.Audit audit = GenericMachineRecipeRuntime.audit(source.getLevel(), machine);
            int machineGroups = audit.unresolvedItemInputGroups().size();
            int machineEntries = audit.unresolvedItemInputDetails().size();
            groups += machineGroups;
            entries += machineEntries;
            source.sendSuccess(() -> Component.literal("Recipe unresolved inputs " + recipeMachineName(machine)
                    + ": groups=" + machineGroups
                    + ", entries=" + machineEntries
                    + ", recipes=" + audit.unresolvedItemInputs().size()), false);
            for (GenericMachineRecipeRuntime.UnresolvedInputGroup group : audit.unresolvedItemInputGroups().values()) {
                source.sendSuccess(() -> Component.literal(" - " + group.diagnosticName()
                        + " entries=" + group.entries().size()
                        + " recipes=" + group.recipeCount()
                        + " at " + unresolvedInputRefs(group.entries())), false);
            }
        }
        int totalGroups = groups;
        int totalEntries = entries;
        source.sendSuccess(() -> Component.literal("Recipe unresolved input summary: groups=" + totalGroups
                + ", entries=" + totalEntries), totalEntries > 0);
        return totalEntries == 0 ? 1 : totalEntries;
    }

    private static int listLegacyMetaMappings(CommandSourceStack source, @Nullable ResourceLocation requestedLegacyId) {
        if (requestedLegacyId != null && LegacyMetaItemMappings.variantCount(requestedLegacyId) == 0) {
            source.sendFailure(Component.literal("Unknown legacy meta mapping family: " + requestedLegacyId));
            return 0;
        }
        int families = 0;
        int variants = 0;
        for (ResourceLocation legacyId : LegacyMetaItemMappings.legacyIds()) {
            if (requestedLegacyId != null && !requestedLegacyId.equals(legacyId)) {
                continue;
            }
            families++;
            List<RegistryObject<Item>> mappedItems = LegacyMetaItemMappings.variants(legacyId);
            variants += mappedItems.size();
            source.sendSuccess(() -> Component.literal("Legacy meta " + legacyId + " variants=" + mappedItems.size()), false);
            for (int meta = 0; meta < mappedItems.size(); meta++) {
                int currentMeta = meta;
                RegistryObject<Item> item = mappedItems.get(meta);
                source.sendSuccess(() -> Component.literal(" - " + currentMeta + " -> " + item.getId()), false);
            }
        }
        int totalFamilies = families;
        int totalVariants = variants;
        source.sendSuccess(() -> Component.literal("Legacy meta mappings: families=" + totalFamilies
                + ", variants=" + totalVariants), false);
        return totalVariants;
    }

    private static int queryLegacyOreMapping(CommandSourceStack source, String oreName) {
        LegacyOreDictionaryMappings.Mapping mapping = LegacyOreDictionaryMappings.resolve(oreName);
        int tagEntries = source.getLevel().registryAccess().registryOrThrow(Registries.ITEM)
                .getTag(LegacyOreDictionaryMappings.itemTag(oreName))
                .map(named -> (int) named.stream()
                        .filter(holder -> holder.value() != Items.AIR)
                        .count())
                .orElse(0);
        source.sendSuccess(() -> Component.literal("Legacy ore " + oreName
                + " -> #" + mapping.tagId()
                + " kind=" + mapping.kind()
                + (mapping.matchedRule().isBlank() ? "" : " rule=" + mapping.matchedRule())
                + (mapping.materialOrPath().isBlank() ? "" : " path=" + mapping.materialOrPath())
                + " entries=" + tagEntries), false);
        return tagEntries;
    }

    private static int getAssemblyInfo(CommandSourceStack source, BlockPos pos) {
        AssemblyMachineBlockEntity assembler = getAssemblyMachine(source, pos);
        if (assembler == null) {
            return 0;
        }
        GenericMachineRecipe recipe = assembler.getSelectedRecipeDefinition();
        String recipeInfo = recipe == null
                ? "none"
                : recipe.getInternalName() + " duration=" + recipe.getDuration() + " power=" + recipe.getPower();
        source.sendSuccess(() -> Component.literal("Assembly machine at " + pos.toShortString()
                + ": recipe=" + recipeInfo
                + " progress=" + Math.round(assembler.getProgress() * 1000.0D) / 10.0D + "%"
                + " power=" + assembler.getPower() + "/" + assembler.getMaxPower() + " HE"
                + " canProcess=" + assembler.canProcessSelectedRecipe()
                + " inputTank=" + formatTank(assembler.getInputTank())
                + " outputTank=" + formatTank(assembler.getOutputTank())), false);
        return recipe == null ? 0 : 1;
    }

    private static int setAssemblyRecipe(CommandSourceStack source, BlockPos pos, String recipeName) {
        AssemblyMachineBlockEntity assembler = getAssemblyMachine(source, pos);
        if (assembler == null) {
            return 0;
        }
        if (!assembler.selectRecipe(recipeName)) {
            source.sendFailure(Component.literal("Unknown assembly recipe '" + recipeName + "'."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Assembly machine at " + pos.toShortString()
                + " selected recipe " + assembler.getSelectedRecipeName()), true);
        return 1;
    }

    private static int clearAssemblyRecipe(CommandSourceStack source, BlockPos pos) {
        AssemblyMachineBlockEntity assembler = getAssemblyMachine(source, pos);
        if (assembler == null) {
            return 0;
        }
        assembler.selectRecipe(GenericMachineRecipeRuntime.NULL_RECIPE);
        source.sendSuccess(() -> Component.literal("Assembly machine at " + pos.toShortString() + " recipe cleared."), true);
        return 1;
    }

    private static AssemblyMachineBlockEntity getAssemblyMachine(CommandSourceStack source, BlockPos pos) {
        BlockEntity blockEntity = CompatEnergyControl.findTileEntity(source.getLevel(), pos);
        if (blockEntity instanceof AssemblyMachineBlockEntity assembler) {
            return assembler;
        }
        source.sendFailure(Component.literal("No HBM assembly machine at " + pos.toShortString()));
        return null;
    }

    private static List<String> recipeMachineNames() {
        return Stream.of(GenericMachineRecipe.Machine.values())
                .map(ModCommands::recipeMachineName)
                .toList();
    }

    @Nullable
    private static GenericMachineRecipe.Machine parseRecipeMachine(String name) {
        String normalized = name.toUpperCase(Locale.ROOT).replace('-', '_');
        for (GenericMachineRecipe.Machine machine : GenericMachineRecipe.Machine.values()) {
            if (machine.name().equals(normalized) || recipeMachineName(machine).equals(name)) {
                return machine;
            }
        }
        return null;
    }

    private static String recipeMachineName(GenericMachineRecipe.Machine machine) {
        return machine.name().toLowerCase(Locale.ROOT);
    }

    private static String recipeIds(List<GenericMachineRecipe> recipes) {
        return recipes.stream()
                .map(recipe -> recipe.getId().toString())
                .collect(Collectors.joining(", "));
    }

    private static String unresolvedRecipeInputs(List<GenericMachineRecipe> recipes) {
        return recipes.stream()
                .map(recipe -> recipe.getId() + " [" + recipe.getItemInputs().stream()
                        .filter(HbmIngredient::unresolvedDisplayInput)
                        .map(HbmIngredient::diagnosticName)
                        .collect(Collectors.joining("; ")) + "]")
                .collect(Collectors.joining(", "));
    }

    private static String unresolvedInputRefs(List<GenericMachineRecipeRuntime.UnresolvedItemInput> entries) {
        return entries.stream()
                .map(entry -> entry.recipe().getId() + "#" + entry.inputIndex())
                .collect(Collectors.joining(", "));
    }

    private static String formatTank(HbmFluidTank tank) {
        return "{fluid=" + tank.getTankType().getName()
                + ", fill=" + tank.getFill()
                + ", capacity=" + tank.getMaxFill()
                + ", pressure=" + tank.getPressure()
                + "}";
    }

    private static Component formatHbmTank(int index, HbmFluidTank tank) {
        FluidType type = tank.getTankType();
        return Component.literal("  [" + index + "] fluid=")
                .append(type.getDisplayName())
                .append(Component.literal(" (" + type.getName() + ") fill="
                        + tank.getFill() + "/" + tank.getMaxFill() + " mB"
                        + " pressure=" + tank.getPressure()
                        + " forge=" + formatForgeMapping(type)));
    }

    private static String formatForgeMapping(FluidType type) {
        if (type == HbmFluids.NONE) {
            return "empty";
        }
        return HbmFluidForgeMappings.canExport(type) ? "exportable" : "hbm-only";
    }

    private static Component formatForgeTank(int index, FluidStack stack, int capacity) {
        Component name = stack.isEmpty() ? Component.literal("empty") : stack.getDisplayName();
        return Component.literal("  [" + index + "] fluid=")
                .append(name)
                .append(Component.literal(" amount=" + stack.getAmount() + "/" + capacity + " mB"));
    }

    private static FluidHandlerLookup getFluidHandler(BlockEntity blockEntity) {
        IFluidHandler nullSide = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).resolve().orElse(null);
        if (nullSide != null) {
            return new FluidHandlerLookup("null", nullSide);
        }
        for (Direction direction : Direction.values()) {
            IFluidHandler sided = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction).resolve().orElse(null);
            if (sided != null) {
                return new FluidHandlerLookup(direction.getName(), sided);
            }
        }
        return new FluidHandlerLookup("none", null);
    }

    private static List<String> getFluidCapabilitySides(BlockEntity blockEntity) {
        List<String> sides = new ArrayList<>();
        if (blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()) {
            sides.add("null");
        }
        for (Direction direction : Direction.values()) {
            if (blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction).isPresent()) {
                sides.add(direction.getName());
            }
        }
        return sides;
    }

    private static int toggleEnergyDebugParticles(CommandSourceStack source) {
        boolean enabled = HbmEnergyDebug.toggleParticleDebug();
        source.sendSuccess(() -> Component.literal("Energy debug particles: " + enabled), true);
        return enabled ? 1 : 0;
    }

    private static int setEnergyDebugParticles(CommandSourceStack source, boolean enabled) {
        HbmEnergyDebug.setParticleDebugEnabled(enabled);
        source.sendSuccess(() -> Component.literal("Energy debug particles: " + enabled), true);
        return enabled ? 1 : 0;
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

    private static int getNetworkProtocolSummary(CommandSourceStack source) {
        List<ModMessages.PacketRegistration> registrations = ModMessages.packetRegistrations();
        long clientbound = registrations.stream().filter(registration -> "S2C".equals(registration.direction())).count();
        long serverbound = registrations.stream().filter(registration -> "C2S".equals(registration.direction())).count();
        source.sendSuccess(() -> Component.literal("HBM network protocol: channel=hbm:main version="
                + ModMessages.protocolVersion()
                + " packets=" + ModMessages.registeredPacketCount()
                + " s2c=" + clientbound
                + " c2s=" + serverbound
                + " legacyRegistered=" + ModMessages.legacyPacketRegistrationCount()
                + " legacyMapped=" + ModMessages.mappedLegacyPacketCount()
                + " legacyUnmapped=" + ModMessages.unmappedLegacyPacketRegistrations().size()
                + " legacyMappings=" + ModMessages.legacyPacketMappingCount()
                + " blockedSends=" + ModMessages.blockedUnregisteredSendCount()), false);
        if (!registrations.isEmpty()) {
            ModMessages.PacketRegistration first = registrations.get(0);
            ModMessages.PacketRegistration last = registrations.get(registrations.size() - 1);
            source.sendSuccess(() -> Component.literal("Packet id range: #"
                    + first.id() + " " + first.typeName()
                    + " -> #" + last.id() + " " + last.typeName()), false);
        }
        return registrations.size();
    }

    private static int getNetworkProtocolProgress(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("HBM network packet library progress: "
                + ModMessages.progressSummary()), false);
        source.sendSuccess(() -> Component.literal("Legacy wrapper: "
                + ModMessages.legacyWrapperSummary()), false);
        source.sendSuccess(() -> Component.literal("Send safety: "
                + ModMessages.sendSafetySummary()), false);
        source.sendSuccess(() -> Component.literal("Coverage means old PacketDispatcher packets have modern carrier paths; "
                + "foundation is conservative and excludes unfinished receiver/business logic."), false);
        return ModMessages.libraryFoundationProgressPercent();
    }

    private static int listNetworkProtocolPackets(CommandSourceStack source) {
        List<ModMessages.PacketRegistration> registrations = ModMessages.packetRegistrations();
        if (registrations.isEmpty()) {
            source.sendSuccess(() -> Component.literal("HBM network protocol has no registered packets yet."), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("HBM network packets:"), false);
        for (ModMessages.PacketRegistration registration : registrations) {
            long legacyMappings = ModMessages.legacyPacketMappings().stream()
                    .filter(mapping -> registration.typeName().equals(mapping.modernName()))
                    .count();
            source.sendSuccess(() -> Component.literal("#" + registration.id()
                    + " " + registration.direction()
                    + " " + registration.typeName()
                    + (legacyMappings > 0 ? " legacyMappings=" + legacyMappings : "")), false);
        }
        return registrations.size();
    }

    private static int auditNetworkProtocol(CommandSourceStack source) {
        ModMessages.ProtocolAudit audit = ModMessages.protocolAudit();
        source.sendSuccess(() -> Component.literal("HBM network protocol audit: "
                + ModMessages.protocolAuditSummary()), false);
        for (ModMessages.LegacyPacketMapping mapping : audit.mappingsToUnregisteredModernPackets()) {
            source.sendSuccess(() -> Component.literal("Mapping points to unregistered modern packet: "
                    + mapping.legacyName() + " -> " + mapping.modernName()), false);
        }
        for (ModMessages.LegacyPacketMapping mapping : audit.mappingsFromUnknownLegacyPackets()) {
            source.sendSuccess(() -> Component.literal("Mapping references unknown legacy packet: "
                    + mapping.legacyName() + " -> " + mapping.modernName()), false);
        }
        for (ModMessages.LegacyPacketMapping mapping : audit.mappingsWithDirectionMismatch()) {
            source.sendSuccess(() -> Component.literal("Mapping direction mismatch: "
                    + mapping.legacyName() + " -> " + mapping.modernName()
                    + " mappingDirection=" + mapping.direction()), false);
        }
        for (ModMessages.LegacyPacketRegistration registration : audit.unmappedLegacyPackets()) {
            source.sendSuccess(() -> Component.literal("Unmapped legacy packet: #"
                    + registration.legacyId() + " " + registration.direction() + " " + registration.legacyName()), false);
        }
        if (!audit.duplicateLegacyIds().isEmpty()) {
            source.sendSuccess(() -> Component.literal("Duplicate legacy ids: " + audit.duplicateLegacyIds()), false);
        }
        if (!audit.duplicateLegacyNames().isEmpty()) {
            source.sendSuccess(() -> Component.literal("Duplicate legacy names: " + audit.duplicateLegacyNames()), false);
        }
        if (!audit.duplicateModernRegistrations().isEmpty()) {
            source.sendSuccess(() -> Component.literal("Duplicate modern registrations: " + audit.duplicateModernRegistrations()), false);
        }
        return audit.hasProblems() ? 0 : 1;
    }

    private static int queryLegacyNetworkPacket(CommandSourceStack source, String legacyName) {
        Optional<ModMessages.LegacyPacketRegistration> registration = ModMessages.legacyPacketRegistration(legacyName);
        List<ModMessages.LegacyPacketMapping> mappings = ModMessages.legacyPacketMappings(legacyName);
        if (registration.isEmpty()) {
            source.sendFailure(Component.literal("Unknown legacy packet: " + legacyName));
            return 0;
        }
        ModMessages.LegacyPacketRegistration legacy = registration.get();
        source.sendSuccess(() -> Component.literal("Legacy packet #" + legacy.legacyId()
                + " " + legacy.direction()
                + " " + legacy.legacyName()
                + " mappings=" + mappings.size()), false);
        for (ModMessages.LegacyPacketMapping mapping : mappings) {
            String registered = ModMessages.packetRegistration(mapping.modernName()).isPresent() ? "registered" : "missing";
            source.sendSuccess(() -> Component.literal("-> " + mapping.modernName()
                    + " " + mapping.direction()
                    + " " + registered
                    + " (" + mapping.notes() + ")"), false);
        }
        return mappings.size();
    }

    private static int queryModernNetworkPacket(CommandSourceStack source, String modernName) {
        Optional<ModMessages.PacketRegistration> registration = ModMessages.packetRegistration(modernName);
        List<ModMessages.LegacyPacketMapping> mappings = ModMessages.modernPacketMappings(modernName);
        if (registration.isEmpty()) {
            source.sendFailure(Component.literal("Unknown modern packet registration: " + modernName));
            return 0;
        }
        ModMessages.PacketRegistration modern = registration.get();
        source.sendSuccess(() -> Component.literal("Modern packet #" + modern.id()
                + " " + modern.direction()
                + " " + modern.typeName()
                + " legacyMappings=" + mappings.size()), false);
        for (ModMessages.LegacyPacketMapping mapping : mappings) {
            source.sendSuccess(() -> Component.literal("<- " + mapping.legacyName()
                    + " " + mapping.direction()
                    + " (" + mapping.notes() + ")"), false);
        }
        return mappings.size();
    }

    private static int getNetworkLegacyWrapper(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("HBM legacy network wrapper: "
                + ModMessages.legacyWrapperSummary()), false);
        source.sendSuccess(() -> Component.literal("Send safety: "
                + ModMessages.sendSafetySummary()), false);
        source.sendSuccess(() -> Component.literal("Use ModMessages.wrapper() when migrating old PacketDispatcher.wrapper call sites; "
                + "use explicit ModMessages helpers for new code."), false);
        return LegacyNetworkDispatcher.directSendHelperCount() + LegacyNetworkDispatcher.threadedSendHelperCount();
    }

    private static int listLegacyNetworkProtocolPackets(CommandSourceStack source) {
        List<ModMessages.LegacyPacketRegistration> registrations = ModMessages.legacyPacketRegistrations();
        if (registrations.isEmpty()) {
            source.sendSuccess(() -> Component.literal("HBM legacy protocol has no recorded packets."), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("HBM legacy 1.7.10 packets:"), false);
        for (ModMessages.LegacyPacketRegistration registration : registrations) {
            long mappingCount = ModMessages.legacyPacketMappings().stream()
                    .filter(mapping -> registration.legacyName().equals(mapping.legacyName()))
                    .count();
            source.sendSuccess(() -> Component.literal("#" + registration.legacyId()
                    + " " + registration.direction()
                    + " " + registration.legacyName()
                    + (mappingCount > 0 ? " mapped=" + mappingCount : " unmapped")), false);
        }
        return registrations.size();
    }

    private static int listNetworkProtocolMappings(CommandSourceStack source) {
        List<ModMessages.LegacyPacketMapping> mappings = ModMessages.legacyPacketMappings();
        if (mappings.isEmpty()) {
            source.sendSuccess(() -> Component.literal("HBM network protocol has no legacy packet mappings."), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("HBM legacy packet mappings: registered="
                + ModMessages.legacyPacketRegistrationCount()
                + " mapped=" + ModMessages.mappedLegacyPacketCount()
                + " unmapped=" + ModMessages.unmappedLegacyPacketRegistrations().size()
                + " mappingRows=" + mappings.size()), false);
        for (ModMessages.LegacyPacketMapping mapping : mappings) {
            source.sendSuccess(() -> Component.literal(mapping.direction()
                    + " " + mapping.legacyName()
                    + " -> " + mapping.modernName()
                    + " (" + mapping.notes() + ")"), false);
        }
        List<ModMessages.LegacyPacketRegistration> unmapped = ModMessages.unmappedLegacyPacketRegistrations();
        if (!unmapped.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Unmapped legacy packets:"), false);
            for (ModMessages.LegacyPacketRegistration registration : unmapped) {
                source.sendSuccess(() -> Component.literal("#" + registration.legacyId()
                        + " " + registration.direction()
                        + " " + registration.legacyName()), false);
            }
        }
        return mappings.size();
    }

    private static int getPacketThreadingStats(CommandSourceStack source) {
        ThreadedPacketDispatcher.Snapshot snapshot = ThreadedPacketDispatcher.snapshot();
        source.sendSuccess(() -> Component.literal("Packet threading: pending=" + snapshot.pending()
                + " enabled=" + snapshot.enabled()
                + " configuredEnabled=" + snapshot.configuredEnabled()
                + " fallback=" + snapshot.fallbackToMainThread()
                + " errorBypass=" + snapshot.errorBypass()
                + " totalQueued=" + snapshot.totalQueued()
                + " totalPrepared=" + snapshot.totalPrepared()
                + " totalSent=" + snapshot.totalSent()
                + " totalFailed=" + snapshot.totalFailed()
                + " totalDiscarded=" + snapshot.totalDiscarded()
                + " prepareFailed=" + snapshot.totalPrepareFailed()
                + " manualClears=" + snapshot.manualClears()), false);
        source.sendSuccess(() -> Component.literal("Last flush: queued=" + snapshot.lastFlushQueued()
                + " completed=" + snapshot.lastFlushCompleted()
                + " discarded=" + snapshot.lastFlushDiscarded()
                + " wait=" + snapshot.lastFlushWaitMillis()
                + "ms observedWait=" + snapshot.lastObservedWaitMillis()
                + "ms clears=" + snapshot.consecutiveClears()), false);
        source.sendSuccess(() -> Component.literal("Thread pool: total=" + snapshot.threadPoolSize()
                + " core=" + snapshot.corePoolSize()
                + " max=" + snapshot.maximumPoolSize()
                + " active=" + snapshot.activeThreadCount()
                + " queued=" + snapshot.executorQueueSize()
                + " completed=" + snapshot.completedTaskCount()), false);
        source.sendSuccess(() -> Component.literal("Tile binary control uploads: pending="
                + ServerTileBinaryControlTransfers.pendingTransfers()), false);
        source.sendSuccess(() -> Component.literal("Client resync cooldowns: tile="
                + TileSyncPacket.pendingClientResyncRequests()
                + " tileBinary=" + ClientTileBinaryData.pendingClientResyncRequests()
                + " entity=" + EntitySyncPacket.pendingClientResyncRequests()
                + " cooldownTicks=" + TileSyncPacket.clientResyncRequestCooldownTicks()), false);
        source.sendSuccess(() -> Component.literal("Client network caches: binaryChannels="
                + ClientBinaryData.channelCount()
                + " binaryEntries=" + ClientBinaryData.entryCount()
                + " readyChannels=" + ClientBinaryData.readyChannelCount()
                + " binaryTransfers=" + ClientBinaryData.pendingTransfers()
                + " tileBinaryTransfers=" + ClientTileBinaryData.pendingTransfers()
                + " tileBinaryChunks=" + ClientTileBinaryData.pendingChunkCount()), false);
        source.sendSuccess(() -> Component.literal("Client sync caches: biomeChunks="
                + ClientBiomeSyncData.chunkCount()
                + " panelTypes=" + ClientPanelData.panelCount()
                + " playerData=" + ClientPlayerSyncData.entryCount()
                + " permaKeys=" + ClientPermaSyncData.keyCount()
                + " radiationEffects=" + ClientRadiationData.getContaminationCount()), false);
        source.sendSuccess(() -> Component.literal("Client transient effects: notices="
                + ClientInformMessages.noticeCount()
                + " muzzleFlashes=" + ClientMuzzleFlashEffects.flashCount()), false);
        if (!snapshot.lastFailureMessage().isBlank()) {
            source.sendSuccess(() -> Component.literal("Last packet threading issue: " + snapshot.lastFailureMessage()), false);
        }
        return snapshot.pending();
    }

    private static int getPacketThreadingConfig(CommandSourceStack source) {
        ThreadedPacketDispatcher.Snapshot snapshot = ThreadedPacketDispatcher.snapshot();
        source.sendSuccess(() -> Component.literal("Packet threading config: prefix="
                + ThreadedPacketDispatcher.threadPrefix()
                + " waitTimeoutMs=" + ThreadedPacketDispatcher.waitTimeoutMillis()
                + " maxPending=" + ThreadedPacketDispatcher.maxPendingOperations()
                + " fallbackClearThreshold=" + ThreadedPacketDispatcher.fallbackClearThreshold()
                + " enabled=" + snapshot.enabled()
                + " configuredEnabled=" + snapshot.configuredEnabled()
                + " fallback=" + snapshot.fallbackToMainThread()
                + " errorBypass=" + snapshot.errorBypass()), false);
        return ThreadedPacketDispatcher.maxPendingOperations();
    }

    private static int listPacketThreadingThreads(CommandSourceStack source) {
        List<ThreadedPacketDispatcher.ThreadSnapshot> threads = ThreadedPacketDispatcher.threadSnapshots();
        if (threads.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No HBM packet threads have been created yet."), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("HBM packet threads:"), false);
        for (ThreadedPacketDispatcher.ThreadSnapshot thread : threads) {
            String lockOwner = thread.lockOwner().isBlank() ? "none" : thread.lockOwner();
            source.sendSuccess(() -> Component.literal(thread.name()
                    + " id=" + thread.id()
                    + " state=" + thread.state()
                    + " lockOwner=" + lockOwner), false);
        }
        return threads.size();
    }

    private static int togglePacketThreading(CommandSourceStack source) {
        boolean enabled = ThreadedPacketDispatcher.toggleEnabled();
        source.sendSuccess(() -> Component.literal("Packet threading enabled: " + enabled), true);
        return enabled ? 1 : 0;
    }

    private static int setPacketThreading(CommandSourceStack source, boolean enabled) {
        ThreadedPacketDispatcher.setEnabled(enabled);
        source.sendSuccess(() -> Component.literal("Packet threading enabled: " + enabled), true);
        return enabled ? 1 : 0;
    }

    private static int clearPacketThreading(CommandSourceStack source) {
        int discarded = ThreadedPacketDispatcher.clearPending("Manual packet threading queue clear from command.");
        source.sendSuccess(() -> Component.literal("Cleared packet threading queue: discarded=" + discarded), true);
        return discarded;
    }

    private static int resetPacketThreading(CommandSourceStack source) {
        ThreadedPacketDispatcher.resetState();
        source.sendSuccess(() -> Component.literal("Reset packet threading dispatcher state."), true);
        return 1;
    }

    private static boolean parseBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equals(value);
    }

    private static FluidType parseFluid(String value) {
        return HbmFluids.fromName(value);
    }

    private static String formatPressureArray(long[] values) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(i).append('=').append(values[i]);
        }
        return builder.append(']').toString();
    }

    private static void syncPlayers(Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            CommonForgeEvents.syncRadiationNow(player);
        }
    }

    private static float round(float value) {
        return Math.round(value * 10.0F) / 10.0F;
    }

    private record FluidHandlerLookup(String source, IFluidHandler handler) {
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
