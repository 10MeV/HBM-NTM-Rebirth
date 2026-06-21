package com.hbm.ntm.recipe;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public final class RotaryFurnaceRecipeRuntime {
    private static final List<Recipe> RECIPES = createRecipes();

    private RotaryFurnaceRecipeRuntime() {
    }

    public static List<Recipe> recipes() {
        return RECIPES;
    }

    @Nullable
    public static Recipe find(ItemStack first, ItemStack second, ItemStack third) {
        for (Recipe recipe : RECIPES) {
            if (recipe.matches(first, second, third)) {
                return recipe;
            }
        }
        return null;
    }

    private static List<Recipe> createRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(new Recipe(new MaterialStack(Mats.MAT_STEEL, MaterialShapes.INGOT.q(1)), 100, 100, null,
                List.of(IngredientSpec.tag(Tags.Items.INGOTS_IRON, 1), IngredientSpec.item(Items.COAL, 1))));
        recipes.add(new Recipe(new MaterialStack(Mats.MAT_STEEL, MaterialShapes.INGOT.q(1)), 100, 100, null,
                List.of(IngredientSpec.tag(Tags.Items.INGOTS_IRON, 1), IngredientSpec.anyLegacyItem(1,
                        "coke_coal", "coke_lignite", "coke_petroleum"))));
        recipes.add(new Recipe(new MaterialStack(Mats.MAT_DESH, MaterialShapes.INGOT.q(1)), 100, 200,
                new FluidInput(HbmFluids.LIGHTOIL, 100),
                List.of(IngredientSpec.legacyItem("powder_desh_ready", 1))));
        recipes.add(new Recipe(new MaterialStack(Mats.MAT_GUNMETAL, MaterialShapes.INGOT.q(4)), 200, 100, null,
                List.of(IngredientSpec.item(ModItems.COPPER_INGOT, 3),
                        IngredientSpec.item(ModItems.ALUMINIUM_INGOT, 1))));
        recipes.add(new Recipe(new MaterialStack(Mats.MAT_WEAPONSTEEL, MaterialShapes.INGOT.q(1)), 200, 400,
                new FluidInput(HbmFluids.GAS_COKER, 100),
                List.of(IngredientSpec.tag(tag("ingots/steel"), 1),
                        IngredientSpec.legacyItem("powder_flux", 2))));
        recipes.add(new Recipe(new MaterialStack(Mats.MAT_SATURN, MaterialShapes.INGOT.q(2)), 200, 400,
                new FluidInput(HbmFluids.REFORMGAS, 250),
                List.of(IngredientSpec.legacyItem("powder_dura_steel", 4),
                        IngredientSpec.item(ModItems.COPPER_POWDER, 1))));
        recipes.add(new Recipe(new MaterialStack(Mats.MAT_SATURN, MaterialShapes.INGOT.q(4)), 200, 300,
                new FluidInput(HbmFluids.REFORMGAS, 250),
                List.of(IngredientSpec.legacyItem("powder_dura_steel", 4),
                        IngredientSpec.item(ModItems.COPPER_POWDER, 1),
                        IngredientSpec.tag(tag("dusts/borax"), 1))));
        recipes.add(new Recipe(new MaterialStack(Mats.MAT_ALUMINIUM, MaterialShapes.INGOT.q(2)), 100, 400,
                new FluidInput(HbmFluids.SODIUM_ALUMINATE, 150), List.of()));
        recipes.add(new Recipe(new MaterialStack(Mats.MAT_ALUMINIUM, MaterialShapes.INGOT.q(3)), 40, 200,
                new FluidInput(HbmFluids.SODIUM_ALUMINATE, 150),
                List.of(IngredientSpec.legacyItem("powder_flux", 2))));
        return List.copyOf(recipes);
    }

    private static net.minecraft.tags.TagKey<Item> tag(String forgePath) {
        return net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                new net.minecraft.resources.ResourceLocation("forge", forgePath));
    }

    public record FluidInput(FluidType type, int amount) {
    }

    public record Recipe(MaterialStack output, int duration, int steam, @Nullable FluidInput fluid,
                         List<IngredientSpec> ingredients) {
        public Recipe {
            output = output == null ? null : output.copy();
            duration = Math.max(1, duration);
            steam = Math.max(0, steam);
            ingredients = ingredients == null ? List.of() : List.copyOf(ingredients);
        }

        public boolean matches(ItemStack first, ItemStack second, ItemStack third) {
            List<IngredientSpec> remaining = new ArrayList<>(ingredients);
            for (ItemStack stack : List.of(first, second, third)) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                boolean found = false;
                for (IngredientSpec ingredient : List.copyOf(remaining)) {
                    if (ingredient.matches(stack)) {
                        remaining.remove(ingredient);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return remaining.isEmpty();
        }
    }

    public static final class IngredientSpec {
        private final Item item;
        private final net.minecraft.tags.TagKey<Item> tag;
        private final List<String> legacyItems;
        private final int count;

        private IngredientSpec(@Nullable Item item, @Nullable net.minecraft.tags.TagKey<Item> tag,
                List<String> legacyItems, int count) {
            this.item = item;
            this.tag = tag;
            this.legacyItems = legacyItems == null ? List.of() : List.copyOf(legacyItems);
            this.count = Math.max(1, count);
        }

        public static IngredientSpec item(Item item, int count) {
            return new IngredientSpec(item, null, List.of(), count);
        }

        public static IngredientSpec item(RegistryObject<Item> item, int count) {
            return item(item.get(), count);
        }

        public static IngredientSpec legacyItem(String name, int count) {
            return anyLegacyItem(count, name);
        }

        public static IngredientSpec anyLegacyItem(int count, String... names) {
            return new IngredientSpec(null, null, names == null ? List.of() : List.of(names), count);
        }

        public static IngredientSpec tag(net.minecraft.tags.TagKey<Item> tag, int count) {
            return new IngredientSpec(null, tag, List.of(), count);
        }

        public boolean matches(ItemStack stack) {
            if (stack == null || stack.isEmpty() || stack.getCount() < count) {
                return false;
            }
            if (item != null) {
                return stack.is(item);
            }
            if (tag != null) {
                return stack.is(tag);
            }
            for (String name : legacyItems) {
                RegistryObject<Item> resolved = ModItems.legacyItem(name);
                if (resolved != null && stack.is(resolved.get())) {
                    return true;
                }
            }
            return false;
        }

        public int count() {
            return count;
        }

        public List<ItemStack> displayStacks() {
            if (item != null) {
                return List.of(new ItemStack(item, count));
            }
            if (tag != null) {
                return java.util.Arrays.stream(Ingredient.of(tag).getItems())
                        .map(stack -> stack.copyWithCount(count))
                        .filter(stack -> !stack.isEmpty())
                        .toList();
            }
            List<ItemStack> stacks = new ArrayList<>();
            for (String name : legacyItems) {
                RegistryObject<Item> resolved = ModItems.legacyItem(name);
                if (resolved != null) {
                    stacks.add(new ItemStack(resolved.get(), count));
                }
            }
            return List.copyOf(stacks);
        }
    }
}
