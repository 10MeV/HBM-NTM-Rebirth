package com.hbm.ntm.datagen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.FluidIconItem;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.recipe.HbmIngredient;
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
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
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

import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class HbmRecipeProvider extends RecipeProvider {
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

        chemicalPlantSourceRecipes(consumer);
        chemicalBatteryRecipes(consumer);
        assemblyCapacitorRecipes(consumer);
        assemblyMachineBodyRecipes(consumer);
        fluidContainerRecipes(consumer);
        fluidNetworkRecipes(consumer);
        liquefactionRecipes(consumer);
        pressRecipes(consumer);
    }

    private static void selfChargingConversion(Consumer<FinishedRecipe> consumer, ItemLike result, String recipeName, ItemLike isotopeBillet) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, result)
                .requires(ModItems.BATTERY_SC_EMPTY.get())
                .requires(isotopeBillet, 2)
                .unlockedBy("has_empty_self_charging_battery", has(ModItems.BATTERY_SC_EMPTY.get()))
                .save(consumer, id("energy/" + recipeName));
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
                .sourceOrder(21)
                .save(consumer, id("chemical_plant/concrete"));

        GenericMachineRecipeBuilder.chemical("chem.concreteasbestos", 100, 100)
                .inputItem(item("powder_cement"), 4)
                .inputLegacyOre("ingotAsbestos", 4)
                .inputLegacyOre("sand", 8)
                .inputFluid(HbmFluids.WATER, 2_000)
                .outputItem(new ItemStack(block("concrete_asbestos"), 16))
                .sourceOrder(22)
                .save(consumer, id("chemical_plant/concreteasbestos"));

        GenericMachineRecipeBuilder.chemical("chem.ducrete", 150, 100)
                .inputItem(item("powder_cement"), 4)
                .inputLegacyOre("ingotFerrouranium", 1)
                .inputLegacyOre("sand", 8)
                .inputFluid(HbmFluids.WATER, 2_000)
                .outputItem(new ItemStack(block("ducrete_smooth"), 8))
                .sourceOrder(23)
                .save(consumer, id("chemical_plant/ducrete"));

        GenericMachineRecipeBuilder.chemical("chem.liquidconk", 100, 100)
                .inputItem(item("powder_cement"), 1)
                .inputItem(Blocks.GRAVEL, 8)
                .inputLegacyOre("sand", 8)
                .inputFluid(HbmFluids.WATER, 2_000)
                .outputFluid(HbmFluids.CONCRETE, 16_000)
                .sourceOrder(24)
                .save(consumer, id("chemical_plant/liquidconk"));

        GenericMachineRecipeBuilder.chemical("chem.asphalt", 100, 100)
                .inputItem(Blocks.GRAVEL, 2)
                .inputLegacyOre("sand", 6)
                .inputFluid(HbmFluids.BITUMEN, 1_000)
                .outputItem(new ItemStack(block("asphalt"), 16))
                .sourceOrder(25)
                .save(consumer, id("chemical_plant/asphalt"));

        GenericMachineRecipeBuilder.chemical("chem.polymer", 100, 100)
                .inputLegacyOre("dustCoal", 2)
                .inputLegacyOre("dustFluorite", 1)
                .inputFluid(HbmFluids.PETROLEUM, 1_000)
                .outputItem(new ItemStack(item("ingot_polymer"), 4))
                .sourceOrder(33)
                .save(consumer, id("chemical_plant/polymer"));

        GenericMachineRecipeBuilder.chemical("chem.bakelite", 100, 100)
                .inputFluid(HbmFluids.AROMATICS, 500)
                .inputFluid(HbmFluids.PETROLEUM, 500)
                .outputItem(item("ingot_bakelite"))
                .sourceOrder(34)
                .save(consumer, id("chemical_plant/bakelite"));

        GenericMachineRecipeBuilder.chemical("chem.rubber", 100, 200)
                .inputLegacyOre("dustSulfur", 1)
                .inputFluid(HbmFluids.UNSATURATEDS, 500)
                .outputItem(new ItemStack(item("ingot_rubber"), 2))
                .sourceOrder(35)
                .save(consumer, id("chemical_plant/rubber"));

        GenericMachineRecipeBuilder.chemical("chem.hardplastic", 100, 1_000)
                .inputFluid(HbmFluids.XYLENE, 500)
                .inputFluid(HbmFluids.PHOSGENE, 500)
                .outputItem(item("ingot_pc"))
                .sourceOrder(36)
                .save(consumer, id("chemical_plant/hardplastic"));

        GenericMachineRecipeBuilder.chemical("chem.pvc", 100, 1_000)
                .inputLegacyOre("dustCadmium", 1)
                .inputFluid(HbmFluids.UNSATURATEDS, 250)
                .inputFluid(HbmFluids.CHLORINE, 250)
                .outputItem(new ItemStack(item("ingot_pvc"), 2))
                .sourceOrder(37)
                .save(consumer, id("chemical_plant/pvc"));

        GenericMachineRecipeBuilder.chemical("chem.kevlar", 60, 300)
                .inputFluid(HbmFluids.AROMATICS, 200)
                .inputFluid(HbmFluids.NITRIC_ACID, 100)
                .inputFluid(HbmFluids.CHLORINE, 100)
                .outputItem(new ItemStack(item("plate_kevlar"), 4))
                .sourceOrder(38)
                .save(consumer, id("chemical_plant/kevlar"));

        GenericMachineRecipeBuilder.chemical("chem.biosolidfuel", 40, 100)
                .inputItem(ModItems.BIOMASS_COMPRESSED.get(), 4)
                .outputItem(item("solid_fuel"))
                .pool(LegacyBlueprintPools.PREFIX_ALT + ".biosolidfuel")
                .customLocalization()
                .sourceOrder(43)
                .save(consumer, id("chemical_plant/biosolidfuel"));

        GenericMachineRecipeBuilder.chemical("chem.biooilsolidfuel", 40, 100)
                .inputItem(ModItems.BIOMASS_COMPRESSED.get(), 2)
                .inputFluid(HbmFluids.HEATINGOIL, 100)
                .outputItem(item("solid_fuel"))
                .pool(LegacyBlueprintPools.PREFIX_ALT + ".biosolidfuel")
                .customLocalization()
                .sourceOrder(44)
                .save(consumer, id("chemical_plant/biooilsolidfuel"));

        GenericMachineRecipeBuilder.chemical("chem.peroxide", 50, 100)
                .inputFluid(HbmFluids.WATER, 1_000)
                .outputFluid(HbmFluids.PEROXIDE, 1_000)
                .sourceOrder(47)
                .save(consumer, id("chemical_plant/peroxide"));

        GenericMachineRecipeBuilder.chemical("chem.sulfuricacid", 50, 100)
                .inputLegacyOre("dustSulfur", 1)
                .inputFluid(HbmFluids.PEROXIDE, 1_000)
                .inputFluid(HbmFluids.WATER, 1_000)
                .outputFluid(HbmFluids.SULFURIC_ACID, 2_000)
                .sourceOrder(48)
                .save(consumer, id("chemical_plant/sulfuricacid"));

        GenericMachineRecipeBuilder.chemical("chem.nitricacid", 50, 100)
                .inputLegacyOre("dustSaltpeter", 1)
                .inputFluid(HbmFluids.SULFURIC_ACID, 500)
                .outputFluid(HbmFluids.NITRIC_ACID, 1_000)
                .sourceOrder(49)
                .save(consumer, id("chemical_plant/nitricacid"));

        GenericMachineRecipeBuilder.chemical("chem.birkeland", 200, 5_000)
                .inputFluid(HbmFluids.AIR, 8_000)
                .inputFluid(HbmFluids.WATER, 2_000)
                .outputFluid(HbmFluids.NITRIC_ACID, 1_000)
                .pool(LegacyBlueprintPools.PREFIX_ALT + ".birkeland")
                .customLocalization()
                .sourceOrder(50)
                .save(consumer, id("chemical_plant/birkeland"));

        GenericMachineRecipeBuilder.chemical("chem.schrabidic", 60, 5_000)
                .inputItem(item("pellet_charged"), 1)
                .inputFluid(HbmFluids.SAS3, 2_000)
                .inputFluid(HbmFluids.PEROXIDE, 2_000)
                .outputFluid(HbmFluids.SCHRABIDIC, 2_000)
                .sourceOrder(51)
                .save(consumer, id("chemical_plant/schrabidic"));

        GenericMachineRecipeBuilder.chemical("chem.schrabidate", 150, 5_000)
                .inputLegacyOre("dustIron", 1)
                .inputFluid(HbmFluids.SCHRABIDIC, 250)
                .outputItem(item("powder_schrabidate"))
                .sourceOrder(52)
                .save(consumer, id("chemical_plant/schrabidate"));

        GenericMachineRecipeBuilder.chemical("chem.epearl", 100, 300)
                .inputLegacyOre("dustDiamond", 1)
                .inputFluid(HbmFluids.XPJUICE, 500)
                .outputFluid(HbmFluids.ENDERJUICE, 100)
                .sourceOrder(40)
                .save(consumer, id("chemical_plant/epearl"));

        GenericMachineRecipeBuilder.chemical("chem.rustysteel", 40, 100)
                .inputItem(block("deco_steel"), 8)
                .inputFluid(HbmFluids.WATER, 1_000)
                .outputItem(new ItemStack(block("deco_rusty_steel"), 8))
                .sourceOrder(42)
                .save(consumer, id("chemical_plant/rustysteel"));

        GenericMachineRecipeBuilder.chemical("chem.coltanpain", 120, 100)
                .inputItem(item("powder_coltan_ore"), 1)
                .inputLegacyOre("dustFluorite", 1)
                .inputFluid(HbmFluids.GAS, 1_000)
                .inputFluid(HbmFluids.OXYGEN, 500)
                .outputFluid(HbmFluids.PAIN, 1_000)
                .sourceOrder(54)
                .save(consumer, id("chemical_plant/coltanpain"));

        GenericMachineRecipeBuilder.chemical("chem.rocketfuel", 200, 100)
                .inputItem(item("solid_fuel"), 2)
                .inputFluid(HbmFluids.PETROLEUM, 200)
                .inputFluid(HbmFluids.NITRIC_ACID, 100)
                .outputItem(new ItemStack(item("rocket_fuel"), 4))
                .sourceOrder(57)
                .save(consumer, id("chemical_plant/rocketfuel"));

        GenericMachineRecipeBuilder.chemical("chem.napalm", 40, 100)
                .inputItem(ModItems.CANISTER_EMPTY.get(), 1)
                .inputFluid(HbmFluids.GASOLINE, 100)
                .inputFluid(HbmFluids.AROMATICS, 50)
                .outputItem(ModItems.CANISTER_NAPALM.get())
                .sourceOrder(58)
                .save(consumer, id("chemical_plant/napalm"));

        GenericMachineRecipeBuilder.chemical("chem.yellowcake", 250, 500)
                .inputLegacyOre("billetUranium", 2)
                .inputLegacyOre("dustSulfur", 2)
                .inputFluid(HbmFluids.PEROXIDE, 500)
                .outputItem(item("powder_yellowcake"))
                .sourceOrder(64)
                .save(consumer, id("chemical_plant/yellowcake"));

        GenericMachineRecipeBuilder.chemical("chem.uf6", 100, 500)
                .inputItem(item("powder_yellowcake"), 1)
                .inputLegacyOre("dustFluorite", 4)
                .inputFluid(HbmFluids.WATER, 1_000)
                .outputItem(new ItemStack(item("sulfur"), 2))
                .outputFluid(HbmFluids.UF6, 1_200)
                .sourceOrder(65)
                .save(consumer, id("chemical_plant/uf6"));

        GenericMachineRecipeBuilder.chemical("chem.puf6", 200, 500)
                .inputLegacyOre("dustPlutonium", 1)
                .inputLegacyOre("dustFluorite", 3)
                .inputFluid(HbmFluids.WATER, 1_000)
                .outputFluid(HbmFluids.PUF6, 900)
                .sourceOrder(66)
                .save(consumer, id("chemical_plant/puf6"));

        GenericMachineRecipeBuilder.chemical("chem.sas3", 200, 5_000)
                .inputLegacyOre("dustSchrabidium", 1)
                .inputLegacyOre("dustSulfur", 2)
                .inputFluid(HbmFluids.PEROXIDE, 2_000)
                .outputFluid(HbmFluids.SAS3, 1_000)
                .sourceOrder(67)
                .save(consumer, id("chemical_plant/sas3"));

        GenericMachineRecipeBuilder.chemical("chem.dhc", 400, 500)
                .inputFluid(HbmFluids.DEUTERIUM, 500)
                .inputFluid(HbmFluids.REFORMGAS, 250)
                .inputFluid(HbmFluids.SYNGAS, 250)
                .outputFluid(HbmFluids.DHC, 500)
                .sourceOrder(69)
                .save(consumer, id("chemical_plant/dhc"));

        GenericMachineRecipeBuilder.chemical("chem.osmiridiumdeath", 240, 1_000)
                .inputItem(item("powder_paleogenite"), 1)
                .inputLegacyOre("dustFluorite", 8)
                .inputItem(item("nugget_bismuth"), 4)
                .inputFluid(HbmFluids.PEROXIDE, 1_000, 5)
                .outputFluid(HbmFluids.DEATH, 1_000)
                .sourceOrder(70)
                .save(consumer, id("chemical_plant/osmiridiumdeath"));
    }

    private static void chemicalBatteryRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.chemical("chem.batterylead", 100, 100)
                .inputItem(ModItems.STEEL_PLATE.get(), 4)
                .inputItem(ModItems.LEAD_INGOT.get(), 4)
                .inputFluid(HbmFluids.SULFURIC_ACID, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 1)
                .sourceOrder(26)
                .save(consumer, id("chemical_plant/batterylead"));

        GenericMachineRecipeBuilder.chemical("chem.batterylithium", 100, 1_000)
                .inputTag(forgeTag("dusts/lithium"), 12)
                .inputTag(forgeTag("dusts/cobalt"), 8)
                .inputTag(forgeTag("ingots/any_plastic"), 4)
                .inputFluid(HbmFluids.OXYGEN, 2_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 2)
                .sourceOrder(27)
                .save(consumer, id("chemical_plant/batterylithium"));

        GenericMachineRecipeBuilder.chemical("chem.batterysodium", 100, 10_000)
                .inputTag(forgeTag("dusts/sodium"), 24)
                .inputTag(forgeTag("dusts/iron"), 24)
                .inputTag(forgeTag("ingots/any_hardplastic"), 12)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 3)
                .sourceOrder(28)
                .save(consumer, id("chemical_plant/batterysodium"));

        GenericMachineRecipeBuilder.chemical("chem.batteryschrabidium", 100, 25_000)
                .inputTag(forgeTag("dusts/schrabidium"), 24)
                .inputTag(forgeTag("cast_plates/any_bismoid_bronze"), 8)
                .inputFluid(HbmFluids.HELIUM4, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 4)
                .sourceOrder(29)
                .save(consumer, id("chemical_plant/batteryschrabidium"));

        GenericMachineRecipeBuilder.chemical("chem.batteryquantum", 100, 100_000)
                .inputTag(forgeTag("dense_wires/bscco"), 24)
                .inputItem(item("pellet_charged"), 32)
                .inputItem(item("ingot_cft"), 16)
                .inputFluid(HbmFluids.PERFLUOROMETHYL_COLD, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 5)
                .outputFluid(HbmFluids.PERFLUOROMETHYL, 8_000)
                .sourceOrder(30)
                .save(consumer, id("chemical_plant/batteryquantum"));
    }

    private static void assemblyCapacitorRecipes(Consumer<FinishedRecipe> consumer) {
        assemblyPlateRecipe(consumer, "ass.plateiron", "iron", "ingots/iron", "plate_iron", 0);
        assemblyPlateRecipe(consumer, "ass.plategold", "gold", "ingots/gold", "plate_gold", 1);
        assemblyPlateRecipe(consumer, "ass.platetitanium", "titanium", "ingots/titanium", "plate_titanium", 2);
        assemblyPlateRecipe(consumer, "ass.platealu", "aluminium", "ingots/aluminium", "plate_aluminium", 3);
        assemblyPlateRecipe(consumer, "ass.platesteel", "steel", "ingots/steel", "plate_steel", 4);
        assemblyPlateRecipe(consumer, "ass.platelead", "lead", "ingots/lead", "plate_lead", 5);
        assemblyPlateRecipe(consumer, "ass.platecopper", "copper", "ingots/copper", "plate_copper", 6);

        GenericMachineRecipeBuilder.assembly("ass.capacitorgold", 100, 100)
                .inputItem(ModItems.STEEL_PLATE.get(), 8)
                .inputTag(forgeTag("dense_wires/gold"), 16)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 7)
                .sourceOrder(134)
                .save(consumer, id("assembly_machine/capacitorgold"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorniobium", 100, 1_000)
                .inputTag(forgeTag("ingots/any_plastic"), 12)
                .inputTag(forgeTag("dense_wires/niobium"), 24)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 8)
                .sourceOrder(135)
                .save(consumer, id("assembly_machine/capacitorniobium"));

        GenericMachineRecipeBuilder.assembly("ass.capacitortantalum", 100, 10_000)
                .inputTag(forgeTag("ingots/any_hardplastic"), 16)
                .inputTag(forgeTag("ingots/tantalum"), 24)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 9)
                .sourceOrder(136)
                .save(consumer, id("assembly_machine/capacitortantalum"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorbismuth", 100, 25_000)
                .inputTag(forgeTag("ingots/any_hardplastic"), 24)
                .inputTag(forgeTag("ingots/bismuth"), 24)
                .inputTag(forgeTag("circuits/chip_quantum"), 1)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 10)
                .sourceOrder(137)
                .save(consumer, id("assembly_machine/capacitorbismuth"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorspark", 100, 100_000)
                .inputTag(forgeTag("cast_plates/combine_steel"), 12)
                .inputItem(item("powder_spark_mix"), 32)
                .inputItem(item("pellet_charged"), 32)
                .inputTag(forgeTag("circuits/chip_quantum"), 16)
                .inputFluid(HbmFluids.PERFLUOROMETHYL_COLD, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 11)
                .outputFluid(HbmFluids.PERFLUOROMETHYL, 8_000)
                .sourceOrder(138)
                .save(consumer, id("assembly_machine/capacitorspark"));

        assemblyPlateRecipe(consumer, "ass.plateschrab", "schrabidium", "ingots/schrabidium", "plate_schrabidium", 8);
        assemblyPlateRecipe(consumer, "ass.platecmb", "combine_steel", "ingots/combine_steel", "plate_combine_steel", 9);
        assemblyPlateRecipe(consumer, "ass.plateweaponsteel", "weaponsteel", "ingots/weapon_steel", "plate_weaponsteel", 11);
        assemblyPlateRecipe(consumer, "ass.platesaturnite", "saturnite", "ingots/saturnite", "plate_saturnite", 12);
        assemblyPlateRecipe(consumer, "ass.platedura", "dura_steel", "ingots/dura_steel", "plate_dura_steel", 13);
        assemblyPlateRecipe(consumer, "ass.plategunmetal", "gunmetal", "ingots/gun_metal", "plate_gunmetal", 10);

        GenericMachineRecipeBuilder.assembly("ass.dalekanium", 200, 100)
                .inputItem(block("block_meteor"), 1)
                .outputItem(item("plate_dalekanium"))
                .sourceOrder(15)
                .save(consumer, id("assembly_machine/dalekanium"));

        GenericMachineRecipeBuilder.assembly("ass.platemixed", 50, 100)
                .inputLegacyOre("plateCopper", 2)
                .inputItem(item("neutron_reflector"), 1)
                .inputLegacyOre("plateSaturnite", 1)
                .outputItem(new ItemStack(item("plate_mixed"), 4))
                .sourceOrder(14)
                .save(consumer, id("assembly_machine/plate_mixed"));

        GenericMachineRecipeBuilder.assembly("ass.platedesh", 200, 100)
                .inputLegacyOre("ingotWorkersAlloy", 4)
                .inputLegacyOre("dustAnyPlastic", 2)
                .inputLegacyOre("ingotDuraSteel", 1)
                .outputItem(new ItemStack(item("plate_desh"), 4))
                .sourceOrder(16)
                .save(consumer, id("assembly_machine/plate_desh"));

        GenericMachineRecipeBuilder.assembly("ass.platebismuth", 200, 100)
                .inputItem(item("nugget_bismuth"), 2)
                .inputLegacyOre("billetU238", 2)
                .inputLegacyOre("dustNiobium", 1)
                .outputItem(item("plate_bismuth"))
                .sourceOrder(17)
                .save(consumer, id("assembly_machine/plate_bismuth"));

        GenericMachineRecipeBuilder.assembly("ass.exsteelplating", 200, 400)
                .inputLegacyOre("plateCastSteel", 4)
                .inputLegacyOre("plateTitanium", 4)
                .inputLegacyOre("boltSteel", 16)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 0)
                .sourceOrder(18)
                .save(consumer, id("assembly_machine/expensive_steel_plating"));

        GenericMachineRecipeBuilder.assembly("ass.exheavyframe", 600, 800)
                .inputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 0, 3)
                .inputLegacyOre("ingotAnyPlastic", 8)
                .inputLegacyOre("plateSextupleCopper", 4)
                .inputLegacyOre("ingotWorkersAlloy", 1)
                .inputLegacyOre("boltDuraSteel", 32)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 1)
                .sourceOrder(19)
                .save(consumer, id("assembly_machine/expensive_heavy_frame"));

        GenericMachineRecipeBuilder.assembly("ass.excircuit", 400, 4_000)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 8, 12)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 1, 8)
                .inputLegacyOre("ingotRubber", 4)
                .inputFluid(HbmFluids.SULFURIC_ACID, 1_000)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 2)
                .sourceOrder(20)
                .save(consumer, id("assembly_machine/expensive_circuit"));

        GenericMachineRecipeBuilder.assembly("ass.exleadplating", 400, 4_000)
                .inputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 0, 2)
                .inputLegacyOre("plateCastLead", 8)
                .inputLegacyOre("ingotBoron", 2)
                .inputLegacyOre("boltTungsten", 32)
                .inputFluid(HbmFluids.LUBRICANT, 1_000)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 3)
                .sourceOrder(21)
                .save(consumer, id("assembly_machine/expensive_lead_plating"));

        GenericMachineRecipeBuilder.assembly("ass.exferroplating", 1_200, 10_000)
                .inputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 3, 3)
                .inputLegacyOre("plateCastFerrouranium", 4)
                .inputLegacyOre("ingotAnyResistantAlloy", 4)
                .inputFluid(HbmFluids.UNSATURATEDS, 1_000)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 4)
                .sourceOrder(22)
                .save(consumer, id("assembly_machine/expensive_ferro_plating"));

        GenericMachineRecipeBuilder.assembly("ass.excomputer", 1_200, 16_000)
                .inputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 2, 3)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 13, 4)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 10, 4)
                .inputItem(block("glass_quartz"), 8)
                .inputFluid(HbmFluids.PERFLUOROMETHYL, 2_000)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 5)
                .sourceOrder(23)
                .save(consumer, id("assembly_machine/expensive_computer"));

        GenericMachineRecipeBuilder.assembly("ass.bronzetubes", 3_000, 250_000)
                .inputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 1, 3)
                .inputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 4, 1)
                .inputLegacyOre("plateCastAnyBismoidBronze", 4)
                .inputLegacyOre("plateSextupleZirconium", 1)
                .inputFluid(HbmFluids.PERFLUOROMETHYL_COLD, 4_000)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 6)
                .outputFluid(HbmFluids.PERFLUOROMETHYL, 4_000)
                .sourceOrder(24)
                .save(consumer, id("assembly_machine/expensive_bronze_tubes"));

        GenericMachineRecipeBuilder.assembly("ass.explastic", 600, 20_000)
                .inputLegacyOre("ingotAnyHardPlastic", 4)
                .inputLegacyOre("ingotAnyPlastic", 16)
                .inputLegacyOre("ingotRubber", 8)
                .inputFluid(HbmFluids.SOLVENT, 1_000)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 7)
                .sourceOrder(25)
                .save(consumer, id("assembly_machine/expensive_plastic"));

        GenericMachineRecipeBuilder.assembly("ass.exgold", 600, 10_000)
                .inputLegacyOre("dustGold", 64)
                .inputLegacyOre("dustGold", 64)
                .outputLegacyMeta(LegacyMetaItemMappings.ITEM_EXPENSIVE, 8)
                .sourceOrder(26)
                .save(consumer, id("assembly_machine/expensive_gold_dust"));

        GenericMachineRecipeBuilder.assembly("ass.hazcloth", 50, 100)
                .inputLegacyOre("dustLead", 4)
                .inputItem(Items.STRING, 8)
                .outputItem(new ItemStack(item("hazmat_cloth"), 4))
                .sourceOrder(27)
                .save(consumer, id("assembly_machine/hazmat_cloth"));

        GenericMachineRecipeBuilder.assembly("ass.firecloth", 50, 100)
                .inputLegacyOre("ingotAsbestos", 1)
                .inputItem(Items.STRING, 8)
                .outputItem(new ItemStack(item("asbestos_cloth"), 4))
                .sourceOrder(28)
                .save(consumer, id("assembly_machine/asbestos_cloth"));

        GenericMachineRecipeBuilder.assembly("ass.filtercoal", 50, 100)
                .inputLegacyOre("dustCoal", 4)
                .inputItem(Items.STRING, 2)
                .inputItem(Items.PAPER, 1)
                .outputItem(item("filter_coal"))
                .sourceOrder(29)
                .save(consumer, id("assembly_machine/filter_coal"));

        GenericMachineRecipeBuilder.assembly("ass.centrifugetower", 100, 100)
                .inputLegacyOre("plateDuraSteel", 4)
                .inputLegacyOre("plateTitanium", 4)
                .inputItem(item("motor"), 1)
                .outputItem(item("centrifuge_element"))
                .sourceOrder(30)
                .save(consumer, id("assembly_machine/centrifuge_tower"));

        GenericMachineRecipeBuilder.assembly("ass.reactorcore", 100, 100)
                .inputLegacyOre("plateCastLead", 4)
                .inputLegacyOre("ingotBeryllium", 8)
                .inputLegacyOre("plateDuraSteel", 8)
                .inputLegacyOre("ingotAsbestos", 4)
                .outputItem(item("reactor_core"))
                .sourceOrder(31)
                .save(consumer, id("assembly_machine/reactor_core"));

        GenericMachineRecipeBuilder.assembly("ass.thermoelement", 60, 100)
                .inputLegacyOre("plateSteel", 1)
                .inputLegacyOre("wireFineMingrade", 2)
                .inputLegacyOre("dustNetherQuartz", 2)
                .outputItem(item("thermo_element"))
                .sourceOrder(32)
                .save(consumer, id("assembly_machine/thermo_element"));

        GenericMachineRecipeBuilder.assembly("ass.thermoelementsilicon", 60, 100)
                .inputLegacyOre("plateSteel", 1)
                .inputLegacyOre("wireFineGold", 2)
                .inputLegacyOre("billetSilicon", 1)
                .outputItem(item("thermo_element"))
                .sourceOrder(33)
                .save(consumer, id("assembly_machine/thermo_element_silicon"));

        GenericMachineRecipeBuilder.assembly("ass.rtgunit", 100, 100)
                .inputLegacyOre("plateCastLead", 2)
                .inputLegacyOre("plateCopper", 4)
                .inputItem(item("thermo_element"), 2)
                .outputItem(item("rtg_unit"))
                .sourceOrder(34)
                .save(consumer, id("assembly_machine/rtg_unit"));

        GenericMachineRecipeBuilder.assembly("ass.magnetron", 40, 100)
                .inputLegacyOre("plateCopper", 3)
                .inputLegacyOre("wireFineTungsten", 4)
                .outputItem(item("magnetron"))
                .sourceOrder(35)
                .save(consumer, id("assembly_machine/magnetron"));

        GenericMachineRecipeBuilder.assembly("ass.titaniumdrill", 100, 100)
                .inputLegacyOre("plateCastDuraSteel", 1)
                .inputLegacyOre("plateTitanium", 8)
                .outputItem(item("drill_titanium"))
                .sourceOrder(36)
                .save(consumer, id("assembly_machine/titanium_drill"));

        GenericMachineRecipeBuilder.assembly("ass.entanglementkit", 200, 100)
                .inputLegacyOre("plateCastDuraSteel", 4)
                .inputLegacyOre("plateCopper", 24)
                .inputLegacyOre("wireDenseGold", 16)
                .inputFluid(HbmFluids.XENON, 8_000)
                .outputItem(item("entanglement_kit"))
                .sourceOrder(37)
                .save(consumer, id("assembly_machine/entanglement_kit"));

        GenericMachineRecipeBuilder.assembly("ass.protoreactor", 200, 100)
                .inputLegacyOre("shellSteel", 4)
                .inputLegacyOre("plateCastLead", 4)
                .inputItem(item("rod_quad_empty"), 10)
                .inputLegacyOre("dyeBrown", 3)
                .outputItem(item("dysfunctional_reactor"))
                .sourceOrder(38)
                .save(consumer, id("assembly_machine/proto_reactor"));

        GenericMachineRecipeBuilder.assembly("ass.partlith", 40, 100)
                .inputLegacyOre("dustLithium", 1)
                .outputItem(new ItemStack(item("part_lithium"), 8))
                .sourceOrder(39)
                .save(consumer, id("assembly_machine/part_lithium"));

        GenericMachineRecipeBuilder.assembly("ass.partberyl", 40, 100)
                .inputLegacyOre("dustBeryllium", 1)
                .outputItem(new ItemStack(item("part_beryllium"), 8))
                .sourceOrder(40)
                .save(consumer, id("assembly_machine/part_beryllium"));

        GenericMachineRecipeBuilder.assembly("ass.partcoal", 40, 100)
                .inputLegacyOre("dustCoal", 1)
                .outputItem(new ItemStack(item("part_carbon"), 8))
                .sourceOrder(41)
                .save(consumer, id("assembly_machine/part_carbon"));

        GenericMachineRecipeBuilder.assembly("ass.partcop", 40, 100)
                .inputLegacyOre("dustCopper", 1)
                .outputItem(new ItemStack(item("part_copper"), 8))
                .sourceOrder(42)
                .save(consumer, id("assembly_machine/part_copper"));

        GenericMachineRecipeBuilder.assembly("ass.partplut", 40, 100)
                .inputLegacyOre("dustPlutonium", 1)
                .outputItem(new ItemStack(item("part_plutonium"), 8))
                .sourceOrder(43)
                .save(consumer, id("assembly_machine/part_plutonium"));

        GenericMachineRecipeBuilder.assembly("ass.cmbtile", 100, 100)
                .inputLegacyOre("anyConcrete", 4)
                .inputLegacyOre("plateCMBSteel", 4)
                .outputItem(new ItemStack(block("cmb_brick"), 8))
                .sourceOrder(44)
                .save(consumer, id("assembly_machine/cmb_tile"));

        GenericMachineRecipeBuilder.assembly("ass.cmbbrick", 100, 100)
                .inputLegacyOre("ingotMagnetizedTungsten", 8)
                .inputItem(block("ducrete"), 4)
                .inputItem(block("cmb_brick"), 8)
                .outputItem(new ItemStack(block("cmb_brick_reinforced"), 8))
                .sourceOrder(45)
                .save(consumer, id("assembly_machine/cmb_brick"));

        GenericMachineRecipeBuilder.assembly("ass.yellowbarrel", 400, 400)
                .inputItem(item("tank_steel"), 1)
                .inputLegacyOre("plateLead", 2)
                .inputItem(item("nuclear_waste"), 10)
                .outputItem(block("yellow_barrel"))
                .sourceOrder(48)
                .save(consumer, id("assembly_machine/yellow_barrel"));

        GenericMachineRecipeBuilder.assembly("ass.vitrifiedbarrel", 400, 400)
                .inputItem(item("tank_steel"), 1)
                .inputLegacyOre("plateLead", 2)
                .inputItem(item("nuclear_waste_vitrified"), 10)
                .outputItem(block("vitrified_barrel"))
                .sourceOrder(49)
                .save(consumer, id("assembly_machine/vitrified_barrel"));
    }

    private static void assemblyPlateRecipe(Consumer<FinishedRecipe> consumer, String internalName, String recipeName,
            String inputTag, String outputItem, int sourceOrder) {
        GenericMachineRecipeBuilder.assembly(internalName, 60, 100)
                .inputTag(forgeTag(inputTag), 1)
                .outputItem(item(outputItem))
                .pool(LegacyBlueprintPools.PREFIX_ALT + "plates")
                .autoSwitchGroup("autoswitch.plates")
                .sourceOrder(sourceOrder)
                .save(consumer, id("assembly_machine/plate_" + recipeName));
    }

    private static void assemblyMachineBodyRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.assembly("ass.shredder", 100, 100)
                .inputLegacyOre("plateSteel", 8)
                .inputLegacyOre("plateCopper", 4)
                .inputItem(ModItems.MOTOR.get(), 2)
                .outputItem(ModBlocks.MACHINE_SHREDDER.get())
                .sourceOrder(70)
                .save(consumer, id("assembly_machine/shredder"));

        GenericMachineRecipeBuilder.assembly("ass.assembler", 200, 100)
                .inputLegacyOre("ingotSteel", 4)
                .inputLegacyOre("plateCopper", 4)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 7), 1)
                .outputItem(ModBlocks.MACHINE_ASSEMBLY_MACHINE.get())
                .sourceOrder(71)
                .save(consumer, id("assembly_machine/assembler"));

        GenericMachineRecipeBuilder.assembly("ass.chemplant", 200, 100)
                .inputLegacyOre("ingotSteel", 8)
                .inputLegacyOre("ntmpipeCopper", 2)
                .inputItem(item("plate_polymer"), 16)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputItem(ModItems.TUNGSTEN_COIL.get(), 2)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 7), 1)
                .outputItem(ModBlocks.MACHINE_CHEMICAL_PLANT.get())
                .sourceOrder(72)
                .save(consumer, id("assembly_machine/chemplant"));

        GenericMachineRecipeBuilder.assembly("ass.purex", 300, 100)
                .inputLegacyOre("shellSteel", 4)
                .inputLegacyOre("ntmpipeRubber", 8)
                .inputLegacyOre("plateTripleLead", 4)
                .inputItem(item("motor_desh"), 1)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 8), 4)
                .outputItem(ModBlocks.MACHINE_PUREX.get())
                .sourceOrder(73)
                .save(consumer, id("assembly_machine/purex"));

        GenericMachineRecipeBuilder.assembly("ass.centrifuge", 200, 100)
                .inputItem(item("centrifuge_element"), 1)
                .inputLegacyOre("ingotAnyPlastic", 4)
                .inputLegacyOre("plateSteel", 8)
                .inputLegacyOre("plateCopper", 4)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 7), 1)
                .outputItem(ModBlocks.MACHINE_CENTRIFUGE.get())
                .sourceOrder(75)
                .save(consumer, id("assembly_machine/centrifuge"));

        GenericMachineRecipeBuilder.assembly("ass.gascent", 400, 100)
                .inputItem(item("centrifuge_element"), 4)
                .inputLegacyOre("ingotAnyPlastic", 8)
                .inputLegacyOre("ingotWorkersAlloy", 2)
                .inputLegacyOre("plateSteel", 8)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 9), 1)
                .outputItem(ModBlocks.MACHINE_GASCENT.get())
                .sourceOrder(76)
                .save(consumer, id("assembly_machine/gascent"));

        GenericMachineRecipeBuilder.assembly("ass.derrick", 200, 100)
                .inputLegacyOre("plateSteel", 8)
                .inputLegacyOre("plateTripleCopper", 2)
                .inputLegacyOre("ntmpipeSteel", 4)
                .inputItem(ModItems.MOTOR.get(), 1)
                .inputItem(item("drill_titanium"), 1)
                .outputItem(ModBlocks.MACHINE_WELL.get())
                .sourceOrder(81)
                .save(consumer, id("assembly_machine/derrick"));

        GenericMachineRecipeBuilder.assembly("ass.pumpjack", 400, 100)
                .inputLegacyOre("plateDuraSteel", 8)
                .inputLegacyOre("plateSextupleSteel", 8)
                .inputLegacyOre("ntmpipeSteel", 12)
                .inputItem(item("motor_desh"), 1)
                .inputItem(item("drill_titanium"), 1)
                .outputItem(ModBlocks.MACHINE_PUMPJACK.get())
                .sourceOrder(82)
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
                .sourceOrder(83)
                .save(consumer, id("assembly_machine/fracker"));

        GenericMachineRecipeBuilder.assembly("ass.refinery", 200, 100)
                .inputLegacyOre("plateSextupleSteel", 3)
                .inputLegacyOre("plateCopper", 8)
                .inputLegacyOre("shellSteel", 4)
                .inputLegacyOre("ntmpipeSteel", 12)
                .inputItem(item("plate_polymer"), 8)
                .inputItem(legacyMetaItem(LegacyMetaItemMappings.CIRCUIT, 7), 3)
                .outputItem(ModBlocks.MACHINE_REFINERY.get())
                .sourceOrder(85)
                .save(consumer, id("assembly_machine/refinery"));
    }

    private static void fluidContainerRecipes(Consumer<FinishedRecipe> consumer) {
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

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModItems.CANISTER_NAPALM.get())
                .requires(StrictNBTIngredient.of(fluidContainerStack(ModItems.CANISTER_FULL.get(), 1, HbmFluids.GASOLINE, 1_000, 0)))
                .requires(Items.SLIME_BALL)
                .unlockedBy("has_canister_full", has(ModItems.CANISTER_FULL.get()))
                .save(consumer, id("blast_furnace/canister_napalm"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.INF_WATER.get())
                .pattern("AAA")
                .pattern("WDW")
                .pattern("AAA")
                .define('A', ModItems.ALUMINIUM_PLATE.get())
                .define('W', HbmFluidContainerIngredient.of(HbmFluids.WATER, 1_000))
                .define('D', Items.DIAMOND)
                .unlockedBy("has_aluminium_plate", has(ModItems.ALUMINIUM_PLATE.get()))
                .save(consumer, id("control/inf_water"));

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
                .sourceOrder(327)
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

    private static void liquefactionRecipes(Consumer<FinishedRecipe> consumer) {
        LiquefactionRecipeBuilder.liquefaction(ModItems.BIOMASS.get(), HbmFluids.BIOGAS, 125)
                .save(consumer, id("liquefaction/biomass"));
        LiquefactionRecipeBuilder.liquefaction(ModItems.GLYPHID_GLAND_EMPTY.get(), HbmFluids.BIOGAS, 2_000)
                .save(consumer, id("liquefaction/glyphid_gland_empty"));
        LiquefactionRecipeBuilder.liquefaction(HbmItemTagsProvider.forgeItemTag("gems/coal"), HbmFluids.COALOIL, 100)
                .save(consumer, id("liquefaction/coal_gem"));
        LiquefactionRecipeBuilder.liquefaction(HbmItemTagsProvider.forgeItemTag("dusts/coal"), HbmFluids.COALOIL, 100)
                .save(consumer, id("liquefaction/coal_dust"));
        LiquefactionRecipeBuilder.liquefaction(HbmItemTagsProvider.forgeItemTag("gems/lignite"), HbmFluids.COALOIL, 50)
                .save(consumer, id("liquefaction/lignite_gem"));
        LiquefactionRecipeBuilder.liquefaction(HbmItemTagsProvider.forgeItemTag("dusts/lignite"), HbmFluids.COALOIL, 50)
                .save(consumer, id("liquefaction/lignite_dust"));
        LiquefactionRecipeBuilder.liquefaction(HbmItemTagsProvider.forgeItemTag("dusts/sodium"), HbmFluids.SODIUM, 100)
                .save(consumer, id("liquefaction/sodium_dust"));
        LiquefactionRecipeBuilder.liquefaction(HbmItemTagsProvider.forgeItemTag("ingots/lead"), HbmFluids.LEAD, 100)
                .save(consumer, id("liquefaction/lead_ingot"));
        LiquefactionRecipeBuilder.liquefaction(HbmItemTagsProvider.forgeItemTag("dusts/lead"), HbmFluids.LEAD, 100)
                .save(consumer, id("liquefaction/lead_dust"));
        LiquefactionRecipeBuilder.liquefaction(HbmItemTagsProvider.forgeItemTag("storage_blocks/lead"), HbmFluids.LEAD, 900)
                .save(consumer, id("liquefaction/lead_block"));
        LiquefactionRecipeBuilder.liquefaction(ItemTags.LOGS, HbmFluids.MUG, 100)
                .save(consumer, id("liquefaction/logs"));
        LiquefactionRecipeBuilder.liquefaction(block("ore_oil_sand"), HbmFluids.BITUMEN, 100)
                .save(consumer, id("liquefaction/ore_oil_sand"));
        LiquefactionRecipeBuilder.liquefaction(Items.SNOWBALL, HbmFluids.WATER, 125)
                .save(consumer, id("liquefaction/snowball"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.SNOW_BLOCK, HbmFluids.WATER, 500)
                .save(consumer, id("liquefaction/snow_block"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.ICE, HbmFluids.WATER, 1_000)
                .save(consumer, id("liquefaction/ice"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.PACKED_ICE, HbmFluids.WATER, 1_000)
                .save(consumer, id("liquefaction/packed_ice"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.NETHERRACK, HbmFluids.LAVA, 250)
                .save(consumer, id("liquefaction/netherrack"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.COBBLESTONE, HbmFluids.LAVA, 250)
                .save(consumer, id("liquefaction/cobblestone"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.STONE, HbmFluids.LAVA, 250)
                .save(consumer, id("liquefaction/stone"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.OBSIDIAN, HbmFluids.LAVA, 500)
                .save(consumer, id("liquefaction/obsidian"));
        LiquefactionRecipeBuilder.liquefaction(Items.ENDER_PEARL, HbmFluids.ENDERJUICE, 100)
                .save(consumer, id("liquefaction/ender_pearl"));
        LiquefactionRecipeBuilder.liquefaction(Items.SUGAR, HbmFluids.ETHANOL, 100)
                .save(consumer, id("liquefaction/sugar"));
        LiquefactionRecipeBuilder.liquefaction(Items.WHEAT_SEEDS, HbmFluids.SEEDSLURRY, 50)
                .save(consumer, id("liquefaction/wheat_seeds"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.VINE, HbmFluids.SEEDSLURRY, 100)
                .save(consumer, id("liquefaction/vine"));
        LiquefactionRecipeBuilder.liquefaction(Items.KELP, HbmFluids.SEEDSLURRY, 100)
                .save(consumer, id("liquefaction/kelp"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.GRASS, HbmFluids.SEEDSLURRY, 100)
                .save(consumer, id("liquefaction/grass"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.FERN, HbmFluids.SEEDSLURRY, 100)
                .save(consumer, id("liquefaction/fern"));
        LiquefactionRecipeBuilder.liquefaction(Items.COD, HbmFluids.FISHOIL, 100)
                .save(consumer, id("liquefaction/cod"));
        LiquefactionRecipeBuilder.liquefaction(Items.SALMON, HbmFluids.FISHOIL, 100)
                .save(consumer, id("liquefaction/salmon"));
        LiquefactionRecipeBuilder.liquefaction(Items.TROPICAL_FISH, HbmFluids.FISHOIL, 100)
                .save(consumer, id("liquefaction/tropical_fish"));
        LiquefactionRecipeBuilder.liquefaction(Items.PUFFERFISH, HbmFluids.FISHOIL, 100)
                .save(consumer, id("liquefaction/pufferfish"));
        LiquefactionRecipeBuilder.liquefaction(Blocks.SUNFLOWER, HbmFluids.SUNFLOWEROIL, 100)
                .save(consumer, id("liquefaction/sunflower"));
    }

    private static void pressRecipes(Consumer<FinishedRecipe> consumer) {
        flatPressRecipes(consumer);

        for (int i = 0; i < 8; i++) {
            ItemPressStamp.StampType stamp = ItemPressStamp.StampType.byName("printing" + (i + 1));
            PressRecipeBuilder.press(stamp, Ingredient.of(Items.PAPER),
                            new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.PAGE_OF, i).get()))
                    .save(consumer, id("press/page_of_page" + (i + 1)));
        }

        PressRecipeBuilder.press(ItemPressStamp.StampType.C9, Ingredient.of(forgeTag("plates/gun_metal")),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.CASING, 0).get(), 4))
                .save(consumer, id("press/casing_small_gunmetal"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.C50, Ingredient.of(forgeTag("plates/gun_metal")),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.CASING, 1).get(), 2))
                .save(consumer, id("press/casing_large_gunmetal"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, Ingredient.of(forgeTag("ingots/weapon_steel")),
                        new ItemStack(item("plate_weaponsteel")))
                .save(consumer, id("press/weaponsteel_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, Ingredient.of(forgeTag("ingots/schrabidium")),
                        new ItemStack(item("plate_schrabidium")))
                .save(consumer, id("press/schrabidium_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, Ingredient.of(forgeTag("ingots/combine_steel")),
                        new ItemStack(item("plate_combine_steel")))
                .save(consumer, id("press/combine_steel_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, Ingredient.of(forgeTag("ingots/saturnite")),
                        new ItemStack(item("plate_saturnite")))
                .save(consumer, id("press/saturnite_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.PLATE, Ingredient.of(forgeTag("ingots/dura_steel")),
                        new ItemStack(item("plate_dura_steel")))
                .save(consumer, id("press/dura_steel_plate"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.C9, Ingredient.of(forgeTag("plates/weapon_steel")),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.CASING, 2).get(), 4))
                .save(consumer, id("press/casing_small_weaponsteel"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.C50, Ingredient.of(forgeTag("plates/weapon_steel")),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.CASING, 3).get(), 2))
                .save(consumer, id("press/casing_large_weaponsteel"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.WIRE, Ingredient.of(Items.GOLD_INGOT),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.WIRE_FINE, 7_900).get(), 8))
                .save(consumer, id("press/wire_gold"));
    }

    private static void flatPressRecipes(Consumer<FinishedRecipe> consumer) {
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, Ingredient.of(forgeTag("dusts/quartz")),
                        new ItemStack(Items.QUARTZ))
                .save(consumer, id("press/flat_quartz"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, Ingredient.of(forgeTag("dusts/lapis")),
                        new ItemStack(Items.LAPIS_LAZULI))
                .save(consumer, id("press/flat_lapis"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, Ingredient.of(forgeTag("dusts/diamond")),
                        new ItemStack(Items.DIAMOND))
                .save(consumer, id("press/flat_diamond"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, Ingredient.of(forgeTag("dusts/emerald")),
                        new ItemStack(Items.EMERALD))
                .save(consumer, id("press/flat_emerald"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, Ingredient.of(item("biomass")),
                        new ItemStack(item("biomass_compressed")))
                .save(consumer, id("press/flat_biomass"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, Ingredient.of(forgeTag("gems/coke")),
                        new ItemStack(item("ingot_graphite")))
                .save(consumer, id("press/flat_graphite"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, Ingredient.of(forgeTag("dusts/coal")),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.BRIQUETTE, 0).get()))
                .save(consumer, id("press/flat_briquette_coal"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, Ingredient.of(forgeTag("dusts/lignite")),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.BRIQUETTE, 1).get()))
                .save(consumer, id("press/flat_briquette_lignite"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, Ingredient.of(item("powder_sawdust")),
                        new ItemStack(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.BRIQUETTE, 2).get()))
                .save(consumer, id("press/flat_briquette_wood"));
        PressRecipeBuilder.press(ItemPressStamp.StampType.FLAT, Ingredient.of(new ItemStack(Blocks.JUNGLE_LOG)),
                        new ItemStack(item("ball_resin")))
                .save(consumer, id("press/flat_resin"));
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

    private static ItemLike legacyBatteryPack(int legacyMeta) {
        return LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.BATTERY_PACK, legacyMeta).get();
    }

    private static ItemLike legacySelfChargingBattery(int legacyMeta) {
        return LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.BATTERY_SC, legacyMeta).get();
    }

    private static ItemLike legacyMetaItem(ResourceLocation legacyId, int legacyMeta) {
        return LegacyMetaItemMappings.requireItem(legacyId, legacyMeta).get();
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    private static TagKey<Item> forgeTag(String path) {
        return HbmItemTagsProvider.forgeItemTag(path);
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

        private static GenericMachineRecipeBuilder precass(String internalName, int duration, long power) {
            return new GenericMachineRecipeBuilder(GenericMachineRecipe.Machine.PRECASS,
                    id("precass"), internalName, duration, power);
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

        private GenericMachineRecipeBuilder pool(String pool) {
            pools.add(pool);
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
                    return BuiltInRegistries.RECIPE_SERIALIZER.get(serializerId);
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
            if (outputItems.isEmpty() && outputFluids.isEmpty()) {
                throw new IllegalStateException("HBM machine recipe has no outputs: " + recipeId);
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
            object.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
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
        private final Ingredient ingredient;
        private final ItemStack result;

        private PressRecipeBuilder(ItemPressStamp.StampType stamp, Ingredient ingredient, ItemStack result) {
            this.stamp = stamp;
            this.ingredient = ingredient;
            this.result = result.copy();
        }

        private static PressRecipeBuilder press(ItemPressStamp.StampType stamp, Ingredient ingredient, ItemStack result) {
            return new PressRecipeBuilder(stamp, ingredient, result);
        }

        private void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
            if (result.isEmpty()) {
                throw new IllegalStateException("HBM press recipe has no output: " + recipeId);
            }
            consumer.accept(new FinishedRecipe() {
                @Override
                public void serializeRecipeData(JsonObject json) {
                    json.addProperty("stamp", stamp.getSerializedName());
                    json.add("ingredient", ingredient.toJson());
                    json.add("result", itemStackJson(result));
                }

                @Override
                public ResourceLocation getId() {
                    return recipeId;
                }

                @Override
                public RecipeSerializer<?> getType() {
                    return BuiltInRegistries.RECIPE_SERIALIZER.get(id("press"));
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
            object.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            if (stack.getCount() > 1) {
                object.addProperty("count", stack.getCount());
            }
            if (stack.hasTag() && !stack.getTag().isEmpty()) {
                object.addProperty("nbt", stack.getTag().toString());
            }
            return object;
        }
    }

    private static final class LiquefactionRecipeBuilder {
        private final Ingredient input;
        private final FluidType output;
        private final int amount;

        private LiquefactionRecipeBuilder(Ingredient input, FluidType output, int amount) {
            this.input = input;
            this.output = output;
            this.amount = amount;
        }

        private static LiquefactionRecipeBuilder liquefaction(ItemLike input, FluidType output, int amount) {
            return new LiquefactionRecipeBuilder(Ingredient.of(input), output, amount);
        }

        private static LiquefactionRecipeBuilder liquefaction(TagKey<Item> input, FluidType output, int amount) {
            return new LiquefactionRecipeBuilder(Ingredient.of(input), output, amount);
        }

        private void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
            consumer.accept(new FinishedRecipe() {
                @Override
                public void serializeRecipeData(JsonObject json) {
                    json.add("ingredient", input.toJson());
                    JsonObject fluid = new JsonObject();
                    fluid.addProperty("fluid", output.getName());
                    fluid.addProperty("amount", amount);
                    fluid.addProperty("pressure", 0);
                    json.add("output", fluid);
                }

                @Override
                public ResourceLocation getId() {
                    return recipeId;
                }

                @Override
                public RecipeSerializer<?> getType() {
                    return BuiltInRegistries.RECIPE_SERIALIZER.get(id("liquefaction"));
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
