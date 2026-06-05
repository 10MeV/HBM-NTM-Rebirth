package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public final class HbmJeiPlugin implements IModPlugin {
    public static final RecipeType<GenericMachineRecipe> ASSEMBLY_MACHINE =
            RecipeType.create(HbmNtm.MOD_ID, "assembly_machine", GenericMachineRecipe.class);
    public static final RecipeType<GenericMachineRecipe> CHEMICAL_PLANT =
            RecipeType.create(HbmNtm.MOD_ID, "chemical_plant", GenericMachineRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(HbmNtm.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new HbmMachineRecipeCategory(ASSEMBLY_MACHINE, GenericMachineRecipe.Machine.ASSEMBLY_MACHINE,
                        ModBlocks.MACHINE_ASSEMBLY_MACHINE.get(), guiHelper),
                new HbmMachineRecipeCategory(CHEMICAL_PLANT, GenericMachineRecipe.Machine.CHEMICAL_PLANT,
                        ModBlocks.MACHINE_CHEMICAL_PLANT.get(), guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipes(ASSEMBLY_MACHINE, sorted(recipeManager.getAllRecipesFor(ModRecipes.ASSEMBLY_MACHINE.type().get())));
        registration.addRecipes(CHEMICAL_PLANT, sorted(recipeManager.getAllRecipesFor(ModRecipes.CHEMICAL_PLANT.type().get())));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_ASSEMBLY_MACHINE.get()), ASSEMBLY_MACHINE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_ASSEMBLY_FACTORY.get()), ASSEMBLY_MACHINE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CHEMICAL_PLANT.get()), CHEMICAL_PLANT);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CHEMICAL_FACTORY.get()), CHEMICAL_PLANT);
    }

    private static List<GenericMachineRecipe> sorted(List<GenericMachineRecipe> recipes) {
        return recipes.stream()
                .sorted(GenericMachineRecipe.LEGACY_ORDER)
                .toList();
    }
}
