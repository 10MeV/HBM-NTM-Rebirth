package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.ntm.fluid.HbmFluidContainerRegistry;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.item.FoundryMoldItem.Mold;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.compat.CompatRecipeRegistry;
import com.hbm.ntm.recipe.LegacySerializableRecipeHandlers;
import com.hbm.ntm.recipe.BoilerRecipeRuntime;
import com.hbm.ntm.recipe.PWRFuelRuntime;
import com.hbm.ntm.recipe.RtgRecipeRuntime;
import com.hbm.ntm.recipe.WatzFuelRuntime;
import com.hbm.ntm.recipe.ZirnoxFuelRuntime;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LegacyJavaRecipeCoverageProvider implements DataProvider {
    private static final Pattern LEGACY_RECIPE_NAME =
            Pattern.compile("new\\s+(?:GenericRecipe|FusionRecipe|PUREXRecipe|PlasmaForgeRecipe)\\(\"([^\"]+)\"\\)");
    private static final Pattern LEGACY_SERIALIZABLE_RECIPE_CLASS = Pattern.compile(
            "class\\s+([A-Za-z0-9_]+)\\s+extends\\s+(?:[A-Za-z0-9_$.]+\\.)?(?:SerializableRecipe|GenericRecipes)(?:\\b|<)");
    private static final Set<String> MIGRATED_PLASMA_FORGE_RECIPES = Set.of(
            "plsm.plateeuphemium",
            "plsm.platednt",
            "plsm.hde",
            "plsm.weldiron",
            "plsm.weldsteel",
            "plsm.weldcopper",
            "plsm.weldtitanium",
            "plsm.weldzirconium",
            "plsm.weldaluminium",
            "plsm.weldtcalloy",
            "plsm.weldcdalloy",
            "plsm.weldtungsten",
            "plsm.weldcmb",
            "plsm.weldosmiridium",
            "plsm.fusionvessel",
            "plsm.icfcell",
            "plsm.icfemitter",
            "plsm.icfcapacitor",
            "plsm.icfturbo",
            "plsm.icfcasing",
            "plsm.icfport",
            "plsm.icfcontroller",
            "plsm.icfscaffold",
            "plsm.icfvessel",
            "plsm.icfstructural",
            "plsm.icfcore",
            "plsm.icfpress",
            "plsm.dfccore",
            "plsm.dfcemitter",
            "plsm.dfcreceiver",
            "plsm.dfcinjector",
            "plsm.dfcstabilizer");
    private static final Map<String, String> RUNTIME_PAUSED_RECIPE_BLOCKERS = Map.of();
    private static final Map<Integer, String> ANNIHILATOR_528_EXCLUSIONS = Map.ofEntries(
            Map.entry(0, "GeneralConfig.enable528 steel blueprint milestone is excluded by the configuration modernization rule"),
            Map.entry(1, "GeneralConfig.enable528 silicon-chip blueprint milestone is excluded by the configuration modernization rule"),
            Map.entry(2, "GeneralConfig.enable528 bismoid-chip blueprint milestone is excluded by the configuration modernization rule"),
            Map.entry(3, "GeneralConfig.enable528 quantum-chip blueprint milestone is excluded by the configuration modernization rule"),
            Map.entry(4, "GeneralConfig.enable528 gas centrifuge blueprint milestone is excluded by the configuration modernization rule"),
            Map.entry(5, "GeneralConfig.enable528 plastic blueprint milestone is excluded by the configuration modernization rule"),
            Map.entry(6, "GeneralConfig.enable528 rubber blueprint milestone is excluded by the configuration modernization rule"),
            Map.entry(7, "GeneralConfig.enable528 ferrouranium blueprint milestone is excluded by the configuration modernization rule"),
            Map.entry(8, "GeneralConfig.enable528 strontium blueprint milestone is excluded by the configuration modernization rule"),
            Map.entry(9, "GeneralConfig.enable528 hard-plastic blueprint milestone is excluded by the configuration modernization rule"),
            Map.entry(10, "GeneralConfig.enable528 tcalloy blueprint milestone is excluded by the configuration modernization rule"),
            Map.entry(11, "GeneralConfig.enable528 chlorophyte blueprint milestone is excluded by the configuration modernization rule"),
            Map.entry(12, "GeneralConfig.enable528 50BMG blueprint milestone is excluded by the configuration modernization rule"),
            Map.entry(13, "GeneralConfig.enable528 artillery blueprint milestone is excluded by the configuration modernization rule"),
            Map.entry(14, "GeneralConfig.enable528 controller blueprint milestone is excluded by the configuration modernization rule"));
    private static final List<CountRecipeFamily> COUNT_RECIPE_FAMILIES = List.of(
            new CountRecipeFamily("press", "PressRecipes.java", "hbmPress.json", "press", 48),
            new CountRecipeFamily("blast_furnace", "BlastFurnaceRecipesNT.java", "hbmBlastFurnace.json",
                    "blast_furnace", 14),
            new CountRecipeFamily("difurnace", "BlastFurnaceRecipes.java", "hbmBlastFurnaceLegacy.json",
                    "difurnace", 14,
                    "deprecated legacy BlastFurnaceRecipes is used by TileEntityDiFurnace and TileEntityDiFurnaceRTG; source_order 13 is the LBSM oil-canister config branch and stays excluded because recipe config gates are not restored",
                    Map.of(13, "GeneralConfig.enableLBSM && enableLBSMSimpleChemsitry canister_empty + COAL -> oil canister branch is excluded; modern variants belong in datapack overrides")),
            new CountRecipeFamily("shredder", "ShredderRecipes.java", "hbmShredder.json", "shredder", 302),
            new CountRecipeFamily("soldering_station", "SolderingRecipes.java", "hbmSoldering.json",
                    "soldering_station", 26),
            new CountRecipeFamily("combination_oven", "CombinationRecipes.java", "hbmCombination.json",
                    "combination_oven", 23),
            new CountRecipeFamily("centrifuge", "CentrifugeRecipes.java", "hbmCentrifuge.json", "centrifuge",
                    56),
            new CountRecipeFamily("crystallizer", "CrystallizerRecipes.java", "hbmCrystallizer.json",
                    "crystallizer", 107),
            new CountRecipeFamily("refinery", "RefineryRecipes.java", "hbmRefinery.json", "refinery", 4),
            new CountRecipeFamily("vacuum_distill", "VacuumRefineryRecipes.java", "hbmVacRefinery.json",
                    "vacuum_distill", 2),
            new CountRecipeFamily("fraction_tower", "FractionRecipes.java", "hbmFractions.json",
                    "fraction_tower", 19),
            new CountRecipeFamily("catalytic_cracker", "CrackingRecipes.java", "hbmCracking.json",
                    "catalytic_cracker", 12),
            new CountRecipeFamily("catalytic_reformer", "ReformingRecipes.java", "hbmReforming.json",
                    "catalytic_reformer", 9),
            new CountRecipeFamily("hydrotreater", "HydrotreatingRecipes.java", "hbmHydrotreating.json",
                    "hydrotreater", 6),
            new CountRecipeFamily("liquefaction", "LiquefactionRecipes.java", "hbmLiquefactor.json",
                    "liquefaction", 33),
            new CountRecipeFamily("solidifier", "SolidificationRecipes.java", "hbmSolidifier.json",
                    "solidifier", 47),
            new CountRecipeFamily("coker", "CokerRecipes.java", "hbmCoker.json", "coker", 33),
            new CountRecipeFamily("pyro_oven", "PyroOvenRecipes.java", "hbmPyrolysis.json", "pyro_oven",
                    40),
            new CountRecipeFamily("breeding_reactor", "BreederRecipes.java", "hbmBreeder.json", "breeding_reactor",
                    31),
            new CountRecipeFamily("cyclotron", "CyclotronRecipes.java", "hbmCyclotron.json", "cyclotron", 42),
            new CountRecipeFamily("fuel_pool", "FuelPoolRecipes.java", "hbmFuelpool.json", "fuel_pool", 31),
            new CountRecipeFamily("mixer", "MixerRecipes.java", "hbmMixer.json", "mixer", 50),
            new CountRecipeFamily("outgasser", "OutgasserRecipes.java", "hbmIrradiation.json", "outgasser", 19),
            new CountRecipeFamily("exposure_chamber", "ExposureChamberRecipes.java", "hbmExposureChamber.json",
                    "exposure_chamber", 4),
            new CountRecipeFamily("electrolyzer_fluid", "ElectrolyserFluidRecipes.java",
                    "hbmElectrolyzerFluid.json", "electrolyzer_fluid", 8),
            new CountRecipeFamily("electrolyzer_metal", "ElectrolyserMetalRecipes.java",
                    "hbmElectrolyzerMetal.json", "electrolyzer_metal", 18),
            new CountRecipeFamily("fusion_fluid_breeder", "FluidBreederRecipes.java", "hbmIrradiationFluids.json",
                    "fusion_fluid_breeder", 3),
            new CountRecipeFamily("compressor", "CompressorRecipes.java", "hbmCompressor.json", "compressor",
                    5),
            new CountRecipeFamily("arc_welder", "ArcWelderRecipes.java", "hbmArcWelder.json", "arc_welder",
                    47),
            new CountRecipeFamily("rotary_furnace", "RotaryFurnaceRecipes.java", "hbmRotaryFurnace.json",
                    "rotary_furnace", 12),
            new CountRecipeFamily("particle_accelerator", "ParticleAcceleratorRecipes.java",
                    "hbmParticleAccelerator.json", "particle_accelerator", 11),
            new CountRecipeFamily("ammo_press", "AmmoPressRecipes.java", "hbmAmmoPress.json", "ammo_press",
                    89),
            new CountRecipeFamily("annihilator", "AnnihilatorRecipes.java", "hbmAnnihilator.json",
                    "annihilator", 15,
                    "1.7.10 defaults are all gated behind GeneralConfig.enable528; modern shipped defaults remain empty and custom/datapack annihilator recipes are still supported",
                    ANNIHILATOR_528_EXCLUSIONS),
            new CountRecipeFamily("anvil_construction", "inventory/recipes/anvil/AnvilRecipes.java",
                    "hbmAnvil.json", "anvil_construction", 61,
                    "current source-backed construction-only hbmAnvil.json surface; smithing is tracked separately because 1.7.10 hbmAnvil.json serializes constructionRecipes only"),
            new CountRecipeFamily("anvil_smithing", "inventory/recipes/anvil/AnvilRecipes.java", null,
                    "anvil_smithing", 58,
                    "1.7.10 registerSmithing() Java defaults; old hbmAnvil.json does not serialize smithing"),
            new CountRecipeFamily("pedestal", "PedestalRecipes.java", "hbmPedestal.json", "pedestal", 17,
                    "legacy registerDefaults has 17 entries; source orders 10, 11, 12, 15, and 16 depend on item_secret reward chains from red-room or dungeon generation and are excluded by project rule",
                    Map.of(
                            10, "gun_folly requires item_secret FOLLY and CONTROLLER from RedRoom item pools",
                            11, "gun_aberrator requires item_secret ABERRATOR from dungeon/logic-block skeleton rewards",
                            12, "gun_aberrator_eott requires item_secret ABERRATOR from dungeon/logic-block skeleton rewards",
                            15, "ammo_secret_p35_800 requires item_secret ABERRATOR from dungeon/logic-block skeleton rewards",
                            16, "ammo_secret_p35_800_bl requires item_secret ABERRATOR from dungeon/logic-block skeleton rewards")),
            new CountRecipeFamily("crucible", "CrucibleRecipes.java", "hbmCrucible.json", "crucible", 13),
            new CountRecipeFamily("crucible_smelting", "inventory/material/MatDistribution.java",
                    "hbmCrucibleSmelting.json", "crucible_smelting", 45,
                    "MatDistribution fixed item/ore material distributions after final map semantics; the second old Items.minecart registration is equivalent to the earlier minecart entry and is not a distinct final recipe"),
            new CountRecipeFamily("arc_furnace", "ArcFurnaceRecipes.java", "hbmArcFurnace.json",
                    "arc_furnace", 12),
            new CountRecipeFamily("gas_cent", "GasCentrifugeRecipes.java", "hbmGasCentrifuge.json",
                    "gas_cent", 4),
            new CountRecipeFamily("lemegeton", "LemegetonRecipes.java", "hbmLemegeton.json", "lemegeton",
                    37),
            new CountRecipeFamily("magic", "MagicRecipes.java", null, "magic", 7,
                    "old 2x2 book_of_ magic GUI surface is frozen by the guide/manual hard-exclusion; default datapack/crafting recipes must not be published from this table",
                    Map.of(
                            0, "ingot_u238m2 self-combine recipe belongs to the frozen book_of_ carrier and includes excluded special-obtainment ingot_u238m2 content",
                            1, "rod_of_discord recipe belongs to the frozen book_of_ magic GUI carrier",
                            2, "balefire_and_steel recipe belongs to the frozen book_of_ magic GUI carrier",
                            3, "mysteryshovel recipe belongs to the frozen book_of_ magic GUI carrier",
                            4, "ingot_electronium recipe belongs to the frozen book_of_ carrier and electronium acquisition needs a separate source-backed decision",
                            5, "diamond_gavel recipe belongs to the frozen book_of_ magic GUI carrier",
                            6, "mese_gavel recipe belongs to the frozen book_of_ magic GUI carrier")),
            new CountRecipeFamily("radiolysis", "RadiolysisRecipes.java", "hbmRadiolysis.json",
                    "radiolysis", 1),
            new CountRecipeFamily("radgen", "tileentity/machine/TileEntityMachineRadGen.java", null, "radgen", 28,
                    "hardcoded TileEntityMachineRadGen.fuels table; no 1.7.10 SerializableRecipe JSON surface"),
            CountRecipeFamily.displayOnly("tooling", "handler/nei/ToolingHandler.java", 4,
                    "display-only structure tool conversion list from BlockToolConversion; no 1.7.10 SerializableRecipe JSON surface"),
            CountRecipeFamily.displayOnly("construction", "handler/nei/ConstructionHandler.java", 6,
                    "display-only multiblock construction helper list for WATZ, launcher, Soyuz, ICF, and fusion torus rows; no 1.7.10 SerializableRecipe JSON surface"),
            CountRecipeFamily.displayOnly("ashpit", "handler/nei/AshpitHandler.java", 9,
                    "NEIUniversalHandler display-only ash byproduct table built from oven/chimney item and smoke-fluid families; no 1.7.10 SerializableRecipe JSON surface"),
            CountRecipeFamily.displayOnly("sawmill", "handler/nei/SawmillHandler.java", 4,
                    "NEIUniversalHandler display-only sawmill list from TileEntitySawmill.getRecipes(); no 1.7.10 SerializableRecipe JSON surface"),
            CountRecipeFamily.displayOnly("deuterium_tower", "handler/nei/DeuteriumHandler.java", 1,
                    "NEIUniversalHandler display-only water-to-heavy-water recipe shared by extractor and tower; no 1.7.10 SerializableRecipe JSON surface"),
            CountRecipeFamily.displayOnly("ore_slopper", "handler/nei/OreSlopperHandler.java", 1,
                    "NEIUniversalHandler display-only bedrock ore processor row with water + bedrock ore base input and base bedrock ore outputs plus slop; no 1.7.10 SerializableRecipe JSON surface"),
            CountRecipeFamily.displayOnly("rbmk_waste_decay", "handler/nei/RBMKWasteDecayHandler.java", 26,
                    "NEIUniversalHandler display-only nuclear waste short/long decay table; no 1.7.10 SerializableRecipe JSON surface"),
            CountRecipeFamily.displayOnly("satellite_cargo", "handler/nei/SatelliteHandler.java", 2,
                    "display-only satellite miner and lunar miner cargo-pool rows; old ItemPool live/config lifecycle is not restored"),
            CountRecipeFamily.special("rbmk_fuel_disassembly", "crafting/handlers/RBMKFuelCraftingHandler.java",
                    "rbmk/rbmk_fuel_disassembly",
                    "1.7.10 IRecipe special crafting handler materialized as a datapack CustomRecipe JSON"),
            CountRecipeFamily.special("grenade_crafting", "crafting/handlers/GrenadeCraftingHandler.java",
                    "weapon/grenade_crafting",
                    "1.7.10 IRecipe special crafting handler materialized as a datapack CustomRecipe JSON"),
            CountRecipeFamily.special("cargo_shell_crafting", "crafting/handlers/CargoShellCraftingHandler.java",
                    "weapon/cargo_shell_crafting",
                    "1.7.10 IRecipe special crafting handler materialized as a datapack CustomRecipe JSON"),
            CountRecipeFamily.special("scraps_split", "crafting/handlers/ScrapsCraftingHandler.java",
                    "parts/scraps_split",
                    "1.7.10 IRecipe special crafting handler materialized as a datapack CustomRecipe JSON"),
            CountRecipeFamily.specialMulti("container_upgrade_crafting",
                    "crafting/handlers/ContainerUpgradeCraftingHandler.java",
                    List.of("blocks/crate_desh", "blocks/crate_tungsten", "blocks/safe",
                            "blocks/mass_storage_desh", "blocks/mass_storage_tungsten"),
                    "1.7.10 ShapedOreRecipe subclass materialized as five NBT-preserving container upgrade JSONs; initial mass_storage meta 0 remains an ordinary shaped recipe"),
            CountRecipeFamily.excluded("mku_crafting", "crafting/handlers/MKUCraftingHandler.java",
                    "world-seeded MKU recipe and its lore-book hints are tied to the globally excluded flame_pony / rainbow pony painting chain"),
            new CountRecipeFamily("silex", "SILEXRecipes.java", null, "silex", 325,
                    "hardcoded Java table only; 1.7.10 SILEXRecipes does not extend SerializableRecipe and has no getFileName/readRecipe/writeRecipe JSON surface"),
            new CountRecipeFamily("precass", "PrecAssRecipes.java", "hbmPrecisionAssembly.json", "precass",
                    2));
    private static final List<DynamicDisplaySurface> DYNAMIC_DISPLAY_SURFACES = List.of(
            new DynamicDisplaySurface("boiler", "handler/nei/BoilingHandler.java", "ntmBoiling",
                    "BoilerRecipeRuntime.displayRecipes()",
                    () -> BoilerRecipeRuntime.displayRecipes().size(),
                    "dynamic NEI/list surface generated from Fluids.getInNiceOrder() heatable traits with positive BOILER efficiency"),
            new DynamicDisplaySurface("rtg", "handler/nei/RTGRecipeHandler.java", "ntmRTG",
                    "RtgRecipeRuntime.displayRecipes()",
                    () -> RtgRecipeRuntime.displayRecipes().size(),
                    "dynamic NEI/list surface generated from ItemRTGPellet.getRecipeMap() / accepted RTG pellet specs"),
            new DynamicDisplaySurface("pwr", "handler/nei/PWRRecipeHandler.java", "ntmPWR",
                    "PWRFuelRuntime.displayFuels()",
                    () -> PWRFuelRuntime.displayFuels().size(),
                    "runtime NEI/list surface generated from EnumPWRFuel split fuel items; displays PWR fuel to hot fuel like the 1.7.10 handler",
                    "runtime_list"),
            new DynamicDisplaySurface("zirnox", "handler/nei/ZirnoxRecipeHandler.java", "ntmZirnox",
                    "ZirnoxFuelRuntime.displayRods()",
                    () -> ZirnoxFuelRuntime.displayRods().size(),
                    "runtime NEI/list surface generated from TileEntityReactorZirnox.fuelMap equivalent product mappings",
                    "runtime_list"),
            new DynamicDisplaySurface("watz", "handler/nei/WatzRecipeHandler.java", "ntmWatz",
                    "WatzFuelRuntime.displayPellets()",
                    () -> WatzFuelRuntime.displayPellets().size(),
                    "runtime NEI/list surface generated from EnumWatzType split pellet items; displays active to depleted pellets like the 1.7.10 handler",
                    "runtime_list"),
            new DynamicDisplaySurface("fluid_containers", "handler/nei/FluidRecipeHandler.java", "fluidcons",
                    "HbmFluidContainerRegistry.getAllContainers()",
                    LegacyJavaRecipeCoverageProvider::countFluidContainerDisplayRecipes,
                    "dynamic NEI/list surface generated from MachineRecipes#getFluidContainers() / fluid container registry"),
            new DynamicDisplaySurface("crucible_casting", "handler/nei/CrucibleCastingHandler.java",
                    "ntmCrucibleFoundry",
                    "CrucibleCastingRecipeCategory.recipes()",
                    LegacyJavaRecipeCoverageProvider::countCrucibleCastingDisplayRecipes,
                    "dynamic NEI/list surface generated from CrucibleRecipes#getMoldRecipes() using smeltable materials and source-backed foundry molds",
                    "dynamic_material_mold_registry"));
    private static final List<NeiRegistrationSurface> NEI_REGISTRATION_SURFACES = List.of(
            new NeiRegistrationSurface("anvil_overlay_focus", "handler/nei/AnvilOverlayHandler.java",
                    "main/NEIConfig.java", "AnvilConstructionRecipeTransferHandler + AnvilScreen.focusRecipe(...)",
                    "modern_transfer_focus_bridge",
                    "1.7.10 registers this outside NEIRegistry#listAllHandlers(); selecting an AnvilRecipeHandler row in GUIAnvil focuses that construction recipe without crafting it"),
            new NeiRegistrationSurface("custom_machine_nei", "handler/nei/CustomMachineHandler.java",
                    "main/NEIConfig.java", "",
                    "excluded",
                    "registered per CustomMachineConfigJSON.niceList in 1.7.10, but the whole custom-machine feature family is hard-excluded by project rule"),
            new NeiRegistrationSurface("book_magic_nei", "handler/nei/BookRecipeHandler.java",
                    "main/NEIRegistry.java",
                    "magic recipe family excluded by MagicRecipes.java / frozen book_of_ GUI carrier",
                    "excluded",
                    "registered in 1.7.10 NEIRegistry#listAllHandlers(); it displays MagicRecipes#getRecipes() through the book_of_ GUI/GUIBook surface, and all seven legacy magic entries are already excluded by the frozen book/manual carrier and ingot_u238m2/source-acquisition exclusions"),
            new NeiRegistrationSurface("alloy_furnace_unregistered_handler",
                    "handler/nei/AlloyFurnaceRecipeHandler.java", "",
                    "difurnace coverage via BlastFurnaceRecipes.java / ModRecipes.DIFURNACE",
                    "unregistered_legacy_handler",
                    "legacy class exists but is not registered by NEIConfig or NEIRegistry#listAllHandlers(); the active DiFurnace table is already tracked as difurnace"),
            new NeiRegistrationSurface("modern_only_radgen_jei",
                    "tileentity/machine/TileEntityMachineRadGen.java", "main/NEIRegistry.java",
                    "RadGenRecipeCategory / RadGenRecipeRuntime.displayRecipes(...)",
                    "modern_only_no_legacy_nei_handler",
                    "1.7.10 has TileEntityMachineRadGen.fuels but no RadGenRecipeHandler in NEIRegistry#listAllHandlers(); the default fuel table is tracked as the radgen count-family"),
            new NeiRegistrationSurface("modern_only_wood_burner_jei",
                    "tileentity/machine/TileEntityMachineWoodBurner.java", "main/NEIRegistry.java",
                    "WoodBurnerRecipeCategory / WoodBurnerRecipeRuntime.displayRecipes()",
                    "modern_only_no_legacy_nei_handler",
                    "1.7.10 has wood-burner runtime fuel behavior but no WoodBurner NEI handler registered by NEIRegistry#listAllHandlers()"),
            new NeiRegistrationSurface("modern_only_turbofan_jei",
                    "tileentity/machine/TileEntityMachineTurbofan.java", "main/NEIRegistry.java",
                    "TurbofanFuelRecipeCategory / TurbofanRecipeRuntime.displayRecipes()",
                    "modern_only_no_legacy_nei_handler",
                    "1.7.10 has turbofan runtime fuel behavior but no Turbofan NEI handler registered by NEIRegistry#listAllHandlers()"),
            new NeiRegistrationSurface("modern_only_turbine_gas_jei",
                    "tileentity/machine/TileEntityMachineTurbineGas.java", "main/NEIRegistry.java",
                    "TurbineGasFuelRecipeCategory / TurbineGasRecipeRuntime.displayRecipes()",
                    "modern_only_no_legacy_nei_handler",
                    "1.7.10 has gas-turbine runtime fuel behavior but no TurbineGas NEI handler registered by NEIRegistry#listAllHandlers()"),
            new NeiRegistrationSurface("modern_only_research_reactor_fuel_jei",
                    "tileentity/machine/TileEntityReactorResearch.java", "main/NEIRegistry.java",
                    "ResearchReactorFuelRecipeCategory / ResearchReactorFuelRuntime.displayFuels()",
                    "modern_only_no_legacy_nei_handler",
                    "1.7.10 has research-reactor fuel runtime behavior but no ResearchReactor fuel NEI handler registered by NEIRegistry#listAllHandlers()"),
            new NeiRegistrationSurface("modern_only_icf_pellet_jei",
                    "tileentity/machine/TileEntityICFPress.java", "main/NEIRegistry.java",
                    "ICFPelletRecipeCategory / ICFPelletRecipeRuntime.displayPellets()",
                    "modern_only_no_legacy_nei_handler",
                    "1.7.10 has ICF press and ItemICFPellet fuel-combination runtime behavior but no ICF pellet NEI handler registered by NEIRegistry#listAllHandlers()"));
    private static final List<LegacyNeiHandlerSurface> LEGACY_NEI_HANDLER_SURFACES = List.of(
            registeredHandler("AmmoPressHandler.java", "ammo_press",
                    "AmmoPressRecipeCategory / ModRecipes.AMMO_PRESS",
                    "old NEIRegistry list handler is covered by the ammo_press datapack recipe family"),
            registeredHandler("AnnihilatorHandler.java", "annihilator",
                    "AnnihilatorRecipeCategory / ModRecipes.ANNIHILATOR",
                    "old 528-gated annihilator milestones remain excluded, while the supported datapack surface is covered by the annihilator family"),
            registeredHandler("AnvilRecipeHandler.java", "anvil_construction",
                    "AnvilConstructionRecipeCategory / ModRecipes.ANVIL_CONSTRUCTION",
                    "old construction-recipe table is covered by the anvil_construction family"),
            registeredHandler("ArcFurnaceFluidHandler.java", "arc_furnace",
                    "ItemProcessingRecipeCategory arc_furnace fluid display / ModRecipes.ARC_FURNACE",
                    "old arc furnace fluid handler is covered by the arc_furnace datapack recipe family"),
            registeredHandler("ArcFurnaceSolidHandler.java", "arc_furnace",
                    "ItemProcessingRecipeCategory arc_furnace solid display / ModRecipes.ARC_FURNACE",
                    "old arc furnace solid handler is covered by the arc_furnace datapack recipe family"),
            registeredHandler("ArcWelderHandler.java", "arc_welder",
                    "ItemProcessingRecipeCategory arc_welder / ModRecipes.ARC_WELDER",
                    "old universal arc welder handler is covered by the arc_welder datapack recipe family"),
            registeredHandler("AssemblyMachineRecipeHandler.java", "assembly_machine",
                    "HbmMachineRecipeCategory / GenericMachineRecipe.Machine.ASSEMBLY_MACHINE",
                    "old generic NEI handler is covered by the assembly_machine machine surface"),
            registeredDisplayHandler("AshpitHandler.java", "ashpit", "AshpitRecipeCategory",
                    "old display-only ash byproduct list is tracked by the ashpit display family"),
            registeredHandler("BlastFurnaceHandler.java", "blast_furnace",
                    "BlastFurnaceRecipeCategory / ModRecipes.BLAST_FURNACE",
                    "old generic blast furnace handler is covered by the blast_furnace datapack recipe family"),
            registeredDynamicHandler("BoilingHandler.java", "boiler", "BoilerRecipeCategory",
                    "old fluid-trait boiler list is dynamic and tracked under dynamic_display_surfaces"),
            registeredExclusionHandler("BookRecipeHandler.java", "magic",
                    "magic recipe family excluded by frozen book_of_ GUI carrier",
                    "old book magic NEI table belongs to the frozen guide/manual/book GUI carrier and excluded item chains"),
            registeredHandler("BreederRecipeHandler.java", "breeding_reactor",
                    "BreedingReactorRecipeCategory / ModRecipes.BREEDING_REACTOR",
                    "old breeding reactor handler is covered by the breeding_reactor datapack recipe family"),
            registeredHandler("CentrifugeRecipeHandler.java", "centrifuge",
                    "ItemProcessingRecipeCategory centrifuge / ModRecipes.CENTRIFUGE",
                    "old universal centrifuge handler is covered by the centrifuge datapack recipe family"),
            registeredHandler("ChemicalPlantRecipeHandler.java", "chemical_plant",
                    "HbmMachineRecipeCategory / GenericMachineRecipe.Machine.CHEMICAL_PLANT",
                    "old generic chemical plant handler is covered by the chemical_plant machine surface"),
            registeredHandler("CokingHandler.java", "coker",
                    "PyroOvenRecipeCategory coker / ModRecipes.COKER",
                    "old universal coking handler is covered by the coker datapack recipe family"),
            registeredHandler("CombinationHandler.java", "combination_oven",
                    "CombinationOvenRecipeCategory / ModRecipes.COMBINATION_OVEN",
                    "old universal combination oven handler is covered by the combination_oven datapack recipe family"),
            registeredHandler("CompressorHandler.java", "compressor",
                    "CompressorRecipeCategory / ModRecipes.COMPRESSOR",
                    "old universal compressor handler is covered by the compressor datapack recipe family"),
            registeredDisplayHandler("ConstructionHandler.java", "construction",
                    "ConstructionRecipeCategory",
                    "old display-only multiblock construction list is tracked by the construction display family"),
            registeredHandler("CrackingHandler.java", "catalytic_cracker",
                    "HbmOilRecipeCategory catalytic_cracker / ModRecipes.CATALYTIC_CRACKER",
                    "old universal catalytic cracker handler is covered by the catalytic_cracker datapack recipe family"),
            registeredHandler("CrucibleAlloyingHandler.java", "crucible",
                    "CrucibleRecipeCategory / ModRecipes.CRUCIBLE",
                    "old crucible alloying handler is covered by the crucible datapack recipe family"),
            registeredDynamicHandler("CrucibleCastingHandler.java", "crucible_casting",
                    "CrucibleCastingRecipeCategory",
                    "old foundry mold list is dynamic material/mold display data and tracked under dynamic_display_surfaces"),
            registeredHandler("CrucibleSmeltingHandler.java", "crucible_smelting",
                    "CrucibleSmeltingRecipeCategory / ModRecipes.CRUCIBLE_SMELTING",
                    "old crucible smelting handler is covered by the crucible_smelting material distribution family"),
            registeredHandler("CrystallizerRecipeHandler.java", "crystallizer",
                    "ItemProcessingRecipeCategory crystallizer / ModRecipes.CRYSTALLIZER",
                    "old universal crystallizer handler is covered by the crystallizer datapack recipe family"),
            registeredHandler("CyclotronRecipeHandler.java", "cyclotron",
                    "CyclotronRecipeCategory / ModRecipes.CYCLOTRON",
                    "old dedicated cyclotron handler is covered by the cyclotron datapack recipe family"),
            registeredDisplayHandler("DeuteriumHandler.java", "deuterium_tower",
                    "DeuteriumTowerRecipeCategory",
                    "old display-only deuterium water conversion row is tracked by the deuterium_tower display family"),
            registeredHandler("ElectrolyserFluidHandler.java", "electrolyzer_fluid",
                    "ElectrolyserRecipeCategory fluid / ModRecipes.ELECTROLYZER_FLUID",
                    "old universal fluid electrolysis handler is covered by the electrolyzer_fluid datapack recipe family"),
            registeredHandler("ElectrolyserMetalHandler.java", "electrolyzer_metal",
                    "ElectrolyserRecipeCategory metal / ModRecipes.ELECTROLYZER_METAL",
                    "old universal metal electrolysis handler is covered by the electrolyzer_metal datapack recipe family"),
            registeredHandler("ExposureChamberHandler.java", "exposure_chamber",
                    "ExposureChamberRecipeCategory / ModRecipes.EXPOSURE_CHAMBER",
                    "old universal exposure chamber handler is covered by the exposure_chamber datapack recipe family"),
            registeredDynamicHandler("FluidRecipeHandler.java", "fluid_containers",
                    "FluidContainerRecipeCategory / HbmFluidContainerRegistry",
                    "old final fluid container NEI handler is dynamic display data tracked under dynamic_display_surfaces"),
            registeredHandler("FractioningHandler.java", "fraction_tower",
                    "HbmOilRecipeCategory fraction_tower / ModRecipes.FRACTION_TOWER",
                    "old universal fraction tower handler is covered by the fraction_tower datapack recipe family"),
            registeredHandler("FuelPoolHandler.java", "fuel_pool",
                    "FuelPoolRecipeCategory / ModRecipes.FUEL_POOL",
                    "old spent fuel pool handler is covered by the fuel_pool datapack recipe family"),
            registeredHandler("FusionBreederHandler.java", "fusion_fluid_breeder",
                    "FusionFluidBreederRecipeCategory / ModRecipes.FUSION_FLUID_BREEDER",
                    "old universal fusion breeder handler is covered by the fusion_fluid_breeder datapack recipe family"),
            registeredHandler("FusionRecipeHandler.java", "fusion_reactor",
                    "HbmMachineRecipeCategory / GenericMachineRecipe.Machine.FUSION_REACTOR",
                    "old generic fusion handler is covered by the fusion_reactor machine surface"),
            registeredHandler("GasCentrifugeRecipeHandler.java", "gas_cent",
                    "GasCentRecipeCategory / ModRecipes.GAS_CENT",
                    "old gas centrifuge handler is covered by the gas_cent datapack recipe family"),
            registeredHandler("GrenadeRecipeHandler.java", "grenade_crafting",
                    "GrenadeRecipeCategory + grenade component datapack recipes",
                    "old universal grenade handler is covered by the grenade special crafting and component recipe surfaces"),
            registeredHandler("HydrotreatingHandler.java", "hydrotreater",
                    "HbmOilRecipeCategory hydrotreater / ModRecipes.HYDROTREATER",
                    "old universal hydrotreater handler is covered by the hydrotreater datapack recipe family"),
            registeredHandler("LiquefactionHandler.java", "liquefaction",
                    "LiquefactionRecipeCategory / ModRecipes.LIQUEFACTION",
                    "old universal liquefaction handler is covered by the liquefaction datapack recipe family"),
            registeredHandler("MixerHandler.java", "mixer",
                    "MixerRecipeCategory / ModRecipes.MIXER",
                    "old universal mixer handler is covered by the mixer datapack recipe family"),
            registeredHandler("OutgasserHandler.java", "outgasser",
                    "OutgasserRecipeCategory / ModRecipes.OUTGASSER",
                    "old universal outgasser handler is covered by the outgasser datapack recipe family"),
            registeredDisplayHandler("OreSlopperHandler.java", "ore_slopper",
                    "OreSlopperRecipeCategory",
                    "old display-only ore slopper row is tracked by the ore_slopper display family"),
            registeredHandler("ParticleAcceleratorHandler.java", "particle_accelerator",
                    "ParticleAcceleratorRecipeCategory / ModRecipes.PARTICLE_ACCELERATOR",
                    "old universal particle accelerator handler is covered by the particle_accelerator datapack recipe family"),
            registeredHandler("PlasmaForgeRecipeHandler.java", "plasma_forge_reliable_scope",
                    "HbmMachineRecipeCategory / GenericMachineRecipe.Machine.PLASMA_FORGE",
                    "old generic plasma forge handler is covered by the plasma_forge reliable machine surface"),
            registeredHandler("PrecAssRecipeHandler.java", "precass",
                    "HbmMachineRecipeCategory / GenericMachineRecipe.Machine.PRECISION_ASSEMBLY",
                    "old generic precision assembly handler is covered by the precass recipe family"),
            registeredHandler("PressRecipeHandler.java", "press",
                    "PressRecipeCategory / ModRecipes.PRESS",
                    "old dedicated press handler is covered by the press datapack recipe family"),
            registeredHandler("PUREXRecipeHandler.java", "purex",
                    "HbmMachineRecipeCategory / GenericMachineRecipe.Machine.PUREX",
                    "old generic PUREX handler is covered by the purex machine surface"),
            registeredDynamicHandler("PWRRecipeHandler.java", "pwr",
                    "PWRFuelRecipeCategory / PWRFuelRuntime.displayFuels()",
                    "old runtime PWR fuel list is tracked under dynamic_display_surfaces"),
            registeredHandler("PyroHandler.java", "pyro_oven",
                    "PyroOvenRecipeCategory / ModRecipes.PYRO_OVEN",
                    "old universal pyrolysis handler is covered by the pyro_oven datapack recipe family"),
            registeredHandler("RadiolysisRecipeHandler.java", "radiolysis",
                    "RadiolysisRecipeCategory / ModRecipes.RADIOLYSIS",
                    "old dedicated radiolysis handler is covered by the radiolysis datapack recipe family"),
            registeredHandler("RBMKRodDisassemblyHandler.java", "rbmk_fuel_disassembly",
                    "RBMKFuelDisassemblyRecipeCategory / rbmk_fuel_disassembly CustomRecipe",
                    "old display handler mirrors the source-backed RBMK fuel disassembly special crafting surface"),
            registeredDisplayHandler("RBMKWasteDecayHandler.java", "rbmk_waste_decay",
                    "RBMKWasteDecayRecipeCategory",
                    "old display-only RBMK waste decay table is tracked by the rbmk_waste_decay display family"),
            registeredHandler("RefineryRecipeHandler.java", "refinery",
                    "HbmOilRecipeCategory refinery / ModRecipes.REFINERY",
                    "old dedicated refinery handler is covered by the refinery datapack recipe family"),
            registeredHandler("ReformingHandler.java", "catalytic_reformer",
                    "HbmOilRecipeCategory catalytic_reformer / ModRecipes.CATALYTIC_REFORMER",
                    "old universal catalytic reformer handler is covered by the catalytic_reformer datapack recipe family"),
            registeredHandler("RotaryFurnaceHandler.java", "rotary_furnace",
                    "RotaryFurnaceRecipeCategory / ModRecipes.ROTARY_FURNACE",
                    "old universal rotary furnace handler is covered by the rotary_furnace datapack recipe family"),
            registeredDynamicHandler("RTGRecipeHandler.java", "rtg",
                    "RtgFuelRecipeCategory / RtgRecipeRuntime.displayRecipes()",
                    "old conditional RTG NEI handler is dynamic display data tracked under dynamic_display_surfaces"),
            registeredDisplayHandler("SatelliteHandler.java", "satellite_cargo",
                    "SatelliteCargoRecipeCategory",
                    "old satellite cargo display rows are tracked by the satellite_cargo display family"),
            registeredDisplayHandler("SawmillHandler.java", "sawmill",
                    "SawmillRecipeCategory",
                    "old sawmill display rows are tracked by the sawmill display family"),
            registeredHandler("ShredderRecipeHandler.java", "shredder",
                    "ItemProcessingRecipeCategory shredder / ModRecipes.SHREDDER",
                    "old dedicated shredder handler is covered by the shredder datapack recipe family"),
            registeredHandler("SILEXRecipeHandler.java", "silex",
                    "SilexRecipeCategory / ModRecipes.SILEX",
                    "old dedicated SILEX handler is covered by the silex datapack recipe family"),
            registeredHandler("SmithingRecipeHandler.java", "anvil_smithing",
                    "AnvilSmithingRecipeCategory / ModRecipes.ANVIL_SMITHING",
                    "old smithing table is covered by the anvil_smithing datapack recipe family"),
            registeredHandler("SolderingStationHandler.java", "soldering_station",
                    "SolderingStationRecipeCategory / ModRecipes.SOLDERING_STATION",
                    "old universal soldering handler is covered by the soldering_station datapack recipe family"),
            registeredHandler("SolidificationHandler.java", "solidifier",
                    "LiquefactionRecipeCategory solidifier / ModRecipes.SOLIDIFIER",
                    "old universal solidification handler is covered by the solidifier datapack recipe family"),
            registeredDisplayHandler("ToolingHandler.java", "tooling",
                    "ToolingRecipeCategory",
                    "old structure tool conversion list is tracked by the tooling display family"),
            registeredDynamicHandler("WatzRecipeHandler.java", "watz",
                    "WatzFuelRecipeCategory / WatzFuelRuntime.displayPellets()",
                    "old runtime Watz pellet list is tracked under dynamic_display_surfaces"),
            registeredDynamicHandler("ZirnoxRecipeHandler.java", "zirnox",
                    "ZirnoxFuelRecipeCategory / ZirnoxFuelRuntime.displayRods()",
                    "old runtime ZIRNOX rod list is tracked under dynamic_display_surfaces"),
            registeredHandler("VacuumRecipeHandler.java", "vacuum_distill",
                    "HbmOilRecipeCategory vacuum_distill / ModRecipes.VACUUM_DISTILL",
                    "old universal vacuum refinery handler is covered by the vacuum_distill datapack recipe family"),
            new LegacyNeiHandlerSurface("AlloyFurnaceRecipeHandler.java", "", "difurnace",
                    "BlastFurnaceRecipeCategory / ModRecipes.DIFURNACE",
                    "source_only_unregistered",
                    "legacy class exists but NEIConfig and NEIRegistry#listAllHandlers() do not register it; active DiFurnace coverage stays under difurnace"),
            new LegacyNeiHandlerSurface("AnvilOverlayHandler.java", "main/NEIConfig.java",
                    "anvil_overlay_focus", "AnvilConstructionRecipeTransferHandler + AnvilScreen.focusRecipe(...)",
                    "nei_config_bridge_covered",
                    "legacy GUI overlay focus bridge is not a recipe table and is covered by the modern transfer/focus bridge"),
            new LegacyNeiHandlerSurface("CustomMachineHandler.java", "main/NEIConfig.java",
                    "custom_machine_nei", "",
                    "nei_config_excluded",
                    "legacy per-custom-machine NEI table belongs to the hard-excluded custom-machine feature family"),
            new LegacyNeiHandlerSurface("NEIGenericRecipeHandler.java", "", "generic_machine_layout",
                    "HbmMachineRecipeCategory + LegacyGenericRecipeFormat",
                    "abstract_base_facade_covered",
                    "legacy abstract generic NEI base is covered by the modern generic machine JEI/list layout and recipe format bridge"),
            new LegacyNeiHandlerSurface("NEIUniversalHandler.java", "", "legacy_universal_layout",
                    "LegacyNeiUniversalLayout",
                    "abstract_base_facade_covered",
                    "legacy abstract universal NEI base is covered by the shared modern LegacyNeiUniversalLayout"));
    private static final List<CraftingRegistrationSurface> SPECIAL_CRAFTING_SURFACES = List.of(
            new CraftingRegistrationSurface("rbmk_fuel_disassembly",
                    "crafting/handlers/RBMKFuelCraftingHandler.java", "main/CraftingManager.java",
                    "rbmk/rbmk_fuel_disassembly",
                    "materialized_datapack_custom_recipe",
                    "registered by CraftingManager.mainRegistry() and materialized as a datapack CustomRecipe JSON"),
            new CraftingRegistrationSurface("mku_crafting", "crafting/handlers/MKUCraftingHandler.java",
                    "main/CraftingManager.java", "",
                    "excluded",
                    "registered by 1.7.10, but the world-seeded MKU recipe and lore-book hints are tied to the globally excluded flame_pony / rainbow pony painting chain"),
            new CraftingRegistrationSurface("cargo_shell_crafting",
                    "crafting/handlers/CargoShellCraftingHandler.java", "main/CraftingManager.java",
                    "weapon/cargo_shell_crafting",
                    "materialized_datapack_custom_recipe",
                    "registered by CraftingManager.mainRegistry() and materialized as a datapack CustomRecipe JSON"),
            new CraftingRegistrationSurface("scraps_split", "crafting/handlers/ScrapsCraftingHandler.java",
                    "main/CraftingManager.java", "parts/scraps_split",
                    "materialized_datapack_custom_recipe",
                    "registered by CraftingManager.mainRegistry() and materialized as a datapack CustomRecipe JSON"),
            new CraftingRegistrationSurface("grenade_crafting",
                    "crafting/handlers/GrenadeCraftingHandler.java", "main/CraftingManager.java",
                    "weapon/grenade_crafting",
                    "materialized_datapack_custom_recipe",
                    "registered by CraftingManager.mainRegistry() and materialized as a datapack CustomRecipe JSON"),
            new CraftingRegistrationSurface("container_upgrade_crafting",
                    "crafting/handlers/ContainerUpgradeCraftingHandler.java", "main/CraftingManager.java",
                    "blocks/crate_desh, blocks/crate_tungsten, blocks/safe, blocks/mass_storage_desh, blocks/mass_storage_tungsten",
                    "materialized_datapack_special_shaped_recipes",
                    "registered by CraftingManager.AddCraftingRec() for five NBT-preserving upgrade recipes"),
            new CraftingRegistrationSurface("test_crafting_handler",
                    "crafting/handlers/TestCraftingHandler.java", "", "",
                    "source_only_unregistered",
                    "legacy IRecipe class exists in source, but a full-source search finds no new TestCraftingHandler(...) registration or call site"),
            new CraftingRegistrationSurface("shaped_ore_recipe_ext",
                    "crafting/handlers/ShapedOreRecipeExt.java", "", "",
                    "source_only_unregistered_helper",
                    "legacy ShapedOreRecipe subclass exists only as a helper class, and a full-source search finds no constructor call site"));
    private static final List<RecipeSourceSurface> RECIPE_SOURCE_SURFACES = List.of(
            new RecipeSourceSurface("material_shapes", "inventory/material/MaterialShapes.java",
                    "com.hbm.inventory.material.MaterialShapes + Mats material bridge; HbmRecipeProvider material autogen; FoundryMoldItem mold output mapping",
                    "material_domain_helper",
                    "legacy material shape definitions are domain data used by MatDistribution, ArcFurnace material autogen, and Crucible casting; they are not standalone default recipe tables"),
            new RecipeSourceSurface("material_registry", "inventory/material/Mats.java",
                    "com.hbm.inventory.material.Mats + NTMMaterial; HbmRecipeProvider arc_furnace/material and crucible_smelting sources",
                    "material_domain_helper",
                    "legacy material registry is retained as the source-backed material domain for recipe generation and display, not as a separate JSON recipe handler"),
            new RecipeSourceSurface("ntm_material", "inventory/material/NTMMaterial.java",
                    "com.hbm.inventory.material.NTMMaterial",
                    "material_domain_helper",
                    "legacy material value object backs Mats and material-stack recipe generation; no standalone recipe registration surface exists"),
            new RecipeSourceSurface("anvil_smithing_recipe_base",
                    "inventory/recipes/anvil/AnvilSmithingRecipe.java",
                    "com.hbm.ntm.recipe.AnvilSmithingRecipe + AnvilSmithingRecipeRuntime + HbmRecipeProvider#legacyAnvilSmithingRecipes",
                    "support_class_covered_by_family",
                    "base smithing DTO for AnvilRecipes.registerSmithing(); the 58 source-backed smithing defaults are tracked by the anvil_smithing family"),
            new RecipeSourceSurface("anvil_smithing_hot", "inventory/recipes/anvil/AnvilSmithingHotRecipe.java",
                    "com.hbm.ntm.recipe.AnvilSmithingRecipe.Kind.HOT",
                    "support_class_covered_by_family",
                    "hot-output variant used only from AnvilRecipes.registerSmithing(); covered by the anvil_smithing datapack family"),
            new RecipeSourceSurface("anvil_smithing_cyanide",
                    "inventory/recipes/anvil/AnvilSmithingCyanideRecipe.java",
                    "com.hbm.ntm.recipe.AnvilSmithingRecipe.Kind.CYANIDE",
                    "support_class_covered_by_family",
                    "cyanide/red-pill variant used only from AnvilRecipes.registerSmithing(); covered by the anvil_smithing datapack family and runtime tags"),
            new RecipeSourceSurface("anvil_smithing_mold", "inventory/recipes/anvil/AnvilSmithingMold.java",
                    "com.hbm.ntm.recipe.AnvilSmithingRecipe.Kind.MOLD_PREFIX / MOLD_EXACT",
                    "support_class_covered_by_family",
                    "mold variant used only from AnvilRecipes.registerSmithing(); covered by the anvil_smithing datapack family"),
            new RecipeSourceSurface("anvil_smithing_rename",
                    "inventory/recipes/anvil/AnvilSmithingRenameRecipe.java",
                    "com.hbm.ntm.recipe.AnvilSmithingRecipe.Kind.RENAME",
                    "support_class_covered_by_family",
                    "rename variant used only from AnvilRecipes.registerSmithing(); covered by the anvil_smithing datapack family"),
            new RecipeSourceSurface("blast_furnace_recipe_dto", "inventory/recipes/BlastFurnaceRecipe.java",
                    "com.hbm.ntm.recipe.BlastFurnaceRecipe + LegacyBlastFurnaceRecipeImportProvider/ExportProvider",
                    "support_class_covered_by_family",
                    "legacy GenericRecipe subclass DTO used by BlastFurnaceRecipesNT; defaults are tracked by the blast_furnace family"),
            new RecipeSourceSurface("crucible_recipe_dto", "inventory/recipes/CrucibleRecipe.java",
                    "com.hbm.ntm.recipe.CrucibleRecipe + CrucibleRecipeRuntime + LegacyCrucibleRecipeImportProvider/ExportProvider",
                    "support_class_covered_by_family",
                    "legacy GenericRecipe subclass DTO used by CrucibleRecipes; defaults are tracked by the crucible family"),
            new RecipeSourceSurface("custom_machine_recipes", "inventory/recipes/CustomMachineRecipes.java",
                    "LegacySerializableRecipeHandlers unsupported metadata",
                    "excluded",
                    "legacy custom-machine recipe templates belong to the custom-machine feature family and are hard-excluded by project rule"),
            new RecipeSourceSurface("fusion_recipe_dto", "inventory/recipes/FusionRecipe.java",
                    "com.hbm.ntm.recipe.GenericMachineRecipe.Machine.FUSION_REACTOR + LegacyGenericRecipeImporter/ExportProvider",
                    "support_class_covered_by_family",
                    "legacy GenericRecipe subclass DTO used by FusionRecipes; defaults are tracked by the fusion_reactor machine surface"),
            new RecipeSourceSurface("fusion_recipes_legacy", "inventory/recipes/FusionRecipesLegacy.java",
                    "",
                    "source_only_unreferenced",
                    "legacy class defines plasma byproduct helper maps, but a full 1.7.10 source search finds no call sites outside the class itself"),
            new RecipeSourceSurface("generic_recipe_loader_dto", "inventory/recipes/loader/GenericRecipe.java",
                    "com.hbm.inventory.recipes.loader.GenericRecipe + com.hbm.ntm.recipe.LegacyGenericRecipeFormat",
                    "legacy_loader_facade_covered",
                    "legacy loader DTO/API surface is restored for old-format tooling; shipped defaults remain datapack JSON"),
            new RecipeSourceSurface("generic_recipes_loader", "inventory/recipes/loader/GenericRecipes.java",
                    "com.hbm.inventory.recipes.loader.GenericRecipes + LegacyGenericRecipeImporter/ExportProvider",
                    "legacy_loader_facade_covered",
                    "legacy generic handler facade is restored for old-format import/export and display metadata, not as a live static recipe map"),
            new RecipeSourceSurface("serializable_recipe_loader",
                    "inventory/recipes/loader/SerializableRecipe.java",
                    "com.hbm.inventory.recipes.loader.SerializableRecipe + LegacySerializableRecipeHandlers metadata",
                    "legacy_loader_facade_covered",
                    "legacy serialization helper facade is restored while the old config-directory lifecycle remains intentionally disabled"),
            new RecipeSourceSurface("machine_recipes_helper", "inventory/recipes/MachineRecipes.java",
                    "com.hbm.inventory.recipes.MachineRecipes + HbmFluidContainerRegistry + HbmLegacyBatteryMaps",
                    "display_helper_facade_covered",
                    "legacy helper lists back alloy fuel, battery display, ore-dict checks, and fluid-container NEI/list surfaces; fluid containers are also tracked under dynamic_display_surfaces"),
            new RecipeSourceSurface("plasma_forge_recipe_dto", "inventory/recipes/PlasmaForgeRecipe.java",
                    "com.hbm.ntm.recipe.GenericMachineRecipe.Machine.PLASMA_FORGE + GenericMachineRecipeExtraData.PlasmaForge",
                    "support_class_covered_by_family",
                    "legacy GenericRecipe subclass DTO used by PlasmaForgeRecipes; reliable source-backed defaults are tracked by the plasma_forge machine surface"),
            new RecipeSourceSurface("purex_recipe_dto", "inventory/recipes/PUREXRecipe.java",
                    "com.hbm.ntm.recipe.GenericMachineRecipe.Machine.PUREX + LegacyGenericRecipeImporter/ExportProvider",
                    "support_class_covered_by_family",
                    "legacy GenericRecipe subclass DTO used by PUREXRecipes; defaults are tracked by the purex machine surface"));
    private static final Map<String, String> MOD_INTEGRATION_RECIPE_EXCLUSIONS = Map.of(
            "ass.digimemer", "legacy conditional Mekanism integration output is excluded by the mod-integration freeze");
    private static final Map<String, String> BOSS_ENTITY_RECIPE_EXCLUSIONS = Map.of(
            "ass.chopper", "Hunter Chopper spawn output is boss/entity content and is excluded by the boss-content migration rule",
            "ass.ballsotron", "mechanical worm spawn output is boss/entity content and is excluded by the boss-content migration rule");

    private final PackOutput output;
    private final Path projectRoot;
    private final Path reportPath;

    public LegacyJavaRecipeCoverageProvider(PackOutput output, Path projectRoot) {
        this.output = output;
        this.projectRoot = projectRoot;
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_java_recipe_coverage_report.json");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        JsonObject root = new JsonObject();
        root.addProperty("note", "Coverage is extracted from 1.7.10 Java GenericRecipe registrations, not legacy template JSON.");
        JsonArray machines = new JsonArray();
        root.add("machines", machines);
        JsonArray recipeFamilies = new JsonArray();
        root.add("recipe_families", recipeFamilies);
        JsonArray dynamicDisplaySurfaces = new JsonArray();
        root.add("dynamic_display_surfaces", dynamicDisplaySurfaces);
        JsonArray legacyNeiHandlerSurfaces = new JsonArray();
        root.add("legacy_nei_handler_surfaces", legacyNeiHandlerSurfaces);
        JsonArray neiRegistrationSurfaces = new JsonArray();
        root.add("nei_registration_surfaces", neiRegistrationSurfaces);
        JsonArray specialCraftingSurfaces = new JsonArray();
        root.add("special_crafting_surfaces", specialCraftingSurfaces);
        JsonArray recipeSourceSurfaces = new JsonArray();
        root.add("recipe_source_surfaces", recipeSourceSurfaces);
        root.add("serializable_handlers", legacySerializableHandlerCoverage());
        root.add("recipe_facades", recipeFacadeCoverage());
        root.add("recipe_source_file_audit", recipeSourceFileAudit());

        Map<String, ModernRecipe> modernRecipes = collectModernRecipes();
        addMachineReport(machines, "chemical_plant", "ChemicalPlantRecipes.java", "chem.", modernRecipes);
        addMachineReport(machines, "assembly_machine", "AssemblyMachineRecipes.java", "ass.", modernRecipes);
        addMachineReport(machines, "purex", "PUREXRecipes.java", "purex.", modernRecipes);
        addMachineReport(machines, "fusion_reactor", "FusionRecipes.java", "fus.", modernRecipes);
        addMachineReport(machines, "plasma_forge_reliable_scope", "PlasmaForgeRecipes.java", "plsm.",
                MIGRATED_PLASMA_FORGE_RECIPES, modernRecipes);
        for (CountRecipeFamily family : COUNT_RECIPE_FAMILIES) {
            addCountFamilyReport(recipeFamilies, family);
        }
        for (DynamicDisplaySurface surface : DYNAMIC_DISPLAY_SURFACES) {
            addDynamicDisplaySurfaceReport(dynamicDisplaySurfaces, surface);
        }
        addLegacyNeiHandlerSurfaceReports(legacyNeiHandlerSurfaces);
        for (NeiRegistrationSurface surface : NEI_REGISTRATION_SURFACES) {
            addNeiRegistrationSurfaceReport(neiRegistrationSurfaces, surface);
        }
        for (CraftingRegistrationSurface surface : SPECIAL_CRAFTING_SURFACES) {
            addCraftingRegistrationSurfaceReport(specialCraftingSurfaces, surface);
        }
        for (RecipeSourceSurface surface : RECIPE_SOURCE_SURFACES) {
            addRecipeSourceSurfaceReport(recipeSourceSurfaces, surface);
        }

        return DataProvider.saveStable(cachedOutput, root, reportPath);
    }

    @Override
    public String getName() {
        return "HBM legacy Java machine recipe coverage";
    }

    private static JsonObject recipeFacadeCoverage() {
        JsonObject report = new JsonObject();
        JsonArray supported = new JsonArray();
        JsonArray deferred = new JsonArray();
        report.add("supported", supported);
        report.add("deferred", deferred);

        for (CompatRecipeRegistry.RecipeFacadeStatus status : CompatRecipeRegistry.recipeFacadeStatuses()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("legacy_method", status.legacyMethod());
            entry.addProperty("modern_facade", status.modernFacade());
            entry.addProperty("note", status.note());
            if (status.supported()) {
                supported.add(entry);
            } else {
                deferred.add(entry);
            }
        }

        report.addProperty("total_count", supported.size() + deferred.size());
        report.addProperty("supported_count", supported.size());
        report.addProperty("deferred_count", deferred.size());
        report.addProperty("missing_count", deferred.size());
        report.addProperty("blocked_count", 0);
        report.addProperty("status", deferred.isEmpty() ? "all_supported" : "has_deferred_facades");
        return report;
    }

    private void addMachineReport(JsonArray machines, String machine, String legacyFileName, String prefix,
            Map<String, ModernRecipe> modernRecipes) {
        addMachineReport(machines, machine, legacyFileName, prefix, null, modernRecipes);
    }

    private void addMachineReport(JsonArray machines, String machine, String legacyFileName, String prefix,
            Set<String> includedInternalNames, Map<String, ModernRecipe> modernRecipes) {
        JsonObject report = new JsonObject();
        machines.add(report);
        report.addProperty("machine", machine);
        report.addProperty("legacy_file", legacyFileName);
        Path source = legacyRecipeSource(legacyFileName);
        report.addProperty("legacy_source", reportPath(source));

        JsonArray present = new JsonArray();
        JsonArray missing = new JsonArray();
        JsonArray excluded = new JsonArray();
        JsonArray blocked = new JsonArray();
        report.add("present", present);
        report.add("missing", missing);
        report.add("excluded", excluded);
        report.add("blocked", blocked);

        if (!Files.isRegularFile(source)) {
            report.addProperty("status", "missing_legacy_source");
            report.addProperty("legacy_count", 0);
            report.addProperty("modern_count", modernRecipes.values().stream()
                    .filter(recipe -> recipe.internalName().startsWith(prefix))
                    .count());
            return;
        }

        report.addProperty("status", "checked");
        int legacyCount = 0;
        for (LegacyRecipe recipe : extractLegacyRecipes(source)) {
            if (includedInternalNames != null && !includedInternalNames.contains(recipe.internalName())) {
                continue;
            }
            legacyCount++;
            ModernRecipe modern = modernRecipes.get(recipe.internalName());
            JsonObject entry = new JsonObject();
            entry.addProperty("source_order", recipe.sourceOrder());
            entry.addProperty("internal_name", recipe.internalName());
            if (RUNTIME_PAUSED_RECIPE_BLOCKERS.containsKey(recipe.internalName())) {
                entry.addProperty("reason", RUNTIME_PAUSED_RECIPE_BLOCKERS.get(recipe.internalName()));
                if (modern != null) {
                    entry.addProperty("modern_id", modern.id());
                    entry.addProperty("modern_source_order", modern.sourceOrder());
                }
                blocked.add(entry);
            } else if (modern == null) {
                if (MOD_INTEGRATION_RECIPE_EXCLUSIONS.containsKey(recipe.internalName())) {
                    entry.addProperty("reason", MOD_INTEGRATION_RECIPE_EXCLUSIONS.get(recipe.internalName()));
                    excluded.add(entry);
                } else if (BOSS_ENTITY_RECIPE_EXCLUSIONS.containsKey(recipe.internalName())) {
                    entry.addProperty("reason", BOSS_ENTITY_RECIPE_EXCLUSIONS.get(recipe.internalName()));
                    excluded.add(entry);
                } else {
                    missing.add(entry);
                }
            } else {
                entry.addProperty("modern_id", modern.id());
                entry.addProperty("modern_source_order", modern.sourceOrder());
                present.add(entry);
            }
        }
        long modernCount = modernRecipes.values().stream()
                .filter(recipe -> recipe.internalName().startsWith(prefix))
                .filter(recipe -> includedInternalNames == null || includedInternalNames.contains(recipe.internalName()))
                .count();
        report.addProperty("legacy_count", legacyCount);
        report.addProperty("modern_count", modernCount);
        report.addProperty("present_count", present.size());
        report.addProperty("excluded_count", excluded.size());
        report.addProperty("blocked_count", blocked.size());
        report.addProperty("missing_count", missing.size());
    }

    private void addCountFamilyReport(JsonArray families, CountRecipeFamily family) {
        JsonObject report = new JsonObject();
        families.add(report);
        report.addProperty("machine", family.machine());
        report.addProperty("legacy_file", family.legacyFileName());
        report.addProperty("legacy_json_file", family.legacyJsonFileName());
        if (!family.materializedRecipePaths().isEmpty()) {
            report.addProperty("modern_recipe_type", "special_crafting");
        } else if (family.modernRecipeFolder() != null) {
            report.addProperty("modern_recipe_type", family.modernRecipeFolder());
        } else {
            report.addProperty("modern_recipe_type", family.displayOnlyModernCount() >= 0 ? "display_only" : "excluded");
        }
        if (!family.legacyFormatNote().isBlank()) {
            report.addProperty("legacy_format_note", family.legacyFormatNote());
        }
        report.addProperty("legacy_source", reportPath(legacyRecipeSource(family.legacyFileName())));

        int modernCount;
        if (!family.materializedRecipePaths().isEmpty()) {
            JsonArray files = new JsonArray();
            modernCount = 0;
            for (String materializedRecipePath : family.materializedRecipePaths()) {
                Path mainResourceFile = recipeFile(materializedRecipePath);
                files.add(reportPath(mainResourceFile));
                if (Files.isRegularFile(mainResourceFile)) {
                    modernCount++;
                }
            }
            if (files.size() == 1) {
                report.addProperty("main_resource_recipe_file", files.get(0).getAsString());
            } else {
                report.add("main_resource_recipe_files", files);
            }
        } else if (family.displayOnlyModernCount() >= 0) {
            modernCount = family.displayOnlyModernCount();
        } else if (family.modernRecipeFolder() != null) {
            Path mainResourceDir = recipeDirectory(family.modernRecipeFolder());
            report.addProperty("main_resource_recipe_dir", reportPath(mainResourceDir));
            modernCount = countMainResourceRecipes(mainResourceDir);
        } else {
            modernCount = 0;
        }
        report.addProperty("legacy_count", family.legacyDefaultCount());
        report.addProperty("modern_count", modernCount);
        JsonArray excluded = new JsonArray();
        family.excludedSourceOrders().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(exclusion -> {
                    JsonObject entry = new JsonObject();
                    entry.addProperty("source_order", exclusion.getKey());
                    entry.addProperty("reason", exclusion.getValue());
                    excluded.add(entry);
                });
        if (excluded.size() > 0) {
            report.add("excluded", excluded);
        }
        int activeLegacyCount = Math.max(0, family.legacyDefaultCount() - excluded.size());
        report.addProperty("present_count", Math.min(activeLegacyCount, modernCount));
        report.addProperty("excluded_count", excluded.size());
        report.addProperty("missing_count", Math.max(0, activeLegacyCount - modernCount));
        report.addProperty("blocked_count", 0);
        String alignedStatus = !family.materializedRecipePaths().isEmpty() ? "main_resource_special_aligned"
                : family.displayOnlyModernCount() >= 0 ? "display_surface_aligned"
                : family.modernRecipeFolder() != null ? "main_resources_aligned" : "excluded";
        String shortStatus = !family.materializedRecipePaths().isEmpty() ? "main_resource_special_missing"
                : family.displayOnlyModernCount() >= 0 ? "display_surface_short"
                : family.modernRecipeFolder() != null ? "main_resources_short" : "excluded";
        report.addProperty("status", modernCount >= activeLegacyCount ? alignedStatus : shortStatus);
    }

    private void addDynamicDisplaySurfaceReport(JsonArray surfaces, DynamicDisplaySurface surface) {
        JsonObject report = new JsonObject();
        surfaces.add(report);
        report.addProperty("machine", surface.machine());
        report.addProperty("legacy_file", surface.legacyFileName());
        report.addProperty("legacy_source", reportPath(legacyRecipeSource(surface.legacyFileName())));
        report.addProperty("legacy_recipe_id", surface.legacyRecipeId());
        report.addProperty("modern_source", surface.modernSource());
        report.addProperty("modern_count_current", surface.modernCountSupplier().getAsInt());
        report.addProperty("count_contract", surface.countContract());
        report.addProperty("missing_count", 0);
        report.addProperty("blocked_count", 0);
        report.addProperty("status", "dynamic_display_surface_tracked");
        report.addProperty("legacy_format_note", surface.legacyFormatNote());
    }

    private void addLegacyNeiHandlerSurfaceReports(JsonArray surfaces) {
        Map<String, LegacyNeiHandlerSurface> known = new LinkedHashMap<>();
        for (LegacyNeiHandlerSurface surface : LEGACY_NEI_HANDLER_SURFACES) {
            known.put(surface.legacyFileName(), surface);
        }

        Set<String> seen = new LinkedHashSet<>();
        Path handlerRoot = legacyRoot().resolve("src").resolve("main").resolve("java").resolve("com")
                .resolve("hbm").resolve("handler").resolve("nei");
        if (Files.isDirectory(handlerRoot)) {
            try (var paths = Files.list(handlerRoot)) {
                paths.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java"))
                        .sorted()
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            seen.add(fileName);
                            LegacyNeiHandlerSurface surface = known.getOrDefault(fileName,
                                    LegacyNeiHandlerSurface.unclassified(fileName));
                            addLegacyNeiHandlerSurfaceReport(surfaces, surface, path);
                        });
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to scan legacy NEI handlers under " + handlerRoot,
                        exception);
            }
        }

        for (LegacyNeiHandlerSurface surface : LEGACY_NEI_HANDLER_SURFACES) {
            if (!seen.contains(surface.legacyFileName())) {
                addLegacyNeiHandlerSurfaceReport(surfaces, surface,
                        legacyNeiHandlerSource(surface.legacyFileName()));
            }
        }
    }

    private void addLegacyNeiHandlerSurfaceReport(JsonArray surfaces, LegacyNeiHandlerSurface surface,
            Path legacySource) {
        JsonObject report = new JsonObject();
        surfaces.add(report);
        report.addProperty("legacy_handler", surface.legacyFileName().replace(".java", ""));
        report.addProperty("legacy_file", surface.legacyFileName());
        report.addProperty("legacy_source", reportPath(legacySource));
        if (!surface.legacyRegistrationFileName().isBlank()) {
            report.addProperty("legacy_registration_source",
                    reportPath(legacyRecipeSource(surface.legacyRegistrationFileName())));
        }
        if (!surface.coverageSurface().isBlank()) {
            report.addProperty("coverage_surface", surface.coverageSurface());
        }
        if (!surface.modernSource().isBlank()) {
            report.addProperty("modern_source", surface.modernSource());
        }
        String status = Files.isRegularFile(legacySource) ? surface.status() : "missing_legacy_source";
        report.addProperty("status", status);
        report.addProperty("missing_count", "unclassified_legacy_nei_handler".equals(status) ? 1 : 0);
        report.addProperty("blocked_count", 0);
        report.addProperty("excluded_count", status.contains("excluded") ? 1 : 0);
        report.addProperty("legacy_format_note", surface.legacyFormatNote());
    }

    private void addNeiRegistrationSurfaceReport(JsonArray surfaces, NeiRegistrationSurface surface) {
        JsonObject report = new JsonObject();
        surfaces.add(report);
        report.addProperty("surface", surface.surface());
        report.addProperty("legacy_file", surface.legacyFileName());
        report.addProperty("legacy_source", reportPath(legacyRecipeSource(surface.legacyFileName())));
        if (!surface.legacyRegistrationFileName().isBlank()) {
            report.addProperty("legacy_registration_source",
                    reportPath(legacyRecipeSource(surface.legacyRegistrationFileName())));
        }
        if (!surface.modernSource().isBlank()) {
            report.addProperty("modern_source", surface.modernSource());
        }
        report.addProperty("status", surface.status());
        report.addProperty("missing_count", 0);
        report.addProperty("blocked_count", 0);
        report.addProperty("excluded_count", "excluded".equals(surface.status()) ? 1 : 0);
        report.addProperty("legacy_format_note", surface.legacyFormatNote());
    }

    private void addCraftingRegistrationSurfaceReport(JsonArray surfaces, CraftingRegistrationSurface surface) {
        JsonObject report = new JsonObject();
        surfaces.add(report);
        report.addProperty("surface", surface.surface());
        report.addProperty("legacy_file", surface.legacyFileName());
        report.addProperty("legacy_source", reportPath(legacyRecipeSource(surface.legacyFileName())));
        if (!surface.legacyRegistrationFileName().isBlank()) {
            report.addProperty("legacy_registration_source",
                    reportPath(legacyRecipeSource(surface.legacyRegistrationFileName())));
        }
        if (!surface.materializedRecipePath().isBlank()) {
            report.addProperty("materialized_recipe_path", surface.materializedRecipePath());
        }
        report.addProperty("status", surface.status());
        report.addProperty("missing_count", 0);
        report.addProperty("blocked_count", 0);
        report.addProperty("excluded_count", "excluded".equals(surface.status()) ? 1 : 0);
        report.addProperty("legacy_format_note", surface.legacyFormatNote());
    }

    private void addRecipeSourceSurfaceReport(JsonArray surfaces, RecipeSourceSurface surface) {
        JsonObject report = new JsonObject();
        surfaces.add(report);
        report.addProperty("surface", surface.surface());
        report.addProperty("legacy_file", surface.legacyFileName());
        report.addProperty("legacy_source", reportPath(legacyRecipeSource(surface.legacyFileName())));
        if (!surface.modernSource().isBlank()) {
            report.addProperty("modern_source", surface.modernSource());
        }
        report.addProperty("status", surface.status());
        report.addProperty("missing_count", 0);
        report.addProperty("blocked_count", 0);
        report.addProperty("excluded_count", "excluded".equals(surface.status()) ? 1 : 0);
        report.addProperty("legacy_format_note", surface.legacyFormatNote());
    }

    private Map<String, ModernRecipe> collectModernRecipes() {
        Map<String, ModernRecipe> recipes = new LinkedHashMap<>();
        new HbmRecipeProvider(output).buildRecipes(recipe -> {
            JsonObject json = new JsonObject();
            recipe.serializeRecipeData(json);
            if (!json.has("internal_name")) {
                return;
            }
            String internalName = json.get("internal_name").getAsString();
            int sourceOrder = json.has("source_order") ? json.get("source_order").getAsInt() : Integer.MAX_VALUE;
            recipes.put(internalName, new ModernRecipe(recipe.getId().toString(), internalName, sourceOrder));
        });
        collectMaterializedModernRecipes(recipes);
        return recipes;
    }

    private void collectMaterializedModernRecipes(Map<String, ModernRecipe> recipes) {
        Path recipeRoot = projectRoot.resolve("src").resolve("main").resolve("resources").resolve("data")
                .resolve("hbm_ntm_rebirth").resolve("recipes");
        if (!Files.isDirectory(recipeRoot)) {
            return;
        }
        try (var paths = Files.walk(recipeRoot)) {
            paths.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> collectMaterializedModernRecipe(recipeRoot, path, recipes));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read materialized modern recipes under " + recipeRoot,
                    exception);
        }
    }

    private void collectMaterializedModernRecipe(Path recipeRoot, Path path, Map<String, ModernRecipe> recipes) {
        try {
            JsonElement element = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
            if (!element.isJsonObject()) {
                return;
            }
            JsonObject json = element.getAsJsonObject();
            if (!json.has("internal_name")) {
                return;
            }
            String internalName = json.get("internal_name").getAsString();
            int sourceOrder = json.has("source_order") ? json.get("source_order").getAsInt() : Integer.MAX_VALUE;
            Path relative = recipeRoot.relativize(path);
            String idPath = relative.toString().replace('\\', '/').replaceFirst("\\.json$", "");
            recipes.putIfAbsent(internalName,
                    new ModernRecipe("hbm_ntm_rebirth:" + idPath, internalName, sourceOrder));
        } catch (RuntimeException | IOException exception) {
            throw new IllegalStateException("Failed to parse materialized modern recipe " + path, exception);
        }
    }

    private static Iterable<LegacyRecipe> extractLegacyRecipes(Path source) {
        try {
            String text = Files.readString(source, StandardCharsets.UTF_8);
            Matcher matcher = LEGACY_RECIPE_NAME.matcher(text);
            java.util.List<LegacyRecipe> recipes = new java.util.ArrayList<>();
            int order = 0;
            while (matcher.find()) {
                recipes.add(new LegacyRecipe(order++, matcher.group(1)));
            }
            return recipes;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read legacy recipe source " + source, exception);
        }
    }

    private Path legacyRecipeSource(String legacyFileName) {
        if (legacyFileName.contains("/") || legacyFileName.contains("\\")) {
            return legacyRoot().resolve("src").resolve("main").resolve("java").resolve("com").resolve("hbm")
                    .resolve(legacyFileName.replace('\\', '/'));
        }
        return legacyRoot().resolve("src").resolve("main").resolve("java").resolve("com").resolve("hbm")
                .resolve("inventory").resolve("recipes").resolve(legacyFileName);
    }

    private Path legacyNeiHandlerSource(String legacyFileName) {
        return legacyRoot().resolve("src").resolve("main").resolve("java").resolve("com").resolve("hbm")
                .resolve("handler").resolve("nei").resolve(legacyFileName);
    }

    private JsonObject legacySerializableHandlerCoverage() {
        JsonObject report = new JsonObject();
        JsonArray tracked = new JsonArray();
        JsonArray untracked = new JsonArray();
        JsonArray metadataOnly = new JsonArray();
        report.add("tracked", tracked);
        report.add("untracked", untracked);
        report.add("metadata_only", metadataOnly);

        Set<String> seenLegacyClasses = new LinkedHashSet<>();
        for (LegacySerializableHandler legacy : findLegacySerializableHandlers()) {
            seenLegacyClasses.add(legacy.className());
            JsonObject entry = new JsonObject();
            entry.addProperty("legacy_class", legacy.className());
            entry.addProperty("legacy_source", reportPath(legacy.path()));
            LegacySerializableRecipeHandlers.byLegacyClassName(legacy.className()).ifPresentOrElse(handler -> {
                entry.addProperty("legacy_json_file", handler.legacyFileName());
                entry.addProperty("category", handler.category().name());
                entry.addProperty("import_status", handler.importStatus().name());
                entry.addProperty("modern_recipe_type", handler.modernRecipeType());
                entry.addProperty("notes", handler.notes());
                tracked.add(entry);
            }, () -> untracked.add(entry));
        }

        for (LegacySerializableRecipeHandlers.Handler handler : LegacySerializableRecipeHandlers.all()) {
            if (seenLegacyClasses.contains(handler.legacyClassName())) {
                continue;
            }
            JsonObject entry = new JsonObject();
            entry.addProperty("legacy_class", handler.legacyClassName());
            entry.addProperty("legacy_json_file", handler.legacyFileName());
            entry.addProperty("category", handler.category().name());
            entry.addProperty("import_status", handler.importStatus().name());
            entry.addProperty("modern_recipe_type", handler.modernRecipeType());
            entry.addProperty("notes", handler.notes());
            metadataOnly.add(entry);
        }

        report.addProperty("legacy_handler_count", seenLegacyClasses.size());
        report.addProperty("tracked_count", tracked.size());
        report.addProperty("untracked_count", untracked.size());
        report.addProperty("metadata_only_count", metadataOnly.size());
        return report;
    }

    private JsonObject recipeSourceFileAudit() {
        JsonObject report = new JsonObject();
        JsonArray auditedRoots = new JsonArray();
        auditedRoots.add("inventory/recipes");
        auditedRoots.add("inventory/material");
        report.add("audited_roots", auditedRoots);
        report.addProperty("legacy_source_root", reportPath(legacyJavaRoot()));

        Set<String> allSources = scanAuditedRecipeSourceFiles();
        Set<String> classifiedSources = collectClassifiedRecipeSourceFiles();
        Set<String> inScopeClassified = new LinkedHashSet<>(classifiedSources);
        inScopeClassified.retainAll(allSources);

        Set<String> unclassified = new LinkedHashSet<>(allSources);
        unclassified.removeAll(classifiedSources);

        Set<String> missingRegistered = new LinkedHashSet<>(classifiedSources);
        missingRegistered.removeAll(allSources);

        report.addProperty("total_count", allSources.size());
        report.addProperty("classified_count", inScopeClassified.size());
        report.addProperty("unclassified_count", unclassified.size());
        report.addProperty("missing_registered_count", missingRegistered.size());
        report.addProperty("missing_count", unclassified.size() + missingRegistered.size());
        report.addProperty("blocked_count", 0);
        report.addProperty("status", unclassified.isEmpty() && missingRegistered.isEmpty()
                ? "all_classified" : "has_unclassified_sources");
        report.add("classified", stringArray(inScopeClassified));
        report.add("unclassified", stringArray(unclassified));
        report.add("missing_registered_sources", stringArray(missingRegistered));
        return report;
    }

    private Set<String> scanAuditedRecipeSourceFiles() {
        Set<String> sources = new LinkedHashSet<>();
        for (Path root : legacySerializableSearchRoots()) {
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (var paths = Files.walk(root)) {
                paths.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java"))
                        .map(this::legacySourceRelativePath)
                        .filter(LegacyJavaRecipeCoverageProvider::isAuditedRecipeSource)
                        .forEach(sources::add);
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to scan legacy recipe source files under " + root,
                        exception);
            }
        }
        return sources;
    }

    private Set<String> collectClassifiedRecipeSourceFiles() {
        Set<String> sources = new LinkedHashSet<>();
        for (LegacySerializableHandler handler : findLegacySerializableHandlers()) {
            addAuditedRecipeSource(sources, handler.path());
        }
        for (CountRecipeFamily family : COUNT_RECIPE_FAMILIES) {
            addAuditedRecipeSource(sources, legacyRecipeSource(family.legacyFileName()));
        }
        for (RecipeSourceSurface surface : RECIPE_SOURCE_SURFACES) {
            addAuditedRecipeSource(sources, legacyRecipeSource(surface.legacyFileName()));
        }
        return sources;
    }

    private void addAuditedRecipeSource(Set<String> sources, Path source) {
        String relative = legacySourceRelativePath(source);
        if (isAuditedRecipeSource(relative)) {
            sources.add(relative);
        }
    }

    private Path legacyJavaRoot() {
        return legacyRoot().resolve("src").resolve("main").resolve("java").resolve("com").resolve("hbm");
    }

    private String legacySourceRelativePath(Path source) {
        Path javaRoot = legacyJavaRoot().toAbsolutePath().normalize();
        Path normalized = source.toAbsolutePath().normalize();
        if (normalized.startsWith(javaRoot)) {
            return reportPath(javaRoot.relativize(normalized));
        }
        return reportPath(source);
    }

    private static boolean isAuditedRecipeSource(String relativePath) {
        return relativePath.startsWith("inventory/recipes/") || relativePath.startsWith("inventory/material/");
    }

    private static JsonArray stringArray(Set<String> values) {
        JsonArray array = new JsonArray();
        values.stream().sorted().forEach(array::add);
        return array;
    }

    private List<LegacySerializableHandler> findLegacySerializableHandlers() {
        List<LegacySerializableHandler> handlers = new ArrayList<>();
        for (Path root : legacySerializableSearchRoots()) {
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (var paths = Files.walk(root)) {
                paths.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java"))
                        .filter(path -> !isLegacyRecipeLoaderPath(path))
                        .forEach(path -> {
                            String text;
                            try {
                                text = Files.readString(path, StandardCharsets.UTF_8);
                            } catch (IOException exception) {
                                throw new IllegalStateException("Failed to read legacy recipe handler source " + path,
                                        exception);
                            }
                            Matcher matcher = LEGACY_SERIALIZABLE_RECIPE_CLASS.matcher(text);
                            if (matcher.find()) {
                                handlers.add(new LegacySerializableHandler(matcher.group(1), path));
                            }
                        });
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to scan legacy recipe handlers under " + root, exception);
            }
        }
        return handlers;
    }

    private static boolean isLegacyRecipeLoaderPath(Path path) {
        return path.toString().replace('\\', '/').contains("/inventory/recipes/loader/");
    }

    private List<Path> legacySerializableSearchRoots() {
        Path inventory = legacyRoot().resolve("src").resolve("main").resolve("java").resolve("com")
                .resolve("hbm").resolve("inventory");
        return List.of(inventory.resolve("recipes"), inventory.resolve("material"));
    }

    private Path legacyRoot() {
        Path envRoot = envPath("HBM_LEGACY_1710_ROOT");
        return envRoot != null ? envRoot : defaultLegacyRoot();
    }

    private Path recipeDirectory(String recipeFolder) {
        return projectRoot.resolve("src").resolve("main").resolve("resources").resolve("data")
                .resolve("hbm_ntm_rebirth").resolve("recipes").resolve(recipeFolder);
    }

    private Path recipeFile(String recipePath) {
        return projectRoot.resolve("src").resolve("main").resolve("resources").resolve("data")
                .resolve("hbm_ntm_rebirth").resolve("recipes").resolve(recipePath + ".json");
    }

    private static int countMainResourceRecipes(Path directory) {
        if (!Files.isDirectory(directory)) {
            return 0;
        }
        try (var paths = Files.walk(directory)) {
            return (int) paths.filter(path -> Files.isRegularFile(path)
                    && path.getFileName().toString().endsWith(".json")).count();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to count materialized modern recipes under " + directory,
                    exception);
        }
    }

    private static int countFluidContainerDisplayRecipes() {
        return (int) HbmFluidContainerRegistry.getAllContainers().stream()
                .filter(entry -> entry.type() != HbmFluids.NONE)
                .filter(entry -> entry.content() > 0)
                .filter(entry -> !entry.copyFullContainer().isEmpty())
                .count();
    }

    private static int countCrucibleCastingDisplayRecipes() {
        int count = 0;
        for (NTMMaterial material : Mats.orderedList) {
            if (material.smeltable != SmeltingBehavior.SMELTABLE) {
                continue;
            }
            for (Mold mold : FoundryMoldItem.molds()) {
                if (mold.size() > 1) {
                    continue;
                }
                if (!mold.getOutput(material).isEmpty()
                        && !FoundryScrapsItem.create(new MaterialStack(material, mold.cost()), true).isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }

    private static LegacyNeiHandlerSurface registeredHandler(String legacyFileName, String coverageSurface,
            String modernSource, String legacyFormatNote) {
        return new LegacyNeiHandlerSurface(legacyFileName, "main/NEIRegistry.java", coverageSurface, modernSource,
                "registered_recipe_family_covered", legacyFormatNote);
    }

    private static LegacyNeiHandlerSurface registeredDisplayHandler(String legacyFileName, String coverageSurface,
            String modernSource, String legacyFormatNote) {
        return new LegacyNeiHandlerSurface(legacyFileName, "main/NEIRegistry.java", coverageSurface, modernSource,
                "registered_display_surface_aligned", legacyFormatNote);
    }

    private static LegacyNeiHandlerSurface registeredDynamicHandler(String legacyFileName, String coverageSurface,
            String modernSource, String legacyFormatNote) {
        return new LegacyNeiHandlerSurface(legacyFileName, "main/NEIRegistry.java", coverageSurface, modernSource,
                "registered_dynamic_display_surface_tracked", legacyFormatNote);
    }

    private static LegacyNeiHandlerSurface registeredExclusionHandler(String legacyFileName, String coverageSurface,
            String modernSource, String legacyFormatNote) {
        return new LegacyNeiHandlerSurface(legacyFileName, "main/NEIRegistry.java", coverageSurface, modernSource,
                "registered_excluded", legacyFormatNote);
    }

    private Path defaultLegacyRoot() {
        return Path.of("E:", "游戏", "我的世界", "源码包", "Hbm-s-Nuclear-Tech-GIT-master");
    }

    private static Path envPath(String name) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? null : Path.of(value).toAbsolutePath().normalize();
    }

    private static String reportPath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private record LegacyRecipe(int sourceOrder, String internalName) {
    }

    private record ModernRecipe(String id, String internalName, int sourceOrder) {
    }

    private record LegacySerializableHandler(String className, Path path) {
    }

    private record DynamicDisplaySurface(String machine, String legacyFileName, String legacyRecipeId,
            String modernSource, IntSupplier modernCountSupplier, String legacyFormatNote, String countContract) {
        private DynamicDisplaySurface(String machine, String legacyFileName, String legacyRecipeId,
                String modernSource, IntSupplier modernCountSupplier, String legacyFormatNote) {
            this(machine, legacyFileName, legacyRecipeId, modernSource, modernCountSupplier, legacyFormatNote,
                    "dynamic_registry");
        }
    }

    private record NeiRegistrationSurface(String surface, String legacyFileName, String legacyRegistrationFileName,
            String modernSource, String status, String legacyFormatNote) {
    }

    private record LegacyNeiHandlerSurface(String legacyFileName, String legacyRegistrationFileName,
            String coverageSurface, String modernSource, String status, String legacyFormatNote) {
        private static LegacyNeiHandlerSurface unclassified(String legacyFileName) {
            return new LegacyNeiHandlerSurface(legacyFileName, "", "", "", "unclassified_legacy_nei_handler",
                    "legacy handler/nei source file has not been classified in LegacyJavaRecipeCoverageProvider");
        }
    }

    private record CraftingRegistrationSurface(String surface, String legacyFileName,
            String legacyRegistrationFileName, String materializedRecipePath, String status,
            String legacyFormatNote) {
    }

    private record RecipeSourceSurface(String surface, String legacyFileName, String modernSource, String status,
            String legacyFormatNote) {
    }

    private record CountRecipeFamily(String machine, String legacyFileName, String legacyJsonFileName,
            String modernRecipeFolder, int legacyDefaultCount, String legacyFormatNote,
            Map<Integer, String> excludedSourceOrders, int displayOnlyModernCount,
            List<String> materializedRecipePaths) {
        private CountRecipeFamily(String machine, String legacyFileName, String legacyJsonFileName,
                String modernRecipeFolder, int legacyDefaultCount) {
            this(machine, legacyFileName, legacyJsonFileName, modernRecipeFolder, legacyDefaultCount, "");
        }

        private CountRecipeFamily(String machine, String legacyFileName, String legacyJsonFileName,
                String modernRecipeFolder, int legacyDefaultCount, String legacyFormatNote) {
            this(machine, legacyFileName, legacyJsonFileName, modernRecipeFolder, legacyDefaultCount,
                    legacyFormatNote, Map.of());
        }

        private CountRecipeFamily(String machine, String legacyFileName, String legacyJsonFileName,
                String modernRecipeFolder, int legacyDefaultCount, String legacyFormatNote,
                Map<Integer, String> excludedSourceOrders) {
            this(machine, legacyFileName, legacyJsonFileName, modernRecipeFolder, legacyDefaultCount,
                    legacyFormatNote, excludedSourceOrders, -1, List.of());
        }

        private static CountRecipeFamily displayOnly(String machine, String legacyFileName, int legacyDefaultCount,
                String legacyFormatNote) {
            return new CountRecipeFamily(machine, legacyFileName, null, null, legacyDefaultCount,
                    legacyFormatNote, Map.of(), legacyDefaultCount, List.of());
        }

        private static CountRecipeFamily special(String machine, String legacyFileName, String materializedRecipePath,
                String legacyFormatNote) {
            return new CountRecipeFamily(machine, legacyFileName, null, null, 1, legacyFormatNote, Map.of(), -1,
                    List.of(materializedRecipePath));
        }

        private static CountRecipeFamily specialMulti(String machine, String legacyFileName,
                List<String> materializedRecipePaths, String legacyFormatNote) {
            return new CountRecipeFamily(machine, legacyFileName, null, null, materializedRecipePaths.size(),
                    legacyFormatNote, Map.of(), -1, List.copyOf(materializedRecipePaths));
        }

        private static CountRecipeFamily excluded(String machine, String legacyFileName, String reason) {
            return new CountRecipeFamily(machine, legacyFileName, null, null, 1, reason, Map.of(0, reason), -1,
                    List.of());
        }
    }
}
