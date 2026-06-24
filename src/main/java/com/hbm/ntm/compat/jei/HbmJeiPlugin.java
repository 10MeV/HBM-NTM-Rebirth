package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.fluid.HbmFluidCompressorRecipes;
import com.hbm.ntm.item.ConveyorWandItem;
import com.hbm.ntm.item.DepletedFuelItem;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.FluidDuctVariantBlockItem;
import com.hbm.ntm.item.FluidPipeBlockItem;
import com.hbm.ntm.item.HbmFluidContainerItem;
import com.hbm.ntm.item.ICFPelletItem;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.item.LegacyStateMultiblockBlockItem;
import com.hbm.ntm.item.RBMKFuelRodItem;
import com.hbm.ntm.item.RBMKPelletItem;
import com.hbm.ntm.item.TrinketBlockItem;
import com.hbm.ntm.recipe.AmmoPressRecipe;
import com.hbm.ntm.recipe.BlastFurnaceRecipe;
import com.hbm.ntm.recipe.BoilerRecipeRuntime;
import com.hbm.ntm.recipe.CombinationOvenRecipe;
import com.hbm.ntm.recipe.BreedingReactorRecipeRuntime;
import com.hbm.ntm.recipe.DeuteriumTowerRecipeRuntime;
import com.hbm.ntm.recipe.ElectrolyserRecipeRuntime;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import com.hbm.ntm.recipe.ICFPelletRecipeRuntime;
import com.hbm.ntm.recipe.LegacyBlueprintPools;
import com.hbm.ntm.recipe.LiquefactionRecipe;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.CrucibleRecipeRuntime;
import com.hbm.ntm.recipe.CyclotronRecipeRuntime;
import com.hbm.ntm.recipe.ExposureChamberRecipe;
import com.hbm.ntm.recipe.FusionFluidBreederRecipe;
import com.hbm.ntm.recipe.FuelPoolRecipes;
import com.hbm.ntm.recipe.MixerRecipe;
import com.hbm.ntm.recipe.OreSlopperRecipeRuntime;
import com.hbm.ntm.recipe.OutgasserRecipe;
import com.hbm.ntm.recipe.PWRFuelRuntime;
import com.hbm.ntm.recipe.RadiolysisRecipes;
import com.hbm.ntm.recipe.RadGenRecipeRuntime;
import com.hbm.ntm.recipe.ResearchReactorFuelRuntime;
import com.hbm.ntm.recipe.RotaryFurnaceRecipeRuntime;
import com.hbm.ntm.recipe.SilexRecipeRuntime;
import com.hbm.ntm.recipe.SolderingStationRecipe;
import com.hbm.ntm.recipe.RtgRecipeRuntime;
import com.hbm.ntm.recipe.TurbofanRecipeRuntime;
import com.hbm.ntm.recipe.TurbineGasRecipeRuntime;
import com.hbm.ntm.recipe.WatzFuelRuntime;
import com.hbm.ntm.recipe.WoodBurnerRecipeRuntime;
import com.hbm.ntm.recipe.ZirnoxFuelRuntime;
import com.hbm.ntm.recipe.PressRecipe;
import com.hbm.ntm.recipe.PyroOvenRecipe;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.satellite.SoyuzRocketItem;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.registries.RegistryObject;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JeiPlugin
public final class HbmJeiPlugin implements IModPlugin {
    public static final RecipeType<GenericMachineRecipe> ASSEMBLY_MACHINE =
            RecipeType.create(HbmNtm.MOD_ID, "assembly_machine", GenericMachineRecipe.class);
    public static final RecipeType<GenericMachineRecipe> CHEMICAL_PLANT =
            RecipeType.create(HbmNtm.MOD_ID, "chemical_plant", GenericMachineRecipe.class);
    public static final RecipeType<GenericMachineRecipe> PUREX =
            RecipeType.create(HbmNtm.MOD_ID, "purex", GenericMachineRecipe.class);
    public static final RecipeType<GenericMachineRecipe> PRECASS =
            RecipeType.create(HbmNtm.MOD_ID, "precass", GenericMachineRecipe.class);
    public static final RecipeType<GenericMachineRecipe> ARC_WELDER =
            RecipeType.create(HbmNtm.MOD_ID, "arc_welder", GenericMachineRecipe.class);
    public static final RecipeType<GenericMachineRecipe> ARC_FURNACE =
            RecipeType.create(HbmNtm.MOD_ID, "arc_furnace", GenericMachineRecipe.class);
    public static final RecipeType<GenericMachineRecipe> PLASMA_FORGE =
            RecipeType.create(HbmNtm.MOD_ID, "plasma_forge", GenericMachineRecipe.class);
    public static final RecipeType<GenericMachineRecipe> FUSION_REACTOR =
            RecipeType.create(HbmNtm.MOD_ID, "fusion_reactor", GenericMachineRecipe.class);
    public static final RecipeType<FusionFluidBreederRecipe> FUSION_FLUID_BREEDER =
            RecipeType.create(HbmNtm.MOD_ID, "fusion_fluid_breeder", FusionFluidBreederRecipe.class);
    public static final RecipeType<PressRecipe> PRESS =
            RecipeType.create(HbmNtm.MOD_ID, "press", PressRecipe.class);
    public static final RecipeType<HbmOilRecipe> REFINERY =
            RecipeType.create(HbmNtm.MOD_ID, "refinery", HbmOilRecipe.class);
    public static final RecipeType<HbmOilRecipe> CATALYTIC_CRACKER =
            RecipeType.create(HbmNtm.MOD_ID, "catalytic_cracker", HbmOilRecipe.class);
    public static final RecipeType<HbmOilRecipe> CATALYTIC_REFORMER =
            RecipeType.create(HbmNtm.MOD_ID, "catalytic_reformer", HbmOilRecipe.class);
    public static final RecipeType<HbmOilRecipe> VACUUM_DISTILL =
            RecipeType.create(HbmNtm.MOD_ID, "vacuum_distill", HbmOilRecipe.class);
    public static final RecipeType<HbmOilRecipe> FRACTION_TOWER =
            RecipeType.create(HbmNtm.MOD_ID, "fraction_tower", HbmOilRecipe.class);
    public static final RecipeType<HbmOilRecipe> HYDROTREATER =
            RecipeType.create(HbmNtm.MOD_ID, "hydrotreater", HbmOilRecipe.class);
    public static final RecipeType<HbmOilRecipe> COKER =
            RecipeType.create(HbmNtm.MOD_ID, "coker", HbmOilRecipe.class);
    public static final RecipeType<HbmOilRecipe> SOLIDIFIER =
            RecipeType.create(HbmNtm.MOD_ID, "solidifier", HbmOilRecipe.class);
    public static final RecipeType<LiquefactionRecipe> LIQUEFACTION =
            RecipeType.create(HbmNtm.MOD_ID, "liquefaction", LiquefactionRecipe.class);
    public static final RecipeType<HbmFluidCompressorRecipes.RecipeEntry> COMPRESSOR =
            RecipeType.create(HbmNtm.MOD_ID, "compressor", HbmFluidCompressorRecipes.RecipeEntry.class);
    public static final RecipeType<PyroOvenRecipe> PYRO_OVEN =
            RecipeType.create(HbmNtm.MOD_ID, "pyro_oven", PyroOvenRecipe.class);
    public static final RecipeType<CombinationOvenRecipe> COMBINATION_OVEN =
            RecipeType.create(HbmNtm.MOD_ID, "combination_oven", CombinationOvenRecipe.class);
    public static final RecipeType<BlastFurnaceRecipe> BLAST_FURNACE =
            RecipeType.create(HbmNtm.MOD_ID, "blast_furnace", BlastFurnaceRecipe.class);
    public static final RecipeType<MixerRecipe> MIXER =
            RecipeType.create(HbmNtm.MOD_ID, "mixer", MixerRecipe.class);
    public static final RecipeType<AmmoPressRecipe> AMMO_PRESS =
            RecipeType.create(HbmNtm.MOD_ID, "ammo_press", AmmoPressRecipe.class);
    public static final RecipeType<ItemProcessingRecipe> SHREDDER =
            RecipeType.create(HbmNtm.MOD_ID, "shredder", ItemProcessingRecipe.class);
    public static final RecipeType<ItemProcessingRecipe> CENTRIFUGE =
            RecipeType.create(HbmNtm.MOD_ID, "centrifuge", ItemProcessingRecipe.class);
    public static final RecipeType<ItemProcessingRecipe> CRYSTALLIZER =
            RecipeType.create(HbmNtm.MOD_ID, "crystallizer", ItemProcessingRecipe.class);
    public static final RecipeType<GasCentJeiRecipe> GAS_CENT =
            RecipeType.create(HbmNtm.MOD_ID, "gas_cent", GasCentJeiRecipe.class);
    public static final RecipeType<SawmillJeiRecipe> SAWMILL =
            RecipeType.create(HbmNtm.MOD_ID, "sawmill", SawmillJeiRecipe.class);
    public static final RecipeType<CyclotronRecipeRuntime.DisplayRecipe> CYCLOTRON =
            RecipeType.create(HbmNtm.MOD_ID, "cyclotron", CyclotronRecipeRuntime.DisplayRecipe.class);
    public static final RecipeType<ExposureChamberRecipe> EXPOSURE_CHAMBER =
            RecipeType.create(HbmNtm.MOD_ID, "exposure_chamber", ExposureChamberRecipe.class);
    public static final RecipeType<SolderingStationRecipe> SOLDERING_STATION =
            RecipeType.create(HbmNtm.MOD_ID, "soldering_station", SolderingStationRecipe.class);
    public static final RecipeType<SilexRecipeRuntime.DisplayRecipe> SILEX =
            RecipeType.create(HbmNtm.MOD_ID, "silex", SilexRecipeRuntime.DisplayRecipe.class);
    public static final RecipeType<RotaryFurnaceRecipeRuntime.Recipe> ROTARY_FURNACE =
            RecipeType.create(HbmNtm.MOD_ID, "rotary_furnace", RotaryFurnaceRecipeRuntime.Recipe.class);
    public static final RecipeType<CrucibleRecipeRuntime.Recipe> CRUCIBLE =
            RecipeType.create(HbmNtm.MOD_ID, "crucible", CrucibleRecipeRuntime.Recipe.class);
    public static final RecipeType<RadiolysisRecipes.DisplayRecipe> RADIOLYSIS =
            RecipeType.create(HbmNtm.MOD_ID, "radiolysis", RadiolysisRecipes.DisplayRecipe.class);
    public static final RecipeType<ElectrolyserRecipeRuntime.DisplayRecipe> ELECTROLYSER =
            RecipeType.create(HbmNtm.MOD_ID, "electrolyser", ElectrolyserRecipeRuntime.DisplayRecipe.class);
    public static final RecipeType<DeuteriumTowerRecipeRuntime.DisplayRecipe> DEUTERIUM_TOWER =
            RecipeType.create(HbmNtm.MOD_ID, "deuterium_tower", DeuteriumTowerRecipeRuntime.DisplayRecipe.class);
    public static final RecipeType<OreSlopperRecipeRuntime.DisplayRecipe> ORE_SLOPPER =
            RecipeType.create(HbmNtm.MOD_ID, "ore_slopper", OreSlopperRecipeRuntime.DisplayRecipe.class);
    public static final RecipeType<RadGenRecipeRuntime.FuelSpec> RADGEN =
            RecipeType.create(HbmNtm.MOD_ID, "radgen", RadGenRecipeRuntime.FuelSpec.class);
    public static final RecipeType<WoodBurnerRecipeRuntime.DisplayRecipe> WOOD_BURNER =
            RecipeType.create(HbmNtm.MOD_ID, "wood_burner", WoodBurnerRecipeRuntime.DisplayRecipe.class);
    public static final RecipeType<TurbofanRecipeRuntime.DisplayRecipe> TURBOFAN =
            RecipeType.create(HbmNtm.MOD_ID, "turbofan", TurbofanRecipeRuntime.DisplayRecipe.class);
    public static final RecipeType<TurbineGasRecipeRuntime.DisplayRecipe> TURBINE_GAS =
            RecipeType.create(HbmNtm.MOD_ID, "turbinegas", TurbineGasRecipeRuntime.DisplayRecipe.class);
    public static final RecipeType<com.hbm.ntm.blockentity.RtgBlockEntity.FuelSpec> RTG =
            RecipeType.create(HbmNtm.MOD_ID, "rtg", com.hbm.ntm.blockentity.RtgBlockEntity.FuelSpec.class);
    public static final RecipeType<ResearchReactorFuelRuntime.DisplayFuel> RESEARCH_REACTOR =
            RecipeType.create(HbmNtm.MOD_ID, "research_reactor", ResearchReactorFuelRuntime.DisplayFuel.class);
    public static final RecipeType<BreedingReactorRecipeRuntime.DisplayRecipe> BREEDING_REACTOR =
            RecipeType.create(HbmNtm.MOD_ID, "breeding_reactor", BreedingReactorRecipeRuntime.DisplayRecipe.class);
    public static final RecipeType<PWRFuelRuntime.DisplayFuel> PWR =
            RecipeType.create(HbmNtm.MOD_ID, "pwr", PWRFuelRuntime.DisplayFuel.class);
    public static final RecipeType<FuelPoolRecipes.DisplayRecipe> FUEL_POOL =
            RecipeType.create(HbmNtm.MOD_ID, "fuel_pool", FuelPoolRecipes.DisplayRecipe.class);
    public static final RecipeType<ZirnoxFuelRuntime.DisplayRod> ZIRNOX =
            RecipeType.create(HbmNtm.MOD_ID, "zirnox", ZirnoxFuelRuntime.DisplayRod.class);
    public static final RecipeType<WatzFuelRuntime.DisplayPellet> WATZ =
            RecipeType.create(HbmNtm.MOD_ID, "watz", WatzFuelRuntime.DisplayPellet.class);
    public static final RecipeType<ICFPelletRecipeRuntime.DisplayPellet> ICF_PELLET =
            RecipeType.create(HbmNtm.MOD_ID, "icf_pellet", ICFPelletRecipeRuntime.DisplayPellet.class);
    public static final RecipeType<AshpitJeiRecipe> ASHPIT =
            RecipeType.create(HbmNtm.MOD_ID, "ashpit", AshpitJeiRecipe.class);
    public static final RecipeType<BoilerRecipeRuntime.DisplayRecipe> BOILER =
            RecipeType.create(HbmNtm.MOD_ID, "boiler", BoilerRecipeRuntime.DisplayRecipe.class);
    public static final RecipeType<OutgasserRecipe> OUTGASSER =
            RecipeType.create(HbmNtm.MOD_ID, "outgasser", OutgasserRecipe.class);
    public static final RecipeType<RBMKFuelDisassemblyRecipeCategory.DisplayRecipe> RBMK_FUEL_DISASSEMBLY =
            RecipeType.create(HbmNtm.MOD_ID, "rbmk_fuel_disassembly",
                    RBMKFuelDisassemblyRecipeCategory.DisplayRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(HbmNtm.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        Set<Item> seen = new HashSet<>();
        for (RegistryObject<Item> item : ModItems.ITEMS.getEntries()) {
            registerHbmSubtype(registration, seen, item.get());
        }
        for (RegistryObject<? extends net.minecraft.world.level.block.Block> block : ModBlocks.BLOCKS.getEntries()) {
            registerHbmSubtype(registration, seen, block.get().asItem());
        }
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new HbmMachineRecipeCategory(ASSEMBLY_MACHINE, GenericMachineRecipe.Machine.ASSEMBLY_MACHINE,
                        ModBlocks.MACHINE_ASSEMBLY_MACHINE.get(), guiHelper),
                new HbmMachineRecipeCategory(CHEMICAL_PLANT, GenericMachineRecipe.Machine.CHEMICAL_PLANT,
                        ModBlocks.MACHINE_CHEMICAL_PLANT.get(), guiHelper),
                new HbmMachineRecipeCategory(PUREX, GenericMachineRecipe.Machine.PUREX,
                        ModBlocks.MACHINE_PUREX.get(), guiHelper),
                new HbmMachineRecipeCategory(PRECASS, GenericMachineRecipe.Machine.PRECASS,
                        ModBlocks.MACHINE_PRECASS.get(), guiHelper),
                new HbmMachineRecipeCategory(ARC_WELDER, GenericMachineRecipe.Machine.ARC_WELDER,
                        ModBlocks.MACHINE_ARC_WELDER.get(), guiHelper),
                new HbmMachineRecipeCategory(ARC_FURNACE, GenericMachineRecipe.Machine.ARC_FURNACE,
                        ModBlocks.MACHINE_ARC_FURNACE.get(), guiHelper),
                new HbmMachineRecipeCategory(PLASMA_FORGE, GenericMachineRecipe.Machine.PLASMA_FORGE,
                        ModBlocks.FUSION_PLASMA_FORGE.get(), guiHelper),
                new HbmMachineRecipeCategory(FUSION_REACTOR, GenericMachineRecipe.Machine.FUSION_REACTOR,
                        ModBlocks.FUSION_TORUS.get(), guiHelper),
                new FusionFluidBreederRecipeCategory(FUSION_FLUID_BREEDER,
                        ModBlocks.FUSION_BREEDER.get(), guiHelper),
                new PressRecipeCategory(PRESS, ModBlocks.MACHINE_PRESS.get(), guiHelper),
                new HbmOilRecipeCategory(REFINERY,
                        Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_refinery", "Refinery"),
                        ModBlocks.MACHINE_REFINERY.get(), guiHelper),
                new HbmOilRecipeCategory(CATALYTIC_CRACKER,
                        Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_catalytic_cracker", "Catalytic Cracker"),
                        ModBlocks.MACHINE_CATALYTIC_CRACKER.get(), guiHelper),
                new HbmOilRecipeCategory(CATALYTIC_REFORMER,
                        Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_catalytic_reformer", "Catalytic Reformer"),
                        ModBlocks.MACHINE_CATALYTIC_REFORMER.get(), guiHelper),
                new HbmOilRecipeCategory(VACUUM_DISTILL,
                        Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_vacuum_distill", "Vacuum Distillation Tower"),
                        ModBlocks.MACHINE_VACUUM_DISTILL.get(), guiHelper),
                new HbmOilRecipeCategory(FRACTION_TOWER,
                        Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_fraction_tower", "Fractioning Tower"),
                        ModBlocks.MACHINE_FRACTION_TOWER.get(), guiHelper),
                new HbmOilRecipeCategory(HYDROTREATER,
                        Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_hydrotreater", "Hydrotreater"),
                        ModBlocks.MACHINE_HYDROTREATER.get(), guiHelper),
                new HbmOilRecipeCategory(COKER,
                        Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_coker", "Coker"),
                        ModBlocks.MACHINE_COKER.get(), guiHelper),
                new HbmOilRecipeCategory(SOLIDIFIER,
                        Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_solidifier", "Solidifier"),
                        ModBlocks.MACHINE_SOLIDIFIER.get(), guiHelper),
                new LiquefactionRecipeCategory(LIQUEFACTION, ModBlocks.MACHINE_LIQUEFACTOR.get(), guiHelper),
                new CompressorRecipeCategory(COMPRESSOR, ModBlocks.MACHINE_COMPRESSOR.get(), guiHelper),
                new PyroOvenRecipeCategory(PYRO_OVEN, ModBlocks.MACHINE_PYROOVEN.get(), guiHelper),
                new CombinationOvenRecipeCategory(COMBINATION_OVEN, ModBlocks.FURNACE_COMBINATION.get(), guiHelper),
                new BlastFurnaceRecipeCategory(BLAST_FURNACE, ModBlocks.MACHINE_BLAST_FURNACE.get(), guiHelper),
                new MixerRecipeCategory(MIXER, ModBlocks.MACHINE_MIXER.get(), guiHelper),
                new AmmoPressRecipeCategory(AMMO_PRESS, ModBlocks.MACHINE_AMMO_PRESS.get(), guiHelper),
                new ItemProcessingRecipeCategory(SHREDDER, ItemProcessingRecipe.Machine.SHREDDER,
                        ModBlocks.MACHINE_SHREDDER.get(), guiHelper),
                new ItemProcessingRecipeCategory(CENTRIFUGE, ItemProcessingRecipe.Machine.CENTRIFUGE,
                        ModBlocks.MACHINE_CENTRIFUGE.get(), guiHelper),
                new ItemProcessingRecipeCategory(CRYSTALLIZER, ItemProcessingRecipe.Machine.CRYSTALLIZER,
                        ModBlocks.MACHINE_CRYSTALLIZER.get(), guiHelper),
                new GasCentRecipeCategory(GAS_CENT, ModBlocks.MACHINE_GASCENT.get(), guiHelper),
                new SawmillRecipeCategory(SAWMILL, ModBlocks.MACHINE_SAWMILL.get(), guiHelper),
                new CyclotronRecipeCategory(CYCLOTRON, ModBlocks.MACHINE_CYCLOTRON.get(), guiHelper),
                new ExposureChamberRecipeCategory(EXPOSURE_CHAMBER,
                        ModBlocks.MACHINE_EXPOSURE_CHAMBER.get(), guiHelper),
                new SolderingStationRecipeCategory(SOLDERING_STATION,
                        ModBlocks.MACHINE_SOLDERING_STATION.get(), guiHelper),
                new SilexRecipeCategory(SILEX, ModBlocks.MACHINE_SILEX.get(), guiHelper),
                new RotaryFurnaceRecipeCategory(ROTARY_FURNACE,
                        ModBlocks.MACHINE_ROTARY_FURNACE.get(), guiHelper),
                new CrucibleRecipeCategory(CRUCIBLE, ModBlocks.MACHINE_CRUCIBLE.get(), guiHelper),
                new RadiolysisRecipeCategory(RADIOLYSIS, ModBlocks.MACHINE_RADIOLYSIS.get(), guiHelper),
                new ElectrolyserRecipeCategory(ELECTROLYSER, ModBlocks.MACHINE_ELECTROLYSER.get(), guiHelper),
                new DeuteriumTowerRecipeCategory(DEUTERIUM_TOWER,
                        ModBlocks.MACHINE_DEUTERIUM_TOWER.get(), guiHelper),
                new OreSlopperRecipeCategory(ORE_SLOPPER, ModBlocks.MACHINE_ORE_SLOPPER.get(), guiHelper),
                new RadGenRecipeCategory(RADGEN, ModBlocks.MACHINE_RADGEN.get(), guiHelper),
                new WoodBurnerRecipeCategory(WOOD_BURNER, ModBlocks.MACHINE_WOOD_BURNER.get(), guiHelper),
                new TurbofanFuelRecipeCategory(TURBOFAN, ModBlocks.MACHINE_TURBOFAN.get(), guiHelper),
                new TurbineGasFuelRecipeCategory(TURBINE_GAS, ModBlocks.MACHINE_TURBINEGAS.get(), guiHelper),
                new RtgFuelRecipeCategory(RTG, ModBlocks.MACHINE_RTG_GREY.get(), guiHelper),
                new ResearchReactorFuelRecipeCategory(RESEARCH_REACTOR, ModBlocks.REACTOR_RESEARCH.get(),
                        guiHelper),
                new BreedingReactorRecipeCategory(BREEDING_REACTOR,
                        ModBlocks.MACHINE_REACTOR_BREEDING.get(), guiHelper),
                new PWRFuelRecipeCategory(PWR, ModBlocks.PWR_CONTROLLER.get(), guiHelper),
                new FuelPoolRecipeCategory(FUEL_POOL, ModBlocks.MACHINE_WASTE_DRUM.get(), guiHelper),
                new ZirnoxFuelRecipeCategory(ZIRNOX, ModBlocks.REACTOR_ZIRNOX.get(), guiHelper),
                new WatzFuelRecipeCategory(WATZ, ModBlocks.WATZ.get(), guiHelper),
                new ICFPelletRecipeCategory(ICF_PELLET, ModBlocks.MACHINE_ICF_PRESS.get(), guiHelper),
                new AshpitRecipeCategory(ASHPIT, ModBlocks.MACHINE_ASHPIT.get(), guiHelper),
                new BoilerRecipeCategory(BOILER, ModBlocks.MACHINE_BOILER.get(), guiHelper),
                new OutgasserRecipeCategory(OUTGASSER, ModBlocks.RBMK_OUTGASSER.get(), guiHelper),
                new RBMKFuelDisassemblyRecipeCategory(RBMK_FUEL_DISASSEMBLY,
                        ModItems.RBMK_FUEL_EMPTY.get(), guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipes(ASSEMBLY_MACHINE, sorted(recipeManager.getAllRecipesFor(ModRecipes.ASSEMBLY_MACHINE.type().get())));
        registration.addRecipes(CHEMICAL_PLANT, sorted(recipeManager.getAllRecipesFor(ModRecipes.CHEMICAL_PLANT.type().get())));
        registration.addRecipes(PUREX, sorted(recipeManager.getAllRecipesFor(ModRecipes.PUREX.type().get())));
        registration.addRecipes(PRECASS, sorted(recipeManager.getAllRecipesFor(ModRecipes.PRECASS.type().get())));
        registration.addRecipes(ARC_WELDER, sorted(recipeManager.getAllRecipesFor(ModRecipes.ARC_WELDER.type().get())));
        registration.addRecipes(ARC_FURNACE, sorted(recipeManager.getAllRecipesFor(ModRecipes.ARC_FURNACE.type().get())));
        registration.addRecipes(PLASMA_FORGE, sorted(recipeManager.getAllRecipesFor(ModRecipes.PLASMA_FORGE.type().get())));
        registration.addRecipes(FUSION_REACTOR, sorted(recipeManager.getAllRecipesFor(ModRecipes.FUSION_REACTOR.type().get())));
        registration.addRecipes(FUSION_FLUID_BREEDER,
                recipeManager.getAllRecipesFor(ModRecipes.FUSION_FLUID_BREEDER.type().get()));
        registration.addRecipes(PRESS, recipeManager.getAllRecipesFor(ModRecipes.PRESS.type().get()));
        registration.addRecipes(REFINERY, HbmOilRecipe.refineryRecipes());
        registration.addRecipes(CATALYTIC_CRACKER, HbmOilRecipe.crackingRecipes());
        registration.addRecipes(CATALYTIC_REFORMER, HbmOilRecipe.reformingRecipes());
        registration.addRecipes(VACUUM_DISTILL, HbmOilRecipe.vacuumRecipes());
        registration.addRecipes(FRACTION_TOWER, HbmOilRecipe.fractioningRecipes());
        registration.addRecipes(HYDROTREATER, HbmOilRecipe.hydrotreatingRecipes());
        registration.addRecipes(COKER, HbmOilRecipe.cokingRecipes());
        registration.addRecipes(SOLIDIFIER, HbmOilRecipe.solidificationRecipes());
        registration.addRecipes(LIQUEFACTION, recipeManager.getAllRecipesFor(ModRecipes.LIQUEFACTION.type().get()));
        registration.addRecipes(COMPRESSOR, HbmFluidCompressorRecipes.recipes());
        registration.addRecipes(PYRO_OVEN, recipeManager.getAllRecipesFor(ModRecipes.PYRO_OVEN.type().get()));
        registration.addRecipes(COMBINATION_OVEN, sortedCombination(recipeManager.getAllRecipesFor(ModRecipes.COMBINATION_OVEN.type().get())));
        registration.addRecipes(BLAST_FURNACE, sortedBlastFurnace(recipeManager.getAllRecipesFor(ModRecipes.BLAST_FURNACE.type().get())));
        registration.addRecipes(MIXER, sortedMixer(recipeManager.getAllRecipesFor(ModRecipes.MIXER.type().get())));
        registration.addRecipes(AMMO_PRESS, AmmoPressRecipeCategory.sorted(recipeManager.getAllRecipesFor(ModRecipes.AMMO_PRESS.type().get())));
        registration.addRecipes(SHREDDER, sortedItemProcessing(recipeManager.getAllRecipesFor(ModRecipes.SHREDDER.type().get())));
        registration.addRecipes(CENTRIFUGE, sortedItemProcessing(recipeManager.getAllRecipesFor(ModRecipes.CENTRIFUGE.type().get())));
        registration.addRecipes(CRYSTALLIZER, sortedItemProcessing(recipeManager.getAllRecipesFor(ModRecipes.CRYSTALLIZER.type().get())));
        registration.addRecipes(GAS_CENT, GasCentRecipeCategory.recipes());
        registration.addRecipes(SAWMILL, SawmillRecipeCategory.recipes());
        registration.addRecipes(CYCLOTRON, CyclotronRecipeRuntime.displayRecipes());
        registration.addRecipes(EXPOSURE_CHAMBER, sortedExposure(recipeManager.getAllRecipesFor(ModRecipes.EXPOSURE_CHAMBER.type().get())));
        registration.addRecipes(SOLDERING_STATION, sortedSoldering(recipeManager.getAllRecipesFor(ModRecipes.SOLDERING_STATION.type().get())));
        registration.addRecipes(SILEX, SilexRecipeRuntime.displayRecipes());
        registration.addRecipes(ROTARY_FURNACE, RotaryFurnaceRecipeRuntime.recipes());
        registration.addRecipes(CRUCIBLE, CrucibleRecipeRuntime.recipes());
        registration.addRecipes(RADIOLYSIS, RadiolysisRecipes.displayRecipes());
        registration.addRecipes(ELECTROLYSER, ElectrolyserRecipeRuntime.displayRecipes());
        registration.addRecipes(DEUTERIUM_TOWER, DeuteriumTowerRecipeRuntime.displayRecipes());
        registration.addRecipes(ORE_SLOPPER, OreSlopperRecipeRuntime.displayRecipes());
        registration.addRecipes(RADGEN, RadGenRecipeRuntime.recipes());
        registration.addRecipes(WOOD_BURNER, WoodBurnerRecipeRuntime.displayRecipes());
        registration.addRecipes(TURBOFAN, TurbofanRecipeRuntime.displayRecipes());
        registration.addRecipes(TURBINE_GAS, TurbineGasRecipeRuntime.displayRecipes());
        registration.addRecipes(RTG, RtgRecipeRuntime.displayRecipes());
        registration.addRecipes(RESEARCH_REACTOR, ResearchReactorFuelRuntime.displayFuels());
        registration.addRecipes(BREEDING_REACTOR, BreedingReactorRecipeRuntime.displayRecipes(Minecraft.getInstance().level));
        registration.addRecipes(PWR, PWRFuelRuntime.displayFuels());
        registration.addRecipes(FUEL_POOL, FuelPoolRecipes.displayRecipes(Minecraft.getInstance().level));
        registration.addRecipes(ZIRNOX, ZirnoxFuelRuntime.displayRods());
        registration.addRecipes(WATZ, WatzFuelRuntime.displayPellets());
        registration.addRecipes(ICF_PELLET, ICFPelletRecipeRuntime.displayPellets());
        registration.addRecipes(ASHPIT, AshpitJeiRecipe.recipes());
        registration.addRecipes(BOILER, BoilerRecipeRuntime.displayRecipes());
        registration.addRecipes(OUTGASSER,
                sortedOutgasser(recipeManager.getAllRecipesFor(ModRecipes.OUTGASSER.type().get())));
        registration.addRecipes(RBMK_FUEL_DISASSEMBLY, RBMKFuelDisassemblyRecipeCategory.recipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_ASSEMBLY_MACHINE.get()), ASSEMBLY_MACHINE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_ASSEMBLY_FACTORY.get()), ASSEMBLY_MACHINE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CHEMICAL_PLANT.get()), CHEMICAL_PLANT);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CHEMICAL_FACTORY.get()), CHEMICAL_PLANT);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_PUREX.get()), PUREX);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_PRECASS.get()), PRECASS);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_ARC_WELDER.get()), ARC_WELDER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_ARC_FURNACE.get()), ARC_FURNACE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FUSION_PLASMA_FORGE.get()), PLASMA_FORGE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FUSION_TORUS.get()), FUSION_REACTOR);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FUSION_BREEDER.get()), FUSION_FLUID_BREEDER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_PRESS.get()), PRESS);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_EPRESS.get()), PRESS);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_REFINERY.get()), REFINERY);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CATALYTIC_CRACKER.get()), CATALYTIC_CRACKER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CATALYTIC_REFORMER.get()), CATALYTIC_REFORMER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_VACUUM_DISTILL.get()), VACUUM_DISTILL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_FRACTION_TOWER.get()), FRACTION_TOWER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_HYDROTREATER.get()), HYDROTREATER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_COKER.get()), COKER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_SOLIDIFIER.get()), SOLIDIFIER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_LIQUEFACTOR.get()), LIQUEFACTION);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_COMPRESSOR.get()), COMPRESSOR);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_COMPRESSOR_COMPACT.get()), COMPRESSOR);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_PYROOVEN.get()), PYRO_OVEN);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FURNACE_COMBINATION.get()), COMBINATION_OVEN);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_BLAST_FURNACE.get()), BLAST_FURNACE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_MIXER.get()), MIXER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_AMMO_PRESS.get()), AMMO_PRESS);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_SHREDDER.get()), SHREDDER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CENTRIFUGE.get()), CENTRIFUGE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CRYSTALLIZER.get()), CRYSTALLIZER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_GASCENT.get()), GAS_CENT);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_SAWMILL.get()), SAWMILL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CYCLOTRON.get()), CYCLOTRON);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_EXPOSURE_CHAMBER.get()), EXPOSURE_CHAMBER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_SOLDERING_STATION.get()), SOLDERING_STATION);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_SILEX.get()), SILEX);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_ROTARY_FURNACE.get()), ROTARY_FURNACE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CRUCIBLE.get()), CRUCIBLE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_RADIOLYSIS.get()), RADIOLYSIS);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_ELECTROLYSER.get()), ELECTROLYSER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_DEUTERIUM_TOWER.get()), DEUTERIUM_TOWER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_ORE_SLOPPER.get()), ORE_SLOPPER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_RADGEN.get()), RADGEN);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_WOOD_BURNER.get()), WOOD_BURNER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_TURBOFAN.get()), TURBOFAN);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_TURBINEGAS.get()), TURBINE_GAS);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_RTG_GREY.get()), RTG);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.REACTOR_RESEARCH.get()), RESEARCH_REACTOR);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_REACTOR_BREEDING.get()), BREEDING_REACTOR);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PWR_CONTROLLER.get()), PWR);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PWR_FUEL.get()), PWR);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_WASTE_DRUM.get()), FUEL_POOL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.REACTOR_ZIRNOX.get()), ZIRNOX);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.WATZ.get()), WATZ);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_ICF_PRESS.get()), ICF_PELLET);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ICF.get()), ICF_PELLET);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_ASHPIT.get()), ASHPIT);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_BOILER.get()), BOILER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_INDUSTRIAL_BOILER.get()), BOILER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RBMK_OUTGASSER.get()), OUTGASSER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FUSION_BREEDER.get()), OUTGASSER);
        registration.addRecipeCatalyst(new ItemStack(net.minecraft.world.level.block.Blocks.CRAFTING_TABLE),
                RBMK_FUEL_DISASSEMBLY);
    }
    private static List<GenericMachineRecipe> sorted(List<GenericMachineRecipe> recipes) {
        return recipes.stream()
                .filter(HbmJeiPlugin::isVisibleGenericRecipe)
                .sorted(GenericMachineRecipe.LEGACY_ORDER)
                .toList();
    }

    private static boolean isVisibleGenericRecipe(GenericMachineRecipe recipe) {
        return !HbmClientConfig.hideSecretJeiRecipes()
                || recipe.getPools().stream().noneMatch(pool -> LegacyBlueprintPools.kind(pool) == LegacyBlueprintPools.Kind.SECRET);
    }

    private static List<ItemProcessingRecipe> sortedItemProcessing(List<ItemProcessingRecipe> recipes) {
        return recipes.stream()
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    private static List<OutgasserRecipe> sortedOutgasser(List<OutgasserRecipe> recipes) {
        return recipes.stream()
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    private static List<CombinationOvenRecipe> sortedCombination(List<CombinationOvenRecipe> recipes) {
        return recipes.stream()
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    private static List<BlastFurnaceRecipe> sortedBlastFurnace(List<BlastFurnaceRecipe> recipes) {
        return recipes.stream()
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    private static List<MixerRecipe> sortedMixer(List<MixerRecipe> recipes) {
        return recipes.stream()
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    private static List<ExposureChamberRecipe> sortedExposure(List<ExposureChamberRecipe> recipes) {
        return recipes.stream()
                .sorted(Comparator.comparingInt(ExposureChamberRecipe::sourceOrder)
                        .thenComparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    private static List<SolderingStationRecipe> sortedSoldering(List<SolderingStationRecipe> recipes) {
        return recipes.stream()
                .sorted(Comparator.comparingInt(SolderingStationRecipe::sourceOrder)
                        .thenComparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    private static void registerHbmSubtype(ISubtypeRegistration registration, Set<Item> seen, Item item) {
        if (!seen.add(item)) {
            return;
        }
        if (item instanceof HbmFluidContainerItem container) {
            registration.registerSubtypeInterpreter(item, (stack, context) -> fluidContainerSubtype(container, stack));
        } else if (item instanceof HbmBatteryItem battery) {
            registration.registerSubtypeInterpreter(item, (stack, context) -> batterySubtype(battery, stack));
        } else if (item instanceof DepletedFuelItem) {
            registration.registerSubtypeInterpreter(item, (stack, context) -> "damage=" + stack.getDamageValue());
        } else if (item instanceof RBMKFuelRodItem rod) {
            registration.registerSubtypeInterpreter(item, (stack, context) -> rbmkFuelRodSubtype(rod, stack));
        } else if (item instanceof RBMKPelletItem) {
            registration.registerSubtypeInterpreter(item, (stack, context) -> "damage=" + stack.getDamageValue());
        } else if (item instanceof ICFPelletItem) {
            registration.registerSubtypeInterpreter(item, HbmJeiPlugin::icfPelletSubtype);
        } else if (item instanceof FluidIdentifierItem) {
            registration.registerSubtypeInterpreter(item, (stack, context) -> "primary="
                    + FluidIdentifierItem.getType(stack, true).getName());
        } else if (item instanceof FluidPipeBlockItem) {
            registration.registerSubtypeInterpreter(item, (stack, context) -> "fluid="
                    + FluidPipeBlockItem.getFluidType(stack).getName());
        } else if (item instanceof FluidDuctVariantBlockItem duct) {
            registration.registerSubtypeInterpreter(item, (stack, context) -> "variant=" + duct.getLegacyMetadata(stack));
        } else if (item instanceof LegacyStateBlockItem stateItem) {
            registration.registerSubtypeInterpreter(item, (stack, context) -> "variant=" + stateItem.getVariant(stack));
        } else if (item instanceof LegacyStateMultiblockBlockItem stateItem) {
            registration.registerSubtypeInterpreter(item, (stack, context) -> "variant=" + stateItem.getVariant(stack));
        } else if (item instanceof TrinketBlockItem trinket) {
            registration.registerSubtypeInterpreter(item, (stack, context) -> "variant=" + TrinketBlockItem.getVariant(stack));
        } else if (item instanceof SoyuzRocketItem) {
            registration.registerSubtypeInterpreter(item, (stack, context) -> "skin=" + SoyuzRocketItem.getSkin(stack));
        } else if (item instanceof ConveyorWandItem) {
            registration.registerSubtypeInterpreter(item, HbmJeiPlugin::conveyorWandSubtype);
        }
    }

    private static String fluidContainerSubtype(HbmFluidContainerItem item, ItemStack stack) {
        return "kind=" + item.getContainerKind().name()
                + ";fluid=" + item.getFirstFluidType(stack).getName()
                + ";amount=" + item.getFill(stack)
                + ";pressure=" + item.getPressure(stack);
    }

    private static String batterySubtype(HbmBatteryItem item, ItemStack stack) {
        return "charge=" + item.getCharge(stack) + ";max=" + item.getMaxCharge(stack);
    }

    private static String rbmkFuelRodSubtype(RBMKFuelRodItem item, ItemStack stack) {
        com.hbm.ntm.neutron.RBMKFuelRodState state = item.getState(stack);
        return "yield=" + Math.round(state.remainingYield())
                + ";xenon=" + Math.round(state.xenon())
                + ";core=" + Math.round(state.coreHeat())
                + ";hull=" + Math.round(state.hullHeat());
    }

    private static String icfPelletSubtype(ItemStack stack, UidContext context) {
        return "first=" + ICFPelletItem.type(stack, true).name()
                + ";second=" + ICFPelletItem.type(stack, false).name()
                + ";muon=" + ICFPelletItem.isMuonCatalyzed(stack)
                + ";depletion=" + ICFPelletItem.getDepletion(stack);
    }

    private static String conveyorWandSubtype(ItemStack stack, UidContext context) {
        CompoundTag tag = stack.getTag();
        String type = tag == null || !tag.contains("Type") ? "REGULAR" : tag.getString("Type");
        return "type=" + type;
    }
}
