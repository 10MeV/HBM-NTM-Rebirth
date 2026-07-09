package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
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

public class LemegetonRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmIngredient input;
    private final HbmItemOutput output;
    private final int sourceOrder;

    public LemegetonRecipe(ResourceLocation id, HbmIngredient input, HbmItemOutput output, int sourceOrder) {
        if (input == null) {
            throw new IllegalArgumentException("Lemegeton recipe needs an input");
        }
        if (output == null) {
            throw new IllegalArgumentException("Lemegeton recipe needs an output");
        }
        this.id = id;
        this.input = input;
        this.output = output;
        this.sourceOrder = sourceOrder;
    }

    public HbmIngredient input() {
        return input;
    }

    public HbmItemOutput output() {
        return output;
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && input.test(stack, true);
    }

    public ItemStack result() {
        return output.representativeStack();
    }

    @Override
    public boolean matches(Container container, Level level) {
        return container != null && container.getContainerSize() > 0 && matches(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input.ingredient());
        return ingredients;
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
        return ModRecipes.LEMEGETON.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.LEMEGETON.type().get();
    }

    public static class Serializer implements RecipeSerializer<LemegetonRecipe> {
        @Override
        public LemegetonRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmIngredient input = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            HbmItemOutput output = HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "output"));
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", Integer.MAX_VALUE);
            return new LemegetonRecipe(id, input, output, sourceOrder);
        }

        @Nullable
        @Override
        public LemegetonRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient input = HbmIngredient.fromNetwork(buffer);
            HbmItemOutput output = HbmItemOutput.fromNetwork(buffer);
            int sourceOrder = buffer.readVarInt();
            return new LemegetonRecipe(id, input, output, sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, LemegetonRecipe recipe) {
            recipe.input.toNetwork(buffer);
            recipe.output.toNetwork(buffer);
            buffer.writeVarInt(recipe.sourceOrder);
        }
    }
}
