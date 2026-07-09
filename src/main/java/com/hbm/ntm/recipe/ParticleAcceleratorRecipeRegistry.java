package com.hbm.ntm.recipe;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ParticleAcceleratorRecipeRegistry {
    private static final Comparator<ParticleAcceleratorRecipe> RECIPE_ORDER = Comparator
            .comparingInt(ParticleAcceleratorRecipe::sourceOrder)
            .thenComparing(recipe -> recipe.getId().toString());
    private static final List<ParticleAcceleratorRecipe> FALLBACK_RECIPES = createFallbackRecipes();

    private ParticleAcceleratorRecipeRegistry() {
    }

    public static List<Recipe> recipes() {
        return recipes((RecipeManager) null);
    }

    public static List<Recipe> recipes(@Nullable Level level) {
        return recipes(level == null ? null : level.getRecipeManager());
    }

    public static List<Recipe> recipes(@Nullable RecipeManager recipeManager) {
        if (recipeManager != null) {
            return recipeManager.getAllRecipesFor(ModRecipes.PARTICLE_ACCELERATOR.type().get()).stream()
                    .sorted(RECIPE_ORDER)
                    .map(Recipe::fromDatapack)
                    .toList();
        }
        return FALLBACK_RECIPES.stream()
                .sorted(RECIPE_ORDER)
                .map(Recipe::fromDatapack)
                .toList();
    }

    @Nullable
    public static Recipe getOutput(@Nullable Level level, ItemStack input1, ItemStack input2) {
        for (Recipe recipe : recipes(level)) {
            if (recipe.matches(input1, input2)) {
                return recipe;
            }
        }
        return null;
    }

    @Nullable
    public static Recipe getOutput(ItemStack input1, ItemStack input2) {
        return getOutput(null, input1, input2);
    }

    private static List<ParticleAcceleratorRecipe> createFallbackRecipes() {
        List<ParticleAcceleratorRecipe> recipes = new ArrayList<>();
        add(recipes, "amat_from_hydrogen_copper", ingredient("particle_hydrogen"), ingredient("particle_copper"),
                300, stack("particle_amat"), ItemStack.EMPTY, 0);
        add(recipes, "aschrab_from_amat", ingredient("particle_amat"), ingredient("particle_amat"),
                400, stack("particle_aschrab"), ItemStack.EMPTY, 1);
        add(recipes, "dark_from_aschrab", ingredient("particle_aschrab"), ingredient("particle_aschrab"),
                10_000, stack("particle_dark"), ItemStack.EMPTY, 2);
        add(recipes, "muon_from_hydrogen_amat", ingredient("particle_hydrogen"), ingredient("particle_amat"),
                2_500, stack("particle_muon"), ItemStack.EMPTY, 3);
        add(recipes, "higgs_from_hydrogen_lead", ingredient("particle_hydrogen"), ingredient("particle_lead"),
                6_500, stack("particle_higgs"), ItemStack.EMPTY, 4);
        add(recipes, "tachyon_from_muon_higgs", ingredient("particle_muon"), ingredient("particle_higgs"),
                5_000, stack("particle_tachyon"), ItemStack.EMPTY, 5);
        add(recipes, "strange_from_muon_dark", ingredient("particle_muon"), ingredient("particle_dark"),
                12_500, stack("particle_strange"), ItemStack.EMPTY, 6);
        add(recipes, "sparkticle_from_strange_magic", ingredient("particle_strange"), ingredient("powder_magic"),
                12_500, stack("particle_sparkticle"), stack("dust"), 7);
        add(recipes, "digamma_from_sparkticle_higgs", ingredient("particle_sparkticle"),
                ingredient("particle_higgs"), 70_000, stack("particle_digamma"), ItemStack.EMPTY, 8);
        add(recipes, "degenerate_matter", ingredient("item_expensive_gold_dust"),
                HbmIngredient.legacyOre("ingotSchrabidium", 1), 10_000,
                stack("item_expensive_degenerate_matter"), ItemStack.EMPTY, 9);
        add(recipes, "chicken_nugget", HbmIngredient.of(Items.CHICKEN, 1), HbmIngredient.of(Items.CHICKEN, 1),
                100, stack("nugget"), stack("nugget"), 10);
        return List.copyOf(recipes);
    }

    private static void add(List<ParticleAcceleratorRecipe> recipes, String name, HbmIngredient input1,
            HbmIngredient input2, int momentum, ItemStack output1, ItemStack output2, int sourceOrder) {
        recipes.add(new ParticleAcceleratorRecipe(new ResourceLocation(HbmNtm.MOD_ID,
                "particle_accelerator/" + name), input1, input2, momentum, output1, output2, sourceOrder));
    }

    private static HbmIngredient ingredient(String name) {
        return HbmIngredient.of(item(name), 1);
    }

    private static ItemStack stack(String name) {
        return new ItemStack(item(name));
    }

    private static ItemLike item(String name) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        if (item == null) {
            throw new IllegalStateException("Missing legacy PA recipe item: " + name);
        }
        return item.get();
    }

    public record Recipe(HbmIngredient input1, HbmIngredient input2, int momentum, ItemStack output1,
                         ItemStack output2, int sourceOrder, ResourceLocation id) {
        public Recipe {
            if (input1 == null || input2 == null) {
                throw new IllegalArgumentException("Particle accelerator display recipe requires two inputs");
            }
            output1 = output1 == null ? ItemStack.EMPTY : output1.copy();
            if (output1.isEmpty()) {
                throw new IllegalArgumentException("Particle accelerator display recipe requires a primary output");
            }
            output2 = output2 == null ? ItemStack.EMPTY : output2.copy();
            momentum = Math.max(0, momentum);
        }

        public static Recipe fromDatapack(ParticleAcceleratorRecipe recipe) {
            return new Recipe(recipe.input1(), recipe.input2(), recipe.momentum(), recipe.output1(),
                    recipe.output2(), recipe.sourceOrder(), recipe.getId());
        }

        public boolean matches(ItemStack first, ItemStack second) {
            return matchesOrdered(first, second) || matchesOrdered(second, first);
        }

        private boolean matchesOrdered(ItemStack first, ItemStack second) {
            return first != null && second != null && !first.isEmpty() && !second.isEmpty()
                    && input1.test(first, true) && input2.test(second, true);
        }

        public ItemStack output1Stack() {
            return output1.copy();
        }

        public ItemStack output2Stack() {
            return output2.copy();
        }
    }
}
