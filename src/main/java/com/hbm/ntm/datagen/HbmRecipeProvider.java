package com.hbm.ntm.datagen;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.ntm.util.HbmRegistryUtil;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.GasCentBlockEntity.PseudoFluidType;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.compat.CompatRecipeRegistry;
import com.hbm.ntm.item.DepletedFuelItem;
import com.hbm.ntm.item.BedrockOreItem;
import com.hbm.ntm.item.BedrockOreItem.BedrockOreGrade;
import com.hbm.ntm.item.BedrockOreItem.BedrockOreType;
import com.hbm.ntm.item.BedrockOreFragmentItem;
import com.hbm.ntm.item.FluidIconItem;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.item.LegacyTemFlakesItem;
import com.hbm.ntm.item.MarshmallowItem;
import com.hbm.ntm.item.PlasticScrapItem;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.AnvilSmithingRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeExtraData;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.LegacyBlueprintPools;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.recipe.HbmFluidContainerIngredient;
import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public final class HbmRecipeProvider extends RecipeProvider {
    private static final BedrockOreGrade[] BEDROCK_ORE_PRIMARY_SPLIT_INPUTS = {
            BedrockOreGrade.PRIMARY,
            BedrockOreGrade.PRIMARY_ROASTED,
            BedrockOreGrade.PRIMARY_SULFURIC,
            BedrockOreGrade.PRIMARY_NOSULFURIC,
            BedrockOreGrade.PRIMARY_SOLVENT,
            BedrockOreGrade.PRIMARY_NOSOLVENT,
            BedrockOreGrade.PRIMARY_RAD,
            BedrockOreGrade.PRIMARY_NORAD
    };
    private static final List<BedrockOreProducts> BEDROCK_ORE_PRODUCTS = List.of(
            new BedrockOreProducts(BedrockOreType.LIGHT_METAL,
                    bo(Mats.MAT_IRON, 9), bo(Mats.MAT_COPPER, 9),
                    bo(Mats.MAT_TITANIUM, 6), bo(Mats.MAT_BAUXITE, 9), bo(Mats.MAT_CRYOLITE, 3),
                    bo(Mats.MAT_CHLOROCALCITE, 5), bo(Mats.MAT_LITHIUM, 5), bo(Mats.MAT_SODIUM, 3),
                    bo(Mats.MAT_CHLOROCALCITE, 6), bo(Mats.MAT_LITHIUM, 6), bo(Mats.MAT_SODIUM, 6)),
            new BedrockOreProducts(BedrockOreType.HEAVY_METAL,
                    bo(Mats.MAT_TUNGSTEN, 9), bo(Mats.MAT_LEAD, 9),
                    bo(Mats.MAT_GOLD, 2), bo(Mats.MAT_GOLD, 2), bo(Mats.MAT_BERYLLIUM, 3),
                    bo(Mats.MAT_TUNGSTEN, 9), bo(Mats.MAT_LEAD, 9), bo(Mats.MAT_GOLD, 5),
                    bo(Mats.MAT_BISMUTH, 2), bo(Mats.MAT_TANTALIUM, 2), bo(Mats.MAT_GOLD, 6)),
            new BedrockOreProducts(BedrockOreType.RARE_EARTH,
                    bo(Mats.MAT_COBALT, 5), bo(Mats.MAT_RAREEARTH, 5),
                    bo(Mats.MAT_BORON, 5), bo(Mats.MAT_LANTHANIUM, 3), bo(Mats.MAT_NIOBIUM, 4),
                    bo(Mats.MAT_NEODYMIUM, 3), bo(Mats.MAT_STRONTIUM, 3), bo(Mats.MAT_ZIRCONIUM, 3),
                    bo(Mats.MAT_NIOBIUM, 5), bo(Mats.MAT_NEODYMIUM, 5), bo(Mats.MAT_STRONTIUM, 3)),
            new BedrockOreProducts(BedrockOreType.ACTINIDE,
                    bo(Mats.MAT_URANIUM, 4), bo(Mats.MAT_THORIUM, 4),
                    bo(Mats.MAT_RADIUM, 2), bo(Mats.MAT_RADIUM, 2), bo(Mats.MAT_POLONIUM, 2),
                    bo(Mats.MAT_RADIUM, 2), bo(Mats.MAT_RADIUM, 2), bo(Mats.MAT_POLONIUM, 2),
                    bo(Mats.MAT_TECHNETIUM, 1), bo(Mats.MAT_TECHNETIUM, 1), bo(Mats.MAT_U238, 1)),
            new BedrockOreProducts(BedrockOreType.NON_METAL,
                    bo(Mats.MAT_COAL, 9), bo(Mats.MAT_SULFUR, 9),
                    bo(Mats.MAT_LIGNITE, 9), bo(Mats.MAT_KNO, 6), bo(Mats.MAT_FLUORITE, 6),
                    bo(Mats.MAT_PHOSPHORUS, 5), bo(Mats.MAT_FLUORITE, 6), bo(Mats.MAT_SULFUR, 6),
                    bo(Mats.MAT_CHLOROCALCITE, 6), bo(Mats.MAT_SILICON, 2), bo(Mats.MAT_SILICON, 2)),
            new BedrockOreProducts(BedrockOreType.CRYSTALLINE,
                    bo(Mats.MAT_REDSTONE, 9), bo(Mats.MAT_CINNABAR, 4),
                    bo(Mats.MAT_SODALITE, 9), bo(Mats.MAT_ASBESTOS, 6), bo(Mats.MAT_DIAMOND, 3),
                    bo(Mats.MAT_CINNABAR, 3), bo(Mats.MAT_ASBESTOS, 5), bo(Mats.MAT_EMERALD, 3),
                    bo(Mats.MAT_BORAX, 3), bo(Mats.MAT_MOLYSITE, 3), bo(Mats.MAT_SODALITE, 9)));
    private static final List<String> LEGACY_DEPLETED_WASTE_ITEMS = List.of(
            "waste_natural_uranium",
            "waste_uranium",
            "waste_thorium",
            "waste_mox",
            "waste_plutonium",
            "waste_u233",
            "waste_u235",
            "waste_schrabidium",
            "waste_zfb_mox",
            "waste_plate_u233",
            "waste_plate_u235",
            "waste_plate_mox",
            "waste_plate_pu239",
            "waste_plate_sa326",
            "waste_plate_ra226be",
            "waste_plate_pu238be");

    public HbmRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike polymerPlate = item("plate_polymer");

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.MACHINE_BATTERY_SOCKET.get())
                .pattern("I I")
                .pattern("I I")
                .pattern("IRI")
                .define('I', polymerPlate)
                .define('R', ModItems.COPPER_COIL.get())
                .unlockedBy("has_plate_polymer", has(polymerPlate))
                .save(consumer, id("energy/machine_battery_socket_polymer"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.MACHINE_BATTERY_SOCKET.get())
                .pattern("PRP")
                .define('P', ModItems.STEEL_PLATE.get())
                .define('R', ModItems.COPPER_INGOT.get())
                .unlockedBy("has_steel_plate", has(ModItems.STEEL_PLATE.get()))
                .save(consumer, id("energy/machine_battery_socket_steel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.UPGRADE_SCREM.get())
                .pattern("SUS")
                .pattern("SCS")
                .pattern("SUS")
                .define('S', forgeTag("plates/steel"))
                .define('U', ModItems.UPGRADE_TEMPLATE.get())
                .define('C', item("crystal_xen"))
                .unlockedBy("has_crystal_xen", has(item("crystal_xen")))
                .save(consumer, id("control/upgrade_screm"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.UPGRADE_MUFFLER.get(), 16)
                .pattern("III")
                .pattern("IWI")
                .pattern("III")
                .define('I', forgeTag("ingots/any_rubber"))
                .define('W', ItemTags.WOOL)
                .unlockedBy("has_any_rubber", has(forgeTag("ingots/any_rubber")))
                .save(consumer, id("parts/upgrade_muffler"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.HOLOTAPE_DAMAGED.get())
                .requires(ModItems.HOLOTAPE_IMAGE_RESTORED.get())
                .requires(ModItems.UPGRADE_MUFFLER.get())
                .requires(item("crt_display"))
                .requires(item("gem_alexandrite"))
                .unlockedBy("has_holotape_image_restored", has(ModItems.HOLOTAPE_IMAGE_RESTORED.get()))
                .save(consumer, id("holotape/holotape_damaged"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.HOLOTAPE_IMAGE_RESTORED.get())
                .requires(ModItems.HOLOTAPE_IMAGE_DIGAMMA.get())
                .requires(forgeTag("tools/screwdrivers"))
                .requires(item("ducttape"))
                .requires(ModItems.ARMOR_POLISH.get())
                .unlockedBy("has_holotape_image_digamma", has(ModItems.HOLOTAPE_IMAGE_DIGAMMA.get()))
                .save(consumer, id("holotape/holotape_image_restored"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, legacyBatteryPack(0))
                .pattern("IRI")
                .pattern("PRP")
                .pattern("IRI")
                .define('I', ModItems.IRON_PLATE.get())
                .define('R', Blocks.REDSTONE_BLOCK)
                .define('P', polymerPlate)
                .unlockedBy("has_redstone_block", has(Blocks.REDSTONE_BLOCK))
                .save(consumer, id("energy/battery_redstone"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, legacyBatteryPack(6))
                .pattern("IRI")
                .pattern("PRP")
                .pattern("IRI")
                .define('I', ModItems.STEEL_PLATE.get())
                .define('R', block("block_copper"))
                .define('P', polymerPlate)
                .unlockedBy("has_copper_block", has(block("block_copper")))
                .save(consumer, id("energy/capacitor_copper"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, item("battery_spark"))
                .pattern(" W ")
                .pattern("DSD")
                .pattern("DSD")
                .define('W', forgeTag("dense_wires/magnetized_tungsten"))
                .define('D', item("plate_dineutronium"))
                .define('S', item("powder_spark_mix"))
                .unlockedBy("has_spark_mix", has(item("powder_spark_mix")))
                .save(consumer, id("energy/battery_spark"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, item("battery_trixite"))
                .pattern(" W ")
                .pattern("DSD")
                .pattern("DTD")
                .define('W', forgeTag("dense_wires/magnetized_tungsten"))
                .define('D', forgeTag("cast_plates/saturnite"))
                .define('S', item("powder_power"))
                .define('T', forgeTag("crystals/trixite"))
                .unlockedBy("has_trixite", has(forgeTag("crystals/trixite")))
                .save(consumer, id("energy/battery_trixite"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, item("battery_trixite"))
                .pattern(" W ")
                .pattern("DTD")
                .pattern("DSD")
                .define('W', forgeTag("dense_wires/magnetized_tungsten"))
                .define('D', forgeTag("cast_plates/saturnite"))
                .define('S', item("powder_power"))
                .define('T', forgeTag("crystals/trixite"))
                .unlockedBy("has_trixite", has(forgeTag("crystals/trixite")))
                .save(consumer, id("energy/battery_trixite_reversed"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, legacySelfChargingBattery(0))
                .pattern("PGP")
                .pattern("L L")
                .pattern("PGP")
                .define('P', forgeTag("ingots/any_plastic"))
                .define('G', forgeTag("wires/gold"))
                .define('L', ModItems.LEAD_PLATE.get())
                .unlockedBy("has_any_plastic", has(forgeTag("ingots/any_plastic")))
                .save(consumer, id("energy/battery_sc_empty"));

        selfChargingConversion(consumer, legacySelfChargingBattery(1), "battery_sc_waste", item("billet_nuclear_waste"));
        selfChargingConversion(consumer, legacySelfChargingBattery(2), "battery_sc_ra226", item("billet_ra226"));
        selfChargingConversion(consumer, legacySelfChargingBattery(3), "battery_sc_tc99", item("billet_technetium"));
        selfChargingConversion(consumer, legacySelfChargingBattery(4), "battery_sc_co60", item("billet_co60"));
        selfChargingConversion(consumer, legacySelfChargingBattery(5), "battery_sc_pu238", item("billet_pu238"));
        selfChargingConversion(consumer, legacySelfChargingBattery(6), "battery_sc_po210", item("billet_polonium"));
        selfChargingConversion(consumer, legacySelfChargingBattery(7), "battery_sc_au198", item("billet_au198"));
        selfChargingConversion(consumer, legacySelfChargingBattery(8), "battery_sc_pb209", item("billet_pb209"));
        selfChargingConversion(consumer, legacySelfChargingBattery(9), "battery_sc_am241", item("billet_am241"));
        legacyFullBatteryShapelessRecipe(consumer, id("energy/battery_potato"), ModItems.BATTERY_POTATO.get(), 1_000L,
                ingredientItem(Items.POTATO),
                ingredientItem(item("wire_fine_aluminium")),
                ingredientItem(item("wire_fine_copper")));
        legacyFullBatteryShapelessRecipe(consumer, id("energy/battery_potatos"), ModItems.BATTERY_POTATOS.get(), 500_000L,
                ingredientNbtItem(ModItems.BATTERY_POTATO.get(), "{charge:1000L}"),
                ingredientItem(ModItems.TURRET_CHIP.get()),
                ingredientItem(Items.REDSTONE));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, item("fuse"))
                .requires(forgeTag("plates/steel"))
                .requires(item("plate_polymer"))
                .requires(forgeTag("wires/tungsten"))
                .unlockedBy("has_tungsten_wire", has(forgeTag("wires/tungsten")))
                .save(consumer, id("energy/fuse"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModItems.ENERGY_CORE.get())
                .requires(ModItems.FUSION_CORE.get())
                .requires(item("fuse"))
                .unlockedBy("has_fusion_core", has(ModItems.FUSION_CORE.get()))
                .save(consumer, id("energy/energy_core"));
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.HEV_BATTERY.get(), 4)
                .pattern(" W ")
                .pattern("IEI")
                .pattern("ICI")
                .define('W', forgeTag("wires/gold"))
                .define('I', item("plate_polymer"))
                .define('E', Items.REDSTONE)
                .define('C', item("powder_cobalt"))
                .unlockedBy("has_plate_polymer", has(item("plate_polymer")))
                .save(consumer, id("energy/hev_battery"));
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.HEV_BATTERY.get(), 4)
                .pattern(" W ")
                .pattern("ICI")
                .pattern("IEI")
                .define('W', forgeTag("wires/gold"))
                .define('I', item("plate_polymer"))
                .define('E', Items.REDSTONE)
                .define('C', item("powder_cobalt"))
                .unlockedBy("has_plate_polymer", has(item("plate_polymer")))
                .save(consumer, id("energy/hev_battery_reversed"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModItems.HEV_BATTERY.get())
                .requires(item("hev_battery_block"))
                .unlockedBy("has_hev_battery_block", has(item("hev_battery_block")))
                .save(consumer, id("energy/hev_battery_from_block"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, item("hev_battery_block"))
                .requires(ModItems.HEV_BATTERY.get())
                .unlockedBy("has_hev_battery", has(ModItems.HEV_BATTERY.get()))
                .save(consumer, id("energy/hev_battery_block"));
        lemegetonRecipes(consumer);
        anvilSmithingRecipes(consumer);
        anvilSmithingSupportRecipes(consumer);
        hotSmeltingRecipes(consumer);
        energyNetworkRecipes(consumer);
        redstoneOverRadioRecipes(consumer);
        rbmkRecipes(consumer);
        legacySandMixRecipes(consumer);
        legacySmeltingRecipes(consumer);
        legacyToolRecipes(consumer);
        legacyPartRecipes(consumer);
        legacyMissingItemRecipes(consumer);
        missileSystemRecipes(consumer);
        legacyStructuralRecipes(consumer);
        coloredConcreteRecipes(consumer);
        legacyUpgradeRecipes(consumer);
        legacyWeaponPartRecipes(consumer);
        legacyLandmineRecipes(consumer);
        legacyBombPartRecipes(consumer);
        legacyStandardMissileRecipes(consumer);
        legacyArmorTableRecipe(consumer);
        legacyArmorModuleMaterialRecipes(consumer);
        legacyArmorModuleRecipes(consumer);
        legacyHazmatRecipes(consumer);
        legacyArmorRecipes(consumer);
        legacyArtilleryAmmoRecipes(consumer);
        legacyCustomMissilePartRecipes(consumer);
        legacyAmmunitionRecipes(consumer);
        legacyWeaponTableRecipes(consumer);
        legacyWeaponModRecipes(consumer);
        legacySednaGunRecipes(consumer);
        legacyTurretRecipes(consumer);
        legacyMissingMachineAcquisitionRecipes(consumer);

        chemicalPlantSourceRecipes(consumer);
        chemicalBatteryRecipes(consumer);
        assemblyCapacitorRecipes(consumer);
        assemblyMachineBodyRecipes(consumer);
        reactorAssemblyRecipes(consumer);
        pwrAssemblyRecipes(consumer);
        watzPelletRecipes(consumer);
        satelliteRecipes(consumer);
        fluidContainerRecipes(consumer);
        fluidNetworkRecipes(consumer);
        breedingReactorRecipes(consumer);
        fuelPoolRecipes(consumer);
        outgasserRecipes(consumer);
        shredderRecipes(consumer);
        exposureChamberRecipes(consumer);
        radGenRecipes(consumer);
        oilProcessingRecipes(consumer);
        radiolysisRecipes(consumer);
        compressorRecipes(consumer);
        combinationOvenRecipes(consumer);
        centrifugeRecipes(consumer);
        crystallizerRecipes(consumer);
        gasCentRecipes(consumer);
        blastFurnaceRecipes(consumer);
        diFurnaceRecipes(consumer);
        solderingStationRecipes(consumer);
        cyclotronRecipes(consumer);
        silexRecipes(consumer);
        particleAcceleratorRecipes(consumer);
        fusionReactorRecipes(consumer);
        fusionFluidBreederRecipes(consumer);
        reactorPlasmaForgeRecipes(consumer);
        purexRecipes(consumer);
        precassRecipes(consumer);
        liquefactionRecipes(consumer);
        rotaryFurnaceRecipes(consumer);
        crucibleRecipes(consumer);
        crucibleSmeltingRecipes(consumer);
        pedestalRecipes(consumer);
        electrolyzerFluidRecipes(consumer);
        electrolyzerMetalRecipes(consumer);
        pyroOvenRecipes(consumer);
        mixerRecipes(consumer);
        pressRecipes(consumer);
        arcWelderRecipes(consumer);
        arcFurnaceRecipes(consumer);
        compatRecipeListenerRecipes(consumer);
    }

    private static void radGenRecipes(Consumer<FinishedRecipe> consumer) {
        Path recipeDir = projectRoot().resolve("src").resolve("main").resolve("resources")
                .resolve("data").resolve(HbmNtm.MOD_ID).resolve("recipes").resolve("radgen");
        if (!Files.isDirectory(recipeDir)) {
            throw new IllegalStateException("Missing materialized RadGen recipe directory: " + recipeDir);
        }
        try (Stream<Path> paths = Files.list(recipeDir)) {
            paths.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(path -> materializedRadGenRecipe(consumer, recipeDir, path));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read materialized RadGen recipes from " + recipeDir,
                    exception);
        }
    }

    private static void materializedRadGenRecipe(Consumer<FinishedRecipe> consumer, Path recipeDir, Path path) {
        try {
            JsonElement element = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
            if (!element.isJsonObject()) {
                throw new IllegalStateException("Materialized RadGen recipe is not a JSON object: " + path);
            }
            JsonObject json = element.getAsJsonObject();
            String expectedType = HbmNtm.MOD_ID + ":radgen";
            if (!json.has("type") || !expectedType.equals(json.get("type").getAsString())) {
                throw new IllegalStateException("Materialized RadGen recipe has wrong type: " + path);
            }
            if (!json.has("source_order")) {
                throw new IllegalStateException("Materialized RadGen recipe has no source_order: " + path);
            }
            String idPath = recipeDir.relativize(path).toString().replace('\\', '/').replaceFirst("\\.json$", "");
            consumer.accept(finishedCompatRecipe(id("radgen/" + idPath), json));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read materialized RadGen recipe " + path, exception);
        }
    }

    private static void shredderRecipes(Consumer<FinishedRecipe> consumer) {
        Path recipeDir = projectRoot().resolve("src").resolve("main").resolve("resources")
                .resolve("data").resolve(HbmNtm.MOD_ID).resolve("recipes").resolve("shredder");
        if (!Files.isDirectory(recipeDir)) {
            throw new IllegalStateException("Missing materialized Shredder recipe directory: " + recipeDir);
        }
        try (Stream<Path> paths = Files.list(recipeDir)) {
            paths.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(path -> materializedShredderRecipe(consumer, recipeDir, path));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read materialized Shredder recipes from " + recipeDir,
                    exception);
        }
    }

    private static void materializedShredderRecipe(Consumer<FinishedRecipe> consumer, Path recipeDir, Path path) {
        try {
            JsonElement element = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
            if (!element.isJsonObject()) {
                throw new IllegalStateException("Materialized Shredder recipe is not a JSON object: " + path);
            }
            JsonObject json = element.getAsJsonObject();
            String expectedType = HbmNtm.MOD_ID + ":shredder";
            if (!json.has("type") || !expectedType.equals(json.get("type").getAsString())) {
                throw new IllegalStateException("Materialized Shredder recipe has wrong type: " + path);
            }
            if (!json.has("source_order")) {
                throw new IllegalStateException("Materialized Shredder recipe has no source_order: " + path);
            }
            String idPath = recipeDir.relativize(path).toString().replace('\\', '/').replaceFirst("\\.json$", "");
            consumer.accept(finishedCompatRecipe(id("shredder/" + idPath), json));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read materialized Shredder recipe " + path, exception);
        }
    }

    private static void silexRecipes(Consumer<FinishedRecipe> consumer) {
        Path recipeDir = projectRoot().resolve("src").resolve("main").resolve("resources")
                .resolve("data").resolve(HbmNtm.MOD_ID).resolve("recipes").resolve("silex");
        if (!Files.isDirectory(recipeDir)) {
            throw new IllegalStateException("Missing materialized SILEX recipe directory: " + recipeDir);
        }
        try (Stream<Path> paths = Files.list(recipeDir)) {
            paths.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(path -> materializedSilexRecipe(consumer, recipeDir, path));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read materialized SILEX recipes from " + recipeDir,
                    exception);
        }
    }

    private static void materializedSilexRecipe(Consumer<FinishedRecipe> consumer, Path recipeDir, Path path) {
        try {
            JsonElement element = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
            if (!element.isJsonObject()) {
                throw new IllegalStateException("Materialized SILEX recipe is not a JSON object: " + path);
            }
            JsonObject json = element.getAsJsonObject();
            String expectedType = HbmNtm.MOD_ID + ":silex";
            if (!json.has("type") || !expectedType.equals(json.get("type").getAsString())) {
                throw new IllegalStateException("Materialized SILEX recipe has wrong type: " + path);
            }
            if (!json.has("source_order")) {
                throw new IllegalStateException("Materialized SILEX recipe has no source_order: " + path);
            }
            String idPath = recipeDir.relativize(path).toString().replace('\\', '/').replaceFirst("\\.json$", "");
            consumer.accept(finishedCompatRecipe(id("silex/" + idPath), json));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read materialized SILEX recipe " + path, exception);
        }
    }

    private static void pedestalRecipes(Consumer<FinishedRecipe> consumer) {
        Path recipeDir = projectRoot().resolve("src").resolve("main").resolve("resources")
                .resolve("data").resolve(HbmNtm.MOD_ID).resolve("recipes").resolve("pedestal");
        if (!Files.isDirectory(recipeDir)) {
            throw new IllegalStateException("Missing materialized Pedestal recipe directory: " + recipeDir);
        }
        try (Stream<Path> paths = Files.list(recipeDir)) {
            paths.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(path -> materializedPedestalRecipe(consumer, recipeDir, path));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read materialized Pedestal recipes from " + recipeDir,
                    exception);
        }
    }

    private static void materializedPedestalRecipe(Consumer<FinishedRecipe> consumer, Path recipeDir, Path path) {
        try {
            JsonElement element = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
            if (!element.isJsonObject()) {
                throw new IllegalStateException("Materialized Pedestal recipe is not a JSON object: " + path);
            }
            JsonObject json = element.getAsJsonObject();
            String expectedType = HbmNtm.MOD_ID + ":pedestal";
            if (!json.has("type") || !expectedType.equals(json.get("type").getAsString())) {
                throw new IllegalStateException("Materialized Pedestal recipe has wrong type: " + path);
            }
            if (!json.has("source_order")) {
                throw new IllegalStateException("Materialized Pedestal recipe has no source_order: " + path);
            }
            String idPath = recipeDir.relativize(path).toString().replace('\\', '/').replaceFirst("\\.json$", "");
            consumer.accept(finishedCompatRecipe(id("pedestal/" + idPath), json));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read materialized Pedestal recipe " + path, exception);
        }
    }

    private static void radiolysisRecipes(Consumer<FinishedRecipe> consumer) {
        radiolysisRecipe(consumer, "water", HbmFluids.WATER, HbmFluids.PEROXIDE, 80,
                HbmFluids.HYDROGEN, 20, 0);
    }

    private static void oilProcessingRecipes(Consumer<FinishedRecipe> consumer) {
        refineryRecipes(consumer);
        vacuumDistillRecipes(consumer);
        fractionTowerRecipes(consumer);
        catalyticCrackerRecipes(consumer);
        catalyticReformerRecipes(consumer);
        hydrotreaterRecipes(consumer);
        solidifierRecipes(consumer);
        cokerRecipes(consumer);
    }

    private static void refineryRecipes(Consumer<FinishedRecipe> consumer) {
        int sourceOrder = 0;
        for (var entry : LegacyOilFluidRecipes.refineryRecipes()) {
            FluidType input = entry.getKey();
            LegacyOilFluidRecipes.RefineryRecipe recipe = entry.getValue();
            JsonObject json = oilProcessingBase(input, 100, sourceOrder++);
            HbmFluidStack[] outputs = recipe.outputs();
            for (int i = 0; i < outputs.length; i++) {
                json.add("output" + i, fluidStackJson(outputs[i]));
            }
            ItemStack solid = requireOilItemOutput(recipe.solidStack(), "refinery/" + input.toPath());
            json.add("solid", itemStackJson(solid));
            consumer.accept(finishedRecipe(id("refinery/" + input.toPath()), json,
                    ModRecipes.REFINERY.serializer().get()));
        }
    }

    private static void vacuumDistillRecipes(Consumer<FinishedRecipe> consumer) {
        int sourceOrder = 0;
        for (var entry : LegacyOilFluidRecipes.vacuumRecipes()) {
            FluidType input = entry.getKey();
            LegacyOilFluidRecipes.VacuumRecipe recipe = entry.getValue();
            JsonObject json = oilProcessingBase(input, 100, sourceOrder++);
            HbmFluidStack[] outputs = recipe.outputs();
            for (int i = 0; i < outputs.length; i++) {
                json.add("output" + i, fluidStackJson(outputs[i]));
            }
            consumer.accept(finishedRecipe(id("vacuum_distill/" + input.toPath()), json,
                    ModRecipes.VACUUM_DISTILL.serializer().get()));
        }
    }

    private static void fractionTowerRecipes(Consumer<FinishedRecipe> consumer) {
        int sourceOrder = 0;
        for (var entry : LegacyOilFluidRecipes.fractioningRecipes()) {
            FluidType input = entry.getKey();
            LegacyOilFluidRecipes.PairRecipe recipe = entry.getValue();
            JsonObject json = oilProcessingBase(input, 100, sourceOrder++);
            json.add("output1", fluidStackJson(recipe.left()));
            json.add("output2", fluidStackJson(recipe.right()));
            consumer.accept(finishedRecipe(id("fraction_tower/" + input.toPath()), json,
                    ModRecipes.FRACTION_TOWER.serializer().get()));
        }
    }

    private static void catalyticCrackerRecipes(Consumer<FinishedRecipe> consumer) {
        int sourceOrder = 0;
        for (var entry : LegacyOilFluidRecipes.crackingRecipes()) {
            FluidType input = entry.getKey();
            LegacyOilFluidRecipes.PairRecipe recipe = entry.getValue();
            JsonObject json = oilProcessingBase(input, 100, sourceOrder++);
            json.add("output1", fluidStackJson(recipe.left()));
            json.add("output2", fluidStackJson(recipe.right()));
            consumer.accept(finishedRecipe(id("catalytic_cracker/" + input.toPath()), json,
                    ModRecipes.CATALYTIC_CRACKER.serializer().get()));
        }
    }

    private static void catalyticReformerRecipes(Consumer<FinishedRecipe> consumer) {
        int sourceOrder = 0;
        for (var entry : LegacyOilFluidRecipes.reformingRecipes()) {
            FluidType input = entry.getKey();
            LegacyOilFluidRecipes.TripleRecipe recipe = entry.getValue();
            JsonObject json = oilProcessingBase(input, 100, sourceOrder++);
            json.add("output1", fluidStackJson(recipe.first()));
            json.add("output2", fluidStackJson(recipe.second()));
            json.add("output3", fluidStackJson(recipe.third()));
            consumer.accept(finishedRecipe(id("catalytic_reformer/" + input.toPath()), json,
                    ModRecipes.CATALYTIC_REFORMER.serializer().get()));
        }
    }

    private static void hydrotreaterRecipes(Consumer<FinishedRecipe> consumer) {
        int sourceOrder = 0;
        for (var entry : LegacyOilFluidRecipes.hydrotreatingRecipes()) {
            FluidType input = entry.getKey();
            LegacyOilFluidRecipes.TripleRecipe recipe = entry.getValue();
            JsonObject json = oilProcessingBase(input, 100, sourceOrder++);
            json.add("hydrogen", fluidStackJson(recipe.first()));
            json.add("output1", fluidStackJson(recipe.second()));
            json.add("output2", fluidStackJson(recipe.third()));
            consumer.accept(finishedRecipe(id("hydrotreater/" + input.toPath()), json,
                    ModRecipes.HYDROTREATER.serializer().get()));
        }
    }

    private static void solidifierRecipes(Consumer<FinishedRecipe> consumer) {
        int sourceOrder = 0;
        for (var entry : LegacyOilFluidRecipes.solidificationRecipes()) {
            FluidType input = entry.getKey();
            LegacyOilFluidRecipes.SolidificationRecipe recipe = entry.getValue();
            JsonObject json = oilProcessingBase(input, recipe.inputAmount(), sourceOrder++);
            ItemStack output = requireOilItemOutput(recipe.outputStack(), "solidifier/" + input.toPath());
            json.add("output", itemStackJson(output));
            consumer.accept(finishedRecipe(id("solidifier/" + input.toPath()), json,
                    ModRecipes.SOLIDIFIER.serializer().get()));
        }
    }

    private static void cokerRecipes(Consumer<FinishedRecipe> consumer) {
        int sourceOrder = 0;
        for (var entry : LegacyOilFluidRecipes.cokingRecipes()) {
            FluidType input = entry.getKey();
            LegacyOilFluidRecipes.CokerRecipe recipe = entry.getValue();
            JsonObject json = oilProcessingBase(input, recipe.inputAmount(), sourceOrder++);
            ItemStack output = recipe.outputStack();
            if (!output.isEmpty()) {
                json.add("output", itemStackJson(output));
            }
            if (recipe.byproduct() != null && !recipe.byproduct().isEmpty()) {
                json.add("byproduct", fluidStackJson(recipe.byproduct()));
            }
            if (output.isEmpty() && (recipe.byproduct() == null || recipe.byproduct().isEmpty())) {
                throw new IllegalStateException("Oil processing coker/" + input.toPath() + " has no output");
            }
            consumer.accept(finishedRecipe(id("coker/" + input.toPath()), json,
                    ModRecipes.COKER.serializer().get()));
        }
    }

    private static JsonObject oilProcessingBase(FluidType input, int amount, int sourceOrder) {
        JsonObject json = new JsonObject();
        json.add("input", fluidStackJson(input, amount));
        json.addProperty("source_order", sourceOrder);
        return json;
    }

    private static void radiolysisRecipe(Consumer<FinishedRecipe> consumer, String name, FluidType input,
            FluidType output1, int output1Amount, FluidType output2, int output2Amount, int sourceOrder) {
        JsonObject json = new JsonObject();
        json.add("input", fluidStackJson(input, 100));
        json.add("output1", fluidStackJson(output1, output1Amount));
        json.add("output2", fluidStackJson(output2, output2Amount));
        json.addProperty("source_order", sourceOrder);
        consumer.accept(finishedRecipe(id("radiolysis/" + name), json,
                ModRecipes.RADIOLYSIS.serializer().get()));
    }

    private static void arcWelderRecipes(Consumer<FinishedRecipe> consumer) {
        arcWelderRecipe(consumer, "motor", "arc.motor", 100, 400, "motor", 2, 0,
                builder -> builder.inputLegacyOre("plateSteel", 2)
                        .inputLegacyOre("wireDenseMingrade", 2));
        arcWelderRecipe(consumer, "lde_aluminium", "arc.lde.aluminium", 200, 5_000, "part_generic_lde", 1, 1,
                builder -> builder.inputLegacyOre("plateAluminum", 4)
                        .inputLegacyOre("ingotFiberglass", 4)
                        .inputLegacyOre("ingotAnyHardplastic", 1));
        arcWelderRecipe(consumer, "lde_titanium", "arc.lde.titanium", 200, 10_000, "part_generic_lde", 1, 2,
                builder -> builder.inputLegacyOre("plateTitanium", 2)
                        .inputLegacyOre("ingotFiberglass", 4)
                        .inputLegacyOre("ingotAnyHardplastic", 1));
        arcWelderRecipe(consumer, "neutron_reflector", "arc.neutronreflector", 400, 50_000,
                "neutron_reflector", 2, 3,
                builder -> builder.inputLegacyOre("ingotTungstenCarbide", 2)
                        .inputLegacyOre("plateDuraSteel", 1));

        arcWelderRecipe(consumer, "dense_wire_copper", "arc.wire.copper", 100, 10_000, "wire_dense_copper", 1, 4,
                builder -> builder.inputLegacyOre("wireFineCopper", 8));
        arcWelderRecipe(consumer, "dense_wire_mingrade", "arc.wire.mingrade", 100, 10_000,
                "wire_dense_mingrade", 1, 5,
                builder -> builder.inputLegacyOre("wireFineMingrade", 8));
        arcWelderRecipe(consumer, "dense_wire_gold", "arc.wire.gold", 100, 10_000, "wire_dense_gold", 1, 6,
                builder -> builder.inputLegacyOre("wireFineGold", 8));

        arcWelderRecipe(consumer, "welded_plate_iron", "arc.plate.iron", 100, 100, "plate_welded_iron", 1, 7,
                builder -> builder.inputLegacyOre("plateTripleIron", 2));
        arcWelderRecipe(consumer, "welded_plate_steel", "arc.plate.steel", 100, 500, "plate_welded_steel", 1, 8,
                builder -> builder.inputLegacyOre("plateTripleSteel", 2));
        arcWelderRecipe(consumer, "welded_plate_copper", "arc.plate.copper", 200, 1_000, "plate_welded_copper", 1, 9,
                builder -> builder.inputLegacyOre("plateTripleCopper", 2));
        arcWelderRecipe(consumer, "welded_plate_titanium", "arc.plate.titanium", 600, 50_000,
                "plate_welded_titanium", 1, 10,
                builder -> builder.inputLegacyOre("plateTripleTitanium", 2));
        arcWelderRecipe(consumer, "welded_plate_zirconium", "arc.plate.zirconium", 600, 10_000,
                "plate_welded_zirconium", 1, 11,
                builder -> builder.inputLegacyOre("plateTripleZirconium", 2));
        arcWelderRecipe(consumer, "welded_plate_aluminium", "arc.plate.aluminium", 300, 10_000,
                "plate_welded_aluminium", 1, 12,
                builder -> builder.inputLegacyOre("plateTripleAluminum", 2));
        arcWelderRecipe(consumer, "welded_plate_tcalloy", "arc.plate.tcalloy", 1_200, 1_000_000,
                "plate_welded_tcalloy", 1, 13,
                builder -> builder.inputLegacyOre("plateTripleTcAlloy", 2)
                        .inputFluid(HbmFluids.OXYGEN, 1_000));
        arcWelderRecipe(consumer, "welded_plate_cdalloy", "arc.plate.cdalloy", 1_200, 1_000_000,
                "plate_welded_cdalloy", 1, 14,
                builder -> builder.inputLegacyOre("plateTripleCdAlloy", 2)
                        .inputFluid(HbmFluids.OXYGEN, 1_000));
        arcWelderRecipe(consumer, "welded_plate_tungsten", "arc.plate.tungsten", 1_200, 250_000,
                "plate_welded_tungsten", 1, 15,
                builder -> builder.inputLegacyOre("plateTripleTungsten", 2)
                        .inputFluid(HbmFluids.OXYGEN, 1_000));
        arcWelderRecipe(consumer, "welded_plate_combine_steel", "arc.plate.cmb", 1_200, 10_000_000,
                "plate_welded_combine_steel", 1, 16,
                builder -> builder.inputLegacyOre("plateTripleCMBSteel", 2)
                        .inputFluid(HbmFluids.REFORMGAS, 1_000));
        arcWelderRecipe(consumer, "welded_plate_osmiridium", "arc.plate.osmiridium", 6_000, 50_000_000,
                "plate_welded_osmiridium", 1, 17,
                builder -> builder.inputLegacyOre("plateTripleOsmiridium", 2)
                        .inputFluid(HbmFluids.REFORMGAS, 16_000));

        arcWelderRecipe(consumer, "thruster_small", "arc.thruster.small", 60, 1_000, "thruster_small", 1, 18,
                builder -> builder.inputLegacyOre("plateSteel", 4)
                        .inputLegacyOre("wireFineAluminum", 4)
                        .inputLegacyOre("plateCopper", 4));
        arcWelderRecipe(consumer, "thruster_medium", "arc.thruster.medium", 100, 2_000, "thruster_medium", 1, 19,
                builder -> builder.inputLegacyOre("plateSteel", 8)
                        .inputItem(item("motor"), 1)
                        .inputLegacyOre("ingotGraphite", 8));
        arcWelderRecipe(consumer, "thruster_large", "arc.thruster.large", 200, 5_000, "thruster_large", 1, 20,
                builder -> builder.inputLegacyOre("ingotDuraSteel", 10)
                        .inputItem(item("motor"), 1)
                        .inputLegacyOre("ingotTungstenCarbide", 12));

        arcWelderRecipe(consumer, "fuel_tank_small", "arc.fueltank.small", 60, 1_000, "fuel_tank_small", 1, 21,
                builder -> builder.inputLegacyOre("plateAluminum", 6)
                        .inputLegacyOre("plateCopper", 4)
                        .inputItem(block("steel_scaffold"), 4));
        arcWelderRecipe(consumer, "fuel_tank_medium", "arc.fueltank.medium", 100, 2_000, "fuel_tank_medium", 1, 22,
                builder -> builder.inputLegacyOre("plateTripleAluminum", 4)
                        .inputLegacyOre("plateTitanium", 8)
                        .inputItem(block("steel_scaffold"), 12));
        arcWelderRecipe(consumer, "fuel_tank_large", "arc.fueltank.large", 200, 5_000, "fuel_tank_large", 1, 23,
                builder -> builder.inputLegacyOre("plateSextupleAluminum", 8)
                        .inputLegacyOre("plateSaturnite", 12)
                        .inputItem(block("steel_scaffold"), 16));

        arcWelderRecipe(consumer, "missile_anti_ballistic", "arc.missile.abm", 100, 5_000,
                "missile_anti_ballistic", 1, 24,
                builder -> builder.inputLegacyOre("ingotAnyHighexplosive", 3)
                        .inputItem(item("missile_assembly"), 1)
                        .inputItem(item("thruster_small"), 4));
        arcWelderRecipe(consumer, "missile_generic", "arc.missile.generic", 100, 5_000, "missile_generic", 1, 25,
                builder -> builder.inputItem(item("warhead_generic_small"), 1)
                        .inputItem(item("fuel_tank_small"), 1)
                        .inputItem(item("thruster_small"), 1));
        arcWelderRecipe(consumer, "missile_incendiary", "arc.missile.incendiary", 100, 5_000,
                "missile_incendiary", 1, 26,
                builder -> builder.inputItem(item("warhead_incendiary_small"), 1)
                        .inputItem(item("fuel_tank_small"), 1)
                        .inputItem(item("thruster_small"), 1));
        arcWelderRecipe(consumer, "missile_cluster", "arc.missile.cluster", 100, 5_000, "missile_cluster", 1, 27,
                builder -> builder.inputItem(item("warhead_cluster_small"), 1)
                        .inputItem(item("fuel_tank_small"), 1)
                        .inputItem(item("thruster_small"), 1));
        arcWelderRecipe(consumer, "missile_buster", "arc.missile.buster", 100, 5_000, "missile_buster", 1, 28,
                builder -> builder.inputItem(item("warhead_buster_small"), 1)
                        .inputItem(item("fuel_tank_small"), 1)
                        .inputItem(item("thruster_small"), 1));
        arcWelderRecipe(consumer, "missile_decoy", "arc.missile.decoy", 60, 2_500, "missile_decoy", 1, 29,
                builder -> builder.inputLegacyOre("ingotSteel", 1)
                        .inputItem(item("fuel_tank_small"), 1)
                        .inputItem(item("thruster_small"), 1));

        arcWelderRecipe(consumer, "missile_strong", "arc.missile.strong", 200, 10_000, "missile_strong", 1, 30,
                builder -> builder.inputItem(item("warhead_generic_medium"), 1)
                        .inputItem(item("fuel_tank_medium"), 1)
                        .inputItem(item("thruster_medium"), 1));
        arcWelderRecipe(consumer, "missile_incendiary_strong", "arc.missile.incendiarystrong", 200, 10_000,
                "missile_incendiary_strong", 1, 31,
                builder -> builder.inputItem(item("warhead_incendiary_medium"), 1)
                        .inputItem(item("fuel_tank_medium"), 1)
                        .inputItem(item("thruster_medium"), 1));
        arcWelderRecipe(consumer, "missile_cluster_strong", "arc.missile.clusterstrong", 200, 10_000,
                "missile_cluster_strong", 1, 32,
                builder -> builder.inputItem(item("warhead_cluster_medium"), 1)
                        .inputItem(item("fuel_tank_medium"), 1)
                        .inputItem(item("thruster_medium"), 1));
        arcWelderRecipe(consumer, "missile_buster_strong", "arc.missile.busterstrong", 200, 10_000,
                "missile_buster_strong", 1, 33,
                builder -> builder.inputItem(item("warhead_buster_medium"), 1)
                        .inputItem(item("fuel_tank_medium"), 1)
                        .inputItem(item("thruster_medium"), 1));
        arcWelderRecipe(consumer, "missile_emp_strong", "arc.missile.empstrong", 200, 10_000,
                "missile_emp_strong", 1, 34,
                builder -> builder.inputItem(block("emp_bomb"), 3)
                        .inputItem(item("fuel_tank_medium"), 1)
                        .inputItem(item("thruster_medium"), 1));
        arcWelderRecipe(consumer, "missile_burst", "arc.missile.burst", 300, 25_000, "missile_burst", 1, 35,
                builder -> builder.inputItem(item("warhead_generic_large"), 1)
                        .inputItem(item("fuel_tank_medium"), 2)
                        .inputItem(item("thruster_medium"), 4));
        arcWelderRecipe(consumer, "missile_inferno", "arc.missile.inferno", 300, 25_000, "missile_inferno", 1, 36,
                builder -> builder.inputItem(item("warhead_incendiary_large"), 1)
                        .inputItem(item("fuel_tank_medium"), 2)
                        .inputItem(item("thruster_medium"), 4));
        arcWelderRecipe(consumer, "missile_rain", "arc.missile.rain", 300, 25_000, "missile_rain", 1, 37,
                builder -> builder.inputItem(item("warhead_cluster_large"), 1)
                        .inputItem(item("fuel_tank_medium"), 2)
                        .inputItem(item("thruster_medium"), 4));
        arcWelderRecipe(consumer, "missile_drill", "arc.missile.drill", 300, 25_000, "missile_drill", 1, 38,
                builder -> builder.inputItem(item("warhead_buster_large"), 1)
                        .inputItem(item("fuel_tank_medium"), 2)
                        .inputItem(item("thruster_medium"), 4));

        arcWelderRecipe(consumer, "missile_nuclear", "arc.missile.nuclear", 600, 50_000, "missile_nuclear", 1, 39,
                builder -> builder.inputItem(item("warhead_nuclear"), 1)
                        .inputItem(item("fuel_tank_large"), 1)
                        .inputItem(item("thruster_large"), 3));
        arcWelderRecipe(consumer, "missile_nuclear_cluster", "arc.missile.nuclearcluster", 600, 50_000,
                "missile_nuclear_cluster", 1, 40,
                builder -> builder.inputItem(item("warhead_mirv"), 1)
                        .inputItem(item("fuel_tank_large"), 1)
                        .inputItem(item("thruster_large"), 3));
        arcWelderRecipe(consumer, "missile_volcano", "arc.missile.volcano", 600, 50_000, "missile_volcano", 1, 41,
                builder -> builder.inputItem(item("warhead_volcano"), 1)
                        .inputItem(item("fuel_tank_large"), 1)
                        .inputItem(item("thruster_large"), 3));

        arcWelderRecipe(consumer, "satellite_mapper", "arc.satellitemapper", 600, 10_000, "sat_mapper", 1, 42,
                builder -> builder.inputItem(item("sat_base"), 1)
                        .inputItem(item("sat_head_mapper"), 1));
        arcWelderRecipe(consumer, "satellite_scanner", "arc.satellitescanner", 600, 10_000, "sat_scanner", 1, 43,
                builder -> builder.inputItem(item("sat_base"), 1)
                        .inputItem(item("sat_head_scanner"), 1));
        arcWelderRecipe(consumer, "satellite_radar", "arc.satelliteradar", 600, 10_000, "sat_radar", 1, 44,
                builder -> builder.inputItem(item("sat_base"), 1)
                        .inputItem(item("sat_head_radar"), 1));
        arcWelderRecipe(consumer, "satellite_laser", "arc.satellitelaser", 600, 50_000, "sat_laser", 1, 45,
                builder -> builder.inputItem(item("sat_base"), 1)
                        .inputItem(item("sat_head_laser"), 1));
        arcWelderRecipe(consumer, "satellite_resonator", "arc.satelliteresonator", 600, 50_000,
                "sat_resonator", 1, 46,
                builder -> builder.inputItem(item("sat_base"), 1)
                        .inputItem(item("sat_head_resonator"), 1));
    }

    private static void arcWelderRecipe(Consumer<FinishedRecipe> consumer, String path, String internalName,
            int duration, long power, String outputItem, int outputCount, int sourceOrder,
            Consumer<GenericMachineRecipeBuilder> configure) {
        GenericMachineRecipeBuilder builder = GenericMachineRecipeBuilder.arcWelder(internalName, duration, power)
                .outputItem(new ItemStack(item(outputItem), outputCount))
                .sourceOrder(sourceOrder);
        configure.accept(builder);
        builder.save(consumer, id("arc_welder/" + path));
    }

    private static void centrifugeRecipes(Consumer<FinishedRecipe> consumer) {
        centrifuge(consumer, "chunk_ore_rare", HbmIngredient.legacyMeta(LegacyMetaItemMappings.CHUNK_ORE, 0, 1), 0,
                out("powder_cobalt_tiny", 2), out("powder_boron_tiny", 2), out("powder_niobium_tiny", 2),
                out("nugget_zirconium", 3));
        centrifuge(consumer, "coal_ore", HbmIngredient.legacyOre("oreCoal", 1), 1,
                out("powder_coal", 2), out("powder_coal", 2), out("powder_coal", 2),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "lignite_ore", HbmIngredient.legacyOre("oreLignite", 1), 2,
                out("powder_lignite", 2), out("powder_lignite", 2), out("powder_lignite", 2),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "iron_ore", HbmIngredient.legacyOre("oreIron", 1), 3,
                out("powder_iron", 1), out("powder_iron", 1), out("powder_iron", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "gold_ore", HbmIngredient.legacyOre("oreGold", 1), 4,
                out("powder_gold", 1), out("powder_gold", 1), out("powder_gold", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "diamond_ore", HbmIngredient.legacyOre("oreDiamond", 1), 5,
                out("powder_diamond", 1), out("powder_diamond", 1), out("powder_diamond", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "emerald_ore", HbmIngredient.legacyOre("oreEmerald", 1), 6,
                out("powder_emerald", 1), out("powder_emerald", 1), out("powder_emerald", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "titanium_ore", HbmIngredient.legacyOre("oreTitanium", 1), 7,
                out("powder_titanium", 1), out("powder_titanium", 1), out("powder_iron", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "quartz_ore", HbmIngredient.legacyOre("oreNetherQuartz", 1), 8,
                out("powder_quartz", 1), out("powder_quartz", 1), out("powder_lithium_tiny", 1),
                new ItemStack(Blocks.NETHERRACK));
        centrifuge(consumer, "tungsten_ore", HbmIngredient.legacyOre("oreTungsten", 1), 9,
                out("powder_tungsten", 1), out("powder_tungsten", 1), out("powder_iron", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "copper_ore", HbmIngredient.legacyOre("oreCopper", 1), 10,
                out("powder_copper", 1), out("powder_copper", 1), out("powder_gold", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "aluminium_ore", HbmIngredient.legacyOre("oreAluminum", 1), 11,
                out("chunk_ore_cryolite", 2), out("powder_titanium", 1), out("powder_iron", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "lead_ore", HbmIngredient.legacyOre("oreLead", 1), 12,
                out("powder_lead", 1), out("powder_lead", 1), out("powder_gold", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "schrabidium_ore", HbmIngredient.legacyOre("oreSchrabidium", 1), 13,
                out("powder_schrabidium", 1), out("powder_schrabidium", 1), out("nugget_solinium", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "rare_earth_ore", HbmIngredient.legacyOre("oreRareEarth", 1), 14,
                out("powder_desh_mix", 1), out("nugget_zirconium", 1), out("nugget_zirconium", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "plutonium_ore", HbmIngredient.legacyOre("orePlutonium", 1), 15,
                out("powder_plutonium", 1), out("powder_plutonium", 1), out("nugget_polonium", 3),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "uranium_ore", HbmIngredient.legacyOre("oreUranium", 1), 16,
                out("powder_uranium", 1), out("powder_uranium", 1), out("nugget_ra226", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "thorium_ore", HbmIngredient.legacyOre("oreThorium", 1), 17,
                out("powder_thorium", 1), out("powder_thorium", 1), out("powder_uranium", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "beryllium_ore", HbmIngredient.legacyOre("oreBeryllium", 1), 18,
                out("powder_beryllium", 1), out("powder_beryllium", 1), out("powder_emerald", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "fluorite_ore", HbmIngredient.legacyOre("oreFluorite", 1), 19,
                out("fluorite", 3), out("fluorite", 3), out("gem_sodalite", 1), new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "redstone_ore", HbmIngredient.legacyOre("oreRedstone", 1), 20,
                new ItemStack(Items.REDSTONE, 3), new ItemStack(Items.REDSTONE, 3), out("ingot_mercury", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "tikite_ore", HbmIngredient.of(item("ore_tikite"), 1), 21,
                out("powder_plutonium", 1), out("powder_cobalt", 2), out("powder_niobium", 2),
                new ItemStack(Blocks.END_STONE));
        centrifuge(consumer, "lapis_ore", HbmIngredient.legacyOre("oreLapis", 1), 22,
                out("powder_lapis", 6), out("powder_cobalt_tiny", 1), out("gem_sodalite", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "block_euphemium_cluster", HbmIngredient.of(item("block_euphemium_cluster"), 1), 23,
                out("nugget_euphemium", 7), out("powder_schrabidium", 4), out("ingot_starmetal", 2),
                out("nugget_solinium", 2));
        centrifuge(consumer, "nether_fire_ore", HbmIngredient.of(item("ore_nether_fire"), 1), 24,
                new ItemStack(Items.BLAZE_POWDER, 2), out("powder_fire", 2), out("ingot_phosphorus", 1),
                new ItemStack(Blocks.NETHERRACK));
        centrifuge(consumer, "cobalt_ore", HbmIngredient.legacyOre("oreCobalt", 1), 25,
                out("powder_cobalt", 2), out("powder_iron", 1), out("powder_copper", 1),
                new ItemStack(Blocks.GRAVEL));
        centrifuge(consumer, "powder_tektite", HbmIngredient.of(item("powder_tektite"), 1), 26,
                out("powder_meteorite_tiny", 1), out("powder_paleogenite_tiny", 1),
                out("powder_meteorite_tiny", 1), out("dust", 6));
        centrifuge(consumer, "block_slag", HbmIngredient.of(item("block_slag"), 1), 27,
                new ItemStack(Blocks.GRAVEL), out("powder_fire", 1), out("powder_calcium", 1), out("dust", 1));
        centrifuge(consumer, "powder_ash_coal", HbmIngredient.of(item("powder_ash_coal"), 1), 28,
                out("powder_coal_tiny", 2), out("powder_boron_tiny", 1), out("dust_tiny", 6));

        bedrockOreCentrifugeRecipes(consumer, 56);

        centrifuge(consumer, "blaze_rod", HbmIngredient.of(Items.BLAZE_ROD, 1), 29,
                new ItemStack(Items.BLAZE_POWDER), new ItemStack(Items.BLAZE_POWDER), out("powder_fire", 1),
                out("powder_fire", 1));
        centrifuge(consumer, "crystal_coal", legacyHbmItem("crystal_coal"), 30,
                out("powder_coal", 3), out("powder_coal", 3), out("powder_coal", 3),
                out("powder_lithium_tiny", 1));
        centrifuge(consumer, "crystal_iron", legacyHbmItem("crystal_iron"), 31,
                out("powder_iron", 2), out("powder_iron", 2), out("powder_titanium", 1),
                out("powder_lithium_tiny", 1));
        centrifuge(consumer, "crystal_gold", legacyHbmItem("crystal_gold"), 32,
                out("powder_gold", 2), out("powder_gold", 2), out("ingot_mercury", 1),
                out("powder_lithium_tiny", 1));
        centrifuge(consumer, "crystal_redstone", legacyHbmItem("crystal_redstone"), 33,
                new ItemStack(Items.REDSTONE, 3), new ItemStack(Items.REDSTONE, 3),
                new ItemStack(Items.REDSTONE, 3), out("ingot_mercury", 3));
        centrifuge(consumer, "crystal_lapis", legacyHbmItem("crystal_lapis"), 34,
                out("powder_lapis", 4), out("powder_lapis", 4), out("powder_cobalt", 1),
                out("gem_sodalite", 2));
        centrifuge(consumer, "crystal_diamond", legacyHbmItem("crystal_diamond"), 35,
                out("powder_diamond", 1), out("powder_diamond", 1), out("powder_diamond", 1),
                out("powder_diamond", 1));
        centrifuge(consumer, "crystal_uranium", legacyHbmItem("crystal_uranium"), 36,
                out("powder_uranium", 2), out("powder_uranium", 2), out("nugget_ra226", 2),
                out("powder_lithium_tiny", 1));
        centrifuge(consumer, "crystal_thorium", legacyHbmItem("crystal_thorium"), 37,
                out("powder_thorium", 2), out("powder_thorium", 2), out("powder_uranium", 1),
                out("nugget_ra226", 1));
        centrifuge(consumer, "crystal_plutonium", legacyHbmItem("crystal_plutonium"), 38,
                out("powder_plutonium", 2), out("powder_plutonium", 2), out("powder_polonium", 1),
                out("powder_lithium_tiny", 1));
        centrifuge(consumer, "crystal_titanium", legacyHbmItem("crystal_titanium"), 39,
                out("powder_titanium", 2), out("powder_titanium", 2), out("powder_iron", 1),
                out("powder_lithium_tiny", 1));
        centrifuge(consumer, "crystal_sulfur", legacyHbmItem("crystal_sulfur"), 40,
                out("sulfur", 4), out("sulfur", 4), out("powder_iron", 1), out("ingot_mercury", 1));
        centrifuge(consumer, "crystal_niter", legacyHbmItem("crystal_niter"), 41,
                out("niter", 3), out("niter", 3), out("niter", 3), out("powder_lithium_tiny", 1));
        centrifuge(consumer, "crystal_copper", legacyHbmItem("crystal_copper"), 42,
                out("powder_copper", 2), out("powder_copper", 2), out("sulfur", 1),
                out("powder_cobalt_tiny", 1));
        centrifuge(consumer, "crystal_tungsten", legacyHbmItem("crystal_tungsten"), 43,
                out("powder_tungsten", 2), out("powder_tungsten", 2), out("powder_iron", 1),
                out("powder_lithium_tiny", 1));
        centrifuge(consumer, "crystal_aluminium", legacyHbmItem("crystal_aluminium"), 44,
                out("chunk_ore_cryolite", 3), out("powder_titanium", 1), out("powder_iron", 1),
                out("powder_lithium_tiny", 1));
        centrifuge(consumer, "crystal_fluorite", legacyHbmItem("crystal_fluorite"), 45,
                out("fluorite", 4), out("fluorite", 4), out("gem_sodalite", 2),
                out("powder_lithium_tiny", 1));
        centrifuge(consumer, "crystal_beryllium", legacyHbmItem("crystal_beryllium"), 46,
                out("powder_beryllium", 2), out("powder_beryllium", 2), out("powder_quartz", 1),
                out("powder_lithium_tiny", 1));
        centrifuge(consumer, "crystal_lead", legacyHbmItem("crystal_lead"), 47,
                out("powder_lead", 2), out("powder_lead", 2), out("powder_gold", 1),
                out("powder_lithium_tiny", 1));
        centrifuge(consumer, "crystal_schraranium", legacyHbmItem("crystal_schraranium"), 48,
                out("nugget_schrabidium", 2), out("nugget_schrabidium", 2), out("nugget_uranium", 2),
                out("nugget_neptunium", 2));
        centrifuge(consumer, "crystal_schrabidium", legacyHbmItem("crystal_schrabidium"), 49,
                out("powder_schrabidium", 2), out("powder_schrabidium", 2), out("powder_plutonium", 1),
                out("powder_lithium_tiny", 1));
        centrifuge(consumer, "crystal_rare", legacyHbmItem("crystal_rare"), 50,
                out("powder_desh_mix", 1), out("powder_desh_mix", 1), out("nugget_zirconium", 2),
                out("nugget_zirconium", 2));
        centrifuge(consumer, "crystal_phosphorus", legacyHbmItem("crystal_phosphorus"), 51,
                out("powder_fire", 3), out("powder_fire", 3), out("ingot_phosphorus", 2),
                new ItemStack(Items.BLAZE_POWDER, 2));
        centrifuge(consumer, "crystal_trixite", legacyHbmItem("crystal_trixite"), 52,
                out("powder_plutonium", 2), out("powder_cobalt", 3), out("powder_niobium", 2),
                out("powder_nitan_mix", 1));
        centrifuge(consumer, "crystal_lithium", legacyHbmItem("crystal_lithium"), 53,
                out("powder_lithium", 2), out("powder_lithium", 2), out("powder_quartz", 1),
                out("fluorite", 1));
        centrifuge(consumer, "crystal_starmetal", legacyHbmItem("crystal_starmetal"), 54,
                out("powder_dura_steel", 3), out("powder_cobalt", 3), out("powder_astatine", 2),
                out("ingot_mercury", 5));
        centrifuge(consumer, "crystal_cobalt", legacyHbmItem("crystal_cobalt"), 55,
                out("powder_cobalt", 2), out("powder_iron", 3), out("powder_copper", 3),
                out("powder_lithium_tiny", 1));
    }

    private static void centrifuge(Consumer<FinishedRecipe> consumer, String name, HbmIngredient input,
            int sourceOrder, ItemStack... outputs) {
        JsonObject json = CompatRecipeRegistry.createCentrifuge(input, outputs);
        json.addProperty("source_order", sourceOrder);
        consumer.accept(finishedRecipe(id("centrifuge/" + name), json, ModRecipes.CENTRIFUGE.serializer().get()));
    }

    private static void bedrockOreCentrifugeRecipes(Consumer<FinishedRecipe> consumer, int sourceOrderStart) {
        int sourceOrder = sourceOrderStart;
        for (BedrockOreType type : BedrockOreType.values()) {
            String suffix = type.suffix();
            centrifuge(consumer, "bedrock_ore_" + suffix + "_base",
                    bedrockOreInput(BedrockOreGrade.BASE, type), sourceOrder++,
                    bedrockOre(BedrockOreGrade.PRIMARY, type), new ItemStack(Blocks.GRAVEL));
            centrifuge(consumer, "bedrock_ore_" + suffix + "_base_roasted",
                    bedrockOreInput(BedrockOreGrade.BASE_ROASTED, type), sourceOrder++,
                    bedrockOre(BedrockOreGrade.PRIMARY, type), new ItemStack(Blocks.GRAVEL));
            centrifuge(consumer, "bedrock_ore_" + suffix + "_base_washed",
                    bedrockOreInput(BedrockOreGrade.BASE_WASHED, type), sourceOrder++,
                    bedrockOre(BedrockOreGrade.PRIMARY, type), bedrockOre(BedrockOreGrade.PRIMARY, type),
                    new ItemStack(Blocks.GRAVEL));
            centrifuge(consumer, "bedrock_ore_" + suffix + "_primary_sulfuric",
                    bedrockOreInput(BedrockOreGrade.PRIMARY_SULFURIC, type), sourceOrder++,
                    bedrockOre(BedrockOreGrade.PRIMARY_NOSULFURIC, type, 2),
                    bedrockOre(BedrockOreGrade.SULFURIC_BYPRODUCT, type, 2));
            centrifuge(consumer, "bedrock_ore_" + suffix + "_primary_solvent",
                    bedrockOreInput(BedrockOreGrade.PRIMARY_SOLVENT, type), sourceOrder++,
                    bedrockOre(BedrockOreGrade.PRIMARY_NOSOLVENT, type, 2),
                    bedrockOre(BedrockOreGrade.SULFURIC_BYPRODUCT, type, 2),
                    bedrockOre(BedrockOreGrade.SOLVENT_BYPRODUCT, type, 2));
            centrifuge(consumer, "bedrock_ore_" + suffix + "_primary_rad",
                    bedrockOreInput(BedrockOreGrade.PRIMARY_RAD, type), sourceOrder++,
                    bedrockOre(BedrockOreGrade.PRIMARY_NORAD, type, 2),
                    bedrockOre(BedrockOreGrade.SULFURIC_BYPRODUCT, type, 2),
                    bedrockOre(BedrockOreGrade.SOLVENT_BYPRODUCT, type, 2),
                    bedrockOre(BedrockOreGrade.RAD_BYPRODUCT, type, 2));
            BedrockOreProducts products = bedrockOreProducts(type);
            centrifuge(consumer, "bedrock_ore_" + suffix + "_primary",
                    bedrockOreInput(BedrockOreGrade.PRIMARY, type), sourceOrder++,
                    bedrockOreFragment(products.primary1(), 1), bedrockOreFragment(products.primary2(), 1));
            centrifuge(consumer, "bedrock_ore_" + suffix + "_primary_roasted",
                    bedrockOreInput(BedrockOreGrade.PRIMARY_ROASTED, type), sourceOrder++,
                    bedrockOreFragment(products.primary1(), 1), bedrockOreFragment(products.primary2(), 1));
            centrifuge(consumer, "bedrock_ore_" + suffix + "_primary_nosulfuric",
                    bedrockOreInput(BedrockOreGrade.PRIMARY_NOSULFURIC, type), sourceOrder++,
                    bedrockOreFragment(products.primary1(), 1), bedrockOreFragment(products.primary2(), 1),
                    bedrockOre(BedrockOreGrade.CRUMBS, type));
            centrifuge(consumer, "bedrock_ore_" + suffix + "_primary_nosolvent",
                    bedrockOreInput(BedrockOreGrade.PRIMARY_NOSOLVENT, type), sourceOrder++,
                    bedrockOreFragment(products.primary1(), 1), bedrockOreFragment(products.primary2(), 1),
                    bedrockOre(BedrockOreGrade.CRUMBS, type));
            centrifuge(consumer, "bedrock_ore_" + suffix + "_primary_norad",
                    bedrockOreInput(BedrockOreGrade.PRIMARY_NORAD, type), sourceOrder++,
                    bedrockOreFragment(products.primary1(), 1), bedrockOreFragment(products.primary2(), 1),
                    bedrockOre(BedrockOreGrade.CRUMBS, type));
            centrifuge(consumer, "bedrock_ore_" + suffix + "_primary_first",
                    bedrockOreInput(BedrockOreGrade.PRIMARY_FIRST, type), sourceOrder++,
                    bedrockOreFragment(products.primary1(), 1), bedrockOreFragment(products.primary1(), 1),
                    bedrockOreFragment(products.primary2(), 1), bedrockOre(BedrockOreGrade.CRUMBS, type));
            centrifuge(consumer, "bedrock_ore_" + suffix + "_primary_second",
                    bedrockOreInput(BedrockOreGrade.PRIMARY_SECOND, type), sourceOrder++,
                    bedrockOreFragment(products.primary1(), 1), bedrockOreFragment(products.primary2(), 1),
                    bedrockOreFragment(products.primary2(), 1), bedrockOre(BedrockOreGrade.CRUMBS, type));
            centrifuge(consumer, "bedrock_ore_" + suffix + "_sulfuric_washed",
                    bedrockOreInput(BedrockOreGrade.SULFURIC_WASHED, type), sourceOrder++,
                    bedrockOreFragment(products.byproductAcid1(), 1),
                    bedrockOreFragment(products.byproductAcid2(), 1),
                    bedrockOreFragment(products.byproductAcid3(), 1),
                    bedrockOre(BedrockOreGrade.CRUMBS, type));
            centrifuge(consumer, "bedrock_ore_" + suffix + "_solvent_washed",
                    bedrockOreInput(BedrockOreGrade.SOLVENT_WASHED, type), sourceOrder++,
                    bedrockOreFragment(products.byproductSolvent1(), 1),
                    bedrockOreFragment(products.byproductSolvent2(), 1),
                    bedrockOreFragment(products.byproductSolvent3(), 1),
                    bedrockOre(BedrockOreGrade.CRUMBS, type));
            centrifuge(consumer, "bedrock_ore_" + suffix + "_rad_washed",
                    bedrockOreInput(BedrockOreGrade.RAD_WASHED, type), sourceOrder++,
                    bedrockOreFragment(products.byproductRad1(), 1),
                    bedrockOreFragment(products.byproductRad2(), 1),
                    bedrockOreFragment(products.byproductRad3(), 1),
                    bedrockOre(BedrockOreGrade.CRUMBS, type));
        }
    }

    private static HbmIngredient bedrockOreInput(BedrockOreGrade grade, BedrockOreType type) {
        return bedrockOreInput(grade, type, 1);
    }

    private static HbmIngredient bedrockOreInput(BedrockOreGrade grade, BedrockOreType type, int amount) {
        return HbmIngredient.partialNbt(BedrockOreItem.make(grade, type, amount));
    }

    private static ItemStack bedrockOre(BedrockOreGrade grade, BedrockOreType type) {
        return bedrockOre(grade, type, 1);
    }

    private static ItemStack bedrockOre(BedrockOreGrade grade, BedrockOreType type, int amount) {
        return BedrockOreItem.make(grade, type, amount);
    }

    private static BedrockOreOutput bo(NTMMaterial material, int amount) {
        return new BedrockOreOutput(material, amount);
    }

    private static BedrockOreProducts bedrockOreProducts(BedrockOreType type) {
        for (BedrockOreProducts products : BEDROCK_ORE_PRODUCTS) {
            if (products.type() == type) {
                return products;
            }
        }
        throw new IllegalStateException("Missing new bedrock ore output mapping for " + type);
    }

    private static ItemStack bedrockOreFragment(BedrockOreOutput output, double multiplier) {
        int count = Math.min((int) Math.ceil(output.amount() * multiplier), 64);
        return BedrockOreFragmentItem.make(output.material(), count);
    }

    private static HbmIngredient bedrockOreFragmentInput(NTMMaterial material, int count) {
        return HbmIngredient.partialNbt(BedrockOreFragmentItem.make(material, count));
    }

    @Nullable
    private static MaterialStack bedrockOreFluid(BedrockOreOutput output, double multiplier) {
        if (output.material().smeltable != SmeltingBehavior.SMELTABLE) {
            return null;
        }
        int amount = (int) Math.ceil(MaterialShapes.FRAGMENT.q(output.amount()) * multiplier);
        return mat(output.material(), amount);
    }

    private record BedrockOreOutput(NTMMaterial material, int amount) {
    }

    private record BedrockOreProducts(BedrockOreType type, BedrockOreOutput primary1,
                                      BedrockOreOutput primary2, BedrockOreOutput byproductAcid1,
                                      BedrockOreOutput byproductAcid2, BedrockOreOutput byproductAcid3,
                                      BedrockOreOutput byproductSolvent1,
                                      BedrockOreOutput byproductSolvent2,
                                      BedrockOreOutput byproductSolvent3, BedrockOreOutput byproductRad1,
                                      BedrockOreOutput byproductRad2, BedrockOreOutput byproductRad3) {
    }

    private record BedrockOreProductEntry(@Nullable BedrockOreOutput output, double multiplier, ItemStack stack) {
        private BedrockOreProductEntry(BedrockOreOutput output, double multiplier) {
            this(output, multiplier, ItemStack.EMPTY);
        }

        private BedrockOreProductEntry(ItemStack stack) {
            this(null, 0.0D, stack.copy());
        }
    }

    private record BedrockOreElectrolyzerProduct(MaterialStack output1, @Nullable MaterialStack output2,
                                                 List<ItemStack> byproducts) {
    }

    private static void gasCentRecipes(Consumer<FinishedRecipe> consumer) {
        gasCent(consumer, "uf6_full_chain", HbmFluids.UF6, 1_200, true, 4, PseudoFluidType.NUF6,
                PseudoFluidType.NONE, 0,
                out("nugget_u238", 11), out("nugget_u235", 1), out("fluorite", 4));
        gasCent(consumer, "uf6_fuel_chain", HbmFluids.UF6, 1_200, false, 2, PseudoFluidType.LEUF6,
                PseudoFluidType.NONE, 1,
                out("nugget_u238", 6), out("nugget_uranium_fuel", 6), out("fluorite", 4));
        gasCent(consumer, "puf6", HbmFluids.PUF6, 900, false, 1, PseudoFluidType.PF6,
                PseudoFluidType.NONE, 2,
                out("nugget_pu238", 3), out("nugget_pu_mix", 6), out("fluorite", 3));
        gasCent(consumer, "watz_mud", HbmFluids.WATZ, 1_000, false, 2, PseudoFluidType.MUD,
                PseudoFluidType.NONE, 3,
                out("powder_iron", 1), out("powder_lead", 1), out("nuclear_waste_tiny", 1), out("dust", 2));
    }

    private static void gasCent(Consumer<FinishedRecipe> consumer, String name, FluidType input, int amount,
            boolean highSpeed, int centrifuges, PseudoFluidType inputType, PseudoFluidType outputType,
            int sourceOrder, ItemStack... outputs) {
        JsonObject json = new JsonObject();
        json.add("input", fluidStackJson(input, amount));
        JsonArray outputArray = new JsonArray();
        for (ItemStack output : outputs) {
            outputArray.add(itemStackJson(output));
        }
        json.add("outputs", outputArray);
        json.addProperty("high_speed", highSpeed);
        json.addProperty("centrifuges", centrifuges);
        json.addProperty("input_type", inputType.legacyName());
        json.addProperty("output_type", outputType.legacyName());
        json.addProperty("source_order", sourceOrder);
        consumer.accept(finishedRecipe(id("gas_cent/" + name), json, ModRecipes.GAS_CENT.serializer().get()));
    }

    private static void crystallizerRecipes(Consumer<FinishedRecipe> consumer) {
        int baseTime = 600;
        int utilityTime = 100;
        int mixingTime = 20;

        crystallizer(consumer, "coal_ore", HbmIngredient.legacyOre("oreCoal", 1),
                item("crystal_coal"), baseTime, 0.05F, 0);
        crystallizer(consumer, "iron_ore", HbmIngredient.legacyOre("oreIron", 1),
                item("crystal_iron"), baseTime, 0.05F, 1);
        crystallizer(consumer, "gold_ore", HbmIngredient.legacyOre("oreGold", 1),
                item("crystal_gold"), baseTime, 0.05F, 2);
        crystallizer(consumer, "redstone_ore", HbmIngredient.legacyOre("oreRedstone", 1),
                item("crystal_redstone"), baseTime, 0.05F, 3);
        crystallizer(consumer, "lapis_ore", HbmIngredient.legacyOre("oreLapis", 1),
                item("crystal_lapis"), baseTime, 0.05F, 4);
        crystallizer(consumer, "diamond_ore", HbmIngredient.legacyOre("oreDiamond", 1),
                item("crystal_diamond"), baseTime, 0.05F, 5);
        crystallizer(consumer, "uranium_ore", HbmIngredient.legacyOre("oreUranium", 1),
                item("crystal_uranium"), baseTime, HbmFluids.SULFURIC_ACID, 500, 0.05F, 6);
        crystallizer(consumer, "thorium_ore", HbmIngredient.legacyOre("oreThorium", 1),
                item("crystal_thorium"), baseTime, HbmFluids.SULFURIC_ACID, 500, 0.05F, 7);
        crystallizer(consumer, "plutonium_ore", HbmIngredient.legacyOre("orePlutonium", 1),
                item("crystal_plutonium"), baseTime, HbmFluids.SULFURIC_ACID, 500, 0.05F, 8);
        crystallizer(consumer, "titanium_ore", HbmIngredient.legacyOre("oreTitanium", 1),
                item("crystal_titanium"), baseTime, HbmFluids.SULFURIC_ACID, 500, 0.05F, 9);
        crystallizer(consumer, "sulfur_ore", HbmIngredient.legacyOre("oreSulfur", 1),
                item("crystal_sulfur"), baseTime, 0.05F, 10);
        crystallizer(consumer, "niter_ore", HbmIngredient.legacyOre("oreSaltpeter",
                Ingredient.of(forgeTag("ores/niter")), 1),
                item("crystal_niter"), baseTime, 0.05F, 11);
        crystallizer(consumer, "copper_ore", HbmIngredient.legacyOre("oreCopper", 1),
                item("crystal_copper"), baseTime, 0.05F, 12);
        crystallizer(consumer, "tungsten_ore", HbmIngredient.legacyOre("oreTungsten", 1),
                item("crystal_tungsten"), baseTime, HbmFluids.SULFURIC_ACID, 500, 0.05F, 13);
        crystallizer(consumer, "aluminium_ore", HbmIngredient.legacyOre("oreAluminum", 1),
                item("crystal_aluminium"), baseTime, 0.05F, 14);
        crystallizer(consumer, "fluorite_ore", HbmIngredient.legacyOre("oreFluorite", 1),
                item("crystal_fluorite"), baseTime, 0.05F, 15);
        crystallizer(consumer, "beryllium_ore", HbmIngredient.legacyOre("oreBeryllium", 1),
                item("crystal_beryllium"), baseTime, 0.05F, 16);
        crystallizer(consumer, "lead_ore", HbmIngredient.legacyOre("oreLead", 1),
                item("crystal_lead"), baseTime, 0.05F, 17);
        crystallizer(consumer, "schrabidium_ore", HbmIngredient.legacyOre("oreSchrabidium", 1),
                item("crystal_schrabidium"), baseTime, HbmFluids.SULFURIC_ACID, 500, 0.05F, 18);
        crystallizer(consumer, "lithium_ore", HbmIngredient.legacyOre("oreLithium", 1),
                item("crystal_lithium"), baseTime, HbmFluids.SULFURIC_ACID, 500, 0.05F, 19);
        crystallizer(consumer, "cobalt_ore", HbmIngredient.legacyOre("oreCobalt", 1),
                item("crystal_cobalt"), baseTime, HbmFluids.SULFURIC_ACID, 500, 0.05F, 20);

        crystallizer(consumer, "powder_calcium_cement", HbmIngredient.of(item("powder_calcium"), 1),
                new ItemStack(item("powder_cement"), 8), utilityTime, HbmFluids.REDMUD, 75, 0.1F, 21);
        crystallizer(consumer, "malachite_copper_scraps", HbmIngredient.legacyOre("ingotMalachite", 1),
                FoundryScrapsItem.create(new MaterialStack(Mats.MAT_COPPER, MaterialShapes.INGOT.q(1))), 300,
                HbmFluids.SULFURIC_ACID, 250, 0.1F, 22);
        crystallizer(consumer, "rare_earth_ore", HbmIngredient.legacyOre("oreRareEarth", 1),
                item("crystal_rare"), baseTime, HbmFluids.SULFURIC_ACID, 500, 0.05F, 23);
        crystallizer(consumer, "cinnabar_ore", HbmIngredient.legacyOre("oreCinnabar", 1),
                item("crystal_cinnebar"), baseTime, 0.05F, 24);
        crystallizer(consumer, "nether_fire_ore", HbmIngredient.of(item("ore_nether_fire"), 1),
                item("crystal_phosphorus"), baseTime, 0.05F, 25);
        crystallizer(consumer, "tikite_ore", HbmIngredient.of(item("ore_tikite"), 1),
                item("crystal_trixite"), baseTime, HbmFluids.SULFURIC_ACID, 500, 0.05F, 26);
        crystallizer(consumer, "gravel_diamond", HbmIngredient.of(item("gravel_diamond"), 1),
                item("crystal_diamond"), baseTime, 0.05F, 27);
        crystallizer(consumer, "schraranium_ingot", HbmIngredient.legacyOre("ingotSchraranium", 1),
                item("crystal_schraranium"), baseTime, 0.05F, 28);

        crystallizer(consumer, "fiberglass_sand", HbmIngredient.legacyOre("sand", 1),
                item("ingot_fiberglass"), utilityTime, 0.15F, 29);
        crystallizer(consumer, "ingot_silicon_quartz", HbmIngredient.legacyOre("ingotSilicon", 1),
                new ItemStack(Items.QUARTZ, 2), utilityTime, HbmFluids.OXYGEN, 250, 0.1F, 30);
        crystallizer(consumer, "redstone_block", HbmIngredient.legacyOre("blockRedstone", 1),
                item("ingot_mercury"), baseTime, 0.25F, 31);
        crystallizer(consumer, "cinnabar_crystal", HbmIngredient.legacyOre("crystalCinnabar", 1),
                new ItemStack(item("ingot_mercury"), 3), baseTime, 0.25F, 32);
        crystallizer(consumer, "borax_dust", HbmIngredient.legacyOre("dustBorax", 1),
                new ItemStack(item("powder_boron_tiny"), 3), baseTime, HbmFluids.SULFURIC_ACID, 500, 0.25F, 33);
        crystallizer(consumer, "coal_storage_block", HbmIngredient.legacyOre("blockCoal", 1),
                item("block_graphite"), baseTime, 0.0F, 34);

        crystallizer(consumer, "cobblestone", HbmIngredient.of(Blocks.COBBLESTONE, 1),
                item("reinforced_stone"), utilityTime, 0.0F, 35);
        crystallizer(consumer, "gravel_obsidian", HbmIngredient.of(item("gravel_obsidian"), 1),
                item("brick_obsidian"), utilityTime, 0.0F, 36);
        crystallizer(consumer, "rotten_flesh", HbmIngredient.of(Items.ROTTEN_FLESH, 1),
                Items.LEATHER, utilityTime, 0.25F, 37);
        crystallizer(consumer, "coal_infernal", HbmIngredient.of(item("coal_infernal"), 1),
                item("solid_fuel"), utilityTime, 0.0F, 38);
        crystallizer(consumer, "stone_gneiss", HbmIngredient.of(item("stone_gneiss"), 1),
                item("powder_lithium"), utilityTime, 0.25F, 39);
        crystallizer(consumer, "bone_meal_slime", HbmIngredient.of(Items.BONE_MEAL, 1),
                new ItemStack(Items.SLIME_BALL, 4), mixingTime, HbmFluids.SULFURIC_ACID, 250, 0.0F, 40);
        crystallizer(consumer, "bone_slime", HbmIngredient.of(Items.BONE, 1),
                new ItemStack(Items.SLIME_BALL, 16), mixingTime, HbmFluids.SULFURIC_ACID, 1_000, 0.0F, 41);
        crystallizer(consumer, "mustardwillow_cadmium", HbmIngredient.of(item("plant_item_mustardwillow"), 10),
                item("powder_cadmium"), utilityTime, HbmFluids.RADIOSOLVENT, 250, 0.0F, 42);
        crystallizer(consumer, "scrap_oil_arsenic", HbmIngredient.of(item("scrap_oil"), 16),
                item("nugget_arsenic"), utilityTime, HbmFluids.RADIOSOLVENT, 100, 0.3F, 43);
        crystallizer(consumer, "powder_ash_fullerene_cft", HbmIngredient.of(item("powder_ash_fullerene"), 4),
                item("ingot_cft"), baseTime, HbmFluids.XYLENE, 1_000, 0.1F, 44);

        crystallizer(consumer, "powder_diamond", HbmIngredient.legacyOre("dustDiamond", 1),
                Items.DIAMOND, utilityTime, 0.0F, 45);
        crystallizer(consumer, "powder_emerald", HbmIngredient.legacyOre("dustEmerald", 1),
                Items.EMERALD, utilityTime, 0.0F, 46);
        crystallizer(consumer, "powder_lapis", HbmIngredient.legacyOre("dustLapis", 1),
                Items.LAPIS_LAZULI, utilityTime, 0.0F, 47);
        crystallizer(consumer, "powder_semtex_mix", HbmIngredient.of(item("powder_semtex_mix"), 1),
                item("ingot_semtex"), baseTime, 0.0F, 48);
        crystallizer(consumer, "powder_desh_ready", HbmIngredient.of(item("powder_desh_ready"), 1),
                item("ingot_desh"), baseTime, 0.0F, 49);
        crystallizer(consumer, "powder_meteorite", HbmIngredient.of(item("powder_meteorite"), 1),
                item("fragment_meteorite"), utilityTime, 0.0F, 50);
        crystallizer(consumer, "cadmium_dust_rubber", HbmIngredient.legacyOre("dustCadmium", 1),
                new ItemStack(item("ingot_rubber"), 16), utilityTime, HbmFluids.FISHOIL, 4_000, 0.0F, 51);
        crystallizer(consumer, "rubber_ingot_sourgas", HbmIngredient.legacyOre("ingotLatex", 1),
                item("ingot_rubber"), mixingTime, HbmFluids.SOURGAS, 25, 0.15F, 52);
        crystallizer(consumer, "powder_sawdust_cordite", HbmIngredient.of(item("powder_sawdust"), 1),
                item("cordite"), mixingTime, HbmFluids.NITROGLYCERIN, 250, 0.25F, 53);
        crystallizer(consumer, "rebar_concrete", HbmIngredient.of(item("rebar"), 1),
                item("concrete_rebar"), 10, HbmFluids.CONCRETE, 1_000, 0.0F, 54);
        crystallizer(consumer, "meteorite_sword_etched", HbmIngredient.of(item("meteorite_sword_treated"), 1),
                item("meteorite_sword_etched"), baseTime, 0.0F, 55);
        crystallizer(consumer, "powder_impure_osmiridium", HbmIngredient.of(item("powder_impure_osmiridium"), 1),
                item("crystal_osmiridium"), baseTime, HbmFluids.SCHRABIDIC, 1_000, 0.0F, 56);

        bedrockOreCrystallizerRecipes(consumer, 128);

        PlasticScrapItem.ScrapType[] scrapTypes = PlasticScrapItem.ScrapType.values();
        for (int meta = 0; meta < scrapTypes.length; meta++) {
            PlasticScrapItem.ScrapType type = scrapTypes[meta];
            String suffix = type.name().toLowerCase(Locale.ROOT);
            crystallizer(consumer, "scrap_plastic_" + suffix,
                    HbmIngredient.partialNbt(PlasticScrapItem.createStack(item("scrap_plastic").asItem(), type)),
                    ModItems.CIRCUIT_STAR_PIECE_ITEMS.get(meta).get(), baseTime, 0.0F, 57 + meta);
        }

        FluidType[] dyeFluids = {HbmFluids.WOODOIL, HbmFluids.FISHOIL, HbmFluids.LIGHTOIL};
        String[] dyeFluidNames = {"woodoil", "fishoil", "lightoil"};
        String[] dyeNames = {"black", "white", "red", "yellow", "green", "blue"};
        String[] dyeOres = {"dustCoal", "dustTitanium", "dustIron", "dustTungsten", "dustCopper", "dustCobalt"};
        int[] dyeMetas = {0, 15, 1, 11, 2, 4};
        int dyeOrder = 378;
        for (int fluidIndex = 0; fluidIndex < dyeFluids.length; fluidIndex++) {
            for (int dyeIndex = 0; dyeIndex < dyeNames.length; dyeIndex++) {
                crystallizer(consumer, "chemical_dye_" + dyeNames[dyeIndex] + "_" + dyeFluidNames[fluidIndex],
                        HbmIngredient.legacyOre(dyeOres[dyeIndex], 1),
                        new ItemStack(ModItems.CHEMICAL_DYE_ITEMS.get(dyeMetas[dyeIndex]).get(), 4),
                        mixingTime, dyeFluids[fluidIndex], 100, 0.15F, dyeOrder++);
            }
        }

        crystallizer(consumer, "oil_tar_crude_wax", HbmIngredient.of(item("oil_tar_crude"), 1),
                item("oil_tar_wax"), 20, HbmFluids.CHLORINE, 250, 0.0F, 396);
        crystallizer(consumer, "oil_tar_crack_wax", HbmIngredient.of(item("oil_tar_crack"), 1),
                item("oil_tar_wax"), 20, HbmFluids.CHLORINE, 100, 0.0F, 397);
        crystallizer(consumer, "oil_tar_paraffin_wax", HbmIngredient.of(item("oil_tar_paraffin"), 1),
                item("oil_tar_wax"), 20, HbmFluids.CHLORINE, 100, 0.0F, 398);
        crystallizer(consumer, "oil_tar_wax_pellet_charged", HbmIngredient.of(item("oil_tar_wax"), 1),
                item("pellet_charged"), 200, HbmFluids.IONGEL, 500, 0.0F, 399);
        crystallizer(consumer, "oil_tar_paraffin_pill_red", HbmIngredient.of(item("oil_tar_paraffin"), 1),
                item("pill_red"), 200, HbmFluids.ESTRADIOL, 250, 0.0F, 400);
        crystallizer(consumer, "sand_clay", HbmIngredient.legacyOre("sand", 1),
                Blocks.CLAY, 20, HbmFluids.COLLOID, 1_000, 0.0F, 401);
        crystallizer(consumer, "sand_quartz_dynamite", HbmIngredient.of(item("sand_quartz"), 1),
                new ItemStack(item("ball_dynamite"), 16), 20, HbmFluids.NITROGLYCERIN, 1_000, 0.0F, 402);
        crystallizer(consumer, "quartz_dust_dynamite", HbmIngredient.legacyOre("dustNetherQuartz", 1),
                new ItemStack(item("ball_dynamite"), 4), 20, HbmFluids.NITROGLYCERIN, 250, 0.0F, 403);

        crystallizer(consumer, "moon_turf", HbmIngredient.of(item("moon_turf"), 16),
                item("chunk_ore_moonstone"), 1200, 0.0F, 407);
    }

    private static void bedrockOreCrystallizerRecipes(Consumer<FinishedRecipe> consumer, int sourceOrderStart) {
        int sourceOrder = sourceOrderStart;
        for (BedrockOreType type : BedrockOreType.values()) {
            String suffix = type.suffix();
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_base_water",
                    BedrockOreGrade.BASE, type, 1, BedrockOreGrade.BASE_WASHED, 100, HbmFluids.WATER, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_base_roasted_water",
                    BedrockOreGrade.BASE_ROASTED, type, 1, BedrockOreGrade.BASE_WASHED, 100, HbmFluids.WATER, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_primary_sulfuric",
                    BedrockOreGrade.PRIMARY, type, 1, BedrockOreGrade.PRIMARY_SULFURIC, 200,
                    HbmFluids.SULFURIC_ACID, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_primary_roasted_sulfuric",
                    BedrockOreGrade.PRIMARY_ROASTED, type, 1, BedrockOreGrade.PRIMARY_SULFURIC, 200,
                    HbmFluids.SULFURIC_ACID, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_primary_solvent",
                    BedrockOreGrade.PRIMARY, type, 1, BedrockOreGrade.PRIMARY_SOLVENT, 200,
                    HbmFluids.SOLVENT, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_primary_roasted_solvent",
                    BedrockOreGrade.PRIMARY_ROASTED, type, 1, BedrockOreGrade.PRIMARY_SOLVENT, 200,
                    HbmFluids.SOLVENT, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_primary_nosulfuric_solvent",
                    BedrockOreGrade.PRIMARY_NOSULFURIC, type, 1, BedrockOreGrade.PRIMARY_SOLVENT, 200,
                    HbmFluids.SOLVENT, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_primary_rad",
                    BedrockOreGrade.PRIMARY, type, 1, BedrockOreGrade.PRIMARY_RAD, 200,
                    HbmFluids.RADIOSOLVENT, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_primary_roasted_rad",
                    BedrockOreGrade.PRIMARY_ROASTED, type, 1, BedrockOreGrade.PRIMARY_RAD, 200,
                    HbmFluids.RADIOSOLVENT, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_primary_nosulfuric_rad",
                    BedrockOreGrade.PRIMARY_NOSULFURIC, type, 1, BedrockOreGrade.PRIMARY_RAD, 200,
                    HbmFluids.RADIOSOLVENT, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_primary_nosolvent_rad",
                    BedrockOreGrade.PRIMARY_NOSOLVENT, type, 1, BedrockOreGrade.PRIMARY_RAD, 200,
                    HbmFluids.RADIOSOLVENT, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_sulfuric_byproduct_water",
                    BedrockOreGrade.SULFURIC_BYPRODUCT, type, 4, BedrockOreGrade.SULFURIC_WASHED, 100,
                    HbmFluids.WATER, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_sulfuric_roasted_water",
                    BedrockOreGrade.SULFURIC_ROASTED, type, 4, BedrockOreGrade.SULFURIC_WASHED, 100,
                    HbmFluids.WATER, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_sulfuric_arc_water",
                    BedrockOreGrade.SULFURIC_ARC, type, 4, BedrockOreGrade.SULFURIC_WASHED, 100,
                    HbmFluids.WATER, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_solvent_byproduct_water",
                    BedrockOreGrade.SOLVENT_BYPRODUCT, type, 4, BedrockOreGrade.SOLVENT_WASHED, 100,
                    HbmFluids.WATER, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_solvent_roasted_water",
                    BedrockOreGrade.SOLVENT_ROASTED, type, 4, BedrockOreGrade.SOLVENT_WASHED, 100,
                    HbmFluids.WATER, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_solvent_arc_water",
                    BedrockOreGrade.SOLVENT_ARC, type, 4, BedrockOreGrade.SOLVENT_WASHED, 100,
                    HbmFluids.WATER, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_rad_byproduct_water",
                    BedrockOreGrade.RAD_BYPRODUCT, type, 4, BedrockOreGrade.RAD_WASHED, 100,
                    HbmFluids.WATER, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_rad_roasted_water",
                    BedrockOreGrade.RAD_ROASTED, type, 4, BedrockOreGrade.RAD_WASHED, 100,
                    HbmFluids.WATER, 250);
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_rad_arc_water",
                    BedrockOreGrade.RAD_ARC, type, 4, BedrockOreGrade.RAD_WASHED, 100,
                    HbmFluids.WATER, 250);
            for (BedrockOreGrade input : BEDROCK_ORE_PRIMARY_SPLIT_INPUTS) {
                sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder,
                        suffix + "_" + input.serializedName() + "_hydrogen", input, type, 1,
                        BedrockOreGrade.PRIMARY_FIRST, 200, HbmFluids.HYDROGEN, 250);
            }
            for (BedrockOreGrade input : BEDROCK_ORE_PRIMARY_SPLIT_INPUTS) {
                sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder,
                        suffix + "_" + input.serializedName() + "_chlorine", input, type, 1,
                        BedrockOreGrade.PRIMARY_SECOND, 200, HbmFluids.CHLORINE, 250);
            }
            sourceOrder = bedrockOreCrystallizer(consumer, sourceOrder, suffix + "_crumbs_slop",
                    BedrockOreGrade.CRUMBS, type, 64, BedrockOreGrade.BASE, 200, HbmFluids.SLOP, 1_000);
        }
    }

    private static int bedrockOreCrystallizer(Consumer<FinishedRecipe> consumer, int sourceOrder, String name,
            BedrockOreGrade inputGrade, BedrockOreType type, int inputCount, BedrockOreGrade outputGrade,
            int duration, FluidType fluid, int fluidAmount) {
        crystallizer(consumer, "bedrock_ore_" + name, bedrockOreInput(inputGrade, type, inputCount),
                bedrockOre(outputGrade, type), duration, fluid, fluidAmount, 0.0F, sourceOrder);
        return sourceOrder + 1;
    }

    private static void crystallizer(Consumer<FinishedRecipe> consumer, String name, HbmIngredient input,
            ItemLike output, int duration, float productivity, int sourceOrder) {
        crystallizer(consumer, name, input, new ItemStack(output), duration, HbmFluids.PEROXIDE, 500,
                productivity, sourceOrder);
    }

    private static void crystallizer(Consumer<FinishedRecipe> consumer, String name, HbmIngredient input,
            ItemStack output, int duration, float productivity, int sourceOrder) {
        crystallizer(consumer, name, input, output, duration, HbmFluids.PEROXIDE, 500, productivity, sourceOrder);
    }

    private static void crystallizer(Consumer<FinishedRecipe> consumer, String name, HbmIngredient input,
            ItemLike output, int duration, FluidType fluid, int fluidAmount, float productivity, int sourceOrder) {
        crystallizer(consumer, name, input, new ItemStack(output), duration, fluid, fluidAmount, productivity,
                sourceOrder);
    }

    private static void crystallizer(Consumer<FinishedRecipe> consumer, String name, HbmIngredient input,
            ItemStack output, int duration, FluidType fluid, int fluidAmount, float productivity, int sourceOrder) {
        JsonObject json = new JsonObject();
        json.add("input", input.toJson());
        json.add("output", itemStackJson(output));
        json.addProperty("duration", duration);
        json.add("fluid", crystallizerFluidJson(fluid, fluidAmount));
        if (productivity > 0.0F) {
            json.addProperty("productivity", productivity);
        }
        json.addProperty("source_order", sourceOrder);
        consumer.accept(finishedRecipe(id("crystallizer/" + name), json, ModRecipes.CRYSTALLIZER.serializer().get()));
    }

    private static JsonObject crystallizerFluidJson(FluidType fluid, int amount) {
        JsonObject object = new JsonObject();
        object.addProperty("fluid", fluid.getName());
        object.addProperty("amount", amount);
        return object;
    }

    private static void arcFurnaceRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.arcFurnace("arc.sand", 400, 1_000)
                .inputLegacyOre("sand", Ingredient.of(forgeTag("sand")), 1)
                .outputItem(item("nugget_silicon"))
                .arcMaterialOutput(mat(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(1)))
                .sourceOrder(0)
                .save(consumer, id("arc_furnace/sand"));
        GenericMachineRecipeBuilder.arcFurnace("arc.flint", 400, 1_000)
                .inputItem(Items.FLINT, 1)
                .outputItem(new ItemStack(item("nugget_silicon"), 4))
                .arcMaterialOutput(mat(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2)))
                .sourceOrder(1)
                .save(consumer, id("arc_furnace/flint"));
        GenericMachineRecipeBuilder.arcFurnace("arc.quartz", 400, 1_000)
                .inputLegacyOre("gemQuartz", Ingredient.of(forgeTag("gems/quartz")), 1)
                .outputItem(new ItemStack(item("nugget_silicon"), 3))
                .arcMaterialOutput(mat(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(3)))
                .sourceOrder(2)
                .save(consumer, id("arc_furnace/quartz"));
        GenericMachineRecipeBuilder.arcFurnace("arc.quartzdust", 400, 1_000)
                .inputLegacyOre("dustQuartz", Ingredient.of(forgeTag("dusts/quartz")), 1)
                .outputItem(new ItemStack(item("nugget_silicon"), 3))
                .arcMaterialOutput(mat(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(3)))
                .sourceOrder(3)
                .save(consumer, id("arc_furnace/quartz_dust"));
        GenericMachineRecipeBuilder.arcFurnace("arc.quartzblock", 400, 1_000)
                .inputLegacyOre("blockQuartz", Ingredient.of(Blocks.QUARTZ_BLOCK), 1)
                .outputItem(new ItemStack(item("nugget_silicon"), 12))
                .arcMaterialOutput(mat(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(12)))
                .sourceOrder(4)
                .save(consumer, id("arc_furnace/quartz_block"));
        GenericMachineRecipeBuilder.arcFurnace("arc.fiberglass", 400, 1_000)
                .inputLegacyOre("ingotFiberglass", 1)
                .outputItem(new ItemStack(item("nugget_silicon"), 4))
                .arcMaterialOutput(mat(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2)))
                .sourceOrder(5)
                .save(consumer, id("arc_furnace/fiberglass"));
        GenericMachineRecipeBuilder.arcFurnace("arc.fiberglassblock", 400, 1_000)
                .inputLegacyOre("blockFiberglass", 1)
                .outputItem(new ItemStack(item("nugget_silicon"), 40))
                .arcMaterialOutput(mat(Mats.MAT_SILICON, MaterialShapes.INGOT.q(9, 2)))
                .sourceOrder(6)
                .save(consumer, id("arc_furnace/fiberglass_block"));
        GenericMachineRecipeBuilder.arcFurnace("arc.asbestos", 400, 1_000)
                .inputLegacyOre("ingotAsbestos", 1)
                .outputItem(new ItemStack(item("nugget_silicon"), 4))
                .arcMaterialOutput(mat(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2)))
                .sourceOrder(7)
                .save(consumer, id("arc_furnace/asbestos"));
        GenericMachineRecipeBuilder.arcFurnace("arc.asbestosdust", 400, 1_000)
                .inputLegacyOre("dustAsbestos", 1)
                .outputItem(new ItemStack(item("nugget_silicon"), 4))
                .arcMaterialOutput(mat(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2)))
                .sourceOrder(8)
                .save(consumer, id("arc_furnace/asbestos_dust"));
        GenericMachineRecipeBuilder.arcFurnace("arc.asbestosblock", 400, 1_000)
                .inputLegacyOre("blockAsbestos", 1)
                .outputItem(new ItemStack(item("nugget_silicon"), 40))
                .arcMaterialOutput(mat(Mats.MAT_SILICON, MaterialShapes.INGOT.q(9, 2)))
                .sourceOrder(9)
                .save(consumer, id("arc_furnace/asbestos_block"));
        GenericMachineRecipeBuilder.arcFurnace("arc.sandquartz", 400, 1_000)
                .inputItem(ModBlocks.SAND_QUARTZ.get(), 1)
                .outputItem(ModBlocks.GLASS_QUARTZ.get())
                .sourceOrder(10)
                .save(consumer, id("arc_furnace/sand_quartz"));
        GenericMachineRecipeBuilder.arcFurnace("arc.borax", 400, 1_000)
                .inputLegacyOre("dustBorax", 1)
                .outputItem(new ItemStack(item("powder_boron_tiny"), 3))
                .arcMaterialOutput(mat(Mats.MAT_BORON, MaterialShapes.NUGGET.q(3)))
                .sourceOrder(11)
                .save(consumer, id("arc_furnace/borax"));

        bedrockOreArcFurnaceRecipes(consumer, 12);

        int nextSourceOrder = arcFurnaceMaterialAutogenRecipes(consumer, 1000);
        nextSourceOrder = arcFurnaceCustomSmeltableRecipes(consumer, nextSourceOrder);
        arcFurnaceFurnaceSmeltableRecipes(consumer, nextSourceOrder);
    }

    private static int bedrockOreArcFurnaceRecipes(Consumer<FinishedRecipe> consumer, int sourceOrderStart) {
        int sourceOrder = sourceOrderStart;
        for (BedrockOreType type : BedrockOreType.values()) {
            String suffix = type.suffix();
            BedrockOreProducts products = bedrockOreProducts(type);
            sourceOrder = bedrockOreArcSolid(consumer, sourceOrder, suffix + "_sulfuric_byproduct",
                    BedrockOreGrade.SULFURIC_BYPRODUCT, type, BedrockOreGrade.SULFURIC_ARC, 2);
            sourceOrder = bedrockOreArcSolid(consumer, sourceOrder, suffix + "_sulfuric_roasted",
                    BedrockOreGrade.SULFURIC_ROASTED, type, BedrockOreGrade.SULFURIC_ARC, 4);
            sourceOrder = bedrockOreArcSolid(consumer, sourceOrder, suffix + "_solvent_byproduct",
                    BedrockOreGrade.SOLVENT_BYPRODUCT, type, BedrockOreGrade.SOLVENT_ARC, 2);
            sourceOrder = bedrockOreArcSolid(consumer, sourceOrder, suffix + "_solvent_roasted",
                    BedrockOreGrade.SOLVENT_ROASTED, type, BedrockOreGrade.SOLVENT_ARC, 4);
            sourceOrder = bedrockOreArcSolid(consumer, sourceOrder, suffix + "_rad_byproduct",
                    BedrockOreGrade.RAD_BYPRODUCT, type, BedrockOreGrade.RAD_ARC, 2);
            sourceOrder = bedrockOreArcSolid(consumer, sourceOrder, suffix + "_rad_roasted",
                    BedrockOreGrade.RAD_ROASTED, type, BedrockOreGrade.RAD_ARC, 4);

            sourceOrder = bedrockOreArcFluid(consumer, sourceOrder, suffix + "_primary_first",
                    BedrockOreGrade.PRIMARY_FIRST, type,
                    bedrockOreFluid(products.primary1(), 5), bedrockOreFluid(products.primary2(), 2));
            sourceOrder = bedrockOreArcFluid(consumer, sourceOrder, suffix + "_primary_second",
                    BedrockOreGrade.PRIMARY_SECOND, type,
                    bedrockOreFluid(products.primary1(), 2), bedrockOreFluid(products.primary2(), 5));
            sourceOrder = bedrockOreArcFluid(consumer, sourceOrder, suffix + "_crumbs",
                    BedrockOreGrade.CRUMBS, type,
                    bedrockOreFluid(products.primary1(), 1), bedrockOreFluid(products.primary2(), 1));
            sourceOrder = bedrockOreArcFluid(consumer, sourceOrder, suffix + "_sulfuric_washed",
                    BedrockOreGrade.SULFURIC_WASHED, type,
                    bedrockOreFluid(products.byproductAcid1(), 3),
                    bedrockOreFluid(products.byproductAcid2(), 3),
                    bedrockOreFluid(products.byproductAcid3(), 3));
            sourceOrder = bedrockOreArcFluid(consumer, sourceOrder, suffix + "_solvent_washed",
                    BedrockOreGrade.SOLVENT_WASHED, type,
                    bedrockOreFluid(products.byproductSolvent1(), 3),
                    bedrockOreFluid(products.byproductSolvent2(), 3),
                    bedrockOreFluid(products.byproductSolvent3(), 3));
            sourceOrder = bedrockOreArcFluid(consumer, sourceOrder, suffix + "_rad_washed",
                    BedrockOreGrade.RAD_WASHED, type,
                    bedrockOreFluid(products.byproductRad1(), 3),
                    bedrockOreFluid(products.byproductRad2(), 3),
                    bedrockOreFluid(products.byproductRad3(), 3));
        }
        return sourceOrder;
    }

    private static int bedrockOreArcSolid(Consumer<FinishedRecipe> consumer, int sourceOrder, String name,
            BedrockOreGrade inputGrade, BedrockOreType type, BedrockOreGrade outputGrade, int outputCount) {
        GenericMachineRecipeBuilder.arcFurnace("arc.bedrock_ore." + name, 400, 1_000)
                .inputIngredient(bedrockOreInput(inputGrade, type))
                .outputItem(bedrockOre(outputGrade, type, outputCount))
                .sourceOrder(sourceOrder)
                .save(consumer, id("arc_furnace/bedrock_ore_" + name));
        return sourceOrder + 1;
    }

    private static int bedrockOreArcFluid(Consumer<FinishedRecipe> consumer, int sourceOrder, String name,
            BedrockOreGrade inputGrade, BedrockOreType type, @Nullable MaterialStack... outputs) {
        GenericMachineRecipeBuilder builder = GenericMachineRecipeBuilder.arcFurnace("arc.bedrock_ore." + name,
                400, 1_000)
                .inputIngredient(bedrockOreInput(inputGrade, type))
                .sourceOrder(sourceOrder);
        int outputCount = 0;
        for (MaterialStack output : outputs) {
            if (output != null && !output.isEmpty()) {
                builder.arcMaterialOutput(output);
                outputCount++;
            }
        }
        if (outputCount > 0) {
            builder.save(consumer, id("arc_furnace/bedrock_ore_" + name));
        }
        return sourceOrder + 1;
    }

    private static int arcFurnaceMaterialAutogenRecipes(Consumer<FinishedRecipe> consumer, int sourceOrder) {
        for (NTMMaterial material : Mats.orderedList) {
            int in = material.convIn;
            int out = material.convOut;
            NTMMaterial convert = material.smeltsInto;
            if (convert.smeltable != SmeltingBehavior.SMELTABLE) {
                continue;
            }
            for (MaterialShapes shape : MaterialShapes.allShapes) {
                if (shape.noAutogen || shape == MaterialShapes.FRAGMENT
                        || shape.prefixes == null || shape.prefixes.length == 0) {
                    continue;
                }
                String legacyOreName = shape.name() + legacyArcMaterialName(material);
                if (isExplicitArcFurnaceLiquidInput(legacyOreName)) {
                    continue;
                }
                int amount = shape.q(1) * out / in;
                if (amount <= 0) {
                    continue;
                }
                TagKey<Item> inputTag = LegacyOreDictionaryMappings.itemTag(legacyOreName);
                GenericMachineRecipeBuilder.arcFurnace("arc.autogen." + legacyOreName, 400, 1_000)
                        .inputLegacyOre(legacyOreName, 1)
                        .arcMaterialOutput(mat(convert, amount))
                        .conditionNotTagEmpty(inputTag)
                        .sourceOrder(sourceOrder++)
                        .save(consumer, id("arc_furnace/material/" + arcAutogenRecipePath(legacyOreName)));
            }
        }
        return sourceOrder;
    }

    private static String legacyArcMaterialName(NTMMaterial material) {
        if (material == Mats.MAT_ALUMINIUM) {
            return "Aluminum";
        }
        if (material == Mats.MAT_TANTALIUM) {
            return "Tantalum";
        }
        if (material == Mats.MAT_TECHNETIUM) {
            return "Technetium99";
        }
        if (material == Mats.MAT_DESH) {
            return "WorkersAlloy";
        }
        if (material == Mats.MAT_MUD) {
            return "WatzMud";
        }
        if (material == Mats.MAT_GUNMETAL) {
            return "GunMetal";
        }
        return material.names[0];
    }

    private static boolean isExplicitArcFurnaceLiquidInput(String legacyOreName) {
        return switch (legacyOreName) {
            case "gemQuartz", "dustQuartz", "blockQuartz",
                    "ingotFiberglass", "blockFiberglass",
                    "ingotAsbestos", "dustAsbestos", "blockAsbestos",
                    "dustBorax" -> true;
            default -> false;
        };
    }

    private static String arcAutogenRecipePath(String legacyOreName) {
        return LegacyOreDictionaryMappings.itemTagPath(legacyOreName)
                .replace('/', '_')
                .toLowerCase(Locale.ROOT);
    }

    private static int arcFurnaceCustomSmeltableRecipes(Consumer<FinishedRecipe> consumer, int sourceOrder) {
        int block = MaterialShapes.BLOCK.q(1);
        int ingot = MaterialShapes.INGOT.q(1);
        int nugget = MaterialShapes.NUGGET.q(1);
        int dust = MaterialShapes.DUST.q(1);
        int gem = MaterialShapes.GEM.q(1);
        int quart = MaterialShapes.QUART.q(1);

        sourceOrder = arcFurnaceCustomSmeltable(consumer, "stone", sourceOrder, HbmIngredient.legacyOre("stone", 1),
                mat(Mats.MAT_STONE, block));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "cobblestone", sourceOrder,
                HbmIngredient.legacyOre("cobblestone", 1), mat(Mats.MAT_STONE, block));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "obsidian", sourceOrder, HbmIngredient.of(Blocks.OBSIDIAN, 1),
                mat(Mats.MAT_OBSIDIAN, block));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "rail", sourceOrder, HbmIngredient.of(Items.RAIL, 1),
                mat(Mats.MAT_IRON, MaterialShapes.INGOT.q(6, 16)));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "powered_rail", sourceOrder,
                HbmIngredient.of(Items.POWERED_RAIL, 1),
                mat(Mats.MAT_GOLD, MaterialShapes.INGOT.q(6, 6)),
                mat(Mats.MAT_REDSTONE, MaterialShapes.DUST.q(1, 6)));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "detector_rail", sourceOrder,
                HbmIngredient.of(Items.DETECTOR_RAIL, 1),
                mat(Mats.MAT_IRON, MaterialShapes.INGOT.q(6, 6)),
                mat(Mats.MAT_REDSTONE, MaterialShapes.DUST.q(1, 6)));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "minecart", sourceOrder, HbmIngredient.of(Items.MINECART, 1),
                mat(Mats.MAT_IRON, ingot * 5));

        sourceOrder = arcFurnaceCustomSmeltable(consumer, "blade_titanium", sourceOrder,
                HbmIngredient.of(item("blade_titanium"), 1), mat(Mats.MAT_TITANIUM, ingot * 3));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "blade_tungsten", sourceOrder,
                HbmIngredient.of(item("blade_tungsten"), 1), mat(Mats.MAT_TUNGSTEN, ingot * 3));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "blades_steel", sourceOrder,
                HbmIngredient.of(item("blades_steel"), 1), mat(Mats.MAT_STEEL, ingot * 4));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "blades_titanium", sourceOrder,
                HbmIngredient.of(item("blades_titanium"), 1), mat(Mats.MAT_TITANIUM, ingot * 4));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "stamp_stone_flat", sourceOrder,
                HbmIngredient.of(item("stamp_stone_flat"), 1), mat(Mats.MAT_STONE, ingot * 3));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "stamp_iron_flat", sourceOrder,
                HbmIngredient.of(item("stamp_iron_flat"), 1), mat(Mats.MAT_IRON, ingot * 3));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "stamp_steel_flat", sourceOrder,
                HbmIngredient.of(item("stamp_steel_flat"), 1), mat(Mats.MAT_STEEL, ingot * 3));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "stamp_titanium_flat", sourceOrder,
                HbmIngredient.of(item("stamp_titanium_flat"), 1), mat(Mats.MAT_TITANIUM, ingot * 3));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "stamp_obsidian_flat", sourceOrder,
                HbmIngredient.of(item("stamp_obsidian_flat"), 1), mat(Mats.MAT_OBSIDIAN, ingot * 3));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "pipes_steel", sourceOrder,
                HbmIngredient.of(item("pipes_steel"), 1), mat(Mats.MAT_STEEL, block * 3));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "casing_small", sourceOrder,
                HbmIngredient.of(item("casing_small"), 1), mat(Mats.MAT_GUNMETAL, MaterialShapes.PLATE.q(1, 4)));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "casing_small_steel", sourceOrder,
                HbmIngredient.of(item("casing_small_steel"), 1),
                mat(Mats.MAT_WEAPONSTEEL, MaterialShapes.PLATE.q(1, 4)));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "casing_large", sourceOrder,
                HbmIngredient.of(item("casing_large"), 1), mat(Mats.MAT_GUNMETAL, MaterialShapes.PLATE.q(1, 2)));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "casing_large_steel", sourceOrder,
                HbmIngredient.of(item("casing_large_steel"), 1),
                mat(Mats.MAT_WEAPONSTEEL, MaterialShapes.PLATE.q(1, 2)));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "chunk_ore_cryolite", sourceOrder,
                HbmIngredient.of(item("chunk_ore_cryolite"), 1),
                mat(Mats.MAT_ALUMINIUM, ingot), mat(Mats.MAT_SODIUM, ingot));

        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_iron", sourceOrder,
                HbmIngredient.legacyOre("oreIron", 1),
                mat(Mats.MAT_IRON, ingot * 2), mat(Mats.MAT_TITANIUM, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_titanium", sourceOrder,
                HbmIngredient.legacyOre("oreTitanium", 1),
                mat(Mats.MAT_TITANIUM, ingot * 2), mat(Mats.MAT_IRON, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_tungsten", sourceOrder,
                HbmIngredient.legacyOre("oreTungsten", 1),
                mat(Mats.MAT_TUNGSTEN, ingot * 2), mat(Mats.MAT_STONE, quart));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_aluminium", sourceOrder,
                HbmIngredient.legacyOre("oreAluminium", 1),
                mat(Mats.MAT_ALUMINIUM, ingot * 2), mat(Mats.MAT_SODIUM, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_coal", sourceOrder,
                HbmIngredient.legacyOre("oreCoal", 1), mat(Mats.MAT_CARBON, gem * 3), mat(Mats.MAT_STONE, quart));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_gold", sourceOrder,
                HbmIngredient.legacyOre("oreGold", 1),
                mat(Mats.MAT_GOLD, ingot * 2), mat(Mats.MAT_LEAD, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_uranium", sourceOrder,
                HbmIngredient.legacyOre("oreUranium", 1),
                mat(Mats.MAT_URANIUM, ingot * 2), mat(Mats.MAT_LEAD, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_thorium232", sourceOrder,
                HbmIngredient.legacyOre("oreThorium232", 1),
                mat(Mats.MAT_THORIUM, ingot * 2), mat(Mats.MAT_URANIUM, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_th232", sourceOrder,
                HbmIngredient.legacyOre("oreTh232", 1),
                mat(Mats.MAT_THORIUM, ingot * 2), mat(Mats.MAT_URANIUM, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_thorium", sourceOrder,
                HbmIngredient.legacyOre("oreThorium", 1),
                mat(Mats.MAT_THORIUM, ingot * 2), mat(Mats.MAT_URANIUM, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_copper", sourceOrder,
                HbmIngredient.legacyOre("oreCopper", 1),
                mat(Mats.MAT_COPPER, ingot * 2), mat(Mats.MAT_STONE, quart));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_lead", sourceOrder,
                HbmIngredient.legacyOre("oreLead", 1),
                mat(Mats.MAT_LEAD, ingot * 2), mat(Mats.MAT_GOLD, nugget),
                mat(Mats.MAT_STONE, quart));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_beryllium", sourceOrder,
                HbmIngredient.legacyOre("oreBeryllium", 1),
                mat(Mats.MAT_BERYLLIUM, ingot * 2), mat(Mats.MAT_STONE, quart));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_cobalt", sourceOrder,
                HbmIngredient.legacyOre("oreCobalt", 1), mat(Mats.MAT_COBALT, ingot), mat(Mats.MAT_STONE, quart));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_redstone", sourceOrder,
                HbmIngredient.legacyOre("oreRedstone", 1),
                mat(Mats.MAT_REDSTONE, ingot * 4), mat(Mats.MAT_STONE, quart));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_hematite", sourceOrder,
                HbmIngredient.legacyOre("oreHematite", 1), mat(Mats.MAT_HEMATITE, ingot));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "ore_malachite", sourceOrder,
                HbmIngredient.legacyOre("oreMalachite", 1), mat(Mats.MAT_MALACHITE, ingot * 6));

        sourceOrder = arcFurnaceCustomSmeltable(consumer, "stone_resource_limestone", sourceOrder,
                HbmIngredient.of(block("stone_resource_limestone"), 1), mat(Mats.MAT_FLUX, dust * 10));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "powder_flux", sourceOrder,
                HbmIngredient.of(item("powder_flux"), 1), mat(Mats.MAT_FLUX, dust));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "charcoal", sourceOrder,
                HbmIngredient.of(Items.CHARCOAL, 1), mat(Mats.MAT_CARBON, nugget * 3));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "powder_ash_wood", sourceOrder,
                HbmIngredient.of(item("powder_ash_wood"), 1), mat(Mats.MAT_CARBON, nugget));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "powder_ash_coal", sourceOrder,
                HbmIngredient.of(item("powder_ash_coal"), 1), mat(Mats.MAT_CARBON, nugget * 2));
        sourceOrder = arcFurnaceCustomSmeltable(consumer, "powder_ash_misc", sourceOrder,
                HbmIngredient.of(item("powder_ash_misc"), 1), mat(Mats.MAT_CARBON, nugget));
        return sourceOrder;
    }

    private static int arcFurnaceCustomSmeltable(Consumer<FinishedRecipe> consumer, String name, int sourceOrder,
            HbmIngredient input, MaterialStack... output) {
        List<MaterialStack> smeltableOutputs = new ArrayList<>();
        for (MaterialStack stack : output) {
            if (stack != null && stack.material != null && stack.material.smeltable == SmeltingBehavior.SMELTABLE) {
                smeltableOutputs.add(stack);
            }
        }
        if (smeltableOutputs.isEmpty()) {
            return sourceOrder;
        }
        GenericMachineRecipeBuilder builder = GenericMachineRecipeBuilder.arcFurnace("arc.custom." + name, 400, 1_000)
                .inputIngredient(input)
                .sourceOrder(sourceOrder);
        for (MaterialStack stack : smeltableOutputs) {
            builder.arcMaterialOutput(stack);
        }
        builder.save(consumer, id("arc_furnace/custom_smeltable/" + name));
        return sourceOrder + 1;
    }

    private static void arcFurnaceFurnaceSmeltableRecipes(Consumer<FinishedRecipe> consumer, int sourceOrder) {
        int ironOreSourceOrder = sourceOrder++;
        arcFurnaceFurnaceSmeltable(consumer, "iron_ore", Blocks.IRON_ORE,
                new ItemStack(Items.IRON_INGOT), ironOreSourceOrder);
        arcFurnaceFurnaceSmeltable(consumer, "deepslate_iron_ore", Blocks.DEEPSLATE_IRON_ORE,
                new ItemStack(Items.IRON_INGOT), ironOreSourceOrder);
        int goldOreSourceOrder = sourceOrder++;
        arcFurnaceFurnaceSmeltable(consumer, "gold_ore", Blocks.GOLD_ORE,
                new ItemStack(Items.GOLD_INGOT), goldOreSourceOrder);
        arcFurnaceFurnaceSmeltable(consumer, "deepslate_gold_ore", Blocks.DEEPSLATE_GOLD_ORE,
                new ItemStack(Items.GOLD_INGOT), goldOreSourceOrder);
        int diamondOreSourceOrder = sourceOrder++;
        arcFurnaceFurnaceSmeltable(consumer, "diamond_ore", Blocks.DIAMOND_ORE,
                new ItemStack(Items.DIAMOND), diamondOreSourceOrder);
        arcFurnaceFurnaceSmeltable(consumer, "deepslate_diamond_ore", Blocks.DEEPSLATE_DIAMOND_ORE,
                new ItemStack(Items.DIAMOND), diamondOreSourceOrder);
        int emeraldOreSourceOrder = sourceOrder++;
        arcFurnaceFurnaceSmeltable(consumer, "emerald_ore", Blocks.EMERALD_ORE,
                new ItemStack(Items.EMERALD), emeraldOreSourceOrder);
        arcFurnaceFurnaceSmeltable(consumer, "deepslate_emerald_ore", Blocks.DEEPSLATE_EMERALD_ORE,
                new ItemStack(Items.EMERALD), emeraldOreSourceOrder);
        int redstoneOreSourceOrder = sourceOrder++;
        arcFurnaceFurnaceSmeltable(consumer, "redstone_ore", Blocks.REDSTONE_ORE,
                new ItemStack(Items.REDSTONE), redstoneOreSourceOrder);
        arcFurnaceFurnaceSmeltable(consumer, "deepslate_redstone_ore", Blocks.DEEPSLATE_REDSTONE_ORE,
                new ItemStack(Items.REDSTONE), redstoneOreSourceOrder);
        int lapisOreSourceOrder = sourceOrder++;
        arcFurnaceFurnaceSmeltable(consumer, "lapis_ore", Blocks.LAPIS_ORE,
                new ItemStack(Items.LAPIS_LAZULI), lapisOreSourceOrder);
        arcFurnaceFurnaceSmeltable(consumer, "deepslate_lapis_ore", Blocks.DEEPSLATE_LAPIS_ORE,
                new ItemStack(Items.LAPIS_LAZULI), lapisOreSourceOrder);
        arcFurnaceFurnaceSmeltable(consumer, "nether_quartz_ore", Blocks.NETHER_QUARTZ_ORE,
                new ItemStack(Items.QUARTZ), sourceOrder++);
        arcFurnaceFurnaceSmeltable(consumer, "clay_ball", Items.CLAY_BALL,
                new ItemStack(Items.BRICK), sourceOrder++);
        arcFurnaceFurnaceSmeltable(consumer, "netherrack", Blocks.NETHERRACK,
                new ItemStack(Items.NETHER_BRICK), sourceOrder++);
        arcFurnaceFurnaceSmeltable(consumer, "powder_iron", item("powder_iron"),
                new ItemStack(Items.IRON_INGOT), sourceOrder++);
        arcFurnaceFurnaceSmeltable(consumer, "powder_gold", item("powder_gold"),
                new ItemStack(Items.GOLD_INGOT), sourceOrder++);
        arcFurnaceFurnaceSmeltable(consumer, "ore_gneiss_iron", block("ore_gneiss_iron"),
                new ItemStack(Items.IRON_INGOT), sourceOrder++);
        arcFurnaceFurnaceSmeltable(consumer, "ore_gneiss_gold", block("ore_gneiss_gold"),
                new ItemStack(Items.GOLD_INGOT), sourceOrder++);
        arcFurnaceFurnaceSmeltable(consumer, "crystal_iron", item("crystal_iron"),
                new ItemStack(Items.IRON_INGOT, 2), sourceOrder++);
        arcFurnaceFurnaceSmeltable(consumer, "crystal_gold", item("crystal_gold"),
                new ItemStack(Items.GOLD_INGOT, 2), sourceOrder);
        arcFurnaceFurnaceSmeltable(consumer, "ore_australium", block("ore_australium"),
                new ItemStack(item("nugget_australium")), sourceOrder + 1);
    }

    private static void arcFurnaceFurnaceSmeltable(Consumer<FinishedRecipe> consumer, String name,
            ItemLike input, ItemStack output, int sourceOrder) {
        GenericMachineRecipeBuilder.arcFurnace("arc.furnace." + name, 400, 1_000)
                .inputItem(input, 1)
                .outputItem(output)
                .sourceOrder(sourceOrder)
                .save(consumer, id("arc_furnace/furnace_smeltable/" + name));
    }

    private static void compatRecipeListenerRecipes(Consumer<FinishedRecipe> consumer) {
        CompatRecipeRegistry.emitRecipeRegisterListeners((recipeId, recipeJson) ->
                consumer.accept(finishedCompatRecipe(recipeId, recipeJson)));
    }

    private static void fusionReactorRecipes(Consumer<FinishedRecipe> consumer) {
        double breederCapacity = 10_000.0D;
        fusionRecipe(consumer, "dd", 750_000, 1_000_000, breederCapacity / 200.0D, 1.0F, 0.2F, 0.2F)
                .inputFluid(HbmFluids.DEUTERIUM, 20)
                .outputFluid(HbmFluids.HELIUM4, 1_000)
                .icon(fluidContainerStack(ModItems.GAS_FULL.get(), 1, HbmFluids.DEUTERIUM, 0, 0))
                .sourceOrder(0)
                .save(consumer, id("fusion_reactor/dd"));
        fusionRecipe(consumer, "do", 250_000, 1_250_000, breederCapacity / 200.0D, 1.0F, 0.2F, 0.6F)
                .inputFluid(HbmFluids.DEUTERIUM, 10)
                .inputFluid(HbmFluids.OXYGEN, 10)
                .outputItem(item("pellet_charged"))
                .icon(fluidContainerStack(ModItems.GAS_FULL.get(), 1, HbmFluids.OXYGEN, 0, 0))
                .sourceOrder(1)
                .save(consumer, id("fusion_reactor/do"));
        fusionRecipe(consumer, "dt", 750_000, 3_750_000, breederCapacity / 100.0D, 1.0F, 0.2F, 0.6F)
                .inputFluid(HbmFluids.DEUTERIUM, 10)
                .inputFluid(HbmFluids.TRITIUM, 10)
                .outputFluid(HbmFluids.HELIUM4, 1_000)
                .icon(fluidContainerStack(ModItems.GAS_FULL.get(), 1, HbmFluids.HELIUM4, 0, 0))
                .sourceOrder(2)
                .save(consumer, id("fusion_reactor/dt"));
        fusionRecipe(consumer, "tcl", 2_500_000, 6_250_000, breederCapacity / 20.0D, 0.8F, 0.6F, 0.4F)
                .inputFluid(HbmFluids.TRITIUM, 10)
                .inputFluid(HbmFluids.CHLORINE, 10)
                .outputItem(item("powder_chlorophyte"))
                .icon(item("powder_chlorophyte"))
                .sourceOrder(3)
                .save(consumer, id("fusion_reactor/tcl"));
        fusionRecipe(consumer, "h3", 500_000, 3_750_000, 0.0D, 0.2F, 0.2F, 1.0F)
                .inputFluid(HbmFluids.HELIUM3, 20)
                .outputFluid(HbmFluids.HELIUM4, 1_000)
                .icon(fluidContainerStack(ModItems.GAS_FULL.get(), 1, HbmFluids.HELIUM3, 0, 0))
                .sourceOrder(4)
                .save(consumer, id("fusion_reactor/h3"));
        fusionRecipe(consumer, "th4", 875_000, 4_000_000, breederCapacity / 20.0D, 0.2F, 0.2F, 1.0F)
                .inputFluid(HbmFluids.TRITIUM, 10)
                .inputFluid(HbmFluids.HELIUM4, 10)
                .outputItem(item("pellet_charged"))
                .icon(fluidContainerStack(ModItems.GAS_FULL.get(), 1, HbmFluids.TRITIUM, 0, 0))
                .sourceOrder(5)
                .save(consumer, id("fusion_reactor/th4"));
        fusionRecipe(consumer, "cl", 3_750_000, 10_000_000, breederCapacity / 10.0D, 1.0F, 0.6F, 0.2F)
                .inputFluid(HbmFluids.CHLORINE, 20)
                .outputItem(item("powder_chlorophyte"))
                .icon(item("powder_chlorophyte"))
                .sourceOrder(6)
                .save(consumer, id("fusion_reactor/cl"));
        fusionRecipe(consumer, "dhc", 10_000_000, 25_000_000, breederCapacity / 5.0D, 0.2F, 0.8F, 0.8F)
                .inputFluid(HbmFluids.DHC, 20)
                .outputItem(item("powder_chlorophyte"))
                .icon(fluidIconStack(HbmFluids.DHC, 0, 0))
                .sourceOrder(7)
                .save(consumer, id("fusion_reactor/dhc"));
        fusionRecipe(consumer, "bf", 1_000_000, 12_500_000, breederCapacity / 5.0D, 0.2F, 1.0F, 0.2F)
                .inputFluid(HbmFluids.BALEFIRE, 15)
                .inputFluid(HbmFluids.AMAT, 5)
                .outputItem(item("powder_balefire"))
                .icon(fluidIconStack(HbmFluids.BALEFIRE, 0, 0))
                .sourceOrder(8)
                .save(consumer, id("fusion_reactor/bf"));
        fusionRecipe(consumer, "stellar", 10_000_000, 50_000_000, breederCapacity, 1.0F, 0.4F, 0.1F)
                .inputFluid(HbmFluids.STELLAR_FLUX, 10)
                .outputItem(item("powder_gold"))
                .icon(fluidIconStack(HbmFluids.STELLAR_FLUX, 0, 0))
                .sourceOrder(9)
                .save(consumer, id("fusion_reactor/stellar"));
    }

    private static void cyclotronRecipes(Consumer<FinishedRecipe> consumer) {
        cyclotron(consumer, "lithium_to_beryllium", "part_lithium", HbmIngredient.legacyOre("dustLithium", 1),
                "powder_beryllium", 50, 0);
        cyclotron(consumer, "lithium_to_boron", "part_lithium", HbmIngredient.legacyOre("dustBeryllium", 1),
                "powder_boron", 50, 1);
        cyclotron(consumer, "lithium_to_coal", "part_lithium", HbmIngredient.legacyOre("dustBoron", 1),
                "powder_coal", 50, 2);
        cyclotron(consumer, "lithium_to_red_phosphorus", "part_lithium",
                HbmIngredient.legacyOre("dustNetherQuartz", 1), "powder_fire", 50, 3);
        cyclotron(consumer, "lithium_to_sulfur", "part_lithium", HbmIngredient.legacyOre("dustPhosphorus", 1),
                "sulfur", 50, 4);
        cyclotron(consumer, "lithium_to_cobalt", "part_lithium", HbmIngredient.legacyOre("dustIron", 1),
                "powder_cobalt", 50, 5);
        cyclotron(consumer, "lithium_to_zirconium", "part_lithium", HbmIngredient.of(item("powder_strontium"), 1),
                "powder_zirconium", 50, 6);
        cyclotron(consumer, "lithium_to_mercury", "part_lithium", HbmIngredient.legacyOre("dustGold", 1),
                "ingot_mercury", 50, 7);
        cyclotron(consumer, "lithium_to_astatine", "part_lithium", HbmIngredient.legacyOre("dustPolonium", 1),
                "powder_astatine", 50, 8);
        cyclotron(consumer, "lithium_to_cerium", "part_lithium", HbmIngredient.legacyOre("dustLanthanium", 1),
                "powder_cerium", 50, 9);
        cyclotron(consumer, "lithium_to_thorium", "part_lithium", HbmIngredient.legacyOre("dustActinium", 1),
                "powder_thorium", 50, 10);
        cyclotron(consumer, "lithium_to_neptunium", "part_lithium", HbmIngredient.legacyOre("dustUranium", 1),
                "powder_neptunium", 50, 11);
        cyclotron(consumer, "lithium_to_plutonium", "part_lithium", HbmIngredient.legacyOre("dustNp237", 1),
                "powder_plutonium", 50, 12);

        cyclotron(consumer, "beryllium_to_boron", "part_beryllium", HbmIngredient.legacyOre("dustLithium", 1),
                "powder_boron", 25, 13);
        cyclotron(consumer, "beryllium_to_sulfur", "part_beryllium",
                HbmIngredient.legacyOre("dustNetherQuartz", 1), "sulfur", 25, 14);
        cyclotron(consumer, "beryllium_to_iron", "part_beryllium", HbmIngredient.legacyOre("dustTitanium", 1),
                "powder_iron", 25, 15);
        cyclotron(consumer, "beryllium_to_copper", "part_beryllium", HbmIngredient.legacyOre("dustCobalt", 1),
                "powder_copper", 25, 16);
        cyclotron(consumer, "beryllium_to_niobium", "part_beryllium",
                HbmIngredient.of(item("powder_strontium"), 1), "powder_niobium", 25, 17);
        cyclotron(consumer, "beryllium_to_neodymium", "part_beryllium",
                HbmIngredient.of(item("powder_cerium"), 1), "powder_neodymium", 25, 18);
        cyclotron(consumer, "beryllium_to_uranium", "part_beryllium", HbmIngredient.legacyOre("dustThorium", 1),
                "powder_uranium", 25, 19);

        cyclotron(consumer, "carbon_to_aluminium", "part_carbon", HbmIngredient.legacyOre("dustBoron", 1),
                "powder_aluminium", 10, 20);
        cyclotron(consumer, "carbon_to_titanium", "part_carbon", HbmIngredient.legacyOre("dustSulfur", 1),
                "powder_titanium", 10, 21);
        cyclotron(consumer, "carbon_to_cobalt", "part_carbon", HbmIngredient.legacyOre("dustTitanium", 1),
                "powder_cobalt", 10, 22);
        cyclotron(consumer, "carbon_to_lanthanium", "part_carbon", HbmIngredient.of(item("powder_caesium"), 1),
                "powder_lanthanium", 10, 23);
        cyclotron(consumer, "carbon_to_gold", "part_carbon", HbmIngredient.of(item("powder_neodymium"), 1),
                "powder_gold", 10, 24);
        cyclotron(consumer, "carbon_to_polonium", "part_carbon", HbmIngredient.of(item("ingot_mercury"), 1),
                "powder_polonium", 10, 25);
        cyclotron(consumer, "carbon_to_ra226", "part_carbon", HbmIngredient.legacyOre("dustLead", 1),
                "powder_ra226", 10, 26);
        cyclotron(consumer, "carbon_to_actinium", "part_carbon", HbmIngredient.of(item("powder_astatine"), 1),
                "powder_actinium", 10, 27);

        cyclotron(consumer, "copper_to_quartz", "part_copper", HbmIngredient.legacyOre("dustBeryllium", 1),
                "powder_quartz", 15, 28);
        cyclotron(consumer, "copper_to_bromine", "part_copper", HbmIngredient.legacyOre("dustCoal", 1),
                "powder_bromine", 15, 29);
        cyclotron(consumer, "copper_to_strontium", "part_copper", HbmIngredient.legacyOre("dustTitanium", 1),
                "powder_strontium", 15, 30);
        cyclotron(consumer, "copper_to_niobium", "part_copper", HbmIngredient.legacyOre("dustIron", 1),
                "powder_niobium", 15, 31);
        cyclotron(consumer, "copper_to_iodine", "part_copper", HbmIngredient.of(item("powder_bromine"), 1),
                "powder_iodine", 15, 32);
        cyclotron(consumer, "copper_to_neodymium", "part_copper", HbmIngredient.of(item("powder_strontium"), 1),
                "powder_neodymium", 15, 33);
        cyclotron(consumer, "copper_to_caesium", "part_copper", HbmIngredient.of(item("powder_niobium"), 1),
                "powder_caesium", 15, 34);
        cyclotron(consumer, "copper_to_polonium", "part_copper", HbmIngredient.of(item("powder_iodine"), 1),
                "powder_polonium", 15, 35);
        cyclotron(consumer, "copper_to_actinium", "part_copper", HbmIngredient.of(item("powder_caesium"), 1),
                "powder_actinium", 15, 36);
        cyclotron(consumer, "copper_to_uranium", "part_copper", HbmIngredient.legacyOre("dustGold", 1),
                "powder_uranium", 15, 37);

        cyclotron(consumer, "plutonium_to_tennessine_from_phosphorus", "part_plutonium",
                HbmIngredient.legacyOre("dustPhosphorus", 1), "powder_tennessine", 100, 38);
        cyclotron(consumer, "plutonium_to_tennessine", "part_plutonium",
                HbmIngredient.legacyOre("dustPlutonium", 1), "powder_tennessine", 100, 39);
        cyclotron(consumer, "plutonium_to_australium", "part_plutonium",
                HbmIngredient.of(item("powder_tennessine"), 1), "powder_australium", 100, 40);
        cyclotron(consumer, "plutonium_to_schrabidium_nugget", "part_plutonium",
                HbmIngredient.of(item("pellet_charged"), 1), "nugget_schrabidium", 1000, 41);
    }

    private static void cyclotron(Consumer<FinishedRecipe> consumer, String name, String particle,
            HbmIngredient input, String output, int antimatter, int sourceOrder) {
        JsonObject json = new JsonObject();
        json.add("particle", HbmIngredient.of(item(particle), 1).toJson());
        json.add("input", input.toJson());
        json.add("output", HbmItemOutput.of(new ItemStack(item(output))).toJson());
        json.addProperty("antimatter", antimatter);
        json.addProperty("source_order", sourceOrder);
        consumer.accept(finishedRecipe(id("cyclotron/" + name), json, ModRecipes.CYCLOTRON.serializer().get()));
    }

    private static void particleAcceleratorRecipes(Consumer<FinishedRecipe> consumer) {
        particleAccelerator(consumer, "amat_from_hydrogen_copper", HbmIngredient.of(item("particle_hydrogen"), 1),
                HbmIngredient.of(item("particle_copper"), 1), 300, out("particle_amat", 1), ItemStack.EMPTY, 0);
        particleAccelerator(consumer, "aschrab_from_amat", HbmIngredient.of(item("particle_amat"), 1),
                HbmIngredient.of(item("particle_amat"), 1), 400, out("particle_aschrab", 1), ItemStack.EMPTY, 1);
        particleAccelerator(consumer, "dark_from_aschrab", HbmIngredient.of(item("particle_aschrab"), 1),
                HbmIngredient.of(item("particle_aschrab"), 1), 10_000, out("particle_dark", 1), ItemStack.EMPTY, 2);
        particleAccelerator(consumer, "muon_from_hydrogen_amat", HbmIngredient.of(item("particle_hydrogen"), 1),
                HbmIngredient.of(item("particle_amat"), 1), 2_500, out("particle_muon", 1), ItemStack.EMPTY, 3);
        particleAccelerator(consumer, "higgs_from_hydrogen_lead", HbmIngredient.of(item("particle_hydrogen"), 1),
                HbmIngredient.of(item("particle_lead"), 1), 6_500, out("particle_higgs", 1), ItemStack.EMPTY, 4);
        particleAccelerator(consumer, "tachyon_from_muon_higgs", HbmIngredient.of(item("particle_muon"), 1),
                HbmIngredient.of(item("particle_higgs"), 1), 5_000, out("particle_tachyon", 1), ItemStack.EMPTY, 5);
        particleAccelerator(consumer, "strange_from_muon_dark", HbmIngredient.of(item("particle_muon"), 1),
                HbmIngredient.of(item("particle_dark"), 1), 12_500, out("particle_strange", 1), ItemStack.EMPTY, 6);
        particleAccelerator(consumer, "sparkticle_from_strange_magic", HbmIngredient.of(item("particle_strange"), 1),
                HbmIngredient.of(item("powder_magic"), 1), 12_500, out("particle_sparkticle", 1), out("dust", 1), 7);
        particleAccelerator(consumer, "digamma_from_sparkticle_higgs",
                HbmIngredient.of(item("particle_sparkticle"), 1), HbmIngredient.of(item("particle_higgs"), 1),
                70_000, out("particle_digamma", 1), ItemStack.EMPTY, 8);
        particleAccelerator(consumer, "degenerate_matter", HbmIngredient.of(item("item_expensive_gold_dust"), 1),
                HbmIngredient.legacyOre("ingotSchrabidium", 1), 10_000,
                out("item_expensive_degenerate_matter", 1), ItemStack.EMPTY, 9);
        particleAccelerator(consumer, "chicken_nugget", HbmIngredient.of(Items.CHICKEN, 1),
                HbmIngredient.of(Items.CHICKEN, 1), 100, out("nugget", 1), out("nugget", 1), 10);
    }

    private static void particleAccelerator(Consumer<FinishedRecipe> consumer, String name, HbmIngredient input1,
            HbmIngredient input2, int momentum, ItemStack output1, ItemStack output2, int sourceOrder) {
        JsonObject json = CompatRecipeRegistry.createParticleAccelerator(input1, input2, momentum, output1,
                output2, sourceOrder);
        consumer.accept(finishedCompatRecipe(id("particle_accelerator/" + name), json));
    }

    private static void exposureChamberRecipes(Consumer<FinishedRecipe> consumer) {
        exposureChamber(consumer, "higgs_uranium", "particle_higgs", HbmIngredient.legacyOre("ingotUranium", 1),
                "ingot_schraranium", 0);
        exposureChamber(consumer, "higgs_u238", "particle_higgs", HbmIngredient.legacyOre("ingotU238", 1),
                "ingot_schrabidium", 1);
        exposureChamber(consumer, "dark_plutonium", "particle_dark", HbmIngredient.legacyOre("ingotPlutonium", 1),
                "ingot_euphemium", 2);
        exposureChamber(consumer, "sparkticle_schrabidium", "particle_sparkticle",
                HbmIngredient.legacyOre("ingotSchrabidium", 1), "ingot_dineutronium", 3);
    }

    private static void exposureChamber(Consumer<FinishedRecipe> consumer, String name, String particle,
            HbmIngredient ingredient, String output, int sourceOrder) {
        JsonObject json = new JsonObject();
        json.add("particle", HbmIngredient.of(item(particle), 1).toJson());
        json.add("ingredient", ingredient.toJson());
        json.add("output", HbmItemOutput.of(new ItemStack(item(output))).toJson());
        json.addProperty("source_order", sourceOrder);
        consumer.accept(finishedRecipe(id("exposure_chamber/" + name), json,
                ModRecipes.EXPOSURE_CHAMBER.serializer().get()));
    }

    private static void combinationOvenRecipes(Consumer<FinishedRecipe> consumer) {
        combinationOven(consumer, "coal_gem", HbmIngredient.legacyOre("gemCoal", 1),
                new ItemStack(item("coke_coal")), HbmFluids.COALCREOSOTE, 100);
        combinationOven(consumer, "coal_dust", HbmIngredient.legacyOre("dustCoal", 1),
                new ItemStack(item("coke_coal")), HbmFluids.COALCREOSOTE, 100);
        combinationOven(consumer, "coal_briquette", HbmIngredient.of(item("briquette_coal"), 1),
                new ItemStack(item("coke_coal")), HbmFluids.COALCREOSOTE, 150);

        combinationOven(consumer, "lignite_gem", HbmIngredient.legacyOre("gemLignite", 1),
                new ItemStack(item("coke_lignite")), HbmFluids.COALCREOSOTE, 50);
        combinationOven(consumer, "lignite_dust", HbmIngredient.legacyOre("dustLignite", 1),
                new ItemStack(item("coke_lignite")), HbmFluids.COALCREOSOTE, 50);
        combinationOven(consumer, "lignite_briquette", HbmIngredient.of(item("briquette_lignite"), 1),
                new ItemStack(item("coke_lignite")), HbmFluids.COALCREOSOTE, 100);

        combinationOven(consumer, "chlorocalcite_dust", HbmIngredient.legacyOre("dustChlorocalcite", 1),
                new ItemStack(item("powder_calcium")), HbmFluids.CHLORINE, 250);
        combinationOven(consumer, "molysite_dust", HbmIngredient.legacyOre("dustMolysite", 1),
                new ItemStack(Items.IRON_INGOT), HbmFluids.CHLORINE, 250);
        combinationOven(consumer, "cinnabar_crystal", HbmIngredient.legacyOre("crystalCinnabar", 1),
                new ItemStack(item("sulfur")), HbmFluids.MERCURY, 100);
        combinationOven(consumer, "glowstone_dust", HbmIngredient.of(Items.GLOWSTONE_DUST, 1),
                new ItemStack(item("sulfur")), HbmFluids.CHLORINE, 100);
        combinationOven(consumer, "sodalite_gem", HbmIngredient.legacyOre("gemSodalite", 1),
                new ItemStack(item("powder_sodium")), HbmFluids.CHLORINE, 100);
        combinationOven(consumer, "cryolite_chunk", HbmIngredient.of(item("chunk_ore_cryolite"), 1),
                new ItemStack(item("powder_aluminium")), HbmFluids.LYE, 150);
        combinationOven(consumer, "sodium_dust", HbmIngredient.legacyOre("dustSodium", 1),
                ItemStack.EMPTY, HbmFluids.SODIUM, 100);
        combinationOven(consumer, "limestone_dust", HbmIngredient.legacyOre("dustLimestone",
                Ingredient.of(item("powder_limestone")), 1),
                new ItemStack(item("powder_calcium")), HbmFluids.CARBONDIOXIDE, 50);

        combinationOven(consumer, "logs", HbmIngredient.legacyOre("logWood", 1),
                new ItemStack(Items.CHARCOAL), HbmFluids.WOODOIL, 250);
        combinationOven(consumer, "saplings", HbmIngredient.legacyOre("treeSapling", 1),
                new ItemStack(item("powder_ash_wood")), HbmFluids.WOODOIL, 50);
        combinationOven(consumer, "wood_briquette", HbmIngredient.of(item("briquette_wood"), 1),
                new ItemStack(Items.CHARCOAL), HbmFluids.WOODOIL, 500);

        combinationOven(consumer, "oil_tar_crude", HbmIngredient.of(item("oil_tar_crude"), 1),
                new ItemStack(item("coke_petroleum")), null, 0);
        combinationOven(consumer, "oil_tar_crack", HbmIngredient.of(item("oil_tar_crack"), 1),
                new ItemStack(item("coke_petroleum")), null, 0);
        combinationOven(consumer, "oil_tar_coal", HbmIngredient.of(item("oil_tar_coal"), 1),
                new ItemStack(item("coke_coal")), null, 0);
        combinationOven(consumer, "oil_tar_wood", HbmIngredient.of(item("oil_tar_wood"), 1),
                new ItemStack(item("coke_coal")), null, 0);

        combinationOven(consumer, "sugar_cane", HbmIngredient.of(Items.SUGAR_CANE, 1),
                new ItemStack(Items.SUGAR, 2), HbmFluids.ETHANOL, 50);
        combinationOven(consumer, "clay", HbmIngredient.of(Blocks.CLAY, 1),
                new ItemStack(Blocks.BRICKS), null, 0);

        bedrockOreCombinationOvenRecipes(consumer);
    }

    private static void bedrockOreCombinationOvenRecipes(Consumer<FinishedRecipe> consumer) {
        for (BedrockOreType type : BedrockOreType.values()) {
            String suffix = type.suffix();
            bedrockOreCombinationOven(consumer, suffix + "_base", BedrockOreGrade.BASE, type,
                    BedrockOreGrade.BASE_ROASTED);
            bedrockOreCombinationOven(consumer, suffix + "_primary", BedrockOreGrade.PRIMARY, type,
                    BedrockOreGrade.PRIMARY_ROASTED);
            bedrockOreCombinationOven(consumer, suffix + "_sulfuric_byproduct",
                    BedrockOreGrade.SULFURIC_BYPRODUCT, type, BedrockOreGrade.SULFURIC_ROASTED);
            bedrockOreCombinationOven(consumer, suffix + "_solvent_byproduct",
                    BedrockOreGrade.SOLVENT_BYPRODUCT, type, BedrockOreGrade.SOLVENT_ROASTED);
            bedrockOreCombinationOven(consumer, suffix + "_rad_byproduct",
                    BedrockOreGrade.RAD_BYPRODUCT, type, BedrockOreGrade.RAD_ROASTED);
        }
    }

    private static void bedrockOreCombinationOven(Consumer<FinishedRecipe> consumer, String name,
            BedrockOreGrade inputGrade, BedrockOreType type, BedrockOreGrade outputGrade) {
        combinationOven(consumer, "bedrock_ore_" + name, bedrockOreInput(inputGrade, type),
                bedrockOre(outputGrade, type), HbmFluids.VITRIOL, 50);
    }

    private static void combinationOven(Consumer<FinishedRecipe> consumer, String name, HbmIngredient input,
            ItemStack outputItem, @Nullable FluidType outputFluid, int outputFluidAmount) {
        JsonObject json = new JsonObject();
        json.add("input", input.toJson());
        if (!outputItem.isEmpty()) {
            json.add("output_item", HbmItemOutput.of(outputItem).toJson());
        }
        if (outputFluid != null && outputFluidAmount > 0) {
            json.add("output_fluid", combinationFluid(outputFluid, outputFluidAmount));
        }
        if (!json.has("output_item") && !json.has("output_fluid")) {
            throw new IllegalStateException("HBM combination oven recipe has no output: " + name);
        }
        consumer.accept(finishedRecipe(id("combination_oven/" + name), json,
                ModRecipes.COMBINATION_OVEN.serializer().get()));
    }

    private static JsonObject combinationFluid(FluidType fluid, int amount) {
        JsonObject object = new JsonObject();
        object.addProperty("fluid", fluid.getName());
        object.addProperty("amount", amount);
        return object;
    }

    private static void compressorRecipes(Consumer<FinishedRecipe> consumer) {
        compressor(consumer, "petroleum_0_to_petroleum_1",
                HbmFluids.PETROLEUM, 2_000, 0, HbmFluids.PETROLEUM, 2_000, 1, 20);
        compressor(consumer, "petroleum_1_to_lpg",
                HbmFluids.PETROLEUM, 2_000, 1, HbmFluids.LPG, 1_000, 0, 20);
        compressor(consumer, "blood_3_to_heavyoil",
                HbmFluids.BLOOD, 1_000, 3, HbmFluids.HEAVYOIL, 250, 0, 200);
        compressor(consumer, "perfluoromethyl_0_to_perfluoromethyl_1",
                HbmFluids.PERFLUOROMETHYL, 1_000, 0, HbmFluids.PERFLUOROMETHYL, 1_000, 1, 50);
        compressor(consumer, "perfluoromethyl_1_to_cold",
                HbmFluids.PERFLUOROMETHYL, 1_000, 1, HbmFluids.PERFLUOROMETHYL_COLD, 1_000, 0, 50);
    }

    private static void compressor(Consumer<FinishedRecipe> consumer, String name,
            FluidType inputType, int inputAmount, int inputPressure,
            FluidType outputType, int outputAmount, int outputPressure, int duration) {
        JsonObject json = new JsonObject();
        json.add("input", legacyFluidStack(inputType, inputAmount, inputPressure));
        json.add("output", legacyFluidStack(outputType, outputAmount, outputPressure));
        json.addProperty("duration", duration);
        consumer.accept(finishedRecipe(id("compressor/" + name), json,
                ModRecipes.COMPRESSOR.serializer().get()));
    }

    private static JsonObject legacyFluidStack(FluidType fluid, int amount, int pressure) {
        JsonObject object = new JsonObject();
        object.addProperty("fluid", new ResourceLocation("hbm", fluid.toPath()).toString());
        object.addProperty("amount", amount);
        object.addProperty("pressure", pressure);
        return object;
    }

    private static void diFurnaceRecipes(Consumer<FinishedRecipe> consumer) {
        diFurnace(consumer, "steel_from_ingot", "difurnace.steelFromIngot", 0,
                diFurnaceDictFrame("Iron", "ingot", "plate", "dust"),
                diFurnaceDictFrame("Coal", "gem", "dust"),
                diFurnaceOutput("ingot_steel", 1));
        diFurnace(consumer, "steel_from_coke", "difurnace.steelFromCoke", 1,
                diFurnaceDictFrame("Iron", "ingot", "plate", "dust"),
                diFurnaceDictFrame("AnyCoke", "gem"),
                diFurnaceOutput("ingot_steel", 1));
        diFurnace(consumer, "steel_from_ore_coal", "difurnace.steelFromOreCoal", 2,
                diFurnaceInput(HbmIngredient.legacyOre("oreIron", 1)),
                diFurnaceDictFrame("Coal", "gem", "dust"),
                diFurnaceOutput("ingot_steel", 2));
        diFurnace(consumer, "steel_from_ore_coke", "difurnace.steelFromOreCoke", 3,
                diFurnaceInput(HbmIngredient.legacyOre("oreIron", 1)),
                diFurnaceDictFrame("AnyCoke", "gem"),
                diFurnaceOutput("ingot_steel", 3));
        diFurnace(consumer, "steel_from_ore_flux", "difurnace.steelFromOreFlux", 4,
                diFurnaceInput(HbmIngredient.legacyOre("oreIron", 1)),
                diFurnaceInput(legacyHbmItem("powder_flux")),
                diFurnaceOutput("ingot_steel", 3));

        diFurnace(consumer, "red_copper", "difurnace.redCopper", 5,
                diFurnaceDictFrame("Copper", "ingot", "plate", "dust"),
                diFurnaceDictFrame("Redstone", "dust"),
                diFurnaceOutput("ingot_red_copper", 2));
        diFurnace(consumer, "canister_napalm", "difurnace.canisterNapalm", 6,
                diFurnaceGasolineCanisterInput(),
                diFurnaceInput(HbmIngredient.legacyOre("slimeball", Ingredient.of(Items.SLIME_BALL), 1)),
                diFurnaceOutput("canister_napalm", 1));
        diFurnace(consumer, "magnetized_tungsten", "difurnace.magnetizedTungsten", 7,
                diFurnaceDictFrame("Tungsten", "ingot", "dust"),
                diFurnaceInput(HbmIngredient.legacyOre("nuggetSchrabidium", 1)),
                diFurnaceOutput("ingot_magnetized_tungsten", 1));
        diFurnace(consumer, "tcalloy", "difurnace.tcalloy", 8,
                diFurnaceDictFrame("Steel", "ingot", "plate", "dust"),
                diFurnaceInput(HbmIngredient.legacyOre("nuggetTechnetium99", 1)),
                diFurnaceOutput("ingot_tcalloy", 1));
        diFurnace(consumer, "paa", "difurnace.paa", 9,
                diFurnaceInput(HbmIngredient.legacyOre("plateGold", 1)),
                diFurnaceInput(legacyHbmItem("plate_mixed")),
                diFurnaceOutput("plate_paa", 2));
        diFurnace(consumer, "starmetal", "difurnace.starmetal", 10,
                diFurnaceDictFrame("Saturnite", "ingot", "plate"),
                diFurnaceInput(legacyHbmItem("ingot_meteorite")),
                diFurnaceOutput("ingot_starmetal", 2));
        diFurnace(consumer, "meteorite", "difurnace.meteorite", 11,
                diFurnaceDictFrame("Cobalt", "ingot", "dust"),
                diFurnaceInput(legacyHbmItem("powder_meteorite")),
                diFurnaceOutput("ingot_meteorite", 1));
        diFurnace(consumer, "meteorite_sword_alloyed", "difurnace.meteoriteSwordAlloyed", 12,
                diFurnaceInput(legacyHbmItem("meteorite_sword_hardened")),
                diFurnaceDictFrame("Cobalt", "ingot", "dust"),
                diFurnaceOutput("meteorite_sword_alloyed", 1), true);
    }

    private static void diFurnace(Consumer<FinishedRecipe> consumer, String name, String internalName,
            int sourceOrder, JsonObject firstInput, JsonObject secondInput, HbmItemOutput output) {
        diFurnace(consumer, name, internalName, sourceOrder, firstInput, secondInput, output, false);
    }

    private static void diFurnace(Consumer<FinishedRecipe> consumer, String name, String internalName,
            int sourceOrder, JsonObject firstInput, JsonObject secondInput, HbmItemOutput output,
            boolean legacyHidden) {
        JsonObject json = new JsonObject();
        json.addProperty("internal_name", internalName);
        json.addProperty("source_order", sourceOrder);
        JsonArray inputs = new JsonArray();
        inputs.add(firstInput);
        inputs.add(secondInput);
        json.add("inputs", inputs);
        json.add("output", output.toJson());
        if (legacyHidden) {
            json.addProperty("legacy_hidden", true);
        }
        consumer.accept(finishedRecipe(id("difurnace/" + name), json, ModRecipes.DIFURNACE.serializer().get()));
    }

    private static JsonObject diFurnaceInput(HbmIngredient input) {
        return input.toJson();
    }

    private static JsonObject diFurnaceDictFrame(String materialName, String... shapes) {
        JsonArray alternatives = new JsonArray();
        for (String shape : shapes) {
            addLegacyOreAlternative(alternatives, shape + materialName);
        }
        JsonObject input = HbmIngredient.of(Ingredient.fromJson(alternatives), 1).toJson();
        input.addProperty("legacy_dictframe", materialName);
        return input;
    }

    private static void addLegacyOreAlternative(JsonArray alternatives, String legacyOreName) {
        JsonObject tag = new JsonObject();
        tag.addProperty("tag", LegacyOreDictionaryMappings.itemTagId(legacyOreName).toString());
        alternatives.add(tag);
    }

    private static JsonObject diFurnaceGasolineCanisterInput() {
        JsonObject input = HbmIngredient.partialNbt(
                fluidContainerStack(ModItems.CANISTER_FULL.get(), 1, HbmFluids.GASOLINE, 1_000, 0)).toJson();
        input.addProperty("legacy_item", "canister_full");
        return input;
    }

    private static HbmItemOutput diFurnaceOutput(String itemName, int count) {
        return HbmItemOutput.of(new ItemStack(item(itemName), count));
    }

    private static void blastFurnaceRecipes(Consumer<FinishedRecipe> consumer) {
        blastFurnace(consumer, "steel_from_ingot", "blast.steelFromIngot", 0, 800,
                List.of(blastInput(HbmIngredient.legacyOre("ingotIron", 2)),
                        blastInput(HbmIngredient.legacyOre("sand", 1))),
                List.of(blastOutput("ingot_steel", 2), slagOutput(MaterialShapes.INGOT.q(1))));
        blastFurnace(consumer, "steel_from_dust", "blast.steelFromDust", 1, 800,
                List.of(blastInput(HbmIngredient.legacyOre("dustIron", 2)),
                        blastInput(HbmIngredient.legacyOre("sand", 1))),
                List.of(blastOutput("ingot_steel", 2), slagOutput(MaterialShapes.INGOT.q(1))));
        blastFurnace(consumer, "steel_from_ore", "blast.steelFromOre", 2, 800,
                List.of(blastInput(HbmIngredient.legacyOre("oreIron", 1)),
                        blastInput(HbmIngredient.legacyOre("sand", 1))),
                List.of(blastOutput("ingot_steel", 2), slagOutput(MaterialShapes.INGOT.q(2))));
        blastFurnace(consumer, "steel_with_flux", "blast.steelWithFlux", 3, 1_200,
                List.of(blastInput(HbmIngredient.legacyOre("oreIron", 1)),
                        blastInput(HbmIngredient.of(item("powder_flux"), 1))),
                List.of(blastOutput("ingot_steel", 3), slagOutput(MaterialShapes.INGOT.q(2))));

        blastFurnace(consumer, "red_copper", "blast.mingrade", 4, 400,
                List.of(blastInput(HbmIngredient.legacyOre("ingotCopper", 1)),
                        blastLegacyOreItemInput(Items.REDSTONE, 1, "dustRedstone")),
                List.of(blastOutput("ingot_red_copper", 2)));
        blastFurnace(consumer, "red_copper_dust", "blast.mingradeDust", 5, 400,
                List.of(blastInput(HbmIngredient.legacyOre("dustCopper", 1)),
                        blastLegacyOreItemInput(Items.REDSTONE, 1, "dustRedstone")),
                List.of(blastOutput("ingot_red_copper", 2)));
        blastFurnace(consumer, "red_copper_ingot_redstone_ingot", "blast.mingradeIngot", 6, 400,
                List.of(blastInput(HbmIngredient.legacyOre("ingotCopper", 1)),
                        blastInput(HbmIngredient.legacyOre("ingotRedstone", 1))),
                List.of(blastOutput("ingot_red_copper", 2)));
        blastFurnace(consumer, "red_copper_dust_redstone_ingot", "blast.mingradeCursed", 7, 400,
                List.of(blastInput(HbmIngredient.legacyOre("dustCopper", 1)),
                        blastInput(HbmIngredient.legacyOre("ingotRedstone", 1))),
                List.of(blastOutput("ingot_red_copper", 2)));
        blastFurnace(consumer, "red_copper_ore", "blast.mingradeOre", 8, 1_200,
                List.of(blastLegacyOreIngredientInput(Ingredient.of(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE),
                                1, "oreCopper"),
                        blastLegacyOreItemInput(Items.REDSTONE, 6, "dustRedstone")),
                List.of(blastOutput("ingot_red_copper", 6), slagOutput(MaterialShapes.INGOT.q(1))));

        blastFurnace(consumer, "meteorite_sword", "blast.meteorSword", 9, 1_200,
                List.of(blastInput(HbmIngredient.legacyOre("ingotCobalt", 1)),
                        blastLegacyItemInput(item("meteorite_sword_hardened"), 1, "meteorite_sword_hardened")),
                List.of(blastOutput("meteorite_sword_alloyed", 1)));
        blastFurnace(consumer, "starmetal", "blast.starmetal", 10, 600,
                List.of(blastInput(HbmIngredient.legacyOre("ingotSaturnite", 1)),
                        blastLegacyItemInput(item("powder_meteorite"), 1, "powder_meteorite")),
                List.of(blastOutput("ingot_starmetal", 1)));
        blastFurnace(consumer, "paa", "blast.paa", 11, 600,
                List.of(blastInput(HbmIngredient.legacyOre("ingotGold", 1)),
                        blastLegacyItemInput(item("plate_mixed"), 1, "plate_mixed")),
                List.of(blastOutput("plate_paa", 1)));

        blastFurnace(consumer, "firebrick", "blast.firebrick", 12, 800,
                List.of(blastInput(HbmIngredient.legacyOre("dustAluminum", 1)),
                        blastLegacyItemInput(Items.CLAY_BALL, 7, "minecraft:clay_ball")),
                List.of(blastOutput("ingot_firebrick", 8)));
        blastFurnace(consumer, "firebrick_limestone", "blast.firebrickLimestone", 13, 800,
                List.of(blastInput(HbmIngredient.legacyOre("oreLimestone", 1)),
                        blastLegacyItemInput(Items.CLAY_BALL, 6, "minecraft:clay_ball")),
                List.of(blastOutput("ingot_firebrick", 8)));
    }

    private static void blastFurnace(Consumer<FinishedRecipe> consumer, String name, String legacyName,
            int sourceOrder, int duration,
            List<JsonObject> inputs, List<HbmItemOutput> outputs) {
        JsonObject json = new JsonObject();
        json.addProperty("name", legacyName);
        json.addProperty("duration", duration);
        json.addProperty("source_order", sourceOrder);
        JsonArray inputArray = new JsonArray();
        inputs.forEach(inputArray::add);
        json.add("inputs", inputArray);
        JsonArray outputArray = new JsonArray();
        outputs.forEach(output -> outputArray.add(output.toJson()));
        json.add("outputs", outputArray);
        consumer.accept(finishedRecipe(id("blast_furnace/" + name), json,
                ModRecipes.BLAST_FURNACE.serializer().get()));
    }

    private static JsonObject blastInput(HbmIngredient input) {
        return input.toJson();
    }

    private static JsonObject blastLegacyItemInput(ItemLike item, int count, String legacyItem) {
        JsonObject input = HbmIngredient.of(item, count).toJson();
        input.addProperty("legacy_item", legacyItem);
        return input;
    }

    private static JsonObject blastLegacyOreItemInput(ItemLike item, int count, String legacyOre) {
        JsonObject input = HbmIngredient.of(item, count).toJson();
        input.addProperty("legacy_ore", legacyOre);
        return input;
    }

    private static JsonObject blastLegacyOreIngredientInput(Ingredient ingredient, int count, String legacyOre) {
        JsonObject input = new JsonObject();
        input.add("ingredient", ingredient.toJson());
        input.addProperty("count", count);
        input.addProperty("legacy_ore", legacyOre);
        return input;
    }

    private static HbmItemOutput blastOutput(String itemName, int count) {
        return HbmItemOutput.of(new ItemStack(item(itemName), count));
    }

    private static HbmItemOutput slagOutput(int amount) {
        ItemStack stack = new ItemStack(item("scraps"));
        CompoundTag tag = new CompoundTag();
        tag.putInt("mat", Mats.MAT_SLAG.id);
        tag.putInt("amount", amount);
        stack.setTag(tag);
        return HbmItemOutput.of(stack);
    }

    private static void breedingReactorRecipes(Consumer<FinishedRecipe> consumer) {
        int sourceOrder = 0;
        sourceOrder = breedingRod(consumer, "lithium", "tritium", 200, sourceOrder);
        sourceOrder = breedingRod(consumer, "co", "co60", 100, sourceOrder);
        sourceOrder = breedingRod(consumer, "ra226", "ac227", 300, sourceOrder);
        sourceOrder = breedingRod(consumer, "th232", "thf", 500, sourceOrder);
        sourceOrder = breedingRod(consumer, "u235", "np237", 300, sourceOrder);
        sourceOrder = breedingRod(consumer, "np237", "pu238", 200, sourceOrder);
        sourceOrder = breedingRod(consumer, "pu238", "pu239", 1_000, sourceOrder);
        sourceOrder = breedingRod(consumer, "u238", "rgp", 300, sourceOrder);
        sourceOrder = breedingRod(consumer, "uranium", "rgp", 200, sourceOrder);
        sourceOrder = breedingRod(consumer, "rgp", "waste", 200, sourceOrder);
        breedingRecipe(consumer, "meteorite_sword_etched", "meteorite_sword_bred", 1_000, sourceOrder);
    }

    private static int breedingRod(Consumer<FinishedRecipe> consumer, String input, String output, int flux,
            int sourceOrder) {
        breedingRecipe(consumer, "rod_" + input, "rod_" + output, flux, sourceOrder++);
        breedingRecipe(consumer, "rod_dual_" + input, "rod_dual_" + output, flux * 2, sourceOrder++);
        breedingRecipe(consumer, "rod_quad_" + input, "rod_quad_" + output, flux * 3, sourceOrder++);
        return sourceOrder;
    }

    private static void breedingRecipe(Consumer<FinishedRecipe> consumer, String input, String output, int flux,
            int sourceOrder) {
        JsonObject json = new JsonObject();
        json.add("input", HbmIngredient.of(item(input), 1).toJson());
        json.add("output", HbmItemOutput.of(new ItemStack(item(output))).toJson());
        json.addProperty("flux", flux);
        json.addProperty("source_order", sourceOrder);
        consumer.accept(finishedRecipe(id("breeding_reactor/" + input), json,
                ModRecipes.BREEDING_REACTOR.serializer().get()));
    }

    private static void fuelPoolRecipes(Consumer<FinishedRecipe> consumer) {
        int sourceOrder = 0;
        for (String name : LEGACY_DEPLETED_WASTE_ITEMS) {
            RegistryObject<Item> item = ModItems.legacyItem(name);
            if (item != null) {
                fuelPoolRecipe(consumer, name,
                        DepletedFuelItem.stack(item.get(), DepletedFuelItem.HOT_DAMAGE),
                        DepletedFuelItem.stack(item.get(), DepletedFuelItem.COLD_DAMAGE),
                        sourceOrder++);
            }
        }

        int pwrCount = Math.min(ModItems.PWR_FUEL_HOT_ITEMS.size(), ModItems.PWR_FUEL_DEPLETED_ITEMS.size());
        for (int i = 0; i < pwrCount; i++) {
            ItemStack input = new ItemStack(ModItems.PWR_FUEL_HOT_ITEMS.get(i).get());
            ItemStack output = new ItemStack(ModItems.PWR_FUEL_DEPLETED_ITEMS.get(i).get());
            fuelPoolRecipe(consumer, HbmRegistryUtil.itemKey(input.getItem()).getPath(), input, output, sourceOrder++);
        }
    }

    private static void fuelPoolRecipe(Consumer<FinishedRecipe> consumer, String name, ItemStack input,
            ItemStack output, int sourceOrder) {
        JsonObject json = new JsonObject();
        json.add("input", HbmIngredient.exact(input).toJson());
        json.add("output", HbmItemOutput.of(output).toJson());
        json.addProperty("source_order", sourceOrder);
        consumer.accept(finishedRecipe(id("fuel_pool/" + name), json, ModRecipes.FUEL_POOL.serializer().get()));
    }

    private static GenericMachineRecipeBuilder fusionRecipe(Consumer<FinishedRecipe> consumer, String name,
            long ignitionTemp, long outputTemp, double outputFlux, float r, float g, float b) {
        return GenericMachineRecipeBuilder.fusionReactor("fus." + name, 100, 25_000)
                .fusionExtra(ignitionTemp, outputTemp, outputFlux, r, g, b);
    }

    private static void fusionFluidBreederRecipes(Consumer<FinishedRecipe> consumer) {
        fusionFluidBreeder(consumer, "gas_to_syngas", HbmFluids.GAS, 1_000, HbmFluids.SYNGAS, 1_000, 0);
        fusionFluidBreeder(consumer, "lightoil_to_reformgas", HbmFluids.LIGHTOIL, 1_000, HbmFluids.REFORMGAS, 1_000,
                1);
        fusionFluidBreeder(consumer, "lightoil_crack_to_reformgas", HbmFluids.LIGHTOIL_CRACK, 1_000,
                HbmFluids.REFORMGAS, 1_000, 2);
    }

    private static void fusionFluidBreeder(Consumer<FinishedRecipe> consumer, String name, FluidType input,
            int inputAmount, FluidType output, int outputAmount, int sourceOrder) {
        consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                json.add("input", fluidStackJson(input, inputAmount));
                json.add("output", fluidStackJson(output, outputAmount));
                json.addProperty("source_order", sourceOrder);
            }

            @Override
            public ResourceLocation getId() {
                return id("fusion_fluid_breeder/" + name);
            }

            @Override
            public RecipeSerializer<?> getType() {
                return ModRecipes.FUSION_FLUID_BREEDER.serializer().get();
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }

    private static void reactorPlasmaForgeRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.plasmaForge("plsm.plateeuphemium", 600, 10_000_000)
                .plasmaForgeExtra(1_000_000)
                .inputLegacyOre("ingotEuphemium", 4)
                .inputLegacyOre("dustAstatine", 3)
                .inputLegacyOre("dustBismuth", 1)
                .inputLegacyOre("gemVolcanic", 1)
                .inputLegacyOre("ingotOsmiridium", 1)
                .outputItem(new ItemStack(item("plate_euphemium"), 4))
                .sourceOrder(104)
                .save(consumer, id("plasma_forge/plate_euphemium"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.platednt", 600, 10_000_000)
                .plasmaForgeExtra(1_000_000)
                .inputLegacyOre("ingotDineutronium", 4)
                .inputItem(item("powder_spark_mix"), 2)
                .inputLegacyOre("ingotWorkersAlloy", 1)
                .outputItem(new ItemStack(item("plate_dineutronium"), 4))
                .sourceOrder(105)
                .save(consumer, id("plasma_forge/plate_dineutronium"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.hde", 600, 25_000_000)
                .plasmaForgeExtra(10_000_000)
                .inputLegacyOre("plateCastAnyBismoidBronze", 2)
                .inputLegacyOre("plateWeldedCMBSteel", 1)
                .inputItem(item("ingot_cft"), 1)
                .inputFluid(HbmFluids.STELLAR_FLUX, 4_000)
                .outputLegacyMeta(LegacyMetaItemMappings.PART_GENERIC, 4)
                .sourceOrder(106)
                .save(consumer, id("plasma_forge/heavy_duty_element"));

        plasmaForgeWeldedPlate(consumer, "welded_plate_iron", "plsm.weldiron",
                "plateCastIron", "plate_welded_iron", 50, 100L, 107, null);
        plasmaForgeWeldedPlate(consumer, "welded_plate_steel", "plsm.weldsteel",
                "plateCastSteel", "plate_welded_steel", 50, 500L, 108, null);
        plasmaForgeWeldedPlate(consumer, "welded_plate_copper", "plsm.weldcopper",
                "plateCastCopper", "plate_welded_copper", 50, 1_000L, 109, null);
        plasmaForgeWeldedPlate(consumer, "welded_plate_titanium", "plsm.weldtitanium",
                "plateCastTitanium", "plate_welded_titanium", 300, 50_000L, 110, null);
        plasmaForgeWeldedPlate(consumer, "welded_plate_zirconium", "plsm.weldzirconium",
                "plateCastZirconium", "plate_welded_zirconium", 300, 10_000L, 111, null);
        plasmaForgeWeldedPlate(consumer, "welded_plate_aluminium", "plsm.weldaluminium",
                "plateCastAluminum", "plate_welded_aluminium", 150, 10_000L, 112, null);
        plasmaForgeWeldedPlate(consumer, "welded_plate_tcalloy", "plsm.weldtcalloy",
                "plateCastTcAlloy", "plate_welded_tcalloy", 600, 1_000_000L, 113,
                builder -> builder.inputFluid(HbmFluids.OXYGEN, 1_000));
        plasmaForgeWeldedPlate(consumer, "welded_plate_cdalloy", "plsm.weldcdalloy",
                "plateCastCdAlloy", "plate_welded_cdalloy", 600, 1_000_000L, 114,
                builder -> builder.inputFluid(HbmFluids.OXYGEN, 1_000));
        plasmaForgeWeldedPlate(consumer, "welded_plate_tungsten", "plsm.weldtungsten",
                "plateCastTungsten", "plate_welded_tungsten", 600, 250_000L, 115,
                builder -> builder.inputFluid(HbmFluids.OXYGEN, 1_000));
        plasmaForgeWeldedPlate(consumer, "welded_plate_combine_steel", "plsm.weldcmb",
                "plateCastCMBSteel", "plate_welded_combine_steel", 600, 10_000_000L, 116,
                builder -> builder.inputFluid(HbmFluids.REFORMGAS, 1_000));
        plasmaForgeWeldedPlate(consumer, "welded_plate_osmiridium", "plsm.weldosmiridium",
                "plateCastOsmiridium", "plate_welded_osmiridium", 3_000, 50_000_000L, 117,
                builder -> builder.inputFluid(HbmFluids.REFORMGAS, 16_000));

        GenericMachineRecipeBuilder.plasmaForge("plsm.fusionvessel", 1_200, 2_000_000)
                .plasmaForgeExtra(3_000_000)
                .inputLegacyMeta(LegacyMetaItemMappings.FUSION_COMPONENT, 0, 64)
                .inputLegacyMeta(LegacyMetaItemMappings.FUSION_COMPONENT, 0, 64)
                .inputLegacyMeta(LegacyMetaItemMappings.FUSION_COMPONENT, 0, 64)
                .inputLegacyMeta(LegacyMetaItemMappings.FUSION_COMPONENT, 0, 64)
                .inputLegacyMeta(LegacyMetaItemMappings.FUSION_COMPONENT, 0, 64)
                .inputLegacyMeta(LegacyMetaItemMappings.FUSION_COMPONENT, 0, 64)
                .inputLegacyMeta(LegacyMetaItemMappings.FUSION_COMPONENT, 3, 64)
                .inputLegacyMeta(LegacyMetaItemMappings.FUSION_COMPONENT, 3, 64)
                .inputLegacyMeta(LegacyMetaItemMappings.FUSION_COMPONENT, 3, 64)
                .inputLegacyMeta(LegacyMetaItemMappings.FUSION_COMPONENT, 2, 64)
                .inputLegacyMeta(LegacyMetaItemMappings.FUSION_COMPONENT, 2, 64)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 4)
                .outputItem(ModBlocks.FUSION_TORUS.get())
                .sourceOrder(118)
                .save(consumer, id("plasma_forge/fusion_vessel"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.icfcell", 800, 10_000_000)
                .plasmaForgeExtra(1_000_000)
                .inputItem(item("ingot_cft"), 2)
                .inputLegacyOre("plateCastAnyBismoidBronze", 4)
                .inputItem(block("glass_quartz"), 16)
                .outputItem(ModBlocks.ICF_LASER_CELL.get())
                .sourceOrder(119)
                .save(consumer, id("plasma_forge/icf_laser_cell"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.icfemitter", 800, 10_000_000)
                .plasmaForgeExtra(1_000_000)
                .inputLegacyOre("plateWeldedTungsten", 4)
                .inputLegacyOre("wireDenseMagnetizedTungsten", 16)
                .inputFluid(HbmFluids.XENON, 16_000)
                .outputItem(ModBlocks.ICF_LASER_EMITTER.get())
                .sourceOrder(120)
                .save(consumer, id("plasma_forge/icf_laser_emitter"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.icfcapacitor", 800, 10_000_000)
                .plasmaForgeExtra(1_000_000)
                .inputLegacyOre("plateWeldedAnyResistantAlloy", 1)
                .inputLegacyOre("wireDenseNeodymium", 16)
                .inputLegacyOre("wireDenseSchrabidium", 2)
                .outputItem(ModBlocks.ICF_LASER_CAPACITOR.get())
                .sourceOrder(121)
                .save(consumer, id("plasma_forge/icf_laser_capacitor"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.icfturbo", 800, 10_000_000)
                .plasmaForgeExtra(1_000_000)
                .inputLegacyOre("plateWeldedAnyResistantAlloy", 2)
                .inputLegacyOre("wireDenseDineutronium", 4)
                .inputLegacyOre("wireDenseSchrabidium", 4)
                .outputItem(ModBlocks.ICF_LASER_TURBO.get())
                .sourceOrder(122)
                .save(consumer, id("plasma_forge/icf_laser_turbo"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.icfcasing", 800, 10_000_000)
                .plasmaForgeExtra(1_000_000)
                .inputLegacyOre("plateCastAnyBismoidBronze", 4)
                .inputLegacyOre("plateCastSaturnite", 4)
                .inputLegacyOre("ingotAnyHardplastic", 16)
                .outputItem(ModBlocks.ICF_LASER_CASING.get())
                .sourceOrder(123)
                .save(consumer, id("plasma_forge/icf_laser_casing"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.icfport", 800, 10_000_000)
                .plasmaForgeExtra(1_000_000)
                .inputLegacyOre("plateCastAnyBismoidBronze", 4)
                .inputLegacyOre("ingotAnyHardplastic", 16)
                .inputLegacyOre("wireDenseNeodymium", 16)
                .outputItem(ModBlocks.ICF_LASER_PORT.get())
                .sourceOrder(124)
                .save(consumer, id("plasma_forge/icf_laser_port"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.icfcontroller", 800, 10_000_000)
                .plasmaForgeExtra(1_000_000)
                .inputItem(item("ingot_cft"), 16)
                .inputLegacyOre("plateCastAnyBismoidBronze", 4)
                .inputLegacyOre("ingotAnyHardplastic", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 12, 16)
                .outputItem(ModBlocks.ICF_CONTROLLER.get())
                .sourceOrder(125)
                .save(consumer, id("plasma_forge/icf_controller"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.icfscaffold", 800, 10_000_000)
                .plasmaForgeExtra(1_000_000)
                .inputLegacyOre("plateWeldedSteel", 4)
                .inputLegacyOre("plateWeldedTitanium", 2)
                .outputItem(ModBlocks.ICF_COMPONENT_SCAFFOLD.get())
                .sourceOrder(126)
                .save(consumer, id("plasma_forge/icf_component_scaffold"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.icfvessel", 800, 10_000_000)
                .plasmaForgeExtra(1_000_000)
                .inputItem(item("ingot_cft"), 1)
                .inputLegacyOre("plateCastCMBSteel", 1)
                .inputLegacyOre("plateWeldedTungsten", 2)
                .outputItem(ModBlocks.ICF_COMPONENT_VESSEL.get())
                .sourceOrder(127)
                .save(consumer, id("plasma_forge/icf_component_vessel"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.icfstructural", 800, 10_000_000)
                .plasmaForgeExtra(1_000_000)
                .inputLegacyOre("plateWeldedSteel", 2)
                .inputLegacyOre("plateWeldedCopper", 2)
                .inputLegacyOre("plateCastAnyBismoidBronze", 1)
                .outputItem(ModBlocks.ICF_COMPONENT_STRUCTURE.get())
                .sourceOrder(128)
                .save(consumer, id("plasma_forge/icf_component_structural"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.icfcore", 3_000, 10_000_000)
                .plasmaForgeExtra(1_000_000)
                .inputLegacyOre("plateWeldedCombineSteel", 16)
                .inputLegacyOre("plateWeldedAnyResistantAlloy", 16)
                .inputLegacyOre("plateCastAnyBismoidBronze", 16)
                .inputLegacyOre("wireDenseSchrabidium", 32)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 12, 32)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 16)
                .outputItem(ModBlocks.STRUCT_ICF_CORE.get())
                .sourceOrder(130)
                .save(consumer, id("plasma_forge/icf_core"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.icfpress", 800, 10_000_000)
                .plasmaForgeExtra(1_000_000)
                .inputLegacyOre("plateCastGold", 8)
                .inputItem(item("motor"), 4)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 12, 1)
                .outputItem(ModBlocks.MACHINE_ICF_PRESS.get())
                .sourceOrder(134)
                .save(consumer, id("plasma_forge/icf_press"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.schrabhammer", 6_000, 10_000_000)
                .plasmaForgeExtra(25_000_000)
                .inputLegacyOre("blockSchrabidium", 35)
                .inputItem(item("billet_yharonite"), 64)
                .inputItem(item("billet_yharonite"), 64)
                .inputItem(item("fragment_meteorite"), 64)
                .inputItem(item("fragment_meteorite"), 64)
                .inputItem(item("fragment_meteorite"), 64)
                .inputItem(item("fragment_meteorite"), 64)
                .inputItem(item("fragment_meteorite"), 64)
                .inputItem(item("fragment_meteorite"), 64)
                .inputItem(item("fragment_meteorite"), 64)
                .inputItem(item("fragment_meteorite"), 64)
                .outputItem(item("schrabidium_hammer"))
                .sourceOrder(135)
                .save(consumer, id("plasma_forge/schrabidium_hammer"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.gerald", 12_000, 50_000_000)
                .plasmaForgeExtra(25_000_000)
                .inputLegacyOre("plateCastSchrabidium", 64)
                .inputLegacyOre("plateCastSchrabidium", 64)
                .inputLegacyOre("wireDenseBSCCO", 64)
                .inputLegacyOre("wireDenseBSCCO", 64)
                .inputItem(block("det_nuke"), 64)
                .inputLegacyMeta(LegacyMetaItemMappings.PART_GENERIC, 4, 64)
                .inputLegacyMeta(LegacyMetaItemMappings.PART_GENERIC, 4, 64)
                .inputLegacyMeta(LegacyMetaItemMappings.PART_GENERIC, 4, 64)
                .inputLegacyMeta(LegacyMetaItemMappings.PART_GENERIC, 4, 64)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 17, 64)
                .outputItem(item("sat_gerald"))
                .pool("discover.gerald")
                .sourceOrder(137)
                .save(consumer, id("plasma_forge/gerald"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.dfccore", 12_000, 100_000_000)
                .plasmaForgeExtra(50_000_000)
                .inputFluid(HbmFluids.STELLAR_FLUX, 12_000)
                .inputLegacyOre("plateWeldedOsmiridium", 16)
                .inputLegacyOre("wireDenseDineutronium", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 12)
                .inputItem(item("singularity_spark"), 1)
                .inputItem(item("powder_chlorophyte"), 64)
                .outputItem(ModBlocks.DFC_CORE.get())
                .sourceOrder(138)
                .save(consumer, id("plasma_forge/dfc_core"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.dfcemitter", 1_200, 10_000_000)
                .plasmaForgeExtra(50_000_000)
                .inputFluid(HbmFluids.STELLAR_FLUX, 4_000)
                .inputLegacyOre("plateWeldedOsmiridium", 16)
                .inputLegacyOre("wireDenseStarmetal", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 8)
                .outputItem(ModBlocks.DFC_EMITTER.get())
                .sourceOrder(139)
                .save(consumer, id("plasma_forge/dfc_emitter"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.dfcreceiver", 1_200, 10_000_000)
                .plasmaForgeExtra(50_000_000)
                .inputFluid(HbmFluids.STELLAR_FLUX, 4_000)
                .inputLegacyOre("plateWeldedOsmiridium", 16)
                .inputLegacyOre("plateCastStarmetal", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 8)
                .outputItem(ModBlocks.DFC_RECEIVER.get())
                .sourceOrder(140)
                .save(consumer, id("plasma_forge/dfc_receiver"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.dfcinjector", 1_200, 10_000_000)
                .plasmaForgeExtra(50_000_000)
                .inputFluid(HbmFluids.STELLAR_FLUX, 4_000)
                .inputLegacyOre("plateWeldedOsmiridium", 16)
                .inputLegacyOre("plateCastSaturnite", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 4)
                .outputItem(ModBlocks.DFC_INJECTOR.get())
                .sourceOrder(141)
                .save(consumer, id("plasma_forge/dfc_injector"));

        GenericMachineRecipeBuilder.plasmaForge("plsm.dfcstabilizer", 1_200, 10_000_000)
                .plasmaForgeExtra(50_000_000)
                .inputFluid(HbmFluids.STELLAR_FLUX, 4_000)
                .inputLegacyOre("plateWeldedOsmiridium", 16)
                .inputLegacyOre("wireDenseSchrabidium", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 8)
                .outputItem(ModBlocks.DFC_STABILIZER.get())
                .sourceOrder(142)
                .save(consumer, id("plasma_forge/dfc_stabilizer"));
    }

    private static void plasmaForgeWeldedPlate(Consumer<FinishedRecipe> consumer, String idPath, String internalName,
            String inputLegacyOre, String outputItem, int duration, long power, int sourceOrder,
            @Nullable Consumer<GenericMachineRecipeBuilder> extraInputs) {
        GenericMachineRecipeBuilder builder = GenericMachineRecipeBuilder.plasmaForge(internalName, duration, power)
                .plasmaForgeExtra(500_000)
                .inputLegacyOre(inputLegacyOre, 2)
                .outputItem(item(outputItem))
                .autoSwitchGroup("autoswitch.weldPlates")
                .sourceOrder(sourceOrder);
        if (extraInputs != null) {
            extraInputs.accept(builder);
        }
        builder.save(consumer, id("plasma_forge/" + idPath));
    }

    private static void purexRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.purex("purex.uzh", 600, 1_000)
                .inputItem(item("billet_uranium_fuel"), 1)
                .inputItem(item("billet_zirconium"), 3)
                .inputFluid(HbmFluids.NITRIC_ACID, 1_000)
                .inputFluid(HbmFluids.HYDROGEN, 4_000)
                .outputItem(new ItemStack(item("billet_uzh"), 4))
                .sourceOrder(0)
                .save(consumer, id("purex/uzh"));

        pilePurex(consumer, "pilepu", "pile_rod_plutonium", 1,
                out("billet_pu_mix", 2), out("billet_uranium", 1), out("plate_iron", 2));
        pilePurex(consumer, "pilepu239", "pile_rod_pu239", 2,
                out("billet_pu239", 1), out("billet_pu_mix", 1), out("billet_uranium", 1),
                out("plate_iron", 2));

        zirnoxPurex(consumer, "zirnoxnu", "waste_natural_uranium", 3,
                out("nugget_u238", 1), out("nugget_pu_mix", 2), out("nugget_pu239", 1),
                out("nuclear_waste_tiny", 2));
        zirnoxPurex(consumer, "zirnoxmeu", "waste_uranium", 4,
                out("nugget_pu_mix", 1), out("nugget_plutonium", 2), out("nugget_technetium", 1),
                out("nuclear_waste_tiny", 2));
        zirnoxPurex(consumer, "zirnoxthmeu", "waste_thorium", 5,
                out("nugget_u238", 1), out("nugget_th232", 1), out("nugget_u233", 2),
                out("nuclear_waste_tiny", 2));
        zirnoxPurex(consumer, "zirnoxmox", "waste_mox", 6,
                out("nugget_pu_mix", 1), out("nugget_technetium", 1), out("nugget_u238", 1),
                out("nuclear_waste_tiny", 3));
        zirnoxPurex(consumer, "zirnoxmep", "waste_plutonium", 7,
                out("nugget_pu_mix", 2), out("nugget_technetium", 1), out("nuclear_waste_tiny", 3));
        zirnoxPurex(consumer, "zirnoxheu233", "waste_u233", 8,
                out("nugget_u235", 1), out("nugget_neptunium", 1), out("nugget_technetium", 1),
                out("nuclear_waste_tiny", 3));
        zirnoxPurex(consumer, "zirnoxheu235", "waste_u235", 9,
                out("nugget_pu238", 1), out("nugget_neptunium", 1), out("nugget_technetium", 1),
                out("nuclear_waste_tiny", 3));
        zirnoxPurex(consumer, "zirnoxles", "waste_schrabidium", 10,
                out("nugget_beryllium", 2), out("nugget_pu239", 1), out("nuclear_waste_tiny", 1),
                out("nuclear_waste_tiny", 2));
        zirnoxPurex(consumer, "zirnoxzfbmox", "waste_zfb_mox", 11,
                out("nugget_zirconium", 3), out("nugget_technetium", 1), out("nugget_pu_mix", 1),
                out("nuclear_waste_tiny", 1));

        platePurex(consumer, "platemox", "waste_plate_mox", 12,
                out("powder_sr90_tiny", 1), out("nugget_pu_mix", 3), out("powder_cs137_tiny", 1),
                out("nuclear_waste_tiny", 4));
        platePurex(consumer, "platepu238be", "waste_plate_pu238be", 13,
                out("nugget_beryllium", 1), out("nugget_pu238", 1), out("powder_coal_tiny", 2),
                out("nugget_lead", 2));
        platePurex(consumer, "platepu239", "waste_plate_pu239", 14,
                out("nugget_pu240", 2), out("nugget_technetium", 1), out("powder_cs137_tiny", 1),
                out("nuclear_waste_tiny", 5));
        platePurex(consumer, "platera226be", "waste_plate_ra226be", 15,
                out("nugget_beryllium", 2), out("nugget_polonium", 2), out("powder_coal_tiny", 1),
                out("nugget_lead", 1));
        platePurex(consumer, "platesa326", "waste_plate_sa326", 16,
                out("nugget_solinium", 1), out("powder_neodymium_tiny", 1), out("nugget_tantalium", 1),
                out("nuclear_waste_tiny", 6));
        platePurex(consumer, "plateu233", "waste_plate_u233", 17,
                out("nugget_u235", 1), out("powder_i131_tiny", 1), out("powder_sr90_tiny", 1),
                out("nuclear_waste_tiny", 6));
        platePurex(consumer, "plateu235", "waste_plate_u235", 18,
                out("nugget_neptunium", 1), out("nugget_pu238", 1), out("nugget_technetium", 1),
                out("nuclear_waste_tiny", 6));

        thoriumSaltPurex(consumer);

        watzPurex(consumer, "watzschrab", 0, 35,
                out("nugget_solinium", 15), out("nugget_euphemium", 3), out("nuclear_waste", 2));
        watzPurex(consumer, "watzhes", 1, 36,
                out("nugget_solinium", 17), out("nugget_euphemium", 1), out("nuclear_waste", 2));
        watzPurex(consumer, "watzmes", 2, 37,
                out("nugget_solinium", 12), out("nugget_tantalium", 6), out("nuclear_waste", 2));
        watzPurex(consumer, "watzles", 3, 38,
                out("nugget_solinium", 9), out("nugget_tantalium", 9), out("nuclear_waste", 2));
        watzPurex(consumer, "watzhen", 4, 39,
                out("nugget_pu239", 12), out("nugget_technetium", 6), out("nuclear_waste", 2));
        watzPurex(consumer, "watzmeu", 5, 40,
                out("nugget_pu239", 12), out("nugget_bismuth", 6), out("nuclear_waste", 2));
        watzPurex(consumer, "watzmep", 6, 41,
                out("nugget_pu241", 12), out("nugget_bismuth", 6), out("nuclear_waste", 2));
        watzPurex(consumer, "watzlead", 7, 42,
                out("nugget_lead", 6), out("nugget_bismuth", 12), out("nuclear_waste", 2));
        watzPurex(consumer, "watzboron", 8, 43,
                out("powder_coal_tiny", 12), out("nugget_co60", 6), out("nuclear_waste", 2));
        watzPurex(consumer, "watzdu", 9, 44,
                out("nugget_polonium", 12), out("nugget_pu238", 6), out("nuclear_waste", 2));
        watzConditionalNaquadriaPurex(consumer);

        GenericMachineRecipeBuilder.purex("purex.icf", 300, 10_000)
                .inputItem(ModItems.ICF_PELLET_DEPLETED.get(), 1)
                .outputItem(ModItems.ICF_PELLET_EMPTY.get())
                .outputItem(item("pellet_charged"))
                .outputItem(ModItems.IRON_POWDER.get())
                .outputFluid(HbmFluids.HELIUM4, 1_250)
                .nameWrapper("purex.recycle")
                .sourceOrder(47)
                .save(consumer, id("purex/icf_pellet_recycle"));

        vitrificationPurex(consumer, "vitliquid", HbmFluids.WASTEFLUID, 1, 48);
        vitrificationPurex(consumer, "vitgaseous", HbmFluids.WASTEGAS, 1, 49);
        GenericMachineRecipeBuilder.purex("purex.vitsolid", 300, 1_000)
                .inputItem(ModBlocks.SAND_LEAD.get(), 1)
                .inputItem(item("nuclear_waste"), 4)
                .outputItem(new ItemStack(item("nuclear_waste_vitrified"), 4))
                .sourceOrder(50)
                .save(consumer, id("purex/vitsolid"));

        GenericMachineRecipeBuilder.purex("purex.schraranium", 200, 1_000)
                .inputItem(item("ingot_schraranium"), 1)
                .inputFluid(HbmFluids.KEROSENE, 2_000)
                .inputFluid(HbmFluids.NITRIC_ACID, 1_000)
                .outputItem(new ItemStack(item("nugget_schrabidium"), 3))
                .outputItem(new ItemStack(item("nugget_uranium"), 3))
                .outputItem(new ItemStack(item("nugget_neptunium"), 2))
                .nameWrapper("purex.schrab")
                .sourceOrder(51)
                .save(consumer, id("purex/schraranium"));

        pwrPurex(consumer, "pwrmeu", 0, 19,
                out("nugget_u238", 3), out("nugget_plutonium", 4), out("nugget_technetium", 2),
                out("nuclear_waste_tiny", 3));
        pwrPurex(consumer, "pwrheu233", 1, 20,
                out("nugget_u235", 3), out("nugget_pu238", 3), out("nugget_technetium", 1),
                out("nuclear_waste_tiny", 5));
        pwrPurex(consumer, "pwrheu235", 2, 21,
                out("nugget_neptunium", 3), out("nugget_pu238", 3), out("nugget_technetium", 1),
                out("nuclear_waste_tiny", 5));
        pwrPurex(consumer, "pwrmen", 3, 22,
                out("nugget_u238", 3), out("nugget_pu239", 4), out("nugget_technetium", 2),
                out("nuclear_waste_tiny", 3));
        pwrPurex(consumer, "pwrhen237", 4, 23,
                out("nugget_pu238", 2), out("nugget_pu239", 4), out("nugget_technetium", 1),
                out("nuclear_waste_tiny", 5));
        pwrPurex(consumer, "pwrmox", 5, 24,
                out("nugget_u238", 3), out("nugget_pu240", 4), out("nugget_technetium", 2),
                out("nuclear_waste_tiny", 3));
        pwrPurex(consumer, "pwrmep", 6, 25,
                out("nugget_lead", 2), out("nugget_pu_mix", 4), out("nugget_technetium", 2),
                out("nuclear_waste_tiny", 3));
        pwrPurex(consumer, "pwrhep239", 7, 26,
                out("nugget_pu_mix", 2), out("nugget_pu240", 4), out("nugget_technetium", 1),
                out("nuclear_waste_tiny", 5));
        pwrPurex(consumer, "pwrhep241", 8, 27,
                out("nugget_lead", 3), out("nugget_zirconium", 2), out("nugget_technetium", 1),
                out("nuclear_waste_tiny", 6));
        pwrPurex(consumer, "pwrmea", 9, 28,
                out("nugget_lead", 3), out("nugget_zirconium", 2), out("nugget_technetium", 1),
                out("nuclear_waste_tiny", 6));
        pwrPurex(consumer, "pwrhea242", 10, 29,
                out("nugget_lead", 3), out("nugget_zirconium", 2), out("nugget_technetium", 1),
                out("nuclear_waste_tiny", 6));
        pwrPurex(consumer, "pwrhes326", 11, 30,
                out("nugget_solinium", 3), out("nugget_lead", 2), out("nugget_euphemium", 1),
                out("nuclear_waste_tiny", 6));
        pwrPurex(consumer, "pwrhes327", 12, 31,
                out("nugget_australium", 4), out("nugget_lead", 1), out("nugget_euphemium", 1),
                out("nuclear_waste_tiny", 6));
        pwrPurex(consumer, "pwrbfbam", 13, 32,
                out("nugget_am_mix", 9), out("nugget_pu_mix", 2), out("nugget_bismuth", 6),
                out("nuclear_waste_tiny", 1));
        pwrPurex(consumer, "pwrbfpu241", 14, 33,
                out("nugget_pu241", 9), out("nugget_pu_mix", 2), out("nugget_bismuth", 6),
                out("nuclear_waste_tiny", 1));

        purexSchrab(consumer, "schrabzirnox", item("waste_plutonium"), 52);
        pwrSchrabPurex(consumer, "schrabpwr", 6, 53);
        pwrSchrabPurex(consumer, "schrabmen", 3, 54);
    }

    private static void vitrificationPurex(Consumer<FinishedRecipe> consumer, String name, FluidType wasteFluid,
            int outputCount, int sourceOrder) {
        GenericMachineRecipeBuilder.purex("purex." + name, 100, 1_000)
                .inputItem(ModBlocks.SAND_LEAD.get(), 1)
                .inputFluid(wasteFluid, 1_000)
                .outputItem(new ItemStack(item("nuclear_waste_vitrified"), outputCount))
                .sourceOrder(sourceOrder)
                .save(consumer, id("purex/" + name));
    }

    private static void pilePurex(Consumer<FinishedRecipe> consumer, String name, String inputName, int sourceOrder,
            ItemStack... outputs) {
        GenericMachineRecipeBuilder builder = GenericMachineRecipeBuilder.purex("purex." + name, 40, 100)
                .inputItem(item(inputName), 1)
                .inputFluid(HbmFluids.SULFURIC_ACID, 100)
                .nameWrapper("purex.recycle")
                .autoSwitchGroup("autoswitch.pile")
                .sourceOrder(sourceOrder);
        for (ItemStack output : outputs) {
            builder.outputItem(output);
        }
        builder.save(consumer, id("purex/" + name));
    }

    private static void zirnoxPurex(Consumer<FinishedRecipe> consumer, String name, String inputName, int sourceOrder,
            ItemStack... outputs) {
        wasteFuelPurex(consumer, name, inputName, 100, 1_000, "autoswitch.zirnox", sourceOrder, outputs);
    }

    private static void platePurex(Consumer<FinishedRecipe> consumer, String name, String inputName, int sourceOrder,
            ItemStack... outputs) {
        wasteFuelPurex(consumer, name, inputName, 100, 1_500, "autoswitch.plate", sourceOrder, outputs);
    }

    private static void wasteFuelPurex(Consumer<FinishedRecipe> consumer, String name, String inputName, int duration,
            long power, String autoSwitchGroup, int sourceOrder, ItemStack... outputs) {
        GenericMachineRecipeBuilder builder = GenericMachineRecipeBuilder.purex("purex." + name, duration, power)
                .inputItem(item(inputName), 1)
                .inputFluid(HbmFluids.KEROSENE, 500)
                .inputFluid(HbmFluids.NITRIC_ACID, 250)
                .nameWrapper("purex.recycle")
                .autoSwitchGroup(autoSwitchGroup)
                .sourceOrder(sourceOrder);
        for (ItemStack output : outputs) {
            builder.outputItem(output);
        }
        builder.save(consumer, id("purex/" + name));
    }

    private static void thoriumSaltPurex(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.purex("purex.thoriumsalt", 20, 10_000)
                .inputFluid(HbmFluids.THORIUM_SALT_DEPLETED, 16_000)
                .inputItem(item("nugget_th232"), 2)
                .outputFluid(HbmFluids.THORIUM_SALT, 16_000)
                .outputChance(item("nugget_u233"), 0.5F)
                .outputChance(item("nuclear_waste_tiny"), 0.25F)
                .sourceOrder(34)
                .save(consumer, id("purex/thoriumsalt"));
    }

    private static void watzPurex(Consumer<FinishedRecipe> consumer, String name, int depletedIndex, int sourceOrder,
            ItemStack... outputs) {
        GenericMachineRecipeBuilder builder = GenericMachineRecipeBuilder.purex("purex." + name, 60, 10_000)
                .inputItem(ModItems.WATZ_PELLET_DEPLETED_ITEMS.get(depletedIndex).get(), 1)
                .inputFluid(HbmFluids.KEROSENE, 500)
                .inputFluid(HbmFluids.NITRIC_ACID, 250)
                .nameWrapper("purex.recycle")
                .autoSwitchGroup("autoswitch.watz")
                .sourceOrder(sourceOrder);
        for (ItemStack output : outputs) {
            builder.outputItem(output);
        }
        builder.outputFluid(HbmFluids.WATZ, 1_000)
                .save(consumer, id("purex/" + name));
    }

    private static void watzConditionalNaquadriaPurex(Consumer<FinishedRecipe> consumer) {
        TagKey<Item> naquadriaNuggets = forgeTag("nuggets/naquadria");
        GenericMachineRecipeBuilder.purex("purex.watznaqadah", 60, 10_000)
                .conditionNotTagEmpty(naquadriaNuggets)
                .inputItem(ModItems.WATZ_PELLET_DEPLETED_ITEMS.get(10).get(), 1)
                .inputFluid(HbmFluids.KEROSENE, 500)
                .inputFluid(HbmFluids.NITRIC_ACID, 250)
                .outputTag(naquadriaNuggets, 12)
                .outputItem(new ItemStack(item("nugget_euphemium"), 6))
                .outputItem(new ItemStack(item("nuclear_waste"), 2))
                .outputFluid(HbmFluids.WATZ, 1_000)
                .nameWrapper("purex.recycle")
                .autoSwitchGroup("autoswitch.watz")
                .sourceOrder(45)
                .save(consumer, id("purex/watznaqadah"));

        GenericMachineRecipeBuilder.purex("purex.watznaqadria", 60, 10_000)
                .conditionNotTagEmpty(naquadriaNuggets)
                .inputItem(ModItems.WATZ_PELLET_DEPLETED_ITEMS.get(11).get(), 1)
                .inputFluid(HbmFluids.KEROSENE, 500)
                .inputFluid(HbmFluids.NITRIC_ACID, 250)
                .outputItem(new ItemStack(item("nugget_co60"), 12))
                .outputItem(new ItemStack(item("nugget_euphemium"), 6))
                .outputItem(new ItemStack(item("nuclear_waste"), 2))
                .outputFluid(HbmFluids.WATZ, 1_000)
                .nameWrapper("purex.recycle")
                .autoSwitchGroup("autoswitch.watz")
                .sourceOrder(46)
                .save(consumer, id("purex/watznaqadria"));
    }

    private static void pwrPurex(Consumer<FinishedRecipe> consumer, String name, int depletedIndex, int sourceOrder,
            ItemStack... outputs) {
        GenericMachineRecipeBuilder builder = GenericMachineRecipeBuilder.purex("purex." + name, 100, 2_500)
                .inputItem(ModItems.PWR_FUEL_DEPLETED_ITEMS.get(depletedIndex).get(), 1)
                .inputFluid(HbmFluids.KEROSENE, 500)
                .inputFluid(HbmFluids.NITRIC_ACID, 250)
                .nameWrapper("purex.recycle")
                .autoSwitchGroup("autoswitch.pwr")
                .sourceOrder(sourceOrder);
        for (ItemStack output : outputs) {
            builder.outputItem(output);
        }
        builder.save(consumer, id("purex/" + name));
    }

    private static void pwrSchrabPurex(Consumer<FinishedRecipe> consumer, String name, int depletedIndex, int sourceOrder) {
        GenericMachineRecipeBuilder.purex("purex." + name, 200, 50_000)
                .inputItem(ModItems.PWR_FUEL_DEPLETED_ITEMS.get(depletedIndex).get(), 1)
                .inputFluid(HbmFluids.SOLVENT, 4_000)
                .inputFluid(HbmFluids.SCHRABIDIC, 250)
                .outputItem(item("powder_schrabidium"))
                .outputItem(new ItemStack(item("nugget_technetium"), 3))
                .outputItem(new ItemStack(item("nuclear_waste_tiny"), 4))
                .nameWrapper("purex.schrab")
                .autoSwitchGroup("autoswitch.schrab")
                .sourceOrder(sourceOrder)
                .save(consumer, id("purex/" + name));
    }

    private static void purexSchrab(Consumer<FinishedRecipe> consumer, String name, ItemLike input, int sourceOrder) {
        GenericMachineRecipeBuilder.purex("purex." + name, 200, 50_000)
                .inputItem(input, 1)
                .inputFluid(HbmFluids.SOLVENT, 4_000)
                .inputFluid(HbmFluids.SCHRABIDIC, 250)
                .outputItem(item("powder_schrabidium"))
                .outputItem(new ItemStack(item("nugget_technetium"), 3))
                .outputItem(new ItemStack(item("nuclear_waste_tiny"), 4))
                .nameWrapper("purex.schrab")
                .autoSwitchGroup("autoswitch.schrab")
                .sourceOrder(sourceOrder)
                .save(consumer, id("purex/" + name));
    }

    private static ItemStack out(String legacyName, int count) {
        return new ItemStack(item(legacyName), count);
    }

    private static void electrolyzerFluidRecipes(Consumer<FinishedRecipe> consumer) {
        electrolyzerFluid(consumer, "water", HbmFluids.WATER, 2_000,
                HbmFluids.HYDROGEN, 200, HbmFluids.OXYGEN, 200, 10);
        electrolyzerFluid(consumer, "heavywater", HbmFluids.HEAVYWATER, 2_000,
                HbmFluids.DEUTERIUM, 200, HbmFluids.OXYGEN, 200, 10);
        electrolyzerFluid(consumer, "vitriol", HbmFluids.VITRIOL, 1_000,
                HbmFluids.SULFURIC_ACID, 500, HbmFluids.CHLORINE, 500, 20,
                out("powder_iron", 1), out("ingot_mercury", 1));
        electrolyzerFluid(consumer, "slop", HbmFluids.SLOP, 1_000,
                HbmFluids.MERCURY, 250, HbmFluids.NONE, 0, 20,
                out("niter", 2), out("powder_limestone", 2), out("sulfur", 1));
        electrolyzerFluid(consumer, "redmud", HbmFluids.REDMUD, 450,
                HbmFluids.MERCURY, 150, HbmFluids.LYE, 50, 20,
                out("powder_titanium", 3), out("powder_iron", 3), out("powder_aluminium", 2));
        electrolyzerFluid(consumer, "alumina", HbmFluids.ALUMINA, 200,
                HbmFluids.CARBONDIOXIDE, 100, HbmFluids.NONE, 0, 40,
                out("powder_aluminium", 7), out("fluorite", 2));
        electrolyzerFluid(consumer, "potassium_chloride", HbmFluids.POTASSIUM_CHLORIDE, 250,
                HbmFluids.CHLORINE, 125, HbmFluids.NONE, 0, 20,
                out("dust", 1));
        electrolyzerFluid(consumer, "calcium_chloride", HbmFluids.CALCIUM_CHLORIDE, 250,
                HbmFluids.CHLORINE, 125, HbmFluids.CALCIUM_SOLUTION, 125, 20);
    }

    private static void electrolyzerFluid(Consumer<FinishedRecipe> consumer, String name, FluidType input,
            int inputAmount, FluidType output1, int output1Amount, FluidType output2, int output2Amount,
            int duration, ItemStack... byproducts) {
        JsonObject json = CompatRecipeRegistry.createElectrolyzerFluid(
                new HbmFluidStack(input, inputAmount), new HbmFluidStack(output1, output1Amount),
                new HbmFluidStack(output2, output2Amount), byproducts, duration);
        consumer.accept(finishedCompatRecipe(id("electrolyzer_fluid/" + name), json));
    }

    private static void electrolyzerMetalRecipes(Consumer<FinishedRecipe> consumer) {
        electrolyzerMetal(consumer, "crystal_iron", "crystal_iron",
                mat(Mats.MAT_IRON, MaterialShapes.INGOT.q(6)),
                mat(Mats.MAT_TITANIUM, MaterialShapes.INGOT.q(2)),
                out("powder_lithium_tiny", 3));
        electrolyzerMetal(consumer, "crystal_gold", "crystal_gold",
                mat(Mats.MAT_GOLD, MaterialShapes.INGOT.q(6)),
                mat(Mats.MAT_LEAD, MaterialShapes.INGOT.q(2)),
                out("powder_lithium_tiny", 3), out("ingot_mercury", 2));
        electrolyzerMetal(consumer, "crystal_uranium", "crystal_uranium",
                mat(Mats.MAT_URANIUM, MaterialShapes.INGOT.q(6)),
                mat(Mats.MAT_RADIUM, MaterialShapes.NUGGET.q(4)),
                out("powder_lithium_tiny", 3));
        electrolyzerMetal(consumer, "crystal_thorium", "crystal_thorium",
                mat(Mats.MAT_THORIUM, MaterialShapes.INGOT.q(6)),
                mat(Mats.MAT_URANIUM, MaterialShapes.INGOT.q(2)),
                out("powder_lithium_tiny", 3));
        electrolyzerMetal(consumer, "crystal_plutonium", "crystal_plutonium",
                mat(Mats.MAT_PLUTONIUM, MaterialShapes.INGOT.q(6)),
                mat(Mats.MAT_POLONIUM, MaterialShapes.INGOT.q(2)),
                out("powder_lithium_tiny", 3));
        electrolyzerMetal(consumer, "crystal_titanium", "crystal_titanium",
                mat(Mats.MAT_TITANIUM, MaterialShapes.INGOT.q(6)),
                mat(Mats.MAT_IRON, MaterialShapes.INGOT.q(2)),
                out("powder_lithium_tiny", 3));
        electrolyzerMetal(consumer, "crystal_copper", "crystal_copper",
                mat(Mats.MAT_COPPER, MaterialShapes.INGOT.q(6)),
                mat(Mats.MAT_LEAD, MaterialShapes.NUGGET.q(4)),
                out("powder_lithium_tiny", 3), out("sulfur", 2));
        electrolyzerMetal(consumer, "crystal_tungsten", "crystal_tungsten",
                mat(Mats.MAT_TUNGSTEN, MaterialShapes.INGOT.q(6)),
                mat(Mats.MAT_IRON, MaterialShapes.INGOT.q(2)),
                out("powder_lithium_tiny", 3));
        electrolyzerMetal(consumer, "crystal_aluminium", "crystal_aluminium",
                mat(Mats.MAT_ALUMINIUM, MaterialShapes.INGOT.q(2)),
                mat(Mats.MAT_IRON, MaterialShapes.INGOT.q(2)),
                out("chunk_ore_cryolite", 4), out("powder_lithium_tiny", 3));
        electrolyzerMetal(consumer, "crystal_beryllium", "crystal_beryllium",
                mat(Mats.MAT_BERYLLIUM, MaterialShapes.INGOT.q(6)),
                mat(Mats.MAT_LEAD, MaterialShapes.NUGGET.q(4)),
                out("powder_lithium_tiny", 3), out("powder_quartz", 2));
        electrolyzerMetal(consumer, "crystal_lead", "crystal_lead",
                mat(Mats.MAT_LEAD, MaterialShapes.INGOT.q(6)),
                mat(Mats.MAT_GOLD, MaterialShapes.INGOT.q(2)),
                out("powder_lithium_tiny", 3));
        electrolyzerMetal(consumer, "crystal_schraranium", "crystal_schraranium",
                mat(Mats.MAT_SCHRABIDIUM, MaterialShapes.NUGGET.q(5)),
                mat(Mats.MAT_URANIUM, MaterialShapes.NUGGET.q(2)),
                out("nugget_neptunium", 2));
        electrolyzerMetal(consumer, "crystal_schrabidium", "crystal_schrabidium",
                mat(Mats.MAT_SCHRABIDIUM, MaterialShapes.INGOT.q(6)),
                mat(Mats.MAT_PLUTONIUM, MaterialShapes.INGOT.q(2)),
                out("powder_lithium_tiny", 3));
        electrolyzerMetal(consumer, "crystal_rare", "crystal_rare",
                mat(Mats.MAT_ZIRCONIUM, MaterialShapes.NUGGET.q(6)),
                mat(Mats.MAT_BORON, MaterialShapes.NUGGET.q(2)),
                out("powder_desh_mix", 3));
        electrolyzerMetal(consumer, "crystal_trixite", "crystal_trixite",
                mat(Mats.MAT_PLUTONIUM, MaterialShapes.INGOT.q(3)),
                mat(Mats.MAT_COBALT, MaterialShapes.INGOT.q(4)),
                out("powder_niobium", 4), out("powder_nitan_mix", 2));
        electrolyzerMetal(consumer, "crystal_lithium", "crystal_lithium",
                mat(Mats.MAT_LITHIUM, MaterialShapes.INGOT.q(6)),
                mat(Mats.MAT_BORON, MaterialShapes.INGOT.q(2)),
                out("powder_quartz", 2), out("fluorite", 2));
        electrolyzerMetal(consumer, "crystal_starmetal", "crystal_starmetal",
                mat(Mats.MAT_DURA, MaterialShapes.INGOT.q(4)),
                mat(Mats.MAT_COBALT, MaterialShapes.INGOT.q(4)),
                out("powder_astatine", 3), out("ingot_mercury", 8));
        electrolyzerMetal(consumer, "crystal_cobalt", "crystal_cobalt",
                mat(Mats.MAT_COBALT, MaterialShapes.INGOT.q(3)),
                mat(Mats.MAT_IRON, MaterialShapes.INGOT.q(4)),
                out("powder_copper", 4), out("powder_lithium_tiny", 3));
        bedrockOreElectrolyzerRecipes(consumer);
    }

    private static void electrolyzerMetal(Consumer<FinishedRecipe> consumer, String name, String input,
            MaterialStack output1, MaterialStack output2, ItemStack... byproducts) {
        JsonObject json = CompatRecipeRegistry.createElectrolyzerMetal(HbmIngredient.of(item(input), 1), output1,
                output2, byproducts, 600);
        consumer.accept(finishedCompatRecipe(id("electrolyzer_metal/" + name), json));
    }

    private static void bedrockOreElectrolyzerRecipes(Consumer<FinishedRecipe> consumer) {
        for (BedrockOreType type : BedrockOreType.values()) {
            String suffix = type.suffix();
            BedrockOreProducts products = bedrockOreProducts(type);
            electrolyzerBedrockOre(consumer, suffix + "_primary_first", BedrockOreGrade.PRIMARY_FIRST, type,
                    List.of(new BedrockOreProductEntry(products.primary1(), 8),
                            new BedrockOreProductEntry(products.primary2(), 4),
                            new BedrockOreProductEntry(bedrockOre(BedrockOreGrade.CRUMBS, type))));
            electrolyzerBedrockOre(consumer, suffix + "_primary_second", BedrockOreGrade.PRIMARY_SECOND, type,
                    List.of(new BedrockOreProductEntry(products.primary1(), 4),
                            new BedrockOreProductEntry(products.primary2(), 8),
                            new BedrockOreProductEntry(bedrockOre(BedrockOreGrade.CRUMBS, type))));
            electrolyzerBedrockOre(consumer, suffix + "_crumbs", BedrockOreGrade.CRUMBS, type,
                    List.of(new BedrockOreProductEntry(products.primary1(), 2),
                            new BedrockOreProductEntry(products.primary2(), 2)));
        }
    }

    private static void electrolyzerBedrockOre(Consumer<FinishedRecipe> consumer, String name,
            BedrockOreGrade inputGrade, BedrockOreType type, List<BedrockOreProductEntry> entries) {
        BedrockOreElectrolyzerProduct product = bedrockOreElectrolyzerProduct(entries);
        JsonObject json = CompatRecipeRegistry.createElectrolyzerMetal(bedrockOreInput(inputGrade, type),
                product.output1(), product.output2(), product.byproducts().toArray(ItemStack[]::new), 600);
        consumer.accept(finishedCompatRecipe(id("electrolyzer_metal/bedrock_ore_" + name), json));
    }

    private static BedrockOreElectrolyzerProduct bedrockOreElectrolyzerProduct(
            List<BedrockOreProductEntry> entries) {
        List<MaterialStack> moltenProducts = new ArrayList<>();
        List<ItemStack> solidProducts = new ArrayList<>();
        for (BedrockOreProductEntry entry : entries) {
            if (entry.output() != null) {
                MaterialStack melt = moltenProducts.size() < 2
                        ? bedrockOreFluid(entry.output(), entry.multiplier())
                        : null;
                if (melt != null && !melt.isEmpty()) {
                    moltenProducts.add(melt);
                } else {
                    solidProducts.add(bedrockOreFragment(entry.output(), entry.multiplier()));
                }
            } else if (!entry.stack().isEmpty()) {
                solidProducts.add(entry.stack().copy());
            }
        }
        if (moltenProducts.isEmpty()) {
            moltenProducts.add(mat(Mats.MAT_SLAG, MaterialShapes.INGOT.q(2)));
        }
        MaterialStack output1 = moltenProducts.get(0);
        MaterialStack output2 = moltenProducts.size() > 1 ? moltenProducts.get(1) : null;
        return new BedrockOreElectrolyzerProduct(output1, output2, solidProducts);
    }

    private static void rotaryFurnaceRecipes(Consumer<FinishedRecipe> consumer) {
        rotaryFurnace(consumer, "steel_from_coal", 0,
                mat(Mats.MAT_STEEL, MaterialShapes.INGOT.q(1)), 100, 100, null,
                HbmIngredient.legacyOre("ingotIron", 1),
                HbmIngredient.legacyOre("gemCoal", Ingredient.of(Items.COAL), 1));
        rotaryFurnace(consumer, "steel_from_coke", 1,
                mat(Mats.MAT_STEEL, MaterialShapes.INGOT.q(1)), 100, 100, null,
                HbmIngredient.legacyOre("ingotIron", 1),
                HbmIngredient.legacyOre("gemAnyCoke", 1));
        rotaryFurnace(consumer, "steel_fragments_from_coal", 2,
                mat(Mats.MAT_STEEL, MaterialShapes.INGOT.q(2)), 200, 25, null,
                HbmIngredient.legacyOre("bedrockorefragmentIron", 9),
                HbmIngredient.legacyOre("gemCoal", Ingredient.of(Items.COAL), 1));
        rotaryFurnace(consumer, "steel_bedrock_ore_fragment_from_coal", 2,
                mat(Mats.MAT_STEEL, MaterialShapes.INGOT.q(2)), 200, 25, null,
                bedrockOreFragmentInput(Mats.MAT_IRON, 9),
                HbmIngredient.legacyOre("gemCoal", Ingredient.of(Items.COAL), 1));
        rotaryFurnace(consumer, "steel_fragments_from_coke", 3,
                mat(Mats.MAT_STEEL, MaterialShapes.INGOT.q(3)), 200, 25, null,
                HbmIngredient.legacyOre("bedrockorefragmentIron", 9),
                HbmIngredient.legacyOre("gemAnyCoke", 1));
        rotaryFurnace(consumer, "steel_bedrock_ore_fragment_from_coke", 3,
                mat(Mats.MAT_STEEL, MaterialShapes.INGOT.q(3)), 200, 25, null,
                bedrockOreFragmentInput(Mats.MAT_IRON, 9),
                HbmIngredient.legacyOre("gemAnyCoke", 1));
        rotaryFurnace(consumer, "steel_fragments_from_coke_flux", 4,
                mat(Mats.MAT_STEEL, MaterialShapes.INGOT.q(4)), 400, 25, null,
                HbmIngredient.legacyOre("bedrockorefragmentIron", 9),
                HbmIngredient.legacyOre("gemAnyCoke", 1),
                HbmIngredient.of(item("powder_flux"), 1));
        rotaryFurnace(consumer, "steel_bedrock_ore_fragment_from_coke_flux", 4,
                mat(Mats.MAT_STEEL, MaterialShapes.INGOT.q(4)), 400, 25, null,
                bedrockOreFragmentInput(Mats.MAT_IRON, 9),
                HbmIngredient.legacyOre("gemAnyCoke", 1),
                HbmIngredient.of(item("powder_flux"), 1));
        rotaryFurnace(consumer, "desh_from_lightoil", 5,
                mat(Mats.MAT_DESH, MaterialShapes.INGOT.q(1)), 100, 200,
                new HbmFluidStack(HbmFluids.LIGHTOIL, 100),
                HbmIngredient.of(item("powder_desh_ready"), 1));
        rotaryFurnace(consumer, "gunmetal", 6,
                mat(Mats.MAT_GUNMETAL, MaterialShapes.INGOT.q(4)), 200, 100, null,
                HbmIngredient.legacyOre("ingotCopper", Ingredient.of(Items.COPPER_INGOT), 3),
                HbmIngredient.legacyOre("ingotAluminum", 1));
        rotaryFurnace(consumer, "weaponsteel_flux", 7,
                mat(Mats.MAT_WEAPONSTEEL, MaterialShapes.INGOT.q(1)), 200, 400,
                new HbmFluidStack(HbmFluids.GAS_COKER, 100),
                HbmIngredient.legacyOre("ingotSteel", 1),
                HbmIngredient.of(item("powder_flux"), 2));
        rotaryFurnace(consumer, "saturnite", 8,
                mat(Mats.MAT_SATURN, MaterialShapes.INGOT.q(2)), 200, 400,
                new HbmFluidStack(HbmFluids.REFORMGAS, 250),
                HbmIngredient.legacyOre("dustDuraSteel", 4),
                HbmIngredient.legacyOre("dustCopper", 1));
        rotaryFurnace(consumer, "saturnite_borax", 9,
                mat(Mats.MAT_SATURN, MaterialShapes.INGOT.q(4)), 200, 300,
                new HbmFluidStack(HbmFluids.REFORMGAS, 250),
                HbmIngredient.legacyOre("dustDuraSteel", 4),
                HbmIngredient.legacyOre("dustCopper", 1),
                HbmIngredient.legacyOre("dustBorax", 1));
        rotaryFurnace(consumer, "aluminium_from_sodium_aluminate", 10,
                mat(Mats.MAT_ALUMINIUM, MaterialShapes.INGOT.q(2)), 100, 400,
                new HbmFluidStack(HbmFluids.SODIUM_ALUMINATE, 150));
        rotaryFurnace(consumer, "aluminium_flux", 11,
                mat(Mats.MAT_ALUMINIUM, MaterialShapes.INGOT.q(3)), 40, 200,
                new HbmFluidStack(HbmFluids.SODIUM_ALUMINATE, 150),
                HbmIngredient.of(item("powder_flux"), 2));
    }

    private static void rotaryFurnace(Consumer<FinishedRecipe> consumer, String name, int sourceOrder,
            MaterialStack output, int duration, int steam, @Nullable HbmFluidStack fluid,
            HbmIngredient... inputs) {
        JsonObject json = CompatRecipeRegistry.createRotaryFurnace(output, duration, steam, fluid, inputs,
                sourceOrder);
        consumer.accept(finishedCompatRecipe(id("rotary_furnace/" + name), json));
    }

    private static void crucibleRecipes(Consumer<FinishedRecipe> consumer) {
        int n = MaterialShapes.NUGGET.q(1);
        int i = MaterialShapes.INGOT.q(1);
        crucible(consumer, "steel", 0, "crucible.steel", "Steel Production", item("ingot_steel"), 20,
                mats(mat(Mats.MAT_IRON, n * 2), mat(Mats.MAT_CARBON, n * 3), mat(Mats.MAT_FLUX, n)),
                mats(mat(Mats.MAT_STEEL, n * 2)));
        crucible(consumer, "hematite", 1, "crucible.hematite", "Iron Production from Hematite",
                block("stone_resource_hematite"), 6,
                mats(mat(Mats.MAT_HEMATITE, i * 2), mat(Mats.MAT_FLUX, n * 2)),
                mats(mat(Mats.MAT_IRON, i), mat(Mats.MAT_SLAG, n * 3)));
        crucible(consumer, "malachite", 2, "crucible.malachite", "Copper Production from Malachite",
                block("stone_resource_malachite"), 6,
                mats(mat(Mats.MAT_MALACHITE, i * 2), mat(Mats.MAT_FLUX, n * 2)),
                mats(mat(Mats.MAT_COPPER, i), mat(Mats.MAT_SLAG, n * 3)));
        crucible(consumer, "redcopper", 3, "crucible.redcopper", "Red Copper Production",
                item("ingot_red_copper"), 2,
                mats(mat(Mats.MAT_COPPER, n), mat(Mats.MAT_REDSTONE, n)),
                mats(mat(Mats.MAT_MINGRADE, n * 2)));
        crucible(consumer, "hss", 4, "crucible.hss", "High-Speed Steel Production",
                item("ingot_dura_steel"), 9,
                mats(mat(Mats.MAT_STEEL, n * 5), mat(Mats.MAT_TUNGSTEN, n * 3), mat(Mats.MAT_COBALT, n)),
                mats(mat(Mats.MAT_DURA, n * 9)));
        crucible(consumer, "ferro", 5, "crucible.ferro", "Ferrouranium Production",
                item("ingot_ferrouranium"), 3,
                mats(mat(Mats.MAT_STEEL, n * 2), mat(Mats.MAT_U238, n)),
                mats(mat(Mats.MAT_FERRO, n * 3)));
        crucible(consumer, "tcalloy", 6, "crucible.tcalloy", "Technetium Steel Production",
                item("ingot_tcalloy"), 9,
                mats(mat(Mats.MAT_STEEL, n * 8), mat(Mats.MAT_TECHNETIUM, n)),
                mats(mat(Mats.MAT_TCALLOY, i)));
        crucible(consumer, "cdalloy", 7, "crucible.cdalloy", "Cadmium Steel Production",
                item("ingot_cdalloy"), 9,
                mats(mat(Mats.MAT_STEEL, n * 8), mat(Mats.MAT_CADMIUM, n)),
                mats(mat(Mats.MAT_CDALLOY, i)));
        crucible(consumer, "bbronze", 8, "crucible.bbronze", "Bismuth Bronze Production",
                item("ingot_bismuth_bronze"), 9,
                mats(mat(Mats.MAT_COPPER, n * 8), mat(Mats.MAT_BISMUTH, n), mat(Mats.MAT_FLUX, n * 3)),
                mats(mat(Mats.MAT_BBRONZE, i), mat(Mats.MAT_SLAG, n * 3)));
        crucible(consumer, "abronze", 9, "crucible.abronze", "Arsenic Bronze Production",
                item("ingot_arsenic_bronze"), 9,
                mats(mat(Mats.MAT_COPPER, n * 8), mat(Mats.MAT_ARSENIC, n), mat(Mats.MAT_FLUX, n * 3)),
                mats(mat(Mats.MAT_ABRONZE, i), mat(Mats.MAT_SLAG, n * 3)));
        crucible(consumer, "cmb", 10, "crucible.cmb", "CMB Steel Production",
                item("ingot_combine_steel"), 3,
                mats(mat(Mats.MAT_MAGTUNG, n * 6), mat(Mats.MAT_MUD, n * 3)),
                mats(mat(Mats.MAT_CMB, i)));
        crucible(consumer, "magtung", 11, "crucible.magtung", "Magnetized Tungsten Production",
                item("ingot_magnetized_tungsten"), 3,
                mats(mat(Mats.MAT_TUNGSTEN, i), mat(Mats.MAT_SCHRABIDIUM, n)),
                mats(mat(Mats.MAT_MAGTUNG, i)));
        crucible(consumer, "bscco", 12, "crucible.bscco", "BSCCO Production",
                item("ingot_bscco"), 3,
                mats(mat(Mats.MAT_BISMUTH, n * 2), mat(Mats.MAT_STRONTIUM, n * 2),
                        mat(Mats.MAT_CALCIUM, n * 2), mat(Mats.MAT_COPPER, n * 3)),
                mats(mat(Mats.MAT_BSCCO, i)));
    }

    private static void crucibleSmeltingRecipes(Consumer<FinishedRecipe> consumer) {
        int block = MaterialShapes.BLOCK.q(1);
        int ingot = MaterialShapes.INGOT.q(1);
        int nugget = MaterialShapes.NUGGET.q(1);
        int dust = MaterialShapes.DUST.q(1);
        int gem = MaterialShapes.GEM.q(1);
        int quart = MaterialShapes.QUART.q(1);

        crucibleSmelting(consumer, "stone", 0, HbmIngredient.legacyOre("stone", 1),
                mat(Mats.MAT_STONE, block));
        crucibleSmelting(consumer, "cobblestone", 1, HbmIngredient.legacyOre("cobblestone", 1),
                mat(Mats.MAT_STONE, block));
        crucibleSmelting(consumer, "obsidian", 2, HbmIngredient.of(Blocks.OBSIDIAN, 1),
                mat(Mats.MAT_OBSIDIAN, block));
        crucibleSmelting(consumer, "rail", 3, HbmIngredient.of(Items.RAIL, 1),
                mat(Mats.MAT_IRON, MaterialShapes.INGOT.q(6, 16)));
        crucibleSmelting(consumer, "powered_rail", 4, HbmIngredient.of(Items.POWERED_RAIL, 1),
                mat(Mats.MAT_GOLD, MaterialShapes.INGOT.q(6, 6)),
                mat(Mats.MAT_REDSTONE, MaterialShapes.DUST.q(1, 6)));
        crucibleSmelting(consumer, "detector_rail", 5, HbmIngredient.of(Items.DETECTOR_RAIL, 1),
                mat(Mats.MAT_IRON, MaterialShapes.INGOT.q(6, 6)),
                mat(Mats.MAT_REDSTONE, MaterialShapes.DUST.q(1, 6)));
        crucibleSmelting(consumer, "minecart", 6, HbmIngredient.of(Items.MINECART, 1),
                mat(Mats.MAT_IRON, ingot * 5));

        crucibleSmelting(consumer, "blade_titanium", 7, HbmIngredient.of(item("blade_titanium"), 1),
                mat(Mats.MAT_TITANIUM, ingot * 3));
        crucibleSmelting(consumer, "blade_tungsten", 8, HbmIngredient.of(item("blade_tungsten"), 1),
                mat(Mats.MAT_TUNGSTEN, ingot * 3));
        crucibleSmelting(consumer, "blades_steel", 9, HbmIngredient.of(item("blades_steel"), 1),
                mat(Mats.MAT_STEEL, ingot * 4));
        crucibleSmelting(consumer, "blades_titanium", 10, HbmIngredient.of(item("blades_titanium"), 1),
                mat(Mats.MAT_TITANIUM, ingot * 4));
        crucibleSmelting(consumer, "stamp_stone_flat", 11, HbmIngredient.of(item("stamp_stone_flat"), 1),
                mat(Mats.MAT_STONE, ingot * 3));
        crucibleSmelting(consumer, "stamp_iron_flat", 12, HbmIngredient.of(item("stamp_iron_flat"), 1),
                mat(Mats.MAT_IRON, ingot * 3));
        crucibleSmelting(consumer, "stamp_steel_flat", 13, HbmIngredient.of(item("stamp_steel_flat"), 1),
                mat(Mats.MAT_STEEL, ingot * 3));
        crucibleSmelting(consumer, "stamp_titanium_flat", 14, HbmIngredient.of(item("stamp_titanium_flat"), 1),
                mat(Mats.MAT_TITANIUM, ingot * 3));
        crucibleSmelting(consumer, "stamp_obsidian_flat", 15, HbmIngredient.of(item("stamp_obsidian_flat"), 1),
                mat(Mats.MAT_OBSIDIAN, ingot * 3));
        crucibleSmelting(consumer, "pipes_steel", 16, HbmIngredient.of(item("pipes_steel"), 1),
                mat(Mats.MAT_STEEL, block * 3));
        crucibleSmelting(consumer, "casing_small", 17, HbmIngredient.of(item("casing_small"), 1),
                mat(Mats.MAT_GUNMETAL, MaterialShapes.PLATE.q(1, 4)));
        crucibleSmelting(consumer, "casing_small_steel", 18, HbmIngredient.of(item("casing_small_steel"), 1),
                mat(Mats.MAT_WEAPONSTEEL, MaterialShapes.PLATE.q(1, 4)));
        crucibleSmelting(consumer, "casing_large", 19, HbmIngredient.of(item("casing_large"), 1),
                mat(Mats.MAT_GUNMETAL, MaterialShapes.PLATE.q(1, 2)));
        crucibleSmelting(consumer, "casing_large_steel", 20, HbmIngredient.of(item("casing_large_steel"), 1),
                mat(Mats.MAT_WEAPONSTEEL, MaterialShapes.PLATE.q(1, 2)));
        crucibleSmelting(consumer, "chunk_ore_cryolite", 22, HbmIngredient.of(item("chunk_ore_cryolite"), 1),
                mat(Mats.MAT_ALUMINIUM, ingot), mat(Mats.MAT_SODIUM, ingot));

        crucibleSmelting(consumer, "ore_iron", 23, HbmIngredient.legacyOre("oreIron", 1),
                mat(Mats.MAT_IRON, ingot * 2), mat(Mats.MAT_TITANIUM, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        crucibleSmelting(consumer, "ore_titanium", 24, HbmIngredient.legacyOre("oreTitanium", 1),
                mat(Mats.MAT_TITANIUM, ingot * 2), mat(Mats.MAT_IRON, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        crucibleSmelting(consumer, "ore_tungsten", 25, HbmIngredient.legacyOre("oreTungsten", 1),
                mat(Mats.MAT_TUNGSTEN, ingot * 2), mat(Mats.MAT_STONE, quart));
        crucibleSmelting(consumer, "ore_aluminium", 26, HbmIngredient.legacyOre("oreAluminium", 1),
                mat(Mats.MAT_ALUMINIUM, ingot * 2), mat(Mats.MAT_SODIUM, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        crucibleSmelting(consumer, "ore_coal", 27, HbmIngredient.legacyOre("oreCoal", 1),
                mat(Mats.MAT_CARBON, gem * 3), mat(Mats.MAT_STONE, quart));
        crucibleSmelting(consumer, "ore_gold", 28, HbmIngredient.legacyOre("oreGold", 1),
                mat(Mats.MAT_GOLD, ingot * 2), mat(Mats.MAT_LEAD, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        crucibleSmelting(consumer, "ore_uranium", 29, HbmIngredient.legacyOre("oreUranium", 1),
                mat(Mats.MAT_URANIUM, ingot * 2), mat(Mats.MAT_LEAD, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        crucibleSmelting(consumer, "ore_thorium232", 30, HbmIngredient.legacyOre("oreThorium232", 1),
                mat(Mats.MAT_THORIUM, ingot * 2), mat(Mats.MAT_URANIUM, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        crucibleSmelting(consumer, "ore_th232", 31, HbmIngredient.legacyOre("oreTh232", 1),
                mat(Mats.MAT_THORIUM, ingot * 2), mat(Mats.MAT_URANIUM, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        crucibleSmelting(consumer, "ore_thorium", 32, HbmIngredient.legacyOre("oreThorium", 1),
                mat(Mats.MAT_THORIUM, ingot * 2), mat(Mats.MAT_URANIUM, nugget * 3),
                mat(Mats.MAT_STONE, quart));
        crucibleSmelting(consumer, "ore_copper", 33, HbmIngredient.legacyOre("oreCopper", 1),
                mat(Mats.MAT_COPPER, ingot * 2), mat(Mats.MAT_STONE, quart));
        crucibleSmelting(consumer, "ore_lead", 34, HbmIngredient.legacyOre("oreLead", 1),
                mat(Mats.MAT_LEAD, ingot * 2), mat(Mats.MAT_GOLD, nugget),
                mat(Mats.MAT_STONE, quart));
        crucibleSmelting(consumer, "ore_beryllium", 35, HbmIngredient.legacyOre("oreBeryllium", 1),
                mat(Mats.MAT_BERYLLIUM, ingot * 2), mat(Mats.MAT_STONE, quart));
        crucibleSmelting(consumer, "ore_cobalt", 36, HbmIngredient.legacyOre("oreCobalt", 1),
                mat(Mats.MAT_COBALT, ingot), mat(Mats.MAT_STONE, quart));
        crucibleSmelting(consumer, "ore_redstone", 37, HbmIngredient.legacyOre("oreRedstone", 1),
                mat(Mats.MAT_REDSTONE, ingot * 4), mat(Mats.MAT_STONE, quart));
        crucibleSmelting(consumer, "ore_hematite", 38, HbmIngredient.legacyOre("oreHematite", 1),
                mat(Mats.MAT_HEMATITE, ingot));
        crucibleSmelting(consumer, "ore_malachite", 39, HbmIngredient.legacyOre("oreMalachite", 1),
                mat(Mats.MAT_MALACHITE, ingot * 6));

        crucibleSmelting(consumer, "stone_resource_limestone", 40,
                HbmIngredient.of(block("stone_resource_limestone"), 1), mat(Mats.MAT_FLUX, dust * 10));
        crucibleSmelting(consumer, "powder_flux", 41, HbmIngredient.of(item("powder_flux"), 1),
                mat(Mats.MAT_FLUX, dust));
        crucibleSmelting(consumer, "charcoal", 42, HbmIngredient.of(Items.CHARCOAL, 1),
                mat(Mats.MAT_CARBON, nugget * 3));
        crucibleSmelting(consumer, "powder_ash_wood", 43, HbmIngredient.of(item("powder_ash_wood"), 1),
                mat(Mats.MAT_CARBON, nugget));
        crucibleSmelting(consumer, "powder_ash_coal", 44, HbmIngredient.of(item("powder_ash_coal"), 1),
                mat(Mats.MAT_CARBON, nugget * 2));
        crucibleSmelting(consumer, "powder_ash_misc", 45, HbmIngredient.of(item("powder_ash_misc"), 1),
                mat(Mats.MAT_CARBON, nugget));
    }

    private static void crucibleSmelting(Consumer<FinishedRecipe> consumer, String name, int sourceOrder,
            HbmIngredient input, MaterialStack... output) {
        JsonObject json = CompatRecipeRegistry.createCrucibleSmelting(input, output, sourceOrder);
        consumer.accept(finishedCompatRecipe(id("crucible_smelting/" + name), json));
    }

    private static void crucible(Consumer<FinishedRecipe> consumer, String name, int sourceOrder,
            String internalName, String fallbackName, ItemLike icon, int frequency, MaterialStack[] input,
            MaterialStack[] output) {
        JsonObject json = CompatRecipeRegistry.createCrucible(internalName, fallbackName, new ItemStack(icon),
                frequency, input, output, sourceOrder);
        consumer.accept(finishedCompatRecipe(id("crucible/" + name), json));
    }

    private static MaterialStack mat(com.hbm.inventory.material.NTMMaterial material, int amount) {
        return new MaterialStack(material, amount);
    }

    private static MaterialStack[] mats(MaterialStack... stacks) {
        return stacks;
    }

    private static FinishedRecipe finishedCompatRecipe(ResourceLocation recipeId, JsonObject recipeJson) {
        if (!recipeJson.has("type")) {
            throw new IllegalStateException("HBM compat recipe has no serializer type: " + recipeId);
        }
        ResourceLocation serializerId = ResourceLocation.tryParse(recipeJson.get("type").getAsString());
        if (serializerId == null) {
            throw new IllegalStateException("HBM compat recipe has invalid serializer type: " + recipeId);
        }
        RecipeSerializer<?> serializer = HbmRegistryUtil.recipeSerializer(serializerId)
                .orElseThrow(() -> new IllegalStateException("Unknown HBM compat recipe serializer "
                        + serializerId + " for " + recipeId));
        JsonObject payload = recipeJson.deepCopy();
        payload.remove("type");
        return new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                for (String key : payload.keySet()) {
                    json.add(key, payload.get(key).deepCopy());
                }
            }

            @Override
            public ResourceLocation getId() {
                return recipeId;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return serializer;
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        };
    }

    private static FinishedRecipe finishedRecipe(ResourceLocation recipeId, JsonObject payload,
            RecipeSerializer<?> serializer) {
        JsonObject recipePayload = payload.deepCopy();
        return new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                for (String key : recipePayload.keySet()) {
                    json.add(key, recipePayload.get(key).deepCopy());
                }
            }

            @Override
            public ResourceLocation getId() {
                return recipeId;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return serializer;
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        };
    }

    private static void selfChargingConversion(Consumer<FinishedRecipe> consumer, ItemLike result, String recipeName, ItemLike isotopeBillet) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, result)
                .requires(ModItems.BATTERY_SC_EMPTY.get())
                .requires(isotopeBillet, 2)
                .unlockedBy("has_empty_self_charging_battery", has(ModItems.BATTERY_SC_EMPTY.get()))
                .save(consumer, id("energy/" + recipeName));
    }

    private static void lemegetonRecipes(Consumer<FinishedRecipe> consumer) {
        lemegetonRecipe(consumer, "iron_to_steel", HbmIngredient.legacyOre("ingotIron", 1),
                item("ingot_steel"), 0);
        lemegetonRecipe(consumer, "steel_to_dura_steel", HbmIngredient.legacyOre("ingotSteel", 1),
                item("ingot_dura_steel"), 1);
        lemegetonRecipe(consumer, "dura_steel_to_tcalloy", HbmIngredient.legacyOre("ingotDuraSteel", 1),
                item("ingot_tcalloy"), 2);
        lemegetonRecipe(consumer, "tcalloy_to_combine_steel", HbmIngredient.legacyOre("ingotTcAlloy", 1),
                item("ingot_combine_steel"), 3);
        lemegetonRecipe(consumer, "combine_steel_to_dineutronium", HbmIngredient.legacyOre("ingotCMBSteel", 1),
                item("ingot_dineutronium"), 4);
        lemegetonRecipe(consumer, "titanium_to_saturnite", HbmIngredient.legacyOre("ingotTitanium", 1),
                item("ingot_saturnite"), 5);
        lemegetonRecipe(consumer, "saturnite_to_starmetal", HbmIngredient.legacyOre("ingotSaturnite", 1),
                item("ingot_starmetal"), 6);
        lemegetonRecipe(consumer, "copper_to_red_copper", HbmIngredient.of(Items.COPPER_INGOT, 1),
                item("ingot_red_copper"), 7);
        lemegetonRecipe(consumer, "mingrade_to_desh", HbmIngredient.legacyOre("ingotMingrade", 1),
                item("ingot_desh"), 8);
        lemegetonRecipe(consumer, "desh_to_bscco", HbmIngredient.legacyOre("ingotWorkersAlloy", 1),
                item("ingot_bscco"), 9);
        lemegetonRecipe(consumer, "lead_to_gold", HbmIngredient.legacyOre("ingotLead", 1),
                Items.GOLD_INGOT, 10);
        lemegetonRecipe(consumer, "gold_to_bismuth", HbmIngredient.legacyOre("ingotGold", 1),
                item("ingot_bismuth"), 11);
        lemegetonRecipe(consumer, "bismuth_to_osmiridium", HbmIngredient.legacyOre("ingotBismuth", 1),
                item("ingot_osmiridium"), 12);
        lemegetonRecipe(consumer, "thorium232_to_uranium", HbmIngredient.legacyOre("ingotThorium232", 1),
                item("ingot_uranium"), 13);
        lemegetonRecipe(consumer, "uranium_to_u238", HbmIngredient.legacyOre("ingotUranium", 1),
                item("ingot_u238"), 14);
        lemegetonRecipe(consumer, "u238_to_u235", HbmIngredient.legacyOre("ingotUranium238", 1),
                item("ingot_u235"), 15);
        lemegetonRecipe(consumer, "u235_to_plutonium", HbmIngredient.legacyOre("ingotUranium235", 1),
                item("ingot_plutonium"), 16);
        lemegetonRecipe(consumer, "plutonium_to_pu238", HbmIngredient.legacyOre("ingotPlutonium", 1),
                item("ingot_pu238"), 17);
        lemegetonRecipe(consumer, "pu238_to_pu239", HbmIngredient.legacyOre("ingotPlutonium238", 1),
                item("ingot_pu239"), 18);
        lemegetonRecipe(consumer, "pu239_to_pu240", HbmIngredient.legacyOre("ingotPlutonium239", 1),
                item("ingot_pu240"), 19);
        lemegetonRecipe(consumer, "pu240_to_pu241", HbmIngredient.legacyOre("ingotPlutonium240", 1),
                item("ingot_pu241"), 20);
        lemegetonRecipe(consumer, "pu241_to_am241", HbmIngredient.legacyOre("ingotPlutonium241", 1),
                item("ingot_am241"), 21);
        lemegetonRecipe(consumer, "am241_to_am242", HbmIngredient.legacyOre("ingotAmericium241", 1),
                item("ingot_am242"), 22);
        lemegetonRecipe(consumer, "ra226_to_polonium", HbmIngredient.legacyOre("ingotRadium226", 1),
                item("ingot_polonium"), 23);
        lemegetonRecipe(consumer, "po210_to_technetium", HbmIngredient.legacyOre("ingotPolonium210", 1),
                item("ingot_technetium"), 24);
        lemegetonRecipe(consumer, "polymer_to_pc", HbmIngredient.legacyOre("ingotPolymer", 1),
                item("ingot_pc"), 25);
        lemegetonRecipe(consumer, "bakelite_to_pvc", HbmIngredient.legacyOre("ingotBakelite", 1),
                item("ingot_pvc"), 26);
        lemegetonRecipe(consumer, "latex_to_rubber", HbmIngredient.legacyOre("ingotLatex", 1),
                item("ingot_rubber"), 27);
        lemegetonRecipe(consumer, "coal_to_graphite", HbmIngredient.legacyOre("gemCoal", 1),
                item("ingot_graphite"), 28);
        lemegetonRecipe(consumer, "graphite_to_diamond", HbmIngredient.legacyOre("ingotGraphite", 1),
                Items.DIAMOND, 29);
        lemegetonRecipe(consumer, "diamond_to_cft", HbmIngredient.legacyOre("gemDiamond", 1),
                item("ingot_cft"), 30);
        lemegetonRecipe(consumer, "fluorite_to_sodalite", HbmIngredient.legacyOre("dustFluorite", 1),
                item("gem_sodalite"), 31);
        lemegetonRecipe(consumer, "sodalite_to_volcanic", HbmIngredient.legacyOre("gemSodalite", 1),
                item("gem_volcanic"), 32);
        lemegetonRecipe(consumer, "volcanic_to_rad", HbmIngredient.legacyOre("gemVolcanic", 1),
                item("gem_rad"), 33);
        lemegetonRecipe(consumer, "rad_to_alexandrite", HbmIngredient.of(item("gem_rad"), 1),
                item("gem_alexandrite"), 34);
        lemegetonRecipe(consumer, "sand_to_fiberglass", HbmIngredient.legacyOre("sand", 1),
                item("ingot_fiberglass"), 35);
        lemegetonRecipe(consumer, "fiberglass_to_asbestos", HbmIngredient.legacyOre("ingotFiberglass", 1),
                item("ingot_asbestos"), 36);
    }

    private static void lemegetonRecipe(Consumer<FinishedRecipe> consumer, String name, HbmIngredient input,
            ItemLike output, int sourceOrder) {
        lemegetonRecipe(consumer, name, input, new ItemStack(output), sourceOrder);
    }

    private static void lemegetonRecipe(Consumer<FinishedRecipe> consumer, String name, HbmIngredient input,
            ItemStack output, int sourceOrder) {
        ResourceLocation recipeId = id("lemegeton/" + name);
        consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                json.add("input", input.toJson());
                json.add("output", HbmItemOutput.of(output).toJson());
                json.addProperty("source_order", sourceOrder);
            }

            @Override
            public ResourceLocation getId() {
                return recipeId;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return ModRecipes.LEMEGETON.serializer().get();
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }

    private static void anvilSmithingRecipes(Consumer<FinishedRecipe> consumer) {
        anvilUpgradeRecipes(consumer, "iron", ModBlocks.ANVIL_IRON.get(), 0);
        anvilUpgradeRecipes(consumer, "lead", ModBlocks.ANVIL_LEAD.get(), 9);
        legacyAnvilSmithingRecipes(consumer);
    }

    private static void legacyAnvilSmithingRecipes(Consumer<FinishedRecipe> consumer) {
        for (int forged = 0; forged < 9; forged++) {
            anvilSmithingRecipe(consumer,
                    id("anvil_smithing/ingot_steel_dusted_" + forged + "_to_" + (forged + 1)),
                    18 + forged,
                    HbmIngredient.legacyMeta(LegacyMetaItemMappings.INGOT_STEEL_DUSTED, forged, 1),
                    HbmIngredient.legacyMeta(LegacyMetaItemMappings.INGOT_STEEL_DUSTED, forged, 1),
                    new ItemStack(ModItems.INGOT_STEEL_DUSTED_ITEMS.get(forged + 1).get()), 3,
                    AnvilSmithingRecipe.Kind.HOT, null, false);
        }
        anvilSmithingRecipe(consumer, id("anvil_smithing/ingot_steel_dusted_9_to_chainsteel"), 27,
                HbmIngredient.legacyMeta(LegacyMetaItemMappings.INGOT_STEEL_DUSTED, 9, 1),
                HbmIngredient.legacyMeta(LegacyMetaItemMappings.INGOT_STEEL_DUSTED, 9, 1),
                new ItemStack(ModItems.INGOT_CHAINSTEEL.get()), 3, AnvilSmithingRecipe.Kind.HOT, null, false);
        anvilSmithingRecipe(consumer, id("anvil_smithing/ingot_meteorite_to_forged"), 28,
                HbmIngredient.of(ModItems.INGOT_METEORITE.get(), 1),
                HbmIngredient.of(ModItems.INGOT_METEORITE.get(), 1),
                new ItemStack(ModItems.INGOT_METEORITE_FORGED.get()), 3, AnvilSmithingRecipe.Kind.HOT, null, false);
        anvilSmithingRecipe(consumer, id("anvil_smithing/ingot_meteorite_forged_to_blade"), 29,
                HbmIngredient.of(ModItems.INGOT_METEORITE_FORGED.get(), 1),
                HbmIngredient.of(ModItems.INGOT_METEORITE_FORGED.get(), 1),
                new ItemStack(ModItems.BLADE_METEORITE.get()), 3, AnvilSmithingRecipe.Kind.HOT, null, false);
        anvilSmithingRecipe(consumer, id("anvil_smithing/meteorite_sword_seared_to_reforged"), 30,
                HbmIngredient.of(ModItems.METEORITE_SWORD_SEARED.get(), 1),
                HbmIngredient.of(ModItems.INGOT_METEORITE_FORGED.get(), 1),
                new ItemStack(ModItems.METEORITE_SWORD_REFORGED.get()), 3, AnvilSmithingRecipe.Kind.HOT, null,
                false);
        anvilSmithingRecipe(consumer, id("anvil_smithing/cobalt_sword_to_decorated"), 31,
                HbmIngredient.of(ModItems.COBALT_SWORD.get(), 1),
                HbmIngredient.of(ModItems.INGOT_METEORITE.get(), 1),
                new ItemStack(ModItems.COBALT_DECORATED_SWORD.get()), 3, AnvilSmithingRecipe.Kind.HOT, null, false);
        anvilSmithingRecipe(consumer, id("anvil_smithing/cobalt_pickaxe_to_decorated"), 32,
                HbmIngredient.of(ModItems.COBALT_PICKAXE.get(), 1),
                HbmIngredient.of(ModItems.INGOT_METEORITE.get(), 1),
                new ItemStack(ModItems.COBALT_DECORATED_PICKAXE.get()), 3, AnvilSmithingRecipe.Kind.HOT, null,
                false);
        anvilSmithingRecipe(consumer, id("anvil_smithing/cobalt_axe_to_decorated"), 33,
                HbmIngredient.of(ModItems.COBALT_AXE.get(), 1),
                HbmIngredient.of(ModItems.INGOT_METEORITE.get(), 1),
                new ItemStack(ModItems.COBALT_DECORATED_AXE.get()), 3, AnvilSmithingRecipe.Kind.HOT, null, false);
        anvilSmithingRecipe(consumer, id("anvil_smithing/cobalt_shovel_to_decorated"), 34,
                HbmIngredient.of(ModItems.COBALT_SHOVEL.get(), 1),
                HbmIngredient.of(ModItems.INGOT_METEORITE.get(), 1),
                new ItemStack(ModItems.COBALT_DECORATED_SHOVEL.get()), 3, AnvilSmithingRecipe.Kind.HOT, null,
                false);
        anvilSmithingRecipe(consumer, id("anvil_smithing/cobalt_hoe_to_decorated"), 35,
                HbmIngredient.of(ModItems.COBALT_HOE.get(), 1),
                HbmIngredient.of(ModItems.INGOT_METEORITE.get(), 1),
                new ItemStack(ModItems.COBALT_DECORATED_HOE.get()), 3, AnvilSmithingRecipe.Kind.HOT, null, false);
        anvilSmithingRecipe(consumer, id("anvil_smithing/wings_limp_to_murk"), 36,
                HbmIngredient.of(ModItems.WINGS_LIMP.get(), 1),
                HbmIngredient.of(ModItems.PARTICLE_TACHYON.get(), 1),
                new ItemStack(ModItems.WINGS_MURK.get()), 1_916_169, AnvilSmithingRecipe.Kind.STANDARD, null,
                false);
        anvilSmithingRecipe(consumer, id("anvil_smithing/flask_infusion_shield"), 37,
                HbmIngredient.of(item("gem_alexandrite"), 1),
                HbmIngredient.of(ModItems.BOTTLE_NUKA.get(), 1),
                new ItemStack(ModItems.FLASK_INFUSION.get()), 4, AnvilSmithingRecipe.Kind.STANDARD, null, false);
        anvilSmithingRecipe(consumer, id("anvil_smithing/ingot_gunmetal"), 38,
                HbmIngredient.of(forgeTag("ingots/copper"), 1),
                HbmIngredient.of(forgeTag("ingots/aluminium"), 1),
                new ItemStack(item("ingot_gunmetal")), 1, AnvilSmithingRecipe.Kind.STANDARD, null, false);

        anvilMoldPrefixRecipe(consumer, "mold_nugget", 39, HbmIngredient.of(Items.GOLD_NUGGET, 1), "nugget", 0);
        anvilMoldPrefixRecipe(consumer, "mold_billet", 40, HbmIngredient.of(item("billet_u238"), 1), "billet", 1);
        anvilMoldPrefixRecipe(consumer, "mold_ingot", 41, HbmIngredient.of(Items.IRON_INGOT, 1), "ingot", 2);
        anvilMoldPrefixRecipe(consumer, "mold_plate", 42, HbmIngredient.of(ModItems.IRON_PLATE.get(), 1), "plate", 3);
        anvilMoldPrefixRecipe(consumer, "mold_plate_cast_single", 43,
                HbmIngredient.of(item("plate_cast_iron"), 1), "plateTriple", 19);
        anvilMoldPrefixRecipe(consumer, "mold_plate_cast_triple", 44,
                HbmIngredient.of(item("plate_cast_iron"), 3), "plateTriple", 13);
        anvilMoldPrefixRecipe(consumer, "mold_wire_fine", 45, HbmIngredient.of(item("wire_fine_copper"), 1),
                "wireFine", 4);
        anvilMoldExactRecipe(consumer, "mold_blade", 46, 5, 1, item("blade_titanium"), item("blade_tungsten"));
        anvilMoldExactRecipe(consumer, "mold_blades", 47, 6, 1, ModItems.SHREDDER_BLADES_STEEL.get(),
                ModItems.SHREDDER_BLADES_TITANIUM.get());
        anvilMoldExactRecipe(consumer, "mold_stamp", 48, 7, 1, ModItems.STONE_FLAT_STAMP.get(),
                ModItems.IRON_FLAT_STAMP.get(), ModItems.STEEL_FLAT_STAMP.get(),
                ModItems.TITANIUM_FLAT_STAMP.get(), ModItems.OBSIDIAN_FLAT_STAMP.get());
        anvilMoldPrefixRecipe(consumer, "mold_shell", 49, HbmIngredient.of(item("shell_steel"), 1), "shell", 8);
        anvilMoldPrefixRecipe(consumer, "mold_pipe", 50, HbmIngredient.of(item("pipes_steel"), 1), "pipe", 9);
        anvilMoldPrefixRecipe(consumer, "mold_ingots", 51, HbmIngredient.of(Items.IRON_INGOT, 9), "ingot", 10);
        anvilMoldPrefixRecipe(consumer, "mold_plates", 52, HbmIngredient.of(ModItems.IRON_PLATE.get(), 9), "plate", 11);
        anvilMoldPrefixRecipe(consumer, "mold_block", 53, HbmIngredient.of(Items.IRON_BLOCK, 1), "block", 12);
        anvilMoldPrefixRecipe(consumer, "mold_wire_dense", 54, HbmIngredient.of(item("wire_dense_mingrade"), 1),
                "wireDense", 20);
        anvilMoldPrefixRecipe(consumer, "mold_wires_dense", 55, HbmIngredient.of(item("wire_dense_mingrade"), 9),
                "wireDense", 21);
        anvilSmithingRecipe(consumer, id("anvil_smithing/food_cyanide"), 56,
                HbmIngredient.of(Items.APPLE, 1),
                HbmIngredient.of(Ingredient.of(ModItems.PLAN_C.get(), ModItems.PILL_RED.get()), 1),
                new ItemStack(Items.APPLE), 1, AnvilSmithingRecipe.Kind.CYANIDE, null, false);
        anvilSmithingRecipe(consumer, id("anvil_smithing/rename"), 57,
                HbmIngredient.of(Items.STONE, 1), HbmIngredient.of(Items.NAME_TAG, 1),
                new ItemStack(Items.STONE), 1, AnvilSmithingRecipe.Kind.RENAME, null, false);
    }

    private static void anvilMoldPrefixRecipe(Consumer<FinishedRecipe> consumer, String name, int sourceOrder,
            HbmIngredient demo, String prefix, int moldId) {
        anvilSmithingRecipe(consumer, id("anvil_smithing/" + name), sourceOrder, demo,
                HbmIngredient.of(ModItems.MOLD_BASE.get(), 1), FoundryMoldItem.stackForId(moldId), 1,
                AnvilSmithingRecipe.Kind.MOLD_PREFIX, prefix, false);
    }

    private static void anvilMoldExactRecipe(Consumer<FinishedRecipe> consumer, String name, int sourceOrder,
            int moldId, int count, ItemLike... matches) {
        anvilSmithingRecipe(consumer, id("anvil_smithing/" + name), sourceOrder,
                HbmIngredient.of(Ingredient.of(matches), count), HbmIngredient.of(ModItems.MOLD_BASE.get(), 1),
                FoundryMoldItem.stackForId(moldId), 1, AnvilSmithingRecipe.Kind.MOLD_EXACT, null, false);
    }

    private static void anvilUpgradeRecipes(Consumer<FinishedRecipe> consumer, String baseName, ItemLike baseAnvil,
            int sourceOrderOffset) {
        anvilSmithingRecipe(consumer, id("anvil_smithing/" + baseName + "_to_steel"), sourceOrderOffset,
                HbmIngredient.of(baseAnvil, 1), HbmIngredient.of(forgeTag("ingots/steel"), 10),
                new ItemStack(ModBlocks.ANVIL_STEEL.get()));
        anvilSmithingRecipe(consumer, id("anvil_smithing/" + baseName + "_to_desh"), sourceOrderOffset + 1,
                HbmIngredient.of(baseAnvil, 1), HbmIngredient.of(forgeTag("ingots/desh"), 10),
                new ItemStack(ModBlocks.ANVIL_DESH.get()));
        anvilSmithingRecipe(consumer, id("anvil_smithing/" + baseName + "_to_saturnite"), sourceOrderOffset + 2,
                HbmIngredient.of(baseAnvil, 1), HbmIngredient.of(forgeTag("ingots/saturnite"), 10),
                new ItemStack(ModBlocks.ANVIL_SATURNITE.get()));
        anvilSmithingRecipe(consumer, id("anvil_smithing/" + baseName + "_to_ferrouranium"), sourceOrderOffset + 3,
                HbmIngredient.of(item("ingot_ferrouranium"), 10), new ItemStack(ModBlocks.ANVIL_FERROURANIUM.get()),
                baseAnvil);
        anvilSmithingRecipe(consumer, id("anvil_smithing/" + baseName + "_to_bismuth_bronze"), sourceOrderOffset + 4,
                HbmIngredient.of(baseAnvil, 1), HbmIngredient.of(forgeTag("ingots/bismuth_bronze"), 10),
                new ItemStack(ModBlocks.ANVIL_BISMUTH_BRONZE.get()));
        anvilSmithingRecipe(consumer, id("anvil_smithing/" + baseName + "_to_arsenic_bronze"), sourceOrderOffset + 5,
                HbmIngredient.of(baseAnvil, 1), HbmIngredient.of(forgeTag("ingots/arsenic_bronze"), 10),
                new ItemStack(ModBlocks.ANVIL_ARSENIC_BRONZE.get()));
        anvilSmithingRecipe(consumer, id("anvil_smithing/" + baseName + "_to_schrabidate"), sourceOrderOffset + 6,
                HbmIngredient.of(baseAnvil, 1), HbmIngredient.of(forgeTag("ingots/schrabidate"), 10),
                new ItemStack(ModBlocks.ANVIL_SCHRABIDATE.get()));
        anvilSmithingRecipe(consumer, id("anvil_smithing/" + baseName + "_to_dineutronium"), sourceOrderOffset + 7,
                HbmIngredient.of(baseAnvil, 1), HbmIngredient.of(forgeTag("ingots/dineutronium"), 10),
                new ItemStack(ModBlocks.ANVIL_DNT.get()));
        anvilSmithingRecipe(consumer, id("anvil_smithing/" + baseName + "_to_osmiridium"), sourceOrderOffset + 8,
                HbmIngredient.of(baseAnvil, 1), HbmIngredient.of(forgeTag("ingots/osmiridium"), 10),
                new ItemStack(ModBlocks.ANVIL_OSMIRIDIUM.get()));
    }

    private static void anvilSmithingRecipe(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId,
            int sourceOrder, HbmIngredient left, HbmIngredient right, ItemStack output) {
        anvilSmithingRecipe(consumer, recipeId, sourceOrder, left, right, output, false);
    }

    private static void anvilSmithingRecipe(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId,
            int sourceOrder, HbmIngredient ferrouranium, ItemStack output, ItemLike baseAnvil) {
        anvilSmithingRecipe(consumer, recipeId, sourceOrder, HbmIngredient.of(baseAnvil, 1), ferrouranium, output,
                false);
    }

    private static void anvilSmithingRecipe(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId,
            int sourceOrder, HbmIngredient left, HbmIngredient right, ItemStack output, boolean shapeless) {
        anvilSmithingRecipe(consumer, recipeId, sourceOrder, left, right, output, 1,
                AnvilSmithingRecipe.Kind.STANDARD, null, shapeless);
    }

    private static void anvilSmithingRecipe(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId,
            int sourceOrder, HbmIngredient left, HbmIngredient right, ItemStack output, int tier,
            AnvilSmithingRecipe.Kind kind, @Nullable String moldPrefix, boolean shapeless) {
        consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                json.add("left", left.toJson());
                json.add("right", right.toJson());
                json.add("output", HbmItemOutput.of(output).toJson());
                json.addProperty("tier", tier);
                if (shapeless) {
                    json.addProperty("shapeless", true);
                }
                json.addProperty("source_order", sourceOrder);
                if (kind != AnvilSmithingRecipe.Kind.STANDARD) {
                    json.addProperty("kind", kind.jsonName());
                }
                if (moldPrefix != null && !moldPrefix.isBlank()) {
                    json.addProperty("mold_prefix", moldPrefix);
                }
            }

            @Override
            public ResourceLocation getId() {
                return recipeId;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return ModRecipes.ANVIL_SMITHING.serializer().get();
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }

    private static void anvilSmithingSupportRecipes(Consumer<FinishedRecipe> consumer) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.INGOT_STEEL_DUSTED.get())
                .requires(forgeTag("ingots/steel"))
                .requires(forgeTag("dusts/coal"))
                .unlockedBy("has_steel_ingot", has(forgeTag("ingots/steel")))
                .save(consumer, id("parts/ingot_steel_dusted"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MOLD_BASE.get())
                .pattern(" B ")
                .pattern("BIB")
                .pattern(" B ")
                .define('B', item("ingot_firebrick"))
                .define('I', forgeTag("ingots/iron"))
                .unlockedBy("has_firebrick_ingot", has(item("ingot_firebrick")))
                .save(consumer, id("parts/mold_base"));
    }

    private static void hotSmeltingRecipes(Consumer<FinishedRecipe> consumer) {
        hotSmeltingRecipe(consumer, "hot_ingot_chainsteel", ModItems.INGOT_CHAINSTEEL.get(),
                new ItemStack(ModItems.INGOT_CHAINSTEEL.get()), 0.0F);
        hotSmeltingRecipe(consumer, "hot_ingot_meteorite", ModItems.INGOT_METEORITE.get(),
                new ItemStack(ModItems.INGOT_METEORITE.get()), 0.0F);
        hotSmeltingRecipe(consumer, "hot_ingot_meteorite_forged", ModItems.INGOT_METEORITE_FORGED.get(),
                new ItemStack(ModItems.INGOT_METEORITE_FORGED.get()), 0.0F);
        hotSmeltingRecipe(consumer, "hot_blade_meteorite", ModItems.BLADE_METEORITE.get(),
                new ItemStack(ModItems.BLADE_METEORITE.get()), 0.0F);
        for (RegistryObject<Item> dusted : ModItems.INGOT_STEEL_DUSTED_ITEMS) {
            String name = HbmRegistryUtil.itemKey(dusted.get()).getPath();
            hotSmeltingRecipe(consumer, "hot_" + name, dusted.get(), new ItemStack(dusted.get()), 1.0F);
        }
    }

    private static void hotSmeltingRecipe(Consumer<FinishedRecipe> consumer, String name, ItemLike input,
            ItemStack output, float experience) {
        ResourceLocation recipeId = id("smelting/" + name);
        consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                json.addProperty("category", "misc");
                json.addProperty("cookingtime", 200);
                json.addProperty("experience", experience);
                json.add("ingredient", Ingredient.of(input).toJson());
                json.add("result", HbmItemOutput.of(output).toJson());
            }

            @Override
            public ResourceLocation getId() {
                return recipeId;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return ModRecipes.HOT_SMELTING.get();
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }

    private static void energyNetworkRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.CABLE_DIODE.get())
                .pattern(" Q ")
                .pattern("CAC")
                .pattern(" Q ")
                .define('Q', item("nugget_silicon"))
                .define('C', ModBlocks.RED_CABLE.get())
                .define('A', ModItems.ALUMINIUM_INGOT.get())
                .unlockedBy("has_red_cable", has(ModBlocks.RED_CABLE.get()))
                .save(consumer, id("energy/cable_diode"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModBlocks.RED_CABLE_CLASSIC.get())
                .requires(ModBlocks.RED_CABLE.get())
                .unlockedBy("has_red_cable", has(ModBlocks.RED_CABLE.get()))
                .save(consumer, id("energy/red_cable_classic"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModBlocks.RED_CABLE.get())
                .requires(ModBlocks.RED_CABLE_CLASSIC.get())
                .unlockedBy("has_red_cable_classic", has(ModBlocks.RED_CABLE_CLASSIC.get()))
                .save(consumer, id("energy/red_cable_from_classic"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RED_CABLE_PAINTABLE.get(), 16)
                .pattern("WRW")
                .pattern("RIR")
                .pattern("WRW")
                .define('W', ModItems.STEEL_PLATE.get())
                .define('R', item("wire_fine_mingrade"))
                .define('I', item("ingot_red_copper"))
                .unlockedBy("has_red_copper", has(item("ingot_red_copper")))
                .save(consumer, id("energy/red_cable_paintable"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RED_CONNECTOR_SUPER.get(), 2)
                .pattern("CCC")
                .pattern("III")
                .pattern(" S ")
                .define('C', ModItems.COPPER_COIL.get())
                .define('I', item("plate_polymer"))
                .define('S', forgeTag("ingots/any_resistant_alloy"))
                .unlockedBy("has_copper_coil", has(ModItems.COPPER_COIL.get()))
                .save(consumer, id("energy/red_connector_super"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RED_PYLON.get(), 4)
                .pattern("CWC")
                .pattern("PWP")
                .pattern(" T ")
                .define('C', ModItems.COPPER_COIL.get())
                .define('W', vanillaTag("planks"))
                .define('P', item("plate_polymer"))
                .define('T', ModBlocks.RED_WIRE_COATED.get())
                .unlockedBy("has_red_wire_coated", has(ModBlocks.RED_WIRE_COATED.get()))
                .save(consumer, id("energy/red_pylon"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RED_PYLON_MEDIUM_WOOD.get(), 2)
                .pattern("CCW")
                .pattern("IIW")
                .pattern("  S")
                .define('C', ModItems.COPPER_COIL.get())
                .define('W', vanillaTag("planks"))
                .define('I', item("plate_polymer"))
                .define('S', forgeTag("any/concrete"))
                .unlockedBy("has_copper_coil", has(ModItems.COPPER_COIL.get()))
                .save(consumer, id("energy/red_pylon_medium_wood"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE,
                        ModBlocks.RED_PYLON_MEDIUM_WOOD_TRANSFORMER.get())
                .requires(ModBlocks.RED_PYLON_MEDIUM_WOOD.get())
                .requires(item("plate_polymer"))
                .requires(ModItems.COPPER_COIL.get())
                .unlockedBy("has_red_pylon_medium_wood", has(ModBlocks.RED_PYLON_MEDIUM_WOOD.get()))
                .save(consumer, id("energy/red_pylon_medium_wood_transformer"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RED_PYLON_MEDIUM_STEEL.get(), 2)
                .pattern("CCW")
                .pattern("IIW")
                .pattern("  S")
                .define('C', ModItems.COPPER_COIL.get())
                .define('W', forgeTag("pipes/steel"))
                .define('I', item("plate_polymer"))
                .define('S', forgeTag("any/concrete"))
                .unlockedBy("has_copper_coil", has(ModItems.COPPER_COIL.get()))
                .save(consumer, id("energy/red_pylon_medium_steel"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE,
                        ModBlocks.RED_PYLON_MEDIUM_STEEL_TRANSFORMER.get())
                .requires(ModBlocks.RED_PYLON_MEDIUM_STEEL.get())
                .requires(item("plate_polymer"))
                .requires(ModItems.COPPER_COIL.get())
                .unlockedBy("has_red_pylon_medium_steel", has(ModBlocks.RED_PYLON_MEDIUM_STEEL.get()))
                .save(consumer, id("energy/red_pylon_medium_steel_transformer"));

        anvilConstructionRecipe(consumer, id("anvil_construction/energy/red_pylon_large"), 2,
                new ItemStack(ModBlocks.RED_PYLON_LARGE.get()),
                HbmIngredient.legacyOre("anyConcrete", 2),
                HbmIngredient.of(ModBlocks.STEEL_SCAFFOLD.get(), 8),
                HbmIngredient.of(item("plate_polymer"), 8),
                HbmIngredient.of(ModItems.COPPER_COIL.get(), 4));

        anvilConstructionRecipe(consumer, id("anvil_construction/energy/substation"), 2,
                new ItemStack(ModBlocks.SUBSTATION.get(), 2),
                HbmIngredient.of(forgeTag("any/concrete"), 8),
                HbmIngredient.of(forgeTag("ingots/steel"), 8),
                HbmIngredient.of(item("plate_polymer"), 12),
                HbmIngredient.of(ModItems.COPPER_COIL.get(), 8));

        for (int variant = 0; variant < 5; variant++) {
            anvilConstructionRecipe(consumer, id("anvil_construction/energy/red_cable_box_" + variant), 2,
                    legacyVariantStack(ModBlocks.RED_CABLE_BOX.get(), 16, variant),
                    HbmIngredient.legacyOre("ingotMingrade", 1),
                    HbmIngredient.of(item("plate_polymer"), 1));

            anvilConstructionRecipe(consumer, id("anvil_construction/energy/red_cable_box_" + variant + "_recycling"),
                    2,
                    List.of(HbmItemOutput.of(new ItemStack(item("ingot_red_copper"))),
                            HbmItemOutput.of(new ItemStack(item("plate_polymer")))),
                    "recycling",
                    HbmIngredient.partialNbt(legacyVariantStack(ModBlocks.RED_CABLE_BOX.get(), 16, variant)));
        }
    }

    private static void anvilConstructionRecipe(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId,
            int tierLower, ItemStack output, HbmIngredient... inputs) {
        anvilConstructionRecipe(consumer, recipeId, tierLower, List.of(HbmItemOutput.of(output)), "construction",
                inputs);
    }

    private static void anvilConstructionRecipe(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId,
            int tierLower, List<HbmItemOutput> outputs, String overlay, HbmIngredient... inputs) {
        consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                JsonArray inputArray = new JsonArray();
                for (HbmIngredient input : inputs) {
                    inputArray.add(input.toJson());
                }
                json.add("inputs", inputArray);
                if (outputs.size() == 1) {
                    json.add("output", outputs.get(0).toJson());
                } else {
                    JsonArray outputArray = new JsonArray();
                    for (HbmItemOutput output : outputs) {
                        outputArray.add(output.toJson());
                    }
                    json.add("outputs", outputArray);
                }
                json.addProperty("tier_lower", tierLower);
                json.addProperty("overlay", overlay);
            }

            @Override
            public ResourceLocation getId() {
                return recipeId;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return ModRecipes.ANVIL_CONSTRUCTION.serializer().get();
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }

    private static ItemStack legacyVariantStack(ItemLike item, int count, int variant) {
        ItemStack stack = new ItemStack(item, count);
        CompoundTag tag = new CompoundTag();
        tag.putInt("hbmLegacyVariant", variant);
        stack.setTag(tag);
        return stack;
    }

    private static void redstoneOverRadioRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_DISPLAY_BLANK.get(), 8)
                .pattern("B")
                .pattern("D")
                .define('B', forgeTag("ingots/bismuth"))
                .define('D', block("concrete_asbestos"))
                .unlockedBy("has_concrete_asbestos", has(block("concrete_asbestos")))
                .save(consumer, id("redstone_over_radio/rbmk_display_blank"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_DISPLAY.get())
                .pattern("C")
                .pattern("B")
                .define('C', item("crt_display"))
                .define('B', ModBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(ModBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(consumer, id("redstone_over_radio/rbmk_display"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_KEY_PAD.get())
                .pattern("R")
                .pattern("C")
                .pattern("B")
                .define('R', ModBlocks.RADIO_TORCH_SENDER.get())
                .define('C', forgeTag("circuits/vacuum_tube"))
                .define('B', ModBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(ModBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(consumer, id("redstone_over_radio/rbmk_key_pad"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_GAUGE.get())
                .pattern("R")
                .pattern("C")
                .pattern("B")
                .define('R', ModBlocks.RADIO_TORCH_RECEIVER.get())
                .define('C', forgeTag("circuits/vacuum_tube"))
                .define('B', ModBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(ModBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(consumer, id("redstone_over_radio/rbmk_gauge"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_NUMITRON.get())
                .pattern(" R ")
                .pattern("CCC")
                .pattern(" B ")
                .define('R', ModBlocks.RADIO_TORCH_RECEIVER.get())
                .define('C', forgeTag("circuits/numitron"))
                .define('B', ModBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(ModBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(consumer, id("redstone_over_radio/rbmk_numitron"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_GRAPH.get())
                .pattern("R")
                .pattern("C")
                .pattern("B")
                .define('R', ModBlocks.RADIO_TORCH_RECEIVER.get())
                .define('C', item("crt_display"))
                .define('B', ModBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(ModBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(consumer, id("redstone_over_radio/rbmk_graph"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_LEVER.get())
                .pattern("R")
                .pattern("C")
                .pattern("B")
                .define('R', ModBlocks.RADIO_TORCH_SENDER.get())
                .define('C', forgeTag("ingots/copper"))
                .define('B', ModBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(ModBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(consumer, id("redstone_over_radio/rbmk_lever"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_INDICATOR.get())
                .pattern("R")
                .pattern("C")
                .pattern("B")
                .define('R', ModBlocks.RADIO_TORCH_RECEIVER.get())
                .define('C', ModItems.TUNGSTEN_COIL.get())
                .define('B', ModBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(ModBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(consumer, id("redstone_over_radio/rbmk_indicator"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_TERMINAL.get())
                .pattern("R ")
                .pattern("CD")
                .pattern("B ")
                .define('R', ModBlocks.RADIO_TORCH_SENDER.get())
                .define('C', forgeTag("circuits/analog"))
                .define('D', item("crt_display"))
                .define('B', ModBlocks.RBMK_DISPLAY_BLANK.get())
                .unlockedBy("has_rbmk_display_blank", has(ModBlocks.RBMK_DISPLAY_BLANK.get()))
                .save(consumer, id("redstone_over_radio/rbmk_terminal"));
    }

    private static void rbmkRecipes(Consumer<FinishedRecipe> consumer) {
        SpecialRecipeBuilder.special(ModRecipes.RBMK_FUEL_DISASSEMBLY.get())
                .save(consumer, id("rbmk/rbmk_fuel_disassembly").toString());

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.RBMK_LID.get(), 4)
                .pattern("PPP")
                .pattern("CCC")
                .pattern("PPP")
                .define('P', forgeTag("plates/steel"))
                .define('C', block("concrete_asbestos"))
                .unlockedBy("has_concrete_asbestos", has(block("concrete_asbestos")))
                .save(consumer, id("rbmk/rbmk_lid"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.RBMK_LID_GLASS.get(), 4)
                .pattern("LLL")
                .pattern("BBB")
                .pattern("P P")
                .define('P', forgeTag("plates/steel"))
                .define('L', ModBlocks.GLASS_LEAD.get())
                .define('B', ModBlocks.GLASS_BORON.get())
                .unlockedBy("has_lead_glass", has(ModBlocks.GLASS_LEAD.get()))
                .save(consumer, id("rbmk/rbmk_lid_glass_lead_top"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.RBMK_LID_GLASS.get(), 4)
                .pattern("BBB")
                .pattern("LLL")
                .pattern("P P")
                .define('P', forgeTag("plates/steel"))
                .define('L', ModBlocks.GLASS_LEAD.get())
                .define('B', ModBlocks.GLASS_BORON.get())
                .unlockedBy("has_boron_glass", has(ModBlocks.GLASS_BORON.get()))
                .save(consumer, id("rbmk/rbmk_lid_glass_boron_top"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.RBMK_TOOL.get())
                .pattern(" A ")
                .pattern(" IA")
                .pattern("I  ")
                .define('A', forgeTag("ingots/lead"))
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_lead_ingot", has(forgeTag("ingots/lead")))
                .save(consumer, id("rbmk/rbmk_tool"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RBMK_FUEL_EMPTY.get())
                .pattern("ZRZ")
                .pattern("Z Z")
                .pattern("ZRZ")
                .define('Z', item("ingot_zirconium"))
                .define('R', item("rod_quad_empty"))
                .unlockedBy("has_zirconium_ingot", has(item("ingot_zirconium")))
                .save(consumer, id("rbmk/rbmk_fuel_empty"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_BLANK.get())
                .pattern("RRR")
                .pattern("R R")
                .pattern("RRR")
                .define('R', block("deco_rbmk"))
                .unlockedBy("has_deco_rbmk", has(block("deco_rbmk")))
                .save(consumer, id("rbmk/rbmk_blank_from_deco_rbmk"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_BLANK.get())
                .pattern("RRR")
                .pattern("R R")
                .pattern("RRR")
                .define('R', block("deco_rbmk_smooth"))
                .unlockedBy("has_deco_rbmk_smooth", has(block("deco_rbmk_smooth")))
                .save(consumer, id("rbmk/rbmk_blank_from_deco_rbmk_smooth"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_MODERATOR.get())
                .pattern(" G ")
                .pattern("GRG")
                .pattern(" G ")
                .define('G', ModBlocks.BLOCK_GRAPHITE.get())
                .define('R', ModBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(ModBlocks.RBMK_BLANK.get()))
                .save(consumer, id("rbmk/rbmk_moderator"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_ABSORBER.get())
                .pattern("GGG")
                .pattern("GRG")
                .pattern("GGG")
                .define('G', forgeTag("ingots/boron"))
                .define('R', ModBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(ModBlocks.RBMK_BLANK.get()))
                .save(consumer, id("rbmk/rbmk_absorber"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_REFLECTOR.get())
                .pattern("GGG")
                .pattern("GRG")
                .pattern("GGG")
                .define('G', item("neutron_reflector"))
                .define('R', ModBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(ModBlocks.RBMK_BLANK.get()))
                .save(consumer, id("rbmk/rbmk_reflector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_ROD.get())
                .pattern("C")
                .pattern("R")
                .pattern("C")
                .define('C', forgeTag("shells/steel"))
                .define('R', ModBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(ModBlocks.RBMK_BLANK.get()))
                .save(consumer, id("rbmk/rbmk_rod"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_ROD_MOD.get())
                .pattern("BGB")
                .pattern("GRG")
                .pattern("BGB")
                .define('G', ModBlocks.BLOCK_GRAPHITE.get())
                .define('R', ModBlocks.RBMK_ROD.get())
                .define('B', item("nugget_bismuth"))
                .unlockedBy("has_rbmk_rod", has(ModBlocks.RBMK_ROD.get()))
                .save(consumer, id("rbmk/rbmk_rod_mod"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_ROD_REASIM.get())
                .pattern("ZCZ")
                .pattern("ZRZ")
                .pattern("ZCZ")
                .define('Z', item("ingot_zirconium"))
                .define('C', forgeTag("shells/steel"))
                .define('R', ModBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(ModBlocks.RBMK_BLANK.get()))
                .save(consumer, id("rbmk/rbmk_rod_reasim"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_ROD_REASIM_MOD.get())
                .pattern("BGB")
                .pattern("GRG")
                .pattern("BGB")
                .define('G', ModBlocks.BLOCK_GRAPHITE.get())
                .define('R', ModBlocks.RBMK_ROD_REASIM.get())
                .define('B', forgeTag("ingots/any_resistant_alloy"))
                .unlockedBy("has_rbmk_rod_reasim", has(ModBlocks.RBMK_ROD_REASIM.get()))
                .save(consumer, id("rbmk/rbmk_rod_reasim_mod"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_BOILER.get())
                .pattern("CPC")
                .pattern("CRC")
                .pattern("CPC")
                .define('C', forgeTag("pipes/copper"))
                .define('P', forgeTag("shells/copper"))
                .define('R', ModBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(ModBlocks.RBMK_BLANK.get()))
                .save(consumer, id("rbmk/rbmk_boiler"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_HEATER.get())
                .pattern("CIC")
                .pattern("PRP")
                .pattern("CIC")
                .define('C', forgeTag("pipes/copper"))
                .define('I', forgeTag("ingots/any_plastic"))
                .define('P', forgeTag("shells/steel"))
                .define('R', ModBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(ModBlocks.RBMK_BLANK.get()))
                .save(consumer, id("rbmk/rbmk_heater"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_STORAGE.get())
                .pattern("C")
                .pattern("R")
                .pattern("C")
                .define('C', ModBlocks.CRATE_STEEL.get())
                .define('R', ModBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(ModBlocks.RBMK_BLANK.get()))
                .save(consumer, id("rbmk/rbmk_storage"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_OUTGASSER.get())
                .pattern("GHG")
                .pattern("GRG")
                .pattern("GTG")
                .define('G', ModBlocks.STEEL_GRATE.get())
                .define('H', Items.HOPPER)
                .define('R', ModBlocks.RBMK_BLANK.get())
                .define('T', item("tank_steel"))
                .unlockedBy("has_rbmk_blank", has(ModBlocks.RBMK_BLANK.get()))
                .save(consumer, id("rbmk/rbmk_outgasser"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_COOLER.get())
                .pattern("IGI")
                .pattern("GCG")
                .pattern("IGI")
                .define('I', item("plate_polymer"))
                .define('G', ModBlocks.STEEL_GRATE.get())
                .define('C', ModBlocks.RBMK_BLANK.get())
                .unlockedBy("has_rbmk_blank", has(ModBlocks.RBMK_BLANK.get()))
                .save(consumer, id("rbmk/rbmk_cooler"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_CRANE_CONSOLE.get())
                .pattern("BCD")
                .pattern("DDD")
                .define('B', forgeTag("ingots/boron"))
                .define('C', forgeTag("circuits/analog"))
                .define('D', block("deco_rbmk"))
                .unlockedBy("has_deco_rbmk", has(block("deco_rbmk")))
                .save(consumer, id("rbmk/rbmk_crane_console"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_CONSOLE.get())
                .pattern("BBB")
                .pattern("DGD")
                .pattern("DCD")
                .define('B', forgeTag("ingots/boron"))
                .define('D', block("deco_rbmk"))
                .define('G', forgeTag("glass_panes"))
                .define('C', forgeTag("circuits/analog"))
                .unlockedBy("has_deco_rbmk", has(block("deco_rbmk")))
                .save(consumer, id("rbmk/rbmk_console"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_CONTROL.get())
                .pattern(" B ")
                .pattern("GRG")
                .pattern(" B ")
                .define('G', item("ingot_graphite"))
                .define('B', item("motor"))
                .define('R', ModBlocks.RBMK_ABSORBER.get())
                .unlockedBy("has_rbmk_absorber", has(ModBlocks.RBMK_ABSORBER.get()))
                .save(consumer, id("rbmk/rbmk_control"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_CONTROL_MOD.get())
                .pattern("BGB")
                .pattern("GRG")
                .pattern("BGB")
                .define('G', ModBlocks.BLOCK_GRAPHITE.get())
                .define('R', ModBlocks.RBMK_CONTROL.get())
                .define('B', item("nugget_bismuth"))
                .unlockedBy("has_rbmk_control", has(ModBlocks.RBMK_CONTROL.get()))
                .save(consumer, id("rbmk/rbmk_control_mod"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_CONTROL_AUTO.get())
                .pattern("C")
                .pattern("R")
                .pattern("D")
                .define('C', forgeTag("circuits/advanced"))
                .define('R', ModBlocks.RBMK_CONTROL.get())
                .define('D', item("crt_display"))
                .unlockedBy("has_rbmk_control", has(ModBlocks.RBMK_CONTROL.get()))
                .save(consumer, id("rbmk/rbmk_control_auto"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_CONTROL_REASIM.get())
                .pattern(" B ")
                .pattern("GRG")
                .pattern(" B ")
                .define('G', item("ingot_graphite"))
                .define('B', item("motor"))
                .define('R', ModBlocks.RBMK_ABSORBER.get())
                .unlockedBy("has_rbmk_absorber", has(ModBlocks.RBMK_ABSORBER.get()))
                .save(consumer, id("rbmk/rbmk_control_reasim"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_CONTROL_REASIM_AUTO.get())
                .pattern("C")
                .pattern("R")
                .pattern("D")
                .define('C', forgeTag("circuits/advanced"))
                .define('R', ModBlocks.RBMK_CONTROL.get())
                .define('D', item("crt_display"))
                .unlockedBy("has_rbmk_control", has(ModBlocks.RBMK_CONTROL.get()))
                .save(consumer, id("rbmk/rbmk_control_reasim_auto"));

        rbmkFuelRodRecipe(consumer, "rbmk_fuel_ueu", "billet_uranium");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_meu", "billet_uranium_fuel");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_heu233", "billet_u233");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_heu235", "billet_u235");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_uzh", "billet_uzh");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_thmeu", "billet_thorium_fuel");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_mox", "billet_mox_fuel");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_lep", "billet_plutonium_fuel");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_mep", "billet_pu_mix");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_hep239", "billet_pu239");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_hep241", "billet_pu241");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_lea", "billet_americium_fuel");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_mea", "billet_am_mix");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_hea241", "billet_am241");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_hea242", "billet_am242");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_men", "billet_neptunium_fuel");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_hen", "billet_neptunium");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_po210be", "billet_po210be");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_ra226be", "billet_ra226be");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_pu238be", "billet_pu238be");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_leaus", "billet_australium_lesser");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_heaus", "billet_australium_greater");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_balefire", "egg_balefire_shard");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_les", "billet_les");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_mes", "billet_schrabidium_fuel");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_hes", "billet_hes");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_balefire_gold", "billet_balefire_gold");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_flashlead", "billet_flashlead");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_zfb_bismuth", "billet_zfb_bismuth");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_zfb_pu241", "billet_zfb_pu241");
        rbmkFuelRodRecipe(consumer, "rbmk_fuel_zfb_am_mix", "billet_zfb_am_mix");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("rbmk_fuel_drx"))
                .requires(item("rbmk_fuel_balefire"))
                .requires(ModItems.PARTICLE_DIGAMMA.get())
                .unlockedBy("has_balefire_rbmk_fuel", has(item("rbmk_fuel_balefire")))
                .save(consumer, id("rbmk/rbmk_fuel_drx"));
    }

    private static void legacySandMixRecipes(Consumer<FinishedRecipe> consumer) {
        sandMixRecipe(consumer, ModBlocks.SAND_URANIUM.get(), "uranium", forgeTag("dusts/uranium"), 8);
        sandMixRecipe(consumer, ModBlocks.SAND_POLONIUM.get(), "polonium", forgeTag("dusts/polonium"), 8);
        sandMixRecipe(consumer, ModBlocks.SAND_BORON.get(), "boron", forgeTag("dusts/boron"), 8);
        sandMixRecipe(consumer, ModBlocks.SAND_LEAD.get(), "lead", forgeTag("dusts/lead"), 8);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SAND_QUARTZ.get())
                .requires(Ingredient.of(forgeTag("sand")), 2)
                .requires(Ingredient.of(forgeTag("dusts/quartz")), 2)
                .unlockedBy("has_quartz_dust", has(forgeTag("dusts/quartz")))
                .save(consumer, id("blocks/sand_quartz"));

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModBlocks.SAND_BORON.get()), RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.GLASS_BORON.get(), 0.25F, 200)
                .unlockedBy("has_boron_sand", has(ModBlocks.SAND_BORON.get()))
                .save(consumer, id("smelting/glass_boron_from_sand_boron"));

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModBlocks.SAND_LEAD.get()), RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.GLASS_LEAD.get(), 0.25F, 200)
                .unlockedBy("has_lead_sand", has(ModBlocks.SAND_LEAD.get()))
                .save(consumer, id("smelting/glass_lead_from_sand_lead"));

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModBlocks.SAND_URANIUM.get()), RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.GLASS_URANIUM.get(), 0.25F, 200)
                .unlockedBy("has_uranium_sand", has(ModBlocks.SAND_URANIUM.get()))
                .save(consumer, id("smelting/glass_uranium_from_sand_uranium"));

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModBlocks.SAND_POLONIUM.get()), RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.GLASS_POLONIUM.get(), 0.75F, 200)
                .unlockedBy("has_polonium_sand", has(ModBlocks.SAND_POLONIUM.get()))
                .save(consumer, id("smelting/glass_polonium_from_sand_polonium"));

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModBlocks.SAND_QUARTZ.get()), RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.GLASS_QUARTZ.get(), 0.25F, 200)
                .unlockedBy("has_quartz_sand", has(ModBlocks.SAND_QUARTZ.get()))
                .save(consumer, id("smelting/glass_quartz_from_sand_quartz"));
    }

    private static void legacySmeltingRecipes(Consumer<FinishedRecipe> consumer) {
        legacySmeltingRecipe(consumer, "glyphid_meat", item("glyphid_meat"), item("glyphid_meat_grilled"), 1.0F,
                RecipeCategory.FOOD);

        legacySmeltingRecipe(consumer, "ore_thorium_to_ingot_th232", block("ore_thorium"), item("ingot_th232"), 3.0F);
        legacySmeltingRecipe(consumer, "deepslate_ore_thorium_to_ingot_th232", block("deepslate_ore_thorium"), item("ingot_th232"), 3.0F);
        legacySmeltingRecipe(consumer, "ore_uranium_to_ingot_uranium", block("ore_uranium"), item("ingot_uranium"), 6.0F);
        legacySmeltingRecipe(consumer, "deepslate_ore_uranium_to_ingot_uranium", block("deepslate_ore_uranium"), item("ingot_uranium"), 6.0F);
        legacySmeltingRecipe(consumer, "ore_uranium_scorched_to_ingot_uranium", block("ore_uranium_scorched"), item("ingot_uranium"), 6.0F);
        legacySmeltingRecipe(consumer, "ore_nether_uranium_to_ingot_uranium", block("ore_nether_uranium"), item("ingot_uranium"), 12.0F);
        legacySmeltingRecipe(consumer, "ore_nether_uranium_scorched_to_ingot_uranium", block("ore_nether_uranium_scorched"), item("ingot_uranium"), 12.0F);
        legacySmeltingRecipe(consumer, "ore_nether_plutonium_to_ingot_plutonium", block("ore_nether_plutonium"), item("ingot_plutonium"), 24.0F);
        legacySmeltingRecipe(consumer, "ore_titanium_to_ingot_titanium", block("ore_titanium"), item("ingot_titanium"), 3.0F);
        legacySmeltingRecipe(consumer, "deepslate_ore_titanium_to_ingot_titanium", block("deepslate_ore_titanium"), item("ingot_titanium"), 3.0F);
        legacySmeltingRecipe(consumer, "ore_tungsten_to_ingot_tungsten", block("ore_tungsten"), item("ingot_tungsten"), 6.0F);
        legacySmeltingRecipe(consumer, "deepslate_ore_tungsten_to_ingot_tungsten", block("deepslate_ore_tungsten"), item("ingot_tungsten"), 6.0F);
        legacySmeltingRecipe(consumer, "ore_nether_tungsten_to_ingot_tungsten", block("ore_nether_tungsten"), item("ingot_tungsten"), 12.0F);
        legacySmeltingRecipe(consumer, "ore_aluminium_to_chunk_ore_cryolite", block("ore_aluminium"), item("chunk_ore_cryolite"), 2.5F);
        legacySmeltingRecipe(consumer, "deepslate_ore_aluminium_to_chunk_ore_cryolite", block("deepslate_ore_aluminium"), item("chunk_ore_cryolite"), 2.5F);
        legacySmeltingRecipe(consumer, "ore_lead_to_ingot_lead", block("ore_lead"), item("ingot_lead"), 3.0F);
        legacySmeltingRecipe(consumer, "deepslate_ore_lead_to_ingot_lead", block("deepslate_ore_lead"), item("ingot_lead"), 3.0F);
        legacySmeltingRecipe(consumer, "ore_beryllium_to_ingot_beryllium", block("ore_beryllium"), item("ingot_beryllium"), 2.0F);
        legacySmeltingRecipe(consumer, "deepslate_ore_beryllium_to_ingot_beryllium", block("deepslate_ore_beryllium"), item("ingot_beryllium"), 2.0F);
        legacySmeltingRecipe(consumer, "ore_schrabidium_to_ingot_schrabidium", block("ore_schrabidium"), item("ingot_schrabidium"), 128.0F);
        legacySmeltingRecipe(consumer, "ore_nether_schrabidium_to_ingot_schrabidium", block("ore_nether_schrabidium"), item("ingot_schrabidium"), 256.0F);
        legacySmeltingRecipe(consumer, "ore_cobalt_to_ingot_cobalt", block("ore_cobalt"), item("ingot_cobalt"), 2.0F);
        legacySmeltingRecipe(consumer, "deepslate_ore_cobalt_to_ingot_cobalt", block("deepslate_ore_cobalt"), item("ingot_cobalt"), 2.0F);
        legacySmeltingRecipe(consumer, "ore_nether_cobalt_to_ingot_cobalt", block("ore_nether_cobalt"), item("ingot_cobalt"), 2.0F);
        legacySmeltingRecipe(consumer, "ore_gneiss_iron_to_iron_ingot", block("ore_gneiss_iron"), Items.IRON_INGOT, 5.0F);
        legacySmeltingRecipe(consumer, "ore_gneiss_gold_to_gold_ingot", block("ore_gneiss_gold"), Items.GOLD_INGOT, 5.0F);
        legacySmeltingRecipe(consumer, "ore_gneiss_uranium_to_ingot_uranium", block("ore_gneiss_uranium"), item("ingot_uranium"), 12.0F);
        legacySmeltingRecipe(consumer, "ore_gneiss_uranium_scorched_to_ingot_uranium", block("ore_gneiss_uranium_scorched"), item("ingot_uranium"), 12.0F);
        legacySmeltingRecipe(consumer, "ore_gneiss_lithium_to_lithium", block("ore_gneiss_lithium"), item("lithium"), 10.0F);
        legacySmeltingRecipe(consumer, "ore_gneiss_schrabidium_to_ingot_schrabidium", block("ore_gneiss_schrabidium"), item("ingot_schrabidium"), 256.0F);
        legacySmeltingRecipe(consumer, "ore_australium_to_nugget_australium", block("ore_australium"), item("nugget_australium"), 2.5F);

        legacySmeltingRecipe(consumer, "powder_australium_to_ingot_australium", item("powder_australium"), item("ingot_australium"), 5.0F);
        legacySmeltingRecipe(consumer, "briquette_coal_to_coke_coal", item("briquette_coal"), item("coke_coal"), 1.0F);
        legacySmeltingRecipe(consumer, "briquette_lignite_to_coke_lignite", item("briquette_lignite"), item("coke_lignite"), 1.0F);
        legacySmeltingRecipe(consumer, "briquette_wood_to_charcoal", item("briquette_wood"), Items.CHARCOAL, 1.0F);
        legacySmeltingRecipe(consumer, "powder_lead_to_ingot_lead", item("powder_lead"), item("ingot_lead"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_neptunium_to_ingot_neptunium", item("powder_neptunium"), item("ingot_neptunium"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_polonium_to_ingot_polonium", item("powder_polonium"), item("ingot_polonium"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_schrabidium_to_ingot_schrabidium", item("powder_schrabidium"), item("ingot_schrabidium"), 5.0F);
        legacySmeltingRecipe(consumer, "powder_schrabidate_to_ingot_schrabidate", item("powder_schrabidate"), item("ingot_schrabidate"), 5.0F);
        legacySmeltingRecipe(consumer, "powder_euphemium_to_ingot_euphemium", item("powder_euphemium"), item("ingot_euphemium"), 10.0F);
        legacySmeltingRecipe(consumer, "powder_aluminium_to_ingot_aluminium", item("powder_aluminium"), item("ingot_aluminium"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_beryllium_to_ingot_beryllium", item("powder_beryllium"), item("ingot_beryllium"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_copper_to_copper_ingot", item("powder_copper"), Items.COPPER_INGOT, 1.0F);
        legacySmeltingRecipe(consumer, "powder_gold_to_gold_ingot", item("powder_gold"), Items.GOLD_INGOT, 1.0F);
        legacySmeltingRecipe(consumer, "powder_iron_to_iron_ingot", item("powder_iron"), Items.IRON_INGOT, 1.0F);
        legacySmeltingRecipe(consumer, "powder_titanium_to_ingot_titanium", item("powder_titanium"), item("ingot_titanium"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_cobalt_to_ingot_cobalt", item("powder_cobalt"), item("ingot_cobalt"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_tungsten_to_ingot_tungsten", item("powder_tungsten"), item("ingot_tungsten"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_uranium_to_ingot_uranium", item("powder_uranium"), item("ingot_uranium"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_thorium_to_ingot_th232", item("powder_thorium"), item("ingot_th232"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_plutonium_to_ingot_plutonium", item("powder_plutonium"), item("ingot_plutonium"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_combine_steel_to_ingot_combine_steel", item("powder_combine_steel"), item("ingot_combine_steel"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_magnetized_tungsten_to_ingot_magnetized_tungsten", item("powder_magnetized_tungsten"), item("ingot_magnetized_tungsten"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_red_copper_to_ingot_red_copper", item("powder_red_copper"), item("ingot_red_copper"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_steel_to_ingot_steel", item("powder_steel"), item("ingot_steel"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_lithium_to_lithium", item("powder_lithium"), item("lithium"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_dura_steel_to_ingot_dura_steel", item("powder_dura_steel"), item("ingot_dura_steel"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_polymer_to_ingot_polymer", item("powder_polymer"), item("ingot_polymer"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_bakelite_to_ingot_bakelite", item("powder_bakelite"), item("ingot_bakelite"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_lanthanium_to_ingot_lanthanium", item("powder_lanthanium"), item("ingot_lanthanium"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_actinium_to_ingot_actinium", item("powder_actinium"), item("ingot_actinium"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_boron_to_ingot_boron", item("powder_boron"), item("ingot_boron"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_desh_to_ingot_desh", item("powder_desh"), item("ingot_desh"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_dineutronium_to_ingot_dineutronium", item("powder_dineutronium"), item("ingot_dineutronium"), 5.0F);
        legacySmeltingRecipe(consumer, "powder_asbestos_to_ingot_asbestos", item("powder_asbestos"), item("ingot_asbestos"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_zirconium_to_ingot_zirconium", item("powder_zirconium"), item("ingot_zirconium"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_tcalloy_to_ingot_tcalloy", item("powder_tcalloy"), item("ingot_tcalloy"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_au198_to_ingot_au198", item("powder_au198"), item("ingot_au198"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_sr90_to_ingot_sr90", item("powder_sr90"), item("ingot_sr90"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_ra226_to_ingot_ra226", item("powder_ra226"), item("ingot_ra226"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_tantalium_to_ingot_tantalium", item("powder_tantalium"), item("ingot_tantalium"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_niobium_to_ingot_niobium", item("powder_niobium"), item("ingot_niobium"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_bismuth_to_ingot_bismuth", item("powder_bismuth"), item("ingot_bismuth"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_calcium_to_ingot_calcium", item("powder_calcium"), item("ingot_calcium"), 1.0F);
        legacySmeltingRecipe(consumer, "powder_cadmium_to_ingot_cadmium", item("powder_cadmium"), item("ingot_cadmium"), 1.0F);
        legacySmeltingRecipe(consumer, "ball_resin_to_ingot_biorubber", item("ball_resin"), item("ingot_biorubber"), 0.1F);

        legacySmeltingRecipe(consumer, "arc_electrode_burnt_graphite_to_ingot_graphite", item("arc_electrode_burnt_graphite"), item("ingot_graphite"), 3.0F);
        legacySmeltingRecipe(consumer, "arc_electrode_burnt_lanthanium_to_ingot_lanthanium", item("arc_electrode_burnt_lanthanium"), item("ingot_lanthanium"), 3.0F);
        legacySmeltingRecipe(consumer, "arc_electrode_burnt_desh_to_ingot_desh", item("arc_electrode_burnt_desh"), item("ingot_desh"), 3.0F);
        legacySmeltingRecipe(consumer, "arc_electrode_burnt_saturnite_to_ingot_saturnite", item("arc_electrode_burnt_saturnite"), item("ingot_saturnite"), 3.0F);
        legacySmeltingRecipe(consumer, "rag_damp_to_rag", item("rag_damp"), item("rag"), 0.1F);
        legacySmeltingRecipe(consumer, "rag_piss_to_rag", item("rag_piss"), item("rag"), 0.1F);
        legacySmeltingRecipe(consumer, "ball_fireclay_to_ingot_firebrick", item("ball_fireclay"), item("ingot_firebrick"), 0.1F);
        legacySmeltingRecipe(consumer, "gravel_to_cobblestone", Blocks.GRAVEL, Blocks.COBBLESTONE, 0.0F,
                RecipeCategory.BUILDING_BLOCKS);
        legacySmeltingRecipe(consumer, "gravel_obsidian_to_obsidian", block("gravel_obsidian"), Blocks.OBSIDIAN, 0.0F,
                RecipeCategory.BUILDING_BLOCKS);
        legacySmeltingRecipe(consumer, "gravel_diamond_to_diamond", block("gravel_diamond"), Items.DIAMOND, 3.0F);
        legacySmeltingRecipe(consumer, "waste_trinitite_to_glass_trinitite", block("waste_trinitite"), block("glass_trinitite"), 0.25F,
                RecipeCategory.BUILDING_BLOCKS);
        legacySmeltingRecipe(consumer, "waste_trinitite_red_to_glass_trinitite", block("waste_trinitite_red"), block("glass_trinitite"), 0.25F,
                RecipeCategory.BUILDING_BLOCKS);
        legacySmeltingRecipe(consumer, "ash_digamma_to_glass_ash", block("ash_digamma"), block("glass_ash"), 10.0F,
                RecipeCategory.BUILDING_BLOCKS);
        legacySmeltingRecipe(consumer, "basalt_to_basalt_smooth", block("basalt"), block("basalt_smooth"), 0.1F,
                RecipeCategory.BUILDING_BLOCKS);
        legacySmeltingRecipe(consumer, "ingot_schraranium_to_nugget_schrabidium", item("ingot_schraranium"), item("nugget_schrabidium"), 2.0F);
        legacySmeltingRecipe(consumer, "lodestone_to_crystal_iron", item("lodestone"), item("crystal_iron"), 5.0F);
        legacySmeltingRecipe(consumer, "crystal_rare_to_powder_desh_mix", item("crystal_rare"), item("powder_desh_mix"), 2.0F);
        legacySmeltingRecipe(consumer, "crystal_osmiridium_to_ingot_osmiridium", item("crystal_osmiridium"), item("ingot_osmiridium"), 2.0F);
        legacySmeltingRecipe(consumer, "scrap_plastic_to_ingot_polymer", item("scrap_plastic"), item("ingot_polymer"), 0.1F);
    }

    private static void legacySmeltingRecipe(Consumer<FinishedRecipe> consumer, String name, ItemLike input,
            ItemLike output, float experience) {
        legacySmeltingRecipe(consumer, name, input, output, experience, RecipeCategory.MISC);
    }

    private static void legacySmeltingRecipe(Consumer<FinishedRecipe> consumer, String name, ItemLike input,
            ItemLike output, float experience, RecipeCategory category) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), category, output, experience, 200)
                .unlockedBy("has_" + name, has(input))
                .save(consumer, id("smelting/" + name));
    }

    private static void sandMixRecipe(Consumer<FinishedRecipe> consumer, ItemLike result, String name,
            TagKey<Item> dust, int count) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, result, count)
                .requires(Ingredient.of(forgeTag("sand")), 8)
                .requires(dust)
                .unlockedBy("has_" + name + "_dust", has(dust))
                .save(consumer, id("blocks/sand_" + name));
    }

    private static void rbmkFuelRodRecipe(Consumer<FinishedRecipe> consumer, String result, String billet) {
        ItemLike emptyRod = ModItems.RBMK_FUEL_EMPTY.get();
        ItemLike billetItem = item(billet);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item(result))
                .requires(emptyRod)
                .requires(billetItem, 8)
                .unlockedBy("has_empty_rbmk_fuel_rod", has(emptyRod))
                .save(consumer, id("rbmk/" + result));
    }

    private static void outgasserRecipes(Consumer<FinishedRecipe> consumer) {
        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyOre("blockLithium", 1))
                .outputFluid(HbmFluids.TRITIUM, 10_000)
                .save(consumer, id("outgasser/tritium_from_lithium_block"), 0);
        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyOre("ingotLithium", 1))
                .outputFluid(HbmFluids.TRITIUM, 1_000)
                .save(consumer, id("outgasser/tritium_from_lithium_ingot"), 1);
        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyOre("dustLithium", 1))
                .outputFluid(HbmFluids.TRITIUM, 1_000)
                .save(consumer, id("outgasser/tritium_from_lithium_dust"), 2);
        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyOre("dustTinyLithium", 1))
                .outputFluid(HbmFluids.TRITIUM, 100)
                .save(consumer, id("outgasser/tritium_from_lithium_tiny_dust"), 3);

        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyOre("ingotGold", 1))
                .outputItem(item("ingot_au198"))
                .save(consumer, id("outgasser/au198_ingot_from_gold_ingot"), 4);
        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyOre("nuggetGold", 1))
                .outputItem(item("nugget_au198"))
                .save(consumer, id("outgasser/au198_nugget_from_gold_nugget"), 5);
        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyOre("dustGold", 1))
                .outputItem(item("powder_au198"))
                .save(consumer, id("outgasser/au198_powder_from_gold_dust"), 6);

        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyOre("ingotTh232", 1))
                .outputItem(item("ingot_thorium_fuel"))
                .save(consumer, id("outgasser/thorium_fuel_ingot_from_th232_ingot"), 7);
        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyOre("nuggetTh232", 1))
                .outputItem(item("nugget_thorium_fuel"))
                .save(consumer, id("outgasser/thorium_fuel_nugget_from_th232_nugget"), 8);
        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyOre("billetTh232", 1))
                .outputItem(item("billet_thorium_fuel"))
                .save(consumer, id("outgasser/thorium_fuel_billet_from_th232_billet"), 9);

        OutgasserRecipeBuilder.outgasser(HbmIngredient.of(Items.BROWN_MUSHROOM, 1))
                .outputItem(ModBlocks.MUSH.get())
                .save(consumer, id("outgasser/glowing_mushroom_from_brown_mushroom"), 10);
        OutgasserRecipeBuilder.outgasser(HbmIngredient.of(Items.RED_MUSHROOM, 1))
                .outputItem(ModBlocks.MUSH.get())
                .save(consumer, id("outgasser/glowing_mushroom_from_red_mushroom"), 11);
        OutgasserRecipeBuilder.outgasser(HbmIngredient.of(Items.MUSHROOM_STEW, 1))
                .outputItem(ModItems.GLOWING_STEW.get())
                .save(consumer, id("outgasser/glowing_stew_from_mushroom_stew"), 12);

        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyOre("gemCoal", 1))
                .outputItem(item("oil_tar_coal"))
                .outputFluid(HbmFluids.SYNGAS, 50)
                .save(consumer, id("outgasser/coal_tar_from_coal"), 13);
        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyOre("dustCoal", 1))
                .outputItem(item("oil_tar_coal"))
                .outputFluid(HbmFluids.SYNGAS, 50)
                .save(consumer, id("outgasser/coal_tar_from_coal_dust"), 14);
        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyOre("blockCoal", 1))
                .outputItem(new ItemStack(item("oil_tar_coal"), 9))
                .outputFluid(HbmFluids.SYNGAS, 500)
                .save(consumer, id("outgasser/coal_tar_from_coal_block"), 15);

        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyOre("ingotPvc", 1))
                .outputItem(item("ingot_c4"))
                .outputFluid(HbmFluids.COLLOID, 250)
                .save(consumer, id("outgasser/c4_from_pvc"), 16);
        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyMeta(LegacyMetaItemMappings.OIL_TAR, 2, 1))
                .outputFluid(HbmFluids.COALOIL, 100)
                .save(consumer, id("outgasser/coaloil_from_coal_tar"), 17);
        OutgasserRecipeBuilder.outgasser(HbmIngredient.legacyMeta(LegacyMetaItemMappings.OIL_TAR, 4, 1))
                .outputFluid(HbmFluids.RADIOSOLVENT, 100)
                .save(consumer, id("outgasser/radiosolvent_from_wax_tar"), 18);
    }

    private static void solderingStationRecipes(Consumer<FinishedRecipe> consumer) {
        SolderingStationRecipeBuilder.soldering(item("circuit_analog"), 100, 100)
                .toppingItem("circuit_vacuum_tube", 3)
                .toppingItem("circuit_capacitor", 2)
                .pcbItem("circuit_pcb", 4)
                .solderLegacyOre("wireFineLead", 4)
                .save(consumer, id("soldering_station/circuit_analog"), 0);
        SolderingStationRecipeBuilder.soldering(item("circuit_basic"), 200, 250)
                .toppingItem("circuit_chip", 4)
                .pcbItem("circuit_pcb", 4)
                .solderLegacyOre("wireFineLead", 4)
                .save(consumer, id("soldering_station/circuit_basic"), 1);
        SolderingStationRecipeBuilder.soldering(item("circuit_advanced"), 300, 1_000)
                .fluid(HbmFluids.SULFURIC_ACID, 1_000)
                .toppingItem("circuit_chip", 16)
                .toppingItem("circuit_capacitor", 4)
                .pcbItem("circuit_pcb", 8)
                .pcbLegacyOre("ingotRubber", 2)
                .solderLegacyOre("wireFineLead", 8)
                .save(consumer, id("soldering_station/circuit_advanced"), 2);
        SolderingStationRecipeBuilder.soldering(item("circuit_capacitor_board"), 200, 300)
                .fluid(HbmFluids.PEROXIDE, 250)
                .toppingItem("circuit_capacitor_tantalium", 3)
                .pcbItem("circuit_pcb", 1)
                .solderLegacyOre("wireFineLead", 3)
                .save(consumer, id("soldering_station/circuit_capacitor_board"), 3);
        SolderingStationRecipeBuilder.soldering(item("circuit_bismoid"), 400, 10_000)
                .fluid(HbmFluids.SOLVENT, 1_000)
                .toppingItem("circuit_chip_bismoid", 4)
                .toppingItem("circuit_chip", 16)
                .toppingItem("circuit_capacitor", 24)
                .pcbItem("circuit_pcb", 12)
                .pcbLegacyOre("ingotAnyHardPlastic", 2)
                .solderLegacyOre("wireFineLead", 12)
                .save(consumer, id("soldering_station/circuit_bismoid"), 4);
        SolderingStationRecipeBuilder.soldering(item("circuit_quantum"), 400, 100_000)
                .fluid(HbmFluids.HELIUM4, 1_000)
                .toppingItem("circuit_chip_quantum", 4)
                .toppingItem("circuit_chip_bismoid", 16)
                .toppingItem("circuit_atomic_clock", 4)
                .pcbItem("circuit_pcb", 16)
                .pcbLegacyOre("ingotAnyHardPlastic", 4)
                .solderLegacyOre("wireFineLead", 16)
                .save(consumer, id("soldering_station/circuit_quantum"), 5);

        SolderingStationRecipeBuilder.soldering(item("circuit_controller"), 400, 15_000)
                .fluid(HbmFluids.PERFLUOROMETHYL, 1_000)
                .toppingItem("circuit_chip", 32)
                .toppingItem("circuit_capacitor", 32)
                .toppingItem("circuit_capacitor_tantalium", 16)
                .pcbItem("circuit_controller_chassis", 1)
                .pcbItem("upgrade_speed_1", 1)
                .solderLegacyOre("wireFineLead", 16)
                .save(consumer, id("soldering_station/circuit_controller"), 6);
        SolderingStationRecipeBuilder.soldering(item("circuit_controller_advanced"), 600, 25_000)
                .fluid(HbmFluids.PERFLUOROMETHYL, 4_000)
                .toppingItem("circuit_chip_bismoid", 16)
                .toppingItem("circuit_capacitor_tantalium", 48)
                .toppingItem("circuit_atomic_clock", 1)
                .pcbItem("circuit_controller_chassis", 1)
                .pcbItem("upgrade_speed_3", 1)
                .solderLegacyOre("wireFineLead", 24)
                .save(consumer, id("soldering_station/circuit_controller_advanced"), 7);
        SolderingStationRecipeBuilder.soldering(item("circuit_controller_quantum"), 600, 250_000)
                .fluid(HbmFluids.PERFLUOROMETHYL_COLD, 6_000)
                .toppingItem("circuit_chip_quantum", 16)
                .toppingItem("circuit_chip_bismoid", 48)
                .toppingItem("circuit_atomic_clock", 8)
                .pcbItem("circuit_controller_advanced", 2)
                .pcbItem("upgrade_overdrive_1", 1)
                .solderLegacyOre("wireFineLead", 32)
                .save(consumer, id("soldering_station/circuit_controller_quantum"), 8);

        SolderingStationRecipeBuilder.soldering(item("upgrade_speed_1"), 200, 1_000)
                .toppingItem("circuit_vacuum_tube", 4)
                .toppingItem("circuit_capacitor", 1)
                .pcbItem("upgrade_template", 1)
                .pcbLegacyOre("dustMingrade", 4)
                .save(consumer, id("soldering_station/upgrade_speed_1"), 99);
        SolderingStationRecipeBuilder.soldering(item("upgrade_effect_1"), 200, 1_000)
                .toppingItem("circuit_vacuum_tube", 4)
                .toppingItem("circuit_capacitor", 1)
                .pcbItem("upgrade_template", 1)
                .pcbLegacyOre("dustEmerald", 4)
                .save(consumer, id("soldering_station/upgrade_effect_1"), 100);
        SolderingStationRecipeBuilder.soldering(item("upgrade_power_1"), 200, 1_000)
                .toppingItem("circuit_vacuum_tube", 4)
                .toppingItem("circuit_capacitor", 1)
                .pcbItem("upgrade_template", 1)
                .pcbLegacyOre("dustGold", 4)
                .save(consumer, id("soldering_station/upgrade_power_1"), 101);
        SolderingStationRecipeBuilder.soldering(item("upgrade_afterburn_1"), 200, 1_000)
                .toppingItem("circuit_vacuum_tube", 4)
                .toppingItem("circuit_capacitor", 1)
                .pcbItem("upgrade_template", 1)
                .pcbLegacyOre("dustTungsten", 4)
                .save(consumer, id("soldering_station/upgrade_afterburn_1"), 102);
        SolderingStationRecipeBuilder.soldering(item("upgrade_fortune_1"), 200, 1_000)
                .toppingItem("circuit_vacuum_tube", 4)
                .toppingItem("circuit_capacitor", 1)
                .pcbItem("upgrade_template", 1)
                .pcbLegacyOre("dustNiobium", 4)
                .save(consumer, id("soldering_station/upgrade_fortune_1"), 103);
        SolderingStationRecipeBuilder.soldering(item("upgrade_radius"), 200, 1_000)
                .toppingItem("circuit_chip", 4)
                .toppingItem("circuit_capacitor", 4)
                .pcbItem("upgrade_template", 1)
                .pcbItemWithLegacyOre(Items.GLOWSTONE_DUST, 4, "dustGlowstone")
                .save(consumer, id("soldering_station/upgrade_radius"), 104);
        SolderingStationRecipeBuilder.soldering(item("upgrade_health"), 200, 1_000)
                .toppingItem("circuit_chip", 4)
                .toppingItem("circuit_capacitor", 4)
                .pcbItem("upgrade_template", 1)
                .pcbLegacyOre("dustLithium", 4)
                .save(consumer, id("soldering_station/upgrade_health"), 105);

        addFirstSolderingUpgrade(consumer, "upgrade_speed_1", "upgrade_speed_2", 107);
        addSecondSolderingUpgrade(consumer, "upgrade_speed_2", "upgrade_speed_3", 108);
        addFirstSolderingUpgrade(consumer, "upgrade_effect_1", "upgrade_effect_2", 109);
        addSecondSolderingUpgrade(consumer, "upgrade_effect_2", "upgrade_effect_3", 110);
        addFirstSolderingUpgrade(consumer, "upgrade_power_1", "upgrade_power_2", 111);
        addSecondSolderingUpgrade(consumer, "upgrade_power_2", "upgrade_power_3", 112);
        addFirstSolderingUpgrade(consumer, "upgrade_fortune_1", "upgrade_fortune_2", 113);
        addSecondSolderingUpgrade(consumer, "upgrade_fortune_2", "upgrade_fortune_3", 114);
        addFirstSolderingUpgrade(consumer, "upgrade_afterburn_1", "upgrade_afterburn_2", 115);
        addSecondSolderingUpgrade(consumer, "upgrade_afterburn_2", "upgrade_afterburn_3", 116);
    }

    private static void addFirstSolderingUpgrade(Consumer<FinishedRecipe> consumer, String lower, String higher,
            int sourceOrder) {
        SolderingStationRecipeBuilder.soldering(item(higher), 300, 10_000)
                .toppingItem("circuit_chip", 8)
                .toppingItem("circuit_capacitor", 4)
                .pcbItem(lower, 1)
                .pcbLegacyOre("ingotAnyPlastic", 4)
                .save(consumer, id("soldering_station/" + higher), sourceOrder);
    }

    private static void addSecondSolderingUpgrade(Consumer<FinishedRecipe> consumer, String lower, String higher,
            int sourceOrder) {
        SolderingStationRecipeBuilder.soldering(item(higher), 400, 25_000)
                .fluid(HbmFluids.SOLVENT, 500)
                .toppingItem("circuit_chip", 16)
                .toppingItem("circuit_capacitor", 16)
                .pcbItem(lower, 1)
                .pcbLegacyOre("ingotRubber", 4)
                .save(consumer, id("soldering_station/" + higher), sourceOrder);
    }

    private static void legacyToolRecipes(Consumer<FinishedRecipe> consumer) {
        standardToolSet(consumer, "steel", forgeTag("ingots/steel"),
                ModItems.STEEL_SWORD.get(), ModItems.STEEL_PICKAXE.get(), ModItems.STEEL_AXE.get(),
                ModItems.STEEL_SHOVEL.get(), ModItems.STEEL_HOE.get());
        standardToolSet(consumer, "titanium", forgeTag("ingots/titanium"),
                ModItems.TITANIUM_SWORD.get(), ModItems.TITANIUM_PICKAXE.get(), ModItems.TITANIUM_AXE.get(),
                ModItems.TITANIUM_SHOVEL.get(), ModItems.TITANIUM_HOE.get());
        standardToolSet(consumer, "cobalt", forgeTag("ingots/cobalt"),
                ModItems.COBALT_SWORD.get(), ModItems.COBALT_PICKAXE.get(), ModItems.COBALT_AXE.get(),
                ModItems.COBALT_SHOVEL.get(), ModItems.COBALT_HOE.get());
        standardToolSet(consumer, "cmb", forgeTag("ingots/combine_steel"),
                ModItems.CMB_SWORD.get(), ModItems.CMB_PICKAXE.get(), ModItems.CMB_AXE.get(),
                ModItems.CMB_SHOVEL.get(), ModItems.CMB_HOE.get());
        standardToolSet(consumer, "desh", forgeTag("ingots/desh"),
                ModItems.DESH_SWORD.get(), ModItems.DESH_PICKAXE.get(), ModItems.DESH_AXE.get(),
                ModItems.DESH_SHOVEL.get(), ModItems.DESH_HOE.get());

        legacyElectricToolRecipes(consumer);
        legacyNonLbsmAdvancedToolRecipes(consumer);
        legacyChainsawRecipe(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CROWBAR.get())
                .pattern("II")
                .pattern(" I")
                .pattern(" I")
                .define('I', forgeTag("ingots/steel"))
                .unlockedBy("has_steel_ingot", has(forgeTag("ingots/steel")))
                .save(consumer, id("tools/crowbar"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.MATCHSTICK.get(), 16)
                .pattern("I")
                .pattern("S")
                .define('I', forgeTag("dusts/sulfur"))
                .define('S', forgeTag("rods/wooden"))
                .unlockedBy("has_sulfur_dust", has(forgeTag("dusts/sulfur")))
                .save(consumer, id("tools/matchstick_sulfur"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.MATCHSTICK.get(), 24)
                .pattern("I")
                .pattern("S")
                .define('I', forgeTag("dusts/red_phosphorus"))
                .define('S', forgeTag("rods/wooden"))
                .unlockedBy("has_red_phosphorus_dust", has(forgeTag("dusts/red_phosphorus")))
                .save(consumer, id("tools/matchstick_red_phosphorus"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.WOOD_GAVEL.get())
                .pattern("SWS")
                .pattern(" R ")
                .pattern(" R ")
                .define('S', ItemTags.WOODEN_SLABS)
                .define('W', ItemTags.LOGS)
                .define('R', Items.STICK)
                .unlockedBy("has_wooden_slab", has(ItemTags.WOODEN_SLABS))
                .save(consumer, id("tools/wood_gavel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LEAD_GAVEL.get())
                .pattern("PIP")
                .pattern("IGI")
                .pattern("PIP")
                .define('P', item("pellet_buckshot"))
                .define('I', forgeTag("ingots/lead"))
                .define('G', ModItems.WOOD_GAVEL.get())
                .unlockedBy("has_wood_gavel", has(ModItems.WOOD_GAVEL.get()))
                .save(consumer, id("tools/lead_gavel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.PIPE_LEAD.get())
                .pattern("II")
                .pattern(" I")
                .pattern(" I")
                .define('I', forgeTag("pipes/lead"))
                .unlockedBy("has_lead_pipe", has(forgeTag("pipes/lead")))
                .save(consumer, id("tools/pipe_lead"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ULLAPOOL_CABER.get())
                .pattern("ITI")
                .pattern(" S ")
                .pattern(" S ")
                .define('I', forgeTag("plates/iron"))
                .define('T', Items.TNT)
                .define('S', Items.STICK)
                .unlockedBy("has_tnt", has(Items.TNT))
                .save(consumer, id("tools/ullapool_caber"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.CENTRI_STICK.get())
                .requires(item("centrifuge_element"))
                .requires(item("energy_core"))
                .requires(Items.STICK)
                .unlockedBy("has_centrifuge_element", has(item("centrifuge_element")))
                .save(consumer, id("tools/centri_stick"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SMASHING_HAMMER.get())
                .pattern("STS")
                .pattern("SPS")
                .pattern(" P ")
                .define('S', forgeTag("storage_blocks/steel"))
                .define('T', forgeTag("storage_blocks/tungsten"))
                .define('P', forgeTag("ingots/any_plastic"))
                .unlockedBy("has_steel_block", has(forgeTag("storage_blocks/steel")))
                .save(consumer, id("tools/smashing_hammer"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SHIMMER_SLEDGE.get())
                .pattern("H")
                .pattern("G")
                .pattern("G")
                .define('H', item("shimmer_head"))
                .define('G', item("shimmer_handle"))
                .unlockedBy("has_shimmer_head", has(item("shimmer_head")))
                .save(consumer, id("tools/shimmer_sledge"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SHIMMER_AXE.get())
                .pattern("H")
                .pattern("G")
                .pattern("G")
                .define('H', item("shimmer_axe_head"))
                .define('G', item("shimmer_handle"))
                .unlockedBy("has_shimmer_axe_head", has(item("shimmer_axe_head")))
                .save(consumer, id("tools/shimmer_axe"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.METEORITE_SWORD.get())
                .pattern("  B")
                .pattern("GB ")
                .pattern("SG ")
                .define('B', ModItems.BLADE_METEORITE.get())
                .define('G', forgeTag("plates/gold"))
                .define('S', Items.STICK)
                .unlockedBy("has_meteorite_blade", has(ModItems.BLADE_METEORITE.get()))
                .save(consumer, id("tools/meteorite_sword"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.DWARVEN_PICKAXE.get())
                .pattern("CIC")
                .pattern(" S ")
                .pattern(" S ")
                .define('C', forgeTag("ingots/copper"))
                .define('I', forgeTag("ingots/iron"))
                .define('S', Items.STICK)
                .unlockedBy("has_copper_ingot", has(forgeTag("ingots/copper")))
                .save(consumer, id("tools/dwarven_pickaxe"));

        meteorUpgradeToolRecipe(consumer, "bismuth_pickaxe", ModItems.BISMUTH_PICKAXE.get(),
                item("ingot_bismuth"), ModItems.STARMETAL_PICKAXE.get());
        meteorUpgradeToolRecipe(consumer, "volcanic_pickaxe", ModItems.VOLCANIC_PICKAXE.get(),
                item("gem_volcanic"), ModItems.STARMETAL_PICKAXE.get());
        chlorophyteToolRecipe(consumer, "chlorophyte_pickaxe_from_bismuth_pickaxe",
                ModItems.CHLOROPHYTE_PICKAXE.get(), ModItems.BISMUTH_PICKAXE.get());
        chlorophyteToolRecipe(consumer, "chlorophyte_pickaxe_from_volcanic_pickaxe",
                ModItems.CHLOROPHYTE_PICKAXE.get(), ModItems.VOLCANIC_PICKAXE.get());
        meseToolRecipe(consumer, "mese_pickaxe", ModItems.MESE_PICKAXE.get(),
                ModItems.CHLOROPHYTE_PICKAXE.get());

        meteorUpgradeToolRecipe(consumer, "bismuth_axe", ModItems.BISMUTH_AXE.get(),
                item("ingot_bismuth"), ModItems.STARMETAL_AXE.get());
        meteorUpgradeToolRecipe(consumer, "volcanic_axe", ModItems.VOLCANIC_AXE.get(),
                item("gem_volcanic"), ModItems.STARMETAL_AXE.get());
        chlorophyteToolRecipe(consumer, "chlorophyte_axe_from_bismuth_axe",
                ModItems.CHLOROPHYTE_AXE.get(), ModItems.BISMUTH_AXE.get());
        chlorophyteToolRecipe(consumer, "chlorophyte_axe_from_volcanic_axe",
                ModItems.CHLOROPHYTE_AXE.get(), ModItems.VOLCANIC_AXE.get());
        meseToolRecipe(consumer, "mese_axe", ModItems.MESE_AXE.get(), ModItems.CHLOROPHYTE_AXE.get());

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.RANGEFINDER.get())
                .pattern("GRC")
                .pattern("  S")
                .define('G', forgeTag("glass_panes"))
                .define('R', Items.REDSTONE)
                .define('C', forgeTag("circuits/basic"))
                .define('S', forgeTag("plates/steel"))
                .unlockedBy("has_basic_circuit", has(forgeTag("circuits/basic")))
                .save(consumer, id("tools/rangefinder"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.REACTOR_SENSOR.get())
                .pattern("WPW")
                .pattern("CMC")
                .pattern("PPP")
                .define('W', forgeTag("wires/tungsten"))
                .define('P', forgeTag("plates/lead"))
                .define('C', forgeTag("circuits/basic"))
                .define('M', item("magnetron"))
                .unlockedBy("has_magnetron", has(item("magnetron")))
                .save(consumer, id("tools/reactor_sensor"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SURVEY_SCANNER.get())
                .pattern("SWS")
                .pattern(" G ")
                .pattern("PCP")
                .define('S', forgeTag("plates/steel"))
                .define('W', forgeTag("wires/gold"))
                .define('G', Items.GOLD_INGOT)
                .define('P', forgeTag("ingots/any_plastic"))
                .define('C', forgeTag("circuits/advanced"))
                .unlockedBy("has_advanced_circuit", has(forgeTag("circuits/advanced")))
                .save(consumer, id("tools/survey_scanner"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.GEIGER_COUNTER.get())
                .pattern("GPP")
                .pattern("WCS")
                .pattern("WBB")
                .define('G', forgeTag("ingots/gold"))
                .define('P', forgeTag("ingots/any_rubber"))
                .define('W', forgeTag("wires/gold"))
                .define('C', forgeTag("circuits/basic"))
                .define('S', forgeTag("plates/steel"))
                .define('B', forgeTag("ingots/beryllium"))
                .unlockedBy("has_basic_circuit", has(forgeTag("circuits/basic")))
                .save(consumer, id("tools/geiger_counter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.DOSIMETER.get())
                .pattern("WGW")
                .pattern("WCW")
                .pattern("WBW")
                .define('W', vanillaTag("planks"))
                .define('G', forgeTag("glass_panes"))
                .define('C', forgeTag("circuits/vacuum_tube"))
                .define('B', forgeTag("ingots/beryllium"))
                .unlockedBy("has_vacuum_tube", has(forgeTag("circuits/vacuum_tube")))
                .save(consumer, id("tools/dosimeter"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModBlocks.GEIGER.get())
                .requires(ModItems.GEIGER_COUNTER.get())
                .unlockedBy("has_geiger_counter", has(ModItems.GEIGER_COUNTER.get()))
                .save(consumer, id("blocks/geiger"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.GEIGER_COUNTER.get())
                .requires(ModBlocks.GEIGER.get())
                .unlockedBy("has_geiger", has(ModBlocks.GEIGER.get()))
                .save(consumer, id("tools/geiger_counter_from_block"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.DIGAMMA_DIAGNOSTIC.get())
                .requires(ModItems.GEIGER_COUNTER.get())
                .requires(item("billet_polonium"))
                .requires(forgeTag("ingots/asbestos"))
                .unlockedBy("has_geiger_counter", has(ModItems.GEIGER_COUNTER.get()))
                .save(consumer, id("tools/digamma_diagnostic"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.BOTTLE_OPENER.get())
                .pattern("S")
                .pattern("P")
                .define('S', forgeTag("plates/steel"))
                .define('P', vanillaTag("planks"))
                .unlockedBy("has_steel_plate", has(forgeTag("plates/steel")))
                .save(consumer, id("tools/bottle_opener"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, Items.SADDLE)
                .pattern("LLL")
                .pattern("LRL")
                .pattern(" S ")
                .define('L', Items.LEATHER)
                .define('R', item("plant_item_rope"))
                .define('S', forgeTag("ingots/steel"))
                .unlockedBy("has_rope", has(item("plant_item_rope")))
                .save(consumer, id("tools/saddle"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BOBMAZON.get())
                .requires(Items.BOOK)
                .requires(Items.GOLD_NUGGET)
                .requires(Items.STRING)
                .requires(forgeTag("dyes/blue"))
                .unlockedBy("has_book", has(Items.BOOK))
                .save(consumer, id("tools/bobmazon"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, ModItems.BOAT_RUBBER.get())
                .pattern("L L")
                .pattern("LLL")
                .define('L', forgeTag("ingots/any_rubber"))
                .unlockedBy("has_any_rubber", has(forgeTag("ingots/any_rubber")))
                .save(consumer, id("tools/boat_rubber"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.REBAR_PLACER.get())
                .pattern("RDR")
                .pattern("DWD")
                .pattern("RDR")
                .define('R', ModBlocks.legacyBlock("rebar").get())
                .define('D', item("ducttape"))
                .define('W', item("wrench"))
                .unlockedBy("has_rebar", has(ModBlocks.legacyBlock("rebar").get()))
                .save(consumer, id("tools/rebar_placer"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, ModItems.CART_EMPTY_WOOD.get())
                .pattern("P P")
                .pattern("WPW")
                .define('P', ItemTags.WOODEN_SLABS)
                .define('W', vanillaTag("planks"))
                .unlockedBy("has_wooden_slab", has(ItemTags.WOODEN_SLABS))
                .save(consumer, id("tools/cart_empty_wood"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, ModItems.CART_EMPTY_STEEL.get())
                .pattern("P P")
                .pattern("IPI")
                .define('P', forgeTag("plates/steel"))
                .define('I', forgeTag("ingots/steel"))
                .unlockedBy("has_steel_plate", has(forgeTag("plates/steel")))
                .save(consumer, id("tools/cart_empty_steel"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, ModItems.CART_EMPTY_PAINTED.get())
                .requires(ModItems.CART_EMPTY_STEEL.get())
                .requires(forgeTag("dyes/red"))
                .unlockedBy("has_empty_steel_cart", has(ModItems.CART_EMPTY_STEEL.get()))
                .save(consumer, id("tools/cart_empty_painted"));

        cartPowderRecipe(consumer, "wood", ModItems.CART_EMPTY_WOOD.get(), ModItems.CART_POWDER_WOOD.get());
        cartPowderRecipe(consumer, "steel", ModItems.CART_EMPTY_STEEL.get(), ModItems.CART_POWDER_STEEL.get());
        cartPowderRecipe(consumer, "painted", ModItems.CART_EMPTY_PAINTED.get(), ModItems.CART_POWDER_PAINTED.get());
        cartSemtexRecipe(consumer, "wood", ModItems.CART_EMPTY_WOOD.get(), ModItems.CART_SEMTEX_WOOD.get());
        cartSemtexRecipe(consumer, "steel", ModItems.CART_EMPTY_STEEL.get(), ModItems.CART_SEMTEX_STEEL.get());
        cartSemtexRecipe(consumer, "painted", ModItems.CART_EMPTY_PAINTED.get(), ModItems.CART_SEMTEX_PAINTED.get());
        cartDestroyerRecipe(consumer, "steel", ModItems.CART_EMPTY_STEEL.get(),
                ModItems.CART_DESTROYER_STEEL.get());
        cartDestroyerRecipe(consumer, "painted", ModItems.CART_EMPTY_PAINTED.get(),
                ModItems.CART_DESTROYER_PAINTED.get());
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, ModItems.CART_CRATE.get())
                .pattern("C")
                .pattern("S")
                .define('C', ModBlocks.CRATE_STEEL.get())
                .define('S', Items.MINECART)
                .unlockedBy("has_steel_crate", has(ModBlocks.CRATE_STEEL.get()))
                .save(consumer, id("tools/cart_crate"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.OIL_DETECTOR.get())
                .pattern("W I")
                .pattern("WCI")
                .pattern("PPP")
                .define('W', forgeTag("wires/gold"))
                .define('I', forgeTag("ingots/copper"))
                .define('C', forgeTag("circuits/analog"))
                .define('P', forgeTag("plates/steel"))
                .unlockedBy("has_analog_circuit", has(forgeTag("circuits/analog")))
                .save(consumer, id("tools/oil_detector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.COLTAN_TOOL.get())
                .pattern("ACA")
                .pattern("CXC")
                .pattern("ACA")
                .define('A', forgeTag("ingots/copper"))
                .define('C', forgeTag("crystals/cinnabar"))
                .define('X', Items.COMPASS)
                .unlockedBy("has_cinnabar", has(forgeTag("crystals/cinnabar")))
                .save(consumer, id("tools/coltan_tool"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, item("reacher"))
                .pattern("BIB")
                .pattern("P P")
                .pattern("B B")
                .define('B', forgeTag("bolts/tungsten"))
                .define('I', forgeTag("ingots/tungsten"))
                .define('P', forgeTag("ingots/any_rubber"))
                .unlockedBy("has_tungsten_ingot", has(forgeTag("ingots/tungsten")))
                .save(consumer, id("tools/reacher"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.ORE_DENSITY_SCANNER.get())
                .pattern("VVV")
                .pattern("CSC")
                .pattern("GGG")
                .define('V', forgeTag("circuits/vacuum_tube"))
                .define('C', forgeTag("circuits/capacitor"))
                .define('S', forgeTag("circuits/controller_chassis"))
                .define('G', forgeTag("plates/gold"))
                .unlockedBy("has_controller_chassis", has(forgeTag("circuits/controller_chassis")))
                .save(consumer, id("tools/ore_density_scanner"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.DESIGNATOR.get())
                .pattern("  A")
                .pattern("#B#")
                .pattern("#B#")
                .define('#', forgeTag("ingots/any_plastic"))
                .define('A', forgeTag("plates/steel"))
                .define('B', forgeTag("circuits/basic"))
                .unlockedBy("has_basic_circuit", has(forgeTag("circuits/basic")))
                .save(consumer, id("tools/designator"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.DESIGNATOR_RANGE.get())
                .requires(ModItems.RANGEFINDER.get())
                .requires(ModItems.DESIGNATOR.get())
                .requires(forgeTag("ingots/any_plastic"))
                .unlockedBy("has_designator", has(ModItems.DESIGNATOR.get()))
                .save(consumer, id("tools/designator_range"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.DESIGNATOR_MANUAL.get())
                .pattern("  A")
                .pattern("#C#")
                .pattern("#B#")
                .define('#', forgeTag("ingots/any_plastic"))
                .define('A', forgeTag("plates/lead"))
                .define('B', forgeTag("circuits/advanced"))
                .define('C', ModItems.DESIGNATOR.get())
                .unlockedBy("has_designator", has(ModItems.DESIGNATOR.get()))
                .save(consumer, id("tools/designator_manual"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.DESIGNATOR_ARTY_RANGE.get())
                .requires(ModItems.RANGEFINDER.get())
                .requires(forgeTag("circuits/advanced"))
                .requires(forgeTag("ingots/any_plastic"))
                .unlockedBy("has_rangefinder", has(ModItems.RANGEFINDER.get()))
                .save(consumer, id("tools/designator_arty_range"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.LINKER.get())
                .pattern("I I")
                .pattern("ICI")
                .pattern("GGG")
                .define('I', forgeTag("plates/iron"))
                .define('C', forgeTag("circuits/advanced"))
                .define('G', forgeTag("plates/gold"))
                .unlockedBy("has_advanced_circuit", has(forgeTag("circuits/advanced")))
                .save(consumer, id("tools/linker"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.TURRET_CHIP.get())
                .pattern("WWW")
                .pattern("CPC")
                .pattern("WWW")
                .define('W', forgeTag("wires/gold"))
                .define('C', forgeTag("circuits/advanced"))
                .define('P', forgeTag("ingots/any_plastic"))
                .unlockedBy("has_advanced_circuit", has(forgeTag("circuits/advanced")))
                .save(consumer, id("tools/turret_chip"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.POLLUTION_DETECTOR.get())
                .pattern("SFS")
                .pattern("SCS")
                .pattern(" S ")
                .define('S', forgeTag("plates/steel"))
                .define('F', item("filter_coal"))
                .define('C', forgeTag("circuits/vacuum_tube"))
                .unlockedBy("has_filter_coal", has(item("filter_coal")))
                .save(consumer, id("tools/pollution_detector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.DEFUSER.get())
                .pattern(" PS")
                .pattern("P P")
                .pattern(" P ")
                .define('P', forgeTag("ingots/any_plastic"))
                .define('S', ModItems.STEEL_PLATE.get())
                .unlockedBy("has_any_plastic", has(forgeTag("ingots/any_plastic")))
                .save(consumer, id("tools/defuser"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SCREWDRIVER.get())
                .pattern("  I")
                .pattern(" I ")
                .pattern("S  ")
                .define('S', ModItems.STEEL_INGOT.get())
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_steel_ingot", has(ModItems.STEEL_INGOT.get()))
                .save(consumer, id("tools/screwdriver"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SCREWDRIVER_DESH.get())
                .pattern("  I")
                .pattern(" I ")
                .pattern("S  ")
                .define('S', forgeTag("ingots/any_plastic"))
                .define('I', forgeTag("ingots/desh"))
                .unlockedBy("has_desh_ingot", has(forgeTag("ingots/desh")))
                .save(consumer, id("tools/screwdriver_desh"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.HAND_DRILL.get())
                .pattern(" D")
                .pattern("S ")
                .pattern(" S")
                .define('D', item("ingot_dura_steel"))
                .define('S', Items.STICK)
                .unlockedBy("has_dura_steel", has(item("ingot_dura_steel")))
                .save(consumer, id("tools/hand_drill"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.HAND_DRILL_DESH.get())
                .pattern(" D")
                .pattern("S ")
                .pattern(" S")
                .define('D', forgeTag("ingots/desh"))
                .define('S', forgeTag("ingots/any_plastic"))
                .unlockedBy("has_desh_ingot", has(forgeTag("ingots/desh")))
                .save(consumer, id("tools/hand_drill_desh"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.BLOWTORCH.get())
                .pattern("CC ")
                .pattern(" I ")
                .pattern("CCC")
                .define('C', forgeTag("plates/copper"))
                .define('I', forgeTag("ingots/iron"))
                .unlockedBy("has_copper_plate", has(forgeTag("plates/copper")))
                .save(consumer, id("tools/blowtorch"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.ACETYLENE_TORCH.get())
                .pattern("SS ")
                .pattern(" PS")
                .pattern(" T ")
                .define('S', forgeTag("plates/steel"))
                .define('P', forgeTag("ingots/any_plastic"))
                .define('T', item("tank_steel"))
                .unlockedBy("has_tank_steel", has(item("tank_steel")))
                .save(consumer, id("tools/acetylene_torch"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.BOLTGUN.get())
                .pattern("DPS")
                .pattern(" RD")
                .pattern(" D ")
                .define('D', forgeTag("ingots/dura_steel"))
                .define('P', item("part_generic_piston_pneumatic"))
                .define('S', forgeTag("shells/steel"))
                .define('R', forgeTag("ingots/rubber"))
                .unlockedBy("has_pneumatic_piston", has(item("part_generic_piston_pneumatic")))
                .save(consumer, id("tools/boltgun"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.MIRROR_TOOL.get())
                .pattern(" A ")
                .pattern(" IA")
                .pattern("I  ")
                .define('A', forgeTag("ingots/aluminium"))
                .define('I', forgeTag("ingots/iron"))
                .unlockedBy("has_aluminium_ingot", has(forgeTag("ingots/aluminium")))
                .save(consumer, id("tools/mirror_tool"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.POWER_NET_TOOL.get())
                .pattern("WRW")
                .pattern(" I ")
                .pattern(" B ")
                .define('W', forgeTag("wires/mingrade"))
                .define('R', Items.REDSTONE)
                .define('I', forgeTag("ingots/iron"))
                .define('B', ModItems.BATTERY_LEAD.get())
                .unlockedBy("has_mingrade_wire", has(forgeTag("wires/mingrade")))
                .save(consumer, id("tools/power_net_tool"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SETTINGS_TOOL.get())
                .pattern(" P ")
                .pattern("PCP")
                .pattern("III")
                .define('P', forgeTag("plates/iron"))
                .define('C', forgeTag("circuits/analog"))
                .define('I', item("plate_polymer"))
                .unlockedBy("has_analog_circuit", has(forgeTag("circuits/analog")))
                .save(consumer, id("tools/settings_tool"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.ANALYSIS_TOOL.get())
                .pattern("  G")
                .pattern(" S ")
                .pattern("S  ")
                .define('G', forgeTag("glass_panes"))
                .define('S', forgeTag("ingots/steel"))
                .unlockedBy("has_steel_ingot", has(forgeTag("ingots/steel")))
                .save(consumer, id("tools/analysis_tool"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.TOOLBOX.get())
                .pattern("CCC")
                .pattern("CIC")
                .define('C', forgeTag("plates/copper"))
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_copper_plate", has(forgeTag("plates/copper")))
                .save(consumer, id("tools/toolbox"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.CHEMISTRY_SET.get())
                .pattern("GIG")
                .pattern("GCG")
                .define('G', forgeTag("glass"))
                .define('I', forgeTag("ingots/iron"))
                .define('C', forgeTag("ingots/copper"))
                .unlockedBy("has_glass", has(forgeTag("glass")))
                .save(consumer, id("tools/chemistry_set"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.CHEMISTRY_SET_BORON.get())
                .pattern("GIG")
                .pattern("GCG")
                .define('G', ModBlocks.GLASS_BORON.get())
                .define('I', forgeTag("ingots/steel"))
                .define('C', forgeTag("ingots/cobalt"))
                .unlockedBy("has_boron_glass", has(ModBlocks.GLASS_BORON.get()))
                .save(consumer, id("tools/chemistry_set_boron"));
    }

    private static void legacyChainsawRecipe(Consumer<FinishedRecipe> consumer) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", id("shaped_nbt").toString());
        recipe.addProperty("category", "tools");

        JsonArray pattern = new JsonArray();
        pattern.add("CCH");
        pattern.add("BBP");
        pattern.add("CCE");
        recipe.add("pattern", pattern);

        JsonObject key = new JsonObject();
        key.add("H", ingredientTag(forgeTag("shells/steel")));
        key.add("B", ingredientItem(ModItems.SHREDDER_BLADES_STEEL.get()));
        key.add("P", ingredientItem(ModItems.PISTON_SELENIUM.get()));
        key.add("C", ingredientItem(ModBlocks.CHAIN.get()));
        key.add("E", ingredientItem(ModItems.CANISTER_EMPTY.get()));
        recipe.add("key", key);

        JsonObject result = new JsonObject();
        result.addProperty("item", HbmRegistryUtil.itemKey(ModItems.CHAINSAW.get()).toString());
        result.addProperty("count", 1);
        result.addProperty("nbt", "{fuel:0}");
        recipe.add("result", result);
        recipe.addProperty("show_notification", true);

        consumer.accept(finishedCompatRecipe(id("tools/chainsaw"), recipe));
    }

    private static void legacyElectricToolRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ELEC_SWORD.get())
                .pattern("RPR")
                .pattern("RPR")
                .pattern(" B ")
                .define('R', forgeTag("bolts/dura_steel"))
                .define('P', forgeTag("ingots/any_plastic"))
                .define('B', ModItems.BATTERY_LEAD.get())
                .unlockedBy("has_lead_battery", has(ModItems.BATTERY_LEAD.get()))
                .save(consumer, id("tools/elec_sword"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.ELEC_PICKAXE.get())
                .pattern("RDM")
                .pattern(" PB")
                .pattern(" P ")
                .define('R', forgeTag("bolts/dura_steel"))
                .define('D', forgeTag("ingots/dura_steel"))
                .define('M', ModItems.MOTOR.get())
                .define('P', forgeTag("ingots/any_plastic"))
                .define('B', ModItems.BATTERY_LEAD.get())
                .unlockedBy("has_motor", has(ModItems.MOTOR.get()))
                .save(consumer, id("tools/elec_pickaxe"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.ELEC_AXE.get())
                .pattern(" DP")
                .pattern("RRM")
                .pattern(" PB")
                .define('D', forgeTag("ingots/dura_steel"))
                .define('P', forgeTag("ingots/any_plastic"))
                .define('R', forgeTag("bolts/dura_steel"))
                .define('M', ModItems.MOTOR.get())
                .define('B', ModItems.BATTERY_LEAD.get())
                .unlockedBy("has_motor", has(ModItems.MOTOR.get()))
                .save(consumer, id("tools/elec_axe"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.ELEC_SHOVEL.get())
                .pattern("  P")
                .pattern("RRM")
                .pattern("  B")
                .define('P', forgeTag("ingots/any_plastic"))
                .define('R', forgeTag("bolts/dura_steel"))
                .define('M', ModItems.MOTOR.get())
                .define('B', ModItems.BATTERY_LEAD.get())
                .unlockedBy("has_motor", has(ModItems.MOTOR.get()))
                .save(consumer, id("tools/elec_shovel"));
    }

    private static void legacyNonLbsmAdvancedToolRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.STARMETAL_SWORD.get())
                .pattern(" I ")
                .pattern(" B ")
                .pattern("ISI")
                .define('I', forgeTag("ingots/starmetal"))
                .define('S', item("ring_starmetal"))
                .define('B', ModItems.COBALT_DECORATED_SWORD.get())
                .unlockedBy("has_starmetal_ring", has(item("ring_starmetal")))
                .save(consumer, id("tools/starmetal_sword"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.STARMETAL_PICKAXE.get())
                .pattern("ISI")
                .pattern(" B ")
                .pattern(" I ")
                .define('I', forgeTag("ingots/starmetal"))
                .define('S', item("ring_starmetal"))
                .define('B', ModItems.COBALT_DECORATED_PICKAXE.get())
                .unlockedBy("has_starmetal_ring", has(item("ring_starmetal")))
                .save(consumer, id("tools/starmetal_pickaxe"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.STARMETAL_AXE.get())
                .pattern("IS")
                .pattern("IB")
                .pattern(" I")
                .define('I', forgeTag("ingots/starmetal"))
                .define('S', item("ring_starmetal"))
                .define('B', ModItems.COBALT_DECORATED_AXE.get())
                .unlockedBy("has_starmetal_ring", has(item("ring_starmetal")))
                .save(consumer, id("tools/starmetal_axe"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.STARMETAL_SHOVEL.get())
                .pattern("I")
                .pattern("B")
                .pattern("I")
                .define('I', forgeTag("ingots/starmetal"))
                .define('B', ModItems.COBALT_DECORATED_SHOVEL.get())
                .unlockedBy("has_starmetal_ingot", has(forgeTag("ingots/starmetal")))
                .save(consumer, id("tools/starmetal_shovel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.STARMETAL_HOE.get())
                .pattern("IS")
                .pattern(" B")
                .pattern(" I")
                .define('I', forgeTag("ingots/starmetal"))
                .define('S', item("ring_starmetal"))
                .define('B', ModItems.COBALT_DECORATED_HOE.get())
                .unlockedBy("has_starmetal_ring", has(item("ring_starmetal")))
                .save(consumer, id("tools/starmetal_hoe"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SCHRABIDIUM_SWORD.get())
                .pattern("I")
                .pattern("W")
                .pattern("S")
                .define('I', forgeTag("storage_blocks/schrabidium"))
                .define('W', ModItems.DESH_SWORD.get())
                .define('S', forgeTag("ingots/any_plastic"))
                .unlockedBy("has_schrabidium_block", has(forgeTag("storage_blocks/schrabidium")))
                .save(consumer, id("tools/schrabidium_sword"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SCHRABIDIUM_PICKAXE.get())
                .pattern("BSB")
                .pattern(" W ")
                .pattern(" P ")
                .define('B', ModItems.SHREDDER_BLADES_DESH.get())
                .define('S', forgeTag("storage_blocks/schrabidium"))
                .define('W', ModItems.DESH_PICKAXE.get())
                .define('P', forgeTag("ingots/any_plastic"))
                .unlockedBy("has_schrabidium_block", has(forgeTag("storage_blocks/schrabidium")))
                .save(consumer, id("tools/schrabidium_pickaxe"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SCHRABIDIUM_AXE.get())
                .pattern("BS")
                .pattern("BW")
                .pattern(" P")
                .define('B', ModItems.SHREDDER_BLADES_DESH.get())
                .define('S', forgeTag("storage_blocks/schrabidium"))
                .define('W', ModItems.DESH_AXE.get())
                .define('P', forgeTag("ingots/any_plastic"))
                .unlockedBy("has_schrabidium_block", has(forgeTag("storage_blocks/schrabidium")))
                .save(consumer, id("tools/schrabidium_axe"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SCHRABIDIUM_SHOVEL.get())
                .pattern("B")
                .pattern("W")
                .pattern("P")
                .define('B', forgeTag("storage_blocks/schrabidium"))
                .define('W', ModItems.DESH_SHOVEL.get())
                .define('P', forgeTag("ingots/any_plastic"))
                .unlockedBy("has_schrabidium_block", has(forgeTag("storage_blocks/schrabidium")))
                .save(consumer, id("tools/schrabidium_shovel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SCHRABIDIUM_HOE.get())
                .pattern("IW")
                .pattern(" S")
                .pattern(" S")
                .define('I', forgeTag("ingots/schrabidium"))
                .define('W', ModItems.DESH_HOE.get())
                .define('S', forgeTag("ingots/any_plastic"))
                .unlockedBy("has_schrabidium_ingot", has(forgeTag("ingots/schrabidium")))
                .save(consumer, id("tools/schrabidium_hoe"));
    }

    private static void standardToolSet(Consumer<FinishedRecipe> consumer, String prefix, TagKey<Item> ingot,
            ItemLike sword, ItemLike pickaxe, ItemLike axe, ItemLike shovel, ItemLike hoe) {
        standardToolRecipe(consumer, prefix + "_sword", sword, ingot, "X", "X", "#");
        standardToolRecipe(consumer, prefix + "_pickaxe", pickaxe, ingot, "XXX", " # ", " # ");
        standardToolRecipe(consumer, prefix + "_axe", axe, ingot, "XX", "X#", " #");
        standardToolRecipe(consumer, prefix + "_shovel", shovel, ingot, "X", "#", "#");
        standardToolRecipe(consumer, prefix + "_hoe", hoe, ingot, "XX", " #", " #");
    }

    private static void standardToolRecipe(Consumer<FinishedRecipe> consumer, String name, ItemLike output,
            TagKey<Item> ingot, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, output);
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.define('X', ingot)
                .define('#', Items.STICK)
                .unlockedBy("has_" + name.substring(0, name.indexOf('_')) + "_ingot", has(ingot))
                .save(consumer, id("tools/" + name));
    }

    private static void meteorUpgradeToolRecipe(Consumer<FinishedRecipe> consumer, String name, ItemLike output,
            ItemLike material, ItemLike baseTool) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, output)
                .pattern(" BM")
                .pattern("BPB")
                .pattern("TB ")
                .define('B', material)
                .define('M', ModItems.INGOT_METEORITE.get())
                .define('P', baseTool)
                .define('T', forgeTag("bolts/tungsten"))
                .unlockedBy("has_meteorite_ingot", has(ModItems.INGOT_METEORITE.get()))
                .save(consumer, id("tools/" + name));
    }

    private static void chlorophyteToolRecipe(Consumer<FinishedRecipe> consumer, String name, ItemLike output,
            ItemLike baseTool) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, output)
                .pattern(" SD")
                .pattern("APS")
                .pattern("FA ")
                .define('S', ModItems.SHREDDER_BLADES_STEEL.get())
                .define('D', item("powder_chlorophyte"))
                .define('A', forgeTag("ingots/fiberglass"))
                .define('P', baseTool)
                .define('F', forgeTag("bolts/dura_steel"))
                .unlockedBy("has_chlorophyte_powder", has(item("powder_chlorophyte")))
                .save(consumer, id("tools/" + name));
    }

    private static void meseToolRecipe(Consumer<FinishedRecipe> consumer, String name, ItemLike output,
            ItemLike baseTool) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, output)
                .pattern(" SD")
                .pattern("APS")
                .pattern("FA ")
                .define('S', ModItems.SHREDDER_BLADES_DESH.get())
                .define('D', item("powder_dineutronium"))
                .define('A', item("plate_paa"))
                .define('P', baseTool)
                .define('F', item("shimmer_handle"))
                .unlockedBy("has_shimmer_handle", has(item("shimmer_handle")))
                .save(consumer, id("tools/" + name));
    }

    private static void legacyHazmatRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike hazmatCloth = item("hazmat_cloth");
        ItemLike redHazmatCloth = item("hazmat_cloth_red");
        ItemLike greyHazmatCloth = item("hazmat_cloth_grey");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, redHazmatCloth)
                .pattern("C")
                .pattern("R")
                .pattern("C")
                .define('C', hazmatCloth)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_hazmat_cloth", has(hazmatCloth))
                .save(consumer, id("parts/hazmat_cloth_red"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, greyHazmatCloth)
                .pattern(" P ")
                .pattern("ICI")
                .pattern(" L ")
                .define('C', redHazmatCloth)
                .define('P', forgeTag("plates/iron"))
                .define('L', forgeTag("plates/lead"))
                .define('I', forgeTag("ingots/any_rubber"))
                .unlockedBy("has_hazmat_cloth_red", has(redHazmatCloth))
                .save(consumer, id("parts/hazmat_cloth_grey"));

        hazmatArmorSetRecipes(consumer, "hazmat", hazmatCloth,
                ModItems.HAZMAT_HELMET.get(), ModItems.HAZMAT_PLATE.get(),
                ModItems.HAZMAT_LEGS.get(), ModItems.HAZMAT_BOOTS.get(), false);
        hazmatArmorSetRecipes(consumer, "hazmat_red", redHazmatCloth,
                ModItems.HAZMAT_HELMET_RED.get(), ModItems.HAZMAT_PLATE_RED.get(),
                ModItems.HAZMAT_LEGS_RED.get(), ModItems.HAZMAT_BOOTS_RED.get(), true);
        hazmatArmorSetRecipes(consumer, "hazmat_grey", greyHazmatCloth,
                ModItems.HAZMAT_HELMET_GREY.get(), ModItems.HAZMAT_PLATE_GREY.get(),
                ModItems.HAZMAT_LEGS_GREY.get(), ModItems.HAZMAT_BOOTS_GREY.get(), true);
    }

    private static void hazmatArmorSetRecipes(Consumer<FinishedRecipe> consumer, String recipePrefix,
            ItemLike cloth, ItemLike helmet, ItemLike chestplate, ItemLike leggings, ItemLike boots,
            boolean reinforcedHelmet) {
        ShapedRecipeBuilder helmetRecipe = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, helmet)
                .pattern("EEE")
                .pattern(reinforcedHelmet ? "IEI" : "EIE")
                .define('E', cloth)
                .define('I', forgeTag("glass_panes"))
                .unlockedBy("has_" + recipePrefix + "_cloth", has(cloth));
        if (reinforcedHelmet) {
            helmetRecipe.pattern("EFE")
                    .define('F', forgeTag("plates/iron"));
        } else {
            helmetRecipe.pattern(" P ")
                    .define('P', forgeTag("plates/iron"));
        }
        helmetRecipe.save(consumer, id("armor/" + recipePrefix + "_helmet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, chestplate)
                .pattern("E E")
                .pattern("EEE")
                .pattern("EEE")
                .define('E', cloth)
                .unlockedBy("has_" + recipePrefix + "_cloth", has(cloth))
                .save(consumer, id("armor/" + recipePrefix + "_plate"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, leggings)
                .pattern("EEE")
                .pattern("E E")
                .pattern("E E")
                .define('E', cloth)
                .unlockedBy("has_" + recipePrefix + "_cloth", has(cloth))
                .save(consumer, id("armor/" + recipePrefix + "_legs"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, boots)
                .pattern("E E")
                .pattern("E E")
                .define('E', cloth)
                .unlockedBy("has_" + recipePrefix + "_cloth", has(cloth))
                .save(consumer, id("armor/" + recipePrefix + "_boots"));
    }

    private static void legacyArmorRecipes(Consumer<FinishedRecipe> consumer) {
        standardArmorSet(consumer, "steel", forgeTag("ingots/steel"),
                ModItems.STEEL_HELMET.get(), ModItems.STEEL_CHESTPLATE.get(),
                ModItems.STEEL_LEGS.get(), ModItems.STEEL_BOOTS.get());
        standardArmorSet(consumer, "titanium", forgeTag("ingots/titanium"),
                ModItems.TITANIUM_HELMET.get(), ModItems.TITANIUM_CHESTPLATE.get(),
                ModItems.TITANIUM_LEGS.get(), ModItems.TITANIUM_BOOTS.get());
        standardArmorSet(consumer, "cmb", forgeTag("ingots/combine_steel"),
                ModItems.CMB_HELMET.get(), ModItems.CMB_PLATE.get(),
                ModItems.CMB_LEGS.get(), ModItems.CMB_BOOTS.get());

        cobaltArmorRecipes(consumer);
        securityArmorRecipes(consumer);
        asbestosArmorRecipes(consumer);
        hazmatPaaArmorRecipes(consumer);
        paaArmorRecipes(consumer);
        liquidatorArmorRecipes(consumer);
        maskRecipes(consumer);
        starmetalArmorRecipes(consumer);
        robesArmorRecipes(consumer);
        zirconiumAndDntArmorRecipes(consumer);
        schrabidiumArmorRecipes(consumer);
        euphemiumArmorRecipes(consumer);
        bismuthArmorRecipes(consumer);
        poweredArmorRecipes(consumer);
    }

    private static void cobaltArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike cobaltBillet = item("billet_cobalt");
        upgradeArmorPiece(consumer, "cobalt_helmet", ModItems.COBALT_HELMET.get(),
                "ECE",
                'E', cobaltBillet,
                'C', ModItems.STEEL_HELMET.get());
        upgradeArmorPiece(consumer, "cobalt_plate", ModItems.COBALT_PLATE.get(),
                " E ", "ECE", " E ",
                'E', cobaltBillet,
                'C', ModItems.STEEL_CHESTPLATE.get());
        upgradeArmorPiece(consumer, "cobalt_legs", ModItems.COBALT_LEGS.get(),
                "ECE", "E E",
                'E', cobaltBillet,
                'C', ModItems.STEEL_LEGS.get());
        upgradeArmorPiece(consumer, "cobalt_boots", ModItems.COBALT_BOOTS.get(),
                "ECE",
                'E', cobaltBillet,
                'C', ModItems.STEEL_BOOTS.get());
    }

    private static void securityArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SECURITY_HELMET.get())
                .pattern("SSS")
                .pattern("IGI")
                .define('S', forgeTag("plates/steel"))
                .define('I', forgeTag("ingots/any_rubber"))
                .define('G', forgeTag("glass_panes"))
                .unlockedBy("has_steel_plate", has(forgeTag("plates/steel")))
                .save(consumer, id("armor/security_helmet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SECURITY_PLATE.get())
                .pattern("KWK")
                .pattern("IKI")
                .pattern("WKW")
                .define('K', item("plate_kevlar"))
                .define('I', forgeTag("ingots/any_plastic"))
                .define('W', ItemTags.WOOL)
                .unlockedBy("has_plate_kevlar", has(item("plate_kevlar")))
                .save(consumer, id("armor/security_plate"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SECURITY_LEGS.get())
                .pattern("IWI")
                .pattern("K K")
                .pattern("W W")
                .define('K', item("plate_kevlar"))
                .define('I', forgeTag("ingots/any_plastic"))
                .define('W', ItemTags.WOOL)
                .unlockedBy("has_plate_kevlar", has(item("plate_kevlar")))
                .save(consumer, id("armor/security_legs"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SECURITY_BOOTS.get())
                .pattern("P P")
                .pattern("I I")
                .define('P', forgeTag("plates/steel"))
                .define('I', forgeTag("ingots/any_rubber"))
                .unlockedBy("has_steel_plate", has(forgeTag("plates/steel")))
                .save(consumer, id("armor/security_boots"));
    }

    private static void asbestosArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike asbestosCloth = item("asbestos_cloth");
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ASBESTOS_HELMET.get())
                .pattern("EEE")
                .pattern("EIE")
                .define('E', asbestosCloth)
                .define('I', forgeTag("plates/gold"))
                .unlockedBy("has_asbestos_cloth", has(asbestosCloth))
                .save(consumer, id("armor/asbestos_helmet"));
        armorPiece(consumer, "asbestos_plate", ModItems.ASBESTOS_PLATE.get(), asbestosCloth, "E E", "EEE", "EEE");
        armorPiece(consumer, "asbestos_legs", ModItems.ASBESTOS_LEGS.get(), asbestosCloth, "EEE", "E E", "E E");
        armorPiece(consumer, "asbestos_boots", ModItems.ASBESTOS_BOOTS.get(), asbestosCloth, "E E", "E E");
    }

    private static void hazmatPaaArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike platePaa = item("plate_paa");
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HAZMAT_PAA_HELMET.get())
                .pattern("EEE")
                .pattern("IEI")
                .pattern(" P ")
                .define('E', platePaa)
                .define('I', forgeTag("glass_panes"))
                .define('P', forgeTag("plates/iron"))
                .unlockedBy("has_plate_paa", has(platePaa))
                .save(consumer, id("armor/hazmat_paa_helmet"));
        armorPiece(consumer, "hazmat_paa_plate", ModItems.HAZMAT_PAA_PLATE.get(), platePaa, "E E", "EEE", "EEE");
        armorPiece(consumer, "hazmat_paa_legs", ModItems.HAZMAT_PAA_LEGS.get(), platePaa, "EEE", "E E", "E E");
        armorPiece(consumer, "hazmat_paa_boots", ModItems.HAZMAT_PAA_BOOTS.get(), platePaa, "E E", "E E");
    }

    private static void paaArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike platePaa = item("plate_paa");
        ItemLike neutronReflector = item("neutron_reflector");
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.PAA_PLATE.get())
                .pattern("E E")
                .pattern("NEN")
                .pattern("ENE")
                .define('E', platePaa)
                .define('N', neutronReflector)
                .unlockedBy("has_plate_paa", has(platePaa))
                .save(consumer, id("armor/paa_plate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.PAA_LEGS.get())
                .pattern("EEE")
                .pattern("N N")
                .pattern("E E")
                .define('E', platePaa)
                .define('N', neutronReflector)
                .unlockedBy("has_plate_paa", has(platePaa))
                .save(consumer, id("armor/paa_legs"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.PAA_BOOTS.get())
                .pattern("E E")
                .pattern("N N")
                .define('E', platePaa)
                .define('N', neutronReflector)
                .unlockedBy("has_plate_paa", has(platePaa))
                .save(consumer, id("armor/paa_boots"));
    }

    private static void liquidatorArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LIQUIDATOR_HELMET.get())
                .pattern("III")
                .pattern("CBC")
                .pattern("III")
                .define('I', forgeTag("ingots/any_rubber"))
                .define('C', ModItems.CLADDING_LEAD.get())
                .define('B', ModItems.HAZMAT_HELMET_GREY.get())
                .unlockedBy("has_grey_hazmat_helmet", has(ModItems.HAZMAT_HELMET_GREY.get()))
                .save(consumer, id("armor/liquidator_helmet"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LIQUIDATOR_PLATE.get())
                .pattern("ICI")
                .pattern("TBT")
                .pattern("ICI")
                .define('I', forgeTag("ingots/any_rubber"))
                .define('C', ModItems.CLADDING_LEAD.get())
                .define('B', ModItems.HAZMAT_PLATE_GREY.get())
                .define('T', ModItems.GAS_EMPTY.get())
                .unlockedBy("has_grey_hazmat_plate", has(ModItems.HAZMAT_PLATE_GREY.get()))
                .save(consumer, id("armor/liquidator_plate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LIQUIDATOR_LEGS.get())
                .pattern("III")
                .pattern("CBC")
                .pattern("I I")
                .define('I', forgeTag("ingots/any_rubber"))
                .define('C', ModItems.CLADDING_LEAD.get())
                .define('B', ModItems.HAZMAT_LEGS_GREY.get())
                .unlockedBy("has_grey_hazmat_legs", has(ModItems.HAZMAT_LEGS_GREY.get()))
                .save(consumer, id("armor/liquidator_legs"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LIQUIDATOR_BOOTS.get())
                .pattern("ICI")
                .pattern("IBI")
                .define('I', forgeTag("ingots/any_rubber"))
                .define('C', ModItems.CLADDING_LEAD.get())
                .define('B', ModItems.HAZMAT_BOOTS_GREY.get())
                .unlockedBy("has_grey_hazmat_boots", has(ModItems.HAZMAT_BOOTS_GREY.get()))
                .save(consumer, id("armor/liquidator_boots"));
    }

    private static void maskRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.GOGGLES.get())
                .pattern("P P")
                .pattern("GPG")
                .define('G', forgeTag("glass_panes"))
                .define('P', forgeTag("plates/steel"))
                .unlockedBy("has_steel_plate", has(forgeTag("plates/steel")))
                .save(consumer, id("armor/goggles"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.GAS_MASK.get())
                .pattern("PPP")
                .pattern("GPG")
                .pattern(" F ")
                .define('G', forgeTag("glass_panes"))
                .define('P', forgeTag("plates/steel"))
                .define('F', forgeTag("plates/iron"))
                .unlockedBy("has_steel_plate", has(forgeTag("plates/steel")))
                .save(consumer, id("armor/gas_mask"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.GAS_MASK_M65.get())
                .pattern("PPP")
                .pattern("GPG")
                .pattern(" F ")
                .define('G', forgeTag("glass_panes"))
                .define('P', forgeTag("ingots/any_rubber"))
                .define('F', forgeTag("plates/iron"))
                .unlockedBy("has_any_rubber", has(forgeTag("ingots/any_rubber")))
                .save(consumer, id("armor/gas_mask_m65"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.GAS_MASK_OLDE.get())
                .pattern("PPP")
                .pattern("GPG")
                .pattern(" F ")
                .define('G', forgeTag("glass_panes"))
                .define('P', Items.LEATHER)
                .define('F', Items.IRON_INGOT)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(consumer, id("armor/gas_mask_olde"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.GAS_MASK_MONO.get())
                .pattern(" P ")
                .pattern("PPP")
                .pattern(" F ")
                .define('P', forgeTag("ingots/any_rubber"))
                .define('F', forgeTag("plates/iron"))
                .unlockedBy("has_any_rubber", has(forgeTag("ingots/any_rubber")))
                .save(consumer, id("armor/gas_mask_mono"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MASK_OF_INFAMY.get())
                .pattern("III")
                .pattern("III")
                .pattern(" I ")
                .define('I', forgeTag("plates/iron"))
                .unlockedBy("has_iron_plate", has(forgeTag("plates/iron")))
                .save(consumer, id("armor/mask_of_infamy"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ASHGLASSES.get())
                .pattern("I I")
                .pattern("GPG")
                .define('I', forgeTag("ingots/any_rubber"))
                .define('G', ModBlocks.GLASS_ASH.get())
                .define('P', forgeTag("ingots/any_plastic"))
                .unlockedBy("has_ash_glass", has(ModBlocks.GLASS_ASH.get()))
                .save(consumer, id("armor/ashglasses"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MASK_RAG.get())
                .pattern("RRR")
                .define('R', item("rag_damp"))
                .unlockedBy("has_damp_rag", has(item("rag_damp")))
                .save(consumer, id("armor/mask_rag"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MASK_PISS.get())
                .pattern("RRR")
                .define('R', item("rag_piss"))
                .unlockedBy("has_piss_rag", has(item("rag_piss")))
                .save(consumer, id("armor/mask_piss"));
    }

    private static void starmetalArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike starmetal = item("ingot_starmetal");
        upgradeArmorPiece(consumer, "starmetal_helmet", ModItems.STARMETAL_HELMET.get(),
                "EEE", "ECE",
                'E', starmetal,
                'C', ModItems.COBALT_HELMET.get());
        upgradeArmorPiece(consumer, "starmetal_plate", ModItems.STARMETAL_PLATE.get(),
                "ECE", "EEE", "EEE",
                'E', starmetal,
                'C', ModItems.COBALT_PLATE.get());
        upgradeArmorPiece(consumer, "starmetal_legs", ModItems.STARMETAL_LEGS.get(),
                "EEE", "ECE", "E E",
                'E', starmetal,
                'C', ModItems.COBALT_LEGS.get());
        upgradeArmorPiece(consumer, "starmetal_boots", ModItems.STARMETAL_BOOTS.get(),
                "E E", "ECE",
                'E', starmetal,
                'C', ModItems.COBALT_BOOTS.get());
    }

    private static void robesArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike rag = item("rag");
        armorPiece(consumer, "robes_helmet", ModItems.ROBES_HELMET.get(), rag, "EEE", "E E");
        armorPiece(consumer, "robes_plate", ModItems.ROBES_PLATE.get(), rag, "E E", "EEE", "EEE");
        armorPiece(consumer, "robes_legs", ModItems.ROBES_LEGS.get(), rag, "EEE", "E E", "E E");
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ROBES_BOOTS.get())
                .pattern("R R")
                .pattern("P P")
                .define('R', rag)
                .define('P', forgeTag("ingots/any_rubber"))
                .unlockedBy("has_rag", has(rag))
                .save(consumer, id("armor/robes_boots"));
    }

    private static void zirconiumAndDntArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike zirconium = item("ingot_zirconium");
        ItemLike dineutronium = item("ingot_dineutronium");

        armorPiece(consumer, "zirconium_legs", ModItems.ZIRCONIUM_LEGS.get(),
                zirconium, "EEE", "E E", "E E");
        armorPiece(consumer, "dnt_helmet", ModItems.DNT_HELMET.get(), dineutronium, "EEE", "EE ");
        armorPiece(consumer, "dnt_plate", ModItems.DNT_PLATE.get(), dineutronium, "EE ", "EEE", "EEE");
        armorPiece(consumer, "dnt_legs", ModItems.DNT_LEGS.get(), dineutronium, "EE ", "EEE", "E E");
        armorPiece(consumer, "dnt_boots", ModItems.DNT_BOOTS.get(), dineutronium, "  E", "E  ", "E E");
    }

    private static void schrabidiumArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike schrabidium = item("ingot_schrabidium");
        ItemLike chargedPellet = item("pellet_charged");
        upgradeArmorPiece(consumer, "schrabidium_helmet", ModItems.SCHRABIDIUM_HELMET.get(),
                "EEE", "ESE", " P ",
                'E', schrabidium,
                'S', ModItems.STARMETAL_HELMET.get(),
                'P', chargedPellet);
        upgradeArmorPiece(consumer, "schrabidium_plate", ModItems.SCHRABIDIUM_PLATE.get(),
                "ESE", "EPE", "EEE",
                'E', schrabidium,
                'S', ModItems.STARMETAL_PLATE.get(),
                'P', chargedPellet);
        upgradeArmorPiece(consumer, "schrabidium_legs", ModItems.SCHRABIDIUM_LEGS.get(),
                "EEE", "ESE", "EPE",
                'E', schrabidium,
                'S', ModItems.STARMETAL_LEGS.get(),
                'P', chargedPellet);
        upgradeArmorPiece(consumer, "schrabidium_boots", ModItems.SCHRABIDIUM_BOOTS.get(),
                "EPE", "ESE",
                'E', schrabidium,
                'S', ModItems.STARMETAL_BOOTS.get(),
                'P', chargedPellet);
    }

    private static void euphemiumArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike plate = item("plate_euphemium");
        armorPiece(consumer, "euphemium_helmet", ModItems.EUPHEMIUM_HELMET.get(), plate, "EEE", "E E");
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.EUPHEMIUM_PLATE.get())
                .pattern("EWE")
                .pattern("EEE")
                .pattern("EEE")
                .define('E', plate)
                .define('W', item("watch"))
                .unlockedBy("has_plate_euphemium", has(plate))
                .save(consumer, id("armor/euphemium_plate"));
        armorPiece(consumer, "euphemium_legs", ModItems.EUPHEMIUM_LEGS.get(), plate, "EEE", "E E", "E E");
        armorPiece(consumer, "euphemium_boots", ModItems.EUPHEMIUM_BOOTS.get(), plate, "E E", "E E");
    }

    private static void bismuthArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike plate = item("plate_bismuth");
        ItemLike rag = item("rag");
        ItemLike starmetalRing = item("ring_starmetal");

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BISMUTH_HELMET.get())
                .pattern("GPP")
                .pattern("P  ")
                .pattern("FPP")
                .define('G', forgeTag("ingots/gold"))
                .define('P', plate)
                .define('F', rag)
                .unlockedBy("has_bismuth_plate", has(plate))
                .save(consumer, id("armor/bismuth_helmet"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BISMUTH_PLATE.get())
                .pattern("RWR")
                .pattern("PCP")
                .pattern("SFS")
                .define('R', item("crystal_rare"))
                .define('W', forgeTag("wires/gold"))
                .define('P', plate)
                .define('C', item("laser_crystal_bismuth"))
                .define('S', starmetalRing)
                .define('F', rag)
                .unlockedBy("has_bismuth_plate", has(plate))
                .save(consumer, id("armor/bismuth_plate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BISMUTH_LEGS.get())
                .pattern("FSF")
                .pattern("   ")
                .pattern("FSF")
                .define('F', rag)
                .define('S', starmetalRing)
                .unlockedBy("has_starmetal_ring", has(starmetalRing))
                .save(consumer, id("armor/bismuth_legs"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BISMUTH_BOOTS.get())
                .pattern("W W")
                .pattern("P P")
                .define('W', forgeTag("wires/gold"))
                .define('P', plate)
                .unlockedBy("has_bismuth_plate", has(plate))
                .save(consumer, id("armor/bismuth_boots"));
    }

    private static void poweredArmorRecipes(Consumer<FinishedRecipe> consumer) {
        t51ArmorRecipes(consumer);
        ajrArmorRecipes(consumer);
        bjArmorRecipes(consumer);
        hevArmorRecipes(consumer);
        rpaArmorRecipes(consumer);
        steamsuitArmorRecipes(consumer);
        dieselSuitArmorRecipes(consumer);
        envSuitArmorRecipes(consumer);
        fauArmorRecipes(consumer);
        dnsArmorRecipes(consumer);
    }

    private static void t51ArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike plate = item("plate_armor_titanium");
        ItemLike basicCircuit = item("circuit_basic");
        ItemLike motor = item("motor");

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.T51_HELMET.get())
                .pattern("PPC")
                .pattern("PBP")
                .pattern("IXI")
                .define('P', plate)
                .define('C', basicCircuit)
                .define('B', ModItems.TITANIUM_HELMET.get())
                .define('I', forgeTag("ingots/any_rubber"))
                .define('X', ModItems.GAS_MASK_M65.get())
                .unlockedBy("has_titanium_armor_plate", has(plate))
                .save(consumer, id("armor/t51_helmet"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.T51_PLATE.get())
                .pattern("MPM")
                .pattern("TBT")
                .pattern("PPP")
                .define('M', motor)
                .define('P', plate)
                .define('T', ModItems.GAS_EMPTY.get())
                .define('B', ModItems.TITANIUM_CHESTPLATE.get())
                .unlockedBy("has_titanium_armor_plate", has(plate))
                .save(consumer, id("armor/t51_plate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.T51_LEGS.get())
                .pattern("MPM")
                .pattern("PBP")
                .pattern("P P")
                .define('M', motor)
                .define('P', plate)
                .define('B', ModItems.TITANIUM_LEGS.get())
                .unlockedBy("has_titanium_armor_plate", has(plate))
                .save(consumer, id("armor/t51_legs"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.T51_BOOTS.get())
                .pattern("P P")
                .pattern("PBP")
                .define('P', plate)
                .define('B', ModItems.TITANIUM_BOOTS.get())
                .unlockedBy("has_titanium_armor_plate", has(plate))
                .save(consumer, id("armor/t51_boots"));
    }

    private static void ajrArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike plate = item("plate_armor_ajr");
        ItemLike basicCircuit = item("circuit_basic");
        ItemLike motor = item("motor_desh");

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.AJR_HELMET.get())
                .pattern("PPC")
                .pattern("PBP")
                .pattern("IXI")
                .define('P', plate)
                .define('C', basicCircuit)
                .define('B', ModItems.TITANIUM_HELMET.get())
                .define('I', forgeTag("ingots/any_plastic"))
                .define('X', ModItems.GAS_MASK_M65.get())
                .unlockedBy("has_ajr_armor_plate", has(plate))
                .save(consumer, id("armor/ajr_helmet"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.AJR_PLATE.get())
                .pattern("MPM")
                .pattern("TBT")
                .pattern("PPP")
                .define('M', motor)
                .define('P', plate)
                .define('T', ModItems.GAS_EMPTY.get())
                .define('B', ModItems.TITANIUM_CHESTPLATE.get())
                .unlockedBy("has_ajr_armor_plate", has(plate))
                .save(consumer, id("armor/ajr_plate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.AJR_LEGS.get())
                .pattern("MPM")
                .pattern("PBP")
                .pattern("P P")
                .define('M', motor)
                .define('P', plate)
                .define('B', ModItems.TITANIUM_LEGS.get())
                .unlockedBy("has_ajr_armor_plate", has(plate))
                .save(consumer, id("armor/ajr_legs"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.AJR_BOOTS.get())
                .pattern("P P")
                .pattern("PBP")
                .define('P', plate)
                .define('B', ModItems.TITANIUM_BOOTS.get())
                .unlockedBy("has_ajr_armor_plate", has(plate))
                .save(consumer, id("armor/ajr_boots"));

        ajroRecolorRecipe(consumer, "ajro_helmet", ModItems.AJRO_HELMET.get(), ModItems.AJR_HELMET.get());
        ajroRecolorRecipe(consumer, "ajro_plate", ModItems.AJRO_PLATE.get(), ModItems.AJR_PLATE.get());
        ajroRecolorRecipe(consumer, "ajro_legs", ModItems.AJRO_LEGS.get(), ModItems.AJR_LEGS.get());
        ajroRecolorRecipe(consumer, "ajro_boots", ModItems.AJRO_BOOTS.get(), ModItems.AJR_BOOTS.get());
    }

    private static void ajroRecolorRecipe(Consumer<FinishedRecipe> consumer, String name, ItemLike result,
            ItemLike base) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, result)
                .requires(base)
                .requires(Items.RED_DYE)
                .requires(Items.BLACK_DYE)
                .unlockedBy("has_" + base.asItem(), has(base))
                .save(consumer, id("armor/" + name));
    }

    private static void bjArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike lunarPlate = item("plate_armor_lunar");
        ItemLike motor = item("motor_desh");
        ItemLike advancedCircuit = item("circuit_advanced");

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BJ_HELMET.get())
                .pattern("SBS")
                .pattern(" C ")
                .pattern(" I ")
                .define('S', Items.STRING)
                .define('B', Items.BLACK_WOOL)
                .define('C', advancedCircuit)
                .define('I', item("ingot_starmetal"))
                .unlockedBy("has_advanced_circuit", has(advancedCircuit))
                .save(consumer, id("armor/bj_helmet"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BJ_PLATE.get())
                .pattern("N N")
                .pattern("MSM")
                .pattern("NCN")
                .define('N', lunarPlate)
                .define('M', motor)
                .define('S', ModItems.STARMETAL_PLATE.get())
                .define('C', advancedCircuit)
                .unlockedBy("has_lunar_armor_plate", has(lunarPlate))
                .save(consumer, id("armor/bj_plate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BJ_PLATE_JETPACK.get())
                .pattern("NFN")
                .pattern("TPT")
                .pattern("ICI")
                .define('N', lunarPlate)
                .define('F', item("fins_quad_titanium"))
                .define('T', HbmFluidContainerIngredient.of(HbmFluids.XENON, 1_000))
                .define('P', ModItems.BJ_PLATE.get())
                .define('I', item("mp_thruster_10_xenon"))
                .define('C', item("crystal_phosphorus"))
                .unlockedBy("has_bj_plate", has(ModItems.BJ_PLATE.get()))
                .save(consumer, id("armor/bj_plate_jetpack"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BJ_LEGS.get())
                .pattern("MBM")
                .pattern("NSN")
                .pattern("N N")
                .define('N', lunarPlate)
                .define('M', motor)
                .define('S', ModItems.STARMETAL_LEGS.get())
                .define('B', block("block_starmetal"))
                .unlockedBy("has_lunar_armor_plate", has(lunarPlate))
                .save(consumer, id("armor/bj_legs"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BJ_BOOTS.get())
                .pattern("N N")
                .pattern("BSB")
                .define('N', lunarPlate)
                .define('S', ModItems.STARMETAL_BOOTS.get())
                .define('B', block("block_starmetal"))
                .unlockedBy("has_lunar_armor_plate", has(lunarPlate))
                .save(consumer, id("armor/bj_boots"));
    }

    private static void hevArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike plate = item("plate_armor_hev");
        ItemLike basicCircuit = item("circuit_basic");
        ItemLike motor = item("motor_desh");

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HEV_HELMET.get())
                .pattern("PPC")
                .pattern("PBP")
                .pattern("IFI")
                .define('P', plate)
                .define('C', basicCircuit)
                .define('B', ModItems.TITANIUM_HELMET.get())
                .define('I', forgeTag("ingots/any_plastic"))
                .define('F', ModItems.GAS_MASK_FILTER.get())
                .unlockedBy("has_hev_armor_plate", has(plate))
                .save(consumer, id("armor/hev_helmet"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HEV_PLATE.get())
                .pattern("MPM")
                .pattern("IBI")
                .pattern("PPP")
                .define('M', motor)
                .define('P', plate)
                .define('I', forgeTag("ingots/any_plastic"))
                .define('B', ModItems.TITANIUM_CHESTPLATE.get())
                .unlockedBy("has_hev_armor_plate", has(plate))
                .save(consumer, id("armor/hev_plate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HEV_LEGS.get())
                .pattern("MPM")
                .pattern("IBI")
                .pattern("P P")
                .define('M', motor)
                .define('P', plate)
                .define('I', forgeTag("ingots/any_plastic"))
                .define('B', ModItems.TITANIUM_LEGS.get())
                .unlockedBy("has_hev_armor_plate", has(plate))
                .save(consumer, id("armor/hev_legs"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HEV_BOOTS.get())
                .pattern("P P")
                .pattern("PBP")
                .define('P', plate)
                .define('B', ModItems.TITANIUM_BOOTS.get())
                .unlockedBy("has_hev_armor_plate", has(plate))
                .save(consumer, id("armor/hev_boots"));
    }

    private static void rpaArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike legendary = item("parts_legendary_tier2");
        ItemLike kevlar = item("plate_kevlar");
        ItemLike ajrPlate = item("plate_armor_ajr");
        ItemLike motor = item("motor_desh");

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RPA_HELMET.get())
                .pattern("KPK")
                .pattern("PLP")
                .pattern(" F ")
                .define('L', legendary)
                .define('K', kevlar)
                .define('P', ajrPlate)
                .define('F', ModItems.GAS_MASK_FILTER_COMBO.get())
                .unlockedBy("has_legendary_tier2_part", has(legendary))
                .save(consumer, id("armor/rpa_helmet"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RPA_PLATE.get())
                .pattern("P P")
                .pattern("MLM")
                .pattern("PKP")
                .define('L', legendary)
                .define('K', kevlar)
                .define('P', ajrPlate)
                .define('M', motor)
                .unlockedBy("has_legendary_tier2_part", has(legendary))
                .save(consumer, id("armor/rpa_plate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RPA_LEGS.get())
                .pattern("MPM")
                .pattern("KLK")
                .pattern("P P")
                .define('L', legendary)
                .define('K', kevlar)
                .define('P', ajrPlate)
                .define('M', motor)
                .unlockedBy("has_legendary_tier2_part", has(legendary))
                .save(consumer, id("armor/rpa_legs"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RPA_BOOTS.get())
                .pattern("KLK")
                .pattern("P P")
                .define('L', legendary)
                .define('K', kevlar)
                .define('P', ajrPlate)
                .unlockedBy("has_legendary_tier2_part", has(legendary))
                .save(consumer, id("armor/rpa_boots"));
    }

    private static void steamsuitArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike desh = item("ingot_desh");

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.STEAMSUIT_HELMET.get())
                .pattern("DCD")
                .pattern("CXC")
                .pattern(" F ")
                .define('D', desh)
                .define('C', forgeTag("plates/copper"))
                .define('X', ModItems.STEEL_HELMET.get())
                .define('F', ModItems.GAS_MASK_FILTER.get())
                .unlockedBy("has_desh_ingot", has(desh))
                .save(consumer, id("armor/steamsuit_helmet"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.STEAMSUIT_PLATE.get())
                .pattern("C C")
                .pattern("DXD")
                .pattern("CFC")
                .define('D', desh)
                .define('C', forgeTag("plates/copper"))
                .define('X', ModItems.STEEL_CHESTPLATE.get())
                .define('F', item("tank_steel"))
                .unlockedBy("has_desh_ingot", has(desh))
                .save(consumer, id("armor/steamsuit_plate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.STEAMSUIT_LEGS.get())
                .pattern("CCC")
                .pattern("DXD")
                .pattern("C C")
                .define('D', desh)
                .define('C', forgeTag("plates/copper"))
                .define('X', ModItems.STEEL_LEGS.get())
                .unlockedBy("has_desh_ingot", has(desh))
                .save(consumer, id("armor/steamsuit_legs"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.STEAMSUIT_BOOTS.get())
                .pattern("C C")
                .pattern("DXD")
                .define('D', desh)
                .define('C', forgeTag("plates/copper"))
                .define('X', ModItems.STEEL_BOOTS.get())
                .unlockedBy("has_desh_ingot", has(desh))
                .save(consumer, id("armor/steamsuit_boots"));
    }

    private static void dieselSuitArmorRecipes(Consumer<FinishedRecipe> consumer) {
        TagKey<Item> steel = forgeTag("ingots/steel");
        ItemLike analogCircuit = legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 7);
        ItemLike motor = item("motor");

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DIESELSUIT_HELMET.get())
                .pattern("W W")
                .pattern("W W")
                .pattern("SCS")
                .define('W', Items.RED_WOOL)
                .define('S', steel)
                .define('C', analogCircuit)
                .unlockedBy("has_analog_circuit", has(analogCircuit))
                .save(consumer, id("armor/dieselsuit_helmet"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DIESELSUIT_PLATE.get())
                .pattern("W W")
                .pattern("CDC")
                .pattern("SWS")
                .define('W', Items.RED_WOOL)
                .define('S', steel)
                .define('C', analogCircuit)
                .define('D', ModBlocks.MACHINE_DIESEL.get())
                .unlockedBy("has_diesel_generator", has(ModBlocks.MACHINE_DIESEL.get()))
                .save(consumer, id("armor/dieselsuit_plate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DIESELSUIT_LEGS.get())
                .pattern("M M")
                .pattern("S S")
                .pattern("W W")
                .define('W', Items.RED_WOOL)
                .define('S', steel)
                .define('M', motor)
                .unlockedBy("has_motor", has(motor))
                .save(consumer, id("armor/dieselsuit_legs"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DIESELSUIT_BOOTS.get())
                .pattern("W W")
                .pattern("S S")
                .define('W', Items.RED_WOOL)
                .define('S', steel)
                .unlockedBy("has_steel_ingot", has(steel))
                .save(consumer, id("armor/dieselsuit_boots"));
    }

    private static void envSuitArmorRecipes(Consumer<FinishedRecipe> consumer) {
        TagKey<Item> titaniumPlate = forgeTag("plates/titanium");
        TagKey<Item> glassPanes = forgeTag("glass_panes");
        TagKey<Item> rubber = forgeTag("ingots/rubber");
        ItemLike circuitChip = item("circuit_chip");
        ItemLike titaniumCastPlate = item("plate_cast_titanium");

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ENVSUIT_HELMET.get())
                .pattern("TCT")
                .pattern("TGT")
                .pattern("RRR")
                .define('T', titaniumPlate)
                .define('C', circuitChip)
                .define('G', glassPanes)
                .define('R', rubber)
                .unlockedBy("has_circuit_chip", has(circuitChip))
                .save(consumer, id("armor/envsuit_helmet"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ENVSUIT_PLATE.get())
                .pattern("T T")
                .pattern("TCT")
                .pattern("RRR")
                .define('T', titaniumPlate)
                .define('C', titaniumCastPlate)
                .define('R', rubber)
                .unlockedBy("has_titanium_cast_plate", has(titaniumCastPlate))
                .save(consumer, id("armor/envsuit_plate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ENVSUIT_LEGS.get())
                .pattern("TCT")
                .pattern("R R")
                .pattern("T T")
                .define('T', titaniumPlate)
                .define('C', titaniumCastPlate)
                .define('R', rubber)
                .unlockedBy("has_titanium_cast_plate", has(titaniumCastPlate))
                .save(consumer, id("armor/envsuit_legs"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ENVSUIT_BOOTS.get())
                .pattern("R R")
                .pattern("T T")
                .define('T', titaniumPlate)
                .define('R', rubber)
                .unlockedBy("has_titanium_plate", has(titaniumPlate))
                .save(consumer, id("armor/envsuit_boots"));
    }

    private static void fauArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike plate = item("plate_armor_fau");
        ItemLike motor = item("motor_desh");
        ItemLike poloniumBillet = item("billet_polonium");

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FAU_HELMET.get())
                .pattern("PWP")
                .pattern("PBP")
                .pattern("FSF")
                .define('P', plate)
                .define('W', Items.RED_WOOL)
                .define('B', ModItems.STARMETAL_HELMET.get())
                .define('F', ModItems.GAS_MASK_FILTER.get())
                .define('S', forgeTag("pipes/steel"))
                .unlockedBy("has_fau_armor_plate", has(plate))
                .save(consumer, id("armor/fau_helmet"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FAU_PLATE.get())
                .pattern("MCM")
                .pattern("PBP")
                .pattern("PSP")
                .define('M', motor)
                .define('C', ModItems.DEMON_CORE_CLOSED.get())
                .define('P', plate)
                .define('B', ModItems.STARMETAL_PLATE.get())
                .define('S', block("ancient_scrap"))
                .unlockedBy("has_fau_armor_plate", has(plate))
                .save(consumer, id("armor/fau_plate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FAU_LEGS.get())
                .pattern("MPM")
                .pattern("PBP")
                .pattern("PDP")
                .define('M', motor)
                .define('P', plate)
                .define('B', ModItems.STARMETAL_LEGS.get())
                .define('D', poloniumBillet)
                .unlockedBy("has_fau_armor_plate", has(plate))
                .save(consumer, id("armor/fau_legs"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FAU_BOOTS.get())
                .pattern("PDP")
                .pattern("PBP")
                .define('P', plate)
                .define('D', poloniumBillet)
                .define('B', ModItems.STARMETAL_BOOTS.get())
                .unlockedBy("has_fau_armor_plate", has(plate))
                .save(consumer, id("armor/fau_boots"));
    }

    private static void dnsArmorRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike plate = item("plate_armor_dnt");
        ItemLike quantumCircuit = item("circuit_quantum");
        ItemLike chainsteel = ModItems.INGOT_CHAINSTEEL.get();

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DNS_HELMET.get())
                .pattern("PCP")
                .pattern("PBP")
                .pattern("PSP")
                .define('P', plate)
                .define('C', quantumCircuit)
                .define('B', ModItems.BJ_HELMET.get())
                .define('S', chainsteel)
                .unlockedBy("has_dnt_armor_plate", has(plate))
                .save(consumer, id("armor/dns_helmet"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DNS_PLATE.get())
                .pattern("PCP")
                .pattern("PBP")
                .pattern("PSP")
                .define('P', plate)
                .define('C', ModItems.SINGULARITY_SPARK.get())
                .define('B', ModItems.BJ_PLATE_JETPACK.get())
                .define('S', chainsteel)
                .unlockedBy("has_dnt_armor_plate", has(plate))
                .save(consumer, id("armor/dns_plate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DNS_LEGS.get())
                .pattern("P P")
                .pattern("PBP")
                .pattern("PSP")
                .define('P', plate)
                .define('B', ModItems.BJ_LEGS.get())
                .define('S', chainsteel)
                .unlockedBy("has_dnt_armor_plate", has(plate))
                .save(consumer, id("armor/dns_legs"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DNS_BOOTS.get())
                .pattern("PCP")
                .pattern("PBP")
                .pattern("PSP")
                .define('P', plate)
                .define('C', ModItems.DEMON_CORE_CLOSED.get())
                .define('B', ModItems.BJ_BOOTS.get())
                .define('S', chainsteel)
                .unlockedBy("has_dnt_armor_plate", has(plate))
                .save(consumer, id("armor/dns_boots"));
    }

    private static void standardArmorSet(Consumer<FinishedRecipe> consumer, String prefix, TagKey<Item> material,
            ItemLike helmet, ItemLike chestplate, ItemLike leggings, ItemLike boots) {
        armorPiece(consumer, prefix + "_helmet", helmet, material, "XXX", "X X");
        armorPiece(consumer, prefix + "_plate", chestplate, material, "X X", "XXX", "XXX");
        armorPiece(consumer, prefix + "_legs", leggings, material, "XXX", "X X", "X X");
        armorPiece(consumer, prefix + "_boots", boots, material, "X X", "X X");
    }

    private static void armorPiece(Consumer<FinishedRecipe> consumer, String name, ItemLike result,
            ItemLike material, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result);
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.define(pattern[0].indexOf('X') >= 0 ? 'X' : 'E', material)
                .unlockedBy("has_" + material.asItem(), has(material))
                .save(consumer, id("armor/" + name));
    }

    private static void armorPiece(Consumer<FinishedRecipe> consumer, String name, ItemLike result,
            TagKey<Item> material, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result);
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.define(pattern[0].indexOf('X') >= 0 ? 'X' : 'E', material)
                .unlockedBy("has_" + name + "_material", has(material))
                .save(consumer, id("armor/" + name));
    }

    private static void upgradeArmorPiece(Consumer<FinishedRecipe> consumer, String name, ItemLike result,
            String row1, char key1, ItemLike item1, char key2, ItemLike item2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result)
                .pattern(row1)
                .define(key1, item1)
                .define(key2, item2)
                .unlockedBy("has_" + item2.asItem(), has(item2))
                .save(consumer, id("armor/" + name));
    }

    private static void upgradeArmorPiece(Consumer<FinishedRecipe> consumer, String name, ItemLike result,
            String row1, String row2, char key1, ItemLike item1, char key2, ItemLike item2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result)
                .pattern(row1)
                .pattern(row2)
                .define(key1, item1)
                .define(key2, item2)
                .unlockedBy("has_" + item2.asItem(), has(item2))
                .save(consumer, id("armor/" + name));
    }

    private static void upgradeArmorPiece(Consumer<FinishedRecipe> consumer, String name, ItemLike result,
            String row1, String row2, char key1, ItemLike item1, char key2, ItemLike item2,
            char key3, ItemLike item3) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result)
                .pattern(row1)
                .pattern(row2)
                .define(key1, item1)
                .define(key2, item2)
                .define(key3, item3)
                .unlockedBy("has_" + item2.asItem(), has(item2))
                .save(consumer, id("armor/" + name));
    }

    private static void upgradeArmorPiece(Consumer<FinishedRecipe> consumer, String name, ItemLike result,
            String row1, String row2, String row3, char key1, ItemLike item1, char key2, ItemLike item2) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result)
                .pattern(row1)
                .pattern(row2)
                .pattern(row3)
                .define(key1, item1)
                .define(key2, item2)
                .unlockedBy("has_" + item2.asItem(), has(item2))
                .save(consumer, id("armor/" + name));
    }

    private static void upgradeArmorPiece(Consumer<FinishedRecipe> consumer, String name, ItemLike result,
            String row1, String row2, String row3, char key1, ItemLike item1, char key2, ItemLike item2,
            char key3, ItemLike item3) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result)
                .pattern(row1)
                .pattern(row2)
                .pattern(row3)
                .define(key1, item1)
                .define(key2, item2)
                .define(key3, item3)
                .unlockedBy("has_" + item2.asItem(), has(item2))
                .save(consumer, id("armor/" + name));
    }

    private static void legacyArmorModuleMaterialRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("ingot_euphemium"))
                .pattern("EEE")
                .pattern("EEE")
                .pattern("EEE")
                .define('E', item("nugget_euphemium"))
                .unlockedBy("has_euphemium_nugget", has(item("nugget_euphemium")))
                .save(consumer, id("parts/ingot_euphemium_from_nuggets"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("nugget_euphemium"), 9)
                .requires(item("ingot_euphemium"))
                .unlockedBy("has_euphemium_ingot", has(item("ingot_euphemium")))
                .save(consumer, id("parts/nugget_euphemium_from_ingot"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block("block_euphemium"))
                .pattern("EEE")
                .pattern("EEE")
                .pattern("EEE")
                .define('E', item("ingot_euphemium"))
                .unlockedBy("has_euphemium_ingot", has(item("ingot_euphemium")))
                .save(consumer, id("parts/block_euphemium"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("ingot_euphemium"), 9)
                .requires(block("block_euphemium"))
                .unlockedBy("has_euphemium_block", has(block("block_euphemium")))
                .save(consumer, id("parts/ingot_euphemium_from_block"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.CANTEEN_VODKA.get())
                .pattern("O")
                .pattern("P")
                .define('O', Items.POTATO)
                .define('P', forgeTag("plates/steel"))
                .unlockedBy("has_steel_plate", has(forgeTag("plates/steel")))
                .save(consumer, id("consumables/canteen_vodka"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CAN_EMPTY.get())
                .pattern("P")
                .pattern("P")
                .define('P', forgeTag("plates/aluminium"))
                .unlockedBy("has_aluminium_plate", has(forgeTag("plates/aluminium")))
                .save(consumer, id("consumables/can_empty"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.PILL_IODINE.get(), 8)
                .pattern("IF")
                .define('I', forgeTag("dusts/iodine"))
                .define('F', forgeTag("dusts/fluorite"))
                .unlockedBy("has_iodine_dust", has(forgeTag("dusts/iodine")))
                .save(consumer, id("consumables/pill_iodine"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CONTAINMENT_BOX.get())
                .pattern("LUL")
                .pattern("UCU")
                .pattern("LUL")
                .define('L', forgeTag("plates/lead"))
                .define('U', forgeTag("ingots/ferrouranium"))
                .define('C', ModBlocks.CRATE_STEEL.get())
                .unlockedBy("has_steel_crate", has(ModBlocks.CRATE_STEEL.get()))
                .save(consumer, id("consumables/containment_box"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IV_EMPTY.get(), 4)
                .pattern("S")
                .pattern("I")
                .pattern("S")
                .define('S', forgeTag("ingots/any_rubber"))
                .define('I', forgeTag("plates/iron"))
                .unlockedBy("has_rubber_ingot", has(forgeTag("ingots/any_rubber")))
                .save(consumer, id("consumables/iv_empty"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.IV_XP_EMPTY.get())
                .requires(ModItems.IV_EMPTY.get())
                .requires(item("powder_magic"))
                .unlockedBy("has_iv_empty", has(ModItems.IV_EMPTY.get()))
                .save(consumer, id("consumables/iv_xp_empty"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_EMPTY.get(), 6)
                .pattern("P")
                .pattern("C")
                .pattern("B")
                .define('P', forgeTag("plates/iron"))
                .define('C', item("cell_empty"))
                .define('B', Items.IRON_BARS)
                .unlockedBy("has_empty_cell", has(item("cell_empty")))
                .save(consumer, id("consumables/syringe_empty"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_ANTIDOTE.get(), 6)
                .pattern("SSS")
                .pattern("PMP")
                .pattern("SSS")
                .define('S', ModItems.SYRINGE_EMPTY.get())
                .define('P', Items.PUMPKIN_SEEDS)
                .define('M', Items.MILK_BUCKET)
                .unlockedBy("has_empty_syringe", has(ModItems.SYRINGE_EMPTY.get()))
                .save(consumer, id("consumables/syringe_antidote_milk_full"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_ANTIDOTE.get(), 6)
                .pattern("SPS")
                .pattern("SMS")
                .pattern("SPS")
                .define('S', ModItems.SYRINGE_EMPTY.get())
                .define('P', Items.PUMPKIN_SEEDS)
                .define('M', Items.MILK_BUCKET)
                .unlockedBy("has_empty_syringe", has(ModItems.SYRINGE_EMPTY.get()))
                .save(consumer, id("consumables/syringe_antidote_milk_cross"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_ANTIDOTE.get(), 6)
                .pattern("SSS")
                .pattern("PMP")
                .pattern("SSS")
                .define('S', ModItems.SYRINGE_EMPTY.get())
                .define('P', Items.PUMPKIN_SEEDS)
                .define('M', Items.SUGAR_CANE)
                .unlockedBy("has_empty_syringe", has(ModItems.SYRINGE_EMPTY.get()))
                .save(consumer, id("consumables/syringe_antidote_reeds_full"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_ANTIDOTE.get(), 6)
                .pattern("SPS")
                .pattern("SMS")
                .pattern("SPS")
                .define('S', ModItems.SYRINGE_EMPTY.get())
                .define('P', Items.PUMPKIN_SEEDS)
                .define('M', Items.SUGAR_CANE)
                .unlockedBy("has_empty_syringe", has(ModItems.SYRINGE_EMPTY.get()))
                .save(consumer, id("consumables/syringe_antidote_reeds_cross"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_POISON.get())
                .pattern("SLS")
                .pattern("LCL")
                .pattern("SLS")
                .define('S', Items.SPIDER_EYE)
                .define('L', forgeTag("dusts/lead"))
                .define('C', ModItems.SYRINGE_EMPTY.get())
                .unlockedBy("has_empty_syringe", has(ModItems.SYRINGE_EMPTY.get()))
                .save(consumer, id("consumables/syringe_poison_lead"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_POISON.get())
                .pattern("SLS")
                .pattern("LCL")
                .pattern("SLS")
                .define('S', Items.SPIDER_EYE)
                .define('L', item("powder_poison"))
                .define('C', ModItems.SYRINGE_EMPTY.get())
                .unlockedBy("has_empty_syringe", has(ModItems.SYRINGE_EMPTY.get()))
                .save(consumer, id("consumables/syringe_poison_powder"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_AWESOME.get())
                .pattern("SPS")
                .pattern("NCN")
                .pattern("SPS")
                .define('S', forgeTag("dusts/sulfur"))
                .define('P', forgeTag("nuggets/pu239"))
                .define('N', forgeTag("nuggets/pu238"))
                .define('C', ModItems.SYRINGE_EMPTY.get())
                .unlockedBy("has_empty_syringe", has(ModItems.SYRINGE_EMPTY.get()))
                .save(consumer, id("consumables/syringe_awesome_pu239_vertical"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_AWESOME.get())
                .pattern("SNS")
                .pattern("PCP")
                .pattern("SNS")
                .define('S', forgeTag("dusts/sulfur"))
                .define('P', forgeTag("nuggets/pu239"))
                .define('N', forgeTag("nuggets/pu238"))
                .define('C', ModItems.SYRINGE_EMPTY.get())
                .unlockedBy("has_empty_syringe", has(ModItems.SYRINGE_EMPTY.get()))
                .save(consumer, id("consumables/syringe_awesome_pu238_vertical"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BOTTLE_EMPTY.get(), 6)
                .pattern(" G ")
                .pattern("G G")
                .pattern("GGG")
                .define('G', forgeTag("glass_panes"))
                .unlockedBy("has_glass_pane", has(forgeTag("glass_panes")))
                .save(consumer, id("consumables/bottle_empty"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BOTTLE_NUKA.get())
                .requires(ModItems.BOTTLE_EMPTY.get())
                .requires(Items.POTION)
                .requires(Items.SUGAR)
                .requires(forgeTag("dusts/coal"))
                .unlockedBy("has_empty_bottle", has(ModItems.BOTTLE_EMPTY.get()))
                .save(consumer, id("consumables/bottle_nuka"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BOTTLE_CHERRY.get())
                .requires(ModItems.BOTTLE_EMPTY.get())
                .requires(Items.POTION)
                .requires(Items.SUGAR)
                .requires(forgeTag("dusts/redstone"))
                .unlockedBy("has_empty_bottle", has(ModItems.BOTTLE_EMPTY.get()))
                .save(consumer, id("consumables/bottle_cherry"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BOTTLE_QUANTUM.get())
                .requires(ModItems.BOTTLE_EMPTY.get())
                .requires(Items.POTION)
                .requires(Items.SUGAR)
                .requires(item("trinitite"))
                .unlockedBy("has_empty_bottle", has(ModItems.BOTTLE_EMPTY.get()))
                .save(consumer, id("consumables/bottle_quantum"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BOTTLE_SPARKLE.get())
                .requires(ModItems.BOTTLE_NUKA.get())
                .requires(Items.CARROT)
                .requires(forgeTag("nuggets/gold"))
                .unlockedBy("has_nuka_bottle", has(ModItems.BOTTLE_NUKA.get()))
                .save(consumer, id("consumables/bottle_sparkle"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BOTTLE_RAD.get())
                .requires(ModItems.BOTTLE_QUANTUM.get())
                .requires(Items.CARROT)
                .requires(forgeTag("nuggets/gold"))
                .unlockedBy("has_quantum_bottle", has(ModItems.BOTTLE_QUANTUM.get()))
                .save(consumer, id("consumables/bottle_rad"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BOTTLE2_EMPTY.get(), 6)
                .pattern(" G ")
                .pattern("G G")
                .pattern("G G")
                .define('G', forgeTag("glass_panes"))
                .unlockedBy("has_glass_pane", has(forgeTag("glass_panes")))
                .save(consumer, id("consumables/bottle2_empty"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BOTTLE2_KORL.get())
                .requires(ModItems.BOTTLE2_EMPTY.get())
                .requires(Items.POTION)
                .requires(Items.SUGAR)
                .requires(forgeTag("dusts/copper"))
                .unlockedBy("has_empty_bottle2", has(ModItems.BOTTLE2_EMPTY.get()))
                .save(consumer, id("consumables/bottle2_korl"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BOTTLE2_FRITZ.get())
                .requires(ModItems.BOTTLE2_EMPTY.get())
                .requires(Items.POTION)
                .requires(Items.SUGAR)
                .requires(forgeTag("dusts/tungsten"))
                .unlockedBy("has_empty_bottle2", has(ModItems.BOTTLE2_EMPTY.get()))
                .save(consumer, id("consumables/bottle2_fritz"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CAN_SMART.get())
                .requires(ModItems.CAN_EMPTY.get())
                .requires(Items.POTION)
                .requires(Items.SUGAR)
                .requires(forgeTag("dusts/niter"))
                .unlockedBy("has_empty_can", has(ModItems.CAN_EMPTY.get()))
                .save(consumer, id("consumables/can_smart"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CAN_CREATURE.get())
                .requires(ModItems.CAN_EMPTY.get())
                .requires(Items.POTION)
                .requires(Items.SUGAR)
                .requires(HbmFluidContainerIngredient.of(HbmFluids.DIESEL, 1_000))
                .unlockedBy("has_empty_can", has(ModItems.CAN_EMPTY.get()))
                .save(consumer, id("consumables/can_creature"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CAN_REDBOMB.get())
                .requires(ModItems.CAN_EMPTY.get())
                .requires(Items.POTION)
                .requires(Items.SUGAR)
                .requires(item("pellet_cluster"))
                .unlockedBy("has_empty_can", has(ModItems.CAN_EMPTY.get()))
                .save(consumer, id("consumables/can_redbomb"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CAN_MRSUGAR.get())
                .requires(ModItems.CAN_EMPTY.get())
                .requires(Items.POTION)
                .requires(Items.SUGAR)
                .requires(forgeTag("dusts/fluorite"))
                .unlockedBy("has_empty_can", has(ModItems.CAN_EMPTY.get()))
                .save(consumer, id("consumables/can_mrsugar"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CAN_OVERCHARGE.get())
                .requires(ModItems.CAN_EMPTY.get())
                .requires(Items.POTION)
                .requires(Items.SUGAR)
                .requires(forgeTag("dusts/sulfur"))
                .unlockedBy("has_empty_can", has(ModItems.CAN_EMPTY.get()))
                .save(consumer, id("consumables/can_overcharge"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CAN_LUNA.get())
                .requires(ModItems.CAN_EMPTY.get())
                .requires(Items.POTION)
                .requires(Items.SUGAR)
                .requires(item("powder_meteorite_tiny"))
                .unlockedBy("has_empty_can", has(ModItems.CAN_EMPTY.get()))
                .save(consumer, id("consumables/can_luna"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CANNED_RECURSION.get())
                .requires(ModItems.CANNED_RECURSION.get())
                .unlockedBy("has_canned_recursion", has(ModItems.CANNED_RECURSION.get()))
                .save(consumer, id("consumables/canned_recursion"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CHOCOLATE_MILK.get())
                .requires(forgeTag("glass_panes"))
                .requires(forgeTag("dyes/brown"))
                .requires(Items.MILK_BUCKET)
                .requires(HbmFluidContainerIngredient.of(HbmFluids.NITROGLYCERIN, 1_000))
                .unlockedBy("has_milk_bucket", has(Items.MILK_BUCKET))
                .save(consumer, id("consumables/chocolate_milk"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.COFFEE.get())
                .requires(forgeTag("dusts/coal"))
                .requires(Items.MILK_BUCKET)
                .requires(Items.POTION)
                .requires(Items.SUGAR)
                .unlockedBy("has_coal_dust", has(forgeTag("dusts/coal")))
                .save(consumer, id("consumables/coffee"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.COFFEE_RADIUM.get())
                .requires(ModItems.COFFEE.get())
                .requires(forgeTag("nuggets/radium226"))
                .unlockedBy("has_coffee", has(ModItems.COFFEE.get()))
                .save(consumer, id("consumables/coffee_radium"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_METAL_EMPTY.get(), 6)
                .pattern("P")
                .pattern("C")
                .pattern("B")
                .define('P', forgeTag("plates/iron"))
                .define('C', item("rod_empty"))
                .define('B', Items.IRON_BARS)
                .unlockedBy("has_empty_rod", has(item("rod_empty")))
                .save(consumer, id("consumables/syringe_metal_empty"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_METAL_STIMPAK.get())
                .pattern(" N ")
                .pattern("NSN")
                .pattern(" N ")
                .define('N', Items.NETHER_WART)
                .define('S', ModItems.SYRINGE_METAL_EMPTY.get())
                .unlockedBy("has_metal_syringe", has(ModItems.SYRINGE_METAL_EMPTY.get()))
                .save(consumer, id("consumables/syringe_metal_stimpak_nether_wart"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SYRINGE_METAL_STIMPAK.get())
                .requires(item("nitra_small"), 3)
                .requires(ModItems.SYRINGE_METAL_EMPTY.get())
                .unlockedBy("has_metal_syringe", has(ModItems.SYRINGE_METAL_EMPTY.get()))
                .save(consumer, id("consumables/syringe_metal_stimpak_nitra"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_METAL_MEDX.get())
                .pattern(" N ")
                .pattern("NSN")
                .pattern(" N ")
                .define('N', Items.QUARTZ)
                .define('S', ModItems.SYRINGE_METAL_EMPTY.get())
                .unlockedBy("has_metal_syringe", has(ModItems.SYRINGE_METAL_EMPTY.get()))
                .save(consumer, id("consumables/syringe_metal_medx"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_METAL_PSYCHO.get())
                .pattern(" N ")
                .pattern("NSN")
                .pattern(" N ")
                .define('N', Items.GLOWSTONE_DUST)
                .define('S', ModItems.SYRINGE_METAL_EMPTY.get())
                .unlockedBy("has_metal_syringe", has(ModItems.SYRINGE_METAL_EMPTY.get()))
                .save(consumer, id("consumables/syringe_metal_psycho"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_METAL_SUPER.get())
                .pattern(" N ")
                .pattern("PSP")
                .pattern("L L")
                .define('N', ModItems.BOTTLE_NUKA.get())
                .define('P', forgeTag("plates/steel"))
                .define('S', ModItems.SYRINGE_METAL_STIMPAK.get())
                .define('L', Items.LEATHER)
                .unlockedBy("has_stimpak", has(ModItems.SYRINGE_METAL_STIMPAK.get()))
                .save(consumer, id("consumables/syringe_metal_super_nuka_leather"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_METAL_SUPER.get())
                .pattern(" N ")
                .pattern("PSP")
                .pattern("L L")
                .define('N', ModItems.BOTTLE_NUKA.get())
                .define('P', forgeTag("plates/steel"))
                .define('S', ModItems.SYRINGE_METAL_STIMPAK.get())
                .define('L', forgeTag("ingots/any_rubber"))
                .unlockedBy("has_stimpak", has(ModItems.SYRINGE_METAL_STIMPAK.get()))
                .save(consumer, id("consumables/syringe_metal_super_nuka_rubber"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_METAL_SUPER.get())
                .pattern(" N ")
                .pattern("PSP")
                .pattern("L L")
                .define('N', ModItems.BOTTLE_CHERRY.get())
                .define('P', forgeTag("plates/steel"))
                .define('S', ModItems.SYRINGE_METAL_STIMPAK.get())
                .define('L', Items.LEATHER)
                .unlockedBy("has_stimpak", has(ModItems.SYRINGE_METAL_STIMPAK.get()))
                .save(consumer, id("consumables/syringe_metal_super_cherry_leather"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYRINGE_METAL_SUPER.get())
                .pattern(" N ")
                .pattern("PSP")
                .pattern("L L")
                .define('N', ModItems.BOTTLE_CHERRY.get())
                .define('P', forgeTag("plates/steel"))
                .define('S', ModItems.SYRINGE_METAL_STIMPAK.get())
                .define('L', forgeTag("ingots/any_rubber"))
                .unlockedBy("has_stimpak", has(ModItems.SYRINGE_METAL_STIMPAK.get()))
                .save(consumer, id("consumables/syringe_metal_super_cherry_rubber"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SYRINGE_TAINT.get())
                .requires(ModItems.BOTTLE2_EMPTY.get())
                .requires(ModItems.SYRINGE_METAL_EMPTY.get())
                .requires(item("ducttape"))
                .requires(item("powder_magic"))
                .requires(forgeTag("nuggets/schrabidium"))
                .requires(Items.POTION)
                .unlockedBy("has_metal_syringe", has(ModItems.SYRINGE_METAL_EMPTY.get()))
                .save(consumer, id("consumables/syringe_taint"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MED_BAG.get())
                .pattern("LLL")
                .pattern("SIS")
                .pattern("LLL")
                .define('L', Items.LEATHER)
                .define('S', ModItems.SYRINGE_METAL_STIMPAK.get())
                .define('I', ModItems.SYRINGE_ANTIDOTE.get())
                .unlockedBy("has_stimpak", has(ModItems.SYRINGE_METAL_STIMPAK.get()))
                .save(consumer, id("consumables/med_bag_leather_antidote"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MED_BAG.get())
                .pattern("LLL")
                .pattern("SIS")
                .pattern("LLL")
                .define('L', Items.LEATHER)
                .define('S', ModItems.SYRINGE_METAL_STIMPAK.get())
                .define('I', ModItems.PILL_IODINE.get())
                .unlockedBy("has_stimpak", has(ModItems.SYRINGE_METAL_STIMPAK.get()))
                .save(consumer, id("consumables/med_bag_leather_iodine"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MED_BAG.get())
                .pattern("LL")
                .pattern("SI")
                .pattern("LL")
                .define('L', Items.LEATHER)
                .define('S', ModItems.SYRINGE_METAL_SUPER.get())
                .define('I', ModItems.RADAWAY.get())
                .unlockedBy("has_super_stimpak", has(ModItems.SYRINGE_METAL_SUPER.get()))
                .save(consumer, id("consumables/med_bag_leather_radaway"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MED_BAG.get())
                .pattern("LLL")
                .pattern("SIS")
                .pattern("LLL")
                .define('L', forgeTag("ingots/any_rubber"))
                .define('S', ModItems.SYRINGE_METAL_STIMPAK.get())
                .define('I', ModItems.SYRINGE_ANTIDOTE.get())
                .unlockedBy("has_any_rubber", has(forgeTag("ingots/any_rubber")))
                .save(consumer, id("consumables/med_bag_rubber_antidote"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MED_BAG.get())
                .pattern("LLL")
                .pattern("SIS")
                .pattern("LLL")
                .define('L', forgeTag("ingots/any_rubber"))
                .define('S', ModItems.SYRINGE_METAL_STIMPAK.get())
                .define('I', ModItems.PILL_IODINE.get())
                .unlockedBy("has_any_rubber", has(forgeTag("ingots/any_rubber")))
                .save(consumer, id("consumables/med_bag_rubber_iodine"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MED_BAG.get())
                .pattern("LL")
                .pattern("SI")
                .pattern("LL")
                .define('L', forgeTag("ingots/any_rubber"))
                .define('S', ModItems.SYRINGE_METAL_SUPER.get())
                .define('I', ModItems.RADAWAY.get())
                .unlockedBy("has_any_rubber", has(forgeTag("ingots/any_rubber")))
                .save(consumer, id("consumables/med_bag_rubber_radaway"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEALTH_BOY.get())
                .pattern(" B")
                .pattern("LI")
                .pattern("LC")
                .define('B', Blocks.STONE_BUTTON)
                .define('L', Items.LEATHER)
                .define('I', forgeTag("ingots/steel"))
                .define('C', item("circuit_basic"))
                .unlockedBy("has_basic_circuit", has(item("circuit_basic")))
                .save(consumer, id("consumables/stealth_boy"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("catalyst_clay"))
                .requires(forgeTag("dusts/iron"))
                .requires(Items.CLAY_BALL)
                .unlockedBy("has_iron_dust", has(forgeTag("dusts/iron")))
                .save(consumer, id("parts/catalyst_clay"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PISTON_SELENIUM.get())
                .pattern("SSS")
                .pattern("STS")
                .pattern(" D ")
                .define('S', forgeTag("plates/steel"))
                .define('T', forgeTag("ingots/tungsten"))
                .define('D', forgeTag("bolts/dura_steel"))
                .unlockedBy("has_tungsten_ingot", has(forgeTag("ingots/tungsten")))
                .save(consumer, id("parts/piston_selenium"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GAS_MASK_FILTER.get())
                .pattern("I")
                .pattern("F")
                .define('I', forgeTag("plates/iron"))
                .define('F', item("filter_coal"))
                .unlockedBy("has_filter_coal", has(item("filter_coal")))
                .save(consumer, id("consumables/gas_mask_filter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GAS_MASK_FILTER_MONO.get())
                .pattern("ZZZ")
                .pattern("ZCZ")
                .pattern("ZZZ")
                .define('Z', forgeTag("nuggets/zirconium"))
                .define('C', item("catalyst_clay"))
                .unlockedBy("has_zirconium_nugget", has(forgeTag("nuggets/zirconium")))
                .save(consumer, id("consumables/gas_mask_filter_mono"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GAS_MASK_FILTER_COMBO.get())
                .pattern("ZCZ")
                .pattern("CFC")
                .pattern("ZCZ")
                .define('Z', forgeTag("ingots/zirconium"))
                .define('C', item("catalyst_clay"))
                .define('F', ModItems.GAS_MASK_FILTER.get())
                .unlockedBy("has_gas_mask_filter", has(ModItems.GAS_MASK_FILTER.get()))
                .save(consumer, id("consumables/gas_mask_filter_combo"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GAS_MASK_FILTER_RAG.get())
                .pattern("I")
                .pattern("F")
                .define('I', Items.IRON_INGOT)
                .define('F', item("rag_damp"))
                .unlockedBy("has_damp_rag", has(item("rag_damp")))
                .save(consumer, id("consumables/gas_mask_filter_rag"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GAS_MASK_FILTER_PISS.get())
                .pattern("I")
                .pattern("F")
                .define('I', Items.IRON_INGOT)
                .define('F', item("rag_piss"))
                .unlockedBy("has_piss_rag", has(item("rag_piss")))
                .save(consumer, id("consumables/gas_mask_filter_piss"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RADAWAY.get())
                .requires(ModItems.IV_BLOOD.get())
                .requires(forgeTag("dusts/coal"))
                .requires(Items.PUMPKIN_SEEDS)
                .unlockedBy("has_iv_blood", has(ModItems.IV_BLOOD.get()))
                .save(consumer, id("consumables/radaway"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RADAWAY_STRONG.get())
                .requires(ModItems.RADAWAY.get())
                .requires(ModBlocks.MUSH.get())
                .unlockedBy("has_radaway", has(ModItems.RADAWAY.get()))
                .save(consumer, id("consumables/radaway_strong"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RADAWAY_FLUSH.get())
                .requires(ModItems.RADAWAY_STRONG.get())
                .requires(forgeTag("dusts/iodine"))
                .unlockedBy("has_strong_radaway", has(ModItems.RADAWAY_STRONG.get()))
                .save(consumer, id("consumables/radaway_flush"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.RADX.get())
                .requires(Ingredient.of(forgeTag("dusts/coal")), 2)
                .requires(forgeTag("dusts/fluorite"))
                .unlockedBy("has_fluorite_dust", has(forgeTag("dusts/fluorite")))
                .save(consumer, id("consumables/radx"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.PLAN_C.get())
                .pattern("PFP")
                .define('P', item("powder_poison"))
                .define('F', forgeTag("dusts/fluorite"))
                .unlockedBy("has_poison_powder", has(item("powder_poison")))
                .save(consumer, id("consumables/plan_c"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.FMN.get())
                .requires(forgeTag("dusts/coal"))
                .requires(forgeTag("dusts/polonium"))
                .requires(forgeTag("dusts/strontium"))
                .unlockedBy("has_polonium_dust", has(forgeTag("dusts/polonium")))
                .save(consumer, id("consumables/fmn"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.FIVE_HTP.get())
                .requires(forgeTag("dusts/coal"))
                .requires(forgeTag("dusts/euphemium"))
                .requires(ModItems.CANTEEN_VODKA.get())
                .unlockedBy("has_vodka_canteen", has(ModItems.CANTEEN_VODKA.get()))
                .save(consumer, id("consumables/five_htp"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.SIOX.get(), 8)
                .requires(forgeTag("dusts/coal"))
                .requires(forgeTag("dusts/asbestos"))
                .requires(forgeTag("nuggets/bismuth"))
                .unlockedBy("has_asbestos_dust", has(forgeTag("dusts/asbestos")))
                .save(consumer, id("consumables/siox"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.XANAX.get())
                .requires(forgeTag("dusts/coal"))
                .requires(forgeTag("dusts/niter"))
                .requires(forgeTag("dusts/bromine"))
                .unlockedBy("has_bromine_dust", has(forgeTag("dusts/bromine")))
                .save(consumer, id("consumables/xanax"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CIGARETTE.get(), 16)
                .requires(forgeTag("ingots/asbestos"))
                .requires(forgeTag("any/tar"))
                .requires(forgeTag("nuggets/polonium210"))
                .requires(item("plant_item_tobacco"))
                .unlockedBy("has_tobacco", has(item("plant_item_tobacco")))
                .save(consumer, id("consumables/cigarette"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CRACKPIPE.get())
                .requires(ModItems.CATALYTIC_CONVERTER.get())
                .unlockedBy("has_catalytic_converter", has(ModItems.CATALYTIC_CONVERTER.get()))
                .save(consumer, id("consumables/crackpipe"));

        legacyTemFlakesRecipe(consumer, "tem_flakes_discount", 0,
                ingredientTag(forgeTag("nuggets/gold")),
                ingredientItem(Items.PAPER));
        legacyTemFlakesRecipe(consumer, "tem_flakes", 1,
                ingredientTag(forgeTag("nuggets/gold")),
                ingredientTag(forgeTag("nuggets/gold")),
                ingredientTag(forgeTag("nuggets/gold")),
                ingredientItem(Items.PAPER));
        legacyTemFlakesRecipe(consumer, "tem_flakes_expensive", 2,
                ingredientTag(forgeTag("ingots/gold")),
                ingredientTag(forgeTag("ingots/gold")),
                ingredientTag(forgeTag("nuggets/gold")),
                ingredientTag(forgeTag("nuggets/gold")),
                ingredientItem(Items.PAPER));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.GLOWING_STEW.get())
                .requires(Items.BOWL)
                .requires(ModBlocks.MUSH.get(), 2)
                .unlockedBy("has_glowing_mushroom", has(ModBlocks.MUSH.get()))
                .save(consumer, id("consumables/glowing_stew"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.BALEFIRE_SCRAMBLED.get())
                .requires(Items.BOWL)
                .requires(item("egg_balefire"))
                .unlockedBy("has_balefire_egg", has(item("egg_balefire")))
                .save(consumer, id("consumables/balefire_scrambled"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.BALEFIRE_AND_HAM.get())
                .requires(ModItems.BALEFIRE_SCRAMBLED.get())
                .requires(Items.COOKED_BEEF)
                .unlockedBy("has_balefire_scrambled", has(ModItems.BALEFIRE_SCRAMBLED.get()))
                .save(consumer, id("consumables/balefire_and_ham"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.MARSHMALLOW.get())
                .requires(Items.STICK)
                .requires(Items.SUGAR)
                .requires(Items.WHEAT_SEEDS)
                .unlockedBy("has_sugar", has(Items.SUGAR))
                .save(consumer, id("consumables/marshmallow"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.MED_IPECAC.get())
                .requires(Items.GLASS_BOTTLE)
                .requires(Items.NETHER_WART)
                .unlockedBy("has_nether_wart", has(Items.NETHER_WART))
                .save(consumer, id("consumables/med_ipecac"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.MED_PTSD.get())
                .requires(ModItems.MED_IPECAC.get())
                .unlockedBy("has_ipecac", has(ModItems.MED_IPECAC.get()))
                .save(consumer, id("consumables/med_ptsd"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.PANCAKE.get())
                .requires(forgeTag("dusts/redstone"))
                .requires(forgeTag("dusts/diamond"))
                .requires(Items.WHEAT)
                .requires(forgeTag("bolts/steel"))
                .requires(forgeTag("wires/copper"))
                .requires(forgeTag("plates/steel"))
                .unlockedBy("has_diamond_dust", has(forgeTag("dusts/diamond")))
                .save(consumer, id("consumables/pancake_diamond"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.PANCAKE.get())
                .requires(forgeTag("dusts/redstone"))
                .requires(forgeTag("dusts/emerald"))
                .requires(Items.WHEAT)
                .requires(forgeTag("bolts/steel"))
                .requires(forgeTag("wires/copper"))
                .requires(forgeTag("plates/steel"))
                .unlockedBy("has_emerald_dust", has(forgeTag("dusts/emerald")))
                .save(consumer, id("consumables/pancake_emerald"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.BOMB_WAFFLE.get())
                .pattern("WEW")
                .pattern("MPM")
                .pattern("WEW")
                .define('W', Items.WHEAT)
                .define('E', Items.EGG)
                .define('M', Items.MILK_BUCKET)
                .define('P', item("man_core"))
                .unlockedBy("has_man_core", has(item("man_core")))
                .save(consumer, id("consumables/bomb_waffle"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.SCHNITZEL_VEGAN.get(), 3)
                .pattern("RWR")
                .pattern("WPW")
                .pattern("RWR")
                .define('R', Items.SUGAR_CANE)
                .define('W', item("nuclear_waste"))
                .define('P', Items.PUMPKIN_SEEDS)
                .unlockedBy("has_nuclear_waste", has(item("nuclear_waste")))
                .save(consumer, id("consumables/schnitzel_vegan"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.COTTON_CANDY.get(), 2)
                .pattern(" S ")
                .pattern("SPS")
                .pattern(" H ")
                .define('S', Items.SUGAR)
                .define('P', forgeTag("nuggets/pu239"))
                .define('H', Items.STICK)
                .unlockedBy("has_pu239_nugget", has(forgeTag("nuggets/pu239")))
                .save(consumer, id("consumables/cotton_candy"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.APPLE_SCHRABIDIUM.get())
                .pattern("SSS")
                .pattern("SAS")
                .pattern("SSS")
                .define('S', forgeTag("nuggets/schrabidium"))
                .define('A', Items.APPLE)
                .unlockedBy("has_schrabidium_nugget", has(forgeTag("nuggets/schrabidium")))
                .save(consumer, id("consumables/apple_schrabidium"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.APPLE_SCHRABIDIUM_INGOT.get())
                .pattern("SSS")
                .pattern("SAS")
                .pattern("SSS")
                .define('S', forgeTag("ingots/schrabidium"))
                .define('A', Items.APPLE)
                .unlockedBy("has_schrabidium_ingot", has(forgeTag("ingots/schrabidium")))
                .save(consumer, id("consumables/apple_schrabidium_ingot"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.APPLE_SCHRABIDIUM_BLOCK.get())
                .pattern("SSS")
                .pattern("SAS")
                .pattern("SSS")
                .define('S', forgeTag("storage_blocks/schrabidium"))
                .define('A', Items.APPLE)
                .unlockedBy("has_schrabidium_block", has(forgeTag("storage_blocks/schrabidium")))
                .save(consumer, id("consumables/apple_schrabidium_block"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.APPLE_LEAD.get())
                .pattern("SSS")
                .pattern("SAS")
                .pattern("SSS")
                .define('S', forgeTag("nuggets/lead"))
                .define('A', Items.APPLE)
                .unlockedBy("has_lead_nugget", has(forgeTag("nuggets/lead")))
                .save(consumer, id("consumables/apple_lead"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.APPLE_LEAD_INGOT.get())
                .pattern("SSS")
                .pattern("SAS")
                .pattern("SSS")
                .define('S', forgeTag("ingots/lead"))
                .define('A', Items.APPLE)
                .unlockedBy("has_lead_ingot", has(forgeTag("ingots/lead")))
                .save(consumer, id("consumables/apple_lead_ingot"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.APPLE_LEAD_BLOCK.get())
                .pattern("SSS")
                .pattern("SAS")
                .pattern("SSS")
                .define('S', forgeTag("storage_blocks/lead"))
                .define('A', Items.APPLE)
                .unlockedBy("has_lead_block", has(forgeTag("storage_blocks/lead")))
                .save(consumer, id("consumables/apple_lead_block"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.APPLE_EUPHEMIUM.get())
                .pattern("EEE")
                .pattern("EAE")
                .pattern("EEE")
                .define('E', forgeTag("nuggets/euphemium"))
                .define('A', Items.APPLE)
                .unlockedBy("has_euphemium_nugget", has(forgeTag("nuggets/euphemium")))
                .save(consumer, id("consumables/apple_euphemium"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.INGOT_SMORE.get())
                .requires(Items.WHEAT)
                .requires(StrictNBTIngredient.of(MarshmallowItem.roastedStack(ModItems.MARSHMALLOW.get())))
                .requires(Items.COCOA_BEANS)
                .unlockedBy("has_roasted_marshmallow", has(ModItems.MARSHMALLOW.get()))
                .save(consumer, id("consumables/ingot_smore"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.QUESADILLA.get(), 3)
                .requires(ModItems.CHEESE.get(), 2)
                .requires(Items.BREAD)
                .unlockedBy("has_cheese", has(ModItems.CHEESE.get()))
                .save(consumer, id("consumables/quesadilla"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.MUCHO_MANGO.get())
                .requires(Items.POTION)
                .requires(Items.SUGAR, 2)
                .requires(forgeTag("dyes/orange"))
                .unlockedBy("has_orange_dye", has(forgeTag("dyes/orange")))
                .save(consumer, id("consumables/mucho_mango"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PEAS.get())
                .pattern(" S ")
                .pattern("SNS")
                .pattern(" S ")
                .define('S', Items.WHEAT_SEEDS)
                .define('N', forgeTag("nuggets/gold"))
                .unlockedBy("has_gold_nugget", has(forgeTag("nuggets/gold")))
                .save(consumer, id("consumables/peas"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BOTTLE_MERCURY.get())
                .pattern("MMM")
                .pattern("MBM")
                .pattern("MMM")
                .define('M', item("ingot_mercury"))
                .define('B', Items.GLASS_BOTTLE)
                .unlockedBy("has_mercury_ingot", has(item("ingot_mercury")))
                .save(consumer, id("parts/bottle_mercury"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("ingot_mercury"), 8)
                .requires(ModItems.BOTTLE_MERCURY.get())
                .unlockedBy("has_bottle_mercury", has(ModItems.BOTTLE_MERCURY.get()))
                .save(consumer, id("parts/ingot_mercury_from_bottle"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("ingot_mercury"))
                .pattern("MMM")
                .pattern("MMM")
                .pattern("MMM")
                .define('M', item("nugget_mercury"))
                .unlockedBy("has_mercury_drop", has(item("nugget_mercury")))
                .save(consumer, id("parts/ingot_mercury_from_nuggets"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("nugget_mercury"), 9)
                .requires(item("ingot_mercury"))
                .unlockedBy("has_mercury_ingot", has(item("ingot_mercury")))
                .save(consumer, id("parts/nugget_mercury_from_ingot"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("nugget_mercury"))
                .pattern("MMM")
                .pattern("MMM")
                .pattern("MMM")
                .define('M', item("nugget_mercury_tiny"))
                .unlockedBy("has_tiny_mercury_drop", has(item("nugget_mercury_tiny")))
                .save(consumer, id("parts/nugget_mercury_from_tiny_drops"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("nugget_mercury_tiny"), 9)
                .requires(item("nugget_mercury"))
                .unlockedBy("has_mercury_drop", has(item("nugget_mercury")))
                .save(consumer, id("parts/nugget_mercury_tiny_from_drop"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("billet_australium"))
                .pattern("NNN")
                .pattern("NNN")
                .define('N', item("nugget_australium"))
                .unlockedBy("has_australium_nugget", has(item("nugget_australium")))
                .save(consumer, id("parts/billet_australium_from_hbm_nuggets"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("billet_australium"))
                .pattern("NNN")
                .pattern("NNN")
                .define('N', forgeTag("nuggets/australium"))
                .unlockedBy("has_australium_nugget", has(forgeTag("nuggets/australium")))
                .save(consumer, id("parts/billet_australium_from_nuggets"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("nugget_australium"), 6)
                .requires(item("billet_australium"))
                .unlockedBy("has_australium_billet", has(item("billet_australium")))
                .save(consumer, id("parts/nugget_australium_from_billet"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("ingot_australium"), 2)
                .requires(item("billet_australium"), 3)
                .unlockedBy("has_australium_billet", has(item("billet_australium")))
                .save(consumer, id("parts/ingot_australium_from_billets"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("billet_australium"), 3)
                .pattern("II")
                .define('I', item("ingot_australium"))
                .unlockedBy("has_australium_ingot", has(item("ingot_australium")))
                .save(consumer, id("parts/billet_australium_from_ingots"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("ingot_lead"))
                .pattern("LLL")
                .pattern("LLL")
                .pattern("LLL")
                .define('L', item("nugget_lead"))
                .unlockedBy("has_lead_nugget", has(item("nugget_lead")))
                .save(consumer, id("parts/ingot_lead_from_nuggets"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("nugget_lead"), 9)
                .requires(item("ingot_lead"))
                .unlockedBy("has_lead_ingot", has(item("ingot_lead")))
                .save(consumer, id("parts/nugget_lead_from_ingot"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("billet_balefire_gold"))
                .requires(item("billet_au198"))
                .requires(ModItems.CELL_ANTIMATTER.get())
                .requires(item("pellet_charged"))
                .unlockedBy("has_au198_billet", has(item("billet_au198")))
                .save(consumer, id("parts/billet_balefire_gold"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("billet_flashlead"), 2)
                .requires(item("billet_balefire_gold"))
                .requires(item("billet_pb209"))
                .requires(ModItems.CELL_ANTIMATTER.get())
                .unlockedBy("has_balefire_gold_billet", has(item("billet_balefire_gold")))
                .save(consumer, id("parts/billet_flashlead"));

        rtgPelletRecipe(consumer, "pellet_rtg", "billet_pu238", "billet_pu238", "billet_pu238");
        rtgPelletRecipe(consumer, "pellet_rtg_radium", "billet_ra226", "billet_ra226", "billet_ra226");
        rtgPelletRecipe(consumer, "pellet_rtg_weak", "billet_u238", "billet_u238", "billet_pu238");
        rtgPelletRecipe(consumer, "pellet_rtg_strontium", "billet_sr90", "billet_sr90", "billet_sr90");
        rtgPelletRecipe(consumer, "pellet_rtg_cobalt", "billet_co60", "billet_co60", "billet_co60");
        rtgPelletRecipe(consumer, "pellet_rtg_actinium", "billet_actinium", "billet_actinium",
                "billet_actinium");
        rtgPelletRecipe(consumer, "pellet_rtg_polonium", "billet_polonium", "billet_polonium",
                "billet_polonium");
        rtgPelletRecipe(consumer, "pellet_rtg_lead", "billet_pb209", "billet_pb209", "billet_pb209");
        rtgPelletRecipe(consumer, "pellet_rtg_gold", "billet_au198", "billet_au198", "billet_au198");
        rtgPelletRecipe(consumer, "pellet_rtg_americium", "billet_am241", "billet_am241", "billet_am241");

        rtgDepletedPelletRecycling(consumer, "bismuth", "billet_bismuth", 3);
        rtgDepletedPelletRecycling(consumer, "lead", "ingot_lead", 2);
        rtgDepletedPelletRecycling(consumer, "mercury", "ingot_mercury", 2);
        rtgDepletedPelletRecycling(consumer, "neptunium", "billet_neptunium", 3);
        rtgDepletedPelletRecycling(consumer, "zirconium", "billet_zirconium", 3);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("ingot_gh336"))
                .pattern("GGG")
                .pattern("GGG")
                .pattern("GGG")
                .define('G', item("nugget_gh336"))
                .unlockedBy("has_gh336_nugget", has(item("nugget_gh336")))
                .save(consumer, id("parts/ingot_gh336_from_nuggets"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("nugget_gh336"), 9)
                .requires(item("ingot_gh336"))
                .unlockedBy("has_gh336_ingot", has(item("ingot_gh336")))
                .save(consumer, id("parts/nugget_gh336_from_ingot"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("billet_gh336"))
                .pattern("GGG")
                .pattern("GGG")
                .define('G', item("nugget_gh336"))
                .unlockedBy("has_gh336_nugget", has(item("nugget_gh336")))
                .save(consumer, id("parts/billet_gh336_from_nuggets"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("nugget_gh336"), 6)
                .requires(item("billet_gh336"))
                .unlockedBy("has_gh336_billet", has(item("billet_gh336")))
                .save(consumer, id("parts/nugget_gh336_from_billet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("billet_gh336"), 3)
                .pattern("GG")
                .define('G', item("ingot_gh336"))
                .unlockedBy("has_gh336_ingot", has(item("ingot_gh336")))
                .save(consumer, id("parts/billet_gh336_from_ingots"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("ingot_gh336"), 2)
                .requires(item("billet_gh336"), 3)
                .unlockedBy("has_gh336_billet", has(item("billet_gh336")))
                .save(consumer, id("parts/ingot_gh336_from_billets"));

        storageBlockPair(consumer, "block_fluorite", "fluorite");
        storageBlockPair(consumer, "block_niter", "niter");
        storageBlockPair(consumer, "block_steel", "ingot_steel");
        storageBlockPair(consumer, "block_sulfur", "sulfur");
        storageBlockPair(consumer, "block_titanium", "ingot_titanium");
        storageBlockPair(consumer, "block_tungsten", "ingot_tungsten");
        storageBlockPair(consumer, "block_uranium", "ingot_uranium");
        storageBlockPair(consumer, "block_thorium", "ingot_th232");
        storageBlockPair(consumer, "block_lead", "ingot_lead");
        storageBlockPair(consumer, "block_red_copper", "ingot_red_copper");
        storageBlockPair(consumer, "block_trinitite", "trinitite");
        storageBlockPair(consumer, "block_waste", "nuclear_waste");
        storageBlockPair(consumer, "block_beryllium", "ingot_beryllium");
        storageBlockPair(consumer, "block_schrabidium", "ingot_schrabidium");
        cokeBlockPair(consumer, "coal", "coke_coal", 0);
        cokeBlockPair(consumer, "lignite", "coke_lignite", 1);
        cokeBlockPair(consumer, "petroleum", "coke_petroleum", 2);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block("block_schrabidium_cluster"))
                .pattern("#S#")
                .pattern("SXS")
                .pattern("#S#")
                .define('#', item("ingot_schrabidium"))
                .define('S', item("ingot_starmetal"))
                .define('X', item("ingot_schrabidate"))
                .unlockedBy("has_schrabidate_ingot", has(item("ingot_schrabidate")))
                .save(consumer, id("parts/block_schrabidium_cluster"));

        storageBlockPair(consumer, "block_magnetized_tungsten", "ingot_magnetized_tungsten");
        storageBlockPair(consumer, "block_combine_steel", "ingot_combine_steel");
        storageBlockPair(consumer, "block_australium", "ingot_australium");
        storageBlockPair(consumer, "block_desh", "ingot_desh");
        storageBlockPair(consumer, "block_dineutronium", "ingot_dineutronium");
        storageBlockPair(consumer, "block_niobium", "ingot_niobium");
        storageBlockPair(consumer, "block_dura_steel", "ingot_dura_steel");
        storageBlockPair(consumer, "block_yellowcake", "powder_yellowcake");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block("block_starmetal"))
                .pattern("SSS")
                .pattern("SSS")
                .pattern("SSS")
                .define('S', item("ingot_starmetal"))
                .unlockedBy("has_starmetal_ingot", has(item("ingot_starmetal")))
                .save(consumer, id("parts/block_starmetal"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("ingot_starmetal"), 9)
                .requires(block("block_starmetal"))
                .unlockedBy("has_starmetal_block", has(block("block_starmetal")))
                .save(consumer, id("parts/ingot_starmetal_from_block"));

        storageBlockPair(consumer, "block_u233", "ingot_u233");
        storageBlockPair(consumer, "block_u235", "ingot_u235");
        storageBlockPair(consumer, "block_u238", "ingot_u238");
        storageBlockPair(consumer, "block_uranium_fuel", "ingot_uranium_fuel");
        storageBlockPair(consumer, "block_neptunium", "ingot_neptunium");
        storageBlockPair(consumer, "block_polonium", "ingot_polonium");
        storageBlockPair(consumer, "block_plutonium", "ingot_plutonium");
        storageBlockPair(consumer, "block_pu238", "ingot_pu238");
        storageBlockPair(consumer, "block_pu239", "ingot_pu239");
        storageBlockPair(consumer, "block_pu240", "ingot_pu240");
        storageBlockPair(consumer, "block_mox_fuel", "ingot_mox_fuel");
        storageBlockPair(consumer, "block_plutonium_fuel", "ingot_plutonium_fuel");
        storageBlockPair(consumer, "block_thorium_fuel", "ingot_thorium_fuel");
        storageBlockPair(consumer, "block_solinium", "ingot_solinium");
        storageBlockPair(consumer, "block_schrabidium_fuel", "ingot_schrabidium_fuel");
        storageBlockPair(consumer, "block_lithium", "lithium");
        storageBlockPair(consumer, "block_white_phosphorus", "ingot_phosphorus");
        storageBlockPair(consumer, "block_red_phosphorus", "powder_fire");
        storageBlockPair(consumer, "block_insulator", "plate_polymer");
        storageBlockPair(consumer, "block_asbestos", "ingot_asbestos");
        storageBlockPair(consumer, "block_fiberglass", "ingot_fiberglass");
        storageBlockPair(consumer, "block_cobalt", "ingot_cobalt");

        nuggetIngotPair(consumer, "ingot_plutonium", "nugget_plutonium");
        nuggetIngotPair(consumer, "ingot_pu238", "nugget_pu238");
        nuggetIngotPair(consumer, "ingot_pu239", "nugget_pu239");
        nuggetIngotPair(consumer, "ingot_pu240", "nugget_pu240");
        nuggetIngotPair(consumer, "ingot_th232", "nugget_th232");
        nuggetIngotPair(consumer, "ingot_uranium", "nugget_uranium");
        nuggetIngotPair(consumer, "ingot_u233", "nugget_u233");
        nuggetIngotPair(consumer, "ingot_u235", "nugget_u235");
        nuggetIngotPair(consumer, "ingot_u238", "nugget_u238");
        nuggetIngotPair(consumer, "ingot_neptunium", "nugget_neptunium");
        nuggetIngotPair(consumer, "ingot_polonium", "nugget_polonium");
        nuggetIngotPair(consumer, "ingot_beryllium", "nugget_beryllium");
        nuggetIngotPair(consumer, "ingot_schrabidium", "nugget_schrabidium");
        nuggetIngotPair(consumer, "ingot_uranium_fuel", "nugget_uranium_fuel");
        nuggetIngotPair(consumer, "ingot_thorium_fuel", "nugget_thorium_fuel");
        nuggetIngotPair(consumer, "ingot_plutonium_fuel", "nugget_plutonium_fuel");
        nuggetIngotPair(consumer, "ingot_mox_fuel", "nugget_mox_fuel");
        nuggetIngotPair(consumer, "ingot_schrabidium_fuel", "nugget_schrabidium_fuel");
        nuggetIngotPair(consumer, "ingot_hes", "nugget_hes");
        nuggetIngotPair(consumer, "ingot_les", "nugget_les");
        nuggetIngotPair(consumer, "ingot_australium", "nugget_australium");
        nuggetIngotPair(consumer, "ingot_osmiridium", "nugget_osmiridium");
        nuggetIngotPair(consumer, "ingot_arsenic", "nugget_arsenic");
        nuggetIngotPair(consumer, "ingot_dineutronium", "nugget_dineutronium");
        nuggetIngotPair(consumer, "ingot_niobium", "nugget_niobium");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.POWDER_FERTILIZER.get(), 4)
                .requires(forgeTag("dusts/calcium"))
                .requires(forgeTag("dusts/red_phosphorus"))
                .requires(forgeTag("dusts/saltpeter"))
                .requires(forgeTag("dusts/sulfur"))
                .unlockedBy("has_calcium_dust", has(forgeTag("dusts/calcium")))
                .save(consumer, id("parts/powder_fertilizer_from_calcium"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.POWDER_FERTILIZER.get(), 4)
                .requires(forgeTag("any/ash"))
                .requires(forgeTag("dusts/red_phosphorus"))
                .requires(forgeTag("dusts/saltpeter"))
                .requires(forgeTag("dusts/sulfur"))
                .unlockedBy("has_ash", has(forgeTag("any/ash")))
                .save(consumer, id("parts/powder_fertilizer_from_ash"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.POWDER_THERMITE.get(), 4)
                .requires(Ingredient.of(forgeTag("dusts/iron")), 3)
                .requires(forgeTag("dusts/aluminium"))
                .unlockedBy("has_iron_dust", has(forgeTag("dusts/iron")))
                .save(consumer, id("parts/powder_thermite"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.PELLET_GAS.get(), 2)
                .requires(Items.WATER_BUCKET)
                .requires(forgeTag("dusts/glowstone"))
                .requires(forgeTag("plates/steel"))
                .unlockedBy("has_steel_plate", has(forgeTag("plates/steel")))
                .save(consumer, id("parts/pellet_gas"));
        itemCompactPair(consumer, "powder_steel", "powder_steel_tiny", "powder_steel_from_tiny",
                "powder_steel_tiny_from_powder");
        itemCompactPair(consumer, "powder_lithium", "powder_lithium_tiny", "powder_lithium_from_tiny",
                "powder_lithium_tiny_from_powder");
        itemCompactPair(consumer, "powder_cobalt", "powder_cobalt_tiny", "powder_cobalt_from_tiny",
                "powder_cobalt_tiny_from_powder");
        itemCompactPair(consumer, "powder_neodymium", "powder_neodymium_tiny", "powder_neodymium_from_tiny",
                "powder_neodymium_tiny_from_powder");
        itemCompactPair(consumer, "powder_niobium", "powder_niobium_tiny", "powder_niobium_from_tiny",
                "powder_niobium_tiny_from_powder");
        itemCompactPair(consumer, "powder_cerium", "powder_cerium_tiny", "powder_cerium_from_tiny",
                "powder_cerium_tiny_from_powder");
        itemCompactPair(consumer, "powder_lanthanium", "powder_lanthanium_tiny", "powder_lanthanium_from_tiny",
                "powder_lanthanium_tiny_from_powder");
        itemCompactPair(consumer, "powder_actinium", "powder_actinium_tiny", "powder_actinium_from_tiny",
                "powder_actinium_tiny_from_powder");
        itemCompactPair(consumer, "powder_meteorite", "powder_meteorite_tiny", "powder_meteorite_from_tiny",
                "powder_meteorite_tiny_from_powder");
        nuggetIngotPair(consumer, "ingot_solinium", "nugget_solinium");
        itemCompactPair(consumer, "nuclear_waste", "nuclear_waste_tiny", "nuclear_waste_from_tiny",
                "nuclear_waste_tiny_from_waste");
        itemCompactPair(consumer, "egg_balefire", "egg_balefire_shard", "egg_balefire_from_shards",
                "egg_balefire_shard_from_egg");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("egg_balefire_shard"))
                .pattern("##")
                .pattern("##")
                .define('#', item("powder_balefire"))
                .unlockedBy("has_balefire_powder", has(item("powder_balefire")))
                .save(consumer, id("parts/egg_balefire_shard_from_powder_balefire"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("egg_balefire_shard"))
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', item("cell_balefire"))
                .unlockedBy("has_balefire_cell", has(item("cell_balefire")))
                .save(consumer, id("parts/egg_balefire_shard_from_cell_balefire"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("ingot_schrabidium_fuel"))
                .requires(Ingredient.of(legacyOreTag("nuggetSchrabidium")), 3)
                .requires(Ingredient.of(legacyOreTag("nuggetNeptunium237")), 3)
                .requires(item("nugget_beryllium"), 3)
                .unlockedBy("has_schrabidium_nugget", has(legacyOreTag("nuggetSchrabidium")))
                .save(consumer, id("parts/ingot_schrabidium_fuel_from_fuel_mix"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("ingot_hes"))
                .requires(Ingredient.of(legacyOreTag("nuggetSchrabidium")), 5)
                .requires(Ingredient.of(legacyOreTag("nuggetNeptunium237")), 2)
                .requires(item("nugget_beryllium"), 2)
                .unlockedBy("has_schrabidium_nugget", has(legacyOreTag("nuggetSchrabidium")))
                .save(consumer, id("parts/ingot_hes_from_fuel_mix"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("ingot_les"))
                .requires(Ingredient.of(legacyOreTag("nuggetSchrabidium")))
                .requires(Ingredient.of(legacyOreTag("nuggetNeptunium237")), 4)
                .requires(item("nugget_beryllium"), 4)
                .unlockedBy("has_neptunium_nugget", has(legacyOreTag("nuggetNeptunium237")))
                .save(consumer, id("parts/ingot_les_from_fuel_mix"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("ingot_pu_mix"))
                .requires(Ingredient.of(legacyOreTag("nuggetPlutonium239")), 6)
                .requires(Ingredient.of(legacyOreTag("nuggetPlutonium240")), 3)
                .unlockedBy("has_pu239_nugget", has(legacyOreTag("nuggetPlutonium239")))
                .save(consumer, id("parts/ingot_pu_mix_from_nuggets"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("ingot_pu_mix"))
                .requires(Ingredient.of(legacyOreTag("tinyPu239")), 6)
                .requires(Ingredient.of(legacyOreTag("tinyPu240")), 3)
                .unlockedBy("has_pu239_nugget", has(legacyOreTag("tinyPu239")))
                .save(consumer, id("parts/ingot_pu_mix_from_tiny"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("ingot_am_mix"))
                .requires(Ingredient.of(legacyOreTag("nuggetAmericium241")), 3)
                .requires(Ingredient.of(legacyOreTag("nuggetAmericium242")), 6)
                .unlockedBy("has_am241_nugget", has(legacyOreTag("nuggetAmericium241")))
                .save(consumer, id("parts/ingot_am_mix_from_nuggets"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("ingot_am_mix"))
                .requires(Ingredient.of(legacyOreTag("tinyAm241")), 3)
                .requires(Ingredient.of(legacyOreTag("tinyAm242")), 6)
                .unlockedBy("has_am241_tiny", has(legacyOreTag("tinyAm241")))
                .save(consumer, id("parts/ingot_am_mix_from_tiny"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("ball_fireclay"), 4)
                .requires(Items.CLAY_BALL, 3)
                .requires(Ingredient.of(legacyOreTag("dustAluminum")))
                .unlockedBy("has_aluminium_dust", has(legacyOreTag("dustAluminum")))
                .save(consumer, id("parts/ball_fireclay_from_aluminium_dust"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("ball_fireclay"), 4)
                .requires(Items.CLAY_BALL, 3)
                .requires(Ingredient.of(legacyOreTag("oreAluminum")))
                .unlockedBy("has_aluminium_ore", has(legacyOreTag("oreAluminum")))
                .save(consumer, id("parts/ball_fireclay_from_aluminium_ore"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("ball_fireclay"), 4)
                .requires(Items.CLAY_BALL, 2)
                .requires(block("stone_resource_limestone"))
                .requires(Ingredient.of(legacyOreTag("sand")))
                .unlockedBy("has_limestone", has(block("stone_resource_limestone")))
                .save(consumer, id("parts/ball_fireclay_from_limestone_sand"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("nitra"))
                .pattern("##")
                .pattern("##")
                .define('#', item("nitra_small"))
                .unlockedBy("has_small_nitra", has(item("nitra_small")))
                .save(consumer, id("parts/nitra_from_small"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item("nitra_small"), 4)
                .requires(item("nitra"))
                .unlockedBy("has_nitra", has(item("nitra")))
                .save(consumer, id("parts/nitra_small_from_nitra"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("ammo_container_alt"))
                .pattern("##")
                .pattern("##")
                .define('#', item("nitra"))
                .unlockedBy("has_nitra", has(item("nitra")))
                .save(consumer, id("weapon/ammo_container_alt"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block("glass_polarized"), 4)
                .pattern("##")
                .pattern("##")
                .define('#', item("part_generic_glass_polarized"))
                .unlockedBy("has_polarized_lens", has(item("part_generic_glass_polarized")))
                .save(consumer, id("blocks/glass_polarized"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block("block_scrap"))
                .pattern("##")
                .pattern("##")
                .define('#', item("scrap"))
                .unlockedBy("has_scrap", has(item("scrap")))
                .save(consumer, id("parts/block_scrap_from_scrap"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block("block_scrap"))
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', item("dust"))
                .unlockedBy("has_dust", has(item("dust")))
                .save(consumer, id("parts/block_scrap_from_dust"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block("block_meteor_cobble"))
                .pattern("##")
                .pattern("##")
                .define('#', item("fragment_meteorite"))
                .unlockedBy("has_meteorite_fragment", has(item("fragment_meteorite")))
                .save(consumer, id("parts/block_meteor_cobble_from_fragments"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block("block_meteor_broken"))
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', item("fragment_meteorite"))
                .unlockedBy("has_meteorite_fragment", has(item("fragment_meteorite")))
                .save(consumer, id("parts/block_meteor_broken_from_fragments"));

        itemCompactPair(consumer, "powder_paleogenite", "powder_paleogenite_tiny",
                "powder_paleogenite_from_tiny", "powder_paleogenite_tiny_from_powder");

        byproductCompact(consumer, "ore_byproduct_b_iron", "powder_iron", 1);
        byproductCompact(consumer, "ore_byproduct_b_copper", "powder_copper", 1);
        byproductCompact(consumer, "ore_byproduct_b_lithium", "powder_lithium", 1);
        byproductCompact(consumer, "ore_byproduct_b_silicon", "nugget_silicon", 3);
        byproductCompact(consumer, "ore_byproduct_b_lead", "powder_lead", 1);
        byproductCompact(consumer, "ore_byproduct_b_titanium", "powder_titanium", 1);
        byproductCompact(consumer, "ore_byproduct_b_aluminium", "powder_aluminium", 1);
        byproductCompact(consumer, "ore_byproduct_b_sulfur", "sulfur", 1);
        byproductCompact(consumer, "ore_byproduct_b_calcium", "powder_calcium", 1);
        byproductCompact(consumer, "ore_byproduct_b_bismuth", "powder_bismuth", 1);
        byproductCompact(consumer, "ore_byproduct_b_radium", "powder_ra226", 1);
        byproductCompact(consumer, "ore_byproduct_b_technetium", "billet_technetium", 1);
        byproductCompact(consumer, "ore_byproduct_b_polonium", "billet_polonium", 1);
        byproductCompact(consumer, "ore_byproduct_b_uranium", "powder_uranium", 1);
    }

    private static void storageBlockPair(Consumer<FinishedRecipe> consumer, String blockName, String itemName) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block(blockName))
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', item(itemName))
                .unlockedBy("has_" + itemName, has(item(itemName)))
                .save(consumer, id("parts/" + blockName));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item(itemName), 9)
                .requires(block(blockName))
                .unlockedBy("has_" + blockName, has(block(blockName)))
                .save(consumer, id("parts/" + itemName + "_from_" + blockName));
    }

    private static void cokeBlockPair(Consumer<FinishedRecipe> consumer, String suffix, String itemName, int variant) {
        JsonObject compact = new JsonObject();
        compact.addProperty("category", "building");
        JsonArray pattern = new JsonArray();
        pattern.add("###");
        pattern.add("###");
        pattern.add("###");
        compact.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("#", ingredientItem(item(itemName)));
        compact.add("key", key);
        JsonObject result = ingredientItem(block("block_coke"));
        result.addProperty("count", 1);
        result.addProperty("nbt", "{hbmLegacyVariant:" + variant + "}");
        compact.add("result", result);
        compact.addProperty("show_notification", true);
        consumer.accept(finishedRecipe(id("parts/block_coke_" + suffix), compact,
                ModRecipes.LEGACY_NBT_SHAPED.get()));

        JsonObject unpack = new JsonObject();
        unpack.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(ingredientNbtItem(block("block_coke"), "{hbmLegacyVariant:" + variant + "}"));
        unpack.add("ingredients", ingredients);
        JsonObject unpackResult = ingredientItem(item(itemName));
        unpackResult.addProperty("count", 9);
        unpack.add("result", unpackResult);
        consumer.accept(finishedRecipe(id("parts/" + itemName + "_from_block_coke_" + suffix), unpack,
                RecipeSerializer.SHAPELESS_RECIPE));
    }

    private static void nuggetIngotPair(Consumer<FinishedRecipe> consumer, String ingotName, String nuggetName) {
        itemCompactPair(consumer, ingotName, nuggetName, ingotName + "_from_nuggets", nuggetName + "_from_ingot");
    }

    private static void itemCompactPair(Consumer<FinishedRecipe> consumer, String oneName, String nineName,
            String compactRecipeName, String unpackRecipeName) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item(oneName))
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', item(nineName))
                .unlockedBy("has_" + nineName, has(item(nineName)))
                .save(consumer, id("parts/" + compactRecipeName));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item(nineName), 9)
                .requires(item(oneName))
                .unlockedBy("has_" + oneName, has(item(oneName)))
                .save(consumer, id("parts/" + unpackRecipeName));
    }

    private static void byproductCompact(Consumer<FinishedRecipe> consumer, String byproductName, String outputName,
            int outputCount) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item(outputName), outputCount)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', item(byproductName))
                .unlockedBy("has_" + byproductName, has(item(byproductName)))
                .save(consumer, id("parts/" + outputName + "_from_" + byproductName));
    }

    private static void rtgPelletRecipe(Consumer<FinishedRecipe> consumer, String output, String... billets) {
        ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item(output));
        for (String billet : billets) {
            builder.requires(item(billet));
        }
        builder.requires(forgeTag("plates/iron"))
                .unlockedBy("has_" + billets[0], has(item(billets[0])))
                .save(consumer, id("parts/" + output));
    }

    private static void rtgDepletedPelletRecycling(Consumer<FinishedRecipe> consumer, String material, String output,
            int count) {
        ItemLike depletedPellet = item("pellet_rtg_depleted_" + material);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item(output), count)
                .requires(depletedPellet)
                .unlockedBy("has_depleted_rtg_pellet_" + material, has(depletedPellet))
                .save(consumer, id("parts/rtg_depleted_" + material + "_recycling"));
    }

    private static void legacyArmorModuleRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike ductTape = item("ducttape");
        ItemLike basicCircuit = legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8);
        ItemLike advancedCircuit = legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 9);
        ItemLike bismoidCircuit = legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 11);
        ItemLike vacuumTube = legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 0);
        ItemLike denseMingradeWire = legacyMetaItem(LegacyMetaItemMappings.WIRE_DENSE, 31);
        ItemLike fineCopperWire = legacyMetaItem(LegacyMetaItemMappings.WIRE_FINE, 2_900);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ModItems.CLADDING_PAINT.get())
                .requires(Ingredient.of(forgeTag("nuggets/lead")), 4)
                .requires(Items.CLAY_BALL)
                .requires(Items.GLASS_BOTTLE)
                .unlockedBy("has_lead_nugget", has(forgeTag("nuggets/lead")))
                .save(consumer, id("armor_modules/cladding_paint"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLADDING_RUBBER.get())
                .pattern("RCR")
                .pattern("CDC")
                .pattern("RCR")
                .define('R', forgeTag("ingots/any_rubber"))
                .define('C', forgeTag("dusts/coal"))
                .define('D', ductTape)
                .unlockedBy("has_ducttape", has(ductTape))
                .save(consumer, id("armor_modules/cladding_rubber"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLADDING_LEAD.get())
                .pattern("DPD")
                .pattern("PRP")
                .pattern("DPD")
                .define('D', ductTape)
                .define('P', forgeTag("plates/lead"))
                .define('R', ModItems.CLADDING_RUBBER.get())
                .unlockedBy("has_cladding_rubber", has(ModItems.CLADDING_RUBBER.get()))
                .save(consumer, id("armor_modules/cladding_lead"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLADDING_DESH.get())
                .pattern("DPD")
                .pattern("PRP")
                .pattern("DPD")
                .define('D', ductTape)
                .define('P', item("plate_desh"))
                .define('R', ModItems.CLADDING_LEAD.get())
                .unlockedBy("has_cladding_lead", has(ModItems.CLADDING_LEAD.get()))
                .save(consumer, id("armor_modules/cladding_desh"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLADDING_GHIORSIUM.get())
                .pattern("DPD")
                .pattern("PRP")
                .pattern("DPD")
                .define('D', ductTape)
                .define('P', item("ingot_gh336"))
                .define('R', ModItems.CLADDING_DESH.get())
                .unlockedBy("has_cladding_desh", has(ModItems.CLADDING_DESH.get()))
                .save(consumer, id("armor_modules/cladding_ghiorsium"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLADDING_OBSIDIAN.get())
                .pattern("OOO")
                .pattern("PDP")
                .pattern("OOO")
                .define('O', Blocks.OBSIDIAN)
                .define('P', forgeTag("plates/steel"))
                .define('D', ductTape)
                .unlockedBy("has_obsidian", has(Blocks.OBSIDIAN))
                .save(consumer, id("armor_modules/cladding_obsidian"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLADDING_IRON.get())
                .pattern("OOO")
                .pattern("PDP")
                .pattern("OOO")
                .define('O', forgeTag("plates/iron"))
                .define('P', item("plate_polymer"))
                .define('D', ductTape)
                .unlockedBy("has_plate_polymer", has(item("plate_polymer")))
                .save(consumer, id("armor_modules/cladding_iron"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.INSERT_STEEL.get())
                .pattern("DPD")
                .pattern("PSP")
                .pattern("DPD")
                .define('D', ductTape)
                .define('P', forgeTag("plates/iron"))
                .define('S', block("block_steel"))
                .unlockedBy("has_steel_block", has(block("block_steel")))
                .save(consumer, id("armor_modules/insert_steel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.INSERT_DU.get())
                .pattern("DPD")
                .pattern("PSP")
                .pattern("DPD")
                .define('D', ductTape)
                .define('P', forgeTag("plates/iron"))
                .define('S', block("block_u238"))
                .unlockedBy("has_u238_block", has(block("block_u238")))
                .save(consumer, id("armor_modules/insert_du"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.INSERT_GHIORSIUM.get())
                .pattern("DPD")
                .pattern("PSP")
                .pattern("DPD")
                .define('D', ductTape)
                .define('P', forgeTag("ingots/gh336"))
                .define('S', forgeTag("ingots/u238"))
                .unlockedBy("has_gh336_ingot", has(forgeTag("ingots/gh336")))
                .save(consumer, id("armor_modules/insert_ghiorsium"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.INSERT_POLONIUM.get())
                .pattern("DPD")
                .pattern("PSP")
                .pattern("DPD")
                .define('D', ductTape)
                .define('P', forgeTag("plates/iron"))
                .define('S', block("block_polonium"))
                .unlockedBy("has_polonium_block", has(block("block_polonium")))
                .save(consumer, id("armor_modules/insert_polonium"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.INSERT_ERA.get())
                .pattern("DPD")
                .pattern("PSP")
                .pattern("DPD")
                .define('D', ductTape)
                .define('P', forgeTag("plates/iron"))
                .define('S', ModItems.INGOT_SEMTEX.get())
                .unlockedBy("has_semtex_bar", has(ModItems.INGOT_SEMTEX.get()))
                .save(consumer, id("armor_modules/insert_era"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.INSERT_KEVLAR.get())
                .pattern("KIK")
                .pattern("IDI")
                .pattern("KIK")
                .define('K', item("plate_kevlar"))
                .define('I', forgeTag("ingots/any_rubber"))
                .define('D', ductTape)
                .unlockedBy("has_plate_kevlar", has(item("plate_kevlar")))
                .save(consumer, id("armor_modules/insert_kevlar"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.INSERT_SAPI.get())
                .pattern("PKP")
                .pattern("DPD")
                .pattern("PKP")
                .define('P', forgeTag("ingots/any_plastic"))
                .define('K', ModItems.INSERT_KEVLAR.get())
                .define('D', ductTape)
                .unlockedBy("has_insert_kevlar", has(ModItems.INSERT_KEVLAR.get()))
                .save(consumer, id("armor_modules/insert_sapi"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.INSERT_ESAPI.get())
                .pattern("PKP")
                .pattern("DSD")
                .pattern("PKP")
                .define('P', forgeTag("ingots/any_plastic"))
                .define('K', ModItems.INSERT_SAPI.get())
                .define('D', ductTape)
                .define('S', forgeTag("plates/weapon_steel"))
                .unlockedBy("has_insert_sapi", has(ModItems.INSERT_SAPI.get()))
                .save(consumer, id("armor_modules/insert_esapi"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.INSERT_XSAPI.get())
                .pattern("PKP")
                .pattern("DSD")
                .pattern("PKP")
                .define('P', forgeTag("ingots/asbestos"))
                .define('K', ModItems.INSERT_ESAPI.get())
                .define('D', ductTape)
                .define('S', forgeTag("plates/saturnite"))
                .unlockedBy("has_insert_esapi", has(ModItems.INSERT_ESAPI.get()))
                .save(consumer, id("armor_modules/insert_xsapi"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.INSERT_YHARONITE.get())
                .pattern("YIY")
                .pattern("IYI")
                .pattern("YIY")
                .define('Y', item("billet_yharonite"))
                .define('I', ModItems.INSERT_DU.get())
                .unlockedBy("has_billet_yharonite", has(item("billet_yharonite")))
                .save(consumer, id("armor_modules/insert_yharonite"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.AUSTRALIUM_III.get())
                .pattern("WSW")
                .pattern("PAP")
                .pattern("SPS")
                .define('W', forgeTag("dense_wires/gold"))
                .define('S', forgeTag("welded_plates/steel"))
                .define('P', forgeTag("ingots/any_plastic"))
                .define('A', forgeTag("ingots/australium"))
                .unlockedBy("has_australium_ingot", has(forgeTag("ingots/australium")))
                .save(consumer, id("armor_modules/australium_iii"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SERVO_SET.get())
                .pattern("MBM")
                .pattern("PBP")
                .pattern("MBM")
                .define('M', ModItems.MOTOR.get())
                .define('B', forgeTag("bolts/steel"))
                .define('P', forgeTag("plates/iron"))
                .unlockedBy("has_motor", has(ModItems.MOTOR.get()))
                .save(consumer, id("armor_modules/servo_set"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SERVO_SET_DESH.get())
                .pattern("MBM")
                .pattern("PSP")
                .pattern("MBM")
                .define('M', item("motor_desh"))
                .define('B', forgeTag("bolts/dura_steel"))
                .define('P', item("plate_desh"))
                .define('S', ModItems.SERVO_SET.get())
                .unlockedBy("has_servo_set", has(ModItems.SERVO_SET.get()))
                .save(consumer, id("armor_modules/servo_set_desh"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ATTACHMENT_MASK.get())
                .pattern("DID")
                .pattern("IGI")
                .pattern(" F ")
                .define('D', ductTape)
                .define('I', forgeTag("ingots/any_rubber"))
                .define('G', forgeTag("glass_panes"))
                .define('F', forgeTag("plates/iron"))
                .unlockedBy("has_ducttape", has(ductTape))
                .save(consumer, id("armor_modules/attachment_mask"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ATTACHMENT_MASK_MONO.get())
                .pattern(" D ")
                .pattern("DID")
                .pattern(" F ")
                .define('D', ductTape)
                .define('I', forgeTag("ingots/any_rubber"))
                .define('F', forgeTag("plates/iron"))
                .unlockedBy("has_ducttape", has(ductTape))
                .save(consumer, id("armor_modules/attachment_mask_mono"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.PADS_RUBBER.get())
                .pattern("P P")
                .pattern("IDI")
                .pattern("P P")
                .define('P', forgeTag("ingots/any_rubber"))
                .define('I', forgeTag("plates/iron"))
                .define('D', ductTape)
                .unlockedBy("has_ducttape", has(ductTape))
                .save(consumer, id("armor_modules/pads_rubber"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.PADS_SLIME.get())
                .pattern("SPS")
                .pattern("DSD")
                .pattern("SPS")
                .define('S', Items.SLIME_BALL)
                .define('P', ModItems.PADS_RUBBER.get())
                .define('D', ductTape)
                .unlockedBy("has_pads_rubber", has(ModItems.PADS_RUBBER.get()))
                .save(consumer, id("armor_modules/pads_slime"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.PADS_STATIC.get())
                .pattern("CDC")
                .pattern("ISI")
                .pattern("CDC")
                .define('C', forgeTag("ingots/copper"))
                .define('D', ductTape)
                .define('I', forgeTag("ingots/any_rubber"))
                .define('S', ModItems.PADS_SLIME.get())
                .unlockedBy("has_pads_slime", has(ModItems.PADS_SLIME.get()))
                .save(consumer, id("armor_modules/pads_static"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ARMOR_BATTERY.get())
                .pattern("PWP")
                .pattern("PCP")
                .pattern("PWP")
                .define('P', forgeTag("plates/steel"))
                .define('W', denseMingradeWire)
                .define('C', legacyBatteryPack(7))
                .unlockedBy("has_gold_capacitor", has(legacyBatteryPack(7)))
                .save(consumer, id("armor_modules/armor_battery"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ARMOR_BATTERY_MK2.get())
                .pattern("PWP")
                .pattern("PCP")
                .pattern("PWP")
                .define('P', forgeTag("ingots/any_plastic"))
                .define('W', denseMingradeWire)
                .define('C', legacyBatteryPack(8))
                .unlockedBy("has_niobium_capacitor", has(legacyBatteryPack(8)))
                .save(consumer, id("armor_modules/armor_battery_mk2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ARMOR_BATTERY_MK3.get())
                .pattern("PWP")
                .pattern("PCP")
                .pattern("PWP")
                .define('P', forgeTag("plates/gold"))
                .define('W', denseMingradeWire)
                .define('C', legacyBatteryPack(9))
                .unlockedBy("has_tantalum_capacitor", has(legacyBatteryPack(9)))
                .save(consumer, id("armor_modules/armor_battery_mk3"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.JETPACK_FLY.get())
                .pattern("ACA")
                .pattern("TLT")
                .pattern("D D")
                .define('A', ModItems.ALUMINIUM_PLATE.get())
                .define('C', basicCircuit)
                .define('T', item("tank_steel"))
                .define('L', Items.LEATHER)
                .define('D', item("thruster_small"))
                .unlockedBy("has_thruster_small", has(item("thruster_small")))
                .save(consumer, id("armor_modules/jetpack_fly"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.JETPACK_BREAK.get())
                .pattern("ICI")
                .pattern("TJT")
                .pattern("I I")
                .define('I', item("plate_polymer"))
                .define('C', basicCircuit)
                .define('T', item("ingot_dura_steel"))
                .define('J', ModItems.JETPACK_FLY.get())
                .unlockedBy("has_jetpack_fly", has(ModItems.JETPACK_FLY.get()))
                .save(consumer, id("armor_modules/jetpack_break"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.JETPACK_VECTOR.get())
                .pattern("TCT")
                .pattern("MJM")
                .pattern("B B")
                .define('T', item("tank_steel"))
                .define('C', advancedCircuit)
                .define('M', ModItems.MOTOR.get())
                .define('J', ModItems.JETPACK_BREAK.get())
                .define('B', forgeTag("bolts/dura_steel"))
                .unlockedBy("has_jetpack_break", has(ModItems.JETPACK_BREAK.get()))
                .save(consumer, id("armor_modules/jetpack_vector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.JETPACK_BOOST.get())
                .pattern("PCP")
                .pattern("DJD")
                .pattern("PAP")
                .define('P', forgeTag("plates/saturnite"))
                .define('C', advancedCircuit)
                .define('D', forgeTag("ingots/desh"))
                .define('J', ModItems.JETPACK_VECTOR.get())
                .define('A', forgeTag("cast_plates/copper"))
                .unlockedBy("has_jetpack_vector", has(ModItems.JETPACK_VECTOR.get()))
                .save(consumer, id("armor_modules/jetpack_boost"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HORSESHOE_MAGNET.get())
                .pattern("L L")
                .pattern("I I")
                .pattern("ILI")
                .define('L', ModItems.LODESTONE.get())
                .define('I', forgeTag("ingots/iron"))
                .unlockedBy("has_lodestone", has(ModItems.LODESTONE.get()))
                .save(consumer, id("armor_modules/horseshoe_magnet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.INDUSTRIAL_MAGNET.get())
                .pattern("SMS")
                .pattern(" B ")
                .pattern("SMS")
                .define('S', forgeTag("ingots/steel"))
                .define('M', ModItems.HORSESHOE_MAGNET.get())
                .define('B', denseMingradeWire)
                .unlockedBy("has_horseshoe_magnet", has(ModItems.HORSESHOE_MAGNET.get()))
                .save(consumer, id("armor_modules/industrial_magnet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BATHWATER_MK2.get())
                .pattern("MWM")
                .pattern("WBW")
                .pattern("MWM")
                .define('M', ModItems.BOTTLE_MERCURY.get())
                .define('W', item("nuclear_waste"))
                .define('B', ModItems.BATHWATER.get())
                .unlockedBy("has_bathwater", has(ModItems.BATHWATER.get()))
                .save(consumer, id("armor_modules/bathwater_mk2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BACK_TESLA.get())
                .pattern("DGD")
                .pattern("GTG")
                .pattern("DGD")
                .define('D', ductTape)
                .define('G', forgeTag("wires/gold"))
                .define('T', ModBlocks.TESLA.get())
                .unlockedBy("has_tesla", has(ModBlocks.TESLA.get()))
                .save(consumer, id("armor_modules/back_tesla"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MEDAL_LIQUIDATOR.get())
                .pattern("GBG")
                .pattern("BFB")
                .pattern("GBG")
                .define('G', forgeTag("nuggets/au198"))
                .define('B', forgeTag("ingots/boron"))
                .define('F', item("debris_fuel"))
                .unlockedBy("has_debris_fuel", has(item("debris_fuel")))
                .save(consumer, id("armor_modules/medal_liquidator"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.INK.get())
                .pattern("FPF")
                .pattern("PIP")
                .pattern("FPF")
                .define('F', Ingredient.of(
                        Items.POPPY,
                        Items.BLUE_ORCHID,
                        Items.ALLIUM,
                        Items.AZURE_BLUET,
                        Items.RED_TULIP,
                        Items.ORANGE_TULIP,
                        Items.WHITE_TULIP,
                        Items.PINK_TULIP,
                        Items.OXEYE_DAISY))
                .define('P', ModItems.ARMOR_POLISH.get())
                .define('I', forgeTag("dyes/black"))
                .unlockedBy("has_armor_polish", has(ModItems.ARMOR_POLISH.get()))
                .save(consumer, id("armor_modules/ink"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BLACK_DIAMOND.get())
                .pattern("NIN")
                .pattern("IGI")
                .pattern("NIN")
                .define('N', forgeTag("nuggets/au198"))
                .define('I', ModItems.INK.get())
                .define('G', forgeTag("gems/volcanic"))
                .unlockedBy("has_volcanic_gem", has(forgeTag("gems/volcanic")))
                .save(consumer, id("armor_modules/black_diamond"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.PROTECTION_CHARM.get())
                .pattern(" M ")
                .pattern("MDM")
                .pattern(" M ")
                .define('M', item("fragment_meteorite"))
                .define('D', forgeTag("gems/diamond"))
                .unlockedBy("has_meteorite_fragment", has(item("fragment_meteorite")))
                .save(consumer, id("armor_modules/protection_charm"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.METEOR_CHARM.get())
                .pattern(" M ")
                .pattern("MDM")
                .pattern(" M ")
                .define('M', item("fragment_meteorite"))
                .define('D', forgeTag("gems/volcanic"))
                .unlockedBy("has_volcanic_gem", has(forgeTag("gems/volcanic")))
                .save(consumer, id("armor_modules/meteor_charm"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ModItems.INJECTOR_5HTP.get())
                .requires(ModItems.FIVE_HTP.get())
                .requires(basicCircuit)
                .requires(forgeTag("plates/saturnite"))
                .unlockedBy("has_five_htp", has(ModItems.FIVE_HTP.get()))
                .save(consumer, id("armor_modules/injector_5htp"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ModItems.INJECTOR_KNIFE.get())
                .requires(ModItems.INJECTOR_5HTP.get())
                .requires(Items.IRON_SWORD)
                .unlockedBy("has_injector_5htp", has(ModItems.INJECTOR_5HTP.get()))
                .save(consumer, id("armor_modules/injector_knife"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SHACKLES.get())
                .pattern("CIC")
                .pattern("C C")
                .pattern("I I")
                .define('C', block("chain"))
                .define('I', item("ingot_chainsteel"))
                .unlockedBy("has_chainsteel_ingot", has(item("ingot_chainsteel")))
                .save(consumer, id("armor_modules/shackles"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.GAS_TESTER.get())
                .pattern("G")
                .pattern("C")
                .pattern("I")
                .define('G', forgeTag("plates/gold"))
                .define('C', vacuumTube)
                .define('I', forgeTag("plates/iron"))
                .unlockedBy("has_vacuum_tube", has(vacuumTube))
                .save(consumer, id("armor_modules/gas_tester"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DEFUSER_GOLD.get())
                .pattern("GPG")
                .pattern("PRP")
                .pattern("GPG")
                .define('G', Items.GUNPOWDER)
                .define('P', forgeTag("plates/gold"))
                .define('R', vanillaTag("music_discs"))
                .unlockedBy("has_music_disc", has(vanillaTag("music_discs")))
                .save(consumer, id("armor_modules/defuser_gold"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BALLISTIC_GAUNTLET.get())
                .pattern(" WS")
                .pattern("WRS")
                .pattern(" RS")
                .define('W', fineCopperWire)
                .define('R', item("ring_starmetal"))
                .define('S', forgeTag("plates/steel"))
                .unlockedBy("has_starmetal_ring", has(item("ring_starmetal")))
                .save(consumer, id("armor_modules/ballistic_gauntlet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.NEUTRINO_LENS.get())
                .pattern("PSP")
                .pattern("SCS")
                .pattern("PSP")
                .define('P', forgeTag("ingots/any_plastic"))
                .define('S', forgeTag("ingots/starmetal"))
                .define('C', bismoidCircuit)
                .unlockedBy("has_starmetal_ingot", has(forgeTag("ingots/starmetal")))
                .save(consumer, id("armor_modules/neutrino_lens"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.NIGHT_VISION.get())
                .pattern("P P")
                .pattern("GCG")
                .define('P', forgeTag("ingots/any_plastic"))
                .define('G', forgeTag("glass"))
                .define('C', basicCircuit)
                .unlockedBy("has_basic_circuit", has(basicCircuit))
                .save(consumer, id("armor_modules/night_vision"));
    }

    private static void legacyPartRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("shimmer_handle"))
                .pattern("GP")
                .pattern("GP")
                .pattern("GP")
                .define('G', forgeTag("plates/gold"))
                .define('P', forgeTag("ingots/any_plastic"))
                .unlockedBy("has_gold_plate", has(forgeTag("plates/gold")))
                .save(consumer, id("parts/shimmer_handle"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("shimmer_head"))
                .pattern("SSS")
                .pattern("DTD")
                .pattern("SSS")
                .define('S', forgeTag("ingots/steel"))
                .define('D', block("block_desh"))
                .define('T', block("block_tungsten"))
                .unlockedBy("has_desh_block", has(block("block_desh")))
                .save(consumer, id("parts/shimmer_head"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("shimmer_axe_head"))
                .pattern("PII")
                .pattern("PBB")
                .pattern("PII")
                .define('P', forgeTag("plates/steel"))
                .define('B', block("block_desh"))
                .define('I', forgeTag("ingots/tungsten"))
                .unlockedBy("has_desh_block", has(block("block_desh")))
                .save(consumer, id("parts/shimmer_axe_head"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.AMS_LENS.get())
                .pattern("PDP")
                .pattern("GDG")
                .pattern("PDP")
                .define('P', item("plate_dineutronium"))
                .define('D', Blocks.DIAMOND_BLOCK)
                .define('G', ModBlocks.REINFORCED_GLASS.get())
                .unlockedBy("has_reinforced_glass", has(ModBlocks.REINFORCED_GLASS.get()))
                .save(consumer, id("parts/ams_lens"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("bolt_spike"), 2)
                .pattern("BB")
                .pattern("B ")
                .pattern("B ")
                .define('B', forgeTag("bolts/steel"))
                .unlockedBy("has_steel_bolt", has(forgeTag("bolts/steel")))
                .save(consumer, id("parts/bolt_spike"));

        JsonObject scrapsSplit = new JsonObject();
        scrapsSplit.addProperty("type", id("scraps_split").toString());
        scrapsSplit.addProperty("category", "misc");
        consumer.accept(finishedCompatRecipe(id("parts/scraps_split"), scrapsSplit));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("sphere_steel"))
                .pattern("PIP")
                .pattern("I I")
                .pattern("PIP")
                .define('P', forgeTag("plates/steel"))
                .define('I', forgeTag("ingots/steel"))
                .unlockedBy("has_steel_plate", has(forgeTag("plates/steel")))
                .save(consumer, id("parts/sphere_steel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("turbine_tungsten"))
                .pattern("BBB")
                .pattern("BSB")
                .pattern("BBB")
                .define('B', item("blade_tungsten"))
                .define('S', forgeTag("ingots/dura_steel"))
                .unlockedBy("has_tungsten_blade", has(item("blade_tungsten")))
                .save(consumer, id("parts/turbine_tungsten"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("blade_titanium"), 2)
                .pattern("TP")
                .pattern("TP")
                .pattern("TT")
                .define('T', forgeTag("ingots/titanium"))
                .define('P', forgeTag("plates/titanium"))
                .unlockedBy("has_titanium_ingot", has(forgeTag("ingots/titanium")))
                .save(consumer, id("parts/blade_titanium"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("turbine_titanium"))
                .pattern("BBB")
                .pattern("BSB")
                .pattern("BBB")
                .define('B', item("blade_titanium"))
                .define('S', forgeTag("ingots/steel"))
                .unlockedBy("has_titanium_blade", has(item("blade_titanium")))
                .save(consumer, id("parts/turbine_titanium"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("flywheel_beryllium"))
                .pattern("IBI")
                .pattern("BTB")
                .pattern("IBI")
                .define('I', forgeTag("cast_plates/iron"))
                .define('B', forgeTag("storage_blocks/beryllium"))
                .define('T', forgeTag("pipes/dura_steel"))
                .unlockedBy("has_beryllium_block", has(forgeTag("storage_blocks/beryllium")))
                .save(consumer, id("parts/flywheel_beryllium"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("fins_flat"))
                .pattern("IP")
                .pattern("PP")
                .pattern("IP")
                .define('P', forgeTag("plates/steel"))
                .define('I', forgeTag("ingots/steel"))
                .unlockedBy("has_steel_plate", has(forgeTag("plates/steel")))
                .save(consumer, id("parts/fins_flat"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("fins_small_steel"))
                .pattern(" PP")
                .pattern("PII")
                .pattern(" PP")
                .define('P', forgeTag("plates/steel"))
                .define('I', forgeTag("ingots/steel"))
                .unlockedBy("has_steel_plate", has(forgeTag("plates/steel")))
                .save(consumer, id("parts/fins_small_steel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("fins_big_steel"))
                .pattern(" PI")
                .pattern("III")
                .pattern(" PI")
                .define('P', forgeTag("plates/steel"))
                .define('I', forgeTag("ingots/steel"))
                .unlockedBy("has_steel_ingot", has(forgeTag("ingots/steel")))
                .save(consumer, id("parts/fins_big_steel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("fins_tri_steel"))
                .pattern(" PI")
                .pattern("IIB")
                .pattern(" PI")
                .define('P', forgeTag("plates/steel"))
                .define('I', forgeTag("ingots/steel"))
                .define('B', forgeTag("storage_blocks/steel"))
                .unlockedBy("has_steel_block", has(forgeTag("storage_blocks/steel")))
                .save(consumer, id("parts/fins_tri_steel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("fins_quad_titanium"))
                .pattern(" PP")
                .pattern("III")
                .pattern(" PP")
                .define('P', forgeTag("plates/titanium"))
                .define('I', forgeTag("ingots/titanium"))
                .unlockedBy("has_titanium_plate", has(forgeTag("plates/titanium")))
                .save(consumer, id("parts/fins_quad_titanium"));
    }

    /**
     * Workbench and anvil recipes for the small group of ordinary legacy items
     * which were absent from the initial modern registry sweep.  The recipes
     * are kept here as datagen sources and are also materialized under
     * {@code src/main/resources}; do not move them into a runtime-only map.
     */
    private static void legacyMissingItemRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BIG_SWORD.get())
                .pattern("QIQ")
                .pattern("QIQ")
                .pattern("GSG")
                .define('Q', Items.QUARTZ)
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GOLD_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_quartz", has(Items.QUARTZ))
                .save(consumer, id("weapon/big_sword"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.BDCL.get())
                .requires(forgeTag("any/tar"))
                .requires(HbmFluidContainerIngredient.of(HbmFluids.WATER, 1_000))
                .requires(Items.WHITE_DYE)
                .unlockedBy("has_tar", has(forgeTag("any/tar")))
                .save(consumer, id("consumables/bdcl"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEUTERIUM_FILTER.get())
                .pattern("TST")
                .pattern("SCS")
                .pattern("TST")
                .define('T', forgeTag("ingots/any_resistant_alloy"))
                .define('S', forgeTag("dusts/sulfur"))
                .define('C', item("catalyst_clay"))
                .unlockedBy("has_catalyst_clay", has(item("catalyst_clay")))
                .save(consumer, id("parts/deuterium_filter"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CRYSTAL_HORN.get())
                .requires(item("powder_neptunium"))
                .requires(item("powder_iron"))
                .requires(item("powder_thorium"))
                .requires(item("powder_astatine"))
                .requires(item("powder_neodymium"))
                .requires(item("powder_cs137"))
                .requires(block("block_meteor"))
                .requires(block("gravel_obsidian"))
                .requires(Items.WATER_BUCKET)
                .unlockedBy("has_meteor_block", has(block("block_meteor")))
                .save(consumer, id("parts/crystal_horn"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CRYSTAL_CHARRED.get())
                .requires(item("powder_steel"))
                .requires(item("powder_cobalt"))
                .requires(item("powder_bromine"))
                .requires(item("powder_niobium"))
                .requires(item("powder_tennessine"))
                .requires(item("powder_cerium"))
                .requires(block("block_meteor"))
                .requires(block("block_aluminium"))
                .requires(Items.WATER_BUCKET)
                .unlockedBy("has_meteor_block", has(block("block_meteor")))
                .save(consumer, id("parts/crystal_charred"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, block("crystal_virus"))
                .pattern("STS")
                .pattern("THT")
                .pattern("STS")
                .define('S', ModItems.PARTICLE_STRANGE.get())
                .define('T', item("powder_tungsten"))
                .define('H', ModItems.CRYSTAL_HORN.get())
                .unlockedBy("has_crystal_horn", has(ModItems.CRYSTAL_HORN.get()))
                .save(consumer, id("parts/crystal_virus"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, block("crystal_pulsar"), 32)
                .pattern("STS")
                .pattern("THT")
                .pattern("STS")
                .define('S', item("cell_uf6"))
                .define('T', item("powder_aluminium"))
                .define('H', ModItems.CRYSTAL_CHARRED.get())
                .unlockedBy("has_crystal_charred", has(ModItems.CRYSTAL_CHARRED.get()))
                .save(consumer, id("parts/crystal_pulsar"));

        String[] crayonColours = {
                "black", "red", "green", "brown", "blue", "purple", "cyan", "silver", "gray", "pink",
                "lime", "yellow", "lightblue", "magenta", "orange"
        };
        for (String colour : crayonColours) {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, item("crayon_" + colour), 4)
                    .requires(item("chemical_dye_" + colour))
                    .requires(forgeTag("any/tar"))
                    .requires(Items.PAPER)
                    .unlockedBy("has_chemical_dye_" + colour, has(item("chemical_dye_" + colour)))
                    .save(consumer, id("parts/crayon_" + colour));
        }

        anvilConstructionRecipe(consumer, id("anvil_construction/machine_deuterium_tower"), 4,
                new ItemStack(block("machine_deuterium_tower")),
                HbmIngredient.of(ModItems.DEUTERIUM_FILTER.get(), 2),
                HbmIngredient.of(forgeTag("shells/steel"), 5),
                HbmIngredient.of(forgeTag("pipes/steel"), 12),
                HbmIngredient.of(block("concrete_asbestos"), 8),
                HbmIngredient.of(ModBlocks.STEEL_SCAFFOLD.get(), 16),
                HbmIngredient.fluidContainer(HbmFluids.SOURGAS, 1_000, 8));
    }

    private static void missileSystemRecipes(Consumer<FinishedRecipe> consumer) {
        // 1.7.10 CraftingManager: steel pedestal / wrench / steel plate / steel scaffold.
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.MACHINE_MISSILE_ASSEMBLY.get())
                .pattern("PWP")
                .pattern("SSS")
                .pattern("CCC")
                .define('P', item("pedestal_steel"))
                .define('W', item("wrench"))
                .define('S', forgeTag("plates/steel"))
                .define('C', ModBlocks.STEEL_SCAFFOLD.get())
                .unlockedBy("has_steel_pedestal", has(item("pedestal_steel")))
                .save(consumer, id("missile/machine_missile_assembly"));

        // 1.7.10 CraftingManager: source-sized custom-missile construction segments.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("seg_10"))
                .pattern("P")
                .pattern("S")
                .pattern("B")
                .define('P', forgeTag("plates/aluminium"))
                .define('S', ModBlocks.STEEL_SCAFFOLD.get())
                .define('B', ModBlocks.STEEL_BEAM.get())
                .unlockedBy("has_aluminium_plate", has(forgeTag("plates/aluminium")))
                .save(consumer, id("missile/seg_10"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("seg_15"))
                .pattern("PP")
                .pattern("SS")
                .pattern("BB")
                .define('P', forgeTag("plates/titanium"))
                .define('S', ModBlocks.STEEL_SCAFFOLD.get())
                .define('B', ModBlocks.STEEL_BEAM.get())
                .unlockedBy("has_titanium_plate", has(forgeTag("plates/titanium")))
                .save(consumer, id("missile/seg_15"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("seg_20"))
                .pattern("PGP")
                .pattern("SSS")
                .pattern("BBB")
                .define('P', forgeTag("plates/steel"))
                .define('G', forgeTag("plates/gold"))
                .define('S', ModBlocks.STEEL_SCAFFOLD.get())
                .define('B', ModBlocks.STEEL_BEAM.get())
                .unlockedBy("has_steel_plate", has(forgeTag("plates/steel")))
                .save(consumer, id("missile/seg_20"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.LAUNCH_CODE.get())
                .requires(ModItems.LAUNCH_CODE_PIECE.get(), 8)
                .requires(item("circuit_advanced"))
                .unlockedBy("has_launch_code_piece", has(ModItems.LAUNCH_CODE_PIECE.get()))
                .save(consumer, id("parts/launch_code"));
    }

    private static void legacyStructuralRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STEEL_BEAM.get(), 8)
                .pattern("S")
                .pattern("S")
                .pattern("S")
                .define('S', forgeTag("ingots/steel"))
                .unlockedBy("has_steel_ingot", has(forgeTag("ingots/steel")))
                .save(consumer, id("blocks/steel_beam"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STEEL_SCAFFOLD.get(), 8)
                .pattern("SSS")
                .pattern(" S ")
                .pattern("SSS")
                .define('S', forgeTag("ingots/steel"))
                .unlockedBy("has_steel_ingot", has(forgeTag("ingots/steel")))
                .save(consumer, id("blocks/steel_scaffold"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CHAIN.get(), 8)
                .pattern("S")
                .pattern("S")
                .pattern("S")
                .define('S', ModBlocks.STEEL_BEAM.get())
                .unlockedBy("has_steel_beam", has(ModBlocks.STEEL_BEAM.get()))
                .save(consumer, id("blocks/chain"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STEEL_GRATE.get(), 4)
                .pattern("SS")
                .pattern("SS")
                .define('S', ModBlocks.STEEL_BEAM.get())
                .unlockedBy("has_steel_beam", has(ModBlocks.STEEL_BEAM.get()))
                .save(consumer, id("blocks/steel_grate"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STEEL_GRATE_WIDE.get(), 4)
                .pattern("SS")
                .define('S', ModBlocks.STEEL_GRATE.get())
                .unlockedBy("has_steel_grate", has(ModBlocks.STEEL_GRATE.get()))
                .save(consumer, id("blocks/steel_grate_wide"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STEEL_GRATE.get())
                .pattern("SS")
                .define('S', ModBlocks.STEEL_GRATE_WIDE.get())
                .unlockedBy("has_steel_grate_wide", has(ModBlocks.STEEL_GRATE_WIDE.get()))
                .save(consumer, id("blocks/steel_grate_from_wide"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.legacyBlock("wood_barrier").get(), 8)
                .pattern("SFS")
                .pattern("SFS")
                .define('S', ItemTags.WOODEN_SLABS)
                .define('F', Blocks.OAK_FENCE)
                .unlockedBy("has_wooden_slab", has(ItemTags.WOODEN_SLABS))
                .save(consumer, id("blocks/wood_barrier"));

        Object[][] woodStructureIngredients = {
                {'S', ItemTags.WOODEN_SLABS},
                {'F', Blocks.OAK_FENCE}
        };
        shapedLegacyVariantRecipe(consumer, id("blocks/wood_structure_roof"), ModBlocks.WOOD_STRUCTURE.get(),
                16, 0, new String[] {"SSS", "F F"}, woodStructureIngredients, Blocks.OAK_SLAB,
                "has_wooden_slab");
        shapedLegacyVariantRecipe(consumer, id("blocks/wood_structure_ceiling"), ModBlocks.WOOD_STRUCTURE.get(),
                16, 2, new String[] {"F F", "SSS"}, woodStructureIngredients, Blocks.OAK_SLAB,
                "has_wooden_slab");
        shapedLegacyVariantRecipe(consumer, id("blocks/wood_structure_scaffold"), ModBlocks.WOOD_STRUCTURE.get(),
                4, 1, new String[] {"SSS", "F F", "F F"}, woodStructureIngredients, Blocks.OAK_SLAB,
                "has_wooden_slab");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CRATE_IRON.get())
                .pattern("PPP")
                .pattern("I I")
                .pattern("III")
                .define('P', forgeTag("plates/iron"))
                .define('I', forgeTag("ingots/iron"))
                .unlockedBy("has_iron_plate", has(forgeTag("plates/iron")))
                .save(consumer, id("blocks/crate_iron"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CRATE_STEEL.get())
                .pattern("PPP")
                .pattern("I I")
                .pattern("III")
                .define('P', forgeTag("plates/steel"))
                .define('I', forgeTag("ingots/steel"))
                .unlockedBy("has_steel_plate", has(forgeTag("plates/steel")))
                .save(consumer, id("blocks/crate_steel"));

        containerUpgradeRecipe(consumer, id("blocks/crate_desh"), block("crate_desh"), null,
                new String[] {" D ", "DSD", " D "},
                new Object[][] {
                        {'D', forgeTag("plates/desh")},
                        {'S', block("crate_steel")}
                });

        containerUpgradeRecipe(consumer, id("blocks/crate_tungsten"), block("crate_tungsten"), null,
                new String[] {"BPB", "PCP", "BPB"},
                new Object[][] {
                        {'B', forgeTag("storage_blocks/tungsten")},
                        {'P', forgeTag("cast_plates/copper")},
                        {'C', block("crate_steel")}
                });

        containerUpgradeRecipe(consumer, id("blocks/safe"), block("safe"), null,
                new String[] {"LAL", "ACA", "LAL"},
                new Object[][] {
                        {'L', forgeTag("plates/lead")},
                        {'A', forgeTag("plates/titanium")},
                        {'C', block("crate_steel")}
                });

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.MASS_STORAGE.get())
                .pattern(" L ")
                .pattern("ICI")
                .pattern(" I ")
                .define('L', forgeTag("circuits/vacuum_tube"))
                .define('I', forgeTag("ingots/titanium"))
                .define('C', block("crate_steel"))
                .unlockedBy("has_steel_crate", has(block("crate_steel")))
                .save(consumer, id("blocks/mass_storage"));

        containerUpgradeRecipe(consumer, id("blocks/mass_storage_desh"), block("mass_storage"),
                "{hbmLegacyVariant:1}", new String[] {" C ", "PMP", " P "},
                new Object[][] {
                        {'C', forgeTag("circuits/chip")},
                        {'P', forgeTag("ingots/desh")},
                        {'M', block("mass_storage")}
                });

        containerUpgradeRecipe(consumer, id("blocks/mass_storage_tungsten"), block("mass_storage"),
                "{hbmLegacyVariant:2}", new String[] {" C ", "PMP", " P "},
                new Object[][] {
                        {'C', forgeTag("circuits/advanced")},
                        {'P', forgeTag("ingots/any_resistant_alloy")},
                        {'M', block("mass_storage")}
                });

        shapedLegacyVariantRecipe(consumer, id("blocks/filing_cabinet_steel"), ModBlocks.FILING_CABINET.get(),
                1, 1, new String[] {" P ", "PIP", " P "},
                new Object[][] {
                        {'P', forgeTag("plates/steel")},
                        {'I', item("plate_polymer")}
                },
                item("plate_polymer"), "has_plate_polymer");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.REINFORCED_GLASS.get(), 4)
                .pattern("FBF")
                .pattern("BFB")
                .pattern("FBF")
                .define('F', Blocks.IRON_BARS)
                .define('B', Blocks.GLASS)
                .unlockedBy("has_iron_bars", has(Blocks.IRON_BARS))
                .save(consumer, id("blocks/reinforced_glass"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.REINFORCED_GLASS_PANE.get(), 16)
                .pattern("GGG")
                .pattern("GGG")
                .define('G', ModBlocks.REINFORCED_GLASS.get())
                .unlockedBy("has_reinforced_glass", has(ModBlocks.REINFORCED_GLASS.get()))
                .save(consumer, id("blocks/reinforced_glass_pane"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.REINFORCED_LAMINATE_PANE.get(), 16)
                .pattern("LLL")
                .pattern("LLL")
                .define('L', ModBlocks.REINFORCED_LAMINATE.get())
                .unlockedBy("has_reinforced_laminate", has(ModBlocks.REINFORCED_LAMINATE.get()))
                .save(consumer, id("blocks/reinforced_laminate_pane"));
    }

    private static void chemicalPlantSourceRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.chemical("chem.hydrogen", 20, 400)
                .inputLegacyOre("gemCoal", 1)
                .inputFluid(HbmFluids.WATER, 8_000)
                .outputFluid(HbmFluids.HYDROGEN, 500)
                .customLocalization()
                .sourceOrder(0)
                .save(consumer, id("chemical_plant/hydrogen"));

        GenericMachineRecipeBuilder.chemical("chem.hydrogencoke", 20, 400)
                .inputLegacyOre("gemAnyCoke", 1)
                .inputFluid(HbmFluids.WATER, 8_000)
                .outputFluid(HbmFluids.HYDROGEN, 500)
                .customLocalization()
                .sourceOrder(1)
                .save(consumer, id("chemical_plant/hydrogencoke"));

        GenericMachineRecipeBuilder.chemical("chem.oxygen", 20, 400)
                .inputFluid(HbmFluids.AIR, 8_000)
                .outputFluid(HbmFluids.OXYGEN, 500)
                .customLocalization()
                .sourceOrder(2)
                .save(consumer, id("chemical_plant/oxygen"));

        GenericMachineRecipeBuilder.chemical("chem.xenon", 300, 1_000)
                .inputFluid(HbmFluids.AIR, 16_000)
                .outputFluid(HbmFluids.XENON, 50)
                .customLocalization()
                .sourceOrder(3)
                .save(consumer, id("chemical_plant/xenon"));

        GenericMachineRecipeBuilder.chemical("chem.xenonoxy", 20, 1_000)
                .inputFluid(HbmFluids.AIR, 8_000)
                .inputFluid(HbmFluids.OXYGEN, 250)
                .outputFluid(HbmFluids.XENON, 50)
                .pool(LegacyBlueprintPools.PREFIX_ALT + ".xenonoxy")
                .customLocalization()
                .sourceOrder(4)
                .save(consumer, id("chemical_plant/xenonoxy"));

        GenericMachineRecipeBuilder.chemical("chem.helium3", 25, 2_000)
                .inputItem(ModBlocks.MOON_TURF.get(), 1)
                .outputFluid(HbmFluids.HELIUM3, 125)
                .customLocalization()
                .sourceOrder(5)
                .save(consumer, id("chemical_plant/helium3"));

        GenericMachineRecipeBuilder.chemical("chem.co2", 60, 100)
                .inputFluid(HbmFluids.GAS, 1_000)
                .outputFluid(HbmFluids.CARBONDIOXIDE, 1_000)
                .sourceOrder(6)
                .save(consumer, id("chemical_plant/co2"));

        GenericMachineRecipeBuilder.chemical("chem.perfluoromethyl", 20, 100)
                .inputLegacyOre("dustFluorite", 1)
                .inputFluid(HbmFluids.PETROLEUM, 1_000)
                .inputFluid(HbmFluids.UNSATURATEDS, 500)
                .outputFluid(HbmFluids.PERFLUOROMETHYL, 1_000)
                .sourceOrder(7)
                .save(consumer, id("chemical_plant/perfluoromethyl"));

        GenericMachineRecipeBuilder.chemical("chem.cccentrifuge", 200, 100)
                .inputFluid(HbmFluids.CHLOROCALCITE_CLEANED, 500)
                .inputFluid(HbmFluids.SULFURIC_ACID, 8_000)
                .outputFluid(HbmFluids.POTASSIUM_CHLORIDE, 250)
                .outputFluid(HbmFluids.CALCIUM_CHLORIDE, 250)
                .sourceOrder(8)
                .save(consumer, id("chemical_plant/cccentrifuge"));

        GenericMachineRecipeBuilder.chemical("chem.ethanol", 50, 100)
                .inputItem(Items.SUGAR, 10)
                .outputFluid(HbmFluids.ETHANOL, 1_000)
                .customLocalization()
                .sourceOrder(9)
                .save(consumer, id("chemical_plant/ethanol"));

        GenericMachineRecipeBuilder.chemical("chem.biogas", 60, 100)
                .inputItem(ModItems.BIOMASS.get(), 16)
                .inputFluid(HbmFluids.AIR, 4_000)
                .outputFluid(HbmFluids.BIOGAS, 2_000)
                .customLocalization()
                .sourceOrder(10)
                .save(consumer, id("chemical_plant/biogas"));

        GenericMachineRecipeBuilder.chemical("chem.biofuel", 60, 100)
                .inputFluid(HbmFluids.BIOGAS, 1_500)
                .inputFluid(HbmFluids.ETHANOL, 250)
                .outputFluid(HbmFluids.BIOFUEL, 1_000)
                .customLocalization()
                .sourceOrder(11)
                .save(consumer, id("chemical_plant/biofuel"));

        GenericMachineRecipeBuilder.chemical("chem.reoil", 40, 100)
                .inputFluid(HbmFluids.SMEAR, 1_000)
                .outputFluid(HbmFluids.RECLAIMED, 800)
                .customLocalization()
                .sourceOrder(12)
                .save(consumer, id("chemical_plant/reoil"));

        GenericMachineRecipeBuilder.chemical("chem.gasoline", 40, 100)
                .inputFluid(HbmFluids.NAPHTHA, 1_000)
                .outputFluid(HbmFluids.GASOLINE, 800)
                .customLocalization()
                .sourceOrder(13)
                .save(consumer, id("chemical_plant/gasoline"));

        GenericMachineRecipeBuilder.chemical("chem.coallube", 40, 100)
                .inputFluid(HbmFluids.COALCREOSOTE, 1_000)
                .outputFluid(HbmFluids.LUBRICANT, 1_000)
                .pool(LegacyBlueprintPools.PREFIX_ALT + ".lube")
                .customLocalization()
                .sourceOrder(14)
                .save(consumer, id("chemical_plant/coallube"));

        GenericMachineRecipeBuilder.chemical("chem.heavylube", 40, 100)
                .inputFluid(HbmFluids.HEAVYOIL, 2_000)
                .outputFluid(HbmFluids.LUBRICANT, 1_000)
                .pool(LegacyBlueprintPools.PREFIX_ALT + ".lube")
                .customLocalization()
                .sourceOrder(15)
                .save(consumer, id("chemical_plant/heavylube"));

        GenericMachineRecipeBuilder.chemical("chem.tarsand", 200, 100)
                .inputItem(block("ore_oil_sand"), 16)
                .inputLegacyOre("anyTar", 1)
                .outputItem(new ItemStack(Blocks.SAND, 16))
                .outputFluid(HbmFluids.BITUMEN, 1_000)
                .customLocalization()
                .sourceOrder(16)
                .save(consumer, id("chemical_plant/tarsand"));

        GenericMachineRecipeBuilder.chemical("chem.tel", 40, 100)
                .inputLegacyOre("anyTar", 1)
                .inputLegacyOre("dustLead", 1)
                .inputFluid(HbmFluids.PETROLEUM, 100)
                .inputFluid(HbmFluids.STEAM, 1_000)
                .outputLegacyMeta(LegacyMetaItemMappings.FUEL_ADDITIVE, 0)
                .sourceOrder(17)
                .save(consumer, id("chemical_plant/tel"));

        GenericMachineRecipeBuilder.chemical("chem.deicer", 40, 100)
                .inputFluid(HbmFluids.GAS, 100)
                .inputFluid(HbmFluids.HYDROGEN, 50)
                .outputLegacyMeta(LegacyMetaItemMappings.FUEL_ADDITIVE, 1)
                .sourceOrder(18)
                .save(consumer, id("chemical_plant/deicer"));

        GenericMachineRecipeBuilder.chemical("chem.cobble", 20, 100)
                .inputFluid(HbmFluids.WATER, 1_000)
                .inputFluid(HbmFluids.LAVA, 25)
                .outputItem(Blocks.COBBLESTONE)
                .sourceOrder(19)
                .save(consumer, id("chemical_plant/cobble"));

        GenericMachineRecipeBuilder.chemical("chem.stone", 60, 500)
                .inputFluid(HbmFluids.WATER, 1_000)
                .inputFluid(HbmFluids.LAVA, 25)
                .inputFluid(HbmFluids.AIR, 4_000)
                .outputItem(Blocks.STONE)
                .pool(LegacyBlueprintPools.PREFIX_DISCOVER + "stone")
                .sourceOrder(20)
                .save(consumer, id("chemical_plant/stone"));

        GenericMachineRecipeBuilder.chemical("chem.obsidian", 60, 500)
                .inputFluid(HbmFluids.WATER, 1_000)
                .inputFluid(HbmFluids.LAVA, 500)
                .inputFluid(HbmFluids.AIR, 4_000)
                .outputItem(Blocks.OBSIDIAN)
                .pool(LegacyBlueprintPools.PREFIX_DISCOVER + "stone")
                .sourceOrder(21)
                .save(consumer, id("chemical_plant/obsidian"));

        GenericMachineRecipeBuilder.chemical("chem.aggregate", 320, 500)
                .inputItem(Blocks.COBBLESTONE, 16)
                .outputItem(new ItemStack(Blocks.GRAVEL, 8))
                .outputItem(new ItemStack(Blocks.SAND, 8))
                .pool(LegacyBlueprintPools.PREFIX_DISCOVER + "stone")
                .customLocalization()
                .sourceOrder(22)
                .save(consumer, id("chemical_plant/aggregate"));

        GenericMachineRecipeBuilder.chemical("chem.concrete", 100, 100)
                .inputItem(item("powder_cement"), 1)
                .inputItem(Blocks.GRAVEL, 8)
                .inputLegacyOre("sand", 8)
                .inputFluid(HbmFluids.WATER, 2_000)
                .outputItem(new ItemStack(block("concrete_smooth"), 16))
                .sourceOrder(23)
                .save(consumer, id("chemical_plant/concrete"));

        GenericMachineRecipeBuilder.chemical("chem.concreteasbestos", 100, 100)
                .inputItem(item("powder_cement"), 4)
                .inputLegacyOre("ingotAsbestos", 4)
                .inputLegacyOre("sand", 8)
                .inputFluid(HbmFluids.WATER, 2_000)
                .outputItem(new ItemStack(block("concrete_asbestos"), 16))
                .sourceOrder(24)
                .save(consumer, id("chemical_plant/concreteasbestos"));

        GenericMachineRecipeBuilder.chemical("chem.ducrete", 150, 100)
                .inputItem(item("powder_cement"), 4)
                .inputLegacyOre("ingotFerrouranium", 1)
                .inputLegacyOre("sand", 8)
                .inputFluid(HbmFluids.WATER, 2_000)
                .outputItem(new ItemStack(block("ducrete_smooth"), 8))
                .sourceOrder(25)
                .save(consumer, id("chemical_plant/ducrete"));

        GenericMachineRecipeBuilder.chemical("chem.liquidconk", 100, 100)
                .inputItem(item("powder_cement"), 1)
                .inputItem(Blocks.GRAVEL, 8)
                .inputLegacyOre("sand", 8)
                .inputFluid(HbmFluids.WATER, 2_000)
                .outputFluid(HbmFluids.CONCRETE, 16_000)
                .sourceOrder(26)
                .save(consumer, id("chemical_plant/liquidconk"));

        GenericMachineRecipeBuilder.chemical("chem.asphalt", 100, 100)
                .inputItem(Blocks.GRAVEL, 2)
                .inputLegacyOre("sand", 6)
                .inputFluid(HbmFluids.BITUMEN, 1_000)
                .outputItem(new ItemStack(block("asphalt"), 16))
                .sourceOrder(27)
                .save(consumer, id("chemical_plant/asphalt"));

        GenericMachineRecipeBuilder.chemical("chem.desh", 100, 100)
                .inputItem(item("powder_desh_mix"), 1)
                .inputFluid(HbmFluids.LIGHTOIL, 200)
                .inputFluid(HbmFluids.MERCURY, 200)
                .outputItem(item("ingot_desh"))
                .sourceOrder(33)
                .save(consumer, id("chemical_plant/desh"));

        GenericMachineRecipeBuilder.chemical("chem.deshcracked", 100, 100)
                .inputItem(item("powder_desh_mix"), 1)
                .inputFluid(HbmFluids.LIGHTOIL_CRACK, 500, 1)
                .inputFluid(HbmFluids.MERCURY, 100)
                .outputItem(item("ingot_desh"))
                .sourceOrder(34)
                .save(consumer, id("chemical_plant/desh_cracked"));

        GenericMachineRecipeBuilder.chemical("chem.polymer", 100, 100)
                .inputLegacyOre("dustCoal", 2)
                .inputLegacyOre("dustFluorite", 1)
                .inputFluid(HbmFluids.PETROLEUM, 1_000)
                .outputItem(new ItemStack(item("ingot_polymer"), 4))
                .sourceOrder(35)
                .save(consumer, id("chemical_plant/polymer"));

        GenericMachineRecipeBuilder.chemical("chem.bakelite", 100, 100)
                .inputFluid(HbmFluids.AROMATICS, 500)
                .inputFluid(HbmFluids.PETROLEUM, 500)
                .outputItem(item("ingot_bakelite"))
                .sourceOrder(36)
                .save(consumer, id("chemical_plant/bakelite"));

        GenericMachineRecipeBuilder.chemical("chem.rubber", 100, 200)
                .inputLegacyOre("dustSulfur", 1)
                .inputFluid(HbmFluids.UNSATURATEDS, 500)
                .outputItem(new ItemStack(item("ingot_rubber"), 2))
                .sourceOrder(37)
                .save(consumer, id("chemical_plant/rubber"));

        GenericMachineRecipeBuilder.chemical("chem.hardplastic", 100, 1_000)
                .inputFluid(HbmFluids.XYLENE, 500)
                .inputFluid(HbmFluids.PHOSGENE, 500)
                .outputItem(item("ingot_pc"))
                .sourceOrder(38)
                .save(consumer, id("chemical_plant/hardplastic"));

        GenericMachineRecipeBuilder.chemical("chem.pvc", 100, 1_000)
                .inputLegacyOre("dustCadmium", 1)
                .inputFluid(HbmFluids.UNSATURATEDS, 250)
                .inputFluid(HbmFluids.CHLORINE, 250)
                .outputItem(new ItemStack(item("ingot_pvc"), 2))
                .sourceOrder(39)
                .save(consumer, id("chemical_plant/pvc"));

        GenericMachineRecipeBuilder.chemical("chem.kevlar", 60, 300)
                .inputFluid(HbmFluids.AROMATICS, 200)
                .inputFluid(HbmFluids.NITRIC_ACID, 100)
                .inputFluid(HbmFluids.CHLORINE, 100)
                .outputItem(new ItemStack(item("plate_kevlar"), 4))
                .sourceOrder(40)
                .save(consumer, id("chemical_plant/kevlar"));

        GenericMachineRecipeBuilder.chemical("chem.biosolidfuel", 40, 100)
                .inputItem(ModItems.BIOMASS_COMPRESSED.get(), 4)
                .outputItem(item("solid_fuel"))
                .pool(LegacyBlueprintPools.PREFIX_ALT + ".biosolidfuel")
                .customLocalization()
                .sourceOrder(45)
                .save(consumer, id("chemical_plant/biosolidfuel"));

        GenericMachineRecipeBuilder.chemical("chem.biooilsolidfuel", 40, 100)
                .inputItem(ModItems.BIOMASS_COMPRESSED.get(), 2)
                .inputFluid(HbmFluids.HEATINGOIL, 100)
                .outputItem(item("solid_fuel"))
                .pool(LegacyBlueprintPools.PREFIX_ALT + ".biosolidfuel")
                .customLocalization()
                .sourceOrder(46)
                .save(consumer, id("chemical_plant/biooilsolidfuel"));

        GenericMachineRecipeBuilder.chemical("chem.oilelectrodes", 600, 100)
                .inputFluid(HbmFluids.HEATINGOIL, 4_000)
                .outputLegacyMeta(LegacyMetaItemMappings.ARC_ELECTRODE, 0)
                .pool(LegacyBlueprintPools.PREFIX_ALT + ".electrodes")
                .customLocalization()
                .sourceOrder(47)
                .save(consumer, id("chemical_plant/oil_electrodes"));

        GenericMachineRecipeBuilder.chemical("chem.lubeelectrodes", 600, 100)
                .inputFluid(HbmFluids.LUBRICANT, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.ARC_ELECTRODE, 0)
                .pool(LegacyBlueprintPools.PREFIX_ALT + ".electrodes")
                .customLocalization()
                .sourceOrder(48)
                .save(consumer, id("chemical_plant/lube_electrodes"));

        GenericMachineRecipeBuilder.chemical("chem.peroxide", 50, 100)
                .inputFluid(HbmFluids.WATER, 1_000)
                .outputFluid(HbmFluids.PEROXIDE, 1_000)
                .sourceOrder(49)
                .save(consumer, id("chemical_plant/peroxide"));

        GenericMachineRecipeBuilder.chemical("chem.sulfuricacid", 50, 100)
                .inputLegacyOre("dustSulfur", 1)
                .inputFluid(HbmFluids.PEROXIDE, 1_000)
                .inputFluid(HbmFluids.WATER, 1_000)
                .outputFluid(HbmFluids.SULFURIC_ACID, 2_000)
                .sourceOrder(50)
                .save(consumer, id("chemical_plant/sulfuricacid"));

        GenericMachineRecipeBuilder.chemical("chem.nitricacid", 50, 100)
                .inputLegacyOre("dustSaltpeter", 1)
                .inputFluid(HbmFluids.SULFURIC_ACID, 500)
                .outputFluid(HbmFluids.NITRIC_ACID, 1_000)
                .sourceOrder(51)
                .save(consumer, id("chemical_plant/nitricacid"));

        GenericMachineRecipeBuilder.chemical("chem.birkeland", 200, 5_000)
                .inputFluid(HbmFluids.AIR, 8_000)
                .inputFluid(HbmFluids.WATER, 2_000)
                .outputFluid(HbmFluids.NITRIC_ACID, 1_000)
                .pool(LegacyBlueprintPools.PREFIX_ALT + ".birkeland")
                .customLocalization()
                .sourceOrder(52)
                .save(consumer, id("chemical_plant/birkeland"));

        GenericMachineRecipeBuilder.chemical("chem.schrabidic", 60, 5_000)
                .inputItem(item("pellet_charged"), 1)
                .inputFluid(HbmFluids.SAS3, 2_000)
                .inputFluid(HbmFluids.PEROXIDE, 2_000)
                .outputFluid(HbmFluids.SCHRABIDIC, 2_000)
                .sourceOrder(53)
                .save(consumer, id("chemical_plant/schrabidic"));

        GenericMachineRecipeBuilder.chemical("chem.schrabidate", 150, 5_000)
                .inputLegacyOre("dustIron", 1)
                .inputFluid(HbmFluids.SCHRABIDIC, 250)
                .outputItem(item("powder_schrabidate"))
                .sourceOrder(54)
                .save(consumer, id("chemical_plant/schrabidate"));

        GenericMachineRecipeBuilder.chemical("chem.epearl", 100, 300)
                .inputLegacyOre("dustDiamond", 1)
                .inputFluid(HbmFluids.XPJUICE, 500)
                .outputFluid(HbmFluids.ENDERJUICE, 100)
                .sourceOrder(42)
                .save(consumer, id("chemical_plant/epearl"));

        GenericMachineRecipeBuilder.chemical("chem.meth", 60, 300)
                .inputItem(Items.WHEAT, 1)
                .inputItem(Items.COCOA_BEANS, 2)
                .inputFluid(HbmFluids.LUBRICANT, 400)
                .inputFluid(HbmFluids.PEROXIDE, 500)
                .outputItem(new ItemStack(item("chocolate"), 4))
                .sourceOrder(41)
                .save(consumer, id("chemical_plant/meth"));

        GenericMachineRecipeBuilder.chemical("chem.meatprocessing", 200, 200)
                .inputLegacyOre("glyphidMeat", 3)
                .inputFluid(HbmFluids.WATER, 1_000)
                .outputItem(new ItemStack(item("sulfur"), 4))
                .outputItem(new ItemStack(item("niter"), 3))
                .outputFluid(HbmFluids.SALIENT, 250)
                .icon(item("glyphid_meat"))
                .customLocalization()
                .sourceOrder(43)
                .save(consumer, id("chemical_plant/meat_processing"));

        GenericMachineRecipeBuilder.chemical("chem.rustysteel", 40, 100)
                .inputItem(block("deco_steel"), 8)
                .inputFluid(HbmFluids.WATER, 1_000)
                .outputItem(new ItemStack(block("deco_rusty_steel"), 8))
                .sourceOrder(44)
                .save(consumer, id("chemical_plant/rustysteel"));

        GenericMachineRecipeBuilder.chemical("chem.coltancleaning", 60, 100)
                .inputLegacyOre("dustColtan", 2)
                .inputLegacyOre("dustCoal", 1)
                .inputFluid(HbmFluids.PEROXIDE, 250)
                .inputFluid(HbmFluids.HYDROGEN, 500)
                .outputItem(item("powder_coltan"))
                .outputItem(item("powder_niobium"))
                .outputItem(item("dust"))
                .outputFluid(HbmFluids.WATER, 500)
                .sourceOrder(55)
                .save(consumer, id("chemical_plant/coltan_cleaning"));

        GenericMachineRecipeBuilder.chemical("chem.coltanpain", 120, 100)
                .inputItem(item("powder_coltan"), 1)
                .inputLegacyOre("dustFluorite", 1)
                .inputFluid(HbmFluids.GAS, 1_000)
                .inputFluid(HbmFluids.OXYGEN, 500)
                .outputFluid(HbmFluids.PAIN, 1_000)
                .sourceOrder(56)
                .save(consumer, id("chemical_plant/coltanpain"));

        GenericMachineRecipeBuilder.chemical("chem.coltancrystal", 80, 100)
                .inputFluid(HbmFluids.PAIN, 1_000)
                .inputFluid(HbmFluids.PEROXIDE, 500)
                .outputItem(item("gem_tantalium"))
                .outputItem(new ItemStack(item("dust"), 3))
                .outputFluid(HbmFluids.WATER, 250)
                .sourceOrder(57)
                .save(consumer, id("chemical_plant/coltan_crystal"));

        GenericMachineRecipeBuilder.chemical("chem.cordite", 40, 100)
                .inputLegacyOre("dustSaltpeter", 2)
                .inputItem(item("powder_sawdust"), 2)
                .inputFluid(HbmFluids.GAS, 200)
                .outputItem(new ItemStack(item("cordite"), 4))
                .sourceOrder(58)
                .save(consumer, id("chemical_plant/cordite"));

        GenericMachineRecipeBuilder.chemical("chem.rocketfuel", 200, 100)
                .inputItem(item("solid_fuel"), 2)
                .inputFluid(HbmFluids.PETROLEUM, 200)
                .inputFluid(HbmFluids.NITRIC_ACID, 100)
                .outputItem(new ItemStack(item("rocket_fuel"), 4))
                .sourceOrder(59)
                .save(consumer, id("chemical_plant/rocketfuel"));

        GenericMachineRecipeBuilder.chemical("chem.dynamite", 50, 100)
                .inputItem(Items.SUGAR, 1)
                .inputLegacyOre("dustSaltpeter", 1)
                .inputLegacyOre("sand", 1)
                .outputItem(new ItemStack(item("ball_dynamite"), 2))
                .sourceOrder(60)
                .save(consumer, id("chemical_plant/dynamite"));

        GenericMachineRecipeBuilder.chemical("chem.tnt", 100, 1_000)
                .inputLegacyOre("dustSaltpeter", 1)
                .inputFluid(HbmFluids.AROMATICS, 500)
                .outputItem(new ItemStack(item("ball_tnt"), 4))
                .sourceOrder(61)
                .save(consumer, id("chemical_plant/tnt"));

        GenericMachineRecipeBuilder.chemical("chem.tatb", 50, 5_000)
                .inputItem(item("ball_tnt"), 1)
                .inputFluid(HbmFluids.SOURGAS, 200, 1)
                .inputFluid(HbmFluids.NITRIC_ACID, 10)
                .outputItem(item("ball_tatb"))
                .sourceOrder(62)
                .save(consumer, id("chemical_plant/tatb"));

        GenericMachineRecipeBuilder.chemical("chem.c4", 100, 1_000)
                .inputLegacyOre("dustSaltpeter", 1)
                .inputFluid(HbmFluids.UNSATURATEDS, 500)
                .outputItem(new ItemStack(item("ingot_c4"), 4))
                .sourceOrder(63)
                .save(consumer, id("chemical_plant/c4"));

        GenericMachineRecipeBuilder.chemical("chem.napalm", 40, 100)
                .inputItem(ModItems.CANISTER_EMPTY.get(), 1)
                .inputFluid(HbmFluids.GASOLINE, 100)
                .inputFluid(HbmFluids.AROMATICS, 50)
                .outputItem(ModItems.CANISTER_NAPALM.get())
                .sourceOrder(64)
                .save(consumer, id("chemical_plant/napalm"));

        GenericMachineRecipeBuilder.chemical("chem.laminate", 20, 100)
                .inputLegacyOre("blockGlass", 1)
                .inputLegacyOre("boltSteel", 4)
                .inputFluid(HbmFluids.XYLENE, 50)
                .inputFluid(HbmFluids.PHOSGENE, 50)
                .outputItem(ModBlocks.REINFORCED_LAMINATE.get())
                .sourceOrder(65)
                .save(consumer, id("chemical_plant/laminate"));

        GenericMachineRecipeBuilder.chemical("chem.polarized", 100, 500)
                .inputLegacyOre("paneGlass", 1)
                .inputFluid(HbmFluids.PETROLEUM, 1_000)
                .outputItem(new ItemStack(legacyMetaItem(LegacyMetaItemMappings.PART_GENERIC, 5), 16))
                .sourceOrder(66)
                .save(consumer, id("chemical_plant/polarized"));

        GenericMachineRecipeBuilder.chemical("chem.yellowcake", 250, 500)
                .inputLegacyOre("billetUranium", 2)
                .inputLegacyOre("dustSulfur", 2)
                .inputFluid(HbmFluids.PEROXIDE, 500)
                .outputItem(item("powder_yellowcake"))
                .sourceOrder(67)
                .save(consumer, id("chemical_plant/yellowcake"));

        GenericMachineRecipeBuilder.chemical("chem.uf6", 100, 500)
                .inputItem(item("powder_yellowcake"), 1)
                .inputLegacyOre("dustFluorite", 4)
                .inputFluid(HbmFluids.WATER, 1_000)
                .outputItem(new ItemStack(item("sulfur"), 2))
                .outputFluid(HbmFluids.UF6, 1_200)
                .sourceOrder(68)
                .save(consumer, id("chemical_plant/uf6"));

        GenericMachineRecipeBuilder.chemical("chem.puf6", 200, 500)
                .inputLegacyOre("dustPlutonium", 1)
                .inputLegacyOre("dustFluorite", 3)
                .inputFluid(HbmFluids.WATER, 1_000)
                .outputFluid(HbmFluids.PUF6, 900)
                .sourceOrder(69)
                .save(consumer, id("chemical_plant/puf6"));

        GenericMachineRecipeBuilder.chemical("chem.sas3", 200, 5_000)
                .inputLegacyOre("dustSchrabidium", 1)
                .inputLegacyOre("dustSulfur", 2)
                .inputFluid(HbmFluids.PEROXIDE, 2_000)
                .outputFluid(HbmFluids.SAS3, 1_000)
                .sourceOrder(70)
                .save(consumer, id("chemical_plant/sas3"));

        GenericMachineRecipeBuilder.chemical("chem.balefire", 100, 10_000)
                .inputItem(item("egg_balefire_shard"), 1)
                .inputFluid(HbmFluids.KEROSENE, 6_000)
                .outputItem(item("powder_balefire"))
                .outputFluid(HbmFluids.BALEFIRE, 8_000)
                .sourceOrder(71)
                .save(consumer, id("chemical_plant/balefire"));

        GenericMachineRecipeBuilder.chemical("chem.dhc", 400, 500)
                .inputFluid(HbmFluids.DEUTERIUM, 500)
                .inputFluid(HbmFluids.REFORMGAS, 250)
                .inputFluid(HbmFluids.SYNGAS, 250)
                .outputFluid(HbmFluids.DHC, 500)
                .sourceOrder(72)
                .save(consumer, id("chemical_plant/dhc"));

        GenericMachineRecipeBuilder.chemical("chem.osmiridiumdeath", 240, 1_000)
                .inputItem(item("powder_paleogenite"), 1)
                .inputLegacyOre("dustFluorite", 8)
                .inputItem(item("nugget_bismuth"), 4)
                .inputFluid(HbmFluids.PEROXIDE, 1_000, 5)
                .outputFluid(HbmFluids.DEATH, 1_000)
                .sourceOrder(73)
                .save(consumer, id("chemical_plant/osmiridiumdeath"));
    }

    private static void chemicalBatteryRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.chemical("chem.batterylead", 100, 100)
                .inputItem(ModItems.STEEL_PLATE.get(), 4)
                .inputItem(ModItems.LEAD_INGOT.get(), 4)
                .inputFluid(HbmFluids.SULFURIC_ACID, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 1)
                .sourceOrder(28)
                .save(consumer, id("chemical_plant/batterylead"));

        GenericMachineRecipeBuilder.chemical("chem.batterylithium", 100, 1_000)
                .inputLegacyOre("dustLithium", 12)
                .inputLegacyOre("dustCobalt", 8)
                .inputLegacyOre("ingotAnyPlastic", 4)
                .inputFluid(HbmFluids.OXYGEN, 2_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 2)
                .sourceOrder(29)
                .save(consumer, id("chemical_plant/batterylithium"));

        GenericMachineRecipeBuilder.chemical("chem.batterysodium", 100, 10_000)
                .inputLegacyOre("dustSodium", 24)
                .inputLegacyOre("dustIron", 24)
                .inputLegacyOre("ingotAnyHardPlastic", 12)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 3)
                .sourceOrder(30)
                .save(consumer, id("chemical_plant/batterysodium"));

        GenericMachineRecipeBuilder.chemical("chem.batteryschrabidium", 100, 25_000)
                .inputLegacyOre("dustSchrabidium", 24)
                .inputLegacyOre("plateCastAnyBismoidBronze", 8)
                .inputFluid(HbmFluids.HELIUM4, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 4)
                .sourceOrder(31)
                .save(consumer, id("chemical_plant/batteryschrabidium"));

        GenericMachineRecipeBuilder.chemical("chem.batteryquantum", 100, 100_000)
                .inputLegacyOre("wireDenseBSCCO", 24)
                .inputItem(item("pellet_charged"), 32)
                .inputItem(item("ingot_cft"), 16)
                .inputFluid(HbmFluids.PERFLUOROMETHYL_COLD, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 5)
                .outputFluid(HbmFluids.PERFLUOROMETHYL, 8_000)
                .sourceOrder(32)
                .save(consumer, id("chemical_plant/batteryquantum"));
    }

    private static void assemblyCapacitorRecipes(Consumer<FinishedRecipe> consumer) {
        assemblyPlateRecipe(consumer, "ass.plateiron", "iron", "ingotIron", "plate_iron", 0);
        assemblyPlateRecipe(consumer, "ass.plategold", "gold", "ingotGold", "plate_gold", 1);
        assemblyPlateRecipe(consumer, "ass.platetitanium", "titanium", "ingotTitanium", "plate_titanium", 2);
        assemblyPlateRecipe(consumer, "ass.platealu", "aluminium", "ingotAluminum", "plate_aluminium", 3);
        assemblyPlateRecipe(consumer, "ass.platesteel", "steel", "ingotSteel", "plate_steel", 4);
        assemblyPlateRecipe(consumer, "ass.platelead", "lead", "ingotLead", "plate_lead", 5);
        assemblyPlateRecipe(consumer, "ass.platecopper", "copper", "ingotCopper", "plate_copper", 6);

        GenericMachineRecipeBuilder.assembly("ass.sealframe", 100, 100)
                .inputLegacyOre("ingotDuraSteel", 1)
                .inputLegacyOre("plateCastSteel", 1)
                .inputLegacyOre("wireDenseMingrade", 1)
                .outputItem(ModBlocks.SEAL_FRAME.get())
                .sourceOrder(52)
                .save(consumer, id("assembly_machine/seal_frame"));

        GenericMachineRecipeBuilder.assembly("ass.sealcontroller", 100, 100)
                .inputLegacyOre("ingotDuraSteel", 1)
                .inputLegacyOre("plateCastSteel", 1)
                .inputLegacyOre("ingotAnyPlastic", 4)
                .inputLegacyOre("wireDenseMingrade", 4)
                .outputItem(ModBlocks.SEAL_CONTROLLER.get())
                .sourceOrder(53)
                .save(consumer, id("assembly_machine/seal_controller"));

        GenericMachineRecipeBuilder.assembly("ass.vaultdoor", 600, 100)
                .inputLegacyOre("ingotSteel", 32)
                .inputLegacyOre("ingotDuraSteel", 32)
                .inputLegacyOre("plateCastLead", 8)
                .inputLegacyOre("ingotAnyRubber", 12)
                .inputLegacyOre("boltDuraSteel", 32)
                .inputItem(item("motor"), 3)
                .outputItem(ModBlocks.VAULT_DOOR.get())
                .sourceOrder(56)
                .save(consumer, id("assembly_machine/vault_door"));

        GenericMachineRecipeBuilder.assembly("ass.blastdoor", 200, 100)
                .inputLegacyOre("ingotSteel", 12)
                .inputLegacyOre("plateLead", 6)
                .inputLegacyOre("ingotAnyRubber", 2)
                .inputLegacyOre("boltDuraSteel", 8)
                .inputItem(item("motor"), 1)
                .outputItem(ModBlocks.BLAST_DOOR.get())
                .sourceOrder(57)
                .save(consumer, id("assembly_machine/blast_door"));

        GenericMachineRecipeBuilder.assembly("ass.firedoor", 300, 100)
                .inputLegacyOre("plateSteel", 16)
                .inputLegacyOre("boltDuraSteel", 8)
                .inputItem(item("motor"), 2)
                .outputItem(ModBlocks.FIRE_DOOR.get())
                .sourceOrder(58)
                .save(consumer, id("assembly_machine/fire_door"));

        GenericMachineRecipeBuilder.assembly("ass.seal", 1_200, 100)
                .inputItem(block("cmb_brick_reinforced"), 16)
                .inputLegacyOre("plateSteel", 64)
                .inputLegacyOre("ingotAnyRubber", 36)
                .inputItem(block("block_steel"), 32)
                .inputItem(item("motor_desh"), 16)
                .inputLegacyOre("dyeYellow", 4)
                .outputItem(ModBlocks.TRANSITION_SEAL.get())
                .sourceOrder(59)
                .save(consumer, id("assembly_machine/transition_seal"));

        GenericMachineRecipeBuilder.assembly("ass.slidingdoor", 200, 100)
                .inputLegacyOre("plateSteel", 16)
                .inputLegacyOre("ingotTungsten", 8)
                .inputItem(block("reinforced_glass"), 4)
                .inputLegacyOre("ingotAnyRubber", 4)
                .inputLegacyOre("boltDuraSteel", 16)
                .inputItem(item("motor"), 2)
                .outputItem(ModBlocks.SLIDING_BLAST_DOOR.get())
                .sourceOrder(60)
                .save(consumer, id("assembly_machine/sliding_blast_door"));

        GenericMachineRecipeBuilder.assembly("ass.vehicledoor", 400, 100)
                .inputLegacyOre("plateCastSteel", 16)
                .inputItem(item("plate_polymer"), 4)
                .inputItem(item("motor"), 4)
                .inputLegacyOre("boltDuraSteel", 16)
                .inputLegacyOre("dyeGreen", 4)
                .outputItem(ModBlocks.LARGE_VEHICLE_DOOR.get())
                .sourceOrder(61)
                .save(consumer, id("assembly_machine/large_vehicle_door"));

        GenericMachineRecipeBuilder.assembly("ass.waterdoor", 200, 100)
                .inputLegacyOre("plateSteel", 16)
                .inputLegacyOre("boltDuraSteel", 4)
                .inputLegacyOre("dyeRed", 1)
                .outputItem(ModBlocks.WATER_DOOR.get())
                .sourceOrder(62)
                .save(consumer, id("assembly_machine/water_door"));

        GenericMachineRecipeBuilder.assembly("ass.qedoor", 400, 100)
                .inputLegacyOre("plateCastSteel", 6)
                .inputItem(item("plate_polymer"), 8)
                .inputItem(item("motor"), 2)
                .inputLegacyOre("boltDuraSteel", 32)
                .inputLegacyOre("dyeBlack", 4)
                .outputItem(ModBlocks.QE_CONTAINMENT.get())
                .sourceOrder(63)
                .save(consumer, id("assembly_machine/qe_containment"));

        GenericMachineRecipeBuilder.assembly("ass.queslidingdoor", 200, 100)
                .inputLegacyOre("plateSteel", 4)
                .inputItem(item("plate_polymer"), 4)
                .inputItem(item("motor"), 2)
                .inputLegacyOre("boltDuraSteel", 4)
                .inputLegacyOre("dyeWhite", 4)
                .inputItem(Items.GLASS, 4)
                .outputItem(ModBlocks.QE_SLIDING_DOOR.get())
                .sourceOrder(64)
                .save(consumer, id("assembly_machine/qe_sliding_door"));

        GenericMachineRecipeBuilder.assembly("ass.roundairlock", 400, 100)
                .inputLegacyOre("plateCastSteel", 12)
                .inputItem(item("plate_polymer"), 16)
                .inputItem(item("motor"), 4)
                .inputLegacyOre("boltDuraSteel", 16)
                .inputLegacyOre("dyeGreen", 4)
                .outputItem(ModBlocks.ROUND_AIRLOCK_DOOR.get())
                .sourceOrder(65)
                .save(consumer, id("assembly_machine/round_airlock_door"));

        GenericMachineRecipeBuilder.assembly("ass.secureaccess", 400, 100)
                .inputLegacyOre("plateCastSteel", 12)
                .inputItem(item("plate_polymer"), 8)
                .inputItem(item("motor"), 4)
                .inputLegacyOre("boltDuraSteel", 32)
                .inputLegacyOre("dyeRed", 8)
                .outputItem(ModBlocks.SECURE_ACCESS_DOOR.get())
                .sourceOrder(66)
                .save(consumer, id("assembly_machine/secure_access_door"));

        GenericMachineRecipeBuilder.assembly("ass.slidingseal", 200, 100)
                .inputLegacyOre("plateSteel", 12)
                .inputItem(item("plate_polymer"), 4)
                .inputItem(item("motor"), 2)
                .inputLegacyOre("boltDuraSteel", 4)
                .inputLegacyOre("dyeWhite", 2)
                .outputItem(ModBlocks.SLIDING_SEAL_DOOR.get())
                .sourceOrder(67)
                .save(consumer, id("assembly_machine/sliding_seal_door"));

        GenericMachineRecipeBuilder.assembly("ass.silohatch", 200, 100)
                .inputLegacyOre("plateWeldedSteel", 4)
                .inputItem(item("plate_polymer"), 4)
                .inputItem(item("motor"), 2)
                .inputLegacyOre("boltSteel", 16)
                .inputLegacyOre("dyeGreen", 4)
                .outputItem(ModBlocks.SILO_HATCH.get())
                .sourceOrder(68)
                .save(consumer, id("assembly_machine/silo_hatch"));

        GenericMachineRecipeBuilder.assembly("ass.silohatchlarge", 300, 100)
                .inputLegacyOre("plateWeldedSteel", 6)
                .inputItem(item("plate_polymer"), 8)
                .inputItem(item("motor"), 2)
                .inputLegacyOre("boltSteel", 16)
                .inputLegacyOre("dyeGreen", 8)
                .outputItem(ModBlocks.SILO_HATCH_LARGE.get())
                .sourceOrder(69)
                .save(consumer, id("assembly_machine/silo_hatch_large"));

        GenericMachineRecipeBuilder.assembly("ass.capnuka", 10, 100)
                .inputItem(ModItems.CAP_NUKA.get(), 64)
                .inputItem(ModItems.CAP_NUKA.get(), 64)
                .outputItem(ModBlocks.BLOCK_CAP_NUKA.get())
                .sourceOrder(70)
                .save(consumer, id("assembly_machine/cap_nuka"));

        GenericMachineRecipeBuilder.assembly("ass.capquantum", 10, 100)
                .inputItem(ModItems.CAP_QUANTUM.get(), 64)
                .inputItem(ModItems.CAP_QUANTUM.get(), 64)
                .outputItem(ModBlocks.BLOCK_CAP_QUANTUM.get())
                .sourceOrder(71)
                .save(consumer, id("assembly_machine/cap_quantum"));

        GenericMachineRecipeBuilder.assembly("ass.capsparkle", 10, 100)
                .inputItem(ModItems.CAP_SPARKLE.get(), 64)
                .inputItem(ModItems.CAP_SPARKLE.get(), 64)
                .outputItem(ModBlocks.BLOCK_CAP_SPARKLE.get())
                .sourceOrder(72)
                .save(consumer, id("assembly_machine/cap_sparkle"));

        GenericMachineRecipeBuilder.assembly("ass.caprad", 10, 100)
                .inputItem(ModItems.CAP_RAD.get(), 64)
                .inputItem(ModItems.CAP_RAD.get(), 64)
                .outputItem(ModBlocks.BLOCK_CAP_RAD.get())
                .sourceOrder(73)
                .save(consumer, id("assembly_machine/cap_rad"));

        GenericMachineRecipeBuilder.assembly("ass.capfritz", 10, 100)
                .inputItem(ModItems.CAP_FRITZ.get(), 64)
                .inputItem(ModItems.CAP_FRITZ.get(), 64)
                .outputItem(ModBlocks.BLOCK_CAP_FRITZ.get())
                .sourceOrder(74)
                .save(consumer, id("assembly_machine/cap_fritz"));

        GenericMachineRecipeBuilder.assembly("ass.capkorl", 10, 100)
                .inputItem(ModItems.CAP_KORL.get(), 64)
                .inputItem(ModItems.CAP_KORL.get(), 64)
                .outputItem(ModBlocks.BLOCK_CAP_KORL.get())
                .sourceOrder(75)
                .save(consumer, id("assembly_machine/cap_korl"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorgold", 100, 100)
                .inputLegacyOre("plateSteel", 8)
                .inputLegacyOre("wireDenseGold", 16)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 7)
                .sourceOrder(139)
                .save(consumer, id("assembly_machine/capacitorgold"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorniobium", 100, 1_000)
                .inputLegacyOre("ingotAnyPlastic", 12)
                .inputLegacyOre("wireDenseNiobium", 24)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 8)
                .sourceOrder(140)
                .save(consumer, id("assembly_machine/capacitorniobium"));

        GenericMachineRecipeBuilder.assembly("ass.capacitortantalum", 100, 10_000)
                .inputLegacyOre("ingotAnyHardPlastic", 16)
                .inputLegacyOre("ingotTantalum", 24)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 9)
                .sourceOrder(141)
                .save(consumer, id("assembly_machine/capacitortantalum"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorbismuth", 100, 25_000)
                .inputLegacyOre("ingotAnyHardPlastic", 24)
                .inputLegacyOre("ingotBismuth", 24)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 16, 1)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 10)
                .sourceOrder(142)
                .save(consumer, id("assembly_machine/capacitorbismuth"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorspark", 100, 100_000)
                .inputLegacyOre("plateCastCMBSteel", 12)
                .inputItem(item("powder_spark_mix"), 32)
                .inputItem(item("pellet_charged"), 32)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 16, 16)
                .inputFluid(HbmFluids.PERFLUOROMETHYL_COLD, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 11)
                .outputFluid(HbmFluids.PERFLUOROMETHYL, 8_000)
                .sourceOrder(143)
                .save(consumer, id("assembly_machine/capacitorspark"));

        assemblyPlateRecipe(consumer, "ass.plateschrab", "schrabidium", "ingotSchrabidium", "plate_schrabidium", 7);
        assemblyPlateRecipe(consumer, "ass.platecmb", "combine_steel", "ingotCMBSteel", "plate_combine_steel", 8);
        assemblyPlateRecipe(consumer, "ass.plateweaponsteel", "weaponsteel", "ingotWeaponSteel", "plate_weaponsteel", 10);
        assemblyPlateRecipe(consumer, "ass.platesaturnite", "saturnite", "ingotSaturnite", "plate_saturnite", 11);
        assemblyPlateRecipe(consumer, "ass.platedura", "dura_steel", "ingotDuraSteel", "plate_dura_steel", 12);
        assemblyPlateRecipe(consumer, "ass.plategunmetal", "gunmetal", "ingotGunMetal", "plate_gunmetal", 9);

        GenericMachineRecipeBuilder.assembly("ass.dalekanium", 200, 100)
                .inputItem(block("block_meteor"), 1)
                .outputItem(item("plate_dalekanium"))
                .sourceOrder(14)
                .save(consumer, id("assembly_machine/dalekanium"));

        GenericMachineRecipeBuilder.assembly("ass.platemixed", 50, 100)
                .inputLegacyOre("plateCopper", 2)
                .inputItem(item("neutron_reflector"), 1)
                .inputLegacyOre("plateSaturnite", 1)
                .outputItem(new ItemStack(item("plate_mixed"), 4))
                .sourceOrder(13)
                .save(consumer, id("assembly_machine/plate_mixed"));

        GenericMachineRecipeBuilder.assembly("ass.platedesh", 200, 100)
                .inputLegacyOre("ingotWorkersAlloy", 4)
                .inputLegacyOre("dustAnyPlastic", 2)
                .inputLegacyOre("ingotDuraSteel", 1)
                .outputItem(new ItemStack(item("plate_desh"), 4))
                .sourceOrder(15)
                .save(consumer, id("assembly_machine/plate_desh"));

        GenericMachineRecipeBuilder.assembly("ass.platebismuth", 200, 100)
                .inputItem(item("nugget_bismuth"), 2)
                .inputLegacyOre("billetU238", 2)
                .inputLegacyOre("dustNiobium", 1)
                .outputItem(item("plate_bismuth"))
                .sourceOrder(16)
                .save(consumer, id("assembly_machine/plate_bismuth"));

        GenericMachineRecipeBuilder.assembly("ass.exsteelplating", 200, 400)
                .inputLegacyOre("plateCastSteel", 4)
                .inputLegacyOre("plateTitanium", 4)
                .inputLegacyOre("boltSteel", 16)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 0)
                .sourceOrder(17)
                .save(consumer, id("assembly_machine/expensive_steel_plating"));

        GenericMachineRecipeBuilder.assembly("ass.exheavyframe", 600, 800)
                .inputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 0, 3)
                .inputLegacyOre("ingotAnyPlastic", 8)
                .inputLegacyOre("plateSextupleCopper", 4)
                .inputLegacyOre("ingotWorkersAlloy", 1)
                .inputLegacyOre("boltDuraSteel", 32)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 1)
                .sourceOrder(18)
                .save(consumer, id("assembly_machine/expensive_heavy_frame"));

        GenericMachineRecipeBuilder.assembly("ass.excircuit", 400, 4_000)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 12)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 1, 8)
                .inputLegacyOre("ingotRubber", 4)
                .inputFluid(HbmFluids.SULFURIC_ACID, 1_000)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 2)
                .sourceOrder(19)
                .save(consumer, id("assembly_machine/expensive_circuit"));

        GenericMachineRecipeBuilder.assembly("ass.exleadplating", 400, 4_000)
                .inputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 0, 2)
                .inputLegacyOre("plateCastLead", 8)
                .inputLegacyOre("ingotBoron", 2)
                .inputLegacyOre("boltTungsten", 32)
                .inputFluid(HbmFluids.LUBRICANT, 1_000)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 3)
                .sourceOrder(20)
                .save(consumer, id("assembly_machine/expensive_lead_plating"));

        GenericMachineRecipeBuilder.assembly("ass.exferroplating", 1_200, 10_000)
                .inputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 3, 3)
                .inputLegacyOre("plateCastFerrouranium", 4)
                .inputLegacyOre("ingotAnyResistantAlloy", 4)
                .inputFluid(HbmFluids.UNSATURATEDS, 1_000)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 4)
                .sourceOrder(21)
                .save(consumer, id("assembly_machine/expensive_ferro_plating"));

        GenericMachineRecipeBuilder.assembly("ass.excomputer", 1_200, 16_000)
                .inputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 2, 3)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 4)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 10, 4)
                .inputItem(block("glass_quartz"), 8)
                .inputFluid(HbmFluids.PERFLUOROMETHYL, 2_000)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 5)
                .sourceOrder(22)
                .save(consumer, id("assembly_machine/expensive_computer"));

        GenericMachineRecipeBuilder.assembly("ass.bronzetubes", 3_000, 250_000)
                .inputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 1, 3)
                .inputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 4, 1)
                .inputLegacyOre("plateCastAnyBismoidBronze", 4)
                .inputLegacyOre("plateSextupleZirconium", 1)
                .inputFluid(HbmFluids.PERFLUOROMETHYL_COLD, 4_000)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 6)
                .outputFluid(HbmFluids.PERFLUOROMETHYL, 4_000)
                .sourceOrder(23)
                .save(consumer, id("assembly_machine/expensive_bronze_tubes"));

        GenericMachineRecipeBuilder.assembly("ass.explastic", 600, 20_000)
                .inputLegacyOre("ingotAnyHardPlastic", 4)
                .inputLegacyOre("ingotAnyPlastic", 16)
                .inputLegacyOre("ingotRubber", 8)
                .inputFluid(HbmFluids.SOLVENT, 1_000)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 7)
                .sourceOrder(24)
                .save(consumer, id("assembly_machine/expensive_plastic"));

        GenericMachineRecipeBuilder.assembly("ass.exgold", 600, 10_000)
                .inputLegacyOre("dustGold", 64)
                .inputLegacyOre("dustGold", 64)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 8)
                .sourceOrder(25)
                .save(consumer, id("assembly_machine/expensive_gold_dust"));

        GenericMachineRecipeBuilder.assembly("ass.hazcloth", 50, 100)
                .inputLegacyOre("dustLead", 4)
                .inputItem(Items.STRING, 8)
                .outputItem(new ItemStack(item("hazmat_cloth"), 4))
                .sourceOrder(26)
                .save(consumer, id("assembly_machine/hazmat_cloth"));

        GenericMachineRecipeBuilder.assembly("ass.firecloth", 50, 100)
                .inputLegacyOre("ingotAsbestos", 1)
                .inputItem(Items.STRING, 8)
                .outputItem(new ItemStack(item("asbestos_cloth"), 4))
                .sourceOrder(27)
                .save(consumer, id("assembly_machine/asbestos_cloth"));

        GenericMachineRecipeBuilder.assembly("ass.filtercoal", 50, 100)
                .inputLegacyOre("dustCoal", 4)
                .inputItem(Items.STRING, 2)
                .inputItem(Items.PAPER, 1)
                .outputItem(item("filter_coal"))
                .sourceOrder(28)
                .save(consumer, id("assembly_machine/filter_coal"));

        GenericMachineRecipeBuilder.assembly("ass.chip", 50, 250)
                .inputItem(item("plate_polymer"), 1)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 4, 1)
                .inputLegacyOre("wireFineGold", 1)
                .outputItem(item("circuit_chip"))
                .sourceOrder(29)
                .save(consumer, id("assembly_machine/chip"));

        GenericMachineRecipeBuilder.assembly("ass.chipBismoid", 100, 1_500)
                .inputItem(item("plate_polymer"), 2)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 4, 2)
                .inputLegacyOre("nuggetAnyBismoid", 1)
                .inputLegacyOre("wireFineGold", 2)
                .outputItem(item("circuit_chip_bismoid"))
                .sourceOrder(30)
                .save(consumer, id("assembly_machine/chip_bismoid"));

        GenericMachineRecipeBuilder.assembly("ass.chipQuantum", 200, 15_000)
                .inputLegacyOre("ingotAnyHardPlastic", 2)
                .inputLegacyOre("wireDenseBSCCO", 1)
                .inputItem(item("pellet_charged"), 1)
                .inputLegacyOre("wireFineGold", 8)
                .outputItem(item("circuit_chip_quantum"))
                .sourceOrder(31)
                .save(consumer, id("assembly_machine/chip_quantum"));

        GenericMachineRecipeBuilder.assembly("ass.atomicClock", 300, 1_000)
                .inputLegacyOre("ingotAnyPlastic", 2)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 5, 3)
                .inputLegacyOre("dustStrontium", 1)
                .outputItem(item("circuit_atomic_clock"))
                .sourceOrder(32)
                .save(consumer, id("assembly_machine/atomic_clock"));

        GenericMachineRecipeBuilder.assembly("ass.analogAlt", 200, 250)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 3, 4)
                .inputLegacyOre("ingotAnyPlastic", 4)
                .inputLegacyOre("wireFineTungsten", 8)
                .inputLegacyOre("ingotNiobium", 1)
                .outputItem(item("circuit_analog"))
                .pool(LegacyBlueprintPools.PREFIX_ALT + ".circuit")
                .customLocalization()
                .sourceOrder(33)
                .save(consumer, id("assembly_machine/analog_alt"));

        GenericMachineRecipeBuilder.assembly("ass.factorioChip", 300, 20_000)
                .inputLegacyOre("ingotRubber", 2)
                .inputLegacyOre("plateIron", 4)
                .inputLegacyOre("wireFineCopper", 8)
                .outputItem(item("circuit_basic"))
                .pool(LegacyBlueprintPools.PREFIX_ALT + ".circuit")
                .customLocalization()
                .sourceOrder(34)
                .save(consumer, id("assembly_machine/factorio_chip"));

        GenericMachineRecipeBuilder.assembly("ass.atomicClockAlt", 300, 20_000)
                .inputLegacyOre("ingotAnyPlastic", 4)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 5, 3)
                .inputLegacyOre("dustCs137", 1)
                .outputItem(new ItemStack(item("circuit_atomic_clock"), 4))
                .pool(LegacyBlueprintPools.PREFIX_ALT + ".circuit")
                .customLocalization()
                .sourceOrder(35)
                .save(consumer, id("assembly_machine/atomic_clock_alt"));

        GenericMachineRecipeBuilder.assembly("ass.centrifugetower", 100, 100)
                .inputLegacyOre("plateDuraSteel", 4)
                .inputLegacyOre("plateTitanium", 4)
                .inputItem(item("motor"), 1)
                .outputItem(item("centrifuge_element"))
                .sourceOrder(36)
                .save(consumer, id("assembly_machine/centrifuge_tower"));

        GenericMachineRecipeBuilder.assembly("ass.reactorcore", 100, 100)
                .inputLegacyOre("plateCastLead", 4)
                .inputLegacyOre("ingotBeryllium", 8)
                .inputLegacyOre("plateDuraSteel", 8)
                .inputLegacyOre("ingotAsbestos", 4)
                .outputItem(item("reactor_core"))
                .sourceOrder(37)
                .save(consumer, id("assembly_machine/reactor_core"));

        GenericMachineRecipeBuilder.assembly("ass.thermoelement", 60, 100)
                .inputLegacyOre("plateSteel", 1)
                .inputLegacyOre("wireFineMingrade", 2)
                .inputLegacyOre("dustNetherQuartz", 2)
                .outputItem(item("thermo_element"))
                .sourceOrder(38)
                .save(consumer, id("assembly_machine/thermo_element"));

        GenericMachineRecipeBuilder.assembly("ass.thermoelementsilicon", 60, 100)
                .inputLegacyOre("plateSteel", 1)
                .inputLegacyOre("wireFineGold", 2)
                .inputLegacyOre("billetSilicon", 1)
                .outputItem(item("thermo_element"))
                .sourceOrder(39)
                .save(consumer, id("assembly_machine/thermo_element_silicon"));

        GenericMachineRecipeBuilder.assembly("ass.rtgunit", 100, 100)
                .inputLegacyOre("plateCastLead", 2)
                .inputLegacyOre("plateCopper", 4)
                .inputItem(item("thermo_element"), 2)
                .outputItem(item("rtg_unit"))
                .sourceOrder(40)
                .save(consumer, id("assembly_machine/rtg_unit"));

        GenericMachineRecipeBuilder.assembly("ass.magnetron", 40, 100)
                .inputLegacyOre("plateCopper", 3)
                .inputLegacyOre("wireFineTungsten", 4)
                .outputItem(item("magnetron"))
                .sourceOrder(41)
                .save(consumer, id("assembly_machine/magnetron"));

        GenericMachineRecipeBuilder.assembly("ass.titaniumdrill", 100, 100)
                .inputLegacyOre("plateCastDuraSteel", 1)
                .inputLegacyOre("plateTitanium", 8)
                .outputItem(item("drill_titanium"))
                .sourceOrder(42)
                .save(consumer, id("assembly_machine/titanium_drill"));

        GenericMachineRecipeBuilder.assembly("ass.entanglementkit", 200, 100)
                .inputLegacyOre("plateCastDuraSteel", 4)
                .inputLegacyOre("plateCopper", 24)
                .inputLegacyOre("wireDenseGold", 16)
                .inputFluid(HbmFluids.XENON, 8_000)
                .outputItem(item("entanglement_kit"))
                .sourceOrder(43)
                .save(consumer, id("assembly_machine/entanglement_kit"));

        GenericMachineRecipeBuilder.assembly("ass.protoreactor", 200, 100)
                .inputLegacyOre("shellSteel", 4)
                .inputLegacyOre("plateCastLead", 4)
                .inputItem(item("rod_quad_empty"), 10)
                .inputLegacyOre("dyeBrown", 3)
                .outputItem(item("dysfunctional_reactor"))
                .sourceOrder(44)
                .save(consumer, id("assembly_machine/proto_reactor"));

        GenericMachineRecipeBuilder.assembly("ass.partlith", 40, 100)
                .inputLegacyOre("dustLithium", 1)
                .outputItem(new ItemStack(item("part_lithium"), 8))
                .sourceOrder(45)
                .save(consumer, id("assembly_machine/part_lithium"));

        GenericMachineRecipeBuilder.assembly("ass.partberyl", 40, 100)
                .inputLegacyOre("dustBeryllium", 1)
                .outputItem(new ItemStack(item("part_beryllium"), 8))
                .sourceOrder(46)
                .save(consumer, id("assembly_machine/part_beryllium"));

        GenericMachineRecipeBuilder.assembly("ass.partcoal", 40, 100)
                .inputLegacyOre("dustCoal", 1)
                .outputItem(new ItemStack(item("part_carbon"), 8))
                .sourceOrder(47)
                .save(consumer, id("assembly_machine/part_carbon"));

        GenericMachineRecipeBuilder.assembly("ass.partcop", 40, 100)
                .inputLegacyOre("dustCopper", 1)
                .outputItem(new ItemStack(item("part_copper"), 8))
                .sourceOrder(48)
                .save(consumer, id("assembly_machine/part_copper"));

        GenericMachineRecipeBuilder.assembly("ass.partplut", 40, 100)
                .inputLegacyOre("dustPlutonium", 1)
                .outputItem(new ItemStack(item("part_plutonium"), 8))
                .sourceOrder(49)
                .save(consumer, id("assembly_machine/part_plutonium"));

        GenericMachineRecipeBuilder.assembly("ass.cmbtile", 100, 100)
                .inputLegacyOre("anyConcrete", 4)
                .inputLegacyOre("plateCMBSteel", 4)
                .outputItem(new ItemStack(block("cmb_brick"), 8))
                .sourceOrder(50)
                .save(consumer, id("assembly_machine/cmb_tile"));

        GenericMachineRecipeBuilder.assembly("ass.cmbbrick", 100, 100)
                .inputLegacyOre("ingotMagnetizedTungsten", 8)
                .inputItem(block("ducrete"), 4)
                .inputItem(block("cmb_brick"), 8)
                .outputItem(new ItemStack(block("cmb_brick_reinforced"), 8))
                .sourceOrder(51)
                .save(consumer, id("assembly_machine/cmb_brick"));

        GenericMachineRecipeBuilder.assembly("ass.yellowbarrel", 400, 400)
                .inputItem(item("tank_steel"), 1)
                .inputLegacyOre("plateLead", 2)
                .inputItem(item("nuclear_waste"), 10)
                .outputItem(block("yellow_barrel"))
                .sourceOrder(54)
                .save(consumer, id("assembly_machine/yellow_barrel"));

        GenericMachineRecipeBuilder.assembly("ass.vitrifiedbarrel", 400, 400)
                .inputItem(item("tank_steel"), 1)
                .inputLegacyOre("plateLead", 2)
                .inputItem(item("nuclear_waste_vitrified"), 10)
                .outputItem(block("vitrified_barrel"))
                .sourceOrder(55)
                .save(consumer, id("assembly_machine/vitrified_barrel"));
    }

    private static void assemblyPlateRecipe(Consumer<FinishedRecipe> consumer, String internalName, String recipeName,
            String legacyOreName, String outputItem, int sourceOrder) {
        GenericMachineRecipeBuilder.assembly(internalName, 60, 100)
                .inputLegacyOre(legacyOreName, 1)
                .outputItem(item(outputItem))
                .pool(LegacyBlueprintPools.PREFIX_ALT + "plates")
                .autoSwitchGroup("autoswitch.plates")
                .sourceOrder(sourceOrder)
                .save(consumer, id("assembly_machine/plate_" + recipeName));
    }

    private static JsonObject ingredientItem(ItemLike item) {
        JsonObject object = new JsonObject();
        object.addProperty("item", HbmRegistryUtil.itemKey(item.asItem()).toString());
        return object;
    }

    private static JsonObject ingredientTag(TagKey<Item> tag) {
        JsonObject object = new JsonObject();
        object.addProperty("tag", tag.location().toString());
        return object;
    }

    private static JsonObject ingredientNbtItem(ItemLike item, String nbt) {
        JsonObject object = ingredientItem(item);
        object.addProperty("type", "forge:nbt");
        object.addProperty("nbt", nbt);
        return object;
    }

    private static void legacyTemFlakesRecipe(Consumer<FinishedRecipe> consumer, String name, int legacyDamage,
            JsonObject... ingredients) {
        consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                json.addProperty("category", "food");
                JsonArray ingredientArray = new JsonArray();
                for (JsonObject ingredient : ingredients) {
                    ingredientArray.add(ingredient.deepCopy());
                }
                json.add("ingredients", ingredientArray);

                JsonObject result = new JsonObject();
                result.addProperty("item", HbmRegistryUtil.itemKey(ModItems.TEM_FLAKES.get()).toString());
                result.addProperty("nbt", "{" + LegacyTemFlakesItem.TAG_LEGACY_DAMAGE + ":" + legacyDamage + "}");
                json.add("result", result);
            }

            @Override
            public ResourceLocation getId() {
                return id("consumables/" + name);
            }

            @Override
            public RecipeSerializer<?> getType() {
                return ModRecipes.LEGACY_NBT_SHAPELESS.get();
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }

    private static void legacyFullBatteryShapelessRecipe(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId,
            ItemLike resultItem, long charge, JsonObject... ingredients) {
        consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                json.addProperty("category", "redstone");
                JsonArray ingredientArray = new JsonArray();
                for (JsonObject ingredient : ingredients) {
                    ingredientArray.add(ingredient.deepCopy());
                }
                json.add("ingredients", ingredientArray);

                JsonObject result = new JsonObject();
                result.addProperty("item", HbmRegistryUtil.itemKey(resultItem.asItem()).toString());
                result.addProperty("nbt", "{charge:" + charge + "L}");
                json.add("result", result);
            }

            @Override
            public ResourceLocation getId() {
                return recipeId;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return ModRecipes.LEGACY_NBT_SHAPELESS.get();
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }

    private static void reactorAssemblyRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.assembly("ass.breedingreactor", 200, 100)
                .inputItem(item("reactor_core"), 1)
                .inputLegacyOre("ingotSteel", 12)
                .inputLegacyOre("plateLead", 16)
                .inputItem(block("reinforced_glass"), 4)
                .inputLegacyOre("ingotAsbestos", 4)
                .inputLegacyOre("ingotAnyResistantAlloy", 4)
                .inputItem(item("crt_display"), 1)
                .outputItem(ModBlocks.MACHINE_REACTOR_BREEDING.get())
                .sourceOrder(159)
                .save(consumer, id("assembly_machine/breeding_reactor"));

        GenericMachineRecipeBuilder.assembly("ass.researchreactor", 200, 100)
                .inputLegacyOre("ingotSteel", 8)
                .inputLegacyOre("ingotAnyResistantAlloy", 4)
                .inputItem(item("motor_desh"), 2)
                .inputLegacyOre("ingotBoron", 5)
                .inputLegacyOre("plateLead", 8)
                .inputItem(item("crt_display"), 3)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 4)
                .outputItem(ModBlocks.REACTOR_RESEARCH.get())
                .sourceOrder(160)
                .save(consumer, id("assembly_machine/research_reactor"));

        GenericMachineRecipeBuilder.assembly("ass.cirnox", 600, 100)
                .inputLegacyOre("shellSteel", 4)
                .inputLegacyOre("pipeSteel", 8)
                .inputLegacyOre("ingotBoron", 8)
                .inputLegacyOre("ingotGraphite", 16)
                .inputLegacyOre("ingotRubber", 16)
                .inputLegacyOre("anyConcrete", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 4)
                .outputItem(ModBlocks.REACTOR_ZIRNOX.get())
                .sourceOrder(161)
                .save(consumer, id("assembly_machine/zirnox_reactor"));

        GenericMachineRecipeBuilder.assembly("ass.fusioncore", 600, 100)
                .inputLegacyOre("plateWeldedAnyResistantAlloy", 8)
                .inputLegacyOre("ingotAnyHardPlastic", 32)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 12, 8)
                .outputItem(ModBlocks.STRUCT_TORUS_CORE.get())
                .sourceOrder(175)
                .save(consumer, id("assembly_machine/fusion_core_component"));

        GenericMachineRecipeBuilder.assembly("ass.fusionbscco", 100, 100)
                .inputLegacyOre("wireDenseBSCCO", 1)
                .inputLegacyOre("pipeCopper", 1)
                .inputLegacyOre("ingotAnyResistantAlloy", 1)
                .inputLegacyOre("ingotAnyPlastic", 4)
                .outputItem(new ItemStack(ModBlocks.FUSION_COMPONENT_BSCCO.get(), 2))
                .sourceOrder(176)
                .save(consumer, id("assembly_machine/fusion_bscco_component"));

        GenericMachineRecipeBuilder.assembly("ass.fusionblanket", 100, 100)
                .inputLegacyOre("plateWeldedTungsten", 1)
                .inputLegacyOre("plateWeldedSteel", 2)
                .inputLegacyOre("ingotBeryllium", 4)
                .outputItem(new ItemStack(ModBlocks.FUSION_COMPONENT_BLANKET.get(), 4))
                .sourceOrder(177)
                .save(consumer, id("assembly_machine/fusion_blanket_component"));

        GenericMachineRecipeBuilder.assembly("ass.fusionpipes", 100, 100)
                .inputLegacyOre("ingotAnyHardPlastic", 4)
                .inputLegacyOre("pipeCopper", 2)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 1)
                .outputItem(new ItemStack(ModBlocks.FUSION_COMPONENT_MOTOR.get(), 4))
                .sourceOrder(178)
                .save(consumer, id("assembly_machine/fusion_pipes_component"));

        GenericMachineRecipeBuilder.assembly("ass.fusionklystron", 300, 100)
                .inputLegacyOre("plateWeldedTungsten", 4)
                .inputLegacyOre("plateCastAnyResistantAlloy", 16)
                .inputLegacyOre("plateCopper", 32)
                .inputLegacyOre("ingotAnyHardPlastic", 16)
                .inputLegacyOre("wireDenseBSCCO", 8)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 12, 2)
                .outputItem(ModBlocks.FUSION_KLYSTRON.get())
                .sourceOrder(179)
                .save(consumer, id("assembly_machine/fusion_klystron"));

        GenericMachineRecipeBuilder.assembly("ass.fusioncollector", 300, 100)
                .inputLegacyOre("plateCastAnyResistantAlloy", 4)
                .inputLegacyOre("plateSteel", 16)
                .inputLegacyOre("ingotGraphite", 16)
                .inputLegacyOre("ingotAnyHardPlastic", 4)
                .outputItem(ModBlocks.FUSION_COLLECTOR.get())
                .sourceOrder(180)
                .save(consumer, id("assembly_machine/fusion_collector"));

        GenericMachineRecipeBuilder.assembly("ass.fusionbreeder", 300, 100)
                .inputLegacyOre("plateCastAnyResistantAlloy", 4)
                .inputLegacyOre("pipeSteel", 4)
                .inputLegacyOre("ingotBoron", 16)
                .inputLegacyOre("ingotAnyHardPlastic", 16)
                .outputItem(ModBlocks.FUSION_BREEDER.get())
                .sourceOrder(181)
                .save(consumer, id("assembly_machine/fusion_breeder"));

        GenericMachineRecipeBuilder.assembly("ass.fusionboiler", 300, 100)
                .inputLegacyOre("plateCastAnyResistantAlloy", 16)
                .inputLegacyOre("shellCopper", 16)
                .inputLegacyOre("pipeSteel", 8)
                .inputLegacyOre("ingotAnyHardPlastic", 16)
                .outputItem(ModBlocks.FUSION_BOILER.get())
                .sourceOrder(182)
                .save(consumer, id("assembly_machine/fusion_boiler"));

        GenericMachineRecipeBuilder.assembly("ass.fusionmhdt", 1_200, 100)
                .inputLegacyOre("plateWeldedAnyResistantAlloy", 16)
                .inputLegacyOre("plateWeldedCopper", 64)
                .inputLegacyOre("plateCastAnyBismoidBronze", 16)
                .inputLegacyOre("wireDenseSchrabidium", 64)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 4)
                .outputItem(ModBlocks.FUSION_MHDT.get())
                .sourceOrder(183)
                .save(consumer, id("assembly_machine/fusion_mhdt"));

        GenericMachineRecipeBuilder.assembly("ass.fusioncoupler", 300, 100)
                .inputLegacyOre("plateWeldedAnyResistantAlloy", 4)
                .inputLegacyOre("plateCopper", 32)
                .inputLegacyOre("wireDenseBSCCO", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 12, 4)
                .outputItem(ModBlocks.FUSION_COUPLER.get())
                .sourceOrder(184)
                .save(consumer, id("assembly_machine/fusion_coupler"));

        GenericMachineRecipeBuilder.assembly("ass.fusionplasmaforge", 1_200, 100)
                .inputLegacyOre("plateWeldedAnyResistantAlloy", 8)
                .inputLegacyOre("wireDenseBSCCO", 32)
                .inputLegacyOre("plateCastAnyBismoidBronze", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 12, 4)
                .outputItem(ModBlocks.FUSION_PLASMA_FORGE.get())
                .sourceOrder(185)
                .save(consumer, id("assembly_machine/fusion_plasma_forge"));

        GenericMachineRecipeBuilder.assembly("ass.watzrod", 200, 100)
                .inputLegacyOre("plateCastSteel", 2)
                .inputLegacyOre("ingotZirconium", 2)
                .inputLegacyOre("ingotSaturnite", 2)
                .inputLegacyOre("ingotAnyHardPlastic", 4)
                .outputItem(new ItemStack(ModBlocks.WATZ_ELEMENT.get(), 3))
                .sourceOrder(186)
                .save(consumer, id("assembly_machine/watz_element"));

        GenericMachineRecipeBuilder.assembly("ass.watzcooler", 200, 100)
                .inputLegacyOre("plateCastSteel", 2)
                .inputLegacyOre("plateCastCopper", 4)
                .inputLegacyOre("ingotRubber", 2)
                .outputItem(new ItemStack(ModBlocks.WATZ_COOLER.get(), 3))
                .sourceOrder(187)
                .save(consumer, id("assembly_machine/watz_cooler"));

        GenericMachineRecipeBuilder.assembly("ass.watzcasing", 100, 100)
                .inputLegacyOre("plateWeldedAnyResistantAlloy", 1)
                .inputLegacyOre("ingotBoron", 3)
                .inputLegacyOre("plateWeldedSteel", 2)
                .outputItem(new ItemStack(ModBlocks.WATZ_END.get(), 3))
                .sourceOrder(188)
                .save(consumer, id("assembly_machine/watz_end"));
    }

    private static void pwrAssemblyRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.assembly("ass.pwrfuel", 200, 500)
                .inputLegacyOre("plateCastLead", 4)
                .inputLegacyOre("plateWeldedZirconium", 2)
                .outputItem(new ItemStack(ModBlocks.PWR_FUEL.get(), 4))
                .sourceOrder(164)
                .save(consumer, id("assembly_machine/pwr_fuel"));

        GenericMachineRecipeBuilder.assembly("ass.pwrcontrol", 200, 500)
                .inputLegacyOre("plateCastSteel", 2)
                .inputLegacyOre("ingotBoron", 4)
                .inputItem(ModItems.MOTOR.get(), 1)
                .outputItem(new ItemStack(ModBlocks.PWR_CONTROL.get(), 4))
                .sourceOrder(165)
                .save(consumer, id("assembly_machine/pwr_control"));

        GenericMachineRecipeBuilder.assembly("ass.pwrchannel", 200, 500)
                .inputLegacyOre("pipeSteel", 4)
                .inputLegacyOre("plateCopper", 4)
                .outputItem(new ItemStack(ModBlocks.PWR_CHANNEL.get(), 4))
                .sourceOrder(166)
                .save(consumer, id("assembly_machine/pwr_channel"));

        GenericMachineRecipeBuilder.assembly("ass.pwrheatex", 200, 500)
                .inputLegacyOre("plateCastCopper", 4)
                .inputItem(ModItems.MOTOR.get(), 1)
                .inputFluid(HbmFluids.PERFLUOROMETHYL, 4_000)
                .outputItem(new ItemStack(ModBlocks.PWR_HEATEX.get(), 4))
                .sourceOrder(167)
                .save(consumer, id("assembly_machine/pwr_heatex"));

        GenericMachineRecipeBuilder.assembly("ass.pwrheatsink", 200, 500)
                .inputLegacyOre("plateCastAnyBismoidBronze", 4)
                .inputLegacyOre("plateCastCopper", 4)
                .outputItem(new ItemStack(ModBlocks.PWR_HEATSINK.get(), 4))
                .sourceOrder(168)
                .save(consumer, id("assembly_machine/pwr_heatsink"));

        GenericMachineRecipeBuilder.assembly("ass.pwrreflector", 200, 500)
                .inputLegacyOre("plateCastSteel", 2)
                .inputItem(item("neutron_reflector"), 4)
                .outputItem(new ItemStack(ModBlocks.PWR_REFLECTOR.get(), 4))
                .sourceOrder(169)
                .save(consumer, id("assembly_machine/pwr_reflector"));

        GenericMachineRecipeBuilder.assembly("ass.pwrreflectoralt", 200, 500)
                .inputLegacyOre("plateCastSteel", 2)
                .inputLegacyOre("plateWeaponSteel", 16)
                .outputItem(new ItemStack(ModBlocks.PWR_REFLECTOR.get(), 4))
                .sourceOrder(170)
                .save(consumer, id("assembly_machine/pwr_reflector_alt"));

        GenericMachineRecipeBuilder.assembly("ass.pwrcasing", 200, 500)
                .inputLegacyOre("plateLead", 4)
                .inputLegacyOre("anyConcrete", 4)
                .outputItem(new ItemStack(ModBlocks.PWR_CASING.get(), 4))
                .sourceOrder(171)
                .save(consumer, id("assembly_machine/pwr_casing"));

        GenericMachineRecipeBuilder.assembly("ass.pwrcontroller", 200, 500)
                .inputLegacyOre("plateCastLead", 4)
                .inputLegacyOre("ingotRubber", 4)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 4)
                .outputItem(ModBlocks.PWR_CONTROLLER.get())
                .sourceOrder(172)
                .save(consumer, id("assembly_machine/pwr_controller"));

        GenericMachineRecipeBuilder.assembly("ass.pwrport", 200, 500)
                .inputLegacyOre("plateLead", 4)
                .inputLegacyOre("anyConcrete", 4)
                .inputLegacyOre("pipeSteel", 4)
                .outputItem(new ItemStack(ModBlocks.PWR_PORT.get(), 4))
                .sourceOrder(173)
                .save(consumer, id("assembly_machine/pwr_port"));

        GenericMachineRecipeBuilder.assembly("ass.pwrneutronsource", 200, 500)
                .inputLegacyOre("plateWeldedZirconium", 1)
                .inputItem(item("billet_ra226be"), 3)
                .outputItem(ModBlocks.PWR_NEUTRON_SOURCE.get())
                .sourceOrder(174)
                .save(consumer, id("assembly_machine/pwr_neutron_source"));
    }

    private static void watzPelletRecipes(Consumer<FinishedRecipe> consumer) {
        watzPellet(consumer, "schrabidium", forgeTag("ingots/schrabidium"));
        watzPellet(consumer, "hes", item("ingot_hes"));
        watzPellet(consumer, "mes", item("ingot_schrabidium_fuel"));
        watzPellet(consumer, "les", item("ingot_les"));
        watzPellet(consumer, "hen", forgeTag("ingots/neptunium"));
        watzPellet(consumer, "meu", item("ingot_uranium_fuel"));
        watzPellet(consumer, "mep", item("ingot_pu_mix"));
        watzPellet(consumer, "lead", forgeTag("ingots/lead"));
        watzPellet(consumer, "boron", forgeTag("ingots/boron"));
        watzPellet(consumer, "du", item("ingot_u238"));
        conditionalWatzPellet(consumer, "nqd", forgeTag("ingots/naquadah_enriched"));
        conditionalWatzPellet(consumer, "nqr", forgeTag("ingots/naquadria"));
    }

    private static void watzPellet(Consumer<FinishedRecipe> consumer, String pelletSuffix, ItemLike ingot) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("watz_pellet_" + pelletSuffix))
                .pattern(" I ")
                .pattern("IGI")
                .pattern(" I ")
                .define('G', forgeTag("ingots/graphite"))
                .define('I', ingot)
                .unlockedBy("has_" + HbmRegistryUtil.itemKey(ingot.asItem()).getPath(), has(ingot))
                .save(consumer, id("watz/watz_pellet_" + pelletSuffix));
    }

    private static void watzPellet(Consumer<FinishedRecipe> consumer, String pelletSuffix, TagKey<Item> ingot) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("watz_pellet_" + pelletSuffix))
                .pattern(" I ")
                .pattern("IGI")
                .pattern(" I ")
                .define('G', forgeTag("ingots/graphite"))
                .define('I', ingot)
                .unlockedBy("has_" + ingot.location().getPath().replace('/', '_'), has(ingot))
                .save(consumer, id("watz/watz_pellet_" + pelletSuffix));
    }

    private static void conditionalWatzPellet(Consumer<FinishedRecipe> consumer, String pelletSuffix,
            TagKey<Item> ingot) {
        JsonObject payload = new JsonObject();
        payload.addProperty("category", "misc");
        JsonArray conditions = new JsonArray();
        conditions.add(notTagEmptyCondition(ingot));
        payload.add("conditions", conditions);

        JsonArray pattern = new JsonArray();
        pattern.add(" I ");
        pattern.add("IGI");
        pattern.add(" I ");
        payload.add("pattern", pattern);

        JsonObject key = new JsonObject();
        JsonObject graphite = new JsonObject();
        graphite.addProperty("tag", "forge:ingots/graphite");
        key.add("G", graphite);
        JsonObject ingotKey = new JsonObject();
        ingotKey.addProperty("tag", ingot.location().toString());
        key.add("I", ingotKey);
        payload.add("key", key);

        JsonObject result = new JsonObject();
        result.addProperty("item", id("watz_pellet_" + pelletSuffix).toString());
        payload.add("result", result);
        payload.addProperty("show_notification", true);

        consumer.accept(finishedRecipe(id("watz/watz_pellet_" + pelletSuffix), payload,
                RecipeSerializer.SHAPED_RECIPE));
    }

    private static JsonObject notTagEmptyCondition(TagKey<Item> tag) {
        JsonObject tagEmpty = new JsonObject();
        tagEmpty.addProperty("type", "forge:tag_empty");
        tagEmpty.addProperty("tag", tag.location().toString());
        JsonObject not = new JsonObject();
        not.addProperty("type", "forge:not");
        not.add("value", tagEmpty);
        return not;
    }

    private static void assemblyMachineBodyRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.assembly("ass.shredder", 100, 100)
                .inputLegacyOre("plateSteel", 8)
                .inputLegacyOre("plateCopper", 4)
                .inputItem(ModItems.MOTOR.get(), 2)
                .outputItem(ModBlocks.MACHINE_SHREDDER.get())
                .sourceOrder(76)
                .save(consumer, id("assembly_machine/shredder"));

        GenericMachineRecipeBuilder.assembly("ass.assembler", 200, 100)
                .inputLegacyOre("ingotSteel", 4)
                .inputLegacyOre("plateCopper", 4)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 7), 1)
                .outputItem(ModBlocks.MACHINE_ASSEMBLY_MACHINE.get())
                .sourceOrder(77)
                .save(consumer, id("assembly_machine/assembler"));

        GenericMachineRecipeBuilder.assembly("ass.chemplant", 200, 100)
                .inputLegacyOre("ingotSteel", 8)
                .inputLegacyOre("ntmpipeCopper", 2)
                .inputItem(item("plate_polymer"), 16)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputItem(ModItems.TUNGSTEN_COIL.get(), 2)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 7), 1)
                .outputItem(ModBlocks.MACHINE_CHEMICAL_PLANT.get())
                .sourceOrder(78)
                .save(consumer, id("assembly_machine/chemplant"));

        GenericMachineRecipeBuilder.assembly("ass.purex", 300, 100)
                .inputLegacyOre("shellSteel", 4)
                .inputLegacyOre("ntmpipeRubber", 8)
                .inputLegacyOre("plateTripleLead", 4)
                .inputItem(item("motor_desh"), 1)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8), 4)
                .outputItem(ModBlocks.MACHINE_PUREX.get())
                .sourceOrder(79)
                .save(consumer, id("assembly_machine/purex"));

        GenericMachineRecipeBuilder.assembly("ass.centrifuge", 200, 100)
                .inputItem(item("centrifuge_element"), 1)
                .inputLegacyOre("ingotAnyPlastic", 4)
                .inputLegacyOre("plateSteel", 8)
                .inputLegacyOre("plateCopper", 4)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 7), 1)
                .outputItem(ModBlocks.MACHINE_CENTRIFUGE.get())
                .sourceOrder(81)
                .save(consumer, id("assembly_machine/centrifuge"));

        GenericMachineRecipeBuilder.assembly("ass.gascent", 400, 100)
                .inputItem(item("centrifuge_element"), 4)
                .inputLegacyOre("ingotAnyPlastic", 8)
                .inputLegacyOre("ingotWorkersAlloy", 2)
                .inputLegacyOre("plateSteel", 8)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 9), 1)
                .outputItem(ModBlocks.MACHINE_GASCENT.get())
                .sourceOrder(82)
                .save(consumer, id("assembly_machine/gascent"));

        GenericMachineRecipeBuilder.assembly("ass.acidizer", 200, 100)
                .inputLegacyOre("plateSextupleSteel", 2)
                .inputLegacyOre("shellTitanium", 3)
                .inputLegacyOre("ingotDesh", 4)
                .inputItem(ModItems.MOTOR.get(), 1)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8), 2)
                .outputItem(ModBlocks.MACHINE_CRYSTALLIZER.get())
                .sourceOrder(84)
                .save(consumer, id("assembly_machine/acidizer"));

        GenericMachineRecipeBuilder.assembly("ass.electrolyzer", 200, 100)
                .inputLegacyOre("plateCastSteel", 8)
                .inputLegacyOre("plateCopper", 16)
                .inputLegacyOre("shellTitanium", 3)
                .inputLegacyOre("ingotRubber", 8)
                .inputItem(item("ingot_firebrick"), 16)
                .inputItem(ModItems.COPPER_COIL.get(), 16)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8), 8)
                .outputItem(ModBlocks.MACHINE_ELECTROLYSER.get())
                .sourceOrder(85)
                .save(consumer, id("assembly_machine/electrolyser"));

        GenericMachineRecipeBuilder.assembly("ass.derrick", 200, 100)
                .inputLegacyOre("plateSteel", 8)
                .inputLegacyOre("plateTripleCopper", 2)
                .inputLegacyOre("ntmpipeSteel", 4)
                .inputItem(ModItems.MOTOR.get(), 1)
                .inputItem(item("drill_titanium"), 1)
                .outputItem(ModBlocks.MACHINE_WELL.get())
                .sourceOrder(87)
                .save(consumer, id("assembly_machine/derrick"));

        GenericMachineRecipeBuilder.assembly("ass.pumpjack", 400, 100)
                .inputLegacyOre("plateDuraSteel", 8)
                .inputLegacyOre("plateSextupleSteel", 8)
                .inputLegacyOre("ntmpipeSteel", 12)
                .inputItem(item("motor_desh"), 1)
                .inputItem(item("drill_titanium"), 1)
                .outputItem(ModBlocks.MACHINE_PUMPJACK.get())
                .sourceOrder(88)
                .save(consumer, id("assembly_machine/pumpjack"));

        GenericMachineRecipeBuilder.assembly("ass.fracker", 600, 100)
                .inputLegacyOre("shellSteel", 24)
                .inputLegacyOre("ntmpipeSteel", 12)
                .inputItem(block("concrete_smooth"), 64)
                .inputItem(item("drill_titanium"), 1)
                .inputItem(item("motor_desh"), 2)
                .inputItem(item("plate_desh"), 24)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 1), 16)
                .outputItem(ModBlocks.MACHINE_FRACKING_TOWER.get())
                .sourceOrder(89)
                .save(consumer, id("assembly_machine/fracker"));

        GenericMachineRecipeBuilder.assembly("ass.flarestack", 100, 100)
                .inputLegacyOre("plateSteel", 12)
                .inputLegacyOre("plateCopper", 4)
                .inputLegacyOre("shellSteel", 4)
                .inputItem(item("thermo_element"), 3)
                .outputItem(ModBlocks.MACHINE_GASFLARE.get())
                .sourceOrder(90)
                .save(consumer, id("assembly_machine/flare_stack"));

        GenericMachineRecipeBuilder.assembly("ass.refinery", 200, 100)
                .inputLegacyOre("plateSextupleSteel", 3)
                .inputLegacyOre("plateCopper", 8)
                .inputLegacyOre("shellSteel", 4)
                .inputLegacyOre("ntmpipeSteel", 12)
                .inputItem(item("plate_polymer"), 8)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 7), 3)
                .outputItem(ModBlocks.MACHINE_REFINERY.get())
                .sourceOrder(91)
                .save(consumer, id("assembly_machine/refinery"));

        GenericMachineRecipeBuilder.assembly("ass.crackingtower", 200, 100)
                .inputItem(ModBlocks.STEEL_SCAFFOLD.get(), 16)
                .inputLegacyOre("shellSteel", 6)
                .inputLegacyOre("ingotDesh", 12)
                .inputLegacyOre("ingotNiobium", 4)
                .outputItem(ModBlocks.MACHINE_CATALYTIC_CRACKER.get())
                .sourceOrder(92)
                .save(consumer, id("assembly_machine/catalytic_cracker"));

        GenericMachineRecipeBuilder.assembly("ass.radiolysis", 200, 100)
                .inputLegacyOre("shellSteel", 4)
                .inputLegacyOre("ingotAnyResistantAlloy", 4)
                .inputLegacyOre("plateLead", 12)
                .inputLegacyOre("plateCastCopper", 4)
                .inputLegacyOre("ingotRubber", 8)
                .inputItem(item("thermo_element"), 8)
                .outputItem(ModBlocks.MACHINE_RADIOLYSIS.get())
                .sourceOrder(93)
                .save(consumer, id("assembly_machine/radiolysis"));

        GenericMachineRecipeBuilder.assembly("ass.rtg", 200, 100)
                .inputItem(item("rtg_unit"), 3)
                .inputLegacyOre("plateSteel", 4)
                .inputLegacyOre("wireFineMingrade", 16)
                .inputLegacyOre("ingotAnyPlastic", 4)
                .outputItem(ModBlocks.MACHINE_RTG_GREY.get())
                .sourceOrder(86)
                .save(consumer, id("assembly_machine/rtg"));

        GenericMachineRecipeBuilder.assembly("ass.forcefield", 600, 100)
                .inputLegacyOre("plateDuraSteel", 8)
                .inputItem(item("plate_desh"), 4)
                .inputItem(item("coil_gold_torus"), 6)
                .inputItem(item("coil_magnetized_tungsten"), 12)
                .inputItem(ModItems.MOTOR.get(), 1)
                .inputItem(ModItems.UPGRADE_RADIUS.get(), 1)
                .inputItem(ModItems.UPGRADE_HEALTH.get(), 1)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 4)
                .inputItem(block("machine_transformer"), 1)
                .outputItem(ModBlocks.MACHINE_FORCEFIELD.get())
                .sourceOrder(122)
                .save(consumer, id("assembly_machine/forcefield"));

        GenericMachineRecipeBuilder.assembly("ass.coker", 200, 100)
                .inputLegacyOre("plateSextupleSteel", 8)
                .inputLegacyOre("shellSteel", 4)
                .inputLegacyOre("plateCopper", 8)
                .inputLegacyOre("ingotRubber", 4)
                .inputLegacyOre("ingotNiobium", 4)
                .outputItem(ModBlocks.MACHINE_COKER.get())
                .sourceOrder(94)
                .save(consumer, id("assembly_machine/coker"));

        GenericMachineRecipeBuilder.assembly("ass.vaccumrefinery", 200, 100)
                .inputLegacyOre("plateCastSteel", 16)
                .inputLegacyOre("plateCopper", 16)
                .inputLegacyOre("ingotAnyResistantAlloy", 4)
                .inputItem(item("sphere_steel"), 1)
                .inputLegacyOre("ntmpipeSteel", 12)
                .inputItem(item("motor_desh"), 3)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 6), 4)
                .outputItem(ModBlocks.MACHINE_VACUUM_DISTILL.get())
                .sourceOrder(95)
                .save(consumer, id("assembly_machine/vacuum_refinery"));

        GenericMachineRecipeBuilder.assembly("ass.reformer", 200, 100)
                .inputLegacyOre("plateCastSteel", 12)
                .inputLegacyOre("plateCopper", 8)
                .inputLegacyOre("ingotNiobium", 8)
                .inputLegacyOre("ingotAnyResistantAlloy", 4)
                .inputLegacyOre("shellSteel", 3)
                .inputLegacyOre("ntmpipeSteel", 8)
                .inputItem(ModItems.MOTOR.get(), 1)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 11), 1)
                .outputItem(ModBlocks.MACHINE_CATALYTIC_REFORMER.get())
                .sourceOrder(96)
                .save(consumer, id("assembly_machine/catalytic_reformer"));

        GenericMachineRecipeBuilder.assembly("ass.hydrotreater", 200, 100)
                .inputLegacyOre("plateSextupleSteel", 8)
                .inputLegacyOre("plateCastCopper", 4)
                .inputLegacyOre("ingotNiobium", 8)
                .inputLegacyOre("ingotAnyResistantAlloy", 4)
                .inputLegacyOre("shellSteel", 2)
                .inputLegacyOre("ntmpipeSteel", 8)
                .inputItem(item("motor_desh"), 2)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 11), 1)
                .outputItem(ModBlocks.MACHINE_HYDROTREATER.get())
                .sourceOrder(97)
                .save(consumer, id("assembly_machine/hydrotreater"));

        GenericMachineRecipeBuilder.assembly("ass.pyrooven", 300, 100)
                .inputLegacyOre("plateSextupleSteel", 16)
                .inputLegacyOre("ingotAnyHardPlastic", 16)
                .inputItem(item("ingot_cft"), 4)
                .inputLegacyOre("ntmpipeCopper", 12)
                .inputItem(item("motor_desh"), 1)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 11), 1)
                .outputItem(ModBlocks.MACHINE_PYROOVEN.get())
                .sourceOrder(98)
                .save(consumer, id("assembly_machine/pyrooven"));

        GenericMachineRecipeBuilder.assembly("ass.liquefactor", 200, 100)
                .inputLegacyOre("shellSteel", 4)
                .inputLegacyOre("plateCopper", 12)
                .inputLegacyOre("anyTar", 4)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 1), 12)
                .inputItem(ModItems.TUNGSTEN_COIL.get(), 8)
                .outputItem(ModBlocks.MACHINE_LIQUEFACTOR.get())
                .sourceOrder(99)
                .save(consumer, id("assembly_machine/liquefactor"));

        GenericMachineRecipeBuilder.assembly("ass.solidifier", 200, 100)
                .inputLegacyOre("shellSteel", 4)
                .inputLegacyOre("plateAluminum", 12)
                .inputLegacyOre("ingotAnyPlastic", 4)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 1), 12)
                .inputItem(ModItems.COPPER_COIL.get(), 4)
                .outputItem(ModBlocks.MACHINE_SOLIDIFIER.get())
                .sourceOrder(100)
                .save(consumer, id("assembly_machine/solidifier"));

        GenericMachineRecipeBuilder.assembly("ass.compressor", 200, 100)
                .inputLegacyOre("plateCastSteel", 8)
                .inputLegacyOre("plateCopper", 4)
                .inputLegacyOre("shellSteel", 2)
                .inputItem(ModItems.MOTOR.get(), 3)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 7), 1)
                .outputItem(ModBlocks.MACHINE_COMPRESSOR.get())
                .sourceOrder(101)
                .save(consumer, id("assembly_machine/compressor"));

        GenericMachineRecipeBuilder.assembly("ass.compactcompressor", 200, 100)
                .inputLegacyOre("plateCastSteel", 8)
                .inputLegacyOre("shellTitanium", 4)
                .inputLegacyOre("ntmpipeCopper", 4)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 0), 4)
                .outputItem(ModBlocks.MACHINE_COMPRESSOR_COMPACT.get())
                .sourceOrder(102)
                .save(consumer, id("assembly_machine/compact_compressor"));

        GenericMachineRecipeBuilder.assembly("ass.silex", 400, 100)
                .inputItem(block("glass_quartz"), 16)
                .inputLegacyOre("plateCastSteel", 8)
                .inputLegacyOre("ingotWorkersAlloy", 4)
                .inputLegacyOre("ingotRubber", 8)
                .inputLegacyOre("ntmpipeSteel", 8)
                .outputItem(ModBlocks.MACHINE_SILEX.get())
                .sourceOrder(105)
                .save(consumer, id("assembly_machine/silex"));

        GenericMachineRecipeBuilder.assembly("ass.excavator", 200, 100)
                .inputItem(Blocks.STONE_BRICKS, 8)
                .inputLegacyOre("ingotSteel", 8)
                .inputLegacyOre("ingotIron", 8)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 7, 1)
                .outputItem(ModBlocks.MACHINE_EXCAVATOR.get())
                .sourceOrder(106)
                .save(consumer, id("assembly_machine/excavator"));

        GenericMachineRecipeBuilder.assembly("ass.drillsteel", 100, 100)
                .inputLegacyOre("ingotSteel", 12)
                .inputLegacyOre("ingotTungsten", 4)
                .outputLegacyMeta(LegacyMetaItemMappings.DRILLBIT, 0)
                .sourceOrder(107)
                .save(consumer, id("assembly_machine/drillbit_steel"));

        GenericMachineRecipeBuilder.assembly("ass.drillsteeldiamond", 100, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.DRILLBIT, 0, 1)
                .inputLegacyOre("dustDiamond", 16)
                .outputLegacyMeta(LegacyMetaItemMappings.DRILLBIT, 1)
                .sourceOrder(108)
                .save(consumer, id("assembly_machine/drillbit_steel_diamond"));

        GenericMachineRecipeBuilder.assembly("ass.drilldura", 100, 100)
                .inputLegacyOre("ingotDuraSteel", 12)
                .inputLegacyOre("ingotAnyPlastic", 12)
                .inputLegacyOre("ingotTitanium", 8)
                .outputLegacyMeta(LegacyMetaItemMappings.DRILLBIT, 2)
                .sourceOrder(109)
                .save(consumer, id("assembly_machine/drillbit_hss"));

        GenericMachineRecipeBuilder.assembly("ass.drillduradiamond", 100, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.DRILLBIT, 2, 1)
                .inputLegacyOre("dustDiamond", 24)
                .outputLegacyMeta(LegacyMetaItemMappings.DRILLBIT, 3)
                .sourceOrder(110)
                .save(consumer, id("assembly_machine/drillbit_hss_diamond"));

        GenericMachineRecipeBuilder.assembly("ass.drilldesh", 100, 100)
                .inputLegacyOre("ingotDesh", 16)
                .inputLegacyOre("ingotRubber", 12)
                .inputLegacyOre("ingotNiobium", 4)
                .outputLegacyMeta(LegacyMetaItemMappings.DRILLBIT, 4)
                .sourceOrder(111)
                .save(consumer, id("assembly_machine/drillbit_desh"));

        GenericMachineRecipeBuilder.assembly("ass.drilldeshdiamond", 100, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.DRILLBIT, 4, 1)
                .inputLegacyOre("dustDiamond", 32)
                .outputLegacyMeta(LegacyMetaItemMappings.DRILLBIT, 5)
                .sourceOrder(112)
                .save(consumer, id("assembly_machine/drillbit_desh_diamond"));

        GenericMachineRecipeBuilder.assembly("ass.drilltc", 100, 100)
                .inputLegacyOre("ingotAnyResistantAlloy", 20)
                .inputLegacyOre("ingotDesh", 12)
                .inputLegacyOre("ingotRubber", 8)
                .outputLegacyMeta(LegacyMetaItemMappings.DRILLBIT, 6)
                .sourceOrder(113)
                .save(consumer, id("assembly_machine/drillbit_tcalloy"));

        GenericMachineRecipeBuilder.assembly("ass.drilltcdiamond", 100, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.DRILLBIT, 6, 1)
                .inputLegacyOre("dustDiamond", 48)
                .outputLegacyMeta(LegacyMetaItemMappings.DRILLBIT, 7)
                .sourceOrder(114)
                .save(consumer, id("assembly_machine/drillbit_tcalloy_diamond"));

        GenericMachineRecipeBuilder.assembly("ass.drillferro", 100, 100)
                .inputLegacyOre("ingotFerrouranium", 24)
                .inputLegacyOre("ingotAnyResistantAlloy", 12)
                .inputLegacyOre("ingotBismuth", 4)
                .outputLegacyMeta(LegacyMetaItemMappings.DRILLBIT, 8)
                .sourceOrder(115)
                .save(consumer, id("assembly_machine/drillbit_ferro"));

        GenericMachineRecipeBuilder.assembly("ass.drillferrodiamond", 100, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.DRILLBIT, 8, 1)
                .inputLegacyOre("dustDiamond", 56)
                .outputLegacyMeta(LegacyMetaItemMappings.DRILLBIT, 9)
                .sourceOrder(116)
                .save(consumer, id("assembly_machine/drillbit_ferro_diamond"));

        GenericMachineRecipeBuilder.assembly("ass.slopper", 200, 100)
                .inputLegacyOre("plateCastSteel", 6)
                .inputLegacyOre("plateTitanium", 8)
                .inputLegacyOre("ntmpipeCopper", 3)
                .inputItem(ModItems.MOTOR.get(), 3)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 7), 1)
                .outputItem(ModBlocks.MACHINE_ORE_SLOPPER.get())
                .sourceOrder(117)
                .save(consumer, id("assembly_machine/ore_slopper"));

        GenericMachineRecipeBuilder.assembly("ass.rbmk", 100, 100)
                .inputItem(block("concrete_asbestos"), 4)
                .inputLegacyOre("plateCastSteel", 2)
                .inputLegacyOre("plateCopper", 4)
                .inputLegacyOre("ingotRubber", 2)
                .outputItem(ModBlocks.RBMK_BLANK.get())
                .sourceOrder(162)
                .save(consumer, id("assembly_machine/rbmk_blank"));

        GenericMachineRecipeBuilder.assembly("ass.rbmkautoloader", 100, 100)
                .inputLegacyOre("plateWeldedSteel", 4)
                .inputLegacyOre("plateCastLead", 4)
                .inputLegacyOre("ingotBoron", 4)
                .inputItem(ModItems.MOTOR.get(), 3)
                .outputItem(ModBlocks.RBMK_AUTOLOADER.get())
                .sourceOrder(163)
                .save(consumer, id("assembly_machine/rbmk_autoloader"));

        GenericMachineRecipeBuilder.assembly("ass.assemfac", 400, 100)
                .inputLegacyOre("ingotDuraSteel", 16)
                .inputLegacyOre("ingotAnyResistantAlloy", 8)
                .inputLegacyOre("ingotRubber", 16)
                .inputLegacyOre("ingotBoron", 8)
                .inputLegacyOre("shellSteel", 4)
                .inputItem(ModItems.MOTOR.get(), 12)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8), 16)
                .outputItem(ModBlocks.MACHINE_ASSEMBLY_FACTORY.get())
                .sourceOrder(124)
                .save(consumer, id("assembly_machine/assembly_factory"));

        GenericMachineRecipeBuilder.assembly("ass.chemfac", 400, 100)
                .inputLegacyOre("ingotDuraSteel", 16)
                .inputLegacyOre("ingotAnyResistantAlloy", 8)
                .inputLegacyOre("ingotRubber", 16)
                .inputLegacyOre("shellSteel", 12)
                .inputLegacyOre("ntmpipeCopper", 8)
                .inputItem(item("motor_desh"), 4)
                .inputItem(ModItems.TUNGSTEN_COIL.get(), 16)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8), 16)
                .outputItem(ModBlocks.MACHINE_CHEMICAL_FACTORY.get())
                .sourceOrder(125)
                .save(consumer, id("assembly_machine/chemical_factory"));

        GenericMachineRecipeBuilder.assembly("ass.combustiongen", 300, 100)
                .inputLegacyOre("plateSteel", 16)
                .inputLegacyOre("ingotCopper", 12)
                .inputLegacyOre("wireDenseGold", 8)
                .inputItem(item("canister_empty"), 4)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8), 1)
                .outputItem(ModBlocks.MACHINE_COMBUSTION_ENGINE.get())
                .sourceOrder(127)
                .save(consumer, id("assembly_machine/combustion_engine"));

        GenericMachineRecipeBuilder.assembly("ass.dieselgen", 200, 100)
                .inputLegacyOre("shellSteel", 1)
                .inputLegacyOre("plateCastCopper", 2)
                .inputItem(item("coil_copper"), 4)
                .outputItem(ModBlocks.MACHINE_DIESEL.get())
                .sourceOrder(126)
                .save(consumer, id("assembly_machine/diesel_generator"));

        GenericMachineRecipeBuilder.assembly("ass.launchpadsilo", 200, 100)
                .inputLegacyOre("plateWeldedSteel", 8)
                .inputLegacyOre("anyConcrete", 8)
                .inputLegacyOre("ingotAnyHardPlastic", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 4)
                .outputItem(ModBlocks.LAUNCH_PAD.get())
                .sourceOrder(269)
                .save(consumer, id("assembly_machine/launch_pad_silo"));

        GenericMachineRecipeBuilder.assembly("ass.launchpad", 200, 100)
                .inputLegacyOre("plateCastSteel", 6)
                .inputLegacyOre("anyConcrete", 64)
                .inputLegacyOre("ingotAnyPlastic", 16)
                .inputItem(ModBlocks.STEEL_SCAFFOLD.get(), 24)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 2)
                .outputItem(ModBlocks.LAUNCH_PAD_LARGE.get())
                .sourceOrder(268)
                .save(consumer, id("assembly_machine/launch_pad"));

        GenericMachineRecipeBuilder.assembly("ass.turbofan", 300, 100)
                .inputLegacyOre("shellTitanium", 8)
                .inputLegacyOre("ntmpipeDuraSteel", 4)
                .inputLegacyOre("ingotAnyPlastic", 12)
                .inputItem(item("turbine_tungsten"), 1)
                .inputLegacyOre("wireDenseGold", 12)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8), 3)
                .outputItem(ModBlocks.MACHINE_TURBOFAN.get())
                .sourceOrder(132)
                .save(consumer, id("assembly_machine/turbofan"));

        GenericMachineRecipeBuilder.assembly("ass.gasturbine", 400, 100)
                .inputLegacyOre("shellSteel", 10)
                .inputLegacyOre("wireDenseGold", 12)
                .inputLegacyOre("ntmpipeDuraSteel", 4)
                .inputLegacyOre("ntmpipeSteel", 4)
                .inputItem(item("turbine_tungsten"), 1)
                .inputItem(item("ingot_rubber"), 12)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8), 3)
                .outputItem(ModBlocks.MACHINE_TURBINEGAS.get())
                .sourceOrder(133)
                .save(consumer, id("assembly_machine/gas_turbine"));

        GenericMachineRecipeBuilder.assembly("ass.iturbine", 200, 100)
                .inputLegacyOre("plateSteel", 16)
                .inputLegacyOre("ingotRubber", 4)
                .inputItem(item("turbine_titanium"), 2)
                .inputLegacyOre("wireDenseGold", 4)
                .inputLegacyOre("ntmpipeDuraSteel", 4)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8), 2)
                .outputItem(ModBlocks.MACHINE_INDUSTRIAL_TURBINE.get())
                .sourceOrder(135)
                .save(consumer, id("assembly_machine/industrial_turbine"));

        GenericMachineRecipeBuilder.assembly("ass.pistonsetsteel", 200, 100)
                .inputLegacyOre("plateSteel", 16)
                .inputLegacyOre("plateCopper", 4)
                .inputLegacyOre("ingotTungsten", 8)
                .inputLegacyOre("boltTungsten", 16)
                .outputLegacyMeta(LegacyMetaItemMappings.PISTON_SET, 0)
                .sourceOrder(128)
                .save(consumer, id("assembly_machine/piston_set_steel"));

        GenericMachineRecipeBuilder.assembly("ass.pistonsetdura", 200, 100)
                .inputLegacyOre("ingotDuraSteel", 24)
                .inputLegacyOre("plateTitanium", 8)
                .inputLegacyOre("ingotTungsten", 8)
                .inputLegacyOre("boltDuraSteel", 16)
                .outputLegacyMeta(LegacyMetaItemMappings.PISTON_SET, 1)
                .sourceOrder(129)
                .save(consumer, id("assembly_machine/piston_set_dura"));

        GenericMachineRecipeBuilder.assembly("ass.pistonsetdesh", 200, 100)
                .inputLegacyOre("ingotDesh", 24)
                .inputLegacyOre("ingotAnyPlastic", 12)
                .inputLegacyOre("plateCopper", 24)
                .inputLegacyOre("ingotTungsten", 16)
                .inputLegacyOre("ntmpipeDuraSteel", 4)
                .outputLegacyMeta(LegacyMetaItemMappings.PISTON_SET, 2)
                .sourceOrder(130)
                .save(consumer, id("assembly_machine/piston_set_desh"));

        GenericMachineRecipeBuilder.assembly("ass.pistonsetstar", 200, 100)
                .inputLegacyOre("ingotStarmetal", 24)
                .inputLegacyOre("ingotRubber", 16)
                .inputLegacyOre("plateSaturnite", 24)
                .inputLegacyOre("ingotNiobium", 16)
                .inputLegacyOre("ntmpipeDuraSteel", 4)
                .outputLegacyMeta(LegacyMetaItemMappings.PISTON_SET, 3)
                .sourceOrder(131)
                .save(consumer, id("assembly_machine/piston_set_starmetal"));

        GenericMachineRecipeBuilder.assembly("ass.tank", 200, 100)
                .inputLegacyOre("plateSteel", 8)
                .inputLegacyOre("shellSteel", 4)
                .outputItem(ModBlocks.MACHINE_FLUIDTANK.get())
                .sourceOrder(144)
                .save(consumer, id("assembly_machine/tank"));

        GenericMachineRecipeBuilder.assembly("ass.bigasstank", 200, 100)
                .inputLegacyOre("plateSteel", 16)
                .inputLegacyOre("plateSextupleAnyResistantAlloy", 4)
                .inputItem(ModBlocks.STEEL_SCAFFOLD.get(), 16)
                .outputItem(ModBlocks.MACHINE_BIGASSTANK.get())
                .sourceOrder(145)
                .save(consumer, id("assembly_machine/big_ass_tank"));

        GenericMachineRecipeBuilder.assembly("ass.cyclotron", 600, 100)
                .inputItem(legacyBatteryPack(2), 1)
                .inputLegacyOre("wireDenseNeodymium", 32)
                .inputLegacyOre("plateCastSteel", 16)
                .inputLegacyOre("ingotAnyPlastic", 24)
                .inputLegacyOre("ingotRubber", 24)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8), 16)
                .outputItem(ModBlocks.MACHINE_CYCLOTRON.get())
                .sourceOrder(147)
                .save(consumer, id("assembly_machine/cyclotron"));

        GenericMachineRecipeBuilder.assembly("ass.beamline", 200, 100)
                .inputLegacyOre("plateCastSteel", 8)
                .inputLegacyOre("plateCopper", 16)
                .inputLegacyOre("wireDenseGold", 4)
                .outputItem(ModBlocks.PA_BEAMLINE.get())
                .sourceOrder(148)
                .save(consumer, id("assembly_machine/pa_beamline"));

        GenericMachineRecipeBuilder.assembly("ass.rfc", 400, 100)
                .inputItem(ModBlocks.PA_BEAMLINE.get(), 3)
                .inputLegacyOre("plateCastSteel", 16)
                .inputLegacyOre("plateCopper", 64)
                .inputLegacyOre("ingotAnyHardPlastic", 16)
                .inputItem(item("magnetron"), 16)
                .outputItem(ModBlocks.PA_RFC.get())
                .sourceOrder(149)
                .save(consumer, id("assembly_machine/pa_rfc"));

        GenericMachineRecipeBuilder.assembly("ass.quadrupole", 400, 100)
                .inputItem(ModBlocks.PA_BEAMLINE.get(), 1)
                .inputLegacyOre("plateCastSteel", 16)
                .inputLegacyOre("ingotAnyHardPlastic", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 11, 1)
                .outputItem(ModBlocks.PA_QUADRUPOLE.get())
                .sourceOrder(150)
                .save(consumer, id("assembly_machine/pa_quadrupole"));

        GenericMachineRecipeBuilder.assembly("ass.dipole", 400, 100)
                .inputItem(ModBlocks.PA_BEAMLINE.get(), 2)
                .inputLegacyOre("plateCastSteel", 16)
                .inputLegacyOre("ingotAnyHardPlastic", 32)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 11, 4)
                .outputItem(ModBlocks.PA_DIPOLE.get())
                .sourceOrder(151)
                .save(consumer, id("assembly_machine/pa_dipole"));

        GenericMachineRecipeBuilder.assembly("ass.source", 400, 100)
                .inputItem(ModBlocks.PA_BEAMLINE.get(), 3)
                .inputLegacyOre("plateCastSteel", 16)
                .inputLegacyOre("ingotAnyHardPlastic", 16)
                .inputItem(item("magnetron"), 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 15, 1)
                .outputItem(ModBlocks.PA_SOURCE.get())
                .sourceOrder(152)
                .save(consumer, id("assembly_machine/pa_source"));

        GenericMachineRecipeBuilder.assembly("ass.detector", 400, 100)
                .inputItem(ModBlocks.PA_BEAMLINE.get(), 3)
                .inputLegacyOre("plateCastSteel", 24)
                .inputLegacyOre("wireDenseGold", 16)
                .inputLegacyOre("ingotAnyHardPlastic", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 15, 4)
                .outputItem(ModBlocks.PA_DETECTOR.get())
                .sourceOrder(153)
                .save(consumer, id("assembly_machine/pa_detector"));

        GenericMachineRecipeBuilder.assembly("ass.pagold", 400, 100)
                .inputLegacyOre("wireDenseGold", 64)
                .inputLegacyOre("wireDenseGold", 64)
                .outputLegacyMeta(LegacyMetaItemMappings.PA_COIL, 0)
                .sourceOrder(154)
                .save(consumer, id("assembly_machine/pa_coil_gold"));

        GenericMachineRecipeBuilder.assembly("ass.panbti", 400, 100)
                .inputLegacyOre("wireDenseNiobium", 64)
                .inputLegacyOre("wireDenseTitanium", 64)
                .outputLegacyMeta(LegacyMetaItemMappings.PA_COIL, 1)
                .sourceOrder(155)
                .save(consumer, id("assembly_machine/pa_coil_niobium"));

        GenericMachineRecipeBuilder.assembly("ass.pabscco", 400, 100)
                .inputLegacyOre("wireDenseBSCCO", 64)
                .inputLegacyOre("ingotAnyPlastic", 64)
                .outputLegacyMeta(LegacyMetaItemMappings.PA_COIL, 2)
                .sourceOrder(156)
                .save(consumer, id("assembly_machine/pa_coil_bscco"));

        GenericMachineRecipeBuilder.assembly("ass.pachlorophyte", 400, 100)
                .inputLegacyOre("wireDenseCopper", 64)
                .inputLegacyOre("wireDenseCopper", 64)
                .inputItem(item("powder_chlorophyte"), 16)
                .outputLegacyMeta(LegacyMetaItemMappings.PA_COIL, 3)
                .sourceOrder(157)
                .save(consumer, id("assembly_machine/pa_coil_chlorophyte"));

        GenericMachineRecipeBuilder.assembly("ass.exposurechamber", 200, 100)
                .inputLegacyOre("plateCastAluminum", 12)
                .inputLegacyOre("ingotAnyResistantAlloy", 4)
                .inputLegacyOre("ingotAnyHardPlastic", 12)
                .inputLegacyOre("wireDenseGold", 32)
                .inputItem(item("motor_desh"), 2)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 11), 4)
                .inputItem(legacyBatteryPack(9), 1)
                .inputItem(block("glass_quartz"), 16)
                .outputItem(ModBlocks.MACHINE_EXPOSURE_CHAMBER.get())
                .sourceOrder(158)
                .save(consumer, id("assembly_machine/exposure_chamber"));

        GenericMachineRecipeBuilder.assembly("ass.radar", 300, 100)
                .inputLegacyOre("plateSteel", 12)
                .inputLegacyOre("ingotAnyRubber", 12)
                .inputItem(item("magnetron"), 5)
                .inputItem(ModItems.MOTOR.get(), 1)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8), 8)
                .inputItem(item("crt_display"), 4)
                .outputItem(ModBlocks.MACHINE_RADAR.get())
                .sourceOrder(120)
                .save(consumer, id("assembly_machine/radar"));

        GenericMachineRecipeBuilder.assembly("ass.radarlarge", 400, 100)
                .inputLegacyOre("plateSextupleSteel", 6)
                .inputLegacyOre("ingotAnyResistantAlloy", 4)
                .inputLegacyOre("ingotAnyRubber", 24)
                .inputItem(item("magnetron"), 16)
                .inputItem(item("motor_desh"), 1)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 9), 4)
                .inputItem(item("crt_display"), 4)
                .outputItem(ModBlocks.MACHINE_RADAR_LARGE.get())
                .sourceOrder(121)
                .save(consumer, id("assembly_machine/radar_large"));
    }

    private static void satelliteRecipes(Consumer<FinishedRecipe> consumer) {
        ItemLike advancedCircuit = legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 9);
        ItemLike basicCircuit = legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8);

        // 1.7.10 CraftingManager: crt display / BASIC circuit / steel plate.
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.RADAR_LINKER.get())
                .pattern("S")
                .pattern("C")
                .pattern("P")
                .define('S', item("crt_display"))
                .define('C', basicCircuit)
                .define('P', forgeTag("plates/steel"))
                .unlockedBy("has_basic_circuit", has(basicCircuit))
                .save(consumer, id("satellite/radar_linker"));

        // 1.7.10 CraftingManager: plastic/circuit/steel/CRT radar display.
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.MACHINE_RADAR_SCREEN.get())
                .pattern("PCP")
                .pattern("SRS")
                .pattern("PCP")
                .define('P', forgeTag("ingots/any_plastic"))
                .define('C', basicCircuit)
                .define('S', forgeTag("plates/steel"))
                .define('R', item("crt_display"))
                .unlockedBy("has_basic_circuit", has(basicCircuit))
                .save(consumer, id("satellite/radar_screen"));

        // 1.7.10 CraftingManager: decorative satellite receiver pole.
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.POLE_SATELLITE_RECEIVER.get())
                .pattern("SS ")
                .pattern("SCR")
                .pattern("SS ")
                .define('S', forgeTag("ingots/steel"))
                .define('C', forgeTag("circuits/vacuum_tube"))
                .define('R', forgeTag("wires/mingrade"))
                .unlockedBy("has_vacuum_tube_circuit", has(forgeTag("circuits/vacuum_tube")))
                .save(consumer, id("satellite/pole_satellite_receiver"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.MACHINE_SATLINKER.get())
                .pattern("PSP")
                .pattern("SCS")
                .pattern("PSP")
                .define('P', forgeTag("plates/steel"))
                .define('S', forgeTag("ingots/saturnite"))
                .define('C', ModItems.SAT_CHIP.get())
                .unlockedBy("has_sat_chip", has(ModItems.SAT_CHIP.get()))
                .save(consumer, id("satellite/machine_satlinker"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.SAT_CHIP.get())
                .pattern("WWW")
                .pattern("CIC")
                .pattern("WWW")
                .define('W', forgeTag("wires/mingrade"))
                .define('C', advancedCircuit)
                .define('I', forgeTag("ingots/any_plastic"))
                .unlockedBy("has_advanced_circuit", has(advancedCircuit))
                .save(consumer, id("satellite/sat_chip"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.SAT_INTERFACE.get())
                .pattern("ISI")
                .pattern("PCP")
                .pattern("PAP")
                .define('I', forgeTag("ingots/steel"))
                .define('S', forgeTag("ingots/saturnite"))
                .define('P', item("plate_polymer"))
                .define('C', ModItems.SAT_CHIP.get())
                .define('A', advancedCircuit)
                .unlockedBy("has_sat_chip", has(ModItems.SAT_CHIP.get()))
                .save(consumer, id("satellite/sat_interface"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.SAT_COORD.get())
                .pattern("SII")
                .pattern("SCA")
                .pattern("SPP")
                .define('I', forgeTag("ingots/steel"))
                .define('S', forgeTag("ingots/saturnite"))
                .define('P', item("plate_polymer"))
                .define('C', ModItems.SAT_CHIP.get())
                .define('A', advancedCircuit)
                .unlockedBy("has_sat_chip", has(ModItems.SAT_CHIP.get()))
                .save(consumer, id("satellite/sat_coord"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SAT_DESIGNATOR.get())
                .pattern("RRD")
                .pattern("PIC")
                .pattern("  P")
                .define('R', Items.REDSTONE)
                .define('D', ModItems.SAT_CHIP.get())
                .define('P', forgeTag("plates/gold"))
                .define('I', forgeTag("ingots/gold"))
                .define('C', advancedCircuit)
                .unlockedBy("has_sat_chip", has(ModItems.SAT_CHIP.get()))
                .save(consumer, id("satellite/sat_designator"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModItems.SAT_RELAY.get())
                .requires(ModItems.SAT_CHIP.get())
                .requires(item("ducttape"))
                .requires(ModItems.RADAR_LINKER.get())
                .unlockedBy("has_sat_chip", has(ModItems.SAT_CHIP.get()))
                .save(consumer, id("satellite/sat_relay"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STRUCT_LAUNCHER.get(), 8)
                .pattern("PPP")
                .pattern("SDS")
                .pattern("CCC")
                .define('P', forgeTag("plates/steel"))
                .define('S', block("steel_scaffold"))
                .define('D', forgeTag("pipes/steel"))
                .define('C', forgeTag("any/concrete"))
                .unlockedBy("has_steel_scaffold", has(block("steel_scaffold")))
                .save(consumer, id("satellite/struct_launcher"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STRUCT_SCAFFOLD.get(), 8)
                .pattern("SSS")
                .pattern("DCD")
                .pattern("SSS")
                .define('S', block("steel_scaffold"))
                .define('D', block("fluid_duct_neo"))
                .define('C', block("red_cable"))
                .unlockedBy("has_steel_scaffold", has(block("steel_scaffold")))
                .save(consumer, id("satellite/struct_scaffold"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STRUCT_LAUNCHER_CORE.get())
                .pattern("SCS")
                .pattern("SIS")
                .pattern("BEB")
                .define('S', block("steel_scaffold"))
                .define('C', item("circuit_basic"))
                .define('I', Items.IRON_BARS)
                .define('B', ModBlocks.STRUCT_LAUNCHER.get())
                .define('E', ModItems.BATTERY_LEAD.get())
                .unlockedBy("has_struct_launcher", has(ModBlocks.STRUCT_LAUNCHER.get()))
                .save(consumer, id("satellite/struct_launcher_core"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STRUCT_LAUNCHER_CORE_LARGE.get())
                .pattern("SIS")
                .pattern("ICI")
                .pattern("BEB")
                .define('S', item("circuit_advanced"))
                .define('I', Items.IRON_BARS)
                .define('C', item("circuit_advanced"))
                .define('B', ModBlocks.STRUCT_LAUNCHER.get())
                .define('E', ModItems.BATTERY_LEAD.get())
                .unlockedBy("has_struct_launcher", has(ModBlocks.STRUCT_LAUNCHER.get()))
                .save(consumer, id("satellite/struct_launcher_core_large"));

        GenericMachineRecipeBuilder.assembly("ass.soyuzcore", 1_200, 100)
                .inputLegacyOre("plateSextupleSteel", 16)
                .inputItem(item("upgrade_speed_3"), 1)
                .inputItem(item("upgrade_power_3"), 1)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 4)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 2, 1)
                .outputItem(ModBlocks.STRUCT_SOYUZ_CORE.get())
                .pool(LegacyBlueprintPools.PREFIX_DISCOVER + "soyuz")
                .sourceOrder(320)
                .save(consumer, id("assembly_machine/soyuz_core"));

        GenericMachineRecipeBuilder.assembly("ass.soyuz", 6_000, 100)
                .inputLegacyOre("shellTitanium", 32)
                .inputLegacyOre("ingotRubber", 64)
                .inputItem(item("rocket_fuel"), 64)
                .inputItem(item("thruster_small"), 12)
                .inputItem(item("thruster_medium"), 12)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 4)
                .inputLegacyMeta(LegacyMetaItemMappings.PART_GENERIC, 3, 32)
                .outputItem(ModItems.MISSILE_SOYUZ.get())
                .pool(LegacyBlueprintPools.PREFIX_DISCOVER + "soyuz")
                .sourceOrder(321)
                .save(consumer, id("assembly_machine/soyuz"));

        GenericMachineRecipeBuilder.assembly("ass.lander", 2_400, 100)
                .inputLegacyOre("shellAluminum", 4)
                .inputLegacyOre("ingotRubber", 16)
                .inputItem(item("rocket_fuel"), 16)
                .inputItem(item("thruster_small"), 3)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 14, 3)
                .inputLegacyMeta(LegacyMetaItemMappings.PART_GENERIC, 3, 12)
                .outputItem(ModItems.MISSILE_SOYUZ_LANDER.get())
                .pool(LegacyBlueprintPools.PREFIX_DISCOVER + "soyuz")
                .sourceOrder(322)
                .save(consumer, id("assembly_machine/soyuz_lander"));

        GenericMachineRecipeBuilder.assembly("ass.satellitebase", 600, 100)
                .inputLegacyOre("ingotRubber", 12)
                .inputLegacyOre("shellTitanium", 3)
                .inputItem(item("thruster_medium"), 1)
                .inputLegacyMeta(LegacyMetaItemMappings.PART_GENERIC, 3, 8)
                .inputItem(item("plate_desh"), 4)
                .inputItem(fluidContainerStack(ModItems.FLUID_BARREL_FULL.get(), 1, HbmFluids.KEROSENE, 16_000, 0))
                .inputItem(item("photo_panel"), 24)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 12)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 2, 1)
                .outputItem(item("sat_base"))
                .sourceOrder(323)
                .save(consumer, id("assembly_machine/satellite_base"));

        GenericMachineRecipeBuilder.assembly("ass.satellitemapper", 600, 100)
                .inputLegacyOre("shellSteel", 3)
                .inputItem(item("plate_desh"), 4)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 4)
                .inputItem(block("glass_quartz"), 8)
                .outputItem(item("sat_head_mapper"))
                .sourceOrder(324)
                .save(consumer, id("assembly_machine/satellite_mapper"));

        GenericMachineRecipeBuilder.assembly("ass.satellitescanner", 600, 100)
                .inputLegacyOre("shellSteel", 3)
                .inputLegacyOre("plateCastTitanium", 8)
                .inputItem(item("plate_desh"), 4)
                .inputItem(item("magnetron"), 8)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 8)
                .outputItem(item("sat_head_scanner"))
                .sourceOrder(325)
                .save(consumer, id("assembly_machine/satellite_scanner"));

        GenericMachineRecipeBuilder.assembly("ass.satelliteradar", 600, 100)
                .inputLegacyOre("shellSteel", 3)
                .inputLegacyOre("plateCastTitanium", 12)
                .inputItem(item("magnetron"), 12)
                .inputItem(ModItems.GOLD_COIL.get(), 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 4)
                .outputItem(item("sat_head_radar"))
                .sourceOrder(326)
                .save(consumer, id("assembly_machine/satellite_radar"));

        GenericMachineRecipeBuilder.assembly("ass.satellitelaser", 600, 100)
                .inputLegacyOre("shellSteel", 6)
                .inputLegacyOre("plateCastCopper", 24)
                .inputLegacyOre("ingotAnyHardplastic", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 14, 8)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 10, 16)
                .inputItem(item("crystal_diamond"), 8)
                .inputItem(block("glass_quartz"), 8)
                .outputItem(item("sat_head_laser"))
                .sourceOrder(327)
                .save(consumer, id("assembly_machine/satellite_laser"));

        GenericMachineRecipeBuilder.assembly("ass.satelliteresonator", 600, 100)
                .inputLegacyOre("plateCastSteel", 6)
                .inputLegacyOre("ingotSaturnite", 12)
                .inputLegacyOre("ingotAnyPlastic", 48)
                .inputItem(item("crystal_xen"), 1)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 16)
                .outputItem(item("sat_head_resonator"))
                .sourceOrder(328)
                .save(consumer, id("assembly_machine/satellite_resonator"));

        GenericMachineRecipeBuilder.assembly("ass.satelliterelay", 600, 100)
                .inputLegacyOre("shellTitanium", 3)
                .inputItem(item("plate_desh"), 8)
                .inputItem(fluidContainerStack(ModItems.FLUID_BARREL_FULL.get(), 1, HbmFluids.HYDROGEN, 16_000, 0))
                .inputItem(item("photo_panel"), 16)
                .inputItem(item("thruster_nuclear"), 1)
                .inputItem(item("ingot_uranium_fuel"), 6)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 24)
                .inputItem(item("magnetron"), 3)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 2, 1)
                .outputItem(ModItems.SAT_FOEQ.get())
                .sourceOrder(329)
                .save(consumer, id("assembly_machine/satellite_relay"));

        GenericMachineRecipeBuilder.assembly("ass.satelliteasteroidminer", 600, 100)
                .inputLegacyOre("plateSaturnite", 24)
                .inputItem(item("motor_desh"), 2)
                .inputItem(item("drill_titanium"), 2)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 12)
                .inputItem(fluidContainerStack(ModItems.FLUID_BARREL_FULL.get(), 1, HbmFluids.KEROSENE, 16_000, 0))
                .inputItem(item("thruster_small"), 1)
                .inputItem(item("photo_panel"), 12)
                .inputItem(item("centrifuge_element"), 4)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 2, 1)
                .outputItem(ModItems.SAT_MINER.get())
                .sourceOrder(330)
                .save(consumer, id("assembly_machine/satellite_asteroid_miner"));

        GenericMachineRecipeBuilder.assembly("ass.satellitelunarminer", 600, 100)
                .inputItem(item("ingot_meteorite"), 4)
                .inputItem(item("plate_desh"), 4)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputItem(item("drill_titanium"), 2)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 8)
                .inputItem(fluidContainerStack(ModItems.FLUID_BARREL_FULL.get(), 1, HbmFluids.KEROSENE, 16_000, 0))
                .inputItem(item("thruster_small"), 1)
                .inputItem(item("photo_panel"), 12)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 2, 1)
                .outputItem(ModItems.SAT_LUNAR_MINER.get())
                .sourceOrder(331)
                .save(consumer, id("assembly_machine/satellite_lunar_miner"));

    }

    private static void fluidContainerRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.CELL_EMPTY.get(), 6)
                .pattern(" S ")
                .pattern("G G")
                .pattern(" S ")
                .define('S', ModItems.STEEL_PLATE.get())
                .define('G', forgeTag("glass_panes"))
                .unlockedBy("has_steel_plate", has(ModItems.STEEL_PLATE.get()))
                .save(consumer, id("control/cell_empty"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, item("cell_deuterium"), 8)
                .pattern("DDD")
                .pattern("DTD")
                .pattern("DDD")
                .define('D', ModItems.CELL_EMPTY.get())
                .define('T', item("mike_deut"))
                .unlockedBy("has_mike_deut", has(item("mike_deut")))
                .save(consumer, id("control/cell_deuterium"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.CANISTER_EMPTY.get(), 2)
                .pattern("S ")
                .pattern("AA")
                .pattern("AA")
                .define('S', ModItems.STEEL_PLATE.get())
                .define('A', ModItems.ALUMINIUM_PLATE.get())
                .unlockedBy("has_steel_plate", has(ModItems.STEEL_PLATE.get()))
                .save(consumer, id("control/canister_empty"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.GAS_EMPTY.get(), 2)
                .pattern("S ")
                .pattern("AA")
                .pattern("AA")
                .define('S', ModItems.STEEL_PLATE.get())
                .define('A', ModItems.COPPER_PLATE.get())
                .unlockedBy("has_copper_plate", has(ModItems.COPPER_PLATE.get()))
                .save(consumer, id("control/gas_empty"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.FLUID_TANK_EMPTY.get(), 8)
                .pattern("AIA")
                .pattern("AGA")
                .pattern("AIA")
                .define('A', ModItems.ALUMINIUM_PLATE.get())
                .define('I', ModItems.IRON_PLATE.get())
                .define('G', forgeTag("glass"))
                .unlockedBy("has_aluminium_plate", has(ModItems.ALUMINIUM_PLATE.get()))
                .save(consumer, id("control/fluid_tank_empty"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.FLUID_TANK_LEAD_EMPTY.get(), 4)
                .pattern("LUL")
                .pattern("LTL")
                .pattern("LUL")
                .define('L', ModItems.LEAD_PLATE.get())
                .define('U', ModItems.legacyItem("billet_u238").get())
                .define('T', ModItems.FLUID_TANK_EMPTY.get())
                .unlockedBy("has_lead_plate", has(ModItems.LEAD_PLATE.get()))
                .save(consumer, id("control/fluid_tank_lead_empty"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.FLUID_BARREL_EMPTY.get(), 2)
                .pattern("SAS")
                .pattern("SGS")
                .pattern("SAS")
                .define('S', ModItems.STEEL_PLATE.get())
                .define('A', ModItems.ALUMINIUM_PLATE.get())
                .define('G', forgeTag("glass"))
                .unlockedBy("has_steel_plate", has(ModItems.STEEL_PLATE.get()))
                .save(consumer, id("control/fluid_barrel_empty"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.FLUID_PACK_EMPTY.get())
                .pattern("TI ")
                .pattern("ITI")
                .pattern(" TI")
                .define('T', ModItems.TITANIUM_PLATE.get())
                .define('I', forgeTag("ingots/any_plastic"))
                .unlockedBy("has_titanium_plate", has(ModItems.TITANIUM_PLATE.get()))
                .save(consumer, id("control/fluid_pack_empty"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.DISPERSER_CANISTER_EMPTY.get(), 4)
                .pattern(" P ")
                .pattern("PGP")
                .pattern(" P ")
                .define('P', forgeTag("ingots/any_hardplastic"))
                .define('G', block("glass_boron"))
                .unlockedBy("has_hardplastic", has(forgeTag("ingots/any_hardplastic")))
                .save(consumer, id("control/disperser_canister_empty"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.INF_WATER.get())
                .pattern("AAA")
                .pattern("WDW")
                .pattern("AAA")
                .define('A', ModItems.ALUMINIUM_PLATE.get())
                .define('W', HbmFluidContainerIngredient.of(HbmFluids.WATER, 1_000))
                .define('D', Items.DIAMOND)
                .unlockedBy("has_aluminium_plate", has(ModItems.ALUMINIUM_PLATE.get()))
                .save(consumer, id("control/inf_water"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.INF_WATER_MK2.get())
                .pattern("BPB")
                .pattern("PTP")
                .pattern("BPB")
                .define('B', ModItems.INF_WATER.get())
                .define('P', forgeTag("pipes/steel"))
                .define('T', forgeTag("shells/steel"))
                .unlockedBy("has_infinite_water_tank", has(ModItems.INF_WATER.get()))
                .save(consumer, id("control/inf_water_mk2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.SIPHON.get())
                .pattern(" GR")
                .pattern(" GR")
                .pattern(" G ")
                .define('G', forgeTag("glass/colorless"))
                .define('R', forgeTag("ingots/any_rubber"))
                .unlockedBy("has_any_rubber", has(forgeTag("ingots/any_rubber")))
                .save(consumer, id("tools/siphon"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.PIPETTE.get())
                .pattern("  L")
                .pattern(" G ")
                .pattern("G  ")
                .define('L', forgeTag("ingots/any_rubber"))
                .define('G', forgeTag("glass/colorless"))
                .unlockedBy("has_any_rubber", has(forgeTag("ingots/any_rubber")))
                .save(consumer, id("tools/pipette"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.PIPETTE_BORON.get())
                .pattern("  P")
                .pattern(" B ")
                .pattern("B  ")
                .define('P', forgeTag("ingots/any_rubber"))
                .define('B', block("glass_boron"))
                .unlockedBy("has_glass_boron", has(block("glass_boron")))
                .save(consumer, id("tools/pipette_boron"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.PIPETTE_LABORATORY.get())
                .pattern("  C")
                .pattern(" R ")
                .pattern("P  ")
                .define('C', forgeTag("circuits/chip"))
                .define('R', forgeTag("ingots/any_rubber"))
                .define('P', ModItems.PIPETTE_BORON.get())
                .unlockedBy("has_pipette_boron", has(ModItems.PIPETTE_BORON.get()))
                .save(consumer, id("tools/pipette_laboratory"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.SLIME_BALL, 16)
                .requires(Items.BONE_MEAL, 4)
                .requires(HbmFluidContainerIngredient.of(HbmFluids.SULFURIC_ACID, 1_000))
                .unlockedBy("has_sulfuric_acid_container", has(ModItems.CANISTER_FULL.get()))
                .save(consumer, id("control/slime_ball_from_sulfuric_acid"));

        GenericMachineRecipeBuilder.assembly("ass.emptypackage", 40, 100)
                .inputItem(ModItems.TITANIUM_PLATE.get(), 4)
                .inputItem(ModItems.PLASTIC_BAG.get(), 1)
                .inputItem(ModItems.PLASTIC_BAG.get(), 1)
                .outputItem(ModItems.FLUID_PACK_EMPTY.get())
                .sourceOrder(333)
                .save(consumer, id("assembly_machine/emptypackage"));

        HbmFluids.all().stream()
                .filter(type -> type != HbmFluids.NONE && !type.hasNoContainer())
                .forEach(type -> {
                    GenericMachineRecipeBuilder.assembly("ass.package" + type.getName(), 40, 100)
                            .inputItem(fluidContainerStack(ModItems.FLUID_PACK_EMPTY.get(), 1, null, 0, 0))
                            .inputFluid(type, 32_000)
                            .outputItem(fluidContainerStack(ModItems.FLUID_PACK_FULL.get(), 1, type, 32_000, 0))
                            .icon(fluidContainerStack(ModItems.FLUID_PACK_FULL.get(), 1, type, 32_000, 0))
                            .save(consumer, id("assembly_machine/package_" + type.toPath()));

                    GenericMachineRecipeBuilder.assembly("ass.unpackage" + type.getName(), 40, 100)
                            .inputItem(fluidContainerStack(ModItems.FLUID_PACK_FULL.get(), 1, type, 32_000, 0))
                            .outputFluid(type, 32_000)
                            .outputItem(ModItems.FLUID_PACK_EMPTY.get())
                            .icon(fluidIconStack(type, 32_000, 0))
                            .save(consumer, id("assembly_machine/unpackage_" + type.toPath()));
                });
    }

    private static void fluidNetworkRecipes(Consumer<FinishedRecipe> consumer) {
        SpecialRecipeBuilder.special(ModRecipes.FLUID_DUCT_IDENTIFIER.get())
                .save(consumer, id("fluid_network/fluid_duct_identifier").toString());

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.FLUID_DUCT_NEO.get(), 8)
                .pattern("SAS")
                .pattern("   ")
                .pattern("SAS")
                .define('S', ModItems.STEEL_PLATE.get())
                .define('A', ModItems.ALUMINIUM_PLATE.get())
                .unlockedBy("has_steel_plate", has(ModItems.STEEL_PLATE.get()))
                .save(consumer, id("fluid_network/fluid_duct_neo"));

        shapedLegacyVariantRecipe(consumer, id("fluid_network/fluid_duct_neo_silver"),
                ModBlocks.FLUID_DUCT_NEO.get(), 8, 1,
                new String[] {"IAI", "   ", "IAI"},
                new Object[][] {
                        {'I', ModItems.IRON_PLATE.get()},
                        {'A', ModItems.ALUMINIUM_PLATE.get()}
                },
                ModItems.IRON_PLATE.get(), "has_iron_plate");

        shapedLegacyVariantRecipe(consumer, id("fluid_network/fluid_duct_neo_colored"),
                ModBlocks.FLUID_DUCT_NEO.get(), 8, 2,
                new String[] {"ASA", "   ", "ASA"},
                new Object[][] {
                        {'S', ModItems.STEEL_PLATE.get()},
                        {'A', ModItems.ALUMINIUM_PLATE.get()}
                },
                ModItems.STEEL_PLATE.get(), "has_steel_plate");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModBlocks.FLUID_DUCT_NEO.get())
                .requires(ModItems.FLUID_DUCT.get())
                .unlockedBy("has_typed_fluid_duct", has(ModItems.FLUID_DUCT.get()))
                .save(consumer, id("fluid_network/fluid_duct_neo_plain_from_typed"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.FLUID_DUCT_PAINTABLE.get(), 8)
                .pattern("SAS")
                .pattern("A A")
                .pattern("SAS")
                .define('S', ModItems.STEEL_INGOT.get())
                .define('A', ModItems.ALUMINIUM_PLATE.get())
                .unlockedBy("has_steel_ingot", has(ModItems.STEEL_INGOT.get()))
                .save(consumer, id("fluid_network/fluid_duct_paintable"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.FLUID_DUCT_PAINTABLE_BLOCK_EXHAUST.get(), 8)
                .pattern("SAS")
                .pattern("A A")
                .pattern("SAS")
                .define('S', Items.IRON_INGOT)
                .define('A', item("plate_polymer"))
                .unlockedBy("has_plate_polymer", has(item("plate_polymer")))
                .save(consumer, id("fluid_network/fluid_duct_paintable_block_exhaust"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModBlocks.FLUID_DUCT_GAUGE.get())
                .requires(ModBlocks.FLUID_DUCT_PAINTABLE.get())
                .requires(ModItems.STEEL_INGOT.get())
                .requires(forgeTag("circuits/basic"))
                .unlockedBy("has_paintable_fluid_duct", has(ModBlocks.FLUID_DUCT_PAINTABLE.get()))
                .save(consumer, id("fluid_network/fluid_duct_gauge"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.FLUID_VALVE.get())
                .pattern("S")
                .pattern("W")
                .define('S', Blocks.LEVER)
                .define('W', ModBlocks.FLUID_DUCT_PAINTABLE.get())
                .unlockedBy("has_paintable_fluid_duct", has(ModBlocks.FLUID_DUCT_PAINTABLE.get()))
                .save(consumer, id("fluid_network/fluid_valve"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.FLUID_SWITCH.get())
                .pattern("S")
                .pattern("W")
                .define('S', Items.REDSTONE)
                .define('W', ModBlocks.FLUID_DUCT_PAINTABLE.get())
                .unlockedBy("has_paintable_fluid_duct", has(ModBlocks.FLUID_DUCT_PAINTABLE.get()))
                .save(consumer, id("fluid_network/fluid_switch"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.FLUID_COUNTER_VALVE.get())
                .pattern("S")
                .pattern("W")
                .define('S', forgeTag("circuits/chip"))
                .define('W', ModBlocks.FLUID_SWITCH.get())
                .unlockedBy("has_fluid_switch", has(ModBlocks.FLUID_SWITCH.get()))
                .save(consumer, id("fluid_network/fluid_counter_valve"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.FLUID_PUMP.get())
                .pattern(" S ")
                .pattern("PGP")
                .pattern("IMI")
                .define('S', ModItems.STEEL_PLATE.get())
                .define('P', item("pipes_steel"))
                .define('G', item("ingot_graphite"))
                .define('I', ModItems.STEEL_INGOT.get())
                .define('M', ModItems.MOTOR.get())
                .unlockedBy("has_motor", has(ModItems.MOTOR.get()))
                .save(consumer, id("fluid_network/fluid_pump"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.PIPE_ANCHOR.get(), 2)
                .pattern("P")
                .pattern("P")
                .pattern("S")
                .define('P', item("pipes_steel"))
                .define('S', ModItems.STEEL_INGOT.get())
                .unlockedBy("has_steel_pipe", has(item("pipes_steel")))
                .save(consumer, id("fluid_network/pipe_anchor"));
    }

    private static void shapedLegacyVariantRecipe(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId,
            Block result, int count, int legacyVariant, String[] pattern, Object[][] keys, ItemLike unlockItem,
            String unlockName) {
        shapedLegacyVariantRecipe(consumer, recipeId, result.asItem(), count, legacyVariant, pattern, keys,
                unlockItem, unlockName);
    }

    private static void containerUpgradeRecipe(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId,
            ItemLike result, @Nullable String resultNbt, String[] pattern, Object[][] keys) {
        consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                json.addProperty("category", "building");
                JsonArray patternArray = new JsonArray();
                for (String line : pattern) {
                    patternArray.add(line);
                }
                json.add("pattern", patternArray);

                JsonObject keyObject = new JsonObject();
                for (Object[] entry : keys) {
                    keyObject.add(String.valueOf((Character) entry[0]), shapedIngredient(entry[1]));
                }
                json.add("key", keyObject);

                JsonObject resultObject = new JsonObject();
                resultObject.addProperty("item", HbmRegistryUtil.itemKey(result.asItem()).toString());
                if (resultNbt != null && !resultNbt.isBlank()) {
                    resultObject.addProperty("nbt", resultNbt);
                }
                json.add("result", resultObject);
                json.addProperty("show_notification", true);
            }

            @Override
            public ResourceLocation getId() {
                return recipeId;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return ModRecipes.CONTAINER_UPGRADE_CRAFTING.get();
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }

    private static JsonObject shapedIngredient(Object value) {
        if (value instanceof JsonObject object) {
            return object.deepCopy();
        }
        if (value instanceof TagKey<?> tagKey && tagKey.isFor(Registries.ITEM)) {
            JsonObject ingredient = new JsonObject();
            ingredient.addProperty("tag", tagKey.location().toString());
            return ingredient;
        }
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", HbmRegistryUtil.itemKey(((ItemLike) value).asItem()).toString());
        return ingredient;
    }

    private static void shapedLegacyVariantRecipe(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId,
            ItemLike result, int count, int legacyVariant, String[] pattern, Object[][] keys, ItemLike unlockItem,
            String unlockName) {
        consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                json.addProperty("category", "redstone");
                JsonArray patternArray = new JsonArray();
                for (String line : pattern) {
                    patternArray.add(line);
                }
                json.add("pattern", patternArray);

                JsonObject keyObject = new JsonObject();
                for (Object[] entry : keys) {
                    JsonObject ingredient = new JsonObject();
                    if (entry[1] instanceof TagKey<?> tagKey && tagKey.isFor(Registries.ITEM)) {
                        ingredient.addProperty("tag", tagKey.location().toString());
                    } else {
                        ingredient.addProperty("item", HbmRegistryUtil.itemKey(((ItemLike) entry[1]).asItem()).toString());
                    }
                    keyObject.add(String.valueOf((Character) entry[0]), ingredient);
                }
                json.add("key", keyObject);

                JsonObject resultObject = new JsonObject();
                resultObject.addProperty("item", HbmRegistryUtil.itemKey(result.asItem()).toString());
                resultObject.addProperty("count", count);
                resultObject.addProperty("nbt", "{hbmLegacyVariant:" + legacyVariant + "}");
                json.add("result", resultObject);

                json.addProperty("show_notification", true);
            }

            @Override
            public ResourceLocation getId() {
                return recipeId;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return ModRecipes.LEGACY_NBT_SHAPED.get();
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                JsonObject advancement = new JsonObject();
                JsonObject criteria = new JsonObject();
                JsonObject criterion = new JsonObject();
                criterion.addProperty("trigger", "minecraft:inventory_changed");
                JsonObject conditions = new JsonObject();
                JsonArray items = new JsonArray();
                JsonObject item = new JsonObject();
                item.addProperty("item", HbmRegistryUtil.itemKey(unlockItem.asItem()).toString());
                items.add(item);
                conditions.add("items", items);
                criterion.add("conditions", conditions);
                criteria.add(unlockName, criterion);
                advancement.add("criteria", criteria);
                JsonArray requirements = new JsonArray();
                JsonArray requirement = new JsonArray();
                requirement.add(unlockName);
                requirements.add(requirement);
                advancement.add("requirements", requirements);
                return advancement;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return new ResourceLocation(recipeId.getNamespace(), "recipes/redstone/" + recipeId.getPath());
            }
        });
    }

    private static void precassRecipes(Consumer<FinishedRecipe> consumer) {
        int min = 1_200;
        GenericMachineRecipeBuilder.precass("precass.blueprints", 5 * min, 20_000L)
                .inputItem(Items.PAPER, 16)
                .inputLegacyOre("dyeBlue", 16)
                .inputItem(Items.PUFFERFISH, 4)
                .outputOneOf(
                        GenericMachineRecipeBuilder.WeightedOutput.of(ModItems.BLUEPRINT_FOLDER.get(), 10),
                        GenericMachineRecipeBuilder.WeightedOutput.of(new ItemStack(Items.PAPER, 16), 90))
                .sourceOrder(0)
                .save(consumer, id("precass/blueprints"));
        GenericMachineRecipeBuilder.precass("precass.beigeprints", 5 * min, 50_000L)
                .inputItem(Items.PAPER, 24)
                .inputLegacyOre("gemCinnabar", 24)
                .inputItem(Items.PUFFERFISH, 8)
                .outputOneOf(
                        GenericMachineRecipeBuilder.WeightedOutput.of(ModItems.BLUEPRINT_FOLDER_DISCOVER.get(), 5),
                        GenericMachineRecipeBuilder.WeightedOutput.of(new ItemStack(Items.PAPER, 24), 95))
                .sourceOrder(1)
                .save(consumer, id("precass/beigeprints"));
    }

    private static void liquefactionRecipes(Consumer<FinishedRecipe> consumer) {
        LiquefactionRecipeBuilder.liquefactionLegacyOre("gemCoal", HbmItemTagsProvider.forgeItemTag("gems/coal"), HbmFluids.COALOIL, 100)
                .sourceOrder(0)
                .save(consumer, id("liquefaction/coal_gem"));
        LiquefactionRecipeBuilder.liquefactionLegacyOre("dustCoal", HbmItemTagsProvider.forgeItemTag("dusts/coal"), HbmFluids.COALOIL, 100)
                .sourceOrder(1)
                .save(consumer, id("liquefaction/coal_dust"));
        LiquefactionRecipeBuilder.liquefactionLegacyOre("gemLignite", HbmItemTagsProvider.forgeItemTag("gems/lignite"), HbmFluids.COALOIL, 50)
                .sourceOrder(2)
                .save(consumer, id("liquefaction/lignite_gem"));
        LiquefactionRecipeBuilder.liquefactionLegacyOre("dustLignite", HbmItemTagsProvider.forgeItemTag("dusts/lignite"), HbmFluids.COALOIL, 50)
                .sourceOrder(3)
                .save(consumer, id("liquefaction/lignite_dust"));
        LiquefactionRecipeBuilder.liquefactionLegacyOre("oiltar", HbmItemTagsProvider.forgeItemTag("tar/oil"), HbmFluids.BITUMEN, 75)
                .sourceOrder(4)
                .save(consumer, id("liquefaction/oil_tar_crude"));
        LiquefactionRecipeBuilder.liquefactionLegacyOre("cracktar", HbmItemTagsProvider.forgeItemTag("tar/crack"), HbmFluids.BITUMEN, 100)
                .sourceOrder(5)
                .save(consumer, id("liquefaction/oil_tar_crack"));
        LiquefactionRecipeBuilder.liquefactionLegacyOre("coaltar", HbmItemTagsProvider.forgeItemTag("tar/coal"), HbmFluids.BITUMEN, 50)
                .sourceOrder(6)
                .save(consumer, id("liquefaction/oil_tar_coal"));
        LiquefactionRecipeBuilder.liquefactionLegacyOre("logWood", ItemTags.LOGS, HbmFluids.MUG, 100)
                .sourceOrder(7)
                .save(consumer, id("liquefaction/logs"));
        LiquefactionRecipeBuilder.liquefactionLegacyOre("dustSodium", HbmItemTagsProvider.forgeItemTag("dusts/sodium"), HbmFluids.SODIUM, 100)
                .sourceOrder(8)
                .save(consumer, id("liquefaction/sodium_dust"));
        LiquefactionRecipeBuilder.liquefactionLegacyOre("ingotLead", HbmItemTagsProvider.forgeItemTag("ingots/lead"), HbmFluids.LEAD, 100)
                .sourceOrder(9)
                .save(consumer, id("liquefaction/lead_ingot"));
        LiquefactionRecipeBuilder.liquefactionLegacyOre("dustLead", HbmItemTagsProvider.forgeItemTag("dusts/lead"), HbmFluids.LEAD, 100)
                .sourceOrder(10)
                .save(consumer, id("liquefaction/lead_dust"));
        LiquefactionRecipeBuilder.liquefactionLegacyOre("blockLead", HbmItemTagsProvider.forgeItemTag("storage_blocks/lead"), HbmFluids.LEAD, 900)
                .sourceOrder(11)
                .save(consumer, id("liquefaction/lead_block"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.NETHERRACK, HbmFluids.LAVA, 250)
                .sourceOrder(12)
                .save(consumer, id("liquefaction/netherrack"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.COBBLESTONE, HbmFluids.LAVA, 250)
                .sourceOrder(13)
                .save(consumer, id("liquefaction/cobblestone"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.STONE, HbmFluids.LAVA, 250)
                .sourceOrder(14)
                .save(consumer, id("liquefaction/stone"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.OBSIDIAN, HbmFluids.LAVA, 500)
                .sourceOrder(15)
                .save(consumer, id("liquefaction/obsidian"));
        LiquefactionRecipeBuilder.liquefaction(Items.SNOWBALL, HbmFluids.WATER, 125)
                .sourceOrder(16)
                .save(consumer, id("liquefaction/snowball"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.SNOW_BLOCK, HbmFluids.WATER, 500)
                .sourceOrder(17)
                .save(consumer, id("liquefaction/snow_block"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.ICE, HbmFluids.WATER, 1_000)
                .sourceOrder(18)
                .save(consumer, id("liquefaction/ice"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.PACKED_ICE, HbmFluids.WATER, 1_000)
                .sourceOrder(19)
                .save(consumer, id("liquefaction/packed_ice"));
        LiquefactionRecipeBuilder.liquefaction(Items.ENDER_PEARL, HbmFluids.ENDERJUICE, 100)
                .sourceOrder(20)
                .save(consumer, id("liquefaction/ender_pearl"));
        LiquefactionRecipeBuilder.liquefaction(block("ore_oil_sand"), HbmFluids.BITUMEN, 100)
                .sourceOrder(21)
                .save(consumer, id("liquefaction/ore_oil_sand"));
        LiquefactionRecipeBuilder.liquefaction(Items.SUGAR, HbmFluids.ETHANOL, 100)
                .sourceOrder(22)
                .save(consumer, id("liquefaction/sugar"));
        LiquefactionRecipeBuilder.liquefaction(ModBlocks.PLANT_FLOWER_WEED.get(), HbmFluids.ETHANOL, 150)
                .sourceOrder(23)
                .save(consumer, id("liquefaction/plant_flower_weed"));
        LiquefactionRecipeBuilder.liquefaction(ModBlocks.PLANT_FLOWER_CD0.get(), HbmFluids.ETHANOL, 50)
                .sourceOrder(24)
                .save(consumer, id("liquefaction/plant_flower_cd0"));
        LiquefactionRecipeBuilder.liquefaction(ModItems.BIOMASS.get(), HbmFluids.BIOGAS, 125)
                .sourceOrder(25)
                .save(consumer, id("liquefaction/biomass"));
        LiquefactionRecipeBuilder.liquefaction(ModItems.GLYPHID_GLAND_EMPTY.get(), HbmFluids.BIOGAS, 2_000)
                .sourceOrder(26)
                .save(consumer, id("liquefaction/glyphid_gland_empty"));
        LiquefactionRecipeBuilder.liquefactionLegacyWildcard(new ResourceLocation("minecraft", "fish"), HbmFluids.FISHOIL, 100)
                .sourceOrder(27)
                .save(consumer, id("liquefaction/fish"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.SUNFLOWER, HbmFluids.SUNFLOWEROIL, 100)
                .sourceOrder(28)
                .save(consumer, id("liquefaction/sunflower"));
        LiquefactionRecipeBuilder.liquefaction(Items.WHEAT_SEEDS, HbmFluids.SEEDSLURRY, 50)
                .sourceOrder(29)
                .save(consumer, id("liquefaction/wheat_seeds"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.GRASS, HbmFluids.SEEDSLURRY, 100)
                .sourceOrder(30)
                .save(consumer, id("liquefaction/grass"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.FERN, HbmFluids.SEEDSLURRY, 100)
                .sourceOrder(31)
                .save(consumer, id("liquefaction/fern"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.VINE, HbmFluids.SEEDSLURRY, 100)
                .sourceOrder(32)
                .save(consumer, id("liquefaction/vine"));
    }

    private static void pyroOvenRecipes(Consumer<FinishedRecipe> consumer) {
        int sourceOrder = 0;
        pyroSolidFuel(consumer, HbmFluids.SMEAR, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.HEATINGOIL, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.HEATINGOIL_VACUUM, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.RECLAIMED, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.PETROIL, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.NAPHTHA, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.NAPHTHA_CRACK, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.DIESEL, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.DIESEL_REFORM, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.DIESEL_CRACK, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.DIESEL_CRACK_REFORM, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.LIGHTOIL, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.LIGHTOIL_CRACK, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.LIGHTOIL_VACUUM, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.KEROSENE, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.KEROSENE_REFORM, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.SOURGAS, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.REFORMGAS, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.SYNGAS, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.PETROLEUM, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.LPG, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.BIOFUEL, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.AROMATICS, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.UNSATURATEDS, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.REFORMATE, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.XYLENE, sourceOrder++);
        pyroSolidFuel(consumer, HbmFluids.BALEFIRE, 24_000_000L, item("solid_fuel_bf"), sourceOrder++);

        bedrockOrePyroOvenRecipes(consumer, 40);

        PyroOvenRecipeBuilder.pyro(100)
                .inputFluid(HbmFluids.STEAM, 250)
                .inputLegacyOre("gemAnyCoke", forgeTag("gems/any_coke"), 1)
                .outputFluid(HbmFluids.SYNGAS, 1_000)
                .sourceOrder(30)
                .save(consumer, id("pyro_oven/syngas_from_coke"));
        PyroOvenRecipeBuilder.pyro(100)
                .inputItem(ModItems.BIOMASS.get(), 4)
                .outputFluid(HbmFluids.SYNGAS, 1_000)
                .outputItem(new ItemStack(Items.CHARCOAL))
                .sourceOrder(31)
                .save(consumer, id("pyro_oven/syngas_from_biomass"));
        PyroOvenRecipeBuilder.pyro(40)
                .inputLegacyOre("anyTar", forgeTag("any/tar"), 4)
                .outputFluid(HbmFluids.CARBONDIOXIDE, 1_000)
                .outputItem(item("powder_ash_soot"))
                .sourceOrder(32)
                .save(consumer, id("pyro_oven/soot_from_tar"));
        PyroOvenRecipeBuilder.pyro(100)
                .inputFluid(HbmFluids.STEAM, 500)
                .inputLegacyOre("gemCoal", forgeTag("gems/coal"), 1)
                .outputFluid(HbmFluids.SYNGAS, 1_000)
                .sourceOrder(28)
                .save(consumer, id("pyro_oven/syngas_from_coal"));
        PyroOvenRecipeBuilder.pyro(100)
                .inputFluid(HbmFluids.STEAM, 500)
                .inputLegacyOre("dustCoal", forgeTag("dusts/coal"), 1)
                .outputFluid(HbmFluids.SYNGAS, 1_000)
                .sourceOrder(29)
                .save(consumer, id("pyro_oven/syngas_from_coal_dust"));
        PyroOvenRecipeBuilder.pyro(100)
                .inputFluid(HbmFluids.HYDROGEN, 500)
                .inputLegacyOre("gemCoal", forgeTag("gems/coal"), 1)
                .outputFluid(HbmFluids.HEAVYOIL, 1_000)
                .sourceOrder(33)
                .save(consumer, id("pyro_oven/heavyoil_from_coal"));
        PyroOvenRecipeBuilder.pyro(100)
                .inputFluid(HbmFluids.HYDROGEN, 500)
                .inputLegacyOre("dustCoal", forgeTag("dusts/coal"), 1)
                .outputFluid(HbmFluids.HEAVYOIL, 1_000)
                .sourceOrder(34)
                .save(consumer, id("pyro_oven/heavyoil_from_coal_dust"));
        PyroOvenRecipeBuilder.pyro(50)
                .inputFluid(HbmFluids.HEAVYOIL, 500)
                .inputLegacyOre("gemCoal", forgeTag("gems/coal"), 1)
                .outputFluid(HbmFluids.COALGAS, 1_000)
                .sourceOrder(35)
                .save(consumer, id("pyro_oven/coalgas_from_coal"));
        PyroOvenRecipeBuilder.pyro(50)
                .inputFluid(HbmFluids.HEAVYOIL, 500)
                .inputLegacyOre("dustCoal", forgeTag("dusts/coal"), 1)
                .outputFluid(HbmFluids.COALGAS, 1_000)
                .sourceOrder(36)
                .save(consumer, id("pyro_oven/coalgas_from_coal_dust"));
        PyroOvenRecipeBuilder.pyro(50)
                .inputFluid(HbmFluids.HEAVYOIL, 500)
                .inputLegacyOre("gemAnyCoke", forgeTag("gems/any_coke"), 1)
                .outputFluid(HbmFluids.COALGAS, 1_000)
                .sourceOrder(37)
                .save(consumer, id("pyro_oven/coalgas_from_coke"));
        PyroOvenRecipeBuilder.pyro(60)
                .inputFluid(HbmFluids.GAS_COKER, 4_000)
                .outputFluid(HbmFluids.REFORMGAS, 100)
                .sourceOrder(38)
                .save(consumer, id("pyro_oven/reformgas_from_coker_gas"));
        PyroOvenRecipeBuilder.pyro(60)
                .inputFluid(HbmFluids.GAS, 12_000)
                .outputFluid(HbmFluids.HYDROGEN, 8_000)
                .outputItem(item("ingot_graphite"))
                .sourceOrder(39)
                .save(consumer, id("pyro_oven/hydrogen_from_natural_gas"));
        PyroOvenRecipeBuilder.pyro(300)
                .inputFluid(HbmFluids.SYNGAS, 2_000)
                .inputLegacyOre("dustTungsten", forgeTag("dusts/tungsten"), 1)
                .outputFluid(HbmFluids.SPENTSTEAM, 1_000)
                .outputItem(item("ingot_tungsten_carbide"))
                .sourceOrder(27)
                .save(consumer, id("pyro_oven/tungsten_carbide_from_syngas"));
    }

    private static void pyroSolidFuel(Consumer<FinishedRecipe> consumer, FluidType fluid) {
        pyroSolidFuel(consumer, fluid, 1_440_000L, item("solid_fuel"), Integer.MAX_VALUE);
    }

    private static void pyroSolidFuel(Consumer<FinishedRecipe> consumer, FluidType fluid, int sourceOrder) {
        pyroSolidFuel(consumer, fluid, 1_440_000L, item("solid_fuel"), sourceOrder);
    }

    private static void pyroSolidFuel(Consumer<FinishedRecipe> consumer, FluidType fluid, long tuPerFuel,
            ItemLike fuel) {
        pyroSolidFuel(consumer, fluid, tuPerFuel, fuel, Integer.MAX_VALUE);
    }

    private static void pyroSolidFuel(Consumer<FinishedRecipe> consumer, FluidType fluid, long tuPerFuel,
            ItemLike fuel, int sourceOrder) {
        int amount = pyroAutoAmount(fluid, tuPerFuel);
        if (amount <= 0) {
            return;
        }
        PyroOvenRecipeBuilder.pyro(60)
                .inputFluid(fluid, amount)
                .outputItem(fuel)
                .sourceOrder(sourceOrder)
                .save(consumer, id("pyro_oven/solid_fuel_from_" + fluid.toPath()));
    }

    private static void bedrockOrePyroOvenRecipes(Consumer<FinishedRecipe> consumer, int sourceOrderStart) {
        int sourceOrder = sourceOrderStart;
        for (BedrockOreType type : BedrockOreType.values()) {
            String suffix = type.suffix();
            sourceOrder = bedrockOrePyroOven(consumer, sourceOrder, suffix + "_base", BedrockOreGrade.BASE, type,
                    BedrockOreGrade.BASE_ROASTED);
            sourceOrder = bedrockOrePyroOven(consumer, sourceOrder, suffix + "_primary", BedrockOreGrade.PRIMARY,
                    type, BedrockOreGrade.PRIMARY_ROASTED);
            sourceOrder = bedrockOrePyroOven(consumer, sourceOrder, suffix + "_sulfuric_byproduct",
                    BedrockOreGrade.SULFURIC_BYPRODUCT, type, BedrockOreGrade.SULFURIC_ROASTED);
            sourceOrder = bedrockOrePyroOven(consumer, sourceOrder, suffix + "_solvent_byproduct",
                    BedrockOreGrade.SOLVENT_BYPRODUCT, type, BedrockOreGrade.SOLVENT_ROASTED);
            sourceOrder = bedrockOrePyroOven(consumer, sourceOrder, suffix + "_rad_byproduct",
                    BedrockOreGrade.RAD_BYPRODUCT, type, BedrockOreGrade.RAD_ROASTED);
        }
    }

    private static int bedrockOrePyroOven(Consumer<FinishedRecipe> consumer, int sourceOrder, String name,
            BedrockOreGrade inputGrade, BedrockOreType type, BedrockOreGrade outputGrade) {
        PyroOvenRecipeBuilder.pyro(10)
                .inputItem(bedrockOreInput(inputGrade, type))
                .outputFluid(HbmFluids.VITRIOL, 50)
                .outputItem(bedrockOre(outputGrade, type))
                .sourceOrder(sourceOrder)
                .save(consumer, id("pyro_oven/bedrock_ore_" + name));
        return sourceOrder + 1;
    }

    private static int pyroAutoAmount(FluidType fluid, long tuPerFuel) {
        FlammableFluidTrait trait = fluid.getTrait(FlammableFluidTrait.class);
        if (trait == null || trait.getHeatEnergyPerBucket() <= 0L) {
            return 0;
        }
        int amount = (int) (tuPerFuel * 1_000L * 0.5D / trait.getHeatEnergyPerBucket());
        if (amount > 10_000) {
            amount -= amount % 1_000;
        } else if (amount > 1_000) {
            amount -= amount % 100;
        } else if (amount > 100) {
            amount -= amount % 10;
        }
        return Math.max(amount, 1);
    }

    private static void mixerRecipes(Consumer<FinishedRecipe> consumer) {
        MixerRecipeBuilder.mixer(HbmFluids.COOLANT, 2_000, 50)
                .input1(HbmFluids.WATER, 1_800)
                .solidLegacyOre("dustNiter", 1)
                .save(consumer, id("mixer/coolant"));
        MixerRecipeBuilder.mixer(HbmFluids.CRYOGEL, 2_000, 50)
                .input1(HbmFluids.COOLANT, 1_800)
                .solidItem(item("powder_ice"), 1)
                .save(consumer, id("mixer/cryogel"));
        MixerRecipeBuilder.mixer(HbmFluids.NITAN, 1_000, 50)
                .input1(HbmFluids.KEROSENE, 600)
                .input2(HbmFluids.MERCURY, 200)
                .solidItem(item("powder_nitan_mix"), 1)
                .save(consumer, id("mixer/nitan"));
        MixerRecipeBuilder.mixer(HbmFluids.FRACKSOL, 1_000, 20)
                .input1(HbmFluids.SULFURIC_ACID, 900)
                .input2(HbmFluids.PETROLEUM, 100)
                .sourceOrder(0)
                .save(consumer, id("mixer/fracksol_sulfuric"));
        MixerRecipeBuilder.mixer(HbmFluids.FRACKSOL, 1_000, 20)
                .input1(HbmFluids.WATER, 1_000)
                .input2(HbmFluids.PETROLEUM, 100)
                .solidLegacyOre("dustSulfur", 1)
                .sourceOrder(1)
                .save(consumer, id("mixer/fracksol_sulfur"));
        MixerRecipeBuilder.mixer(HbmFluids.ENDERJUICE, 100, 100)
                .input1(HbmFluids.XPJUICE, 500)
                .solidLegacyOre("dustDiamond", 1)
                .save(consumer, id("mixer/enderjuice"));
        MixerRecipeBuilder.mixer(HbmFluids.SALIENT, 1_000, 20)
                .input1(HbmFluids.SEEDSLURRY, 500)
                .input2(HbmFluids.BLOOD, 500)
                .save(consumer, id("mixer/salient"));
        MixerRecipeBuilder.mixer(HbmFluids.COLLOID, 500, 20)
                .input1(HbmFluids.WATER, 500)
                .solidItem(item("dust"), 1)
                .save(consumer, id("mixer/colloid"));
        MixerRecipeBuilder.mixer(HbmFluids.PHOSGENE, 1_000, 20)
                .input1(HbmFluids.UNSATURATEDS, 500)
                .input2(HbmFluids.CHLORINE, 500)
                .save(consumer, id("mixer/phosgene"));
        MixerRecipeBuilder.mixer(HbmFluids.MUSTARDGAS, 1_000, 20)
                .input1(HbmFluids.REFORMGAS, 750)
                .input2(HbmFluids.CHLORINE, 250)
                .solidLegacyOre("dustSulfur", 1)
                .save(consumer, id("mixer/mustardgas"));
        MixerRecipeBuilder.mixer(HbmFluids.IONGEL, 1_000, 50)
                .input1(HbmFluids.WATER, 1_000)
                .input2(HbmFluids.HYDROGEN, 200)
                .solidItem(item("pellet_charged"), 1)
                .save(consumer, id("mixer/iongel"));
        MixerRecipeBuilder.mixer(HbmFluids.EGG, 1_000, 50)
                .input1(HbmFluids.RADIOSOLVENT, 500)
                .solidItem(Items.EGG, 1)
                .save(consumer, id("mixer/egg"));
        MixerRecipeBuilder.mixer(HbmFluids.FISHOIL, 100, 50)
                .solidLegacyWildcard(new ResourceLocation("minecraft", "fish"), 1)
                .save(consumer, id("mixer/fishoil_raw_fish"));
        MixerRecipeBuilder.mixer(HbmFluids.SUNFLOWEROIL, 100, 50)
                .solidItem(Blocks.SUNFLOWER, 1)
                .save(consumer, id("mixer/sunfloweroil"));
        MixerRecipeBuilder.mixer(HbmFluids.FULLERENE, 250, 50)
                .input1(HbmFluids.RADIOSOLVENT, 500)
                .solidItem(item("powder_ash_soot"), 1)
                .save(consumer, id("mixer/fullerene"));
        MixerRecipeBuilder.mixer(HbmFluids.SOLVENT, 1_000, 50)
                .input1(HbmFluids.NAPHTHA, 500)
                .input2(HbmFluids.AROMATICS, 500)
                .sourceOrder(0)
                .save(consumer, id("mixer/solvent_naphtha"));
        MixerRecipeBuilder.mixer(HbmFluids.SOLVENT, 1_000, 50)
                .input1(HbmFluids.NAPHTHA_CRACK, 500)
                .input2(HbmFluids.AROMATICS, 500)
                .sourceOrder(1)
                .save(consumer, id("mixer/solvent_naphtha_crack"));
        MixerRecipeBuilder.mixer(HbmFluids.SOLVENT, 1_000, 50)
                .input1(HbmFluids.NAPHTHA_DS, 500)
                .input2(HbmFluids.AROMATICS, 500)
                .sourceOrder(2)
                .save(consumer, id("mixer/solvent_naphtha_ds"));
        MixerRecipeBuilder.mixer(HbmFluids.SOLVENT, 1_000, 50)
                .input1(HbmFluids.NAPHTHA_COKER, 500)
                .input2(HbmFluids.AROMATICS, 500)
                .sourceOrder(3)
                .save(consumer, id("mixer/solvent_naphtha_coker"));
        MixerRecipeBuilder.mixer(HbmFluids.SULFURIC_ACID, 500, 50)
                .input1(HbmFluids.PEROXIDE, 800)
                .solidLegacyOre("dustSulfur", 1)
                .save(consumer, id("mixer/sulfuric_acid"));
        MixerRecipeBuilder.mixer(HbmFluids.NITRIC_ACID, 1_000, 50)
                .input1(HbmFluids.SULFURIC_ACID, 500)
                .solidLegacyOre("dustNiter", 1)
                .save(consumer, id("mixer/nitric_acid"));
        MixerRecipeBuilder.mixer(HbmFluids.RADIOSOLVENT, 1_000, 50)
                .input1(HbmFluids.REFORMGAS, 750)
                .input2(HbmFluids.CHLORINE, 250)
                .save(consumer, id("mixer/radiosolvent"));
        MixerRecipeBuilder.mixer(HbmFluids.SCHRABIDIC, 16_000, 100)
                .input1(HbmFluids.SAS3, 8_000)
                .input2(HbmFluids.PEROXIDE, 6_000)
                .solidItem(item("pellet_charged"), 1)
                .save(consumer, id("mixer/schrabidic"));
        MixerRecipeBuilder.mixer(HbmFluids.PETROIL, 1_000, 30)
                .input1(HbmFluids.RECLAIMED, 800)
                .input2(HbmFluids.LUBRICANT, 200)
                .save(consumer, id("mixer/petroil"));
        MixerRecipeBuilder.mixer(HbmFluids.LUBRICANT, 1_000, 20)
                .input1(HbmFluids.HEATINGOIL, 500)
                .input2(HbmFluids.UNSATURATEDS, 500)
                .sourceOrder(0)
                .save(consumer, id("mixer/lubricant_heatingoil"));
        MixerRecipeBuilder.mixer(HbmFluids.LUBRICANT, 1_000, 20)
                .input1(HbmFluids.FISHOIL, 800)
                .input2(HbmFluids.ETHANOL, 200)
                .sourceOrder(1)
                .save(consumer, id("mixer/lubricant_fishoil"));
        MixerRecipeBuilder.mixer(HbmFluids.LUBRICANT, 1_000, 20)
                .input1(HbmFluids.SUNFLOWEROIL, 800)
                .input2(HbmFluids.ETHANOL, 200)
                .sourceOrder(2)
                .save(consumer, id("mixer/lubricant_sunflower"));
        MixerRecipeBuilder.mixer(HbmFluids.BIOFUEL, 250, 20)
                .input1(HbmFluids.FISHOIL, 500)
                .input2(HbmFluids.WOODOIL, 500)
                .sourceOrder(0)
                .save(consumer, id("mixer/biofuel_fishoil"));
        MixerRecipeBuilder.mixer(HbmFluids.BIOFUEL, 200, 20)
                .input1(HbmFluids.SUNFLOWEROIL, 500)
                .input2(HbmFluids.WOODOIL, 500)
                .sourceOrder(1)
                .save(consumer, id("mixer/biofuel_sunflower"));
        MixerRecipeBuilder.mixer(HbmFluids.NITROGLYCERIN, 1_000, 20)
                .input1(HbmFluids.PETROLEUM, 1_000)
                .input2(HbmFluids.NITRIC_ACID, 1_000)
                .sourceOrder(0)
                .save(consumer, id("mixer/nitroglycerin_petroleum"));
        MixerRecipeBuilder.mixer(HbmFluids.NITROGLYCERIN, 1_000, 20)
                .input1(HbmFluids.FISHOIL, 500)
                .input2(HbmFluids.NITRIC_ACID, 500)
                .sourceOrder(1)
                .save(consumer, id("mixer/nitroglycerin_fishoil"));
        MixerRecipeBuilder.mixer(HbmFluids.THORIUM_SALT, 1_000, 30)
                .input1(HbmFluids.CHLORINE, 1_000)
                .solidLegacyOre("dustTh232", 1)
                .save(consumer, id("mixer/thorium_salt"));
        MixerRecipeBuilder.mixer(HbmFluids.SYNGAS, 1_000, 50)
                .input1(HbmFluids.COALOIL, 500)
                .input2(HbmFluids.STEAM, 500)
                .save(consumer, id("mixer/syngas"));
        MixerRecipeBuilder.mixer(HbmFluids.OXYHYDROGEN, 1_000, 50)
                .input1(HbmFluids.HYDROGEN, 500)
                .input2(HbmFluids.AIR, 2_000)
                .sourceOrder(0)
                .save(consumer, id("mixer/oxyhydrogen_air"));
        MixerRecipeBuilder.mixer(HbmFluids.OXYHYDROGEN, 1_000, 50)
                .input1(HbmFluids.HYDROGEN, 500)
                .input2(HbmFluids.OXYGEN, 500)
                .sourceOrder(1)
                .save(consumer, id("mixer/oxyhydrogen_oxygen"));
        MixerRecipeBuilder.mixer(HbmFluids.PETROIL_LEADED, 12_000, 40)
                .input1(HbmFluids.PETROIL, 10_000)
                .solidItem(item("fuel_additive_antiknock"), 1)
                .save(consumer, id("mixer/petroil_leaded"));
        MixerRecipeBuilder.mixer(HbmFluids.GASOLINE_LEADED, 12_000, 40)
                .input1(HbmFluids.GASOLINE, 10_000)
                .solidItem(item("fuel_additive_antiknock"), 1)
                .save(consumer, id("mixer/gasoline_leaded"));
        MixerRecipeBuilder.mixer(HbmFluids.COALGAS_LEADED, 12_000, 40)
                .input1(HbmFluids.COALGAS, 10_000)
                .solidItem(item("fuel_additive_antiknock"), 1)
                .save(consumer, id("mixer/coalgas_leaded"));
        MixerRecipeBuilder.mixer(HbmFluids.DIESEL_REFORM, 1_000, 50)
                .input1(HbmFluids.DIESEL, 900)
                .input2(HbmFluids.REFORMATE, 100)
                .save(consumer, id("mixer/diesel_reform"));
        MixerRecipeBuilder.mixer(HbmFluids.DIESEL_CRACK_REFORM, 1_000, 50)
                .input1(HbmFluids.DIESEL_CRACK, 900)
                .input2(HbmFluids.REFORMATE, 100)
                .save(consumer, id("mixer/diesel_crack_reform"));
        MixerRecipeBuilder.mixer(HbmFluids.KEROSENE_REFORM, 1_000, 50)
                .input1(HbmFluids.KEROSENE, 900)
                .input2(HbmFluids.REFORMATE, 100)
                .save(consumer, id("mixer/kerosene_reform"));
        MixerRecipeBuilder.mixer(HbmFluids.CHLOROCALCITE_SOLUTION, 500, 50)
                .input1(HbmFluids.WATER, 250)
                .input2(HbmFluids.NITRIC_ACID, 250)
                .solidLegacyOre("dustChlorocalcite", 1)
                .save(consumer, id("mixer/chlorocalcite_solution"));
        MixerRecipeBuilder.mixer(HbmFluids.CHLOROCALCITE_MIX, 1_000, 50)
                .input1(HbmFluids.CHLOROCALCITE_SOLUTION, 500)
                .input2(HbmFluids.SULFURIC_ACID, 500)
                .solidItem(item("powder_flux"), 1)
                .save(consumer, id("mixer/chlorocalcite_mix"));
        MixerRecipeBuilder.mixer(HbmFluids.PHEROMONE_M, 2_000, 10)
                .input1(HbmFluids.PHEROMONE, 1_500)
                .input2(HbmFluids.BLOOD, 500)
                .solidItem(item("pill_herbal"), 1)
                .save(consumer, id("mixer/pheromone_m"));
        MixerRecipeBuilder.mixer(HbmFluids.BAUXITE_SOLUTION, 300, 80)
                .input1(HbmFluids.LYE, 50)
                .solidItem(block("stone_resource_bauxite"), 1)
                .save(consumer, id("mixer/bauxite_solution"));
        MixerRecipeBuilder.mixer(HbmFluids.LYE, 100, 100)
                .input1(HbmFluids.WATER, 100)
                .solidItem(item("powder_ash_wood"), 1)
                .save(consumer, id("mixer/lye"));
        MixerRecipeBuilder.mixer(HbmFluids.ALUMINA, 200, 40)
                .input1(HbmFluids.SODIUM_ALUMINATE, 150)
                .solidLegacyOre("dustFluorite", 3)
                .sourceOrder(0)
                .save(consumer, id("mixer/alumina_fluorite"));
        MixerRecipeBuilder.mixer(HbmFluids.ALUMINA, 300, 40)
                .input1(HbmFluids.SODIUM_ALUMINATE, 150)
                .solidItem(item("chunk_ore_cryolite"), 1)
                .sourceOrder(1)
                .save(consumer, id("mixer/alumina_cryolite"));
        MixerRecipeBuilder.mixer(HbmFluids.PERFLUOROMETHYL, 1_000, 20)
                .input1(HbmFluids.PETROLEUM, 1_000)
                .input2(HbmFluids.UNSATURATEDS, 500)
                .solidLegacyOre("dustFluorite", 1)
                .save(consumer, id("mixer/perfluoromethyl"));
        MixerRecipeBuilder.mixer(HbmFluids.BITUMEN, 50, 20)
                .solidLegacyOre("anyTar", Ingredient.of(forgeTag("any/tar")), 1)
                .sourceOrder(0)
                .save(consumer, id("mixer/bitumen_tar"));
    }

    private static void pressRecipes(Consumer<FinishedRecipe> consumer) {
        flatPressRecipes(consumer);
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, HbmIngredient.legacyOre("ingotIron", 1),
                        new ItemStack(item("plate_iron")))
                .sourceOrder(11)
                .save(consumer, id("press/iron_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, HbmIngredient.legacyOre("ingotGold", 1),
                        new ItemStack(item("plate_gold")))
                .sourceOrder(12)
                .save(consumer, id("press/gold_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, HbmIngredient.legacyOre("ingotTitanium", 1),
                        new ItemStack(item("plate_titanium")))
                .sourceOrder(13)
                .save(consumer, id("press/titanium_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, HbmIngredient.legacyOre("ingotAluminum", 1),
                        new ItemStack(item("plate_aluminium")))
                .sourceOrder(14)
                .save(consumer, id("press/aluminium_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, HbmIngredient.legacyOre("ingotSteel", 1),
                        new ItemStack(item("plate_steel")))
                .sourceOrder(15)
                .save(consumer, id("press/steel_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, HbmIngredient.legacyOre("ingotLead", 1),
                        new ItemStack(item("plate_lead")))
                .sourceOrder(16)
                .save(consumer, id("press/lead_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE,
                        HbmIngredient.legacyOre("ingotCopper", Ingredient.of(Items.COPPER_INGOT), 1),
                        new ItemStack(item("plate_copper")))
                .sourceOrder(17)
                .save(consumer, id("press/copper_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, HbmIngredient.legacyOre("ingotSchrabidium", 1),
                        new ItemStack(item("plate_schrabidium")))
                .sourceOrder(18)
                .save(consumer, id("press/schrabidium_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, HbmIngredient.legacyOre("ingotCMBSteel", 1),
                        new ItemStack(item("plate_combine_steel")))
                .sourceOrder(19)
                .save(consumer, id("press/combine_steel_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, HbmIngredient.legacyOre("ingotGunMetal", 1),
                        new ItemStack(item("plate_gunmetal")))
                .sourceOrder(20)
                .save(consumer, id("press/gunmetal_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, HbmIngredient.legacyOre("ingotWeaponSteel", 1),
                        new ItemStack(item("plate_weaponsteel")))
                .sourceOrder(21)
                .save(consumer, id("press/weaponsteel_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, HbmIngredient.legacyOre("ingotSaturnite", 1),
                        new ItemStack(item("plate_saturnite")))
                .sourceOrder(22)
                .save(consumer, id("press/saturnite_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, HbmIngredient.legacyOre("ingotDuraSteel", 1),
                        new ItemStack(item("plate_dura_steel")))
                .sourceOrder(23)
                .save(consumer, id("press/dura_steel_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.C9, HbmIngredient.legacyOre("plateGunMetal", 1),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.CASING, 0).get(), 4))
                .sourceOrder(24)
                .save(consumer, id("press/casing_small_gunmetal"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.C50, HbmIngredient.legacyOre("plateGunMetal", 1),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.CASING, 1).get(), 2))
                .sourceOrder(25)
                .save(consumer, id("press/casing_large_gunmetal"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.C9, HbmIngredient.legacyOre("plateWeaponSteel", 1),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.CASING, 2).get(), 4))
                .sourceOrder(26)
                .save(consumer, id("press/casing_small_weaponsteel"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.C50, HbmIngredient.legacyOre("plateWeaponSteel", 1),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.CASING, 3).get(), 2))
                .sourceOrder(27)
                .save(consumer, id("press/casing_large_weaponsteel"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.WIRE, HbmIngredient.legacyOre("ingotCarbon", 1),
                        new ItemStack(item("wire_fine_carbon"), 8))
                .sourceOrder(28)
                .save(consumer, id("press/carbon_wire"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.WIRE, HbmIngredient.legacyOre("ingotGold", 1),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.WIRE_FINE, 7_900).get(), 8))
                .sourceOrder(29)
                .save(consumer, id("press/wire_gold"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.WIRE, HbmIngredient.legacyOre("ingotSchrabidium", 1),
                        new ItemStack(item("wire_fine_schrabidium"), 8))
                .sourceOrder(30)
                .save(consumer, id("press/schrabidium_wire"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.WIRE,
                        HbmIngredient.legacyOre("ingotCopper", Ingredient.of(Items.COPPER_INGOT), 1),
                        new ItemStack(item("wire_fine_copper"), 8))
                .sourceOrder(31)
                .save(consumer, id("press/copper_wire"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.WIRE, HbmIngredient.legacyOre("ingotTungsten", 1),
                        new ItemStack(item("wire_fine_tungsten"), 8))
                .sourceOrder(32)
                .save(consumer, id("press/tungsten_wire"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.WIRE, HbmIngredient.legacyOre("ingotAluminum", 1),
                        new ItemStack(item("wire_fine_aluminium"), 8))
                .sourceOrder(33)
                .save(consumer, id("press/aluminium_wire"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.WIRE, HbmIngredient.legacyOre("ingotLead", 1),
                        new ItemStack(item("wire_fine_lead"), 8))
                .sourceOrder(34)
                .save(consumer, id("press/lead_wire"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.WIRE, HbmIngredient.legacyOre("ingotZirconium", 1),
                        new ItemStack(item("wire_fine_zirconium"), 8))
                .sourceOrder(35)
                .save(consumer, id("press/zirconium_wire"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.WIRE, HbmIngredient.legacyOre("ingotSteel", 1),
                        new ItemStack(item("wire_fine_steel"), 8))
                .sourceOrder(36)
                .save(consumer, id("press/steel_wire"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.WIRE, HbmIngredient.legacyOre("ingotMingrade", 1),
                        new ItemStack(item("wire_fine_mingrade"), 8))
                .sourceOrder(37)
                .save(consumer, id("press/mingrade_wire"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.WIRE, HbmIngredient.legacyOre("ingotMagnetizedTungsten", 1),
                        new ItemStack(item("wire_fine_magnetized_tungsten"), 8))
                .sourceOrder(38)
                .save(consumer, id("press/magnetized_tungsten_wire"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.CIRCUIT, HbmIngredient.legacyOre("billetSilicon", 1),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.CIRCUIT, 4).get()))
                .sourceOrder(39)
                .save(consumer, id("press/circuit_silicon"));

        for (int i = 0; i < 8; i++) {
            ItemPressStamp.StampType stamp = ItemPressStamp.StampType.byName("printing" + (i + 1));
            PressRecipeBuilder.press(stamp, legacyItem("minecraft", "paper", Items.PAPER),
                            new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.PAGE_OF, i).get()))
                    .sourceOrder(40 + i)
                    .save(consumer, id("press/page_of_page" + (i + 1)));
        }
    }

    private static void flatPressRecipes(Consumer<FinishedRecipe> consumer) {
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, HbmIngredient.legacyOre("dustNetherQuartz", 1),
                        new ItemStack(Items.QUARTZ))
                .sourceOrder(0)
                .save(consumer, id("press/flat_quartz"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, HbmIngredient.legacyOre("dustLapis", 1),
                        new ItemStack(Items.LAPIS_LAZULI))
                .sourceOrder(1)
                .save(consumer, id("press/flat_lapis"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, HbmIngredient.legacyOre("dustDiamond", 1),
                        new ItemStack(Items.DIAMOND))
                .sourceOrder(2)
                .save(consumer, id("press/flat_diamond"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, HbmIngredient.legacyOre("dustEmerald", 1),
                        new ItemStack(Items.EMERALD))
                .sourceOrder(3)
                .save(consumer, id("press/flat_emerald"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, legacyHbmItem("biomass", item("biomass")),
                        new ItemStack(item("biomass_compressed")))
                .sourceOrder(4)
                .save(consumer, id("press/flat_biomass"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, HbmIngredient.legacyOre("gemAnyCoke", 1),
                        new ItemStack(item("ingot_graphite")))
                .sourceOrder(5)
                .save(consumer, id("press/flat_graphite"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT,
                        legacyHbmItem("meteorite_sword_reforged", item("meteorite_sword_reforged")),
                        new ItemStack(item("meteorite_sword_hardened")))
                .sourceOrder(6)
                .save(consumer, id("press/flat_meteorite_sword_hardened"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT,
                        legacyItem("minecraft", "log", 3, Blocks.JUNGLE_LOG),
                        new ItemStack(item("ball_resin")))
                .sourceOrder(7)
                .save(consumer, id("press/flat_resin"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, HbmIngredient.legacyOre("dustCoal", 1),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.BRIQUETTE, 0).get()))
                .sourceOrder(8)
                .save(consumer, id("press/flat_briquette_coal"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, HbmIngredient.legacyOre("dustLignite", 1),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.BRIQUETTE, 1).get()))
                .sourceOrder(9)
                .save(consumer, id("press/flat_briquette_lignite"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT,
                        legacyHbmItem("powder_sawdust", item("powder_sawdust")),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.BRIQUETTE, 2).get()))
                .sourceOrder(10)
                .save(consumer, id("press/flat_briquette_wood"));
    }

    private static void legacyMissingMachineAcquisitionRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.DECON.get())
                .pattern("BGB")
                .pattern("SAS")
                .pattern("BSB")
                .define('B', forgeTag("ingots/beryllium"))
                .define('G', Blocks.IRON_BARS)
                .define('S', forgeTag("ingots/steel"))
                .define('A', StrictNBTIngredient.of(legacyVariantStack(ModBlocks.RAD_ABSORBER.get(), 1, 0)))
                .unlockedBy("has_rad_absorber", has(ModBlocks.RAD_ABSORBER.get()))
                .save(consumer, id("blocks/decon"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.TELEANCHOR.get())
                .pattern("ODO")
                .pattern("EAE")
                .pattern("ODO")
                .define('O', Blocks.OBSIDIAN)
                .define('D', Items.DIAMOND)
                .define('E', item("powder_magic"))
                .define('A', item("gem_alexandrite"))
                .unlockedBy("has_alexandrite", has(item("gem_alexandrite")))
                .save(consumer, id("machines/teleanchor"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.FIELD_DISTURBER.get())
                .pattern("ICI")
                .pattern("CAC")
                .pattern("ICI")
                .define('I', forgeTag("ingots/starmetal"))
                .define('C', item("circuit_bismoid"))
                .define('A', item("gem_alexandrite"))
                .unlockedBy("has_bismoid_circuit", has(item("circuit_bismoid")))
                .save(consumer, id("machines/field_disturber"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.FAN.get())
                .pattern("BPB")
                .pattern("PRP")
                .pattern("BPB")
                .define('B', forgeTag("bolts/steel"))
                .define('P', forgeTag("plates/iron"))
                .define('R', Items.REDSTONE)
                .unlockedBy("has_steel_bolt", has(forgeTag("bolts/steel")))
                .save(consumer, id("machines/fan"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.PRESS_PREHEATER.get())
                .pattern("CCC")
                .pattern("SLS")
                .pattern("TST")
                .define('C', forgeTag("plates/copper"))
                .define('S', Blocks.STONE)
                .define('L', Items.LAVA_BUCKET)
                .define('T', forgeTag("ingots/tungsten"))
                .unlockedBy("has_lava_bucket", has(Items.LAVA_BUCKET))
                .save(consumer, id("machines/press_preheater"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.MACHINE_CONVEYOR_PRESS.get())
                .pattern("CPC")
                .pattern("CBC")
                .pattern("CCC")
                .define('C', forgeTag("plates/copper"))
                .define('P', ModBlocks.MACHINE_EPRESS.get())
                .define('B', ModItems.CONVEYOR_WAND.get())
                .unlockedBy("has_electric_press", has(ModBlocks.MACHINE_EPRESS.get()))
                .save(consumer, id("machines/conveyor_press"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.MACHINE_WOOD_BURNER.get())
                .pattern("PPP")
                .pattern("CFC")
                .pattern("I I")
                .define('P', forgeTag("plates/steel"))
                .define('C', ModItems.COPPER_COIL.get())
                .define('F', Blocks.FURNACE)
                .define('I', forgeTag("ingots/iron"))
                .unlockedBy("has_steel_plate", has(forgeTag("plates/steel")))
                .save(consumer, id("machines/wood_burner"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.MACHINE_AMMO_PRESS.get())
                .pattern("IPI")
                .pattern("C C")
                .pattern("SSS")
                .define('I', forgeTag("ingots/iron"))
                .define('P', Blocks.PISTON)
                .define('C', forgeTag("ingots/copper"))
                .define('S', Blocks.STONE)
                .unlockedBy("has_piston", has(Blocks.PISTON))
                .save(consumer, id("machines/ammo_press"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.REFUELER.get())
                .pattern("SS")
                .pattern("HC")
                .pattern("SS")
                .define('S', forgeTag("plates/titanium"))
                .define('H', item("part_generic_piston_hydraulic"))
                .define('C', item("circuit_basic"))
                .unlockedBy("has_titanium_plate", has(forgeTag("plates/titanium")))
                .save(consumer, id("machines/refueler"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RADIOBOX.get())
                .pattern("PLP")
                .pattern("PSP")
                .pattern("PLP")
                .define('P', forgeTag("plates/steel"))
                .define('L', forgeTag("plates/dura_steel"))
                .define('S', item("ring_starmetal"))
                .unlockedBy("has_starmetal_ring", has(item("ring_starmetal")))
                .save(consumer, id("machines/radiobox"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RADIOREC.get())
                .pattern("  W")
                .pattern("PCP")
                .pattern("PIP")
                .define('W', item("wire_fine_copper"))
                .define('P', forgeTag("plates/steel"))
                .define('C', forgeTag("circuits/vacuum_tube"))
                .define('I', forgeTag("ingots/any_plastic"))
                .unlockedBy("has_vacuum_tube_circuit", has(forgeTag("circuits/vacuum_tube")))
                .save(consumer, id("machines/radiorec"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.TESLA.get())
                .pattern("CCC")
                .pattern("PIP")
                .pattern("WTW")
                .define('C', ModItems.COPPER_COIL.get())
                .define('P', forgeTag("ingots/any_plastic"))
                .define('I', forgeTag("ingots/iron"))
                .define('W', vanillaTag("planks"))
                .define('T', ModBlocks.MACHINE_TRANSFORMER.get())
                .unlockedBy("has_copper_coil", has(ModItems.COPPER_COIL.get()))
                .save(consumer, id("machines/tesla"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.MACHINE_MIXER.get())
                .pattern("PIP")
                .pattern("GCG")
                .pattern("PMP")
                .define('P', forgeTag("plates/steel"))
                .define('I', forgeTag("ingots/dura_steel"))
                .define('G', forgeTag("glass_panes"))
                .define('C', legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 0))
                .define('M', ModItems.MOTOR.get())
                .unlockedBy("has_motor", has(ModItems.MOTOR.get()))
                .save(consumer, id("machines/mixer"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.MACHINE_DRAIN.get())
                .pattern("PPP")
                .pattern("T  ")
                .pattern("PPP")
                .define('P', forgeTag("cast_plates/steel"))
                .define('T', item("tank_steel"))
                .unlockedBy("has_tank_steel", has(item("tank_steel")))
                .save(consumer, id("machines/drain"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.MACHINE_INTAKE.get())
                .pattern("GGG")
                .pattern("PMP")
                .pattern("PTP")
                .define('G', ModBlocks.STEEL_GRATE.get())
                .define('P', forgeTag("plates/steel"))
                .define('M', ModItems.MOTOR.get())
                .define('T', item("tank_steel"))
                .unlockedBy("has_steel_grate", has(ModBlocks.STEEL_GRATE.get()))
                .save(consumer, id("machines/intake"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.MACHINE_STORAGE_DRUM.get())
                .pattern("LLL")
                .pattern("LTL")
                .pattern("LLL")
                .define('L', forgeTag("plates/lead"))
                .define('T', item("tank_steel"))
                .unlockedBy("has_tank_steel", has(item("tank_steel")))
                .save(consumer, id("machines/storage_drum"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.MACHINE_AUTOCRAFTER.get())
                .pattern("SCS")
                .pattern("MWM")
                .pattern("SCS")
                .define('S', forgeTag("plates/steel"))
                .define('C', forgeTag("circuits/vacuum_tube"))
                .define('M', ModItems.MOTOR.get())
                .define('W', net.minecraft.world.level.block.Blocks.CRAFTING_TABLE)
                .unlockedBy("has_vacuum_tube", has(forgeTag("circuits/vacuum_tube")))
                .save(consumer, id("machines/autocrafter"));

        GenericMachineRecipeBuilder.assembly("ass.precass", 1_200, 100)
                .inputLegacyOre("plateCastSteel", 8)
                .inputLegacyOre("ingotZirconium", 8)
                .inputItem(ModItems.MOTOR.get(), 4)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 10, 4)
                .outputItem(ModBlocks.MACHINE_PRECASS.get())
                .sourceOrder(80)
                .save(consumer, id("assembly_machine/precass"));

        GenericMachineRecipeBuilder.assembly("ass.arcfurnace", 200, 100)
                .inputLegacyOre("blockAnyConcrete", 12)
                .inputLegacyOre("ingotAnyPlastic", 8)
                .inputItem(item("ingot_firebrick"), 16)
                .inputLegacyOre("plateCastSteel", 8)
                .inputItem(block("machine_transformer"), 1)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 7, 1)
                .outputItem(ModBlocks.MACHINE_ARC_FURNACE.get())
                .sourceOrder(83)
                .save(consumer, id("assembly_machine/arc_furnace"));

        GenericMachineRecipeBuilder.assembly("ass.epress", 100, 100)
                .inputLegacyOre("plateSteel", 8)
                .inputLegacyOre("ingotAnyRubber", 4)
                .inputLegacyMeta(LegacyMetaItemMappings.PART_GENERIC, 1, 2)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 1)
                .outputItem(ModBlocks.MACHINE_EPRESS.get())
                .sourceOrder(103)
                .save(consumer, id("assembly_machine/electric_press"));

        GenericMachineRecipeBuilder.assembly("ass.fel", 400, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 2, 1)
                .inputLegacyOre("wireDenseGold", 64)
                .inputLegacyOre("plateCastSteel", 12)
                .inputLegacyOre("ingotAnyPlastic", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.PART_GENERIC, 5, 4)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 1, 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 4)
                .outputItem(ModBlocks.MACHINE_FEL.get())
                .sourceOrder(104)
                .save(consumer, id("assembly_machine/fel"));

        GenericMachineRecipeBuilder.assembly("ass.mininglaser", 400, 100)
                .inputLegacyOre("plateSteel", 16)
                .inputLegacyOre("shellTitanium", 4)
                .inputLegacyOre("plateDuraSteel", 4)
                .inputItem(item("crystal_redstone"), 3)
                .inputItem(Items.DIAMOND, 3)
                .inputLegacyOre("ingotAnyPlastic", 8)
                .inputItem(ModItems.MOTOR.get(), 3)
                .outputItem(ModBlocks.MACHINE_MINING_LASER.get())
                .sourceOrder(118)
                .save(consumer, id("assembly_machine/mining_laser"));

        GenericMachineRecipeBuilder.assembly("ass.teleporter", 100, 100)
                .inputLegacyOre("plateTitanium", 12)
                .inputLegacyOre("plateDuraSteel", 12)
                .inputLegacyOre("wireFineGold", 32)
                .inputItem(item("entanglement_kit"), 1)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 2, 1)
                .outputItem(ModBlocks.MACHINE_TELEPORTER.get())
                .sourceOrder(119)
                .save(consumer, id("assembly_machine/teleporter"));

        GenericMachineRecipeBuilder.assembly("ass.strandcaster", 200, 100)
                .inputItem(item("ingot_firebrick"), 16)
                .inputLegacyOre("plateCastSteel", 6)
                .inputLegacyOre("plateSextupleCopper", 2)
                .inputLegacyOre("shellSteel", 2)
                .inputLegacyOre("blockAnyConcrete", 8)
                .outputItem(ModBlocks.MACHINE_STRAND_CASTER.get())
                .sourceOrder(123)
                .save(consumer, id("assembly_machine/strand_caster"));

        GenericMachineRecipeBuilder.assembly("ass.hephaestus", 200, 100)
                .inputLegacyOre("ntmpipeSteel", 12)
                .inputLegacyOre("ingotSteel", 24)
                .inputLegacyOre("plateCopper", 24)
                .inputLegacyOre("ingotNiobium", 4)
                .inputLegacyOre("ingotRubber", 12)
                .inputItem(block("glass_quartz"), 16)
                .outputItem(ModBlocks.MACHINE_HEPHAESTUS.get())
                .sourceOrder(134)
                .save(consumer, id("assembly_machine/hephaestus"));

        GenericMachineRecipeBuilder.assembly("ass.leviturbine", 600, 100)
                .inputLegacyOre("shellSteel", 6)
                .inputLegacyOre("plateSextupleSteel", 16)
                .inputLegacyOre("plateTitanium", 12)
                .inputLegacyOre("ingotAnyResistantAlloy", 16)
                .inputItem(item("turbine_tungsten"), 5)
                .inputItem(item("turbine_titanium"), 3)
                .inputItem(item("flywheel_beryllium"), 1)
                .inputLegacyOre("wireDenseGold", 48)
                .inputLegacyOre("ntmpipeDuraSteel", 16)
                .inputLegacyOre("ntmpipeSteel", 16)
                .outputItem(ModBlocks.MACHINE_CHUNGUS.get())
                .sourceOrder(136)
                .save(consumer, id("assembly_machine/leviturbine"));

        GenericMachineRecipeBuilder.assembly("ass.radgen", 400, 100)
                .inputLegacyOre("ingotSteel", 8)
                .inputLegacyOre("plateSteel", 32)
                .inputItem(item("coil_magnetized_tungsten"), 6)
                .inputLegacyOre("wireFineMagnetizedTungsten", 24)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 16)
                .inputItem(item("reactor_core"), 3)
                .inputLegacyOre("ingotStarmetal", 1)
                .inputLegacyOre("dyeRed", 1)
                .outputItem(ModBlocks.MACHINE_RADGEN.get())
                .pool(LegacyBlueprintPools.PREFIX_DISCOVER + "radgen")
                .sourceOrder(137)
                .save(consumer, id("assembly_machine/radgen"));

        GenericMachineRecipeBuilder.assembly("ass.hpcondenser", 600, 100)
                .inputLegacyOre("plateSextupleSteel", 8)
                .inputLegacyOre("plateSextupleAnyResistantAlloy", 4)
                .inputLegacyOre("plateCopper", 16)
                .inputItem(item("motor_desh"), 3)
                .inputLegacyOre("ntmpipeSteel", 24)
                .inputFluidContainerLegacyOre(HbmFluids.LUBRICANT, 1_000, 4)
                .outputItem(ModBlocks.MACHINE_CONDENSER_POWERED.get())
                .sourceOrder(138)
                .save(consumer, id("assembly_machine/powered_condenser"));

        GenericMachineRecipeBuilder.assembly("ass.orbus", 300, 100)
                .inputLegacyOre("plateSextupleAnyResistantAlloy", 8)
                .inputLegacyOre("plateCastSaturnite", 4)
                .inputLegacyOre("wireDenseBscco", 8)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_SC, 6, 1)
                .outputItem(ModBlocks.MACHINE_ORBUS.get())
                .sourceOrder(146)
                .save(consumer, id("assembly_machine/orbus"));

        GenericMachineRecipeBuilder.assembly("ass.minenaval", 300, 100)
                .inputItem(item("sphere_steel"), 1)
                .inputLegacyOre("pipeSteel", 3)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 1)
                .inputLegacyOre("ingotAnyPlasticExplosive", 24)
                .outputItem(ModBlocks.MINE_NAVAL.get())
                .sourceOrder(196)
                .save(consumer, id("assembly_machine/mine_naval"));

        GenericMachineRecipeBuilder.assembly("ass.gadget", 300, 100)
                .inputItem(item("sphere_steel"), 1)
                .inputItem(item("fins_flat"), 2)
                .inputItem(item("pedestal_steel"), 1)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 1)
                .inputLegacyOre("dyeGray", 8)
                .outputItem(ModBlocks.NUKE_GADGET.get())
                .sourceOrder(197)
                .save(consumer, id("assembly_machine/nuke_gadget"));

        GenericMachineRecipeBuilder.assembly("ass.littleboy", 300, 100)
                .inputLegacyOre("shellSteel", 2)
                .inputItem(item("fins_small_steel"), 1)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 2)
                .inputLegacyOre("dyeBlue", 4)
                .outputItem(ModBlocks.NUKE_BOY.get())
                .sourceOrder(198)
                .save(consumer, id("assembly_machine/nuke_boy"));

        GenericMachineRecipeBuilder.assembly("ass.fatman", 300, 100)
                .inputItem(item("sphere_steel"), 1)
                .inputLegacyOre("shellSteel", 2)
                .inputItem(item("fins_big_steel"), 1)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 3)
                .inputLegacyOre("dyeYellow", 6)
                .outputItem(ModBlocks.NUKE_MAN.get())
                .sourceOrder(199)
                .save(consumer, id("assembly_machine/nuke_man"));

        GenericMachineRecipeBuilder.assembly("ass.ivymike", 600, 100)
                .inputItem(item("sphere_steel"), 1)
                .inputLegacyOre("shellAluminium", 4)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 14, 8)
                .inputLegacyOre("dyeLightGray", 16)
                .outputItem(ModBlocks.NUKE_MIKE.get())
                .sourceOrder(200)
                .save(consumer, id("assembly_machine/nuke_mike"));

        GenericMachineRecipeBuilder.assembly("ass.tsarbomba", 1_200, 100)
                .inputItem(item("sphere_steel"), 1)
                .inputLegacyOre("shellTitanium", 6)
                .inputLegacyOre("shellSteel", 2)
                .inputItem(item("fins_tri_steel"), 1)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 14, 16)
                .inputLegacyOre("dyeBlack", 8)
                .outputItem(ModBlocks.NUKE_TSAR.get())
                .sourceOrder(201)
                .save(consumer, id("assembly_machine/nuke_tsar"));

        GenericMachineRecipeBuilder.assembly("ass.ninadidnothingwrong", 300, 100)
                .inputItem(item("dysfunctional_reactor"), 1)
                .inputLegacyOre("shellSteel", 2)
                .inputItem(item("ingot_euphemium"), 3)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 14, 8)
                .outputItem(ModBlocks.NUKE_PROTOTYPE.get())
                .sourceOrder(202)
                .save(consumer, id("assembly_machine/nuke_prototype"));

        GenericMachineRecipeBuilder.assembly("ass.fleija", 300, 100)
                .inputLegacyOre("shellAluminium", 1)
                .inputItem(item("fins_quad_titanium"), 1)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 1)
                .inputLegacyOre("dyeWhite", 4)
                .outputItem(ModBlocks.NUKE_FLEIJA.get())
                .sourceOrder(203)
                .save(consumer, id("assembly_machine/nuke_fleija"));

        GenericMachineRecipeBuilder.assembly("ass.solinium", 300, 100)
                .inputLegacyOre("shellSteel", 2)
                .inputItem(item("fins_quad_titanium"), 1)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 1)
                .inputLegacyOre("dyeGray", 8)
                .outputItem(ModBlocks.NUKE_SOLINIUM.get())
                .sourceOrder(204)
                .save(consumer, id("assembly_machine/nuke_solinium"));

        GenericMachineRecipeBuilder.assembly("ass.n2mine", 200, 100)
                .inputLegacyOre("shellSteel", 6)
                .inputLegacyOre("wireFineMagnetizedTungsten", 12)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 2)
                .inputLegacyOre("dyeGreen", 8)
                .outputItem(ModBlocks.NUKE_N2.get())
                .sourceOrder(205)
                .save(consumer, id("assembly_machine/nuke_n2"));

        GenericMachineRecipeBuilder.assembly("ass.balefirebomb", 400, 100)
                .inputItem(item("sphere_steel"), 1)
                .inputLegacyOre("shellTitanium", 6)
                .inputItem(item("fins_big_steel"), 1)
                .inputItem(item("powder_magic"), 8)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 14, 4)
                .inputLegacyOre("dyeGray", 8)
                .outputItem(ModBlocks.NUKE_FSTBMB.get())
                .sourceOrder(206)
                .save(consumer, id("assembly_machine/nuke_fstbmb"));

        GenericMachineRecipeBuilder.assembly("ass.customnuke", 300, 100)
                .inputLegacyOre("shellSteel", 2)
                .inputItem(item("fins_small_steel"), 1)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 14, 8)
                .inputLegacyOre("dyeGray", 4)
                .outputItem(ModBlocks.NUKE_CUSTOM.get())
                .sourceOrder(207)
                .save(consumer, id("assembly_machine/nuke_custom"));

        GenericMachineRecipeBuilder.assembly("ass.levibomb", 200, 100)
                .inputLegacyOre("plateTitanium", 12)
                .inputLegacyOre("nuggetSchrabidium", 3)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 4)
                .inputLegacyOre("wireDenseGold", 8)
                .outputItem(ModBlocks.FLOAT_BOMB.get())
                .sourceOrder(208)
                .save(consumer, id("assembly_machine/float_bomb"));

        GenericMachineRecipeBuilder.assembly("ass.endobomb", 200, 100)
                .inputLegacyOre("plateTitanium", 12)
                .inputItem(item("powder_ice"), 32)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 1)
                .inputItem(item("coil_gold"), 4)
                .outputItem(ModBlocks.THERM_ENDO.get())
                .sourceOrder(209)
                .save(consumer, id("assembly_machine/therm_endo"));

        GenericMachineRecipeBuilder.assembly("ass.exobomb", 200, 100)
                .inputLegacyOre("plateTitanium", 12)
                .inputLegacyOre("dustRedPhosphorus", 32)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 1)
                .inputItem(item("coil_gold"), 4)
                .outputItem(ModBlocks.THERM_EXO.get())
                .sourceOrder(210)
                .save(consumer, id("assembly_machine/therm_exo"));
    }

    private static ItemLike item(String legacyName) {
        RegistryObject<Item> item = ModItems.legacyItem(legacyName);
        if (item == null) {
            throw new IllegalStateException("Missing legacy item for recipe: " + legacyName);
        }
        return item.get();
    }

    private static ItemLike block(String legacyName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        if (block == null) {
            throw new IllegalStateException("Missing legacy block for recipe: " + legacyName);
        }
        return block.get();
    }

    private static void cartPowderRecipe(Consumer<FinishedRecipe> consumer, String base, ItemLike emptyCart,
            ItemLike output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, output)
                .pattern("PPP")
                .pattern("PCP")
                .pattern("PPP")
                .define('P', Items.GUNPOWDER)
                .define('C', emptyCart)
                .unlockedBy("has_empty_cart", has(emptyCart))
                .save(consumer, id("tools/cart_powder_" + base));
    }

    private static void cartSemtexRecipe(Consumer<FinishedRecipe> consumer, String base, ItemLike emptyCart,
            ItemLike output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, output)
                .pattern("S")
                .pattern("C")
                .define('S', ModBlocks.SEMTEX.get())
                .define('C', emptyCart)
                .unlockedBy("has_empty_cart", has(emptyCart))
                .save(consumer, id("tools/cart_semtex_" + base));
    }

    private static void cartDestroyerRecipe(Consumer<FinishedRecipe> consumer, String base, ItemLike emptyCart,
            ItemLike output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, output)
                .pattern("S S")
                .pattern("BLB")
                .pattern("SCS")
                .define('S', forgeTag("ingots/steel"))
                .define('B', ModItems.SHREDDER_BLADES_STEEL.get())
                .define('L', HbmFluidContainerIngredient.of(HbmFluids.LAVA, 1_000))
                .define('C', emptyCart)
                .unlockedBy("has_empty_cart", has(emptyCart))
                .save(consumer, id("tools/cart_destroyer_" + base));
    }

    private static void legacyArmorTableRecipe(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.MACHINE_ARMOR_TABLE.get())
                .pattern("PPP")
                .pattern("TCT")
                .pattern("TST")
                .define('P', ModItems.STEEL_PLATE.get())
                .define('T', ModItems.TUNGSTEN_INGOT.get())
                .define('C', Blocks.CRAFTING_TABLE)
                .define('S', block("block_steel"))
                .unlockedBy("has_steel_plate", has(ModItems.STEEL_PLATE.get()))
                .save(consumer, id("machines/armor_table"));
    }

    private static ItemLike legacyBatteryPack(int legacyMeta) {
        return LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.BATTERY_PACK, legacyMeta).get();
    }

    private static ItemLike legacySelfChargingBattery(int legacyMeta) {
        return LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.BATTERY_SC, legacyMeta).get();
    }

    private static ItemLike legacyCircuit(int legacyMeta) {
        return LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.CIRCUIT, legacyMeta).get();
    }

    private static ItemLike legacyMetaItem(ResourceLocation legacyId, int legacyMeta) {
        return LegacyMetaItemMappings.requireItem(legacyId, legacyMeta).get();
    }

    private static void legacyWeaponTableRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModBlocks.MACHINE_WEAPON_TABLE.get())
                .pattern("PPP")
                .pattern("TCT")
                .pattern("TST")
                .define('P', forgeTag("plates/gun_metal"))
                .define('T', forgeTag("ingots/steel"))
                .define('C', Blocks.CRAFTING_TABLE)
                .define('S', block("block_steel"))
                .unlockedBy("has_gunmetal_plate", has(forgeTag("plates/gun_metal")))
                .save(consumer, id("weapon/machine_weapon_table"));
    }

    private static void legacyWeaponModRecipes(Consumer<FinishedRecipe> consumer) {
        weaponModGenericRecipe(consumer, "iron_damage", "ingots/gun_metal", "ingots/iron", true);
        weaponModGenericRecipe(consumer, "iron_dura", "ingots/gun_metal", "ingots/iron", false);
        weaponModGenericRecipe(consumer, "steel_damage", "gun_mechanisms/gun_metal", "cast_plates/steel", true);
        weaponModGenericRecipe(consumer, "steel_dura", "plates/gun_metal", "cast_plates/steel", false);
        weaponModGenericRecipe(consumer, "dura_damage", "gun_mechanisms/gun_metal", "cast_plates/dura_steel", true);
        weaponModGenericRecipe(consumer, "dura_dura", "plates/gun_metal", "cast_plates/dura_steel", false);
        weaponModGenericRecipe(consumer, "desh_damage", "gun_mechanisms/gun_metal", "cast_plates/desh", true);
        weaponModGenericRecipe(consumer, "desh_dura", "plates/gun_metal", "cast_plates/desh", false);
        weaponModGenericRecipe(consumer, "wsteel_damage", "gun_mechanisms/weapon_steel", "cast_plates/weapon_steel", true);
        weaponModGenericRecipe(consumer, "wsteel_dura", "plates/weapon_steel", "cast_plates/weapon_steel", false);
        weaponModGenericRecipe(consumer, "ferro_damage", "gun_mechanisms/weapon_steel", "cast_plates/ferrouranium", true);
        weaponModGenericRecipe(consumer, "ferro_dura", "plates/weapon_steel", "cast_plates/ferrouranium", false);
        weaponModGenericRecipe(consumer, "tcalloy_damage", "gun_mechanisms/weapon_steel", "cast_plates/any_resistant_alloy", true);
        weaponModGenericRecipe(consumer, "tcalloy_dura", "plates/weapon_steel", "cast_plates/any_resistant_alloy", false);
        weaponModGenericRecipe(consumer, "bigmt_damage", "gun_mechanisms/saturnite", "cast_plates/saturnite", true);
        weaponModGenericRecipe(consumer, "bigmt_dura", "plates/saturnite", "cast_plates/saturnite", false);
        weaponModGenericRecipe(consumer, "bronze_damage", "gun_mechanisms/saturnite", "cast_plates/any_bismoid_bronze", true);
        weaponModGenericRecipe(consumer, "bronze_dura", "plates/saturnite", "cast_plates/any_bismoid_bronze", false);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_silencer"))
                .pattern("P")
                .pattern("B")
                .pattern("P")
                .define('P', forgeTag("ingots/any_plastic"))
                .define('B', forgeTag("light_barrels/steel"))
                .unlockedBy("has_steel_barrel", has(forgeTag("light_barrels/steel")))
                .save(consumer, id("weapon/weapon_mod_special_silencer"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_scope"))
                .pattern("SPS")
                .pattern("G G")
                .pattern("SPS")
                .define('P', forgeTag("ingots/any_plastic"))
                .define('S', forgeTag("plates/steel"))
                .define('G', forgeTag("glass_panes"))
                .unlockedBy("has_glass_pane", has(forgeTag("glass_panes")))
                .save(consumer, id("weapon/weapon_mod_special_scope"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_saw"))
                .pattern("BBS")
                .pattern("BHS")
                .define('B', forgeTag("bolts/steel"))
                .define('S', forgeTag("rods/wooden"))
                .define('H', forgeTag("plates/dura_steel"))
                .unlockedBy("has_dura_plate", has(forgeTag("plates/dura_steel")))
                .save(consumer, id("weapon/weapon_mod_special_saw"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_speedloader"))
                .pattern(" B ")
                .pattern("BSB")
                .pattern(" B ")
                .define('B', forgeTag("bolts/steel"))
                .define('S', forgeTag("plates/weapon_steel"))
                .unlockedBy("has_weaponsteel_plate", has(forgeTag("plates/weapon_steel")))
                .save(consumer, id("weapon/weapon_mod_special_speedloader"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_slowdown"))
                .pattern(" I ")
                .pattern(" M ")
                .pattern("I I")
                .define('I', forgeTag("ingots/weapon_steel"))
                .define('M', forgeTag("gun_mechanisms/weapon_steel"))
                .unlockedBy("has_weaponsteel_mechanism", has(forgeTag("gun_mechanisms/weapon_steel")))
                .save(consumer, id("weapon/weapon_mod_special_slowdown"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_speedup"))
                .pattern("PIP")
                .pattern("WWW")
                .pattern("PIP")
                .define('P', forgeTag("plates/weapon_steel"))
                .define('I', forgeTag("ingots/gun_metal"))
                .define('W', forgeTag("dense_wires/gold"))
                .unlockedBy("has_gold_dense_wire", has(forgeTag("dense_wires/gold")))
                .save(consumer, id("weapon/weapon_mod_special_speedup"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_greasegun"))
                .pattern("BRM")
                .pattern("P G")
                .define('B', forgeTag("light_barrels/weapon_steel"))
                .define('R', forgeTag("light_receivers/weapon_steel"))
                .define('M', forgeTag("gun_mechanisms/weapon_steel"))
                .define('P', forgeTag("plates/dura_steel"))
                .define('G', forgeTag("grips/any_plastic"))
                .unlockedBy("has_weaponsteel_mechanism", has(forgeTag("gun_mechanisms/weapon_steel")))
                .save(consumer, id("weapon/weapon_mod_special_greasegun"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_choke"))
                .pattern("P")
                .pattern("B")
                .pattern("P")
                .define('P', forgeTag("plates/weapon_steel"))
                .define('B', forgeTag("light_barrels/dura_steel"))
                .unlockedBy("has_dura_barrel", has(forgeTag("light_barrels/dura_steel")))
                .save(consumer, id("weapon/weapon_mod_special_choke"));
        weaponModFurnitureRecipe(consumer, "furniture_green", "dyes/green");
        weaponModFurnitureRecipe(consumer, "furniture_black", "dyes/black");
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_skin_saturnite"))
                .pattern("BRM")
                .pattern(" P ")
                .define('B', forgeTag("light_barrels/saturnite"))
                .define('R', forgeTag("light_receivers/saturnite"))
                .define('M', forgeTag("gun_mechanisms/saturnite"))
                .define('P', forgeTag("plates/saturnite"))
                .unlockedBy("has_saturnite_mechanism", has(forgeTag("gun_mechanisms/saturnite")))
                .save(consumer, id("weapon/weapon_mod_special_skin_saturnite"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_stack_mag"))
                .pattern("P P")
                .pattern("P P")
                .pattern("PMP")
                .define('P', forgeTag("plates/weapon_steel"))
                .define('M', forgeTag("gun_mechanisms/saturnite"))
                .unlockedBy("has_saturnite_mechanism", has(forgeTag("gun_mechanisms/saturnite")))
                .save(consumer, id("weapon/weapon_mod_special_stack_mag"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_bayonet"))
                .pattern("  P")
                .pattern("BBB")
                .define('P', forgeTag("plates/steel"))
                .define('B', forgeTag("bolts/steel"))
                .unlockedBy("has_steel_bolt", has(forgeTag("bolts/steel")))
                .save(consumer, id("weapon/weapon_mod_special_bayonet"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_las_shotgun"))
                .pattern("PPP")
                .pattern("RCR")
                .pattern("PPP")
                .define('P', forgeTag("ingots/any_hardplastic"))
                .define('R', item("crystal_redstone"))
                .define('C', legacyCircuit(9))
                .unlockedBy("has_advanced_circuit", has(legacyCircuit(9)))
                .save(consumer, id("weapon/weapon_mod_special_las_shotgun"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_las_capacitor"))
                .pattern("CCC")
                .pattern("PIP")
                .define('C', legacyCircuit(2))
                .define('P', forgeTag("ingots/any_hardplastic"))
                .define('I', legacyCircuit(6))
                .unlockedBy("has_tantalium_capacitor", has(legacyCircuit(2)))
                .save(consumer, id("weapon/weapon_mod_special_las_capacitor"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_las_auto"))
                .pattern(" C ")
                .pattern("RFR")
                .pattern(" C ")
                .define('C', legacyCircuit(6))
                .define('R', item("crystal_redstone"))
                .define('F', forgeTag("heavy_receivers/any_bismoid_bronze"))
                .unlockedBy("has_bismoid_chip", has(legacyCircuit(6)))
                .save(consumer, id("weapon/weapon_mod_special_las_auto"));
        weaponModDrillRecipe(consumer, "drill_hss", "ingots/dura_steel", "ingots/any_plastic", "gun_mechanisms/gun_metal");
        weaponModDrillRecipe(consumer, "drill_weaponsteel", "ingots/weapon_steel", "ingots/rubber", "gun_mechanisms/gun_metal");
        weaponModDrillRecipe(consumer, "drill_tcalloy", "ingots/any_resistant_alloy", "ingots/rubber", "gun_mechanisms/weapon_steel");
        weaponModDrillRecipe(consumer, "drill_saturnite", "ingots/saturnite", "ingots/any_hardplastic", "gun_mechanisms/weapon_steel");
        weaponModEngineRecipe(consumer, "engine_diesel", "plates/dura_steel", item("piston_selenium"), forgeTag("pipes/steel"));
        weaponModEngineRecipe(consumer, "engine_aviation", "cast_plates/dura_steel", item("piston_selenium"), forgeTag("gun_mechanisms/gun_metal"));
        weaponModEngineRecipe(consumer, "engine_electric", "ingots/any_plastic", forgeTag("dense_wires/gold"), legacyBatteryPack(7));
        weaponModEngineRecipe(consumer, "engine_turbo", "cast_plates/any_bismoid_bronze", item("piston_selenium"), forgeTag("gun_mechanisms/weapon_steel"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_magnet"))
                .pattern("RGR")
                .pattern("GBG")
                .pattern("RGR")
                .define('R', forgeTag("ingots/rubber"))
                .define('G', forgeTag("dense_wires/gold"))
                .define('B', forgeTag("storage_blocks/niobium"))
                .unlockedBy("has_niobium_block", has(forgeTag("storage_blocks/niobium")))
                .save(consumer, id("weapon/weapon_mod_special_magnet"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_sifter"))
                .pattern("IGI")
                .pattern("IGI")
                .define('I', forgeTag("ingots/dura_steel"))
                .define('G', block("steel_grate"))
                .unlockedBy("has_steel_grate", has(block("steel_grate")))
                .save(consumer, id("weapon/weapon_mod_special_sifter"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_canisters"))
                .pattern(" R ")
                .pattern("CCC")
                .pattern("SSS")
                .define('R', forgeTag("pipes/rubber"))
                .define('C', item("canister_empty"))
                .define('S', forgeTag("plates/steel"))
                .unlockedBy("has_empty_canister", has(item("canister_empty")))
                .save(consumer, id("weapon/weapon_mod_special_canisters"));
    }

    private static void legacySednaGunRecipes(Consumer<FinishedRecipe> consumer) {
        legacyGunRecipe(consumer, "gun_pepperbox", new String[] { "IIW", "  C" },
                'I', forgeTag("ingots/iron"), 'W', vanillaTag("planks"), 'C', forgeTag("ingots/copper"));
        legacyGunRecipe(consumer, "gun_light_revolver", new String[] { "BRM", "  G" },
                'B', forgeTag("light_barrels/steel"), 'R', forgeTag("light_receivers/steel"),
                'M', forgeTag("gun_mechanisms/gun_metal"), 'G', forgeTag("grips/wood"));
        legacyGunRecipe(consumer, "gun_light_revolver_atlas", new String[] { " M ", "MAM", " M " },
                'M', forgeTag("gun_mechanisms/weapon_steel"), 'A', item("gun_light_revolver"));
        legacyGunRecipe(consumer, "gun_henry", new String[] { "BRP", "BMS" },
                'B', forgeTag("light_barrels/steel"), 'R', forgeTag("light_receivers/gun_metal"),
                'M', forgeTag("gun_mechanisms/gun_metal"), 'S', forgeTag("stocks/wood"),
                'P', forgeTag("plates/gun_metal"));
        legacyGunRecipe(consumer, "gun_henry_lincoln", new String[] { " M ", "PGP", " M " },
                'M', forgeTag("gun_mechanisms/weapon_steel"), 'P', forgeTag("cast_plates/gold"),
                'G', item("gun_henry"));
        legacyGunRecipe(consumer, "gun_greasegun", new String[] { "BRS", "SMG" },
                'B', forgeTag("light_barrels/steel"), 'R', forgeTag("light_receivers/steel"),
                'S', forgeTag("bolts/steel"), 'M', forgeTag("gun_mechanisms/gun_metal"),
                'G', forgeTag("grips/steel"));
        legacyGunRecipe(consumer, "gun_maresleg", new String[] { "BRM", "BGS" },
                'B', forgeTag("light_barrels/steel"), 'R', forgeTag("light_receivers/steel"),
                'M', forgeTag("gun_mechanisms/gun_metal"), 'G', forgeTag("bolts/steel"),
                'S', forgeTag("stocks/wood"));
        legacyGunRecipe(consumer, "gun_maresleg_akimbo", new String[] { "SMS" },
                'S', item("gun_maresleg"), 'M', forgeTag("gun_mechanisms/weapon_steel"));
        legacyGunRecipe(consumer, "gun_flaregun", new String[] { "BRM", "  G" },
                'B', forgeTag("heavy_barrels/steel"), 'R', forgeTag("light_receivers/steel"),
                'M', forgeTag("gun_mechanisms/gun_metal"), 'G', forgeTag("grips/steel"));
        legacyGunRecipe(consumer, "gun_am180", new String[] { "BRS", "GMG" },
                'B', forgeTag("light_barrels/dura_steel"), 'R', forgeTag("light_receivers/dura_steel"),
                'M', forgeTag("gun_mechanisms/gun_metal"), 'G', forgeTag("grips/wood"),
                'S', forgeTag("stocks/wood"));
        legacyGunRecipe(consumer, "gun_liberator", new String[] { "BB ", "BBM", "G G" },
                'B', forgeTag("light_barrels/dura_steel"), 'M', forgeTag("gun_mechanisms/gun_metal"),
                'G', forgeTag("grips/wood"));
        legacyGunRecipe(consumer, "gun_congolake", new String[] { "BM ", "BRS", "G  " },
                'B', forgeTag("heavy_barrels/dura_steel"), 'M', forgeTag("gun_mechanisms/gun_metal"),
                'R', forgeTag("light_receivers/dura_steel"), 'S', forgeTag("stocks/wood"),
                'G', forgeTag("grips/wood"));
        legacyGunRecipe(consumer, "gun_flamer", new String[] { " MG", "BBR", " GM" },
                'M', forgeTag("gun_mechanisms/gun_metal"), 'G', forgeTag("grips/dura_steel"),
                'B', forgeTag("heavy_barrels/dura_steel"), 'R', forgeTag("heavy_receivers/dura_steel"));
        legacyGunRecipe(consumer, "gun_flamer_topaz", new String[] { " M ", "MFM", " M " },
                'M', forgeTag("gun_mechanisms/weapon_steel"), 'F', item("gun_flamer"));
        legacyGunRecipe(consumer, "gun_heavy_revolver", new String[] { "BRM", "  G" },
                'B', forgeTag("light_barrels/desh"), 'R', forgeTag("light_receivers/desh"),
                'M', forgeTag("gun_mechanisms/gun_metal"), 'G', forgeTag("grips/wood"));
        legacyGunRecipe(consumer, "gun_carbine", new String[] { "BRM", "G S" },
                'B', forgeTag("light_barrels/desh"), 'R', forgeTag("light_receivers/desh"),
                'M', forgeTag("gun_mechanisms/gun_metal"), 'G', forgeTag("grips/wood"),
                'S', forgeTag("stocks/wood"));
        legacyGunRecipe(consumer, "gun_uzi", new String[] { "BRS", " GM" },
                'B', forgeTag("light_barrels/desh"), 'R', forgeTag("light_receivers/desh"),
                'S', forgeTag("stocks/any_plastic"), 'G', forgeTag("grips/any_plastic"),
                'M', forgeTag("gun_mechanisms/gun_metal"));
        legacyGunRecipe(consumer, "gun_uzi_akimbo", new String[] { "UMU" },
                'U', item("gun_uzi"), 'M', forgeTag("gun_mechanisms/weapon_steel"));
        legacyGunRecipe(consumer, "gun_spas12", new String[] { "BRM", "BGS" },
                'B', forgeTag("light_barrels/desh"), 'R', forgeTag("light_receivers/desh"),
                'M', forgeTag("gun_mechanisms/gun_metal"), 'G', forgeTag("grips/any_plastic"),
                'S', forgeTag("stocks/desh"));
        legacyGunRecipe(consumer, "gun_panzerschreck", new String[] { "BBB", "PGM" },
                'B', forgeTag("heavy_barrels/desh"), 'P', forgeTag("cast_plates/steel"),
                'G', forgeTag("grips/desh"), 'M', forgeTag("gun_mechanisms/gun_metal"));
        legacyGunRecipe(consumer, "gun_star_f", new String[] { "BRM", "  G" },
                'B', forgeTag("light_barrels/weapon_steel"), 'R', forgeTag("light_receivers/weapon_steel"),
                'M', forgeTag("gun_mechanisms/weapon_steel"), 'G', forgeTag("grips/any_plastic"));
        legacyGunRecipe(consumer, "gun_star_f_akimbo", new String[] { "UMU" },
                'U', item("gun_star_f"), 'M', forgeTag("gun_mechanisms/saturnite"));
        legacyGunRecipe(consumer, "gun_g3", new String[] { "BRM", "WGS" },
                'B', forgeTag("light_barrels/weapon_steel"), 'R', forgeTag("light_receivers/weapon_steel"),
                'M', forgeTag("gun_mechanisms/weapon_steel"), 'W', forgeTag("grips/wood"),
                'G', forgeTag("grips/rubber"), 'S', forgeTag("stocks/wood"));
        legacyGunRecipe(consumer, "gun_g3_zebra", new String[] { " M ", "MPM", " M " },
                'M', forgeTag("gun_mechanisms/saturnite"), 'P', item("gun_g3"));
        legacyGunRecipe(consumer, "gun_stinger", new String[] { "BBB", "PGM" },
                'B', forgeTag("heavy_barrels/weapon_steel"), 'P', legacyCircuit(9),
                'G', forgeTag("grips/weapon_steel"), 'M', forgeTag("gun_mechanisms/weapon_steel"));
        legacyGunRecipe(consumer, "gun_mk108", new String[] { " GG", "BRM", " D " },
                'G', forgeTag("grips/any_plastic"), 'B', forgeTag("heavy_barrels/weapon_steel"),
                'R', forgeTag("heavy_receivers/weapon_steel"), 'M', forgeTag("gun_mechanisms/weapon_steel"),
                'D', forgeTag("shells/weapon_steel"));
        legacyGunRecipe(consumer, "gun_chemthrower", new String[] { "MHW", "PSS" },
                'M', forgeTag("gun_mechanisms/weapon_steel"), 'H', forgeTag("pipes/rubber"),
                'W', item("wrench"), 'P', forgeTag("heavy_barrels/weapon_steel"),
                'S', forgeTag("shells/weapon_steel"));
        legacyGunRecipe(consumer, "gun_amat", new String[] { " C ", "BRS", " MG" },
                'G', forgeTag("grips/wood"), 'B', forgeTag("heavy_barrels/ferrouranium"),
                'R', forgeTag("heavy_receivers/ferrouranium"), 'M', forgeTag("gun_mechanisms/weapon_steel"),
                'C', item("weapon_mod_special_scope"), 'S', forgeTag("stocks/wood"));
        legacyGunRecipe(consumer, "gun_m2", new String[] { "  G", "BRM", "  G" },
                'G', forgeTag("grips/wood"), 'B', forgeTag("heavy_barrels/ferrouranium"),
                'R', forgeTag("heavy_receivers/ferrouranium"), 'M', forgeTag("gun_mechanisms/weapon_steel"));
        legacyGunRecipe(consumer, "gun_autoshotgun", new String[] { "BRM", "G G" },
                'B', forgeTag("heavy_barrels/ferrouranium"), 'R', forgeTag("heavy_receivers/ferrouranium"),
                'M', forgeTag("gun_mechanisms/weapon_steel"), 'G', forgeTag("grips/any_plastic"));
        legacyGunRecipe(consumer, "gun_autoshotgun_shredder", new String[] { " M ", "MAM", " M " },
                'M', forgeTag("gun_mechanisms/saturnite"), 'A', item("gun_autoshotgun"));
        legacyGunRecipe(consumer, "gun_quadro", new String[] { "BCB", "BMB", "GG " },
                'B', forgeTag("heavy_barrels/ferrouranium"), 'C', legacyCircuit(9),
                'M', forgeTag("gun_mechanisms/weapon_steel"), 'G', forgeTag("grips/any_plastic"));
        legacyGunRecipe(consumer, "gun_lag", new String[] { "BRM", "  G" },
                'B', forgeTag("light_barrels/any_resistant_alloy"),
                'R', forgeTag("light_receivers/any_resistant_alloy"),
                'M', forgeTag("gun_mechanisms/weapon_steel"), 'G', forgeTag("grips/any_plastic"));
        legacyGunRecipe(consumer, "gun_minigun", new String[] { "BMG", "BRE", "BGM" },
                'B', forgeTag("light_barrels/any_resistant_alloy"),
                'M', forgeTag("gun_mechanisms/weapon_steel"), 'G', forgeTag("grips/any_plastic"),
                'R', forgeTag("heavy_receivers/any_resistant_alloy"), 'E', item("motor_desh"));
        legacyGunRecipe(consumer, "gun_missile_launcher", new String[] { " CM", "BBB", "G  " },
                'C', legacyCircuit(9), 'M', forgeTag("gun_mechanisms/weapon_steel"),
                'B', forgeTag("heavy_barrels/any_resistant_alloy"), 'G', forgeTag("grips/any_plastic"));
        legacyGunRecipe(consumer, "gun_tesla_cannon", new String[] { "CCC", "BRB", "MGE" },
                'C', item("coil_copper"), 'B', forgeTag("heavy_barrels/any_resistant_alloy"),
                'R', forgeTag("heavy_receivers/any_resistant_alloy"),
                'M', forgeTag("gun_mechanisms/weapon_steel"), 'G', forgeTag("grips/any_plastic"),
                'E', legacyCircuit(9));
        legacyGunRecipe(consumer, "gun_laser_pistol", new String[] { "CRM", "GG " },
                'C', item("crystal_redstone"), 'R', forgeTag("light_receivers/saturnite"),
                'M', forgeTag("gun_mechanisms/saturnite"), 'G', forgeTag("grips/any_hardplastic"));
        legacyGunRecipe(consumer, "gun_laser_pistol_pew_pew", new String[] { " M ", "MPM", " M " },
                'M', forgeTag("gun_mechanisms/saturnite"), 'P', item("gun_laser_pistol"));
        legacyGunRecipe(consumer, "gun_stg77", new String[] { " D ", "BRS", "GGM" },
                'D', item("weapon_mod_special_scope"), 'B', forgeTag("light_barrels/saturnite"),
                'R', forgeTag("light_receivers/saturnite"), 'S', forgeTag("stocks/any_hardplastic"),
                'G', forgeTag("grips/any_hardplastic"), 'M', forgeTag("gun_mechanisms/saturnite"));
        legacyGunRecipe(consumer, "gun_fatman", new String[] { "PPP", "BSR", "G M" },
                'P', forgeTag("plates/saturnite"), 'B', forgeTag("heavy_barrels/saturnite"),
                'S', forgeTag("shells/saturnite"), 'R', forgeTag("heavy_receivers/saturnite"),
                'G', forgeTag("grips/any_hardplastic"), 'M', forgeTag("gun_mechanisms/saturnite"));
        legacyGunRecipe(consumer, "gun_tau", new String[] { " RD", "CTT", "GMS" },
                'D', legacyCircuit(6), 'C', forgeTag("pipes/copper"), 'T', item("coil_copper_torus"),
                'G', forgeTag("grips/any_hardplastic"), 'R', forgeTag("light_receivers/saturnite"),
                'M', forgeTag("gun_mechanisms/saturnite"), 'S', forgeTag("stocks/any_hardplastic"));
        legacyGunRecipe(consumer, "gun_lasrifle", new String[] { "DLC", "BRS", "MG " },
                'D', item("crystal_redstone"), 'L', item("weapon_mod_special_scope"), 'C', legacyCircuit(6),
                'B', forgeTag("light_barrels/any_bismoid_bronze"),
                'R', forgeTag("light_receivers/any_bismoid_bronze"),
                'S', forgeTag("stocks/any_hardplastic"), 'M', forgeTag("gun_mechanisms/saturnite"),
                'G', forgeTag("grips/any_hardplastic"));
        legacyGunRecipe(consumer, "gun_charge_thrower", "gun_charge_thrower_leather",
                new String[] { "MMM", "BBL", "GG " },
                'M', forgeTag("gun_mechanisms/gun_metal"), 'B', forgeTag("heavy_barrels/steel"),
                'G', forgeTag("grips/steel"), 'L', Items.LEATHER);
        legacyGunRecipe(consumer, "gun_charge_thrower", "gun_charge_thrower_rubber",
                new String[] { "MMM", "BBL", "GG " },
                'M', forgeTag("gun_mechanisms/gun_metal"), 'B', forgeTag("heavy_barrels/steel"),
                'G', forgeTag("grips/steel"), 'L', forgeTag("ingots/any_rubber"));
        legacyGunRecipe(consumer, "gun_drill", new String[] { " GL", "IBP", " GL" },
                'G', forgeTag("ingots/gun_metal"), 'L', forgeTag("ingots/any_rubber"),
                'I', forgeTag("ingots/titanium"), 'B', forgeTag("storage_blocks/steel"),
                'P', item("piston_selenium"));
        legacyGunRecipe(consumer, "gun_pa_melee", new String[] { " C ", "MWM" },
                'C', item("circuit_basic"), 'M', item("motor"), 'W', forgeTag("dense_wires/gold"));
        legacyGunRecipe(consumer, "gun_pa_ranged", new String[] { "C", "W", "P" },
                'C', item("circuit_basic"), 'W', forgeTag("dense_wires/gold"),
                'P', forgeTag("ingots/any_plastic"));
        legacyGunRecipe(consumer, "gun_fireext", new String[] { "HB", " T" },
                'H', forgeTag("pipes/steel"), 'B', forgeTag("bolts/steel"), 'T', item("tank_steel"));
    }

    private static void legacyGunRecipe(Consumer<FinishedRecipe> consumer, String result, String[] pattern,
            Object... definitions) {
        legacyGunRecipe(consumer, result, result, pattern, definitions);
    }

    private static void legacyGunRecipe(Consumer<FinishedRecipe> consumer, String result, String recipeName,
            String[] pattern, Object... definitions) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item(result));
        for (String row : pattern) {
            builder.pattern(row);
        }
        for (int i = 0; i < definitions.length; i += 2) {
            defineIngredient(builder, (Character) definitions[i], definitions[i + 1]);
        }
        builder.unlockedBy("has_weapon_table", has(ModBlocks.MACHINE_WEAPON_TABLE.get()))
                .save(consumer, id("weapon/" + recipeName));
    }

    private static void weaponModGenericRecipe(Consumer<FinishedRecipe> consumer, String suffix, String coreTag,
            String materialTag, boolean damage) {
        ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT,
                        item("weapon_mod_generic_" + suffix))
                .requires(forgeTag(coreTag))
                .requires(forgeTag(materialTag))
                .requires(item("ducttape"))
                .unlockedBy("has_ducttape", has(item("ducttape")));
        if (damage) {
            builder.requires(forgeTag(materialTag)).requires(forgeTag(materialTag));
        }
        builder.save(consumer, id("weapon/weapon_mod_generic_" + suffix));
    }

    private static void weaponModFurnitureRecipe(Consumer<FinishedRecipe> consumer, String suffix, String dyeTag) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_" + suffix))
                .pattern("PDS")
                .pattern("  G")
                .define('P', forgeTag("ingots/any_plastic"))
                .define('D', forgeTag(dyeTag))
                .define('S', forgeTag("stocks/any_plastic"))
                .define('G', forgeTag("grips/any_plastic"))
                .unlockedBy("has_any_plastic", has(forgeTag("ingots/any_plastic")))
                .save(consumer, id("weapon/weapon_mod_special_" + suffix));
    }

    private static void weaponModDrillRecipe(Consumer<FinishedRecipe> consumer, String suffix, String ingotTag,
            String gripTag, String mechanismTag) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("weapon_mod_special_" + suffix))
                .pattern(" IP")
                .pattern("IIM")
                .pattern(" IP")
                .define('I', forgeTag(ingotTag))
                .define('P', forgeTag(gripTag))
                .define('M', forgeTag(mechanismTag))
                .unlockedBy("has_drill_material", has(forgeTag(ingotTag)))
                .save(consumer, id("weapon/weapon_mod_special_" + suffix));
    }

    private static void shapedExplosiveStick(Consumer<FinishedRecipe> consumer, String result, ItemLike fuse,
                                             ItemLike explosive, String unlockName, ItemLike unlockItem) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item(result), 4)
                .pattern(" S ")
                .pattern("PDP")
                .pattern("PDP")
                .define('S', fuse)
                .define('P', Items.PAPER)
                .define('D', explosive)
                .unlockedBy(unlockName, has(unlockItem))
                .save(consumer, id("weapon/" + result));
    }

    private static void weaponModEngineRecipe(Consumer<FinishedRecipe> consumer, String suffix, String frameTag,
            Object pistonIngredient, Object centerIngredient) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,
                        item("weapon_mod_special_" + suffix))
                .pattern("DSD")
                .pattern("PPP")
                .pattern("DSD")
                .define('D', forgeTag(frameTag))
                .unlockedBy("has_engine_frame", has(forgeTag(frameTag)));
        defineIngredient(builder, 'P', pistonIngredient);
        defineIngredient(builder, 'S', centerIngredient);
        builder.save(consumer, id("weapon/weapon_mod_special_" + suffix));
    }

    private static void defineIngredient(ShapedRecipeBuilder builder, char key, Object ingredient) {
        if (ingredient instanceof TagKey<?> tag) {
            @SuppressWarnings("unchecked")
            TagKey<Item> itemTag = (TagKey<Item>) tag;
            builder.define(key, itemTag);
        } else if (ingredient instanceof ItemLike itemLike) {
            builder.define(key, itemLike);
        } else {
            throw new IllegalArgumentException("Unsupported recipe ingredient: " + ingredient);
        }
    }

    private static void legacyArtilleryAmmoRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("ammo_arty"))
                .pattern("CIC")
                .pattern("CSC")
                .pattern("CCC")
                .define('C', item("cordite"))
                .define('I', forgeTag("storage_blocks/iron"))
                .define('S', forgeTag("shells/copper"))
                .unlockedBy("has_cordite", has(item("cordite")))
                .save(consumer, id("weapon/ammo_arty"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("ammo_arty_classic"))
                .pattern(" D ")
                .pattern("DSD")
                .pattern(" D ")
                .define('D', item("ball_dynamite"))
                .define('S', item("ammo_arty"))
                .unlockedBy("has_ammo_arty", has(item("ammo_arty")))
                .save(consumer, id("weapon/ammo_arty_classic"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("ammo_arty_he"))
                .pattern("TTT")
                .pattern("TST")
                .pattern("TTT")
                .define('T', item("ball_tnt"))
                .define('S', item("ammo_arty"))
                .unlockedBy("has_ammo_arty", has(item("ammo_arty")))
                .save(consumer, id("weapon/ammo_arty_he"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("ammo_arty_phosphorus"))
                .pattern("D")
                .pattern("S")
                .pattern("D")
                .define('D', item("ingot_phosphorus"))
                .define('S', item("ammo_arty"))
                .unlockedBy("has_ammo_arty", has(item("ammo_arty")))
                .save(consumer, id("weapon/ammo_arty_phosphorus"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("ammo_arty_phosphorus_multi"))
                .pattern("DSD")
                .pattern("SCS")
                .pattern("DSD")
                .define('D', item("ingot_phosphorus"))
                .define('S', item("ammo_arty_phosphorus"))
                .define('C', ModBlocks.DET_CORD.get())
                .unlockedBy("has_ammo_arty_phosphorus", has(item("ammo_arty_phosphorus")))
                .save(consumer, id("weapon/ammo_arty_phosphorus_multi"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("ammo_arty_mini_nuke"))
                .pattern(" P ")
                .pattern("NSN")
                .pattern(" P ")
                .define('P', item("nugget_pu239"))
                .define('N', item("neutron_reflector"))
                .define('S', item("ammo_arty"))
                .unlockedBy("has_ammo_arty", has(item("ammo_arty")))
                .save(consumer, id("weapon/ammo_arty_mini_nuke"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("ammo_arty_mini_nuke_multi"))
                .pattern("DSD")
                .pattern("SCS")
                .pattern("DSD")
                .define('D', item("neutron_reflector"))
                .define('S', item("ammo_arty_mini_nuke"))
                .define('C', ModBlocks.DET_CORD.get())
                .unlockedBy("has_ammo_arty_mini_nuke", has(item("ammo_arty_mini_nuke")))
                .save(consumer, id("weapon/ammo_arty_mini_nuke_multi"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, item("ammo_arty_nuke"))
                .requires(item("ammo_arty_he"))
                .requires(item("boy_bullet"))
                .requires(item("boy_target"))
                .requires(item("boy_shielding"))
                .requires(item("circuit_controller"))
                .requires(item("ducttape"))
                .unlockedBy("has_ammo_arty_he", has(item("ammo_arty_he")))
                .save(consumer, id("weapon/ammo_arty_nuke"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("ammo_arty_cargo"))
                .pattern(" I ")
                .pattern(" S ")
                .pattern("CCC")
                .define('C', item("cordite"))
                .define('I', item("sphere_steel"))
                .define('S', forgeTag("shells/copper"))
                .unlockedBy("has_sphere_steel", has(item("sphere_steel")))
                .save(consumer, id("weapon/ammo_arty_cargo"));

        JsonObject cargoShellCrafting = new JsonObject();
        cargoShellCrafting.addProperty("type", id("cargo_shell_crafting").toString());
        cargoShellCrafting.addProperty("category", "equipment");
        consumer.accept(finishedCompatRecipe(id("weapon/cargo_shell_crafting"), cargoShellCrafting));

        GenericMachineRecipeBuilder.assembly("chem.shellchlorine", 100, 1_000)
                .inputItem(item("ammo_arty"), 1)
                .inputLegacyOre("ingotAnyPlastic", 1)
                .inputFluid(HbmFluids.CHLORINE, 4_000)
                .outputItem(item("ammo_arty_chlorine"))
                .sourceOrder(317)
                .save(consumer, id("assembly_machine/shell_chlorine"));

        GenericMachineRecipeBuilder.assembly("ass.shellphosgene", 100, 1_000)
                .inputItem(item("ammo_arty"), 1)
                .inputLegacyOre("ingotAnyPlastic", 1)
                .inputFluid(HbmFluids.PHOSGENE, 4_000)
                .outputItem(item("ammo_arty_phosgene"))
                .sourceOrder(318)
                .save(consumer, id("assembly_machine/shell_phosgene"));

        GenericMachineRecipeBuilder.assembly("ass.shellmustard", 100, 1_000)
                .inputItem(item("ammo_arty"), 1)
                .inputLegacyOre("ingotAnyPlastic", 1)
                .inputFluid(HbmFluids.MUSTARDGAS, 4_000)
                .outputItem(item("ammo_arty_mustard_gas"))
                .sourceOrder(319)
                .save(consumer, id("assembly_machine/shell_mustard"));

        GenericMachineRecipeBuilder.assembly("ass.himarssmall", 100, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputLegacyOre("ingotAnyPlastic", 12)
                .inputItem(item("rocket_fuel"), 48)
                .inputLegacyOre("ingotAnyHighExplosive", 48)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 6)
                .outputItem(item("ammo_himars_standard"))
                .sourceOrder(241)
                .save(consumer, id("assembly_machine/himarssmall"));

        GenericMachineRecipeBuilder.assembly("ass.himarssmallhe", 100, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputLegacyOre("ingotAnyPlastic", 24)
                .inputItem(item("rocket_fuel"), 48)
                .inputLegacyOre("ingotAnyPlasticExplosive", 18)
                .inputLegacyOre("ingotAnyHighExplosive", 48)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 6)
                .outputItem(item("ammo_himars_standard_he"))
                .sourceOrder(242)
                .save(consumer, id("assembly_machine/himarssmallhe"));

        GenericMachineRecipeBuilder.assembly("ass.himarssmallwp", 100, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputLegacyOre("ingotAnyPlastic", 24)
                .inputItem(item("rocket_fuel"), 48)
                .inputItem(item("ingot_phosphorus"), 18)
                .inputLegacyOre("ingotAnyHighExplosive", 48)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 6)
                .outputItem(item("ammo_himars_standard_wp"))
                .sourceOrder(243)
                .save(consumer, id("assembly_machine/himarssmallwp"));

        GenericMachineRecipeBuilder.assembly("ass.himarssmalltb", 100, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputLegacyOre("ingotAnyPlastic", 24)
                .inputItem(item("rocket_fuel"), 48)
                .inputItem(item("ball_tatb"), 32)
                .inputFluidContainerLegacyOre(HbmFluids.KEROSENE_REFORM, 1_000, 12)
                .inputFluidContainerLegacyOre(HbmFluids.PEROXIDE, 1_000, 12)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 6)
                .outputItem(item("ammo_himars_standard_tb"))
                .sourceOrder(244)
                .save(consumer, id("assembly_machine/himarssmalltb"));

        GenericMachineRecipeBuilder.assembly("ass.himarssmallnuke", 100, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputLegacyOre("ingotAnyPlastic", 24)
                .inputItem(item("rocket_fuel"), 48)
                .inputItem(item("ball_tatb"), 6)
                .inputItem(item("nugget_pu239"), 12)
                .inputItem(item("neutron_reflector"), 12)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 6)
                .outputItem(item("ammo_himars_standard_mini_nuke"))
                .sourceOrder(245)
                .save(consumer, id("assembly_machine/himarssmallnuke"));

        GenericMachineRecipeBuilder.assembly("ass.himarssmalllava", 100, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputLegacyOre("ingotAnyHardPlastic", 12)
                .inputItem(item("rocket_fuel"), 32)
                .inputItem(item("ball_tatb"), 4)
                .inputLegacyOre("gemVolcanic", 1)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 6)
                .outputItem(item("ammo_himars_standard_lava"))
                .sourceOrder(246)
                .save(consumer, id("assembly_machine/himarssmalllava"));

        GenericMachineRecipeBuilder.assembly("ass.himarslarge", 200, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputLegacyOre("ingotAnyHardPlastic", 12)
                .inputItem(item("rocket_fuel"), 36)
                .inputItem(item("ball_tatb"), 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 1)
                .outputItem(item("ammo_himars_single"))
                .sourceOrder(247)
                .save(consumer, id("assembly_machine/himarslarge"));

        GenericMachineRecipeBuilder.assembly("ass.himarslargetb", 200, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputLegacyOre("ingotAnyHardPlastic", 12)
                .inputItem(item("rocket_fuel"), 36)
                .inputItem(item("ball_tatb"), 24)
                .inputFluidContainerLegacyOre(HbmFluids.KEROSENE_REFORM, 1_000, 16)
                .inputFluidContainerLegacyOre(HbmFluids.PEROXIDE, 1_000, 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 1)
                .outputItem(item("ammo_himars_single_tb"))
                .sourceOrder(248)
                .save(consumer, id("assembly_machine/himarslargetb"));
    }

    private static void legacyCustomMissilePartRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.assembly("ass.mpt10kero", 100, 100)
                .inputItem(item("seg_10"), 1)
                .inputLegacyOre("ntmpipeSteel", 1)
                .inputLegacyOre("ingotTungsten", 4)
                .inputLegacyOre("plateSteel", 4)
                .outputItem(item("mp_thruster_10_kerosene"))
                .sourceOrder(270)
                .save(consumer, id("assembly_machine/mpt10kero"));

        GenericMachineRecipeBuilder.assembly("ass.mpt10solid", 100, 100)
                .inputItem(item("seg_10"), 1)
                .inputLegacyOre("ntmpipeDuraSteel", 1)
                .inputLegacyOre("ingotTungsten", 4)
                .inputLegacyOre("plateSteel", 4)
                .outputItem(item("mp_thruster_10_solid"))
                .sourceOrder(271)
                .save(consumer, id("assembly_machine/mpt10solid"));

        GenericMachineRecipeBuilder.assembly("ass.mpt10xenon", 100, 100)
                .inputItem(item("seg_10"), 1)
                .inputLegacyOre("plateDuraSteel", 4)
                .inputItem(item("arc_electrode_graphite"), 1)
                .outputItem(item("mp_thruster_10_xenon"))
                .sourceOrder(272)
                .save(consumer, id("assembly_machine/mpt10xenon"));

        GenericMachineRecipeBuilder.assembly("ass.mpt15kero", 200, 100)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("ntmpipeSteel", 3)
                .inputLegacyOre("ingotTungsten", 8)
                .inputLegacyOre("plateSteel", 8)
                .outputItem(item("mp_thruster_15_kerosene"))
                .sourceOrder(273)
                .save(consumer, id("assembly_machine/mpt15kero"));

        GenericMachineRecipeBuilder.assembly("ass.mpt15kerodual", 200, 100)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("ntmpipeSteel", 3)
                .inputLegacyOre("ingotTungsten", 8)
                .inputLegacyOre("plateSteel", 8)
                .outputItem(item("mp_thruster_15_kerosene_dual"))
                .sourceOrder(274)
                .save(consumer, id("assembly_machine/mpt15kerodual"));

        GenericMachineRecipeBuilder.assembly("ass.mpt15kerotriple", 200, 100)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("ntmpipeSteel", 3)
                .inputLegacyOre("ingotTungsten", 8)
                .inputLegacyOre("plateSteel", 8)
                .outputItem(item("mp_thruster_15_kerosene_triple"))
                .sourceOrder(275)
                .save(consumer, id("assembly_machine/mpt15kerotriple"));

        GenericMachineRecipeBuilder.assembly("ass.mpt15solid", 200, 100)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("ntmpipeDuraSteel", 3)
                .inputLegacyOre("ingotTungsten", 8)
                .inputLegacyOre("plateSteel", 8)
                .outputItem(item("mp_thruster_15_solid"))
                .sourceOrder(276)
                .save(consumer, id("assembly_machine/mpt15solid"));

        GenericMachineRecipeBuilder.assembly("ass.mpt15solid16", 200, 100)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("ntmpipeDuraSteel", 3)
                .inputLegacyOre("ingotTungsten", 8)
                .inputLegacyOre("plateSteel", 8)
                .outputItem(item("mp_thruster_15_solid_hexdecuple"))
                .sourceOrder(277)
                .save(consumer, id("assembly_machine/mpt15solid16"));

        GenericMachineRecipeBuilder.assembly("ass.mpt15hydro", 200, 100)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("ntmpipeDuraSteel", 3)
                .inputLegacyOre("ingotTungsten", 8)
                .inputLegacyOre("ingotDesh", 4)
                .outputItem(item("mp_thruster_15_hydrogen"))
                .sourceOrder(278)
                .save(consumer, id("assembly_machine/mpt15hydro"));

        GenericMachineRecipeBuilder.assembly("ass.mpt15hydrodual", 200, 100)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("ntmpipeDuraSteel", 3)
                .inputLegacyOre("ingotTungsten", 8)
                .inputLegacyOre("ingotDesh", 4)
                .outputItem(item("mp_thruster_15_hydrogen_dual"))
                .sourceOrder(279)
                .save(consumer, id("assembly_machine/mpt15hydrodual"));

        GenericMachineRecipeBuilder.assembly("ass.mpt15bfshort", 200, 100)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("ntmpipeDuraSteel", 5)
                .inputLegacyOre("plateCastTungsten", 8)
                .inputLegacyOre("plateSaturnite", 8)
                .outputItem(item("mp_thruster_15_balefire_short"))
                .sourceOrder(280)
                .save(consumer, id("assembly_machine/mpt15bfshort"));

        GenericMachineRecipeBuilder.assembly("ass.mpt15bf", 200, 100)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("ntmpipeDuraSteel", 5)
                .inputLegacyOre("plateCastTungsten", 16)
                .inputLegacyOre("plateSaturnite", 16)
                .outputItem(item("mp_thruster_15_balefire_short"))
                .sourceOrder(281)
                .save(consumer, id("assembly_machine/mpt15bf"));

        GenericMachineRecipeBuilder.assembly("ass.mpt15bflarge", 200, 100)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("ntmpipeDuraSteel", 10)
                .inputLegacyOre("plateCastTungsten", 16)
                .inputLegacyOre("plateSaturnite", 24)
                .outputItem(item("mp_thruster_15_balefire_large"))
                .sourceOrder(282)
                .save(consumer, id("assembly_machine/mpt15bflarge"));

        GenericMachineRecipeBuilder.assembly("ass.mpt20kero", 400, 100)
                .inputItem(item("seg_20"), 1)
                .inputLegacyOre("ntmpipeSteel", 6)
                .inputLegacyOre("ingotTungsten", 16)
                .inputLegacyOre("plateSteel", 16)
                .outputItem(item("mp_thruster_20_kerosene"))
                .sourceOrder(283)
                .save(consumer, id("assembly_machine/mpt20kero"));

        GenericMachineRecipeBuilder.assembly("ass.mpt20kerodual", 400, 100)
                .inputItem(item("seg_20"), 1)
                .inputLegacyOre("ntmpipeSteel", 6)
                .inputLegacyOre("ingotTungsten", 16)
                .inputLegacyOre("plateSteel", 16)
                .outputItem(item("mp_thruster_20_kerosene_dual"))
                .sourceOrder(284)
                .save(consumer, id("assembly_machine/mpt20kerodual"));

        GenericMachineRecipeBuilder.assembly("ass.mpt20kerotriple", 400, 100)
                .inputItem(item("seg_20"), 1)
                .inputLegacyOre("ntmpipeSteel", 6)
                .inputLegacyOre("ingotTungsten", 16)
                .inputLegacyOre("plateSteel", 16)
                .outputItem(item("mp_thruster_20_kerosene_triple"))
                .sourceOrder(285)
                .save(consumer, id("assembly_machine/mpt20kerotriple"));

        GenericMachineRecipeBuilder.assembly("ass.mpt20solid", 400, 100)
                .inputItem(item("seg_20"), 1)
                .inputLegacyOre("ntmpipeDuraSteel", 6)
                .inputLegacyOre("ingotTungsten", 16)
                .inputLegacyOre("plateSteel", 16)
                .outputItem(item("mp_thruster_20_solid"))
                .sourceOrder(286)
                .save(consumer, id("assembly_machine/mpt20solid"));

        GenericMachineRecipeBuilder.assembly("ass.mpt20solidmulti", 400, 100)
                .inputItem(item("seg_20"), 1)
                .inputLegacyOre("ntmpipeDuraSteel", 6)
                .inputLegacyOre("ingotTungsten", 16)
                .inputLegacyOre("plateSteel", 16)
                .outputItem(item("mp_thruster_20_solid_multi"))
                .sourceOrder(287)
                .save(consumer, id("assembly_machine/mpt20solidmulti"));

        GenericMachineRecipeBuilder.assembly("ass.mpt20solidmultier", 400, 100)
                .inputItem(item("seg_20"), 1)
                .inputLegacyOre("ntmpipeDuraSteel", 6)
                .inputLegacyOre("ingotTungsten", 16)
                .inputLegacyOre("plateSteel", 16)
                .outputItem(item("mp_thruster_20_solid_multier"))
                .sourceOrder(288)
                .save(consumer, id("assembly_machine/mpt20solidmultier"));

        GenericMachineRecipeBuilder.assembly("ass.mpf10kero", 100, 100)
                .inputItem(item("seg_10"), 2)
                .inputLegacyOre("plateAluminum", 12)
                .inputLegacyOre("plateSteel", 3)
                .outputItem(item("mp_fuselage_10_kerosene"))
                .sourceOrder(289)
                .save(consumer, id("assembly_machine/mpf10kero"));

        GenericMachineRecipeBuilder.assembly("ass.mpf10kerolong", 100, 100)
                .inputItem(item("seg_10"), 2)
                .inputLegacyOre("plateAluminum", 16)
                .inputLegacyOre("plateSteel", 6)
                .outputItem(item("mp_fuselage_10_kerosene"))
                .sourceOrder(290)
                .save(consumer, id("assembly_machine/mpf10kerolong"));

        GenericMachineRecipeBuilder.assembly("ass.mpf10solid", 100, 100)
                .inputItem(item("seg_10"), 2)
                .inputLegacyOre("plateTitanium", 12)
                .inputLegacyOre("plateSteel", 3)
                .outputItem(item("mp_fuselage_10_solid"))
                .sourceOrder(291)
                .save(consumer, id("assembly_machine/mpf10solid"));

        GenericMachineRecipeBuilder.assembly("ass.mpf10solidlong", 100, 100)
                .inputItem(item("seg_10"), 2)
                .inputLegacyOre("plateTitanium", 16)
                .inputLegacyOre("plateSteel", 6)
                .outputItem(item("mp_fuselage_10_solid"))
                .sourceOrder(292)
                .save(consumer, id("assembly_machine/mpf10solidlong"));

        GenericMachineRecipeBuilder.assembly("ass.mpf10xenon", 100, 100)
                .inputItem(item("seg_10"), 2)
                .inputLegacyOre("plateCopper", 12)
                .inputLegacyOre("plateSteel", 3)
                .outputItem(item("mp_fuselage_10_xenon"))
                .sourceOrder(293)
                .save(consumer, id("assembly_machine/mpf10xenon"));

        GenericMachineRecipeBuilder.assembly("ass.mpf1015kero", 200, 100)
                .inputItem(item("seg_10"), 1)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("plateAluminum", 24)
                .inputLegacyOre("plateSteel", 8)
                .outputItem(item("mp_fuselage_10_15_kerosene"))
                .sourceOrder(294)
                .save(consumer, id("assembly_machine/mpf1015kero"));

        GenericMachineRecipeBuilder.assembly("ass.mpf1015solid", 200, 100)
                .inputItem(item("seg_10"), 1)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("plateTitanium", 24)
                .inputLegacyOre("plateSteel", 8)
                .outputItem(item("mp_fuselage_10_15_solid"))
                .sourceOrder(295)
                .save(consumer, id("assembly_machine/mpf1015solid"));

        GenericMachineRecipeBuilder.assembly("ass.mpf1015hydro", 200, 100)
                .inputItem(item("seg_10"), 1)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("plateCopper", 24)
                .inputLegacyOre("plateSteel", 8)
                .outputItem(item("mp_fuselage_10_15_hydrogen"))
                .sourceOrder(296)
                .save(consumer, id("assembly_machine/mpf1015hydro"));

        GenericMachineRecipeBuilder.assembly("ass.mpf1015bf", 200, 100)
                .inputItem(item("seg_10"), 1)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("plateSaturnite", 24)
                .inputLegacyOre("plateSteel", 8)
                .outputItem(item("mp_fuselage_10_15_balefire"))
                .sourceOrder(297)
                .save(consumer, id("assembly_machine/mpf1015bf"));

        GenericMachineRecipeBuilder.assembly("ass.mpf15kero", 200, 100)
                .inputItem(item("seg_15"), 2)
                .inputLegacyOre("plateAluminum", 32)
                .inputLegacyOre("plateSteel", 12)
                .outputItem(item("mp_fuselage_15_kerosene"))
                .sourceOrder(298)
                .save(consumer, id("assembly_machine/mpf15kero"));

        GenericMachineRecipeBuilder.assembly("ass.mpf15solid", 200, 100)
                .inputItem(item("seg_15"), 2)
                .inputLegacyOre("plateTitanium", 32)
                .inputLegacyOre("plateSteel", 12)
                .outputItem(item("mp_fuselage_15_solid"))
                .sourceOrder(299)
                .save(consumer, id("assembly_machine/mpf15solid"));

        GenericMachineRecipeBuilder.assembly("ass.mpf15hydro", 200, 100)
                .inputItem(item("seg_15"), 2)
                .inputLegacyOre("plateCopper", 32)
                .inputLegacyOre("plateSteel", 12)
                .outputItem(item("mp_fuselage_15_hydrogen"))
                .sourceOrder(300)
                .save(consumer, id("assembly_machine/mpf15hydro"));

        GenericMachineRecipeBuilder.assembly("ass.mpf1520kero", 400, 100)
                .inputItem(item("seg_15"), 1)
                .inputItem(item("seg_20"), 1)
                .inputLegacyOre("plateAluminum", 48)
                .inputLegacyOre("plateSteel", 16)
                .outputItem(item("mp_fuselage_15_20_kerosene"))
                .sourceOrder(301)
                .save(consumer, id("assembly_machine/mpf1520kero"));

        GenericMachineRecipeBuilder.assembly("ass.mpf1520solid", 400, 100)
                .inputItem(item("seg_15"), 1)
                .inputItem(item("seg_20"), 1)
                .inputLegacyOre("plateTitanium", 48)
                .inputLegacyOre("plateSteel", 16)
                .outputItem(item("mp_fuselage_15_20_solid"))
                .sourceOrder(302)
                .save(consumer, id("assembly_machine/mpf1520solid"));

        GenericMachineRecipeBuilder.assembly("ass.mpw10he", 100, 100)
                .inputItem(item("seg_10"), 1)
                .inputLegacyOre("plateSteel", 6)
                .inputLegacyOre("ingotAnyHighExplosive", 3)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 1)
                .outputItem(item("mp_warhead_10_he"))
                .sourceOrder(303)
                .save(consumer, id("assembly_machine/mpw10he"));

        GenericMachineRecipeBuilder.assembly("ass.mpw10inc", 100, 100)
                .inputItem(item("seg_10"), 1)
                .inputLegacyOre("plateSteel", 6)
                .inputLegacyOre("ingotAnyHighExplosive", 2)
                .inputLegacyOre("dustRedPhosphorus", 6)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 1)
                .outputItem(item("mp_warhead_10_incendiary"))
                .sourceOrder(304)
                .save(consumer, id("assembly_machine/mpw10inc"));

        GenericMachineRecipeBuilder.assembly("ass.mpw10bus", 100, 100)
                .inputItem(item("seg_10"), 1)
                .inputLegacyOre("plateWeaponSteel", 6)
                .inputLegacyOre("ingotAnyHighExplosive", 6)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 2)
                .outputItem(item("mp_warhead_10_buster"))
                .sourceOrder(305)
                .save(consumer, id("assembly_machine/mpw10bus"));

        GenericMachineRecipeBuilder.assembly("ass.mpw10nukesmall", 200, 100)
                .inputItem(item("seg_10"), 1)
                .inputLegacyOre("plateWeaponSteel", 16)
                .inputLegacyOre("billetPu239", 2)
                .inputItem(item("neutron_reflector"), 4)
                .inputLegacyOre("ingotAnyHighExplosive", 4)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 1)
                .outputItem(item("mp_warhead_10_nuclear"))
                .sourceOrder(306)
                .save(consumer, id("assembly_machine/mpw10nukesmall"));

        GenericMachineRecipeBuilder.assembly("ass.mpw10nukelarge", 200, 100)
                .inputItem(item("seg_10"), 1)
                .inputLegacyOre("plateWeaponSteel", 16)
                .inputLegacyOre("billetPu239", 6)
                .inputItem(item("neutron_reflector"), 8)
                .inputLegacyOre("ingotAnyHighExplosive", 12)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 1)
                .outputItem(item("mp_warhead_10_nuclear_large"))
                .sourceOrder(307)
                .save(consumer, id("assembly_machine/mpw10nukelarge"));

        GenericMachineRecipeBuilder.assembly("ass.mpw10taint", 100, 100)
                .inputItem(item("seg_10"), 1)
                .inputLegacyOre("plateSteel", 12)
                .inputItem(block("det_cord"), 2)
                .inputItem(item("powder_magic"), 12)
                .inputFluidContainerLegacyOre(HbmFluids.WATZ, 1_000, 1)
                .outputItem(item("mp_warhead_10_taint"))
                .sourceOrder(308)
                .save(consumer, id("assembly_machine/mpw10taint"));

        GenericMachineRecipeBuilder.assembly("ass.mpw10cloud", 100, 100)
                .inputItem(item("seg_10"), 1)
                .inputLegacyOre("plateSteel", 12)
                .inputItem(block("det_cord"), 2)
                .inputItem(item("powder_magic"), 16)
                .outputItem(item("mp_warhead_10_cloud"))
                .sourceOrder(309)
                .save(consumer, id("assembly_machine/mpw10cloud"));

        GenericMachineRecipeBuilder.assembly("ass.mpw15he", 200, 100)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("plateSteel", 12)
                .inputLegacyOre("ingotAnyHighExplosive", 12)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 3)
                .outputItem(item("mp_warhead_15_he"))
                .sourceOrder(310)
                .save(consumer, id("assembly_machine/mpw15he"));

        GenericMachineRecipeBuilder.assembly("ass.mpw15inc", 200, 100)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("plateSteel", 12)
                .inputLegacyOre("ingotAnyHighExplosive", 8)
                .inputLegacyOre("dustRedPhosphorus", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 3)
                .outputItem(item("mp_warhead_15_incendiary"))
                .sourceOrder(311)
                .save(consumer, id("assembly_machine/mpw15inc"));

        GenericMachineRecipeBuilder.assembly("ass.mpw15nuke", 400, 100)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("plateWeaponSteel", 32)
                .inputLegacyOre("billetPu239", 12)
                .inputItem(item("neutron_reflector"), 12)
                .inputItem(item("ball_tatb"), 24)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 3)
                .outputItem(item("mp_warhead_15_nuclear"))
                .sourceOrder(312)
                .save(consumer, id("assembly_machine/mpw15nuke"));

        GenericMachineRecipeBuilder.assembly("ass.mpw15n2", 400, 100)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("plateWeaponSteel", 32)
                .inputItem(item("ball_tatb"), 32)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 8)
                .outputItem(item("mp_warhead_15_n2"))
                .sourceOrder(313)
                .save(consumer, id("assembly_machine/mpw15n2"));

        GenericMachineRecipeBuilder.assembly("ass.mpw15bf", 400, 100)
                .inputItem(item("seg_15"), 1)
                .inputLegacyOre("plateWeaponSteel", 32)
                .inputItem(item("neutron_reflector"), 16)
                .inputItem(item("powder_magic"), 8)
                .inputItem(item("egg_balefire_shard"), 4)
                .inputLegacyOre("ingotAnyHighExplosive", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 2)
                .outputItem(item("mp_warhead_15_balefire"))
                .sourceOrder(314)
                .save(consumer, id("assembly_machine/mpw15bf"));
    }

    private static void legacyTurretRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.assembly("ass.turretchekhov", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 1, 1)
                .inputLegacyOre("ingotSteel", 16)
                .inputItem(ModItems.MOTOR.get(), 3)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 1)
                .inputLegacyOre("ntmpipeSteel", 3)
                .inputLegacyOre("gunMechanismGunMetal", 3)
                .inputItem(block("crate_iron"), 1)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_chekhov"))
                .sourceOrder(231)
                .save(consumer, id("assembly_machine/turret_chekhov"));

        GenericMachineRecipeBuilder.assembly("ass.turretfriendly", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 1, 1)
                .inputLegacyOre("ingotSteel", 16)
                .inputItem(ModItems.MOTOR.get(), 3)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 1)
                .inputLegacyOre("ntmpipeSteel", 3)
                .inputLegacyOre("gunMechanismGunMetal", 1)
                .inputItem(block("crate_iron"), 1)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_friendly"))
                .sourceOrder(232)
                .save(consumer, id("assembly_machine/turret_friendly"));

        GenericMachineRecipeBuilder.assembly("ass.turretjeremy", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 1, 1)
                .inputLegacyOre("ingotSteel", 16)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 1)
                .inputItem(item("motor_desh"), 1)
                .inputLegacyOre("shellSteel", 3)
                .inputLegacyOre("gunMechanismWeaponSteel", 3)
                .inputItem(block("crate_steel"), 1)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_jeremy"))
                .sourceOrder(233)
                .save(consumer, id("assembly_machine/turret_jeremy"));

        GenericMachineRecipeBuilder.assembly("ass.turrettauon", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 8, 1)
                .inputLegacyOre("ingotSteel", 16)
                .inputLegacyOre("ingotAnyPlastic", 4)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 1)
                .inputItem(item("motor_desh"), 1)
                .inputLegacyOre("ingotCopper", 32)
                .inputLegacyOre("gunMechanismSaturnite", 3)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_tauon"))
                .sourceOrder(234)
                .save(consumer, id("assembly_machine/turret_tauon"));

        GenericMachineRecipeBuilder.assembly("ass.turretrichard", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 1, 1)
                .inputLegacyOre("ingotSteel", 16)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 1)
                .inputLegacyOre("ingotAnyPlastic", 2)
                .inputLegacyOre("shellSteel", 8)
                .inputLegacyOre("gunMechanismWeaponSteel", 3)
                .inputItem(block("crate_steel"), 1)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_richard"))
                .sourceOrder(235)
                .save(consumer, id("assembly_machine/turret_richard"));

        GenericMachineRecipeBuilder.assembly("ass.turrethoward", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 1, 1)
                .inputLegacyOre("ingotSteel", 24)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputItem(item("motor_desh"), 2)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 3)
                .inputLegacyOre("ntmpipeSteel", 10)
                .inputLegacyOre("gunMechanismWeaponSteel", 3)
                .inputItem(block("crate_steel"), 1)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_howard"))
                .sourceOrder(236)
                .save(consumer, id("assembly_machine/turret_howard"));

        GenericMachineRecipeBuilder.assembly("ass.maxwell", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 8, 1)
                .inputLegacyOre("ingotSteel", 24)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 2)
                .inputLegacyOre("ntmpipeSteel", 4)
                .inputLegacyOre("gunMechanismSaturnite", 3)
                .inputItem(item("magnetron"), 16)
                .inputLegacyOre("ingotAnyResistantAlloy", 8)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_maxwell"))
                .sourceOrder(237)
                .save(consumer, id("assembly_machine/turret_maxwell"));

        GenericMachineRecipeBuilder.assembly("ass.fritz", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 1, 1)
                .inputLegacyOre("ingotSteel", 16)
                .inputItem(ModItems.MOTOR.get(), 3)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 1)
                .inputLegacyOre("ntmpipeSteel", 8)
                .inputLegacyOre("gunMechanismGunMetal", 3)
                .inputItem(block("barrel_steel"), 1)
                .outputItem(block("turret_fritz"))
                .sourceOrder(238)
                .save(consumer, id("assembly_machine/turret_fritz"));

        GenericMachineRecipeBuilder.assembly("ass.arty", 1_200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 2, 1)
                .inputLegacyOre("ingotSteel", 64)
                .inputLegacyOre("ingotSteel", 64)
                .inputItem(item("motor_desh"), 5)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 3)
                .inputLegacyOre("ntmpipeSteel", 12)
                .inputLegacyOre("gunMechanismWeaponSteel", 16)
                .inputItem(block("machine_radar"), 1)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_arty"))
                .sourceOrder(239)
                .save(consumer, id("assembly_machine/turret_arty"));

        GenericMachineRecipeBuilder.assembly("ass.himars", 1_200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 2, 1)
                .inputLegacyOre("ingotSteel", 64)
                .inputLegacyOre("ingotSteel", 64)
                .inputLegacyOre("ingotAnyPlastic", 64)
                .inputItem(item("motor_desh"), 5)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 8)
                .inputLegacyOre("gunMechanismSaturnite", 8)
                .inputItem(block("machine_radar"), 1)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_himars"))
                .sourceOrder(240)
                .save(consumer, id("assembly_machine/turret_himars"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, block("turret_sentry"))
                .pattern("PPL")
                .pattern(" MD")
                .pattern(" SC")
                .define('P', forgeTag("plates/steel"))
                .define('M', ModItems.MOTOR.get())
                .define('L', forgeTag("gun_mechanisms/gun_metal"))
                .define('S', block("steel_scaffold"))
                .define('C', forgeTag("circuits/basic"))
                .define('D', item("crt_display"))
                .unlockedBy("has_crt_display", has(item("crt_display")))
                .save(consumer, id("weapon/turret_sentry"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block("machine_controller"))
                .pattern("TDT")
                .pattern("DCD")
                .pattern("TDT")
                .define('T', forgeTag("ingots/any_resistant_alloy"))
                .define('D', item("crt_display"))
                .define('C', forgeTag("circuits/advanced"))
                .unlockedBy("has_crt_display", has(item("crt_display")))
                .save(consumer, id("machines/machine_controller"));
    }

    private static void legacyUpgradeRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.assembly("ass.overdrive1", 200, 100)
                .inputItem(item("upgrade_speed_3"), 1)
                .inputItem(item("upgrade_effect_3"), 1)
                .inputLegacyOre("ingotSaturnite", 16)
                .inputLegacyOre("ingotAnyHardPlastic", 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 16)
                .outputItem(item("upgrade_overdrive_1"))
                .sourceOrder(189)
                .save(consumer, id("assembly_machine/overdrive1"));

        GenericMachineRecipeBuilder.assembly("ass.overdrive2", 600, 100)
                .inputItem(item("upgrade_overdrive_1"), 1)
                .inputItem(item("upgrade_speed_3"), 1)
                .inputItem(item("upgrade_effect_3"), 1)
                .inputLegacyOre("ingotSaturnite", 16)
                .inputItem(item("ingot_cft"), 8)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 10, 16)
                .outputItem(item("upgrade_overdrive_2"))
                .sourceOrder(190)
                .save(consumer, id("assembly_machine/overdrive2"));

        GenericMachineRecipeBuilder.assembly("ass.overdrive3", 1_200, 100)
                .inputItem(item("upgrade_overdrive_2"), 1)
                .inputItem(item("upgrade_speed_3"), 1)
                .inputItem(item("upgrade_effect_3"), 1)
                .inputLegacyOre("ingotAnyBismoidBronze", 16)
                .inputItem(item("ingot_cft"), 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 11, 16)
                .outputItem(item("upgrade_overdrive_3"))
                .sourceOrder(191)
                .save(consumer, id("assembly_machine/overdrive3"));
    }

    private static void legacyLandmineRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModBlocks.MINE_AP.get(), 4)
                .pattern("I")
                .pattern("C")
                .pattern("S")
                .define('I', item("plate_polymer"))
                .define('C', forgeTag("dusts/any_smokeless"))
                .define('S', forgeTag("ingots/steel"))
                .unlockedBy("has_any_smokeless", has(forgeTag("dusts/any_smokeless")))
                .save(consumer, id("weapon/mine_ap"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModBlocks.MINE_SHRAP.get())
                .pattern("L")
                .pattern("M")
                .define('L', item("pellet_buckshot"))
                .define('M', ModBlocks.MINE_AP.get())
                .unlockedBy("has_mine_ap", has(ModBlocks.MINE_AP.get()))
                .save(consumer, id("weapon/mine_shrap"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModBlocks.MINE_HE.get())
                .pattern(" C ")
                .pattern("PTP")
                .define('C', legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8))
                .define('P', forgeTag("plates/steel"))
                .define('T', forgeTag("ingots/any_high_explosive"))
                .unlockedBy("has_basic_circuit", has(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8)))
                .save(consumer, id("weapon/mine_he"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModBlocks.MINE_FAT.get())
                .pattern("CDN")
                .define('C', legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 7))
                .define('D', item("ducttape"))
                .define('N', item("ammo_standard_nuke_demo"))
                .unlockedBy("has_nuke_demo_ammo", has(item("ammo_standard_nuke_demo")))
                .save(consumer, id("weapon/mine_fat"));
    }

    /** Exact CraftingManager.java:385-399 colored-concrete recipes. */
    private static void coloredConcreteRecipes(Consumer<FinishedRecipe> consumer) {
        Block concreteColored = ModBlocks.legacyBlock("concrete_colored").get();
        String[] dyeNames = {
                "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
                "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
        };
        for (int meta = 0; meta < dyeNames.length; meta++) {
            String color = dyeNames[meta];
            shapedLegacyVariantRecipe(consumer, id("blocks/concrete_colored_" + color), concreteColored, 8, meta,
                    new String[] {"CCC", "CDC", "CCC"},
                    new Object[][] {
                            {'C', block("concrete_smooth")},
                            {'D', forgeTag("dyes/" + color)}
                    },
                    block("concrete_smooth"), "has_concrete_smooth");
        }

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, block("concrete_smooth"))
                .requires(concreteColored)
                .unlockedBy("has_colored_concrete", has(concreteColored))
                .save(consumer, id("blocks/concrete_smooth_from_colored"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, block("concrete_smooth"))
                .requires(ModBlocks.CONCRETE_COLORED_EXT.get())
                .unlockedBy("has_extended_colored_concrete", has(ModBlocks.CONCRETE_COLORED_EXT.get()))
                .save(consumer, id("blocks/concrete_smooth_from_colored_ext"));

        String[] extendedNames = {
                "machine", "machine_stripe", "indigo", "purple", "pink", "hazard", "sand", "bronze"
        };
        String[][] extendedDyes = {
                {"brown", "gray"}, {"brown", "black"}, {"blue", "purple"}, {"purple", "purple"},
                {"pink", "red"}, {"yellow", "black"}, {"yellow", "gray"}, {"orange", "brown"}
        };
        for (int meta = 0; meta < extendedNames.length; meta++) {
            shapedLegacyVariantRecipe(consumer, id("blocks/concrete_colored_ext_" + extendedNames[meta]),
                    ModBlocks.CONCRETE_COLORED_EXT.get(), 6, meta,
                    new String[] {"CCC", "1 2", "CCC"},
                    new Object[][] {
                            {'C', block("concrete_smooth")},
                            {'1', forgeTag("dyes/" + extendedDyes[meta][0])},
                            {'2', forgeTag("dyes/" + extendedDyes[meta][1])}
                    },
                    block("concrete_smooth"), "has_concrete_smooth");
        }
    }

    private static void legacyWeaponPartRecipes(Consumer<FinishedRecipe> consumer) {
        stockRecipe(consumer, "stock_wood", vanillaTag("planks"));
        gripRecipe(consumer, "grip_wood", vanillaTag("planks"));
        stockRecipe(consumer, "stock_polymer", forgeTag("ingots/polymer"));
        gripRecipe(consumer, "grip_polymer", forgeTag("ingots/polymer"));
        stockRecipe(consumer, "stock_bakelite", forgeTag("ingots/bakelite"));
        gripRecipe(consumer, "grip_bakelite", forgeTag("ingots/bakelite"));
        stockRecipe(consumer, "stock_pc", forgeTag("ingots/pc"));
        gripRecipe(consumer, "grip_pc", forgeTag("ingots/pc"));
        stockRecipe(consumer, "stock_pvc", forgeTag("ingots/pvc"));
        gripRecipe(consumer, "grip_pvc", forgeTag("ingots/pvc"));
        gripRecipe(consumer, "grip_rubber", forgeTag("ingots/rubber"));
        gripRecipe(consumer, "grip_ivory", Items.BONE);

        GenericMachineRecipeBuilder.assembly("ass.clusterpellets", 50, 100)
                .inputLegacyOre("plateSteel", 4)
                .inputLegacyOre("ingotAnyHighExplosive", 1)
                .outputItem(item("pellet_cluster"))
                .sourceOrder(194)
                .save(consumer, id("assembly_machine/clusterpellets"));

        GenericMachineRecipeBuilder.assembly("ass.buckshot", 50, 100)
                .inputLegacyOre("nuggetLead", 6)
                .outputItem(item("pellet_buckshot"))
                .sourceOrder(195)
                .save(consumer, id("assembly_machine/buckshot"));
    }

    private static void stockRecipe(Consumer<FinishedRecipe> consumer, String result, TagKey<Item> material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item(result))
                .pattern("WWW")
                .pattern("  W")
                .define('W', material)
                .unlockedBy("has_stock_material", has(material))
                .save(consumer, id("weapon/" + result));
    }

    private static void gripRecipe(Consumer<FinishedRecipe> consumer, String result, TagKey<Item> material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item(result))
                .pattern("W ")
                .pattern(" W")
                .pattern(" W")
                .define('W', material)
                .unlockedBy("has_grip_material", has(material))
                .save(consumer, id("weapon/" + result));
    }

    private static void gripRecipe(Consumer<FinishedRecipe> consumer, String result, ItemLike material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item(result))
                .pattern("W ")
                .pattern(" W")
                .pattern(" W")
                .define('W', material)
                .unlockedBy("has_grip_material", has(material))
                .save(consumer, id("weapon/" + result));
    }

    private static void legacyAmmunitionRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.assembly("ass.50bmgsm", 100, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.CASING, 3, 1)
                .inputLegacyOre("dustAnySmokeless", 6)
                .inputLegacyOre("ingotStarmetal", 3)
                .outputItem(new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.AMMO_STANDARD, 94).get(), 6))
                .pool(LegacyBlueprintPools.PREFIX_DISCOVER + "silverstorm")
                .sourceOrder(315)
                .save(consumer, id("assembly_machine/50bmgsm"));

        GenericMachineRecipeBuilder.assembly("ass.50bmgbypass", 100, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.CASING, 3, 2)
                .inputLegacyOre("dustAnySmokeless", 24)
                .inputItem(ModItems.ITEM_SECRET_SELENIUM_STEEL.get(), 1)
                .inputItem(ModItems.BLACK_DIAMOND.get(), 1)
                .outputItem(new ItemStack(ModItems.AMMO_SECRET_BMG50_BLACK.get(), 12))
                .pool(LegacyBlueprintPools.PREFIX_SECRET + "psalm")
                .sourceOrder(316)
                .save(consumer, id("assembly_machine/50bmgbypass"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("stick_dynamite"), 4)
                .pattern(" S ")
                .pattern("PDP")
                .pattern("PDP")
                .define('S', item("safety_fuse"))
                .define('P', Items.PAPER)
                .define('D', item("ball_dynamite"))
                .unlockedBy("has_dynamite", has(item("ball_dynamite")))
                .save(consumer, id("weapon/stick_dynamite"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, item("stick_dynamite_fishing"))
                .requires(item("stick_dynamite"), 3)
                .requires(Items.PAPER)
                .requires(forgeTag("any/tar"))
                .unlockedBy("has_stick_dynamite", has(item("stick_dynamite")))
                .save(consumer, id("weapon/stick_dynamite_fishing"));

        shapedExplosiveStick(consumer, "stick_tnt", block("det_cord"), item("ball_tnt"), "has_tnt", item("ball_tnt"));
        shapedExplosiveStick(consumer, "stick_semtex", block("det_cord"), item("ingot_semtex"),
                "has_semtex", item("ingot_semtex"));
        shapedExplosiveStick(consumer, "stick_c4", block("det_cord"), item("ingot_c4"), "has_c4", item("ingot_c4"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_shell_frag"), 4)
                .pattern("B")
                .pattern("P")
                .pattern("S")
                .define('B', forgeTag("bolts/steel"))
                .define('P', forgeTag("plates/aluminium"))
                .define('S', forgeTag("shells/steel"))
                .unlockedBy("has_steel_shell", has(forgeTag("shells/steel")))
                .save(consumer, id("weapon/grenade_shell_frag"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_shell_stick"), 4)
                .pattern("S")
                .pattern("B")
                .pattern("W")
                .define('S', forgeTag("shells/steel"))
                .define('B', forgeTag("bolts/steel"))
                .define('W', vanillaTag("planks"))
                .unlockedBy("has_steel_shell", has(forgeTag("shells/steel")))
                .save(consumer, id("weapon/grenade_shell_stick"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_shell_tech"), 4)
                .pattern("C")
                .pattern("M")
                .pattern("S")
                .define('C', legacyCircuit(8))
                .define('M', forgeTag("gun_mechanisms/weapon_steel"))
                .define('S', forgeTag("shells/weapon_steel"))
                .unlockedBy("has_weaponsteel_mechanism", has(forgeTag("gun_mechanisms/weapon_steel")))
                .save(consumer, id("weapon/grenade_shell_tech"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_shell_nuke"), 2)
                .pattern(" S ")
                .pattern("CMC")
                .pattern(" S ")
                .define('S', forgeTag("shells/weapon_steel"))
                .define('C', legacyCircuit(9))
                .define('M', forgeTag("gun_mechanisms/weapon_steel"))
                .unlockedBy("has_weaponsteel_mechanism", has(forgeTag("gun_mechanisms/weapon_steel")))
                .save(consumer, id("weapon/grenade_shell_nuke"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_fuze_s3"), 4)
                .pattern("S")
                .pattern("F")
                .define('S', forgeTag("bolts/steel"))
                .define('F', item("safety_fuse"))
                .unlockedBy("has_safety_fuse", has(item("safety_fuse")))
                .save(consumer, id("weapon/grenade_fuze_s3"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_fuze_s7"), 4)
                .pattern("S")
                .pattern("F")
                .pattern("F")
                .define('S', forgeTag("bolts/steel"))
                .define('F', item("safety_fuse"))
                .unlockedBy("has_safety_fuse", has(item("safety_fuse")))
                .save(consumer, id("weapon/grenade_fuze_s7"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_fuze_s15"), 4)
                .pattern(" S ")
                .pattern(" F ")
                .pattern("FFF")
                .define('S', forgeTag("bolts/steel"))
                .define('F', item("safety_fuse"))
                .unlockedBy("has_safety_fuse", has(item("safety_fuse")))
                .save(consumer, id("weapon/grenade_fuze_s15"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_fuze_impact"), 4)
                .pattern("C")
                .pattern("S")
                .pattern("F")
                .define('C', forgeTag("dusts/any_smokeless"))
                .define('S', forgeTag("bolts/steel"))
                .define('F', item("safety_fuse"))
                .unlockedBy("has_safety_fuse", has(item("safety_fuse")))
                .save(consumer, id("weapon/grenade_fuze_impact"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_fuze_airburst"), 4)
                .pattern("C")
                .pattern("S")
                .pattern("F")
                .define('C', legacyCircuit(0))
                .define('S', forgeTag("bolts/steel"))
                .define('F', item("safety_fuse"))
                .unlockedBy("has_safety_fuse", has(item("safety_fuse")))
                .save(consumer, id("weapon/grenade_fuze_airburst"));

        JsonObject grenadeCrafting = new JsonObject();
        grenadeCrafting.addProperty("type", id("grenade_crafting").toString());
        grenadeCrafting.addProperty("category", "equipment");
        consumer.accept(finishedCompatRecipe(id("weapon/grenade_crafting"), grenadeCrafting));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_filling_powder"), 4)
                .pattern("F")
                .pattern("I")
                .pattern("F")
                .define('F', Items.GUNPOWDER)
                .define('I', item("plate_polymer"))
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
                .save(consumer, id("weapon/grenade_filling_powder"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_filling_he"), 4)
                .pattern("F")
                .pattern("I")
                .pattern("F")
                .define('F', item("ball_dynamite"))
                .define('I', item("plate_polymer"))
                .unlockedBy("has_ball_dynamite", has(item("ball_dynamite")))
                .save(consumer, id("weapon/grenade_filling_he"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_filling_demo"), 4)
                .pattern("F")
                .pattern("I")
                .pattern("F")
                .define('F', forgeTag("ingots/any_high_explosive"))
                .define('I', item("plate_polymer"))
                .unlockedBy("has_any_high_explosive", has(forgeTag("ingots/any_high_explosive")))
                .save(consumer, id("weapon/grenade_filling_demo"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_filling_inc"), 4)
                .pattern("F")
                .pattern("I")
                .pattern("F")
                .define('F', forgeTag("dusts/red_phosphorus"))
                .define('I', item("plate_polymer"))
                .unlockedBy("has_red_phosphorus", has(forgeTag("dusts/red_phosphorus")))
                .save(consumer, id("weapon/grenade_filling_inc"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_filling_wp"), 4)
                .pattern("F")
                .pattern("I")
                .pattern("F")
                .define('F', forgeTag("ingots/white_phosphorus"))
                .define('I', item("plate_polymer"))
                .unlockedBy("has_white_phosphorus", has(forgeTag("ingots/white_phosphorus")))
                .save(consumer, id("weapon/grenade_filling_wp"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_filling_cluster"), 4)
                .pattern("F")
                .pattern("I")
                .pattern("F")
                .define('F', item("pellet_cluster"))
                .define('I', item("plate_polymer"))
                .unlockedBy("has_cluster_pellet", has(item("pellet_cluster")))
                .save(consumer, id("weapon/grenade_filling_cluster"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_filling_cluster_heavy"))
                .pattern("F")
                .pattern("I")
                .pattern("F")
                .define('F', item("pellet_cluster"))
                .define('I', forgeTag("plates/weapon_steel"))
                .unlockedBy("has_cluster_pellet", has(item("pellet_cluster")))
                .save(consumer, id("weapon/grenade_filling_cluster_heavy"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_filling_emp"), 4)
                .pattern(" C ")
                .pattern("KWK")
                .define('C', legacyCircuit(8))
                .define('K', item("coil_gold"))
                .define('W', forgeTag("plates/weapon_steel"))
                .unlockedBy("has_gold_coil", has(item("coil_gold")))
                .save(consumer, id("weapon/grenade_filling_emp"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_filling_plasma"), 4)
                .pattern(" C ")
                .pattern("KWK")
                .define('C', legacyCircuit(10))
                .define('K', item("cell_tritium"))
                .define('W', forgeTag("plates/weapon_steel"))
                .unlockedBy("has_tritium_cell", has(item("cell_tritium")))
                .save(consumer, id("weapon/grenade_filling_plasma"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_filling_laser"), 4)
                .pattern(" C ")
                .pattern("KWK")
                .define('C', legacyCircuit(18))
                .define('K', item("crystal_redstone"))
                .define('W', forgeTag("plates/weapon_steel"))
                .unlockedBy("has_redstone_crystal", has(item("crystal_redstone")))
                .save(consumer, id("weapon/grenade_filling_laser"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_filling_nuclear"))
                .pattern(" T ")
                .pattern("CPC")
                .pattern(" T ")
                .define('T', item("ball_tatb"))
                .define('C', forgeTag("plates/weapon_steel"))
                .define('P', forgeTag("nuggets/pu239"))
                .unlockedBy("has_pu239_nugget", has(forgeTag("nuggets/pu239")))
                .save(consumer, id("weapon/grenade_filling_nuclear"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_filling_nuclear_demo"))
                .pattern("TPT")
                .pattern("CPC")
                .pattern("TPT")
                .define('T', item("ball_tatb"))
                .define('C', item("neutron_reflector"))
                .define('P', forgeTag("nuggets/pu239"))
                .unlockedBy("has_pu239_nugget", has(forgeTag("nuggets/pu239")))
                .save(consumer, id("weapon/grenade_filling_nuclear_demo"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_filling_schrab"))
                .pattern("BCB")
                .pattern("TST")
                .pattern("BCB")
                .define('B', forgeTag("cast_plates/combine_steel"))
                .define('C', legacyCircuit(13))
                .define('T', item("ball_tatb"))
                .define('S', item("cell_sas3"))
                .unlockedBy("has_sas3_cell", has(item("cell_sas3")))
                .save(consumer, id("weapon/grenade_filling_schrab"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_extra_glue"))
                .pattern(" P ")
                .pattern("PSP")
                .pattern(" P ")
                .define('P', Items.PAPER)
                .define('S', Items.SLIME_BALL)
                .unlockedBy("has_slime_ball", has(Items.SLIME_BALL))
                .save(consumer, id("weapon/grenade_extra_glue"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_extra_proxy_fuze"))
                .pattern("C")
                .pattern("F")
                .define('C', legacyCircuit(5))
                .define('F', item("safety_fuse"))
                .unlockedBy("has_safety_fuse", has(item("safety_fuse")))
                .save(consumer, id("weapon/grenade_extra_proxy_fuze"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, item("grenade_extra_frag_sleeve"))
                .pattern("BBB")
                .pattern(" T ")
                .pattern("BBB")
                .define('B', forgeTag("bolts/steel"))
                .define('T', item("ducttape"))
                .unlockedBy("has_steel_bolt", has(forgeTag("bolts/steel")))
                .save(consumer, id("weapon/grenade_extra_frag_sleeve"));

        GenericMachineRecipeBuilder.assembly("ass.nitra", 200, 500)
                .inputItem(item("nitra"), 1)
                .outputOneOf(
                        GenericMachineRecipeBuilder.WeightedOutput.of(item("casing_shotshell"), 20),
                        GenericMachineRecipeBuilder.WeightedOutput.of(item("casing_buckshot"), 10),
                        GenericMachineRecipeBuilder.WeightedOutput.of(new ItemStack(item("casing_small"), 2), 20),
                        GenericMachineRecipeBuilder.WeightedOutput.of(item("casing_large"), 10),
                        GenericMachineRecipeBuilder.WeightedOutput.of(Items.GUNPOWDER, 30),
                        GenericMachineRecipeBuilder.WeightedOutput.of(item("cordite"), 20),
                        GenericMachineRecipeBuilder.WeightedOutput.of(item("ballistite"), 20),
                        GenericMachineRecipeBuilder.WeightedOutput.of(item("grenade_shell_frag"), 5),
                        GenericMachineRecipeBuilder.WeightedOutput.of(item("grenade_shell_stick"), 5),
                        GenericMachineRecipeBuilder.WeightedOutput.of(item("grenade_fuze_s3"), 10))
                .icon(item("nitra"))
                .customLocalization()
                .sourceOrder(332)
                .save(consumer, id("assembly_machine/nitra"));
    }

    private static void legacyBombPartRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, block("emp_bomb"))
                .pattern("LML")
                .pattern("LCL")
                .pattern("LML")
                .define('L', forgeTag("plates/lead"))
                .define('M', item("magnetron"))
                .define('C', item("circuit_advanced"))
                .unlockedBy("has_magnetron", has(item("magnetron")))
                .save(consumer, id("weapon/emp_bomb"));

        GenericMachineRecipeBuilder.assembly("ass.explosivelenses1", 400, 100)
                .inputLegacyOre("plateAluminum", 8)
                .inputItem(block("det_cord"), 8)
                .inputLegacyOre("plateSaturnite", 2)
                .inputLegacyOre("ingotAnyHighExplosive", 20)
                .inputLegacyOre("ingotAnyPlastic", 4)
                .outputItem(item("early_explosive_lenses"))
                .sourceOrder(211)
                .save(consumer, id("assembly_machine/explosivelenses1"));

        GenericMachineRecipeBuilder.assembly("ass.explosivelenses2", 400, 100)
                .inputLegacyOre("plateAluminum", 8)
                .inputLegacyOre("ingotAnyPlasticExplosive", 4)
                .inputItem(item("neutron_reflector"), 2)
                .inputItem(item("ball_tatb"), 16)
                .inputLegacyOre("ingotRubber", 2)
                .outputItem(item("explosive_lenses"))
                .sourceOrder(212)
                .save(consumer, id("assembly_machine/explosivelenses2"));

        GenericMachineRecipeBuilder.assembly("ass.wiring", 200, 100)
                .inputLegacyOre("wireFineGold", 24)
                .outputItem(item("gadget_wireing"))
                .sourceOrder(213)
                .save(consumer, id("assembly_machine/wiring"));

        GenericMachineRecipeBuilder.assembly("ass.core1", 1_200, 100)
                .inputLegacyOre("nuggetPu239", 7)
                .inputLegacyOre("nuggetU238", 3)
                .outputItem(item("gadget_core"))
                .sourceOrder(214)
                .save(consumer, id("assembly_machine/core1"));

        GenericMachineRecipeBuilder.assembly("ass.boyshield", 200, 100)
                .inputItem(item("neutron_reflector"), 12)
                .inputLegacyOre("plateSteel", 4)
                .outputItem(item("boy_shielding"))
                .sourceOrder(215)
                .save(consumer, id("assembly_machine/boyshield"));

        GenericMachineRecipeBuilder.assembly("ass.boytarget", 200, 100)
                .inputLegacyOre("nuggetU235", 18)
                .outputItem(item("boy_target"))
                .sourceOrder(216)
                .save(consumer, id("assembly_machine/boytarget"));

        GenericMachineRecipeBuilder.assembly("ass.boybullet", 200, 100)
                .inputLegacyOre("nuggetU235", 9)
                .outputItem(item("boy_bullet"))
                .sourceOrder(217)
                .save(consumer, id("assembly_machine/boybullet"));

        GenericMachineRecipeBuilder.assembly("ass.boypropellant", 200, 100)
                .inputItem(item("cordite"), 8)
                .inputLegacyOre("plateIron", 8)
                .inputLegacyOre("plateAluminum", 4)
                .inputLegacyOre("wireFineMingrade", 4)
                .outputItem(item("boy_propellant"))
                .sourceOrder(218)
                .save(consumer, id("assembly_machine/boypropellant"));

        GenericMachineRecipeBuilder.assembly("ass.boyigniter", 200, 100)
                .inputLegacyOre("shellAluminum", 3)
                .inputLegacyOre("plateCastDuraSteel", 1)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 1)
                .inputLegacyOre("wireFineMingrade", 16)
                .outputItem(item("boy_igniter"))
                .sourceOrder(219)
                .save(consumer, id("assembly_machine/boyigniter"));

        GenericMachineRecipeBuilder.assembly("ass.manigniter", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 3)
                .inputLegacyOre("wireFineGold", 24)
                .outputItem(item("man_igniter"))
                .sourceOrder(220)
                .save(consumer, id("assembly_machine/manigniter"));

        GenericMachineRecipeBuilder.assembly("ass.mancore", 1_200, 100)
                .inputLegacyOre("nuggetPu239", 8)
                .inputLegacyOre("nuggetBeryllium", 2)
                .outputItem(item("man_core"))
                .sourceOrder(221)
                .save(consumer, id("assembly_machine/mancore"));

        GenericMachineRecipeBuilder.assembly("ass.mikecore", 1_200, 100)
                .inputLegacyOre("nuggetU238", 24)
                .inputLegacyOre("plateLead", 6)
                .outputItem(item("mike_core"))
                .sourceOrder(222)
                .save(consumer, id("assembly_machine/mikecore"));

        GenericMachineRecipeBuilder.assembly("ass.mikedeut", 600, 100)
                .inputLegacyOre("plateWeaponSteel", 16)
                .inputLegacyOre("plateTitanium", 16)
                .inputFluid(HbmFluids.DEUTERIUM, 10_000)
                .outputItem(item("mike_deut"))
                .sourceOrder(223)
                .save(consumer, id("assembly_machine/mikedeut"));

        GenericMachineRecipeBuilder.assembly("ass.mikecooler", 300, 100)
                .inputLegacyOre("plateDuraSteel", 8)
                .inputItem(item("coil_copper"), 5)
                .inputItem(item("coil_tungsten"), 5)
                .inputItem(item("motor"), 2)
                .outputItem(item("mike_cooling_unit"))
                .sourceOrder(224)
                .save(consumer, id("assembly_machine/mikecooler"));

        GenericMachineRecipeBuilder.assembly("ass.fleijaigniter", 200, 100)
                .inputLegacyOre("plateTitanium", 6)
                .inputLegacyOre("wireFineSchrabidium", 2)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 1)
                .outputItem(item("fleija_igniter"))
                .sourceOrder(225)
                .save(consumer, id("assembly_machine/fleijaigniter"));

        GenericMachineRecipeBuilder.assembly("ass.fleijacore", 600, 100)
                .inputLegacyOre("nuggetU235", 8)
                .inputLegacyOre("nuggetNp237", 2)
                .inputLegacyOre("nuggetBeryllium", 4)
                .inputItem(item("coil_copper"), 2)
                .outputItem(item("fleija_core"))
                .sourceOrder(226)
                .save(consumer, id("assembly_machine/fleijacore"));

        GenericMachineRecipeBuilder.assembly("ass.fleijacharge", 300, 100)
                .inputLegacyOre("ingotAnyHighExplosive", 3)
                .inputLegacyOre("plateSchrabidium", 8)
                .outputItem(item("fleija_propellant"))
                .sourceOrder(227)
                .save(consumer, id("assembly_machine/fleijacharge"));

        GenericMachineRecipeBuilder.assembly("ass.soliniumigniter", 200, 100)
                .inputLegacyOre("plateTitanium", 4)
                .inputLegacyOre("wireFineMingrade", 8)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 1)
                .inputItem(item("coil_gold"), 1)
                .outputItem(item("solinium_igniter"))
                .sourceOrder(228)
                .save(consumer, id("assembly_machine/soliniumigniter"));

        GenericMachineRecipeBuilder.assembly("ass.soliniumcore", 600, 100)
                .inputLegacyOre("nuggetSolinium", 9)
                .inputLegacyOre("nuggetEuphemium", 1)
                .outputItem(item("solinium_core"))
                .sourceOrder(229)
                .save(consumer, id("assembly_machine/soliniumcore"));

        GenericMachineRecipeBuilder.assembly("ass.soliniumcharge", 300, 100)
                .inputLegacyOre("ingotAnyHighExplosive", 3)
                .inputItem(item("neutron_reflector"), 2)
                .inputItem(item("plate_polymer"), 6)
                .inputLegacyOre("wireFineTungsten", 6)
                .inputItem(item("biomass_compressed"), 4)
                .outputItem(item("solinium_propellant"))
                .sourceOrder(230)
                .save(consumer, id("assembly_machine/soliniumcharge"));
    }

    private static void legacyStandardMissileRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.assembly("ass.missileassembly", 200, 100)
                .inputLegacyOre("shellAluminum", 2)
                .inputLegacyOre("shellTitanium", 4)
                .inputLegacyOre("ingotAnyPlastic", 8)
                .inputItem(item("rocket_fuel"), 8)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 1)
                .outputItem(item("missile_assembly"))
                .sourceOrder(249)
                .save(consumer, id("assembly_machine/missileassembly"));

        GenericMachineRecipeBuilder.assembly("ass.warheadhe1", 100, 100)
                .inputLegacyOre("plateTitanium", 4)
                .inputItem(item("ball_dynamite"), 2)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 5, 1)
                .outputItem(item("warhead_generic_small"))
                .sourceOrder(250)
                .save(consumer, id("assembly_machine/warheadhe1"));

        GenericMachineRecipeBuilder.assembly("ass.warheadhe2", 200, 100)
                .inputLegacyOre("plateTitanium", 8)
                .inputLegacyOre("ingotAnyHighExplosive", 4)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 1)
                .outputItem(item("warhead_generic_medium"))
                .sourceOrder(251)
                .save(consumer, id("assembly_machine/warheadhe2"));

        GenericMachineRecipeBuilder.assembly("ass.warheadhe3", 400, 100)
                .inputLegacyOre("plateTitanium", 16)
                .inputLegacyOre("ingotAnyHighExplosive", 8)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 1)
                .outputItem(item("warhead_generic_large"))
                .sourceOrder(252)
                .save(consumer, id("assembly_machine/warheadhe3"));

        GenericMachineRecipeBuilder.assembly("ass.warheadinc1", 100, 100)
                .inputItem(item("warhead_generic_small"), 1)
                .inputLegacyOre("dustRedPhosphorus", 2)
                .outputItem(item("warhead_incendiary_small"))
                .sourceOrder(253)
                .save(consumer, id("assembly_machine/warheadinc1"));

        GenericMachineRecipeBuilder.assembly("ass.warheadinc2", 200, 100)
                .inputItem(item("warhead_generic_medium"), 1)
                .inputLegacyOre("dustRedPhosphorus", 4)
                .outputItem(item("warhead_incendiary_medium"))
                .sourceOrder(254)
                .save(consumer, id("assembly_machine/warheadinc2"));

        GenericMachineRecipeBuilder.assembly("ass.warheadinc3", 400, 100)
                .inputItem(item("warhead_generic_large"), 1)
                .inputLegacyOre("dustRedPhosphorus", 8)
                .outputItem(item("warhead_incendiary_large"))
                .sourceOrder(255)
                .save(consumer, id("assembly_machine/warheadinc3"));

        GenericMachineRecipeBuilder.assembly("ass.warheadcl1", 100, 100)
                .inputItem(item("warhead_generic_small"), 1)
                .inputItem(item("pellet_cluster"), 2)
                .outputItem(item("warhead_cluster_small"))
                .sourceOrder(256)
                .save(consumer, id("assembly_machine/warheadcl1"));

        GenericMachineRecipeBuilder.assembly("ass.warheadcl2", 200, 100)
                .inputItem(item("warhead_generic_medium"), 1)
                .inputItem(item("pellet_cluster"), 4)
                .outputItem(item("warhead_cluster_medium"))
                .sourceOrder(257)
                .save(consumer, id("assembly_machine/warheadcl2"));

        GenericMachineRecipeBuilder.assembly("ass.warheadcl3", 400, 100)
                .inputItem(item("warhead_generic_large"), 1)
                .inputItem(item("pellet_cluster"), 8)
                .outputItem(item("warhead_cluster_large"))
                .sourceOrder(258)
                .save(consumer, id("assembly_machine/warheadcl3"));

        GenericMachineRecipeBuilder.assembly("ass.warheadbb1", 100, 100)
                .inputItem(item("warhead_generic_small"), 1)
                .inputLegacyOre("ingotAnyHighExplosive", 2)
                .outputItem(item("warhead_buster_small"))
                .sourceOrder(259)
                .save(consumer, id("assembly_machine/warheadbb1"));

        GenericMachineRecipeBuilder.assembly("ass.warheadbb2", 200, 100)
                .inputItem(item("warhead_generic_medium"), 1)
                .inputLegacyOre("ingotAnyHighExplosive", 4)
                .outputItem(item("warhead_buster_medium"))
                .sourceOrder(260)
                .save(consumer, id("assembly_machine/warheadbb2"));

        GenericMachineRecipeBuilder.assembly("ass.warheadbb3", 400, 100)
                .inputItem(item("warhead_generic_large"), 1)
                .inputLegacyOre("ingotAnyHighExplosive", 8)
                .outputItem(item("warhead_buster_large"))
                .sourceOrder(261)
                .save(consumer, id("assembly_machine/warheadbb3"));

        GenericMachineRecipeBuilder.assembly("ass.warheadnuke", 400, 100)
                .inputLegacyOre("plateCastTitanium", 12)
                .inputLegacyOre("plateCastLead", 6)
                .inputLegacyOre("billetU235", 6)
                .inputItem(item("cordite"), 12)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 1)
                .outputItem(item("warhead_nuclear"))
                .sourceOrder(262)
                .save(consumer, id("assembly_machine/warheadnuke"));

        GenericMachineRecipeBuilder.assembly("ass.warheadthermonuke", 600, 100)
                .inputLegacyOre("plateCastTitanium", 12)
                .inputLegacyOre("plateCastLead", 6)
                .inputLegacyOre("billetPu239", 8)
                .inputItem(item("ball_tatb"), 12)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 14, 2)
                .inputFluid(HbmFluids.DEUTERIUM, 4_000)
                .outputItem(item("warhead_mirv"))
                .sourceOrder(263)
                .save(consumer, id("assembly_machine/warheadthermonuke"));

        GenericMachineRecipeBuilder.assembly("ass.warheadvolcano", 600, 100)
                .inputLegacyOre("plateCastTitanium", 12)
                .inputLegacyOre("plateCastSteel", 6)
                .inputItem(block("det_nuke"), 3)
                .inputLegacyOre("blockU238", 24)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 10, 5)
                .outputItem(item("warhead_volcano"))
                .sourceOrder(264)
                .save(consumer, id("assembly_machine/warheadvolcano"));

        GenericMachineRecipeBuilder.assembly("ass.thrusternerva", 600, 100)
                .inputLegacyOre("ingotDuraSteel", 32)
                .inputLegacyOre("ingotBoron", 8)
                .inputLegacyOre("plateLead", 16)
                .inputLegacyOre("ntmpipeSteel", 4)
                .outputItem(item("thruster_nuclear"))
                .sourceOrder(265)
                .save(consumer, id("assembly_machine/thrusternerva"));

        GenericMachineRecipeBuilder.assembly("ass.stealthmissile", 1_200, 100)
                .inputLegacyOre("plateTitanium", 20)
                .inputLegacyOre("plateAluminum", 20)
                .inputLegacyOre("dyeBlack", 16)
                .inputLegacyOre("ingotAnyHardPlastic", 16)
                .inputLegacyOre("ingotAnyHighExplosive", 4)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 4)
                .inputLegacyOre("boltSteel", 32)
                .outputItem(item("missile_stealth"))
                .sourceOrder(266)
                .save(consumer, id("assembly_machine/stealthmissile"));

        GenericMachineRecipeBuilder.assembly("ass.shuttlemissile", 200, 100)
                .inputItem(item("missile_generic"), 1)
                .inputItem(item("missile_strong"), 1)
                .inputLegacyOre("dyeOrange", 5)
                .inputFluidContainerLegacyOre(HbmFluids.GASOLINE_LEADED, 1_000, 24)
                .inputLegacyOre("ingotFiberglass", 12)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 3)
                .inputLegacyOre("ingotAnyPlasticExplosive", 8)
                .inputLegacyOre("paneGlass", 6)
                .inputLegacyOre("plateSteel", 4)
                .outputItem(item("missile_shuttle"))
                .sourceOrder(267)
                .save(consumer, id("assembly_machine/shuttlemissile"));

        anvilConstructionRecipe(consumer, id("anvil_construction/missile/doomsday"), 5,
                new ItemStack(item("missile_doomsday")),
                HbmIngredient.of(item("missile_doomsday_rusted"), 1),
                HbmIngredient.of(forgeTag("ingots/any_hardplastic"), 8),
                HbmIngredient.of(forgeTag("welded_plates/aluminium"), 2),
                HbmIngredient.of(forgeTag("billets/pu239"), 3));
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    private static HbmIngredient legacyHbmItem(String legacyPath) {
        return legacyHbmItem(legacyPath, item(legacyPath));
    }

    private static HbmIngredient legacyHbmItem(String legacyPath, ItemLike item) {
        return legacyItem("hbm", legacyPath, item);
    }

    private static HbmIngredient legacyItem(String namespace, String path, ItemLike item) {
        return legacyItem(namespace, path, 0, item);
    }

    private static HbmIngredient legacyItem(String namespace, String path, int legacyMeta, ItemLike item) {
        return HbmIngredient.legacyItem(new ResourceLocation(namespace, path), legacyMeta, new ItemStack(item));
    }

    private static Path projectRoot() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        return "run-data".equals(current.getFileName().toString()) && current.getParent() != null
                ? current.getParent()
                : current;
    }

    private static JsonObject fluidStackJson(FluidType fluid, int amount) {
        JsonObject object = new JsonObject();
        object.addProperty("fluid", id(fluid.toPath()).toString());
        object.addProperty("amount", amount);
        return object;
    }

    private static JsonObject fluidStackJson(HbmFluidStack stack) {
        JsonObject object = fluidStackJson(stack.type(), stack.amount());
        if (stack.pressure() != 0) {
            object.addProperty("pressure", stack.pressure());
        }
        return object;
    }

    private static JsonObject itemStackJson(ItemStack stack) {
        JsonObject object = new JsonObject();
        object.addProperty("item", HbmRegistryUtil.itemKey(stack.getItem()).toString());
        if (stack.getCount() > 1) {
            object.addProperty("count", stack.getCount());
        }
        if (stack.hasTag() && !stack.getTag().isEmpty()) {
            object.addProperty("nbt", stack.getTag().toString());
        }
        return object;
    }

    private static ItemStack requireOilItemOutput(ItemStack stack, String recipeName) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalStateException("Oil processing default recipe " + recipeName + " has no item output");
        }
        return stack;
    }

    private static TagKey<Item> forgeTag(String path) {
        return HbmItemTagsProvider.forgeItemTag(path);
    }

    private static TagKey<Item> legacyOreTag(String legacyOreName) {
        return HbmItemTagsProvider.legacyOreItemTag(legacyOreName);
    }

    private static TagKey<Item> vanillaTag(String path) {
        return TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", path));
    }

    private static final class SolderingStationRecipeBuilder {
        private final ItemStack output;
        private final int duration;
        private final long consumption;
        private final JsonArray toppings = new JsonArray();
        private final JsonArray pcb = new JsonArray();
        private final JsonArray solder = new JsonArray();
        @Nullable
        private JsonObject fluid;

        private SolderingStationRecipeBuilder(ItemStack output, int duration, long consumption) {
            this.output = output.copy();
            this.duration = duration;
            this.consumption = consumption;
        }

        private static SolderingStationRecipeBuilder soldering(ItemLike output, int duration, long consumption) {
            return new SolderingStationRecipeBuilder(new ItemStack(output), duration, consumption);
        }

        private SolderingStationRecipeBuilder toppingItem(String itemName, int count) {
            toppings.add(HbmIngredient.of(item(itemName), count).toJson());
            return this;
        }

        private SolderingStationRecipeBuilder pcbItem(String itemName, int count) {
            pcb.add(HbmIngredient.of(item(itemName), count).toJson());
            return this;
        }

        private SolderingStationRecipeBuilder pcbItemWithLegacyOre(ItemLike item, int count, String legacyOre) {
            JsonObject input = HbmIngredient.of(item, count).toJson();
            input.addProperty("legacy_ore", legacyOre);
            pcb.add(input);
            return this;
        }

        private SolderingStationRecipeBuilder pcbLegacyOre(String legacyOre, int count) {
            pcb.add(HbmIngredient.legacyOre(legacyOre, count).toJson());
            return this;
        }

        private SolderingStationRecipeBuilder solderLegacyOre(String legacyOre, int count) {
            solder.add(HbmIngredient.legacyOre(legacyOre, count).toJson());
            return this;
        }

        private SolderingStationRecipeBuilder fluid(FluidType type, int amount) {
            JsonObject object = new JsonObject();
            object.addProperty("fluid", type.getName());
            object.addProperty("amount", amount);
            fluid = object;
            return this;
        }

        private void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId, int sourceOrder) {
            consumer.accept(new FinishedRecipe() {
                @Override
                public void serializeRecipeData(JsonObject json) {
                    json.add("toppings", toppings);
                    json.add("pcb", pcb);
                    json.add("solder", solder);
                    if (fluid != null) {
                        json.add("fluid", fluid);
                    }
                    json.add("output", HbmItemOutput.of(output).toJson());
                    json.addProperty("duration", duration);
                    json.addProperty("consumption", consumption);
                    json.addProperty("source_order", sourceOrder);
                }

                @Override
                public ResourceLocation getId() {
                    return recipeId;
                }

                @Override
                public RecipeSerializer<?> getType() {
                    return HbmRegistryUtil.recipeSerializer(id("soldering_station")).orElseThrow();
                }

                @Nullable
                @Override
                public JsonObject serializeAdvancement() {
                    return null;
                }

                @Nullable
                @Override
                public ResourceLocation getAdvancementId() {
                    return null;
                }
            });
        }
    }

    private static final class OutgasserRecipeBuilder {
        private final HbmIngredient input;
        @Nullable
        private ItemStack outputItem;
        @Nullable
        private JsonObject outputFluid;

        private OutgasserRecipeBuilder(HbmIngredient input) {
            this.input = input;
        }

        private static OutgasserRecipeBuilder outgasser(HbmIngredient input) {
            return new OutgasserRecipeBuilder(input);
        }

        private OutgasserRecipeBuilder outputItem(ItemLike item) {
            return outputItem(new ItemStack(item));
        }

        private OutgasserRecipeBuilder outputItem(ItemStack stack) {
            outputItem = stack.copy();
            return this;
        }

        private OutgasserRecipeBuilder outputFluid(FluidType fluid, int amount) {
            JsonObject object = new JsonObject();
            object.addProperty("fluid", fluid.getName());
            object.addProperty("amount", amount);
            outputFluid = object;
            return this;
        }

        private void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
            save(consumer, recipeId, -1);
        }

        private void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId, int sourceOrder) {
            if ((outputItem == null || outputItem.isEmpty()) && outputFluid == null) {
                throw new IllegalStateException("HBM outgasser recipe has no output: " + recipeId);
            }
            consumer.accept(new FinishedRecipe() {
                @Override
                public void serializeRecipeData(JsonObject json) {
                    if (sourceOrder >= 0) {
                        json.addProperty("source_order", sourceOrder);
                    }
                    json.add("input", input.toJson());
                    if (outputItem != null && !outputItem.isEmpty()) {
                        json.add("solid_output", itemStackJson(outputItem));
                    }
                    if (outputFluid != null) {
                        json.add("fluid_output", outputFluid);
                    }
                }

                @Override
                public ResourceLocation getId() {
                    return recipeId;
                }

                @Override
                public RecipeSerializer<?> getType() {
                    return HbmRegistryUtil.recipeSerializer(id("outgasser")).orElseThrow();
                }

                @Nullable
                @Override
                public JsonObject serializeAdvancement() {
                    return null;
                }

                @Nullable
                @Override
                public ResourceLocation getAdvancementId() {
                    return null;
                }
            });
        }

        private static JsonObject itemStackJson(ItemStack stack) {
            JsonObject object = new JsonObject();
            object.addProperty("item", HbmRegistryUtil.itemKey(stack.getItem()).toString());
            if (stack.getCount() > 1) {
                object.addProperty("count", stack.getCount());
            }
            return object;
        }
    }

    private static final class GenericMachineRecipeBuilder {
        private final ResourceLocation serializerId;
        private final GenericMachineRecipe.Machine machine;
        private final String internalName;
        private final int duration;
        private final long power;
        private final List<HbmIngredient> itemInputEntries = new ArrayList<>();
        private final JsonArray inputItems = new JsonArray();
        private final JsonArray inputFluids = new JsonArray();
        private final JsonArray outputItems = new JsonArray();
        private final JsonArray outputFluids = new JsonArray();
        private final JsonArray pools = new JsonArray();
        private final JsonArray conditions = new JsonArray();
        private ItemStack icon = ItemStack.EMPTY;
        private boolean customLocalization;
        private GenericMachineRecipeExtraData extraData = GenericMachineRecipeExtraData.EMPTY;
        private int sourceOrder = GenericMachineRecipe.UNSPECIFIED_SOURCE_ORDER;
        @Nullable
        private String autoSwitchGroup;
        @Nullable
        private String nameWrapper;

        private GenericMachineRecipeBuilder(GenericMachineRecipe.Machine machine, ResourceLocation serializerId,
                String internalName, int duration, long power) {
            this.machine = machine;
            this.serializerId = serializerId;
            this.internalName = internalName;
            this.duration = duration;
            this.power = power;
        }

        private static GenericMachineRecipeBuilder chemical(String internalName, int duration, long power) {
            return new GenericMachineRecipeBuilder(GenericMachineRecipe.Machine.CHEMICAL_PLANT,
                    id("chemical_plant"), internalName, duration, power);
        }

        private static GenericMachineRecipeBuilder assembly(String internalName, int duration, long power) {
            return new GenericMachineRecipeBuilder(GenericMachineRecipe.Machine.ASSEMBLY_MACHINE,
                    id("assembly_machine"), internalName, duration, power);
        }

        private static GenericMachineRecipeBuilder purex(String internalName, int duration, long power) {
            return new GenericMachineRecipeBuilder(GenericMachineRecipe.Machine.PUREX,
                    id("purex"), internalName, duration, power);
        }

        private static GenericMachineRecipeBuilder fusionReactor(String internalName, int duration, long power) {
            return new GenericMachineRecipeBuilder(GenericMachineRecipe.Machine.FUSION_REACTOR,
                    id("fusion_reactor"), internalName, duration, power);
        }

        private static GenericMachineRecipeBuilder plasmaForge(String internalName, int duration, long power) {
            return new GenericMachineRecipeBuilder(GenericMachineRecipe.Machine.PLASMA_FORGE,
                    id("plasma_forge"), internalName, duration, power);
        }

        private static GenericMachineRecipeBuilder precass(String internalName, int duration, long power) {
            return new GenericMachineRecipeBuilder(GenericMachineRecipe.Machine.PRECASS,
                    id("precass"), internalName, duration, power);
        }

        private static GenericMachineRecipeBuilder arcWelder(String internalName, int duration, long power) {
            return new GenericMachineRecipeBuilder(GenericMachineRecipe.Machine.ARC_WELDER,
                    id("arc_welder"), internalName, duration, power);
        }

        private static GenericMachineRecipeBuilder arcFurnace(String internalName, int duration, long power) {
            return new GenericMachineRecipeBuilder(GenericMachineRecipe.Machine.ARC_FURNACE,
                    id("arc_furnace"), internalName, duration, power);
        }

        private GenericMachineRecipeBuilder inputItem(ItemLike item, int count) {
            return inputIngredient(HbmIngredient.of(item, count));
        }

        private GenericMachineRecipeBuilder inputItem(ItemStack stack) {
            return inputIngredient(HbmIngredient.exact(stack));
        }

        private GenericMachineRecipeBuilder inputPartialNbt(ItemStack stack) {
            return inputIngredient(HbmIngredient.partialNbt(stack));
        }

        private GenericMachineRecipeBuilder inputTag(TagKey<Item> tag, int count) {
            return inputIngredient(HbmIngredient.of(tag, count));
        }

        private GenericMachineRecipeBuilder inputLegacyOre(String legacyOreName, int count) {
            return inputIngredient(HbmIngredient.legacyOre(legacyOreName, count));
        }

        private GenericMachineRecipeBuilder inputLegacyOre(String legacyOreName, Ingredient ingredient, int count) {
            return inputIngredient(HbmIngredient.legacyOre(legacyOreName, ingredient, count));
        }

        private GenericMachineRecipeBuilder inputFluidContainer(FluidType fluid, int amount, int count) {
            return inputIngredient(HbmIngredient.fluidContainer(fluid, amount, count));
        }

        private GenericMachineRecipeBuilder inputFluidContainerLegacyOre(FluidType fluid, int amount, int count) {
            return inputIngredient(HbmIngredient.fluidContainer(fluid, amount, count, fluid.getDict(amount)));
        }

        private GenericMachineRecipeBuilder inputLegacyMeta(ResourceLocation legacyId, int legacyMeta, int count) {
            return inputIngredient(HbmIngredient.legacyMeta(legacyId, legacyMeta, count));
        }

        private GenericMachineRecipeBuilder inputLegacyWildcard(ResourceLocation legacyId, int count) {
            return inputIngredient(HbmIngredient.legacyWildcard(legacyId, count));
        }

        private GenericMachineRecipeBuilder inputIngredient(HbmIngredient ingredient) {
            itemInputEntries.add(ingredient);
            inputItems.add(ingredient.toJson());
            return this;
        }

        private GenericMachineRecipeBuilder inputFluid(FluidType fluid, int amount) {
            return inputFluid(fluid, amount, 0);
        }

        private GenericMachineRecipeBuilder inputFluid(FluidType fluid, int amount, int pressure) {
            inputFluids.add(fluidStack(fluid, amount, pressure));
            return this;
        }

        private GenericMachineRecipeBuilder outputItem(ItemLike item) {
            return outputItem(new ItemStack(item));
        }

        private GenericMachineRecipeBuilder outputItem(ItemStack stack) {
            outputItems.add(itemStackJson(stack));
            return this;
        }

        private GenericMachineRecipeBuilder outputTag(TagKey<Item> tag, int count) {
            JsonObject object = new JsonObject();
            object.addProperty("tag", tag.location().toString());
            if (count > 1) {
                object.addProperty("count", count);
            }
            outputItems.add(object);
            return this;
        }

        private GenericMachineRecipeBuilder outputChance(ItemLike item, float chance) {
            return outputChance(new ItemStack(item), chance);
        }

        private GenericMachineRecipeBuilder outputChance(ItemStack stack, float chance) {
            JsonObject object = itemStackJson(stack);
            object.addProperty("chance", chance);
            outputItems.add(object);
            return this;
        }

        private GenericMachineRecipeBuilder outputOneOf(WeightedOutput... outputs) {
            JsonObject object = new JsonObject();
            object.addProperty("type", "one_of");
            JsonArray entries = new JsonArray();
            for (WeightedOutput output : outputs) {
                entries.add(output.toJson());
            }
            object.add("entries", entries);
            outputItems.add(object);
            return this;
        }

        private GenericMachineRecipeBuilder outputLegacyMeta(ResourceLocation legacyId, int legacyMeta) {
            return outputItem(LegacyMetaItemMappings.requireItem(legacyId, legacyMeta).get());
        }

        private GenericMachineRecipeBuilder outputLegacyMetaChance(ResourceLocation legacyId, int legacyMeta, float chance) {
            return outputChance(new ItemStack(LegacyMetaItemMappings.requireItem(legacyId, legacyMeta).get()), chance);
        }

        private GenericMachineRecipeBuilder outputFluid(FluidType fluid, int amount) {
            return outputFluid(fluid, amount, 0);
        }

        private GenericMachineRecipeBuilder outputFluid(FluidType fluid, int amount, int pressure) {
            outputFluids.add(fluidStack(fluid, amount, pressure));
            return this;
        }

        private GenericMachineRecipeBuilder arcMaterialOutput(MaterialStack stack) {
            List<MaterialStack> outputs = new ArrayList<>(extraData.arcMaterialOutputs());
            outputs.add(stack);
            this.extraData = extraData.withArcMaterialOutputs(outputs);
            return this;
        }

        private GenericMachineRecipeBuilder pool(String pool) {
            pools.add(pool);
            return this;
        }

        private GenericMachineRecipeBuilder conditionNotTagEmpty(TagKey<Item> tag) {
            JsonObject tagEmpty = new JsonObject();
            tagEmpty.addProperty("type", "forge:tag_empty");
            tagEmpty.addProperty("tag", tag.location().toString());
            JsonObject not = new JsonObject();
            not.addProperty("type", "forge:not");
            not.add("value", tagEmpty);
            conditions.add(not);
            return this;
        }

        private GenericMachineRecipeBuilder autoSwitchGroup(String group) {
            this.autoSwitchGroup = group;
            return this;
        }

        private GenericMachineRecipeBuilder icon(ItemLike item) {
            return icon(new ItemStack(item));
        }

        private GenericMachineRecipeBuilder icon(ItemStack stack) {
            this.icon = stack.copy();
            return this;
        }

        private GenericMachineRecipeBuilder customLocalization() {
            this.customLocalization = true;
            return this;
        }

        private GenericMachineRecipeBuilder nameWrapper(String wrapper) {
            this.nameWrapper = wrapper;
            return this;
        }

        private GenericMachineRecipeBuilder sourceOrder(int sourceOrder) {
            this.sourceOrder = sourceOrder;
            return this;
        }

        private GenericMachineRecipeBuilder plasmaForgeExtra(long ignitionTemp) {
            this.extraData = new GenericMachineRecipeExtraData(
                    Optional.of(new GenericMachineRecipeExtraData.PlasmaForge(ignitionTemp)),
                    Optional.empty());
            return this;
        }

        private GenericMachineRecipeBuilder fusionExtra(long ignitionTemp, long outputTemp, double outputFlux,
                float r, float g, float b) {
            this.extraData = new GenericMachineRecipeExtraData(
                    Optional.empty(),
                    Optional.of(new GenericMachineRecipeExtraData.Fusion(ignitionTemp, outputTemp,
                            outputFlux, r, g, b)));
            return this;
        }

        private void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
            validate(recipeId);
            consumer.accept(new FinishedRecipe() {
                @Override
                public void serializeRecipeData(JsonObject json) {
                    json.addProperty("internal_name", internalName);
                    json.addProperty("duration", duration);
                    json.addProperty("power", power);
                    if (conditions.size() > 0) {
                        json.add("conditions", conditions);
                    }
                    json.add("input_items", inputItems);
                    json.add("input_fluids", inputFluids);
                    json.add("output_items", outputItems);
                    json.add("output_fluids", outputFluids);
                    json.add("pools", pools);
                    if (!icon.isEmpty()) {
                        json.add("icon", itemStackJson(icon));
                    }
                    if (customLocalization) {
                        json.addProperty("custom_localization", true);
                    }
                    if (autoSwitchGroup != null) {
                        json.addProperty("auto_switch_group", autoSwitchGroup);
                    }
                    if (nameWrapper != null) {
                        json.addProperty("name_wrapper", nameWrapper);
                    }
                    if (sourceOrder != GenericMachineRecipe.UNSPECIFIED_SOURCE_ORDER) {
                        json.addProperty("source_order", sourceOrder);
                    }
                    extraData.writeToJson(json);
                }

                @Override
                public ResourceLocation getId() {
                    return recipeId;
                }

                @Override
                public RecipeSerializer<?> getType() {
                    return HbmRegistryUtil.recipeSerializer(serializerId).orElseThrow();
                }

                @Nullable
                @Override
                public JsonObject serializeAdvancement() {
                    return null;
                }

                @Nullable
                @Override
                public ResourceLocation getAdvancementId() {
                    return null;
                }
            });
        }

        private void validate(ResourceLocation recipeId) {
            if (duration <= 0) {
                throw new IllegalStateException("Invalid HBM machine recipe duration for " + recipeId + ": " + duration);
            }
            if (inputItems.isEmpty() && inputFluids.isEmpty()) {
                throw new IllegalStateException("HBM machine recipe has no inputs: " + recipeId);
            }
            if (outputItems.isEmpty() && outputFluids.isEmpty() && extraData.arcMaterialOutputs().isEmpty()) {
                throw new IllegalStateException("HBM machine recipe has no outputs: " + recipeId);
            }
            if (!extraData.arcMaterialOutputs().isEmpty() && machine != GenericMachineRecipe.Machine.ARC_FURNACE) {
                throw new IllegalStateException("HBM machine recipe has arc material outputs outside arc furnace: "
                        + recipeId);
            }
            try {
                machine.validateRecipeLimits(recipeId, inputItems.size(), inputFluids.size(), outputItems.size(), outputFluids.size());
            } catch (com.google.gson.JsonSyntaxException exception) {
                throw new IllegalStateException(exception.getMessage(), exception);
            }
            for (HbmIngredient input : itemInputEntries) {
                if (input.exceedsStackLimit()) {
                    int limit = input.stackLimit().orElse(64);
                    throw new IllegalStateException("HBM machine recipe " + recipeId
                            + " input count exceeds stack limit: " + input.count() + " > " + limit);
                }
            }
        }

        private static JsonObject fluidStack(FluidType fluid, int amount) {
            return fluidStack(fluid, amount, 0);
        }

        private static JsonObject fluidStack(FluidType fluid, int amount, int pressure) {
            JsonObject object = new JsonObject();
            object.addProperty("fluid", new ResourceLocation(HbmNtm.MOD_ID, fluid.toPath()).toString());
            object.addProperty("amount", amount);
            if (pressure != 0) {
                object.addProperty("pressure", pressure);
            }
            return object;
        }

        private static JsonObject itemStackJson(ItemStack stack) {
            JsonObject object = new JsonObject();
            object.addProperty("item", HbmRegistryUtil.itemKey(stack.getItem()).toString());
            if (stack.getCount() > 1) {
                object.addProperty("count", stack.getCount());
            }
            if (stack.hasTag() && !stack.getTag().isEmpty()) {
                object.addProperty("nbt", stack.getTag().toString());
            }
            return object;
        }

        private record WeightedOutput(ItemStack stack, float chance, int weight) {
            private static WeightedOutput of(ItemLike item, int weight) {
                return new WeightedOutput(new ItemStack(item), 1.0F, weight);
            }

            private static WeightedOutput of(ItemStack stack, int weight) {
                return new WeightedOutput(stack, 1.0F, weight);
            }

            private static WeightedOutput chance(ItemLike item, float chance, int weight) {
                return new WeightedOutput(new ItemStack(item), chance, weight);
            }

            private JsonObject toJson() {
                JsonObject object = itemStackJson(stack);
                if (chance < 1.0F) {
                    object.addProperty("chance", chance);
                }
                if (weight > 0) {
                    object.addProperty("weight", weight);
                }
                return object;
            }
        }
    }

    private static final class PressRecipeBuilder {
        private final ItemPressStamp.StampType stamp;
        private final HbmIngredient input;
        private final ItemStack result;
        private int sourceOrder = Integer.MAX_VALUE;

        private PressRecipeBuilder(ItemPressStamp.StampType stamp, HbmIngredient input, ItemStack result) {
            this.stamp = stamp;
            this.input = input;
            this.result = result.copy();
        }

        private static PressRecipeBuilder press(ItemPressStamp.StampType stamp, Ingredient ingredient, ItemStack result) {
            return press(stamp, HbmIngredient.of(ingredient, 1), result);
        }

        private static PressRecipeBuilder press(ItemPressStamp.StampType stamp, HbmIngredient input, ItemStack result) {
            return new PressRecipeBuilder(stamp, input, result);
        }

        private PressRecipeBuilder sourceOrder(int sourceOrder) {
            this.sourceOrder = sourceOrder;
            return this;
        }

        private void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
            if (result.isEmpty()) {
                throw new IllegalStateException("HBM press recipe has no output: " + recipeId);
            }
            consumer.accept(new FinishedRecipe() {
                @Override
                public void serializeRecipeData(JsonObject json) {
                    json.addProperty("stamp", stamp.getSerializedName());
                    json.add("input", input.toJson());
                    json.add("result", itemStackJson(result));
                    if (sourceOrder != Integer.MAX_VALUE) {
                        json.addProperty("source_order", sourceOrder);
                    }
                }

                @Override
                public ResourceLocation getId() {
                    return recipeId;
                }

                @Override
                public RecipeSerializer<?> getType() {
                    return HbmRegistryUtil.recipeSerializer(id("press")).orElseThrow();
                }

                @Nullable
                @Override
                public JsonObject serializeAdvancement() {
                    return null;
                }

                @Nullable
                @Override
                public ResourceLocation getAdvancementId() {
                    return null;
                }
            });
        }

        private static JsonObject itemStackJson(ItemStack stack) {
            JsonObject object = new JsonObject();
            object.addProperty("item", HbmRegistryUtil.itemKey(stack.getItem()).toString());
            if (stack.getCount() > 1) {
                object.addProperty("count", stack.getCount());
            }
            if (stack.hasTag() && !stack.getTag().isEmpty()) {
                object.addProperty("nbt", stack.getTag().toString());
            }
            return object;
        }
    }

    private static final class PyroOvenRecipeBuilder {
        private final int duration;
        private JsonObject inputItem;
        private JsonObject inputFluid;
        private JsonObject outputItem;
        private JsonObject outputFluid;
        private int sourceOrder = Integer.MAX_VALUE;

        private PyroOvenRecipeBuilder(int duration) {
            this.duration = Math.max(1, duration);
        }

        private static PyroOvenRecipeBuilder pyro(int duration) {
            return new PyroOvenRecipeBuilder(duration);
        }

        private PyroOvenRecipeBuilder inputTag(TagKey<Item> tag, int count) {
            JsonObject object = new JsonObject();
            object.add("ingredient", Ingredient.of(tag).toJson());
            object.addProperty("count", Math.max(1, count));
            inputItem = object;
            return this;
        }

        private PyroOvenRecipeBuilder inputLegacyOre(String legacyOreName, TagKey<Item> tag, int count) {
            return inputLegacyOre(legacyOreName, Ingredient.of(tag), count);
        }

        private PyroOvenRecipeBuilder inputLegacyOre(String legacyOreName, Ingredient ingredient, int count) {
            inputItem = HbmIngredient.legacyOre(legacyOreName, ingredient, count).toJson();
            return this;
        }

        private PyroOvenRecipeBuilder inputItem(ItemLike item, int count) {
            JsonObject object = new JsonObject();
            object.add("ingredient", Ingredient.of(item).toJson());
            object.addProperty("count", Math.max(1, count));
            inputItem = object;
            return this;
        }

        private PyroOvenRecipeBuilder inputItem(HbmIngredient ingredient) {
            inputItem = ingredient.toJson();
            return this;
        }

        private PyroOvenRecipeBuilder inputFluid(FluidType fluid, int amount) {
            inputFluid = fluidStack(fluid, amount);
            return this;
        }

        private PyroOvenRecipeBuilder outputItem(ItemLike item) {
            return outputItem(new ItemStack(item));
        }

        private PyroOvenRecipeBuilder outputItem(ItemStack stack) {
            outputItem = itemStackJson(stack);
            return this;
        }

        private PyroOvenRecipeBuilder outputFluid(FluidType fluid, int amount) {
            outputFluid = fluidStack(fluid, amount);
            return this;
        }

        private PyroOvenRecipeBuilder sourceOrder(int sourceOrder) {
            this.sourceOrder = sourceOrder;
            return this;
        }

        private void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
            if (inputItem == null && inputFluid == null) {
                throw new IllegalStateException("HBM pyro oven recipe has no inputs: " + recipeId);
            }
            if (outputItem == null && outputFluid == null) {
                throw new IllegalStateException("HBM pyro oven recipe has no outputs: " + recipeId);
            }
            consumer.accept(new FinishedRecipe() {
                @Override
                public void serializeRecipeData(JsonObject json) {
                    if (inputFluid != null) {
                        json.add("input_fluid", inputFluid);
                    }
                    if (inputItem != null) {
                        json.add("input_item", inputItem);
                    }
                    if (outputFluid != null) {
                        json.add("output_fluid", outputFluid);
                    }
                    if (outputItem != null) {
                        json.add("output_item", outputItem);
                    }
                    json.addProperty("duration", duration);
                    if (sourceOrder != Integer.MAX_VALUE) {
                        json.addProperty("source_order", sourceOrder);
                    }
                }

                @Override
                public ResourceLocation getId() {
                    return recipeId;
                }

                @Override
                public RecipeSerializer<?> getType() {
                    return HbmRegistryUtil.recipeSerializer(id("pyro_oven")).orElseThrow();
                }

                @Nullable
                @Override
                public JsonObject serializeAdvancement() {
                    return null;
                }

                @Nullable
                @Override
                public ResourceLocation getAdvancementId() {
                    return null;
                }
            });
        }

        private static JsonObject fluidStack(FluidType fluid, int amount) {
            JsonObject object = new JsonObject();
            object.addProperty("fluid", fluid.getName());
            object.addProperty("amount", amount);
            return object;
        }

        private static JsonObject itemStackJson(ItemStack stack) {
            JsonObject object = new JsonObject();
            object.addProperty("item", HbmRegistryUtil.itemKey(stack.getItem()).toString());
            if (stack.getCount() > 1) {
                object.addProperty("count", stack.getCount());
            }
            if (stack.hasTag() && !stack.getTag().isEmpty()) {
                object.addProperty("nbt", stack.getTag().toString());
            }
            return object;
        }
    }

    private static final class MixerRecipeBuilder {
        private final JsonObject output;
        private final int duration;
        private JsonObject input1;
        private JsonObject input2;
        private JsonObject solidInput;
        private int sourceOrder = Integer.MAX_VALUE;

        private MixerRecipeBuilder(FluidType outputFluid, int outputAmount, int duration) {
            this.output = fluidStack(outputFluid, outputAmount);
            this.duration = Math.max(1, duration);
        }

        private static MixerRecipeBuilder mixer(FluidType outputFluid, int outputAmount, int duration) {
            return new MixerRecipeBuilder(outputFluid, outputAmount, duration);
        }

        private MixerRecipeBuilder input1(FluidType fluid, int amount) {
            input1 = fluidStack(fluid, amount);
            return this;
        }

        private MixerRecipeBuilder input2(FluidType fluid, int amount) {
            input2 = fluidStack(fluid, amount);
            return this;
        }

        private MixerRecipeBuilder solidItem(ItemLike item, int count) {
            solidInput = HbmIngredient.of(item, count).toJson();
            return this;
        }

        private MixerRecipeBuilder solidTag(TagKey<Item> tag, int count) {
            solidInput = HbmIngredient.of(tag, count).toJson();
            return this;
        }

        private MixerRecipeBuilder solidLegacyOre(String legacyOreName, int count) {
            solidInput = HbmIngredient.legacyOre(legacyOreName, count).toJson();
            return this;
        }

        private MixerRecipeBuilder solidLegacyOre(String legacyOreName, Ingredient ingredient, int count) {
            solidInput = HbmIngredient.legacyOre(legacyOreName, ingredient, count).toJson();
            return this;
        }

        private MixerRecipeBuilder solidLegacyWildcard(ResourceLocation legacyId, int count) {
            solidInput = HbmIngredient.legacyWildcard(legacyId, count).toJson();
            return this;
        }

        private MixerRecipeBuilder sourceOrder(int sourceOrder) {
            this.sourceOrder = sourceOrder;
            return this;
        }

        private void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
            if (input1 == null && input2 == null && solidInput == null) {
                throw new IllegalStateException("HBM mixer recipe has no inputs: " + recipeId);
            }
            consumer.accept(new FinishedRecipe() {
                @Override
                public void serializeRecipeData(JsonObject json) {
                    json.add("output", output);
                    if (input1 != null) {
                        json.add("input1", input1);
                    }
                    if (input2 != null) {
                        json.add("input2", input2);
                    }
                    if (solidInput != null) {
                        json.add("solid_input", solidInput);
                    }
                    json.addProperty("duration", duration);
                    if (sourceOrder != Integer.MAX_VALUE) {
                        json.addProperty("source_order", sourceOrder);
                    }
                }

                @Override
                public ResourceLocation getId() {
                    return recipeId;
                }

                @Override
                public RecipeSerializer<?> getType() {
                    return HbmRegistryUtil.recipeSerializer(id("mixer")).orElseThrow();
                }

                @Nullable
                @Override
                public JsonObject serializeAdvancement() {
                    return null;
                }

                @Nullable
                @Override
                public ResourceLocation getAdvancementId() {
                    return null;
                }
            });
        }

        private static JsonObject fluidStack(FluidType fluid, int amount) {
            JsonObject object = new JsonObject();
            object.addProperty("fluid", new ResourceLocation(HbmNtm.MOD_ID, fluid.toPath()).toString());
            object.addProperty("amount", amount);
            return object;
        }
    }

    private static final class LiquefactionRecipeBuilder {
        private final Ingredient input;
        @Nullable
        private final HbmIngredient hbmInput;
        private final FluidType output;
        private final int amount;
        private int sourceOrder = Integer.MAX_VALUE;

        private LiquefactionRecipeBuilder(Ingredient input, FluidType output, int amount) {
            this(input, null, output, amount);
        }

        private LiquefactionRecipeBuilder(HbmIngredient input, FluidType output, int amount) {
            this(input.ingredient(), input, output, amount);
        }

        private LiquefactionRecipeBuilder(Ingredient input, @Nullable HbmIngredient hbmInput, FluidType output, int amount) {
            this.input = input;
            this.hbmInput = hbmInput;
            this.output = output;
            this.amount = amount;
        }

        private static LiquefactionRecipeBuilder liquefaction(ItemLike input, FluidType output, int amount) {
            return new LiquefactionRecipeBuilder(Ingredient.of(input), output, amount);
        }

        private static LiquefactionRecipeBuilder liquefaction(TagKey<Item> input, FluidType output, int amount) {
            return new LiquefactionRecipeBuilder(Ingredient.of(input), output, amount);
        }

        private static LiquefactionRecipeBuilder liquefactionLegacyOre(String legacyOreName, TagKey<Item> input, FluidType output, int amount) {
            return new LiquefactionRecipeBuilder(HbmIngredient.legacyOre(legacyOreName, Ingredient.of(input), 1), output, amount);
        }

        private static LiquefactionRecipeBuilder liquefactionLegacyWildcard(ResourceLocation legacyId, FluidType output, int amount) {
            return new LiquefactionRecipeBuilder(HbmIngredient.legacyWildcard(legacyId, 1), output, amount);
        }

        private LiquefactionRecipeBuilder sourceOrder(int sourceOrder) {
            this.sourceOrder = sourceOrder;
            return this;
        }

        private void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
            consumer.accept(new FinishedRecipe() {
                @Override
                public void serializeRecipeData(JsonObject json) {
                    if (hbmInput != null) {
                        json.add("input", hbmInput.toJson());
                    } else {
                        json.add("ingredient", input.toJson());
                    }
                    JsonObject fluid = new JsonObject();
                    fluid.addProperty("fluid", output.getName());
                    fluid.addProperty("amount", amount);
                    fluid.addProperty("pressure", 0);
                    json.add("output", fluid);
                    if (sourceOrder != Integer.MAX_VALUE) {
                        json.addProperty("source_order", sourceOrder);
                    }
                }

                @Override
                public ResourceLocation getId() {
                    return recipeId;
                }

                @Override
                public RecipeSerializer<?> getType() {
                    return HbmRegistryUtil.recipeSerializer(id("liquefaction")).orElseThrow();
                }

                @Nullable
                @Override
                public JsonObject serializeAdvancement() {
                    return null;
                }

                @Nullable
                @Override
                public ResourceLocation getAdvancementId() {
                    return null;
                }
            });
        }
    }

    private static ItemStack fluidContainerStack(ItemLike item, int count, @Nullable FluidType fluid, int amount, int pressure) {
        ItemStack stack = new ItemStack(item, count);
        if (fluid != null) {
            CompoundTag tag = new CompoundTag();
            tag.putString("hbm_fluid", fluid.getName());
            tag.putInt("hbm_fluid_amount", amount);
            tag.putInt("hbm_fluid_pressure", pressure);
            stack.setTag(tag);
        }
        return stack;
    }

    private static ItemStack fluidIconStack(FluidType fluid, int amount, int pressure) {
        return FluidIconItem.make(fluid, amount, pressure);
    }
}
