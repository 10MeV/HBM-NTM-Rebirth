package com.hbm.ntm.command;

import com.google.gson.JsonObject;
import com.hbm.ntm.api.tile.InfoProviderEC;
import com.hbm.ntm.api.redstoneoverradio.RORFunctionException;
import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.ROR;
import com.hbm.ntm.api.redstoneoverradio.RORRemoteBridge;
import com.hbm.ntm.api.redstoneoverradio.RTTYCounterState;
import com.hbm.ntm.api.redstoneoverradio.RTTYLogicEvaluator;
import com.hbm.ntm.api.redstoneoverradio.RTTYReaderState;
import com.hbm.ntm.api.redstoneoverradio.RTTYSignalMapper;
import com.hbm.ntm.api.redstoneoverradio.RTTYSystem;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.blockentity.FluidPipeAnchorBlockEntity;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.blockentity.HbmEnergyAndFluidBlockEntity;
import com.hbm.ntm.blockentity.HbmEnergyBlockEntity;
import com.hbm.ntm.blockentity.HbmFluidBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchCounterBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchDeviceBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchLogicBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchReaderBlockEntity;
import com.hbm.ntm.client.ClientBinaryData;
import com.hbm.ntm.client.ClientBiomeSyncData;
import com.hbm.ntm.client.ClientInformMessages;
import com.hbm.ntm.client.ClientMuzzleFlashEffects;
import com.hbm.ntm.client.ClientPanelData;
import com.hbm.ntm.client.ClientPermaSyncData;
import com.hbm.ntm.client.ClientHbmPlayerProperties;
import com.hbm.ntm.client.ClientHbmLivingProperties;
import com.hbm.ntm.client.ClientTileBinaryData;
import com.hbm.ntm.compat.Compat;
import com.hbm.ntm.compat.CompatCustomWarheadRegistry;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.compat.CompatFluidRegistry;
import com.hbm.ntm.compat.CompatHandler;
import com.hbm.ntm.compat.CompatRecipeRegistry;
import com.hbm.ntm.compat.CompatTurretTargetRegistry;
import com.hbm.ntm.damage.DamageResistanceConfig;
import com.hbm.ntm.damage.DamageResistance;
import com.hbm.ntm.damage.DamageResistanceHandler;
import com.hbm.ntm.damage.DamageResistanceStats;
import com.hbm.ntm.damage.DamageResistanceTooltipUtil;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.energy.HbmEnergyDebug;
import com.hbm.ntm.energy.HbmEnergyNodespace;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.event.CommonForgeEvents;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmCompatFluidRegistry;
import com.hbm.ntm.fluid.HbmFluidContainerRegistry;
import com.hbm.ntm.fluid.HbmFluidContainerConfig;
import com.hbm.ntm.fluid.HbmFluidForgeAliasConfig;
import com.hbm.ntm.fluid.HbmFluidForgeMappings;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.fluid.HbmFluidTypeConfig;
import com.hbm.ntm.fluid.HbmFluidTraitConfig;
import com.hbm.ntm.fluid.HbmFluidUtil;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.itempool.HbmItemPoolRegistry;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.LegacyDimensionIdNetwork;
import com.hbm.ntm.network.LegacyNetworkDispatcher;
import com.hbm.ntm.network.LegacyPacketThreading;
import com.hbm.ntm.network.LegacyRawBufferNetwork;
import com.hbm.ntm.network.LegacyTargetPoint;
import com.hbm.ntm.network.ServerTileBinaryControlTransfers;
import com.hbm.ntm.network.ThreadedPacketDispatcher;
import com.hbm.ntm.network.packet.EntitySyncPacket;
import com.hbm.ntm.network.packet.TileSyncPacket;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionSavedData;
import com.hbm.ntm.pollution.PollutionSavedData.PollutionGridPos;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.LegacyGenericRecipeHandlers;
import com.hbm.ntm.recipe.LegacySerializableRecipeHandlers;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.radiation.CraterRadiationData;
import com.hbm.ntm.radiation.HazmatRegistry;
import com.hbm.ntm.radiation.HazmatResistanceConfig;
import com.hbm.ntm.radiation.LegacyFalloutConversions;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.radiation.RadiationConstants;
import com.hbm.ntm.radiation.RadiationSavedData;
import com.hbm.ntm.world.BlockMigrationHelper;
import com.hbm.ntm.world.SubChunkKey;
import com.hbm.ntm.world.SubChunkSnapshot;
import com.hbm.ntm.world.WorldUtil;
import com.hbm.ntm.world.saveddata.AnnihilatorSavedData;
import com.hbm.ntm.world.saveddata.TomImpactSavedData;
import com.hbm.ntm.world.saveddata.WorldSavedDataDiagnostics;
import com.hbm.ntm.world.saveddata.WorldSavedDataHelper;
import com.hbm.ntm.satellite.ISatelliteChip;
import com.hbm.ntm.satellite.LegacySatelliteType;
import com.hbm.ntm.satellite.Satellite;
import com.hbm.ntm.satellite.SatelliteSavedData;
import com.hbm.ntm.uninos.HbmNodespace;
import com.hbm.ntm.uninos.HbmUninosDiagnostics;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNodespace;
import com.hbm.ntm.util.HbmLegacyLootUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ModCommands {
    private static final int MAX_SUBCHUNK_DIAGNOSTIC_SCAN = 4096;

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
                        .then(Commands.literal("hazmat")
                                .then(Commands.literal("status")
                                        .executes(context -> getHazmatResistanceStatus(context.getSource())))
                                .then(Commands.literal("reload")
                                        .executes(context -> reloadHazmatResistanceConfig(context.getSource()))))
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
                .then(pollutionCommand())
                .then(damageCommand())
                .then(recipeCommand())
                .then(itemPoolCommand())
                .then(machineCommand())
                .then(fluidCommand())
                .then(compatCommand())
                .then(networkCommand())
                .then(worldCommand())
                .then(satelliteCommand()));

        dispatcher.register(legacyPacketThreadingCommand("ntmpackets"));
        dispatcher.register(legacyPacketThreadingCommand("ntmpacket"));
        dispatcher.register(satelliteCommand("ntmsatellites"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> satelliteCommand() {
        return satelliteCommand("satellite");
    }

    private static LiteralArgumentBuilder<CommandSourceStack> worldCommand() {
        return Commands.literal("world")
                .then(Commands.literal("saveddata")
                        .then(Commands.literal("status")
                                .executes(context -> worldSavedDataStatus(context.getSource())))
                        .then(Commands.literal("dimension")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .executes(context -> worldSavedDataStatus(
                                                context.getSource(),
                                                DimensionArgument.getDimension(context, "dimension")))))
                        .then(Commands.literal("all")
                                .executes(context -> worldSavedDataAllStatus(context.getSource())))
                        .then(Commands.literal("health")
                                .executes(context -> worldSavedDataHealth(
                                        context.getSource(),
                                        context.getSource().getLevel()))
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .executes(context -> worldSavedDataHealth(
                                                context.getSource(),
                                                DimensionArgument.getDimension(context, "dimension")))))
                        .then(Commands.literal("healthAll")
                                .executes(context -> worldSavedDataHealthAll(context.getSource())))
                        .then(Commands.literal("known")
                                .executes(context -> worldSavedDataKnownData(
                                        context.getSource(),
                                        context.getSource().getLevel()))
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .executes(context -> worldSavedDataKnownData(
                                                context.getSource(),
                                                DimensionArgument.getDimension(context, "dimension")))))
                        .then(Commands.literal("knownAll")
                                .executes(context -> worldSavedDataKnownDataAll(context.getSource())))
                        .then(Commands.literal("schema")
                                .executes(context -> worldSavedDataSchema(context.getSource())))
                        .then(Commands.literal("normalize")
                                .then(Commands.literal("known")
                                        .executes(context -> worldSavedDataNormalizeKnown(
                                                context.getSource(),
                                                context.getSource().getLevel()))
                                        .then(Commands.literal("all")
                                                .executes(context -> worldSavedDataNormalizeKnownAll(
                                                        context.getSource())))
                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                .executes(context -> worldSavedDataNormalizeKnown(
                                                        context.getSource(),
                                                        DimensionArgument.getDimension(context, "dimension"))))))
                        .then(Commands.literal("promote")
                                .then(Commands.literal("pollution")
                                        .executes(context -> worldSavedDataPromotePollution(
                                                context.getSource(),
                                                context.getSource().getLevel()))
                                        .then(Commands.literal("all")
                                                .executes(context -> worldSavedDataPromotePollutionAll(
                                                        context.getSource())))
                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                .executes(context -> worldSavedDataPromotePollution(
                                                        context.getSource(),
                                                        DimensionArgument.getDimension(context, "dimension"))))))
                        .then(Commands.literal("tom")
                                .then(Commands.literal("summary")
                                        .executes(context -> worldSavedDataTomSummary(context.getSource())))
                                .then(Commands.literal("summaryAll")
                                        .executes(context -> worldSavedDataTomSummaryAll(context.getSource())))
                                .then(Commands.literal("cache")
                                        .executes(context -> worldSavedDataTomCache(context.getSource()))))
                        .then(Commands.literal("annihilator")
                                .then(Commands.literal("summary")
                                        .executes(context -> worldSavedDataAnnihilatorSummary(context.getSource())))
                                .then(Commands.literal("summaryAll")
                                        .executes(context -> worldSavedDataAnnihilatorSummaryAll(context.getSource())))
                                .then(Commands.literal("load")
                                        .executes(context -> worldSavedDataAnnihilatorLoad(context.getSource())))
                                .then(Commands.literal("loadAll")
                                        .executes(context -> worldSavedDataAnnihilatorLoadAll(context.getSource())))
                                .then(Commands.literal("pools")
                                        .executes(context -> worldSavedDataAnnihilatorPools(context.getSource())))
                                .then(Commands.literal("pool")
                                        .then(Commands.argument("name", StringArgumentType.string())
                                                .suggests(ModCommands::suggestAnnihilatorPools)
                                                .executes(context -> worldSavedDataAnnihilatorPool(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "name")))))
                                .then(Commands.literal("top")
                                        .then(Commands.argument("pool", StringArgumentType.string())
                                                .suggests(ModCommands::suggestAnnihilatorPools)
                                                .executes(context -> worldSavedDataAnnihilatorTop(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "pool"),
                                                        12))
                                                .then(Commands.argument("limit", IntegerArgumentType.integer(1, 64))
                                                        .executes(context -> worldSavedDataAnnihilatorTop(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "pool"),
                                                                IntegerArgumentType.getInteger(context, "limit"))))))
                                .then(Commands.literal("kind")
                                        .then(Commands.argument("pool", StringArgumentType.string())
                                                .suggests(ModCommands::suggestAnnihilatorPools)
                                                .then(Commands.argument("kind", StringArgumentType.word())
                                                        .suggests(ModCommands::suggestAnnihilatorKinds)
                                                        .executes(context -> worldSavedDataAnnihilatorKind(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "pool"),
                                                                StringArgumentType.getString(context, "kind"),
                                                                12))
                                                        .then(Commands.argument("limit", IntegerArgumentType.integer(1, 64))
                                                                .executes(context -> worldSavedDataAnnihilatorKind(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(context, "pool"),
                                                                        StringArgumentType.getString(context, "kind"),
                                                                        IntegerArgumentType.getInteger(context, "limit")))))))
                                .then(Commands.literal("amount")
                                        .then(Commands.literal("item")
                                                .then(Commands.argument("pool", StringArgumentType.string())
                                                        .suggests(ModCommands::suggestAnnihilatorPools)
                                                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                                                .suggests(ModCommands::suggestItemIds)
                                                                .executes(context -> worldSavedDataAnnihilatorItemAmount(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(context, "pool"),
                                                                        ResourceLocationArgument.getId(context, "item"))))))
                                        .then(Commands.literal("itemMeta")
                                                .then(Commands.argument("pool", StringArgumentType.string())
                                                        .suggests(ModCommands::suggestAnnihilatorPools)
                                                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                                                .suggests(ModCommands::suggestItemIds)
                                                                .then(Commands.argument("legacyMeta", IntegerArgumentType.integer(0))
                                                                        .executes(context -> worldSavedDataAnnihilatorItemMetaAmount(
                                                                                context.getSource(),
                                                                                StringArgumentType.getString(context, "pool"),
                                                                                ResourceLocationArgument.getId(context, "item"),
                                                                                IntegerArgumentType.getInteger(context, "legacyMeta")))))))
                                        .then(Commands.literal("fluid")
                                                .then(Commands.argument("pool", StringArgumentType.string())
                                                        .suggests(ModCommands::suggestAnnihilatorPools)
                                                        .then(Commands.argument("fluid", StringArgumentType.word())
                                                                .suggests(ModCommands::suggestHbmFluids)
                                                                .executes(context -> worldSavedDataAnnihilatorFluidAmount(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(context, "pool"),
                                                                        StringArgumentType.getString(context, "fluid"))))))
                                        .then(Commands.literal("oredict")
                                                .then(Commands.argument("pool", StringArgumentType.string())
                                                        .suggests(ModCommands::suggestAnnihilatorPools)
                                                        .then(Commands.argument("name", StringArgumentType.word())
                                                                .executes(context -> worldSavedDataAnnihilatorOreDictAmount(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(context, "pool"),
                                                                        StringArgumentType.getString(context, "name"))))))))
                        .then(Commands.literal("satellites")
                                .then(Commands.literal("summary")
                                        .executes(context -> worldSavedDataSatelliteSummary(context.getSource())))
                                .then(Commands.literal("summaryAll")
                                        .executes(context -> worldSavedDataSatelliteSummaryAll(context.getSource())))
                                .then(Commands.literal("load")
                                        .executes(context -> worldSavedDataSatelliteLoad(context.getSource())))
                                .then(Commands.literal("loadAll")
                                        .executes(context -> worldSavedDataSatelliteLoadAll(context.getSource())))
                                .then(Commands.literal("types")
                                        .executes(context -> worldSavedDataSatelliteTypes(context.getSource())))
                                .then(Commands.literal("frequencies")
                                        .executes(context -> worldSavedDataSatelliteFrequencies(
                                                context.getSource(), 32))
                                        .then(Commands.argument("limit", IntegerArgumentType.integer(1, 256))
                                                .executes(context -> worldSavedDataSatelliteFrequencies(
                                                        context.getSource(),
                                                        IntegerArgumentType.getInteger(context, "limit"))))))
                        .then(Commands.literal("pollution")
                                .then(Commands.literal("load")
                                        .executes(context -> worldSavedDataPollutionLoad(context.getSource())))
                                .then(Commands.literal("loadAll")
                                        .executes(context -> worldSavedDataPollutionLoadAll(context.getSource()))))
                        .then(Commands.literal("chunkRadiation")
                                .then(Commands.literal("load")
                                        .executes(context -> worldSavedDataChunkRadiationLoad(context.getSource())))
                                .then(Commands.literal("loadAll")
                                        .executes(context -> worldSavedDataChunkRadiationLoadAll(context.getSource()))))
                        .then(Commands.literal("craterRadiation")
                                .then(Commands.literal("load")
                                        .executes(context -> worldSavedDataCraterRadiationLoad(context.getSource())))
                                .then(Commands.literal("loadAll")
                                        .executes(context -> worldSavedDataCraterRadiationLoadAll(context.getSource()))))
                        .then(Commands.literal("chunks")
                                .then(Commands.literal("here")
                                        .executes(context -> worldSavedDataChunkStatus(
                                                context.getSource(),
                                                new ChunkPos(BlockPos.containing(context.getSource().getPosition())))))
                                .then(Commands.literal("block")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(context -> worldSavedDataChunkStatus(
                                                        context.getSource(),
                                                        new ChunkPos(BlockPosArgument.getBlockPos(context, "pos"))))))
                                .then(Commands.literal("chunk")
                                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                        .executes(context -> worldSavedDataChunkStatus(
                                                                context.getSource(),
                                                                new ChunkPos(
                                                                        IntegerArgumentType.getInteger(context, "x"),
                                                                        IntegerArgumentType.getInteger(context, "z")))))))
                                .then(Commands.literal("square")
                                        .then(Commands.argument("radius", IntegerArgumentType.integer(0, 16))
                                                .executes(context -> worldSavedDataChunkSquareStatus(
                                                        context.getSource(),
                                                        IntegerArgumentType.getInteger(context, "radius"))))))
                        .then(Commands.literal("subchunks")
                                .then(Commands.literal("block")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(context -> worldSavedDataSubChunkStatus(
                                                        context.getSource(),
                                                        SubChunkKey.ofBlock(BlockPosArgument.getBlockPos(context, "pos"))))))
                                .then(Commands.literal("count")
                                        .then(Commands.argument("block", ResourceLocationArgument.id())
                                                .suggests((context, builder) -> suggestBlockIds(context, builder))
                                                .then(Commands.literal("block")
                                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                                .executes(context -> worldSavedDataSubChunkBlockCount(
                                                                        context.getSource(),
                                                                        ResourceLocationArgument.getId(context, "block"),
                                                                        "block",
                                                                        List.of(SubChunkKey.ofBlock(
                                                                                BlockPosArgument.getBlockPos(context, "pos")))))))
                                                .then(Commands.literal("range")
                                                        .then(Commands.argument("first", BlockPosArgument.blockPos())
                                                                .then(Commands.argument("second", BlockPosArgument.blockPos())
                                                                        .executes(context -> worldSavedDataSubChunkBlockCount(
                                                                                context.getSource(),
                                                                                ResourceLocationArgument.getId(context, "block"),
                                                                                "range",
                                                                                SubChunkKey.betweenBuildHeight(
                                                                                        context.getSource().getLevel(),
                                                                                        BlockPosArgument.getBlockPos(context, "first"),
                                                                                        BlockPosArgument.getBlockPos(context, "second")))))))
                                                .then(Commands.literal("sphere")
                                                        .then(Commands.argument("radius", IntegerArgumentType.integer(0, 512))
                                                                .executes(context -> worldSavedDataSubChunkBlockCount(
                                                                        context.getSource(),
                                                                        ResourceLocationArgument.getId(context, "block"),
                                                                        "sphere",
                                                                        SubChunkKey.aroundSphere(
                                                                                context.getSource().getLevel(),
                                                                                BlockPos.containing(context.getSource().getPosition()),
                                                                                IntegerArgumentType.getInteger(context, "radius"))))))
                                                .then(Commands.literal("sphereAt")
                                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                                .then(Commands.argument("radius", IntegerArgumentType.integer(0, 512))
                                                                        .executes(context -> worldSavedDataSubChunkBlockCount(
                                                                                context.getSource(),
                                                                                ResourceLocationArgument.getId(context, "block"),
                                                                                "sphereAt",
                                                                                SubChunkKey.aroundSphere(
                                                                                        context.getSource().getLevel(),
                                                                                        BlockPosArgument.getBlockPos(context, "pos"),
                                                                                        IntegerArgumentType.getInteger(context, "radius")))))))))
                                .then(Commands.literal("range")
                                        .then(Commands.argument("first", BlockPosArgument.blockPos())
                                                .then(Commands.argument("second", BlockPosArgument.blockPos())
                                                        .executes(context -> worldSavedDataSubChunkBatchStatus(
                                                                context.getSource(),
                                                                "range",
                                                                SubChunkKey.betweenBuildHeight(
                                                                        context.getSource().getLevel(),
                                                                        BlockPosArgument.getBlockPos(context, "first"),
                                                                        BlockPosArgument.getBlockPos(context, "second")))))))
                                .then(Commands.literal("sphere")
                                        .then(Commands.argument("radius", IntegerArgumentType.integer(0, 512))
                                                .executes(context -> worldSavedDataSubChunkBatchStatus(
                                                        context.getSource(),
                                                        "sphere",
                                                        SubChunkKey.aroundSphere(
                                                                context.getSource().getLevel(),
                                                                BlockPos.containing(context.getSource().getPosition()),
                                                                IntegerArgumentType.getInteger(context, "radius"))))))
                                .then(Commands.literal("sphereAt")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .then(Commands.argument("radius", IntegerArgumentType.integer(0, 512))
                                                        .executes(context -> worldSavedDataSubChunkBatchStatus(
                                                                context.getSource(),
                                                                "sphere",
                                                                SubChunkKey.aroundSphere(
                                                                        context.getSource().getLevel(),
                                                                        BlockPosArgument.getBlockPos(context, "pos"),
                                                                        IntegerArgumentType.getInteger(context, "radius"))))))))
                        .then(Commands.literal("migrations")
                                .then(Commands.literal("status")
                                        .executes(context -> worldSavedDataMigrationStatus(context.getSource())))
                                .then(Commands.literal("last")
                                        .executes(context -> worldSavedDataMigrationLast(context.getSource())))
                                .then(Commands.literal("marker")
                                        .then(Commands.literal("absent")
                                                .executes(context -> worldSavedDataMigrationMarker(
                                                        context.getSource(), null)))
                                        .then(Commands.literal("build")
                                                .then(Commands.argument("previousBuild", IntegerArgumentType.integer(-1))
                                                        .executes(context -> worldSavedDataMigrationMarker(
                                                                context.getSource(),
                                                                IntegerArgumentType.getInteger(context, "previousBuild"))))))
                                .then(Commands.literal("resetDiagnostics")
                                        .executes(context -> resetWorldSavedDataMigrationDiagnostics(context.getSource())))));
    }

    private static int worldSavedDataStatus(CommandSourceStack source) {
        return worldSavedDataStatus(source, source.getLevel());
    }

    private static int worldSavedDataStatus(CommandSourceStack source, ServerLevel level) {
        WorldSavedDataDiagnostics.LevelStatus status = WorldSavedDataDiagnostics.inspect(level);
        BlockMigrationHelper.MigrationDiagnostics migrations = BlockMigrationHelper.diagnostics();

        source.sendSuccess(() -> Component.literal("World SavedData dimension=" + status.dimension()), false);
        if (status.hasTomImpact()) {
            source.sendSuccess(() -> Component.literal("impactData present "
                    + status.tomImpactSummary().detail()), false);
        } else {
            source.sendSuccess(() -> Component.literal("impactData absent"), false);
        }
        source.sendSuccess(() -> Component.literal(status.hasAnnihilator()
                ? "annihilator present pools=" + status.annihilatorPools()
                + " entries=" + status.annihilatorEntries()
                + " total=" + status.annihilatorTotalAmount()
                + " keys=" + status.annihilatorSummary().keyKindCounts()
                + " kindTotals=" + status.annihilatorSummary().keyKindTotals()
                + " problemPools=" + status.annihilatorSummary().problemPools()
                + " load={" + status.annihilatorSummary().loadDiagnostics().summary() + "}"
                : "annihilator absent"), false);
        source.sendSuccess(() -> Component.literal(status.hasSatellites()
                ? "satellites present entries=" + status.satelliteCount()
                + " types=" + status.satelliteSummary().typeCounts()
                + " frequencies=" + status.satelliteSummary().frequencies()
                + " legacyEntryDiagnostics=" + status.satelliteSummary().legacyEntryDiagnostics()
                + " modernEntryDiagnostics=" + status.satelliteSummary().modernEntryDiagnostics()
                + " problemEntries=" + status.satelliteSummary().problemEntries()
                + " load={" + status.satelliteSummary().loadDiagnostics().summary() + "}"
                : "satellites absent"), false);
        source.sendSuccess(() -> Component.literal(status.hasPollution()
                ? "hbmpollution present " + status.pollutionSummary().detail()
                : "hbmpollution absent"), false);
        source.sendSuccess(() -> Component.literal(status.hasChunkRadiation()
                ? "hbm_chunk_radiation present " + status.chunkRadiationSummary().detail()
                : "hbm_chunk_radiation absent"), false);
        source.sendSuccess(() -> Component.literal(status.hasCraterRadiation()
                ? "hbm_crater_radiation present " + status.craterRadiationSummary().detail()
                : "hbm_crater_radiation absent"), false);
        source.sendSuccess(() -> Component.literal("chunk migrations " + migrations.summary()), false);
        return status.presentDataCount();
    }

    private static int worldSavedDataAllStatus(CommandSourceStack source) {
        WorldSavedDataDiagnostics.ServerStatus status = WorldSavedDataDiagnostics.inspect(source.getServer());
        source.sendSuccess(() -> Component.literal("World SavedData levels=" + status.levels().size()
                + " levelsWithData=" + status.levelsWithDataCount()
                + " presentData=" + status.presentDataCount()
                + " tomClimateDimensions=" + status.tomClimateDimensions()
                + " annihilatorPools=" + status.totalAnnihilatorPools()
                + " annihilatorEntries=" + status.totalAnnihilatorEntries()
                + " annihilatorProblemPools=" + status.totalAnnihilatorProblemPools()
                + " annihilatorTotal=" + status.totalAnnihilatorAmount()
                + " satellites=" + status.totalSatelliteCount()
                + " satelliteProblemEntries=" + status.totalSatelliteProblemEntries()
                + " pollutionEntries=" + status.totalPollutionEntries()
                + " pollutionPositive=" + status.totalPositivePollutionEntries()
                + " pollutionTotal=" + status.totalPollution()
                + " chunkRadiationEntries=" + status.totalChunkRadiationEntries()
                + " chunkRadiationTotal=" + status.totalChunkRadiation()
                + " craterRadiationMarkers=" + status.totalCraterRadiationMarkers()), false);
        for (WorldSavedDataDiagnostics.LevelStatus level : status.levels()) {
            source.sendSuccess(() -> Component.literal(level.summary()
                    + " tom=" + level.tomImpactSummary().detail()
                    + " annihilator=" + level.annihilatorSummary().detail()
                    + " satellites=" + level.satelliteSummary().detail()
                    + " pollution=" + level.pollutionSummary().detail()
                    + " chunkRadiation=" + level.chunkRadiationSummary().detail()
                    + " craterRadiation=" + level.craterRadiationSummary().detail()), false);
        }
        BlockMigrationHelper.MigrationDiagnostics migrations = status.migrations();
        source.sendSuccess(() -> Component.literal("chunk migrations " + migrations.summary()), false);
        return status.presentDataCount();
    }

    private static int worldSavedDataHealth(CommandSourceStack source, ServerLevel level) {
        WorldSavedDataDiagnostics.LevelHealthStatus health = WorldSavedDataDiagnostics.health(level);
        source.sendSuccess(() -> Component.literal("World SavedData health "
                + health.summary()
                + " readOnly=true"), false);
        health.issues().forEach(issue -> source.sendSuccess(() -> Component.literal(" ! " + issue), false));
        return health.clean() ? 1 : 0;
    }

    private static int worldSavedDataHealthAll(CommandSourceStack source) {
        WorldSavedDataDiagnostics.ServerHealthStatus health = WorldSavedDataDiagnostics.health(source.getServer());
        source.sendSuccess(() -> Component.literal("World SavedData healthAll dimensions=" + health.levels().size()
                + " cleanDimensions=" + health.cleanDimensions()
                + " problemDimensions=" + health.problemDimensions()
                + " totalProblems=" + health.totalProblems()
                + " totalDetailProblems=" + health.totalDetailProblems()
                + " problems={impactData=" + health.tomProblems()
                + ", annihilator=" + health.annihilatorProblems()
                + ", satellites=" + health.satelliteProblems()
                + ", hbmpollution=" + health.pollutionProblems()
                + ", hbm_chunk_radiation=" + health.chunkRadiationProblems()
                + ", hbm_crater_radiation=" + health.craterRadiationProblems() + "}"
                + " detailProblems={annihilatorProblemPools=" + health.annihilatorProblemPools()
                + ", satelliteProblemEntries=" + health.satelliteProblemEntries() + "}"
                + " readOnly=true"), false);
        for (WorldSavedDataDiagnostics.LevelHealthStatus level : health.levels()) {
            if (!level.clean()) {
                source.sendSuccess(() -> Component.literal(" ! " + level.summary()), false);
                level.issues().forEach(issue -> source.sendSuccess(() -> Component.literal("   - "
                        + level.dimension() + " " + issue), false));
            }
        }
        return health.totalProblems();
    }

    private static int worldSavedDataKnownData(CommandSourceStack source, ServerLevel level) {
        List<WorldSavedDataDiagnostics.KnownDataStatus> entries = WorldSavedDataDiagnostics.knownData(level);
        long present = entries.stream().filter(WorldSavedDataDiagnostics.KnownDataStatus::present).count();
        long primary = entries.stream().filter(entry -> entry.present() && entry.primary()).count();
        long fallback = entries.stream().filter(WorldSavedDataDiagnostics.KnownDataStatus::fallback).count();
        source.sendSuccess(() -> Component.literal("World SavedData known dimension="
                + level.dimension().location()
                + " present=" + present
                + "/" + entries.size()
                + " primary=" + primary
                + " fallback=" + fallback
                + " absent=" + (entries.size() - present)
                + " readOnly=true"), false);
        for (WorldSavedDataDiagnostics.KnownDataStatus entry : entries) {
            source.sendSuccess(() -> Component.literal(entry.summary()), false);
        }
        return (int) present;
    }

    private static int worldSavedDataKnownDataAll(CommandSourceStack source) {
        WorldSavedDataDiagnostics.ServerKnownDataStatus status =
                WorldSavedDataDiagnostics.knownData(source.getServer());
        source.sendSuccess(() -> Component.literal("World SavedData knownAll dimensions="
                + status.dimensionCount()
                + " present=" + status.presentCount()
                + "/" + status.entries().size()
                + " primary=" + status.primaryCount()
                + " fallback=" + status.fallbackCount()
                + " absent=" + status.absentCount()
                + " readOnly=true"), false);
        for (WorldSavedDataDiagnostics.KnownDataStatus entry : status.entries()) {
            source.sendSuccess(() -> Component.literal(entry.summary()), false);
        }
        return (int) status.presentCount();
    }

    private static int worldSavedDataSchema(CommandSourceStack source) {
        List<WorldSavedDataDiagnostics.KnownDataDefinition> definitions =
                WorldSavedDataDiagnostics.knownDataDefinitions();
        long fallbackCount = definitions.stream()
                .filter(WorldSavedDataDiagnostics.KnownDataDefinition::hasFallbacks).count();
        long promotionCount = definitions.stream()
                .filter(WorldSavedDataDiagnostics.KnownDataDefinition::explicitPromotionSupported).count();
        source.sendSuccess(() -> Component.literal("World SavedData schema knownTypes="
                + definitions.size()
                + " fallbackTypes=" + fallbackCount
                + " explicitPromotionTypes=" + promotionCount
                + " readOnly=true"), false);
        for (WorldSavedDataDiagnostics.KnownDataDefinition definition : definitions) {
            source.sendSuccess(() -> Component.literal(definition.summary()), false);
        }
        return definitions.size();
    }

    private static int worldSavedDataNormalizeKnown(CommandSourceStack source, ServerLevel level) {
        List<WorldSavedDataDiagnostics.SavedDataNormalizationResult> entries =
                WorldSavedDataDiagnostics.normalizeKnownData(level);
        long present = entries.stream().filter(WorldSavedDataDiagnostics.SavedDataNormalizationResult::present).count();
        long promoted = entries.stream().filter(WorldSavedDataDiagnostics.SavedDataNormalizationResult::promoted).count();
        long markedDirty = entries.stream()
                .filter(WorldSavedDataDiagnostics.SavedDataNormalizationResult::markedDirty).count();
        source.sendSuccess(() -> Component.literal("World SavedData normalize known dimension="
                + level.dimension().location()
                + " present=" + present
                + "/" + entries.size()
                + " promoted=" + promoted
                + " markedDirty=" + markedDirty
                + " writesExistingOnly=true"), markedDirty > 0);
        for (WorldSavedDataDiagnostics.SavedDataNormalizationResult entry : entries) {
            source.sendSuccess(() -> Component.literal(entry.summary()), entry.markedDirty());
        }
        return (int) markedDirty;
    }

    private static int worldSavedDataNormalizeKnownAll(CommandSourceStack source) {
        WorldSavedDataDiagnostics.ServerSavedDataNormalizationResult status =
                WorldSavedDataDiagnostics.normalizeKnownData(source.getServer());
        source.sendSuccess(() -> Component.literal("World SavedData normalize knownAll dimensions="
                + status.dimensionCount()
                + " present=" + status.presentCount()
                + "/" + status.entries().size()
                + " promoted=" + status.promotedCount()
                + " fallbackFound=" + status.fallbackFoundCount()
                + " markedDirty=" + status.markedDirtyCount()
                + " absent=" + status.absentCount()
                + " writesExistingOnly=true"), status.markedDirtyCount() > 0);
        for (WorldSavedDataDiagnostics.SavedDataNormalizationResult entry : status.entries()) {
            source.sendSuccess(() -> Component.literal(entry.summary()), entry.markedDirty());
        }
        return (int) status.markedDirtyCount();
    }

    private static int worldSavedDataPromotePollution(CommandSourceStack source, ServerLevel level) {
        WorldSavedDataDiagnostics.SavedDataPromotionResult result =
                WorldSavedDataDiagnostics.promotePollutionFallback(level);
        source.sendSuccess(() -> Component.literal("World SavedData promote pollution "
                + result.summary()
                + " fallbackName=" + PollutionSavedData.MODERN_COMPAT_DATA_NAME
                + " writesOnlyWhenFallback=true"), result.promoted());
        return result.promoted() ? 1 : 0;
    }

    private static int worldSavedDataPromotePollutionAll(CommandSourceStack source) {
        WorldSavedDataDiagnostics.ServerSavedDataPromotionResult status =
                WorldSavedDataDiagnostics.promotePollutionFallback(source.getServer());
        source.sendSuccess(() -> Component.literal("World SavedData promote pollution all dimensions="
                + status.levels().size()
                + " promoted=" + status.promotedCount()
                + " primary=" + status.primaryCount()
                + " fallbackFound=" + status.fallbackFoundCount()
                + " absent=" + status.absentCount()
                + " fallbackName=" + PollutionSavedData.MODERN_COMPAT_DATA_NAME
                + " writesOnlyWhenFallback=true"), status.promotedCount() > 0);
        for (WorldSavedDataDiagnostics.SavedDataPromotionResult result : status.levels()) {
            source.sendSuccess(() -> Component.literal(result.summary()), result.promoted());
        }
        return (int) status.promotedCount();
    }

    private static int worldSavedDataTomSummary(CommandSourceStack source) {
        Optional<TomImpactSavedData> data = TomImpactSavedData.getExisting(source.getLevel());
        if (data.isEmpty()) {
            source.sendSuccess(() -> Component.literal("impactData absent"), false);
            return 0;
        }
        TomImpactSavedData tom = data.get();
        source.sendSuccess(() -> Component.literal("impactData summary " + tom.snapshot().summary()
                + " load={" + tom.loadDiagnostics().summary() + "}"
                + " readOnly=true"), false);
        return tom.snapshot().hasClimate() || tom.snapshot().impact() ? 1 : 0;
    }

    private static int worldSavedDataTomSummaryAll(CommandSourceStack source) {
        WorldSavedDataDiagnostics.ServerStatus status = WorldSavedDataDiagnostics.inspect(source.getServer());
        source.sendSuccess(() -> Component.literal("impactData summaryAll dimensions=" + status.levels().size()
                + " climateDimensions=" + status.tomClimateDimensions()
                + " readOnly=true"), false);
        for (WorldSavedDataDiagnostics.LevelStatus level : status.levels()) {
            if (level.hasTomImpact()) {
                source.sendSuccess(() -> Component.literal(" - " + level.dimension()
                        + " " + level.tomImpactSummary().detail()), false);
            }
        }
        return (int) status.tomClimateDimensions();
    }

    private static int worldSavedDataTomCache(CommandSourceStack source) {
        TomImpactSavedData cached = TomImpactSavedData.getLastCachedOrNull();
        if (cached == null) {
            source.sendSuccess(() -> Component.literal("impactData lastCached=null readOnly=true"), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("impactData lastCached "
                + cached.snapshot().summary()
                + " load={" + cached.loadDiagnostics().summary() + "}"
                + " readOnly=true"), false);
        return 1;
    }

    private static int worldSavedDataMigrationStatus(CommandSourceStack source) {
        BlockMigrationHelper.MigrationDiagnostics migrations = BlockMigrationHelper.diagnostics();
        source.sendSuccess(() -> Component.literal("chunk migrations " + migrations.summary()), false);
        source.sendSuccess(() -> Component.literal("lastLoad " + migrations.lastLoadResult().summary()), false);
        return (int) Math.min(Integer.MAX_VALUE, migrations.loadedChunks());
    }

    private static int worldSavedDataMigrationLast(CommandSourceStack source) {
        BlockMigrationHelper.MigrationResult result = BlockMigrationHelper.diagnostics().lastLoadResult();
        source.sendSuccess(() -> Component.literal("chunk migration lastLoad " + result.summary()), false);
        return result.migrated() ? 1 : 0;
    }

    private static int worldSavedDataMigrationMarker(CommandSourceStack source, Integer previousBuild) {
        BlockMigrationHelper.MigrationMarker marker = BlockMigrationHelper.inspectMarker(previousBuild);
        source.sendSuccess(() -> Component.literal("chunk migration marker " + marker.summary()
                + " readOnly=true"), false);
        return marker.needsMigration() ? 1 : 0;
    }

    private static int resetWorldSavedDataMigrationDiagnostics(CommandSourceStack source) {
        BlockMigrationHelper.resetDiagnostics();
        source.sendSuccess(() -> Component.literal("Reset world saveddata chunk migration diagnostics."), true);
        return 1;
    }

    private static int worldSavedDataChunkStatus(CommandSourceStack source, ChunkPos pos) {
        WorldUtil.ChunkAccessReport report = WorldUtil.inspectChunk(source.getLevel(), pos);
        source.sendSuccess(() -> Component.literal("chunk " + report.chunkX() + "," + report.chunkZ()
                + " loaded=" + report.loaded()
                + " full=" + report.full()
                + " failed=" + report.failed()
                + " detail=" + report.detail()
                + " noGeneration=true"), false);
        return report.available() ? 1 : 0;
    }

    private static int worldSavedDataChunkSquareStatus(CommandSourceStack source, int radius) {
        ChunkPos center = new ChunkPos(BlockPos.containing(source.getPosition()));
        WorldUtil.ChunkBatchReport report = WorldUtil.inspectChunksInSquare(source.getLevel(), center, radius);
        source.sendSuccess(() -> Component.literal("chunk square center=" + center.x + "," + center.z
                + " radius=" + radius
                + " requested=" + report.requestedChunks()
                + " loaded=" + report.loadedChunks()
                + " full=" + report.fullChunks()
                + " failed=" + report.failedChunks()
                + " complete=" + report.complete()
                + " noGeneration=true"), false);
        report.chunks().stream()
                .filter(chunk -> !chunk.available())
                .limit(8)
                .forEach(chunk -> source.sendSuccess(() -> Component.literal(" - " + chunk.chunkX() + ","
                        + chunk.chunkZ()
                        + " loaded=" + chunk.loaded()
                        + " full=" + chunk.full()
                        + " failed=" + chunk.failed()
                        + " detail=" + chunk.detail()), false));
        return report.fullChunks();
    }

    private static int worldSavedDataSubChunkStatus(CommandSourceStack source, SubChunkKey key) {
        SubChunkSnapshot.SnapshotStatus status = SubChunkSnapshot.inspect(source.getLevel(), key, false, 4);
        source.sendSuccess(() -> Component.literal("subchunk " + formatSubChunkKey(key)
                + " chunkLoaded=" + status.chunk().loaded()
                + " chunkFull=" + status.chunk().full()
                + " validSection=" + status.validSection()
                + " palette=" + status.paletteSize()
                + " nonAir=" + status.nonAirBlocks()
                + " detail=" + status.detail()
                + " noGeneration=true"), false);
        for (SubChunkSnapshot.WorldBlockSample sample : status.samples()) {
            source.sendSuccess(() -> Component.literal(" - " + sample.pos().toShortString()
                    + " " + formatBlockState(sample.state())), false);
        }
        sendBlockStateCounts(source, "state", status.blockStateCounts(), 8);
        sendBlockCounts(source, "block", status.blockCounts(), 8);
        return status.nonAirBlocks();
    }

    private static int worldSavedDataSubChunkBatchStatus(CommandSourceStack source, String label,
                                                        List<SubChunkKey> keys) {
        int requested = keys.size();
        List<SubChunkKey> scanned = requested > MAX_SUBCHUNK_DIAGNOSTIC_SCAN
                ? keys.subList(0, MAX_SUBCHUNK_DIAGNOSTIC_SCAN) : keys;
        SubChunkSnapshot.SnapshotBatch report = SubChunkSnapshot.inspectAll(source.getLevel(), scanned, false, 0);
        source.sendSuccess(() -> Component.literal("subchunk " + label
                + " requested=" + requested
                + " scanned=" + report.requestedSubChunks()
                + " fullChunks=" + report.fullChunks()
                + " validSections=" + report.validSections()
                + " nonEmpty=" + report.nonEmptySubChunks()
                + " nonAir=" + report.nonAirBlocks()
                + " complete=" + report.complete()
                + " noGeneration=true"
                + (requested > scanned.size() ? " truncated=true" : "")), false);
        report.statuses().stream()
                .filter(SubChunkSnapshot.SnapshotStatus::nonEmpty)
                .limit(8)
                .forEach(status -> source.sendSuccess(() -> Component.literal(" - " + formatSubChunkKey(status.key())
                        + " nonAir=" + status.nonAirBlocks()
                        + " palette=" + status.paletteSize()), false));
        report.statuses().stream()
                .filter(status -> !status.chunk().full() || !status.validSection())
                .limit(8)
                .forEach(status -> source.sendSuccess(() -> Component.literal(" ! " + formatSubChunkKey(status.key())
                        + " chunkFull=" + status.chunk().full()
                        + " validSection=" + status.validSection()
                        + " detail=" + status.detail()), false));
        sendBlockStateCounts(source, "state", report.blockStateCounts(), 8);
        sendBlockCounts(source, "block", report.blockCounts(), 8);
        return report.nonEmptySubChunks();
    }

    private static int worldSavedDataSubChunkBlockCount(CommandSourceStack source, ResourceLocation blockId,
                                                        String label, List<SubChunkKey> keys) {
        Optional<Block> block = parseBlock(source, blockId);
        if (block.isEmpty()) {
            return 0;
        }
        int requested = keys.size();
        List<SubChunkKey> scanned = requested > MAX_SUBCHUNK_DIAGNOSTIC_SCAN
                ? keys.subList(0, MAX_SUBCHUNK_DIAGNOSTIC_SCAN) : keys;
        SubChunkSnapshot.SnapshotBatch report = SubChunkSnapshot.inspectAll(source.getLevel(), scanned, false, 0);
        int count = report.blockCount(block.get());
        source.sendSuccess(() -> Component.literal("subchunk " + label
                + " block=" + blockId
                + " count=" + count
                + " requested=" + requested
                + " scanned=" + report.requestedSubChunks()
                + " fullChunks=" + report.fullChunks()
                + " validSections=" + report.validSections()
                + " nonEmpty=" + report.nonEmptySubChunks()
                + " noGeneration=true"
                + (requested > scanned.size() ? " truncated=true" : "")), false);
        report.statuses().stream()
                .filter(status -> status.blockCount(block.get()) > 0)
                .limit(8)
                .forEach(status -> source.sendSuccess(() -> Component.literal(" - " + formatSubChunkKey(status.key())
                        + " " + blockId + "=" + status.blockCount(block.get())), false));
        return count;
    }

    private static String formatSubChunkKey(SubChunkKey key) {
        return key.getChunkXPos() + "," + key.getSectionY() + "," + key.getChunkZPos();
    }

    private static String formatBlockState(net.minecraft.world.level.block.state.BlockState state) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return key == null ? state.toString() : key + (state.getValues().isEmpty() ? "" : state.getValues().toString());
    }

    private static String formatBlock(Block block) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        return key == null ? block.toString() : key.toString();
    }

    private static void sendBlockStateCounts(CommandSourceStack source, String prefix,
                                             Map<net.minecraft.world.level.block.state.BlockState, Integer> counts,
                                             int limit) {
        counts.entrySet().stream()
                .sorted(Map.Entry.<net.minecraft.world.level.block.state.BlockState, Integer>comparingByValue().reversed())
                .limit(limit)
                .forEach(entry -> source.sendSuccess(() -> Component.literal(" " + prefix + " "
                        + formatBlockState(entry.getKey()) + "=" + entry.getValue()), false));
    }

    private static void sendBlockCounts(CommandSourceStack source, String prefix, Map<Block, Integer> counts,
                                        int limit) {
        counts.entrySet().stream()
                .sorted(Map.Entry.<Block, Integer>comparingByValue().reversed())
                .limit(limit)
                .forEach(entry -> source.sendSuccess(() -> Component.literal(" " + prefix + " "
                        + formatBlock(entry.getKey()) + "=" + entry.getValue()), false));
    }

    private static CompletableFuture<Suggestions> suggestAnnihilatorPools(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
                                                                          SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                AnnihilatorSavedData.getExisting(context.getSource().getLevel())
                        .map(AnnihilatorSavedData::poolNamesSnapshot)
                        .orElse(List.of()),
                builder);
    }

    private static CompletableFuture<Suggestions> suggestItemIds(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
                                                                 SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                ForgeRegistries.ITEMS.getKeys().stream().map(ResourceLocation::toString),
                builder);
    }

    private static CompletableFuture<Suggestions> suggestBlockIds(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
                                                                  SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                ForgeRegistries.BLOCKS.getKeys().stream().map(ResourceLocation::toString),
                builder);
    }

    private static CompletableFuture<Suggestions> suggestHbmFluids(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
                                                                   SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(HbmFluids.all().stream().map(FluidType::toPath), builder);
    }

    private static CompletableFuture<Suggestions> suggestAnnihilatorKinds(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
                                                                          SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                Stream.of(AnnihilatorSavedData.Kind.values())
                        .filter(kind -> kind != AnnihilatorSavedData.Kind.UNKNOWN)
                        .map(AnnihilatorSavedData.Kind::commandName),
                builder);
    }

    private static int worldSavedDataAnnihilatorSummary(CommandSourceStack source) {
        Optional<AnnihilatorSavedData> data = AnnihilatorSavedData.getExisting(source.getLevel());
        if (data.isEmpty()) {
            source.sendSuccess(() -> Component.literal("annihilator absent"), false);
            return 0;
        }
        AnnihilatorSavedData annihilator = data.get();
        source.sendSuccess(() -> Component.literal("annihilator summary pools=" + annihilator.poolCount()
                + " entries=" + annihilator.poolEntryCount()
                + " total=" + annihilator.totalAmount()
                + " keys=" + annihilator.keyKindCounts()
                + " kindTotals=" + annihilator.keyKindTotals()
                + " readOnly=true"), false);
        sendAnnihilatorPoolSummaries(source, annihilator.topPoolSummariesSnapshot(8));
        return annihilator.poolCount();
    }

    private static int worldSavedDataAnnihilatorSummaryAll(CommandSourceStack source) {
        WorldSavedDataDiagnostics.ServerStatus status = WorldSavedDataDiagnostics.inspect(source.getServer());
        source.sendSuccess(() -> Component.literal("annihilator summaryAll dimensions=" + status.levels().size()
                + " pools=" + status.totalAnnihilatorPools()
                + " entries=" + status.totalAnnihilatorEntries()
                + " problemPools=" + status.totalAnnihilatorProblemPools()
                + " total=" + status.totalAnnihilatorAmount()
                + " readOnly=true"), false);
        for (WorldSavedDataDiagnostics.LevelStatus level : status.levels()) {
            if (level.hasAnnihilator()) {
                source.sendSuccess(() -> Component.literal(" - " + level.dimension()
                        + " " + level.annihilatorSummary().detail()), false);
            }
        }
        return status.totalAnnihilatorPools();
    }

    private static int worldSavedDataAnnihilatorPools(CommandSourceStack source) {
        Optional<AnnihilatorSavedData> data = AnnihilatorSavedData.getExisting(source.getLevel());
        if (data.isEmpty()) {
            source.sendSuccess(() -> Component.literal("annihilator absent"), false);
            return 0;
        }
        AnnihilatorSavedData annihilator = data.get();
        source.sendSuccess(() -> Component.literal("annihilator pools=" + annihilator.poolCount()
                + " entries=" + annihilator.poolEntryCount()
                + " total=" + annihilator.totalAmount()), false);
        for (String pool : annihilator.poolNamesSnapshot()) {
            source.sendSuccess(() -> Component.literal(pool
                    + " entries=" + annihilator.poolEntryCount(pool)
                    + " total=" + annihilator.totalAmount(pool)
                    + " keys=" + annihilator.keyKindCounts(pool)
                    + " kindTotals=" + annihilator.keyKindTotals(pool)), false);
        }
        return annihilator.poolCount();
    }

    private static int worldSavedDataAnnihilatorLoad(CommandSourceStack source) {
        Optional<AnnihilatorSavedData> data = AnnihilatorSavedData.getExisting(source.getLevel());
        if (data.isEmpty()) {
            source.sendSuccess(() -> Component.literal("annihilator absent"), false);
            return 0;
        }
        AnnihilatorSavedData annihilator = data.get();
        AnnihilatorSavedData.LoadDiagnostics diagnostics = annihilator.loadDiagnostics();
        List<AnnihilatorSavedData.PoolLoadDiagnostics> problemPools =
                annihilator.problemPoolLoadDiagnosticsSnapshot();
        source.sendSuccess(() -> Component.literal("annihilator load " + diagnostics.summary()
                + " poolDiagnostics=" + annihilator.poolLoadDiagnosticsSnapshot().size()
                + " problemPools=" + problemPools.size()
                + " readOnly=true"), false);
        problemPools.stream()
                .limit(12)
                .forEach(pool -> source.sendSuccess(() -> Component.literal(" ! " + pool.summary()), false));
        if (problemPools.size() > 12) {
            source.sendSuccess(() -> Component.literal(" ! additionalProblemPools="
                    + (problemPools.size() - 12)
                    + " truncated=true"), false);
        }
        return diagnostics.clean() ? 1 : 0;
    }

    private static int worldSavedDataAnnihilatorLoadAll(CommandSourceStack source) {
        int dimensions = 0;
        int present = 0;
        int rootProblems = 0;
        int poolDiagnostics = 0;
        int problemPools = 0;
        int shownProblems = 0;
        int maxProblemDetails = 24;
        List<String> problemDetails = new ArrayList<>();
        for (ServerLevel level : source.getServer().getAllLevels()) {
            dimensions++;
            Optional<AnnihilatorSavedData> data = AnnihilatorSavedData.getExisting(level);
            if (data.isEmpty()) {
                continue;
            }
            present++;
            AnnihilatorSavedData annihilator = data.get();
            AnnihilatorSavedData.LoadDiagnostics diagnostics = annihilator.loadDiagnostics();
            List<AnnihilatorSavedData.PoolLoadDiagnostics> levelProblemPools =
                    annihilator.problemPoolLoadDiagnosticsSnapshot();
            int levelPoolDiagnostics = annihilator.poolLoadDiagnosticsSnapshot().size();
            rootProblems += diagnostics.problemCount();
            poolDiagnostics += levelPoolDiagnostics;
            problemPools += levelProblemPools.size();
            source.sendSuccess(() -> Component.literal(" - " + level.dimension().location()
                    + " annihilator load " + diagnostics.summary()
                    + " poolDiagnostics=" + levelPoolDiagnostics
                    + " problemPools=" + levelProblemPools.size()), false);
            for (AnnihilatorSavedData.PoolLoadDiagnostics pool : levelProblemPools) {
                if (shownProblems < maxProblemDetails) {
                    problemDetails.add(" ! " + level.dimension().location() + " " + pool.summary());
                    shownProblems++;
                }
            }
        }
        int additionalProblems = Math.max(0, problemPools - shownProblems);
        String summary = "annihilator loadAll dimensions=" + dimensions
                + " present=" + present
                + " rootProblems=" + rootProblems
                + " poolDiagnostics=" + poolDiagnostics
                + " problemPools=" + problemPools
                + " shownProblemPools=" + shownProblems
                + " readOnly=true";
        source.sendSuccess(() -> Component.literal(summary), false);
        problemDetails.forEach(detail -> source.sendSuccess(() -> Component.literal(detail), false));
        if (additionalProblems > 0) {
            source.sendSuccess(() -> Component.literal(" ! additionalProblemPools="
                    + additionalProblems
                    + " truncated=true"), false);
        }
        return rootProblems + problemPools;
    }

    private static int worldSavedDataAnnihilatorPool(CommandSourceStack source, String pool) {
        Optional<AnnihilatorSavedData> data = AnnihilatorSavedData.getExisting(source.getLevel());
        if (data.isEmpty() || !data.get().hasPool(pool)) {
            source.sendFailure(Component.literal("No annihilator pool named '" + pool + "'."));
            return 0;
        }
        AnnihilatorSavedData annihilator = data.get();
        source.sendSuccess(() -> Component.literal("annihilator pool " + pool
                + " entries=" + annihilator.poolEntryCount(pool)
                + " total=" + annihilator.totalAmount(pool)
                + " keys=" + annihilator.keyKindCounts(pool)
                + " kindTotals=" + annihilator.keyKindTotals(pool)), false);
        sendAnnihilatorEntries(source, annihilator.topEntriesSnapshot(pool, 12));
        return annihilator.poolEntryCount(pool);
    }

    private static int worldSavedDataAnnihilatorTop(CommandSourceStack source, String pool, int limit) {
        Optional<AnnihilatorSavedData> data = AnnihilatorSavedData.getExisting(source.getLevel());
        if (data.isEmpty() || !data.get().hasPool(pool)) {
            source.sendFailure(Component.literal("No annihilator pool named '" + pool + "'."));
            return 0;
        }
        List<Map.Entry<AnnihilatorSavedData.PoolKey, java.math.BigInteger>> entries =
                data.get().topEntriesSnapshot(pool, limit);
        source.sendSuccess(() -> Component.literal("annihilator top pool=" + pool
                + " limit=" + limit
                + " shown=" + entries.size()), false);
        sendAnnihilatorEntries(source, entries);
        return entries.size();
    }

    private static int worldSavedDataAnnihilatorKind(CommandSourceStack source, String pool, String kindName,
                                                     int limit) {
        Optional<AnnihilatorSavedData.Kind> kind = AnnihilatorSavedData.Kind.byCommandName(kindName);
        if (kind.isEmpty()) {
            source.sendFailure(Component.literal("Unknown annihilator key kind: " + kindName));
            return 0;
        }
        Optional<AnnihilatorSavedData> data = AnnihilatorSavedData.getExisting(source.getLevel());
        if (data.isEmpty() || !data.get().hasPool(pool)) {
            source.sendFailure(Component.literal("No annihilator pool named '" + pool + "'."));
            return 0;
        }
        List<Map.Entry<AnnihilatorSavedData.PoolKey, java.math.BigInteger>> entries =
                data.get().entriesByKindSnapshot(pool, kind.get(), limit);
        source.sendSuccess(() -> Component.literal("annihilator kind pool=" + pool
                + " kind=" + kind.get().commandName()
                + " total=" + data.get().keyKindTotals(pool).getOrDefault(kind.get(), java.math.BigInteger.ZERO)
                + " entries=" + data.get().keyKindCounts(pool).getOrDefault(kind.get(), 0)
                + " shown=" + entries.size()), false);
        sendAnnihilatorEntries(source, entries);
        return entries.size();
    }

    private static void sendAnnihilatorPoolSummaries(CommandSourceStack source,
                                                     List<AnnihilatorSavedData.PoolSummary> pools) {
        pools.forEach(pool -> source.sendSuccess(() -> Component.literal(" - " + pool.name()
                + " entries=" + pool.entries()
                + " total=" + pool.totalAmount()
                + " keys=" + pool.keyKindCounts()
                + " kindTotals=" + pool.keyKindTotals()), false));
    }

    private static void sendAnnihilatorEntries(CommandSourceStack source,
                                               List<Map.Entry<AnnihilatorSavedData.PoolKey, java.math.BigInteger>> entries) {
        entries.forEach(entry -> source.sendSuccess(() -> Component.literal(formatAnnihilatorKey(entry.getKey())
                + "=" + entry.getValue()), false));
    }

    private static String formatAnnihilatorKey(AnnihilatorSavedData.PoolKey key) {
        return switch (key.kind()) {
            case ITEM -> "item:" + key.item();
            case ITEM_META -> "item_meta:" + key.item() + ":" + key.meta();
            case FLUID -> "fluid:" + key.fluid();
            case ORE_DICT -> "oredict:" + key.oreDict();
            case UNKNOWN -> "unknown";
        };
    }

    private static int worldSavedDataAnnihilatorItemAmount(CommandSourceStack source, String pool,
                                                          ResourceLocation itemName) {
        Optional<Item> item = parseItem(source, itemName);
        if (item.isEmpty()) {
            return 0;
        }
        return worldSavedDataAnnihilatorAmount(source, pool, "item:" + ForgeRegistries.ITEMS.getKey(item.get()),
                data -> data.getItemAmount(pool, item.get()));
    }

    private static int worldSavedDataAnnihilatorItemMetaAmount(CommandSourceStack source, String pool,
                                                              ResourceLocation itemName, int legacyMeta) {
        Optional<Item> item = parseItem(source, itemName);
        if (item.isEmpty()) {
            return 0;
        }
        ItemStack stack = new ItemStack(item.get());
        return worldSavedDataAnnihilatorAmount(source, pool,
                "item_meta:" + ForgeRegistries.ITEMS.getKey(item.get()) + ":" + legacyMeta,
                data -> data.getItemMetaAmount(pool, stack, legacyMeta));
    }

    private static int worldSavedDataAnnihilatorFluidAmount(CommandSourceStack source, String pool, String fluidName) {
        FluidType type = HbmFluids.fromName(fluidName);
        if (type == HbmFluids.NONE) {
            source.sendFailure(Component.literal("Unknown HBM fluid: " + fluidName));
            return 0;
        }
        return worldSavedDataAnnihilatorAmount(source, pool, "fluid:" + type.getName(),
                data -> data.getFluidAmount(pool, type));
    }

    private static int worldSavedDataAnnihilatorOreDictAmount(CommandSourceStack source, String pool, String oreDict) {
        return worldSavedDataAnnihilatorAmount(source, pool, "oredict:" + oreDict,
                data -> data.getOreDictAmount(pool, oreDict));
    }

    private static int worldSavedDataAnnihilatorAmount(CommandSourceStack source, String pool, String key,
                                                       java.util.function.Function<AnnihilatorSavedData, java.math.BigInteger> getter) {
        Optional<AnnihilatorSavedData> data = AnnihilatorSavedData.getExisting(source.getLevel());
        if (data.isEmpty() || !data.get().hasPool(pool)) {
            source.sendFailure(Component.literal("No annihilator pool named '" + pool + "'."));
            return 0;
        }
        java.math.BigInteger amount = getter.apply(data.get());
        source.sendSuccess(() -> Component.literal("annihilator pool " + pool + " " + key + "=" + amount), false);
        return amount.signum() > 0 ? 1 : 0;
    }

    private static int worldSavedDataSatelliteSummary(CommandSourceStack source) {
        Optional<SatelliteSavedData> data = SatelliteSavedData.getExisting(source.getLevel());
        if (data.isEmpty()) {
            source.sendSuccess(() -> Component.literal("satellites absent"), false);
            return 0;
        }
        SatelliteSavedData satellites = data.get();
        SatelliteSavedData.SatelliteStats stats = satellites.statsSnapshot(16, 8);
        source.sendSuccess(() -> Component.literal("satellites summary " + stats.summary()
                + " readOnly=true"), false);
        sendSatelliteSummaries(source, stats.satellites());
        return stats.entries();
    }

    private static int worldSavedDataSatelliteSummaryAll(CommandSourceStack source) {
        WorldSavedDataDiagnostics.ServerStatus status = WorldSavedDataDiagnostics.inspect(source.getServer());
        source.sendSuccess(() -> Component.literal("satellites summaryAll dimensions=" + status.levels().size()
                + " entries=" + status.totalSatelliteCount()
                + " problemEntries=" + status.totalSatelliteProblemEntries()
                + " readOnly=true"), false);
        for (WorldSavedDataDiagnostics.LevelStatus level : status.levels()) {
            if (level.hasSatellites()) {
                source.sendSuccess(() -> Component.literal(" - " + level.dimension()
                        + " " + level.satelliteSummary().detail()), false);
            }
        }
        return status.totalSatelliteCount();
    }

    private static int worldSavedDataSatelliteLoad(CommandSourceStack source) {
        Optional<SatelliteSavedData> data = SatelliteSavedData.getExisting(source.getLevel());
        if (data.isEmpty()) {
            source.sendSuccess(() -> Component.literal("satellites absent"), false);
            return 0;
        }
        SatelliteSavedData satellites = data.get();
        SatelliteSavedData.LoadDiagnostics diagnostics = satellites.loadDiagnostics();
        List<SatelliteSavedData.EntryLoadDiagnostics> problemEntries =
                satellites.problemEntryLoadDiagnosticsSnapshot();
        source.sendSuccess(() -> Component.literal("satellites load " + diagnostics.summary()
                + " legacyEntryDiagnostics=" + satellites.legacyEntryLoadDiagnosticsSnapshot().size()
                + " modernEntryDiagnostics=" + satellites.modernEntryLoadDiagnosticsSnapshot().size()
                + " problemEntries=" + problemEntries.size()
                + " readOnly=true"), false);
        problemEntries.stream()
                .limit(12)
                .forEach(entry -> source.sendSuccess(() -> Component.literal(" ! " + entry.summary()), false));
        if (problemEntries.size() > 12) {
            source.sendSuccess(() -> Component.literal(" ! additionalProblemEntries="
                    + (problemEntries.size() - 12)
                    + " truncated=true"), false);
        }
        return diagnostics.clean() ? 1 : 0;
    }

    private static int worldSavedDataSatelliteLoadAll(CommandSourceStack source) {
        int dimensions = 0;
        int present = 0;
        int rootProblems = 0;
        int legacyEntryDiagnostics = 0;
        int modernEntryDiagnostics = 0;
        int problemEntries = 0;
        int shownProblems = 0;
        int maxProblemDetails = 24;
        List<String> problemDetails = new ArrayList<>();
        for (ServerLevel level : source.getServer().getAllLevels()) {
            dimensions++;
            Optional<SatelliteSavedData> data = SatelliteSavedData.getExisting(level);
            if (data.isEmpty()) {
                continue;
            }
            present++;
            SatelliteSavedData satellites = data.get();
            SatelliteSavedData.LoadDiagnostics diagnostics = satellites.loadDiagnostics();
            List<SatelliteSavedData.EntryLoadDiagnostics> levelProblemEntries =
                    satellites.problemEntryLoadDiagnosticsSnapshot();
            int levelLegacyEntryDiagnostics = satellites.legacyEntryLoadDiagnosticsSnapshot().size();
            int levelModernEntryDiagnostics = satellites.modernEntryLoadDiagnosticsSnapshot().size();
            rootProblems += diagnostics.problemCount();
            legacyEntryDiagnostics += levelLegacyEntryDiagnostics;
            modernEntryDiagnostics += levelModernEntryDiagnostics;
            problemEntries += levelProblemEntries.size();
            source.sendSuccess(() -> Component.literal(" - " + level.dimension().location()
                    + " satellites load " + diagnostics.summary()
                    + " legacyEntryDiagnostics=" + levelLegacyEntryDiagnostics
                    + " modernEntryDiagnostics=" + levelModernEntryDiagnostics
                    + " problemEntries=" + levelProblemEntries.size()), false);
            for (SatelliteSavedData.EntryLoadDiagnostics entry : levelProblemEntries) {
                if (shownProblems < maxProblemDetails) {
                    problemDetails.add(" ! " + level.dimension().location() + " " + entry.summary());
                    shownProblems++;
                }
            }
        }
        int additionalProblems = Math.max(0, problemEntries - shownProblems);
        String summary = "satellites loadAll dimensions=" + dimensions
                + " present=" + present
                + " rootProblems=" + rootProblems
                + " legacyEntryDiagnostics=" + legacyEntryDiagnostics
                + " modernEntryDiagnostics=" + modernEntryDiagnostics
                + " problemEntries=" + problemEntries
                + " shownProblemEntries=" + shownProblems
                + " readOnly=true";
        source.sendSuccess(() -> Component.literal(summary), false);
        problemDetails.forEach(detail -> source.sendSuccess(() -> Component.literal(detail), false));
        if (additionalProblems > 0) {
            source.sendSuccess(() -> Component.literal(" ! additionalProblemEntries="
                    + additionalProblems
                    + " truncated=true"), false);
        }
        return rootProblems + problemEntries;
    }

    private static int worldSavedDataPollutionLoad(CommandSourceStack source) {
        Optional<WorldSavedDataHelper.ExistingDataLookup<PollutionSavedData>> lookup =
                WorldSavedDataHelper.findExistingWithFallback(source.getLevel(), PollutionSavedData.DATA_NAME,
                        PollutionSavedData::load, PollutionSavedData.MODERN_COMPAT_DATA_NAME);
        if (lookup.isEmpty()) {
            source.sendSuccess(() -> Component.literal("hbmpollution absent"), false);
            return 0;
        }
        WorldSavedDataHelper.ExistingDataLookup<PollutionSavedData> result = lookup.get();
        PollutionSavedData.LoadDiagnostics diagnostics = result.data().loadDiagnostics();
        source.sendSuccess(() -> Component.literal("hbmpollution load " + result.summary()
                + " " + diagnostics.summary()
                + " readOnly=true"), false);
        return diagnostics.clean() ? 1 : 0;
    }

    private static int worldSavedDataPollutionLoadAll(CommandSourceStack source) {
        int dimensions = 0;
        int present = 0;
        int primary = 0;
        int fallback = 0;
        int rootProblems = 0;
        for (ServerLevel level : source.getServer().getAllLevels()) {
            dimensions++;
            Optional<WorldSavedDataHelper.ExistingDataLookup<PollutionSavedData>> lookup =
                    WorldSavedDataHelper.findExistingWithFallback(level, PollutionSavedData.DATA_NAME,
                            PollutionSavedData::load, PollutionSavedData.MODERN_COMPAT_DATA_NAME);
            if (lookup.isEmpty()) {
                continue;
            }
            present++;
            WorldSavedDataHelper.ExistingDataLookup<PollutionSavedData> result = lookup.get();
            if (result.primary()) {
                primary++;
            } else {
                fallback++;
            }
            PollutionSavedData.LoadDiagnostics diagnostics = result.data().loadDiagnostics();
            rootProblems += diagnostics.problemCount();
            String detail = " - " + level.dimension().location()
                    + " hbmpollution load " + result.summary()
                    + " " + diagnostics.summary();
            source.sendSuccess(() -> Component.literal(detail), false);
        }
        String summary = "hbmpollution loadAll dimensions=" + dimensions
                + " present=" + present
                + " primary=" + primary
                + " fallback=" + fallback
                + " rootProblems=" + rootProblems
                + " readOnly=true";
        source.sendSuccess(() -> Component.literal(summary), false);
        return rootProblems;
    }

    private static int worldSavedDataChunkRadiationLoad(CommandSourceStack source) {
        Optional<RadiationSavedData> data = WorldSavedDataHelper.getExisting(source.getLevel(),
                RadiationSavedData.DATA_NAME, RadiationSavedData::load);
        if (data.isEmpty()) {
            source.sendSuccess(() -> Component.literal("hbm_chunk_radiation absent"), false);
            return 0;
        }
        RadiationSavedData.LoadDiagnostics diagnostics = data.get().loadDiagnostics();
        source.sendSuccess(() -> Component.literal("hbm_chunk_radiation load "
                + diagnostics.summary()
                + " readOnly=true"), false);
        return diagnostics.clean() ? 1 : 0;
    }

    private static int worldSavedDataChunkRadiationLoadAll(CommandSourceStack source) {
        int dimensions = 0;
        int present = 0;
        int rootProblems = 0;
        int entries = 0;
        for (ServerLevel level : source.getServer().getAllLevels()) {
            dimensions++;
            Optional<RadiationSavedData> data = WorldSavedDataHelper.getExisting(level,
                    RadiationSavedData.DATA_NAME, RadiationSavedData::load);
            if (data.isEmpty()) {
                continue;
            }
            present++;
            RadiationSavedData.LoadDiagnostics diagnostics = data.get().loadDiagnostics();
            rootProblems += diagnostics.problemCount();
            entries += diagnostics.entries();
            String detail = " - " + level.dimension().location()
                    + " hbm_chunk_radiation load " + diagnostics.summary();
            source.sendSuccess(() -> Component.literal(detail), false);
        }
        String summary = "hbm_chunk_radiation loadAll dimensions=" + dimensions
                + " present=" + present
                + " entries=" + entries
                + " rootProblems=" + rootProblems
                + " readOnly=true";
        source.sendSuccess(() -> Component.literal(summary), false);
        return rootProblems;
    }

    private static int worldSavedDataCraterRadiationLoad(CommandSourceStack source) {
        Optional<CraterRadiationData> data = CraterRadiationData.getExisting(source.getLevel());
        if (data.isEmpty()) {
            source.sendSuccess(() -> Component.literal("hbm_crater_radiation absent"), false);
            return 0;
        }
        CraterRadiationData.LoadDiagnostics diagnostics = data.get().loadDiagnostics();
        source.sendSuccess(() -> Component.literal("hbm_crater_radiation load "
                + diagnostics.summary()
                + " readOnly=true"), false);
        return diagnostics.clean() ? 1 : 0;
    }

    private static int worldSavedDataCraterRadiationLoadAll(CommandSourceStack source) {
        int dimensions = 0;
        int present = 0;
        int rootProblems = 0;
        int cells = 0;
        int zones = 0;
        for (ServerLevel level : source.getServer().getAllLevels()) {
            dimensions++;
            Optional<CraterRadiationData> data = CraterRadiationData.getExisting(level);
            if (data.isEmpty()) {
                continue;
            }
            present++;
            CraterRadiationData.LoadDiagnostics diagnostics = data.get().loadDiagnostics();
            rootProblems += diagnostics.problemCount();
            cells += diagnostics.cells();
            zones += diagnostics.zones();
            String detail = " - " + level.dimension().location()
                    + " hbm_crater_radiation load " + diagnostics.summary();
            source.sendSuccess(() -> Component.literal(detail), false);
        }
        String summary = "hbm_crater_radiation loadAll dimensions=" + dimensions
                + " present=" + present
                + " cells=" + cells
                + " zones=" + zones
                + " rootProblems=" + rootProblems
                + " readOnly=true";
        source.sendSuccess(() -> Component.literal(summary), false);
        return rootProblems;
    }

    private static int worldSavedDataSatelliteTypes(CommandSourceStack source) {
        Optional<SatelliteSavedData> data = SatelliteSavedData.getExisting(source.getLevel());
        if (data.isEmpty()) {
            source.sendSuccess(() -> Component.literal("satellites absent"), false);
            return 0;
        }
        SatelliteSavedData satellites = data.get();
        source.sendSuccess(() -> Component.literal("satellites types=" + satellites.typeCounts()
                + " cargoTypes=" + satellites.cargoTypeCounts()
                + " cargoPools=" + satellites.cargoPoolCounts()
                + " readOnly=true"), false);
        return satellites.size();
    }

    private static int worldSavedDataSatelliteFrequencies(CommandSourceStack source, int limit) {
        Optional<SatelliteSavedData> data = SatelliteSavedData.getExisting(source.getLevel());
        if (data.isEmpty()) {
            source.sendSuccess(() -> Component.literal("satellites absent"), false);
            return 0;
        }
        List<Integer> frequencies = data.get().frequenciesSnapshot(limit);
        source.sendSuccess(() -> Component.literal("satellites frequencies limit=" + limit
                + " shown=" + frequencies.size()
                + " values=" + frequencies
                + " readOnly=true"), false);
        return frequencies.size();
    }

    private static void sendSatelliteSummaries(CommandSourceStack source,
                                               List<SatelliteSavedData.SatelliteSummary> satellites) {
        satellites.forEach(satellite -> source.sendSuccess(() -> Component.literal(" - "
                + satellite.detail()), false));
    }

    private static Optional<Item> parseItem(CommandSourceStack source, ResourceLocation id) {
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == Items.AIR) {
            source.sendFailure(Component.literal("Unknown item: " + id));
            return Optional.empty();
        }
        return Optional.of(item);
    }

    private static Optional<Block> parseBlock(CommandSourceStack source, ResourceLocation id) {
        Block block = ForgeRegistries.BLOCKS.getValue(id);
        ResourceLocation key = block == null ? null : ForgeRegistries.BLOCKS.getKey(block);
        if (block == null || key == null || !key.equals(id)) {
            source.sendFailure(Component.literal("Unknown block: " + id));
            return Optional.empty();
        }
        return Optional.of(block);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> satelliteCommand(String name) {
        return Commands.literal(name)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("orbit")
                        .executes(context -> orbitHeldSatellite(context.getSource()))
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(LegacySatelliteType.names(), builder))
                                .then(Commands.argument("frequency", IntegerArgumentType.integer())
                                        .executes(context -> orbitSatellite(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "type"),
                                                IntegerArgumentType.getInteger(context, "frequency"))))))
                .then(Commands.literal("descend")
                        .then(Commands.argument("frequency", IntegerArgumentType.integer())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        SatelliteSavedData.get(context.getSource().getLevel()).frequenciesSnapshot().stream()
                                                .map(String::valueOf),
                                        builder))
                                .executes(context -> descendSatellite(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "frequency")))))
                .then(Commands.literal("list")
                        .executes(context -> listSatellites(context.getSource())))
                .then(Commands.literal("stats")
                        .executes(context -> satelliteStats(context.getSource())))
                .then(Commands.literal("chip")
                        .then(Commands.literal("get")
                                .executes(context -> getHeldSatelliteChipFrequency(context.getSource())))
                        .then(Commands.literal("set")
                                .then(Commands.argument("frequency", IntegerArgumentType.integer())
                                        .executes(context -> setHeldSatelliteChipFrequency(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "frequency"))))));
    }

    private static int orbitHeldSatellite(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        LegacySatelliteType type = Satellite.getTypeFromStack(stack).orElse(null);
        if (type == null) {
            source.sendFailure(Component.literal("Hold a launchable satellite item to orbit it."));
            return 0;
        }
        int frequency = ISatelliteChip.getFrequencyFromStack(stack);
        boolean orbited = Satellite.orbit(source.getLevel(), type, frequency,
                player.getX(), player.getY(), player.getZ());
        if (!orbited) {
            source.sendFailure(Component.literal("Could not create satellite from held item."));
            return 0;
        }
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        source.sendSuccess(() -> Component.literal("Orbited held satellite " + type.legacyName()
                + " on frequency " + frequency + "."), true);
        return frequency;
    }

    private static int orbitSatellite(CommandSourceStack source, String typeName, int frequency) {
        LegacySatelliteType type = LegacySatelliteType.byName(typeName);
        if (type == null) {
            source.sendFailure(Component.literal("Unknown satellite type '" + typeName + "'. Valid: "
                    + String.join(", ", LegacySatelliteType.names())));
            return 0;
        }

        boolean orbited = Satellite.orbit(source.getLevel(), type, frequency,
                source.getPosition().x(), source.getPosition().y(), source.getPosition().z());
        if (!orbited) {
            source.sendFailure(Component.literal("Could not create satellite type '" + type.legacyName() + "'."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Orbited satellite " + type.legacyName()
                + " on frequency " + frequency + "."), true);
        return frequency;
    }

    private static int descendSatellite(CommandSourceStack source, int frequency) {
        SatelliteSavedData data = SatelliteSavedData.get(source.getLevel());
        if (!data.removeSatellite(frequency)) {
            source.sendFailure(Component.literal("No satellite is using frequency " + frequency + "."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Descended satellite on frequency " + frequency + "."), true);
        return frequency;
    }

    private static int listSatellites(CommandSourceStack source) {
        SatelliteSavedData data = SatelliteSavedData.get(source.getLevel());
        if (data.isEmpty()) {
            source.sendFailure(Component.literal("No active satellites."));
            return 0;
        }

        sendSatelliteSummaries(source, data.satelliteSummariesSnapshot(data.size()));
        return data.size();
    }

    private static int satelliteStats(CommandSourceStack source) {
        SatelliteSavedData data = SatelliteSavedData.get(source.getLevel());
        source.sendSuccess(() -> Component.literal("Satellite data: " + data.statsSnapshot(16, 8).summary()
                + " savedData=" + SatelliteSavedData.DATA_NAME), false);
        return data.size();
    }

    private static int getHeldSatelliteChipFrequency(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ItemStack stack = source.getPlayerOrException().getMainHandItem();
        if (!(stack.getItem() instanceof ISatelliteChip)) {
            source.sendFailure(Component.literal("Hold a satellite chip item."));
            return 0;
        }
        int frequency = ISatelliteChip.getFrequencyFromStack(stack);
        source.sendSuccess(() -> Component.literal("Held satellite frequency: " + frequency), false);
        return frequency;
    }

    private static int setHeldSatelliteChipFrequency(CommandSourceStack source, int frequency) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ISatelliteChip)) {
            source.sendFailure(Component.literal("Hold a satellite chip item."));
            return 0;
        }
        ISatelliteChip.setFrequencyOnStack(stack, frequency);
        player.getInventory().setChanged();
        source.sendSuccess(() -> Component.literal("Set held satellite frequency to " + frequency + "."), true);
        return frequency;
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
                .then(Commands.literal("prepare")
                        .executes(context -> getPacketThreadingPrepareStats(context.getSource())))
                .then(Commands.literal("precompile")
                        .executes(context -> getPacketThreadingPrepareStats(context.getSource())))
                .then(Commands.literal("clear")
                        .executes(context -> clearPacketThreading(context.getSource())))
                .then(Commands.literal("forceLock")
                        .executes(context -> rejectLegacyPacketThreadingLock(context.getSource(), "forceLock")))
                .then(Commands.literal("forceUnlock")
                        .executes(context -> rejectLegacyPacketThreadingLock(context.getSource(), "forceUnlock")));
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
                .then(Commands.literal("chunk")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> getEnergyChunk(
                                        context.getSource(),
                                        BlockPosArgument.getBlockPos(context, "pos")))))
                .then(Commands.literal("info")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> getEnergyInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                .then(Commands.literal("port")
                        .then(Commands.argument("machine", BlockPosArgument.blockPos())
                                .then(Commands.argument("offset", BlockPosArgument.blockPos())
                                        .then(Commands.argument("side", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(directionNames(), builder))
                                                .executes(context -> getEnergyPort(
                                                        context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "machine"),
                                                        BlockPosArgument.getBlockPos(context, "offset"),
                                                        parseDirection(StringArgumentType.getString(context, "side"))))))))
                .then(Commands.literal("ports")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> getEnergyPorts(
                                        context.getSource(),
                                        BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                .then(Commands.literal("ror")
                        .then(Commands.literal("functions")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> getRorFunctions(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                        .then(Commands.literal("value")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                                .suggests((context, builder) -> suggestRorValues(context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"), builder))
                                                .executes(context -> getRorValue(
                                                        context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                        StringArgumentType.getString(context, "name"))))))
                        .then(Commands.literal("run")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("command", StringArgumentType.greedyString())
                                                .suggests((context, builder) -> suggestRorFunctionExamples(context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"), builder))
                                                .executes(context -> runRorFunction(
                                                        context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                        StringArgumentType.getString(context, "command"))))))
                        .then(Commands.literal("signal")
                                .then(Commands.literal("broadcast")
                                        .then(Commands.argument("channel", StringArgumentType.string())
                                                .then(Commands.argument("signal", StringArgumentType.greedyString())
                                                        .executes(context -> broadcastRttySignal(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "channel"),
                                                                StringArgumentType.getString(context, "signal"))))))
                                .then(Commands.literal("listen")
                                        .then(Commands.argument("channel", StringArgumentType.string())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        RTTYSystem.channels(context.getSource().getLevel()), builder))
                                                .executes(context -> listenRttySignal(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "channel")))))
                                .then(Commands.literal("read")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .then(Commands.argument("channel", StringArgumentType.string())
                                                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                                                .suggests((context, builder) -> suggestRorValues(context.getSource(),
                                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"), builder))
                                                                .executes(context -> readAndBroadcastRorValue(
                                                                        context.getSource(),
                                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                                        StringArgumentType.getString(context, "channel"),
                                                                        StringArgumentType.getString(context, "name")))))))
                                .then(Commands.literal("run")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .then(Commands.argument("channel", StringArgumentType.string())
                                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                                RTTYSystem.channels(context.getSource().getLevel()), builder))
                                                        .executes(context -> runRorCommandFromSignal(
                                                                context.getSource(),
                                                                BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                                StringArgumentType.getString(context, "channel"))))))
                                .then(rttyTorchCommand())
                                .then(Commands.literal("map")
                                        .then(Commands.literal("send")
                                                .then(Commands.argument("input", IntegerArgumentType.integer(0, 15))
                                                        .then(Commands.argument("custom", StringArgumentType.word())
                                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"false", "true"}, builder))
                                                                .then(Commands.argument("mapped", StringArgumentType.greedyString())
                                                                        .executes(context -> mapRttyRedstoneSignal(
                                                                                context.getSource(),
                                                                                IntegerArgumentType.getInteger(context, "input"),
                                                                                parseBoolean(StringArgumentType.getString(context, "custom")),
                                                                                StringArgumentType.getString(context, "mapped")))))))
                                        .then(Commands.literal("receive")
                                                .then(Commands.argument("signal", StringArgumentType.word())
                                                        .then(Commands.argument("custom", StringArgumentType.word())
                                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"false", "true"}, builder))
                                                                .then(Commands.argument("index", IntegerArgumentType.integer(0, 15))
                                                                        .then(Commands.argument("mapped", StringArgumentType.greedyString())
                                                                                .executes(context -> receiveRttyMappedSignal(
                                                                                        context.getSource(),
                                                                                        StringArgumentType.getString(context, "signal"),
                                                                                        parseBoolean(StringArgumentType.getString(context, "custom")),
                                                                                        IntegerArgumentType.getInteger(context, "index"),
                                                                                        StringArgumentType.getString(context, "mapped")))))))))
                                .then(Commands.literal("counter")
                                        .then(Commands.argument("channel", StringArgumentType.string())
                                                .then(Commands.argument("last", IntegerArgumentType.integer(0))
                                                        .then(Commands.argument("count", IntegerArgumentType.integer(0))
                                                                .then(Commands.argument("polling", StringArgumentType.word())
                                                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"false", "true"}, builder))
                                                                        .executes(context -> evalRttyCounterSignal(
                                                                                context.getSource(),
                                                                                StringArgumentType.getString(context, "channel"),
                                                                                IntegerArgumentType.getInteger(context, "last"),
                                                                                IntegerArgumentType.getInteger(context, "count"),
                                                                                parseBoolean(StringArgumentType.getString(context, "polling")))))))))
                                .then(Commands.literal("logic")
                                        .then(Commands.literal("parse")
                                                .then(Commands.argument("signal", StringArgumentType.word())
                                                        .then(Commands.argument("condition", IntegerArgumentType.integer(0, 9))
                                                                .then(Commands.argument("mapped", StringArgumentType.word())
                                                                        .executes(context -> parseRttyLogicSignal(
                                                                                context.getSource(),
                                                                                StringArgumentType.getString(context, "signal"),
                                                                                IntegerArgumentType.getInteger(context, "condition"),
                                                                                StringArgumentType.getString(context, "mapped")))))))
                                        .then(Commands.literal("eval")
                                                .then(Commands.argument("signal", StringArgumentType.word())
                                                        .then(Commands.argument("descending", StringArgumentType.word())
                                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"false", "true"}, builder))
                                                                .then(Commands.argument("index", IntegerArgumentType.integer(0, 15))
                                                                        .then(Commands.argument("condition", IntegerArgumentType.integer(0, 9))
                                                                                .then(Commands.argument("mapped", StringArgumentType.word())
                                                                                        .executes(context -> evalRttyLogicSignal(
                                                                                                context.getSource(),
                                                                                                StringArgumentType.getString(context, "signal"),
                                                                                                parseBoolean(StringArgumentType.getString(context, "descending")),
                                                                                                IntegerArgumentType.getInteger(context, "index"),
                                                                                                IntegerArgumentType.getInteger(context, "condition"),
                                                                                                StringArgumentType.getString(context, "mapped"))))))))))
                                .then(Commands.literal("stats")
                                        .executes(context -> getRttyStats(context.getSource())))
                                .then(Commands.literal("clear")
                                        .executes(context -> clearRttySignals(context.getSource())))))
                .then(Commands.literal("debug")
                        .then(Commands.literal("particles")
                                .executes(context -> toggleEnergyDebugParticles(context.getSource()))
                        .then(Commands.argument("enabled", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"false", "true"}, builder))
                                .executes(context -> setEnergyDebugParticles(context.getSource(), parseBoolean(StringArgumentType.getString(context, "enabled")))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> rttyTorchCommand() {
        return Commands.literal("torch")
                .then(Commands.literal("inspect")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> inspectRttyTorch(
                                        context.getSource(),
                                        BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                .then(Commands.literal("set")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestRttyTorchKeys(
                                                context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                builder))
                                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                                .executes(context -> setRttyTorchKey(
                                                        context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                        StringArgumentType.getString(context, "key"),
                                                        StringArgumentType.getString(context, "value")))))))
                .then(Commands.literal("channel")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("channel", StringArgumentType.string())
                                        .executes(context -> setRttyTorchChannel(
                                                context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                StringArgumentType.getString(context, "channel"))))))
                .then(Commands.literal("polling")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("polling", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"false", "true"}, builder))
                                        .executes(context -> setRttyTorchPolling(
                                                context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                parseBoolean(StringArgumentType.getString(context, "polling")))))))
                .then(Commands.literal("custom")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("custom", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"false", "true"}, builder))
                                        .executes(context -> setRttyTorchCustomMap(
                                                context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                parseBoolean(StringArgumentType.getString(context, "custom")))))))
                .then(Commands.literal("map")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("index", IntegerArgumentType.integer(0, 15))
                                        .then(Commands.argument("mapped", StringArgumentType.greedyString())
                                                .executes(context -> setRttyTorchMapping(
                                                        context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                        IntegerArgumentType.getInteger(context, "index"),
                                                        StringArgumentType.getString(context, "mapped")))))))
                .then(Commands.literal("logic")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("index", IntegerArgumentType.integer(0, 15))
                                        .then(Commands.argument("condition", IntegerArgumentType.integer(0, 9))
                                                .then(Commands.argument("mapped", StringArgumentType.greedyString())
                                                        .executes(context -> setRttyTorchLogicRule(
                                                                context.getSource(),
                                                                BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                                IntegerArgumentType.getInteger(context, "index"),
                                                                IntegerArgumentType.getInteger(context, "condition"),
                                                                StringArgumentType.getString(context, "mapped"))))))))
                .then(Commands.literal("descending")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("descending", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"false", "true"}, builder))
                                        .executes(context -> setRttyTorchDescending(
                                                context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                parseBoolean(StringArgumentType.getString(context, "descending")))))))
                .then(Commands.literal("reader")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("slot", IntegerArgumentType.integer(0, RTTYReaderState.SLOT_COUNT - 1))
                                        .then(Commands.argument("channel", StringArgumentType.string())
                                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                                        .executes(context -> setRttyTorchReaderSlot(
                                                                context.getSource(),
                                                                BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                                IntegerArgumentType.getInteger(context, "slot"),
                                                                StringArgumentType.getString(context, "channel"),
                                                                StringArgumentType.getString(context, "name"))))))))
                .then(Commands.literal("counter")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("slot", IntegerArgumentType.integer(0, RTTYCounterState.SLOT_COUNT - 1))
                                        .then(Commands.argument("channel", StringArgumentType.string())
                                                .executes(context -> setRttyTorchCounterSlot(
                                                        context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                        IntegerArgumentType.getInteger(context, "slot"),
                                                        StringArgumentType.getString(context, "channel")))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> pollutionCommand() {
        LiteralArgumentBuilder<CommandSourceStack> clearPollutionGrid = Commands.literal("grid")
                .then(Commands.argument("gridX", IntegerArgumentType.integer())
                        .then(Commands.argument("gridZ", IntegerArgumentType.integer())
                                .executes(context -> clearPollution(
                                        context.getSource(),
                                        new PollutionGridPos(
                                                IntegerArgumentType.getInteger(context, "gridX"),
                                                IntegerArgumentType.getInteger(context, "gridZ"))))));
        return Commands.literal("pollution")
                .then(Commands.literal("get")
                        .executes(context -> getPollution(context.getSource()))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> getPollution(
                                        context.getSource(),
                                        BlockPosArgument.getBlockPos(context, "pos"))))
                        .then(Commands.literal("grid")
                                .then(Commands.argument("gridX", IntegerArgumentType.integer())
                                        .then(Commands.argument("gridZ", IntegerArgumentType.integer())
                                                .executes(context -> getPollution(
                                                        context.getSource(),
                                                        new PollutionGridPos(
                                                                IntegerArgumentType.getInteger(context, "gridX"),
                                                                        IntegerArgumentType.getInteger(context, "gridZ")))))))
                .then(Commands.literal("set")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(pollutionTypeNames(), builder))
                                .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                        .executes(context -> setPollution(context.getSource(),
                                                StringArgumentType.getString(context, "type"),
                                                FloatArgumentType.getFloat(context, "amount")))
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(context -> setPollution(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "type"),
                                                        FloatArgumentType.getFloat(context, "amount"),
                                                        BlockPosArgument.getBlockPos(context, "pos"))))
                                        .then(Commands.literal("grid")
                                                .then(Commands.argument("gridX", IntegerArgumentType.integer())
                                                        .then(Commands.argument("gridZ", IntegerArgumentType.integer())
                                                                .executes(context -> setPollution(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(context, "type"),
                                                                        FloatArgumentType.getFloat(context, "amount"),
                                                                        new PollutionGridPos(
                                                                                IntegerArgumentType.getInteger(context, "gridX"),
                                                                                IntegerArgumentType.getInteger(context, "gridZ"))))))))))
                .then(Commands.literal("add")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(pollutionTypeNames(), builder))
                                .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                        .executes(context -> addPollution(context.getSource(),
                                                StringArgumentType.getString(context, "type"),
                                                FloatArgumentType.getFloat(context, "amount")))
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(context -> addPollution(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "type"),
                                                        FloatArgumentType.getFloat(context, "amount"),
                                                        BlockPosArgument.getBlockPos(context, "pos"))))
                                        .then(Commands.literal("grid")
                                                .then(Commands.argument("gridX", IntegerArgumentType.integer())
                                                        .then(Commands.argument("gridZ", IntegerArgumentType.integer())
                                                                .executes(context -> addPollution(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(context, "type"),
                                                                        FloatArgumentType.getFloat(context, "amount"),
                                                                        new PollutionGridPos(
                                                                                IntegerArgumentType.getInteger(context, "gridX"),
                                                                                IntegerArgumentType.getInteger(context, "gridZ"))))))))))
                .then(Commands.literal("stats")
                        .executes(context -> getPollutionStats(context.getSource())))
                .then(Commands.literal("list")
                        .executes(context -> listPollutionEntries(context.getSource(), 10))
                        .then(Commands.argument("limit", IntegerArgumentType.integer(1, 50))
                                .executes(context -> listPollutionEntries(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "limit"))))
                        .then(Commands.literal("type")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                pollutionTypeNames(), builder))
                                        .executes(context -> listPollutionEntriesByType(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "type"),
                                                10))
                                        .then(Commands.argument("limit", IntegerArgumentType.integer(1, 50))
                                                .executes(context -> listPollutionEntriesByType(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "type"),
                                                        IntegerArgumentType.getInteger(context, "limit"))))))
                .then(Commands.literal("previewDiffusion")
                        .executes(context -> previewPollutionDiffusion(context.getSource(), 10))
                        .then(Commands.argument("limit", IntegerArgumentType.integer(1, 50))
                                .executes(context -> previewPollutionDiffusion(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "limit"))))
                        .then(Commands.literal("type")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                pollutionTypeNames(), builder))
                                        .executes(context -> previewPollutionDiffusionByType(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "type"),
                                                10))
                                        .then(Commands.argument("limit", IntegerArgumentType.integer(1, 50))
                                                .executes(context -> previewPollutionDiffusionByType(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "type"),
                                                        IntegerArgumentType.getInteger(context, "limit"))))))
                        .then(Commands.literal("pos")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> previewPollutionDiffusionAt(
                                                context.getSource(),
                                                PollutionGridPos.ofBlock(
                                                        BlockPosArgument.getBlockPos(context, "pos"))))))
                        .then(Commands.literal("grid")
                                .then(Commands.argument("gridX", IntegerArgumentType.integer())
                                        .then(Commands.argument("gridZ", IntegerArgumentType.integer())
                                                .executes(context -> previewPollutionDiffusionAt(
                                                        context.getSource(),
                                                        new PollutionGridPos(
                                                                IntegerArgumentType.getInteger(context, "gridX"),
                                                                IntegerArgumentType.getInteger(context, "gridZ")))))))
                        .then(Commands.literal("neighbors")
                                .executes(context -> previewPollutionDiffusionNeighbors(
                                        context.getSource(),
                                        PollutionGridPos.ofBlock(BlockPos.containing(context.getSource().getPosition()))))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> previewPollutionDiffusionNeighbors(
                                                context.getSource(),
                                                PollutionGridPos.ofBlock(
                                                        BlockPosArgument.getBlockPos(context, "pos")))))
                                .then(Commands.literal("grid")
                                        .then(Commands.argument("gridX", IntegerArgumentType.integer())
                                                .then(Commands.argument("gridZ", IntegerArgumentType.integer())
                                                        .executes(context -> previewPollutionDiffusionNeighbors(
                                                                context.getSource(),
                                                                new PollutionGridPos(
                                                                        IntegerArgumentType.getInteger(context, "gridX"),
                                                                        IntegerArgumentType.getInteger(context, "gridZ")))))))))
                .then(Commands.literal("gridOf")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> getPollutionGridOf(
                                        context.getSource(),
                                        BlockPosArgument.getBlockPos(context, "pos")))))
                .then(Commands.literal("neighbors")
                        .executes(context -> getPollutionNeighbors(
                                context.getSource(),
                                PollutionGridPos.ofBlock(BlockPos.containing(context.getSource().getPosition()))))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> getPollutionNeighbors(
                                        context.getSource(),
                                        PollutionGridPos.ofBlock(BlockPosArgument.getBlockPos(context, "pos")))))
                        .then(Commands.literal("grid")
                                .then(Commands.argument("gridX", IntegerArgumentType.integer())
                                        .then(Commands.argument("gridZ", IntegerArgumentType.integer())
                                                .executes(context -> getPollutionNeighbors(
                                                        context.getSource(),
                                                        new PollutionGridPos(
                                                                        IntegerArgumentType.getInteger(context, "gridX"),
                                                                        IntegerArgumentType.getInteger(context, "gridZ")))))))))
                .then(Commands.literal("prune")
                        .executes(context -> prunePollution(context.getSource())))
                .then(Commands.literal("clear")
                        .executes(context -> clearPollution(context.getSource()))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> clearPollution(
                                        context.getSource(),
                                        BlockPosArgument.getBlockPos(context, "pos"))))
                        .then(clearPollutionGrid)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> damageCommand() {
        return Commands.literal("damage")
                .then(Commands.literal("resistance")
                        .then(Commands.literal("status")
                                .executes(context -> getDamageResistanceStatus(context.getSource())))
                        .then(Commands.literal("selfTest")
                                .executes(context -> selfTestDamageResistance(context.getSource())))
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
                        .executes(context -> getFluidNodespace(context.getSource()))
                        .then(Commands.literal("rebuild")
                                .executes(context -> rebuildFluidNodespace(context.getSource()))))
                .then(Commands.literal("info")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> getFluidInfo(
                                        context.getSource(),
                                        BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                .then(Commands.literal("node")
                        .then(fluidNetworkArgument()))
                .then(Commands.literal("network")
                        .then(fluidNetworkArgument()))
                .then(Commands.literal("overpressure")
                        .then(Commands.literal("all")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> damageAllFluidNetworksFromOverpressure(
                                                context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("fluid", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                HbmFluids.all().stream().map(FluidType::toPath), builder))
                                        .executes(context -> damageFluidNetworkFromOverpressure(
                                                context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                parseFluid(StringArgumentType.getString(context, "fluid")))))))
                .then(Commands.literal("traits")
                        .then(Commands.literal("status")
                                .executes(context -> getFluidTraitConfigStatus(context.getSource())))
                        .then(Commands.literal("reload")
                                .executes(context -> reloadFluidTraitConfig(context.getSource())))
                        .then(Commands.argument("fluid", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        HbmFluids.all().stream().map(FluidType::toPath), builder))
                                .executes(context -> getFluidTraits(
                                        context.getSource(),
                                        parseFluid(StringArgumentType.getString(context, "fluid"))))))
                .then(Commands.literal("types")
                        .then(Commands.literal("status")
                                .executes(context -> getFluidTypeConfigStatus(context.getSource())))
                        .then(Commands.literal("reload")
                                .executes(context -> reloadFluidTypes(context.getSource())))
                        .then(Commands.argument("fluid", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        HbmFluids.all().stream().map(FluidType::toPath), builder))
                                .executes(context -> getFluidDefinition(
                                        context.getSource(),
                                        parseFluid(StringArgumentType.getString(context, "fluid"))))))
                .then(Commands.literal("compat")
                        .then(Commands.literal("status")
                                .executes(context -> getFluidCompatStatus(context.getSource()))))
                .then(Commands.literal("forge")
                        .then(Commands.literal("status")
                                .executes(context -> getFluidForgeMappingStatus(context.getSource())))
                        .then(Commands.literal("resolve")
                                .then(Commands.argument("forge_fluid", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                ForgeRegistries.FLUIDS.getKeys().stream().map(ResourceLocation::toString), builder))
                                        .executes(context -> resolveForgeFluidMapping(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "forge_fluid"))))))
                .then(Commands.literal("containers")
                        .then(Commands.literal("status")
                                .executes(context -> getFluidContainerRegistryStatus(context.getSource())))
                        .then(Commands.literal("summary")
                                .executes(context -> summarizeFluidContainers(context.getSource())))
                        .then(Commands.literal("reload")
                                .executes(context -> reloadFluidContainers(context.getSource())))
                        .then(Commands.literal("list")
                                .executes(context -> listFluidContainers(context.getSource(), null))
                                .then(Commands.argument("fluid", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                HbmFluids.all().stream().map(FluidType::toPath), builder))
                                        .executes(context -> listFluidContainers(
                                                context.getSource(),
                                                parseFluid(StringArgumentType.getString(context, "fluid"))))))
                        .then(Commands.literal("held")
                                .executes(context -> resolveHeldFluidContainer(context.getSource()))))
                .then(Commands.literal("port")
                        .then(Commands.argument("machine", BlockPosArgument.blockPos())
                                .then(Commands.argument("offset", BlockPosArgument.blockPos())
                                        .then(Commands.argument("side", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(directionNames(), builder))
                                                .then(Commands.argument("fluid", StringArgumentType.word())
                                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                                HbmFluids.all().stream().map(FluidType::toPath), builder))
                                                        .executes(context -> getFluidPort(
                                                                context.getSource(),
                                                                BlockPosArgument.getLoadedBlockPos(context, "machine"),
                                                                BlockPosArgument.getBlockPos(context, "offset"),
                                                                parseDirection(StringArgumentType.getString(context, "side")),
                                                                parseFluid(StringArgumentType.getString(context, "fluid")))))))))
                .then(Commands.literal("pipe")
                        .then(Commands.literal("set")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("fluid", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        HbmFluids.all().stream().map(FluidType::toPath), builder))
                                                .executes(context -> setFluidPipeType(
                                                        context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                        parseFluid(StringArgumentType.getString(context, "fluid")))))))
                        .then(Commands.literal("anchor")
                                .then(Commands.literal("link")
                                        .then(Commands.argument("first", BlockPosArgument.blockPos())
                                                .then(Commands.argument("second", BlockPosArgument.blockPos())
                                                        .executes(context -> linkFluidPipeAnchors(
                                                                context.getSource(),
                                                                BlockPosArgument.getLoadedBlockPos(context, "first"),
                                                                BlockPosArgument.getLoadedBlockPos(context, "second"))))))
                                .then(Commands.literal("unlink")
                                        .then(Commands.argument("first", BlockPosArgument.blockPos())
                                                .then(Commands.argument("second", BlockPosArgument.blockPos())
                                                        .executes(context -> unlinkFluidPipeAnchors(
                                                                context.getSource(),
                                                                BlockPosArgument.getLoadedBlockPos(context, "first"),
                                                                BlockPosArgument.getLoadedBlockPos(context, "second"))))))
                                .then(Commands.literal("clear")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(context -> clearFluidPipeAnchor(
                                                        context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                                .then(Commands.literal("info")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(context -> getFluidPipeAnchorInfo(
                                                        context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos")))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> compatCommand() {
        return Commands.literal("compat")
                .executes(context -> getCompatStatus(context.getSource()))
                .then(Commands.literal("status")
                        .executes(context -> getCompatStatus(context.getSource())))
                .then(Commands.literal("mods")
                        .executes(context -> getCompatModsStatus(context.getSource())))
                .then(Commands.literal("recipes")
                        .executes(context -> getCompatRecipeStatus(context.getSource())))
                .then(Commands.literal("fluids")
                        .executes(context -> getCompatFluidStatus(context.getSource())))
                .then(Commands.literal("externals")
                        .executes(context -> getCompatExternalHookStatus(context.getSource())))
                .then(Commands.literal("steam")
                        .then(Commands.literal("level")
                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 3))
                                        .executes(context -> getCompatSteamLevel(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "level")))))
                        .then(Commands.literal("fluid")
                                .then(Commands.argument("fluid", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                HbmFluids.all().stream().map(FluidType::getName),
                                                builder))
                                        .executes(context -> getCompatSteamFluid(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "fluid"))))));
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
                        .then(Commands.literal("channel")
                                .executes(context -> getNetworkProtocolChannel(context.getSource())))
                        .then(Commands.literal("fingerprint")
                                .executes(context -> getNetworkProtocolFingerprint(context.getSource())))
                        .then(Commands.literal("manifest")
                                .executes(context -> getNetworkProtocolManifest(context.getSource())))
                        .then(Commands.literal("contract")
                                .executes(context -> getNetworkProtocolContract(context.getSource())))
                        .then(Commands.literal("codec")
                                .executes(context -> getNetworkProtocolCodec(context.getSource())))
                        .then(Commands.literal("handlers")
                                .executes(context -> getNetworkProtocolHandlers(context.getSource())))
                        .then(Commands.literal("progress")
                                .executes(context -> getNetworkProtocolProgress(context.getSource())))
                        .then(Commands.literal("diagnostics")
                                .executes(context -> getNetworkProtocolDiagnostics(context.getSource())))
                        .then(Commands.literal("resetRuntime")
                                .executes(context -> resetNetworkProtocolRuntime(context.getSource())))
                        .then(Commands.literal("packets")
                                .executes(context -> listNetworkProtocolPackets(context.getSource())))
                        .then(Commands.literal("audit")
                                .executes(context -> auditNetworkProtocol(context.getSource())))
                        .then(Commands.literal("safety")
                                .executes(context -> getNetworkSendSafety(context.getSource())))
                        .then(Commands.literal("resetSafety")
                                .executes(context -> resetNetworkSendSafety(context.getSource())))
                        .then(Commands.literal("rawBuffer")
                                .executes(context -> getNetworkRawBufferStatus(context.getSource())))
                        .then(Commands.literal("dimensionId")
                                .executes(context -> getNetworkDimensionIdStatus(context.getSource())))
                        .then(Commands.literal("targetPoint")
                                .executes(context -> getNetworkTargetPointStatus(context.getSource())))
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
                        .then(Commands.literal("legacyId")
                                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                        .executes(context -> queryLegacyNetworkPacketById(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "id")))))
                        .then(Commands.literal("modern")
                                .then(Commands.argument("packet", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                ModMessages.packetRegistrations().stream()
                                                        .map(ModMessages.PacketRegistration::typeName),
                                                builder))
                                        .executes(context -> queryModernNetworkPacket(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "packet")))))
                        .then(Commands.literal("modernId")
                                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                        .executes(context -> queryModernNetworkPacketById(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "id")))))
                        .then(Commands.literal("wrapper")
                                .executes(context -> getNetworkLegacyWrapper(context.getSource()))))
                .then(Commands.literal("packetthreading")
                        .then(Commands.literal("stats")
                                .executes(context -> getPacketThreadingStats(context.getSource())))
                        .then(Commands.literal("info")
                                .executes(context -> getPacketThreadingStats(context.getSource())))
                        .then(Commands.literal("config")
                                .executes(context -> getPacketThreadingConfig(context.getSource())))
                        .then(Commands.literal("prepare")
                                .executes(context -> getPacketThreadingPrepareStats(context.getSource())))
                        .then(Commands.literal("precompile")
                                .executes(context -> getPacketThreadingPrepareStats(context.getSource())))
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
                                        StringArgumentType.getString(context, "oreName")))))
                .then(Commands.literal("legacyHandlers")
                        .executes(context -> listLegacyGenericRecipeHandlers(context.getSource())))
                .then(Commands.literal("legacySerializableHandlers")
                        .executes(context -> listLegacySerializableRecipeHandlers(context.getSource())));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> itemPoolCommand() {
        return Commands.literal("itempool")
                .executes(context -> getItemPoolSummary(context.getSource()))
                .then(Commands.literal("summary")
                        .executes(context -> getItemPoolSummary(context.getSource())))
                .then(Commands.literal("list")
                        .executes(context -> listItemPoolTables(context.getSource())))
                .then(Commands.literal("missing")
                        .executes(context -> listMissingItemPoolTables(context.getSource())))
                .then(Commands.literal("lootnames")
                        .executes(context -> listLegacyLootNames(context.getSource())))
                .then(Commands.literal("lootstatus")
                        .then(Commands.argument("lootName", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        HbmLegacyLootUtil.LOOT_NAMES.stream(),
                                        builder))
                                .executes(context -> queryLegacyLootName(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "lootName")))))
                .then(Commands.literal("lootroll")
                        .then(Commands.argument("lootName", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        HbmLegacyLootUtil.LOOT_NAMES.stream(),
                                        builder))
                                .executes(context -> rollLegacyLootName(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "lootName")))))
                .then(Commands.literal("status")
                        .then(Commands.argument("legacyPoolId", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        HbmItemPoolRegistry.knownPoolIds().stream().sorted(),
                                        builder))
                                .executes(context -> queryItemPoolTable(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "legacyPoolId")))))
                .then(Commands.literal("table")
                        .then(Commands.argument("legacyPoolId", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        HbmItemPoolRegistry.knownPoolIds().stream().sorted(),
                                        builder))
                                .executes(context -> queryItemPoolTable(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "legacyPoolId")))))
                .then(Commands.literal("roll")
                        .then(Commands.argument("legacyPoolId", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        HbmItemPoolRegistry.knownPoolIds().stream().sorted(),
                                        builder))
                                .executes(context -> rollItemPool(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "legacyPoolId")))));
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

    private static int getPollution(CommandSourceStack source) {
        BlockPos pos = BlockPos.containing(source.getPosition());
        return getPollution(source, pos);
    }

    private static int getPollution(CommandSourceStack source, BlockPos pos) {
        PollutionSavedData.PollutionSample sample = PollutionManager.getPollutionData(source.getLevel(), pos);
        source.sendSuccess(() -> pollutionReadout("Pollution at " + pos.toShortString()
                + " grid " + PollutionGridPos.ofBlock(pos).formatLabel(), sample), false);
        return Math.round(sample.sum());
    }

    private static int getPollution(CommandSourceStack source, PollutionGridPos pos) {
        PollutionSavedData.PollutionSample sample = PollutionManager.getPollutionData(source.getLevel(), pos);
        source.sendSuccess(() -> pollutionReadout("Pollution grid " + pos.formatLabel(), sample), false);
        return Math.round(sample.sum());
    }

    private static int setPollution(CommandSourceStack source, String typeName, float amount) {
        return setPollution(source, typeName, amount, BlockPos.containing(source.getPosition()));
    }

    private static int setPollution(CommandSourceStack source, String typeName, float amount, BlockPos pos) {
        PollutionType type = parsePollutionType(source, typeName);
        if (type == null) {
            return 0;
        }
        PollutionManager.setPollution(source.getLevel(), pos, type, amount);
        source.sendSuccess(() -> Component.literal("Set " + type.id() + " pollution at " + pos.toShortString()
                + " to " + round(amount) + "."), true);
        return Math.round(amount);
    }

    private static int setPollution(CommandSourceStack source, String typeName, float amount, PollutionGridPos pos) {
        PollutionType type = parsePollutionType(source, typeName);
        if (type == null) {
            return 0;
        }
        PollutionManager.setPollution(source.getLevel(), pos, type, amount);
        source.sendSuccess(() -> Component.literal("Set " + type.id() + " pollution at grid "
                + pos.formatLabel() + " to " + round(amount) + "."), true);
        return Math.round(amount);
    }

    private static int addPollution(CommandSourceStack source, String typeName, float amount) {
        return addPollution(source, typeName, amount, BlockPos.containing(source.getPosition()));
    }

    private static int addPollution(CommandSourceStack source, String typeName, float amount, BlockPos pos) {
        PollutionType type = parsePollutionType(source, typeName);
        if (type == null) {
            return 0;
        }
        PollutionManager.incrementPollution(source.getLevel(), pos, type, amount);
        float current = PollutionManager.getPollution(source.getLevel(), pos, type);
        source.sendSuccess(() -> Component.literal(type.id() + " pollution at " + pos.toShortString()
                + ": " + round(current)), true);
        return Math.round(current);
    }

    private static int addPollution(CommandSourceStack source, String typeName, float amount, PollutionGridPos pos) {
        PollutionType type = parsePollutionType(source, typeName);
        if (type == null) {
            return 0;
        }
        PollutionManager.incrementPollution(source.getLevel(), pos, type, amount);
        float current = PollutionManager.getPollution(source.getLevel(), pos, type);
        source.sendSuccess(() -> Component.literal(type.id() + " pollution at grid "
                + pos.formatLabel() + ": " + round(current)), true);
        return Math.round(current);
    }

    private static int clearPollution(CommandSourceStack source) {
        PollutionManager.clear(source.getLevel());
        source.sendSuccess(() -> Component.literal("Cleared pollution data."), true);
        return 1;
    }

    private static int clearPollution(CommandSourceStack source, BlockPos pos) {
        PollutionManager.setPollutionData(source.getLevel(), pos, new PollutionSavedData.PollutionSample());
        source.sendSuccess(() -> Component.literal("Cleared pollution at " + pos.toShortString() + "."), true);
        return 1;
    }

    private static int clearPollution(CommandSourceStack source, PollutionGridPos pos) {
        PollutionManager.setPollutionData(source.getLevel(), pos, new PollutionSavedData.PollutionSample());
        source.sendSuccess(() -> Component.literal("Cleared pollution grid " + pos.formatLabel() + "."), true);
        return 1;
    }

    private static Component pollutionReadout(String label, PollutionSavedData.PollutionSample sample) {
        PollutionSavedData.PollutionSample data = sample == null ? new PollutionSavedData.PollutionSample() : sample;
        return Component.literal(label + ": " + data.formatValues());
    }

    private static int getPollutionStats(CommandSourceStack source) {
        PollutionSavedData.Stats stats = PollutionManager.getStats(source.getLevel());
        source.sendSuccess(() -> Component.literal("Pollution stats: entries=" + stats.totalEntries()
                + " loaded=" + stats.loadedEntries()
                + " positive=" + stats.positiveEntries()
                + " loadedPositive=" + stats.loadedPositiveEntries()
                + " stored=" + stats.storedEntries()
                + " loadedStored=" + stats.loadedStoredEntries()
                + " total=" + round(stats.totalPollution())
                + " loadedTotal=" + round(stats.loadedPollution())
                + " max=" + round(stats.maxPollution())
                + " loadedMax=" + round(stats.loadedMaxPollution())), false);
        source.sendSuccess(() -> Component.literal("Totals: " + stats.formatTotals()), false);
        source.sendSuccess(() -> Component.literal("Loaded totals: " + stats.formatLoadedTotals()), false);
        source.sendSuccess(() -> Component.literal("Grid bounds: all=" + stats.formatGridBounds()
                + " loaded=" + stats.formatLoadedGridBounds()
                + " blocks=" + stats.formatBlockBounds()
                + " loadedBlocks=" + stats.formatLoadedBlockBounds()), false);
        return stats.totalEntries();
    }

    private static int listPollutionEntries(CommandSourceStack source, int limit) {
        List<PollutionSavedData.EntrySnapshot> entries = PollutionManager.positivePollutionEntriesSnapshot(
                source.getLevel(), limit);
        if (entries.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No positive pollution entries."), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Top pollution entries: " + entries.size()), false);
        for (PollutionSavedData.EntrySnapshot entry : entries) {
            PollutionType dominant = entry.dominantType();
            source.sendSuccess(() -> Component.literal(" - grid " + entry.pos().formatLabel()
                    + " total=" + round(entry.totalPollution())
                    + " dominant=" + (dominant == null ? "none" : dominant.id())
                    + " " + entry.formatValues()), false);
        }
        return entries.size();
    }

    private static int listPollutionEntriesByType(CommandSourceStack source, String typeName, int limit) {
        PollutionType type = parsePollutionType(source, typeName);
        if (type == null) {
            return 0;
        }
        List<PollutionSavedData.EntrySnapshot> entries = PollutionManager.positivePollutionEntriesSnapshot(
                source.getLevel(), type, limit);
        if (entries.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No positive " + type.id() + " pollution entries."), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Top " + type.id() + " pollution entries: " + entries.size()),
                false);
        for (PollutionSavedData.EntrySnapshot entry : entries) {
            source.sendSuccess(() -> Component.literal(" - grid " + entry.pos().formatLabel()
                    + " " + type.id() + "=" + round(entry.get(type))
                    + " total=" + round(entry.totalPollution())
                    + " " + entry.formatValues()), false);
        }
        return entries.size();
    }

    private static int previewPollutionDiffusion(CommandSourceStack source, int limit) {
        PollutionSavedData.DiffusionPreview preview = PollutionManager.previewDiffusion(source.getLevel());
        PollutionSavedData.Stats before = preview.before();
        PollutionSavedData.Stats after = preview.after();
        source.sendSuccess(() -> Component.literal("Pollution diffusion preview: entries="
                + before.totalEntries() + " -> " + after.totalEntries()
                + " delta=" + preview.entryDelta()
                + " total=" + round(before.totalPollution()) + " -> " + round(after.totalPollution())
                + " delta=" + round(preview.totalPollutionDelta())), false);
        source.sendSuccess(() -> Component.literal("Preview totals: " + after.formatTotals()), false);
        source.sendSuccess(() -> Component.literal("Preview grid bounds: all=" + after.formatGridBounds()
                + " loaded=" + after.formatLoadedGridBounds()
                + " blocks=" + after.formatBlockBounds()
                + " loadedBlocks=" + after.formatLoadedBlockBounds()), false);

        List<PollutionSavedData.EntrySnapshot> entries = PollutionManager.positiveDiffusionPreviewEntries(
                source.getLevel(), limit);
        List<PollutionSavedData.EntryDelta> deltas = PollutionManager.diffusionDeltaEntries(source.getLevel(), limit);
        if (entries.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Preview has no positive pollution entries."), false);
        } else {
            source.sendSuccess(() -> Component.literal("Top preview entries: " + entries.size()), false);
            for (PollutionSavedData.EntrySnapshot entry : entries) {
                PollutionType dominant = entry.dominantType();
                source.sendSuccess(() -> Component.literal(" - grid " + entry.pos().formatLabel()
                        + " total=" + round(entry.totalPollution())
                        + " dominant=" + (dominant == null ? "none" : dominant.id())
                        + " " + entry.formatValues()), false);
            }
        }
        if (!deltas.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Top preview changes: " + deltas.size()), false);
            for (PollutionSavedData.EntryDelta delta : deltas) {
                source.sendSuccess(() -> Component.literal(" - grid " + delta.pos().formatLabel()
                        + " total=" + round(delta.totalBefore()) + " -> " + round(delta.totalAfter())
                        + " delta=" + round(delta.totalDelta())
                        + " valuesDelta=" + delta.formatDeltaValues()), false);
            }
        }
        return Math.max(entries.size(), deltas.size());
    }

    private static int previewPollutionDiffusionByType(CommandSourceStack source, String typeName, int limit) {
        PollutionType type = parsePollutionType(source, typeName);
        if (type == null) {
            return 0;
        }
        PollutionSavedData.DiffusionPreview preview = PollutionManager.previewDiffusion(source.getLevel());
        PollutionSavedData.Stats before = preview.before();
        PollutionSavedData.Stats after = preview.after();
        source.sendSuccess(() -> Component.literal("Pollution diffusion preview for " + type.id()
                + ": entries=" + before.totalEntries() + " -> " + after.totalEntries()
                + " " + type.id() + "=" + round(before.total(type)) + " -> " + round(after.total(type))
                + " delta=" + round(after.total(type) - before.total(type))
                + " loaded=" + round(before.loadedTotal(type)) + " -> " + round(after.loadedTotal(type))), false);

        List<PollutionSavedData.EntrySnapshot> entries = PollutionManager.positiveDiffusionPreviewEntries(
                source.getLevel(), type, limit);
        List<PollutionSavedData.EntryDelta> deltas = PollutionManager.diffusionDeltaEntries(
                source.getLevel(), type, limit);
        if (entries.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Preview has no positive " + type.id()
                    + " pollution entries."), false);
        } else {
            source.sendSuccess(() -> Component.literal("Top preview " + type.id() + " entries: " + entries.size()),
                    false);
            for (PollutionSavedData.EntrySnapshot entry : entries) {
                source.sendSuccess(() -> Component.literal(" - grid " + entry.pos().formatLabel()
                        + " " + type.id() + "=" + round(entry.get(type))
                        + " total=" + round(entry.totalPollution())
                        + " " + entry.formatValues()), false);
            }
        }
        if (!deltas.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Top preview " + type.id() + " changes: " + deltas.size()),
                    false);
            for (PollutionSavedData.EntryDelta delta : deltas) {
                source.sendSuccess(() -> Component.literal(" - grid " + delta.pos().formatLabel()
                        + " " + type.id() + "=" + round(delta.typeBefore(type))
                        + " -> " + round(delta.typeAfter(type))
                        + " delta=" + round(delta.typeDelta(type))
                        + " totalDelta=" + round(delta.totalDelta())
                        + " valuesDelta=" + delta.formatDeltaValues()), false);
            }
        }
        return Math.max(entries.size(), deltas.size());
    }

    private static int previewPollutionDiffusionAt(CommandSourceStack source, PollutionGridPos pos) {
        PollutionSavedData.EntryDelta delta = PollutionManager.diffusionDeltaAt(source.getLevel(), pos);
        source.sendSuccess(() -> Component.literal("Pollution diffusion preview at grid "
                + delta.pos().formatLabel()), false);
        sendPollutionDiffusionDeltaLine(source, "grid", delta);
        return delta.hasAnyDelta() ? 1 : 0;
    }

    private static int previewPollutionDiffusionNeighbors(CommandSourceStack source, PollutionGridPos center) {
        List<PollutionSavedData.EntryDelta> deltas = PollutionManager.diffusionNeighborDeltas(
                source.getLevel(), center);
        if (deltas.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No pollution diffusion neighbor preview data."), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Pollution diffusion neighbor preview for "
                + center.formatLabel()), false);
        String[] labels = {"center", "east", "west", "south", "north"};
        for (int i = 0; i < deltas.size(); i++) {
            sendPollutionDiffusionDeltaLine(source, i < labels.length ? labels[i] : "grid", deltas.get(i));
        }
        return deltas.size();
    }

    private static void sendPollutionDiffusionDeltaLine(CommandSourceStack source, String label,
                                                        PollutionSavedData.EntryDelta delta) {
        PollutionSavedData.EntryDelta data = delta == null
                ? PollutionSavedData.EntryDelta.of(new PollutionGridPos(0, 0),
                        new PollutionSavedData.PollutionSample(), new PollutionSavedData.PollutionSample())
                : delta;
        source.sendSuccess(() -> Component.literal(" - " + label + " grid " + data.pos().formatLabel()
                + " loaded=" + PollutionManager.isPollutionGridLoaded(source.getLevel(), data.pos())
                + " total=" + round(data.totalBefore()) + " -> " + round(data.totalAfter())
                + " delta=" + round(data.totalDelta())
                + " before=" + data.before().formatValues()
                + " after=" + data.after().formatValues()
                + " valuesDelta=" + data.formatDeltaValues()), false);
    }

    private static int getPollutionGridOf(CommandSourceStack source, BlockPos pos) {
        PollutionGridPos grid = PollutionGridPos.ofBlock(pos);
        source.sendSuccess(() -> Component.literal("Pollution grid for " + pos.toShortString() + ": "
                + grid.formatLabel()), false);
        return 1;
    }

    private static int getPollutionNeighbors(CommandSourceStack source, PollutionGridPos center) {
        source.sendSuccess(() -> Component.literal("Pollution neighbor grid readout for " + center.formatLabel()), false);
        sendPollutionNeighborLine(source, "center", center);
        sendPollutionNeighborLine(source, "east", center.offset(1, 0));
        sendPollutionNeighborLine(source, "west", center.offset(-1, 0));
        sendPollutionNeighborLine(source, "south", center.offset(0, 1));
        sendPollutionNeighborLine(source, "north", center.offset(0, -1));
        return 5;
    }

    private static void sendPollutionNeighborLine(CommandSourceStack source, String label, PollutionGridPos pos) {
        PollutionSavedData.PollutionSample sample = PollutionManager.getPollutionData(source.getLevel(), pos);
        source.sendSuccess(() -> Component.literal(" - " + label + " grid " + pos.formatLabel()
                + " loaded=" + PollutionManager.isPollutionGridLoaded(source.getLevel(), pos)
                + " total=" + round(sample.sum())
                + " " + sample.formatValues()), false);
    }

    private static int prunePollution(CommandSourceStack source) {
        int removed = PollutionManager.pruneUnloaded(source.getLevel());
        source.sendSuccess(() -> Component.literal("Pruned " + removed + " unloaded pollution entrie(s)."), true);
        return removed;
    }

    private static PollutionType parsePollutionType(CommandSourceStack source, String typeName) {
        PollutionType type = PollutionType.byName(typeName);
        if (type == null) {
            source.sendFailure(Component.literal("Unknown pollution type '" + typeName + "'. Valid: "
                    + String.join(", ", pollutionTypeNames())));
        }
        return type;
    }

    private static String[] pollutionTypeNames() {
        return PollutionType.ids();
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

    private static int getHazmatResistanceStatus(CommandSourceStack source) {
        HazmatResistanceConfig.LoadReport report = HazmatResistanceConfig.loadReport();
        HazmatRegistry.RegistrySnapshot snapshot = HazmatRegistry.registrySnapshot();
        source.sendSuccess(() -> Component.literal("Hazmat resistance: " + report.summary()
                + " config=" + String.valueOf(report.configFile())), false);
        source.sendSuccess(() -> Component.literal("Hazmat registry: resistanceEntries=" + snapshot.resistanceEntries()
                + " protectionEntries=" + snapshot.protectionEntries()
                + " externalResistanceDefaults=" + snapshot.externalResistanceDefaults()
                + " externalProtectionDefaults=" + snapshot.externalProtectionDefaults()), false);
        for (String warning : report.warnings()) {
            source.sendSuccess(() -> Component.literal("Hazmat warning: " + warning), false);
        }
        return snapshot.resistanceEntries();
    }

    private static int reloadHazmatResistanceConfig(CommandSourceStack source) {
        HazmatResistanceConfig.LoadReport report = HazmatResistanceConfig.initialize(FMLPaths.CONFIGDIR.get());
        source.sendSuccess(() -> Component.literal("Reloaded " + report.summary()), true);
        for (String warning : report.warnings()) {
            source.sendFailure(Component.literal("Hazmat warning: " + warning));
        }
        return report.resistanceEntries();
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
        source.sendSuccess(() -> Component.literal("Damage resistance config skips: items="
                + report.skippedItems()
                + " sets=" + report.skippedSets()
                + " entities=" + report.skippedEntities()), false);
        source.sendSuccess(() -> Component.literal("Damage resistance config warnings: "
                + report.warnings().size()
                + "/" + report.warningCount()
                + (report.warningsTruncated() ? " shown" : "")), false);
        source.sendSuccess(() -> Component.literal("Damage resistance runtime: allowSpecialCancel="
                + EntityDamageUtil.allowSpecialCancel()
                + " currentPierceDT=" + round(DamageResistanceHandler.currentPierceDt())
                + " currentPierceDR=" + round(DamageResistanceHandler.currentPierceDr() * 100.0F) + "%"), false);
        for (String warning : report.warnings()) {
            source.sendSuccess(() -> Component.literal("Damage resistance warning: " + warning), false);
        }
        return report.itemStats() + report.setStats() + report.entityStats();
    }

    private static int reloadDamageResistanceConfig(CommandSourceStack source) {
        DamageResistanceConfig.LoadReport report = DamageResistanceConfig.initialize(FMLPaths.CONFIGDIR.get());
        source.sendSuccess(() -> Component.literal("Reloaded " + report.summary()), true);
        if (report.warningsTruncated()) {
            source.sendFailure(Component.literal("Damage resistance warnings truncated: showing "
                    + report.warnings().size()
                    + " of " + report.warningCount()));
        }
        for (String warning : report.warnings()) {
            source.sendFailure(Component.literal("Damage resistance warning: " + warning));
        }
        return report.itemStats() + report.setStats() + report.entityStats();
    }

    private static int selfTestDamageResistance(CommandSourceStack source) {
        DamageResistanceHandler.CoreAudit audit = DamageResistanceHandler.coreAudit();
        EntityDamageUtil.ApplicationAudit applicationAudit = EntityDamageUtil.applicationAudit();
        ModDamageSources.DamageAliasAudit aliasAudit = ModDamageSources.aliasAudit();
        DamageResistanceConfig.ConfigAudit configAudit = DamageResistanceConfig.configAudit();
        DamageResistanceConfig.DefaultAudit defaultAudit = DamageResistanceConfig.defaultAudit();
        DamageResistanceTooltipUtil.TooltipAudit tooltipAudit = DamageResistanceTooltipUtil.tooltipAudit();
        if (audit.passed() && applicationAudit.passed() && aliasAudit.passed() && configAudit.passed()
                && defaultAudit.passed() && tooltipAudit.passed()) {
            source.sendSuccess(() -> Component.literal("Damage resistance self-test passed: core, application, damage aliases, config parser, legacy defaults, and tooltip fallbacks stable."), false);
            source.sendSuccess(() -> Component.literal("Damage resistance damage alias audit: legacyTypes="
                    + aliasAudit.legacyTypes()
                    + " aliases=" + aliasAudit.aliases()), false);
            source.sendSuccess(() -> Component.literal("Damage resistance config audit: skipped items="
                    + configAudit.skippedItems()
                    + " sets=" + configAudit.skippedSets()
                    + " entities=" + configAudit.skippedEntities()
                    + " warningCap=" + configAudit.storedWarnings() + "/" + configAudit.totalWarnings()), false);
            source.sendSuccess(() -> Component.literal("Damage resistance default audit: items="
                    + defaultAudit.itemEntries()
                    + " sets=" + defaultAudit.setEntries()
                    + " entities=" + defaultAudit.entityEntries()), false);
            return 1;
        }
        if (!audit.passed()) {
            source.sendFailure(Component.literal("Damage resistance core self-test failed: "
                    + String.join(", ", audit.problems())));
        }
        if (!applicationAudit.passed()) {
            source.sendFailure(Component.literal("Damage resistance application self-test failed: "
                    + String.join(", ", applicationAudit.problems())));
        }
        if (!aliasAudit.passed()) {
            source.sendFailure(Component.literal("Damage resistance damage alias self-test failed: "
                    + String.join(", ", aliasAudit.problems())));
        }
        if (!configAudit.passed()) {
            source.sendFailure(Component.literal("Damage resistance config self-test failed: "
                    + String.join(", ", configAudit.problems())));
        }
        if (!defaultAudit.passed()) {
            source.sendFailure(Component.literal("Damage resistance default self-test failed: "
                    + String.join(", ", defaultAudit.problems())));
        }
        if (!tooltipAudit.passed()) {
            source.sendFailure(Component.literal("Damage resistance tooltip self-test failed: "
                    + String.join(", ", tooltipAudit.problems())));
        }
        return 0;
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
            DamageType damageType = registry.getHolderOrThrow(legacy.key()).value();
            DamageSource damageSource = new DamageSource(registry.getHolderOrThrow(legacy.key()));
            problems += checkDamageTypeMessageId(source, legacy, damageType.msgId());
            problems += checkDamageTypeTag(source, legacy, "projectile", legacy.projectile(), damageSource.is(DamageTypeTags.IS_PROJECTILE));
            problems += checkDamageTypeTag(source, legacy, "explosion", legacy.explosion(), damageSource.is(DamageTypeTags.IS_EXPLOSION));
            problems += checkDamageTypeTag(source, legacy, "fire", legacy.fire(), damageSource.is(DamageTypeTags.IS_FIRE));
            problems += checkDamageTypeTag(source, legacy, "bypassesArmor", legacy.bypassesArmor(), damageSource.is(DamageTypeTags.BYPASSES_ARMOR));
            problems += checkDamageTypeTag(source, legacy, "absolute", legacy.absolute(), damageSource.is(DamageTypeTags.BYPASSES_RESISTANCE));
            problems += checkDamageTypeTag(source, legacy, "effects", legacy.bypassesEffects(), damageSource.is(DamageTypeTags.BYPASSES_EFFECTS));
            problems += checkDamageTypeTag(source, legacy, "creativeAllowed", legacy.creativeAllowed(), damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY));
        }
        int totalProblems = problems;
        source.sendSuccess(() -> Component.literal("HBM damage type audit: entries="
                + ModDamageSources.legacyDamageTypes().size()
                + " problems=" + totalProblems), totalProblems > 0);
        return totalProblems == 0 ? 1 : totalProblems;
    }

    private static int checkDamageTypeMessageId(CommandSourceStack source, ModDamageSources.LegacyDamageType legacy,
            String actual) {
        String expected = legacy.expectedMessageId();
        if (expected.equals(actual)) {
            return 0;
        }
        source.sendFailure(Component.literal("Damage type message_id mismatch: " + legacy.location()
                + " expected=" + expected + " actual=" + actual
                + " exactKey=" + DamageResistanceHandler.exactTypeKey(actual)));
        return 1;
    }

    private static int checkDamageTypeTag(CommandSourceStack source, ModDamageSources.LegacyDamageType legacy,
            String name, boolean expected, boolean actual) {
        if (expected == actual) {
            return 0;
        }
        source.sendFailure(Component.literal("Damage type tag mismatch: " + legacy.location()
                + " " + name + " expected=" + expected + " actual=" + actual
                + " expectedTags=" + tagLabelSummary(legacy.expectedTagLabels())));
        return 1;
    }

    private static int listDamageResistanceDamageTypes(CommandSourceStack source) {
        var registry = source.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        for (ModDamageSources.LegacyDamageType legacy : ModDamageSources.legacyDamageTypes()) {
            boolean present = registry.containsKey(legacy.location());
            String actualMessageId = present ? registry.get(legacy.location()).msgId() : "<missing>";
            List<String> aliases = ModDamageSources.legacyAliases(legacy.key());
            source.sendSuccess(() -> Component.literal(legacy.location()
                    + " present=" + present
                    + " expectedMessageId=" + legacy.expectedMessageId()
                    + " actualMessageId=" + actualMessageId
                    + " expectedTags=" + tagLabelSummary(legacy.expectedTagLabels())
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
        var registry = source.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        boolean present = registry.containsKey(key.location());
        String expectedTags = ModDamageSources.legacyDamageType(key)
                .map(legacy -> " expectedTags=" + tagLabelSummary(legacy.expectedTagLabels()))
                .orElse("");
        String expectedMessageId = ModDamageSources.legacyDamageType(key)
                .map(legacy -> " expectedMessageId=" + legacy.expectedMessageId())
                .orElse("");
        String actualMessageId = present ? " actualMessageId=" + registry.get(key.location()).msgId() : "";
        source.sendSuccess(() -> Component.literal("Damage type alias '" + damageType + "' -> "
                + key.location()
                + " present=" + present
                + expectedMessageId
                + actualMessageId
                + expectedTags
                + " aliases=" + String.join(",", ModDamageSources.legacyAliases(key))), false);
        return present ? 1 : 0;
    }

    private static String tagLabelSummary(List<String> labels) {
        return labels.isEmpty() ? "none" : String.join(",", labels);
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
                    + " absolute=" + breakdown.absolute()
                    + " amount=" + round(amount)
                    + " DT=" + round(breakdown.rawDt())
                    + " DR=" + round(breakdown.rawDr() * 100.0F) + "%"
                    + " pierceDT=" + round(pierceDt)
                    + " pierceDR=" + round(pierceDr * 100.0F) + "%"
                    + " effectiveDT=" + round(breakdown.effectiveDt())
                    + " effectiveDR=" + round(breakdown.effectiveDr() * 100.0F) + "%"
                    + " fullyAbsorbed=" + breakdown.fullyAbsorbed(amount)
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

    private static String itemStackId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        String nbt = stack.hasTag() ? " +nbt" : "";
        return stack.getCount() + "x " + itemId(stack) + nbt;
    }

    private static String itemStackSummary(List<ItemStack> stacks) {
        return stacks.stream()
                .map(stack -> stack.getCount() + "x " + itemId(stack))
                .collect(Collectors.joining(", "));
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
                "hbm_ntm_rebirth:explosion",
                "hbm_ntm_rebirth:nuclear_blast",
                "hbm_ntm_rebirth:laser",
                "hbm_ntm_rebirth:plasma",
                "hbm_ntm_rebirth:microwave",
                "hbm_ntm_rebirth:electric",
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

    private static int getEnergyChunk(CommandSourceStack source, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        HbmEnergyNodespace.ChunkDiagnostics diagnostics =
                HbmEnergyNodespace.getChunkDiagnostics(source.getLevel(), chunkPos);
        source.sendSuccess(() -> Component.literal("Energy chunk [" + chunkPos.x + ", " + chunkPos.z + "]"
                + " from=" + pos.toShortString()
                + ": loaded=" + diagnostics.loaded()
                + " positions=" + diagnostics.nodePositions()
                + " nodes=" + diagnostics.uniqueNodes()
                + " networks=" + diagnostics.networks()
                + " invalidNetworks=" + diagnostics.invalidNetworks()
                + " linkRefs=" + diagnostics.linkRefs()
                + " dirtyNodes=" + diagnostics.dirtyNodes()
                + " expiredNodes=" + diagnostics.expiredNodes()
                + " orphanNodes=" + diagnostics.orphanNodes()
                + " providers=" + diagnostics.providerEntries()
                + " receivers=" + diagnostics.receiverEntries()), false);
        return diagnostics.uniqueNodes();
    }

    private static int getEnergyPort(CommandSourceStack source, BlockPos machinePos, BlockPos offset, Direction side) {
        if (side == null) {
            source.sendFailure(Component.literal("Unknown direction."));
            return 0;
        }
        HbmEnergyUtil.EnergyPort port = new HbmEnergyUtil.EnergyPort(offset, side);
        HbmEnergyUtil.PortSnapshot snapshot = HbmEnergyUtil.inspectPort(source.getLevel(), machinePos, port);
        source.sendSuccess(() -> Component.literal("Energy port from " + machinePos.toShortString()
                + " offset=" + offset.toShortString()
                + " side=" + side.getName()
                + " conductor=" + snapshot.conductorPos().toShortString()
                + " conductorSide=" + snapshot.conductorSide().getName()
                + " connectorPresent=" + snapshot.connectorPresent()
                + " connectable=" + snapshot.connectable()
                + " node=" + snapshot.nodePresent()
                + " network=" + snapshot.networkPresent()
                + " links=" + snapshot.links()
                + " providers=" + snapshot.providers()
                + " receivers=" + snapshot.receivers()
                + " providerPower=" + snapshot.providerPower() + " HE"
                + " providerRate=" + snapshot.providerRate() + " HE/t"
                + " receiverDemand=" + snapshot.receiverDemand() + " HE"
                + " receiverRate=" + snapshot.receiverRate() + " HE/t"
                + " oldestProvider=" + snapshot.oldestProviderAgeMs() + "ms"
                + " oldestReceiver=" + snapshot.oldestReceiverAgeMs() + "ms"
                + " lastTransfer=" + snapshot.lastTransfer() + " HE"), false);
        return snapshot.networkPresent() ? snapshot.links() : 0;
    }

    private static int getEnergyPorts(CommandSourceStack source, BlockPos pos) {
        BlockEntity blockEntity = source.getLevel().getBlockEntity(pos);
        HbmEnergyUtil.PortSetSnapshot snapshot;
        if (blockEntity instanceof HbmEnergyBlockEntity energyBlockEntity) {
            snapshot = energyBlockEntity.inspectEnergyPorts();
        } else if (blockEntity instanceof HbmEnergyAndFluidBlockEntity energyAndFluidBlockEntity) {
            snapshot = energyAndFluidBlockEntity.inspectEnergyPorts();
        } else {
            source.sendFailure(Component.literal("No HBM energy port machine at " + pos.toShortString()));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Energy ports at " + pos.toShortString()
                + ": total=" + snapshot.totalPorts()
                + " connectable=" + snapshot.connectablePorts()
                + " networked=" + snapshot.networkedPorts()
                + " links=" + snapshot.links()
                + " providers=" + snapshot.providers()
                + " receivers=" + snapshot.receivers()
                + " providerPower=" + snapshot.providerPower() + " HE"
                + " receiverDemand=" + snapshot.receiverDemand() + " HE"), false);
        return snapshot.networkedPorts();
    }

    private static int getEnergyInfo(CommandSourceStack source, BlockPos pos) {
        BlockEntity blockEntity = CompatEnergyControl.findTileEntity(source.getLevel(), pos);
        if (!(blockEntity instanceof InfoProviderEC)
                && !CompatEnergyControl.hasEnergy(blockEntity)
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
        String valueName = ROR.valueName(name);
        String value = provider.provideRORValue(valueName);
        if (value == null) {
            source.sendFailure(Component.literal("No ROR value '" + name + "' at " + pos.toShortString()));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("ROR value " + valueName + " at " + pos.toShortString() + ": " + value), false);
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
            String result = ROR.run(interactive, command);
            source.sendSuccess(() -> Component.literal("ROR function " + name + " at " + pos.toShortString()
                    + (result == null ? ": ok" : ": " + result)), true);
            return 1;
        } catch (RORFunctionException ex) {
            source.sendFailure(Component.literal(ex.getMessage()));
            return 0;
        }
    }

    private static CompletableFuture<Suggestions> suggestRorValues(CommandSourceStack source, BlockPos pos, SuggestionsBuilder builder) {
        return suggestRorEntries(source, pos, builder, RORInfo.PREFIX_VALUE, false);
    }

    private static CompletableFuture<Suggestions> suggestRorFunctionExamples(CommandSourceStack source, BlockPos pos, SuggestionsBuilder builder) {
        return suggestRorEntries(source, pos, builder, RORInfo.PREFIX_FUNCTION, true);
    }

    private static CompletableFuture<Suggestions> suggestRorEntries(CommandSourceStack source, BlockPos pos,
            SuggestionsBuilder builder, String prefix, boolean commandExamples) {
        BlockEntity blockEntity = source.getLevel().getBlockEntity(pos);
        if (!(blockEntity instanceof RORInfo info)) {
            return builder.buildFuture();
        }
        List<String> suggestions = new ArrayList<>();
        for (String entry : info.getFunctionInfo()) {
            if (entry == null || !entry.startsWith(prefix)) {
                continue;
            }
            String suggestion = commandExamples ? rorCommandExample(entry) : entry;
            if (!suggestion.isBlank() && !suggestions.contains(suggestion)) {
                suggestions.add(suggestion);
            }
        }
        return SharedSuggestionProvider.suggest(suggestions, builder);
    }

    private static String rorCommandExample(String entry) {
        int separator = entry.indexOf(RORInteractive.NAME_SEPARATOR);
        if (separator < 0) {
            return entry;
        }
        String command = entry.substring(0, separator + 1);
        String[] params = entry.substring(separator + 1).split(RORInteractive.PARAM_SEPARATOR);
        List<String> examples = new ArrayList<>();
        for (String param : params) {
            examples.add(rorParameterExample(param));
        }
        return command + String.join(RORInteractive.PARAM_SEPARATOR, examples);
    }

    private static String rorParameterExample(String parameterInfo) {
        String normalized = parameterInfo.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("state")) {
            return "1";
        }
        if (normalized.startsWith("mode")) {
            return "0";
        }
        if (normalized.startsWith("priority")) {
            return "1";
        }
        if (normalized.startsWith("fallback")) {
            return "0";
        }
        return "0";
    }

    private static int broadcastRttySignal(CommandSourceStack source, String channel, String signal) {
        RTTYSystem.broadcast(source.getLevel(), channel, signal);
        source.sendSuccess(() -> Component.literal("Queued RTTY signal on " + channel
                + " in " + source.getLevel().dimension().location()
                + ": " + signal), true);
        return 1;
    }

    private static int listenRttySignal(CommandSourceStack source, String channel) {
        RTTYSystem.RTTYChannel rttyChannel = RTTYSystem.listen(source.getLevel(), channel);
        if (rttyChannel == null) {
            source.sendFailure(Component.literal("No RTTY signal on " + channel
                    + " in " + source.getLevel().dimension().location()));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("RTTY signal on " + channel
                + " in " + source.getLevel().dimension().location()
                + ": signal=" + rttyChannel.signalString()
                + " timeStamp=" + rttyChannel.timeStamp()), false);
        return 1;
    }

    private static int readAndBroadcastRorValue(CommandSourceStack source, BlockPos pos, String channel, String name) {
        BlockEntity blockEntity = source.getLevel().getBlockEntity(pos);
        if (!(blockEntity instanceof RORValueProvider provider)) {
            source.sendFailure(Component.literal("No HBM ROR value provider at " + pos.toShortString()));
            return 0;
        }
        String valueName = ROR.valueName(name);
        String value = RORRemoteBridge.readValue(provider, valueName);
        if (value == null) {
            source.sendFailure(Component.literal("No ROR value '" + name + "' at " + pos.toShortString()));
            return 0;
        }
        RTTYSystem.broadcast(source.getLevel(), channel, value);
        source.sendSuccess(() -> Component.literal("Queued ROR value " + valueName
                + " from " + pos.toShortString()
                + " on RTTY channel " + channel
                + ": " + value), true);
        return 1;
    }

    private static int runRorCommandFromSignal(CommandSourceStack source, BlockPos pos, String channel) {
        BlockEntity blockEntity = source.getLevel().getBlockEntity(pos);
        if (!(blockEntity instanceof RORInteractive interactive)) {
            source.sendFailure(Component.literal("No HBM ROR interactive component at " + pos.toShortString()));
            return 0;
        }
        RTTYSystem.RTTYChannel rttyChannel = RTTYSystem.listen(source.getLevel(), channel);
        if (rttyChannel == null) {
            source.sendFailure(Component.literal("No RTTY signal on " + channel));
            return 0;
        }
        String command = rttyChannel.signalString();
        try {
            String result = RORRemoteBridge.runCommand(interactive, command);
            source.sendSuccess(() -> Component.literal("Ran RTTY command from " + channel
                    + " on " + pos.toShortString()
                    + ": " + command
                    + (result == null ? " -> ok" : " -> " + result)), true);
            return 1;
        } catch (RORFunctionException ex) {
            source.sendFailure(Component.literal(ex.getMessage()));
            return 0;
        }
    }

    private static int inspectRttyTorch(CommandSourceStack source, BlockPos pos) {
        RadioTorchBlockEntity torch = getRttyTorch(source, pos);
        if (torch == null) {
            return 0;
        }
        List<Component> lines = torch.describeRadioConfiguration();
        source.sendSuccess(() -> Component.literal("RTTY torch at " + pos.toShortString()
                + " type=" + torch.getType()), false);
        for (Component line : lines) {
            source.sendSuccess(() -> line, false);
        }
        return Math.max(1, lines.size());
    }

    private static int setRttyTorchKey(CommandSourceStack source, BlockPos pos, String key, String value) {
        RadioTorchBlockEntity torch = getRttyTorch(source, pos);
        if (torch == null) {
            return 0;
        }
        CompoundTag tag = createRttyControlTag(source, torch, key, value);
        if (tag == null) {
            return 0;
        }
        return applyRttyTorchConfiguration(source, pos, torch, tag, key + "=" + value);
    }

    private static int setRttyTorchChannel(CommandSourceStack source, BlockPos pos, String channel) {
        RadioTorchBlockEntity torch = getRttyTorch(source, pos);
        if (torch == null) {
            return 0;
        }
        if (torch instanceof RadioTorchReaderBlockEntity || torch instanceof RadioTorchCounterBlockEntity) {
            source.sendFailure(Component.literal("Use reader/counter slot commands for multi-channel RTTY torches"));
            return 0;
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("c", channel);
        return applyRttyTorchConfiguration(source, pos, torch, tag, "channel=" + channel);
    }

    private static int setRttyTorchPolling(CommandSourceStack source, BlockPos pos, boolean polling) {
        RadioTorchBlockEntity torch = getRttyTorch(source, pos);
        if (torch == null) {
            return 0;
        }
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("p", polling);
        return applyRttyTorchConfiguration(source, pos, torch, tag, "polling=" + polling);
    }

    private static int setRttyTorchCustomMap(CommandSourceStack source, BlockPos pos, boolean customMap) {
        RadioTorchBlockEntity torch = getRttyTorch(source, pos);
        if (!(torch instanceof RadioTorchDeviceBlockEntity)) {
            source.sendFailure(Component.literal("Custom redstone mapping is only valid on sender/receiver RTTY torches"));
            return 0;
        }
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("m", customMap);
        return applyRttyTorchConfiguration(source, pos, torch, tag, "customMap=" + customMap);
    }

    private static int setRttyTorchMapping(CommandSourceStack source, BlockPos pos, int index, String mapped) {
        RadioTorchBlockEntity torch = getRttyTorch(source, pos);
        if (!(torch instanceof RadioTorchDeviceBlockEntity || torch instanceof RadioTorchLogicBlockEntity)) {
            source.sendFailure(Component.literal("Mapping entries are only valid on sender/receiver/logic RTTY torches"));
            return 0;
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("m" + index, mapped);
        return applyRttyTorchConfiguration(source, pos, torch, tag, "m" + index + "=" + mapped);
    }

    private static int setRttyTorchLogicRule(CommandSourceStack source, BlockPos pos, int index, int condition,
            String mapped) {
        RadioTorchBlockEntity torch = getRttyTorch(source, pos);
        if (!(torch instanceof RadioTorchLogicBlockEntity)) {
            source.sendFailure(Component.literal("Logic rules are only valid on RTTY logic receivers"));
            return 0;
        }
        CompoundTag tag = new CompoundTag();
        tag.putInt("c" + index, condition);
        tag.putString("m" + index, mapped);
        return applyRttyTorchConfiguration(source, pos, torch, tag,
                "rule " + index + " condition=" + condition + " mapped=" + mapped);
    }

    private static int setRttyTorchDescending(CommandSourceStack source, BlockPos pos, boolean descending) {
        RadioTorchBlockEntity torch = getRttyTorch(source, pos);
        if (!(torch instanceof RadioTorchLogicBlockEntity)) {
            source.sendFailure(Component.literal("Descending scan order is only valid on RTTY logic receivers"));
            return 0;
        }
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("d", descending);
        return applyRttyTorchConfiguration(source, pos, torch, tag, "descending=" + descending);
    }

    private static int setRttyTorchReaderSlot(CommandSourceStack source, BlockPos pos, int slot, String channel,
            String name) {
        RadioTorchBlockEntity torch = getRttyTorch(source, pos);
        if (!(torch instanceof RadioTorchReaderBlockEntity)) {
            source.sendFailure(Component.literal("Reader slots are only valid on RTTY readers"));
            return 0;
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("c" + slot, channel);
        tag.putString("n" + slot, name);
        return applyRttyTorchConfiguration(source, pos, torch, tag,
                "slot " + slot + " channel=" + channel + " name=" + name);
    }

    private static int setRttyTorchCounterSlot(CommandSourceStack source, BlockPos pos, int slot, String channel) {
        RadioTorchBlockEntity torch = getRttyTorch(source, pos);
        if (!(torch instanceof RadioTorchCounterBlockEntity)) {
            source.sendFailure(Component.literal("Counter slots are only valid on RTTY counters"));
            return 0;
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("c" + slot, channel);
        return applyRttyTorchConfiguration(source, pos, torch, tag, "slot " + slot + " channel=" + channel);
    }

    private static CompletableFuture<Suggestions> suggestRttyTorchKeys(CommandSourceStack source, BlockPos pos,
            SuggestionsBuilder builder) {
        BlockEntity blockEntity = source.getLevel().getBlockEntity(pos);
        if (!(blockEntity instanceof RadioTorchBlockEntity torch)) {
            return builder.buildFuture();
        }
        List<String> keys = new ArrayList<>();
        keys.add("p");
        if (torch instanceof RadioTorchDeviceBlockEntity) {
            keys.add("c");
            keys.add("m");
            for (int i = 0; i < 16; i++) {
                keys.add("m" + i);
            }
        } else if (torch instanceof RadioTorchLogicBlockEntity) {
            keys.add("c");
            keys.add("d");
            for (int i = 0; i < 16; i++) {
                keys.add("m" + i);
                keys.add("c" + i);
            }
        } else if (torch instanceof RadioTorchReaderBlockEntity) {
            for (int i = 0; i < RTTYReaderState.SLOT_COUNT; i++) {
                keys.add("c" + i);
                keys.add("n" + i);
            }
        } else if (torch instanceof RadioTorchCounterBlockEntity) {
            for (int i = 0; i < RTTYCounterState.SLOT_COUNT; i++) {
                keys.add("c" + i);
            }
        } else {
            keys.add("c");
        }
        return SharedSuggestionProvider.suggest(keys, builder);
    }

    private static RadioTorchBlockEntity getRttyTorch(CommandSourceStack source, BlockPos pos) {
        BlockEntity blockEntity = source.getLevel().getBlockEntity(pos);
        if (blockEntity instanceof RadioTorchBlockEntity torch) {
            return torch;
        }
        source.sendFailure(Component.literal("No RTTY torch block entity at " + pos.toShortString()));
        return null;
    }

    private static CompoundTag createRttyControlTag(CommandSourceStack source, RadioTorchBlockEntity torch,
            String key, String value) {
        CompoundTag tag = new CompoundTag();
        if ("p".equals(key)) {
            tag.putBoolean(key, parseBoolean(value));
            return tag;
        }
        if ("m".equals(key) && torch instanceof RadioTorchDeviceBlockEntity) {
            tag.putBoolean(key, parseBoolean(value));
            return tag;
        }
        if ("d".equals(key) && torch instanceof RadioTorchLogicBlockEntity) {
            tag.putBoolean(key, parseBoolean(value));
            return tag;
        }
        if ("c".equals(key)
                && !(torch instanceof RadioTorchReaderBlockEntity)
                && !(torch instanceof RadioTorchCounterBlockEntity)) {
            tag.putString(key, value);
            return tag;
        }
        int index = indexedRttyKey(key, 'm', 15);
        if (index >= 0 && (torch instanceof RadioTorchDeviceBlockEntity || torch instanceof RadioTorchLogicBlockEntity)) {
            tag.putString(key, value);
            return tag;
        }
        index = indexedRttyKey(key, 'c', torch instanceof RadioTorchReaderBlockEntity
                ? RTTYReaderState.SLOT_COUNT - 1
                : torch instanceof RadioTorchCounterBlockEntity
                        ? RTTYCounterState.SLOT_COUNT - 1
                        : 15);
        if (index >= 0) {
            if (torch instanceof RadioTorchLogicBlockEntity) {
                tag.putInt(key, Mth.clamp(parseInt(value, 0), 0, 9));
                return tag;
            }
            if (torch instanceof RadioTorchReaderBlockEntity || torch instanceof RadioTorchCounterBlockEntity) {
                tag.putString(key, value);
                return tag;
            }
        }
        index = indexedRttyKey(key, 'n', RTTYReaderState.SLOT_COUNT - 1);
        if (index >= 0 && torch instanceof RadioTorchReaderBlockEntity) {
            tag.putString(key, value);
            return tag;
        }
        source.sendFailure(Component.literal("Unsupported RTTY torch control key '" + key
                + "' for " + torch.getType()));
        return null;
    }

    private static int applyRttyTorchConfiguration(CommandSourceStack source, BlockPos pos, RadioTorchBlockEntity torch,
            CompoundTag tag, String action) {
        boolean changed = torch.applyRadioConfiguration(tag);
        source.sendSuccess(() -> Component.literal("RTTY torch " + pos.toShortString()
                + " " + action
                + (changed ? " applied" : " unchanged")), true);
        return changed ? 1 : 0;
    }

    private static int indexedRttyKey(String key, char prefix, int maxIndex) {
        if (key.length() < 2 || key.charAt(0) != prefix) {
            return -1;
        }
        int index = parseInt(key.substring(1), -1);
        return index >= 0 && index <= maxIndex ? index : -1;
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static int mapRttyRedstoneSignal(CommandSourceStack source, int input, boolean custom, String mapped) {
        String[] mapping = singleMappedSignal(input, mapped);
        String signal = RTTYSignalMapper.redstoneToSignal(input, custom, mapping);
        source.sendSuccess(() -> Component.literal("RTTY map send: input=" + input
                + " custom=" + custom
                + " mapped=" + mapped
                + " signal=" + signal
                + " willBroadcast=" + !signal.isEmpty()), false);
        return signal.isEmpty() ? 0 : 1;
    }

    private static int receiveRttyMappedSignal(CommandSourceStack source, String signal, boolean custom, int index,
            String mapped) {
        String[] mapping = singleMappedSignal(index, mapped);
        int redstone = RTTYSignalMapper.signalToRedstone(signal, custom, mapping);
        source.sendSuccess(() -> Component.literal("RTTY map receive: signal=" + signal
                + " custom=" + custom
                + " index=" + index
                + " mapped=" + mapped
                + " redstone=" + redstone), false);
        return redstone;
    }

    private static int evalRttyCounterSignal(CommandSourceStack source, String channel, int last, int count,
            boolean polling) {
        RTTYCounterState counter = new RTTYCounterState();
        counter.setChannel(0, channel);
        counter.setLastCount(0, last);
        counter.setPolling(polling);
        RTTYCounterState.CounterDecision decision = counter.evaluateCount(0, count);
        if (decision.shouldBroadcast()) {
            RTTYSystem.broadcast(source.getLevel(), decision.channel(), decision.count());
        }
        source.sendSuccess(() -> Component.literal("RTTY counter eval: channel=" + channel
                + " last=" + last
                + " count=" + count
                + " polling=" + polling
                + " changed=" + decision.countChanged()
                + " queued=" + decision.shouldBroadcast()), true);
        return decision.shouldBroadcast() ? 1 : 0;
    }

    private static String[] singleMappedSignal(int index, String mapped) {
        String[] mapping = new String[RTTYSignalMapper.REDSTONE_LEVELS];
        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = "";
        }
        mapping[index] = mapped;
        return mapping;
    }

    private static int parseRttyLogicSignal(CommandSourceStack source, String signal, int condition, String mapped) {
        boolean result = RTTYLogicEvaluator.parseSignal(signal, mapped, condition);
        source.sendSuccess(() -> Component.literal("RTTY logic parse: signal=" + signal
                + " condition=" + condition
                + " mapped=" + mapped
                + " result=" + result), false);
        return result ? 1 : 0;
    }

    private static int evalRttyLogicSignal(CommandSourceStack source, String signal, boolean descending, int index,
            int condition, String mapped) {
        String[] mapping = new String[16];
        int[] conditions = new int[16];
        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = "";
        }
        mapping[index] = mapped;
        conditions[index] = condition;
        int result = RTTYLogicEvaluator.evaluate(signal, mapping, conditions, descending);
        source.sendSuccess(() -> Component.literal("RTTY logic eval: signal=" + signal
                + " descending=" + descending
                + " index=" + index
                + " condition=" + condition
                + " mapped=" + mapped
                + " resultRedstone=" + result), false);
        return result;
    }

    private static int getRttyStats(CommandSourceStack source) {
        RTTYSystem.Diagnostics diagnostics = RTTYSystem.diagnostics(source.getLevel());
        List<String> channels = RTTYSystem.channels(source.getLevel());
        source.sendSuccess(() -> Component.literal("RTTY system: " + diagnostics.summary()
                + " channels=" + String.join(", ", channels)), false);
        return diagnostics.levelBroadcastChannels();
    }

    private static int clearRttySignals(CommandSourceStack source) {
        RTTYSystem.clear(source.getLevel());
        source.sendSuccess(() -> Component.literal("Cleared RTTY channels for "
                + source.getLevel().dimension().location()), true);
        return 1;
    }

    private static int getFluidNodespace(CommandSourceStack source) {
        HbmFluidNodespace.Diagnostics diagnostics = HbmFluidNodespace.getDiagnostics(source.getLevel());
        source.sendSuccess(() -> Component.literal("Fluid nodespace: positions=" + diagnostics.nodePositions()
                + " nodes=" + diagnostics.uniqueNodes()
                + " networks=" + diagnostics.networks()
                + " invalidNetworks=" + diagnostics.invalidNetworks()
                + " linkRefs=" + diagnostics.linkRefs()
                + " dirtyNodes=" + diagnostics.dirtyNodes()
                + " expiredNodes=" + diagnostics.expiredNodes()
                + " orphanNodes=" + diagnostics.orphanNodes()
                + " providers=" + diagnostics.providerEntries()
                + " receivers=" + diagnostics.receiverEntries()
                + " reapTimer=" + diagnostics.reapTimer()), false);
        return diagnostics.uniqueNodes();
    }

    private static int rebuildFluidNodespace(CommandSourceStack source) {
        HbmFluidNodespace.ForceRebuildResult result = HbmFluidNodespace.forceRebuild(source.getLevel());
        source.sendSuccess(() -> Component.literal("Fluid nodespace rebuilt: nodes=" + result.nodes()
                + " oldNetworks=" + result.oldNetworks()
                + " newNetworks=" + result.newNetworks()
                + " reapTimer=" + result.reapTimer()), true);
        return result.newNetworks();
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
                + " lastTransfer=" + network.lastTransfer() + " mB"
                + " lastAttempted=" + formatPressureArray(network.lastAttemptedByPressure()) + " mB"
                + " lastTransferred=" + formatPressureArray(network.lastTransferredByPressure()) + " mB"
                + " lastProviderUse=" + formatPressureArray(network.lastProviderUseByPressure()) + " mB"
                + " lastUnaccounted=" + formatPressureArray(network.lastUnaccountedByPressure()) + " mB"), false);
        return network.links();
    }

    private static int damageFluidNetworkFromOverpressure(CommandSourceStack source, BlockPos pos, FluidType type) {
        if (type == HbmFluids.NONE) {
            source.sendFailure(Component.literal("Unknown or empty HBM fluid type."));
            return 0;
        }
        HbmFluidNodespace.OverpressureDamageResult result =
                HbmFluidNodespace.damageNetworkFromOverpressure(source.getLevel(), pos, type);
        if (!result.nodePresent()) {
            source.sendFailure(Component.literal("No HBM fluid node for ")
                    .append(type.getDisplayName())
                    .append(" at " + pos.toShortString()));
            return 0;
        }
        if (!result.networkPresent()) {
            source.sendSuccess(() -> Component.literal("Fluid node at " + pos.toShortString()
                    + " type=" + result.fluid()
                    + " has no active network to overpressure."), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Fluid overpressure at " + pos.toShortString()
                + " type=" + result.fluid()
                + ": pipeNodes=" + result.pipeNodes()
                + " pipeNodesDamaged=" + result.pipeNodesDamaged()
                + " receiversDamaged=" + result.receiversDamaged()), true);
        return result.pipeNodesDamaged() + result.receiversDamaged();
    }

    private static int damageAllFluidNetworksFromOverpressure(CommandSourceStack source, BlockPos pos) {
        HbmFluidNodespace.OverpressureBatchDamageResult result =
                HbmFluidNodespace.damageAllNetworksFromOverpressure(source.getLevel(), pos);
        if (!result.nodePresent()) {
            source.sendFailure(Component.literal("No HBM fluid nodes at " + pos.toShortString()));
            return 0;
        }
        if (!result.networkPresent()) {
            source.sendSuccess(() -> Component.literal("HBM fluid nodes at " + pos.toShortString()
                    + " have no active networks to overpressure. fluidNodes=" + result.fluidNodes()), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Fluid overpressure at " + pos.toShortString()
                + ": fluidNodes=" + result.fluidNodes()
                + " networks=" + result.networks()
                + " pipeNodes=" + result.pipeNodes()
                + " pipeNodesDamaged=" + result.pipeNodesDamaged()
                + " receivers=" + result.receivers()
                + " receiversDamaged=" + result.receiversDamaged()), true);
        return result.pipeNodesDamaged() + result.receiversDamaged();
    }

    private static int getFluidTraits(CommandSourceStack source, FluidType type) {
        JsonObject traits = new JsonObject();
        for (var entry : type.getTraitJson().entrySet()) {
            traits.add(entry.getKey(), entry.getValue());
        }
        source.sendSuccess(() -> Component.literal("Fluid traits for " + type.getName() + ": " + traits), false);
        return traits.size();
    }

    private static int getFluidTraitConfigStatus(CommandSourceStack source) {
        HbmFluidTraitConfig.LoadReport report = HbmFluidTraitConfig.loadReport();
        source.sendSuccess(() -> Component.literal("Fluid trait config: " + report.summary()), false);
        for (String warning : report.warnings()) {
            source.sendSuccess(() -> Component.literal(" - " + warning), false);
        }
        return report.traits();
    }

    private static int reloadFluidTraitConfig(CommandSourceStack source) {
        HbmFluidTraitConfig.LoadReport report = HbmFluids.bootstrap(FMLPaths.CONFIGDIR.get());
        source.sendSuccess(() -> Component.literal("Reloaded " + report.summary()), true);
        for (String warning : report.warnings()) {
            source.sendSuccess(() -> Component.literal(" - " + warning), false);
        }
        return report.traits();
    }

    private static int getFluidDefinition(CommandSourceStack source, FluidType type) {
        JsonObject definition = type.toJson();
        source.sendSuccess(() -> Component.literal("Fluid definition for " + type.getName() + ": " + definition), false);
        return type.getId();
    }

    private static int getFluidTypeConfigStatus(CommandSourceStack source) {
        HbmFluidTypeConfig.LoadReport report = HbmFluidTypeConfig.loadReport();
        source.sendSuccess(() -> Component.literal("Fluid type config: " + report.summary()), false);
        for (String warning : report.warnings()) {
            source.sendSuccess(() -> Component.literal(" - " + warning), false);
        }
        source.sendSuccess(() -> Component.literal("Custom HBM fluids currently loaded: " + HbmFluids.customFluids().size()), false);
        return report.customFluids();
    }

    private static int reloadFluidTypes(CommandSourceStack source) {
        HbmFluidTraitConfig.LoadReport traitReport = HbmFluids.bootstrap(FMLPaths.CONFIGDIR.get());
        HbmFluidTypeConfig.LoadReport typeReport = HbmFluidTypeConfig.loadReport();
        source.sendSuccess(() -> Component.literal("Reloaded " + typeReport.summary()), true);
        for (String warning : typeReport.warnings()) {
            source.sendSuccess(() -> Component.literal(" - " + warning), false);
        }
        source.sendSuccess(() -> Component.literal("Reloaded " + traitReport.summary()), false);
        for (String warning : traitReport.warnings()) {
            source.sendSuccess(() -> Component.literal(" - " + warning), false);
        }
        return typeReport.customFluids();
    }

    private static int getFluidCompatStatus(CommandSourceStack source) {
        HbmCompatFluidRegistry.Diagnostics diagnostics = HbmCompatFluidRegistry.diagnostics();
        source.sendSuccess(() -> Component.literal("Fluid compat registry: " + diagnostics.summary()), false);
        return diagnostics.foreignFluids();
    }

    private static int getCompatStatus(CommandSourceStack source) {
        CompatRecipeRegistry.Diagnostics recipeDiagnostics = CompatRecipeRegistry.diagnostics();
        HbmCompatFluidRegistry.Diagnostics fluidDiagnostics = HbmCompatFluidRegistry.diagnostics();
        HbmFluidForgeMappings.Diagnostics forgeFluidDiagnostics = HbmFluidForgeMappings.diagnostics();
        HbmFluidContainerRegistry.Diagnostics containerDiagnostics = HbmFluidContainerRegistry.diagnostics();
        CompatTurretTargetRegistry.Diagnostics turretDiagnostics = CompatTurretTargetRegistry.diagnostics();
        CompatCustomWarheadRegistry.Diagnostics warheadDiagnostics = CompatCustomWarheadRegistry.diagnostics();
        CompatRecipeRegistry.RecipeFacadeCoverage recipeCoverage = CompatRecipeRegistry.recipeFacadeCoverage();
        source.sendSuccess(() -> Component.literal("HBM compat integration status:"), false);
        source.sendSuccess(() -> Component.literal(" - optional mods loaded: "
                + CompatHandler.optionalCompatSummary()), false);
        source.sendSuccess(() -> Component.literal(" - " + recipeDiagnostics.summary()), false);
        source.sendSuccess(() -> Component.literal(" - " + recipeCoverage.summary()), false);
        source.sendSuccess(() -> Component.literal(" - recipe JSON facades: "
                + String.join(", ", CompatRecipeRegistry.supportedRecipeFacades())), false);
        source.sendSuccess(() -> Component.literal(" - " + fluidDiagnostics.summary()), false);
        source.sendSuccess(() -> Component.literal(" - " + forgeFluidDiagnostics.summary()), false);
        source.sendSuccess(() -> Component.literal(" - " + containerDiagnostics.summary()), false);
        source.sendSuccess(() -> Component.literal(" - " + turretDiagnostics.summary()), false);
        source.sendSuccess(() -> Component.literal(" - " + warheadDiagnostics.summary()), false);
        source.sendSuccess(() -> Component.literal(" - computer/peripheral integrations: "
                + CompatHandler.computerIntegrationStatus() + "."), false);
        source.sendSuccess(() -> Component.literal(" - AE2 integration: deferred optional compatibility slice."), false);
        return recipeDiagnostics.listeners()
                + fluidDiagnostics.listeners()
                + containerDiagnostics.listeners()
                + forgeFluidDiagnostics.exportMappings()
                + forgeFluidDiagnostics.importMappings()
                + turretDiagnostics.totalRegistrations()
                + warheadDiagnostics.totalRegistrations();
    }

    private static int getCompatModsStatus(CommandSourceStack source) {
        List<String> modIds = compatOptionalModIds();
        source.sendSuccess(() -> Component.literal("HBM optional compat mods:"), false);
        for (String modId : modIds) {
            source.sendSuccess(() -> Component.literal(" - " + modId + ": " + Compat.isModLoaded(modId)), false);
        }
        source.sendSuccess(() -> Component.literal("Computer/peripheral integrations: "
                + CompatHandler.computerIntegrationStatus() + "."), false);
        source.sendSuccess(() -> Component.literal("AE2 integration: deferred optional compatibility slice."), false);
        source.sendSuccess(() -> Component.literal("Railcraft/Torcherino legacy hacks: disabled in modern compat."), false);
        return Compat.loadedModIds(modIds).size();
    }

    private static int getCompatRecipeStatus(CommandSourceStack source) {
        CompatRecipeRegistry.Diagnostics diagnostics = CompatRecipeRegistry.diagnostics();
        CompatRecipeRegistry.RecipeFacadeCoverage coverage = CompatRecipeRegistry.recipeFacadeCoverage();
        source.sendSuccess(() -> Component.literal("Compat recipe registry: " + diagnostics.summary()), false);
        source.sendSuccess(() -> Component.literal("Supported datapack JSON facades: "
                + String.join(", ", CompatRecipeRegistry.supportedRecipeFacades())), false);
        source.sendSuccess(() -> Component.literal("Legacy recipe facade coverage: " + coverage.summary()), false);
        source.sendSuccess(() -> Component.literal("Deferred legacy recipe facades: "
                + String.join(", ", CompatRecipeRegistry.deferredLegacyRecipeFacades())), false);
        for (CompatRecipeRegistry.RecipeFacadeStatus status : CompatRecipeRegistry.recipeFacadeStatuses()) {
            if (!status.supported()) {
                source.sendSuccess(() -> Component.literal(" - " + status.summary()), false);
            }
        }
        source.sendSuccess(() -> Component.literal("Runtime RecipeManager mutation: disabled; use datapack/datagen/reload-stage sinks."), false);
        return diagnostics.listeners() + diagnostics.lastEmittedRecipes();
    }

    private static int getCompatFluidStatus(CommandSourceStack source) {
        HbmCompatFluidRegistry.Diagnostics fluidDiagnostics = HbmCompatFluidRegistry.diagnostics();
        HbmFluidForgeMappings.Diagnostics forgeFluidDiagnostics = HbmFluidForgeMappings.diagnostics();
        HbmFluidContainerRegistry.Diagnostics containerDiagnostics = HbmFluidContainerRegistry.diagnostics();
        source.sendSuccess(() -> Component.literal("Compat fluid registry: " + fluidDiagnostics.summary()), false);
        source.sendSuccess(() -> Component.literal("Forge fluid mappings: " + forgeFluidDiagnostics.summary()), false);
        source.sendSuccess(() -> Component.literal("Fluid container registry: " + containerDiagnostics.summary()), false);
        source.sendSuccess(() -> Component.literal("HBM fluid definitions: total=" + HbmFluids.all().size()
                + " custom=" + HbmFluids.customFluids().size()
                + " foreign=" + HbmFluids.foreignFluids().size()
                + " fixedContainers=" + HbmFluidContainerRegistry.getFixedContainersSnapshot().size()), false);
        source.sendSuccess(() -> Component.literal("Compat fluid facade: "
                + CompatFluidRegistry.diagnostics().summary()
                + "; containers=" + CompatFluidRegistry.containerDiagnostics().summary()), false);
        return fluidDiagnostics.listeners()
                + forgeFluidDiagnostics.exportMappings()
                + forgeFluidDiagnostics.importMappings()
                + containerDiagnostics.externalContainers();
    }

    private static int getCompatExternalHookStatus(CommandSourceStack source) {
        CompatTurretTargetRegistry.Diagnostics turretDiagnostics = CompatTurretTargetRegistry.diagnostics();
        CompatCustomWarheadRegistry.Diagnostics warheadDiagnostics = CompatCustomWarheadRegistry.diagnostics();
        source.sendSuccess(() -> Component.literal("Compat external hooks: "
                + turretDiagnostics.summary()
                + "; " + warheadDiagnostics.summary()), false);
        source.sendSuccess(() -> Component.literal("Turret target hooks are registered for future turret consumers; "
                + "they do not target entities until a migrated turret uses them."), false);
        source.sendSuccess(() -> Component.literal("Custom warhead impact hooks are consumed by CustomMissileExplosion CUSTOM0..CUSTOM9."), false);
        return turretDiagnostics.totalRegistrations() + warheadDiagnostics.totalRegistrations();
    }

    private static int getCompatSteamLevel(CommandSourceStack source, int level) {
        FluidType type = Compat.intToSteamType(level);
        source.sendSuccess(() -> Component.literal("Steam compression level " + level + " -> " + type.getName()), false);
        return level;
    }

    private static int getCompatSteamFluid(CommandSourceStack source, String fluidName) {
        FluidType type = HbmFluids.fromName(fluidName);
        if (type == HbmFluids.NONE) {
            source.sendFailure(Component.literal("Unknown HBM fluid: " + fluidName));
            return 0;
        }
        int level = Compat.steamTypeToInt(type);
        source.sendSuccess(() -> Component.literal("HBM fluid " + type.getName() + " -> steam compression level " + level), false);
        return level;
    }

    private static List<String> compatOptionalModIds() {
        return CompatHandler.optionalCompatModIds();
    }

    private static int getFluidForgeMappingStatus(CommandSourceStack source) {
        HbmFluidForgeAliasConfig.LoadReport report = HbmFluidForgeAliasConfig.loadReport();
        HbmFluidForgeMappings.Diagnostics diagnostics = HbmFluidForgeMappings.diagnostics();
        source.sendSuccess(() -> Component.literal("Fluid Forge mapping config: " + report.summary()), false);
        for (String warning : report.warnings()) {
            source.sendSuccess(() -> Component.literal(" - " + warning), false);
        }
        source.sendSuccess(() -> Component.literal("Fluid Forge mappings: " + diagnostics.summary()), false);
        return diagnostics.tagAliases();
    }

    private static int resolveForgeFluidMapping(CommandSourceStack source, String forgeFluidName) {
        ResourceLocation id = ResourceLocation.tryParse(forgeFluidName);
        if (id == null) {
            source.sendFailure(Component.literal("Invalid Forge fluid id: " + forgeFluidName));
            return 0;
        }
        var forgeFluid = ForgeRegistries.FLUIDS.getValue(id);
        if (forgeFluid == null) {
            source.sendFailure(Component.literal("Unknown Forge fluid: " + id));
            return 0;
        }
        FluidType type = HbmFluidForgeMappings.fromForge(forgeFluid);
        source.sendSuccess(() -> Component.literal("Forge fluid " + id
                + " -> HBM " + type.getName()
                + " exportable=" + HbmFluidForgeMappings.canExport(type)), false);
        return type == HbmFluids.NONE ? 0 : 1;
    }

    private static int getFluidContainerRegistryStatus(CommandSourceStack source) {
        HbmFluidContainerRegistry.Diagnostics diagnostics = HbmFluidContainerRegistry.diagnostics();
        HbmFluidContainerConfig.LoadReport report = HbmFluidContainerConfig.loadReport();
        source.sendSuccess(() -> Component.literal("Fluid container registry: " + diagnostics.summary()), false);
        source.sendSuccess(() -> Component.literal("Fluid container config: " + report.summary()), false);
        for (String warning : report.warnings()) {
            source.sendSuccess(() -> Component.literal(" - " + warning), false);
        }
        return diagnostics.externalContainers();
    }

    private static int reloadFluidContainers(CommandSourceStack source) {
        HbmFluidContainerRegistry.reloadExternalContainers(FMLPaths.CONFIGDIR.get());
        HbmFluidContainerRegistry.Diagnostics diagnostics = HbmFluidContainerRegistry.diagnostics();
        HbmFluidContainerConfig.LoadReport report = HbmFluidContainerConfig.loadReport();
        source.sendSuccess(() -> Component.literal("Reloaded fluid containers: " + diagnostics.summary()), true);
        source.sendSuccess(() -> Component.literal("Fluid container config: " + report.summary()), false);
        for (String warning : report.warnings()) {
            source.sendSuccess(() -> Component.literal(" - " + warning), false);
        }
        return diagnostics.externalContainers();
    }

    private static int summarizeFluidContainers(CommandSourceStack source) {
        List<HbmFluidContainerRegistry.ContainerEntry> fixedEntries = HbmFluidContainerRegistry.getFixedContainersSnapshot();
        Map<HbmFluidContainerRegistry.ContainerSource, Integer> sourceCounts =
                new EnumMap<>(HbmFluidContainerRegistry.ContainerSource.class);
        Map<FluidType, Integer> fluidCounts = new java.util.IdentityHashMap<>();
        int consumableEntries = 0;
        int emptyReturningEntries = 0;
        for (HbmFluidContainerRegistry.ContainerEntry entry : fixedEntries) {
            sourceCounts.merge(entry.source(), 1, Integer::sum);
            fluidCounts.merge(entry.type(), 1, Integer::sum);
            if (entry.emptyContainer().isEmpty()) {
                consumableEntries++;
            } else {
                emptyReturningEntries++;
            }
        }
        int finalConsumableEntries = consumableEntries;
        int finalEmptyReturningEntries = emptyReturningEntries;
        source.sendSuccess(() -> Component.literal("Fixed fluid container summary: entries=" + fixedEntries.size()
                + " sources=" + formatContainerSourceCounts(sourceCounts)
                + " emptyReturning=" + finalEmptyReturningEntries
                + " consumable=" + finalConsumableEntries), false);
        for (FluidType type : HbmFluids.niceOrder()) {
            int count = fluidCounts.getOrDefault(type, 0);
            if (count > 0) {
                source.sendSuccess(() -> Component.literal(" - " + type.getName() + ": " + count), false);
            }
        }
        source.sendSuccess(() -> Component.literal("Dynamic HBM container kinds are counted by /hbm fluid containers list <fluid>."), false);
        return fixedEntries.size();
    }

    private static int listFluidContainers(CommandSourceStack source, @Nullable FluidType type) {
        List<HbmFluidContainerRegistry.ContainerEntry> entries;
        if (type == null) {
            entries = HbmFluidContainerRegistry.getFixedContainersSnapshot();
            source.sendSuccess(() -> Component.literal("Fixed fluid container entries: " + entries.size()
                    + " (use /hbm fluid containers list <fluid> to include dynamic HBM container kinds)"), false);
        } else if (type == HbmFluids.NONE) {
            source.sendFailure(Component.literal("Unknown or empty HBM fluid type."));
            return 0;
        } else {
            entries = HbmFluidContainerRegistry.getContainers(type);
            source.sendSuccess(() -> Component.literal("Fluid container entries for " + type.getName() + ": " + entries.size()), false);
        }
        for (HbmFluidContainerRegistry.ContainerEntry entry : entries) {
            source.sendSuccess(() -> Component.literal(" - " + formatFluidContainerEntry(entry)), false);
        }
        return entries.size();
    }

    private static int resolveHeldFluidContainer(CommandSourceStack source) throws CommandSyntaxException {
        ItemStack stack = source.getPlayerOrException().getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("Hold a fluid container item."));
            return 0;
        }
        FluidType type = HbmFluidContainerRegistry.getFluidType(stack);
        int content = type == HbmFluids.NONE ? 0 : HbmFluidContainerRegistry.getFluidContent(stack, type);
        ItemStack empty = HbmFluidContainerRegistry.getEmptyContainer(stack);
        source.sendSuccess(() -> Component.literal("Held fluid container: item=" + itemStackId(stack)
                + " fluid=" + type.getName()
                + " content=" + content + " mB"
                + " empty=" + itemStackId(empty)), false);
        if (type != HbmFluids.NONE) {
            HbmFluidContainerRegistry.ContainerEntry entry = HbmFluidContainerRegistry.getFixedContainersSnapshot().stream()
                    .filter(candidate -> candidate.type() == type && candidate.matchesFull(stack))
                    .findFirst()
                    .orElse(null);
            if (entry != null) {
                source.sendSuccess(() -> Component.literal("Matched fixed entry: " + formatFluidContainerEntry(entry)), false);
            }
        }
        int capabilityTanks = describeHeldFluidItemCapability(source, stack);
        return type == HbmFluids.NONE ? capabilityTanks : Math.max(content, capabilityTanks);
    }

    private static int describeHeldFluidItemCapability(CommandSourceStack source, ItemStack stack) {
        IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve().orElse(null);
        if (handler == null) {
            source.sendSuccess(() -> Component.literal("Forge fluid item capability: none"), false);
            return 0;
        }
        int tanks = handler.getTanks();
        source.sendSuccess(() -> Component.literal("Forge fluid item capability: tanks=" + tanks
                + " container=" + itemStackId(handler.getContainer())), false);
        for (int i = 0; i < tanks; i++) {
            int tankIndex = i;
            source.sendSuccess(() -> formatForgeTank(tankIndex, handler.getFluidInTank(tankIndex),
                    handler.getTankCapacity(tankIndex)), false);
        }
        return tanks;
    }

    private static int getFluidPort(CommandSourceStack source, BlockPos machinePos, BlockPos offset, Direction side,
            FluidType type) {
        if (type == HbmFluids.NONE) {
            source.sendFailure(Component.literal("Unknown or empty HBM fluid type."));
            return 0;
        }
        if (side == null) {
            source.sendFailure(Component.literal("Unknown direction."));
            return 0;
        }
        HbmFluidUtil.FluidPort port = new HbmFluidUtil.FluidPort(offset, side);
        HbmFluidUtil.PortSnapshot snapshot = HbmFluidUtil.inspectPort(source.getLevel(), machinePos, port, type);
        source.sendSuccess(() -> Component.literal("Fluid port from " + machinePos.toShortString()
                + " offset=" + offset.toShortString()
                + " side=" + side.getName()
                + " connector=" + snapshot.connectorPos().toShortString()
                + " connectorSide=" + snapshot.connectorSide().getName()
                + " fluid=" + snapshot.fluid()
                + " connectorPresent=" + snapshot.connectorPresent()
                + " connectable=" + snapshot.connectable()
                + " node=" + snapshot.nodePresent()
                + " network=" + snapshot.networkPresent()
                + " links=" + snapshot.links()
                + " providers=" + snapshot.providers()
                + " receivers=" + snapshot.receivers()), false);
        return snapshot.networkPresent() ? snapshot.links() : 0;
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

    private static int linkFluidPipeAnchors(CommandSourceStack source, BlockPos firstPos, BlockPos secondPos) {
        FluidPipeAnchorBlockEntity first = fluidPipeAnchorAt(source, firstPos);
        FluidPipeAnchorBlockEntity second = fluidPipeAnchorAt(source, secondPos);
        if (first == null || second == null) {
            return 0;
        }
        FluidPipeAnchorBlockEntity.LinkResult result = FluidPipeAnchorBlockEntity.link(first, second);
        if (result != FluidPipeAnchorBlockEntity.LinkResult.CONNECTED) {
            source.sendFailure(Component.literal("Fluid pipe anchor link failed: " + fluidAnchorLinkMessage(result)));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Fluid pipe anchors linked: "
                + firstPos.toShortString() + " <-> " + secondPos.toShortString()), true);
        return 1;
    }

    private static int unlinkFluidPipeAnchors(CommandSourceStack source, BlockPos firstPos, BlockPos secondPos) {
        FluidPipeAnchorBlockEntity first = fluidPipeAnchorAt(source, firstPos);
        FluidPipeAnchorBlockEntity second = fluidPipeAnchorAt(source, secondPos);
        if (first == null || second == null) {
            return 0;
        }
        boolean changed = first.unlinkRemote(secondPos) | second.unlinkRemote(firstPos);
        if (!changed) {
            source.sendFailure(Component.literal("Fluid pipe anchors were not linked: "
                    + firstPos.toShortString() + " <-> " + secondPos.toShortString()));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Fluid pipe anchors unlinked: "
                + firstPos.toShortString() + " <-> " + secondPos.toShortString()), true);
        return 1;
    }

    private static int clearFluidPipeAnchor(CommandSourceStack source, BlockPos pos) {
        FluidPipeAnchorBlockEntity anchor = fluidPipeAnchorAt(source, pos);
        if (anchor == null) {
            return 0;
        }
        int count = anchor.getRemoteConnections().size();
        boolean changed = anchor.clearRemoteConnections();
        if (!changed) {
            source.sendFailure(Component.literal("Fluid pipe anchor has no remote links at " + pos.toShortString()));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Cleared " + count + " fluid pipe anchor link(s) at "
                + pos.toShortString()), true);
        return count;
    }

    private static int getFluidPipeAnchorInfo(CommandSourceStack source, BlockPos pos) {
        FluidPipeAnchorBlockEntity anchor = fluidPipeAnchorAt(source, pos);
        if (anchor == null) {
            return 0;
        }
        List<String> connections = anchor.getRemoteConnections().stream()
                .map(BlockPos::toShortString)
                .sorted()
                .toList();
        source.sendSuccess(() -> Component.literal("Fluid pipe anchor at " + pos.toShortString()
                + " type=" + anchor.getFluidType().getName()
                + " remoteLinks=" + connections.size()
                + " [" + String.join(", ", connections) + "]"), false);
        return connections.size();
    }

    private static FluidPipeAnchorBlockEntity fluidPipeAnchorAt(CommandSourceStack source, BlockPos pos) {
        BlockEntity blockEntity = source.getLevel().getBlockEntity(pos);
        if (!(blockEntity instanceof FluidPipeAnchorBlockEntity anchor)) {
            source.sendFailure(Component.literal("No HBM fluid pipe anchor at " + pos.toShortString()));
            return null;
        }
        return anchor;
    }

    private static String fluidAnchorLinkMessage(FluidPipeAnchorBlockEntity.LinkResult result) {
        return switch (result) {
            case CONNECTED -> "connected";
            case INCOMPATIBLE -> "anchors are not compatible";
            case SAME_BLOCK -> "cannot connect an anchor to itself";
            case TOO_FAR -> "pipe anchor is too far away";
            case FLUID_MISMATCH -> "pipe anchor fluid types do not match";
        };
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
            java.util.Map<Integer, RegistryObject<Item>> mappedItems = LegacyMetaItemMappings.mappingsByMeta().getOrDefault(legacyId, java.util.Map.of());
            variants += mappedItems.size();
            source.sendSuccess(() -> Component.literal("Legacy meta " + legacyId + " variants=" + mappedItems.size()), false);
            for (java.util.Map.Entry<Integer, RegistryObject<Item>> entry : mappedItems.entrySet()) {
                source.sendSuccess(() -> Component.literal(" - " + entry.getKey() + " -> " + entry.getValue().getId()), false);
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

    private static int listLegacyGenericRecipeHandlers(CommandSourceStack source) {
        List<LegacyGenericRecipeHandlers.Handler> handlers = LegacyGenericRecipeHandlers.all();
        long supported = handlers.stream().filter(LegacyGenericRecipeHandlers.Handler::supported).count();
        source.sendSuccess(() -> Component.literal("Legacy generic recipe handlers: supported=" + supported
                + "/" + handlers.size()), false);
        for (LegacyGenericRecipeHandlers.Handler handler : handlers) {
            source.sendSuccess(() -> Component.literal(" - " + handler.commandSummary()), false);
        }
        return (int) supported;
    }

    private static int listLegacySerializableRecipeHandlers(CommandSourceStack source) {
        List<LegacySerializableRecipeHandlers.Handler> handlers = LegacySerializableRecipeHandlers.all();
        LegacySerializableRecipeHandlers.Coverage coverage = LegacySerializableRecipeHandlers.coverage();
        source.sendSuccess(() -> Component.literal("Legacy SerializableRecipe handlers: total="
                + coverage.totalHandlers()
                + ", genericImporter=" + coverage.genericSupported()
                + ", modernSerializerOnly=" + coverage.modernSerializerOnly()
                + ", unsupported=" + coverage.unsupported()), false);
        for (LegacySerializableRecipeHandlers.Handler handler : handlers) {
            source.sendSuccess(() -> Component.literal(" - " + handler.commandSummary()), false);
        }
        return coverage.genericSupported();
    }

    private static int getItemPoolSummary(CommandSourceStack source) {
        Map<String, ResourceLocation> knownTables = HbmItemPoolRegistry.knownTables();
        List<HbmItemPoolRegistry.MissingTable> missing = HbmItemPoolRegistry.missingKnownTables(source.getLevel());
        long available = HbmItemPoolRegistry.availableKnownTableCount(source.getLevel());
        source.sendSuccess(() -> Component.literal("HBM item pools: known=" + knownTables.size()
                + ", availableTables=" + available
                + ", missingTables=" + missing.size()), missing.size() > 0);
        return missing.isEmpty() ? 1 : missing.size();
    }

    private static int listItemPoolTables(CommandSourceStack source) {
        List<HbmItemPoolRegistry.TableStatus> statuses = HbmItemPoolRegistry.knownTableStatuses(source.getLevel());
        long available = statuses.stream().filter(HbmItemPoolRegistry.TableStatus::available).count();
        source.sendSuccess(() -> Component.literal("HBM item pool tables: known=" + statuses.size()
                + ", available=" + available
                + ", missing=" + (statuses.size() - available)), statuses.size() != available);
        int shown = 0;
        for (HbmItemPoolRegistry.TableStatus status : statuses.stream().limit(96).toList()) {
            shown++;
            source.sendSuccess(() -> Component.literal(" - " + status.legacyPoolId()
                    + " -> " + status.tableId()
                    + " available=" + status.available()), false);
        }
        int remaining = statuses.size() - shown;
        if (remaining > 0) {
            source.sendSuccess(() -> Component.literal(" - ... " + remaining + " more"), false);
        }
        return (int) available;
    }

    private static int listMissingItemPoolTables(CommandSourceStack source) {
        List<HbmItemPoolRegistry.MissingTable> missing = HbmItemPoolRegistry.missingKnownTables(source.getLevel());
        if (missing.isEmpty()) {
            source.sendSuccess(() -> Component.literal("All known HBM item pool loot tables are available."), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("Missing HBM item pool loot tables: " + missing.size()), true);
        int shown = 0;
        for (HbmItemPoolRegistry.MissingTable table : missing.stream()
                .sorted((left, right) -> left.legacyPoolId().compareTo(right.legacyPoolId()))
                .limit(64)
                .toList()) {
            shown++;
            source.sendSuccess(() -> Component.literal(" - " + table.legacyPoolId()
                    + " -> " + table.tableId()), false);
        }
        int remaining = missing.size() - shown;
        if (remaining > 0) {
            source.sendSuccess(() -> Component.literal(" - ... " + remaining + " more"), false);
        }
        return missing.size();
    }

    private static int listLegacyLootNames(CommandSourceStack source) {
        Map<String, String> mapped = HbmLegacyLootUtil.mappedItemPoolLootNames();
        List<String> deferred = HbmLegacyLootUtil.deferredLootNames();
        source.sendSuccess(() -> Component.literal("HBM legacy loot names: total=" + HbmLegacyLootUtil.LOOT_NAMES.size()
                + ", itemPoolMapped=" + mapped.size()
                + ", deferred=" + deferred.size()), !deferred.isEmpty());
        for (String lootName : HbmLegacyLootUtil.LOOT_NAMES) {
            Optional<String> poolId = HbmLegacyLootUtil.itemPoolIdForLootName(lootName);
            if (poolId.isPresent()) {
                ResourceLocation tableId = HbmItemPoolRegistry.lootTableId(poolId.get());
                boolean available = HbmItemPoolRegistry.hasTable(source.getLevel(), tableId);
                source.sendSuccess(() -> Component.literal(" - " + lootName
                        + " -> " + poolId.get()
                        + " -> " + tableId
                        + " available=" + available), false);
            } else {
                source.sendSuccess(() -> Component.literal(" - " + lootName + " deferred"), false);
            }
        }
        return mapped.size();
    }

    private static int queryLegacyLootName(CommandSourceStack source, String lootName) {
        Optional<String> poolId = HbmLegacyLootUtil.itemPoolIdForLootName(lootName);
        if (poolId.isEmpty()) {
            source.sendSuccess(() -> Component.literal("HBM legacy loot name " + lootName
                    + " is deferred; no safe Item Pool mapping yet."), false);
            return 0;
        }
        ResourceLocation tableId = HbmItemPoolRegistry.lootTableId(poolId.get());
        boolean available = HbmItemPoolRegistry.hasTable(source.getLevel(), tableId);
        source.sendSuccess(() -> Component.literal("HBM legacy loot name " + lootName
                + " -> " + poolId.get()
                + " -> " + tableId
                + " available=" + available), false);
        return available ? 1 : 0;
    }

    private static int rollLegacyLootName(CommandSourceStack source, String lootName) {
        Optional<String> poolId = HbmLegacyLootUtil.itemPoolIdForLootName(lootName);
        if (poolId.isEmpty()) {
            source.sendFailure(Component.literal("HBM legacy loot name " + lootName
                    + " has no safe Item Pool mapping yet."));
            return 0;
        }

        List<HbmLegacyLootUtil.PlacedLootStack> placedStacks = HbmLegacyLootUtil.rollMappedItemPoolLoot(
                source.getLevel(), lootName, source.getPosition(), RandomSource.create());
        List<ItemStack> stacks = placedStacks.stream().map(HbmLegacyLootUtil.PlacedLootStack::stack).toList();
        if (stacks.isEmpty()) {
            source.sendFailure(Component.literal("HBM legacy loot name " + lootName
                    + " mapped to " + poolId.get() + " but produced no loot."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("HBM legacy loot name " + lootName
                + " mapped to " + poolId.get()
                + " rolled " + stacks.size() + " stack(s): "
                + itemStackSummary(stacks)), false);
        return stacks.size();
    }

    private static int queryItemPoolTable(CommandSourceStack source, String legacyPoolId) {
        ResourceLocation tableId = HbmItemPoolRegistry.lootTableId(legacyPoolId);
        boolean known = HbmItemPoolRegistry.isKnownPoolId(legacyPoolId);
        boolean available = HbmItemPoolRegistry.hasTable(source.getLevel(), tableId);
        source.sendSuccess(() -> Component.literal("HBM item pool " + legacyPoolId
                + " -> " + tableId
                + " known=" + known
                + " available=" + available), false);
        return available ? 1 : 0;
    }

    private static int rollItemPool(CommandSourceStack source, String legacyPoolId) {
        Vec3 origin = source.getPosition();
        HbmItemPoolRegistry.RollResult result = HbmItemPoolRegistry.rollStacks(source.getLevel(), legacyPoolId, origin);
        List<ItemStack> stacks = result.stacks();
        if (stacks.isEmpty()) {
            source.sendFailure(Component.literal("HBM item pool " + legacyPoolId + " produced no loot."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("HBM item pool " + legacyPoolId
                + " rolled " + stacks.size() + " stack(s)"
                + " table=" + result.rolledTableId()
                + (result.usedBackup() ? " fallbackFrom=" + result.requestedTableId() : "")
                + ": " + itemStackSummary(stacks)), false);
        return stacks.size();
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

    private static String formatFluidContainerEntry(HbmFluidContainerRegistry.ContainerEntry entry) {
        String kind = entry.kind() == null ? "fixed" : entry.kind().name().toLowerCase(Locale.US);
        return "source=" + entry.source().name().toLowerCase(Locale.US)
                + " kind=" + kind
                + " fluid=" + entry.type().getName()
                + " amount=" + entry.content() + " mB"
                + " full=" + itemStackId(entry.fullContainer())
                + " empty=" + itemStackId(entry.emptyContainer());
    }

    private static String formatContainerSourceCounts(Map<HbmFluidContainerRegistry.ContainerSource, Integer> counts) {
        List<String> parts = new ArrayList<>();
        for (HbmFluidContainerRegistry.ContainerSource source : HbmFluidContainerRegistry.ContainerSource.values()) {
            int count = counts.getOrDefault(source, 0);
            if (count > 0) {
                parts.add(source.name().toLowerCase(Locale.US) + "=" + count);
            }
        }
        return parts.isEmpty() ? "none" : String.join(", ", parts);
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
        source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " radiation: " + round(HbmLivingProperties.getRadiation(player)) + " RAD"), false);
        return Math.round(HbmLivingProperties.getRadiation(player));
    }

    private static int getPlayerRadiation(CommandSourceStack source, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            getPlayerRadiation(source, player);
        }
        return players.size();
    }

    private static int setPlayerRadiation(CommandSourceStack source, Collection<ServerPlayer> players, float amount) {
        for (ServerPlayer player : players) {
            HbmLivingProperties.setRadiation(player, amount);
        }
        syncPlayers(players);
        source.sendSuccess(() -> Component.literal("Set radiation for " + players.size() + " player(s)."), true);
        return players.size();
    }

    private static int addPlayerRadiation(CommandSourceStack source, Collection<ServerPlayer> players, float amount) {
        for (ServerPlayer player : players) {
            HbmLivingProperties.incrementRadiation(player, amount);
        }
        syncPlayers(players);
        source.sendSuccess(() -> Component.literal("Updated radiation for " + players.size() + " player(s)."), true);
        return players.size();
    }

    private static int getPlayerDigamma(CommandSourceStack source, Player player) {
        source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " digamma: " + round(HbmLivingProperties.getDigamma(player))), false);
        return Math.round(HbmLivingProperties.getDigamma(player));
    }

    private static int getPlayerDigamma(CommandSourceStack source, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            getPlayerDigamma(source, player);
        }
        return players.size();
    }

    private static int setPlayerDigamma(CommandSourceStack source, Collection<ServerPlayer> players, float amount) {
        for (ServerPlayer player : players) {
            HbmLivingProperties.setDigamma(player, amount);
        }
        syncPlayers(players);
        source.sendSuccess(() -> Component.literal("Set digamma for " + players.size() + " player(s)."), true);
        return players.size();
    }

    private static int addPlayerDigamma(CommandSourceStack source, Collection<ServerPlayer> players, float amount) {
        for (ServerPlayer player : players) {
            HbmLivingProperties.incrementDigamma(player, amount);
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
        Collection<HbmLivingProperties.ContaminationEffect> effects = HbmLivingProperties.getCont(player);
        source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " contamination entries: " + effects.size()), false);
        int index = 0;
        for (HbmLivingProperties.ContaminationEffect effect : effects) {
            int currentIndex = index++;
            source.sendSuccess(() -> Component.literal("#" + currentIndex
                    + " maxRad=" + round(effect.maxRad)
                    + " maxTime=" + effect.maxTime
                    + " time=" + effect.time
                    + " currentRad=" + round(effect.currentRadiation())
                    + " ignoreArmor=" + effect.ignoreArmor), false);
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
            HbmLivingProperties.addCont(player, new HbmLivingProperties.ContaminationEffect(maxRad, maxTime, time, ignoreArmor));
        }
        syncPlayers(players);
        source.sendSuccess(() -> Component.literal("Added contamination entry for " + players.size() + " player(s)."), true);
        return players.size();
    }

    private static int removeContamination(CommandSourceStack source, Collection<ServerPlayer> players, int index) {
        int removed = 0;
        for (ServerPlayer player : players) {
            if (HbmLivingProperties.removeCont(player, index)) {
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
            removed += HbmLivingProperties.clearCont(player);
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
        source.sendSuccess(() -> Component.literal("HBM network protocol: "
                + ModMessages.networkChannelSummary()
                + " fingerprint=" + ModMessages.protocolManifestFingerprint()
                + " packets=" + ModMessages.registeredPacketCount()
                + " s2c=" + clientbound
                + " c2s=" + serverbound
                + " legacyRegistered=" + ModMessages.legacyPacketRegistrationCount()
                + " legacyMapped=" + ModMessages.mappedLegacyPacketCount()
                + " legacyUnmapped=" + ModMessages.unmappedLegacyPacketRegistrations().size()
                + " legacyMappings=" + ModMessages.legacyPacketMappingCount()
                + " blockedUnregisteredSends=" + ModMessages.blockedUnregisteredSendCount()
                + " blockedWrongDirectionSends=" + ModMessages.blockedWrongDirectionSendCount()
                + " blockedInvalidTargetSends=" + ModMessages.blockedInvalidTargetSendCount()), false);
        if (!registrations.isEmpty()) {
            ModMessages.PacketRegistration first = registrations.get(0);
            ModMessages.PacketRegistration last = registrations.get(registrations.size() - 1);
            source.sendSuccess(() -> Component.literal("Packet id range: #"
                    + first.id() + " " + first.typeName()
                    + " -> #" + last.id() + " " + last.typeName()), false);
        }
        return registrations.size();
    }

    private static int getNetworkProtocolChannel(CommandSourceStack source) {
        ModMessages.NetworkChannelSnapshot snapshot = ModMessages.networkChannelSnapshot();
        source.sendSuccess(() -> Component.literal("HBM network channel: legacy="
                + snapshot.legacyChannelName()
                + " modern=" + snapshot.modernChannelName()
                + " protocol=" + snapshot.protocolVersion()), false);
        source.sendSuccess(() -> Component.literal("Modern packet id range: count="
                + snapshot.registeredPacketCount()
                + " first=#" + snapshot.firstModernPacketId() + " " + snapshot.firstModernPacketName()
                + " last=#" + snapshot.lastModernPacketId() + " " + snapshot.lastModernPacketName()
                + " contiguous=" + snapshot.modernPacketIdsContiguous()), false);
        source.sendSuccess(() -> Component.literal("Legacy discriminator range: count="
                + snapshot.legacyPacketRegistrationCount()
                + " first=#" + snapshot.firstLegacyPacketId() + " " + snapshot.firstLegacyPacketName()
                + " last=#" + snapshot.lastLegacyPacketId() + " " + snapshot.lastLegacyPacketName()
                + " contiguous=" + snapshot.legacyPacketIdsContiguous()), false);
        source.sendSuccess(() -> Component.literal("Channel note: " + snapshot.notes()), false);
        return snapshot.registeredPacketCount();
    }

    private static int getNetworkProtocolFingerprint(CommandSourceStack source) {
        ModMessages.ProtocolManifestSnapshot snapshot = ModMessages.protocolManifestSnapshot();
        source.sendSuccess(() -> Component.literal("HBM network protocol fingerprint: "
                + snapshot.fingerprint()
                + " legacyChannel=" + snapshot.legacyChannelName()
                + " modernChannel=" + snapshot.modernChannelName()
                + " protocol=" + snapshot.protocolVersion()
                + " modernPackets=" + snapshot.modernPacketCount()
                + " legacyPackets=" + snapshot.legacyPacketCount()
                + " mappingRows=" + snapshot.mappingRowCount()
                + " auditProblems=" + snapshot.auditProblems()), false);
        source.sendSuccess(() -> Component.literal(snapshot.notes()), false);
        return snapshot.auditProblems() ? 0 : 1;
    }

    private static int getNetworkProtocolManifest(CommandSourceStack source) {
        ModMessages.ProtocolManifestSnapshot snapshot = ModMessages.protocolManifestSnapshot();
        source.sendSuccess(() -> Component.literal("HBM network manifest: fingerprint="
                + snapshot.fingerprint()
                + " legacy=" + snapshot.legacyPacketCount()
                + " modern=" + snapshot.modernPacketCount()
                + " mappings=" + snapshot.mappingRowCount()
                + " auditProblems=" + snapshot.auditProblems()), false);
        for (ModMessages.ProtocolManifestRow row : snapshot.rows()) {
            source.sendSuccess(() -> Component.literal("#" + row.legacyId()
                    + " " + row.direction()
                    + " " + row.legacyName()
                    + " mappings=" + row.mappingCount()
                    + " -> " + row.modernPackets()
                    + (row.notes().isBlank() ? "" : " (" + row.notes() + ")")), false);
        }
        return snapshot.rows().size();
    }

    private static int getNetworkProtocolContract(CommandSourceStack source) {
        ModMessages.ProtocolContractSnapshot snapshot = ModMessages.protocolContractSnapshot();
        source.sendSuccess(() -> Component.literal("HBM network protocol contract: "
                + ModMessages.protocolContractSummary()), false);
        source.sendSuccess(() -> Component.literal("Contract channels: legacy="
                + snapshot.legacyChannelName()
                + " modern=" + snapshot.modernChannelName()
                + " protocol=" + snapshot.protocolVersion()), false);
        source.sendSuccess(() -> Component.literal(snapshot.notes()), false);
        for (String problem : snapshot.problems()) {
            source.sendSuccess(() -> Component.literal(" - " + problem), false);
        }
        return snapshot.passed() ? 1 : 0;
    }

    private static int getNetworkProtocolHandlers(CommandSourceStack source) {
        ModMessages.HandlerRuntimeSnapshot snapshot = ModMessages.handlerRuntimeSnapshot();
        source.sendSuccess(() -> Component.literal("HBM network handler runtime: "
                + ModMessages.handlerRuntimeSummary()), false);
        if (!snapshot.lastFailure().isBlank()) {
            source.sendSuccess(() -> Component.literal("Last handler failure: " + snapshot.lastFailure()), false);
        }
        return snapshot.hasFailures() ? 0 : (int) Math.min(Integer.MAX_VALUE, snapshot.totalDispatches());
    }

    private static int getNetworkProtocolCodec(CommandSourceStack source) {
        ModMessages.CodecRuntimeSnapshot snapshot = ModMessages.codecRuntimeSnapshot();
        source.sendSuccess(() -> Component.literal("HBM network codec runtime: "
                + ModMessages.codecRuntimeSummary()), false);
        if (!snapshot.lastFailure().isBlank()) {
            source.sendSuccess(() -> Component.literal("Last codec failure: " + snapshot.lastFailure()), false);
        }
        if (!snapshot.lastSizeWarning().isBlank()) {
            source.sendSuccess(() -> Component.literal("Last codec size warning: " + snapshot.lastSizeWarning()), false);
        }
        return snapshot.hasWarnings() ? 0 : (int) Math.min(Integer.MAX_VALUE, snapshot.encodes() + snapshot.decodes());
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

    private static int getNetworkProtocolDiagnostics(CommandSourceStack source) {
        ModMessages.NetworkRuntimeSnapshot snapshot = ModMessages.networkRuntimeSnapshot();
        source.sendSuccess(() -> Component.literal("HBM network runtime: "
                + ModMessages.networkRuntimeSummary()), false);
        source.sendSuccess(() -> Component.literal("Protocol contract: "
                + ModMessages.protocolContractSummary()), false);
        source.sendSuccess(() -> Component.literal("Protocol audit: problems=" + snapshot.auditProblems()
                + " missingModern=" + snapshot.auditMissingModern()
                + " unknownLegacy=" + snapshot.auditUnknownLegacy()
                + " directionMismatch=" + snapshot.auditDirectionMismatch()
                + " duplicateEntries=" + snapshot.auditDuplicateEntries()
                + " unmappedLegacy=" + snapshot.unmappedLegacyPacketCount()), false);
        source.sendSuccess(() -> Component.literal("Send safety: blocked="
                + snapshot.sendSafety().totalBlockedSends()
                + " unregistered=" + snapshot.sendSafety().blockedUnregisteredSends()
                + " wrongDirection=" + snapshot.sendSafety().blockedWrongDirectionSends()
                + " invalidTarget=" + snapshot.sendSafety().blockedInvalidTargetSends()
                + (snapshot.sendSafety().lastBlockedSend().isBlank()
                        ? ""
                        : " lastBlocked=\"" + snapshot.sendSafety().lastBlockedSend() + "\"")), false);
        source.sendSuccess(() -> Component.literal("Codec runtime: "
                + ModMessages.codecRuntimeSummary()), false);
        source.sendSuccess(() -> Component.literal("Handler runtime: "
                + ModMessages.handlerRuntimeSummary()), false);
        source.sendSuccess(() -> Component.literal("Threading runtime: pending=" + snapshot.threaded().pending()
                + " queued=" + snapshot.threaded().totalQueued()
                + " prepared=" + snapshot.threaded().totalPrepared()
                + " preparable=" + snapshot.threaded().preparableMessages()
                + " plain=" + snapshot.threaded().nonPreparableMessages()
                + " preparedCopies=" + snapshot.threaded().preparedCopyInstance()
                + " sent=" + snapshot.threaded().totalSent()
                + " failed=" + snapshot.threaded().totalFailed()
                + " discarded=" + snapshot.threaded().totalDiscarded()
                + " fallback=" + snapshot.threaded().fallbackToMainThread()
                + " enabled=" + snapshot.threaded().enabled()), false);
        source.sendSuccess(() -> Component.literal("Legacy facade runtime: flushCalls="
                + snapshot.legacyFlushCalls()
                + " waitCalls=" + snapshot.legacyWaitCalls()
                + " rawBufferBlocked=" + snapshot.rawBufferBlockedSends()
                + " dimensionIdBlocked=" + snapshot.dimensionIdBlockedSends()
                + " lastTickTotal=" + snapshot.legacyPacketThreading().lastTickTotal()
                + " remaining=" + snapshot.legacyPacketThreading().remaining()
                + " helpers=" + snapshot.totalHelperCount()), false);
        if (!snapshot.lastRawBufferBlockedSend().isBlank()) {
            source.sendSuccess(() -> Component.literal("Last legacy raw buffer block: "
                    + snapshot.lastRawBufferBlockedSend()), false);
        }
        if (!snapshot.lastDimensionIdBlockedSend().isBlank()) {
            source.sendSuccess(() -> Component.literal("Last legacy dimension id block: "
                    + snapshot.lastDimensionIdBlockedSend()), false);
        }
        return snapshot.hasRuntimeWarnings() ? 0 : 1;
    }

    private static int resetNetworkProtocolRuntime(CommandSourceStack source) {
        ModMessages.resetNetworkRuntimeDiagnostics();
        source.sendSuccess(() -> Component.literal("Reset HBM network runtime diagnostics: "
                + ModMessages.networkRuntimeSummary()), true);
        return 1;
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

    private static int getNetworkSendSafety(CommandSourceStack source) {
        ModMessages.SendSafetySnapshot snapshot = ModMessages.sendSafetySnapshot();
        source.sendSuccess(() -> Component.literal("HBM network send safety: registeredTypes="
                + snapshot.registeredTypes()
                + " blockedTotal=" + snapshot.totalBlockedSends()
                + " blockedUnregistered=" + snapshot.blockedUnregisteredSends()
                + " blockedWrongDirection=" + snapshot.blockedWrongDirectionSends()
                + " blockedInvalidTarget=" + snapshot.blockedInvalidTargetSends()
                + (snapshot.lastBlockedSend().isBlank() ? "" : " lastBlocked=\"" + snapshot.lastBlockedSend() + "\"")), false);
        return (int) Math.min(Integer.MAX_VALUE, snapshot.totalBlockedSends());
    }

    private static int resetNetworkSendSafety(CommandSourceStack source) {
        ModMessages.resetSendSafetyCounters();
        source.sendSuccess(() -> Component.literal("Reset HBM network send safety counters."), true);
        return 1;
    }

    private static int getNetworkRawBufferStatus(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("HBM legacy raw buffer network: "
                + LegacyRawBufferNetwork.summary()), false);
        return (int) Math.min(Integer.MAX_VALUE, LegacyRawBufferNetwork.blockedRawBufferSendCount());
    }

    private static int getNetworkDimensionIdStatus(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("HBM legacy dimension id network: "
                + LegacyDimensionIdNetwork.summary()), false);
        return (int) Math.min(Integer.MAX_VALUE, LegacyDimensionIdNetwork.blockedDimensionIdSendCount());
    }

    private static int getNetworkTargetPointStatus(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("HBM legacy target point network: "
                + LegacyTargetPoint.compatibilitySummary()), false);
        source.sendSuccess(() -> Component.literal("Legacy target point dimension audit: "
                + LegacyDimensionIdNetwork.summary()), false);
        return LegacyTargetPoint.modernFactoryCount() + LegacyTargetPoint.legacyFactoryCount();
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

    private static int queryLegacyNetworkPacketById(CommandSourceStack source, int legacyId) {
        Optional<ModMessages.LegacyPacketRegistration> registration = ModMessages.legacyPacketRegistration(legacyId);
        if (registration.isEmpty()) {
            source.sendFailure(Component.literal("Unknown legacy packet discriminator: " + legacyId));
            return 0;
        }
        return queryLegacyNetworkPacket(source, registration.get().legacyName());
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

    private static int queryModernNetworkPacketById(CommandSourceStack source, int id) {
        Optional<ModMessages.PacketRegistration> registration = ModMessages.packetRegistration(id);
        if (registration.isEmpty()) {
            source.sendFailure(Component.literal("Unknown modern packet id: " + id));
            return 0;
        }
        return queryModernNetworkPacket(source, registration.get().typeName());
    }

    private static int getNetworkLegacyWrapper(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("HBM legacy network wrapper: "
                + ModMessages.legacyWrapperSummary()), false);
        source.sendSuccess(() -> Component.literal("Legacy flush: "
                + LegacyNetworkDispatcher.flushCompatibilitySummary()), false);
        source.sendSuccess(() -> Component.literal("Legacy packet threading: "
                + LegacyPacketThreading.compatibilitySummary()), false);
        source.sendSuccess(() -> Component.literal("Legacy raw buffer: "
                + LegacyRawBufferNetwork.summary()), false);
        source.sendSuccess(() -> Component.literal("Legacy dimension id: "
                + LegacyDimensionIdNetwork.summary()), false);
        source.sendSuccess(() -> Component.literal("Legacy target point: "
                + LegacyTargetPoint.compatibilitySummary()), false);
        source.sendSuccess(() -> Component.literal("Send safety: "
                + ModMessages.sendSafetySummary()), false);
        source.sendSuccess(() -> Component.literal("Use ModMessages.wrapper() when migrating old PacketDispatcher.wrapper call sites; "
                + "use LegacyPacketThreading for old PacketThreading call sites."), false);
        return LegacyNetworkDispatcher.directSendHelperCount()
                + LegacyNetworkDispatcher.threadedSendHelperCount()
                + LegacyNetworkDispatcher.packetThreadingHelperCount();
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
                + " preparable=" + snapshot.preparableMessages()
                + " plain=" + snapshot.nonPreparableMessages()
                + " preparedSame=" + snapshot.preparedSameInstance()
                + " preparedCopies=" + snapshot.preparedCopyInstance()
                + " totalSent=" + snapshot.totalSent()
                + " totalFailed=" + snapshot.totalFailed()
                + " totalDiscarded=" + snapshot.totalDiscarded()
                + " prepareFailed=" + snapshot.totalPrepareFailed()
                + " manualClears=" + snapshot.manualClears()), false);
        source.sendSuccess(() -> Component.literal("Packet threading discard reasons: invalidTarget="
                + snapshot.discardedInvalidTarget()
                + " invalidMessage=" + snapshot.discardedInvalidMessage()
                + " prepareNull=" + snapshot.discardedPrepareNull()
                + " prepareException=" + snapshot.discardedPrepareException()
                + " queueClear=" + snapshot.discardedQueueClear()
                + " disableClear=" + snapshot.discardedDisableClear()
                + " manualClear=" + snapshot.discardedManualClear()
                + " reasonedTotal=" + snapshot.reasonedDiscardTotal()), false);
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
        source.sendSuccess(() -> Component.literal("Legacy PacketThreading facade: "
                + LegacyPacketThreading.compatibilitySummary()), false);
        source.sendSuccess(() -> Component.literal("Legacy ntmpackets info: "
                + LegacyPacketThreading.legacyCommandInfoSummary()), false);
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
                + " playerData=" + ClientHbmPlayerProperties.syncedEntryCount()
                + " permaKeys=" + ClientPermaSyncData.keyCount()
                + " radiationEffects=" + ClientHbmLivingProperties.getContaminationCount()), false);
        source.sendSuccess(() -> Component.literal("Client transient effects: notices="
                + ClientInformMessages.noticeCount()
                + " muzzleFlashes=" + ClientMuzzleFlashEffects.flashCount()), false);
        if (!snapshot.lastFailureMessage().isBlank()) {
            source.sendSuccess(() -> Component.literal("Last packet threading issue: " + snapshot.lastFailureMessage()), false);
        }
        return snapshot.pending();
    }

    private static int getPacketThreadingPrepareStats(CommandSourceStack source) {
        ThreadedPacketDispatcher.Snapshot snapshot = ThreadedPacketDispatcher.snapshot();
        source.sendSuccess(() -> Component.literal("Packet threading prepare: totalPrepared="
                + snapshot.totalPrepared()
                + " prepareFailed=" + snapshot.totalPrepareFailed()
                + " preparableMessages=" + snapshot.preparableMessages()
                + " nonPreparableMessages=" + snapshot.nonPreparableMessages()
                + " sameInstance=" + snapshot.preparedSameInstance()
                + " copyInstance=" + snapshot.preparedCopyInstance()
                + " preparedInstanceTotal=" + snapshot.preparedInstanceTotal()), false);
        source.sendSuccess(() -> Component.literal("Legacy precompile compatibility: "
                + "HbmPreparablePacket replaces ThreadedPacket/PrecompiledPacket; "
                + "non-preparable registered S2C messages may still be threaded but are not pre-copied."), false);
        if (!snapshot.lastFailureMessage().isBlank()) {
            source.sendSuccess(() -> Component.literal("Last packet threading issue: " + snapshot.lastFailureMessage()), false);
        }
        return (int) Math.min(Integer.MAX_VALUE, snapshot.totalPrepared());
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
        LegacyPacketThreading.resetLegacyCounters();
        LegacyNetworkDispatcher.resetLegacyCounters();
        source.sendSuccess(() -> Component.literal("Reset packet threading dispatcher state."), true);
        return 1;
    }

    private static int rejectLegacyPacketThreadingLock(CommandSourceStack source, String command) {
        source.sendFailure(Component.literal("Legacy /ntmpackets " + command
                + " is intentionally unsupported in the modern dispatcher; use clear/reset/toggle instead."));
        return 0;
    }

    private static boolean parseBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equals(value);
    }

    private static FluidType parseFluid(String value) {
        return HbmFluids.fromName(value);
    }

    private static Direction parseDirection(String value) {
        for (Direction direction : Direction.values()) {
            if (direction.getName().equalsIgnoreCase(value)) {
                return direction;
            }
        }
        return null;
    }

    private static String[] directionNames() {
        return Arrays.stream(Direction.values()).map(Direction::getName).toArray(String[]::new);
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
        ASBESTOS("asbestos", player -> HbmLivingProperties.getAsbestos(player), (player, value) -> HbmLivingProperties.setAsbestos(player, value)),
        BLACKLUNG("blacklung", player -> HbmLivingProperties.getBlackLung(player), (player, value) -> HbmLivingProperties.setBlackLung(player, value)),
        BOMB_TIMER("bomb_timer", player -> HbmLivingProperties.getBombTimer(player), (player, value) -> HbmLivingProperties.setBombTimer(player, value)),
        CONTAGION("contagion", player -> HbmLivingProperties.getContagion(player), (player, value) -> HbmLivingProperties.setContagion(player, value)),
        OIL("oil", player -> HbmLivingProperties.getOil(player), (player, value) -> HbmLivingProperties.setOil(player, value)),
        FIRE("fire", player -> HbmLivingProperties.getFire(player), (player, value) -> HbmLivingProperties.setFire(player, value)),
        PHOSPHORUS("phosphorus", player -> HbmLivingProperties.getPhosphorus(player), (player, value) -> HbmLivingProperties.setPhosphorus(player, value)),
        BALEFIRE("balefire", player -> HbmLivingProperties.getBalefire(player), (player, value) -> HbmLivingProperties.setBalefire(player, value)),
        BLACK_FIRE("black_fire", player -> HbmLivingProperties.getBlackFire(player), (player, value) -> HbmLivingProperties.setBlackFire(player, value));

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
