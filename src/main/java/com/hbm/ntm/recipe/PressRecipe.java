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
    private final HbmIngredient input;
    private final ItemPressStamp.StampType stampType;
    private final ItemStack result;
    private final int sourceOrder;

    public PressRecipe(ResourceLocation id, Ingredient input, ItemPressStamp.StampType stampType, ItemStack result,
            int sourceOrder) {
        this(id, HbmIngredient.of(input, 1), stampType, result, sourceOrder);
    }

    public PressRecipe(ResourceLocation id, HbmIngredient input, ItemPressStamp.StampType stampType, ItemStack result,
            int sourceOrder) {
        this.id = id;
        this.input = input;
        this.stampType = stampType;
        this.result = result;
        this.sourceOrder = sourceOrder;
    }

    public ItemPressStamp.StampType getStampType() {
        return stampType;
    }

    public HbmIngredient input() {
        return input;
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    @Override
    public boolean matches(Container container, Level level) {
        ItemStack inputStack = container.getItem(0);
        ItemStack stampStack = container.getItem(1);
        return input.test(inputStack, true)
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
        ingredients.add(input.ingredient());
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_PRESS.get());
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
        return ModRecipes.PRESS.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.PRESS.type().get();
    }

    public static class Serializer implements RecipeSerializer<PressRecipe> {
        @Override
        public PressRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmIngredient input = json.has("input")
                    ? HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input"))
                    : HbmIngredient.of(Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient")), 1);
            ItemPressStamp.StampType stampType = ItemPressStamp.StampType.byName(GsonHelper.getAsString(json, "stamp"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", Integer.MAX_VALUE);
            return new PressRecipe(id, input, stampType, result, sourceOrder);
        }

        @Nullable
        @Override
        public PressRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient input = HbmIngredient.fromNetwork(buffer);
            ItemPressStamp.StampType stampType = buffer.readEnum(ItemPressStamp.StampType.class);
            ItemStack result = buffer.readItem();
            int sourceOrder = buffer.readVarInt();
            return new PressRecipe(id, input, stampType, result, sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, PressRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeEnum(recipe.stampType);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.sourceOrder);
        }
    }
}
