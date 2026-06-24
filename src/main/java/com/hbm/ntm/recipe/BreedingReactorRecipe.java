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

public class BreedingReactorRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmIngredient input;
    private final HbmItemOutput output;
    private final int flux;

    public BreedingReactorRecipe(ResourceLocation id, HbmIngredient input, HbmItemOutput output, int flux) {
        if (input == null || output == null || output.representativeStack().isEmpty()) {
            throw new IllegalArgumentException("Breeding reactor recipe requires non-empty input and output");
        }
        if (flux <= 0) {
            throw new IllegalArgumentException("Breeding reactor recipe flux must be positive");
        }
        this.id = id;
        this.input = input;
        this.output = output;
        this.flux = flux;
    }

    public HbmIngredient input() {
        return input;
    }

    public HbmItemOutput output() {
        return output;
    }

    public int flux() {
        return flux;
    }

    public boolean matches(ItemStack stack) {
        return input.test(stack, true);
    }

    public BreedingReactorRecipeRuntime.BreederRecipe asRuntimeRecipe() {
        return new BreedingReactorRecipeRuntime.BreederRecipe(output.representativeStack(), flux);
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
        return new ItemStack(ModBlocks.MACHINE_REACTOR_BREEDING.get());
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
        return ModRecipes.BREEDING_REACTOR.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.BREEDING_REACTOR.type().get();
    }

    public static class Serializer implements RecipeSerializer<BreedingReactorRecipe> {
        @Override
        public BreedingReactorRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmIngredient input = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            HbmItemOutput output = HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "output"));
            if (output.oneOf()) {
                throw new JsonSyntaxException("Breeding reactor recipe " + id + " requires a deterministic output");
            }
            int flux = GsonHelper.getAsInt(json, "flux");
            return new BreedingReactorRecipe(id, input, output, flux);
        }

        @Nullable
        @Override
        public BreedingReactorRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new BreedingReactorRecipe(id, HbmIngredient.fromNetwork(buffer),
                    HbmItemOutput.fromNetwork(buffer), buffer.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BreedingReactorRecipe recipe) {
            recipe.input.toNetwork(buffer);
            recipe.output.toNetwork(buffer);
            buffer.writeVarInt(recipe.flux);
        }
    }
}
