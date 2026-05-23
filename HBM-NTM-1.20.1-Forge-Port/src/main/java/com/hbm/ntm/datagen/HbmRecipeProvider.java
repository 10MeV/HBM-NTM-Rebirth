package com.hbm.ntm.datagen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.energy.HbmLegacyBatteryMaps;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

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
                .outputItem(legacyBatteryPack(1))
                .save(consumer, id("chemical_plant/batterylead"));

        GenericMachineRecipeBuilder.chemical("chem.batterylithium", 100, 1_000)
                .inputTag(forgeTag("dusts/lithium"), 12)
                .inputTag(forgeTag("dusts/cobalt"), 8)
                .inputTag(forgeTag("ingots/any_plastic"), 4)
                .inputFluid(HbmFluids.OXYGEN, 2_000)
                .outputItem(legacyBatteryPack(2))
                .save(consumer, id("chemical_plant/batterylithium"));

        GenericMachineRecipeBuilder.chemical("chem.batterysodium", 100, 10_000)
                .inputTag(forgeTag("dusts/sodium"), 24)
                .inputTag(forgeTag("dusts/iron"), 24)
                .inputTag(forgeTag("ingots/any_hardplastic"), 12)
                .outputItem(legacyBatteryPack(3))
                .save(consumer, id("chemical_plant/batterysodium"));

        GenericMachineRecipeBuilder.chemical("chem.batteryschrabidium", 100, 25_000)
                .inputTag(forgeTag("dusts/schrabidium"), 24)
                .inputTag(forgeTag("cast_plates/any_bismoid_bronze"), 8)
                .inputFluid(HbmFluids.HELIUM4, 8_000)
                .outputItem(legacyBatteryPack(4))
                .save(consumer, id("chemical_plant/batteryschrabidium"));

        GenericMachineRecipeBuilder.chemical("chem.batteryquantum", 100, 100_000)
                .inputTag(forgeTag("dense_wires/bscco"), 24)
                .inputItem(item("pellet_charged"), 32)
                .inputItem(item("ingot_cft"), 16)
                .inputFluid(HbmFluids.PERFLUOROMETHYL_COLD, 8_000)
                .outputItem(legacyBatteryPack(5))
                .outputFluid(HbmFluids.PERFLUOROMETHYL, 8_000)
                .save(consumer, id("chemical_plant/batteryquantum"));
    }

    private static void assemblyCapacitorRecipes(Consumer<FinishedRecipe> consumer) {
        GenericMachineRecipeBuilder.assembly("ass.capacitorgold", 100, 100)
                .inputItem(ModItems.STEEL_PLATE.get(), 8)
                .inputTag(forgeTag("dense_wires/gold"), 16)
                .outputItem(legacyBatteryPack(7))
                .save(consumer, id("assembly_machine/capacitorgold"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorniobium", 100, 1_000)
                .inputTag(forgeTag("ingots/any_plastic"), 12)
                .inputTag(forgeTag("dense_wires/niobium"), 24)
                .outputItem(legacyBatteryPack(8))
                .save(consumer, id("assembly_machine/capacitorniobium"));

        GenericMachineRecipeBuilder.assembly("ass.capacitortantalum", 100, 10_000)
                .inputTag(forgeTag("ingots/any_hardplastic"), 16)
                .inputTag(forgeTag("ingots/tantalum"), 24)
                .outputItem(legacyBatteryPack(9))
                .save(consumer, id("assembly_machine/capacitortantalum"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorbismuth", 100, 25_000)
                .inputTag(forgeTag("ingots/any_hardplastic"), 24)
                .inputTag(forgeTag("ingots/bismuth"), 24)
                .inputTag(forgeTag("circuits/chip_quantum"), 1)
                .outputItem(legacyBatteryPack(10))
                .save(consumer, id("assembly_machine/capacitorbismuth"));

        GenericMachineRecipeBuilder.assembly("ass.capacitorspark", 100, 100_000)
                .inputTag(forgeTag("cast_plates/combine_steel"), 12)
                .inputItem(item("powder_spark_mix"), 32)
                .inputItem(item("pellet_charged"), 32)
                .inputTag(forgeTag("circuits/chip_quantum"), 16)
                .inputFluid(HbmFluids.PERFLUOROMETHYL_COLD, 8_000)
                .outputItem(legacyBatteryPack(11))
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

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.FLUID_TANK_LEAD_EMPTY.get(), 4)
                .pattern("LUL")
                .pattern("LTL")
                .pattern("LUL")
                .define('L', ModItems.LEAD_PLATE.get())
                .define('U', ModItems.legacyItem("billet_u238").get())
                .define('T', ModItems.FLUID_TANK_EMPTY.get())
                .unlockedBy("has_lead_plate", has(ModItems.LEAD_PLATE.get()))
                .save(consumer, id("control/fluid_tank_lead_empty"));

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
                .requires(ModItems.CANISTER_FULL.get())
                .requires(Items.SLIME_BALL)
                .unlockedBy("has_canister_full", has(ModItems.CANISTER_FULL.get()))
                .save(consumer, id("blast_furnace/canister_napalm"));

        GenericMachineRecipeBuilder.assembly("ass.emptypackage", 40, 100)
                .inputItem(ModItems.TITANIUM_PLATE.get(), 4)
                .inputItem(ModItems.PLASTIC_BAG.get(), 2)
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
        return HbmLegacyBatteryMaps.batteryPackByLegacyMeta(legacyMeta)
                .orElseThrow(() -> new IllegalStateException("Missing legacy battery_pack meta: " + legacyMeta))
                .get();
    }

    private static ItemLike legacySelfChargingBattery(int legacyMeta) {
        return HbmLegacyBatteryMaps.selfChargingByLegacyMeta(legacyMeta)
                .orElseThrow(() -> new IllegalStateException("Missing legacy battery_sc meta: " + legacyMeta))
                .get();
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    private static TagKey<Item> forgeTag(String path) {
        return HbmItemTagsProvider.forgeItemTag(path);
    }

    private static final class GenericMachineRecipeBuilder {
        private final ResourceLocation serializerId;
        private final String internalName;
        private final int duration;
        private final long power;
        private final JsonArray inputItems = new JsonArray();
        private final JsonArray inputFluids = new JsonArray();
        private final JsonArray outputItems = new JsonArray();
        private final JsonArray outputFluids = new JsonArray();
        private final JsonArray pools = new JsonArray();
        @Nullable
        private String autoSwitchGroup;

        private GenericMachineRecipeBuilder(ResourceLocation serializerId, String internalName, int duration, long power) {
            this.serializerId = serializerId;
            this.internalName = internalName;
            this.duration = duration;
            this.power = power;
        }

        private static GenericMachineRecipeBuilder chemical(String internalName, int duration, long power) {
            return new GenericMachineRecipeBuilder(id("chemical_plant"), internalName, duration, power);
        }

        private static GenericMachineRecipeBuilder assembly(String internalName, int duration, long power) {
            return new GenericMachineRecipeBuilder(id("assembly_machine"), internalName, duration, power);
        }

        private GenericMachineRecipeBuilder inputItem(ItemLike item, int count) {
            return inputIngredient(Ingredient.of(item), count);
        }

        private GenericMachineRecipeBuilder inputItem(ItemStack stack) {
            return inputIngredient(Ingredient.of(stack), stack.getCount());
        }

        private GenericMachineRecipeBuilder inputTag(TagKey<Item> tag, int count) {
            return inputIngredient(Ingredient.of(tag), count);
        }

        private GenericMachineRecipeBuilder inputIngredient(Ingredient ingredient, int count) {
            JsonObject entry = new JsonObject();
            entry.add("ingredient", ingredient.toJson());
            entry.addProperty("count", count);
            inputItems.add(entry);
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
            JsonObject object = new JsonObject();
            object.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            if (stack.getCount() > 1) {
                object.addProperty("count", stack.getCount());
            }
            if (stack.hasTag() && !stack.getTag().isEmpty()) {
                object.addProperty("nbt", stack.getTag().toString());
            }
            outputItems.add(object);
            return this;
        }

        private GenericMachineRecipeBuilder outputFluid(FluidType fluid, int amount) {
            outputFluids.add(fluidStack(fluid, amount));
            return this;
        }

        private void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
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
                    if (autoSwitchGroup != null) {
                        json.addProperty("auto_switch_group", autoSwitchGroup);
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

        private static JsonObject fluidStack(FluidType fluid, int amount) {
            JsonObject object = new JsonObject();
            object.addProperty("fluid", new ResourceLocation(HbmNtm.MOD_ID, fluid.toPath()).toString());
            object.addProperty("amount", amount);
            return object;
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
