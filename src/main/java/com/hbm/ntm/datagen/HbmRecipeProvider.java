package com.hbm.ntm.datagen;

import com.hbm.ntm.util.HbmRegistryUtil;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.compat.CompatRecipeRegistry;
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

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.UPGRADE_SCREM.get())
                .pattern("SUS")
                .pattern("SCS")
                .pattern("SUS")
                .define('S', forgeTag("plates/steel"))
                .define('U', ModItems.UPGRADE_TEMPLATE.get())
                .define('C', item("crystal_xen"))
                .unlockedBy("has_crystal_xen", has(item("crystal_xen")))
                .save(consumer, id("control/upgrade_screm"));

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(item("glyphid_meat")), RecipeCategory.FOOD,
                        item("glyphid_meat_grilled"), 1.0F, 200)
                .unlockedBy("has_glyphid_meat", has(item("glyphid_meat")))
                .save(consumer, id("smelting/glyphid_meat"));

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
        energyNetworkRecipes(consumer);
        redstoneOverRadioRecipes(consumer);
        rbmkRecipes(consumer);
        legacySandMixRecipes(consumer);
        legacyToolRecipes(consumer);
        legacyPartRecipes(consumer);
        legacyStructuralRecipes(consumer);
        legacyArmorTableRecipe(consumer);
        legacyArmorModuleMaterialRecipes(consumer);
        legacyArmorModuleRecipes(consumer);
        legacyHazmatRecipes(consumer);
        legacyArmorRecipes(consumer);
        legacyArtilleryAmmoRecipes(consumer);
        legacyTurretRecipes(consumer);

        chemicalPlantSourceRecipes(consumer);
        chemicalBatteryRecipes(consumer);
        assemblyCapacitorRecipes(consumer);
        assemblyMachineBodyRecipes(consumer);
        satelliteRecipes(consumer);
        fluidContainerRecipes(consumer);
        fluidNetworkRecipes(consumer);
        liquefactionRecipes(consumer);
        pyroOvenRecipes(consumer);
        pressRecipes(consumer);
        compatRecipeListenerRecipes(consumer);
    }

    private static void compatRecipeListenerRecipes(Consumer<FinishedRecipe> consumer) {
        CompatRecipeRegistry.emitRecipeRegisterListeners((recipeId, recipeJson) ->
                consumer.accept(finishedCompatRecipe(recipeId, recipeJson)));
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

    private static void selfChargingConversion(Consumer<FinishedRecipe> consumer, ItemLike result, String recipeName, ItemLike isotopeBillet) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, result)
                .requires(ModItems.BATTERY_SC_EMPTY.get())
                .requires(isotopeBillet, 2)
                .unlockedBy("has_empty_self_charging_battery", has(ModItems.BATTERY_SC_EMPTY.get()))
                .save(consumer, id("energy/" + recipeName));
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
    }

    private static void redstoneOverRadioRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.RBMK_DISPLAY_BLANK.get(), 8)
                .pattern("B")
                .pattern("D")
                .define('B', forgeTag("ingots/bismuth"))
                .define('D', block("concrete_asbestos"))
                .unlockedBy("has_concrete_asbestos", has(block("concrete_asbestos")))
                .save(consumer, id("redstone_over_radio/rbmk_display_blank"));

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
    }

    private static void rbmkRecipes(Consumer<FinishedRecipe> consumer) {
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

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModBlocks.SAND_QUARTZ.get()), RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.GLASS_QUARTZ.get(), 0.25F, 200)
                .unlockedBy("has_quartz_sand", has(ModBlocks.SAND_QUARTZ.get()))
                .save(consumer, id("smelting/glass_quartz_from_sand_quartz"));
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

    private static void legacyToolRecipes(Consumer<FinishedRecipe> consumer) {
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

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.HAND_DRILL.get())
                .pattern(" D")
                .pattern("S ")
                .pattern(" S")
                .define('D', item("ingot_dura_steel"))
                .define('S', Items.STICK)
                .unlockedBy("has_dura_steel", has(item("ingot_dura_steel")))
                .save(consumer, id("tools/hand_drill"));
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
                .pattern("EIE")
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

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.FIVE_HTP.get())
                .requires(forgeTag("dusts/coal"))
                .requires(forgeTag("dusts/euphemium"))
                .requires(ModItems.CANTEEN_VODKA.get())
                .unlockedBy("has_vodka_canteen", has(ModItems.CANTEEN_VODKA.get()))
                .save(consumer, id("consumables/five_htp"));

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
                .requires(item("nugget_lead"), 4)
                .requires(Items.CLAY_BALL)
                .requires(Items.GLASS_BOTTLE)
                .unlockedBy("has_lead_nugget", has(item("nugget_lead")))
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

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MEDAL_LIQUIDATOR.get())
                .pattern("GBG")
                .pattern("BFB")
                .pattern("GBG")
                .define('G', forgeTag("nuggets/au198"))
                .define('B', forgeTag("ingots/boron"))
                .define('F', item("debris_fuel"))
                .unlockedBy("has_debris_fuel", has(item("debris_fuel")))
                .save(consumer, id("armor_modules/medal_liquidator"));

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
                .define('D', Items.DIAMOND)
                .unlockedBy("has_meteorite_fragment", has(item("fragment_meteorite")))
                .save(consumer, id("armor_modules/protection_charm"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.METEOR_CHARM.get())
                .pattern(" M ")
                .pattern("MDM")
                .pattern(" M ")
                .define('M', item("fragment_meteorite"))
                .define('D', forgeTag("gems/volcanic"))
                .unlockedBy("has_meteorite_fragment", has(item("fragment_meteorite")))
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
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("sphere_steel"))
                .pattern("PIP")
                .pattern("I I")
                .pattern("PIP")
                .define('P', forgeTag("plates/steel"))
                .define('I', forgeTag("ingots/steel"))
                .unlockedBy("has_steel_plate", has(forgeTag("plates/steel")))
                .save(consumer, id("parts/sphere_steel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("blade_tungsten"), 2)
                .pattern("TP")
                .pattern("TP")
                .pattern("TT")
                .define('T', forgeTag("ingots/tungsten"))
                .define('P', forgeTag("plates/tungsten"))
                .unlockedBy("has_tungsten_ingot", has(forgeTag("ingots/tungsten")))
                .save(consumer, id("parts/blade_tungsten"));

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
                .inputTag(forgeTag("dusts/lithium"), 12)
                .inputTag(forgeTag("dusts/cobalt"), 8)
                .inputTag(forgeTag("ingots/any_plastic"), 4)
                .inputFluid(HbmFluids.OXYGEN, 2_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 2)
                .sourceOrder(29)
                .save(consumer, id("chemical_plant/batterylithium"));

        GenericMachineRecipeBuilder.chemical("chem.batterysodium", 100, 10_000)
                .inputTag(forgeTag("dusts/sodium"), 24)
                .inputTag(forgeTag("dusts/iron"), 24)
                .inputTag(forgeTag("ingots/any_hardplastic"), 12)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 3)
                .sourceOrder(30)
                .save(consumer, id("chemical_plant/batterysodium"));

        GenericMachineRecipeBuilder.chemical("chem.batteryschrabidium", 100, 25_000)
                .inputTag(forgeTag("dusts/schrabidium"), 24)
                .inputTag(forgeTag("cast_plates/any_bismoid_bronze"), 8)
                .inputFluid(HbmFluids.HELIUM4, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 4)
                .sourceOrder(31)
                .save(consumer, id("chemical_plant/batteryschrabidium"));

        GenericMachineRecipeBuilder.chemical("chem.batteryquantum", 100, 100_000)
                .inputTag(forgeTag("dense_wires/bscco"), 24)
                .inputItem(item("pellet_charged"), 32)
                .inputItem(item("ingot_cft"), 16)
                .inputFluid(HbmFluids.PERFLUOROMETHYL_COLD, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 5)
                .outputFluid(HbmFluids.PERFLUOROMETHYL, 8_000)
                .sourceOrder(32)
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
                .inputItem(ModItems.STEEL_PLATE.get(), 8)
                .inputTag(forgeTag("dense_wires/gold"), 16)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 7)
                .sourceOrder(139)
                .save(consumer, id("assembly_machine/capacitorgold"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorniobium", 100, 1_000)
                .inputTag(forgeTag("ingots/any_plastic"), 12)
                .inputTag(forgeTag("dense_wires/niobium"), 24)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 8)
                .sourceOrder(140)
                .save(consumer, id("assembly_machine/capacitorniobium"));

        GenericMachineRecipeBuilder.assembly("ass.capacitortantalum", 100, 10_000)
                .inputTag(forgeTag("ingots/any_hardplastic"), 16)
                .inputTag(forgeTag("ingots/tantalum"), 24)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 9)
                .sourceOrder(141)
                .save(consumer, id("assembly_machine/capacitortantalum"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorbismuth", 100, 25_000)
                .inputTag(forgeTag("ingots/any_hardplastic"), 24)
                .inputTag(forgeTag("ingots/bismuth"), 24)
                .inputTag(forgeTag("circuits/chip_quantum"), 1)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 10)
                .sourceOrder(142)
                .save(consumer, id("assembly_machine/capacitorbismuth"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorspark", 100, 100_000)
                .inputTag(forgeTag("cast_plates/combine_steel"), 12)
                .inputItem(item("powder_spark_mix"), 32)
                .inputItem(item("pellet_charged"), 32)
                .inputTag(forgeTag("circuits/chip_quantum"), 16)
                .inputFluid(HbmFluids.PERFLUOROMETHYL_COLD, 8_000)
                .outputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 11)
                .outputFluid(HbmFluids.PERFLUOROMETHYL, 8_000)
                .sourceOrder(143)
                .save(consumer, id("assembly_machine/capacitorspark"));

        assemblyPlateRecipe(consumer, "ass.plateschrab", "schrabidium", "ingots/schrabidium", "plate_schrabidium", 7);
        assemblyPlateRecipe(consumer, "ass.platecmb", "combine_steel", "ingots/combine_steel", "plate_combine_steel", 8);
        assemblyPlateRecipe(consumer, "ass.plateweaponsteel", "weaponsteel", "ingots/weapon_steel", "plate_weaponsteel", 10);
        assemblyPlateRecipe(consumer, "ass.platesaturnite", "saturnite", "ingots/saturnite", "plate_saturnite", 11);
        assemblyPlateRecipe(consumer, "ass.platedura", "dura_steel", "ingots/dura_steel", "plate_dura_steel", 12);
        assemblyPlateRecipe(consumer, "ass.plategunmetal", "gunmetal", "ingots/gun_metal", "plate_gunmetal", 9);

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
                .inputTag(forgeTag("circuits/silicon"), 1)
                .inputLegacyOre("wireFineGold", 1)
                .outputItem(item("circuit_chip"))
                .sourceOrder(29)
                .save(consumer, id("assembly_machine/chip"));

        GenericMachineRecipeBuilder.assembly("ass.chipBismoid", 100, 1_500)
                .inputItem(item("plate_polymer"), 2)
                .inputTag(forgeTag("circuits/silicon"), 2)
                .inputLegacyOre("nuggetAnyBismoid", 1)
                .inputLegacyOre("wireFineGold", 2)
                .outputItem(item("circuit_chip_bismoid"))
                .sourceOrder(30)
                .save(consumer, id("assembly_machine/chip_bismoid"));

        GenericMachineRecipeBuilder.assembly("ass.chipQuantum", 200, 15_000)
                .inputTag(forgeTag("ingots/any_hardplastic"), 2)
                .inputTag(forgeTag("dense_wires/bscco"), 1)
                .inputItem(item("pellet_charged"), 1)
                .inputLegacyOre("wireFineGold", 8)
                .outputItem(item("circuit_chip_quantum"))
                .sourceOrder(31)
                .save(consumer, id("assembly_machine/chip_quantum"));

        GenericMachineRecipeBuilder.assembly("ass.atomicClock", 300, 1_000)
                .inputTag(forgeTag("ingots/any_plastic"), 2)
                .inputTag(forgeTag("circuits/chip"), 3)
                .inputLegacyOre("dustStrontium", 1)
                .outputItem(item("circuit_atomic_clock"))
                .sourceOrder(32)
                .save(consumer, id("assembly_machine/atomic_clock"));

        GenericMachineRecipeBuilder.assembly("ass.analogAlt", 200, 250)
                .inputTag(forgeTag("circuits/pcb"), 4)
                .inputTag(forgeTag("ingots/any_plastic"), 4)
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
                .inputTag(forgeTag("ingots/any_plastic"), 4)
                .inputTag(forgeTag("circuits/chip"), 3)
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
                .inputTag(forgeTag("any/tar"), 4)
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
                .save(consumer, id("assembly_machine/satellite_base"));

        GenericMachineRecipeBuilder.assembly("ass.satellitemapper", 600, 100)
                .inputLegacyOre("shellSteel", 3)
                .inputItem(item("plate_desh"), 4)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 4)
                .inputItem(block("glass_quartz"), 8)
                .outputItem(item("sat_head_mapper"))
                .save(consumer, id("assembly_machine/satellite_mapper"));

        GenericMachineRecipeBuilder.assembly("ass.satellitescanner", 600, 100)
                .inputLegacyOre("shellSteel", 3)
                .inputLegacyOre("plateCastTitanium", 8)
                .inputItem(item("plate_desh"), 4)
                .inputItem(item("magnetron"), 8)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 8)
                .outputItem(item("sat_head_scanner"))
                .save(consumer, id("assembly_machine/satellite_scanner"));

        GenericMachineRecipeBuilder.assembly("ass.satelliteradar", 600, 100)
                .inputLegacyOre("shellSteel", 3)
                .inputLegacyOre("plateCastTitanium", 12)
                .inputItem(item("magnetron"), 12)
                .inputItem(ModItems.GOLD_COIL.get(), 16)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 4)
                .outputItem(item("sat_head_radar"))
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
                .save(consumer, id("assembly_machine/satellite_laser"));

        GenericMachineRecipeBuilder.assembly("ass.satelliteresonator", 600, 100)
                .inputLegacyOre("plateCastSteel", 6)
                .inputLegacyOre("ingotSaturnite", 12)
                .inputLegacyOre("ingotAnyPlastic", 48)
                .inputItem(item("crystal_xen"), 1)
                .inputLegacyMeta(LegacyMetaItemMappings.CIRCUIT, 9, 16)
                .outputItem(item("sat_head_resonator"))
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
                .save(consumer, id("assembly_machine/satellite_lunar_miner"));

        GenericMachineRecipeBuilder.arcWelder("arc.satellitemapper", 600, 10_000)
                .inputItem(item("sat_base"), 1)
                .inputItem(item("sat_head_mapper"), 1)
                .outputItem(ModItems.SAT_MAPPER.get())
                .save(consumer, id("arc_welder/satellite_mapper"));

        GenericMachineRecipeBuilder.arcWelder("arc.satellitescanner", 600, 10_000)
                .inputItem(item("sat_base"), 1)
                .inputItem(item("sat_head_scanner"), 1)
                .outputItem(ModItems.SAT_SCANNER.get())
                .save(consumer, id("arc_welder/satellite_scanner"));

        GenericMachineRecipeBuilder.arcWelder("arc.satelliteradar", 600, 10_000)
                .inputItem(item("sat_base"), 1)
                .inputItem(item("sat_head_radar"), 1)
                .outputItem(ModItems.SAT_RADAR.get())
                .save(consumer, id("arc_welder/satellite_radar"));

        GenericMachineRecipeBuilder.arcWelder("arc.satellitelaser", 600, 50_000)
                .inputItem(item("sat_base"), 1)
                .inputItem(item("sat_head_laser"), 1)
                .outputItem(ModItems.SAT_LASER.get())
                .save(consumer, id("arc_welder/satellite_laser"));

        GenericMachineRecipeBuilder.arcWelder("arc.satelliteresonator", 600, 50_000)
                .inputItem(item("sat_base"), 1)
                .inputItem(item("sat_head_resonator"), 1)
                .outputItem(ModItems.SAT_RESONATOR.get())
                .save(consumer, id("arc_welder/satellite_resonator"));
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

    private static void pyroOvenRecipes(Consumer<FinishedRecipe> consumer) {
        pyroSolidFuel(consumer, HbmFluids.SMEAR);
        pyroSolidFuel(consumer, HbmFluids.HEATINGOIL);
        pyroSolidFuel(consumer, HbmFluids.HEATINGOIL_VACUUM);
        pyroSolidFuel(consumer, HbmFluids.RECLAIMED);
        pyroSolidFuel(consumer, HbmFluids.PETROIL);
        pyroSolidFuel(consumer, HbmFluids.NAPHTHA);
        pyroSolidFuel(consumer, HbmFluids.NAPHTHA_CRACK);
        pyroSolidFuel(consumer, HbmFluids.DIESEL);
        pyroSolidFuel(consumer, HbmFluids.DIESEL_REFORM);
        pyroSolidFuel(consumer, HbmFluids.DIESEL_CRACK);
        pyroSolidFuel(consumer, HbmFluids.DIESEL_CRACK_REFORM);
        pyroSolidFuel(consumer, HbmFluids.LIGHTOIL);
        pyroSolidFuel(consumer, HbmFluids.LIGHTOIL_CRACK);
        pyroSolidFuel(consumer, HbmFluids.LIGHTOIL_VACUUM);
        pyroSolidFuel(consumer, HbmFluids.KEROSENE);
        pyroSolidFuel(consumer, HbmFluids.KEROSENE_REFORM);
        pyroSolidFuel(consumer, HbmFluids.SOURGAS);
        pyroSolidFuel(consumer, HbmFluids.REFORMGAS);
        pyroSolidFuel(consumer, HbmFluids.SYNGAS);
        pyroSolidFuel(consumer, HbmFluids.PETROLEUM);
        pyroSolidFuel(consumer, HbmFluids.LPG);
        pyroSolidFuel(consumer, HbmFluids.BIOFUEL);
        pyroSolidFuel(consumer, HbmFluids.AROMATICS);
        pyroSolidFuel(consumer, HbmFluids.UNSATURATEDS);
        pyroSolidFuel(consumer, HbmFluids.REFORMATE);
        pyroSolidFuel(consumer, HbmFluids.XYLENE);
        pyroSolidFuel(consumer, HbmFluids.BALEFIRE, 24_000_000L, item("solid_fuel_bf"));

        PyroOvenRecipeBuilder.pyro(100)
                .inputFluid(HbmFluids.STEAM, 250)
                .inputTag(forgeTag("gems/any_coke"), 1)
                .outputFluid(HbmFluids.SYNGAS, 1_000)
                .save(consumer, id("pyro_oven/syngas_from_coke"));
        PyroOvenRecipeBuilder.pyro(40)
                .inputTag(forgeTag("any/tar"), 4)
                .outputFluid(HbmFluids.CARBONDIOXIDE, 1_000)
                .outputItem(item("powder_ash_soot"))
                .save(consumer, id("pyro_oven/soot_from_tar"));
        PyroOvenRecipeBuilder.pyro(300)
                .inputFluid(HbmFluids.SYNGAS, 2_000)
                .inputTag(forgeTag("dusts/tungsten"), 1)
                .outputFluid(HbmFluids.SPENTSTEAM, 1_000)
                .outputItem(item("ingot_tungsten_carbide"))
                .save(consumer, id("pyro_oven/tungsten_carbide_from_syngas"));
    }

    private static void pyroSolidFuel(Consumer<FinishedRecipe> consumer, FluidType fluid) {
        pyroSolidFuel(consumer, fluid, 1_440_000L, item("solid_fuel"));
    }

    private static void pyroSolidFuel(Consumer<FinishedRecipe> consumer, FluidType fluid, long tuPerFuel,
            ItemLike fuel) {
        int amount = pyroAutoAmount(fluid, tuPerFuel);
        if (amount <= 0) {
            return;
        }
        PyroOvenRecipeBuilder.pyro(60)
                .inputFluid(fluid, amount)
                .outputItem(fuel)
                .save(consumer, id("pyro_oven/solid_fuel_from_" + fluid.toPath()));
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

    private static ItemLike legacyMetaItem(ResourceLocation legacyId, int legacyMeta) {
        return LegacyMetaItemMappings.requireItem(legacyId, legacyMeta).get();
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

        GenericMachineRecipeBuilder.assembly("chem.shellchlorine", 100, 1_000)
                .inputItem(item("ammo_arty"), 1)
                .inputTag(forgeTag("ingots/any_plastic"), 1)
                .inputFluid(HbmFluids.CHLORINE, 4_000)
                .outputItem(item("ammo_arty_chlorine"))
                .save(consumer, id("assembly_machine/shell_chlorine"));

        GenericMachineRecipeBuilder.assembly("ass.shellphosgene", 100, 1_000)
                .inputItem(item("ammo_arty"), 1)
                .inputTag(forgeTag("ingots/any_plastic"), 1)
                .inputFluid(HbmFluids.PHOSGENE, 4_000)
                .outputItem(item("ammo_arty_phosgene"))
                .save(consumer, id("assembly_machine/shell_phosgene"));

        GenericMachineRecipeBuilder.assembly("ass.shellmustard", 100, 1_000)
                .inputItem(item("ammo_arty"), 1)
                .inputTag(forgeTag("ingots/any_plastic"), 1)
                .inputFluid(HbmFluids.MUSTARDGAS, 4_000)
                .outputItem(item("ammo_arty_mustard_gas"))
                .save(consumer, id("assembly_machine/shell_mustard"));

        GenericMachineRecipeBuilder.assembly("ass.himarssmall", 100, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputLegacyOre("ingotAnyPlastic", 12)
                .inputItem(item("rocket_fuel"), 48)
                .inputLegacyOre("ingotAnyHighExplosive", 48)
                .inputTag(forgeTag("circuits/basic"), 6)
                .outputItem(item("ammo_himars_standard"))
                .save(consumer, id("assembly_machine/himarssmall"));

        GenericMachineRecipeBuilder.assembly("ass.himarssmallhe", 100, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputLegacyOre("ingotAnyPlastic", 24)
                .inputItem(item("rocket_fuel"), 48)
                .inputLegacyOre("ingotAnyPlasticExplosive", 18)
                .inputLegacyOre("ingotAnyHighExplosive", 48)
                .inputTag(forgeTag("circuits/basic"), 6)
                .outputItem(item("ammo_himars_standard_he"))
                .save(consumer, id("assembly_machine/himarssmallhe"));

        GenericMachineRecipeBuilder.assembly("ass.himarssmallwp", 100, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputLegacyOre("ingotAnyPlastic", 24)
                .inputItem(item("rocket_fuel"), 48)
                .inputItem(item("ingot_phosphorus"), 18)
                .inputLegacyOre("ingotAnyHighExplosive", 48)
                .inputTag(forgeTag("circuits/basic"), 6)
                .outputItem(item("ammo_himars_standard_wp"))
                .save(consumer, id("assembly_machine/himarssmallwp"));

        GenericMachineRecipeBuilder.assembly("ass.himarssmalltb", 100, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputLegacyOre("ingotAnyPlastic", 24)
                .inputItem(item("rocket_fuel"), 48)
                .inputItem(item("ball_tatb"), 32)
                .inputFluidContainer(HbmFluids.KEROSENE_REFORM, 1_000, 12)
                .inputFluidContainer(HbmFluids.PEROXIDE, 1_000, 12)
                .inputTag(forgeTag("circuits/basic"), 6)
                .outputItem(item("ammo_himars_standard_tb"))
                .save(consumer, id("assembly_machine/himarssmalltb"));

        GenericMachineRecipeBuilder.assembly("ass.himarssmallnuke", 100, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputLegacyOre("ingotAnyPlastic", 24)
                .inputItem(item("rocket_fuel"), 48)
                .inputItem(item("ball_tatb"), 6)
                .inputItem(item("nugget_pu239"), 12)
                .inputItem(item("neutron_reflector"), 12)
                .inputTag(forgeTag("circuits/basic"), 6)
                .outputItem(item("ammo_himars_standard_mini_nuke"))
                .save(consumer, id("assembly_machine/himarssmallnuke"));

        GenericMachineRecipeBuilder.assembly("ass.himarssmalllava", 100, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputTag(forgeTag("ingots/any_hardplastic"), 12)
                .inputItem(item("rocket_fuel"), 32)
                .inputItem(item("ball_tatb"), 4)
                .inputTag(forgeTag("gems/volcanic"), 1)
                .inputTag(forgeTag("circuits/basic"), 6)
                .outputItem(item("ammo_himars_standard_lava"))
                .save(consumer, id("assembly_machine/himarssmalllava"));

        GenericMachineRecipeBuilder.assembly("ass.himarslarge", 200, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputTag(forgeTag("ingots/any_hardplastic"), 12)
                .inputItem(item("rocket_fuel"), 36)
                .inputItem(item("ball_tatb"), 16)
                .inputTag(forgeTag("circuits/advanced"), 1)
                .outputItem(item("ammo_himars_single"))
                .save(consumer, id("assembly_machine/himarslarge"));

        GenericMachineRecipeBuilder.assembly("ass.himarslargetb", 200, 100)
                .inputLegacyOre("plateSteel", 24)
                .inputTag(forgeTag("ingots/any_hardplastic"), 12)
                .inputItem(item("rocket_fuel"), 36)
                .inputItem(item("ball_tatb"), 24)
                .inputFluidContainer(HbmFluids.KEROSENE_REFORM, 1_000, 16)
                .inputFluidContainer(HbmFluids.PEROXIDE, 1_000, 16)
                .inputTag(forgeTag("circuits/advanced"), 1)
                .outputItem(item("ammo_himars_single_tb"))
                .save(consumer, id("assembly_machine/himarslargetb"));
    }

    private static void legacyTurretRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.assembly("ass.turretchekhov", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 1, 1)
                .inputLegacyOre("ingotSteel", 16)
                .inputItem(ModItems.MOTOR.get(), 3)
                .inputTag(forgeTag("circuits/advanced"), 1)
                .inputLegacyOre("ntmpipeSteel", 3)
                .inputLegacyOre("gunMechanismGunMetal", 3)
                .inputItem(block("crate_iron"), 1)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_chekhov"))
                .save(consumer, id("assembly_machine/turret_chekhov"));

        GenericMachineRecipeBuilder.assembly("ass.turretfriendly", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 1, 1)
                .inputLegacyOre("ingotSteel", 16)
                .inputItem(ModItems.MOTOR.get(), 3)
                .inputTag(forgeTag("circuits/basic"), 1)
                .inputLegacyOre("ntmpipeSteel", 3)
                .inputLegacyOre("gunMechanismGunMetal", 1)
                .inputItem(block("crate_iron"), 1)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_friendly"))
                .save(consumer, id("assembly_machine/turret_friendly"));

        GenericMachineRecipeBuilder.assembly("ass.turretjeremy", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 1, 1)
                .inputLegacyOre("ingotSteel", 16)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputTag(forgeTag("circuits/advanced"), 1)
                .inputItem(item("motor_desh"), 1)
                .inputLegacyOre("shellSteel", 3)
                .inputLegacyOre("gunMechanismWeaponSteel", 3)
                .inputItem(block("crate_steel"), 1)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_jeremy"))
                .save(consumer, id("assembly_machine/turret_jeremy"));

        GenericMachineRecipeBuilder.assembly("ass.turrettauon", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 8, 1)
                .inputLegacyOre("ingotSteel", 16)
                .inputLegacyOre("ingotAnyPlastic", 4)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputTag(forgeTag("circuits/advanced"), 1)
                .inputItem(item("motor_desh"), 1)
                .inputLegacyOre("ingotCopper", 32)
                .inputLegacyOre("gunMechanismSaturnite", 3)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_tauon"))
                .save(consumer, id("assembly_machine/turret_tauon"));

        GenericMachineRecipeBuilder.assembly("ass.turretrichard", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 1, 1)
                .inputLegacyOre("ingotSteel", 16)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputTag(forgeTag("circuits/advanced"), 1)
                .inputLegacyOre("ingotAnyPlastic", 2)
                .inputLegacyOre("shellSteel", 8)
                .inputLegacyOre("gunMechanismWeaponSteel", 3)
                .inputItem(block("crate_steel"), 1)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_richard"))
                .save(consumer, id("assembly_machine/turret_richard"));

        GenericMachineRecipeBuilder.assembly("ass.turrethoward", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 1, 1)
                .inputLegacyOre("ingotSteel", 24)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputItem(item("motor_desh"), 2)
                .inputTag(forgeTag("circuits/advanced"), 3)
                .inputLegacyOre("ntmpipeSteel", 10)
                .inputLegacyOre("gunMechanismWeaponSteel", 3)
                .inputItem(block("crate_steel"), 1)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_howard"))
                .save(consumer, id("assembly_machine/turret_howard"));

        GenericMachineRecipeBuilder.assembly("ass.maxwell", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 8, 1)
                .inputLegacyOre("ingotSteel", 24)
                .inputItem(ModItems.MOTOR.get(), 2)
                .inputTag(forgeTag("circuits/advanced"), 2)
                .inputLegacyOre("ntmpipeSteel", 4)
                .inputLegacyOre("gunMechanismSaturnite", 3)
                .inputItem(item("magnetron"), 16)
                .inputLegacyOre("ingotAnyResistantAlloy", 8)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_maxwell"))
                .save(consumer, id("assembly_machine/turret_maxwell"));

        GenericMachineRecipeBuilder.assembly("ass.fritz", 200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 1, 1)
                .inputLegacyOre("ingotSteel", 16)
                .inputItem(ModItems.MOTOR.get(), 3)
                .inputTag(forgeTag("circuits/advanced"), 1)
                .inputLegacyOre("ntmpipeSteel", 8)
                .inputLegacyOre("gunMechanismGunMetal", 3)
                .inputItem(block("barrel_steel"), 1)
                .outputItem(block("turret_fritz"))
                .save(consumer, id("assembly_machine/turret_fritz"));

        GenericMachineRecipeBuilder.assembly("ass.arty", 1_200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 2, 1)
                .inputLegacyOre("ingotSteel", 64)
                .inputLegacyOre("ingotSteel", 64)
                .inputItem(item("motor_desh"), 5)
                .inputTag(forgeTag("circuits/advanced"), 3)
                .inputLegacyOre("ntmpipeSteel", 12)
                .inputLegacyOre("gunMechanismWeaponSteel", 16)
                .inputItem(block("machine_radar"), 1)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_arty"))
                .save(consumer, id("assembly_machine/turret_arty"));

        GenericMachineRecipeBuilder.assembly("ass.himars", 1_200, 100)
                .inputLegacyMeta(LegacyMetaItemMappings.BATTERY_PACK, 2, 1)
                .inputLegacyOre("ingotSteel", 64)
                .inputLegacyOre("ingotSteel", 64)
                .inputLegacyOre("ingotAnyPlastic", 64)
                .inputItem(item("motor_desh"), 5)
                .inputTag(forgeTag("circuits/advanced"), 8)
                .inputLegacyOre("gunMechanismSaturnite", 8)
                .inputItem(block("machine_radar"), 1)
                .inputItem(item("crt_display"), 1)
                .outputItem(block("turret_himars"))
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
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    private static TagKey<Item> forgeTag(String path) {
        return HbmItemTagsProvider.forgeItemTag(path);
    }

    private static TagKey<Item> vanillaTag(String path) {
        return TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", path));
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

        private static GenericMachineRecipeBuilder arcWelder(String internalName, int duration, long power) {
            return new GenericMachineRecipeBuilder(GenericMachineRecipe.Machine.ARC_WELDER,
                    id("arc_welder"), internalName, duration, power);
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

        private GenericMachineRecipeBuilder inputFluidContainer(FluidType fluid, int amount, int count) {
            return inputIngredient(HbmIngredient.fluidContainer(fluid, amount, count));
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
                    Optional.empty(),
                    Optional.empty());
            return this;
        }

        private GenericMachineRecipeBuilder fusionExtra(long ignitionTemp, long outputTemp, double outputFlux,
                float r, float g, float b) {
            this.extraData = new GenericMachineRecipeExtraData(
                    Optional.empty(),
                    Optional.of(new GenericMachineRecipeExtraData.Fusion(ignitionTemp, outputTemp,
                            outputFlux, r, g, b)),
                    Optional.empty());
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

        private PyroOvenRecipeBuilder inputFluid(FluidType fluid, int amount) {
            inputFluid = fluidStack(fluid, amount);
            return this;
        }

        private PyroOvenRecipeBuilder outputItem(ItemLike item) {
            outputItem = itemStackJson(new ItemStack(item));
            return this;
        }

        private PyroOvenRecipeBuilder outputFluid(FluidType fluid, int amount) {
            outputFluid = fluidStack(fluid, amount);
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
