package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.hbm.ntm.item.ItemPressStamp;
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
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class PressRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemPressStamp.StampType stampType;
    private final ItemStack result;

    public PressRecipe(ResourceLocation id, Ingredient input, ItemPressStamp.StampType stampType, ItemStack result) {
        this.id = id;
        this.input = input;
        this.stampType = stampType;
        this.result = result;
    }

    public ItemPressStamp.StampType getStampType() {
        return stampType;
    }

    @Override
    public boolean matches(Container container, Level level) {
        ItemStack inputStack = container.getItem(0);
        ItemStack stampStack = container.getItem(1);
        return input.test(inputStack)
                && stampStack.getItem() instanceof ItemPressStamp stamp
                && stamp.getStampType() == stampType;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input);
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_PRESS.get());
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.PRESS.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.PRESS.type().get();
    }

    public static class Serializer implements RecipeSerializer<PressRecipe> {
        @Override
        public PressRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            ItemPressStamp.StampType stampType = ItemPressStamp.StampType.byName(GsonHelper.getAsString(json, "stamp"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new PressRecipe(id, input, stampType, result);
        }

        @Nullable
        @Override
        public PressRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            ItemPressStamp.StampType stampType = buffer.readEnum(ItemPressStamp.StampType.class);
            ItemStack result = buffer.readItem();
            return new PressRecipe(id, input, stampType, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, PressRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeEnum(recipe.stampType);
            buffer.writeItem(recipe.result);
        }
    }
}
