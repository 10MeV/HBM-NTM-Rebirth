package com.hbm.ntm.recipe;

import com.hbm.ntm.HbmNtm;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, HbmNtm.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, HbmNtm.MOD_ID);

    public static final RegistryObject<RecipeSerializer<FluidDuctIdentifierRecipe>> FLUID_DUCT_IDENTIFIER =
            SERIALIZERS.register("fluid_duct_identifier",
                    () -> new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(
                            FluidDuctIdentifierRecipe::new));
    public static final RegistryObject<RecipeSerializer<RBMKFuelDisassemblyRecipe>> RBMK_FUEL_DISASSEMBLY =
            SERIALIZERS.register("rbmk_fuel_disassembly",
                    () -> new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(
                            RBMKFuelDisassemblyRecipe::new));
    public static final RecipeHolder<PressRecipe> PRESS = register("press", PressRecipe.Serializer::new);
    public static final RecipeHolder<GenericMachineRecipe> CHEMICAL_PLANT =
            register("chemical_plant", () -> new GenericMachineRecipe.Serializer(GenericMachineRecipe.Machine.CHEMICAL_PLANT));
    public static final RecipeHolder<GenericMachineRecipe> ASSEMBLY_MACHINE =
            register("assembly_machine", () -> new GenericMachineRecipe.Serializer(GenericMachineRecipe.Machine.ASSEMBLY_MACHINE));
    public static final RecipeHolder<GenericMachineRecipe> PUREX =
            register("purex", () -> new GenericMachineRecipe.Serializer(GenericMachineRecipe.Machine.PUREX));
    public static final RecipeHolder<GenericMachineRecipe> PRECASS =
            register("precass", () -> new GenericMachineRecipe.Serializer(GenericMachineRecipe.Machine.PRECASS));
    public static final RecipeHolder<GenericMachineRecipe> ARC_WELDER =
            register("arc_welder", () -> new GenericMachineRecipe.Serializer(GenericMachineRecipe.Machine.ARC_WELDER));
    public static final RecipeHolder<LiquefactionRecipe> LIQUEFACTION = register("liquefaction", LiquefactionRecipe.Serializer::new);
    public static final RecipeHolder<PyroOvenRecipe> PYRO_OVEN = register("pyro_oven", PyroOvenRecipe.Serializer::new);
    public static final RecipeHolder<CombinationOvenRecipe> COMBINATION_OVEN =
            register("combination_oven", CombinationOvenRecipe.Serializer::new);
    public static final RecipeHolder<BlastFurnaceRecipe> BLAST_FURNACE =
            register("blast_furnace", BlastFurnaceRecipe.Serializer::new);
    public static final RecipeHolder<MixerRecipe> MIXER =
            register("mixer", MixerRecipe.Serializer::new);
    public static final RecipeHolder<ItemProcessingRecipe> SHREDDER =
            register("shredder", () -> new ItemProcessingRecipe.Serializer(ItemProcessingRecipe.Machine.SHREDDER));
    public static final RecipeHolder<ItemProcessingRecipe> CENTRIFUGE =
            register("centrifuge", () -> new ItemProcessingRecipe.Serializer(ItemProcessingRecipe.Machine.CENTRIFUGE));
    public static final RecipeHolder<ItemProcessingRecipe> CRYSTALLIZER =
            register("crystallizer", () -> new ItemProcessingRecipe.Serializer(ItemProcessingRecipe.Machine.CRYSTALLIZER));
    public static final RecipeHolder<AmmoPressRecipe> AMMO_PRESS =
            register("ammo_press", AmmoPressRecipe.Serializer::new);
    public static final RecipeHolder<OutgasserRecipe> OUTGASSER =
            register("outgasser", OutgasserRecipe.Serializer::new);
    public static final RecipeHolder<ExposureChamberRecipe> EXPOSURE_CHAMBER =
            register("exposure_chamber", ExposureChamberRecipe.Serializer::new);
    public static final RecipeHolder<SolderingStationRecipe> SOLDERING_STATION =
            register("soldering_station", SolderingStationRecipe.Serializer::new);

    public static void register(IEventBus modBus) {
        RECIPE_TYPES.register(modBus);
        SERIALIZERS.register(modBus);
    }

    private static <T extends Recipe<Container>> RecipeHolder<T> register(String name, java.util.function.Supplier<RecipeSerializer<T>> serializer) {
        RegistryObject<RecipeType<T>> type = RECIPE_TYPES.register(name, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return HbmNtm.MOD_ID + ":" + name;
            }
        });
        RegistryObject<RecipeSerializer<T>> recipeSerializer = SERIALIZERS.register(name, serializer);
        return new RecipeHolder<>(type, recipeSerializer);
    }

    public record RecipeHolder<T extends Recipe<?>>(RegistryObject<RecipeType<T>> type,
                                                    RegistryObject<RecipeSerializer<T>> serializer) {
    }

    private ModRecipes() {
    }
}
