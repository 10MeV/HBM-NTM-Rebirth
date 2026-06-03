package com.hbm.ntm.datagen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.recipe.ModRecipes;
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

        chemicalBatteryRecipes(consumer);
        assemblyCapacitorRecipes(consumer);
        fluidContainerRecipes(consumer);
        fluidNetworkRecipes(consumer);
        liquefactionRecipes(consumer);
    }

    private static void selfChargingConversion(Consumer<FinishedRecipe> consumer, ItemLike result, String recipeName, ItemLike isotopeBillet) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, result)
                .requires(ModItems.BATTERY_SC_EMPTY.get())
                .requires(isotopeBillet, 2)
                .unlockedBy("has_empty_self_charging_battery", has(ModItems.BATTERY_SC_EMPTY.get()))
                .save(consumer, id("energy/" + recipeName));
    }

    private static void chemicalBatteryRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.chemical("chem.batterylead", 100, 100)
                .inputItem(ModItems.STEEL_PLATE.get(), 4)
                .inputItem(ModItems.LEAD_INGOT.get(), 4)
                .inputFluid(HbmFluids.SULFURIC_ACID, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 1)
                .save(consumer, id("chemical_plant/batterylead"));

        GenericMachineRecipeBuilder.chemical("chem.batterylithium", 100, 1_000)
                .inputTag(forgeTag("dusts/lithium"), 12)
                .inputTag(forgeTag("dusts/cobalt"), 8)
                .inputTag(forgeTag("ingots/any_plastic"), 4)
                .inputFluid(HbmFluids.OXYGEN, 2_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 2)
                .save(consumer, id("chemical_plant/batterylithium"));

        GenericMachineRecipeBuilder.chemical("chem.batterysodium", 100, 10_000)
                .inputTag(forgeTag("dusts/sodium"), 24)
                .inputTag(forgeTag("dusts/iron"), 24)
                .inputTag(forgeTag("ingots/any_hardplastic"), 12)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 3)
                .save(consumer, id("chemical_plant/batterysodium"));

        GenericMachineRecipeBuilder.chemical("chem.batteryschrabidium", 100, 25_000)
                .inputTag(forgeTag("dusts/schrabidium"), 24)
                .inputTag(forgeTag("cast_plates/any_bismoid_bronze"), 8)
                .inputFluid(HbmFluids.HELIUM4, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 4)
                .save(consumer, id("chemical_plant/batteryschrabidium"));

        GenericMachineRecipeBuilder.chemical("chem.batteryquantum", 100, 100_000)
                .inputTag(forgeTag("dense_wires/bscco"), 24)
                .inputItem(item("pellet_charged"), 32)
                .inputItem(item("ingot_cft"), 16)
                .inputFluid(HbmFluids.PERFLUOROMETHYL_COLD, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 5)
                .outputFluid(HbmFluids.PERFLUOROMETHYL, 8_000)
                .save(consumer, id("chemical_plant/batteryquantum"));
    }

    private static void assemblyCapacitorRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.assembly("ass.capacitorgold", 100, 100)
                .inputItem(ModItems.STEEL_PLATE.get(), 8)
                .inputTag(forgeTag("dense_wires/gold"), 16)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 7)
                .save(consumer, id("assembly_machine/capacitorgold"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorniobium", 100, 1_000)
                .inputTag(forgeTag("ingots/any_plastic"), 12)
                .inputTag(forgeTag("dense_wires/niobium"), 24)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 8)
                .save(consumer, id("assembly_machine/capacitorniobium"));

        GenericMachineRecipeBuilder.assembly("ass.capacitortantalum", 100, 10_000)
                .inputTag(forgeTag("ingots/any_hardplastic"), 16)
                .inputTag(forgeTag("ingots/tantalum"), 24)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 9)
                .save(consumer, id("assembly_machine/capacitortantalum"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorbismuth", 100, 25_000)
                .inputTag(forgeTag("ingots/any_hardplastic"), 24)
                .inputTag(forgeTag("ingots/bismuth"), 24)
                .inputTag(forgeTag("circuits/chip_quantum"), 1)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 10)
                .save(consumer, id("assembly_machine/capacitorbismuth"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorspark", 100, 100_000)
                .inputTag(forgeTag("cast_plates/combine_steel"), 12)
                .inputItem(item("powder_spark_mix"), 32)
                .inputItem(item("pellet_charged"), 32)
                .inputTag(forgeTag("circuits/chip_quantum"), 16)
                .inputFluid(HbmFluids.PERFLUOROMETHYL_COLD, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 11)
                .outputFluid(HbmFluids.PERFLUOROMETHYL, 8_000)
                .save(consumer, id("assembly_machine/capacitorspark"));
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
                .save(consumer, id("assembly_machine/emptypackage"));

        HbmFluids.all().stream()
                .filter(type -> type != HbmFluids.NONE && !type.hasNoContainer())
                .forEach(type -> {
                    GenericMachineRecipeBuilder.assembly("ass.package" + type.getName(), 40, 100)
                            .inputItem(fluidContainerStack(ModItems.FLUID_PACK_EMPTY.get(), 1, null, 0, 0))
                            .inputFluid(type, 32_000)
                            .outputItem(fluidContainerStack(ModItems.FLUID_PACK_FULL.get(), 1, type, 32_000, 0))
                            .save(consumer, id("assembly_machine/package_" + type.toPath()));

                    GenericMachineRecipeBuilder.assembly("ass.unpackage" + type.getName(), 40, 100)
                            .inputItem(fluidContainerStack(ModItems.FLUID_PACK_FULL.get(), 1, type, 32_000, 0))
                            .outputFluid(type, 32_000)
                            .outputItem(ModItems.FLUID_PACK_EMPTY.get())
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
            inputFluids.add(fluidStack(fluid, amount));
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
            outputFluids.add(fluidStack(fluid, amount));
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
            JsonObject object = new JsonObject();
            object.addProperty("fluid", new ResourceLocation(HbmNtm.MOD_ID, fluid.toPath()).toString());
            object.addProperty("amount", amount);
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
}
