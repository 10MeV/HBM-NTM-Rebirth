package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FuelPoolRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmIngredient input;
    private final HbmItemOutput output;

    public FuelPoolRecipe(ResourceLocation id, HbmIngredient input, HbmItemOutput output) {
        if (input == null || output == null || output.representativeStack().isEmpty()) {
            throw new IllegalArgumentException("Fuel pool recipe requires non-empty input and output");
        }
        this.id = id;
        this.input = input;
        this.output = output;
    }

    public HbmIngredient input() {
        return input;
    }

    public HbmItemOutput output() {
        return output;
    }

    public boolean matches(ItemStack stack) {
        return input.test(stack, true);
    }

    public ItemStack cool() {
        return output.representativeStack();
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return output.representativeStack();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output.representativeStack();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input.ingredient());
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_WASTE_DRUM.get());
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FUEL_POOL.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.FUEL_POOL.type().get();
    }

    public static class Serializer implements RecipeSerializer<FuelPoolRecipe> {
        @Override
        public FuelPoolRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmIngredient input = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            HbmItemOutput output = HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "output"));
            if (output.oneOf()) {
                throw new JsonSyntaxException("Fuel pool recipe " + id + " requires a deterministic output");
            }
            return new FuelPoolRecipe(id, input, output);
        }

        @Nullable
        @Override
        public FuelPoolRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new FuelPoolRecipe(id, HbmIngredient.fromNetwork(buffer), HbmItemOutput.fromNetwork(buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, FuelPoolRecipe recipe) {
            recipe.input.toNetwork(buffer);
            recipe.output.toNetwork(buffer);
        }
    }
}
