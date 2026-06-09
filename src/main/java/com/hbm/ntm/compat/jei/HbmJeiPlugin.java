package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import com.hbm.ntm.recipe.LiquefactionRecipe;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.PressRecipe;
import com.hbm.ntm.recipe.PyroOvenRecipe;
import com.hbm.ntm.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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
    public static final RecipeType<GenericMachineRecipe> PUREX =
            RecipeType.create(HbmNtm.MOD_ID, "purex", GenericMachineRecipe.class);
    public static final RecipeType<GenericMachineRecipe> PRECASS =
            RecipeType.create(HbmNtm.MOD_ID, "precass", GenericMachineRecipe.class);
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
    public static final RecipeType<PyroOvenRecipe> PYRO_OVEN =
            RecipeType.create(HbmNtm.MOD_ID, "pyro_oven", PyroOvenRecipe.class);
    public static final RecipeType<ItemProcessingRecipe> SHREDDER =
            RecipeType.create(HbmNtm.MOD_ID, "shredder", ItemProcessingRecipe.class);
    public static final RecipeType<ItemProcessingRecipe> CENTRIFUGE =
            RecipeType.create(HbmNtm.MOD_ID, "centrifuge", ItemProcessingRecipe.class);
    public static final RecipeType<ItemProcessingRecipe> CRYSTALLIZER =
            RecipeType.create(HbmNtm.MOD_ID, "crystallizer", ItemProcessingRecipe.class);

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
                        ModBlocks.MACHINE_CHEMICAL_PLANT.get(), guiHelper),
                new HbmMachineRecipeCategory(PUREX, GenericMachineRecipe.Machine.PUREX,
                        ModBlocks.MACHINE_PUREX.get(), guiHelper),
                new HbmMachineRecipeCategory(PRECASS, GenericMachineRecipe.Machine.PRECASS,
                        ModBlocks.MACHINE_ASSEMBLY_MACHINE.get(), guiHelper),
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
                new PyroOvenRecipeCategory(PYRO_OVEN, ModBlocks.MACHINE_PYROOVEN.get(), guiHelper),
                new ItemProcessingRecipeCategory(SHREDDER, ItemProcessingRecipe.Machine.SHREDDER,
                        ModBlocks.MACHINE_SHREDDER.get(), guiHelper),
                new ItemProcessingRecipeCategory(CENTRIFUGE, ItemProcessingRecipe.Machine.CENTRIFUGE,
                        ModBlocks.MACHINE_CENTRIFUGE.get(), guiHelper),
                new ItemProcessingRecipeCategory(CRYSTALLIZER, ItemProcessingRecipe.Machine.CRYSTALLIZER,
                        ModBlocks.MACHINE_CRYSTALLIZER.get(), guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipes(ASSEMBLY_MACHINE, sorted(recipeManager.getAllRecipesFor(ModRecipes.ASSEMBLY_MACHINE.type().get())));
        registration.addRecipes(CHEMICAL_PLANT, sorted(recipeManager.getAllRecipesFor(ModRecipes.CHEMICAL_PLANT.type().get())));
        registration.addRecipes(PUREX, sorted(recipeManager.getAllRecipesFor(ModRecipes.PUREX.type().get())));
        registration.addRecipes(PRECASS, sorted(recipeManager.getAllRecipesFor(ModRecipes.PRECASS.type().get())));
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
        registration.addRecipes(PYRO_OVEN, recipeManager.getAllRecipesFor(ModRecipes.PYRO_OVEN.type().get()));
        registration.addRecipes(SHREDDER, sortedItemProcessing(recipeManager.getAllRecipesFor(ModRecipes.SHREDDER.type().get())));
        registration.addRecipes(CENTRIFUGE, sortedItemProcessing(recipeManager.getAllRecipesFor(ModRecipes.CENTRIFUGE.type().get())));
        registration.addRecipes(CRYSTALLIZER, sortedItemProcessing(recipeManager.getAllRecipesFor(ModRecipes.CRYSTALLIZER.type().get())));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_ASSEMBLY_MACHINE.get()), ASSEMBLY_MACHINE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_ASSEMBLY_FACTORY.get()), ASSEMBLY_MACHINE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CHEMICAL_PLANT.get()), CHEMICAL_PLANT);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CHEMICAL_FACTORY.get()), CHEMICAL_PLANT);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_PUREX.get()), PUREX);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_ASSEMBLY_MACHINE.get()), PRECASS);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_ASSEMBLY_FACTORY.get()), PRECASS);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_PRESS.get()), PRESS);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_REFINERY.get()), REFINERY);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CATALYTIC_CRACKER.get()), CATALYTIC_CRACKER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CATALYTIC_REFORMER.get()), CATALYTIC_REFORMER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_VACUUM_DISTILL.get()), VACUUM_DISTILL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_FRACTION_TOWER.get()), FRACTION_TOWER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_HYDROTREATER.get()), HYDROTREATER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_COKER.get()), COKER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_SOLIDIFIER.get()), SOLIDIFIER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_LIQUEFACTOR.get()), LIQUEFACTION);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_PYROOVEN.get()), PYRO_OVEN);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_SHREDDER.get()), SHREDDER);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CENTRIFUGE.get()), CENTRIFUGE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_CRYSTALLIZER.get()), CRYSTALLIZER);
    }

    private static List<GenericMachineRecipe> sorted(List<GenericMachineRecipe> recipes) {
        return recipes.stream()
                .sorted(GenericMachineRecipe.LEGACY_ORDER)
                .toList();
    }

    private static List<ItemProcessingRecipe> sortedItemProcessing(List<ItemProcessingRecipe> recipes) {
        return recipes.stream()
                .sorted(java.util.Comparator.comparing(recipe -> recipe.getId().toString()))
                .toList();
    }
}
