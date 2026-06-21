package com.hbm.ntm.recipe;

import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Legacy two-slot anvil smithing recipes that are not datapack construction recipes.
 */
public final class AnvilSmithingRecipeRuntime {
    private static final List<SmithingRecipe> RECIPES = createRecipes();

    private AnvilSmithingRecipeRuntime() {
    }

    public static Optional<SmithingRecipe> match(Container input, int tier) {
        if (input == null || input.getContainerSize() < 2) {
            return Optional.empty();
        }
        ItemStack left = input.getItem(0);
        ItemStack right = input.getItem(1);
        if (left.isEmpty() || right.isEmpty()) {
            return Optional.empty();
        }
        return RECIPES.stream()
                .filter(recipe -> recipe.tier() <= tier && recipe.matches(left, right))
                .findFirst();
    }

    public static ItemStack result(Container input, int tier) {
        return match(input, tier)
                .map(SmithingRecipe::output)
                .orElse(ItemStack.EMPTY);
    }

    public static boolean consume(Container input, int tier) {
        Optional<SmithingRecipe> match = match(input, tier);
        if (match.isEmpty()) {
            return false;
        }
        SmithingRecipe recipe = match.get();
        input.removeItem(0, 1);
        input.removeItem(1, recipe.rightCount());
        input.setChanged();
        return true;
    }

    private static List<SmithingRecipe> createRecipes() {
        List<SmithingRecipe> recipes = new ArrayList<>();
        addAnvilUpgradeRecipes(recipes, ModBlocks.ANVIL_IRON);
        addAnvilUpgradeRecipes(recipes, ModBlocks.ANVIL_LEAD);
        return List.copyOf(recipes);
    }

    private static void addAnvilUpgradeRecipes(List<SmithingRecipe> recipes, RegistryObject<?> baseAnvil) {
        recipes.add(upgrade(baseAnvil, tag("steel"), 10, ModBlocks.ANVIL_STEEL));
        recipes.add(upgrade(baseAnvil, tag("desh"), 10, ModBlocks.ANVIL_DESH));
        recipes.add(upgrade(baseAnvil, tag("saturnite"), 10, ModBlocks.ANVIL_SATURNITE));
        recipes.add(upgrade(baseAnvil, legacyItem("ingot_ferrouranium"), 10, ModBlocks.ANVIL_FERROURANIUM));
        recipes.add(upgrade(baseAnvil, legacyItem("ingot_bismuth_bronze"), 10,
                ModBlocks.ANVIL_BISMUTH_BRONZE));
        recipes.add(upgrade(baseAnvil, legacyItem("ingot_arsenic_bronze"), 10,
                ModBlocks.ANVIL_ARSENIC_BRONZE));
        recipes.add(upgrade(baseAnvil, legacyItem("ingot_schrabidate"), 10, ModBlocks.ANVIL_SCHRABIDATE));
        recipes.add(upgrade(baseAnvil, tag("dineutronium"), 10, ModBlocks.ANVIL_DNT));
    }

    private static SmithingRecipe upgrade(RegistryObject<?> baseAnvil, TagKey<Item> right, int rightCount,
            RegistryObject<?> output) {
        return new SmithingRecipe(1, Ingredient.of(asItem(baseAnvil)), Ingredient.of(right), rightCount,
                new ItemStack(asItem(output)));
    }

    private static SmithingRecipe upgrade(RegistryObject<?> baseAnvil, Item right, int rightCount,
            RegistryObject<?> output) {
        return new SmithingRecipe(1, Ingredient.of(asItem(baseAnvil)), Ingredient.of(right), rightCount,
                new ItemStack(asItem(output)));
    }

    private static Item asItem(RegistryObject<?> object) {
        Object value = object.get();
        if (value instanceof Item item) {
            return item;
        }
        if (value instanceof net.minecraft.world.level.ItemLike itemLike) {
            return itemLike.asItem();
        }
        throw new IllegalStateException("Registry object is not item-like: " + object.getId());
    }

    private static Item legacyItem(String name) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        if (item == null) {
            throw new IllegalStateException("Missing legacy anvil smithing item: " + name);
        }
        return item.get();
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ingots/" + path));
    }

    public record SmithingRecipe(int tier, Ingredient left, Ingredient right, int rightCount, ItemStack output) {
        public SmithingRecipe {
            rightCount = Math.max(1, rightCount);
            output = output == null ? ItemStack.EMPTY : output.copy();
        }

        public boolean matches(ItemStack leftStack, ItemStack rightStack) {
            return left.test(leftStack) && right.test(rightStack) && rightStack.getCount() >= rightCount;
        }

        @Override
        public ItemStack output() {
            return output.copy();
        }
    }
}
